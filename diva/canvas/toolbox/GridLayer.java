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
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import diva.canvas.CanvasLayer;
import diva.canvas.VisibleComponent;

/**
 * A grid layer displays a rectangular grid. It can be made to display
 * grid lines, grid vertices, or both. For grid lines, the stroke
 * and the paint can be specified; for grid vertices, the shape
 * drawn at the vertex and the paint can be specified. Reasonable
 * default are also supplied.
 *
 * @author Michael Shilman
 * @author John Reekie
 * @version        $Id$
 */
public class GridLayer extends CanvasLayer implements VisibleComponent {
    /* The paint used to draw grid lines
     */
    private Paint _gridPaint = Color.lightGray;

    /* The stroke for drawing grid lines
     */
    private Stroke _gridStroke = new BasicStroke();

    /* The origin of the grid. The default is (0,0).
     */
    private Point2D _origin = new Point2D.Double(0.0, 0.0);

    /* The distance between grid points. The default is (10.0, 10.0).
     * (There's no overriding reason to set the default at this,
     * but we decided that it's more convenient to have a default
     * grid size and let the client change it when necessary.)
     */
    private Point2D _size = new Point2D.Double(10.0, 10.0);

    /* The paint used to draw grid vertices. There is no default
     */
    private Paint _vertexPaint;

    /* The shape used to draw grid vertices.
     */
    private Shape _vertexShape;

    /* The visibility flag.
     */
    private boolean _visible = true;

    /** Create a new GridLayer with a default one-pixel grid stroke
     * and a light grey grid color.
     */
    public GridLayer() {
        super();
    }

    /** Create a new GridLayer with the given grid stroke and paint
     */
    public GridLayer(Stroke s, Paint p) {
        super();
        setGridStroke(s);
        setGridPaint(p);
    }

    /** Create a new GridLayer with the given vertex shape and paint.
     * Grid lines will not be displayed.
     */
    public GridLayer(Shape s, Paint p) {
        super();
        setGridPaint(null);
        setVertexShape(s);
        setVertexPaint(p);
    }

    /** Get the grid line stroke.
     */
    public Stroke getGridStroke() {
        return _gridStroke;
    }

    /** Get the grid line paint.
     */
    public Paint getGridPaint() {
        return _gridPaint;
    }

    /** Get the vertex point shape.
     */
    public Shape getVertexShape() {
        return _vertexShape;
    }

    /** Get the vertex point paint.
     */
    public Paint getVertexPaint() {
        return _vertexPaint;
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
     * is not visible, return immediately. Otherwise paint the grid
     * lines if they have a non-null paint, and paint the grid vertices
     * if they have a non-null paint.
     *
     * <p>FIXME: Vertexes are not yet supported.
     */

    /*
     public void paint (Graphics2D g) {
     Point2D paneSize = getCanvasPane().getSize();
     if (!isVisible()) {
     return;
     }

     double originX = _origin.getX();
     double originY = _origin.getY();
     double sizeX = _size.getX();
     double sizeY = _size.getY();
     double paneX = paneSize.getX();
     double paneY = paneSize.getY();

     if (_gridPaint != null) {
     g.setPaint(_gridPaint);
     g.setStroke(_gridStroke);

     Line2D.Double vline = new Line2D.Double();
     Line2D.Double hline = new Line2D.Double();

     double x = originX;
     double y;

     vline.y1 = originY;
     vline.y2 = paneY;

     hline.x1 = originY;
     hline.x2 = paneX;

     while (x < paneX) {
     vline.x1 = x;
     vline.x2 = x;
     g.draw(vline);

     y = originY;
     while (y < paneY) {
     hline.y1 = y;
     hline.y2 = y;
     g.draw(hline);
     y += sizeY;
     }
     x += sizeX;
     }
     }
     }
     */
    @Override
    public void paint(Graphics2D g) {
        Point2D paneSize = getCanvasPane().getSize();

        if (!isVisible()) {
            return;
        }

        double originX = _origin.getX();
        double originY = _origin.getY();
        double sizeX = _size.getX();
        double sizeY = _size.getY();
        double paneX = paneSize.getX();
        double paneY = paneSize.getY();

        if (_gridPaint != null) {
            g.setPaint(_gridPaint);

            // g.setStroke(_gridStroke);
            double x = originX;
            double y;

            while (x < paneX) {
                g.drawLine((int) x, (int) originY, (int) x, (int) paneY);

                y = originY;

                while (y < paneY) {
                    g.drawLine((int) originX, (int) y, (int) paneX, (int) y);
                    y += sizeY;
                }

                x += sizeX;
            }
        }
    }

    /** Paint this layer onto a 2D graphics object, within the given
     * region.  If the layer is not visible, return
     * immediately.  Otherwise paint the grid
     * lines if they have a non-null paint, and paint the grid vertices
     * if they have a non-null paint.
     */
    @Override
    public void paint(Graphics2D g, Rectangle2D region) {
        if (!isVisible()) {
            return;
        }

        // FIXME: paint in region
        paint(g);
    }

    /** Set the grid line stroke.
     */
    public void setGridStroke(Stroke s) {
        if (s == null) {
            throw new NullPointerException("Cannot set stroke to null");
        }

        _gridStroke = s;
        repaint();
    }

    /** Set the grid line paint. If this is set to null, grid lines
     * will not be displayed.
     */
    public void setGridPaint(Paint p) {
        _gridPaint = p;
        repaint();
    }

    /** Set the vertex point shape. The default is a circle
     * with radius equal to 10% of the smallest grid size in either
     * dimension.
     */
    public void setVertexShape(Shape s) {
        if (s == null) {
            throw new NullPointerException("Cannot set shape to null");
        }

        _vertexShape = s;
        repaint();
    }

    /** Set the vertex point paint. If this is set to null,
     * vertices will not be displayed.
     */
    public void setVertexPaint(Paint p) {
        _vertexPaint = p;

        if (_vertexPaint != null && _vertexShape == null) {
            double r = java.lang.Math.min(_size.getX(), _size.getY());
            _vertexShape = new Ellipse2D.Double(-r, -r, 2 * r, 2 * r);
        }

        repaint();
    }

    /** Set the visibility flag of this layer. If the flag is false,
     * then the layer will not be drawn on the screen.
     */
    @Override
    public void setVisible(boolean flag) {
        _visible = flag;
    }
}
