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

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.VertxFactory;
import org.vertx.java.core.VoidHandler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.ServerWebSocket;
import org.vertx.java.core.http.WebSocket;
import org.vertx.java.core.http.WebSocketBase;

///////////////////////////////////////////////////////////////////
//// WebSocketHelper

/**
   A helper class for the WebSocket module in JavaScript.
   Creates only one Vert.x object and uses it internally.
   The Vert.x object can create an instance of Java WebSocket.
   Each Java WebSocket belongs to one JavaScript WebSocket. 
   
   @author Hokeun Kim and Edward A. Lee
   @version $Id$
   @since Ptolemy II 11.0
   @Pt.ProposedRating Yellow (eal)
   @Pt.AcceptedRating Red (bilung)
 */
public class WebSocketHelper {
    
    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////
    
    /** Close the web socket.
     *  FIXME: Does this need to be invoked on the client or server side or both?
     *  Demos don't use it. Not exposed in JavaScript.
     */
    public void close() {
        if (_webSocket != null) {
            _webSocket.close();
        }
    }

    /** Create a WebSocketHelper instance for the specified JavaScript
     *  Socket instance for the client side of the socket.
     *  @param engine The JavaScript engine of the JavaScript actor.
     *  @param currentObj The JavaScript instance of the Socket.
     *  @param address address The URL of the WebSocket host and the port number. 
     *   (e.g. 'ws://localhost:8000'). If no port number is given, then 80 is used.
     *  @return A new WebSocketHelper instance.
     */
    public static WebSocketHelper createClientSocket(
	    ScriptEngine engine, ScriptObjectMirror currentObj, String address) {
	return new WebSocketHelper(engine, currentObj, address);
    }
    
    /** Create a WebSocketHelper instance to help a JavaScript Server instance.
     *  @param engine The JavaScript engine creating this helper.
     *  @param currentObj The JavaScript Server instance for which this is a helper.
     *  @param port The port number that the server will use.
     *  @return A new WebSocketHelper instance.
     */
    public static WebSocketHelper createServer(
	    ScriptEngine engine, ScriptObjectMirror currentObj, int port) {
        return new WebSocketHelper(engine, currentObj, port);
    }

    /** Create a WebSocketHelper instance for the specified JavaScript
     *  Socket instance for the server side of the socket.
     *  @param engine The JavaScript engine of the JavaScript actor.
     *  @param currentObj The JavaScript instance of the Socket.
     *  @param serverWebSocket The given server-side Java socket.
     *  @return A new WebSocketHelper instance.
     */
    public static WebSocketHelper createServerSocket(
	    ScriptEngine engine, ScriptObjectMirror currentObj, WebSocketBase serverWebSocket) {
        return new WebSocketHelper(engine, currentObj, serverWebSocket);
    }

    /**
     * Send binary data through the internal web socket.
     * 
     * @param msg A binary message to be sent.
     */
    public void sendBinary(byte[] msg) {
        Buffer buffer = new Buffer(msg);
        _webSocket.writeBinaryFrame(buffer);
    }
    
    /**
     * Send text data through the internal web socket.
     * 
     * @param msg A text message to be sent.
     */
    public void sendText(String msg) {
        _webSocket.writeTextFrame(msg);
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
        HttpServer server = _vertx.createHttpServer();
        server.websocketHandler(new Handler<ServerWebSocket>() {
            @Override
            public void handle(ServerWebSocket serverWebSocket) {
                // Create the socket on this server side.
                Object jsWebSocket = _currentObj.callMember("createServerWebSocket", serverWebSocket);
                // Emit an event indicating that the connection is created.
                _currentObj.callMember("emit", "connection", jsWebSocket);
            }
        });
        server.listen(_port, "localhost", new Handler<AsyncResult<HttpServer>>() {
            @Override
            public void handle(AsyncResult<HttpServer> arg0) {
        	_currentObj.callMember("emit", "listening");
            }
        });
    }

    /** Return whether the web socket is opened successfully.
     *  @return True if the socket is open.
     */
    public boolean isOpen() {
	if (_webSocket == null) {
	    return false;
	}
	return _wsIsOpen;
    }
        
    ///////////////////////////////////////////////////////////////////
    ////                     private constructors                   ////

