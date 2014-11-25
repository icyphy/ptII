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
package diva.gui.toolbox;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import diva.canvas.CanvasLayer;
import diva.canvas.CanvasUtilities;
import diva.canvas.JCanvas;
import diva.canvas.demo.SimplePane;
import diva.util.java2d.ShapeUtilities;

/**
 * A canvas panner is a window that provides a mechanism to visualize
 * and manipulate a JCanvas object.  Unlike the JPanner class, this
 * class is only useful for a JCanvas.  It has the advantage that it
 * handles the infinite space metaphor of the canvas better than the
 * JViewport, which implicitly assumes that a swing component
 * represents a finite, predetermined area.  This class contains a
 * complete, scaled down rendition of all of the 'interesting things'
 * contained in the canvas.  The bounds of the visible portion of the
 * canvas is visible on the panner as a red rectangle.  Clicking or
 * dragging within the JCanvasPanner centers the visible portion of
 * the canvas at that point on the component, without zooming in or
 * out.
 *
 * @author Steve Neuendorffer
 * @version $Id$
 */
@SuppressWarnings("serial")
public class JCanvasPanner extends JPanel {
    /**
     * The target window that is being wrapped.
     */
    private JCanvas _target = null;

    /**
     * The mouse listener on the panner that is responsible for scaling.
     */

    //  private ScaleMouseListener _scaleMouseListener = new ScaleMouseListener();
    /**
     * Construct a new panner that is initially viewing
     * nothing.  Use setCanvas() to assign it to something.
     */
    public JCanvasPanner() {
        this(null);
    }

    /**
     * Construct a new wrapper that wraps the given
     * target.
     */
    public JCanvasPanner(JCanvas target) {
        setCanvas(target);
        addMouseListener(new PanMouseListener());
        addMouseMotionListener(new PanMouseListener());

        // NOTE: Removed this listener, since it didn't work well.  EAL
        // _scaleMouseListener = new ScaleMouseListener();
    }

    /** Return the total size of everything in the canvas, in canvas
     *  coordinates.
     */
    public Rectangle2D getViewSize() {
        Rectangle2D viewRect = null;

        for (Iterator layers = _target.getCanvasPane().layers(); layers
                .hasNext();) {
            CanvasLayer layer = (CanvasLayer) layers.next();
            Rectangle2D rect = layer.getLayerBounds();

            if (!rect.isEmpty()) {
                if (viewRect == null) {
                    viewRect = rect;
                } else {
                    viewRect.add(rect);
                }
            }
        }

        if (viewRect == null) {
            // We can't actually return an empty rectangle, because then
            // we get a bad transform.
            return getVisibleSize();
        } else {
            return viewRect;
        }
    }

    /** Return the size of the visible part of the canvas, in canvas
     *  coordinates.
     */
    public Rectangle2D getVisibleSize() {
        AffineTransform current = _target.getCanvasPane().getTransformContext()
                .getTransform();
        AffineTransform inverse;

        try {
            inverse = current.createInverse();
        } catch (NoninvertibleTransformException e) {
            throw new RuntimeException(e.toString());
        }

        Dimension size = _target.getSize();
        Rectangle2D visibleRect = new Rectangle2D.Double(0, 0, size.getWidth(),
                size.getHeight());
        return ShapeUtilities.transformBounds(visibleRect, inverse);
    }

    /**
     *  Set the position of the viewport associated with this panner
     *  centered on the given position relative to the rendition shown in
     *  the panner.
     */
    public void setPosition(int x, int y) {
        // The total size of everything that is in the canvas.
        Rectangle2D viewRect = getViewSize();
        Rectangle2D visibleRect = getVisibleSize();

        // The total size of the panner.
        Rectangle myRect = _getInsetBounds();

        // The transform from the view to the panner.
        AffineTransform forward = CanvasUtilities.computeFitTransform(viewRect,
                myRect);

        // Note that inverse is NOT computeFitTransform(myRect, viewRect);
        AffineTransform inverse;

        try {
            inverse = forward.createInverse();
        } catch (NoninvertibleTransformException e) {
            throw new RuntimeException(e.toString());
        }

        Point2D newCenter = new Point2D.Double(x, y);

        // Transform the desired point.
        inverse.transform(newCenter, newCenter);

        // Place the center of the canvas at the desired point.
        AffineTransform newTransform = _target.getCanvasPane()
                .getTransformContext().getTransform();

        newTransform.translate(visibleRect.getCenterX() - newCenter.getX(),
                visibleRect.getCenterY() - newCenter.getY());

        _target.getCanvasPane().setTransform(newTransform);

        repaint();
    }

