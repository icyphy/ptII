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

import java.awt.geom.Rectangle2D;

import diva.canvas.event.LayerEvent;

/** A canvas layer is a single layer that lives within a CanvasPane.
 * This is an abstract class -- concrete subclasses provide facilities
 * for drawing graphics or handling events.
 *
 * @version        $Id$
 * @author John Reekie
 * @Pt.AcceptedRating Yellow
 */
public abstract class CanvasLayer implements CanvasComponent {
    /** The pane containing this layer.
     */
    CanvasPane _containingPane;

    /** Create a new layer that is not in a pane. The layer will
     * not be displayed, and its coordinate transformation will be
     * as though it were a one-to-one mapping. Use of this constructor
     * is strongly discouraged, as many of the geometry-related methods
     * expect to see a pane.
     */
    public CanvasLayer() {
        _containingPane = null; // OK... be careful...
    }

    /** Create a new layer within the given pane.
     *  @param pane The pane in which to create the layer
     */
    public CanvasLayer(CanvasPane pane) {
        _containingPane = pane;
    }

    /** Get the pane containing this layer. This may be null.
     *  @return The pane containing the layter
     */
    public final CanvasPane getCanvasPane() {
        return _containingPane;
    }

    /** Get the bounds of the shapes draw in this layer.  In this base
     *  class, return an empty rectangle.
     *  @return The bounds of this layer.
     */
    public Rectangle2D getLayerBounds() {
        return new Rectangle2D.Double();
    }

    /** Get the parent component, or null if there isn't one.
     * This will return the same object as getCanvasPane().
     */
    @Override
    public final CanvasComponent getParent() {
        return _containingPane;
    }

    /** Get the toolTipText for the point in the given MouseEvent.
     *  This works pretty much as regular event propagation in
     *  processLayerEvent.
     *  @param e The layer event, ignored in this class.
     *  @return This method always returns null.
     */
    public String getToolTipText(LayerEvent e) {
        return null;
    }

    /** Return the transform context of the parent pane, if there is one.
     */
    @Override
    public final TransformContext getTransformContext() {
        if (_containingPane == null) {
            return null;
        } else {
            return _containingPane.getTransformContext();
        }
    }

    /** Schedule a repaint of this layer. The layer passes
     * the repaint request to its containing pane, if there is one.
     * Otherwise it does nothing.
     */
    @Override
    public void repaint() {
        if (_containingPane != null) {
            _containingPane.repaint();
        }
    }

    /** Accept notification that a repaint has occurred somewhere
     * in this layer. Pass the notification up to the parent pane.
     */
    @Override
    public void repaint(DamageRegion d) {
        if (_containingPane != null) {
            _containingPane.repaint(d);
        }
    }

    /** Set the parent component of this layer. This must be an
     * instance of CanvasPane.
     * @param parent The parent of this layer
     */
    public final void setParent(CanvasComponent parent) {
        if (!(parent instanceof CanvasPane)) {
            throw new IllegalArgumentException("The component " + parent
                    + " is not an instance of CanvasPane");
        }

        this._containingPane = (CanvasPane) parent;
    }
}
