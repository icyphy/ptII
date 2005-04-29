/*
  Copyright (c) 1998-2005 The Regents of the University of California
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
  *
  */
package diva.canvas.demo;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.AffineTransform;

import diva.canvas.FigureLayer;
import diva.canvas.GraphicsPane;
import diva.canvas.interactor.SelectionInteractor;
import diva.canvas.toolbox.BasicController;
import diva.canvas.toolbox.BasicEllipse;
import diva.canvas.toolbox.BasicFigure;
import diva.canvas.toolbox.BasicRectangle;
import diva.util.java2d.Polygon2D;
import diva.util.java2d.ShapeUtilities;


/** A pane containing instances of some basic figures. The
 * figures can be dragged about with the mouse.
 *
 * @author John Reekie
 * @version $Id$
 */
public class SimplePane extends GraphicsPane {
    /** The controller
     */
    BasicController controller;

    /** The interactor to give to all figures
     */
    SelectionInteractor selectionInteractor;

    /** The layer to draw all figure in
     */
    FigureLayer figureLayer;

    /**
     * Constructor
     */
    public SimplePane() {
        super();

        // Get the figure layer
        figureLayer = getForegroundLayer();

        // Construct a simple controller and get the default interactor
        controller = new BasicController(this);
        selectionInteractor = controller.getSelectionInteractor();

        // Draw it
        drawFigures();
    }

    /** Draw some figures
     */
    public void drawFigures() {
        // Here's a square
        BasicRectangle rect = new BasicRectangle(40.0, 40.0, 80.0, 80.0);
        rect.setLineWidth(8);
        rect.setStrokePaint(Color.red);
        figureLayer.add(rect);
        rect.setInteractor(selectionInteractor);

        // Here's an ellipse
        BasicEllipse oval = new BasicEllipse(160.0, 10.0, 120.0, 80.0);
        oval.setLineWidth(2);
        oval.setFillPaint(Color.magenta);
        figureLayer.add(oval);
        oval.setInteractor(selectionInteractor);

        // Create a star using a general path object
        Polygon2D p = new Polygon2D.Double();
        p.moveTo(-100.0f, -25.0f);
        p.lineTo(+100.0f, -25.0f);
        p.lineTo(-50.0f, +100.0f);
        p.lineTo(+0.0f, -100.0f);
        p.lineTo(+50.0f, +100.0f);
        p.closePath();

        // translate origin towards center of canvas
        AffineTransform at = new AffineTransform();
        at.translate(200.0f, 200.0f);
        p.transform(at);

        BasicFigure star = new BasicFigure(p);
        star.setLineWidth(2);
        star.setStrokePaint(Color.blue);
        figureLayer.add(star);
        star.setInteractor(selectionInteractor);

        // Create a cloud, Claude
        Shape area = ShapeUtilities.createCloudShape();

        //         Area area = new Area();
        //         Ellipse2D c = new Ellipse2D.Double();
        //         c.setFrame(0,25,50,50);
        //         area.add(new Area(c));
        //         c.setFrame(25,0,40,40);
        //         area.add(new Area(c));
        //         c.setFrame(25,25,60,60);
        //         area.add(new Area(c));
        //         c.setFrame(60,30,40,40);
        //         area.add(new Area(c));
        //         c.setFrame(60,10,30,30);
        //         area.add(new Area(c));
        AffineTransform cat = new AffineTransform();
        cat.translate(200, 100);
        cat.scale(2.0, 2.0);

        Shape bigarea = cat.createTransformedShape(area);

        BasicFigure cloud = new BasicFigure(bigarea, Color.blue);
        cloud.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                                   0.5f));
        figureLayer.add(cloud);
        cloud.setInteractor(selectionInteractor);
    }

    /** Return the selection interactor
     */
    public SelectionInteractor getSelectionInteractor() {
        return selectionInteractor;
    }
}
