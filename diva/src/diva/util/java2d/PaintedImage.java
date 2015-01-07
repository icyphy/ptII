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
package diva.util.java2d;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;

import diva.canvas.CanvasUtilities;

/**
 *
 * @version        $Id$
 * @author         Steve Neuendorffer
 */
public class PaintedImage implements PaintedObject, ImageObserver {
    // Note that this class was deprecated becase we were to use
    // diva.compat.canvas instead.  However, the Ptolemy sources
    // do not include diva.compat.canvas, so I'm making this class
    // undeprecated. -cxh 7/05
    // The image that we are drawing.
    private Image _image;

    // The location of the image.
    private AffineTransform _transform;

    // The bounds
    private Rectangle2D _bounds;

    public PaintedImage(Image image, Rectangle2D bounds) {
        _image = image;
        _bounds = bounds;
        _transform = CanvasUtilities.computeTransform(new Rectangle2D.Double(0,
                0, image.getWidth(this), image.getHeight(this)), bounds);
    }

    /** Get the bounding box of the object when painted.
     */
    @Override
    public Rectangle2D getBounds() {
        return _bounds;
    }

    /** Paint the shape. Implementations are expected to redraw
     * the entire object. Whether or not the paint overwrites
     * fields in the graphics context such as the current
     * paint, stroke, and composite, depends on the implementing class.
     */
    @Override
    public void paint(Graphics2D g) {
        g.drawImage(_image, _transform, this);
    }

    /**
     */
    @Override
    public boolean imageUpdate(Image image, int flags, int x, int y, int w,
            int h) {
        if ((flags & (ImageObserver.ABORT | ImageObserver.ERROR)) == 0) {
            _transform = CanvasUtilities.computeTransform(
                    new Rectangle2D.Double(0, 0, image.getWidth(this), image
                            .getHeight(this)), _bounds);
        }

        return true;
    }
}
