/* A CSP model of hardware bus contention.

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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.csp.demo.BusContention;

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
import diva.graph.NodeRenderer;
import diva.graph.basic.BasicGraphController;
import diva.graph.basic.BasicGraphModel;
import diva.graph.basic.BasicLayoutTarget;
import diva.graph.layout.LayoutTarget;
import diva.graph.layout.LevelLayout;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.gui.PtolemyApplet;
import ptolemy.domains.csp.kernel.CSPDirector;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.DebugEvent;
import ptolemy.kernel.util.DebugListener;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.HashMap;

import javax.swing.JPanel;
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
 *  @since Ptolemy II 0.3
 */
public class BusContentionApplet extends PtolemyApplet {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create a model.
     *  @param workspace The workspace in which to create the model.
     *  @return A model.
     *  @exception Exception If something goes wrong.  This is a broad
     *   exception to allow derived classes wide lattitude as to which
     *   exception to throw.
     */
    protected NamedObj _createModel(Workspace workspace)
            throws Exception {
        TypedCompositeActor toplevel = new TypedCompositeActor(workspace);
        _toplevel = toplevel;
        toplevel.setName("BusContention");
        new CSPDirector(toplevel, "CSPDirector");

        // Instantiate Actors
        _contentionActor = new Controller( toplevel, "controller" );
        _alarmActor = new ContentionAlarm( toplevel, "alarm" );
        _memoryActor = new Memory( toplevel, "memory" );
        _processActor1 = new Processor( toplevel, "proc1", 1 );
        _processActor2 = new Processor( toplevel, "proc2", 2 );
        _processActor3 = new Processor( toplevel, "proc3", 3 );

        // Set up connections
        toplevel.connect(
                _contentionActor.requestInput,
                _processActor1.requestOutput );
        toplevel.connect(
                _contentionActor.requestInput,
                _processActor2.requestOutput );
        toplevel.connect(
                _contentionActor.requestInput,
                _processActor3.requestOutput );
        toplevel.connect(
                _contentionActor.contendOutput,
                _alarmActor.input );
        toplevel.connect(
                _contentionActor.contendInput,
                _alarmActor.output );
        toplevel.connect(
                _contentionActor.requestOutput,
                _processActor1.requestInput );
        toplevel.connect(
                _contentionActor.requestOutput,
                _processActor2.requestInput );
        toplevel.connect(
                _contentionActor.requestOutput,
                _processActor3.requestInput );
        toplevel.connect(
                _memoryActor.output,
                _processActor1.memoryInput );
        toplevel.connect(
                _memoryActor.output,
                _processActor2.memoryInput );
        toplevel.connect(
                _memoryActor.output,
                _processActor3.memoryInput );
        toplevel.connect(
                _memoryActor.input,
                _processActor1.memoryOutput );
        toplevel.connect(
                _memoryActor.input,
                _processActor2.memoryOutput );
        toplevel.connect(
                _memoryActor.input,
                _processActor3.memoryOutput );
        return toplevel;
    }

    /** Create an animation pane.
     */
    protected void _createView() {
        super._createView();

        _divaPanel = new JPanel( new BorderLayout() );
        _divaPanel.setBorder(new TitledBorder(
                new LineBorder(Color.black), "Animation"));
        _divaPanel.setBackground(getBackground());
        _divaPanel.setPreferredSize( new Dimension(500, 450) );
        _divaPanel.setBackground(getBackground());
        getContentPane().add( _divaPanel, BorderLayout.SOUTH );

        _graph = _constructGraph();

        // display the graph.
        final GraphController gc = new BusContentionGraphController();

        final GraphPane gp = new GraphPane(gc, _graph);
        _jgraph = new JGraph(gp);
        _jgraph.repaint();

        // Adding it to the center so that it fills the containing panel.
        _divaPanel.add(_jgraph, BorderLayout.CENTER );

        _jgraph.setBackground(getBackground());

        StateListener listener =
            new StateListener((GraphPane)_jgraph.getCanvasPane());
        _processActor1.addDebugListener(listener);
        _processActor2.addDebugListener(listener);
        _processActor3.addDebugListener(listener);
    }

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
    ////                         private methods                   ////

