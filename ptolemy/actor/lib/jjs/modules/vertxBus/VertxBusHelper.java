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
package ptolemy.actor.lib.jjs.modules.vertxBus;

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
import org.vertx.java.core.json.JsonObject;

import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// VertxBusHelper

/**
   A helper class for the Vert.x event bus API.
   
   @author Patricia Derler
   @version $Id$
   @since Ptolemy II 11.0
   @Pt.ProposedRating Yellow (eal)
   @Pt.AcceptedRating Red (bilung)
 */
public class VertxBusHelper {
    
    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////
    /**
     * Create a VertxBusHelper instance as a client-side web socket for
     * each JavaScript instance.
     * 
     * @param engine The JavaScript engine of the JavaScript actor.
     * @param namespaceName The name of the JavaScript module namespace.
     * @param currentObj The JavaScript instance of the WebSocket.
     * @param host The host of the Vert.x bus.
     * @param port The port on the host that provides access to the Vert.x bus.
     * @return A new VertxBusHelper.
     */
    public static VertxBusHelper getEventBus(ScriptEngine engine,
            String namespaceName, Object currentObj, String host, int port) {
        return new VertxBusHelper(engine, namespaceName, currentObj, host, port);
    }

    /**
     * Close the internal web socket, cancel periodic ping.
     */
    public void close() {
        _vertx.cancelTimer(_periodicPing);
        if (_webSocket != null) {
            _webSocket.close();
        }
    }
    
    
    /** Return whether the web socket is opened successfully. 
     * @return True if the socket was opened successfully.
     */
    public boolean isOpen() {
    	if (_webSocket == null) {
    	    return false;
    	}
    	return _wsIsOpen;
    }
    
    /** Publish text data onto vertx bus. 
     * @param address The address.
     * @param message A text message to be sent.
     */
    public void publish(String address, String message) {
        JsonObject json = new JsonObject().putString("type", "publish")
                .putString("address", address)
                .putString("body", message);
        try {
            System.out.println("S " + json);
            _sendTextFrame(json);
        } catch (IllegalActionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /** Register subscriber handler at vertx bus.
     * @param address The address on the bus that should be suscribed to.
     */
    public void registerHandler(String address) {
        JsonObject message = new JsonObject().putString("type",
                "register").putString("address", address);
        _webSocket.writeTextFrame(message.encode());
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private methods                        ////
    /**
     * Private constructor for VertxBusHelper to open a 
     * client-side web socket and add a ping to keep the websocket open.
     * 
     * @param engine The JavaScript engine of the JavaScript actor.
     * @param namespaceName The name of the JavaScript module constructor.
     * @param currentObj The JavaScript instance of the WebSocket.
     * @param host The host of the Vert.x bus.
     * @param port The port on the host that provides access to the Vert.x bus.
     */
    private VertxBusHelper(ScriptEngine engine, String namespaceName,
            Object currentObj, String host, int port) {
        _engine = engine;
        _namespaceName = namespaceName;
        _currentObj = currentObj;

        HttpClient client = _vertx.createHttpClient().setHost(host).setPort(port);
        
        client.connectWebsocket("/eventbus/websocket", new Handler<WebSocket>() {
            @Override
            public void handle(WebSocket websocket) {
                _wsIsOpen = true;
                _webSocket = websocket;
                try {
                    Object obj = _engine.eval(_namespaceName);
                    
                    Object[] args = new Object[2];
                    args[0] = _currentObj;
                    args[1] = "open";
                    ((Invocable) _engine).invokeMethod(obj, "invokeCallback", args);
                }
                catch (NoSuchMethodException | ScriptException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
                _webSocket.dataHandler(new DataHandler());
                _webSocket.endHandler(new EndHandler());
                _webSocket.exceptionHandler(new ExceptionHandler());
            }
        });
        
        _periodicPing = _vertx.setPeriodic(5000, new Handler<Long>() {
            @Override
            public void handle(Long timerID) {
                JsonObject json = new JsonObject().putString("type", "ping");
                try {
                    _sendTextFrame(json);
                } catch (IllegalActionException e) {
                    e.printStackTrace();
                    //_exception = e;
                }
            }
          });
    }
    
    private void _sendTextFrame(JsonObject message) throws IllegalActionException {
        if (_webSocket != null) {
            _webSocket.writeTextFrame(message.encode());
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     private fields                        ////

    /** The name of the constructor of the JavaScript module. */
    private String _namespaceName;
    
    /** The current instance of the JavaScript module. */
    private Object _currentObj;
    
    /** Instance of the current JavaScript engine. */
    private static ScriptEngine _engine;

    private long _periodicPing;
    
    /** Instance of Vertx. Apparently we need only one. */
    private static Vertx _vertx = VertxFactory.newVertx();

    /** The internal web socket created by Vert.x */
    private WebSocket _webSocket = null;

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
            String message = buff.toString();
            JsonObject received = new JsonObject(message);
            try {
                Object obj = _engine.eval(_namespaceName);

                Object[] args = new Object[3];
                args[0] = _currentObj;
                args[1] = "received";
                Object[] jsArgs = new Object[2];
                System.out.println("R " + received);
                jsArgs[0] = received.getField("body").toString();
                jsArgs[1] = false;
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
