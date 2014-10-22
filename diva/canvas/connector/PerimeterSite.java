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

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import diva.canvas.AbstractSite;
import diva.canvas.Figure;
import diva.util.java2d.Polygon2D;

/** A site that locates itself on the perimeter of a figure.
 * It implements the setNormal() method to change its position
 * so that a line drawn through the site at the given normal
 * will appear to originate from the center of the figure.
 * Currently, perimeter sites are able to locate themselves
 * on the perimeter of ellipses, rectangles, and arbitrary polygons.  For
 * any other shape, the site locates itself on the perimeter of the bounding
 * box for the shape.
 *
 * @version        $Id$
 * @author         John Reekie, Steve Neuendorffer
 */
public class PerimeterSite extends AbstractSite {
    /** The id
     */
    private int _id;

    /** The parent figure
     */
    private Figure _parentFigure;

    /** Create a new site on the given figure with the given ID.
     */
    public PerimeterSite(Figure figure, int id) {
        _parentFigure = figure;
        _id = id;
    }

    /** Get the figure to which this site is attached.
     */
    @Override
    public Figure getFigure() {
        return _parentFigure;
    }

    /** Get the ID of this site.
     */
    @Override
    public int getID() {
        return _id;
    }

    /** Get the point location of the site, in the enclosing
     * transform context.
     */
    @Override
    public Point2D getPoint(double normal) {
        // We operate on the figure's shape, not the bounds!
        Shape shape = _parentFigure.getShape();
        Rectangle2D bounds = shape.getBounds2D();

        // Get the center of the parent
        double x = bounds.getX();
        double y = bounds.getY();
        double width = bounds.getWidth();
        double height = bounds.getHeight();

        double xCenter = x + width / 2;
        double yCenter = y + height / 2;

        // Switch on the shape of the figure
        double xout = 0.0;

        // Switch on the shape of the figure
        double yout = 0.0;
        double pi = Math.PI;
        double pi2 = Math.PI / 2.0;
        double alpha = normal;
        double beta = pi2 - alpha;

        if (shape instanceof Ellipse2D) {
            double rx = width / 2.0;
            double ry = height / 2.0;
            double dx;
            double dy;

            if (Math.abs(width - height) < 0.001) {
                // Circle
                dx = rx * Math.cos(alpha);
                dy = ry * Math.sin(alpha);
            } else {
                // Ellipse
                if (Math.abs(alpha - pi2) < 0.001) {
                    dx = 0.0;
                    dy = ry;
                } else if (Math.abs(alpha + pi2) < 0.001) {
                    dx = 0.0;
                    dy = -ry;
                } else {
                    double m = Math.tan(alpha);
                    dx = rx * ry / Math.sqrt(ry * ry + rx * rx * m * m);

                    if (alpha > pi2 || alpha < -pi2) {
                        dx = -dx;
                    }

                    dy = m * dx;
                }
            }

            xout = x + rx + dx;
            yout = y + ry + dy;
        } else if (shape instanceof Polygon2D) {
            Polygon2D polygon = (Polygon2D) shape;
            double pointx = xCenter;
            double pointy = yCenter;
            double max_r = 0;
            int vertexes = polygon.getVertexCount();

            if (vertexes > 1) {
                // compute the intersection of the line segment passing through
                // (x0,y0) and (x1,y1) with the ray passing through
                // (xCenter, yCenter) and (px,py)
                double x0;

                // compute the intersection of the line segment passing through
                // (x0,y0) and (x1,y1) with the ray passing through
                // (xCenter, yCenter) and (px,py)
                double x1;

                // compute the intersection of the line segment passing through
                // (x0,y0) and (x1,y1) with the ray passing through
                // (xCenter, yCenter) and (px,py)
                double y0;

                // compute the intersection of the line segment passing through
                // (x0,y0) and (x1,y1) with the ray passing through
                // (xCenter, yCenter) and (px,py)
                double y1;
                double px = xCenter + Math.cos(alpha);
                double py = yCenter + Math.sin(alpha);

                // Assume the polygon is closed, so do the "closing stroke"
                // first
                x1 = polygon.getX(vertexes - 1);
                y1 = polygon.getY(vertexes - 1);

                for (int vertexPair = 0; vertexPair < vertexes; vertexPair++) {
                    x0 = x1;
                    y0 = y1;
                    x1 = polygon.getX(vertexPair);
                    y1 = polygon.getY(vertexPair);

                    double A = (x0 - xCenter) * (py - yCenter) - (y0 - yCenter)
                            * (px - xCenter);
                    double B = (y1 - y0) * (px - xCenter) - (x1 - x0)
                            * (py - yCenter);
                    double t = A / B;

                    // Must be between (x0,y0) and (x1,y1)
                    if (0 <= t && t <= 1) {
                        double tx = x0 + (x1 - x0) * t;
                        double ty = y0 + (y1 - y0) * t;
                        boolean xGood = tx >= xCenter && px >= xCenter
                                || tx < xCenter && px < xCenter;
                        boolean yGood = ty >= yCenter && py >= yCenter
                                || ty < yCenter && py < yCenter;

                        // Must be on (px,py) side of (xCenter, yCenter)
                        if (xGood && yGood) {
                            double r = (tx - xCenter) * (tx - xCenter)
                                    + (ty - yCenter) * (ty - yCenter);

                            if (r > max_r) {
                                pointx = tx;
                                pointy = ty;
                                max_r = r;
                            }
                        }
                    }
                }

                xout = pointx;
                yout = pointy;
            } else {
                // If there is only one vertex, then just return the center.
                // We can't intersect with a line.
                xout = x;
                yout = y;
            }
        } else {
            //            if (!(shape instanceof Rectangle2D)) {
            //                // Anything that's not a rectangle looks like one...
            //                shape = bounds;
            //            }

            // The angle of the top-right corner
            double t = Math.atan2(height, width);

            if (alpha < -pi + t || alpha > pi - t) {
                // on left edge
                xout = x;
                yout = y + height / 2.0 - width / 2.0 * Math.tan(alpha);
            } else if (alpha < -t) {
                // top edge
                yout = y;
                xout = x + width / 2.0 - height / 2.0 * Math.tan(beta);
            } else if (alpha < t) {
                // right edge
                xout = x + width;
                yout = y + height / 2.0 + width / 2.0 * Math.tan(alpha);
            } else {
                // on bottom edge
                yout = y + height;
                xout = x + width / 2.0 + height / 2.0 * Math.tan(beta);
            }
        }

        return new Point2D.Double(xout, yout);
    }

    /** Get the x-coordinate of the site. The site
     * is located in the center of the parent figure's bounding
     * box.
     */
    @Override
    public double getX() {
        return getPoint().getX();
    }

    /** Get the y-coordinate of the site.  The site
     * is located in the center of the parent figure's bounding
     * box.
     */
    @Override
    public double getY() {
        return getPoint().getY();
    }
}
