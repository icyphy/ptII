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

import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.net.URL;
import java.net.MalformedURLException;

//////////////////////////////////////////////////////////////////////////
//// BootstrapClassLoader
/**
This class loader is instantiated in order to load ptolemy classes.  It is
intended to be used as the very first ptolemy class that is loaded, and 
preferably the only one that is loaded by the system class loader.  This 
allows the majority of ptolemy classes to be loaded from arbitrary places 
(by adding their URLs to this class loader) and to allow them to be
recompiled and dynamically reloaded (by throwing away this classloader and 
creating a new one.)  

This would usually be invoked with something like:
java -classpath $PTII/boot -Dptolemy.PTII=file:$PTII BootstrapClassLoader ptolemy.vergil.VergilApplication

@author Steve Neuendorffer
@version $Id$
*/
public class BootstrapClassLoader extends URLClassLoader {
    public BootstrapClassLoader(URL[] urls, ClassLoader parent) {
	super(urls, parent);
    }

    public void addToClassPath(URL url) {
	addURL(url);
    }
    
    public static void main(String argv[]) {
	String ptIILocation = System.getProperty("ptolemy.PTII");
	URL urls[];
	try {
	    URL ptIIURL = new URL(ptIILocation);
	    urls = new URL[1];
	    urls[0] = ptIIURL;
	} catch (MalformedURLException ex) {
	    urls = new URL[0];
	}
	System.out.println("ptII = " + ptIILocation);
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



