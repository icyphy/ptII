/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 *
 */

package diva.canvas.tutorial;

import java.awt.Color;
import java.awt.geom.GeneralPath;

import javax.swing.SwingUtilities;

import diva.canvas.Figure;
import diva.canvas.FigureLayer;
import diva.canvas.GraphicsPane;
import diva.canvas.JCanvas;
import diva.canvas.toolbox.BasicFigure;
import diva.canvas.toolbox.BasicRectangle;
import diva.gui.BasicFrame;
import diva.util.java2d.Polyline2D;

/**
 * <p> This tutorial shows how to construct a JCanvas and place
 * figures on it.
 * <p>
 * <img src="../../../../packages/canvas/tutorial/images/SimpleTutorial.gif" align="right">
 *
 * <P> In this example, we create figures with a few
 * different shapes, using the BasicFigure class in
 * <b>diva.canvas.toolbox</b>, which takes an instance of Shape (an
 * interface defined in the Java AWT) and draws it on the screen using
 * given colors and strokes.
 *
 * <p> Each JCanvas contains by default an instance of GraphicsPane.
 * To get the graphics pane from a JCanvas:
 *
 * <pre>
 *   GraphicsPane graphicsPane = (GraphicsPane)canvas.getCanvasPane();
 * </pre>
 *
 * The pane contains several layers, one of which is a foreground
 * FigureLayer upon which figures are drawn and interacted with.  To
 * get the figure layer:
 *
 * <pre>
 *   FigureLayer layer = graphicsPane.getForegroundLayer();
 * </pre>
 *
 * There are three figures on the screen, each created using a
 * different kind of shape. The code to create a rectangle and add it
 * to the figure layer is:
 *
 * <pre>
 *   Figure rectangle = new BasicRectangle(10,10,100,100,Color.blue);
 *   layer.add(rectangle);
 * </pre>
 *
 * The code to create the curved shape is more complex, and uses an
 * instance of <b>java.awt.geom.GeneralPath</b>.
 *
 * <pre>
 *    GeneralPath path = new GeneralPath();
 *    path.moveTo(120,240);
 *    path.lineTo(240,240);
 *    path.quadTo(180,120,120,240);
 *    path.closePath();
 *    Figure semi = new BasicFigure(path, Color.green);
 *    layer.add(semi);
 * </pre>
 *
 * The third figure is much the same, but uses an instance of
 * <b>diva.util.java2d.Polyline2D</b>. Polyline2D is more efficient
 * than GeneralPath, and should be used anytime only straight-line
 * segments are needed.
 *
 * @author John Reekie
 * @version $Revision$ */
public class SimpleTutorial {

    // The JCanvas
    private JCanvas canvas;

    // The GraphicsPane
    private GraphicsPane graphicsPane;

    /** Create a JCanvas and put it into a window
     */
    public SimpleTutorial () {
        canvas = new JCanvas();
        graphicsPane = (GraphicsPane)canvas.getCanvasPane();

        BasicFrame frame = new BasicFrame("Simple canvas tutorial", canvas);
        frame.setSize(600,400);
        frame.setVisible(true);
    }

    /** Create a rectangle figure. The rectangle is an instance of
     * the BasicRectangle class. This class, together with a number
     * other useful predefined figure classes, is contained in the
     * package <b>diva.canvas.toolbox</b>.
     */
    public void createBasicRectangle () {
        FigureLayer layer = graphicsPane.getForegroundLayer();
        Figure rectangle = new BasicRectangle(50,50,80,80,Color.blue);
        layer.add(rectangle);
    }

    /** Create an odd-shaped figure. The rectangle is an instance of
     * the BasicShape class, which draws itself using any instance
     * of the Java2D interface, <b>java.awt.Shape</b>. In this example,
     * we use an instance of GeneralPath.
     */
    public void createBasicFigure () {
        FigureLayer layer = graphicsPane.getForegroundLayer();
        GeneralPath path = new GeneralPath();
        path.moveTo(120,240);
        path.lineTo(240,240);
        path.quadTo(180,120,120,240);
        path.closePath();
        Figure semi = new BasicFigure(path, Color.green);
        layer.add(semi);
    }

    /** Create a polyline. Again, this uses the BasicFigure class,
     * but this time the shape is an instance of <b>diva.util.Polyline2D</b>.
     */
    public void createPolyline () {
        FigureLayer layer = graphicsPane.getForegroundLayer();
        Polyline2D path = new Polyline2D.Double();
        path.moveTo(240,120);
        path.lineTo(280,140);
        path.lineTo(240,160);
        path.lineTo(280,180);
        path.lineTo(240,200);
        path.lineTo(280,220);
        path.lineTo(240,240);
        Figure line = new BasicFigure(path);
        layer.add(line);
    }

    /** Main function
     */
    public static void main (String argv[]) {
        // Always invoke graphics code in the event thread
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    SimpleTutorial ex = new SimpleTutorial();
                    ex.createBasicRectangle();
                    ex.createBasicFigure();
                    ex.createPolyline();
                }
            });
    }
}


