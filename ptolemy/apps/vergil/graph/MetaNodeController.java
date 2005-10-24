/*
 * $Id$
 *
 * Copyright (c) 1998-2005 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package ptolemy.apps.vergil.graph;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;

import diva.canvas.CanvasComponent;
import diva.canvas.CanvasUtilities;
import diva.canvas.CompositeFigure;
import diva.canvas.Figure;
import diva.canvas.FigureLayer;
import diva.canvas.GraphicsPane;
import diva.canvas.Site;
import diva.canvas.connector.AutonomousSite;
import diva.canvas.connector.CenterSite;
import diva.canvas.connector.Connector;
import diva.canvas.connector.ConnectorAdapter;
import diva.canvas.connector.ConnectorEvent;
import diva.canvas.connector.ConnectorListener;
import diva.canvas.connector.ConnectorManipulator;
import diva.canvas.connector.ConnectorTarget;
import diva.canvas.connector.PerimeterSite;
import diva.canvas.connector.PerimeterTarget;
import diva.canvas.event.LayerAdapter;
import diva.canvas.event.LayerEvent;
import diva.canvas.event.MouseFilter;
import diva.canvas.interactor.AbstractInteractor;
import diva.canvas.interactor.GrabHandle;
import diva.canvas.interactor.Interactor;
import diva.canvas.interactor.SelectionDragger;
import diva.canvas.interactor.SelectionInteractor;
import diva.canvas.interactor.SelectionModel;
import diva.graph.*;
import diva.util.Filter;

import java.awt.event.InputEvent;
import java.awt.geom.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


/**
 *
 * @author         Steve Neuendorffer
 * @version        $Revision$
 * @Pt.AcceptedRating      Red
 */
