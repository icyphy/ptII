package ptolemy.actor.lib.opencv.jna.cv;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;

public class CvLib implements Library {
    public static final String LibraryName = "cv100";
    public static final NativeLibrary LibraryInstance = NativeLibrary.getInstance(LibraryName);
    static {
        Native.register(LibraryName);
    }

    public static native void cvSmooth (Pointer src, Pointer dst, int soomthtype, int size1, int size2, double sigma1, double sigma2) ;

    public static final int CV_BLUR_NO_SCALE = 0;
    public static final int CV_BLUR          = 1;
    public static final int CV_GAUSSIAN      = 2;
    public static final int CV_MEDIAN        = 3;
    public static final int CV_BILATERAL     = 4;

    public static final int IPL_DEPTH_1U  =   1;
    public static final int IPL_DEPTH_8U  =   8;
    public static final int IPL_DEPTH_16U =  16;
    public static final int IPL_DEPTH_32F =  32;
}
