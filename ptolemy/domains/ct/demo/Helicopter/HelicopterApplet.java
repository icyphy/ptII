/* An applet that uses Ptolemy II CT and DE domains.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Red (liuj@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.ct.demo.Helicopter;

import ptolemy.domains.ct.kernel.*;
import ptolemy.domains.ct.kernel.util.*;
import ptolemy.domains.ct.gui.CTApplet;
import ptolemy.domains.ct.lib.*;
import ptolemy.domains.fsm.kernel.*;
import ptolemy.actor.*;
import ptolemy.actor.gui.*;
import ptolemy.gui.*;
import ptolemy.actor.lib.*;
import ptolemy.actor.lib.gui.*;
import ptolemy.actor.util.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.event.*;
import java.util.Iterator;
import javax.swing.*;

///////////////////////////////////////////////////////////
////  HelicopterApplet
/**
An applet that models a 2-D helicopter control system.

@author Jie Liu, Xiaojun Liu
@version $Id$
*/
public class HelicopterApplet extends CTApplet {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Initialize the applet.
     */
    public void init() {

        super.init();
        JPanel controlpanel = new JPanel();
        controlpanel.setBackground(getBackground());
        controlpanel.setLayout(new GridLayout(1, 3));
        getContentPane().add(controlpanel, BorderLayout.NORTH);

        _query = new Query();
        _query.setBackground(getBackground());
        //_query.addQueryListener(new ParameterListener());
        controlpanel.add(_query, BorderLayout.WEST);
        _query.addLine("stopT", "Stop Time", "70.0");

        JPanel runcontrols = new JPanel();
        runcontrols.setBackground(getBackground());
        runcontrols.setLayout(new GridLayout(1, 1));
        controlpanel.add(runcontrols, BorderLayout.EAST);
        runcontrols.add(_createRunControls(1));

        //_currentTimeCanvas = new ProgressBar();
        //controlpanel.add(_currentTimeCanvas, BorderLayout.EAST);

        JPanel plotPanel = new JPanel();
        plotPanel.setLayout(new GridLayout(2, 2));
        plotPanel.setBackground(getBackground());
        getContentPane().add(plotPanel);

        try {
            // Set up the top level composite actor, director and manager
            _toplevel.setName("HelicopterSystem");
            _dir = new CTMultiSolverDirector(_toplevel, "OuterDirector");
            //_dir.addDebugListener(new StreamListener());
            _thisManager = _manager;

            // ---------------------------------
            // Create the composite actors.
            // ---------------------------------

            CTCompositeActor sub = new CTCompositeActor(_toplevel,
                    "Controllers");
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

            FSMActor hsctrl = new FSMActor(sub, "HSController");
            _hsdir.controllerName.setToken(new StringToken("HSController"));

            TypedIOPort hscInAct = new TypedIOPort(hsctrl, "inputAction");
            hscInAct.setInput(true);
            hscInAct.setOutput(false);
            TypedIOPort hscInPz = new TypedIOPort(hsctrl, "inputPz");
            hscInPz.setInput(true);
            hscInPz.setOutput(false);
            TypedIOPort hscInV = new TypedIOPort(hsctrl, "outputV");
            hscInV.setInput(true);
            hscInV.setOutput(false);
            TypedIOPort hscInR = new TypedIOPort(hsctrl, "outputR");
            hscInR.setInput(true);
            hscInR.setOutput(false);

            State hoverState = new State(hsctrl, "HoverState");
            State accelState = new State(hsctrl, "AccelState");
            State cruise1State = new State(hsctrl, "Cruise1State");
            State climbState = new State(hsctrl, "ClimbState");
            State cruise2State = new State(hsctrl, "Cruise2State");
            hsctrl.initialStateName.setToken(new StringToken("HoverState"));
            CTCompositeActor linHover = _createLinearizer(sub, 0);
            CTCompositeActor linAccel = _createLinearizer(sub, 1);
            CTCompositeActor linCruise1 = _createLinearizer(sub, 2);
            CTCompositeActor linClimb = _createLinearizer(sub, 3);
            CTCompositeActor linCruise2 = _createLinearizer(sub, 4);
            hoverState.refinementName.setToken(new StringToken("HoverCTSub"));
            accelState.refinementName.setToken(new StringToken("AccelCTSub"));
            cruise1State.refinementName.setToken(new StringToken("Cruise1CTSub"));
            climbState.refinementName.setToken(new StringToken("ClimbCTSub"));
            cruise2State.refinementName.setToken(new StringToken("Cruise2CTSub"));
            Transition tr1 = new Transition(hsctrl, "tr1");
            hoverState.outgoingPort.link(tr1);
            accelState.incomingPort.link(tr1);
            tr1.setGuardExpression("inputAction_V");
            Transition tr2 = new Transition(hsctrl, "tr2");
            accelState.outgoingPort.link(tr2);
            cruise1State.incomingPort.link(tr2);
            tr2.setGuardExpression("(outputV_V >= 5.0) && (inputPz_V> -2.05) " +
                    "&& (inputPz_V < -1.95)");

            Transition tr3 = new Transition(hsctrl, "tr3");
            cruise1State.outgoingPort.link(tr3);
            climbState.incomingPort.link(tr3);
            tr3.setGuardExpression("(outputV_V > 4.9) && (outputV_V < 5.1) " +
                    "&& (outputR_V > -0.01) && (outputR_V < 0.01)");
            Transition tr4 = new Transition(hsctrl, "tr4");
            climbState.outgoingPort.link(tr4);
            cruise2State.incomingPort.link(tr4);
            // 
            tr4.setGuardExpression("(outputV_V > 4.9) && (outputV_V < 5.1) " +
                    "&& (inputPz_V > -10.05) && (inputPz_V < -9.95)");

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
            hscInV.link(rSubOutV);
            hscInR.link(rSubOutR);
            Iterator entities = sub.entityList().iterator();
            while(entities.hasNext()) {
                Entity ent = (Entity)entities.next();
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

            _clock = new Clock(_toplevel, "Clock");
            _clock.period.setToken(new DoubleToken(1e308));
            double offsets[][] = {{0.0, 20.0}};
            _clock.offsets.setToken(new DoubleMatrixToken(offsets));
            BooleanToken[] defaultValues = new BooleanToken[2];
            defaultValues[0] = new BooleanToken(false);
            defaultValues[1] = new BooleanToken(true);
            ArrayToken defaultValueToken = new ArrayToken(defaultValues);
            _clock.values.setToken(defaultValueToken);
            _toplevel.connect(_clock.output, subinAction);

            HelicopterActor heli = new HelicopterActor(_toplevel, "Helicopter");
            ControllerActor ctrl = new ControllerActor(_toplevel, "Controller");
            XZHigherDerivatives higher = new XZHigherDerivatives(_toplevel,
                    "XZHigherDerivatives");

            Integrator Px = new Integrator(_toplevel, "IntegratorPx");
            Integrator DPx = new Integrator(_toplevel, "IntegratorDPx");

            Integrator Pz = new Integrator(_toplevel, "IntegratorPz");
            Integrator DPz = new Integrator(_toplevel, "IntegratorDPz");

            Integrator Th = new Integrator(_toplevel, "IntegratorTh");
            Integrator DTh = new Integrator(_toplevel, "IntegratorDTh");

            Integrator Tm = new Integrator(_toplevel, "IntegratorTm");
            Integrator DTm = new Integrator(_toplevel, "IntegratorDTm");
            Integrator DDTm = new Integrator(_toplevel, "IntegratorDDTm");

            Integrator A = new Integrator(_toplevel, "IntegratorA");

            Scale MINUS = new Scale(_toplevel, "MINUS");
            //CTPlot ctPlot = new CTPlot(_toplevel, "CTPlot", ctPanel);
            XYPlotter xzPlot = new XYPlotter(_toplevel, "Helicopter Position");
            xzPlot.place(plotPanel);
            xzPlot.plot.setBackground(getBackground());
            xzPlot.plot.setTitle("Helicopter Position");
            xzPlot.plot.setButtons(false);
            xzPlot.plot.setGrid(true);
            xzPlot.plot.setXRange(-1.0, 100.0);
            xzPlot.plot.setYRange(1.0, 12.0);
            xzPlot.plot.setSize(200, 200);
            xzPlot.plot.addLegend(0, "x, z");

            TimedPlotter vxPlot = new TimedPlotter(_toplevel,
                    "Horizontal Speed");
            vxPlot.place(plotPanel);
            vxPlot.plot.setBackground(getBackground());
            vxPlot.plot.setTitle("Horizontal Speed");
            vxPlot.plot.setButtons(false);
            vxPlot.plot.setGrid(true);
            vxPlot.plot.setXRange(0.0, 70.0);
            vxPlot.plot.setYRange(0.0, 6.0);
            vxPlot.plot.setSize(200, 200);
            vxPlot.plot.addLegend(0,"Vx");

            TimedPlotter pzPlot = new TimedPlotter(_toplevel,
                    "Vertical Position");
            pzPlot.place(plotPanel);
            pzPlot.plot.setBackground(getBackground());
            pzPlot.plot.setTitle("Vertical Position");
            pzPlot.plot.setButtons(false);
            pzPlot.plot.setGrid(true);
            pzPlot.plot.setXRange(0.0, 70.0);
            pzPlot.plot.setYRange(0.0, 12.0);
            pzPlot.plot.setSize(200, 200);
            pzPlot.plot.addLegend(0,"Pz");

            TimedPlotter thPlot = new TimedPlotter(_toplevel,
                    "Pitch Angle");
            thPlot.place(plotPanel);
            thPlot.plot.setBackground(getBackground());
            thPlot.plot.setTitle("Pitch Angle");
            thPlot.plot.setButtons(false);
            thPlot.plot.setGrid(true);
            thPlot.plot.setXRange(0.0, 70.0);
            thPlot.plot.setYRange(-0.05, 0.05);
            thPlot.plot.setSize(200, 200);
            thPlot.plot.addLegend(0,"Th");

            // CTConnections
            TypedIORelation rPx = new TypedIORelation(_toplevel, "rPx");
            TypedIORelation rDPx = new TypedIORelation(_toplevel, "rDPx");
            TypedIORelation rDDPx = new TypedIORelation(_toplevel, "rDDPx");
            TypedIORelation rD3Px = new TypedIORelation(_toplevel, "rD3Px");
            TypedIORelation rD4Px = new TypedIORelation(_toplevel, "rD4Px");

            TypedIORelation rPz = new TypedIORelation(_toplevel, "rPz");
            TypedIORelation rDPz = new TypedIORelation(_toplevel, "rDPz");
            TypedIORelation rDDPz = new TypedIORelation(_toplevel, "rDDPz");
            TypedIORelation rD3Pz = new TypedIORelation(_toplevel, "rD3Pz");
            TypedIORelation rD4Pz = new TypedIORelation(_toplevel, "rD4Pz");

            TypedIORelation rTh = new TypedIORelation(_toplevel, "rTh");
            TypedIORelation rDTh = new TypedIORelation(_toplevel, "rDTh");
            TypedIORelation rDDTh = new TypedIORelation(_toplevel, "rDDTh");

            TypedIORelation rTm = new TypedIORelation(_toplevel, "rTm");
            TypedIORelation rDTm = new TypedIORelation(_toplevel, "rDTm");
            TypedIORelation rDDTm = new TypedIORelation(_toplevel, "rDDTm");

            TypedIORelation rA = new TypedIORelation(_toplevel, "rA");
            TypedIORelation rVx = new TypedIORelation(_toplevel, "rVx");
            TypedIORelation rVz = new TypedIORelation(_toplevel, "rVz");

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
            _toplevel.connect(ctrl.outputDDDTm, DDTm.input);
            _toplevel.connect(ctrl.outputDA, A.input);
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

            TypedIORelation rmPz = new TypedIORelation(_toplevel, "RMPz");
            MINUS.input.link(rPz);
            MINUS.output.link(rmPz);
            xzPlot.inputX.link(rPx);
            xzPlot.inputY.link(rmPz);
            //ctPlot.input.link(rPz);
            vxPlot.input.link(rDPx);
            pzPlot.input.link(rmPz);
            thPlot.input.link(rTh);

            //_debug("Set parameters");
            // CT Director parameters
            _dir.initStepSize.setToken(new DoubleToken(0.1));

            _dir.minStepSize.setToken(new DoubleToken(1e-3));

            _dir.maxStepSize.setToken(new DoubleToken(0.5));

            StringToken token1 = new StringToken(
                    "ptolemy.domains.ct.kernel.solver.BackwardEulerSolver");
            _dir.breakpointODESolver.setToken(token1);

            StringToken token2 = new StringToken(
                    "ptolemy.domains.ct.kernel.solver.ExplicitRK23Solver");
            _dir.ODESolver.setToken(token2);


            Px.initialState.setToken(new DoubleToken(0.0));

            Pz.initialState.setToken(new DoubleToken(-1.5));

            Tm.initialState.setToken(new DoubleToken(48.02));

            MINUS.factor.setToken(new DoubleToken(-1.0));

            //_paramButton = (Parameter)button.getAttribute("ButtonClicked");
            // System.out.println(_toplevel.description());
            // System.out.println(_dir.getScheduler().description());
            // System.out.println(subdir.getScheduler().description());
            // System.out.println(_dir.getScheduler().description());
            // System.out.println(((CTDirector)sub.getDirector()).
            // getScheduler().description());

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
        CTEmbeddedDirector subdir =
            new CTEmbeddedDirector(sub, "CTInnerDirector");

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

        TypedAtomicActor lin = null;
        ThresholdMonitor mon1 = null;
        ThresholdMonitor mon2 = null;
        switch (code) {
        case 0: // hover state
            sub.setName("HoverCTSub");
            lin = new HoverLinearizer(sub, "Hover");
            break;
        case 1: // acc state
            sub.setName("AccelCTSub");
            lin = new AccelerLinearizer(sub, "Accel");
            mon1 = new ThresholdMonitor(sub, "Mon1");
            break;
        case 2: // first cruise state
            sub.setName("Cruise1CTSub");
            lin = new CruiseLinearizer(sub, "Cruise1");
            mon1 = new ThresholdMonitor(sub, "Mon1");
            mon2 = new ThresholdMonitor(sub, "Mon2");
            break;
        case 3: // climb state
            sub.setName("ClimbCTSub");
            lin = new ClimbLinearizer(sub, "Climb");
            mon1 = new ThresholdMonitor(sub, "Mon1");
            mon2 = new ThresholdMonitor(sub, "Mon2");
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

        ZeroOrderHold hPx = new ZeroOrderHold(sub, "HPx");
        ZeroOrderHold hDPx = new ZeroOrderHold(sub, "HDPx");
        ZeroOrderHold hDDPx = new ZeroOrderHold(sub, "HDDPx");
        ZeroOrderHold hD3Px = new ZeroOrderHold(sub, "HD3Px");
        ZeroOrderHold hD4Px = new ZeroOrderHold(sub, "HD4Px");
        ZeroOrderHold hPz = new ZeroOrderHold(sub, "HPz");
        ZeroOrderHold hDPz = new ZeroOrderHold(sub, "HDPz");
        ZeroOrderHold hDDPz = new ZeroOrderHold(sub, "HDDPz");
        ZeroOrderHold hD3Pz = new ZeroOrderHold(sub, "HD3Pz");
        ZeroOrderHold hD4Pz = new ZeroOrderHold(sub, "HD4Pz");

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

        Relation rV = sub.connect(suboutV,
                (ComponentPort)lin.getPort("outputV"));
        Relation rR = sub.connect(suboutR,
                (ComponentPort)lin.getPort("outputR"));

        // connect and set the monitors
        Parameter p = null;
        switch (code) {
        case 1: // accel state
            mon1.input.link(rInPz);
            p = (Parameter)mon1.getAttribute("thresholdWidth");
            p.setToken(new DoubleToken(0.1));
            p = (Parameter)mon1.getAttribute("thresholdCenter");
            p.setToken(new DoubleToken(-2.0));
            break;
        case 2: // first cruise state
            mon1.input.link(rV);
            p = (Parameter)mon1.getAttribute("thresholdWidth");
            p.setToken(new DoubleToken(0.2));
            p = (Parameter)mon1.getAttribute("thresholdCenter");
            p.setToken(new DoubleToken(5.0));
            mon2.input.link(rR);
            p = (Parameter)mon2.getAttribute("thresholdWidth");
            p.setToken(new DoubleToken(0.02));
            p = (Parameter)mon2.getAttribute("thresholdCenter");
            p.setToken(new DoubleToken(0.0));
            break;
        case 3: // climb state
            mon1.input.link(rInPz);
            p = (Parameter)mon1.getAttribute("thresholdWidth");
            p.setToken(new DoubleToken(0.1));
            p = (Parameter)mon1.getAttribute("thresholdCenter");
            p.setToken(new DoubleToken(-10.0));
            mon2.input.link(rV);
            p = (Parameter)mon2.getAttribute("thresholdWidth");
            p.setToken(new DoubleToken(0.2));
            p = (Parameter)mon2.getAttribute("thresholdCenter");
            p.setToken(new DoubleToken(5.0));
            break;
        default:
            break;
        }

        subdir.initStepSize.setToken(new DoubleToken(0.1));

        subdir.minStepSize.setToken(new DoubleToken(1e-3));

        subdir.maxStepSize.setToken(new DoubleToken(0.5));

        Token token1 = new StringToken(
                "ptolemy.domains.ct.kernel.solver.BackwardEulerSolver");
        subdir.breakpointODESolver.setToken(token1);

        Token token2 = new StringToken(
                "ptolemy.domains.ct.kernel.solver.ForwardEulerSolver");
        subdir.ODESolver.setToken(token2);

        return sub;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Execute the system.  This overrides the base class to read the
     *  values in the query box first.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    protected void _go() throws IllegalActionException {
        try {
            _dir.stopTime.setToken(new DoubleToken(
                    _query.doubleValue("stopT")));
            super._go();
        } catch (Exception ex) {
            report(ex);
        }
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private CTMultiSolverDirector _dir;
    private HSDirector _hsdir;
    private int _currentState;
    private int[] _switchTime = new int[5];
    private boolean _switched;

    private Manager _thisManager;
    private Query _query;
    //private ProgressBar _currentTimeCanvas;
    private double _stopTime = 70.0;
    private JButton _actionButton;
    private Clock _clock;
}
