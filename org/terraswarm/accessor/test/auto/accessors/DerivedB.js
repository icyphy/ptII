exports.setup = function() {
   this.extend('BaseB');
}
//Override input Handler of base
exports.inputHandler = function() {
   console.log('sending false');
   this.send('output', false);
}

exports.initialize = function() {
   this.ssuper.initialize.apply(this);
   printProperties(exports);
}

function printProperties(obj) {
   console.log('printing non-prototype properties');
   for (var i in obj) {
      if (obj.hasOwnProperty(i)) {
         console.log(i + ": " + obj[i].toString());
      }
   }
   console.log('printing protoytpe');
   for (var j in Object.getPrototypeOf(obj)) {
      console.log(j + ": " + obj[j].toString());
   }
}



