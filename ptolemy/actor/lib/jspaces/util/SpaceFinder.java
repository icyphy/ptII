/* A utility class that finds a Java Space.

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

@ProposedRating Red (yuhong@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.jspaces.util;

import ptolemy.kernel.util.IllegalActionException;

import java.rmi.*;

import net.jini.space.JavaSpace;

import com.sun.jini.mahout.binder.RefHolder;
import com.sun.jini.mahout.Locator;
import com.sun.jini.outrigger.Finder;

//////////////////////////////////////////////////////////////////////////
//// SpaceFiner
/**
An utility that finds a Java Space.

This class is based on Sun's SpaceAccessor class in the book "JavaSpaces
Principles, Patterns, and Practice", by Freeman, Hupfer, and Arnold.

@author Yuhong Xiong, Jie Liu
@version $Id$
*/

public class SpaceFinder {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a JavaSpace with the specified name.
     *  @param name A String.
     *  @return A JavaSpace.
     *  @exception IllegalActionException If a JavaSpace cannot be
     *   found.
     */
    public static JavaSpace getSpace(String name)
	    throws IllegalActionException {
        try {
            if (System.getSecurityManager() == null) {
                System.setSecurityManager(
                    new RMISecurityManager());
            }
                      
            if (System.getProperty("com.sun.jini.use.registry") == null) {
                Locator locator =
                    new com.sun.jini.outrigger.DiscoveryLocator();
                Finder finder =
                    new com.sun.jini.outrigger.LookupFinder();
                return (JavaSpace)finder.find(locator, name);
            } else {
                RefHolder rh = (RefHolder)Naming.lookup(name);
                return (JavaSpace)rh.proxy();
            }
        } catch (Exception ex) {
            throw new IllegalActionException("Cannot find JavaSpace. " +
					ex.getMessage());
        }
    }
    
    /** Return a JavaSpace with the default name "JavaSpaces".
     *  @return A JavaSpace.
     *  @exception IllegalActionException If a JavaSpace cannot be
     *   found.
     */
    public static JavaSpace getSpace() throws IllegalActionException {
        return getSpace("JavaSpaces");
    }
}

