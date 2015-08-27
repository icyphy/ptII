/*
Copyright (c) 2013 by the Regents of the University of Michigan.
Developed by the APRIL robotics lab under the direction of Edwin Olson (ebolson@umich.edu).

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the
   distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

The views and conclusions contained in the software and documentation
are those of the authors and should not be interpreted as representing
official policies, either expressed or implied, of the FreeBSD
Project.

This implementation of the AprilTags detector is provided for
convenience as a demonstration.  It is an older version implemented in
Java that has been supplanted by a much better performing C version.
If your application demands better performance, you will need to
replace this implementation with the newer C version and using JNI or
JNA to interface the C version to Java.

For details about the C version, see
https://april.eecs.umich.edu/wiki/index.php/AprilTags-C

 */

package edu.umich.eecs.april.image;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.Random;

import edu.umich.eecs.april.jmat.LinAlg;

public class FloatImage {
    public int width;
    public int height;
    public float d[];

    static Random r = new Random();

    public FloatImage(int width, int height) {
        this.width = width;
        this.height = height;
        d = new float[width * height];
    }

    public FloatImage(int width, int height, float d[]) {
        this.width = width;
        this.height = height;
        this.d = d;
    }

    public FloatImage(int width, int height, double d[]) {
        this.width = width;
        this.height = height;
        this.d = LinAlg.copyFloats(d);
    }

    /** Make FloatImage from byte array, scaling inputs to
     * range (0,1)
     **/
    public FloatImage(int width, int height, byte d[]) {
        this.width = width;
        this.height = height;
        this.d = new float[width * height];
        for (int i = 0; i < d.length; i++)
            this.d[i] = (d[i] & 0xff) / (float) 255.0;
    }

    public FloatImage(BufferedImage im) {
        this(im, 8);
    }

    public FloatImage(BufferedImage im, int rightshift) {
        width = im.getWidth();
        height = im.getHeight();
        d = imageToFloats(im, rightshift);
    }

    public FloatImage copy() {
        float r[] = new float[d.length];
        for (int i = 0; i < d.length; i++)
            r[i] = d[i];
        return new FloatImage(width, height, r);
    }

    /** Get byte data from FloatImage scaled from 0 to 255
     **/
    public final byte[] getByteData() {
        byte r[] = new byte[d.length];
        for (int i = 0; i < d.length; i++)
            r[i] = (byte) (255 * d[i]);
        return r;
    }

    public final float get(int x, int y) {
        return d[y * width + x];
    }

    public final void set(int x, int y, float v) {
        d[y * width + x] = v;
    }

    /** Perform 2D convolution using f as the factor of a separable
     * filter, shifting the output by -f.length/2 so there is no net
     * shift.
     **/
    public FloatImage filterFactoredCentered(float fhoriz[], float fvert[]) {
        return filterFactoredCentered(fhoriz, fvert, null);
    }

    /** Perform 2D convolution using f as the factor of a separable
     * filter, shifting the output by -f.length/2 so there is no net
     * shift. Allows specifying the output buffer to prevent unnecessary data allocation.
     *
     **/
    public FloatImage filterFactoredCentered(float fhoriz[], float fvert[],
            float r[]) {
        if (r == null)
            r = new float[d.length];
        else
            assert (r.length == d.length);

        // do horizontal
        for (int y = 0; y < height; y++) {
            SigProc.convolveSymmetricCentered(d, y * width, width, fhoriz, r, y
                    * width);
        }

        // do vertical
        float tmp[] = new float[height]; // the column before convolution
        float tmp2[] = new float[height]; // the column after convolution.

        for (int x = 0; x < width; x++) {

            // copy the column out for locality.
            for (int y = 0; y < height; y++)
                tmp[y] = r[y * width + x];

            SigProc.convolveSymmetricCentered(tmp, 0, height, fvert, tmp2, 0);

            for (int y = 0; y < height; y++)
                r[y * width + x] = tmp2[y];
        }

        return new FloatImage(width, height, r);
    }

