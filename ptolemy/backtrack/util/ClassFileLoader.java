/* Class loader that tries to load a class from the given file.

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

import ptolemy.backtrack.ast.UnknownASTException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

//////////////////////////////////////////////////////////////////////////
//// ClassFileLoader

/**
 Class loader that tries to load a class from the given file. This class
 loader, unlike other class loaders that accept class names and load classes
 with those names, accepts class file names and loads them as classes. It
 uses {@link URLClassLoader#defineClass(java.lang.String, sun.misc.Resource)}
 to define the class with the contents in the given file. If error occurs, it
 falls back to traditional class loading with the class name in the exception
 message.

 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class ClassFileLoader extends URLClassLoader {
    /** Construct a class loader with no special class path. This class
     *  loader can only load Java built-in classes indirectly, when they are
     *  required by the class file to be loaded.
     */
    public ClassFileLoader() {
        this(null);
    }

    /** Construct a class loader with a set of class paths specified as
     *  a string array. When the class file to be loaded requires other classes,
     *  this class loader loads those required classes with the given class
     *  paths.
     *
     *  @param classPaths The array of class paths to be searched in order.
     */
    public ClassFileLoader(String[] classPaths) {
        super(Strings.stringsToUrls(classPaths), null, null);
    }

    /** Load a class defined in a file, and return the {@link Class} object of
     *  the class.
     *
     *  @param classFile The file that defines the class.
     *  @return The object of the loaded class.
     *  @exception FileNotFoundException If the file cannot be found.
     *  @exception IOException If error occurs when trying to read the file.
     *  @exception LinkageError If {@link
     *   URLClassLoader#defineClass(java.lang.String, sun.misc.Resource)}
     *   issues a {@link LinkageError} and fails to define the class.
     *  @exception ClassNotFoundException If some classes referenced by the
     *   class in the file cannot be found.
     */
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

                if ((pos = className.indexOf(' ')) >= 0) {
                    className = className.substring(0, pos);
                }

                return loadClass(className);
            } else {
                throw e;
            }
        } catch (LinkageError e) {
            // Class already loaded.
            // FIXME: Any better solution here?
            String errorPrefix = "duplicate class definition: ";
            String message = e.getMessage();

            if (message.startsWith(errorPrefix)) {
                String path = message.substring(errorPrefix.length());
                String classFullName = path.replace('/', '.');
                return loadClass(classFullName);
            } else {
                throw e;
            }
        }
    }
}
