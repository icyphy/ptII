/* A Classloader for bootstrapping Ptolemy.

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

@ProposedRating Red (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

import java.io.File;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.StringTokenizer;

//////////////////////////////////////////////////////////////////////////
//// BootstrapClassLoader
/**
This class loader is instantiated in order to load ptolemy classes in a more
flexible way than is allowed by the default class loader.  It is
intended to be used as the very first ptolemy class that is loaded, and 
preferably the only one that is loaded by the system class loader.  This 
allows the majority of ptolemy classes to be loaded from arbitrary places 
(by adding their URLs to this class loader) and to allow them to be
recompiled and dynamically reloaded (by throwing away this classloader and 
creating a new one.)  

This would usually be invoked with something like:
java -classpath $PTII/boot -Dptolemy.class.path=file:$PTII/ BootstrapClassLoader ptolemy.vergil.VergilApplication

Note that the constructors to URL that take a string are rather picky as to the
format of the string.  For example, a file URL that references a directory 
must end in '/'.  A slightly more robust way to create file URLs is to create
a File object and use the toURL() method, since the string constructors to the
File class refer to files and directories in the same way.

@author Steve Neuendorffer
@version $Id$
*/
public class BootstrapClassLoader extends URLClassLoader {
    /**
     * Create a new class loader that searches the given urls for classes and
     * has the given class loader as its parent.
     */
    public BootstrapClassLoader(URL[] urls, ClassLoader parent) {
	super(urls, parent);
    }

    /**
     * Add the given URL to the classpath that this class searches for classes.
     * This method may be called after this class is created to dynamically
     * add classes to the classpath.
     */
    public void addToClassPath(URL url) {
	addURL(url);
    }
    
    /**
     * Create a new bootstrap class loader and use it to load the class
     * whose full name is given by the first argument.  Since ideally this
     * class is the only one that the default class loader can find, it is 
     * necessary to give a set of default URLs to search to the bootstrap
     * class loader.  These URLs are created by parsing the environment
     * variable "ptolemy.class.path".  This environment variable works 
     * similarly to a regular classpath: it accepts any kind of URL or 
     * directory and multiple paths are separated by the platform-dependent 
     * class path separator.  
     *
     * After instiating the class given in the first argument, its static
     * main method is reflected and called with the remaining arguments.
     */
    public static void main(String argv[]) {
	// Create an array of URLs representing the class path.
	String classpath = System.getProperty("ptolemy.class.path");
	String separator = System.getProperty("path.separator");
	StringTokenizer paths = new StringTokenizer(classpath, separator);
	URL urls[] = new URL[paths.countTokens()];
	int count = 0;
	while(paths.hasMoreTokens()) {
	    String path = paths.nextToken();
	    try {
		urls[count] = new URL(path);
	    } catch (MalformedURLException ex) {
		// hmm.. maybe they wrote it down as a file.  Let's
		// try adding it as a file.  
		try {
		    File file = new File(path);
		    urls[count] = file.toURL();
		} catch (MalformedURLException ex2) {
		    ex2.printStackTrace();
		    System.exit(1);
		}
	    }
	    count++;
	}
	ClassLoader loader = 
	new BootstrapClassLoader(urls, getSystemClassLoader());
	try {
	    Class startClass = loader.loadClass(argv[0]);
	    Class[] argumentTypes = {argv.getClass()};
	    Object[] arguments = {new String[argv.length-1]};
	    System.arraycopy(argv, 1, arguments[0], 0, argv.length-1);
	    Method main = startClass.getMethod("main", argumentTypes);
	    main.invoke(null, arguments);	
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }
}



