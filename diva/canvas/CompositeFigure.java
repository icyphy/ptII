/*
 Copyright (c) 1998-2001 The Regents of the University of California
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
import java.util.Iterator;

import diva.util.Filter;
import diva.util.UnitIterator;
import diva.util.java2d.ShapeUtilities;

/** A CompositeFigure is a figure that contains a set of child
 * figures.  It uses a ZList as the internal representation of its
 * children, and implements wrappers for many of the z-list methods to
 * provide flexible access to the contained figures. Note that this
 * class exposes the ability to add and remove children by any client,
 * and is thus intended for use in situations in which the number of
 * child figures is either large or unpredictable. For applications in
 * which the number of children is smaller and predictable, and more
 * restricted access to the children is require, applications should
 * implement a custom subclass of AbstractFigureContainer. For an
 * example, see diva.canvas.toolbox.IconFigure.
 *
 * @version $Id$
 * @author John Reekie
 * @rating Yellow
 */
public class CompositeFigure extends AbstractFigureContainer {

    /** The background figure
     */
    private Figure _background = null;

    /** The cached bounding box
     */
    private Rectangle2D _cachedBounds = null;

    /** The children.
     */
    private ZList _children;

    /** The transform context
     */
    private TransformContext _transformContext = new TransformContext(this);

    /** Create a new composite figure containing no figures.
     */
    public CompositeFigure () {
        _children = new BasicZList();
    }

    /** Create a new composite figure containing no figures,
     * that uses the given z-list for its storage. If you have
     * a composite figure that you know is going to contain a
     * lot of children, you can give it an optimized z-list.
     */
    public CompositeFigure (ZList zlist) {
        _children = zlist;
    }

    /**
     * Construct a composite figure with the given figure as its
     * background.
     */
    public CompositeFigure(Figure background) {
        this();
        setBackgroundFigure(background);
    }

    /** Add a child figure to this composite.
     */
    public void add (Figure f) {
        _children.add(f);
        f.setParent(this);
        _cachedBounds = null;
        f.repaint();
    }

    /** Insert a figure at the given position.
     */
    public void add (int index, Figure f) {
        _children.add(index, f);
        ((AbstractFigure) f).setParent(this);
        _cachedBounds = null;
        f.repaint();
    }

    /** Test if the given figure is a child of this composite.
     * Note that this method, although provided, should not actually
     * be used for performance reasons -- instead, test if the parent
     * of the child is the same as this composite.
     */
    public boolean contains (Figure f) {
        return _children.contains(f);
    }

    /** Return an iteration of the children, in an undefined order.
     * This does not include the background figure, even if there is
     * one.
     */
    public Iterator figures () {
        return _children.figures();
    }

    /** Return an iteration of the children, from back to front. This
     * is the order in which the children are painted.  This does not
     * include the background figure, even if there is one.
     */
    public Iterator figuresFromBack () {
        return _children.figuresFromBack();
    }

    /** Return an iteration of the children, from front to back. This
     * is the order in which events are intercepted.  This does not
     * include the background figure, even if there is one.
     */
    public Iterator figuresFromFront () {
        return _children.figuresFromFront();
    }

    /** Return the figure at the given index.
     *
     * @exception IndexOutOfBoundsException The index is out of range.
     */
    public Figure get (int index) {
        return _children.get(index);
    }

    /** Get the background figure. The background figure
     *  is treated specially; its shape is the one returned by
     *  getShape(), and most of the methods that return iterators
     *  over figures (such as figures()) do not include the background
     *  figure.
     *  @see #figures()
     *  @see #setBackgroundFigure(Figure)
     */
    public Figure getBackgroundFigure () {
        return _background;
    }

