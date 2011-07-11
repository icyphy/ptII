/* KIELER Bend Points with Manhattan Geometry Link.

 Copyright (c) 1998-2010 The Regents of the University of California.
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
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.vergil.actor;

import java.awt.Rectangle;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.List;

import ptolemy.kernel.Relation;
import ptolemy.vergil.basic.layout.kieler.LayoutHint;
import ptolemy.vergil.basic.layout.kieler.LayoutHint.LayoutHintItem;
import ptolemy.vergil.kernel.Link;
import diva.canvas.Site;
import diva.canvas.TransformContext;
import diva.canvas.connector.BasicManhattanRouter;
import diva.canvas.connector.Connector;
import diva.canvas.connector.PerimeterSite;
import diva.util.java2d.Polyline2D;

///////////////////////////////////////////////////////////////////
////KielerLayoutConnector

/**
 * An extension to LinkManhattanRouter supporting bend points for routing links
 * provided by the corresponding relation. These bend points can be set by any
 * mechanism, e.g. the KIELER dataflow layout. The latter is currently used by
 * the automatic layout mechanism that simply adds such bend point layout hints
 * to relations.
 *
 * @author Christian Motika
 * @since Ptolemy II 8.2
 * @Pt.ProposedRating Red (cmot)
 */

public class KielerLayoutConnector extends LinkManhattanConnector {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Construct a new connector with the given tail and head for the specified
     * link. The head and tail sites may be representative sites for multiport,
     * in which case they are not necessarily the ones returned by getHeadSite()
     * or getTailSite(). Those methods will return new sites as needed to ensure
     * that each each connection is to its own site.
     *
     * @param tail The tail site.
     * @param head The head site.
     * @param link The link.
     */
    public KielerLayoutConnector(Site tail, Site head, Link link) {
        super(tail, head, link);
    }

    /**
     * Tell the connector to route itself between the current positions of the
     * head and tail sites. If bend points are available, draw the line with
     * these instead. Delete bend point information if modification detected
     * (i.e., movement of one or the other end of a link).
     */
    public void route() {
        repaint();

        // Parse the bend points if existing.
        List<Point2D> bendPointList = null;
        Object object = this.getUserObject();
        Link link = null;
        Relation relation = null;
        LayoutHintItem layoutHintItem = null;
        boolean considerBendPoints = false;
        if (object instanceof Link) {
            link = (Link) object;
            relation = link.getRelation();
            // relation may be null if a new link is currently dragged from some
            // port
            if (relation != null) {
                LayoutHint layoutHint = (LayoutHint) relation
                        .getAttribute("_layoutHint");
                if (layoutHint != null) {
                    layoutHintItem = layoutHint.getLayoutHintItem(
                            link.getHead(), link.getTail());
                    if (layoutHintItem != null) {
                        considerBendPoints = layoutHintItem.revalidate();
                        if (!considerBendPoints) {
                            layoutHint.removeLayoutHintItem(link.getHead(),
                                    link.getTail());
                            if (layoutHint.isEmpty()) {
                                layoutHint.removeLayoutHintProperty(relation);
                            }
                        } else {
                            bendPointList = layoutHintItem.getBendPointList();
                        }
                    }
                }
            }
        }

        Polyline2D polyline = (Polyline2D) getRouter().route(this);
        int count = polyline.getVertexCount();

        if (count > 1) {
            // Pick a location for the label in the middle of the connector.
            _labelLocation = (new Point2D.Double(
                    (polyline.getX(count / 2) + polyline.getX((count / 2) - 1)) / 2,
                    (polyline.getY(count / 2) + polyline.getY((count / 2) - 1)) / 2));
        } else {
            // Attach the label to the only point of the connector.
            _labelLocation = new Point2D.Double(polyline.getX(0),
                    polyline.getY(0));
        }

        if (_bendRadius == 0) {
            setShape(polyline);
        } else {
            GeneralPath path = new GeneralPath();

            if (considerBendPoints && bendPointList != null) {

                // we need the "real" start and end points, i.e. the anchor points on the sites
                Point2D[] startEnd = _getHeadTailPoints(this, bendPointList);
                double startX = startEnd[0].getX();
                double startY = startEnd[0].getY();
                double previousX = startX;
                double previousY = startY;
                double endX = startEnd[1].getX();
                double endY = startEnd[1].getY();

                // Start drawing the line.
                // Under Java 1.5, we only have moveTo(float, float).
                path.moveTo((float) startX, (float) startY);

                // Add the start point and end point to the bendPointList in
                // order to get the curveTo-effect working.
                bendPointList.add(0, new Point2D.Double(startX, startY));
                bendPointList.add(new Point2D.Double(endX, endY));

                for (int i = 1; i <= bendPointList.size() - 1; i++) {
                    int i1 = i;
                    int i0 = i - 1;
                    if (i0 < 0) {
                        i0 = 0;
                    }
                    if (i0 > bendPointList.size() - 1) {
                        i0 = bendPointList.size() - 1;
                    }

                    // Consider triplets of coordinates.
                    double x0 = previousX;
                    double y0 = previousY;
                    double x1 = bendPointList.get(i0).getX();
                    double y1 = bendPointList.get(i0).getY();

                    double x2 = bendPointList.get(i1).getX();
                    double y2 = bendPointList.get(i1).getY();

                    // Calculate midpoints.
                    x2 = (x1 + x2) / 2;
                    y2 = (y1 + y2) / 2;

                    // First make sure that the radius is not
                    // bigger than half one of the arms of the triplets
                    double d0 = Math.sqrt(((x1 - x0) * (x1 - x0))
                            + ((y1 - y0) * (y1 - y0)));
                    double d1 = Math.sqrt(((x2 - x1) * (x2 - x1))
                            + ((y2 - y1) * (y2 - y1)));
                    double r = Math.min(_bendRadius, d0);
                    r = Math.min(r, d1);

                    // The degenerate case of a direct line.
                    if ((d0 == 0.0) || (d1 == 0.0)) {
                        path.lineTo((float) x1, (float) y1);
                    } else {
                        // Next calculate the intermediate points
                        // that define the bend.
                        double intX0 = x1 + ((r / d0) * (x0 - x1));
                        double intY0 = y1 + ((r / d0) * (y0 - y1));
                        double intX1 = x1 + ((r / d1) * (x2 - x1));
                        double intY1 = y1 + ((r / d1) * (y2 - y1));

                        // Next draw the line from the previous
                        // coord to the intermediate coord, and
                        // curve around the corner.
                        path.lineTo((float) intX0, (float) intY0);
                        path.curveTo((float) x1, (float) y1, (float) x1,
                                (float) y1, (float) intX1, (float) intY1);
                        previousX = x2;
                        previousY = y2;
                    }
                }

                // Finally close the last segment with a line.
                // Under Java 1.5, we only have moveTo(float, float).
                path.lineTo((float) endX, (float) endY);

                // Now set the shape.
                setShape(path);

                // Move the label.
                repositionLabel();

                repaint();
            } else {
                // In this case we have no bend point annotations available so
                // we use the normal draw functionality.
                super.route();
            }
        } // else bendradius == 2
          // System.out.println("end route: "+relation.exportMoMLPlain());
    }

