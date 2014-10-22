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
 */
package diva.util.java2d;

import java.awt.BasicStroke;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;

/**
 * An abstract implementation of the PaintedGraphic interface.
 * This class implements the common elements of the PaintedGraphic
 * abstraction.
 *
 * @author Nick Zamora
 * @version $Id$
 */
public abstract class AbstractPaintedGraphic implements PaintedGraphic {
    // Note that this class was deprecated becase we were to use
    // diva.compat.canvas instead.  However, the Ptolemy sources
    // do not include diva.compat.canvas, so I'm making this class
    // undeprecated. -cxh 7/05

    /** The stroke.
     */
    public Stroke stroke;

    /** The shape being painted.
     */
    public Shape shape;

    /** The stroke paint.
     */
    public Paint strokePaint;

    /** A static array of cached strokes
     */
    private static BasicStroke[] _strokes = new BasicStroke[16];

    /** Get the line width.
     */
    @Override
    public abstract float getLineWidth();

    /** Get the bounding box of the shape when stroked. This method takes
     * account of the thickness of the stroke.
     */
    @Override
    public Rectangle2D getBounds() {
        // FIXME: these bounds REALLY need to be cached.  But it's
        // painful because of the public members.
        if (stroke == null) {
            return shape.getBounds2D();
        } else if (stroke instanceof BasicStroke) {
            // For some reason (antialiasing?) the bounds returned by
            // BasicStroke is off by one.  This code works around it.
            // if all we want is the bounds, then we don't need to actually
            // stroke the shape.  We've had reports that this is no longer
            // necessary with JDK1.3.
            Rectangle2D rect = shape.getBounds2D();
            int width = (int) ((BasicStroke) stroke).getLineWidth() + 2;
            return new Rectangle2D.Double(rect.getX() - width, rect.getY()
                    - width, rect.getWidth() + width + width, rect.getHeight()
                    + width + width);
        } else {
            // For some reason (antialiasing?) the bounds returned by
            // BasicStroke is off by one.  This code works around it.
            // We've had reports that this is no longer
            // necessary with JDK1.3.
            Rectangle2D rect = stroke.createStrokedShape(shape).getBounds2D();
            return new Rectangle2D.Double(rect.getX() - 1, rect.getY() - 1,
                    rect.getWidth() + 2, rect.getHeight() + 2);
        }
    }

    /** Get the stroke.
     */
    @Override
    public Stroke getStroke() {
        return stroke;
    }

    /** Get a new stroke of the given width and with no dashing.
     * This method will generally return an existing stroke
     * object, and can be used to save creating zillions of
     * Stroke objects.
     */
    public static BasicStroke getStroke(int width) {
        if (width < _strokes.length) {
            if (_strokes[width] == null) {
                _strokes[width] = new BasicStroke(width);
            }

            return _strokes[width];
        } else {
            return new BasicStroke(width);
        }
    }

    /** Get a new stroke of the given width and with no dashing.  This
     * method will return an existing stroke object if the width is
     * integer-valued and has a reasonably small width. This method
     * can be used to save creating zillions of Stroke objects.
     */
    public static BasicStroke getStroke(float floatwidth) {
        int width = Math.round(floatwidth);

        if (width == floatwidth) {
            return getStroke(width);
        } else {
            return new BasicStroke(floatwidth);
        }
    }

    /** Test if this shape intersects the given rectangle.  Currently
     * this does not take into account the width of the stroke
     * or other things such as dashes, because of problems with
     * geometry testing with GeneralPath in the first version of
     * JDK1.2.
     */
    @Override
    public abstract boolean hit(Rectangle2D r);

    /** Set the line width.
     */
    @Override
    public abstract void setLineWidth(float lineWidth);
}
