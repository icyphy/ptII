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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import javax.swing.SwingUtilities;

import diva.canvas.AbstractFigure;
import diva.canvas.FigureLayer;
import diva.canvas.GraphicsPane;
import diva.canvas.JCanvas;
import diva.canvas.interactor.BoundsManipulator;
import diva.canvas.interactor.SelectionInteractor;
import diva.canvas.toolbox.BasicController;
import diva.canvas.toolbox.ImageFigure;
import diva.gui.BasicFrame;
import diva.util.java2d.ShapeUtilities;


/** An example showing how to make custom figures.
 *
 * <p>
 * <img src="../../../../packages/canvas/tutorial/images/FigureTutorial.gif" align="right">
 *
 * Although the Diva Canvas provides a number of pre-built concrete
 * figures in <b>diva.canvas.toolbox</b> (BasicFigure is used in many
 * of the examples), you will in general want to define your own
 * Figure classes. The Diva Canvas deliberately does not attempt to
 * hide the power of the Java2D API, but instead to augment it. This
 * means that anything but the simplest types of Figure require some
 * knowledge of Java2D.
 *
 * <p> In general, defining a completely new type of Figure means
 * implementing the Figure interface. However, it is usually simpler
 * just to subclass the AbstractFigure class and override at least the
 * methods getShape(), transform(), and paint(). The example in this
 * file does that to create a new leaf figure.
 *
 * Our example (see the source file for the complete code) extends
 * AbstractFigure:
 *
 * <pre>
 *     public class CustomRectangle extends AbstractFigure {
 *         private Rectangle2D _bounds;
 *         public CustomRectangle (
 *                 double x, double y,
 *                 double width, double height) {
 *             _bounds = new Rectangle2D.Double(x,y,width,height);
 *         }
 *
 *         ....
 *     }
 * </pre>
 *
 * <p> The appearance of this figure is shown in the image above (the
 * yellow outline is the highlighting, and is not part of this figure).
 * It has a fill, a 1-point black outline, and some additional stuff
 * drawn on it.
 *
 * <p>
 * Let's look at the individual methods of this class. The getBounds()
 * method gets the shape of the figure's stroke outline, and then
 * takes the bounding box of that shape. (If we were to simply return
 * <b>_bounds</b>, part of the outline would fall outside the bounding
 * box and we would get "dirt" on the screen when we moved the figure.)
 *
 * <pre>
 *     public Rectangle2D getBounds () {
 *         Stroke s = new BasicStroke(1.0f);
 *         return s.createStrokedShape(_bounds).getBounds2D();
 *     }
 * </pre>
 *
 * <p>
 * The getShape() method simply returns the outline rectangle. getShape()
 * is used by methods in the Diva canvas to do things like place grab-handles
 * on figures and to put highlights around them:
 *
 * <pre>
 *     public Shape getShape () {
 *         return _bounds;
 *     }
 * </pre>
 *
 * <p>
 * The paint() method is where most of the work (in this example) is
 * done. Note that the argument is an instance of
 * <b>java.awt.Graphics2D</b>.  This method will be called in the event
 * thread whenever AWT redraws the JCanvas that contains this figure.
 * We won't show all of this method here, just the start:
 *
 * <pre>
 *     public void paint (Graphics2D g) {
 *         Stroke s = new BasicStroke(1.0f);
 *         g.setStroke(s);
 *         g.setPaint(Color.blue);
 *         g.fill(_bounds);
 *         g.setPaint(Color.black);
 *         g.draw(_bounds);
 *
 *         ....
 *      }
 * </pre>
 *
 * Finally, the transform() method transforms the figure. This method
 * is used whenever a figure is scaled or moved. Note that this code
 * calls the repaint() method before and after transforming the figure shape.
 * This ensures that the screen is properly redrawn.
 *
 * <pre>
 *     public void transform (AffineTransform at) {
 *         repaint();
 *         _bounds = (Rectangle2D) CanvasUtilities.transform(_bounds, at);
 *         repaint();
 *     }
 * </pre>
 *
 * There are other methods that may need to be over-ridden, depending
 * on what exactly your figure class does. For more information on the
 * methods of Figure, see the API documentation.
 *
 * <p>
 * A simpler, although somewhat slower executing, way to specify the look of a
 * figure is to create a GIF file that looks like the figure.
 * The GIF file can be loaded into an Image and the Image embedded
 * into a figure.   An ImageFigure does exactly this.
 *
 * <pre>
 *         // Create an image figure and make it draggable
 *         Image img = Toolkit.getDefaultToolkit().getImage(IMAGE_FILE_NAME);
 *         MediaTracker tracker = new MediaTracker(canvas);
 *         tracker.addImage(img,0);
 *         try {
 *             tracker.waitForID(0);
 *         }
 *         catch (InterruptedException e) {
 *             System.err.println(e + "... in FigureTutorial");
 *         }
 *         ImageFigure imgFig = new ImageFigure(img);
 *         imgFig.translate(300,100);
 *         layer.add(imgFig);
 *         imgFig.setInteractor(defaultInteractor);
 * </pre>
 *
 * The media tracker is responsible for waiting for the image to be completely
 * loaded from the file before creating the ImageFigure.  After being created,
 * the ImageFigure can be used exactly like other figures.
 *
 * <p> There are also other ways of creating new Figure classes.
 * You can subclass AbstractFigureContainer to produce a new figure
 * class that contains other figures. You can also subclass FigureWrapper
 * to add application-specific behavior to an existing figure class
 * by "wrapping" it.
 *
 * @author John Reekie
 * @version $Id$ */
