/* Utilities used to manipulate classes

Copyright (c) 2003-2005 The Regents of the University of California.
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
package ptolemy.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;


//////////////////////////////////////////////////////////////////////////
//// ClassUtilities

/**
   A collection of utilities for manipulating classes
   These utilities do not depend on any other ptolemy.* packages.


   @author Christopher Hylands
   @version $Id$
   @since Ptolemy II 4.0
   @Pt.ProposedRating Red (eal)
   @Pt.AcceptedRating Red (cxh)
*/
public class ClassUtilities {
    /** Instances of this class cannot be created.
     */
    private ClassUtilities() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Lookup a jar URL and return the resource.
     *  Given a jar url of the format jar:<i>url</i>!/{entry}, return
     *  the resource, if any of the {entry}.
     *  If the string does not contain <code>!/</code>, then return null.
     *  Web Start uses jar URL, and there are some cases where
     *  if we have a jar URL, then we may need to strip off the
     *  jar:<i>url</i>!/ part so that we can search for the {entry}
     *  as a resource.
     *
     *  @param spec The string containing the jar url.
     *  @return The resource, if any.
     *  @exception IOException If it cannot convert the specification to
     *   a URL.
     *  @see java.net.JarURLConnection
     */
    public static URL jarURLEntryResource(String spec)
            throws IOException {
        // At first glance, it would appear that this method could appear
        // in specToURL(), but the problem is that specToURL() creates
        // a new URL with the spec, so it only does further checks if
        // the URL is malformed.  Unfortunately, in Web Start applications
        // the URL will often refer to a resource in another jar file,
        // which means that the jar url is not malformed, but there is
        // no resource by that name.  Probably specToURL() should return
        // the resource after calling new URL().
        int jarEntry = spec.indexOf("!/");

        if (jarEntry == -1) {
            return null;
        } else {
            try {
                // !/ means that this could be in a jar file.
                String entry = spec.substring(jarEntry + 2);

                // We might be in the Swing Event thread, so
                // Thread.currentThread().getContextClassLoader()
                // .getResource(entry) probably will not work.
                Class refClass = Class.forName("ptolemy.kernel.util.NamedObj");
                URL entryURL = refClass.getClassLoader().getResource(entry);
                return entryURL;
            } catch (Exception ex) {
                throw new IOException("File not found: " + spec + ": " + ex);
            }
        }
    }

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
        String necessaryResource = StringUtilities.substitute(necessaryClass,
                ".", "/") + ".class";

        URL necessaryURL = Thread.currentThread().getContextClassLoader()
            .getResource(necessaryResource);

        if (necessaryURL != null) {
            String resourceResults = necessaryURL.getFile();

            // Strip off the file:/ and the necessaryResource.
            if (resourceResults.startsWith("file:/")) {
                resourceResults = resourceResults.substring(6);
            }

            // Strip off the name of the resource we were looking for
            // so that we are left with the directory or jar file
            // it is in
            resourceResults = resourceResults.substring(0,
                    resourceResults.length() - necessaryResource.length());

            // Strip off the file:/
            if (resourceResults.startsWith("file:/")) {
                resourceResults = resourceResults.substring(6);
            }

            // Strip off the trailing !/
            if (resourceResults.endsWith("!/")) {
                resourceResults = resourceResults.substring(0,
                        resourceResults.length() - 2);
            }

            // Unfortunately, under Windows, URL.getFile() may
            // return things like /c:/ptII, so we create a new
            // File and get its path, which will return c:\ptII
            File resourceFile = new File(resourceResults);

            // Convert backslashes
            String sanitizedResourceName = StringUtilities.substitute(resourceFile
                    .getPath(), "\\", "/");
            return sanitizedResourceName;
        }

        return null;
    }
}
