/*
 * $Id$
 *
 * Copyright (c) 1998 The Regents of the University of California.
 * All rights reserved.  See the file COPYRIGHT for details.
 */
package ptolemy.schematic.util;

import diva.graph.*; 

import diva.graph.model.Graph;
import diva.graph.model.GraphImpl;
import diva.graph.model.Node;
import diva.graph.model.Edge;

import diva.canvas.Figure;
import diva.canvas.FigureLayer;
import diva.canvas.GraphicsPane;
import diva.canvas.Site;

import diva.canvas.connector.AutonomousSite;
import diva.canvas.connector.CenterSite;
import diva.canvas.connector.PerimeterSite;
import diva.canvas.connector.PerimeterTarget;
import diva.canvas.connector.Connector;
import diva.canvas.connector.ConnectorAdapter;
import diva.canvas.connector.ConnectorManipulator;
import diva.canvas.connector.ConnectorEvent;
import diva.canvas.connector.ConnectorListener;
import diva.canvas.connector.ConnectorTarget;

import diva.canvas.event.LayerAdapter;
import diva.canvas.event.LayerEvent;
import diva.canvas.event.MouseFilter;

import diva.canvas.interactor.Interactor;
import diva.canvas.interactor.AbstractInteractor;
import diva.canvas.interactor.GrabHandle;
import diva.canvas.interactor.SelectionInteractor;
import diva.canvas.interactor.SelectionModel;
import diva.canvas.interactor.SelectionDragger;
import diva.canvas.interactor.BoundsManipulator;

import diva.util.Filter;

import java.awt.event.InputEvent;
import java.util.HashMap;

/**
 * A Graph Controller for the Ptolemy II schematic editor.  
 * Terminal creation: Ctrl-button 1
 * Edge creation: Ctrl-Button 1 Drag
 * Entity creation: Shift-Button 1
 * Edges can connect to Terminals, but not entities.
 *
 * @author Steve Neuendorffer (neuendor@eecs.berkeley.edu)
 * @version	$Id$
 * @rating      Red
 */
public class EditorGraphController extends GraphController {
 
    /** The selection interactor for drag-selecting nodes
     */
    private SelectionDragger _selectionDragger;

    /** The interactor for creating new terminals
     */
    private TerminalCreator _terminalCreator;

    /** The interactor for creating new entities
     */
    private EntityCreator _entityCreator;

    /** The interactor that interactively creates edges
     */
    private EdgeCreator _edgeCreator;

    /** The filter for control operations
     */
    private MouseFilter _controlFilter = new MouseFilter (
            InputEvent.BUTTON1_MASK,
            InputEvent.CTRL_MASK);

    /** The filter for shift operations 
     */
    private MouseFilter _shiftFilter = new MouseFilter (
            InputEvent.BUTTON1_MASK,
            InputEvent.SHIFT_MASK);

    /**
     * Create a new basic controller with default terminal and edge interactors.
     */
    public EditorGraphController () {
        // The interactors attached to terminals and edges
        SelectionModel sm = getSelectionModel();
        NodeInteractor ni = new NodeInteractor(this, sm);
	ni.setSelectionManipulator(new BoundsManipulator());
        EdgeInteractor ei = new EdgeInteractor(this, sm);
        setNodeInteractor(ni);
        setEdgeInteractor(ei);

        // Create and set up the target for connectors
        PerimeterTarget ct = new PerimeterTarget() {
	    public boolean accept (Figure f) {
                return (f.getUserObject() instanceof SchematicTerminal);
		// FIXME Used needs something like: ||
		// (f instanceof FigureWrapper &&
                //             ((FigureWrapper)f).getChild().instanceof Node);
            }
	};
        setConnectorTarget(ct);

        // Create and set up the manipulator for connectors
        ConnectorManipulator manipulator = new ConnectorManipulator();
        manipulator.setSnapHalo(4.0);
        manipulator.setConnectorTarget(ct);
        manipulator.addConnectorListener(new EdgeDropper());
        ei.setSelectionManipulator(manipulator);

        // The mouse filter needs to accept regular click or control click
        MouseFilter handleFilter = new MouseFilter(1, 0, 0);
        manipulator.setHandleFilter(handleFilter);
    }

