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

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.Iterator;

import javax.swing.SwingConstants;

import diva.util.Filter;
import diva.util.FilteredIterator;
import diva.util.java2d.ShapeUtilities;

/** A collection of canvas utilities. These utilities perform
 * useful functions related to the structural aspects of diva.canvas
 * that do not properly belong in any one class. Some of them
 * perform utility geometric functions that are not available
 * in the Java 2D API, while others accept iterators over Figures
 * or Shapes and compute a useful composite result.
 *
 * @version $Id$
 * @author John Reekie
 * @Pt.AcceptedRating Red
 */
public final class CanvasUtilities {
    ///////////////////////////////////////////////////////////////////
    //// Constants used by transform()
    private final static int m00 = 0;

    //private final static int m10 = 1;

    //private final static int m01 = 2;

    private final static int m11 = 3;

    private final static int m02 = 4;

    private final static int m12 = 5;

    /** double representation of WEST: 180 degrees (PI) **/
    public static final double WEST = Math.PI;

    /** double representation of NORTH: -90 degrees (-PI/2) **/
    public static final double NORTH = -Math.PI / 2;

    /** double representation of EAST: 0 degrees **/
    public static final double EAST = 0;

    /** double representation of SOUTH: 90 degrees (PI/2) **/
    public static final double SOUTH = Math.PI / 2;

    /** double representation of NORTHWEST: -135 degrees (-PI*3/4) **/
    public static final double NORTHWEST = -Math.PI * 3 / 4;

    /** double representation of NORTHEAST: -45 degrees (PI/4) **/
    public static final double NORTHEAST = -Math.PI / 4;

    /** double representation of SOUTHWEST: 135 degrees (PI*3/4) **/
    public static final double SOUTHWEST = Math.PI * 3 / 4;

    /** double representation of SOUTHEAST: 45 degrees (PI/4) **/
    public static final double SOUTHEAST = Math.PI / 4;

    /** Cannot instantiate
     */
    private CanvasUtilities() {
    }

    /** Clone a shape. This method is needed because Shape by itself
     * does not define clone(), although many (all?) shape instances
     * do.
     *
     * @deprecated Use ShapeUtilities.cloneShape() instead
     */
    @Deprecated
    public static Shape clone(Shape s) {
        // FIXME Add more specific shapes
        if (s instanceof RectangularShape) {
            return (RectangularShape) ((RectangularShape) s).clone();
        } else {
            return new GeneralPath(s);
        }
    }

    /** Compute a composite shape. The iterator must contain figures,
     * from which the shapes are obtained and joined into a
     * more complex shape. If the iterator is empty, return
     * a very small rectangle.
     */
    public static Shape computeCompositeShape(Iterator i) {
        if (!i.hasNext()) {
            return new Rectangle2D.Double();
        }

        // Start the union with the shape of the first child
        Figure f = (Figure) i.next();
        GeneralPath shape = new GeneralPath(f.getShape());

        // Scan the rest of the children and take the union
        while (i.hasNext()) {
            f = (Figure) i.next();
            shape.append(f.getShape(), false); // Don't connect
        }

        return shape;
    }

    /** Compute the bounding box of a set of connectors. The iterator
     * must contain connectors.
     */
    public static Rectangle2D computeSiteBounds(Iterator i) {
        double x1;
        double y1;
        double x2;
        double y2;

        Site f = (Site) i.next();
        x1 = x2 = f.getX();
        y1 = y2 = f.getY();

        while (i.hasNext()) {
            f = (Site) i.next();

            double x = f.getX();
            double y = f.getY();

            if (x < x1) {
                x1 = x;
            }

            if (x > x2) {
                x2 = x;
            }

            if (y < y1) {
                y1 = y;
            }

            if (y > y2) {
                y2 = y;
            }
        }

        return new Rectangle2D.Double(x1, y1, x2 - x1, y2 - y1);
    }

