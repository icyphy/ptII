// Copyright (c) 2015-2016 The Regents of the University of California.
// All rights reserved.

// Permission is hereby granted, without written agreement and without
// license or royalty fees, to use, copy, modify, and distribute this
// software and its documentation for any purpose, provided that the above
// copyright notice and the following two paragraphs appear in all copies
// of this software.

// IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
// FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE.

// THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
// INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
// ENHANCEMENTS, OR MODIFICATIONS.

/**
 * Module supporting web sockets.
 * This module defines three classes, Client, Server, and Socket.
 * To make a connection, create an instance of Server, set up listeners,
 * and start the server. On another machine (or the same machine), create
 * an instance of Client and set up listeners and/or invoke this.send() to send
 * a message. When a client connects to the Server, the Server will create
 * an instance of the Socket object.
 *
 * This module also provides two utility functions that return arrays
 * of MIME types supported for sending or receiving messages.
 *
 * @module webSocket
 * @author Hokeun Kim and Edward A. Lee
 */

// Stop extra messages from jslint.  Note that there should be no
// space between the / and the * and global.
/*globals exports, Java, require, util */
/*jshint globalstrict: true */
"use strict";

var WebSocketHelper = Java.type('ptolemy.actor.lib.jjs.modules.webSocket.WebSocketHelper');
var WebSocketServerHelper = Java.type('ptolemy.actor.lib.jjs.modules.webSocket.WebSocketServerHelper');
var EventEmitter = require('events').EventEmitter;

///////////////////////////////////////////////////////////////////////////////
//// supportedReceiveTypes

/** Return an array of the types supported by the current host for
 *  receiveType arguments.
 */
exports.supportedReceiveTypes = function () {
    return WebSocketHelper.supportedReceiveTypes();
};

///////////////////////////////////////////////////////////////////////////////
//// supportedSendTypes

/** Return an array of the types supported by the current host for
 *  sendType arguments.
 */
exports.supportedSendTypes = function () {
    return WebSocketHelper.supportedSendTypes();
};

///////////////////////////////////////////////////////////////////////////////
//// Client

/** Construct an instance of a socket client that can send or receive messages
 *  to a server at the specified host and port.
 *  The returned object subclasses EventEmitter.
 *  You can register handlers for events 'open', 'message', 'close', or 'error'.
 *  The event 'open' will be emitted when the socket has been successfully opened.
 *  The event 'message' will be emitted with the body of the message as an
 *  argument when an incoming message arrives on the socket.
 *  You can invoke the send() function to send data to the server.
 *
 *  The type of data sent and received can be specified with the 'sendType'
 *  and 'receiveType' options.
 *  In principle, any MIME type can be specified, but the host may support only
 *  a subset of MIME types.  The client and the server have to agree on the type,
 *  or the data will not get through correctly.
 *
 *  The default type for both sending and receiving
 *  is 'application/json'. The types supported by this implementation
 *  include at least:
 *  * __application/json__: The this.send() function uses JSON.stringify() and sends the
 *    result with a UTF-8 encoding. An incoming byte stream will be parsed as JSON,
 *    and if the parsing fails, will be provided as a string interpretation of the byte
 *    stream.
 *  * __text/\*__: Any text type is sent as a string encoded in UTF-8.
 *  * __image/x__: Where __x__ is one of __json__, __png__, __gif__,
 *    and more (FIXME: which, exactly?).
 *    In this case, the data passed to this.send() is assumed to be an image, as encoded
 *    on the host, and the image will be encoded as a byte stream in the specified
 *    format before sending.  A received byte stream will be decoded as an image,
 *    if possible. FIXME: What happens if decoding fails?
 *  
 *  The event 'close' will be emitted when the socket is closed, and 'error' if an
 *  an error occurs (with an error message as an argument).
 *  For example,
 *  
 *      var WebSocket = require('webSocket');
 *      var client = new WebSocket.Client({'host': 'localhost', 'port': 8080});
 *      client.send({'foo': 'bar'});
 *      client.on('message', onMessage);
 *      function onMessage(message) {
 *          print('Received from web socket: ' + message);
 *      }
 *      client.open();
 *  
 *  The above code may send a message even before the socket is opened. This module
 *  implementation will queue that message to be sent later when the socket is opened.
 *  
 *  The options argument is a JSON object that can contain the following fields:
 *  * host: The IP address or host name for the host. Defaults to 'localhost'.
 *  * port: The port on which the host is listening. Defaults to 80.
 *  * receiveType: The MIME type for incoming messages, which defaults to 'application/json'.
 *  * sendType: The MIME type for outgoing messages, which defaults to 'application/json'.
 *  * connectTimeout: The time to wait before giving up on a connection, in milliseconds
 *    (defaults to 1000).
 *  * numberOfRetries: The number of times to retry connecting. Defaults to 10.
 *  * timeBetweenRetries: The time between retries, in milliseconds. Defaults to 500.
 *  * discardMessagesBeforeOpen: If true, discard messages before the socket is open. Defaults to false.
 *  * throttleFactor: The number milliseconds to stall for each item that is queued waiting to be sent. Defaults to 0.
 *
 *  @param options The options.
 */
