/* An interface to OpenCV computer vision functions.

   @Copyright (c) 2015-2017 The Regents of the University of California.
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

   The BSD License
   By downloading, copying, installing or using the software you agree to this license.
   If you do not agree to this license, do not download, install,
   copy or use the software.


   License Agreement
   For Open Source Computer Vision Library
   (3-clause BSD License)

   Copyright (C) 2000-2015, Intel Corporation, all rights reserved.
   Copyright (C) 2009-2011, Willow Garage Inc., all rights reserved.
   Copyright (C) 2009-2015, NVIDIA Corporation, all rights reserved.
   Copyright (C) 2010-2013, Advanced Micro Devices, Inc., all rights reserved.
   Copyright (C) 2015, OpenCV Foundation, all rights reserved.
   Copyright (C) 2015, Itseez Inc., all rights reserved.
   Third party copyrights are property of their respective owners.

   Redistribution and use in source and binary forms, with or without modification,
   are permitted provided that the following conditions are met:

   * Redistributions of source code must retain the above copyright notice,
   this list of conditions and the following disclaimer.

   * Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

   * Neither the names of the copyright holders nor the names of the contributors
   may be used to endorse or promote products derived from this software
   without specific prior written permission.

   This software is provided by the copyright holders and contributors "as is" and
   any express or implied warranties, including, but not limited to, the implied
   warranties of merchantability and fitness for a particular purpose are disclaimed.
   In no event shall copyright holders or contributors be liable for any direct,
   indirect, incidental, special, exemplary, or consequential damages
   (including, but not limited to, procurement of substitute goods or services;
   loss of use, data, or profits; or business interruption) however caused
   and on any theory of liability, whether in contract, strict liability,
   or tort (including negligence or otherwise) arising in any way out of
   the use of this software, even if advised of the possibility of such damage.


*/
package org.ptolemy.opencv;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBuffer;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Vector;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.*;
import org.opencv.objdetect.CascadeClassifier;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.util.FileUtilities;

import com.jhlabs.image.AbstractBufferedImageOp;

///////////////////////////////////////////////////////////////////
//// ComputerVision

/** A set of OpenCV image filters.
 *
 *  The parameters of the filter are:
 *  <ul>
 *  <li> <i>MinFaceSize</i>: Minimum face size to be considered ( one side of a bounding square)</li>
 *  <li> <i>MaxFaceSize</i>: Maximum face size to be considered ( one side of a bounding square)</li>
 *  </ul>
 *
 *  @author Elizabeth Osyk, based on FaceRecognizer by Ilga Akkaya
 *  @version $Id$
 *  @since Ptolemy II 11.0
 *  @Pt.ProposedRating Yellow (cxh)
 *  @Pt.AcceptedRating Red (cxh)
 */
public class ComputerVision {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** Construct an instance of ComputerVision and load OpenCV.
     */
    public ComputerVision() throws IOException {
        super();
        
        /** Load Native C Library for OpenCV */
        //System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        // Use a better loader that looks for the shared library for the Mac.
        OpenCVLoader.loadOpenCV(Core.NATIVE_LIBRARY_NAME);
        
        _blur = new Blur();
        _dilate = new Dilate();
        _erode = new Erode();
        _findContours = new FindContours();
        _findEdges = new FindEdges();
        _gaussianBlur = new GaussianBlur();
        _histogram = new Histogram();
        _makeBGRA = new MakeBGRA();
        _makeGray = new MakeGray();
        _makeHSV = new MakeHSV();
        _makeYUV = new MakeYUV();
        _medianBlur = new MedianBlur();
    }

    /** Apply the specified filter to the source image. 

     *  @param source The source image to transform.
     *  @return The transformed image.
     */
    public BufferedImage filter(BufferedImage source, String filterName) 
            throws IllegalActionException {

        switch (filterName) {
            case "blur" : return _blur.filter(source, null);
            case "dilate" : return _dilate.filter(source,  null);
            case "erode" : return _erode.filter(source, null);
            case "findContours" : return _findContours.filter(source, null);
            case "findEdges" : return _findEdges.filter(source,  null);
            case "gaussianBlur" :  return _gaussianBlur.filter(source, null);
            case "histogram" : return _histogram.filter(source, null);
            case "makeBGRA" : return _makeBGRA.filter(source,  null);
            case "makeGray" : return _makeGray.filter(source, null);
            case "makeHSV" : return _makeHSV.filter(source, null);
            case "makeYUV" : return _makeYUV.filter(source, null);
            case "medianBlur" : return _medianBlur.filter(source, null);
            default: throw new IllegalActionException("ComputerVision: No filter found for " + filterName);
        }
    }

