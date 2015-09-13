exports.setup = function() {
   input('in1');
   output('output');
}

exports.initialize = function() {
   addInputHandler('in1', this.inputHandler);
}

exports.inputHandler = function() {
   console.log('sending true');
   send('output', true);
}
