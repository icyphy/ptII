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
 */
package diva.canvas.toolbox;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import java.awt.image.ImageObserver;

import diva.canvas.AbstractFigure;

/**
 * A figure which draws a user-specified image.
 *
 * @author Michael Shilman  (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public class ImageFigure extends AbstractFigure
    implements ImageObserver {
    /**
     * The local transform
     */
    private AffineTransform _xf = new AffineTransform();

    /**
     * The image of this figure.
     */
    private Image _image;

    /**
     * Create an empty image figure.
     */
    public ImageFigure() {
        this(null);
    }

    /**
     * Create an image figure displaying
     * the given image. This needs to be called in the Swing thread.
     */
    public ImageFigure(Image i) {
        setImage(i);
    }

    /**
     * Return the figure's image.
     */
    public Image getImage() {
        return _image;
    }

    /** Return the origin of the figure in the enclosing transform
     *  context.  This class overrides the base class to return the
     *  upper left corner of the image.  This ensures that the origin
     *  does not move as the image loads.
     *  @return The origin of the figure.
     */
    public Point2D getOrigin () {
        Rectangle2D bounds = getBounds();
        return new Point2D.Double(bounds.getX(), bounds.getY());
    }

    /**
     * Return the rectangular shape of the
     * image, or a small rectangle if the
     * image is null. NOTE: You cannot rely on the
     * return value unless the image has been fully
     * rendered.
     */
    public Shape getShape() {
        if (_image != null) {
            int w = _image.getWidth(this);
            int h = _image.getHeight(this);
            Rectangle2D r = new Rectangle2D.Double(0, 0, w, h);
            return _xf.createTransformedShape(r);
        }
        else {
            return new Rectangle2D.Double();
        }
    }

    /** This method, which is required by the ImageObserver interface,
     *  is called if something has changed in a background loading of
     *  the image.  It simply calls repaint().
     *  @param image The image being observed.
     *  @param infoflags The bitwise inclusive OR of the following flags:
     *   WIDTH, HEIGHT, PROPERTIES, SOMEBITS, FRAMEBITS, ALLBITS, ERROR,
     *   ABORT.
     *  @param x The x coordinate of the image.
     *  @param y The y coordinate of the image.
     *  @param width The width of the image.
     *  @param height The height of the image.
     *  @return False if the infoflags indicate that the image is
     *   completely loaded; true otherwise.  This always returns false.
     */
    public boolean imageUpdate(Image image,
            int infoflags,
            int x,
            int y,
            int width,
            int height) {
        // FIXME: This should probably create some default error
        // image if the infoflags argument contains ERROR or ABORT.
        repaint();

        // If we get a further update, then make sure we call this
        // method again.
        _image.getWidth(this);
        return true;
    }

    /**
     * Paint the figure's image.
     */
    public void paint(Graphics2D g) {
        if (_image != null) {
            // The image may not be ready to be painted, so we pass this
            // as an argument to ensure that imageUpdate() is called
            // when the image is ready.
            g.drawImage(_image, _xf, this);
        }
    }

    /**
     * Set the figure's image. This should be called only
     * from the Swing thread.
     * @param i The image.
     */
    public void setImage(Image i) {
        // NOTE: Repainting needs to be done twice because
        // the size of the image might change and the bounding
        // box of both the image before and after need to be
        // repainted.
        repaint();
        _image = i;
        repaint();
    }

    /**
     * Perform an affine transform on this
     * image.
     */
    public void transform(AffineTransform t) {
        repaint();
        _xf.preConcatenate(t);
        repaint();
    }
}



