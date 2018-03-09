/** Accessor for a particular sensor. 
 *  This accessor subscribes to a web socket that is published to by the 
 *  Sensors service, filtering the data provided by that service so that 
 *  only data for sensor with the  specified ID is monitored. 
 *  This accessor produces the parsed JSON messages received on that 
 *  socket on its output. The control input is ignored.
 *  
 *  This has embedded in it the IP address of the host running the Sensors
 *  model. The Sensors model, when it executes, modifies that IP address 
 *  to conform with the IP address of whatever server is running the 
 *  Sensors model and then publishes this accessor, modified with the 
 *  correct IP address, to a KeyValueStoreServer running on the same host.
 *  That model also inserts the 
 *  
 *  @accessor Sensor
 *  @author Edward A. Lee (eal@eecs.berkeley.edu)
 *  @input control Ignored.
 *  @output data Output from the sensor.
 *  @output schema A description of the output.
 *  @version $$Id$$
 */
var webSocket = require('@accessors-modules/web-socket-client');

exports.setup = function() {
	this.implement('ControllableSensor');
};

exports.initialize = function() {
	// At initialize, send the schema;
	this.send('schema', schema);
	// Also send null data.
	this.send('data', null);
	
	var thiz = this;
	
	var client = new webSocket.Client({'host': 'localhost', 'port': 8077});
	
	// Provide a filter input to the WebSocketClient.
    client.send({"filter": {"id":"sensorID"}});
    
    client.on('message', function(message) {
    	thiz.send('data', {
    		'sensor': 'sensorName',
    		'type': 'sensorType',
    		'location': 'sensorLocation',
    		'data': message
    	});
    });
    client.open();
};

var schema = {
  "type": "object",
  "properties": {
    "filter": {
      	"type": "string",
      	"title": "A JSON object specifying a filter",
      	"description": "If the control has the form of an object with \
property 'device_id', then the service will be informed to forward only \
messages from the specified device. If the object has property 'device', \
then the service will be informed to forward only messages from the specified \
device type (e.g. 'PowerBlade'). Any other form will be interpreted as no filter.",
      	"value": "no filter"
    }
  }
};
