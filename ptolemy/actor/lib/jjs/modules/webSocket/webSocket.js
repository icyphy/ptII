/**
 * Module supporting web sockets.
 * This module defines three classes, Client, Server, and Socket.
 * To make a connection, create an instance of Server, set up listeners,
 * and start the server. On another machine (or the same machine), create
 * an instance of Client and set up listeners and/or invoke send() to send
 * a message. When a client connects to the Server, the Server will create
 * an instance of the Socket object.
 *
 * This module also provides two utility functions that return arrays
 * of MIME types supported for sending or receiving messages.
 *
 * @module webSocket
 * @authors: Hokeun Kim and Edward A. Lee
 * @copyright: http://terraswarm.org/accessors/copyright.txt
 */

var WebSocketHelper = Java.type('ptolemy.actor.lib.jjs.modules.webSocket.WebSocketHelper');
var WebSocketServerHelper = Java.type('ptolemy.actor.lib.jjs.modules.webSocket.WebSocketServerHelper');
var EventEmitter = require('events').EventEmitter;

///////////////////////////////////////////////////////////////////////////////
//// supportedReceiveTypes

/** Return an array of the types supported by the current host for
 *  receiveType arguments.
 */
exports.supportedReceiveTypes = function() {
    return WebSocketHelper.supportedReceiveTypes();
}

///////////////////////////////////////////////////////////////////////////////
//// supportedSendTypes

/** Return an array of the types supported by the current host for
 *  sendType arguments.
 */
exports.supportedSendTypes = function() {
    return WebSocketHelper.supportedSendTypes();
}

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
 *      var WebSocket = require('webSocket');
 *      var client = new WebSocket.Client({'host': 'localhost', 'port': 8080});
 *      client.send({'foo': 'bar'});
 *      client.on('message', onMessage);
 *      function onMessage(message) {
 *          print('Received from web socket: ' + message);
 *      }
 *  
 *  The options argument is a JSON object that can contain the following fields:
 *  * host: The IP address or host name for the host. Defaults to 'localhost'.
 *  * port: The port on which the host is listening. Defaults to 80.
 *  * receiveType: The MIME type for incoming messages, which defaults to 'application/json'.
 *  * sendType: The MIME type for outgoing messages, which defaults to 'application/json'.
 *  * connectTimeout: The time to wait before giving up on a connection.
 *  * maxFrameSize: The maximum frame size for a received message.
 *  * numberOfRetries: The number of times to retry connecting. Defaults to 0.
 *  * timeBetweenRetries: The time between retries, in milliseconds. Defaults to 100.
 *  * discardMessagesBeforeOpen: If true, discard messages before the socket is open. Defaults to false.
 *  * throttleFactor: The number milliseconds to stall for each item that is queued waiting to be sent. Defaults to 0.
 *
 *  @param options The options.
 */
exports.Client = function(options) {
    options = options || {};
    this.port = options['port'] || 80;
    this.host = options['host'] || 'localhost';
    this.receiveType = options['receiveType'] || 'application/json';
    this.sendType = options['sendType'] || 'application/json';
    this.connectTimeout = options['connectTimeout'] || 60000;
    this.maxFrameSize = options['maxFrameSize'] || 65536;
    this.numberOfRetries = options['numberOfRetries'] || 1;
    this.timeBetweenRetries = options['timeBetweenRetries'] || 100;
    this.discardMessagesBeforeOpen = options['discardMessagesBeforeOpen'] || false;
    this.throttleFactor = options['throttleFactor'] || 0;
    this.helper = WebSocketHelper.createClientSocket(
        this,
        this.host,
        this.port,
        this.receiveType,
        this.sendType,
        this.connectTimeout,
        this.maxFrameSize,
        this.numberOfRetries,
        this.timeBetweenRetries,
        this.discardMessagesBeforeOpen,
        this.throttleFactor);
}
util.inherits(exports.Client, EventEmitter);

/** Send data over the web socket.
 *  If the socket has not yet been successfully opened, then queue
 *  data to be sent later, when the socket is opened.
 *  @param data The data to send.
 */
exports.Client.prototype.send = function(data) {
    if (this.sendType == 'application/json') {
        this.helper.send(JSON.stringify(data));
    } else if (this.sendType.search(/text\//) == 0) {
        this.helper.send(data.toString());
    } else {
        this.helper.send(data);
    }        
}

/** Close the current connection with the server.
 *  If there is data that was passed to send() but has not yet
 *  been successfully sent (because the socket was not open),
 *  then throw an exception.
 */
exports.Client.prototype.close = function() {
    this.helper.close();
}

/** Notify this object of a received message from the socket.
 *  This function attempts to interpret the message according to the
 *  receiveType, and emits a "message" event with the message as an argument.
 *  For example, with the default receiveType of 'application/json', it will
 *  use JSON.parse() to parse the message and emit the result of the parse.
 *  This function is called by the Java helper used by this particular
 *  implementation and should not be normally called by the user.
 *  FIXME: Any way to hide it?
 *  @param message The incoming message.
 */
exports.Client.prototype.notifyIncoming = function(message) {
    if (this.receiveType == 'application/json') {
        try {
            message = JSON.parse(message);
        } catch(error) {
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

/** Notify this object of a received message from the socket.
 *  This function attempts to parse the message as JSON and then
 *  emits a "message" event with the message as an argument.
 *  @param message The incoming message.
 */
exports.Socket.prototype.notifyIncoming = function(message) {
    if (this.receiveType == 'application/json') {
        try {
            message = JSON.parse(message);
        } catch(error) {
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
exports.Socket.prototype.send = function(data) {
    if (this.sendType == 'application/json') {
        this.helper.send(JSON.stringify(data));
    } else if (this.sendType.search(/text\//) == 0) {
        this.helper.send(data.toString());
    } else {
        this.helper.send(data);
    }        
}
