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
package diva.canvas;

import java.awt.AWTEvent;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import diva.canvas.event.EventAcceptor;
import diva.canvas.event.LayerEvent;
import diva.canvas.interactor.Interactor;
import diva.canvas.interactor.SelectionInteractor;

/** A figure that wraps a whole canvas pane.  This class is thus one
 * of the ways in which a canvas pane can be nested within other
 * canvas panes.  It can be given a figure that is drawn as the
 * background or outline of the pane.  Once an instance of this class
 * has been created, the wrapped pane cannot be changed.
 *
 * <p> In order to pass events down into the contained pane,
 * the PaneWrapper implements EventAcceptor. It forwards events to the
 * internal pane.
 *
 * @version        $Id$
 * @author John Reekie
 * @Pt.AcceptedRating Red
 */
public class PaneWrapper extends AbstractFigure implements EventAcceptor {
    /** The flag saying whether to clip.
     */
    private boolean _clipEnabled = true;

    /** The contained pane.
     */
    private CanvasPane _wrappedPane = null;

    /** The background figure
     */
    private Figure _background = null;

    /** Create a new pane figure with the given pane. The coordinate
     * transform will be unity. After creating, the coordinate transform
     * can be changed with setTransform(), and a background figure added
     * if desired with setBackground().
     */
    public PaneWrapper(CanvasPane pane) {
        super();
        _wrappedPane = pane;
        pane.setParent(this);
    }

    /** Dispatch an AWT event on this pane figure. Currently only
     * layer events are handled.
     */
    @Override
    public void dispatchEvent(AWTEvent event) {
        if (event instanceof LayerEvent) {
            processLayerEvent((LayerEvent) event);
        } else {
            // FIXME
            System.out.println("Bad event: " + event);
        }
    }

    /** Get the background figure.
     */
    public Figure getBackground() {
        return _background;
    }

    /** Get the shape of this figure. This will be the shape of
     * the background if there is one, otherwise a rectangle
     * formed by taking the size of the contained pane and
     * converting it with its transform.
     */
    @Override
    public Shape getShape() {
        if (_background != null) {
            return _background.getShape();
        } else {
            Point2D d = _wrappedPane.getSize();
            Rectangle2D r = new Rectangle2D.Double(0.0, 0.0, d.getX(), d.getY());
            AffineTransform at = _wrappedPane.getTransformContext()
                    .getTransform();
            return at.createTransformedShape(r);
        }
    }

    /** Get the wrapped pane
     */
    public CanvasPane getWrappedPane() {
        return _wrappedPane;
    }

    /** Get the clipping enabled flag.
     */
    public boolean isClipEnabled() {
        return _clipEnabled;
    }

    /** Test the enabled flag of the wrapped pane. If true, then events
     * on this figure will be passed to the wrapped pane; otherwise, the
     * whole pane will be treated as though it were a single figure, and
     * events passed to its event dispatcher, if it has one.
     */
    @Override
    public boolean isEnabled() {
        return _wrappedPane.isEnabled();
    }

    /** Paint the pane figure. The background (if any) is drawn first, then
     * the wrapped pane. The contents of the pane are clipped to the
     * background or to the size of the pane. (Note: the clip algorithm
     * appears to change the way that lines are rendered in the presence
     * of scaling. Don't know why...)
     */
    @Override
    public void paint(Graphics2D g) {
        // Paint the background, if any
        if (_background != null) {
            _background.paint(g);
        }

        // Set the clip region. This could probably be made more
        // efficient by downcasting to Rectangle2D where possible
        Shape currentClip = g.getClip();

        if (isClipEnabled() && currentClip != null) {
            // Note: clip screws up down-scaled lines
            // Note: we need to take the intersection of the current
            // clip with the background figure. This is probably slow...
            // FIXME: Optimize for rectangles
            Area a = new Area(currentClip);
            a.intersect(new Area(getShape()));
            g.setClip(a);

            // g.setClip(getShape());
            // Paint the pane
            _wrappedPane.paint(g);

            // Restore the clip region
            g.setClip(currentClip);
        } else {
            // Paint the pane
            _wrappedPane.paint(g);
        }
    }

