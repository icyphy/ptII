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

import diva.canvas.Figure;
import diva.canvas.FigureLayer;
import diva.canvas.GraphicsPane;
import diva.canvas.JCanvas;
import diva.canvas.Site;
import diva.canvas.connector.ArcConnector;
import diva.canvas.connector.ArcManipulator;
import diva.canvas.connector.Arrowhead;
import diva.canvas.connector.Connector;
import diva.canvas.connector.ConnectorManipulator;
import diva.canvas.connector.ConnectorTarget;
import diva.canvas.connector.PerimeterTarget;
import diva.canvas.connector.StraightConnector;
import diva.canvas.event.LayerAdapter;
import diva.canvas.event.LayerEvent;
import diva.canvas.interactor.BoundsManipulator;
import diva.canvas.interactor.DragInteractor;
import diva.canvas.interactor.SelectionInteractor;
import diva.canvas.toolbox.BasicController;
import diva.canvas.toolbox.BasicEllipse;
import diva.canvas.toolbox.BasicRectangle;
import diva.canvas.toolbox.TypedDecorator;
import diva.gui.BasicFrame;

/** This tutorial shows how to use "arc" connectors.
 *
 * <img src="../../../../packages/canvas/tutorial/images/ArcTutorial.gif" align="right">
 *
 * In this example,
 * the connectors are atached to "perimeter sites" -- that is,
 * sites that can relocated themselves to maintain themselves
 * on the perimeter of an object. Unlike the first connector
 * example, this one does not need to create a special kind of
 * figure, as perimeter sites can attach to any figure that
 * has a rectangle or circle shape.
 *
 * <P> The code to create the connectors and set up the interaction is
 * much the same as the previous tutorial, except that it uses
 * ArcConnectors instead of StraightConnectors. One noticable
 * difference is that the connector target uses a a sub-class of the
 * off-the-shelf PerimeterTarget class provided with the Diva canvas.
 * Although this would work:
 *
 * <pre>
 *    ConnectorTarget target = new PerimeterTarget();
 * </pre>
 *
 * we use a subclass that allows "self-loops." In other words, the default
 * behaviour of targets is not to allow a connection back to the same
 * object; the inner class in this example does allow this.
 * <P>
 * A second difference is that the initialization of the manipulators
 * is more complicated. Because there are two different kinds of connectors,
 * and we want different manipulators for each, we use an
 * instance of the TypedDecorator class to set this up:
 *
 * <pre>
 *     ConnectorManipulator cManipulator = new ConnectorManipulator();
 *     cManipulator.setSnapHalo(4.0);
 *     cManipulator.setConnectorTarget(target);
 *
 *     ArcManipulator aManipulator = new ArcManipulator();
 *     aManipulator.setSnapHalo(4.0);
 *     aManipulator.setConnectorTarget(target);
 *
 *     TypedDecorator typedDecorator = new TypedDecorator();
 *     typedDecorator.addDecorator(StraightConnector.class, cManipulator);
 *     typedDecorator.addDecorator(ArcConnector.class, aManipulator);
 * </pre>
 *
 * A different way to get the same effect would be to use two different
 * SelectionInteractors, one for the arcs with an ArcManipulator and one
 * for the StraightConnectors with a ConnectorManipulator.
 * (Currently, the ArcManipulator looks the same as the
 * ConnectorManipulator, but in the near future it will have additional
 * grab-handles for reshaping the arc.)
 *
 * <p> To make this example a little more interesting, selected
 * figures have resize handles attached to them. As the figure
 * is resived, attached connectors change accordingly. This
 * tutorial also illustrates the use of the TypedDecorator
 * class to attach different kinds of manipulators to different
 * kinds of figures (in this case, different kinds of connectors).
 *
 * @author John Reekie
 * @version $Revision$ */
public class ArcTutorial {

    // The JCanvas
    private JCanvas canvas;

    // The GraphicsPane
    private GraphicsPane graphicsPane;

    /** The controller
     */
    private BasicController controller;

    /** The two figures
     */
    private Figure figureA;
    private Figure figureB;
    private Figure figureC;

    /** The connectors
     */
    private StraightConnector connectorA;
    private ArcConnector connectorB;
    private ArcConnector connectorC;
    private ArcConnector connectorD;

    /** The target that finds sites on the figures
     */
    private ConnectorTarget target;

    /** Create a JCanvas and put it into a window.
     */
    public ArcTutorial () {
        canvas = new JCanvas();
        graphicsPane = (GraphicsPane)canvas.getCanvasPane();

        BasicFrame frame = new BasicFrame("Connector tutorial", canvas);
        frame.setSize(600,400);
        frame.setVisible(true);

        controller = new BasicController(graphicsPane);
   }

    /** Create the figures that we will draw connectors between.
     * This is fairly uninteresting.
     */
    public void createFigures () {
        FigureLayer layer = graphicsPane.getForegroundLayer();

        figureA = new BasicRectangle(10.0,10.0,100.0,50.0,Color.red);
        figureB = new BasicEllipse(100.0,100.0,100.0,100.0,Color.green);
        figureC = new BasicEllipse(300.0,100.0,100.0,100.0,Color.blue);

        layer.add(figureA);
        layer.add(figureB);
        layer.add(figureC);
    }

