exports.setup = function () {
    this.extend('BaseA');
    this.input('in2');
    this.output('output');
    console.log('derived set up');
}

exports.initialize = function () {
    exports.ssuper.initialize.call(this);
    console.log('derived init');
    var self = this;
    this.addInputHandler('in1', function () {
        self.send('output', self.get('in1'));
    });
}

exports.wrapup = function () {
    console.log('derived wrapped up');
}
