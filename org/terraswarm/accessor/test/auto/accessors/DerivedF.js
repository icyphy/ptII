// Derived class that sends the trigger input to in1.
exports.setup = function() {
   extend('BaseD');
   input('trigger');
}

exports.fire = function() {
    var value = get('trigger');
    if (value !== null) {
        send('in1', value);
    }
    this.ssuper.fire();
}