    /**
     * Convert a BufferedImage object to OpenCV's Mat representation.
     * @param source BufferedImage input
     * @return Mat matrix
     */
    public Mat bufferedImage2Mat(BufferedImage source) {
        int w = source.getWidth();
        int h = source.getHeight();
        Mat outputImage = new Mat(h, w, CvType.CV_8UC3);
        byte [] pixels = null;
        DataBuffer dataBuffer = source.getRaster().getDataBuffer();
        if (dataBuffer instanceof DataBufferByte) {
            // Most of the time we get bytes.
            pixels = ((DataBufferByte) source.getRaster().getDataBuffer()).getData();
        } else {
            // The WebcamDummyDriver will emit a TYPE_INT_RGB which
            // uses a DataBufferInt so we convert the BufferImage to a
            // TYPE_3BYTE_BGR.

            // See http://stackoverflow.com/questions/33403526/how-to-match-the-color-models-of-bufferedimage-and-mat
            BufferedImage greyImage = new BufferedImage(source.getWidth(),
                                                        source.getHeight(),
                                                        BufferedImage.TYPE_3BYTE_BGR);
            Graphics g = greyImage.getGraphics();
            g.drawImage(source, 0, 0, null);
            g.dispose();

            pixels = ((DataBufferByte) greyImage.getRaster().getDataBuffer()).getData();
        }
        outputImage.put(0, 0, pixels);
        return outputImage;
    }

    /**
     * Convert a Mat image into a BufferedImage.
     * @param matrix Mat input
     * @return BufferedImage output
     */
    public BufferedImage mat2BufferedImage(Mat matrix) {

        int h = matrix.height();
        int w = matrix.width();

        BufferedImage destination = new BufferedImage(w,h,BufferedImage.TYPE_3BYTE_BGR);

        for (int x = 0; x < h; x++) {
            for (int y = 0; y < w; y++) {
                double[] color = matrix.get(x, y);
                if ( color.length  > 1 ) {
                    int r = (int) color[0]; int g = (int) color[1]; int b = (int) color[2];
                    destination.setRGB(y, x, new Color(r,g,b).getRGB());
                } else {
                    int c = (int) color[0];
                    destination.setRGB(y, x, new Color(c,c,c).getRGB());
                }
            }
        }
        return destination;
    }
    
    /** Set the blur size.  1 to 25.
     * @param blurSize The new blur size.
     */
    public void setBlurSize(int blurSize) {
        if (blurSize < 1) {
            _blurSize = 1;
        } else if (blurSize > 25) {
            _blurSize = 25;
        } else {
            _blurSize = blurSize;
        }
    }
    
    /** Set the canny edge detection threshold.  10 to 150.
     * @param threshold The new threshold
     */
    public void setCannyThreshold(int threshold) {
        if (threshold < 10) {
            _cannyThreshold = 10;
        } else if (threshold > 150) {
            _cannyThreshold = 150;
        } else {
            _cannyThreshold = threshold;
        }
    }
    
    /** Set the erosion size.  0 to 21.
     * @param erosionSize The new erosion size.
     */
    public void setErosionSize(int erosionSize) {
        if (erosionSize < 1) {
            _erosionSize = 1;
        } else if (erosionSize > 25) {
            _erosionSize = 25;
        } else {
            _erosionSize = erosionSize;
        }
    }
    
    /** Return a string description of the filter.
     *  @return The string "ComputerVisionFilter".
     */
    @Override
    public String toString() {
        return "ComputerVisionFilter";
    }


    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    
    /** A filter that blurs the image. 
     */
    public class Blur extends AbstractBufferedImageOp {
        /** Blur the source image. 
         *  @param source The source image.
         *  @param destination Not used here.  Required by superclass.
         *  @return The blurred image.
         */
        @Override
        public BufferedImage filter(BufferedImage src, BufferedImage dest) {
            Mat source = bufferedImage2Mat(src);
            Mat converted = new Mat();
            Imgproc.cvtColor(source, converted, Imgproc.COLOR_RGB2BGRA);

            Mat result = new Mat();
            Size blurSize = new Size(_blurSize, _blurSize);
            Imgproc.blur(converted, result, blurSize);
            
            return mat2BufferedImage(result);
        }
    }
    
