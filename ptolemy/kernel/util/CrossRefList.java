/* List of cross-references.

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

@ProposedRating Red (galicia@eecs.berkeley.edu)

*/

package pt.kernel;

import java.util.Enumeration;
import java.util.NoSuchElementException;
import collections.CorruptedEnumerationException;

//////////////////////////////////////////////////////////////////////////
//// CrossRefList
/** Lists for implementing links between Objects.  
If you need an Object to keep a list of references to other Objects
(i.e. neighbors), and those other Objects need to maintain
back-references, then use CrossRefList.  This list requires an owning
Object in the constructor argument and access methods return or refer
to neighboring Objects directly.  Removing a reference automatically
updates back-references in N neighboring Objects in O(N) time.
@author Geroncio Galicia
@version $Id$ */
public final class CrossRefList {

    // FIXME: add "final" modifiers noted below when JDK 1.2 is released.

    /** 
     * CrossRefList requires owner to prevent null pointer accesses below.
     */	
    public CrossRefList(Object owner) {
        _nearObj = owner; // This would've been initializing a blank final.
        _listVersion = 0;
        _dimen = 0;
        _headNode = null;
        _lastNode = null;
    }

    /** Copy constructor. 
     * Creates a copy of this list with a new owner.
     */
    public CrossRefList(Object owner, CrossRefList originalList) {
        this(owner);
        duplicate(originalList);
    }



    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Link to an CrossRefList.
     * Instantiate a new CrossRef in the specified (far) list and link
     * that to the the new one here.  Redundant links are allowed.
     * Time complexity: O(1).
     */
    public synchronized void associate(CrossRefList farList) {
        synchronized(farList) {
            ++_listVersion;
            CrossRef localCrossRef = new CrossRef();
            // FIXME: put below line in initializer and
            // make _far a "blank final"
            localCrossRef._far = farList.new CrossRef(localCrossRef);
        }
    }

    /** Delete all CrossRefs.
     * Time complexity: O(n).
     */
    public synchronized void dissociate() {
        ++_listVersion;
        if(isEmpty()) return; // List is already empty.

        CrossRef deadHead = _headNode; // _deadHead is marked for deletion.

        // As long as there's a next element.
        while( (_headNode = deadHead._next) != null) { // Increment _headNode.
            deadHead._dissociate();     // Delete old head and its partner.
            deadHead = _headNode;
        }
        // Delete the last CrossRef.
        deadHead._dissociate(); 

        // Mark the list empty.
        _headNode = null;
        _lastNode = null;
        _dimen = 0;
    }

    /** Delete a CrossRef indexed by a neighboring Object. 
     * Time complexity: O(n).
     */
    public synchronized void dissociate(Object element) {
        ++_listVersion;
        if (element == null || _dimen == 0) return;
        Object v;
        CrossRef p = _headNode;
        while(p != null) {
            CrossRef n = p._next;
            // If associations are always created using associate then
            // p._farOwner() will never return null.
            if (p._farOwner().equals(element)) {
                p._dissociate();
                return;
            }
            p = n;
        }
    }

    /** Check if a neighboring Object is referenced. 
     * Time complexity: O(n).
     */
    public synchronized boolean isMember(Object element) {
        if (element == null || _dimen == 0) return false;
        for(CrossRef p = _headNode; p != null; p = p._next) {
            if (p._farOwner().equals(element)) {
                return true;
            }
        }
        return false;
    }

    /** Check if list is empty. 
     * Time complexity: O(1).
     */
    public synchronized boolean isEmpty() {
        return _headNode == null;
    }

    /** Return enumeration for this list. 
     * Enumeration returns neighboring Objects.
     * Time complexity: O(1).
     */
    public synchronized Enumeration elements() { 
        return new CrossRefEnumeration();
    }

    /** Return size of this list. 
     * Time complexity: O(1).
     */
    public synchronized int size() { return _dimen; }

    /** Make an independent copy.  Does not clone elements. 
     * This methods assume that the new list already has an owning Object.
     * Time complexity: O(n).
     */
    public synchronized void duplicate(CrossRefList originalList) {
        synchronized(originalList) {
            if(originalList.isEmpty()) return; // List to copy is empty.
            for(CrossRef p = originalList._headNode; p != null; p = p._next) {
                if(p._far != null) {
                    if(p._far._nearList() != null) {
                        associate(p._far._nearList());
                    }
                }
            }
        }
    }



    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    private int _listVersion;

    private int _dimen;

    private CrossRef _headNode;

    private CrossRef _lastNode;

    // FIXME: make final to prohibit reseating
    private Object _nearObj;



    //////////////////////////////////////////////////////////////////////////
    ////                         inner classes                            ////

    // Class CrossRef
    private class CrossRef {

        protected CrossRef _far;

        private CrossRef _next;
    
        private CrossRef _previous;

        private CrossRef() { this(null); }

        private CrossRef(CrossRef spouse) {
            _far = spouse;
            if(_dimen > 0) {  // List isn't empty.
                _next = _headNode;
                _headNode._previous = this;
                _headNode = this;
            } else {  // List is empty.
                _lastNode = _headNode = this;
            }
            ++_dimen;
        }

        private synchronized Object _nearOwner() {
            return _nearObj;
        }

        private synchronized Object _farOwner() {
            // Returning null shouldn't happen.
            return _far != null ? _far._nearOwner() : null; 
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
            _dimen--; // Modify list.
        }
    }



    // Class CrossRefEnumeration
    /** 
        Enumerator for CrossRefList.
        @see CrossRefList
    */
    private class CrossRefEnumeration implements Enumeration {
    
        public CrossRefEnumeration() {
            _enumeratorVersion = _listVersion;
            _startAtHead = true;
            _ref = null;
        }

        /** Check if there are remaining elements to enumerate. */
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

        /** Return the next element in the enumeration. */
        public Object nextElement() throws NoSuchElementException {
            if(_enumeratorVersion != _listVersion) {
                throw new CorruptedEnumerationException();
            }
            if(_startAtHead) { // If starting at beginning of list.
                _startAtHead = false;
                if(_headNode != null) { // List not empty.
                    _ref = _headNode;
                    return _ref._farOwner();
                } else { // List is empty, throw exception.
                    throw new NoSuchElementException("exhausted enumeration");
                }
            } else { // If not at beginning of list.
                if (_ref != _lastNode) { // If not at end of list.
                    if (_ref != null) { 
                        // If pointer to element not NULL, return next.
                        _ref = _ref._next;
                        return _ref._farOwner();
                    } else {
                        // If pointer is NULL, then end of list was
                        // already passed.  Throw exception.
                        throw new NoSuchElementException("exhausted enumeration");
                    }
                } else { // If at end of list.
                    throw new NoSuchElementException("exhausted enumeration");
                }
            }
        }

        private int _enumeratorVersion;
    
        private CrossRef _ref;
    
        private boolean _startAtHead;
    
    }
  
}
