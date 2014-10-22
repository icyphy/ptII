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

/** BoundsGeometry is a class that provides support for manipulating
 * the bounds of a figure.
 *
 * @version        $Id$
 * @author         John Reekie
 */
public class BoundsGeometry implements Geometry {
    ///////////////////////////////////////////////////////////////////
    //// private fields

    /** The figure to which the sites are attached
     */
    private Figure _parentFigure;

    /** The minimum size of the rectangle
     */
    private double _minSize = 1.0;

    /** The overshoot of the x and y coordinates
     */
    private double _xOvershoot = 0;

    private double _yOvershoot = 0;

    /** The defining rectangle
     */
    private Rectangle2D _rect;

    /** The number of sites in the sites array.
     * Note: SwingConstants start at 1!
     */
    private static int _siteCount = 9;

    /** The sites that exist so far
     */
    private BoundsSite[] _sites = new BoundsSite[_siteCount];

    ///////////////////////////////////////////////////////////////////
    //// public methods

    /** Create a new geometry object on the given figure and with the
     * given initial bounds.
     */
    public BoundsGeometry(Figure figure, Rectangle2D bounds) {
        this._parentFigure = figure;
        setShape(bounds);
    }

    /** Get the single site with the given ID.
     */
    public Site getSite(int id) {
        if (_sites[id] == null) {
            _sites[id] = new BoundsSite(id);
        }

        return _sites[id];
    }

    /** Get the minimum size of the rectangle.
     */
    public double getMinimumSize() {
        return _minSize;
    }

    /** Get the north-east site.
     */
    public Site getNE() {
        return getSite(SwingConstants.NORTH_EAST);
    }

    /** Get the north-west site.
     */
    public Site getNW() {
        return getSite(SwingConstants.NORTH_WEST);
    }

    /** Get the south-east site.
     */
    public Site getSE() {
        return getSite(SwingConstants.SOUTH_EAST);
    }

    /** Get the south-west site.
     */
    public Site getSW() {
        return getSite(SwingConstants.SOUTH_WEST);
    }

    /** Get the north site.
     */
    public Site getN() {
        return getSite(SwingConstants.NORTH);
    }

    /** Get the south site.
     */
    public Site getS() {
        return getSite(SwingConstants.SOUTH);
    }

    /** Get the east site.
     */
    public Site getE() {
        return getSite(SwingConstants.EAST);
    }

