/*
 Copyright (c) 1998-2001 The Regents of the University of California
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
package diva.graph;
import java.util.Iterator;

/**
 * A graph model is an abstraction of a graph implementation and a
 * registration point for GraphListeners. A graph model contains
 * a root graph which in turn contains nodes, composite nodes (i.e.
 * nodes that contain other nodes), and edges between nodes. <p>
 *
 * A graph model provides read-only access to a graph.  The getRoot()
 * method provides access to the root of a (possibly hierarchically
 * nested) graph.  Given that object, nodes() returns an iterator
 * over that graphs contents, and inEdges() and outEdges() can be used
 * to traverse edges.  get/setVisited() are utility functions to help
 * with graph traversals. <p>
 *
 * For read-write access to the graph, use the sub-interface
 * MutableGraphModel.
 *
 * @author Michael Shilman  (michaels@eecs.berkeley.edu)
 * @author John Reekie      (johnr@eecs.berkeley.edu)
 * @version $Id$
 * @rating Yellow
 */
public interface GraphModel {
    /**
     * Add a graph listener to the model.  Graph listeners are
     * notified with a GraphEvent any time the graph is modified.
     */
    public void addGraphListener(GraphListener l);

    /**
     * Return true if this graph or composite
     * node contains the given node.
     */
    public boolean containsNode(Object composite, Object node);

    /**
     * Send an graph event to all of the graph listeners.  This
     * allows manual control of sending graph graph events, or
     * allows the user to send a STRUCTURE_CHANGED after some
     * inner-loop operations.
     *
     * @see #setDispatchEnabled(boolean)
     */
    public void dispatchGraphEvent(GraphEvent e);

    /**
     * Return the root graph of this graph model.
     */
    public Object getRoot();

    /**
     * Return the head node of the given edge.
     */
    public Object getHead(Object edge);

    /**
     * Return the number of nodes contained in
     * this graph or composite node.
     */
    public int getNodeCount(Object composite);

    /**
     * Return the parent graph of this node, return
     * null if there is no parent.
     */
    public Object getParent(Object node);

    /**
     * Return the tail node of this edge.
     */
    public Object getTail(Object edge);

    /**
     * Return the property of the object associated with
     * the given property name.  If no property exists with the given
     * name, then return null.
     */
    public Object getProperty(Object o, String propertyName);

    /**
     * Return the visual object corresponding
     * to the given node, composite, or edge.  If the object does not
     * have a semantic object, then return null.
     */
    public Object getSemanticObject(Object o);

    /**
     * Return true if the given object is a composite
     * node, i.e. it can contain children.
     */
    public boolean isComposite(Object o);

    /**
     * Return whether or not this edge is directed.
     */
    public boolean isDirected(Object edge);

    /**
     * Return true if the given object is an edge in this
     * model.
     */
    public boolean isEdge(Object o);

    /**
     * Return true if the given object is a node
     * in this model.
     */
    public boolean isNode(Object o);

    /**
     * Provide an iterator over the nodes in the
     * given graph or composite node.  This iterator
     * does not necessarily support removal operations.
     */
    public Iterator nodes(Object composite);

    /**
     * Return an iterator over the <i>in</i> edges of this
     * node. This iterator does not support removal operations.
     * If there are no in-edges, an iterator with no elements is
     * returned.
     */
    public Iterator inEdges(Object node);

    /**
     * Return an iterator over the <i>out</i> edges of this
     * node.  This iterator does not support removal operations.
     * If there are no out-edges, an iterator with no elements is
     * returned.
     */
    public Iterator outEdges(Object node);

    /**
     * Remove a graph listener from the model so that
     * the listener will no longer be notified of changes
     * to the graph.
     */
    public void removeGraphListener(GraphListener l);

    /**
     * Turn on/off all event dispatches from this graph model, for use
     * in an inner-loop algorithm.  When turning dispatch back on
     * again, if the client has made changes that listeners should
     * know about, he should create an appropriate STRUCTURE_CHANGED
     * and dispatch it using the dispatchGraphEvent() method.
     *
     * @see dispatchGraphEvent(GraphEvent)
     */
    public void setDispatchEnabled(boolean val);

    /**
     * Set the property of the object associated with
     * the given property name.
     */
    public void setProperty(Object o, String propertyName, Object value);

    /**
     * Set the semantic object corresponding
     * to the given node, composite, or edge.
     */
    public void setSemanticObject(Object o, Object visual);
}

