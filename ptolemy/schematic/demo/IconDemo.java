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
import ptolemy.kernel.util.IllegalActionException;

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

import java.net.URL;

import java.util.HashMap;
import java.util.Enumeration;
import javax.swing.SwingConstants;

/**
 * This tutorial illustrates how to use Icons.
 *
 * @author John Reekie
 * @version $Id$
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

    // The root XML element
    XMLElement root;

    // The root icon library
    IconLibrary iconLibrary = null;

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

    /** Parse the icon libraries
     */
    public void parseIconLibraries () {
        PTMLParser parser = new PTMLParser();
        
        // Get the path to the icon library. Read the PTII root from
        // the system properties
        String url = "";
        try {
            URL urlbase = new URL("file:" + System.getProperty("PTII"));
            urlbase = new URL(urlbase, "ptII/ptolemy/schematic/lib/pticons.ptml");
            url = urlbase.toString();
            System.out.println("Icon library URL = " + url);

            //String url = "file:" + System.getProperty("PTII") +
            //  "/ptolemy/schematic/lib/pticons.ptml";
            //String url = "file:/users/johnr/java/ptII/ptolemy/schematic/lib/pticons.ptml";
        }
        catch (Exception ex) {
            System.out.println("Couldn't construct url");
            System.out.println(ex.getMessage());
        }

        // Parse the icon libraries
        root = null;
        iconLibrary = null;
        try {
            root = parser.parse(url);
            System.out.println("Parsed:\n" + root);

            iconLibrary = PTMLObjectFactory.createIconLibrary(root);
            System.out.println("Icon library: " + iconLibrary);
        }
        catch (Exception e) {
            System.out.println(e);
        }
    } 

    /** Create an icon from an XML library
     *  @throws IllegalActionException If the libname can't be found.
     */
    public IconFigure createIconFromLibrary (String libname, String iconname)
        throws IllegalActionException {
        FigureLayer layer = graphicsPane.getForegroundLayer();

        // Get the "sources" library and the "Const" icon
        IconLibrary sources = iconLibrary.getSubLibrary(libname);
        Icon constIcon = sources.getIcon(iconname);

        // Create a new painted object for the graphic
        PaintedList graphic = new PaintedList();
        for (Enumeration i = constIcon.graphicElements(); i.hasMoreElements(); ) {
            GraphicElement ge = (GraphicElement) i.nextElement();
            String type = ge.getType();
	    String content = ge.getContent();
            HashMap map = new HashMap();
            for (Enumeration j = ge.attributeNames(); j.hasMoreElements(); ) {
                String key = (String) j.nextElement();
                String val = (String) ge.getAttribute(key);
                map.put(key,val);
            }
            PaintedObject po = 
		GraphicsParser.createPaintedObject(type, map, content);
            graphic.add(po);
        }

        // Create an icon for it
        BasicRectangle background = new BasicRectangle(0,0,100,100,
                Color.green);
        IconFigure icon = new IconFigure(background, graphic);
        layer.add(icon);
        icon.setInteractor(defaultInteractor);
        /// icon.translate(200,100);
        return icon;
     }
    
    /** Create a display of icons
     *  @throws IllegalActionException if a library cannot be found.
     */
    public void createIconDisplay () throws IllegalActionException {
        IconFigure icon = createIconFromLibrary("Sources", "Clock");
        icon.translate(100,50);
        
        icon = createIconFromLibrary("Sources", "Const");
        icon.translate(250,50);
    }

    /** Main function
     */
    public static void main (String argv[]) {
        IconDemo ex = new IconDemo();
        ex.parseIconLibraries();
        try {
            ex.createIconDisplay();
        } catch (IllegalActionException e) {
            System.out.println("Failed to create Icon Display: " +
                    "Library not found?");
        }
    }
}
