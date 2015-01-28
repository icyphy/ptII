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
// Create HttpClient.
exports.VertxHttpClient = function(options) {
    this.port = options['port'] || 8080;
    this.host = options['host'] || "localhost";
    this.eventbus = VertxBusHelper.getHttpClient(actor.getEngine(),
        this, this.host, this.port);
}
util.inherits(exports.VertxHttpClient, EventEmitter);

////////////////////
// Construct a VertxBusServer that hosts a VertxBus
exports.VertxBusServer = function(options) {
    this.port = options['port'] || 8080;
    this.server = VertxBusHelper.getEventBusServer(actor.getEngine(),
        this, this.port);
}
util.inherits(exports.VertxBusServer, EventEmitter);

////////////////////
// Construct a VertxHttpServer
exports.VertxHttpServer = function(options) {
    this.port = options['port'] || 8080;
    this.server = VertxBusHelper.getHttpServer(actor.getEngine(),
        this, this.port);
}
util.inherits(exports.VertxHttpServer, EventEmitter);

////////////////////
// Send text or binary data to the server. 
exports.VertxBus.prototype.publish = function(address, data) {
    if (!this.eventbus.isOpen()) {
        throw new Error('not opened');
    }
    this.eventbus.publish(address, data);
}

////////////////////
// Register handler at an address. 
exports.VertxBus.prototype.registerHandler = function(data) {
    if (!this.eventbus.isOpen()) {
        throw new Error('not opened');
    }
    this.eventbus.registerHandler(data);
}

////////////////////
// Close the eventbus connection.
exports.VertxBus.prototype.close = function() {
    if (!this.eventbus.isOpen()) {
        throw new Error('not opened');
    }
    this.eventbus.close();
}

////////////////////
// Close the vertx bus server.
exports.VertxBusServer.prototype.closeServer = function() {
    if (this.server != null) {
        this.server.closeServer();
    }
}

////////////////////
// Close the http server.
exports.VertxHttpServer.prototype.closeServer = function() {
    if (this.server != null) {
        this.server.closeServer();
    }
}

////////////////////
// Send http response.
exports.VertxHttpServer.prototype.sendHttpResponse = function(data, value) {
    if (this.server != null) {
        this.server.sendHttpResponse(data, value);
    }
}

