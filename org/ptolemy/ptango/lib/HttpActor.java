/* An actor that handles an HttpRequest by producing an output and waiting for an input.

 Copyright (c) 1997-2012 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */

package org.ptolemy.ptango.lib;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.MicrostepDelay;
import ptolemy.actor.lib.io.FileReader;
import ptolemy.actor.util.Time;
import ptolemy.data.ArrayToken;
import ptolemy.data.LongToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/** An actor that handles an HTTP request by producing output,
 *  requesting a firing, and waiting for an input that provides a response.
 *  This actor requires that the model that contains it includes an
 *  instance of {@link WebServer}, which discovers this actor and
 *  delegates HTTP requests to a servlet that this actor creates.
 *  <p>
 *  The <i>path</i> parameter specifies which HTTP requests will
 *  be delegated to this actor. If the base URL for the web server
 *  is "http://localhost:8080", say, then request of the form
 *  "http://localhost:8080/<i>path</i>" will be delegated to this
 *  actor.
 *  <p>
 *  When this actor receives an HTTP request, if the web server is running
 *  (initialize() has been called and wrapup() has not), then this actor
 *  issues a request for the director to fire it at the greater of
 *  the current time of the director or the time elapsed since
 *  the last invocation of initialize() (in seconds).
 *  When the actor fires, it produces on its output ports the details
 *  of the request, time stamped by the elapsed time since the model
 *  started executing. It expects to be in a model that will fire
 *  it again again some time later
 *  with the response to HTTP request provided on its input ports.
 *  If that response does not arrive within <i>timeout</i>
 *  (default 10000) milliseconds, then it issues a timeout response.
 *  <p>
 *  This actor should be used in DE model and should be in a feedback
 *  loop, so that producing outputs causes inputs to appear.
 *  The downstream model should be used to construct a response.
 *  For example, to simply serve a web page, put a
 *  {@link FileReader} actor and a {@link MicrostepDelay}
 *  (to make the feedback loop work) downstream in a feedback
 *  loop connected back to the input.
 *
 *  @author Elizabeth Latronico and Edward A. Lee
 *  @version $Id$
 *  @since Ptolemy II 9.0
 *  @Pt.ProposedRating Red (ltrnc)
 *  @Pt.AcceptedRating Red (ltrnc)
 *  @see org.ptolemy.ptango.WebServer
 */
public class HttpActor extends TypedAtomicActor implements HttpService {

    /** Create an instance of the actor.
     *  @param container The container
     *  @param name The name.
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the super
     */
    public HttpActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        path = new StringParameter(this, "path");
        path.setExpression("/*");

        // Ports
        response = new TypedIOPort(this, "response", true, false);
        response.setTypeEquals(BaseType.STRING);
        response.setMultiport(true);
        new Parameter(response, "_showName").setExpression("true");

        getRequestURI = new TypedIOPort(this, "getRequestURI", false, true);
        getRequestURI.setTypeEquals(BaseType.STRING);
        new Parameter(getRequestURI, "_showName").setExpression("true");

        // NOTE: The type will be inferred from how this output is used.
        getParameters = new TypedIOPort(this, "getParameters", false, true);
        new Parameter(getParameters, "_showName").setExpression("true");

        getCookies = new TypedIOPort(this, "getCookies", false, true);
        new Parameter(getCookies, "_showName").setExpression("true");

        postRequestURI = new TypedIOPort(this, "postRequestURI", false, true);
        postRequestURI.setTypeEquals(BaseType.STRING);
        new Parameter(postRequestURI, "_showName").setExpression("true");

        // NOTE: The type will be inferred from how this output is used.
        postParameters = new TypedIOPort(this, "postParameters", false, true);
        new Parameter(postParameters, "_showName").setExpression("true");

        postCookies = new TypedIOPort(this, "postCookies", false, true);
        new Parameter(postCookies, "_showName").setExpression("true");

