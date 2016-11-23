// Below is the copyright agreement for the Ptolemy II system.
//
// Copyright (c) 2015-2016 The Regents of the University of California.
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
 * Module supporting publishing and subscribing on the Vert.x event bus.
 * This module provides an interface to the Vert.x event bus, which
 * supports a peer-to-peer publish-and-subscribe network on a local
 * area network. Upon invoking the VertxBus constructor in this module,
 * the host running this module participates in the pub-sub network.
 * The pub-sub network extends as far as multicast packets extend
 * in the local-area network, and other participants will be automatically
 * discovered. Hence, you can publish events on the local network or
 * subscribe to events on the local network using this module.
 * <p>
 * Events are published with an address, which is an arbitrary string
 * that identifies the event or stream of events. This address could
 * indicate the topic of message. A network or service using this pub-sub
 * mechanism should develop a convention for these addresses to minimize
 * accidental name collisions. For example, a sensor might publish
 * sensor data using an address like 'org.terraswarm.sensor.accelerometer.onShoe'.
 * <p>
 * The published data can be any JavaScript object that has a string
 * JSON representation (using JSON.stringify()).
 * <p>
 * If the host has more than one network interface (e.g. WiFi and wired),
 * then the one to use can be specified by name as an optional argument
 * to the constructor. You can also specify the port to use, though if
 * you do, then you are responsible for avoiding port collisions.
 * The default network interface used is whichever one is represented by
 * 'localhost'. The default port is an arbitrary unused port.
 *
 * @module eventbus
 * @author Patricia Derler and Edward A. Lee
 * @version $$Id$$
 */

// Stop extra messages from jslint.  Note that there should be no
// space between the / and the * and global.
/*globals Java, actor, exports, require, util */
/*jshint globalstrict: true*/
"use strict";

var EventBusHelper = Java.type('ptolemy.actor.lib.jjs.modules.eventbus.EventBusHelper');
var events = require('events');

/** Construct an interface to the Vert.x bus. Use this as follows:
 *  <pre>
 *     var eventbus = require('eventbus');
 *     var bus = new eventbus.VertxBus();
 *     bus.subscribe('topic');
 *     bus.on('topic',
 *      function(msg) {
 *        print(msg);
 *      }
 *     );
 *     bus.publish('topic', {'hello':'world'});
 *  </pre>
 *  This creates an interface to the event bus, subscribes to events
 *  with address 'topic', provides a handler for such events,
 *  and publishes a single event to that same address.
 *  The result should be to print:
 *  <pre>
 *    {'hello':'world'}
 *  </pre>
 *  on the standard output.
 *  <p>
 *  This implementation uses the event emitter pattern common in JavaScript.
 *  Once you have subscribed to an address, you can specify any number of
 *  handlers as follows:
 *  <pre>
 *     bus.on(address, function);
 *  </pre>
 *  To give a handler that reacts only to exactly one event with this address, use
 *  <pre>
 *     bus.once(address, function);
 *  </pre>
 *  To unsubscribe to an address, use
 *  <pre>
 *     bus.unsubscribe(address);
 *  </pre>
 *  To unsubscribe to all addresses, use
 *  <pre>
 *     bus.unsubscribe();
 *  </pre>
 *  In addition, this module supports point-to-point communication, which sends an event
 *  to exactly one subscriber, chosen in a approximately round-robin fashion. To send
 *  to exactly one subscriber, instead of '''publish''' use '''send''', as follows:
 *  <pre>
 *     bus.send('topic', {'hello':'world'});
 *  </pre>
 *  When sending a point-to-point message, it is possible to get a reply from the
 *  recipient.  The recipient (which also uses this module) should set the reply message
 *  as in the following example:
 *  <pre>
 *     bus.setReply('confirmed');
 *  </pre>
 *  where 'confirmed' can be replaced with any string or value that has a JSON string representation.
 *  The sender can then specify a handler to receive the reply as follows:
 *  <pre>
 *     bus.send('topic', {'hello':'world'}, handler);
 *  </pre>
 *  where handler is a function that takes one argument, the reply message.
 *  @constructor
 *  @param options A JSON record containing optional fields 'port' (an int)
 *   and 'host' (a string). These specify the network interface on the local host
 *   to use to connect to the Vert.x event bus cluster. This defaults to
 *   \{'host':'localhost', 'port':0\}, where a port value of 0 means "find
 *   an open port and use that. If no options parameter is given, then use
 *   the defaults.
 */
