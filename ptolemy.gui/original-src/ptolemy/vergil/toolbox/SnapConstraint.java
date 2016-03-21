/* A snap constraint for locatable nodes

 Copyright (c) 1998-2014 The Regents of the University of California.
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

 */
package ptolemy.vergil.toolbox;

import java.awt.geom.Point2D;

import diva.canvas.interactor.PointConstraint;

/**
 This constraint ensures that a point is a multiple of a constant
 that defaults to 5.0.

 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 @author Edward A. Lee
 */
public class SnapConstraint implements PointConstraint {
    /** Construct a new instance of a snap constraint.
     */
    public SnapConstraint() {
        super();
        _resolution = _defaultResolution;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Modify the specified point to snap to grid using the local
     *  resolution.
     *  @param point The point to modify.
     */
    @Override
    public void constrain(Point2D point) {
        double x = Math.round(point.getX() / _resolution) * _resolution;
        double y = Math.round(point.getY() / _resolution) * _resolution;
        point.setLocation(x, y);
    }

    /** Modify the specified point to snap to grid using the local
     *  resolution.
     *  @param point The point to modify (a dimension 2 array).
     *  @return The constrained point.
     */
    public double[] constrain(double[] point) {
        double[] result = new double[2];
        result[0] = Math.round(point[0] / _resolution) * _resolution;
        result[1] = Math.round(point[1] / _resolution) * _resolution;
        return result;
    }

    /** Modify the specified point to snap to grid using the local
     *  resolution.
     *  @param x The x dimension of the point to modify.
     *  @param y The y dimension of the point to modify.
     *  @return The constrained point.
     */
    public double[] constrain(double x, double y) {
        double[] result = new double[2];
        result[0] = Math.round(x / _resolution) * _resolution;
        result[1] = Math.round(y / _resolution) * _resolution;
        return result;
    }

    /** Modify the specified point to snap to grid using the global
     *  default resolution.
     *  @param point The point to modify.
     *  @return The constrained point.
     */
    public static Point2D constrainPoint(Point2D point) {
        double[] originalPoint = new double[2];
        originalPoint[0] = point.getX();
        originalPoint[1] = point.getY();

        double[] result = constrainPoint(originalPoint);
        return new Point2D.Double(result[0], result[1]);
    }

    /** Modify the specified point to snap to grid using the global
     *  default resolution.
     *  @param point The point to modify (a dimension 2 array).
     *  @return The constrained point.
     */
    public static double[] constrainPoint(double[] point) {
        return constrainPoint(point[0], point[1]);
    }

    /** Modify the specified point to snap to grid using the global
     *  default resolution.
     *  @param x The x dimension of the point to modify.
     *  @param y The y dimension of the point to modify.
     *  @return The constrained point.
     */
    public static double[] constrainPoint(double x, double y) {
        double[] result = new double[2];
        result[0] = Math.round(x / _defaultResolution) * _defaultResolution;
        result[1] = Math.round(y / _defaultResolution) * _defaultResolution;
        return result;
    }

    /** Return the default resolution.
     *  @return The global default resolution.
     */
    public static double getDefaultResolution() {
        return _defaultResolution;
    }

    /** Return the resolution for this instance.
     *  @return The global default resolution.
     *  @see #setResolution(double)
     */
    public double getResolution() {
        return _resolution;
    }

    /** Return true to indicate that this does snap to grid.
     *  @return True.
     */
    @Override
    public boolean snapped() {
        return true;
    }

    /** Set the resolution for this instance.
     *  @param resolution The new resolution.
     *  @see #getResolution()
     */
    public void setResolution(double resolution) {
        _resolution = resolution;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The default resolution globally. */
    private static double _defaultResolution = 5.0;

    /** The resolution for this instance. */
    private double _resolution = 5.0;
}