        setCookies = new TypedIOPort(this, "setCookies", true, false);
        new Parameter(setCookies, "_showName").setExpression("true");
        // For now, only allow RecordTokens.  In future, expand to other types
        // of tokens such as StringTokens representing JSON data.
        setCookies.setTypeEquals(BaseType.RECORD);

        // Parameters
        timeout = new Parameter(this, "timeout");
        timeout.setExpression("10000L");
        timeout.setTypeEquals(BaseType.LONG);

        requestedCookies = new Parameter(this, "requestedCookies");
        // Special constructor for ArrayToken of length 0
        requestedCookies.setToken(new ArrayToken(BaseType.STRING));
        requestedCookies.setTypeEquals(new ArrayType(BaseType.STRING));

        // Internal variables
        _cookieCollection = new Hashtable<String, Token>();
        _hasNewCookies = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** An output port that sends the cookies provided by a get request.
     *  The output provided will be a RecordToken with the names given by
     *  If there are any cookies, then the output provided by this
     *  port will be an ArrayToken of RecordTokens of the form
     *  {cookieName = value, ...}.
     */

    public TypedIOPort getCookies;

    /** An output port that sends parameters included in a get request.
     *  These are values appended to the URL in the form
     *  of ...?name=value. The output will be a record with
     *  one field for each name. If the request assigns multiple
     *  values to the same name, then the field value of the record
     *  will be an array of strings. Otherwise, it will simply
     *  be a string.
     */
    public TypedIOPort getParameters;

    /** An output port that sends the relative URI of a get request,
     *  which must match the pattern given by the <i>path</i> parameter.
     *  This has type string.
     */
    public TypedIOPort getRequestURI;

    /** The relative URL of HTTP requests that this actor handles.
     *  This is a string that defaults to "/*", meaning that all
     *  requests are handled, unless there is another instance
     *  of the actor with a more specific path that matches.
     *  Preference is given to longer paths. So, for example,
     *  a request "http://localhost:8080/foo/bar" will be
     *  delegated to an actor with <i>path</i> = "/foo/bar/*",
     *  if there is one, and otherwise to an actor with
     *  <i>path</i> = "/foo/*", if there is one, and finally
     *  to an actor with <i>path</i> = "/*", if the first two
     *  don't exist.  If two actors specify the same path,
     *  then it is undefined which one gets the request.
     */
    public StringParameter path;

    /** An output that sends the cookies provided by a post request.
     *  The output provided will be a RecordToken with the names given by
     *  If there are any cookies, then the output provided by this
     *  port will be an ArrayToken of RecordTokens of the form
     *  {cookieName = value, ...}.
     */

    public TypedIOPort postCookies;

    /** An output port that sends parameters included in a post request.
     *  The output will be a record with
     *  one field for each name. If the request assigns multiple
     *  values to the name, then the field value of the record
     *  will be an array of strings. Otherwise, it will simply
     *  be a string.
     */
    public TypedIOPort postParameters;

    /** An output port that sends the relative URI of a post request,
     *  which must match the pattern given by the <i>path</i> parameter.
     *  This has type string.
     */
    public TypedIOPort postRequestURI;

    /** A parameter storing a list of names that HttpActor should output the
     *  cookie values for.  This is needed so that the receivedCookies output
     *  port will always have the same type for that particular model, to
     *  avoid runtime type checking errors.  Note that if the value of this
     *  parameter changes during runtime, a runtime type checking error may
     *  occur.
     */
    public Parameter requestedCookies;

    /** An input port on which to provide the
     *  response to issue to an HTTP request. When this input port
     *  receives an event, if there is a pending get or post request from
     *  a web server, then that pending request responds with the
     *  value of the input. Otherwise, the response is recorded,
     *  and the next get or post request received will be given the response.
     *  For convenience, this is a multiport, and a response can be
     *  provided on input channel. If multiple responses are provided,
     *  the last one will prevail.
     */
    public TypedIOPort response;

    /**
     * An input port on which to provide new cookies and/or new cookie values.
     * These will be set in the HttpResponse.
     * The token should be a RecordToken or an ArrayToken of RecordTokens
     */
    public TypedIOPort setCookies;