public class MetaNodeController extends CompositeEntity
    implements NodeController {
    /** The selection interactor for drag-selecting nodes
     */
    private SelectionDragger _selectionDragger;

    /** The filter for control operations
     */
    private MouseFilter _controlFilter = new MouseFilter(InputEvent.BUTTON1_MASK,
            InputEvent.CTRL_MASK);

    /**
     * Create a new basic controller with default node and edge interactors.
     */
    public MetaNodeController(CompositeEntity container, String name)
        throws IllegalActionException, NameDuplicationException {
        super(container, name);

        //        SelectionModel sm = getController().getSelectionModel();
        //       setNodeInteractor(new NodeInteractor(getController(), sm));
        setNodeInteractor(new MetaInteractor(this, "_interactor"));
        setNodeRenderer(new MetaNodeRenderer(this, "_renderer"));
    }

    /** Given a node, add it to this graph editor and perform a layout
     * on the new node.
     */
    public void addNode(Object node) {
        // FIXME this may cause a classcast exception.
        MutableGraphModel model = (MutableGraphModel) getController()
                                                                  .getGraphModel();
        model.addNode(getController(), node, model.getRoot());
        drawNode(node);
    }

    /** Add the node to this graph editor and render it
     * at the given location.
     */
    public void addNode(Object node, double x, double y) {
        MutableGraphModel model = (MutableGraphModel) getController()
                                                                  .getGraphModel();
        model.addNode(getController(), node, model.getRoot());

        Figure nf = drawNode(node);
        CanvasUtilities.translateTo(nf, x, y);
    }

    /**
     * Add the node to this graph editor, inside the given parent node
     * and place it where convenient
     */
    public void addNode(Object node, Object parent) {
        MutableGraphModel model = (MutableGraphModel) getController()
                                                                  .getGraphModel();
        model.addNode(getController(), node, parent);
        drawNode(node, parent);
    }

    /**
     * Add the node to this graph editor, inside the given parent node
     * and render it at the given location relative to its parent.
     */
    public void addNode(Object node, Object parent, double x, double y) {
        MutableGraphModel model = (MutableGraphModel) getController()
                                                                  .getGraphModel();
        model.addNode(getController(), node, parent);

        Figure nf = drawNode(node, parent);
        CanvasUtilities.translateTo(nf, x, y);
    }

    /**
     * Remove the figure for the given node.
     */
    public void clearNode(Object node) {
        GraphModel model = getController().getGraphModel();

        for (Iterator i = model.outEdges(node); i.hasNext();) {
            Object edge = i.next();
            getController().clearEdge(edge);
        }

        for (Iterator i = model.inEdges(node); i.hasNext();) {
            Object edge = i.next();
            getController().clearEdge(edge);
        }

        Figure f = getController().getFigure(node);

        if (f != null) {
            CanvasComponent container = f.getParent();
            f.setUserObject(null);
            getController().setFigure(node, null);

            if (container instanceof FigureLayer) {
                ((FigureLayer) container).remove(f);
            } else if (container instanceof CompositeFigure) {
                ((CompositeFigure) container).remove(f);
            }
        }
    }

    /**
     * Render the given node and add the resulting figure to the foreground
     * layer of the graph pane.  If the node was previously rendered, then
     * infer the new location of the figure from the old.
     */
    public Figure drawNode(Object node) {
        Figure oldFigure = getController().getFigure(node);

        // Infer the location for the new node.
        Point2D center;

        if (oldFigure != null) {
            center = CanvasUtilities.getCenterPoint(oldFigure.getBounds());
            clearNode(node);
        } else {
            // no previous figure.  which means that we are probably
            // rendering for the first time.
            center = null; //FIXME: layout?
        }

        Figure newFigure = _renderNode(node);
        System.out.println("renderedFigure = " + newFigure);
        getController().getGraphPane().getForegroundLayer().add(newFigure);

        // Now draw the contained nodes, letting them go where they want to.
        _drawChildren(node);

        if (center != null) {
            // place the new figure where the old one was, if there
            // was an old figure.
            CanvasUtilities.translateTo(newFigure, center.getX(), center.getY());
        }

        getController().dispatch(new GraphViewEvent(this,
                GraphViewEvent.NODE_DRAWN, node));

        return newFigure;
    }

    /**
     * Render the given node and add the resulting figure to the given
     * node's figure, which is assumed to be a CompositeFigure
     * in the controller's graph pane.
     */
    public Figure drawNode(Object node, Object parent) {
        // FIXME what if node was previously rendered?
        Figure newFigure = _renderNode(node);
        CompositeFigure cf = (CompositeFigure) getController().getFigure(parent);
        cf.add(newFigure);

        // Now draw the contained nodes, letting them go where they want to.
        _drawChildren(node);

        getController().dispatch(new GraphViewEvent(this,
                GraphViewEvent.NODE_DRAWN, node));

        return newFigure;
    }

    /**
     * Return the graph controller containing this controller.
     */
    public GraphController getController() {
        return (GraphController) getContainer();
    }

    /**
     * Return the node interactor associated with this controller.
     */
    public Interactor getNodeInteractor() {
        List list = entityList(Interactor.class);

        if (list.size() > 0) {
            return (Interactor) list.get(0);
        }

        return null;
    }

    /**
     * Return the node renderer associated with this controller.
     */
    public NodeRenderer getNodeRenderer() {
        List list = entityList(NodeRenderer.class);

        if (list.size() > 0) {
            System.out.println("NodeRenderer = " + list.get(0));
            return (NodeRenderer) list.get(0);
        }

        return null;
    }

    /**
     * Initialize all interaction on the graph pane. This method
     * is called by the setGraphPane() method of the superclass.
     * This initialization cannot be done in the constructor because
     * the controller does not yet have a reference to its pane
     * at that time.
     */
    protected void initializeInteraction() {
        GraphPane pane = getController().getGraphPane();

        // Create and set up the selection dragger
        _selectionDragger = new SelectionDragger(pane);
        _selectionDragger.addSelectionInteractor((NodeInteractor) getNodeInteractor());
    }

    /**
     * Remove the node.
     */
    public void removeNode(Object node) {
        // FIXME why isn't this symmetric with addNode?
        MutableGraphModel model = (MutableGraphModel) getController()
                                                                  .getGraphModel();

        // clearing the nodes is responsible for clearing any edges that are
        // connected
        if (model.isComposite(node)) {
            for (Iterator i = model.nodes(node); i.hasNext();) {
                Object insideNode = i.next();
                getController().clearNode(insideNode);
            }
        }

        clearNode(node);

        // we assume that the model will remove any edges that are connected.
        model.removeNode(getController(), node);
        getController().getGraphPane().repaint();
    }

    /**
     * Set the node interactor for this controller
     */
    public void setNodeInteractor(Interactor interactor) {
        if (!(interactor instanceof ComponentEntity)) {
            throw new RuntimeException("Interactor " + interactor
                + " is not a component entity");
        }

        try {
            Iterator iterator = entityList(Interactor.class).iterator();

            while (iterator.hasNext()) {
                ((ComponentEntity) iterator.next()).setContainer(null);
            }

            ((ComponentEntity) interactor).setContainer(this);
        } catch (KernelException ex) {
            throw new RuntimeException("Interactor " + interactor
                + " could not be set");
        }
    }

    /**
     * Set the node renderer for this controller
     */
    public void setNodeRenderer(NodeRenderer renderer) {
        if (!(renderer instanceof ComponentEntity)) {
            throw new RuntimeException("Renderer " + renderer
                + " is not a component entity");
        }

        try {
            Iterator iterator = entityList(NodeRenderer.class).iterator();

            while (iterator.hasNext()) {
                ((ComponentEntity) iterator.next()).setContainer(null);
            }

            ((ComponentEntity) renderer).setContainer(this);
        } catch (KernelException ex) {
            throw new RuntimeException("Renderer " + renderer
                + " could not be set");
        }
    }

    /**
     * Render the given node using the node renderer.  Set the interactor
     * of the resulting figure to the node interactor.
     */
    protected Figure _renderNode(Object node) {
        Figure newFigure = getNodeRenderer().render(node);
        newFigure.setInteractor(getNodeInteractor());
        newFigure.setUserObject(node);
        getController().setFigure(node, newFigure);
        return newFigure;
    }

    /**
     * Draw the children of the given node.
     */
    protected void _drawChildren(Object node) {
        GraphModel model = getController().getGraphModel();

        if (model.isComposite(node)) {
            Iterator children = model.nodes(node);

            while (children.hasNext()) {
                Object child = children.next();
                getController().drawNode(child, node);
            }
        }
    }
}
