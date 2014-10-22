/*
 Copyright (c) 1998-2014 The Regents of the University of California
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
package diva.canvas.interactor;

import java.util.Iterator;

import diva.canvas.Figure;
import diva.canvas.FigureDecorator;
import diva.canvas.event.LayerEvent;
import diva.canvas.event.MouseFilter;

/**
 * A SelectionInteractor is attached to an object that can be put
 * into and out of a selection. Associated with each such role
 * is a selection model, which is the selection that figures
 * are added to or removed from.
 *
 * When a mouse pressed event has occurred, all figures associated
 * with the same SelectionModel will be unselected before the new
 * one is selected.  So, to make sure only one figure is selected
 * at a time, do this:
 * SelectionModel m = new SelectionModel();
 * SelectionInteractor s1 = new SelectionInteractor(m);
 * SelectionInteractor s2 = new SelectionInteractor(m);
 *
 * <p> When an item is selected, events are then forwarded to
 * other interactors. If, however, the clicked-on figure has
 * just been removed from the figure, do not forward events.
 *
 * @version $Id$
 * @author John Reekie
 */
public class SelectionInteractor extends CompositeInteractor {
    /** The selection model
     */
    private SelectionModel _selection;

    /** The selection renderer
     */
    private SelectionRenderer _renderer;

    /** The mouse filter for selecting items
     */
    private MouseFilter _selectionFilter = MouseFilter.selectionFilter;

    /** The mouse filter for toggling items
     */
    private MouseFilter _toggleFilter = MouseFilter.alternateSelectionFilter;

    ///////////////////////////////////////////////////////////////////
    ////                         constructors                      ////

    /**
     * Create a new SelectionInteractor with a default selection model and
     * a default selection renderer.
     */
    public SelectionInteractor() {
        super();
        setSelectionRenderer(new BasicSelectionRenderer());
        setSelectionModel(new BasicSelectionModel());
    }

    /**
     * Create a new SelectionInteractor with the given selection model
     * and a null selection renderer.
     */
    public SelectionInteractor(SelectionModel model) {
        super();
        setSelectionRenderer(new BasicSelectionRenderer());
        setSelectionModel(model);
    }

    ///////////////////////////////////////////////////////////////////
    //// public methods

    /**
     * Accept an event if it will be accepted by the selection
     * filters.
     */
    @Override
    public boolean accept(LayerEvent e) {
        return _selectionFilter.accept(e) || _toggleFilter.accept(e)
                || super.accept(e);
    }

    /**
     * Get the mouse filter that controls when this selection
     * filter is activated.
     */
    public MouseFilter getSelectionFilter() {
        return _selectionFilter;
    }

    /**
     * Get the selection model
     */
    public SelectionModel getSelectionModel() {
        return _selection;
    }

    /**
     * Get the selection renderer
     */
    public SelectionRenderer getSelectionRenderer() {
        return _renderer;
    }

    /**
     * Get the mouse filter that controls the toggling of
     * selections
     */
    public MouseFilter getToggleFilter() {
        return _toggleFilter;
    }

    /** Given a mouse event, check that the figure it contains
     * as its event source is selected. By selected, we mean that
     * figure has a SelectionInteractor as its interactor, and that
     * the figure is in the SelectionModel of that interactor.
     */
    public static boolean isSelected(LayerEvent e) {
        Figure f = e.getFigureSource();

        if (f.getInteractor() instanceof SelectionInteractor) {
            SelectionInteractor i = (SelectionInteractor) f.getInteractor();
            return i.getSelectionModel().containsSelection(f);
        }

        return false;
    }

