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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import diva.canvas.Figure;
import diva.canvas.FigureLayer;
import diva.canvas.interactor.BasicSelectionModel;
import diva.canvas.interactor.SelectionModel;

/**
 * An abstract implementation of the GraphController interface.
 * Concrete subclasses must implement the getNodeController()
 * and getEdgeController() methods, to return the correct
 * controller for a given node or edge.
 *
 * @author         John Reekie (johnr@eecs.berkeley.edu)
 * @author         Michael Shilman (michaels@eecs.berkeley.edu)
 * @author         Steve Neuendorffer (neuendor@eecs.berkeley.edu)
 * @version        $Id$
 * @rating      Red
 */
public abstract class AbstractGraphController implements GraphController {
    /** Map semantic objects to their figure representations
     */
    private HashMap _map = new HashMap();

    /**
     * The graph pane that this is controlling.
     */
    private GraphPane _pane;

    /**
     * The graph that is being displayed.
     */
    private GraphModel _model;

    /** The default selection model
     */
    private SelectionModel _selectionModel = new BasicSelectionModel();

    /** The listener of graph events.
     */
    private ChangeListener _localListener = new ChangeListener();

    /** The list of view listeners.
     */
    private List _graphViewListenerList = new LinkedList();

    /**
     * Construct a graph controller without a parent
     * pane.
     */
    public AbstractGraphController() {
    }

    /** Add an edge to this graph editor and render it
     * from the given tail node to an autonomous site at the
     * given location. Give the new edge the given semanticObject.
     * The "end" flag is either HEAD_END
     * or TAIL_END, from diva.canvas.connector.ConnectorEvent.
     * @exception GraphException If the connector target cannot return a
     * valid site on the node's figure.
     */
    public void addEdge(Object edge, Object node,
            int end, double x, double y) {
        getEdgeController(edge).addEdge(edge, node, end, x, y);
    }

    /** Add an edge to this graph between the given tail and head
     * nodes.  Give the new edge the given semanticObject.
     * @return the new edge.
     */
    public void addEdge(Object edge, Object tail, Object head) {
        getEdgeController(edge).addEdge(edge, tail, head);
    }

    /**
     */
    public void addGraphViewListener(GraphViewListener l) {
        _graphViewListenerList.add(l);
    }

    /**
     * Add the node to this graph editor and place it wherever convenient.
     */
    public void addNode(Object node) {
        NodeController nc = getNodeController(node);
        nc.addNode(node);
    }

    /**
     * Add the node to this graph editor and render it
     * at the given location.
     */
    public void addNode(Object node, double x, double y) {
        NodeController nc = getNodeController(node);
        nc.addNode(node, x, y);
    }

    /**
     * Add the node to this graph editor, inside the given parent node
     * at whatever position is convenient
     */
    public void addNode(Object node, Object parent) {
        NodeController nc = getNodeController(node);
        nc.addNode(node, parent);
    }

    /**
     * Add the node to this graph editor, inside the given parent node
     * and render it at the given location relative to its parent.
     */
    public void addNode(Object node, Object parent, double x, double y) {
        NodeController nc = getNodeController(node);
        nc.addNode(node, parent, x, y);
    }

    /**
     * Remove all figures from the display
     */
    public void clear () {
        // FIXME
    }

    /**
     * Remove the figure for the given edge.
     */
    public void clearEdge(Object edge) {
        EdgeController ec = getEdgeController(edge);
        ec.clearEdge(edge);
    }

    /**
     * Remove the figure for the given node.
     */
    public void clearNode(Object node) {
        NodeController nc = getNodeController(node);
        nc.clearNode(node);
    }

    /**
     * Draw the given edge: create a figure, place it in the canvas,
     * and associate the figure with the edge.  This should only be
     * called when the object is in the model but does not yet have a
     * figure associated with it.
     */
    public Figure drawEdge(Object edge) {
        EdgeController ec = getEdgeController(edge);
        Figure f = ec.drawEdge(edge);
        return f;
    }

