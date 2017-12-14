/* A motion detector image filter.

@Copyright (c) 2015 The Regents of the University of California.
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

and

The MIT License (MIT)

Copyright (c) 2012 - 2015 Bartosz Firyn and Contributors

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

 */
package com.jhlabs.image;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;

/** A motion detector image filter.
 *  This filter compares each image to be filtered against the previously
 *  provided image to be filtered. If enough of the pixels differ by enough,
 *  then it modifies the image to be filtered by adding a small red circle
 *  at the center of gravity of the pixels that differ by enough.
 *
 *  The parameters of the filter are:
 *  <ul>
 *  <li> <i>PixelThreshold</i>: Intensity threshold whereby a pixel is
 *       deemed to different (an int 0 - 255, with default 25).
 *  <li> <i>AreaThreshold</i>: The percentage threshold of image
 *       that has different pixels for motion to be detected
 *       (a double 0-100, with default 0.2).
 *  <li> <i>ModifyImage</i>: If true (the default), the modify the
 *       provided image with a visual indication of the location
 *       and amount of motion.
 *  </ul>
 *  The implementation is taken from the webcam-capture package by
 *  Bartosz Firyn (SarXos), available from:
 *    https://github.com/sarxos/webcam-capture
 *  The webcam-capture package is licensed under the MIT License.
 *
 *  The filter architecture follows the pattern defined by Jerry Huxtable
 *  in the JH Labs Java Image Processing library, available from:
 *    http://www.jhlabs.com/ip/filters
 *  and licensed under the Apache License, Version 2.0
 *  (http://www.apache.org/licenses/LICENSE-2.0).
 *
 *  @author Bartosz Firyn (SarXos) and Edward A. Lee
 *  @version $Id$
 *  @since Ptolemy II 11.0
 *  @Pt.ProposedRating Yellow (eal)
 *  @Pt.AcceptedRating Red (cxh)
 */
public class MotionDetectorFilter extends AbstractBufferedImageOp {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Filter the source image, and if motion is detected, place a red circle at
     *  the center of gravity of the motion. If the destination argument is null,
     *  then the red circle is added directly to the source, and the source is
     *  returned. Otherwise, the red circle is added to the destination image.
     *
     *  @param source The source image, on which motion is detected.
     *  @param destination The destination image, on which the red circle is added,
     *   or null to specify to add the circle to the source image.
     */
    @Override
    public BufferedImage filter(BufferedImage source,
            BufferedImage destination) {
        if (destination == null) {
            // If no destination is provided, overwrite the source.
            destination = source;
        }

        BufferedImage modified = _blur.filter(source, null);
        modified = _gray.filter(modified, null);

        // Count of pixels that are different.
        int p = 0;
        // "cog" is "center of gravity".
        int cogX = 0;
        int cogY = 0;

        int w = modified.getWidth();
        int h = modified.getHeight();

        if (_previous != null) {
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {

                    int cpx = modified.getRGB(x, y);
                    int ppx = _previous.getRGB(x, y);
                    int pid = combinePixels(cpx, ppx) & 0x000000ff;

                    if (pid >= _pixelThreshold) {
                        cogX += x;
                        cogY += y;
                        p += 1;
                    }
                }
            }
        } else {
            _previous = modified;
            _motion = false;
            return source;
        }

        // Percentage of area with pixels that are different.
        _area = p * 100d / (w * h);

        if (_area >= _areaThreshold) {
            _cog = new Point(cogX / p, cogY / p);
            _motion = true;
        } else {
            _cog = new Point(w / 2, h / 2);
            _motion = false;
        }

        _previous = modified;

        if (_modify) {
            Graphics2D g = destination.createGraphics();
            g.setColor(Color.WHITE);
            double s = getMotionArea();
            g.drawString(String.format("Area: %.2f%%", s), 10, 20);

            if (_motion) {
                g.setStroke(new BasicStroke(2));
                g.setColor(Color.RED);
                g.drawOval(_cog.x - 5, _cog.y - 5, 10, 10);
            } else {
                /* Original would put a green rectangle in the center. Don't do this.
                g.setColor(Color.GREEN);
                g.drawRect(_cog.x - 5, _cog.y - 5, 10, 10);
                 */
            }
            g.dispose();
        }

