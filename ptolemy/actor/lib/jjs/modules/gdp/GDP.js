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
// Ptolemy II includes the work of others, to see those copyrights, follow
// the copyright link on the splash page or see copyright.htm.

/**
 * Module to access the Global Data Plane.
 * 
 * @module GDP
 * @author Edward A. Lee, Christopher Brooks
 * @version $$Id$$
 */

// Stop extra messages from jslint.  Note that there should be no
// space between the / and the * and global.
/*globals Java, exports */
/*jshint globalstrict: true*/
"use strict";

var GDPHelper = Java.type('ptolemy.actor.lib.jjs.modules.gdp.GDPHelper');

exports.GDP = function (name, iomode) {
    this.helper = new GDPHelper(name, iomode);
    return this.helper;
};

exports.GDP.prototype.read = function (recno) {
    var data = this.helper.read(recno);
    return data;
};

exports.GDP.prototype.append = function (data) {
    this.helper.append(data);
};

exports.GDP.prototype.setDebugLevel = function (debugLevel) {
    this.helper.setDebugLevel(debugLevel);
}

exports.GDP.prototype.subscribe = function (startrec, numrecs, timeout) {
    this.helper.subscribe(this, startrec, numrecs, timeout);
};

exports.GDP.prototype.getNextData = function (timeout_msec) {
    return this.helper.getNextData(timeout_msec);
};
