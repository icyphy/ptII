// Copyright (c) 2015-2017 The Regents of the University of California.
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
 * Module to display an image.
 *
 * @module imageDisplay
 * @author Christopher Brooks
 * @version $$Id$$
 */

// Stop extra messages from jslint.  Note that there should be no
// space between the / and the * and global.
/*globals actor, Java, exports, require, util */
/*jshint globalstrict: true*/
"use strict";

var ImageDisplayHelper = Java.type('ptolemy.actor.lib.jjs.modules.imageDisplay.ImageDisplayHelper');
var EventEmitter = require('events').EventEmitter;

// util.inherits(exports.Camera, EventEmitter);

exports.ImageDisplay = function (name) {
    this.helper = new ImageDisplayHelper();
};

/** Display an image on the console.
 *  @param image The image
 */
exports.ImageDisplay.prototype.displayImage = function (image) {
    this.helper.displayImage(image);
};
