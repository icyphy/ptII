// JavaScript module for outputting messages.
// This implementation is attempting to be compatible with the console module in Node.js,
// http://nodejs.org/api/console.html.
//
// Author: Edward A. Lee
// Copyright: http://terraswarm.org/accessors/copyright.txt
//
// Requires the util module.
var util = require('util');

////////////////////
// Send a message to listeners of the JavaScript object.
// The first string can be a printf-style formatting string, followed by arguments
// to insert into the output, as for example:
//    console.log('My count: %d', myCount);
// If the first string does not contain any formatting elements, then util.inspect()
// is applied to all arguments.
exports.log = function() {
    var formatted = util.format.apply(this, arguments);
    actor.log(formatted);
};

////////////////////
// Same as console.log.
exports.info = exports.log;

////////////////////
// Same as console.log, but prefix the message with "ERROR: " and send to stderr
// (if debugging is off).
exports.error = function() {
    var formatted = util.format.apply(this, arguments);
    actor.error('ERROR: ' + formatted);
};

////////////////////
// Same as console.log, but prefix the message with "WARNING " and send to stderr
// (if debugging is off).
exports.warn = function() {
    var formatted = util.format.apply(this, arguments);
    actor.error('WARNING: ' + formatted);
};

////////////////////
// Apply util.inspect to the specified object (possibly with options) and
// then report as done by log(). The optional options argument is an object
// that may contain the following fields:
// - showHidden - if true then non-enumerable properties will be shown as well. Defaults to false.
// - depth - tells inspect how many times to recurse while formatting the object. Defaults to 2.
//           Use null to get unbounded depth.
// - colors - if true, then the output will be styled with ANSI color codes. Defaults to false.
// - customInspect - if false, then custom inspect() functions defined on the objects being
//            inspected won't be called. Defaults to true.
exports.dir = function(object, options) {
    var result = util.inspect(object, options);
    actor.log(result);
};

// Local variable for storing times.
var _times = {};

////////////////////
// Record the current time using the specified label for use by a later call to timeEnd().
exports.time = function(label) {
    _times[label] = Date.now();
};

////////////////////
// Log the time elapsed since the last call to time() that 
// gave the same label (using the log() function).
exports.timeEnd = function(label) {
    var start = _times[label];
    if (!start) {
        throw 'No time found for label: ' + label;
    }
    this.log('Time since %s: %dms', label, Date.now() - start);
};

////////////////////
// Send a stack trace to the actor listener window or to stderr
// (if debugging is off) prefixed by "TRACE: " and any supplied
// (formatted) message.
exports.trace = function() {
    var formatted = util.format.apply(this, arguments);
    actor.error('TRACE: ' + (new Error(formatted)).stack);
};

////////////////////
// If the first argument is not "truthy", then throw an error that
// includes a (formatted) message given by the remaining arguments.
exports.assert = function(assertion, message) {
    if (!assertion) {
        // Get an array of arguments excluding the first.
        var tail = Array.prototype.slice.call(arguments, 1);
        var formatted = util.format.apply(this, tail);
        throw new Error('ASSERTION FAILED: ' + formatted);
    }
};
