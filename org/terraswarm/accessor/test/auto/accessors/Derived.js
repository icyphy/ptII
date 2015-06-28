exports.setup = function() {
   extend('Base');
   input('in2');
   output('output');
   console.log('derived set up');
}

var handle;
exports.initialize = function() {
   Object.getPrototypeOf(exports).initialize.apply(this);
   console.log('derived init');
   handle = addInputHandler('in1', function() {
      send('output', get('in1'));
   });
}

exports.wrapup1 = function() {
   console.log('derived wrapped up');
   removeInputHandler(handle);
}

