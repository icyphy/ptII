/* Utilities used to manipulate classes

 Copyright (c) 2003 The Regents of the University of California.
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
@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.util;

// Note that classes in ptolemy.util do not depend on any
// other ptolemy packages.

import java.io.File;
import java.net.URL;

//////////////////////////////////////////////////////////////////////////
//// ClassUtilities
/**
A collection of utilities for manipulating classes
These utilities do not depend on any other ptolemy.* packages.


@author Christopher Hylands
@version $Id$
@since Ptolemy II 3.1
*/
public class ClassUtilities {

    /** Instances of this class cannot be created.
     */
    private ClassUtilities() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Given a dot separated classname, return the jar file or directory
     *  where the class can be found.
     *  @param necessaryClass  The dot separated class name, for example
     *  "ptolemy.kernel.util.NamedObj"
     *  @return If the class can be found as a resource, return the
     *  directory or jar file where the necessary class can be found.
     *  otherwise, return null.
     */
    public static String lookupClassAsResource(String necessaryClass) {
        // This method is called from copernicus.kernel.GeneratorAttribute
        // and actor.lib.python.PythonScript.  We moved it here
        // to avoid dependencies
        String necessaryResource =
            StringUtilities.substitute(necessaryClass, ".", "/")
            + ".class";

        URL necessaryURL = Thread.currentThread()
            .getContextClassLoader().getResource(necessaryResource);

        if (necessaryURL != null) {
            String resourceResults = necessaryURL.getFile();

            // Strip off the file:/ and the necessaryResource.
            if (resourceResults.startsWith("file:/")) {
                resourceResults = resourceResults.substring(6);
            }

            // Strip off the name of the resource we were looking for
            // so that we are left with the directory or jar file
            // it is in
            resourceResults =
                resourceResults.substring(0,resourceResults.length()-
                        necessaryResource.length());
            // Strip off the file:/
            if (resourceResults.startsWith("file:/")) {
                resourceResults = resourceResults.substring(6);
            }

            // Strip off the trailing !/
            if (resourceResults.endsWith("!/")) {
                resourceResults =
                    resourceResults.substring(0,
                            resourceResults.length()-2);
            }

            // Unfortunately, under Windows, URL.getFile() may
            // return things like /c:/ptII, so we create a new
            // File and get its path, which will return c:\ptII
            File resourceFile = new File(resourceResults);

            // Convert backslashes
            String sanitizedResourceName =
                StringUtilities.substitute(resourceFile.getPath(),
                        "\\", "/");
            return sanitizedResourceName;
        }
        return null;
    }
}
