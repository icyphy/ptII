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

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import diva.canvas.AbstractFigure;
import diva.canvas.CanvasUtilities;
import diva.canvas.interactor.ShapedFigure;
import diva.util.java2d.ShapeUtilities;

/** A BasicFigure is one that contains a single instance of Shape. The
 *  figure can have a fill with optional compositing (for
 *  translucency), and a stroke with a different fill. With this
 *  class, simple objects can be created on-the-fly simply by passing
 *  an instance of java.awt.Shape to the constructor. This class is
 *  mainly intended for use for simple open and closed shapes. For
 *  more complex Figures, use the VectorFigure class.
 *
 * @version        $Id$
 * @author         John Reekie
 * @author      Nick Zamora
 */
public class BasicFigure extends AbstractFigure implements ShapedFigure {

    /** Indicator of whether this figure should be centered on its origin.
     *  By default, this class is centered, like the superclass.
     */
    private boolean _centered = true;

    /** The color compositing operator.
     */
    private Composite _composite = AlphaComposite.SrcOver; // opaque

    /** The shape of this figure.
     */
    private Shape _shape;

    /** The paint for the fill.
     */
    private Paint _fillPaint;

    /** The stroke.
     */
    private Stroke _stroke;

    /** The stroke paint.
     */
    private Paint _strokePaint;

    /** The transform.
     */
    private AffineTransform _transform = new AffineTransform();

    /** Create a new figure with the given shape. The figure, by
     *  default, has a unit-width continuous black outline and no
     *  fill.  The given shape will be cloned to prevent the original
     *  from being modified.
     */
    public BasicFigure (Shape shape) {
        this(shape, null, 1.0f);
    }

    /** Create a new figure with the given shape and outline width.
     * It has no fill. The default outline paint is black.  The given
     * shape will be cloned to prevent the original from being
     * modified.
     *
     * @deprecated  Use the float constructor instead.
     */
    public BasicFigure (Shape shape, int lineWidth) {
        this(shape, null, (float)lineWidth);
    }

    /** Create a new figure with the given shape and outline width.
     * It has no fill. The default outline paint is black.  The given
     * shape will be cloned to prevent the original from being
     * modified.
     */
    public BasicFigure (Shape shape, float lineWidth) {
        this(shape, null, lineWidth);
    }

    /** Create a new figure with the given paint pattern. The figure,
     *  by default, has no stroke.  The given shape will be cloned to
     *  prevent the original from being modified.
     */
    public BasicFigure (Shape shape, Paint fill) {
        this(shape, fill, 1.0f);
    }

    /** Create a new figure with the given paint pattern and line
     *  width.  The given shape will be cloned to prevent the original
     *  from being modified.
     */
    public BasicFigure (Shape shape, Paint fill, float lineWidth) {
        _shape = ShapeUtilities.cloneShape(shape);
        _fillPaint = fill;
        _stroke = ShapeUtilities.getStroke(lineWidth);
        _strokePaint = Color.black;
    }

    /** Get the bounding box of this figure. This method overrides
     * the inherited method to take account of the thickness of
     * the stroke, if there is one.
     */
    public Rectangle2D getBounds () {
        if (_stroke == null) {
            return _shape.getBounds2D();
        } else {
            return ShapeUtilities.computeStrokedBounds(_shape, _stroke);
        }
    }

    /**
     * Get the compositing operator
     */
    public Composite getComposite() {
        return _composite;
    }

    /** Get the dash array. If the stroke is not a BasicStroke
     * then null will always be returned.
     */
    public float[] getDashArray () {
        if (_stroke instanceof BasicStroke) {
            return ((BasicStroke) _stroke).getDashArray();
        } else {
            return null;
        }
    }

    /**
     * Get the fill paint
     */
    public Paint getFillPaint() {
        return _fillPaint;
    }

    /** Get the line width. If the stroke is not a BasicStroke
     * then 1.0 will always be returned.
     */
    public float getLineWidth () {
        if (_stroke instanceof BasicStroke) {
            return ((BasicStroke) _stroke).getLineWidth();
        } else {
            return 1.0f;
        }
    }

    /** Return the origin of the figure in the enclosing transform
     *  context.  This overrides the base class to return the center
     *  of the shape, if the figure is centered, or the origin of the
     *  shape if the figure is not centered.
     *  @return The origin of the figure.
     */
    public Point2D getOrigin () {
        Rectangle2D bounds = getBounds();
        if (_centered) {
            return super.getOrigin();
        } else {
            Point2D point = new Point2D.Double(0,0);
            _transform.transform(point, point);
            return point;
        }
    }

    /** Get the shape of this figure.
     */
    public Shape getShape () {
        return _shape;
    }

