/* Embedding of a Datagram (UDP) socket.

@Copyright (c) 2015-2017 The Regents of the University of California.
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

package ptolemy.actor.lib.jjs.modules.udpSocket;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.datagram.DatagramPacket;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.datagram.DatagramSocketOptions;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;
import ptolemy.actor.lib.jjs.VertxHelperBase;
import ptolemy.actor.lib.jjs.modules.socket.SocketHelper.ByteArrayBackedInputStream;
import ptolemy.data.AWTImageToken;
import ptolemy.data.ImageToken;

///////////////////////////////////////////////////////////////////
////UDPSocketHelper

/**
   A helper class for the udpSocket module in JavaScript.
   See the documentation of that module for instructions.

   @author Hokeun Kim
   @version $Id$
   @since Ptolemy II 11.0
   @Pt.ProposedRating Yellow (eal)
   @Pt.AcceptedRating Red (bilung)
 */

public class UDPSocketHelper extends VertxHelperBase {

    /** Constructor for UDPSocketHelper for the specified actor.
     *  @param actor The actor that this will help.
     *  @param helping The JavaScript object that this helping.
     */
    public UDPSocketHelper(Object actor, ScriptObjectMirror helping) {
        super(actor, helping);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create the UDP socket.
     *  @param scriptObjectMirror The JavaScript instance invoking the shell.
     *  @param enableBroadcast enabling or not message broadcasting
     *  @return The UDP socket helper.
     */
    public UDPSocket createSocket(ScriptObjectMirror scriptObjectMirror,
            boolean enableBroadcast) {
        // FIXME: Support send and receive types.
        return new UDPSocket(scriptObjectMirror, enableBroadcast);
    }

    /** Get or create a helper for the specified actor.
     *  If one has been created before and has not been garbage collected, return
     *  that one. Otherwise, create a new one.
     *  @param actor Either a JavaScript actor or a RestrictedJavaScriptInterface.
     *  @param helping The JavaScript object that this is helping.
     *  @return The UDPSocketHelper.
     */
    public static UDPSocketHelper getOrCreateHelper(Object actor,
            ScriptObjectMirror helping) {
        VertxHelperBase helper = VertxHelperBase.getHelper(actor);
        if (helper instanceof UDPSocketHelper
                && helper.getHelping() == helping) {
            return (UDPSocketHelper) helper;
        }
        return new UDPSocketHelper(actor, helping);
    }

    /** Return an array of the types supported by the current host for
     *  receiveType arguments.
     *  @return An array of types.
     */
    public static String[] supportedReceiveTypes() {
        // NOTE: Regrettably, Nashorn can't find the static method of the base class, even though
        // in theory that method is inherited by this class. Hence, we have to define it here
        // as well.
        return VertxHelperBase.supportedReceiveTypes();
    }

    /** Return an array of the types supported by the current host for
     *  sendType arguments.
     *  @return An array of types.
     */
    public static String[] supportedSendTypes() {
        // NOTE: Regrettably, Nashorn can't find the static method of the base class, even though
        // in theory that method is inherited by this class. Hence, we have to define it here
        // as well.
        return VertxHelperBase.supportedSendTypes();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Socket helper for individual sockets.
     */
    public class UDPSocket {

        /** Construct a socket.
         *  @param currentObj The corresponding JavaScript Socket object.
         *  @param enableBroadcast enabling or not message broadcasting
         */
        public UDPSocket(ScriptObjectMirror currentObj,
                boolean enableBroadcast) {
            _currentObj = currentObj;
            _isOpen = false;
            if (enableBroadcast == true) {
                _socket = _vertx.createDatagramSocket(
                        new DatagramSocketOptions().setBroadcast(true));
            } else {
                _socket = _vertx.createDatagramSocket();
            }
        }

        /** Listen for datagram messages on the specified port and optional address.
         *  Once binding is complete, a 'listening' event is emitted and the
         *  optional callback function is called.
         *  @param port The port number.
         *  @param address The address of the network interface on which to listen,
         *   or "0.0.0.0" to listen on all addresses.
         *  @param callback A callback function to invoke when the binding is complete,
         *   or null to not request a callback.
         */
        public void bind(final int port, final String address,
                final ScriptObjectMirror callback) {
            submit(() -> {
                _socket.listen(port, address,
                        new Handler<AsyncResult<DatagramSocket>>() {
                            @Override
                            public void handle(
                                    AsyncResult<DatagramSocket> asyncResult) {
                                if (asyncResult.succeeded()) {
                                    _isOpen = true;

                                    _socket.handler(
                                            new Handler<DatagramPacket>() {
                                                @Override
                                                public void handle(
                                                        final DatagramPacket packet) {
                                                    // Emit the message in the director thread.
                                                    _issueResponse(() -> {
                                                        // Construct a string with the sender information following
                                                        // the JSON format
                                                        String sender = "{\"ipAddress\": \""
                                                                + packet.sender()
                                                                        .host()
                                                                + "\",";
                                                        sender += "\"port\": "
                                                                + packet.sender()
                                                                        .port()
                                                                + "}";
                                                        _emitMessage(
                                                                packet.data(),
                                                                sender);
                                                    });
                                                }
                                            });
                                    _socket.endHandler(new Handler<Void>() {
                                        @Override
                                        public void handle(Void foo) {
                                            close();
                                        }
                                    });
                                    _socket.exceptionHandler(
                                            new Handler<Throwable>() {
                                                @Override
                                                public void handle(
                                                        Throwable error) {
                                                    _currentObj.callMember(
                                                            "emit", "error",
                                                            error.getMessage());
                                                }
                                            });

                                    _issueResponse(() -> {
                                        if (callback != null) {
                                            callback.call(_currentObj);
                                        }
                                        _currentObj.callMember("emit",
                                                "listening");
                                    });
                                } else {
                                    _error(_currentObj, "Bind failed: "
                                            + asyncResult.cause());
                                }
                            }
                        });
            });
        }

        /** Close the UDP socket.
         */
        public void close() {
            if (_isOpen) {
                _isOpen = false; // Assign here, instead of in callback, since
                                 // more calls to close() may occur before
                                 // callback completes.  Assumes success.
                _socket.close((AsyncResult<Void> result) -> {
                    _currentObj.callMember("emit", "close");
                });
            }
        }

        /** Send a datagram message.
         *  @param data The data to send.
         *  @param port The destination port.
         *  @param hostname The name of the destination host (a hostname or IP address).
         *  @param callback An optional callback function to invoke when the send is complete,
         *   or if an error occurs. In the latter case, the cause of the error will be passed
         *   as an argument to the callback.
         */
        public void send(final Object data, final int port,
                final String hostname, final ScriptObjectMirror callback) {
            submit(() -> {
                Buffer buffer = Buffer.buffer();
                // Handle the case where data is an array.
                if (data instanceof Object[]) {
                    for (Object element : (Object[]) data) {
                        // JavaScript arrays can have holes, and moreover,
                        // it seems that Nashorn's Java.to() function creates
                        // a bigger array than needed with trailing null elements.
                        if (element != null) {
                            _appendToBuffer(element, _sendType, _sendImageType,
                                    buffer);
                        }
                    }
                } else if (data instanceof ScriptObjectMirror) {
                    // If we pass a Uint8Array to UDPSocketSender,
                    // then we end up passing a JavaScript object like
                    // { '1': 48, '2': 57 ...}  and data is a
                    // ScriptObjectMirror. So we go through the values
                    // and create an array.

                    // FIXME: The object might not be an array.  This
                    // is ignoring the keys.

                    // One idea would be to traverse the keys and only
                    // create an array if all the keys were numbers
                    // starting with 1 and increasing monotonically.
                    if (!_rawBytes) {
                        for (Object element : ((ScriptObjectMirror) data)
                                .values()) {
                            if (element != null) {
                                _appendToBuffer(element, _sendType,
                                        _sendImageType, buffer);
                            }
                        }
                    } else {
                        for (Object element : ((ScriptObjectMirror) data)
                                .values()) {
                            if (element != null) {
                                byte lowByte = (byte) (((Integer) element)
                                        & 0xFF);
                                buffer.appendByte(lowByte);
                            }
                        }
                    }
                } else {
                    _appendToBuffer(data, _sendType, _sendImageType, buffer);
                }

                // Send a Buffer
                _socket.send(buffer, port, hostname,
                        new Handler<AsyncResult<DatagramSocket>>() {
                            @Override
                            public void handle(
                                    AsyncResult<DatagramSocket> asyncResult) {
                                if (asyncResult.succeeded()) {
                                    if (callback != null) {
                                        callback.call(_currentObj);
                                    }
                                } else {
                                    if (callback != null) {
                                        callback.call(_currentObj,
                                                asyncResult.cause());
                                    }
                                    _error(_currentObj,
                                            "Send failed: "
                                                    + asyncResult.cause(),
                                            asyncResult.cause());
                                }
                            }
                        });
            });
        }

        /** Set raw bytes support. If this is not called, the attribute defaults to false.
         *  @param rawBytes The value of rawBytes.
         */
        public void setRawBytes(boolean rawBytes) {
            _rawBytes = rawBytes;
        }

        /** Set the receive type. If this is not called, the type defaults to "string".
         *  @param type The name of the receive type.
         */
        public void setReceiveType(String type) {
            try {
                _receiveType = Enum.valueOf(DATA_TYPE.class,
                        type.trim().toUpperCase());
            } catch (Exception ex) {
                _error(_currentObj, "Invalid receive data type: " + type, ex);
            }
        }

        /** Set the send type. If this is not called, the type defaults to "string".
         *  @param type The name of the send type.
         */
        public void setSendType(String type) {
            try {
                _sendType = Enum.valueOf(DATA_TYPE.class,
                        type.trim().toUpperCase());
            } catch (Exception ex) {
                // It might be an image type.
                if (getImageTypes().contains(type)) {
                    _sendImageType = type;
                    _sendType = DATA_TYPE.IMAGE;
                } else {
                    _error(_currentObj, "Invalid send data type: " + type);
                    throw new IllegalArgumentException(
                            "Invalid send data type: " + type);
                }
            }
        }

        /** Convert the buffer to a message depending on the specified receive type
         *  and emit a 'message' event. Both the message and sender address are emitted.
         *  @param buffer The buffer containing the message.
         *  @param sender A string containing the IP address and port of the sender.
         */
        protected void _emitMessage(Buffer buffer, String sender) {
            if (_receiveType == DATA_TYPE.STRING) {
                if (!_rawBytes) {
                    _currentObj.callMember("emit", "message",
                            buffer.getString(0, buffer.length()), sender);
                } else {
                    byte[] bytes = buffer.getBytes();
                    _currentObj.callMember("emit", "message", bytes, sender);
                }
            } else if (_receiveType == DATA_TYPE.IMAGE) {
                try {
                    byte[] bytes = buffer.getBytes();
                    ByteArrayBackedInputStream byteStream = new ByteArrayBackedInputStream(
                            bytes);
                    BufferedImage image = ImageIO.read(byteStream);
                    if (image != null && image.getHeight() > 0
                            && image.getWidth() > 0) {
                        ImageToken token = new AWTImageToken(image);
                        _currentObj.callMember("emit", "message", token,
                                sender);
                    } else {
                        _error(_currentObj, "Received corrupted image.");
                    }
                } catch (IOException e) {
                    _error(_currentObj,
                            "Failed to read incoming image: " + e.toString(),
                            e);
                }
            } else {
                // Assume a numeric type.
                int size = _sizeOfType(_receiveType);
                // Coverity Scan reports that _sizeOfType() can return
                // 0, which would invoke _error() but might return so
                // we check here.
                if (size == 0) {
                    _error(_currentObj,
                            "Type " + _receiveType + " is not supported.");
                } else {
                    int length = buffer.length();
                    int numberOfElements = length / size;
                    if (numberOfElements == 1) {
                        _currentObj.callMember("emit", "message",
                                _extractFromBuffer(buffer, _receiveType, 0),
                                sender);
                    } else if (numberOfElements > 1) {
                        // Using message framing, so we output a single array.
                        Object[] result = new Object[numberOfElements];
                        int position = 0;
                        for (int i = 0; i < result.length; i++) {
                            result[i] = _extractFromBuffer(buffer, _receiveType,
                                    position);
                            position += size;
                        }
                        // NOTE: If we return result, then the emitter will not
                        // emit a native JavaScript array. We have to do a song and
                        // dance here which is probably very inefficient (almost
                        // certainly... the array gets copied).
                        try {
                            _currentObj.callMember("emit", "message",
                                    _actor.toJSArray(result), sender);
                        } catch (Exception e) {
                            _error(_currentObj,
                                    "Failed to convert to a JavaScript array: "
                                            + e,
                                    e);
                            _currentObj.callMember("emit", "message", result,
                                    sender);
                        }
                    } else {
                        _error(_currentObj, "Expect to receive type "
                                + _receiveType
                                + ", but received an insufficient number of bytes: "
                                + buffer.length());
                    }
                }
            }
        }

        /** True if the socket is open, false otherwise.  Vert.x does not seem
         * to offer a way to check the socket status.  Needed to prevent calling
         * close() on a closed socket - doing so causes multiple close events
         * to be emitted, which an accessor may be listening to.
         */
        private boolean _isOpen;

        /** If set to true, then the string data to send will be interpreted as bytes. */
        private boolean _rawBytes = false;

        /** The receive type. */
        private DATA_TYPE _receiveType = DATA_TYPE.STRING;

        /** If an image, the send image type. */
        private String _sendImageType;

        /** The send type. */
        private DATA_TYPE _sendType = DATA_TYPE.STRING;

        /** The current instance of the Vert.x UDP socket. */
        private DatagramSocket _socket;

        /** The current instance of the JavaScript module. */
        private ScriptObjectMirror _currentObj;
    }
}
