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
package diva.canvas.interactor;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.swing.SwingConstants;

import diva.canvas.AbstractSite;
import diva.canvas.Figure;
import diva.canvas.Site;

/**
 * A class that provides support for manipulating
 * the bounds of a figure.  Because this geometry only provides one site
 * at the bounds of the figure, and transforms the bounds equally in all
 * directions, it makes sense to only use this Geometry type for figures
 * containing circular shapes or otherwise highly-symmetric shapes.
 *
 * @author Nick Zamora
 * @version $Id$
 */
public class CircleGeometry implements Geometry {
    ///////////////////////////////////////////////////////////////////
    //// private fields

    /** The figure to which the sites are attached
     */
    private Figure _parentFigure;

    /** The minimum size of the circle
     */
    private double _minSize = 1.0;

    /** The defining rectangle
     */
    private Rectangle2D _rect;

    /** The number of sites in the sites array.
     * Note: SwingConstants start at 1!
     */
    private static int _siteCount = 1;

    /** The sites that exist so far
     */
    private CircleSite[] _sites = new CircleSite[_siteCount];

    ///////////////////////////////////////////////////////////////////
    //// public methods

    /** Create a new geometry object on the given figure and with the
     * given initial bounds.
     */
    public CircleGeometry(Figure figure, Rectangle2D bounds) {
        this._parentFigure = figure;
        setShape(bounds);
    }

    /** Get the single site with the given ID.
     */
    public Site getSite(int id) {
        if (_sites[id] == null) {
            _sites[id] = new CircleSite(id);
        }

        return _sites[id];
    }

    /** Get the minimum size of the rectangle.
     */
    public double getMinimumSize() {
        return _minSize;
    }

    /** Get the north site (only site).
     */
    public Site getN() {
        return getSite(SwingConstants.NORTH);
    }

    /** Get the figure to which this geometry object is attached.
     * Returns null if there isn't one.
     */
    @Override
    public Figure getFigure() {
        return _parentFigure;
    }

    /** Get the current shape that defines this geometry
     */
    @Override
    public Shape getShape() {
        return _rect;
    }

    /** Get the current rectangle that defines this geometry. This
     * returns the same shape as getShape(), but as a Rectangle2D type.
     */
    public Rectangle2D getBounds() {
        return _rect;
    }

    /** Set the minimum size of the rectangle. The default is 1.0.
     */
    public void setMinimumSize(double minimumSize) {
        _minSize = minimumSize;
    }

    /** Set the shape that defines this geometry object.
     * The shape must be a Rectangle2D, or an exception
     * will be thrown.
     */
    @Override
    public void setShape(Shape shape) {
        if (!(shape instanceof Rectangle2D)) {
            throw new IllegalArgumentException("Argument must be a Rectangle2D");
        }

        // Important: make a copy of it
        _rect = (Rectangle2D) ((Rectangle2D) shape).clone();
    }

    /** Set the rectangle that defines this geometry object.
     * This is the same as setShape(), but does not need to
     * perform the type check.
     */
    public void setBounds(Rectangle2D rect) {
        // Important: make a copy of it
        _rect = (Rectangle2D) rect.clone();
    }

    /** Return an iteration over the sites in this geometry object.
     */
    public Iterator sites() {
        return new Iterator() {
            // Note: SwingConstants start at 1!
            int cursor = 0;

            @Override
            public boolean hasNext() {
                return cursor < _siteCount;
            }

            @Override
            public Object next() throws NoSuchElementException {
                if (!hasNext()) {
                    throw new NoSuchElementException("Can't get " + cursor
                            + "'th element from CircleGeometry of size "
                            + _siteCount);
                }
                if (_sites[cursor] == null) {
                    _sites[cursor] = new CircleSite(cursor);
                }

                return _sites[cursor++];
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException(
                        "Site cannot be removed");
            }
        };
    }

    /** Translate the geometry object
     */
    @Override
    public void translate(double x, double y) {
        _rect.setFrame(_rect.getX() + x, _rect.getY() + y, _rect.getWidth(),
                _rect.getHeight());
    }

    ///////////////////////////////////////////////////////////////////
    //// CircleSite

