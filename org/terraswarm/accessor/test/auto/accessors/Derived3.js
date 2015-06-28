exports.setup = function() {
   extend('Base3');
}

//Override input Handler of base
exports.inputHandler = function() {
   console.log('sending false');
   send('out1', this.derivedField);
}

exports.initialize = function() {
   Object.getPrototypeOf(exports).initialize.apply(this);
}

exports.derivedField = 2;

