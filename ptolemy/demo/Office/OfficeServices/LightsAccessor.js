/** Accessor for the Lights service. 
 *  This accessor sends messages to the Lights model via a web socket.
 *  It has embedded in it the IP address of the host running the Lights model.
 *  The Lights model, when it executes, modifies that IP address to conform
 *  with the IP address of whatever server is running the Lights model
 *  and then publishes this accessor, modified with the correct IP address,
 *  to a KeyValueStoreServer running on the same host.
 *  
 *  On startup, this accessor sends to the schema output a description of the
 *  control input that it expects.  It also sends null to the data output to
 *  signal anything downstream that it is starting up.
 *
 *  @accessor LightsAccessor
 *  @author Edward A. Lee (eal@eecs.berkeley.edu)
 *  @input control A JSON object of the form {"mood":"moodname"}, where 
 *   moodname is one of the supported mood names as indicated in the schema
 *   output.
 *  @output data A JSON object of the form {"response":"OK"} or
 *   {"response":"error message"} to indicate whether the
 *   command has suceeded.
 *  @output schema Upon initialization, this output port produces a JSON object that
 *   is a schema for the JSON expected on the control input. This schema
 *   conforms to the JSON schema standard at http://json-schema.org/.
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
	    'value': '8079'
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
    "mood": {
      	"type": "string",
      	"title": "Mood",
      	"description": "Name of the mood to produce using the lights",
      	"choices": ["dark", "bright", "warm", "cool"]
    }
  }
};
