/* An actor that handles an HttpRequest by producing an output and waiting for an input.

 Copyright (c) 1997-2013 The Regents of the University of California.
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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ptolemy.actor.Director;
import ptolemy.actor.SuperdenseTimeDirector;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.MicrostepDelay;
import ptolemy.actor.lib.TimeDelay;
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

/** An actor that handles an HTTP request by producing output
 *  and waiting for an input that provides a response.
 *  This actor requires that the model that contains it includes an
 *  instance of {@link WebServer}, which discovers this actor and
 *  delegates HTTP requests to a servlet that this actor creates.
 *  It also requires that the model containing this actor provide
 *  exactly one input to either the {@link #response} or {@link #setCookies}
 *  input port (or both) in response to every output request,
 *  and that such responses are delivered in the same order as
 *  the requests they are responding to.
 *  <p>
 *  The <i>path</i> parameter specifies which HTTP requests will
 *  be delegated to this actor. If the base URL for the web server
 *  is "http://localhost:8080", say, then request of the form
 *  "http://localhost:8080/<i>path</i>" will be delegated to this
 *  actor.
 *  <p>
 *  When this actor receives an HTTP request from the WebServer, it
 *  issues a request for the director to fire it at the greater of
 *  the current time of the director or the time elapsed since
 *  the last invocation of initialize() (in seconds).
 *  When the actor fires, it produces on its output ports the details
 *  of the request, time stamped by the elapsed time since the model
 *  started executing. It expects to be in a model that will send
 *  it data to either the {@link #response} or {@link #setCookies}
 *  input port (or both) with the response to HTTP request.
 *  If that response does not arrive within <i>timeout</i>
 *  (default 10000) milliseconds, then this actor will a issue
 *  timeout response and will discard the response when it eventually
 *  arrives.
 *  <p>
 *  This actor is designed to be used in a DE model (with a DEDirector).
 *  Since a model using this actor must deliver a response for
 *  every request, the model must put this actor in a feedback
 *  loop. The requests appear on the outputs of this actor,
 *  and the responses appear on the inputs. Each such feedback
 *  loop is required to include an instance of {@link MicrostepDelay}
 *  (or {@link TimeDelay}, if you wish to model response times) in order
 *  to break the causality loop that the feedback loop would
 *  otherwise incur.
 *  The downstream model should be used to construct a response.
 *  For example, to simply serve a web page, put a
 *  {@link FileReader} actor to read the response from a local
 *  file and a {@link MicrostepDelay} downstream in a feedback
 *  loop connected back to the response input.
 *
 *  @author Elizabeth Latronico and Edward A. Lee
 *  @version $Id$
 *  @since Ptolemy II 9.0
 *  @Pt.ProposedRating Red (ltrnc)
 *  @Pt.AcceptedRating Red (ltrnc)
 *  @see org.ptolemy.ptango.lib.WebServer
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
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** An output that sends the cookies specified by the
     *  {@link #requestedCookies} parameter, with values
     *  provided by a get request. If the get request does
     *  have cookies with names matching those in requestedCookies,
     *  then those values will be empty strings.
     *  The output will be a RecordToken with the field names given by
     *  requestedCookies, and the field values being strings.
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

    /** An output that sends the cookies specified by the
     *  {@link #requestedCookies} parameter, with values
     *  provided by a post request. If the post request does
     *  have cookies with names matching those in requestedCookies,
     *  then those values will be empty strings.
     *  The output will be a RecordToken with the field names given by
     *  requestedCookies, and the field values being strings.
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

    /** An array of names of cookies that this actor should retrieve from
     *  an HTTP request and produce on the getCookies and putCookies output
     *  ports. This is an array of strings that defaults to an empty array.
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

    /** An input on which to provide new cookies and/or new cookie values.
     *  These will be set in the HttpResponse received on the {@link #response}
     *  input in the same firing, or if there is no token on the response
     *  input, will be treated as cookies accompanying an empty string response.
     *  This has type record.
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
        newObject._initializeModelTime = null;
        newObject._initializeRealTime = 0L;
        newObject._URIpath = null;
        
        newObject._requestQueue = null;
        newObject._responseQueue = null;
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

    /** If there are input tokens on the {@link #response} or
     *  {@link #setCookies} ports, then queue a response to be
     *  sent by the servelet for a corresponding request. 
     *  If the servlet has received
     *  an HTTP request, then also produce on the output ports
     *  the details of the request.
     *  If there is an HTTP request, but the current superdense time
     *  matches the time of the previously produced output,
     *  then request a refiring at the current time (next microstep)
     *  so that the outputs are produced at the next microstep.
     *  This actor is designed to normally be fired twice at each superdense
     *  time, first to produce outputs corresponding to an HTTP
     *  request, and next to send back the response to that request.
     *  This strategy avoids creating signals with multiple values
     *  at the same superdense time instant.
     *  @exception IllegalActionException If sending the
     *   outputs fails.
     */
    public synchronized void fire() throws IllegalActionException {
        // The methods of the servlet are invoked in another
        // thread, so we synchronize on this actor for mutual exclusion.
        super.fire();

        boolean responseFound = false;
        HttpResponse responseData = new HttpResponse();
        // Check for new cookies on the setCookies port.
        if (setCookies.getWidth() > 0 && setCookies.hasToken(0)) {
            RecordToken cookieToken = (RecordToken) setCookies.get(0);
            // Note that we issue a response if we get cookies only.
            // If there is no response input, then the response will be an empty string.
            responseFound = true;
            responseData.hasNewCookies = true;
            responseData.cookies = cookieToken;
            // Default response has empty text.
            responseData.response = "";
            if (_debugging) {
                _debug("Received cookies on the setCookies input port: "
                        + cookieToken);
            }
        }

        for (int i = 0; i < response.getWidth(); i++) {
            if (response.hasToken(i)) {
                responseData.response = ((StringToken) response.get(i)).stringValue();
                responseFound = true;
                if (_debugging) {
                    _debug("Received response on the response input port: "
                            + responseData.response);
                }
            }
        }
        if (responseFound) {
            if (_timeouts > 0) {
                // A timeout occurred. Discard the response.
                _timeouts--;
                if (_debugging) {
                    _debug("Discarding the response because of an earlier timeout.");
                }
            } else {
                _responseQueue.add(responseData);
                // If there is a pending request, notify it.
                notifyAll();
            }
        }

        // If there is a pending request, produce outputs for that request,
        // including any cookies from that request.
        if (_requestQueue.size() > 0) {
            
            // To avoid the risk of producing two outputs at the same superdense time,
            // check the time of the last output.
            Director director = getDirector();
            // Do not do this if the director does not support superdense time.
            // In that case, there is no issue with multiple outputs.
            if (director instanceof SuperdenseTimeDirector) {
                Time currentTime = director.getModelTime();
                int microstep = ((SuperdenseTimeDirector)director).getIndex();
                if (_lastOutputTime != null
                        && _lastOutputTime.equals(currentTime)
                        && microstep == _lastMicrostep) {
                    director.fireAtCurrentTime(this);
                    // Leave the request queue alone and don't produce outputs now.
                    return;
                }
                _lastMicrostep = microstep;
                _lastOutputTime = currentTime;
            }
            
            // Remove the request from the head of the queue so that
            // each request is handled no more than once.
            HttpRequest request = _requestQueue.poll();
            if (request.requestType == 0) {
                if (_debugging) {
                    _debug("Sending get request URI: " + request.requestURI);
                    _debug("Sending get request parameters: " + request.parameters);
                }
                getRequestURI.send(0, new StringToken(request.requestURI));
                getParameters.send(0, request.parameters);
                if (request.cookies != null && request.cookies.length() > 0) {
                    if (_debugging) {
                        _debug("Sending cookies to getCookies port: " + request.cookies);
                    }
                    getCookies.send(0, request.cookies);
                }
            } else {
                if (_debugging) {
                    _debug("Sending post request URI: " + request.requestURI);
                    _debug("Sending post request parameters: " + request.parameters);
                }
                postRequestURI.send(0, new StringToken(request.requestURI));
                postParameters.send(0, request.parameters);
                if (request.cookies != null && request.cookies.length() > 0) {
                    postCookies.send(0, request.cookies);
                    if (_debugging) {
                        _debug("Sending cookies to postCookies port: " + request.cookies);
                    }
                }
            }
        }
        if (_debugging) {
            _debug("Ending fire.");
        }
    }

    /** Record the current model time and the current real time
     *  so that output events can be time stamped with the elapsed
     *  time since model start.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        if (_requestQueue == null) {
            _requestQueue = new LinkedList<HttpRequest>();
        } else {
            _requestQueue.clear();
        }
        if (_responseQueue == null) {
            _responseQueue = new LinkedList<HttpResponse>();
        } else {
            _responseQueue.clear();
        }
        _initializeModelTime = getDirector().getModelTime();
        _initializeRealTime = System.currentTimeMillis();
        _timeouts = 0;
        _lastOutputTime = null;
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
    
    /** The model time at which this actor was last initialized. */
    private Time _initializeModelTime;

    /** The real time at which this actor was last initialized, in milliseconds. */
    private long _initializeRealTime;
    
    /** Time of the last output. */
    private Time _lastOutputTime;
    
    /** Microstep of the last output. */
    private int _lastMicrostep;
    
    /** The queue of pending requests. */
    private LinkedList<HttpRequest> _requestQueue;
    
    /** The queue of pending responses. */
    private LinkedList<HttpResponse> _responseQueue;
    
    /** Number of timeouts that have occurred, which is the number of responses
     *  to be discarded when they finally arrive.
     */
    private int _timeouts;

    /** The URI for the relative path from the "path" parameter.
     *  A URI is used here to make sure the "path" parameter conforms to
     *  all of the URI naming conventions.
     */
    private URI _URIpath;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    ///////////////////////////////////////////////////////////////////
    //// ActorServlet
    
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
         *  See <a href="http://docs.oracle.com/javaee/6/api/javax/servlet/http/HttpServletRequest.html">http://docs.oracle.com/javaee/6/api/javax/servlet/http/HttpServletRequest.html</a>.
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
                HttpRequest requestData = new HttpRequest();
                _requestQueue.add(requestData);
                
                requestData.requestURI = request.getRequestURI();
                requestData.requestType = type;

                if (_debugging) {
                    _debug("**** Handling a "
                            + ((type == 0) ? "get" : "post")
                            + " request to URI "
                            + requestData.requestURI);
                }

                try {
                    // Read cookies from the request and store in the queue.
                    requestData.cookies = _readCookies(request);

                    // Get the parameters that have been either posted or included
                    // as assignments in a get using the URL syntax ...?name=value.
                    // Note that each parameter name may have more than one value,
                    // hence the array of strings.
                    requestData.parameters = _readParameters(request);

                    // Figure out what time to request a firing for.
                    long elapsedRealTime = System.currentTimeMillis() - _initializeRealTime;
                    Time timeOfRequest = _initializeModelTime.add(elapsedRealTime);
                    
                    // Note that fireAt() will modify the requested firing time if it is in the past.
                    getDirector().fireAt(HttpActor.this, timeOfRequest);
                    
                    if (_debugging) {
                        _debug("**** Requested firing at time " + timeOfRequest);
                    }
                } catch (IllegalActionException e) {
                    _writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                            e.getMessage());
                    return;
                }
                //////////////////////////////////////////////////////
                // Wait for a response.
                // We are assuming every request gets exactly one response in FIFO order.
                while (_responseQueue.size() == 0) {
                    if (_debugging) {
                        _debug("**** Waiting for a response");
                    }
                    try {
                        // Timeout after time given by timeout parameter.
                        // Unfortunately, we can't tell whether the timeout
                        // occurred unless we record the current time.
                        long startTime = System.currentTimeMillis();
                        long timeoutValue = 10000L;
                        try {
                            timeoutValue = ((LongToken) timeout.getToken()).longValue();
                        } catch (IllegalActionException e) {
                            // Ignore and use default of 10 seconds.
                        }
                        HttpActor.this.wait(timeoutValue);
                        if (System.currentTimeMillis() - startTime >= timeoutValue) {
                            if (_debugging) {
                                _debug("**** Request timed out.");
                            }
                            response.getWriter().println("<h1> Request timed out </h1>");
                            _timeouts++;
                            return;
                        }
                    } catch (InterruptedException e) {
                        if (_debugging) {
                            _debug("Request thread interrupted.");
                        }
                        response.getWriter().println("<h1> Get request thread interrupted </h1>");
                        return;
                    }
                }
                
                HttpResponse responseData = _responseQueue.poll();

                response.setContentType("text/html");
                response.setStatus(HttpServletResponse.SC_OK);

                // Write all cookies to the response, if there are some new
                // cookies to write
                if (responseData.hasNewCookies) {
                    _writeCookies(responseData.cookies, response);
                }
                if (_debugging) {
                    _debug("**** Responding to get request: " + responseData.response);
                }
                response.getWriter().println(responseData.response);
            }
        }

        /** Read the Cookies from the HttpServletRequest, construct
         *  a record token with one field for each name in the requestedCookies
         *  parameter and the value given by the request, or if there is no
         *  value, with an empty string as a value.
         *  @param request  The HttpServletRequest to read Cookies from.  The
         *   HttpServletRequest can be of any type - i.e. both GET and POST
         *   requests are allowed to have Cookies.
         *  @return A record of cookies.
         *  @throws IllegalActionException If construction of the record token fails.
         */
        private RecordToken _readCookies(HttpServletRequest request)
                throws IllegalActionException {
            ArrayToken labels = (ArrayToken)requestedCookies.getToken();
            if (labels.length() == 0) {
                // No cookies requested.
                // Return an empty record.
                return RecordToken.EMPTY_RECORD;
            }
            // First, provide default empty string values for requested cookies.
            LinkedHashMap<String,Token> map = new LinkedHashMap<String,Token>();
            StringToken emptyString = new StringToken("");
            for (int i = 0; i < labels.length(); i++) {
                String label = ((StringToken)labels.getElement(i)).stringValue();
                map.put(label, emptyString);
            }
            // Next, override these default values with any cookies provided in the request.
            for (Cookie cookie : request.getCookies()) {
                // Cookie must have a name
                if (cookie.getName() != null && !cookie.getName().isEmpty()) {
                    String label = cookie.getName();
                    if (map.containsKey(label)) {
                        // Override the default value.
                        map.put(label, new StringToken(cookie.getValue()));
                    }
                }
            }
            return new RecordToken(map);
        }
        
        /** Read the parameters from the HttpServletRequest, construct
         *  a record token containing the parameters, and return that record.
         *  @param request  The HttpServletRequest to read paramters from.  The
         *   HttpServletRequest can be of any type - i.e. both GET and POST
         *   requests are allowed to have parameters.
         *  @return A record of parameters.
         *  @throws IllegalActionException If construction of the record token fails.
         */
        private RecordToken _readParameters(HttpServletRequest request)
                throws IllegalActionException {
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
                    ArrayToken array = new ArrayToken(arrayEntries);
                    fieldValues[i] = array;
                }
                i++;
            }
            return new RecordToken(fieldNames, fieldValues);
        }

        /** Write the cookies in the specified RecordToken to the HttpResponse.
         *  If the value of a cookie is an empty string, then clear the cookie instead.
         *  @param cookies The cookies.
         *  @param response The HttpServletResponse to write the cookies to.
         */
        private void _writeCookies(RecordToken cookies, HttpServletResponse response) {
            // TODO:  Allow permanent cookies.  Current implementation produces
            // session cookies.  Session cookies are stored in
            // the browser's memory, and are erased when the browser is closed
            // http://www.javascriptkit.com/javatutors/cookie.shtml
            for (String label : cookies.labelSet()) {
                String value;
                Token recordValue = cookies.get(label);
                if (recordValue instanceof StringToken) {
                    value = ((StringToken)recordValue).stringValue();
                } else {
                    value = recordValue.toString();
                }
                // Clear the cookie if the value is empty
                // TODO:  Should there be an explicit clear cookie port?
                // Is there a scenario where we want the cookie to exist,
                // but to have an empty string for the value?
                if (value.isEmpty()) {
                    Cookie cookie = new Cookie(label, "");
                    cookie.setMaxAge(0);
                    cookie.setPath("/");
                    response.addCookie(cookie);
                } else {
                    // FIXME:  Special character handling?  Copied from previous
                    // code. Should cookies be encoded using URLEncoder.encode()?
                    if (value != null && value.indexOf("\"") != -1) {
                        value = value.replace("\"", "");
                    }
                    response.addCookie(new Cookie(label, value));
                }
            }
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
    
    ///////////////////////////////////////////////////////////////////
    //// HttpRequest

    /** A data structure with all the relevant information about an
     *  HTTP request.
     */
    protected static class HttpRequest {
        /** Cookies associated with the request. */
        public RecordToken cookies;

        /** Parameters received in a get or post. */
        public RecordToken parameters;

        /** The type of request. 0 for get, 1 for put. */
        public int requestType;

        /** The URI issued in the get request. */
        public String requestURI;
    }
    
    ///////////////////////////////////////////////////////////////////
    //// HttpResponse

    /** A data structure with all the relevant information about an
     *  HTTP response.
     */
    protected static class HttpResponse {
        /** All cookies from the setCookies port plus the Cookies from the
         *  HttpRequest.  Values provided on the setCookies port override values
         *  from the HttpRequest for cookies with the same name.  (I.e., the model
         *  developer wants to replace the cookie on the client with the cookie
         *  provided on the input port.)
         */
        public RecordToken cookies;
        
        /** A flag indicating that new cookies have been received on the
         *  setCookies port, and that all cookies should be written to the
         *  HttpServletResponse in the doGet() or doPost() method.
         *  It would not be wrong to always write the cookies to every
         *  HttpServletResponse, but it would be inefficient.
         */
        public boolean hasNewCookies;
        
        /** The text of the response. */
        public String response;
    }
}
