/**
 * Module supporting UDP sockets.
 * @module udpSocket
 * @authors: Hokeun Kim
 * @copyright: http://terraswarm.org/accessors/copyright.txt
 */

var UDPSocketHelper = Java.type('ptolemy.actor.lib.jjs.modules.udpSocket.UDPSocketHelper');
var EventEmitter = require('events').EventEmitter;

// This file contains first the code for a UDP socket.

///////////////////////////////////////////////////////////////////////////////
//// Client

/** Construct an instance of a socket client that can send or receive messages
 *  to a server at the specified host and port.
 *  The returned object subclasses EventEmitter.
 *  You can register handlers for events 'open', 'message', 'close', or 'error'.
 *  For example,
 *  <pre>
 *    var WebSocket = require('webSocket');
 *    var client = new WebSocket.Client('localhost', 8080);
 *    client.on('message', onMessage);
 *    function onMessage(message) {
 *      print('Received from web socket: ' + message);
 *    }
 *  </pre>
 *  @param options A JSON object with fields 'host' and 'port' that give the
 *   IP address or host name for the host and the port on which the host is listening.
 *   If the host is omitted, 'localhost' is used. If the port is omitted, 80 is used.
 */

 
exports.Socket = function() {
    this.helper = UDPSocketHelper.createSocket(this);
}
util.inherits(exports.Socket, EventEmitter);

exports.createSocket = function() {
    return new exports.Socket();
}

/** Send data over the web socket.
 *  The data can be anything that has a JSON representation.
 *  If the socket has not yet been successfully opened, then queue
 *  data to be sent later, when the socket is opened.
 *  @param data The data to send.
 */
exports.Socket.prototype.send = function(data) {
    if (typeof data != 'string') {
        data = JSON.stringify(data);
    }
    this.helper.sendText(data);
}

exports.Socket.prototype.bind = function(port) {
    this.helper.bind(port);
}

/** Close the current connection with the server.
 *  If there is data that was passed to send() but has not yet
 *  been successfully sent (because the socket was not open),
 *  then throw an exception.
 */
exports.Socket.prototype.close = function() {
    this.helper.close();
}

/** Notify this object of a received message from the socket.
 *  This function attempts to parse the message as JSON and then
 *  emits a "message" event with the message as an argument.
 *  This function is called by the Java helper used by this particular
 *  implementation and should not be normally called by the user.
 *  FIXME: Any way to hide it?
 *  @param message The incoming message.
 */
exports.Socket.prototype.notifyIncoming = function(message) {
    try {
        message = JSON.parse(message);
    } catch (exception) {
        // Assume that the message is a string.
        // We can ignore the exception, because the message
        // will be passed as a string.
    }
    this.emit("message", message);
};
