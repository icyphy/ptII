// Module supporting the MQTT protocol.
// Authors: Hokeun Kim
// Copyright: http://terraswarm.org/accessors/copyright.txt
//
////////////////////
// The default name of the namespace
// FIXME this is a hack to enable callbacks from the JavaScript actor.
// There must be a better way to do this (e.g. EventEmitter in node.js)
module.exports.namespaceName = "Mqtt";

module.exports.createClient = function(port, host, opts)
{
    return new Client(port, host, opts);
}

////////////////////
// Invoke a callback for the MQTT client instance and the triggered event.
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
// Construct an instance of an MQTT client.
function Client(port, host, opts) {
    if (typeof port != 'number') {
        opts = host;
        host = port;
        port = 1883;
    }
    if (typeof host != 'string') {
        opts = host;
        host = 'localhost';
    }
    if (!opts) {
        opts ={};
    }

    var MqtttHelper = Java.type('ptolemy.actor.lib.jjs.modules.mqtt.MqttHelper');
    if (!opts['clientId']) {
        opts['clientId'] = MqtttHelper.getDefaultId();
    }
    this.callbacks = {};

    this.javaClient = new MqtttHelper(actor.getEngine(), module.exports.namespaceName,
        this, port, host, opts['clientId']);

    this.connected = undefined;
}

////////////////////
// A property for whether the client is connected to a broker server.
Object.defineProperties(Client.prototype, {
    connected: {
        get: function() { return this.javaClient.isConnected(); }
    }
});

////////////////////
// Subscribe a topic using the given maximum QoS level. Start getting messages on the topic.
Client.prototype.subscribe = function(topic, opts) {
    if(!opts) {
        opts = {qos: 0};
    }

    var qos;
    if (opts['qos']) {
        qos = opts['qos'];
    }
    else {
        qos = 0;
    }

    this.javaClient.subscribe(topic, qos);
}

////////////////////
// Unsubscribe a topic. Stop getting messages on the topic.
Client.prototype.unsubscribe = function(topic) {
    this.javaClient.unsubscribe(topic);
}

////////////////////
// Publish an MQTT message to subscribers listening to the topic.
Client.prototype.publish = function(topic, message, opts, callback) {
    if(!opts) {
        opts = {qos: 0, retain: false};
    }

    var qos;
    if (opts['qos']) {
        qos = opts['qos'];
    }
    else {
        qos = 0;
    }

    var retain;
    if (opts['retain']) {
        retain = opts['retain'];
    }
    else {
        retain = false;
    }

    this.javaClient.publish(topic, message, qos, retain);
}

////////////////////
// Disconnect from the broker server and close (i.e. return all allocated resources) the client.
Client.prototype.end = function() {
    this.javaClient.end();
}

////////////////////
// Add callbacks to handle events.
// Usage: on('connect', function).
// This method Supports the following events.
// Event 'connect', function(): triggered when the client is connected to the broker server.
// Event 'message', function(topic, message): triggered when a message arrives from the publisher. 
// Event 'close', function(): triggered when the connection with the broker server is closed.
// Event 'error', function(err): triggered when the connection with the broker server is refused.
// Event 'published', function (): triggered when the pubhlish complets (for QoS >= 1);
Client.prototype.on = function(event, fn) {
    this.callbacks[event] = fn;
}
