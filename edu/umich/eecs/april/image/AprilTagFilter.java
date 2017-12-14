/* An AprilTag image filter. */

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

/*
The filter architecture defined here follows the pattern defined by
Jerry Huxtable in the JH Labs Java Image Processing library, available
from:
    http://www.jhlabs.com/ip/filters
and licensed under the Apache License, Version 2.0
(http://www.apache.org/licenses/LICENSE-2.0).
 */

package edu.umich.eecs.april.image;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.jhlabs.image.AbstractBufferedImageOp;

import edu.umich.eecs.april.jmat.LinAlg;
import edu.umich.eecs.april.jmat.MathUtil;
import edu.umich.eecs.april.jmat.geom.GLine2D;
import edu.umich.eecs.april.jmat.geom.GLineSegment2D;
import edu.umich.eecs.april.tag.TagDetection;
import edu.umich.eecs.april.tag.TagFamily;
import edu.umich.eecs.april.util.Gridder;
import edu.umich.eecs.april.util.ReflectUtil;
import edu.umich.eecs.april.util.UnionFindSimple;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;

/** An AprilTag detector image filter.
 *  This filter identifies April tags (https://april.eecs.umich.edu/wiki/index.php/AprilTags),
 *  which are two-dimensional images, similar to QR codes, but easier to detect.
 *  The options for the filter are the ones that have setter methods. For example,
 *  the TagFamily option as a setTagFamily() method.
 *
 *  <a href="https://april.eecs.umich.edu/software/apriltag.html#in_browser">https://april.eecs.umich.edu/software/apriltag.html</a>
 *  contains a set of pregenerated tags as png and PostScript files.
 *  However, these are of low resolution.  To scale them, use linear
 *  interpolation to avoid blurring.
 *  For example, with ImageMagik, use:
 *
 *  mogrify -scale 1000x1000 *.png
 *
 *  Or, search the web for "tag 36H11".
 *
 *  To create a pdf with all the images:
 *
 *  convert *.png tag36h11.pdf
 *
 *  To annotate an image with a string:
 *
 *  convert tag36_11_00026.png label:'26' -gravity Center -append tag36_11_00026_labeled.png
 *
 *  In the Ptolemy tree, a sample file may be found at
 *  $PTII/ptolemy/actor/lib/jjs/modules/aprilTags//demo/AprilTags/tag36_11_00586.pdf
 *
 *  @author Edwin Bolson and Edward A. Lee
 *  @version $Id$
 *  @since Ptolemy II 11.0
 *  @Pt.ProposedRating Yellow (eal)
 *  @Pt.AcceptedRating Red (cxh)
 */
public class AprilTagFilter extends AbstractBufferedImageOp {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Filter the source image, and if motion is detected, place a red circle at
     *  the center of gravity of the motion. If the destination argument is null,
     *  then the red circle is added directly to the source, and the source is
     *  returned. Otherwise, the red circle is added to the destination image.
     *  In addition, this method records the tags discovered so that they can later
     *  by retrieved by invoking the {@link #tags()} method.
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

        _tagFamily = (TagFamily) ReflectUtil
                .createObject("edu.umich.eecs.april.tag." + _tagFamilyName);
        if (_tagFamily == null) {
            throw new IllegalArgumentException("Failed to find tag family "
                    + _tagFamilyName + " in package edu.umich.eecs.april.tag");
        }

        int width = source.getWidth();
        int height = source.getHeight();

        _tags = _process(source, new double[] { width / 2, height / 2 });

        Graphics2D g = destination.createGraphics();
        g.setStroke(new BasicStroke(2));

        for (TagDetection tag : _tags) {
            double[] centerOfTag = tag.cxy;
            g.setColor(Color.GREEN);
            for (int i = 0; i < 4; i++) {
                g.drawLine((int) tag.p[i][0], (int) tag.p[i][1],
                        (int) tag.p[i + 1 & 3][0], (int) tag.p[i + 1 & 3][1]);
            }
            int x = (int) centerOfTag[0];
            int y = (int) centerOfTag[1];
            g.setColor(Color.RED);
            g.drawOval(x - 5, y - 5, 10, 10);
            g.drawString(String.format("ID: %d", tag.id), x + 10, y);

        }
        g.dispose();

        return destination;
    }

    /** When growing components, the intra component variation is
     * allowed to grow when the component is small in size. This
     * threshold affects how much. The default is 1200.
     * @see #setMagThresh(double)
     * @return The mag threshold
     */
    public double getMagThresh() {
        return _magThresh;
    }

    /** Return the maximum angle range allowed for the gradient directions
     *  when connecting edges, in radians. This defaults to the radian
     *  equivalent of 30 degrees.
     *  @see #setMaxEdgeCost(double)
     *  @return The maximum angle range allowed.
     */
    public double getMaxEdgeCost() {
        return _maxEdgeCost;
    }

    /** Return the gradient magnitude threshold for ignoring pixels.
     * Do not consider pixels whose gradient magnitude is less than
     * minMag. Small values make the detector more sensitive, but also
     * force us to consider many more edges resulting in slower
     * computation time. A value of 0.001 is very sensitive. A value
     * of 0.01 is quite fast. The default is 0.004.
     * @see #setMinMag(double)
     */
    public double getMinMag() {
        return _minMag;
    }

    /** Return the Gaussian smoothing kernel applied to image (0 == no filter)
     * used when detecting the outline of the box. It is almost always
     * useful to have some filtering, since the loss of small details
     * won't hurt. Recommended value = 0.8 (the default). The case where sigma ==
     * segsigma has been optimized to avoid a redundant filter
     * operation.
     * @see #setSegSigma(double)
     */
    public double getSegSigma() {
        return _segSigma;
    }

    /** Return the Gaussian smoothing kernel applied to image (0 == no filter, the default)
     * used when sampling bits. Filtering is a good idea in cases
     * where A) a cheap camera is introducing artifical sharpening, B)
     * the bayer pattern is creating artifcats, C) the sensor is very
     * noisy and/or has hot/cold pixels. However, filtering makes it
     * harder to decode very small tags. Reasonable values are 0, or
     * [0.8, 1.5].
     * @see #setSigma(double)
     */
    public double getSigma() {
        return _sigma;
    }

    /** Get the name of the tag family being detected.
     *  This defaults to "Tag36h11".
     *  The supported families are "Tag16h5", "Tag25h7", "Tag25h9", "Tag36h10", and "Tag36h11".
     *  The default family seems least susceptible to false positives.
     *  @return The name of the tag family.
     *  @see #setTagFamily(String)
     */
    public String getTagFamily() {
        return _tagFamilyName;
    }

