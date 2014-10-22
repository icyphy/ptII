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
package diva.graph.layout;

import diva.graph.GraphViewEvent;
import diva.util.Filter;

/**
 * A Listener that applies the given incremental layout whenever a graph
 * event is received.  The listener applies an optional filter which can
 * be used to limit the context into which the layout algorithm will be
 * applied.
 *
 * @author Steve Neuendorffer
 * @version $Id$
 * @Pt.AcceptedRating Red
 */
public class IncrementalLayoutListener implements diva.graph.GraphViewListener {
    private IncrementalLayout _layout;

    private Filter _filter;

    /**
     * Construct a new listener that invokes the given incremental layout
     * whenever a graph event is received.
     */
    public IncrementalLayoutListener(IncrementalLayout layout, Filter filter) {
        _layout = layout;
        _filter = filter;
    }

    /**
     */
    @Override
    public void edgeDrawn(GraphViewEvent e) {
        if (_filter != null && !_filter.accept(e.getTarget())) {
            return;
        }

        _layout.edgeDrawn(e.getTarget());
    }

    /**
     */
    @Override
    public void edgeRouted(GraphViewEvent e) {
        if (_filter != null && !_filter.accept(e.getTarget())) {
            return;
        }

        _layout.edgeRouted(e.getTarget());
    }

    /**
     * Return the filter for this listener.
     */
    public Filter getFilter() {
        return _filter;
    }

    /**
     * Return the layout.
     */
    public IncrementalLayout getLayout() {
        return _layout;
    }

    /**
     */
    @Override
    public void nodeDrawn(GraphViewEvent e) {
        if (_filter != null && !_filter.accept(e.getTarget())) {
            return;
        }

        _layout.nodeDrawn(e.getTarget());
    }

    /**
     */
    @Override
    public void nodeMoved(GraphViewEvent e) {
        if (_filter != null && !_filter.accept(e.getTarget())) {
            return;
        }

        _layout.nodeMoved(e.getTarget());
    }
}
