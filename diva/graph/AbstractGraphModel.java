/*
 Copyright (c) 1998-2001 The Regents of the University of California
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
package diva.graph;
import javax.swing.SwingUtilities;

import diva.graph.toolbox.GraphEventMulticaster;

/**
 * An abstract implementation of the GraphModel interface that provides
 * the basic event notification system
 *
 * @author Steve Neuendorffer (neuendor@eecs.berkeley.edu)
 * @version $Id$
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
     * @see #setDispatchEnabled(boolean)
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
     * @see #dispatchGraphEvent(GraphEvent)
     */
    public void setDispatchEnabled(boolean val) {
        _dispatch = val;
    }
}

