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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import diva.canvas.CanvasUtilities;
import diva.canvas.JCanvas;
import diva.canvas.TransformContext;

/**
 * A panner is a window that provides a mechanism to visualize and
 * manipulate a JViewport object without using scrollbars.  Unlike the
 * viewport, which contains a partial, full size rendition of the
 * contained component, this class contains a complete, scaled down
 * rendition of the component.  The bounds of the component are represented
 * by a blue rectangle and the bounds of the viewport on the component
 * are visible as a red rectangle. Clicking or dragging within the
 * JPanner centers the viewport at that point on the component.
 *
 * @author Michael Shilman
 * @author Steve Neuendorffer
 * @version $Id$
 */
@SuppressWarnings("serial")
public class JPanner extends JPanel {
    /**
     * The target window that is being wrapped.
     */
    private JViewport _target = null;

    /**
     * The scrolling listener;
     */
    private ScrollListener _listener = new ScrollListener();

    /**
     * The mouse listener on the panner that is responsible for scaling.
     */
    private ScaleMouseListener _scaleMouseListener = new ScaleMouseListener();

    /**
     * Construct a new panner that is initially viewing
     * nothing.  Use setViewport() to assign it to something.
     */
    public JPanner() {
        this(null);
    }

    /**
     * Construct a new wrapper that wraps the given
     * target.
     */
    public JPanner(JViewport target) {
        setViewport(target);
        addMouseListener(new PanMouseListener());
        addMouseMotionListener(new PanMouseListener());

        // NOTE: Removed this listener, since it didn't work well.  EAL
        // _scaleMouseListener = new ScaleMouseListener();
    }

    /**
     *  Set the position of the viewport associated with this panner
     *  centered on the given position relative to the rendition shown in
     *  the panner.
     */
    public void setPosition(int x, int y) {
        Dimension viewSize = _target.getView().getSize();
        Rectangle viewRect = new Rectangle(0, 0, viewSize.width,
                viewSize.height);
        Rectangle myRect = _getInsetBounds();

        AffineTransform forward = CanvasUtilities.computeFitTransform(viewRect,
                myRect);

        Dimension extentSize = _target.getExtentSize();

        x = (int) (x / forward.getScaleX()) - extentSize.width / 2;
        y = (int) (y / forward.getScaleY()) - extentSize.height / 2;

        int max;

        if (x < 0) {
            x = 0;
        }

        max = viewSize.width - extentSize.width;

        if (x > max) {
            x = max;
        }

        if (y < 0) {
            y = 0;
        }

        max = viewSize.height - extentSize.height;

        if (y > max) {
            y = max;
        }

        _target.setViewPosition(new Point(x, y));
    }

    /**
     * Set the target component that is being
     * wrapped.
     */
    public void setViewport(JViewport target) {
        if (_target != null) {
            _target.removeChangeListener(_listener);

            if (_target.getView() instanceof JCanvas) {
                removeMouseListener(_scaleMouseListener);
                removeMouseMotionListener(_scaleMouseListener);
            }
        }

        _target = target;

        if (_target != null) {
            _target.addChangeListener(_listener);

            if (_target.getView() instanceof JCanvas) {
                addMouseListener(_scaleMouseListener);
                addMouseMotionListener(_scaleMouseListener);
            }
        }

        repaint();
    }

    /**
     * Return the target component that is being
     * wrapped.
     */
    public JViewport getViewport() {
        return _target;
    }

