/*
 * @(#)Collections.java	1.32 98/11/05
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
import java.io.Serializable;

/**
 * This class consists exclusively of static methods that operate on or return
 * collections.  It contains polymorphic algorithms that operate on
 * collections, "wrappers", which return a new collection backed by a
 * specified collection, and a few other odds and ends.<p>
 *
 * The documentation for the polymorphic algorithms contained in this class
 * generally includes a brief description of the <i>implementation</i>.  Such
 * descriptions should be regarded as <i>implementation notes</i>, rather than
 * parts of the <i>specification</i>.  Implementors should feel free to
 * substitute other algorithms, so long as the specification itself is adhered
 * to.  (For example, the algorithm used by <tt>sort</tt> does not have to be
 * a mergesort, but it does have to be <i>stable</i>.)
 *
 * @author  Josh Bloch
 * @version 1.32 11/05/98
 * @see	    Collection
 * @see	    Set
 * @see	    List
 * @see	    Map
 * @since JDK1.2
 */

public class Collections {
    // Suppresses default constructor, ensuring non-instantiability.
    private Collections() {
    }

    // Algorithms

    /**
     * Sorts the specified list into ascending order, according to the
     * <i>natural ordering</i> of its elements.  All elements in the list must
     * implement the <tt>Comparable</tt> interface.  Furthermore, all elements
     * in the list must be <i>mutually comparable</i> (that is,
     * <tt>e1.compareTo(e2)</tt> must not throw a <tt>ClassCastException</tt>
     * for any elements <tt>e1</tt> and <tt>e2</tt> in the list).<p>
     *
     * This sort is guaranteed to be <i>stable</i>:  equal elements will
     * not be reordered as a result of the sort.<p>
     *
     * The specified list must be modifiable, but need not be resizable.<p>
     *
     * The sorting algorithm is a modified mergesort (in which the merge is
     * omitted if the highest element in the low sublist is less than the
     * lowest element in the high sublist).  This algorithm offers guaranteed
     * n log(n) performance, and can approach linear performance on nearly
     * sorted lists.<p>
     *
     * This implementation dumps the specified list into an array, sorts
     * the array, and iterates over the list resetting each element
     * from the corresponding position in the array.  This avoids the
     * n<sup>2</sup> log(n) performance that would result from attempting
     * to sort a linked list in place.
     *
     * @param  list the list to be sorted.
     * @exception ClassCastException if the list contains elements that are not
     *	       <i>mutually comparable</i> (for example, strings and integers).
     * @exception UnsupportedOperationException if the specified list's
     *	       list-iterator does not support the <tt>set</tt> operation.
     * @see Comparable
     */
    public static void sort(List list) {
	Object a[] = list.toArray();
	Arrays.sort(a);
	ListIterator i = list.listIterator();
	for (int j=0; j<a.length; j++) {
	    i.next();
	    i.set(a[j]);
	}
    }

    /**
     * Sorts the specified list according to the order induced by the
     * specified comparator.  All elements in the list must be <i>mutually
     * comparable</i> using the specified comparator (that is,
     * <tt>c.compare(e1, e2)</tt> must not throw a <tt>ClassCastException</tt>
     * for any elements <tt>e1</tt> and <tt>e2</tt> in the list).<p>
     *
     * This sort is guaranteed to be <i>stable</i>:  equal elements will
     * not be reordered as a result of the sort.<p>
     *
     * The sorting algorithm is a modified mergesort (in which the merge is
     * omitted if the highest element in the low sublist is less than the
     * lowest element in the high sublist).  This algorithm offers guaranteed
     * n log(n) performance, and can approach linear performance on nearly
     * sorted lists.<p>
     *
     * The specified list must be modifiable, but need not be resizable.
     * This implementation dumps the specified list into an array, sorts
     * the array, and iterates over the list resetting each element
     * from the corresponding position in the array.  This avoids the
     * n<sup>2</sup> log(n) performance that would result from attempting
     * to sort a linked list in place.
     *
     * @param  list the list to be sorted.
     * @param  c the comparator to determine the order of the array.
     * @exception ClassCastException if the list contains elements that are not
     *	       <i>mutually comparable</i> using the specified comparator.
     * @exception UnsupportedOperationException if the specified list's
     *	       list-iterator does not support the <tt>set</tt> operation.
     * @see Comparator
     */
    public static void sort(List list, Comparator c) {
	Object a[] = list.toArray();
	Arrays.sort(a, c);
	ListIterator i = list.listIterator();
	for (int j=0; j<a.length; j++) {
	    i.next();
	    i.set(a[j]);
	}
    }


    /**
     * Searches the specified list for the specified object using the binary
     * search algorithm.  The list must be sorted into ascending order
     * according to the <i>natural ordering</i> of its elements (as by the
     * <tt>sort(List)</tt> method, above) prior to making this call.  If it is
     * not sorted, the results are undefined.  If the list contains multiple
     * elements equal to the specified object, there is no guarantee which one
     * will be found.<p>
     *
     * This method runs in log(n) time for a "random access" list (which
     * provides near-constant-time positional access).  It may
     * run in n log(n) time if it is called on a "sequential access" list
     * (which provides linear-time positional access).</p>
     *
     * If the specified list implements the <tt>AbstracSequentialList</tt>
     * interface, this method will do a sequential search instead of a binary
     * search; this offers linear performance instead of n log(n) performance
     * if this method is called on a <tt>LinkedList</tt> object.
     *
     * @param  list the list to be searched.
     * @param  key the key to be searched for.
     * @return index of the search key, if it is contained in the list;
     *	       otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
     *	       <i>insertion point</i> is defined as the point at which the
     *	       key would be inserted into the list: the index of the first
     *	       element greater than the key, or <tt>list.size()</tt>, if all
     *	       elements in the list are less than the specified key.  Note
     *	       that this guarantees that the return value will be &gt;= 0 if
     *	       and only if the key is found.
     * @exception ClassCastException if the list contains elements that are not
     *	       <i>mutually comparable</i> (for example, strings and
     *	       integers), or the search key in not mutually comparable
     *	       with the elements of the list.
     * @see    Comparable
     * @see #sort(List)
     */
    public static int binarySearch(List list, Object key) {
	// Do a sequential search if appropriate
	if (list instanceof AbstractSequentialList) {
	    ListIterator i = list.listIterator();
	    while (i.hasNext()) {
		int cmp = ((Comparable)(i.next())).compareTo(key);
		if (cmp == 0)
		    return i.previousIndex();
		else if (cmp > 0)
		    return -i.nextIndex();  // key not found.
	    }
	    return -i.nextIndex()-1;  // key not found, list exhausted
	}

	// Otherwise, do a binary search
	int low = 0;
	int high = list.size()-1;

	while (low <= high) {
	    int mid =(low + high)/2;
	    Object midVal = list.get(mid);
	    int cmp = ((Comparable)midVal).compareTo(key);

	    if (cmp < 0)
		low = mid + 1;
	    else if (cmp > 0)
		high = mid - 1;
	    else
		return mid; // key found
	}
	return -(low + 1);  // key not found
    }

