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

package ptolemy.domains.ct.demo.Helicopter;

import java.awt.*;
import java.awt.event.*;
import ptolemy.domains.de.kernel.*;
import ptolemy.domains.de.lib.*;
import ptolemy.domains.ct.kernel.*;
import ptolemy.domains.ct.kernel.util.*;
import ptolemy.domains.ct.lib.*;
import ptolemy.domains.fsm.kernel.*;
import ptolemy.domains.fsm.lib.*;
import ptolemy.actor.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.plot.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import collections.LinkedList;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
////  HelicopterApplet
/**
An applet that models a 2-D helicopter control system.

@author Jie Liu, Xiaojun Liu
@version $Id$
*/
public class HelicopterApplet extends CTApplet {

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Initialize the applet.
     */
    public void init() {

        super.init();
        // Initialization
        _stopTimeBox = new TextField("70.0", 10);
        _currentTimeCanvas = new ProgressBar();
        _goButton = new Button("Go");
        _actionButton = new Button("Climb");

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
        simulationParam.setLayout(new GridLayout(1,5));
	//simulationParam.setLayout(new BorderLayout());
        controlPanel.add(simulationParam);
        // Done adding simulation parameter panel.

	// Add a dummy panel.
	Panel dummy1 = new Panel();
	simulationParam.add(dummy1);

        // Adding current time in the sub panel.
        simulationParam.add(_currentTimeCanvas);
        //simulationParam.add(_currentTimeCanvas, BorderLayout.WEST);
        // Done adding average wait time.

        // Adding Stop time in the simulation panel.
        Panel subSimul = new Panel();
        simulationParam.add(subSimul);
        //simulationParam.add(subSimul, BorderLayout.CENTER);
        subSimul.add(new Label("Stop time:"));
        subSimul.add(_stopTimeBox);
        // Done adding stop time.

        Panel buttonPanel = new Panel();
        simulationParam.add(buttonPanel);
        //simulationParam.add(buttonPanel, BorderLayout.EAST);
        // Adding go button in the control panel.
        buttonPanel.add(_goButton);
        _goButton.addActionListener(new GoButtonListener());

        // Adding action button in the control panel.
        buttonPanel.add(_actionButton);
        _actionButton.addActionListener(new ActionButtonListener());

        // Add another dummy panel.
        Panel dummy2 = new Panel();
        simulationParam.add(dummy2);
        
        // _debug("Construct the topology");
        // Creating the topology.
        try {
            // Set up the top level composite actor, director and manager
            TypedCompositeActor sys = _toplevel;
            sys.setName("HelicopterSystem");
            _dir = new CTMultiSolverDirector(sys, "OuterDirector");
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
            _hsdir = new HSDirector(sub, "HSDirector");

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
            _hsdir.setController(hsctrl);

            TypedIOPort hscInAct = new TypedIOPort(hsctrl, "inputAction");
            hscInAct.setInput(true);
            hscInAct.setOutput(false);
            TypedIOPort hscInPz = new TypedIOPort(hsctrl, "inputPz");
            hscInPz.setInput(true);
            hscInPz.setOutput(false);

            FSMState hoverState = new FSMState(hsctrl, "HoverState");
            FSMState accelState = new FSMState(hsctrl, "AccelState");
            FSMState cruise1State = new FSMState(hsctrl, "Cruise1State");
            FSMState climbState = new FSMState(hsctrl, "ClimbState");
            FSMState cruise2State = new FSMState(hsctrl, "Cruise2State");
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
            FSMTransition tr1 = hsctrl.createTransition(hoverState, accelState);
            tr1.setTriggerCondition("inputAction");
            FSMTransition tr2 = hsctrl.createTransition(accelState, cruise1State);
            tr2.setTriggerCondition("(outputV >= 5.0) && (inputPz > -2.05) && (inputPz < -1.95)");
            FSMTransition tr3 = hsctrl.createTransition(cruise1State, climbState);
            tr3.setTriggerCondition("(outputV > 4.9) && (outputV < 5.1) && (outputR > -0.01) && (outputR < 0.01)");
            FSMTransition tr4 = hsctrl.createTransition(climbState, cruise2State);
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

            //_debug("Set parameters");
            // CT Director parameters
            Parameter initstep = 
                (Parameter)_dir.getAttribute("InitialStepSize");
            initstep.setToken(new DoubleToken(0.01));

            Parameter minstep =
                (Parameter)_dir.getAttribute("MinimumStepSize");
            minstep.setToken(new DoubleToken(1e-6));
            
            Parameter maxstep =
                (Parameter)_dir.getAttribute("MaximumStepSize");
            maxstep.setToken(new DoubleToken(0.5));

            Parameter solver1 =
                (Parameter)_dir.getAttribute("BreakpointODESolver");
            StringToken token1 = new StringToken(
                    "ptolemy.domains.ct.kernel.solver.BackwardEulerSolver");
            solver1.setToken(token1);
           
            Parameter solver2 =
                (Parameter)_dir.getAttribute("ODESolver");
            StringToken token2 = new StringToken(
                    "ptolemy.domains.ct.kernel.solver.ExplicitRK23Solver");
            solver2.setToken(token2);

           
            // CTActorParameters
            Parameter Pxi = (Parameter)Px.getAttribute("InitialState");
            Pxi.setToken(new DoubleToken(0.0));

            Parameter Pzi = (Parameter)Pz.getAttribute("InitialState");
            Pzi.setToken(new DoubleToken(-1.5));

            Parameter Tmi = (Parameter)Tm.getAttribute("InitialState");
            Tmi.setToken(new DoubleToken(48.02));            
            
            Parameter m1 = (Parameter)MINUS.getAttribute("Gain");
            m1.setToken(new DoubleToken(-1.0));
            
            //XYPlot ranges
            Parameter xmin = (Parameter)ctPlot.getAttribute("X_Min");
            xmin.setToken(new DoubleToken(-1.0));
            
            Parameter xmax = (Parameter)ctPlot.getAttribute("X_Max");
            xmax.setToken(new DoubleToken(100.0));

            Parameter ymin = (Parameter)ctPlot.getAttribute("Y_Min");
            ymin.setToken(new DoubleToken(1.0));

            Parameter ymax = (Parameter)ctPlot.getAttribute("Y_Max");
            ymax.setToken(new DoubleToken(12.0));

            /* CTPlot pxPlot = new CTPlot(sys, "VxPlot", pxPanel);
            String[] pxLegends = {"Vx"};
            pxPlot.setLegend(pxLegends);

            CTPlot pzPlot = new CTPlot(sys, "PzPlot", pzPanel);
            String[] pzLegends = {"Pz"};
            pzPlot.setLegend(pzLegends);

            CTPlot thPlot = new CTPlot(sys, "ThPlot", thPanel);
            String[] thLegends = {"Th"};
            thPlot.setLegend(thLegends);*/
            //VxPlot
            xmin = (Parameter)pxPlot.getAttribute("X_Min");
            xmin.setToken(new DoubleToken(0.0));
            
            xmax = (Parameter)pxPlot.getAttribute("X_Max");
            xmax.setToken(new DoubleToken(70.0));

            ymin = (Parameter)pxPlot.getAttribute("Y_Min");
            ymin.setToken(new DoubleToken(0.0));

            ymax = (Parameter)pxPlot.getAttribute("Y_Max");
            ymax.setToken(new DoubleToken(6.0));

            //PzPlot
            xmin = (Parameter)pzPlot.getAttribute("X_Min");
            xmin.setToken(new DoubleToken(0.0));
            
            xmax = (Parameter)pzPlot.getAttribute("X_Max");
            xmax.setToken(new DoubleToken(70.0));

            ymin = (Parameter)pzPlot.getAttribute("Y_Min");
            ymin.setToken(new DoubleToken(1.0));

            ymax = (Parameter)pzPlot.getAttribute("Y_Max");
            ymax.setToken(new DoubleToken(12.0));

            //thPlot
            xmin = (Parameter)thPlot.getAttribute("X_Min");
            xmin.setToken(new DoubleToken(0.0));
            
            xmax = (Parameter)thPlot.getAttribute("X_Max");
            xmax.setToken(new DoubleToken(70.0));

            ymin = (Parameter)thPlot.getAttribute("Y_Min");
            ymin.setToken(new DoubleToken(-0.05));

            ymax = (Parameter)thPlot.getAttribute("Y_Max");
            ymax.setToken(new DoubleToken(0.05));

            // Setting up parameters.
            //_paramAlphaP = (Parameter)hover.getAttribute("AlphaP");
            //_paramAlphaV = (Parameter)hover.getAttribute("AlphaV");
            //_paramAlphaA = (Parameter)hover.getAttribute("AlphaA");
            _paramStopT = (Parameter)_dir.getAttribute("StopTime");
            _paramButton = (Parameter)button.getAttribute("ButtonClicked");
            // System.out.println(sys.description());
            // System.out.println(_dir.getScheduler().description());
            // System.out.println(subdir.getScheduler().description());
            // System.out.println(_dir.getScheduler().description());
            // System.out.println(((CTDirector)sub.getDirector()).getScheduler().description());

        } catch (Exception ex) {
            report("Setup failed: ", ex);
            ex.printStackTrace();
        }
    }
    ////////////////////////////////////////////////////////////////////////
    ////                         private methods                        ////

