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
import java.awt.geom.AffineTransform;

import javax.swing.SwingUtilities;

import diva.canvas.Figure;
import diva.canvas.FigureLayer;
import diva.canvas.GraphicsPane;
import diva.canvas.JCanvas;
import diva.canvas.Site;
import diva.canvas.connector.AbstractConnectorTarget;
import diva.canvas.connector.Arrowhead;
import diva.canvas.connector.Blob;
import diva.canvas.connector.ConnectorEvent;
import diva.canvas.connector.ConnectorListener;
import diva.canvas.connector.ConnectorManipulator;
import diva.canvas.connector.ManhattanConnector;
import diva.canvas.connector.StraightConnector;
import diva.canvas.event.LayerAdapter;
import diva.canvas.event.LayerEvent;
import diva.canvas.interactor.BoundsGeometry;
import diva.canvas.interactor.DragInteractor;
import diva.canvas.interactor.SelectionInteractor;
import diva.canvas.toolbox.BasicController;
import diva.canvas.toolbox.BasicRectangle;
import diva.gui.BasicFrame;


/** This tutorial shows how to use Connectors.
 *
 * <img src="../../../../packages/canvas/tutorial/images/ConnectorTutorial.gif" align="right">
 *
 * Connectors are objects
 * that connect locations on two different figures. These locations
 * are identified by objects called Sites, which represents points
 * such as the north-west corner of a rectangle, or a vertex of a
 * poly-line. In general, sites can be anywhere on a figure, and
 * specific figure classes have methods to access their sites.  (See
 * the SitesTutorial for more examples of Sites.)
 *
 * <p> Once a Connector is connected between two sites, it is easy to make
 * it appear as though the Connector is "glued" to the figures
 * containing the sites. Any code that moves or changes one of the two
 * figure only needs to call one of route(), reroute(), headMoved(),
 * or tailMoved(), and the connector will redraw itself between the
 * sites.
 *
 * <p>
 * The code to create a connector looks like this:
 * <pre>
 *     Site a = figureA.getE();
 *     Site b = figureB.getN();
 *     connectorA = new StraightConnector(a, b);
 *     layer.add(connectorA);
 * </pre>
 *
 * (Where the methods getE() and getN() are methods on some figure
 * that return Sites -- in this case, on the east and north edges
 * of the respective figures.)
 *
 * <p> In general, there can be arbitrarily many different kinds of
 * connector. The Diva canvas currently provides two: one that simply
 * draws a straight line between the two sites, and one that draws a
 * "manhattan" routing between the two sites. Each of these also
 * accepts an object on each end that will draw a decoration such as
 * an arrow-head or a circle at the attachment point.  For example,
 * we can add an arrowhead to the connector with:
 *
 * <pre>
 *     Arrowhead arrow = new Arrowhead(b.getX(), b.getY(), b.getNormal());
 *     connectorA.setHeadEnd(arrow);
 * </pre>
 *
 * <p>
 * Once a Connector is connected between two sites, it is easy to make it
 * appear as though the Connector is "glued" to the figures containing
 * the sites. Any code that moves or changes one of the two figure needs
 * to call one of the methods route(), reroute(), headMoved(), or
 * tailMoved() on the connector, and the connector will redraw itself
 * between the sites. In this example, we have set up a DragInteractor
 * to move the two rectangles shown in the image above. To also reroute
 * the connectors when either changes, we add a layer listener to the
 * interactor.  After the interactor has handled a mouse event, it passes
 * the event on to this listener.
 *
 * <pre>
 *     DragInteractor i = controller.getDragInteractor();
 *     i.addLayerListener(new LayerAdapter () {
 *         public void mouseDragged (LayerEvent e) {
 *             connectorA.reroute();
 *             connectorB.reroute();
 *         }
 *     });
 * </pre>
 *
 * <p>
 * When a connector is selected, it gets a grab-handle on either end, which
 * can be grabbed to allow the connector to be disconnected from
 * the figure and reconnected to another. Although the code is
 * a little convoluted, the essence of it is this:
 *
 * <pre>
 *     SelectionInteractor ci = new SelectionInteractor(si.getSelectionModel());
 *     connectorA.setInteractor(ci);
 *     connectorB.setInteractor(ci);
 *
 *     ConnectorManipulator manipulator = new ConnectorManipulator();
 *     manipulator.setSnapHalo(4.0);
 *     manipulator.setConnectorTarget(new SRTarget());
 *     ci.setPrototypeDecorator(manipulator);
 * </pre>
 *
 * This code first creates an instance of SelectionInteractor,
 * which it sets as the interactor for the connectors. It then creates an
 * instance of ConnectorManipulator, and tell the interactor that when a
 * connector is selected, it should create a copy of the manipulator and
 * wrap it around the connector. The manipulator highlights the connector
 * with grab handles when the connector is selected.
 *
 * <p> Notice that in this code there are actually two selection interactors,
 * one for the rectangles and one for the connectors.  They are linked by having
 * the same selection model.  One effect that this has is that selecting a
 * rectangle when a connector is already selected will deselect the connector.
 * If there were two selection models then connectors and rectangles could
 * be selected entirely independently from eachother.
 *
 * <p> The SRTarget in the above code is an inner class that implements
 * the ConnectorTarget interface. This class is used by the
 * ConnectorManipulator when trying to find a good place to connect
 * to. In this example, the target is returning sites on the four edges
 * of the rectangles, which is why the connectors snap to those points.
 *
 * @author John Reekie
 * @version $Revision$
 */
