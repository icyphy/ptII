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

import java.awt.event.InputEvent;
import java.awt.geom.Rectangle2D;

import diva.canvas.CanvasComponent;
import diva.canvas.CompositeFigure;
import diva.canvas.Figure;
import diva.canvas.FigureLayer;
import diva.canvas.Site;
import diva.canvas.connector.AutonomousSite;
import diva.canvas.connector.Connector;
import diva.canvas.connector.ConnectorAdapter;
import diva.canvas.connector.ConnectorEvent;
import diva.canvas.connector.ConnectorManipulator;
import diva.canvas.connector.ConnectorTarget;
import diva.canvas.connector.PerimeterTarget;
import diva.canvas.connector.Terminal;
import diva.canvas.event.MouseFilter;
import diva.canvas.interactor.BasicSelectionRenderer;
import diva.canvas.interactor.Interactor;
import diva.canvas.interactor.SelectionDragger;
import diva.canvas.interactor.SelectionModel;

/**
 * A basic implementation of EdgeController, which works with
 * graphs that have edges connecting simple nodes.
 *
 * @author         Michael Shilman (michaels@eecs.berkeley.edu)
 * @version        $Id$
 * @rating      Red
 */
public class BasicEdgeController implements EdgeController {

    /** The selection interactor for drag-selecting nodes
     */
    private SelectionDragger _selectionDragger;

    /** The connector target
     */
    private ConnectorTarget _connectorTarget;

    /** The filter for control operations
     */
    private MouseFilter _controlFilter = new MouseFilter (
            InputEvent.BUTTON1_MASK,
            InputEvent.CTRL_MASK);

    private Interactor _interactor;
    private EdgeRenderer _renderer;
    private GraphController _controller;

    /**
     * Create a new edge controller with basic interaction.  Specifically,
     * this method creates an edge interactor and initializes its manipulator
     * so that edges get attached appropriately.  Furthermore, the edge
     * interactor is initialized with the selection model of the graph
     * controller.  The manipulator is activated by either a regular click
     * or a control click.  Also initialize a basic connector target that
     * generally attaches to the perimeter of nodes, except that it is
     * smart enough to properly handle terminals.
     */
    public BasicEdgeController (GraphController controller) {
        _controller = controller;
        SelectionModel sm = controller.getSelectionModel();
        _interactor = new EdgeInteractor(sm);

        // Create and set up the manipulator for connectors
        ConnectorManipulator manipulator = new ConnectorManipulator();
        manipulator.setSnapHalo(4.0);
        manipulator.addConnectorListener(new EdgeDropper());
        ((EdgeInteractor)_interactor).setPrototypeDecorator(manipulator);

        // The mouse filter needs to accept regular click or control click
        MouseFilter handleFilter = new MouseFilter(1, 0, 0);
        manipulator.setHandleFilter(handleFilter);

        // Create and set up the target for connectors
        PerimeterTarget ct = new PerimeterTarget() {
                // Accept the head if the model graph model allows it.
                public boolean acceptHead (Connector c, Figure f) {
                    Object node = f.getUserObject();
                    Object edge = c.getUserObject();
                    MutableGraphModel model =
                        (MutableGraphModel)_controller.getGraphModel();
                    if (model.isNode(node) &&
                            model.isEdge(edge) &&
                            model.acceptHead(edge, node)) {
                        return super.acceptHead(c, f);
                    } else return false;
                }

                // Accept the tail if the model graph model allows it.
                public boolean acceptTail (Connector c, Figure f) {
                    Object node = f.getUserObject();
                    Object edge = c.getUserObject();
                    MutableGraphModel model =
                        (MutableGraphModel)_controller.getGraphModel();
                    if (model.isNode(node) &&
                            model.isEdge(edge) &&
                            model.acceptTail(edge, node)) {
                        return super.acceptTail(c, f);
                    } else return false;
                }

                /** If we have any terminals, then return the connection
                 *  site of the terminal instead of a new perimeter site.
                 */
                public Site getHeadSite(Figure f, double x, double y) {
                    if (f instanceof Terminal) {
                        Site site = ((Terminal)f).getConnectSite();
                        return site;
                    } else {
                        return super.getHeadSite(f, x, y);
                    }
                }
            };
        setConnectorTarget(ct);
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
        MutableGraphModel model =
            (MutableGraphModel)_controller.getGraphModel();
        Figure nf = _controller.getFigure(node);
        FigureLayer layer = _controller.getGraphPane().getForegroundLayer();
        Site headSite, tailSite;


        // Temporary sites.  One of these will get blown away later.
        headSite = new AutonomousSite(layer, x, y);
        tailSite = new AutonomousSite(layer, x, y);

        // Render the edge.
        Connector c = render(edge, layer, tailSite, headSite);

        try {
            //Attach the appropriate end of the edge to the node.
            if (end == ConnectorEvent.TAIL_END) {
                tailSite = getConnectorTarget().getTailSite(c, nf, x, y);
                if (tailSite == null) {
                    throw new RuntimeException("Invalid connector target: " +
                            "no valid site found for tail of new connector.");
                }
                model.setEdgeTail(_controller, edge, node);
                c.setTailSite(tailSite);
            } else {
                headSite = getConnectorTarget().getHeadSite(c, nf, x, y);
                if (headSite == null) {
                    throw new RuntimeException("Invalid connector target: " +
                            "no valid site found for head of new connector.");
                }
                model.setEdgeHead(_controller, edge, node);
                c.setHeadSite(headSite);
            }
        } catch (GraphException ex) {
            // If an error happened then blow away the edge, and rethrow
            // the exception
            removeEdge(edge);
            throw ex;
        }
    }