    /** Paint the pane figure within the given region. The background
     * (if any) is drawn first, then the wrapped pane. The contents of
     * the pane are clipped to the background or to the size of the
     * pane. (Note: the clip algorithm appears to change the way that
     * lines are rendered in the presence of scaling. Don't know
     * why...)
     */
    @Override
    public void paint(Graphics2D g, Rectangle2D region) {
        // Paint the background, if any
        if (_background != null) {
            _background.paint(g, region);
        }

        // Set the clip region. This could probably be made more
        // efficient by downcasting to Rectangle2D where possible
        Shape currentClip = g.getClip();

        if (isClipEnabled() && currentClip != null) {
            // Note: clip screws up down-scaled lines
            // Note: we need to take the intersection of the current
            // clip with the background figure. This is probably slow...
            // FIXME: Optimize for rectangles
            Area a = new Area(currentClip);
            a.intersect(new Area(getShape()));
            g.setClip(a);

            // g.setClip(getShape());
            // Paint the pane
            _wrappedPane.paint(g, region);

            // Restore the clip region
            g.setClip(currentClip);
        } else {
            // Paint the pane
            _wrappedPane.paint(g, region);
        }
    }

    /** Process a layer event.  If the wrapped pane is not enabled, just
     * return. Otherwise, generate a new layer event with coordinates
     * in transform context of the wrapped pane and pass the new event
     * to the pane.
     *
     * <p> Currently, this methods also implements a simple technique
     * to manage event-handling between the "inner" and "outer"
     * panes. If this PaneWrapper has a selection interactor, and
     * it is in the selection, then don't process the event. This
     * means that the outer pane gets to handle all events if
     * the wrapper has already been selected.
     */
    protected void processLayerEvent(LayerEvent event) {
        if (!isEnabled()) {
            return;
        }

        // See whether to handle this event
        // FIXME This is only temporary
        Interactor r = getInteractor();

        if (r != null && r instanceof SelectionInteractor) {
            if (((SelectionInteractor) r).getSelectionModel()
                    .containsSelection(this)) {
                return;
            }
        }

        // Transform the layer coordinates in the event if needed
        double savedX = event.getLayerX();
        double savedY = event.getLayerX();
        AffineTransform at = _wrappedPane.getTransformContext()
                .getInverseTransform();

        if (!at.isIdentity()) {
            event.transform(at);
        }

        // Process it on the pane and then restore coordinates
        _wrappedPane.dispatchEvent(event);
        event.setLayerX(savedX);
        event.setLayerY(savedY);
    }

    /** Accept notification that a repaint has occurred in the wrapped
     * pane.  This implementation forwards the notification to its parent.
     */
    @Override
    public void repaint(DamageRegion d) {
        if (getParent() != null) {
            getParent().repaint(d);
        }
    }

    /** Set the background figure.
     */
    public void setBackground(Figure background) {
        if (background != null) {
            background.setParent(null);
        }

        this._background = background;
        if (background != null) {
            background.setParent(this);
        }
        repaint();
    }

    /** Set the clipping enabled flag. If true, the clip
     * region will be set to the outline of the figure
     * before painting the contents. This is on by default,
     * but if the pane is well-behaved, this should be turned
     * off as it seems to slow things down.
     */
    public void setClipEnabled(boolean flag) {
        _clipEnabled = flag;
    }

    /** Set the enabled flag of the wrapped pane. If true, then events
     * on this figure will be passed to the wrapped pane; otherwise, the
     * whole pane will be treated as though it were a single figure, and
     * events passed to its event dispatcher, if it has one.
     */
    @Override
    public void setEnabled(boolean flag) {
        _wrappedPane.setEnabled(flag);
    }

    /** Set the transform of the internal pane, relative to the
     * external one. This call will not affect the background
     * figure.
     */
    public void setTransform(AffineTransform at) {
        repaint();
        _wrappedPane.getTransformContext().setTransform(at);
        repaint();
    }

    /** Transform the figure with the supplied transform. The background,
     *  if any, will also be transformed.
     */
    @Override
    public void transform(AffineTransform at) {
        repaint();

        if (_background != null) {
            _background.transform(at);
        }

        _wrappedPane.getTransformContext().preConcatenate(at);
        repaint();
    }

    /** Translate this pane wrapper the given distance.
     */
    @Override
    public void translate(double x, double y) {
        repaint();

        if (_background != null) {
            _background.translate(x, y);
        }

        _wrappedPane.getTransformContext().translate(x, y);
        repaint();
    }
}
