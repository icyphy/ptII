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
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.NoSuchElementException;

import diva.canvas.AbstractSite;
import diva.canvas.Figure;
import diva.canvas.Site;

/** PathGeometry represents a path. Actually, any shape. Its
 * defining shape is any instance of Shape, although generally
 * this class is most useful for shapes that are instances of
 * GeneralPath, since any modification to any of the
 * will cause the shape returned by getShape() to be changed
 * into an instance of GeneralPath.
 *
 * <p>PathGeometry provides sites for the vertices
 * of the path, and for the control point for those segments
 * that are quadratic or cubic curves.
 *
 * @version        $Id$
 * @author         John Reekie
 * @author      Nick Zamora
 */
public class PathGeometry implements Geometry {
    ///////////////////////////////////////////////////////////////////
    //// private fields

    /** The figure to which the sites are attached
     */
    private Figure _parentFigure;

    /** The defining path
     */
    private Shape _path;

    /** The vertex sites. Other sites are referenced
     * by these sites
     */
    private Vertex[] _vertices;

    /** A flag that says whether the geometry arrays are up-to-date
     */
    private boolean _geometryValid = false;

    /** The number of vertices in the path
     */
    private int _vertexCount;

    /** The number of coordinates in the path
     */
    private int _coordCount;

    /** The array of coordinates of vertices and control points, in
     * the same format as for GeneralPath
     */
    private float[] _coordinate = new float[4];

    /** The array of vertex type, using the fields defined in GeneralPath
     * and returned by PathIterator.
     */
    private int[] _type = new int[2];

    /** The array of indexes into the coordinate array, indexed
     * by vertex number
     */
    private int[] _index = new int[2];

    /** Needed for getPathIterator
     */
    private AffineTransform _unitTransform = new AffineTransform();

    ///////////////////////////////////////////////////////////////////
    //// public methods

    /** Create a new geometry object on the given figure and with the
     * given initial shape.
     */
    public PathGeometry(Figure figure, Shape shape) {
        _parentFigure = figure;
        _path = shape;
    }

    /** Get the figure to which this geometry object is attached.
     * Returns null if there isn't one.
     */
    @Override
    public Figure getFigure() {
        return _parentFigure;
    }

    /** Get the shape that defines this geometry object. If any of
     * the sites have been translated since this shape was set, a
     * new shape will be produced and returned.
     */
    @Override
    public Shape getShape() {
        if (_path == null) {
            GeneralPath p = new GeneralPath(Path2D.WIND_NON_ZERO,
                    _vertexCount + 2);
            int c = 0;

            for (int i = 0; i < _vertexCount; i++) {
                switch (_type[i]) {
                case PathIterator.SEG_CLOSE:
                    p.closePath();
                    break;

                case PathIterator.SEG_MOVETO:
                    p.moveTo(_coordinate[c], _coordinate[c + 1]);
                    c += 2;
                    break;

                case PathIterator.SEG_LINETO:
                    p.lineTo(_coordinate[c], _coordinate[c + 1]);
                    c += 2;
                    break;

                case PathIterator.SEG_QUADTO:
                    p.quadTo(_coordinate[c], _coordinate[c + 1],
                            _coordinate[c + 2], _coordinate[c + 3]);
                    c += 4;
                    break;

                case PathIterator.SEG_CUBICTO:
                    p.curveTo(_coordinate[c], _coordinate[c + 1],
                            _coordinate[c + 2], _coordinate[c + 3],
                            _coordinate[c + 4], _coordinate[c + 5]);
                    c += 6;
                    break;
                }
            }

            _path = p;
        }

        return _path;
    }

    /** Get the site on the given vertex.
     */
    public Site getVertex(int number) {
        if (!_geometryValid) {
            updateGeometry();
        }

        if (_vertices[number] == null) {
            if (_type[number] == PathIterator.SEG_CLOSE) {
                _vertices[number] = new CloseSegment(number);
            } else {
                _vertices[number] = new Vertex(number);
            }
        }

        return _vertices[number];
    }

    /** Get the number of vertices of this shape. This number includes
     * all "Close" segments.
     */
    public int getVertexCount() {
        if (!_geometryValid) {
            updateGeometry();
        }

        return _vertexCount;
    }

    /** Set the shape that defines this geometry object.
     */
    @Override
    public void setShape(Shape shape) {
        _path = shape;
        invalidateGeometry();
    }

    /** Translate the geometry object
     */
    @Override
    public void translate(double x, double y) {
        if (!_geometryValid) {
            updateGeometry();
        }

        for (int i = 0; i < _coordCount;) {
            _coordinate[i++] += x;
            _coordinate[i++] += y;
        }
    }

