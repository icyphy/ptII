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

package org.ptolemy.ptango.lib.webSocket;

import java.net.URI;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
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
////WebSocketReader

/** An actor that reads information from a websocket.  Multiple readers of the
 * same URL path are allowed.
 *
 *  @author Elizabeth Latronico
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (ltrnc)
 *  @Pt.AcceptedRating Red (ltrnc)
 */

public class WebSocketReader extends TypedAtomicActor implements
        WebSocketService {
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
        
        client = new Parameter(this, "client");
        client.setToken("true");
        // Separate menu items are available for clients and servlets.
        // This parameter is set in the menu.
        client.setVisibility(Settable.NOT_EDITABLE);

        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.STRING);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    
    /** A flag indicating if the reader acts as a client or as part of 
     * the server.  True for client; false for server.
     */
    public Parameter client;

    /** A port that outputs each message received from the websocket. */
    public TypedIOPort output;
    
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
                 && _endpointManager != null){
            // TODO: Shared vs. individual
                _endpointManager.unsubscribe(this, _URIpath.toString());
            }
            
            // TODO:  Allow dynamic subscribing here, vs. only in initialize()
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
        WebSocketReader newObject = (WebSocketReader) super.clone(workspace);

        newObject._endpointManager = null;
        newObject._initializeModelTime = null;
        newObject._initializeRealTime = 0L;
        newObject._message = null;
        newObject._URIpath = null;
        return newObject;
    }

    /** Send the message from the websocket to the output port.
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
    
    /** Get the URI associated with this service.
     * @return The URI associated with this service.
     */
    @Override
    public URI getRelativePath(){
        return _URIpath;
    }

    /** Remember the time at which this actor was initialized in order to
     *  request firings at a particular time. Subscribe this service to the 
     *  appropriate endpoint.
     *  @exception IllegalActionException If the parent throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        
        if (_endpointManager == null) {
            _endpointManager = WebSocketEndpointManager.getInstance();
        }

        _initializeModelTime = getDirector().getModelTime();
        // Subtract a tenth of a second.  As this actor is initialized
        // after the director, the initial time here could be later than what
        // the director perceives as the initial real time.
        // FIXME:  The director could offer e.g. a getInitialRealTime() function
        // to avoid this workaround
        _initializeRealTime = System.currentTimeMillis() - 100;

        // Subscribe to this endpoint.  Creates a new endpoint if needed.
        // Do not subscribe local clients.  The WebServer handles these, since 
        // it ensures that connections are only opened after it starts.
        if (! (!WebSocketEndpointManager.isRemoteURI(_URIpath) && isClient())) {
            _endpointManager.subscribe(this, _URIpath.toString());
        }
    }
    
    /** Return true if this actor acts as a client; false if it acts as a 
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

    /** Upon receipt of a message, store the message and request a firing.
     * @param message The message that was received.
     */
    @Override
    public void onMessage(String message) {
        _message = message;

        // Request a firing
        // Figure out what time to request a firing for.
        long elapsedRealTime = System.currentTimeMillis() - _initializeRealTime;

        // Assume model time is in seconds, not milliseconds.
        Time timeOfRequest = _initializeModelTime.add(elapsedRealTime / 1000.0);

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
        } catch (IllegalActionException e) {
            // Can't throw an exception from the onMessage() method
            if (_debugging) {
                _debug("**** Firing denied for time " + timeOfRequest);
            }
        }
    }
    
    /** Do nothing here.  The reader does not send messages and therefore
     * does not need a reference to its endpoint.
     */
    @Override
    public void setEndpoint(WebSocketEndpoint endpoint) {
    }
    
    /** Unsubscribe this service from the endpoint manager.  The endpoint 
     * manager will close connections with no subscribers.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        _endpointManager.unsubscribe(this, _URIpath.toString());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** A manager responsible for generating and releasing websockets. */
    private WebSocketEndpointManager _endpointManager;

    /** The model time at which this actor was last initialized. */
    private Time _initializeModelTime;

    /** The real time at which this actor was last initialized, in milliseconds. 
     */
    private long _initializeRealTime;

    /** The last message received. */
    private String _message;

    /** The URI for the relative path from the "path" parameter.
     *  A URI is used here to make sure the "path" parameter conforms to
     *  all of the URI naming conventions.
     */
    private URI _URIpath;
}