    /**
     * Reposition the text label of the connector.
     */
    public void repositionLabel() {
        if (_labelLocation == null) {
            route();

            // Route will call this method recursively.
            return;
        }

        if (getLabelFigure() != null) {
            getLabelFigure().translateTo(_labelLocation);
            getLabelFigure().autoAnchor(getShape());
        }
    }

    /**
     * Get the center point of a Perimeter Site. Copied the idea from
     * {@link PerimeterSite#getPoint(double)}.
     *
     * @param site the site
     * @return the center point of the shape that corresponds to the site
     */
    private Point2D _getCenterPoint(Site site) {
        try {
            Rectangle bounds = site.getFigure().getShape().getBounds();
            return new Point2D.Double(bounds.getCenterX(), bounds.getCenterY());
        } catch (NullPointerException e) {
            return site.getPoint();
        }
    }

    /**
     * Get the starting and ending points of a connector. Copied some code from
     * {@link BasicManhattanRouter#routeManhattan(diva.canvas.connector.ManhattanConnector)}.
     * @param c the corresponding connector
     * @param bendPoints a list of bendpoints to determine the anchor point on the site
     * @return the anchor points at the start and end of the connectior, i.e. a Point2D array of size 2
     *
     */
    private Point2D[] _getHeadTailPoints(Connector c, List<Point2D> bendPoints) {
        TransformContext currentContext = c.getTransformContext();
        Point2D headPt, tailPt;
        Site headSite = c.getHeadSite();
        Site tailSite = c.getTailSite();
        if (currentContext != null) {
            headPt = _getCenterPoint(headSite);//headSite.getPoint(currentContext);
            tailPt = _getCenterPoint(tailSite);//tailSite.getPoint(currentContext);
            // get neighbor point to head and tail to determine the output sides
            Point2D headBend, tailBend;
            if (!bendPoints.isEmpty()) {
                headBend = bendPoints.get(0);
                tailBend = bendPoints.get(bendPoints.size() - 1);
            } else {
                headBend = tailPt;
                tailBend = headPt;
            }
            // now change the "Normal" side of the site
            headSite.setNormal(_getNormal(headPt, headBend));
            // headSite.setNormal(Math.PI);
            tailSite.setNormal(_getNormal(tailPt, tailBend));
            // and get the points again
            headPt = headSite.getPoint(currentContext);
            tailPt = tailSite.getPoint(currentContext);
        } else {
            // fallback if called too early, i.e. no context available
            tailPt = tailSite.getPoint();
            headPt = headSite.getPoint();
        }
        Point2D[] result = { headPt, tailPt };
        return result;
    }

    /**
     * Get the angle in radians from the origin to the other point.
     *
     * @param origin the original point
     * @param other the other point
     * @return angle in radians
     */
    private double _getNormal(Point2D origin, Point2D other) {
        Point2D normalPoint = new Point2D.Double(other.getX() - origin.getX(),
                other.getY() - origin.getY());
        // use negative y coordinate, because atan uses "normal" y direction
        double theta = Math.atan2(-normalPoint.getY(), normalPoint.getX());
        return theta;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     * The radius for filleting the corners of the connector.
     */
    private double _bendRadius = 10;

    /**
     * The location to attach the label to.
     */
    private Point2D _labelLocation;

}
