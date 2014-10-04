/* An actor that reads incoming information from a websocket.

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

package org.ptolemy.ptango.lib.websocket;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketClient;
import org.ptolemy.ptango.lib.WebServer;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
import ptolemy.data.StringToken;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
////WebSocketReader

/** An actor that reads information from a websocket.  Multiple readers of the
 * same URL path are allowed.  (Internally, this is managed using a separate 
 * websocket connection for each reader). 
 *
 *  @author Elizabeth Latronico 
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (ltrnc)
 *  @Pt.AcceptedRating Red (ltrnc)
 */

public class WebSocketReader extends TypedAtomicActor 
    implements WebSocketService {
    /** Create an instance of the actor.
     *  @param container The container
     *  @param name The name.
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the superclass throws it.
     */
    public WebSocketReader(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        path = new StringParameter(this, "path");
        path.setExpression("/*");
        
        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.STRING);
        
        // Assume false until the path is set
        _isLocal = false;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    
    /** The URL affiliated with the websocket.  Can refer to a locally 
     * hosted websocket or a remotely hosted websocket.  Locally hosted
     * websockets have paths of the form /* such as / or /mysocket 
     * or /mysocket/first   .  These will internally be translated to 
     * ws://localhost:port/path e.g. ws://localhost:8078/mysocket    .
     * Paths for remotely hosted websockets should start with ws:// or wss://  
     */
    public StringParameter path;
    
    /** A port that outputs each message received from the websocket. */
    public TypedIOPort output;
    
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
                // Paths connecting to remote websockets should start with ws://
                // or wss:// (for secure websockets)
                // Paths not starting with these are assumed to be local
                // For locally hosted websockets should start with a "/" 
                // or be "*"
                if (!pathValue.trim().equals("")) {
                    // Check for common incorrect protocols
                    if (pathValue.startsWith("http") || 
                            pathValue.startsWith("ftp")) {
                      throw new IllegalActionException(this, "Remote websocket" 
                            + " paths must start with ws://");  
                    } 
                    
                    if (pathValue.startsWith("ws://") || 
                            pathValue.startsWith("wss://")) {
                        _URIpath = URI.create(pathValue);
                        _isLocal = false;
                    }
                    else if(!pathValue.trim().startsWith("/")) {
                        _URIpath = URI.create("/" + pathValue);
                        _isLocal = true;
                    } else {
                        _URIpath = URI.create(pathValue);
                        _isLocal = true;
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
     *  @return The cloned attribute.
     *  @exception CloneNotSupportedException Not thrown in this base class.
     *  @see java.lang.Object#clone()
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        WebSocketReader newObject = (WebSocketReader) super.clone(workspace);
        
        newObject._client = null;
        newObject._connection = null;
        newObject._connectionException = null;
        newObject._connectionFuture = null;
        newObject._connectionTimeout = 5000;
        newObject._initializeModelTime = null;
        newObject._initializeRealTime = 0L;
        newObject._isLocal = false;
        newObject._message = null;
        newObject._server = null;
        newObject._URIpath = null;
        return newObject;
    }
    
    /** Send the message from the websocket to the output port.
     *  
     *  @exception IllegalActionException If the message cannot be sent to the 
     *  output port.
     */

    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        
        if (_message != null) {
            output.send(0, new StringToken(_message));
        }
        
        _message = null;
    }
    
    /** Return the relative path that this WebSocketService is mapped to.
     *  
     *  @return The relative path that this WebSocketService is mapped to.
     *  @see #setRelativePath(URI)
     */
    @Override
    public URI getRelativePath() {
        return _URIpath;
    }
    
    /** Remember the time at which this actor was initialized in order to 
     *  request firings at a particular time.
     *  
     *  @exception IllegalActionException If the parent throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        
        _initializeModelTime = getDirector().getModelTime();
        // Subtract a tenth of a second.  As this actor is initialized
        // after the director, the initial time here could be later than what
        // the director perceives as the initial real time.
        // FIXME:  The director could offer e.g. a getInitialRealTime() function
        // to avoid this workaround
        _initializeRealTime = System.currentTimeMillis() - 100;
        
        // The server will initiate websocket opening later once the server
        // has acquired a port.  The port number is needed for the websocket's 
        // connection URL for locally hosted websockets.
    }
    
    /** Upon receipt of a message, store the message and request a firing. 
     * 
     * @param sender The WebSocketEndpoint that sent the message.
     * @param message The message that was received.
     */
    @Override
    public void onMessage(WebSocketEndpoint endpoint, String message) {
        _message = message;
        
        // Request a firing
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
        try {
            getDirector().fireAt(WebSocketReader.this, timeOfRequest);
        } catch (IllegalActionException e){
            // Can't throw an exception from the onMessage() method
            if (_debugging) {
                _debug("**** Firing denied for time " + timeOfRequest);
            }
        }
    }
    
    /** Open the WebSocket connection on the given port.
     * 
     * @param port  The port to use in the connection URL.
     * @exception IllegalActionException If the websocket cannot be opened.
     */
    public void open(int port) throws IllegalActionException {
        
        // Based on http://download.eclipse.org/jetty/stable-8/apidocs/org/eclipse/jetty/websocket/WebSocketClient.html
        // and http://stackoverflow.com/questions/19770278/jetty-8-1-1-websocket-client-handshake
        
        // Open a new connection to the web server
        try {
            _client = _server.newWebSocketClient();
        } catch(Exception e) {
            throw new IllegalActionException(this, 
                    "Can't create WebSocket client");
        }
        
        // Only local ones need to wait for the webserver to start
        // TODO:  Implement remote opening.  Refactor to not be dependent on 
        // WebServer.
        if (_isLocal) {
            // Request connection.  Wait it a separate thread to avoid blocking.
            try {
                WebSocketEndpoint endpoint = new WebSocketEndpoint(this);
                
                // Prepend ws://localhost:port to URI
                // TODO:  Add support for secure websockets
                URI path = URI.create("ws://localhost:" + port + 
                        _URIpath.toString());
                 _connectionFuture = _client.open(path, endpoint);
                 
                 // Create and start new runnable to obtain result of Future
                 _connectionThread = new Thread(new RunnableConnectionGetter());
                 _connectionThread.start();
                 
                 if (_debugging) {
                     _debug("Websocket connection opened for " + getName());
                 }
                 
            } catch(IOException e){
                throw new IllegalActionException(this, 
                        "Can't open WebSocket connection");
            }
        }
    }
    
    /** Set the relative path that this WebSocketService is mapped to.
     *  This method is required by the WebSocketService interface.
     *  
     *  @param path The relative path that this WebSocketService is mapped to.
     *  @see #getRelativePath()
     */
    @Override
    public void setRelativePath(URI path) {
        _URIpath = path;
    }
    
    /** Set the WebServer responsible for opening connections for this 
     *  websocket.
     *  
     *  @param server The WebServer responsible for opening connections for 
     *  this websocket.
     */
    public void setWebServer(WebServer server) {
        _server = server;
    }
    
    /** Close any open WebSocket connections.
    *
    * @exception IllegalActionException If thrown by the parent. */
   @Override
   public void wrapup() throws IllegalActionException {
       super.wrapup();
       
       _connection.close();
       _connection  = null;
   }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** The client for the WebSocket. Handles connection opening and closing. */
    private WebSocketClient _client; 
    
    /** The WebSocket connection. */
    private WebSocket.Connection _connection;
    
    /** The exception, if any,  raised from obtaining a WebSocket connection. */
    private Exception _connectionException;
    
    /** A Future object for the WebSocket connection */
    private Future<WebSocket.Connection> _connectionFuture;
    
    /** The timeout for establishing a connection.  In milliseconds. */
    private int _connectionTimeout = 5000; 
    
    /** The thread that obtains a WebSocket connection. */
    private Thread _connectionThread;
    
    /** The model time at which this actor was last initialized. */
    private Time _initializeModelTime;

    /** The real time at which this actor was last initialized, in milliseconds. */
    private long _initializeRealTime;
    
    /** True if the WebSocket is hosted locally be the Ptolemy model; false 
     *  otherwise.
     */
    private boolean _isLocal;
    
    /** The last message received. */
    private String _message;
    
    /** The WebServer attribute handling communication for this socket. */
    private WebServer _server;
    
    /** The URI for the relative path from the "path" parameter.
     *  A URI is used here to make sure the "path" parameter conforms to
     *  all of the URI naming conventions.
     */
    private URI _URIpath;
    
    /** A Runnable class to obtain a Connection object in a separate thread,
     *  so as not to block the main thread.
     */
    private class RunnableConnectionGetter implements Runnable {
        
        /** Obtain a Connection object. */
        @Override
        public void run() {
            if (_connection == null) {
                try {
                    _connection = _connectionFuture
                            .get(_connectionTimeout, TimeUnit.MILLISECONDS);
                } catch(Exception e) {
                    _connectionException = e;
                }
            }
        }

    }
}