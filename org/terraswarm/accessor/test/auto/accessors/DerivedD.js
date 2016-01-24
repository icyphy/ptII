// Derived class the changes the type and default value of in1.
exports.setup = function() {
   this.extend('BaseD');
   this.input('in1', {'value':42, 'type':'int'});
   this.input('trigger');
}



