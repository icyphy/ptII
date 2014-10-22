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
package diva.canvas;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

/** An abstract implementation of Site. This class provides default
 * implementations of several methods in the Site interface, to
 * make it easier to implement site classes.
 *
 * @version $Id$
 * @author John Reekie
 * @Pt.AcceptedRating Red
 */
public abstract class AbstractSite implements Site {
    /** The normal of the site. This is 0.0 by default.
     */
    protected double _normal = 0.0;

    /** True if the site has had its normal set by setNormal.
     *  Default is false.
     */
    protected boolean _hasNormal = false;

    /** Get the figure to which this site is attached.
     */
    @Override
    public abstract Figure getFigure();

    /** Get the ID of this site.
     */
    @Override
    public abstract int getID();

    /** Get the angle of the normal to this site, in radians
     * between zero and 2pi. This default method returns 0.0.
     */
    @Override
    public double getNormal() {
        return _normal;
    }

    /** Get the point location of the site, in the enclosing
     * transform context with the default normal.  This method uses
     * the getPoint(double) method, so subclasses only have to override
     * that method.
     */
    @Override
    public Point2D getPoint() {
        return getPoint(getNormal());
    }

    /** Get the point location of the site, in the given
     * transform context with the default normal.
     * The given context must be an enclosing
     * context of the site.  This method uses
     * the getPoint(double) method, so subclasses only have to override
     * that method.
     */
    @Override
    public Point2D getPoint(TransformContext tc) {
        return getTransformContext().getTransform(tc).transform(getPoint(),
                null);

        // Formerly used deprecated method. EAL 6/12/05
        // return CanvasUtilities.transformInto(getPoint(), getTransformContext(), tc);
    }

    /** Get the point location of the site, in the enclosing
     * transform context with the given normal.
     */
    @Override
    public Point2D getPoint(double normal) {
        return new Point2D.Double(getX(), getY());
    }

    /** Get the point location of the site, in the given
     * transform context with the given normal.
     * The given context must be an enclosing
     * context of the site.  This method uses
     * the getPoint(double) method, so subclasses only have to override
     * that method.
     */
    @Override
    public Point2D getPoint(TransformContext tc, double normal) {
        AffineTransform transform = getTransformContext().getTransform(tc);
        Point2D point = getPoint(normal);
        return transform.transform(point, point);
    }

    /** Get the enclosing transform context of this site.
     *  As a default behavior, return the transform context
     *  of the associated figure.
     */
    @Override
    public TransformContext getTransformContext() {
        return getFigure().getParent().getTransformContext();
    }

    /** Get the x-coordinate of the site, in the enclosing
     * transform context.
     */
    @Override
    public abstract double getX();

    /** Get the y-coordinate of the site, in the enclosing
     * transform context.
     */
    @Override
    public abstract double getY();

    /** Test if this site has a "normal" to it. Return true if
     * setNormal has been called and false otherwise.
     */
    @Override
    public boolean hasNormal() {
        return _hasNormal;
    }

    /** Test if this site has a normal in the given direction.
     * This default implementation returns false.
     */
    @Override
    public boolean isNormal(int direction) {
        return false;
    }

    /** Set the normal "out" of the site. The site effectively
     * moves so that it passes through the center of the given figure.
     * The normal is limited to be between -pi and pi.  A normal of zero
     * points to the east, and a normal of pi/2 points to the south.  This
     * "upside down" coordinate system is consistent with the upside down
     * coordinate system of the canvas, which has the origin in the upper left.
     */
    @Override
    public void setNormal(double normal) {
        _hasNormal = true;
        _normal = CanvasUtilities.moduloAngle(normal);
    }

    /** Translate the site by the indicated distance. This
     * default implementation does nothing.
     */
    @Override
    public void translate(double x, double y) {
        // do nothing
    }
}
