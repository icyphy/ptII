/* An applet that demonstrates the Ptolemy II type system.

 Copyright (c) 1999-2000 The Regents of the University of California.
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
@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.demo.Type;

// FIXME: Trim this.
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.actor.*;
import ptolemy.actor.gui.PtolemyApplet;
import ptolemy.actor.lib.*;
import ptolemy.actor.lib.gui.*;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.*;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.plot.*;
import ptolemy.vergil.MoMLViewerApplet;

import diva.canvas.*;
import diva.canvas.connector.*;
import diva.canvas.interactor.*;
import diva.canvas.toolbox.*;
import diva.graph.*;
import diva.graph.basic.*;
import diva.graph.layout.*;
import diva.surfaces.trace.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Font;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.Hashtable;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;


//////////////////////////////////////////////////////////////////////////
//// Type
/**
An applet that demonstrates the Ptolemy II type system.  This applet
connects two ramps to two inputs of an expression actor, and connects
the output of the expression actor to either a display or a plotter.
It displays a Diva animation of the type resolution process.

@author Yuhong Xiong, John Reekie, Edward A. Lee
@version $Id$
*/

public class Type extends MoMLViewerApplet implements ValueListener {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to the fact that the manager state has changed.  This is
     *  called by the manager.  In this class, we detect the start of
     *  execution of the model to initialize the trace display.
     *  @param manager The manager that changed.
     */
    public void managerStateChanged(Manager manager) {
        super.managerStateChanged(manager);

        if (manager.getState() == Manager.PREINITIALIZING) {
            // Reinitialize the trace display
            _tracePane.getTraceView().clear();
            _tracePane.getTraceModel().clear();
            _initTraceModel(_tracePane.getTraceModel());
            _initTraceView();
	    _counter = 0;
        }
    }

    /** Override the base class to avoid executing the model automatically
     *  when the applet starts.  This way, the initial types (UNKNOWN) are
     *  displayed in the animation.
     */
    public void start() {
    }

