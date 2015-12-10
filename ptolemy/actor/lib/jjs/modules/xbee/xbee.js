/**
 * Module supporting XBee radios.
 *
 * @module xbee
 * @authors: Edward A. Lee
 */

var XBeeHelper = Java.type('ptolemy.actor.lib.jjs.modules.xbee.XBeeHelper');
var CommPortIdentifier = Java.type('gnu.io.CommPortIdentifier');
var EventEmitter = require('events').EventEmitter;

///////////////////////////////////////////////////////////////////////////////
//// hostSerialPorts

/** Return an array of serial port names or null if none are found.
 *  @return An array of port names.
 */
exports.hostSerialPorts = function() {
    var enumeration = CommPortIdentifier.getPortIdentifiers();
    var result = [];
    while (enumeration.hasMoreElements()) {
        var identifier = enumeration.nextElement();
        if (identifier.getPortType() == CommPortIdentifier.PORT_SERIAL) {
            var value = identifier.getName();
            result.push(value);
        }
    }
    return result;
};

///////////////////////////////////////////////////////////////////////////////
//// XBee

/** Construct an XBee object, initialize it with the specified options.
 *  It is an event emitter that emits the following events:
 *  * 'data': Emitted when new data arrives on the radio.
 *
 *  @param portName The name of the port to open.
 *  @param ownerName The name of the owner assigned to this port, if opening is successful.
 *  @param timeout Time in milliseconds before failing.
 *  @param options Serial port options (FIXME: define).
 *  @return A serial port interface.
 *  @throws If the port is in use or initializing the port fails.
 */
exports.XBee = function(portName, ownerName, timeout, options) {
    this.helper = new XBeeHelper(this, portName, ownerName, timeout, options);
}

util.inherits(exports.XBee, EventEmitter);

/** Close the port.
 */
exports.XBee.prototype.close = function() {
    this.helper.close();
};

/** Send data over the radio.
 *  @param data The data to send.
 */
exports.XBee.prototype.send = function(data) {
    if (Array.isArray(data)) {
        data = Java.to(data);
    }
    this.helper.send(data);
}
