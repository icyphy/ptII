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
package diva.canvas.interactor;

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.event.EventListenerList;

import diva.canvas.Figure;

/**
 * A basic implementation of the SelectionModel interface.
 * This model requires that each object in the selection be
 * an instance of Figure. When each item is added to the selection,
 * the model gets its interactor, and if it is an
 * instance of SelectionInteractor, gets a selection renderer
 * from the interactor and uses it to highlight the selected
 * figures.
 *
 * @author         Michael Shilman
 * @version        $Id$
 */
public class BasicSelectionModel implements SelectionModel {
    /** The selection mode -- single or multiple selection
     */
    private int _mode = MULTIPLE_SELECTION;

    /**
     * The list of event listeners.
     */
    private EventListenerList _listeners = null;

    /**
     * The current selection.
     */
    private ArrayList _selection = null;

    /**
     * A graph selection event, reused for efficiency.
     */
    private SelectionEvent _selEvent = new SelectionEvent(this);

    /**
     * Construct an empty selection.
     */
    public BasicSelectionModel() {
        _selection = new ArrayList();
        _listeners = new EventListenerList();
    }

    /**
     * Add a selection listener to this model.
     */
    @Override
    public void addSelectionListener(SelectionListener l) {
        _listeners.add(SelectionListener.class, l);
    }

    /**
     * Add an object to the selection. The model highlights
     * the selection.  If the given object is null, then it is not
     * added to the selection, but if the selection model is a single
     * selection, the current selection is cleared.
     */
    @Override
    public void addSelection(Object sel) {
        Object[] additions = null;
        Object[] removals = null;

        if (getSelectionMode() == SINGLE_SELECTION) {
            removals = _selection.toArray();
            clearSelection();
        }

        if (sel == null) {
            additions = new Object[0];
        } else {
            _selection.add(sel);
            renderSelected(sel);
            additions = new Object[1];
            additions[0] = sel;
        }

        _selEvent.set(additions, removals, getFirstSelection());
        dispatchSelectionEvent(_selEvent);
    }

    /**
     * Add an array of objects to the selection and
     * highlight the selected objects.
     */
    @Override
    public void addSelections(Object[] sels) {
        if (getSelectionMode() == SINGLE_SELECTION) {
            //only add the last selection
            addSelection(sels[sels.length - 1]);
        } else {
            for (Object sel : sels) {
                _selection.add(sel);
                renderSelected(sel);
            }

            _selEvent.set(sels, null, getFirstSelection());
            dispatchSelectionEvent(_selEvent);
        }
    }

    /**
     * Clear the selection. The model should remove highlighting from
     * the previously selected objects.
     */
    @Override
    public void clearSelection() {
        Object[] removals = _selection.toArray();
        _selection.clear();

        for (Object removal : removals) {
            renderDeselected(removal);
        }

        _selEvent.set(null, removals, getFirstSelection());
        dispatchSelectionEvent(_selEvent);
    }

    /**
     * Test if the selection contains the given object
     */
    @Override
    public boolean containsSelection(Object sel) {
        return _selection.contains(sel);
    }

    /** Dispatch a selection event to all registered listeners
     */
    public void dispatchSelectionEvent(SelectionEvent e) {
        Object[] listeners = _listeners.getListenerList();

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == SelectionListener.class) {
                ((SelectionListener) listeners[i + 1]).selectionChanged(e);
            }
        }
    }

    /**
     * Return the first selection in the list.
     */
    @Override
    public Object getFirstSelection() {
        if (_selection.size() == 0) {
            return null;
        }

        return _selection.get(0);
    }

    /**
     * Return the last selection in the list.
     */
    @Override
    public Object getLastSelection() {
        if (_selection.size() == 0) {
            return null;
        }

        return _selection.get(_selection.size() - 1);
    }

    /**
     * Return an iterator over the selected objects.
     */
    @Override
    public Iterator getSelection() {
        return _selection.iterator();
    }

    /**
     * Return the contents of the selection as an array.
     * This method is typically used by clients that
     * need to pass the selection contents to another object
     * that does not know about selection models, and which needs
     * to traverse the selection contents multiple times.
     */
    @Override
    public Object[] getSelectionAsArray() {
        return _selection.toArray();
    }

    /**
     * Return the number of selected objects.
     */
    @Override
    public int getSelectionCount() {
        return _selection.size();
    }

    /**
     * Return the mode of the selection, either
     * SINGLE_SELECTION or MULTIPLE_SELECTION.
     */
    @Override
    public int getSelectionMode() {
        return _mode;
    }

    /**
     * Remove an object from the selection.
     */
    @Override
    public void removeSelection(Object sel) {
        Object[] removals = new Object[1];
        removals[0] = sel;
        _selection.remove(sel);
        renderDeselected(sel);

        _selEvent.set(null, removals, getFirstSelection());
        dispatchSelectionEvent(_selEvent);
    }

    /**
     * Remove a listener from the list of listeners.
     */
    @Override
    public void removeSelectionListener(SelectionListener l) {
        _listeners.remove(SelectionListener.class, l);
    }

    /** Set the rendering of the object as deselected.
     * This works only if it is an instance of Figure,
     * and it has a SelectionInteractor attached to it.
     */
    private void renderDeselected(Object o) {
        if (o instanceof Figure) {
            Interactor ir = ((Figure) o).getInteractor();

            if (ir instanceof SelectionInteractor) {
                SelectionRenderer sr = ((SelectionInteractor) ir)
                        .getSelectionRenderer();

                if (sr != null) {
                    sr.renderDeselected((Figure) o);
                }
            }
        }
    }

    /** Set the rendering of the object as selected.
     * This works only if it is an instance of Figure,
     * and it has a SelectionInteractor.
     */
    private void renderSelected(Object o) {
        if (o instanceof Figure) {
            Interactor ir = ((Figure) o).getInteractor();

            if (ir instanceof SelectionInteractor) {
                SelectionRenderer sr = ((SelectionInteractor) ir)
                        .getSelectionRenderer();

                if (sr != null) {
                    sr.renderSelected((Figure) o);
                }
            }
        }
    }

    /**
     * Set the selection mode, either
     * SINGLE_SELECTION or MULTIPLE_SELECTION.
     */
    @Override
    public void setSelectionMode(int mode) {
        _mode = mode;
    }
}
