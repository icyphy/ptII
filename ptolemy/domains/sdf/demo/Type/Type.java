/* An applet that demonstrates the Ptolemy II type system.

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
@ProposedRating Red (yuhong@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.demo.Type;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Font;
import java.awt.event.*;
import java.util.Hashtable;

import diva.graph.*;
import diva.graph.model.*;
import diva.graph.layout.*;
import diva.canvas.*;
import diva.canvas.connector.*;
import diva.canvas.interactor.*;
import diva.canvas.toolbox.*;
import diva.surfaces.trace.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.actor.*;
import ptolemy.actor.lib.*;
import ptolemy.actor.gui.*;
import ptolemy.gui.Query;
import ptolemy.gui.QueryListener;
import ptolemy.domains.sdf.gui.SDFApplet;
import ptolemy.plot.*;

//////////////////////////////////////////////////////////////////////////
//// Type
/**
An applet that demonstrates the Ptolemy II type system.  This applet
connects two ramps to two inputs of an expression actor, and connects
the output of the expression actor to either a display or a plotter.
It displays a Diva animation of the type resolution process.

@author Yuhong Xiong, John Reekie
@version $Id$
*/

public class Type extends SDFApplet implements ChangeListener {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in the selection of plotter or display.
     *  @param event The tabbed pane event.
     */
    public void stateChanged(ChangeEvent event) {
        try {
            int display = _plotDisplay.getSelectedIndex();

            if (display == 0 && _previousWasDisplay) { // plotter selected
                _previousWasDisplay = false;
                _plotter.plot.setGrid(true);
                _plotter.plot.setXRange(0.0, 10.0);
                _plotter.plot.setYRange(0.0, 20.0);
                _plotter.plot.setConnected(false);
                _plotter.plot.setImpulses(true);
                _plotter.plot.setMarksStyle("dots");

                _expr.output.unlinkAll();
                _display.setContainer(null);
                _plotter.setContainer(_toplevel);

                _toplevel.connect(_expr.output, _plotter.input);
                _director.setScheduleValid(false);

            } else if (display == 1 && !_previousWasDisplay) {
                _previousWasDisplay = true;
                _expr.output.unlinkAll();
                _plotter.setContainer(null);
                _display.setContainer(_toplevel);
                _toplevel.connect(_expr.output, _display.input);
                _director.setScheduleValid(false);
            } else {
                return;
            }
        } catch (Exception ex) {
            report("setDisplay failed:", ex);
        }
    }

