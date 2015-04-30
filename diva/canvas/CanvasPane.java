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
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import javax.swing.DefaultBoundedRangeModel;

import diva.canvas.event.EventAcceptor;
import diva.canvas.event.LayerEvent;
import diva.util.java2d.ShapeUtilities;

/** A canvas pane groups canvas layers. The layers all share the same
 * logical-to-screen transform as the canvas pane. This is an abstract
 * superclass of all canvas panes, and provides the implementation of
 * everything related to panes but the storage of the
 * layers. Subclasses must provide methods to add and (possibly)
 * reorder and remove layers. Particular applications may choose
 * to create their own special-purpose sub-classes.
 *
 * @version        $Id$
 * @author John Reekie
 * @Pt.AcceptedRating Yellow
 */
public abstract class CanvasPane implements EventAcceptor, CanvasComponent {

    ///////////////////////////////////////////////////////////////////
    //// Public methods

    /** Dispatch an AWT event on this pane. Currently only
     * layer events are handled.
     * @param The event
     */
    @Override
    public void dispatchEvent(AWTEvent event) {
        if (event instanceof LayerEvent) {
            processLayerEvent((LayerEvent) event);
        } else {
            throw new IllegalArgumentException("Event type not recognized by "
                    + "CanvasPane.dispatchEvent: " + event);
        }
    }

    /** Get the parent component, or null if there isn't one.
     * Only one of the canvas or the display parent can be non-null.
     * @return the parent component or null.
     */
    @Override
    public final CanvasComponent getParent() {
        return _parent;
    }

    /** Get the containing canvas, or null if there isn't one.
     * Only one of the canvas or the display parent can be non-null.
     * @return the containing canvas or null.
     */
    public final JCanvas getCanvas() {
        return _canvas;
    }

    /** Return whether or not this pane is antialiased.
     *  @return true if the pane is antialiased.   
     */
    public final boolean isAntialiasing() {
        return _antialias;
    }

    /** Get the toolTipText for the point in the given LayerEvent.
     *  Pass the event to all the layers in this canvas pane in order from
     *  front to back.   The returned value will be the first non-null
     *  tooltip string returned by one of the layers.  If all the layers
     *  return a null tooltip, then return null indicating that no
     *  tooltip should be displayed.
     *  @param event The event
     *  @return The tool tip or null
     */
    protected String getToolTipText(LayerEvent event) {
        if (!isEnabled()) {
            return null;
        }

        Iterator i = layersFromFront();

        while (i.hasNext()) {
            CanvasLayer layer = (CanvasLayer) i.next();

            // Set the layer source to the layer, then
            // pass the event to that layer
            event.setLayerSource(layer);

            String tip = layer.getToolTipText(event);

            if (tip != null) {
                return tip;
            }
        }

        return null;
    }

    /** Return the transform context of this pane.
     *  @return the transform context.
     */
    @Override
    public final TransformContext getTransformContext() {
        return _transformContext;
    }

    /** Get the size of this pane, in logical coordinates.
     * If the pane is contained directly in a JCanvas, the
     * size is obtained from the JCanvas. Otherwise, it
     * returns the size previously set with setSize().
     * @return the size
     */
    public Point2D getSize() {
        if (_canvas != null) {
            Dimension d = _canvas.getSize();
            Point2D s = new Point2D.Double(d.width, d.height);
            _transformContext.getTransform().transform(s, s);
            return s;
        } else {
            return _paneSize;
        }
    }

    /** Test the enabled flag of this pane. Note that this flag
     *  does not indicate whether the pane is actually enabled,
     *  as its canvas or one if its ancestors may not be enabled.
     *  @return True if the pane is enabled.
     */
    @Override
    public final boolean isEnabled() {
        return _enabled;
    }

    /** Return an iteration of the layers, in undefined order. The
     * default implementation simply calls layersFromFront().
     * @return The iterator.
     */
    public Iterator layers() {
        return layersFromFront();
    }

    /** Return an iteration of the layers from back to front --
     * that is, in redraw order.
     * @return The iterator.
     */
    public abstract Iterator layersFromBack();

    /** Return an iteration of the layers from front to back --
     * that is, in event-processing order.
     * @return The iterator.
     */
    public abstract Iterator layersFromFront();

    /** Paint this pane onto a 2D graphics context. This implementation
     * paints all layers that implement VisibleComponent, from back
     * to front.  The transform of this pane is written
     * into the graphics context, so any layer that changes the
     * transform is obliged to return it to its prior state after
     * finishing.
     * @param g The graphics context on which to paint.
     */
    public void paint(Graphics2D g) {
        _transformContext.push(g);

        if (isAntialiasing()) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
        }

        CanvasLayer layer;
        Iterator i = layersFromBack();

