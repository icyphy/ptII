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
 *
 */
package diva.canvas.connector;

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import diva.canvas.Site;
import diva.util.java2d.Polyline2D;

/** A Connector that draws itself with perpendicular lines.
 * To help it route itself, the connector contains an
 * instance of ManhattanRouter, which can be changed to create
 * other kinds of (or smarter) connectors.
 * By default the connector routes itself with rounded corners, which
 * tend to look a little nicer in complex views.  To get standard right
 * angles at the corners, set the bend radius to zero.
 *
 * @version $Id$
 * @author  John Reekie
 * @author  Michael Shilman
 */
public class ManhattanConnector extends AbstractConnector {
    /** The radius for filleting the corners of the connector.
     */
    private double _bendRadius = 50;

    /** The location to attach the label to.
     */
    private Point2D _labelLocation;

    /** The router used to route this connector.
     */
    private ManhattanRouter _router;

    /** Create a new manhattan connector between the given
     * sites. The connector is drawn with a width of one
     * and in black. The router is an instance of BasicManhattanRouter.
     * The connector is not routed until route() is called.
     * The corners of the connector will be rounded with a bend radius of 50.
     */
    public ManhattanConnector(Site tail, Site head) {
        this(tail, head, new BasicManhattanRouter());
    }

    /** Create a new manhattan connector between the given sites and
     * with the given router. The connector is drawn with a width of one
     * and in black. The connector is routed between the head and tail
     * sites.
     * The corners of the connector will be rounded with a bend radius of 50.
     */
    public ManhattanConnector(Site tail, Site head, ManhattanRouter router) {
        super(tail, head);
        setRouter(router);
    }

    /** Return the router.
     */
    public ManhattanRouter getRouter() {
        return _router;
    }

    /**
     * Return the maximum bend radius of the manhattan-routed
     * edge.  A value of zero means that the corners of the route
     * are square; the larger the value, the more curvy the corners
     * will be.
     */
    public double getBendRadius() {
        return _bendRadius;
    }

    /** Tell the connector to reposition the text label.
     */
    public void repositionLabel() {
        if (_labelLocation == null) {
            route();

            // route will call this method recursively.
            return;
        }

        if (getLabelFigure() != null) {
            getLabelFigure().translateTo(_labelLocation);
            getLabelFigure().autoAnchor(getShape());
        }
    }

    /**
     * Tell the connector to route itself between the
     * current positions of the head and tail sites.
     */
    public void route() {
        repaint();

        Polyline2D poly = (Polyline2D) _router.route(this);
        int count = poly.getVertexCount();

        if (count > 1) {
            // pick a location for the label in the middle of the connector.
            _labelLocation = new Point2D.Double((poly.getX(count / 2) + poly
                    .getX((count / 2) - 1)) / 2, (poly.getY(count / 2) + poly
                    .getY((count / 2) - 1)) / 2);
        } else {
            // attach the label to the only point of the connector.
            _labelLocation = new Point2D.Double(poly.getX(0), poly.getY(0));
        }

        if (_bendRadius == 0) {
            setShape(poly);
        } else {
            GeneralPath path = new GeneralPath();
            path.moveTo((float) poly.getX(0), (float) poly.getY(0));

            double prevX = poly.getX(0);
            double prevY = poly.getY(0);

            for (int i = 2; i < poly.getVertexCount(); i++) {
                //consider triplets of coordinates
                double x0 = prevX; //poly.getX(i-2);
                double y0 = prevY; //poly.getY(i-2);
                double x1 = poly.getX(i - 1);
                double y1 = poly.getY(i - 1);
                double x2 = poly.getX(i);
                double y2 = poly.getY(i);

                //midpoints
                x2 = (x1 + x2) / 2;
                y2 = (y1 + y2) / 2;

                //first make sure that the radius is not
                //bigger than half one of the arms of the triplets
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
                    //next calculate the intermediate points
                    //that define the bend.
                    double intX0 = x1 + ((r / d0) * (x0 - x1));
                    double intY0 = y1 + ((r / d0) * (y0 - y1));
                    double intX1 = x1 + ((r / d1) * (x2 - x1));
                    double intY1 = y1 + ((r / d1) * (y2 - y1));

                    //next draw the line from the previous
                    //coord to the intermediate coord, and
                    //curve around the corner
                    path.lineTo((float) intX0, (float) intY0);
                    path.curveTo((float) x1, (float) y1, (float) x1,
                            (float) y1, (float) intX1, (float) intY1);
                    prevX = x2;
                    prevY = y2;
                }
            }

            //finally close the last segment with a line.
            path.lineTo((float) poly.getX(poly.getVertexCount() - 1),
                    (float) poly.getY(poly.getVertexCount() - 1));

            //now set the shape
            setShape(path);
        }

        // Move the label
        repositionLabel();

        repaint();
    }

    /**
     * Set the maximum bend radius of the manhattan-routed
     * edge.  A value of zero means that the corners of the route
     * are square; the larger the value, the more curvy the corners
     * will be.
     *
     * @see #getBendRadius()
     */
    public void setBendRadius(double r) {
        if (r < 0) {
            throw new IllegalArgumentException("Illegal radius: " + r);
        }

        _bendRadius = r;
    }

    /** Set the router and route again.
     */
    public void setRouter(ManhattanRouter router) {
        this._router = router;
        setShape(_router.route(this));
    }

    /** Translate the connector. This method is implemented, since
     * controllers may wish to translate connectors when the
     * sites at both ends are moved the same distance.
     */
    public void translate(double x, double y) {
        repaint();

        Polyline2D line = (Polyline2D) getShape();
        line.translate(x, y);
        repaint();
    }
}
