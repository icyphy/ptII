/*
 * $Id$
 *
@Copyright (c) 1998-2004 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

						PT_COPYRIGHT_VERSION_2
						COPYRIGHTENDKEY


 *
 */

package diva.canvas.tutorial;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import javax.swing.SwingUtilities;

import diva.canvas.AbstractFigure;
import diva.canvas.Figure;
import diva.canvas.FigureLayer;
import diva.canvas.GraphicsPane;
import diva.canvas.JCanvas;
import diva.canvas.TransformContext;
import diva.canvas.interactor.DragInteractor;
import diva.canvas.interactor.Interactor;
import diva.gui.BasicFrame;
import diva.util.java2d.ShapeUtilities;

/** This tutorial shows how to make custom figures that contain
 * their own TransformContext.
 *
 * <img src="../../../../packages/canvas/tutorial/images/TransformedFigureTutorial.gif" align="right">
 *
 * In the FigureTutorial class, we
 * showed how to make a custom figure, and how to transform the
 * various 2D shapes in the paint() method. Here, we will use
 * an AffineTransform to do the same thing. This technique is
 * a little more work to figure out how to do, but it's probably
 * better if your figure has more than a couple of Shapes in it.
 *
 * <p> Transforms are a little tricky to get right, so the Diva
 * Canvas provides a class, TransformContext, that you need to use
 * to give a figure its own transform. Each instance of TransformContext
 * contains a single AffineTransform, and a bunch of methods that
 * deal with it.
 *
 * <p> The start of the CloudFigure class contains this code:
 *
 * <pre>
 *     private TransformContext _transformContext;
 *     private Rectangle2D _cachedBounds = null;
 *     private Shape _cachedShape = null;
 *
 *     public CloudFigure (
 *             double x, double y,
 *             double width, double height ) {
 *
 *         _transformContext = new TransformContext(this);
 *         AffineTransform at = _transformContext.getTransform();
 *         at.translate(x,y);
 *         at.scale(width/100, height/100);
 *         _transformContext.invalidateCache();
 *
 *         ....
 *     }
 * </pre>
 *
 * The initial shape of this figure is in fact a "cloud" shape
 * that is located at (0,0) and is 100 units on each side. The
 * internal transform is therefore initialized to scale this
 * shape to the requested coordinates.
 *
 * <p>
 * Now, because the shape of this figure is fairly expensive to
 * transform, the two instance variables <b>_cachedBounds</b> and
 * <b>_cachedShape</b> store the bounds and shape for the current
 * transform. If you look at the source code for this class, you will see
 * that these are created and remembered in getBounds() and
 * getShape(). In getShape(), for example, the internally-stored
 * shape needs to be transformed into "external" coordinates:
 *
 * <pre>
 *     public Shape getShape () {
 *         if (_cachedShape == null) {
 *             AffineTransform at = _transformContext.getTransform();
 *             _cachedShape = at.createTransformedShape(_shape);
 *         }
 *         return _cachedShape;
 *     }
 * </pre>
 *
 * Whenever the transform changes, these shapes must be cleared.
 * For example:
 *
 * <pre>
 *     public void transform (AffineTransform at) {
 *         repaint();
 *         _cachedShape = null;
 *         _cachedBounds = null;
 *         _transformContext.preConcatenate(at);
 *         repaint();
 *     }
 * </pre>
 *
 * <p>
 * The only other interesting thing about this class is the
 * paint() method. Because paint() is called recursively down
 * the tree of figures, the TransformContext class provides
 * two methods that "stack" transform contexts as the tree
 * is traversed. The paint() method calls push() and pop()
 * before and after painting the figure's contents:
 *
 * <pre>
 *     public void paint (Graphics2D g) {
 *         _transformContext.push(g);
 *
 *         ....
 *         // Paint the big cloud
 *         AlphaComposite c = AlphaComposite.getInstance(
 *                 AlphaComposite.SRC_OVER,0.5f);
 *         g.setComposite(c);
 *         g.setPaint(Color.magenta);
 *         g.fill(_shape);
 *
 *         ....
 *         _transformContext.pop(g);
 *     }
 * </pre>
 *
 * That's about all that's needed to use transform contexts
 * in a figure.
 *
 * @author John Reekie
 * @version $Revision$
 */
public class TransformedFigureTutorial {

    // The JCanvas
    private JCanvas canvas;

    // The GraphicsPane
    private GraphicsPane graphicsPane;

    /** Create a JCanvas and put it into a window.
     */
    public TransformedFigureTutorial () {
        canvas = new JCanvas();
        graphicsPane = (GraphicsPane)canvas.getCanvasPane();

        BasicFrame frame = new BasicFrame("Figure tutorial", canvas);
        frame.setSize(600,400);
        frame.setVisible(true);
    }

    /** Create instances of the class defined
     * in this file. To make the demo a little more interesting,
     * make them draggable.
     */
    public void createFigures () {
        FigureLayer layer = graphicsPane.getForegroundLayer();

        // Create an interactor to do the work.
        Interactor dragger = new DragInteractor();

        // Create the figure
        Figure one = new CloudFigure(10.0,10.0,80.0,80.0);
        layer.add(one);
        one.setInteractor(dragger);

        Figure two = new CloudFigure(150, 150, 200, 180);
        layer.add(two);
        two.setInteractor(dragger);
    }

