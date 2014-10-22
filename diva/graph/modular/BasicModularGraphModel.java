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

import diva.util.NullIterator;
import diva.util.PropertyContainer;
import diva.util.SemanticObjectContainer;

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
public class BasicModularGraphModel extends MutableModularGraphModel {
    /** The node model
     */
    private BasicNodeModel _nodeModel = new BasicNodeModel();

    /** The edge model
     */
    private BasicEdgeModel _edgeModel = new BasicEdgeModel();

    /**
     * Construct an empty graph model whose
     * root is the given semantic object.
     */
    public BasicModularGraphModel(Graph root) {
        super(root);
    }

    /**
     * Return the model for the given composite object.  If the object is not
     * a composite, meaning that it does not contain other nodes,
     * then return null.
     */
    @Override
    public CompositeModel getCompositeModel(Object composite) {
        if (composite instanceof Graph) {
            return _nodeModel;
        } else {
            return null;
        }
    }

    /**
     * Return the model for the given edge object.  If the object is not
     * an edge, then return null.
     */
    @Override
    public EdgeModel getEdgeModel(Object edge) {
        if (edge instanceof Edge) {
            return _edgeModel;
        } else {
            return null;
        }
    }

    /**
     * Return the node model for the given object.  If the object is not
     * a node, then return null.
     */
    @Override
    public NodeModel getNodeModel(Object node) {
        if (node instanceof Node) {
            return _nodeModel;
        } else {
            return null;
        }
    }

    /**
     * Return the property of the object associated with
     * the given property name.
     */
    @Override
    public Object getProperty(Object o, String propertyName) {
        return ((PropertyContainer) o).getProperty(propertyName);
    }

    /**
     * Return the semantic object corresponding
     * to the given node, edge, or composite.
     */
    @Override
    public Object getSemanticObject(Object o) {
        return ((SemanticObjectContainer) o).getSemanticObject();
    }

    /**
     * Set the property of the object associated with
     * the given property name.
     */
    @Override
    public void setProperty(Object o, String propertyName, Object value) {
        ((PropertyContainer) o).setProperty(propertyName, value);
    }

    /**
     * Set the semantic object corresponding
     * to the given node, edge, or composite.
     */
    @Override
    public void setSemanticObject(Object o, Object sem) {
        ((SemanticObjectContainer) o).setSemanticObject(sem);
    }

    public static class BasicEdgeModel implements MutableEdgeModel {
        // FindBugs suggests making this class static so as to decrease
        // the size of instances and avoid dangling references.

        /**
         * Return true if the head of the given edge can be attached to the
         * given node.
         */
        @Override
        public boolean acceptHead(Object edge, Object node) {
            return ((Edge) edge).acceptHead((Node) node);
        }

        /**
         * Return true if the tail of the given edge can be attached to the
         * given node.
         */
        @Override
        public boolean acceptTail(Object edge, Object node) {
            return ((Edge) edge).acceptTail((Node) node);
        }

        /**
         * Return the head node of the given edge.
         */
        @Override
        public Object getHead(Object edge) {
            Edge edgePeer = (Edge) edge;
            Node headPeer = edgePeer.getHead();
            return headPeer;
        }

        /**
         * Return the tail node of this edge.
         */
        @Override
        public Object getTail(Object edge) {
            Edge edgePeer = (Edge) edge;
            Node tailPeer = edgePeer.getTail();
            return tailPeer;
        }

        /**
         * Return whether or not this edge is directed.
         */
        @Override
        public boolean isDirected(Object edge) {
            Edge edgePeer = (Edge) edge;
            return edgePeer.isDirected();
        }

        /**
         * Connect an edge to the given head node and notify listeners
         * with an EDGE_HEAD_CHANGED event.
         */
        @Override
        public void setHead(Object edge, Object newHead) {
            Edge edgePeer = (Edge) edge;
            Node headPeer = (Node) newHead;
            edgePeer.setHead(headPeer);
        }

        /**
         * Connect an edge to the given tail node and notify listeners
         * with an EDGE_TAIL_CHANGED event.
         */
        @Override
        public void setTail(Object edge, Object newTail) {
            Edge edgePeer = (Edge) edge;
            Node tailPeer = (Node) newTail;
            edgePeer.setTail(tailPeer);
        }
    }

    public static class BasicNodeModel implements MutableCompositeNodeModel {
        // FindBugs suggests making this class static so as to decrease
        // the size of instances and avoid dangling references.

        /**
         * Return an iterator over the edges coming into the given node.
         */
        @Override
        public Iterator inEdges(Object node) {
            Node nodePeer = (Node) node;
            return nodePeer.inEdges();
        }

        /**
         * Return an iterator over the edges coming out of the given node.
         */
        @Override
        public Iterator outEdges(Object node) {
            Node nodePeer = (Node) node;
            return nodePeer.outEdges();
        }

        /**
         * Return the graph parent of the given node.
         */
        @Override
        public Object getParent(Object node) {
            Node nodePeer = (Node) node;
            return nodePeer.getParent();
        }

        /**
         * Set the graph parent of the given node.  Implementors of this method
         * are also responsible for insuring that it is set properly as
         * the child of the graph in the graph.
         */
        @Override
        public void setParent(Object node, Object parent) {
            Node nodePeer = (Node) node;
            Graph parentPeer = (Graph) parent;
            nodePeer.setParent(parentPeer);
        }

        /**
         * Return the number of nodes contained in
         * this graph or composite node.
         */
        @Override
        public int getNodeCount(Object composite) {
            CompositeNode compositePeer = (CompositeNode) composite;
            return compositePeer.getNodeCount();
        }

        /**
         * Provide an iterator over the nodes in the
         * given graph or composite node.  This iterator
         * does not necessarily support removal operations.
         */
        @Override
        public Iterator nodes(Object composite) {
            CompositeNode compositePeer = (CompositeNode) composite;
            return compositePeer.nodes();
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
            return nodes(composite);
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
            return new NullIterator();
        }
    }
}
