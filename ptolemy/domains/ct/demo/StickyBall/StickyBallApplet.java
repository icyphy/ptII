/* A demo with two sticky balls.

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

@ProposedRating Red (liuj@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.ct.demo.StickyBall;

import java.awt.*;
import java.awt.event.*;
import ptolemy.domains.fsm.kernel.*;
import ptolemy.domains.fsm.lib.*;
import ptolemy.domains.ct.kernel.*;
import ptolemy.domains.ct.gui.CTApplet;
import ptolemy.domains.ct.lib.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.util.*;
import ptolemy.actor.lib.*;
import ptolemy.actor.gui.TimedPlotter;
import ptolemy.actor.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// Sticky Ball
/*
@author Jie Liu, Xiaojun Liu
@version $Id$
*/
public class StickyBallApplet extends CTApplet {

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Initialize the applet.
     */
    public void init() {

        super.init();

        try {
            // The 1 argument requests only a go button.
            add(_createRunControls(1));

            // the top level composite actor
            _toplevel.setName("HybridSystem");

            // the top level CT director
            CTMultiSolverDirector topdir = new CTMultiSolverDirector(
                    _toplevel, "CTTopLevelDirector");
            //topdir.addDebugListener(new StreamListener());
            // two const sources
            // FIXME: to be replaced by impulse sources
            Const source1 = new Const(_toplevel, "Const1");
            source1.value.setToken(new DoubleToken(0.0));
            Const source2 = new Const(_toplevel, "Const2");
            source2.value.setToken(new DoubleToken(0.0));

            // the plot
            // FIXME: adjust configuration.
            TimedPlotter myplot = new TimedPlotter(_toplevel, "plot");
            myplot.setPanel(this);
            myplot.plot.setGrid(true);
            myplot.plot.setTitle("Sticky Ball");
            myplot.plot.addLegend(0, "Ball 1 Position");
            myplot.plot.addLegend(1, "Ball 2 Position");
            //myplot.plot.setWrap(true);
            //myplot.plot.setPointsPersistence(1024);
            //myplot.plot.setConnected(true);
            myplot.plot.setXRange(0.0, 25.0);
            myplot.plot.setYRange(0, 3.0);
            myplot.plot.setSize(500, 300);

            //System.out.println("Building a simple hybrid system.");
            CTCompositeActor hs = new CTCompositeActor(_toplevel, "HS");

            // the ports
            // Input for ball 1
            TypedIOPort hsin1 = (TypedIOPort)hs.newPort("force1");
            hsin1.setInput(true);
            hsin1.setTypeEquals(DoubleToken.class);
            // Input for ball 2
            TypedIOPort hsin2 = (TypedIOPort)hs.newPort("force2");
            hsin2.setInput(true);
            hsin2.setTypeEquals(DoubleToken.class);
            // Output ball 1 position
            TypedIOPort hsout1 = (TypedIOPort)hs.newPort("P1");
            hsout1.setOutput(true);
            hsout1.setTypeEquals(DoubleToken.class);
            // Output ball 2 position
            TypedIOPort hsout2 = (TypedIOPort)hs.newPort("P2");
            hsout2.setOutput(true);
            hsout2.setTypeEquals(DoubleToken.class);
            
            //System.out.println("Building the FSM controller.");
            HSController ctrl = new HSController(hs, "Controller");
            FSMState ctrlInc = new FSMState(ctrl, "Separate");
            FSMState ctrlDec = new FSMState(ctrl, "Together");
            ctrl.setInitialState(ctrlInc);
            FSMTransition ctrlTr1 = 
                    ctrl.createTransition(ctrlInc, ctrlDec);
            ctrlTr1.setTriggerEvent("touched");
            // ctrlTr1.setInitEntry(true);
            HSInit hsinit1 = new HSInit(ctrlTr1, "P1", "P1");
            HSInit hsinit4 = new HSInit(ctrlTr1, "V1", "(V1*1.0+V2*1.0)/2.0");
            // FIXME: the initial sticking force.
            HSInit hsinit0 = new HSInit(ctrlTr1, "STI", "5.0");
            FSMTransition ctrlTr2 = 
                    ctrl.createTransition(ctrlDec, ctrlInc);            
            ctrlTr2.setTriggerCondition("F > STI || F < -STI");
            //ctrlTr2.setInitEntry(true);
            HSInit hsinit2 = new HSInit(ctrlTr2, "P1", "P1");
            HSInit hsinit3 = new HSInit(ctrlTr2, "P2", "P1");
            HSInit hsinit5 = new HSInit(ctrlTr2, "V1", "V1");
            HSInit hsinit6 = new HSInit(ctrlTr2, "V2", "V1");

            // the hybrid system director
            HSDirector hsdir = new HSDirector(hs, "HSDirector");
            //hs.setDirector(hsdir);
            hsdir.setController(ctrl);

            //System.out.println("Building the dynamics of " 
            //        + "two separate balls.");
            CTCompositeActor ctInc = new CTCompositeActor(hs, "Separate");

            //CTZeroOrderHold ctIncH1 = new CTZeroOrderHold(ctInc, "Hold1");
            CTIntegrator ctIncV1 = new CTIntegrator(ctInc, "V1");
            ctIncV1.InitialState.setToken(new DoubleToken(0.0));
            CTIntegrator ctIncP1 = new CTIntegrator(ctInc, "P1");
            ctIncP1.InitialState.setToken(new DoubleToken(0.0));
            Expression ctIncE1 = new Expression(ctInc, "E1");
            TypedIOPort ctIncE1In = (TypedIOPort)ctIncE1.newPort("In");
            ctIncE1In.setInput(true);
            ctIncE1In.setTypeEquals(DoubleToken.class);
            ctIncE1.output.setTypeEquals(DoubleToken.class);
            TypedIOPort ctIncE1P1 = (TypedIOPort)ctIncE1.newPort("P1");
            ctIncE1P1.setInput(true);
            ctIncE1P1.setTypeEquals(DoubleToken.class);
            // The expression is:
            // (K1*Y1 + In - K1*P1)/M1
            ctIncE1.expression.setExpression("1.0*1.0 + In - 1.0*P1");

            //CTZeroOrderHold ctIncH2 = new CTZeroOrderHold(ctInc, "Hold2");
            CTIntegrator ctIncV2 = new CTIntegrator(ctInc, "V2");
            ctIncV2.InitialState.setToken(new DoubleToken(0.0));
            CTIntegrator ctIncP2 = new CTIntegrator(ctInc, "P2");
            ctIncP2.InitialState.setToken(new DoubleToken(3.0));
            Expression ctIncE2 = new Expression(ctInc, "E2");
            TypedIOPort ctIncE2In = (TypedIOPort)ctIncE2.newPort("In");
            ctIncE2In.setInput(true);
            ctIncE2In.setTypeEquals(DoubleToken.class);
            ctIncE2.output.setTypeEquals(DoubleToken.class);
            TypedIOPort ctIncE2P2 = (TypedIOPort)ctIncE2.newPort("P2");
            ctIncE2P2.setInput(true);
            ctIncE2P2.setTypeEquals(DoubleToken.class);
            // The expression is:
            // (K2*Y2 + In - K2*P2)/M2
            ctIncE2.expression.setExpression("2.0*2.0 + In - 2.0*P2");

            AddSubtract ctIncE3 = new AddSubtract(ctInc, "E3");
            CTZeroCrossingDetector ctIncD = 
                    new CTZeroCrossingDetector(ctInc, "ZD");

            // the ports
            // Force on ball 1
            TypedIOPort ctIncF1 = (TypedIOPort)ctInc.newPort("force1");
            ctIncF1.setInput(true);
            ctIncF1.setTypeEquals(DoubleToken.class);
            // Force on ball 2
            TypedIOPort ctIncF2 = (TypedIOPort)ctInc.newPort("force2");
            ctIncF2.setInput(true);
            ctIncF2.setTypeEquals(DoubleToken.class);
            // Touched trigger
            TypedIOPort ctIncTouched = (TypedIOPort)ctInc.newPort("touched");
            ctIncTouched.setOutput(true);
            ctIncTouched.setTypeEquals(DoubleToken.class);
            // Position of ball 1
            TypedIOPort ctIncOP1 = (TypedIOPort)ctInc.newPort("P1");
            ctIncOP1.setOutput(true);
            ctIncOP1.setTypeEquals(DoubleToken.class);
            // Position of ball 2
            TypedIOPort ctIncOP2 = (TypedIOPort)ctInc.newPort("P2");
            ctIncOP2.setOutput(true);
            ctIncOP2.setTypeEquals(DoubleToken.class);
            // Velocity of ball 1
            TypedIOPort ctIncOV1 = (TypedIOPort)ctInc.newPort("V1");
            ctIncOV1.setOutput(true);
            ctIncOV1.setTypeEquals(DoubleToken.class);
            // Velocity of ball 2
            TypedIOPort ctIncOV2 = (TypedIOPort)ctInc.newPort("V2");
            ctIncOV2.setOutput(true);
            ctIncOV2.setTypeEquals(DoubleToken.class);

            // connect ctInc
            //ctInc.connect(ctIncF1, ctIncH1.input);
            //ctInc.connect(ctIncH1.output, ctIncE1In);
            ctInc.connect(ctIncF1, ctIncE1In);
            ctInc.connect(ctIncE1.output, ctIncV1.input);
            Relation ctIncRB0 = ctInc.connect(ctIncV1.output, ctIncP1.input);
            ctIncOV1.link(ctIncRB0);
            Relation ctIncRB1 = ctInc.connect(ctIncP1.output, ctIncE1P1);
            ctIncOP1.link(ctIncRB1);
            
            //ctInc.connect(ctIncF2, ctIncH2.input);
            //ctInc.connect(ctIncH2.output, ctIncE2In);
            ctInc.connect(ctIncF2, ctIncE2In);
            ctInc.connect(ctIncE2.output, ctIncV2.input);
            Relation ctIncRB3 = ctInc.connect(ctIncV2.output, ctIncP2.input);
            ctIncOV2.link(ctIncRB3);
            Relation ctIncRB2 = ctInc.connect(ctIncP2.output, ctIncE2P2);
            ctIncOP2.link(ctIncRB2);

            ctIncE3.plus.link(ctIncRB1);
            ctIncE3.minus.link(ctIncRB2);
            Relation ctIncTr = ctInc.connect(
                    ctIncD.trigger, ctIncE3.output);
            ctIncD.input.link(ctIncTr);
            ctInc.connect(ctIncD.output, ctIncTouched);

            CTEmbeddedNRDirector ctIncDir = new CTEmbeddedNRDirector(
                    ctInc, "CTIncDir");
            //ctIncDir.addDebugListener(new StreamListener());
            //System.out.println("Building the dynamics of two balls "
            //        + "sticking together.");
            CTCompositeActor ctDec = new CTCompositeActor(hs, "Together");

            CTIntegrator ctDecV1 = new CTIntegrator(ctDec, "V1");
            CTIntegrator ctDecP1 = new CTIntegrator(ctDec, "P1");
            CTIntegrator ctDecSTI = new CTIntegrator(ctDec, "STI");
            Scale ctGain = new Scale(ctDec, "Gain");
            ctGain.gain.setToken(new DoubleToken(-1.0));

            Expression ctDecE1 = new Expression(ctDec, "E1");
            TypedIOPort ctDecE1P1 = (TypedIOPort)ctDecE1.newPort("P1");
            ctDecE1P1.setInput(true);
            ctDecE1P1.setTypeEquals(DoubleToken.class);
            ctDecE1.output.setTypeEquals(DoubleToken.class);
            // The expression is:
            // (K1*Y1 + K2*Y2 - K1*P1 - K2*P1)/(M1+M2)
            ctDecE1.expression.setExpression("(1.0*1.0 + 2.0*2.0 - (1.0+2.0)*P1)/2.0");
            Expression ctDecE2 = new Expression(ctDec, "E2");
            TypedIOPort ctDecE2P1 = (TypedIOPort)ctDecE2.newPort("P1");
            ctDecE2P1.setInput(true);
            ctDecE2P1.setTypeEquals(DoubleToken.class);
            ctDecE2.output.setTypeEquals(DoubleToken.class);
            // The expression is:
            // (K1*Y1 - K2*Y2 - K1*P1 + K2*P1)
            ctDecE2.expression.setExpression("1.0*1.0 - 2.0*2.0 - (1.0-2.0)*P1");

            // Sticky force
            TypedIOPort ctDecOSTI = (TypedIOPort)ctDec.newPort("STI");
            ctDecOSTI.setOutput(true);
            ctDecOSTI.setTypeEquals(DoubleToken.class);
            TypedIOPort ctDecOF = (TypedIOPort)ctDec.newPort("F");
            ctDecOF.setOutput(true);
            ctDecOF.setTypeEquals(DoubleToken.class);
            // Position of ball 1
            TypedIOPort ctDecOP1 = (TypedIOPort)ctDec.newPort("P1");
            ctDecOP1.setOutput(true);
            ctDecOP1.setTypeEquals(DoubleToken.class);
            // Position of ball 2
            TypedIOPort ctDecOP2 = (TypedIOPort)ctDec.newPort("P2");
            ctDecOP2.setOutput(true);
            ctDecOP2.setTypeEquals(DoubleToken.class);
            // Velocity of balls
            TypedIOPort ctDecOV1 = (TypedIOPort)ctDec.newPort("V1");
            ctDecOV1.setOutput(true);
            ctDecOV1.setTypeEquals(DoubleToken.class);

            // connect
            ctDec.connect(ctDecSTI.input, ctGain.output);
            Relation ctDecR1 = ctDec.connect(ctDecSTI.output, ctGain.input);
            ctDecOSTI.link(ctDecR1);
            
            ctDec.connect(ctDecE1.output, ctDecV1.input);
            Relation ctDecR3 = ctDec.connect(ctDecV1.output, ctDecP1.input);
            ctDecOV1.link(ctDecR3);
            Relation ctDecR2 = ctDec.connect(ctDecP1.output, ctDecE1P1);
            ctDecE2P1.link(ctDecR2);
            ctDecOP1.link(ctDecR2);
            ctDecOP2.link(ctDecR2);
            ctDec.connect(ctDecOF, ctDecE2.output);
            CTEmbeddedNRDirector ctDecDir = new CTEmbeddedNRDirector(
                    ctDec, "CTDecDir");            
            //ctDecDir.addDebugListener(new StreamListener());

            // connect the hybrid system
            ctrlInc.setRefinement(ctInc);
            ctrlDec.setRefinement(ctDec);
            hs.connect(hsin1, ctIncF1);
            hs.connect(hsin2, ctIncF2);
            Relation hsR1 = hs.connect(ctIncOP1, ctDecOP1);
            Relation hsR2 = hs.connect(ctIncOP2, ctDecOP2);
            TypedIORelation hsV1 = new TypedIORelation(hs, "HSV1");
            TypedIORelation hsV2 = new TypedIORelation(hs, "HSV2");
            TypedIORelation hsTch = new TypedIORelation(hs, "HSTouched");
            ctIncTouched.link(hsTch);
            ctIncOV1.link(hsV1);
            ctIncOV2.link(hsV2);
            ctDecOV1.link(hsV1);
            hsout1.link(hsR1);
            hsout2.link(hsR2);

            TypedIORelation hsF = new TypedIORelation(hs, "HSF");
            ctDecOF.link(hsF);
            TypedIORelation hsSTI = new TypedIORelation(hs, "HSSTI");
            ctDecOSTI.link(hsSTI);

            // connect the top-level system
            _toplevel.connect(source1.output, hsin1);
            _toplevel.connect(source2.output, hsin2);
            _toplevel.connect(hsout1, myplot.input);
            _toplevel.connect(hsout2, myplot.input);

            //System.out.println("Set parameters.");
            // try to run the system
            //topdir.StartTime.setToken(new DoubleToken(-100.0));
            topdir.StopTime.setToken(new DoubleToken(100.0));
           
            // CT embedded director 1 parameters
            ctIncDir.InitStepSize.setToken(new DoubleToken(0.01));
            
            ctIncDir.MinStepSize.setToken(new DoubleToken(1e-5));
            
            StringToken tok = new StringToken(
                    "ptolemy.domains.ct.kernel.solver.BackwardEulerSolver");
            ctIncDir.BreakpointODESolver.setToken(tok);
            Parameter dfsol = (Parameter)ctIncDir.getAttribute("ODESolver");
            tok = new StringToken(
                    "ptolemy.domains.ct.kernel.solver.ForwardEulerSolver");
            ctIncDir.ODESolver.setToken(tok);

            // CT embedded director 2  parameters
            ctDecDir.InitStepSize.setToken(new DoubleToken(0.01));
            ctDecDir.MinStepSize.setToken(new DoubleToken(1e-5));
            tok = new StringToken(
                    "ptolemy.domains.ct.kernel.solver.BackwardEulerSolver");
            ctDecDir.BreakpointODESolver.setToken(tok);
            tok = new StringToken(
                    "ptolemy.domains.ct.kernel.solver.ForwardEulerSolver");
            ctDecDir.ODESolver.setToken(tok);

            // CT director parameters
            topdir.InitStepSize.setToken(new DoubleToken(0.01));
            topdir.MinStepSize.setToken(new DoubleToken(1e-5));
            topdir.MaxStepSize.setToken(new DoubleToken(0.3));
            tok = new StringToken(
                    "ptolemy.domains.ct.kernel.solver.BackwardEulerSolver");
            topdir.BreakpointODESolver.setToken(tok);
            tok = new StringToken(
                    "ptolemy.domains.ct.kernel.solver.ForwardEulerSolver");
            topdir.ODESolver.setToken(tok);
            
        }catch (KernelException ex) {
            report("Setup failed:", ex);
        }
    }
}

         
