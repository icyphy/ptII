/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.util;

/**
 * An iterator over an array, in which elements are filtered
 * by some function. To make this efficient, the filter is specified
 * by overriding the accept() method in a subclass rather than
 * by passing an instance of the Filter class.
 *
 * A null value (of the whole array) is treated as an empty array.
 *
 * This class can be subclassed to implement the remove() method.
 * The method getLastIndex() return the index of the previously
 * returned element.
 *
 * @author John Reekie
 * @version $Revision$
 */
public abstract class FilteredArrayIterator extends NullArrayIterator {

    public FilteredArrayIterator(Object[] array) {
        super(array);
    }

    public FilteredArrayIterator(Object[] array, int length) {
        super(array, length);
    }

    /** Test if the object is acceptable for return by the iterator.
     */
    public abstract boolean accept(Object o);

    /** Advance the next index to the next non-null element. Set it
     * to -1 if there are no more elements.
     */
    protected void advance() {
        _nextindex++;
        while (_nextindex < _arraylen && !accept(_array[_nextindex])) {
            _nextindex++;
        }
        if (_nextindex == _arraylen) {
            _nextindex = -1;
        }
    }
}
