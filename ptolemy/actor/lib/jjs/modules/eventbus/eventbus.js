// Module supporting publishing and subscribing on the Vert.x event bus.
// Authors: Patricia Derler and Edward A. Lee
// Copyright: http://terraswarm.org/accessors/copyright.txt
//
// This module is used by the accessors VertxPublish and VertxSubscribe
// at http://terraswarm.org/accessors.  See those accessors for the usage pattern.

////////////////////

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
 *  To unsubscribe, use
 *  <pre>
 *     bus.removeListener(address, function);
 *  </pre>
 *  where the function is the same function passed to on().
 *  To unsubscribe all listeners to the address, use
 *  <pre>
 *     bus.unsubscribe(address);
 *  </pre>
 *  To unsubscribe to all addresses, use
 *  <pre>
 *     bus.unsubscribe();
 *  </pre>
 *  @constructor
 *  @param options A JSON record containing optional fields 'port' (an int)
 *   and 'host' (a string). These specify the network interface on the local host
 *   to use to connect to the Vert.x event bus cluster. This defaults to
 *   {'host':'localhost', 'port':0}, where a port value of 0 means "find
 *   an open port and use that. If no options parameter is given, then use
 *   the defaults.
 */
function VertxBus(options) {
    this.port = 0; // 0 specifies to find an open port.
    this.host = 'localhost';
    if (options) {
        this.port = options['port'] || 0;
        this.host = options['host'] || 'localhost';
    }
    this.helper = new EventBusHelper(this, this.port, this.host);
};
util.inherits(VertxBus, events.EventEmitter);

/** Notify this object of a received message from the event bus.
 *  @param address The address.
 *  @param body The message body
 */
VertxBus.prototype.notify = function(address, body) {
    try {
        var converted = JSON.parse(body);
        this.emit(address, converted);
    } catch (exception) {
        throw('Failed to parse JSON: ' + body + '\nException: ' + exception);
    }
};

/** Publish the specified data on the specified address.
 *  The data is first converted to a string representation in JSON format.
 *  @param address The address (or topic) of the event bus channel.
 *   This is a string.
 *  @param data The data to publish. This can be any JavaScript object
 *   that has a JSON representation using JSON.stringify().
 */
VertxBus.prototype.publish = function(address, data) {
    if (typeof(data) != 'string') {
        data = JSON.stringify(data);
    }
    this.helper.publish(address, data);
};

/** Subscribe to events with the specified address.
 *  To react to those events, use on() or once() as explained above.
 */
VertxBus.prototype.subscribe = function(address) {
    this.helper.subscribe(address);
};

/** Unsubscribe to events with the specified address.
 */
VertxBus.prototype.unsubscribe = function(address) {
    if (address) {
        this.helper.unsubscribe(address);
        this.removeAllListeners(address);
    } else {
        this.helper.unsubscribe(null);
        this.removeAllListeners();
    }
};

exports.VertxBus = VertxBus;