/* A CSP application.

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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.csp.demo.BusContention;

import diva.graph.compat.*;
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
import ptolemy.actor.gui.*;
import ptolemy.domains.csp.lib.*;
import ptolemy.domains.csp.kernel.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

//////////////////////////////////////////////////////////////////////////
//// BusContentionApplication

/** A model of hardware subsystems accessing a shared resource using
 *  rendezvous. The model shows the use of timed CSP to
 *  deterministically handle nondeterministic events.
 *  <p>
 *  The applet consists of a controller, three processors and a memory
 *  block. At randomly selected points in time, each processor can
 *  request permission from the controller to access the memory. The
 *  processors each have priorities associated with them, and in cases
 *  where there is a simultaneous memory access request, the controller
 *  grants permission to the processor with the highest priority.
 *  <p>
 *  All communication between actors in a CSP model of computation
 *  occurs via rendezvous. Rendezvous is an atomic form of
 *  communication. This model uses a timed extension to CSP, so each
 *  rendezvous logically occurs at a specific point in time.
 *  <p>
 *  Because of the atomic nature of rendezvous, when the controller
 *  receives a request for access, it cannot know whether there is
 *  another, higher priority request pending at the same time. To
 *  overcome this difficulty, an alarm is employed. The alarm is started
 *  by the controller immediately following the first request for memory
 *  access. It is awakened when time is ready to advance (the model
 *  blocks on delays). This indicates to the controller that no more
 *  memory requests will occur at the given point in time. Hence, the
 *  alarm uses centralized time to make deterministic an inherently
 *  non-deterministic activity.
 *  <p>
 *  In the applet, each of the initially blue processors (the circular
 *  nodes) can be in one of three states. The color yellow indicates
 *  that a processor is in state 1 and is waiting for the controller to
 *  give it permission to access memory. The color green indicates that
 *  a processor has been granted permission to access memory. The color
 *  red indicates that the processor has been denied memory access.
 *
 *  @author John S. Davis II (davisj@eecs.berkeley.edu)
 *  @author Michael Shilman  (michaels@eecs.berkeley.edu)
 *  @version $Id$
 */
public class BusContentionApplication implements ActionListener {

