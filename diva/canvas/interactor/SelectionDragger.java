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
package diva.canvas.interactor;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Iterator;

import diva.canvas.Figure;
import diva.canvas.FigureDecorator;
import diva.canvas.FigureLayer;
import diva.canvas.GeometricSet;
import diva.canvas.GraphicsPane;
import diva.canvas.OverlayLayer;
import diva.canvas.event.EventLayer;
import diva.canvas.event.LayerEvent;
import diva.canvas.event.MouseFilter;
import diva.util.CompoundIterator;

/** A class that implements rubber-banding on a canvas. It contains
 * references to one or more instances of SelectionInteractor, which it
 * notifies whenever dragging on the canvas covers or uncovers items.
 * The SelectionDragger requires three layers: an Event Layer,
 * which it listens to perform drag-selection, an OutlineLayer, on
 * which it draws the drag-selection box, and a FigureLayer, which it
 * selects figures on. It can also accept a GraphicsPane in its
 * constructor, in which case it will use the background event layer,
 * outline layer, and foreground event layer from that pane.
 *
 * @version $Id$
 * @author John Reekie
 */
public class SelectionDragger extends DragInteractor {

    /* The overlay layer
     */
    private OverlayLayer _overlayLayer;

    /* The event layer
     */
    private EventLayer _eventLayer;

    /* The figure layer
     */
    private FigureLayer _figureLayer;

    /* The rubber-band
     */
    private Rectangle2D _rubberBand = null;

    /* The set of figures covered by the rubber-band
     */
    private GeometricSet _intersectedFigures;

    /** A hash-set containing those figures
     */
    private HashSet _currentFigures;

    /** A hash-set containing figures that overlap the rubber-band
     * but are not "hit"
     */
    private HashSet _holdovers;

    /* The origin points
     */
    private double _originX;
    private double _originY;

    /** The list of selection models selected by this dragger.
     */
    private List _selectionModels = new ArrayList();

    /** The mouse filter for selecting items
     */
    private MouseFilter _selectionFilter = MouseFilter.selectionFilter;

    /** The mouse filter for toggling items
     */
    private MouseFilter _toggleFilter = MouseFilter.alternateSelectionFilter;

    /** The selection mode flags
     */
    private boolean _isSelecting;
    private boolean _isToggling;

    ///////////////////////////////////////////////////////////////////
    ////                         constructors                      ////

    /**
     * Create a new SelectionDragger
     */
    public SelectionDragger () {
        super();
    }

    /**
     * Create a new SelectionDragger attached to the given graphics
     * pane.
     */
    public SelectionDragger (GraphicsPane gpane) {
        super();
        setOverlayLayer(gpane.getOverlayLayer());
        setEventLayer(gpane.getBackgroundEventLayer());
        setFigureLayer(gpane.getForegroundLayer());
    }

    ///////////////////////////////////////////////////////////////////
    //// public methods

    /**
     * Add the given selection model to the set of selection models
     * selected by this dragger.  When drag-selecting, only figures
     * that have a selection interactor with a selection model in this
     * list are added to the selection model.
     */
    public void addSelectionModel (SelectionModel model) {
        if ( !(_selectionModels.contains(model))) {
            _selectionModels.add(model);
        }
    }

    /**
     * Clear the selection in all the relevant selection interactors.
     */
    public void clearSelection () {
        Iterator models = _selectionModels.iterator();
        while (models.hasNext()) {
            SelectionModel model = (SelectionModel) models.next();
            model.clearSelection();
        }
    }

    /**
     * Contract the selection by removing an item from it and
     * removing highlight rendering. If the figure is not in
     * the selection, do nothing.
     */
    public void contractSelection (SelectionInteractor i, Figure figure) {
        if (i.getSelectionModel().containsSelection(figure)) {
            i.getSelectionModel().removeSelection(figure);
        }
    }

