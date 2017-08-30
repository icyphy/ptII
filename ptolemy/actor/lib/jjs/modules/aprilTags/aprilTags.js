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
Module to identify AprilTags in an image.

This code uses AprilTags Java code found in $PTII/edu/umich/eecs/april.

The AprilTags code developed by the APRIL robotics lab under the
direction of Edwin Olson (ebolson@umich.edu).

The Java implementation of the AprilTags detector is provided for
convenience as a demonstration.  It is an older version implemented in
Java that has been supplanted by a much better performing C version.
If your application demands better performance, you will need to
replace this implementation with the newer C version and using JNI or
JNA to interface the C version to Java.

For details about the C version, see
https://april.eecs.umich.edu/wiki/index.php/AprilTags-C

https://april.eecs.umich.edu/software/apriltag.html contains a set of
pregenerated tags as png and PostScript files.  However, these are of
low resolution.  To scale them, use linear interpolation to avoid blurring.

For example, with ImageMagik, use:

mogrify -scale 1000x1000 *.png; convert *.png tag36h11.pdf

Or, search the web for "tag 36H11".

In the Ptolemy tree, a sample file may be found at
$PTII/ptolemy/actor/lib/jjs/modules/aprilTags//demo/AprilTags/tag36_11_00586.pdf

@module aprilTags
@author Edward A. Lee
 */

// Stop extra messages from jslint.  Note that there should be no
// space between the / and the * and global.
/*globals exports, Java */
/*jshint globalstrict: true*/
"use strict";

////////////////////////////////////////////////////////////
//// Private variables.

var Filter = Java.type('edu.umich.eecs.april.image.AprilTagFilter');
var filter = new Filter();

////////////////////////////////////////////////////////////
//// Functions provided in this module.

/** Invoke the AprilTag detector on the specified image with the specified
 *  options and return the result.  Any unrecognized options are ignored.
 *  Note that previously applied options for a given filter will still be
 *  used, even if they are not set in this call.
 *  @param image The image to filter.
 *  @param options An object whose fields specify filter options.
 *  @return The filtered image.
 */
exports.filter = function (image, options) {
    var optionName, setter;
    image = image.asAWTImage();
    if (options) {
        for (optionName in options) {
            // jslint: avoid "The body of a for in should be wrapped
            // in an if statement to filter unwanted properties from
            // the prototype."
            if (options.hasOwnProperty(optionName)) {
                // Look for a setter function for the option.
                setter = 'set' + optionName;
                if (typeof filter[setter] === 'function') {
                    // Invoke the setter function.
                    filter[setter](options[optionName]);
                }
            }
        }
    }
    // The second (null) argument declines to give a destination image.
    return filter.filter(image, null);
};

/** Return an array of tags detected by the most recent call to filter().
 *  The returned value is null if there has been no call to filter().
 *  Otherwise, it is an array of objects with the following fields:
 *  <ul>
 *  <li> id: The ID of the detected tag.
 *  <li> center: An array with two doubles giving the center of the tag in pixel coordinates.
 *  <li> perimeter: An array with four arrays, each of which gives the x and y coordinates of
 *       a corner of the AprilTag, listed in a counter-clockwise direction.
 *  </ul>
 *  @return The detected tags.
 */
exports.tags = function () {
    return filter.tags();
};
