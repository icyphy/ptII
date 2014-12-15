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
package diva.graph.modular;

import java.util.Iterator;
import java.util.LinkedList;

import diva.graph.AbstractGraphModel;

/**
 * A modular implementation of the graph model, whereby users with
 * heterogeneous graphs can implement the graph model interface by
 * implementing the simple interfaces of Graph, Node, CompositeNode,
 * and Edge.
 *
 * @author Michael Shilman
 * @version $Id$
 * @Pt.AcceptedRating Red
 */
public abstract class ModularGraphModel extends AbstractGraphModel {
    /**
     * The root of the graph contained by this model.
     */
    private Object _root = null;

    /**
     * Construct an empty graph model whose
     * root is the given semantic object.
     */
    public ModularGraphModel(Object root) {
        _root = root;
    }

    /**
     * Return true if this composite node contains the given node.
     */
    @Override
    public boolean containsNode(Object composite, Object node) {
        return composite.equals(getNodeModel(node).getParent(node));
    }

    /**
     * Return the model for the given composite object.  If the object is not
     * a composite, meaning that it does not contain other nodes,
     * then return null.
     */
    public abstract CompositeModel getCompositeModel(Object composite);

    /**
     * Return the model for the given edge object.  If the object is not
     * an edge, then return null.
     */
    public abstract EdgeModel getEdgeModel(Object edge);

    /**
     * Return the head node of the given edge.
     */
    @Override
    public Object getHead(Object edge) {
        return getEdgeModel(edge).getHead(edge);
    }

    /**
     * Return the number of nodes contained in
     * this graph or composite node.
     */
    @Override
    public int getNodeCount(Object composite) {
        return getCompositeModel(composite).getNodeCount(composite);
    }

    /**
     * Return the node model for the given object.  If the object is not
     * a node, then return null.
     */
    public abstract NodeModel getNodeModel(Object node);

    /**
     * Return the parent graph of this node, return
     * null if there is no parent.
     */
    @Override
    public Object getParent(Object node) {
        if (node != _root) {
            NodeModel model = getNodeModel(node);

            if (model != null) {
                return model.getParent(node);
            }
        }

        return null;
    }

    /**
     * Return the property of the object associated with
     * the given property name.
     */
    @Override
    public abstract Object getProperty(Object o, String propertyName);

    /**
     * Return the root graph of this graph model.
     */
    @Override
    public Object getRoot() {
        return _root;
    }

    /**
     * Return the semantic object corresponding
     * to the given node, edge, or composite.
     */
    @Override
    public abstract Object getSemanticObject(Object o);

    /**
     * Return the tail node of this edge.
     */
    @Override
    public Object getTail(Object edge) {
        return getEdgeModel(edge).getTail(edge);
    }

    /**
     * Return whether or not this edge is directed.
     */
    @Override
    public boolean isDirected(Object edge) {
        return getEdgeModel(edge).isDirected(edge);
    }

    /**
     * Return true if the given object is a composite
     * node in this model, i.e. it contains children.
     */
    @Override
    public boolean isComposite(Object o) {
        return getCompositeModel(o) != null;
    }

    /**
     * Return true if the given object is a
     * node in this model.
     */
    @Override
    public boolean isEdge(Object o) {
        return getEdgeModel(o) != null;
    }

    /**
     * Return true if the given object is a
     * node in this model.
     */
    @Override
    public boolean isNode(Object o) {
        return getNodeModel(o) != null;
    }

    /**
     * Provide an iterator over the nodes in the
     * given graph or composite node.  This iterator
     * does not necessarily support removal operations.
     */
    @Override
    public Iterator nodes(Object composite) {
        // If we copy text from annotation and then paste it in to the background
        // we get a NPE here.
        if (getCompositeModel(composite) == null) {
            throw new NullPointerException("Could not get the composite model for "
                    + composite);
        }
        return getCompositeModel(composite).nodes(composite);
    }

    /**
     * Provide an iterator over the nodes that should
     * be rendered prior to the edges. This iterator
     * does not necessarily support removal operations.
     * In this base class, this returns the same iterator
     * as the nodes(Object) method.
     */
    @Override
    public Iterator nodesBeforeEdges(Object composite) {
        return getCompositeModel(composite).nodesBeforeEdges(composite);
    }

    /**
     * Provide an iterator over the nodes that should
     * be rendered after to the edges. This iterator
     * does not necessarily support removal operations.
     * In this base class, this returns an iterator over
     * nothing.
     */
    @Override
    public Iterator nodesAfterEdges(Object composite) {
        return getCompositeModel(composite).nodesAfterEdges(composite);
    }

    /**
     * Return an iterator over the <i>in</i> edges of this
     * node. This iterator does not support removal operations.
     * If there are no in-edges, an iterator with no elements is
     * returned.
     */
    @Override
    public Iterator inEdges(Object node) {
        NodeModel model = getNodeModel(node);

        if (model != null) {
            return model.inEdges(node);
        }

        return new LinkedList().iterator();
    }

    /**
     * Return an iterator over the <i>out</i> edges of this
     * node.  This iterator does not support removal operations.
     * If there are no out-edges, an iterator with no elements is
     * returned.
     */
    @Override
    public Iterator outEdges(Object node) {
        NodeModel model = getNodeModel(node);

        if (model != null) {
            return model.outEdges(node);
        }

        return new LinkedList().iterator();
    }

    /**
     * Set the property of the object associated with
     * the given property name.
     */
    @Override
    public abstract void setProperty(Object o, String propertyName, Object value);

    /**
     * Set the semantic object corresponding
     * to the given node, edge, or composite.
     */
    @Override
    public abstract void setSemanticObject(Object o, Object sem);
}
