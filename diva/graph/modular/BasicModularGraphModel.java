/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.graph.modular;
import java.util.Iterator;

import diva.util.PropertyContainer;
import diva.util.SemanticObjectContainer;

/**
 * A modular implementation of the graph model, whereby users with
 * heterogeneous graphs can implement the graph model interface by
 * implementing the simple interfaces of Graph, Node, CompositeNode,
 * and Edge.
 *
 * @author Michael Shilman  (michaels@eecs.berkeley.edu)
 * @version $Revision$
 * @rating Red
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
    public CompositeModel getCompositeModel(Object composite) {
        if(composite instanceof Graph) {
            return _nodeModel;
        } else {
            return null;
        }
    }

    /**
     * Return the model for the given edge object.  If the object is not
     * an edge, then return null.
     */
    public EdgeModel getEdgeModel(Object edge) {
        if(edge instanceof Edge) {
            return _edgeModel;
        } else {
            return null;
        }
    }

    /**
     * Return the node model for the given object.  If the object is not
     * a node, then return null.
     */
    public NodeModel getNodeModel(Object node) {
        if(node instanceof Node) {
            return _nodeModel;
        } else {
            return null;
        }
    }

    /**
     * Return the property of the object associated with
     * the given property name.
     */
    public Object getProperty(Object o, String propertyName) {
        return ((PropertyContainer)o).getProperty(propertyName);
    }

    /**
     * Return the semantic object correspoding
     * to the given node, edge, or composite.
     */
    public Object getSemanticObject(Object o) {
        return ((SemanticObjectContainer)o).getSemanticObject();
    }

    /**
     * Set the property of the object associated with
     * the given property name.
     */
    public void setProperty(Object o, String propertyName, Object value) {
        ((PropertyContainer)o).setProperty(propertyName, value);
    }

    /**
     * Set the semantic object correspoding
     * to the given node, edge, or composite.
     */
    public void setSemanticObject(Object o, Object sem) {
        ((SemanticObjectContainer)o).setSemanticObject(sem);
    }

    public class BasicEdgeModel implements MutableEdgeModel {
        /**
         * Return true if the head of the given edge can be attached to the
         * given node.
         */
        public boolean acceptHead(Object edge, Object node) {
            return ((Edge)edge).acceptHead((Node)node);
        }

        /**
         * Return true if the tail of the given edge can be attached to the
         * given node.
         */
        public boolean acceptTail(Object edge, Object node) {
            return ((Edge)edge).acceptTail((Node)node);
        }

        /**
         * Return the head node of the given edge.
         */
        public Object getHead(Object edge) {
            Edge edgePeer = (Edge)edge;
            Node headPeer = edgePeer.getHead();
            return headPeer;
        }

        /**
         * Return the tail node of this edge.
         */
        public Object getTail(Object edge) {
            Edge edgePeer = (Edge)edge;
            Node tailPeer = edgePeer.getTail();
            return tailPeer;
        }

        /**
         * Return whether or not this edge is directed.
         */
        public boolean isDirected(Object edge) {
            Edge edgePeer = (Edge)edge;
            return edgePeer.isDirected();
        }

        /**
         * Connect an edge to the given head node and notify listeners
         * with an EDGE_HEAD_CHANGED event.
         */
        public void setHead(Object edge, Object newHead) {
            Edge edgePeer = (Edge)edge;
            Node headPeer = (Node)newHead;
            edgePeer.setHead(headPeer);
        }

        /**
         * Connect an edge to the given tail node and notify listeners
         * with an EDGE_TAIL_CHANGED event.
         */
        public void setTail(Object edge, Object newTail) {
            Edge edgePeer = (Edge)edge;
            Node tailPeer = (Node)newTail;
            edgePeer.setTail(tailPeer);
        }
    }

    public class BasicNodeModel implements MutableCompositeNodeModel {
        /**
         * Return an iterator over the edges coming into the given node.
         */
        public Iterator inEdges(Object node) {
            Node nodePeer = (Node)node;
            return nodePeer.inEdges();
        }

        /**
         * Return an iterator over the edges coming out of the given node.
         */
        public Iterator outEdges(Object node) {
            Node nodePeer = (Node)node;
            return nodePeer.outEdges();
        }

        /**
         * Return the graph parent of the given node.
         */
        public Object getParent(Object node) {
            Node nodePeer = (Node)node;
            return nodePeer.getParent();
        }

        /**
         * Set the graph parent of the given node.  Implementors of this method
         * are also responsible for insuring that it is set properly as
         * the child of the graph in the graph.
         */
        public void setParent(Object node, Object parent) {
            Node nodePeer = (Node)node;
            Graph parentPeer = (Graph)parent;
            nodePeer.setParent(parentPeer);
        }

        /**
         * Return the number of nodes contained in
         * this graph or composite node.
         */
        public int getNodeCount(Object composite) {
            CompositeNode compositePeer = (CompositeNode)composite;
            return compositePeer.getNodeCount();
        }

        /**
         * Provide an iterator over the nodes in the
         * given graph or composite node.  This iterator
         * does not necessarily support removal operations.
         */
        public Iterator nodes(Object composite) {
            CompositeNode compositePeer = (CompositeNode)composite;
            return compositePeer.nodes();
        }
    }
}

