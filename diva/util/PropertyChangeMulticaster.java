/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.util;
import java.util.*;
import java.beans.*;

/**
 * A list of PropertyChangeListeners.
 *
 * @author Michael Shilman  (michaels@eecs.berkeley.edu)
 * @version $Revision$
 */
public class PropertyChangeMulticaster implements PropertyChangeListener {
    /**
     * The list of graph listeners.
     */
    private List _listeners;

    /**
     * Create an empty multicaster object.
     */
    public PropertyChangeMulticaster() {
        _listeners = new LinkedList();
    }

    /**
     * Add the given listener to the list of listeners.
     */
    public void add(PropertyChangeListener l) {
        _listeners.add(l);
    }

    /**
     * Return an iterator over the list of listeners.
     */
    public Iterator listeners() {
        return _listeners.iterator();
    }

    /**
     * Dispatch the given event to all of the listeners.
     */
    public void dispatchEvent(PropertyChangeEvent evt) {
        propertyChange(evt);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        for (Iterator i = listeners(); i.hasNext(); ) {
            PropertyChangeListener l = (PropertyChangeListener)i.next();
            l.propertyChange(evt);
        }
    }

    /**
     * Remove the given listener from the list
     * of listeners.
     */
    public void remove(PropertyChangeListener l) {
        _listeners.remove(l);
    }
}