        return destination;
    }

    /** Get the percentage fraction of detected motion area threshold above which it is classified as
     *  "moved". Minimum value for this is 0 and maximum is 100, which corresponds to full image
     *  covered by spontaneous motion.
     *  @return The percentage of image area.
     *  @see #setAreaThreshold(double)
     */
    public double getAreaThreshold() {
        return _areaThreshold;
    }

    /** Return center of gravity of motion detected by the most recent invocation
     *  of filter(), or null if no motion was detected.
     *  @return The center of gravity of motion (in pixels).
     */
    public Point getCOG() {
        if (_motion) {
            return _cog;
        } else {
            return null;
        }
    }

    /** Return whether the filter will modify the provided image with a visual
     *  indication of the amount and location of the motion.
     *  @return True if the image will be modified (the default).
     */
    public boolean getModifyImage() {
        return _modify;
    }

    /** Return the motion strength (0 = no motion, 100 = full image covered by motion).
     *  @return The motion area percentage.
     */
    public double getMotionArea() {
        return _area;
    }

    /** Get the pixel intensity difference threshold above which pixel is classified as "moved".
     *  The minimum value is 0 and maximum is 255. Default value is 10.
     *  This value is equal for all RGB components difference.
     *  @return The pixel intensity difference threshold.
     *  @see #setPixelThreshold(int)
     */
    public int getPixelThreshold() {
        return _pixelThreshold;
    }

    /** Return whether motion was detected in the last filter operation.
     *  @return Whether motion was detected.
     */
    public boolean isMotion() {
        return _motion;
    }

    /** Set the percentage fraction of detected motion area threshold above which it is classified as
     *  "moved". The minimum value for this is 0 and maximum is 100, which corresponds to full image
     *  covered by spontaneous motion.
     *  @param threshold The percentage of image area.
     *  @see #getAreaThreshold()
     */
    public void setAreaThreshold(double threshold) {
        if (threshold < 0) {
            throw new IllegalArgumentException(
                    "Area fraction threshold cannot be negative!");
        }
        if (threshold > 100) {
            throw new IllegalArgumentException(
                    "Area fraction threshold cannot be higher than 100!");
        }
        _areaThreshold = threshold;
    }

    /** Specify whether to modify the image with a visual indication of the amount
     *  and location of the motion.
     *  @param modify True to modify the image.
     *  @see #getModifyImage()
     */
    public void setModifyImage(boolean modify) {
        _modify = modify;
    }

    /** Set the pixel intensity difference threshold above which pixel is classified as "moved".
     *  The minimum value is 0 and maximum is 255. Default value is 10.
     *  This value is equal for all RGB components difference.
     *  @param threshold The pixel intensity difference threshold.
     *  @see #getPixelThreshold()
     */
    public void setPixelThreshold(int threshold) {
        if (threshold < 0) {
            throw new IllegalArgumentException(
                    "Pixel intensity threshold cannot be negative!");
        }
        if (threshold > 255) {
            throw new IllegalArgumentException(
                    "Pixel intensity threshold cannot be higher than 255!");
        }
        _pixelThreshold = threshold;
    }

    /** Return a string description of the filter.
     *  @return The string "MotionDetectorFilter".
     */
    @Override
    public String toString() {
        return "MotionDetectorFilter";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Compare two pixels and return a number between 0 and 255 that is
     *  an indicator of how different they are. If they are identical, the
     *  returned result is 0.
     *  @param rgb1 The first pixel.
     *  @param rgb2 The second pixel.
     *  @return An indicator of the difference.
     *  @author Bartosz Firyn (SarXos)
     */
    private static int combinePixels(int rgb1, int rgb2) {

        // first ARGB

        int a1 = (rgb1 >> 24) & 0xff;
        int r1 = (rgb1 >> 16) & 0xff;
        int g1 = (rgb1 >> 8) & 0xff;
        int b1 = rgb1 & 0xff;

        // second ARGB

        int a2 = (rgb2 >> 24) & 0xff;
        int r2 = (rgb2 >> 16) & 0xff;
        int g2 = (rgb2 >> 8) & 0xff;
        int b2 = rgb2 & 0xff;

        r1 = clamp(Math.abs(r1 - r2));
        g1 = clamp(Math.abs(g1 - g2));
        b1 = clamp(Math.abs(b1 - b2));

        // in case if alpha is enabled (translucent image)

        if (a1 != 0xff) {
            a1 = a1 * 0xff / 255;
            int a3 = (255 - a1) * a2 / 255;
            r1 = clamp((r1 * a1 + r2 * a3) / 255);
            g1 = clamp((g1 * a1 + g2 * a3) / 255);
            b1 = clamp((b1 * a1 + b2 * a3) / 255);
            a1 = clamp(a1 + a3);
        }

        return (a1 << 24) | (r1 << 16) | (g1 << 8) | b1;
    }

    /** Clamp a value to the range 0..255.
     *  @return The clamped value.
     *  @author Bartosz Firyn (SarXos)
     */
    private static int clamp(int c) {
        if (c < 0) {
            return 0;
        }
        if (c > 255) {
            return 255;
        }
        return c;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Motion strength (0 = no motion, 100 = full image covered by motion). */
    private double _area = 0.0;

    /** The percentage threshold of image that has different pixels
     * for motion to be detected (a double 0-100, with default 0.2).
     */
    private double _areaThreshold = 0.2;

    /** Center of gravity of the motion. */
    private Point _cog = null;

    /** Blur filter instance. */
    private final BoxBlurFilter _blur = new BoxBlurFilter(6, 6, 1);

    /** Gray filter instance. */
    private final GrayFilter _gray = new GrayFilter();

    /** True to modify the provided image. */
    private boolean _modify = true;

    /** Indicator that motion has been detected by the filter operation. */
    private boolean _motion = false;

    /** Intensity threshold whereby a pixel is deemed to different
     * (an int 0 - 255, with default 25).
     */
    private int _pixelThreshold = 25;

    /** Previously captured image. */
    private BufferedImage _previous = null;
}
