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

/** Module offering OpenCV image processing.
 *
 *  The filter architecture follows the pattern defined by Jerry Huxtable
 *  in the JH Labs Java Image Processing library, available from:
 *    http://www.jhlabs.com/ip/filters
 *  and licensed under the Apache License, Version 2.0
 *  (http://www.apache.org/licenses/LICENSE-2.0).
 *  
 * To use, import the module:
 * var cv = require('computerVision');
 * 
 * To obtain a list of filters:
 * var filters = cv.filters;
 * 
 * Invoke a filter and handle the result.  For example, in an accessor with an 
 * input "image" and output "result", to run findEdges():
 * 
 * var self = this;
 * 
 * this.addinputHandler('input', function() {
 * 	var image = this.get('input');
 *  var options = {};
 *  options.cannyThreshold = 20;
 *  
 *  cv.filter(image, 'findEdges', options, function(result) {
 *  	self.send('output', result);
 *  });
 * });
 *
 * The module supports these transforms:
 * Filter.blur(options): Blur the image, optionally passing in options.blurSize (1-25).
 * Filter.dilate(options): Dilate the image, optionally passing in options.erosionSize (0-21).
 * Filter.erode(options): Erode the image, optionally passing in options.erosionSize (0-21).
 * Filter.findContours(options): Find contours of an image, optionally passing in options.cannyThreshold (10-150).
 * Filter.findEdges(options): Find edges of an image, optionally passing in options.cannyThreshold (10-150).
 * Filter.gaussianBlur(options): Blur the image, optionally passing in options.blurSize (1-25).
 * Filter.histogram(): Create a histogram from the image showing red, green and blue content.
 * Filter.makeBGRA(): Convert image to blue, green, red, alpha colorspace.
 * Filter.makeGray(): Convert image to grayscale.
 * Filter.makeHSV(): Convert image to hue, saturation, value colorspace.
 * Filter.makeYUV(): Convert image to luminance, chroma colorspace. 
 *
 *  @module computerVision
 *  @author Elizabeth Osyk
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
    Filter = Java.type('org.ptolemy.opencv.ComputerVision');
} catch (e) {
    throw new Error('Could not find the org.ptolemy.opencv.ComputerVision class, perhaps OpenCV was not installed. The OpenCV API can be downloaded from http://opencv.org.  Under Mac OS X, try:  :sudo port install opencv +python27 +java".See org/ptolemy/opencv/package.html for installation help.');
}
var filter = new Filter();

////////////////////////////////////////////////////////////
//// Functions provided in this module.

/** Apply a filter to an image using (optional) options.
 *  Any unrecognized options are ignored.
 *  Note that previously applied options for a given filter will still be
 *  used, even if they are not set in this call.
 *  @param image The image to filter.
 *  @param transform The name of the transform to apply to the image.
 *  @param options An object whose fields specify filter options.
 *  @param callback The callback to invoke once the result image is ready.
 */
exports.filter = function (image, transform, options, callback) {
    image = image.asAWTImage();

    if (options) {
        for (var optionName in options) {
            var setterName = optionName.substring(0, 1).toUpperCase() +
                optionName.substring(1);

            // Look for a setter function for the option.
            var setter = 'set' + setterName;
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

/** Return an array of filters provided by this module.
 */
exports.filters = function () {
    return ['blur', 'dilate', 'erode', 'findEdges', 'findContours',
        'gaussianBlur', 'histogram', 'makeBGRA', 'makeGray', 'makeHSV',
        'makeYUV', 'medianBlur'
    ];
};