    /** When growing components, the intra component variation is
     * allowed to grow when the component is small in size. This
     * threshold affects how much. The default is 100.
     * @see #setThetaThresh(double)
     */
    public double getThetaThresh() {
        return _thetaThresh;
    }

    /** Return whether decimating before segmenting is enabled.
     * Instead of blurring the input image before segmentation, we
     * can achieve similar effects by decimating the image by a factor
     * of two. When enabled, this option applies a block LPF filter of
     * width 2, then decimates the image. With this option, not only
     * can we safely set segSigma = 0, but the slowest part of the
     * algorithm (the segmentation) runs about 4 times faster. The
     * downside is that the position of the targets is determined
     * based on the segmentation: lower resolution will result in more
     * localization error. However, the effect on quality is quite
     * modest, and this optimization is generally recommended (along
     * with segSigma = 0). If segSigma is non-zero, the filtering by
     * segSigma occurs first, followed by the block LPF, and the
     * decimation. This defaults to false, indicating that the option
     * is not enabled.
     * @see #setSegDecimate(boolean)
     */
    public boolean isSegDecimate() {
        return _segDecimate;
    }

    /** When growing components, the intra component variation is
     * allowed to grow when the component is small in size. This
     * threshold affects how much. The default is 1200.
     * @see #getMagThresh()
     */
    public void setMagThresh(double magThresh) {
        _magThresh = magThresh;
    }

    /** Set the maximum angle range allowed for the gradient directions
     *  when connecting edges, in radians. This defaults to the radian
     *  equivalent of 30 degrees.
     *  @see #getMaxEdgeCost()
     */
    public void setMaxEdgeCost(double maxEdgeCost) {
        _maxEdgeCost = maxEdgeCost;
    }

    /** Set the gradient magnitude threshold for ignoring pixels.
     * Do not consider pixels whose gradient magnitude is less than
     * minMag. Small values make the detector more sensitive, but also
     * force us to consider many more edges resulting in slower
     * computation time. A value of 0.001 is very sensitive. A value
     * of 0.01 is quite fast. The default is 0.004.
     * @see #getMinMag()
     */
    public void setMinMag(double minMag) {
        _minMag = minMag;
    }

    /** Set whether decimating before segmenting is enabled.
     * Instead of blurring the input image before segmentation, we
     * can achieve similar effects by decimating the image by a factor
     * of two. When enabled, this option applies a block LPF filter of
     * width 2, then decimates the image. With this option, not only
     * can we safely set segSigma = 0, but the slowest part of the
     * algorithm (the segmentation) runs about 4 times faster. The
     * downside is that the position of the targets is determined
     * based on the segmentation: lower resolution will result in more
     * localization error. However, the effect on quality is quite
     * modest, and this optimization is generally recommended (along
     * with segSigma = 0). If segSigma is non-zero, the filtering by
     * segSigma occurs first, followed by the block LPF, and the
     * decimation. This defaults to false, indicating that the option
     * is not enabled.
     */
    public void setSegDecimate(boolean segDecimate) {
        _segDecimate = segDecimate;
    }

    /** Set the Gaussian smoothing kernel applied to image (0 == no filter)
     * used when detecting the outline of the box. It is almost always
     * useful to have some filtering, since the loss of small details
     * won't hurt. Recommended value = 0.8 (the default). The case where sigma ==
     * segsigma has been optimized to avoid a redundant filter
     * operation.
     * @see #getSegSigma()
     */
    public void setSegSigma(double segSigma) {
        _segSigma = segSigma;
    }

    /** Set the Gaussian smoothing kernel applied to image (0 == no filter, the default)
     * used when sampling bits. Filtering is a good idea in cases
     * where A) a cheap camera is introducing artifical sharpening, B)
     * the bayer pattern is creating artifcats, C) the sensor is very
     * noisy and/or has hot/cold pixels. However, filtering makes it
     * harder to decode very small tags. Reasonable values are 0, or
     * [0.8, 1.5].
     * @see #getSigma()
     */
    public void setSigma(double sigma) {
        _sigma = sigma;
    }

    /** Set the name of the tag family being detected.
     *  This defaults to "Tag36h11".
     *  The supported families are "Tag16h5", "Tag25h7", "Tag25h9", "Tag36h10", and "Tag36h11".
     *  The default family seems least susceptible to false positives.
     *  @param name The name of the tag family.
     *  @see #getTagFamily()
     */
    public void setTagFamily(String name) {
        _tagFamilyName = name;
    }

    /** When growing components, the intra component variation is
     * allowed to grow when the component is small in size. This
     * threshold affects how much. The default is 100.
     * @see #getThetaThresh()
     */
    public void setThetaThresh(double thetaThresh) {
        _thetaThresh = thetaThresh;
    }

