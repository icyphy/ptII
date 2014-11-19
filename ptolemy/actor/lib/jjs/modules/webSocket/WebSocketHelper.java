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

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.VertxFactory;
import org.vertx.java.core.VoidHandler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.WebSocket;

import ptolemy.actor.TypedAtomicActor;

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
public class WebSocketHelper extends TypedAtomicActor {
    
    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////
    /**
     * Create a WebSocketHelper instance for each JavaScript instance.
     * 
     * @param engine The JavaScript engine of the JavaScript actor.
     * @param constructorName The name of the JavaScript module constructor.
     * @param address address The URL of the WebSocket host and the port number. 
     * (e.g. 'ws://localhost:8000')
     * @param currentObj The JavaScript instance of the WebSocket.
     * @return
     */
    public static WebSocketHelper create(ScriptEngine engine,
            String constructorName, String address, Object currentObj) {
	return new WebSocketHelper(engine, constructorName, address, currentObj);
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
     * Private constructor for WebSocketHelper, called by the create() method.
     * Open an internal web socket using Vert.x.
     * 
     * @param engine The JavaScript engine of the JavaScript actor.
     * @param constructorName The name of the JavaScript module constructor.
     * @param address The URL of the WebSocket host and the port number. 
     * (e.g. 'ws://localhost:8000')
     * @param currentObj The JavaScript instance of the WebSocket.
     */
    private WebSocketHelper(ScriptEngine engine, String constructorName,
            String address, Object currentObj) {
        _engine = engine;
        _constructorName = constructorName;
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
                    Object obj = _engine.eval(_constructorName);
                    
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
                
                _webSocket.dataHandler(new Handler<Buffer>() {
                    @Override
                    public void handle(Buffer buff) {
                        byte[] bytes = buff.getBytes();
                        Integer[] objBytes = new Integer[bytes.length];
                        for (int i = 0; i < bytes.length; i++) {
                            objBytes[i] = (int)bytes[i];
                        }

                        try {
                            Object obj = _engine.eval(_constructorName);
    
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
                });
                
                _webSocket.endHandler(new VoidHandler() {
                    @Override
                    protected void handle() {
                        try {
                            Object obj = _engine.eval(_constructorName);
                            
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
                });
                
                _webSocket.exceptionHandler(new Handler<Throwable>() {
                    @Override
                    public void handle(Throwable arg0) {
                        try {
                            Object obj = _engine.eval(_constructorName);
                            
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
                });
            }
        });
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private fields                        ////

    /** Instance of Vertx. Apparently we need only one. */
    private static Vertx _vertx = VertxFactory.newVertx();
      
    /** Instance of the current JavaScript engine. */
    private static ScriptEngine _engine;
    
    /** The name of the constructor of the JavaScript module. */
    private String _constructorName;
    
    /** The current instance of the JavaScript module. */
    private Object _currentObj;

    /** The internal web socket created by Vert.x */
    private WebSocket _webSocket = null;

    /** Whether the internal web socket is opened successfully. */
    private boolean _wsIsOpen = false;
}
