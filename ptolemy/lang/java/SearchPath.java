/* A vector containing paths to search for when resolving an import or
package.

Copyright (c) 1998-2001 The Regents of the University of California.
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
import java.io.FileNotFoundException;
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

This class is used by the code generation system to find class
definitions.  The codegen system uses reflection to find definitions
of some classes and parses .java files for other classes.  This
class provides functionality to find classes in both categories.
This class can be thought of as an augmentation to the CLASSPATH
facility.  Note that we can't use the ClassLoader because
not all of the .java files we are interested in will have been compiled.

This class also defines Sets of classes and packages to help find
System classes and packages as well as Ptolemy core classes and
packages.

<p>There are three ways that classes are read in.
<ol>
<li> The class is a System class (such as java.lang.Object),
and it is read in using reflection.
<li> The class is a Ptolemy core class (such as ptolemy.kernel.util.NamedObj)
and it is read in using reflection.
<li> The class is read in as a file and parsed.
</ol>



<p>
Portions of this code were derived from sources developed under the
auspices of the Titanium project, under funding from the DARPA, DoE,
and Army Research Office.

<p>FIXME: This should probably not extend Vector, instead it should
extend ArrayList, which is not synchronized, see:
http://www.javasoft.com/docs/books/tutorial/collections/implementations/general.html

@author Jeff Tsay, Christopher Hylands
@version $Id$
 */
public class SearchPath extends Vector {

