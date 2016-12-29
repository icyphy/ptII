// Below is the copyright agreement for the Ptolemy II system.
//
// Copyright (c) 2014-2016 The Regents of the University of California.
// All rights reserved.
//
// Permission is hereby granted, without written agreement and without
// license or royalty fees, to use, copy, modify, and distribute this
// software and its documentation for any purpose, provided that the above
// copyright notice and the following two paragraphs appear in all copies
// of this software.
//
// IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
// FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE.
//
// THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
// INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
// ENHANCEMENTS, OR MODIFICATIONS.
//
//
// Ptolemy II includes the work of others, to see those copyrights, follow
// the copyright link on the splash page or see copyright.htm.
/**
 * Module supporting the MQTT protocol.
 * @module mqtt
 * @author Hokeun Kim
 * @version $$Id$$
 */

// Stop extra messages from jslint.  Note that there should be no
// space between the / and the * and global.
/*globals actor, Java, module, require, util */
/*jshint globalstrict: true */
"use strict";

var EventEmitter = require('events').EventEmitter;
var MqttHelper = Java.type('ptolemy.actor.lib.jjs.modules.mqtt.MqttHelper');

module.exports.createClient = function (port, host, options) {
    return new Client(port, host, options);
};

////////////////////
// Convert data fromat from binary array to string.
module.exports.byteArrayToString = function (data) {
    var result = "";
    for (var i = 0; i < data.length; i++) {
        result += String.fromCharCode(data[i]);
    }
    return result;
};

////////////////////
// Construct an instance of an MQTT client.
var events = require('events');

function Client(port, host, options) {
    if (typeof port != 'number') {
        throw "Invalid MQTT broker port";
    }
    if (typeof host != 'string') {
        throw "Invalid MQTT broker host";
    }
    if (options == null) {
        options = {};
    }

    if (!options.clientId) {
        options.clientId = MqttHelper.getDefaultId();
    }

    if (options.rawBytes == null) {
        options.rawBytes = false;
    }
    
    var helper = MqttHelper.getOrCreateHelper(actor, this);

    this.javaClient = new MqttHelper.MqttClientWrapper(helper, this, port, host, options.clientId, options.rawBytes);

    // When "use strict" was added, the following exception occurred because of this.connected = undefined.
    //
    // ptolemy.kernel.util.IllegalActionException: Failure executing the initialize function: TypeError: Cannot set property "connected" of [object Object] that has only a getter in /Users/cxh/ptII/ptolemy/actor/lib/jjs/external/require.js#209:26<eval> at line number 79
    //    in .MQTTPubSub.MqttSubscriber
    //Because:
    //TypeError: Cannot set property "connected" of [object Object] that has only a getter in /Users/cxh/ptII/ptolemy/actor/lib/jjs/external/require.js#209:26<eval> at line number 79
    //at ptolemy.actor.lib.jjs.JavaScript._invokeMethodInContext(JavaScript.java:1846)

    //this.connected = undefined;

    events.EventEmitter.call(this);
}
util.inherits(Client, events.EventEmitter);

////////////////////
// A property for whether the client is connected to a broker server.
Object.defineProperties(Client.prototype, {
    connected: {
        get: function () {
            return this.javaClient.isConnected();
        }
    }
});

////////////////////
// Subscribe a topic using the given maximum QoS level. Start getting messages on the topic.
Client.prototype.subscribe = function (topic, options) {
    if (!options) {
        options = {
            qos: 0
        };
    }

    var qos;
    if (options.qos) {
        qos = options.qos;
    } else {
        qos = 0;
    }

    this.javaClient.subscribe(topic, qos);
};

/** Start connection between the client and the broker server. */
Client.prototype.start = function () {
    this.javaClient.start();
};

/** Unsubscribe a topic. Stop getting messages on the topic. */
Client.prototype.unsubscribe = function (topic) {
    this.javaClient.unsubscribe(topic);
};

/** Publish an MQTT message to subscribers listening to the topic. */
Client.prototype.publish = function (topic, message, opts, callback) {
    if (!opts) {
        opts = {
            qos: 0,
            retain: false
        };
    }

    var qos;
    if (opts.qos) {
        qos = opts.qos;
    } else {
        qos = 0;
    }

    var retain;
    if (opts.retain) {
        retain = opts.retain;
    } else {
        retain = false;
    }

    this.javaClient.publish(topic, message, qos, retain);
};

/** Disconnect from the broker server and close (i.e. return all allocated resources of) the client. */
Client.prototype.end = function () {
    this.javaClient.end();
};
