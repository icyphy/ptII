/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */

package diva.util;
import java.util.Iterator;

/**
 * An iterator that takes another iterator, and iterates
 * over it.  This meant to be extended by over-riding the
 * next() method.
 *
 * @author Michael Shilman     (michaels@eecs.berkeley.edu)
 * @version $Revision$
 */
public class ProxyIterator extends IteratorAdapter {
    private Iterator _iterator;

    public ProxyIterator(Iterator i) {
        _iterator = i;
    }

    public boolean hasNext() {
        return _iterator.hasNext();
    }

    public Object next() {
        return _iterator.next();
    }
}


