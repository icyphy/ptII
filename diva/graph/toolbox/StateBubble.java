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
package diva.graph.toolbox;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import diva.canvas.AbstractFigure;
import diva.util.java2d.PaintedShape;
import diva.util.java2d.ShapeUtilities;

/** A Figure that is customized for representing state bubbles.
 *
 * @version        $Id$
 * @author         John Reekie
 * @author         Xiaojun Liu
 */
public class StateBubble extends AbstractFigure {
    /** The painted shape that we use to draw the outside ellipse.
     */
    private PaintedShape _outsideEllipse;

    /** The painted shape that we use to draw the inside ellipse.
     */
    private PaintedShape _insideEllipse;

    /** The distance between the inside and outside ellipses
     */
    private double _spacing = 10.0;

    /** The type of the state
     */
    private int _stateType = NORMAL_STATE;

    /** The style of a regular state
     */
    public static final int NORMAL_STATE = 35;

    /** The style of an initial state
     */
    public static final int INITIAL_STATE = NORMAL_STATE + 1;

    /** The style of a finalstate
     */
    public static final int FINAL_STATE = INITIAL_STATE + 1;

    /** Create a new figure at the given coordinates. The figure, by
     *  default, has a unit-width continuous black outline and no fill.
     */
    public StateBubble(double x, double y, double width, double height) {
        Shape s = new Ellipse2D.Double(x, y, width, height);
        _outsideEllipse = new PaintedShape(s);
    }

    /** Create a new figure at the given coordinates and with the
     * given fill.
     */
    public StateBubble(double x, double y, double width, double height,
            Paint fill) {
        Shape s = new Ellipse2D.Double(x, y, width, height);
        _outsideEllipse = new PaintedShape(s, fill);
    }

    /** Get the bounding box of this figure. This method overrides
     * the inherited method to take account of the thickness of
     * the stroke, if there is one.
     */
    @Override
    public Rectangle2D getBounds() {
        return _outsideEllipse.getBounds();
    }

    /** Get the shape of this figure.
     */
    @Override
    public Shape getShape() {
        return _outsideEllipse.shape;
    }

    /** Get the state type. This will be one of NORMAL_STATE,
     * INITIAL_STATE, or FINAL_STATE.
     */
    public int getStateType() {
        return _stateType;
    }

    /** Get the stroke of this figure.
     */
    public Stroke getStroke() {
        return _outsideEllipse.getStroke();
    }

    /** Get the stroke paint pattern of this figure.
     */
    public Paint getStrokePaint() {
        return _outsideEllipse.strokePaint;
    }

    /** Test if this figure intersects the given rectangle. If there
     * is a fill but no outline, then there is a hit if the shape
     * is intersected. If there is an outline but no fill, then the
     * area covered by the outline stroke is tested. If there
     * is both a fill and a stroke, the region bounded by the outside
     * edge of the stroke is tested. If there is neither a fill nor
     * a stroke, then return false. If the figure is not visible,
     * always return false.
     */
    @Override
    public boolean hit(Rectangle2D r) {
        if (!isVisible()) {
            return false;
        }

        return _outsideEllipse.hit(r);
    }

    /** Paint the figure. The figure is redrawn with the current
     *  shape, fill, and outline.
     */
    @Override
    public void paint(Graphics2D g) {
        if (!isVisible()) {
            return;
        }

        _outsideEllipse.paint(g);

        if (_insideEllipse != null) {
            _insideEllipse.paint(g);
        }
    }

    /** Set the fill paint pattern of this figure. The figure will be
     *  filled with this paint pattern. If no pattern is given, do not
     *  fill it.
     */
    public void setFillPaint(Paint p) {
        _outsideEllipse.fillPaint = p;

        if (_insideEllipse != null) {
            _insideEllipse.fillPaint = p;
        }

        repaint();
    }

    /** Set the stroke of this figure.
     */
    public void setStroke(BasicStroke s) {
        repaint();
        _outsideEllipse.stroke = s;

        if (_insideEllipse != null) {
            _insideEllipse.stroke = s;
        }

        repaint();
    }

    /** Set the type of the state
     */
    public void setStateType(int type) {
        repaint();
        _stateType = type;

        switch (type) {
        case NORMAL_STATE:

            if (_insideEllipse != null) {
                _insideEllipse = null;
            }

            _outsideEllipse.setLineWidth(1);
            break;

        case INITIAL_STATE:

            Ellipse2D bounds = (Ellipse2D) _outsideEllipse.shape;
            Shape s = new Ellipse2D.Double(bounds.getX() + _spacing,
                    bounds.getY() + _spacing, bounds.getWidth() - 2 * _spacing,
                    bounds.getHeight() - 2 * _spacing);

            _insideEllipse = new PaintedShape(s);
            _outsideEllipse.setLineWidth(1);
            break;

        case FINAL_STATE:

            if (_insideEllipse != null) {
                _insideEllipse = null;
            }

            _outsideEllipse.setLineWidth((float) _spacing);
            break;
        }

        repaint();
    }

    /** Set the stroke paint pattern of this figure.
     */
    public void setStrokePaint(Paint p) {
        _outsideEllipse.strokePaint = p;

        if (_insideEllipse != null) {
            _insideEllipse.strokePaint = p;
        }

        repaint();
    }

    /** Transform the figure with the supplied transform. This can be
     * used to perform arbitrary translation, scaling, shearing, and
     * rotation operations. As much as possible, this method attempts
     * to preserve the type of the shape: if the shape of this figure
     * is an of RectangularShape or Polyline, then the shape may be
     * modified directly. Otherwise, a general transformation is used
     * that loses the type of the shape, converting it into a
     * GeneralPath.
     */
    @Override
    public void transform(AffineTransform at) {
        repaint();
        _outsideEllipse.shape = ShapeUtilities.transformModify(
                _outsideEllipse.shape, at);

        if (_insideEllipse != null) {
            _insideEllipse.shape = ShapeUtilities.transformModify(
                    _insideEllipse.shape, at);
        }

        repaint();
    }

    /** Translate the figure with by the given distance.
     * As much as possible, this method attempts
     * to preserve the type of the shape: if the shape of this figure
     * is an of RectangularShape or Polyline, then the shape may be
     * modified directly. Otherwise, a general transformation is used
     * that loses the type of the shape, converting it into a
     * GeneralPath.
     */
    @Override
    public void translate(double x, double y) {
        repaint();
        _outsideEllipse.shape = ShapeUtilities.translateModify(
                _outsideEllipse.shape, x, y);

        if (_insideEllipse != null) {
            _insideEllipse.shape = ShapeUtilities.translateModify(
                    _insideEllipse.shape, x, y);
        }

        repaint();
    }
}
