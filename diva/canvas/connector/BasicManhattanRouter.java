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
package diva.canvas.connector;

import java.awt.Shape;
import java.awt.geom.Point2D;

import javax.swing.SwingConstants;

import diva.canvas.CanvasUtilities;
import diva.canvas.Site;
import diva.canvas.TransformContext;
import diva.util.java2d.Polyline2D;

/**
 * A basic manhattan router.
 *
 * @version $Id$
 * @author  Steve Neuendorffer, Contributor: Christoph Daniel Schulze
 */
public class BasicManhattanRouter implements ManhattanRouter {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Reroute the given Shape, given that the head site moved.
     */
    @Override
    public void rerouteHead(Connector c, Shape s) {
        reroute(c, s);
    }

    /** Reroute the given Shape, given that the tail site moved.
     */
    @Override
    public void rerouteTail(Connector c, Shape s) {
        reroute(c, s);
    }

    /** Reroute the given shape, given that both the head the tail
     * sites moved. The shape is modified by the router.
     */
    @Override
    public void reroute(Connector c, Shape s) {
        // We're stupid...  don't route incrementally.
        route(c);
    }

    /** Route the given connector, returning a Shape.
     */
    @Override
    public Shape route(Connector c) {
        return routeManhattan((ManhattanConnector) c);
    }