    /** Compute a composite bounding box. The iterator must contain
     * figures, from which the bounding boxes are obtained and joined
     * into a more complex shape. If the iterator is empty, return
     * a very small rectangle.
     */
    public static Rectangle2D computeCompositeBounds(Iterator i) {
        if (!i.hasNext()) {
            return new Rectangle2D.Double();
        }

        // Get a copy of the bounds of the first child
        Figure f = (Figure) i.next();
        // Deal with the possibility that the figure may not be visible.
        while (!f.isVisible()) {
            if (!i.hasNext()) {
                return new Rectangle2D.Double();
            }
            f = (Figure) i.next();
        }
        Rectangle2D b = f.getBounds();
        Rectangle2D bounds = new Rectangle2D.Double(b.getX(), b.getY(),
                b.getWidth(), b.getHeight());

        // Scan the rest of the children and take the union
        while (i.hasNext()) {
            f = (Figure) i.next();
            if (!f.isVisible()) {
                continue;
            }
            Rectangle2D.union(bounds, f.getBounds(), bounds);
        }

        return bounds;
    }

    /** Get the transform that will make the first
     * rectangle change into the second.
     */
    public static AffineTransform computeTransform(RectangularShape r,
            RectangularShape s) {
        AffineTransform at = new AffineTransform();
        at.translate(s.getX(), s.getY());
        at.scale(s.getWidth() / r.getWidth(), s.getHeight() / r.getHeight());
        at.translate(-r.getX(), -r.getY());
        return at;
    }

    /** Get the transform that will make the first
     * rectangle change fit within the second, while preserving the shape.
     */
    public static AffineTransform computeFitTransform(RectangularShape r,
            RectangularShape s) {
        AffineTransform at = new AffineTransform();
        at.translate(s.getX(), s.getY());

        double scaleX = s.getWidth() / r.getWidth();
        double scaleY = s.getHeight() / r.getHeight();
        double scale = Math.min(scaleX, scaleY);
        at.scale(scale, scale);
        at.translate(-r.getX(), -r.getY());
        return at;
    }

    /** Return the point at the center of a Rectangle.
     */
    public static Point2D getCenterPoint(Rectangle2D r) {
        return new Point2D.Double(r.getCenterX(), r.getCenterY());
    }

    /** Return the point at the center of a figure. This is
     * simple but so common it's worth having a method for it.
     */
    public static Point2D getCenterPoint(Figure f) {
        return getCenterPoint(f.getBounds());
    }

    /** Return the point at the center of a figure, in the
     * given transform context. This is
     * simple but so common it's worth having a method for it.
     */
    public static Point2D getCenterPoint(Figure f, TransformContext root) {
        Point2D p = getCenterPoint(f.getBounds());
        return transformInto(p, f.getParent().getTransformContext(), root);
    }

    /** Return the closest direction from SwingConstants, based on the
     *  given angle.    West corresponds to 0 degrees and south is PI/2.
     */
    public static int getDirection(double angle) {
        angle = moduloAngle(angle);

        if (angle < -Math.PI * 7 / 8) {
            return SwingConstants.WEST;
        } else if (angle < -Math.PI * 5 / 8) {
            return SwingConstants.NORTH_WEST;
        } else if (angle < -Math.PI * 3 / 8) {
            return SwingConstants.NORTH;
        } else if (angle < -Math.PI * 1 / 8) {
            return SwingConstants.NORTH_EAST;
        } else if (angle < Math.PI * 1 / 8) {
            return SwingConstants.EAST;
        } else if (angle < Math.PI * 3 / 8) {
            return SwingConstants.SOUTH_EAST;
        } else if (angle < Math.PI * 5 / 8) {
            return SwingConstants.SOUTH;
        } else if (angle < Math.PI * 7 / 8) {
            return SwingConstants.SOUTH_WEST;
        } else {
            return SwingConstants.WEST;
        }
    }

    /** Return an angle in radians, given a direction from SwingConstants.
     *  West corresponds to 0 degrees and south is PI/2.  The angle returned
     *  is between -PI and PI as per the normalizeAngle method.
     */
    public static double getNormal(int direction) {
        if (direction == SwingConstants.EAST) {
            return EAST;
        }

        if (direction == SwingConstants.NORTH) {
            return NORTH;
        }

        if (direction == SwingConstants.WEST) {
            return WEST;
        }

        if (direction == SwingConstants.SOUTH) {
            return SOUTH;
        }

        if (direction == SwingConstants.NORTH_EAST) {
            return NORTHEAST;
        }

        if (direction == SwingConstants.NORTH_WEST) {
            return NORTHWEST;
        }

        if (direction == SwingConstants.SOUTH_EAST) {
            return SOUTHEAST;
        }

        if (direction == SwingConstants.SOUTH_WEST) {
            return SOUTHWEST;
        }

        throw new RuntimeException("Unknown Direction");
    }