    /**
     * Searches the specified list for the specified object using the binary
     * search algorithm.  The list must be sorted into ascending order
     * according to the specified comparator (as by the <tt>Sort(List,
     * Comparator)</tt> method, above), prior to making this call.  If it is
     * not sorted, the results are undefined.  If the list contains multiple
     * elements equal to the specified object, there is no guarantee which one
     * will be found.<p>
     *
     * This method runs in log(n) time for a "random access" list (which
     * provides near-constant-time positional access).  It may
     * run in n log(n) time if it is called on a "sequential access" list
     * (which provides linear-time positional access).</p>
     *
     * If the specified list implements the <tt>AbstracSequentialList</tt>
     * interface, this method will do a sequential search instead of a binary
     * search; this offers linear performance instead of n log(n) performance
     * if this method is called on a <tt>LinkedList</tt> object.
     *
     * @param  list the list to be searched.
     * @param  key the key to be searched for.
     * @param  c the comparator by which the list is ordered.
     * @return index of the search key, if it is contained in the list;
     *	       otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
     *	       <i>insertion point</i> is defined as the point at which the
     *	       key would be inserted into the list: the index of the first
     *	       element greater than the key, or <tt>list.size()</tt>, if all
     *	       elements in the list are less than the specified key.  Note
     *	       that this guarantees that the return value will be &gt;= 0 if
     *	       and only if the key is found.
     * @exception ClassCastException if the list contains elements that are not
     *	       <i>mutually comparable</i> using the specified comparator,
     *	       or the search key in not mutually comparable with the
     *	       elements of the list using this comparator.
     * @see    Comparable
     * @see #sort(List, Comparator)
     */
    public static int binarySearch(List list, Object key, Comparator c) {
	// Do a sequential search if appropriate
	if (list instanceof AbstractSequentialList) {
	    ListIterator i = list.listIterator();
	    while (i.hasNext()) {
		int cmp = c.compare(i.next(), key);
		if (cmp == 0)
		    return i.previousIndex();
		else if (cmp > 0)
		    return -i.nextIndex();  // key not found.
	    }
	    return -i.nextIndex()-1;  // key not found, list exhausted
	}

	// Otherwise, do a binary search
	int low = 0;
	int high = list.size()-1;

	while (low <= high) {
	    int mid =(low + high)/2;
	    Object midVal = list.get(mid);
	    int cmp = c.compare(midVal, key);

	    if (cmp < 0)
		low = mid + 1;
	    else if (cmp > 0)
		high = mid - 1;
	    else
		return mid; // key found
	}
	return -(low + 1);  // key not found
    }

    /**
     * Reverses the order of the elements in the specified list.<p>
     *
     * This method runs in linear time.
     *
     * @param  list the list whose elements are to be reversed.
     * @exception UnsupportedOperationException if the specified list's
     *	       list-iterator does not support the <tt>set</tt> operation.
     */
    public static void reverse(List l) {
        ListIterator fwd = l.listIterator(), rev = l.listIterator(l.size());
        for (int i=0, n=l.size()/2; i<n; i++) {
            Object tmp = fwd.next();
            fwd.set(rev.previous());
            rev.set(tmp);
        }
    }

    /**
     * Randomly permutes the specified list using a default source of
     * randomness.  All permutations occur with approximately equal
     * likelihood.<p>
     *
     * The hedge "approximately" is used in the foregoing description because
     * default source of randomenss is only approximately an unbiased source
     * of independently chosen bits. If it were a perfect source of randomly
     * chosen bits, then the algorithm would choose permutations with perfect
     * uniformity.<p>
     *
     * This implementation traverses the list backwards, from the last element
     * up to the second, repeatedly swapping a randomly selected element into
     * the "current position".  Elements are randomly selected from the
     * portion of the list that runs from the first element to the current
     * position, inclusive.<p>
     *
     * This method runs in linear time for a "random access" list (which
     * provides near-constant-time positional access).  It may require
     * quadratic time for a "sequential access" list.
     *
     * @param  list the list to be shuffled.
     * @exception UnsupportedOperationException if the specified list's
     *         list-iterator does not support the <tt>set</tt> operation.
     */
    public static void shuffle(List list) {
        shuffle(list, r);
    }
    private static Random r = new Random();

    /**
     * Randomly permute the specified list using the specified source of
     * randomness.  All permutations occur with equal likelihood
     * assuming that the source of randomness is fair.<p>
     *
     * This implementation traverses the list backwards, from the last element
     * up to the second, repeatedly swapping a randomly selected element into
     * the "current position".  Elements are randomly selected from the
     * portion of the list that runs from the first element to the current
     * position, inclusive.<p>
     *
     * This method runs in linear time for a "random access" list (which
     * provides near-constant-time positional access).  It may require
     * quadratic time for a "sequential access" list.
     *
     * @param  list the list to be shuffled.
     * @param  r the source of randomness to use to shuffle the list.
     * @exception UnsupportedOperationException if the specified list's
     *         list-iterator does not support the <tt>set</tt> operation.
     */
    public static void shuffle(List list, Random rnd) {
        for (int i=list.size(); i>1; i--)
            swap(list, i-1, rnd.nextInt(i));
    }

    /**
     * Swaps the two specified elements in the specified list.
     */
    private static void swap(List a, int i, int j) {
        Object tmp = a.get(i);
        a.set(i, a.get(j));
        a.set(j, tmp);
    }

    /**
     * Replaces all of the elements of the specified list with the specified
     * element. <p>
     *
     * This method runs in linear time.
     *
     * @param  list the list to be filled with the specified element.
     * @param  o The element with which to fill the specified list.
     * @exception UnsupportedOperationException if the specified list's
     *	       list-iterator does not support the <tt>set</tt> operation.
     */
    public static void fill(List list, Object o) {
        for (ListIterator i = list.listIterator(); i.hasNext(); ) {
            i.next();
            i.set(o);
        }
    }

