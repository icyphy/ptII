/*
 * @(#)AbstractSequentialList.java	1.14 98/09/30
 *
 * Copyright 1997, 1998 by Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Sun Microsystems, Inc. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Sun.
 */

package java.util;

/**
 * This class provides a skeletal implementation of the <tt>List</tt>
 * interface to minimize the effort required to implement this interface
 * backed by a "sequential access" data store (such as a linked list).  For
 * random access data (such as an array), <tt>AbstractList</tt> should be used
 * in preference to this class.<p>
 *
 * This class is the opposite of the <tt>AbstractList</tt> class in the sense
 * that it implements the "random access" methods (<tt>get(int index)</tt>,
 * <tt>set(int index, Object element)</tt>, <tt>set(int index, Object
 * element)</tt>, <tt>add(int index, Object element)</tt> and <tt>remove(int
 * index)</tt>) on top of the list's list iterator, instead of the other way
 * around.<p>
 *
 * To implement a list the programmer needs only to extend this class and
 * provide implementations for the <tt>listIterator</tt> and <tt>size</tt>
 * methods.  For an unmodifiable list, the programmer need only implement the
 * list iterator's <tt>hasNext</tt>, <tt>next</tt>, <tt>hasPrevious</tt>,
 * <tt>previous</tt> and <tt>index</tt> methods.<p>
 *
 * For a modifiable list the programmer should additionally implement the list
 * iterator's <tt>set</tt> method.  For a variable-size list the programmer
 * should additionally implement the list iterator's <tt>remove</tt> and
 * <tt>add</tt> methods.<p>
 *
 * The programmer should generally provide a void (no argument) and collection
 * constructor, as per the recommendation in the <tt>Collection</tt> interface
 * specification.
 *
 * @author  Josh Bloch
 * @version 1.14 09/30/98
 * @see Collection
 * @see List
 * @see AbstractList
 * @see AbstractCollection
 * @since JDK1.2
 */

public abstract class AbstractSequentialList extends AbstractList {
    /**
     * Sole constructor.  (For invocation by subclass constructors, typically
     * implicit.)
     */
    protected AbstractSequentialList() {
    }

    /**
     * Returns the element at the specified position in this list. <p>
     *
     * This implementation first gets a list iterator pointing to the indexed
     * element (with <tt>listIterator(index)</tt>).  Then, it gets the element
     * using <tt>ListIterator.next</tt> and returns it.
     *
     * @return the element at the specified position in this list.  * @param
     * 		  index index of element to return.  * @exception
     * 		  IndexOutOfBoundsException if the specified index is out of
     * 		  range (<tt>index &lt; 0 || index &gt;= size()</tt>).
     */
    public Object get(int index) {
	ListIterator e = listIterator(index);
	try {
	    return(e.next());
	} catch(NoSuchElementException exc) {
	    throw(new IndexOutOfBoundsException("Index: "+index));
	}
    }

    /**
     * Replaces the element at the specified position in this list with the
     * specified element. <p>
     *
     * This implementation first gets a list iterator pointing to the
     * indexed element (with <tt>listIterator(index)</tt>).  Then, it gets
     * the current element using <tt>ListIterator.next</tt> and replaces it
     * with <tt>ListIterator.set</tt>.<p>
     *
     * Note that this implementation will throw an
     * UnsupportedOperationException if list iterator does not implement
     * the set operation.
     *
     * @param index index of element to replace.
     * @param element element to be stored at the specified position.
     * @return the element previously at the specified position.
     * @exception    UnsupportedOperationException set is not supported
     *		  by this list.
     * @exception    NullPointerException this list does not permit null
     * 		  elements and one of the elements of <code>c</code> is null.
     * @exception    ClassCastException class of the specified element
     * 		  prevents it from being added to this list.
     * @exception    IllegalArgumentException some aspect of the specified
     *		  element prevents it from being added to this list.
     * @exception    IndexOutOfBoundsException index out of range
     *		  <tt>(index &lt; 0 || index &gt;= size()</tt>).
     * @exception    IllegalArgumentException fromIndex &gt; toIndex.
     */
    public Object set(int index, Object element) {
	ListIterator e = listIterator(index);
	try {
	    Object oldVal = e.next();
	    e.set(element);
	    return oldVal;
	} catch(NoSuchElementException exc) {
	    throw(new IndexOutOfBoundsException("Index: "+index));
	}
    }

