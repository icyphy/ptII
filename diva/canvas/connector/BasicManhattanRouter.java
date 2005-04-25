/*
  Copyright (c) 1998-2005 The Regents of the University of California
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

import diva.canvas.CanvasUtilities;
import diva.canvas.Site;
import diva.canvas.TransformContext;
import diva.util.java2d.Polyline2D;

import java.awt.Shape;
import java.awt.geom.Point2D;

import javax.swing.SwingUtilities;


/**
 * A basic manhattan router.
 *
 * @version $Id$
 * @author  Steve Neuendorffer
 */
public class BasicManhattanRouter implements ManhattanRouter {
    /** Reroute the given Shape, given that the head site moved.
     */
    public void rerouteHead(Connector c, Shape s) {
        reroute(c, s);
    }

    /** Reroute the given Shape, given that the tail site moved.
     */
    public void rerouteTail(Connector c, Shape s) {
        reroute(c, s);
    }

    /** Reroute the given shape, given that both the head the tail
     * sites moved. The shape is modified by the router.
     */
    public void reroute(Connector c, Shape s) {
        // We're stupid...  don't route incrementally.
        route(c);
    }

    /** Route the given connector, returning a Shape.
     */
    public Shape route(Connector c) {
        return routeManhattan((ManhattanConnector) c);
    }

