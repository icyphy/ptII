/*
A vector containing paths to search for when resolving an import or
package. Code was converted from SearchPath.cc from the Titanium project.

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
import java.util.Vector;
import ptolemy.lang.ApplicationUtility;

public class SearchPath extends Vector {

    public SearchPath(String envar, String fallbacks) {
         
        if (envar != null) {
           String envalue = System.getProperty(envar, ".");

           ApplicationUtility.trace("envalue = " + envalue);

           if (envalue != null) {
              _addPaths(envalue);
           } else {
              _addPaths(fallbacks);
           }
        } else {
           _addPaths(fallbacks);        
        }
    }
    
    /** Convert a Java qualified name into a partial pathname, without the file 
     *  extension. For example, "ptolemy.lang.java.SearchPath" is converted to 
     *  "ptolemy/lang/java/SearchPath" if the platform is UNIX.
     */
    public String javaNameToPath(String javaName) {
        return javaName.replace('.', File.separatorChar);    
    }

    public File openDirectory(String target) {
        for (int i = 0; i < size(); i++) {
            String candidate = (String) get(i);

            String fullname = candidate + target;
 
            File directory = new File(fullname);
            if (directory.isDirectory()) {
               // target = fullname
               return directory;
            }
        }
        return null;
    }

    /** Open the Java source file with the qualified class name. The name may
     *  either be qualified by the '.' character or by the value of
     *  File.pathSeparatorChar. Try to open a skeleton version of the source
     *  code before trying the ordinary version. Return an instance of File
     *  associated with the path of the source code. If the source code 
     *  cannot be found, return null. This method simply calls 
     *  openSource(target, true)
     */
    public File openSource(String target) {
        return openSource(target, true);
    } 
 

    /** Open the Java source file with the qualified class name. The name may
     *  either be qualified by the '.' character or by the value of
     *  File.pathSeparatorChar. If favorSkeletons is true, try to open
     *  a skeleton version of the source code before trying the ordinary version.
     *  Return an instance of File associated with the path of the source code. 
     *  If the source code cannot be found, return null.
     */
    public File openSource(String target, boolean favorSkeletons) {
        String targetPath = javaNameToPath(target);
        
        for (int i = 0; i < size(); i++) {
            String candidate = (String) get(i);

            File file = null;
            
            if (favorSkeletons) {
               // favor skeletons instead of full versions for performance
               file = tryOpen(candidate, targetPath, "jskel");
            }

            if (file == null) {
               file = tryOpen(candidate, targetPath, "java");
            }

            if (file != null) {
               return file;
            }
        }
        return null;
    }

    public static File tryOpen(String directory, String target, String suffix) {
        String fullname = directory + target + '.' + suffix;
        File file = new File(fullname);

        if (file.isFile()) {
           return file;
        } else {
           return null;
        }
    }

    protected void _addPaths(String paths) {
        int begin = 0;

        int end;
        do {
          end = paths.indexOf(File.pathSeparator, begin);
          if (end == -1) {
             _addPath(paths.substring(begin));
          } else {
             _addPath(paths.substring(begin, end));
             begin = end + 1;
          }
        } while (end > -1);
    }

    protected void _addPath(String path) {
      if (path.length() > 0) {
         ApplicationUtility.trace("adding path " + path);
         add(path + File.separatorChar);
      } else {
         throw new RuntimeException("_addPath() called with empty path string");
      }
    }

    public static final SearchPath NAMED_PATH =
     new SearchPath("java.class.path", ".");

    public static final SearchPath UNNAMED_PATH =
     new SearchPath(null, ".");
}
