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
package diva.util.java2d;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/** A polygon shape. This class implements <b>java.awt.Shape</b>, and
 * consists of a series of straight-line segments. This class
 * should be used instead of GeneralPath for shapes that consist
 * only of straight-line segments and are always closed. It is
 * more efficient than GeneralPath, and allows the coordinates of
 * vertices to be modified.
 *
 * Following the convention set by the Java2D shape classes,
 * the Polygon class is an abstract class, which contains
 * two concrete inner classes, one storing floats and one
 * storing doubles.
 *
 * @version        $Id$
 * @author         John Reekie, Contributor: Billy Hinners
 */
public abstract class Polygon2D implements Shape {
    /** The current number of coordinates
     */
    protected int _coordCount = 0;

    /** The flag that says the the polygon has been closed
     */
    protected boolean _closed = false;

    /** Close the polygon. No further segments can be added.
     * If this method not called, then the path iterators will
     * treat the polygon as thought it were closed, and implicitly
     * join the most recently added vertex to the first one. However,
     * this method should generally be called, as if the last vertex
     * is the same as the first vertex, then it merges them.
     */
    public void closePath() {
        if (getX(getVertexCount() - 1) == getX(0)
                && getY(getVertexCount() - 1) == getY(0)) {
            _coordCount -= 2;
        }

        _closed = true;
    }

    /** Return true if the given point is inside the polygon.
     */
    @Override
    public boolean contains(double x, double y) {
        // This method creates a java.awt.geom.Area and calls contains()
        // on it.
        // The previous implementation failed if the polygon had a vertical or horizontal
        // edge with the same value as one of the arguments.
        // See diva/util/java2d/test/junit/Polygon2DJUnitTest.java
        Path2D path = this instanceof Polygon2D.Float ? new Path2D.Float()
                : new Path2D.Double();
        path.moveTo(getX(0), getY(0));
        for (int i = 1; i < getVertexCount(); i++) {
            path.lineTo(getX(i), getY(i));
        }
        path.closePath();
        Area area = new Area(path);
        return area.contains(x, y);
    }

    /** Return true if the given point is inside the polygon.
     */
    @Override
    public boolean contains(Point2D p) {
        return contains(p.getX(), p.getY());
    }

