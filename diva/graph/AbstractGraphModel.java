/*
 @Copyright (c) 1998-2014 The Regents of the University of California.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY


 */
package diva.graph;

import java.util.LinkedList;

import javax.swing.SwingUtilities;

import diva.graph.toolbox.GraphEventMulticaster;

/**
 * An abstract implementation of the GraphModel interface that provides
 * the basic event notification system
 *
 * @author Steve Neuendorffer, Contributor Bert Rodiers
 * @version $Id$
 * @Pt.AcceptedRating Red
 */
public abstract class AbstractGraphModel implements GraphModel {
    /**
     * Whether or not to dispatch events.
     */
    private boolean _dispatch = true;

    /**
     * The list of graph listeners.
     */
    protected GraphEventMulticaster _graphListeners = new GraphEventMulticaster();

    /**
     * Add a graph listener to the model.  Graph listeners are
     * notified with a GraphEvent any time the graph is modified.
     */
    @Override
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
    @Override
    public void dispatchGraphEvent(final GraphEvent e) {
        if (_dispatch) {
            if (SwingUtilities.isEventDispatchThread()) {
                _graphListeners.dispatchEvent(e);
            } else {
                // We only handle events that are actually added
                // by _addEvent.
                if (_addEvent(e)) {

                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            // _getEvent will remove the event from the queue
                            // which allows new events to be added. You could argue
                            // that it is to early here since the previous repaint is
                            // not done yet and this might make a new repaint superfluous.
                            // However we can't wait to remove the element from the queue
                            // until after the dispatchEvent since then we might miss repaints
                            // that needed to be done.
                            GraphEvent event = _getEvent();
                            if (event != null) {
                                _graphListeners.dispatchEvent(event);
                            }
                        }
                    });
                }
            }
        }
    }

    /**
     * Remove the given listener from this graph model.
     * The listener will no longer be notified of changes
     * to the graph.
     */
    @Override
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
    @Override
    public void setDispatchEnabled(boolean val) {
        _dispatch = val;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**Add a GraphEvent to be processed. These elements will be put
     * in a queue if necessary (some repaints are not necessary).
     * @param e The GraphEvent to be processed.
     * @return true when the GraphEvent has been added to the queue.
     */
    private synchronized boolean _addEvent(GraphEvent e) {
        // If the structural event queue is not empty we can
        // ignore this change.
        if (structuralChangeEvents.isEmpty()) {
            if (e.getID() == GraphEvent.STRUCTURE_CHANGED) {
                structuralChangeEvents.add(e);
                otherEvents.clear();
            } else {
                otherEvents.add(e);
            }
            return true;
        } else {
            return false;
        }
    }

    /**Get a GraphEvent to process it. If there are no such events null
     * will be returned.
     * @return The GraphEvent to process.
     */
    private synchronized GraphEvent _getEvent() {
        if (!structuralChangeEvents.isEmpty()) {
            return structuralChangeEvents.removeFirst();
        }
        if (!otherEvents.isEmpty()) {
            return otherEvents.removeFirst();
        }
        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // A queue to keep track of all GraphEvents that have a lot of structural changes
    // and hence will lead to a complete repaint of the model.
    private LinkedList<GraphEvent> structuralChangeEvents = new LinkedList<GraphEvent>();

    // A queue to keep track of all other GraphEvents.
    private LinkedList<GraphEvent> otherEvents = new LinkedList<GraphEvent>();
}
