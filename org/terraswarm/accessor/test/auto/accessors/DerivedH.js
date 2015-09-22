// Derived class that sets the value of in1, which has type JSON.
exports.setup = function() {
   extend('BaseD');
   input('in1', {'type':'JSON', 'value':'{"foo":42}'});
   input('trigger');
}

exports.fire = function() {
    var value = get('trigger');
    if (value !== null) {
        setDefault('in1', value);
    }
    this.ssuper.fire();
}

