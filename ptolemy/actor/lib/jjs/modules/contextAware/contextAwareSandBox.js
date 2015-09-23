var ContextAwareHelper = Java.type('ptolemy.actor.lib.jjs.modules.contextAware.ContextAwareHelper');
var helper = new ContextAwareHelper();

/** A temporary function that returns existing list of service names
 */
exports.services = function() {
    return Java.from(helper.availableServices());
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
