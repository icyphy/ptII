// Base class that sends the value of input in fire.
exports.setup = function () {
    this.input('in1');
    this.output('output');
}

exports.fire = function () {
    var value = this.get('in1');
    this.send('output', value);
}
