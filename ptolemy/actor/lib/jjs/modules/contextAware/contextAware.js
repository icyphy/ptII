var ContextAwareHelper = Java.type('ptolemy.actor.lib.jjs.modules.contextAware.ContextAwareHelper');


exports.DiscoveryOfRESTService = DiscoveryOfRESTService;

/** A discovery "class" that gathers details of a particular REST service
 */
function DiscoveryOfRESTService() {
   var self = this;
   var helper = new ContextAwareHelper();
   var ipHostOfMiddleware;
   /** Discover IoT middleware properties */
   this.discoverServices = function () {
        var currentMW;
        currentMW = helper.getMiddleware();
        console.log(currentMW);
        ipHostOfMiddleware = helper.getParameterData(currentMW);
        console.log(ipHostOfMiddleware);
        return ipHostOfMiddleware;
    };
}
