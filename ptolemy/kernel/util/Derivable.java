/* Interface for objects that can be inherited.

Copyright (c) 2003-2004 The Regents of the University of California.
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

package ptolemy.kernel.util;

import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// Derivable
/**
   This interface is for objects that can be derived.  A derived object
   is "inherited" via the class mechanism in Ptolemy II.
   It is a created in its container as a side effect of the
   creation of another instance of Derivable in some other container.
   <p>
   After being created, a derived object, which is called "heritage,"
   might be modified in some (visible) way. For example, it might have parameter
   values that are changed and hence need to be explicitly recorded if
   the object is exported, say, to a persistent file format.
   Such a changed object is said to "override" its inherited value.

   @author Edward A. Lee
   @version $Id$
   @since Ptolemy II 4.0
   @Pt.ProposedRating Yellow (eal)
   @Pt.AcceptedRating Red (johnr)
*/

public interface Derivable extends Nameable {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the level above this object in the hierarchy where a
     *  parent-child relationship defines the existence of this object.
     *  A negative value (-1) is used to indicate that this object is
     *  not a derived object. A value of 0 indicates that the object
     *  is a child itself. A value of 1 indicates that the container
     *  of the object is a child, and that the this object is derived
     *  from a like object in the parent of the container. Etc.
     *  If an object is derived, then normally has no persistent
     *  representation when it is exported to MoML (unless it
     *  is overridden), and normally it cannot have its name or
     *  container changed.  An exception, however, is that the object
     *  may appear in the MoML if the exported MoML does not include
     *  the level of the hierarchy above this with the parent-child
     *  relationship.
     *  @return The level above this object in the containment
     *   hierarchy where a parent-child relationship defines this object.
     *  @see Instantiable
     *  @see #setDerivedLevel(int)
     */
    public int getDerivedLevel();

    /** Return a list of objects derived from this one.
     *  This is the list of objects that are "inherited" by their
     *  containers from a container of this object).
     *  Implementors may return an empty
     *  list, but should not return null. This method should return a
     *  complete list, including objects that have been overridden.
     *  All objects in the returned list are required to be of the same
     *  class as the object on which this method is called (they should
     *  be clones constructed directly or indirectly, via another clone).
     *  @return A list of objects of the same class as the object on
     *   which this is called.
     */
    public List getDerivedList();
    
    /** Propagate the value (if any) held by an implementor of this
     *  object to derived objects that have not been overridden.
     *  Implementors are required to leave all derived objects
     *  unchanged if any single derived object throws an exception
     *  when attempting to propagate the value to it.
     *  @return The list of objects to which propagation occurred.
     *  @throws IllegalActionException If propagation is not possible.
     */
    public List propagateValue() throws IllegalActionException;

    /** Set the level above this object in the hierarchy where a
     *  parent-child relationship defines the existence of this object.
     *  A negative value (-1) is used to indicate that this object is
     *  not a derived object. A value of 0 indicates that the object
     *  is a child itself. A value of 1 indicates that the container
     *  of the object is a child, and that the this object is derived
     *  from a like object in the parent of the container. Etc.
     *  If an object is derived, then normally has no persistent
     *  representation when it is exported to MoML (unless it
     *  is overridden), and normally it cannot have its name or
     *  container changed.  An exception, however, is that the object
     *  may appear in the MoML if the exported MoML does not include
     *  the level of the hierarchy above this with the parent-child
     *  relationship.
     *  @param level The level above this object in the containment
     *   hierarchy where a parent-child relationship defines this object.
     *  @see #getDerivedLevel()
     */
    public void setDerivedLevel(int level);
}
