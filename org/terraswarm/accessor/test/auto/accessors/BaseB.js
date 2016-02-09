exports.setup = function () {
    this.input('in1');
    this.output('output');
}

exports.initialize = function () {
    this.addInputHandler('in1', this.exports.inputHandler.bind(this));
}

exports.inputHandler = function () {
    console.log('sending true');
    this.send('output', true);
}