    /**
     * Draw the given node: create a figure, place it in the canvas,
     * and associate the figure with the node.  This should only be
     * called when the object is in the model but does not yet have a
     * figure associated with it.  The location of the figure should be
     * set if some location is appropriate for it.
     */
    public Figure drawNode(Object node) {
        NodeController nc = getNodeController(node);
        Figure f = nc.drawNode(node);
        return f;
    }

    /**
     * Draw the given node: create a figure, place it in the figure of the
     * given parent node,
     * and associate the figure with the node.  This should only be
     * called when the object is in the model but does not yet have a
     * figure associated with it.  The location of the figure should be
     * set if some location is appropriate for it.
     */
    public Figure drawNode(Object node, Object parent) {
        NodeController nc = getNodeController(node);
        Figure f = nc.drawNode(node, parent);
        return f;
    }

    /**
     * Given an edge, return the controller associated with that
     * edge.
     */
    public abstract EdgeController getEdgeController(Object edge);

    /**
     * Given an node, return the controller associated with that
     * node.
     */
    public abstract NodeController getNodeController(Object node);

    /**
     * Return the graph being viewed.
     */
    public GraphModel getGraphModel() {
        return _model;
    }

    /**
     * Return the graphics pane of this controller cast as a GraphPane.
     */
    public GraphPane getGraphPane() {
        return (GraphPane) _pane;
    }

    /**
     * Return the figure associated with the given
     * semantic object (node or edge), or null
     * if there is no association.
     */
    public Figure getFigure(Object semanticObj) {
        return (Figure)_map.get(semanticObj);
    }

    /**
     * Get the default selection model
     */
    public SelectionModel getSelectionModel () {
        return _selectionModel;
    }

    /**
     * Remove the given edge.  Find the edge controller associated with that
     * edge and delegate to that edge controller.
     */
    public void removeEdge(Object edge) {
        getEdgeController(edge).removeEdge(edge);
    }

    /** Remove the given view listener.
     */
    public void removeGraphViewListener(GraphViewListener l) {
        _graphViewListenerList.remove(l);
    }

    /**
     * Remove the given node.  Find the node controller associated with that
     * node and delegate to that node controller.
     */
    public void removeNode(Object node) {
        getNodeController(node).removeNode(node);
    }

    /**
     * Render the current graph again by recreating the figures for all
     * nodes and edges, but do not alter the connectivity in the graph.
     * This should be called when changes to renderers are made.
     */
    public void rerender () {
        // FIXME: it would be nice to be able to do this without all
        // stupid business with the selection model.
        List selectedEdges = new LinkedList();
        List selectedNodes = new LinkedList();

        // Remove any old objects that no longer exist in the model Do
        // the edges first, then the nodes, since we cannot blow away
        // selection on an edge before getting rid of the endpoints of
        // the edge.
        Iterator figures = (new HashSet(_map.values())).iterator();
        while (figures.hasNext()) {
            Figure figure = (Figure)figures.next();
            Object object = figure.getUserObject();

            if (_model.isEdge(object)) {
                if (!GraphUtilities.isPartiallyContainedEdge(object,
                        _model.getRoot(),
                        _model)) {
                    if (_selectionModel.containsSelection(figure)) {
                        _selectionModel.removeSelection(figure);
                    }
                    clearEdge(object);
                }
            }
        }
        figures = (new HashSet(_map.values())).iterator();
        while (figures.hasNext()) {
            Figure figure = (Figure)figures.next();
            Object object = figure.getUserObject();

            if (_model.isNode(object)) {
                if (!GraphUtilities.isContainedNode(object,
                        _model.getRoot(),
                        _model)) {
                    if (_selectionModel.containsSelection(figure)) {
                        _selectionModel.removeSelection(figure);
                    }
                    clearNode(object);
                }
            }
        }

        // Save the selected edges.
        Iterator edges = GraphUtilities.totallyContainedEdges(
                _model.getRoot(), _model);
        while (edges.hasNext()) {
            Object edge = edges.next();
            Figure oldFigure = getFigure(edge);
            boolean selected = _selectionModel.containsSelection(oldFigure);
            if (selected) {
                selectedEdges.add(edge);
            }
        }
        // Save the selected nodes.
        Iterator nodes = (GraphUtilities.nodeSet(_model.getRoot(), _model)).iterator();
        while (nodes.hasNext()) {
            Object node = nodes.next();
            Figure oldFigure = getFigure(node);
            boolean selected = _selectionModel.containsSelection(oldFigure);
            if (selected) {
                selectedNodes.add(node);
            }
        }
        // Clear all the selections (note that there may be selected objects
        // which no longer exist!)
        _selectionModel.clearSelection();

        // draw the nodes.
        nodes = _model.nodes(_model.getRoot());
        while (nodes.hasNext()) {
            Object node = nodes.next();
            drawNode(node);
        }
        nodes = (GraphUtilities.nodeSet(_model.getRoot(), _model)).iterator();
        while (nodes.hasNext()) {
            Object node = nodes.next();
            if (selectedNodes.contains(node)) {
                _selectionModel.addSelection(getFigure(node));
            }
        }
        // draw the edges that are connected to any of the above nodes.
        edges = GraphUtilities.partiallyContainedEdges(
                _model.getRoot(), _model);
        while (edges.hasNext()) {
            Object edge = edges.next();
            drawEdge(edge);
            if (selectedEdges.contains(edge)) {
                _selectionModel.addSelection(getFigure(edge));
            }
        }
    }