function VertxBus(options) {
    this.port = 0; // 0 specifies to find an open port.
    this.host = 'localhost';
    if (options) {
        this.port = options.port || 0;
        this.host = options.host || 'localhost';
    }
    this.helper = new EventBusHelper(actor, this, this.port, this.host);
}
util.inherits(VertxBus, events.EventEmitter);

/** Notify this object of a received message from the event bus.
 *  This function is called from the Nashorn Java helper for this module and
 *  should not be directly invoked by the user of the module.
 *  This method assumes that the body of the message is a string
 *  in JSON format. If it is not, then it will just emit the body as is.
 *  @param address The address.
 *  @param body The message body
 */
VertxBus.prototype.notify = function (address, body) {
    try {
        body = JSON.parse(body);
    } catch (exception) {
        // NOTE: Just emit this and interpret a string.
        // throw 'Failed to parse JSON: ' + body + '\nException: ' + exception;
    }
    this.emit(address, body);
};

/** Notify this object of a received reply from the event bus
 *  This function is called from the Nashorn Java helper for this module and
 *  should not be directly invoked by the user of the module.
 *  confirming completion of a point-to-point send.
 *  @param handler The callback function to invoke.
 *  @param message The message to send to the callback function.
 */
VertxBus.prototype.notifyReply = function (handler, message) {
    try {
        message = JSON.parse(message);
    } catch (exception) {
        // Assume that the message is a string.
        // We can ignore the exception, because the message
        // will be passed as a string.
    }
    handler.apply(this, [message]);
};

/** Publish the specified data on the specified address.
 *  The data is first converted to a string representation in JSON format.
 *  @param address The address (or topic) of the event bus channel.
 *   This is a string.
 *  @param data The data to publish. This can be any JavaScript object
 *   that has a JSON representation using JSON.stringify().
 *  @see this.send()
 */
VertxBus.prototype.publish = function (address, data) {
    if (typeof (data) != 'string') {
        data = JSON.stringify(data);
    }
    this.helper.publish(address, data);
};

/** Send the specified data to exactly one receiver at the specified address.
 *  This implements a point-to-point send, vs. the broadcast realized by publish().
 *  The data is first converted to a string representation in JSON format.
 *  According to the Vert.x documentation, the recipient will be chosen in a
 *  loosely round robin fashion.
 *  @param address The address (or topic) of the event bus channel.
 *   This is a string.
 *  @param data The data to publish. This can be a string or any JavaScript object
 *   that has a JSON representation using JSON.stringify().
 *  @param handler A function to invoke with argument address and reply body
 *   when the recipient has received the message, or null to not provide a reply handler.
 *  @see publish()
 */
VertxBus.prototype.send = function (address, data, handler) {
    if (typeof (data) != 'string') {
        data = JSON.stringify(data);
    }
    if (handler === null) {
        this.helper.send(address, data);
    } else {
        this.helper.send(address, data, handler);
    }
};

/** Set the reply to send when events are received in the future via a
 *  point-to-point send.
 *  @param reply The reply to respond with, or null to send no reply.
 *   this should be a string or any object that can be encoded as a
 *   JSON string.
 *  @see this.send(address, data)
 */
VertxBus.prototype.setReply = function (reply) {
    if (typeof (reply) != 'string') {
        reply = JSON.stringify(reply);
    }
    this.helper.setReply(reply);
};

/** Subscribe to events with the specified address.
 *  To react to those events, use on() or once() as explained above.
 */
VertxBus.prototype.subscribe = function (address) {
    this.helper.subscribe(address);
};

/** Unsubscribe to events with the specified address.
 */
VertxBus.prototype.unsubscribe = function (address) {
    if (address) {
        this.helper.unsubscribe(address);
        this.removeAllListeners(address);
    } else {
        this.helper.unsubscribe(null);
        this.removeAllListeners();
    }
};

exports.VertxBus = VertxBus;
