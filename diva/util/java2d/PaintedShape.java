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
package diva.util.java2d;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

/** A utility class that paints a shape with an stroke and/or fill.
 * This class is intended for use as a low-level class to simplify
 * construction of drawn graphics.  It contains a number of fields that
 * govern how the shape is painted, such as the fill paint, stroke
 * stroke or line width, and stroke paint.
 *
 * @version        $Id$
 * @author         John Reekie
 * @author      Nick Zamora
 */
public class PaintedShape extends AbstractPaintedGraphic {
    // Note that this class was deprecated becase we were to use
    // diva.compat.canvas instead.  However, the Ptolemy sources
    // do not include diva.compat.canvas, so I'm making this class
    // undeprecated. -cxh 7/05

    /** The paint for the fill.
     */
    public Paint fillPaint;

    /** Create a painted shape on the given Shape. The shape
     * will be painted as a black outline only.
     */
    public PaintedShape(Shape shape) {
        this(shape, null, 1.0f, Color.black);
    }

    /** Create a painted shape on the given Shape. The shape
     * will be filled with the given paint, but not painted with
     * an outline.
     */
    public PaintedShape(Shape shape, Paint fillPaint) {
        this(shape, fillPaint, 1.0f, null);
    }

    /** Create a painted shape on the given Shape.  The shape
     * will be outlined in black with the given width but not filled.
     */
    public PaintedShape(Shape shape, float lineWidth) {
        this(shape, null, lineWidth, Color.black);
    }

    /** Create a painted shape on the given Shape. The shape
     * will be filled with the given paint and outlined with
     * a black outline of the given width.
     */
    public PaintedShape(Shape shape, Paint fillPaint, float lineWidth) {
        this(shape, fillPaint, lineWidth, Color.black);
    }

    /** Create a painted shape on the given Shape. The shape
     * will be outlined with an outline of the given color and
     * given width and not filled.
     */
    public PaintedShape(Shape shape, float lineWidth, Paint strokePaint) {
        this(shape, null, lineWidth, strokePaint);
    }

    /** Create a painted shape on the given Shape. The shape
     * will be filled with the given paint and outlined with
     * an outline of the given color and given width.
     */
    public PaintedShape(Shape shape, Paint fillPaint, float lineWidth,
            Paint strokePaint) {
        this.shape = shape;
        setFillPaint(fillPaint);
        setStrokePaint(strokePaint);
        setLineWidth(lineWidth);
    }

    /** Get the line width
     */
    @Override
    public float getLineWidth() {
        if (stroke == null) {
            return 0.0f;
        } else {
            return ((BasicStroke) stroke).getLineWidth();
        }
    }

    /** Test if this shape is hit by the given rectangle. Currently
     * this does not take into account the width of the stroke
     * or other things such as dashes, because of problems with
     * geometry testing with GeneralPath in the first version of
     * JDK1.2.
     */
    @Override
    public boolean hit(Rectangle2D r) {
        // Hit testing on strokes doesn't appear to work too
        // well in JDK1.2, so we will cheat and ignore the width
        // of the stroke
        if (fillPaint != null) {
            // There's a fill, so test the internal of the path
            return shape.intersects(r);
        } else {
            // Otherwise test for intersection with the path
            return ShapeUtilities.intersectsOutline(r, shape);
        }
    }

    /** Test if this shape intersects the given rectangle. Currently
     * this does not take into account the width of the stroke
     * or other things such as dashes, because of problems with
     * geometry testing with GeneralPath in the first version of
     * JDK1.2.
     */
    @Override
    public boolean intersects(Rectangle2D r) {
        return shape.intersects(r);
    }

    /** Paint the shape. The shape is redrawn with the current
     *  shape, fill, and stroke.
     */
    @Override
    public void paint(Graphics2D g) {
        // Fill it
        if (fillPaint != null) {
            g.setPaint(fillPaint);
            g.fill(shape);
        }

        // Stroke it
        if (stroke != null && strokePaint != null) {
            g.setStroke(stroke);
            g.setPaint(strokePaint);
            g.draw(shape);
        }
    }

    /** Set the line width. If the line width is less than
     * or equal to zero, the stroke will be removed.
     */
    @Override
    public void setLineWidth(float lineWidth) {
        if (lineWidth <= 0.0) {
            stroke = null;
        } else {
            if (stroke == null
                    || lineWidth != ((BasicStroke) stroke).getLineWidth()) {
                stroke = AbstractPaintedGraphic.getStroke(lineWidth);
            }
        }
    }

    /**
     * Set the stroke paint for this shape (its outline if it is closed).
     */
    public void setStrokePaint(Paint strokePaint) {
        this.strokePaint = strokePaint;
    }

    /**
     * Set the fill paint for this shape (its fill if it is closed).
     */
    public void setFillPaint(Paint fillPaint) {
        this.fillPaint = fillPaint;
    }
}