    /** A filter that dilates the image.
     */
    public class Dilate extends AbstractBufferedImageOp {
        /** Dilate the source image. 
         *  @param source The source image.
         *  @param destination Not used here.  Required by superclass.
         *  @return The dilated image.
         */
        @Override
        public BufferedImage filter(BufferedImage src, BufferedImage dest) {
            Mat source = bufferedImage2Mat(src);
            Mat converted = new Mat();
            Imgproc.cvtColor(source, converted, Imgproc.COLOR_RGB2BGRA);

            Size erosionSize = new Size(2*_erosionSize+1, 2*_erosionSize+1);
            int erosionType = Imgproc.MORPH_RECT;
            Mat element = Imgproc.getStructuringElement(erosionType, erosionSize);
            Mat result = new Mat();
            // Note: Browser version additionally specifies border arguments.
            Imgproc.dilate(source, result, element, new Point(-1, -1), 1);
            
            return mat2BufferedImage(result);
        }
    }
    
    /** A filter that erodes the image.
     */
    public class Erode extends AbstractBufferedImageOp {
        /** Erode the source image. 
         *  @param source The source image.
         *  @param destination Not used here.  Required by superclass.
         *  @return The eroded image.
         */
        @Override
        public BufferedImage filter(BufferedImage src, BufferedImage dest) {
            Mat source = bufferedImage2Mat(src);
            Mat converted = new Mat();
            Imgproc.cvtColor(source, converted, Imgproc.COLOR_RGB2BGRA);

            Size erosionSize = new Size(2*_erosionSize+1, 2*_erosionSize+1);
            int erosionType = Imgproc.MORPH_RECT;
            Mat element = Imgproc.getStructuringElement(erosionType, erosionSize);
            Mat result = new Mat();
            // Note: Browser version additionally specifies border arguments.
            Imgproc.erode(source, result, element, new Point(-1,-1), 1);
            
            return mat2BufferedImage(result);
        }
    }
    
    /** A filter that finds contours in the image.
     */
    public class FindContours extends AbstractBufferedImageOp {
        /** Find contours in the source image. 
         *  @param source The source image.
         *  @param destination Not used here.  Required by superclass.
         *  @return An image of the contours.
         */
        @Override
        public BufferedImage filter(BufferedImage src, BufferedImage dest) {
            Mat source = bufferedImage2Mat(src);
            Mat converted = new Mat();
            Imgproc.cvtColor(source, converted, Imgproc.COLOR_RGB2BGRA);

            Mat blurred = new Mat();
            Mat cannied = new Mat();
            Mat hierarchy = new Mat();
            
            // Note: Browser implementation also includes border arguments.
            Imgproc.blur(converted, blurred, new Size(5,5));
            Imgproc.Canny(blurred, cannied, _cannyThreshold, _cannyThreshold*2, 3, false);
            
            ArrayList<MatOfPoint> contours = new ArrayList();
            Imgproc.findContours(cannied, contours, hierarchy, 3, 2, new Point(0,0));
            
            // Note:  This implementation does not draw the convex hull as in 
            // the browser version.  The Java version of Impgproc.convexHull
            // does not allow the option to return the points directly (only
            // indices to the points), and I'm not sure how to fetch the points.
            
            // Draw contours 
            Size size = cannied.size();
            Mat drawing = Mat.zeros(size, CvType.CV_8UC4);
            
            for(int i = 0; i< contours.size(); i++ )
            {
                Scalar color = new Scalar(Math.random()*255, Math.random()*255, Math.random()*255);
                Imgproc.drawContours(drawing, contours, i, color, 2, 8, hierarchy, 0, new Point(0,0));
            }
            return mat2BufferedImage(drawing);
        }
    }

    /** A filter that finds edges in an image. 
     */
    public class FindEdges extends AbstractBufferedImageOp {
        /** Find edges in an image. 
         *  @param source The source image.
         *  @param destination Not used here.  Required by superclass.
         *  @return An image of the edges.
         */
        @Override
        public BufferedImage filter(BufferedImage src, BufferedImage dest) {
            Mat source = bufferedImage2Mat(src);
            Mat converted = new Mat();
            Imgproc.cvtColor(source, converted, Imgproc.COLOR_RGB2BGRA);

            Mat blurred = new Mat();
            Mat result = new Mat();
            
            // Note: Browser implementation also includes border arguments.
            Imgproc.blur(converted, blurred, new Size(5,5));
            Imgproc.Canny(blurred, result, _cannyThreshold, _cannyThreshold*2, 3, false);

            return mat2BufferedImage(result);
        }
    }
    