    /**
     * Set the target component that is being wrapped.
     */
    public void setCanvas(JCanvas target) {
        if (_target != null) {
            // _target.removeChangeListener(_listener);
            //         removeMouseListener(_scaleMouseListener);
            //removeMouseMotionListener(_scaleMouseListener);
        }

        _target = target;

        if (_target != null) {
            //  _target.addChangeListener(_listener);
            //addMouseListener(_scaleMouseListener);
            //addMouseMotionListener(_scaleMouseListener);
        }

        repaint();
    }

    /**
     * Return the target component that is being wrapped.
     */
    public JCanvas getCanvas() {
        return _target;
    }

    @Override
    public void paintComponent(Graphics g) {
        if (_target != null) {
            JCanvas canvas = _target;
            Rectangle2D viewRect = getViewSize();

            //  System.out.println("viewRect = " + viewRect);
            Rectangle myRect = _getInsetBounds();

            AffineTransform forward = CanvasUtilities.computeFitTransform(
                    viewRect, myRect);

            // Also invert the current transform on the canvas.
            AffineTransform current = canvas.getCanvasPane()
                    .getTransformContext().getTransform();

            AffineTransform inverse = null;

            // Here's a bug:
            // Open a new empty vergil window, and resize it to be
            // small enough that the panner window disappears.
            // An exception appears: "java.awt.geom.NoninvertibleTransformException: Determinant is 0"
            // So, we check to see if the determinants are greater than 0.0.
            if (current.getDeterminant() > 0.0
                    && forward.getDeterminant() > 0.0) {
                try {
                    forward.concatenate(current.createInverse());
                    inverse = forward.createInverse();
                } catch (NoninvertibleTransformException ex) {
                    throw new RuntimeException("Failed to create an inverse of an AffineTransform.\n viewRect: "
                            + viewRect
                            + "\ncurrent: " + current + " current determinant:" + current.getDeterminant()
                            + "\nforward: " + forward + " forward determinant:" + forward.getDeterminant(),
                            ex);
                }
            } else {
                // One or more determinates are 0, treat this like _target == null.
                Rectangle r = _getInsetBounds();
                g.clearRect(r.x, r.y, r.width, r.height);
                return;
            }

            Graphics2D g2d = (Graphics2D) g;
                    
            g2d.transform(forward);
            canvas.paint(g);
            g2d.transform(inverse);

            // Draw the Rectangles in untransformed coordinates, since we
            // always want them to show up 1 pixel wide.
            Dimension size = _target.getSize();
            Rectangle2D visibleRect = new Rectangle2D.Double(0, 0,
                    size.getWidth(), size.getHeight());
            visibleRect = ShapeUtilities.transformBounds(visibleRect, forward);

            g.setColor(Color.red);
            g.drawRect((int) visibleRect.getX(), (int) visibleRect.getY(),
                    (int) visibleRect.getWidth(), (int) visibleRect.getHeight());

            // NOTE: No longer meaningful, since always full space.

            /*      g.setColor(Color.blue);
             Dimension d = canvas.getSize();
             g.drawRect(0, 0, d.width, d.height);
             */
        } else {
            Rectangle r = _getInsetBounds();
            g.clearRect(r.x, r.y, r.width, r.height);
        }
    }

    // Return a rectangle that fits inside the border
    private Rectangle _getInsetBounds() {
        Dimension mySize = getSize();
        Insets insets = getInsets();

        // There is a little extra border...
        int border = 2;
        Rectangle myRect = new Rectangle(insets.left + border, insets.top
                + border, mySize.width - insets.top - insets.bottom - border,
                mySize.height - insets.left - insets.right - border);
        return myRect;
    }

