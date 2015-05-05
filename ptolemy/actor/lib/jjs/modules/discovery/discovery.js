/**  A device discovery library for the Javascript Nashorn engine.
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
var DiscoveryHelper = Java.type
    ('ptolemy.actor.lib.jjs.modules.discovery.DiscoveryHelper');

exports.DiscoveryService = DiscoveryService;

/** A DiscoveryService "class" that polls the local network for available 
 *  devices.  A device is considered available if it responds to a ping 
 *  request.  Emits an event once device polling is complete.
 */
function DiscoveryService() {
	EventEmitter.call(this);
	var self = this;
	
	/** Discover devices on the local area network.
	 * 
	 * @param IPaddress The IP address of the host machine.
	 * @param discoveryMethod  Optional. The discovery method to use, e.g. nmap.
	 */
	this.discoverDevices = function(IPaddress, discoveryMethod) {
		
		var helper = new DiscoveryHelper();
		
		// discoverDevices() returns a string representation of a JSON array of 
		// devices.  Covert this to a format the Ptolemy type system likes.
		var devices;
		if (typeof discoveryMethod !== undefined) {
			devices = helper.discoverDevices(IPaddress, discoveryMethod);
		} else {
			devices = helper.discoverDevices(IPaddress, "ping");
		}
		var ipArray = [];
		ipArray.push(JSON.parse(devices)); 
		
		self.emit('discovered', ipArray);
	};
}
//DiscoveryService emits events.  See:
//http://smalljs.org/object/events/event-emitter/
//http://www.sitepoint.com/nodejs-events-and-eventemitter/
DiscoveryService.prototype = new EventEmitter;