    /** Handle a mouse press event. Add or remove the clicked-on
     * item to or from the selection. If it's still in the selection,
     * pass the event to the superclass to handle.
     */
    @Override
    public void mousePressed(LayerEvent event) {
        if (!isEnabled()) {
            return;
        }

        Figure figure = event.getFigureSource();
        boolean isChanged = false;

        if (_selectionFilter.accept(event)) {
            // If the item is not already in the selection, clear
            // the selection and then add this one.
            if (!_selection.containsSelection(figure)) {
                _selection.clearSelection();
                _selection.addSelection(figure);
                isChanged = true;
            }
        } else if (_toggleFilter.accept(event)) {
            // Toggle the item
            if (_selection.containsSelection(figure)) {
                _selection.removeSelection(figure);
            } else {
                _selection.addSelection(figure);
            }

            isChanged = true;
        }

        // Set the target of all attached drag interactors
        if (_selection.getSelectionCount() > 0
                && _selection.containsSelection(figure)) {
            Object[] target = _selection.getSelectionAsArray();
            Iterator<?> i = interactors();

            while (i.hasNext()) {
                Interactor interactor = (Interactor) i.next();

                if (interactor instanceof DragInteractor) {
                    ((DragInteractor) interactor).setTargetArray(target);
                }
            }
        }

        // Allow superclass to process event
        super.mousePressed(event);

        // Always consume the event if the figure was moved into or
        // out of the selection, regardless of the consuming flag
        if (isChanged) {
            event.consume();
        }
    }

    /**
     * Set the consuming flag of this interactor. This flag is a little
     * more complicated than in simple interactors: if not set, then
     * the event is consumed only if the clicked-on figure is added
     * to or removed from the selection. Otherwise it is not consumed.
     * If the flag is set, then the event is always consumed, thus
     * making it effectively "opaque" to events.
     *
     * <P> Note that the behaviour when the flag is false is the desired
     * behaviour when building panes that have an interactor attached
     * to the background. That way, the event passes through to the background
     * if a figure is hit on but the selection interactor's filters are
     * set up to ignore that particular event.
     *
     * <p> There is a third possibility, which is not supported: never
     * consume events. There is no way to do this currently, as the other
     * two behaviors seemed more likely to be useful. (Also, that behaviour
     * is harder to implement because of interaction with the superclass.)
     */
    @Override
    public void setConsuming(boolean flag) {
        // This method is only here for documentation purposes
        super.setConsuming(flag);
    }

    /**
     * Set the selection model. The existing selection model is
     * cleared first.
     */
    public void setSelectionModel(SelectionModel model) {
        if (_selection != null) {
            _selection.clearSelection();
        }

        _selection = model;
    }

    /**
     * Set the mouse filter that controls when this selection
     * filter is activated.
     */
    public void setSelectionFilter(MouseFilter f) {
        _selectionFilter = f;
    }

    /** Set the prototype decorator for selected figures.  Selected
     * figures will have a copy of this decorator wrapped around them.
     * This call is a convenience short-hand for
     * <pre>
     *    this.setSelectionRenderer(new BasicSelectionRenderer(decorator);
     * </pre>
     * <P>
     * This method nullifies any previous renderers set with
     * setSelectionRenderer();
     */
    public void setPrototypeDecorator(FigureDecorator decorator) {
        _renderer = new BasicSelectionRenderer(decorator);
    }

    /** Set the prototype selection manipulator. Selected figures
     * will have a copy of this manipulator wrapped around them.
     * This method nullifies any previous renderers set with
     * setSelectionRenderer();
     *
     * @deprecated Use setPrototypeDecorator instead
     */
    @Deprecated
    public void setSelectionManipulator(Manipulator manipulator) {
        _renderer = new BasicSelectionRenderer(manipulator);
    }

    /**
     * Set the selection renderer. The object is not used in this class,
     * but can be set here so that other objects that need to render
     * selected objects know how to do so.
     */
    public void setSelectionRenderer(SelectionRenderer r) {
        _renderer = r;
    }

    /**
     * Set the mouse filter that controls the toggling of
     * selections.
     */
    public void setToggleFilter(MouseFilter f) {
        _toggleFilter = f;
    }
}