    /**
     * Copies all of the elements from one list into another.  After the
     * operation, the index of each copied element in the destination list
     * will be identical to its index in the source list.  The destination
     * list must be at least as long as the source list.  If it is longer, the
     * remaining elements in the destination list are unaffected. <p>
     *
     * This method runs in linear time.
     *
     * @param  dest The destination list.
     * @param  src The source list.
     * @exception IndexOutOfBoundsException if the destination list is too small
     *         to contain the entire source List.
     * @exception UnsupportedOperationException if the destination list's
     *         list-iterator does not support the <tt>set</tt> operation.
     */
    public static void copy (List dest, List src) {
        try {
	    for (ListIterator di=dest.listIterator(), si=src.listIterator();
		 si.hasNext(); ) {
                di.next();
                di.set(si.next());
            }
	} catch(NoSuchElementException e) {
           throw new IndexOutOfBoundsException("Source does not fit in dest.");
        }
    }

    /**
     * Returns the minimum element of the given collection, according to the
     * <i>natural ordering</i> of its elements.  All elements in the
     * collection must implement the <tt>Comparable</tt> interface.
     * Furthermore, all elements in the collection must be <i>mutually
     * comparable</i> (that is, <tt>e1.compareTo(e2)</tt> must not throw a
     * <tt>ClassCastException</tt> for any elements <tt>e1</tt> and
     * <tt>e2</tt> in the collection).<p>
     *
     * This method iterates over the entire collection, hence it requires
     * time proportional to the size of the collection.
     *
     * @param  coll the collection whose minimum element is to be determined.
     * @return the minimum element of the given collection, according
     *         to the <i>natural ordering</i> of its elements.
     * @exception ClassCastException if the collection contains elements that are
     *	       not <i>mutually comparable</i> (for example, strings and
     *	       integers).
     * @exception NoSuchElementException if the collection is empty.
     * @see Comparable
     */
    public static Object min(Collection coll) {
	Iterator i = coll.iterator();
	Comparable candidate = (Comparable)(i.next());
	while (i.hasNext()) {
	    Comparable next = (Comparable)(i.next());
	    if (next.compareTo(candidate) < 0)
		candidate = next;
	}
	return candidate;
    }

    /**
     * Returns the minimum element of the given collection, according to the
     * order induced by the specified comparator.  All elements in the
     * collection must be <i>mutually comparable</i> by the specified
     * comparator (that is, <tt>comp.compare(e1, e2)</tt> must not throw a
     * <tt>ClassCastException</tt> for any elements <tt>e1</tt> and
     * <tt>e2</tt> in the collection).<p>
     *
     * This method iterates over the entire collection, hence it requires
     * time proportional to the size of the collection.
     *
     * @param  coll the collection whose minimum element is to be determined.
     * @return the minimum element of the given collection, according
     *         to the specified comparator.
     * @exception ClassCastException if the collection contains elements that are
     *	       not <i>mutually comparable</i> using the specified comparator.
     * @exception NoSuchElementException if the collection is empty.
     * @see Comparable
     */
    public static Object min(Collection coll, Comparator comp) {
	Iterator i = coll.iterator();
	Object candidate = i.next();
	while (i.hasNext()) {
	    Object next = i.next();
	    if (comp.compare(next, candidate) < 0)
		candidate = next;
	}
	return candidate;
    }

    /**
     * Returns the maximum element of the given collection, according to the
     * <i>natural ordering</i> of its elements.  All elements in the
     * collection must implement the <tt>Comparable</tt> interface.
     * Furthermore, all elements in the collection must be <i>mutually
     * comparable</i> (that is, <tt>e1.compareTo(e2)</tt> must not throw a
     * <tt>ClassCastException</tt> for any elements <tt>e1</tt> and
     * <tt>e2</tt> in the collection).<p>
     *
     * This method iterates over the entire collection, hence it requires
     * time proportional to the size of the collection.
     *
     * @param  coll the collection whose maximum element is to be determined.
     * @return the maximum element of the given collection, according
     *         to the <i>natural ordering</i> of its elements.
     * @exception ClassCastException if the collection contains elements that are
     *	       not <i>mutually comparable</i> (for example, strings and
     *         integers).
     * @exception NoSuchElementException if the collection is empty.
     * @see Comparable
     */
    public static Object max(Collection coll) {
	Iterator i = coll.iterator();
	Comparable candidate = (Comparable)(i.next());
	while (i.hasNext()) {
	    Comparable next = (Comparable)(i.next());
	    if (next.compareTo(candidate) > 0)
		candidate = next;
	}
	return candidate;
    }

    /**
     * Returns the maximum element of the given collection, according to the
     * order induced by the specified comparator.  All elements in the
     * collection must be <i>mutually comparable</i> by the specified
     * comparator (that is, <tt>comp.compare(e1, e2)</tt> must not throw a
     * <tt>ClassCastException</tt> for any elements <tt>e1</tt> and
     * <tt>e2</tt> in the collection).<p>
     *
     * This method iterates over the entire collection, hence it requires
     * time proportional to the size of the collection.
     *
     * @param  coll the collection whose maximum element is to be determined.
     * @return the maximum element of the given collection, according
     *         to the specified comparator.
     * @exception ClassCastException if the collection contains elements that are
     *	       not <i>mutually comparable</i> using the specified comparator.
     * @exception NoSuchElementException if the collection is empty.
     * @see Comparable
     */
    public static Object max(Collection coll, Comparator comp) {
	Iterator i = coll.iterator();
	Object candidate = i.next();
	while (i.hasNext()) {
	    Object next = i.next();
	    if (comp.compare(next, candidate) > 0)
		candidate = next;
	}
	return candidate;
    }


    // Unmodifiable Wrappers

    /**
     * Returns an unmodifiable view of the specified collection.  This method
     * allows modules to provide users with "read-only" access to internal
     * collections.  Query operations on the returned collection "read through"
     * to the specified collection, and attempts to modify the returned
     * collection, whether direct or via its iterator, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned collection does <i>not</i> pass the hashCode and equals
     * operations through to the backing collection, but relies on
     * <tt>Object</tt>'s <tt>equals</tt> and <tt>hashCode</tt> methods.  This
     * is necessary to preserve the contracts of these operations in the case
     * that the backing collection is a set or a list.<p>
     *
     * The returned collection will be serializable if the specified collection
     * is serializable. 
     *
     * @param  c the collection for which an unmodifiable view is to be
     *	       returned.
     * @return an unmodifiable view of the specified collection.
     */
    public static Collection unmodifiableCollection(Collection c) {
	return new UnmodifiableCollection(c);
    }

