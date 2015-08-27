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

package edu.umich.eecs.april.tag;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import edu.umich.eecs.april.util.ReflectUtil;

/** Generic class for all tag encoding families **/
public class TagFamily {
    /** How many pixels wide is the outer-most white border? This is
     * only used when rendering a tag, not for decoding a tag (since
     * we look for the white/black edge). **/
    public int whiteBorder = 1;

    /** How many pixels wide is the inner black border? **/
    public int blackBorder = 1;

    /** number of bits in the tag. Must be a square (n^2). **/
    public final int bits;

    /** dimension of tag. e.g. for 16 bits, d=4. Must be sqrt(bits). **/
    public final int d;

    /** What is the minimum hamming distance between any two codes
     * (accounting for rotational ambiguity? The code can recover
     * (minHammingDistance-1)/2 bit errors.
     **/
    public final int minimumHammingDistance;

    /** The error recovery value determines our position on the ROC
     * curve. We will report codes that are within errorRecoveryBits
     * of a valid code. Small values mean greater rejection of bogus
     * tags (but false negatives). Large values mean aggressive
     * reporting of bad tags (but with a corresponding increase in
     * false positives).
     **/
    public int errorRecoveryBits = 1;

    /** The array of the codes. The id for a code is its index. **/
    public final long codes[];

    /** The codes array is not copied internally and so must not be
     * modified externally. **/
    public TagFamily(int bits, int minimumHammingDistance, long codes[]) {
        this.bits = bits;
        d = (int) Math.sqrt(bits);
        assert (d * d == bits);

        this.minimumHammingDistance = minimumHammingDistance;
        this.codes = codes;
    }

    public void setErrorRecoveryBits(int b) {
        errorRecoveryBits = b;
    }

    public void setErrorRecoveryFraction(double v) {
        errorRecoveryBits = (int) (((minimumHammingDistance - 1) / 2) * v);
    }

    /** if the bits in w were arranged in a d*d grid and that grid was
     * rotated, what would the new bits in w be?
     * The bits are organized like this (for d = 3):
     *
     *  8 7 6       2 5 8      0 1 2
     *  5 4 3  ==>  1 4 7 ==>  3 4 5    (rotate90 applied twice)
     *  2 1 0       0 3 6      6 7 8
     **/
    public static long rotate90(long w, int d) {
        long wr = 0;

        for (int r = d - 1; r >= 0; r--) {
            for (int c = 0; c < d; c++) {
                int b = r + d * c;

                wr = wr << 1;

                if ((w & (1L << b)) != 0)
                    wr |= 1;

            }
        }
        return wr;
    }

    /** Compute the hamming distance between two longs. **/
    public static final int hammingDistance(long a, long b) {
        return popCount(a ^ b);
    }

    /** How many bits are set in the long? **/
    static final int popCountReal(long w) {
        int cnt = 0;
        while (w != 0) {
            w &= (w - 1);
            cnt++;
        }
        return cnt;
    }

    static final int popCountTableShift = 12;
    static final byte[] popCountTable = new byte[1 << popCountTableShift];
    static {
        for (int i = 0; i < popCountTable.length; i++)
            popCountTable[i] = (byte) popCountReal(i);
    }

    public static final int popCount(long w) {
        int count = 0;

        while (w != 0) {
            count += popCountTable[(int) (w & (popCountTable.length - 1))];
            w >>= popCountTableShift;
        }
        return count;
    }

    /** Given an observed tag with code 'rcode', try to recover the
     * id. The corresponding fields of TagDetection will be filled
     * in. **/
    public void decode(TagDetection det, long rcode) {
        int bestid = -1;
        int besthamming = Integer.MAX_VALUE;
        int bestrotation = 0;
        long bestcode = 0;

        long rcodes[] = new long[4];

        rcodes[0] = rcode;
        rcodes[1] = rotate90(rcodes[0], d);
        rcodes[2] = rotate90(rcodes[1], d);
        rcodes[3] = rotate90(rcodes[2], d);

        for (int id = 0; id < codes.length; id++) {

            for (int rot = 0; rot < rcodes.length; rot++) {
                int thishamming = hammingDistance(rcodes[rot], codes[id]);
                if (thishamming < besthamming) {
                    besthamming = thishamming;
                    bestrotation = rot;
                    bestid = id;
                    bestcode = codes[id];
                }
            }
        }

        det.id = bestid;
        det.hammingDistance = besthamming;
        det.rotation = bestrotation;
        det.good = (det.hammingDistance <= errorRecoveryBits);
        det.obsCode = rcode;
        det.code = bestcode;
    }

    /** Return the dimension of the tag including borders when we render it.**/
    public int getTagRenderDimension() {
        return whiteBorder * 2 + blackBorder * 2 + d;
    }

    public BufferedImage makeImage(int id) {
        long v = codes[id];

        int width = getTagRenderDimension();
        int height = getTagRenderDimension();

        BufferedImage im = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);

        // Draw the borders.  It's easier to do this by iterating over
        // the whole tag than just drawing the borders.
        for (int y = 0; y < width; y++) {
            for (int x = 0; x < height; x++) {
                if (y < whiteBorder || y + whiteBorder >= height
                        || x < whiteBorder || x + whiteBorder >= width)
                    im.setRGB(x, y, 0xffffff);
                else
                    im.setRGB(x, y, 0x000000);
            }
        }

        // Now, draw the payload.
        for (int y = 0; y < d; y++) {
            for (int x = 0; x < d; x++) {
                if ((v & (1L << (bits - 1))) != 0)
                    im.setRGB(x + whiteBorder + blackBorder, y + whiteBorder
                            + blackBorder, 0xffffff);
                else
                    im.setRGB(x + whiteBorder + blackBorder, y + whiteBorder
                            + blackBorder, 0x000000);

                v = v << 1;
            }
        }

