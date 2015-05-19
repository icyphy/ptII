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

import java.util.LinkedList;
import java.util.List;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

import org.vertx.java.core.Handler;
import org.vertx.java.core.VoidHandler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.WebSocket;
import org.vertx.java.core.http.WebSocketBase;

///////////////////////////////////////////////////////////////////
//// WebSocketHelper

/**
   A helper class for the WebSocket module in JavaScript.
   The Vert.x object from its parent can create an instance of Java WebSocket.
   Each Java WebSocket belongs to one JavaScript WebSocket. 
   
   @author Hokeun Kim and Edward A. Lee
   @version $Id$
   @since Ptolemy II 11.0
   @Pt.ProposedRating Yellow (eal)
   @Pt.AcceptedRating Red (bilung)
 */
public class WebSocketHelper extends WebSocketHelperBase {
    
    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////
    
    /** Close the web socket.
     */
    public synchronized void close() {
        if (_webSocket != null) {
            if (_wsIsOpen) {
                _webSocket.close();
            }
            _webSocket = null;
        }
        if (_pendingOutputs != null && _pendingOutputs.size() > 0) {
	    _currentObj.callMember("emit", "error", "Unsent messages remain that were queued before the socket opened.");
        }
    }

    /** Create a WebSocketHelper instance for the specified JavaScript
     *  Socket instance for the client side of the socket.
     *  @param currentObj The JavaScript instance of the Socket.
     *  @param host IP address or host name of the host.
     *  @param port The port number that the host listens on.
     *  @return A new WebSocketHelper instance.
     */
    public static WebSocketHelper createClientSocket(
	    ScriptObjectMirror currentObj, String host, int port) {
	return new WebSocketHelper(currentObj, host, port);
    }

    /** Create a WebSocketHelper instance for the specified JavaScript
     *  Socket instance for the server side of the socket.
     *  @param currentObj The JavaScript instance of the Socket.
     *  @param serverWebSocket The given server-side Java socket.
     *  @return A new WebSocketHelper instance.
     */
    public static WebSocketHelper createServerSocket(
	    ScriptObjectMirror currentObj, WebSocketBase serverWebSocket) {
        return new WebSocketHelper(currentObj, serverWebSocket);
    }
    
    /** Send text data through the web socket.
     *  @param msg A text message to be sent.
     */
    public synchronized void sendText(String msg) {
        if (isOpen()) {
            _webSocket.writeTextFrame(msg);
        } else {
            if (_pendingOutputs == null) {
        	_pendingOutputs = new LinkedList();
            }
            _pendingOutputs.add(msg);
        }
    }

    /** Return whether the web socket is opened successfully.
     *  @return True if the socket is open.
     */
    public synchronized boolean isOpen() {
	if (_webSocket == null) {
	    return false;
	}
	return _wsIsOpen;
    }
        
    ///////////////////////////////////////////////////////////////////
    ////                     private constructors                   ////

    /** Private constructor for WebSocketHelper to open a client-side web socket.
     *  Open an internal web socket using Vert.x.
     *  @param currentObj The JavaScript instance of Socket that this helps.
     *  @param host The IP address or host name of the host.
     *  @param port The port number of the host.
     */
    private WebSocketHelper(
	    ScriptObjectMirror currentObj, String host, int port) {
        _currentObj = currentObj;

        HttpClient client = _vertx.createHttpClient();
        client.setHost(host);
        client.setPort(port);
        client.exceptionHandler(new HttpClientExceptionHandler());
        // FIXME: Provide a timeout. Use setTimeout() of the client.
        // FIXME: Why does Vertx require the URI here in addition to setHost() and setPort() above? Seems lame.
        String address = "ws://" + host + ":" + port;
        client.connectWebsocket(address, new Handler<WebSocket>() {
            @Override
            public void handle(WebSocket websocket) {
        	synchronized(WebSocketHelper.this) {
        	    _wsIsOpen = true;
        	    _webSocket = websocket;

        	    _webSocket.dataHandler(new DataHandler());
        	    _webSocket.endHandler(new EndHandler());
        	    _webSocket.exceptionHandler(new WebSocketExceptionHandler());

        	    // Socket.io uses the name "connect" for this event, but WS uses "open",
        	    // so we just emit both events.
        	    _currentObj.callMember("emit", "connect");
        	    _currentObj.callMember("emit", "open");
        	    
        	    // Send any pending messages.
        	    if (_pendingOutputs != null && _pendingOutputs.size() > 0) {
        		for (Object message : _pendingOutputs) {
        		    if (message instanceof String) {
        			_webSocket.writeTextFrame((String)message);
        		    } else {
        			_webSocket.writeBinaryFrame((Buffer)message);
        		    }
        		}
        		_pendingOutputs.clear();
        	    }
        	}
            }
        });
    }

    /** Private constructor for WebSocketHelper for a server-side web socket.
     *  @param currentObj The JavaScript instance of Socket that this helps.
     *  @param serverWebSocket The server-side web socket, provided by the web socket server.
     */
    private WebSocketHelper(ScriptObjectMirror currentObj, WebSocketBase serverWebSocket) {
        _currentObj = currentObj;
        _webSocket = serverWebSocket;
        // The serverSocket was already opened because a client successfully connected to the server.
        _wsIsOpen = true;

        _webSocket.dataHandler(new DataHandler());
        _webSocket.endHandler(new EndHandler());
        _webSocket.exceptionHandler(new WebSocketExceptionHandler());
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private fields                        ////
        
    /** The current instance of the JavaScript module. */
    private ScriptObjectMirror _currentObj;
    
    /** Pending outputs received before the socket is opened. */
    private List _pendingOutputs;

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
        public void handle(Buffer buffer) {
            synchronized(WebSocketHelper.this) {
        	// This assumes the input is a string encoded in UTF-8.
        	_currentObj.callMember("notifyIncoming", buffer.toString());
            }
        }
    }

    /** The event handler that is triggered when the web socket connection is closed.
     */
    private class EndHandler extends VoidHandler {
        @Override
        protected void handle() {
            synchronized(WebSocketHelper.this) {
        	_currentObj.callMember("emit", "close");
                _wsIsOpen = false;
                if (_pendingOutputs != null && _pendingOutputs.size() > 0) {
                    _currentObj.callMember("emit", "error", "Unsent data remains in the queue.");
                }
            }
        }
    }

    /** The event handler that is triggered when an error occurs in the web socket connection.
     */
    private class WebSocketExceptionHandler implements Handler<Throwable> {
        @Override
        public void handle(Throwable throwable) {
            synchronized(WebSocketHelper.this) {
        	_currentObj.callMember("emit", "error", throwable.getMessage());
        	_wsIsOpen = false;
            }
        }
    }

    /** The event handler that is triggered when an error occurs in the http client.
     */
    private class HttpClientExceptionHandler implements Handler<Throwable> {
        @Override
        public void handle(Throwable arg0) {
            synchronized(WebSocketHelper.this) {
        	_currentObj.callMember("emit", "close", arg0.getMessage());
        	_wsIsOpen = false;
            }
        }
    }
}