    /** Return an array of records representing the most recently detected tags
     *  from an invocation of {@link #filter(BufferedImage, BufferedImage)}, or
     *  null if there has been no such invocation or if no tags were detected.
     *  Each record includes the following fields:
     *  <ul>
     *  <li> id: The ID of the detected tag.
     *  <li> center: An array with two doubles giving the center of the tag in pixel coordinates.
     *  <li> perimeter: An array with four arrays, each of which gives the x and y coordinates of
     *   a corner of the AprilTag, listed in a counter-clockwise direction.
     *  </ul>
     *  @exception IllegalActionException If the array cannot be constructed.
     */
    public ArrayToken tags() throws IllegalActionException {
        if (_tags == null || _tags.size() == 0) {
            return null;
        }
        Token[] tags = new RecordToken[_tags.size()];
        int i = 0;
        for (TagDetection tag : _tags) {
            Map<String, Token> fieldMap = new HashMap<String, Token>();

            fieldMap.put("id", new IntToken(tag.id));

            DoubleToken[] center = new DoubleToken[2];
            center[0] = new DoubleToken(tag.cxy[0]);
            center[1] = new DoubleToken(tag.cxy[1]);
            fieldMap.put("center", new ArrayToken(center));

            DoubleToken[] corner = new DoubleToken[2];
            ArrayToken[] perimeter = new ArrayToken[4];
            for (int k = 0; k < 4; k++) {
                corner[0] = new DoubleToken(tag.p[k][0]);
                corner[1] = new DoubleToken(tag.p[k][1]);
                perimeter[k] = new ArrayToken(corner);
            }
            fieldMap.put("perimeter", new ArrayToken(perimeter));

            tags[i++] = new RecordToken(fieldMap);
        }
        return new ArrayToken(tags);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public fields                     ////

    // These fields represent a number of tuning parameters.

    /** Early pruning of quads which have insane aspect ratios. **/
    public double maxQuadAspectRatio = 32;

    /** Minimum line length in pixels. Set based on minimum plausible decoding size for Tag9 family. */
    public double minimumLineLength = 4;

    /** Minimum number of pixels in a segment before we'll fit a line to it. */
    public double minimumSegmentSize = 4;

    /** Minimum size of tag (in pixels) as measured along edges and diagonals. */
    public double minimumTagSize = 6;

    // when stitching a quad together from segments, how far away can
    // one line begin from the end of the other?
    //
    // range = quadSearchRangePix + quadSearchRangeFraction*line_length
    public double quadSearchRangePix = 6;
    public double quadSearchRangeFraction = 0.5;

    /** Produce debugging output. If the debugging code annoys you (or
     * makes porting harder) you can delete all of the code in an
     * if (debug) block.
     * NOTE: Most of the debugging code is disabled in this implementation.
     **/
    public boolean debug = false;
    public BufferedImage debugInput; // the input image (after preprocessing)
    public BufferedImage debugSegInput; // the input image passed to segmentation.
    public BufferedImage debugSegmentation; // segmented image
    public BufferedImage debugTheta, debugMag;

    /** During segmentation, the weight of an edge is related to the
     * change in theta between the two pixels. This change is
     * normalized via maxEdgeCost, resulting in a number[0,1]. We then
     * convert this number to fixed-point by multiplying it by
     * WEIGHT_SCALE. The resulting number must fit in the number of
     * bits allocated to it, currently 16 (see Step 3). Large values
     * of WEIGHT_SCALE are good because they lead to better
     * fixed-point approximations. However, small numbers are better,
     * because there are fewer discrete values of edge costs, which
     * means that there is more spatial/temporal coherence when
     * processing the sorted edges. This results in faster
     * processing. Given that these orientations are pretty noisy to
     * begin with, some quantization error is acceptable.
     **/
    public int WEIGHT_SCALE = 100;//10000;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Lousy approximation of arctan function, but good enough for our purposes (about 4 degrees). */
    private final double _arctan2(double y, double x) {
        double coeff_1 = Math.PI / 4;
        double coeff_2 = 3 * coeff_1;
        double abs_y = Math.abs(y) + 1e-10; // kludge to prevent 0/0 condition

        double angle;

        if (x >= 0) {
            double r = (x - abs_y) / (x + abs_y);
            angle = coeff_1 - coeff_1 * r;
        } else {
            double r = (x + abs_y) / (abs_y - x);
            angle = coeff_2 - coeff_1 * r;
        }

        if (y < 0) {
            return -angle; // negate if in quad III or IV
        } else {
            return angle;
        }
    }

    /** Sort and return the first vlength values in v[] by the value
     * of v[i]&amp;mask. The maximum value in the array 'v' is maxv
     * (if maxv is negative, maxv will be found). These weights must
     * be small enough to fit in an integer. This implementation is
     * stable.
     **/
    private static long[] _countingSortLongArray(long v[], int vlength,
            int maxv, long mask) {
        if (maxv < 0) {
            for (int i = 0; i < vlength; i++) {
                maxv = Math.max(maxv, (int) (v[i] & mask));
            }
        }

        // For weight 'w', counts[w] will give the output position for
        // the next sample with weight w.  To build this, we begin by
        // counting how many samples there are with each weight. (Note
        // that the initial position for weight w is only affected by
        // the number of weights less than w, hence the +1 in
        // counts[w+1].
        int counts[] = new int[maxv + 2];

        for (int i = 0; i < vlength; i++) {
            int w = (int) (v[i] & mask);
            counts[w + 1]++;
        }

        // accumulate.
        for (int i = 1; i < counts.length; i++) {
            counts[i] += counts[i - 1];
        }

        long newv[] = new long[vlength];
        for (int i = 0; i < vlength; i++) {
            int w = (int) (v[i] & mask);
            newv[counts[w]] = v[i];
            counts[w]++;
        }

        /*       // test (debugging code)
                for (int i = 0; i+1 < newv.length; i++) {
                    int w0 = (int) (newv[i]&mask);
                    int w1 = (int) (newv[i+1]&mask);
                    assert(w0 <= w1);
                }
        */

        return newv;
    }

    private boolean _detectionsOverlapTooMuch(TagDetection a, TagDetection b) {
        // Compute a sort of "radius" of the two targets. We'll do
        // this by computing the average length of the edges of the
        // quads (in pixels).
        double radius = 0.0625 * (LinAlg.distance(a.p[0], a.p[1])
                + LinAlg.distance(a.p[1], a.p[2])
                + LinAlg.distance(a.p[2], a.p[3])
                + LinAlg.distance(a.p[3], a.p[0])
                + LinAlg.distance(b.p[0], b.p[1])
                + LinAlg.distance(b.p[1], b.p[2])
                + LinAlg.distance(b.p[2], b.p[3])
                + LinAlg.distance(b.p[3], b.p[0]));

        // distance (in pixels) between two tag centers.
        double d = LinAlg.distance(a.cxy, b.cxy);

        // reject pairs where the distance between centroids is
        // smaller than the "radius" of one of the tags.
        return (d < radius);
    }

    private final int _edgeCost(double theta0, double mag0, double theta1,
            double mag1) {
        if (mag0 < _minMag || mag1 < _minMag) {
            return -1;
        }

        double thetaErr = Math.abs(MathUtil.mod2pi(theta1 - theta0));
        if (thetaErr > _maxEdgeCost) {
            return -1;
        }

        double normErr = thetaErr / _maxEdgeCost;

        return (int) (normErr * WEIGHT_SCALE);
    }

    /** Coordinate system conventions:
    
    Here's a tag, as you might view the png's...
    
    
        ^
        |  Y axis
        |
                Top right corner is (1,1) in tag coordinates
         -----
        |     |
        |     |
        |     |
         -----   ---> X axis
    
       Bottom left corner is (-1,-1) in tag coordinates
    
       Bits will be read off in row-major order from top left to lower
       right, with the top left bit being the MSB and the bottom right bit
       being the LSB.
    
       Note that these conventions are usually "natural", but can be a bit
       confusing with regard to *image* coordinates (of the individual
       tags or camera images), where the top left corner is (0,0).
     **/

    /** Detect the features in the specified image. We need the
     * optical center, but it is usually fine to pass in (width/2,
     * height/2).
     **/
    private ArrayList<TagDetection> _process(BufferedImage im,
            double opticalCenter[]) {
        // This is a very long function, but it can't really be
        // factored any more simply: it's just a long sequence of
        // sequential operations.

        ///////////////////////////////////////////////////////////
        // Step one. Preprocess image (convert to float (grayscale)
        // and low pass if necessary.)
        FloatImage fimOrig = new FloatImage(im);

        FloatImage fim = fimOrig;
        if (_sigma > 0) {
            int filtsz = ((int) Math.max(3, 3 * _sigma)) | 1;
            float filt[] = SigProc.makeGaussianFilter(_sigma, filtsz);
            fim = fimOrig.filterFactoredCentered(filt, filt);
        }

        if (debug) {
            debugInput = fim.makeImage();
        }

        ///////////////////////////////////////////////////////////
        // Step two. For each pixel, compute the local gradient. We
        // store the direction and magnitude.

        // This step is quite sensitive to noise, since a few bad
        // theta estimates will break up segments, causing us to miss
        // quads. It is helpful to do a Gaussian low-pass on this step
        // even if we don't want it for decoding.
        FloatImage fimseg = fimOrig; // default
        if (_segSigma > 0) {
            if (_segSigma == _sigma) {
                // reuse the already-filtered image...
                fimseg = fim;
            } else {
                // blur anew.
                int filtsz = ((int) Math.max(3, 3 * _segSigma)) | 1;
                float filt[] = SigProc.makeGaussianFilter(_segSigma, filtsz);
                fimseg = fimOrig.filterFactoredCentered(filt, filt);
            }
        }
        if (_segDecimate) {
            fimseg = fimseg.decimateAvg();
        }

        FloatImage fimTheta = new FloatImage(fimseg.width, fimseg.height);
        FloatImage fimMag = new FloatImage(fimseg.width, fimseg.height);

        for (int y = 1; y + 1 < fimseg.height; y++) {
            for (int x = 1; x + 1 < fimseg.width; x++) {

                float Ix = fimseg.get(x + 1, y) - fimseg.get(x - 1, y);
                float Iy = fimseg.get(x, y + 1) - fimseg.get(x, y - 1);

                double mag = Ix * Ix + Iy * Iy;
                double theta = _arctan2(Iy, Ix);

                fimTheta.set(x, y, (float) theta);
                fimMag.set(x, y, (float) mag);
            }
        }

        if (debug) {
            debugTheta = fimTheta.normalize().makeImage();
            debugMag = fimMag.normalize().makeImage();
        }

        ///////////////////////////////////////////////////////////
        // Step three. Segment the edges, grouping pixels with similar
        // thetas together. This is a greedy algorithm: we start with
        // the most similar pixels.  We use 4-connectivity.
        UnionFindSimple uf = new UnionFindSimple(fimseg.width * fimseg.height);

        if (true) {
            int width = fimseg.width;
            int height = fimseg.height;

            long edges[] = new long[width * height * 4];
            int nedges = 0;

            // for efficiency, each edge is encoded as a single
            // long. The constants below are used to pack/unpack the
            // long.
            final long IDA_SHIFT = 40, IDB_SHIFT = 16,
                    INDEX_MASK = (1 << 24) - 1, WEIGHT_MASK = (1 << 16) - 1;

            // bounds on the thetas assigned to this group. Note that
            // because theta is periodic, these are defined such that the
            // average value is contained *within* the interval.
            double tmin[] = new double[width * height];
            double tmax[] = new double[width * height];

            double mmin[] = new double[width * height];
            double mmax[] = new double[width * height];

            for (int y = 0; y + 1 < fimseg.height; y++) {
                for (int x = 0; x + 1 < fimseg.width; x++) {

                    double mag0 = fimMag.get(x, y);
                    if (mag0 < _minMag) {
                        continue;
                    }
                    mmax[y * width + x] = mag0;
                    mmin[y * width + x] = mag0;

                    double theta0 = fimTheta.get(x, y);
                    tmin[y * width + x] = theta0;
                    tmax[y * width + x] = theta0;

                    int edgeCost;

                    // 8 connectivity
                    edgeCost = _edgeCost(theta0, mag0, fimTheta.get(x + 1, y),
                            fimMag.get(x + 1, y));
                    if (edgeCost >= 0) {
                        edges[nedges++] = (((long) y * width + x) << IDA_SHIFT)
                                + (((long) y * width + x + 1) << IDB_SHIFT)
                                + edgeCost;
                    }

                    edgeCost = _edgeCost(theta0, mag0, fimTheta.get(x, y + 1),
                            fimMag.get(x, y + 1));
                    if (edgeCost >= 0) {
                        edges[nedges++] = ((long) (y * width + x) << IDA_SHIFT)
                                + (((long) (y + 1) * width + x) << IDB_SHIFT)
                                + edgeCost;
                    }

                    edgeCost = _edgeCost(theta0, mag0,
                            fimTheta.get(x + 1, y + 1),
                            fimMag.get(x + 1, y + 1));
                    if (edgeCost >= 0) {
                        edges[nedges++] = (((long) y * width + x) << IDA_SHIFT)
                                + (((long) (y + 1) * width + x
                                        + 1) << IDB_SHIFT)
                                + edgeCost;
                    }

                    edgeCost = (x == 0) ? -1
                            : _edgeCost(theta0, mag0,
                                    fimTheta.get(x - 1, y + 1),
                                    fimMag.get(x - 1, y + 1));
                    if (edgeCost >= 0) {
                        edges[nedges++] = (((long) y * width + x) << IDA_SHIFT)
                                + (((long) (y + 1) * width + x
                                        - 1) << IDB_SHIFT)
                                + edgeCost;
                    }
                }
            }

            // sort those edges by weight (lowest weight first).
            edges = _countingSortLongArray(edges, nedges, -1, WEIGHT_MASK);

            // process edges in order of increasing weight, merging
            // clusters if we can do so without exceeding the
            // thetaThresh.
            for (int i = 0; i < nedges; i++) {
                int ida = (int) ((edges[i] >> IDA_SHIFT) & INDEX_MASK);
                int idb = (int) ((edges[i] >> IDB_SHIFT) & INDEX_MASK);

                ida = uf.getRepresentative(ida);
                idb = uf.getRepresentative(idb);

                if (ida == idb) {
                    continue;
                }

                int sza = uf.getSetSize(ida);
                int szb = uf.getSetSize(idb);

                double tmina = tmin[ida], tmaxa = tmax[ida];
                double tminb = tmin[idb], tmaxb = tmax[idb];

                double costa = (tmaxa - tmina);
                double costb = (tmaxb - tminb);

                // bshift will be a multiple of 2pi that aligns the spans
                // of b with a so that we can properly take the union of
                // them.
                double bshift = MathUtil.mod2pi((tmina + tmaxa) / 2,
                        (tminb + tmaxb) / 2) - (tminb + tmaxb) / 2;

                double tminab = Math.min(tmina, tminb + bshift);
                double tmaxab = Math.max(tmaxa, tmaxb + bshift);

                if (tmaxab - tminab > 2 * Math.PI) {
                    tmaxab = tminab + 2 * Math.PI;
                }

                double mmaxab = Math.max(mmax[ida], mmax[idb]);
                double mminab = Math.min(mmin[ida], mmin[idb]);

                // merge these two clusters?
                double costab = (tmaxab - tminab);
                if (costab <= (Math.min(costa, costb)
                        + _thetaThresh / (sza + szb))
                        && (mmaxab - mminab) <= Math.min(mmax[ida] - mmin[ida],
                                mmax[idb] - mmin[idb])
                                + _magThresh / (sza + szb)) {

                    int idab = uf.connectNodes(ida, idb);

                    tmin[idab] = tminab;
                    tmax[idab] = tmaxab;

                    mmin[idab] = mminab;
                    mmax[idab] = mmaxab;
                }
            }
        }

        ///////////////////////////////////////////////////////////
        // Step four. Loop over the pixels again, collecting
        // statistics for each cluster. We will soon fit lines to
        // these points.

        if (debug) {
            debugSegmentation = new BufferedImage(fimseg.width, fimseg.height,
                    BufferedImage.TYPE_INT_RGB);
        }

        HashMap<Integer, ArrayList<double[]>> clusters = new HashMap<Integer, ArrayList<double[]>>();
        for (int y = 0; y + 1 < fimseg.height; y++) {
            for (int x = 0; x + 1 < fimseg.width; x++) {
                if (uf.getSetSize(y * fimseg.width + x) < minimumSegmentSize) {
                    if (debug) {
                        debugSegmentation.setRGB(x, y, 0);
                    }
                    continue;
                }

                int rep = uf.getRepresentative(y * fimseg.width + x);
                if (debug) {
                    debugSegmentation.setRGB(x, y, rep);
                }

                ArrayList<double[]> points = clusters.get(rep);
                if (points == null) {
                    points = new ArrayList<double[]>();
                    clusters.put(rep, points);
                }

                double pt[] = new double[] { x, y, fimMag.get(x, y) };

                points.add(pt);
            }
        }

        ///////////////////////////////////////////////////////////
        // Step five. Loop over the clusters, fitting lines (which we
        // call Segments).
        ArrayList<Segment> segments = new ArrayList<Segment>();

        for (ArrayList<double[]> points : clusters.values()) {
            GLineSegment2D gseg = GLineSegment2D.lsqFitXYW(points);

            // filter short lines
            double length = LinAlg.distance(gseg.p1, gseg.p2);
            if (length < minimumLineLength) {
                continue;
            }

            Segment seg = new Segment();
            double dy = gseg.p2[1] - gseg.p1[1];
            double dx = gseg.p2[0] - gseg.p1[0];

            seg.theta = MathUtil.atan2(gseg.p2[1] - gseg.p1[1],
                    gseg.p2[0] - gseg.p1[0]);
            seg.length = length;

            // We add an extra semantic to segments: the vector
            // p1->p2 will have dark on the left, white on the right.
            // To do this, we'll look at every gradient and each one
            // will vote for which way they think the gradient should
            // go. (This is way more retentive than necessary: we
            // could probably sample just one point!)
            double flip = 0, noflip = 0;
            for (double xyw[] : points) {
                double theta = fimTheta.get((int) xyw[0], (int) xyw[1]);
                double mag = fimMag.get((int) xyw[0], (int) xyw[1]);

                // err *should* be +Math.PI/2 for the correct winding,
                // but if we've got the wrong winding, it'll be around
                // -Math.PI/2.
                double err = MathUtil.mod2pi(theta - seg.theta);

                if (err < 0) {
                    noflip += mag;
                } else {
                    flip += mag;
                }
            }

            if (flip > noflip) {
                seg.theta += Math.PI;
            }

            double dot = dx * Math.cos(seg.theta) + dy * Math.sin(seg.theta);
            double xbug = 0, ybug = 0;
            if (dot > 0) {
                seg.x0 = gseg.p2[0] + xbug;
                seg.y0 = gseg.p2[1] + ybug;
                seg.x1 = gseg.p1[0] + xbug;
                seg.y1 = gseg.p1[1] + ybug;
            } else {
                seg.x0 = gseg.p1[0];
                seg.y0 = gseg.p1[1];
                seg.x1 = gseg.p2[0];
                seg.y1 = gseg.p2[1];
            }

            if (_segDecimate) {
                seg.x0 = 2 * seg.x0 + .5;
                seg.y0 = 2 * seg.y0 + .5;
                seg.x1 = 2 * seg.x1 + .5;
                seg.y1 = 2 * seg.y1 + .5;
                seg.length *= 2;
            }

            segments.add(seg);
        }

        int width = fim.width, height = fim.height;

        /*
        if (debug && debugSegments != null) {
        
            for (Segment seg : segments) {
                double cx = (seg.x0 + seg.x1)/2, cy = (seg.y0 + seg.y1)/2;
        
                double notch = Math.max(2, 0.1*seg.length);
        
                debugSegments.addBack(new VisChain(LinAlg.translate(0, height, 0),
                                                   LinAlg.scale(1, -1, 1),
                                                   new VzLines(new VisVertexData(new double[] { seg.x0, seg.y0},
                                                                                 new double[] { seg.x1, seg.y1}),
                                                               VzLines.LINE_STRIP,
                                                               new VzLines.Style(Color.yellow, 1)),
                                                   new VzLines(new VisVertexData(new double[] { cx,  cy },
                                                                                 new double[] { cx + notch*Math.sin(seg.theta),
                                                                                                cy - notch*Math.cos(seg.theta) }),
                                                               VzLines.LINE_STRIP,
                                                               new VzLines.Style(Color.yellow, 1)),
                                                   new VzPoints(new VisVertexData(new double[] { seg.x0, seg.y0 }),
                                                                new VzPoints.Style(Color.red, 4))
                                          ));
        
            }
        }
         */

        ////////////////////////////////////////////////////////////////
        // Step six. For each segment, find segments that begin where
        // this segment ends. (We will chain segments together
        // next...) The gridder accelerates the search by building
        // (essentially) a 2D hash table.
        Gridder<Segment> gridder = new Gridder<Segment>(0, 0, width, height,
                10);

        // add every segment to the hash table according to the
        // position of the segment's first point. (Remember that the
        // first point has a specific meaning due to our left-hand
        // rule above.)
        for (Segment seg : segments) {
            gridder.add(seg.x0, seg.y0, seg);
        }

        // Now, find child segments that begin where each parent
        // segments ends.
        for (Segment parent : segments) {

            // compute length of the line segment
            GLine2D parentLine = new GLine2D(
                    new double[] { parent.x0, parent.y0 },
                    new double[] { parent.x1, parent.y1 });

            for (Segment child : gridder.find(parent.x1, parent.y1,
                    quadSearchRangePix
                            + quadSearchRangeFraction * parent.length)) {
                //            for (Segment child : gridder.find(parent.x1, parent.y1, 5+parent.length)) {
                // require child to have the right handedness...
                if (MathUtil.mod2pi(child.theta - parent.theta) > 0) {
                    continue;
                }

                // compute intersection of points.
                GLine2D childLine = new GLine2D(
                        new double[] { child.x0, child.y0 },
                        new double[] { child.x1, child.y1 });

                double p[] = parentLine.intersectionWith(childLine);
                if (p == null) {
                    continue;
                }

                double parentDist = LinAlg.distance(p,
                        new double[] { parent.x1, parent.y1 });
                double childDist = LinAlg.distance(p,
                        new double[] { child.x0, child.y0 });

                if (Math.max(parentDist, childDist) > parent.length) {
                    continue;
                }

                // everything's okay, this child is a reasonable successor.
                parent.children.add(child);

            }
        }

        ////////////////////////////////////////////////////////////////
        // Step seven. Search all connected segments to see if any
        // form a loop of length 4. Add those to the quads list.
        ArrayList<Quad> quads = new ArrayList<Quad>();

        if (true) {
            Segment tmp[] = new Segment[5];
            for (Segment seg : segments) {
                tmp[0] = seg;
                _search(quads, tmp, seg, 0);
            }
        }

        /* Remove Vis-dependent code.
        if (debug && debugQuads != null) {
            for (Quad q : quads) {
                debugQuads.addBack(new VisChain(LinAlg.translate(0, height, 0),
                                                LinAlg.scale(1, -1, 1),
                                                new VzLines(new VisVertexData(q.p[0], q.p[1], q.p[2], q.p[3], q.p[0]),
                                                            VzLines.LINE_STRIP,
                                                            new VzLines.Style(Color.orange, 2))));
            }
        }
         */

        ////////////////////////////////////////////////////////////////
        // Step eight. Decode the quads. For each quad, we first
        // estimate a threshold color to decided between 0 and
        // 1. Then, we read off the bits and see if they make sense.
        ArrayList<TagDetection> detections = new ArrayList<TagDetection>();

        for (Quad quad : quads) {
            // Find a threshold
            GrayModel blackModel = new GrayModel();
            GrayModel whiteModel = new GrayModel();

            /* Remove Vis-dependent code.
            VisVertexData vdblack = null;
            VisVertexData vdwhite = null;
            VisVertexData vdsamp = null;
            
            if (debug && debugSamples != null) {
                vdblack = new VisVertexData();
                vdwhite = new VisVertexData();
                vdsamp = new VisVertexData();
            }
             */

            // sample points around the black and white border in
            // order to calibrate our gray threshold. This code is
            // simpler if we loop over the whole rectangle and discard
            // the points we don't want.
            int dd = 2 * _tagFamily.blackBorder + _tagFamily.d;

            for (int iy = -1; iy <= dd; iy++) {
                for (int ix = -1; ix <= dd; ix++) {
                    double y = (iy + .5) / dd;
                    double x = (ix + .5) / dd;

                    double pxy[] = quad.interpolate01(x, y);
                    int irx = (int) (pxy[0] + .5);
                    int iry = (int) (pxy[1] + .5);

                    if (irx < 0 || irx >= width || iry < 0 || iry >= height) {
                        continue;
                    }

                    float v = fim.get(irx, iry);

                    if ((iy == -1 || iy == dd) || (ix == -1 || ix == dd)) {
                        // part of the outer white border.
                        whiteModel.addObservation(x, y, v);

                        /* Remove Vis-dependent code.
                        if (debug && debugSamples != null)
                            vdwhite.add(pxy);
                         */
                    } else if ((iy == 0 || iy == (dd - 1))
                            || (ix == 0 || ix == (dd - 1))) {
                        // part of the outer black border.
                        blackModel.addObservation(x, y, v);

                        /* Remove Vis-dependent code.
                        if (debug && debugSamples != null)
                            vdblack.add(pxy);
                         */
                    }
                }
            }

            boolean bad = false;
            long tagCode = 0;

            // Try reading off the bits.
            // XXX: todo: multiple samples within each cell and vote?

            // reminder: the MSB is at tag coordinates (-1,1) (the top left).
            for (int iy = _tagFamily.d - 1; iy >= 0; iy--) {
                for (int ix = 0; ix < _tagFamily.d; ix++) {
                    double y = (_tagFamily.blackBorder + iy + .5) / dd;
                    double x = (_tagFamily.blackBorder + ix + .5) / dd;

                    double pxy[] = quad.interpolate01(x, y);
                    int irx = (int) (pxy[0] + .5);
                    int iry = (int) (pxy[1] + .5);

                    if (irx < 0 || irx >= width || iry < 0 || iry >= height) {
                        bad = true;
                        continue;
                    }

                    double threshold = (blackModel.interpolate(x, y)
                            + whiteModel.interpolate(x, y)) * .5;

                    /* Remove Vis-dependent code.
                    if (debug && debugSamples != null)
                        vdsamp.add(pxy);
                     */

                    float v = fim.get(irx, iry);

                    tagCode = tagCode << 1;
                    if (v > threshold) {
                        tagCode |= 1;
                    }
                }
            }

            /* Remove Vis-dependent code.
            if (debug && debugSamples != null) {
                debugSamples.addBack(new VisChain(LinAlg.translate(0, height, 0),
                                                  LinAlg.scale(1, -1, 1),
                                                  new VzPoints(vdwhite,
                                                               new VzPoints.Style(Color.white, 3)),
                                                  new VzPoints(vdblack,
                                                               new VzPoints.Style(Color.black, 3)),
                                                  new VzPoints(vdsamp,
                                                               new VzPoints.Style(Color.orange, 4))));
            }
             */

            if (!bad) {
                TagDetection d = new TagDetection();
                _tagFamily.decode(d, tagCode);

                // rotate points in detection according to decoded
                // orientation. Thus the order of the points in the
                // detection object can be used to determine the
                // orientation of the target.
                d.p = new double[4][];

                for (int i = 0; i < 4; i++) {
                    d.p[(4 + i - d.rotation) % 4] = quad.p[i];
                }

                // compute the homography (and rotate it appropriately)
                d.homography = quad.homography.getH();
                d.hxy = new double[2]; //quad.homography.getCXY();

                if (true) {
                    double c = Math.cos(d.rotation * Math.PI / 2.0);
                    double s = Math.sin(d.rotation * Math.PI / 2.0);
                    double R[][] = new double[][] { { c, -s, 0 }, { s, c, 0 },
                            { 0, 0, 1 } };
                    d.homography = LinAlg.matrixAB(d.homography, R);
                }

                /* Unreachable code.
                if (false) {
                    d.homography[1][1] *= -1;
                    d.homography[0][1] *= -1;
                    d.homography[0][2] *= -1;
                    d.homography[2][2] *= -1;
                    d.homography[2][1] *= -1;
                }
                 */

                if (d.good) {
                    detections.add(d);
                    d.cxy = quad.interpolate01(.5, .5);
                    d.observedPerimeter = quad.observedPerimeter;
                }
            }
        }

        /* Remove Vis-dependent code.
        if (debug) {
            if (debugSegments != null)
                debugSegments.swap();
            if (debugQuads != null)
                debugQuads.swap();
            if (debugSamples != null)
                debugSamples.swap();
            if (debugLabels != null)
                debugLabels.swap();
        }
         */

        ////////////////////////////////////////////////////////////////
        // Step nine. Some quads may be detected more than once, due
        // to partial occlusion and our aggressive attempts to recover
        // from broken lines. When two quads (with the same id)
        // overlap, we will keep the one with the lowest error, and if
        // the error is the same, the one with the greatest observed
        // perimeter.

        ArrayList<TagDetection> goodDetections = new ArrayList<TagDetection>();

        // NOTE: allow multiple (non-overlapping) detections of the same target.
        for (TagDetection d : detections) {

            boolean newFeature = true;

            for (int odidx = 0; odidx < goodDetections.size(); odidx++) {
                TagDetection od = goodDetections.get(odidx);

                if (d.id != od.id || !_detectionsOverlapTooMuch(d, od)) {
                    continue;
                }

                // there's a conflict. we must pick one to keep.
                newFeature = false;

                // this detection is worse than the previous one... just don't use it.
                if (d.hammingDistance > od.hammingDistance) {
                    continue;
                }

                // otherwise, keep the new one if it either has
                // *lower* error, or has greater perimeter
                if (d.hammingDistance < od.hammingDistance
                        || d.observedPerimeter > od.observedPerimeter) {
                    goodDetections.set(odidx, d);
                }
            }

            if (newFeature) {
                goodDetections.add(d);
            }
        }

        detections = goodDetections;

        ////////////////////////////////////////////////////////////////
        // I thought it would never end.
        return detections;
    }

    /** quads: any discovered quads will be added to this list.
    path: The segments currently part of the search.
    parent: The first segment in the quad.
    depth: how deep in the search are we?
     **/
    private void _search(ArrayList<Quad> quads, Segment[] path, Segment parent,
            int depth) {
        // terminal depth occurs when we've found four segments.
        if (depth == 4) {
            // Is the first segment the same as the last segment (i.e., a loop?)
            if (path[4] == path[0]) {

                // the 4 corners of the quad as computed by the intersection of segments.
                double p[][] = new double[4][];
                double observedPerimeter = 0;

                boolean bad = false;
                for (int i = 0; i < 4; i++) {
                    // compute intersections between all the
                    // lines. This will give us sub-pixel accuracy for
                    // the corners of the quad.
                    GLine2D linea = new GLine2D(
                            new double[] { path[i].x0, path[i].y0 },
                            new double[] { path[i].x1, path[i].y1 });
                    GLine2D lineb = new GLine2D(
                            new double[] { path[i + 1].x0, path[i + 1].y0 },
                            new double[] { path[i + 1].x1, path[i + 1].y1 });
                    p[i] = linea.intersectionWith(lineb);

                    observedPerimeter += path[i].length();

                    // no intersection? Occurs when the lines are almost parallel.
                    if (p[i] == null) {
                        bad = true;
                    }
                }

                // eliminate quads that don't form a simply connected
                // loop (i.e., those that form an hour glass, or wind
                // the wrong way.)
                if (!bad) {
                    double t0 = MathUtil.atan2(p[1][1] - p[0][1],
                            p[1][0] - p[0][0]);
                    double t1 = MathUtil.atan2(p[2][1] - p[1][1],
                            p[2][0] - p[1][0]);
                    double t2 = MathUtil.atan2(p[3][1] - p[2][1],
                            p[3][0] - p[2][0]);
                    double t3 = MathUtil.atan2(p[0][1] - p[3][1],
                            p[0][0] - p[3][0]);

                    double ttheta = MathUtil.mod2pi(t1 - t0)
                            + MathUtil.mod2pi(t2 - t1)
                            + MathUtil.mod2pi(t3 - t2)
                            + MathUtil.mod2pi(t0 - t3);

                    // the magic value is -2*PI. It should be exact,
                    // but we allow for (lots of) numeric imprecision.
                    if (ttheta < -7 || ttheta > -5) {
                        bad = true;
                    }
                }

                if (!bad) {
                    double d0 = LinAlg.distance(p[0], p[1]);
                    double d1 = LinAlg.distance(p[1], p[2]);
                    double d2 = LinAlg.distance(p[2], p[3]);
                    double d3 = LinAlg.distance(p[3], p[0]);
                    double d4 = LinAlg.distance(p[0], p[2]);
                    double d5 = LinAlg.distance(p[1], p[3]);

                    // check sizes
                    if (d0 < minimumTagSize || d1 < minimumTagSize
                            || d2 < minimumTagSize || d3 < minimumTagSize
                            || d4 < minimumTagSize || d5 < minimumTagSize) {
                        bad = true;
                    }

                    // check aspect ratio
                    double dmax = Math.max(Math.max(d0, d1), Math.max(d2, d3));
                    double dmin = Math.min(Math.min(d0, d1), Math.min(d2, d3));

                    if (dmax > dmin * maxQuadAspectRatio) {
                        bad = true;
                    }
                }

                if (!bad) {
                    Quad q = new Quad(p);
                    q.observedPerimeter = observedPerimeter;
                    quads.add(q);
                }
            }
            return;
        }

        // Not terminal depth. Recurse on any children that obey the correct handedness.
        for (Segment child : parent.children) {
            // (handedness was checked when we created the children)

            // we could rediscover each quad 4 times (starting from
            // each corner). If we had an arbitrary ordering over
            // points, we can eliminate the redundant detections by
            // requiring that the first corner have the lowest
            // value. We're arbitrarily going to use theta...
            if (child.theta > path[0].theta) {
                continue;
            }

            path[depth + 1] = child;
            _search(quads, path, child, depth + 1);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** When growing components, the intra component variation is
     * allowed to grow when the component is small in size. This
     * threshold affects how much. The default is 1200.
     */
    private double _magThresh = 1200;

    /** When connecting edges, what is the maximum range allowed for
     * the gradient directions?
     */
    private double _maxEdgeCost = Math.toRadians(30);

    /** Do not consider pixels whose gradient magnitude is less than
     * minMag. Small values make the detector more sensitive, but also
     * force us to consider many more edges resulting in slower
     * computation time. A value of 0.001 is very sensitive. A value
     * of 0.01 is quite fast. The default is 0.004.
     */
    private double _minMag = 0.004;

    /** Instead of blurring the input image before segmentation, we
     * can achieve similar effects by decimating the image by a factor
     * of two. When enabled, this option applies a block LPF filter of
     * width 2, then decimates the image. With this option, not only
     * can we safely set segSigma = 0, but the slowest part of the
     * algorithm (the segmentation) runs about 4 times faster. The
     * downside is that the position of the targets is determined
     * based on the segmentation: lower resolution will result in more
     * localization error. However, the effect on quality is quite
     * modest, and this optimization is generally recommended (along
     * with segSigma = 0). If segSigma is non-zero, the filtering by
     * segSigma occurs first, followed by the block LPF, and the
     * decimation. This defaults to false, indicating that the option
     * is not enabled.
     * @see #isSegDecimate()
     */
    private boolean _segDecimate = false;

    /** Gaussian smoothing kernel applied to image (0 == no filter)
     * used when detecting the outline of the box. It is almost always
     * useful to have some filtering, since the loss of small details
     * won't hurt. Recommended value = 0.8. The case where sigma ==
     * segsigma has been optimized to avoid a redundant filter
     * operation. **/
    private double _segSigma = 0.8;

    /** The Gaussian smoothing kernel used when sampling bits. */
    private double _sigma = 0;

    /** The tag family being detected. */
    private TagFamily _tagFamily;

    /** The name of the tag family being detected. */
    private String _tagFamilyName = "Tag36h11";

    /** The list of tags most recently detected by an invocation of filter(). */
    private ArrayList<TagDetection> _tags;

    /** When growing components, the intra component variation is
     * allowed to grow when the component is small in size. This
     * threshold affects how much. The default is 100.
     */
    private double _thetaThresh = 100;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Fits a grayscale model over an area of the form:
    Ax + By + Cxy + D = value
    
    We use this model to compute spatially-varying thresholds for
    reading bits.
     **/
    static class GrayModel {
        // we're solving Ax = b. For each observation, we add a row to
        // A of the form [x y xy 1] and to be of the form [gray]. x is
        // the vector [A B C D].
        //
        // The least-squares solution to the system is x = inv(A'A)A'b
        double A[][] = new double[4][4]; // The A'A matrix
        double b[] = new double[4]; // The A'b matrix
        double X[]; // our solution, [A B C D]

        int nobs; // how many observations?

        public void addObservation(double x, double y, double gray) {
            double xy = x * y;

            // update only upper-right elements. A'A is symmetric,
            // we'll fill the other elements in later.
            A[0][0] += x * x;
            A[0][1] += x * y;
            A[0][2] += x * xy;
            A[0][3] += x;
            A[1][1] += y * y;
            A[1][2] += y * xy;
            A[1][3] += y;
            A[2][2] += xy * xy;
            A[2][3] += xy;
            A[3][3] += 1;

            b[0] += x * gray;
            b[1] += y * gray;
            b[2] += xy * gray;
            b[3] += gray;

            nobs++;
            X = null; // force a new solution to be computed.
        }

        int getNumObservations() {
            return nobs;
        }

        void compute() {
            if (X != null) {
                return;
            }

            if (nobs >= 6) {
                // we really only need 4 linearly independent
                // observations to fit our answer, but we'll be very
                // sensitive to noise if we don't have an
                // over-determined system. Thus, require at least 6
                // observations (or we'll use a constant model below).

                // make symmetric
                for (int i = 0; i < 4; i++) {
                    for (int j = i + 1; j < 4; j++) {
                        A[j][i] = A[i][j];
                    }
                }

                double Ainv[][] = LinAlg.inverse(A);
                if (Ainv != null) {
                    X = LinAlg.matrixAB(Ainv, b);
                }
            }

            if (X == null) {
                // not enough samples to fit a good model. Use a flat model.
                X = new double[4];
                X[3] = b[3] / nobs;
            }
        }

        public double interpolate(double x, double y) {
            compute();

            return X[0] * x + X[1] * y + X[2] * x * y + X[3];
        }
    }

    /** Represents a line fit to a set of pixels whose gradients are
     * similar.
     **/
    class Segment {
        double x0, y0, x1, y1;
        double theta; // gradient direction (points towards white)
        double length; // length of line segment in pixels
        ArrayList<Segment> children = new ArrayList<Segment>();

        public double length() {
            return Math.sqrt((x1 - x0) * (x1 - x0) + (y1 - y0) * (y1 - y0));
        }
    }

    /** Represents four segments that form a loop, and might be a tag. **/
    class Quad {
        // points for the quad (in pixel coordinates), in counter
        // clockwise order. These points are the intersections of
        // segments.
        double p[][] = new double[4][];

        // The total length (in pixels) of the actual perimeter
        // observed for the quad. This is in contrast to the geometric
        // perimeter, some of which may not have been directly
        // observed but rather inferred by intersecting
        // segments. Quads with more observed perimeter are preferred
        // over others.
        double observedPerimeter;

        // Given that the whole quad spans from (0,0) to (1,1) in
        // "quad space", compute the pixel coordinates for a given
        // point within that quad. Note that for most of the Quad's
        // existence, we will not know the correct orientation of the
        // tag.
        Homography33b homography;

        /** (x,y) are the optical center of the camera, which is
         * needed to correctly compute the homography.
         **/
        public Quad(double p[][]) {
            this.p = p;

            homography = new Homography33b(); //(opticalCenter[0], opticalCenter[1]);
            //            homography = new Homography33(opticalCenter[0], opticalCenter[1]);
            homography.addCorrespondence(-1, -1, p[0][0], p[0][1]);
            homography.addCorrespondence(1, -1, p[1][0], p[1][1]);
            homography.addCorrespondence(1, 1, p[2][0], p[2][1]);
            homography.addCorrespondence(-1, 1, p[3][0], p[3][1]);

            //       for (double wi[] : homography.correspondences) {
            //                System.out.printf("%15f %15f %15f %15f\n", wi[0], wi[1], wi[2], wi[3]);
            //            }
        }

        // Same as interpolate, except that the coordinates are
        // interpreted between 0 and 1, instead of -1 and 1.
        double[] interpolate01(double x, double y) {
            return interpolate(2 * x - 1, 2 * y - 1);
        }

        // interpolate given that the lower left corner of the lower
        // left cell is at (-1,-1) and the upper right corner of
        // the upper right cell is at (1,1)
        double[] interpolate(double x, double y) {
            return homography.project(x, y);
        }
    }

}
