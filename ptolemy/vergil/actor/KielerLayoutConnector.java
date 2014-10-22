/* KIELER Bend Points with Manhattan Geometry Link.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
import diva.canvas.Figure;
import diva.canvas.Site;
import diva.canvas.TransformContext;
import diva.canvas.connector.BasicManhattanRouter;
import diva.canvas.connector.PerimeterSite;

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
 * @since Ptolemy II 10.0
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
    @Override
    public void route() {
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
            // relation may be null if a new link is currently dragged from some port
            if (relation != null) {
                LayoutHint layoutHint = (LayoutHint) relation
                        .getAttribute("_layoutHint");
                if (layoutHint != null) {
                    layoutHintItem = layoutHint.getLayoutHintItem(
                            link.getHead(), link.getTail());
                    if (layoutHintItem != null) {
                        // Bend points are always considered while a layout operation is
                        // in progress to keep them from being removed. This is not quite
                        // thread-safe, but should work since no more than one MomlChangeRequest
                        // is executed at a given time, and those are the only things that
                        // could trigger a problem with this code.
                        considerBendPoints = _layoutInProgress
                                || layoutHintItem.revalidate();
                        if (considerBendPoints) {
                            bendPointList = layoutHintItem.getBendPointList();
                        } else {
                            layoutHint.removeLayoutHintItem(layoutHintItem);
                        }
                    }
                }
            }
        }

        if (considerBendPoints) {
            repaint();

            GeneralPath path = new GeneralPath();

            // we need the "real" start and end points, i.e. the anchor points on the sites
            Point2D[] startEnd = _getHeadTailPoints(bendPointList);
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
                // Coverity points out that i0 can never be less than 0.
                //if (i0 < 0) {
                //    i0 = 0;
                //}
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
                double d0 = Math.sqrt((x1 - x0) * (x1 - x0) + (y1 - y0)
                        * (y1 - y0));
                double d1 = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1)
                        * (y2 - y1));
                double r = Math.min(_bendRadius, d0);
                r = Math.min(r, d1);

                // The degenerate case of a direct line.
                if (d0 == 0.0 || d1 == 0.0) {
                    path.lineTo((float) x1, (float) y1);
                } else {
                    // Next calculate the intermediate points
                    // that define the bend.
                    double intX0 = x1 + r / d0 * (x0 - x1);
                    double intY0 = y1 + r / d0 * (y0 - y1);
                    double intX1 = x1 + r / d1 * (x2 - x1);
                    double intY1 = y1 + r / d1 * (y2 - y1);

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
            int count = bendPointList.size();
            // Pick a location for the label in the middle of the connector.
            Point2D point1 = bendPointList.get(count / 2 - 1);
            Point2D point2 = bendPointList.get(count / 2);
            _labelLocation = new Point2D.Double(
                    (point1.getX() + point2.getX()) / 2,
                    (point1.getY() + point2.getY()) / 2);
            repositionLabel();

            repaint();
        } else {
            // In this case we have no bend point annotations available so
            // we use the normal draw functionality.
            super.route();
        }
    }

    /**
     * Notifies layout connections that a layout is in progress, which stops them
     * from deciding to remove layout hints from relations. Without this mechanism,
     * it can happen that layout hints get removed seemingly at random. This is
     * caused by layout connectors thinking that one actor in a relation is moved
     * during the application of the layout results. This in turn triggers the
     * corresponding layout hint to be viewed as being invalid, and consequently to
     * be removed.
     * <p>
     * A call to this method with the parameter value {@code true} must always be
     * followed by a call with the parameter value {@code false}.</p>
     * <p>
     * <b>Note:</b> This mechanism is not thread-safe! However, since the problem
     * only occurs while a layout result is being applied through a
     * {@code MoMLChangeRequest} (of which only one is ever being executed at a
     * given time), this shouldn't be a problem.</p>
     *
     * @param inProgress {@code true} if a layout result is currently being applied.
     */
    public static void setLayoutInProgress(boolean inProgress) {
        _layoutInProgress = inProgress;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Get the center point of a Perimeter Site. Copied the idea from
     * {@link PerimeterSite#getPoint(double)}.
     *
     * @param site the site
     * @return the center point of the shape that corresponds to the site
     */
    private Point2D _getCenterPoint(Site site) {
        Figure figure = site.getFigure();
        if (figure == null) {
            return site.getPoint();
        }
        // Port figures return bounds that are relative to the containing node.
        if (site instanceof PortConnectSite
                && figure.getParent() instanceof Figure) {
            figure = (Figure) figure.getParent();
        }
        Rectangle bounds = figure.getShape().getBounds();
        return new Point2D.Double(bounds.getCenterX(), bounds.getCenterY());
    }

    /**
     * Get the starting and ending points of a connector. Copied some code from
     * {@link BasicManhattanRouter#routeManhattan(diva.canvas.connector.ManhattanConnector)}.
     * @param bendPoints a list of bendpoints to determine the anchor point on the site
     * @return the anchor points at the start and end of the
     * connection, i.e. a Point2D array of size 2
     */
    private Point2D[] _getHeadTailPoints(List<Point2D> bendPoints) {
        TransformContext currentContext = getTransformContext();
        Point2D headPt, tailPt;
        Site headSite = getHeadSite();
        Site tailSite = getTailSite();
        if (currentContext != null) {
            headPt = _getCenterPoint(headSite);
            tailPt = _getCenterPoint(tailSite);
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
        double normalX = other.getX() - origin.getX();
        double normalY = other.getY() - origin.getY();
        double theta = Math.atan2(normalY, normalX);
        return theta;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     * The radius for filleting the corners of the connector.
     */
    private double _bendRadius = 10;

    /**
     * Whether automatic layout is currently in progress. If so, no layout hints
     * are removed.
     */
    private static boolean _layoutInProgress = false;

}
