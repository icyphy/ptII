/*
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 2010-2014 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

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

Ptolemy II includes the work of others, to see those copyrights, follow
the copyright link on the splash page or see copyright.htm.
 */
package ptolemy.actor.lib.opencv.jna;

import static ptolemy.actor.lib.opencv.jna.cv.CvLib.CV_GAUSSIAN;
import static ptolemy.actor.lib.opencv.jna.cv.CvLib.cvSmooth;
import static ptolemy.actor.lib.opencv.jna.highgui.HighguiLib.cvCreateCameraCapture;
import static ptolemy.actor.lib.opencv.jna.highgui.HighguiLib.cvDestroyWindow;
import static ptolemy.actor.lib.opencv.jna.highgui.HighguiLib.cvNamedWindow;
import static ptolemy.actor.lib.opencv.jna.highgui.HighguiLib.cvQueryFrame;
import static ptolemy.actor.lib.opencv.jna.highgui.HighguiLib.cvReleaseCapture;
import static ptolemy.actor.lib.opencv.jna.highgui.HighguiLib.cvShowImage;
import static ptolemy.actor.lib.opencv.jna.highgui.HighguiLib.cvWaitKey;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public class JnaTest {
    public static void main(String[] args) throws Exception {
        //        // OK
        //        cvNamedWindow("Example1", 1);
        //        while (true) {
        //           int c = cvWaitKey (20);
        //           if (c == 0x1b) break;
        //        }

        // OK
        //        Pointer p = cvLoadImage("c:/temp/test_cap1.png",1);
        //        cvSmooth(p,p,CV_GAUSSIAN,5,5,0,0);
        //        cvSaveImage("c:/temp/test_out2.png", p);
        //        cvReleaseImage(new PointerByReference(p));

        // capture sample
        Pointer capture = cvCreateCameraCapture(0);
        //Pointer p = highgui.cvLoadImage("c:/temp/test_cap1.png",1);

        //cvSetCaptureProperty (capture, CV_CAP_PROP_FRAME_WIDTH, 320);
        //cvSetCaptureProperty (capture, CV_CAP_PROP_FRAME_HEIGHT, 240);

        cvNamedWindow("Capture", 1);

        while (true) {
            //IplImage frame = cvQueryFrame(capture);
            //Pointer p = frame.getPointer();
            Pointer p = cvQueryFrame(capture);
            //cvSmooth(frame, frame, CV_GAUSSIAN, 5, 5, 0, 0);
            cvSmooth(p, p, CV_GAUSSIAN, 5, 5, 0, 0);
            cvShowImage("Capture", p);
            int c = cvWaitKey(2);
            if (c == 0x1b) {
                break;
            }
        }
        cvReleaseCapture(new PointerByReference(capture));
        cvDestroyWindow("Capture");
        System.out.println("done");

    }
}
