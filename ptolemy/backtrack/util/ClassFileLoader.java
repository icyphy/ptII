/*

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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import ptolemy.backtrack.ast.UnknownASTException;

//////////////////////////////////////////////////////////////////////////
//// ClassFileLoader
/**


@author Thomas Feng
@version $Id$
@since Ptolemy II 5.1
@Pt.ProposedRating Red (tfeng)
@Pt.AcceptedRating Red (tfeng)
*/
public class ClassFileLoader extends URLClassLoader {

    public ClassFileLoader() {
        this(null);
    }
    
    public ClassFileLoader(String[] classPaths) {
        super(_stringsToUrls(classPaths), null, null);
    }
    
    public Class loadClass(File classFile) throws FileNotFoundException,
            IOException, LinkageError, ClassNotFoundException {
        FileInputStream inputStream = new FileInputStream(classFile);
        byte[] buffer = new byte[inputStream.available()];
        inputStream.read(buffer);

        try {
            return defineClass(null, buffer, 0, buffer.length);
        } catch (IllegalAccessError e) {
            String errorPrefix = "class ";
            String message = e.getMessage();
            if (message.startsWith(errorPrefix)) {
                String className = message.substring(errorPrefix.length());
                int pos;
                if ((pos = className.indexOf(' ')) >= 0)
                    className = className.substring(0, pos);
                return loadClass(className);
            } else
                throw e;
        } catch (LinkageError e) {
            // Class already loaded.
            // FIXME: Any better solution here?
            String errorPrefix = "duplicate class definition: ";
            String message = e.getMessage();
            if (message.startsWith(errorPrefix)) {
                String path = message.substring(errorPrefix.length());
                String classFullName = path.replace('/', '.');
                return loadClass(classFullName);
            } else
                throw e;
        }
    }

    /** Convert an array of strings to an array of {@link URL}s with
     *  {@link File#toURL()}.
     *
     *  @param strings The array of strings.
     *  @return The array of urls.
     */
    private static URL[] _stringsToUrls(String[] strings) {
        if (strings == null)
            return new URL[0];

        URL[] urls = new URL[strings.length];
        for (int i = 0; i < strings.length; i++)
            try {
                urls[i] = new File(strings[i]).toURL();
            } catch (MalformedURLException e) {
                throw new UnknownASTException();
            }
        return urls;
    }
}
