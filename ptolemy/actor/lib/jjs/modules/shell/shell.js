// Below is the copyright agreement for the Ptolemy II system.
//
// Copyright (c) 2015 The Regents of the University of California.
// All rights reserved.
//
// Permission is hereby granted, without written agreement and without
// license or royalty fees, to use, copy, modify, and distribute this
// software and its documentation for any purpose, provided that the above
// copyright notice and the following two paragraphs appear in all copies
// of this software.
//
// IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
// FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE.
//
// THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
// INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
// ENHANCEMENTS, OR MODIFICATIONS.
//
// Ptolemy II includes the work of others, to see those copyrights, follow
// the copyright link on the splash page or see copyright.htm.

/**
 * Module supporting shell commands.
 * @module shell
 * @authors Armin Wasicek
 * @version $$Id$$
 */

// Stop extra messages from jslint.  Note that there should be no
// space between the / and the * and global.
/*globals Java, exports, require, util */
/*jshint globalstrict: true */
"use strict";

var EventEmitter = require('events').EventEmitter;
var ShellHelper = Java.type('ptolemy.actor.lib.jjs.modules.shell.ShellHelper');
var helper = null;


/** Construct an instance of a shell that executes the specified command and
 *  redirects stdin and stdout to the accessor via function <i>write</i> and
 *  event <i>'message'</i>.
 *  @param options A javascript object specifying the options for the invocation.
 */
exports.Shell = function(options) {
	helper = ShellHelper.createShell(this, options.cmd);
};
util.inherits(exports.Shell, EventEmitter);


/** Wraps the write function to send input to the process' stdin.
 *  @param data The input data to be sent to stdin.
 */
exports.Shell.prototype.write = function(data) {
	if (helper)  {
		helper.write(data);
	}
};

/** Starts up the process to execute the command. Call after all callbacks have 
 *  been registered.
 */
exports.Shell.prototype.start = function () {
	helper.start();
};

/** Wrap up the execution. Terminate the process and the reader thread and clean 
 *  up.
 */
exports.Shell.prototype.wrapup = function()  {
	helper.wrapup();
};


