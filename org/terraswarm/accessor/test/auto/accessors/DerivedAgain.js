exports.setup = function() {
   extend('Derived3');
   output('out2');
}

//Override input Handler of base. try to access fields defined in
//base and derived
exports.inputHandler = function() {
   send('out1', this.baseField);
   send('out2', this.derivedField);
}

exports.initialize = function() {
   Object.getPrototypeOf(exports).initialize.apply(this);
}



