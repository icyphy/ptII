/* Support for the websocket accessor.

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

import io.vertx.core.Handler;
import io.vertx.core.VoidHandler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.WebSocket;
import io.vertx.core.http.WebSocketBase;
import io.vertx.core.http.WebSocketFrame;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.imageio.ImageIO;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import ptolemy.actor.lib.jjs.VertxHelperBase;
import ptolemy.data.AWTImageToken;
import ptolemy.data.ImageToken;
import ptolemy.kernel.util.IllegalActionException;

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
   @Pt.AcceptedRating Red (eal)
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
        }
        // Defer the rest of the close to the associated verticle.
        submit(new Runnable() {
            public void run() {
                if (_webSocket != null) {
                    if (_wsIsOpen) {
                        _webSocket.close();
                    }
                    _webSocket = null;
                }

                if (_pendingOutputs != null && !_pendingOutputs.isEmpty()) {
                    _error("Unsent messages remain that were queued before the socket opened: "
                            + _pendingOutputs.toString());
                }
            }
        });
    }

    /** Create a WebSocketHelper instance for the specified JavaScript
     *  Socket instance for the client side of the socket.
     *  @param currentObj The JavaScript instance of the Socket.
     *  @param host IP address or host name of the host.
     *  @param port The port number that the host listens on.
     *  @param receiveType The type to assume for incoming messages.
     *  @param sendType The type for outgoing messages.
     *  @param connectTimeout The time to wait before giving up on a connection.
     *  @param numberOfRetries The number of retries.
     *  @param timeBetweenRetries The time between retries, in milliseconds.
     *  @param discardMessagesBeforeOpen True to discard messages before the socket is open. False to queue them.
     *  @param throttleFactor The number of milliseconds to stall for each queued item waiting to be sent.
     *  @return A new WebSocketHelper instance.
     */
    public static WebSocketHelper createClientSocket(
            ScriptObjectMirror currentObj, String host, int port,
            String receiveType, String sendType,
            int connectTimeout,
            int numberOfRetries, int timeBetweenRetries,
            boolean discardMessagesBeforeOpen, int throttleFactor) {
        return new WebSocketHelper(currentObj, host, port, receiveType, sendType,
                connectTimeout, numberOfRetries,
                timeBetweenRetries, discardMessagesBeforeOpen, throttleFactor);
    }

    /** Create a WebSocketHelper instance for the specified JavaScript
     *  Socket instance for the server side of the socket.
     *  @param currentObj The JavaScript instance of the Socket.
     *  @param serverWebSocket The given server-side Java socket.
     *  @param receiveType The type to assume for incoming messages.
     *  @param sendType The type for outgoing messages.
     *  @return A new WebSocketHelper instance.
     */
    public static WebSocketHelper createServerSocket(
            ScriptObjectMirror currentObj, WebSocketBase serverWebSocket,
            String receiveType, String sendType) {
        return new WebSocketHelper(
                currentObj, serverWebSocket, receiveType, sendType);
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

    /** Send data through the web socket.
     *  Note that if throttleFactor is not zero, then this method could
     *  block for some time. Thus, it must not be called in a verticle.
     *  It is called by the input handler of the actor.
     *  @param msg A message to be sent.
     *  @exception IllegalActionException If establishing the connection to the web socket has
     *   permanently failed.
     */
    public void send(final Object msg) throws IllegalActionException {

        if (isOpen()) {
            // Block if the send buffer is full.
            // Note that this should be called in the director thread, not
            // in the Vert.x thread, so blocking is OK. We need to stall
            // execution of the model to not get ahead of the capability.
            while(isOpen() && _webSocket.writeQueueFull()) {
                synchronized(this) {
                    try {
                        // Coverity Scan warned:
                        // "wait_cond_improperly_checked: The wait
                        // condition prompting the wait upon
                        // WebSocketHelper.this is not checked
                        // correctly. This code can wait for a
                        // condition that has already been satisfied,
                        // which can cause a never-ending wait."

                        // The suggested fix: "Refactor the code to
                        // protect the call to wait with a loop that
                        // rechecks the wait condition inside the
                        // locked region."
                        if (isOpen() && _webSocket.writeQueueFull()) {
                            _actor.log("WARNING: Send buffer is full. Stalling to allow it to drain.");
                            wait();
                        }
                    } catch (InterruptedException e) {
                        _error("Buffer is full, and wait for draining was interrupted");
                    }
                }
            }
        } else {
            // Socket is not open.
            // If there are already pending outputs, we stall before submitting.
            // Note that this is not the same as stalling when the send buffer
            // is full. That happens after the socket is open, whereas this happens before it is open.
            // FIXME: Maybe it would just be better to bound the queue?
            if (_pendingOutputs != null && !_pendingOutputs.isEmpty() && _throttleFactor > 0) {
                try {
                    Thread.sleep(_throttleFactor * _pendingOutputs.size());
                } catch (InterruptedException e) {
                    // Ignore.
                }
            }
        }

        // Ready to send.
        
        // Defer this action to be executed in the associated verticle.
        final Runnable action = new Runnable() {
            public void run() {
                if (_wsFailed != null) {
                    _error("Failed to establish connection: "
                            + _wsFailed.toString());
                }
                // If the message is not a string, attempt to create a Buffer object.
                Object message = msg;
                if (!(msg instanceof String)) {
                    if (msg instanceof ImageToken) {
                        Image image = ((ImageToken)msg).asAWTImage();
                        if (!(image instanceof BufferedImage)) {
                            _error("Unsupported image token type: " + image.getClass());
                        }
                        if (!_sendType.startsWith("image/")) {
                            _error("Trying to send an image, but sendType is " + _sendType);
                        }
                        String imageType = _sendType.substring(6);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        try {
                            ImageIO.write((BufferedImage)image, imageType, stream);
                        } catch (IOException e) {
                            _error("Failed to convert image to byte array for sending: " + e.toString());
                        }
                        message = Buffer.buffer(stream.toByteArray());
                    }
                }
                if (isOpen()) {
                    _sendMessageOverSocket(message);
                } else if (_discardMessagesBeforeOpen) {
                    _actor.log("WARNING: Data discarded because socket is not open: "
                            + message);
                } else {
                    // Add the message to the queue of messages to be sent.
                    // It is important that this be done in the verticle thread
                    // to ensure that each message is sent exactly once, because the
                    // queue is drained in this thread.
                    if (_pendingOutputs == null) {
                        _pendingOutputs = new ConcurrentLinkedQueue<Object>();
                    }
                    _pendingOutputs.add(message);
                }
            }
        };
        submit(action);
    }

    /** Return an array of the types supported by the current host for
     *  receiveType arguments.
     */
    public static String[] supportedReceiveTypes() {
        String[] imageTypes = ImageIO.getReaderFormatNames();
        String[] result = new String[imageTypes.length + 2];
        result[0] = "application/json";
        result[1] = "text/plain";
        System.arraycopy(imageTypes, 0, result, 2, imageTypes.length);
        return result;
    }

    /** Return an array of the types supported by the current host for
     *  sendType arguments.
     */
    public static String[] supportedSendTypes() {
        String[] imageTypes = ImageIO.getWriterFormatNames();
        String[] result = new String[imageTypes.length + 2];
        result[0] = "application/json";
        result[1] = "text/plain";
        System.arraycopy(imageTypes, 0, result, 2, imageTypes.length);
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     protected methods                     ////

    /** Send the specified message over the websocket.
     *  @param message The message.
     */
    protected void _sendMessageOverSocket(Object message) {
        if (message instanceof String) {
            message = Buffer.buffer((String)message);
        }
        if (!(message instanceof Buffer)) {
            _error("Message type not recognized: "
                    + message.getClass()
                    + ". Perhaps the sendType doesn't match the data type.");
            return;
        }
        // NOTE: If the message exceeds the frame buffer size, the following will break
        // it down into chunks.
        _webSocket.writeBinaryMessage((Buffer)message);
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
     *  @param numberOfRetries The maximum number of retries if a connect attempt fails.
     *  @param timeBetweenRetries The time between retries, in milliseconds.
     *  @param discardMessagesBeforeOpen True to discard messages before the socket is open, 
     *   false to queue them.
     *  @param throttleFactor The number of milliseconds to stall for each queued item
     *   waiting to be sent.
     */
    private WebSocketHelper(ScriptObjectMirror currentObj, String host,
            int port, String receiveType, String sendType,
            int connectTimeout,
            int numberOfRetries, int timeBetweenRetries,
            boolean discardMessagesBeforeOpen, int throttleFactor) {
        super(currentObj);

        _host = host;
        _port = port;
        _receiveType = receiveType;
        _sendType = sendType;
        _connectTimeout = connectTimeout;
        _numberOfRetries = numberOfRetries;
        _timeBetweenRetries = timeBetweenRetries;
        _discardMessagesBeforeOpen = discardMessagesBeforeOpen;
        _throttleFactor = throttleFactor;
        // Ask the verticle to set up the web socket.
        // This will execute in a vert.x event loop thread, and
        // all callbacks that are set up as a side effect will also
        // execute in that thread.
        submit(new Runnable() {
            public void run() {
                _client = _vertx.createHttpClient(new HttpClientOptions()
                .setDefaultHost(host)
                .setDefaultPort(port)
                .setKeepAlive(true)
                .setConnectTimeout(_connectTimeout));
                
                _wsFailed = null;

                // Make the connection. This might eventually be a wrapped in a public method.
                _numberOfTries = 1;
                _connectWebsocket(host, port, _client);
            }
        });
    }

    /** Private constructor for WebSocketHelper for a server-side web socket.
     *  @param currentObj The JavaScript instance of Socket that this helps.
     *  @param serverWebSocket The server-side web socket, provided by the web socket server.
     *  @param receiveType The type to assume for incoming messages.
     *  @param sendType The type for outgoing messages.
     */
    private WebSocketHelper(ScriptObjectMirror currentObj,
            WebSocketBase serverWebSocket, String receiveType, String sendType) {
        super(currentObj);
        _webSocket = serverWebSocket;
        // The serverSocket was already opened because a client successfully connected to the server.
        _wsIsOpen = true;

        _receiveType = receiveType;
        _sendType = sendType;

        // FIXME: Grab the headers and get the Content-Type from that.
        // However, the headers don't seem to available.
        // MultiMap headers = serverWebSocket.headers();

        // Note that his is already called in a vert.x thread, so we do not
        // (and should not) defer this using submit().
        _webSocket.frameHandler(new DataHandler());
        _webSocket.endHandler(new EndHandler());
        _webSocket.exceptionHandler(new WebSocketExceptionHandler());
        _webSocket.closeHandler(new WebSocketCloseHandler());
        _webSocket.drainHandler(new WebSocketDrainHandler());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Connect to a web socket on the specified host.
     *  @param host The host IP or name.
     *  @param port The port.
     *  @param client The HttpClient object.
     */
    private void _connectWebsocket(String host, int port, HttpClient client) {
        // FIXME: Header with content type should be provided?
        client.websocket(port, host, "", new Handler<WebSocket>() {
            @Override
            public void handle(WebSocket websocket) {
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

                _webSocket.frameHandler(new DataHandler());
                _webSocket.endHandler(new EndHandler());
                _webSocket.exceptionHandler(new WebSocketExceptionHandler());
                _webSocket.closeHandler(new WebSocketCloseHandler());
                _webSocket.drainHandler(new WebSocketDrainHandler());

                // Socket.io uses the name "connect" for this event, but WS uses "open".
                // We choose "open".
                // Issue the response in the director thread, not in the verticle.
                _issueResponse(() -> {
                    _currentObj.callMember("emit", "open");
                });

                // Send any pending messages.
                if (_pendingOutputs != null && !_pendingOutputs.isEmpty()) {
                    for (Object message : _pendingOutputs) {
                        _sendMessageOverSocket(message);
                    }
                    _pendingOutputs.clear();
                }
            }
        }, new HttpClientExceptionHandler());
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private fields                        ////

    /** The HttpClient object. */
    private HttpClient _client;

    /** The time to wait before giving up on a connection. */
    private int _connectTimeout = 5000;

    /** True to discard messages before the socket is open. False to discard them. */
    private boolean _discardMessagesBeforeOpen;

    /** The host IP or name. */
    private String _host;

    /** Maximum number of attempts to reconnect to the web socket if the first try fails. */
    private int _numberOfRetries = 0;

    /** Number of attempts so far to connect to the web socket. */
    private int _numberOfTries = 1;

    /** Pending outputs received before the socket is opened. */
    private ConcurrentLinkedQueue<Object> _pendingOutputs;

    /** The host port. */
    private int _port;
    
    /** The received buffer, used to collect messages if spread over multiple frames. */
    private Buffer _receivedBuffer;

    /** The MIME type to assume for received messages. */
    private String _receiveType;

    /** The MIME type to assume for sent messages. */
    private String _sendType;

    /** The number of milliseconds to stall for each queued item waiting to be sent. */
    private int _throttleFactor;

    /** The time between retries, in milliseconds. */
    private int _timeBetweenRetries;

    /** The internal web socket created by Vert.x */
    private WebSocketBase _webSocket = null;

    /** Indicator that web socket connection has failed. No need to keep waiting. */
    private Throwable _wsFailed = null;

    /** Whether the internal web socket is opened successfully. */
    private boolean _wsIsOpen = false;

    ///////////////////////////////////////////////////////////////////
    ////                     private classes                        ////

    /** The event handler that is triggered when a frame arrives on the web socket.
     */
    private class DataHandler implements Handler<WebSocketFrame> {
        @Override
        public void handle(WebSocketFrame frame) {
            if (_receivedBuffer == null) {
                _receivedBuffer = frame.binaryData();
            } else {
                _receivedBuffer.appendBuffer(frame.binaryData());
            }
            if (frame.isFinal()) {
                // Complete message has been received.
                final Buffer buffer = _receivedBuffer;
                _receivedBuffer = null;
                // Issue the response in the director thread, not in the verticle.
                _issueResponse(() -> {
                    if (_receiveType.equals("application/json")
                            || _receiveType.startsWith("text/")) {
                        // This assumes the input is a string encoded in UTF-8.
                        _currentObj.callMember("_notifyIncoming", buffer.toString());
                    } else if (_receiveType.startsWith("image/")) {
                        try {
                            BufferedImage image = ImageIO.read(new ByteArrayInputStream(buffer.getBytes()));
                            ImageToken token = new AWTImageToken(image);
                            _currentObj.callMember("_notifyIncoming", token);
                        } catch (IOException e) {
                            _error("Failed to read incoming image: " + e.toString());
                        }
                    } else {
                        _error("Unsupported receiveType: " + _receiveType);
                    }
                });
            }
        }
    }

    /** The event handler that is triggered when the web socket connection is closed.
     */
    private class EndHandler extends VoidHandler {
        @Override
        protected void handle() {
            // Issue the response in the director thread, not in the verticle.
            _issueResponse(() -> {
                _currentObj.callMember("emit", "close", "Stream has ended.");
                _wsIsOpen = false;
                if (_pendingOutputs != null && !_pendingOutputs.isEmpty()) {
                    _error("Unsent data remains in the queue.");
                }
            });
        }
    }

    /** The event handler that is triggered when an error occurs in the web socket connection.
     *  An error here occurs after a socket connection has been established.
     */
    private class WebSocketExceptionHandler implements Handler<Throwable> {
        @Override
        public void handle(Throwable throwable) {
            _error(throwable.getMessage());
            _wsIsOpen = false;
        }
    }

    /** The event handler that is triggered when a socket is closed.
     */
    private class WebSocketCloseHandler implements Handler<Void> {
        @Override
        public void handle(Void ignored) {
            // Issue the response in the director thread, not in the verticle.
            _issueResponse(() -> {
                _currentObj.callMember("emit", "close");
            });
            _wsIsOpen = false;
        }
    }
    
    /** The event handler that is triggered when a full send buffer
     *  has been half emptied.
     */
    private class WebSocketDrainHandler implements Handler<Void> {
        @Override
        public void handle(Void ignored) {
            synchronized(WebSocketHelper.this) {
                // This should unblock send(),
                WebSocketHelper.this.notifyAll();
            }
        }
    }

    /** The event handler that is triggered when an error occurs in the http client.
     *  An error here may occur, for example, when a socket fails to get established.
     */
    private class HttpClientExceptionHandler implements Handler<Throwable> {
        @Override
        public void handle(Throwable arg0) {
            if (_numberOfTries >= _numberOfRetries + 1) {
                _error("Connection failed after "
                        + _numberOfTries
                        + " tries: "
                        + arg0.getMessage());
                _wsIsOpen = false;
                _wsFailed = arg0;
            } else if (_numberOfTries < 0) {
                _error("Connection closed while trying to connect: "
                        + arg0.getMessage());
                _wsIsOpen = false;
            } else {
                _actor.log("Connection failed. Will try again: "
                        + arg0.getMessage());
                // Retry after some time.
                _vertx.setTimer(_timeBetweenRetries, id -> {
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
                });
            }
        }
    }
}
