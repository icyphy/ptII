/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.graph.modular;
import java.util.Iterator;

import diva.graph.GraphEvent;
import diva.graph.GraphUtilities;
import diva.graph.MutableGraphModel;

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
public abstract class MutableModularGraphModel extends ModularGraphModel
    implements MutableGraphModel {

    /**
     * Construct an empty graph model whose
     * root is the given semantic object.
     */
    public MutableModularGraphModel(Object root) {
        super(root);
    }

    /**
     * Return true if the head of the given edge can be attached to the
     * given node.
     */
    public boolean acceptHead(Object edge, Object node) {
        return getMutableEdgeModel(edge).acceptHead(edge, node);
    }

    /**
     * Return true if the tail of the given edge can be attached to the
     * given node.
     */
    public boolean acceptTail(Object edge, Object node) {
        return getMutableEdgeModel(edge).acceptTail(edge, node);
    }

    /**
     * Add a node to the given graph and notify listeners with a
     * NODE_ADDED event. <p>
     */
    public void addNode(Object eventSource, Object node, Object parent) {
        Object prevParent = getMutableNodeModel(node).getParent(node);
        getMutableNodeModel(node).setParent(node, parent);

        GraphEvent e = new GraphEvent(eventSource, GraphEvent.NODE_ADDED,
                node, prevParent);
        dispatchGraphEvent(e);
    }

    /**
     * Connect the given edge to the given tail and head nodes,
     * then dispatch events to the listeners.
     */
    public void connectEdge(Object eventSource, Object edge,
            Object tailNode, Object headNode) {
        Object prevTail = getMutableEdgeModel(edge).getTail(edge);
        Object prevHead = getMutableEdgeModel(edge).getHead(edge);
        getMutableEdgeModel(edge).setHead(edge, headNode);
        getMutableEdgeModel(edge).setTail(edge, tailNode);

        GraphEvent e1 = new GraphEvent(eventSource,
                GraphEvent.EDGE_HEAD_CHANGED,
                edge, prevHead);
        dispatchGraphEvent(e1);

        GraphEvent e2 = new GraphEvent(eventSource,
                GraphEvent.EDGE_TAIL_CHANGED,
                edge, prevTail);
        dispatchGraphEvent(e2);
    }

    /**
     * Disconnect an edge from its two endpoints and notify graph
     * listeners with an EDGE_HEAD_CHANGED and an EDGE_TAIL_CHANGED
     * event.
     */
    public void disconnectEdge(Object eventSource, Object edge) {
        MutableEdgeModel model = getMutableEdgeModel(edge);
        Object head = model.getHead(edge);
        Object tail = model.getTail(edge);
        model.setTail(edge, null);
        model.setHead(edge, null);
        if(head != null) {
            GraphEvent e = new GraphEvent(eventSource,
                    GraphEvent.EDGE_HEAD_CHANGED,
                    edge, head);
            dispatchGraphEvent(e);
        }
        if(tail != null) {
            GraphEvent e = new GraphEvent(eventSource,
                    GraphEvent.EDGE_TAIL_CHANGED,
                    edge, tail);
            dispatchGraphEvent(e);
        }
    }

    /**
     * Return the model for the given edge object, cast as a MutableEdgeModel.
     * If the object is not an edge, then return null.  This method
     * assumes that the edge model for the given edge is mutable.
     */
    public MutableEdgeModel getMutableEdgeModel(Object edge) {
        return (MutableEdgeModel)getEdgeModel(edge);
    }

    /**
     * Return the node model for the given object, cast as a MutableNodeModel.
     * If the object is not a node, then return null.  This method
     * assumes that the edge model for the given edge is mutable.
     */
    public MutableNodeModel getMutableNodeModel(Object node) {
        return (MutableNodeModel)getNodeModel(node);
    }

    /**
     * Delete a node from its parent graph and notify
     * graph listeners with a NODE_REMOVED event.  This first removes all the
     * edges that are connected to the given node, or some subnode of that
     * node, and then sets the parent of the node to null.
     */
    public void removeNode(Object eventSource, Object node) {
        // Remove the edges.
        Iterator i = GraphUtilities.partiallyContainedEdges(node, this);
        while(i.hasNext()) {
            Object edge = i.next();
            disconnectEdge(eventSource, edge);
        }

        i = outEdges(node);
        while(i.hasNext()) {
            Object edge = i.next();
            disconnectEdge(eventSource, edge);
        }

        i = inEdges(node);
        while(i.hasNext()) {
            Object edge = i.next();
            disconnectEdge(eventSource, edge);
        }

        // remove the node.
        Object prevParent = getMutableNodeModel(node).getParent(node);
        getMutableNodeModel(node).setParent(node, null);
        GraphEvent e = new GraphEvent(eventSource, GraphEvent.NODE_REMOVED,
                node, prevParent);
        dispatchGraphEvent(e);
    }

    /**
     * Connect an edge to the given head node and notify listeners
     * with an EDGE_HEAD_CHANGED event.
     */
    public void setEdgeHead(Object eventSource, Object edge, Object head) {
        Object prevHead = getMutableEdgeModel(edge).getHead(edge);
        getMutableEdgeModel(edge).setHead(edge, head);
        GraphEvent e = new GraphEvent(eventSource,
                GraphEvent.EDGE_HEAD_CHANGED,
                edge, prevHead);
        dispatchGraphEvent(e);
    }

    /**
     * Connect an edge to the given tail node and notify listeners
     * with an EDGE_TAIL_CHANGED event.
     */
    public void setEdgeTail(Object eventSource, Object edge, Object tail) {
        Object prevTail = getMutableEdgeModel(edge).getTail(edge);
        getMutableEdgeModel(edge).setTail(edge, tail);
        GraphEvent e = new GraphEvent(eventSource,
                GraphEvent.EDGE_TAIL_CHANGED,
                edge, prevTail);
        dispatchGraphEvent(e);
    }
}

