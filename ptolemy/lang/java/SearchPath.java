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
import java.util.Enumeration;
import java.util.Set;
import java.util.HashSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import ptolemy.lang.StringManip;

/** A vector containing paths to search for when resolving an import or
package.

<p>There are three ways that classes are read in.
<ol>
<li> The class is a System classes (such as java.lang.Object), 
and it is read in using reflection
<li> The class is a Ptolemy core class (such as ptolemy.kernel.util.NamedObj)
and it is read in using reflection.
<li> The class is read in as a filel and parsed.
</ol>

This class also defines sets of classes and packages to help find
System classes and packages as well as Ptolemy Core classes and
packages.


<p>
Portions of this code were derived from sources developed under the
auspices of the Titanium project, under funding from the DARPA, DoE,
and Army Research Office.

FIXME: This should probably not extend Vector, instead it should
extend ArrayList, which is not synchronized.
http://www.javasoft.com/docs/books/tutorial/collections/implementations/general.html

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

            //System.out.println("propertyValue = " + propertyValue);

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

    /** Open the Java source file with the qualified class name.
     *  @param The qualified class name, which may either be qualified
     *  by the '.' character or by the value of File.pathSeparatorChar.
     *  @return an instance of File associated with the path of the
     *  source code. If the source code cannot be found, return null.
     */
    public File openSource(String target) {

	// Convert a Java qualified name into a partial pathname, without the
	// file extension. For example, "ptolemy.lang.java.SearchPath" is
	// converted to "ptolemy/lang/java/SearchPath" under Unix
        String targetPath = target.replace('.', File.separatorChar);


        for (int i = 0; i < size(); i++) {
            String candidate = (String) get(i);

            String fullName = new String(candidate + targetPath + ".java");
            File file = new File(fullName);

            if (file.isFile()) {
                try {
                    file = file.getCanonicalFile();
                } catch (IOException ioe) {
                    throw new RuntimeException(
                            "cannot get canonical filename");
                }
            }

            if (file != null) {
                return file;
            }
        }
        return null;
    }

    /** Return a Set that contains an entry for each class in the
     * in the Ptolemy II core as listed in ptolemyCorePackages.
     * The entry will be of the form ptolemy/kernel/util/NamedObj
     */
    public static Set ptolemyCoreClasses() {
        // Create a HashSet with a size of 373
        // The number of .class files in the Ptolemy core is 186
        // Determine that the number of .class files in rt.jar with:
        // find . -name "*.class" -print | egrep 'ptolemy/kernel|ptolemy/actor/util|ptolemy/actor/sched|ptolemy/data|ptolemy/graph|ptolemy/math' | grep -v test | wc
        // The Collections tutorial suggests a prime number slighly
        // larger than twice the size of the Set.
        Set classSet = new HashSet(273);

        // Array of names of packages that are in the Ptolemy core.
        // We don't parse java files in these packages, we use
        // reflection instead.
        String [] ptolemyCorePackages = {
            "ptolemy/actor/sched",
            "ptolemy/actor/util",
            "ptolemy/kernel",
            "ptolemy/kernel/util",
            "ptolemy/data",
            "ptolemy/data/type",
            "ptolemy/graph",
            "ptolemy/math" 
        };

        ptolemyCorePackageSet = new HashSet();

        // As we find packages, we mark them as done in this array
        // so as to avoid duplication.
        boolean [] foundPackages = new boolean [ptolemyCorePackages.length];

        for (int i = 0; i < NAMED_PATH.size(); i++) {
            String path = (String) NAMED_PATH.get(i);        
            for(int p = 0; p < ptolemyCorePackages.length &&
                    !foundPackages[p];
                    p++) {
                String dirName = path + ptolemyCorePackages[p];
                File dir = new File(dirName);
                if (dir.isDirectory()) {
                    String[] nameList = dir.list();
                    foundPackages[p] = true;
                    for (int j = 0; j < nameList.length; j++) {
                        String name = nameList[j];
                        int length = name.length();
                        String className = null;
                            if ((length > 6) && 
                                    name.substring(length - 6).
                                    equals(".class")) {
                                className = name.substring(0, length - 6);
                            }
                        classSet.add(ptolemyCorePackages[p].replace('/','.') + "." +
                                className);
                    }
                }
            }
        }
        // Check that we found all the packages and add them
        // to ptolemyCorePackageSet
        for (int p = 0; p < ptolemyCorePackages.length; p++) {
            if (!foundPackages[p]) {
                throw new RuntimeException("SearchPath.ptolemyCoreClasses():" +
                        " Could not find package " + ptolemyCorePackages[p] +
                        " Searched in " + NAMED_PATH.toString());
            }
            ptolemyCorePackageSet.add(ptolemyCorePackages[p].replace('/','.'));
        }

        return classSet;
    }
    /** Return a Set that contains an entry for each class in the
     * system jar file. 
     * Note that classes will have entries like java.lang.Object, they
     * will not have extension like .class or .java
     */
    public static Set systemClasses() {
        // We use classnames because they are . separated,
        // whereas filenames are separated by a platform dependent char.

        // Create a HashSet with a size of 10427.
        // The number of .class files in rt.jar is 5213.
        // Determine that the number of .class files in rt.jar with:
        // jar -tvf rt.jar | grep '.class' | wc -l
        // The Collections tutorial at
        // http://www.javasoft.com/docs/books/tutorial/collections/implementations/general.html
        // says:
        // "If you accept the default load factor but you do want to
        // specify an initial capacity, pick a number that's 
        // about twice the size that you expect the Set to grow to."
        // It also suggests selecting a prime number just larger.
        // Primes can be found at
        // http://www.utm.edu/research/primes/lists/small/10000.txt
        Set classSet = new HashSet(10427); 

        systemPackageSet = new HashSet();
        // Now read in the system jar file (jre/lib/rt.jar) and
        // add each .class file to the set
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
                systemPackageSet.add(jarFile.getPath().
				     replace(File.separatorChar,'.'));
            } else {
                if (jarFile.getPath().endsWith(".class")) {
		    // Strip off the .class, 
		    // substitute . for File.separatorChar
                    classSet.add((StringManip.partBeforeLast(jarFile.getPath(),
                            '.')).replace(File.separatorChar,'.'));
                }
            }
	}
        return classSet;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    public static final SearchPath NAMED_PATH =
	new SearchPath("java.class.path", ".");

    public static final SearchPath UNNAMED_PATH =
	new SearchPath(null, ".");

    /** Set of Strings that name all class files in the system jar file.
     */
    public static Set systemClassSet = systemClasses() ;

    /** Set of Strings that name all the packages in the system jar file.
     */   
    public static Set systemPackageSet;

    /** Set of Strings that name the .java files in the Ptolemy II core
     */
    public static Set ptolemyCoreClassSet = ptolemyCoreClasses();

    /** Set of Strings that name all the Ptolemy II Core packages */
    public static Set ptolemyCorePackageSet;


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
                    System.out.println("adding " + path + File.separatorChar);
		    add(path + File.separatorChar);
                }
            } else {
                path = paths.substring(begin, end).replace('/',
							   File.separatorChar);
                if (path.length() > 0) {
                    System.out.println("adding"  + path + File.separatorChar);
		    add(path + File.separatorChar);
                }
                begin = end + 1;
            }
        } while (end > -1);
    }

    // Return the pathname to the system jar file, usually rt.jar.
    private static File _getSystemJar() {
	String systemJarPathName =
	    new String(System.getProperty("java.home") + "/lib/rt.jar");
	File systemJar = new File(systemJarPathName);

	// This would be a good place to search in other places, perhaps
        // by reading a property like ptolemy.system.jar
	if ( ! systemJar.canRead()) {
	    throw new RuntimeException("Can't read '" + systemJarPathName +
				       "'");
	}
	return systemJar;
    }
}
