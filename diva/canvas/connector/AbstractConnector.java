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
package diva.canvas.connector;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import diva.canvas.AbstractFigure;
import diva.canvas.Site;
import diva.canvas.toolbox.LabelFigure;
import diva.util.java2d.ShapeUtilities;

/** An abstract implementation of Connector. The implementation
 * provides default implementations of all routing methods except
 * for route(). It also provides a set of methods for setting
 * the appearance of the connector, such as line width, dashes,
 * and color. To do so, it uses an instance of PaintedPath, so
 * see that class for a more detailed description of the
 * paint- and stroke-related methods.
 *
 * @version $Id$
 * @author  John Reekie
 * @author  Michael Shilman
 */
public abstract class AbstractConnector extends AbstractFigure implements
Connector {
    /** The head end
     */
    private ConnectorEnd _headEnd = null;

    /** The tail end
     */
    private ConnectorEnd _tailEnd = null;

    /** The head site
     */
    private Site _headSite = null;

    /** The tail site
     */
    private Site _tailSite = null;

    /** The label figure
     */
    private LabelFigure _labelFigure;

    /** The shape that we use to draw the connector.
     */
    private Shape _shape;

    /** The stroke.
     */
    private Stroke _stroke;

    /** The stroke paint.
     */
    private Paint _paint;

    /** Create a new connector between the given sites. The connector, by
     *  default, is stroked with a unit-width continuous black stroke.
     */
    public AbstractConnector(Site tail, Site head) {
        _tailSite = tail;
        _headSite = head;
        _stroke = ShapeUtilities.getStroke(1);
        _paint = Color.black;
    }

    /** Get the bounding box of this connector.
     */
    @Override
    public Rectangle2D getBounds() {
        Rectangle2D bounds = ShapeUtilities.computeStrokedBounds(_shape,
                _stroke);

        if (_headEnd != null) {
            Rectangle2D.union(bounds, _headEnd.getBounds(), bounds);
        }

        if (_tailEnd != null) {
            Rectangle2D.union(bounds, _tailEnd.getBounds(), bounds);
        }

        if (_labelFigure != null) {
            Rectangle2D.union(bounds, _labelFigure.getBounds(), bounds);
        }

        return bounds;
    }

    /** Get the dash array. If the stroke is not a BasicStroke
     * then null will always be returned.
     */
    public float[] getDashArray() {
        if (_stroke instanceof BasicStroke) {
            return ((BasicStroke) _stroke).getDashArray();
        } else {
            return null;
        }
    }

    /** Get the object drawn at the head end of the connector, if there
     * is one.
     */
    public ConnectorEnd getHeadEnd() {
        return _headEnd;
    }

    /** Get the site that marks the "head" of the connector.
     */
    @Override
    public Site getHeadSite() {
        return _headSite;
    }

    /** Get the figure that displays this connector's label.
     * This may be null.
     */
    public LabelFigure getLabelFigure() {
        return _labelFigure;
    }

    /** Get the line width of this figure. If the stroke is not a BasicStroke
     * then 1.0 will always be returned.
     */
    public float getLineWidth() {
        if (_stroke instanceof BasicStroke) {
            return ((BasicStroke) _stroke).getLineWidth();
        } else {
            return 1.0f;
        }
    }

    /** Get the object drawn at the tail end of the connector, if there
     * is one.
     */
    public ConnectorEnd getTailEnd() {
        return _tailEnd;
    }

    /** Get the outline shape of this connector.
     */
    @Override
    public Shape getShape() {
        return _shape;
    }

    /** Get the stroke of this connector.
     */
    public Stroke getStroke() {
        return _stroke;
    }

    /** Get the stroke paint pattern of this connector.
     */
    public Paint getStrokePaint() {
        return _paint;
    }

    /** Get the site that marks the "tail" of the connector.
     */
    @Override
    public Site getTailSite() {
        return _tailSite;
    }

    /** Inform the connector that the head site has moved.
     * This default implementation simply calls reroute().
     */
    @Override
    public void headMoved() {
        repaint();
        reroute();
        repaint();
    }

    /** Test if this connector is hit by the given rectangle.
     * If the connector is not visible, always return false, otherwise
     * check to see if the rectangle intersects the path of the connector,
     * either of its ends, or the label.
     */
    @Override
    public boolean hit(Rectangle2D r) {
        if (!isVisible()) {
            return false;
        }

        boolean hit = ShapeUtilities.intersectsOutline(r, _shape);

        if (_labelFigure != null) {
            hit = hit || _labelFigure.hit(r);
        }

        // Do the ends too. Does ConnectorEnd needs a proper hit() method?
        if (_headEnd != null) {
            hit = hit || r.intersects(_headEnd.getBounds());
        }

        if (_tailEnd != null) {
            hit = hit || r.intersects(_tailEnd.getBounds());
        }

        return hit;
    }

    /** Test if this connector intersects the given rectangle. This default
     *  implementation checks to see if the rectangle intersects with the
     *  path of the connector, the label, or either of the connector ends.
     */
    @Override
    public boolean intersects(Rectangle2D r) {
        boolean hit = ShapeUtilities.intersectsOutline(r, _shape);

        if (_labelFigure != null) {
            hit = hit || _labelFigure.intersects(r);
        }

        // Do the ends too. Does ConnectorEnd needs a proper hit() method?
        if (_headEnd != null) {
            hit = hit || r.intersects(_headEnd.getBounds());
        }

        if (_tailEnd != null) {
            hit = hit || r.intersects(_tailEnd.getBounds());
        }

        return hit;
    }

    /** Paint the connector.
     */
    @Override
    public void paint(Graphics2D g) {
        g.setStroke(_stroke);
        g.setPaint(_paint);
        g.draw(_shape);

        if (_headEnd != null) {
            _headEnd.paint(g);
        }

        if (_tailEnd != null) {
            _tailEnd.paint(g);
        }

        if (_labelFigure != null) {
            _labelFigure.paint(g);
        }
    }

    /** Tell the connector to reposition its label if it has one.
     * This is an abstract method that must be implemented by
     * subclasses. In general, implementations of the routing
     * methods will also call this method.
     */
    public abstract void repositionLabel();

    /** Tell the connector to re-route itself. This default implementation
     * simply calls route(). In general, this method should be overridden
     * to perform this more efficiently.
     */
    @Override
    public void reroute() {
        route();
    }

    /** Tell the connector to route itself completely,
     * using all available information.
     */
    @Override
    public abstract void route();

    /** Set the dash array of the stroke. The existing stroke will
     * be removed, but the line width will be preserved if possible.
     */
    public void setDashArray(float[] dashArray) {
        repaint();

        if (_stroke instanceof BasicStroke) {
            _stroke = new BasicStroke(((BasicStroke) _stroke).getLineWidth(),
                    ((BasicStroke) _stroke).getEndCap(),
                    ((BasicStroke) _stroke).getLineJoin(),
                    ((BasicStroke) _stroke).getMiterLimit(), dashArray, 0.0f);
        } else {
            _stroke = new BasicStroke(1.0f, BasicStroke.CAP_SQUARE,
                    BasicStroke.JOIN_MITER, 10.0f, dashArray, 0.0f);
        }

        repaint();
    }

    /**
     * Set the object drawn at the head end of the connector.
     */
    public void setHeadEnd(ConnectorEnd e) {
        // We can't just call reroute, because then route() doesn't have a
        // chance to set the normal of the end before painting it.
        repaint();
        _headEnd = e;
        repaint();
        reroute();
    }

    /** Set the site that marks the "head" of the connector,
     * and call headMoved();
     */
    @Override
    public void setHeadSite(Site s) {
        _headSite = s;
        headMoved();
    }

    /** Set the LabelFigure of this connector. If there is no label
     *  figure currently, one is created and placed on the connector.
     */
    public void setLabelFigure(LabelFigure label) {
        _labelFigure = label;
        repositionLabel();
    }

    /** Set the line width. The existing stroke will
     * be removed, but the dash array will be preserved if possible.
     */
    public void setLineWidth(float lineWidth) {
        repaint();

        if (_stroke instanceof BasicStroke) {
            _stroke = new BasicStroke(lineWidth,
                    ((BasicStroke) _stroke).getEndCap(),
                    ((BasicStroke) _stroke).getLineJoin(),
                    ((BasicStroke) _stroke).getMiterLimit(),
                    ((BasicStroke) _stroke).getDashArray(), 0.0f);
        } else {
            new BasicStroke(lineWidth, BasicStroke.CAP_SQUARE,
                    BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
        }

        repaint();
    }

    /** Set the shape, for subclasses only.
     */
    protected void setShape(Shape s) {
        _shape = s;
    }

    /** Set the stroke of this connector.
     */
    public void setStroke(Stroke s) {
        repaint();
        _stroke = s;
        repaint();
    }

    /** Set the stroke paint pattern of this connector.
     */
    public void setStrokePaint(Paint p) {
        repaint();
        _paint = p;
        repaint();
    }

    /**
     * Set the object drawn at the tail end of the connector.
     */
    public void setTailEnd(ConnectorEnd e) {
        // We can't just call reroute, because then route() doesn't have a
        // chance to set the normal of the end before painting it.
        repaint();
        _tailEnd = e;
        repaint();
        reroute();
    }

    /** Set the site that marks the "tail" of the connector.
     */
    @Override
    public void setTailSite(Site s) {
        _tailSite = s;
        tailMoved();
    }

    /** Inform the connector that the tail site has moved.
     * This default implementation simply calls reroute().
     */
    @Override
    public void tailMoved() {
        repaint();
        reroute();
        repaint();
    }

    /** Transform the connector. This method is ignored, since
     * connectors are defined by the head and tail sites.
     */
    @Override
    public void transform(AffineTransform at) {
        // do nothing
    }

    /** Translate the connector. This method must be implemented, since
     * controllers may wish to translate connectors when the
     * sites at both ends are moved the same distance.
     */
    @Override
    public abstract void translate(double x, double y);
}
