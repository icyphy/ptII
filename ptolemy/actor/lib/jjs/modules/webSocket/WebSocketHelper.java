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

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

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
    /**
     * Create a WebSocketHelper instance as a client-side web socket for
     * each JavaScript instance.
     * 
     * @param engine The JavaScript engine of the JavaScript actor.
     * @param namespaceName The name of the JavaScript module namespace.
     * @param currentObj The JavaScript instance of the WebSocket.
     * @param address address The URL of the WebSocket host and the port number. 
     * (e.g. 'ws://localhost:8000')
     * @return
     */
    public static WebSocketHelper createClientSocket(ScriptEngine engine,
            String namespaceName, Object currentObj, String address) {
	return new WebSocketHelper(engine, namespaceName, currentObj, address);
    }
    
    /**
     * Create a WebSocketHelper instance as a web socket server.
     * 
     * @param engine The JavaScript engine of the JavaScript actor.
     * @param namespaceName The name of the JavaScript module namespace.
     * @param currentObj The JavaScript instance of the WebSocketServer.
     * @param port The port number which the server will listen to.
     * @return
     */
    public static WebSocketHelper createServer(ScriptEngine engine,
            String namespaceName, Object currentObj, int port) {
        return new WebSocketHelper(engine, namespaceName, currentObj, port);
    }

    /**
     * Create a WebSocketHelper instance as a server-side web socket, with
     * the given server-side Java socket.
     * 
     * @param engine The JavaScript engine of the JavaScript actor.
     * @param namespaceName The name of the JavaScript module namespace.
     * @param currentObj The JavaScript instance of the WebSocket.
     * @param serverWebSocket The given server-side Java socket.
     * @return
     */
    public static WebSocketHelper createServerSocket(ScriptEngine engine,
            String namespaceName, Object currentObj, WebSocketBase serverWebSocket) {
        return new WebSocketHelper(engine, namespaceName, currentObj, serverWebSocket);
    }

    /**
     * Close the internal web socket.
     */
    public void close() {
        if (_webSocket != null) {
            _webSocket.close();
        }
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

    /**
     * Return whether the web socket is opened successfully.
     * 
     * @return
     */
    public boolean isOpen() {
	if (_webSocket == null) {
	    return false;
	}
	return _wsIsOpen;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private methods                        ////
    /**
     * Private constructor for WebSocketHelper to open a client-side web socket.
     * Open an internal web socket using Vert.x.
     * 
     * @param engine The JavaScript engine of the JavaScript actor.
     * @param namespaceName The name of the JavaScript module constructor.
     * @param currentObj The JavaScript instance of the WebSocket.
     * @param address The URL of the WebSocket host and the port number. 
     * (e.g. 'ws://localhost:8000')
     */
    private WebSocketHelper(ScriptEngine engine, String namespaceName,
            Object currentObj, String address) {
        _engine = engine;
        _namespaceName = namespaceName;
        _currentObj = currentObj;

        HttpClient client = _vertx.createHttpClient();
        if (address.length() > 0 && address.charAt(address.length() - 1) == '/') {
            address = address.substring(0, address.length() - 1);
        }
        int sep = address.lastIndexOf(':');
        client.setPort(Integer.parseInt(address.substring(sep + 1))); 

        client.connectWebsocket(address, new Handler<WebSocket>() {
            @Override
            public void handle(WebSocket websocket) {
                _wsIsOpen = true;
                try {
                    Object obj = _engine.eval(_namespaceName);
                    
                    Object[] args = new Object[2];
                    args[0] = _currentObj;
                    
                    args[1] = "connect";
                    ((Invocable) _engine).invokeMethod(obj, "invokeCallback", args);
                    
                    args[1] = "open";
                    ((Invocable) _engine).invokeMethod(obj, "invokeCallback", args);
                }
                catch (NoSuchMethodException | ScriptException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                _webSocket = websocket;
                
                _webSocket.dataHandler(new DataHandler());
                _webSocket.endHandler(new EndHandler());
                _webSocket.exceptionHandler(new ExceptionHandler());
            }
        });
    }

    /**
     * Private constructor for WebSocketHelper to create a web socket server.
     */
    private WebSocketHelper(ScriptEngine engine, String namespaceName,
            Object currentObj, int port) {
        _engine = engine;
        _namespaceName = namespaceName;
        _currentObj = currentObj;
        
        HttpServer server = _vertx.createHttpServer();
        server.websocketHandler(new Handler<ServerWebSocket>() {
            @Override
            public void handle(ServerWebSocket serverWebSocket) {
                try {
                    Object obj = _engine.eval(_namespaceName);
                    
                    Object[] args = new Object[1];
                    args[0] = serverWebSocket;
                    Object jsWebSocket = ((Invocable) _engine).invokeMethod(obj, "createServerWebSocket", args);

                    args = new Object[3];
                    args[0] = _currentObj;
                    args[1] = "connection";
                    
                    Object[] jsArgs = new Object[1];
                    jsArgs[0] = jsWebSocket;
                    args[2] = jsArgs;
                    ((Invocable) _engine).invokeMethod(obj, "invokeCallback", args);
                }
                catch (NoSuchMethodException | ScriptException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            
        });
        server.listen(port, "localhost", new Handler<AsyncResult<HttpServer>>() {
            @Override
            public void handle(AsyncResult<HttpServer> arg0) {
                try {
                    Object obj = _engine.eval(_namespaceName);
    
                    Object[] args = new Object[2];
                    args[0] = _currentObj;
                    
                    args[1] = "listening";
                    ((Invocable) _engine).invokeMethod(obj, "invokeCallback", args);
                }
                catch (NoSuchMethodException | ScriptException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            
        });
    }
    
    /**
     * Private constructor for WebSocketHelper with a server-side web socket.
     * The server-side web socket is given from the web socket server.
     */
    private WebSocketHelper(ScriptEngine engine, String namespaceName,
            Object currentObj, WebSocketBase serverWebSocket) {
        _engine = engine;
        _namespaceName = namespaceName;
        _currentObj = currentObj;
        _webSocket = serverWebSocket;

        _webSocket.dataHandler(new DataHandler());
        _webSocket.endHandler(new EndHandler());
        _webSocket.exceptionHandler(new ExceptionHandler());
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private fields                        ////

    /** Instance of Vertx. Apparently we need only one. */
    private static Vertx _vertx = VertxFactory.newVertx();
      
    /** Instance of the current JavaScript engine. */
    private static ScriptEngine _engine;
    
    /** The name of the constructor of the JavaScript module. */
    private String _namespaceName;
    
    /** The current instance of the JavaScript module. */
    private Object _currentObj;

    /** The internal web socket created by Vert.x */
    private WebSocketBase _webSocket = null;

    /** Whether the internal web socket is opened successfully. */
    private boolean _wsIsOpen = false;

    ///////////////////////////////////////////////////////////////////
    ////                     private classes                        ////

    /**
     * The event handler that is triggered when a message arrives on the web socket.
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
                Object obj = _engine.eval(_namespaceName);

                Object[] args = new Object[3];
                args[0] = _currentObj;
                args[1] = "message";
                Object[] jsArgs = new Object[2];
                jsArgs[0] = objBytes;
                jsArgs[1] = _engine.eval("new function() { this.binary = true; }");
                args[2] = jsArgs;
                
                ((Invocable) _engine).invokeMethod(obj, "invokeCallback", args);
            }
            catch (NoSuchMethodException | ScriptException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * The event handler that is triggered when the web socket connection is closed.
     */
    private class EndHandler extends VoidHandler {
        @Override
        protected void handle() {
            try {
                Object obj = _engine.eval(_namespaceName);
                
                Object[] args = new Object[2];
                args[0] = _currentObj;
                args[1] = "close";
                
                ((Invocable) _engine).invokeMethod(obj, "invokeCallback", args);
            }
            catch (NoSuchMethodException | ScriptException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * The event handler that is triggered when an error occurs in the web socket connection.
     */
    private class ExceptionHandler implements Handler<Throwable> {
        @Override
        public void handle(Throwable arg0) {
            try {
                Object obj = _engine.eval(_namespaceName);
                
                Object[] args = new Object[2];
                args[0] = _currentObj;
                args[1] = "error";
                
                ((Invocable) _engine).invokeMethod(obj, "invokeCallback", args);
            }
            catch (NoSuchMethodException | ScriptException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
}
