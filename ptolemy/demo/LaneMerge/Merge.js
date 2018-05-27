/** Specialized accessor emulating a sensor that, when an input arrives at the
 *  **merge** input port, replies on the **grant** output port when it is OK to merge. 
 *
 *  @accessor FreewayRamp
 *  @author Edward A. Lee (eal@eecs.berkeley.edu)
 */
var WebSocket = require('@accessors-modules/web-socket-client');

exports.setup = function() {
    this.input('merge');
    this.output('grant');
}
exports.initialize = function() {
    var thiz = this;
    var client = new WebSocket.Client({'host': 'localhost', 'port': 8080});
    
    this.addInputHandler('merge', function() {
        client.send(true);
    });
    client.on('message', function(message) {
        this.send('grant', true);
    });
    client.open();
}