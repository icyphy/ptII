/** Accessor for the Services service. 
 *  This accessor sends messages to the Services model via a web socket
 *  and produces the response on its output.
 *  It has embedded in it the IP address of the host running the Services model.
 *  The Services model, when it executes, modifies that IP address to conform
 *  with the IP address of whatever server is running the Services model
 *  and then publishes this accessor, modified with the correct IP address,
 *  to a KeyValueStoreServer running on the same host.
 *  
 *  On startup, this accessor sends null to the data output to
 *  signal anything downstream that it is starting up.
 *
 *  @accessor Services
 *  @author Edward A. Lee (eal@eecs.berkeley.edu)
 *  @input control A trigger, which can be anything, that causes this accessor
 *   to query for the status of services.
 *  @output data An array of objects reporting the status of services.
 *  @output schema Not used by this accessor
 *  @version $$Id$$
 */
// Stop extra messages from jslint.  Note that there should be no
// space between the / and the * and global.
/*globals console, error, exports, require */
/*jshint globalstrict: true*/
"use strict";

exports.setup = function() {
	this.implement('ControllableSensor');
	var WebSocketClient = this.instantiate('WebSocketClient', 'net/WebSocketClient');
	WebSocketClient.input('server', {
	    'value': '128.32.47.81'
	});
	WebSocketClient.input('port', {
	    'value': '8070'
	});
    this.connect('control', WebSocketClient, 'toSend');
    this.connect(WebSocketClient, 'received', 'data');
};

exports.initialize = function() {
	// At initialize, send the schema;
	this.send('schema', schema);
	// Also send null data.
	this.send('data', null);
};
var schema = {
  "type": "object",
  "properties": {
    "sound": {
      	"type": "string",
      	"title": "Any trigger at all",
      	"description": "Any trigger at all",
      	"value": "request service status"
    }
  }
};
