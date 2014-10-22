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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A list of PropertyChangeListeners.
 *
 * @author Michael Shilman
 * @version $Id$
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

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        for (Iterator i = listeners(); i.hasNext();) {
            PropertyChangeListener l = (PropertyChangeListener) i.next();
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
