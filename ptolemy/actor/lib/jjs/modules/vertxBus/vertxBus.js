// Module supporting web sockets.
// Authors: Patricia Derler
// Copyright: http://terraswarm.org/accessors/copyright.txt
//
////////////////////
// Export this web socket module.
module.exports = VertxBus;

////////////////////
// The default name of the namespace
// FIXME this is a hack to enable callbacks from the JavaScript actor.
// There must be a better way to do this (e.g. EventEmitter in node.js)
module.exports.namespaceName = "VertxBus";

////////////////////
// Construct an instance of VertxBus.
function VertxBus(host, port) {
    this.host = host;
    this.port = port;
    this.callbacks = {};

    var VertxBusHelper = Java.type('ptolemy.actor.lib.jjs.modules.vertxBus.VertxBusHelper');
    this.socket = VertxBusHelper.getEventBus(actor.getEngine(), module.exports.namespaceName,
        this, this.host, this.port);
}

////////////////////
// Add callbacks to handle events.
// Usage: on('open', function).
// This method Supports the following events.
// Event 'open': triggered when the web socket is successfully connected to the server.
// Event 'message': triggered when a message arrives from the server.
// Event 'close': triggered when the connection with the server ends.
// Event 'error': trigerred when any exception related to the connection occurs.
VertxBus.prototype.on = function(event, fn) {
    this.callbacks[event] = fn;
}

////////////////////
// Send text or binary data to the server. 
VertxBus.prototype.publish = function(address, data) {
    //if (!this.socket.isOpen()) {
      //  throw new Error('not opened');
    //}
    //if (typeof data == 'string') {
        this.socket.publish(address, data);
    //}
}

VertxBus.prototype.registerHandler = function(data) {
    //if (!this.socket.isOpen()) {
      //  throw new Error('not opened');
    //}
    //if (typeof data == 'string') {
        this.socket.registerHandler(data);
    //}
}

////////////////////
// Close the current connection with the server.
VertxBus.prototype.close = function() {
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