        return im;
    }

    /** Generate all valid tags, writing them as PNGs in the specified
     * directory.  The files will be named tag##_##_####.png, where
     * the first block is nbits, the second block is hamming distance,
     * and the final block is the id.
     **/
    public void writeAllImages(String dirpath) throws IOException {
        for (int i = 0; i < codes.length; i++) {
            BufferedImage im = makeImage(i);
            String fname = String.format("tag%02d_%02d_%05d.png", bits,
                    minimumHammingDistance, i);
            try {
                ImageIO.write(im, "png", new File(dirpath + "/" + fname));
            } catch (IOException ex) {
                System.out.println("ex: " + ex);
            }
        }
    }

    public BufferedImage getAllImagesMosaic() {
        ArrayList<BufferedImage> ims = new ArrayList<BufferedImage>();

        for (int i = 0; i < codes.length; i++) {
            BufferedImage im = makeImage(i);
            ims.add(im);
        }

        int width = (int) Math.sqrt(ims.size());
        int height = ims.size() / width + 1;
        int dim = getTagRenderDimension();

        BufferedImage im = new BufferedImage(dim * width, dim * height,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g = im.createGraphics();

        g.setColor(Color.white);
        g.fillRect(0, 0, im.getWidth(), im.getHeight());

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int id = y * width + x;
                if (id >= codes.length)
                    continue;

                g.drawImage(ims.get(id), x * dim, y * dim, null);
            }
        }

        return im;
    }

    public void writeAllImagesMosaic(String filepath) throws IOException {
        BufferedImage im = getAllImagesMosaic();

        ImageIO.write(im, "png", new File(filepath));
    }

    public void printHammingDistances() {
        int hammings[] = new int[d * d + 1];

        for (int i = 0; i < codes.length; i++) {
            long r0 = codes[i];
            long r1 = rotate90(r0, d);
            long r2 = rotate90(r1, d);
            long r3 = rotate90(r2, d);

            for (int j = i + 1; j < codes.length; j++) {

                int d = Math.min(Math.min(hammingDistance(r0, codes[j]),
                        hammingDistance(r1, codes[j])), Math.min(
                        hammingDistance(r2, codes[j]),
                        hammingDistance(r3, codes[j])));

                hammings[d]++;
            }
        }

        for (int i = 0; i < hammings.length; i++)
            System.out.printf("%10d  %10d\n", i, hammings[i]);
    }

    public void writeAllImagesPostScript(String filepath) throws IOException {
        int sz = d + 2 * whiteBorder + 2 * blackBorder;

        BufferedWriter outs = new BufferedWriter(new FileWriter(filepath));

        outs.write("/pagewidth 8.5 72 mul def                      \n"
                + "/pageheight 11 72 mul def                      \n" +

                "/maketag                                       \n"
                + "{                                              \n"
                + "  /img exch def                                \n"
                + "  /name exch def                               \n"
                + "  gsave                                        \n"
                + "  pagewidth 2 div pageheight 2 div translate   \n"
                + "  0 0 moveto                                   \n"
                + "  1.0 pagewidth mul dup scale                  \n"
                + "  1 -1 scale                                   \n"
                + "  -.5 -.5 translate                            \n" + "  "
                + sz + " " + sz + " 1 [ " + sz + " 0 0 " + sz
                + " 0 0 ] { img } image \n"
                + "  0 setlinewidth .5 setgray [0.002 0.01] 0 setdash \n"
                + "  0 0 moveto 1 0 lineto 1 1 lineto 0 1 lineto  \n"
                + "  closepath stroke                             \n"
                + "  grestore                                     \n"
                + "  gsave                                        \n"
                + "  pagewidth 2 div 72 translate                 \n"
                + "  /Helvetica-Bold findfont 20 scalefont setfont \n"
                + "  name                                         \n"
                + "  dup stringwidth pop -.5 mul 0 moveto         \n"
                + "  show                                         \n"
                + "  grestore                                     \n"
                + "  showpage                                     \n"
                + "} def                                          \n");

        for (int id = 0; id < codes.length; id++) {
            BufferedImage im = makeImage(id);

            // convert image into a postscript string
            int width = im.getWidth(), height = im.getHeight();

            String imgdata = "";

            for (int y = 0; y < height; y++) {
                long v = 0;
                int vlen = 0;

                for (int x = 0; x < width; x++) {
                    int b = ((im.getRGB(x, y) & 0xffffff) > 0) ? 1 : 0;
                    v = (v << 1) | b;
                    vlen++;
                }

                // pad to a byte boundary.
                while ((vlen % 8) != 0) {
                    v = (v << 1) | 0;
                    vlen++;
                }
                imgdata += String.format("%0" + (vlen / 4) + "x", v);
            }

            outs.write("(" + this.getClass().getName() + ", id = " + id + ") <"
                    + imgdata + "> maketag\n");
        }

        outs.close();
    }

    public static void main(String args[]) {
        if (args.length != 2) {
            System.out.printf("Usage: <tagclass> <outputdir>\n");
            System.out.printf("Example: art.tag.Tag25h11 /tmp/tag25h11\n");
            return;
        }

        String cls = args[0];
        String dirpath = args[1] + "/";

        TagFamily tagFamily = (TagFamily) ReflectUtil.createObject(cls);
        if (tagFamily == null)
            return;

        try {
            File f = new File(dirpath);
            if (!f.exists())
                f.mkdirs();

            tagFamily.writeAllImagesMosaic(dirpath + "mosaic.png");
            tagFamily.writeAllImages(dirpath);
            tagFamily.writeAllImagesPostScript(dirpath + "alltags.ps");
        } catch (IOException ex) {
            System.out.println("ex: " + ex);
        }
    }
}
