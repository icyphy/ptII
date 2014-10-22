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

/** A PathFigure is one that contains a single instance of
 *  Shape. The figure can have a fill with optional compositing (for
 *  translucency), and a stroke with a different fill. With this
 *  class, simple objects can be created on-the-fly simply by passing
 *  an instance of java.awt.Shape to the constructor. This class
 *  is mainly intended for use for open shapes (without fill).
 *  For filled shapes, use the BasicFigure class, and for more complex
 *  figures, use VectorFigure or create a custom Figure class.
 *
 * @version        $Id$
 * @author         John Reekie
 * @deprecated  BasicFigure now does everything this class used to do.
 */
@Deprecated
public class PathFigure extends BasicFigure {
    /** Create a new figure with the given shape. The figure, by
     *  default, has a unit-width continuous black outline and no
     *  fill.  The given shape will be cloned to prevent the original
     *  from being modified.
     */
    public PathFigure(Shape shape) {
        this(shape, null, 1.0f);
    }

    /** Create a new figure with the given shape and outline width.
     * It has no fill. The default outline paint is black.  The given
     * shape will be cloned to prevent the original from being
     * modified.
     *
     * @deprecated  Use the float constructor instead.
     */
    @Deprecated
    public PathFigure(Shape shape, int lineWidth) {
        this(shape, null, lineWidth);
    }

    /** Create a new figure with the given shape and outline width.
     * It has no fill. The default outline paint is black.  The given
     * shape will be cloned to prevent the original from being
     * modified.
     */
    public PathFigure(Shape shape, float lineWidth) {
        this(shape, null, lineWidth);
    }

    /** Create a new figure with the given paint pattern. The figure,
     *  by default, has no stroke.  The given shape will be cloned to
     *  prevent the original from being modified.
     */
    public PathFigure(Shape shape, Paint fill) {
        this(shape, fill, 1.0f);
    }

    /** Create a new figure with the given paint pattern and line
     *  width.  The given shape will be cloned to prevent the original
     *  from being modified.
     */
    public PathFigure(Shape shape, Paint fill, float lineWidth) {
        super(shape, fill, lineWidth);
    }
}
