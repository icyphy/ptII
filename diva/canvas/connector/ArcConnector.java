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

import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import diva.canvas.CanvasUtilities;
import diva.canvas.Figure;
import diva.canvas.Site;
import diva.canvas.TransformContext;
import diva.canvas.toolbox.LabelFigure;

/** A Connector that draws itself in an arc. The connector
 * draws itself approximately through the center of the figures
 * that own the sites to which it is connected. The curvature of the
 * arc can be specified in one of two ways, depending on which
 * variable of the arc's shape remain constant as the distance
 * between the two figures at the ends of the arc is varied:
 *
 * <ol>
 * <li> Constant incident angle: The angle at which the arc connects
 * to the figure remains constant. This is the default behaviour,
 * and the default angle is 45 degrees.
 *
 * <li> Constant displacement at the maximum arc point, from
 * the straight line drawn between the two end points of the
 * connector. The default displacement is 20 (not for any good reason,
 * but is has to be something...).
 * </ol>
 *
 * <p>Currently, only the first is supported.</p>
 *
 * <p> The connector uses an instance of PaintedPath to draw itself,
 * so see that class for a more detailed description of the paint- and
 * stroke-related methods.</p>
 *
 * @version $Id$
 * @author  Edward Lee
 * @author  John Reekie
 * @Pt.AcceptedRating  Red
 */
public class ArcConnector extends AbstractConnector {
    // The minimum exit angle that a self-loop can take
    private static double MINSELFLOOPANGLE = Math.PI * 0.6;

    /** The arc shape that defines the connector shape
     */
    private Arc2D _arc;

    /** The flag that says whether this connector is a "self-loop"
     */
    private boolean _selfloop;

    /** The exit angle of the arc. This is the *difference* of the
     * angle of the line between the center of the head and tail
     * objects, and the angle at which the arc "exits" the figure
     * on the tail end. This parameter is used as the primary means
     * of controlling the shape or the arc.
     */
    private double _exitAngle = Math.PI / 5;

    /** The previous exit angle of the arc. This is used when the arc
     * switches between a self-loop and a non-self-loop.
     */
    private double _previousAngle = Math.PI * 0.75;

    /** The calculated parameters of the arc
     */
    private double _centerX;

    private double _centerY;

    private double _radius;

    private double _startAngle;

    private double _extentAngle;

    /** The angle between the two ends of the arc.
     */
    private double _gamma = 0.0;

    /** The threshold for when a source and a destination of an arc
     *  are considered close to one another.  This determines how self-loops
     *  get drawn.
     */
    protected static final double _CLOSE_THRESHOLD = 5.0;

    /** The midpoint site. */
    private ArcMidpointSite _midpointSite;

    /** The arc displacement
     */

    //// private double _displacement = 20.0;
    /** Create a new arc connector between the given
     * sites. The connector is drawn with a width of one
     * and in black, and at the default incident angle of
     * 45 degrees (PI/4 radians).
     */
    public ArcConnector(Site tail, Site head) {
        super(tail, head);
        _arc = new Arc2D.Double();
        setShape(_arc);
        route();
    }

    /** Get the angle at which the arc leaves the tail figure.
     */
    public double getAngle() {
        return _exitAngle;
    }

    /** Get the angle that determines the orientation of a
     * self-loop. This method should be used when saving an arc
     * to an external representation, if the arc is a self-loop.
     */
    public double getGamma() {
        return _gamma;
    }

    /** Return the midpoint of the arc.
     *  @return The midpoint of the arc.
     */
    public Point2D getArcMidpoint() {
        // Hm... I don't know why I need the PI/2 here -- johnr
        return new Point2D.Double(
                _centerX
                + _radius
                * Math.sin(_startAngle + _extentAngle / 2 + Math.PI / 2),
                _centerY
                + _radius
                * Math.cos(_startAngle + _extentAngle / 2 + Math.PI / 2));
    }

