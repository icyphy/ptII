// Derived class that sends to in1, which has type JSON.
exports.setup = function() {
   extend('BaseD');
   input('in1', {'type':'JSON', 'value':'{"foo":42}'});
   input('trigger');
   parameter('test', {'type':'int', 'value':42});
}

exports.fire = function() {
    var value = get('trigger');
    if (value !== null) {
        send('in1', value);
    }
    this.ssuper.fire();
}

