/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 *
 */

package diva.canvas;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

import javax.swing.JComponent;

import diva.canvas.event.LayerEvent;


/** The JCanvas class is the center-piece of this package.  The canvas
 * is composed of a canvas pane, which in turn is composed of one or
 * more layers. Each layer may contain arbitrary graphics, although
 * commonly at least one layer is an instance of a "figure layer"
 * class that contains figure objects. The main role of the JCanvas
 * class is to provide the physical screen space on which layers draw
 * themselves, and to interface to the Swing component hierarchy.
 *
 * <p>
 * This architecture allows a graphics developer to write code for a
 * pane and a set of layers, without being concerned about whether the
 * pane and its layers will be directly contained by a JCanvas or
 * within some other layer. For example, it will be possible for a
 * visualization component to be "embedded" in a larger component.
 *
 * @version        $Revision$
 * @author John Reekie
 * @rating Yellow
 */
public class JCanvas extends JComponent implements Printable {

    /** The off-screen image.
     * @serial
     */
    private BufferedImage _offscreen = null;

    /** The contained canvas pane.
     * @serial
     */
    private CanvasPane _canvasPane = null;

    /** A flag to tell us whether to work around the
     * clearRect bug in beta4
     */
    private transient boolean _workaroundClearRectBug = _checkForClearRectBug();

    /** Create a new canvas that contains a single GraphicsPane. This
     * is the simplest way of using the JCanvas. Mouse events on the
     * canvas are enabled by default.
     */
    public JCanvas ( ) {
        this(new GraphicsPane());
    }

    /** Create a new canvas that contains the given CanvasPane. Mouse
     * events on the canvas are enabled by default.
     */
    public JCanvas (CanvasPane pane) {
        super();
        setBackground(Color.white);
        setCanvasPane(pane);
        enableEvents(AWTEvent.MOUSE_EVENT_MASK
                | AWTEvent.MOUSE_MOTION_EVENT_MASK );
        // We have to set this to something other than null, or else no
        // tool tips will appear!
        super.setToolTipText("");
    }

    /** Get the canvas pane contained by this component.
     */
    public final CanvasPane getCanvasPane () {
        return _canvasPane;
    }

    /** Get the toolTipText for the point in the given MouseEvent.
     *  Ask the canvasPane for a toolTip for the location of the mouse
     *  event.  Note that you should not in general call
     *  setToolTipText on this canvas, since the value is ignored and the
     *  value returned by this method used instead.  Even worse,
     *  if you call setToolTipText(null), then tooltips will probably get
     *  disabled entirely!
     */
    public String getToolTipText(MouseEvent e) {
        LayerEvent layerevent = null;

        if (_canvasPane == null) {
            return null;
        }

        // Create a new event and transform layer coordinates if necessary
        layerevent = new LayerEvent(e);
        AffineTransform at =
            _canvasPane.getTransformContext().getInverseTransform();
        layerevent.transform(at);

        // Process it on the pane
        String tip = _canvasPane.getToolTipText(layerevent);
        return tip;
    }


    /** Return whether or not focus should be traversable across this object.
     *  This must return true to allow keyboard events to be grabbed.  Return
     *  true in this class.
     */
    public boolean isFocusTraversable() {
        return true;
    }

    /** Paint the canvas. Every layer in this canvas will be
     * requested to paint itself.
     * <p>
     * This method allocates an offscreen buffer if necessary, and then
     * paints the canvas into the right buffer and blits it to the
     * on-screen buffer.
     * <p>
     * Acknowledgement: some of this code was adapted from code
     * posted by Jonathon Knudsen to the Java2D mailing list, May
     * 1998.
     */
    public void paint (Graphics g) {
        // It appears that Swing already sets the clip region when
        // we are ready to draw. So let's see if we are drawing the
        // whole canvas or not...
        Rectangle clip = g.getClipBounds();
        Dimension d = getSize();
        if (clip == null) {
            // This may happen if the component is not visible, for
            // instance if we are manually calling the paint method
            // to print.
            clip = new Rectangle(0, 0, d.width, d.height);
        }
        boolean paintAll = (clip.x == 0 && clip.y == 0
                && clip.width == d.width
                && clip.height == d.height);

        if (!isDoubleBuffered()) {
            Graphics2D g2d = (Graphics2D) g;

            // Clear the clip region to the background color
            g2d.setBackground(getBackground());
            if (_workaroundClearRectBug) {
                g2d.clearRect(0, 0, clip.width, clip.height);
            } else {
                g2d.clearRect(clip.x, clip.y, clip.width, clip.height);
            }

            // Draw directly onto the graphics pane
            if (paintAll) {
                _canvasPane.paint(g2d);
            } else {
                _canvasPane.paint(g2d, clip);
            }
        } else {
            // Get a new offscreen buffer if necessary. Clear the reference
            // to the off-screen buffer, so that the memory can be freed
            // if necessary by the GC and reallocated for the new buffer.
            if (_offscreen == null ||
                    _offscreen.getWidth() != clip.width ||
                    _offscreen.getHeight() != clip.height) {
                _offscreen = null;    // in case GC needs it
                _offscreen = new BufferedImage(
                        clip.width,
                        clip.height,
                        BufferedImage.TYPE_INT_RGB);
            }
            Graphics2D g2d = _offscreen.createGraphics();

            // Clear the clip region to the background color
            g2d.setBackground(getBackground());
            if (_workaroundClearRectBug) {
                g2d.clearRect(0, 0, clip.width, clip.height);
            } else {
                g2d.clearRect(clip.x, clip.y, clip.width, clip.height);
            }

            // Paint on it
            if (paintAll) {
                _canvasPane.paint(g2d);
            } else {
                // Translate drawing into the offscreen buffer
                g2d.translate(-clip.x,-clip.y);

                // Paint the root canvas pane in the clip region
                _canvasPane.paint(g2d, clip);
            }

            // Blit it to the onscreen buffer
            g.drawImage(_offscreen, clip.x, clip.y, null);
        }

        super.paint(g);
    }

