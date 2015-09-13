exports.setup = function() {
   extend('BaseA');
   input('in2');
   output('output');
   console.log('derived set up');
}

exports.initialize = function() {
   this.ssuper.initialize();
   console.log('derived init');
   addInputHandler('in1', function() {
      send('output', get('in1'));
   });
}

exports.wrapup = function() {
   console.log('derived wrapped up');
}

