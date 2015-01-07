/*
 Copyright (c) 2006-2014 The Regents of the University of California
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
import java.awt.geom.RoundRectangle2D;

/** A figure that displays as a rounded rectangle. This is a convenience class
 * for creating rectangles. It inherits from BasicFigure, and so contains
 * a single RoundRectangle2D as its shape. It provides a useful set of
 * constructors.
 *
 * @version $Id$
 * @author Edward A. Lee
 * @since Ptolemy II 6.0
 */
public class RoundedRectangle extends BasicFigure {

    /** Create a new rectangle with the given origin, size, fill,
     *  outline width, arc width, and arc height.
     */
    public RoundedRectangle(double x, double y, double width, double height,
            Paint fill, float lineWidth, double arcWidth, double arcHeight) {
        super(new RoundRectangle2D.Double(x, y, width, height, arcWidth,
                arcHeight), fill, lineWidth);
    }

    /** Translate the rectangle the given distance
     */
    @Override
    public void translate(double x, double y) {
        Shape s = getShape();

        if (s instanceof RoundRectangle2D) {
            RoundRectangle2D r = (RoundRectangle2D) s;
            repaint();
            r.setFrame(r.getX() + x, r.getY() + y, r.getWidth(), r.getHeight());
            repaint();
        } else {
            super.translate(x, y);
        }
    }
}
