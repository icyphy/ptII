/* An applet that uses Ptolemy II CT and DE domains.

 Copyright (c) 1998-1999 The Regents of the University of California.
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

package ptolemy.domains.ct.demo.Lorenz;

import java.awt.*;
import java.awt.event.*;
import ptolemy.domains.de.kernel.*;
import ptolemy.domains.de.lib.*;
import ptolemy.domains.ct.kernel.*;
import ptolemy.domains.ct.kernel.util.*;
import ptolemy.domains.ct.lib.*;
import ptolemy.actor.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.plot.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// LorenzApplet
/**
@author Jie Liu
@version $Id$
*/
public class LorenzApplet extends CTApplet {

    public static final boolean DEBUG = false;

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Initialize the applet.
     */
    public void init() {

        super.init();
        // Initialization
        _stopTimeBox = new TextField("50.0", 10);
        _sigmaBox = new TextField("10.0", 10);
        _lamdaBox = new TextField("25.0", 10);
        _bBox = new TextField("-2.0", 10);
        _currentTimeLabel = new Label("Current time = 0.0     ");
        _goButton = new Button("Go");

        // The applet has two panels, stacked vertically
        setLayout(new BorderLayout());
        Panel appletPanel = new Panel();
        appletPanel.setLayout(new GridLayout(1, 1));
        add(appletPanel, "Center");

        Plot ctPanel = new Plot();
        appletPanel.add(ctPanel);

        // Adding a control panel in the main panel.
        Panel controlPanel = new Panel();
        add(controlPanel, "South");
        // Done adding a control panel.

        // Adding simulation parameter panel in the control panel.
        Panel simulationParam = new Panel();
        simulationParam.setLayout(new GridLayout(2, 3));
        controlPanel.add(simulationParam);
        // Done adding simulation parameter panel.

        // Adding current time in the sub panel.
        simulationParam.add(_currentTimeLabel);
        // Done adding average wait time.

        // Adding sample time (minimum service time) in the simulation panel
        Panel sigmaPanel = new Panel();
        simulationParam.add(sigmaPanel);
        sigmaPanel.add(new Label("Sigma:"));
        sigmaPanel.add(_sigmaBox);
        // done adding sigma

        // Adding Stop time in the simulation panel.
        Panel subSimul = new Panel();
        simulationParam.add(subSimul);
        subSimul.add(new Label("Stop time:"));
        subSimul.add(_stopTimeBox);
        // Done adding stop time.

        // Adding lamda in the simulation panel
        Panel lamdaPanel = new Panel();
        simulationParam.add(lamdaPanel);
        lamdaPanel.add(new Label("Lamda"));
        lamdaPanel.add(_lamdaBox);
        // done adding lamda

        // Adding b in the simulation panel
        Panel bPanel = new Panel();
        simulationParam.add(bPanel);
        bPanel.add(new Label("-b"));
        bPanel.add(_bBox);
        // done adding b

        // Adding go button in the control panel.
        ctPanel.add(_goButton);
        _goButton.addActionListener(new GoButtonListener());

        //System.out.println("Construct ptII");
        // Creating the topology.
        try {
            
            // Set up the top level composite actor, director and manager
            TypedCompositeActor sys = _toplevel;
            sys.setName("LorenzSystem");
            _dir = new CTSingleSolverDirector(sys, "CTSingleSolverDirector");
            // FIXME: temporary
            _dir.addDebugListener(new StreamListener());

            //_manager = new Manager("Manager");
            _manager.addExecutionListener(new MyExecutionListener());
            //sys.setManager(_manager);
            _thismanager = _manager;
            // ---------------------------------
            // Create the actors.
            // ---------------------------------

            // CTActors

            CTConst LAMDA = new CTConst(sys, "LAMDA");
            CTGain SIGMA = new CTGain(sys, "SIGMA");
            CTGain B = new CTGain(sys, "B");

            CTAdd ADD1 = new CTAdd(sys, "Add1");
            CTAdd ADD2 = new CTAdd(sys, "Add2");
            CTAdd ADD3 = new CTAdd(sys, "Add3");
            CTAdd ADD4 = new CTAdd(sys, "Add4");

            CTMultiply MULT1 = new CTMultiply(sys, "MULT1");
            CTMultiply MULT2 = new CTMultiply(sys, "MULT2");

            CTIntegrator X1 = new CTIntegrator(sys, "IntegratorX1");
            CTIntegrator X2 = new CTIntegrator(sys, "IntegratorX2");
            CTIntegrator X3 = new CTIntegrator(sys, "IntegratorX3");

            CTGain MINUS1 = new CTGain(sys, "MINUS1");
            CTGain MINUS2 = new CTGain(sys, "MINUS2");
            CTGain MINUS3 = new CTGain(sys, "MINUS3");

            CTXYPlot ctPlot = new CTXYPlot(sys, "CTXYPlot", ctPanel);
            String[] ctLegends = {"(x1, x2)"};

            ctPlot.setLegend(ctLegends);

            // CTConnections
            TypedIORelation x1 = new TypedIORelation(sys, "X1");
            TypedIORelation x2 = new TypedIORelation(sys, "X2");
            TypedIORelation x3 = new TypedIORelation(sys, "X3");
            X1.output.link(x1);
            X2.output.link(x2);
            X3.output.link(x3);
            MINUS1.input.link(x1);
            MINUS2.input.link(x2);
            MINUS3.input.link(x3);

            // dx1/dt = sigma*(x2-x1)
            sys.connect(MINUS1.output, ADD1.input);
            ADD1.input.link(x2);
            sys.connect(ADD1.output, SIGMA.input);
            sys.connect(SIGMA.output, X1.input);

            // dx2/dt = (lamda-x3)*x1-x2
            sys.connect(LAMDA.output, ADD2.input);
            sys.connect(MINUS3.output, ADD2.input);
            sys.connect(ADD2.output, MULT1.input);
            MULT1.input.link(x1);
            sys.connect(MULT1.output, ADD3.input);
            sys.connect(MINUS2.output, ADD3.input);
            sys.connect(ADD3.output, X2.input);

            // dx3/dt = x1*x2-b*x3
            MULT2.input.link(x1);
            MULT2.input.link(x2);
            B.input.link(x3);
            sys.connect(MULT2.output, ADD4.input);
            sys.connect(B.output, ADD4.input);
            sys.connect(ADD4.output, X3.input);

            ctPlot.inputX.link(x1);
            ctPlot.inputY.link(x2);

            //System.out.println("Parameters");
            // CT Director parameters
            Parameter initstep =
                (Parameter)_dir.getAttribute("InitialStepSize");
            initstep.setToken(new DoubleToken(0.01));

            Parameter minstep =
                (Parameter)_dir.getAttribute("MinimumStepSize");
            minstep.setToken(new DoubleToken(1e-6));

            /*Parameter solver1 =
                (Parameter)_dir.getAttribute("BreakpointODESolver");
            StringToken token1 = new StringToken(
                    "ptolemy.domains.ct.kernel.solver.BackwardEulerSolver");
            solver1.setToken(token1);
            */

            Parameter solver2 =
                (Parameter)_dir.getAttribute("ODESolver");
            StringToken token2 = new StringToken(
                    "ptolemy.domains.ct.kernel.solver.ExplicitRK23Solver");
            solver2.setToken(token2);

            // CTActorParameters
            Parameter xi1 = (Parameter)X1.getAttribute("InitialState");
            xi1.setToken(new DoubleToken(1.0));

            Parameter xi2 = (Parameter)X2.getAttribute("InitialState");
            xi2.setToken(new DoubleToken(1.0));

            Parameter xi3 = (Parameter)X3.getAttribute("InitialState");
            xi3.setToken(new DoubleToken(1.0));

            Parameter m1 = (Parameter)MINUS1.getAttribute("Gain");
            m1.setToken(new DoubleToken(-1.0));

            Parameter m2 = (Parameter)MINUS2.getAttribute("Gain");
            m2.setToken(new DoubleToken(-1.0));

            Parameter m3 = (Parameter)MINUS3.getAttribute("Gain");
            m3.setToken(new DoubleToken(-1.0));

            //XYPlot ranges
            Parameter xmin = (Parameter)ctPlot.getAttribute("X_Min");
            xmin.setToken(new DoubleToken(-25.0));

            Parameter xmax = (Parameter)ctPlot.getAttribute("X_Max");
            xmax.setToken(new DoubleToken(25.0));

            Parameter ymin = (Parameter)ctPlot.getAttribute("Y_Min");
            ymin.setToken(new DoubleToken(-25.0));

            Parameter ymax = (Parameter)ctPlot.getAttribute("Y_Max");
            ymax.setToken(new DoubleToken(25.0));

            // Setting up parameters.
            _paramSigma = (Parameter)SIGMA.getAttribute("Gain");
            _paramLamda = (Parameter)LAMDA.getAttribute("Value");
            _paramB = (Parameter)B.getAttribute("Gain");
            _paramStopT = (Parameter)_dir.getAttribute("StopTime");
        } catch (Exception ex) {
            report("Setup failed: ",  ex);
        }
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    // The thread that runs the simulation.
    private boolean _isSimulationRunning;

    // FIXME: Under jdk 1.2, the following can (and should) be private
    private CTSingleSolverDirector _dir;
    private Manager _thismanager;

    private TextField _stopTimeBox;
    private TextField _lamdaBox;
    private TextField _bBox;
    private TextField _sigmaBox;
    private Label _currentTimeLabel;
    private double _stopTime = 100.0;
    private Button _goButton;

    private Parameter _paramLamda;
    private Parameter _paramSigma;
    private Parameter _paramB;
    private Parameter _paramStopT;

    //private Label _currentTimeLabel;
    private boolean _isSimulationPaused = false;

    ////////////////////////////////////////////////////////////////////////
    ////                         private methods                        ////


    //////////////////////////////////////////////////////////////////////////
    ////                       inner classes                              ////

    /* Show simulation progress.
     */
    private class CurrentTimeThread extends Thread {
        public void run() {
            while (_isSimulationRunning) {
                // get the current time from director.
                double currenttime = _dir.getCurrentTime();
                _currentTimeLabel.setText("Current time = "+currenttime);
                try {
                    sleep(500);
                } catch (InterruptedException e) {}
            }
        }
    }

    private class MyExecutionListener extends DefaultExecutionListener {
        public void executionFinished(Manager manager) {
            super.executionFinished(manager);
            _isSimulationRunning = false;
        }

    }

    private class GoButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            if (_isSimulationRunning) {
                // report("Simulation still running.. hold on..");
                return;
            }

            try {
                // The simulation is started non-paused (of course :-) )
                _isSimulationPaused = false;

                // Set the stop time.
                try {
                    Double tmp = Double.valueOf( _stopTimeBox.getText());
                    _paramStopT.setToken(new DoubleToken(tmp.doubleValue()));
                } catch (NumberFormatException ex) {
                    report("Invalid stop time: ", ex);
                    return;
                }
                // set lamda
                try {
                    Double tmp = Double.valueOf(_lamdaBox.getText());
                    _paramLamda.setToken(new DoubleToken(tmp.doubleValue()));
                } catch (NumberFormatException ex) {
                    report("Invalid Lamda value: ", ex);
                    return;
                }
                // set b.
                try {
                    Double tmp = Double.valueOf(_bBox.getText());
                    _paramB.setToken(new DoubleToken(tmp.doubleValue()));
                } catch (NumberFormatException ex) {
                    report("Invalid B value: ", ex);
                    return;
                }
                // set sigma.
                try {
                    Double tmp = Double.valueOf(_sigmaBox.getText());
                    _paramSigma.setToken(new DoubleToken(tmp.doubleValue()));
                } catch (NumberFormatException ex) {
                    report("Invalid SigmaValue: ", ex);
                    return;
                }

                // Start the CurrentTimeThread.
                Thread ctt = new CurrentTimeThread();
                _isSimulationRunning = true;
                ctt.start();
                _thismanager.startRun();

            } catch (Exception e) {
                report("Error: ",  e);
            }

        }
    }
}



