/* AnExecute a script in JavaScript.

@Copyright (c) 2015 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

import org.vertx.java.core.Handler;
import org.vertx.java.core.VoidHandler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.WebSocket;
import org.vertx.java.core.http.WebSocketBase;

import ptolemy.actor.lib.jjs.modules.VertxHelperBase;
import ptolemy.data.AWTImageToken;
import ptolemy.data.ImageToken;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;

///////////////////////////////////////////////////////////////////
//// WebSocketHelper

/**
   A helper class for the webSocket module in JavaScript.
   Instances of this class are helpers for individual sockets.
   See the documentation of that module for instructions.
   This uses Vert.x for the implementation.

   @author Hokeun Kim and Edward A. Lee
   @version $Id$
   @see WebSocketServerHelper
   @since Ptolemy II 11.0
   @Pt.ProposedRating Yellow (eal)
   @Pt.AcceptedRating Red (bilung)
 */
public class WebSocketHelper extends VertxHelperBase {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Close the web socket.
     */
    public void close() {
        synchronized (_actor) {
            if (!_actor.isExecuting()) {
                _actor.log("Connection closed because model is no longer running.");
            } else {
                _actor.log("Connection closed.");
            }

            // Stop reconnect attempts. This will also
            // abort any connection that is established after this close()
            // method has been called.
            _numberOfTries = -1;
            if (_thread != null) {
                _thread.interrupt();
            }

            if (_webSocket != null) {
                if (_wsIsOpen) {
                    _webSocket.close();
                }
                _webSocket = null;
            }

            if (_pendingOutputs != null && _pendingOutputs.size() > 0) {
                try {
                    _currentObj.callMember("emit", "error",
                            "Unsent messages remain that were queued before the socket opened: "
                                    + _pendingOutputs.toString());
                } catch (Throwable ex) {
                    // Emitting an error event doesn't work for some reason.
                    // Report the error anyway.
                    _actor.error("Unsent messages remain that were queued before the socket opened: "
                            + _pendingOutputs.toString());
                }
            }
        }
    }

    /** Create a WebSocketHelper instance for the specified JavaScript
     *  Socket instance for the client side of the socket.
     *  @param currentObj The JavaScript instance of the Socket.
     *  @param host IP address or host name of the host.
     *  @param port The port number that the host listens on.
     *  @param receiveType The type to assume for incoming messages.
     *  @param sendType The type for outgoing messages.
     *  @param connectTimeout The time to wait before giving up on a connection.
     *  @param maxFrameSize The maximum frame size for a received message.
     *  @param numberOfRetries The number of retries.
     *  @param timeBetweenRetries The time between retries, in milliseconds.
     *  @param discardMessagesBeforeOpen True to discard messages before the socket is open. False to queue them.
     *  @param throttleFactor The number of milliseconds to stall for each queued item waiting to be sent.
     *  @return A new WebSocketHelper instance.
     */
    public static WebSocketHelper createClientSocket(
            ScriptObjectMirror currentObj, String host, int port,
            String receiveType, String sendType,
            int connectTimeout, int maxFrameSize,
            int numberOfRetries, int timeBetweenRetries,
            boolean discardMessagesBeforeOpen, int throttleFactor) {
        return new WebSocketHelper(currentObj, host, port, receiveType, sendType,
                connectTimeout, maxFrameSize, numberOfRetries,
                timeBetweenRetries, discardMessagesBeforeOpen, throttleFactor);
    }

    /** Create a WebSocketHelper instance for the specified JavaScript
     *  Socket instance for the server side of the socket.
     *  @param currentObj The JavaScript instance of the Socket.
     *  @param serverWebSocket The given server-side Java socket.
     *  @param receiveType The type to assume for incoming messages.
     *  @param sendType The type for outgoing messages.
     *  @param maxFrameSize The maximum frame size for a received message.
     *  @return A new WebSocketHelper instance.
     */
    public static WebSocketHelper createServerSocket(
            ScriptObjectMirror currentObj, WebSocketBase serverWebSocket,
            String receiveType, String sendType, int maxFrameSize) {
        return new WebSocketHelper(
                currentObj, serverWebSocket, receiveType, sendType, maxFrameSize);
    }

