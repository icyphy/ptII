/* Utility methods for KIELER connector implementations. 
 
 Copyright (c) 2015 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

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
import java.awt.geom.Point2D;
import java.util.List;

import diva.canvas.Figure;
import diva.canvas.Site;
import diva.canvas.TransformContext;
import diva.canvas.connector.BasicManhattanRouter;
import diva.canvas.connector.Connector;
import diva.canvas.connector.PerimeterSite;

/**
 * Static helper class for the KIELER classes 
 * implementing connector behavior, i.e. special edge 
 * routing mechanisms.
 * 
 * @author Ulf Rueegg
 *
 */
public final class KielerLayoutUtil {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     * Get the center point of a Perimeter Site. Copied the idea from
     * {@link PerimeterSite#getPoint(double)}.
     *
     * @param site the site
     * @return the center point of the shape that corresponds to the site
     */
    public static Point2D getCenterPoint(Site site) {
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
    public static Point2D[] getHeadTailPoints(Connector connector, List<Point2D> bendPoints) {
        TransformContext currentContext = connector.getTransformContext();
        Point2D headPt, tailPt;
        Site headSite = connector.getHeadSite();
        Site tailSite = connector.getTailSite();
        if (currentContext != null) {
            headPt = getCenterPoint(headSite);
            tailPt = getCenterPoint(tailSite);
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
            headSite.setNormal(getNormal(headPt, headBend));
            tailSite.setNormal(getNormal(tailPt, tailBend));
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
    public static double getNormal(Point2D origin, Point2D other) {
        double normalX = other.getX() - origin.getX();
        double normalY = other.getY() - origin.getY();
        double theta = Math.atan2(normalY, normalX);
        return theta;
    }
}