    CTCompositeActor _createLinearizer(TypedCompositeActor container, 
            int code)
            throws NameDuplicationException, IllegalActionException {
        CTCompositeActor sub = new CTCompositeActor(container, "dummy");
        CTEmbeddedNRDirector subdir = 
                new CTEmbeddedNRDirector(sub, "CTInnerDirector");

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
                param = (Parameter)lin.getAttribute("CVx");
                param.setToken(new DoubleToken(5.0));
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
                p = (Parameter)mon1.getAttribute("ThresholdCenter");
                p.setToken(new DoubleToken(-2.0));
                break;
            case 2: // first cruise state
                mon1.input.link(rV);
                p = (Parameter)mon1.getAttribute("ThresholdWidth");
                p.setToken(new DoubleToken(0.2));
                p = (Parameter)mon1.getAttribute("ThresholdCenter");
                p.setToken(new DoubleToken(5.0));
                mon2.input.link(rR);
                p = (Parameter)mon2.getAttribute("ThresholdWidth");
                p.setToken(new DoubleToken(0.02));
                p = (Parameter)mon2.getAttribute("ThresholdCenter");
                p.setToken(new DoubleToken(0.0));
                break;
            case 3: // climb state
                mon1.input.link(rInPz);
                p = (Parameter)mon1.getAttribute("ThresholdWidth");
                p.setToken(new DoubleToken(0.1));
                p = (Parameter)mon1.getAttribute("ThresholdCenter");
                p.setToken(new DoubleToken(-10.0));
                mon2.input.link(rV);
                p = (Parameter)mon2.getAttribute("ThresholdWidth");
                p.setToken(new DoubleToken(0.2));
                p = (Parameter)mon2.getAttribute("ThresholdCenter");
                p.setToken(new DoubleToken(5.0));
                break;
            default:
                break;
        }