    /** Send data through the web socket.
     *  @param msg A message to be sent.
     *  @exception IllegalActionException If establishing the connection to the web socket has
     *   permanently failed.
     */
    public void send(Object msg) throws IllegalActionException {
        synchronized (_actor) {
            if (_wsFailed != null) {
                throw new IllegalActionException(_actor, _wsFailed,
                        "Failed to establish connection after "
                                + _numberOfTries + " tries.");
            }
            // If the message is not a string, attempt to create a Buffer object.
            if (!(msg instanceof String)) {
                if (msg instanceof ImageToken) {
                    Image image = ((ImageToken)msg).asAWTImage();
                    if (!(image instanceof BufferedImage)) {
                        throw new IllegalActionException(_actor, "Unsupported image token type: " + image.getClass());
                    }
                    if (!_sendType.startsWith("image/")) {
                        throw new IllegalActionException(_actor, "Trying to send an image, but sendType is " + _sendType);
                    }
                    String imageType = _sendType.substring(6);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    try {
                        ImageIO.write((BufferedImage)image, imageType, stream);
                    } catch (IOException e) {
                        throw new IllegalActionException(_actor, e, "Failed to convert image to byte array for sending.");
                    }
                    msg = new Buffer(stream.toByteArray());
                }
            }
            if (isOpen()) {
                _sendMessageOverSocket(msg);
            } else if (!_discardMessagesBeforeOpen) {
                // FIXME: Bound the queue?
                if (_pendingOutputs == null) {
                    _pendingOutputs = new LinkedList();
                }
                _pendingOutputs.add(msg);
                if (_throttleFactor > 0) {
                    try {
                        Thread.sleep(_throttleFactor * _pendingOutputs.size());
                    } catch (InterruptedException e) {
                        // Ignore.
                    }
                }
            } else {
                _actor.log("WARNING: Data discarded because socket is not open: "
                        + msg);
            }
        }
    }

