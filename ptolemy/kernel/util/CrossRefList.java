/* List of cross-references.

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

@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Green (bart@eecs.berkeley.edu)
*/

package ptolemy.kernel.util;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.NoSuchElementException;

//////////////////////////////////////////////////////////////////////////
//// CrossRefList
/**

CrossRefList is a list that maintains pointers to other CrossRefLists.
This class is meant to be used to keep track of links between ports
and relations in Ptolemy II.  It is a highly specialized form of a list.
An instance has a container (an Object), which is immutable, meaning
that the container cannot be changed after the instance is constructed.
CrossRefList enumerators and query methods do not return references to
other CrossRefLists; instead, references to the containers of the CrossRefLists
(which can be any Object) are returned.
<p>
For efficiency, CrossRefList maintains a list of pairwise links
between Objects (CrossRefs). That is, each member of a set of objects
has a list of references to other members of the set, and each
reference has a similar list that contains a corresponding back
reference. Each reference list is an instance of this class. The class
is used as if it were a simple list of references to objects, but it
ensures the symmetry of the references, and supports efficient removal
of links.  Removing a reference in one list automatically updates N
back-references in O(N) time, independent of the sizes of the
cross-referenced lists.
<p>
It is possible to create links at specified points in the list (called
the <i>index number</i>).  This may result in gaps in the list at
lower index numbers.  Gaps are representing by null in an enumeration
of the list.

@author Geroncio Galicia, Contributor: Edward A. Lee
@version $Id$
@since Ptolemy II 0.2
*/
public final class CrossRefList implements Serializable  {

    /** Construct a list with the specified container.
     *  The container is immutable (it cannot be changed).
     *  If the argument is null, then a null pointer exception will result
     *  (eventually).
     *  @param container The container of the object to be constructed.
     */
    public CrossRefList(Object container) {
        _container = container;
    }

