/* List of cross-references.

 Copyright (c) 1997- The Regents of the University of California.
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

import collections.CorruptedEnumerationException;

import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.io.Serializable;

//////////////////////////////////////////////////////////////////////////
//// CrossRefList
/**
Maintain a list of pairwise links between Objects (cross
references). That is, each member of a set of objects has a list of
references to other members of the set, and each reference has
similar list that contains a corresponding back reference. Each
reference list is an instance of this class. The class is used as if
it were a simple list of references to objects, but it ensures the
symmetry of the references, and supports efficient removal of links.
Removing a reference in one list automatically updates N
back-references in O(N) time, independent of the
sizes of the cross-reference lists.

@author Geroncio Galicia
@contributor Edward A. Lee
@version $Id$
*/
public final class CrossRefList implements Serializable  {

    // FIXME: add "final" modifiers noted below when JDK 1.2 is released.

    /** Constructor requires a non-null container.
     *  @param container
     *  @exception IllegalActionException Argument is null.
     */
    public CrossRefList(Object container) 
            throws IllegalActionException {
        if (container == null) {
            throw new IllegalActionException(
                    "Attempt to create CrossRefList with a null container.");
        }
        // This should be initializing a blank final.
        _container = container; 
        _listVersion = 0;
        _size = 0;
        _headNode = null;
        _lastNode = null;
    }
    
