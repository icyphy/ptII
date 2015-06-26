exports.setup = function() {
   input('in1');
   output('output');
}

var handle;

exports.initialize = function() {
   handle = addInputHandler('in1', exports.inputHandler);
}

exports.inputHandler = function() {
   console.log('sending true');
   send('output', true);
}


exports.wrapup = function() {
   console.log('base wrapped up');
   removeInputHandler(exports.handle);
}

