/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.graph.layout;

/**
 * A proxy layout which forwards all events to a given incremental
 * layout delegate.  This class implements the proxy design pattern,
 * hence its name.  It is a useful base class for building layouts
 * that know how to do a certain specific task, but want to delegate
 * work to a more general layout for the cases that it dosn't know how
 * to deal with.
 *
 * @author Michael Shilman  (michaels@eecs.berkeley.edu)
 * @version $Revision$
 * @rating Red
 */
public class ProxyIncrLayout implements IncrementalLayout {
    /**
     * The target incremental layout that this delegates to.
     */
    private IncrementalLayout _delegate;

    /**
     * Construct the proxy with the given target.
     */
    public ProxyIncrLayout(IncrementalLayout delegate) {
        _delegate = delegate;
    }

    /** Called in response to the given edge being given a figure.
     */
    public void edgeDrawn(Object edge) {
        _delegate.edgeDrawn(edge);
    }

    /** Called in response to the connector representing the given edge being
     *  rereouted.
     */
    public void edgeRouted(Object edge) {
        _delegate.edgeRouted(edge);
    }

    /** Called in response to the given node being given a figure.
     */
    public void nodeDrawn(Object node) {
        _delegate.nodeDrawn(node);
    }

    /** Called in response to the figure representing the
     *  given node being moved.
     */
    public void nodeMoved(Object node) {
        _delegate.nodeMoved(node);
    }

    /** Return the layout target.
     */
    public LayoutTarget getLayoutTarget() {
        return _delegate.getLayoutTarget();
    }

    /** Set the layout target.
     */
    public void setLayoutTarget(LayoutTarget target) {
        _delegate.setLayoutTarget(target);
    }

    /**
     * Layout the graph model in the viewport
     * specified by the layout target environment.
     */
    public void layout(Object composite) {
        _delegate.layout(composite);
    }
}


