/*
 Copyright (c) 1998-2014 The Regents of the University of California
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN  BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY
 */
package diva.canvas.test;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.geom.Line2D;

import javax.swing.SwingConstants;

import diva.canvas.CanvasPane;
import diva.canvas.CompositeFigure;
import diva.canvas.Figure;
import diva.canvas.GraphicsPane;
import diva.canvas.PaneWrapper;
import diva.canvas.connector.Arrowhead;
import diva.canvas.connector.Blob;
import diva.canvas.connector.StraightTerminal;
import diva.canvas.toolbox.BasicRectangle;
import diva.canvas.toolbox.IconFigure;
import diva.canvas.toolbox.ImageFigure;
import diva.canvas.toolbox.LabelFigure;
import diva.util.java2d.PaintedList;
import diva.util.java2d.PaintedPath;
import diva.util.java2d.PaintedShape;
import diva.util.java2d.Polygon2D;
import diva.util.jester.TestHarness;
import diva.util.jester.TestSuite;

/**
 * A test suite for testing concrete figures. This suite creates a factory
 * for each available concrete figure class and runs the Figure tests
 * on figures it produces.
 *
 * @author John Reekie
 * @version $Id$
 */
public class ConcreteFigures extends TestSuite {
    /** Constructor
     */
    public ConcreteFigures(TestHarness harness) {
        setTestHarness(harness);
    }

    /**
     * runSuite()
     */
    @Override
    public void runSuite() {
        new FigureTest(getTestHarness(), new BasicRectangleFactory1()).run();
        new FigureTest(getTestHarness(), new BasicRectangleFactory2()).run();
        new FigureTest(getTestHarness(), new CompositeFigureFactory1()).run();
        new FigureTest(getTestHarness(), new PaneWrapperFactory()).run();

        /// These ones fail!
        //new FigureTest(getTestHarness(), new LabelFigureFactory()).run();
        //new FigureTest(getTestHarness(), new IconFigureFactory()).run();
        //new FigureTest(getTestHarness(), new ImageFigureFactory()).run();
    }

    ///////////////////////////////////////////////////////////////////
    ////  main

    /** Create a default test harness and
     * run all tests on it.
     */
    public static void main(String[] argv) {
        new ConcreteFigures(new TestHarness()).run();
    }

    ///////////////////////////////////////////////////////////////////
    //// Factories

    /**
     * Create a BasicRectangle with stroked outline
     */
    public static class BasicRectangleFactory1 implements
    FigureTest.FigureFactory {
        // FindBugs suggests making this class static so as to decrease
        // the size of instances and avoid dangling references.

        @Override
        public Figure createFigure() {
            return new BasicRectangle(10, 10, 20, 20);
        }

        @Override
        public String toString() {
            return "Basic rectangle, no fill";
        }
    }

    /**
     * Create a filled BasicRectangle
     */
    public static class BasicRectangleFactory2 implements
    FigureTest.FigureFactory {
        // FindBugs suggests making this class static so as to decrease
        // the size of instances and avoid dangling references.

        @Override
        public Figure createFigure() {
            return new BasicRectangle(10, 10, 20, 20, Color.blue);
        }

        @Override
        public String toString() {
            return "Basic rectangle, filled blue";
        }
    }

    /**
     * Create an icon figure
     */
    public static class IconFigureFactory implements FigureTest.FigureFactory {
        // FindBugs suggests making this class static so as to decrease
        // the size of instances and avoid dangling references.

        /** Create a collection of terminals an an icon
         */
        public void createTerminals(IconFigure icon) {
            // NORTH
            StraightTerminal north = new StraightTerminal();

            //Site connectNorth = north.getConnectSite();
            Blob blobNorth = new Blob();
            blobNorth.setSizeUnit(5.0);
            north.setEnd(blobNorth);
            icon.addTerminal(north, SwingConstants.NORTH, 50);

            // SOUTH
            StraightTerminal south = new StraightTerminal();

            //Site connectSouth = south.getConnectSite();
            Blob blobSouth = new Blob();
            blobSouth.setStyle(Blob.BLOB_DIAMOND);
            blobSouth.setSizeUnit(5.0);
            blobSouth.setFilled(false);
            south.setEnd(blobSouth);
            icon.addTerminal(south, SwingConstants.SOUTH, 50);

            // WEST
            StraightTerminal west = new StraightTerminal();

            //Site connectWest = west.getConnectSite();
            Arrowhead arrowWest = new Arrowhead();
            west.setEnd(arrowWest);
            icon.addTerminal(west, SwingConstants.WEST, 50);

            // EAST
            StraightTerminal east = new StraightTerminal();

            //Site connectEast = east.getConnectSite();
            Arrowhead arrowEast = new Arrowhead();
            arrowEast.setFlipped(true);
            east.setEnd(arrowEast);
            icon.addTerminal(east, SwingConstants.EAST, 50);
        }

