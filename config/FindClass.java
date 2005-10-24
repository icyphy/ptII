/* Find the location of a class file

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
import java.io.File;
import java.net.URI;
import java.net.URL;


/** Search the classpath for a jar file that contains a class file
    @author Christopher Hylands Brooks
    @version $Id$
    @since Ptolemy II 3.1
    @Pt.ProposedRating Green (cxh)
    @Pt.AcceptedRating Red
*/
public class FindClass {
    /** Given a dot separated class name, return the location of the
     *  class in the classpath.
     *  For example:
     *  java FindClass javax.media.CaptureDeviceInfo
     */
    public static void main(String[] args) throws Throwable {
        // The first argument should be a dot separated classname,
        // for example javax.media.CaptureDeviceInfo
        String className = args[0];
        Class refClass;

        try {
            refClass = Class.forName(className);
        } catch (Throwable throwable) {
            // Test this with:
            // java FindClass foo
            System.out.println("Not Found");
            return;
        }

        // Convert the classname to a path
        // for example javax/media/CaptureDeviceInfo.class
        String classNamePath = className.replace('.', '/') + ".class";
        URL entryURL = refClass.getClassLoader().getResource(classNamePath);

        if (entryURL.toString().startsWith("jar:file:")) {
            // The class was found in a jar file, so return
            // that jar file
            // Test this with:
            // java FindClass javax.media.CaptureDeviceInfo
            // C:/Program Files/JMF2.1.1/lib/jmf.jar
            String longJarFile = entryURL.getFile();
            String shortJarFile = longJarFile.substring(0,
                    longJarFile.indexOf("!/"));
            URL jarFileURL = new URL(shortJarFile);

            File jarFileURLFile = new File(jarFileURL.getFile().toString()
                                                             .replaceAll("%20",
                                " "));

            // If the jar file is in the jre directory, then don't
            // report it.  The reason is that we should not add things
            // in the jre directory to the classpath as Eclipse will barf
            // with a message about duplicate elements in the classpath
            String javaHome = System.getProperty("java.home");

            if (javaHome == null) {
                throw new Exception("Could not look up java.home property");
            }

            boolean foundInJavaHome = false;

            // Work our way up the directory tree and look for a match
            // with the value of java.home.
            File javaHomeFile = new File(javaHome);

            File directory = jarFileURLFile.getCanonicalFile();

            while (((directory = directory.getParentFile()) != null)
                        && (foundInJavaHome == false)) {
                if (directory.compareTo(javaHomeFile) == 0) {
                    foundInJavaHome = true;
                }
            }

            String jarFileURLFileName = jarFileURLFile.getCanonicalPath()
                                                              .replace('\\', '/');

            if (foundInJavaHome) {
                System.out.println("FindClass: '" + className + "' was found "
                    + "in\n      '" + jarFileURLFileName + "',\n      "
                    + "which is inside the java.home property\n      "
                    + javaHome + ".\n      "
                    + "Thus, it is not necessary to add the jar file "
                    + "to the eclipse classpath");
            } else {
                System.out.println(jarFileURLFileName);
            }
        } else if (entryURL.getProtocol().equals("file")) {
            // Test this with
            // java -classpath "$PTII;." FindClass ptolemy.kernel.util.NamedObj
            // You should get something like:
            // C:/cxh/ptII/ptolemy/kernel/util/NamedObj.class
            File classFile = new File(entryURL.getFile());
            System.out.println(classFile.getCanonicalPath().replace('\\', '/'));
        } else {
            System.out.println(entryURL);
        }
    }
}
