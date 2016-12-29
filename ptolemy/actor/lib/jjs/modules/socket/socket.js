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

/**
 * Module supporting TCP sockets.
 * This module defines three classes, SocketClient, SocketServer, and Socket.
 *
 * To establish a connection, create an instance of SocketServer and listen for
 * connection events. When a connection request comes in, the listener will be
 * passed an instance of Socket. The server can send data through that instance
 * and listen for incoming data events.
 *
 * On another machine (or the same machine), create an instance of SocketClient.
 * When the connection is established to the server, this instance will emit an
 * 'open' event. When data arrives from the server, it will emit a 'data' event.
 * You can invoke this.send() to send data to the server.
 *
 * The this.send() function can accept data in many different forms.
 * You can send a string, an image, a number, or an array of numbers.
 * Two utility functions supportedReceiveTypes() and supportedSendTypes()
 * tell you exactly which data types supported by the host.
 * Arrays of numeric types are also supported.
 *
 * If the rawBytes option is true (the default), then data is sent without any
 * message framing. As a consequence, the recipient of the data may emit only a
 * portion of any sent data, or it may even coalesce data provided in separate
 * invocations of this.send(). If rawBytes is false, then messages will be framed so
 * that each invocation of this.send() results in exactly one data item emitted at the
 * other end.  This will only work if both sides of the connection implement the
 * same framing protocol, e.g. if they both are implemented with this same module.
 * To communicate with external tools that do not support this message framing
 * protocol, leave rawBytes set to true.
 *
 * The message framing protocol used here is very simple. Each message is preceded
 * by one byte indicating the length of the message. If the message has length
 * greater than 254, then the value of this byte will be 255 and the subsequent four
 * bytes will represent the length of the message. The message then follows these bytes.
 *
 * @module socket
 * @author Edward A. Lee
 * @version $$Id$$
 */

// Stop extra messages from jslint.  Note that there should be no
// space between the / and the * and global.
/*globals actor, console, exports, Java, require, util */
/*jslint nomen: true */
/*jshint globalstrict: true */
"use strict";

var SocketHelper = Java.type('ptolemy.actor.lib.jjs.modules.socket.SocketHelper');
var EventEmitter = require('events').EventEmitter;

///////////////////////////////////////////////////////////////////////////////
//// supportedReceiveTypes

/** Return an array of the types supported by the current host for
 *  receiveType arguments.
 */
exports.supportedReceiveTypes = function () {
    return SocketHelper.supportedReceiveTypes();
};

///////////////////////////////////////////////////////////////////////////////
//// supportedSendTypes

/** Return an array of the types supported by the current host for
 *  sendType arguments.
 */
exports.supportedSendTypes = function () {
    return SocketHelper.supportedSendTypes();
};

///////////////////////////////////////////////////////////////////////////////
//// defaultClientOptions

/** The default options for socket connections from the client side.
 */
var defaultClientOptions = {
    'connectTimeout': 6000, // in milliseconds.
    'idleTimeout': 0, // In seconds. 0 means don't timeout.
    'discardMessagesBeforeOpen': false,
    'emitBatchDataAsAvailable': false,
    'keepAlive': true,
    'maxUnsentMessages': 100,
    'noDelay': true,
    'pfxKeyCertPassword': '',
    'pfxKeyCertPath': '',
    'rawBytes': true,
    'receiveBufferSize': 65536,
    'receiveType': 'string',
    'reconnectAttempts': 10,
    'reconnectInterval': 1000,
    'sendBufferSize': 65536,
    'sendType': 'string',
    'sslTls': false,
    'trustAll': false,
    'trustedCACertPath': ''
};

// FIXME:
// There are additional options in Vert.x NetClientOptions that
// are not documented in the Vert.x documentation, so I don't know what
// they mean.

///////////////////////////////////////////////////////////////////////////////
//// SocketClient

