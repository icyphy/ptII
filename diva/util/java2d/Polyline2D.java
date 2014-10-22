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
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/** A polyline shape. This class implements <b>java.awt.Shape</b>, and
 * consists of a series of straight-line segments. This class
 * should be used instead of GeneralPath for shapes that consist
 * only of straight-line segments (and that are no closed). It is
 * more efficient than GeneralPath, and allows the coordinates of
 * vertices to be modified.
 *
 * Following the convention set by the Java2D shape classes,
 * the Polyline class is an abstract class, which contains
 * two concrete inner classes, one storing floats and one
 * storing doubles.
 *
 * @version        $Id$
 * @author         John Reekie
 */
public abstract class Polyline2D implements Shape {
    /** The current number of coordinates
     */
    protected int _coordCount = 0;

    /** Return false. A line never contains any point.
     */
    @Override
    public boolean contains(double x, double y) {
        return false;
    }

    /** Return false. A line never contains any point.
     */
    @Override
    public boolean contains(Point2D p) {
        return false;
    }

    /** Return false. A line never contains a rectangle.
     */
    @Override
    public boolean contains(Rectangle2D r) {
        return false;
    }

    /** Return false. A line never contains a rectangle.
     */
    @Override
    public boolean contains(double x, double y, double w, double h) {
        return false;
    }

    /** Get the integer bounds of the polyline.
     */
    @Override
    public Rectangle getBounds() {
        return getBounds2D().getBounds();
    }

    /** Get the floating-point bounds of the polyline.
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
        return new PolylineIterator(this, at);
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

    /** Test if the polyline is intersected by the given
     * rectangle.
     */
    @Override
    public boolean intersects(Rectangle2D r) {
        if (_coordCount == 0) {
            return false;
        } else if (_coordCount == 1) {
            return r.contains(getX(0), getY(0));
        }

        int count = this.getVertexCount();
        double x1;
        double y1;
        double x2 = getX(0);
        double y2 = getY(0);

        for (int i = 1; i < count; i++) {
            x1 = x2;
            y1 = y2;
            x2 = getX(i);
            y2 = getY(i);

            if (r.intersectsLine(x1, y1, x2, y2)) {
                return true;
            }
        }

        return false;
    }

    /** Test if the polyline is intersected by the given
     * rectangle.
     */
    @Override
    public boolean intersects(double x, double y, double w, double h) {
        return intersects(new Rectangle2D.Double(x, y, w, h));
    }

    /** Add a new vertex to the end of the line.
     */
    public abstract void lineTo(double x, double y);

    /** Move the start point of the vertex to the given position.
     * Throw an exception if the line already contains any vertices.
     */
    public abstract void moveTo(double x, double y);

