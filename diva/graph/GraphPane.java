/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.graph;

//import diva.graph.layout.LayoutTarget;
import diva.canvas.GraphicsPane;

/**
 * The display part of the JGraph user-level widget.
 *
 * @see JGraph
 * @author         Michael Shilman (michaels@eecs.berkeley.edu)
 * @version        $Revision$
 * @rating Red
 */
public class GraphPane extends GraphicsPane {
    /** The controller
     */
    private GraphController _controller;

    /**
     * The graph that is being displayed.
     */
    private GraphModel _model;

    /** Create a new graph pane with a view on the given model.
     */
    public GraphPane (GraphModel model) {
        // Set up the controller
        _model = model;
    }

    /** Create a new graph pane with the given controller and model.
     */
    public GraphPane (GraphController controller, GraphModel model) {
        // Set up the controller
        this(model);
        _controller = controller;
        _controller.setGraphPane(this);
        _controller.setGraphModel(model);
    }

    /** Get the graph controller
     */
    public GraphController getGraphController () {
        return _controller;
    }

    /**
     * Return the graph being viewed.
     */
    public GraphModel getGraphModel() {
        return _model;
    }
}



