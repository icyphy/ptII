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

import java.awt.geom.Point2D;

/**
 * This is an abstract decorator for sites.  It can be used to add or change
 * the functionality of an arbitrary site.
 *
 * @version $Id$
 * @author  Steve Neuendorffer
 * @Pt.AcceptedRating  Red
 */
public class SiteDecorator implements Site {
    // This class was deprecated with the message
    // "This is complete unneeded. Do not use."  However, it has been
    // deprecated for a long time and still has users, so I'm undeprecating it.
    private Site _site;

    /** Create a new decorator on the given site.
     */
    public SiteDecorator(Site site) {
        _site = site;
    }

    /** Get the figure to which this site is attached. Usually, this
     * will return a valid Figure, but clients must be aware that
     * certain types of site may return null.
     */
    @Override
    public Figure getFigure() {
        return _site.getFigure();
    }

    /** Get the ID of this site. Within each figure, the IDs of
     * the sites must be unique.
     */
    @Override
    public int getID() {
        return _site.getID();
    }

    /** Get the angle of the normal to this site, in radians
     * between zero and 2pi. The direction is "out" of the site.
     * The result is meaningful only if hasNormal() returns true.
     */
    @Override
    public double getNormal() {
        return _site.getNormal();
    }

    /** Get the point location of the site, in the enclosing
     * transform context with default normal.
     */
    @Override
    public Point2D getPoint() {
        return _site.getPoint();
    }

    /** Get the point location of the site, in the given
     * transform context with the default normal.
     * The given context must be an enclosing
     * context of the site.
     */
    @Override
    public Point2D getPoint(TransformContext tc) {
        return _site.getPoint(tc);
    }

    /** Get the point location of the site, in the enclosing
     * transform context with the given normal.
     */
    @Override
    public Point2D getPoint(double normal) {
        return _site.getPoint(normal);
    }

    /** Get the point location of the site, in the given
     * transform context with the given normal.
     * The given context must be an enclosing
     * context of the site.
     */
    @Override
    public Point2D getPoint(TransformContext tc, double normal) {
        return _site.getPoint(tc, normal);
    }

    /** Get the enclosing transform context of this site.
     */
    @Override
    public TransformContext getTransformContext() {
        return _site.getTransformContext();
    }

    /** Get the x-coordinate of the site, in the enclosing
     * transform context.
     */
    @Override
    public double getX() {
        return _site.getX();
    }

    /** Get the y-coordinate of the site, in the enclosing
     * transform context.
     */
    @Override
    public double getY() {
        return _site.getY();
    }

    /** Test if this site has a "normal" to it. The normal
     * is accessible by the methods getNormal()
     * and isNormal(). Generally, sites on the boundary of
     * a shape will return true to this method, and sites
     * in the center of an object will return false.
     */
    @Override
    public boolean hasNormal() {
        return _site.hasNormal();
    }

    /** Test if this site has a normal in the given direction.
     * The direction is that given by one of the static constants
     * NORTH, SOUTH, EAST, or WEST, defined in
     * <b>javax.swing.SwingConstants</b>
     */
    @Override
    public boolean isNormal(int direction) {
        return _site.isNormal(direction);
    }

    /** Set the normal "out" of the site. If the site cannot
     * change its normal, it can ignore this call, so clients
     * that care should always check the normal after calling.
     * If the site can change its normal, it can also change
     * its position. For example, a site on the perimeter of a
     * figure may move to a different position.
     */
    @Override
    public void setNormal(double normal) {
        _site.setNormal(normal);
    }

    /** Translate the site by the indicated distance, where distances
     * are in the local coordinates of the containing pane. Usually,
     * this will mean that the figure is reshaped so that the
     * site moves the given distance. If the site cannot
     * be moved the given distance, then either do nothing, or move
     * it part of the distance. Clients are expected to check the
     * new location of the site.
     */
    @Override
    public void translate(double x, double y) {
        _site.translate(x, y);
    }
}
