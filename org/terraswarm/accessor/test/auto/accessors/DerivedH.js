// Derived class that sets the value of in1, which has type JSON.
exports.setup = function() {
   this.extend('BaseD');
   this.input('in1', {'type':'JSON', 'value':'{"foo":42}'});
   this.input('trigger');
}

exports.fire = function() {
    var value = this.get('trigger');
    if (value !== null) {
        this.setDefault('in1', value);
    }
    exports.ssuper.fire.call(this);
}