        // sub dir parameters
        Parameter initstep = 
                (Parameter)subdir.getAttribute("InitialStepSize");
        initstep.setToken(new DoubleToken(0.01));

        Parameter minstep =
                (Parameter)subdir.getAttribute("MinimumStepSize");
        minstep.setToken(new DoubleToken(1e-6));
            
        Parameter solver1 =
                (Parameter)subdir.getAttribute("BreakpointODESolver");
        Token token1 = new StringToken(
                "ptolemy.domains.ct.kernel.solver.BackwardEulerSolver");
        solver1.setToken(token1);
                      
        Parameter solver2 =
                (Parameter)subdir.getAttribute("ODESolver");
        Token token2 = new StringToken(
                "ptolemy.domains.ct.kernel.solver.ForwardEulerSolver");
        solver2.setToken(token2);

        return sub;
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    // The thread that runs the simulation.
    private boolean _isSimulationRunning;

    // FIXME: Under jdk 1.2, the following can (and should) be private
    private CTMultiSolverDirector _dir;
    private Manager _thismanager;
    private HSDirector _hsdir;
    private int _currentState;
    private int[] _switchTime = new int[5];
    private boolean _switched;

    private TextField _stopTimeBox;
    private ProgressBar _currentTimeCanvas;
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

    //////////////////////////////////////////////////////////////////////////
    ////                       inner classes                              ////
    /* Show simulation progress.
     */
    public class CurrentTimeThread extends Thread {
        public void run() {
            _switchTime[0] = 0;
            while (_isSimulationRunning) {
                _switched = false;
                // get current FSM state.
                FSMState st = _hsdir.currentState();
                if( st == null) {
                    continue;
                }
                String stateName = st.getName();
                if (stateName.equals("HoverState")){
                    _currentState = 0;
                } else if(stateName.equals("AccelState")) {
                    if(_currentState ==0) {
                        _switched = true;
                    }
                    _currentState = 1;
                } else if(stateName.equals("Cruise1State")) {
                    if(_currentState == 1) {
                        _switched = true;
                    }
                    _currentState = 2;
                } else if(stateName.equals("ClimbState")) {
                    if (_currentState == 1) {  // state2 is skipped
                        _switchTime[2] = _switchTime[1]+1;
                        _switched = true;
                    }
                    if(_currentState ==2) {
                        _switched = true;
                    }
                   _currentState = 3;
                } else {
                    if(_currentState ==3) {
                        _switched = true;
                    }
                    _currentState = 4;
                }
                
                // get the current time from director.
                double currenttime = _dir.getCurrentTime();
                double ratio = (currenttime/_stopTime)*140.0;
                int width = (new Double(ratio)).intValue();
                if((ratio - (double)width) > 0.5) {
                    width +=1;
                }
                if(_switched) {
                    _switchTime[_currentState] = width-1;
                }
                int incwidth = width - _switchTime[_currentState];
                _currentTimeCanvas.setWidth(incwidth);
                _currentTimeCanvas.repaint();
                try {
                    sleep(100);
                } catch (InterruptedException e) {}
            }
        }
    }

