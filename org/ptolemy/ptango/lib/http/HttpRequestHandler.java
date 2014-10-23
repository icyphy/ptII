/* An actor that handles an HttpRequest by producing an output and waiting for an input.

 Copyright (c) 2014 The Regents of the University of California.
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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Enumeration;
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
import ptolemy.actor.lib.CatchExceptionAttribute;
import ptolemy.actor.lib.ExceptionSubscriber;
import ptolemy.actor.lib.MicrostepDelay;
import ptolemy.actor.lib.RecordDisassembler;
import ptolemy.actor.lib.io.FileReader;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.actor.util.Time;
import ptolemy.data.ArrayToken;
import ptolemy.data.IntToken;
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
import ptolemy.util.StringUtilities;

/** An actor that handles an HTTP request by producing output
 *  and waiting for an input that provides a response.
 *  This actor requires that the model that contains
 *  it include an instance of {@link WebServer}, which discovers this actor and
 *  delegates HTTP requests to a servlet that this actor creates.
 *  It also requires that the model containing this actor provide
 *  exactly one input to at least one of the input ports in response to
 *  every output request,
 *  and that such responses are delivered to its input in the very
 *  next microstep, or at least before any other request is made
 *  by the WebServer.
 *  <p>
 *  The <i>path</i> parameter specifies which HTTP requests will
 *  be delegated to this actor. If the base URL for the web server
 *  is "http://localhost:8080", say, then request of the form
 *  "http://localhost:8080/<i>path</i>" will be delegated to this
 *  actor. It is an error if two instances of this actor to have the
 *  same <i>path</i> in a model?
 *  <p>
 *  When this actor receives an HTTP request from the WebServer, it
 *  issues a request for the director to fire it at the greater of
 *  the current time of the director or the time elapsed since
 *  the last invocation of initialize() (in seconds).
 *  When the actor fires, it produces on its output ports the details
 *  of the request, time stamped by the elapsed time since the model
 *  started executing. It expects to be in a model that will send
 *  it data to input ports with the response to HTTP request.
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
 *  in order to break the causality loop that the feedback loop would
 *  otherwise incur.
 *  The downstream model should be used to construct a response.
 *  For example, to simply serve a web page, put a
 *  {@link FileReader} actor to read the response from a local
 *  file and a {@link MicrostepDelay} downstream in a feedback
 *  loop connected back to the response input.
 *  <p>
 *  Some of the output ports ({@link #parameters} and {@link #headers})
 *  produce records and constrain their output type to be less than
 *  or equal to an empty record. This does not, however, provide
 *  enough information for type inference. If the downstream model
 *  that receives these records <i>requires</i> particular fields
 *  in the record, then putting a {@link RecordDisassembler} actor downstream
 *  to extract the field, and enabling backward type inference at the top
 *  level of the model will result in a constraint on this port that the
 *  record contain the specified field.  A malformed URL that does
 *  not contain the specified record fields will result in a response
 *  400 Bad Request, meaning "the request could not be understood
 *  by the server due to malformed syntax.
 *  The client SHOULD NOT repeat the request without modifications."
 *  If the downstream model does not require
 *  any particular fields, but will rather examine any fields that
 *  are provided, for example in a {@link ptolemy.actor.lib.js.JavaScript} actor, then
 *  you can declare the output ports of this actor to be of type "record",
 *  or you can declare the input port of the downstream actor to be
 *  of type "record" and enable backward type inference. In this case,
 *  any fields that are provided will be passed downstream as a record.
 *
 *  @author Elizabeth Latronico, Edward A. Lee, and Marten Lohstroh
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (ltrnc)
 *  @Pt.AcceptedRating Red (ltrnc)
 *  @see org.ptolemy.ptango.lib.WebServer
 */
