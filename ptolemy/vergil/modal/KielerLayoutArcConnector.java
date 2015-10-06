/* KIELER Bend Points with Arc Connector.

 Copyright (c) 1998-2015 The Regents of the University of California.
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
package ptolemy.vergil.modal;

import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.List;

import javax.swing.SwingConstants;

import diva.canvas.CanvasUtilities;
import diva.canvas.Site;
import diva.canvas.connector.ArcConnector;
import diva.canvas.toolbox.LabelFigure;
import ptolemy.kernel.Relation;
import ptolemy.vergil.actor.KielerLayoutUtil;
import ptolemy.vergil.actor.LayoutHint;
import ptolemy.vergil.actor.LayoutHint.LayoutHintItem;
import ptolemy.vergil.kernel.Link;

///////////////////////////////////////////////////////////////////
//// KielerLayoutArcConnector

/**
 * Extends the regular ArcConnector, allowing to draw spline
 * paths, i.e. series of bezier curves. 
 * 
 * @version $Id$
 * @author Ulf Rueegg
 * @see ptolemy.vergil.actor.KielerLayoutConnector
 * @since Ptolemy II 11.0
 * @Pt.ProposedRating red (uru)
 */

public class KielerLayoutArcConnector extends ArcConnector {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** 
     * Construct a new connector with the given tail and head for the specified
     * link. The connector is either drawn as a spline (in case 
     * KIELER layout information is available) or in the classic 
     * arc-style fashion as implemented by the super-class.
     *
     * @param tail The tail site.
     * @param head The head site.
     * @param link The link.
     */
    public KielerLayoutArcConnector(Site tail, Site head) {
        super(tail, head);
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

        // Remember the original arc shape if we are going to replace it 
        // with a spline path
        if (getShape() instanceof Arc2D) {
            _originalShape = getShape();
        }
        
        if (!considerBendPoints) {
            // In this case we have no bend point annotations available so
            // we use the normal draw functionality.
            setShape(_originalShape);
            _labelLocation = null;
            super.route();
        }
        
        if (considerBendPoints) {
            
            repaint();
            
            // The following code proceeds as follows:
            // We drop the first and the last point of the curve that 
            //  klay calculated. This is due to the #_applyEdgeLayoutBendPointAnnotation
            //  method ignoring the two anchor points of an edge.
            // Here we determine two anchors that are placed on the 
            //  actual boundary of the figure (as opposed to the rectangular 
            //  bounding box) and add them to the curve. This alters the 
            //  intended shape of the curve but should be fine.
            
            Site headSite = getHeadSite();
            Site tailSite = getTailSite();

            Point2D[] headTail = KielerLayoutUtil
                    .getHeadTailPoints(this, bendPointList);
            Point2D headCenter = headTail[0];
            Point2D tailCenter = headTail[1];

            // Rotate the edge's start/end decorator (if any).
            double tailNormal = 0, headNormal = 0;
            if (!bendPointList.isEmpty()) {
                headNormal = KielerLayoutUtil.getNormal(headTail[0],
                        bendPointList.get(0));
                tailNormal = KielerLayoutUtil.getNormal(headTail[1],
                        bendPointList.get(bendPointList.size() - 1));
            }
            
            tailSite.setNormal(tailNormal);
            headSite.setNormal(headNormal);
            
            // Adjust for decorations on the ends
            if (getHeadEnd() != null) {
                getHeadEnd().setNormal(headNormal);
                getHeadEnd().setOrigin(headCenter.getX(), headCenter.getY());
                getHeadEnd().getConnection(headCenter);
            }

            if (getTailEnd() != null) {
                getTailEnd().setNormal(tailNormal);
                getTailEnd().setOrigin(tailCenter.getX(), tailCenter.getY());
                getTailEnd().getConnection(tailCenter);
            }
            
            // In case there are connector decorators 
            //  at the endpoints of the edge,
            //  use the center point of the decorator as end/start point
            //  of the spline 
            // Note that it is _not_ used as the origin of the actual 
            //  ConnectorEnds
            if (getHeadEnd() != null) {
                double x = getHeadEnd().getBounds().getWidth() / 2f;

                Point2D connectorCenter = _rotatePoint(x, 0, headNormal);
                bendPointList.add(0, _sub(headCenter, connectorCenter));
            } else {
                bendPointList.add(0, headCenter);
            }

            if (getTailEnd() != null) {
                double x = getTailEnd().getBounds().getWidth() / 2f;

                Point2D connectorCenter = _rotatePoint(x, 0, headNormal);
                bendPointList.add(0, _sub(tailCenter, connectorCenter));
            } else {
                bendPointList.add(tailCenter);
            }
             
            // We can now create a path object for the spline points            
            Point2D[] pts = bendPointList.toArray(new Point2D[bendPointList.size()]);
            
            GeneralPath path = _createSplinePath(null, pts);
            
            // Now set the shape.
            setShape(path);
            
            // Move the label
            if (layoutHintItem.getLabelLocation() != null) {
                // either we got a position for the label from KIELER
                Point2D.Double loc = layoutHintItem.getLabelLocation();
                _labelLocation = new Point2D.Double(loc.x, loc.y);
            } else {
                // ... or we pick a location for the label in the middle of the connector.
                int count = bendPointList.size();
                Point2D point1 = bendPointList.get(count / 2 - 1);
                Point2D point2 = bendPointList.get(count / 2);

                _labelLocation = new Point2D.Double(
                        (point1.getX() + point2.getX()) / 2,
                        (point1.getY() + point2.getY()) / 2);
            }

            repositionLabel();

            repaint();
        } 
    }
    
