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

package ptolemy.domains.sdf.guidemo;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.util.Hashtable;

import diva.graph.*;
import diva.graph.model.*;
import diva.graph.layout.*;
import diva.canvas.*;
import diva.canvas.connector.*;
import diva.canvas.toolbox.*;
import diva.util.gui.TutorialWindow;
import diva.surfaces.trace.*;

import javax.swing.*;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;


// import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
//import ptolemy.data.expr.*;
import ptolemy.data.expr.Parameter;
import ptolemy.actor.*;
import ptolemy.actor.lib.*;
import ptolemy.actor.util.*;
// import ptolemy.domains.sdf.kernel.*;
// import ptolemy.domains.sdf.lib.*;
// import ptolemy.plot.*;

//////////////////////////////////////////////////////////////////////////
//// TypeApplet
/**
An applet that demonstrates the Ptolemy II type system.

@author Yuhong Xiong
@version $Id$
*/

public class TypeApplet extends SDFApplet {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** The JGraph where we display stuff
     */
    JGraph jgraph = new JGraph();

    /** The pane displaying the trace
     */
    private TracePane tracePane;

    /** The canvas displaying the trace
     */
    private JCanvas traceCanvas;

    /** The type listener
     */
    private MyTypeListener typeListener;

    // The start time for the trace
    private long _startTime = 0;

        // The current element of each state;
        private TraceModel.Element _currentElement[];


    /** Initialize the applet.
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
	    //			       plotter/printer selection)
	    //	       plot/print Panel
	    //     _schemPanel (schematics with type annotation)

	    _buildType2String();

	    setLayout(new GridLayout(3, 1));
//            setLayout(new BorderLayout());
            _ioPanel.setLayout(new GridLayout(1, 2));
            add(_ioPanel);
	    add(_schemPanel);

//            add(BorderLayout.NORTH,_ioPanel);
//            _ioPanel.setSize(new Dimension(800,200));
//	    add(BorderLayout.CENTER,_schemPanel);
//            _schemPanel.setSize(new Dimension(800,250));

            Panel controlPanel = new Panel();
	    controlPanel.setLayout(new BorderLayout());
            _ioPanel.add(controlPanel);
	    _buildControlPanel(controlPanel);

	    _buildModel();

            JPanel visPanel = new JPanel();
            visPanel.setLayout(new BorderLayout());
            add(visPanel);
//            add(BorderLayout.SOUTH,visPanel);

            visPanel.add(jgraph, BorderLayout.WEST);
            jgraph.setPreferredSize(new Dimension(400, 270));
            jgraph.setSize(new Dimension(400, 270));

            // Construct the Ptolemy type lattice model
            final GraphModel graphModel = constructLattice();

            // Display the type lattice 
            displayGraph(jgraph, graphModel);

            // Construct a new trace model
            TraceModel traceModel = new TraceModel();

            // Display the trace
            traceCanvas = displayTrace(traceModel);
            traceCanvas.setPreferredSize(new Dimension(400,270));
            visPanel.add(traceCanvas, BorderLayout.EAST);

            _addListeners();
            
         } catch (Exception ex) {
             report("Setup failed:", ex);
         }
     }

     /**  Construct the graph representing the Ptolemy type lattice
      */
     public GraphModel constructLattice () {
         GraphModel model = new GraphModel();

         // nodes, with user object set to the actor
         Node n1 = model.createNode(Void.TYPE);
         Node n2 = model.createNode(IntToken.class);
         Node n3 = model.createNode(DoubleToken.class);
         Node n4 = model.createNode(ComplexToken.class);
         Node n5 = model.createNode(StringToken.class);
         Node n6 = model.createNode(Token.class);
         Node n7 = model.createNode(BooleanToken.class);
         Node n8 = model.createNode(ObjectToken.class);
         Node n9 = model.createNode(ScalarToken.class);
         Node n10 = model.createNode(LongToken.class);

         model.addNode(n1);
         model.addNode(n2);
         model.addNode(n3);
         model.addNode(n4);
         model.addNode(n5);
         model.addNode(n6);
         model.addNode(n7);
         model.addNode(n8);
         model.addNode(n9);
         model.addNode(n10);

         /** 
         nodeMap.put(a1,n1);
         nodeMap.put(a2,n2);
         nodeMap.put(a3,n3);
         nodeMap.put(a4,n4);
         nodeMap.put(a5,n5);
         nodeMap.put(a6,n6);
         nodeMap.put(a7,n7);
         nodeMap.put(a8,n8);
         */

         // Edges
         model.createEdge(n8,n1);
         model.createEdge(n6,n8);

         model.createEdge(n6,n5);
         model.createEdge(n5,n7);
         model.createEdge(n7,n1);

         model.createEdge(n5,n9);
         model.createEdge(n9,n10);
         model.createEdge(n10,n2);
         model.createEdge(n2,n1);
         model.createEdge(n3,n2);
         model.createEdge(n4,n3);
         model.createEdge(n9,n4);

         return model;
     }


