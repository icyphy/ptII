/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.graph.toolbox;
import diva.canvas.Figure;
import java.util.Hashtable;
import diva.graph.NodeRenderer;
import diva.graph.GraphController;

/**
 * A NodeRenderer implementation which allows a user to add different
 * specialized node renderers which are called selectively, based on
 * the type of the user object that is contained in the node that is
 * being rendered.
 *
 * @author Michael Shilman  (michaels@eecs.berkeley.edu)
 * @version $Revision$
 * @rating Red
 */
public class TypedNodeRenderer implements NodeRenderer {
    /**
     * The graph controller is using this renderer
     */
    private GraphController _controller = null;

    /**
     * The default renderer.
     */
    private NodeRenderer _defaultRenderer = null;

    /**
     * The typed renderers.
     */
    private Hashtable _typedRenderers = null;

    /**
     * A typed node renderer with the given node renderer
     * as its default (i.e. how to render a node if it
     * has a type that is not understood by this renderer).
     */
    public TypedNodeRenderer(GraphController controller, NodeRenderer defaultRenderer) {
        _controller = controller;
        _defaultRenderer = defaultRenderer;
        _typedRenderers = new Hashtable();
    }

    /**
     * Add a renderer which is invoked when the rendered node's
     * semantic object is an instance of the given class.
     */
    public void addTypedRenderer(Class c, NodeRenderer r) {
        _typedRenderers.put(c, r);
    }

    /**
     * Remove a typed renderer.
     *
     * @see #addTypedRenderer(Class,NodeRenderer)
     */
    public void removeTypedRenderer(Class c) {
        _typedRenderers.remove(c);
    }

    /**
     * Return the rendered visual representation of this node by
     * looking up the class of its semantic object.
     */
    public Figure render(Object node) {
        try {
            Object o = _controller.getGraphModel().getSemanticObject(node);
            NodeRenderer r = (NodeRenderer)_typedRenderers.get(o.getClass());
            return r.render(node);
        } catch (Exception ex) {
            return _defaultRenderer.render(node);
        }
    }
}


