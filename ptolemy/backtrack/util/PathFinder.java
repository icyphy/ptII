/* A tool to create class paths for customized class loading.

Copyright (c) 2005 The Regents of the University of California.
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

package ptolemy.backtrack.util;

import java.io.File;
import java.io.FileFilter;

//////////////////////////////////////////////////////////////////////////
//// PathFinder
/**
   A tool to set up customized class paths from which class are loaded
   with {@link LocalClassLoader}.
 
   @author Thomas Feng
   @version $Id$
   @since Ptolemy II 5.1
   @Pt.ProposedRating Red (tfeng)
   @Pt.AcceptedRating Red (tfeng)
*/
public class PathFinder {

    /** Return the class paths containing the root of the Ptolemy tree, 
     *  and the Jar files in sub-directories <tt>lib/</tt>, 
     *  <tt>vendors/sun/commapi/</tt> and <tt>vendors/sun/jxta</tt>.
     *
     *  @return The class paths.
     */
    public static String[] getPtClassPaths() {
        String PTII = "../../..";
        String[] subdirs = new String[] {
                "lib",
                "vendors/sun/commapi",
                "vendors/sun/jxta"
        };
        File[][]files = new File[subdirs.length][];
        int totalNumber = 0;
        for (int i = 0; i < subdirs.length; i++) {
            files[i] = 
                new File(PTII + "/" + subdirs[i]).listFiles(
                        new JarFileFilter()
                );
            totalNumber += files[i].length;
        }

        String[] classPaths = new String[totalNumber + 1];
        classPaths[0] = PTII;
        int currentNumber = 1;
        for (int i = 0; i < files.length; i++)
            for (int j = 0; j < files[i].length; j++)
                classPaths[currentNumber++] = files[i][j].getPath();

        return classPaths;
    }

    //////////////////////////////////////////////////////////////////////////
    //// JarFileFilter
    /**
     *  Filter out all the files in a directory, except for those with
     *  "<tt>.jar</tt>" postfix.
     *
     *  @author Thomas Feng
     *  @version $Id$
     *  @since Ptolemy II 4.1
     *  @Pt.ProposedRating Red (tfeng)
     */
    static class JarFileFilter implements FileFilter {

        /** Accept only files with names ending with "<tt>.jar</tt>".
         *
         *  @param pathname The full name of a file.
         *  @return <tt>true</tt> if the file has "<tt>.jar</tt>"
         *   postfix.
         */
        public boolean accept(File pathname) {
            return pathname.getName().endsWith(".jar");
        }
    }
}
