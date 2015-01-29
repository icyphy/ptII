/* A Javascript helper for Vert.x.

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
package ptolemy.actor.lib.jjs.modules.vertx;

import javax.script.ScriptEngine;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.VertxFactory;
import org.vertx.java.core.VoidHandler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.WebSocket;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.sockjs.SockJSServer;

import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// VertxHelper

/** A helper class for the Vert.x event bus API.
   
   @author Patricia Derler
   @version $Id: VertxHelper.java 71355 2015-01-13 05:40:56Z cxh $
   @since Ptolemy II 11.0
   @Pt.ProposedRating Yellow (pd)
   @Pt.AcceptedRating Red (pd)
 */
public class VertxHelper {
    
    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////
    /** Create a VertxHelper instance as a client-side web socket for
     * each JavaScript instance.
     * 
     * @param engine The JavaScript engine of the JavaScript actor.
     * @param currentObj The JavaScript instance of the WebSocket.
     * @param host The host of the Vert.x bus.
     * @param port The port on the host that provides access to the Vert.x bus.
     * @return A new VertxHelper.
     */
    public static VertxHelper getEventBus(ScriptEngine engine,
            ScriptObjectMirror currentObj, String host, int port) {
        return new VertxHelper(engine, currentObj, host, port, true);
    }
    
    /** Create a VertxHelper instance as a server-side vertx bus host.
     * 
     * @param engine The JavaScript engine of the JavaScript actor.
     * @param currentObj The JavaScript instance of the WebSocket.
     * @param port The port on the host that provides access to the Vert.x bus.
     * @return A new VertxHelper.
     */
    public static VertxHelper getEventBusServer(ScriptEngine engine, 
            ScriptObjectMirror currentObj, int port) {
        return new VertxHelper(engine, currentObj, port, true);
    }
    
    /**
     * Create a VertxHelper instance as an http client.
     * 
     * @param engine The JavaScript engine of the JavaScript actor.
     * @param currentObj The JavaScript instance of the WebSocket.
     * @param host The host of the Vert.x bus.
     * @param port The port on the host that provides access to the Vert.x bus.
     * @return A new VertxHelper.
     */
    public static VertxHelper getHttpClient(ScriptEngine engine,
            ScriptObjectMirror currentObj, String host, int port) {
        return new VertxHelper(engine, currentObj, host, port, false);
    }

    /**
     * Create a VertxHelper instance as an http server.
     * 
     * @param engine The JavaScript engine of the JavaScript actor.
     * @param currentObj The JavaScript instance of the WebSocket.
     * @param port The port on the host that provides access to the Vert.x bus.
     * @return A new VertxHelper.
     */
    public static VertxHelper getHttpServer(ScriptEngine engine, 
            ScriptObjectMirror currentObj, int port) {
        return new VertxHelper(engine, currentObj, port, false);
    }
    
    /** Close the internal web socket, cancel periodic ping.
     */
    public void close() {
        _vertx.cancelTimer(_periodicPing);
        if (_webSocket != null) {
            _webSocket.close();
        }
    }
    
