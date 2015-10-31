// Below is the copyright agreement for the Ptolemy II system.
//
// Copyright (c) 2015 The Regents of the University of California.
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
var ContextAwareHelper = Java.type('ptolemy.actor.lib.jjs.modules.contextAwareTest.ContextAwareHelperTest');
var helper = new ContextAwareHelper();

/** A temporary function that returns existing list of service names
 */
exports.services = function() {
    return Java.from(helper.availableServices());
}

exports.gsnServices=function() {
	return Java.from(helper.getGsnOutput());
}
exports.paraimpuServices = function() {
	return Java.from(helper.getParaimpuOutput());
}

exports.firebaseServices = function() {
	return Java.from(helper.getFirebaseOutput());
}

exports.xmlToJson= function(response) {
	return Java.from(helper.convertXMLtoJSON(response));
}
exports.DiscoveryOfRESTService = DiscoveryOfRESTService;
/** A discovery "class" that gathers details of a particular REST service. Not used now.
 */
function DiscoveryOfRESTService() {
    var self = this;
    var serviceURL;
    /** Discover IoT services */
    this.discoverServices = function () {
        var currentService;
        currentService = helper.getSelectedService();
        console.log(currentService);
        serviceURL = helper.getSelectedServiceParameter(currentService);
        console.log(serviceURL);
        return serviceURL;
    };
}
