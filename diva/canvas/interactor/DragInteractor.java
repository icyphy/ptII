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

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;

import diva.canvas.Figure;
import diva.canvas.event.LayerEvent;
import diva.canvas.event.LayerEventMulticaster;
import diva.canvas.event.LayerListener;
import diva.util.ArrayIterator;

/**
 * An interactor that responds to mouse drag events. It adds
 * the notion of constraints, so that dragging can be limited
 * to certain coordinates or "snapped" to suitable locations,
 * and a "target" array, which contain the figure or figures
 * that are dragged.
 *
 * @version $Id$
 * @author John Reekie
 */
public class DragInteractor extends AbstractInteractor {
    /** The set of constraints
     */
    private ArrayList<PointConstraint> _constraints;

    /** The target array. This is an array of objects to make it
     * easier to use with SelectionModel.
     */
    private transient Object[] _targetArray;

    /** Layer listeners
     */
    private transient LayerListener _layerListener;

    /* The most recent coordinates
     */
    private double _prevX = 0.0;

    private double _prevY = 0.0;

    /* Enable only if the figure in the event is in the selection
     */
    private boolean _selectiveEnabled;

    ///////////////////////////////////////////////////////////////////
    //// public methods

    /** Add the given layer listener to this interactor.  Any event that is
     * received by this interactor will be passed on to the listener after
     * it is handled by this interactor.
     */
    public void addLayerListener(LayerListener l) {
        _layerListener = LayerEventMulticaster.add(_layerListener, l);
    }

    /** Append a constraint to the list of constraints on
     * this interactor.
     */
    public void appendConstraint(PointConstraint constraint) {
        if (_constraints == null) {
            _constraints = new ArrayList<PointConstraint>();
        }

        _constraints.add(constraint);
    }

    /** Constrain a point using the current constraints.  The given
     * point will be modified according to the current
     * constraints. This method <i>does not</i> trigger constraint
     * events. The caller must be careful to make a copy of the passed
     * point if it is not guaranteed that changing the point will not
     * affect other objects with a reference to it.
     */
    public void constrainPoint(Point2D p) {
        if (_constraints != null) {

            for (PointConstraint c : _constraints) {
                c.constrain(p);
            }
        }
    }

    /** Fire a layer event.
     */
    public void fireLayerEvent(LayerEvent event) {
        if (_layerListener != null) {
            int id = event.getID();

            switch (id) {
            case MouseEvent.MOUSE_PRESSED:
                _layerListener.mousePressed(event);
                break;

            case MouseEvent.MOUSE_DRAGGED:
                _layerListener.mouseDragged(event);
                break;

            case MouseEvent.MOUSE_RELEASED:
                _layerListener.mouseReleased(event);
                break;
            }
        }
    }

    /** Get the flag that says that the interactor responds only
     * if the figure being moused on is selected. By default, this
     * flag is false.
     */
    public boolean getSelectiveEnabled() {
        return _selectiveEnabled;
    }

    /** Get the target array.
     */
    public Object[] getTargetArray() {
        return _targetArray;
    }

    /** Get the current value of the X coordinate
     */
    public double getX() {
        return _prevX;
    }

    /** Get the current value of the Y coordinate
     */
    public double getY() {
        return _prevY;
    }

    /** Constrain the point and move the target if the mouse
     * move. The target movement is done by the translate()
     * method, which can be overridden to change the behaviour.
     * Nothing happens if the interactor is not enabled, or if it
     * is "selective enabled" but not in the selection.
     */
    @Override
    public void mouseDragged(LayerEvent e) {
        if (!isEnabled() || _selectiveEnabled
                && !SelectionInteractor.isSelected(e)) {
            return;
        }

        if (getMouseFilter() == null || getMouseFilter().accept(e)) {
            // Constrain the point
            Point2D p = e.getLayerPoint();
            constrainPoint(p);

            // Translate and consume if the point changed
            double x = p.getX();
            double y = p.getY();
            double deltaX = x - _prevX;
            double deltaY = y - _prevY;

            if (deltaX != 0 || deltaY != 0) {
                translate(e, deltaX, deltaY);
                fireLayerEvent(e);
            }

            _prevX = x;
            _prevY = y;

            // Consume the event
            if (isConsuming()) {
                e.consume();
            }
        }
    }