    static class UnmodifiableCollection implements Collection, Serializable {
	Collection c;

	UnmodifiableCollection(Collection c) {this.c = c;}

	public int size() 		    {return c.size();}
	public boolean isEmpty() 	    {return c.isEmpty();}
	public boolean contains(Object o)   {return c.contains(o);}
	public Object[] toArray() 	    {return c.toArray();}
	public Object[] toArray(Object[] a) {return c.toArray(a);}

	public Iterator iterator() {
	    return new Iterator() {
		Iterator i = c.iterator();

		public boolean hasNext() {return i.hasNext();}
		public Object next() 	 {return i.next();}
		public void remove() {
		    throw new UnsupportedOperationException();
                }
	    };
        }

	public boolean add(Object o){
	    throw new UnsupportedOperationException();
        }
	public boolean remove(Object o) {
	    throw new UnsupportedOperationException();
        }

	public boolean containsAll(Collection coll) {
	    return c.containsAll(coll);
        }
	public boolean addAll(Collection coll) {
	    throw new UnsupportedOperationException();
        }
	public boolean removeAll(Collection coll) {
	    throw new UnsupportedOperationException();
        }
	public boolean retainAll(Collection coll) {
	    throw new UnsupportedOperationException();
        }
	public void clear() {
	    throw new UnsupportedOperationException();
        }
    }

    /**
     * Returns an unmodifiable view of the specified set.  This method allows
     * modules to provide users with "read-only" access to internal sets.
     * Query operations on the returned set "read through" to the specified
     * set, and attempts to modify the returned set, whether direct or via its
     * iterator, result in an <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned set will be serializable if the specified set
     * is serializable. 
     *
     * @param  s the set for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified set.
     */

    public static Set unmodifiableSet(Set s) {
	return new UnmodifiableSet(s);
    }

    static class UnmodifiableSet extends UnmodifiableCollection
    				 implements Set, Serializable {
	UnmodifiableSet(Set s) 		{super(s);}

	public boolean equals(Object o) {return c.equals(o);}
	public int hashCode() 		{return c.hashCode();}
    }

    /**
     * Returns an unmodifiable view of the specified sorted set.  This method
     * allows modules to provide users with "read-only" access to internal
     * sorted sets.  Query operations on the returned sorted set "read
     * through" to the specified sorted set.  Attempts to modify the returned
     * sorted set, whether direct, via its iterator, or via its
     * <tt>subSet</tt>, <tt>headSet</tt>, or <tt>tailSet</tt> views, result in
     * an <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned sorted set will be serializable if the specified sorted set
     * is serializable. 
     *
     * @param s the sorted set for which an unmodifiable view is to be
     *        returned. 
     * @return an unmodifiable view of the specified sorted set.
     */
    public static SortedSet unmodifiableSortedSet(SortedSet s) {
	return new UnmodifiableSortedSet(s);
    }

    static class UnmodifiableSortedSet extends UnmodifiableSet
    				 implements SortedSet, Serializable {
        private SortedSet ss;

	UnmodifiableSortedSet(SortedSet s) {super(s); ss = s;}

        public Comparator comparator()     {return ss.comparator();}

        public SortedSet subSet(Object fromElement, Object toElement) {
            return new UnmodifiableSortedSet(ss.subSet(fromElement,toElement));
        }
        public SortedSet headSet(Object toElement) {
            return new UnmodifiableSortedSet(ss.headSet(toElement));
        }
        public SortedSet tailSet(Object fromElement) {
            return new UnmodifiableSortedSet(ss.tailSet(fromElement));
        }

        public Object first() 	           {return ss.first();}
        public Object last()  	           {return ss.last();}
    }

    /**
     * Returns an unmodifiable view of the specified list.  This method allows
     * modules to provide users with "read-only" access to internal
     * lists.  Query operations on the returned list "read through" to the
     * specified list, and attempts to modify the returned list, whether
     * direct or via its iterator, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned list will be serializable if the specified list
     * is serializable. 
     *
     * @param  list the list for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified list.
     */
    public static List unmodifiableList(List list) {
	return new UnmodifiableList(list);
    }

    static class UnmodifiableList extends UnmodifiableCollection
    				  implements List {
	private List list;

	UnmodifiableList(List list) {
	    super(list);
	    this.list = list;
	}

	public boolean equals(Object o) {return list.equals(o);}
	public int hashCode() 		{return list.hashCode();}

	public Object get(int index) {return list.get(index);}
	public Object set(int index, Object element) {
	    throw new UnsupportedOperationException();
        }
	public void add(int index, Object element) {
	    throw new UnsupportedOperationException();
        }
	public Object remove(int index) {
	    throw new UnsupportedOperationException();
        }
	public int indexOf(Object o)            {return list.indexOf(o);}
	public int lastIndexOf(Object o)        {return list.lastIndexOf(o);}
	public boolean addAll(int index, Collection c) {
	    throw new UnsupportedOperationException();
        }
	public ListIterator listIterator() 	{return listIterator(0);}

	public ListIterator listIterator(final int index) {
	    return new ListIterator() {
		ListIterator i = list.listIterator(index);

		public boolean hasNext()     {return i.hasNext();}
		public Object next()         {return i.next();}
		public boolean hasPrevious() {return i.hasPrevious();}
		public Object previous()     {return i.previous();}
		public int nextIndex()       {return i.nextIndex();}
		public int previousIndex()   {return i.previousIndex();}

		public void remove() {
		    throw new UnsupportedOperationException();
                }
		public void set(Object o) {
		    throw new UnsupportedOperationException();
                }
		public void add(Object o) {
		    throw new UnsupportedOperationException();
                }
	    };
	}

	public List subList(int fromIndex, int toIndex) {
            return new UnmodifiableList(list.subList(fromIndex, toIndex));
        }
    }

