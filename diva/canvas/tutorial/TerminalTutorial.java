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


