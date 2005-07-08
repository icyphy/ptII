/* Image display component.

 @Copyright (c) 1998-2005 The Regents of the University of California.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY
 */
package ptolemy.media;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.ColorModel;
import java.awt.image.MemoryImageSource;

import javax.swing.JPanel;

//////////////////////////////////////////////////////////////////////////
//// Picture

/** A component that displays an image.  The image can be updated
 in real time to create videos.  It can be monochrome or color.
 To use it, simply create it, populate it with pixels using one or more
 of the set methods, and call displayImage().

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class Picture extends JPanel {
    /** Create an image with the specified width and height, in pixels.
     *  @param width The width in pixels.
     *  @param height The height in pixels.
     */
    public Picture(int width, int height) {
        _width = width;
        _height = height;
        _pixels = new int[_width * _height];
        setSize(width, height);
    }

    /** Return the preferred size.
     *  @return The size of the image.
     */
    public Dimension getPreferredSize() {
        return new Dimension(_width, _height);
    }

    /** Return the minimum size.
     *  @return The size of the image.
     */
    public Dimension getMinimumSize() {
        return new Dimension(_width, _height);
    }

    /** Return the maximum size.
     *  @return The size of the image.
     */
    public Dimension getMaximumSize() {
        return new Dimension(_width, _height);
    }

    /** Notify this picture that its image has been changed and that it is
     *  now OK to display the new image.
     */
    public void displayImage() {
        if ((_imageSource == null) && (_image == null)) {
            _imageSource = new MemoryImageSource(_width, _height, ColorModel
                    .getRGBdefault(), _pixels, 0, _width);
            _imageSource.setAnimated(true);
            _image = createImage(_imageSource);
        }

        if (_imageSource != null) {
            _imageSource.newPixels();
        }
    }

    /** Paint this component.  If no pixels have been set, do nothing.
     *  @param graphics The graphics context.
     */
    public synchronized void paint(Graphics graphics) {
        if (_image != null) {
            graphics.drawImage(_image, 0, 0, this);
        }
    }

    /** Specify the java.awt.Image representation of the image.
     *  @param image A java.awt.Image object
     *  @exception IllegalArgumentException If the image size does not
     */
    public void setImage(Image image) throws IllegalArgumentException {
        _image = image;
    }

    /** Specify the packed ARGB representation of the image.
     *  Each pixel is a 32-bit integer where the top 8 bits give alpha
     *  (the transparency), then next 8 bits give the red value,
     *  the next 8 bits the green value, and the bottom 8 bits give
     *  the blue value.  The pixels are assembled into a single
     *  one-dimensional array that contains the first row followed by
     *  the second row, etc.  So the size of the array is the product
     *  of the width and the height of the image.  An element in the
     *  array might be set using a statement like:
     *  <pre>
     *    pix[row*col] = (alpha << 24) | (red << 16) | (green << 8) | blue;
     *  </pre>
     *
     *  @param pixels The packed ARGB representation of the image.
     *  @exception IllegalArgumentException If the image size does not
     *   match.
     */
    public void setImage(int[] pixels) throws IllegalArgumentException {
        if (pixels.length != (_width * _height)) {
            throw new IllegalArgumentException(
                    "setImage: Specified image size does not"
                            + "match that of the component.");
        }

        _pixels = pixels;

        if (_imageSource != null) {
            _imageSource.newPixels(pixels, ColorModel.getRGBdefault(), 0,
                    _width);
        }
    }

    /** Set the specified pixel to the given monochrome value, which
     *  should be in the range from zero (black) to 255 (white). If the
     *  argument is outside this range, then either white or black will
     *  be displayed, depending on the sign of the argument.
     *  If the row and column are out of bounds, then the command
     *  is ignored.
     *
     *  @param row The index of the row for the pixel.
     *  @param col The index of the column for the pixel.
     *  @param intensity The value of the pixel.
     */
    public void setPixel(int row, int col, int intensity) {
        if ((row < 0) || (row >= _height) || (col < 0) || (col >= _width)) {
            return;
        }

        if (intensity < 0) {
            intensity = 0;
        } else if (intensity > 255) {
            intensity = 255;
        }

        // Alpha, red, green, blue, where alpha controls transparency.
        _pixels[(row * _width) + col] = (255 << 24) | (intensity << 16)
                | (intensity << 8) | intensity;
    }

    /** Set the specified pixel to the given color value, where each color
     *  argument should be in the range from zero (absent) to 255 (fully
     *  saturated). If an argument is outside this range, then the nearest
     *  extreme of the range will be displayed.
     *  If the row and column are out of bounds, then the command
     *  is ignored.
     *
     *  @param row The index of the row for the pixel.
     *  @param col The index of the column for the pixel.
     *  @param red The red value of the pixel.
     *  @param green The green value of the pixel.
     *  @param blue The blue value of the pixel.
     */
    public void setPixel(int row, int col, int red, int green, int blue) {
        if ((row < 0) || (row >= _height) || (col < 0) || (col >= _width)) {
            return;
        }

        if (red < 0) {
            red = 0;
        } else if (red > 255) {
            red = 255;
        }

        if (green < 0) {
            green = 0;
        } else if (green > 255) {
            green = 255;
        }

        if (blue < 0) {
            blue = 0;
        } else if (blue > 255) {
            blue = 255;
        }

        // Alpha, red, green, blue, where alpha controls transparency.
        _pixels[(row * _width) + col] = (255 << 24) | (red << 16)
                | (green << 8) | blue;
    }

    /** Override the base class to prevent blanking, which causes flashing
     *  of the display.
     *  @param graphics The graphics context.
     */
    public void update(Graphics graphics) {
        paint(graphics);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private int _width;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private int _height;

    private int[] _pixels;

    private Image _image;

    private MemoryImageSource _imageSource;
}
