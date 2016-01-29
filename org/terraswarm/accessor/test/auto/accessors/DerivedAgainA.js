/** Doubly derived accessor that overrides a function (inputHandler) of the base
 *  two levels up, it should produce an output value of 2 on both outputs.
 *  @accessor TestDerivedC
 *  @author Edward A. Lee
 */ 
exports.setup = function() {
   this.extend('DerivedC');
   this.output('out2');
}

//Override input Handler of base. try to access fields defined in
//base and derived
exports.inputHandler = function() {
   // Invoke the base class inputHandler, defined two levels up.
   Object.getPrototypeOf(exports).inputHandler.call(this);
   this.send('out2', this.derivedField);
}



