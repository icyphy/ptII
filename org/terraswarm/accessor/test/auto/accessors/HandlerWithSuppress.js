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
    input("suppress", {'value':false, 'type': "boolean"});
    input("produce");
    output("output", {'type':"number"});
}
var count;
var handleTimeout = function() {
    send('produce', true);
};
var inputHandler = function() {
    count = count + 1;
    if (!get('suppress')) {
        send('output', count);
    }
};
exports.initialize = function() {
	addInputHandler('produce', inputHandler);
    count = 0;
    setInterval(handleTimeout, 1000);
}
