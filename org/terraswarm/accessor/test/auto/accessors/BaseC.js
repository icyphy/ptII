exports.setup = function() {
   input('in1');
   output('out1');
}

var handle;

exports.initialize = function() {
   handle = addInputHandler('in1', this.inputHandler);
}

exports.inputHandler = function() {
   send('out1', this.baseField);
}

exports.wrapup = function() {
   removeInputHandler(exports.handle);
}

exports.baseField = 1;
