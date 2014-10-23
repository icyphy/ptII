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

import org.eclipse.jetty.websocket.WebSocket.Connection;

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

        input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(BaseType.STRING);

        _connectionManager = WebSocketConnectionManager.getInstance();
        _connectionMonitor = new Object();

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
                    if (pathValue.startsWith("http")
                            || pathValue.startsWith("ftp")) {
                        throw new IllegalActionException(this,
                                "Remote websocket"
                                        + " paths must start with ws://");
                    }

                    if (pathValue.startsWith("ws://")
                            || pathValue.startsWith("wss://")) {
                        _URIpath = URI.create(pathValue);
                        _isLocal = false;
                    } else if (!pathValue.trim().startsWith("/")) {
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

        newObject._connection = null;
        newObject._connectionManager = WebSocketConnectionManager.getInstance();
        newObject._connectionMonitor = new Object();
        newObject._connectionTimeout = 5000;
        newObject._isLocal = false;
        newObject._isShared = true;
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

        // Read token on input port, if any.  Write token contents to socket.
        // Empty string tokens are acceptable

        if (input.hasToken(0)) {

            // If connection is not ready, wait on it, up to a max time limit
            // setConnection() calls notifyAll() on _connectionMonitor
            if (_connection == null) {
                synchronized (_connectionMonitor) {
                    try {
                        _connectionMonitor.wait(_connectionTimeout);
                    } catch (InterruptedException e) {
                        throw new IllegalActionException(this,
                                "Cannot establish "
                                        + "a websocket connection to write to.");
                    }
                }
            }

            if (_connection == null) {
                throw new IllegalActionException(this, "Timed out waiting for "
                        + "websocket connection.");
            }

            String message = ((StringToken) input.get(0)).stringValue();
            if (_connection != null) {
                try {
                    _connection.sendMessage(message);
                } catch (IOException e) {
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

    /** Returns true if connecting to a locally hosted service; false otherwise.
     * @return True if connecting to a locally hosted service; false otherwise.
     */
    public boolean isLocal() {
        return _isLocal;
    }

    /** Do nothing upon receipt of a message, since this actor is a writer.
     *
     * @param sender The WebSocketEndpoint that sent the message.
     * @param message The message that was received from the websocket.
     */
    public void onMessage(WebSocketEndpoint sender, String message) {
    }

    /** Open the WebSocket connection on the given port.
     * @param path The URI to connect to.
     * @exception IllegalActionException If the websocket cannot be opened.
     */
    public void open(URI path) throws IllegalActionException {

        if (_isLocal || !_isShared) {
            _connectionManager.newConnection(path, this);
        } else {
            _connectionManager.requestConnection(path, this);
        }

        if (_debugging) {
            _debug("Websocket connected for " + getName());
        }
    }

    /** Set the connection that this WebSocketService will use.
     * @param connection The connection that this WebSocketService will use.
     */
    @Override
    public void setConnection(Connection connection) {
        _connection = connection;

        // fire() might be waiting for a connection to be established
        _connectionMonitor.notifyAll();
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

        _connectionManager.releaseConnection(_URIpath, this);
        _connection = null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The WebSocket connection. */
    private Connection _connection;

    /** A manager responsible for generating and releasing connections. */
    private WebSocketConnectionManager _connectionManager;

    /** An object to wait on if the connection is not yet ready in fire() */
    private Object _connectionMonitor;

    /** The timeout for establishing a connection.  In milliseconds. */
    private int _connectionTimeout = 5000;

    /** True if the WebSocket is hosted locally be the Ptolemy model; false
     *  otherwise.
     */
    private boolean _isLocal;

    /** True if the actor wishes to share a socket connection.  Used for
     * remote services for e.g. writing to a socket, then receiving a response.
     * Always true for WebSocketReader.
     */
    private boolean _isShared = true;

    /** The URI for the relative path from the "path" parameter.
     *  A URI is used here to make sure the "path" parameter conforms to
     *  all of the URI naming conventions.
     */
    private URI _URIpath;
}