    /** Return an iteration over the vertices in this geometry object.
     */
    public Iterator vertices() {
        if (!_geometryValid) {
            updateGeometry();
        }

        return new Iterator() {
            // cursor is the current place in the iteration
            int cursor = 0;

            // control_point is an internal counter to cursor, needed if the segment is quadratic or cubic.
            int control_point = 0;

            @Override
            public boolean hasNext() {
                return cursor < _vertexCount;
            }

            // Get the next Vertex
            @Override
            public Object next() throws NoSuchElementException {
                if (!hasNext()) {
                    throw new NoSuchElementException("Can't get " + cursor
                            + "'th element from PathyGeometry of size "
                            + _vertexCount);
                }
                // The first time through, getVertex() needs to be called

                if (_vertices[cursor] == null) {
                    getVertex(cursor);
                }

                // Depending on the type of segment
                switch (_type[cursor]) {
                // If a cubic curve, then make sure to include the two control points
                case PathIterator.SEG_CUBICTO:

                    if (control_point == 0) {
                        control_point = 1;
                        return new Vertex(cursor, control_point);
                    } else if (control_point == 1) {
                        control_point = 2;
                        return new Vertex(cursor, control_point);
                    } else {
                        control_point = 0;
                        return _vertices[cursor++];
                    }

                    // If a quadratic curve, then make sure to include the one control point
                case PathIterator.SEG_QUADTO:

                    if (control_point == 0) {
                        control_point = 1;
                        return new Vertex(cursor, control_point);
                    } else {
                        // Otherwise, there is no control points on the segment.
                        control_point = 0;
                        return _vertices[cursor++];
                    }

                default:
                    return _vertices[cursor++];
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException(
                        "Vertex sites cannot be removed");
            }
        };
    }

    ///////////////////////////////////////////////////////////////////
    //// Private methods

    /* Invalidate the geometry cache. The site
     * locations will be forced to recompute on next access. This
     * method is always called by setGeometryShape().
     */
    private void invalidateGeometry() {
        _geometryValid = false;
    }

    /* Update the geometry of the sites. The geometry will be
     * recomputed immediately. This should only be called after calling
     * invalidateGeometry.
     */
    private void updateGeometry() {
        _vertexCount = 0;
        _coordCount = 0;
        _vertices = null;

        // Iterate through the segments, constructing the internal data arrays
        float[] segment = new float[6];
        PathIterator i = _path.getPathIterator(_unitTransform);

        while (!i.isDone()) {
            // Stretch arrays if necessary
            if (_vertexCount == _type.length) {
                int[] temp1 = new int[_type.length * 2];
                int[] temp2 = new int[_type.length * 2];
                System.arraycopy(_type, 0, temp1, 0, _vertexCount);
                System.arraycopy(_index, 0, temp2, 0, _vertexCount);
                _type = temp1;
                _index = temp2;
            }

            if (_coordCount + 6 > _coordinate.length) {
                float[] temp = new float[_coordinate.length * 2];
                System.arraycopy(_coordinate, 0, temp, 0, _coordCount);
                _coordinate = temp;
            }

            // Get segment
            _index[_vertexCount] = _coordCount;
            _type[_vertexCount] = i.currentSegment(segment);

            switch (_type[_vertexCount]) {
            case PathIterator.SEG_MOVETO:
            case PathIterator.SEG_LINETO:
                _coordinate[_coordCount++] = segment[0];
                _coordinate[_coordCount++] = segment[1];
                break;

            case PathIterator.SEG_QUADTO:
                _coordinate[_coordCount++] = segment[0];
                _coordinate[_coordCount++] = segment[1];
                _coordinate[_coordCount++] = segment[2];
                _coordinate[_coordCount++] = segment[3];
                break;

            case PathIterator.SEG_CUBICTO:
                _coordinate[_coordCount++] = segment[0];
                _coordinate[_coordCount++] = segment[1];
                _coordinate[_coordCount++] = segment[2];
                _coordinate[_coordCount++] = segment[3];
                _coordinate[_coordCount++] = segment[4];
                _coordinate[_coordCount++] = segment[5];
                break;
            }

            _vertexCount++;
            i.next();
        }

        // Allocate data and set flags
        _vertices = new Vertex[_vertexCount];
        _geometryValid = true;
    }

    ///////////////////////////////////////////////////////////////////
    //// Vertex

    /** Vertex is the site that represents vertexes of the path.
     * Any object that has a reference to a Vertex is obliged
     * to lose it if the Vertex's id becomes larger than the
     * number of vertexes in the path. (If they don't, then accessing the
     * site may result in a out-of-bounds exception.) Clients must
     * also be aware
     * that the type of the vertex may also change if the shape changes,
     * so they should use the getType() method to get the type
     * of the vertex if necessary.
     */
    public class Vertex extends AbstractSite {
        // The vertex number
        private int _id;

        // Used to keep track of the control points in the quadratic and cubic curves.
        private int _controlPoint;

        /** Create a new site with the given ID
         */
        Vertex(int id) {
            this._id = id;
            _controlPoint = 0;
        }

        /** Create a new site with the given ID and control point
         */
        Vertex(int id, int control_point) {
            this._id = id;
            _controlPoint = control_point;
        }

        /** Get the ID of this site.
         */
        @Override
        public int getID() {
            return _id;
        }

        /** Get the control point of this site.
         */
        public int getControlPoint() {
            return _controlPoint;
        }

        /** Get the figure to which this site is attached, or null
         * if it is not attached to a figure.
         */
        @Override
        public Figure getFigure() {
            return _parentFigure;
        }

        /** Get the x-coordinate of the site, in the local
         * coordinates of the containing pane.
         */
        @Override
        public double getX() {
            if (!_geometryValid) {
                updateGeometry();
            }

            // If this vertex is not a control point of a segment, return the startpoint's x coordinate.
            if (_controlPoint == 0) {
                return _coordinate[_index[_id]];
            }
            // If this vertex is a control point of a segment, return that control point's x coordinate.
            else if (_controlPoint == 1) {
                return _coordinate[_index[_id] + 2];
            } else {
                return _coordinate[_index[_id] + 4];
            }
        }

        /** Get the y-coordinate of the site, in the local
         * coordinates of the containing pane.
         */
        @Override
        public double getY() {
            if (!_geometryValid) {
                updateGeometry();
            }

            // If this vertex is not a control point of a segment, return the startpoint's y coordinate.
            if (_controlPoint == 0) {
                return _coordinate[_index[_id] + 1];
            }
            // If this vertex is a control point of a segment, return that control point's y coordinate.
            else if (_controlPoint == 1) {
                return _coordinate[_index[_id] + 3];
            } else {
                return _coordinate[_index[_id] + 5];
            }
        }

        /** Set the point location of the site
         */
        public void setPoint(Point2D point) {
            translate(point.getX() - getX(), point.getY() - getY());
        }

        /** Translate the site by the indicated distance. If this
         * vertex is one end of a cubic curve, move the adjacent
         * control point or control points the same distance. If
         * this site is one end of a quadratic curve, move
         * the adjacent control points half of the distance.
         */
        @Override
        public void translate(double x, double y) {
            if (!_geometryValid) {
                updateGeometry();
            }

            int index = _index[_id];

            // Move it.  If this vertex is not a control point, move the startpoint.
            if (_controlPoint == 0) {
                _coordinate[index] += x;
                _coordinate[index + 1] += y;
            }
            // If this vertex is a control point, move that control point.
            else if (_controlPoint == 1) {
                _coordinate[index + 2] += x;
                _coordinate[index + 3] += y;
            } else {
                _coordinate[index + 4] += x;
                _coordinate[index + 5] += y;
            }

            _path = null;
        }

        /** Describe this site
         */
        @Override
        public String toString() {
            StringBuffer s = new StringBuffer(getClass().getName());
            s.append(": vertex " + _id + " of " + _vertexCount);
            s.append(", type ");

            switch (_type[_id]) {
            case PathIterator.SEG_CLOSE:
                s.append("close");
                break;

            case PathIterator.SEG_MOVETO:
                s.append("move");
                break;

            case PathIterator.SEG_LINETO:
                s.append("line");
                break;

            case PathIterator.SEG_QUADTO:
                s.append("quadratic");
                break;

            case PathIterator.SEG_CUBICTO:
                s.append("cubic");
                break;
            }

            return s.toString();
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// CloseSegment

    /** CloseSegment is a special type of Vertex, that closes a sub-path.
     * CloseSegments will be returned in the iterators over vertices, but
     * clients must be aware of this, and not (for example) create
     * grab-handles on them. The coordinates returned by a CloseSegment
     * will be the coordinates of the first vertex in that subpath, but
     * generally this should not be used as it is inefficient.
     */
    public class CloseSegment extends Vertex {
        /** Create a new close segment with the given ID
         */
        CloseSegment(int id) {
            super(id);
        }

        /** Get the x-coordinate of the site, in the local
         * coordinates of the containing pane
         */
        @Override
        public double getX() {
            if (!_geometryValid) {
                updateGeometry();
            }

            return _coordinate[_index[getID()]];
        }

        /** Get the y-coordinate of the site, in the local
         * coordinates of the containing pane.
         */
        @Override
        public double getY() {
            if (!_geometryValid) {
                updateGeometry();
            }

            return _coordinate[_index[getID()] + 1];
        }

        /** Set the point location of the site
         */
        @Override
        public void setPoint(Point2D point) {
            translate(point.getX() - getX(), point.getY() - getY());
        }

        /** Translate the site by the indicated distance. This
         * is an illegal operation for close segments and throws an
         * exception.
         */
        @Override
        public void translate(double x, double y) {
            throw new UnsupportedOperationException(
                    "Cannot translate close segments of a path");
        }
    }
}