    /** Route the given connector, returning a Polyline2D. This
     * method is the same as route(), except that the return
     * type is tighter.
     */
    public Polyline2D routeManhattan(ManhattanConnector c) {
        TransformContext currentContext = c.getTransformContext();

        Site headSite = c.getHeadSite();
        Site tailSite = c.getTailSite();
        Point2D headPt;
        Point2D tailPt;

        // Get the transformed head and tail points. Sometimes
        // people will call this before the connector is added
        // to a container, so deal with it
        if (currentContext != null) {
            tailPt = tailSite.getPoint(currentContext);
            headPt = headSite.getPoint(currentContext);
        } else {
            tailPt = tailSite.getPoint();
            headPt = headSite.getPoint();
        }

        double xDiff = headPt.getX() - tailPt.getX();
        double yDiff = headPt.getY() - tailPt.getY();

        // Infer normals if there are none.  This needs to be
        // smarter, and should depend on the normal at the other
        // end if there is one.
        int headDir = CanvasUtilities.reverseDirection(getManhattanDirection(
                    xDiff, yDiff));
        headSite.setNormal(CanvasUtilities.getNormal(headDir));

        if (currentContext != null) {
            headPt = headSite.getPoint(currentContext);
        } else {
            headPt = headSite.getPoint();
        }

        int tailDir = getManhattanDirection(xDiff, yDiff);
        tailSite.setNormal(CanvasUtilities.getNormal(tailDir));

        if (currentContext != null) {
            tailPt = tailSite.getPoint(currentContext);
        } else {
            tailPt = tailSite.getPoint();
        }

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

    private Polyline2D _route(Point2D head, int headDir, Point2D tail,
        int tailDir) {
        double xDiff = head.getX() - tail.getX();
        double yDiff = head.getY() - tail.getY();
        Point2D point;
        int dir;

        // System.out.println("routing, diff=(" + xDiff + ", " + yDiff + ")");
        if (((xDiff * xDiff) < (TOL * TOL)) && ((yDiff * yDiff) < (TOL * TOL))) {
            Polyline2D route = new Polyline2D.Double();
            route.moveTo(tail.getX(), tail.getY());
            return route;
        }

        //System.out.println("headDir = " + headDir);
        if (headDir == SwingUtilities.WEST) {
            //System.out.println("head is east");
            if ((xDiff > 0) && ((yDiff * yDiff) < TOL)
                            && (tailDir == SwingUtilities.EAST)) {
                //System.out.println("completing straight");
                point = tail;
                dir = tailDir;
            } else {
                if (xDiff < 0) {
                    //System.out.println("routing backwards");
                    point = new Point2D.Double(head.getX() - MINDIST,
                            head.getY());
                } else if (((yDiff > 0) && (tailDir == SwingUtilities.SOUTH))
                                || ((yDiff < 0)
                                && (tailDir == SwingUtilities.NORTH))) {
                    //System.out.println("completing 90");
                    point = new Point2D.Double(tail.getX(), head.getY());
                } else if (headDir == tailDir) {
                    double pos = Math.min(head.getX(), tail.getX()) - MINDIST;
                    point = new Point2D.Double(pos, head.getY());
                } else {
                    point = new Point2D.Double(head.getX() - (xDiff / 2),
                            head.getY());
                }

                if (yDiff > 0) {
                    dir = SwingUtilities.NORTH;
                } else {
                    dir = SwingUtilities.SOUTH;
                }
            }
        } else if (headDir == SwingUtilities.EAST) {
            //System.out.println("head is west");
            if ((xDiff < 0) && ((yDiff * yDiff) < TOL)
                            && (tailDir == SwingUtilities.WEST)) {
                //System.out.println("completing");
                point = tail;
                dir = tailDir;
            } else {
                if (xDiff > 0) {
                    //System.out.println("routing backwards");
                    point = new Point2D.Double(head.getX() + MINDIST,
                            head.getY());
                } else if (((yDiff > 0) && (tailDir == SwingUtilities.SOUTH))
                                || ((yDiff < 0)
                                && (tailDir == SwingUtilities.NORTH))) {
                    //System.out.println("completing 90");
                    point = new Point2D.Double(tail.getX(), head.getY());
                } else if (headDir == tailDir) {
                    double pos = Math.max(head.getX(), tail.getX()) + MINDIST;
                    point = new Point2D.Double(pos, head.getY());
                } else {
                    point = new Point2D.Double(head.getX() - (xDiff / 2),
                            head.getY());
                }

                if (yDiff > 0) {
                    dir = SwingUtilities.NORTH;
                } else {
                    dir = SwingUtilities.SOUTH;
                }
            }
        } else if (headDir == SwingUtilities.SOUTH) {
            //System.out.println("head is north");
            if (((xDiff * xDiff) < TOL) && (yDiff < 0)
                            && (tailDir == SwingUtilities.NORTH)) {
                //System.out.println("completing");
                point = tail;
                dir = tailDir;
            } else {
                if (yDiff > 0) {
                    //System.out.println("routing backwards");
                    point = new Point2D.Double(head.getX(),
                            head.getY() + MINDIST);
                } else if (((xDiff > 0) && (tailDir == SwingUtilities.EAST))
                                || ((xDiff < 0)
                                && (tailDir == SwingUtilities.WEST))) {
                    //System.out.println("completing 90");
                    point = new Point2D.Double(head.getX(), tail.getY());
                } else if (headDir == tailDir) {
                    double pos = Math.max(head.getY(), tail.getY()) + MINDIST;
                    point = new Point2D.Double(head.getX(), pos);
                } else {
                    point = new Point2D.Double(head.getX(),
                            head.getY() - (yDiff / 2));
                }

                if (xDiff > 0) {
                    dir = SwingUtilities.WEST;
                } else {
                    dir = SwingUtilities.EAST;
                }
            }
        } else if (headDir == SwingUtilities.NORTH) {
            //System.out.println("head is south");
            if (((xDiff * xDiff) < TOL) && (yDiff > 0)
                            && (tailDir == SwingUtilities.SOUTH)) {
                //System.out.println("completing");
                point = tail;
                dir = tailDir;
            } else {
                if (yDiff < 0) {
                    //System.out.println("routing backwards");
                    point = new Point2D.Double(head.getX(),
                            head.getY() - MINDIST);
                } else if (((xDiff > 0) && (tailDir == SwingUtilities.EAST))
                                || ((xDiff < 0)
                                && (tailDir == SwingUtilities.WEST))) {
                    //System.out.println("completing 90");
                    point = new Point2D.Double(head.getX(), tail.getY());
                } else if (headDir == tailDir) {
                    double pos = Math.min(head.getY(), tail.getY()) - MINDIST;
                    point = new Point2D.Double(head.getX(), pos);
                } else {
                    point = new Point2D.Double(head.getX(),
                            head.getY() - (yDiff / 2));
                }

                if (xDiff > 0) {
                    dir = SwingUtilities.WEST;
                } else {
                    dir = SwingUtilities.EAST;
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

    private double TOL = .1;
    private double MINDIST = 7;

    /** Return the direction between two points who differ by the
     *  given amounts.  The direction returned is restricted to the
     *  closest orthogonal direction.  The integer returned is from
     *  SwingUtilities.
     */
    private int getManhattanDirection(double xDiff, double yDiff) {
        int dir;

        if ((xDiff > 0) && (yDiff > 0)) {
            if (xDiff > yDiff) {
                dir = SwingUtilities.EAST;
            } else {
                dir = SwingUtilities.SOUTH;
            }
        } else if ((xDiff < 0) && (yDiff < 0)) {
            if (xDiff > yDiff) {
                dir = SwingUtilities.NORTH;
            } else {
                dir = SwingUtilities.WEST;
            }
        } else if (xDiff > 0) {
            if (xDiff > -yDiff) {
                dir = SwingUtilities.EAST;
            } else {
                dir = SwingUtilities.NORTH;
            }
        } else {
            if (-xDiff > yDiff) {
                dir = SwingUtilities.WEST;
            } else {
                dir = SwingUtilities.SOUTH;
            }
        }

        return dir;
    }
}