    /** Perform 2D convolution using f as the factor of a separable
     * filter, shifting the output by -f.length/2 so there is no net
     * shift.
     **/
    public FloatImage filterFactoredCenteredMax(float fhoriz[], float fvert[]) {
        // do horizontal
        float r[] = new float[d.length];

        for (int y = 0; y < height; y++) {
            SigProc.convolveSymmetricCenteredMax(d, y * width, width, fhoriz,
                    r, y * width);
        }

        // do vertical
        float tmp[] = new float[height]; // the column before convolution
        float tmp2[] = new float[height]; // the column after convolution.

        for (int x = 0; x < width; x++) {

            // copy the column out for locality.
            for (int y = 0; y < height; y++)
                tmp[y] = r[y * width + x];

            SigProc.convolveSymmetricCenteredMax(tmp, 0, height, fvert, tmp2, 0);

            for (int y = 0; y < height; y++)
                r[y * width + x] = tmp2[y];
        }

        return new FloatImage(width, height, r);
    }

    public FloatImage filterHorizontalCentered(float f[]) {
        float r[] = new float[d.length];

        for (int y = 0; y < height; y++) {
            SigProc.convolveSymmetricCentered(d, y * width, width, f, r, y
                    * width);
        }

        return new FloatImage(width, height, r);
    }

    public FloatImage filterVerticalCentered(float f[]) {
        // do vertical
        float r[] = new float[d.length];

        float tmp[] = new float[height]; // the column before convolution
        float tmp2[] = new float[height]; // the column after convolution.

        for (int x = 0; x < width; x++) {

            // copy the column out for locality.
            for (int y = 0; y < height; y++)
                tmp[y] = d[y * width + x];

            SigProc.convolveSymmetricCentered(tmp, 0, height, f, tmp2, 0);

            for (int y = 0; y < height; y++)
                r[y * width + x] = tmp2[y];
        }

        return new FloatImage(width, height, r);
    }

    /** Perform 2D convolution using 2D filter. The output is shifted by
        public FloatImage filterCentered(float f[][])
        {
        return null;
        }
     **/

    public FloatImage clamp(float minv, float maxv) {
        float r[] = new float[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float v = d[y * width + x];
                if (v < minv)
                    v = minv;
                if (v > maxv)
                    v = maxv;
                r[y * width + x] = v;
            }
        }