    /**
     * Rerender the given edge by replacing its figure with a new figure.
     * This should be called if the state of the edge has changed in such a
     * way that the rendering should change.
     */
    public void rerenderEdge (Object edge) {
        // FIXME this is way overkill.
        rerender();
        /*
          Figure oldFigure = getFigure(edge);
          boolean selected = _selectionModel.containsSelection(oldFigure);
          if (selected) {
          _selectionModel.removeSelection(oldFigure);
          }
          clearEdge(edge);
          Figure newFigure = drawEdge(edge);
          if (selected)
          _selectionModel.addSelection(newFigure);
        */
    }

    /**
     * Rerender the given node by replacing its figure with a new
     * figure.  This should be called if the state of the node has
     * changed in such a way that the rendering should change.
     */
    public void rerenderNode (Object node) {
        // FIXME this is way overkill.
        rerender();
        /*
          Figure oldFigure = getFigure(node);
          boolean selected = _selectionModel.containsSelection(oldFigure);
          if (selected) {
          _selectionModel.removeSelection(oldFigure);
          }
          Point2D center;
          if (oldFigure != null) {
          center = CanvasUtilities.getCenterPoint(oldFigure.getBounds());
          clearNode(node);
          } else {
          // no previous figure.  which means that we are probably
          // rendering for the first time.
          center = null; //FIXME: layout?
          }
          Figure newFigure = drawNode(node);
          if (center != null) {
          // place the new figure where the old one was.
          CanvasUtilities.translateTo(newFigure,
          center.getX(), center.getY());
          }
          if (selected)
          _selectionModel.addSelection(newFigure);
        */
    }

    /**
     * Set the graph being viewed. If there is a graph already
     * and it contains data, delete the figures of that graph's
     * nodes and edges (but don't modify the graph itself).
     */
    public void setGraphModel(GraphModel model) {
        Iterator i;
        // FIXME we shouldn't have to cast this.
        FigureLayer layer = getGraphPane().getForegroundLayer();

        if (_model != null) {
            // Clear existing figures
            Object root = _model.getRoot();
            if (_model.getNodeCount(root) != 0) {
                for (i = _model.nodes(root); i.hasNext(); ) {
                    clearNode(i.next());
                }
                for (i = GraphUtilities.localEdges(root, _model);
                     i.hasNext(); ) {
                    clearEdge(i.next());
                }
            }
            _model.removeGraphListener(_localListener);
        }

        // Set the graph
        _model = model;

        if (_model != null) {
            _model.addGraphListener(_localListener);
            Object root = _model.getRoot();
            GraphEvent evt = new GraphEvent(new Object(),
                    GraphEvent.STRUCTURE_CHANGED, root);
            _localListener.structureChanged(evt);
        }
    }