    /** Reset the polyline back to empty.
     */
    public void reset() {
        _coordCount = 0;
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

    /** Return a string representing this object
     */
    @Override
    public String toString() {
        StringBuffer string = new StringBuffer(super.toString());

        for (int i = 0; i < this.getVertexCount(); i++) {
            string.append("(" + getX(i) + "," + getY(i) + "),");
        }

        return string.toString();
    }

    /** Transform the polyline with the given transform.
     */
    public abstract void transform(AffineTransform at);

    /** Translate the polyline the given distance.
     */
    public abstract void translate(double x, double y);

    ///////////////////////////////////////////////////////////////////
    //// Float

    /** The concrete Polyline class that stores coordinates internally
     * as floats.
     */
    public static class Float extends Polyline2D {
        /** The coordinates
         */
        float[] _coords = new float[4];

        /** Create a new polyline with no vertices.
         */
        public Float() {
            // empty
        }

        /** Create a new polyline with "initSize" number of vertices.
         */
        public Float(int initSize) {
            _coords = new float[initSize * 2];
        }

        /** Copy constructor for efficient copying of Polyline2D.
         */
        public Float(Polyline2D in) {
            _coordCount = in._coordCount;
            _coords = new float[in._coordCount];

            if (in instanceof Float) {
                Float floatIn = (Float) in;
                System.arraycopy(floatIn._coords, 0, _coords, 0, _coordCount);
            } else {
                Double doubleIn = (Double) in;

                for (int i = 0; i < _coordCount; i++) {
                    _coords[i] = (float) doubleIn._coords[i];
                }
            }
        }

        /** Create a new polyline with two vertices.
         */
        public Float(float x1, float y1, float x2, float y2) {
            _coords[0] = x1;
            _coords[1] = y1;
            _coords[2] = x2;
            _coords[3] = y2;
            _coordCount = 4;
        }

        /** Get the floating-point bounds of the polyline.
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
            if (index < 0 || index >= this.getVertexCount()) {
                throw new IndexOutOfBoundsException("Index: " + index
                        + ", Size: " + this.getVertexCount());
            }

            return _coords[index * 2];
        }

        /** Get the given Y-coordinate
         *
         * @exception IndexOutOfBoundsException The index is out of bounds.
         */
        @Override
        public double getY(int index) {
            if (index < 0 || index >= this.getVertexCount()) {
                throw new IndexOutOfBoundsException("Index: " + index
                        + ", Size: " + this.getVertexCount());
            }

            return _coords[index * 2 + 1];
        }

        /** Add a new vertex to the end of the line.
         */
        @Override
        public void lineTo(double x, double y) {
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
         * @exception UnsupportedOperationException The polyline already
         * has vertices
         */
        @Override
        public void moveTo(double x, double y) {
            if (_coordCount > 0) {
                throw new UnsupportedOperationException(
                        "This polyline already has vertices");
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
            if (index < 0 || index >= this.getVertexCount()) {
                throw new IndexOutOfBoundsException("Index: " + index
                        + ", Size: " + this.getVertexCount());
            }

            _coords[index * 2] = (float) x;
        }

        /** Set the given Y-coordinate
         *
         * @exception IndexOutOfBoundsException The index is out of bounds.
         */
        @Override
        public void setY(int index, double y) {
            if (index < 0 || index >= this.getVertexCount()) {
                throw new IndexOutOfBoundsException("Index: " + index
                        + ", Size: " + this.getVertexCount());
            }

            _coords[index * 2 + 1] = (float) y;
        }

        /** Transform the polyline with the given transform.
         */
        @Override
        public void transform(AffineTransform at) {
            at.transform(_coords, 0, _coords, 0, _coordCount / 2);
        }

        /** Translate the polyline the given distance.
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

    /** The concrete Polyline class that stores coordinates internally
     * as doubles.
     */
    public static class Double extends Polyline2D {
        /** The coordinates
         */
        double[] _coords = new double[4];

        /** Create a new polyline with no coordinates
         */
        public Double() {
            // empty
        }

        /** Create a new polyline with "initSize" number of vertices.
         */
        public Double(int initSize) {
            _coords = new double[initSize * 2];
        }

        /** Create a new polyline with two vertices.
         */
        public Double(double x1, double y1, double x2, double y2) {
            _coords[0] = x1;
            _coords[1] = y1;
            _coords[2] = x2;
            _coords[3] = y2;
            _coordCount = 4;
        }

        /** Copy constructor for efficient copying of Polyline2D.
         */
        public Double(Polyline2D in) {
            _coordCount = in._coordCount;
            _coords = new double[in._coordCount];

            if (in instanceof Double) {
                Double doubleIn = (Double) in;
                System.arraycopy(doubleIn._coords, 0, _coords, 0, _coordCount);
            } else {
                Float floatIn = (Float) in;

                for (int i = 0; i < _coordCount; i++) {
                    _coords[i] = floatIn._coords[i];
                }
            }
        }

        /** Get the floating-point bounds of the polyline.
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

        /** Get the given X-coordinate
         *
         * @exception IndexOutOfBoundsException The index is out of bounds.
         */
        @Override
        public double getX(int index) {
            if (index < 0 || index >= this.getVertexCount()) {
                throw new IndexOutOfBoundsException("Index: " + index
                        + ", Size: " + this.getVertexCount());
            }

            return _coords[index * 2];
        }

        /** Get the given Y-coordinate
         *
         * @exception IndexOutOfBoundsException The index is out of bounds.
         */
        @Override
        public double getY(int index) {
            if (index < 0 || index >= this.getVertexCount()) {
                throw new IndexOutOfBoundsException("Index: " + index
                        + ", Size: " + this.getVertexCount());
            }

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
         * @exception UnsupportedOperationException The polyline already
         * has vertices
         */
        @Override
        public void moveTo(double x, double y) {
            if (_coordCount > 0) {
                throw new UnsupportedOperationException(
                        "This polyline already has vertices");
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
            if (index < 0 || index >= this.getVertexCount()) {
                throw new IndexOutOfBoundsException("Index: " + index
                        + ", Size: " + this.getVertexCount());
            }

            _coords[index * 2] = x;
        }

        /** Set the given Y-coordinate
         *
         * @exception IndexOutOfBoundsException The index is out of bounds.
         */
        @Override
        public void setY(int index, double y) {
            if (index < 0 || index >= this.getVertexCount()) {
                throw new IndexOutOfBoundsException("Index: " + index
                        + ", Size: " + this.getVertexCount());
            }

            _coords[index * 2 + 1] = y;
        }

        /** Transform the polyline with the given transform.
         */
        @Override
        public void transform(AffineTransform at) {
            at.transform(_coords, 0, _coords, 0, _coordCount / 2);
        }

        /** Translate the polyline the given distance.
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
