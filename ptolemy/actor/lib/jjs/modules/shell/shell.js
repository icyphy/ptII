
var EventEmitter = require('events').EventEmitter;
var ShellHelper = Java.type('ptolemy.actor.lib.jjs.modules.shell.ShellHelper');
var helper = null;

exports.Shell = function(options) {
	helper = ShellHelper.createShell(this, options['cmd']);
}
util.inherits(exports.Shell, EventEmitter);


exports.Shell.prototype.write = function(data) {
	if(helper)  {
		helper.write(data);
	}
}

exports.Shell.prototype.start = function (data) {
	helper.start();
}

exports.Shell.prototype.wrapup = function()  {
	helper.wrapup();
}


