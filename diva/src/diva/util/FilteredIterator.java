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

/**
 * An iterator that takes another iterator, and applies a filter
 * to each element that it gets.
 *
 * @author John Reekie
 * @version $Id$
 */
public class FilteredIterator extends IteratorAdapter {
    private Filter _filter;

    private Iterator _iterator;

    private Object _nextObject = null;

    public FilteredIterator(Iterator i, Filter f) {
        _iterator = i;
        _filter = f;
    }

    @Override
    public boolean hasNext() {
        if (_nextObject != null) {
            return true;
        } else {
            getNext();

            if (_nextObject != null) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Object next() {
        if (_nextObject == null) {
            getNext();
        }

        Object result = _nextObject;
        _nextObject = null;
        return result;
    }

    private void getNext() {
        while (_iterator.hasNext()) {
            Object o = _iterator.next();

            if (_filter.accept(o)) {
                _nextObject = o;
                break;
            }
        }
    }
}
