/* A DDE application illustrating localized Zeno conditions.

 Copyright (c) 1999 The Regents of the University of California.
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
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY

@ProposedRating Red (davisj@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.dde.demo.LocalZeno;

import diva.graph.*;
import diva.graph.model.*;
import diva.graph.model.Node;
import diva.graph.layout.*;
import diva.canvas.*;
import diva.canvas.toolbox.*;
import diva.canvas.connector.*;
import diva.util.gui.BasicWindow;

import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.actor.lib.*;
import ptolemy.actor.gui.*;
import ptolemy.domains.dde.lib.*;
import ptolemy.domains.dde.kernel.*;
import ptolemy.domains.dde.kernel.test.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

//////////////////////////////////////////////////////////////////////////
//// LocalZenoApplication

/** 
 *  A DDE application illustrating localized Zeno conditions.
 *
 *  @author John S. Davis II (davisj@eecs.berkeley.edu)
 *  @author Michael Shilman  (michaels@eecs.berkeley.edu)
 *  @version $Id$
 */
public class LocalZenoApplication implements ActionListener {

    /** Create an empty model with the specified manager and top-level
     *  composite actor. The model is actually created by the
     *  initializeDemo() method.
     */
    public LocalZenoApplication(Manager manager, 
	    TypedCompositeActor topLevel) {
	_manager = manager;
	_topLevel = topLevel;
    }

    ///////////////////////////////////////////////////////////////////
    ////                          main method                      ////

