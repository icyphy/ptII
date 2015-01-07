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

import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

/** A figure that displays as a rectangle. This is a convenience class
 * for creating rectangles. It inherits from BasicFigure, and so contains
 * a single Rectangle2D as its shape. It provides a useful set of
 * constructors.
 *
 * @version        $Id$
 * @author         John Reekie
 */
public class BasicRectangle extends BasicFigure {
    /** Create a new rectangle with the given rectangle shape, a
     * unit-width continuous stroke and no paint pattern.
     */
    public BasicRectangle(Rectangle2D rect) {
        super(rect);
    }

    /** Create a new rectangle with the given origin and size, a
     * unit-width continuous stroke and no paint pattern.
     */
    public BasicRectangle(double x, double y, double width, double height) {
        super(new Rectangle2D.Double(x, y, width, height));
    }

    /** Create a new rectangle with the given origin, size, and
     * fill paint. It has no outline.
     */
    public BasicRectangle(double x, double y, double width, double height,
            Paint fill) {
        super(new Rectangle2D.Double(x, y, width, height), fill);
    }

    /** Create a new rectangle with the given bounds and
     * fill paint. It has no outline.
     */
    public BasicRectangle(Rectangle2D bounds, Paint fill) {
        super(bounds, fill);
    }

    /** Create a new rectangle with the given origin, size, and
     * outline width. It has no fill.
     */
    public BasicRectangle(double x, double y, double width, double height,
            float lineWidth) {
        super(new Rectangle2D.Double(x, y, width, height), lineWidth);
    }

    /** Create a new rectangle with the given origin, size, fill, and
     * outline width.
     */
    public BasicRectangle(double x, double y, double width, double height,
            Paint fill, float lineWidth) {
        super(new Rectangle2D.Double(x, y, width, height), fill, lineWidth);
    }

    /** Translate the rectangle the given distance
     */
    @Override
    public void translate(double x, double y) {
        Shape s = getShape();

        if (s instanceof Rectangle2D) {
            Rectangle2D r = (Rectangle2D) s;
            repaint();
            r.setFrame(r.getX() + x, r.getY() + y, r.getWidth(), r.getHeight());
            repaint();
        } else {
            super.translate(x, y);
        }
    }
}
