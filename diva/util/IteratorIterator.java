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

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An iterator that takes an iterator over objects that themselves
 * produce iterators, and which iterators over the elements in
 * the sub-iterators. To get the iterators from the objects
 * iterated over, the method iterator() must be over-ridden to
 * convert a given result from the top-level iterator into a
 * sub-iterator.
 *
 * @author John Reekie
 * @version $Id$
 */
public abstract class IteratorIterator extends IteratorAdapter {
    private Iterator _iterator;

    private Iterator _subiterator = null;

    /* Construct a new iterator over the contents of the given iterator
     */
    public IteratorIterator(Iterator i) {
        _iterator = i;

        if (_iterator.hasNext()) {
            _subiterator = iterator(_iterator.next());
        }
    }

    /* Test if there are more elements. This will return false if
     * the top-level iterator has no more elements.
     */
    @Override
    public boolean hasNext() {
        while (_subiterator != null && !_subiterator.hasNext()) {
            _subiterator = iterator(_iterator.next());
        }
        return _subiterator != null;
    }

    /* Convert an object returned by the top-level iterator
     * into a sub-iterator.
     */
    abstract protected Iterator iterator(Object o);

    /* Return the next object.
     */
    @Override
    public Object next() throws NoSuchElementException {
        if (_subiterator == null) {
            throw new NoSuchElementException("Ack! No more elements");
        }

        while (!_subiterator.hasNext()) {
            _subiterator = iterator(_iterator.next());
        }

        return _subiterator.next();
    }
}