    /**
     * Add an edge to this graph between the given tail and head
     * nodes.  Give the new edge the given semanticObject.
     */
    public void addEdge(Object edge, Object tail, Object head) {
        // Connect the edge
        MutableGraphModel model =
            (MutableGraphModel)_controller.getGraphModel();
        model.connectEdge(_controller, edge, tail, head);

        drawEdge(edge);
    }

    /**
     * Remove the figure for the given edge, but do not remove the
     * edge from the graph model.
     */
    public void clearEdge(Object edge) {
        Figure f = _controller.getFigure(edge);
        if (f != null) {
            CanvasComponent container = f.getParent();
            f.setUserObject(null);
            _controller.setFigure(edge, null);
            if (container instanceof FigureLayer) {
                ((FigureLayer)container).remove(f);
            } else if (container instanceof CompositeFigure) {
                ((CompositeFigure)container).remove(f);
            }
        }
    }

    /**
     * Draw the edge and add it to the layer, establishing
     * a two-way correspondence between the model and the
     * view.  If the edge already has been associated with some figure in
     * the view, then use any information in that figure to help draw the
     * edge.
     */
    public Figure drawEdge(Object edge) {
        GraphModel model = _controller.getGraphModel();
        FigureLayer layer = _controller.getGraphPane().getForegroundLayer();
        Object tail = model.getTail(edge);
        Object head = model.getHead(edge);

        Connector connector = (Connector)_controller.getFigure(edge);
        Figure tailFigure = _controller.getFigure(tail);
        Figure headFigure = _controller.getFigure(head);

        Site tailSite;
        Site headSite;
        // If the tail is not attached,
        if (tailFigure == null) {
            // Then try to find the old tail site.
            if (connector != null) {
                tailSite = connector.getTailSite();
            } else {
                // FIXME try to manufacture a site.
                //throw new RuntimeException("drawEdge failed: could not find" +
                //                           " a tail site.");
                return null;
            }
        } else {
            // Get a new tail site based on the tail figure.
            Rectangle2D bounds = tailFigure.getBounds();
            tailSite = getConnectorTarget().getTailSite(tailFigure,
                    bounds.getCenterX(), bounds.getCenterY());
        }

        // If the head is not attached,
        if (headFigure == null) {
            // Then try to find the old head site.
            if (connector != null) {
                headSite = connector.getHeadSite();
            } else {
                // FIXME try to manufacture a site.
                //throw new RuntimeException("drawEdge failed: could not find" +
                //                           " a head site.");
                return null;
            }
        } else {
            // Get a new head site based on the head figure.
            Rectangle2D bounds = headFigure.getBounds();
            headSite = getConnectorTarget().getHeadSite(headFigure,
                    bounds.getCenterX(), bounds.getCenterY());
        }

        // If we did have an old figure, throw it away.
        if (connector != null) {
            clearEdge(edge);
        }

        // Create the figure
        Connector c = render(edge, layer, tailSite, headSite);
        _controller.dispatch(new GraphViewEvent(this,
                GraphViewEvent.EDGE_DRAWN,
                edge));
        return c;
    }