    /**
     * Expand the selection by adding an item to it and adding
     * highlight rendering to it. If the
     * figure is already in the selection, do nothing.
     */
    public void expandSelection (SelectionInteractor i, Figure figure) {
        if ( !(i.getSelectionModel().containsSelection(figure))) {
            i.getSelectionModel().addSelection(figure);
        }
    }

    /**
     * Get the layer that drag rectangles are drawn on
     */
    public OverlayLayer getOverlayLayer () {
        return _overlayLayer;
    }

    /**
     * Get the layer that drag events are listened on
     */
    public EventLayer getEventLayer () {
        return _eventLayer;
    }

    /**
     * Get the layer that figures are selected on
     */
    public FigureLayer getFigureLayer () {
        return _figureLayer;
    }

    /**
     * Get the mouse filter that controls when this selection
     * filter is activated.
     */
    public MouseFilter getSelectionFilter () {
        return _selectionFilter;
    }

    /**
     * Get the mouse filter that controls the toggling of
     * selections
     */
    public MouseFilter getToggleFilter () {
        return _toggleFilter;
    }

    /** Reshape the rubber-band, swapping coordinates if necessary.
     * Any figures that are newly included or excluded from
     * the drag region are added to or removed from the appropriate
     * selection.
     */
    public void mouseDragged (LayerEvent event) {
        if (!isEnabled()) {
            return;
        }
        if (!_isToggling && !_isSelecting) {
            return;
        }
        if (_rubberBand == null) {
            // This should never happen, but it does.
            return;
        }

        double x = event.getLayerX();
        double y = event.getLayerY();
        double w;
        double h;

        // Figure out the coordinates of the rubber band
        _overlayLayer.repaint(_rubberBand);
        if ( x < _originX ) {
            w = _originX - x;
        } else {
            w = x -_originX;
            x = _originX;
        }
        if ( y < _originY ) {
            h = _originY - y;
        } else {
            h = y - _originY;
            y = _originY;
        }
        _rubberBand.setFrame(x,y,w,h);
        _overlayLayer.repaint(_rubberBand);

        // Update the intersected figure set
        _intersectedFigures.setGeometry(_rubberBand);
        HashSet freshFigures = new HashSet();
        for (Iterator i = _intersectedFigures.figures(); i.hasNext(); ) {
            Figure f = (Figure)i.next();
            if (f instanceof FigureDecorator) {
                f = ((FigureDecorator)f).getDecoratedFigure();
            }
            if (f.hit(_rubberBand)) {
                freshFigures.add(f);
            } else {
                _holdovers.add(f);
            }
        }
        for (Iterator i = ((HashSet)_holdovers.clone()).iterator();
             i.hasNext(); ) {
            Figure f = (Figure)i.next();
            if (f.hit(_rubberBand)) {
                freshFigures.add(f);
                _holdovers.remove(f);
            }
        }
        // stale = current-fresh;
        HashSet staleFigures = (HashSet) _currentFigures.clone();
        staleFigures.removeAll(freshFigures);
        // current = fresh-current
        HashSet temp = (HashSet) freshFigures.clone();
        freshFigures.removeAll(_currentFigures);
        _currentFigures = temp;

        // If in selection mode, add and remove figures
        if (_isSelecting) {
            // Add figures to the selection
            Iterator i = freshFigures.iterator();
            while (i.hasNext()) {
                Figure f = (Figure) i.next();
                Interactor r = f.getInteractor();
               if (r != null &&
                        r instanceof SelectionInteractor) {
                    SelectionInteractor interactor = (SelectionInteractor)r;
                    if(_selectionModels.contains(
                               interactor.getSelectionModel())) {
                        expandSelection((SelectionInteractor) r, f);
                    }
                }
            }

            // Remove figures from the selection
            i = staleFigures.iterator();
            while (i.hasNext()) {
                Figure f = (Figure) i.next();
                Interactor r = f.getInteractor();
                if (r != null &&
                        r instanceof SelectionInteractor) {
                    SelectionInteractor interactor = (SelectionInteractor)r;
                    if(_selectionModels.contains(
                               interactor.getSelectionModel())) {
                        contractSelection((SelectionInteractor) r, f);
                    }
                }
            }
        } else {
            // Toggle figures into and out of the selection
            Iterator i = new CompoundIterator(
                    freshFigures.iterator(),
                    staleFigures.iterator());
            while (i.hasNext()) {
                Figure f = (Figure) i.next();
                Interactor r = f.getInteractor();
                if (r != null &&
                        r instanceof SelectionInteractor) {
                    SelectionInteractor interactor = (SelectionInteractor)r;
                    if(_selectionModels.contains(
                               interactor.getSelectionModel())) {
                        if (interactor.getSelectionModel().containsSelection(f)) {
                            contractSelection(interactor, f);
                        } else {
                            expandSelection(interactor, f);
                        }
                    }
                }
            }
        }
        // Consume the event
        if (isConsuming()) {
            event.consume();
        }
    }

