/* A DDE application illustrating localized Zeno conditions.

 Copyright (c) 1999-2003 The Regents of the University of California.
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

@ProposedRating Yellow (davisj@eecs.berkeley.edu)
@AcceptedRating Red (davisj@eecs.berkeley.edu)
*/

package ptolemy.domains.dde.demo.LocalZeno;

import diva.canvas.Figure;
import diva.canvas.Site;
import diva.canvas.connector.Arrowhead;
import diva.canvas.connector.Connector;
import diva.canvas.connector.StraightConnector;
import diva.canvas.interactor.SelectionDragger;
import diva.canvas.interactor.SelectionInteractor;
import diva.canvas.toolbox.BasicEllipse;
import diva.canvas.toolbox.BasicFigure;
import diva.canvas.toolbox.BasicRectangle;
import diva.canvas.toolbox.LabelWrapper;
import diva.graph.BasicEdgeController;
import diva.graph.BasicNodeController;
import diva.graph.EdgeRenderer;
import diva.graph.GraphController;
import diva.graph.GraphModel;
import diva.graph.GraphPane;
import diva.graph.JGraph;
import diva.graph.MutableGraphModel;
import diva.graph.NodeRenderer;
import diva.graph.basic.BasicGraphController;
import diva.graph.basic.BasicGraphModel;
import diva.graph.basic.BasicLayoutTarget;
import diva.graph.layout.LayoutTarget;
import diva.graph.layout.LevelLayout;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.gui.PtolemyApplet;
import ptolemy.actor.lib.gui.TimedPlotter;
import ptolemy.data.DoubleToken;
import ptolemy.domains.dde.kernel.DDEDirector;
import ptolemy.domains.dde.lib.TimeAdvance;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.DebugEvent;
import ptolemy.kernel.util.DebugListener;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

//////////////////////////////////////////////////////////////////////////
//// LocalZenoApplet

/**
 *  A DDE application illustrating localized Zeno conditions.
 *
 *  @author John S. Davis II (davisj@eecs.berkeley.edu)
 *  @author Michael Shilman  (michaels@eecs.berkeley.edu)
 *  @version $Id$
 *  @since Ptolemy II 0.3
 */
