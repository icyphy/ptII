package ptolemy.actor.lib.opencv.jna.cxcore;

import ptolemy.actor.lib.opencv.jna.cv.CvSize;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.ptr.PointerByReference;

public class CxcoreLib implements Library {
    //public static final String LibraryName = "C:/OpenCV2.0/bin/libcxcore200.dll";
    public static final String LibraryName = "cxcore100";
    public static final NativeLibrary LibraryInstance = NativeLibrary
            .getInstance(LibraryName);
    static {
        Native.register(LibraryName);
    }

    //public static native void cvFlip (CvArr src, CvArr dst, int flip_mode) ;
    public static native void cvFlip(Pointer src, Pointer dst, int flip_mode);

    //public static native IplImage cvCreateImage( CvSize size, int depth, int channels );
    public static native Pointer cvCreateImage(CvSize size, int depth,
            int channels);

    public static native void cvReleaseImage(PointerByReference image);

    public static native IplImage cvCloneImage(Pointer image);

    public static native void cvCopy(Pointer src, Pointer dst, Pointer mask);

    public static class IplTileInfo extends PointerType {
    }
}