    /**
     * Get the target used to find sites on nodes to connect to.
     */
    public ConnectorTarget getConnectorTarget () {
        return _connectorTarget;
    }

    /**
     * Get the graph controller that this controller is contained in.
     */
    public GraphController getController() {
        return _controller;
    }

    /**
     * Get the interactor given to edge figures.
     */
    public Interactor getEdgeInteractor () {
        return _interactor;
    }

    /**
     * Return the edge renderer for this view.
     */
    public EdgeRenderer getEdgeRenderer() {
        return _renderer;
    }

    /**
     * Remove the edge.
     */
    public void removeEdge(Object edge) {
        clearEdge(edge);
        MutableGraphModel model =
            (MutableGraphModel)_controller.getGraphModel();
        model.setEdgeHead(_controller, edge, null);
        model.setEdgeTail(_controller, edge, null);
        _controller.getGraphPane().repaint();
    }

    /**
     * Set the target used to find sites on nodes to connect to.  This
     * sets the local connector target (which is often used to find the
     * starting point of an edge) and the manipulator's connector target, which
     * is used after the connector is being dragged.
     */
    public void setConnectorTarget (ConnectorTarget t) {
        _connectorTarget = t;

        // FIXME: This is rather dangerous because it assumes a
        // basic selection renderer.
        BasicSelectionRenderer selectionRenderer = (BasicSelectionRenderer)
            ((EdgeInteractor)_interactor).getSelectionRenderer();
        ConnectorManipulator manipulator = (ConnectorManipulator)
            selectionRenderer.getDecorator();
        manipulator.setConnectorTarget(t);
    }

    /**
     * Set the interactor given to edge figures.
     */
    public void setEdgeInteractor (Interactor interactor) {
        _interactor = interactor;
    }

    /**
     * Set the edge renderer for this view.
     */
    public void setEdgeRenderer(EdgeRenderer er) {
        _renderer = er;
    }

    /** Render the edge on the given layer between the two sites.
     */
    public Connector render(Object edge, FigureLayer layer,
            Site tailSite, Site headSite) {
        Connector ef = getEdgeRenderer().render(edge, tailSite, headSite);
        ef.setInteractor(getEdgeInteractor());
        ef.setUserObject(edge);
        _controller.setFigure(edge, ef);

        layer.add(ef);
        ef.route();
        return ef;
    }

    ///////////////////////////////////////////////////////////////
    //// EdgeDropper

    /** An inner class that handles interactive changes to connectivity.
     */
    protected class EdgeDropper extends ConnectorAdapter {
        /**
         * Called when a connector end is dropped--attach or
         * detach the edge as appropriate.
         */
        public void connectorDropped(ConnectorEvent evt) {
            Connector c = evt.getConnector();
            Figure f = evt.getTarget();
            Object edge = c.getUserObject();
            Object node = (f == null) ? null : f.getUserObject();
            MutableGraphModel model =
                (MutableGraphModel) _controller.getGraphModel();
            try {
                switch (evt.getEnd()) {
                case ConnectorEvent.HEAD_END:
                    model.setEdgeHead(_controller, edge, node);
                    break;
                case ConnectorEvent.TAIL_END:
                    model.setEdgeTail(_controller, edge, node);
                    break;
                default:
                    throw new IllegalStateException(
                            "Cannot handle both ends of an edge being dragged.");
                }
            } catch (GraphException ex) {
                SelectionModel selectionModel =
                    _controller.getSelectionModel();
                // If it is illegal then blow away the edge.
                if (selectionModel.containsSelection(c)) {
                    selectionModel.removeSelection(c);
                }
                removeEdge(edge);
                throw ex;
            }
        }
    }
}


