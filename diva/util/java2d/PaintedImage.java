/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 *
 */

package diva.util.java2d;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.ImageObserver;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import diva.canvas.CanvasUtilities;

/**
 *
 * @version        $Revision$
 * @author         Steve Neuendorffer
 * @deprecated Will be removed in Diva 0.4. Use diva.compat.canvas if needed.
 */
public class PaintedImage implements PaintedObject, ImageObserver {

    // The image that we are drawing.
    private Image _image;
    // The location of the image.
    private AffineTransform _transform;
    // The bounds
    private Rectangle2D _bounds;

    public PaintedImage(Image image, Rectangle2D bounds) {
        _image = image;
        _bounds = bounds;
        _transform = CanvasUtilities.computeTransform(
                new Rectangle2D.Double(0, 0,
                        image.getWidth(this), image.getHeight(this))
                    , bounds);
    }

    /** Get the bounding box of the object when painted.
     */
    public Rectangle2D getBounds() {
        return _bounds;
    }

    /** Paint the shape. Implementations are expected to redraw
     * the entire object. Whether or not the paint overwrites
     * fields in the graphics context such as the current
     * paint, stroke, and composite, depends on the implementing class.
     */
    public void paint (Graphics2D g) {
        g.drawImage(_image, _transform, this);
    }

    /**
     */
    public boolean imageUpdate(Image image, int flags,
            int x, int y, int w, int h) {
        if ((flags & (ImageObserver.ABORT | ImageObserver.ERROR)) == 0) {
            _transform = CanvasUtilities.computeTransform(
                    new Rectangle2D.Double(0, 0,
                            image.getWidth(this), image.getHeight(this))
                        , _bounds);
        }
        return true;
    }
}