    /**
     * Set the figure associated with the given semantic object (node
     * or edge).  A null figure clears the association.
     */
    public void setFigure(Object semanticObj, Figure f) {
        if (f == null) {
            _map.remove(semanticObj);
        }
        else {
            _map.put(semanticObj, f);
        }
    }

    /**
     * Set the graph pane. This is called by the GraphPane.
     * FIXME: should this be package private?
     */
    public void setGraphPane (GraphPane pane) {
        _pane = pane;
        initializeInteraction();
    }

    /**
     * Set the default selection model. The caller is expected to ensure
     * that the old model is empty before calling this.
     */
    public void setSelectionModel (SelectionModel m) {
        _selectionModel = m;
    }

    /**
     * Dispatch the given graph view event to all registered graph view
     * listeners.  This method is generally only called by subclasses and
     * representatives of thse subclasses, such as a node controller or
     * an edge controller.
     */
    public void dispatch(GraphViewEvent e) {
        for (Iterator i = _graphViewListenerList.iterator(); i.hasNext(); ) {
            GraphViewListener l = (GraphViewListener)i.next();
            switch(e.getID()) {
            case GraphViewEvent.NODE_MOVED:
                l.nodeMoved(e);
                break;
            case GraphViewEvent.EDGE_ROUTED:
                l.edgeRouted(e);
                break;
            case GraphViewEvent.NODE_DRAWN:
                l.nodeDrawn(e);
                break;
            case GraphViewEvent.EDGE_DRAWN:
                l.edgeDrawn(e);
                break;
            }
        }
    }

    /**
     * Initialize all interaction on the graph pane. This method
     * is called by the setGraphPane() method, and must be overridden
     * by subclasses.
     * This initialization cannot be done in the constructor because
     * the controller does not yet have a reference to its pane
     * at that time.
     */
    protected abstract void initializeInteraction ();

    /**
     * Debugging output.
     */
    private void debug(String s) {
        System.err.println("AbstractGraphController " + s);
    }

    /**
     * This inner class responds to changes in the graph
     * we are controlling.
     */
    private class ChangeListener implements GraphListener {
        /**
         * An edge's head has been changed in a registered
         * graph or one of its subgraphs.  The added edge
         * is the "source" of the event.  The previous head
         * is accessible via e.getOldValue().
         */
        public void edgeHeadChanged(GraphEvent e) {
            if (e.getSource() != AbstractGraphController.this) {
                rerenderEdge(e.getTarget());
            }
        }

        /**
         * An edge's tail has been changed in a registered
         * graph or one of its subgraphs.  The added edge
         * is the "source" of the event.  The previous tail
         * is accessible via e.getOldValue().
         */
        public void edgeTailChanged(GraphEvent e) {
            if (e.getSource() != AbstractGraphController.this) {
                rerenderEdge(e.getTarget());
            }
        }

        /**
         * A node has been been added to the registered
         * graph or one of its subgraphs.  The added node
         * is the "source" of the event.
         */
        public void nodeAdded(GraphEvent e) {
            if (e.getSource() != AbstractGraphController.this) {
                drawNode(e.getTarget());
            }
        }

        /**
         * A node has been been deleted from the registered
         * graphs or one of its subgraphs.  The deleted node
         * is the "source" of the event.  The previous parent
         * graph is accessible via e.getOldValue().
         */
        public void nodeRemoved(GraphEvent e) {
            if (e.getSource() != AbstractGraphController.this) {
                //Remove the figure from the view
                clearNode(e.getTarget());
            }
        }

        /**
         * The structure of the event's "source" graph has
         * been drastically changed in some way, and this
         * event signals the listener to refresh its view
         * of that graph from model.
         */
        public void structureChanged(GraphEvent e) {
            if (e.getSource() != AbstractGraphController.this) {
                rerender();
                /* Object root = e.getTarget();

                   //FIXME - this could be optimized--
                   //        we may not need to rerender every
                   //        node.

                   for (Iterator i = _model.nodes(root); i.hasNext(); ) {
                   rerenderNode(i.next());
                   }
                   for (Iterator i = GraphUtilities.localEdges(root, _model);
                   i.hasNext(); ) {
                   rerenderEdge(i.next());
                   }
                */
            }
        }
    }
}



