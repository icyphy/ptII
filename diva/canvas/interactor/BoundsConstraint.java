/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 *
 */
package diva.canvas.interactor;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Keep a point within a given rectangular bounds.
 *
 * @version $Revision$
 * @author John Reekie
 */
public class BoundsConstraint implements PointConstraint {

    /** The bounds
     */
    private Rectangle2D _bounds;
    private double _boundsX0;
    private double _boundsY0;
    private double _boundsX1;
    private double _boundsY1;

    /** Create a new BoundsConstraint with the given bounds.
     */
    public BoundsConstraint (Rectangle2D bounds) {
        setBounds(bounds);
    }

    /** Ask the bounds constraint to constrain this point.
     */
    public void constrain(Point2D point) {
        double x = point.getX();
        double y = point.getY();

        if (x < _boundsX0) {
            x = _boundsX0;
        }
        if (y < _boundsY0) {
            y = _boundsY0;
        }
        if (x > _boundsX1) {
            x = _boundsX1;
        }
        if (y > _boundsY1) {
            y = _boundsY1;
        }
        point.setLocation(x,y);
    }

    /** Get the bounds
     */
    public Rectangle2D getBounds () {
        return _bounds;
    }

    /** Return false. This constraint never snaps.
     */
    public boolean snapped () {
        return false;
    }

    /** Set the bounds
     */
    public void setBounds (Rectangle2D bounds) {
        this._bounds = bounds;
        _boundsX0 = bounds.getX();
        _boundsY0 = bounds.getY();
        _boundsX1 = _boundsX0 + bounds.getWidth();
        _boundsY1 = _boundsY0 + bounds.getHeight();
    }
}


