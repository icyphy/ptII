/* Interface for objects that can be inherited.

Copyright (c) 2004-2005 The Regents of the University of California.
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
   is "inherited" via the class mechanism in Ptolemy II and tracks the
   object from which it is derived. That object is its "prototype."
   The derived object is usually created in its container as a side effect
   of the creation of the prototype.  The method propagateExistence()
   is the mechanism by which derived objects are created.
   <p>
   The prototype and its derived objects are instances of the same Java
   class, and by default, if they have a "value," then they have the
   same value. The derived object may, however, "override" its value.
   Propagation of changes in the value from the prototype to the
   derived objects that do not override the value is handled by the
   propagateValue() method.  It is up to that method to properly
   handle shadowing that might occur if a derived object that is
   overridden is also a prototype for other derived objects.
   <p>
   A derived object arises from a parent-child relationship, which
   is supported by the subinterface Instantiable. Every object that
   is (deeply) contained by a parent has a corresponding derived object
   that is (deeply) contained by the child. Thus, the existence of the
   derived object is "implied" by the parent-child relationship.
   The depth of a derived object relative to this parent-child
   relationship is returned by the getDerivedLevel() method.
   For example, if the container of a derived object is the
   child of the container of its prototype, then getDerivedLevel()
   will return 1.
   <p>
   The derived level returned by getDerivedLevel() affects whether
   a derived object is explicitly mentioned when a model is exported
   to MoML. Normally, a derived object is not mentioned in the MoML
   since its existence is "implied" by the existence of the prototype
   and the parent-child relationship. However, since it is possible
   to export MoML for any object in a hierarchical model, the exported
   MoML may not include the parent-child relationship, since it might
   be below it in the hierarchy.  In this case, the derived object
   will be described in the exported MoML, since it is not implied
   in the exported MoML.
   <p>
   The getDerivedList() method returns a list of all the objects that
   are derived from a prototype. The getPrototypeList() method returns
   a list of all prototypes from which this object is derived.

   @author Edward A. Lee
   @version $Id$
   @since Ptolemy II 4.0
   @see Instantiable
   @see NamedObj
   @Pt.ProposedRating Green (eal)
   @Pt.AcceptedRating Green (neuendor)
*/
public interface Derivable extends Nameable {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the minimum level above this object in the hierarchy where a
     *  parent-child relationship implies the existence of this object.
     *  A value Integer.MAX_VALUE is used to indicate that this object is
     *  not a derived object. A value of 1 indicates that the container
     *  of the object is a child, and that the this object is derived
     *  from a prototype in the parent of the container. Etc.
     *  @return The level above this object in the containment
     *   hierarchy where a parent-child relationship implies this object.
     */
    public int getDerivedLevel();

    /** Return a list of objects derived from this one.
     *  This is the list of objects that are "inherited" by their
     *  containers from a container of this object. The existence of
     *  these derived objects is "implied" by a parent-child relationship
     *  somewhere above this object in the containment hierarchy.
     *  <p>
     *  Implementors of this method may return an empty list,
     *  but should not return null. This method should return a
     *  complete list, including objects that have been overridden.
     *  All objects in the returned list are required to be of the same
     *  class as the object on which this method is called (they should
     *  be clones constructed directly or indirectly, via another clone).
     *  @return A list of objects of the same class as the object on
     *   which this is called.
     */
    public List getDerivedList();

    /** Return a list of prototypes for this object. The list is ordered
     *  so that more local prototypes are listed before more remote
     *  prototypes. A prototype is more local if the parent-child
     *  relationship is deeper in the containment hierarchy.
     *  @return A list of prototypes for this object, each of which is
     *   assured of being an instance of the same (Java) class as this
     *   object, or an empty list if there are no prototypes.
     *  @exception IllegalActionException If a prototype with the right
     *   name but the wrong class is found.
     */
    public List getPrototypeList() throws IllegalActionException;

    /** Propagate the existence of this object.
     *  If this object has a container, then ensure that all
     *  objects derived from the container contain an object
     *  with the same class and name as this object. Create that
     *  object when needed. Return the list of objects that are created.
     *  @return A list of derived objects of the same class
     *   as this implementor that are created, or an empty list
     *   if none are created.
     *  @exception IllegalActionException If the object cannot be created.
     */
    public List propagateExistence() throws IllegalActionException;

    /** Propagate the value (if any) held by this
     *  object to derived objects that have not been overridden.
     *  Implementors are required to leave all derived objects
     *  unchanged if any single derived object throws an exception
     *  when attempting to propagate the value to it.
     *  @return The list of objects to which propagation occurred.
     *  @exception IllegalActionException If propagation is not possible.
     */
    public List propagateValue() throws IllegalActionException;
}
