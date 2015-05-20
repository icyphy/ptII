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
   A helper class for the webSocket module's Server object in JavaScript.
   See the documentation of that module for instructions.
   This uses Vert.x for the implementation.
   
   @author Hokeun Kim and Edward A. Lee
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
    public synchronized void closeServer() {
        if (_server != null) {
            _server.close();
            _server = null;
        }
    }
    
    /** Create a WebSocketServerHelper instance to help a JavaScript Server instance.
     *  @param currentObj The JavaScript Server instance for which this is a helper.
     *  @param hostInterface The host interface to use, in case there the host has more
     *   than one interface (e.g. Ethernet and WiFi). This is IP address or name, and if
     *   the argument is null, then "localhost" will be used.
     *  @param port The port number that the server will use.
     *  @return A new WebSocketServerHelper instance.
     */
    public static WebSocketServerHelper createServer(
	    ScriptObjectMirror currentObj, String hostInterface, int port) {
        return new WebSocketServerHelper(currentObj, hostInterface, port);
    }
    
    /** Create and start the server and beginning listening for
     *  connections. When a new connection request is received and
     *  a socket has been opened, emit the 'connection' event with the
     *  socket as an argument.
     */
    public synchronized void startServer() {
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
        	synchronized(WebSocketServerHelper.this) {
        	    // FIXME: Create error handler, close handler, etc. on this socket.

        	    // Notify of a new connection.
        	    // This will have the side effect of creating a new JS Socket
        	    // object, which is an event emitter.
        	    _currentObj.callMember("socketCreated", serverWebSocket);
        	}
            }
        });
        _server.listen(_port, _hostInterface, new Handler<AsyncResult<HttpServer>>() {
            @Override
            public void handle(AsyncResult<HttpServer> arg0) {
        	synchronized(WebSocketServerHelper.this) {
        	    _currentObj.callMember("emit", "listening");
        	}
            }
        });
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     private constructors                   ////

    /** Private constructor for WebSocketServerHelper to create a web socket server.
     *  @param currentObj The JavaScript Server instance for which this a helper.
     *  @param hostInterface The host interface to use, in case there the host has more
     *   than one interface (e.g. Ethernet and WiFi). This is IP address or name, and if
     *   the argument is null, then "localhost" will be used.
     *  @param port The port on which to create the server.
     */
    private WebSocketServerHelper(
	    ScriptObjectMirror currentObj, String hostInterface, int port) {
        _currentObj = currentObj;
        _hostInterface = hostInterface;
        if (hostInterface == null) {
            _hostInterface = "localhost";
        }
        _port = port;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private fields                        ////
        
    /** The current instance of the JavaScript module. */
    private ScriptObjectMirror _currentObj;
    
    /** The host interface. */
    private String _hostInterface;
    
    /** The port on which the server listens. */
    private int _port;
    
    /** The internal http server created by Vert.x */
    private HttpServer _server = null;

}
