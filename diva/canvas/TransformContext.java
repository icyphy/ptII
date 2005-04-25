/*
  Copyright (c) 1998-2005 The Regents of the University of California
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
  * */
package diva.canvas;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;


/** A transform context defines a transformed coordinate system.
 * Each transform context is associated with a component in the
 * display tree.  This class provides the support for most of the
 * transform-related operations on the display tree.
 *
 * @version $Id$
 * @author John Reekie
 * @Pt.AcceptedRating Yellow
 */
public class TransformContext {
    /** A flag saying whether the cached data is valid.
     */
    private boolean _cacheValid = false;

    /** The version number
     */
    private int _version = 0;

    /** The component associated with this context
     */
    private CanvasComponent _component;

    /** The temporary graphics context transform
     */
    private AffineTransform _graphicsTransform;

    /** The main transform, which is from this context to the parent
     * context
     */
    private AffineTransform _transform = new AffineTransform();

    /** The transform from the parent context to this context
     */
    private AffineTransform _inverseTransform;

    /** The cached local-to-screen coordinate transform
     */
    private AffineTransform _screenTransform;

    /** Create a transform context associated with the
     * given display component.
     */
    public TransformContext(CanvasComponent component) {
        this._component = component;
    }

    ///////////////////////////////////////////////////////////////////
    //// public methods

    /** Check transform cache validity. The argument is an ancestor
     * context of this context. If the ancestor is not valid, then
     * all of the contexts from this one up to the ancestor
     * are marked invalid.
     */
    public void checkCacheValid(TransformContext ancestor) {
        TransformContext c = this;

        while ((c != ancestor) && (c != null)) {
            c.invalidateCache();
            c = c.getParent();
        }
    }

    /** Concatenate this transform context with the given transform.
     */
    public void concatenate(AffineTransform at) {
        _transform.concatenate(at);
        invalidateCache();
    }

    /** Get the component that this context is associated with. The
     * component is set in the constructor and cannot be changed.
     */
    public CanvasComponent getComponent() {
        return _component;
    }

    /** Get the transform from parent coordinates into
     * local coordinates.
     */
    public AffineTransform getInverseTransform() {
        if (_inverseTransform == null) {
            try {
                _inverseTransform = _transform.createInverse();
            } catch (NoninvertibleTransformException e) {
                // Now what??? FIXME
                throw new UnsupportedOperationException(e.getMessage());
            }
        }

        return _inverseTransform;
    }

    /** Get the parent transform context of this one, or null
     * if this context is at the root of the transform tree.
     */
    public TransformContext getParent() {
        if (_component.getParent() == null) {
            return null;
        } else {
            return _component.getParent().getTransformContext();
        }
    }

    /** Get the transform from local coordinates into screen coordinates.
     */
    public AffineTransform getScreenTransform() {
        if (!_cacheValid) {
            validateCache();
        }

        return _screenTransform;
    }

    /** Get the transform of this context. This is the transform from
     * this context into the parent's context.
     */
    public AffineTransform getTransform() {
        return _transform;
    }

    /** Get the transform of this context, relative to the given context.
     *  This is the transform from this context into the given context, which
     *  must enclose this one.
     */
    public AffineTransform getTransform(TransformContext context) {
        if (context == this) {
            return new AffineTransform();
        } else {
            TransformContext parentContext = _component.getParent()
                .getTransformContext();
            AffineTransform transform = _transform;

            if (parentContext == context) {
                transform = new AffineTransform(transform);
                transform.preConcatenate(parentContext.getTransform(context));
            }

            return transform;
        }
    }

    /** Get the version number of the transform context. The version number
     * is incremented whenever the transform changes, so can be
     * used by client components to figure out when to update cached
     * data based upon the transform.
     */
    public int getVersion() {
        return _version;
    }

    /** Notify that cached data based on the transform is now invalid.
     * Increment the version number so that clients can use it to
     * tell if they need to update data based on the transform.
     */
    public void invalidateCache() {
        _version++;
        _cacheValid = false;
        _inverseTransform = null;
    }

    /** Test if the cache is valid.
     */
    public boolean isCacheValid() {
        return _cacheValid;
    }

    /** Push this transform onto the graphics stack.
     */
    public void push(Graphics2D g) {
        _graphicsTransform = g.getTransform();
        g.transform(_transform);
    }

    /** Pop this transform off the graphics stack.
     */
    public void pop(Graphics2D g) {
        g.setTransform(_graphicsTransform);
    }

    /** Pre-concatenate this transform context with the given transform.
     */
    public void preConcatenate(AffineTransform at) {
        _transform.preConcatenate(at);
        invalidateCache();
    }

    /** Set the transform that maps local coordinates into the
     * parent's coordinates. If there is no parent, the "parent" is
     * taken to be the screen. An exception will be thrown if the
     * transform is null. This method invalidates any cached
     * transforms.  Note that the transform may be remembered by this
     * context, so the caller must make sure that it will not be
     * subsequently modified.
     */
    public void setTransform(AffineTransform at) {
        _transform = at;
        invalidateCache();
    }

    /** Translate this context the given distance.
     */
    public void translate(double x, double y) {
        // It might be worth optimizing this by keep this transform around
        AffineTransform at = new AffineTransform();
        at.translate(x, y);
        _transform.preConcatenate(at);

        invalidateCache();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Recompute the cached transforms.
     */
    private void validateCache() {
        // Update screen transform. Is this the top of the tree?
        if (_component.getParent() == null) {
            _screenTransform = _transform;
        } else {
            CanvasComponent p = _component.getParent();
            _screenTransform = new AffineTransform(p.getTransformContext()
                    .getScreenTransform());
            _screenTransform.concatenate(_transform);
        }

        _cacheValid = true;
    }
}
