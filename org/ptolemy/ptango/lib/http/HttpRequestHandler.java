/* An actor that handles an HttpRequest by producing an output and waiting for an input.

 Copyright (c) 1997-2014 The Regents of the University of California.
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

package org.ptolemy.ptango.lib.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ptolemy.actor.Director;
import ptolemy.actor.SuperdenseTimeDirector;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.CatchExceptionAttribute;
import ptolemy.actor.lib.ExceptionSubscriber;
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
import ptolemy.data.type.RecordType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/** An abstract base class for
 *  actors that handle an HTTP request by producing output
 *  and waiting for an input that provides a response.
 *  Concrete subclasses of this actor requires that the model that contains
 *  it includes an instance of {@link WebServer}, which discovers this actor and
 *  delegates HTTP requests to a servlet that subclasses of this actor create.
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
 *  (default 30000) milliseconds, then this actor will a issue
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
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (ltrnc)
 *  @Pt.AcceptedRating Red (ltrnc)
 *  @see org.ptolemy.ptango.lib.WebServer
 */
public abstract class HttpRequestHandler extends TypedAtomicActor 
		implements HttpService, ExceptionSubscriber {

    /** Create an instance of the actor.
     *  @param container The container
     *  @param name The name.
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the super
     */
    public HttpRequestHandler(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        path = new StringParameter(this, "path");
        path.setExpression("/*");

        // Ports
        response = new TypedIOPort(this, "response", true, false);
        response.setTypeEquals(BaseType.STRING);
        new Parameter(response, "_showName").setExpression("true");

        uri = new TypedIOPort(this, "getRequestURI", false, true);
        uri.setTypeEquals(BaseType.STRING);
        new Parameter(uri, "_showName").setExpression("true");

        // NOTE: The type will be inferred from how this output is used.
        parameters = new TypedIOPort(this, "getParameters", false, true);
        new Parameter(parameters, "_showName").setExpression("true");

        cookies = new TypedIOPort(this, "getCookies", false, true);
        new Parameter(cookies, "_showName").setExpression("true");

        setCookies = new TypedIOPort(this, "setCookies", true, false);
        new Parameter(setCookies, "_showName").setExpression("true");
        // For now, only allow RecordTokens.  In future, expand to other types
        // of tokens such as StringTokens representing JSON data.
        setCookies.setTypeAtMost(BaseType.RECORD);

        // Parameters
        timeout = new Parameter(this, "timeout");
        timeout.setExpression("30000L");
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
    public TypedIOPort cookies;

    /** An output port that sends parameters included in a get request.
     *  These are values appended to the URL in the form
     *  of ...?name=value. The output will be a record with
     *  one field for each name. If the request assigns multiple
     *  values to the same name, then the field value of the record
     *  will be an array of strings. Otherwise, it will simply
     *  be a string.
     */
    public TypedIOPort parameters;

    /** An output port that sends the relative URI of a get request,
     *  which must match the pattern given by the <i>path</i> parameter.
     *  This has type string.
     */
    public TypedIOPort uri;

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

    /** An array of names of cookies that this actor should retrieve from
     *  an HTTP request and produce on the cookies output
     *  port. This is an array of strings that defaults to an empty array.
     */
    public Parameter requestedCookies;

    /** An input port on which to provide the
     *  response to issue to an HTTP request. When this input port
     *  receives an event, if there is a pending request from
     *  a web server, then that pending request responds with the
     *  value of the input. Otherwise, the response is recorded,
     *  and the next request received will be given the response.
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
     *  the input ports. This is a long that defaults to 30,000.
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
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == path) {
            String pathValue = ((StringToken) path.getToken()).stringValue();
            try {
                // Path should start with a "/", or be "*"
                if (!pathValue.trim().equals("")) {
                    if (!pathValue.trim().startsWith("/")) {
                        _URIpath = URI.create("/" + pathValue);
                    } else {
                        _URIpath = URI.create(pathValue);
                    }
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
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        HttpRequestHandler newObject = (HttpRequestHandler) super.clone(workspace);
        newObject._initializeModelTime = null;
        newObject._initializeRealTime = 0L;
        newObject._URIpath = null;

        newObject._request = null;
        newObject._response = null;

        newObject.setCookies.setTypeAtMost(BaseType.RECORD);

        return newObject;
    }

    /** Not used here.  Required for ExceptionSubscriber interface. */
    // FIXME:  Would it make more sense to send the retry page here, only
    // if restart is successful?  Write a test case.
    @Override
    public boolean exceptionHandled(boolean succesful, String message) {
        return true;
    }

    /** Generate an HTTP response for the client if an exception occurs.  No
     *  response will be received on the input port in the event of an
     *  exception.
     *
     *  @param policy The exception handling policy of the exception handler;
     *   see {@link CatchExceptionAttribute}
     *  @param exception  The exception that occurred
     *  @return True since a response is always sent to the client, and there
     *    there are no operations that throw exceptions
     */

    @Override
    public synchronized boolean exceptionOccurred(String policy, 
            Throwable exception) {

        // If there is a pending request,
        // For "restart" policy, generate an error page with retry
        // For other policies, generate a Server Error error page
        // These method calls notifyAll() so that the response will be sent
        // by the servlet thread
        if (_request != null) {
            if (policy.equals(CatchExceptionAttribute.RESTART)) {
                _respondWithRetryMessage(10);
            } else {
                _respondWithServerErrorMessage();
            }
        }

        return true;
    }

    /** Return the relative path that this HttpService is mapped to,
     *  which is the value of the <i>path</i> parameter.
     *  This method is required by the HttpService interface.
     *  @return The relative path that this HttpService is mapped to.
     *  @see #setRelativePath(URI)
     */
    @Override
    public URI getRelativePath() {
        return _URIpath;
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
    @Override
    public synchronized void fire() throws IllegalActionException {
        // The methods of the servlet are invoked in another
        // thread, so we synchronize on this actor for mutual exclusion.
        super.fire();

        boolean responseFound = false;
        HttpResponseItems responseData = new HttpResponseItems();
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
                responseData.response = ((StringToken) response.get(i))
                        .stringValue();
                responseFound = true;
                if (_debugging) {
                    _debug("Received response on the response input port: "
                            + responseData.response);
                }
            }
        }
        if (responseFound) {
            if (_request == null) {
                // There is no pending request, so ignore the response.
                if (_debugging) {
                    _debug("Discarding the response because there is no pending request.");
                }
            } else {
                _response = responseData;
                // Indicate that the request has been handled.
                _request = null;
                // Notify the servlet thread.
                notifyAll();
            }
        }

        // If there is a pending request, produce outputs for that request,
        // including any cookies from that request.
        if (_request != null) {

            // To avoid the risk of producing two outputs at the same superdense time,
            // check the time of the last output.
            Director director = getDirector();
            // Do not do this if the director does not support superdense time.
            // In that case, there is no issue with multiple outputs.
            if (director instanceof SuperdenseTimeDirector) {
                Time currentTime = director.getModelTime();
                int microstep = ((SuperdenseTimeDirector) director).getIndex();
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
            if (_debugging) {
            	_debug("Sending request URI: " + _request.requestURI);
            	_debug("Sending request parameters: "
            			+ _request.parameters);
            }
            uri.send(0, new StringToken(_request.requestURI));
            // Send parameters, but handle type errors locally.
            try {
            	parameters.send(0, _request.parameters);
            } catch (TypedIOPort.RunTimeTypeCheckException ex) {
            	// Parameters provided do not match the required type.
            	// Construct an appropriate response.
            	_respondWithBadRequestMessage(_request.parameters,
            			parameters.getType(), "parameters");
            }
            if (_request.cookies != null && _request.cookies.length() > 0) {
            	if (_debugging) {
            		_debug("Sending cookies to getCookies port: "
            				+ _request.cookies);
            	}
            	try {
            		cookies.send(0, _request.cookies);
            	} catch (TypedIOPort.RunTimeTypeCheckException ex) {
            		// Parameters provided do not match the required type.
            		// Construct an appropriate response.
            		_respondWithBadRequestMessage(_request.parameters,
            				parameters.getType(), "cookies");
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
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _request = null;
        _response = null;
        _initializeModelTime = getDirector().getModelTime();
        // Subtract a tenth of a second.  As this actor is initialized
        // after the director, the initial time here could be later than what
        // the director perceives as the initial real time.
        // FIXME:  The director could offer e.g. a getInitialRealTime() function
        // to avoid this workaround
        _initializeRealTime = System.currentTimeMillis() - 100;
        _lastOutputTime = null;
    }

    /** Set the relative path that this HttpService is mapped to.
     *  This method is required by the HttpService interface.
     *  @param path The relative path that this HttpService is mapped to.
     *  @see #getRelativePath()
     */
    @Override
    public void setRelativePath(URI path) {
        _URIpath = path;
    }

    /** Specify the web server for this service. This will
     *  be called by the {@link WebServer} attribute of a model,
     *  if there is one, and will enable this service to access
     *  information about the web server (such as
     *  the resourcePath, resourceLocation, or temporaryFileLocation).
     */
    @Override
    public void setWebServer(WebServer server) {
        // Ignore. This actor doesn't need to know.
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected methods                   ////

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
    protected void _handleRequest(HttpServletRequest request,
            HttpServletResponse response, int type)
            throws ServletException, IOException {
        // The following codeblock is synchronized on the enclosing
        // actor. This lock _is_ released while waiting for the response,
        // allowing the fire method to execute its own synchronized blocks.
        synchronized (HttpRequestHandler.this) {
            _request = new HttpRequestItems();

            _request.requestURI = request.getRequestURI();
            _request.requestType = type;

            response.setContentType("text/html");

            // Set up a buffer for the output so we can set the length of
            // the response, thereby enabling persistent connections
            // http://docstore.mik.ua/orelly/java-ent/servlet/ch05_03.htm
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            PrintWriter writer = new PrintWriter(bytes, true);// true forces
            // flushing

            if (_debugging) {
                _debug("**** Handling a " + ((type == 0) ? "get" : "post")
                        + " request to URI " + _request.requestURI);
            }

            try {
                // Read cookies from the request and store.
                _request.cookies = _readCookies(request);

                // Get the parameters that have been either posted or included
                // as assignments in a get using the URL syntax ...?name=value.
                // Note that each parameter name may have more than one value,
                // hence the array of strings.
                _request.parameters = _readParameters(request);

                // Figure out what time to request a firing for.
                long elapsedRealTime = System.currentTimeMillis()
                        - _initializeRealTime;

                // Assume model time is in seconds, not milliseconds.
                Time timeOfRequest = _initializeModelTime
                        .add(elapsedRealTime / 1000.0);

                if (_debugging) {
                    _debug("**** Request firing at time " + timeOfRequest);
                }

                // Note that fireAt() will modify the requested firing time
                // if it is in the past.
                // Note that past firing times might not be modified
                // if ThreadedComposite actors are used (since the request
                // might be at a present time inside the ThreadedComposite,
                // but a past time for the top-level model).
                getDirector().fireAt(HttpRequestHandler.this, timeOfRequest);
            } catch (IllegalActionException e) {
                _request = null;
                _writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                        e.getMessage(), bytes, writer);
                return;
            }
            //////////////////////////////////////////////////////
            // Wait for a response.
            // We are assuming every request gets exactly one response in FIFO order.
            while (_response == null) {
                if (_debugging) {
                    _debug("**** Waiting for a response");
                }
                try {
                    // Timeout after time given by timeout parameter.
                    // Unfortunately, we can't tell whether the timeout
                    // occurred unless we record the current time.
                    long startTime = System.currentTimeMillis();
                    long timeoutValue = 30000L;
                    try {
                        timeoutValue = ((LongToken) timeout.getToken())
                                .longValue();
                    } catch (IllegalActionException e) {
                        // Ignore and use default of 30 seconds.
                    }
                    HttpRequestHandler.this.wait(timeoutValue);
                    if (System.currentTimeMillis() - startTime >= timeoutValue) {
                        if (_debugging) {
                            _debug("**** Request timed out.");
                        }

                        // Set the content length (enables persistent
                        // connections) and send the buffer
                        writer.println("Request timed out");
                        response.setContentLength(bytes.size());
                        bytes.writeTo(response.getOutputStream());

                        // Indicate that there is no longer a pending request or response.
                        _request = null;
                        _response = null;

                        // Close the PrintWriter
                        // Close the ByteArrayOutputStream to avoid a
                        // warning, though this is not necessary since
                        // ByteArrayOutputStream's close() method has no
                        // effect
                        // http://docs.oracle.com/javase/6/docs/api/java/io/ByteArrayOutputStream.html#close%28%29
                        writer.close();
                        bytes.close();
                        response.setStatus(HttpServletResponse.SC_REQUEST_TIMEOUT);

                        return;
                    }
                } catch (InterruptedException e) {
                    if (_debugging) {
                        _debug("*** Request thread interrupted.");
                    }

                    // Set the content length (enables persistent
                    // connections) and send the buffer
                    writer.println("Get request thread interrupted");
                    response.setContentLength(bytes.size());
                    bytes.writeTo(response.getOutputStream());

                    // Indicate that there is no longer a pending request or response.
                    _request = null;
                    _response = null;

                    // Close the PrintWriter
                    // Close the ByteArrayOutputStream to avoid a
                    // warning, though this is not necessary since
                    // ByteArrayOutputStream's close() method has no
                    // effect
                    // http://docs.oracle.com/javase/6/docs/api/java/io/ByteArrayOutputStream.html#close%28%29
                    writer.close();
                    bytes.close();
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    return;
                }
            }

            response.setStatus(_response.statusCode);

            // Write all cookies to the response, if there are some new
            // cookies to write
            if (_response.hasNewCookies) {
                _writeCookies(_response.cookies, response);
            }
            if (_debugging) {
                _debug("**** Servet received response: "
                        + _response.response);
            }

            // Set the content length (enables persistent
            // connections) and send the buffer
            // Use print rather than println to prevent an extra \n on the response.
            writer.print(_response.response);
            // Printwriter only flushes println() automatically. So force a flush.
            writer.flush();
            response.setContentLength(bytes.size());
            bytes.writeTo(response.getOutputStream());

            // Close the PrintWriter
            // Close the ByteArrayOutputStream to avoid a
            // warning, though this is not necessary since
            // ByteArrayOutputStream's close() method has no
            // effect
            // http://docs.oracle.com/javase/6/docs/api/java/io/ByteArrayOutputStream.html#close%28%29
            writer.close();
            bytes.close();

            // Indicate response has been handled.
            _response = null;
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
     *  @exception IllegalActionException If construction of the record token fails.
     */
    protected RecordToken _readCookies(HttpServletRequest request)
            throws IllegalActionException {
        ArrayToken labels = (ArrayToken) requestedCookies.getToken();
        if (labels.length() == 0) {
            // No cookies requested.
            // Return an empty record.
            return RecordToken.EMPTY_RECORD;
        }
        // First, provide default empty string values for requested cookies.
        LinkedHashMap<String, Token> map = new LinkedHashMap<String, Token>();
        StringToken emptyString = new StringToken("");
        for (int i = 0; i < labels.length(); i++) {
            String label = ((StringToken) labels.getElement(i))
                    .stringValue();
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
     *  @exception IllegalActionException If construction of the record token fails.
     */
    protected RecordToken _readParameters(HttpServletRequest request)
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
    protected void _writeCookies(RecordToken cookies,
            HttpServletResponse response) {
        // TODO:  Allow permanent cookies.  Current implementation produces
        // session cookies.  Session cookies are stored in
        // the browser's memory, and are erased when the browser is closed
        // http://www.javascriptkit.com/javatutors/cookie.shtml
        for (String label : cookies.labelSet()) {
            String value;
            Token recordValue = cookies.get(label);
            if (recordValue instanceof StringToken) {
                value = ((StringToken) recordValue).stringValue();
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
                cookie.setPath("/"); // Cookie visible to all web pages
                response.addCookie(cookie);
            } else {
                // FIXME:  Special character handling?  Copied from previous
                // code. Should cookies be encoded using URLEncoder.encode()?
                if (value.indexOf("\"") != -1) {
                    value = value.replace("\"", "");
                }
                Cookie cookie = new Cookie(label, value);
                cookie.setPath("/");
                response.addCookie(cookie);
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
    protected void _writeError(HttpServletResponse response,
            int responseCode, String message, ByteArrayOutputStream bytes,
            PrintWriter writer) throws IOException {

        response.setContentType("text/html");
        response.setStatus(responseCode);

        writer.println("<!DOCTYPE html>");
        writer.println("<html>");
        writer.println("<body>");
        writer.println("<h1> Error </h1>");
        writer.println(message);
        writer.println("</body>");
        writer.println("</html>");

        response.setContentLength(bytes.size());
        bytes.writeTo(response.getOutputStream());

        // Close the PrintWriter
        // Close the ByteArrayOutputStream to avoid a
        // warning, though this is not necessary since
        // ByteArrayOutputStream's close() method has no
        // effect
        // http://docs.oracle.com/javase/6/docs/api/java/io/ByteArrayOutputStream.html#close%28%29
        writer.close();
        bytes.close();
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Given a record token type, construct the URL string that would result
     *  in that record, and record that string in the specified message buffer.
     *  @param expected The record type.
     *  @param message The message buffer.
     */
    private void _recordToURLString(RecordType expected, StringBuffer message) {
        boolean first = true;
        for (String label : expected.labelSet()) {
            if (first) {
                message.append("?");
            } else {
                message.append("&");
            }
            first = false;
            message.append(label);
            message.append("=");
            message.append(expected.get(label).toString());
        }
    }

    /** Issue a response to the current request indicating malformed syntax.
     *  According to: www.w3.org/Protocols/rfc2616/rfc2616-sec10.html,
     *  the correct response is
     *  10.4.1 400 Bad Request, "The request could not be understood
     *  by the server due to malformed syntax.
     *  The client SHOULD NOT repeat the request without modifications."
     *  @param record The record that was received.
     *  @param expectedType The type of record expected.
     *  @param what What triggered the error ("parameters" or "cookies").
     */
    private void _respondWithBadRequestMessage(RecordToken record,
            Type expectedType, String what) {
        _response = new HttpResponseItems();
        _response.statusCode = HttpServletResponse.SC_BAD_REQUEST;
        StringBuffer message = new StringBuffer();
        message.append("<html><body><h1> Bad Request (code "
                + HttpServletResponse.SC_BAD_REQUEST + ")</h1>\n");
        message.append("<p>Expected ");
        message.append(what);
        message.append(" of the form: ");
        if (expectedType instanceof RecordType) {
            _recordToURLString((RecordType) expectedType, message);
        } else {
            message.append(expectedType.toString());
        }
        message.append("</p><p>Got: ");
        _recordToURLString((RecordType) record.getType(), message);
        message.append("</p></body></html>");

        _response.response = message.toString();

        notifyAll();
    }

    /** Issue a response indicating a server error and the intent to retry.
     *  The Javascript on the response page will invoke the
     *
     *  @param timeout The time to wait, in seconds
     */
    private void _respondWithRetryMessage(int timeout) {

        int timeoutValue = timeout;

        if (timeoutValue < 1) {
            timeoutValue = 1;
        }

        // Construct a response page.  The page will issue a second request to
        // the URL after a specified period of time.  The retry page content and
        // time are currently fixed.
        // TODO:  Allow dynamic content and timers for retry

        _response = new HttpResponseItems();
        _response.statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

        String ajax = "";
        if (_request.requestType == 0) {
            ajax = "jQuery.get(\""
                    + _request.requestURI
                    + "\")\n"
                    + ".done(function(data) { \n "
                    +
                    // Wrap result page with <div> </div> since an HTML page is not
                    // valid xml due to unclosed <!DOCTYPE HTML> tag
                    // jQuery has problems parsing otherwise
                    // http://www.sitepoint.com/secrets-selecting-elements-returned-jquery-ajax-response-strings/
                    "result = \"<div>\" + data + \"</div>\";"
                    + "jQuery(\"#contents\").html(jQuery(result).find(\"#contents\").html());"
                    + "\n });";
        } else if (_request.requestType == 1) {
            StringBuffer parameters = new StringBuffer("{");
            if (_request.parameters != null) {
                for (String label : _request.parameters.labelSet()) {
                    // TODO:  Test if this works for strings
                    // I believe these require quotation marks around them
                    parameters.append(label + ": "
                            + _request.parameters.get(label).toString() + ",");
                }

                // Erase the last , and add }
                if (parameters.length() > 0) {
                    parameters.deleteCharAt(parameters.length() - 1);
                    parameters.append('}');
                    ajax = "jQuery.post(\""
                            + _request.requestURI
                            + "\", "
                            + parameters.toString()
                            + ")\n"
                            + ".done(function(data) { \n "
                            + "result = \"<div>\" + data + \"</div>\";"
                            + "jQuery(\"#contents\").html(jQuery(result).find(\"#contents\").html());"
                            + "\n });";
                } else {
                    ajax = "jQuery.post(\""
                            + _request.requestURI
                            + "\")\n"
                            + ".done(function(data) { \n "
                            + "result = \"<div>\" + data + \"</div>\";"
                            + "jQuery(\"#contents\").html(jQuery(result).find(\"#contents\").html());"
                            + "\n });";
                }
            }
        }

        StringBuffer message = new StringBuffer();

        message.append("<!DOCTYPE html>\n<html>\n<head> "
                + "<meta charset=\"UTF-8\">\n");
        message.append("<script src=\"http://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js\"></script>\n");
        message.append("<script>\n var count=" + timeoutValue + ";\n");
        message.append("var interval=setInterval(timer,1000);\nvar result;\n");
        message.append("function timer() {\n " + "count=count-1;\n"
                + "jQuery(\"#countdown\").html(count+1);\n"
                + "if (count <= 0) {\n" + "clearInterval(interval);\n"
                + ajax.toString() + "\n}\n}\n");
        message.append("jQuery(document).ready(function() {\n"
                + "timer();\n});");
        message.append("</script></head>\n");
        message.append("<body><div id=\"contents\"> \n"
                + "<h1> Internal Server Error (code "
                + HttpServletResponse.SC_INTERNAL_SERVER_ERROR + ")</h1>\n");
        message.append("<div> Retrying in <div id=\"countdown\">"
                + timeoutValue + "</div></div></div>\n");
        message.append("</body></html>");

        _response.response = message.toString();

        notifyAll();

    }

    /** Issue a response indicating a server error (i.e., a problem running the
     *  Ptolemy model.
     */
    private void _respondWithServerErrorMessage() {
        _response = new HttpResponseItems();
        _response.statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        StringBuffer message = new StringBuffer();
        message.append("<html><body><h1> Internal Server Error (code "
                + HttpServletResponse.SC_INTERNAL_SERVER_ERROR + ")</h1>\n");
        message.append("</p></body></html>");

        _response.response = message.toString();

        notifyAll();
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

    /** The pending request. */
    private HttpRequestItems _request;

    /** The pending response. */
    private HttpResponseItems _response;

    /** The URI for the relative path from the "path" parameter.
     *  A URI is used here to make sure the "path" parameter conforms to
     *  all of the URI naming conventions.
     */
    private URI _URIpath;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    ///////////////////////////////////////////////////////////////////
    //// HttpRequestItems

    /** A data structure with all the relevant information about an
     *  HTTP request.
     */
    protected static class HttpRequestItems {
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
    //// HttpResponseItems

    /** A data structure with all the relevant information about an
     *  HTTP response.
     */
    protected static class HttpResponseItems {
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

        /** Status code of the response. */
        public int statusCode = HttpServletResponse.SC_OK;
    }
}
