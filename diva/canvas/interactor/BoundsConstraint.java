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
import java.awt.geom.Rectangle2D;

/**
 * Keep a point within a given rectangular bounds.
 *
 * @version $Id$
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
    public BoundsConstraint(Rectangle2D bounds) {
        setBounds(bounds);
    }

    /** Ask the bounds constraint to constrain this point.
     */
    @Override
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

        point.setLocation(x, y);
    }

    /** Get the bounds
     */
    public Rectangle2D getBounds() {
        return _bounds;
    }

    /** Return false. This constraint never snaps.
     */
    @Override
    public boolean snapped() {
        return false;
    }

    /** Set the bounds
     */
    public void setBounds(Rectangle2D bounds) {
        this._bounds = bounds;
        _boundsX0 = bounds.getX();
        _boundsY0 = bounds.getY();
        _boundsX1 = _boundsX0 + bounds.getWidth();
        _boundsY1 = _boundsY0 + bounds.getHeight();
    }
}
