exports.setup = function () {
    this.extend('DerivedC');
    this.output('out2');
}

//Override input Handler of base. try to access fields defined in
//base and derived
exports.inputHandler = function () {
    // Invoke the base class inputHandler, defined two levels up.
    // Note that in ECMA 6, we can just say super.inputHandler().
    exports.ssuper.inputHandler.call(this);
    this.send('out2', exports.derivedField);
}