/** Construct an instance of a socket client that can send or receive messages
 *  to a server at the specified host and port.
 *  The returned object subclasses EventEmitter and emits the following events:
 *
 *  * open: Emitted with no arguments when the socket has been successfully opened.
 *  * data: Emitted with the data as an argument when data arrives on the socket.
 *  * close: Emitted with no arguments when the socket is closed.
 *  * error: Emitted with an error message when an error occurs.
 *
 *  You can invoke the this.send() function of this SocketClient object
 *  to send data to the server. If the socket is not opened yet,
 *  then data will be discarded or queued to be sent later,
 *  depending on the value of the discardMessagesBeforeOpen option
 *  (which defaults to false).
 *
 *  The event 'close' will be emitted when the socket is closed, and 'error' if an
 *  an error occurs (with an error message as an argument).
 *
 *  A simple example that sends a message, and closes the socket on receiving a reply.
 *
 *      var socket = require('socket');
 *      var client = new socket.SocketClient();
 *      client.on('open', function() {
 *          client.send('hello world');
 *      });
 *      client.on('data', function onData(data) {
 *          print('Received from socket: ' + data);
 *          client.close();
 *      });
 *      socket.open();
 *
 *  The options argument is a JSON object that can include:
 *  * connectTimeout: The time to wait (in milliseconds) before declaring
 *    a connection attempt to have failed. This defaults to 6000.
 *  * idleTimeout: The amount of idle time in seconds that will cause
 *    a disconnection of a socket. This defaults to 0, which means no
 *    timeout.
 *  * discardMessagesBeforeOpen: If true, then discard any messages
 *    passed to SocketClient.send() before the socket is opened. If false,
 *    then queue the messages to be sent when the socket opens. This
 *    defaults to false.
 *  * emitBatchDataAsAvailable: Whether to emit all data available when the TCP stream
 *    is received and when rawBytes is true. This parameter is intended for socket.js module,
 *    and will not be exposed to TCP socket accessors. Set this true only when
 *    the TCP stream is going to be handled by upper layer protocols.
 *  * keepAlive: Whether to keep a connection alive and reuse it. This
 *    defaults to true.
 *  * maxUnsentMessages: The maximum number of unsent messages to queue before
 *    further calls to this.send() will fail. A value of 0 means no limit.
 *    This defaults to 100.
 *  * noDelay: If true, data as sent as soon as it is available (the default).
 *    If false, data may be accumulated until a reasonable packet size is formed
 *    in order to make more efficient use of the network (using Nagle's algorithm).
 *  * rawBytes: If true (the default), then transmit only the data bytes provided
 *    to this.send() without any header. If false, then prepend sent data with length
 *    information and assume receive data starts with length information.
 *    Setting this false on both ends will ensure that each data item passed to
 *    this.send() is emitted once in its entirety at the receiving end, as a single
 *    message. When this is false, the receiving end can emit a partially received
 *    message or could concatenate two messages and emit them together.
 *  * receiveBufferSize: The size of the receive buffer. Defaults to
 *    65536.
 *  * receiveType: See below.
 *  * reconnectAttempts: The number of times to try to reconnect.
 *    If this is greater than 0, then a failure to attempt will trigger
 *    additional attempts. This defaults to 10.
 *  * reconnectInterval: The time between reconnect attempts, in
 *    milliseconds. This defaults to 1000 (1 second).
 *  * sendBufferSize: The size of the receive buffer. Defaults to
 *    65536.
 *  * sendType: See below.
 *  * sslTls: Whether SSL/TLS is enabled. This defaults to false.
 *  * trustAll: Whether to trust servers. This defaults to false.
 *    Setting it to true means that if sslTls is set to true, then
 *    any certificate provided by the server will be trusted.
 *    FIXME: Need to provide a trusted list if this is false.
 *
 *  The send and receive types can be any of those returned by
 *  supportedReceiveTypes() and supportedSendTypes(), respectively.
 *  If both ends of the socket are known to be JavaScript clients,
 *  then you should use the 'number' data type for numeric data.
 *  If one end or the other is not JavaScript, then
 *  you can use more specified types such as 'float' or 'int', if they
 *  are supported by the host. In all cases, received numeric
 *  data will be converted to JavaScript 'number' when emitted.
 *  For sent data, this will try to convert a JavaScript number
 *  to the specified type. The type 'number' is equivalent
 *  to 'double'.
 *
 *  When type conversions are needed, e.g. when you send a double
 *  with sendType set to int, or an int with sendType set to byte,
 *  then a "primitive narrowing conversion" will be applied, as specified here:
 *  https://docs.oracle.com/javase/specs/jls/se8/html/jls-5.html#jls-5.1.3 .
 *
 *  For numeric types, you can also send an array with a single call
 *  to this.send(). The elements of the array will be sent in sequence all
 *  at once, and may be received in one batch. If both ends have
 *  rawBytes set to false (specifying message framing), then these
 *  elements will be emitted at the receiving end all at once in a single
 *  array. Otherwise, they will be emitted one at a time.
 *
 *  For strings, you can also send an array of strings in a single call,
 *  but these will be simply be concatenated and received as a single string.
 *
 *  If the rawBytes option is set to false, then each data item passed to this.send(),
 *  of any type or array of types, will be coalesced into a single message and
 *  the receiving end (if it also has rawBytes set to false) will emit the entire
 *  message, and only the message, exactly once.  Otherwise, a message may get
 *  fragmented, emitted in pieces, or coalesced with subsequent messages.
 *
 *  The meaning of the options is (partially) defined here:
 *     http://vertx.io/docs/vertx-core/java/
 *
 *  After this SocketClient is constructed, it will have properties 'port'
 *  and 'host' equal to the port and host options passed to the constructor.
 *
 *  @param port The remote port to connect to.
 *  @param host The remote host to connect to.
 *  @param options The options.
 */