    /** Get the site that marks the midpoint of the connector.
     *  @return A site representing the midpoint of the arc.
     */
    public Site getMidpointSite() {
        if (_midpointSite == null) {
            // The 0 is an ID.  What role does this serve? EAL
            _midpointSite = new ArcMidpointSite(this, 0);
        }

        return _midpointSite;
    }

    /** Get the flag saying whether this arc is to be drawn as a self-loop
     */
    public boolean getSelfLoop() {
        return _selfloop;
    }

    /** Tell the connector to reposition its label if it has one.
     * The label is currently only positioned at the center of the arc.
     */
    @Override
    public void repositionLabel() {
        LabelFigure label = getLabelFigure();

        if (label != null) {
            Point2D pt = getArcMidpoint();
            label.translateTo(pt);

            // FIXME: Need a way to override the positioning.
            label.autoAnchor(_arc);
        }
    }

    /** Tell the connector to route itself between the
     * current positions of the head and tail sites.
     */
    @Override
    public void route() {
        repaint();

        TransformContext currentContext = getTransformContext();
        Site headSite = getHeadSite();
        Site tailSite = getTailSite();
        Figure tailFigure = tailSite.getFigure();
        Figure headFigure = headSite.getFigure();

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

        // Figure out the centers of the attached figures
        Point2D tailCenter;

        // Figure out the centers of the attached figures
        Point2D headCenter;

        if (tailFigure != null) {
            tailCenter = CanvasUtilities.getCenterPoint(tailFigure,
                    currentContext);
        } else {
            tailCenter = tailPt;
        }

        if (headFigure != null) {
            headCenter = CanvasUtilities.getCenterPoint(headFigure,
                    currentContext);
        } else {
            headCenter = headPt;
        }

        // Change self-loop mode if necessary
        boolean selfloop = _selfloop;

        if (tailFigure != null && headFigure != null) {
            selfloop = tailFigure == headFigure;
        }

        if (selfloop && !_selfloop) {
            setSelfLoop(true);
        } else if (!selfloop && _selfloop) {
            setSelfLoop(false);
        }

        // Figure out the angle between the centers. If a selfloop,
        // use the angle that was previously stored.
        double gamma;
        double x = headCenter.getX() - tailCenter.getX();
        double y = headCenter.getY() - tailCenter.getY();

        if (_selfloop) {
            gamma = _gamma;
        } else {
            gamma = Math.atan2(y, x);
        }

        // Tell the sites to adjust their positions
        double alpha = _exitAngle;
        double beta = Math.PI / 2.0 - alpha;
        double headNormal = gamma - alpha - Math.PI;
        double tailNormal = gamma + alpha;
        tailSite.setNormal(tailNormal);
        headSite.setNormal(headNormal);

        // Recompute the head and tail points
        if (currentContext != null) {
            tailPt = tailSite.getPoint(currentContext);
            headPt = headSite.getPoint(currentContext);
        } else {
            tailPt = tailSite.getPoint();
            headPt = headSite.getPoint();
        }

        // Adjust for decorations on the ends
        if (getHeadEnd() != null) {
            getHeadEnd().setNormal(headNormal);
            getHeadEnd().setOrigin(headPt.getX(), headPt.getY());
            getHeadEnd().getConnection(headPt);
        }

        if (getTailEnd() != null) {
            getTailEnd().setNormal(tailNormal);
            getTailEnd().setOrigin(tailPt.getX(), tailPt.getY());
            getTailEnd().getConnection(tailPt);
        }

        // Figure out the angle yet again (!)
        x = headPt.getX() - tailPt.getX();
        y = headPt.getY() - tailPt.getY();

        if (_selfloop && tailFigure != null && headFigure != null) {
            // In this case, don't remember the modified gamma!
            this._gamma = gamma;
            gamma = Math.atan2(y, x);
        } else {
            gamma = Math.atan2(y, x);
            this._gamma = gamma;
        }

        // Finally! Now that we have angle between the head and
        // tail of the connector, figure out what the center
        // of the arc is. First, compute assuming that gamma is
        // zero.
        double dx = Math.sqrt(x * x + y * y) / 2.0;
        double dy = -dx * Math.tan(beta);

        // That's the offset from the tail point to the center
        // of the arc's circle. Rotate it through gamma.
        double dxdash = dx * Math.cos(gamma) - dy * Math.sin(gamma);
        double dydash = dx * Math.sin(gamma) + dy * Math.cos(gamma);

        // Get the center
        double centerX = tailPt.getX() + dxdash;
        double centerY = tailPt.getY() + dydash;
        double radius = Math.sqrt(dx * dx + dy * dy);

        // Remember some parameters for later use
        this._centerX = centerX;
        this._centerY = centerY;
        this._radius = radius;
        this._startAngle = 3 * Math.PI / 2 - alpha - gamma;

        // NOTE: I don't know why I need to do this, I screwed
        // up the math somewhere... -- hjr
        if (_exitAngle < 0) {
            _startAngle += Math.PI;
        }

        // Draw self loops correctly.
        //        if (false && _selfloop) {
        //            if (alpha < 0.0) {
        //                _extentAngle = (2.0 * alpha) + (2 * Math.PI);
        //            } else {
        //                _extentAngle = (2.0 * alpha) - (2 * Math.PI);
        //            }
        //        } else {
        _extentAngle = 2.0 * alpha;
        //        }

        // Set the arc
        _arc.setArcByCenter(centerX, centerY, radius, _startAngle / Math.PI
                * 180, _extentAngle / Math.PI * 180, Arc2D.OPEN);

        // Move the label
        repositionLabel();

        // Woo-hoo!
        repaint();
    }

