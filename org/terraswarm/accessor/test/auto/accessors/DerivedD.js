// Derived class the changes the type and default value of in1.
exports.setup = function() {
   extend('BaseD');
   input('in1', {'value':42, 'type':'int'});
   input('trigger');
}



