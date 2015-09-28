// Derived class to test hiding of ports and parameters.
exports.setup = function() {
   extend('DerivedI');
   input('in1', {'visibility':'expert'});
   input('trigger', {'visibility':'none'});
   input('anotherTrigger');
   parameter('test', {'visibility':'expert'});
}