    /** Set the angle at which the arc leaves the tail figure, in
     *  radians. Because of the sign of the geometry, an arc with
     *  positive angle and with an arrowhead on its head will appear
     *  to be drawn counter-clockwise, and an arc with a negative
     *  angle will appear to be drawn clockwise. As a general rule,
     *  angles should be somewhat less than PI/2, and PI/4 a good
     *  general maximum figure.  If the angle is outside the range -PI
     *  to PI, then it is corrected to lie within that range.
     */
    public void setAngle(double angle) {
        while (angle > Math.PI) {
            angle -= 2.0 * Math.PI;
        }

        while (angle < -Math.PI) {
            angle += 2.0 * Math.PI;
        }

        _exitAngle = angle;
    }

    /** Set the angle that determines the orientation of a self-loop.
     * This value is roughly equal to the angle of the tangent to the
     * loop at it's mid-point. This method is only intended for use
     * when creating self-loop arcs from a saved representation.
     */
    public void setGamma(double gamma) {
        _gamma = gamma;
    }

    /** Set the flag that says that this arc is drawn as a "self-loop."
     * Apart from changing (slightly) the way the arc geometry is
     * determined, this method resets some internal variables so that
     * the arc doesn't get into a "funny state" when switching between
     * self-loops and non-self-loops.
     *
     * Not, however, that this method should only be called when the
     * arc changes, otherwise manipulation won't work properly. Use
     * getSelfLoop() to test the current state of this flag.
     */
    public void setSelfLoop(boolean selfloop) {
        // If becoming a self-loop, use the current angle if it is
        // wide enough, otherwise use the minimum angle or the
        // previous record angle, whichever is largest.
        double temp = _exitAngle;

        if (selfloop) {
            if (_exitAngle > 0 && _exitAngle < MINSELFLOOPANGLE) {
                setAngle(Math.max(_previousAngle, MINSELFLOOPANGLE));
            } else if (_exitAngle < 0 && _exitAngle > -MINSELFLOOPANGLE) {
                setAngle(Math.min(-_previousAngle, -MINSELFLOOPANGLE));
            }
        } else {
            // If switching to a non-selfloop, use the previous angle if
            // it is small. If it is large, use the current angle
            if (_previousAngle < MINSELFLOOPANGLE) {
                setAngle(_previousAngle);
            }
        }

        _previousAngle = temp;
        _selfloop = selfloop;
    }

