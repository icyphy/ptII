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
package diva.graph.basic;

import java.awt.event.InputEvent;

import diva.canvas.event.LayerEvent;
import diva.canvas.event.MouseFilter;
import diva.canvas.interactor.AbstractInteractor;
import diva.canvas.interactor.SelectionDragger;
import diva.canvas.interactor.SelectionInteractor;
import diva.graph.BasicEdgeController;
import diva.graph.BasicNodeController;
import diva.graph.EdgeCreator;
import diva.graph.GraphPane;
import diva.graph.NodeController;
import diva.graph.NodeInteractor;
import diva.graph.SimpleGraphController;

/**
 * A basic implementation of GraphController, which works with
 * simple graphs that have edges connecting simple nodes. It
 * sets up some simple interaction on its view's pane.
 *
 * @author         Michael Shilman (michaels@eecs.berkeley.edu)
 * @version        $Id$
 * @rating      Red
 */
public class BasicGraphController extends SimpleGraphController {
    /**
     * The global count for the default node/edge creation.
     */
    private int _globalCount = 0;

    /** The selection interactor for drag-selecting nodes
     */
    private SelectionDragger _selectionDragger;

    /** The interactor for creating new nodes
     */
    private NodeCreator _nodeCreator;

    /** The interactor that interactively creates edges
     */
    private EdgeCreator _edgeCreator;

    /** The filter for control operations
     */
    private MouseFilter _controlFilter = new MouseFilter (
            InputEvent.BUTTON1_MASK,
            InputEvent.CTRL_MASK);

    /**
     * Create a new basic controller with default node and edge controllers.
     */
    public BasicGraphController () {
        NodeController nc = new BasicNodeController(this);
        nc.setNodeRenderer(new BasicNodeRenderer(this));
        setNodeController(nc);

        BasicEdgeController ec = new BasicEdgeController(this);
        ec.setEdgeRenderer(new BasicEdgeRenderer());
        setEdgeController(ec);

        //        addGraphViewListener(new IncrementalLayoutListener(new IncrLayoutAdapter(new LevelLayout(new BasicLayoutTarget(this))), null));
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
        _selectionDragger = new SelectionDragger(pane);
        _selectionDragger.addSelectionModel(getSelectionModel());
        
        // Create a listener that creates new nodes
        _nodeCreator = new NodeCreator();
        _nodeCreator.setMouseFilter(_controlFilter);
        pane.getBackgroundEventLayer().addInteractor(_nodeCreator);

        // Create the interactor that drags new edges.
        _edgeCreator = new EdgeCreator(this) {
                public Object createEdge() {
                    Object semanticObject = new Integer(_globalCount++);
                    BasicGraphModel bgm = (BasicGraphModel)getGraphModel();
                    return bgm.createEdge(semanticObject);
                }
            };
        _edgeCreator.setMouseFilter(_controlFilter);
        ((NodeInteractor)getNodeController().getNodeInteractor()).addInteractor(_edgeCreator);
    }

    ///////////////////////////////////////////////////////////////
    //// NodeCreator

    /** An inner class that places a node at the clicked-on point
     * on the screen, if control-clicked with mouse button 1. This
     * needs to be made more customizable.
     */
    protected class NodeCreator extends AbstractInteractor {
        public void mousePressed(LayerEvent e) {
            Object semanticObject = new Integer(_globalCount++);
            BasicGraphModel bgm = (BasicGraphModel)getGraphModel();
            Object node = bgm.createNode(semanticObject);
            addNode(node,  e.getLayerX(), e.getLayerY());
        }
    }
}