    /** Get the west site.
     */
    public Site getW() {
        return getSite(SwingConstants.WEST);
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
            int cursor = 1;

            @Override
            public boolean hasNext() {
                return cursor < _siteCount;
            }

            @Override
            public Object next() throws NoSuchElementException {
                if (!hasNext()) {
                    throw new NoSuchElementException("Can't get " + cursor
                            + "'th element from BoundsGeometry of size "
                            + _siteCount);
                }
                if (_sites[cursor] == null) {
                    _sites[cursor] = new BoundsSite(cursor);
                }

                Site result = _sites[cursor];
                cursor++;
                return result;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException(
                        "Sites cannot be removed");
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
    //// BoundsSite

    /** BoundsSite is the local class that implements
     * editable sites of Rectangle objects.
     */
    public class BoundsSite extends AbstractSite {
        // Its id
        private int _id;

        // pi/4
        private double piOver4 = Math.PI / 4;

        /** Create a new site with the given ID
         */
        BoundsSite(int id) {
            this._id = id;
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
            switch (_id) {
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
            case SwingConstants.EAST:
            default:
                return 0.0;
            }

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
            switch (_id) {
            case SwingConstants.NORTH_WEST:
            case SwingConstants.SOUTH_WEST:
            case SwingConstants.WEST:
                return _rect.getX();

            case SwingConstants.NORTH_EAST:
            case SwingConstants.SOUTH_EAST:
            case SwingConstants.EAST:
                return _rect.getX() + _rect.getWidth();

            case SwingConstants.NORTH:
            case SwingConstants.SOUTH:
                return _rect.getX() + _rect.getWidth() / 2.0;
            }

            return 0.0;
        }

        /** Get the y-coordinate of the site, in the local
         * coordinates of the containing pane.
         */
        @Override
        public double getY() {
            switch (_id) {
            case SwingConstants.NORTH_WEST:
            case SwingConstants.NORTH_EAST:
            case SwingConstants.NORTH:
                return _rect.getY();

            case SwingConstants.SOUTH_WEST:
            case SwingConstants.SOUTH_EAST:
            case SwingConstants.SOUTH:
                return _rect.getY() + _rect.getHeight();

            case SwingConstants.EAST:
            case SwingConstants.WEST:
                return _rect.getY() + _rect.getHeight() / 2.0;
            }

            return 0.0;
        }

        /** Test if this site has a "normal" to it. Returns
         * true.
         */
        @Override
        public boolean hasNormal() {
            return true;
        }

        /** Test if this site has a normal in the given direction.
         */
        @Override
        public boolean isNormal(int direction) {
            return direction == _id;
        }

        /** Translate the site by the indicated distance,
         * where distances are in the local coordinates of the
         * containing pane.
         */
        @Override
        public void translate(double x, double y) {
            // Adjust the coordinates
            double x1 = _rect.getX();
            double y1 = _rect.getY();
            double x2 = x1 + _rect.getWidth();
            double y2 = y1 + _rect.getHeight();

            // Move the rectangle
            switch (_id) {
            case SwingConstants.NORTH_WEST:
                x1 += x;
                y1 += y;
                break;

            case SwingConstants.SOUTH_WEST:
                x1 += x;
                y2 += y;
                break;

            case SwingConstants.WEST:
                x1 += x;
                break;

            case SwingConstants.NORTH_EAST:
                x2 += x;
                y1 += y;
                break;

            case SwingConstants.SOUTH_EAST:
                x2 += x;
                y2 += y;
                break;

            case SwingConstants.EAST:
                x2 += x;
                break;

            case SwingConstants.NORTH:
                y1 += y;
                break;

            case SwingConstants.SOUTH:
                y2 += y;
                break;
            }

            // Constrain x
            switch (_id) {
            case SwingConstants.NORTH_EAST:
            case SwingConstants.SOUTH_EAST:
            case SwingConstants.EAST:

                if (x2 < x1 + _minSize || _xOvershoot < 0) {
                    _xOvershoot += x - (x1 + _minSize - (x2 - x));
                    x2 = x1 + _minSize;
                } else {
                    _xOvershoot = 0;
                }

                break;

            case SwingConstants.NORTH_WEST:
            case SwingConstants.SOUTH_WEST:
            case SwingConstants.WEST:

                if (x1 > x2 - _minSize || _xOvershoot < 0) {
                    _xOvershoot += x - (x1 + _minSize - (x2 - x));
                    x1 = x2 - _minSize;
                } else {
                    _xOvershoot = 0;
                }

                break;
            }

            // Constrain y
            switch (_id) {
            case SwingConstants.NORTH_EAST:
            case SwingConstants.NORTH_WEST:
            case SwingConstants.NORTH:

                if (y1 > y2 - _minSize || _yOvershoot < 0) {
                    _yOvershoot += y - (y1 + _minSize - (y2 - y));
                    y1 = y2 - _minSize;
                } else {
                    _yOvershoot = 0;
                }

                break;

            case SwingConstants.SOUTH_EAST:
            case SwingConstants.SOUTH_WEST:
            case SwingConstants.SOUTH:

                if (y2 < y1 + _minSize || _yOvershoot < 0) {
                    _yOvershoot += y - (y1 + _minSize - (y2 - y));
                    y2 = y1 + _minSize;
                } else {
                    _yOvershoot = 0;
                }

                break;
            }

            // Set the rectangle
            _rect.setFrameFromDiagonal(x1, y1, x2, y2);
        }

        /** Set the point location of the site
         */
        public void setPoint(Point2D point) {
            translate(point.getX() - getX(), point.getY() - getY());
        }
    }
}
