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
package ptolemy.actor.lib.jjs.modules.socket;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.http.ClientAuth;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.NetSocket;

import java.util.Map;

import javax.imageio.ImageIO;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import ptolemy.actor.lib.jjs.VertxHelperBase;

///////////////////////////////////////////////////////////////////
//// SocketHelper

/**
   A helper class for the socket module in JavaScript.
   You should use {@link #getOrCreateHelper(Object)} to create
   exactly one instance of this helper per actor. Pass the actor
   as an argument.

   @author Edward A. Lee
   @version $Id$
   @see SocketServerHelper
   @since Ptolemy II 11.0
   @Pt.ProposedRating Yellow (eal)
   @Pt.AcceptedRating Red (eal)
 */
public class SocketHelper extends VertxHelperBase {

    /** Constructor for SocketHelper for the specified actor.
     *  @param actor The actor that this will help.
     */
    public SocketHelper(Object actor) {
    	super(actor);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a client-side socket on behalf of the specified
     *  JavaScript SocketClient object. After this is called,
     *  the specified socketClient will emit the following events:
     *  * open: Emitted when the connection has been established
     *    with the server. This will be passed an instance of SocketWrapper
     *    that is unique to this socket and has a send() and close()
     *    function that can be used to send data or to close the socket.
     *  * data: Emitted when data is received on the socket.
     *    The received data will be an argument to the event.
     *  * close: Emitted when a socket is closed.
     *    This will be passed the instance of SocketWrapper for
     *    the socket that was closed.
     *  * error: Emitted when an error occurs. This will be passed
     *    an error message.
     * 
     *  @param socketClient The JavaScript SocketClient instance.
     *  @param port The remote port to connect to.
     *  @param host The remote host to connect to.
     *  @param options The options (see the socket.js JavaScript module).
     */
    public void openClientSocket(
    		ScriptObjectMirror socketClient,
    		int port,
    		String host,
    		Map<String,Object> options) {
    	
    	// NOTE: The following assumes all the options are defined.
    	// This is handled in the associated JavaScript socket.js module.
    	final NetClientOptions clientOptions = new NetClientOptions()
    			.setConnectTimeout((Integer)options.get("connectTimeout"))
    			.setIdleTimeout((Integer)options.get("idleTimeout"))
    			.setReceiveBufferSize((Integer)options.get("receiveBufferSize"))
    			.setReconnectAttempts((Integer)options.get("reconnectAttempts"))
    			.setReconnectInterval((Integer)options.get("reconnectInterval"))
    			.setSendBufferSize((Integer)options.get("sendBufferSize"))
    			.setSsl((Boolean)options.get("sslTls"))
    			.setTcpKeepAlive((Boolean)options.get("keepAlive"));

    	/* FIXME: Not used options:
        'receiveType': 'application/json',
        'sendType': 'application/json',
        'discardMessagesBeforeOpen': false,
		*/

    	// Create the socket in the associated verticle.
    	submit(() -> {
	    	NetClient client = _vertx.createNetClient(clientOptions);
	    	ConnectResponseHandler handler = new ConnectResponseHandler(socketClient, client);
	    	// NOTE: In principle, this client can handle multiple connections.
	    	// But here we use exactly one client per connection. Is this OK?
	    	client.connect(port, host, handler);
    	});
    }
    
    /** Get or create a helper for the specified actor.
     *  If one has been created before and has not been garbage collected, return
     *  that one. Otherwise, create a new one.
	 *  @param actor Either a JavaScript actor or a RestrictedJavaScriptInterface.
     */
    public static SocketHelper getOrCreateHelper(Object actor) {
    	VertxHelperBase helper = VertxHelperBase.getHelper(actor);
    	if (helper instanceof SocketHelper) {
    		return (SocketHelper) helper;
    	}
    	return new SocketHelper(actor);
    }

    /** Create a server that can accept socket connection requests
     *  on behalf of the specified JavaScript SocketServer object.
     *  After this is called, the specified socketServer will emit
     *  the following events:
     *  <ul>
     *  <li> listening: Emitted when the server is listening.
     *    This will be passed the NetServer instance that is created.
     *    This can be used, for example, to get the port number that
     *    the server is listening on by calling the NetServer's
     *    actualPort() method (this is useful if the port is specified to be 0).
     *  <li> connection: Emitted when a new connection is established
     *    after a request from (possibly remote) client.
     *    This will be passed an instance of SocketWrapper
     *    that is unique to this socket and has a send() and close()
     *    function that can be used to send data or to close the socket.
     *  <li> data: Emitted when data is received on a socket.
     *    This will be passed two arguments, the wrapper for the
     *    socket that received the data and the data.
     *  <li> close: Emitted when a socket is closed.
     *    This will be passed the instance of SocketWrapper for
     *    the socket that was closed.
     *  </ul>
     *  @param socketServer The JavaScript SocketServer instance.
     *  @param options The options (see the socket.js JavaScript module).
     */
    public void openServer(
    		final ScriptObjectMirror socketServer,
    		final Map<String,Object> options) {
    	
    	// Translate clientAuth option to an enum.
    	ClientAuth auth = ClientAuth.NONE;
    	String authSpec = (String)options.get("clientAuth");
    	if (authSpec.toLowerCase().trim().equals("request")) {
    		auth = ClientAuth.REQUEST;
    	} else if (authSpec.toLowerCase().trim().equals("required")) {
    		auth = ClientAuth.REQUIRED;
    	}
    	
    	// NOTE: The following assumes all the options are defined.
    	// This is handled in the associated JavaScript socket.js module.
    	final NetServerOptions serverOptions = new NetServerOptions()
    			.setClientAuth(auth)
    			.setHost((String)options.get("hostInterface"))
    			.setIdleTimeout((Integer)options.get("idleTimeout"))
    			.setTcpKeepAlive((Boolean)options.get("keepAlive"))
    			.setPort((Integer)options.get("port"))
    			.setReceiveBufferSize((Integer)options.get("receiveBufferSize"))
    			.setSendBufferSize((Integer)options.get("sendBufferSize"))
    			.setSsl((Boolean)options.get("sslTls"));
    	
    	/* FIXME: Not used:
        'receiveType': 'string',
        'sendType': 'string',
        */

    	// Create the server in the associated verticle.
    	submit(() -> {
	    	NetServer server = _vertx.createNetServer(serverOptions);
	    	
	    	server.connectHandler(socket -> {
	    		// Connection is established with a client.
	    		// Create a wrapper for the socket.
	    		// Second argument being null indicates that this is a server-side socket.
    			final SocketWrapper wrapper = new SocketWrapper(socketServer, null, socket);
    			
        	    // Set up handlers for data, errors, etc.
        	    socket.closeHandler((Void) -> {
        			_issueResponse(() -> {
        				socketServer.callMember("emit", "close", wrapper);
        			});
        	    });
        	    socket.drainHandler((Void) -> {
        			// FIXME: This should unblock send(),
        			// which should block itself when the buffer gets full.
        	    });
        	    socket.endHandler((Void) -> {
        	    	// End event on the socket triggers a close of the socket.
        	    	// This gets called when the remote side sends a FIN packet.
        	    	// FIXME: This isn't right. Need FIN from both ends to close the socket!
        	    	// socket.close();
        	    });
        	    socket.exceptionHandler(throwable -> {
        			_error(socketServer, throwable.toString());
        	    });
        	    socket.handler(buffer -> {
        			_issueResponse(() -> {
        				// FIXME: handle the buffer data more intelligently here.
        				// If the received type is 'double', 'byte', etc., then do multiple emits.
        				// See defaultOptions in the JS module.
        				// Share code with the Client.
        				socketServer.callMember("emit", "data", wrapper, buffer.toString());
        			});
        	    });

        	    // Emit the 'connection' event to indicate that a socket connection
        	    // is open.
    			_issueResponse(() -> {
    				socketServer.callMember("emit", "connection", wrapper);
    			});

	    	});
	    	
	    	server.listen(result -> {
				_issueResponse(() -> {
					if (result.succeeded()) {
						socketServer.callMember("emit", "listening", server);
					} else {
						_error("Failed to start server listening.");
					}
				});
	    	});
    	});
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
    ////                     private fields                        ////

    /** True to discard messages before the socket is open. False to discard them. */
    private boolean _discardMessagesBeforeOpen;
    
    /** The MIME type to assume for received messages. */
    private String _receiveType;
    
    /** The MIME type to assume for sent messages. */
    private String _sendType;

    ///////////////////////////////////////////////////////////////////
    ////                     public classes                        ////

    /** Wrapper for connected sockets.  */
    public class SocketWrapper {
    	
    	/** Construct a handler for connections established.
    	 *  @param socketClient The JavaScript SocketClient object.
    	 *  @param client The NetClient object establishing the connection.
    	 *  @param socket The Vertx socket object.
    	 */
    	public SocketWrapper(ScriptObjectMirror socketClient, NetClient client, NetSocket socket) {
    		_socketClient = socketClient;
    		_client = client;
    		_socket = socket;
    	}
    	/** Close the socket.
    	 */
		public void close() {
			submit(() -> {
				_socket.close();
				// For a server socket, the _client field is null.
				if (_client != null) {
					_client.close();
				}
			});
		}
		/** Send data over the socket.
		 *  @param data The data to send.
		 */
		public void send(final Object data) {
			// FIXME: Should block if the send buffer is full.
			
			submit(() -> {
				// FIXME: Need to handle data types here.
				if (data instanceof String) {
					// FIXME: A second argument could take an encoding.
					// Defaults to UTF-8. Option?
					_socket.write((String)data);
				} else {
					_error(_socketClient, "Unsupported type for socket: "
							+ data.getClass().getName());
				}
			});
		}
		private NetClient _client;
		private NetSocket _socket;
		private ScriptObjectMirror _socketClient;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private classes                        ////
    
    /** Handler for socket connection established.  */
    private class ConnectResponseHandler implements Handler<AsyncResult<NetSocket>> {
    	
    	/** Construct a handler for connections established.
    	 *  @param socketClient The JavaScript SocketClient object.
    	 *  @param client The NetClient object establishing the connection.
    	 */
    	public ConnectResponseHandler(ScriptObjectMirror socketClient, NetClient client) {
    		_socketClient = socketClient;
    		_client = client;
    	}
		@Override
		public void handle(AsyncResult<NetSocket> response) {
    		if (response.succeeded()) {
    			// Socket has been opened.
        	    NetSocket socket = response.result();
        	    // Create a wrapper for the socket.
    			final SocketWrapper wrapper = new SocketWrapper(_socketClient, _client, socket);
        	    // Set up handlers for data, errors, etc.
        	    socket.closeHandler((Void) -> {
        			_issueResponse(() -> {
        				_socketClient.callMember("emit", "close", wrapper);
        			});
        	    });
        	    socket.drainHandler((Void) -> {
        			// FIXME: This should unblock send(),
        			// which should block itself when the buffer gets full.
        	    });
        	    socket.endHandler((Void) -> {
        	    	// End event on the socket triggers a close of the socket.
        	    	// This gets called when the remote side sends a FIN packet.
        	    	// FIXME: This isn't right. Need FIN from both ends to close the socket!
        	    	// _client.close();
        	    });
        	    socket.exceptionHandler(throwable -> {
        			_error(_socketClient, throwable.toString());
        	    });
        	    socket.handler(buffer -> {
        			_issueResponse(() -> {
        				// FIXME: handle the buffer data more intelligently here.
        				// If the received type is 'double', 'byte', etc., then do multiple emits.
        				// See defaultOptions in the JS module.
        				_socketClient.callMember("emit", "data", buffer.toString());
        			});
        	    });
        	    
    			_issueResponse(() -> {
    				_socketClient.callMember("emit", "open", wrapper);
    			});
    		} else {
        	    _error(_socketClient, "Failed to connect: " + response.cause().getMessage());
    		}
		}
		private NetClient _client;
		private ScriptObjectMirror _socketClient;
    }
}
