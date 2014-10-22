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
 * An iterator over two iterators.
 *
 * @author John Reekie
 * @version $Id$
 */
public class CompoundIterator implements Iterator {
    // The iterators
    private Iterator _first;

    private Iterator _second;

    private boolean _onFirst = true;

    /**
     * Create a new iterator
     */
    public CompoundIterator(Iterator first, Iterator second) {
        this._first = first;
        this._second = second;
    }

    @Override
    public boolean hasNext() {
        if (_onFirst) {
            if (_first.hasNext()) {
                return true;
            } else {
                _onFirst = false;
                return _second.hasNext();
            }
        } else {
            return _second.hasNext();
        }
    }

    @Override
    public Object next() {
        if (_onFirst) {
            if (_first.hasNext()) {
                return _first.next();
            } else {
                _onFirst = false;
                return _second.next();
            }
        } else {
            return _second.next();
        }
    }

    @Override
    public void remove() {
        if (_onFirst) {
            _first.remove();
        } else {
            _second.remove();
        }
    }
}
