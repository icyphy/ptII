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
import diva.canvas.TransformContext;
import diva.canvas.toolbox.BasicRectangle;

/**
 * A basic rectangle grab-handle implementation.
 *
 * @author John Reekie      (johnr@eecs.berkeley.edu)
 * @author Michael Shilman  (michaels@eecs.berkeley.edu)
 * @version        $Revision$
 */
public class BasicGrabHandle extends BasicRectangle implements GrabHandle {
    /** The site
     */
    private Site _site;

    /* The most recent x and y coordinates
     */
    private double _x = 0.0;
    private double _y = 0.0;

    /* The half-length of the sides
     */
    private float _size = 4.0f;

    /**
     * Construct a new grab handle attached to the given
     * site.
     */
    public BasicGrabHandle(Site s) {
        // Can't reference variable until superclass is called...
        super(0,0,0,0, java.awt.Color.blue);
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
        // Be sure to take into account that the transformContext of the
        // site and the context of the grab handle may be different.
        TransformContext tc = getTransformContext();
        Point2D p = _site.getPoint(tc);
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
     * Translating a grab-handle moves its site, but _doesn't_
     * move the grab-handle itself (that will be handled by the
     * reshape manipulator).
     */
    public void translate (double x, double y) {
        _site.translate(x,y);
    }
}


