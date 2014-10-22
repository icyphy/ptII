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
 * An iterator that implements the Iterator, intended for
 * subclassing so that you don't have to provide the remove()
 * method all the time....
 *
 * @author John Reekie
 * @author Michael Shilman
 * @version $Id$
 */
public class IteratorAdapter implements Iterator {
    @Override
    public boolean hasNext() {
        throw new UnsupportedOperationException(
                "This method must be overridden");
    }

    @Override
    public Object next() throws NoSuchElementException {
        // FindBugs: throw NoSuchElementException here.
        throw new NoSuchElementException("This method must be overridden");
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Can't remove element");
    }
}
