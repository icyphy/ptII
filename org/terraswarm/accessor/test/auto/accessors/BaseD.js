// Base class that sends the value of input in fire.
exports.setup = function() {
   input('in1');
   output('output');
}

exports.fire = function() {
    var value = get('in1');
    send('output', value);
}
