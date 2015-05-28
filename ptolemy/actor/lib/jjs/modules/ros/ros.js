/**
 * Module supporting ROS.
 * @module ros
 * @authors: Edward A. Lee
 * @copyright: http://terraswarm.org/accessors/copyright.txt
 */

var WebSocket = require('webSocket');
var EventEmitter = require('events').EventEmitter;
var Service = require('./src/core/Service');
var ServiceRequest = require('./src/core/ServiceRequest');

///////////////////////////////////////////////////////////////////////////////
//// Ros

/**
 * Manages connection to the server and all interactions with ROS.
 *
 * Emits the following events:
 *  * 'error' - there was an error with ROS
 *  * 'connection' - connected to the WebSocket server
 *  * 'close' - disconnected to the WebSocket server
 *  * <topicName> - a message came from rosbridge with the given topic name
 *  * <serviceID> - a service response came from rosbridge with the given ID
 *
 * @constructor
 * @param options A JSON object with fields 'host' and 'port' that give the
 *   IP address or host name for the host and the port on which the host is listening.
 *   If the host is omitted, 'localhost' is used. If the port is omitted, 80 is used.
 */
exports.Ros = function(options) {
    options = options || {};
    this.port = options['port'] || 80;
    this.host = options['host'] || 'localhost';
    this.idCounter = 0;
    this.socket = new WebSocket.Client(options);
}
util.inherits(exports.Ros, EventEmitter);

/** Disconnect from the WebSocket server. */
exports.Ros.prototype.close = function() {
  if (this.socket) {
    this.socket.close();
  }
};

/** Send a message, or queue it to send when the connection is established.
 *  @param message The message, which will be converted to a JSON string and sent.
 */
exports.Ros.prototype.callOnConnection = function(message) {
  this.socket.send(message);
};

/** Retrieves list of topics in ROS as an array.
 *  @param callback Function that will be called with params:
 *   * topics - Array of topic names
 */
exports.Ros.prototype.getTopics = function(callback) {
  var topicsClient = new Service({
    ros : this,
    name : '/rosapi/topics',
    serviceType : 'rosapi/Topics'
  });

  var request = new ServiceRequest();

  topicsClient.callService(request, function(result) {
    callback(result.topics);
  });
};
