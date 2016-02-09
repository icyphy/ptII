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
// Ptolemy II includes the work of others, to see those copyrights, follow
// the copyright link on the splash page or see copyright.htm.

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
/*globals Java, console, exports */
/*jshint globalstrict: true*/
"use strict";

var ContextAwareHelper = Java
    .type('ptolemy.actor.lib.jjs.modules.contextAware.ContextAwareHelper');
var helper = new ContextAwareHelper();


////////////////////////////////////////////////////////////
////Functions provided in this module.

/** 
 * Use a helper class to return the list of data to be extracted from the GSN service.
 */
exports.gsnServices = function () {
    return Java.from(helper.getGsnOutput());
};

/** 
 * Use a helper class to return the list of data to be extracted from  the Firebase service.
 */
exports.firebaseServices = function () {
    return Java.from(helper.getFirebaseOutput());
};

/** 
 * Use a helper class to return the list of data to be extracted from  the Paraimpu service
 */
exports.paraimpuServices = function () {
    return Java.from(helper.getParaimpuOutput());
};

/**
 * Use a helper class to return a list of known REST services
 */
exports.services = function () {
    return Java.from(helper.availableServices());
};

/**
 * Use a helper class to convert xml data format to json data format.
 */
exports.xmlToJson = function (response) {
    return Java.from(helper.convertXMLtoJSON(response));
};

exports.DiscoveryOfRESTService = DiscoveryOfRESTService;

/** A function that will take a user request and query a registry or invoke a discovery
 *  process to find the 'best' service that the context aware accessor can use. 
 *  This function is not used currently.
 */
function DiscoveryOfRESTService() {
    var self = this;
    var serviceParam;
    /** Discover IoT services */
    this.discoverServices = function () {
        var currentService;
        currentService = helper.getSelectedService();
        serviceParam = helper.getSelectedServiceParameter(currentService);
        console.log("Service Param :" + serviceParam);
        return serviceParam;
    };
}
