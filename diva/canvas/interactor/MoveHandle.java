/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
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
 * @author John Reekie      (johnr@eecs.berkeley.edu)
 * @author Michael Shilman  (michaels@eecs.berkeley.edu)
 * @version        $Revision$
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
        super(0,0,0,0, java.awt.Color.red);
        setSize(_size);
        _site = s;
    }

    /**
     * Get the site that this handle is attached to
     */
    public Site getSite() {
        return _site;
    }

    /**
     * Get the "size" of the grab-handle. The size is half the
     * length of each side. The default is 4.0.
     */
    public float getSize () {
      return _size;
    }

    /**
     * Reposition the grab-handle if necessary
     */
    public void relocate () {
        // BIG FIXME This doesn't work for hierarchical figures because we
        // need to take the transformcontext into account.
        // FIXME.  OK, we hacked it, but what about nested panes?
        /*
        TransformContext tc = getTransformContext();
        while(tc.getParent() != null) {
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
            ((Rectangle2D)getShape()).setFrame(
                    x - _size, y - _size, _size*2, _size*2);
        }
    }

    /**
     * Set the set to which this grab-handle is attached.
     */
    public void setSite (Site s) {
       _site = s;
       relocate();
    }

   /**
     * Set the "size" of the grab-handle.  The size is half the
     * length of each side.
     */
    public void setSize (float size) {
      this._size = size;
      ((Rectangle2D)getShape()).setFrame(
                          _x - _size, _y - _size, _size*2, _size*2);
    }

    /**
     * Translating a grab-handle moves its parent object, but _doesn't_
     * move the grab-handle itself.
     */
    public void translate (double x, double y) {
        _site.getFigure().translate(x,y);
    }
}


