/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.graph;

import diva.canvas.Figure;
import diva.canvas.FigureLayer;
import diva.canvas.connector.ConnectorEvent;
import diva.canvas.connector.ConnectorManipulator;
import diva.canvas.event.LayerEvent;
import diva.canvas.interactor.AbstractInteractor;
import diva.canvas.interactor.GrabHandle;

/** An interactor that interactively drags edges from one node
 * to another.
 *
 * @author         Steve Neuendorffer (neuendor@eecs.berkeley.edu)
 * @version        $Revision$
 * @rating      Red
 */
public abstract class EdgeCreator extends AbstractInteractor {
    // The Controller that this creator is using.
    GraphController _controller;

    public EdgeCreator(GraphController controller) {
        _controller = controller;
    }

    /** Create a new edge, add it to the graph controller and add
     * the connector to the selection.
     */
    public void mousePressed(LayerEvent e) {
        Figure source = e.getFigureSource();
        FigureLayer layer = (FigureLayer) e.getLayerSource();

        Object edge = createEdge();

        // Add it to the editor
        // FIXME what about an error?
        _controller.addEdge(edge,
                            source.getUserObject(),
                            ConnectorEvent.TAIL_END,
                            e.getLayerX(),
                            e.getLayerY());

        // Add it to the selection so it gets a manipulator, and
        // make events go to the grab-handle under the mouse
        Figure ef = _controller.getFigure(edge);
        _controller.getSelectionModel().addSelection(ef);
        ConnectorManipulator cm = (ConnectorManipulator) ef.getParent();
        GrabHandle gh = cm.getHeadHandle();
        layer.grabPointer(e, gh);
    }

    /** Create a new Edge.  Subclasses should implement this method to create
     * an object that is consistent with the graphmodel being used.
     */
    public abstract Object createEdge();
}



