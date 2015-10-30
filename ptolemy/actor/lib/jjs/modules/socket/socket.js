/**
 * Module supporting sockets.
 * This module defines three classes, SocketClient, SocketServer, and Socket.
 * To make a connection, create an instance of SocketServer, set up listeners,
 * and start the server. On another machine (or the same machine), create
 * an instance of SocketClient and set up listeners and/or invoke send() to send
 * a message. When a client connects to the SocketServer, the SocketServer will create
 * an instance of the Socket object.
 *
 * This module also provides two utility functions that return arrays
 * of MIME types supported for sending or receiving messages.
 *
 * @module socket
 * @authors: Edward A. Lee
 * @copyright: http://terraswarm.org/accessors/copyright.txt
 */

var SocketHelper = Java.type('ptolemy.actor.lib.jjs.modules.socket.SocketHelper');
var EventEmitter = require('events').EventEmitter;

///////////////////////////////////////////////////////////////////////////////
//// supportedReceiveTypes

/** Return an array of the types supported by the current host for
 *  receiveType arguments.
 */
exports.supportedReceiveTypes = function() {
    return SocketHelper.supportedReceiveTypes();
}

///////////////////////////////////////////////////////////////////////////////
//// supportedSendTypes

/** Return an array of the types supported by the current host for
 *  sendType arguments.
 */
exports.supportedSendTypes = function() {
    return SocketHelper.supportedSendTypes();
}

///////////////////////////////////////////////////////////////////////////////
//// defaultOptions

/** The default options for socket connections.
 *  The meaning of the options is defined here:
 *     http://vertx.io/docs/vertx-core/java/
 *  The send and receive types can be one of 'string', 'number',
 *  or 'byte'. For connecting to sockets that are not JavaScript,
 *  they can alternatively be
 *  'double', 'float', 'int', 'long', 'short', 'unsignedByte',
 *  'unsignedInt', or 'unsignedShort', all of which will be converted
 *  to 'number' when emitted as data.
 */
var defaultOptions = {
    'connectTimeout': 6000, // in milliseconds.
    'idleTimeout': 0, // In second. 0 means don't timeout.
    'discardMessagesBeforeOpen': false,
    'keepAlive': true,
    'numberOfRetries': 1,
    'receiveType': 'string',
    'receiveBufferSize': 65536,
    'reconnectAttempts': 10,
    'reconnectInterval': 100,
    'sendBufferSize': 65536,
    'sendType': 'string',
    'sslTls': false,
    'trustAll': true,
}

///////////////////////////////////////////////////////////////////////////////
//// SocketClient

/** Construct an instance of a socket client that can send or receive messages
 *  to a server at the specified host and port.
 *  The returned object subclasses EventEmitter.
 *  You can register handlers for events 'open', 'data', 'close', or 'error'.
 *  The event 'open' will be emitted when the socket has been successfully opened
 *  and will be passed an object that has a send() and a close() function that
 *  you can use to send data or close the socket.
 *  The event 'data' will be emitted with data as an
 *  argument when incoming data arrives on the socket.
 *  You can alternatively invoke the send() function of this SocketClient object
 *  to send data to the server, but in this case, if the socket is not opened yet,
 *  then data will be queued to be sent when it does get opened or discarded,
 *  depending on the value of the discardMessagesBeforeOpen option (which defaults
 *  to false).
 *
 *  FIXME: check this
 *  The type of data sent and received can be specified with the 'sendType'
 *  and 'receiveType' options.
 *  In principle, any MIME type can be specified, but the host may support only
 *  a subset of MIME types.  The client and the server have to agree on the type,
 *  or the data will not get through correctly.
 *
 *  The default type for both sending and receiving
 *  is 'application/json'. The types supported by this implementation
 *  include at least:
 *  * __application/json__: The send() function uses JSON.stringify() and sends the
 *    result with a UTF-8 encoding. An incoming byte stream will be parsed as JSON,
 *    and if the parsing fails, will be provided as a string interpretation of the byte
 *    stream.
 *  * __text/\*__: Any text type is sent as a string encoded in UTF-8.
 *  * __image/x__: Where __x__ is one of __json__, __png__, __gif__,
 *    and more (FIXME: which, exactly?).
 *    In this case, the data passed to send() is assumed to be an image, as encoded
 *    on the host, and the image will be encoded as a byte stream in the specified
 *    format before sending.  A received byte stream will be decoded as an image,
 *    if possible. FIXME: What happens if decoding fails?
 *  
 *  The event 'close' will be emitted when the socket is closed, and 'error' if an
 *  an error occurs (with an error message as an argument).
 *  For example,
 *  
 *      var socket = require('socket');
 *      var client = new socket.SocketClient({'host': 'localhost', 'port': 8080});
 *      client.send({'foo': 'bar'});
 *      client.on('data', onData);
 *      function onData(data) {
 *          print('Received from socket: ' + data);
 *      }
 *  
 *  The options argument is a JSON object that can contain the following fields:
 *  * host: The IP address or host name for the host. Defaults to 'localhost'.
 *  * port: The port on which the host is listening. Defaults to 4000.
 *  * receiveType: The MIME type for incoming messages, which defaults to 'application/json'.
 *  * sendType: The MIME type for outgoing messages, which defaults to 'application/json'.
 *  * connectTimeout: The time to wait before giving up on a connection.
 *  * maxFrameSize: The maximum frame size for a received message.  FIXME?
 *  * numberOfRetries: The number of times to retry connecting. Defaults to 0.
 *  * timeBetweenRetries: The time between retries, in milliseconds. Defaults to 100.
 *  * discardMessagesBeforeOpen: If true, discard messages before the socket is open. Defaults to false.
 *  * throttleFactor: The number milliseconds to stall for each item that is queued waiting to be sent. Defaults to 0.
 *
 *  @param options The options.
 */