exports.SocketClient = function (port, host, options) {
    // Set default values of arguments.
    // Careful: port == 0 means to find an available port, I think.
    this.port = port;
    if (port === null) {
        this.port = 4000;
    }
    this.host = host || 'localhost';

    // Fill in default values.
    this.options = options || {};
    this.options = util._extend(defaultClientOptions, this.options);

    this.helper = SocketHelper.getOrCreateHelper(actor, this);
    this.pendingSends = [];
};
util.inherits(exports.SocketClient, EventEmitter);

/** Open the client. Call this after setting up listeners. */
exports.SocketClient.prototype.open = function () {
    this.helper.openClientSocket(this, this.port, this.host, this.options);
};

/** This method will be called by the helper when a client's request to
 *  open a socket has been granted and the socket is open.
 *  This function should not be called by users of this module.
 *  It will emit an 'open' event with no arguments.
 *  @param netSocket The Vert.x NetSocket object.
 */
exports.SocketClient.prototype._opened = function (netSocket) {
    // For a client, this instance of SocketClient will be the event emitter.

    // Because we are creating an inner class, the first argument needs to be
    // the instance of the enclosing socketHelper class.
    this.wrapper = new SocketHelper.SocketWrapper(
        this.helper,
        this,
        netSocket,
        this.options.sendType,
        this.options.receiveType,
        this.options.rawBytes,
        this.options.emitBatchDataAsAvailable
    );
    this.emit('open');

    var i;
    // Send any pending data.
    for (i = 0; i < this.pendingSends.length; i += 1) {
        this.send(this.pendingSends[i]);
    }
    this.pendingSends = [];
};

/** Send data over the socket.
 *  If the socket has not yet been successfully opened, then queue
 *  data to be sent later, when the socket is opened, unless
 *  discardMessagesBeforeOpen is true.
 *  @param data The data to send.
 */