    /** Tell the connector to reposition its label if it has one.
     * The label is currently only positioned at the center of the arc.
     */
    @Override
    public void repositionLabel() {
        LabelFigure label = getLabelFigure();

        if (label != null) {
            
            if (_labelLocation != null) {
                
                Point2D pos = (Point2D) _labelLocation.clone();
                CanvasUtilities.translate(pos, label.getPadding(), SwingConstants.NORTH_WEST);
                label.translateTo(pos);

                // the positions calculated by KIELER are always for the top-left corner of an element
                label.setAnchor(SwingConstants.NORTH_WEST);
                
            } else {
                Point2D pt = getArcMidpoint();
                label.translateTo(pt);
                
                // FIXME: Need a way to override the positioning.
                label.autoAnchor(getShape());
            }
            
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
     * Resets the given <code>thePath</code> and adds the required segments for drawing a spline.
     * If <code>thePath</code> is <code>null</code> a new path object is created.
     * 
     * Method is copied from KIELER's KLighD project, from class PolylineUtil.
     * 
     * @param thePath
     *            the path object to put the segments into, may be <code>null</code>
     * @param points
     *            an array of AWT Geometry {@link Point2D Point2Ds} 
     * @return the path object containing the required segments
     */
    private GeneralPath _createSplinePath(final GeneralPath thePath, final Point2D[] points) {

        final GeneralPath path = thePath != null ? thePath : new GeneralPath();
        
        path.reset();
        final int size = points.length;
        
        if (size < 1) {
            return path; // nothing to do
        }
        
        path.moveTo(points[0].getX(), points[0].getY());

        // draw cubic sections
        int i = 1;
        for (; i < size - 2; i += 3) { // SUPPRESS CHECKSTYLE MagicNumber
            path.curveTo(points[i].getX(), points[i].getY(),
                    points[i + 1].getX(), points[i + 1].getY(),
                    points[i + 2].getX(), points[i + 2].getY());
        }

        // in case there are not enough points for a final bezier curve,
        // draw something reasonable
        // size-1: one straight line
        // size-2: one quadratic
        switch (size - i) {
        case 1:
            path.lineTo(points[i].getX(), points[i].getY());
            break;
        case 2:
            path.quadTo(points[i].getX(), points[i].getY(),
                    points[i + 1].getX(), points[i + 1].getY());
            break;
        default:
            // this should not happen
            break;
        }
        
        return path;
    }

    /**
     * Rotates the passed point (x,y) by the given angle.
     * 
     * @param x coordinate
     * @param y coordinate
     * @param angle by which to rotate the point, in radians.
     * @return a newly created point rotated by the given angle.
     */
    private Point2D _rotatePoint(double x, double y, double angle) {
        double xnew = x * Math.cos(angle) - y * Math.sin(angle);
        double ynew = x * Math.sin(angle) + y * Math.cos(angle);
        
        return new Point2D.Double(xnew, ynew);
    }
    
    /**
     * @param p1 first point
     * @param p2 second point to be subtracted from p1
     * @return a new point where point p2 is component-wise subtracted from p1.
     */
    private Point2D _sub(Point2D p1, Point2D p2) {
        return new Point2D.Double(p1.getX() - p2.getX(), p1.getY() - p2.getY());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     * Whether automatic layout is currently in progress. If so, no layout hints
     * are removed.
     */
    private static boolean _layoutInProgress = false;
    
    /**
     * The original arc shape, cached, s.t. we can reuse it if no 
     * layout information is available.
     */
    private Shape _originalShape = null;
    
    /**
     * The location to be applied to the label if layout information is available.
     */
    private Point2D _labelLocation = new Point2D.Float();

}
