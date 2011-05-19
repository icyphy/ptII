package ptolemy.actor.lib.opencv.javacv.demo;


import static java.lang.Math.round;
import static name.audet.samuel.javacv.jna.cv.CV_BGR2GRAY;
import static name.audet.samuel.javacv.jna.cv.cvCvtColor;
import static name.audet.samuel.javacv.jna.cv.cvEqualizeHist;
import static name.audet.samuel.javacv.jna.cv.cvHaarDetectObjects;
import static name.audet.samuel.javacv.jna.cxcore.IPL_DEPTH_8U;
import static name.audet.samuel.javacv.jna.cxcore.cvCircle;
import static name.audet.samuel.javacv.jna.cxcore.cvClearMemStorage;
import static name.audet.samuel.javacv.jna.cxcore.cvCreateImage;
import static name.audet.samuel.javacv.jna.cxcore.cvCreateMemStorage;
import static name.audet.samuel.javacv.jna.cxcore.cvGetSeqElem;
import static name.audet.samuel.javacv.jna.cxcore.cvGetSize;
import static name.audet.samuel.javacv.jna.cxcore.cvLoad;
import static name.audet.samuel.javacv.jna.cxcore.cvReleaseImage;
import static name.audet.samuel.javacv.jna.cxcore.cvReleaseMemStorage;
import static name.audet.samuel.javacv.jna.highgui.cvLoadImage;
import static name.audet.samuel.javacv.jna.highgui.v10or11.cvSaveImage;
import name.audet.samuel.javacv.jna.cv;
import name.audet.samuel.javacv.jna.cv.CvHaarClassifierCascade;
import name.audet.samuel.javacv.jna.cxcore.CvMemStorage;
import name.audet.samuel.javacv.jna.cxcore.CvPoint;
import name.audet.samuel.javacv.jna.cxcore.CvRect;
import name.audet.samuel.javacv.jna.cxcore.CvScalar;
import name.audet.samuel.javacv.jna.cxcore.CvSeq;
import name.audet.samuel.javacv.jna.cxcore.CvSize;
import name.audet.samuel.javacv.jna.cxcore.IplImage;

import com.sun.jna.Pointer;



public class FaceDetectTest {
    public static void main(String[] args) throws Exception {
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
        
//        // capture sample
//        Pointer capture = cvCreateCameraCapture(0);
//        //Pointer p = highgui.cvLoadImage("c:/temp/test_cap1.png",1);
//        
//        //cvSetCaptureProperty (capture, CV_CAP_PROP_FRAME_WIDTH, 320);
//        //cvSetCaptureProperty (capture, CV_CAP_PROP_FRAME_HEIGHT, 240);
//
//        cvNamedWindow ("Capture", 1);
//        
//        while (true) {
//            //IplImage frame = cvQueryFrame(capture);
//            //Pointer p = frame.getPointer();
//            Pointer p = cvQueryFrame(capture);
//            //cvSmooth(frame, frame, CV_GAUSSIAN, 5, 5, 0, 0);
//            cvSmooth(p,p,CV_GAUSSIAN,5,5,0,0);
//            cvShowImage ("Capture", p);
//            int c = cvWaitKey (2);
//            if (c == 0x1b) break;
//         }
//         cvReleaseCapture (new PointerByReference(capture));
//         cvDestroyWindow ("Capture");      
        
        // face detection
        IplImage src_img  = cvLoadImage("c:/temp/face_test.jpg",1);
        //IplImage src_img  = cvLoadImage("c:/temp/4.2.04.png",1);
        //IplImage src_gray  = cvLoadImage("c:/temp/face_test_gray.png",1);
        //IplImage src_img_inst = new IplImage(src_img);
        //IplImage src_img = cvLoadImage("c:/temp/face_test.jpg",1);
        
        //CvSize.ByValue size = cvGetSize(src_gray);
        //cvGetSize(src_img);
        //CvSize size = new CvSize(420,360);
        IplImage src_gray = cvCreateImage(cvGetSize(src_img), IPL_DEPTH_8U, 1);
        cvCvtColor (src_img, src_gray, CV_BGR2GRAY);
        
        cvEqualizeHist (src_gray, src_gray);
   
        String libname = cv.libname;
        String version = "100";
        System.out.println(libname);
        if(libname.indexOf(version) >= 0){
            System.out.println("ver.1");
        }
        
        // dummy call
        //IplImage dummy_img = cvCreateImage(size, IPL_DEPTH_8U, 1);
        //cvEqualizeHist (dummy_img, dummy_img);
        

        String cascade_name = "C:/temp/haarcascade_frontalface_default.xml";
        CvHaarClassifierCascade cascade = new CvHaarClassifierCascade(cvLoad (cascade_name, null, null, null));

        CvMemStorage storage = cvCreateMemStorage (0);
        cvClearMemStorage (storage);
        
        //cvSaveImage("c:/temp/face_test_gray.png",src_gray);
        
        //size.width = 40; size.height = 40;
        CvSize size = new CvSize(40,40);
        //Pointer faces_p = cvHaarDetectObjects (src_gray, cascade, storage, 1.11, 4, 0, size);
        //CvSeq faces = new CvSeq(faces_p);
        CvSeq faces = cvHaarDetectObjects (src_gray, cascade, storage, 1.11, 4, 0, size.byValue());

        int i = 0;
        int faceTotal = 0;
        if(faces != null) faceTotal = faces.total;

        CvScalar.ByValue[] colors = { 
                CvScalar.RED, CvScalar.BLUE, CvScalar.GREEN, CvScalar.CYAN,
                CvScalar.YELLOW, CvScalar.MAGENTA, CvScalar.WHITE, CvScalar.GRAY
                };
        
        for (i = 0; i < faceTotal; i++) {
            Pointer r = cvGetSeqElem (faces, i);
            CvRect rect = new CvRect(r);
            CvPoint center = new CvPoint(0,0);
            int radius;
            center.x = (int)round (rect.x + rect.width * 0.5);
            center.y = (int)round (rect.y + rect.height * 0.5);
            radius = (int)round ((rect.width + rect.height) * 0.25);
            cvCircle (src_img, center.byValue(), radius, colors[i % 8] , 3, 8, 0);
          }
        
//        cvNamedWindow ("Face Detection", WINDOW_AUTOSIZE);
//        cvShowImage ("Face Detection", src_img);
//        cvWaitKey (0);
//
//        cvDestroyWindow ("Face Detection");
        
        cvSaveImage("c:/temp/face_detect_result.png",src_img);
        
        cvReleaseImage (src_img.pointerByReference());
        cvReleaseImage (src_gray.pointerByReference());
        cvReleaseMemStorage (storage.pointerByReference());
        
        System.out.println("done");

    }
}