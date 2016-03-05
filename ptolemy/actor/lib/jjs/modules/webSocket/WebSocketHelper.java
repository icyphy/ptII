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

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.imageio.ImageIO;

import io.vertx.core.Handler;
import io.vertx.core.VoidHandler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.WebSocket;
import io.vertx.core.http.WebSocketBase;
import io.vertx.core.http.WebSocketFrame;
import io.vertx.core.net.PemTrustOptions;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import ptolemy.actor.lib.jjs.VertxHelperBase;
import ptolemy.data.AWTImageToken;
import ptolemy.data.ImageToken;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.util.FileUtilities;
import ptolemy.util.MessageHandler;

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
                synchronized(WebSocketHelper.this) {
                    if (_webSocket != null) {
                        if (_wsIsOpen) {
                            _webSocket.close();
                            _wsIsOpen = false;
                        }
                        _webSocket = null;
                    }

                    if (_pendingOutputs != null && !_pendingOutputs.isEmpty()) {
                        _error("Unsent messages remain that were queued before the socket opened: "
                                + _pendingOutputs.toString());
                    }
                }
            }
        });
    }

    /** Create a WebSocketHelper instance for the specified JavaScript
     *  Socket instance for the client side of the socket.
     *  @param currentObj The JavaScript instance of the Socket.
     *  @param host IP address or host name of the host.
     *  @param port The port number that the host listens on.
     *  @param sslTls Whether SSL/TLS is enabled. This defaults to false.
     *  @param receiveType The type to assume for incoming messages.
     *  @param sendType The type for outgoing messages.
     *  @param connectTimeout The time to wait before giving up on a connection.
     *  @param numberOfRetries The number of retries.
     *  @param timeBetweenRetries The time between retries, in milliseconds.
     *  @param trustAll Whether to trust any server certificate. This defaults to false.
     *  @param trustedCACertPath The filename for the file that stores the certificate of a certificate authority (CA).
     *  @param discardMessagesBeforeOpen True to discard messages before the socket is open. False to queue them.
     *  @param throttleFactor The number of milliseconds to stall for each queued item waiting to be sent.
     *  @return A new WebSocketHelper instance.
     */
    public static WebSocketHelper createClientSocket(
            ScriptObjectMirror currentObj, String host, boolean sslTls, int port,
            String receiveType, String sendType,
            int connectTimeout,
            int numberOfRetries, int timeBetweenRetries,
            boolean trustAll, String trustedCACertPath,
            boolean discardMessagesBeforeOpen, int throttleFactor) {

        if (trustAll) {
            if (!MessageHandler.yesNoQuestion(
                    "The client is set to trust all certificates ('trustAll' option is true). "
                    + "This means that the client can connect to any server with no "
                    + "verification of the identity of the server. "
                    + "Are you sure?")) {
                return null;
            }
        }
        return new WebSocketHelper(currentObj, host, sslTls, port, receiveType, sendType,
                connectTimeout, numberOfRetries, timeBetweenRetries, trustAll, trustedCACertPath,
                discardMessagesBeforeOpen, throttleFactor);
    }

    /** Create a WebSocketHelper instance for the specified JavaScript
     *  Socket instance for the server side of the socket.
     *  @param currentObj The JavaScript instance of the Socket.
     *  @param serverWebSocket The given server-side Java socket.
     *  @param helper The helper in charge of this socket.
     *  @param receiveType The type to assume for incoming messages.
     *  @param sendType The type for outgoing messages.
     *  @return A new WebSocketHelper instance.
     */
    public static WebSocketHelper createServerSocket(
            ScriptObjectMirror currentObj, WebSocketBase serverWebSocket,
            WebSocketServerHelper helper,
            String receiveType, String sendType) {
        return new WebSocketHelper(
                currentObj, serverWebSocket, helper, receiveType, sendType);
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
    
    /** Open the web socket. This will be deferred to a vertx event loop.
     */
    public void open() {
        submit(() -> {
            _numberOfTries = 1;
            if (_DEBUG) {
                _actor.log("### Requesting connection to server: " + _host + " at port " + _port);
            }
            _connectWebsocket(_host, _port, _client);
        });
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

        boolean queuedMessage = false;
        synchronized(WebSocketHelper.this) {
            if (!isOpen()) {
                // Socket is not open.
                if (_discardMessagesBeforeOpen) {
                    _actor.log("WARNING: Data discarded because socket is not open.");
                    return;
                } else {
                    // Add the message to the queue of messages to be sent.
                    // Synchronize to ensure that we are not in the middle of draining
                    // the _pendingOutputs queue.
                    if (_DEBUG) {
                        _actor.log("Adding message to pending messages queue.");
                    }
                    _pendingOutputs.add(message);
                    queuedMessage = true;
                }
            }
        }
        if (queuedMessage) {
            // Message was queued. May want to throttle message production.
            // Do not do this while holding the synchronization lock.
            // If there are already pending outputs, stall the director thread.
            // Note that this is not the same as stalling when the send buffer
            // is full. That happens after the socket is open, whereas this happens before it is open.
            // Notice that the stall occurs in the calling thread only if the calling thread is
            // the director thread.  Otherwise, the stall is deferred to the director thread.
            if (_throttleFactor > 0) {
                _issueResponse(() -> {
                    long sleepTime = 0L;
                    synchronized(WebSocketHelper.this) {
                        // NOTE that _pendingOutputs may have already been drained.
                        if (_pendingOutputs.size() > 0) {
                            sleepTime = _throttleFactor * (_pendingOutputs.size() - 1);
                        }
                    }
                    if (sleepTime > 0L) {
                        if (_DEBUG) {
                            _actor.log("Sleeping for " + sleepTime + " ms in thread " + Thread.currentThread());
                        }
                        try {
                            Thread.sleep(sleepTime);
                        } catch (InterruptedException e) {
                            // Ignore.
                        }
                    }
                });
            }
            return;
        }
        
        // Socket is open. Ready to send.
        final Object finalMessage = message;
        
        // First, create the task that will perform the send.
        final Runnable sendTask = new Runnable() {
            public void run() {
                if (_wsFailed != null) {
                    _error("Failed to establish connection: "
                            + _wsFailed.toString());
                }
                if (isOpen()) {
                    if (_DEBUG) {
                        _actor.log("Sending message over socket.");
                    }
                    _sendMessageOverSocket(finalMessage);
                } else if (_discardMessagesBeforeOpen) {
                    _actor.log("WARNING: Data discarded because socket is not open: "
                            + finalMessage);
                } else {
                    // Add the message to the queue of messages to be sent.
                    // It is important that this be done in the verticle thread
                    // to ensure that each message is sent exactly once, because the
                    // queue is drained in this thread.
                    // Synchronize to make sure we are not in the middle of draining
                    // the queue.
                    synchronized(WebSocketHelper.this) {
                        if (_DEBUG) {
                            _actor.log("Adding message to pending messages queue (2).");
                        }
                        _pendingOutputs.add(finalMessage);
                    }
                }
            }
        };
        
        // Block if the send buffer is full.
        // Note that this should be called in the director thread, not
        // in the Vert.x thread, so blocking is OK. We need to stall
        // execution of the model to not get ahead of the capability.
        if (_webSocket.writeQueueFull()) {
            // Blocking _must not_ be done in the verticle.
            // If this is called outside the director thread, then defer to the
            // director thread.
            _issueResponse(() -> {
                synchronized(this) {
                    try {
                        if (isOpen() && _webSocket.writeQueueFull()) {
                            _actor.log("WARNING: Send buffer is full. Stalling to allow it to drain.");
                            wait();
                        }
                    } catch (InterruptedException e) {
                        _error("Buffer is full, and wait for draining was interrupted");
                    }
                }
                submit(sendTask);
            });
            // The send has been either completed or deferred, depending on which 
            // thread calls this.
            return;
        }

        // Ready to send.
        // Defer this action to be executed in the associated verticle.
        submit(sendTask);
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
        /* NOTE: Previously, we had a bug where we created two verticles, and the following
         * workaround _seemed_ to solve the problem. It was an illusion.
        String eventBusID = _webSocket.binaryHandlerID();
        _vertx.eventBus().send(eventBusID, (Buffer)message);
        */
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private constructors                   ////

    /** Private constructor for WebSocketHelper to open a client-side web socket.
     *  This does not open the socket. You must call open on this helper to open it.
     *  Do this after setting up event handlers.
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
    private WebSocketHelper(ScriptObjectMirror currentObj, String host, boolean sslTls,
            int port, String receiveType, String sendType,
            int connectTimeout,
            int numberOfRetries, int timeBetweenRetries,
            boolean trustAll, String trustedCACertPath,
            boolean discardMessagesBeforeOpen, int throttleFactor) {
        super(currentObj);

        _host = host;
        _sslTls = sslTls;
        _port = port;
        _receiveType = receiveType;
        _sendType = sendType;
        _connectTimeout = connectTimeout;
        _numberOfRetries = numberOfRetries;
        _timeBetweenRetries = timeBetweenRetries;
        _trustAll = trustAll;
        _trustedCACertPath = trustedCACertPath;
        _discardMessagesBeforeOpen = discardMessagesBeforeOpen;
        _throttleFactor = throttleFactor;
        // Ask the verticle to set up the web socket.
        // This will execute in a vert.x event loop thread, and
        // all callbacks that are set up as a side effect will also
        // execute in that thread.
        submit(new Runnable() {
            public void run() {
                HttpClientOptions clientOptions = new HttpClientOptions()
                        .setDefaultHost(host)
                        .setDefaultPort(port)
                        .setKeepAlive(true)
                        .setConnectTimeout(_connectTimeout)
                        .setSsl(_sslTls)
                        .setTrustAll(_trustAll);

                // If SSL/TLS is enabled and trustAll is false, it has to be configured.
                if (clientOptions.isSsl() && !clientOptions.isTrustAll()) {
                    PemTrustOptions pemTrustOptions = new PemTrustOptions();

                    String caCertPath = _trustedCACertPath;
                    File caCertFile = FileUtilities.nameToFile(caCertPath, null);

                    if (caCertFile == null) {
                        _error(currentObj, "Empty trustedCACertPath option. Can't find the trusted CA certificate.");
                        return;
                    }
                    try {
                        pemTrustOptions.addCertPath(caCertFile.getCanonicalPath());

                        clientOptions.setPemTrustOptions(pemTrustOptions);
                    } catch (IOException e) {
                        _error(currentObj, "Failed to find the trusted CA certificate at " + caCertFile);
                        return;
                    }
                }
                _client = _vertx.createHttpClient(clientOptions);
                _wsFailed = null;
            }
        });
    }

    /** Private constructor for WebSocketHelper for a server-side web socket.
     *  @param currentObj The JavaScript instance of Socket that this helps.
     *  @param serverWebSocket The server-side web socket, provided by the web socket server.
     *  @param helper The server helper in charge of this socket.
     *  @param receiveType The type to assume for incoming messages.
     *  @param sendType The type for outgoing messages.
     */
    private WebSocketHelper(
            ScriptObjectMirror currentObj,
            WebSocketBase serverWebSocket,
            WebSocketServerHelper helper,
            String receiveType,
            String sendType) {
        super(currentObj, helper);
        _webSocket = serverWebSocket;
        // The serverSocket was already opened because a client successfully connected to the server.
        _wsIsOpen = true;

        _receiveType = receiveType;
        _sendType = sendType;

        // FIXME: Grab the headers and get the Content-Type from that.
        // However, the headers don't seem to available.
        // MultiMap headers = serverWebSocket.headers();

        // Note that this is already called in a vert.x thread, so we do not
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
                if (_DEBUG) {
                    _actor.log("Response to request for websocket received from server.");
                }
                if (_numberOfTries < 0) {
                    // close() has been called. Abort the connection.
                    if (_DEBUG) {
                        _actor.log("close() has been called. Abort the connection.");
                    }
                    websocket.close();
                    return;
                }
                if (!_actor.isExecuting()) {
                    // Actor is not executing.
                    if (_DEBUG) {
                        _actor.log("Actor is not executing. Abort the connection.");
                    }
                    websocket.close();
                    return;
                }
                _webSocket = websocket;

                _webSocket.frameHandler(new DataHandler());
                _webSocket.endHandler(new EndHandler());
                _webSocket.exceptionHandler(new WebSocketExceptionHandler());
                _webSocket.closeHandler(new WebSocketCloseHandler());
                _webSocket.drainHandler(new WebSocketDrainHandler());

                // Socket.io uses the name "connect" for this event, but WS uses "open".
                // We choose "open".
                // Issue the response in the verticle, not in the director thread,
                // because the response may involve setting up listeners.
                // Grab a synchronization lock on the actor because JavaScript
                // uses such a synchronization lock when invoking input handlers,
                // and we need to make sure that the "open" handler and a "toSend"
                // input handler are not executed simultaneously (to preserve the
                // integrity of the pendingSends queue).
                synchronized(_actor) {
                    _currentObj.callMember("emit", "open");
                }

                // Send any pending messages.
                // Synchronize to make sure no pending outputs are added while
                // we are iterating through the queue.
                synchronized(WebSocketHelper.this) {
                    // Set this inside the synchronized block to ensure that
                    // after this block is executed, messages are not put
                    // on the pending queue.
                    _wsIsOpen = true;
                    if (_pendingOutputs != null && !_pendingOutputs.isEmpty()) {
                        if (_DEBUG) {
                            _actor.log("Sending " + _pendingOutputs.size() + " pending messages.");
                        }
                        for (Object message : _pendingOutputs) {
                            _sendMessageOverSocket(message);
                        }
                        _pendingOutputs.clear();
                    }
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
    
    /** Verbose debug flag. */
    private final static boolean _DEBUG = false;

    /** True to discard messages before the socket is open. False to discard them. */
    private boolean _discardMessagesBeforeOpen;

    /** The host IP or name. */
    private String _host;
    
    /** Whether the client connects to secure web server through SSL/TLS. */
    private boolean _sslTls;

    /** Maximum number of attempts to reconnect to the web socket if the first try fails. */
    private int _numberOfRetries = 0;

    /** Number of attempts so far to connect to the web socket. */
    private int _numberOfTries = 1;

    /** Pending outputs received before the socket is opened. */
    private ConcurrentLinkedQueue<Object> _pendingOutputs = new ConcurrentLinkedQueue<Object>();

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

    /** Whether the client trust all certificates. */
    private boolean _trustAll;

    /** The path for a certificate of the trusted certificate authority (CA). */
    private String _trustedCACertPath;

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
                // This ensures that if the handler responds to a "data" event by
                // producing multiple outputs, then all those outputs appear simultaneously
                // (with the same time stamp) on the accessor output.
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
            _currentObj.callMember("emit", "close", "Stream has ended.");
            synchronized(WebSocketHelper.this) {
                _wsIsOpen = false;
                if (_pendingOutputs != null && !_pendingOutputs.isEmpty()) {
                    _error("Unsent data remains in the queue.");
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
            _error(throwable.getMessage());
            _wsIsOpen = false;
        }
    }

    /** The event handler that is triggered when a socket is closed.
     */
    private class WebSocketCloseHandler implements Handler<Void> {
        @Override
        public void handle(Void ignored) {
            _currentObj.callMember("emit", "close");
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
            String message = arg0.getMessage();
            Throwable cause = arg0.getCause();
            while (cause != null) {
                message = message + "/n" + cause.getMessage();
                cause = cause.getCause();
            }
            if (_numberOfTries >= _numberOfRetries + 1) {
                _error("Connection failed after "
                        + _numberOfTries
                        + " tries: "
                        + message);
                _wsIsOpen = false;
                _wsFailed = arg0;
            } else if (_numberOfTries < 0) {
                _error("Connection closed while trying to connect: "
                        + message);
                _wsIsOpen = false;
            } else {
                _actor.log("Connection failed. Will try again: "
                        + message);
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
