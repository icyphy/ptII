/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 *
 */

package diva.canvas.toolbox;

import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

/** A figure that displays as a rectangle. This is a convenience class
 * for creating rectangles. It inherits from BasicFigure, and so contains
 * a single Rectangle2D as its shape. It provides a useful set of
 * constructors.
 *
 * @version        $Revision$
 * @author         John Reekie
 */
public class BasicRectangle extends BasicFigure {

    /** Create a new rectangle with the given rectangle shape, a
     * unit-width continuous stroke and no paint pattern.
     */
    public BasicRectangle (Rectangle2D rect) {
        super(rect);
    }

    /** Create a new rectangle with the given origin and size, a
     * unit-width continuous stroke and no paint pattern.
     */
    public BasicRectangle (double x, double y, double width, double height) {
        super(new Rectangle2D.Double(x,y,width,height));
    }

    /** Create a new rectangle with the given origin, size, and
     * fill paint. It has no outline.
     */
    public BasicRectangle (
            double x, double y, double width, double height,
            Paint fill) {
        super(new Rectangle2D.Double(x,y,width,height), fill);
    }

    /** Create a new rectangle with the given bounds and
     * fill paint. It has no outline.
     */
    public BasicRectangle (
            Rectangle2D bounds,
            Paint fill) {
        super(bounds, fill);
    }

    /** Create a new rectangle with the given origin, size, and
     * outline width. It has no fill.
     */
    public BasicRectangle (
            double x, double y, double width, double height,
            float lineWidth) {
        super(new Rectangle2D.Double(x,y,width,height), lineWidth);
    }

    /** Create a new rectangle with the given origin, size, fill, and
     * outline width.
     */
    public BasicRectangle (
            double x, double y, double width, double height,
            Paint fill,
            float lineWidth) {
        super(new Rectangle2D.Double(x,y,width,height), fill, lineWidth);
    }

    /** Translate the rectangle the given distance
     */
    public void translate (double x, double y) {
        Shape s = getShape();
        if (s instanceof Rectangle2D) {
            Rectangle2D r = (Rectangle2D)s;
            repaint();
            r.setFrame(r.getX()+x, r.getY()+y, r.getWidth(), r.getHeight());
            repaint();
        } else {
            super.translate(x,y);
        }
    }
}