    /** Translate the connector. This method is implemented, since
     * controllers may wish to translate connectors when the
     * sites at both ends are moved the same distance.
     */
    @Override
    public void translate(double x, double y) {
        Rectangle2D bounds = _arc.getBounds();
        repaint();
        _arc.setFrame(bounds.getX() + x, bounds.getY() + y, bounds.getWidth(),
                bounds.getHeight());

        if (getLabelFigure() != null) {
            getLabelFigure().translate(x, y);
        }

        repaint();
    }

    /** Translate the midpoint of the arc. This method is not exact,
     * but attempts to alter the shape of the arc so that the
     * midpoint moves by something close to the given amount.
     */
    public void translateMidpoint(double dx, double dy) {
        // Calculate some parameters
        TransformContext currentContext = getTransformContext();
        Site headSite = getHeadSite();
        Site tailSite = getTailSite();
        Figure tailFigure = tailSite.getFigure();
        Figure headFigure = headSite.getFigure();

        Point2D headPt;
        Point2D tailPt;

        // Get the transformed head and tail points
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

        if (tailFigure != null) {
            tailCenter = CanvasUtilities.getCenterPoint(tailFigure,
                    currentContext);
        } else {
            tailCenter = tailPt;
        }

        if (headFigure != null) {
            headCenter = CanvasUtilities.getCenterPoint(headFigure,
                    currentContext);
        } else {
            headCenter = headPt;
        }

        // Figure out the angle between the centers
        double x = headCenter.getX() - tailCenter.getX();
        double y = headCenter.getY() - tailCenter.getY();
        double gamma;

        if (_selfloop) {
            gamma = _gamma;
        } else {
            gamma = Math.atan2(y, x);
        }

        // Project the displacement onto the normal of the chord.
        double beta = Math.atan2(dy, dx);
        double magnitude = Math.sqrt(dx * dx + dy * dy);
        double shift = -magnitude * Math.sin(gamma - beta);
        double rotate = magnitude * Math.cos(gamma - beta);

        // Guess at an amount to change the angle by
        double delta;
        double absangle = Math.abs(_exitAngle);
        double newangle;

        if (!_selfloop) {
            if (absangle < Math.PI / 4) {
                delta = shift / 75.0;
            } else if (absangle < Math.PI / 2) {
                delta = shift / 120.0;
            } else if (absangle < Math.PI * 0.65) {
                delta = shift / 400.0;
            } else if (absangle < Math.PI * 0.80) {
                delta = shift / 1000.0;
            } else if (absangle < Math.PI * 0.90) {
                delta = shift / 4000.0;
            } else {
                delta = shift / 10000.0;
            }

            newangle = _exitAngle + delta;
        } else {
            if (absangle < Math.PI * 0.75) {
                delta = shift / 100.0;
            } else if (absangle < Math.PI * 0.85) {
                delta = shift / 300.0;
            } else if (absangle < Math.PI * 0.90) {
                delta = shift / 1000.0;
            } else if (absangle < Math.PI * 0.95) {
                delta = shift / 4000.0;
            } else {
                delta = shift / 10000;
            }

            newangle = _exitAngle + delta;

            if (newangle > 0 && newangle < Math.PI * 0.6) {
                newangle = Math.PI * 0.6;
            } else if (newangle < 0 && newangle > -Math.PI * 0.6) {
                newangle = -Math.PI * 0.6;
            }

            // That was for changing the size of the self-loop. This next
            // part is for changing the exit angle.
            if (tailFigure != null && headFigure != null) {
                double phi = rotate / _radius / 2.0;

                if (newangle < 0) {
                    _gamma += phi;
                } else {
                    _gamma -= phi;
                }
            }
        }

        setAngle(newangle);
    }
}