    /** Main function
     */
    public static void main (String argv[]) {
        // Always invoke graphics code in the event thread
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    TransformedFigureTutorial ex = new TransformedFigureTutorial();
                    ex.createFigures();
                }
            });
    }

    //////////////////////////////////////////////////////////////////////
    //// CloudFigure

    /** CloudFigure is a class that paints itself as a
     * translucent "cloud."
     * This example figure class illustrates the use of different
     * paints and strokes to create the required image, and the use
     * of TransformContext to position that image.
     */
    public class CloudFigure extends AbstractFigure {
        // The cloud shape
        private Shape _shape;

        // Little cloud 1
        private Shape _cloud1;

        // Little cloud 2
        private Shape _cloud2;

        // The transform
        private TransformContext _transformContext;

        // The cached bounding box
        private Rectangle2D _cachedBounds = null;

        // The cached shape, in the external transform context
        private Shape _cachedShape = null;

        /** Create a new instance of this figure. The cloud is initially
         * created at coordinates (0,0) and then transformed to the requested
         * coordinates.
         * To create the cloud shape, use the Area class in Java2D, which
         * implements constructive area geometry, and join a bunch
         * of circles into a single shape.
         */
        public CloudFigure (
                double x, double y,
                double width, double height ) {

            // Create the transform context and initialize it
            // so that the figure is drawn at the requested coordinates
            _transformContext = new TransformContext(this);
            AffineTransform at = _transformContext.getTransform();
            at.translate(x,y);
            at.scale(width/100, height/100);
            _transformContext.invalidateCache();

            // Create the shape we will use to draw the figure
            //              Area area = new Area();
            //              Ellipse2D c = new Ellipse2D.Double();
            //              c.setFrame(0,25,50,50);
            //              area.add(new Area(c));
            //              c.setFrame(25,0,40,40);
            //              area.add(new Area(c));
            //              c.setFrame(25,25,60,60);
            //              area.add(new Area(c));
            //              c.setFrame(60,30,40,40);
            //              area.add(new Area(c));
            //              c.setFrame(60,10,30,30);
            //              area.add(new Area(c));
            //            _shape = area;
            _shape = ShapeUtilities.createSwatchShape();

            // Create the shapes for the little clouds. This could
            // also be done in the paint() method, but since it's
            // rather slow, we do it one in the constructor.
            // Watch it -- don't modify the main transform!
            Shape c = ShapeUtilities.createCloudShape();
            at = new AffineTransform();
            at.setToTranslation(20,20);
            at.scale(0.25,0.25);
            _cloud1 = at.createTransformedShape(c);

            at.setToTranslation(50,40);
            at.scale(0.4,0.4);
            _cloud2 = at.createTransformedShape(c);
        }

        /** Get the bounds of this figure. Because this figure has
         * its own transform, we need to transform the internal bounds
         * into the enclosing context. To make this more efficient,
         * we use a previously-cached copy of the transformed bounds
         * if there is one.
         */
        public Rectangle2D getBounds () {
            if (_cachedBounds == null) {
                _cachedBounds = getShape().getBounds2D();
            }
            return _cachedBounds;
        }

        /** Get the shape of this figure. Because this figure has
         * its own transform, we need to transform the internal shape
         * into the enclosing context. To make this more efficient,
         * we use a previously-cached copy of the transformed shape
         * if there is one.
         */
        public Shape getShape () {
            if (_cachedShape == null) {
                AffineTransform at = _transformContext.getTransform();
                _cachedShape = at.createTransformedShape(_shape);
            }
            return _cachedShape;
        }

        /** Get the transform context. This method must be overridden
         * since this figure defined its own context.
         */
        public TransformContext getTransformContext () {
            return _transformContext;
        }

        /** Paint this figure onto the given graphics context.
         * First we "push" the transform context onto the transform
         * stack, so that the graphics port has the correct transform.
         * Then we paint the cloud a translucent magenta (yum!),
         * and then we make a couple of little clouds and paint them
         * opaque. (The way this is done in this example is horrendously
         * inefficient.) Finally, we "pop" the transform context off
         * the stack.
         */
        public void paint (Graphics2D g) {
            // Push the context
            _transformContext.push(g);

            // Paint the big cloud
            AlphaComposite c = AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER,0.3f);
            g.setComposite(c);
            g.setPaint(Color.magenta);
            g.fill(_shape);

            // Paint the little clouds
            g.setComposite(AlphaComposite.SrcOver);
            g.setPaint(Color.red);
            g.fill(_cloud1);

            g.setPaint(Color.green);
            g.fill(_cloud2);

            // Pop the context
            _transformContext.pop(g);
        }

        /** Transform the object.
         * In this example, we pre-concatenate the given transform with
         * the transform in the transform context. When the figure is
         * repainted, it will be redrawn in the right place.
         * We also must be sure to invalidate the cached
         * geometry objects that depend on the transform.
         */
        public void transform (AffineTransform at) {
            repaint();
            _cachedShape = null;
            _cachedBounds = null;
            _transformContext.preConcatenate(at);
            repaint();
        }
    }
}


