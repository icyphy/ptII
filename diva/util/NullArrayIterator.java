/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.util;
import java.util.NoSuchElementException;

/**
 * An iterator over a given array which may contain nulls. Any
 * element of the array that is null is not returned, but is
 * skipped over.
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
public class NullArrayIterator extends IteratorAdapter {

    Object[] _array;
    int _lastindex = -1;
    int _nextindex = -1;
    int _arraylen;

    /** Construct an iterator over the given array
     */
    public NullArrayIterator(Object[] array) {
        _array = array;
        _arraylen = array.length;
        if (array != null) {
            advance();
        }
    }

    /** Construct an iterator over the given array, where the
     * effective array length is given by the length argument
     * (and must be smaller than the real length of the array).
     */
    public NullArrayIterator(Object[] array, int length) {
        _array = array;
        _arraylen = Math.min(length, array.length);
        advance();
    }

    /** Advance the next index to the next non-null element. Set it
     * to -1 if there are no more elements.
     */
    protected void advance() {
        _nextindex++;
        while (_nextindex < _arraylen && _array[_nextindex] == null) {
            _nextindex++;
        }
        if (_nextindex == _arraylen) {
            _nextindex = -1;
        }
    }

    /** Return the index of the element last returned. This will be
     * -1 if next() hasn't been called yet.
     */
    protected int getLastIndex() {
        return _lastindex;
    }

    /** Return true if there are more non-null elements in the array.
     */
    public boolean hasNext() {
        return _nextindex >= 0;
    }

    /** Return the next non-null element in the array.
     */
    public Object next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more elements");
        }
        Object result = _array[_nextindex];
        _lastindex = _nextindex;
        advance();
        return result;
    }
}