    /** Get the bounding box of this figure. If the figure hasn't
     * changed since last time, as indicated by repaint() not having
     * been called here or by any descendents, a cached copy of the
     * bounding box will be returned, otherwise a new one will be generated.
     */
    public Rectangle2D getBounds () {
        if (_cachedBounds == null) {
            // This could be made faster by optimizing for orthogonal
            // transforms. Since it's cached though, don't worry about it.
            AffineTransform at = _transformContext.getTransform();
            Rectangle2D bounds = null;
            if (_children.getFigureCount() == 0) {
                if (_background != null) {
                    bounds = _background.getBounds();
                } else {
                    bounds = new Rectangle2D.Double(); // zero
                }
            } else {
                bounds = _children.getBounds();
                if (_background != null) {
                    Rectangle2D.union(bounds, _background.getBounds(), bounds);
                }
            }
            _cachedBounds = ShapeUtilities.transformBounds(bounds, at);
        }
        return _cachedBounds;
    }

    /** Get the internal z-list. Clients must <i>not</i> modify
     * the z-list, but can use it for making queries on its contents.
     */
    public ZList getChildren () {
        return _children;
    }

    /** Return the number of elements in this container.
     */
    public int getFigureCount () {
        return _children.getFigureCount();
    }

    /** Return the origin of the background figure in the enclosing
     *  transform context.
     *  @return The origin of the background figure.
     */
    public Point2D getOrigin () {
        if ( _background != null ) {
            AffineTransform at = _transformContext.getTransform();
            Point2D point = _background.getOrigin();
            return at.transform(point, point);
        } else {
            return super.getOrigin();
        }
    }

    /** Get the shape of this figure. This will be the shape of
     * the background if there is one, otherwise the bounding box.
     */
    public Shape getShape () {
        if ( _background != null ) {
            AffineTransform at = _transformContext.getTransform();
            return at.createTransformedShape(_background.getShape());
        } else {
            return getBounds();
        }
    }

    /** Return the transform context of this figure.
     */
    public TransformContext getTransformContext () {
        return _transformContext;
    }

    /** Return the index of the given figure in the Z-list, or -1
     * if the figure is not in this list.
     */
    public int indexOf (Figure f) {
        return _children.indexOf(f);
    }

    /** Test if this figure intersects the given rectangle.
     */
    public boolean intersects (Rectangle2D region) {
        // Transform the region to test
        AffineTransform t = _transformContext.getInverseTransform();
        Rectangle2D r = ShapeUtilities.transformBounds(region, t);

        // Check against all children
        boolean result;
        if (_background!= null) {
            result = getBackgroundFigure().intersects(r);
        } else {
            result = false;
        }
        Iterator i = _children.figures();
        while (!result && i.hasNext()) {
            result = result || ((Figure) i.next()).intersects(r);
        }
        return result;
    }

    protected void invalidateCachedBounds() {
        _cachedBounds = null;
    }

    /** Paint this composite figure onto a 2D graphics object.
     * This implementation pushes the transform context onto the
     * transform stack, and then paints all children.
     */
    public void paint (Graphics2D g) {
        if (!isVisible()) {
            return;
        }
        _transformContext.push(g);

        // Paint the background first, if there is one
        if (_background != null) {
            _background.paint(g);
        }
        // Now the rest of the children
        Figure f;
        Iterator i = _children.figuresFromBack();
        while (i.hasNext()) {
            f = (Figure) i.next();
            f.paint(g);
        }
        _transformContext.pop(g);
    }

    /** Paint this composite figure onto a 2D graphics object, within
     * the given region.  If the figure is not visible, return
     * immediately. Otherwise paint all figures that overlap the given
     * region, from back to front.
     */
    public void paint (Graphics2D g, Rectangle2D region) {
        if (!isVisible()) {
            return;
        }
        _transformContext.push(g);

        // Transform the region to paint
        AffineTransform t = _transformContext.getInverseTransform();
        Rectangle2D r = ShapeUtilities.transformBounds(region, t);

        // Paint the background first, if there is one
        if (_background != null) {
            _background.paint(g, r);
        }
        // Paint the children
        Iterator i = _children.getIntersectedFigures(r).figuresFromBack();
        while (i.hasNext()) {
            Figure f = (Figure) i.next();
            f.paint(g, r);
        }
        _transformContext.pop(g);
    }

