/*
 * @(#)ConcurrentModificationException.java	1.6 98/09/30
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
 * This exception may be thrown by methods that have detected concurrent
 * modification of a backing object when such modification is not permissible.
 * <p>
 * For example, it is not permssible for one thread to modify a Collection
 * while another thread is iterating over it.  In general, the results of the
 * iteration are undefined under these circumstances.  Some Iterator
 * implementations (including those of all the collection implementations
 * provided by the JDK) may choose to throw this exception if this behavior is
 * detected.  Iterators that do this are known as <i>fail-fast</i> iterators,
 * as they fail quickly and cleanly, rather that risking arbitrary,
 * non-deterministic behavior at an undetermined time in the future.
 *
 * @author  Josh Bloch
 * @version 1.6, 09/30/98
 * @see	    Collection
 * @see     Iterator
 * @see     ListIterator
 * @see	    Vector
 * @see	    LinkedList
 * @see	    HashSet
 * @see	    Hashtable
 * @see	    TreeMap
 * @see	    AbstractList
 * @since   JDK1.2
 */
public class ConcurrentModificationException extends RuntimeException {
    /**
     * Constructs a ConcurrentModificationException with no
     * detail message.
     */
    public ConcurrentModificationException() {
    }

    /**
     * Constructs a <tt>ConcurrentModificationException</tt> with the
     * specified detail message.
     */
    public ConcurrentModificationException(String message) {
	super(message);
    }
}
