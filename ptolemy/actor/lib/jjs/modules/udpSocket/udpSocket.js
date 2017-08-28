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
 * Module supporting UDP (datagram) sockets.
 * @module udpSocket
 * @author Hokeun Kim and Edward A. Lee
 * @version $$Id$$
 */

// Stop extra messages from jslint.  Note that there should be no
// space between the / and the * and global.
/*globals Java, actor, exports, require, util */
/*jshint globalstrict: true */
"use strict";

var UDPSocketHelper = Java.type('ptolemy.actor.lib.jjs.modules.udpSocket.UDPSocketHelper');
var EventEmitter = require('events').EventEmitter;

///////////////////////////////////////////////////////////////////////////////
//// createSocket

/** Create a socket of the specified type.
 *  This returns an instance of the Socket class.
 *  @param type One of "udp4" or "udp6". Defaults to "udp4" if not given.
 *  @param callback Optional function to bind to "message" events.
 */
exports.createSocket = function (type, callback) {
    if (!type) {
        type = "udp4";
    }
    var socket = new exports.Socket(type);
    if (callback) {
        socket.on("message", callback);
    }
    return socket;
};

///////////////////////////////////////////////////////////////////////////////
//// supportedReceiveTypes

/** Return an array of the types supported by the current host for
 *  receiveType arguments.
 */
exports.supportedReceiveTypes = function () {
    return UDPSocketHelper.supportedReceiveTypes();
};

///////////////////////////////////////////////////////////////////////////////
//// supportedSendTypes

/** Return an array of the types supported by the current host for
 *  sendType arguments.
 */
exports.supportedSendTypes = function () {
    return UDPSocketHelper.supportedSendTypes();
};

///////////////////////////////////////////////////////////////////////////////
//// Socket

/** Construct an instance of a UDP (datagram) socket that can send or receive messages.
 *  To receive messages, call bind() on the returned object.
 *  To send messages, call send().
 *  The returned object is an event emitter that emits
 *  'listening', 'message', 'close', or 'error'.
 *  For example,
 *  <pre>
 *    var UDPSocket = require('@accessors-modules/udp-socket');
 *    var socket = UDPSocket.createSocket();
 *    socket.on('message', function(message) {
 *      print('Received from web socket: ' + message);
 *    });
 *    socket.bind(8084);
 *  </pre>
 *  This class is fashioned after the Socket class in Node's dgram module,
 *  with the only exception being that the messages it emits are not instances
 *  of Buffer, but rather appropriate data types as specified by the receiveType
 *  argument to setReceiveType(). Similarly, the data provided to send() will be
 *  converted to a Buffer according to the type set by setSendType(). It is also
 *  possible to deal with the data to send as raw bytes, if setRawBytes is called
 *  with value true.
 *
 *  @param type One of "udp4" or "udp6", which is ignored in Cape Code.
 */
exports.Socket = function (type) {
    // FIXME: type is ignored.
    var helper = UDPSocketHelper.getOrCreateHelper(actor, this);
    this.socket = helper.createSocket(this);
};

util.inherits(exports.Socket, EventEmitter);

/** Listen for datagram messages on the specified port and optional address.
 *  If no port is specified, then attempt to bind to a random port.
 *  If no address is specified, attempt to listen on all addresses.
 *  Once binding is complete, a 'listening' event is emitted and the
 *  optional callback function is called. The value of 'this' in the
 *  callback invocation will be this Socket object.
 *  @param port The port to listen on.
 *  @param address The network interface on which to listen.
 *  @param callback A function to call when the binding is complete.
 */
exports.Socket.prototype.bind = function (port, address, callback) {
    if (!address) {
        // FIXME: This assumes udp4?
        address = "0.0.0.0";
    }
    if (!callback) {
        callback = null;
    }
    this.socket.bind(port, address, callback);
};

/** Close the current connection with the server.
 *  If there is data that was passed to this.send() but has not yet
 *  been successfully sent (because the socket was not open),
 *  then throw an exception.
 */
exports.Socket.prototype.close = function () {
    this.socket.close();
};

/** Send a datagram message.
 *  @param data The data to send.
 *  @param port The destination port.
 *  @param hostname The name of the destination host (a hostname or IP address).
 *  @param callback An optional callback function to invoke when the send is complete,
 *   or if an error occurs. In the latter case, the cause of the error will be passed
 *   as an argument to the callback.
 */
exports.Socket.prototype.send = function (data, port, hostname, callback) {
    if (!callback) {
        callback = null;
    }
    this.socket.send(data, port, hostname, callback);
};

/** Set if the exchanged packets will be considered as raw bytes or not. 
 *  @param value Boolean set set or reset raw byte
 */
exports.Socket.prototype.setRawBytes = function (value) {
	this.socket.setRawBytes(value);
};

/** Set the receive type. If this is not called, the type defaults to "string".
 *  @param type The name of the receive type.
 */
exports.Socket.prototype.setReceiveType = function (type) {
    this.socket.setReceiveType(type);
};

/** Set the send type. If this is not called, the type defaults to "string".
 *  @param type The name of the send type.
 */
exports.Socket.prototype.setSendType = function (type) {
    this.socket.setSendType(type);
};
