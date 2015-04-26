// Module supporting vert.x bus.
// Authors: Patricia Derler
// Copyright: http://terraswarm.org/accessors/copyright.txt
//
////////////////////

var VertxHelper = Java.type('ptolemy.actor.lib.jjs.modules.vertxEventBus.VertxHelper');

var EventEmitter = require('events').EventEmitter;


////////////////////
// Construct an instance of VertxBus.
exports.VertxBus = function(options) {
    this.port = options['port'] || 8080;
    this.host = options['host'] || "localhost";
    this.eventbus = VertxHelper.getEventBus(this, this.host, this.port);
};
util.inherits(exports.VertxBus, EventEmitter);

////////////////////
// Construct a VertxBusServer that hosts a VertxBus.
// The returned object will have a 'port' field, a
// 'server' field, and a set of functions defined by
// the prototype below.
exports.VertxBusServer = function(options) {
    this.port = options['port'] || 8080;
    this.server = VertxHelper.getEventBusServer(this, this.port);
};
util.inherits(exports.VertxBusServer, EventEmitter);

////////////////////
// Send text or binary data to the server. 
exports.VertxBus.prototype.publish = function(address, data) {
    if (!this.eventbus.isOpen()) {
        throw new Error('not opened');
    }
    this.eventbus.publish(address, data);
};

////////////////////
// Register handler at an address. 
exports.VertxBus.prototype.registerAddressListener = function(data, onReceivedFunction) {
    if (!this.eventbus.isOpen()) {
        throw new Error('not opened');
    }
    this.eventbus.registerHandler(data);
    this.on('received', onReceivedFunction);
};

////////////////////
// Close the eventbus connection.
exports.VertxBus.prototype.close = function() {
    if (!this.eventbus.isOpen()) {
        throw new Error('not opened');
    }
    this.eventbus.close();
};

////////////////////
//Establish a connection to the eventbus.  
//This is attempted automatically upon creation.
//If connecting fails (e.g. server is not up yet), can retry with this method.
exports.VertxBus.prototype.connect = function() {
	this.eventbus.connect();
};

////////////////////
// Close the vertx bus server.
exports.VertxBusServer.prototype.closeServer = function() {
    if (this.server != null) {
        this.server.closeServer();
    }
};

////////////////////
// Send http response.
exports.VertxBusServer.prototype.sendHttpResponse = function(data, value) {
    if (this.server != null) {
        this.server.sendHttpResponse(data, value);
    }
};