     /**
      * Construct the graph widget with
      * the default constructor (giving it an empty graph),
      * and then set the model once the window is showing.
      */
     public void displayGraph(JGraph g, GraphModel model) {
         // add(g);
         ///g.setPreferredSize(new Dimension(300, 400));

         // Make sure we have the right renderers and then
         // display the graph
         final GraphPane gp = (GraphPane) g.getCanvasPane();
         final GraphView gv = gp.getGraphView();
         gv.setNodeRenderer(new TypeRenderer());
         gv.setEdgeRenderer(new LineRenderer());
         g.setGraphModel(model);

         // Do the layout
         final GraphModel m = model;
         try {
             SwingUtilities.invokeLater(new Runnable() {
                 public void run () {
                     // Layout is a bit stupid
                     gv.setLayoutPercentage(0.7);
                     LevelLayout staticLayout = new LevelLayout();
                     staticLayout.setOrientation(LevelLayout.VERTICAL);
                     staticLayout.layout(gv, m.getGraph());
                     gp.repaint();
                 }
             });
         } catch (Exception e) {
             System.out.println(e);
         }
     }

     /**
      * Initialize the trace model.
      */
     public void initTraceModel(TraceModel model) {
         TraceModel.Trace t;

         t = new TraceModel.Trace();
         t.setUserObject("ramp1.output");
         model.addTrace(_ramp1.output, t);

         t = new TraceModel.Trace();
         t.setUserObject("ramp2.output");
         model.addTrace(_ramp2.output, t);

         t = new TraceModel.Trace();
         t.setUserObject("expr.input1");
         model.addTrace(_expr.getPort("input1"), t);

         t = new TraceModel.Trace();
         t.setUserObject("expr.input2");
         model.addTrace(_expr.getPort("input2"), t);

         t = new TraceModel.Trace();
         t.setUserObject("expr.output");
         model.addTrace(_expr.output, t);

         t = new TraceModel.Trace();
         t.setUserObject("plotter.input");
         model.addTrace(_plotter.input, t);

         t = new TraceModel.Trace();
         t.setUserObject("printer.input");
         model.addTrace(_printer.input, t);
     }

