// This script defines an interface for an accessor that
// sends an output after receiving an input.

exports.setup = function() {
    this.extend('utilities/MutableBase');
    this.implement('ValueToSend');
};