    /** Create a new CrossRefList that is linked to the same
     *  CrossRefLists as the original CrossRefList except that this
     *  new one has a new container.  This method synchronizes on the
     *  original list.  Note that this behaves like a copy constructor,
     *  except that the remote list is affected so that it will now point
     *  back to both lists.  If either argument is null, then a null
     *  pointer exception will be thrown.
     *  @param container The container of the object to be constructed.
     *  @param originalList The model to copy.
     */
    public CrossRefList(Object container, CrossRefList originalList) {
        this(container);
        synchronized(originalList) {
            if (originalList.size() == 0) return; // List to copy is empty.
            for (CrossRef p = originalList._headNode; p != null; p = p._next) {
                if (p._far._nearList() != null) {
                    try {
                        link(p._far._nearList());
                    } catch (IllegalActionException ex) {
                        // This should not be thrown.
                        throw new InternalErrorException(null, ex, null);
                    }
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the first container linked to this list, or
     *  null if the list is empty.  Note that we may also return null
     *  if the list is not empty, but the first link happens to not point
     *  anywhere.  Thus, do not use this method to check to see whether
     *  the list is empty.
     *  Time complexity: O(1).
     *  @return The first entry, or null if there is none.
     */
    public synchronized Object first() {
        if (_headNode != null) {
            return _headNode._farContainer();
        } else {
            return null;
        }
    }

    /** Get the container at the specified index.  If there is no such
     *  container, return null. Indices begin with 0.
     *  @param index The index of the container to return.
     *  @return The container at the specified index.
     */
    public synchronized Object get(int index) {
        if (index < 0 || index >= _size) return null;
        int count = 0;
        CrossRef result = _headNode;
        while (result != null && count++ < index) {
            result = result._next;
        }
        if (result != null) {
            return result._farContainer();
        } else {
            return null;
        }
    }

    /** Enumerate the containers linked to this list.  The
     *  enumeration returns the container object itself and not the
     *  CrossRefList instance in this list that the object owns or
     *  contains.  Note that an object may be enumerated more than
     *  once if more than one link to it has been established.
     *  Also, there may be elements in the list that are null,
     *  indicating that nothing is linked at that index position.
     *  Time complexity: O(1).
     *  NOTE: This method is not well named, since it returns the
     *  containers of other instances of CrossRefList that are linked
     *  to this one, and not the container of this one.
     *  @return An enumeration of remote referenced objects.
     */
    public synchronized Enumeration getContainers() {
        return new CrossRefEnumeration();
    }

    /** Insert a link to the specified CrossRefList (<i>farList</i>) at
     *  the specified position (<i>index</i>).  The index can be any
     *  non-negative integer.  If the index is greater than or equal to the
     *  size of this list, then null links are created to make the list
     *  big enough.  If there are already links with indices equal to or
     *  larger than <i>index</i>, then their indices become one larger.
     *  This method creates a back reference in the far list, unless the
     *  <i>farList</i> argument is null.  That back reference is appended
     *  to the end of the far list.
     *  <p>
     *  Note that this method specifies the index on the local reference
     *  list, but not on the remote reference list.  There is no mechanism
     *  provided to specify both indices.  This is because this class is
     *  used to linked ports to relations, and indices of links have
     *  no meaning in relations.  Thus, there is no need for a richer
     *  interface.
     *  <p>
     *  Note that this method does not synchronize on the remote object.
     *  Thus, this method should be called within a write-synchronization of
     *  the common workspace.
     *  @param index The position in the list at which to insert this link.
     *  @param farList The cross reference list contained by the remote object.
     *  @exception IllegalActionException If this list tries linking to
     *   itself.
     */
    public synchronized void insertLink(int index, CrossRefList farList)
            throws IllegalActionException {
        if (farList == this) {
            throw new IllegalActionException(
                    "CrossRefLink.link: Illegal self-link.");
        }
        ++_listVersion;
        CrossRef localCrossRef = new CrossRef(index);
        if (farList != null) {
            localCrossRef._far = farList.new CrossRef(localCrossRef);
        }
    }

    /** Return true if the specified container is linked to this
     *  list.  If no container is passed or if this CrossRefList has
     *  no links then just return.
     *  Time complexity: O(n).
     *  @param obj An object that might be referenced.
     *  @return A boolean indicating whether the object is referenced.
     */
    public synchronized boolean isLinked(Object obj) {
        if (obj == null || _size == 0) return false;
        for (CrossRef p = _headNode; p != null; p = p._next) {
            Object far = p._farContainer();
            if (far != null && far.equals(obj)) {
                return true;
            }
        }
        return false;
    }

    /** Link this list to a different CrossRefList (<i>farList</i>).
     *  The link is appended at the end of the list.
     *  This action additionally creates a back-reference in the far list,
     *  also at the end, unless the <i>farList</i> argument is null, in
     *  which case the new link is a null link.
     *  Redundant entries are allowed; that is, you can link more than
     *  once to the same remote list.
     *  Note that this method does not synchronize on the remote object.
     *  Thus, this method should be called within a write-synchronization of
     *  the common workspace.
     *  Time complexity: O(1).
     *  @param farList The cross reference list contained by the remote object.
     *  @exception IllegalActionException If this list tries linking to
     *  itself.
     */
    public synchronized void link(CrossRefList farList)
            throws IllegalActionException {
        if (farList == this) {
            throw new IllegalActionException(
                    "CrossRefLink.link: Illegal self-link.");
        }
        ++_listVersion;
        CrossRef localCrossRef = new CrossRef();
        if (farList != null) {
            localCrossRef._far = farList.new CrossRef(localCrossRef);
        }
    }

    /** Return size of this list.
     *  Time complexity: O(1).
     *  @return A non-negative integer.
     */
    public synchronized int size() {
        return _size;
    }

    /** Delete the link at the specified index.  If there is no such
     *  link, ignore.  Back references are likewise updated.
     *  Note that the index numbers of any links at higher indices
     *  will decrease by one.
     *  Time complexity: O(n).
     *  @param index The index of the link to delete.
     */
    public synchronized void unlink(int index) {
        int count = 0;
        CrossRef toDelete = _headNode;
        while (toDelete != null && count++ < index) {
            toDelete = toDelete._next;
        }
        if (toDelete != null) {
            toDelete._dissociate();
        }
    }

    /** Delete all links to the specified container.  If there is no such
     *  link, ignore.  Back references are likewise updated.
     *  In the case of redundant links this deletes the first link
     *  to that container.
     *  Time complexity: O(n).
     *  @param obj The object to delete.
     */
    public synchronized void unlink(Object obj) {
        if (obj == null || _size == 0) return;
        CrossRef p = _headNode;
        while (p != null) {
            CrossRef n = p._next;
            Object far = p._farContainer();
            if (far != null && far.equals(obj)) {
                p._dissociate();
            }
            p = n;
        }
    }

    /** Delete all cross references.
     *  Time complexity: O(n).
     */
    public synchronized void unlinkAll() {
        if (_size == 0) return;

        CrossRef p = _headNode;
        while (p != null) {
            CrossRef n = p._next;
            p._dissociate();
            p = n;
        }

        // Mark the list empty.
        _headNode = null;
        _lastNode = null;
        _size = 0;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** @serial Version number is incremented each time the list is modified.
     * This is used to make sure that elements accessed via an enumeration
     * are valid.  This is inspired by a similar mechanism in Doug Lea's
     * Java Collections.
     */
    private long _listVersion = 0;

    /** @serial The code ensures that if this is non-zero, then _headNode
     *  is non-null.
     */
    private int _size = 0;

    /** @serial Head Node */
    private CrossRef _headNode;

    /** @serial Last Node */
    private CrossRef _lastNode;

    /** @serial NOTE: In jdk 1.2 or higher, this could be made final to
     *  prohibit what is called "reference reseating" (not resetting),
     *  i.e. to make the variable immutable.
     */
    private Object _container;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /**
     * Objects of this type form the elements of the list.
     * They occur in pairs, one in each list at each end of a link.
     * A CrossRef is similar to a "link" in a doubly-linked list.
     * (Note that the usage of "link" here is different from "link"
     * in the comments for the above public methods.  "Link" here
     * refers to an element of a doubly-linked list.)
     */
    protected class CrossRef implements Serializable{

        /** The far CrossRef. */ 
        protected CrossRef _far;

        private CrossRef _next;

        private CrossRef _previous;

        private CrossRef() { this(null); }

        private CrossRef(CrossRef spouse) {
            _far = spouse;
            if (_size > 0) {
                _previous = _lastNode;
                _lastNode._next = this;
                _lastNode = this;
            } else {
                // List is empty.
                _lastNode = this;
                _headNode = this;
            }
            ++_size;
        }

        // Insert an empty link at the specified index number, which
        // can be any non-negative integer.
        // This may result in empty links being created at lower
        // index numbers, if there are not already links present.
        private CrossRef(int index) {
            if (index == 0) {
                if (_size == 0) {
                    // list is empty
                    _lastNode = this;
                    _headNode = this;
                } else {
                    _next = _headNode;
                    _headNode._previous = this;
                    _headNode = this;
                }
            } else {
                // Index is not the first.

                // Chain down the list, creating empty links if necessary.
                // First chaining step is special, setting "previous" to
                // get us started.
                CrossRef previous = _headNode;
                if (previous == null && index > 0) {
                    // List is empty.
                    previous = new CrossRef();
                    _headNode = previous;
                    _lastNode = previous;
                }
                int ind = 1;
                while (ind++ < index) {
                    // previous cannot possibly be null here.
                    if (previous._next == null) {
                        // We are off the end of the list.
                        CrossRef newCrossRef = new CrossRef();
                        previous._next = newCrossRef;
                        newCrossRef._previous = previous;
                        _lastNode = newCrossRef;
                        previous = newCrossRef;
                    } else {
                        // There is an entry in the list.
                        previous = previous._next;
                    }
                }
                // Now we are assured that there are at least index-1 entries
                // in the list.
                if (previous != null) {
                    // There is at least one entry.
                    // If the new node is the last in the list, update the
                    // list's pointer to the last node.
                    if (_lastNode == previous) _lastNode = this;
                    _next = previous._next;
                    previous._next = this;
                    _previous = previous;
                } else {
                    // List is empty.
                    _lastNode = this;
                    _headNode = this;
                }
            }
            ++_size;
        }

        private synchronized Object _nearContainer() {
            return _container;
        }

        private synchronized Object _farContainer() {
            if (_far != null) return _far._nearContainer();
            else return null;
        }

        private synchronized CrossRefList _nearList() {
            return CrossRefList.this;
        }

        private synchronized void _dissociate() {
            _unlink(); // Remove this.
            if (_far != null) _far._unlink(); // Remove far
        }

        private synchronized void _unlink() {
            ++_listVersion;
            // Removes this from enclosing CrossRefList.
            if (_next != null)
                _next._previous = _previous; // Modify next.
            else
                _lastNode = _previous;
            if (_previous != null)
                _previous._next = _next; // Modify previous.
            else
                _headNode = _next;
            _size--; // Modify list.
        }
    }


    /** Enumerate the objects pointed to by the list.
     *  @see CrossRefList
     */
    private class CrossRefEnumeration implements Enumeration {

        public CrossRefEnumeration() {
            _enumeratorVersion = _listVersion;
            _ref = _headNode;
        }

        /** Return true if there are more elements to enumerate. */
        public boolean hasMoreElements() {
            if (_enumeratorVersion != _listVersion) {
                throw new InvalidStateException(
                        "CrossRefList.hasMoreElements(): "
                        + "The list has been modified.");
            }

            if (_ref == null)
                return false;
            else
                return true;
        }

        /** Return the next element in the enumeration.
         *  @exception java.util.NoSuchElementException If the enumeration is
         *  exhausted.
         */
        public Object nextElement()
                throws NoSuchElementException {
            if (_enumeratorVersion != _listVersion) {
                throw new InvalidStateException(
                        "CrossRefList.nextElement(): "
                        + "The list has been modified.");
            }

            if (_ref == null) {
                throw new NoSuchElementException("exhausted enumeration");
            } else {
                CrossRef p = _ref;
                Object v = _ref._farContainer();
                _ref = p._next;
                return v;
            }
        }

        private long _enumeratorVersion;

        private CrossRef _ref;
    }
}
