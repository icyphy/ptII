/**  A device discovery library for the Javascript Rhino engine.
 *  
 *  This library uses the "ping" and "arp" commands to discover devices 
 *  connected to the local network.  Command syntax is adjusted for OS 
 *  differences.
 *  
 *  It implements the discover(IPaddress) method required by the device 
 *  discovery accessor, and returns a JSON object containing a list of device
 *  IP addresses, and names and MAC addresses when available.
 *  
 *  Accessors overview: https://www.terraswarm.org/accessors/
 *  Device discovery overview:  http://www.terraswarm.org/testbeds/wiki/Main/DiscoverEthernetMACs
 *
 *  Author: Elizabeth Latronico
 */

var EventEmitter = require("events").EventEmitter;

// Use a helper classs to execute the ping and arp commands on the host
var DeviceDiscoveryHelper = Java.type
    ('org.terraswarm.accessor.jjs.demo.DeviceDiscovery.DeviceDiscoveryHelper');

// Create a map (just an object) to hold IP lookup results
var ipMap = {};

exports.DiscoveryService = DiscoveryService;

/** A DiscoveryService "class" that polls the local network for available 
 *  devices.  A device is considered available if it responds to a ping 
 *  request.  Emits an event once device polling is complete.
 */
function DiscoveryService() {
	EventEmitter.call(this);
	var self = this;
	
	this.discover = function(IPaddress) {
		
		var helper = new DeviceDiscoveryHelper();
		
		// discover() returns a string representation of a JSON array of 
		// devices.  Covert this to a format the Ptolemy type system likes.
		var devices = helper.discover(IPaddress);
		var ipArray = [];
		ipArray.push(JSON.parse(devices));
		
		self.emit('discovered', ipArray);
	};
}
//DiscoveryService emits events.  See:
//http://smalljs.org/object/events/event-emitter/
//http://www.sitepoint.com/nodejs-events-and-eventemitter/
DiscoveryService.prototype = new EventEmitter;


