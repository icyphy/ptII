/*
 * $Id$
 *
 * Copyright (c) 1998 The Regents of the University of California.
 * All rights reserved.  See the file COPYRIGHT for details.
 *
 */

package ptolemy.schematic.demo;

import ptolemy.schematic.util.*;
import ptolemy.schematic.xml.*;

import diva.canvas.*;
import diva.canvas.connector.*;
import diva.canvas.interactor.*;
import diva.canvas.manipulator.*;

////import diva.canvas.toolbox.*;

import diva.canvas.toolbox.BasicRectangle;
import diva.canvas.toolbox.BasicEllipse;
import diva.canvas.toolbox.BasicController;
import diva.canvas.toolbox.GraphicsParser;
import diva.canvas.toolbox.PaintedFigure;
import diva.canvas.toolbox.BasicRectangle;
import diva.canvas.toolbox.IconFigure;

import diva.util.gui.*;
import diva.util.java2d.*;

import java.awt.*;
import java.awt.geom.*;

import java.util.HashMap;
import java.util.Enumeration;
import javax.swing.SwingConstants;

/**
 * This tutorial illustrates how to use Icons.
 *
 * @author John Reekie
 * @version $Revision$
 */
public class IconDemo {

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
    public IconDemo () {
        canvas = new JCanvas();
        graphicsPane = (GraphicsPane)canvas.getCanvasPane();

        // Create the interaction role and an interactor to do the work.
        controller = new BasicController(graphicsPane);
        defaultInteractor = controller.getSelectionInteractor();

        // Create the window
        TutorialWindow window = new TutorialWindow("Simple canvas tutorial");
        window.getContentPane().add("Center",canvas);
        window.setSize(600,400);
        window.setVisible(true);
    }

    /** Create an icon. The icon's graphic is created
     * by directly calling the PaintedShape API.
     */
    public void createIcon () {
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
                Color.green.brighter().brighter().brighter().brighter());
        IconFigure icon = new IconFigure(background, graphic);
        layer.add(icon);
        icon.setInteractor(defaultInteractor);
    }

    /** Create an icon from an XML library
     */
    public void createIconFromLibrary () {
        FigureLayer layer = graphicsPane.getForegroundLayer();
        PTMLParser parser = new PTMLParser();
        
        // FIXME Un-hardwire this
        // String url = "file:c:/java/ptII/ptolemy/schematic/lib/pticons.ptml";
        String url = "file:/users/johnr/java/ptII/ptolemy/schematic/lib/pticons.ptml";
            
        // Parse the icon libraries
        XMLElement root = null;
        IconLibrary iconLibrary = null;
        try {
            root = parser.parse(url);
            System.out.println("Parsed:\n" + root);

            iconLibrary = PTMLObjectFactory.createIconLibrary(root);
            System.out.println("Icon library: " + iconLibrary);
        }
        catch (Exception e) {
            System.out.println(e);
        }
 
        // Get the "sources" library and the "Const" icon
        IconLibrary sources = iconLibrary.getSubLibrary("Sources");
        Icon constIcon = sources.getIcon("Clock");

        // Create a new painted object for the graphic
        PaintedList graphic = new PaintedList();
        for (Enumeration i = constIcon.graphicElements(); i.hasMoreElements(); ) {
            GraphicElement ge = (GraphicElement) i.nextElement();
            String type = ge.getType();
            HashMap map = new HashMap();
            for (Enumeration j = ge.attributeNames(); j.hasMoreElements(); ) {
                String key = (String) j.nextElement();
                String val = (String) ge.getAttribute(key);
                map.put(key,val);
            }
            PaintedObject po = GraphicsParser.createPaintedObject(type, map);
            graphic.add(po);
        }

        // Create an icon for it
        BasicRectangle background = new BasicRectangle(0,0,100,100,
                Color.yellow.brighter().brighter().brighter().brighter().brighter());
        IconFigure icon = new IconFigure(background, graphic);
        layer.add(icon);
        icon.setInteractor(defaultInteractor);
        icon.translate(200,100);
     }
    
    /** Main function
     */
    public static void main (String argv[]) {
        IconDemo ex = new IconDemo();
        ex.createIcon();
        ex.createIconFromLibrary();
    }
}
