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
import java.awt.geom.Rectangle2D;

import diva.util.java2d.ShapeUtilities;

/** A damage region represents a region of the canvas that has
 * been "damaged" and hence needs to be repainted. Damage regions
 * are passed up from a component that creates the damage until
 * they reach the JCanvas, at which point the Swing RepaintManager
 * gets called.
 *
 * @version $Id$
 * @author John Reekie
 * @Pt.AcceptedRating Red
 */
public abstract class DamageRegion {
    /* The context in which this damage region was created
     */
    TransformContext _context;

    /** This class cannot be directly instantiated.
     */
    /** FIXME: Visual Cafe barfs on this
     private
     */
    DamageRegion() {
    }

    /** Tell the damage region to inflict itself on the given
     * JCanvas.
     */
    public abstract void apply(JCanvas canvas);

    /** Check transform cache validity. This must be called from
     * the repaint(DamageRegion) method of any component that
     * has a transform context.
     */
    public void checkCacheValid(TransformContext c) {
        _context.checkCacheValid(c);
    }

    /** Create a damage region in this context over the given rectangle.
     */
    public static DamageRegion createDamageRegion(TransformContext c,
            Rectangle2D r) {
        // FIXME: cache this object
        return new RectangleDamageRegion(c, r);
    }

    /** Create a damage region in this context over the given rectangle.
     */
    public static DamageRegion createDamageRegion(TransformContext c, double x,
            double y, double w, double h) {
        // FIXME: cache this object
        return new RectangleDamageRegion(c, x, y, w, h);
    }

    /** Extend the damage region with the given rectangle.
     */
    public abstract void extend(Rectangle2D r);

    /** Get the bounds of this damage region.
     */
    public abstract Rectangle2D getBounds();

    /** Get the transform context in which this damage region was created.
     */
    public TransformContext getContext() {
        return _context;
    }

    ///////////////////////////////////////////////////////////////////
    //// RectangleDamageRegion

    /**
     * A rectangular damage region. At the moment, this is the
     * only one we have.
     */
    private static class RectangleDamageRegion extends DamageRegion {
        /** The damaged rectangle
         */
        Rectangle2D _rectangle;

        /** Create it
         */
        private RectangleDamageRegion(TransformContext c, Rectangle2D r) {
            _context = c;
            _rectangle = r;
        }

        /** Create it
         */
        private RectangleDamageRegion(TransformContext c, double x, double y,
                double w, double h) {
            _context = c;
            _rectangle = new Rectangle2D.Double(x, y, w, h);
        }

        /** Apply it
         */
        @Override
        public void apply(JCanvas canvas) {
            Rectangle2D r = _rectangle;

            // Transform the damage rectangle if necessary
            AffineTransform t = _context.getScreenTransform();

            r = ShapeUtilities.transformBounds(r, t);

            // Take the next largest integer bounds and pass it to the canvas
            double x = r.getX();
            double y = r.getY();
            double w = r.getWidth();
            double h = r.getHeight();

            double xdash = Math.floor(x);
            double ydash = Math.floor(y);
            double xdash1 = Math.ceil(x + w) - xdash;
            double ydash1 = Math.ceil(y + h) - ydash;

            // urk -- we lost the time argument...
            canvas.repaint(0, (int) xdash, (int) ydash, (int) xdash1,
                    (int) ydash1);
        }

        /** Get the bounds of this damage region.
         */
        @Override
        public Rectangle2D getBounds() {
            return _rectangle;
        }

        /** Extend it
         */
        @Override
        public void extend(Rectangle2D r) {
            Rectangle2D.union(_rectangle, r, _rectangle);
        }

        /** Describe it
         */
        @Override
        public String toString() {
            return "Damage region: " + _rectangle;
        }
    }
}
