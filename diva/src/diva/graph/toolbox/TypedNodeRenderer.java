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

import java.util.Hashtable;

import diva.canvas.Figure;
import diva.graph.GraphController;
import diva.graph.NodeRenderer;

/**
 * A NodeRenderer implementation which allows a user to add different
 * specialized node renderers which are called selectively, based on
 * the type of the user object that is contained in the node that is
 * being rendered.
 *
 * @author Michael Shilman
 * @version $Id$
 * @Pt.AcceptedRating Red
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
    public TypedNodeRenderer(GraphController controller,
            NodeRenderer defaultRenderer) {
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
    @Override
    public Figure render(Object node) {
        try {
            Object o = _controller.getGraphModel().getSemanticObject(node);
            NodeRenderer r = (NodeRenderer) _typedRenderers.get(o.getClass());
            return r.render(node);
        } catch (Exception ex) {
            return _defaultRenderer.render(node);
        }
    }
}
