/*
An application that creates a tree of zero length .java files
that mimic the java.* and sun.* tree

Copyright (c) 2000 The Regents of the University of California.
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

import ptolemy.lang.StringManip;
import java.io.IOException;
import java.io.File;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/** An application that creates a tree of zero length .java files
that mimic the java.* and sun.* tree by reading in the rt.jar
file.  The java and sun directories are created in the current directory.

JavaParserManip.CompileUnitNode() looks for zero length
files and uses reflection if it finds them.  Eventually, this
class should be deleted.

@author: Christopher
@version: $Id$
 */
public class GenerateZeroLengthStubs {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Generate zero length .java files with the same name as
     *  the .class files in the system jar file.
     */
    public static void generateZeroLengthStubs() {
	File systemJarFile = _getSystemJar();
	JarFile systemJar = null;
	try {
	    systemJar = new JarFile(systemJarFile);
	} catch (IOException e) {
	    throw new RuntimeException("Failed to read '" + systemJarFile +
				       "': " + e);
	}

	for (Enumeration enumeration = systemJar.entries();
	     enumeration.hasMoreElements();) {
	    JarEntry jarEntry = (JarEntry)enumeration.nextElement();
	    //System.out.println(jarEntry.getName());
	    File jarFile = new File(jarEntry.getName());
	    if (jarEntry.isDirectory()) {
		System.out.println(jarFile.toString());
		jarFile.mkdirs();
	    } else {
		if (jarFile.getPath().endsWith(".class")) {
		    // Substitute in .java
		    File javaFile =
			new File(StringManip.partBeforeLast(jarFile.getPath(),
							    '.') + ".java");
		    //System.out.println(javaFile.toString());
		    try {
			javaFile.createNewFile();
		    } catch (IOException e2) {
			throw new RuntimeException("Failed to create '" +
						   jarFile.getName() + "': "
						   + e2);
		    }
		}
	    }
	}
    }


    /** If the number of args is less than 1 print a usage string, otherwise
     *  generate skeleton files for the remaining arguments.
     */
    public static void main(String[] args) {
	generateZeroLengthStubs();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Return the pathname to the system jar file, usually rt.jar.
    private static File _getSystemJar() {
	String systemJarPathName =
	    new String(System.getProperty("java.home") + "/lib/rt.jar");
	File systemJar = new File(systemJarPathName);

	// This would be a good place to search in other places.
	if ( ! systemJar.canRead()) {
	    throw new RuntimeException("Can't read '" + systemJarPathName +
				       "'");
	}
	return systemJar;
    }
}
