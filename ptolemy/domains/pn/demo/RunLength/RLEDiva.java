/* An applet that performs run-length encoding on an image and uses diva
for visualization

 Copyright (c) 1997-1999 The Regents of the University of California.
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
*/

package ptolemy.domains.pn.demo.RunLength;


import diva.graph.compat.*;
import diva.graph.model.*;
import diva.graph.layout.*;
import diva.canvas.*;
import diva.canvas.toolbox.*;
import diva.util.gui.BasicWindow;
import diva.surfaces.trace.*;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.util.*;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import ptolemy.media.Picture;
import ptolemy.domains.pn.gui.PNApplet;
import ptolemy.domains.pn.kernel.*;
import ptolemy.domains.pn.kernel.event.*;
import ptolemy.domains.pn.lib.*;
import ptolemy.actor.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// RLEncodingApplet
/**
An applet that performs run-length encoding on an image and provides
a visualization of the processes in various states. This requires a jdk1.2
compatible browser or requires a jdk1.2 plugin.
@author Mudit Goel
@version $Id$
*/

public class RLEDiva extends PNApplet implements Runnable {

    /** Describe the applet parameters.
     *  @return An array describing the applet parameters.
     */
    public String[][] getParameterInfo() {
        String basepinfo[][] = super.getParameterInfo();
        String[][] pinfo = new String[basepinfo.length + 1][];
        for (int i = 0; i < basepinfo.length; i++) {
            pinfo[i] = basepinfo[i];
        }
        String newinfo[][] = {
            {"imageurl", "url", "the URL of the ppm image to process"}
        };
        pinfo[basepinfo.length] = newinfo[0];
        return pinfo;
    }

    /** Initialize the applet.
     */
    public void init() {

        // Process the background parameter.
        super.init();

	setLayout(new BorderLayout(15, 15));
	add(_createRunControls(3), BorderLayout.NORTH);

        Panel displayPanel = new Panel();
        displayPanel.setLayout(new BorderLayout(15, 15));
        displayPanel.setSize(283, 94);

        Panel originalPanel = new Panel();
	//FIXME: HACK HACK
        originalPanel.setSize(134, 94);
        displayPanel.add(originalPanel, "West");

        Panel compressedPanel = new Panel();
	//FIXME: HACK HACK
        compressedPanel.setSize(134, 94);
        displayPanel.add(compressedPanel, "East");

	add(displayPanel, BorderLayout.SOUTH);

        // Construct the Ptolemy kernel topology
        constructPtolemyModel();
	a8.setPanel(compressedPanel);
	a7.setPanel(originalPanel);

	divapanel = new JPanel(new BorderLayout());
	divapanel.setSize(new Dimension(800, 615));
	divapanel.setBackground(Color.white);
	add(divapanel, BorderLayout.CENTER);

        // Construct the graph representing the PN topology
	_gmodel = constructThreadGraph();
        final GraphModel model = _gmodel;

	//Display the graph
	try {
	    SwingUtilities.invokeAndWait(new Runnable (){
		public void run() {
		    displayGraph(jgraph, model);
		}
	    });
        } catch(Exception ex) {
	    ex.printStackTrace();
	    System.exit(0);
	}

        final TraceModel traceModel = constructTraceModel();

        // Display the trace
	try {
	    SwingUtilities.invokeAndWait(new Runnable (){
		public void run() {
		    displayTrace(traceModel);
		}
	    });
        } catch(Exception ex) {
	    ex.printStackTrace();
	    System.exit(0);
	}

	//validate();

        // Add the process state listener
	//FIXME: HACK HACK HACK
	listener = new StateListener((GraphPane) jgraph.getCanvasPane());
        _director.addProcessListener(listener);


        // Run the model
	System.out.println("Connections made");
        Parameter p = (Parameter)_director.getAttribute(
		"Initial_queue_capacity");
        try {
            p.setToken(new IntToken(10));
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(ex.toString());
        }
	return;
    }


    /**  Construct the graph representing the PN topology.
     * This is sort of bogus because it's totally hird-wired,
     * but it will do for now...
     */
    public GraphModel constructThreadGraph() {
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

        _nodeMap.put(a1, n1);
        _nodeMap.put(a2, n2);
        _nodeMap.put(a3, n3);
        _nodeMap.put(a4, n4);
        _nodeMap.put(a5, n5);
        _nodeMap.put(a6, n6);
        _nodeMap.put(a7, n7);
        _nodeMap.put(a8, n8);

        // Edges
        model.createEdge(n1, n2);
        model.createEdge(n2, n3);
        model.createEdge(n3, n4);
        model.createEdge(n4, n5);
        model.createEdge(n5, n6);

        model.createEdge(n1, n7);
        model.createEdge(n5, n8);

        return model;
    }

