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
*/

package pt.kernel;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// CrossRefList
/** 
Description of the class
@author Geroncio Galicia
@version $Id$
*/
public final class CrossRefList {

    // FIXME: add "final" modifiers noted below when JDK 1.2 is released.

    /** Constructor
     * CrossRefList requires owner to prevent null pointer accesses below.
     */	
    public CrossRefList(Object owner) {
        _nearObj = owner; // This would've been initializing a blank final.
        _dimen = 0;
        _headNode = null;
        _lastNode = null;
    }

    /** Copy constructor. */
    public CrossRefList(Object owner, CrossRefList originalList) {
        this(owner);
        copyList(originalList);
    }



    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Link to an CrossRefList.
     * Instantiate a new CrossRefList in the specified (far) list and
     * link that to the the new one here.  Redundant links are allowed.
     */
    public synchronized void associate(CrossRefList farList) {
        synchronized(farList) {
            CrossRef localCrossRef = new CrossRef();
            // FIXME: put below line in initializer and make _far a "blank final"
            localCrossRef._far = farList.new CrossRef(localCrossRef);
        }
    }

    /** Delete all CrossRefs. */
    public synchronized void dissociate() {
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

    /** Delete a CrossRef indexed by far Object. */
    public synchronized void dissociate(Object element) {
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

    /** Check if a far Object is referenced. */
    public synchronized boolean isMember(Object element) {
        if (element == null || _dimen == 0) return false;
        for(CrossRef p = _headNode; p != null; p = p._next) {
            if (p._farOwner().equals(element)) {
                return true;
            }
        }
        return false;
    }

    /** Check if list is empty. */
    public synchronized boolean isEmpty() {
        return _headNode == null;
    }

    /** Return enumeration for this list. */
    public synchronized Enumeration enumerate() { 
        return new CrossRefEnumeration();
    }

    /** Return size of this list. */
    public synchronized int size() { return _dimen; }

    /** Make an independent copy.  Does not clone elements. */
    public synchronized void copyList(CrossRefList originalList) {
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
            _startAtHead = true;
            _ref = null;
        }

        /** Check if there are remaining elements to enumerate. */
        public boolean hasMoreElements() {
            CrossRef tmp1 = _ref; // Callee-save.
            boolean tmp2 = _startAtHead; // Callee-save.
            Object testObj = nextElement();
            _ref = tmp1; // Restore state.
            _startAtHead = tmp2; // Restore state.
            return testObj != null;
        }

        /** Return the next element in the enumeration. */
        public Object nextElement() {
            if(_startAtHead) { // If starting at beginning of list.
                _startAtHead = false;
                if(_headNode != null) { // List not empty.
                    _ref = _headNode;
                    return _ref._farOwner();
                } else { // List is empty, pass NULL.
                    _ref = null;
                    return _ref;
                }
            } else { // If not at beginning of list.
                if (_ref != _lastNode) { // If not at end of list.
                    if (_ref != null) { // If pointer to element not NULL, return next.
                        _ref = _ref._next;
                        return _ref._farOwner();
                    } else { // If pointer is NULL, then end of list was already passed.
                        _ref = null;
                        return _ref;
                    }
                } else { // If at end of list.
                    _ref = null;
                    return _ref;
                }
            }
        }
    
        private CrossRef _ref;
    
        private boolean _startAtHead;
    
    }
  
}
