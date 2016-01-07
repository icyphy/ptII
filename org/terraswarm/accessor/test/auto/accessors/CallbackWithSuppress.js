/** This accessor produces outputs periodically, at one second intervals,
 *  as long as the most recently received suppress input is not true.
 *  The 'output' is a count of the periods, starting with 1.
 */
exports.setup = function() {
    input('suppress', {'value':false, 'type':'boolean'});
    input('produce');
    output('output', {'type':'number'});
}
var count;
var handle;
exports.initialize = function() {
  count = 0;
  handle = setInterval(
    function() {
      // Send to my own input to trigger fire().
      send('produce', true);
    },
    1000);
}
exports.fire = function() {
  var isSuppressed = get('suppress');
  var toProduce = get('produce');
  if (toProduce) {
    count = count + 1;
    if (!isSuppressed) {
      send('output', count);
    }
  }
}
exports.wrapup = function() {
    if (handle) {
        clearInterval(handle);
    }
}
