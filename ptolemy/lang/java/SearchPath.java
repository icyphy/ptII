/* A vector containing paths to search for when resolving an import or
package.

Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Red (ctsay@eecs.berkeley.edu)
@AcceptedRating Red (ctsay@eecs.berkeley.edu)
*/

package ptolemy.lang.java;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import ptolemy.lang.ApplicationUtility;

/** A vector containing paths to search for when resolving an import or
package.

<p>
Portions of this code were derived from sources developed under the
auspices of the Titanium project, under funding from the DARPA, DoE,
and Army Research Office.

@author Jeff Tsay, Christopher Hylands
@version $Id$
 */
public class SearchPath extends Vector {

    /** Construct a SearchPath object by reading the propertyName
     *  property, if propertyName is null or does not name a property
     *  then use fallbackPaths.  The value of the property named by 
     *  propertyName and the value of fallbackPaths should contain
     *  a string with pathnames separated by File.separatorChar.
     *  @param propertyName Name of the property to look for.
     *  @param fallbackPaths Path list to use if propertyName can't be found.
     */ 
    public SearchPath(String propertyName, String fallbackPaths) {
        if (propertyName != null) {
            String propertyValue = System.getProperty(propertyName, ".");

            ApplicationUtility.trace("propertyValue = " + propertyValue);

            if (propertyValue != null) {
                _addPaths(propertyValue);
            } else {
                _addPaths(fallbackPaths);
            }
        } else {
            _addPaths(fallbackPaths);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Open the Java source file with the qualified class name. The name may
     *  either be qualified by the '.' character or by the value of
     *  File.pathSeparatorChar. Try to open a skeleton version of the source
     *  code before trying the ordinary version. Return an instance of File
     *  associated with the path of the source code. If the source code
     *  cannot be found, return null. This method simply calls
     *  openSource(target, true).
     */
    public File openSource(String target) {
        return openSource(target, true);
    }


    /** Open the Java source file with the qualified class name.
     *  @param The qualified class name, which may either be qualified
     *  by the '.' character or by the value of File.pathSeparatorChar.
     *  @param favorSkeletons True if .jskel files are opened instead of 
     *  .java files.
     *  @return an instance of File associated with the path of the
     *  source code. If the source code cannot be found, return null.
     */
    public File openSource(String target, boolean favorSkeletons) {

	// Convert a Java qualified name into a partial pathname, without the
	// file extension. For example, "ptolemy.lang.java.SearchPath" is
	// converted to "ptolemy/lang/java/SearchPath" under Unix
        String targetPath = target.replace('.', File.separatorChar);

        for (int i = 0; i < size(); i++) {
            String candidate = (String) get(i);

            File file = null;

            if (favorSkeletons) {
                // favor skeletons instead of full versions for performance
                file = _tryOpen(candidate, targetPath, "jskel");
                if (file != null) {
                    File javaFile = _tryOpen(candidate, targetPath, "java");
                    if (javaFile.lastModified() > file.lastModified()) {
                        throw new RuntimeException("SearchPath.openSource():" +
                                candidate + target + 
                                ".java was modified more recently than" +
                                candidate + target + ".jskel. Rerun '" +
                                "cd $PTII/ptolemy/lang/java; make skeleton'");
                    }
                }
            }

            if (file == null) {
                file = _tryOpen(candidate, targetPath, "java");
            }

            if (file != null) {
                return file;
            }
        }
        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    public static final SearchPath NAMED_PATH =
	new SearchPath("java.class.path", ".");

    public static final SearchPath UNNAMED_PATH =
	new SearchPath(null, ".");


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Split a String consisting of 0 or more pathnames separated by
    // File.pathSeparated pathnames, and add them to the Vector of paths.
    private void _addPaths(String paths) {
        int begin = 0;

        int end;
        do {
            end = paths.indexOf(File.pathSeparator, begin);
            String path = null;
            if (end == -1) {
                path = paths.substring(begin);
                if (path.length() > 0) {
		    add(path + File.separatorChar);
                }
            } else {
                path = paths.substring(begin, end);
                if (path.length() > 0) {
		    add(path + File.separatorChar);
                }
                begin = end + 1;
            }
        } while (end > -1);
    }

    /* Try to open a file in directory, with a filename target
     * and a suffix.  
     * @param directory The base directory 
     * @param target The pathname to the file
     * @param suffix The suffix of the path.
     */
    private static File _tryOpen(String directory, String target,
			       String suffix) {
        String fullname = directory + target + '.' + suffix;
        File file = new File(fullname);

        if (file.isFile()) {
            try {
                return file.getCanonicalFile();
            } catch (IOException ioe) {
                ApplicationUtility.error("cannot get canonical filename");
            }
        }
        return null;
    }
}
