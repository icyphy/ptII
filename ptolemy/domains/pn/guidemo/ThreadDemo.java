/*
 * $Id$
 *
 * Copyright (c) 1998 The Regents of the University of California.
 * All rights reserved.  See the file COPYRIGHT for details.
 */
package ptolemy.domains.pn.guidemo;

import diva.graph.*;
import diva.graph.model.*;
import diva.graph.layout.*;
import diva.canvas.*;
import diva.canvas.toolbox.*;
import diva.util.gui.TutorialWindow;

import ptolemy.domains.pn.kernel.*;
import ptolemy.domains.pn.lib.*;
import ptolemy.actor.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;

import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;


/**
 * This demo shows a PN universe with thread states.
 *
 * @author Mudit Goel  (mudit@eecs.berkeley.edu)
 * @author Michael Shilman  (michaels@eecs.berkeley.edu)
 * @version $Revision$
 * @rating Red
 */
public class ThreadDemo {
    /** The mapping from Ptolemy actors to graph nodes
     */
    private HashMap nodeMap = new HashMap();

    /** The JGraph where we display stuff
     */
    JGraph jgraph = new JGraph();

    /** The window to display in
     */
    private TutorialWindow window;

    /* The actors
     */
    PNImageSource a1;
    MatrixUnpacker a2;
    RLEncoder a3;
    RLDecoder a4;
    MatrixPacker a5;
    PNImageSink a6;
    ImageDisplay a7;
    ImageDisplay a8;

    public static void main(String argv[]) {
        new ThreadDemo();
    }

    private ThreadDemo() {

        // Construct the Ptolemy kernel topology
        CompositeActor compositeActor = constructPtolemyModel();

        // Construct the graph representing the PN topology
        GraphModel model = constructThreadGraph();

        // Display the model in the window
        try {
            displayGraph(jgraph, model);
        }
        catch(Exception ex) {
            ex.printStackTrace();
            System.exit(0);
        }
 
        // Add the process state listener
        BasePNDirector pnDir = (BasePNDirector) compositeActor.getDirector();
        pnDir.addProcessListener(new StateListener(
                 (GraphPane) jgraph.getCanvasPane()));

        // Run the model
	System.out.println("Connections made");
        Parameter p = (Parameter)pnDir.getAttribute("Initial_queue_capacity");
        p.setToken(new IntToken(10));
 	compositeActor.getManager().run();
        System.out.println("Bye World\n");
	return;
    }

    /**  Construct the graph representing the PN topology.
     * This is sort of bogus because it's totally hird-wired,
     * but it will do for now...
     */
    public GraphModel constructThreadGraph () {
        GraphModel model = new GraphModel();

        // nodes, with user object set to the actor
        Node n1 = model.createNode(a1);
        Node n2 = model.createNode(a2);
        Node n3 = model.createNode(a3);
        Node n4 = model.createNode(a4);
        Node n5 = model.createNode(a5);
        Node n6 = model.createNode(a6);
        Node n7 = model.createNode(a7);
        Node n8 = model.createNode(a8);

        model.addNode(n1);
        model.addNode(n2);
        model.addNode(n3);
        model.addNode(n4);
        model.addNode(n5);
        model.addNode(n6);
        model.addNode(n7);
        model.addNode(n8);

        nodeMap.put(a1,n1);
        nodeMap.put(a2,n2);
        nodeMap.put(a3,n3);
        nodeMap.put(a4,n4);
        nodeMap.put(a5,n5);
        nodeMap.put(a6,n6);
        nodeMap.put(a7,n7);
        nodeMap.put(a8,n8);

        // Edges
        model.createEdge(n1,n2);
        model.createEdge(n2,n3);
        model.createEdge(n3,n4);
        model.createEdge(n4,n5);
        model.createEdge(n5,n6);

        model.createEdge(n1,n7);
        model.createEdge(n5,n8);

        return model;
    }

