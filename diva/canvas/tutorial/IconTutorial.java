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
import java.awt.geom.Line2D;
import java.util.HashMap;

import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import diva.canvas.FigureLayer;
import diva.canvas.GraphicsPane;
import diva.canvas.JCanvas;
import diva.canvas.Site;
import diva.canvas.connector.Arrowhead;
import diva.canvas.connector.Blob;
import diva.canvas.connector.StraightTerminal;
import diva.canvas.interactor.BoundsManipulator;
import diva.canvas.interactor.Interactor;
import diva.canvas.toolbox.BasicController;
import diva.canvas.toolbox.BasicRectangle;
import diva.canvas.toolbox.GraphicsParser;
import diva.canvas.toolbox.IconFigure;
import diva.canvas.toolbox.PaintedFigure;
import diva.gui.BasicFrame;
import diva.util.java2d.PaintedList;
import diva.util.java2d.PaintedPath;
import diva.util.java2d.PaintedShape;
import diva.util.java2d.Polygon2D;


/**
 * This tutorial illustrates how to use Icons.
 *
 * @author John Reekie
 * @version $Id$
 */
public class IconTutorial {

    // The JCanvas
    private JCanvas canvas;

    // The GraphicsPane
    private GraphicsPane graphicsPane;

    // The controller
    BasicController controller;

    // The default interactor
    Interactor defaultInteractor;

    // The two icons
    private IconFigure _icon1;
    private IconFigure _icon2;

    /** Create a JCanvas and put it into a window
     */
    public IconTutorial () {
        canvas = new JCanvas();
        graphicsPane = (GraphicsPane)canvas.getCanvasPane();

        // Create a controller to do the work.
        controller = new BasicController(graphicsPane);
        defaultInteractor = controller.getSelectionInteractor();

        // Create a manipulator to give resize handles on figures
        BoundsManipulator figureManipulator = new BoundsManipulator();
        controller.setSelectionManipulator(figureManipulator);

        // Create the window
        BasicFrame frame = new BasicFrame("Icons 'R' us", canvas);
        frame.setSize(600,400);
        frame.setVisible(true);
    }

    /** Create a collection of terminals an an icon
     */
    public void createTerminals (IconFigure iconFigure) {
        FigureLayer layer = graphicsPane.getForegroundLayer();

        // NORTH
        StraightTerminal north = new StraightTerminal();
        Site connectNorth = north.getConnectSite();
        Blob blobNorth = new Blob();
        blobNorth.setSizeUnit(5.0);
        north.setEnd(blobNorth);
        iconFigure.addTerminal(north, SwingConstants.NORTH, 50);

        // SOUTH
        StraightTerminal south = new StraightTerminal();
        Site connectSouth = south.getConnectSite();
        Blob blobSouth = new Blob();
        blobSouth.setStyle(Blob.BLOB_DIAMOND);
        blobSouth.setSizeUnit(5.0);
        blobSouth.setFilled(false);
        south.setEnd(blobSouth);
        iconFigure.addTerminal(south, SwingConstants.SOUTH, 50);

        // WEST
        StraightTerminal west = new StraightTerminal();
        Site connectWest = west.getConnectSite();
        Arrowhead arrowWest = new Arrowhead();
        west.setEnd(arrowWest);
        iconFigure.addTerminal(west, SwingConstants.WEST, 50);

        // EAST
        StraightTerminal east = new StraightTerminal();
        Site connectEast = east.getConnectSite();
        Arrowhead arrowEast = new Arrowhead();
        arrowEast.setFlipped(true);
        east.setEnd(arrowEast);
        iconFigure.addTerminal(east, SwingConstants.EAST, 50);

        // Make sure it's clean now
        layer.repaint();
    }

    /** Create an icon. The icon's graphic is created
     * by directly calling the PaintedShape API.
     */
    public void createIcon1 () {
        FigureLayer layer = graphicsPane.getForegroundLayer();

        // Create the graphic
        PaintedList graphic = new PaintedList();

        Polygon2D polygon = new Polygon2D.Double();
        polygon.moveTo(30,50);
        polygon.lineTo(70,80);
        polygon.lineTo(70,20);
        graphic.add(new PaintedShape(polygon, Color.red, 1.0f));

        Line2D line1 = new Line2D.Double(10,50,30,50);
        graphic.add(new PaintedPath(line1));

        Line2D line2 = new Line2D.Double(70,50,90,50);
        graphic.add(new PaintedPath(line2));

        // Create the icon
        BasicRectangle background = new BasicRectangle(0,0,100,100,
                Color.green.brighter().brighter());
        IconFigure _icon1 = new IconFigure(background, graphic);
        layer.add(_icon1);
        _icon1.setInteractor(defaultInteractor);

        // Add its terminals
        createTerminals(_icon1);
        _icon1.translate(100, 100);
    }

    /** Create an icon. The icon's graphic is created
     * by using GraphicsParse.
     */
    public void createIcon2 () {
        FigureLayer layer = graphicsPane.getForegroundLayer();

        // Use a hash-table
        PaintedFigure g = new PaintedFigure();

        HashMap map = new HashMap();
        map.put("coords", "30 50 70 80 70 20");
        map.put("fill", "red");
        map.put("width", "1");
        g.add(GraphicsParser.createPaintedObject(
                "polygon", map));

        //Line2D line1 = new Line2D.Double(10,50,30,50);
        //f.add(new PaintedPath(line1));

        //Line2D line2 = new Line2D.Double(70,50,90,50);
        //f.add(new PaintedPath(line2));

        layer.add(g);
        g.setInteractor(defaultInteractor);
        g.translate(100,0);
    }

    /** Main function
     */
    public static void main (String argv[]) {
        // Always invoke graphics code in the event thread
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    IconTutorial ex = new IconTutorial();
                    ex.createIcon1();
                    ex.createIcon2();
                }
            });
    }
}


