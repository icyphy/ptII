/* An actor that handles an HttpRequest by producing an output and waiting for an input.

 Copyright (c) 1997-2011 The Regents of the University of California.
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
import java.net.URI;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
import ptolemy.data.LongToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;


/** An actor that handles an HTTP request by producing an output and
 *  waiting for an input to give a response.
 *  This actor creates a servlet.
 *  When this actor receives an HTTP request, if the model is running
 *  (initialize() has been called and wrapup() has not), then it
 *  issues a request for the director to fire it at the greater of
 *  the current time of the director or the time elapsed since
 *  the last invocation of initialize() (in seconds).
 *  When the actor fires, it produces on its output ports the details
 *  of the request. It expects to be fired again some time later
 *  with the response to send appearing on its input ports.
 *  If that response does not arrive within <i>timeout</i>
 *  (default 10000) milliseconds, then it issues a timeout response.
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
     *  @throws IllegalActionException If the superclass throws it.
     *  @throws NameDuplicationException If the super
     */
    public HttpActor(CompositeActor container, String name)
        throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        path = new StringParameter(this, "path");
        path.setExpression("/*");
        
        response = new TypedIOPort(this, "response", true, false);
        response.setTypeEquals(BaseType.STRING);
        
        cookies = new TypedIOPort(this, "cookies", false, true);
        // FIXME: The following requires the output to be a record.
        // This won't work until new type system is checked in.
        // cookies.setTypeAtMost(RecordType.EMPTY_RECORD);
        cookies.setTypeEquals(BaseType.GENERAL);
        new Parameter(cookies, "_showName").setExpression("true");
        
        timeout = new Parameter(this, "timeout");
        timeout.setExpression("10000L");
        timeout.setTypeEquals(BaseType.LONG);
        
        getRequestURI = new TypedIOPort(this, "getRequestURI", false, true);
        getRequestURI.setTypeEquals(BaseType.STRING);
        new Parameter(getRequestURI, "_showName").setExpression("true");

        putRequestURI = new TypedIOPort(this, "putRequestURI", false, true);
        putRequestURI.setTypeEquals(BaseType.STRING);
        new Parameter(putRequestURI, "_showName").setExpression("true");
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The cookies provided by a request.
     *  If there are any cookies, then the output provided by this
     *  port will be a record of the form {cookieName = value, ...}.
     */
    // FIXME: Cookies have many other fields. Do we need to include them?
    public TypedIOPort cookies;

    /** The relative URL to map this servlet to.
     *  This is a string that defaults to "/*", meaning that all
     *  requests are handled.
     */
    public StringParameter path;
    
    /** The relative URI of a get request.
     *  When a get request is received from
     *  a web server, then this actor will request a firing,
     *  and on that firing, it will send the URI
     *  of the request, relative to the base, to this output port.
     */
    public TypedIOPort getRequestURI;

    /** The relative URI of a put request.
     *  When a put request is received from
     *  a web server, then this actor will request a firing,
     *  and on that firing, it will send the URI
     *  of the request, relative to the base, to this output port.
     */
    public TypedIOPort putRequestURI;

    /** The response to issue to a request. When this input port
     *  receives an event, if there is a pending get request from
     *  a web server, then that pending request responds with the
     *  value of the input. Otherwise, the response is recorded,
     *  and the next get request received will be given the response.
     */
    public TypedIOPort response;
    
    /** The time in milliseconds to wait after producing the details
     *  of a request on the output ports for a response to appear at
     *  the input ports. This is a long that defaults to 10,000.
     */
    public Parameter timeout;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////
    
    /** React to a change in an attribute.  In this case, check the
     *  value of the <i>path</i> attribute to make sure it is a valid URI.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (not thrown in this base class).
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == path) {
            String pathValue = ((StringToken)path.getToken()).stringValue();
            try {
                if (!pathValue.trim().equals("")) {
                    _URIpath = URI.create(pathValue);
                } else {
                    _URIpath = URI.create("/*");
                }
            } catch(IllegalArgumentException e2){
                throw new IllegalActionException(this,
                        "Path is not a valid URI: " + pathValue);
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the attribute.
     *  @param workspace The workspace in which to place the cloned attribute.
     *  @exception CloneNotSupportedException Not thrown in this base class.
     *  @see java.lang.Object#clone()
     *  @return The cloned attribute.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        HttpActor newObject = (HttpActor) super.clone(workspace);
        newObject._initializeModelTime = null;
        return newObject;
    }

    /** Returns the relative path that this HttpService is mapped to.
     *  This method is required by the HttpService interface.
     *  @return The relative path that this HttpService is mapped to.
     */
    public URI getRelativePath() {
        return _URIpath;
    }

    /** Create and return an HttpServlet that is used to handle requests that
     *  arrive at the given path.
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
    
    /** FIXME
     *  @exception IllegalActionException FIXME.
     */
    public void fire() throws IllegalActionException {
        // The methods of the servlet are invoked in another
        // thread, so we use this actor for mutual exclusion.
        synchronized(this) {
            super.fire();
            if (response.getWidth() > 0 && response.hasToken(0)) {
                _response = ((StringToken)response.get(0)).stringValue();
                if (_debugging) {
                    _debug("Received response on the input port: " + _response);
                }
                // If there is a pending request, notify it.
                notifyAll();
            }
            // If there is a pending request, produce outputs for that request.
            if (_requestURI != null) {
                if (_requestType == 0) {
                    getRequestURI.send(0, new StringToken(_requestURI));
                } else {
                    putRequestURI.send(0, new StringToken(_requestURI));     
                }
                _requestURI = null;
                if (_cookies != null && _cookies.length > 0) {
                    // Construct a record.
                    String[] labels = new String[_cookies.length];
                    Token[] values = new Token[_cookies.length];
                    for (int i = 0; i < _cookies.length; i++) {
                        labels[i] = _cookies[i].getName();
                        values[i] = new StringToken(_cookies[i].getValue());
                    }
                    cookies.send(0, new RecordToken(labels, values));
                }
            }
        }
    }

    /** Set the relative path for which this actor will receive requests.  This is
     *  set here in case the path does not conform to URI syntax rules.  If the
     *  path does not conform, an IllegalActionException is thrown.
     *  @exception IllegalActionException If the path is invalid URI syntax
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _response = null;
        _requestURI = null;
        _initializeModelTime = getDirector().getModelTime();
        _initializeRealTime = System.currentTimeMillis();
    }
        
    /** Set the relative path that this HttpService is mapped to.
     *  This method is required by the HttpService interface.
     * @param path The relative path that this HttpService is mapped to.
     */
    public void setRelativePath(URI path) {
        _URIpath = path;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     private variables                     ////
    
    /** Cookies provided as part of a get request. */
    private Cookie[] _cookies;
    
    /** The model time at which this actor was last initialized. */
    private Time _initializeModelTime;
    
    /** The real time at which this actor was last initialized, in milliseconds. */
    private long _initializeRealTime;
    
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
    ////                     inner classes                         ////    
    
    /** FIXME 
     *  http://wiki.eclipse.org/Jetty/Tutorial/Embedding_Jetty
     */
    protected class ActorServlet extends HttpServlet {
        
        /** Handle an HTTP get request by creating a web page as the HTTP 
         *  response.
         *  NOTE: This method is synchronized, and the lock is _not_ released
         *  while waiting for the response. This strategy helps prevent a pending
         *  get request from overlapping with a pending put request. The second
         *  request will be postponed until the first has been completely handled.
         *  @see http://docs.oracle.com/javaee/6/api/javax/servlet/http/HttpServletRequest.html
         *  @param request The HTTP get request.
         *  @param response The HTTP response to write to.
         */
        protected synchronized void doGet(HttpServletRequest request, 
                HttpServletResponse response) 
                throws ServletException, IOException {
            _handleRequest(request, response, 0);
        }
        
        /** Handle an HTTP put request by creating a web page as the HTTP 
         *  response.
         *  NOTE: This method is synchronized, and the lock is _not_ released
         *  while waiting for the response. This strategy helps prevent a pending
         *  get request from overlapping with a pending put request. The second
         *  request will be postponed until the first has been completely handled.
         *  @see http://docs.oracle.com/javaee/6/api/javax/servlet/http/HttpServletRequest.html
         *  @param request The HTTP get request.
         *  @param response The HTTP response to write to.
         */
        protected synchronized void doPost(HttpServletRequest request, 
                HttpServletResponse response) 
                throws ServletException, IOException {
            _handleRequest(request, response, 1);
        }
        
        /** Handle an HTTP get or put request by creating a web page as the HTTP 
         *  response.
         *  @see http://docs.oracle.com/javaee/6/api/javax/servlet/http/HttpServletRequest.html
         *  @param request The HTTP get request.
         *  @param response The HTTP response to write to.
         *  @param type The type of request. 0 for get, 1 for put.
         */
        private void _handleRequest(HttpServletRequest request, 
                HttpServletResponse response, int type) 
                throws ServletException, IOException {
            // The following codeblock is synchronized on the enclosing
            // actor. This lock _is_ released while waiting for the response,
            // allowing the fire method to execute its own synchronized blocks.
            synchronized(HttpActor.this) {
                _requestURI = request.getRequestURI();
                _requestType = type;
                _cookies = request.getCookies();
                if (_debugging) {
                    _debug("Received get request with URI: " + _requestURI);
                    _debug("Requesting firing at the current time.");
                }
                Map<String, String[]> parameter = request.getParameterMap();
                // FIXME: Other information?
                // response.getWriter().println("session=" + request.getSession(true).getId());
                try {
                    long elapsedRealTime = System.currentTimeMillis() - _initializeRealTime;
                    Time timeOfRequest = _initializeModelTime.add(elapsedRealTime);
                    // Note that fireAt() will modify the requested firing time if it is in the past.
                    getDirector().fireAt(HttpActor.this, timeOfRequest);
                } catch (IllegalActionException e) {
                    // Will this response be seen?
                    response.setContentType("text/html");
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().println("<h1> Exception requesting firing:</h1><p>" + e.getMessage());
                    return;
                    // throw new ServletException(e.getMessage());
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
                            timeoutValue = ((LongToken)timeout.getToken()).longValue();
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
                if (timeoutOccurred) {
                    if (_debugging) {
                        _debug("Get request timed out.");
                    }
                    response.getWriter().println("<h1> Request timed out </h1>");
                } else if (_response == null) {
                    if (_debugging) {
                        _debug("Get request thread interrupted.");
                    }
                    response.getWriter().println("<h1> Get request thread interrupted </h1>");
                } else {
                    if (_debugging) {
                        _debug("Responding to get request: " + _response);
                    }
                    response.getWriter().println(_response);
                    _response = null;
                }
            }
        }
    }
}