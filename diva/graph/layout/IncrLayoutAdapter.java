/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.graph.layout;
import diva.graph.GraphModel;

/**
 * An adapter to make global layouts incremental.  This
 * class just calls a global layout on every change
 * to the graph.
 *
 * @author Michael Shilman  (michaels@eecs.berkeley.edu)
 * @version $Revision$
 * @rating Red
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
    public void nodeDrawn(Object node) {
        layout(_target.getGraphModel().getParent(node));
    }

    /** Called in response to the given node being moved.
     */
    public void nodeMoved(Object node) {
        //FIXME
        //layout(oldContainer);
    }

    /**
     * Called in response to the edge head being changed.
     */
    public void edgeDrawn(Object edge) {
        GraphModel model = _target.getGraphModel();
        // FIXME this is probably not quite right.
        Object root = model.getRoot();
        layout(root);
    }

    /**
     * Called in response to the edge tail being changed.
     */
    public void edgeRouted(Object edge) {
        GraphModel model = _target.getGraphModel();
        // FIXME this is probably not quite right.
        Object root = model.getRoot();
        layout(root);
    }

    /** Return the layout target.
     */
    public LayoutTarget getLayoutTarget() {
        return _layout.getLayoutTarget();
    }

    /** Set the layout target.
     */
    public void setLayoutTarget(LayoutTarget target) {
        _layout.setLayoutTarget(target);
    }

    /**
     * Call the global layout on the graph.
     */
    public void layout(Object composite) {
        try {
            _layout.layout(composite);
        }
        catch(Exception e) {
            System.err.println("Layout Failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}


