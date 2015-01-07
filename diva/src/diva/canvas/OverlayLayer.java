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
package diva.canvas;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * OutlineLayer is a layer that is used to display the grey
 * figures commonly used for drag-selection, reshaping items,
 * and so on. It can have shapes added to it, which are all drawn
 * in outline in grey (by default -- the color can be changed).
 * There is no concept of a display list or z-depth in this
 * layer, as all shapes are drawn in exactly the same color.
 *
 * <P> Although currently it does not do so, this class
 * will become optimized so that repaints of this layer do not
 * require a repaint of backing layers.
 *
 * @author Michael Shilman
 * @author John Reekie
 * @version $Id$
 * @Pt.AcceptedRating Red
 */
public class OverlayLayer extends CanvasLayer implements VisibleComponent {
    /* The list of shapes that are stroked out
     * on this layer.
     */
    private ArrayList _shapes = new ArrayList();

    /* The stroke for drawing shapes
     */
    private Stroke _stroke;

    /* The paint used to draw shapes
     */
    private Paint _paint;

    /* The visibility flag.
     */
    private boolean _visible = true;

    /** Create a new OverlayLayer with a default one-pixel stroke
     * and a light grey stroke color.
     */
    public OverlayLayer() {
        super();
        setStroke(new BasicStroke());
        setPaint(Color.lightGray);
    }

    /** Create a new OverlayLayer with the given stroke and paint
     */
    public OverlayLayer(Stroke s, Paint p) {
        super();
        setStroke(s);
        setPaint(p);
    }

    /** Add a new shape to the list of shapes in this layer
     */
    public void add(Shape s) {
        _shapes.add(s);
    }

    /** Clear the layer
     */
    public void clear() {
        _shapes.clear();
    }

    /** Get the current paint stroke
     */
    public Stroke getStroke() {
        return _stroke;
    }

    /** Get the current paint
     */
    public Paint getPaint() {
        return _paint;
    }

    /** Test the visibility flag of this layer. Note that this flag
     *  does not indicate whether the layer is actually visible on
     *  the screen, as its pane or one if its ancestors may not be visible.
     */
    @Override
    public boolean isVisible() {
        return _visible;
    }

    /** Paint this layer onto a 2D graphics object. If the layer
     * is not visible, return immediately. Otherwise draw all shapes
     * with the current stroke and paint.
     */
    @Override
    public void paint(Graphics2D g) {
        if (!isVisible()) {
            return;
        }

        g.setStroke(_stroke);
        g.setPaint(_paint);

        Shape s;
        Iterator i = shapes();

        while (i.hasNext()) {
            s = (Shape) i.next();
            g.draw(s);
        }
    }

    /** Paint this layer onto a 2D graphics object, within the given
     * region.  If the layer is not visible, return immediately.
     * Otherwise draw all figures that overlap the given region.
     */
    @Override
    public void paint(Graphics2D g, Rectangle2D region) {
        if (!isVisible()) {
            return;
        }

        g.setStroke(_stroke);
        g.setPaint(_paint);

        Shape s;
        Iterator i = shapes();

        while (i.hasNext()) {
            s = (Shape) i.next();

            if (s.intersects(region)) {
                g.draw(s);
            }
        }
    }

    /** Remove a shape from the list of shapes in this layer
     */
    public void remove(Shape s) {
        _shapes.remove(s);
    }

    /** Schedule a repaint of this layer over the given shape.
     * The shape is assumed to be a shape that is on this layer.
     * It is made larger by the line width of the outlines, to
     * ensure that enough of the canvas is repainted. This
     * particular version of this method is optimized for
     * rectangles.
     */
    public void repaint(Rectangle2D region) {
        // If we don't have a BasicStroke, revert to the
        // general version of this method.
        if (!(_stroke instanceof BasicStroke)) {
            repaint((Shape) region);
        }

        // Otherwise adjust the region by the line width and repaint
        float lineWidth = ((BasicStroke) _stroke).getLineWidth();
        double x = region.getX();
        double y = region.getY();
        double w = region.getWidth();
        double h = region.getHeight();
        repaint(DamageRegion.createDamageRegion(getTransformContext(), x
                - lineWidth, y - lineWidth, w + 2 * lineWidth, h + 2
                * lineWidth));
    }

    /** Schedule a repaint of this layer over the given shape.
     * The shape is assumed to be a shape that is on this layer.
     * The width of the outline is taken into account when
     * repainting the canvas.
     */
    public void repaint(Shape shape) {
        Shape s = _stroke.createStrokedShape(shape);
        Rectangle2D bounds = s.getBounds();

        // Repaint the region
        repaint(DamageRegion.createDamageRegion(getTransformContext(), bounds));
    }

    /** Return an iterator over the shapes currently in this layer.
     */
    public Iterator shapes() {
        return _shapes.iterator();
    }

    /** Set the stroke. All shapes will be stroked with
     * the same stroke.
     */
    public void setStroke(Stroke s) {
        _stroke = s;
        repaint();
    }

    /** Set the paint. All shapes will be drawn with
     * the same paint.
     */
    public void setPaint(Paint p) {
        _paint = p;
        repaint();
    }

    /** Set the visibility flag of this layer. If the flag is false,
     * then the layer will not be drawn on the screen.
     */
    @Override
    public void setVisible(boolean flag) {
        _visible = flag;
        repaint();
    }
}
