exports.setup = function() {
   this.input('in1');
   console.log('base set up');
}
exports.initialize = function() {
   console.log('base init');
}


exports.wrapupBase = function() {
   console.log('base wrapped up');
}