    /**
     * Returns an unmodifiable view of the specified map.  This method
     * allows modules to provide users with "read-only" access to internal
     * maps.  Query operations on the returned map "read through"
     * to the specified map, and attempts to modify the returned
     * map, whether direct or via its collection views, result in an
     * <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned map will be serializable if the specified map
     * is serializable. 
     *
     * @param  m the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified map.
     */
    public static Map unmodifiableMap(Map m) {
	return new UnmodifiableMap(m);
    }

    private static class UnmodifiableMap implements Map, Serializable {
	private final Map m;

	UnmodifiableMap(Map m) 		         {this.m = m;}

	public int size() 		         {return m.size();}
	public boolean isEmpty() 	         {return m.isEmpty();}
	public boolean containsKey(Object key)   {return m.containsKey(key);}
	public boolean containsValue(Object val) {return m.containsValue(val);}
	public Object get(Object key) 	         {return m.get(key);}

	public Object put(Object key, Object value) {
	    throw new UnsupportedOperationException();
        }
	public Object remove(Object key) {
	    throw new UnsupportedOperationException();
        }
	public void putAll(Map t) {
	    throw new UnsupportedOperationException();
        }
	public void clear() {
	    throw new UnsupportedOperationException();
        }

	private transient Set keySet = null;
	private transient Set entrySet = null;
	private transient Collection values = null;

	public Set keySet() {
	    if (keySet==null)
		keySet = unmodifiableSet(m.keySet());
	    return keySet;
	}

	public Set entrySet() {
	    if (entrySet==null)
		entrySet = new UnmodifiableEntrySet(m.entrySet());
	    return entrySet;
	}

	public Collection values() {
	    if (values==null)
		values = unmodifiableCollection(m.values());
	    return values;
	}

	public boolean equals(Object o) {return m.equals(o);}
	public int hashCode()           {return m.hashCode();}


        /**
         * We need this class in addition to UnmodifiableSet as
         * Map.Entries themselves permit modification of the backing Map
         * via their setValue operation.  This class is subtle: there are
         * many possible attacks that must be thwarted.
         */
        static class UnmodifiableEntrySet extends UnmodifiableSet {
            UnmodifiableEntrySet(Set s) {
                super(s);
            }

            public Iterator iterator() {
                return new Iterator() {
                    Iterator i = c.iterator();

                    public boolean hasNext() {
                        return i.hasNext();
                    }
                    public Object next() 	 {
                        return new UnmodifiableEntry((Map.Entry)i.next());
                    }
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }

            public Object[] toArray() {
                Object[] a = c.toArray();
                for (int i=0; i<a.length; i++)
                    a[i] = new UnmodifiableEntry((Map.Entry)a[i]);
                return a;
            }

            public Object[] toArray(Object a[]) {
                // We don't pass a to c.toArray, to avoid window of
                // vulnerability wherein an unscrupulous multithreaded client
                // could get his hands on raw (unwrapped) Entries from c.
                Object[] arr = c.toArray(a.length==0 ? a :
                            (Object[])java.lang.reflect.Array.newInstance(
                                          a.getClass().getComponentType(), 0));
                for (int i=0; i<arr.length; i++)
                    arr[i] = new UnmodifiableEntry((Map.Entry)arr[i]);

                if (arr.length > a.length)
                    return arr;

                System.arraycopy(arr, 0, a, 0, arr.length);
                if (a.length > arr.length)
                    a[arr.length] = null;
                return a;
            }

            /**
             * This method is overridden to protect the backing set against
             * an object with a nefarious equals function that senses
             * that the equality-candidate is Map.Entry and calls its
             * setValue method.
             */
            public boolean contains(Object o) {
                if (!(o instanceof Map.Entry))
                    return false;
                return c.contains(new UnmodifiableEntry((Map.Entry)o));
            }

            /**
             * The next two methods are overridden to protect against
             * an unscrupulous List whose contains(Object o) method senses
             * when o is a Map.Entry, and calls o.setValue.
             */
            public boolean containsAll(Collection coll) {
                Iterator e = coll.iterator();
                while (e.hasNext())
                    if(!contains(e.next())) // Invokes safe contains() above
                        return false;
                return true;
            }
            public boolean equals(Object o) {
                if (o == this)
                    return true;

                if (!(o instanceof Set))
                    return false;
                Set s = (Set) o;
                if (s.size() != c.size())
                    return false;
                return containsAll(s); // Invokes safe containsAll() above
            }

            /**
             * This "wrapper class" serves two purposes: it prevents
             * the client from modifying the backing Map, by short-circuiting
             * the setValue method, and it protects the backing Map against
             * an ill-behaved Map.Entry that attempts to modify another
             * Map Entry when asked to perform an equality check.
             */
            private static class UnmodifiableEntry implements Map.Entry {
                private Map.Entry e;

                UnmodifiableEntry(Map.Entry e) {this.e = e;}

                public Object getKey()	  {return e.getKey();}
                public Object getValue()  {return e.getValue();}
                public Object setValue(Object value) {
                    throw new UnsupportedOperationException();
                }
                public int hashCode()	  {return e.hashCode();}
                public boolean equals(Object o) {
                    if (!(o instanceof Map.Entry))
                        return false;
                    Map.Entry t = (Map.Entry)o;
                    return eq(e.getKey(),   t.getKey()) &&
                           eq(e.getValue(), t.getValue());
                }
                public String toString()  {return e.toString();}
            }
        }
    }

    /**
     * Returns an unmodifiable view of the specified sorted map.  This method
     * allows modules to provide users with "read-only" access to internal
     * sorted maps.  Query operations on the returned sorted map "read through"
     * to the specified sorted map.  Attempts to modify the returned
     * sorted map, whether direct, via its collection views, or via its
     * <tt>subMap</tt>, <tt>headMap</tt>, or <tt>tailMap</tt> views, result in
     * an <tt>UnsupportedOperationException</tt>.<p>
     *
     * The returned sorted map will be serializable if the specified sorted map
     * is serializable. 
     *
     * @param m the sorted map for which an unmodifiable view is to be
     *        returned. 
     * @return an unmodifiable view of the specified sorted map.
     */
    public static SortedMap unmodifiableSortedMap(SortedMap m) {
	return new UnmodifiableSortedMap(m);
    }

