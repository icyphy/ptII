/* A face detector that uses OpenCV.

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
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
 

import java.net.URL;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.*;
import org.opencv.objdetect.CascadeClassifier;

import ptolemy.kernel.util.IllegalActionException;

import com.jhlabs.image.AbstractBufferedImageOp;

///////////////////////////////////////////////////////////////////
//// FaceRecognizer

/** A face recognition image filter.
 *  This filter detects faces in the given frame that lie in a square of 
 *  dimensions [x,x], where _minFaceSize <= x <= _maxFaceSize.
 *
 *  The parameters of the filter are:
 *  <ul>
 *  <li> <i>MinFaceSize</i>: Minimum face size to be considered ( one side of a bounding square)</li>
 *  <li> <i>MaxFaceSize</i>: Maximum face size to be considered ( one side of a bounding square)</li>
 *  </ul>
 *
 *  The class uses Haar Cascade classifier based object detection, as
 *  well as the classifiers for frontal face recognition are based on
 *  implementations provided by OpenCV 3.1.0.
 *
 *  @author Ilge Akkaya
 *  @version $Id$
 *  @since Ptolemy II 11.0
 *  @Pt.ProposedRating Yellow (cxh)
 *  @Pt.AcceptedRating Red (cxh)
 */
public class FaceRecognizer extends AbstractBufferedImageOp {

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
    public BufferedImage filter(BufferedImage source, BufferedImage destination) {

        /** Load Native C Library for OpenCV */
        //System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        // Use a better loader that looks for the shared library for the Mac.
        OpenCVLoader.loadOpenCV(Core.NATIVE_LIBRARY_NAME);

        // Get OpenCV image
        Mat inputImage = bufferedImage2Mat(source); 

        // Detect faces in image
        Rect[] faceRectangles = detectFaces(inputImage);
        // Draw rectangles around each detected face
        
        for (int i= 0; i < faceRectangles.length; i++) {
            Rect rect = faceRectangles[i];
            // draw bounding rectangle around face
            Imgproc.rectangle(inputImage, new Point(rect.x,rect.y), 
                    new Point(rect.x+rect.width,rect.y+rect.height),new Scalar(0,0,255));
        }
 
        setFaceCount(faceRectangles.length);

        return mat2BufferedImage(inputImage); 

    }

    /**
     * Convert a BufferedImage object to OpenCV's Mat representation
     * @param source BufferedImage input
     * @return Mat matrix
     */
    public Mat bufferedImage2Mat( BufferedImage source) {  
        int w = source.getWidth();
        int h = source.getHeight();  
        Mat outputImage = new Mat(h, w, CvType.CV_8UC3);
        byte[] pixels = ((DataBufferByte) source.getRaster().getDataBuffer()).getData(); 
        outputImage.put(0, 0, pixels);  
        return outputImage;
    }
    
    /**
     * Convert a Mat image into a BufferedImage
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
                if ( color.length  > 1 ){
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



    public Rect[] detectFaces(Mat img) {
        Mat gray = new Mat();
        Imgproc.cvtColor(img, gray, Imgproc.COLOR_RGB2GRAY);

        CascadeClassifier cas = new CascadeClassifier();
        URL url = getClass().getResource("haarcascade_frontalface_default.xml"); 
        
        cas.load(url.getPath());
        MatOfRect faces = new MatOfRect(); 
        cas.detectMultiScale(img, faces, 1.05, 5, 0, new Size(_minFaceSize,_minFaceSize), 
                new Size(_maxFaceSize,_maxFaceSize)); 
        Rect[] fbox = faces.toArray();
        return fbox;
    }
 
    /** Return center of gravity of motion detected by the most recent invocation
     *  of filter(), or null if no motion was detected.
     *  @return The center of gravity of motion (in pixels).
     */
    public int getFaceCount() {
        return _facesDetected;
    }

    public void setFaceCount(int count) {
        _facesDetected = count;
    }
   
    public void setMinFaceSize( int size ) throws IllegalActionException {
        if ( size > 0) {
            _minFaceSize = size;
        } else {
            throw new IllegalActionException("Minimum face size must be positive.");
        }
    }
    
    public void setMaxFaceSize( int size ) throws IllegalActionException {
        if ( size > 0) {
            _maxFaceSize = size;
        } else {
            throw new IllegalActionException("Maximum face size must be positive.");
        }
    }
    
    /** Return a string description of the filter.
     *  @return The string "MotionDetectorFilter".
     */
    @Override
    public String toString() {
        return "FaceRecognitionFilter";
    }
 

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Minimum face size to be considered in face recognition. */
    private int _minFaceSize = 30;

    /** Maximum face size to be considered in face recognition. */
    private int _maxFaceSize = 400;
    
    /** Indicator that motion has been detected by the filter operation. */
    private int _facesDetected = 0; 
}
