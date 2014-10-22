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
 * @version $Id$
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
    @Override
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
