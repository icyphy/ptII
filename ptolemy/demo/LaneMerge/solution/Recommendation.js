/** Specialized accessor that retrieves a recommended ramp speed from a
 *  roadside service, assumed to have a RESTful interface.
 *
 *  @accessor FreewayRamp
 *  @author Edward A. Lee (eal@eecs.berkeley.edu)
 */
var httpClient = require('@accessors-modules/http-client');

exports.setup = function() {
    this.input('enteringRamp');
    this.output('desiredSpeed', {'type':'number'});
}
 
exports.initialize = function() {
    var thiz = this;
    this.addInputHandler('enteringRamp', function() {
    	httpClient.get('http://localhost:8081', function(message) {
         	var speed = JSON.parse(message.body);
         	thiz.send('desiredSpeed', speed);
    	});
    });
}
