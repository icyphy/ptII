/*
 * $Id$
 *
 * Copyright (c) 1998 The Regents of the University of California.
 * All rights reserved.  See the file COPYRIGHT for details.
 */
package ptolemy.domains.csp.guidemo;

import diva.graph.*;
import diva.graph.model.*;
import diva.graph.layout.*;
import diva.canvas.*;
import diva.canvas.toolbox.*;
import diva.canvas.connector.*;
import diva.util.gui.BasicWindow;

import ptolemy.data.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.actor.process.*;
import ptolemy.domains.csp.lib.*;
import ptolemy.domains.csp.kernel.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

//////////////////////////////////////////////////////////////////////////
//// BusContentionApplication

/**
 * This demo shows a CSP universe with thread states.
 *
 * @author John S. Davis II (davisj@eecs.berkeley.edu)
 * @author Michael Shilman  (michaels@eecs.berkeley.edu)
 * @version $Revision$
 * @rating Red
 */
public class BusContentionApplication implements ActionListener {

    public BusContentionApplication() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                          main method                      ////

    public static void main(String argv[]) {
        BusContentionApplication app = new BusContentionApplication();
	app.initializeDemo();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     */
    public void actionPerformed(ActionEvent event) {
	String action = event.getActionCommand();
	if( action.equals("START") ) {
	    try {
                this.runDemo();
            } catch( Exception e ) {
	        e.printStackTrace(); 
	        throw new InternalErrorException("Error in GoButton: " 
                        + e.getMessage()); 
            }
	}
	if( action.equals("QUIT") ) {
	    shutDown();
	}
    }

    /**  Construct the graph representing the topology.
     * This is sort of bogus because it's totally hird-wired,
     * but it will do for now...
     */
    public GraphModel constructDivaGraph () {
        GraphModel model = new GraphModel();

        // Nodes, with user object set to the actor
        Node n1 = model.createNode(_contentionActor);
        Node n2 = model.createNode(_alarmActor);
        Node n3 = model.createNode(_memoryActor);
        Node n4 = model.createNode(_processActor1);
        Node n5 = model.createNode(_processActor2);
        Node n6 = model.createNode(_processActor3);

        model.addNode(n1);
        model.addNode(n2);
        model.addNode(n3);
        model.addNode(n4);
        model.addNode(n5);
        model.addNode(n6);

        _nodeMap.put(_contentionActor,n1);
        _nodeMap.put(_alarmActor,n2);
        _nodeMap.put(_memoryActor,n3);
        _nodeMap.put(_processActor1,n4);
        _nodeMap.put(_processActor2,n5);
        _nodeMap.put(_processActor3,n6);

        // Edges
        model.createEdge(n1,n2);
        model.createEdge(n1,n4);
        model.createEdge(n1,n5);
        model.createEdge(n1,n6);
        model.createEdge(n3,n4);
        model.createEdge(n3,n5);
        model.createEdge(n3,n6);

        return model;
    }

    /** Construct the Ptolemy system
     */
    public CompositeActor constructPtolemyModel () {
        CompositeActor topLevelActor = new CompositeActor();
	Manager manager = new Manager("Manager");
	Director director = new CSPDirector("Director");
        try {
	    topLevelActor.setManager( manager );
	    topLevelActor.setDirector( director );
	    
            // Instantiate Actors 
	    _contentionActor = new CSPController( topLevelActor, "controller" ); 
	    _alarmActor = new CSPContentionAlarm( topLevelActor, "alarm" );
            _memoryActor = new CSPMemory( topLevelActor, "memory" ); 
	    _processActor1 = new CSPProcessor( topLevelActor, "proc1", 1 ); 
	    _processActor2 = new CSPProcessor( topLevelActor, "proc2", 2 ); 
	    _processActor3 = new CSPProcessor( topLevelActor, "proc3", 3 ); 

	    // Set up ports, relation 
	    IOPort reqOut = (IOPort)_contentionActor.getPort("requestOut"); 
	    IOPort reqIn = (IOPort)_contentionActor.getPort("requestIn"); 
	    IOPort contendOut = (IOPort)_contentionActor.getPort("contendOut"); 
	    IOPort contendIn = (IOPort)_contentionActor.getPort("contendIn"); 

	    IOPort _alarmOut = (IOPort)_alarmActor.getPort("output"); 
	    IOPort _alarmIn = (IOPort)_alarmActor.getPort("input"); 
	    IOPort memOut = (IOPort)_memoryActor.getPort("output"); 
	    IOPort memIn = (IOPort)_memoryActor.getPort("input"); 

	    IOPort p1_ReqOut = (IOPort)_processActor1.getPort("requestOut"); 
	    IOPort p2_ReqOut = (IOPort)_processActor2.getPort("requestOut"); 
	    IOPort p3_ReqOut = (IOPort)_processActor3.getPort("requestOut"); 
	    IOPort p1_ReqIn = (IOPort)_processActor1.getPort("requestIn"); 
	    IOPort p2_ReqIn = (IOPort)_processActor2.getPort("requestIn"); 
	    IOPort p3_ReqIn = (IOPort)_processActor3.getPort("requestIn"); 

	    IOPort p1_MemOut = (IOPort)_processActor1.getPort("memoryOut"); 
	    IOPort p2_MemOut = (IOPort)_processActor2.getPort("memoryOut"); 
	    IOPort p3_MemOut = (IOPort)_processActor3.getPort("memoryOut"); 
	    IOPort p1_MemIn = (IOPort)_processActor1.getPort("memoryIn"); 
	    IOPort p2_MemIn = (IOPort)_processActor2.getPort("memoryIn"); 
	    IOPort p3_MemIn = (IOPort)_processActor3.getPort("memoryIn"); 

	    IORelation inReqs, outReqs, reads, writes, outContends, inContends;

	    // Set up connections 
	    inReqs = (IORelation)topLevelActor.connect(reqIn, p1_ReqOut ); 
	    inReqs = (IORelation)topLevelActor.connect(reqIn, p2_ReqOut ); 
	    inReqs = (IORelation)topLevelActor.connect(reqIn, p3_ReqOut ); 

	    outContends = (IORelation)topLevelActor.connect(contendOut, 
	            _alarmIn );
            inContends = (IORelation)topLevelActor.connect(contendIn,
	            _alarmOut );

            outReqs = (IORelation)topLevelActor.connect( reqOut, p1_ReqIn ); 
	    outReqs = (IORelation)topLevelActor.connect( reqOut, p2_ReqIn ); 
	    outReqs = (IORelation)topLevelActor.connect( reqOut, p3_ReqIn ); 

	    reads = (IORelation)topLevelActor.connect( memOut, p1_MemIn ); 
	    reads = (IORelation)topLevelActor.connect( memOut, p2_MemIn ); 
	    reads = (IORelation)topLevelActor.connect( memOut, p3_MemIn ); 

	    writes = (IORelation)topLevelActor.connect( memIn, p1_MemOut ); 
	    writes = (IORelation)topLevelActor.connect( memIn, p2_MemOut ); 
	    writes = (IORelation)topLevelActor.connect( memIn, p3_MemOut );

            System.out.println("Connections are complete.");

        } catch (Exception e) {
	    e.printStackTrace();
            throw new RuntimeException(e.toString());
        }
        return topLevelActor;
    }

    /**
     * Construct the graph widget with the default constructor (giving 
     * it an empty graph), and then set the model once the _window is 
     * showing. Add control buttons to the _window.
     */
    public void displayGraph(JGraph g, GraphModel model) {
        _window = new BasicWindow("Basic Window"); 
	Panel controlPanel = new Panel(); 

	Button startButton = new Button("START"); 
	startButton.addActionListener( this ); 
	controlPanel.add(startButton, BorderLayout.WEST); 

	Button quitButton = new Button("QUIT"); 
	quitButton.addActionListener( this ); 
	controlPanel.add(quitButton, BorderLayout.EAST); 

	controlPanel.setVisible(true); 
	_window.getContentPane().add(controlPanel, BorderLayout.NORTH); 

	_window.getContentPane().add(g, BorderLayout.CENTER); 

	_window.setSize(500, 600); 
	_window.setLocation(100, 100); 
	_window.setVisible(true);

        // Make sure we have the right renderers and then display the graph
        GraphPane gp = (GraphPane) g.getCanvasPane();
        GraphView gv = gp.getGraphView();
        gv.setNodeRenderer(new ThreadRenderer());
        gv.setEdgeRenderer(new LocalEdgeRenderer());
        g.setGraphModel(model);

        // Do the layout
        LevelLayout staticLayout = new LevelLayout();
        staticLayout.setOrientation(LevelLayout.HORIZONTAL);
        staticLayout.layout(gv, model.getGraph());
        gp.repaint();
    }

    /**
     */
    public void initializeDemo() {
        _nodeMap = new HashMap();
        _jgraph = new JGraph();

        // Construct the Ptolemy kernel topology
        _topLevelActor = constructPtolemyModel();

        // Construct the graph representing the topology
        _model = constructDivaGraph();

        // Display the model in the _window
        try {
            displayGraph(_jgraph, _model);
        }
        catch(Exception ex) {
            ex.printStackTrace();
            System.exit(0);
        }
    }
 
    /**
     */
    public void runDemo() {
        StateListener listener = 
	        new StateListener((GraphPane)_jgraph.getCanvasPane());
	_processActor1.addListeners(listener);
	_processActor2.addListeners(listener);
	_processActor3.addListeners(listener);
	System.out.println("Listeners set");

        // Run the model
 	Manager manager = _topLevelActor.getManager();

	// I'm not sure why but "manager.run()" doesn't seem to work.
	manager.startRun();
        System.out.println("Goodbye.\n");
	return;
    }

    /**
     */
    public void shutDown() {
        Manager manager = _topLevelActor.getManager(); 
	manager.terminate(); 
	_window.setVisible(false); 
	// FIXME:  _window.displose(); 
	_window =  null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ////

    // The mapping from Ptolemy actors to graph nodes
    private HashMap _nodeMap;

    // The JGraph where we display stuff
    private JGraph _jgraph;

    // The Diva graph model
    private GraphModel _model;

    // The window to display in
    private BasicWindow _window;

    // The Actors
    CSPController _contentionActor;
    CSPContentionAlarm _alarmActor;
    CSPMemory _memoryActor;
    CSPProcessor _processActor1;
    CSPProcessor _processActor2;
    CSPProcessor _processActor3;

    // Top Level Actor
    CompositeActor _topLevelActor;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////


    //////////////////////////////////
    //// StateListener
    /**
     * StateListener is an inner class that listens to state
     * events on the Ptolemy kernel and changes the color of
     * the nodes appropriately.
     */
    public class StateListener implements ExecEventListener {

        // The Pane
        GraphPane _graphPane;

        /** Create a listener on the given graph pane
         */
        public StateListener (GraphPane pane) {
            _graphPane = pane;
        }

        /** Respond to a state changed event.
         */
        public void stateChanged(ExecEvent event) {
            final int state = event.getCurrentState();
            Actor actor = event.getActor();

            // Get the corresponding graph node and its figure
            Node node = (Node) _nodeMap.get(actor);
            LabelWrapper wrapper = (LabelWrapper)
                _graphPane.getGraphView().getNodeFigure(node);
            final BasicFigure figure = (BasicFigure)
                wrapper.getChild();

            // Color the graph
            try {
                SwingUtilities.invokeAndWait(new Runnable () {
                    public void run () {
                        switch (state) {
                        case 1:
			    figure.setFillPaint(Color.yellow);
                            break;
                        
                        case 2:
                            figure.setFillPaint(Color.yellow);
                            break;
                        
                        case 3:
                            figure.setFillPaint(Color.green);
                            break;

                        case 4:
                            figure.setFillPaint(Color.red);
                            break;

                        default:
                            System.out.println("Unknown state: " + state);
                        }
                    }
                });
            } 
            catch (Exception e) {}
        }
    }


    //////////////////////////////////
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
                   actor instanceof CSPController 
                || actor instanceof CSPMemory 
                || actor instanceof CSPContentionAlarm;

            
            BasicFigure f;
            if (isEllipse) {
                f = new BasicEllipse(0, 0, _size, _size);
            } else {
                f = new BasicRectangle(0, 0, _size, _size); 
		f.setFillPaint(Color.blue);
            }
            String label = actor.getName();
            System.out.println("Actor " + actor + " has label " + label);
            LabelWrapper w = new LabelWrapper(f, label);
            w.setAnchor(SwingConstants.SOUTH);
            w.getLabel().setAnchor(SwingConstants.NORTH);
            return w;
        }
    }

    //////////////////////////////////
    //// LocalEdgeRenderer

    /**
     * LocalEdgeRenderer draws arrowheads on both ends of the connector
     */
    public class LocalEdgeRenderer implements EdgeRenderer {
      /**
       * Render the edge
       */
      public Connector render (Edge edge, Site tailSite, Site headSite) {
	StraightConnector c = new StraightConnector(tailSite, headSite);

	// Create an arrow at the head
	Arrowhead headArrow = new Arrowhead(
					    headSite.getX(), headSite.getY(),
					    headSite.getNormal());
	c.setHeadEnd(headArrow);

	// Create an arrow at the tail
	Arrowhead tailArrow = new Arrowhead(
					    tailSite.getX(), tailSite.getY(),
					    tailSite.getNormal());
	c.setTailEnd(tailArrow);

	c.setUserObject(edge);
	return c;
      }
    }
}
