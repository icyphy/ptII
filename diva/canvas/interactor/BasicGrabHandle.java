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

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import diva.canvas.Figure;
import diva.canvas.Site;
import diva.canvas.TransformContext;
import diva.canvas.toolbox.BasicRectangle;

/**
 * A basic rectangle grab-handle implementation.
 *
 * @author John Reekie
 * @author Michael Shilman
 * @version        $Id$
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
        super(0, 0, 0, 0, java.awt.Color.blue);
        Figure figure = s.getFigure();
        // Set the size of the grab handles to be independent of the zoom factor.
        if (figure != null) {
            AffineTransform transform = figure.getTransformContext()
                    .getScreenTransform();
            // Assume the zoom factor in the x axis is the same as the y axis.
            double scaleX = transform.getScaleX();
            setSize(4.0f / (float) scaleX);
        } else {
            setSize(_size);
        }
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
        // Be sure to take into account that the transformContext of the
        // site and the context of the grab handle may be different.
        TransformContext tc = getTransformContext();

        Point2D p = _site.getPoint(tc);
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
     * Translating a grab-handle moves its site, but _doesn't_
     * move the grab-handle itself (that will be handled by the
     * reshape manipulator).
     */
    @Override
    public void translate(double x, double y) {
        _site.translate(x, y);
    }
}
