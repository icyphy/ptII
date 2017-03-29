// Below is the copyright agreement for the Ptolemy II system.
//
// Copyright (c) 2017 The Regents of the University of California.
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
//
// Ptolemy II includes the work of others, to see those copyrights, follow
// the copyright link on the splash page or see copyright.htm.

/** A Nashorn process module that implements a subset of the Node process module.
 *
 *  @module process
 *  @author Christopher Broosk
 *  @version $$Id$$
 */

// Stop extra messages from jslint.  Note that there should be no
// space between the / and the * and global.
/*globals Java, exports, print, require */
/*jshint globalstrict: true, moz: true*/
"use strict";

var env = {};

// Populate the env Object.
var System = Java.type('java.lang.System');
// 'for each' is a Nashorn extension, so jsdoc will complain.
// See https://wiki.openjdk.java.net/display/Nashorn/Nashorn+extensions
for each (var javaVariable in System.env.entrySet()) {
    env[javaVariable.key] = javaVariable.value;
}

// Use this to print out the env Object.
//for (var v in env) {
//    print(v + " = " + env[v]);
//}


/** Exit the process.
 *  It is usually a very bad idea to call this function.
 *
 *  If the ptolemy.ptII.exitAfterWrapup or the
 *  ptolemy.ptII.doNotExit properties are not set, then call
 *  System.exit().
 *  Ptolemy code should call this method instead of directly calling
 *  System.exit() so that we can test code that would usually exit.
 *
 *  @param code If non-zero, then typically an error.
 */
exports.exit = function(code) {
    var StringUtilities = Java.type('ptolemy.util.StringUtilities');
    print("ptolemy/actor/lib/jjs/modules/process/process.js: called exit()");
    StringUtilities.exit(code);
};

exports.env = env;
