/*
 * @(#)AbstractSet.java	1.8 98/09/30
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
 * This class provides a skeletal implementation of the <tt>Set</tt>
 * interface to minimize the effort required to implement this
 * interface. <p>
 *
 * The process of implementing a set by extending this class is identical
 * to that of implementing a Collection by extending AbstractCollection,
 * except that all of the methods and constructors in subclasses of this
 * class must obey the additional constraints imposed by the <tt>Set</tt>
 * interface (for instance, the add method must not permit addition of
 * multiple intances of an object to a set).<p>
 *
 * Note that this class does not override any of the implementations from
 * the <tt>AbstractCollection</tt> class.  It merely adds implementations
 * for <tt>equals</tt> and <tt>hashCode</tt>.
 *
 * @author  Josh Bloch
 * @version 1.8 09/30/98
 * @see Collection
 * @see AbstractCollection
 * @see Set
 * @since JDK1.2
 */

public abstract class AbstractSet extends AbstractCollection implements Set {
    /**
     * Sole constructor.  (For invocation by subclass constructors, typically
     * implicit.)
     */
    protected AbstractSet() {
    }

    // Comparison and hashing

    /**
     * Compares the specified object with this set for equality.  Returns
     * <tt>true</tt> if the given object is also a set, the two sets have
     * the same size, and every member of the given set is contained in
     * this set.  This ensures that the <tt>equals</tt> method works
     * properly across different implementations of the <tt>Set</tt>
     * interface.<p>
     *
     * This implementation first checks if the specified object is this
     * set; if so it returns <tt>true</tt>.  Then, it checks if the
     * specified object is a set whose size is identical to the size of
     * this set; if not, it it returns false.  If so, it returns
     * <tt>containsAll((Collection) o)</tt>.
     *
     * @param o Object to be compared for equality with this set.
     * @return <tt>true</tt> if the specified object is equal to this set.
     */
    public boolean equals(Object o) {
	if (o == this)
	    return true;

	if (!(o instanceof Set))
	    return false;
	Collection c = (Collection) o;
	if (c.size() != size())
	    return false;
	return containsAll(c);
    }

    /**
     * Returns the hash code value for this set.  The hash code of a set is
     * defined to be the sum of the hash codes of the elements in the set.
     * This ensures that <tt>s1.equals(s2)</tt> implies that
     * <tt>s1.hashCode()==s2.hashCode()</tt> for any two sets <tt>s1</tt>
     * and <tt>s2</tt>, as required by the general contract of
     * Object.hashCode.<p>
     *
     * This implementation enumerates over the set, calling the
     * <tt>hashCode</tt> method on each element in the collection, and
     * adding up the results.
     *
     * @returns the hash code value for this set.
     */
    public int hashCode() {
	int h = 0;
	Iterator i = iterator();
	while (i.hasNext()) {
	    Object obj = i.next();
            if (obj != null)
                h += obj.hashCode();
        }
	return h;
    }
}
