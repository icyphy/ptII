/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.canvas.toolbox;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;

import diva.canvas.AbstractFigure;
import diva.canvas.CanvasComponent;
import diva.canvas.CanvasLayer;

/**
 * A figure that embeds swing components in canvas drawings.
 *
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @author John Reekie (johnr@eecs.berkeley.edu)
 * @version $Revision$
 */
public class SwingWrapper extends AbstractFigure {
    /**
     * The high-res version of the bounding box
     */
    /// private Rectangle2D _bounds;

    /**
     * The embedded swing component.
     */
    private JComponent _component;

    /**
     * Construct a new swing wrapper instance
     * to wrap the given component.
     */
    public SwingWrapper (JComponent c) {
        super();
        setComponent(c);
    }

    /** Get the bounding box of this figure. This method overrides
     * the inherited method to take account of the thickness of
     * the stroke, if there is one.
     */
    public Rectangle2D getBounds () {
        // We really want to return the high-res version of the bounding box
        return _component.getBounds();
    }

    /**
     * Return the component that this is
     * wrapping.
     */
    public JComponent getComponent () {
        return _component;
    }

    /**
     * Get the shape of this figure.
     */
    public Shape getShape () {
        return getBounds();
    }

    /**
     * Ask the wrapped component to paint itself.
     */
    public void paint (Graphics2D g) {
        if (isVisible()) {
            //Rectangle2D bounds = getBounds();
            //System.out.println("Painting component: " + bounds); //DEBUG
            //Graphics cg = g.create((int)bounds.getX(), (int)bounds.getY(),
            //        (int)bounds.getWidth(), (int)bounds.getHeight());
            //try {
            //    _component.paint(cg);
            //}
            //finally {
            //    cg.dispose();
            //}

            // This compiles but doesn't work right:
            _component.paint(g);

            // This is supposed to work right but doesn't compile:
            // _component.paintComponent(g);

            // Nor does this:
            //ComponentUI ui = UIManager.getUI(_component);
            //if (ui != null) {
            //    Graphics scratchGraphics = Graphics.createSwingGraphics(g);
            //    try {
            //        ui.update(scratchGraphics, this);
            //    }
            //    finally {
            //        scratchGraphics.dispose();
            //    }
            //}
        }
    }

    /**
     * Replace the currently wrapped component
     * with the given component and reshape/repaint
     * the figure.
     */
    public void setComponent (JComponent c) {
        _component = c;

        // Set the component's size to its preferred size
        c.setSize(c.getPreferredSize());

        // If the figure is already in a layer, set the canvas
        // as the parent of the component
        CanvasLayer layer = getLayer();
        if (layer != null) {
            layer.getCanvasPane().getCanvas().add(_component);
        }
        repaint();
    }

    /** Set the parent of this figure. This method overrides the
     * inherited method to deal with the Swing component hierarchy.
     */
    public void setParent (CanvasComponent fc) {
        super.setParent(fc);
        if (_component.getParent() == null) {
            CanvasLayer layer = getLayer();
            layer.getCanvasPane().getCanvas().setLayout(null);
            layer.getCanvasPane().getCanvas().add(_component);
        }
    }

    /**
     * Transform the figure with the supplied transform.  For
     * now this throws an UnsupportedOperationException because
     * it is unclear how we want to deal with transformations
     * that are not shape-preserving, such as shears and
     * rotations.
     */
    public void transform (AffineTransform at) {
        throw new UnsupportedOperationException("FIXME");
    }

    /**
     * Translate the figure by the given distance.
     */
    public void translate (double x, double y) {
        repaint();

        // This is not really right -- we want to keep a high-res
        // version of the bounding box
        if (_component != null) {
            Point p = _component.getLocation();
            _component.setLocation(p.x + (int)x, p.y + (int)y);
        }
        repaint();
    }
}