exports.SocketClient.prototype.send = function (data) {
    if (this.wrapper) {
        if (Array.isArray(data)) {
            data = Java.to(data);
        }
        this.wrapper.send(data);
    } else {
        if (!this.options.discardMessagesBeforeOpen) {
            this.pendingSends.push(data);
            var maxUnsentMessages = this.options.maxUnsentMessages;
            if (maxUnsentMessages > 0 && this.pendingSends.length >= maxUnsentMessages) {
                throw "Maximum number of unsent messages has been exceeded: " +
                    maxUnsentMessages +
                    ". Consider setting discardMessagesBeforeOpen to true.";
            }
        } else {
            console.log('Discarding because socket is not open.');
        }
    }
};

/** Close the current connection with the server.
 *  This will indicate to the server that no more data
 *  will be sent, but data may still be received from the server.
 */
exports.SocketClient.prototype.close = function () {
    if (this.wrapper) {
        this.wrapper.close();
        //} else {
        // FIXME: Set a flag to close immediately upon opening.
    }
};

///////////////////////////////////////////////////////////////////////////////
//// defaultServerOptions

/** The default options for socket servers.
 */
var defaultServerOptions = {
    'clientAuth': 'none', // No SSL/TSL will be used.
    'emitBatchDataAsAvailable': false,
    'hostInterface': '0.0.0.0', // Means listen on all available interfaces.
    'idleTimeout': 0, // In seconds. 0 means don't timeout.
    'keepAlive': true,
    'pfxKeyCertPassword': '',
    'pfxKeyCertPath': '',
    'noDelay': true,
    'port': 4000,
    'rawBytes': true,
    'receiveBufferSize': 65536,
    'receiveType': 'string',
    'sendBufferSize': 65536,
    'sendType': 'string',
    'sslTls': false,
    'trustedCACertPath': ''
};

// FIXME: one of the server options in NetServerOptions is 'acceptBacklog'.
// This is undocumented in Vert.x, so I have no idea what it is. Left it out.
// Also, 'reuseAddress', 'SoLinger', 'TcpNoDelay', 'trafficClass',
// 'usePooledBuffers'.  Maybe the TCP wikipedia page will help.

///////////////////////////////////////////////////////////////////////////////
//// SocketServer

