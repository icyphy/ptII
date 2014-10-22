/* An object of this class is a totally ordered set.

 Copyright (c) 2006-2014 The Regents of the University of California.
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
package ptolemy.actor.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

///////////////////////////////////////////////////////////////////
//// TotallyOrderedSet

/**
 An object of this class is a totally ordered set with an increasing order.
 The order between any two elements in the set can be checked by calling the
 compare() method of a comparator associated with this object. An element, a,
 in this set is said to precede another one, b, if compare(a, b) returns -1.
 <p>
 The set does not contain repeated elements, which means comparing any two
 elements in this set never returns 0.

 @author  Jie Liu, Haiyang Zheng
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Green (hyzheng)
 @Pt.AcceptedRating Green (hyzheng)
 */
public class TotallyOrderedSet {
    /** Construct a set with the given comparator.
     *  @param comparator The Comparator with which to compare elements.
     *  Note that the comparator cannot be changed after this TotallyOrderedSet
     *  object is constructed.
     *  @see java.util.Comparator
     */
    public TotallyOrderedSet(Comparator comparator) {
        _comparator = comparator;
        _set = new LinkedList();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the element with the given index. The index starts with 0.
     *  @param index The index of the element to return.
     *  @return The requested element.
     */
    public Object at(int index) {
        return _set.get(index);
    }

    /** Clear the set by removing all elements.
     */
    public void clear() {
        _set.clear();
    }

    /** Return true if the given element is contained in this set.
     *  The equivalence relation is defined by the comparator.
     *  If the type of given element is not comparable by the comparator,
     *  then a ClassCastException will be thrown.
     *  @param object The object to check for containment.
     *  @return True If the element is contained in this set.
     */
    public boolean contains(Object object) {
        boolean result = false;
        Iterator elements = _set.iterator();

        while (elements.hasNext()) {
            Object next = elements.next();
            int comparator = _comparator.compare(object, next);

            if (comparator == 0) {
                result = true;
                break;
            }

            if (comparator < 0) {
                break;
            }
        }

        return result;
    }

    /** Return a list of all the elements.
     *  @return The list of all the elements.
     */
    public List elementList() {
        return _set;
    }

    /** Return an enumeration of all the elements.
     *  @return The enumeration of all the elements.
     *  @deprecated Use elementList() instead.
     */
    @Deprecated
    public Enumeration elements() {
        return Collections.enumeration(_set);
    }

    /** Return the first element, ie. the <i>smallest</i> element.
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
     *  @param obj The object to get index for.
     *  @return The index.
     */
    public int indexOf(Object obj) {
        return _set.indexOf(obj);
    }

    /** Insert the given element while keeping the set sorted. If the set
     *  contains an element "equal to" the given element, then do nothing.
     *  The equivalence relation is defined by the comparator.
     *  If the type of the given element is not comparable, then a
     *  ClassCastException will be thrown.
     *  @param obj The element to be inserted.
     */
    public void insert(Object obj) {
        int count = 0;
        Iterator elements = _set.iterator();

        while (elements.hasNext()) {
            Object next = elements.next();
            int comparisonResult = _comparator.compare(obj, next);

            if (comparisonResult == 0) {
                return;
            } else if (comparisonResult < 0) {
                _set.add(count, obj);
                return;
            }

            count++;
        }

        _set.addLast(obj);
    }

    /** Return true if the set is empty.
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
        while (!isEmpty()) {
            Object first = first();
            int com = _comparator.compare(obj, first);

            if (com <= 0) {
                return;
            } else {
                removeFirst();
            }
        }
    }

    /** Remove and return the element with the given index.
     *  @param index The index of the element.
     *  @return The removed element.
     */
    public Object removeAt(int index) {
        return _set.remove(index);
    }

    /** Remove and return the first element, ie. the <i>smallest</i>
     *  element in the set.
     *  @return The removed element.
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

    /** Return the first element, ie. the <i>smallest</i> element and
     *  remove it from the set.
     *  @return The smallest element.
     *  @deprecated Use removeFirst() instead.
     */
    @Deprecated
    public Object take() {
        return _set.removeFirst();
    }

    /** Return a string that consists of the contents of the elements
     *  in the set. The elements are represented by there toString()
     *  value. This method is for test purpose.
     *  @return The string description of the set.
     */
    @Override
    public String toString() {
        StringBuffer result = new StringBuffer();
        Iterator elements = elementList().iterator();

        while (elements.hasNext()) {
            result.append(elements.next().toString() + " ");
        }

        return result.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The comparator for the order.  The comparator is a blank final
     *  field that can't be changed after creation.
     */
    private final Comparator _comparator;

    /** The set. */
    private LinkedList _set;
}
