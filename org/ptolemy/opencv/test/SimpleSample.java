/* A simple test for OpenCV.

@Copyright (c) 2016 The Regents of the University of California.
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
http://opencv.org/license.html

License Agreement
For Open Source Computer Vision Library
(3-clause BSD License)

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

    Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.

    Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in
    the documentation and/or other materials provided with the
    distribution.

    Neither the names of the copyright holders nor the names of the
    contributors may be used to endorse or promote products derived
    from this software without specific prior written permission.

This software is provided by the copyright holders and contributors
"as is" and any express or implied warranties, including, but not
limited to, the implied warranties of merchantability and fitness for
a particular purpose are disclaimed. In no event shall copyright
holders or contributors be liable for any direct, indirect,
incidental, special, exemplary, or consequential damages (including,
but not limited to, procurement of substitute goods or services; loss
of use, data, or profits; or business interruption) however caused and
on any theory of liability, whether in contract, strict liability, or
tort (including negligence or otherwise) arising in any way out of the
use of this software, even if advised of the possibility of such
damage.
*/

package org.ptolemy.opencv.test;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.ptolemy.opencv.OpenCVLoader;

///////////////////////////////////////////////////////////////////
//// SimpleSample

/** A simple OpenCV Sample.
 *  Based on
 *  http://docs.opencv.org/2.4/doc/tutorials/introduction/desktop_java/java_dev_intro.html.
 *
 *  @author Christopher Brooks
 *  @version $Id$
 *  @since Ptolemy II 11.0
 *  @Pt.ProposedRating Yellow (cxh)
 *  @Pt.AcceptedRating Red (cxh)
 */
class SimpleSample {

    /** A simple test for OpenCV.
     *  @param args Ignored
     */
    public static void main(String[] args) {
        System.out.println("Welcome to OpenCV " + Core.VERSION);
        Mat m = new Mat(5, 10, CvType.CV_8UC1, new Scalar(0));
        System.out.println("OpenCV Mat: " + m);
        Mat mr1 = m.row(1);
        mr1.setTo(new Scalar(1));
        Mat mc5 = m.col(5);
        mc5.setTo(new Scalar(5));
        System.out.println("OpenCV Mat data:\n" + m.dump());
    }

    static {
        // Use a better loader that looks for the shared library for the Mac.
        OpenCVLoader.loadOpenCV(Core.NATIVE_LIBRARY_NAME);
    }

}
