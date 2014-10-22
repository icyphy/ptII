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

import java.util.List;
import java.util.NoSuchElementException;

/**
 * A reverse-order iterator over a List.
 *
 * @author John Reekie
 * @version $Id$
 */
public class ReverseIterator extends IteratorAdapter {
    private List _list;

    private int _cursor;

    /** Construct a reverse iterator on the given list.
     *  @param list The list with which to construct the iterator.
     */
    public ReverseIterator(List list) {
        _list = list;
        _cursor = list.size();
    }

    /** Test if there are more elements.
     *  @return true if there more elements.
     */
    @Override
    public boolean hasNext() {
        return _cursor > 0;
    }

    /** Return the next element.
     *  @return the next element.
     *  @exception NoSuchElementException If the element does not exist.
     */
    @Override
    public Object next() throws NoSuchElementException {
        if (!hasNext()) {
            throw new NoSuchElementException("Can't get " + _cursor
                    + "'th element from ReverseIterator of size "
                    + _list.size());
        }
        _cursor--;
        return _list.get(_cursor);
    }
}