    /** Route the given connector, returning a Polyline2D. This
     * method is the same as route(), except that the return
     * type is tighter.
     */
    @Override
    public Polyline2D routeManhattan(ManhattanConnector c) {
        TransformContext currentContext = c.getTransformContext();

        Site headSite = c.getHeadSite();
        Site tailSite = c.getTailSite();
        Point2D headPt;
        Point2D tailPt;

        /* Sometimes people will call this method before the connector is
         * even added to a container. If that's the case, we don't know
         * the final coordinates of the head and tail points yet, which
         * makes all of the work below useless. In particular, the code
         * below would calculate wrong directions from which the connector
         * approaches the head and tail points, which the next call would
         * base its calculations on. This leads to strange quirks with
         * edges connected to relation vertices.
         */
        if (currentContext == null) {
            // Coordinates are not final, so return a direct line to avoid
            // problems with code that expects the polyline to not be empty
            tailPt = tailSite.getPoint();
            headPt = headSite.getPoint();

            Polyline2D.Double polyline = new Polyline2D.Double();
            polyline.moveTo(tailPt.getX(), tailPt.getY());
            polyline.lineTo(headPt.getX(), headPt.getY());

            return polyline;
        } else {
            // Get the transformed head and tail points
            tailPt = tailSite.getPoint(currentContext);
            headPt = headSite.getPoint(currentContext);
        }

        /* There's a problem to be dealt with here in the corner case where
         * the tail and head connection points form a line close to a diagonal.
         * When determining the direction in which an edge leaves one of the
         * connection points, the specific connection point might be changed to
         * correspond to that new angle (think about where horizontal and
         * vertical edges touch relation vertices). This may result in the angle
         * flipping to the other side of that diagonal, thereby resulting in a
         * different direction on the next method call.
         * To fix this, we now allow a certain tolerance around the diagonals
         * before we change the edge direction from the old direction, if any
         * had already been determined previously.
         */

        // Find the angle formed by the direct line connecting the tail
        // point with the head point and the x axis
        double radAngle = Math.atan2(tailPt.getY() - headPt.getY(),
                headPt.getX() - tailPt.getX());
        double distance = tailPt.distance(headPt);

        // Infer direction in which the edge has to leave the head
        // connection point
        int headDir = _getManhattanDirection(
                radAngle,
                distance,
                true,
                headSite.hasNormal() ? CanvasUtilities.getDirection(headSite
                        .getNormal()) : -1);
        headSite.setNormal(CanvasUtilities.getNormal(headDir));
        headPt = headSite.getPoint(currentContext);

        // Infer direction in which the edge has to leave the tail
        // connection point
        int tailDir = _getManhattanDirection(
                radAngle,
                distance,
                false,
                tailSite.hasNormal() ? CanvasUtilities.getDirection(tailSite
                        .getNormal()) : -1);
        tailSite.setNormal(CanvasUtilities.getNormal(tailDir));
        tailPt = tailSite.getPoint(currentContext);

        // The site may not allow it's normal to be changed.  In
        // which case, we have to ask it for its site again.
        headDir = CanvasUtilities.getDirection(headSite.getNormal());
        tailDir = CanvasUtilities.getDirection(tailSite.getNormal());

        // Adjust for decorations on the ends
        double headAngle = CanvasUtilities.getNormal(headDir);
        double tailAngle = CanvasUtilities.getNormal(tailDir);

        if (c.getHeadEnd() != null) {
            c.getHeadEnd().setNormal(headAngle);
            c.getHeadEnd().setOrigin(headPt.getX(), headPt.getY());
            c.getHeadEnd().getConnection(headPt);
        }

        if (c.getTailEnd() != null) {
            c.getTailEnd().setNormal(tailAngle);
            c.getTailEnd().setOrigin(tailPt.getX(), tailPt.getY());
            c.getTailEnd().getConnection(tailPt);
        }

        Polyline2D route = _route(headPt, headDir, tailPt, tailDir);
        return route;

        // FIXME Adjust for decorations on the ends

        /*
         if (c.getHeadEnd() != null) {
         c.getHeadEnd().setNormal(angle+Math.PI);
         c.getHeadEnd().setOrigin(headPt.getX(), headPt.getY());
         c.getHeadEnd().getConnection(headPt);
         }
         if (c.getTailEnd() != null) {
         c.getTailEnd().setNormal(angle);
         c.getTailEnd().setOrigin(tailPt.getX(), tailPt.getY());
         c.getTailEnd().getConnection(tailPt);
         }

         */
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return a polyline describing the routed edge from the head connection
     *  point to the tail connection point.
     *  @param head head connection point.
     *  @param headDir direction from which the edge is to approach the head
     *                 connection point.
     *  @param tail tail connection point.
     *  @param tailDir direction from which the edge is to approach the tail
     *                 connection point.
     *  @return polyline describing the routed edge.
     */
    private Polyline2D _route(Point2D head, int headDir, Point2D tail,
            int tailDir) {
        double xDiff = head.getX() - tail.getX();
        double yDiff = head.getY() - tail.getY();
        Point2D point;
        int dir;

        // System.out.println("routing, diff=(" + xDiff + ", " + yDiff + ")");
        if (xDiff * xDiff < TOL * TOL && yDiff * yDiff < TOL * TOL) {
            Polyline2D route = new Polyline2D.Double();
            route.moveTo(tail.getX(), tail.getY());
            return route;
        }

        //System.out.println("headDir = " + headDir);
        if (headDir == SwingConstants.WEST) {
            //System.out.println("head is east");
            if (xDiff > 0 && yDiff * yDiff < TOL
                    && tailDir == SwingConstants.EAST) {
                //System.out.println("completing straight");
                point = tail;
                dir = tailDir;
            } else {
                if (xDiff < 0) {
                    //System.out.println("routing backwards");
                    point = new Point2D.Double(head.getX() - MINDIST,
                            head.getY());
                } else if (yDiff > 0 && tailDir == SwingConstants.SOUTH
                        || yDiff < 0 && tailDir == SwingConstants.NORTH) {
                    //System.out.println("completing 90");
                    point = new Point2D.Double(tail.getX(), head.getY());
                } else if (headDir == tailDir) {
                    double pos = Math.min(head.getX(), tail.getX()) - MINDIST;
                    point = new Point2D.Double(pos, head.getY());
                } else {
                    point = new Point2D.Double(head.getX() - xDiff / 2,
                            head.getY());
                }

                if (yDiff > 0) {
                    dir = SwingConstants.NORTH;
                } else {
                    dir = SwingConstants.SOUTH;
                }
            }
        } else if (headDir == SwingConstants.EAST) {
            //System.out.println("head is west");
            if (xDiff < 0 && yDiff * yDiff < TOL
                    && tailDir == SwingConstants.WEST) {
                //System.out.println("completing");
                point = tail;
                dir = tailDir;
            } else {
                if (xDiff > 0) {
                    //System.out.println("routing backwards");
                    point = new Point2D.Double(head.getX() + MINDIST,
                            head.getY());
                } else if (yDiff > 0 && tailDir == SwingConstants.SOUTH
                        || yDiff < 0 && tailDir == SwingConstants.NORTH) {
                    //System.out.println("completing 90");
                    point = new Point2D.Double(tail.getX(), head.getY());
                } else if (headDir == tailDir) {
                    double pos = Math.max(head.getX(), tail.getX()) + MINDIST;
                    point = new Point2D.Double(pos, head.getY());
                } else {
                    point = new Point2D.Double(head.getX() - xDiff / 2,
                            head.getY());
                }

                if (yDiff > 0) {
                    dir = SwingConstants.NORTH;
                } else {
                    dir = SwingConstants.SOUTH;
                }
            }
        } else if (headDir == SwingConstants.SOUTH) {
            //System.out.println("head is north");
            if (xDiff * xDiff < TOL && yDiff < 0
                    && tailDir == SwingConstants.NORTH) {
                //System.out.println("completing");
                point = tail;
                dir = tailDir;
            } else {
                if (yDiff > 0) {
                    //System.out.println("routing backwards");
                    point = new Point2D.Double(head.getX(), head.getY()
                            + MINDIST);
                } else if (xDiff > 0 && tailDir == SwingConstants.EAST
                        || xDiff < 0 && tailDir == SwingConstants.WEST) {
                    //System.out.println("completing 90");
                    point = new Point2D.Double(head.getX(), tail.getY());
                } else if (headDir == tailDir) {
                    double pos = Math.max(head.getY(), tail.getY()) + MINDIST;
                    point = new Point2D.Double(head.getX(), pos);
                } else {
                    point = new Point2D.Double(head.getX(), head.getY() - yDiff
                            / 2);
                }

                if (xDiff > 0) {
                    dir = SwingConstants.WEST;
                } else {
                    dir = SwingConstants.EAST;
                }
            }
        } else if (headDir == SwingConstants.NORTH) {
            //System.out.println("head is south");
            if (xDiff * xDiff < TOL && yDiff > 0
                    && tailDir == SwingConstants.SOUTH) {
                //System.out.println("completing");
                point = tail;
                dir = tailDir;
            } else {
                if (yDiff < 0) {
                    //System.out.println("routing backwards");
                    point = new Point2D.Double(head.getX(), head.getY()
                            - MINDIST);
                } else if (xDiff > 0 && tailDir == SwingConstants.EAST
                        || xDiff < 0 && tailDir == SwingConstants.WEST) {
                    //System.out.println("completing 90");
                    point = new Point2D.Double(head.getX(), tail.getY());
                } else if (headDir == tailDir) {
                    double pos = Math.min(head.getY(), tail.getY()) - MINDIST;
                    point = new Point2D.Double(head.getX(), pos);
                } else {
                    point = new Point2D.Double(head.getX(), head.getY() - yDiff
                            / 2);
                }

                if (xDiff > 0) {
                    dir = SwingConstants.WEST;
                } else {
                    dir = SwingConstants.EAST;
                }
            }
        } else {
            throw new RuntimeException("unknown dir");
        }

        Polyline2D route = _route(point, dir, tail, tailDir);
        route.lineTo(head.getX(), head.getY());

        //System.out.println("route = " + route);
        return route;
    }

    /** Return the direction of a line with the given angle to the x axis,
     *  given in radians. If no previous direction has been computed, the
     *  direction is restricted to the closest orthogonal direction. If a
     *  direction was already computed previously, we allow a certain
     *  tolerance around 45 degree angles before the direction is changed
     *  to prevent edges flip-flopping around.
     *  @param radAngle the angle between the edge, pointing from tail to
     *  head, and the x axis.
     *  @param distance distance between the head and tail connectors.
     *  @param head {@code true} if we're computing the direction of
     *  the head connection point, {@code false} for the tail
     *  connection point.
     *  @param oldDirection the old direction at the connection point
     *  or {@code -1} if there was no old direction.
     *  @return one of the constants in {@code SwingConstants}
     *  describing the direction from which the edge should approach
     *  the specified connection point.
     */
    private int _getManhattanDirection(double radAngle, double distance,
            boolean head, int oldDirection) {
        int dir;
        boolean downwards = false;

        // Check if the edge is pointing downwards
        if (radAngle < 0.0) {
            downwards = true;
            radAngle = -1.0 * radAngle;
        }

        // We can now infer the edge direction from the angle
        if (radAngle < PI_QUARTER) {
            dir = SwingConstants.EAST;
        } else if (radAngle > PI_THREE_QUARTERS) {
            dir = SwingConstants.WEST;
        } else {
            dir = downwards ? SwingConstants.SOUTH : SwingConstants.NORTH;
        }

        // Check if we have an old direction and might need to prevent edge flip-flopping
        if (oldDirection != -1) {
            // If we have a head direction, we must reverse it first
            if (head) {
                oldDirection = CanvasUtilities.reverseDirection(oldDirection);
            }

            if (oldDirection != dir) {
                // radAngle currently lies somewhere in the two top quadrants. To make
                // things easier, transform it to the first quadrant
                if (radAngle > PI_HALF) {
                    radAngle -= PI_HALF;
                }

                // Check if radAngle is close to 45 degrees. The definition of "close"
                // should probably depend on the distance of the points, since we might
                // have to allow a larger tolerance for close objects than for distant
                // ones
                if (Math.pow(radAngle - PI_QUARTER, 2) <= 1.0 / distance) {
                    dir = oldDirection;
                }
            }
        }

        return head ? CanvasUtilities.reverseDirection(dir) : dir;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private final static double TOL = .1;

    private final static double MINDIST = 7;

    /**
     * A quarter Pi. Defined as a constant for performance.
     */
    private final static double PI_QUARTER = Math.PI * 0.25;

    /**
     * Half Pi. Defines as a constant for performance.
     */
    private final static double PI_HALF = Math.PI * 0.5;

    /**
     * Three quarters Pi. Defined as a constant for performance.
     */
    private final static double PI_THREE_QUARTERS = Math.PI * 0.75;
}