    /** Get the picked figure. This method recursively traverses the
     * tree until it finds a figure that is "hit" by the region. Note
     * that a region is given instead of a point so that a "pick halo"
     * can be implemented. If no child is hit, testif the background
     * figure is hit. If still no figure is hit, return null. Note that
     * the region should not have zero size, or no figure will be hit.
     */
    public Figure pick (Rectangle2D region) {
        // Transform the region and then do the pick
        AffineTransform at = _transformContext.getInverseTransform();
        region = ShapeUtilities.transformBounds(region, at);
        Figure hit = CanvasUtilities.pick(
                _children.getIntersectedFigures(region).figuresFromFront(),
                region);
        if (hit == null && _background != null && _background.hit(region)) {
            return CanvasUtilities.pick(new UnitIterator(_background), region);
        }
        return hit;
    }

    /** Given a rectangle, return the top-most descendent figure
     * that it hits that is accepted by the given filter.
     * If none does, return null.
     */
    public Figure pick (Rectangle2D region, Filter filter) {
        // Transform the region and then do the pick
        AffineTransform at = _transformContext.getInverseTransform();
        region = ShapeUtilities.transformBounds(region, at);
        Figure hit = CanvasUtilities.pick(figuresFromFront(),
                region, filter);
        if (hit == null && _background != null && _background.hit(region)) {
            return CanvasUtilities.pick(new UnitIterator(_background), region,
                    filter);
        }
        return hit;
    }

    /** Remove the given child from this composite.
     */
    public void remove (Figure f) {
        f.repaint();
        f.setParent(null);
        _children.remove(f);
        _cachedBounds = null;
        f.repaint();
    }

    /** Remove the figure at the given position in the list.
     *
     * @exception IndexOutOfBoundsException The index is out of range.
     */
    public void remove (int index) {
        remove(_children.get(index));
    }

    /** Accept notification that a repaint has occurred somewhere
     * in the hierarchy below this container. This method overrides
     * the inherited method to clear the cached bounding box, and
     * then forwards the notification to the parent.
     */
    public void repaint (DamageRegion d) {
        d.checkCacheValid(_transformContext);
        // Check to make sure that this repaint isn't being triggered
        // by something below in the hierarchy that has moved in such
        // a way as to modify the bounding box.
        if (_cachedBounds != null && !_cachedBounds.contains(d.getBounds())) {
            _cachedBounds = null;
        }
        super.repaint(d);
    }

    /** Set the background figure.  The background figure
     *  is treated specially; its shape is the one returned by
     *  getShape(), and most of the methods that return iterators
     *  over figures (such as figures()) do not include the background
     *  figure.
     *  @see #figures()
     *  @see #getBackgroundFigure()
     */
    public void setBackgroundFigure (Figure background) {
        if (background != null) {
            background.setParent(null);
        }
        this._background = background;
        background.setParent(this);
        _cachedBounds = null;
        repaint();
    }

    /** Set the index of the given figure.
     *
     * @exception IndexOutOfBoundsException The new index is out of range.
     */
    public void setIndex (int index, Figure f) {
        _children.setIndex(index, f);
        _cachedBounds = null;
        repaint();
    }

    /** Replace the first figure, which must be a child, with the
     * second, which must not be a child.
     */
    protected void replaceChild (Figure child, Figure replacement) {
        repaint();
        _children.set(_children.indexOf(child), replacement);
        _cachedBounds = null;
        repaint();
    }

    /** Return a string description of this figure
     */
    public String toString() {
        String s = getClass().getName();
        s += ":Background=" + getBackgroundFigure();
        s += ":others={";
        Iterator i = figuresFromFront();
        while (i.hasNext()) {
            Figure f = (Figure)i.next();
            s += "," + f;
        }
        s += "}";
        return s;
    }

    /** Transform this figure with the supplied transform.
     * This method modifies the transform context with the transform.
     */
    public void transform (AffineTransform at) {
        repaint();
        ShapeUtilities.transformModify(_cachedBounds, at);
        _transformContext.preConcatenate(at);
        repaint();
    }

    /** Translate this figure the given distance.
     * This method modifies the transform context with the transform.
     */
    public void translate (double x, double y) {
        repaint();
        ShapeUtilities.translateModify(_cachedBounds, x, y);
        _transformContext.translate(x,y);
        repaint();
    }
}