        while (i.hasNext()) {
            layer = (CanvasLayer) i.next();

            if (layer instanceof VisibleComponent) {
                ((VisibleComponent) layer).paint(g);
            }
        }

        _transformContext.pop(g);
    }

    /** Paint this pane onto a 2D graphics object, within the given
     * region.  This implementation paints all layers that implement
     * VisibleComponent, from highest index to lowest index.  The
     * transform of this pane is written into the graphics context, so
     * any layer that changes the transform is obliged to return it to
     * its prior state after finishing.
     * @param g The graphics context on which to paint.
     * @param region The object to be painted.
     */
    public void paint(Graphics2D g, Rectangle2D region) {
        _transformContext.push(g);

        if (isAntialiasing()) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
        }

        // Transform the region to paint as well
        AffineTransform t = _transformContext.getInverseTransform();
        region = ShapeUtilities.transformBounds(region, t);

        // paint the VisibleComponents
        CanvasLayer layer;
        Iterator i = layersFromBack();

        while (i.hasNext()) {
            layer = (CanvasLayer) i.next();

            if (layer instanceof VisibleComponent) {
                ((VisibleComponent) layer).paint(g, region);
            }
        }

        _transformContext.pop(g);
    }

    /** Process a layer event that has occurred on this pane.  If the
     * pane is not enabled, return without doing anything. Otherwise,
     * pass the event to each layer that implements the EventAcceptor
     * interface, from front to back. After each layer,
     * check whether the event has been consumed, and return if so.
     * @param event The layer event
     */
    protected void processLayerEvent(LayerEvent event) {
        if (!isEnabled()) {
            return;
        }

        Iterator i = layersFromFront();

        while (i.hasNext()) {
            CanvasLayer layer = (CanvasLayer) i.next();

            // Process on this layer only if it implements EventAccepter
            if (layer instanceof EventAcceptor) {
                EventAcceptor acceptor = (EventAcceptor) layer;

                // Set the layer source to the layer, then
                // pass the event to that layer
                event.setLayerSource(layer);
                acceptor.dispatchEvent(event);

                // stop if the event was consumed
                if (event.isConsumed()) {
                    break;
                }
            }
        }
    }

    /** Schedule a repaint of this pane. The pane passes
     * the repaint request to its parent, if it has one.
     */
    @Override
    public void repaint() {
        if (_canvas != null) {
            _canvas.repaint();
        } else if (_parent != null) {
            _parent.repaint();
        }
    }

    /** Accept notification that a repaint has occurred somewhere
     * in this pane. Notify the damage region that it is passing
     * through a transform context, and then forward the
     * notification up to the parent.
     * @param d The damage region.
     */
    @Override
    public void repaint(DamageRegion d) {
        // Check the transform cache
        d.checkCacheValid(_transformContext);

        // Forward to parent
        if (_canvas != null) {
            _canvas.repaint(d);
        } else if (_parent != null) {
            _parent.repaint(d);
        }
    }

    /** Set whether or not to use antialiasing
     * when drawing this pane.
     * @param val True if antialiasing is used when drawing.
     */
    public void setAntialiasing(boolean val) {
        _antialias = val;
    }

    /** Set the containing canvas of this pane. If the canvas is
     * not null and the parent is not null, throw an exception.
     * This method is not intended for general use, only by JCanvas
     * and subclasses.
     * @param canvase The containing canvas.
     */
    public final void setCanvas(JCanvas canvas) {
        if (canvas != null && _parent != null) {
            throw new IllegalArgumentException("CanvasPane " + this
                    + " is already contained in another CanvasComponent. \n"
                    + "Cannot set the parent canvas to " + canvas);
        }

        this._canvas = canvas;
    }

    /** Set the enabled flag of this pane.
     * @param flag If false, then the pane will not respond to user
     * input events.
     */
    @Override
    public final void setEnabled(boolean flag) {
        _enabled = flag;
    }

    /** Set the parent component of this pane. If the parent is not
     * null and the canvas is not null, throw an exception.
     * @param parent The parent of the pane.
     */
    public final void setParent(CanvasComponent parent) {
        if (parent != null && _canvas != null) {
            throw new IllegalArgumentException("CanvasPane " + this
                    + " is already contained in a JCanvas. \n"
                    + "Cannot set the parent component to " + parent);
        }

        if (_transformContext != null) {
            _transformContext.invalidateCache();
        }

        this._parent = parent;
    }

    /** Set the size of this pane, in logical coordinates. If the
     * pane is directly contained by a JCanvas, subsequent calls to
     * the getSize() and getPreferredSize() methods of the JCanvas
     * will return the size set here.
     * @param width The width
     * @param height The height
     */
    public void setSize(double width, double height) {
        setSize(new Point2D.Double(width, height));
    }

    /** Set the size of this pane, in logical coordinates. If the
     * pane is directly contained by a JCanvas, subsequent calls to
     * the getSize() and getPreferredSize() methods of the JCanvas
     * will return the size set here.
     * @param size The size of the pane.
     */
    public void setSize(Point2D size) {
        _paneSize = size;
        updateRangeModel();
    }

    /** Set the transform that maps logical coordinates into the
     * parent's coordinates. If there is no parent, the "parent" is
     * taken to be the screen. An exception will be thrown if
     * the transform is null. Note that the transform will
     * be remembered by this pane, so any further changes to the
     * transform will affect the pane.
     * This version has a flag that can be used to avoid calling
     * the 'updateRangeModel' method
     * @param at The transform.
     */
    public final void setTransform(AffineTransform at) {
        _transformContext.setTransform(at);
        repaint();
        updateRangeModel();
    }

    /** Translate this pane the given distance. The translation is
     * done such that it works "correctly" in the presence of scaling.
     * @param x The x amount by which to translate.
     * @param y The y amount by which to translate.
     */
    public void translate(double x, double y) {
        _transformContext.translate(x, y);
        repaint();
    }

    /** Scale this pane the given amount.
     * @param xcenter The x value of the point to scale about.
     * @param ycenter The y value of the point to scale about.
     * @param xscale The x amount of which to scale by. 
     * @param yscale The y amount of which to scale by. 
     */
    public void scale(double xcenter, double ycenter, double xscale,
            double yscale) {
        // Construct the translation transform
        AffineTransform at = new AffineTransform();

        at.translate(xcenter, ycenter);
        at.scale(xscale, yscale);
        at.translate(-xcenter, -ycenter);

        // Preconcatenate the pane's transform with it and refresh
        _transformContext.getTransform().preConcatenate(at);
        _transformContext.invalidateCache();
        repaint();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Helper method to initialize a layer when it is added to this
     * pane. Any subclass must be sure to call this whenever it
     * creates a new layer or accepts one to add to itself.
     * @param l The canvas layer
     */
    protected void _initNewLayer(CanvasLayer l) {
        if (l._containingPane != null) {
            throw new IllegalArgumentException("The layer " + l
                    + " does not have its _containingPane set to null");
        }

        l._containingPane = this;
    }

    /** Helper method to tell a layer when it is been removed from
     * this pane. Any subclass must be sure to call this whenever it
     * removes a layer.
     * @param l The canvas layer
     */
    protected void _nullifyLayer(CanvasLayer l) {
        if (l._containingPane != this) {
            throw new IllegalArgumentException("The layer " + l
                    + " does not have its _containingPane set to " + this);
        }

        l._containingPane = null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * set the model params for the range models.  This sets the min, max
     * value and extent which can be used by a panner or scrollbars
     */
    private void updateRangeModel() {
        if (_canvas.getCanvasPane() == this) { //make sure this pane is the top one.

            DefaultBoundedRangeModel _horizontalRangeModel = (DefaultBoundedRangeModel) _canvas
                    .getHorizontalRangeModel();
            DefaultBoundedRangeModel _verticalRangeModel = (DefaultBoundedRangeModel) _canvas
                    .getVerticalRangeModel();
            Rectangle2D viewsize = _canvas.getViewSize();
            Rectangle2D vissize = _canvas.getVisibleSize();

            int visWidth = (int) vissize.getWidth();
            int visHeight = (int) vissize.getHeight();
            int visX = (int) vissize.getX();
            int visY = (int) vissize.getY();
            int viewWidth = (int) viewsize.getWidth();
            int viewHeight = (int) viewsize.getHeight();
            int viewX = (int) viewsize.getX();
            int viewY = (int) viewsize.getY();

            _verticalRangeModel.setMinimum(-viewHeight);
            _horizontalRangeModel.setMinimum(-viewWidth);

            _verticalRangeModel.setMaximum(viewY + 2 * viewHeight);
            _horizontalRangeModel.setMaximum(viewX + 2 * viewWidth);

            _verticalRangeModel.setExtent(visHeight);
            _horizontalRangeModel.setExtent(visWidth);

            _verticalRangeModel.setValue(visY);
            _horizontalRangeModel.setValue(visX);
        }
    }
    /** The parent component.
     */
    private CanvasComponent _parent = null;

    /** The parent canvas.
     */
    private JCanvas _canvas = null;

    /** The enabled flag, which defaults to true.
     */
    private boolean _enabled = true;

    /** The antialiasing flag, which defaults to true.
     */
    private boolean _antialias = true;

    /** The size of this pane, in logical coordinates.
     */
    private Point2D _paneSize = new Point2D.Double(100.0, 100.0);

    /** The transform context of this pane.
     */
    private TransformContext _transformContext = new TransformContext(this);
}
