/**
Module to identify AprilTags in an image.
To use this module, you have separately install a modified version of the
AprilTags code by Edwin Olson. This has to be a separate install, because
(sadly) the code is GPL'd.  See: http://ptolemy.eecs.berkeley.edu/~eal/aprilTags/

@module aprilTags
@authors Edward A. Lee
@copyright http://terraswarm.org/accessors/copyright.txt
 */
 
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
exports.tags = function() {
    return filter.tags();
}