    /** Copy constructor. 
     *  Create a new list that duplicates an original list except that it
     *  has a new container.
     *  @exception IllegalActionException Second argument is null.
     */
    public CrossRefList(Object container, CrossRefList originalList)
            throws IllegalActionException {
        this(container);
        _duplicate(originalList);
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////
    
    /** Return the first object referenced by this list, or null if the
     *  list is empty.
     *  Time complexity: O(1).
     */
    public synchronized Object first() {
        return _headNode._farContainer();
    }
    
    /** Enumerate the objects referenced by this list.
     *  Time complexity: O(1).
     */
    public synchronized Enumeration getLinks() { 
        return new CrossRefEnumeration();
    }
    
    /** Return true if the specified object is referenced on this list. 
     *  Time complexity: O(n).
     */
    public synchronized boolean isLinked(Object obj) {
        if (obj == null || _size == 0) return false;
        for(CrossRef p = _headNode; p != null; p = p._next) {
            if (p._farContainer().equals(obj)) {
                return true;
            }
        }
        return false;
    }
    
    /** Link to another CrossRefList.
     *  Instantiate a new CrossRef in the specified (far) list and link
     *  that to the the new one here.  Redundant links are allowed.
     *  Time complexity: O(1).
     */
    public synchronized void link(CrossRefList farList) 
            throws IllegalActionException {
        if(farList == this)
            throw new IllegalActionException("illegal link-back");

        // FIXME: Technically, this will not always prevent deadlock, since
        // grabbing the lock on "this" is not atomic with grabbing the lock
        // on the modelToCopy.  This method should instead grab a lock on a
        // common container.
        synchronized(farList) {
            ++_listVersion;
            CrossRef localCrossRef = new CrossRef();
            // FIXME: put below line in initializer and
            // make _far a "blank final"
            localCrossRef._far = farList.new CrossRef(localCrossRef);
        }
    }
    
    /** Return size of this list. 
     *  Time complexity: O(1).
     */
    public synchronized int size() {
        return _size;
    }

    /** Delete a link to the specified object. 
     *  Time complexity: O(n).
     */
    public synchronized void unlink(Object obj) {
        if (obj == null || _size == 0) return;
        ++_listVersion;
        Object v;
        CrossRef p = _headNode;
        while(p != null) {
            CrossRef n = p._next;
            // If associations are always created using link then
            // p._farContainer() will never return null.
            if (p._farContainer().equals(obj)) {
                p._dissociate();
                return;
            }
            p = n;
        }
    }
    
    /** Delete all cross references.
     *  Time complexity: O(n).
     */
    public synchronized void unlinkAll() {
        if(_size == 0) return;
        ++_listVersion;
        
        CrossRef deadHead = _headNode; // _deadHead is marked for deletion.
        
        // As long as there's a next element.
        while( (_headNode = deadHead._next) != null) {
            deadHead._dissociate();     // Delete old head and its partner.
            deadHead = _headNode;
        }
        // Delete the last CrossRef.
        deadHead._dissociate(); 
        
        // Mark the list empty.
        _headNode = null;
        _lastNode = null;
        _size = 0;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         private methods                          ////

    /** Duplicate the contents of another list, creating back references in
     *  the remote lists to this one.
     *  Time complexity: O(n).
     */
    private synchronized void _duplicate(CrossRefList modelToCopy) 
            throws IllegalActionException {
        // FIXME: Technically, this will not always prevent deadlock, since
        // grabbing the lock on "this" is not atomic with grabbing the lock
        // on the modelToCopy.  This method should instead grab a lock on a
        // common container.
        synchronized(modelToCopy) {
            if(modelToCopy.size() == 0) return; // List to copy is empty.
            for(CrossRef p = modelToCopy._headNode; p != null; p = p._next) {
                if(p._far != null) {
                    if(p._far._nearList() != null) {
                        link(p._far._nearList());
                    }
                }
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////
    
    // Version number is incremented each time the list is modified.
    // This is used to make sure that elements accessed via an enumeration
    // are valid.  This is inspired by a similar mechanism in Doug Lea's
    // Java Collections.
    private int _listVersion;
    
    private int _size;
    
    private CrossRef _headNode;
    
    private CrossRef _lastNode;
    
    // FIXME: make final to prohibit what is called "reference
    // reseating" (not resetting).
    private Object _container;
    
    //////////////////////////////////////////////////////////////////////////
    ////                         inner classes                            ////
    
    // Class CrossRef.
    // Objects of this type form the elements of the list.
    // They occur in pairs, one in each list at each end of a link.
    private class CrossRef implements Serializable{
        
        protected CrossRef _far;
        
        private CrossRef _next;
        
        private CrossRef _previous;
        
        private CrossRef() { this(null); }
        
        private CrossRef(CrossRef spouse) {
            _far = spouse;
            if(_size > 0) {
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
        
        private synchronized Object _nearContainer() {
            return _container;
        }
        
        private synchronized Object _farContainer() {
            // Returning null shouldn't happen.
            return _far != null ? _far._nearContainer() : null; 
        }
        
        private synchronized CrossRefList _nearList() {
            return CrossRefList.this;
        }
        
        private synchronized void _dissociate() {
            _unlink(); // Remove *this.
            if(_far != null) _far._unlink(); // Remove far 
        }
        
        private synchronized void _unlink() {
            ++_listVersion;
            // Removes *this from enclosing CrossRefList.
            if(_next != null) _next._previous = _previous; // Modify next.
            else _lastNode = _previous;
            if(_previous != null) _previous._next = _next; // Modify previous.
            else _headNode = _next;
            _size--; // Modify list.
        }
    }


    /** Enumerate the objects pointed to by the list.
     *  @see CrossRefList
     */
    private class CrossRefEnumeration implements Enumeration {
        
        public CrossRefEnumeration() {
            _enumeratorVersion = _listVersion;
            _startAtHead = true;
            _ref = null;
        }
        
        /** Return true if there are more elements to enumerate. */
        public boolean hasMoreElements() {
            if(_enumeratorVersion != _listVersion) {
                throw new CorruptedEnumerationException();
            }
            CrossRef tmp1 = _ref; // Callee-save.
            boolean tmp2 = _startAtHead; // Callee-save.
            Object tmpObj;
            try {
                tmpObj = nextElement();
            } catch (NoSuchElementException ex) {
                tmpObj = null;
            }
            _ref = tmp1; // Restore state.
            _startAtHead = tmp2; // Restore state.
            return tmpObj != null;
        }
        
        /** Return the next element in the enumeration. 
         *  @exception java.util.NoSuchElementException Exhausted enumeration.  
         */
        public Object nextElement()
                throws NoSuchElementException {
            if(_enumeratorVersion != _listVersion) {
                throw new CorruptedEnumerationException();
            }
            if(_startAtHead) {
                // Starting at beginning of list.
                _startAtHead = false;
                if(_headNode != null) {
                    // List not empty.
                    _ref = _headNode;
                    return _ref._farContainer();
                } else {
                    // List is empty, throw exception.
                    throw new NoSuchElementException("exhausted enumeration");
                }
            } else {
                // Not at beginning of list.
                if (_ref != _lastNode) {
                    // Not at end of list.
                    if (_ref != null) { 
                        // If pointer to element not NULL, return next.
                        _ref = _ref._next;
                        return _ref._farContainer();
                    } else {
                        // If pointer is NULL, then end of list was
                        // already passed.  Throw exception.
                        throw new NoSuchElementException(
                                "exhausted enumeration");
                    }
                } else {
                    // At end of list.
                    throw new NoSuchElementException("exhausted enumeration");
                }
            }
        }
        
        private int _enumeratorVersion;
        
        private CrossRef _ref;
        
        private boolean _startAtHead;
    }
}