    /** Close server.
     */
    public void closeServer() {
        _httpServer.close();
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
            _sendTextFrame(json);
        } catch (IllegalActionException e) {
            _currentObj.callMember("emit", "error");
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

    /** Send http response string.
     * @param request The request that receives the response.
     * @param response The response as a string.
     */
    public void sendHttpResponse(HttpServerRequest request, String response) {
        request.response().putHeader("content-type", "text/plain");
        request.response().putHeader("content-length", "" + response.length());
        request.response().write(response);
        request.response().end();
    }
    
    
    ///////////////////////////////////////////////////////////////////
    ////                     private methods                        ////
    /**
     * Private constructor for VertxHelper to open a 
     * client-side web socket and add a ping to keep the websocket open.
     * 
     * @param engine The JavaScript engine of the JavaScript actor.
     * @param namespaceName The name of the JavaScript module constructor.
     * @param currentObj The JavaScript instance of the WebSocket.
     * @param host The host of the Vert.x bus.
     * @param port The port on the host that provides access to the Vert.x bus.
     */
    private VertxHelper(ScriptEngine engine,
            ScriptObjectMirror currentObj, String host, int port, boolean withEventBus) {
        _engine = engine;
        _currentObj = currentObj;

        _httpClient = _vertx.createHttpClient().setHost(host).setPort(port);
        
        if (withEventBus) {
            _httpClient.connectWebsocket("/eventbus/websocket", new Handler<WebSocket>() {
                @Override
                public void handle(WebSocket websocket) {
                    _wsIsOpen = true;
                    _webSocket = websocket;
                    _currentObj.callMember("emit", "open");
                    
                    _webSocket.dataHandler(new DataHandler());
                    _webSocket.endHandler(new EndHandler());
                    _webSocket.exceptionHandler(new ExceptionHandler());
                }
            });
            
            // the periodic ping is needed to keep the websocket open.
            _periodicPing = _vertx.setPeriodic(5000, new Handler<Long>() {
                @Override
                public void handle(Long timerID) {
                    JsonObject json = new JsonObject().putString("type", "ping");
                    try {
                        _sendTextFrame(json);
                    } catch (IllegalActionException e) {
                        _currentObj.callMember("emit", "error");
                    }
                }
              });
        }
    }
    
    /**
     * Private constructor for VertxHelper to open a 
     * client-side web socket and add a ping to keep the websocket open.
     * 
     * @param engine The JavaScript engine of the JavaScript actor.
     * @param namespaceName The name of the JavaScript module constructor.
     * @param currentObj The JavaScript instance of the WebSocket.
     * @param host The host of the Vert.x bus.
     * @param port The port on the host that provides access to the Vert.x bus.
     */
    private VertxHelper(ScriptEngine engine,
            ScriptObjectMirror currentObj, int port, boolean withEventBus) {
        _engine = engine;
        _currentObj = currentObj;

        _httpServer = _vertx.createHttpServer();
        
        _httpServer.requestHandler(new Handler<HttpServerRequest>() {
            public void handle(HttpServerRequest request) {
                _currentObj.callMember("emit", "httpRequest", request);
            }
        });

        if (withEventBus) {
            /* Set security permission to let everything go through */
            JsonArray permitted = new JsonArray();
            permitted.add(new JsonObject());
    
            /* Create SockJS and bridge it to the Event Bus */
            SockJSServer sockJSServer = _vertx.createSockJSServer(_httpServer);
            sockJSServer.bridge(new JsonObject().putString("prefix", "/eventbus"),
                    permitted, permitted);
        }
        /* Start the server */
        _httpServer.listen(port);
    }
    
    private void _sendTextFrame(JsonObject message) throws IllegalActionException {
        if (_webSocket != null) {
            _webSocket.writeTextFrame(message.encode());
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     private fields                        ////
    
    /** The current instance of the JavaScript module. */
    private ScriptObjectMirror _currentObj;
    
    /** Instance of the current JavaScript engine. */
    private static ScriptEngine _engine;

    private HttpClient _httpClient = null;

    private HttpServer _httpServer = null;

    /** Periodic ping to keep websocket alive when using the eventbus. */
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
            _currentObj.callMember("emit", "received", received.getField("body").toString());
        }
    }

    /**
     * The event handler that is triggered when the web socket connection is closed.
     */
    private class EndHandler extends VoidHandler {
        @Override
        protected void handle() {
            _currentObj.callMember("emit", "close");
        }
    }

    /**
     * The event handler that is triggered when an error occurs in the web socket connection.
     */
    private class ExceptionHandler implements Handler<Throwable> {
        @Override
        public void handle(Throwable arg0) {
            _currentObj.callMember("emit", "error");
        }
    }
    
}
