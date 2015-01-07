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

/**
 * A proxy layout which forwards all events to a given incremental
 * layout delegate.  This class implements the proxy design pattern,
 * hence its name.  It is a useful base class for building layouts
 * that know how to do a certain specific task, but want to delegate
 * work to a more general layout for the cases that it doesn't know how
 * to deal with.
 *
 * @author Michael Shilman
 * @version $Id$
 * @Pt.AcceptedRating Red
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
    @Override
    public void edgeDrawn(Object edge) {
        _delegate.edgeDrawn(edge);
    }

    /** Called in response to the connector representing the given edge being
     *  rereouted.
     */
    @Override
    public void edgeRouted(Object edge) {
        _delegate.edgeRouted(edge);
    }

    /** Called in response to the given node being given a figure.
     */
    @Override
    public void nodeDrawn(Object node) {
        _delegate.nodeDrawn(node);
    }

    /** Called in response to the figure representing the
     *  given node being moved.
     */
    @Override
    public void nodeMoved(Object node) {
        _delegate.nodeMoved(node);
    }

    /** Return the layout target.
     */
    @Override
    public LayoutTarget getLayoutTarget() {
        return _delegate.getLayoutTarget();
    }

    /** Set the layout target.
     */
    @Override
    public void setLayoutTarget(LayoutTarget target) {
        _delegate.setLayoutTarget(target);
    }

    /**
     * Layout the graph model in the viewport
     * specified by the layout target environment.
     */
    @Override
    public void layout(Object composite) {
        _delegate.layout(composite);
    }
}
