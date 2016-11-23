// Below is the copyright agreement for the Ptolemy II system.
//
// Copyright (c) 2015-2016 The Regents of the University of California.
// All rights reserved.
//
// Permission is hereby granted, without written agreement and without
// license or royalty fees, to use, copy, modify, and distribute this
// software and its documentation for any purpose, provided that the above
// copyright notice and the following two paragraphs appear in all copies
// of this software.
//
// IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
// FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE.
//
// THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
// INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
// ENHANCEMENTS, OR MODIFICATIONS.
//
//
// Ptolemy II includes the work of others, to see those copyrights, follow
// the copyright link on the splash page or see copyright.htm.
/**  A device discovery library for the Javascript Nashorn engine.
 *
 *  This library uses the "ping" and "arp" commands to discover devices
 *  connected to the local network.  Command syntax is adjusted for OS
 *  differences.
 *
 *  It implements the discover(IPAddress) method required by the device
 *  discovery accessor, and returns a JSON object containing a list of device
 *  IP addresses, and names and MAC addresses when available.
 *
 *  Accessors overview: https://www.terraswarm.org/accessors/
 *  Device discovery overview:  http://www.terraswarm.org/testbeds/wiki/Main/DiscoverEthernetMACs
 *
 *  @module discovery
 *  @author Elizabeth Latronico
 */

/**  A service discovery function for the Javascript Nashorn engine.
 *
 *  The long term goal is to provide a discovery process based on a defined ontology
 *  for a particular category of services. Right now, the list of services are known a-priori
 *  and specified as interfaces (e.g. GSNInterface.js) that context aware accessor can  include using the
 *  'implement' function.
 *
 *  The known services are obtained via the services() function which is implemented
 *  by the method availableServices() in the helper class.
 *
 *  @module contextAware
 *  @author Anne H. Ngu
 */

// Stop extra messages from jslint.  Note that there should be no
// space between the / and the * and global.
/*globals Java, exports, require */
/*jshint globalstrict: true*/
"use strict";

var EventEmitter = require("events").EventEmitter;

// Use a helper classs to execute the ping and arp commands on the host
var DiscoveryHelper = Java.type('ptolemy.actor.lib.jjs.modules.discovery.DiscoveryHelper');

exports.DiscoveryService = DiscoveryService;

/** A DiscoveryService "class" that polls the local network for available
 *  devices.  A device is considered available if it responds to a ping
 *  request.  Emits an event once device polling is complete.
 */
function DiscoveryService() {
    EventEmitter.call(this);
    var self = this;
    var helper = new DiscoveryHelper();

    /** Discover devices on the local area network.
     *
     * @param IPAddress The IP address of the host machine.
     * @param discoveryMethod  Optional. The discovery method to use, e.g. nmap.
     */
    this.discoverDevices = function (IPAddress, discoveryMethod) {

        var devices;
        if (typeof discoveryMethod !== 'undefined') {
            devices = helper.discoverDevices(IPAddress, discoveryMethod);
        } else {
            devices = helper.discoverDevices(IPAddress, "ping");
        }

        // Use JSON.parse() here, since discoverDevices() returns a string
        // representation of a JSON array.  Problems occurred if a JSONArray
        // object was directly returned instead of a string.
        self.emit('discovered', JSON.parse(devices));
    };

    this.getHostAddress = function () {
        return helper.getHostAddress();
    };
}
//DiscoveryService emits events.  See:
//http://smalljs.org/object/events/event-emitter/
//http://www.sitepoint.com/nodejs-events-and-eventemitter/
DiscoveryService.prototype = new EventEmitter();
