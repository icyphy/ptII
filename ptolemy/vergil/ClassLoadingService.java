/* A service that contains a classloader for the application.

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

@ProposedRating Red (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil;

import BootstrapClassLoader;
import java.lang.ClassLoader;
import java.net.URLClassLoader;
import java.net.URL;
import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// ClassLoadingService
/**
A service that provides a class loader.  Modules will generally want to
add an appropriate classpath for themselves to this service to allow other
modules to load their classes.  This class assumes that the classloader
that it was created with was an instance of bootstrap class loader.

@see boot/BootstrapClassLoader
@author Steve Neuendorffer
@version $Id$
*/
public class ClassLoadingService implements Service {
    /**
     * Return the class loader that this object was created with.
     * Classes making use of this service can easily use this method to 
     * get a class loader that can be used to create new classes.
     */
    public ClassLoader getClassLoader() {
	return getClass().getClassLoader();
    }

    /**
     * Add the given URL to the search path of the BootstrapClassLoader.  
     * This method requires that the class loader that this class was 
     * loaded with was an instance of BootstrapClassLoader.
     */
    public void addToClassPath(URL url) {
	ClassLoader loader = getClassLoader();
	if(loader instanceof BootstrapClassLoader)
	    ((BootstrapClassLoader)loader).addToClassPath(url);
	else
	    throw new InternalErrorException("ClassLoader should be a " + 
		"BootstrapClassLoader, but instead was " + loader);
    }
}







