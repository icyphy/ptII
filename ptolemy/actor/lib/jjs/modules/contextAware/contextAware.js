
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
 *  @copyright http://terraswarm.org/accessors/copyright.txt
 */

var ContextAwareHelper = Java
		.type('ptolemy.actor.lib.jjs.modules.contextAware.ContextAwareHelper');
var helper = new ContextAwareHelper();


////////////////////////////////////////////////////////////
////Functions provided in this module.
/**
 * Use a helper class to return a list of known services
 */
exports.services = function() {
	return Java.from(helper.availableServices());
}

exports.DiscoveryOfRESTService = DiscoveryOfRESTService;

/** A function that will take a user request and query a registry or invoke a discovery
 *  process to find the 'best' service that the context aware accessor can use. 
 *  This function is not used currently.
 */
function DiscoveryOfRESTService() {
	var self = this;
	var serviceParam;
	/** Discover IoT services */
	this.discoverServices = function() {
		var currentService;
		currentService = helper.getSelectedService();
		serviceParam = helper.getSelectedServiceParameter(currentService);
		console.log("Service Param :" + serviceParam);
		return serviceParam;
	};
}
