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
 * @version        $Id$
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
        _selectionDragger.addSelectionModel(
                _selectionInteractor.getSelectionModel());

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


