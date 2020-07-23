/* Simple Java Test Program for OpenCV

Copyright (c) 2010-2018 The Regents of the University of California.
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

PT_COPYRIGHT_VERSION_2
COPYRIGHTENDKEY

*/
import org.opencv.core.Core;
import org.opencv.imgproc.Imgproc;
import java.io.File;

/** Simple class used by configure to test whether the OpenCV
 *  is present by compiling and loading the shared library.
 *
 *  <p>To compile and run:</p>
 *  <pre>
 *   javac -classpath /opt/local/share/OpenCV/java/opencv-320.jar OpenCVTest.java
 *   java -classpath /opt/local/share/OpenCV/java/opencv-320.jar:. OpenCVTest
 *  </pre>
 *  <p>See https://wiki.eecs.berkeley.edu/ptexternal/Main/Main/OpenCV</p>
 *
 *  @author Christopher Brooks
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Green (cxh)
 *  @Pt.AcceptedRating Red (cxh)
 */
public class OpenCVTest {
    public static void main(String[] args) {
        try {
            System.out.println("About to load " + Core.NATIVE_LIBRARY_NAME);
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

            System.out.println("Imgproc.LINE_8: " + Imgproc.LINE_8);  // Imgproc.LINE_8 is missing from OpenCV 2.4.8, so we test for it here.

            System.out.println("Loaded " + Core.NATIVE_LIBRARY_NAME);
        } catch (Throwable throwable) {
            String osName = System.getProperty("os.name");
            if (osName.startsWith("Mac OS X")) {
                String portPath = "/opt/local/share/OpenCV/java/lib";
                String PTII = System.getenv("PTII");
                String vendorsPath = PTII + "/vendors/opencv/share/OpenCV/java/lib";
                String [] paths = {
                    // FIXME: OpenCV-3.1.0 creates a .so file under Mac OS X.
                    portPath +  Core.NATIVE_LIBRARY_NAME + ".so",
                    portPath +  Core.NATIVE_LIBRARY_NAME + ".dylib",
                    vendorsPath +  Core.NATIVE_LIBRARY_NAME + ".so",
                    vendorsPath +  Core.NATIVE_LIBRARY_NAME + ".dylib"
                };
                for (int i = 0; i < paths.length; i++) {
                    if (new File(paths[i]).exists()) {
                        System.load(paths[i]);
                        System.out.println("Loaded " + paths[i]);
                        return;
                    }
                }
            }
            throw new RuntimeException("Could not load " + Core.NATIVE_LIBRARY_NAME
                                       + ".\n    java.library.path = " + System.getProperty("java.library.path"), throwable);
        }
    }
}
