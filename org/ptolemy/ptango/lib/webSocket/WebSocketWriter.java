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

package org.ptolemy.ptango.lib.webSocket;

import java.net.URI;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
////WebSocketWriter

/** An actor that writes information to a websocket.  Multiple writers to the
 * same URL path are allowed.  
 *
 *  @author Elizabeth Latronico
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (ltrnc)
 *  @Pt.AcceptedRating Red (ltrnc)
 */

public class WebSocketWriter extends TypedAtomicActor implements
        WebSocketService {

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
        
        client = new Parameter(this, "client");
        client.setToken("true");
        // Separate menu items are available for clients and servlets.
        // This parameter is set in the menu.
        client.setVisibility(Settable.NOT_EDITABLE);

        input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(BaseType.STRING);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    
    /** A flag indicating if the reader acts as a client or as part of 
     * the server.  True for client; false for server.
     */
    public Parameter client;

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
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (not thrown in this base class).
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == path) {
            // Unsubscribe from previous path (if any)
            if (_URIpath != null && !_URIpath.toString().isEmpty()
                    && _endpointManager != null) {
            // TODO: Shared vs. individual
                _endpointManager.unsubscribe(this, _URIpath.toString());
            }
            
            // TODO:  Allow dynamic subscribing here
            // If the endpoint exists, service should be added as a subscriber
            // If the endpoint does not exists, a new one should be created
            // and started ONLY if the model is currently executing

            String pathValue = ((StringToken) path.getToken()).stringValue();
            _URIpath = WebSocketEndpointManager.pathToURI(pathValue);
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
        WebSocketWriter newObject = (WebSocketWriter) super.clone(workspace);

        newObject._endpoint = null;
        newObject._endpointManager = null;
        newObject._URIpath = null;
        return newObject;
    }
    
    /** Subscribe this service to the appropriate endpoint.
     *  @exception IllegalActionException If the parent throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        
        if (_endpointManager == null) {
            _endpointManager = WebSocketEndpointManager.getInstance();
        }
        
        // Subscribe to this endpoint.  Creates a new endpoint if needed.
        // Do not subscribe local clients.  The WebServer handles these, since 
        // it ensures that connections are only opened after it starts.
        if (! (!WebSocketEndpointManager.isRemoteURI(_URIpath) && isClient())) {
            _endpointManager.subscribe(this, _URIpath.toString());
        }
    }

    /** Write a message to the websocket.
     * @exception IllegalActionException If the websocket connection cannot be
     * established or if the message cannot be written.
     */

    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        // Read token on input port, if any.  Write token contents to socket.
        // Empty string tokens are acceptable

        if (input.hasToken(0)) {
            
            // Send message through endpoint.  
            // TODO:  Enhance to allow waiting on connection open, buffering,...
            String message = ((StringToken) input.get(0)).stringValue();
            if (_endpoint == null) {
                throw new IllegalActionException(this, "Cannnot connect to " 
                        + "websocket");
            }
            
            if (!_endpoint.sendMessage(message)){
                throw new IllegalActionException(this, "Cannot write to "
                        + "WebSocket");
            }
        }
    }

    /** Return the relative path that this WebSocketService is mapped to,
     *  which is the value of the <i>path</i> parameter.
     *  @return The relative path that this WebSocketService is mapped to.
     */
    @Override
    public URI getRelativePath() {
        return _URIpath;
    }
    
    /** Return true if this actor acts as a client; false if it acts as a 
     * part of the server (responding to incoming messages).
     * @return True if this actor acts as a client; false if it acts as a 
     * part of the server (responding to incoming messages).
     */
    @Override
    public boolean isClient() {
        boolean isClient = true;
        // Assume client side if no value given
        try {
            isClient = ((BooleanToken) client.getToken()).booleanValue();
        } catch(IllegalActionException e){};
        return isClient;
    }

    /** Do nothing upon receipt of a message, since this actor is a writer.
     * @param message The message that was received from the websocket.
     */
    @Override
    public void onMessage(String message) {
    }
    
    /** Set the endpoint responsible for websocket communication.  
     * @param endpoint  The endpoint responsible for websocket communication. 
     */
    @Override
    public void setEndpoint(WebSocketEndpoint endpoint) {
        _endpoint = endpoint;
    }
    
    /** Unsubscribe this service from the endpoint manager.  The endpoint 
     * manager will close connections with no subscribers.
     * @throws IllegalActionException If thrown by the parent.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        _endpointManager.unsubscribe(this, _URIpath.toString());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The endpoint used for obtaining a connection to send messages through.*/
    private WebSocketEndpoint _endpoint;
    
    /** A manager responsible for generating and releasing websockets. */
    private WebSocketEndpointManager _endpointManager;

    /** The URI for the relative path from the "path" parameter.
     *  A URI is used here to make sure the "path" parameter conforms to
     *  all of the URI naming conventions.
     */
    private URI _URIpath;

}
