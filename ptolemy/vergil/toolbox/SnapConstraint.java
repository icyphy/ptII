/* A snap constraint for locatable nodes

 Copyright (c) 1998-2003 The Regents of the University of California.
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
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.toolbox;

import diva.canvas.interactor.PointConstraint;

import java.awt.geom.Point2D;

/**
This constraint ensures that a point is a multiple of a constant
that defaults to 5.0.

@version $Id$
@since Ptolemy II 2.0
@author Edward A. Lee
*/
public class SnapConstraint implements PointConstraint {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Modify the specified point to snap to grid.
     *  @param point The point to modify.
     */
    public void constrain(Point2D point) {
        // Invoke the static version of this method.
        Point2D newPoint = constrainPoint(point);
        point.setLocation(newPoint.getX(), newPoint.getY());
    }

    /** Modify the specified point to snap to grid.
     *  This is a static version of the constrain() method.
     *  The constrain method cannot be static because it is defined
     *  in a base class.
     *  @param point The point to modify.
     */
    public static Point2D constrainPoint(Point2D point) {
        double[] originalPoint = new double[2];
        originalPoint[0] = point.getX();
        originalPoint[1] = point.getY();
        double[] result = constrainPoint(originalPoint);
        return new Point2D.Double(result[0], result[1]);
    }

    /** Modify the specified point to snap to grid.
     *  @param point The point to modify (a dimension 2 array).
     */
    public static double[] constrainPoint(double[] point) {
        return constrainPoint(point[0], point[1]);
    }

    /** Modify the specified point to snap to grid.
     *  @param x The x dimension of the point to modify.
     *  @param y The y dimension of the point to modify.
     */
    public static double[] constrainPoint(double x, double y) {
        double[] result = new double[2];
        result[0] = Math.round(x/_resolution)*_resolution;
        result[1] = Math.round(y/_resolution)*_resolution;
        return result;
    }

    /** Return true to indicate that this does snap to grid.
     *  @return True.
     */
    public boolean snapped() {
        return true;
    }

    /** Set the resolution. Note that this sets the snap resolution
     *  globally.
     *  @param resolution The new resolution.
     */
    public static void setResolution(double resolution) {
        _resolution = resolution;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The resolution. */
    private static double _resolution = 5.0;
}


