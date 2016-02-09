exports.setup = function () {
    this.input('in1');
    this.output('out1');
}

var handle;

exports.initialize = function () {
    // Be sure to refer to the input handler using this.exports
    // so that it can be overridden in derived classes. If you just
    // use exports.inputHandler, it will not be overridden.
    handle = this.addInputHandler('in1', this.exports.inputHandler.bind(this));
}

exports.inputHandler = function () {
    this.send('out1', this.exports.baseField);
}

exports.wrapup = function () {
    this.removeInputHandler(exports.handle);
}

exports.baseField = 1;