public class HttpRequestHandler extends TypedAtomicActor implements
        HttpService, ExceptionSubscriber {

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

        ///////////////// Input Ports
        responseBody = new TypedIOPort(this, "responseBody", true, false);
        responseBody.setTypeEquals(BaseType.STRING);
        new Parameter(responseBody, "_showName").setExpression("true");

        responseCode = new TypedIOPort(this, "responseCode", true, false);
        responseCode.setTypeEquals(BaseType.INT);
        new Parameter(responseCode, "_showName").setExpression("true");

        responseContentType = new PortParameter(this, "responseContentType");
        responseContentType.setTypeEquals(BaseType.STRING);
        responseContentType.setStringMode(true);
        responseContentType.setExpression("text/html");
        new Parameter(responseContentType.getPort(), "_showName")
                .setExpression("true");

        responseHeaders = new PortParameter(this, "responseHeaders");
        responseHeaders.setTypeEquals(BaseType.RECORD);
        new Parameter(responseHeaders.getPort(), "_showName")
                .setExpression("true");

        setCookies = new TypedIOPort(this, "setCookies", true, false);
        new Parameter(setCookies, "_showName").setExpression("true");
        setCookies.setTypeAtMost(BaseType.RECORD);

        ///////////////// Output Ports
        method = new TypedIOPort(this, "method", false, true);
        method.setTypeEquals(BaseType.STRING);
        new Parameter(method, "_showName").setExpression("true");

        uri = new TypedIOPort(this, "uri", false, true);
        uri.setTypeEquals(BaseType.STRING);
        new Parameter(uri, "_showName").setExpression("true");

        headers = new TypedIOPort(this, "headers", false, true);
        new Parameter(headers, "_showName").setExpression("true");
        headers.setTypeAtMost(BaseType.RECORD);

        parameters = new TypedIOPort(this, "parameters", false, true);
        new Parameter(parameters, "_showName").setExpression("true");
        parameters.setTypeAtMost(BaseType.RECORD);

        body = new TypedIOPort(this, "body", false, true);
        new Parameter(body, "_showName").setExpression("true");
        body.setTypeEquals(BaseType.STRING);

        requestor = new TypedIOPort(this, "requestor", false, true);
        new Parameter(requestor, "_showName").setExpression("true");
        requestor.setTypeEquals(BaseType.STRING);

        cookies = new TypedIOPort(this, "cookies", false, true);
        new Parameter(cookies, "_showName").setExpression("true");
        cookies.setTypeAtMost(BaseType.RECORD);

        ////////////////// Parameters
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

    /** An output that sends the body of the request, or an empty string
     *  if there isn't one. At this time, only a string body is supported,
     *  so the type of this port is string.
     */
    public TypedIOPort body;

    /** An output that sends the cookies specified by the
     *  {@link #requestedCookies} parameter, with values
     *  provided by a get request. If the get request does
     *  have cookies with names matching those in requestedCookies,
     *  then those values will be empty strings.
     *  The output will be a RecordToken with the field names given by
     *  requestedCookies, and the field values being strings.
     */
    public TypedIOPort cookies;

    /** The header information of an HTTP request as a record.
     *  Standard message header field names are given at
     *  {@link http://www.iana.org/assignments/message-headers/message-headers.xml#perm-headers}
     *  (see also {@link http://en.wikipedia.org/wiki/List_of_HTTP_header_fields}).
     *  Common header fields include
     *  <ul>
     *  <li> "Accept", which designates what Content-Type
     *  for responses are acceptable, and might have value, for example, "text/html".
     *  <li> "Content-Type", which designates the content type of the body of the request
     *  (for POST and PUT requests that include a body).
     *  <li> "Date", the date and time that the request was sent, in HTTP-date format.
     *  <li> "Host", the domain name (and port) of the server to which the request is sent.
     *  <li> "User-Agent", a string identifying the originator of the request (see
     *  {@link en.wikipedia.org/wiki/User_agent}).
     *  </ul>
     *  See the class comments for type constraints
     *  on output ports that produce records.
     */
    public TypedIOPort headers;

    /** An output port that sends a string indicating the method of
     *  the HTTP request, including at least the possibilities "GET"
     *  and "POST".
     */
    public TypedIOPort method;

    /** An output port that sends a record detailing any
     *  parameters included in an HTTP request.
     *  These are values appended to the URL in the form
     *  of ...?name=value. The output will be a record with
     *  one field for each name. If the request assigns multiple
     *  values to the same name, then the field value of the record
     *  will be an array of strings. Otherwise, it will simply
     *  be a string. See the class comments for type constraints
     *  on output ports that produce records.
     */
    public TypedIOPort parameters;

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
     *  then an exception will be thrown by the
     *  {@link WebServer}.
     */
    public StringParameter path;

    /** An array of names of cookies that this actor should retrieve from
     *  an HTTP request and produce on the cookies output
     *  port. This is an array of strings that defaults to an empty array.
     */
    public Parameter requestedCookies;

    /** Output port that produces the name or IP address of the
     *  client or the last proxy that sent the request.
     *  This is a string.
     */
    public TypedIOPort requestor;

    /** An input port on which to provide the
     *  response body to issue to an HTTP request. When this input port
     *  receives an event, if there is a pending request from
     *  a web server, then that pending request responds with the
     *  value of the input. Otherwise, the response is discarded.
     */
    public TypedIOPort responseBody;

    /** An input port on which to provide the
     *  response code to issue to an HTTP request. When this input port
     *  receives an event, if there is a pending request from
     *  a web server, then that pending request will receive
     *  the specified code as a response. If there is no code
     *  provided, but a response has been provided on the
     *  {@link #responseBody} input port, then the response code
     *  will be 200 (OK). Standard response codes are described at
     *  {@link http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html}.
     *  This port has type int.
     */
    public TypedIOPort responseCode;

    /** The content type of the response.
     *  This is a string with default value "text/html",
     *  which specifies that the content type of the response is text in HTML format.
     *  Valid alternatives include "text/plain", "text/csv", "text/xml", "application/javascript",
     *  "application/json", and many others.
     *  Standard content types are described at
     *  {@link http://en.wikipedia.org/wiki/Internet_media_type}.
     */
    public PortParameter responseContentType;

    /** The header data to include in the response.
     *  This is a record containing any number of fields.
     *  The string value of each field will be the value of the header.
     *  By default, it contains an empty record, which means that the only header
     *  information provided in the response will be Content-Type as given by
     *  {@link #responseContentType}.
     *  Standard message header field names are given at
     *  {@link http://www.iana.org/assignments/message-headers/message-headers.xml#perm-headers}
     *  (see also {@link http://en.wikipedia.org/wiki/List_of_HTTP_header_fields}).
     */
    public PortParameter responseHeaders;

    /** An input on which to provide new cookies and/or new cookie values.
     *  These will be set in the response when data is received on the {@link #responseBody}
     *  input in the same firing, or if there is no token on the responseBody
     *  input, will be treated as cookies accompanying an empty string response.
     *  This has type record.
     */
    public TypedIOPort setCookies;

    /** The time in milliseconds to wait after producing the details
     *  of a request on the output ports for a response to appear at
     *  the input ports. This is a long that defaults to 30,000.
     *  If this time expires before an input is received, then this actor
     *  will issue a generic timeout response to the HTTP request.
     *  If this actor later receives a response, and there is no pending
     *  request, it will discard that response.
     */
    public Parameter timeout;

    /** An output port that sends the relative URI of a get request,
     *  which must match the pattern given by the <i>path</i> parameter.
     *  This has type string.
     */
    public TypedIOPort uri;

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
        HttpRequestHandler newObject = (HttpRequestHandler) super
                .clone(workspace);
        newObject._initializeModelTime = null;
        newObject._initializeRealTime = 0L;
        newObject._URIpath = null;

        newObject._pendingRequest = null;
        newObject._newRequest = null;
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

    /** Generate an HTTP response for the client if an exception occurs in
     *  the model and there is a {@link CatchExceptionAttribute} in the model.
     *  No response will be received on the input port in the event of such an
     *  exception.
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
        if (_pendingRequest != null) {
            if (policy.equals(CatchExceptionAttribute.RESTART)) {
                _respondWithRetryMessage(10);
            } else {
                _respondWithServerErrorMessage();
            }
            // Request is no longer pending.
            _pendingRequest = null;
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

    /** Create and return an HttpServlet that is used to handle requests that
     *  arrive at the path given by the <i>path</i> parameter.
     *  This method is required by the HttpService interface.
     *  @return An HttpServlet to handle requests.
     */
    @Override
    public HttpServlet getServlet() {
        if (_debugging) {
            _debug("*** Creating new servlet.");
        }
        // The relative path for the servlet is calculated in attributeChanged()
        // since the path might not be a valid URI and could throw an exception
        // The getServlet() method does not throw an exception
        return new ActorServlet();
    }

    /** If there are input tokens on the {@link #responseBody},
     *  {@link #setCookies}, or {@link #responseCode} ports and there is a pending
     *  request, then send a response. Otherwise, if the servlet has received
     *  an HTTP request, then also produce on the output ports
     *  the details of the request.
     *  @exception IllegalActionException If sending the outputs fails.
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

        // Handle the response data.
        if (responseBody.hasToken(0)) {
            responseData.response = ((StringToken) responseBody.get(0))
                    .stringValue();
            responseFound = true;
            if (_debugging) {
                _debug("Received response on the response input port: "
                        + responseData.response);
            }
        }
        // Handle the code input.
        if (responseCode.getWidth() > 0 && responseCode.hasToken(0)) {
            responseData.statusCode = ((IntToken) responseCode.get(0))
                    .intValue();
            responseFound = true;
            if (_debugging) {
                _debug("Received response code: " + responseData.statusCode);
            }
        }
        // Handle the responseContentType input.
        responseContentType.update();
        responseData.contentType = ((StringToken) responseContentType
                .getToken()).stringValue();

        // Handle the responseHeaders input.
        responseHeaders.update();
        responseData.headers = (RecordToken) responseHeaders.getToken();

        if (responseFound) {
            if (_pendingRequest == null) {
                // There is no pending request, so ignore the response.
                if (_debugging) {
                    _debug("Discarding the response because there is no pending request.");
                }
            } else {
                _response = responseData;
                // Indicate that the request has been handled.
                _pendingRequest = null;
                // Notify the servlet thread.
                notifyAll();
            }
        }

        // If there is a new request, produce outputs for that request,
        // including any cookies from that request.
        if (_newRequest != null) {
            // Indicate that this request is now pending.
            _pendingRequest = _newRequest;
            _newRequest = null;

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

            if (_debugging) {
                _debug("Sending request method: " + _pendingRequest.method);
                _debug("Sending request URI: " + _pendingRequest.requestURI);
            }
            // Produce header output.
            if (_pendingRequest.headers != null
                    && _pendingRequest.headers.length() > 0) {
                if (!headers.sinkPortList().isEmpty()) {
                    if (_debugging) {
                        _debug("Sending request header: "
                                + _pendingRequest.headers);
                    }
                    try {
                        headers.send(0, _pendingRequest.headers);
                    } catch (TypedIOPort.RunTimeTypeCheckException ex) {
                        // Parameters provided do not match the required type.
                        // Construct an appropriate response.
                        _respondWithBadRequestMessage(_pendingRequest.headers,
                                headers.getType(), "header");
                        return;
                    }
                }
            }
            // Produce requestor output.
            if (_pendingRequest.requestor != null
                    && _pendingRequest.requestor.length() > 0) {
                if (_debugging) {
                    _debug("Sending requestor identification: "
                            + _pendingRequest.requestor);
                }
                requestor.send(0, new StringToken(_pendingRequest.requestor));
            }
            if (!parameters.sinkPortList().isEmpty()) {
                if (_debugging) {
                    _debug("Sending request parameters: "
                            + _pendingRequest.parameters);
                }
                // Send parameters, but handle type errors locally.
                try {
                    parameters.send(0, _pendingRequest.parameters);
                } catch (TypedIOPort.RunTimeTypeCheckException ex) {
                    // Parameters provided do not match the required type.
                    // Construct an appropriate response.
                    _respondWithBadRequestMessage(_pendingRequest.parameters,
                            parameters.getType(), "parameters");
                    return;
                }
            }
            if (_pendingRequest.cookies != null
                    && _pendingRequest.cookies.length() > 0) {
                if (!cookies.sinkPortList().isEmpty()) {
                    if (_debugging) {
                        _debug("Sending request cookies: "
                                + _pendingRequest.cookies);
                    }
                    try {
                        cookies.send(0, _pendingRequest.cookies);
                    } catch (TypedIOPort.RunTimeTypeCheckException ex) {
                        // Parameters provided do not match the required type.
                        // Construct an appropriate response.
                        _respondWithBadRequestMessage(_pendingRequest.cookies,
                                cookies.getType(), "cookies");
                        return;
                    }
                }
            }
            // Send the body of the request.
            body.send(0, new StringToken(_pendingRequest.body));
            // Send the following only if the above succeeded.
            // Otherwise, we will send two responses to this one request.
            method.send(0, new StringToken(_pendingRequest.method));
            uri.send(0, new StringToken(_pendingRequest.requestURI));
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
        _pendingRequest = null;
        _newRequest = null;
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
    ////                         protected methods                 ////

    /** Handle an HTTP request by recording the request data, requesting
     *  a firing of this actor at the current time, waiting for a response
     *  to be created, and then sending the response. Normally, the actor
     *  will fire once to produce the request data on its output ports,
     *  then fire again to receive the response data on its input ports.
     *  Upon that second firing, the thread in which this method is invoked
     *  will be notified, and this method will retrieve the response data
     *  that were provided at the input ports and send the response to the
     *  requestor.
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
            HttpServletResponse response, String type) throws ServletException,
            IOException {
        // The following code block is synchronized on the enclosing
        // actor. This lock _is_ released while waiting for the response,
        // allowing the fire method to execute its own synchronized blocks.
        synchronized (HttpRequestHandler.this) {
            // FIXME: What if there is already a _newRequest that has not been handled?
            // Is this possible?
            // This will simply overwrite it. For now, print a warning to standard error.
            if (_newRequest != null) {
                System.err
                        .println(getFullName()
                                + ": WARNING. Discarding HTTP request that has not yet been handled.");
            }
            _newRequest = new HttpRequestItems();

            _newRequest.requestURI = request.getRequestURI();
            _newRequest.method = type;

            if (_debugging) {
                _debug("**** Handling a " + type + " request to URI "
                        + _newRequest.requestURI);
            }

            try {
                // Read cookies from the request and store.
                _newRequest.cookies = _readCookies(request);

                // Read header information from the request and store.
                _newRequest.headers = _readHeaders(request);

                // Get the name or IP address of the requestor.
                _newRequest.requestor = request.getRemoteHost();

                // Get the parameters that have been either posted or included
                // as assignments in a get using the URL syntax ...?name=value.
                // Note that each parameter name may have more than one value,
                // hence the array of strings.
                _newRequest.parameters = _readParameters(request);

                // Read the body of the request. This may be an empty string.
                _newRequest.body = _readBody(request);

                // Figure out what time to request a firing for.
                long elapsedRealTime = System.currentTimeMillis()
                        - _initializeRealTime;

                // Assume model time is in seconds, not milliseconds.
                Time timeOfRequest = _initializeModelTime
                        .add(elapsedRealTime / 1000.0);

                if (_debugging) {
                    _debug("**** Request firing at time " + timeOfRequest);
                }

                // Request a firing of this actor (to produce output data).
                // Note that fireAt() will modify the requested firing time
                // if it is in the past.
                // Note that past firing times might not be modified
                // if ThreadedComposite actors are used (since the request
                // might be at a present time inside the ThreadedComposite,
                // but a past time for the top-level model).
                getDirector().fireAt(HttpRequestHandler.this, timeOfRequest);
            } catch (IllegalActionException e) {
                _newRequest = null;
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        e.getMessage());
                return;
            }
            //////////////////////////////////////////////////////
            // Wait for a response.
            // We are assuming every request gets exactly one response.
            // Normally, the actor will have fired twice now, first in
            // response to our firing request, and then again in response
            // to input data. The first firing will have sent the
            // _newRequest data to the output ports, moved the
            // data to _pendingRequest, and set _newRequest to null.
            // The second firing could conceivable have another _newRequest,
            // if this method is called again during the wait() call below,
            // in which case the second firing will have input data for
            // a response to the request in _pendingRequest, and new
            // request data in _newRequest, which it will send to its
            // output ports.
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
                    // Wait for notification that a _response has been
                    // constructed.
                    HttpRequestHandler.this.wait(timeoutValue);
                    if (_response == null
                            && System.currentTimeMillis() - startTime >= timeoutValue) {
                        // A timeout has occurred, and there is still no _reponse.
                        // This means that the second firing never occurred, so no
                        // response data have been provided.
                        if (_debugging) {
                            _debug("**** Request timed out.");
                        }

                        // Set the content length (enables persistent
                        // connections) and send the buffer
                        response.sendError(
                                HttpServletResponse.SC_REQUEST_TIMEOUT,
                                "Request Timeout (408)");

                        // Indicate that there is no longer a pending request.
                        // This ensures that if the second firing later occurs,
                        // its response data are not sent.
                        _pendingRequest = null;

                        return;
                    }
                } catch (InterruptedException e) {
                    if (_debugging) {
                        _debug("*** Request thread interrupted.");
                    }
                    response.sendError(
                            HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                            "Get request thread interrupted (Internal Server Error, 500)");

                    // Indicate that there is no longer a pending request or response.
                    _pendingRequest = null;
                    _response = null;

                    return;
                }
            }

            // At this point, we are assured that _response is not null.
            response.setStatus(_response.statusCode);
            response.setContentType(_response.contentType);

            // Write all the headers to the response.
            if (_response.headers != null) {
                for (String name : _response.headers.labelSet()) {
                    Token token = _response.headers.get(name);
                    if (token instanceof StringToken) {
                        response.addHeader(name,
                                ((StringToken) token).stringValue());
                    } else if (token != null) {
                        response.addHeader(name, token.toString());
                    }
                }
            }

            // Write all cookies to the response, if there are some new
            // cookies to write
            if (_response.hasNewCookies) {
                _writeCookies(_response.cookies, response);
            }
            if (_debugging) {
                _debug("**** Servet received response: " + _response.response);
            }

            // Set up a buffer for the output so we can set the length of
            // the response, thereby enabling persistent connections
            // http://docstore.mik.ua/orelly/java-ent/servlet/ch05_03.htm
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            PrintWriter writer = new PrintWriter(bytes, true); // true forces flushing.

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

    /** Read the body information from the HttpServletRequest, which at this time
     *  is constrained to be a string.
     *  @param request  The HttpServletRequest to read header information from.
     *  @return A string containing the body.
     *  @exception IllegalActionException If construction of the record token fails.
     *  @exception IOException
     */
    protected String _readBody(HttpServletRequest request)
            throws IllegalActionException, IOException {
        BufferedReader reader = request.getReader();
        StringBuffer result = new StringBuffer();
        String line = reader.readLine();
        while (line != null) {
            result.append(line);
            result.append(StringUtilities.LINE_SEPARATOR);
        }
        return result.toString();
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
            String label = ((StringToken) labels.getElement(i)).stringValue();
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

    /** Read the header information from the HttpServletRequest, construct
     *  a record token with one field for each header name. If a header name
     *  has just one value, the field value will be a string. Otherwise,
     *  it will be an array of strings.
     *  @param request  The HttpServletRequest to read header information from.
     *  @return A record of header fields.
     *  @exception IllegalActionException If construction of the record token fails.
     */
    protected RecordToken _readHeaders(HttpServletRequest request)
            throws IllegalActionException {
        LinkedHashMap<String, Token> map = new LinkedHashMap<String, Token>();
        Enumeration<String> names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            LinkedList<StringToken> valueList = new LinkedList<StringToken>();
            Enumeration<String> values = request.getHeaders(name);
            while (values.hasMoreElements()) {
                valueList.add(new StringToken(values.nextElement()));
            }
            if (valueList.isEmpty()) {
                // Do not include empty headers.
                continue;
            }
            if (valueList.size() == 1) {
                map.put(name, valueList.get(0));
            } else {
                map.put(name,
                        new ArrayToken(valueList
                                .toArray(new StringToken[valueList.size()])));
            }
        }
        if (map.isEmpty()) {
            // Return an empty record.
            return RecordToken.EMPTY_RECORD;
        }
        return new RecordToken(map);
    }

    /** Read the parameters from the HttpServletRequest, construct
     *  a record token containing the parameters, and return that record.
     *  @param request  The HttpServletRequest to read parameters from.  The
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
     *  According to: {@link www.w3.org/Protocols/rfc2616/rfc2616-sec10.html},
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
     *  This creates a response page that includes
     *  Javascript that will invoke the retry.
     *  FIXME: This only supports GET and POST requests currently.
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
        if (_pendingRequest.method.equals("GET")) {
            ajax = "jQuery.get(\""
                    + _pendingRequest.requestURI
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
        } else if (_pendingRequest.method.equals("POST")) {
            StringBuffer parameters = new StringBuffer("{");
            if (_pendingRequest.parameters != null) {
                for (String label : _pendingRequest.parameters.labelSet()) {
                    // TODO:  Test if this works for strings
                    // I believe these require quotation marks around them
                    parameters.append(label + ": "
                            + _pendingRequest.parameters.get(label).toString()
                            + ",");
                }

                // Erase the last , and add }
                if (parameters.length() > 0) {
                    parameters.deleteCharAt(parameters.length() - 1);
                    parameters.append('}');
                    ajax = "jQuery.post(\""
                            + _pendingRequest.requestURI
                            + "\", "
                            + parameters.toString()
                            + ")\n"
                            + ".done(function(data) { \n "
                            + "result = \"<div>\" + data + \"</div>\";"
                            + "jQuery(\"#contents\").html(jQuery(result).find(\"#contents\").html());"
                            + "\n });";
                } else {
                    ajax = "jQuery.post(\""
                            + _pendingRequest.requestURI
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

    /** The pending request, for which outputs have not yet been generated. */
    private HttpRequestItems _newRequest;

    /** The pending request, for which a response has not yet been issued,
     *  but outputs have been generated to trigger the response.
     */
    private HttpRequestItems _pendingRequest;

    /** The pending response. */
    private HttpResponseItems _response;

    /** The URI for the relative path from the "path" parameter.
     *  A URI is used here to make sure the "path" parameter conforms to
     *  all of the URI naming conventions.
     */
    private URI _URIpath;

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
    @SuppressWarnings("serial")
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
        @Override
        protected synchronized void doGet(HttpServletRequest request,
                HttpServletResponse response) throws ServletException,
                IOException {
            _handleRequest(request, response, "GET");
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
        @Override
        protected synchronized void doPost(HttpServletRequest request,
                HttpServletResponse response) throws ServletException,
                IOException {
            _handleRequest(request, response, "POST");
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// HttpRequestItems

    /** A data structure with all the relevant information about an
     *  HTTP request.
     */
    protected static class HttpRequestItems {
        /** The body of the request. Only strings are supported for now. */
        public String body;

        /** Cookies associated with the request. */
        public RecordToken cookies;

        /** Header fields associated with the request. */
        public RecordToken headers;

        /** Parameters received in a get or post. */
        public RecordToken parameters;

        /** The type of request (the method). */
        public String method;

        /** The name or IP address of the originator of the request (or proxy). */
        public String requestor;

        /** The URI issued in the get request. */
        public String requestURI;
    }

    ///////////////////////////////////////////////////////////////////
    //// HttpResponseItems

    /** A data structure with all the relevant information about an
     *  HTTP response.
     */
    protected static class HttpResponseItems {
        /** The content type of the response. This defaults to "text/html". */
        public String contentType;

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

        /** Headers to include in the response, if any. */
        public RecordToken headers;

        /** The text of the response. */
        public String response;

        /** Status code of the response. */
        public int statusCode = HttpServletResponse.SC_OK;
    }
}
