/* Totally ordered set

 Copyright (c) 1998-2003 The Regents of the University of California.
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
@ProposedRating Yellow (liuj@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)

*/

package ptolemy.domains.ct.kernel.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// TotallyOrderedSet
/**
Totally ordered set with no repeated elements.

A repeated element is an element that the comparator returns 0 for.

@author  Jie Liu
@version $Id$
@since Ptolemy II 0.2
*/
public class TotallyOrderedSet {
    /** Construct the set with the given comparator. comparator is a blank
     *  final field that can't be changed after creation.
     * @see java.util.Comparator
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
     *  @return The requested element.
     */
    public Object at(int index) {
        return _set.get(index);
    }

    /** Clear the set. Remove all elements.
     */
    public void clear() {
        _set.clear();
    }

    /** Return true if the given element is contained in this set.
     *  The equivalence relation is defined by the comparator.
     *  If the type of given element
     *  is uncomparable by the comparator, then a ClassCastException
     *  will be thrown.
     *  @return True If the elements is contained according to the
     *        comparator.
     */
    public boolean contains(Object obj) {
        boolean result = false;
        Iterator elements = _set.iterator();
        while (elements.hasNext()) {
            Object next = elements.next();
            int com = _comparator.compare(obj, next);
            if (com == 0) {
                result = true;
                break;
            }
            if (com < 0) {
                break;
            }
        }
        return result;
    }

    /** Return a list of all the elements.
     *  @return The list of all the elements.
     */
    public List elementList() {
        return  _set;
    }

    /** Return an Enumeration of all the elements.
     *  @return The enumeration of all the elements.
     *  @deprecated Use elementList() instead.
     */
    public Enumeration elements() {
        return  Collections.enumeration(_set);
    }

    /** Return the first element, ie. the <i>"smallest"</i> element.
     *  If the set is empty, then return null.
     *  @return The smallest element.
     */
    public Object first() {
        if (isEmpty()) {
            return null;
        }
        return _set.getFirst();
    }

    /** Return the comparator.
     *  @return The comparator.
     */
    public Comparator getComparator() {
        return _comparator;
    }

    /** Return the index of the given object. Return -1 if the object
     *  is not in the set.
     *  @return The index.
     */
    public int indexOf(Object obj) {
        return _set.indexOf(obj);
    }

    /** Insert the given element, keeping the set sorted. If the set
     *  contains an element "equals" to the given element,
     *  then do nothing.
     *  The equivalence relation is defined by the comparator.
     *  If the type of given element
     *  is uncomparable by the comparator, then a ClassCastException
     *  will be thrown.
     *  @param obj The element to be inserted.
     */
    public void insert(Object obj) {
        int count = 0;
        Iterator elements = _set.iterator();
        while (elements.hasNext()) {
            Object next = elements.next();
            int com = _comparator.compare(obj, next);
            if (com == 0) {
                return;
            }
            if (com < 0) {
                _set.add(count, obj);
                return;
            }
            count ++;
        }
        _set.addLast(obj);
    }

    /** Return true if the set is empty
     *  @return True if the set is empty.
     */
    public boolean isEmpty() {
        return _set.isEmpty();
    }

    /** Remove all the elements that are (strictly) less than the argument.
     *  If the set is empty or all the elements are greater than
     *  the argument, then do nothing.
     *
     *  @param obj The argument.
     */
    public void removeAllLessThan(Object obj) {
        if (_set == null) {
            return;
        }
        while (!isEmpty()) {
            Object first = first();
            int com = _comparator.compare(obj, first);
            if (com <= 0) {
                return;
            } else {
                take();
            }
        }
    }

    /** Remove and return the index-th element.
     *  Thrown an exception if the set is empty.
     *  @param index The index of the element.
     *  @return The removed element.
     *  @exception NoSuchElementException If the specified index is
     *        out of range.
     */
    public Object removeAt(int index) {
        return _set.remove(index);
    }

    /** Remove and return the first element, ie. the <i>"smallest"</i>
     *  element in the set.. Thrown an exception
     *  if the set is empty.
     *  @return The removed element.
     *  @exception NoSuchElementException If the set is empty.
     */
    public Object removeFirst() {
        return _set.removeFirst();
    }

    /** Return the size of the set.
     *  @return The size of the set.
     */
    public int size() {
        return _set.size();
    }

    /** Return the first element, ie. the <i>"smallest"</i> element and
     *  remove it from the set.
     *  @return The smallest element.
     *  @deprecated Use removeFirst() instead.
     */
    public Object take() {
        return _set.removeFirst();
    }

    /** Return a String that consists of the contents of the elements
     *  in the set. The elements are represented by there toString()
     *  value. This method is for test purpose.
     *  @return The String description of the set.
     */
    public String toString() {
        String result = new String();
        Iterator eles = elementList().iterator();
        while (eles.hasNext()) {
            result = result + (eles.next()).toString() + " ";
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The comparator for the order.
    private final Comparator _comparator;

    // The set.
    private LinkedList _set;
}