exports.SocketClient = function(port, host, options) {
    // Set default values of arguments.
    // Careful: port == 0 means to find an available port, I think.
    if (port == null) {
        port = 4000;
    }
    host = host || 'localhost';

    // Fill in default values.
    this.options = options || {};
    this.options = util._extend(defaultOptions, this.options);
    
    this.helper = SocketHelper.getOrCreateHelper(actor);
    this.socket = this.helper.openClientSocket(this, port, host, this.options);
}
util.inherits(exports.SocketClient, EventEmitter);

///////////////////////////////////////////////////////////////////////////////
//// FIXME: Below is not updated yet.


/** Send data over the socket.
 *  If the socket has not yet been successfully opened, then queue
 *  data to be sent later, when the socket is opened.
 *  @param data The data to send.
 */
exports.SocketClient.prototype.send = function(data) {
    error('FIXME: send() on SocketClient not implemented yet');        
}

/** Close the current connection with the server.
 *  If there is data that was passed to send() but has not yet
 *  been successfully sent (because the socket was not open),
 *  then throw an exception.
 */
exports.SocketClient.prototype.close = function() {
    error('FIXME: close() on SocketClient not implemented yet');        
}

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
 *  * maxFrameSize: The maximum frame size for a received message.
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
exports.Server = function(options) {
    this.port = options['port'] || 80;
    this.hostInterface = options['hostInterface'] || 'localhost';
    this.receiveType = options['receiveType'] || 'application/json';
    this.sendType = options['sendType'] || 'application/json';
    this.maxFrameSize = options['maxFrameSize'] || 65536;
    this.helper = WebSocketServerHelper.createServer(
            this, this.hostInterface, this.port, this.receiveType, this.sendType,
            this.maxFrameSize);
}
util.inherits(exports.Server, EventEmitter);

/** Start the server. */
exports.Server.prototype.start = function() {
    this.helper.startServer();
}

/** Stop the server. */
exports.Server.prototype.close = function() {
    this.helper.closeServer();
}

/** Notify that a handshake was successful and a websocket has been created.
 *  This is called by the helper class is not meant to be called by the JavaScript
 *  programmer. When this is called, the Server will a new Socket object
 *  and emit a 'connection' event with that Socket as an argument.
 *  The 'connection' handler can then register for 'message' events from the
 *  Socket or issue replies to the Socket using send(). It can also close() the
 *  Socket.
 *  @param serverWebSocket The Java ServerWebSocket object.
 */
exports.Server.prototype.socketCreated = function(serverWebSocket) {
    var socket = new exports.Socket(
            serverWebSocket, this.receiveType, this.sendType, this.maxFrameSize);
    this.emit('connection', socket);
}

/////////////////////////////////////////////////////////////////
//// Socket

/** Construct (using new) a Socket object for the server side of a new connection.
 *  This is called by the socketCreated function above whenever a new connection is
 *  established at the request of a client. It should not normally be called by
 *  the JavaScript programmer. The returned Socket is an event emitter that emits
 *  'message' events.
 *  @param serverWebSocket The Java ServerWebSocket object.
 *  @param receiveType The MIME type for incoming messages, which defaults to 'application/json'.
 *  @param sendType The MIME type for outgoing messages, which defaults to 'application/json'.
 *  @param maxFrameSize The maximum frame size for a received message.
 */
exports.Socket = function(serverWebSocket, receiveType, sendType, maxFrameSize) {
    this.helper = WebSocketHelper.createServerSocket(
            this, serverWebSocket, receiveType, sendType, maxFrameSize);
    this.receiveType = receiveType;
    this.sendType = sendType;
    this.maxFrameSize = maxFrameSize;
}
util.inherits(exports.Socket, EventEmitter);

/** Close the socket. Normally, this would be called on the client side,
 *  not on the server side. But the server can also close the connection.
 */
exports.Socket.prototype.close = function() {
    this.helper.close();
}

/** Return true if the socket is open.
 */
exports.Socket.prototype.isOpen = function() {
    return this.helper.isOpen();
}


/** Send data over the web socket.
 *  The data can be anything that has a JSON representation.
 *  @param data The data to send.
 */
exports.Socket.prototype.send = function(data) {
    if (this.sendType == 'application/json') {
        this.helper.send(JSON.stringify(data));
    } else if (this.sendType.search(/text\//) == 0) {
        this.helper.send(data.toString());
    } else {
        this.helper.send(data);
    }        
}
