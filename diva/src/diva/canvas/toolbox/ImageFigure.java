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
package diva.canvas.toolbox;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.net.URL;

import diva.canvas.AbstractFigure;
import diva.canvas.CanvasUtilities;

/**
 * A figure which draws a user-specified image.
 *
 * @author Michael Shilman
 * @version $Id$
 */
public class ImageFigure extends AbstractFigure implements ImageObserver {
    /** Indicator of whether this figure should be centered on its origin.
     *  By default, this class is not centered.
     */
    private boolean _centered = false;

    /**
     * The height of the figure.
     */
    private int _height = 0;

    /**
     * The image of this figure.
     */
    private Image _image;

    /**
     * The width of the figure.
     */
    private int _width = 0;

    /**
     * The local transform
     */
    private AffineTransform _xf = new AffineTransform();

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
     *  context.  This overrides the base class to return the center
     *  of the shape, if the figure is centered, or the origin of the
     *  shape if the figure is not centered.
     *  @return The origin of the figure.
     */
    @Override
    public Point2D getOrigin() {
        if (_centered) {
            return super.getOrigin();

            // Used to do:
            // return new Point2D.Double(bounds.getX(), bounds.getY());
        } else {
            Point2D point = new Point2D.Double(0, 0);
            _xf.transform(point, point);
            return point;
        }
    }

    /**
     * Return the rectangular shape of the
     * image, or a small rectangle if the
     * image is null. NOTE: You cannot rely on the
     * return value unless the image has been fully
     * rendered.
     */
    @Override
    public Shape getShape() {
        if (_image != null) {
            int w = _image.getWidth(this);
            int h = _image.getHeight(this);

            if (w < 0 || h < 0) {
                // Width and height are not ready
                // (Image is not fully loaded or it
                // is fully loaded, but the Java
                // implementation stupidly hasn't gotten
                // around to updating the fields in the
                // image object.
                w = _width;
                h = _height;
            }

            Rectangle2D r = new Rectangle2D.Double(0, 0, w, h);
            return _xf.createTransformedShape(r);
        } else {
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
     *   completely loaded; true otherwise.
     */
    @Override
    public boolean imageUpdate(Image image, int infoflags, int x, int y,
            int width, int height) {

        // Sven Koeler writes:
        // "we recently needed support for animated GIFs on the canvas in Kepler.
        // I made some changes in Ptolemy to repaint the Image on frame change
        // events and haven't seen any performance issues."

        // start
        if ((infoflags & ImageObserver.FRAMEBITS) != 0) {
            repaint();
        }
        // end

        if ((infoflags & (ImageObserver.ERROR | ImageObserver.ABORT)) != 0) {
            // Create a default error image.
            URL url = getClass().getClassLoader().getResource(
                    "/diva/canvas/toolbox/errorImage.gif");
            Toolkit tk = Toolkit.getDefaultToolkit();
            _image = tk.getImage(url);
            return true;
        }

        if ((infoflags & (ImageObserver.HEIGHT | ImageObserver.WIDTH)) != 0) {
            // NOTE: Incredibly stupidly, when Java calls this method
            // with a new width and height, it hasn't set those fields
            // in the image yet.  Thus, even though width and height
            // have been updated, they are not accessible in the image,
            // which will still return -1 to getWidth() and getHeight().
            // Go figure...  I guess the idea is that we have to
            // duplicate the image information locally. Dumb.
            _height = height;
            _width = width;

            // In case the width or height is later updated.
            _image.getWidth(this);
            _image.getHeight(this);

            // repaint();
            return true;
        }

        // A zillion calls are made with PROPERTIES or SOMEBITS.
        // If these occur, do not call repaint().
        if ((infoflags & (ImageObserver.PROPERTIES | ImageObserver.SOMEBITS)) != 0) {
            // In case the width or height is later updated.
            _image.getWidth(this);
            _image.getHeight(this);
            return true;
        }

        if ((infoflags & ImageObserver.ALLBITS) != 0) {
            repaint();

            // Return false, indicating that the image is completely loaded.
            return false;
        }

        // In case the width or height is later updated.
        _image.getWidth(this);
        _image.getHeight(this);

        return true;
    }

    /** Return whether the figure should be centered on its origin.
     *  @return False If the origin of the figure, as
     *   returned by getOrigin(), is the upper left corner.
     *  @see #getOrigin()
     *  @see #setCentered(boolean)
     */
    public boolean isCentered() {
        return _centered;
    }

    /**
     * Paint the figure's image.
     */
    @Override
    public void paint(Graphics2D g) {
        if (_image != null) {
            // The image may not be ready to be painted, so we pass this
            // as an argument to ensure that imageUpdate() is called
            // when the image is ready.
            g.drawImage(_image, _xf, this);
        }
    }

    /** Specify whether the figure should be centered on its origin.
     *  By default, it is.
     *  @param centered False to make the origin of the figure, as
     *   returned by getOrigin(), be the upper left corner.
     *  @see #getOrigin()
     */
    public void setCentered(boolean centered) {
        repaint();

        Point2D point = getOrigin();
        _centered = centered;
        CanvasUtilities.translateTo(this, point.getX(), point.getY());
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
    @Override
    public void transform(AffineTransform t) {
        repaint();
        _xf.preConcatenate(t);
        repaint();
    }
}
