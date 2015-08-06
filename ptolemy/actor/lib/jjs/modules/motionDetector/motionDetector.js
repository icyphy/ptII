/** Module to detect motion in a sequence of images.'
 *  This module provides an interface to a motion detection implementation
 *  taken from the webcam-capture package by Bartosz Firyn (SarXos), available from:
 *  [https://github.com/sarxos/webcam-capture](https://github.com/sarxos/webcam-capture).
 *  The webcam-capture package is licensed under the MIT License.
 * 
 *  The filter architecture follows the pattern defined by Jerry Huxtable
 *  in the JH Labs Java Image Processing library, available from:
 *    http://www.jhlabs.com/ip/filters
 *  and licensed under the Apache License, Version 2.0
 *  (http://www.apache.org/licenses/LICENSE-2.0).
 *
 *  @module motionDetector
 *  @authors Edward A. Lee
 *  @copyright http://terraswarm.org/accessors/copyright.txt
 */
 
////////////////////////////////////////////////////////////
//// Private variables.

var Filter = Java.type('com.jhlabs.image.MotionDetectorFilter');
var filter = new Filter();

////////////////////////////////////////////////////////////
//// Functions provided in this module.

/** Invoke the motion detector on the specified image with the specified
 *  options and return the result.
 *  Any unrecognized options are ignored.
 *  Note that previously applied options for a given filter will still be
 *  used, even if they are not set in this call.
 *  If this is the first image provided, then
 *  no motion is detected. Otherwise, the image is compared against the one
 *  previously passed to this same function.
 *  @param image The image to filter.
 *  @param options An object whose fields specify filter options.
 *  @return The filtered image.
 */
exports.filter = function(image, options) {
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
    return filter.filter(image, null);
}

/** Return a percentage of area covered by motion in the most recently provided
 *  image, where 0 means no motion and 100 means full image motion.
 *  @return The percentage of area covered by motion.
 */
exports.area = function() {
    return filter.getMotionArea();
}

/** Return an array of two numbers giving the center of gravity of
 *  the detected motion in pixel coordinates, or return null if no
 *  motion has been detected.
 *  @return The center of gravity of motion.
 */
exports.cog = function() {
    var cog = filter.getCOG();
    if (cog) {
        return [cog.x, cog.y];
    }
    return null;
}
