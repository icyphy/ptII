/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.util;
import java.util.Iterator;

/**
 * An iterator that takes another iterator, and applies a filter
 * to each element that it gets.
 *
 * @author John Reekie      (johnr@eecs.berkeley.edu)
 * @version $Revision$
 */
public class FilteredIterator extends IteratorAdapter {
    private Filter _filter;
    private Iterator _iterator;
    private Object _nextObject = null;

    public FilteredIterator(Iterator i, Filter f) {
        _iterator = i;
        _filter = f;
    }

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