    /** Private constructor for WebSocketHelper to open a client-side web socket.
     *  Open an internal web socket using Vert.x.
     *  @param engine The JavaScript engine of the JavaScript actor.
     *  @param currentObj The JavaScript instance of Socket that this helps.
     *  @param address The URL of the WebSocket host with an optional port number
     *   (e.g. 'ws://localhost:8000'). If no port number is given, 80 is used.
     */
    private WebSocketHelper(
	    ScriptEngine engine, ScriptObjectMirror currentObj, String address) {
        _engine = engine;
        _currentObj = currentObj;

        HttpClient client = _vertx.createHttpClient();
        
        // Parse the address.
        // FIXME: Use utilities for this. Perhaps on the JavaScript side?
        // Strip off any trailing slash from the URI.
        if (address.length() > 0 && address.charAt(address.length() - 1) == '/') {
            address = address.substring(0, address.length() - 1);
        }
        int sep = address.lastIndexOf(':');
        if (sep > 0) {
            try {
        	client.setPort(Integer.parseInt(address.substring(sep + 1)));
            } catch (NumberFormatException e) {
        	throw new RuntimeException("Invalid port in URI: " + address);
            }
        } else {
            client.setPort(80);
        }

        client.connectWebsocket(address, new Handler<WebSocket>() {
            @Override
            public void handle(WebSocket websocket) {
                _wsIsOpen = true;
                // Socket.io uses the name "connect" for this event, but WS uses "open",
                // so we just emit both events.
                _currentObj.callMember("emit", "connect");
                _currentObj.callMember("emit", "open");

                _webSocket = websocket;
                
                _webSocket.dataHandler(new DataHandler());
                _webSocket.endHandler(new EndHandler());
                _webSocket.exceptionHandler(new ExceptionHandler());
            }
        });
    }

    /** Private constructor for WebSocketHelper to create a web socket server.
     *  @param engine The JavaScript engine creating this server.
     *  @param currentObj The JavaScript Server instance for which this a helper.
     *  @param port The port on which to create the server.
     */
    private WebSocketHelper(
	    ScriptEngine engine, ScriptObjectMirror currentObj, int port) {
        _engine = engine;
        _currentObj = currentObj;
        _port = port;
    }

    /** Private constructor for WebSocketHelper for a server-side web socket.
     *  @param engine The JavaScript engine creating this server.
     *  @param currentObj The JavaScript instance of Socket that this helps.
     *  @param serverWebSocket The server-side web socket, provided by the web socket server.
     */
    private WebSocketHelper(
	    ScriptEngine engine, ScriptObjectMirror currentObj, WebSocketBase serverWebSocket) {
        _engine = engine;
        _currentObj = currentObj;
        _webSocket = serverWebSocket;
        // The serverSocket was already opened because a client successfully connected to the server.
        _wsIsOpen = true;

        _webSocket.dataHandler(new DataHandler());
        _webSocket.endHandler(new EndHandler());
        _webSocket.exceptionHandler(new ExceptionHandler());
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private fields                        ////
        
    /** The current instance of the JavaScript module. */
    private ScriptObjectMirror _currentObj;

    /** Instance of the current JavaScript engine. */
    private static ScriptEngine _engine;
    
    /** The port on which the server listens. */
    private int _port;
    
    /** Instance of Vertx. Apparently we need only one. */
    private static Vertx _vertx = VertxFactory.newVertx();

    /** The internal web socket created by Vert.x */
    private WebSocketBase _webSocket = null;

    /** Whether the internal web socket is opened successfully. */
    private boolean _wsIsOpen = false;

    ///////////////////////////////////////////////////////////////////
    ////                     private classes                        ////

    /** The event handler that is triggered when a message arrives on the web socket.
     */
    private class DataHandler implements Handler<Buffer> {
        @Override
        public void handle(Buffer buff) {
            byte[] bytes = buff.getBytes();
            Integer[] objBytes = new Integer[bytes.length];
            for (int i = 0; i < bytes.length; i++) {
                objBytes[i] = (int)bytes[i];
            }

            try {
                // Properties of the data.
        	// FIXME: What are these properties? Pass string directly?
                Object jsArgs = _engine.eval(" var properties = {binary: true}; properties");
                _currentObj.callMember("emit", "message", objBytes, jsArgs);
            } catch (ScriptException e) {
                // FIXME Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /** The event handler that is triggered when the web socket connection is closed.
     */
    private class EndHandler extends VoidHandler {
        @Override
        protected void handle() {
            _currentObj.callMember("emit", "close");
            _wsIsOpen = false;
        }
    }

    /** The event handler that is triggered when an error occurs in the web socket connection.
     */
    private class ExceptionHandler implements Handler<Throwable> {
        @Override
        public void handle(Throwable arg0) {
            _currentObj.callMember("emit", "error");
            _wsIsOpen = false;
        }
    }
}
