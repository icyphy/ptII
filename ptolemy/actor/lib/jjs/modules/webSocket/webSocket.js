// Module supporting web sockets.
// Authors: Hokeun Kim and Edward A. Lee
// Copyright: http://terraswarm.org/accessors/copyright.txt
//
////////////////////
// Export this web socket module.
module.exports = WebSocket;

////////////////////
// The default name of the namespace
module.exports.namespaceName = "WebSocket";

////////////////////
// Construct an instance of WebSocket.
function WebSocket(url, serverWebSocket) {
    this.address = url;
    this.callbacks = {};

    if (serverWebSocket == null) {
        var WebSocketHelper = Java.type('ptolemy.actor.lib.jjs.modules.webSocket.WebSocketHelper');
        this.socket = WebSocketHelper.createClientSocket(actor.getEngine(), module.exports.namespaceName,
            this, this.address);
    }
    else {
        var WebSocketHelper = Java.type('ptolemy.actor.lib.jjs.modules.webSocket.WebSocketHelper');
        this.socket = WebSocketHelper.createServerSocket(actor.getEngine(), module.exports.namespaceName,
            this, serverWebSocket);;
    }
}

////////////////////
// Add callbacks to handle events.
// Usage: on('open', function).
// This method Supports the following events.
// Event 'open': triggered when the web socket is successfully connected to the server.
// Event 'message': triggered when a message arrives from the server.
// Event 'close': triggered when the connection with the server ends.
// Event 'error': trigerred when any exception related to the connection occurs.
WebSocket.prototype.on = function(event, fn) {
    this.callbacks[event] = fn;
}

////////////////////
// Send text or binary data to the server. 
WebSocket.prototype.send = function(data) {
    if (!this.socket.isOpen()) {
        throw new Error('not opened');
    }
    if (typeof data == 'string') {
        this.socket.sendText(data);
    }
    else {
        var JavaByteArray = Java.type("byte[]");
        javaData = new JavaByteArray(data.length);
        for (var i = 0; i < data.length; i++) {
            javaData[i] = data[i];
        }
        this.socket.sendBinary(javaData);
    }
}

////////////////////
// Close the current connection with the server.
WebSocket.prototype.close = function() {
    if (!this.socket.isOpen()) {
        throw new Error('not opened');
    }
    this.socket.close();
}

////////////////////
// Invoke a callback for the web socket instance and the triggered event.
module.exports.invokeCallback = function(obj, event, args) {
    if (obj.callbacks[event] != null) {
        obj.callbacks[event].apply(this, args);
    }
}

////////////////////
// Convert data fromat from binary array to string.
module.exports.binToStr = function(data) {
   var result = "";
  for (var i = 0; i < data.length; i++) {
    result += String.fromCharCode(data[i]);
  }
  return result;
}

////////////////////
// Create a server web socket with the given Java ServerWebSocket object.
module.exports.createServerWebSocket = function(serverWebSocket) {
    return new WebSocket("", serverWebSocket);
}

////////////////////
// Export this web socket server module.
module.exports.Server = Server;

////////////////////
// Construct an instance of WebSocketServer.
function Server(opts) {
    this.port = opts['port'];
    this.callbacks = {};

    var WebSocketHelper = Java.type('ptolemy.actor.lib.jjs.modules.webSocket.WebSocketHelper');
    this.socket = WebSocketHelper.createServer(actor.getEngine(), module.exports.namespaceName,
        this, this.port);
}

////////////////////
// Add callbacks to handle events on the web socket server.
// Usage: on('connection', function).
// This method Supports the following events.
// Event 'connection': triggered when a client web socket is connected to the listening port.
Server.prototype.on = function(event, fn) {
    this.callbacks[event] = fn;
}


