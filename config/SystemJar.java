/*
 Copyright (c) 2000-2003 The Regents of the University of California.
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

@@ProposedRating Green (cxh@@eecs.berkeley.edu)
@@AcceptedRating Red
*/
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/** Print the location of the system jar file, usually rt.jar
@author Christopher Hylands
@@version $Id$
@@since Ptolemy II 2.0
 */
public class SystemJar {
    public static void main(String args[]) {
        try {
            System.out.print(_getSystemJar());
        } catch (Exception exception) {
            System.err.print("SystemJar.main(): " + exception);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    // Return the path name to the system jar file, usually rt.jar.
    private static File _getSystemJar()
            throws IOException, FileNotFoundException {
        String systemJarPathName =
            new String(System.getProperty("java.home") +
                    File.separator + "lib" +
                    File.separator + "rt.jar");

        File systemJar = new File(systemJarPathName);

        // This would be a good place to search in other places, perhaps
        // by reading a property like ptolemy.system.jar
        // However, we should wait until this is a problem.
        // The code works with Sun JDK1.2 and 1.3 and IBM JDK1.3.
        if ( ! systemJar.isFile()) {
            // Try this for IBM JDK 1.4.1
            String systemJarPathName2 =
                new String(System.getProperty("java.home") +
                    File.separator + "lib" +
                    File.separator + "core.jar");
            systemJar = new File(systemJarPathName2);
            if ( ! systemJar.isFile()) {
                throw new FileNotFoundException(systemJarPathName +
                        " and " + systemJarPathName2
                        + "either do not exist or are not readable");
            }
            systemJarPathName = systemJarPathName2;
        }
        if ( ! systemJar.canRead()) {
            throw new IOException("Can't read '" + systemJarPathName +
                    "'");
        }
        return systemJar;
    }
}