    /** Construct a SearchPath object by reading the propertyName
     *  property, if propertyName is null or does not name a property
     *  then use fallbackPaths.  The value of the property named by
     *  propertyName and the value of fallbackPaths should contain
     *  a string with path names separated by File.separatorChar.
     *  @param propertyName Name of the property to look for.
     *  @param fallbackPaths Path list to use if propertyName can't be found.
     *  The Path list consists of path names that name directories
     *  where adjacent path names are separated by File.pathSeparatorChar.
     */
    public SearchPath(String propertyName, String fallbackPaths) {
        if (propertyName != null) {
            String propertyValue = System.getProperty(propertyName, ".");

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
     *  @param target The qualified class name, which may either be qualified
     *  by the '.' character or by the value of File.pathSeparatorChar.
     *  @return an instance of File associated with the path of the
     *  source code.
     *  @exception IOException If there is a problem getting the
     *  canonical name of the file.
     *  @exception FileNotFoundException If the source file cannot be found
     */
    public File openSource(String target)
            throws IOException, FileNotFoundException {

	// Convert a Java qualified name into a partial path name, without the
	// file extension. For example, "ptolemy.lang.java.SearchPath" is
	// converted to "ptolemy/lang/java/SearchPath" under Unix
        String targetPath = target.replace('.', File.separatorChar);


        for (int i = 0; i < size(); i++) {
            String candidate = (String) get(i);

            String fullName = new String(candidate + targetPath + ".java");
            File file = new File(fullName);

            if (file.isFile()) {
                // This might throw IOException if we can't get the
                // canonical name.
                return file.getCanonicalFile();
            }
        }
        throw new FileNotFoundException("Could not find source for " +
                target);
    }

    /** Return a Set that contains an entry for each class in the
     *  in the Ptolemy II core as listed in ptolemyCorePackages.
     *  Classes are found by reading SearchPath.NAMED_PATH.
     *  The entry will be a String of the form "ptolemy.kernel.util.NamedObj".
     *  As a side effect, this method also updates the public variable
     *  ptolemyCorePackageSet, which contains the names of the Ptolemy
     *  core Packages.
     *  @returns A set of Strings where each element is a fully
     *  qualified class name.
     */
    public static Set ptolemyCoreClasses() {
        // Create a HashSet with a size of 421
        // The number of .class files in the Ptolemy core is 219
        // Determine the number of .class files in the Ptolemy core with:
        // find . -name "*.class" -print | egrep 'ptolemy/kernel|ptolemy/actor/util|ptolemy/actor/sched|ptolemy/data|ptolemy/graph|ptolemy/math' | grep -v vergil grep -v test | wc
        // The Collections tutorial suggests a prime number slightly
        // larger than twice the size of the Set.
        // Note that we have a test in the test suite that will warn
        // us if the number of classes is too large and we need to adjust
        // the size of the HashSet.
        Set classSet = new HashSet(421);

        // Array of names of packages that are in the Ptolemy core.
        // We don't parse java files in these packages, we use
        // reflection instead.
        String [] ptolemyCorePackages = {
            "ptolemy/actor/sched",
            "ptolemy/actor/util",
            "ptolemy/kernel",
            "ptolemy/kernel/util",
            "ptolemy/codegen/data",
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
                String directoryName = path + ptolemyCorePackages[p];
                File directory = new File(directoryName);
                if (directory.isDirectory()) {
                    String[] nameList = directory.list();
                    foundPackages[p] = true;
                    for (int j = 0; j < nameList.length; j++) {
                        String name = nameList[j];
                        int length = name.length();
                        if (StringManip.unqualifiedPart(name).
                                equals("class")) {
                            classSet.add(
                                    ptolemyCorePackages[p].replace('/', '.') +
                                    "." +
                                    StringManip.partBeforeLast(name, '.'));
			    if (name.indexOf('$') != -1) {
				// Handle inner classes
				classSet.add(
                                    ptolemyCorePackages[p].replace('/', '.') +
                                    "." +
                                    (StringManip.partBeforeLast(name, '.')).replace('$','.'));
			    }
                        }
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
            ptolemyCorePackageSet.add(
                    ptolemyCorePackages[p].replace('/', '.'));
        }

        return classSet;
    }

    /** Return a Set that contains an entry for each class in the
     *  system jar file and in any jar or zip files in the classpath.
     *  Note that classes will have entries like "java.lang.Object", they
     *  will not have extension like .class or .java
     *  As a side effect, this method also updates the public variable
     *  systemPackageSet, which contains the names of the Java runtime
     *  system Packages.
     *  @returns A set of Strings where each element is a fully
     *  qualified class name.
     *  @exception FileNotFoundException If we cannot find the JDK jar
     *  file 'rt.jar' or any of the jar or zip files in the classpath.
     *  @exception IOException If we cannot read the JDK jar file 'rt.jar'.
     *  or any of the jar or zip files in the classpath.
     */
    public static Set systemClasses()
            throws IOException, FileNotFoundException {
        // We use class names because they are . separated,
        // whereas filenames are separated by a platform dependent char.

        // Create a HashSet with a size of 17011
        // The number of .class files in Windows JDK1.4beta2 rt.jar is 7994
        // Determine that the number of .class files in rt.jar with:
        // jar -tvf rt.jar | grep '.class' | wc -l
        // The number of .class files in JavaScope.zip is 385
        // The Collections tutorial at
        // http://www.javasoft.com/docs/books/tutorial/collections/implementations/general.html
        // says:
        // "If you accept the default load factor but you do want to
        // specify an initial capacity, pick a number that's
        // about twice the size that you expect the Set to grow to."
        // It also suggests selecting a prime number just larger.
        // Primes can be found at
        // http://www.utm.edu/research/primes/lists/small/10000.txt
        // Note that we have a test in the test suite that will warn
        // us if the number of classes is too large and we need to adjust
        // the size of the HashSet.
        Set classSet = new HashSet(17011);

        systemPackageSet = new HashSet();
        // Now read in the system jar file (jre/lib/rt.jar) and
        // add each .class file to the set
        File systemJarFile = _getSystemJar();

	JarFile systemJar = null;
	try {
	    systemJar = new JarFile(systemJarFile);
	} catch (IOException e) {
	    throw new IOException("Failed to read '" + systemJarFile +
                    "': " + e);
	}

	for (Enumeration enumeration = systemJar.entries();
	     enumeration.hasMoreElements();) {
	    JarEntry jarEntry = (JarEntry)enumeration.nextElement();
	    File jarFile = new File(jarEntry.getName());
	    if (jarEntry.isDirectory()) {
                systemPackageSet.add(jarFile.getPath().
                        replace(File.separatorChar, '.'));
            } else {
                if (jarFile.getPath().endsWith(".class")) {
		    // Strip off the .class,
		    // substitute . for File.separatorChar
                    classSet.add((StringManip.partBeforeLast(jarFile.getPath(),
                            '.')).replace(File.separatorChar, '.'));
                }
            }
	}

        // Look for any .zip or .jar files in the classpath and
        // add the classes contained in the zip or jar file to the set.
        for (Enumeration searchPathEnumeration = NAMED_PATH.elements();
	     searchPathEnumeration.hasMoreElements();) {
            String classPathElement = 
                (String)searchPathEnumeration.nextElement();
            if (classPathElement.endsWith(".jar")
                    || classPathElement.endsWith(".zip")) {
                JarFile classPathJar = null;
                try {
                    classPathJar = new JarFile(classPathElement);
                } catch (IOException e) {
                    throw new IOException("Failed to read '"
                            + classPathElement + "': " + e);
                }

                for (Enumeration enumeration = classPathJar.entries();
                     enumeration.hasMoreElements();) {
                    JarEntry jarEntry = (JarEntry)enumeration.nextElement();
                    File jarFile = new File(jarEntry.getName());
                    if (jarEntry.isDirectory()) {
                        systemPackageSet.add(jarFile.getPath().
                                replace(File.separatorChar, '.'));
                    } else {
                        if (jarFile.getPath().endsWith(".class")) {
                            // Strip off the .class,
                            // substitute . for File.separatorChar
                            classSet.add((StringManip.partBeforeLast(jarFile.getPath(),
                                    '.')).replace(File.separatorChar, '.'));
                        }
                    }
                }
            }
        }

        return classSet;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Vector of containing directories to search for classes in.
     *  Initially set to the value of the java.class.path JVM property.
     */
    public static final SearchPath NAMED_PATH =
            new SearchPath("java.class.path", ".");

    /** Vector containing directories to search for classes in.
     *  Initially set to the current directory "."
     */
    public static final SearchPath UNNAMED_PATH =
    new SearchPath(null, ".");

    /** Set of Strings that name all class files in the system jar file.
     *  Entries are fully qualified classname Strings like "java.lang.Object".
     */
    public static Set systemClassSet;

    /** Set of Strings that name all the packages in the system jar file.
     *  Entries are fully qualified package name Strings like "java.lang".
     */
    public static Set systemPackageSet;

    /** Set of Strings that name the .java files in the Ptolemy II core.
     *  Entries are fully qualified classname Strings like
     *  "ptolemy.kernel.util.NamedObj".
     */
    public static Set ptolemyCoreClassSet = ptolemyCoreClasses();

    /** Set of Strings that name all the Ptolemy II Core packages
     *  Entries are fully qualified package name Strings like
     *  "ptolemy.kernel.util".
     */
    public static Set ptolemyCorePackageSet;


    // Do things in a static initializer so that we can properly
    // handle exceptions.
    static {
        try {
            systemClassSet = systemClasses() ;
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Split a String consisting of 0 or more path names separated by
    // File.pathSeparator, and add them to the Vector of paths.
    private void _addPaths(String paths) {
        int begin = 0;

        int end;
        do {
            // FIXME: use a tokenizer here.
            end = paths.indexOf(File.pathSeparator, begin);
            String path = null;
            if (end == -1) {
                path = paths.substring(begin);
                if (path.length() > 0) {
                    if (path.endsWith(".jar") || path.endsWith(".zip")) {
                        add(path);
                    } else {
                        add(path + File.separatorChar);
                    }
                }
            } else {
                path = paths.substring(begin, end).replace('/',
                        File.separatorChar);
                if (path.length() > 0) {
                    if (path.endsWith(".jar") || path.endsWith(".zip")) {
                        add(path);
                    } else {
                        add(path + File.separatorChar);
                    }
                }
                begin = end + 1;
            }
        } while (end > -1);
    }

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
	    throw new FileNotFoundException(systemJarPathName +
                    " either does not exist or is not readable");
        }
	if ( ! systemJar.canRead()) {
	    throw new IOException("Can't read '" + systemJarPathName +
                    "'");
	}
	return systemJar;
    }
}
