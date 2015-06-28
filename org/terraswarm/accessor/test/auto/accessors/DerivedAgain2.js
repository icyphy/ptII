exports.setup = function() {
   extend('Derived3');
   output('out2');
}

//Override input Handler of base. try to access fields defined in
//base and derived
exports.inputHandler = function() {
   // Invoke the base class inputHandler, defined two levels up.
   // Note that in ECMA 6, we can just say super.inputHandler().
   this.ssuper.inputHandler();
   send('out2', this.derivedField);
}



