/*
@Copyright (c) 1998-2004 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

						PT_COPYRIGHT_VERSION_2
						COPYRIGHTENDKEY


 */
package diva.graph;

import diva.canvas.Figure;
import diva.canvas.interactor.SelectionModel;

/**
 * A class that represents the main component of a typical graph
 * editor.  It is associated with a GraphPane, and manages
 * interactors, renderers, and other parameters of user interaction
 * (e.g. connector targets) to implement a flexible graph editor.
 * <p>
 * The interaction and rendering of nodes is managed by node
 * controller objects, each associated with particular kind of node.
 * Edges are similarly managed by an edge controller object.
 *
 * @author         John Reekie (johnr@eecs.berkeley.edu)
 * @author         Michael Shilman (michaels@eecs.berkeley.edu)
 * @author         Steve Neuendorffer (neuendor@eecs.berkeley.edu)
 * @version        $Id$
 * @rating      Red
 */
public interface GraphController {
    /** Add an edge to this graph editor and render it
     * from the given tail node to an autonomous site at the
     * given location. Give the new edge the given semanticObject.
     * The "end" flag is either HEAD_END
     * or TAIL_END, from diva.canvas.connector.ConnectorEvent.
     * @exception GraphException If the connector target cannot return a
     * valid site on the node's figure.
     */
    public void addEdge(Object edge, Object node,
            int end, double x, double y);

    /** Add an edge to this graph between the given tail and head
     * nodes.  Give the new edge the given semanticObject.
     */
    public void addEdge(Object edge, Object tail, Object head);

    /**
     */
    public void addGraphViewListener(GraphViewListener l);

    /**
     * Add the node to this graph editor and place it wherever convenient.
     */
    public void addNode(Object node);

    /**
     * Add the node to this graph editor and render it
     * at the given location.
     */
    public void addNode(Object node, double x, double y);

    /**
     * Add the node to this graph editor, inside the given parent node
     * at whatever position is convenient
     */
    public void addNode(Object node, Object parent);

    /**
     * Add the node to this graph editor, inside the given parent node
     * and render it at the given location relative to its parent.
     */
    public void addNode(Object node, Object parent, double x, double y);

    /**
     * Remove all figures from the display
     */
    public void clear ();

    /**
     * Remove the figure for the given edge.
     */
    public void clearEdge(Object edge);

    /**
     * Remove the figure for the given node.
     */
    public void clearNode(Object node);

    /**
     * Draw the given edge: create a figure, place it in the canvas,
     * and associate the figure with the edge.  This should only be
     * called when the object is in the model but does not yet have a
     * figure associated with it.
     */
    public Figure drawEdge(Object edge);

    /**
     * Draw the given node: create a figure, place it in the canvas,
     * and associate the figure with the node.  This should only be
     * called when the object is in the model but does not yet have a
     * figure associated with it.  The location of the figure should be
     * set if some location is appropriate for it.
     */
    public Figure drawNode(Object node);

    /**
     * Draw the given node: create a figure, place it in the figure of the
     * given parent node,
     * and associate the figure with the node.  This should only be
     * called when the object is in the model but does not yet have a
     * figure associated with it.  The location of the figure should be
     * set if some location is appropriate for it.
     */
    public Figure drawNode(Object node, Object parent);

    /**
     * Given an edge, return the controller associated with that
     * edge.
     */
    public EdgeController getEdgeController(Object edge);

    /**
     * Given an node, return the controller associated with that
     * node.
     */
    public NodeController getNodeController(Object node);

    /**
     * Return the graph being viewed.
     */
    public GraphModel getGraphModel();

    /**
     * Return the graphics pane of this controller cast as a GraphPane.
     */
    public GraphPane getGraphPane();

    /**
     * Return the figure associated with the given
     * semantic object (node or edge), or null
     * if there is no association.
     */
    public Figure getFigure(Object semanticObj);

    /**
     * Get the default selection model
     */
    public SelectionModel getSelectionModel();

    /**
     * Remove the given edge.  Find the edge controller associated with that
     * edge and delegate to that edge controller.
     */
    public void removeEdge(Object edge);

    /** Remove the given view listener.
     */
    public void removeGraphViewListener(GraphViewListener l);

    /**
     * Remove the given node.  Find the node controller associated with that
     * node and delegate to that node controller.
     */
    public void removeNode(Object node);

    /**
     * Render the current graph again by recreating the figures for all
     * nodes and edges, but do not alter the connectivity in the graph.
     * This should be called when changes to renderers are made.
     */
    public void rerender();

    /**
     * Rerender the given edge by replacing its figure with a new figure.
     * This should be called if the state of the edge has changed in such a
     * way that the rendering should change.
     */
    public void rerenderEdge(Object edge);

    /**
     * Rerender the given node by replacing its figure with a new
     * figure.  This should be called if the state of the node has
     * changed in such a way that the rendering should change.
     */
    public void rerenderNode(Object node);

    /**
     * Set the graph being viewed. If there is a graph already
     * and it contains data, delete the figures of that graph's
     * nodes and edges (but don't modify the graph itself).
     */
    public void setGraphModel(GraphModel model);

    /**
     * Set the figure associated with the given semantic object (node
     * or edge).  A null figure clears the association.
     */
    public void setFigure(Object semanticObj, Figure f);

    /**
     * Set the graph pane. This is called by the GraphPane.
     * FIXME: should this be package private?
     */
    public void setGraphPane (GraphPane pane);

    /**
     * Set the default selection model. The caller is expected to ensure
     * that the old model is empty before calling this.
     */
    public void setSelectionModel(SelectionModel m);

    /**
     * Dispatch the given graph view event to all registered graph view
     * listeners.  This method is generally only called by subclasses and
     * representatives of thse subclasses, such as a node controller or
     * an edge controller.
     */
    public void dispatch(GraphViewEvent e);
}