    /** Return true if the given rectangle is entirely inside
     * the polygon. (Currently, this algorithm can be fooled by
     * supplying a rectangle that has all four corners inside the
     * polygon, but which intersects some edges.)
     */
    @Override
    public boolean contains(Rectangle2D r) {
        return contains(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    /** Return true if the given rectangle is entirely inside
     * the polygon. (Currently, this algorithm can be fooled by
     * supplying a rectangle that has all four corners inside the
     * polygon, but which intersects some edges.)
     */
    @Override
    public boolean contains(double x1, double y1, double w, double h) {
        double x2 = x1 + w;
        double y2 = y1 + h;
        return contains(x1, y1) && contains(x1, y2) && contains(x2, y1)
                && contains(x2, y2);
    }

    /** Get the integer bounds of the polygon.
     */
    @Override
    public Rectangle getBounds() {
        return getBounds2D().getBounds();
    }

    /** Get the floating-point bounds of the polygon.
     */
    @Override
    public abstract Rectangle2D getBounds2D();

    /** Get a path iterator over the object.
     */
    @Override
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return getPathIterator(at);
    }

    /** Get a path iterator over the object.
     */
    @Override
    public PathIterator getPathIterator(AffineTransform at) {
        return new PolygonIterator(this, at);
    }

    /** Get the number of vertices
     */
    public int getVertexCount() {
        return _coordCount / 2;
    }

    /** Get the given X-coordinate
     *
     * @exception IndexOutOfBoundsException The index is out of bounds.
     */
    public abstract double getX(int index);

    /** Get the given Y-coordinate
     *
     * @exception IndexOutOfBoundsException The index is out of bounds.
     */
    public abstract double getY(int index);

    /** Test if the polygon is intersected by the given
     * rectangle. (Currently, this algorithm can be fooled by
     * supplying a rectangle that has no corners inside the
     * polygon, and does not contain any vertex of the polygon,
     * but which intersects some edges.)
     */
    @Override
    public boolean intersects(Rectangle2D r) {
        return intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    /** Test if the polygon is intersected by the given
     * rectangle.
     */
    @Override
    public boolean intersects(double x1, double y1, double w, double h) {
        double x2 = x1 + w;
        double y2 = y1 + h;

        if (_coordCount == 0) {
            return false;
        }

        // If the bounds don't intersect, then return false.
        Rectangle2D rect = new Rectangle2D.Double(x1, y1, w, h);

        if (!getBounds().intersects(rect)) {
            return false;
        }

        // return true if the polygon contains any vertex of the rectangle.
        if (contains(x1, y1) || contains(x1, y2) || contains(x2, y1)
                || contains(x2, y2)) {
            return true;
        }

        // return true if the rectangle contains any vertex of the polygon
        for (int i = 0; i < getVertexCount(); i++) {
            if (rect.contains(getX(i), getY(i))) {
                return true;
            }
        }

        // return true if any line segment of the polygon crosses a line
        // segment of the rectangle.
        // This is rather long, I wonder if it could be optimized.
        // Iterate over all vertices
        for (int i = 1; i < getVertexCount(); i++) {
            double vx1 = getX(i - 1);
            double vx2 = getX(i);
            double vy1 = getY(i - 1);
            double vy2 = getY(i);

            if (Line2D.linesIntersect(x1, y1, x1, y2, vx1, vy1, vx2, vy2)) {
                return true;
            }

            if (Line2D.linesIntersect(x1, y2, x2, y2, vx1, vy1, vx2, vy2)) {
                return true;
            }

            if (Line2D.linesIntersect(x2, y2, x2, y1, vx1, vy1, vx2, vy2)) {
                return true;
            }

            if (Line2D.linesIntersect(x2, y1, x1, y1, vx1, vy1, vx2, vy2)) {
                return true;
            }
        }

        return false;
    }

    /** Add a new vertex to the end of the polygon.
     * Throw an exception of the polygon has already been closed.
     */
    public abstract void lineTo(double x, double y);

    /** Move the start point of the vertex to the given position.
     * Throw an exception if the line already contains any vertices.
     */
    public abstract void moveTo(double x, double y);

    /** Reset the polygon back to empty.
     */
    public void reset() {
        _coordCount = 0;
        _closed = false;
    }

    /** Set the given X-coordinate.
     *
     * @exception IndexOutOfBoundsException The index is out of bounds.
     */
    public abstract void setX(int index, double x);

    /** Set the given Y-coordinate
     *
     * @exception IndexOutOfBoundsException The index is out of bounds.
     */
    public abstract void setY(int index, double y);

    /** Transform the polygon with the given transform.
     */
    public abstract void transform(AffineTransform at);

    /** Translate the polygon the given distance.
     */
    public abstract void translate(double x, double y);

    /** Return a string representation of the polygon.
     */
    @Override
    public String toString() {
        StringBuffer out = new StringBuffer(getClass().getName() + "[\n");

        for (int i = 0; i < getVertexCount(); i++) {
            out.append("\t" + getX(i) + ", " + getY(i) + "\n");
        }

        out.append("]");
        return out.toString();
    }

    ///////////////////////////////////////////////////////////////////
    //// Float

    /** The concrete Polygon class that stores coordinates internally
     * as floats.
     */
    public static class Float extends Polygon2D {
        /** The coordinates
         */
        float[] _coords;

        /** Create a new polygon with space for the
         * given number of vertices.
         */
        public Float(int size) {
            _coords = new float[2 * size];
        }

        /** Create a new polygon with space for the
         * given number of vertices.
         */
        public Float(float[] coords) {
            _coords = coords;
            _coordCount = coords.length;
        }

        /** Create a new polygon with no vertices.
         */
        public Float() {
            this(2);
        }

        /** Create a new polygon with a single start point
         */
        public Float(float x, float y) {
            this(2);
            _coords[0] = x;
            _coords[1] = y;
            _coordCount = 2;
        }

        /** Get the floating-point bounds of the polygon.
         */
        @Override
        public Rectangle2D getBounds2D() {
            if (_coordCount <= 1) {
                return new Rectangle2D.Float();
            }

            float x1 = _coords[0];
            float y1 = _coords[1];
            float x2 = x1;
            float y2 = y1;

            for (int i = 2; i < _coordCount;) {
                if (_coords[i] < x1) {
                    x1 = _coords[i];
                } else if (_coords[i] > x2) {
                    x2 = _coords[i];
                }

                i++;

                if (_coords[i] < y1) {
                    y1 = _coords[i];
                } else if (_coords[i] > y2) {
                    y2 = _coords[i];
                }

                i++;
            }

            return new Rectangle2D.Float(x1, y1, x2 - x1, y2 - y1);
        }

        /** Get the given X-coordinate
         *
         * @exception IndexOutOfBoundsException The index is out of bounds.
         */
        @Override
        public double getX(int index) {
            return _coords[index * 2];
        }

        /** Get the given Y-coordinate
         *
         * @exception IndexOutOfBoundsException The index is out of bounds.
         */
        @Override
        public double getY(int index) {
            return _coords[index * 2 + 1];
        }

        /** Add a new vertex to the end of the line.
         */
        @Override
        public void lineTo(double x, double y) {
            if (_closed) {
                throw new UnsupportedOperationException(
                        "This polygon has already been closed");
            }

            if (_coordCount == _coords.length) {
                float[] temp = new float[_coordCount * 2];
                System.arraycopy(_coords, 0, temp, 0, _coordCount);
                _coords = temp;
            }

            _coords[_coordCount++] = (float) x;
            _coords[_coordCount++] = (float) y;
        }

        /** Move the start point of the vertex to the given position.
         *
         * @exception UnsupportedOperationException The polygon already
         * has vertices
         */
        @Override
        public void moveTo(double x, double y) {
            if (_coordCount > 0) {
                throw new UnsupportedOperationException(
                        "This polygon already has vertices");
            }

            _coords[0] = (float) x;
            _coords[1] = (float) y;
            _coordCount = 2;
        }

        /** Set the given X-coordinate.
         *
         * @exception IndexOutOfBoundsException The index is out of bounds.
         */
        @Override
        public void setX(int index, double x) {
            _coords[index * 2] = (float) x;
        }

        /** Set the given Y-coordinate
         *
         * @exception IndexOutOfBoundsException The index is out of bounds.
         */
        @Override
        public void setY(int index, double y) {
            _coords[index * 2 + 1] = (float) y;
        }

        /** Transform the polygon with the given transform.
         */
        @Override
        public void transform(AffineTransform at) {
            at.transform(_coords, 0, _coords, 0, _coordCount / 2);
        }

        /** Translate the polygon the given distance.
         */
        @Override
        public void translate(double x, double y) {
            float fx = (float) x;
            float fy = (float) y;

            for (int i = 0; i < _coordCount;) {
                _coords[i++] += fx;
                _coords[i++] += fy;
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// Double

    /** The concrete Polygon class that stores coordinates internally
     * as doubles.
     */
    public static class Double extends Polygon2D {
        /** The coordinates
         */
        double[] _coords;

        /** Create a new polygon with space for the
         * given number of vertices.
         */
        public Double(int size) {
            _coords = new double[2 * size];
        }

        /** Create a new polygon with the
         * given vertices, in the format
         * [x0, y0, x1, y1, ... ].
         */
        public Double(double[] coords) {
            _coords = coords;
            _coordCount = coords.length;
        }

        /** Create a new polygon with no coordinates
         */
        public Double() {
            this(2);
        }

        /** Create a new polygon with a single start point
         */
        public Double(double x, double y) {
            this(2);
            _coords[0] = x;
            _coords[1] = y;
            _coordCount = 2;
        }

        /** Get the floating-point bounds of the polygon.
         */
        @Override
        public Rectangle2D getBounds2D() {
            if (_coordCount <= 0) {
                return new Rectangle2D.Double();
            }

            double x1 = _coords[0];
            double y1 = _coords[1];
            double x2 = x1;
            double y2 = y1;

            for (int i = 2; i < _coordCount;) {
                if (_coords[i] < x1) {
                    x1 = _coords[i];
                } else if (_coords[i] > x2) {
                    x2 = _coords[i];
                }

                i++;

                if (_coords[i] < y1) {
                    y1 = _coords[i];
                } else if (_coords[i] > y2) {
                    y2 = _coords[i];
                }

                i++;
            }

            return new Rectangle2D.Double(x1, y1, x2 - x1, y2 - y1);
        }

        /** Get the number of vertices
         */
        @Override
        public int getVertexCount() {
            return _coordCount / 2;
        }

        /** Get the given X-coordinate
         *
         * @exception IndexOutOfBoundsException The index is out of bounds.
         */
        @Override
        public double getX(int index) {
            return _coords[index * 2];
        }

        /** Get the given Y-coordinate
         *
         * @exception IndexOutOfBoundsException The index is out of bounds.
         */
        @Override
        public double getY(int index) {
            return _coords[index * 2 + 1];
        }

        /** Add a new vertex to the end of the line.
         */
        @Override
        public void lineTo(double x, double y) {
            if (_coordCount == _coords.length) {
                double[] temp = new double[_coordCount * 2];
                System.arraycopy(_coords, 0, temp, 0, _coordCount);
                _coords = temp;
            }

            _coords[_coordCount++] = x;
            _coords[_coordCount++] = y;
        }

        /** Move the start point of the vertex to the given position.
         *
         * @exception UnsupportedOperationException The polygon already
         * has vertices
         */
        @Override
        public void moveTo(double x, double y) {
            if (_coordCount > 0) {
                throw new UnsupportedOperationException(
                        "This polygon already has vertices");
            }

            _coords[0] = x;
            _coords[1] = y;
            _coordCount = 2;
        }

        /** Set the given X-coordinate.
         *
         * @exception IndexOutOfBoundsException The index is out of bounds.
         */
        @Override
        public void setX(int index, double x) {
            _coords[index * 2] = x;
        }

        /** Set the given Y-coordinate
         *
         * @exception IndexOutOfBoundsException The index is out of bounds.
         */
        @Override
        public void setY(int index, double y) {
            _coords[index * 2 + 1] = y;
        }

        /** Transform the polygon with the given transform.
         */
        @Override
        public void transform(AffineTransform at) {
            at.transform(_coords, 0, _coords, 0, _coordCount / 2);
        }

        /** Translate the polygon the given distance.
         */
        @Override
        public void translate(double x, double y) {
            for (int i = 0; i < _coordCount;) {
                _coords[i++] += x;
                _coords[i++] += y;
            }
        }
    }
}
