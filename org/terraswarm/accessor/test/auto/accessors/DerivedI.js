// Derived class that sends to in1, which has type JSON.
exports.setup = function() {
   this.extend('BaseD');
   this.input('in1', {'type':'JSON', 'value':'{"foo":42}'});
   this.input('trigger');
   this.parameter('test', {'type':'int', 'value':42});
}

exports.fire = function() {
    var value = this.get('trigger');
    if (value !== null) {
        this.send('in1', value);
    }
    exports.ssuper.fire.call(this);
}
