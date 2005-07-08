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
 */
package diva.gui.toolbox;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

import diva.canvas.CanvasUtilities;
import diva.canvas.Figure;
import diva.canvas.toolbox.ImageFigure;

/**
 * An icon that looks like a diva figure.  This class renders the figure into
 * a buffered image and then points the icon at the buffer.  This process is
 * rather slow, so you might want to cache the returned icon somehow to
 * avoid repeating it, especially if you have a large number of icons to
 * render such as in a TreeCellRenderer.
 *
 * This class handles ImageFigure specially, to deal with image
 * loading.  Where possible, instead of painting the figure into a
 * buffered image, this class simply uses the Image of an ImageFigure.
 *
 * @author Steve Neuendorffer
 * @version $Id$
 */
public class FigureIcon extends ImageIcon {
    /**
     * Create a new icon that looks like the given figure.  The icon will
     * have the size of the bounds of the figure.
     * The figure will be rendered into the icon with antialiasing turned off.
     */
    public FigureIcon(Figure figure) {
        this(figure, false);
    }

    /**
     * Create a new icon that looks like the given figure.  The icon will
     * have the size of the bounds of the figure.
     * The figure will be rendered into the icon with antialiasing according
     * to the given flag.
     * @param antialias True if antialiasing should be used.
     */
    public FigureIcon(Figure figure, boolean antialias) {
        super();

        if (figure instanceof ImageFigure) {
            ImageFigure imageFigure = (ImageFigure) figure;
            setImage(imageFigure.getImage());
        } else {
            Rectangle2D bounds = figure.getBounds();
            BufferedImage image = new BufferedImage((int) bounds.getWidth(),
                    (int) bounds.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();

            if (antialias) {
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
            } else {
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_OFF);
            }

            figure.paint(graphics);
            setImage(image);
        }
    }

    /**
     * Create a new icon that looks like the given figure.  The figure will be
     * scaled to fit inside the given size, with any excess size filled
     * with a transparent background color.
     * The figure will be rendered into the icon with antialiasing turned off.
     */
    public FigureIcon(Figure figure, int x, int y) {
        this(figure, x, y, 0, false);
    }

    /**
     * Create a new icon that looks like the given figure.  The icon will be
     * made the given size, and
     * given a border of the given number of pixels.  The rendition of the
     * figure will be scaled to fit inside the border,
     * with any excess size filled with a transparent background color.
     * The figure will be rendered into the icon with antialiasing  according
     * to the given flag.
     * @param antialias True if antialiasing should be used.
     */
    public FigureIcon(Figure figure, int x, int y, int border, boolean antialias) {
        super();

        if (figure instanceof ImageFigure && (border == 0)) {
            ImageFigure imageFigure = (ImageFigure) figure;
            Image image = imageFigure.getImage();
            image = image.getScaledInstance(x, y, Image.SCALE_DEFAULT);
            setImage(image);
        } else {
            Rectangle2D bounds = figure.getBounds();
            Rectangle2D size = new Rectangle2D.Double(border, border, x
                    - (2 * border), y - (2 * border));
            AffineTransform transform = CanvasUtilities.computeFitTransform(
                    bounds, size);
            figure.transform(transform);

            BufferedImage image = new BufferedImage(x, y,
                    BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D graphics = image.createGraphics();

            if (antialias) {
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
            } else {
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_OFF);
            }

            graphics.setBackground(new Color(0, 0, 0, 0));
            graphics.clearRect(0, 0, x, y);
            figure.paint(graphics);
            setImage(image);
        }
    }
}
