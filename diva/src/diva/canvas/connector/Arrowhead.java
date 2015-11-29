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
 *
 */
package diva.canvas.connector;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import diva.util.java2d.Polygon2D;

/** An arrowhead that is drawn on the end of a connector.
 * This is a low-level utility class, not a self-contained Figure.
 *
 * @version $Id$
 * @author  John Reekie
 */
public class Arrowhead implements ConnectorEnd {

    /** Flag that says whether the arrowhead is filled or not.
     */
    private boolean _filled = true;

    /** The arrowhead length, and its x and y components
     */
    private double _length = 12.0;

    /** x and y-origins
     */
    private double _originX = 0.0;

    private double _originY = 0.0;

    /** The normal to the line
     */
    private double _normal = 0.0;

    /** The shape to draw
     */
    private Polygon2D _polygon = null;

    /** A flag that says whether the shape is valid
     */
    private boolean _polygonValid = false;

    /** A flag that says to slip the direction in which the
     * arrowhead is drawn.
     */
    private boolean _flipped = false;

    /** The stroke. This is needed to get the bounding box.  As the
     * default, we use a stroke width of 3.0 pixels, in order that
     * redraw is clean even in the presence of
     * anti-aliasing. Connectors that have non-unit stroke widths
     * should set this variable to a stroke with appropriate width.
     */
    private Stroke _stroke = new BasicStroke(3.0f);

    /**
     * Create a new arrowhead at (0,0).
     */
    public Arrowhead() {
        this(0.0, 0.0, 0.0);
    }

    /**
     * Create a new arrowhead at the given point and
     * with the given normal.
     */
    public Arrowhead(double x, double y, double normal) {
        _originX = x;
        _originY = y;
        _normal = normal;
        reshape();
    }

    /** Get the bounding box of the shape used to draw
     * this connector end.
     */
    @Override
    public Rectangle2D getBounds() {
        if (_filled) {
            // No outline, just use the shape
            return _polygon.getBounds2D();
        } else {
            // Get the bonding box of the stroke
            Shape s = _stroke.createStrokedShape(_polygon);
            return s.getBounds2D();
        }
    }

    /** Get the connection point into the given point
     */
    @Override
    public void getConnection(Point2D p) {
        if (!_polygonValid) {
            reshape();
        }

        // Set the point.
        p.setLocation(_originX + _length, _originY);

        AffineTransform at = new AffineTransform();
        at.setToRotation(_normal, _originX, _originY);
        at.transform(p, p);
    }

    /** Get the flag saying to flip the arrowhead.
     */
    public boolean getFlipped() {
        return _flipped;
    }

    /** Get the origin into the given point.
     */
    @Override
    public void getOrigin(Point2D p) {
        p.setLocation(_originX, _originY);
    }

    /** Get the length.
     */
    public double getLength() {
        return _length;
    }

    /** Test if the blob is filled or not.
     */
    public boolean isFilled() {
        return _filled;
    }

    /** Paint the arrow-head.  This method assumes that
     * the graphics context is already set up with the correct
     * paint and stroke.
     */
    @Override
    public void paint(Graphics2D g) {
        if (!_polygonValid) {
            reshape();
        }
        if (_filled) {
            Paint oldPaint = g.getPaint();
            g.fill(_polygon);
            g.setPaint(oldPaint);
        } else {
            g.draw(_polygon);
        }
    }

    /** Recalculate the shape of the decoration.
     */
    public void reshape() {
        AffineTransform at = new AffineTransform();
        at.setToRotation(_normal, _originX, _originY);

        double l1 = _length * 1.0;
        double l2 = _length * 1.3;
        double w = _length * 0.4;

        if (_flipped) {
            l1 = -l1;
            l2 = -l2;
            at.translate(_length, 0.0);
        }

        _polygon = new Polygon2D.Double();
        _polygon.moveTo(_originX, _originY);
        _polygon.lineTo(_originX + l2, _originY + w);
        _polygon.lineTo(_originX + l1, _originY);
        _polygon.lineTo(_originX + l2, _originY - w);
        _polygon.closePath();
        _polygon.transform(at);
    }

    /** Set the normal of the decoration. The argument is the
     * angle in radians away from the origin. The arrowhead is
     * drawn so that the body of the arrowhead is in the
     * same direction as the normal -- that is, the arrowhead
     * appears to be pointed in the opposite direction to its
     * "normal."
     */
    @Override
    public void setNormal(double angle) {
        _normal = angle;
        _polygonValid = false;
    }

    /** Set the flag that determines whether to fill the arrowhead.
     */
    public void setFilled(boolean flag) {
        _filled = flag;
    }

    /** Set the flag that says the arrowhead is "flipped." This means
     * that the arrowhead is drawn so that the apparent direction
     * of the arrowhead is the same as its normal.
     */
    public void setFlipped(boolean flag) {
        _flipped = flag;
        _polygonValid = false;
    }

    /** Set the origin of the decoration.
     */
    @Override
    public void setOrigin(double x, double y) {
        translate(x - _originX, y - _originY);
    }

    /** Set the length of the arrowhead.
     */
    public void setLength(double l) {
        _length = l;
        _polygonValid = false;
    }

    /** Translate the origin by the given amount.
     */
    @Override
    public void translate(double x, double y) {
        _originX += x;
        _originY += y;

        if (_polygonValid) {
            _polygon.translate(x, y);
        }
    }
}
