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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
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
*/

public interface Derivable extends Nameable {

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
    public List getDerivedList();

    /** Return a list of objects derived from this one that are
     *  not overridden. An object is overridden if either its
     *  getOverrideDepth() method returns 0, or if it is shadowed
     *  by an object along the path from this object to it.
     *  This shadowing is somewhat subtle, and requires some
     *  notation to explain precisely.
     *  <p>
     *  A path from this object to a derived object is a sequence
     *  of objects <i>x</i><sub>1</sub>, ..., <i>x</i><sub>n</sub>,
     *  where <i>x</i><sub>1</sub> is this object and <i>x</i><sub>n</sub>
     *  is the derived object.  For each pair <i>x</i><sub>i</sub>
     *  and <i>x</i><sub>i+1</sub>, there is a pair of (deep) containers
     *  <i>y</i><sub>i</sub> and <i>y'</i><sub>i+1</sub> above them
     *  (respectively) in the hierarchy such that <i>y</i><sub>i</sub>
     *  is a <i>parent</i> (not container)
     *  of <i>y'</i><sub>i+1</sub>. Let <i>m</i><sub>i</sub>
     *  be the depth in the hierarchy between <i>y</i><sub>i</sub>
     *  and <i>x</i><sub>i</sub>.  That is, <i>m</i><sub>i</sub> = 1
     *  if <i>y</i><sub>i</sub> is the immediate container of
     *  <i>x</i><sub>i</sub>, and it is 2 if there is a container
     *  in between, etc.  Then derivation chain is such that
     *  <i>y'</i><sub>i+1</sub> is <i>m</i><sub>i</sub> levels
     *  above <i>x</i><sub>i+1</sub> in the containment hierarchy.
     *  That is, for each neighboring object in the derivation chain,
     *  they each have a container the same depth above them in the
     *  hierarchy where one container is the parent of the other.
     *  A derivation chain is shadowed if for any such pair,
     *  <i>x</i><sub>i</sub>, <i>x</i><sub>i+1</sub>, either
     *  <i>x</i><sub>i+1</sub>.getOverrideDepth() returns 0
     *  or it returns a number greater than <i>m</i><sub>i</sub>.
     *  <p>
     *  Note that between any two objects, there are zero or more
     *  derivation chains between them. That is, the derivation
     *  chain is not unique.  We define preferred derivation chain
     *  to be the one found by a depth-first search.
     *  <p>
     *  Intuitively, this means that shadowing will occur if
     *  a change has been previously propagated higher in the
     *  hierarchy than the change being considered now.
     *  <p>
     *  Implementors may return an empty list, but should not return null.
     *  All objects in the returned list are required to be of the same
     *  class as the object on which this method is called (they should
     *  be clones constructed directly or indirectly).
     *  <p>
     *  If a non-null argument is given, then the specified list
     *  will be populated with the integers <i>m</i><sub>i</sub>
     *  in the derivation chain. Note that the length of this list
     *  is the same as the length of the returned list, and that
     *  the returned list does not include the first object in
     *  the derivation chain (this object).
     *  @param depthList If non-null, then the specified list will
     *   be populated with instances of java.lang.Integer representing
     *   the depths of propagation along the derivation chain.
     *  @return A list of objects of the same class as the object on
     *   which this is called.
     *  @see #isModifiedHeritage()
     *  @see #getHeritageList()
     */
    public List getShadowedDerivedList(List depthList);

    /** Return true if this object is a derived object.  An object
     *  is derived if it is created in its container as a side effect
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
    public boolean isDerived();

    /** Return -1 if this is not an inherited object, 0 if this
     *  is an inherited object that has been modified locally, and
     *  a depth greater than zero if this inherited object has
     *  been modified by propagation at the returned depth above
     *  this object in the containment hierarchy.
     *  @return An integer indicating whether this object has been
     *   modified and how.
     *  @see #setOverrideDepth(int)
     */
    public int getOverrideDepth();

    /** Set whether this object is an inherited object.  If an object
     *  is an inherited object, then normally has no persistent representation
     *  when it is exported (unless it is changed) and cannot have its name
     *  or container changed.
     *  By default, instances of NamedObj are not inherited objects.
     *  If this method is called with a <i>false</i> argument, then
     *  it will call setDerived(false) on the container as
     *  well, making all containers above in the hierarchy not
     *  inherited objects.
     *  @param isDerived True to mark this object as an inherited object.
     *  @see #isInherited()
     */
    public void setDerived(boolean isDerived);

    /** Specify whether this object overrides its inherited value,
     *  and if so, at what depth above in the hiearchy the parent
     *  relationship that triggered this override occurred.  This has an
     *  effect only if setDerived() has been called with a true
     *  argument.  In that case, if this method is called with
     *  argument 0, then this object will export MoML despite the
     *  fact that it is a derived object.  I.e., call this with
     *  0 to specify that this inherited object has been
     *  modified directly. To reverse the effect of this call, call it again
     *  with -1 argument.
     *  @param depth The depth above this object in the hierarchy at
     *   which the propagation occurred, or 0 to indicate that the
     *   override is direct (not propagated), or -1 to indicate that
     *   the object is not overridden.
     *  @see #setDerived(boolean)
     *  @see #getOverrideDepth()
     */
    public void setOverrideDepth(int depth);
}