    static class UnmodifiableSortedMap extends UnmodifiableMap
    				 implements SortedMap, Serializable {
        private SortedMap sm;

	UnmodifiableSortedMap(SortedMap m) {super(m); sm = m;}

        public Comparator comparator()     {return sm.comparator();}

        public SortedMap subMap(Object fromKey, Object toKey) {
            return new UnmodifiableSortedMap(sm.subMap(fromKey, toKey));
        }
        public SortedMap headMap(Object toKey) {
            return new UnmodifiableSortedMap(sm.headMap(toKey));
        }
        public SortedMap tailMap(Object fromKey) {
            return new UnmodifiableSortedMap(sm.tailMap(fromKey));
        }

        public Object firstKey()           {return sm.firstKey();}
        public Object lastKey()            {return sm.lastKey();}
    }


    // Synch Wrappers

    /**
     * Returns a synchronized (thread-safe) collection backed by the specified
     * collection.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing collection is accomplished
     * through the returned collection.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * collection when iterating over it:
     * <pre>
     *  Collection c = Collections.synchronizedCollection(myCollection);
     *     ...
     *  synchronized(c) {
     *      Iterator i = c.iterator(); // Must be in the synchronized block
     *      while (i.hasNext())
     *         foo(i.next());
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned collection does <i>not</i> pass the <tt>hashCode</tt>
     * and <tt>equals</tt> operations through to the backing collection, but
     * relies on <tt>Object</tt>'s equals and hashCode methods.  This is
     * necessary to preserve the contracts of these operations in the case
     * that the backing collection is a set or a list.<p>
     *
     * The returned collection will be serializable if the specified collection
     * is serializable. 
     *
     * @param  c the collection to be "wrapped" in a synchronized collection.
     * @return a synchronized view of the specified collection.
     */
    public static Collection synchronizedCollection(Collection c) {
	return new SynchronizedCollection(c);
    }

    static Collection synchronizedCollection(Collection c, Object mutex) {
	return new SynchronizedCollection(c, mutex);
    }

    static class SynchronizedCollection implements Collection, Serializable {
	Collection c;	   // Backing Collection
	Object	   mutex;  // Object on which to synchronize

	SynchronizedCollection(Collection c) {
	    this.c = c; mutex = this;
        }
	SynchronizedCollection(Collection c, Object mutex) {
	    this.c = c; this.mutex = mutex;
        }

	public int size() {
	    synchronized(mutex) {return c.size();}
        }
	public boolean isEmpty() {
	    synchronized(mutex) {return c.isEmpty();}
        }
	public boolean contains(Object o) {
	    synchronized(mutex) {return c.contains(o);}
        }
	public Object[] toArray() {
	    synchronized(mutex) {return c.toArray();}
        }
	public Object[] toArray(Object[] a) {
	    synchronized(mutex) {return c.toArray(a);}
        }

	public Iterator iterator() {
            return c.iterator(); // Must be manually synched by user!
        }

	public boolean add(Object o) {
	    synchronized(mutex) {return c.add(o);}
        }
	public boolean remove(Object o) {
	    synchronized(mutex) {return c.remove(o);}
        }

	public boolean containsAll(Collection coll) {
	    synchronized(mutex) {return c.containsAll(coll);}
        }
	public boolean addAll(Collection coll) {
	    synchronized(mutex) {return c.addAll(coll);}
        }
	public boolean removeAll(Collection coll) {
	    synchronized(mutex) {return c.removeAll(coll);}
        }
	public boolean retainAll(Collection coll) {
	    synchronized(mutex) {return c.retainAll(coll);}
        }
	public void clear() {
	    synchronized(mutex) {c.clear();}
        }
    }

    /**
     * Returns a synchronized (thread-safe) set backed by the specified
     * set.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing set is accomplished
     * through the returned set.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * set when iterating over it:
     * <pre>
     *  Set s = Collections.synchronizedSet(new HashSet());
     *      ...
     *  synchronized(s) {
     *      Iterator i = s.iterator(); // Must be in the synchronized block
     *      while (i.hasNext())
     *          foo(i.next());
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned set will be serializable if the specified set is
     * serializable.
     *
     * @param  s the set to be "wrapped" in a synchronized set.
     * @return a synchronized view of the specified set.
     */
    public static Set synchronizedSet(Set s) {
	return new SynchronizedSet(s);
    }

    static Set synchronizedSet(Set s, Object mutex) {
	return new SynchronizedSet(s, mutex);
    }

    static class SynchronizedSet extends SynchronizedCollection
			         implements Set {
	SynchronizedSet(Set s) {
            super(s);
        }
	SynchronizedSet(Set s, Object mutex) {
            super(s, mutex);
        }

	public boolean equals(Object o) {
	    synchronized(mutex) {return c.equals(o);}
        }
	public int hashCode() {
	    synchronized(mutex) {return c.hashCode();}
        }
    }

    /**
     * Returns a synchronized (thread-safe) sorted set backed by the specified
     * sorted set.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing sorted set is accomplished
     * through the returned sorted set (or its views).<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * sorted set when iterating over it or any of its <tt>subSet</tt>,
     * <tt>headSet</tt>, or <tt>tailSet</tt> views.
     * <pre>
     *  SortedSet s = Collections.synchronizedSortedSet(new HashSortedSet());
     *      ...
     *  synchronized(s) {
     *      Iterator i = s.iterator(); // Must be in the synchronized block
     *      while (i.hasNext())
     *          foo(i.next());
     *  }
     * </pre>
     * or:
     * <pre>
     *  SortedSet s = Collections.synchronizedSortedSet(new HashSortedSet());
     *  SortedSet s2 = s.headSet(foo);
     *      ...
     *  synchronized(s) {  // Note: s, not s2!!!
     *      Iterator i = s2.iterator(); // Must be in the synchronized block
     *      while (i.hasNext())
     *          foo(i.next());
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned sorted set will be serializable if the specified
     * sorted set is serializable.
     *
     * @param  s the sorted set to be "wrapped" in a synchronized sorted set.
     * @return a synchronized view of the specified sorted set.
     */
    public static SortedSet synchronizedSortedSet(SortedSet s) {
	return new SynchronizedSortedSet(s);
    }