      /**
      * Initialize the trace view.
      */
     public void initTraceView() {
         TraceView view = tracePane.getTraceView();
         TraceModel model = tracePane.getTraceModel();
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
        
    /**
     * Construct the trace display in a JCanvas and return the JCanvas
     */
    public JCanvas displayTrace(TraceModel traceModel) {
        tracePane = new TracePane();
        JCanvas traceWidget = new JCanvas(tracePane);
        
        // Configure the view
        TraceView traceView = tracePane.getTraceView();
        traceView.setTimeScale(0.5);
        traceView.setLayout(10,10,400,20,20);
        traceView.setTraceModel(traceModel);

        return traceWidget;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Execute the system.  This overrides the base class to read the
     *  values in the query box first.
     */
    protected void _go() {
	try {
	    // FIXME: Before parameter change is supported, totally
	    // replace the parameters by new instances.
	    _ramp1.init.setContainer(null);
	    Parameter init1 = new Parameter(_ramp1, "init");
	    _ramp1.init = init1;
//	    init1.setExpression(_ramp1InitQuery.stringValue("ramp1init"));
	    init1.setExpression(_query.stringValue("ramp1init"));

	    _ramp1.step.setContainer(null);
	    Parameter step1 = new Parameter(_ramp1, "step");
	    _ramp1.step = step1;
//	    step1.setExpression(_ramp1StepQuery.stringValue("ramp1step"));
	    step1.setExpression(_query.stringValue("ramp1step"));

	    _ramp2.init.setContainer(null);
	    Parameter init2 = new Parameter(_ramp2, "init");
	    _ramp2.init = init2;
//	    init2.setExpression(_ramp2InitQuery.stringValue("ramp2init"));
	    init2.setExpression(_query.stringValue("ramp2init"));

	    _ramp2.step.setContainer(null);
	    Parameter step2 = new Parameter(_ramp2, "step");
	    _ramp2.step = step2;
//	    step2.setExpression(_ramp2StepQuery.stringValue("ramp2step"));
	    step2.setExpression(_query.stringValue("ramp2step"));

	    _expr.expression.setToken(
//			new StringToken(_exprQuery.stringValue("expr")));
			new StringToken(_query.stringValue("expr")));
            
            // Reinitialize the trace display
            tracePane.getTraceView().clear();
            tracePane.getTraceModel().clear();
            initTraceModel(tracePane.getTraceModel());
            initTraceView();

            // Now set system "start" time
            _startTime = System.currentTimeMillis();

            super._go();
        } catch (Exception ex) {
            report("Go failed:", ex);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                          private methods                  ////

    private void _buildControlPanel(Panel controlPanel) {
	Panel runControlPanel = _createRunControls(1);
	controlPanel.add("North", runControlPanel);

//	Panel paramPanel = new Panel();
//	paramPanel.setLayout(new GridLayout(6, 1));
//	controlPanel.add("Center", paramPanel);
	controlPanel.add("Center", _query);

//	_ramp1InitQuery.line("ramp1init", "Ramp1 Initial Value", "0", 20);
//	_ramp1StepQuery.line("ramp1step", "Ramp1 Step Size", "1", 20);
//	_ramp2InitQuery.line("ramp2init", "Ramp2 Init Value", "0", 20);
//	_ramp2StepQuery.line("ramp2step", "Ramp2 Step Size", "1", 20);
//	_exprQuery.line("expr", "Expression", "input1 + input2", 20);
	_query.line("ramp1init", "Ramp1 Initial Value", "0");
	_query.line("ramp1step", "Ramp1 Step Size", "1");
	_query.line("ramp2init", "Ramp2 Init Value", "0");
	_query.line("ramp2step", "Ramp2 Step Size", "1");
	_query.line("expr", "Expression", "input1 + input2");

//	paramPanel.add(_ramp1InitQuery);
//	paramPanel.add(_ramp1StepQuery);
//	paramPanel.add(_ramp2InitQuery);
//	paramPanel.add(_ramp2StepQuery);
//	paramPanel.add(_exprQuery);

	Panel displayPanel = new Panel();
	displayPanel.add(new Label("Display using"));
	CheckboxGroup displayGroup = new CheckboxGroup();
	_plotterBox = new Checkbox("Plotter", displayGroup, true);
	_printerBox = new Checkbox("Printer", displayGroup, false);
	displayPanel.add(_plotterBox);
	displayPanel.add(_printerBox);
//	paramPanel.add(displayPanel);
	controlPanel.add("South", displayPanel);
    }

    private void _buildModel()
	    throws NameDuplicationException, IllegalActionException {

        // Create the ramps
        _ramp1 = new Ramp(_toplevel, "ramp1");
	_ramp1.init.setToken(new DoubleToken(0.0));
	_ramp1.step.setToken(new DoubleToken(1.0));
	_ramp2 = new Ramp(_toplevel, "ramp2");
	_ramp2.init.setToken(new DoubleToken(0.0));
	_ramp2.step.setToken(new DoubleToken(1.0));

        // Create and configure expr
        _expr = new Expression(_toplevel, "expr");
        TypedIOPort input1 = new TypedIOPort(_expr, "input1", true, false);
        TypedIOPort input2 = new TypedIOPort(_expr, "input2", true, false);

        // Create and configure plotter
        _plotter = new TimePlot(_toplevel, "plot");
        _plotter.setPanel(_ioPanel);
//            _plotter.setPanel(this);
        _plotter.plot.setGrid(true);
        _plotter.plot.setXRange(0.0, 10.0);
        _plotter.plot.setYRange(0.0, 20.0);
	_plotter.plot.setConnected(false);
	_plotter.plot.setImpulses(true);
	_plotter.plot.setMarksStyle("dots");
        _plotter.timed.setToken(new BooleanToken(false));

	// Create printer. Can't use null in constructor, thus
	// set the container to null after construction.
	_printer = new Print(_toplevel, "print");
	_printer.setContainer(null);

        _toplevel.connect(_ramp1.output, input1);
        _toplevel.connect(_ramp2.output, input2);
        _toplevel.connect(_expr.output, _plotter.input);
    }

    private void _addListeners()
	    throws NameDuplicationException, IllegalActionException {

	_plotterBox.addItemListener(new DisplayListener());
	_printerBox.addItemListener(new DisplayListener());

	typeListener = new MyTypeListener();
	_ramp1.output.addTypeListener(typeListener);
	_ramp2.output.addTypeListener(typeListener);
	((TypedIOPort)_expr.getPort("input1")).addTypeListener(typeListener);
	((TypedIOPort)_expr.getPort("input2")).addTypeListener(typeListener);
	_expr.output.addTypeListener(typeListener);
	_plotter.input.addTypeListener(typeListener);
	_printer.input.addTypeListener(typeListener);

        // Add code to extend the trace

        ///FIXME
    }

    private void _buildType2String() {
        _type2String.put(ptolemy.data.Token.class, "General");
        _type2String.put(ObjectToken.class, "Object");
        _type2String.put(StringToken.class, "String");

        _type2String.put(Numerical.class, "Numerical");
        _type2String.put(BooleanMatrixToken.class, "BooleanMatrix");
        _type2String.put(LongMatrixToken.class, "LongMatrix");
        _type2String.put(ComplexMatrixToken.class, "ComplexMatrix");
        _type2String.put(DoubleMatrixToken.class, "DoubleMatrix");
	_type2String.put(IntMatrixToken.class, "IntMatrix");

	_type2String.put(BooleanToken.class, "Boolean");

        _type2String.put(ScalarToken.class, "Scalar");
	_type2String.put(LongToken.class, "Long");
	_type2String.put(ComplexToken.class, "Complex");
	_type2String.put(DoubleToken.class, "Double");
	_type2String.put(IntToken.class, "Int");

	_type2String.put(Void.TYPE, "NaT");
    }

    // set the plotter or the printer as display. connect the
    // topology accordingly.
    private void _setDisplay() {

	try {
	    if (_plotterBox.getState() == true) {
	        _ioPanel.remove(_printer.textArea);
                _plotter.setPanel(_ioPanel);
		_plotter.plot.setGrid(true);
        	_plotter.plot.setXRange(0.0, 10.0);
        	_plotter.plot.setYRange(0.0, 20.0);
		_plotter.plot.setConnected(false);
		_plotter.plot.setImpulses(true);
		_plotter.plot.setMarksStyle("dots");
        	_plotter.timed.setToken(new BooleanToken(false));

	        _expr.output.unlinkAll();
		_printer.setContainer(null);
		_plotter.setContainer(_toplevel);

                _toplevel.connect(_expr.output, _plotter.input);
		_director.setScheduleValid(false);

	    } else {
	        _ioPanel.remove(_plotter.plot);
	        _printer.setPanel(_ioPanel);
	        _expr.output.unlinkAll();
		_plotter.setContainer(null);
		_printer.setContainer(_toplevel);
		_toplevel.connect(_expr.output, _printer.input);
		_director.setScheduleValid(false);
	    }
	    _ioPanel.validate();
	    _schemPanel.repaint();
        } catch (Exception ex) {
            report("setDisplay failed:", ex);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private Expression _expr;
    private Ramp _ramp1, _ramp2;
    private TimePlot _plotter;
    private Print _printer;

//    private Query _ramp1InitQuery = new Query();
//    private Query _ramp1StepQuery = new Query();
//    private Query _ramp2InitQuery = new Query();
//    private Query _ramp2StepQuery = new Query();
//    private Query _exprQuery = new Query();;
    private Query _query = new Query();

    private String _ramp1Type = "NaT";
    private String _ramp2Type = "NaT";
    private String _exprIn1Type = "NaT";
    private String _exprIn2Type = "NaT";
    private String _exprOutType = "NaT";
    private String _plotterType = "Double";
    private String _printerType = "String";

    private Hashtable _type2String = new Hashtable();

    private Checkbox _plotterBox, _printerBox;
    private Panel _ioPanel = new Panel();
    private SchematicPanel _schemPanel = new SchematicPanel();

    ///////////////////////////////////////////////////////////////////
    ////                       inner class                         ////

    private class SchematicPanel extends Panel {

	public SchematicPanel() {
//	    setBackground(new Color(0.9F, 1.0F, 0.7F));
	}

    	public void paint(Graphics graph) {

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
	    graph.setColor(Color.black);
	    graph.drawRoundRect(RAMP1_X, RAMP1_Y, ACTOR_WIDTH, ACTOR_HEIGHT,
				ARC_WIDTH, ARC_HEIGHT);
	    graph.drawRoundRect(RAMP2_X, RAMP2_Y, ACTOR_WIDTH, ACTOR_HEIGHT,
				ARC_WIDTH, ARC_HEIGHT);
	    graph.drawRoundRect(EXPR_X, EXPR_Y, ACTOR_WIDTH, ACTOR_HEIGHT,
				ARC_WIDTH, ARC_HEIGHT);
	    graph.drawRoundRect(PLOT_X, PLOT_Y, ACTOR_WIDTH, ACTOR_HEIGHT,
				ARC_WIDTH, ARC_HEIGHT);

	    graph.setColor(new Color(0.6F, 0.9F, 1.0F));
	    graph.fillRoundRect(RAMP1_X+FILL_OFFSET, RAMP1_Y+FILL_OFFSET,
			ACTOR_WIDTH-FILL_OFFSET, ACTOR_HEIGHT-FILL_OFFSET,
			ARC_WIDTH, ARC_HEIGHT);
	    graph.fillRoundRect(RAMP2_X+FILL_OFFSET, RAMP2_Y+FILL_OFFSET,
			ACTOR_WIDTH-FILL_OFFSET, ACTOR_HEIGHT-FILL_OFFSET,
			ARC_WIDTH, ARC_HEIGHT);
	    graph.fillRoundRect(EXPR_X+FILL_OFFSET, EXPR_Y+FILL_OFFSET,
			ACTOR_WIDTH-FILL_OFFSET, ACTOR_HEIGHT-FILL_OFFSET,
			ARC_WIDTH, ARC_HEIGHT);
	    graph.fillRoundRect(PLOT_X+FILL_OFFSET, PLOT_Y+FILL_OFFSET,
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

	    graph.setColor(Color.black);
	    graph.drawPolygon(xPoints, yPoints, 3);
	    graph.setColor(Color.yellow);
	    xPoints[0] += 1;
	    yPoints[1] += 1;
	    graph.fillPolygon(xPoints, yPoints, 3);

	    // draw triangle in ramp2
	    xPoints[0] = RAMP2_X+15;
	    xPoints[1] = RAMP2_X+ACTOR_WIDTH-15;
	    xPoints[2] = RAMP2_X+ACTOR_WIDTH-15;
	    yPoints[0] = RAMP2_Y+ACTOR_HEIGHT-15;
	    yPoints[1] = RAMP2_Y+15;
	    yPoints[2] = RAMP2_Y+ACTOR_HEIGHT-15;

	    graph.setColor(Color.black);
	    graph.drawPolygon(xPoints, yPoints, 3);
	    graph.setColor(Color.yellow);
	    xPoints[0] += 1;
	    yPoints[1] += 1;
	    graph.fillPolygon(xPoints, yPoints, 3);

	    graph.setColor(Color.black);
	    graph.setFont(new Font("Serif", Font.BOLD, 14));
	    graph.drawString("Expression", EXPR_X+10, EXPR_Y+ACTOR_HEIGHT-20);

	    graph.setColor(Color.white);
	    graph.fillRect(PLOT_X+10, PLOT_Y+10,
				ACTOR_WIDTH-20, ACTOR_HEIGHT-20);
	    graph.setColor(Color.black);
	    if (_plotterBox.getState() == true) {
		// draw the axis
		graph.drawLine(PLOT_X+20, PLOT_Y+ACTOR_HEIGHT-20,
				PLOT_X+20, PLOT_Y+20);
		graph.drawLine(PLOT_X+20, PLOT_Y+ACTOR_HEIGHT-20,
				PLOT_X+ACTOR_WIDTH-20, PLOT_Y+ACTOR_HEIGHT-20);
		// draw the plot line
		int x1 = PLOT_X+25; int y1 = PLOT_Y+ACTOR_HEIGHT-25;
		int x2 = x1+ACTOR_WIDTH/4; int y2 = PLOT_Y+ACTOR_HEIGHT/2;
		int x3 = x2+10; int y3 = y2+10;
		int x4 = PLOT_X+ACTOR_WIDTH-20; int y4 = PLOT_Y+20;
		graph.setColor(new Color(0.1F, 0.2F, 0.9F));
		graph.drawLine(x1, y1, x2, y2);
		graph.drawLine(x2, y2, x3, y3);
		graph.drawLine(x3, y3, x4, y4);
	    } else {
		// draw printer text
	    	graph.setFont(new Font("ScanSerif", Font.BOLD, 12));
		graph.drawString("x", PLOT_X+20, PLOT_Y+20);
		graph.drawString("xx", PLOT_X+20, PLOT_Y+30);
		graph.drawString("xxx", PLOT_X+20, PLOT_Y+40);
	    }

	    // draw ports
	    graph.setColor(Color.red);
	    int rad = 5;
	    graph.fillOval(RAMP1_X+ACTOR_WIDTH-rad,
				RAMP1_Y+ACTOR_HEIGHT/2-rad, rad*2, rad*2);
	    graph.fillOval(RAMP2_X+ACTOR_WIDTH-rad,
				RAMP2_Y+ACTOR_HEIGHT/2-rad, rad*2, rad*2);
	    graph.fillOval(EXPR_X-rad, EXPR_Y+ACTOR_HEIGHT/3-rad-5,
				rad*2, rad*2);
	    graph.fillOval(EXPR_X-rad, EXPR_Y+ACTOR_HEIGHT*2/3-rad+5,
				rad*2, rad*2);
	    graph.fillOval(EXPR_X+ACTOR_WIDTH-rad, EXPR_Y+ACTOR_HEIGHT/2-rad,
				rad*2, rad*2);
	    graph.fillOval(PLOT_X-rad, PLOT_Y+ACTOR_HEIGHT/2-rad,
				rad*2, rad*2);

	    // draw connections
	    graph.setColor(new Color(1.0F, 0.4F, 0.0F));
	    graph.drawLine(RAMP1_X+ACTOR_WIDTH+rad-1, RAMP1_Y+ACTOR_HEIGHT/2+1,
			   EXPR_X-rad, EXPR_Y+ACTOR_HEIGHT/3-rad-2);
	    graph.drawLine(RAMP2_X+ACTOR_WIDTH+rad-1, RAMP2_Y+ACTOR_HEIGHT/2-1,
			   EXPR_X-rad, EXPR_Y+ACTOR_HEIGHT*2/3-rad+8);
	    graph.drawLine(EXPR_X+ACTOR_WIDTH+rad-1, EXPR_Y+ACTOR_HEIGHT/2,
			   PLOT_X-rad, PLOT_Y+ACTOR_HEIGHT/2);

	    // draw types
	    graph.setColor(Color.red);
	    graph.setFont(new Font("Dialog", Font.BOLD, 18));
	    graph.drawString(_ramp1Type, RAMP1_TYPE_X, RAMP1_TYPE_Y);
	    graph.drawString(_ramp2Type, RAMP2_TYPE_X, RAMP2_TYPE_Y);
	    graph.drawString(_exprIn1Type, EXPR_IN_TYPE_X, EXPR_IN_TYPE_Y1);
	    graph.drawString(_exprIn2Type, EXPR_IN_TYPE_X, EXPR_IN_TYPE_Y2);
	    graph.drawString(_exprOutType, EXPR_OUT_TYPE_X, EXPR_OUT_TYPE_Y);

	    if (_plotterBox.getState() == true) {
		graph.drawString(_plotterType, PLOT_TYPE_X, PLOT_TYPE_Y);
	    } else {
		graph.drawString(_printerType, PLOT_TYPE_X, PLOT_TYPE_Y);
	    }
    	}
    }

    private class DisplayListener implements ItemListener {
	public void itemStateChanged(ItemEvent e) {
	    _setDisplay();
	}
    }

    ///////////////////////////////////////////////////////////////////
    //// TypeRenderer

    /**
     * TypeRenderer draws the nodes to represent types in a type lattice
     */
    public class TypeRenderer implements NodeRenderer {

        /** The size
         */
        private double _size = 20;

        /**
         * Return the rendered visual representation of this node.
         */
        public Figure render (Node n) {
            Object typeObj = n.getSemanticObject();

            // Create a colored circle
            BasicFigure figure = new BasicEllipse(0, 0, _size, _size);

            // Get the color and label
            Color color = Color.black;
            String label = "UNKNOWN";
            if (typeObj == Void.TYPE) {
                color = Color.black;
                label = "NaT";
            } else if (typeObj == IntToken.class) {
                color = Color.blue;
                label = "Int";
            } else if (typeObj == DoubleToken.class) {
                color = Color.cyan;
                label = "Double";
            } else if (typeObj == ComplexToken.class) {
                color = Color.green;
                label = "Complex";
            } else if (typeObj == StringToken.class) {
                color = Color.magenta;
                label = "String";
            } else if (typeObj == Token.class) {
                color = Color.red;
                label = "General";
            } else if (typeObj == BooleanToken.class) {
                color = Color.pink;
                label = "Boolean";
            } else if (typeObj == ObjectToken.class) {
                color = Color.yellow;
                label = "Object";
            } else if (typeObj == ScalarToken.class) {
                color = Color.gray;
                label = "Scalar";
            } else if (typeObj == LongToken.class) {
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

    /**
     * LineRenderer draws edges as simple lines
     */
    public class LineRenderer implements EdgeRenderer {
        /**
         * Render a visual representation of the given edge.
         */
        public Connector render(Edge edge, Site tailSite, Site headSite) {
            StraightConnector c = new StraightConnector(tailSite, headSite);
            c.setUserObject(edge);
            return c;
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// MyTypeListener

    /** The local listener class
     */
    public class MyTypeListener implements TypeListener {

        public void typeChanged(TypeEvent event) {
            Class newtype = event.getNewType();
            String typeString = newtype == null ? "NaT" :
                (String)_type2String.get(newtype);
            
            TypedIOPort port = event.getPort();
            int id = 0;
            if (port == _ramp1.output) {
                _ramp1Type = typeString;
                id = 0;
            } else if (port == _ramp2.output) {
                _ramp2Type = typeString;
                id = 1;
            } else if (port == _expr.getPort("input1")) {
                _exprIn1Type = typeString;
                id = 2;
            } else if (port == _expr.getPort("input2")) {
                _exprIn2Type = typeString;
                id = 3;
            } else if (port == _expr.output) {
                _exprOutType = typeString;
                id = 4;
            } else if (port == _plotter.input) {
                _plotterType = typeString;
                id = 5;
            } else if (port == _printer.input) {
                _printerType = typeString;
                id = 6;
            }
            
            _schemPanel.repaint();

              // Figure out which color to draw
              Class typeObj = newtype;
              int color = 7;
              String label = "UNKNOWN";
              if (typeObj == null || typeObj == Void.TYPE) {
                  color = 7;
              } else if (typeObj == IntToken.class) {
                  color = 4;
              } else if (typeObj == DoubleToken.class) {
                  color = 8;
              } else if (typeObj == ComplexToken.class) {
                  color = 3;
              } else if (typeObj == StringToken.class) {
                  color = 5;
              } else if (typeObj == Token.class) {
                  color = 0;
              } else if (typeObj == BooleanToken.class) {
                  color = 9;
              } else if (typeObj == ObjectToken.class) {
                  color = 2;
              } else if (typeObj == ScalarToken.class) {
                  color = 6;
              } else if (typeObj == LongToken.class) {
                  color = 1;
              }

              // Get the trace and element figure
              TraceModel model = tracePane.getTraceView().getTraceModel();
              TraceModel.Trace trace = model.getTrace(id);

              // Create the new element
              double currentTime = (double) (System.currentTimeMillis() - _startTime);

              // Make the elements look large in case they're the
              // last one
              final TraceModel.Element element = new TraceModel.Element(
                      currentTime, currentTime+50, color);
              element.closure = TraceModel.Element.OPEN_END;
              trace.add(element);
            
              // Close the current element
              final TraceModel.Element current = _currentElement[id];
              current.closure = 0;

              // Update all elements
              final int msize = model.size();
              final TraceModel.Element temp[] = new TraceModel.Element[msize];
              for (int i = 0; i < msize; i++) {
                  _currentElement[i].stopTime = currentTime+50;
                  temp[i] = _currentElement[i];
              }

              try {
                  SwingUtilities.invokeAndWait(new Runnable() {
                      public void run () {
                          TraceView v = tracePane.getTraceView();
                          for (int i = 0; i < msize; i++) {
                              v.updateTraceElement(temp[i]);
                          }
                          v.drawTraceElement(element);
                      }
                  });
              }
              catch (Exception e) {
                  System.out.println(e);
              }

              // Update
              _currentElement[id] = element;
            
        }
    }
}