    public static void main(String argv[]) {
	Manager manager = new Manager("manager");
	TypedCompositeActor topLevel = new TypedCompositeActor();

	try {
            topLevel.setName("topLevel"); 
            topLevel.setManager(manager);
	} catch( KernelException e ) {
            // This should not occur.
	    throw new InternalErrorException(e.toString());
	}

        LocalZenoApplication app = 
	        new LocalZenoApplication( manager, topLevel );

	Panel nullAppletPanel = null;
	app.initializeDemo(nullAppletPanel);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Respond to button pushes.
     */
    public void actionPerformed(ActionEvent event) {
	String action = event.getActionCommand();
	if( action.equals("Go") ) {
	    try {
                this.runDemo();
            } catch( Exception e ) {
	        e.printStackTrace(); 
	        throw new InternalErrorException("Error in GoButton: " 
                        + e.getMessage()); 
            }
	}
	if( action.equals("Stop") ) {
	    endSimulation();
	} else if( action.equals("Quit") ) {
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
        Node n1 = model.createNode(_clock);

        Node n2 = model.createNode(_join1);
        Node n3 = model.createNode(_fork1);
        Node n4 = model.createNode(_fBack1);
        Node n5 = model.createNode(_rcvr1);

        Node n6 = model.createNode(_join2);
        Node n7 = model.createNode(_fork2);
        Node n8 = model.createNode(_fBack2);
        Node n9 = model.createNode(_rcvr2);

        model.addNode(n1);
        model.addNode(n2);
        model.addNode(n3);
        model.addNode(n4);
        model.addNode(n5);
        model.addNode(n6);
        model.addNode(n7);
        model.addNode(n8);
        model.addNode(n9);

        _nodeMap.put(_clock, n1);

        _nodeMap.put(_join1, n2);
        _nodeMap.put(_fork1, n3);
        _nodeMap.put(_fBack1, n4);
        _nodeMap.put(_rcvr1, n5);

        _nodeMap.put(_join2, n6);
        _nodeMap.put(_fork2, n7);
        _nodeMap.put(_fBack2, n8);
        _nodeMap.put(_rcvr2, n9);

        // Edges
        model.createEdge(n1,n2);
        model.createEdge(n2,n3);
        model.createEdge(n3,n4);
        model.createEdge(n3,n5);
        model.createEdge(n4,n2);

        model.createEdge(n1,n6);
        model.createEdge(n6,n7);
        model.createEdge(n7,n8);
        model.createEdge(n7,n9);
        model.createEdge(n8,n6);

        return model;
    }

    /** Construct the Ptolemy system
     */
    public void constructPtolemyModel () {
        try {
	    // Instantiate Director
	    DDEDirector director = new DDEDirector(_topLevel, "Director");
	    Parameter dirStopTime = (Parameter)director.getAttribute("stopTime");
	    dirStopTime.setToken( new DoubleToken(30.0) );
	    
            // Instantiate Actors 
	    _clock = new ListenClock( _topLevel, "clock" );
	    _clock.values.setExpression( "[1, 1]" );
	    _clock.period.setToken( new DoubleToken(20.0) );
	    _clock.offsets.setExpression( "[5.0, 15.0]" );
	    _clock.stopTime.setToken( new DoubleToken(30.0) );

	    _join1 = new ListenWire( _topLevel, "join1" );
	    _fork1 = new ListenFork( _topLevel, "fork1" );
	    _fBack1 = new ListenFBDelay( _topLevel, "fBack1" );
	    _join2 = new ListenWire( _topLevel, "join2" );
	    _fork2 = new ListenFork( _topLevel, "fork2" );
	    _fBack2 = new ListenFBDelay( _topLevel, "fBack2" );
            
	    _rcvr1 = new ListenSink( _topLevel, "rcvr1" );
	    _rcvr2 = new ListenSink( _topLevel, "rcvr2" );
            
	    _fBack1.setDelay(4.5);
	    _fBack2.setDelay(0.5);

	    // Set up ports, relation 
	    TypedIOPort clockOut = (TypedIOPort)_clock.getPort("output"); 
	    clockOut.setMultiport(true);

	    // Set up connections 
	    _topLevel.connect( _clock.output, _join1.input );
	    _topLevel.connect( _clock.output, _join2.input );

	    _topLevel.connect( _join1.output, _fork1.input );
	    _topLevel.connect( _fork1.output1, _rcvr1.input );
	    _topLevel.connect( _fork1.output2, _fBack1.input );
	    _topLevel.connect( _fBack1.output, _join1.input );

	    _topLevel.connect( _join2.output, _fork2.input );
	    _topLevel.connect( _fork2.output1, _rcvr2.input );
	    _topLevel.connect( _fork2.output2, _fBack2.input );
	    _topLevel.connect( _fBack2.output, _join2.input );

            System.out.println("Connections are complete.");
	    
        } catch (Exception e) {
	    e.printStackTrace();
            throw new RuntimeException(e.toString());
        }
    }

    /**
     * Construct the graph widget with the default constructor (giving 
     * it an empty graph), and then set the model once the _window is 
     * showing. Add control buttons to the _window.
     */
    public void displayGraph(JGraph g, GraphModel model) {
	Panel controlPanel = new Panel(); 

	Button startButton = new Button("Go"); 
	startButton.addActionListener( this ); 
	controlPanel.add(startButton, BorderLayout.WEST); 

	Button stopButton = new Button("Stop"); 
	stopButton.addActionListener( this ); 
	controlPanel.add(stopButton, BorderLayout.CENTER); 

	Button quitButton = new Button("Quit"); 
	quitButton.addActionListener( this ); 
	controlPanel.add(quitButton, BorderLayout.EAST); 

	controlPanel.setVisible(true); 

        _window = new BasicWindow("Basic Window"); 
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
    public void initializeDemo(Panel appletPanel) {
        _nodeMap = new HashMap();
        _jgraph = new JGraph();
	_appletPanel = appletPanel;

        // Construct the Ptolemy kernel topology
        constructPtolemyModel();

        // Construct the graph representing the topology
        _model = constructDivaGraph();

        // Display the model in the _window
        try {
	    SwingUtilities.invokeAndWait(new Runnable (){
		public void run () {
		    displayGraph(_jgraph, _model);
		}
	    });
	    /*
            displayGraph(_jgraph, _model);
	    */
        }
        catch(Exception ex) {
            ex.printStackTrace();
            System.exit(0);
        }
        StateListener listener = 
	        new StateListener((GraphPane)_jgraph.getCanvasPane());
	_join1.addListeners(listener);
	_join2.addListeners(listener);
	_fork1.addListeners(listener);
	_fork2.addListeners(listener);
	_fBack1.addListeners(listener);
	_fBack2.addListeners(listener);
	_rcvr1.addListeners(listener);
	_rcvr2.addListeners(listener);
	_clock.addListeners(listener);
    }
 
    /**
     */
    public void runDemo() throws IllegalActionException {
        // Run the model
	// I'm not sure why but "manager.run()" doesn't seem to work.
	_manager.startRun();
	return;
    }

    /**
     */
    public void endSimulation() {
        Director director = _topLevel.getDirector(); 
	_manager.finish();
    }

    /**
     */
    public void shutDown() {
        Director director = _topLevel.getDirector(); 
	try {
	    // Eventually we will not need to call Director.wrapup()
	    // as Manager.finish() will subsume this responsibility.
	    director.wrapup();
	    _manager.finish();
	    _manager = null;
	} catch( IllegalActionException e ) {
	    System.err.println("IllegalActionException thrown while " + 
		    "attempting to shutDown()");
	    e.printStackTrace();
	}
	if( _appletPanel == null ) {
	    _window.setVisible(false); 
	    _window.dispose(); 
	    _window = null;

	    System.exit(0);
	} else {
	    _window.setVisible(false); 
	    _window.dispose(); 
	    _window = null;
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ////

    // The mapping from Ptolemy actors to graph nodes
    private HashMap _nodeMap;

    // The JGraph where we display stuff
    private JGraph _jgraph;

    // The Diva graph model
    private GraphModel _model;

    // The BasicWindow to display in if this is an Application
    private BasicWindow _window;

    // The Panel to display in if this is an Applet
    private Panel _appletPanel;

    // The Actors
    ListenClock _clock;
    ListenWire _join1;
    ListenFork _fork1;
    ListenFBDelay _fBack1;
    ListenSink _rcvr1;
    ListenWire _join2;
    ListenFork _fork2;
    ListenFBDelay _fBack2;
    ListenSink _rcvr2;

    // Top Level Actor
    TypedCompositeActor _topLevel;

    // Manager
    Manager _manager;

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
            
            String name = ((Nameable)actor).getName();

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
                            figure.setFillPaint(Color.green);
                            break;
                        
                        case 3:
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
                   actor instanceof ListenWire
                || actor instanceof ListenFork
		|| actor instanceof ListenClock
		|| actor instanceof ListenSink
                || actor instanceof ListenFBDelay;

            
            BasicFigure f;
            if (isEllipse) {
                f = new BasicEllipse(0, 0, _size, _size);
		f.setFillPaint(Color.blue);
            } else {
                f = new BasicRectangle(0, 0, _size, _size); 
		f.setFillPaint(Color.pink);
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

        /*
	// Create an arrow at the tail
	Arrowhead tailArrow = new Arrowhead(
					    tailSite.getX(), tailSite.getY(),
					    tailSite.getNormal());
	c.setTailEnd(tailArrow);
        */

	c.setUserObject(edge);
	return c;
      }
    }
}
