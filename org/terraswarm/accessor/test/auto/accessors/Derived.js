exports.setup = function() {
   extend(this, 'Base');
}

//Override input Handler of base
exports.inputHandler = function() {
   console.log('sending false');
   send('out1', false);
}

exports.initialize = function() {
   Object.getPrototypeOf(exports).initialize.apply(this);
}

exports.derivedField = 2;