    /**
     * Initialize all interaction on the graph pane. This method
     * is called by the setGraphPane() method of the superclass.
     * This initialization cannot be done in the constructor because
     * the controller does not yet have a reference to its pane
     * at that time.
     */
    protected void initializeInteraction () {
        GraphPane pane = getGraphPane();

        // Create and set up the selection dragger
        SelectionDragger _selectionDragger = new SelectionDragger(pane);
        _selectionDragger.addSelectionInteractor(getEdgeInteractor());
        _selectionDragger.addSelectionInteractor(getNodeInteractor());

        // Create a listener that creates new terminals
        _terminalCreator = new TerminalCreator();
        _terminalCreator.setMouseFilter(_controlFilter);
        pane.getBackgroundEventLayer().addInteractor(_terminalCreator);

       // Create a listener that creates new terminals
        _entityCreator = new EntityCreator();
        _entityCreator.setMouseFilter(_shiftFilter);
        pane.getBackgroundEventLayer().addInteractor(_entityCreator);

        // Create the interactor that drags new edges.
        _edgeCreator = new EdgeCreator();
        _edgeCreator.setMouseFilter(_controlFilter);
        getNodeInteractor().addInteractor(_edgeCreator);
    }

    ///////////////////////////////////////////////////////////////
    //// TerminalCreator

    /** An inner class that places a terminal at the clicked-on point
     * on the screen, if control-clicked with mouse button 1. This
     * needs to be made more customizable.
     */
    protected class TerminalCreator extends AbstractInteractor {
        public void mousePressed(LayerEvent e) {
            Node n = getGraphImpl().createNode(null);
            addNode(n, e.getLayerX(), e.getLayerY());
        }
    }

    ///////////////////////////////////////////////////////////////
    //// EntityCreator

    /** An inner class that places a terminal at the clicked-on point
     * on the screen, if control-clicked with mouse button 1. This
     * needs to be made more customizable.
     */
    protected class EntityCreator extends AbstractInteractor {
        public void mousePressed(LayerEvent e) {
            Node n = getGraphImpl().createCompositeNode(null);
            addNode(n, e.getLayerX(), e.getLayerY());
        }
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
            Edge e = (Edge)c.getUserObject();
            Node n = (f == null) ? null : (Node)f.getUserObject();
            GraphImpl impl = getGraphImpl();
            switch (evt.getEnd()) {
            case ConnectorEvent.HEAD_END:
                impl.setEdgeHead(e, n);
                break;
            case ConnectorEvent.TAIL_END:
                impl.setEdgeTail(e, n);
                break;
            default:
                throw new IllegalStateException(
                        "Cannot handle both ends of an edge being dragged.");
            }
        }
    }

    ///////////////////////////////////////////////////////////////
    //// EdgeCreator

    /** An interactor that interactively drags edges from one terminal
     * to another.
     */
    protected class EdgeCreator extends AbstractInteractor {
        public void mousePressed(LayerEvent e) {
            Figure source = e.getFigureSource();
	    Node sourcenode = (Node) source.getUserObject();
	    if(!(sourcenode instanceof SchematicTerminal)) return;

            FigureLayer layer = (FigureLayer) e.getLayerSource();

            // Create a new edge
            Edge edge = getGraphImpl().createEdge(null);

            // Add it to the editor
            addEdge(edge,
                    sourcenode,
                    ConnectorEvent.TAIL_END,
                    e.getLayerX(),
                    e.getLayerY());

            // Add it to the selection so it gets a manipulator, and
            // make events go to the grab-handle under the mouse
            Figure ef = (Figure) edge.getVisualObject();
            getSelectionModel().addSelection(ef);
            ConnectorManipulator cm = (ConnectorManipulator) ef.getParent();
            GrabHandle gh = cm.getHeadHandle();
            layer.grabPointer(e, gh);
        }
    }
}