public class ConnectorTutorial {

    // The JCanvas
    private JCanvas canvas;

    // The GraphicsPane
    private GraphicsPane graphicsPane;

    /** The controller
     */
    private BasicController controller;

    /** The two figures
     */
    private SitedRectangle figureA;
    private SitedRectangle figureB;

    /** The two connectors
     */
    private StraightConnector connectorA;
    private ManhattanConnector connectorB;

    /** Create a JCanvas and put it into a window.
     */
    public ConnectorTutorial () {
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

        figureA = new SitedRectangle(10.0,10.0,50.0,50.0,Color.red);
        figureB = new SitedRectangle(100.0,100.0,100.0,50.0,Color.green);

        layer.add(figureA);
        layer.add(figureB);
    }

    /**
     * Create the connectors between the two figures. We will firstly
     * create one StraightConnector with a circle and an arrowhead
     * on it, and then a ManhattanConnector with a diamon on one end.
     */
    public void createConnectors () {
        FigureLayer layer = graphicsPane.getForegroundLayer();

        // Create the first connector
        Site a = figureA.getE();
        Site b = figureB.getN();
        connectorA = new StraightConnector(a, b);

        // Add the circle and arrowhead to it
        Blob blob = new Blob(a.getX(), a.getY(),
                a.getNormal(), Blob.BLOB_CIRCLE);
        connectorA.setTailEnd(blob);

        Arrowhead arrow = new Arrowhead(b.getX(), b.getY(), b.getNormal());
        connectorA.setHeadEnd(arrow);

        // Add it to the layer
        layer.add(connectorA);

        // Create the second connector
        Site c = figureA.getS();
        Site d = figureB.getW();
        connectorB = new ManhattanConnector(c, d);

        // Add the diamond
        Blob diamond = new Blob(
                c.getX(),
                c.getY(),
                c.getNormal(),
                Blob.BLOB_DIAMOND);
        diamond.setSizeUnit(6.0);
        diamond.setFilled(false);
        connectorB.setTailEnd(diamond);

        // Add it to the layer
        layer.add(connectorB);
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
        layer.setPickHalo(2.0);

        // Add the default interactor to both figures
        SelectionInteractor si = controller.getSelectionInteractor();
        figureA.setInteractor(si);
        figureB.setInteractor(si);

        // Add a layer listener to the drag interactor.
        // The listener just tells both connectors to reroute themselves.
        DragInteractor i = controller.getDragInteractor();
        i.addLayerListener(new LayerAdapter () {
                public void mouseDragged (LayerEvent e) {
                    connectorA.reroute();
                    connectorB.reroute();
                }
            });

        // The connector selection interactor uses the same selection model
        SelectionInteractor ci = new SelectionInteractor(si.getSelectionModel());
        connectorA.setInteractor(ci);
        connectorB.setInteractor(ci);

        // Tell the selection dragger to select connectors too
        controller.getSelectionDragger().addSelectionInteractor(ci);

        // Create and set up the manipulator for connectors
        ConnectorManipulator manipulator = new ConnectorManipulator();
        manipulator.setSnapHalo(4.0);
        manipulator.setConnectorTarget(new SRTarget());
        ci.setPrototypeDecorator(manipulator);

        // To illustrate the notification call-backs from the
        // manipulator, here is an simple example of a connector
        // listener
        ConnectorListener cl = new ConnectorListener() {
                public void connectorDragged(ConnectorEvent e) {
                    //// System.out.println("Dragged");
                }
                public void connectorDropped(ConnectorEvent e) {
                    System.out.println("Dropped");
                }
                public void connectorSnapped(ConnectorEvent e) {
                    System.out.println("Snapped");
                }
                public void connectorUnsnapped(ConnectorEvent e) {
                    System.out.println("Unsnapped");
                }
            };
        manipulator.addConnectorListener(cl);
    }