    public class MyExecutionListener extends DefaultExecutionListener {
        public void executionFinished(Manager manager) {
            super.executionFinished(manager);
            _isSimulationRunning = false;
        }

    }

    public class ActionButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            try {
                _paramButton.setToken(new BooleanToken(true));
            }catch (IllegalActionException ex) {
                report ("Button click failed:", ex);
            }

        }
    }

    public class GoButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            if (_isSimulationRunning) {
                // System.out.println("Simulation still running.. hold on..");
                return;
            }

            try {
                // The simulation is started non-paused (of course :-) )
                _isSimulationPaused = false;

                // Set the stop time.
                try {
                    Double tmp = Double.valueOf( _stopTimeBox.getText());
                    _paramStopT.setToken(new DoubleToken(tmp.doubleValue()));

                    // System.out.println("Set stop time of simulation.");

                } catch (NumberFormatException ex) {
                    report("Invalid stop time: ", ex);
                    return;
                }

                // Start the CurrentTimeThread.
                Thread ctt = new CurrentTimeThread();
                _isSimulationRunning = true;
                ctt.start();
                _thismanager.startRun();

            } catch (Exception e) {
                report("Error: ",  e);
                e.printStackTrace();
            }

        }
    }

    /** Draw the progress bar.
     */
    public class ProgressBar extends Canvas {

        /** draw the progress bar.
         */
        public void paint(Graphics g) {
            g.setColor(new Color(128, 128, 128));
            g.draw3DRect(8, 8, 144, 24, false);
           
            switch(_currentState) {
            case 0:
                g.setColor(new Color(255, 0, 0));
                g.fill3DRect(10, 10, _width, 20, true);
                break;
            case 1:
                g.setColor(new Color(255, 0, 0));
                g.fill3DRect(10, 10, _switchTime[1], 20, true);
                g.setColor(new Color(0, 255, 0));
                g.fill3DRect(10+_switchTime[1], 10, _width, 20, true);
                break;
            case 2:
                g.setColor(new Color(255, 0, 0));
                g.fill3DRect(10, 10, _switchTime[1], 20, true);
                g.setColor(new Color(0, 255, 0));
                g.fill3DRect(10+_switchTime[1], 10, 
                        _switchTime[2]-_switchTime[1], 20, true);
                g.setColor(new Color(0, 0, 255));
                g.fill3DRect(10+_switchTime[2], 10, _width, 20, true);
                break;
            case 3:
                //System.err.println(_switchTime[2] + " " +_switchTime[3]);
                g.setColor(new Color(255, 0, 0));
                g.fill3DRect(10, 10, _switchTime[1], 20, true);
                g.setColor(new Color(0, 255, 0));
                g.fill3DRect(10+_switchTime[1], 10, 
                        _switchTime[2]-_switchTime[1], 20, true);
                g.setColor(new Color(0, 0, 255));
                g.fill3DRect(10+_switchTime[2], 10, 
                        _switchTime[3]-_switchTime[2], 20, true);
                g.setColor(new Color(255, 0, 255));
                g.fill3DRect(10+_switchTime[3], 10, _width, 20, true);
                break;
            case 4:
                g.setColor(new Color(255, 0, 0));
                g.fill3DRect(10, 10, _switchTime[1], 20, true);
                g.setColor(new Color(0, 255, 0));
                g.fill3DRect(10+_switchTime[1], 10, 
                        _switchTime[2]-_switchTime[1], 20, true);
                g.setColor(new Color(0, 0, 255));
                g.fill3DRect(10+_switchTime[2], 10, 
                        _switchTime[3]-_switchTime[2], 20, true);
                g.setColor(new Color(255, 0, 255));
                g.fill3DRect(10+_switchTime[3], 10, 
                        _switchTime[4]-_switchTime[3], 20, true);
                g.setColor(new Color(0, 0, 255)); 
                g.fill3DRect(10+_switchTime[4], 10, _width, 20, true);
                break;
            default:
                break;
            }
        }

        /** set the width of the rectangle.
         */
        public void setWidth(int width) {
            _width = width;
            
        }

        ///////////////////////////////////////////////////////////////////
        ////                    private variables                      ////
        private int _width= 0;
    }
}



