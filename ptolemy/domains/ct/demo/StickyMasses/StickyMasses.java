/* A demo with two sticky point masses.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

package ptolemy.domains.ct.demo.StickyMasses;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.actor.lib.AddSubtract;
import ptolemy.actor.lib.Expression;
import ptolemy.actor.lib.Scale;
import ptolemy.actor.lib.gui.TimedPlotter;
import ptolemy.data.DoubleToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.ct.kernel.CTCompositeActor;
import ptolemy.domains.ct.kernel.CTEmbeddedDirector;
import ptolemy.domains.ct.kernel.CTMultiSolverDirector;
import ptolemy.domains.ct.lib.Integrator;
import ptolemy.domains.ct.lib.ZeroCrossingDetector;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.domains.fsm.kernel.HSDirector;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.domains.fsm.kernel.Transition;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.plot.Plot;

//////////////////////////////////////////////////////////
//// StickyMasses
/*
@author Jie Liu, Xiaojun Liu
@version $Id$
@since Ptolemy II 1.0
*/
public class StickyMasses extends TypedCompositeActor {

    public StickyMasses(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {

        super(workspace);
        setName("HybridSystem");

        // the top level CT director
        CTMultiSolverDirector topdir = new CTMultiSolverDirector(
                this, "CTTopLevelDirector");
        //topdir.addDebugListener(new StreamListener());

        // parameters:
        stickiness = new Parameter(this, "stickiness", new DoubleToken(-1.0));

        // constant source.
        //Const source1 = new Const(this, "source1");
        //source1.value.setToken(new DoubleToken(0.0));
        //Const source2 = new Const(this, "source2");
        //source2.value.setToken(new DoubleToken(0.0));

        // the plot
        TimedPlotter responsePlot = new TimedPlotter(this, "plot");
        responsePlot.plot = new Plot();
        responsePlot.plot.setGrid(true);
        responsePlot.plot.setTitle("Sticky Masses");
        responsePlot.plot.addLegend(0, "Mass 1 Position");
        responsePlot.plot.addLegend(1, "Mass 2 Position");
        //responsePlot.plot.setWrap(true);
        //responsePlot.plot.setPointsPersistence(1024);
        //responsePlot.plot.setConnected(true);
        responsePlot.plot.setXRange(0.0, 50.0);
        responsePlot.plot.setYRange(0, 3.0);
        responsePlot.plot.setSize(500, 300);

        //System.out.println("Building a simple hybrid system.");
        CTCompositeActor hs = new CTCompositeActor(this, "HS");
        Parameter stickiness1 = new Parameter(hs, "stickiness");
        stickiness1.setExpression("stickiness");
        // the ports
        // Input for ball 1
        //TypedIOPort hsin1 = (TypedIOPort)hs.newPort("force1");
        //hsin1.setInput(true);
        //hsin1.setTypeEquals(BaseType.DOUBLE);
        // Input for ball 2
        //TypedIOPort hsin2 = (TypedIOPort)hs.newPort("force2");
        //hsin2.setInput(true);
        //hsin2.setTypeEquals(BaseType.DOUBLE);
        // Output ball 1 position
        TypedIOPort hsout1 = (TypedIOPort)hs.newPort("P1");
        hsout1.setOutput(true);
        hsout1.setTypeEquals(BaseType.DOUBLE);
        // Output ball 2 position
        TypedIOPort hsout2 = (TypedIOPort)hs.newPort("P2");
        hsout2.setOutput(true);
        hsout2.setTypeEquals(BaseType.DOUBLE);

        //System.out.println("Building the FSM controller.");
        FSMActor ctrl = new FSMActor(hs, "Controller");
        State ctrlInit = new State(ctrl, "Init");
        State ctrlInc = new State(ctrl, "Separate");
        State ctrlDec = new State(ctrl, "Together");
        ctrl.initialStateName.setExpression("Init");
        TypedIOPort ctrlInV1 = new TypedIOPort(ctrl, "V1");
        ctrlInV1.setInput(true);
        ctrlInV1.setTypeEquals(BaseType.DOUBLE);
        TypedIOPort ctrlInV2 = new TypedIOPort(ctrl, "V2");
        ctrlInV2.setInput(true);
        ctrlInV2.setTypeEquals(BaseType.DOUBLE);
        TypedIOPort ctrlInP1 = new TypedIOPort(ctrl, "P1");
        ctrlInP1.setInput(true);
        ctrlInP1.setTypeEquals(BaseType.DOUBLE);
        TypedIOPort ctrlInF = new TypedIOPort(ctrl, "F");
        ctrlInF.setInput(true);
        ctrlInF.setTypeEquals(BaseType.DOUBLE);
        TypedIOPort ctrlInSTI = new TypedIOPort(ctrl, "STI");
        ctrlInSTI.setInput(true);
        ctrlInSTI.setTypeEquals(BaseType.DOUBLE);
        TypedIOPort ctrlInT = new TypedIOPort(ctrl, "touched");
        ctrlInT.setInput(true);
        ctrlInT.setTypeEquals(BaseType.DOUBLE);

        Transition ctrlTr0 = new Transition(ctrl, "Tr0");
        ctrlInit.outgoingPort.link(ctrlTr0);
        ctrlInc.incomingPort.link(ctrlTr0);
        // Always true.
        ctrlTr0.setGuardExpression("true");
        ctrlTr0.preemptive.setExpression("false");

        // Actions on Tr0, setting integrator initial states.
        ctrlTr0.setActions.setExpression(
                "Separate.P1.initialState = 0.0; "
                + "Separate.V1.initialState = 0.0; "
                + "Separate.P2.initialState = 3.0; "
                + "Separate.V2.initialState = 0.0");
        ctrlTr0.reset.setExpression("true");

        Transition ctrlTr1 = new Transition(ctrl, "Tr1");
        ctrlInc.outgoingPort.link(ctrlTr1);
        ctrlDec.incomingPort.link(ctrlTr1);
        ctrlTr1.setGuardExpression("touched_isPresent");
        ctrlTr1.setActions.setExpression(
                "Together.P1.initialState = P1; "
                + "Together.V1.initialState = (V1+V2)/2.0; "
                + "Together.STI.initialState = 10.0");
        ctrlTr1.reset.setExpression("true");

        Transition ctrlTr2 = new Transition(ctrl, "Tr2");
        ctrlDec.outgoingPort.link(ctrlTr2);
        ctrlInc.incomingPort.link(ctrlTr2);
        ctrlTr2.setGuardExpression("F > STI || F < -STI");
        ctrlTr2.setActions.setExpression(
                "Separate.P1.initialState = P1; "
                + "Separate.P2.initialState = P1; "
                + "Separate.V1.initialState = V1; "
                + "Separate.V2.initialState = V1");
        ctrlTr2.reset.setExpression("true");

        // the hybrid system director
        HSDirector hsdir = new HSDirector(hs, "HSDirector");
        hsdir.controllerName.setExpression("Controller");

        CTCompositeActor ctInc = new CTCompositeActor(hs, "Separate");

        //ZeroOrderHold ctIncH1 = new ZeroOrderHold(ctInc, "Hold1");
        Integrator ctIncV1 = new Integrator(ctInc, "V1");
        Integrator ctIncP1 = new Integrator(ctInc, "P1");
        ctIncP1.initialState.setToken(new DoubleToken(0.0));
        Expression ctIncE1 = new Expression(ctInc, "E1");
        //TypedIOPort ctIncE1In = (TypedIOPort)ctIncE1.newPort("In");
        //ctIncE1In.setInput(true);
        //ctIncE1In.setTypeEquals(BaseType.DOUBLE);
        ctIncE1.output.setTypeEquals(BaseType.DOUBLE);
        TypedIOPort ctIncE1P1 = (TypedIOPort)ctIncE1.newPort("P1");
        ctIncE1P1.setInput(true);
        ctIncE1P1.setTypeEquals(BaseType.DOUBLE);
        // The expression is:
        // (K1*Y1 + In - K1*P1)/M1
        ctIncE1.expression.setExpression("1.0*1.0 - 1.0*P1");

        //ZeroOrderHold ctIncH2 = new ZeroOrderHold(ctInc, "Hold2");
        Integrator ctIncV2 = new Integrator(ctInc, "V2");
        Integrator ctIncP2 = new Integrator(ctInc, "P2");
        ctIncP2.initialState.setToken(new DoubleToken(3.0));
        Expression ctIncE2 = new Expression(ctInc, "E2");
        //TypedIOPort ctIncE2In = (TypedIOPort)ctIncE2.newPort("In");
        //ctIncE2In.setInput(true);
        //ctIncE2In.setTypeEquals(BaseType.DOUBLE);
        ctIncE2.output.setTypeEquals(BaseType.DOUBLE);
        TypedIOPort ctIncE2P2 = (TypedIOPort)ctIncE2.newPort("P2");
        ctIncE2P2.setInput(true);
        ctIncE2P2.setTypeEquals(BaseType.DOUBLE);
        // The expression is:
        // (K2*Y2 + In - K2*P2)/M2
        ctIncE2.expression.setExpression("2.0*2.0 - 2.0*P2");

        AddSubtract ctIncE3 = new AddSubtract(ctInc, "E3");
        ZeroCrossingDetector ctIncD =
            new ZeroCrossingDetector(ctInc, "ZD");

        // the ports
        // Force on ball 1
        //TypedIOPort ctIncF1 = (TypedIOPort)ctInc.newPort("force1");
        //ctIncF1.setInput(true);
        //ctIncF1.setTypeEquals(BaseType.DOUBLE);
        // Force on ball 2
        //TypedIOPort ctIncF2 = (TypedIOPort)ctInc.newPort("force2");
        //ctIncF2.setInput(true);
        //ctIncF2.setTypeEquals(BaseType.DOUBLE);
        // Touched trigger
        TypedIOPort ctIncTouched = (TypedIOPort)ctInc.newPort("touched");
        ctIncTouched.setOutput(true);
        ctIncTouched.setTypeEquals(BaseType.DOUBLE);
        // Position of ball 1
        TypedIOPort ctIncOP1 = (TypedIOPort)ctInc.newPort("P1");
        ctIncOP1.setOutput(true);
        ctIncOP1.setTypeEquals(BaseType.DOUBLE);
        // Position of ball 2
        TypedIOPort ctIncOP2 = (TypedIOPort)ctInc.newPort("P2");
        ctIncOP2.setOutput(true);
        ctIncOP2.setTypeEquals(BaseType.DOUBLE);
        // Velocity of ball 1
        TypedIOPort ctIncOV1 = (TypedIOPort)ctInc.newPort("V1");
        ctIncOV1.setOutput(true);
        ctIncOV1.setTypeEquals(BaseType.DOUBLE);
        // Velocity of ball 2
        TypedIOPort ctIncOV2 = (TypedIOPort)ctInc.newPort("V2");
        ctIncOV2.setOutput(true);
        ctIncOV2.setTypeEquals(BaseType.DOUBLE);

        // connect ctInc
        //ctInc.connect(ctIncF1, ctIncH1.input);
        //ctInc.connect(ctIncH1.output, ctIncE1In);
        //ctInc.connect(ctIncF1, ctIncE1In);
        ctInc.connect(ctIncE1.output, ctIncV1.input);
        Relation ctIncRB0 = ctInc.connect(ctIncV1.output, ctIncP1.input);
        ctIncOV1.link(ctIncRB0);
        Relation ctIncRB1 = ctInc.connect(ctIncP1.output, ctIncE1P1);
        ctIncOP1.link(ctIncRB1);

        //ctInc.connect(ctIncF2, ctIncH2.input);
        //ctInc.connect(ctIncH2.output, ctIncE2In);
        //ctInc.connect(ctIncF2, ctIncE2In);
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

        CTEmbeddedDirector ctIncDir = new CTEmbeddedDirector(
                ctInc, "CTIncDir");
        //ctIncDir.addDebugListener(new StreamListener());
        //System.out.println("Building the dynamics of two balls "
        //        + "sticking together.");
        CTCompositeActor ctDec = new CTCompositeActor(hs, "Together");
        Parameter stickiness2 = new Parameter(ctDec, "stickiness");
        stickiness2.setExpression("stickiness");

        Integrator ctDecV1 = new Integrator(ctDec, "V1");
        Integrator ctDecP1 = new Integrator(ctDec, "P1");
        Integrator ctDecSTI = new Integrator(ctDec, "STI");
        Scale ctGain = new Scale(ctDec, "Gain");
        ctGain.factor.setExpression("stickiness");

        Expression ctDecE1 = new Expression(ctDec, "E1");
        TypedIOPort ctDecE1P1 = (TypedIOPort)ctDecE1.newPort("P1");
        ctDecE1P1.setInput(true);
        ctDecE1P1.setTypeEquals(BaseType.DOUBLE);
        ctDecE1.output.setTypeEquals(BaseType.DOUBLE);
        // The expression is:
        // (K1*Y1 + K2*Y2 - K1*P1 - K2*P1)/(M1+M2)
        ctDecE1.expression.setExpression(
                "(1.0*1.0 + 2.0*2.0 - (1.0+2.0)*P1)/2.0");
        Expression ctDecE2 = new Expression(ctDec, "E2");
        TypedIOPort ctDecE2P1 = (TypedIOPort)ctDecE2.newPort("P1");
        ctDecE2P1.setInput(true);
        ctDecE2P1.setTypeEquals(BaseType.DOUBLE);
        ctDecE2.output.setTypeEquals(BaseType.DOUBLE);
        // The expression is:
        // (K1*Y1 - K2*Y2 - K1*P1 + K2*P1)
        ctDecE2.expression.setExpression(
                "1.0*1.0 - 2.0*2.0 - (1.0-2.0)*P1");

        // Sticky force
        TypedIOPort ctDecOSTI = (TypedIOPort)ctDec.newPort("STI");
        ctDecOSTI.setOutput(true);
        ctDecOSTI.setTypeEquals(BaseType.DOUBLE);
        TypedIOPort ctDecOF = (TypedIOPort)ctDec.newPort("F");
        ctDecOF.setOutput(true);
        ctDecOF.setTypeEquals(BaseType.DOUBLE);
        // Position of ball 1
        TypedIOPort ctDecOP1 = (TypedIOPort)ctDec.newPort("P1");
        ctDecOP1.setOutput(true);
        ctDecOP1.setTypeEquals(BaseType.DOUBLE);
        // Position of ball 2
        TypedIOPort ctDecOP2 = (TypedIOPort)ctDec.newPort("P2");
        ctDecOP2.setOutput(true);
        ctDecOP2.setTypeEquals(BaseType.DOUBLE);
        // Velocity of balls
        TypedIOPort ctDecOV1 = (TypedIOPort)ctDec.newPort("V1");
        ctDecOV1.setOutput(true);
        ctDecOV1.setTypeEquals(BaseType.DOUBLE);

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
        CTEmbeddedDirector ctDecDir = new CTEmbeddedDirector(
                ctDec, "CTDecDir");
        //ctDecDir.addDebugListener(new StreamListener());

        // connect the hybrid system
        ctrlInc.refinementName.setExpression("Separate");
        ctrlDec.refinementName.setExpression("Together");
        //hs.connect(hsin1, ctIncF1);
        //hs.connect(hsin2, ctIncF2);
        Relation hsR1 = hs.connect(ctIncOP1, ctDecOP1);
        ctrlInP1.link(hsR1);
        Relation hsR2 = hs.connect(ctIncOP2, ctDecOP2);
        TypedIORelation hsV1 = new TypedIORelation(hs, "HSV1");
        TypedIORelation hsV2 = new TypedIORelation(hs, "HSV2");
        TypedIORelation hsTch = new TypedIORelation(hs, "HSTouched");
        ctIncTouched.link(hsTch);
        ctrlInT.link(hsTch);
        ctIncOV1.link(hsV1);
        ctrlInV1.link(hsV1);
        ctIncOV2.link(hsV2);
        ctrlInV2.link(hsV2);
        ctDecOV1.link(hsV1);
        hsout1.link(hsR1);
        hsout2.link(hsR2);

        TypedIORelation hsF = new TypedIORelation(hs, "HSF");
        ctDecOF.link(hsF);
        ctrlInF.link(hsF);
        TypedIORelation hsSTI = new TypedIORelation(hs, "HSSTI");
        ctDecOSTI.link(hsSTI);
        ctrlInSTI.link(hsSTI);

        // connect the top-level system
        //this.connect(source1.output, hsin1);
        //this.connect(source2.output, hsin2);
        this.connect(hsout1, responsePlot.input);
        this.connect(hsout2, responsePlot.input);

        //System.out.println("Set parameters.");
        // try to run the system
        topdir.startTime.setToken(new DoubleToken(0.0));
        topdir.stopTime.setToken(new DoubleToken(50.0));

        // CT embedded director 1 parameters
        ctIncDir.initStepSize.setToken(new DoubleToken(0.01));

        ctIncDir.minStepSize.setToken(new DoubleToken(1e-5));

        StringToken tok = new StringToken(
                "ptolemy.domains.ct.kernel.solver.BackwardEulerSolver");
        ctIncDir.breakpointODESolver.setToken(tok);
        //Parameter dfsol = (Parameter)ctIncDir.getAttribute("ODESolver");
        tok = new StringToken(
                "ptolemy.domains.ct.kernel.solver.ExplicitRK23Solver");
        ctIncDir.ODESolver.setToken(tok);

        // CT embedded director 2  parbmeters
        ctDecDir.initStepSize.setToken(new DoubleToken(0.01));
        ctDecDir.minStepSize.setToken(new DoubleToken(1e-5));
        tok = new StringToken(
                "ptolemy.domains.ct.kernel.solver.BackwardEulerSolver");
        ctDecDir.breakpointODESolver.setToken(tok);
        tok = new StringToken(
                "ptolemy.domains.ct.kernel.solver.ExplicitRK23Solver");
        ctDecDir.ODESolver.setToken(tok);

        // CT director parameters
        topdir.initStepSize.setToken(new DoubleToken(0.01));
        topdir.minStepSize.setToken(new DoubleToken(1e-5));
        topdir.maxStepSize.setToken(new DoubleToken(0.3));
        tok = new StringToken(
                "ptolemy.domains.ct.kernel.solver.DerivativeResolver");
        topdir.breakpointODESolver.setToken(tok);
        tok = new StringToken(
                "ptolemy.domains.ct.kernel.solver.ExplicitRK23Solver");
        topdir.ODESolver.setToken(tok);
        //System.out.println(exportMoML());
    }

    ///////////////////////////////////////////////////////////////////
    ////                             Parameters                    ////
    public Parameter stickiness;
}
