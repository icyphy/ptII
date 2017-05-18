// Below is the copyright agreement for the Ptolemy II system.
//
// Copyright (c) 2016-2016 The Regents of the University of California.
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
/** A module to detect faces.
 *
 *  This module provides an interface to OpenCV face detection.
 *
 *  To run, please point your browser to:
 *  <a href="https://accessors.org/hosts/browser/demo/faceDetector/faceDetector.html#in_browser">https://accessors.org/hosts/browser/demo/faceDetector/faceDetector.html</a>
 *
 *  This module uses the UC Irvine computer vision library; see <a href="https://accessors.org/hosts/browser/modules/cvlicense.txt#in_browser">https://accessors.org/hosts/browser/modules/cvlicense.txt#in_browser"></a>
 *
 *  Based on code from examples in:  <a href="http://ucisysarch.github.io/opencvjs/examples/face_detect.html#in_browser">http://ucisysarch.github.io/opencvjs/examples/face_detect.html</a>
 *
 *  @module faceDetector
 *  @author Sajjad Taheri, Ilga Akkaya, Elizabeth Osyk
 *  @version $$Id$$
 */

// Stop extra messages from jslint.  Note that there should be no
// space between the / and the * and global.
/*globals exports, Java */
/*jshint globalstrict: true */
"use strict";

////////////////////////////////////////////////////////////
//// Private variables.

var Filter;
try {
    Filter = Java.type('org.ptolemy.opencv.FaceRecognizer');
} catch (e) {
    throw new Error('Could not find the org.ptolemy.opencv.FaceRecognizer class, perhaps OpenCV was not installed. The OpenCV API can be downloaded from http://opencv.org.  Under Mac OS X, try:  :sudo port install opencv +python27 +java".See org/ptolemy/opencv/package.html for installation help.');
}
var filter = new Filter();

////////////////////////////////////////////////////////////
//// Functions provided in this module.

/** Return the detected faces rectangles
 *  @return An array of detected faces rectangles.
 */
exports.faceRectangles = function () {
    var rects = filter.getFaceRectangles();
    for (var i = rects.length - 1; i >= 0; i--) {
        var parsedObject = rects[i].toString().replace('{', '').replace('}', '').replace('x', ',').split(',');
        var rectangle = {
            x: Number(parsedObject[0].trim()),
            y: Number(parsedObject[1].trim()),
            width: Number(parsedObject[2].trim()),
            height: Number(parsedObject[3].trim()),
        };
        rectangles = [rectangle];
    }

    return rectangles;
};

/** Detect faces in an image and return the image with squares around the faces.
 *
 *  Options:
 *  MinFaceSize - The minimum face size.  (In pixels?)
 *  MaxFaceSize - The maximum face size.  (In pixels?)
 *
 *  Any unrecognized options are ignored.
 *  Note that previously applied options for a given filter will still be
 *  used, even if they are not set in this call.
 *  @param image The image or path to image to detect faces in.
 *  @param options An object whose fields specify filter options.
 *  @param callback The callback to invoke when the result image is ready.
 *   Needed since there may be a delay if the input image is loaded from a file.
 */
exports.filter = function (image, transform, options, callback) {
    image = image.asAWTImage();
    if (options) {
        for (var optionName in options) {
            // Look for a setter function for the option.
            var setter = 'set' + optionName;
            if (typeof filter[setter] === 'function') {
                // Invoke the setter function.
                filter[setter](options[optionName]);
            }
        }
    }
    // The second (null) argument declines to give a destination image.
    var result = filter.filter(image, transform);
    callback(result);
};


/** A list of available filters.
 */
exports.filters = ['eyes', 'faces'];

/** Return number of detected faces
 *  @return The number of detected faces.
 */
exports.numberOfFaces = function () {
    return filter.getFaceCount();
};

