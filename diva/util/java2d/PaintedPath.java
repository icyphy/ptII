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
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;

/** A utility class that strokes a shape.
 * This class is intended for use as a low-level class to simplify
 * construction of drawn graphics.  It contains a number of fields that
 * govern how the shape is stroked, such as the line width, dashing,
 * and paint.
 *
 * @version        $Id$
 * @author         John Reekie
 * @author      Nick Zamora
 */
public class PaintedPath extends AbstractPaintedGraphic {
    // Note that this class was deprecated becase we were to use
    // diva.compat.canvas instead.  However, the Ptolemy sources
    // do not include diva.compat.canvas, so I'm making this class
    // undeprecated. -cxh 7/05

    /** Create a painted path on the given Shape. The stroke
     * will be in black with a line width of one.
     */
    public PaintedPath(Shape s) {
        this(s, 1);
    }

    /** Create a painted path on the given Shape with a given
     * line width. The stroke will be painted in black.
     */
    public PaintedPath(Shape s, float lineWidth) {
        this(s, 1, Color.black);
    }

    /** Create a painted path on the given Shape with a given
     * line width and stroke color.
     */
    public PaintedPath(Shape s, float lineWidth, Paint paint) {
        shape = s;
        stroke = getStroke(lineWidth);
        strokePaint = paint;
    }

    /** Get the dash array. If the stroke is not a BasicStroke
     * then null will always be returned.
     */
    public float[] getDashArray() {
        if (stroke instanceof BasicStroke) {
            return ((BasicStroke) stroke).getDashArray();
        } else {
            return null;
        }
    }

    /** Get the line width. If the stroke is not a BasicStroke
     * then 1.0 will always be returned.
     */
    @Override
    public float getLineWidth() {
        if (stroke instanceof BasicStroke) {
            return ((BasicStroke) stroke).getLineWidth();
        } else {
            return 1.0f;
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
        return intersects(r);
    }

    /** Test if this shape intersects the given rectangle. Currently
     * this does not take into account the width of the stroke
     * or other things such as dashes, because of problems with
     * geometry testing with GeneralPath in the first version of
     * JDK1.2.
     */
    @Override
    public boolean intersects(Rectangle2D r) {
        // Hit testing on strokes doesn't appear to work too
        // well in JDK1.2, so we will cheat and ignore the width
        // of the stroke
        return ShapeUtilities.intersectsOutline(r, shape);
    }

    /** Paint the shape. The shape is redrawn with the current
     *  shape, paint, and stroke.
     */
    @Override
    public void paint(Graphics2D g) {
        g.setStroke(stroke);
        g.setPaint(strokePaint);
        g.draw(shape);
    }

    /** Set the dash array of the stroke. The existing stroke will
     * be removed, but the line width will be preserved if possible.
     */
    public void setDashArray(float[] dashArray) {
        if (stroke instanceof BasicStroke) {
            stroke = new BasicStroke(((BasicStroke) stroke).getLineWidth(),
                    ((BasicStroke) stroke).getEndCap(),
                    ((BasicStroke) stroke).getLineJoin(),
                    ((BasicStroke) stroke).getMiterLimit(), dashArray, 0.0f);
        } else {
            stroke = new BasicStroke(1.0f, BasicStroke.CAP_SQUARE,
                    BasicStroke.JOIN_MITER, 10.0f, dashArray, 0.0f);
        }
    }

    /** Set the line width. The existing stroke will
     * be removed, but the dash array will be preserved if possible.
     */
    @Override
    public void setLineWidth(float lineWidth) {
        if (stroke instanceof BasicStroke) {
            stroke = new BasicStroke(lineWidth,
                    ((BasicStroke) stroke).getEndCap(),
                    ((BasicStroke) stroke).getLineJoin(),
                    ((BasicStroke) stroke).getMiterLimit(),
                    ((BasicStroke) stroke).getDashArray(), 0.0f);
        } else {
            new BasicStroke(lineWidth, BasicStroke.CAP_SQUARE,
                    BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
        }
    }

    /** Set the stroke
     */
    public void setStroke(Stroke s) {
        stroke = s;
    }
}
