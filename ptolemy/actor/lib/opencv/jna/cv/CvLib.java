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
package ptolemy.actor.lib.opencv.jna.cv;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;

public class CvLib implements Library {
    public static final String LibraryName = "cv100";
    public static final NativeLibrary LibraryInstance = NativeLibrary
            .getInstance(LibraryName);
    static {
        Native.register(LibraryName);
    }

    public static native void cvSmooth(Pointer src, Pointer dst,
            int soomthtype, int size1, int size2, double sigma1, double sigma2);

    public static final int CV_BLUR_NO_SCALE = 0;
    public static final int CV_BLUR = 1;
    public static final int CV_GAUSSIAN = 2;
    public static final int CV_MEDIAN = 3;
    public static final int CV_BILATERAL = 4;

    public static final int IPL_DEPTH_1U = 1;
    public static final int IPL_DEPTH_8U = 8;
    public static final int IPL_DEPTH_16U = 16;
    public static final int IPL_DEPTH_32F = 32;
}
