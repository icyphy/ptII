exports.setup = function() {
   this.input('in1');
   this.output('out1');
}

var handle;

exports.initialize = function() {
   handle = this.addInputHandler('in1', this.inputHandler);
}

exports.inputHandler = function() {
   this.send('out1', this.baseField);
}

exports.wrapup = function() {
   this.removeInputHandler(exports.handle);
}

exports.baseField = 1;
