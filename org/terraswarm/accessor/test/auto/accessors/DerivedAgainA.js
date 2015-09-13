exports.setup = function() {
   extend('DerivedC');
   output('out2');
}

//Override input Handler of base. try to access fields defined in
//base and derived
exports.inputHandler = function() {
   // Invoke the base class inputHandler, defined two levels up.
   Object.getPrototypeOf(exports).inputHandler.apply(this);
   send('out2', this.derivedField);
}