/** Construct an instance of a socket server that listens for connection
 *  requests and opens sockets when it receives them.
 *  After invoking this constructor (using new), the user can set up
 *  listeners for the following events:
 *
 *  * listening: Emitted when the server is listening.
 *    This will be passed the port number that the server is listening
 *    on (this is useful if the port is specified to be 0).
 *  * connection: Emitted when a new connection is established
 *    after a request from (possibly remote) client.
 *    This will be passed an instance of a Socket class
 *    that can be used to send data or to close the socket.
 *    The instance of Socket is also an event emitter that
 *    emits 'close', 'data', and 'error' events.
 *  * error: Emitted if the server fails to start listening.
 *    This will be passed an error message.
 *
 *  A typical usage pattern looks like this:
 *
 *     var server = new socket.SocketServer();
 *     server.on('listening', function(port) {
 *         console.log('Server listening on port: ' + port);
 *     });
 *     var connectionCount = 0;
 *     server.on('connection', function(serverSocket) {
 *         var connectionNumber = connectionCount++;
 *         console.log('Server connected on a new socket number: ' + connectionNumber);
 *         serverSocket.on('data', function(data) {
 *             console.log('Server received data on connection '
 *                     + connectionNumber);
 *         });
 *     });
 *     server.start();
 *
 *  When the 'connection' event is emitted, it will be passed a Socket object,
 *  which has a this.send() function. For example, to send a reply to each incoming
 *  message, replace the above 'data' handler as follows:
 *
 *     serverSocket.on('data', function(data) {
 *        socket.send('Reply message');
 *     });
 *
 *  The Socket object also has a close() function that allows the server to close
 *  the connection.  The ServerSocket object has a close() function that will close
 *  all connections and shut down the server.
 *
 *  An options argument can be passed to the SocketServer constructor above.
 *  This is a JSON object containing the following optional fields:
 *
 *  * clientAuth: One of 'none', 'request', or 'required', meaning whether it
 *    requires that a certificate be presented.
 *  * emitBatchDataAsAvailable: Whether to emit all data available when the TCP stream
 *    is received and when rawBytes is true. This parameter is intended for socket.js module,
 *    and will not be exposed to TCP socket accessors. Set this true only when
 *    the TCP stream is going to be handled by upper layer protocols.
 *  * hostInterface: The name of the network interface to use for listening,
 *    e.g. 'localhost'. The default is '0.0.0.0', which means to
 *    listen on all available interfaces.
 *  * idleTimeout: The amount of idle time in seconds that will cause
 *    a disconnection of a socket. This defaults to 0, which means no
 *    timeout.
 *  * keepAlive: Whether to keep a connection alive and reuse it. This
 *    defaults to true.
 *  * keyStorePassword: If sslTls is set to true, then this option needs to specify
 *    the password for the key store specified by keyStorePath.
 *  * keyStorePath: If sslTls is set to true, then this option needs to specify
 *    the fully qualified filename for the file that stores the certificate that
 *    this server will use to identify itself. This path can be any of those
 *    understood by the Ptolemy host, e.g. paths beginning with $CLASSPATH/.
 *  * noDelay: If true, data as sent as soon as it is available (the default).
 *    If false, data may be accumulated until a reasonable packet size is formed
 *    in order to make more efficient use of the network (using Nagle's algorithm).
 *  * port: The default port to listen on. This defaults to 4000.
 *    a value of 0 means to choose a random ephemeral free port.
 *  * rawBytes: If true (the default), then transmit only the data bytes provided
 *    to this.send() without any header. If false, then prepend sent data with length
 *    information and assume receive data starts with length information.
 *    Setting this false on both ends will ensure that each data item passed to
 *    this.send() is emitted once in its entirety at the receiving end, as a single
 *    message. When this is false, the receiving end can emit a partially received
 *    message or could concatenate two messages and emit them together.
 *  * receiveBufferSize: The size of the receive buffer. Defaults to
 *    65536.
 *  * receiveType: See below.
 *  * sendBufferSize: The size of the receive buffer. Defaults to
 *    65536.
 *  * sendType: See below.
 *  * sslTls: Whether SSL/TLS is enabled. This defaults to false.
 *
 *  The meaning of the options is (partially)defined here:
 *     http://vertx.io/docs/vertx-core/java/
 *
 *  The send and receive types can be any of those returned by
 *  supportedReceiveTypes() and supportedSendTypes(), respectively.
 *  If both ends of the socket are known to be JavaScript clients,
 *  then you should use the 'number' data type for numeric data.
 *  If one end or the other is not JavaScript, then
 *  you can use more specified types such as 'float' or 'int', if they
 *  are supported by the host. In all cases, received numeric
 *  data will be converted to JavaScript 'number' when emitted.
 *  For sent data, this will try to convert a JavaScript number
 *  to the specified type. The type 'number' is equivalent
 *  to 'double'.
 *
 *  When type conversions are needed, e.g. when you send a double
 *  with sendType set to int, or an int with sendType set to byte,
 *  then a "primitive narrowing conversion" will be applied, as specified here:
 *  https://docs.oracle.com/javase/specs/jls/se8/html/jls-5.html#jls-5.1.3 .
 *
 *  For numeric types, you can also send an array with a single call
 *  to this.send(). The elements of the array will be sent in sequence all
 *  at once, and may be received in one batch. If both ends have
 *  rawBytes set to false (specifying message framing), then these
 *  elements will be emitted at the receiving end all at once in a single
 *  array. Otherwise, they will be emitted one at a time.
 *
 *  For strings, you can also send an array of strings in a single call,
 *  but these will be simply be concatenated and received as a single string.
 *
 *  @param options The options.
 */
exports.SocketServer = function (options) {
    // Fill in default values.
    this.options = options || {};
    this.options = util._extend(defaultServerOptions, this.options);

    this.helper = SocketHelper.getOrCreateHelper(actor, this);
};
util.inherits(exports.SocketServer, EventEmitter);

