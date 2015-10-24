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
