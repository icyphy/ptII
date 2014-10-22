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
 */
package diva.canvas.interactor;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import diva.canvas.Site;
import diva.canvas.toolbox.BasicRectangle;

/**
 * A grab-handle that is intended for acting as a "move me"
 * handle.
 *
 * FIXME: This is a hacked-up copy of BasicGrabHandle, I have no
 * idea whether the thing works for anything other than the demo
 * I constructed. -- johnr
 *
 * @author John Reekie
 * @author Michael Shilman
 * @version        $Id$
 */
public class MoveHandle extends BasicRectangle implements GrabHandle {
    /** The site
     */
    private Site _site;

    /* The most recent x and y coordinates
     */
    private double _x = 0.0;

    private double _y = 0.0;

    /* The half-length of the sides
     */
    private float _size = 8.0f;

    /**
     * Construct a new grab handle attached to the given
     * site.
     */
    public MoveHandle(Site s) {
        // Can't reference variable until superclass is called...
        super(0, 0, 0, 0, java.awt.Color.red);
        setSize(_size);
        _site = s;
    }

    /**
     * Get the site that this handle is attached to
     */
    @Override
    public Site getSite() {
        return _site;
    }

    /**
     * Get the "size" of the grab-handle. The size is half the
     * length of each side. The default is 4.0.
     */
    @Override
    public float getSize() {
        return _size;
    }

    /**
     * Reposition the grab-handle if necessary
     */
    @Override
    public void relocate() {
        // BIG FIXME This doesn't work for hierarchical figures because we
        // need to take the transformcontext into account.
        // FIXME.  OK, we hacked it, but what about nested panes?

        /*
         TransformContext tc = getTransformContext();
         while (tc.getParent() != null) {
         tc = tc.getParent(); //hack hack hack
         }
         Point2D p = _site.getPoint(tc);
         */
        Point2D p = _site.getPoint();
        double x = p.getX();
        double y = p.getY();

        if (x != _x || y != _y) {
            _x = x;
            _y = y;
            ((Rectangle2D) getShape()).setFrame(x - _size, y - _size,
                    _size * 2, _size * 2);
        }
    }

    /**
     * Set the set to which this grab-handle is attached.
     */
    @Override
    public void setSite(Site s) {
        _site = s;
        relocate();
    }

    /**
     * Set the "size" of the grab-handle.  The size is half the
     * length of each side.
     */
    @Override
    public void setSize(float size) {
        this._size = size;
        ((Rectangle2D) getShape()).setFrame(_x - _size, _y - _size, _size * 2,
                _size * 2);
    }

    /**
     * Translating a grab-handle moves its parent object, but _doesn't_
     * move the grab-handle itself.
     */
    @Override
    public void translate(double x, double y) {
        _site.getFigure().translate(x, y);
    }
}