    /** Clear the selection, and create the rubber-band
     */
    public void mousePressed (LayerEvent event) {
        if (!isEnabled()) {
            return;
        }
        // Check mouse event, set flags, etc
        _isSelecting = _selectionFilter.accept(event);
        _isToggling = _toggleFilter.accept(event);

        if (!_isToggling && !_isSelecting) {
            return;
        }

        // Do it
        _originX = event.getLayerX();
        _originY = event.getLayerY();
        _rubberBand = new Rectangle2D.Double(
                _originX,
                _originY,
                0.0,
                0.0);

        _overlayLayer.add(_rubberBand);
        _overlayLayer.repaint(_rubberBand);

        _intersectedFigures =
            _figureLayer.getFigures().getIntersectedFigures(_rubberBand);
        _currentFigures = new HashSet();
        _holdovers = new HashSet();

        // Clear all selections
        if (_isSelecting) {
            clearSelection();
        }
        // Consume the event
        if (isConsuming()) {
            event.consume();
        }
    }

    /** Delete the rubber-band
     */
    public void mouseReleased (LayerEvent event) {
        if (!isEnabled()) {
            return;
        }
        if (_rubberBand == null) {
            // This should never happen, but it does.
            return;
        }
        terminateDragSelection();

        // Consume the event
        if (isConsuming()) {
            event.consume();
        }
    }

    /**
     * Remove a selection model from the list of models selected by
     * this dragger.
     */
    public void removeSelectionModel (SelectionModel model) {
        if (_selectionModels.contains(model) ) {
            _selectionModels.remove(model);
        }
    }

    /**
     * Get the selection interactors
     */
    public Iterator selectionModels() {
        return _selectionModels.iterator();
    }

    /**
     * Set the layer that drag rectangles are drawn on
     */
    public void setOverlayLayer (OverlayLayer l) {
        _overlayLayer = l;
    }

    /**
     * Set the layer that drag events are listened on
     */
    public void setEventLayer (EventLayer l) {
        if (_eventLayer != null) {
            _eventLayer.removeLayerListener(this);
        }
        _eventLayer = l;
        _eventLayer.addLayerListener(this);
    }

    /**
     * Set the layer that figures are selected on
     */
    public void setFigureLayer (FigureLayer l) {
        _figureLayer = l;
    }

    /**
     * Set the mouse filter that controls when this selection
     * filter is activated.
     */
    public void setSelectionFilter(MouseFilter f) {
        _selectionFilter = f;
    }

    /**
     * Set the mouse filter that controls the toggling of
     * selections.
     */
    public void setToggleFilter(MouseFilter f) {
        _toggleFilter = f;
    }

    /** Terminate drag-selection operation. This must only be called
     * from events that are triggered during a drag operation.
     */
    public void terminateDragSelection () {
        if (!_isToggling && !_isSelecting) {
            return;
        }

        _overlayLayer.repaint(_rubberBand);
        _overlayLayer.remove(_rubberBand);
        _rubberBand = null;
        _currentFigures = null;
        _holdovers = null;
    }
}