    /** Construct the graph representing the topology.
     *  This is sort of bogus because it's totally hard-wired,
     *  but it will do for now...
     */
    private BasicGraphModel _constructGraph() {
        BasicGraphModel model = new BasicGraphModel();
        Object root = model.getRoot();

        // Nodes, with user object set to the actor
        Object n1 = model.createNode(_contentionActor);
        Object n2 = model.createNode(_alarmActor);
        Object n3 = model.createNode(_memoryActor);
        Object n4 = model.createNode(_processActor1);
        Object n5 = model.createNode(_processActor2);
        Object n6 = model.createNode(_processActor3);

        model.addNode(this, n1, root);
        model.addNode(this, n2, root);
        model.addNode(this, n3, root);
        model.addNode(this, n4, root);
        model.addNode(this, n5, root);
        model.addNode(this, n6, root);

        _nodeMap.put(_contentionActor, n1);
        _nodeMap.put(_alarmActor, n2);
        _nodeMap.put(_memoryActor, n3);
        _nodeMap.put(_processActor1, n4);
        _nodeMap.put(_processActor2, n5);
        _nodeMap.put(_processActor3, n6);

        // Edges
        Object e;

        e = model.createEdge(null);
        model.setEdgeHead(this, e, n1);
        model.setEdgeTail(this, e, n2);

        e = model.createEdge(null);
        model.setEdgeHead(this, e, n1);
        model.setEdgeTail(this, e, n4);

        e = model.createEdge(null);
        model.setEdgeHead(this, e, n1);
        model.setEdgeTail(this, e, n5);

        e = model.createEdge(null);
        model.setEdgeHead(this, e, n1);
        model.setEdgeTail(this, e, n6);

        e = model.createEdge(null);
        model.setEdgeHead(this, e, n3);
        model.setEdgeTail(this, e, n4);

        e = model.createEdge(null);
        model.setEdgeHead(this, e, n3);
        model.setEdgeTail(this, e, n5);

        e = model.createEdge(null);
        model.setEdgeHead(this, e, n3);
        model.setEdgeTail(this, e, n6);

        return model;
    }

    /** Layout the graph again.
     */
    private void _doLayout(GraphModel graph, GraphPane gp) {
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
    Controller _contentionActor;
    ContentionAlarm _alarmActor;
    Memory _memoryActor;
    Processor _processActor1;
    Processor _processActor2;
    Processor _processActor3;

    // The mapping from Ptolemy actors to graph nodes
    private HashMap _nodeMap = new HashMap();

    // The JGraph where we display stuff
    private JGraph _jgraph;

    // The Diva panel where we display stuff
    private JPanel _divaPanel;

    // The Diva graph
    private BasicGraphModel _graph;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    ///////////////////////////////////////////////////////////////////
    //// LayoutListener

    // private class LayoutListener implements ActionListener {
    //    public void actionPerformed(ActionEvent evt) {
    //        final GraphPane gp = (GraphPane)_jgraph.getCanvasPane();
    //        final GraphModel g = _graph;
    //        _doLayout(g, gp);
    //    }
    //}

    ///////////////////////////////////////////////////////////////////
    //// BusContentionGraphController
    public class BusContentionGraphController extends BasicGraphController {
        private SelectionDragger _selectionDragger;
        /**
         * Create a new basic controller with default
         * node and edge interactors.
         */
        public BusContentionGraphController() {
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
        private double _size = 50;

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
            LabelWrapper w = new LabelWrapper(f, label);
            w.setAnchor(SwingConstants.SOUTH);
            w.getLabel().setAnchor(SwingConstants.NORTH);
            return w;
        }
    }
}
