/*
 * @(#)HashSet.java	1.14 98/09/30
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
 * This class implements the <tt>Set</tt> interface, backed by a hash table
 * (actually a <tt>HashMap</tt> instance).  It makes no guarantees as to the
 * iteration order of the set; in particular, it does not guarantee that the
 * order will remain constant over time.  This class permits the <tt>null</tt>
 * element.<p>
 *
 * This class offers constant time performance for the basic operations
 * (<tt>add</tt>, <tt>remove</tt>, <tt>contains</tt> and <tt>size</tt>),
 * assuming the hash function disperses the elements properly among the
 * buckets.  Iterating over this set requires time proportional to the sum of
 * the <tt>HashSet</tt> instance's size (the number of elements) plus the
 * "capacity" of the backing <tt>HashMap</tt> instance (the number of
 * buckets).  Thus, it's very important not to set the intial capacity too
 * high (or the load factor too low) if iteration performance is important.<p>
 *
 * <b>Note that this implementation is not synchronized.</b> If multiple
 * threads access a set concurrently, and at least one of the threads modifies
 * the set, it <i>must</i> be synchronized externally.  This is typically
 * accomplished by synchronizing on some object that naturally encapsulates
 * the set.  If no such object exists, the set should be "wrapped" using the
 * <tt>Collections.synchronizedSet</tt> method.  This is best done at creation
 * time, to prevent accidental unsynchronized access to the <tt>HashSet</tt>
 * instance:
 * 
 * <pre>
 *     Set s = Collections.synchronizedSet(new HashSet(...));
 * </pre><p>
 *
 * The iterators returned by this class's <tt>iterator</tt> method are
 * <i>fail-fast</i>: if the set is modified at any time after the iterator is
 * created, in any way except through the iterator's own <tt>remove</tt>
 * method, the Iterator throws a <tt>ConcurrentModificationException</tt>.
 * Thus, in the face of concurrent modification, the iterator fails quickly
 * and cleanly, rather than risking arbitrary, non-deterministic behavior at
 * an undetermined time in the future.
 * 
 * @author  Josh Bloch
 * @version 1.14 09/30/98
 * @see	    Collection
 * @see	    Set
 * @see	    TreeSet
 * @see	    Collections#synchronizedSet(Set)
 * @see	    HashMap
 * @since JDK1.2
 */

public class HashSet extends AbstractSet
    implements Set /* KVM_FIXME, Cloneable*/, java.io.Serializable
{
    private transient HashMap map;

    // Dummy value to associate with an Object in the backing Map
    private static final Object PRESENT = new Object();

    /**
     * Constructs a new, empty set; the backing <tt>HashMap</tt> instance has
     * default capacity and load factor, which is <tt>0.75</tt>.
     */
    public HashSet() {
	map = new HashMap();
    }

    /**
     * Constructs a new set containing the elements in the specified
     * collection.  The capacity of the backing <tt>HashMap</tt> instance is
     * twice the size of the specified collection or eleven (whichever is
     * greater), and the default load factor (which is <tt>0.75</tt>) is used.
     */
    public HashSet(Collection c) {
	map = new HashMap(Math.max(2*c.size(), 11));
	addAll(c);
    }

    /**
     * Constructs a new, empty set; the backing <tt>HashMap</tt> instance has
     * the specified initial capacity and the specified load factor.
     *
     * @param      initialCapacity   the initial capacity of the hash map.
     * @param      loadFactor        the load factor of the hash map.
     * @exception     IllegalArgumentException if the initial capacity is less
     *             than zero, or if the load factor is nonpositive.
     */
    public HashSet(int initialCapacity, float loadFactor) {
	map = new HashMap(initialCapacity, loadFactor);
    }

    /**
     * Constructs a new, empty set; the backing <tt>HashMap</tt> instance has
     * the specified initial capacity and default load factor, which is
     * <tt>0.75</tt>.
     *
     * @param      initialCapacity   the initial capacity of the hash table.
     * @exception     IllegalArgumentException if the initial capacity is less
     *             than zero.
     */
    public HashSet(int initialCapacity) {
	map = new HashMap(initialCapacity);
    }

    /**
     * Returns an iterator over the elements in this set.  The elements
     * are returned in no particular order.
     *
     * @return an Iterator over the elements in this set.
     * @see ConcurrentModificationException
     */
    public Iterator iterator() {
	return map.keySet().iterator();
    }

    /**
     * Returns the number of elements in this set (its cardinality).
     *
     * @return the number of elements in this set (its cardinality).
     */
    public int size() {
	return map.size();
    }

    /**
     * Returns <tt>true</tt> if this set contains no elements.
     *
     * @return <tt>true</tt> if this set contains no elements.
     */
    public boolean isEmpty() {
	return map.isEmpty();
    }

    /**
     * Returns <tt>true</tt> if this set contains the specified element.
     *
     * @return <tt>true</tt> if this set contains the specified element.
     */
    public boolean contains(Object o) {
	return map.containsKey(o);
    }

    /**
     * Adds the specified element to this set if it is not already
     * present.
     *
     * @param o element to be added to this set.
     * @return <tt>true</tt> if the set did not already contain the specified
     * element.
     */
    public boolean add(Object o) {
	return map.put(o, PRESENT)==null;
    }

    /**
     * Removes the given element from this set if it is present.
     *
     * @param o object to be removed from this set, if present.
     * @return <tt>true</tt> if the set contained the specified element.
     */
    public boolean remove(Object o) {
	return map.remove(o)==PRESENT;
    }

    /**
     * Removes all of the elements from this set.
     */
    public void clear() {
	map.clear();
    }

    /**
     * Returns a shallow copy of this <tt>HashSet</tt> instance: the elements
     * themselves are not cloned.
     *
     * @return a shallow copy of this set.
     */
    /* KVM_FIXME
    public Object clone() {
	try { 
	    HashSet newSet = (HashSet)super.clone();
	    newSet.map = (HashMap)map.clone();
	    return newSet;
	} catch (CloneNotSupportedException e) { 
	    throw new InternalError();
	}
        }*/

    /**
     * Save the state of this <tt>HashSet</tt> instance to a stream (that is,
     * serialize this set).
     *
     * @serialData The capacity of the backing <tt>HashMap</tt> instance
     *		   (int), and its load factor (float) are emitted, followed by
     *		   the size of the set (the number of elements it contains)
     *		   (int), followed by all of its elements (each an Object) in
     *             no particular order.
     */
    /*KVM_FIXME: no io.ObjectOutputStream
    private synchronized void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException {
	// Write out any hidden serialization magic
	s.defaultWriteObject();

        // Write out HashMap capacity and load factor
        s.writeInt(map.capacity());
        s.writeFloat(map.loadFactor());

        // Write out size
        s.writeInt(map.size());

	// Write out all elements in the proper order.
	for (Iterator i=map.keySet().iterator(); i.hasNext(); )
            s.writeObject(i.next());
	    }*/

    /**
     * Reconstitute the <tt>HashSet</tt> instance from a stream (that is,
     * deserialize it).
     */
    /*KVM_FIXME: no io.ObjectInputStream
    private synchronized void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
	// Read in any hidden serialization magic
	s.defaultReadObject();

        // Read in HashMap capacity and load factor and create backing HashMap
        int capacity = s.readInt();
        float loadFactor = s.readFloat();
        map = new HashMap(capacity, loadFactor);

        // Read in size
        int size = s.readInt();

	// Read in all elements in the proper order.
	for (int i=0; i<size; i++) {
            Object e = s.readObject();
            map.put(e, PRESENT);
        }
    }
    */
}
