/* A CSP model of hardware bus contention.

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

import diva.graph.*;
import diva.graph.model.*;
import diva.graph.layout.*;
import diva.canvas.*;
import diva.canvas.toolbox.*;
import diva.canvas.connector.*;
import diva.canvas.interactor.*;

import ptolemy.data.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.actor.process.*;
import ptolemy.actor.gui.*;
import ptolemy.domains.csp.lib.*;
import ptolemy.domains.csp.gui.*;
import ptolemy.domains.csp.kernel.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

//////////////////////////////////////////////////////////////////////////
//// BusContentionApplet

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
public class BusContentionApplet extends CSPApplet {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Initialize the applet.
     */
    public void init() {
	super.init();

	getContentPane().setLayout( new BorderLayout(5, 5) );

	// The '3' argument specifies a 'go', 'stop' and 'layout' buttons.
	getContentPane().add( _createRunControls(3), BorderLayout.NORTH );

	constructPtolemyModel();

	_divaPanel = new JPanel( new BorderLayout() );
        _divaPanel.setBorder(new TitledBorder(
                new LineBorder(Color.black), "Animation"));
        _divaPanel.setBackground(_getBackground());
	_divaPanel.setSize( new Dimension(600, 350) );
	_divaPanel.setBackground(_getBackground());
	_jgraph.setBackground(_getBackground());
	getContentPane().add( _divaPanel, BorderLayout.CENTER );

        _graph = constructDivaGraph();
	final Graph finalGraph = _graph;

        try {
	    SwingUtilities.invokeAndWait(new Runnable (){
		public void run() {
		    displayGraph(_jgraph, finalGraph);
		}
	    });
        } catch(Exception ex) {
            ex.printStackTrace();
            System.exit(0);
        }

        StateListener listener =
	        new StateListener((GraphPane)_jgraph.getCanvasPane());
	_processActor1.addListeners(listener);
	_processActor2.addListeners(listener);
	_processActor3.addListeners(listener);
    }

    /**  Construct the graph representing the topology.
     * This is sort of bogus because it's totally hard-wired,
     * but it will do for now...
     */
    public Graph constructDivaGraph() {
	GraphImpl impl = new BasicGraphImpl();
	Graph graph = impl.createGraph(null);

        // Nodes, with user object set to the actor
        Node n1 = impl.createNode(_contentionActor);
        Node n2 = impl.createNode(_alarmActor);
        Node n3 = impl.createNode(_memoryActor);
        Node n4 = impl.createNode(_processActor1);
        Node n5 = impl.createNode(_processActor2);
        Node n6 = impl.createNode(_processActor3);

        impl.addNode(n1, graph);
        impl.addNode(n2, graph);
        impl.addNode(n3, graph);
        impl.addNode(n4, graph);
        impl.addNode(n5, graph);
        impl.addNode(n6, graph);

        _nodeMap.put(_contentionActor, n1);
        _nodeMap.put(_alarmActor, n2);
        _nodeMap.put(_memoryActor, n3);
        _nodeMap.put(_processActor1, n4);
        _nodeMap.put(_processActor2, n5);
        _nodeMap.put(_processActor3, n6);

        // Edges
	Edge e;

	e = impl.createEdge(null);
	impl.setEdgeHead(e, n1);
	impl.setEdgeTail(e, n2);
	
 	e = impl.createEdge(null);
	impl.setEdgeHead(e, n1);
	impl.setEdgeTail(e, n4);

	e = impl.createEdge(null);
	impl.setEdgeHead(e, n1);
	impl.setEdgeTail(e, n5);

	e = impl.createEdge(null);
	impl.setEdgeHead(e, n1);
	impl.setEdgeTail(e, n6);

	e = impl.createEdge(null);
	impl.setEdgeHead(e, n3);
	impl.setEdgeTail(e, n4);

	e = impl.createEdge(null);
	impl.setEdgeHead(e, n3);
	impl.setEdgeTail(e, n5);

	e = impl.createEdge(null);
	impl.setEdgeHead(e, n3);
	impl.setEdgeTail(e, n6);
	
        return graph;
    }

    /** Construct the Ptolemy system */
    public void constructPtolemyModel() {
        try {
            // Instantiate Actors
	    _contentionActor = new Controller( _toplevel, "controller" );
	    _alarmActor = new ContentionAlarm( _toplevel, "alarm" );
            _memoryActor = new Memory( _toplevel, "memory" );
	    _processActor1 = new Processor( _toplevel, "proc1", 1 );
	    _processActor2 = new Processor( _toplevel, "proc2", 2 );
	    _processActor3 = new Processor( _toplevel, "proc3", 3 );

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
	    inReqs = (TypedIORelation)_toplevel.connect(reqIn, p1_ReqOut );
	    inReqs = (TypedIORelation)_toplevel.connect(reqIn, p2_ReqOut );
	    inReqs = (TypedIORelation)_toplevel.connect(reqIn, p3_ReqOut );

	    outContends = (TypedIORelation)_toplevel.connect(contendOut,
	            _alarmIn );
            inContends = (TypedIORelation)_toplevel.connect(contendIn,
	            _alarmOut );

            outReqs = (TypedIORelation)_toplevel.connect( reqOut, p1_ReqIn );
	    outReqs = (TypedIORelation)_toplevel.connect( reqOut, p2_ReqIn );
	    outReqs = (TypedIORelation)_toplevel.connect( reqOut, p3_ReqIn );

	    reads = (TypedIORelation)_toplevel.connect( memOut, p1_MemIn );
	    reads = (TypedIORelation)_toplevel.connect( memOut, p2_MemIn );
	    reads = (TypedIORelation)_toplevel.connect( memOut, p3_MemIn );

	    writes = (TypedIORelation)_toplevel.connect( memIn, p1_MemOut );
	    writes = (TypedIORelation)_toplevel.connect( memIn, p2_MemOut );
	    writes = (TypedIORelation)_toplevel.connect( memIn, p3_MemOut );

            System.out.println("Connections are complete.");

        } catch (Exception e) {
	    e.printStackTrace();
            throw new RuntimeException(e.toString());
        }
    }

    /** Construct the graph widget with the default constructor (giving
     *  it an empty graph).
     */
    public void displayGraph(JGraph g, Graph graph) {
	_divaPanel.add( g, BorderLayout.NORTH );
	g.setPreferredSize( new Dimension(600, 400) );

    	// display the graph.
	final GraphController gc = new BusContentionGraphController();
	final GraphPane gp = new GraphPane(gc);
	g.setCanvasPane(gp);
	gc.setGraph(graph);

	doLayout(graph, gp);
    }

    /** Layout the graph again.
     */
    public void doLayout(Graph graph, GraphPane gp) {
        // Do the layout
	try {
	    final Graph layoutGraph = graph;
	    final GraphController gc = gp.getGraphController();
	    final GraphPane pane = gp;
	    SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    // Layout is a bit stupid
		    LevelLayout staticLayout = new LevelLayout();
		    staticLayout.setOrientation(LevelLayout.HORIZONTAL);
		    LayoutTarget target = new BasicLayoutTarget(gc);
		    staticLayout.layout(target, layoutGraph);
		    pane.repaint();
		}
	    });
	} catch (Exception e) {
	    System.out.println(e);
	}
    }

    /** Override the baseclass start method so that the model
     *  does not immediately begin executing as soon as the
     *  the applet page is displayed. Execution begins once
     *  the "Go" button is depressed.
     */
    public void start() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** In addition to creating the buttons provided by the base class,
     *  if the number of iterations has not been specified, then create
     *  a dialog box for that number to be entered.  The panel containing
     *  the buttons and the entry box is returned.
     *  @param numButtons The number of buttons to create.
     */
    protected JPanel _createRunControls(int numButtons) {
        JPanel controlPanel = super._createRunControls(numButtons);

        if (numButtons > 2) {
            JButton layout = new JButton("Layout");
            controlPanel.add(layout);
            layout.addActionListener(new LayoutListener());
        }

        return controlPanel;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The Actors
    Controller _contentionActor;
    ContentionAlarm _alarmActor;
    Memory _memoryActor;
    Processor _processActor1;
    Processor _processActor2;
    Processor _processActor3;

    // The mapping from Ptolemy actors to graph nodes
    private HashMap _nodeMap = new HashMap();

    // The JGraph where we display stuff
    private JGraph _jgraph = new JGraph();

    // The Diva panel where we display stuff
    private JPanel _divaPanel;

    // The Diva graph
    private Graph _graph;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    ///////////////////////////////////////////////////////////////////
    //// LayoutListener

    private class LayoutListener implements ActionListener {
	public void actionPerformed(ActionEvent evt) {
	    final GraphPane gp = (GraphPane)_jgraph.getCanvasPane();
	    final Graph g = _graph;
	    doLayout(g, gp);
	}
    }

    ///////////////////////////////////////////////////////////////////
    //// BusContentionGraphController
    public class BusContentionGraphController extends GraphController {
	private SelectionDragger _selectionDragger;
	private NodeController _nodeController;
	private EdgeController _edgeController;
	
	/**
	 * Create a new basic controller with default 
	 * node and edge interactors.
	 */
	public BusContentionGraphController () {
	    // The interactors attached to nodes and edges
	    _nodeController = new NodeController(this);
	    _edgeController = new EdgeController(this);
	}
	
	public void clearEdge(Edge edge) {
	    _edgeController.clearEdge(edge);
	}
	
	public void clearNode(Node node) {
	    _nodeController.clearNode(node);
	}
	
	public Figure drawEdge(Edge edge) {
	    return _edgeController.drawEdge(edge);
	}
	
	public Figure drawNode(Node node) {
	    return _nodeController.drawNode(node);
	}
	
	/**
	 * Initialize all interaction on the graph pane. This method
	 * is called by the setGraphPane() method of the superclass.
	 * This initialization cannot be done in the constructor because
	 * the controller does not yet have a reference to its pane
	 * at that time.
	 */
	protected void initializeInteraction () {
	    GraphPane pane = getGraphPane();
	    
	    // Create and set up the selection dragger
	    _selectionDragger = new SelectionDragger(pane);
	    _selectionDragger.addSelectionInteractor(
		(SelectionInteractor)_edgeController.getEdgeInteractor());
	    _selectionDragger.addSelectionInteractor(
                (SelectionInteractor)_nodeController.getNodeInteractor());

	    _nodeController.setNodeRenderer(new ThreadRenderer());
	    _edgeController.setEdgeRenderer(new LocalEdgeRenderer());
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
                node.getVisualObject();
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
            catch (Exception e) {
		e.printStackTrace();
	    }
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
}