    /** Create an empty model with the specified manager and top-level
     *  composite actor.  The model is actually created by the
     *  initializeDemo() method.
     */
    public BusContentionApplication(Manager manager,
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

        BusContentionApplication app =
            new BusContentionApplication( manager, topLevel );

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
    public GraphModel constructDivaGraph() {
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

        _nodeMap.put(_contentionActor, n1);
        _nodeMap.put(_alarmActor, n2);
        _nodeMap.put(_memoryActor, n3);
        _nodeMap.put(_processActor1, n4);
        _nodeMap.put(_processActor2, n5);
        _nodeMap.put(_processActor3, n6);

        // Edges
        model.createEdge(n1, n2);
        model.createEdge(n1, n4);
        model.createEdge(n1, n5);
        model.createEdge(n1, n6);
        model.createEdge(n3, n4);
        model.createEdge(n3, n5);
        model.createEdge(n3, n6);

        return model;
    }

    /** Construct the Ptolemy system */
    public void constructPtolemyModel() {
        try {
	    Director director = new CSPDirector(_topLevel, "Director");

            // Instantiate Actors
	    _contentionActor = new Controller( _topLevel, "controller" );
	    _alarmActor = new ContentionAlarm( _topLevel, "alarm" );
            _memoryActor = new Memory( _topLevel, "memory" );
	    _processActor1 = new Processor( _topLevel, "proc1", 1 );
	    _processActor2 = new Processor( _topLevel, "proc2", 2 );
	    _processActor3 = new Processor( _topLevel, "proc3", 3 );

	    // Set up ports, relation
	    TypedIOPort reqOut =
                (TypedIOPort)_contentionActor.getPort("requestOut");
	    TypedIOPort reqIn =
                (TypedIOPort)_contentionActor.getPort("requestIn");
	    TypedIOPort contendOut =
                (TypedIOPort)_contentionActor.getPort("contendOut");
	    TypedIOPort contendIn =
                (TypedIOPort)_contentionActor.getPort("contendIn");

	    TypedIOPort _alarmOut =
                (TypedIOPort)_alarmActor.getPort("output");
	    TypedIOPort _alarmIn =
                (TypedIOPort)_alarmActor.getPort("input");
	    TypedIOPort memOut =
                (TypedIOPort)_memoryActor.getPort("output");
	    TypedIOPort memIn =
                (TypedIOPort)_memoryActor.getPort("input");

	    TypedIOPort p1_ReqOut =
                (TypedIOPort)_processActor1.getPort("requestOut");
	    TypedIOPort p2_ReqOut =
                (TypedIOPort)_processActor2.getPort("requestOut");
	    TypedIOPort p3_ReqOut =
                (TypedIOPort)_processActor3.getPort("requestOut");
	    TypedIOPort p1_ReqIn =
                (TypedIOPort)_processActor1.getPort("requestIn");
	    TypedIOPort p2_ReqIn =
                (TypedIOPort)_processActor2.getPort("requestIn");
	    TypedIOPort p3_ReqIn =
                (TypedIOPort)_processActor3.getPort("requestIn");

	    TypedIOPort p1_MemOut =
                (TypedIOPort)_processActor1.getPort("memoryOut");
	    TypedIOPort p2_MemOut =
                (TypedIOPort)_processActor2.getPort("memoryOut");
	    TypedIOPort p3_MemOut =
                (TypedIOPort)_processActor3.getPort("memoryOut");
	    TypedIOPort p1_MemIn =
                (TypedIOPort)_processActor1.getPort("memoryIn");
	    TypedIOPort p2_MemIn =
                (TypedIOPort)_processActor2.getPort("memoryIn");
	    TypedIOPort p3_MemIn =
                (TypedIOPort)_processActor3.getPort("memoryIn");

	    TypedIORelation inReqs, outReqs,
                reads, writes, outContends, inContends;

	    // Set up connections
	    inReqs = (TypedIORelation)_topLevel.connect(reqIn, p1_ReqOut );
	    inReqs = (TypedIORelation)_topLevel.connect(reqIn, p2_ReqOut );
	    inReqs = (TypedIORelation)_topLevel.connect(reqIn, p3_ReqOut );

	    outContends = (TypedIORelation)_topLevel.connect(contendOut,
	            _alarmIn );
            inContends = (TypedIORelation)_topLevel.connect(contendIn,
	            _alarmOut );

            outReqs = (TypedIORelation)_topLevel.connect( reqOut, p1_ReqIn );
	    outReqs = (TypedIORelation)_topLevel.connect( reqOut, p2_ReqIn );
	    outReqs = (TypedIORelation)_topLevel.connect( reqOut, p3_ReqIn );

	    reads = (TypedIORelation)_topLevel.connect( memOut, p1_MemIn );
	    reads = (TypedIORelation)_topLevel.connect( memOut, p2_MemIn );
	    reads = (TypedIORelation)_topLevel.connect( memOut, p3_MemIn );

	    writes = (TypedIORelation)_topLevel.connect( memIn, p1_MemOut );
	    writes = (TypedIORelation)_topLevel.connect( memIn, p2_MemOut );
	    writes = (TypedIORelation)_topLevel.connect( memIn, p3_MemOut );

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
	/*
          if( _appletPanel == null ) {
          _window = new BasicWindow("Basic Window");
          _window.getContentPane().add(controlPanel, BorderLayout.NORTH);
          _window.getContentPane().add(g, BorderLayout.CENTER);
          _window.setSize(500, 600);
          _window.setLocation(100, 100);
          _window.setVisible(true);
          } else {
          _appletPanel.add(controlPanel, BorderLayout.NORTH);
          _appletPanel.add(g, BorderLayout.CENTER);
          _appletPanel.setSize(500, 600);
          _appletPanel.setLocation(100, 100);
          _appletPanel.setVisible(true);
          }
	*/

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
		public void run() {
		    displayGraph(_jgraph, _model);
		}
	    });
            // displayGraph(_jgraph, _model);
        }
        catch(Exception ex) {
            ex.printStackTrace();
            System.exit(0);
        }
        StateListener listener =
            new StateListener((GraphPane)_jgraph.getCanvasPane());
	_processActor1.addListeners(listener);
	_processActor2.addListeners(listener);
	_processActor3.addListeners(listener);
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
    ////                         private variables                 ////

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
    Controller _contentionActor;
    ContentionAlarm _alarmActor;
    Memory _memoryActor;
    Processor _processActor1;
    Processor _processActor2;
    Processor _processActor3;

    // Top Level Actor
    TypedCompositeActor _topLevel;

    // Manager
    Manager _manager;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////


    ///////////////////////////////////////////////////////////////////
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
        public StateListener(GraphPane pane) {
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
                    public void run() {
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
        public Figure render(Node n) {
            ComponentEntity actor = (ComponentEntity) n.getSemanticObject();

            boolean isEllipse =
                actor instanceof Controller
                || actor instanceof Memory
                || actor instanceof ContentionAlarm;


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

    ///////////////////////////////////////////////////////////////////
    //// LocalEdgeRenderer

    /**
     * LocalEdgeRenderer draws arrowheads on both ends of the connector
     */
    public class LocalEdgeRenderer implements EdgeRenderer {
        /**
         * Render the edge
         */
        public Connector render(Edge edge, Site tailSite, Site headSite) {
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
