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
import java.awt.Font;
import java.awt.geom.Point2D;

import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import diva.canvas.CanvasUtilities;
import diva.canvas.Figure;
import diva.canvas.FigureLayer;
import diva.canvas.GraphicsPane;
import diva.canvas.JCanvas;
import diva.canvas.interactor.Interactor;
import diva.canvas.toolbox.BasicController;
import diva.canvas.toolbox.BasicEllipse;
import diva.canvas.toolbox.BasicRectangle;
import diva.canvas.toolbox.LabelFigure;
import diva.canvas.toolbox.LabelWrapper;
import diva.gui.BasicFrame;

/**
 * This tutorial illustrates how to use LabelFigure and related classes.
 *
 * @author John Reekie
 * @version $Revision$
 */
public class LabelTutorial {

    // The JCanvas
    private JCanvas canvas;

    // The GraphicsPane
    private GraphicsPane graphicsPane;

    // The controller
    BasicController controller;

    // The default interactor
    Interactor defaultInteractor;

    /** Create a JCanvas and put it into a window
     */
    public LabelTutorial () {
        canvas = new JCanvas();
        graphicsPane = (GraphicsPane)canvas.getCanvasPane();

        // Create a controller to do the work.
        controller = new BasicController(graphicsPane);
        defaultInteractor = controller.getSelectionInteractor();

        // Create the window
        BasicFrame frame = new BasicFrame("Simple canvas tutorial", canvas);
        frame.setSize(600,400);
        frame.setVisible(true);
    }

    /** Create a collection of labels. One label is created with
     * each possible anchor point. Note that the labels appear to
     * be in the wrong locations: but they are the anchor of the
     * label itself, not the displayed square, so they are correct.
     */
    public void createLabels () {
        FigureLayer layer = graphicsPane.getForegroundLayer();

        // The data to display
        int anchors[] = {
            SwingConstants.CENTER,
            SwingConstants.NORTH,
            SwingConstants.NORTH_EAST,
            SwingConstants.EAST,
            SwingConstants.SOUTH_EAST,
            SwingConstants.SOUTH,
            SwingConstants.SOUTH_WEST,
            SwingConstants.WEST,
            SwingConstants.NORTH_WEST};

        String labels[] = {
            "center",
            "north",
            "north-east",
            "east",
            "south-east",
            "south",
            "south-west",
            "west",
            "north-west"};

        String fonts[] = {
            "Dialog",
            "DialogInput",
            "Monospaced",
            "Serif",
            "SansSerif",
            "Symbol",
            "Times",
            "Courier",
            "Helvetica"};

        int styles[] = {
            Font.PLAIN,
            Font.BOLD,
            Font.ITALIC,
            Font.BOLD | Font.ITALIC,
            Font.PLAIN,
            Font.BOLD,
            Font.ITALIC,
            Font.BOLD | Font.ITALIC,
            Font.PLAIN};


        // Draw a rectangle to position them
        BasicRectangle square = new BasicRectangle(160,80,120,120);
        square.setStrokePaint(Color.gray);
        layer.add(square);

        // Create the labels
        for (int i = 0; i < anchors.length; i++) {
            LabelFigure labelFigure = new LabelFigure(
                    labels[i], fonts[i], styles[i], 20);

            // Set the anchor
            labelFigure.setAnchor(anchors[i]);

            // Move the anchor to the right location
            Point2D pt = CanvasUtilities.getLocation(square.getBounds(),
                    CanvasUtilities.reverseDirection(anchors[i]));
            labelFigure.translateTo(pt);

            // Add to the layer
            layer.add(labelFigure);
            labelFigure.setInteractor(defaultInteractor);

            // Draw a small circle there so we can see it
            Figure mark = new BasicEllipse(
                    pt.getX()-2, pt.getY()-2, 4, 4,
                    Color.red);
            layer.add(mark);
        }
    }

    /** Create a couple of figures with labels attached to them.
     * This simple illustrates the fact that labels can be easily
     * attached to any arbitrary figure.
     */
    public void createLabeledWrappers () {
        FigureLayer layer = graphicsPane.getForegroundLayer();

        Figure a = new BasicEllipse(420,100,100,50);
        LabelWrapper wrapperA = new LabelWrapper(a, "Foo!\nBar!\nBaz!");
        layer.add(wrapperA);
        wrapperA.setInteractor(defaultInteractor);

        Figure b = new BasicRectangle(460,200,50,40, Color.green);
        LabelWrapper wrapperB = new LabelWrapper(b, "Bar!");
        wrapperB.setAnchor(SwingConstants.SOUTH_WEST);
        wrapperB.getLabel().setAnchor(SwingConstants.NORTH_WEST);
        layer.add(wrapperB);
        wrapperB.setInteractor(defaultInteractor);
    }

    /** Main function
     */
    public static void main (String argv[]) {
        // Always invoke graphics code in the event thread
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    LabelTutorial ex = new LabelTutorial();
                    ex.createLabels();
                    ex.createLabeledWrappers();
                }
            });
    }
}