    /** Construct the Ptolemy system
     */
    public void constructPtolemyModel() {
        try {
            a1 = new PNImageSource(_toplevel, "ImageReader");
            a2 = new MatrixUnpacker(_toplevel, "Unpacker");
            a3 = new RLEncoder(_toplevel, "Encoder");
            a4 = new RLDecoder(_toplevel, "Decoder");
            a5 = new MatrixPacker(_toplevel, "Packer");
            a6 = new PNImageSink(_toplevel, "Sink");
            Parameter p1 = (Parameter)a6.getAttribute("Output_file");
            p1.setToken(new StringToken("/tmp/image.pbm"));
            a7 = new ImageDisplay(_toplevel, "InputDisplay");
            p1 = (Parameter)a7.getAttribute("FrameName");
            p1.setToken(new StringToken("InputImage"));
            a8 = new ImageDisplay(_toplevel, "OutputDisplay");
            p1 = (Parameter)a8.getAttribute("FrameName");
            p1.setToken(new StringToken("OutputImage"));

            IOPort portin = (IOPort)a1.getPort("output");
            IOPort portout = (IOPort)a2.getPort("input");
            ComponentRelation rel = _toplevel.connect(portin, portout);
            (a7.getPort("image")).link(rel);

            portin = (IOPort)a2.getPort("output");
            portout = (IOPort)a3.getPort("input");
            _toplevel.connect(portin, portout);

            portin = (IOPort) a2.getPort("dimensions");
            portout = (IOPort)a3.getPort("dimensionsIn");
            _toplevel.connect(portin, portout);

            portin = (IOPort)a3.getPort("dimensionsOut");
            portout = (IOPort)a4.getPort("dimensionsIn");
            _toplevel.connect(portin, portout);

            portin = (IOPort)a3.getPort("output");
            portout = (IOPort)a4.getPort("input");
            _toplevel.connect(portin, portout);

            portin = (IOPort)a4.getPort("dimensionsOut");
            portout = (IOPort)a5.getPort("dimensions");
            _toplevel.connect(portin, portout);

            portin = (IOPort)a4.getPort("output");
            portout = (IOPort)a5.getPort("input");
            _toplevel.connect(portin, portout);

            portin = (IOPort)a5.getPort("output");
            portout = (IOPort)a6.getPort("input");
            rel = _toplevel.connect(portin, portout);
            (a8.getPort("image")).link(rel);

	    String dataurlspec = getParameter("imageurl");
            if (dataurlspec != null) {
                try {
                    showStatus("Reading data");
                    URL dataurl = new URL(getDocumentBase(), dataurlspec);
                    a1.read(dataurl.openStream());
                    showStatus("Done");
                } catch (MalformedURLException e) {
                    System.err.println(e.toString());
                } catch (FileNotFoundException e) {
                    System.err.println("RLEncodingApplet: file not found: "+e);
                } catch (IOException e) {
                    System.err.println("RLEncodingApplet: error reading"+
                            " input file: " +e);
                }
            }

        }
        catch (Exception e) {
            throw new RuntimeException(e.toString());
        }
        return;
    }


