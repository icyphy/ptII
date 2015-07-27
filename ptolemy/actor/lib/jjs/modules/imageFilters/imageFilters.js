/**
Module to filter images. This module provides some set of named image filters,
the list of which is provided by the filters() function.
 
This implementation uses code by Jerry Huxtable, licensed under
the Apache License, Version 2.0 (http://www.apache.org/licenses/LICENSE-2.0).
The code is available at http://www.jhlabs.com/ip/filters/download.html.
The filters provided by this implementation are:
* __Gray__: Gray out an image by averaging each pixel with white.
* __Invert__: Invert the colors of an image.
* __LensBlur__: Simulate a lens blur. Options:
  * _Bloom_: The bloom factor. This is an int that defaults to 2.
  * _BloomThreshold_: The bloom threshold. This is an int that defaults to 255, which disables bloom.
  * _Radius_: The radius, in pixels. This is an int that defaults to 10.
  * _Sides_: The number of sides of the aperture. This is an int that defaults to 5.
* __MotionDetector__: Detect motion and indicate it on the image.
  * _PixelThreshold_: Intensity threshold whereby a pixel is deemed to different (an int 0 - 255, with default 25).
  * _AreaThreshold_: The percentage threshold of image that has different pixels for motion to be detected (a double 0-100, with default 0.2).
* __Solarize__: Solarize an image.
* __Threshold__: Threshold pixels in an image, based on their brightness. Options:
  * _LowerThreshold_: The threshold below which pixels become _Black_. This is an int that defaults to 127.
  * _UpperThreshold_: The threshold above which pixels become _White_. This is an int that defaults to 127.
  * _Black_: The color produced for pixels below _LowerThreshold_. This is an int that defaults to 0x000000.
  * _White_: The color produced for pixels above _UpperThreshold_. This is an int that defaults to 0xFFFFFF.
@module image
@authors Edward A. Lee
@copyright http://terraswarm.org/accessors/copyright.txt
 */
 
////////////////////////////////////////////////////////////
//// Private variables.

// Object to keep filters so that we don't instantiate them more than once.
var filters = {};

////////////////////////////////////////////////////////////
//// Functions provided in this module.

/** Return an array of filter names for image filters currently available
 *  on the current host.
 *  @return An array of filter names.
 */
exports.filters = function() {
    return ['Gray', 'Invert', 'LensBlur', 'MotionDetector', 'Solarize', 'Threshold'];
}

/** Invoke the named filter on the specified image with the specified
 *  options and return the result.  Any unrecognized options are ignored.
 *  Note that previously applied options for a given filter will still be
 *  used, even if they are not set in this call.
 *  @param image The image to filter.
 *  @param filterName The name of the filter.
 *  @param options An object whose fields specify filter options.
 *  @return The filtered image.
 */
exports.filter = function(image, filterName, options) {
    var filter = filters[filterName];
    if (! filter) {
        var Filter = Java.type('com.jhlabs.image.' + filterName + 'Filter');
        filter = new Filter();
        filters[filterName] = filter;
    }
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