    /** Get the point on the given rectangular shape indicated by the location
     * flag. This flag must be one of those defined in javax.swing.Constants.
     */
    public static Point2D getLocation(Rectangle2D r, int location) {
        double x;
        double y;

        switch (location) {
        case SwingConstants.CENTER:
            x = r.getCenterX();
            y = r.getCenterY();
            break;

        case SwingConstants.NORTH:
            x = r.getCenterX();
            y = r.getMinY();
            break;

        case SwingConstants.NORTH_EAST:
            x = r.getMaxX();
            y = r.getMinY();
            break;

        case SwingConstants.EAST:
            x = r.getMaxX();
            y = r.getCenterY();
            break;

        case SwingConstants.SOUTH_EAST:
            x = r.getMaxX();
            y = r.getMaxY();
            break;

        case SwingConstants.SOUTH:
            x = r.getCenterX();
            y = r.getMaxY();
            break;

        case SwingConstants.SOUTH_WEST:
            x = r.getMinX();
            y = r.getMaxY();
            break;

        case SwingConstants.WEST:
            x = r.getMinX();
            y = r.getCenterY();
            break;

        case SwingConstants.NORTH_WEST:
            x = r.getMinX();
            y = r.getMinY();
            break;

        default:
            throw new IllegalArgumentException("Unknown location constant: "
                    + location);
        }

        return new Point2D.Double(x, y);
    }

    /** Return true if the given transform maps a rectangle
     * to a rectangle. If this method returns true, then passing
     * a rectangle to transform() is guaranteed to return a rectangle.
     */
    public static boolean isOrthogonal(AffineTransform at) {
        int t = at.getType();

        // FIXME: should be:
        //return (t &
        //            ( AffineTransform.TYPE_GENERAL_ROTATION
        //            | AffineTransform.TYPE_GENERAL_TRANSFORM)) == 0;
        return (t & (AffineTransform.TYPE_MASK_ROTATION | AffineTransform.TYPE_GENERAL_TRANSFORM)) == 0;
    }

    /** Return the angle between -PI and PI that corresponds to the
     *  given angle.
     */
    public static double moduloAngle(double angle) {
        while (angle > Math.PI) {
            angle -= 2 * Math.PI;
        }

        while (angle < -Math.PI) {
            angle += 2 * Math.PI;
        }

        return angle;
    }

    /** Return the first figure that is hit by the given region.
     * The figures tested are contained in the given iterator, which
     * must contain only figures. And figure that is also a FigureContainer
     * is descended into recursively; other figures are simply tested
     * using their hit() method.
     *
     * @param i An iterator over figures
     * @param region A rectangle which represents the pick or hit region
     * @return The topmost descendent under the pick region, or null
     * there isn't one.
     */
    public static Figure pick(Iterator i, Rectangle2D region) {
        Figure f;
        Figure picked;

        while (i.hasNext()) {
            f = (Figure) i.next();

            if (f instanceof FigureContainer) {
                picked = ((FigureContainer) f).pick(region);

                if (picked != null) {
                    return picked;
                }
            }

            if (f.hit(region)) {
                return f;
            }
        }

        return null;
    }

    /** Return the first figure that is hit by the given region.
     * The figures tested are contained in the given iterator, which
     * must contain only figures. And figure that is also a FigureContainer
     * is descended into recursively; other figures are simply tested
     * using their hit() method.
     *
     * @param i An iterator over figures
     * @param region A rectangle which represents the pick or hit region
     * @return The topmost descendent under the pick region, or null
     * there isn't one.
     */
    public static Figure pick(Iterator i, Rectangle2D region, Filter filter) {
        Figure f;
        Figure picked;

        while (i.hasNext()) {
            f = (Figure) i.next();

            if (f instanceof FigureContainer) {
                picked = ((FigureContainer) f).pick(region, filter);

                if (picked != null) {
                    return picked;
                }
            }

            if (f.hit(region) && filter.accept(f)) {
                return f;
            }
        }

        return null;
    }

    /**
     * Return an iterator over the figures hit by the given region.
     * The figures tested are contained in the given iterator, which
     * must contain only figures. And figure that is also a FigureContainer
     * is descended into recursively; other figures are simply tested
     * using their hit() method.
     *
     * XXX how to do this?
     *
     * @param i An iterator over figures
     * @param region A rectangle which represents the pick or hit region
     * @return An iterator over the hit figures.
     */
    public static Iterator pickIter(Iterator i, Rectangle2D region) {
        final Rectangle2D rl = region;

        return new FilteredIterator(i, new Filter() {
            Rectangle2D _region = rl;

            @Override
            public boolean accept(Object o) {
                Figure f = (Figure) o;
                return f.hit(_region);
            }
        });
    }

