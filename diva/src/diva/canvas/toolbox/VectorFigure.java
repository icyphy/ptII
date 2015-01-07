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
package diva.canvas.toolbox;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;

import diva.canvas.AbstractFigure;
import diva.canvas.Figure;
import diva.canvas.TransformContext;
import diva.util.java2d.ShapeUtilities;

/** A VectorFigure is a figure containing a list of objects that
 * are drawn to produce the figure.
 *
 * The list of objects that are drawn
 * includes both geometric objects and drawing control objects.
 * Specifically:
 * <ul>
 * <li> Shape: any Shape is drawn as determine by the current fill and line
 *      styles.
 * <li> Stroke, Paint, Composite: any subsequent shapes are drawn using
 *      this object set appropriately in the graphics object
 * <li> Figure: Any other Figure can be drawn as well, thus allowing
 *      hierarchical drawing. Note, however, that figures drawn inside
 *      are NOT able to detect events. After the figure is drawn, the
 *      style, stroke, etc, will revert to the default
 * </ul>
 *
 * Strings are not currently supported, but will be soon. Transforms
 * are not supported, but may be at some time in the future.
 *
 * The figure is switched between line drawing mode and filling mode
 * with the calls lineMode() and fillMode(). The default mode
 * is line drawing with a 1-pixel black stroke.
 *
 * @version        $Id$
 * @author         John Reekie
 */
public class VectorFigure extends AbstractFigure {
    static final int FILLMODE = 87;

    static final int LINEMODE = 143;

    /** The list containing the objects that we paint
     */
    private ArrayList _objects = new ArrayList();

    /** The transform for the internals
     */
    private TransformContext _transformContext = new TransformContext(this);

    /** The shape of the figure, without transforming.
     */
    private Shape _shape;

    /** The shape of the figure, transformed.
     */
    private Shape _cachedShape;

    /** The bounds of the figure, without transforming.
     */
    private Rectangle2D _bounds;

    /** The bounds of the figure, transformed.
     */
    private Rectangle2D _cachedBounds;

    /** Create a new blank figure.
     */
    public VectorFigure() {
    }

    /** Add a new painted shape to the list of drawn objects.
     */
    public void add(Shape s) {
        _objects.add(s);
        _bounds = null;
        _cachedBounds = null;
    }

    /** Add a new figure to the list of drawn objects
     */
    public void add(Figure f) {
        _objects.add(f);
        _bounds = null;
        _cachedBounds = null;
    }

    /** Add a new compositioning operator to the list of drawn objects.
     */
    public void add(Composite c) {
        _objects.add(c);
    }

    /** Add a new paint to the list of drawn objects.
     */
    public void add(Paint p) {
        _objects.add(p);
    }

    /** Add a new stroke to the list of drawn objects.
     */
    public void add(Stroke s) {
        _objects.add(s);
    }

    /** Add an object to the list that puts drawing into fill mode
     */
    public void fillMode() {
        _objects.add(new CtrlObj(FILLMODE));
    }

    /** Get the bounding box of this figure. If a bounding box has not yet
     * been set (by calling the constructor that takes a Figure, or by
     * calling _setBounds()), then a new bounding box will be computed by
     * traversing the list of objects.
     */
    @Override
    public Rectangle2D getBounds() {
        if (_bounds == null) {
            Iterator i = _objects.iterator();
            Rectangle2D bounds = null;
            boolean fillMode = false;
            Stroke stroke = new BasicStroke(1.0f);

            // Scan the object list take the union of shapes and figures
            while (i.hasNext()) {
                Object obj = i.next();
                Rectangle2D b = null;

                if (obj instanceof CtrlObj) {
                    switch (((CtrlObj) obj).code) {
                    case FILLMODE:
                        fillMode = true;
                        break;

                    case LINEMODE:
                        fillMode = false;
                        break;
                    }
                } else if (obj instanceof Shape) {
                    if (fillMode) {
                        b = ((Shape) obj).getBounds2D();
                    } else {
                        b = ShapeUtilities.computeStrokedBounds((Shape) obj,
                                stroke);
                    }
                } else if (obj instanceof Figure) {
                    b = ((Figure) obj).getBounds();
                } else if (obj instanceof Stroke) {
                    stroke = (Stroke) obj;
                }

                if (b != null) {
                    if (bounds == null) {
                        bounds = b;
                    } else {
                        Rectangle2D.union(bounds, b, bounds);
                    }
                }
            }

            if (bounds != null) {
                _bounds = bounds;
            }
        }

        if (_cachedBounds == null) {
            AffineTransform at = _transformContext.getTransform();
            _cachedBounds = ShapeUtilities.transformBounds(_bounds, at);
        }

        return _cachedBounds;
    }

    /** Get the shape of this figure.  If a shape has not yet been set
     *  by calling setShape(), then the shape will be set to the bounding box.
     */
    @Override
    public Shape getShape() {
        if (_shape == null) {
            return getBounds();
        }

        if (_cachedShape == null) {
            AffineTransform at = _transformContext.getTransform();
            _cachedShape = at.createTransformedShape(_shape);
        }

        return _cachedShape;
    }

    /** Add an object to the list that puts drawing into line mode
     */
    public void lineMode() {
        _objects.add(new CtrlObj(LINEMODE));
    }

    /** Paint the figure.
     */
    @Override
    public void paint(Graphics2D g) {
        if (!isVisible()) {
            return;
        }

        // Push the context
        _transformContext.push(g);

        boolean fillMode = false;
        Iterator i = _objects.iterator();

        while (i.hasNext()) {
            Object obj = i.next();

            if (obj instanceof CtrlObj) {
                switch (((CtrlObj) obj).code) {
                case FILLMODE:
                    fillMode = true;
                    break;

                case LINEMODE:
                    fillMode = false;
                    break;
                }
            } else if (obj instanceof Shape) {
                if (fillMode) {
                    g.fill((Shape) obj);
                } else {
                    g.draw((Shape) obj);
                }
            } else if (obj instanceof Figure) {
                ((Figure) obj).paint(g);
            } else if (obj instanceof Color) {
                g.setPaint((Paint) obj);
            } else if (obj instanceof Stroke) {
                g.setStroke((Stroke) obj);
            } else if (obj instanceof Composite) {
                g.setComposite((Composite) obj);
            }
        }

        // Pop the context
        _transformContext.pop(g);
    }

    /** Set the shape of this Figure. This is useful when the default
     * of the bounding box is not the right thing -- such as when
     * a circular shape is required, for instance.
     */
    public void setShape(Shape s) {
        _shape = s;
        _cachedShape = null;
    }

    /** Transform the figure with the supplied transform. This can be
     * used to perform arbitrary translation, scaling, shearing, and
     * rotation operations.
     */
    @Override
    public void transform(AffineTransform at) {
        repaint();
        _cachedBounds = null;
        _cachedShape = null;
        _transformContext.preConcatenate(at);
        repaint();
    }

    private static class CtrlObj {
        // FindBugs suggests making this class static so as to decrease
        // the size of instances and avoid dangling references.

        private int code = 0;

        private CtrlObj(int i) {
            code = i;
        }
    }
}