/** Start the server. */
exports.SocketServer.prototype.start = function () {
    this.helper.startServer(this, this.options);
};

/** Stop the server and close all sockets. */
exports.SocketServer.prototype.stop = function () {
    if (this.server) {
        this.server.close();
        this.server = null;
    }
};

/** Notify this SocketServer that the server has been created.
 *  This is called by the helper, and should not be called by the user of this module.
 *  @param netServer The Vert.x NetServer object.
 */
exports.SocketServer.prototype._serverCreated = function (netServer) {
    this.server = netServer;
};

/** Notify that a handshake was successful and a websocket has been created.
 *  This is called by the helper class is not meant to be called by the JavaScript
 *  programmer. When this is called, the Server will create a new Socket object
 *  and emit a 'connection' event with that Socket as an argument.
 *  The 'connection' handler can then register for 'data' events from the
 *  Socket or issue replies to the Socket using its this.send() function.
 *  It can also close() the Socket.
 *  @param netSocket The Vert.x NetSocket object.
 *  @param server The Vert.x NetServer object.
 */
exports.SocketServer.prototype._socketCreated = function (netSocket) {
    var socket = new exports.Socket(
        this.helper,
        netSocket,
        this.options.sendType,
        this.options.receiveType,
        this.options.rawBytes,
        this.options.emitBatchDataAsAvailable
    );
    this.emit('connection', socket);
};

/////////////////////////////////////////////////////////////////
//// Socket

/** A Socket object for the server side of a new connection.
 *  This is created by the _socketCreated function above whenever a new connection is
 *  established at the request of a client. It should not normally be called by
 *  the JavaScript programmer. The returned Socket is an event emitter that emits
 *  the following events:
 *
 *  * data: Emitted when data is received on any socket handled by this server.
 *    This will be passed the data.
 *  * close: Emitted when a socket is closed.
 *    This is not passed any arguments.
 *  * error: Emitted when an error occurs.
 *    This will be passed an error message.
 *
 *  @param helper The instance of SocketHelper that is helping.
 *  @param netSocket The Vert.x NetSocket object.
 *  @param sendType The type to send over the socket.
 *  @param receiveType The type expected to be received over the socket.
 *  @param rawBytes If false, prepend messages with length information and emit
 *   only complete messages.
 *  @param emitBatchDataAsAvailable If this is true and rawBytes is also true,
 *   all available TCP stream data will be emitted in a single data event.
 */
exports.Socket = function (helper, netSocket, sendType, receiveType, rawBytes, emitBatchDataAsAvailable) {
    // For a server side socket, this instance of Socket will be the event emitter.

    // Because we are creating an inner class, the first argument needs to be
    // the instance of the enclosing socketHelper class.
    this.wrapper = new SocketHelper.SocketWrapper(
        helper,
        this,
        netSocket,
        sendType,
        receiveType,
        rawBytes,
        emitBatchDataAsAvailable
    );
    this.netSocket = netSocket;
};
util.inherits(exports.Socket, EventEmitter);

/** Close the socket. Normally, this would be called on the client side,
 *  not on the server side. But the server can also close the connection.
 *  This will indicate to the client that the server will be sending no more data.
 */
exports.Socket.prototype.close = function () {
    this.wrapper.close();
};

/** Return the remote host (an IP address) for this socket.
 *  @return The remote host, a string.
 */
exports.Socket.prototype.remoteHost = function () {
    var remoteAddress = this.netSocket.remoteAddress();
    return remoteAddress.host();
};

/** Return the remote port for this socket.
 *  @return The remote port, a number.
 */
exports.Socket.prototype.remotePort = function () {
    var remoteAddress = this.netSocket.remoteAddress();
    return remoteAddress.port();
};

/** Send data over the socket.
 *  @param data The data to send.
 */
exports.Socket.prototype.send = function (data) {
    if (Array.isArray(data)) {
        data = Java.to(data);
    }
    this.wrapper.send(data);
};
