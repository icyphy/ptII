// Below is the copyright agreement for the Ptolemy II system.
//
// Copyright (c) 2015-2016 The Regents of the University of California.
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

/**
 * Module to access the Global Data Plane (GDP).
 *
 * See <a href="https://www.terraswarm.org/accessors/wiki/Main/GDPWithAccessors">https://www.terraswarm.org/accessors/wiki/Main/GDPWithAccessors</a>
 *
 * @module GDP
 * @author Edward A. Lee, Christopher Brooks
 * @version $$Id$$
 */

// Stop extra messages from jslint.  Note that there should be no
// space between the / and the * and global.
/*globals Java, exports, util */
/*jslint nomen: true */
/*jshint globalstrict: true*/
"use strict";

var GDPHelper = Java.type('ptolemy.actor.lib.jjs.modules.gdp.GDPHelper');
var EventEmitter = require('events').EventEmitter;

/** Open or create a log with the specified name and mode using the specified GDP logd server.
 *  This function should be called using "new" to create a new object.
 *  The returned object subclasses EventEmitter.
 *  You can register handlers for event 'data'.
 *  The event 'data' will be emitted with the body of a log entry whenever
 *  data is published to the log. At this time, only string data is supported.
 *  You can invoke the object's append() function to append data to the log.
 *
 *  FIXME: This function is misnamed. Should be Log.
 *  FIXME: Follow the pattern of the WebSocket accessors and module to support more data types.
 *
 *  @param name The name of the GDP log.  Dot-separated reverse notation preferred, such as org.terraswarm.demo.DemoName.
 *  @param iomode Opening mode (0: for internal use only, 1: read-only, 2: read-append, 3: append-only).
 *  @param logdName  Name of the logd server that will handle the request.
 */
exports.GDP = function (name, iomode, logdname) {
    this.helper = new GDPHelper(this, name, iomode, logdname);
};
util.inherits(exports.GDP, EventEmitter);

/** Append data to the already open log.
 *  @param {string} data The data to be appended
 */
exports.GDP.prototype.append = function (data) {
    this.helper.append(data);
};

/** Close the log.
 */
exports.GDP.prototype.close = function () {
    this.helper.close();
};

/** Get the next data.
 *  @param {int} timeout The timeout in milliseconds.
 *  @return {string} the next data.
 */
exports.GDP.prototype.getNextData = function (timeout) {
    // FIXME: The timeout should be a long.
    return this.helper.getNextData(timeout);
};

/** Read a record.
 *  @param {int} recno The record number.  GDP records start with 1.
 *  @return {string} The data.
 */
exports.GDP.prototype.read = function (recno) {
    var data = this.helper.read(recno);
    return data;
};

/** Set the debug level.
 *  @param {string} debugLevel The debug level.  "*=10" will set the
 *  level to 10 for all modules.  See gdp/README-developers.md for
 *  details.  The value is typically
 *  <code><i>pattern</i>=<i>level</i></code>, for example
 *  <code>gdplogd.physlog=39</code>.  To see the patterns, use the
 *  "what" command or <code>strings $PTII/lib/libgdp* | grep
 *  '@(#)'</code>.  Use <code>*=40</code> to set the debug level to 40
 *  for all components. The value of level is not usually over 127.
 *  Values over 100 may modify the behavior.
 */
exports.GDP.prototype.setDebugLevel = function (debugLevel) {
    this.helper.setDebugLevel(debugLevel);
};

/** Subscribe to the log. This will cause 'data' events to start to be
 *  emitted each time something is published to the log.
 *
 *  FIXME: What is the meaning of the arguments?  They make no sense for subscription.
 *
 *  @param {int} startrec The starting record. The first record is record 1.
 *  @param {int} numrecs The number of records
 *  @param {int} timeout The timeout in milliseconds.
 */
exports.GDP.prototype.subscribe = function (startrec, numrecs, timeout) {
    // FIXME: The timeout should be a long.
    this.helper.subscribe(startrec, numrecs, timeout);
};

/** Unsubscribe from the log. This will cause 'data' events to stop being
 *  emitted each time something is published to the log.
 */
exports.GDP.prototype.unsubscribe = function () {
    this.helper.unsubscribe();
};

/** Notify this object of a received message from the socket.
 *  This function attempts to interpret the message according to the
 *  receiveType, and emits a "message" event with the message as an argument.
 *  For example, with the default receiveType of 'application/json', it will
 *  use JSON.parse() to parse the message and emit the result of the parse.
 *  This function is called by the Java helper used by this particular
 *  implementation and should not be normally called by the user.
 *  @param message The incoming message.
 */
exports.GDP.prototype._notifyIncoming = function (data) {
    /* FIXME: Maybe useful when supporting more data types.
    if (this.receiveType == 'application/json') {
        try {
            message = JSON.parse(message);
        } catch (error) {
            this.emit('error', error);
            return;
        }
    }
    */
    // Assume the helper has already provided the correct type.
    this.emit("data", data);
};