public class FigureTutorial {
    // The file name for the image that is displayed
    public static final String IMAGE_FILE_NAME = "demo.gif";

    // The JCanvas
    private JCanvas canvas;

    // The GraphicsPane
    private GraphicsPane graphicsPane;

    /** Create a JCanvas and put it into a window.
     */
    public FigureTutorial () {
        canvas = new JCanvas();
        graphicsPane = (GraphicsPane)canvas.getCanvasPane();
        createFigures();
        BasicFrame frame = new BasicFrame("Figure tutorial", canvas);
    }

    /** Create instances of the class defined
     * in this file. To make the demo a little more interesting,
     * make them draggable.
     */
    public void createFigures () {
        FigureLayer layer = graphicsPane.getForegroundLayer();

        // Create a controller to do the work.
        BasicController controller = new BasicController(graphicsPane);
        SelectionInteractor defaultInteractor
            = controller.getSelectionInteractor();
        BoundsManipulator manip = new BoundsManipulator();
        defaultInteractor.setPrototypeDecorator(manip);

        // Create a custom rectangle and make it draggable
        AbstractFigure blue = new CustomRectangle(10.0,10.0,50.0,50.0);
        layer.add(blue);
        blue.setInteractor(defaultInteractor);
        blue.setToolTipText("Blue figure 1");

        // Create a custom rectangle and make it draggable
        AbstractFigure blue2 = new CustomRectangle(100.0,100.0,100.0,50.0);
        layer.add(blue2);
        blue2.setInteractor(defaultInteractor);
        blue2.setToolTipText("Blue figure 2");

        // Create an image figure and make it draggable
        Image img = Toolkit.getDefaultToolkit().getImage(IMAGE_FILE_NAME);
        MediaTracker tracker = new MediaTracker(canvas);
        tracker.addImage(img,0);
        try {
            tracker.waitForID(0);
        }
        catch (InterruptedException e) {
            System.err.println(e + "... in FigureTutorial");
        }
        ImageFigure imgFig = new ImageFigure(img);
        imgFig.translate(300,100);
        layer.add(imgFig);
        imgFig.setInteractor(defaultInteractor);
        imgFig.setToolTipText("Image figure");
    }

    /** Main function
     */
    public static void main (String argv[]) {
        // Always invoke graphics code in the event thread
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    FigureTutorial ex = new FigureTutorial();
                }
            });
    }

    //////////////////////////////////////////////////////////////////////
    //// CustomRectangle

    /** CustomRectangle is a class that paints itself as a
     * rectangle and draw a red plus sign over the top of itself.
     * This example figure class illustrates the use of different
     * paints and strokes to create the required image. It overrides
     * only the absolute minimum number of methods that must be
     * overridden to create a new figure class.
     */
    public class CustomRectangle extends AbstractFigure {
        // The bounds of the figure
        private Rectangle2D _bounds;

        /** Create a new instance of this figure. All we do here
         * is take the coordinates that we have been given and
         * remember them as a rectangle. In general, we may want
         * several constructors, and methods to set and get fields
         * that will control the visual properties of the figure.
         */
        public CustomRectangle (
                double x, double y,
                double width, double height) {
            _bounds = new Rectangle2D.Double(x,y,width,height);
        }

        /** Get the bounds of this figure. Because, in this example,
         * we have stroked the outline of the rectangle, we have
         * to create a new rectangle that is the bounds of the outside
         * of that stroke. In this method the stroke object is
         * being created each time, but it would normally be created
         * only once.
         */
        public Rectangle2D getBounds () {
            Stroke s = new BasicStroke(1.0f);
            return s.createStrokedShape(_bounds).getBounds2D();
        }

        /** Get the shape of this figure. In this example, it's
         * just the bounding rectangle that we stored in the
         * constructor. Note that in general, figures assume
         * that clients will not modify the object returned by
         * this method.
         */
        public Shape getShape () {
            return _bounds;
        }

        /**
         * Paint this figure onto the given graphics context.
         * We first paint the rectangle blue with a stroke
         * width of 1 unit, and then the plus sign with a stroke
         * width of 4 units. The implementation is fairly inefficient:
         * in general, we would want to cache the Stroke objects.
         * Note that we have to set the stroke and paint in the
         * graphics context before we can do anything useful.
         */
        public void paint (Graphics2D g) {
            // Create a stroke and fill then outline the rectangle
            Stroke s = new BasicStroke(1.0f);
            g.setStroke(s);
            g.setPaint(Color.blue);
            g.fill(_bounds);
            g.setPaint(Color.black);
            g.draw(_bounds);

            // Create a new stroke and draw the plus
            double x = _bounds.getX();
            double y = _bounds.getY();
            double w = _bounds.getWidth();
            double h = _bounds.getHeight();

            s = new BasicStroke(4.0f,
                    BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER);
            g.setStroke(s);
            g.setPaint(Color.red);
            g.draw(new Line2D.Double(x,y+h/2,x+w,y+h/2));
            g.draw(new Line2D.Double(x+w/2,y,x+w/2,y+h));
        }

        /** Transform the object. There are various ways of doing this,
         * some more complicated and some even morer complicated...
         * In this example, we use a utility function in the
         * class diva.canvas.CanvasUtils to transform the bounding
         * box. Both before and after we do the transformation,
         * we have to call the repaint() method so that the region
         * of the canvas that changed is properly repainted.
         */
        public void transform (AffineTransform at) {
            repaint();
            _bounds = (Rectangle2D)
                ShapeUtilities.transformBounds(_bounds, at);
            repaint();
        }
    }
}



