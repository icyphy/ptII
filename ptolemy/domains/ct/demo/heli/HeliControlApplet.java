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
*/

package ptolemy.domains.ct.demo.heli;

import java.awt.*;
import java.awt.event.*;
import ptolemy.domains.de.kernel.*;
import ptolemy.domains.de.lib.*;
import ptolemy.domains.ct.kernel.*;
import ptolemy.domains.ct.lib.*;
import ptolemy.actor.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.plot.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
////  HeliControlApplet
/**
An applet that uses Ptolemy II DE domain.

@author Jie Liu
@version $Id$
*/
public class HeliControlApplet extends ptolemy.actor.util.PtolemyApplet {

    public  boolean DEBUG = false;

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Initialize the applet.
     */
    public void init() {

        super.init();
        // Initialization
        _stopTimeBox = new TextField("50.0", 10);
        _alphaPBox = new TextField("500.0 650.0 395.0 121.0 17.8", 10);
        _alphaVBox = new TextField("100.0 110.0 57.0 12.80", 10);
        _alphaABox = new TextField("20.0 18.0 7.80", 10);
        _currentTimeLabel = new Label("Current time = 0.0     ");
        _goButton = new Button("Go");
        _actionButton = new Button("Action");

        // The applet has tow panels, stacked vertically
        setLayout(new BorderLayout());
        Panel appletPanel = new Panel();
        appletPanel.setLayout(new GridLayout(1,1));
        add(appletPanel, "Center");
        
        Plot ctPanel = new Plot();
        appletPanel.add(ctPanel);

        // Adding a control panel in the main panel.
        Panel controlPanel = new Panel();
        add(controlPanel, "South");
        // Done adding a control panel.

        // Adding simulation parameter panel in the control panel.
        Panel simulationParam = new Panel();
        simulationParam.setLayout(new GridLayout(2,3));
        controlPanel.add(simulationParam);
        // Done adding simulation parameter panel.

        // Adding current time in the sub panel.
        simulationParam.add(_currentTimeLabel);
        // Done adding average wait time.

        // Adding sample time (minimum service time) in the simulation panel
        Panel alphaPPanel = new Panel();
        simulationParam.add(alphaPPanel);
        alphaPPanel.add(new Label("alphaP:"));
        alphaPPanel.add(_alphaPBox);
        // done adding sigma

        // Adding Stop time in the simulation panel.
        Panel subSimul = new Panel();
        simulationParam.add(subSimul);
        subSimul.add(new Label("Stop time:"));
        subSimul.add(_stopTimeBox);
        // Done adding stop time.

        // Adding lamda in the simulation panel
        Panel alphaVPanel = new Panel();
        simulationParam.add(alphaVPanel);
        alphaVPanel.add(new Label("alphaV:"));
        alphaVPanel.add(_alphaVBox);
        // done adding lamda

        // Adding b in the simulation panel
        Panel alphaAPanel = new Panel();
        simulationParam.add(alphaAPanel);
        alphaAPanel.add(new Label("alphaA"));
        alphaAPanel.add(_alphaABox);
        // done adding b
        
        // Adding go button in the control panel.
        ctPanel.add(_goButton);
        _goButton.addActionListener(new GoButtonListener());

        // Adding action button in the control panel.
        ctPanel.add(_actionButton);
        _actionButton.addActionListener(new ActionButtonListener());
        
        //System.out.println("Construct ptII");
        // Creating the topology.
        try {
            // Set up the top level composite actor, director and manager
            TypedCompositeActor sys = new TypedCompositeActor();
            sys.setName("HeliControlSystem");
            _dir = new CTMultiSolverDirector("CTSingleSolverDirector");
            sys.setDirector(_dir);
            _manager = new Manager("Manager");
            _manager.addExecutionListener(new MyExecutionListener());
            sys.setManager(_manager);

            //_dir.setVERBOSE(true);
            //_dir.setDEBUG(true);

            // ---------------------------------
            // Create the composite actors.
            // ---------------------------------
            CTCompositeActor sub = new CTCompositeActor(sys, "Linearizers");
            TypedIOPort subinPx = new TypedIOPort(sub, "inputPx");
            subinPx.setInput(true);
            subinPx.setOutput(false);
            TypedIOPort subinDPx = new TypedIOPort(sub, "inputDPx");
            subinDPx.setInput(true);
            subinDPx.setOutput(false);
            TypedIOPort subinDDPx = new TypedIOPort(sub, "inputDDPx");
            subinDDPx.setInput(true);
            subinDDPx.setOutput(false);
            TypedIOPort subinD3Px = new TypedIOPort(sub, "inputD3Px");
            subinD3Px.setInput(true);
            subinD3Px.setOutput(false);
            TypedIOPort subinD4Px = new TypedIOPort(sub, "inputD4Px");
            subinD4Px.setInput(true);
            subinD4Px.setOutput(false);
            
            TypedIOPort subinPz = new TypedIOPort(sub, "inputPz");
            subinPz.setInput(true);
            subinPz.setOutput(false);
            TypedIOPort subinDPz = new TypedIOPort(sub, "inputDPz");
            subinDPz.setInput(true);
            subinDPz.setOutput(false);
            TypedIOPort subinDDPz = new TypedIOPort(sub, "inputDDPz");
            subinDDPz.setInput(true);
            subinDDPz.setOutput(false);
            TypedIOPort subinD3Pz = new TypedIOPort(sub, "inputD3Pz");
            subinD3Pz.setInput(true);
            subinD3Pz.setOutput(false);
            TypedIOPort subinD4Pz = new TypedIOPort(sub, "inputD4Pz");
            subinD4Pz.setInput(true);
            subinD4Pz.setOutput(false);
            
            TypedIOPort suboutVx = new TypedIOPort(sub, "outputVx");
            suboutVx.setInput(true);
            suboutVx.setOutput(false);
            TypedIOPort suboutVz = new TypedIOPort(sub, "outputVz");
            suboutVz.setInput(true);
            suboutVz.setOutput(false);

            // ---------------------------------
            // Create the actors.
            // ---------------------------------

            // CTActors

            HelicopterActor heli = new HelicopterActor(sys, "Helicopter");
            ControllerActor ctrl = new ControllerActor(sys, "Controller");
            XZHigherDerivatives higher = new XZHigherDerivatives(sys, 
                    "XZHigherDerivatives");

            //CTButtonEvent button = new CTButtonEvent(sys, "Button");
            HoverLinearizer hover = new HoverLinearizer(sub, "Hover");
            
            CTIntegrator Px = new CTIntegrator(sys, "IntegratorPx");
            CTIntegrator DPx = new CTIntegrator(sys, "IntegratorDPx");

            CTIntegrator Pz = new CTIntegrator(sys, "IntegratorPz");
            CTIntegrator DPz = new CTIntegrator(sys, "IntegratorDPz");

            CTIntegrator Th = new CTIntegrator(sys, "IntegratorTh");
            CTIntegrator DTh = new CTIntegrator(sys, "IntegratorDTh");

            CTIntegrator Tm = new CTIntegrator(sys, "IntegratorTm");
            CTIntegrator DTm = new CTIntegrator(sys, "IntegratorDTm");
            CTIntegrator DDTm = new CTIntegrator(sys, "IntegratorDDTm");

            CTIntegrator A = new CTIntegrator(sys, "IntegratorA");

            CTPlot ctPlot = new CTPlot(sys, "CTXYPlot", ctPanel);
            String[] ctLegends = {"(Px,Pz)"};
            
            ctPlot.setLegend(ctLegends);
            
            
            // CTConnections
            TypedIORelation rPx = new TypedIORelation(sys, "rPx");
            TypedIORelation rDPx = new TypedIORelation(sys, "rDPx");
            TypedIORelation rDDPx = new TypedIORelation(sys, "rDDPx");
            TypedIORelation rD3Px = new TypedIORelation(sys, "rD3Px");
            TypedIORelation rD4Px = new TypedIORelation(sys, "rD4Px");

            TypedIORelation rPz = new TypedIORelation(sys, "rPz");
            TypedIORelation rDPz = new TypedIORelation(sys, "rDPz");
            TypedIORelation rDDPz = new TypedIORelation(sys, "rDDPz");
            TypedIORelation rD3Pz = new TypedIORelation(sys, "rD3Pz");
            TypedIORelation rD4Pz = new TypedIORelation(sys, "rD4Pz");

            TypedIORelation rTh = new TypedIORelation(sys, "rTh");
            TypedIORelation rDTh = new TypedIORelation(sys, "rDTh");
            TypedIORelation rDDTh = new TypedIORelation(sys, "rDDTh");

            TypedIORelation rTm = new TypedIORelation(sys, "rTm");
            TypedIORelation rDTm = new TypedIORelation(sys, "rDTm");
            TypedIORelation rDDTm = new TypedIORelation(sys, "rDDTm");

            TypedIORelation rA = new TypedIORelation(sys, "rA");
            TypedIORelation rVx = new TypedIORelation(sys, "rVx");
            TypedIORelation rVz = new TypedIORelation(sys, "rVz");

            Px.output.link(rPx);
            DPx.output.link(rDPx);
            heli.outputDDPx.link(rDDPx);
            higher.outputD3Px.link(rD3Px);
            higher.outputD4Px.link(rD4Px);

            Pz.output.link(rPz);
            DPz.output.link(rDPz);
            heli.outputDDPz.link(rDDPz);
            higher.outputD3Pz.link(rD3Pz);
            higher.outputD4Pz.link(rD4Pz);
            
            Th.output.link(rTh);
            DTh.output.link(rDTh);
            heli.outputDDTh.link(rDDTh);

            Tm.output.link(rTm);
            DTm.output.link(rDTm);
            DDTm.output.link(rDDTm);
            A.output.link(rA);
       
            // Connect integrators
            Px.input.link(rDPx);
            DPx.input.link(rDDPx);
            Pz.input.link(rDPz);
            DPz.input.link(rDDPz);
            Th.input.link(rDTh);
            DTh.input.link(rDDTh);
            sys.connect(ctrl.outputDDDTm, DDTm.input);
            sys.connect(ctrl.outputDA, A.input);
            DTm.input.link(rDDTm);
            Tm.input.link(rDTm);

            // Connect Helicopter
            heli.inputTm.link(rTm);
            heli.inputA.link(rA);
            heli.inputTh.link(rTh);

            // Connect Controller
            ctrl.inputTm.link(rTm);
            ctrl.inputDTm.link(rDTm);
            ctrl.inputDDTm.link(rDDTm);
            ctrl.inputA.link(rA);
            ctrl.inputTh.link(rTh);
            ctrl.inputDTh.link(rTh);
            ctrl.inputTm.link(rTm);
            ctrl.inputTm.link(rTm);
            ctrl.inputVx.link(rVx);
            ctrl.inputVz.link(rVz);

            // Connect XZHigherDerivatives
            higher.inputTm.link(rTm);
            higher.inputDTm.link(rDTm);
            higher.inputDDTm.link(rDDTm);
            higher.inputTh.link(rTh);
            higher.inputDTh.link(rDTh);
            higher.inputA.link(rA);

            // Connect HoverLinearizer
            
            /*
            hover.outputVx.link(rVx);
            hover.outputVz.link(rVz);
            hover.inputPx.link(rPx);
            hover.inputDPx.link(rDPx);
            hover.inputDDPx.link(rDDPx);
            hover.inputD3Px.link(rD3Px);
            hover.inputD4Px.link(rD4Px);
            
            hover.inputPz.link(rPz);
            hover.inputDPz.link(rDPz);
            hover.inputDDPz.link(rDDPz);
            hover.inputD3Pz.link(rD3Pz);
            hover.inputD4Pz.link(rD4Pz);
            */
            suboutVx.link(rVx);
            suboutVz.link(rVz);
            subinPx.link(rPx);
            subinDPx.link(rDPx);
            subinDDPx.link(rDDPx);
            subinD3Px.link(rD3Px);
            subinD4Px.link(rD4Px);
            
            subinPz.link(rPz);
            subinDPz.link(rDPz);
            subinDDPz.link(rDDPz);
            subinD3Pz.link(rD3Pz);
            subinD4Pz.link(rD4Pz);

            sub.connect(subinPx, hover.inputPx);
            sub.connect(subinDPx, hover.inputDPx);
            sub.connect(subinDDPx, hover.inputDDPx);
            sub.connect(subinD3Px, hover.inputD3Px);
            sub.connect(subinD4Px, hover.inputD4Px);
            
            sub.connect(subinPz, hover.inputPz);
            sub.connect(subinDPz, hover.inputDPz);
            sub.connect(subinDDPz, hover.inputDDPz);
            sub.connect(subinD3Pz, hover.inputD3Pz);
            sub.connect(subinD4Pz, hover.inputD4Pz);

            sub.connect(suboutVx, hover.outputVx);
            sub.connect(suboutVz, hover.outputVz);

            //ctPlot.inputX.link(rPx);
            //ctPlot.inputY.link(rPz);
            ctPlot.input.link(rPz);

            //System.out.println("Parameters");
            // CT Director parameters
            Parameter initstep = 
                (Parameter)_dir.getAttribute("InitialStepSize");
            initstep.setExpression("0.01");
            initstep.parameterChanged(null);

            Parameter minstep =
                (Parameter)_dir.getAttribute("MinimumStepSize");
            minstep.setExpression("1e-60");
            minstep.parameterChanged(null);
            
            Parameter solver1 =
                (Parameter)_dir.getAttribute("BreakpointODESolver");
            StringToken token1 = new StringToken(
                    "ptolemy.domains.ct.kernel.solver.BackwardEulerSolver");
            solver1.setToken(token1);
            solver1.parameterChanged(null);
           
            
            Parameter solver2 =
                (Parameter)_dir.getAttribute("ODESolver");
            StringToken token2 = new StringToken(
                    "ptolemy.domains.ct.kernel.solver.ExplicitRK23Solver");
            solver2.setToken(token2);
            solver2.parameterChanged(null);

            // CTActorParameters
            Parameter Pxi = (Parameter)Px.getAttribute("InitialState");
            Pxi.setExpression("0.0");
            Pxi.parameterChanged(null);

            Parameter Pzi = (Parameter)Pz.getAttribute("InitialState");
            Pzi.setExpression("2.5");
            Pzi.parameterChanged(null);

            Parameter Tmi = (Parameter)Tm.getAttribute("InitialState");
            Tmi.setExpression("48.02");
            Tmi.parameterChanged(null);
            
            /*
            Parameter m1 = (Parameter)MINUS1.getAttribute("Gain");
            m1.setExpression("-1.0");
            m1.parameterChanged(null);
            
            Parameter m2 = (Parameter)MINUS2.getAttribute("Gain");
            m2.setExpression("-1.0");
            m2.parameterChanged(null);

            Parameter m3 = (Parameter)MINUS3.getAttribute("Gain");
            m3.setExpression("-1.0");
            m3.parameterChanged(null);
            */
            //XYPlot ranges
            Parameter xmin = (Parameter)ctPlot.getAttribute("X_Min");
            xmin.setExpression("-1.0");
            xmin.parameterChanged(null);
            
            Parameter xmax = (Parameter)ctPlot.getAttribute("X_Max");
            xmax.setExpression("1.0");
            xmax.parameterChanged(null);

            Parameter ymin = (Parameter)ctPlot.getAttribute("Y_Min");
            ymin.setExpression("0.0");
            ymin.parameterChanged(null);

            Parameter ymax = (Parameter)ctPlot.getAttribute("Y_Max");
            ymax.setExpression("10.0");
            ymax.parameterChanged(null);

            // Setting up parameters.
            _paramAlphaP = (Parameter)hover.getAttribute("AlphaP");
            _paramAlphaV = (Parameter)hover.getAttribute("AlphaV");
            _paramAlphaA = (Parameter)hover.getAttribute("AlphaA");
            _paramStopT = (Parameter)_dir.getAttribute("StopTime");
            //System.out.println(sys.description());
            System.out.println(_dir.getScheduler().description());
        } catch (Exception ex) {
            System.err.println("Setup failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    // The thread that runs the simulation.
    private boolean _isSimulationRunning;

    // FIXME: Under jdk 1.2, the following can (and should) be private
    private CTSingleSolverDirector _dir;
    private Manager _manager;

    private TextField _stopTimeBox;
    private TextField _alphaPBox;
    private TextField _alphaVBox;
    private TextField _alphaABox;
    private Label _currentTimeLabel;
    private double _stopTime = 100.0;
    private Button _goButton;
    private Button _actionButton;

    private Parameter _paramAlphaP;
    private Parameter _paramAlphaV;
    private Parameter _paramAlphaA;
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
        public void executionFinished(ExecutionEvent e) {
            super.executionFinished(e);
            _isSimulationRunning = false;
        }

    }

    private class ActionButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
        }
    }

