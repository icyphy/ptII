/**
 * Module to filter images. This module provides a set of image filters,
 * each realized by calling the {@link filter} function, passing it an input
 * image, a filter name, and optional filter options.
 * The list of available filters is provided by the {@link filters()} function.
 *  
 * This implementation uses code by Jerry Huxtable, licensed under
 * the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).
 * The code is available at [http://www.jhlabs.com/ip/filters](http://www.jhlabs.com/ip/filters).
 *
 * The filters provided by this implementation include at least the ones listed below,
 * with options in the sublist (if there are options).
 * <font color="red">FIXME:</font> <i>This list is incomplete. See
 * $PTII/com/jhlabs/image for a complete list of filters</i>.
 * * __Annotate__: Annotate an image with an overlaid SVG graphic.
 *   * _Graphic_: The SVG graphic. Defaults to an empty string, which specifies a default graphic. Overrides GraphicURI.
 *   * _GraphicURI_: The location of the graphic. Defaults to an empty string, which specifies a default graphic.
 *   * _Rotation_: A rotation to apply before translating.
 *   * _Scale_: A scaling to apply before translating.
 *   * _XOffset_: The horizontal offset (translation) for the graphic in pixels. Defaults to 0.
 *   * _YOffset_: The vertical offset (translation) for the graphic in pixels. Defaults to 0.
 * * __Average__: Average the 3x3 neighbourhood of each pixel, providing a simple blur.
 * * __BicubicScale__: Scale an image using bi-cubic interpolation.
 *   * _Height_: The height of the result image, in pixels. Defaults to 256.
 *   * _Width_: The width of the result image, in pixels. Defaults to 256.
 * * __Block__: Pixellate an image.
 *   * _BlockSize_: The size of the blocks.
 * * __Border__: Put a border around the image.
 *   * _BorderColor_: The color (see below) for the border. This defaults to white.
 *   * _BottomBorder_: The width of the bottom border, in pixels. This defaults to 10.
 *   * _LeftBorder_: The width of the left border, in pixels. This defaults to 10.
 *   * _RightBorder_: The width of the right border, in pixels. This defaults to 10.
 *   * _TopBorder_: The width of the top border, in pixels. This defaults to 10.
 * * __BoxBlur__: Box blur the image.
 *   * _HRadius_: The horizontal radius, in pixels. This defaults to 10.
 *   * _VRadius_: The vertical radius, in pixels. This defaults to 10.
 *   * _Iterations_: The number of iterations. This defaults to 1, but larger integers will approximate Gaussian blur.
 * * __Bump__: A simple embossing filter.
 * * __ChannelMix__: Mix the red, green and blue channels of an image into each other.
 *   The default options are {"IntoR": 255, "IntoG": 255, "IntoB": 255, "BlueGreen": 127, "GreenRed": 127, "RedBlue": 127},
 *   which inverts the colors while preserving the original brightness.
 *   * _IntoB_: How much of the blue in the output is taken from red and green.
 *     255 means all of it, and 0 means none of it (all the original blue is preserved).
 *   * _IntoG_: How much of the green in the output is taken from red and blue.
 *     255 means all of it, and 0 means none of it (all the original green is preserved).
 *   * _IntoR_: How much of the red in the output is taken from blue and green.
 *     255 means all of it, and 0 means none of it (all the original red is preserved).
 *   * _BlueGreen_: Proportion of blue vs. green that goes into red.
 *      0 means only blue and 255 means only green.
 *   * _GreenRed_: Proportion of green vs. red that goes into blue.
 *      0 means only green and 255 means only red.
 *   * _RedBlue_: Proportion of red vs. blue that goes into green.
 *      0 means only red and 255 means only green.
 * * __Chrome__: Create a chrome effect on the image.
 *   * _Amount_: The amount of the effect. This ranges from 0.0 to 1.0 and defaults to 0.5.
 *   * _Exposure_: The exposure, ranging from 0.0 (black image) to 1.0 (maximum, the default).
 * * __Circle__: Wrap an image around a circular arc.
 *   * _Angle_: The starting angle of the arc, in radians. This defaults to 0.0.
 *   * _Height_: The height of the arc, in pixels. This defaults to 200.
 *   * _Radius_: The radius of the inner arc, in pixels. This defaults to 100.
 *   * _CentreX_: The horizontal center of the arc, as a fraction of the width. This ranges from 0.0 to 1.0 and defaults to 0.5.
 *   * _CentreY_: The vertical center of the arc, as a fraction of the width. This ranges from 0.0 to 1.0 and defaults to 1.0.
 *   * _SpreadAngle_: The spread angle of the arc, in radians. This defaults to $PI.
 * * __ColorHalftone__:
 *   * _DotRadius_: The size of the dots, in pixels. This defaults to 2.0.
 *   * _CyanScreenAngle_: The angle of the cyan screen, in radians. This defaults to 1.885 (108 degrees).
 *   * _MagentaScreenAngle_: The angle of the magenta screen, in radians. This defaults to 2.827 (162 degrees).
 *   * _YellowScreenAngle_: The angle of the yellow screen, in radians. This defaults to 1.571 (90 degrees).
 * * __Contour___: Draw contours on an image at given changes in brightness levels.
 *   * _Levels_: The number of levels into which to divide the image. This defaults to 5.
 *   * _Scale_: The scale factor to apply to the brightness before leveling. This defaults to 1.
 *   * _Offset_: An offset to apply to the brightness. This defaults to 0.
 *   * _ContourColor_: The color to use in drawing the contours. This defaults to "black" (see below for color specs).
 * * __Contrast__: Change the brightness and contrast of an image.
 *   * _Brightness_: The brightness scale factor. This defaults to 1.0, which makes no change in brightness.
 *   * _Contrast_: The contrast scale factor. This defaults to 1.0, which makes no change in contrast.
 * * __Crop__: Crop an image.
 *   * _X_: The starting horizontal position, in pixels. This defaults to 0.
 *   * _Y_: The starting vertical position, in pixels. This defaults to 0.
 *   * _Width_: The width, in pixels. This defaults to 128.
 *   * _Height_: The height, in pixels. This defaults to 128.
 * * __Crystallize__: Apply a crystallizing effect to an image, by producing Voronoi cells filled with colours from the image.
 *   * _Angle_: Angle of the cells in radians. Defaults to 0.0.
 *   * _EdgeColor_: Color for the edge lines (see below). Defaults to black.
 *   * _EdgeThickness_: Thickness of the edges. Defaults to 0.4.
 *   * _FadeEdges_: Boolean indicating whether to fade edges. Defaults to false.
 *   * _Scale_: Size of the cells, in pixels. Defaults to 16.
 *   * _Stretch_: Vertical stretch of the cells. Defaults to 1.0, which does not stretch.
 * * __FIXME__: Missing filters (alphabetically) here.
 * * __Gray__: Gray out an image by averaging each pixel with white.
 * * __Invert__: Invert the colors of an image.
 * * __LensBlur__: Simulate a lens blur. Options:
 *   * _Bloom_: The bloom factor. This is an int that defaults to 2.
 *   * _BloomThreshold_: The bloom threshold. This is an int that defaults to 255, which disables bloom.
 *   * _Radius_: The radius, in pixels. This is an int that defaults to 10.
 *   * _Sides_: The number of sides of the aperture. This is an int that defaults to 5.
 * * __MotionDetector__: Detect motion and indicate it on the image.
 *   * _PixelThreshold_: Intensity threshold whereby a pixel is deemed to different (an int 0 - 255, with default 25).
 *   * _AreaThreshold_: The percentage threshold of image that has different pixels for motion to be detected (a double 0-100, with default 0.2).
 * * __Solarize__: Solarize an image.
 * * __Threshold__: Threshold pixels in an image, based on their brightness. Options:
 *   * _LowerThreshold_: The threshold below which pixels become _Black_. This is an int that defaults to 127.
 *   * _UpperThreshold_: The threshold above which pixels become _White_. This is an int that defaults to 127.
 *   * _Black_: The color produced for pixels below _LowerThreshold_. This is color (see below) that defaults to black.
 *   * _White_: The color produced for pixels above _UpperThreshold_. This is color (see below) that defaults to white.
 * 
 * For options that specify a color, the color may be given as an integer where bits 24-31
 * represent alpha, bits 16-23 represent red, bits 8-15 represent green, and bits 0-7 represent blue.
 * Alternatively, the color may be given as a string of the form of a hexadecimal number,
 * e.g. "0xff0000" for red, a standard color name, e.g. "red", or a CSS-style color specification,
 * e.g. "#FF0000" for red. The color names supported are black, blue, cyan, darkGray, gray, green,
 * lightGray, magenta, orange, pink, red, white, and yellow.
 * 
 * @module imageFilters
 * @authors Edward A. Lee
 * @copyright http://terraswarm.org/accessors/copyright.txt
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
    return ['Annotate', 'Average', 'BicubicScale', 'Block', 'Border', 'BoxBlur',
            'Bump', 'ChannelMix', 'Chrome', 'Circle',
            'ColorHalftone', 'Contour', 'Contrast', 'Crop',
            'Crystallize',
            'Gray', 'Invert', 'LensBlur', 'MotionDetector', 'Solarize', 'Threshold'];
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
    image = image.asAWTImage();
    var filter = filters[filterName];
    if (! filter) {
        var root = 'com.jhlabs.image.';
        var Filter = null;
        try {
            Filter = Java.type(root + filterName + 'Filter');
        } catch(ex) {
            // Try alternative location.
            try {
                Filter = Java.type(root + 'svg.' + filterName + 'Filter');
            } catch(ex) {
                error('Cannot find filter: ' + filterName);
                return image;
            }
        }
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
