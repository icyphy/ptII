/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.canvas.interactor;

import diva.canvas.Figure;
import diva.canvas.interactor.Interactor;

import java.util.*;
import javax.swing.event.EventListenerList;

/**
 * A basic implementation of the SelectionModel interface.
 * This model requires that each object in the selection be
 * an instance of Figure. When each item is added to the selection,
 * the model gets its interactor, and if it is an
 * instance of SelectionInteractor, gets a selection renderer
 * from the interactor and uses it to highlight the selected
 * figures.
 *
 * @author         Michael Shilman (michaels@eecs.berkeley.edu)
 * @version        $Revision$
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
     * Empty array, reused for efficiency.
     */
    private Object[] _dummy = new Object[0];

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
    public void addSelectionListener(SelectionListener l) {
        _listeners.add(SelectionListener.class, l);
    }

    /**
     * Add an object to the selection. The model highlights
     * the selection.  If the given object is null, then it is not
     * added to the selection, but if the selection model is a single
     * selection, the current selection is cleared.
     */
    public void addSelection(Object sel) {
        Object []additions = null;
        Object []removals = null;

        if(getSelectionMode() == SINGLE_SELECTION) {
            removals = _selection.toArray();
            clearSelection();
        }

        if(sel == null) {
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
    public void addSelections(Object[] sels) {
        if(getSelectionMode() == SINGLE_SELECTION) {
            //only add the last selection
            addSelection(sels[sels.length-1]);
        }
        else {
            for(int i = 0; i < sels.length; i++) {
                _selection.add(sels[i]);
                renderSelected(sels[i]);
            }
            _selEvent.set(sels, null, getFirstSelection());
            dispatchSelectionEvent(_selEvent);
        }
    }

    /**
     * Clear the selection. The model should remove highlighting from
     * the previously selected objects.
     */
    public void clearSelection() {
        Object[] removals = _selection.toArray();
        _selection.clear();
        for (int i = 0; i < removals.length; i++) {
            renderDeselected(removals[i]);
        }
        _selEvent.set(null, removals, getFirstSelection());
        dispatchSelectionEvent(_selEvent);
    }

    /**
     * Test if the selection contains the given object
     */
    public boolean containsSelection(Object sel) {
        return _selection.contains(sel);
    }

    /** Dispatch a selection event to all registered listeners
     */
    public void dispatchSelectionEvent(SelectionEvent e) {
        Object[] listeners = _listeners.getListenerList();
        for (int i = listeners.length-2; i>=0; i-=2) {
            if(listeners[i] == SelectionListener.class) {
                ((SelectionListener)listeners[i+1]).selectionChanged(e);
            }
        }
    }

    /**
     * Return the first selection in the list.
     */
    public Object getFirstSelection() {
        if(_selection.size() == 0) {
            return null;
        }
        return _selection.get(0);
    }

    /**
     * Return the last selection in the list.
     */
    public Object getLastSelection() {
        if(_selection.size() == 0) {
            return null;
        }
        return _selection.get(_selection.size()-1);
    }

    /**
     * Return an iterator over the selected objects.
     */
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
    public Object[] getSelectionAsArray() {
        return _selection.toArray();
    }

    /**
     * Return the number of selected objects.
     */
    public int getSelectionCount() {
        return _selection.size();
    }

    /**
     * Return the mode of the selection, either
     * SINGLE_SELECTION or MULTIPLE_SELECTION.
     */
    public int getSelectionMode() {
        return _mode;
    }

    /**
     * Remove an object from the selection.
     */
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
    public void removeSelectionListener(SelectionListener l) {
        _listeners.remove(SelectionListener.class, l);
    }

    /** Set the rendering of the object as deselected.
     * This works only if it is an instance of Figure,
     * and it has a SelectionInteractor attached to it.
     */
    private void renderDeselected (Object o) {
        if (o instanceof Figure) {
            Interactor ir = ((Figure) o).getInteractor();
            if (ir instanceof SelectionInteractor) {
                SelectionRenderer sr
                    = ((SelectionInteractor) ir).getSelectionRenderer();
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
    private void renderSelected (Object o) {
        if (o instanceof Figure) {
            Interactor ir = ((Figure) o).getInteractor();
            if (ir instanceof SelectionInteractor) {
                SelectionRenderer sr
                    = ((SelectionInteractor) ir).getSelectionRenderer();
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
    public void setSelectionMode(int mode) {
        _mode = mode;
    }
}