    private class GoButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            if (_isSimulationRunning) {
                //System.out.println("Simulation still running.. hold on..");
                return;
            }

            try {
                // The simulation is started non-paused (of course :-) )
                _isSimulationPaused = false;

                // Set the stop time.
                try {
                    Double tmp = Double.valueOf( _stopTimeBox.getText());
                    _paramStopT.setToken(new DoubleToken(tmp.doubleValue()));
                    _paramStopT.parameterChanged(null);
                } catch (NumberFormatException ex) {
                    System.err.println("Invalid stop time: " 
                            +ex.getMessage());
                    return;
                }
                // set alphaP
                /*
                try {
                    Double tmp = Double.valueOf(_lamdaBox.getText());
                    _paramLamda.setToken(new DoubleToken(tmp.doubleValue()));
                    _paramLamda.parameterChanged(null);
                } catch (NumberFormatException ex) {
                    System.err.println("Invalid Lamda value: " + 
                            ex.getMessage());
                    return;
                }
                // set b.
                try {
                    Double tmp = Double.valueOf(_bBox.getText());
                    _paramB.setToken(new DoubleToken(tmp.doubleValue()));
                    _paramB.parameterChanged(null);
                } catch (NumberFormatException ex) {
                    System.err.println("Invalid B value: " + 
                                       ex.getMessage());
                    return;
                }
                // set sigma.
                try {
                    Double tmp = Double.valueOf(_sigmaBox.getText());
                    _paramSigma.setToken(new DoubleToken(tmp.doubleValue()));
                    _paramSigma.parameterChanged(null);
                } catch (NumberFormatException ex) {
                    System.err.println("Invalid SigmaValue: " + 
                            ex.getMessage());
                    return;
                }   
                */
                // Start the CurrentTimeThread.
                Thread ctt = new CurrentTimeThread();
                _isSimulationRunning = true;
                ctt.start();
                _manager.startRun();

            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                e.printStackTrace();
            }

        }
    }
}



