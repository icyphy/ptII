/* Interface for objects with a name and a container.

 Copyright (c) 1997 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)

*/

package pt.kernel;

//////////////////////////////////////////////////////////////////////////
//// Nameable
/** 
Interface for objects with a name and a container.
A simple name is an arbitrary string.
In addition, the interface supports a "full name" which in implementation
should identify both the container and the individual object.

This interface is separate from the primary implementation in the package,
NamedObj, so that arbitrary classes not derived from NamedObj can
implement it.  In particular, this permits construction of a NamedList
of arbitrary objects.

@author Edward A. Lee
@version $Id$
*/

public interface Nameable {

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Return the container.
     */
    public Nameable getContainer();

    /** In an implementation, the full name should reflect the container
     *  object, if there is one, for example by concatenating the
     *  full name of the container objects with the name of the this object,
     *  separated by periods ".".
     *  @return The full name of the object.
     */
    public String getFullName() throws InvalidStateException;

    /** @return The name of the object. */	
    public String getName();

    /** Set or change the name.
     *  @param name The new name.  
     */
    public void setName(String name) throws IllegalActionException;
}
