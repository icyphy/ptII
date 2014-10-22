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

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import diva.canvas.interactor.Interactor;

/** AbstractFigure is an abstract superclass implementing
 * the Figure interface. Each subclass is assumed to have some
 * persistent screen representation.  They are responsible for knowing
 * how to repaint themselves on the screen and find out where they and
 * how to move themselves.  They are not required to know when to
 * repaint, as that is done by the canvas they are drawn on.
 *
 * @version $Id$
 * @author John Reekie
 * @Pt.AcceptedRating Yellow
 */
public abstract class AbstractFigure implements Figure {
    /** The interactor
     */
    private Interactor _interactor = null;

    /** The user object
     */
    private Object _userObject = null;

    /** The visibility flag.
     */
    private boolean _visibility = true;

    /** This figure's parent
     */
    private CanvasComponent _parent = null;

    /** The tooltip
     */
    private String _toolTipText = null;

    /** Test whether this figure contains the point given.
     *  This default implementation
     *  tests using the figure's shape.
     */
    @Override
    public boolean contains(Point2D p) {
        return getShape().contains(p);
    }

    /** Get the bounding box of this figure.  This default
     * implementation returns the bounding box of the figure's
     * outline shape.
     */
    @Override
    public Rectangle2D getBounds() {
        return getShape().getBounds2D();
    }

    /** Return the interactor of this figure. Return
     *  null if there isn't one.
     */
    @Override
    public Interactor getInteractor() {
        return _interactor;
    }

    /** Get the most immediate layer containing this figure.
     * Returns null if this figure is not in a layer, either
     * directly or as one of its ancestors.
     */
    @Override
    public CanvasLayer getLayer() {
        if (_parent == null) {
            return null;
        } else if (_parent instanceof CanvasLayer) {
            return (CanvasLayer) _parent;
        } else {
            // If the call to setParent was bogus, then this might
            // throw an exception. Let the caller deal with it.
            return ((Figure) _parent).getLayer();
        }
    }

    /** Return the origin of the figure in the enclosing transform
     *  context, which in this base class is the center of the bounds
     *  returned by getBounds().
     *  @see #getBounds()
     *  @return The origin of the figure.
     */
    @Override
    public Point2D getOrigin() {
        Rectangle2D bounds = getBounds();
        return new Point2D.Double(bounds.getCenterX(), bounds.getCenterY());
    }

    /** Return the parent of this figure. Return null if the figure
     *  does not have a parent.  (Note that a figure with no parent
     *  can exist, but it will not be displayed, as it must be in a
     *  layer for the figure canvas to ever call its paint method.)
     */
    @Override
    public CanvasComponent getParent() {
        return _parent;
    }

    /** Get the outline shape of this figure. The outline shape is
     *  used for things like highlighting.  This is an abstract
     * method, provided  here since a number of other concrete
     * methods use it.
     */
    @Override
    public abstract Shape getShape();

    /** Return the transform context of the figure. This default
     * implementation assumes that the figure does not implement
     * a transform context, so just calls the same method on its
     * parent. If it has no parent, return null.
     */
    @Override
    public TransformContext getTransformContext() {
        if (_parent == null) {
            return null;
        } else {
            return _parent.getTransformContext();
        }
    }

    /** Return the tooltip string for this figure, or null if the figure
     *  does not have a tooltip.
     */
    @Override
    public String getToolTipText() {
        return _toolTipText;
    }

    /** Get the user object of this figure. Return null if
     *  there is none.
     */
    @Override
    public Object getUserObject() {
        return _userObject;
    }

    /** Test if this figure intersects the given rectangle, and the
     *  interior of the figure is not transparent to hits. This is the
     *  same as intersects() if the interior of the figure is not
     *  transparent.  If the figure is not visible, return false.
     *  This default implementation is the same as <b>intersects</b>
     *  if the figure is visible.
     */
    @Override
    public boolean hit(Rectangle2D r) {
        if (!isVisible()) {
            return false;
        }

        return intersects(r);
    }

