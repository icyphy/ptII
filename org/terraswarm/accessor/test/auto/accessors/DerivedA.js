exports.setup = function() {
   this.extend('BaseA');
   this.input('in2');
   this.output('output');
   console.log('derived set up');
}

exports.initialize = function() {
   this.ssuper.initialize();
   console.log('derived init');
   this.addInputHandler('in1', function() {
      this.send('output', this.get('in1'));
   });
}

exports.wrapup = function() {
   console.log('derived wrapped up');
}

