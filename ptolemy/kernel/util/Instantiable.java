/* Interface for objects that can be instantiated.

 Copyright (c) 1997-2003 The Regents of the University of California.
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
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.kernel.util;

import java.util.List;


//////////////////////////////////////////////////////////////////////////
//// Instantiable
/**
This interface is for objects that can be instantiated.  Such objects serve a
role similar to "classes" in Java and other object oriented languages,
but function more as prototypes than classes. They are ordinary Ptolemy II
objects, but whose purpose is to serve as a factory for instances that
are clones of themselves.  Normally, they play no role in the execution
of a model.
<p>
An object that implements this interface can be in one of two states.
Either it is a class definition (isClassDefinition() returns true)
or it is not.  Only objects that are class definitions can be instantiated.
(They are said to be "fertile").  Objects that are class definitions
are expected to be ignored by the execution engine of a model.

@author Edward A. Lee
@version $Id$
@since Ptolemy II 4.0
*/

public interface Instantiable extends Heritable {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get a list of weak references to instance of Instantiable
     *  that defer to this object.
     *  @return An unmodifiable list of weak references to
     *   instances of Instantiable or null if no object defers to this one.
     */
    public List getChildren();

    /** Get the instance to which this object defers its definition.
     *  @return A prototype or null to indicate that this object does
     *   not defer its definition.
     */
    public Instantiable getParent();

    /** Return true if this object is a class definition, which means that
     *  it can be instantiated.
     *  @return True if this object is a class definition.
     *  @see #setClassDefinition(boolean)
     */
    public boolean isClassDefinition();

    /** Return the maximum deferral depth of this object.
     *  Going up the hierarchy, each time a container is encountered
     *  that defers its definition to a parent, increment the parent
     *  depth by one. Return the largest such
     *  incremented depths, or zero if no container defers its
     *  definition.
     *  @return The maximum deferral depth.
     */
    public int maximumParentDepth();

    /** Specify whether this object is a class definition.
     *  @param isClass True to make this object a class definition.
     */
    public void setClassDefinition(boolean isClass);

    /** Specify that this object defers its definition to another
     *  object.  This should be called to make this object either an
     *  an instance or a subclass of the other object.
     *  @param parent The object to defer to, or null to defer to none.
     *  @exception IllegalActionException If the parent is not acceptable.
     */
    public void setParent(Instantiable parent) throws IllegalActionException;

    /** Instantiate an instance by cloning this prototype and making any
     *  changes that are required in the clone.
     *  @param container The container for the instance.
     *  @param name The name for the clone.
     *  @return A new instance of the same class that implements
     *   this Instantiable interface.
     *  @exception CloneNotSupportedException If this prototype
     *   cannot be cloned.
     *  @exception IllegalActionException If this object is not a class definition
     *   or the proposed container is not acceptable.
     *  @exception NameDuplicationException If the name collides with
     *   an object already in the container.
     *  @see #isClassDefinition()
     */
    public Instantiable instantiate(NamedObj container, String name)
            throws CloneNotSupportedException,
            IllegalActionException, NameDuplicationException;
}
