// Module supporting web sockets.
// Authors: Hokeun Kim and Edward A. Lee
// Copyright: http://terraswarm.org/accessors/copyright.txt

var WebSocketHelper = Java.type('ptolemy.actor.lib.jjs.modules.webSocket.WebSocketHelper');
var WebSocketServerHelper = Java.type('ptolemy.actor.lib.jjs.modules.webSocket.WebSocketServerHelper');
var EventEmitter = require('events').EventEmitter;

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
 *  @param host The IP address or host name for the host.
 *  @param port The port on which the host is listening.
 */
exports.Client = function(host, port) {
    this.helper = WebSocketHelper.createClientSocket(this, host, port);
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

// FIXME: Code below needs help.

////////////////////
// Construct an instance of WebSocket Server.
// After invoking this constructor (using new), the user script should set up listeners
// and then invoke the startServer() function on this Server.
// This will create an HTTP server on the local host.
// A typical usage pattern looks like this:
//     var server = new WebSocket.Server({port:8082});
//     server.on('listening', onListening);
//     server.on('connection', onConnection);
//     server.startServer();
// where onListening is a handler for an event that this Server emits
// when it is listening for connections, and onConnection is a handler
// for an event that this Server emits when a client requests a websocket
// connection.
// 
// This subclasses EventEmitter.
// The options argument is an object containing the following (optional)
// fields:
// - port: The port on which to listen for connections (default is 80, the default HTTP port).
// - FIXME: What other options are supported? Why isn't this just a port argument?
// - FIXME: Should provide a mechanism to validate the "Origin" header during the
//   connection establishment process on the serverside (against the expected origins)
//   to avoid Cross-Site WebSocket Hijacking attacks.
exports.Server = function(options) {
    this.port = options['port'] || 80;
    this.helper = WebSocketServerHelper.createServer(this, this.port);
}
util.inherits(exports.Server, EventEmitter);

// Method to start the server. 
exports.Server.prototype.startServer = function() {
    this.helper.startServer();
}

// Method to create a server web socket with the given Java ServerWebSocket object.
exports.Server.prototype.createServerWebSocket = function(serverWebSocket) {
    return new exports.Socket("", serverWebSocket);
}

exports.Server.prototype.close = function() {
    this.helper.closeServer();
}


// FIXME: Below is probably obsolete

// Construct an instance of a socket with the specified URL.
// If the second argument is non-null, then create a socket on the server
// and ignore the url argument.
// Otherwise, create a socket on the client.
// FIXME: What is the second argument? Java expects a WebSocketBase. How to create that?
// This subclasses EventEmitter.
exports.Socket = function(url, serverWebSocket) {
    if (serverWebSocket != null) {
        this.helper = WebSocketHelper.createServerSocket(this, serverWebSocket);;
    } else {
        this.helper = WebSocketHelper.createClientSocket(this, url);
    }
    // Note that additional functions will be added via the prototype below.
    this.address = url;
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

////////////////////
// Close the current connection with the server.
exports.Socket.prototype.close = function() {
    this.helper.close();
}
