/* AnExecute a script in JavaScript.

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
package ptolemy.actor.lib.jjs.modules.webSocket;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.ServerWebSocket;

///////////////////////////////////////////////////////////////////
//// WebSocketServerHelper

/**
   A helper class for the WebSocket module's Server object in JavaScript.
   The Vert.x object from its parent can create an instance of Java WebSocket Server.
   Each Java WebSocketServer belongs to one JavaScript WebSocket.Server. 
   
   @author Hokeun Kim
   @version $Id$
   @since Ptolemy II 11.0
   @Pt.ProposedRating Yellow (eal)
   @Pt.AcceptedRating Red (bilung)
 */
public class WebSocketServerHelper extends WebSocketHelperBase {
    
    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /** Close the web socket server.
     */
    public void closeServer() {
        if (_server != null) {
            _server.close();
            _server = null;
        }
    }
    
    /** Create a WebSocketServerHelper instance to help a JavaScript Server instance.
     *  @param currentObj The JavaScript Server instance for which this is a helper.
     *  @param port The port number that the server will use.
     *  @return A new WebSocketServerHelper instance.
     */
    public static WebSocketServerHelper createServer(
	    ScriptObjectMirror currentObj, int port) {
        return new WebSocketServerHelper(currentObj, port);
    }
    
    /** Create and start the server and beginning listening for
     *  connections. If a callback function "listening" is registered,
     *  then that callback will be called when the server begins
     *  listening (which may be even before this returns?).
     *  If a callback function "connection" has been registered,
     *  then that function will be called when a connection is
     *  requested by a remote client.
     */
    public void startServer() {
        // Note that the following call apparently starts the new server
        // in separate thread. It should not be done in the constructor
        // because the script that starts the server needs to register
        // callbacks before the server starts. Otherwise, there will be
        // a race condition where the callback could be called before
        // the server has started.
        _server = _vertx.createHttpServer();
        _server.websocketHandler(new Handler<ServerWebSocket>() {
            @Override
            public void handle(ServerWebSocket serverWebSocket) {
                // Create the socket on this server side.
                Object jsWebSocket = _currentObj.callMember("createServerWebSocket", serverWebSocket);
                // Emit an event indicating that the connection is created.
                _currentObj.callMember("emit", "connection", jsWebSocket);
            }
        });
        _server.listen(_port, "localhost", new Handler<AsyncResult<HttpServer>>() {
            @Override
            public void handle(AsyncResult<HttpServer> arg0) {
        	_currentObj.callMember("emit", "listening");
            }
        });
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     private constructors                   ////

    /** Private constructor for WebSocketServerHelper to create a web socket server.
     *  @param currentObj The JavaScript Server instance for which this a helper.
     *  @param port The port on which to create the server.
     */
    private WebSocketServerHelper(
	    ScriptObjectMirror currentObj, int port) {
        _currentObj = currentObj;
        _port = port;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private fields                        ////
        
    /** The current instance of the JavaScript module. */
    private ScriptObjectMirror _currentObj;
    
    /** The port on which the server listens. */
    private int _port;
    
    /** The internal http server created by Vert.x */
    private HttpServer _server = null;

}