exports.Client = function (options) {
    options = options || {};
    this.port = options.port || 80;
    this.host = options.host || 'localhost';
    this.receiveType = options.receiveType || 'application/json';
    this.sendType = options.sendType || 'application/json';
    this.connectTimeout = options.connectTimeout || 1000;
    this.numberOfRetries = options.numberOfRetries || 10;
    this.timeBetweenRetries = options.timeBetweenRetries || 500;
    this.discardMessagesBeforeOpen = options.discardMessagesBeforeOpen || false;
    this.throttleFactor = options.throttleFactor || 0;
    this.helper = WebSocketHelper.createClientSocket(
        this,
        this.host,
        this.port,
        this.receiveType,
        this.sendType,
        this.connectTimeout,
        this.numberOfRetries,
        this.timeBetweenRetries,
        this.discardMessagesBeforeOpen,
        this.throttleFactor);
};
util.inherits(exports.Client, EventEmitter);

/** Open the socket connection. Call this after setting up event handlers. */
exports.Client.prototype.open = function () {
    this.helper.open();
}

/** Send data over the web socket.
 *  If the socket has not yet been successfully opened, then queue
 *  data to be sent later, when the socket is opened.
 *  @param data The data to send.
 */
exports.Client.prototype.send = function (data) {
    if (this.sendType == 'application/json') {
        this.helper.send(JSON.stringify(data));
    } else if (this.sendType.search(/text\//) === 0) {
        this.helper.send(data.toString());
    } else {
        this.helper.send(data);
    }
};

/** Close the current connection with the server.
 *  If there is data that was passed to this.send() but has not yet
 *  been successfully sent (because the socket was not open),
 *  then throw an exception.
 */
exports.Client.prototype.close = function () {
    this.helper.close();
};

/** Notify this object of a received message from the socket.
 *  This function attempts to interpret the message according to the
 *  receiveType, and emits a "message" event with the message as an argument.
 *  For example, with the default receiveType of 'application/json', it will
 *  use JSON.parse() to parse the message and emit the result of the parse.
 *  This function is called by the Java helper used by this particular
 *  implementation and should not be normally called by the user.
 *  @param message The incoming message.
 */
exports.Client.prototype._notifyIncoming = function (message) {
    if (this.receiveType == 'application/json') {
        try {
            message = JSON.parse(message);
        } catch (error) {
            this.emit('error', error);
            return;
        }
    }
    // Assume the helper has already provided the correct type.
    this.emit("message", message);
};

///////////////////////////////////////////////////////////////////////////////
//// Server

/** Construct an instance of WebSocket Server.
 *  After invoking this constructor (using new), the user script should set up listeners
 *  and then invoke the start() function on this Server.
 *  This will create an HTTP server on the local host.
 *  The options argument is a JSON object containing the following optional fields:
 *  * hostInterface: The IP address or name of the local interface for the server
 *    to listen on.  This defaults to "localhost", but if the host machine has more
 *    than one network interface, e.g. an Ethernet and WiFi interface, then you may
 *    need to specifically specify the IP address of that interface here.
 *  * port: The port on which to listen for connections (the default is 80,
 *    which is the default HTTP port).
 *  * receiveType: The MIME type for incoming messages, which defaults to 'application/json'.
 *    See the Client documentation for supported types.
 *  * sendType: The MIME type for outgoing messages, which defaults to 'application/json'.
 *    See the Client documentation for supported types.
 * 
 *  This subclasses EventEmitter, emitting events 'listening' and 'connection'.
 *  A typical usage pattern looks like this:
 * 
 *     var server = new WebSocket.Server({'port':8082});
 *     server.on('listening', onListening);
 *     server.on('connection', onConnection);
 *     server.start();
 * 
 *  where onListening is a handler for an event that this Server emits
 *  when it is listening for connections, and onConnection is a handler
 *  for an event that this Server emits when a client requests a websocket
 *  connection and the socket has been successfully established.
 *  When the 'connection' event is emitted, it will be passed a Socket object,
 *  and the onConnection handler can register a listener for 'message' events
 *  on that Socket object, as follows:
 * 
 *     server.on('connection', function(socket) {
 *        socket.on('message', function(message) {
 *            console.log(message);
 *            socket.send('Reply message');
 *        });
 *     });
 * 
 *  The Socket object also has a close() function that allows the server to close
 *  the connection.
 * 
 *  FIXME: Should provide a mechanism to validate the "Origin" header during the
 *    connection establishment process on the serverside (against the expected origins)
 *    to avoid Cross-Site WebSocket Hijacking attacks.
 *
 *  @param options The options.
 */
exports.Server = function (options) {
	if (typeof options.port === 'undefined' || options.port === null) {
		this.port = 80;
	} else {
		this.port = options.port;
	}
    this.hostInterface = options.hostInterface || 'localhost';
    this.receiveType = options.receiveType || 'application/json';
    this.sendType = options.sendType || 'application/json';
    this.helper = WebSocketServerHelper.createServer(
        this, this.hostInterface, this.port, this.receiveType, this.sendType
    );
};
util.inherits(exports.Server, EventEmitter);

/** Start the server. */
exports.Server.prototype.start = function () {
    this.helper.startServer();
};

/** Stop the server. Note that this closing happens
 *  asynchronously. The server may not be closed when this returns.
 */
exports.Server.prototype.stop = function () {
    this.helper.closeServer();
};

/** Notify that a handshake was successful and a websocket has been created.
 *  This is called by the helper class is not meant to be called by the JavaScript
 *  programmer. When this is called, the Server will create a new Socket object
 *  and emit a 'connection' event with that Socket as an argument.
 *  The 'connection' handler can then register for 'message' events from the
 *  Socket or issue replies to the Socket using this.send(). It can also close() the
 *  Socket.
 *  @param serverWebSocket The Java ServerWebSocket object.
 *  @param helper The helper in charge of this socket.
 */
exports.Server.prototype._socketCreated = function (serverWebSocket, helper) {
    var socket = new exports.Socket(
        serverWebSocket, helper, this.receiveType, this.sendType);
    this.emit('connection', socket);
};

/////////////////////////////////////////////////////////////////
//// Socket

/** Construct (using new) a Socket object for the server side of a new connection.
 *  This is called by the socketCreated function above whenever a new connection is
 *  established at the request of a client. It should not normally be called by
 *  the JavaScript programmer. The returned Socket is an event emitter that emits
 *  'message' events.
 *  @param serverWebSocket The Java ServerWebSocket object.
 *  @param helper The helper in charge of this web socket.
 *  @param receiveType The MIME type for incoming messages, which defaults to 'application/json'.
 *  @param sendType The MIME type for outgoing messages, which defaults to 'application/json'.
 */
exports.Socket = function (serverWebSocket, helper, receiveType, sendType) {
    this.helper = WebSocketHelper.createServerSocket(
        this, serverWebSocket, helper, receiveType, sendType);
    this.receiveType = receiveType;
    this.sendType = sendType;
};
util.inherits(exports.Socket, EventEmitter);

/** Close the socket. Normally, this would be called on the client side,
 *  not on the server side. But the server can also close the connection.
 */
exports.Socket.prototype.close = function () {
    this.helper.close();
};

/** Return true if the socket is open.
 */
exports.Socket.prototype.isOpen = function () {
    return this.helper.isOpen();
};

/** Notify this object of a received message from the socket.
 *  This function attempts to parse the message as JSON and then
 *  emits a "message" event with the message as an argument.
 *  This function is called by the helper and should not be called
 *  by the user of this module.
 *  @param message The incoming message.
 */
exports.Socket.prototype._notifyIncoming = function (message) {
    if (this.receiveType == 'application/json') {
        try {
            message = JSON.parse(message);
        } catch (error) {
            this.emit('error', error);
            return;
        }
    }
    // Assume the helper has already provided the correct type.
    this.emit("message", message);
};

/** Send data over the web socket.
 *  The data can be anything that has a JSON representation.
 *  @param data The data to send.
 */
exports.Socket.prototype.send = function (data) {
    if (this.sendType == 'application/json') {
        this.helper.send(JSON.stringify(data));
    } else if (this.sendType.search(/text\//) === 0) {
        this.helper.send(data.toString());
    } else {
        this.helper.send(data);
    }
};
