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
import ptolemy.domains.ct.kernel.util.*;
import ptolemy.domains.ct.lib.*;
import ptolemy.domains.sc.kernel.*;
import ptolemy.domains.sc.lib.*;
import ptolemy.actor.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.plot.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import collections.LinkedList;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
////  HeliControlApplet
/**
An applet that models a 2-D helicopter control system.

@author Jie Liu, Xiaojun Liu
@version $Id$
*/
public class HeliControlApplet extends CTApplet {

    public  boolean DEBUG = false;

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Initialize the applet.
     */
    public void init() {

        super.init();
        // Initialization
        _stopTimeBox = new TextField("70.0", 10);
        _currentTimeLabel = new Label("Current time = 0.0     ");
        _goButton = new Button("Go");
        _actionButton = new Button("Action");

        // The applet has tow panels, stacked vertically
        setLayout(new BorderLayout());
        Panel appletPanel = new Panel();
        appletPanel.setLayout(new GridLayout(2,2));
        add(appletPanel, "Center");
        
        Plot ctPanel = new Plot();
        appletPanel.add(ctPanel);
        Plot pxPanel = new Plot();
        appletPanel.add(pxPanel);
        Plot pzPanel = new Plot();
        appletPanel.add(pzPanel);
        Plot thPanel = new Plot();
        appletPanel.add(thPanel);
        
        // Adding a control panel in the main panel.
        Panel controlPanel = new Panel();
        add(controlPanel, "South");
        // Done adding a control panel.

        // Adding simulation parameter panel in the control panel.
        Panel simulationParam = new Panel();
        simulationParam.setLayout(new GridLayout(1,3));
        controlPanel.add(simulationParam);
        // Done adding simulation parameter panel.

        // Adding current time in the sub panel.
        simulationParam.add(_currentTimeLabel);
        // Done adding average wait time.

        // Adding Stop time in the simulation panel.
        Panel subSimul = new Panel();
        simulationParam.add(subSimul);
        subSimul.add(new Label("Stop time:"));
        subSimul.add(_stopTimeBox);
        // Done adding stop time.

        Panel buttonPanel = new Panel();
        simulationParam.add(buttonPanel);
        // Adding go button in the control panel.
        buttonPanel.add(_goButton);
        _goButton.addActionListener(new GoButtonListener());

        // Adding action button in the control panel.
        buttonPanel.add(_actionButton);
        _actionButton.addActionListener(new ActionButtonListener());
        
        //System.out.println("Construct ptII");
        // Creating the topology.
        try {
            // Set up the top level composite actor, director and manager
            TypedCompositeActor sys = _toplevel;
            sys.setName("HeliControlSystem");
            _dir = new CTMultiSolverDirector("OutterDirector");
            sys.setDirector(_dir);
            //_manager = new Manager("Manager");
            _manager.addExecutionListener(new MyExecutionListener());
            //sys.setManager(_manager);
            _thismanager = _manager;
            //_dir.setVERBOSE(true);
            //_dir.setDEBUG(true);

            // ---------------------------------
            // Create the composite actors.
            // ---------------------------------

            CTCompositeActor sub = new CTCompositeActor(sys, "Linearizers");
            HSDirector hsdir = new HSDirector("HSDirector");

            sub.setDirector(hsdir);
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

            TypedIOPort subinAction = new TypedIOPort(sub, "inputAction");
            subinAction.setInput(true);
            subinAction.setOutput(false);
            
            TypedIOPort suboutVx = new TypedIOPort(sub, "outputVx");
            suboutVx.setInput(false);
            suboutVx.setOutput(true);
            TypedIOPort suboutVz = new TypedIOPort(sub, "outputVz");
            suboutVz.setInput(false);
            suboutVz.setOutput(true);
            
            HSController hsctrl = new HSController(sub, "HSController");
            hsdir.setController(hsctrl);

            TypedIOPort hscInAct = new TypedIOPort(hsctrl, "inputAction");
            hscInAct.setInput(true);
            hscInAct.setOutput(false);
            TypedIOPort hscInPz = new TypedIOPort(hsctrl, "inputPz");
            hscInPz.setInput(true);
            hscInPz.setOutput(false);

            SCState hoverState = new SCState(hsctrl, "HoverState");
            SCState accelState = new SCState(hsctrl, "AccelState");
            SCState cruise1State = new SCState(hsctrl, "Cruise1State");
            SCState climbState = new SCState(hsctrl, "ClimbState");
            SCState cruise2State = new SCState(hsctrl, "Cruise2State");
            hsctrl.setInitialState(hoverState);
            CTCompositeActor linHover = _createLinearizer(sub, 0);
            CTCompositeActor linAccel = _createLinearizer(sub, 1);
            CTCompositeActor linCruise1 = _createLinearizer(sub, 2);
            CTCompositeActor linClimb = _createLinearizer(sub, 3);
            CTCompositeActor linCruise2 = _createLinearizer(sub, 4);
            hoverState.setRefinement(linHover);
            accelState.setRefinement(linAccel);
            cruise1State.setRefinement(linCruise1);
            climbState.setRefinement(linClimb);
            cruise2State.setRefinement(linCruise2);
            SCTransition tr1 = hsctrl.createTransition(hoverState, accelState);
            tr1.setTriggerCondition("inputAction");
            SCTransition tr2 = hsctrl.createTransition(accelState, cruise1State);
            tr2.setTriggerCondition("(outputV >= 5.0) && (inputPz > -2.05) && (inputPz < -1.95)");
            SCTransition tr3 = hsctrl.createTransition(cruise1State, climbState);
            tr3.setTriggerCondition("(outputV > 4.9) && (outputV < 5.1) && (outputR > -0.01) && (outputR < 0.01)");
            SCTransition tr4 = hsctrl.createTransition(climbState, cruise2State);
            tr4.setTriggerCondition("(outputV > 4.9) && (outputV < 5.1) && (inputPz > -10.05) && (inputPz < -9.95)");

            TypedIORelation rSubPx = new TypedIORelation(sub, "rSubPx");
            TypedIORelation rSubDPx = new TypedIORelation(sub, "rSubDPx");
            TypedIORelation rSubDDPx = new TypedIORelation(sub, "rSubDDPx");
            TypedIORelation rSubD3Px = new TypedIORelation(sub, "rSubD3Px");
            TypedIORelation rSubD4Px = new TypedIORelation(sub, "rSubD4Px");
            TypedIORelation rSubPz = new TypedIORelation(sub, "rSubPz");
            TypedIORelation rSubDPz = new TypedIORelation(sub, "rSubDPz");
            TypedIORelation rSubDDPz = new TypedIORelation(sub, "rSubDDPz");
            TypedIORelation rSubD3Pz = new TypedIORelation(sub, "rSubD3Pz");
            TypedIORelation rSubD4Pz = new TypedIORelation(sub, "rSubD4Pz");
            TypedIORelation rSubOutVx = new TypedIORelation(sub, "rSubOutVx");
            TypedIORelation rSubOutVz = new TypedIORelation(sub, "rSubOutVz");
            TypedIORelation rSubOutV = new TypedIORelation(sub, "rSubOutV");
            TypedIORelation rSubOutR = new TypedIORelation(sub, "rSubOutR");

            subinPx.link(rSubPx);
            subinDPx.link(rSubDPx);
            subinDDPx.link(rSubDDPx);
            subinD3Px.link(rSubD3Px);
            subinD4Px.link(rSubD4Px);
            subinPz.link(rSubPz);
            subinDPz.link(rSubDPz);
            subinDDPz.link(rSubDDPz);
            subinD3Pz.link(rSubD3Pz);
            subinD4Pz.link(rSubD4Pz);
            suboutVx.link(rSubOutVx);
            suboutVz.link(rSubOutVz);

            sub.connect(subinAction, hscInAct);
            hscInPz.link(rSubPz);
            Enumeration entities = sub.getEntities();
            while(entities.hasMoreElements()) {
                Entity ent = (Entity)entities.nextElement();
                Port p = ent.getPort("inputPx");
                if (p != null) {
                    p.link(rSubPx);
                }
                p = ent.getPort("inputDPx");
                if (p != null) {
                    p.link(rSubDPx);
                }
                p = ent.getPort("inputDDPx");
                if (p != null) {
                    p.link(rSubDDPx);
                }
                p = ent.getPort("inputD3Px");
                if (p != null) {
                    p.link(rSubD3Px);
                }
                p = ent.getPort("inputD4Px");
                if (p != null) {
                    p.link(rSubD4Px);
                }
                p = ent.getPort("inputPz");
                if (p != null) {
                    p.link(rSubPz);
                }
                p = ent.getPort("inputDPz");
                if (p != null) {
                    p.link(rSubDPz);
                }
                p = ent.getPort("inputDDPz");
                if (p != null) {
                    p.link(rSubDDPz);
                }
                p = ent.getPort("inputD3Pz");
                if (p != null) {
                    p.link(rSubD3Pz);
                }
                p = ent.getPort("inputD4Pz");
                if (p != null) {
                    p.link(rSubD4Pz);
                }
                p = ent.getPort("outputVx");
                if (p != null) {
                    p.link(rSubOutVx);
                }
                p = ent.getPort("outputVz");
                if (p != null) {
                    p.link(rSubOutVz);
                }
                p = ent.getPort("outputV");
                if (p != null) {
                    p.link(rSubOutV);
                }
                p = ent.getPort("outputR");
                if (p != null) {
                    p.link(rSubOutR);
                }
            }
            // CTActors

            CTButtonEvent button = new CTButtonEvent(sys, "Button");
            sys.connect(button.output, subinAction);

            HelicopterActor heli = new HelicopterActor(sys, "Helicopter");
            ControllerActor ctrl = new ControllerActor(sys, "Controller");
            XZHigherDerivatives higher = new XZHigherDerivatives(sys, 
                    "XZHigherDerivatives");     
            
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

            CTGain MINUS = new CTGain(sys, "MINUS");
            //CTPlot ctPlot = new CTPlot(sys, "CTPlot", ctPanel);
            CTXYPlot ctPlot = new CTXYPlot(sys, "XZPlot", ctPanel);
            String[] ctLegends = {"(Px,Pz)"};
            ctPlot.setLegend(ctLegends);

            CTPlot pxPlot = new CTPlot(sys, "VxPlot", pxPanel);
            String[] pxLegends = {"Vx"};
            pxPlot.setLegend(pxLegends);

            CTPlot pzPlot = new CTPlot(sys, "PzPlot", pzPanel);
            String[] pzLegends = {"Pz"};
            pzPlot.setLegend(pzLegends);

            CTPlot thPlot = new CTPlot(sys, "ThPlot", thPanel);
            String[] thLegends = {"Th"};
            thPlot.setLegend(thLegends);
            
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

            sub.getPort("outputVx").link(rVx);
            sub.getPort("outputVz").link(rVz);
            sub.getPort("inputPx").link(rPx);
            sub.getPort("inputDPx").link(rDPx);
            sub.getPort("inputDDPx").link(rDDPx);
            sub.getPort("inputD3Px").link(rD3Px);
            sub.getPort("inputD4Px").link(rD4Px);
            
            sub.getPort("inputPz").link(rPz);
            sub.getPort("inputDPz").link(rDPz);
            sub.getPort("inputDDPz").link(rDDPz);
            sub.getPort("inputD3Pz").link(rD3Pz);
            sub.getPort("inputD4Pz").link(rD4Pz);

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
            
            TypedIORelation rmPz = new TypedIORelation(sys, "RMPz");
            MINUS.input.link(rPz);
            MINUS.output.link(rmPz);
            ctPlot.inputX.link(rPx);
            ctPlot.inputY.link(rmPz);
            //ctPlot.input.link(rPz);
            pxPlot.input.link(rDPx);
            pzPlot.input.link(rmPz);
            thPlot.input.link(rTh);

            //System.out.println("Parameters");
            // CT Director parameters
            Parameter initstep = 
                (Parameter)_dir.getAttribute("InitialStepSize");
            initstep.setExpression("0.01");
            initstep.parameterChanged(null);

            Parameter minstep =
                (Parameter)_dir.getAttribute("MinimumStepSize");
            minstep.setExpression("1e-6");
            minstep.parameterChanged(null);
            
            Parameter maxstep =
                (Parameter)_dir.getAttribute("MaximumStepSize");
            maxstep.setExpression("0.5");
            maxstep.parameterChanged(null);

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
            Pzi.setExpression("-1.5");
            Pzi.parameterChanged(null);

            Parameter Tmi = (Parameter)Tm.getAttribute("InitialState");
            Tmi.setExpression("48.02");
            Tmi.parameterChanged(null);
            
            
            Parameter m1 = (Parameter)MINUS.getAttribute("Gain");
            m1.setExpression("-1.0");
            m1.parameterChanged(null);
            
            //XYPlot ranges
            Parameter xmin = (Parameter)ctPlot.getAttribute("X_Min");
            xmin.setExpression("-1.0");
            xmin.parameterChanged(null);
            
            Parameter xmax = (Parameter)ctPlot.getAttribute("X_Max");
            xmax.setExpression("70.0");
            xmax.parameterChanged(null);

            Parameter ymin = (Parameter)ctPlot.getAttribute("Y_Min");
            ymin.setExpression("-10.0");
            ymin.parameterChanged(null);

            Parameter ymax = (Parameter)ctPlot.getAttribute("Y_Max");
            ymax.setExpression("0.0");
            ymax.parameterChanged(null);

            // Setting up parameters.
            //_paramAlphaP = (Parameter)hover.getAttribute("AlphaP");
            //_paramAlphaV = (Parameter)hover.getAttribute("AlphaV");
            //_paramAlphaA = (Parameter)hover.getAttribute("AlphaA");
            _paramStopT = (Parameter)_dir.getAttribute("StopTime");
            _paramButton = (Parameter)button.getAttribute("ButtonClicked");
            //System.out.println(sys.description());
            //System.out.println(_dir.getScheduler().description());
            //System.out.println(subdir.getScheduler().description());
            //System.out.println(_dir.getScheduler().description());
            //System.out.println(((CTDirector)sub.getDirector()).getScheduler().description());

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
    private CTMultiSolverDirector _dir;
    private Manager _thismanager;

    private TextField _stopTimeBox;
    private Label _currentTimeLabel;
    private double _stopTime = 70.0;
    private Button _goButton;
    private Button _actionButton;

    private Parameter _paramAlphaP;
    private Parameter _paramAlphaV;
    private Parameter _paramAlphaA;
    private Parameter _paramStopT;
    private Parameter _paramButton;

    //private Label _currentTimeLabel;
    private boolean _isSimulationPaused = false;

    ////////////////////////////////////////////////////////////////////////
    ////                         private methods                        ////

    CTCompositeActor _createLinearizer(TypedCompositeActor container, 
            int code)
            throws NameDuplicationException, IllegalActionException {
        CTCompositeActor sub = new CTCompositeActor(container, "dummy");
        CTEmbeddedNRDirector subdir = 
                new CTEmbeddedNRDirector("CTInnerDirector");

        //subdir.setVERBOSE(true);
        //subdir.setDEBUG(true);

        sub.setDirector(subdir);
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
        suboutVx.setInput(false);
        suboutVx.setOutput(true);
        TypedIOPort suboutVz = new TypedIOPort(sub, "outputVz");
        suboutVz.setInput(false);
        suboutVz.setOutput(true);

        TypedIOPort suboutV = new TypedIOPort(sub, "outputV");
        suboutV.setInput(false);
        suboutV.setOutput(true);
        TypedIOPort suboutR = new TypedIOPort(sub, "outputR");
        suboutR.setInput(false);
        suboutR.setOutput(true);

        // ---------------------------------
        // Create the actors.
        // ---------------------------------

        CTActor lin = null;
        CTThresholdMonitor mon1 = null;
        CTThresholdMonitor mon2 = null;
        switch (code) {
            case 0: // hover state
                sub.setName("HoverCTSub");
                lin = new HoverLinearizer(sub, "Hover");
                break;
            case 1: // acc state
                sub.setName("AccelCTSub");
                lin = new AccelerLinearizer(sub, "Accel");
                mon1 = new CTThresholdMonitor(sub, "Mon1");
                break;
            case 2: // first cruise state
                sub.setName("Cruise1CTSub");
                lin = new CruiseLinearizer(sub, "Cruise1");
                mon1 = new CTThresholdMonitor(sub, "Mon1");
                mon2 = new CTThresholdMonitor(sub, "Mon2");
                break;
            case 3: // climb state
                sub.setName("ClimbCTSub");
                lin = new ClimbLinearizer(sub, "Climb");
                mon1 = new CTThresholdMonitor(sub, "Mon1");
                mon2 = new CTThresholdMonitor(sub, "Mon2");
                break;
            case 4: // second cruise state
                sub.setName("Cruise2CTSub");
                lin = new CruiseLinearizer(sub, "Cruise2");
                Parameter param = (Parameter)lin.getAttribute("CPz");
                param.setToken(new DoubleToken(-10.0));
                param.parameterChanged(null);
                param = (Parameter)lin.getAttribute("CVx");
                param.setToken(new DoubleToken(5.0));
                param.parameterChanged(null);
                break;
            default:
                break;
        }
    
        CTZeroOrderHold hPx = new CTZeroOrderHold(sub, "HPx");
        CTZeroOrderHold hDPx = new CTZeroOrderHold(sub, "HDPx");
        CTZeroOrderHold hDDPx = new CTZeroOrderHold(sub, "HDDPx");
        CTZeroOrderHold hD3Px = new CTZeroOrderHold(sub, "HD3Px");
        CTZeroOrderHold hD4Px = new CTZeroOrderHold(sub, "HD4Px");
        CTZeroOrderHold hPz = new CTZeroOrderHold(sub, "HPz");
        CTZeroOrderHold hDPz = new CTZeroOrderHold(sub, "HDPz");
        CTZeroOrderHold hDDPz = new CTZeroOrderHold(sub, "HDDPz");
        CTZeroOrderHold hD3Pz = new CTZeroOrderHold(sub, "HD3Pz");
        CTZeroOrderHold hD4Pz = new CTZeroOrderHold(sub, "HD4Pz");
            
        sub.connect(hPx.input, subinPx);
        sub.connect(hDPx.input, subinDPx);
        sub.connect(hDDPx.input, subinDDPx);
        sub.connect(hD3Px.input, subinD3Px);
        sub.connect(hD4Px.input, subinD4Px);
            
        Relation rInPz = sub.connect(hPz.input, subinPz);
        sub.connect(hDPz.input, subinDPz);
        sub.connect(hDDPz.input, subinDDPz);
        sub.connect(hD3Pz.input, subinD3Pz);
        sub.connect(hD4Pz.input, subinD4Pz);

        sub.connect(hPx.output, (ComponentPort)lin.getPort("inputPx"));
        sub.connect(hDPx.output, (ComponentPort)lin.getPort("inputDPx"));
        sub.connect(hDDPx.output, (ComponentPort)lin.getPort("inputDDPx"));
        sub.connect(hD3Px.output, (ComponentPort)lin.getPort("inputD3Px"));
        sub.connect(hD4Px.output, (ComponentPort)lin.getPort("inputD4Px"));
            
        sub.connect(hPz.output, (ComponentPort)lin.getPort("inputPz"));
        sub.connect(hDPz.output, (ComponentPort)lin.getPort("inputDPz"));
        sub.connect(hDDPz.output, (ComponentPort)lin.getPort("inputDDPz"));
        sub.connect(hD3Pz.output, (ComponentPort)lin.getPort("inputD3Pz"));
        sub.connect(hD4Pz.output, (ComponentPort)lin.getPort("inputD4Pz"));

        sub.connect(suboutVx, (ComponentPort)lin.getPort("outputVx"));
        sub.connect(suboutVz, (ComponentPort)lin.getPort("outputVz"));

        Relation rV = sub.connect(suboutV, (ComponentPort)lin.getPort("outputV"));
        Relation rR = sub.connect(suboutR, (ComponentPort)lin.getPort("outputR"));

        // connect and set the monitors
        Parameter p = null;
        switch (code) {
            case 1: // accel state
                mon1.input.link(rInPz);
                p = (Parameter)mon1.getAttribute("ThresholdWidth");
                p.setToken(new DoubleToken(0.1));
                p.parameterChanged(null);
                p = (Parameter)mon1.getAttribute("ThresholdCenter");
                p.setToken(new DoubleToken(-2.0));
                p.parameterChanged(null);
                break;
            case 2: // first cruise state
                mon1.input.link(rV);
                p = (Parameter)mon1.getAttribute("ThresholdWidth");
                p.setToken(new DoubleToken(0.2));
                p.parameterChanged(null);
                p = (Parameter)mon1.getAttribute("ThresholdCenter");
                p.setToken(new DoubleToken(5.0));
                p.parameterChanged(null);
                mon2.input.link(rR);
                p = (Parameter)mon2.getAttribute("ThresholdWidth");
                p.setToken(new DoubleToken(0.02));
                p.parameterChanged(null);
                p = (Parameter)mon2.getAttribute("ThresholdCenter");
                p.setToken(new DoubleToken(0.0));
                p.parameterChanged(null);
                break;
            case 3: // climb state
                mon1.input.link(rInPz);
                p = (Parameter)mon1.getAttribute("ThresholdWidth");
                p.setToken(new DoubleToken(0.1));
                p.parameterChanged(null);
                p = (Parameter)mon1.getAttribute("ThresholdCenter");
                p.setToken(new DoubleToken(-10.0));
                p.parameterChanged(null);
                mon2.input.link(rV);
                p = (Parameter)mon2.getAttribute("ThresholdWidth");
                p.setToken(new DoubleToken(0.2));
                p.parameterChanged(null);
                p = (Parameter)mon2.getAttribute("ThresholdCenter");
                p.setToken(new DoubleToken(5.0));
                p.parameterChanged(null);
                break;
            default:
                break;
        }

        // sub dir parameters
        Parameter initstep = 
                (Parameter)subdir.getAttribute("InitialStepSize");
        initstep.setExpression("0.01");
        initstep.parameterChanged(null);

        Parameter minstep =
                (Parameter)subdir.getAttribute("MinimumStepSize");
        minstep.setExpression("1e-6");
        minstep.parameterChanged(null);
            
        Parameter solver1 =
                (Parameter)subdir.getAttribute("BreakpointODESolver");
        Token token1 = new StringToken(
                "ptolemy.domains.ct.kernel.solver.BackwardEulerSolver");
        solver1.setToken(token1);
        solver1.parameterChanged(null);
                      
        Parameter solver2 =
                (Parameter)subdir.getAttribute("ODESolver");
        Token token2 = new StringToken(
                "ptolemy.domains.ct.kernel.solver.ForwardEulerSolver");
        solver2.setToken(token2);
        solver2.parameterChanged(null);

        return sub;
    }


    //////////////////////////////////////////////////////////////////////////
    ////                       inner classes                              ////

    /* Show simulation progress.
     */
    public class CurrentTimeThread extends Thread {
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

    public class MyExecutionListener extends DefaultExecutionListener {
        public void executionFinished(ExecutionEvent e) {
            super.executionFinished(e);
            _isSimulationRunning = false;
        }

    }

    public class ActionButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            _paramButton.setToken(new BooleanToken(true));
            _paramButton.parameterChanged(null);
        }
    }

    public class GoButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            if (_isSimulationRunning) {
                System.out.println("Simulation still running.. hold on..");
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

                    //System.out.println("Set stop time of simulation.");

                } catch (NumberFormatException ex) {
                    System.err.println("Invalid stop time: " 
                            +ex.getMessage());
                    return;
                }

                // Start the CurrentTimeThread.
                Thread ctt = new CurrentTimeThread();
                _isSimulationRunning = true;
                ctt.start();
                _thismanager.startRun();

            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                e.printStackTrace();
            }

        }
    }
}



