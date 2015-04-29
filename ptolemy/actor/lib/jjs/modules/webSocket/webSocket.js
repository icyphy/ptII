// Module supporting web sockets.
// Authors: Hokeun Kim and Edward A. Lee
// Copyright: http://terraswarm.org/accessors/copyright.txt
//

var WebSocketHelper = Java.type('ptolemy.actor.lib.jjs.modules.webSocket.WebSocketHelper');
var WebSocketServerHelper = Java.type('ptolemy.actor.lib.jjs.modules.webSocket.WebSocketServerHelper');

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
var EventEmitter = require('events').EventEmitter;
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

// FIXME: Should create one base class, Socket, and two derived classes,
// ClientSocket and ServerSocket. The constructor arguments are different.

////////////////////
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

////////////////////
// Send text or binary data to the server. 
exports.Socket.prototype.send = function(data) {
    if (!this.helper.isOpen()) {
        throw new Error('cannot send, because the socket is not opened');
    }
    if (typeof data == 'string') {
        this.helper.sendText(data);
    }
    else {
        var JavaByteArray = Java.type("byte[]");
        javaData = new JavaByteArray(data.length);
        for (var i = 0; i < data.length; i++) {
            javaData[i] = data[i];
        }
        this.helper.sendBinary(javaData);
    }
}

////////////////////
// Close the current connection with the server.
exports.Socket.prototype.close = function() {
    this.helper.close();
}

////////////////////
// Convert data fromat from binary array to string.
exports.binToStr = function(data) {
   var result = "";
  for (var i = 0; i < data.length; i++) {
    result += String.fromCharCode(data[i]);
  }
  return result;
}
