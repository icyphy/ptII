/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.util;

import java.util.NoSuchElementException;

/**
 * An iterator over nothing.
 *
 * @author John Reekie      (johnr@eecs.berkeley.edu)
 * @author Michael Shilman  (michaels@eecs.berkeley.edu)
 * @version $Revision$
 */
public class NullIterator extends IteratorAdapter {
    public boolean hasNext() {
        return false;
    }
    public Object next() {
        throw new NoSuchElementException("No more elements");
    }
}