    /** Return whether the web socket is opened successfully.
     *  @return True if the socket is open.
     */
    public boolean isOpen() {
        synchronized (_actor) {
            if (_webSocket == null) {
                return false;
            }
            return _wsIsOpen;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                     protected methods                     ////

    /** Send the specified message over the websocket.
     *  @param message The message.
     */
    protected void _sendMessageOverSocket(Object message) {
        if (message instanceof String) {
            message = new Buffer((String)message);
        }
        /* Sadly, the following check doesn't make sense.
         * Only the sender of the message can check whether
         * the frame size is sufficient for the message,
         * but it is the receiver of the message that has to
         * have the frame size set. Thus, we would need to check
         * the frame size of the other end of the socket, and
         * we don't have access to that.
        if (((Buffer)message).length() > _maxFrameSize) {
            _currentObj.callMember("emit", "error", 
                    "Message size of " + ((Buffer)message).length()
                    + " exceeds the maximum frame size of "
                    + _maxFrameSize
                    + ". Message not sent. Consider increasing the maxFrameSize.");
            _wsIsOpen = false;
            return;
        }
        */
        if (!(message instanceof Buffer)) {
            _currentObj.callMember("emit", "error", 
                    "Message type not recognized: " + message.getClass()
                    + ". Perhaps the sendType doesn't match the data type.");
            return;
        }
        _webSocket.write((Buffer)message);
        if (_webSocket.writeQueueFull()) {
            _webSocket.pause();
            _webSocket.drainHandler(new VoidHandler() {
                public void handle() {
                    _webSocket.resume();
                }
            });
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private constructors                   ////

    /** Private constructor for WebSocketHelper to open a client-side web socket.
     *  Open an internal web socket using Vert.x.
     *  @param currentObj The JavaScript instance of Socket that this helps.
     *  @param host The IP address or host name of the host.
     *  @param port The port number of the host.
     *  @param receiveType The type to assume for incoming messages.
     *  @param sendType The type for outgoing messages.
     *  @param connectTimeout The time to wait before giving up on a connection.
     *  @param maxFrameSize The maximum frame size for a received message.
     *  @param numberOfRetries The maximum number of retries if a connect attempt fails.
     *  @param timeBetweenRetries The time between retries, in milliseconds.
     *  @param discardMessagesBeforeOpen True to discard messages before the socket is open, 
     *   false to queue them.
     *  @param throttleFactor The number of milliseconds to stall for each queued item
     *   waiting to be sent.
     */
    private WebSocketHelper(ScriptObjectMirror currentObj, String host,
            int port, String receiveType, String sendType,
            int connectTimeout, int maxFrameSize,
            int numberOfRetries, int timeBetweenRetries,
            boolean discardMessagesBeforeOpen, int throttleFactor) {
        super(currentObj);

        _host = host;
        _port = port;
        _receiveType = receiveType;
        _sendType = sendType;
        _connectTimeout = connectTimeout;
        _maxFrameSize = maxFrameSize;
        _numberOfRetries = numberOfRetries;
        _timeBetweenRetries = timeBetweenRetries;
        _discardMessagesBeforeOpen = discardMessagesBeforeOpen;
        _throttleFactor = throttleFactor;
        // FIXME: This seems to become a zombie thread.
        _client = _vertx.createHttpClient();
        _client.setHost(host)
            .setPort(port)
            .exceptionHandler(new HttpClientExceptionHandler())
            .setKeepAlive(true)
            .setConnectTimeout(_connectTimeout)
            .setMaxWebSocketFrameSize(_maxFrameSize);
        _wsFailed = null;

        // Make the connection. This might eventually be a wrapped in a public method.
        _numberOfTries = 1;
        _connectWebsocket(host, port, _client);
    }

    /** Private constructor for WebSocketHelper for a server-side web socket.
     *  @param currentObj The JavaScript instance of Socket that this helps.
     *  @param serverWebSocket The server-side web socket, provided by the web socket server.
     *  @param receiveType The type to assume for incoming messages.
     *  @param sendType The type for outgoing messages.
     *  @param maxFrameSize The maximum frame size for a received message.
     */
    private WebSocketHelper(ScriptObjectMirror currentObj,
            WebSocketBase serverWebSocket, String receiveType, String sendType, int maxFrameSize) {
        super(currentObj);
        _webSocket = serverWebSocket;
        // The serverSocket was already opened because a client successfully connected to the server.
        _wsIsOpen = true;
        
        _maxFrameSize = maxFrameSize;
        
        // FIXME: Grab the headers and get the Content-Type from that.
        // However, the headers don't seem to available.
        // MultiMap headers = serverWebSocket.headers();
                
        _webSocket.dataHandler(new DataHandler());
        _webSocket.endHandler(new EndHandler());
        _webSocket.exceptionHandler(new WebSocketExceptionHandler());
        _webSocket.closeHandler(new WebSocketCloseHandler());

        _receiveType = receiveType;
        _sendType = sendType;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Connect to a web socket on the specified host.
     *  @param host The host IP or name.
     *  @param port The port.
     *  @param client The HttpClient object.
     */
    private void _connectWebsocket(String host, int port, HttpClient client) {
        // FIXME: Provide a timeout. Use setTimeout() of the client.
        // FIXME: Why does Vertx require the URI here in addition to setHost() and setPort() above? Seems lame.
        String address = "ws://" + host + ":" + port;
        client.connectWebsocket(address, new Handler<WebSocket>() {
            @Override
            public void handle(WebSocket websocket) {
                // Synchronize to ensure mutex w/ the disconnect in wrapup.
                synchronized (_actor) {
                    if (_numberOfTries < 0) {
                        // close() has been called. Abort the connection.
                        websocket.close();
                        return;
                    }
                    if (!_actor.isExecuting()) {
                        // Either wrapup() has been called, or wrapup() is blocked waiting
                        // for the _actor lock we now hold.
                        websocket.close();
                        return;
                    }
                    _wsIsOpen = true;
                    _webSocket = websocket;

                    _webSocket.dataHandler(new DataHandler());
                    _webSocket.endHandler(new EndHandler());
                    _webSocket.exceptionHandler(new WebSocketExceptionHandler());
                    _webSocket.closeHandler(new WebSocketCloseHandler());

                    // Socket.io uses the name "connect" for this event, but WS uses "open",
                    // so we just emit both events.
                    _currentObj.callMember("emit", "connect");
                    _currentObj.callMember("emit", "open");

                    // Send any pending messages.
                    if (_pendingOutputs != null && _pendingOutputs.size() > 0) {
                        for (Object message : _pendingOutputs) {
                            _sendMessageOverSocket(message);
                        }
                        _pendingOutputs.clear();
                    }
                }
            }
        });
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private fields                        ////

    /** The HttpClient object. */
    private HttpClient _client;
    
    /** The time to wait before giving up on a connection. */
    private int _connectTimeout = 60000;

    /** True to discard messages before the socket is open. False to discard them. */
    private boolean _discardMessagesBeforeOpen;

    /** The host IP or name. */
    private String _host;
    
    /** The maximum frame size for a received message. */
    private int _maxFrameSize = 65536;

    /** Maximum number of attempts to reconnect to the web socket if the first try fails. */
    private int _numberOfRetries = 0;

    /** Number of attempts so far to connect to the web socket. */
    private int _numberOfTries = 1;

    /** Pending outputs received before the socket is opened. */
    private List _pendingOutputs;

    /** The host port. */
    private int _port;
    
    /** The MIME type to assume for received messages. */
    private String _receiveType;
    
    /** The MIME type to assume for sent messages. */
    private String _sendType;

    /** The number of milliseconds to stall for each queued item waiting to be sent. */
    private int _throttleFactor;

    /** The time between retries, in milliseconds. */
    private int _timeBetweenRetries;

    /** The thread that reconnects the web socket connection after the timeBetweenRetries interval. */
    private Thread _thread = null;

    /** The internal web socket created by Vert.x */
    private WebSocketBase _webSocket = null;

    /** Indicator that web socket connection has failed. No need to keep waiting. */
    private Throwable _wsFailed = null;

    /** Whether the internal web socket is opened successfully. */
    private boolean _wsIsOpen = false;

    ///////////////////////////////////////////////////////////////////
    ////                     private classes                        ////

    /** The event handler that is triggered when a message arrives on the web socket.
     */
    private class DataHandler implements Handler<Buffer> {
        @Override
        public void handle(Buffer buffer) {
            // FIXME: Why is this synchronized?
            synchronized (_actor) {
                if (_receiveType.equals("application/json")
                        || _receiveType.startsWith("text/")) {
                    // This assumes the input is a string encoded in UTF-8.
                    _currentObj.callMember("notifyIncoming", buffer.toString());
                } else if (_receiveType.startsWith("image/")) {
                    try {
                        BufferedImage image = ImageIO.read(new ByteArrayInputStream(buffer.getBytes()));
                        ImageToken token = new AWTImageToken(image);
                        _currentObj.callMember("notifyIncoming", token);
                    } catch (IOException e) {
                        // FIXME: How to report this error?
                        e.printStackTrace();
                    }
                } else {
                    // FIXME: Need to catch this error earlier!!!
                    throw new InternalErrorException("Unsupported receiveType: " + _receiveType);
                }
            }
        }
    }

    /** The event handler that is triggered when the web socket connection is closed.
     */
    private class EndHandler extends VoidHandler {
        @Override
        protected void handle() {
            // FIXME: Why is this synchronized?
            synchronized (_actor) {
                _currentObj.callMember("emit", "close", "Stream has ended.");
                _wsIsOpen = false;
                if (_pendingOutputs != null && _pendingOutputs.size() > 0) {
                    _currentObj.callMember("emit", "error",
                            "Unsent data remains in the queue.");
                }
            }
        }
    }

    /** The event handler that is triggered when an error occurs in the web socket connection.
     *  An error here occurs after a socket connection has been established.
     */
    private class WebSocketExceptionHandler implements Handler<Throwable> {
        @Override
        public void handle(Throwable throwable) {
            synchronized (_actor) {
                _currentObj.callMember("emit", "error", throwable.getMessage());
                _wsIsOpen = false;
            }
        }
    }
    
    /** The event handler that is triggered when a socket is closed.
     */
    private class WebSocketCloseHandler implements Handler {
        @Override
        public void handle(Object ignored) {
            synchronized (_actor) {
                _currentObj.callMember("emit", "close");
                _wsIsOpen = false;
            }
        }
    }

    /** The event handler that is triggered when an error occurs in the http client.
     *  An error here may occur, for example, when a socket fails to get established.
     */
    private class HttpClientExceptionHandler implements Handler<Throwable> {
        @Override
        public void handle(Throwable arg0) {
            synchronized (_actor) {
                if (_numberOfTries >= _numberOfRetries + 1) {
                    _currentObj.callMember("emit", "error",
                            "Connection failed after " + _numberOfTries
                                    + " tries: " + arg0.getMessage());
                    _wsIsOpen = false;
                    _wsFailed = arg0;
                } else if (_numberOfTries < 0) {
                    _currentObj.callMember("emit", "error",
                            "Connection closed while trying to connect: "
                                    + arg0.getMessage());
                    _wsIsOpen = false;
                } else {
                    _actor.log("Connection failed. Will try again: "
                            + arg0.getMessage());
                    // Retry. Create a new thread to do this.
                    Runnable retry = new Runnable() {
                        public void run() {
                            try {
                                Thread.sleep(_timeBetweenRetries);
                            } catch (InterruptedException e) {
                                _currentObj
                                        .callMember("emit", "error",
                                                "Reconnection thread has been interrupted.");
                                _wsIsOpen = false;
                                _wsFailed = e;
                                return;
                            }
                            synchronized (_actor) {
                                // Prevent any attempt to interrupt this thread, since it is
                                // no longer sleeping.
                                _thread = null;
                                // Check again the status of the number of tries.
                                // This may have been changed if close() was called,
                                // which occurs, for example, when the model stops executing.
                                // NOTE: This may actually try connecting even if there will be
                                // no more firings of the JavaScript actor. But it will only try
                                // if wrapup has not yet been called, so if it succeeds in connecting,
                                // presumably either wrapup() or the handler in _connectWebsocket will
                                // disconnect if the model is no longer executing.
                                if (_numberOfTries >= 0
                                        && _numberOfTries <= _numberOfRetries
                                        && _actor.isExecuting()) {
                                    _numberOfTries++;
                                    _connectWebsocket(_host, _port, _client);
                                }
                            }
                        }
                    };
                    _thread = new Thread(retry);
                    _thread.start();
                }
            }
        }
    }
}