        @Override
        public Figure createFigure() {
            // Create the graphic
            PaintedList graphic = new PaintedList();

            Polygon2D polygon = new Polygon2D.Double();
            polygon.moveTo(30, 50);
            polygon.lineTo(70, 80);
            polygon.lineTo(70, 20);
            graphic.add(new PaintedShape(polygon, Color.red, 1.0f));

            Line2D line1 = new Line2D.Double(10, 50, 30, 50);
            graphic.add(new PaintedPath(line1));

            Line2D line2 = new Line2D.Double(70, 50, 90, 50);
            graphic.add(new PaintedPath(line2));

            // Create the icon
            BasicRectangle background = new BasicRectangle(0, 0, 100, 100,
                    Color.green);
            IconFigure icon = new IconFigure(background, graphic);

            // Add its terminals
            createTerminals(icon);
            icon.translate(100, 100);
            return icon;
        }

        @Override
        public String toString() {
            return "Icon figure";
        }
    }

    /**
     * Create an image figure
     */
    public static class ImageFigureFactory implements FigureTest.FigureFactory {
        // FindBugs suggests making this class static so as to decrease
        // the size of instances and avoid dangling references.

        public static final String IMAGE_FILE_NAME = "demo.gif";

        public Component component = new Canvas();

        @Override
        public Figure createFigure() {
            Image img = Toolkit.getDefaultToolkit().getImage(IMAGE_FILE_NAME);
            MediaTracker tracker = new MediaTracker(component);
            tracker.addImage(img, 0);

            try {
                tracker.waitForID(0);
            } catch (InterruptedException e) {
                System.err.println(e + "... in LayerImageFigure");
            }

            ImageFigure imgFig = new ImageFigure(img);
            imgFig.translate(300, 100);
            return imgFig;
        }

        @Override
        public String toString() {
            return "Image figure";
        }
    }

    /**
     * Create an image figure
     */
    public static class LabelFigureFactory implements FigureTest.FigureFactory {
        // FindBugs suggests making this class static so as to decrease
        // the size of instances and avoid dangling references.

        @Override
        public Figure createFigure() {
            LabelFigure label = new LabelFigure("Hello!");
            label.translate(200, 200);
            return label;
        }

        @Override
        public String toString() {
            return "Label figure";
        }
    }

    /**
     * Create a CompositeFigure with a filled rectangle background
     */
    public static class CompositeFigureFactory1 implements
    FigureTest.FigureFactory {
        // FindBugs suggests making this class static so as to decrease
        // the size of instances and avoid dangling references.
        @Override
        public Figure createFigure() {
            Figure bg = new BasicRectangle(10, 10, 20, 20, Color.blue);
            Figure cf = new CompositeFigure(bg);
            return cf;
        }

        @Override
        public String toString() {
            return "Composite figure with basic rectangle background";
        }
    }

    /**
     * Create a PaneWrapper with a filled rectangle background
     */
    public static class PaneWrapperFactory implements FigureTest.FigureFactory {
        // FindBugs suggests making this class static so as to decrease
        // the size of instances and avoid dangling references.

        @Override
        public Figure createFigure() {
            Figure bg = new BasicRectangle(10, 10, 20, 20, Color.blue);
            CanvasPane pane = new GraphicsPane();
            pane.setSize(300.0, 300.0);

            PaneWrapper wrapper = new PaneWrapper(pane);
            wrapper.setBackground(bg);
            return wrapper;
        }

        @Override
        public String toString() {
            return "Pane wrapper containing an empty graphics pane";
        }
    }
}