    /** React to a change in the selection of plotter or display.
     *  @param settable The parameter that changed.
     */
    public void valueChanged(Settable settable) {
        try {
            boolean usePlotter = ((BooleanToken)((Parameter)settable)
                    .getToken()).booleanValue();

            if (usePlotter) {
                ChangeRequest request
                        = new ChangeRequest(this, "switch display") {
                    /** Execute the change.
                     *  @exception Exception If the change fails.
                     */
                    protected void _execute() throws Exception {
                        _display.setContainer(null);
                        _plotter.setContainer(_toplevelCopy);
                        _plotter.input.link(_displayRelation);
                    }
                };
                _toplevel.requestChange(request);
            } else {
                ChangeRequest request
                        = new ChangeRequest(this, "switch display") {
                    /** Execute the change.
                     *  @exception Exception If the change fails.
                     */
                    protected void _execute() throws Exception {
                        _plotter.setContainer(null);
                        _display.setContainer(_toplevelCopy);
                        _display.input.link(_displayRelation);
                    }
                };
                _toplevel.requestChange(request);
            }
        } catch (Exception ex) {
            report("setDisplay failed:", ex);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create the model.
     *  @param workspace The workspace in which to create the model.
     *  @exception Exception If something goes wrong.
     */    
    protected CompositeActor _createModel(Workspace workspace)
	    throws Exception {
        _toplevel = super._createModel(workspace);
        _manager.addExecutionListener(this);
        _director = (SDFDirector)_toplevel.getDirector();
        _ramp1 = (Ramp)_toplevel.getEntity("Ramp1");
        _ramp2 = (Ramp)_toplevel.getEntity("Ramp2");
        _expr = (Expression)_toplevel.getEntity("Expression");
        _exprInput1 = (TypedIOPort)_expr.getPort("input1");
        _exprInput2 = (TypedIOPort)_expr.getPort("input2");
	_display = (Display)_toplevel.getEntity("Display");
        _displayRelation = _toplevel.getRelation("displayRelation");

        // Create a plotter to keep in reserve.  Have to first construct
        // in the toplevel, then remove it.
        _plotter = new SequencePlotter(_toplevel, "Plotter");
        _plotter.setContainer(null);
        // Rename so name is the same as the display.
        _plotter.setName("Display");
        // Copy the location from the printer.
        Attribute location = _display.getAttribute("_location");
        Attribute copy = (Attribute)location.clone(_toplevel.workspace());
        copy.setContainer(_plotter);

        Parameter selector = (Parameter)_toplevel.getAttribute("Use Plotter");
        selector.addValueListener(this);

        return _toplevel;
    }

    /** Create the on-screen Diva displays.
     */
    protected void _createView() {
        super._createView();

        // Visualization panel contains the type lattice and
        // animation of type resolution progress (trace model).
        JPanel visPanel = new JPanel();
        visPanel.setLayout(new BorderLayout());
        visPanel.setBackground(getBackground());
        _jgraph = _constructLatticeModel();
        visPanel.add(_jgraph, BorderLayout.WEST);
        getContentPane().add(visPanel, BorderLayout.SOUTH);

        // Construct a new trace model
        TraceModel traceModel = new TraceModel();
        _traceCanvas = _displayTrace(traceModel);
        _traceCanvas.setBorder(new LineBorder(Color.black));
        _traceCanvas.setPreferredSize(new Dimension(400, 290));
        visPanel.add(_traceCanvas, BorderLayout.EAST);
        
        _addListeners();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private void _addListeners() {
	TypeListener typeListener = new PortTypeListener();
	_ramp1.output.addTypeListener(typeListener);
	_ramp2.output.addTypeListener(typeListener);
	_exprInput1.addTypeListener(typeListener);
	_exprInput2.addTypeListener(typeListener);
	_expr.output.addTypeListener(typeListener);
	_plotter.input.addTypeListener(typeListener);
	_display.input.addTypeListener(typeListener);
    }

    // Construct the graph representing the Ptolemy type lattice
    private JGraph _constructLatticeModel() {
	BasicGraphModel model = new BasicGraphModel();
        // Make sure we have the right renderers and then
        // display the graph
        GraphController gc = new TypeGraphController();
        GraphPane gp = new GraphPane(gc, model);

        JGraph jgraph = new JGraph(gp);
        jgraph.setBackground(getBackground());
        jgraph.setBorder(new LineBorder(Color.black));
        jgraph.setMinimumSize(new Dimension(400, 290));
        jgraph.setPreferredSize(new Dimension(400, 290));
        jgraph.setMaximumSize(new Dimension(400, 290));

        // nodes, with user object set to the actor
        Object nAny = model.createNode(BaseType.UNKNOWN);
        Object nInt = model.createNode(BaseType.INT);
        Object nDouble = model.createNode(BaseType.DOUBLE);
        Object nComplex = model.createNode(BaseType.COMPLEX);
        Object nString = model.createNode(BaseType.STRING);
        Object nGeneral = model.createNode(BaseType.GENERAL);
        Object nBoolean = model.createNode(BaseType.BOOLEAN);
        Object nObject = model.createNode(BaseType.OBJECT);
        Object nScalar = model.createNode(BaseType.SCALAR);
        Object nLong = model.createNode(BaseType.LONG);

        gc.addNode(nGeneral, 230, 30);
        gc.addNode(nScalar, 120, 80);
        gc.addNode(nString, 170, 55);
        gc.addNode(nComplex, 90, 125);
        gc.addNode(nDouble, 90, 170);
        gc.addNode(nLong, 170, 140);
        gc.addNode(nInt, 120, 220);
        gc.addNode(nBoolean, 250, 120);
        gc.addNode(nObject, 340, 140);
        gc.addNode(nAny, 230, 260);

        Object e;
        e = model.createEdge(null);
	gc.addEdge(e, nObject, nAny);

        e = model.createEdge(null);
	gc.addEdge(e, nGeneral, nObject);

	e = model.createEdge(null);
	gc.addEdge(e, nGeneral, nString);

	e = model.createEdge(null);
	gc.addEdge(e, nString, nBoolean);

	e = model.createEdge(null);
	gc.addEdge(e, nBoolean, nAny);

	e = model.createEdge(null);
	gc.addEdge(e, nString, nScalar);

        e = model.createEdge(null);
	gc.addEdge(e, nScalar, nLong);

        e = model.createEdge(null);
	gc.addEdge(e, nLong, nInt);

        e = model.createEdge(null);
	gc.addEdge(e, nInt, nAny);

        e = model.createEdge(null);
	gc.addEdge(e, nDouble, nInt);

        e = model.createEdge(null);
	gc.addEdge(e, nComplex, nDouble);

	e = model.createEdge(null);
	gc.addEdge(e, nScalar, nComplex);

        return jgraph;
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

    // The director.
    private SDFDirector _director;

    // The relation that connects to the display.
    private Relation _displayRelation;

    // Actors.
    private Expression _expr;
    private Ramp _ramp1, _ramp2;
    private SequencePlotter _plotter;
    private Display _display;

    // The JGraph where we display the type lattice.
    private JGraph _jgraph = null;

    // The pane displaying the trace
    private TracePane _tracePane;

    // The canvas displaying the trace
    private JCanvas _traceCanvas;

    // The start time for the trace
    private long _counter = 0;

    // The current element of each state;
    private TraceModel.Element _currentElement[];

    // Ports of expression actor.
    private TypedIOPort _exprInput1, _exprInput2;

    // FIXME: This is a copy of the _toplevel defined in the super class.
    // The copy is needed for this class to compile under jdk1.2. For some
    // reason, jdk1.2 does not allow inner class to access _toplevel, but
    // jdk1.3 does.
    private CompositeActor _toplevelCopy = _toplevel;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    public class TypeGraphController extends SimpleGraphController {

	/** The selection interactor for drag-selecting nodes
	 */
	private SelectionDragger _selectionDragger;

	/**
	 * Create a new basic controller with default
	 * node and edge interactors.
	 */
	public TypeGraphController() {
	    // The interactors attached to nodes and edges
	    setNodeController(new NodeController(this));
	    setEdgeController(new EdgeController(this));
            getNodeController().setNodeRenderer(new TypeRenderer(this));
            getEdgeController().setEdgeRenderer(new LineRenderer());
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

	// The graph controller
	private GraphController _controller;

	public TypeRenderer(GraphController controller) {
	    _controller = controller;
	}

        // Return the rendered visual representation of this node.
        public Figure render(Object n) {
	    Object typeObj = _controller.getGraphModel().getSemanticObject(n);

	    // Create a colored circle
            BasicFigure figure = new BasicEllipse(0, 0, _size, _size);

            // Get the color and label
            Color color = Color.black;
            String label = "UNKNOWN";
            if (typeObj == BaseType.UNKNOWN) {
                color = Color.black;
                label = "Any";
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

    // LineRenderer draws edges as simple lines
    private class LineRenderer implements EdgeRenderer {
        /**
         * Render a visual representation of the given edge.
         */
        public Connector render(Object edge, Site tailSite, Site headSite) {
            StraightConnector c = new StraightConnector(tailSite, headSite);
            c.setUserObject(edge);
            return c;
        }
    }

    // The local listener class
    private class PortTypeListener implements TypeListener {

        public void typeChanged(final TypeEvent event) {
            ptolemy.data.type.Type newtype = event.getNewType();
            String typeString = newtype.toString();
            final TypedIOPort port = event.getPort();
            
            // Construct the name of the type label from the name
            // of the port relative to the top level.
            String portName = port.getName(_toplevelCopy);
            String labelName = portName.replace('.', '_');
            Attribute label = _toplevelCopy.getAttribute(labelName);
            if (label != null) {
                Configurable config = (Configurable)
                        label.getAttribute("_iconDescription");
                if (config != null) {
                    String moml = "<property name="
                            + "\"_iconDescription\" "
                            + "class=\"ptolemy.kernel.util"
                            + ".SingletonConfigurableAttribute\">"
                            + "<configure><svg><text x=\"20\" "
                            + "style=\"font-size:14; font-family:sanserif; "
                            + "fill:red\" y=\"20\">"
                            + typeString
                            + "</text></svg></configure></property>";

                    label.requestChange(new MoMLChangeRequest(
                           this, label, moml));
                }
            }

            // Update the trace.
            // This has to be done in the swing event thread.
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    ptolemy.data.type.Type typeObj = event.getNewType();
                    int color = 7;
                    if (typeObj == BaseType.UNKNOWN) {
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

                    int id = 0;
                    if (port == _ramp1.output) {
                        id = 0;
                    } else if (port == _ramp2.output) {
                        id = 1;
                    } else if (port == _exprInput1) {
                        id = 2;
                    } else if (port == _exprInput2) {
                        id = 3;
                    } else if (port == _expr.output) {
                        id = 4;
                    } else if (port == _plotter.input) {
                        id = 5;
                    } else if (port == _display.input) {
                        id = 6;
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
                    new TraceModel.Element(currentTime, currentTime+1, color);
                    element.closure = TraceModel.Element.OPEN_END;
                    trace.add(element);

                    // Close the current element
                    TraceModel.Element current = _currentElement[id];
                    current.closure = 0;
                    // Update all elements
                    int msize = model.size();
                    TraceModel.Element temp[] = new TraceModel.Element[msize];
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
                }
            });
        }
    }
}
