/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.graph;

import diva.canvas.interactor.DragInteractor;
import diva.canvas.interactor.SelectionInteractor;
import diva.canvas.interactor.SelectionModel;

/**
 * An interactor for nodes.
 *
 * @author         Michael Shilman (michaels@eecs.berkeley.edu)
 * @author         John Reekie (johnr@eecs.berkeley.edu)
 * @version        $Revision$
 * @rating Red
 */
public class NodeInteractor extends SelectionInteractor {

    /** The interactor that drags nodes
     */
    private DragInteractor _nodeDragInteractor;

    /**
     * The graph controller that manages this interactor.
     */
    private GraphController _controller;

    /** Create a new node interactor that belongs to the given
     * controller.
     */
    public NodeInteractor (GraphController controller) {
        super();
        _controller = controller;
        setDragInteractor(new NodeDragInteractor(controller));
    }

    /** Create a new node interactor that belongs to the given
     * controller and that uses the given selection model
     */
    public NodeInteractor (GraphController controller, SelectionModel sm) {
        this(controller);
        super.setSelectionModel(sm);
    }

    /** Get the interactor that drags nodes
     */
    public DragInteractor getDragInteractor () {
        return _nodeDragInteractor;
    }

    /** Return the graph controller that manages this interactor.
     */
    public GraphController getGraphController(){
        return _controller;
    }

    /** Set the interactor that drags nodes
     */
    public void setDragInteractor (DragInteractor i) {
        if (_nodeDragInteractor != null) {
            removeInteractor(_nodeDragInteractor);
        }
        if (i != null) {
            addInteractor(i);
        }
        _nodeDragInteractor = i;
    }
}


