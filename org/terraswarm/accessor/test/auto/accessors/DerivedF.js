// Derived class that sends the trigger input to in1.
exports.setup = function() {
   this.extend('BaseD');
   this.input('trigger');
}

exports.fire = function() {
    var value = this.get('trigger');
    if (value !== null) {
        this.send('in1', value);
    }
    this.ssuper.fire();
}