        return new FloatImage(width, height, r);
    }

    /** Rescale all values so that they are between [0,1] **/
    public FloatImage normalize() {
        float r[] = new float[width * height];
        float max = LinAlg.max(d);
        float min = LinAlg.min(d);

        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                r[y * width + x] = (d[y * width + x] - min) / (max - min);

        return new FloatImage(width, height, r);
    }

    public void addNoise(double stddev) {
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                d[y * width + x] += r.nextDouble() * stddev;
    }

    public FloatImage scale(double s) {
        float r[] = new float[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                r[y * width + x] = (float) (s * d[y * width + x]);
            }
        }

        return new FloatImage(width, height, r);
    }

    public FloatImage decimate() {
        int nwidth = width / 2;
        int nheight = height / 2;

        float r[] = new float[nwidth * nheight];

        for (int y = 0; y < nheight; y++)
            for (int x = 0; x < nwidth; x++)
                r[y * nwidth + x] = d[(2 * y) * width + (2 * x)];

        return new FloatImage(nwidth, nheight, r);
    }

    public FloatImage decimateAvg() {
        int nwidth = width / 2;
        int nheight = height / 2;

        float r[] = new float[nwidth * nheight];

        for (int y = 0; y < nheight; y++)
            for (int x = 0; x < nwidth; x++)
                r[y * nwidth + x] = 0.25f * (d[(2 * y + 0) * width
                        + (2 * x + 0)]
                        + d[(2 * y + 0) * width + (2 * x + 1)]
                        + d[(2 * y + 1) * width + (2 * x + 0)] + d[(2 * y + 1)
                        * width + (2 * x + 1)]);

        return new FloatImage(nwidth, nheight, r);
    }

    // Decimate, taking the maximum value within the subsampled intervals.
    public FloatImage decimateMax() {
        int nwidth = width / 2;
        int nheight = height / 2;

        float r[] = new float[nwidth * nheight];

        for (int y = 0; y + 1 < height; y++) {
            for (int x = 0; x + 1 < width; x++) {
                int ridx = (y / 2) * nwidth + x / 2;
                int didx = y * width + x;
                r[ridx] = Math.max(r[ridx], d[didx]);
            }
        }

        return new FloatImage(nwidth, nheight, r);
    }

    /** Interpolate image, followed by a gaussian filter with given sigma. **/
    public FloatImage interpolate(double sigma, int size) {
        int nwidth = width * 2;
        int nheight = height * 2;

        float r[] = new float[nwidth * nheight];

        for (int y = 0; y < nheight; y++) {
            for (int x = 0; x < nwidth; x++) {
                r[y * nwidth + x] = d[(y / 2) * width + (x / 2)];
            }
        }

        /*
          for (int y = 0; y < height; y++) {
          for (int x = 0; x < width; x++) {
          r[2*y*nwidth + 2*x] = d[y*width + x];
          }
          }
         */

        FloatImage fim = new FloatImage(nwidth, nheight, r);
        if (sigma > 0 && size > 0) {
            float filt[] = SigProc.makeGaussianFilter(sigma, size);
            fim = fim.filterFactoredCentered(filt, filt);
        }

        return fim;

    }

    public FloatImage abs() {
        return new FloatImage(width, height, LinAlg.abs(d));
    }

    public FloatImage subtract(FloatImage fim) {
        return subtract(fim, null);
    }

    public FloatImage subtract(FloatImage fim, float r[]) {
        assert (width == fim.width && height == fim.height);

        if (r == null)
            r = new float[d.length];
        else
            assert (r.length == d.length);

        for (int i = 0; i < d.length; i++)
            r[i] = d[i] - fim.d[i];

        return new FloatImage(width, height, r);
    }

    public static BufferedImage makeImage(FloatImage rim, FloatImage gim,
            FloatImage bim) {
        int width = rim.width, height = rim.height;

        BufferedImage im = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);

        int out[] = ((DataBufferInt) (im.getRaster().getDataBuffer()))
                .getData();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r = (int) (255 * rim.d[y * width + x]);
                int g = (int) (255 * gim.d[y * width + x]);
                int b = (int) (255 * bim.d[y * width + x]);
                out[y * width + x] = b | (g << 8) | (r << 16);
            }
        }

        return im;
    }

    public BufferedImage makeImage() {
        BufferedImage im = new BufferedImage(width, height,
                BufferedImage.TYPE_BYTE_GRAY);

        byte b[] = ((DataBufferByte) (im.getRaster().getDataBuffer()))
                .getData();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                b[y * width + x] = (byte) (255 * d[y * width + x]);
            }
        }

        return im;
    }

    public BufferedImage makeColorImage() {
        BufferedImage im = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);

        int b[] = ((DataBufferInt) (im.getRaster().getDataBuffer())).getData();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int c = (int) (255 * d[y * width + x]);
                b[y * width + x] = c | (c << 8) | (c << 16);
            }
        }

        return im;
    }

    public static float[] imageToFloats(BufferedImage im) {
        //	return imageToFloats(im, 8); // green channel.

        int width = im.getWidth(), height = im.getHeight();
        float f[] = new float[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = im.getRGB(x, y);
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = (rgb & 0xff);

                int gray = (77 * r + 151 * g + 28 * b) >> 8;
                assert (gray < 256);
                f[y * width + x] = gray * (1.0f / 255.0f);
            }
        }

        return f;
    }

    public static float[] imageToFloats(BufferedImage im, int rightshift) {
        return imageToFloats(im, rightshift, null);
    }

    public static float[] imageToFloats(BufferedImage im, int rightshift,
            float f[]) {
        int width = im.getWidth(), height = im.getHeight();
        if (f == null)
            f = new float[width * height];
        else
            assert (f.length == width * height);

        int type = im.getType();

        switch (type) {
        case BufferedImage.TYPE_INT_RGB: {
            int rgb[] = ((DataBufferInt) (im.getRaster().getDataBuffer()))
                    .getData();
            for (int i = 0; i < rgb.length; i++)
                f[i] = ((rgb[i] >> rightshift) & 0xff) * (1.0f / 255.0f);

            break;
        }

        case BufferedImage.TYPE_BYTE_GRAY: {
            byte b[] = ((DataBufferByte) (im.getRaster().getDataBuffer()))
                    .getData();
            for (int i = 0; i < b.length; i++)
                f[i] = (b[i] & 0xff) * (1.0f / 255.0f);

            break;
        }

        case BufferedImage.TYPE_3BYTE_BGR: {
            byte b[] = ((DataBufferByte) (im.getRaster().getDataBuffer()))
                    .getData();
            for (int i = 0; i < f.length; i++) {
                int rgb = ((b[3 * i + 0] & 0xff) << 0)
                        + ((b[3 * i + 1] & 0xff) << 8)
                        + ((b[3 * i + 2] & 0xff) << 16);
                f[i] = ((rgb >> rightshift) & 0xff) * (1.0f / 255.0f);
            }
            break;
        }

        default: {
            // System.out.printf("FloatImage: slow imageToFloats for type %d\n", type);

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int rgb = im.getRGB(x, y);
                    f[y * width + x] = ((rgb >> rightshift) & 0xff)
                            * (1.0f / 255.0f);
                }
            }
        }
        }
        return f;
    }

    public ArrayList<float[]> localMaxima() {
        ArrayList<float[]> maxima = new ArrayList<float[]>();

        for (int y = 1; y + 1 < height; y++) {
            for (int x = 1; x + 1 < width; x++) {
                float c = d[y * width + x];

                if (d[(y - 1) * width + x - 1] >= c)
                    continue;
                if (d[(y - 1) * width + x + 0] >= c)
                    continue;
                if (d[(y - 1) * width + x + 1] >= c)
                    continue;
                if (d[(y + 0) * width + x - 1] >= c)
                    continue;
                if (d[(y + 0) * width + x + 1] >= c)
                    continue;
                if (d[(y + 1) * width + x - 1] >= c)
                    continue;
                if (d[(y + 1) * width + x + 0] >= c)
                    continue;
                if (d[(y + 1) * width + x + 1] >= c)
                    continue;

                maxima.add(new float[] { x, y, c });
            }
        }
        return maxima;
    }

    /** Returns x, y, magnitude. **/
    public ArrayList<float[]> localMinima() {
        ArrayList<float[]> maxima = new ArrayList<float[]>();
        for (int y = 1; y + 1 < height; y++) {
            for (int x = 1; x + 1 < width; x++) {
                float c = d[y * width + x];

                if (d[(y - 1) * width + x - 1] <= c)
                    continue;
                if (d[(y - 1) * width + x + 0] <= c)
                    continue;
                if (d[(y - 1) * width + x + 1] <= c)
                    continue;
                if (d[(y + 0) * width + x - 1] <= c)
                    continue;
                if (d[(y + 0) * width + x + 1] <= c)
                    continue;
                if (d[(y + 1) * width + x - 1] <= c)
                    continue;
                if (d[(y + 1) * width + x + 0] <= c)
                    continue;
                if (d[(y + 1) * width + x + 1] <= c)
                    continue;

                maxima.add(new float[] { x, y, c });
            }
        }
        return maxima;
    }

}
