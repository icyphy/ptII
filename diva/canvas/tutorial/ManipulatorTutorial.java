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
import java.awt.geom.GeneralPath;

import javax.swing.SwingUtilities;

import diva.canvas.FigureLayer;
import diva.canvas.GraphicsPane;
import diva.canvas.JCanvas;
import diva.canvas.event.MouseFilter;
import diva.canvas.interactor.BasicSelectionModel;
import diva.canvas.interactor.BoundsManipulator;
import diva.canvas.interactor.CircleManipulator;
import diva.canvas.interactor.DragInteractor;
import diva.canvas.interactor.PathManipulator;
import diva.canvas.interactor.SelectionDragger;
import diva.canvas.interactor.SelectionInteractor;
import diva.canvas.interactor.SelectionModel;
import diva.canvas.toolbox.BasicEllipse;
import diva.canvas.toolbox.BasicFigure;
import diva.canvas.toolbox.BasicRectangle;
import diva.canvas.toolbox.PathFigure;
import diva.gui.BasicFrame;
import diva.util.java2d.Polyline2D;

/**
 * <p> This tutorial shows how to add different types of manipulators
 * to figures.
 *
 * @author John Reekie
 * @version $Id$
 */
public class ManipulatorTutorial {

    // The figures
    private BasicRectangle _rectangle;
    private BasicEllipse _circle;
    private PathFigure _line;
    private BasicFigure _shape;

    // The JCanvas
    private JCanvas canvas;

    // The GraphicsPane
    private GraphicsPane graphicsPane;

    /** Create a JCanvas and put it into a window
     */
    public ManipulatorTutorial () {
        canvas = new JCanvas();
        graphicsPane = (GraphicsPane)canvas.getCanvasPane();

        BasicFrame frame = new BasicFrame("Manipulator canvas tutorial", canvas);
        frame.setSize(600,400);
        frame.setVisible(true);
    }

    /** Create the figures.
     */
    public void createFigures () {
        FigureLayer layer = graphicsPane.getForegroundLayer();

        _rectangle = new BasicRectangle(50,50,80,80,Color.black);
        layer.add(_rectangle);

        _circle = new BasicEllipse(150,50,80,80,Color.green);
        layer.add(_circle);

        GeneralPath path = new GeneralPath();
        path.moveTo(120,240);
        path.lineTo(240,240);
        path.quadTo(180,120,120,240);
        path.closePath();
        _shape = new BasicFigure(path, Color.red);
        layer.add(_shape);

        Polyline2D poly = new Polyline2D.Double();
        poly.moveTo(240,120);
        poly.lineTo(280,140);
        poly.lineTo(240,160);
        poly.lineTo(280,180);
        poly.lineTo(240,200);
        poly.lineTo(280,220);
        poly.lineTo(240,240);
        _line = new PathFigure(poly);
        layer.add(_line);
    }

    /** Create the interaction on the figures. We make a different
     * selection interactor for each figure, because each has a
     * different type of manipulator attached to it. Usually, each
     * selection interactor will be attached to many figures.
     */
    public void createInteraction () {
        // Create a selection drag-selector on the pane
        SelectionDragger selectionDragger = new SelectionDragger(graphicsPane);
        SelectionModel model = new BasicSelectionModel();
        SelectionInteractor s;
        DragInteractor d;

        // For each figure, create a selection interactor with
        // a drag interactor attached to it. Set a different
        // manipulator for each.
        s = new SelectionInteractor(model);
        s.setPrototypeDecorator(new BoundsManipulator());
        d = new DragInteractor();
        s.addInteractor(d);
        d.setSelectiveEnabled(true);
        d.setMouseFilter(new MouseFilter(1, 0, 0));
        selectionDragger.addSelectionInteractor(s);
        _rectangle.setInteractor(s);

        s = new SelectionInteractor(model);
        s.setPrototypeDecorator(new CircleManipulator());
        d = new DragInteractor();
        s.addInteractor(d);
        d.setSelectiveEnabled(true);
        d.setMouseFilter(new MouseFilter(1, 0, 0));
        selectionDragger.addSelectionInteractor(s);
        _circle.setInteractor(s);

        s = new SelectionInteractor(model);
        s.setPrototypeDecorator(new PathManipulator());
        d = new DragInteractor();
        s.addInteractor(d);
        d.setSelectiveEnabled(true);
        d.setMouseFilter(new MouseFilter(1, 0, 0));
        selectionDragger.addSelectionInteractor(s);
        _line.setInteractor(s);

        s = new SelectionInteractor(model);
        s.setPrototypeDecorator(new PathManipulator());
        d = new DragInteractor();
        s.addInteractor(d);
        d.setSelectiveEnabled(true);
        d.setMouseFilter(new MouseFilter(1, 0, 0));
        selectionDragger.addSelectionInteractor(s);
        _shape.setInteractor(s);
    }

    /** Main function
     */
    public static void main (String argv[]) {
        // Always invoke graphics code in the event thread
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    ManipulatorTutorial ex = new ManipulatorTutorial();
                    ex.createFigures();
                    ex.createInteraction();
                }
            });
    }
}


