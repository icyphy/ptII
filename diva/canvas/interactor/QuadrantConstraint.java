/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 *
 */
package diva.canvas.interactor;

import java.awt.geom.Point2D;

import javax.swing.SwingConstants;

/**
 * Keep a point within one of the four quadrants relative to some
 * reference point.
 *
 * @version $Revision$
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
    public QuadrantConstraint (Point2D origin, int quadrant) {
        setOrigin(origin);
        setQuadrant(quadrant);
    }

    /** Ask the bounds constraint to constrain this point.
     */
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
        point.setLocation(x,y);
    }

    /** Get the origin
     */
    public Point2D getOrigin () {
         return _origin;
    }

    /** Get the quadrant
     */
    public int getQuadrant () {
         return _quadrant;
    }

    /** Set the origin
     */
    public void setOrigin (Point2D origin) {
        this._origin = origin;
        _originX = origin.getX();
        _originY = origin.getY();
    }

    /** Set the quadrant
     */
    public void setQuadrant (int quadrant) {
      if (quadrant < SwingConstants.NORTH_EAST
              || quadrant > SwingConstants.SOUTH_WEST) {
        throw new IllegalArgumentException(
                        "Quadrant " + quadrant + " not legal");
}
      this._quadrant = quadrant;
    }

    /** Return false. This constraint never snaps.
     */
    public boolean snapped () {
      return false;
    }
}


