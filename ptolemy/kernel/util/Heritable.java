/* Interface for objects that can be inherited.

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
//// Heritable
/**
This interface is for objects that can be inherited.  An inherited object
is one that is inherited by its container from some other object.
That is, it is a created in its container as a side effect of the
creation of another instance of Heritable in some other container.
<p>
Note that unlike the real-world notion of inheritance that inspires
the name of this interface, an inherited object is a <i>new</i> instance
that is similar to the original object, but with a new container.
It is as if instead of dividing an estate, the estate could be cloned
for each inheritor. Moreover, the original owner of the estate does
not need to die, and can continue to own the original objects.
Consequently, there may simultaneously be several heritage objects
which are created as side effects of creating the one. The
heritageList() method returns a list of such objects currently in
existence.
<p>
After being created, an inherited object, which is called "heritage,"
might be modified in some (visible) way. For example, it might have parameter
values that are changed and hence need to be explicitly recorded if
the object is exported, say, to a persistent file format.
Such a changed object is referred to as "modified heritage."

@author Edward A. Lee
@version $Id$
@since Ptolemy II 4.0
*/

public interface Heritable extends Nameable {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a list of objects to which changes to
     *  this object should propagate. This is the list of objects that
     *  are created as a side effect of creating this one (that is, they
     *  are "heritage" that is "inherited" by their containers from
     *  a container of this object). Implementors may return an empty
     *  list, but should not return null. This method should return a
     *  complete list, including objects that have been locally
     *  modified (as indicated by isModifiedHeritage()).
     *  All objects in the returned list are required to be of the same
     *  class as the object on which this method is called (they should
     *  be clones constructed directly or indirectly).
     *  @return A list of objects of the same class as the object on
     *   which this is called.
     *  @see #isModifiedHeritage()
     *  @see #getShadowedHeritageList()
     */
    public List getHeritageList();

    /** Return a list of objects to which a change to this
     *  object should propagate, which is the list of objects that
     *  are created as a side effect of creating this one (that is, they
     *  are "heritage" that is "inherited" by their containers from
     *  a container of this object), and that have not been locally
     *  modified as indicated by isModifiedHeritage().  If an object
     *  has been locally modified, then its heritage is also not
     *  included in the list (it "shadows" changes to those objects).
     *  Implementors may return an empty list, but should not return null.
     *  All objects in the returned list are required to be of the same
     *  class as the object on which this method is called (they should
     *  be clones constructed directly or indirectly).
     *  @return A list of objects of the same class as the object on
     *   which this is called.
     *  @see #isModifiedHeritage()
     *  @see #getHeritageList()
     */
    public List getShadowedHeritageList();

    /** Return true if this object is an inherited object.  An object
     *  is inherited it is created in its container as a side effect
     *  of the creation of a similar object in some other container.
     *  For example, some container of this object may be an instance
     *  of Instantiable that was created by another instance of Instantiable,
     *  and this object was created during that instantiation.
     *  If this method returns true, then there is typically no need
     *  to export a description of this object to a persistent representation
     *  (such as MoML), unless this object has been modified (as indicated
     *  by isModifiedHeritage().  Moreover, if this method returns true,
     *  then it is reasonable to prohibit certain changes to this object,
     *  such as a name change or a change of container.  Such changes
     *  break the relationship with the object from which this inherits.
     *  @see Instantiable
     *  @see #isModifiedHeritage()
     *  @return True if the object is an inherited object.
     */
    public boolean isInherited();

    /** Return true if this object is an inherited object that has been
     *  modified locally.
     *  @return True if this object is an inherited object and it has
     *   been modified locally.
     *  @see #setModifiedHeritage(boolean)
     */
    public boolean isModifiedHeritage();

    /** Set whether this object is an inherited object.  If an object
     *  is an inherited object, then normally has no persistent representation
     *  when it is exported (unless it is changed) and cannot have its name
     *  or container changed.
     *  By default, instances of NamedObj are not inherited objects.
     *  If this method is called with a <i>false</i> argument, then
     *  it will call setInherited(false) on the container as
     *  well, making all containers above in the hierarchy not
     *  inherited objects.
     *  @param classElement True to mark this object as an inherited object.
     *  @see #isInherited()
     */
    public void setInherited(boolean classElement);

    /** Specify whether this object has been modified.  This has an
     *  effect only if setInherited() has been called with a true
     *  argument.  In that case, if this method is called with
     *  argument true, then this object will export MoML despite the
     *  fact that it is an inherited object.  I.e., call this with
     *  true to specify that this inherited object has been
     *  modified. To reverse the effect of this call, call it again
     *  with false argument.
     *  @param modified True to mark modified.
     *  @see #setInherited(boolean)
     *  @see #isModifiedHeritage()
     */
    public void setModifiedHeritage(boolean modified);
}
