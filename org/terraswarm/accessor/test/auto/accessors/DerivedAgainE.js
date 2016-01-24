// Derived class to test hiding of ports and parameters.
exports.setup = function() {
   this.extend('DerivedI');
   this.input('in1', {'visibility':'expert'});
   this.input('trigger', {'visibility':'none'});
   this.input('anotherTrigger');
   this.parameter('test', {'visibility':'expert'});
}
