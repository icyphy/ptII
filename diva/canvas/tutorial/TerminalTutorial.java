/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 *
 */

package diva.canvas.tutorial;

import java.awt.Color;

import javax.swing.SwingUtilities;

import diva.canvas.FigureLayer;
import diva.canvas.GraphicsPane;
import diva.canvas.JCanvas;
import diva.canvas.Site;
import diva.canvas.connector.Arrowhead;
import diva.canvas.connector.Blob;
import diva.canvas.connector.StraightTerminal;
import diva.canvas.interactor.BoundsGeometry;
import diva.canvas.interactor.Interactor;
import diva.canvas.toolbox.BasicController;
import diva.canvas.toolbox.BasicRectangle;
import diva.gui.BasicFrame;

/**
 * This tutorial illustrates how to use Terminals.
 *
 * @author John Reekie
 * @version $Revision$
 */
public class TerminalTutorial {

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
    public TerminalTutorial () {
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

    /** Create a collection of terminals.
     */
    public void createTerminals () {
        FigureLayer layer = graphicsPane.getForegroundLayer();

        // Draw a rectangle to position them
        BasicRectangle square = new BasicRectangle(160,80,80,80);
        square.setStrokePaint(Color.gray);
        layer.add(square);

        // Create a BoundsGeometry to help get sites
        BoundsGeometry _geometry = new BoundsGeometry(square, square.getBounds());

        // Create a set of terminals, one by one

        // NORTH
        StraightTerminal north = new StraightTerminal(_geometry.getN());
        Site connectNorth = north.getConnectSite();
        Blob blobNorth = new Blob();
        blobNorth.setSizeUnit(5.0);
        north.setEnd(blobNorth);
        layer.add(north);

        // SOUTH
        StraightTerminal south = new StraightTerminal(_geometry.getS());
        Site connectSouth = south.getConnectSite();
        Blob blobSouth = new Blob();
        blobSouth.setStyle(Blob.BLOB_DIAMOND);
        blobSouth.setSizeUnit(5.0);
        blobSouth.setFilled(false);
        south.setEnd(blobSouth);
        layer.add(south);

        // WEST
        StraightTerminal west = new StraightTerminal(_geometry.getW());
        Site connectWest = west.getConnectSite();
        Arrowhead arrowWest = new Arrowhead();
        west.setEnd(arrowWest);
        layer.add(west);

        // EAST
        StraightTerminal east = new StraightTerminal(_geometry.getE());
        Site connectEast = east.getConnectSite();
        Arrowhead arrowEast = new Arrowhead();
        arrowEast.setFlipped(true);
        east.setEnd(arrowEast);
        layer.add(east);

        // Make sure it's clean now
        layer.repaint();
    }

    /** Main function
     */
    public static void main (String argv[]) {
        // Always invoke graphics code in the event thread
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    TerminalTutorial ex = new TerminalTutorial();
                    ex.createTerminals();
                }
            });
    }
}


