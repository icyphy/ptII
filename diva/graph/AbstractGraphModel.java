/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.graph;
import javax.swing.SwingUtilities;

import diva.graph.toolbox.GraphEventMulticaster;

/**
 * An abstract implementation of the GraphModel interface that provides
 * the basic event notification system
 *
 * @author Steve Neuendorffer (neuenodr@eecs.berkeley.edu)
 * @version $Revision$
 * @rating Red
 */
public abstract class AbstractGraphModel implements GraphModel {
    /**
     * Whether or not to dispatch events.
     */
    private boolean _dispatch = true;

    /**
     * The list of graph listeners.
     */
    protected GraphEventMulticaster _graphListeners =
    new GraphEventMulticaster();

    /**
     * Add a graph listener to the model.  Graph listeners are
     * notified with a GraphEvent any time the graph is modified.
     */
    public void addGraphListener(GraphListener l) {
        _graphListeners.add(l);
    }

    /**
     * Send an graph event to all of the graph listeners.  This
     * allows manual control of sending graph graph events, or
     * allows the user to send a STRUCTURE_CHANGED after some
     * inner-loop operations.
     * <p>
     * This method furthermore ensures that all graph events are
     * dispatched in the event thread.
     * @see setDispatchEnabled(boolean)
     */
    public void dispatchGraphEvent(final GraphEvent e) {
        if (_dispatch) {
            if (SwingUtilities.isEventDispatchThread()) {
                _graphListeners.dispatchEvent(e);
            } else {
                SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            _graphListeners.dispatchEvent(e);
                        }
                    });
            }
        }
    }

    /**
     * Remove the given listener from this graph model.
     * The listener will no longer be notified of changes
     * to the graph.
     */
    public void removeGraphListener(GraphListener l) {
        _graphListeners.remove(l);
    }

    /**
     * Turn on/off all event dispatches from this graph model, for use
     * in an inner-loop algorithm.  When turning dispatch back on
     * again, if the client has made changes that listeners should
     * know about, he should create an appropriate STRUCTURE_CHANGED
     * and dispatch it using the dispatchGraphEvent() method.
     *
     * @see dispatchGraphEvent(GraphEvent)
     */
    public void setDispatchEnabled(boolean val) {
        _dispatch = val;
    }
}