    /** Get the paint used to stroke this figure
     */
    public Paint getStrokePaint () {
        return _strokePaint;
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
    public boolean hit (Rectangle2D r) {
        if (!isVisible()) {
            return false;
        }
        boolean hit = false;
        if (_fillPaint != null) {
            hit = _shape.intersects(r);
        }
        if (!hit && _stroke != null && _strokePaint != null) {
            hit = hit || ShapeUtilities.intersectsOutline(r, _shape);
        }
        return hit;
    }

    /** Return whether the figure should be centered on its origin.
     *  @return False If the origin of the figure, as
     *   returned by getOrigin(), is the upper left corner.
     *  @see #getOrigin()
     *  @see #setCentered(boolean)
     */
    public boolean isCentered() {
        return _centered;
    }

    /** Paint the figure. The figure is redrawn with the current
     *  shape, fill, and outline.
     */
    public void paint (Graphics2D g) {
        if (!isVisible()) {
            return;
        }
        if (_fillPaint != null) {
            g.setPaint(_fillPaint);
            g.setComposite(_composite);
            g.fill(_shape);
        }
        if (_stroke != null && _strokePaint != null) {
            g.setStroke(_stroke);
            g.setPaint(_strokePaint);
            g.draw(_shape);
        }
    }

    /** Specify whether the figure should be centered on its origin.
     *  By default, it is.
     *  @param centered False to make the origin of the figure, as
     *   returned by getOrigin(), be the upper left corner.
     *  @see #getOrigin()
     */
    public void setCentered(boolean centered) {
        repaint();
        Point2D point = getOrigin();
        _centered = centered;
        CanvasUtilities.translateTo(this, point.getX(), point.getY());
    }

    /** Set the compositing operation for this figure.
     */
    public void setComposite (AlphaComposite c) {
        _composite = c;
        repaint();
    }

    /** Set the dash array of the stroke. The existing stroke will
     * be removed, but the line width will be preserved if possible.
     */
    public void setDashArray (float dashArray[]) {
        repaint();
        if (_stroke instanceof BasicStroke) {
            _stroke = new BasicStroke(
                    ((BasicStroke) _stroke).getLineWidth(),
                    ((BasicStroke) _stroke).getEndCap(),
                    ((BasicStroke) _stroke).getLineJoin(),
                    ((BasicStroke) _stroke).getMiterLimit(),
                    dashArray,
                    0.0f);
        } else {
            _stroke = new BasicStroke(
                    1.0f,
                    BasicStroke.CAP_SQUARE,
                    BasicStroke.JOIN_MITER,
                    10.0f,
                    dashArray,
                    0.0f);
        }
        repaint();
    }

    /**
     * Set the fill paint. If p is null then
     * the figure will not be filled.
     */
    public void setFillPaint(Paint p) {
        repaint();
        _fillPaint = p;
        repaint();
    }

    /** Set the line width. The existing stroke will
     * be removed, but the dash array will be preserved if possible.
     */
    public void setLineWidth (float lineWidth) {
        repaint();
        if (_stroke instanceof BasicStroke) {
            _stroke = new BasicStroke(
                    lineWidth,
                    ((BasicStroke) _stroke).getEndCap(),
                    ((BasicStroke) _stroke).getLineJoin(),
                    ((BasicStroke) _stroke).getMiterLimit(),
                    ((BasicStroke) _stroke).getDashArray(),
                    0.0f);
        } else {
            new BasicStroke(
                    lineWidth,
                    BasicStroke.CAP_SQUARE,
                    BasicStroke.JOIN_MITER,
                    10.0f,
                    null,
                    0.0f);
        }
        repaint();
    }

    /** Change the shape of the figure without modifying its other
     *  properties, such as its position.  If you are writing client
     *  code that is modifying the appearance of the Figure after it
     *  is created, this is most likely the method you want to call,
     *  as opposed to setShape().
     */
    public void setPrototypeShape (Shape s) {
        repaint();
        _shape = _transform.createTransformedShape(s);
        repaint();
    }

    /** Set the shape of this figure.  Note that this method is
     * primarily intended to be used by interactors that modify the
     * figures shape (such as PathManipulator).  In particular, the
     * position of the figure is likely to move.  In order to change
     * the shape of the figure without moving the figure, use the
     * setPrototypeShape() method instead.
     */
    public void setShape (Shape s) {
        repaint();
        _shape = s;
        repaint();
    }

    /**
     * Set the stroke paint
     */
    public void setStrokePaint(Paint p) {
        _strokePaint = p;
        repaint();
    }

    /**
     * Set the stroke
     */
    public void setStroke(Stroke s) {
        repaint();
        _stroke = s;
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
    public void transform (AffineTransform at) {
        repaint();
        _transform.preConcatenate(at);
        _shape = ShapeUtilities.transformModify(_shape, at);
        repaint();
    }
}
