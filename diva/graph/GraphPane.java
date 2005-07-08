/*
 Copyright (c) 1998-2005 The Regents of the University of California
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

//import diva.graph.layout.LayoutTarget;
import diva.canvas.GraphicsPane;

/**
 * The display part of the JGraph user-level widget.
 *
 * @see JGraph
 * @author         Michael Shilman
 * @version        $Id$
 * @Pt.AcceptedRating Red
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
    public GraphPane(GraphModel model) {
        // Set up the controller
        _model = model;
    }

    /** Create a new graph pane with the given controller and model.
     */
    public GraphPane(GraphController controller, GraphModel model) {
        // Set up the controller
        this(model);
        _controller = controller;
        _controller.setGraphPane(this);
        _controller.setGraphModel(model);
    }

    /** Get the graph controller
     */
    public GraphController getGraphController() {
        return _controller;
    }

    /**
     * Return the graph being viewed.
     */
    public GraphModel getGraphModel() {
        return _model;
    }
}