    /** After invoking super.init(), create and connect the actors.
     *  Also, create the on-screen Diva displays.
     */
    public void init() {
        super.init();
        try {
	    // Panel hierarchy:
	    // Applet
	    //     _ioPanel
	    //         controlPanel
	    //		   runControlPanel (go Button)
	    //		   paramPanel (parameters,
	    //			       plotter/display selection)
	    //	       _plotDisplay panel (a TabbedPane)
	    //     _schemPanel (schematics with type annotation)


            // The control panel has run control and parameter control,
            JPanel controlPanel = new JPanel();
            controlPanel.setBackground(getBackground());
            controlPanel.setAlignmentY(0);
            controlPanel.setLayout(new BorderLayout());
            controlPanel.add(_query, BorderLayout.CENTER);
            controlPanel.add(_createRunControls(1), BorderLayout.SOUTH);

            _query.addLine("ramp1init", "Ramp1 Initial Value", "0");
            _query.addLine("ramp1step", "Ramp1 Step Size", "1");
            _query.addLine("ramp2init", "Ramp2 Init Value", "0");
            _query.addLine("ramp2step", "Ramp2 Step Size", "1");
            _query.addLine("expr", "Expression", "input1 + input2");
            _query.setBackground(getBackground());

            // The IO panel has the control panel and the output display.
            _ioPanel.setLayout(new BoxLayout(_ioPanel, BoxLayout.X_AXIS));
            _ioPanel.setOpaque(true);
            _ioPanel.setBackground(getBackground());
            _ioPanel.add(controlPanel);

            // Build the PtII model, placing a plotter in the IO panel.
	    _buildModel();

            // Visualization panel contains the type lattice and
            // animation of type resolution progress (trace model).
            JPanel visPanel = new JPanel();
            visPanel.setLayout(new BorderLayout());
            visPanel.setBackground(getBackground());
            _jgraph.setBackground(getBackground());
            // FIXME: title borders don't work in diva...
            // _jgraph.setBorder(new TitledBorder(new LineBorder(Color.black),
            //        "Type Lattice"));
            _jgraph.setBorder(new LineBorder(Color.black));
            visPanel.add(_jgraph, BorderLayout.WEST);
            _jgraph.setPreferredSize(new Dimension(400, 290));

            // Place items in the top-level.
            getContentPane().add(_ioPanel, BorderLayout.NORTH);
	    getContentPane().add(_schemPanel, BorderLayout.CENTER);
            getContentPane().add(visPanel, BorderLayout.SOUTH);

            getContentPane().setBackground(getBackground());

            // Define the tabbed pane for the plotter and display.
            _plotDisplay.setAlignmentY(0);
            _plotDisplay.setPreferredSize(new Dimension(400, 250));
            _plotDisplay.addTab("Plotter", null, _plotPanel,
                    "Display is plotter");
            _plotDisplay.addTab("Text Display", null, _printPanel,
                    "Display text");
            _plotDisplay.addChangeListener(this);
            _plotPanel.setBackground(getBackground());
            _plotPanel.setBorder(new LineBorder(Color.black));
            _printPanel.setBackground(getBackground());
            _printPanel.setBorder(new LineBorder(Color.black));
            _ioPanel.add(_plotDisplay);

            // Construct the Ptolemy type lattice model
            final Graph graph = _constructLattice();

            // Construct a new trace model
            TraceModel traceModel = new TraceModel();

            // Display the trace
            _traceCanvas = _displayTrace(traceModel);
            // FIXME: title borders don't work in diva...
            // _traceCanvas.setBorder(new TitledBorder(
            //       new LineBorder(Color.black),
            //       "Type Resolution"));
            _traceCanvas.setBorder(new LineBorder(Color.black));
            _traceCanvas.setPreferredSize(new Dimension(400, 290));
            visPanel.add(_traceCanvas, BorderLayout.EAST);

	    _addListeners();

            // Display the type lattice
            _displayGraph(_jgraph, graph);

        } catch (Exception ex) {
            report("Setup failed:", ex);
	}
    }

