package ptolemy.actor.lib.opencv.jna.highgui;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public class HighguiLib implements Library {
    //public static final String LibraryName = "C:/OpenCV2.0/bin/libhighgui200.dll";
    public static final String LibraryName = "highgui100";
    public static final NativeLibrary LibraryInstance = NativeLibrary
            .getInstance(LibraryName);

    static {
        Native.register(LibraryName);
    }

    //public static native IplImage cvLoadImage( String filename, int iscolor);
    public static native Pointer cvLoadImage(String filename, int iscolor);

    public static native int cvSaveImage(String filename, Pointer image);

    public static native int cvNamedWindow(String name, int flags);

    public static native void cvDestroyWindow(String name);

    public static native int cvWaitKey(int delay);

    //public static native CvCapture cvCreateCameraCapture(int index);
    public static native Pointer cvCreateCameraCapture(int index);

    //public static native IplImage cvQueryFrame(CvCapture capture);
    //public static native IplImage cvQueryFrame(Pointer capture);
    public static native Pointer cvQueryFrame(Pointer capture);

    //public static native void cvShowImage(String name, CvArr image);
    public static native void cvShowImage(String name, Pointer image);

    public static native void cvReleaseCapture(PointerByReference capture);
}
