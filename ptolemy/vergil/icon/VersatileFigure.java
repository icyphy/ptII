/*
 * $Id$
 *
 * Copyright (c) 1998-2003 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 *
 */
package ptolemy.vergil.icon;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import diva.canvas.AbstractFigure;
import diva.canvas.interactor.ShapedFigure;
import diva.util.java2d.PaintedObject;
import diva.util.java2d.PaintedPath;
import diva.util.java2d.PaintedShape;
import diva.util.java2d.PaintedString;
import diva.util.java2d.ShapeUtilities;

/** A VersatileFigure is one that contains a single instance of
 *  Shape.  The figure can have a fill with optional compositing (for
 *  translucency), and a stroke with a different fill.  With this
 *  class, simple objects can be created on-the-fly simply by passing
 *  an instance of java.awt.Shape to the constructor.
 *
 *  This figure is versatile because it can happily take a PaintedObject
 *  in addition to specific shapes.  This class takes care of the
 *  complications with all the different types of PaintedObject types,
 *  such as PaintedShape, PaintedString, and PaintedPath.
 *
 * @deprecated Will be moved from Diva to ptolemy/vergil/icon.
 *
 * @version $Revision$
 * @since Ptolemy II 2.0
 * @author  Nick Zamora
 */
public class VersatileFigure extends AbstractFigure
    implements ShapedFigure, Cloneable {

    /** The color compositing operator
     */
    private Composite _composite = AlphaComposite.SrcOver; // opaque

    /** The painted shape that we use to draw the connector.
     */
    private PaintedObject _paintedObject = null;

    /** Create a new figure with the given painted shape.
     */
    public VersatileFigure (PaintedObject paintedObject) {
        super ();
        _paintedObject = paintedObject;
    }
    /** Create a new figure with the given shape. The figure, by
     *  default, has a unit-width continuous black outline and no fill.
     */
    public VersatileFigure (Shape shape) {
        super ();
        _paintedObject = new PaintedShape (shape);
    }

    /** Create a new figure with the given shape and outline width.
     * It has no fill. The default outline paint is black.
     */
    public VersatileFigure (Shape shape, float lineWidth) {
        super ();
        _paintedObject = new PaintedShape (shape, null, lineWidth);
    }

    /** Create a new figure with the given paint pattern. The figure,
     *  by default, has no stroke.
     */
    public VersatileFigure (Shape shape, Paint fill) {
        super ();
        _paintedObject = new PaintedShape (shape, fill);
    }

    /** Create a new figure with the given paint pattern and outline width.
     * The default outline paint is black.
     */
    public VersatileFigure (Shape shape, Paint fill, float lineWidth) {
        super ();
        _paintedObject = new PaintedShape (shape, fill, lineWidth);
    }

    /** Get the bounding box of this figure. This method overrides
     * the inherited method to take account of the thickness of
     * the stroke, if there is one.
     */
    public Rectangle2D getBounds () {
        if (_paintedObject != null) {
            return _paintedObject.getBounds ();
        }
        else {
            return null;
        }
    }

    /** Get the color composition operator of this figure.
     */
    public Composite getComposite () {
        return _composite;
    }

    /** Get the fill paint pattern of this figure if this figure
     *        represents a shape with a fill paint pattern, otherwise
     *        return null.
     */
    public Paint getFillPaint () {
        if (_paintedObject instanceof PaintedShape) {
            return ((PaintedShape) _paintedObject).fillPaint;
        }
        else if (_paintedObject instanceof PaintedString) {
            return ((PaintedString) _paintedObject).getFillPaint ();
        }
        else if (_paintedObject instanceof PaintedPath) {
            return null;
        }
        else {
            return null;
        }
    }

    /** Get the line width of this figure.
     */
    public float getLineWidth () {
        if (_paintedObject instanceof PaintedString) {
            Integer integer = new Integer (((PaintedString) _paintedObject).getSize ());
            return integer.floatValue ();
        }
        else if (_paintedObject instanceof PaintedShape) {
            return ((PaintedShape) _paintedObject).getLineWidth ();
        }
        else if (_paintedObject instanceof PaintedPath) {
            return ((PaintedPath) _paintedObject).getLineWidth ();
        }
        else {
            return -1.0f;
        }
    }

    /** Get the painted object of this figure.
     */
    public PaintedObject getPaintedObject () {
        return _paintedObject;
    }

    /** Get the shape of this figure.  Note that IF this object
     *  represents a PaintedString, this method is the same as
     *  getBounds()
     */
    public Shape getShape () {
        if (_paintedObject instanceof PaintedString) {
            return ((PaintedString) _paintedObject).getShape ();
        }
        else if (_paintedObject instanceof PaintedShape) {
            return ((PaintedShape) _paintedObject).shape;
        }
        else if (_paintedObject instanceof PaintedPath) {
            return ((PaintedPath) _paintedObject).shape;
        }
        else {
            return null;
        }
    }

    /** Get the stroke of this figure if it is a painted shape
     *  or painted path.  Otherwise, return null.
     */
    public Stroke getStroke () {
        if (_paintedObject instanceof PaintedShape) {
            return ((PaintedShape) _paintedObject).getStroke ();
        }
        else if (_paintedObject instanceof PaintedPath) {
            return ((PaintedPath) _paintedObject).getStroke ();
        }
        else if (_paintedObject instanceof PaintedString) {
            return null;
        }
        else {
            return null;
        }
    }

    /** Get the stroke paint pattern of this figure if it is a
     *  painted shape or painted path.  Otherwise, return null.
     */
    public Paint getStrokePaint () {
        if (_paintedObject instanceof PaintedShape) {
            return ((PaintedShape) _paintedObject).strokePaint;
        }
        else if (_paintedObject instanceof PaintedPath) {
            return ((PaintedPath) _paintedObject).strokePaint;
        }
        else if (_paintedObject instanceof PaintedString) {
            return null;
        }
        else {
            return null;
        }
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
        if (!isVisible ()) {
            return false;
        }
        if (_paintedObject instanceof PaintedShape) {
            return ((PaintedShape) _paintedObject).hit (r);
        }
        else if (_paintedObject instanceof PaintedPath) {
            return ((PaintedPath) _paintedObject).hit (r);
        }
        else if (_paintedObject instanceof PaintedString) {
            return false;
        }
        else {
            return false;
        }
    }

    /** Paint the figure. The figure is redrawn with the current
     *  shape, fill, and outline.
     */
    public void paint (Graphics2D g) {
        if (!isVisible ()) {
            return;
        }
        if (_composite != null) {
            g.setComposite (_composite);
        }
        if (_paintedObject != null) {
            _paintedObject.paint (g);
        }
    }

    /** Set the color composition operator of this figure. If the
     * composite is set to null, then the composite will not be
     * changed when the figure is painted. By default, the composite
     * is set to opaque.
     */
    public void setComposite (Composite c) {
        _composite = c;
        repaint ();
    }

    /** Set the fill paint pattern of this figure. The figure will be
     *  filled with this paint pattern. If no pattern is given, do not
     *  fill it.  If this is a painted path, then do nothing.
     */
    public void setFillPaint (Paint p) {
        if (_paintedObject instanceof PaintedShape) {
            ((PaintedShape) _paintedObject).fillPaint = p;
        }
        else if (_paintedObject instanceof PaintedString) {
            ((PaintedString) _paintedObject).setFillPaint (p);
        }
        else if (_paintedObject instanceof PaintedPath) {
        }
        else {
        }
        repaint ();
    }

    /** Set the line width of this figure. If the width is zero,
     * then the stroke will be removed.  Note that if this is a
     * painted string, then the size of the font will be changed
     * to approximately the same size.
     *
     */
    public void setLineWidth (float lineWidth) {
        repaint ();
        if (_paintedObject instanceof PaintedString) {
            ((PaintedString) _paintedObject).setSize (Math.round (lineWidth + 5.0f));
        }
        else if (_paintedObject instanceof PaintedShape) {
            ((PaintedShape) _paintedObject).setLineWidth (lineWidth);
        }
        else if (_paintedObject instanceof PaintedPath) {
            ((PaintedPath) _paintedObject).setLineWidth (lineWidth);
        }
        else {
        }
        repaint ();
    }

    /** Set the shape of this figure.  If this is a
     *  painted string, do nothing.
     */
    public void setShape (Shape s) {
        repaint ();
        if (_paintedObject instanceof PaintedString) {
        }
        else if (_paintedObject instanceof PaintedShape) {
            ((PaintedShape) _paintedObject).shape = s;
        }
        else if (_paintedObject instanceof PaintedPath) {
            ((PaintedPath) _paintedObject).shape = s;
        }
        else {
        }
        repaint ();
    }

    /** Set the stroke of this figure.  If this is a
     *  painted string, do nothing.
     */
    public void setStroke (Stroke s) {
        repaint ();
        if (_paintedObject instanceof PaintedShape) {
            ((PaintedShape) _paintedObject).stroke = s;
        }
        else if (_paintedObject instanceof PaintedPath) {
            ((PaintedPath) _paintedObject).stroke = s;
        }
        else if (_paintedObject instanceof PaintedString) {
        }
        else {
        }
        repaint ();
    }

    /** Set the stroke paint pattern of this figure.  If
     *  this is a painted string, do nothing.
     */
    public void setStrokePaint (Paint p) {
        repaint ();
        if (_paintedObject instanceof PaintedShape) {
            ((PaintedShape) _paintedObject).strokePaint = p;
        }
        else if (_paintedObject instanceof PaintedPath) {
            ((PaintedPath) _paintedObject).strokePaint = p;
        }
        else if (_paintedObject instanceof PaintedString) {
        }
        else {
        }
        repaint ();
    }

    /** Transform the figure with the supplied transform. This can be
     * used to perform arbitrary translation, scaling, shearing, and
     * rotation operations. As much as possible, this method attempts
     * to preserve the type of the shape: if the shape of this figure
     * is an of RectangularShape or Polyline, then the shape may be
     * modified directly. Otherwise, a general transformation is used
     * that loses the type of the shape, converting it into a
     * GeneralPath.
     *
     * If this is a painted string, do nothing.
     */
    public void transform (AffineTransform at) {
        repaint ();
        if (_paintedObject instanceof PaintedString) {
        }
        else if (_paintedObject instanceof PaintedShape) {
            ((PaintedShape) _paintedObject).shape
                = ShapeUtilities.transformModify
                (((PaintedShape) _paintedObject).shape, at);
        }
        else if (_paintedObject instanceof PaintedPath) {
            ((PaintedPath) _paintedObject).shape
                = ShapeUtilities.transformModify
                (((PaintedPath) _paintedObject).shape, at);
        }
        else {
        }
        repaint ();
    }

    /**
     * Translate the figure with by the given distance.
     * As much as possible, this method attempts
     * to preserve the type of the shape: if the shape of this figure
     * is an of RectangularShape or Polyline, then the shape may be
     * modified directly. Otherwise, a general transformation is used
     * that loses the type of the shape, converting it into a
     * GeneralPath.
     *
     * If this is a painted string, do nothing.
     */
    public void translate (double x, double y) {
        repaint ();
        if (_paintedObject instanceof PaintedString) {
        }
        else if (_paintedObject instanceof PaintedShape) {
            ((PaintedShape) _paintedObject).shape
                = ShapeUtilities.translateModify
                (((PaintedShape) _paintedObject).shape, x, y);
        }
        else if (_paintedObject instanceof PaintedPath) {
            ((PaintedPath) _paintedObject).shape
                = ShapeUtilities.translateModify
                (((PaintedPath) _paintedObject).shape, x, y);
        }
        else {
        }
        repaint ();
    }

    /** Clone this Figure.
     */
    public Object clone() {
        Shape cloneShape = ShapeUtilities.cloneShape(getShape());
        PaintedObject object =
            new PaintedShape(cloneShape, getFillPaint(),
                    getLineWidth(), getStrokePaint());
        VersatileFigure clone = new VersatileFigure(object);
        clone.setStroke(getStroke());
        clone.setComposite(getComposite());
        clone.setInteractor(getInteractor());
        return clone;
    }

}