    /** Test if this figure intersects the given rectangle. This default
     *  implementation uses the figure's outline shape.
     */
    @Override
    public boolean intersects(Rectangle2D r) {
        return getShape().intersects(r);
    }

    /** Test the visibility flag of this figure. Note that this flag
     *  does not indicate whether the figure is actually visible on
     *  the screen, as one if its ancestors may not be visible.
     */
    @Override
    public boolean isVisible() {
        return _visibility;
    }

    /** Paint the figure. This is an abstract method. Implementing
     *  subclasses should note that the graphics context may already
     *  have a current transform, so if this figure needs to perform a
     *  transform, it must be cascaded with the current transform and
     *  the transform restored at the end of this method. The graphics
     *  context may also contain a clip region, so any clipping must
     *  be done with the intersection of the current clip and the clip
     *  desired by this method. and the clip restored. Other state in
     *  the graphics context, such as the stroke and fill, do not need
     *  to be preserved.
     */
    @Override
    public abstract void paint(Graphics2D g);

    /** Repaint the figure in the given rectangle. This default
     *  implementation repaints the whole figure. Subclasses should
     *  consider overriding this method to optimize redraw. See the
     *  documentation of the one-argument version of this method.
     */
    @Override
    public void paint(Graphics2D g, Rectangle2D r) {
        paint(g);
    }

    /** Schedule a repaint of the figure. This should be called after
     * performing modifications on the figure, so that the figure
     * is scheduled for redrawing. This default implementation
     * creates a damage region that is the same size as this figure's
     * bounding box, and forwards that to the parent. Subclasses only
     * need to override this method if they need to damage a region that
     * is different from the bounding box.
     */
    @Override
    public void repaint() {
        if (_parent != null) {
            repaint(DamageRegion.createDamageRegion(
                    _parent.getTransformContext(), getBounds()));
        }
    }

    /** Schedule a repaint of the figure within the given damage
     * region. This default implementation
     * forwards the damage region to this figure's parent. See the
     * nullary version of this method.
     */
    @Override
    public void repaint(DamageRegion d) {
        if (_parent != null) {
            _parent.repaint(d);
        }
    }

    /** Set the interactor of this figure. Once a figure has an
     *  interactor given to it, it will respond to events
     *  on the figure canvas.
     */
    @Override
    public void setInteractor(Interactor interactor) {
        _interactor = interactor;
    }

    /** Set the parent of this figure.  A null argument means that the
     * figure is being removed from its parent. No checks are performed
     * to see if the figure already has a parent -- it is the
     * responsibility of the caller to do this, and to change the layer
     * reference of the figure if necessary. This method is intended only
     * for use by classes that implement FigureContainer.
     */
    @Override
    public void setParent(CanvasComponent fc) {
        _parent = fc;

        TransformContext c = getTransformContext();

        if (c != null) {
            c.invalidateCache();
        }
    }

    /** Set the user object. This object is intended for use as a
     * reference to the semantic model.
     */
    @Override
    public void setUserObject(Object o) {
        _userObject = o;
    }

    /** Set the tooltip string for this figure.  If the string is null, then
     *  the figure will not have a tooltip.
     */
    @Override
    public void setToolTipText(String s) {
        _toolTipText = s;
    }

    /** Set the visibility flag of this figure. If the flag is false,
     *  then the figure will not be drawn on the screen and it will
     *  not respond to user input events.
     */
    @Override
    public void setVisible(boolean flag) {
        _visibility = flag;
    }

    /** Transform the figure with the supplied transform. This can
     * be used to perform arbitrary translation, scaling, shearing, and
     * rotation operations.
     */
    @Override
    public abstract void transform(AffineTransform at);

    /** Move the figure the indicated distance. The default
     *  implementation uses the <b>transform</b> method, so
     *  most subclasses can probably implement this more
     *  efficiently.
     */
    @Override
    public void translate(double x, double y) {
        transform(AffineTransform.getTranslateInstance(x, y));
    }
}
