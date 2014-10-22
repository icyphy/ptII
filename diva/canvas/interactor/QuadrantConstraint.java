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
package diva.canvas.interactor;

import java.awt.geom.Point2D;

import javax.swing.SwingConstants;

/**
 * Keep a point within one of the four quadrants relative to some
 * reference point.
 *
 * @version $Id$
 * @author John Reekie
 */
public class QuadrantConstraint implements PointConstraint {
    /** The point
     */
    private Point2D _origin;

    private double _originX;

    private double _originY;

    /** The quadrant
     */
    private int _quadrant;

    /** Create a new QuadrantConstraint with the given origin and
     * quadrant.
     */
    public QuadrantConstraint(Point2D origin, int quadrant) {
        setOrigin(origin);
        setQuadrant(quadrant);
    }

    /** Ask the bounds constraint to constrain this point.
     */
    @Override
    public void constrain(Point2D point) {
        double x = point.getX();
        double y = point.getY();

        // Constrain x
        switch (_quadrant) {
        case SwingConstants.NORTH_EAST:
        case SwingConstants.SOUTH_EAST:

            if (x < _originX) {
                x = _originX;
            }

            break;

        case SwingConstants.NORTH_WEST:
        case SwingConstants.SOUTH_WEST:

            if (x > _originX) {
                x = _originX;
            }

            break;
        }

        // Constrain y
        switch (_quadrant) {
        case SwingConstants.NORTH_EAST:
        case SwingConstants.NORTH_WEST:

            if (y > _originY) {
                y = _originY;
            }

            break;

        case SwingConstants.SOUTH_EAST:
        case SwingConstants.SOUTH_WEST:

            if (y < _originY) {
                y = _originY;
            }

            break;
        }

        point.setLocation(x, y);
    }

    /** Get the origin
     */
    public Point2D getOrigin() {
        return _origin;
    }

    /** Get the quadrant
     */
    public int getQuadrant() {
        return _quadrant;
    }

    /** Set the origin
     */
    public void setOrigin(Point2D origin) {
        this._origin = origin;
        _originX = origin.getX();
        _originY = origin.getY();
    }

    /** Set the quadrant
     */
    public void setQuadrant(int quadrant) {
        if (quadrant < SwingConstants.NORTH_EAST
                || quadrant > SwingConstants.SOUTH_WEST) {
            throw new IllegalArgumentException("Quadrant " + quadrant
                    + " not legal");
        }

        this._quadrant = quadrant;
    }

    /** Return false. This constraint never snaps.
     */
    @Override
    public boolean snapped() {
        return false;
    }
}
