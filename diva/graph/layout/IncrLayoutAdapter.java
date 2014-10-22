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

import diva.graph.GraphModel;

/**
 * An adapter to make global layouts incremental.  This
 * class just calls a global layout on every change
 * to the graph.
 *
 * @author Michael Shilman
 * @version $Id$
 * @Pt.AcceptedRating Red
 */
public class IncrLayoutAdapter implements IncrementalLayout {
    private LayoutTarget _target;

    private GlobalLayout _layout;

    /**
     * Construct a new adapter that uses the given global layout for
     * every change in the graph, and operates within the context of
     * the given layout target.
     */
    public IncrLayoutAdapter(GlobalLayout global) {
        _layout = global;
        _target = global.getLayoutTarget();
    }

    /** Called in response to the given node being added.
     */
    @Override
    public void nodeDrawn(Object node) {
        layout(_target.getGraphModel().getParent(node));
    }

    /** Called in response to the given node being moved.
     */
    @Override
    public void nodeMoved(Object node) {
        //FIXME
        //layout(oldContainer);
    }

    /**
     * Called in response to the edge head being changed.
     */
    @Override
    public void edgeDrawn(Object edge) {
        GraphModel model = _target.getGraphModel();

        // FIXME this is probably not quite right.
        Object root = model.getRoot();
        layout(root);
    }

    /**
     * Called in response to the edge tail being changed.
     */
    @Override
    public void edgeRouted(Object edge) {
        GraphModel model = _target.getGraphModel();

        // FIXME this is probably not quite right.
        Object root = model.getRoot();
        layout(root);
    }

    /** Return the layout target.
     */
    @Override
    public LayoutTarget getLayoutTarget() {
        return _layout.getLayoutTarget();
    }

    /** Set the layout target.
     */
    @Override
    public void setLayoutTarget(LayoutTarget target) {
        _layout.setLayoutTarget(target);
    }

    /**
     * Call the global layout on the graph.
     */
    @Override
    public void layout(Object composite) {
        try {
            _layout.layout(composite);
        } catch (Exception e) {
            System.err.println("Layout Failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
