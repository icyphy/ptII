// JavaScript module for outputting messages.
//
// Copyright (c) 2015 The Regents of the University of California.
// All rights reserved.

// Permission is hereby granted, without written agreement and without
// license or royalty fees, to use, copy, modify, and distribute this
// software and its documentation for any purpose, provided that the above
// copyright notice and the following two paragraphs appear in all copies
// of this software.

// IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
// FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE.

// THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
// INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
// ENHANCEMENTS, OR MODIFICATIONS.

/** JavaScript module for outputting messages.
 *  This implementation is attempting to be compatible with the console module in Node.js,
 *  http://nodejs.org/api/console.html.
 *  It requires the util module.
 * 
 *  @module console
 *  @author Edward A. Lee
 *  @version $$Id$$
 */
 
// Stop extra messages from jslint.  Note that there should be no
// space between the / and the * and global.
/*globals actor, exports, require */
"use strict";

// Requires the util module.
var util = require('util');

/** If the first argument is not "truthy", then throw an error that
 *  includes a (formatted) message given by the remaining arguments.
 *  @param assertion The value being asserted to be true.
 *  @param arguments One or more additional arguments to form an error message if
 *   the assertion is false, where the
 *   first argument is optionally a formatting specification string.
 */
exports.assert = function(assertion, message) {
    if (!assertion) {
        // Get an array of arguments excluding the first.
        var tail = Array.prototype.slice.call(arguments, 1);
        var formatted = util.format.apply(this, tail);
        throw new Error('ASSERTION FAILED: ' + formatted);
    }
};

/** Apply util.inspect to the specified object (possibly with options) and
 *  then report as done by log(). The optional options argument is an object
 *  that may contain the following fields:
 *  <ul>
 *  <li> showHidden - if true then non-enumerable properties will be shown as well. Defaults to false. </li>
 *  <li> depth - tells inspect how many times to recurse while formatting the object. Defaults to 2.
 *            Use null to get unbounded depth. </li>
 *  <li> colors - if true, then the output will be styled with ANSI color codes. Defaults to false. </li>
 *  <li> customInspect - if false, then custom inspect() functions defined on the objects being
 *             inspected won't be called. Defaults to true. </li>
 *  </ul>
 *  @param object Object to send to the log.
 *  @param options Options governing how the object is described.
 */
exports.dir = function(object, options) {
    var result = util.inspect(object, options);
    actor.log(result);
};

/** Same as console.log, but prefix the message with "ERROR: ".
 *  @param arguments One or more arguments to display in the log, where the
 *   first argument is optionally a formatting specification string.
 */
exports.error = function() {
    var formatted = util.format.apply(this, arguments);
    console.log('ERROR: ' + formatted);
};

/** Same as console.log.
 *  @param arguments One or more arguments to display in the log, where the
 *   first argument is optionally a formatting specification string.
 */
exports.info = exports.log;

/** Send a message to listeners of the JavaScript object.
 *  The first string can be a printf-style formatting string, followed by arguments
 *  to insert into the output, as for example:
 *  <pre>
 *     console.log('My count: %d', myCount);
 *  </pre>
 *  If the first string does not contain any formatting elements, then util.inspect()
 *  is applied to all arguments.
 *  @param arguments One or more arguments to display in the log, where the
 *   first argument is optionally a formatting specification string.
 */
exports.log = function() {
    var formatted = util.format.apply(this, arguments);
    actor.log(formatted);
};

/** Same as console.log, but prefix the message with "WARNING: " and send to stderr
 *  (if debugging is off).
 *  @param arguments One or more arguments to display in the log, where the
 *   first argument is optionally a formatting specification string.
 */
exports.warn = function() {
    var formatted = util.format.apply(this, arguments);
    actor.error('WARNING: ' + formatted);
};

// Local variable for storing times.
var _times = {};

/** Record the current time using the specified label for use by a later call to timeEnd().
 *  @param label The label for the time point.
 */
exports.time = function(label) {
    _times[label] = Date.now();
};

/** Log the time elapsed since the last call to time() that 
 *  gave the same label (using the log() function).
 *  @param label The label for the starting time point.
 */
exports.timeEnd = function(label) {
    var start = _times[label];
    if (!start) {
        throw 'No time found for label: ' + label;
    }
    this.log('Time since %s: %dms', label, Date.now() - start);
};

/** Send a stack trace to the actor listener window or to stderr
 *  (if debugging is off) prefixed by "TRACE: " and any supplied
 *  (formatted) message.
 *  @param arguments One or more arguments to display in the log, where the
 *   first argument is optionally a formatting specification string.
 */
exports.trace = function() {
    var formatted = util.format.apply(this, arguments);
    actor.error('TRACE: ' + (new Error(formatted)).stack);
};
