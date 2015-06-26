exports.setup = function() {
   extend(this, 'Derived');
   output('out2');

}

//Override input Handler of base. try to access fields defined in
//base and derived
exports.inputHandler = function() {
   send('out1', exports.baseField);
   send('out2', exports.derivedField);
}

exports.initialize = function() {
   Object.getPrototypeOf(exports).initialize.apply(this);
}



