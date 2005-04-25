/* Interface for objects that can be instantiated.

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
//// Instantiable

/**
   This interface is for objects that can be instantiated.  Such objects serve a
   role similar to "classes" in Java and other object oriented languages.
   They are Ptolemy II objects, but whose purpose is to serve as
   a factory for instances that are (deep) clones of themselves.  Normally,
   they play no role in the execution of a model, and their ports cannot
   be connected to other ports.
   <p>
   An object that implements this interface can be in one of two states.
   Either it is a class definition ({@link #isClassDefinition()} returns true)
   or it is not.  Only objects that are class definitions can be instantiated.
   Instantiation is done via the {@link #instantiate(NamedObj, String)}
   method. If an object
   is instantiated from this object, then the new object is called the "child"
   and this object is called the "parent." An instance of Instantiable can have
   at most one parent (returned by the {@link #getParent()} method), but it
   can have many children (returned by the {@link #getChildren()} method).
   An object may be both a child and a parent.
   <p>
   A child is required to be a deep clone of its parent. That is, every
   object deeply contained by the parent must have a corresponding object
   deeply contained by the child.  The object that is deeply contained
   by the parent is called the "prototype" and the object deeply contained
   by the child is called the "derived" object. A derived object has the
   same name relative to the child as the prototype has relative to the
   parent. Moreover, a derived object is an instance of the same Java
   class as the prototype.
   <p>
   This correspondence between a parent and child is called the
   "derivation invariant." Any correct implementation of this interface
   must ensure that the derivation invariant is always satisfied, even
   if the parent changes after the child was instantiated. If new objects
   are added to the parent, then derived objects must be added to the
   child.

   @author Edward A. Lee
   @version $Id$
   @since Ptolemy II 4.0
   @Pt.ProposedRating Green (eal)
   @Pt.AcceptedRating Green (neuendor)
*/
public interface Instantiable extends Derivable {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a list of weak references to instances of Instantiable
     *  that are children of this object.  An implementor of this method
     *  may return null or an empty list to indicate that there are
     *  no children.
     *  @return An unmodifiable list of instances of
     *   java.lang.ref.WeakReference that refer to
     *   instances of Instantiable or null if this object
     *   has no children.
     */
    public List getChildren();

    /** Return the parent of this object, or null if there is none.
     *  @return The parent of this object, or null if there is none.
     */
    public Instantiable getParent();

    /** Create an instance by (deeply) cloning this object and then adjusting
     *  the parent-child relationships between the clone and its parent.
     *  Specifically, the clone defers its definition to this object,
     *  which becomes its "parent." The "child" inherits all the objects
     *  contained by this object. If this object is a composite, then this
     *  method must adjust any parent-child relationships that are entirely
     *  contained within the child. That is, for any parent-child relationship
     *  that is entirely contained within this object (i.e., both the parent
     *  and the child are deeply contained by this object), a corresponding
     *  parent-child relationship is created within the clone such that
     *  both the parent and the child are entirely contained within
     *  the clone.
     *  <p>
     *  The new object is not a class definition by default (it is an
     *  "instance" rather than a "class"). That is, {@link #isClassDefinition()}
     *  returns <i>false</i>.
     *  @param container The container for the instance, or null
     *   to instantiate it at the top level.
     *  @param name The name for the instance.
     *  @return A new instance that is a clone of this object
     *   with adjusted deferral relationships.
     *  @exception CloneNotSupportedException If this object
     *   cannot be cloned.
     *  @exception IllegalActionException If this object is not a
     *   class definition or the proposed container is not acceptable.
     *  @exception NameDuplicationException If the name collides with
     *   an object already in the container.
     */
    public Instantiable instantiate(NamedObj container, String name)
        throws CloneNotSupportedException, IllegalActionException,
            NameDuplicationException;

    /** Return true if this object is a class definition, which means that
     *  it can be instantiated.
     *  @return True if this object is a class definition.
     */
    public boolean isClassDefinition();
}
