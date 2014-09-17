/*
 Copyright (c) 1998-2006 The Regents of the University of California
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

import java.util.Iterator;
import java.util.NoSuchElementException;

import diva.canvas.event.EventLayer;

/**
 * A CanvasPane which has a default set of layers that are
 * useful for interactive drawing and editing applications.
 * The layers are organized as follows:
 *
 * <PRE>
 *   (front)  Foreground event
 *            Overlay
 *            Foreground graphics
 *            Background graphics
 *            Background event
 * </PRE>
 *
 * This organization allows applications to easily
 * overlay and underlay graphics and event handling
 * around the main application window. <p>
 *
 * Typical uses of each of these layers include:
 *
 * <dl>
 * <dt>Foreground event layer</dt>
 * <dd>Grid or object snapping, stroke filtering,
 * event monitoring and debugging, event grab, etc.
 * By default, this layer is not enabled. If you
 * enable it, but still want events to go through to
 * the underlying figure layers, use setConsuming(false).</dd>
 *
 * <dt>Overlay</dt>
 * <dd>An overlay layer for drag-selection outlining and so
 * on. By default, this layer is an instance of OverlayLayer
 * and is set visible.</dd>
 *
 * <dt>Foreground graphics</dt>
 * <dd>The main application graphics. By default, this layer
 * is an instance of FigureLayer and is set enabled and visible.</dd>
 *
 * <dt>Background graphics
 * <dd>Auxiliary, non-interactive graphics. By default,
 * this layer is an instance of FigureLayer but is set
 * not visible and not enabled.</dd>
 *
 *  <dt>Background Event</dt>
 * <dd>"Last chance" event handling layer,
 * for things such as drag-selection rectangle, panning/zooming, etc.
 * By default, this layer is enabled.</dd>
 *
 * </dl>
 *
 * @author Michael Shilman
 * @author John Reekie
 * @version        $Id$
 * @Pt.AcceptedRating Yellow
 */
public class GraphicsPane extends CanvasPane {
    /*
     * The container of layers, makes it easier to
     * do iterators.
     */
    protected CanvasLayer[] _layers;

    /*
     * The layers
     */
    protected EventLayer _foregroundEventLayer;

    protected OverlayLayer _overlayLayer;

    protected FigureLayer _foregroundLayer;

    protected CanvasLayer _backgroundLayer;

    protected EventLayer _backgroundEventLayer;

    /** Create a new Graphics pane with an instance of
     * FigureLayer as the main figure layer.
     */
    public GraphicsPane() {
        this(new FigureLayer());
    }

    /** Create a new Graphics pane with the passed Layer
     * as the main graphics pane.
     */
    public GraphicsPane(FigureLayer foregroundLayer) {
        // Background events
        _backgroundEventLayer = new EventLayer();
        _backgroundEventLayer.setEnabled(true);

        // Background layer is a figure layer
        _backgroundLayer = new FigureLayer();
        ((FigureLayer) _backgroundLayer).setVisible(false);
        ((FigureLayer) _backgroundLayer).setEnabled(false);

        // Foreground figures
        _foregroundLayer = foregroundLayer;

        // Overlay layer
        _overlayLayer = new OverlayLayer();

        // Overlay events
        _foregroundEventLayer = new EventLayer();
        _foregroundEventLayer.setEnabled(false);

        // Initialize all these layers.
        _initNewLayer(_backgroundEventLayer);
        _initNewLayer(_backgroundLayer);
        _initNewLayer(_foregroundLayer);
        _initNewLayer(_overlayLayer);
        _initNewLayer(_foregroundEventLayer);

        // Add them to the layer array.
        _rebuildLayerArray();
    }

    /** Get the background event layer
     */
    public EventLayer getBackgroundEventLayer() {
        return _backgroundEventLayer;
    }

    /** Get the background layer
     */
    public CanvasLayer getBackgroundLayer() {
        return _backgroundLayer;
    }

    /** Get the foreground layer
     */
    public FigureLayer getForegroundLayer() {
        return _foregroundLayer;
    }

    /** Get the overlay layer
     */
    public OverlayLayer getOverlayLayer() {
        return _overlayLayer;
    }

    /** Get the foreground event layer
     */
    public EventLayer getForegroundEventLayer() {
        return _foregroundEventLayer;
    }

    /** Return an iteration of the layers, in event-processing order
     * (that is, from front to back).
     */
    @Override
    public Iterator layersFromFront() {
        return new Iterator() {
            int cursor = 0;

            @Override
            public boolean hasNext() {
                return cursor < _layers.length;
            }

            @Override
            public Object next() throws NoSuchElementException {
                if (cursor > _layers.length) {
                    throw new NoSuchElementException();
                }
                return _layers[cursor++];
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException(
                        "Cannot delete layer from graphics pane");
            }
        };
    }

    /** Return an iteration of the layers, in redraw order (that is,
     * from back to front).
     */
    @Override
    public Iterator layersFromBack() {
        return new Iterator() {
            int cursor = _layers.length - 1;

            @Override
            public boolean hasNext() {
                return cursor >= 0;
            }

            @Override
            public Object next() throws NoSuchElementException {
                if (cursor < 0) {
                    throw new NoSuchElementException();
                }
                return _layers[cursor--];
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException(
                        "Cannot delete layer from graphics pane");
            }
        };
    }

    /** Set the background event layer
     */
    public void setBackgroundEventLayer(EventLayer l) {
        _nullifyLayer(_backgroundEventLayer);
        _backgroundEventLayer = l;
        _initNewLayer(l);
        _rebuildLayerArray();
    }

    /** Set the background figure layer
     */
    public void setBackgroundLayer(CanvasLayer l) {
        _nullifyLayer(_backgroundLayer);
        _backgroundLayer = l;
        _initNewLayer(l);
        _rebuildLayerArray();
    }

    /** Set the foreground figure layer
     */
    public void setForegroundLayer(FigureLayer l) {
        _nullifyLayer(_foregroundLayer);
        _foregroundLayer = l;
        _initNewLayer(l);
        _rebuildLayerArray();
    }

    /** Set the overlay layer
     */
    public void setOverlayLayer(OverlayLayer l) {
        _nullifyLayer(_overlayLayer);
        _overlayLayer = l;
        _initNewLayer(l);
        _rebuildLayerArray();
    }

    /** Set the foreground event layer
     */
    public void setForegroundEventLayer(EventLayer l) {
        _nullifyLayer(_foregroundEventLayer);
        _foregroundEventLayer = l;
        _initNewLayer(l);
        _rebuildLayerArray();
    }

    /** Rebuild the array of layers for use by iterators
     */
    protected void _rebuildLayerArray() {
        _layers = new CanvasLayer[5];

        int cursor = 0;
        _layers[cursor++] = _foregroundEventLayer;
        _layers[cursor++] = _overlayLayer;
        _layers[cursor++] = _foregroundLayer;
        _layers[cursor++] = _backgroundLayer;
        _layers[cursor++] = _backgroundEventLayer;
    }
}
