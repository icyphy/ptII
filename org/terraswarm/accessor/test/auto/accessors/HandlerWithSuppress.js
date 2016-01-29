/** This accessor produces outputs periodically, at one second intervals,
 *  as long as the most recently received suppress input is not true.
 *  The output is a count of the periods, starting with 1.
 *  @accessor HandlerWithSuppress
 *  @input suppress If true, then suppress the output triggered by a callback.
 *  @input produce An event here is produced as an output, unless suppressed.
 *  @output output The count of the firing.
 *  @author Edward A. Lee
 */
exports.setup = function() {
    this.input("suppress", {'value':false, 'type': "boolean"});
    this.input("produce");
    this.output("output", {'type':"number"});
}
var count;
var handleTimeout = function() {
    this.send('produce', true);
};
var inputHandler = function() {
    count = count + 1;
    if (!this.get('suppress')) {
        this.send('output', count);
    }
};
exports.initialize = function() {
	this.addInputHandler('produce', inputHandler.bind(this));
    count = 0;
    setInterval(handleTimeout.bind(this), 1000);
}