    @Override
    public void paintComponent(Graphics g) {
        if (_target != null) {
            JCanvas canvas = null;
            try {
                canvas = (JCanvas) _target.getView();
            } catch (ClassCastException ex) {
                throw new RuntimeException("Failed to cast "
                        + _target.getView() + " to JCanvas.", ex);
            }
            Dimension viewSize = canvas.getSize();
            Rectangle viewRect = new Rectangle(0, 0, viewSize.width,
                    viewSize.height);

            Rectangle myRect = _getInsetBounds();

            AffineTransform forward = CanvasUtilities.computeFitTransform(
                    viewRect, myRect);

            // Also invert the current transform on the canvas.
            AffineTransform current = canvas.getCanvasPane()
                    .getTransformContext().getTransform();
            AffineTransform inverse;

            try {
                inverse = forward.createInverse();
                inverse.concatenate(current.createInverse());
            } catch (NoninvertibleTransformException e) {
                throw new RuntimeException(e.toString());
            }

            Graphics2D g2d = (Graphics2D) g;
            g2d.transform(forward);
            canvas.paint(g);

            g.setColor(Color.red);

            Rectangle r = _target.getViewRect();
            g.drawRect(r.x, r.y, r.width, r.height);

            /* NOTE: No longer meaningful, since always full space.
             g.setColor(Color.blue);
             Dimension d = canvas.getSize();
             g.drawRect(0, 0, d.width, d.height);
             */
            g2d.transform(inverse);
        } else {
            Rectangle r = _getInsetBounds();
            g.clearRect(r.x, r.y, r.width, r.height);
        }
    }

    // Return a rectangle that fits inside the border
    private Rectangle _getInsetBounds() {
        Dimension mySize = getSize();
        Insets insets = getInsets();
        Rectangle myRect = new Rectangle(insets.left, insets.top, mySize.width
                - insets.top - insets.bottom, mySize.height - insets.left
                - insets.right);
        return myRect;
    }

    //paint???
    private class ScrollListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            repaint();
        }
    }

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

    private class ScaleMouseListener extends MouseAdapter implements
    MouseMotionListener {
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

            JCanvas canvas = (JCanvas) _target.getView();

            AffineTransform current = canvas.getCanvasPane()
                    .getTransformContext().getTransform();
            current.setTransform(transformOrigin);
            current.translate(scaled.getX(), scaled.getY());
            current.scale(scale, scale);
            current.translate(-scaled.getX(), -scaled.getY());
            canvas.getCanvasPane().setTransform(current);
        }

        @Override
        public void mousePressed(MouseEvent evt) {
            if (_target != null
                    && (evt.getModifiers() & InputEvent.BUTTON3_MASK) != 0) {
                setPosition(evt.getX(), evt.getY());
                origin = evt.getPoint();

                JCanvas canvas = (JCanvas) _target.getView();
                TransformContext context = canvas.getCanvasPane()
                        .getTransformContext();

                // clone the transform that is in the context, so we can
                // avoid a lot of repeated scaling of the same transform.
                transformOrigin = (AffineTransform) context.getTransform()
                        .clone();

                // Take the event and first transform it from the panner
                // coordinates into the view coordinates.
                Dimension viewSize = _target.getView().getSize();
                Rectangle viewRect = new Rectangle(0, 0, viewSize.width,
                        viewSize.height);
                Rectangle myRect = _getInsetBounds();

                AffineTransform forward = CanvasUtilities.computeFitTransform(
                        viewRect, myRect);

                double xScaled = (origin.getX() - myRect.getX())
                        / forward.getScaleX();
                double yScaled = (origin.getY() - myRect.getY())
                        / forward.getScaleY();
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

        @Override
        public void mouseMoved(MouseEvent evt) {
        }

        @Override
        public void mouseDragged(MouseEvent evt) {
            if (_target != null
                    && (evt.getModifiers() & InputEvent.BUTTON3_MASK) != 0) {
                setScale(evt.getX(), evt.getY());
            }
        }
    }

    public static void main(String[] argv) {
        try {
            // Run this in the Swing Event Thread.
            Runnable doActions = new Runnable() {
                @Override
                public void run() {
                    try {
                        JFrame f = new JFrame();
                        String[] data = {
                                "oneeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee",
                                "twoooooooooooooooooooooooooooooooooooooooo",
                                "threeeeeeeeeeeeeeeee",
                        "fourrrrrrrrrrrrrrrrrrrrrrrrr" };
                        JList dataList = new JList(data);
                        JScrollPane p = new JScrollPane(dataList);
                        p.setSize(200, 200);

                        JPanner pan = new JPanner(p.getViewport());
                        pan.setSize(200, 200);
                        f.getContentPane().setLayout(new GridLayout(2, 1));
                        f.getContentPane().add(p);
                        f.getContentPane().add(pan);
                        f.setSize(200, 400);
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
