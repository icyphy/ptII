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

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.CompositeActor;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.vergil.basic.layout.kieler.PtolemyModelUtil;
import ptolemy.vergil.kernel.Link;
import diva.canvas.Site;
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
    ////                      public methods                       ////

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
        // Parse the bend points if existing.
        List<Point2D> bendPointList = null;
        Object object = this.getUserObject();
        Link link = null;
        Relation relation = null;
        if (object instanceof Link) {
            link = (Link) object;
            relation = link.getRelation();

            // The following code should try to delete possibly added layout hints.
            if (isModified(relation, link)) {
                try {
                    PtolemyModelUtil _ptolemyModelUtil = (new PtolemyModelUtil());
                    _ptolemyModelUtil
                            ._layoutHints(relation.getName(), link, "");
                    _ptolemyModelUtil
                            ._performChangeRequest((CompositeActor) relation
                                    .getContainer());
                } catch (Exception e) {
                    // Ignore if we cannot delete anything.
                    // FIXME: There should be a way to check first whether bend
                    // point layout hints exist. We only then should try to 
                    // delete this information
                }
            } else {
                bendPointList = parseBendPoints(relation, link);
            }
        }
        repaint();

        Polyline2D polyline = (Polyline2D) getRouter().route(this);
        int count = polyline.getVertexCount();

        if (count > 1) {
            // Pick a location for the label in the middle of the connector.
            _labelLocation = (new Point2D.Double(
                    (polyline.getX(count / 2) + polyline.getX((count / 2) - 1)) / 2,
                    (polyline.getY(count / 2) + polyline.getY((count / 2) - 1)) / 2));
        } else {
            // Attach the label to the only point of the connector.
            _labelLocation = new Point2D.Double(polyline.getX(0), polyline.getY(0));
        }

        if (_bendRadius == 0) {
            setShape(polyline);
        } else {
            GeneralPath path = new GeneralPath();

            double startX = polyline.getX(0);
            double startY = polyline.getY(0);
            double previousX = startX;
            double previousY = startY;
            double endX = (float) polyline.getX(polyline.getVertexCount() - 1);
            double endY = (float) polyline.getY(polyline.getVertexCount() - 1);

            boolean considerBendPoints = bendPointList != null
                    && bendPointList.size() > 0;

            if (considerBendPoints) {
                boolean isVertexConnected = relation.exportMoMLPlain()
                        .contains("<vertex");

                // In this case we have bend points provided e.g., by a layouter.
                // We will consider these instead of doing the originally manhatten
                // routing.
                boolean reverseOrder = false;
                if (bendPointList.size() > 1) {
                    reverseOrder = isReversedOrder(new Point2D.Double(startX,
                            startY), new Point2D.Double(endX, endY),
                            bendPointList);
                }

                // The following is a workaround because vertex "port position" seem
                // to adapt the older ptolemy layout so we replace the first position!
                if (isVertexConnected) {
                    // In this case the reverse order does not matter, we seem always
                    // to draw from the vertex as a starting point.
                    double sY = startY;
                    double bX = bendPointList.get(0).getX();
                    double bY = bendPointList.get(0).getY();

                    double marging = 20;
                    if (!reverseOrder) {
                        if ((sY < bY + marging) && (sY > bY - marging)) {
                            startY = bY;
                            previousY = startY;
                        } else {
                            startX = bX;
                            previousX = startX;
                        }
                    }
                }

                // Start drawing the line.
                path.moveTo(startX, startY);

                // Add the start point and end point to the bendPointList in 
                // order to get the curveTo-effect working.
                if (!reverseOrder) {
                    bendPointList.add(0, new Point2D.Double(startX, startY));
                    bendPointList.add(new Point2D.Double(endX, endY));
                } else {
                    bendPointList.add(new Point2D.Double(startX, startY));
                    bendPointList.add(0, new Point2D.Double(endX, endY));
                }

                for (int i = 1; i <= bendPointList.size() - 1; i++) {
                    int i1 = i;
                    int i0 = i - 1;
                    if (reverseOrder) {
                        i1 = bendPointList.size() - i - 1;
                        i0 = bendPointList.size() - i;
                    }
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
                path.lineTo(endX, endY);

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
        }
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

    ///////////////////////////////////////////////////////////////////
    ////                     private methods                       ////

    /**
     * Returns true if at least one of the end point actors of a link has been
     * moved. Movement invalidates the modificationMarker. This method checks,
     * whether this marker is still valid.
     * 
     * @param relation the relation in question
     * @param link the link in question
     * @return true, if is end point was moved and the link is considered to 
     *         be modified
     */
    private boolean isModified(Relation relation, Link link) {
        if (relation != null) {
            Attribute attribute = relation.getAttribute("_modificationMarker:"
                    + PtolemyModelUtil.getLinkId(link));
            if ((attribute != null) && (attribute instanceof StringAttribute)) {
                String modificationMarker = ((StringAttribute) attribute)
                        .getExpression();
                String currentMarker = PtolemyModelUtil
                        .getModificationMarker(link);
                return (!modificationMarker.equals(currentMarker));
            }
        }
        return false;
    }

    /**
     * Parse the bend points of a link that are saved in a parameter of the
     * corresponding relation.
     * 
     * @param relation the relation
     * @param link the link
     * @return the list of bend points if any, null otherwise
     */
    private List<Point2D> parseBendPoints(Relation relation, Link link) {
        List<Point2D> bendPointList = null;

        if (relation != null) {
            Attribute attribute = relation.getAttribute("_layoutHints:"
                    + PtolemyModelUtil.getLinkId(link));
            if ((attribute != null) && (attribute instanceof StringAttribute)) {
                bendPointList = new LinkedList<Point2D>();
                String bendPoints = ((StringAttribute) attribute)
                        .getExpression();
                String[] bendPointArray = bendPoints.split(";");
                for (String bendPointString : bendPointArray) {
                    String[] bendPointXY = bendPointString.split(",");
                    if (bendPointXY != null && bendPointXY.length == 2) {
                        Point2D bend = new Point2D.Double();
                        bend.setLocation(new Float(bendPointXY[0]).intValue(),
                                new Float(bendPointXY[1]).intValue());
                        bendPointList.add(bend);
                    }
                }
            }
        }
        return bendPointList;
    }

    /**
     * Return true if the bend points must be drawn in reversed order. This
     * is true if the start of drawing the line is the end. Ptolemy has no
     * notion of a start or the end of a link. The KIELER Layout has such a
     * notion. This method helps to detect such cases. Let ps be the start port
     * point pe the end port point. Let b1..n be the n bend points in between.
     * Normally length(ps,b1)+length(bn,pe) should be <
     * length(pe,b1)+length(bs,pe). If this is not the case then the order is
     * assumed to be reversed.
     * 
     * @param start the start
     * @param end the end
     * @param pointList the point list
     * @return true, if is reversed order
     */
    private boolean isReversedOrder(Point2D start, Point2D end,
            List<Point2D> pointList) {
        double d0 = length(start, pointList.get(0))
                + length(end, pointList.get(pointList.size() - 1));
        double d1 = length(end, pointList.get(0))
                + length(start, pointList.get(pointList.size() - 1));
        return (d0 > d1);
    }

    /**
     * Calculate the length between two points.
     * 
     * @param p0 the p0
     * @param p1 the p1
     * @return the double
     */
    private double length(Point2D p0, Point2D p1) {
        return length(p0.getX(), p0.getY(), p1.getX(), p1.getY());
    }

    /**
     * Calculate the length between two pairs of doubles describing two points.
     * 
     * @param x0 the x0
     * @param y0 the y0
     * @param x1 the x1
     * @param y1 the y1
     * @return the double
     */
    private double length(double x0, double y0, double x1, double y1) {
        return Math.sqrt(((x1 - x0) * (x1 - x0)) + ((y1 - y0) * (y1 - y0)));
    }

    ///////////////////////////////////////////////////////////////////
    ////                      private variables                    ////

    /**
     * The radius for filleting the corners of the connector.
     */
    private double _bendRadius = 10;

    /**
     * The location to attach the label to.
     */
    private Point2D _labelLocation;

}
