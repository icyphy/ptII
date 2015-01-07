/*
 Copyright (c) 1998-2014 The Regents of the University of California
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
 PROVIDED HEREUNDER IS ON AN  BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY
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
 * @version $Id$
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
        advance();
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
    @Override
    public boolean hasNext() {
        return _nextindex >= 0;
    }

    /** Return the next non-null element in the array.
     */
    @Override
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
