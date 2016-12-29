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

/**
 * Module supporting serial port access.
 *
 * @module socket
 * @author Edward A. Lee, Rene Vivanco, and Christopher Brooks
 * @version $$Id$$
 */

// Stop extra messages from jslint.  Note that there should be no
// space between the / and the * and global.
/*globals exports, Java, require, util */
/*jshint globalstrict: true */
"use strict";

var SerialHelper = Java.type('ptolemy.actor.lib.jjs.modules.serial.SerialHelper');
var CommPortIdentifier = Java.type('gnu.io.CommPortIdentifier');
var EventEmitter = require('events').EventEmitter;

///////////////////////////////////////////////////////////////////////////////
//// hostSerialPorts

/** Return an array of serial port names or null if none are found.
 *  @return An array of port names.
 */
exports.hostSerialPorts = function () {
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
 *  * 'data': Emitted when new data arrives on the serial port.
 *
 *  @param portName The name of the port to open.
 *  @param ownerName The name of the owner assigned to this port, if opening is successful.
 *  @param timeout Time in milliseconds before failing.
 *  @param options Serial port options (FIXME: define).
 *  @return A serial port interface.
 *  @exception If the port is in use or initializing the port fails.
 */
exports.SerialPort = function (portName, ownerName, timeout, options) {
    this.helper = new SerialHelper(actor, this, portName, ownerName, timeout, options);
};

util.inherits(exports.SerialPort, EventEmitter);

/** Close the port.
 */
exports.SerialPort.prototype.close = function () {
    this.removeAllListeners();
    this.helper.close();
};

/** Open the port.
 */
exports.SerialPort.prototype.open = function () {
    this.helper.open();
};
