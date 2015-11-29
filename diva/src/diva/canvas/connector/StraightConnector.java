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

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import diva.canvas.CanvasUtilities;
import diva.canvas.Site;
import diva.canvas.TransformContext;

/** A Connector that draws itself in a straight line.
 *
 * @version $Id$
 * @author  John Reekie
 * @author  Michael Shilman
 * @Pt.AcceptedRating  Red
 */
public class StraightConnector extends AbstractConnector {
    /** The transformed positions of the start and end of the
     * line, for use by the label positioning code.
     */
    private Point2D _headPt;

    private Point2D _tailPt;

    /** Create a new straight connector between the given
     * sites. The connector is drawn with a width of one
     * and in black.
     */
    public StraightConnector(Site tail, Site head) {
        super(tail, head);
        setShape(new Line2D.Double());
        route();
    }

    /** Tell the connector to reposition its label if it has one.
     * The label is currently only positioned at the center of the arc.
     */
    @Override
    public void repositionLabel() {
        if (getLabelFigure() != null) {
            Point2D pt = new Point2D.Double(
                    (_headPt.getX() + _tailPt.getX()) / 2,
                    (_headPt.getY() + _tailPt.getY()) / 2);
            getLabelFigure().translateTo(pt);
            getLabelFigure().autoAnchor(getShape());
        }
    }

    /** Tell the connector to route itself between the
     * current positions of the head and tail sites.
     */
    @Override
    public void route() {
        TransformContext currentContext = getTransformContext();
        Site headSite = getHeadSite();
        Site tailSite = getTailSite();
        Point2D headPt;
        Point2D tailPt;

        repaint();

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

        // Figure out the centers of the attached figures
        Point2D tailCenter;

        // Figure out the centers of the attached figures
        Point2D headCenter;

        if (tailSite.getFigure() != null) {
            tailCenter = CanvasUtilities.getCenterPoint(tailSite.getFigure(),
                    currentContext);
        } else {
            tailCenter = tailPt;
        }

        if (headSite.getFigure() != null) {
            headCenter = CanvasUtilities.getCenterPoint(headSite.getFigure(),
                    currentContext);
        } else {
            headCenter = headPt;
        }

        // Figure out the normal at the line ends.
        double x = headCenter.getX() - tailCenter.getX();
        double y = headCenter.getY() - tailCenter.getY();
        double angle = Math.atan2(y, x);

        // Tell the sites to adjust their positions
        tailSite.setNormal(angle);
        headSite.setNormal(angle - Math.PI);

        // Recompute the head and tail points
        if (currentContext != null) {
            tailPt = tailSite.getPoint(currentContext);
            headPt = headSite.getPoint(currentContext);
        } else {
            tailPt = tailSite.getPoint();
            headPt = headSite.getPoint();
        }

        // Remember these points for the label
        this._headPt = headPt;
        this._tailPt = tailPt;

        // Figure out the normal again
        x = headPt.getX() - tailPt.getX();
        y = headPt.getY() - tailPt.getY();
        angle = Math.atan2(y, x);

        // Adjust for decorations on the ends
        if (getHeadEnd() != null) {
            getHeadEnd().setNormal(angle + Math.PI);
            getHeadEnd().setOrigin(headPt.getX(), headPt.getY());
            getHeadEnd().getConnection(headPt);
        }

        if (getTailEnd() != null) {
            getTailEnd().setNormal(angle);
            getTailEnd().setOrigin(tailPt.getX(), tailPt.getY());
            getTailEnd().getConnection(tailPt);
        }

        // Change the line shape
        ((Line2D) getShape()).setLine(tailPt, headPt);

        // Move the label
        repositionLabel();

        repaint();
    }

    /** Translate the connector. This method is implemented, since
     * controllers may wish to translate connectors when the
     * sites at both ends are moved the same distance.
     */
    @Override
    public void translate(double x, double y) {
        repaint();

        Line2D line = (Line2D) getShape();
        line.setLine(line.getX1() + x, line.getY1() + y, line.getX2() + x,
                line.getY2() + y);

        if (getLabelFigure() != null) {
            getLabelFigure().translate(x, y);
        }

        repaint();
    }
}
