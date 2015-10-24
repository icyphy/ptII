var GDPHelper = Java.type('ptolemy.actor.lib.jjs.modules.gdp.GDPHelper');

exports.GDP = function(name, iomode) {
    this.helper = new GDPHelper(name, iomode);
    return this.helper;
}

exports.GDP.prototype.read = function(recno) {
    var data = this.helper.read(recno);
    return data;
}

exports.GDP.prototype.append = function(data) {
    this.helper.append(data);
}

exports.GDP.prototype.subscribe = function(startrec, numrecs) {
    this.helper.subscribe(this, startrec, numrecs);
}

exports.GDP.prototype.get_next_data = function(timeout_msec) {
    return this.helper.get_next_data(timeout_msec);
}
