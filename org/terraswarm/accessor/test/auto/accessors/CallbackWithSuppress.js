/** This accessor produces outputs periodically, at one second intervals,
 *  as long as the most recently received suppress input is not true.
 *  The 'output' is a count of the periods, starting with 1.
 */
exports.setup = function() {
    this.input('suppress', {'value':false, 'type':'boolean'});
    this.input('produce');
    this.output('output', {'type':'number'});
}
var count;
var handle;
exports.initialize = function() {
  count = 0;
  handle = setInterval(
    function() {
      // Send to my own input to trigger fire().
      this.send('produce', true);
    },
    1000);
}
exports.fire = function() {
  var isSuppressed = this.get('suppress');
  var toProduce = this.get('produce');
  if (toProduce) {
    count = count + 1;
    if (!isSuppressed) {
      this.send('output', count);
    }
  }
}
exports.wrapup = function() {
    if (handle) {
        clearInterval(handle);
    }
}