    /** A filter that applies a gaussian blur to an image.
     */
    public class GaussianBlur extends AbstractBufferedImageOp {
        /** Apply a gaussian blur to an image. 
         *  @param source The source image.
         *  @param destination Not used here.  Required by superclass.
         *  @return The blurred image.
         */
        @Override
        public BufferedImage filter(BufferedImage src, BufferedImage dest) {
            Mat source = bufferedImage2Mat(src);
            Mat converted = new Mat();
            Imgproc.cvtColor(source, converted, Imgproc.COLOR_RGB2BGRA);
            
            Mat result = new Mat();
            Size blurSize = new Size(2*_blurSize+1, 2*_blurSize+1);
            // Note : Browser version also includes border argument.
            Imgproc.GaussianBlur(source, result, blurSize, 0, 0);
            
            return mat2BufferedImage(result);
        }
    }
    
    /** A class for creating a histogram from the image.  
     */
    public class Histogram extends AbstractBufferedImageOp {
        /** Create a histogram from an image. 
         *  @param source The source image.
         *  @param destination Not used here.  Required by superclass.
         *  @return A histogram.
         */
        @Override
        public BufferedImage filter(BufferedImage src, BufferedImage dest) {
            Mat source = bufferedImage2Mat(src);
            
            int numBins = 255;
            
            ArrayList<Mat> rgbPlanes = new ArrayList();
            Core.split(source, rgbPlanes);

            MatOfInt planes = new MatOfInt();
            MatOfFloat ranges = new MatOfFloat();
            ranges.push_back(new MatOfFloat(0));
            ranges.push_back(new MatOfFloat(255));
            
            MatOfInt histSize = new MatOfInt();
            histSize.push_back(new MatOfInt(numBins));
            
            Mat histr = new Mat();
            Mat histg = new Mat();
            Mat histb = new Mat();
            
            planes.push_back(new MatOfInt(0));
            
            Imgproc.calcHist(rgbPlanes, planes, new Mat(), histr, histSize, ranges, false);
            planes.setTo(new MatOfInt(1));
            Imgproc.calcHist(rgbPlanes, planes, new Mat(), histg, histSize, ranges, false);
            planes.setTo(new MatOfInt(2));
            Imgproc.calcHist(rgbPlanes, planes, new Mat(), histb, histSize, ranges, false);
            
            // Normalize.
            Mat noArray = new Mat();
            Core.normalize(histr, histr, 1, 0, Core.NORM_L2, -1, noArray);
            Core.normalize(histg, histg, 1, 0, Core.NORM_L2, -1, noArray);
            Core.normalize(histb, histb, 1, 0, Core.NORM_L2, -1, noArray);

            // Draw histogram
            int hist_w = 300;
            int hist_h = 300;
            int bin_w = hist_w/numBins|0 ;
            
            Mat histImage = Mat.ones(new Size(hist_h, hist_w), CvType.CV_8UC4);
            Point p1, p2;
            int row, col;
            int cols = histr.cols();
            
            for (int i = 1; i < numBins; i++) {
                row = i / cols;
                col = i % cols;

                p1 =  new Point(bin_w*(i-1), hist_h - histr.get(row, col - 1)[0]*hist_h);
                p2 = new Point(bin_w*(i), hist_h - histr.get(row,  col)[0]*hist_h);
                Imgproc.line(histImage, p1, p2, new Scalar(255, 0, 0), 1, Imgproc.LINE_8, 0);
                
                p1 =  new Point(bin_w*(i-1), hist_h - histg.get(row, col - 1)[0]*hist_h);
                p2 = new Point(bin_w*(i), hist_h - histg.get(row,  col)[0]*hist_h);
                Imgproc.line(histImage, p1, p2, new Scalar(0, 255, 0), 1, Imgproc.LINE_8, 0);
                
                p1 =  new Point(bin_w*(i-1), hist_h - histb.get(row, col - 1)[0]*hist_h);
                p2 = new Point(bin_w*(i), hist_h - histb.get(row,  col)[0]*hist_h);
                Imgproc.line(histImage, p1, p2, new Scalar(0, 0, 255), 1, Imgproc.LINE_8, 0);
            }
            return mat2BufferedImage(histImage);
        }
    }
    
    /** A filter that converts the image to BGRA format (Blue Green Red Alpha).
     */
    public class MakeBGRA extends AbstractBufferedImageOp {