    /** Reverse a direction flag. The flag must one of the eight
     * compass directions defined in SwingConstants. Return the flag
     * that represents the opposite direction.
     */
    public static int reverseDirection(int direction) {
        switch (direction) {
        case SwingConstants.CENTER:
            return SwingConstants.CENTER;

        case SwingConstants.NORTH:
            return SwingConstants.SOUTH;

        case SwingConstants.NORTH_EAST:
            return SwingConstants.SOUTH_WEST;

        case SwingConstants.EAST:
            return SwingConstants.WEST;

        case SwingConstants.SOUTH_EAST:
            return SwingConstants.NORTH_WEST;

        case SwingConstants.SOUTH:
            return SwingConstants.NORTH;

        case SwingConstants.SOUTH_WEST:
            return SwingConstants.NORTH_EAST;

        case SwingConstants.WEST:
            return SwingConstants.EAST;

        case SwingConstants.NORTH_WEST:
            return SwingConstants.SOUTH_EAST;

        default:
            throw new IllegalArgumentException("Unknown direction constant: "
                    + direction);
        }
    }

    // FIXME FIXME
    private static boolean _transformRectangularShapeIsBroken = true;

    /** Transform a shape with the supplied transform. If the shape is
     * an instance of RectangularShape, then the transformation will
     * modify that shape and return it if it is possible to do so.
     * Otherwise, AffineTransform.createTransformedShape() is used
     * to create a new shape, which is then returned.
     *
     * @deprecated Use diva.util.java2d.ShapeUtilities.transformRectangle()
     * or diva.util.java2d.ShapeUtilities.transformRectangularShape()
     */
    @Deprecated
    public static Shape transform(RectangularShape r, AffineTransform at) {
        // FIXME: should be able to deal with quadrant rotations
        if (_transformRectangularShapeIsBroken) {
            // FIXME FIXME
            Rectangle2D bounds = at.createTransformedShape(r).getBounds2D();
            r.setFrame(bounds);
            return r;
        } else if ((at.getType() & (AffineTransform.TYPE_MASK_SCALE
                | AffineTransform.TYPE_TRANSLATION | AffineTransform.TYPE_IDENTITY)) != 0) {
            double x = r.getX();
            double y = r.getY();
            double w = r.getWidth();
            double h = r.getHeight();
            double xdash = 0.0;
            double ydash = 0.0;
            double wdash = 0.0;
            double hdash = 0.0;

            double[] m = new double[6];
            ;
            at.getMatrix(m);

            switch (at.getType()) {
            case AffineTransform.TYPE_GENERAL_SCALE
                    | AffineTransform.TYPE_TRANSLATION:
            case AffineTransform.TYPE_UNIFORM_SCALE
                    | AffineTransform.TYPE_TRANSLATION:
                xdash = x * m[m00] + m[m02];
                ydash = y * m[m11] + m[m12];
                wdash = w * m[m00];
                hdash = h * m[m11];
                break;

            case AffineTransform.TYPE_GENERAL_SCALE:
            case AffineTransform.TYPE_UNIFORM_SCALE:
                xdash = x * m[m00];
                ydash = y * m[m11];
                wdash = w * m[m00];
                hdash = h * m[m11];
                break;

            case AffineTransform.TYPE_TRANSLATION:
                xdash = x + m[m02];
                ydash = y + m[m12];
                wdash = w;
                hdash = h;
                break;

            case AffineTransform.TYPE_IDENTITY:
                xdash = x;
                ydash = y;
                wdash = w;
                hdash = h;
                break;
            }

            r.setFrame(xdash, ydash, wdash, hdash);
            return r;
        } else {
            return at.createTransformedShape(r);
        }
    }

    /** Transform a shape with the supplied transform. As much as
     * possible, this method attempts to preserve the type of the
     * shape and to modify it directly if possible: if the shape of
     * this figure is an instance of RectangularShape or Polyline,
     * then the shape may be modified directly. Otherwise, a general
     * transformation is used that creates and returns a new instance
     * of GeneralPath.
     *
     * @deprecated Use ShapeUtilities.transformModify()
     */
    @Deprecated
    public static Shape transform(Shape s, AffineTransform at) {
        return ShapeUtilities.transformModify(s, at);
    }

