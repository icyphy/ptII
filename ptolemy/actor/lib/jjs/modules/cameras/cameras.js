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
 * Module to access camera hardware on the host.
 *
 * This implementation uses the webcam-capture package by
 * Bartosz Firyn (SarXos), available from:
 *    https://github.com/sarxos/webcam-capture
 *
 * @module camera
 * @author Edward A. Lee
 */
 
// Stop extra messages from jslint.  Note that there should be no
// space between the / and the * and global.
/*globals Java, exports, require, util */
/*jshint globalstrict: true*/
"use strict";

var CameraHelper = Java.type('ptolemy.actor.lib.jjs.modules.cameras.CameraHelper');
var EventEmitter = require('events').EventEmitter;

////////////////////////////////////////////////////////////
//// Functions provided in this module.

/** Return an array of camera names for cameras currently available
 *  on the current host. This array includes a special name "default camera",
 *  which represents the system default camera, if there is one.
 *  @return An array of names, or null if there are no cameras.
 */
exports.cameras = function() {
    // The Java.from() Nashorn extension converts a Java array into a JavaScript array.
    return Java.from(CameraHelper.cameras());
};

/** Return the name of the default camera on the current host, or null
 *  if there is none.
 *  @return A camera name.
 */
exports.defaultCamera = function() {
    return CameraHelper.defaultCamera();
};

////////////////////////////////////////////////////////////
//// Classes provided in this module.

/** Construct an instance of an Camera object type. To capture an image from
 *  the default camera, you can do this:
 *  <pre>
 *     var cameras = require("cameras");
 *     var camera = new cameras.Camera();
 *     camera.open();
 *     var image = camera.snapshot();
 *     camera.close();
 *  </pre>
 *  The image will be a binary object. This object can be sent to an output
 *  port displayed or otherwise further processed. To capture every image
 *  from the camera, you can do this:
 *  <pre>
 *     var cameras = require("cameras");
 *     var camera = new cameras.Camera();
 *     camera.on('image', function(image) { ... handle the image ... });
 *     camera.open();
 *     ...
 *     camera.close();
 *  </pre>
 *  An instance of this object type implements the following functions:
 *  <ul>
 *  <li> close(): Close the camera.
 *  <li> getViewSize(): Return the current view size for this camera as a JSON string, as in {"width":176, "height":144}.
 *  <li> on(event, handler): Specify an event handler for the camera.
 *  <li> open(): Open the camera.
 *  <li> setViewSize(size): Set the current view size for this camera. The argument can either be a JSON string or an object with a width and height field, as in for example {"width":176, "height":144}.
 *  <li> snapshot(): Return the last image recorded by the camera.
 *  <li> viewSizes(): Return an array of view sizes supported by this camera, each given as a JSON string of the form '{"width":176, "height":144}', for example.
 *  </ul>
 *  An instance of this object emits the following events:
 *  <ul>
 *  <li> "opened": The camera has been opened.
 *  <li> "image": A new image has been obtained.
 *  <li> "closed": The camera has been closed.
 *  </ul>
 *  @param name The camera name, or null to use the default camera.
 */
exports.Camera = function(name) {
    this.helper = new CameraHelper(this, name);
};
util.inherits(exports.Camera, EventEmitter);

/** Close the camera, stopping any image acquisition.
 */
exports.Camera.prototype.close = function() {
    this.helper.close();
};

/** Return the current view size for this camera, a JSON string
 *  as in {"width":176, "height":144}.
 *  @return A JSON string representing the current view size.
 */
exports.Camera.prototype.getViewSize = function() {
    var spec = this.helper.getViewSize();
    return spec;
};

/** Open the camera, initiating emission of the 'image' event each
 *  time the camera obtains a new image.
 */
exports.Camera.prototype.open = function() {
    this.helper.open();
};

/** Set the current view size for this camera.
 *  The argument can either be a JSON string or an object with a width and
 *  height field, as in for example {"width":176, "height":144}.
 *  @param size A view size.
 */
exports.Camera.prototype.setViewSize = function(size) {
    if (typeof size === 'string') {
        size = JSON.parse(size);
    }
    this.helper.setViewSize(size);
};

exports.Camera.prototype.snapshot = function() {
    return this.helper.snapshot();
};

/** Return an array of view sizes supported by this camera,
 *  each given as a JSON string of the form '{"width":176, "height":144}', for example.
 *  @return An array of strings representing available view sizes.
 */
exports.Camera.prototype.viewSizes = function() {
    // The Java.from() Nashorn extension converts a Java array into a JavaScript array.
    return Java.from(this.helper.viewSizes());
};