    /**
     * Create the connectors between the two figures. We will firstly
     * create one StraightConnector with a circle and an arrowhead
     * on it, and then an ArcConnector.
     */
    public void createConnectors () {
        FigureLayer layer = graphicsPane.getForegroundLayer();

        // Create the target that finds sites on the figures
        target = new SelfPTarget();

        // Create the first connector. We don't care about the actual
        // location at this stage
        Site a = target.getTailSite(figureA, 0.0, 0.0);
        Site b = target.getHeadSite(figureB, 0.0, 0.0);
        connectorA = new StraightConnector(a, b);
        layer.add(connectorA);

        // Add an arrowhead to it
        Arrowhead arrow = new Arrowhead(b.getX(), b.getY(), b.getNormal());
        connectorA.setHeadEnd(arrow);

        // Create the second connector with an arrowhead
        a = target.getTailSite(figureB, 0.0, 0.0);
        b = target.getHeadSite(figureC, 0.0, 0.0);
        connectorB = new ArcConnector(a, b);
        layer.add(connectorB);
        arrow = new Arrowhead(b.getX(), b.getY(), b.getNormal());
        connectorB.setHeadEnd(arrow);

        // Create the third connector with an arrowhead
        a = target.getTailSite(figureB, 0.0, 0.0);
        b = target.getHeadSite(figureC, 0.0, 0.0);
        connectorC = new ArcConnector(a, b);
        // Swap the direction
        connectorC.setAngle(-connectorC.getAngle());
        layer.add(connectorC);
        arrow = new Arrowhead(b.getX(), b.getY(), b.getNormal());
        connectorC.setHeadEnd(arrow);

        // Create a fourth connector with an arrowhead, which is a "self-loop"
        a = target.getTailSite(figureB, 0.0, 0.0);
        b = target.getHeadSite(figureB, 0.0, 0.0);
        connectorD = new ArcConnector(a, b);
        connectorD.setSelfLoop(true);
        // Swap the direction
        // connectorD.setAngle(-connectorD.getAngle());
        // connectorD.setAngle(-0.1);
        layer.add(connectorD);
        arrow = new Arrowhead(b.getX(), b.getY(), b.getNormal());
        connectorD.setHeadEnd(arrow);
    }

    /**
     * Set up the interaction so that the connectors stay glued to
     * the two figures. Since BasicController has already set up
     * an interactor for us, we will attach a listener to the drag
     * interactor and just call the connectors to re-route whenever
     * the nmouse moves.
     */
    public void setupInteraction () {
        // Because this pane has connectors on it, we make the pick
        // halo larger than the default so we can click-select connectors
        FigureLayer layer = graphicsPane.getForegroundLayer();
        layer.setPickHalo(4.0);

        // Add the default interactor to both figures
        SelectionInteractor si = controller.getSelectionInteractor();
        figureA.setInteractor(si);
        figureB.setInteractor(si);
        figureC.setInteractor(si);

        // Add a layer listener to the drag interactor.
        // The listener just tells both connectors to reroute themselves.
        DragInteractor i = controller.getDragInteractor();
        i.addLayerListener(new LayerAdapter () {
            public void mouseDragged (LayerEvent e) {
                connectorA.reroute();
                connectorB.reroute();
                connectorC.reroute();
                connectorD.reroute();
            }
        });

        // The connector selection interactor uses the same selection model
        SelectionInteractor ci = new SelectionInteractor(si.getSelectionModel());
        connectorA.setInteractor(ci);
        connectorB.setInteractor(ci);
        connectorC.setInteractor(ci);
        connectorD.setInteractor(ci);

        // Tell the selection dragger to select connectors too
        controller.getSelectionDragger().addSelectionInteractor(ci);

        // Create a manipulator to give resize handles on figures
        BoundsManipulator figureManipulator = new BoundsManipulator();
        controller.setSelectionManipulator(figureManipulator);

        // Make resizing reroute the connectors too
        DragInteractor j = figureManipulator.getHandleInteractor();
        j.addLayerListener(new LayerAdapter () {
            public void mouseDragged (LayerEvent e) {
                connectorA.reroute();
                connectorB.reroute();
                connectorC.reroute();
                connectorD.reroute();
            }
        });

        // Create and set up the manipulators for connectors. Straight
        // connectors will have an instance of ConnectorManipulator
        // attached to them, while arc connectors will have an instance
        // of ArcManipulator attached to them.
        ConnectorManipulator cManipulator = new ConnectorManipulator();
        cManipulator.setSnapHalo(4.0);
        cManipulator.setConnectorTarget(target);

        ArcManipulator aManipulator = new ArcManipulator();
        aManipulator.setSnapHalo(4.0);
        aManipulator.setConnectorTarget(target);

        TypedDecorator typedDecorator = new TypedDecorator();
        typedDecorator.addDecorator(StraightConnector.class, cManipulator);
        typedDecorator.addDecorator(ArcConnector.class, aManipulator);

        ci.setPrototypeDecorator(typedDecorator);

        // In ConnectorTutorial, we used connector listeners to
        // illustrate notification call-backs from the manipulator.
        // If we were to do that here, we would need a different listener
        // for each manipulator. We won't do it, once is enough.
    }

    /** Main function
     */
    public static void main (String argv[]) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ArcTutorial ex = new ArcTutorial();
                ex.createFigures();
                ex.createConnectors();
                ex.setupInteraction();
            }
        });
    }

    //////////////////////////////////////////////////////////////////////
    //// SelfPTarget

    /** SelfPTarget is used to find target sites. It overrides
     * PerimeterSite, but allows connector head and tails to
     * be located on the same figure.
     */
    public class SelfPTarget extends PerimeterTarget {
        /** Return true. This allows "self-arcs"
         */
        public boolean acceptHead(Connector c, Figure f) {
            return true;
        }

        /** Return true. This allows "self-arcs"
         */
        public boolean acceptTail(Connector c, Figure f) {
            return true;
        }
    }
}