    /**
     * Inserts the specified element at the specified position in this list.
     * Shifts the element currently at that position (if any) and any
     * subsequent elements to the right (adds one to their indices).<p>
     *
     * This implementation first gets a list iterator pointing to the
     * indexed element (with <tt>listIterator(index)</tt>).  Then, it inserts
     * the specified element with <tt>ListIterator.add</tt>.<p>
     *
     * Note that this implementation will throw an
     * <tt>UnsupportedOperationException</tt> if list iterator does not
     * implement the <tt>add</tt> operation.
     *
     * @param index index at which the specified element is to be inserted.
     * @param element element to be inserted.
     * @exception UnsupportedOperationException if the <tt>add</tt> operation is
     *		  not supported by this list.
     * @exception NullPointerException this list does not permit <tt>null</tt>
     * 		  elements and one of the elements of <code>c</code> is
     * 		  <tt>null</tt>.
     * @exception    ClassCastException if the class of the specified element
     * 		  prevents it from being added to this list.
     * @exception    IllegalArgumentException if some aspect of the specified
     *		  element prevents it from being added to this list.
     * @exception IndexOutOfBoundsException if the specified index is out of
     *            range (<tt>index &lt; 0 || index &gt; size()</tt>).
     */
    public void add(int index, Object element) {
	ListIterator e = listIterator(index);
	e.add(element);
    }

    /**
     * Removes the element at the specified position in this list.  Shifts any
     * subsequent elements to the left (subtracts one from their indices).<p>
     *
     * This implementation first gets a list iterator pointing to the
     * indexed element (with <tt>listIterator(index)</tt>).  Then, it removes
     * the element with <tt>ListIterator.remove</tt>.<p>
     *
     * Note that this implementation will throw an
     * <tt>UnsupportedOperationException</tt> if list iterator does not
     * implement the <tt>remove</tt> operation.
     *
     * @param the index of the element to be removed from the List.
     * @return the element that was removed from the list.
     * @exception UnsupportedOperationException if the <tt>remove</tt> operation
     *		  is not supported by this list.
     * @exception IndexOutOfBoundsException if the specified index is out of
     * 		  range (index &lt; 0 || index &gt;= size()).
     */
    public Object remove(int index) {
	ListIterator e = listIterator(index);
	Object outCast;
	try {
	    outCast = e.next();
	} catch(NoSuchElementException exc) {
	    throw(new IndexOutOfBoundsException("Index: "+index));
	}
	e.remove();
	return(outCast);
    }


    // Bulk Operations

    /**
     * Inserts all of the elements in in the specified collection into this
     * list at the specified position.  Shifts the element currently at that
     * position (if any) and any subsequent elements to the right (increases
     * their indices).  The new elements will appear in the list in the order
     * that they are returned by the specified collection's iterator.  The
     * behavior of this operation is unspecified if the specified collection
     * is modified while the operation is in progress.  (Note that this will
     * occur if the specified collection is this list, and it's nonempty.)
     * Optional operation.<p>
     *
     * This implementation gets an iterator over the specified collection and
     * a list iterator over this list pointing to the indexed element (with
     * <tt>listIterator(index)</tt>).  Then, it iterates over the specified
     * collection, inserting the elements obtained from the iterator into this
     * list, one at a time, using <tt>ListIterator.add</tt> followed by
     * <tt>ListIterator.next</tt> (to skip over the added element).<p>
     *
     * Note that this implementation will throw an
     * <tt>UnsupportedOperationException</tt> if the list iterator returned by
     * the <tt>listIterator</tt> method does not implement the <tt>add</tt>
     * operation.
     *
     * @return <tt>true</tt> if this list changed as a result of the call.
     * @param index index at which to insert first element from the specified
     *		    collection.
     * @param c elements to be inserted into this list.
     * @exception UnsupportedOperationException if the <tt>addAll</tt> operation
     *		  is not supported by this list.
     * @exception NullPointerException this list does not permit <tt>null</tt>
     * 		  elements and one of the elements of the specified collection
     * 		  is <tt>null</tt>.
     * @exception    ClassCastException if the class of the specified element
     * 		  prevents it from being added to this list.
     * @exception    IllegalArgumentException if some aspect of the specified
     *		  element prevents it from being added to this list.
     * @exception IndexOutOfBoundsException if the specified index is out of
     *            range (<tt>index &lt; 0 || index &gt; size()</tt>).
     */
    public boolean addAll(int index, Collection c) {
	boolean modified = false;
	ListIterator e1 = listIterator(index);
	Iterator e2 = c.iterator();
	while (e2.hasNext()) {
	    e1.add(e2.next());
	    e1.next();	// Skip over the element that we just added
	    modified = true;
	}
	return modified;
    }


    // Iterators

    /**
     * Returns an iterator over the elements in this list (in proper
     * sequence).<p> 
     *
     * This implementation merely returns a list iterator over the list.
     *
     * @return an iterator over the elements in this list (in proper sequence).
     */
    public Iterator iterator() {
        return listIterator();
    }

    /**
     * Returns a list iterator over the elements in this list (in proper
     * sequence).
     *
     * @return a list iterator over the elements in this list (in proper
     *      sequence).
     */
    public abstract ListIterator listIterator(int index);
}