public class LocalZenoApplet extends PtolemyApplet {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the baseclass start method so that the model
     *  does not immediately begin executing as soon as the
     *  the applet page is displayed. Execution begins once
     *  the "Go" button is depressed. Layout the graph visualization,
     *  since this can't be done in the init method, because the graph
     *  hasn't yet been displayed.
     */
    public void start() {
        _doLayout(_graph, _jgraph.getGraphPane());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Construct the graph representing the topology.
     *  This is sort of bogus because it's totally hard-wired,
     *  but it will do for now...
     */
    protected MutableGraphModel _constructDivaGraph() {
        BasicGraphModel model = new BasicGraphModel();
        Object root = model.getRoot();

        // Objects, with user object set to the actor
        Object n1 = model.createNode(_clock);

        Object n2 = model.createNode(_join1);
        Object n3 = model.createNode(_fork1);
        Object n4 = model.createNode(_fBack1);
        Object n5 = model.createNode(_rcvr1);

        Object n6 = model.createNode(_join2);
        Object n7 = model.createNode(_fork2);
        Object n8 = model.createNode(_fBack2);
        Object n9 = model.createNode(_rcvr2);

        model.addNode(this, n1, root);
        model.addNode(this, n2, root);
        model.addNode(this, n3, root);
        model.addNode(this, n4, root);
        model.addNode(this, n5, root);
        model.addNode(this, n6, root);
        model.addNode(this, n7, root);
        model.addNode(this, n8, root);
        model.addNode(this, n9, root);

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
        Object e;

        e = model.createEdge(null);
        model.setEdgeTail(this, e, n1);
        model.setEdgeHead(this, e, n2);

        e = model.createEdge(null);
        model.setEdgeTail(this, e, n2);
        model.setEdgeHead(this, e, n3);

        e = model.createEdge(null);
        model.setEdgeTail(this, e, n4);
        model.setEdgeHead(this, e, n2);

        e = model.createEdge(null);
        model.setEdgeTail(this, e, n3);
        model.setEdgeHead(this, e, n4);

        e = model.createEdge(null);
        model.setEdgeTail(this, e, n3);
        model.setEdgeHead(this, e, n5);

        e = model.createEdge(null);
        model.setEdgeTail(this, e, n1);
        model.setEdgeHead(this, e, n6);

        e = model.createEdge(null);
        model.setEdgeTail(this, e, n7);
        model.setEdgeHead(this, e, n8);

        e = model.createEdge(null);
        model.setEdgeTail(this, e, n6);
        model.setEdgeHead(this, e, n7);

        e = model.createEdge(null);
        model.setEdgeTail(this, e, n7);
        model.setEdgeHead(this, e, n9);

        e = model.createEdge(null);
        model.setEdgeTail(this, e, n8);
        model.setEdgeHead(this, e, n6);

        return model;
    }

    /** Construct the Ptolemy model; instantiate all
     *  actors and make connections.
     */
    protected NamedObj _createModel(Workspace workspace)
            throws Exception {

        TypedCompositeActor toplevel = new TypedCompositeActor(workspace);
        _toplevel = toplevel;
        DDEDirector director = new DDEDirector(toplevel, "DDE Director");
        director.stopTime.setExpression("90.0");

        // Instantiate the Actors
        _clock = new ListenClock( toplevel, "Clock" );
        _clock.values.setExpression( "{1, 1, 1}" );
        _clock.period.setToken( new DoubleToken(20.0) );
        _clock.offsets.setExpression( "{5.0, 10.0, 15.0}" );
        _clock.stopTime.setToken( new DoubleToken(0.0) );

        _join1 = new ListenWire( toplevel, "UpperJoin" );
        _fork1 = new ListenFork( toplevel, "UpperFork" );
        _fBack1 = new ListenFeedBackDelay( toplevel, "UpperFeedBack" );
        _join2 = new ListenWire( toplevel, "LowerJoin" );
        _fork2 = new ListenFork( toplevel, "LowerFork" );
        _fBack2 = new ZenoDelay( toplevel, "LowerFeedBack" );

        _rcvr1 = new ListenSink( toplevel, "UpperRcvr" );
        _rcvr2 = new ListenSink( toplevel, "LowerRcvr" );

        _upperTime = new TimeAdvance( toplevel, "upperTime" );
        _upperPlotter = new TimedPlotter( toplevel, "upperPlotter" );

        _lowerTime = new TimeAdvance( toplevel, "lowerTime" );
        _lowerPlotter = new TimedPlotter( toplevel, "lowerPlotter" );

        _fBack1.delay.setToken(new DoubleToken(4.5));
        _fBack2.delay.setToken(new DoubleToken(4.5));

        // Set up ports, relations and connections
        Relation clkRelation =
            toplevel.connect( _clock.output, _join1.input );
        _join2.input.link( clkRelation );

        toplevel.connect( _join1.output, _fork1.input );
        toplevel.connect( _fork1.output1, _rcvr1.input );
        toplevel.connect( _fork1.output2, _fBack1.input );
        toplevel.connect( _fBack1.output, _join1.input );

        toplevel.connect( _join2.output, _fork2.input );
        toplevel.connect( _fork2.output1, _rcvr2.input );
        toplevel.connect( _fork2.output2, _fBack2.input );
        toplevel.connect( _fBack2.output, _join2.input );

        toplevel.connect( _fork1.output1, _upperTime.input );
        toplevel.connect( _upperTime.output, _upperPlotter.input );

        toplevel.connect( _fork2.output1, _lowerTime.input );
        toplevel.connect( _lowerTime.output, _lowerPlotter.input );

        return toplevel;
    }

    /** Create a custom view to control execution of the model and display
     *  its results.  Derived classes may override this to do something
     *  different.
     */
    protected void _createView() {

        getContentPane().setLayout(
                new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // Create control panels.
        super._createView();

        // Panel for controls and plotter
        JPanel topPanel = new JPanel();
        topPanel.setSize( new Dimension(600, 200) );
        topPanel.setBackground(null);

        _plotPanel = new JPanel();
        _plotPanel.setSize( new Dimension(600, 200) );
        _plotPanel.setBackground(getBackground());
        topPanel.add( _plotPanel );

        _upperPlotter.place( _plotPanel );
        _upperPlotter.plot.setTitle("Upper Branch");
        _upperPlotter.plot.setXRange(0.0, 90.0);
        _upperPlotter.plot.setYRange(-1.0, 1.0);
        _upperPlotter.plot.setSize(200, 150);
        _upperPlotter.plot.addLegend(0, "Time");

        _lowerPlotter.place( _plotPanel );
        _lowerPlotter.plot.setTitle("Lower Branch");
        _lowerPlotter.plot.setXRange(0.0, 90.0);
        _lowerPlotter.plot.setYRange(-1.0, 1.0);
        _lowerPlotter.plot.setSize(200, 150);
        _lowerPlotter.plot.addLegend(0, "Time");

        getContentPane().add( topPanel );

        _divaPanel = new JPanel( new BorderLayout() );
        _divaPanel.setSize( new Dimension(600, 400) );
        _divaPanel.setBackground( null );
        getContentPane().add( _divaPanel );

        _graph = _constructDivaGraph();
        // display the graph.
        final GraphController gc = new LocalZenoGraphController();
        final GraphPane gp = new GraphPane(gc, _graph);
        _jgraph = new JGraph(gp);
        _divaPanel.add(_jgraph );

        StateListener listener =
            new StateListener((GraphPane)_jgraph.getCanvasPane());
        _join1.addDebugListener(listener);
        _join2.addDebugListener(listener);
        _fork1.addDebugListener(listener);
        _fork2.addDebugListener(listener);
        _fBack1.addDebugListener(listener);
        _fBack2.addDebugListener(listener);
        _rcvr1.addDebugListener(listener);
        _rcvr2.addDebugListener(listener);
        _clock.addDebugListener(listener);
    }

    /** Layout the graph again.
     */
    protected void _doLayout(GraphModel graph, GraphPane gp) {
        // Do the layout
        try {
            final GraphModel layoutGraph = graph;
            final GraphController gc = gp.getGraphController();
            final GraphPane pane = gp;
            SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        // Layout is a bit stupid
                        LayoutTarget target = new BasicLayoutTarget(gc);
                        LevelLayout staticLayout = new LevelLayout(target);
                        staticLayout.setOrientation(LevelLayout.HORIZONTAL);
                        staticLayout.layout(layoutGraph.getRoot());
                        pane.repaint();
                    }
                });
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The Actors
    private ListenClock _clock;
    private ListenWire _join1;
    private ListenFork _fork1;
    private ListenFeedBackDelay _fBack1;
    private ListenSink _rcvr1;
    private ListenWire _join2;
    private ListenFork _fork2;
    private ZenoDelay _fBack2;
    private ListenSink _rcvr2;
    private TimeAdvance _upperTime;
    private TimeAdvance _lowerTime;
    private TimedPlotter _upperPlotter;
    private TimedPlotter _lowerPlotter;

    // Plot Panel
    private JPanel _plotPanel;

    // The mapping from Ptolemy actors to Diva graph nodes
    private HashMap _nodeMap = new HashMap();

    // The Diva JGraph where we display stuff
    private JGraph _jgraph;

    // The Diva panel where we display stuff
    private JPanel _divaPanel;

    // The Diva graph
    private MutableGraphModel _graph;



      

    ///////////////////////////////////////////////////////////////////
    //// LocalZenoGraphController
    public class LocalZenoGraphController extends BasicGraphController {
        private SelectionDragger _selectionDragger;
        /**
         * Create a new basic controller with default
         * node and edge interactors.
         */
        public LocalZenoGraphController() {
            // The interactors attached to nodes and edges
            setNodeController(new BasicNodeController(this));
            setEdgeController(new BasicEdgeController(this));
            getNodeController().setNodeRenderer(new ThreadRenderer(this));
            getEdgeController().setEdgeRenderer(new LocalEdgeRenderer());
        }

        /**
         * Initialize all interaction on the graph pane. This method
         * is called by the setGraphPane() method of the superclass.
         * This initialization cannot be done in the constructor because
         * the controller does not yet have a reference to its pane
         * at that time.
         */
        protected void initializeInteraction() {
            GraphPane pane = getGraphPane();

            // Create and set up the selection dragger
            _selectionDragger = new SelectionDragger(pane);
            _selectionDragger.addSelectionModel(getSelectionModel());
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
        public Connector render(Object edge, Site tailSite, Site headSite) {
            StraightConnector c = new StraightConnector(tailSite, headSite);

            // Create an arrow at the head
            Arrowhead headArrow = new Arrowhead(
                    headSite.getX(), headSite.getY(),
                    headSite.getNormal());
            c.setHeadEnd(headArrow);
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
    public class StateListener implements DebugListener {

        // The Pane
        GraphPane _graphPane;

        /** Create a listener on the given graph pane
         */
        public StateListener(GraphPane pane) {
            _graphPane = pane;
        }

        /** Ignore messages.
         */
        public void message(String message) {
        }

        /** React to the given event.
         */
        public void event(DebugEvent debugEvent) {
            // only trap ExecEvents.
            if (!(debugEvent instanceof ExecEvent)) return;
            ExecEvent event = (ExecEvent) debugEvent;
            final ExecEvent.ExecEventType state = event.getState();
            NamedObj actor = event.getSource();

            // Get the corresponding graph node and its figure
            Object node = (Object) _nodeMap.get(actor);
            LabelWrapper wrapper = (LabelWrapper)
                _graphPane.getGraphController().getFigure(node);
            final BasicFigure figure = (BasicFigure)
                wrapper.getChild();

            // Color the graph
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            if (state == ExecEvent.WAITING)
                                figure.setFillPaint(Color.yellow);
                            else if (state == ExecEvent.ACCESSING)
                                figure.setFillPaint(Color.green);
                            else if (state == ExecEvent.BLOCKED)
                                figure.setFillPaint(Color.red);
                            else
                                System.err.println("Unknown state: " + state);
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
        private double _size = 40;

        /** The graph controller
         */
        private GraphController _controller;

        public ThreadRenderer(GraphController controller) {
            _controller = controller;
        }

        /**
         * Return the rendered visual representation of this node.
         */
        public Figure render(Object n) {
            ComponentEntity actor = (ComponentEntity)
                _controller.getGraphModel().getSemanticObject(n);

            boolean isEllipse =
                actor instanceof ListenWire
                || actor instanceof ListenFork
                || actor instanceof ListenClock
                || actor instanceof ListenSink
                || actor instanceof ListenFeedBackDelay;


            BasicFigure f;
            if (isEllipse) {
                f = new BasicEllipse(0, 0, _size, _size);
                f.setFillPaint(Color.blue);
            } else {
                f = new BasicRectangle(0, 0, _size, _size);
                f.setFillPaint(Color.pink);
            }
            String label = actor.getName();
            LabelWrapper w = new LabelWrapper(f, label);
            w.setAnchor(SwingConstants.SOUTH);
            w.getLabel().setAnchor(SwingConstants.NORTH);
            return w;
        }
    }
}
