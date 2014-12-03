// Module supporting the MQTT protocol.
// Authors: Hokeun Kim
// Copyright: http://terraswarm.org/accessors/copyright.txt
//

module.exports.createClient = function(port, host, opts)
{
    return new Client(port, host, opts);
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
var events = require('events');
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

    this.javaClient = new MqtttHelper(actor.getEngine(), this, port, host, opts['clientId']);

    this.connected = undefined;

    events.EventEmitter.call(this);
}
util.inherits(Client, events.EventEmitter);

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
// Start connection between the client and the broker server.
Client.prototype.start = function() {
    this.javaClient.start();
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
// Disconnect from the broker server and close (i.e. return all allocated resources of) the client.
Client.prototype.end = function() {
    this.javaClient.end();
}