    /** The time in milliseconds to wait after producing the details
     *  of a request on the output ports for a response to appear at
     *  the input ports. This is a long that defaults to 10,000.
     *  If this time expires before an input is received, then this actor
     *  will issue a generic timeout response to the HTTP request.
     */
    public Parameter timeout;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute.  In this case, check the
     *  value of the <i>path</i> attribute to make sure it is a valid URI.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (not thrown in this base class).
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == path) {
            String pathValue = ((StringToken) path.getToken()).stringValue();
            try {
                if (!pathValue.trim().equals("")) {
                    _URIpath = URI.create(pathValue);
                } else {
                    _URIpath = URI.create("/*");
                }
            } catch (IllegalArgumentException e2) {
                throw new IllegalActionException(this,
                        "Path is not a valid URI: " + pathValue);
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor.
     *  @param workspace The workspace in which to place the cloned attribute.
     *  @exception CloneNotSupportedException Not thrown in this base class.
     *  @see java.lang.Object#clone()
     *  @return The cloned attribute.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        HttpActor newObject = (HttpActor) super.clone(workspace);
        // FIXME:  Need to copy the _cookiesCollectionList from the old object?
        newObject._cookieCollection = new Hashtable<String, Token>();
        newObject._hasNewCookies = false;
        newObject._initializeModelTime = null;
        newObject._initializeRealTime = 0L;
        newObject._parameters = null;
        newObject._requestURI = null;
        newObject._response = null;
        newObject._URIpath = null;
        return newObject;
    }

    /** Return the relative path that this HttpService is mapped to,
     *  which is the value of the <i>path</i> parameter.
     *  This method is required by the HttpService interface.
     *  @return The relative path that this HttpService is mapped to.
     *  @see #setRelativePath(URI)
     */
    public URI getRelativePath() {
        return _URIpath;
    }

    /** Create and return an HttpServlet that is used to handle requests that
     *  arrive at the path given by the <i>path</i> parameter.
     *  This method is required by the HttpService interface.
     *  @return An HttpServlet to handle requests.
     */
    public HttpServlet getServlet() {
        if (_debugging) {
            _debug("Creating new servlet.");
        }
        // The relative path for the servlet is calculated in preinitialize
        // since the path might not be a valid URI and could throw an exception
        // The getServlet() method does not throw an exception
        return new ActorServlet();
    }

    /** Respond to an HTTP request. If there is a
     *  response at the input port, then record that
     *  response and notify the servlet thread that a response
     *  is ready. Otherwise, if the servlet has received
     *  an HTTP request, then produce on the output ports
     *  the details of the request.
     *  @exception IllegalActionException If sending the
     *   outputs fails.
     */
    public void fire() throws IllegalActionException {
        // The methods of the servlet are invoked in another
        // thread, so we use this actor for mutual exclusion.
        synchronized (this) {
            super.fire();

            boolean handledCookies = false;

            for (int i = 0; i < response.getWidth(); i++) {
                if (response.hasToken(i)) {
                    _response = ((StringToken) response.get(i)).stringValue();
                    if (_debugging) {
                        _debug("Received response on the input port: "
                                + _response);
                    }

                    // Check for new cookies on the setCookies port
                    // Only check this when the actor is ready to produce a
                    // response.  This will ensure at most one token is consumed
                    // from the setCookies port per response
                    // If there are multiple tokens on the response port (since
                    // it is a multiport), read at most one token from the
                    // setCookies port per firing
                    if (!handledCookies && setCookies.getWidth() > 0
                            && setCookies.hasToken(0)) {
                        RecordToken cookieToken = (RecordToken) setCookies
                                .get(0);
                        _hasNewCookies = true;
                        _updateCookieCollection(cookieToken);
                        handledCookies = true;
                    }

                    // If there is a pending request, notify it.
                    notifyAll();
                }
            }
            // If there is a pending request, produce outputs for that request,
            // including any cookies from that request.
            if (_requestURI != null) {
                if (_requestType == 0) {
                    getRequestURI.send(0, new StringToken(_requestURI));
                    getParameters.send(0, _parameters);
                } else {
                    postRequestURI.send(0, new StringToken(_requestURI));
                    postParameters.send(0, _parameters);

                }

                // Set to null so each request is only handled once, since this
                // actor will fire again when a response is available on its
                // response input port
                _requestURI = null;

                // Roxana: Send the values in the cookieCollection to the
                // receivedCookies output port
                // Cookies are sent for both get and post requests
                RecordToken cookies = new RecordToken();

                ArrayList<String> names = new ArrayList<String>();
                ArrayList<Token> values = new ArrayList<Token>();

                if (_cookieCollection == null) {
                    _cookieCollection = new Hashtable();
                }

                // Send cookies as named in the requestedCookies parameter
                // as a single RecordToken
                // If a cookie with the given name does not exist,
                // send a StringToken with the empty string for
                // that cookie's value
                // If no cookies are requested, send a blank RecordToken
                String name;
                // Default value is the empty string
                Token value = new StringToken(null);

                if (requestedCookies != null
                        && requestedCookies.getToken() != null) {
                    // The requestedCookies parameter contains an
                    // ArrayToken of StringTokens
                    if (requestedCookies.getToken() instanceof ArrayToken) {
                        ArrayToken cookieNames = (ArrayToken) requestedCookies
                                .getToken();
                        for (int i = 0; i < cookieNames.length(); i++) {
                            if (cookieNames.getElement(i) instanceof StringToken) {
                                name = ((StringToken) cookieNames.getElement(i))
                                        .stringValue();
                                if (name != null && !name.isEmpty()
                                        && _cookieCollection.get(name) != null) {
                                    value = _cookieCollection.get(name);
                                }

                                names.add(name);
                                values.add(value);
                            }
                        }
                    }
                }

                cookies = new RecordToken(
                        names.toArray(new String[names.size()]),
                        values.toArray(new Token[values.size()]));
                if (_requestType == 0) {
                    getCookies.send(0, cookies);
                } else {
                    postCookies.send(0, cookies);
                }

            }
        }
    }

    /** Record the current model time and the current real time
     *  so that output events can be time stamped with the elapsed
     *  time since model start.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _response = null;
        _requestURI = null;
        _initializeModelTime = getDirector().getModelTime();
        _initializeRealTime = System.currentTimeMillis();
        _cookieCollection = new Hashtable<String, Token>();
        _hasNewCookies = false;

    }

    /** Set the relative path that this HttpService is mapped to.
     *  This method is required by the HttpService interface.
     *  @param path The relative path that this HttpService is mapped to.
     *  @see #getRelativePath()
     */
    public void setRelativePath(URI path) {
        _URIpath = path;
    }

    /** Specify the web server for this service. This will
     *  be called by the {@link WebServer} attribute of a model,
     *  if there is one, and will enable this service to access
     *  information about the web server (such as
     *  the resourcePath, resourceLocation, or temporaryFileLocation).
     */
    public void setWebServer(WebServer server) {
        // Ignore. This actor doesn't need to know.
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** All cookies from the setCookies port plus the Cookies from the
     *  HttpRequest.  Values provided on the setCookies port override values
     *  from the HttpRequest for cookies with the same name.  (I.e., the model
     *  developer wants to replace the cookie on the client with the cookie
     *  provided on the input port.)
     */
    private Hashtable<String, Token> _cookieCollection;

    /** A variable indicating that new cookies have been received on the
     * setCookies port, and that all cookies should be written to the
     * HttpServletResponse in the doGet() or doPost() method.
     * It would not be wrong to always write the cookies to every
     * HttpServletResponse, but it would be inefficient.
     */
    private boolean _hasNewCookies;

    /** The model time at which this actor was last initialized. */
    private Time _initializeModelTime;

    /** The real time at which this actor was last initialized, in milliseconds. */
    private long _initializeRealTime;

    /** Parameters received in a get or post. */
    private RecordToken _parameters;

    /** The type of request. 0 for get, 1 for put. */
    private int _requestType;

    /** The URI issued in the get request. */
    private String _requestURI;

    /** The response to issue to a pending or next get request. */
    private String _response;

    /** The URI for the relative path from the "path" parameter.
     *  A URI is used here to make sure the "path" parameter conforms to
     *  all of the URI naming conventions.
     */
    private URI _URIpath;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Take the list of name, value pairs from the cookies RecordToken and
     *  update the _cookieCollection Hashtable.
     *
     * @param cookies The RecordToken containing name, value pairs to
     * add to _cookieCollection or update _cookieCollection with
     */
    private void _updateCookieCollection(RecordToken cookies) {
        if (cookies != null) {
            Iterator iterator = cookies.labelSet().iterator();
            while (iterator.hasNext()) {
                String label = (String) iterator.next();
                _cookieCollection.put(label, cookies.get(label));
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** A servlet providing implementations of get and post.
     *  The way this servlet works is that when a get or post
     *  HTTP request is received, it records the properties of
     *  the request (the URI and parameters) and requests a
     *  firing of the enclosing actor at model time equal to
     *  the time elapsed since the start of execution of the model.
     *  The thread making the get or post request then suspends,
     *  giving the model a chance to execute. When the model has
     *  determined what the response to the request should be,
     *  it notifies this servlet thread, which then completes
     *  the handling of the request, sending back the response
     *  that has been provided by the enclosing actor.
     *
     *  See <a href"http://wiki.eclipse.org/Jetty/Tutorial/Embedding_Jetty">http://wiki.eclipse.org/Jetty/Tutorial/Embedding_Jetty</a>
     */
    protected class ActorServlet extends HttpServlet {

        /** Handle an HTTP get request by creating a web page as the HTTP
         *  response.
         *  NOTE: This method is synchronized, and the lock is _not_ released
         *  while waiting for the response. This strategy helps prevent a pending
         *  get request from overlapping with a pending put request. The second
         *  request will be postponed until the first has been completely handled.
         *
         *  @param request The HTTP get request.
         *  @param response The HTTP response to write to.
         *  @exception ServletException  If there is a problem reading from the
         *  servlet request or other servlet problem
         *  @exception IOException  If there is a problem writing to the servlet
         *  response
         */
        protected synchronized void doGet(HttpServletRequest request,
                HttpServletResponse response) throws ServletException,
                IOException {
            _handleRequest(request, response, 0);
        }

        /** Handle an HTTP post request by creating a web page as the HTTP
         *  response.
         *  NOTE: This method is synchronized, and the lock is _not_ released
         *  while waiting for the response. This strategy helps prevent a pending
         *  get request from overlapping with a pending put request. The second
         *  request will be postponed until the first has been completely handled.
         *  @param request The HTTP get request.
         *  @param response The HTTP response to write to.
         *  @exception ServletException  If there is a problem reading from the
         *  servlet request or other servlet problem
         *  @exception IOException  If there is a problem writing to the servlet
         *  response
         */
        protected synchronized void doPost(HttpServletRequest request,
                HttpServletResponse response) throws ServletException,
                IOException {
            _handleRequest(request, response, 1);
        }

        /** Handle an HTTP get or post request by creating a web page as the HTTP
         *  response.
         *  @see http://docs.oracle.com/javaee/6/api/javax/servlet/http/HttpServletRequest.html
         *  @param request The HTTP request.
         *  @param response The HTTP response to write to.
         *  @param type The type of request. 0 for get, 1 for post.
         *  @exception ServletException  If there is a problem reading from the
         *  servlet request or other servlet problem
         *  @exception IOException  If there is a problem writing to the servlet
         *  response
         */
        private void _handleRequest(HttpServletRequest request,
                HttpServletResponse response, int type)
                throws ServletException, IOException {
            // The following codeblock is synchronized on the enclosing
            // actor. This lock _is_ released while waiting for the response,
            // allowing the fire method to execute its own synchronized blocks.
            synchronized (HttpActor.this) {
                _requestURI = request.getRequestURI();
                _requestType = type;

                // Read cookies from the request and store in _cookiesCollection
                _readCookies(request);

                // Get the parameters that have been either posted or included
                // as assignments in a get using the URL syntax ...?name=value.
                // Note that each parameter name may have more than one value,
                // hence the array of strings.

                // FIXME:  This currently treats all values as StringTokens,
                // which they may not be.  How to either 1) correctly determine
                // the type of the token, or 2) create a token of unspecified
                // type but still be able to set its expression?  Class
                // Token does not have the setExpression() method
                Map<String, String[]> parameterMap = request.getParameterMap();
                // Convert these parameters to a record token to send to
                // the appropriate output.
                Set<String> names = parameterMap.keySet();
                String[] fieldNames = new String[names.size()];
                Token[] fieldValues = new Token[names.size()];
                int i = 0;
                for (String name : names) {
                    fieldNames[i] = name;
                    String[] values = parameterMap.get(name);
                    if (values == null || values.length == 0) {
                        fieldValues[i] = StringToken.NIL;
                    } else if (values.length == 1) {
                        fieldValues[i] = new StringToken(values[0]);
                    } else {
                        // More than one value. Use an array.
                        StringToken[] arrayEntries = new StringToken[values.length];
                        for (int j = 0; j < values.length; j++) {
                            arrayEntries[j] = new StringToken(values[j]);
                        }
                        try {
                            ArrayToken array = new ArrayToken(arrayEntries);
                            fieldValues[i] = array;
                        } catch (IllegalActionException e) {
                            _writeError(response,
                                    HttpServletResponse.SC_BAD_REQUEST,
                                    e.getMessage());
                            return;
                        }
                    }
                    i++;
                }
                try {
                    _parameters = new RecordToken(fieldNames, fieldValues);

                } catch (IllegalActionException e1) {
                    _writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                            e1.getMessage());
                }

                try {
                    long elapsedRealTime = System.currentTimeMillis()
                            - _initializeRealTime;
                    Time timeOfRequest = _initializeModelTime
                            .add(elapsedRealTime);
                    // Note that fireAt() will modify the requested firing time if it is in the past.
                    getDirector().fireAt(HttpActor.this, timeOfRequest);
                } catch (IllegalActionException e) {
                    _writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                            e.getMessage());
                    return;
                }
                // Wait for a response.
                boolean timeoutOccurred = false;
                while (_response == null) {
                    if (_debugging) {
                        _debug("Waiting for a response");
                    }
                    try {
                        // Timeout after 10 seconds.
                        // Unfortunately, we can't tell whether the timeout
                        // occurred unless we record the current time.
                        long startTime = System.currentTimeMillis();
                        long timeoutValue = 10000L;
                        try {
                            timeoutValue = ((LongToken) timeout.getToken())
                                    .longValue();
                        } catch (IllegalActionException e) {
                            // Ignore and use default.
                        }
                        HttpActor.this.wait(timeoutValue);
                        if (System.currentTimeMillis() - startTime >= timeoutValue) {
                            timeoutOccurred = true;
                            break;
                        }
                    } catch (InterruptedException e) {
                        break;
                    }
                }

                response.setContentType("text/html");
                response.setStatus(HttpServletResponse.SC_OK);

                // Write all cookies to the response, if there are some new
                // cookies to write
                if (_hasNewCookies) {
                    _writeCookies(response);
                }
                if (timeoutOccurred) {
                    if (_debugging) {
                        _debug("Get request timed out.");
                    }
                    response.getWriter()
                            .println("<h1> Request timed out </h1>");
                } else if (_response == null) {
                    if (_debugging) {
                        _debug("Get request thread interrupted.");
                    }
                    response.getWriter().println(
                            "<h1> Get request thread interrupted </h1>");
                } else {
                    if (_debugging) {
                        _debug("Responding to get request: " + _response);
                    }
                    response.getWriter().println(_response);
                }
                _response = null;
            }
        }

        /** Read the Cookies from the HttpServletRequest, and store them in
         * the _cookiesCollectionList as an array of RecordTokens, overwriting
         * any the values of any current Cookies with the same name.
         *
         * @param request  The HttpServletRequest to read Cookies from.  The
         * HttpServletRequest can be of any type - i.e. both GET and POST
         * requests are allowed to have Cookies.
         */
        private void _readCookies(HttpServletRequest request) {

            // Clear _cookieCollection here
            // This is needed since there may be multiple HttpActors in the
            // model (for example, one might handle creating cookies,
            // /cookies/create  and another might handle deleting cookies
            // /cookie/delete
            _cookieCollection.clear();

            if (request.getCookies().length > 0) {
                for (Cookie cookie : request.getCookies()) {
                    // Cookie must have a name
                    if (cookie.getName() != null && !cookie.getName().isEmpty()) {
                        // FIXME:  Determine the correct data type of the value
                        // Possible to re-use JSONToToken code?
                        // Right now, generate StringToken for all cookies

                        // FIXME:  Can cookies contain an array?  If so, is
                        // there a standard representation for the array?
                        // Have seen inconsistent formats on the internet...

                        // TODO: Cookies have many other fields besides the
                        // value. Should we include them?
                        if (cookie.getValue() != null
                                && !cookie.getValue().isEmpty()) {
                            _cookieCollection.put(cookie.getName(),
                                    new StringToken(cookie.getValue()));
                        } else {
                            // Set the value to the empty string
                            _cookieCollection.put(cookie.getName(),
                                    new StringToken(null));
                        }
                    }
                }
            }
        }

        /** Write all cookies in _cookiesCollection to the HttpResponse.
         *
         * @param response The HttpServletResponse to write the cookies to.
         */
        private void _writeCookies(HttpServletResponse response) {

            // TODO:  Allow permanent cookies.  Current implementation produces
            // session cookies.  Session cookies are stored in
            // the browser's memory, and are erased when the browser is closed
            // http://www.javascriptkit.com/javatutors/cookie.shtml
            Iterator cookieIterator = _cookieCollection.keySet().iterator();
            while (cookieIterator.hasNext()) {
                String label = (String) cookieIterator.next();
                String value = "";
                if (_cookieCollection.get(label) instanceof StringToken) {
                    value = ((StringToken) _cookieCollection.get(label))
                            .stringValue();
                }

                // Clear the cookie if the value is empty
                // TODO:  Should there be an explicit clear cookie port?
                // Is there a scenario where we want the cookie to exist,
                // but to have an empty string for the value?
                if (value == null || value.isEmpty()) {
                    Cookie cookie = new Cookie(label, "");
                    cookie.setMaxAge(0);
                    cookie.setPath("/");
                    response.addCookie(cookie);
                    _cookieCollection.remove(label);
                } else {

                    // FIXME:  Special character handling?  Copied from previous
                    // code.
                    if (value != null && value.indexOf("\"") != -1) {
                        value = value.replace("\"", "");
                    }

                    response.addCookie(new Cookie(label, value));
                }
            }

            _hasNewCookies = false;
        }

        /** Write an error message to the given HttpServletResponse.
         *  @param response The HttpServletResponse to write the message to.
         *  @param responseCode The HTTP response code for the message.  Should be
         *   one of HttpServletResponse.X
         *  @param message The error message to write.
         *  @exception IOException If the write fails.
         */
        private void _writeError(HttpServletResponse response,
                int responseCode, String message) throws IOException {
            response.setContentType("text/html");
            response.setStatus(responseCode);

            PrintWriter writer = response.getWriter();

            writer.println("<!DOCTYPE html>");
            writer.println("<html>");
            writer.println("<body>");
            writer.println("<h1> Error </h1>");
            writer.println(message);
            writer.println("</body>");
            writer.println("</html>");
            writer.flush();
        }
    }
}
