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
import java.awt.geom.Rectangle2D;

import javax.swing.SwingUtilities;

import diva.canvas.FigureLayer;
import diva.canvas.GraphicsPane;
import diva.canvas.JCanvas;
import diva.canvas.event.MouseFilter;
import diva.canvas.interactor.BoundedDragInteractor;
import diva.canvas.interactor.DragInteractor;
import diva.canvas.interactor.Interactor;
import diva.canvas.toolbox.BasicEllipse;
import diva.canvas.toolbox.BasicFigure;
import diva.canvas.toolbox.BasicRectangle;
import diva.gui.BasicFrame;

/** An example showing how to make figures draggable with interactors.
 *
 * <img src="../../../../packages/canvas/tutorial/images/DragTutorial.gif" align="right">
 *
 * Each figure on the canvas can have its
 * interactor set or read with the methods setInteractor()
 * and getInteractor(). The interactor answers the
 * question "What does this figure do when I mouse on it?" In this
 * example, what the figure does is follow the mouse.
 *
 * <p>
 * To make a figure draggable, we create an instance of
 * DragInteractor and attach it to the figure, like this:
 *
 * <pre>
 *     Interactor dragger = new DragInteractor();
 *     dragger.setMouseFilter(MouseFilter.defaultFilter);
 *
 *     BasicFigure blue = new BasicRectangle(10.0,10.0,50.0,50.0,Color.blue);
 *     layer.add(blue);
 *     blue.setInteractor(dragger);
 * </pre>
 *
 * The mouse filter is used to tell the interactor whether or
 * not to respond to events. The default mouse filter used here is
 * button 1 with no modifiers.
 *
 * <p> Each interactor can also have a set of <i>constraints</i> added to
 * it. In this example, we have created an instance of
 * BoundedDragInteractor, which adds a constraint to itself in its
 * constructor.  BoundedDragInteractor always keeps figures that it is
 * dragging within a rectangular region -- in our example, the region is
 * shown by the grey line.
 *
 * <p> One point to note about interactors. In general, many figures will
 * share a single interactor. For example, all nodes in a graph editor
 * might reference the "node interactor" object. This strategy allows the
 * behaviour of a whole set of figures to be changed together (by
 * changing the behaviour of the interactor).
 *
 * @author John Reekie
 * @version $Revision$
 */
public class DragTutorial {

    // The JCanvas
    private JCanvas canvas;

    // The GraphicsPane
    private GraphicsPane graphicsPane;

    /** Create a JCanvas and put it into a window.
     */
    public DragTutorial () {
        canvas = new JCanvas();
        graphicsPane = (GraphicsPane)canvas.getCanvasPane();

        BasicFrame frame = new BasicFrame("Drag tutorial", canvas);
        frame.setSize(600,400);
        frame.setVisible(true);
    }

    /** Create a couple of simple figures and make them draggable.
     * Both figures are given the same interactor, which
     * means that they behave the same when you mouse on them.
     * The interactor used to move them is an instance of
     * DragInteractor.
     */
    public void createDraggableFigures () {
        FigureLayer layer = graphicsPane.getForegroundLayer();

        // Create the interactor to do the work.
        Interactor dragger = new DragInteractor();
        dragger.setMouseFilter(MouseFilter.defaultFilter);

        // Create a rectangle and make it draggable
        BasicFigure blue = new BasicRectangle(10.0,10.0,50.0,50.0,Color.blue);
        layer.add(blue);
        blue.setInteractor(dragger);

        // Create a circle and make it draggable
        BasicFigure red = new BasicEllipse(200.0,200.0,50.0,50.0,Color.red);
        layer.add(red);
        red.setInteractor(dragger);
    }

    /** Create another simple figures and make it draggable within
     * a region of the canvas. This example uses an instance of
     * BoundedDragInteractor to move the object.
     */
    public void createBoundedDraggableFigure () {
        FigureLayer layer = graphicsPane.getForegroundLayer();

        // Create the interactor and set it up for a 200x200 rectangle
        Rectangle2D bounds = new Rectangle2D.Double(100.0,100.0,200.0,200.0);
        Interactor boundedDragger = new BoundedDragInteractor(bounds);
        boundedDragger.setMouseFilter(MouseFilter.defaultFilter);

        // Create an outline rectangle that shows the boundaries
        graphicsPane.getOverlayLayer().add(bounds);

        // Create a green rectangle that stays inside the boundary
        BasicFigure green = new BasicFigure(
                new Rectangle2D.Double(110.0,110.0,50.0,50.0),
                Color.green);
        layer.add(green);
        green.setInteractor(boundedDragger);
    }

    /** Main function
     */
    public static void main (String argv[]) {
        // Always invoke graphics code in the event thread
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    DragTutorial ex = new DragTutorial();
                    ex.createDraggableFigures();
                    ex.createBoundedDraggableFigure();
                    ex.canvas.repaint();
                }
            });
    }
}



