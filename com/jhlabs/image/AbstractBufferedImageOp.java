/*
Copyright 2006 Jerry Huxtable

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package com.jhlabs.image;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.lang.reflect.Field;

/**
 * A convenience class which implements those methods of BufferedImageOp which are rarely changed.
 */
public abstract class AbstractBufferedImageOp implements BufferedImageOp, Cloneable {

    @Override
    public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel dstCM) {
        if (dstCM == null)
            dstCM = src.getColorModel();
        return new BufferedImage(dstCM, dstCM.createCompatibleWritableRaster(src.getWidth(), src.getHeight()),
                dstCM.isAlphaPremultiplied(), null);
    }

    @Override
    public Rectangle2D getBounds2D(BufferedImage src) {
        return new Rectangle(0, 0, src.getWidth(), src.getHeight());
    }

    @Override
    public Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
        if (dstPt == null)
            dstPt = new Point2D.Double();
        dstPt.setLocation(srcPt.getX(), srcPt.getY());
        return dstPt;
    }

    @Override
    public RenderingHints getRenderingHints() {
        return null;
    }

    /**
     * A convenience method for getting ARGB pixels from an image. This tries to avoid the performance
     * penalty of BufferedImage.getRGB unmanaging the image.
     * @param image   a BufferedImage object
     * @param x       the left edge of the pixel block
     * @param y       the right edge of the pixel block
     * @param width   the width of the pixel arry
     * @param height  the height of the pixel arry
     * @param pixels  the array to hold the returned pixels. May be null.
     * @return the pixels
     * @see #setRGB
     */
    public int[] getRGB(BufferedImage image, int x, int y, int width, int height, int[] pixels) {
        int type = image.getType();
        if (type == BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB)
            return (int[]) image.getRaster().getDataElements(x, y, width, height, pixels);
        return image.getRGB(x, y, width, height, pixels, 0, width);
    }

    /**
     * A convenience method for setting ARGB pixels in an image. This tries to avoid the performance
     * penalty of BufferedImage.setRGB unmanaging the image.
     * @param image   a BufferedImage object
     * @param x       the left edge of the pixel block
     * @param y       the right edge of the pixel block
     * @param width   the width of the pixel arry
     * @param height  the height of the pixel arry
     * @param pixels  the array of pixels to set
     * @see #getRGB
     */
    public void setRGB(BufferedImage image, int x, int y, int width, int height, int[] pixels) {
        int type = image.getType();
        if (type == BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB)
            image.getRaster().setDataElements(x, y, width, height, pixels);
        else
            image.setRGB(x, y, width, height, pixels, 0, width);
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    /** Convert the string color specification into an integer where bits 24-31
     *  represent alpha, bits 16-23 represent red, bits 8-15 represent green,
     *  and bits 0-7 represent blue. The string specification can be of the form
     *  of a hexadecimal number, e.g. "0xff0000" for red, a standard color name,
     *  e.g. "red", or a CSS-style color specification, e.g. "#FF0000" for red.
     *  The color names supported are black, blue, cyan, darkGray, gray, green,
     *  lightGray, magenta, orange, pink, red, white, and yellow.
     *
     * @param colorSpec The string color specification.
     * @param defaultColor The default color to return if the string is malformed.
     * @return An integer color representation.
     * @author Edward A. Lee
     */
    public static int stringToColor(String colorSpec, int defaultColor) {
        try {
            // get color by hex or octal value
            if (colorSpec.startsWith("#")) {
                colorSpec = "0x" + colorSpec.substring(1);
            }
            Color color = Color.decode(colorSpec);
            return color.getRGB();
        } catch (NumberFormatException nfe) {
            // if we can't decode lets try to get it by name
            try {
                // try to get a color by name using reflection
                final Field f = Color.class.getField(colorSpec);

                Color color = (Color) f.get(null);
                return color.getRGB();
            } catch (Exception ce) {
                // if we can't get any color set to black
                return defaultColor;
            }
        }
    }

}