    /** Override the base class to avoid executing the model automatically
     *  when the applet starts.  This way, the initial types (NaT) are
     *  displayed in the Diva animation.
     */
    public void start() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Execute the system.  This overrides the base class to read the
     *  values in the query box first.
     */
    protected void _go() {
	try {
	    _ramp1.init.setExpression(_query.stringValue("ramp1init"));
	    _ramp1.step.setExpression(_query.stringValue("ramp1step"));
	    _ramp2.init.setExpression(_query.stringValue("ramp2init"));
	    _ramp2.step.setExpression(_query.stringValue("ramp2step"));

	    _expr.expression.setExpression(_query.stringValue("expr"));

            // Reinitialize the trace display
            _tracePane.getTraceView().clear();
            _tracePane.getTraceModel().clear();
            _initTraceModel(_tracePane.getTraceModel());
            _initTraceView();

            // Now set system "start" time
            _startTime = 0;
	    _counter = 0;

            super._go();
        } catch (Exception ex) {
            report("Go failed:", ex);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private void _addListeners()
	    throws NameDuplicationException, IllegalActionException {

	TypeListener typeListener = new MyTypeListener();
	_ramp1.output.addTypeListener(typeListener);
	_ramp2.output.addTypeListener(typeListener);
	_exprInput1.addTypeListener(typeListener);
	_exprInput2.addTypeListener(typeListener);
	_expr.output.addTypeListener(typeListener);
	_plotter.input.addTypeListener(typeListener);
	_display.input.addTypeListener(typeListener);
    }

    private void _buildModel()
	    throws NameDuplicationException, IllegalActionException {

        // Create the ramps
        _ramp1 = new Ramp(_toplevel, "ramp1");
	_ramp2 = new Ramp(_toplevel, "ramp2");

        // Create and configure expr
        _expr = new Expression(_toplevel, "expr");
        _exprInput1 = new TypedIOPort(_expr, "input1", true, false);
        _exprInput2 = new TypedIOPort(_expr, "input2", true, false);

        // Create and configure plotter
        _plotter = new SequencePlotter(_toplevel, "plot");

        _plotter.place(_plotPanel);
        _plotter.plot.setSize(390, 200);
        _plotter.plot.setBackground(getBackground());
        _plotter.plot.setGrid(true);
        _plotter.plot.setXRange(0.0, 10.0);
        _plotter.plot.setYRange(0.0, 20.0);
	_plotter.plot.setConnected(false);
	_plotter.plot.setImpulses(true);
	_plotter.plot.setMarksStyle("dots");

	// Create display. Can't use null in constructor, thus
	// set the container to null after construction.
        // NOTE: We used to place only the plotter OR the display,
        // but the Container.remove() method does not work in swing,
        // so we can't do that anymore.
	_display = new Display(_toplevel, "display");
        _display.place(_printPanel);
        _display.textArea.setRows(10);
        _display.textArea.setColumns(30);
	_display.setContainer(null);

        _toplevel.connect(_ramp1.output, _exprInput1);
        _toplevel.connect(_ramp2.output, _exprInput2);
        _toplevel.connect(_expr.output, _plotter.input);
    }

    // Construct the graph representing the Ptolemy type lattice
    private Graph _constructLattice() {
	GraphImpl impl = new BasicGraphImpl();
	Graph graph = impl.createGraph(null);

        // nodes, with user object set to the actor
        Node nNaT = impl.createNode(BaseType.NAT);
        Node nInt = impl.createNode(BaseType.INT);
        Node nDouble = impl.createNode(BaseType.DOUBLE);
        Node nComplex = impl.createNode(BaseType.COMPLEX);
        Node nString = impl.createNode(BaseType.STRING);
        Node nGeneral = impl.createNode(BaseType.GENERAL);
        Node nBoolean = impl.createNode(BaseType.BOOLEAN);
        Node nObject = impl.createNode(BaseType.OBJECT);
        Node nScalar = impl.createNode(BaseType.SCALAR);
        Node nLong = impl.createNode(BaseType.LONG);

        impl.addNode(nNaT, graph);
        impl.addNode(nInt, graph);
        impl.addNode(nDouble, graph);
        impl.addNode(nComplex, graph);
        impl.addNode(nString, graph);
        impl.addNode(nGeneral, graph);
        impl.addNode(nBoolean, graph);
        impl.addNode(nObject, graph);
        impl.addNode(nScalar, graph);
        impl.addNode(nLong, graph);

        /*
          nodeMap.put(a1, nNaT);
          nodeMap.put(a2, nInt);
          nodeMap.put(a3, nDouble);
          nodeMap.put(a4, nComplex);
          nodeMap.put(a5, nString);
          nodeMap.put(a6, nGeneral);
          nodeMap.put(a7, nBoolean);
          nodeMap.put(a8, nObject);
        */

        // Edges
        Edge e;

	e = impl.createEdge(null);
	impl.setEdgeTail(e, nObject);
	impl.setEdgeHead(e, nNaT);

        e = impl.createEdge(null);
	impl.setEdgeTail(e, nGeneral);
	impl.setEdgeHead(e, nObject);

	e = impl.createEdge(null);
	impl.setEdgeTail(e, nGeneral);
	impl.setEdgeHead(e, nString);

	e = impl.createEdge(null);
	impl.setEdgeTail(e, nString);
	impl.setEdgeHead(e, nBoolean);

	e = impl.createEdge(null);
	impl.setEdgeTail(e, nBoolean);
	impl.setEdgeHead(e, nNaT);

	e = impl.createEdge(null);
	impl.setEdgeTail(e, nString);
	impl.setEdgeHead(e, nScalar);

        e = impl.createEdge(null);
	impl.setEdgeTail(e, nScalar);
	impl.setEdgeHead(e, nLong);

        e = impl.createEdge(null);
	impl.setEdgeTail(e, nLong);
	impl.setEdgeHead(e, nInt);

        e = impl.createEdge(null);
	impl.setEdgeTail(e, nInt);
	impl.setEdgeHead(e, nNaT);

        e = impl.createEdge(null);
	impl.setEdgeTail(e, nDouble);
	impl.setEdgeHead(e, nInt);

        e = impl.createEdge(null);
	impl.setEdgeTail(e, nComplex);
	impl.setEdgeHead(e, nDouble);

	e = impl.createEdge(null);
	impl.setEdgeTail(e, nScalar);
	impl.setEdgeHead(e, nComplex);

        return graph;
    }

    // Construct the graph widget to display the type lattice with
    // the default constructor (giving it an empty graph),
    // and then set the model once the window is showing.
    //
    private void _displayGraph(JGraph g, Graph graph) {

        // Make sure we have the right renderers and then
        // display the graph
        final GraphController gc = new TypeGraphController();
	final GraphPane gp = new GraphPane(gc);
	g.setCanvasPane(gp);
	gc.setGraph(graph);

        // Do the layout
	final Graph layoutGraph = graph;
	SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // Layout is a bit stupid
		LevelLayout staticLayout = new LevelLayout();
		staticLayout.setOrientation(LevelLayout.VERTICAL);
		LayoutTarget target = new BasicLayoutTarget(gc);
		staticLayout.layout(target, layoutGraph);
                gp.repaint();
            }
        });
    }

    // Initialize the trace model.
    //
    private void _initTraceModel(TraceModel model) {
        TraceModel.Trace t;

        t = new TraceModel.Trace();
        t.setUserObject("ramp1.output");
        model.addTrace(_ramp1.output, t);

        t = new TraceModel.Trace();
        t.setUserObject("ramp2.output");
        model.addTrace(_ramp2.output, t);

        t = new TraceModel.Trace();
        t.setUserObject("expr.input1");
        model.addTrace(_exprInput1, t);

        t = new TraceModel.Trace();
        t.setUserObject("expr.input2");
        model.addTrace(_exprInput2, t);

        t = new TraceModel.Trace();
        t.setUserObject("expr.output");
        model.addTrace(_expr.output, t);

        t = new TraceModel.Trace();
        t.setUserObject("plotter.input");
        model.addTrace(_plotter.input, t);

        t = new TraceModel.Trace();
        t.setUserObject("display.input");
        model.addTrace(_display.input, t);
    }

    // Initialize the trace view.
    //
    private void _initTraceView() {
        TraceView view = _tracePane.getTraceView();
        TraceModel model = _tracePane.getTraceModel();
        _currentElement = new TraceModel.Element[model.size()];

        for (int i = 0; i < model.size()-2; i++ ) {
            TraceModel.Trace trace = model.getTrace(i);
            TraceModel.Element element = new TraceModel.Element(0, 1, 7);
            element.closure = TraceModel.Element.OPEN_END;
            trace.add(element);
            _currentElement[i] = element;
            view.drawTrace(trace);
            view.drawTraceElement(element);
        }

        // Hack hack hack
        int i = 5;
        TraceModel.Trace trace = model.getTrace(i);
        TraceModel.Element element = new TraceModel.Element(0, 1, 8);
        element.closure = TraceModel.Element.OPEN_END;
        trace.add(element);
        _currentElement[i] = element;
        view.drawTrace(trace);
        view.drawTraceElement(element);

        i = 6;
        trace = model.getTrace(i);
        element = new TraceModel.Element(0, 1, 5);
        element.closure = TraceModel.Element.OPEN_END;
        trace.add(element);
        _currentElement[i] = element;
        view.drawTrace(trace);
        view.drawTraceElement(element);
    }

    // Construct the trace display in a JCanvas and return the JCanvas
    private JCanvas _displayTrace(TraceModel traceModel) {
        _tracePane = new TracePane();
        JCanvas traceWidget = new JCanvas(_tracePane);

        // Configure the view
        TraceView traceView = _tracePane.getTraceView();
        traceView.setTimeScale(25);

        traceView.setLayout(10, 10, 400, 20, 20);
        traceView.setTraceModel(traceModel);

        return traceWidget;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private Expression _expr;
    private Ramp _ramp1, _ramp2;
    private SequencePlotter _plotter;
    private Display _display;

    private Query _query = new Query();

    private String _ramp1Type = "NaT";
    private String _ramp2Type = "NaT";
    private String _exprIn1Type = "NaT";
    private String _exprIn2Type = "NaT";
    private String _exprOutType = "NaT";
    private String _plotterType = "Double";
    private String _displayType = "String";

    private JPanel _ioPanel = new JPanel();
    private JPanel _plotPanel = new JPanel();
    private JPanel _printPanel = new JPanel();
    private SchematicPanel _schemPanel = new SchematicPanel();

    // The JGraph where we display the type lattice.
    private JGraph _jgraph = new JGraph();

    // The pane displaying the trace
    private TracePane _tracePane;

    // The canvas displaying the trace
    private JCanvas _traceCanvas;

    // The start time for the trace
    private long _startTime = 0;
    private long _counter = 0;

    // The current element of each state;
    private TraceModel.Element _currentElement[];

    // Indication of the previous display;
    private boolean _previousWasDisplay = false;

    // Ports of expression actor.
    private TypedIOPort _exprInput1, _exprInput2;

    // Plot/Display
    private JTabbedPane _plotDisplay = new JTabbedPane();

    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////

    private class SchematicPanel extends JPanel {

        public SchematicPanel() {
            setOpaque(false);
            setBackground(getBackground());
            setPreferredSize(new Dimension(700, 200));
            // As usual, the above is ignored.  Try this...
            setSize(new Dimension(700, 200));
            // That is ignored too... Try this...
            setMaximumSize(new Dimension(700, 200));
            // Well, that is ignored too... give up controlling the size...
        }

    	public void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);

	    final int ACTOR_WIDTH = 90;
	    final int ACTOR_HEIGHT = 65;

	    final int RAMP1_X = 70;
	    final int RAMP1_Y = 30;
	    final int RAMP1_TYPE_X = RAMP1_X+ACTOR_WIDTH+8;
	    final int RAMP1_TYPE_Y = RAMP1_Y+ACTOR_HEIGHT/2-8;

	    final int RAMP2_X = 70;
	    final int RAMP2_Y = 160;
	    final int RAMP2_TYPE_X = RAMP2_X+ACTOR_WIDTH+8;
	    final int RAMP2_TYPE_Y = RAMP2_Y+ACTOR_HEIGHT/2+20;

	    final int EXPR_X = 360;
	    final int EXPR_Y = 90;
	    final int EXPR_IN_TYPE_X = EXPR_X-60;
	    final int EXPR_IN_TYPE_Y1 = EXPR_Y-2;
	    final int EXPR_IN_TYPE_Y2 = EXPR_Y+ACTOR_HEIGHT+12;
	    final int EXPR_OUT_TYPE_X = EXPR_X+ACTOR_WIDTH+8;
	    final int EXPR_OUT_TYPE_Y = EXPR_Y+ACTOR_HEIGHT/2-8;

	    final int PLOT_X = 650;
	    final int PLOT_Y = 90;
	    final int PLOT_TYPE_X = PLOT_X-70;
	    final int PLOT_TYPE_Y = PLOT_Y+ACTOR_HEIGHT/2-8;

	    final int ARC_WIDTH = 12;
	    final int ARC_HEIGHT = 12;

	    final int FILL_OFFSET = 1;

	    // draw actors
	    graphics.setColor(Color.black);
	    graphics.drawRoundRect(RAMP1_X, RAMP1_Y, ACTOR_WIDTH, ACTOR_HEIGHT,
                    ARC_WIDTH, ARC_HEIGHT);
	    graphics.drawRoundRect(RAMP2_X, RAMP2_Y, ACTOR_WIDTH, ACTOR_HEIGHT,
                    ARC_WIDTH, ARC_HEIGHT);
	    graphics.drawRoundRect(EXPR_X, EXPR_Y, ACTOR_WIDTH, ACTOR_HEIGHT,
                    ARC_WIDTH, ARC_HEIGHT);
	    graphics.drawRoundRect(PLOT_X, PLOT_Y, ACTOR_WIDTH, ACTOR_HEIGHT,
                    ARC_WIDTH, ARC_HEIGHT);

	    graphics.setColor(new Color(0.6F, 0.9F, 1.0F));
	    graphics.fillRoundRect(RAMP1_X+FILL_OFFSET, RAMP1_Y+FILL_OFFSET,
                    ACTOR_WIDTH-FILL_OFFSET, ACTOR_HEIGHT-FILL_OFFSET,
                    ARC_WIDTH, ARC_HEIGHT);
	    graphics.fillRoundRect(RAMP2_X+FILL_OFFSET, RAMP2_Y+FILL_OFFSET,
                    ACTOR_WIDTH-FILL_OFFSET, ACTOR_HEIGHT-FILL_OFFSET,
                    ARC_WIDTH, ARC_HEIGHT);
	    graphics.fillRoundRect(EXPR_X+FILL_OFFSET, EXPR_Y+FILL_OFFSET,
                    ACTOR_WIDTH-FILL_OFFSET, ACTOR_HEIGHT-FILL_OFFSET,
                    ARC_WIDTH, ARC_HEIGHT);
	    graphics.fillRoundRect(PLOT_X+FILL_OFFSET, PLOT_Y+FILL_OFFSET,
                    ACTOR_WIDTH-FILL_OFFSET, ACTOR_HEIGHT-FILL_OFFSET,
                    ARC_WIDTH, ARC_HEIGHT);

	    // draw triangle in ramp1
	    int[] xPoints = new int[3];
	    int[] yPoints = new int[3];
	    xPoints[0] = RAMP1_X+15;
	    xPoints[1] = RAMP1_X+ACTOR_WIDTH-15;
	    xPoints[2] = RAMP1_X+ACTOR_WIDTH-15;
	    yPoints[0] = RAMP1_Y+ACTOR_HEIGHT-15;
	    yPoints[1] = RAMP1_Y+15;
	    yPoints[2] = RAMP1_Y+ACTOR_HEIGHT-15;

	    graphics.setColor(Color.black);
	    graphics.drawPolygon(xPoints, yPoints, 3);
	    graphics.setColor(Color.yellow);
	    xPoints[0] += 1;
	    yPoints[1] += 1;
	    graphics.fillPolygon(xPoints, yPoints, 3);

	    // draw triangle in ramp2
	    xPoints[0] = RAMP2_X+15;
	    xPoints[1] = RAMP2_X+ACTOR_WIDTH-15;
	    xPoints[2] = RAMP2_X+ACTOR_WIDTH-15;
	    yPoints[0] = RAMP2_Y+ACTOR_HEIGHT-15;
	    yPoints[1] = RAMP2_Y+15;
	    yPoints[2] = RAMP2_Y+ACTOR_HEIGHT-15;

	    graphics.setColor(Color.black);
	    graphics.drawPolygon(xPoints, yPoints, 3);
	    graphics.setColor(Color.yellow);
	    xPoints[0] += 1;
	    yPoints[1] += 1;
	    graphics.fillPolygon(xPoints, yPoints, 3);

	    graphics.setColor(Color.black);
	    graphics.setFont(new Font("Serif", Font.BOLD, 14));
	    graphics.drawString("Expression", EXPR_X+10, EXPR_Y+ACTOR_HEIGHT-20);

	    graphics.setColor(Color.white);
	    graphics.fillRect(PLOT_X+10, PLOT_Y+10,
                    ACTOR_WIDTH-20, ACTOR_HEIGHT-20);
	    graphics.setColor(Color.black);
            int display = _plotDisplay.getSelectedIndex();
	    if (display == 0) {  // plotter selected
		// draw the axis
		graphics.drawLine(PLOT_X+20, PLOT_Y+ACTOR_HEIGHT-20,
                        PLOT_X+20, PLOT_Y+20);
		graphics.drawLine(PLOT_X+20, PLOT_Y+ACTOR_HEIGHT-20,
                        PLOT_X+ACTOR_WIDTH-20, PLOT_Y+ACTOR_HEIGHT-20);
		// draw the plot line
		int x1 = PLOT_X+25; int y1 = PLOT_Y+ACTOR_HEIGHT-25;
		int x2 = x1+ACTOR_WIDTH/4; int y2 = PLOT_Y+ACTOR_HEIGHT/2;
		int x3 = x2+10; int y3 = y2+10;
		int x4 = PLOT_X+ACTOR_WIDTH-20; int y4 = PLOT_Y+20;
		graphics.setColor(new Color(0.1F, 0.2F, 0.9F));
		graphics.drawLine(x1, y1, x2, y2);
		graphics.drawLine(x2, y2, x3, y3);
		graphics.drawLine(x3, y3, x4, y4);
	    } else {  // display selected
		// draw display text
	    	graphics.setFont(new Font("ScanSerif", Font.BOLD, 12));
		graphics.drawString("x", PLOT_X+20, PLOT_Y+20);
		graphics.drawString("xx", PLOT_X+20, PLOT_Y+30);
		graphics.drawString("xxx", PLOT_X+20, PLOT_Y+40);
	    }

	    // draw ports
	    graphics.setColor(Color.red);
	    int rad = 5;
	    graphics.fillOval(RAMP1_X+ACTOR_WIDTH-rad,
                    RAMP1_Y+ACTOR_HEIGHT/2-rad, rad*2, rad*2);
	    graphics.fillOval(RAMP2_X+ACTOR_WIDTH-rad,
                    RAMP2_Y+ACTOR_HEIGHT/2-rad, rad*2, rad*2);
	    graphics.fillOval(EXPR_X-rad, EXPR_Y+ACTOR_HEIGHT/3-rad-5,
                    rad*2, rad*2);
	    graphics.fillOval(EXPR_X-rad, EXPR_Y+ACTOR_HEIGHT*2/3-rad+5,
                    rad*2, rad*2);
	    graphics.fillOval(EXPR_X+ACTOR_WIDTH-rad, EXPR_Y+ACTOR_HEIGHT/2-rad,
                    rad*2, rad*2);
	    graphics.fillOval(PLOT_X-rad, PLOT_Y+ACTOR_HEIGHT/2-rad,
                    rad*2, rad*2);

	    // draw connections
	    graphics.setColor(new Color(1.0F, 0.4F, 0.0F));
	    graphics.drawLine(RAMP1_X+ACTOR_WIDTH+rad-1,
                    RAMP1_Y+ACTOR_HEIGHT/2+1,
                    EXPR_X-rad, EXPR_Y+ACTOR_HEIGHT/3-rad-2);
	    graphics.drawLine(RAMP2_X+ACTOR_WIDTH+rad-1,
                    RAMP2_Y+ACTOR_HEIGHT/2-1,
                    EXPR_X-rad, EXPR_Y+ACTOR_HEIGHT*2/3-rad+8);
	    graphics.drawLine(EXPR_X+ACTOR_WIDTH+rad-1, EXPR_Y+ACTOR_HEIGHT/2,
                    PLOT_X-rad, PLOT_Y+ACTOR_HEIGHT/2);

	    // draw types
	    graphics.setColor(Color.red);
	    graphics.setFont(new Font("Dialog", Font.BOLD, 18));
	    graphics.drawString(_ramp1Type, RAMP1_TYPE_X, RAMP1_TYPE_Y);
	    graphics.drawString(_ramp2Type, RAMP2_TYPE_X, RAMP2_TYPE_Y);
	    graphics.drawString(_exprIn1Type, EXPR_IN_TYPE_X, EXPR_IN_TYPE_Y1);
	    graphics.drawString(_exprIn2Type, EXPR_IN_TYPE_X, EXPR_IN_TYPE_Y2);
	    graphics.drawString(_exprOutType, EXPR_OUT_TYPE_X, EXPR_OUT_TYPE_Y);

	    if (display == 0) { // plotter selected
		graphics.drawString(_plotterType, PLOT_TYPE_X, PLOT_TYPE_Y);
	    } else {  // display selected
		graphics.drawString(_displayType, PLOT_TYPE_X, PLOT_TYPE_Y);
	    }
    	}
    }

    public class TypeGraphController extends BasicGraphController {

	/** The selection interactor for drag-selecting nodes
	 */
	private SelectionDragger _selectionDragger;

	/**
	 * Create a new basic controller with default
	 * node and edge interactors.
	 */
	public TypeGraphController () {
	    // The interactors attached to nodes and edges
	    setNodeController(new NodeController(this));
	    setEdgeController(new EdgeController(this));
            getNodeController().setNodeRenderer(new TypeRenderer());
            getEdgeController().setEdgeRenderer(new LineRenderer());
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
                    (SelectionInteractor)getEdgeController().getEdgeInteractor());
	    _selectionDragger.addSelectionInteractor(
                    (SelectionInteractor)getNodeController().getNodeInteractor());
        }
    }

    // TypeRenderer draws the nodes to represent types in a type lattice
    private class TypeRenderer implements NodeRenderer {

        // The size
        private double _size = 20;

        // Return the rendered visual representation of this node.
        public Figure render(Node n) {
            Object typeObj = n.getSemanticObject();

            // Create a colored circle
            BasicFigure figure = new BasicEllipse(0, 0, _size, _size);

            // Get the color and label
            Color color = Color.black;
            String label = "UNKNOWN";
            if (typeObj == BaseType.NAT) {
                color = Color.black;
                label = "NaT";
            } else if (typeObj == BaseType.INT) {
                color = Color.blue;
                label = "Int";
            } else if (typeObj == BaseType.DOUBLE) {
                color = Color.cyan;
                label = "Double";
            } else if (typeObj == BaseType.COMPLEX) {
                color = Color.green;
                label = "Complex";
            } else if (typeObj == BaseType.STRING) {
                color = Color.magenta;
                label = "String";
            } else if (typeObj == BaseType.GENERAL) {
                color = Color.red;
                label = "General";
            } else if (typeObj == BaseType.BOOLEAN) {
                color = Color.pink;
                label = "Boolean";
            } else if (typeObj == BaseType.OBJECT) {
                color = Color.yellow;
                label = "Object";
            } else if (typeObj == BaseType.SCALAR) {
                color = Color.gray;
                label = "Scalar";
            } else if (typeObj == BaseType.LONG) {
                color = Color.orange;
                label = "Long";
            }

            // Set the color and label
            figure.setFillPaint(color);
            LabelWrapper w = new LabelWrapper(figure, label);
            w.setAnchor(SwingConstants.EAST);
            w.getLabel().setAnchor(SwingConstants.WEST);
            return w;
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// LineRenderer

    // LineRenderer draws edges as simple lines
    private class LineRenderer implements EdgeRenderer {
        /**
         * Render a visual representation of the given edge.
         */
        public Connector render(Edge edge, Site tailSite, Site headSite) {
            StraightConnector c = new StraightConnector(tailSite, headSite);
            c.setUserObject(edge);
            return c;
        }
    }

    // The local listener class
    private class MyTypeListener implements TypeListener {

        public void typeChanged(final TypeEvent event) {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        ptolemy.data.type.Type newtype = event.getNewType();
                        String typeString = newtype.toString();

                        TypedIOPort port = event.getPort();
                        int id = 0;
                        if (port == _ramp1.output) {
                            _ramp1Type = typeString;
                            id = 0;
                        } else if (port == _ramp2.output) {
                            _ramp2Type = typeString;
                            id = 1;
                        } else if (port == _exprInput1) {
                            _exprIn1Type = typeString;
                            id = 2;
                        } else if (port == _exprInput2) {
                            _exprIn2Type = typeString;
                            id = 3;
                        } else if (port == _expr.output) {
                            _exprOutType = typeString;
                            id = 4;
                        } else if (port == _plotter.input) {
                            _plotterType = typeString;
                            id = 5;
                        } else if (port == _display.input) {
                            _displayType = typeString;
                            id = 6;
                        }

                        // Figure out which color to draw
                        ptolemy.data.type.Type typeObj = newtype;
                        int color = 7;
                        String label = "UNKNOWN";
                        if (typeObj == BaseType.NAT) {
                            color = 7;
                        } else if (typeObj == BaseType.INT) {
                            color = 4;
                        } else if (typeObj == BaseType.DOUBLE) {
                            color = 8;
                        } else if (typeObj == BaseType.COMPLEX) {
                            color = 3;
                        } else if (typeObj == BaseType.STRING) {
                            color = 5;
                        } else if (typeObj == BaseType.GENERAL) {
                            color = 0;
                        } else if (typeObj == BaseType.BOOLEAN) {
                            color = 9;
                        } else if (typeObj == BaseType.OBJECT) {
                            color = 2;
                        } else if (typeObj == BaseType.SCALAR) {
                            color = 6;
                        } else if (typeObj == BaseType.LONG) {
                            color = 1;
                        }

                        // Get the trace and element figure
                        TraceModel model =
                            _tracePane.getTraceView().getTraceModel();
                        TraceModel.Trace trace = model.getTrace(id);

                        // Create the new element
                        double currentTime = (double) (_counter);
                        _counter++;

                        // Make the elements look large in case they're the
                        // last one
                        TraceModel.Element element =
                            new TraceModel.Element(
                                    currentTime, currentTime+1, color);
                        element.closure = TraceModel.Element.OPEN_END;
                        trace.add(element);

                        // Close the current element
                        TraceModel.Element current = _currentElement[id];
                        current.closure = 0;
                        // Update all elements
                        int msize = model.size();
                        TraceModel.Element temp[] =
                            new TraceModel.Element[msize];
                        for (int i = 0; i < msize; i++) {
                            _currentElement[i].stopTime = currentTime+1;
                            temp[i] = _currentElement[i];
                        }
                        TraceView v = _tracePane.getTraceView();
                        for (int i = 0; i < msize; i++) {
                            v.updateTraceElement(temp[i]);
                        }
                        v.drawTraceElement(element);

                        // Update
                        _currentElement[id] = element;

                        _schemPanel.repaint();
                    }
                });
            } catch (Exception e) {
                report(e);
            }
        }
    }
}