    /** Print the canvas to a printer, represented by the specified graphics
     *  object.  Scale the size of the canvas to fit onto the printed page,
     *  while preserving the shape of the objects on the page.
     *  @param graphics The context into which the page is drawn.
     *  @param format The size and orientation of the page being drawn.
     *  @param index The zero based index of the page to be drawn.
     *  @return PAGE_EXISTS if the page is rendered successfully, or
     *   NO_SUCH_PAGE if pageIndex specifies a non-existent page.
     *  @exception PrinterException If the print job is terminated.
     */
    public int print(Graphics graphics, PageFormat format,
            int index) throws PrinterException {

        Dimension dimension = getSize();
        Rectangle2D bounds = new Rectangle2D.Double (
                0, 0,
                dimension.width, dimension.height);
        return print(graphics, format, index, bounds);
    }

    /** Print the canvas to a printer, represented by the specified graphics
     *  object.  Scale the given printRegion to fit onto the printed page,
     *  while preserving the shape of the objects on the page.
     *  @param graphics The context into which the page is drawn.
     *  @param format The size and orientation of the page being drawn.
     *  @param index The zero based index of the page to be drawn.
     *  @param printRegion The rectangular region of the canvaws, in screen
     *  coordinates, that will be printed to the screen.
     *  @return PAGE_EXISTS if the page is rendered successfully, or
     *   NO_SUCH_PAGE if pageIndex specifies a non-existent page.
     *  @exception PrinterException If the print job is terminated.
     */
    public int print(Graphics graphics, PageFormat format,
            int index, Rectangle2D printRegion) throws PrinterException {
        // We only print on one page.
        if (index >= 1) {
            return Printable.NO_SUCH_PAGE;
        }
        Rectangle2D pageBounds = new Rectangle2D.Double (
                format.getImageableX(),
                format.getImageableY(),
                format.getImageableWidth(),
                format.getImageableHeight());
        ((Graphics2D) graphics).transform(CanvasUtilities.computeFitTransform(
                printRegion, pageBounds));
        graphics.setClip(printRegion);

        paint(graphics);
        return Printable.PAGE_EXISTS;
    }

    /** Accept notification that a repaint has occurred on
     * in this canvas. Call the given damage region to generate
     * the appopriate calls to the Swing repaint manager.
     */
    public void repaint (DamageRegion d) {
        d.apply(this);
    }

    /** Set the canvas pane contained by this JCanvas.
     * If there is already a pane in this JCanvas, replace it.
     * If the pane already is in a canvas, remove it from
     * that other canvas.
     */
    public final void setCanvasPane (CanvasPane pane) {
        if (_canvasPane != null) {
            _canvasPane.setCanvas(null);
        }
        _canvasPane = pane;
        if (pane != null) {
            pane.setCanvas(this);
        }
    }

    /** Turn double-buffering on this canvas on or off.
     * This method overrides the inherited method to
     * delete the off-screen buffer.
     */
    public void setDoubleBuffered (boolean flag) {
        super.setDoubleBuffered(flag);
        if (!isDoubleBuffered()) {
            _offscreen = null;
        }
    }

    /** Set the preferred size of this JCanvas. In addition to calling
     * the superclass method, this calls setSize() on the contained pane.
     */
    public void setPreferredSize (Dimension d) {
        super.setPreferredSize(d);

        if (_canvasPane != null) {
            // FIXME: Transform size!!!
            Dimension size = getSize();
            Point2D s = new Point2D.Double(size.width, size.height);
            _canvasPane.setSize(s);
        }
    }

    ///////////////////////////////////////////////////////////////////////
    //// protected methods

    /** Process a mouse event. This method overrides the inherited
     * method to create a LayerEvent or LayerMotionEvent and pass the
     * event on to its pane (if it is not null).
     * The mouse event is passed to the superclass' method for
     * handling.
     */
    protected void processMouseEvent(MouseEvent e) {
        internalProcessMouseEvent(e);
        super.processMouseEvent(e);
    }

    /** Process a mouse motion event. This method overrides the
     * inherited method to create a LayerEvent or LayerMotionEvent
     * and pass the event on to its pane (if it is not null).
     * The mouse event is passed to the superclass' method for
     * handling.
     */
    protected void processMouseMotionEvent(MouseEvent e) {
        internalProcessMouseEvent(e);
        super.processMouseMotionEvent(e);
    }

    ///////////////////////////////////////////////////////////////////////
    ////                      private methods                        ////

    /** Process a mouse event. This internal method is called
     * by both processMouseEvent() and processMouseMotionEvent().
     */
    private void internalProcessMouseEvent(MouseEvent e) {
        LayerEvent layerevent = null;

        if (_canvasPane == null) {
            return;
        }

        // Create a new event and transform layer coordinates if necessary
        layerevent = new LayerEvent(e);
        AffineTransform at =
            _canvasPane.getTransformContext().getInverseTransform();
        layerevent.transform(at);

        // Process it on the pane
        _canvasPane.dispatchEvent(layerevent);
    }

    /** Check for  the clearRect bug by looking at the JDK version
     */
    private boolean _checkForClearRectBug() {
        return System.getProperty("java.version").equals("1.2beta4");
    }
}


