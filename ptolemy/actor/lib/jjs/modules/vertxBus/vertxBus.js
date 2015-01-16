// Module supporting vert.x bus.
// Authors: Patricia Derler
// Copyright: http://terraswarm.org/accessors/copyright.txt
//
////////////////////

var VertxBusHelper = Java.type('ptolemy.actor.lib.jjs.modules.vertxBus.VertxBusHelper');

var EventEmitter = require('events').EventEmitter;


////////////////////
// Construct an instance of VertxBus.
exports.VertxBus = function(options) {
    this.port = options['port'] || 8080;
    this.host = options['host'] || "localhost";
    this.eventbus = VertxBusHelper.getEventBus(actor.getEngine(),
        this, this.host, this.port);
}
util.inherits(exports.VertxBus, EventEmitter);

////////////////////
// Construct a VertxBusServer that hosts a VertxBus
exports.VertxBusServer = function(options) {
    this.port = options['port'] || 8080;
    this.server = VertxBusHelper.getEventBusServer(actor.getEngine(),
        this, this.port);
}
util.inherits(exports.VertxBusServer, EventEmitter);

////////////////////
// Send text or binary data to the server. 
exports.VertxBus.prototype.publish = function(address, data) {
    //if (!this.eventbus.isOpen()) {
      //  throw new Error('not opened');
    //}
    //if (typeof data == 'string') {
        this.eventbus.publish(address, data);
    //}
}

exports.VertxBus.prototype.registerHandler = function(data) {
    //if (!this.eventbus.isOpen()) {
      //  throw new Error('not opened');
    //}
    //if (typeof data == 'string') {
        this.eventbus.registerHandler(data);
    //}
}

////////////////////
// Close the current connection with the server.
exports.VertxBus.prototype.close = function() {
    if (!this.eventbus.isOpen()) {
        throw new Error('not opened');
    }
    this.eventbus.close();
}

exports.VertxBusServer.prototype.closeServer = function() {
    if (this.server != null) {
        this.server.closeServer();
    }
}

