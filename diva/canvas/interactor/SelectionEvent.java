/*
  Copyright (c) 1998-2005 The Regents of the University of California
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
package diva.canvas.interactor;

import diva.util.IteratorAdapter;
import diva.util.NullIterator;

import java.util.Iterator;


/**
 * An event representing a change in the graph selection
 * model. The event contains all the information to mirror
 * the selection model.
 *
 * @author         Michael Shilman
 * @version        $Id$
 */
public class SelectionEvent extends java.util.EventObject {
    /**
     * The objects added to the selection.
     * @serial
     */
    private Object[] _added;

    /**
     * The objects removed from the selection.
     * @serial
     */
    private Object[] _removed;

    /**
     * The primary selected object.
     * @serial
     */
    private Object _primary;

    /**
     * Construct a new Selection event from the
     * given source, representing the given selection
     * additions, removals, and primary selection.
     */
    public SelectionEvent(Object source, Object[] added, Object[] removed,
        Object primary) {
        this(source);
        set(added, removed, primary);
    }

    /**
     * Construct an empty Selection event from the
     * given source.
     */
    SelectionEvent(Object source) {
        super(source);
    }

    /**
     * Return an iterator over the objects
     * added to the selection model.
     */
    public Iterator getSelectionAdditions() {
        if (_added == null) {
            return new NullIterator();
        } else {
            return new IteratorAdapter() {
                    int i = 0;

                    public boolean hasNext() {
                        return (i < _added.length);
                    }

                    public Object next() {
                        return _added[i++];
                    }
                };
        }
    }

    /**
     * Return an iterator over the objects
     * removed from the selection model.
     */
    public Iterator getSelectionRemovals() {
        if (_removed == null) {
            return new NullIterator();
        } else {
            return new IteratorAdapter() {
                    int i = 0;

                    public boolean hasNext() {
                        return (i < _removed.length);
                    }

                    public Object next() {
                        return _removed[i++];
                    }
                };
        }
    }

    /**
     * Return the primary selection object.
     */
    public Object getPrimarySelection() {
        return _primary;
    }

    /**
     * Set the contents of the selection event.
     */
    void set(Object[] added, Object[] removed, Object primary) {
        _added = added;
        _removed = removed;
        _primary = primary;
    }
}