    static class SynchronizedSortedSet extends SynchronizedSet
			         implements SortedSet
    {
        private SortedSet ss;

	SynchronizedSortedSet(SortedSet s) {
            super(s);
            ss = s;
        }
	SynchronizedSortedSet(SortedSet s, Object mutex) {
            super(s, mutex);
            ss = s;
        }

	public Comparator comparator() {
	    synchronized(mutex) {return ss.comparator();}
        }

        public SortedSet subSet(Object fromElement, Object toElement) {
	    synchronized(mutex) {
                return new SynchronizedSortedSet(
                    ss.subSet(fromElement, toElement), mutex);
            }
        }
        public SortedSet headSet(Object toElement) {
	    synchronized(mutex) {
                return new SynchronizedSortedSet(ss.headSet(toElement), mutex);
            }
        }
        public SortedSet tailSet(Object fromElement) {
	    synchronized(mutex) {
               return new SynchronizedSortedSet(ss.tailSet(fromElement),mutex);
            }
        }

        public Object first() {
	    synchronized(mutex) {return ss.first();}
        }
        public Object last() {
	    synchronized(mutex) {return ss.last();}
        }
    }

    /**
     * Returns a synchronized (thread-safe) list backed by the specified
     * list.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing list is accomplished
     * through the returned list.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * list when iterating over it:
     * <pre>
     *  List list = Collections.synchronizedList(new ArrayList());
     *      ...
     *  synchronized(list) {
     *      Iterator i = list.iterator(); // Must be in synchronized block
     *      while (i.hasNext())
     *          foo(i.next());
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned list will be serializable if the specified list is
     * serializable.
     *
     * @param  list the list to be "wrapped" in a synchronized list.
     * @return a synchronized view of the specified list.
     */
    public static List synchronizedList(List list) {
	return new SynchronizedList(list);
    }

    static List synchronizedList(List list, Object mutex) {
	return new SynchronizedList(list, mutex);
    }

    static class SynchronizedList extends SynchronizedCollection
    			          implements List {
	private List list;

	SynchronizedList(List list) {
	    super(list);
	    this.list = list;
	}
	SynchronizedList(List list, Object mutex) {
            super(list, mutex);
	    this.list = list;
        }

	public boolean equals(Object o) {
	    synchronized(mutex) {return list.equals(o);}
        }
	public int hashCode() {
	    synchronized(mutex) {return list.hashCode();}
        }

	public Object get(int index) {
	    synchronized(mutex) {return list.get(index);}
        }
	public Object set(int index, Object element) {
	    synchronized(mutex) {return list.set(index, element);}
        }
	public void add(int index, Object element) {
	    synchronized(mutex) {list.add(index, element);}
        }
	public Object remove(int index) {
	    synchronized(mutex) {return list.remove(index);}
        }

	public int indexOf(Object o) {
	    synchronized(mutex) {return list.indexOf(o);}
        }
	public int lastIndexOf(Object o) {
	    synchronized(mutex) {return list.lastIndexOf(o);}
        }

	public boolean addAll(int index, Collection c) {
	    synchronized(mutex) {return list.addAll(index, c);}
        }

	public ListIterator listIterator() {
	    return list.listIterator(); // Must be manually synched by user
        }

	public ListIterator listIterator(int index) {
	    return list.listIterator(index); // Must be manually synched by usr
        }

	public List subList(int fromIndex, int toIndex) {
	    synchronized(mutex) {
                return new SynchronizedList(list.subList(fromIndex, toIndex),
                                            mutex);
            }
        }
    }

    /**
     * Returns a synchronized (thread-safe) map backed by the specified
     * map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing map is accomplished
     * through the returned map.<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * map when iterating over any of its collection views:
     * <pre>
     *  Map m = Collections.synchronizedMap(new HashMap());
     *      ...
     *  Set s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized(m) {  // Synchronizing on m, not s!
     *      Iterator i = s.iterator(); // Must be in synchronized block
     *      while (i.hasNext())
     *          foo(i.next());
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param  m the map to be "wrapped" in a synchronized map.
     * @return a synchronized view of the specified map.
     */
    public static Map synchronizedMap(Map m) {
	return new SynchronizedMap(m);
    }

    private static class SynchronizedMap implements Map, Serializable {
	private Map m;	        // Backing Map
        Object      mutex;	// Object on which to synchronize

	SynchronizedMap(Map m) {
            this.m = m;	 mutex = this;
        }

	SynchronizedMap(Map m, Object mutex) {
            this.m = m;	 this.mutex = mutex;
        }

	public int size() {
	    synchronized(mutex) {return m.size();}
        }
	public boolean isEmpty(){
	    synchronized(mutex) {return m.isEmpty();}
        }
	public boolean containsKey(Object key) {
	    synchronized(mutex) {return m.containsKey(key);}
        }
	public boolean containsValue(Object value){
	    synchronized(mutex) {return m.containsValue(value);}
        }
	public Object get(Object key) {
	    synchronized(mutex) {return m.get(key);}
        }

	public Object put(Object key, Object value) {
	    synchronized(mutex) {return m.put(key, value);}
        }
	public Object remove(Object key) {
	    synchronized(mutex) {return m.remove(key);}
        }
	public void putAll(Map map) {
	    synchronized(mutex) {m.putAll(map);}
        }
	public void clear() {
	    synchronized(mutex) {m.clear();}
        }

	private transient Set keySet = null;
	private transient Set entrySet = null;
	private transient Collection values = null;

	public Set keySet() {
            synchronized(mutex) {
                if (keySet==null)
                    keySet = new SynchronizedSet(m.keySet(), this);
                return keySet;
            }
	}

	public Set entrySet() {
            synchronized(mutex) {
                if (entrySet==null)
                    entrySet = new SynchronizedSet(m.entrySet(), this);
                return entrySet;
            }
	}

	public Collection values() {
            synchronized(mutex) {
                if (values==null)
                    values = new SynchronizedCollection(m.values(), this);
                return values;
            }
        }

	public boolean equals(Object o) {
            synchronized(mutex) {return m.equals(o);}
        }
	public int hashCode() {
            synchronized(mutex) {return m.hashCode();}
        }
    }

    /**
     * Returns a synchronized (thread-safe) sorted map backed by the specified
     * sorted map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing sorted map is accomplished
     * through the returned sorted map (or its views).<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * sorted map when iterating over any of its collection views, or the
     * collections views of any of its <tt>subMap</tt>, <tt>headMap</tt> or
     * <tt>tailMap</tt> views.
     * <pre>
     *  SortedMap m = Collections.synchronizedSortedMap(new HashSortedMap());
     *      ...
     *  Set s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized(m) {  // Synchronizing on m, not s!
     *      Iterator i = s.iterator(); // Must be in synchronized block
     *      while (i.hasNext())
     *          foo(i.next());
     *  }
     * </pre>
     * or:
     * <pre>
     *  SortedMap m = Collections.synchronizedSortedMap(new HashSortedMap());
     *  SortedMap m2 = m.subMap(foo, bar);
     *      ...
     *  Set s2 = m2.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized(m) {  // Synchronizing on m, not m2 or s2!
     *      Iterator i = s.iterator(); // Must be in synchronized block
     *      while (i.hasNext())
     *          foo(i.next());
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned sorted map will be serializable if the specified
     * sorted map is serializable.
     *
     * @param  m the sorted map to be "wrapped" in a synchronized sorted map.
     * @return a synchronized view of the specified sorted map.
     */
    public static SortedMap synchronizedSortedMap(SortedMap m) {
	return new SynchronizedSortedMap(m);
    }