    /** Transform a point from a local transform context into a
     * root transform context.   The root context must enclose
     * the local one, otherwise this method goes into an infinite
     * loop.  You asked for it.
     *  @deprecated Use local.getTransform(root) instead.
     */
    @Deprecated
    public static Point2D transformInto(Point2D p, TransformContext local,
            TransformContext root) {
        Point2D p2 = p;

        while (local != root) {
            p2 = local.getTransform().transform(p, null);
            local = local.getParent();
        }

        return p2;
    }

    /** Translate a figure the given distance in the direction given
     * by the flag. The flag must one of the eight compass directions
     * defined in SwingConstants.
     */
    public static void translate(Figure f, double distance, int direction) {
        Point2D.Double p = new Point2D.Double();
        translate(p, distance, direction);
        f.translate(p.x, p.y);
    }

    /** Translate a point the given distance in the direction given
     * by the flag. The flag must one of the eight compass directions
     * defined in SwingConstants. Return the same point, but modified.
     */
    public static Point2D translate(Point2D p, double distance, int direction) {
        if (p instanceof Point2D.Double) {
            return translate((Point2D.Double) p, distance, direction);
        } else {
            return translate((Point2D.Float) p, distance, direction);
        }
    }

    /** Translate a point the given distance in the direction given
     * by the flag. The flag must one of the eight compass directions
     * defined in SwingConstants. Return the same point, but modified.
     */
    public static Point2D translate(Point2D.Double p, double distance,
            int direction) {
        switch (direction) {
        case SwingConstants.NORTH:
            p.y -= distance;
            break;

        case SwingConstants.NORTH_EAST:
            p.y -= distance;
            p.x += distance;
            break;

        case SwingConstants.EAST:
            p.x += distance;
            break;

        case SwingConstants.SOUTH_EAST:
            p.y += distance;
            p.x += distance;
            break;

        case SwingConstants.SOUTH:
            p.y += distance;
            break;

        case SwingConstants.SOUTH_WEST:
            p.y += distance;
            p.x -= distance;
            break;

        case SwingConstants.WEST:
            p.x -= distance;
            break;

        case SwingConstants.NORTH_WEST:
            p.y -= distance;
            p.x -= distance;
            break;

        default:
            throw new IllegalArgumentException("Unknown direction constant: "
                    + direction);
        }

        return p;
    }

    /** Translate a point the given distance in the direction given
     * by the flag. The flag must one of the eight compass directions
     * defined in SwingConstants. Return the same point, but modified.
     */
    public static Point2D translate(Point2D.Float p, double distance,
            int direction) {
        switch (direction) {
        case SwingConstants.NORTH:
            p.y -= (float) distance;
            break;

        case SwingConstants.NORTH_EAST:
            p.y -= (float) distance;
            p.x += (float) distance;
            break;

        case SwingConstants.EAST:
            p.x += (float) distance;
            break;

        case SwingConstants.SOUTH_EAST:
            p.y += (float) distance;
            p.x += (float) distance;
            break;

        case SwingConstants.SOUTH:
            p.y += (float) distance;
            break;

        case SwingConstants.SOUTH_WEST:
            p.y += (float) distance;
            p.x -= (float) distance;
            break;

        case SwingConstants.WEST:
            p.x -= (float) distance;
            break;

        case SwingConstants.NORTH_WEST:
            p.y -= (float) distance;
            p.x -= (float) distance;
            break;

        default:
            throw new IllegalArgumentException("Unknown direction constant: "
                    + direction);
        }

        return p;
    }

    /** Translate a shape the given distance. If it is possible to do
     * so, perform this translation efficiently, modifying and returning
     * the passed shape. Otherwise use
     * AffineTransform.createTransformedShape() to create a new translated
     * shape, and return that.
     *
     * @deprecated Use ShapeUtilities.translateModify()
     */
    @Deprecated
    public static Shape translate(Shape s, double x, double y) {
        return ShapeUtilities.translateModify(s, x, y);
    }

    /**
     * Move a figure so that its origin is located at the given
     * coordinates.
     */
    public static void translateTo(Figure f, double x, double y) {
        Point2D origin = f.getOrigin();
        double xdash = x - origin.getX();
        double ydash = y - origin.getY();
        f.translate(xdash, ydash);
    }
}