    /** Main function
     */
    public static void main (String argv[]) {
        // Always invoke graphics code in the event thread
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    ConnectorTutorial ex = new ConnectorTutorial();
                    ex.createFigures();
                    ex.createConnectors();
                    ex.setupInteraction();
                }
            });
    }

    //////////////////////////////////////////////////////////////////////
    //// SitedRectangle

    /** SitedRectangle is a class that provides four sites that
     * we use in the examples to attach connectors to. In this
     * example, we make life easy by using an instance of the
     * BoundsGeometry class, but in general, figures will want
     * to define their own sites. One thing to note about this
     * figure: it does not itself contain the code that re-routes
     * the attached connectors. Although it could override transform()
     * and translate, in general it is better for this kind of routing
     * to be initiated by the interaction code.
     */
    public class SitedRectangle extends BasicRectangle {
        // The geometry object
        private BoundsGeometry _geometry;

        /** Create a new instance of this figure.
         */
        public SitedRectangle (
                double x, double y,
                double width, double height, Color color) {
            super(x, y, width, height, color);
            _geometry = new BoundsGeometry(this,getBounds());
        }

        /** Get the north site.
         */
        public Site getN () {
            return _geometry.getN();
        }

        /** Get the south site.
         */
        public Site getS () {
            return _geometry.getS();
        }

        /** Get the east site.
         */
        public Site getE () {
            return _geometry.getE();
        }

        /** Get the west site.
         */
        public Site getW () {
            return _geometry.getW();
        }

        /** Update the geometry
         */
        public void transform (AffineTransform at) {
            super.transform(at);
            _geometry.setShape(getShape());
        }

        /** Update the geometry
         */
        public void translate (double x, double y) {
            super.translate(x, y);
            _geometry.translate(x,y);
        }
    }

    //////////////////////////////////////////////////////////////////////
    //// SRTarget

    /** SRTarget is used to find a useful site on a SitedRectangle.
     */
    public class SRTarget extends AbstractConnectorTarget {

        /** Return the nearest site on the figure
         */
        public Site getHeadSite (Figure f, double x, double y) {
            if (f instanceof SitedRectangle) {
                SitedRectangle sr = (SitedRectangle) f;
                Site s = closest(sr.getN(), sr.getS(), x, y);
                s = closest(s, sr.getE(), x, y);
                s = closest(s, sr.getW(), x, y);
                return s;
            } else {
                return null;
            }
        }

        private Site closest(Site a, Site b, double x, double y) {
            double q = a.getPoint().distanceSq(x,y);
            double r = b.getPoint().distanceSq(x,y);
            if (q < r) {
                return a;
            } else {
                return b;
            }
        }
    }
}



