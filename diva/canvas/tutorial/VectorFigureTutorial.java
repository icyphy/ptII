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

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

import javax.swing.SwingUtilities;

import diva.canvas.FigureLayer;
import diva.canvas.GraphicsPane;
import diva.canvas.JCanvas;
import diva.canvas.interactor.BoundsManipulator;
import diva.canvas.interactor.SelectionInteractor;
import diva.canvas.toolbox.BasicController;
import diva.canvas.toolbox.VectorFigure;
import diva.gui.BasicFrame;


/** An example showing how to use VectorFigure.
 *
 * <p>
 * <img src="../../../../packages/canvas/tutorial/images/VectorFigureTutorial.gif" align="right">
 *
 * The VectorFigure class provides a simple way to make figures that
 * are fairly complex graphically. It has no inherent shape, but draws
 * a series of shapes that are added one at a time to it.
 *
 * @author John Reekie
 * @version $Id$
 */
public class VectorFigureTutorial {

    // The JCanvas
    private JCanvas canvas;

    // The GraphicsPane
    private GraphicsPane graphicsPane;

    /** Create a JCanvas and put it into a window.
     */
    public VectorFigureTutorial () {
        canvas = new JCanvas();
        graphicsPane = (GraphicsPane)canvas.getCanvasPane();
        createFigures();
        BasicFrame frame = new BasicFrame("Vector figure tutorial", canvas);
    }

    /** Create instances of Vector Figures and make them
     * draggable and resizeable.
     */
    public void createFigures () {
        FigureLayer layer = graphicsPane.getForegroundLayer();

        // Create a controller to do the work.
        BasicController controller = new BasicController(graphicsPane);
        SelectionInteractor defaultInteractor
            = controller.getSelectionInteractor();
        BoundsManipulator manip = new BoundsManipulator();
        defaultInteractor.setPrototypeDecorator(manip);

        // Create a simple Vector Figure that draws a cross
        VectorFigure one = new VectorFigure();
        one.add(new Line2D.Double(0.0, 0.0, 100.0, 100.0));
        one.add(new Line2D.Double(100.0, 0.0, 0.0, 100.0));
        layer.add(one);
        one.setInteractor(defaultInteractor);

        // Here's a more complicated one, where we explicitly set the
        // shape to be a circle.
        VectorFigure two = new VectorFigure();
        Shape circle = new Ellipse2D.Double(0.0, 0.0, 100.0, 100.0);

        // Draw some filled circles
        two.fillMode();
        two.setShape(circle);
        two.add(Color.blue);
        two.add(circle);

        two.add(Color.yellow);
        two.add(new Ellipse2D.Double(10.0, 10.0, 80.0, 80.0));

        two.add(Color.red);
        two.add(new Ellipse2D.Double(20.0, 20.0, 60.0, 60.0));

        // Draw some lines
        two.lineMode();
        two.add(Color.black);
        two.add(new Line2D.Double(14.65, 14.65, 85.35, 85.35));
        two.add(new Line2D.Double(85.35, 14.65, 14.65, 85.35));

        two.translate(200,100);
        layer.add(two);
        two.setInteractor(defaultInteractor);
    }

    /** Main function
     */
    public static void main (String argv[]) {
        // Always invoke graphics code in the event thread
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    VectorFigureTutorial ex = new VectorFigureTutorial();
                }
            });
    }
}
