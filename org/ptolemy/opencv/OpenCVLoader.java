/* Load the OpenCV shared library.

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
*/

package org.ptolemy.opencv;

import java.io.File;

///////////////////////////////////////////////////////////////////
//// OpenCVLoader

/** Load the OpenCV shared library.
 * @author Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 11.0
 * @Pt.ProposedRating Yellow (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class OpenCVLoader {
    /** Load the OpenCV Shared library.
     *  @param nativeLibraryName The name of the library, without a
     *  leading "lib" or a trailing . suffix. Typically
     *  Core.NATIVE_LIBRARY_NAME.
     *  @return the library that was loaded if any.
     */
    public static String loadOpenCV(String nativeLibraryName) {
        try {
            System.loadLibrary(nativeLibraryName);
            return nativeLibraryName;
        } catch (Throwable throwable) {
            String osName = System.getProperty("os.name");
            if (osName.startsWith("Mac OS X")) {
                String portPath = "/opt/local/share/OpenCV/java/lib";
                String [] paths = {
                    // FIXME: OpenCV-3.1.0 creates a .so file under Mac OS X.
                    portPath +  nativeLibraryName + ".so",
                    portPath +  nativeLibraryName + ".dylib"
                };
                for (int i = 0; i < paths.length; i++) {
                    if (new File(paths[i]).exists()) {
                        System.load(paths[i]);
                        System.out.println("Loaded " + paths[i]);
                        return paths[i];
                    }
                }
            }
            throw new UnsatisfiedLinkError("Could not load " + nativeLibraryName);
        }
    }
}