    /** Handle a mouse press on a figure or layer. Set the target
     * to be the figure contained in the event. Call the setup()
     * method in case there is additional setup to do, then
     * constrain the point and remember it.
     * Nothing happens if the interactor is not enabled, or if it
     * is "selective enabled" but not in the selection.
     */
    @Override
    public void mousePressed(LayerEvent e) {
        if (!isEnabled() || _selectiveEnabled
                && !SelectionInteractor.isSelected(e)) {
            return;
        }

        if (getMouseFilter() == null || getMouseFilter().accept(e)) {
            // Set up the target array if it hasn't already been
            if (_targetArray == null) {
                _targetArray = new Object[1];
                _targetArray[0] = e.getFigureSource();
            }

            // Set-up
            setup(e);

            // Constrain and remember the point
            Point2D p = e.getLayerPoint();

            // FIXME: no, don't constrain in mouse-pressed!?
            //constrainPoint(p);
            _prevX = p.getX();
            _prevY = p.getY();

            // Inform listeners
            fireLayerEvent(e);

            // Consume the event
            if (isConsuming()) {
                e.consume();
            }
        }
    }

    /** Handle a mouse released event.
     * Nothing happens if the interactor is not enabled, if if it
     * is "selective enabled" but not in the selection.
     */
    @Override
    public void mouseReleased(LayerEvent e) {
        if (!isEnabled() || _selectiveEnabled
                && !SelectionInteractor.isSelected(e)) {
            return;
        }

        if (getMouseFilter() == null || getMouseFilter().accept(e)) {
            fireLayerEvent(e);
            _targetArray = null;

            // Consume the event
            if (isConsuming()) {
                e.consume();
            }
        }
    }

    /** Prepend a constraint to the list of constraints on
     * this interactor.
     */
    public void prependConstraint(PointConstraint constraint) {
        if (_constraints == null) {
            _constraints = new ArrayList<PointConstraint>();
        }

        _constraints.add(0, constraint);
    }

    /** Remove the given layer listener from this interactor.
     */
    public void removeLayerListener(LayerListener l) {
        _layerListener = LayerEventMulticaster.remove(_layerListener, l);
    }

    /** Set the flag that says that the interactor responds only
     * if the figure being moused on is selected. By default, this
     * flag is false; if set true, then the mouse methods check that
     * the figure is contained in the selection model of that
     * figure's selection interactor (if it has one).
     */
    public boolean setSelectiveEnabled(boolean s) {
        return _selectiveEnabled = s;
    }

    /** Set the target that the interactor operates on.
     * By default, this will be the figure obtained from
     * the event, but this method can be used to set it to
     * something else.
     */
    public void setTargetArray(Object[] arr) {
        _targetArray = arr;
    }

    /** Initialize the interactor before a mouse-pressed event is
     * processed. This default implementation
     * does nothing, but clients can override to cause it to
     * perform some action, such as setting up constraints.
     */
    public void setup(LayerEvent e) {
        // do nothing
    }

    /** Get an iterator over the target figures.
     */
    public Iterator targets() {
        return new ArrayIterator(_targetArray);
    }

    /** Translate the target by the given distance. The first argument
     * is the figure that was moused one. Any overriding methods should
     * be aware that the interactor may in general be operating on
     * multiple figures, and use the targets() method to get them.
     */
    public void translate(LayerEvent e, double x, double y) {
        Iterator<?> i = targets();

        while (i.hasNext()) {
            Figure t = (Figure) i.next();
            t.translate(x, y);
        }
    }
}
