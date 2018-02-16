/** Accessor for the Sensors service. 
 *  This accessor subscribes to a web socket that is published to by the Sensors service
 *  and produces the parsed JSON messages received on that socket on its output.
 *  It has embedded in it the IP address of the host running the Sensors model.
 *  The Sensors model, when it executes, modifies that IP address to conform
 *  with the IP address of whatever server is running the Sensors model
 *  and then publishes this accessor, modified with the correct IP address,
 *  to a KeyValueStoreServer running on the same host.
 *  
 *  On startup, this accessor sends null to the data output to
 *  signal anything downstream that it is starting up.
 *
 *  @accessor Sensors
 *  @author Edward A. Lee (eal@eecs.berkeley.edu)
 *  @input control FIXME.
 *  @output data FIXME.
 *  @output schema FIXME.
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
	    'value': '8077'
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

// NOTE: Using "options" instead of "choices" below will result in
// a pull-down list rather than a radio button.
var schema = {
  "type": "object",
  "properties": {
    "trigger": {
      	"type": "string",
      	"title": "Any trigger at all",
      	"description": "Any trigger at all",
      	"value": "request service status"
    }
  }
};