        /** Convert the source image to BGRA format. 
         *  @param source The source image.
         *  @param destination Not used here.  Required by superclass.
         *  @return The image in BGRA format.
         */
        @Override
        public BufferedImage filter(BufferedImage src, BufferedImage dest) {
            Mat source = bufferedImage2Mat(src);
            Mat result = new Mat();
            // Note: This result differs from the browser.  The browser 
            // alters the colors (R -> B, B -> R) whereas the Java version 
            // does not.
            Imgproc.cvtColor(source, result, Imgproc.COLOR_RGBA2BGRA, 0);

            return mat2BufferedImage(result);
        }
    }
    
    /** A filter that converts the image to grayscale.
     */
    public class MakeGray extends AbstractBufferedImageOp {

        /** Convert the source image to grayscale. 
         *  @param source The source image.
         *  @param destination Not used here.  Required by superclass.
         *  @return The grayscale image.
         */
        @Override
        public BufferedImage filter(BufferedImage src, BufferedImage dest) {
            Mat source = bufferedImage2Mat(src);
            Mat result = new Mat();
            Imgproc.cvtColor(source, result, Imgproc.COLOR_RGB2GRAY);

            return mat2BufferedImage(result);
        }
    }
    
    /** A filter that converts the image to HSV (Hue, Saturation, and Value).
     */
    public class MakeHSV extends AbstractBufferedImageOp {

        /** Convert the source image to HSV. 
         *  @param source The source image.
         *  @param destination Not used here.  Required by superclass.
         *  @return The HSV image.
         */
        @Override
        public BufferedImage filter(BufferedImage src, BufferedImage dest) {
            Mat source = bufferedImage2Mat(src);
            Mat tmp = new Mat();
            Mat result = new Mat();
            Imgproc.cvtColor(source, tmp, Imgproc.COLOR_RGBA2RGB);
            Imgproc.cvtColor(tmp, result, Imgproc.COLOR_RGB2HSV);

            return mat2BufferedImage(result);
        }
    }
    
    /** A filter that converts the image to YUV.
     */
    public class MakeYUV extends AbstractBufferedImageOp {

        /** Convert the source image to HSV. 
         *  @param source The source image.
         *  @param destination Not used here.  Required by superclass.
         *  @return The HSV image.
         */
        @Override
        public BufferedImage filter(BufferedImage src, BufferedImage dest) {
            Mat source = bufferedImage2Mat(src);
            Mat result = new Mat();
            Imgproc.cvtColor(source, result, Imgproc.COLOR_RGB2YUV);

            return mat2BufferedImage(result);
        }
    }
    
    
    /** A filter that blurs the image.
     */
    public class MedianBlur extends AbstractBufferedImageOp {
        /** Blur the source image. 
         *  @param source The source image.
         *  @param destination Not used here.  Required by superclass.
         *  @return The blurred image.
         */
        @Override
        public BufferedImage filter(BufferedImage src, BufferedImage dest) {
            Mat source = bufferedImage2Mat(src);
            Mat converted = new Mat();
            Imgproc.cvtColor(source, converted, Imgproc.COLOR_RGB2BGRA);

            Mat result = new Mat();
            Imgproc.medianBlur(converted, result, 2*_blurSize+1);
            
            return mat2BufferedImage(result);
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    // Since filter() is not defined as static in AbstractBufferedImageOp, 
    // create instances for each type of filter.
    
    /** Blur filter */
    private Blur _blur;
    
    /** Blur threshold.  Used by Blur, GaussianBlur, and MedianBlur. 1 to 25. */
    private int _blurSize = 10;
    
    /** Canny edge detection threshold.  Used by FindContours and FindEdges. 
     * 10 to 150.
     */
    private int _cannyThreshold = 20;
    
    /** Dilate filter. */
    private Dilate _dilate;
    
    /** Erode filter. */
    private Erode _erode;
    
    /** Erosion size.  Used by Dilate and Erode.  0 t0 21. */
    private int _erosionSize = 10;
    
    /** Find contours filter. */
    private FindContours _findContours;
    
    /** Find edges filter. */
    private FindEdges _findEdges;
    
    /** Gaussian blur filter. */
    private GaussianBlur _gaussianBlur;
    
    /** Histogram creator.  */
    private Histogram _histogram;
    
    /** BGRA converter. */
    private MakeBGRA _makeBGRA;
    
    /** Grayscale converter. */
    private MakeGray _makeGray;
    
    /** HSV converter. */
    private MakeHSV _makeHSV;
    
    /** YUV converter. */
    private MakeYUV _makeYUV;
    
    /** Median blur filter. */
    private MedianBlur _medianBlur;
}
