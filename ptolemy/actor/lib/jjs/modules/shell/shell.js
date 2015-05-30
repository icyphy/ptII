/**
 * Module supporting shell commands.
 * @module shell
 * @authors: Armin Wasicek
 */

var EventEmitter = require('events').EventEmitter;
var ShellHelper = Java.type('ptolemy.actor.lib.jjs.modules.shell.ShellHelper');
var helper = null;


/** Construct an instance of a shell that executes the specified command and
 *  redirects stdin and stdout to the accessor via function <i>write</i> and
 *  event <i>'message'</i>.
 *  @param options A javascript object specifying the options for the invocation.
 */
exports.Shell = function(options) {
	helper = ShellHelper.createShell(this, options['cmd']);
}
util.inherits(exports.Shell, EventEmitter);


/** Wraps the write function to send input to the process' stdin.
 *  @param data The input data to be sent to stdin.
 */
exports.Shell.prototype.write = function(data) {
	if(helper)  {
		helper.write(data);
	}
}


/** Starts up the process to execute the command. Call after all callbacks have 
 *  been registered.
 */
exports.Shell.prototype.start = function () {
	helper.start();
}


/** Wrap up the execution. Terminate the process and the reader thread and clean 
 *  up.
 */
exports.Shell.prototype.wrapup = function()  {
	helper.wrapup();
}