    /** Construct the Ptolemy system
     */
    public CompositeActor constructPtolemyModel () {
	CompositeActor c1 = new CompositeActor();
	Manager manager = new Manager();
        // FIXME FIXME FIXME
        try {
            c1.setManager(manager);

            BasePNDirector local = new BasePNDirector("Local");
            c1.setDirector(local);
            //myUniverse.setCycles(Integer.parseInt(args[0]));

            a1 = new PNImageSource(c1, "A1");
 
            //Parameter p1 = (Parameter)a1.getAttribute("Image_file");
            //p1.setToken(new StringToken("/users/mudit/ptII/ptolemy/domains/pn/lib/test/ptII.pbm"));
            String filename = 
                "/users/mudit/_PTII/ptolemy/domains/pn/lib/test/ptII.pbm";
            try {
                FileInputStream fis = new FileInputStream(filename);
                a1.read(fis);
            } catch (FileNotFoundException e) {
                System.err.println("FileNotFoundException: "+ e.toString());
            }
            //p1.setToken(new StringToken("/users/ptII/ptolemy/domains/pn/lib/test/ptII.pbm"));
            a2 = new MatrixUnpacker(c1, "A2");
            a3 = new RLEncoder(c1, "A3");
            a4 = new RLDecoder(c1, "A4");
            a5 = new MatrixPacker(c1, "A5");
            a6 = new PNImageSink(c1, "A6");
            Parameter p1 = (Parameter)a6.getAttribute("Output_file");
            p1.setToken(new StringToken("/tmp/image.pbm"));
            a7 = new ImageDisplay(c1, "dispin");
            p1 = (Parameter)a7.getAttribute("FrameName");
            p1.setToken(new StringToken("InputImage"));
            a8 = new ImageDisplay(c1, "dispout");
            p1 = (Parameter)a8.getAttribute("FrameName");
            p1.setToken(new StringToken("OutputImage"));

            IOPort portin = (IOPort)a1.getPort("output");
            IOPort portout = (IOPort)a2.getPort("input");
            ComponentRelation rel = c1.connect(portin, portout);
            (a7.getPort("image")).link(rel);

            portin = (IOPort)a2.getPort("output");
            portout = (IOPort)a3.getPort("input");
            c1.connect(portin, portout);

            portin =(IOPort) a2.getPort("dimensions");
            portout = (IOPort)a3.getPort("dimensionsIn");
            c1.connect(portin, portout);

            portin = (IOPort)a3.getPort("dimensionsOut");
            portout = (IOPort)a4.getPort("dimensionsIn");
            c1.connect(portin, portout);

            portin = (IOPort)a3.getPort("output");
            portout = (IOPort)a4.getPort("input");
            c1.connect(portin, portout);

            portin = (IOPort)a4.getPort("dimensionsOut");
            portout = (IOPort)a5.getPort("dimensions");
            c1.connect(portin, portout);

            portin = (IOPort)a4.getPort("output");
            portout = (IOPort)a5.getPort("input");
            c1.connect(portin, portout);

            portin = (IOPort)a5.getPort("output");
            portout = (IOPort)a6.getPort("input");
            rel = c1.connect(portin, portout);        
            (a8.getPort("image")).link(rel);
        }
        catch (Exception e) {
            throw new RuntimeException(e.toString());
        }
        return c1;
    }

    /**
     * Construct the graph widget with
     * the default constructor (giving it an empty graph),
     * and then set the model once the window is showing.
     */
    public void displayGraph(JGraph g, GraphModel model) {
        window = new TutorialWindow("PN Thread Demo");
        
        // Display the window
        window.getContentPane().add("Center", g);
        window.setSize(800, 300);
        window.setLocation(100, 100);
        window.setVisible(true);

        // Make sure we ahve the right renders and then
        // display the graph
        GraphPane gp = (GraphPane) g.getCanvasPane();
        GraphView gv = gp.getGraphView();
        gv.setNodeRenderer(new ThreadRenderer());
        g.setGraphModel(model);

        // Do the layout
        LevelLayout staticLayout = new LevelLayout();
        staticLayout.setOrientation(LevelLayout.HORIZONTAL);
        staticLayout.layout(gv, model.getGraph());
        gp.repaint();
    }

    ///////////////////////////////////////////////////////////////////
    //// StateListener

    /**
     * StateListener is an inner class that listens to state
     * events on the Ptolemy kernel and changes the color of
     * the nodes appropriately.
     */
    public class StateListener implements PNProcessListener {

        // The pane
        GraphPane _graphPane;

        /* Create a listener on the given graph pane
         */
        public StateListener (GraphPane pane) {
            _graphPane = pane;
        }

        /** Respond to a state changed event.
         */
        public void processStateChanged(PNProcessEvent event) {
            final int state = event.getCurrentState();
            Actor actor = event.getActor();

            // Get the corresponding graph node and its figure
            Node node = (Node) nodeMap.get(actor);
            LabelWrapper wrapper = (LabelWrapper)
                _graphPane.getGraphView().getNodeFigure(node);
            final BasicFigure figure = (BasicFigure)
                wrapper.getChild();

            // Color it!
            try {
                SwingUtilities.invokeAndWait(new Runnable () {
                    public void run () {
                        switch (state) {
                        case PNProcessEvent.PROCESS_BLOCKED:
                            figure.setFillPaint(Color.red);
                            break;
                        
                        case PNProcessEvent.PROCESS_FINISHED:
                            figure.setFillPaint(Color.black);
                            break;
                        
                        case PNProcessEvent.PROCESS_PAUSED:
                            figure.setFillPaint(Color.yellow);
                            break;

                        case PNProcessEvent.PROCESS_RUNNING:
                            figure.setFillPaint(Color.green);
                            break;

                        default:
                            System.out.println("Unknown state: " + state);
                        }
                    }
                });
            } 
            catch (Exception e) {}
        }

        /** Respond to a process finshed event.
         */
        public void processFinished(PNProcessEvent event) {
            // nothing yet
        }
    }


    ///////////////////////////////////////////////////////////////////
    //// ThreadRenderer

    /**
     * ThreadRenderer draws the nodes to represent running threads.
     */
    public class ThreadRenderer implements NodeRenderer {

        /** The rectangle size
         */
        private double _size = 50;

        /**
         * Return the rendered visual representation of this node.
         */
        public Figure render (Node n) {
            ComponentEntity actor = (ComponentEntity) n.getSemanticObject();

            boolean isEllipse = 
                   actor instanceof PNImageSource
                || actor instanceof PNImageSink
                || actor instanceof ImageDisplay;

            
            BasicFigure f;
            if (isEllipse) {
                f = new BasicEllipse(0, 0, _size, _size);
            } else {
                f = new BasicRectangle(0, 0, _size, _size);
            }
            String label = actor.getName();
            System.out.println("Actor " + actor + " has label " + label);
            LabelWrapper w = new LabelWrapper(f, label);
            w.setAnchor(SwingConstants.SOUTH);
            w.getLabel().setAnchor(SwingConstants.NORTH);
            return w;
        }
    }
}
