// Module supporting web sockets.
// Authors: Hokeun Kim and Edward A. Lee
// Copyright: http://terraswarm.org/accessors/copyright.txt
//
////////////////////
// Export this web socket module.
module.exports = WebSocket;

////////////////////
// Construct an instance of WebSocket.
function WebSocket(url) {
    this.address = url;
    this.callbacks = {};

    var WebSocketHelper = Java.type('ptolemy.actor.lib.jjs.modules.webSocket.WebSocketHelper');
    this.socket = WebSocketHelper.create(actor.getEngine(), this.constructor.name,
        this.address, this);
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



