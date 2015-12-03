/**
 * Module supporting serial port access.
 *
 * @module socket
 * @authors: Edward A. Lee, Rene Vivanco, and Christopher Brooks
 */

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
//// SerialPort

/** Construct a serial port object, initialize it with the specified options.
 *  It is an event emitter that emits the following events:
 *  * 'dataReceived': Emitted when new data arrives on the serial port.
 *
 *  @param portName The name of the port to open.
 *  @param ownerName The name of the owner assigned to this port, if opening is successful.
 *  @param timeout Time in milliseconds before failing.
 *  @param options Serial port options (FIXME: define).
 *  @return A serial port interface.
 *  @throws If the port is in use or initializing the port fails.
 */
exports.SerialPort = function(portName, ownerName, timeout, options) {
    var portID = CommPortIdentifier.getPortIdentifier(portName);
    this.interface = portID.open(ownerName, timeout);
    // FIXME: Set the options.
    this.emit('dataReceived', 'hello world');
}

util.inherits(exports.SerialPort, EventEmitter);

/** Close the port.
 */
exports.SerialPort.prototype.close = function() {
    if (this.interface) {
        this.interface.close();
    }
};

