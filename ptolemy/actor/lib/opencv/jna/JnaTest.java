package ptolemy.actor.lib.opencv.jna;

import static ptolemy.actor.lib.opencv.jna.cv.CvLib.*;
import static ptolemy.actor.lib.opencv.jna.highgui.HighguiLib.*;
import static ptolemy.actor.lib.opencv.jna.cxcore.CxcoreLib.*;
import ptolemy.actor.lib.opencv.jna.cxcore.IplImage;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public class JnaTest {
    public static void main(String[] args) throws Exception {
//        HighguiLib highgui = HighguiLib.INSTANCE;
//        CvLib cv = CvLib.INSTANCE;
//        CxcoreLib cxcore = CxcoreLib.INSTANCE;
        
//        // OK
//        cvNamedWindow("Example1", 1);
//        while(true){
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

        cvNamedWindow ("Capture", 1);
        
        while (true) {
            //IplImage frame = cvQueryFrame(capture);
            //Pointer p = frame.getPointer();
            Pointer p = cvQueryFrame(capture);
            //cvSmooth(frame, frame, CV_GAUSSIAN, 5, 5, 0, 0);
            cvSmooth(p,p,CV_GAUSSIAN,5,5,0,0);
            cvShowImage ("Capture", p);
            int c = cvWaitKey (2);
            if (c == 0x1b) break;
         }
         cvReleaseCapture (new PointerByReference(capture));
         cvDestroyWindow ("Capture");

        
//        CvSize size = new CvSize(320,240);
//        img = cxcore.cvCreateImage(size,CvLib.IPL_DEPTH_8U, 1);
//        
        //highgui.cvLoadImage("c:/temp/test.png",1);
        //IplImage img = cvCreateImage(cvSize(320,240),IPL_DEPTH_32S,4);
        //IplImage img;
        
        //cv.cvSmooth(new CvArr(img.getPointer()), new CvArr(img.getPointer()), cv.CV_GAUSSIAN, 5, 5, 0, 0);
        //highgui.cvSaveImage("c:/temp/test_out.png",new CvArr(img.getPointer()));
        //cxcore.cvReleaseImage(new PointerByReference(img.getPointer()));
        System.out.println("done");

    }
}