    static class SynchronizedSortedMap extends SynchronizedMap
			         implements SortedMap
    {
        private SortedMap sm;

	SynchronizedSortedMap(SortedMap m) {
            super(m);
            sm = m;
        }
	SynchronizedSortedMap(SortedMap m, Object mutex) {
            super(m, mutex);
            sm = m;
        }

	public Comparator comparator() {
	    synchronized(mutex) {return sm.comparator();}
        }

        public SortedMap subMap(Object fromKey, Object toKey) {
	    synchronized(mutex) {
                return new SynchronizedSortedMap(
                    sm.subMap(fromKey, toKey), mutex);
            }
        }
        public SortedMap headMap(Object toKey) {
	    synchronized(mutex) {
                return new SynchronizedSortedMap(sm.headMap(toKey), mutex);
            }
        }
        public SortedMap tailMap(Object fromKey) {
	    synchronized(mutex) {
               return new SynchronizedSortedMap(sm.tailMap(fromKey),mutex);
            }
        }

        public Object firstKey() {
	    synchronized(mutex) {return sm.firstKey();}
        }
        public Object lastKey() {
	    synchronized(mutex) {return sm.lastKey();}
        }
    }


    // Miscellaneous

    /**
     * The empty set (immutable).  This set is serializable.
     */
    public static final Set EMPTY_SET = new AbstractSet() {
        public Iterator iterator() {
            return new Iterator() {
                public boolean hasNext() {
                    return false;
                }
                public Object next() {
                    throw new NoSuchElementException();
                }
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }

        public int size() {return 0;}

        public boolean contains(Object obj) {return false;}
    };

    /**
     * The empty list (immutable).  This list is serializable.
     */
    public static final List EMPTY_LIST = new AbstractList() {
        public int size() {return 0;}

        public boolean contains(Object obj) {return false;}

        public Object get(int index) {
            throw new IndexOutOfBoundsException("Index: "+index);
        }
    };

    /**
     * Returns an immutable set containing only the specified object.
     * The returned set is serializable.
     *
     * @return an immutable set containing only the specified object.
     */
    public static Set singleton(final Object o) {
	return new AbstractSet() {
            public Iterator iterator() {
                return new Iterator() {
                    private boolean hasNext = true;
                    public boolean hasNext() {
                        return hasNext;
                    }
                    public Object next() {
                        if (hasNext) {
                            hasNext = false;
                            return o;
                        }
                        throw new NoSuchElementException();
                    }
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }

	    public int size() {return 1;}

	    public boolean contains(Object obj) {return eq(obj, o);}
        };
    }

    /**
     * Returns an immutable list consisting of <tt>n</tt> copies of the
     * specified object.  The newly allocated data object is tiny (it contains
     * a single reference to the data object).  This method is useful in
     * combination with the <tt>List.addAll</tt> method to grow lists.
     * The returned list is serializable.
     *
     * @param  n the number of elements in the returned list.
     * @param  o the element to appear repeatedly in the returned list.
     * @return an immutable list consisting of <tt>n</tt> copies of the
     * 	       specified object.
     * @exception IllegalArgumentException if n &lt; 0.
     * @see    List#addAll(Collection)
     * @see    List#addAll(int, Collection)
     */
    public static List nCopies(final int n, final Object o) {
	if (n < 0)
	    throw new IllegalArgumentException("List length = " + n);

	return new AbstractList() {
	    public int size() {
		return n;
	    }

	    public boolean contains(Object obj) {
		return n != 0 && eq(obj, o);
	    }

	    public Object get(int index) {
		if (index<0 || index>=n)
		    throw new IndexOutOfBoundsException("Index: "+index+
							", Size: "+n);
		return o;
	    }
        };
    }

    /**
     * Returns a comparator that imposes the reverse of the <i>natural
     * ordering</i> on a collection of objects that implement the
     * <tt>Comparable</tt> interface.  (The natural ordering is the ordering
     * imposed by the objects' own <tt>compareTo</tt> method.)  This enables a
     * simple idiom for sorting (or maintaining) collections (or arrays) of
     * objects that implement the <tt>Comparable</tt> interface in
     * reverse-natural-order.  For example, suppose a is an array of
     * strings. Then: <pre>
     * 		Arrays.sort(a, Collections.reverseOrder());
     * </pre> sorts the array in reverse-lexicographic (alphabetical) order.<p>
     *
     * The returned comparator is serializable.
     *
     * @return a comparator that imposes the reverse of the <i>natural
     * 	       ordering</i> on a collection of objects that implement
     *	       the <tt>Comparable</tt> interface.
     * @see Comparable
     */
    public static Comparator reverseOrder() {
        return REVERSE_ORDER;
    }

    private static final Comparator REVERSE_ORDER = new ReverseComparator();

    private static class ReverseComparator implements Comparator,Serializable {
        public int compare(Object o1, Object o2) {
            Comparable c1 = (Comparable)o1;
            Comparable c2 = (Comparable)o2;
            return -c1.compareTo(c2);
        }
    }

    /**
     * Returns an enumeration over the specified collection.  This provides
     * interoperatbility with legacy APIs that require an enumeration
     * as input.
     *
     * @param c the collection for which an enumeration is to be returned.
     * @return an enumeration over the specified collection.
     */
    public static Enumeration enumeration(final Collection c) {
	return new Enumeration() {
	    Iterator i = c.iterator();

	    public boolean hasMoreElements() {
		return i.hasNext();
	    }

	    public Object nextElement() {
		return i.next();
	    }
        };
    }

    /**
     * Returns true if the specified arguments are equal, or both null.
     */
    private static boolean eq(Object o1, Object o2) {
        return (o1==null ? o2==null : o1.equals(o2));
    }
}