    // This listener is attached to this panner and is responsible for
    // panning the target in response to a mouse click on the panner.
    private class PanMouseListener extends MouseAdapter implements
    MouseMotionListener {
        @Override
        public void mousePressed(MouseEvent evt) {
            if (_target != null
                    && (evt.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
                setPosition(evt.getX(), evt.getY());
            }
        }

        @Override
        public void mouseMoved(MouseEvent evt) {
        }

        @Override
        public void mouseDragged(MouseEvent evt) {
            if (_target != null
                    && (evt.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
                setPosition(evt.getX(), evt.getY());
            }
        }
    }

    /*
     private class ScaleMouseListener extends MouseAdapter
     implements MouseMotionListener {
     public Point2D origin = null;
     public Point2D scaled = null;
     public AffineTransform transformOrigin = null;
     public void setScale(int x, int y) {
     double scale;
     // The 5.0 and 1.3 below were determined by trial and error
     // tuning.
     if (x > origin.getX() && y > origin.getY()) {
     if (x - origin.getX() > y - origin.getY()) {
     scale = (y - origin.getY()) / 5.0;
     } else {
     scale = (x - origin.getX()) / 5.0;
     }
     } else if (x < origin.getX() && y < origin.getY()) {
     if (origin.getX() - x > origin.getY() - y) {
     scale = (y - origin.getY()) / 5.0;
     } else {
     scale = (x - origin.getX()) / 5.0;
     }
     } else {
     scale = 0.0;
     }
     scale = Math.pow(1.3, scale);
     JCanvas canvas = (JCanvas)_target.getView();

     AffineTransform current =
     canvas.getCanvasPane().getTransformContext().getTransform();
     current.setTransform(transformOrigin);
     current.translate(scaled.getX(), scaled.getY());
     current.scale(scale, scale);
     current.translate(-scaled.getX(), -scaled.getY());
     canvas.getCanvasPane().setTransform(current);
     }

     public void mousePressed(MouseEvent evt) {
     if (_target != null &&
     (evt.getModifiers() & MouseEvent.BUTTON3_MASK) != 0) {
     setPosition(evt.getX(), evt.getY());
     origin = evt.getPoint();
     JCanvas canvas = ((JCanvas)_target.getView());
     TransformContext context =
     canvas.getCanvasPane().getTransformContext();
     // clone the transform that is in the context, so we can
     // avoid a lot of repeated scaling of the same transform.
     transformOrigin =
     (AffineTransform)context.getTransform().clone();

     // Take the event and first transform it from the panner
     // coordinates into the view coordinates.
     Dimension viewSize =_target.getView().getSize();
     Rectangle viewRect =
     new Rectangle(0, 0, viewSize.width, viewSize.height);
     Rectangle myRect = _getInsetBounds();

     AffineTransform forward =
     CanvasUtilities.computeFitTransform(viewRect, myRect);

     double xScaled =
     (origin.getX() - myRect.getX()) / forward.getScaleX();
     double yScaled =
     (origin.getY() - myRect.getY()) / forward.getScaleY();
     scaled = new Point2D.Double(xScaled, yScaled);

     // Now transform from the view coordinates into the
     // pane coordinates.
     try {
     context.getInverseTransform().transform(scaled, scaled);
     } catch (Exception ex) {
     ex.printStackTrace();
     }
     }
     }
     public void mouseMoved(MouseEvent evt) {
     }
     public void mouseDragged(MouseEvent evt) {
     if (_target != null &&
     (evt.getModifiers() & MouseEvent.BUTTON3_MASK) != 0) {
     setScale(evt.getX(), evt.getY());
     }
     }
     }
     */
    public static void main(String[] argv) {
        try {
            // Run this in the Swing Event Thread.
            Runnable doActions = new Runnable() {
                @Override
                public void run() {
                    try {
                        JFrame f = new JFrame();

                        SimplePane rootPane = new SimplePane();
                        JCanvas canvas = new JCanvas(rootPane);
                        canvas.setSize(200, 200);

                        JCanvasPanner pan = new JCanvasPanner(canvas);
                        pan.setSize(50, 50);
                        pan.setPreferredSize(new Dimension(50, 50));
                        f.getContentPane().setLayout(new GridLayout(2, 1));
                        f.getContentPane().add(canvas);
                        f.getContentPane().add(pan);
                        f.pack();
                        f.setVisible(true);
                    } catch (Exception ex) {
                        System.err.println(ex.toString());
                        ex.printStackTrace();
                    }
                }
            };
            SwingUtilities.invokeAndWait(doActions);
        } catch (Exception ex) {
            System.err.println(ex.toString());
            ex.printStackTrace();
        }
    }
}
