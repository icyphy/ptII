/* A thermostat control demo that uses Ptolemy II CT and FSM domains.

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

package ptolemy.domains.ct.demo.Thermostat;

import java.awt.BorderLayout;
import java.awt.event.*;

import ptolemy.domains.fsm.kernel.*;
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
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;

//////////////////////////////////////////////////////////////////////////
//// Thermostat
/**
This applet shows a simple thermostat system. The temperature of the room
is expected to be controlled between Tl and Th
<p>
The system has two states, heating and cooling. In the heating state,
the temperature of the room is increased linearly, in terms of a differential
equation:
<pre>
<CODE>    dx/dt = 1</CODE>
</pre>
In the cooling state, the temperature is dropped linearly, i.e.
<pre>
<CODE>    dx/dt = -1</CODE>
</pre>
The control rule is that if the temperature reaches Th degree, then switch
the controller to the cooling state; if the temperature decreases to Tl degree
then switch the controller to the heating state.
<p>
We use this demo to illustrate the accuracy of detecting events, and the
ability of simulating hybrid system in Ptolemy II.
@author Jie Liu
@version $Id$
*/
public class Thermostat extends CTApplet {

    ////////////////////////////////////////////////////////////////////////
////                         public methods                         ////

/** Initialize the applet.
 */
public void init() {

    super.init();
    try {
        // The 1 argument requests only a go button.
        getContentPane().add(_createRunControls(2), BorderLayout.NORTH);

        // the top level composite actor
        _toplevel.setName("HybridSystem");

        // the top level CT director
        CTMultiSolverDirector topdir = new CTMultiSolverDirector(
                _toplevel, "CTTopLevelDirector");
        StreamListener dbl = new StreamListener();
        //topdir.addDebugListener(dbl);
        // a const source
        Const source = new Const(_toplevel, "Const");
        source.value.setToken(new DoubleToken(1.0));

        // the plot
        TimedPlotter responsePlot = new TimedPlotter(_toplevel, "plot");
        responsePlot.place(getContentPane());
        responsePlot.plot.setBackground(getBackground());
        responsePlot.plot.setGrid(true);
        responsePlot.plot.setTitle("Thermostat");
        responsePlot.plot.addLegend(0, "Temperature");
        responsePlot.plot.setConnected(true);
        responsePlot.plot.setImpulses(false);
        //responsePlot.plot.addLegend(1, "Trigger");
        responsePlot.plot.setXRange(0.0, 5.0);
        responsePlot.plot.setYRange(-0.1, 0.3);
        responsePlot.plot.setSize(500, 300);

        CTCompositeActor hs = new CTCompositeActor(_toplevel, "HS");
        // the ports
        TypedIOPort hsin = (TypedIOPort)hs.newPort("input");
        hsin.setInput(true);
        hsin.setTypeEquals(BaseType.DOUBLE);
        //TypedIOPort hsout = (TypedIOPort)hs.newPort("output");
        //hsout.setOutput(true);
        //hsout.setTypeEquals(BaseType.DOUBLE);
        TypedIOPort hsst = (TypedIOPort)hs.newPort("state");
        hsst.setOutput(true);
        hsst.setTypeEquals(BaseType.DOUBLE);
        //TypedIOPort hstr = (TypedIOPort)hs.newPort("trig");
        //hstr.setOutput(true);
        //hstr.setTypeEquals(BaseType.DOUBLE);

        FSMActor ctrl = new FSMActor(hs, "Controller");
        //ctrl.addDebugListener(dbl);
        State ctrlInc = new State(ctrl, "Increasing");
        State ctrlDec = new State(ctrl, "Decreasing");
        ctrl.initialStateName.setExpression("\"Increasing\"");
        Transition ctrlTr1 = new Transition(ctrl, "ctrlTr1");
        ctrlInc.outgoingPort.link(ctrlTr1);
        ctrlDec.incomingPort.link(ctrlTr1);
        ctrlTr1.setGuardExpression("output_S");
        // ctrlTr1.setInitEntry(true);
        SetRefinementVariable act0 =
            new SetRefinementVariable(ctrlTr1, "act0");
        act0.expression.setToken(new StringToken("state_V"));
        act0.variableName.setExpression("\"Integrator.initialState\"");
        ResetRefinement act1 =
            new ResetRefinement(ctrlTr1, "act1");

        Transition ctrlTr2 = new Transition(ctrl, "ctrlTr2");
        ctrlDec.outgoingPort.link(ctrlTr2);
        ctrlInc.incomingPort.link(ctrlTr2);
        ctrlTr2.setGuardExpression("output_S");
        //ctrlTr2.setInitEntry(true);
        SetRefinementVariable act2 =
            new SetRefinementVariable(ctrlTr2, "act2");
        act2.expression.setToken(new StringToken("state_V"));
        act2.variableName.setExpression("\"Integrator.initialState\"");
        ResetRefinement act3 =
            new ResetRefinement(ctrlTr2, "act3");

        IOPort ctrlIn = new TypedIOPort(ctrl, "output");
        ctrlIn.setInput(true);
        IOPort ctrlSt = new TypedIOPort(ctrl, "state");
        ctrlSt.setInput(true);

        // the hybrid system director
        HSDirector hsdir = new HSDirector(hs, "HSDirector");
        //hs.setDirector(hsdir);
        hsdir.controllerName.setExpression("\"Controller\"");
        //hsdir.addDebugListener(dbl);

        CTCompositeActor ctInc = new CTCompositeActor(hs, "Increasing");
        ZeroOrderHold ctIncH = new ZeroOrderHold(ctInc, "Hold");
        Integrator ctIncI = new Integrator(ctInc, "Integrator");
        //ctIncI.addDebugListener(dbl);
        ZeroCrossingDetector ctIncD =
            new ZeroCrossingDetector(ctInc, "ZD");
        //ctIncD.addDebugListener(dbl);
        Expression ctIncGF =
            new Expression(ctInc, "EXPRESSION");

        TypedIOPort ctIncGFi = (TypedIOPort)ctIncGF.newPort("in");
        ctIncGFi.setInput(true);
        ctIncGFi.setTypeEquals(BaseType.DOUBLE);
        ctIncGF.output.setTypeEquals(BaseType.DOUBLE);
        ctIncGF.expression.setExpression("in - 0.2");
        // the ports
        TypedIOPort ctIncIn = (TypedIOPort)ctInc.newPort("input");
        ctIncIn.setInput(true);
        ctIncIn.setTypeEquals(BaseType.DOUBLE);
        TypedIOPort ctIncOut = (TypedIOPort)ctInc.newPort("output");
        ctIncOut.setOutput(true);
        ctIncOut.setTypeEquals(BaseType.DOUBLE);
        TypedIOPort ctIncSt = (TypedIOPort)ctInc.newPort("state");
        ctIncSt.setOutput(true);
        ctIncSt.setTypeEquals(BaseType.DOUBLE);
        TypedIOPort ctIncTr = (TypedIOPort)ctInc.newPort("trig");
        ctIncTr.setOutput(true);
        ctIncTr.setTypeEquals(BaseType.DOUBLE);
        // connect ctInc
        ctInc.connect(ctIncIn, ctIncH.input);
        ctInc.connect(ctIncH.output, ctIncI.input);
        Relation ctIncR2 = ctInc.newRelation("R2");
        ctIncGF.output.link(ctIncR2);
        ctIncD.trigger.link(ctIncR2);
        ctIncTr.link(ctIncR2);
        ctInc.connect(ctIncD.output, ctIncOut);
        //ctInc.connect(ctIncS.output, ctIncSt);
        TypedIORelation ctIncR1 =
            (TypedIORelation)ctInc.newRelation("CTIncR1");
        ctIncI.output.link(ctIncR1);
        //ctIncS.input.link(ctIncR1);
        ctIncD.input.link(ctIncR1);
        ctIncGFi.link(ctIncR1);
        ctIncSt.link(ctIncR1);
        CTEmbeddedDirector ctIncDir = new CTEmbeddedDirector(
                ctInc, "CTIncDir");
        //ctIncDir.addDebugListener(dbl);

        CTCompositeActor ctDec = new CTCompositeActor(hs, "Decreasing");
        //ctDec.addDebugListener(dbl);
        ZeroOrderHold ctDecH = new ZeroOrderHold(ctDec, "Hold");
        Integrator ctDecI = new Integrator(ctDec, "Integrator");
        Scale ctGain = new Scale(ctDec, "Gain");
        ZeroCrossingDetector ctDecD =
            new ZeroCrossingDetector(ctDec, "ZD");

        Expression ctDecGF =
            new Expression(ctDec, "EXPRESSION");
        TypedIOPort ctDecGFi = (TypedIOPort)ctDecGF.newPort("in");
        ctDecGFi.setInput(true);
        ctDecGFi.setTypeEquals(BaseType.DOUBLE);
        ctDecGF.output.setTypeEquals(BaseType.DOUBLE);
        ctDecGF.expression.setExpression("in + 0.0");
        // the ports
        TypedIOPort ctDecIn = (TypedIOPort)ctDec.newPort("input");
        ctDecIn.setInput(true);
        ctDecIn.setTypeEquals(BaseType.DOUBLE);
        TypedIOPort ctDecOut = (TypedIOPort)ctDec.newPort("output");
        ctDecOut.setOutput(true);
        ctDecOut.setTypeEquals(BaseType.DOUBLE);
        TypedIOPort ctDecSt = (TypedIOPort)ctDec.newPort("state");
        ctDecSt.setOutput(true);
        ctDecSt.setTypeEquals(BaseType.DOUBLE);
        TypedIOPort ctDecTr = (TypedIOPort)ctDec.newPort("trig");
        ctDecTr.setOutput(true);
        ctDecTr.setTypeEquals(BaseType.DOUBLE);
        // connect ctDec
        ctDec.connect(ctDecIn, ctDecH.input);
        ctDec.connect(ctDecH.output, ctGain.input);
        ctDec.connect(ctGain.output, ctDecI.input);
        Relation ctDecR2 = ctDec.newRelation("R2");
        ctDecGF.output.link(ctDecR2);
        ctDecD.trigger.link(ctDecR2);
        ctDecTr.link(ctDecR2);
        ctDec.connect(ctDecD.output, ctDecOut);
        //ctDec.connect(ctDecS.output, ctDecSt);
        TypedIORelation ctDecR1 =
            (TypedIORelation)ctDec.newRelation("CTDecR1");
        ctDecI.output.link(ctDecR1);
        //ctDecS.input.link(ctDecR1);
        ctDecD.input.link(ctDecR1);
        ctDecGFi.link(ctDecR1);
        ctDecSt.link(ctDecR1);
        CTEmbeddedDirector ctDecDir = new CTEmbeddedDirector(
                ctDec, "CTDecDir");
        //ctDecDir.addDebugListener(dbl);

        ctrlInc.refinementName.setExpression("\"Increasing\"");
        ctrlDec.refinementName.setExpression("\"Decreasing\"");

        // connect hs
        TypedIORelation hsr1 = (TypedIORelation)hs.newRelation("HSr1");
        hsin.link(hsr1);
        ctIncIn.link(hsr1);
        ctDecIn.link(hsr1);
        TypedIORelation hsr2 = (TypedIORelation)hs.newRelation("HSr2");
        ctrlIn.link(hsr2);
        ctIncOut.link(hsr2);
        ctDecOut.link(hsr2);
        TypedIORelation hsr3 = (TypedIORelation)hs.newRelation("HSr3");
        hsst.link(hsr3);
        ctIncSt.link(hsr3);
        ctDecSt.link(hsr3);
        ctrlSt.link(hsr3);
        Relation hsr4 = hs.newRelation("HSr4");
        //hstr.link(hsr4);
        ctIncTr.link(hsr4);
        ctDecTr.link(hsr4);

        // connect the top level system
        _toplevel.connect(source.output, hsin);
        //sys.connect(hsout, responsePlot.input);
        _toplevel.connect(hsst, responsePlot.input);
        //_toplevel.connect(hstr, responsePlot.input);

        // try to run the system
        topdir.setStopTime(5.0);

        // CT embedded director 1 parameters
        ctIncDir.initStepSize.setToken(new DoubleToken(0.01));
        ctIncDir.minStepSize.setToken(new DoubleToken(1e-3));
        ctIncDir.maxStepSize.setToken(new DoubleToken(0.05));

        StringToken tok = new StringToken(
                "ptolemy.domains.ct.kernel.solver.DerivativeResolver");
        ctIncDir.breakpointODESolver.setToken(tok);
        Parameter dfsol = (Parameter)ctIncDir.getAttribute("ODESolver");
        tok = new StringToken(
                "ptolemy.domains.ct.kernel.solver.ExplicitRK23Solver");
        ctIncDir.ODESolver.setToken(tok);

        // CT embedded director 2  parameters
        ctDecDir.initStepSize.setToken(new DoubleToken(0.01));
        ctDecDir.minStepSize.setToken(new DoubleToken(1e-3));
        ctDecDir.maxStepSize.setToken(new DoubleToken(0.05));

        tok = new StringToken(
                "ptolemy.domains.ct.kernel.solver.DerivativeResolver");
        ctDecDir.breakpointODESolver.setToken(tok);
        tok = new StringToken(
                "ptolemy.domains.ct.kernel.solver.ExplicitRK23Solver");
        ctDecDir.ODESolver.setToken(tok);
        ctGain.factor.setToken(new DoubleToken(-1.0));

        // CT director parameters
        topdir.initStepSize.setToken(new DoubleToken(0.01));
        topdir.minStepSize.setToken(new DoubleToken(1e-3));
        topdir.maxStepSize.setToken(new DoubleToken(0.05));
        tok = new StringToken(
                "ptolemy.domains.ct.kernel.solver.DerivativeResolver");
        topdir.breakpointODESolver.setToken(tok);
        tok = new StringToken(
                "ptolemy.domains.ct.kernel.solver.ExplicitRK23Solver");
        topdir.ODESolver.setToken(tok);
    } catch (KernelException ex) {
        report("Setup failed:", ex);
    }
}
}
