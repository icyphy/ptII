/* An actor that writes information to a websocket.

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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketClient;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.StringToken;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
////WebSocketWriter

/** An actor that writes information to a websocket.  Multiple writers to the 
 * same URL path are allowed.  (Internally, this is managed using a separate 
 * websocket connection for each writer).
 *
 *  @author Elizabeth Latronico 
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (ltrnc)
 *  @Pt.AcceptedRating Red (ltrnc)
 */

public class WebSocketWriter extends TypedAtomicActor 
    implements WebSocketService {

    /** Create an instance of the actor.
     *  @param container The container
     *  @param name The name.
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the superclass throws it.
     */
    public WebSocketWriter(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        path = new StringParameter(this, "path");
        path.setExpression("/*");
        
        input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(BaseType.STRING);
        
        // Assume false until the path is set
        _isLocal = false;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    
    /** A port that accepts messages to write to the websocket. */
    public TypedIOPort input;
    
    /** The URL affiliated with the websocket.  Can refer to a locally 
     * hosted websocket or a remotely hosted websocket.  Locally hosted
     * websockets have paths of the form /* such as / or /mysocket 
     * or /mysocket/first   .  These will internally be translated to 
     * ws://localhost:port/path e.g. ws://localhost:8078/mysocket    .
     * Paths for remotely hosted websockets should start with ws:// or wss://  
     */
    public StringParameter path;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** React to a change in an attribute.  In this case, check the
     *  value of the <i>path</i> attribute to make sure it is a valid URI.
     *  
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
                // Paths not starting with ws:// are assumed to be local
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
     * 
     *  @param workspace The workspace in which to place the cloned attribute.
     *  @return The cloned attribute.
     *  @exception CloneNotSupportedException Not thrown in this base class.
     *  @see java.lang.Object#clone()
     */
    
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        WebSocketWriter newObject = (WebSocketWriter) super.clone(workspace);
        
        newObject._client = null;
        newObject._connection = null;
        newObject._connectionFuture = null;
        newObject._connectionTimeout = 5000;
        newObject._isLocal = false;
        newObject._URIpath = null;
        return newObject;
    }

    /** Open the websocket connection, if not already open, and write a 
     * message to it.
     * 
     * @exception IllegalActionException If the websocket connection cannot be 
     * established or if the message cannot be written.
     */
    
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        
        if (_connection == null) {
            
            if (_connectionFuture == null) {
                throw new IllegalActionException(this, "WebSocket connection " 
                        + "not pproperly established");
            }
            
            try {
                _connection = _connectionFuture
                        .get(_connectionTimeout, TimeUnit.MILLISECONDS);
            } catch(TimeoutException e) {
                throw new IllegalActionException(this, "Tiemout establishing" 
                        + " WebSocket connection");
            } catch(ExecutionException e) {
                throw new IllegalActionException(this, "Can't establish "
                        + "WebSocket connection");
            } catch(InterruptedException e) {
                throw new IllegalActionException(this, "WebSocket connection " 
                        + "establishment interrupted");
            }
        }
        
        // Read token on input port, if any.  Write token contents to socket.
        // Empty string tokens are acceptable
        
        if (input.hasToken(0)) {
            String message = ((StringToken) input.get(0)).stringValue();
            if (_connection != null) {
                try {
                    _connection.sendMessage(message);
                } catch(IOException e) {
                    throw new IllegalActionException(this, "Can't write to " 
                            + "WebSocket");
                }
            } else {
                throw new IllegalActionException(this, "Web socket is closed; " 
                        + "cannot write to web socket.");
            }
        }
    }
    
    /** Return the relative path that this WebSocketService is mapped to,
     *  which is the value of the <i>path</i> parameter.
     *  
     *  @return The relative path that this WebSocketService is mapped to.
     *  @see #setRelativePath(URI)
     */
    @Override
    public URI getRelativePath() {
        return _URIpath;
    }
    
    /** Open any websocket connections to remote URIs.  Connections to local
     * URIs will be opened by the web server once it starts up.   
     *  @exception IllegalActionException If the parent throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        
        // Open any websockets connecting to remote locations
        if (!_isLocal) {
            open(_URIpath);
        }
        
        // The server will open websockets to local locations later once it
        // has acquired a port.  The port number is needed for the URL.
    }
    
    /** Do nothing upon receipt of a message, since this actor is a writer.
     * 
     * @param sender The WebSocketEndpoint that sent the message.
     * @param message The message that was received from the websocket.
     */
    public void onMessage(WebSocketEndpoint sender, String message) {
    }
    
    /** Open the WebSocket connection on the given port.
     * 
     * @param path The URI to connect to.
     * @exception IllegalActionException If the websocket cannot be opened.
     */
    public void open(URI path) throws IllegalActionException {
        
        // Based on http://download.eclipse.org/jetty/stable-8/apidocs/org/eclipse/jetty/websocket/WebSocketClient.html
        // and http://stackoverflow.com/questions/19770278/jetty-8-1-1-websocket-client-handshake
        
        // Create a new client
        try {
            _client = PtolemyWebSocketClientFactory.getInstance()
                    .newWebSocketClient();
        } catch(Exception e) {
            throw new IllegalActionException(this, 
                    "Can't create WebSocket client");
        }
        
        // Request connection.  _client.open() is a non-blocking operation.
        try {
            WebSocketEndpoint endpoint = new WebSocketEndpoint(this);             
            _connectionFuture = _client.open(path, endpoint);
                 
            if (_debugging) {
                _debug("Websocket connection opened for " + getName());
            }
        } catch(IOException e){
            throw new IllegalActionException(this, 
                    "Can't open WebSocket connection");
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
    
    /** Close any open WebSocket connections.
    *
    * @exception IllegalActionException If thrown by the parent. 
    */
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
    
    /** A Future object for the WebSocket connection. */
    private Future<WebSocket.Connection> _connectionFuture;
    
    /** The timeout for establishing a connection.  In milliseconds. */
    private int _connectionTimeout = 5000; 
    
    /** True if the WebSocket is hosted locally be the Ptolemy model; false 
     *  otherwise.
     */
    private boolean _isLocal;
    
    /** The URI for the relative path from the "path" parameter.
     *  A URI is used here to make sure the "path" parameter conforms to
     *  all of the URI naming conventions.
     */
    private URI _URIpath;
}