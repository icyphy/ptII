/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 *
 */

package diva.canvas.toolbox;

import diva.canvas.GraphicsPane;
import diva.canvas.event.MouseFilter;
import diva.canvas.interactor.DragInteractor;
import diva.canvas.interactor.Manipulator;
import diva.canvas.interactor.SelectionDragger;
import diva.canvas.interactor.SelectionInteractor;
import diva.canvas.interactor.SelectionRenderer;

/** A basic controller implementation. This controller creates a
 * useful and common interaction that can be used in simple
 * applications. A single interactor, accessed through getRole(), provides
 * selection and dragging. Clients that wish to use this default
 * interaction can give this interactor to figures
 * that they add to the foreground layer of the corresponding pane.
 *
 * @version        $Revision$
 * @author         John Reekie
 */
public class BasicController {

    /** The interactor that drags objects by default
     */
    private DragInteractor _dragInteractor;

    /** The selection interactor.
     */
    private SelectionInteractor _selectionInteractor;

    /** The selection renderer.
     */
    private SelectionRenderer _selectionRenderer;

    /** The selection dragger
     */
    private SelectionDragger _selectionDragger;

    /** The pane that this controller is associated with.
     */
    private GraphicsPane _pane;

    /** Create a new controller for the given pane
     */
    public BasicController (GraphicsPane pane) {
        _pane = pane;

        // Create the selection interactor
        _selectionInteractor = new SelectionInteractor();

        // Create a selection drag-selector
        _selectionDragger = new SelectionDragger(pane);
        _selectionDragger.addSelectionInteractor(_selectionInteractor);

        // Add the drag interactor to the selection interactor so
        // selected items are dragged
        _dragInteractor = new DragInteractor();
        _dragInteractor.setSelectiveEnabled(true);
        _dragInteractor.setMouseFilter(new MouseFilter(1, 0, 0));
        _selectionInteractor.addInteractor(_dragInteractor);

    }

    /** Get the drag interactor
     */
    public DragInteractor getDragInteractor () {
        return _dragInteractor;
    }

    /** Get the selection interactor
     */
    public SelectionDragger getSelectionDragger () {
        return _selectionDragger;
    }

    /** Get the selection renderer
     */
    public SelectionRenderer getSelectionRenderer () {
        return _selectionInteractor.getSelectionRenderer();
    }

    /** Get the selection interactor
     */
    public SelectionInteractor getSelectionInteractor () {
        return _selectionInteractor;
    }

    /** Set the prototype selection manipulator. Selected figures
     * will have a copy of this manipulator wrapped around them.
     * This method nullifies any previous renderers set with
     * setSelectionRenderer();
     */
    public void setSelectionManipulator (Manipulator manipulator) {
        _selectionInteractor.setPrototypeDecorator(manipulator);
    }

    /** Set the selection renderer. Selected figures will be highlighted
     * with this renderer.
     */
    public void setSelectionRenderer (SelectionRenderer renderer) {
        _selectionInteractor.setSelectionRenderer(renderer);
    }
}


