// Module supporting web sockets.
// Authors: Hokeun Kim and Edward A. Lee
// Copyright: http://terraswarm.org/accessors/copyright.txt

var WebSocketHelper = Java.type('ptolemy.actor.lib.jjs.modules.webSocket.WebSocketHelper');
var WebSocketServerHelper = Java.type('ptolemy.actor.lib.jjs.modules.webSocket.WebSocketServerHelper');
var EventEmitter = require('events').EventEmitter;

// This file contains first the code for a Client and then for a Server side
// of a web socket.

///////////////////////////////////////////////////////////////////////////////
//// Client

/** Construct an instance of a socket client that can send or receive messages
 *  to a server at the specified host and port.
 *  The returned object subclasses EventEmitter.
 *  You can register handlers for events 'open', 'message', 'close', or 'error'.
 *  For example,
 *  <pre>
 *    var WebSocket = require('webSocket');
 *    var client = new WebSocket.Client('localhost', 8080);
 *    client.on('message', onMessage);
 *    function onMessage(message) {
 *      print('Received from web socket: ' + message);
 *    }
 *  </pre>
 *  @param options A JSON object with fields 'host' and 'port' that give the
 *   IP address or host name for the host and the port on which the host is listening.
 *   If the host is omitted, 'localhost' is used. If the port is omitted, 80 is used.
 */
exports.Client = function(options) {
    options = options || {};
    this.port = options['port'] || 80;
    this.host = options['host'] || 'localhost';
    this.helper = WebSocketHelper.createClientSocket(this, this.host, this.port);
}
util.inherits(exports.Client, EventEmitter);

/** Send data over the web socket.
 *  The data can be anything that has a JSON representation.
 *  If the socket has not yet been successfully opened, then queue
 *  data to be sent later, when the socket is opened.
 *  @param data The data to send.
 */
exports.Client.prototype.send = function(data) {
    if (typeof data != 'string') {
        data = JSON.stringify(data);
    }
    this.helper.sendText(data);
}

/** Close the current connection with the server.
 *  If there is data that was passed to send() but has not yet
 *  been successfully sent (because the socket was not open),
 *  then throw an exception.
 */
exports.Client.prototype.close = function() {
    this.helper.close();
}

/** Notify this object of a received message from the socket.
 *  This function attempts to parse the message as JSON and then
 *  emits a "message" event with the message as an argument.
 *  This function is called by the Java helper used by this particular
 *  implementation and should not be normally called by the user.
 *  FIXME: Any way to hide it?
 *  @param message The incoming message.
 */
exports.Client.prototype.notifyIncoming = function(message) {
    try {
        message = JSON.parse(message);
    } catch (exception) {
        // Assume that the message is a string.
        // We can ignore the exception, because the message
        // will be passed as a string.
    }
    this.emit("message", message);
};

///////////////////////////////////////////////////////////////////////////////
//// Server

/** Construct an instance of WebSocket Server.
 *  After invoking this constructor (using new), the user script should set up listeners
 *  and then invoke the start() function on this Server.
 *  This will create an HTTP server on the local host.
 *  The options argument is a JSON object containing the following optional fields:
 *  <ul>
 *  <li> hostInterface: The IP address or name of the local interface for the server
 *       to listen on.  This defaults to "localhost", but if the host machine has more
 *       than one network interface, e.g. an Ethernet and WiFi interface, then you may
 *       need to specifically specify the IP address of that interface here.
 *  <li> port: The port on which to listen for connections (the default is 80,
 *       which is the default HTTP port).
 *  </ul>
 *  This subclasses EventEmitter, emitting events 'listening' and 'connection'.
 *  A typical usage pattern looks like this:
 *  <pre>
 *     var server = new WebSocket.Server({port:8082});
 *     server.on('listening', onListening);
 *     server.on('connection', onConnection);
 *     server.start();
 *  </pre>
 *  where onListening is a handler for an event that this Server emits
 *  when it is listening for connections, and onConnection is a handler
 *  for an event that this Server emits when a client requests a websocket
 *  connection and the socket has been successfully established.
 *  When the 'connection' event is emitted, it will be passed a Socket object,
 *  and the onConnection handler can register a listener for 'message' events
 *  on that Socket object, as follows:
 *  <pre>
 *     server.on('connection', function(socket) {
 *        socket.on('message', function(message) {
 *            console.log(message);
 *            socket.send('Reply message');
 *        });
 *     });
 *  </pre>
 *  The Socket object also has a close() function that allows the server to close
 *  the connection.
 *  <p>
 *  FIXME: Should provide a mechanism to validate the "Origin" header during the
 *    connection establishment process on the serverside (against the expected origins)
 *    to avoid Cross-Site WebSocket Hijacking attacks.
 */
exports.Server = function(options) {
    this.port = options['port'] || 80;
    this.hostInterface = options['hostInterface'] || 'localhost';
    this.helper = WebSocketServerHelper.createServer(this, this.hostInterface, this.port);
}
util.inherits(exports.Server, EventEmitter);

/** Start the server. */
exports.Server.prototype.start = function() {
    this.helper.startServer();
}

/** Stop the server. */
exports.Server.prototype.close = function() {
    this.helper.closeServer();
}

/** Notify that a handshake was successful and a websocket has been created.
 *  This is called by the helper class is not meant to be called by the JavaScript
 *  programmer. When this is called, the Server will a new Socket object
 *  and emit a 'connection' event with that Socket as an argument.
 *  The 'connection' handler can then register for 'message' events from the
 *  Socket or issue replies to the Socket using send(). It can also close() the
 *  Socket.
 *  @param serverWebSocket The Java ServerWebSocket object.
 */
exports.Server.prototype.socketCreated = function(serverWebSocket) {
    var socket = new exports.Socket(serverWebSocket);
    this.emit('connection', socket);
}

/** Construct (using new) a Socket object for the server side of a new connection.
 *  This is called by the socketCreated function above whenever a new connection is
 *  established at the request of a client. It should not normally be called by
 *  the JavaScript programmer. The returned Socket is an event emitter that emits
 *  'message' events.
 */
exports.Socket = function(serverWebSocket) {
    this.helper = WebSocketHelper.createServerSocket(this, serverWebSocket);;
}
util.inherits(exports.Socket, EventEmitter);

/** Notify this object of a received message from the socket.
 *  This function attempts to parse the message as JSON and then
 *  emits a "message" event with the message as an argument.
 *  @param message The incoming message.
 */
exports.Socket.prototype.notifyIncoming = function(message) {
    try {
        message = JSON.parse(message);
    } catch (exception) {
        // Assume that the message is a string.
        // We can ignore the exception, because the message
        // will be passed as a string.
    }
    this.emit("message", message);
};

/** Send data over the web socket.
 *  The data can be anything that has a JSON representation.
 *  @param data The data to send.
 */
exports.Socket.prototype.send = function(data) {
    if (typeof data != 'string') {
        data = JSON.stringify(data);
    }
    this.helper.sendText(data);
}

/** Close the socket. Normally, this would be called on the client side,
 *  not on the server side. But the server can also close the connection.
 */
exports.Socket.prototype.close = function() {
    this.helper.close();
}
