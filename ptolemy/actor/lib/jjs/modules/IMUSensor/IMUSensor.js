/**
 * Module to access bluetooth IMU sensor (MotionNet sensor from  UTDallas)
 * @module IMUSensor
 * @author Hunter Massey
 * @copyright http://terraswarm.org/accessors/copyright.txt
 */

 // Reference to the Java class that controls the bluetooth serial stream
var Controller = Java.type('ptolemy.actor.lib.jjs.modules.IMUSensor.SerialPortController');
var SensorStream = null;

/** Construct an instance of a Controller object type.
 *  This starts a thread in the java code that constantly reads input on the serial port that
 *  was linked to the sensor. 
 *  If the code stops unexpectedly it is possible this serial port will remain open and
 *  ptolemy will need to be restarted.
 *  This code may be 'dumb' as it simply reads the input buffer
 *  whenever the getSample method is called and returns it. Checking whether this sample is a new one
 *  is done in the accessor.
 *
 *  Example:
 *  var imuSensor = require('IMUSensor');
 *  var stream = imuSensor.Stream();
 *  initialize: 
 *  	stream.start(bluetooth serialport #);
 *	setInterval(getSample, getParameter(samplingRate));
 *  getSample: 
 *  	var sample = stream.getSample();
 *  	if(prevSample != sample){
 *  		prevSample = sample;
 *  		send('stream', sample);
 *  	}
 */
exports.Stream = function(sensorPort) {
	// Does not take options!
	// Default is set by java code
	// Baud rate: 115200
	// Buffer window: 60 samples - @ 20HZ, this is 3 seconds
	SensorStream = new Controller();
}

/* Initializes connection and collection of data from sensor
* 
*/
exports.Stream.prototype.start = function(sensorPort) {
	SensorStream.start(sensorPort);
}

/** Returns the sample buffer - the sample buffer size should be modifiable in the future
 *  
 */
exports.Stream.prototype.getSample = function(){	
	var sample = Java.from(SensorStream.getSample());
	return [sample, []];
}

/** Stops the sensor stream from continuing to collect data
*/
exports.Stream.prototype.stop = function() {
	SensorStream.stop();
}
