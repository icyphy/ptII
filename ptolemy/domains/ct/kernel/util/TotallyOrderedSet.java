/* Totally ordered set

 Copyright (c) 1998-1999 The Regents of the University of California.
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
@ProposedRating Green (yourname@eecs.berkeley.edu)
@AcceptedRating Green (reviewmoderator@eecs.berkeley.edu)

*/

package ptolemy.domains.ct.kernel.util;

import collections.*;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// TotallyOrderedSet
/**
Totally ordered set, implemented using LinkedList. No repeated elements (
elements that the comparator returns 0).
@author  Jie Liu
@version $Id$
*/
public class TotallyOrderedSet {
    /** Construct the set with the given comparator. comparator is a blank
     *  final field.
     * @see Collections.Comparator
     * @param comparator The Comparator to compare elements.
     */
    public TotallyOrderedSet(Comparator comparator) {
        _comparator = comparator;
        _set = new LinkedList();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /** Return the index-th element. If the index is out of range
     *  a NoSuchElementException will be thrown.
     */
    public Object at(int index) {
        return _set.at(index);
    }

    /** Clear the set. Remove all the elements.
     */
    public void clear() {
        _set.clear();
    }

    /** Return true if the given element is contained in this set.
     *  This is done by sequentially compare the given elements with
     *  all the elements in this set, util the one that GREATER_THEN
     *  the given one is reached. If any comparison returns 0, then
     *  return true; else return false. If the type of given element
     *  is uncomparable by the comparator, then a ClassCastException
     *  will be thrown.
     *  @return True If the elements is contained according to the
     *        comparator.
     */
    public boolean contains(Object obj) {
        boolean result = false;
        Enumeration elements = _set.elements();
        while(elements.hasMoreElements()) {
            Object next = elements.nextElement();
            int com = _comparator.compare(obj, next);
            if(com == 0) {
                result = true;
                break;
            }
            if(com < 0) {
                break;
            }
        }
        return result;
    }

    /** Return an Enumeration of all the elements.
     *  @return The enumeration of all the elements.
     */
    public Enumeration elements() {
        return _set.elements();
    }

    /** Return the firstelement, ie. the <i>"smallest"</i> element.
     *  @return The smallest element.
     */
    public Object first() {
        return _set.first();
    }

    /** Return the index of the given object. Return -1 if the object
     *  is not in the set.
     */
    public int indexOf(Object obj) {
        return _set.firstIndexOf(obj);
    }

    /** Insert the given element, keeping the set sorted. If the set
     *  <i>contains</i> the given element, then do nothing.
     *  If the type of given element
     *  is uncomparable by the comparator, then a ClassCastException
     *  will be thrown.
     *  @param obj The element to be inserted.
     */
    public void insert(Object obj) {
        int count = 0;
        Enumeration elements = _set.elements();
        while(elements.hasMoreElements()) {
            Object next = elements.nextElement();
            int com = _comparator.compare(obj, next);
            if(com == 0) {
                return;
            }
            if(com < 0) {
                _set.insertAt(count, obj);
                return;
            }
            count ++;
        }
        _set.insertLast(obj);
    }

    /** return true if the set is empty
     *  @return True if the set is empty.
     */
    public boolean isEmpty() {
        return _set.isEmpty();
    }

    /** Remove the index-th element.
     *  @param index The index of the element.
     *  @exception NoSuchElementException If the speified index is
     *        out of range.
     */
    public void removeAt(int index) {
        _set.removeAt(index);
    }

    /** Remove the first element.
     *  @exception NoSuchElementException If the set is empty.
     */
    public void removeFirst() {
        _set.removeFirst();
    }

    /** Return the size of the set.
     *  @return The size of the set.
     */
    public int size() {
        return _set.size();
    }


    /** Return the firstelement, ie. the <i>"smallest"</i> element and
     *  remove it from the set.
     *  @return The smallest element.
     */
    public Object take() {
        Object temp = _set.first();
        _set.removeFirst();
        return temp;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Private variables should not have doc comments, they should
    // have regular C++ comments.
    private final Comparator _comparator;

    private LinkedList _set;
}