    /**
     * Construct the graph widget with
     * the default constructor (giving it an empty graph),
     * and then set the model once the window is showing.
     */
    public void displayGraph(JGraph g, GraphModel model) {
	divapanel.add(g, BorderLayout.NORTH);
	g.setPreferredSize(new Dimension(800, 300));

        // Make sure we have the right renders and then
        // display the graph
        final GraphPane gp = (GraphPane) g.getCanvasPane();
        final GraphView gv = gp.getGraphView();
        gv.setNodeRenderer(new ThreadRenderer());
        g.setGraphModel(model);

        // Do the layout
	final GraphModel m = model;
	try {
	    SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    LevelLayout staticLayout = new LevelLayout();
		    staticLayout.setOrientation(LevelLayout.HORIZONTAL);
		    staticLayout.layout(gv, m.getGraph());
		    gp.repaint();
		}
	    });
	} catch (Exception e) {
	    System.out.println(e);
	}
    }

    /**
     * Construct the trace model.
     */
    public TraceModel constructTraceModel() {
        TraceModel traceModel = new TraceModel();
	TraceModel.Trace t;

	t = new TraceModel.Trace();
	t.setUserObject("ImageReader");
        traceModel.addTrace("ImageReader", t);

	t = new TraceModel.Trace();
	t.setUserObject("Unpacker");
        traceModel.addTrace("Unpacker", t);

	t = new TraceModel.Trace();
	t.setUserObject("Encoder");
        traceModel.addTrace("Encoder", t);

	t = new TraceModel.Trace();
	t.setUserObject("Decoder");
        traceModel.addTrace("Decoder", t);

	t = new TraceModel.Trace();
	t.setUserObject("Packer");
        traceModel.addTrace("Packer", t);

	t = new TraceModel.Trace();
	t.setUserObject("Sink");
        traceModel.addTrace("Sink", t);

	t = new TraceModel.Trace();
	t.setUserObject("InputDisplay");
        traceModel.addTrace("InputDisplay", t);

	t = new TraceModel.Trace();
	t.setUserObject("OutputDisplay");
        traceModel.addTrace("OutputDisplay", t);
        return traceModel;
    }

    /**
     *  Construct the trace display.
     */
    public void displayTrace(TraceModel traceModel) {
        _tracePane = new TracePane();
        JCanvas traceWidget = new JCanvas(_tracePane);
        divapanel.add(traceWidget, BorderLayout.CENTER);
	traceWidget.setPreferredSize(new Dimension(800, 300));

        // Configure the view
        TraceView traceView = _tracePane.getTraceView();
	traceView.setTimeScale(0.02);
	traceView.setLayout(10, 10, 500, 20, 15);
	traceView.setTraceModel(traceModel);
    }


    /** Run the simulation.
     */
    public void run() {
        try {
            _manager.run();
            System.out.println("Bye World\n");
            return;
        } catch (Exception ex) {
            System.err.println("Run failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }


    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** In addition to creating the buttons provided by the base class,
     *  if the number of iterations has not been specified, then create
     *  a dialog box for that number to be entered.  The panel containing
     *  the buttons and the entry box is returned.
     *  @param numbuttons The number of buttons to create.
     */
    protected JPanel _createRunControls(int numbuttons) {
        JPanel controlPanel = super._createRunControls(numbuttons);

        if (numbuttons > 2) {
            JButton layout = new JButton("Layout");
            controlPanel.add(layout);
            layout.addActionListener(new LayoutListener());
        }

        return controlPanel;
    }

    /** Execute the system.
     */
    protected void _go() throws IllegalActionException {
        if (_isSimulationRunning) {
            System.out.println("Simulation still running.. hold on..");
            return;
        }

        RLEDiva.this.listener.setStartTime(System.currentTimeMillis());
        super._go();
    }

    /** Stop the simulation of the system
     */
    public void _stop() {
	_isSimulationRunning = false;
        _manager.terminate();
    }


    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

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
        // The pending start times
        private double _startTime[] = new double[9];

        // The absolute start time
        private long _start = -1;

        // The current element of each state;
        private TraceModel.Element _currentElement[];

        /* Create a listener on the given graph pane
         */
        public StateListener(GraphPane pane) {
            _graphPane = pane;

            // Initial elements of all traces
            TraceModel model = _tracePane.getTraceModel();
            _currentElement = new TraceModel.Element[model.size()];

            for (int i = 0; i < model.size(); i++ ) {
                TraceModel.Trace trace = model.getTrace(i);

                final TraceModel.Element element = new TraceModel.Element(
                        0, 1, 3);
                element.closure = TraceModel.Element.OPEN_END;
                trace.add(element);
                _currentElement[i] = element;

                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            _tracePane.getTraceView().drawTraceElement(element);
                        }
                    });
                }
                catch (Exception e) {
                    System.out.println(e);
                }
            }
        }

	/** Set the starting time for the trace graph in milliseconds
	 *  @time the current system time in milliseconds
	 */
	public void setStartTime(long time) {
	    _start = time;
	}

        /** Respond to a state changed event.
         */
        public void processStateChanged(PNProcessEvent event) {
	    System.out.println("Still processing");

            final int state = event.getCurrentState();
	    int cte = -1;
	    if (state == PNProcessEvent.PROCESS_BLOCKED) {
		cte = event.getBlockingCause();
	    }
	    final int cause = cte;
            Actor actor = event.getActor();

            // Get the corresponding graph node and its figure
            Node node = (Node) _nodeMap.get(actor);
            LabelWrapper wrapper = (LabelWrapper)
                _graphPane.getGraphView().getNodeFigure(node);
            final BasicFigure figure = (BasicFigure)
                wrapper.getChild();

            // Color it!
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        switch (state) {
                        case PNProcessEvent.PROCESS_BLOCKED:
			    if (cause == PNProcessEvent.BLOCKED_ON_READ) {
				figure.setFillPaint(Color.red);
			    } else {
				figure.setFillPaint(Color.magenta);
			    }
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
            } catch (Exception e) {
		System.out.println(e.toString());
	    }
	    //System.out.println("In state changed after initial stuff**8**");

            // Get the trace and element figure
            ComponentEntity ce = (ComponentEntity) actor;
            String name = ce.getName();
            TraceModel model = _tracePane.getTraceView().getTraceModel();
            TraceModel.Trace trace = model.getTrace(name);
            int id = trace.getID();
            // OK, this is nasty, but get the color
            // "state" from the process state
            int colorState = 3;
            switch (state) {
            case PNProcessEvent.PROCESS_BLOCKED:
		if (cause == PNProcessEvent.BLOCKED_ON_READ) {
		    colorState = 0;
		} else {
		    colorState = 5;
		}
		break;

            case PNProcessEvent.PROCESS_FINISHED:
                colorState = 7;
                break;

            case PNProcessEvent.PROCESS_PAUSED:
                colorState = 2;
                break;

            case PNProcessEvent.PROCESS_RUNNING:
                colorState = 3;
                break;
            }

	    //System.out.println("In state changed and getting time ???");
            // Create the new element
            double currentTime = (double) (System.currentTimeMillis() - _start);
            final TraceModel.Element element = new TraceModel.Element(
                    currentTime, currentTime+1, colorState);
            element.closure = TraceModel.Element.OPEN_END;
            trace.add(element);

            // Close the current element
            final TraceModel.Element current = _currentElement[id];
            current.closure = 0;

            // Update all elements
            final int msize = model.size();
            final TraceModel.Element temp[] = new TraceModel.Element[msize];
            for (int i = 0; i < msize; i++) {
                _currentElement[i].stopTime = currentTime;
                temp[i] = _currentElement[i];
            }

            try {
		SwingUtilities.invokeAndWait(new Runnable() {
		    public void run() {
			TraceView v = _tracePane.getTraceView();
			for (int i = 0; i < msize; i++) {
			    v.updateTraceElement(temp[i]);
			}
			v.drawTraceElement(element);
		    }
		});
	    }
	    catch (Exception e) {
		System.out.println(e.toString());
	    }

            // Update
            _currentElement[id] = element;
        }

        /** Respond to a process finished event.
         */
        public void processFinished(PNProcessEvent event) {
            processStateChanged(event);
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
            //System.out.println("Actor " + actor + " has label " + label);
            LabelWrapper w = new LabelWrapper(f, label);
            w.setAnchor(SwingConstants.SOUTH);
            w.getLabel().setAnchor(SwingConstants.NORTH);
            return w;
        }
    }



    ///////////////////////////////////////////////////////////////////
    //// ExecutionListener

    //     private class MyExecutionListener extends DefaultExecutionListener {
    //         public void executionFinished(ExecutionEvent e) {
    //             super.executionFinished(e);
    //             _isSimulationRunning = false;
    //         }

    //     }


    ///////////////////////////////////////////////////////////////////
    //// LayoutListener

    private class LayoutListener implements ActionListener {
	public void actionPerformed(ActionEvent evt) {
	    final GraphPane gp = (GraphPane)RLEDiva.this.jgraph.getCanvasPane();
	    final GraphView gv = gp.getGraphView();
	    final GraphModel m = _gmodel;
	    try {
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
			LevelLayout staticLayout = new LevelLayout();
			staticLayout.setOrientation(LevelLayout.HORIZONTAL);
			staticLayout.layout(gv, m.getGraph());
			gp.repaint();
		    }
		});
	    } catch (Exception e) {
		System.out.println(e);
	    }
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The mapping from Ptolemy actors to graph nodes
     */
    private HashMap _nodeMap = new HashMap();

    // FIXME: are these supposed to be private?  If so, they need _ 

    /** Panel that display the diva graphs */
    JPanel divapanel;

    /** The JGraph where we display stuff
     */
    JGraph jgraph = new JGraph();

    /** Listener for traces and graph layout */
    StateListener listener;

    GraphModel _gmodel;

    /** The pane displaying the trace
     */
    private TracePane _tracePane;

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

    //Manager manager;
}
