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
