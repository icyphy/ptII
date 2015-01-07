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
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import diva.canvas.AbstractFigure;
import diva.canvas.AbstractSite;
import diva.canvas.Figure;
import diva.canvas.Site;

/** A terminal that consists of a straight line plus an additional
 * decoration at the connection end of the terminal.
 *
 * @version $Id$
 * @author  John Reekie
 */
public class StraightTerminal extends AbstractFigure implements Terminal {
    /** The end decoration
     */
    private ConnectorEnd _end;

    /** The attachment site
     */
    private Site _attachSite;

    /** The connection site
     */
    private Site _connectSite;

    /** The length of the terminal
     */
    private double _length = 20.0;

    /** The line that is drawn to display the terminal
     */
    private Line2D _line;

    /** The stroke of the line
     */
    private Stroke _stroke;

    /** The paint of the line
     */
    private Paint _paint;

    /** Create a new terminal which is not attached to anything.
     * The terminal should not be displayed until after setAttachSite()
     * is called.
     */
    public StraightTerminal() {
        this(new NullSite(), new BasicStroke(), Color.black);
    }

    /** Create a new terminal attached to the given site.
     */
    public StraightTerminal(Site attachSite) {
        this(attachSite, new BasicStroke(), Color.black);
    }

    /** Create a new terminal attached to the given site,
     * with the given stroke and paint. If the attach site is not
     * yet available, pass a NullSite.
     */
    public StraightTerminal(Site attachSite, Stroke stroke, Paint paint) {
        this._attachSite = attachSite;
        this._paint = paint;
        this._stroke = stroke;

        _connectSite = new ConnectSite();
        _line = new Line2D.Double();

        if (!(_attachSite instanceof NullSite)) {
            recompute();
        }
    }

    /** Get the site to which the terminal is attached
     */
    @Override
    public Site getAttachSite() {
        return _attachSite;
    }

    /** Get the bounding box of this terminal.
     */
    @Override
    public Rectangle2D getBounds() {
        Rectangle2D bounds = _stroke.createStrokedShape(_line).getBounds2D();

        if (_end != null) {
            Rectangle2D.union(bounds, _end.getBounds(), bounds);
        }

        return bounds;
    }

    /** Get the site to which a connector can attach
     */
    @Override
    public Site getConnectSite() {
        return _connectSite;
    }

    /** Get the object drawn at the end of the terminal, if there
     * is one.
     */
    public ConnectorEnd getEnd() {
        return _end;
    }

    /** Get the paint of this terminal.
     */
    public Paint getPaint() {
        return _paint;
    }

    /** Get the outline shape of this terminal.
     */
    @Override
    public Shape getShape() {
        // FIXME: this really should include the "end"
        return _line;
    }

    /** Get the stroke of this terminal.
     */
    public Stroke getStroke() {
        return _stroke;
    }

    /** Test if this terminal is hit by the given rectangle.
     */
    @Override
    public boolean hit(Rectangle2D r) {
        if (!isVisible()) {
            return false;
        }

        boolean hit = intersects(r);

        // Do the end too. Does ConnectorEnd needs a proper hit() method?
        if (_end != null) {
            hit = hit || r.intersects(_end.getBounds());
        }

        return hit;
    }

    /** Paint the terminal.
     */
    @Override
    public void paint(Graphics2D g) {
        g.setStroke(_stroke);
        g.setPaint(_paint);
        g.draw(_line);

        if (_end != null) {
            _end.paint(g);
        }
    }

    /** Tell the terminal to completely recompute its shape.
     */
    private void recompute() {
        double x1 = _attachSite.getX();
        double y1 = _attachSite.getY();
        double normal = _attachSite.getNormal();
        double x2 = x1 + _length * Math.cos(normal);
        double y2 = y1 + _length * Math.sin(normal);

        _line.setLine(x1, y1, x2, y2);

        if (_end != null) {
            _end.setOrigin(x2, y2);
            _end.setNormal(normal);
        }
    }

    /** Tell the terminal to reposition itself over the attachment
     * site.
     */
    @Override
    public void relocate() {
        translate(_attachSite.getX() - _line.getX1(), _attachSite.getY()
                - _line.getY1());
    }

    /** Set the site to which the terminal is attached.
     */
    @Override
    public void setAttachSite(Site s) {
        _attachSite = s;
        recompute();
        repaint();
    }

    /**
     * Set the object drawn at the end of the terminal.
     */
    public void setEnd(ConnectorEnd e) {
        repaint();
        _end = e;
        _end.setOrigin(_connectSite.getX(), _connectSite.getY());
        _end.setNormal(_connectSite.getNormal());
        repaint();
    }

    /** Set the stroke of this terminal.
     * Currently, this call has no effect on the terminal end
     * shape, if it has one.
     */
    public void setStroke(Stroke s) {
        repaint();
        _stroke = s;
        repaint();
    }

    /** Set the stroke paint pattern of this terminal.
     * Currently, this call has no effect on the terminal end
     * shape, if it has one.
     */
    public void setPaint(Paint p) {
        _paint = p;
        repaint();
    }

    /** Transform the terminal. This is ignored, since the location
     * and orientation of a terminal is determined solely by its
     * attachment site and other parameters.
     */
    @Override
    public void transform(AffineTransform at) {
        // do nothing
    }

    /** Translate the terminal. This is implemented since it is
     * the most efficient way for figures that contain terminals
     * to translate themselves. However, this method does not
     * call repaint(), on the assumption that the parent figure
     * will do so anyway.
     */
    @Override
    public void translate(double x, double y) {
        //// repaint();
        _line.setLine(_line.getX1() + x, _line.getY1() + y, _line.getX2() + x,
                _line.getY2() + y);

        if (_end != null) {
            _end.translate(x, y);
        }

        //// repaint();
    }

    ///////////////////////////////////////////////////////////////////
    //// ConnectSite

    /** The site at which a connector can connect to a straight terminal.
     * Each StraightTerminal has exactly one ConnectSite, which has
     * ID 0.
     */
    private class ConnectSite extends AbstractSite {
        /** Get the ID of this site.
         */
        @Override
        public int getID() {
            return 0;
        }

        /** Get the figure to which this site is attached.
         */
        @Override
        public Figure getFigure() {
            return StraightTerminal.this;
        }

        /** Get the normal to this site, in radians
         * between zero and 2pi. The direction is "out" of the site.
         */
        @Override
        public double getNormal() {
            return _attachSite.getNormal();
        }

        /** Get the x-coordinate of the site.
         */
        @Override
        public double getX() {
            return _line.getX2();
        }

        /** Get the y-coordinate of the site.
         */
        @Override
        public double getY() {
            return _line.getY2();
        }

        /** Test if this site has a "normal" to it. This method
         * returns the same value as the attachment site.
         */
        @Override
        public boolean hasNormal() {
            return _attachSite.hasNormal();
        }

        /** Test if this site has a normal in the given direction.
         * The direction is that given by one of the static constants
         * NORTH, SOUTH, EAST, or WEST, defined in
         * <b>javax.swing.SwingConstants</b>.
         */
        @Override
        public boolean isNormal(int direction) {
            return _attachSite.isNormal(direction);
        }

        /** Translate the site by the indicated distance. This
         * method throws an exception.
         */
        @Override
        public void translate(double x, double y) {
            throw new UnsupportedOperationException(
                    "Terminal connection points cannot be moved");
        }
    }
}
