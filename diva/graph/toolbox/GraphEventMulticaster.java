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
package diva.graph.toolbox;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import diva.graph.GraphEvent;
import diva.graph.GraphListener;

/**
 * A list of GraphListeners which is smart enough to call the correct
 * methods on these listeners given a GraphEvent's ID.
 *
 * @author Michael Shilman
 * @version $Id$
 * @Pt.AcceptedRating Yellow
 */
public class GraphEventMulticaster implements GraphListener {
    /**
     * The list of graph listeners.
     */
    private List _listeners;

    /**
     * Create an empty multicaster object.
     */
    public GraphEventMulticaster() {
        _listeners = new LinkedList();
    }

    /**
     * Add the given listener to the list of listeners.
     */
    public void add(GraphListener l) {
        _listeners.add(l);
    }

    /**
     * Dispatch an event to the list of listeners, calling
     * the appropriate method based on the event's ID.
     */
    public void dispatchEvent(GraphEvent e) {
        switch (e.getID()) {
        case GraphEvent.EDGE_HEAD_CHANGED:
            edgeHeadChanged(e);
            break;

        case GraphEvent.EDGE_TAIL_CHANGED:
            edgeTailChanged(e);
            break;

        case GraphEvent.NODE_ADDED:
            nodeAdded(e);
            break;

        case GraphEvent.NODE_REMOVED:
            nodeRemoved(e);
            break;

        case GraphEvent.STRUCTURE_CHANGED:
            structureChanged(e);
            break;
        }
    }

    /**
     * Dispatch the edgeHeadChanged() event to the
     * listeners.
     */
    @Override
    public void edgeHeadChanged(GraphEvent e) {
        for (Iterator i = listeners(); i.hasNext();) {
            GraphListener l = (GraphListener) i.next();
            l.edgeHeadChanged(e);
        }
    }

    /**
     * Dispatch the edgeTailChanged() event to the
     * listeners.
     */
    @Override
    public void edgeTailChanged(GraphEvent e) {
        for (Iterator i = listeners(); i.hasNext();) {
            GraphListener l = (GraphListener) i.next();
            l.edgeTailChanged(e);
        }
    }

    /**
     * Return an iterator over the list of listeners.
     */
    public Iterator listeners() {
        return _listeners.iterator();
    }

    /**
     * Dispatch the nodeAdded() event to each of the listeners.
     */
    @Override
    public void nodeAdded(GraphEvent e) {
        for (Iterator i = listeners(); i.hasNext();) {
            GraphListener l = (GraphListener) i.next();
            l.nodeAdded(e);
        }
    }

    /**
     * Dispatch the nodeRemoved() event to each of the listeners.
     */
    @Override
    public void nodeRemoved(GraphEvent e) {
        for (Iterator i = listeners(); i.hasNext();) {
            GraphListener l = (GraphListener) i.next();
            l.nodeRemoved(e);
        }
    }

    /**
     * Remove the given listener from the list
     * of listeners.
     */
    public void remove(GraphListener l) {
        _listeners.remove(l);
    }

    /**
     * Dispatch the structureChanged() event to each of the listeners.
     */
    @Override
    public void structureChanged(GraphEvent e) {
        for (Iterator i = listeners(); i.hasNext();) {
            GraphListener l = (GraphListener) i.next();
            l.structureChanged(e);
        }
    }
}
