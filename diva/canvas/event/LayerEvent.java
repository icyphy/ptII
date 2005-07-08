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
 *
 */
package diva.canvas.event;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import diva.canvas.CanvasLayer;
import diva.canvas.Figure;

/** The class representing mouse events in layers. This class extends
 * the AWT MouseEvent class, but adds the notion of floating-point
 * coordinates and a "layer source" and "figure source" as well as
 * a component source.
 * Any code that processes a LayerEvent should be sure to
 * use the methods <b>getLayerX</b>, <b>getLayerY</b>,
 * <b>getLayerPoint</b>, instead of the inherited methods <b>getX</b>,
 * <b>getY</b>, <b>getPoint</b>. (Originally, this class was designed
 * to inherit from the AWT InputEvent class, which would have avoided
 * the awkwardness of having two sets of coordinates in the same event
 * object. This wasn't possible because InputEvent has only one
 * package-scope constructor, and hence cannot be subclassed outside
 * the AWT package.)
 *
 * @version        $Id$
 * @author John Reekie
 */
public class LayerEvent extends MouseEvent {
    // Note: This class now uses the improved JDK1.4 mechanism for
    // processing mouse events using getModifiersEx() which correspond
    // correctly to alt, etc. Unfortunately, this means that is now
    // incompatible with jdk1.3. Oh well.

    /** The layer source
     * @serial
     */
    private CanvasLayer _layerSource = null;

    /** The figure source
     * @serial
     */
    private Figure _figureSource = null;

    /** The x coordinate.
     * @serial
     */
    private double _layerX;

    /** The y coordinate.
     * @serial
     */
    private double _layerY;

    /** The event that this event was constructed from.
     * @serial
     */
    private MouseEvent _backingEvent;

    /** Create a new layer event from the given mouse event.  The
     * layer and figure event sources will be set to null, and the
     * layer coordinates will be the same as the canvas coordinates.
     */
    public LayerEvent(MouseEvent e) {
        super(e.getComponent(), e.getID(), e.getWhen(), e.getModifiersEx(), e
                .getX(), e.getY(), e.getClickCount(), e.isPopupTrigger(), e
                .getButton());

        _backingEvent = e;
        _layerX = e.getX();
        _layerY = e.getY();
    }

    /** Create a new layer event from the given mouse event, but give
     * it a different ID.  The layer and figure event sources will be
     * set to null, and the layer coordinates will be the same as the
     * canvas coordinates.
     */
    public LayerEvent(MouseEvent e, int id) {
        super(e.getComponent(), id, e.getWhen(), e.getModifiersEx(), e.getX(),
                e.getY(), e.getClickCount(), e.isPopupTrigger(), e.getButton());

        _backingEvent = e;
        _layerX = e.getX();
        _layerY = e.getY();
    }

    /** Consume this event. If the event was constructed from
     * a backing mouse event, consume the backing mouse event too.
     */
    public void consume() {
        super.consume();

        if (_backingEvent != null) {
            _backingEvent.consume();
        }
    }

    /** Get the layer that the event occurred on.
     */
    public CanvasLayer getLayerSource() {
        return _layerSource;
    }

    /** Get the figure that the event occurred on.
     * This is null if the event did not occur on a figure.
     */
    public Figure getFigureSource() {
        return _figureSource;
    }

    /** Get the point where the event occurred. The point is in layer
     * coordinates, and should be assumed to be a floating-point
     * coordinate. This point is to be a copy of any internal data,
     * so the caller can modify it.
     */
    public Point2D getLayerPoint() {
        return new Point2D.Double(_layerX, _layerY);
    }

    /** Get the horizontal component of the point where the event
     * occurred. The value is in layer coordinates, and should be
     * assumed to be a floating-point coordinate.
     */
    public double getLayerX() {
        return _layerX;
    }

    /** Get the vertical component of the point where the event
     * occurred. The value is in layer coordinates, and should be
     * assumed to be a floating-point coordinate.
     */
    public double getLayerY() {
        return _layerY;
    }

    /** Set the figure that the event occurred on.
     */
    public void setFigureSource(Figure figureSource) {
        this._figureSource = figureSource;
    }

    /** Set the layer that the event occurred on.
     */
    public void setLayerSource(CanvasLayer layerSource) {
        this._layerSource = layerSource;
    }

    /** Set the layer position of the event
     */
    public void setLayerPoint(Point2D point) {
        _layerX = point.getX();
        _layerY = point.getY();
    }

    /** Set the layer X position of the event
     */
    public void setLayerX(double x) {
        _layerX = x;
    }

    /** Set the layer Y position of the event
     */
    public void setLayerY(double y) {
        _layerY = y;
    }

    /** Print the string representation of this event
     */
    public String toString() {
        StringBuffer result = new StringBuffer(this.getClass().getName());
        result.append("[" + idToString(getID()) + ",mods="
                + getModifiersExText(getModifiersEx()) + ",clickcount="
                + getClickCount() + ",figure=" + getFigureSource() + ",layer=("
                + _layerX + "," + _layerY + ")" + ",consumed=" + isConsumed()
                + "]");
        return result.toString() + _backingEvent;
    }

    /** Print the string representation of modifier flags
     */
    public static String toString(int flags) {
        StringBuffer result = new StringBuffer();
        int i = 256;
        boolean sep = false;

        while (i > 0) {
            String s = flagToString(i & flags);

            if (s != null) {
                if (sep) {
                    result.append("|");
                }

                result.append(s);
                sep = true;
            }

            i = i / 2;
        }

        return result.toString();
    }

    /** Print the string representation of a single flag
     */
    private static String flagToString(int flag) {
        switch (flag) {
        case InputEvent.BUTTON1_MASK:
            return "BUTTON1_MASK";

        case InputEvent.BUTTON2_MASK:
            return "BUTTON2_MASK";

        case InputEvent.BUTTON3_MASK:
            return "BUTTON3_MASK";

        case InputEvent.CTRL_MASK:
            return "CTRL_MASK";

        case InputEvent.SHIFT_MASK:
            return "SHIFT_MASK";

        //// AWT is too stupid to handle these properly
        //case InputEvent.ALT_MASK:
        //return "ALT_MASK";
        //case InputEvent.META_MASK:
        //return "META_MASK";
        }

        return null;
    }

    /** Print the string representation of an event ID
     */
    public static String idToString(int id) {
        switch (id) {
        case MouseEvent.MOUSE_PRESSED:
            return "MOUSE_PRESSED";

        case MouseEvent.MOUSE_DRAGGED:
            return "MOUSE_DRAGGED";

        case MouseEvent.MOUSE_RELEASED:
            return "MOUSE_RELEASED";

        case MouseEvent.MOUSE_CLICKED:
            return "MOUSE_CLICKED";

        case MouseEvent.MOUSE_ENTERED:
            return "MOUSE_ENTERED";

        case MouseEvent.MOUSE_MOVED:
            return "MOUSE_MOVED";

        case MouseEvent.MOUSE_EXITED:
            return "MOUSE_EXITED";
        }

        return null;
    }

    /** Transform the layer coordinates of the event with the given
     * transform.
     */
    public void transform(AffineTransform at) {
        if (at.isIdentity()) {
            return;
        }

        Point2D p = new Point2D.Double(_layerX, _layerY);
        at.transform(p, p);
        _layerX = p.getX();
        _layerY = p.getY();
    }
}