    /** CircleSite is the local class that implements
     * an editable site of Rectangle objects.
     */
    public class CircleSite extends AbstractSite {
        // Its id
        private int _id;

        private double _normal;

        private double _offX;

        private double _offY;

        /** Create a new site with the given ID
         */
        CircleSite(int id) {
            _id = id;
            _normal = Math.PI / 2;
            _offX = 0;
            _offY = -_rect.getHeight() / 2;
        }

        /** Get the ID of this site.
         */
        @Override
        public int getID() {
            return _id;
        }

        /** Get the figure to which this site is attached, or null
         * if it is not attached to a figure.
         */
        @Override
        public Figure getFigure() {
            return _parentFigure;
        }

        /** Get the angle of the normal to this site, in radians
         * between zero and 2pi.
         */
        @Override
        public double getNormal() {
            return _normal;
        }

        /** Get the point location of the site.
         */
        @Override
        public Point2D getPoint() {
            return new Point2D.Double(getX(), getY());
        }

        /** Get the x-coordinate of the site, in the local
         * coordinates of the containing pane.
         */
        @Override
        public double getX() {
            return _rect.getCenterX() + _offX;
        }

        /** Get the y-coordinate of the site, in the local
         * coordinates of the containing pane.
         */
        @Override
        public double getY() {
            return _rect.getCenterY() + _offY;
        }

        /** Test if this site has a "normal" to it. Returns
         * true.
         */
        @Override
        public boolean hasNormal() {
            return true;
        }

        private final double getAngle(int direction) {
            double piOver4 = Math.PI / 4;

            switch (direction) {
            case SwingConstants.EAST:
                return 0.0;

            case SwingConstants.NORTH_EAST:
                return piOver4 * 7;

            case SwingConstants.NORTH:
                return piOver4 * 6;

            case SwingConstants.NORTH_WEST:
                return piOver4 * 5;

            case SwingConstants.WEST:
                return piOver4 * 4;

            case SwingConstants.SOUTH_WEST:
                return piOver4 * 3;

            case SwingConstants.SOUTH:
                return piOver4 * 2;

            case SwingConstants.SOUTH_EAST:
                return piOver4 * 1;
            }

            throw new IllegalArgumentException("Illegal direction: "
                    + direction);
        }

        /** Test if this site has a normal in the given direction.
         */
        @Override
        public boolean isNormal(int direction) {
            double theta1 = getAngle(direction);
            double theta2 = _normal < 0 ? _normal + 2 * Math.PI : _normal;
            // Don't compare floats with ==
            return Math.abs(theta1 - theta2) < 0.000001;
        }

        /** Translate the site by the indicated distance,
         * where distances are in the local coordinates of the
         * containing pane.
         */
        @Override
        public void translate(double dx, double dy) {
            if (Math.abs(_offX + dx) > _minSize) {
                _offX = _offX + dx;
            }

            if (Math.abs(_offY + dy) > _minSize) {
                _offY = _offY + dy;
            }

            double cx = _rect.getCenterX();
            double cy = _rect.getCenterY();
            double r = Math.sqrt(_offX * _offX + _offY * _offY);
            double theta = Math.atan(_offY / _offX);

            if (_offX < 0) {
                theta = theta + Math.PI;
            }

            _normal = theta;
            _rect.setFrameFromDiagonal(cx - r, cy - r, cx + r, cy + r);

            /*
             // Adjust the coordinates.
             double x1 = _rect.getX();
             double y1 = _rect.getY();
             double x2 = x1 + _rect.getWidth();
             double y2 = y1 + _rect.getHeight();

             x1 += y;
             y1 += y;
             x2 -= y;
             y2 -= y;

             // Check if below minimum allowable size.  If so, put the
             // coordinates back where they were.
             if (x2 - x1 < _minSize) {
             x1 -= y;
             x2 += y;
             }
             if (y2 - y1 < _minSize) {
             y1 -= y;
             y2 += y;
             }

             // Set the rectangle.
             _rect.setFrameFromDiagonal(x1,y1,x2,y2);
             */
        }

        /** Set the point location of the site
         */
        public void setPoint(Point2D point) {
            translate(point.getX() - getX(), point.getY() - getY());
        }
    }
}
