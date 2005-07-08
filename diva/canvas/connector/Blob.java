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

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import diva.util.java2d.Polygon2D;

/** An object that draws a blob of some kind on the end of
 * a connector. The blob can be one of several styles, such
 * as circle or diamond, and can either be draw filled
 * or not. This is a low-level utility class, not a self-contained
 * Figure.
 *
 * @version $Id$
 * @author  John Reekie
 */
public class Blob implements ConnectorEnd {
    /** Specify a circle style
     */
    public final static int BLOB_CIRCLE = 47;

    /** Specify a diamond style
     */
    public final static int BLOB_DIAMOND = BLOB_CIRCLE + 1;

    /** The style.
     */
    private int _style;

    /** The size unit
     */
    private double _unit = 8.0;

    /** Flag that says whether the blob is filled or not
     */
    private boolean _filled = true;

    /** x and y-origins
     */
    private double _originX = 0.0;

    private double _originY = 0.0;

    /** The normal to the line
     */
    private double _normal = 0.0;

    /** The shape to draw
     */
    private Shape _shape = null;

    /** A flag that says whether the shape is valid
     */
    private boolean _shapeValid = false;

    /** The stroke. This is needed to get the bounding box.  As the
     * default, we use a stroke width of 1.5 pixels, in order that
     * redraw is clean even in the presence of
     * anti-aliasing. Connectors that have non-unit stroke widths
     * should set this variable to a stroke with appropriate width.
     */
    public Stroke stroke = new BasicStroke(1.5f);

    /**
     * Create a new circle blob at (0,0).
     */
    public Blob() {
        this(0, 0, 0, BLOB_CIRCLE);
    }

    /**
     * Create a new blob at (0,0) in the given style.
     */
    public Blob(int style) {
        this(0, 0, 0, style);
    }

    /**
     * Create a new blob at the given coordinates and in the given style.
     */
    public Blob(double x, double y, double normal, int style) {
        _originX = x;
        _originY = y;
        _normal = normal;
        _style = style;
        reshape();
    }

    /** Get the bounding box of the shape used to draw
     * this connector end.
     */
    public Rectangle2D getBounds() {
        if (_filled) {
            // No outline, just use the shape
            return _shape.getBounds2D();
        } else {
            // Get the bonding box of the stroke
            Shape s = stroke.createStrokedShape(_shape);
            return s.getBounds2D();
        }
    }

    /** Get the connection point into the given point
     */
    public void getConnection(Point2D p) {
        if (!_shapeValid) {
            reshape();
        }

        switch (_style) {
        case BLOB_CIRCLE:
            p.setLocation(_originX + (2 * _unit), _originY);
            break;

        case BLOB_DIAMOND:
            p.setLocation(_originX + (3 * _unit), _originY);
            break;
        }

        AffineTransform at = new AffineTransform();
        at.setToRotation(_normal, _originX, _originY);
        at.transform(p, p);
    }

    /** Get the origin into the given point.
     */
    public void getOrigin(Point2D p) {
        p.setLocation(_originX, _originY);
    }

    /** Get the size unit.
     */
    public double getSizeUnit() {
        return _unit;
    }

    /** Get the style.
     */
    public int getStyle() {
        return _style;
    }

    /** Test if the blob is filled or not.
     */
    public boolean isFilled() {
        return _filled;
    }

    /** Paint the blob.  This method assumes that
     * the graphics context is already set up with the correct
     * paint and stroke.
     */
    public void paint(Graphics2D g) {
        if (!_shapeValid) {
            reshape();
        }

        if (_filled) {
            g.fill(_shape);
        } else {
            g.draw(_shape);
        }
    }

    /** Recalculate the shape of the blob.
     */
    public void reshape() {
        AffineTransform at = new AffineTransform();
        at.setToRotation(_normal, _originX, _originY);

        switch (_style) {
        case BLOB_CIRCLE:
            _shape = new Ellipse2D.Double(_originX, _originY - _unit,
                    2 * _unit, 2 * _unit);
            break;

        case BLOB_DIAMOND:

            Polygon2D polygon = new Polygon2D.Float();
            polygon.moveTo(_originX, _originY);
            polygon.lineTo(_originX + (1.5 * _unit), _originY - _unit);
            polygon.lineTo(_originX + (3.0 * _unit), _originY);
            polygon.lineTo(_originX + (1.5 * _unit), _originY + _unit);
            polygon.lineTo(_originX, _originY);
            polygon.closePath();
            _shape = polygon;
            break;
        }

        _shape = at.createTransformedShape(_shape);
    }

    /** Test the flag that sets whether to fill the blob.
     */
    public void setFilled(boolean flag) {
        _filled = flag;
    }

    /** Set the normal of the blob. The argument is the
     * angle in radians away from the origin.
     */
    public void setNormal(double angle) {
        _normal = angle;
        _shapeValid = false;
    }

    /** Set the end-point of the blob.
     */
    public void setOrigin(double x, double y) {
        translate(x - _originX, y - _originY);
    }

    /** Set the size unit. This unit is used differently
     * depending on the style, but is generally half the width
     * of the blob.
     */
    public void setSizeUnit(double s) {
        _unit = s;
        _shapeValid = false;
    }

    /** Set the style.
     */
    public void setStyle(int s) {
        _style = s;
        _shapeValid = false;
    }

    /** Translate the origin by the given amount.
     */
    public void translate(double x, double y) {
        _originX += x;
        _originY += y;

        if (_shapeValid) {
            if (_shape instanceof Polygon2D) {
                ((Polygon2D) _shape).translate(x, y);
            } else {
                AffineTransform at = AffineTransform.getTranslateInstance(x, y);
                _shape = at.createTransformedShape(_shape);
            }
        }
    }
}
