/* A very simple hybrid system.

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

@ProposedRating Red (liuxj@eecs.berkeley.edu)

*/

package ptolemy.domains.fsm.demo;

import ptolemy.actor.*;
//import ptolemy.domains.de.kernel.*;
//import ptolemy.domains.de.lib.*;
import ptolemy.domains.fsm.kernel.*;
import ptolemy.domains.fsm.lib.*;
import ptolemy.domains.ct.kernel.*;
import ptolemy.domains.ct.lib.*;
import ptolemy.data.expr.Variable;
import ptolemy.data.expr.VariableList;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// SimpleHS - A very simple hybrid system.
/**
An application demonstrating the hierarchical combination of DE, FSM, and 
CT domains in PtolemyII.
@author Jie Liu, Xiaojun Liu
@version $Id$
*/
public class SimpleHS {

    public static void main(String[] argv) {
        try {

            // the top level composite actor
            TypedCompositeActor sys = new TypedCompositeActor();
            sys.setName("HybridSystem");

            // the top level manager
            Manager mgr = new Manager();
            sys.setManager(mgr);

            // the top level DE director
            CTMultiSolverDirector dedir = new CTMultiSolverDirector(
                    sys, "CTTopLevelDirector");

            //dedir.setVERBOSE(true);
            //dedir.setDEBUG(true);


            // a DE clock
            //DEClock clk = new DEClock(sys, "Clock");
            //Parameter intv = (Parameter)clk.getAttribute("interval");
            //intv.setToken(new DoubleToken(0.1));

            // a DE ramp
            CTRamp ramp = new CTRamp(sys, "Ramp");
            Parameter p = (Parameter)ramp.getAttribute("InitialValue");
            p.setToken(new DoubleToken(1.0));
            p = (Parameter)ramp.getAttribute("Slope");
            p.setToken(new DoubleToken(0.0));

            // the plot
            CTPlot plot = new CTPlot(sys, "Plot");

            // a simple hybrid system
            CTCompositeActor hs = new CTCompositeActor(sys, "HS");
            // the ports
            TypedIOPort hsin = (TypedIOPort)hs.newPort("input");
            hsin.setInput(true);
            hsin.setTypeEquals(DoubleToken.class);
            //TypedIOPort hsout = (TypedIOPort)hs.newPort("output");
            //hsout.setOutput(true);
            //hsout.setTypeEquals(DoubleToken.class);
            TypedIOPort hsst = (TypedIOPort)hs.newPort("state");
            hsst.setOutput(true);
            hsst.setTypeEquals(DoubleToken.class);
            TypedIOPort hstr = (TypedIOPort)hs.newPort("trig");
            hstr.setOutput(true);
            hstr.setTypeEquals(DoubleToken.class);
 

            // the FSM controller
            HSController ctrl = new HSController(hs, "Controller");
            FSMState ctrlInc = new FSMState(ctrl, "Increasing");
            FSMState ctrlDec = new FSMState(ctrl, "Decreasing");
            ctrl.setInitialState(ctrlInc);
            FSMTransition ctrlTr1 = 
                ctrl.createTransition(ctrlInc, ctrlDec);
            ctrlTr1.setTriggerEvent("output");
            // ctrlTr1.setInitEntry(true);
            HSInit hsinit1 = new HSInit(ctrlTr1, "Integrator", "state");
            FSMTransition ctrlTr2 = 
                ctrl.createTransition(ctrlDec, ctrlInc);            
            ctrlTr2.setTriggerEvent("output");
            //ctrlTr2.setInitEntry(true);
            HSInit hsinit2 = new HSInit(ctrlTr2, "Integrator", "state");

            // the hybrid system director
            HSDirector hsdir = new HSDirector(hs, "HSDirector");
            hsdir.setController(ctrl);

            // the first ct subsystem
            CTCompositeActor ctInc = new CTCompositeActor(hs, "Increasing");
            CTZeroOrderHold ctIncH = new CTZeroOrderHold(ctInc, "Hold");
            CTIntegrator ctIncI = new CTIntegrator(ctInc, "Integrator");
            CTZeroCrossingDetector ctIncD = new CTZeroCrossingDetector(ctInc, "ZD");
            GeneralFunctionActor ctIncGF = new GeneralFunctionActor(ctInc, "GF");
            //CTPeriodicalSampler ctIncS = new CTPeriodicalSampler(ctInc, "Sample");
            TypedIOPort ctIncGFi = (TypedIOPort)ctIncGF.newPort("in");
            ctIncGFi.setInput(true);
            ctIncGFi.setTypeEquals(DoubleToken.class);
            TypedIOPort ctIncGFo = (TypedIOPort)ctIncGF.newPort("out");
            ctIncGFo.setOutput(true);
            ctIncGFo.setTypeEquals(DoubleToken.class);
            ctIncGF.setOutputExpression("out", "in - 0.2");
            // the ports
            TypedIOPort ctIncIn = (TypedIOPort)ctInc.newPort("input");
            ctIncIn.setInput(true);
            ctIncIn.setTypeEquals(DoubleToken.class);
            TypedIOPort ctIncOut = (TypedIOPort)ctInc.newPort("output");
            ctIncOut.setOutput(true);
            ctIncOut.setTypeEquals(DoubleToken.class);
            TypedIOPort ctIncSt = (TypedIOPort)ctInc.newPort("state");
            ctIncSt.setOutput(true);
            ctIncSt.setTypeEquals(DoubleToken.class);
            TypedIOPort ctIncTr = (TypedIOPort)ctInc.newPort("trig");
            ctIncTr.setOutput(true);
            ctIncTr.setTypeEquals(DoubleToken.class);
            // connect ctInc
            ctInc.connect(ctIncIn, ctIncH.input);
            ctInc.connect(ctIncH.output, ctIncI.input);
            //ctInc.connect(ctIncGFo, ctIncD.trigger);
            Relation ctIncR2 = ctInc.newRelation("R2");
            ctIncGFo.link(ctIncR2);
            ctIncD.trigger.link(ctIncR2);
            ctIncTr.link(ctIncR2);
            ctInc.connect(ctIncD.output, ctIncOut);
            //ctInc.connect(ctIncS.output, ctIncSt);
            TypedIORelation ctIncR1 = (TypedIORelation)ctInc.newRelation("CTIncR1");
            ctIncI.output.link(ctIncR1);
            //ctIncS.input.link(ctIncR1);
            ctIncD.input.link(ctIncR1);
            ctIncGFi.link(ctIncR1);
            ctIncSt.link(ctIncR1);
            CTEmbeddedNRDirector ctIncDir = new CTEmbeddedNRDirector(
                    ctInc, "CTIncDir");

            // the second ct subsystem
            CTCompositeActor ctDec = new CTCompositeActor(hs, "Decreasing");
            CTZeroOrderHold ctDecH = new CTZeroOrderHold(ctDec, "Hold");
            CTIntegrator ctDecI = new CTIntegrator(ctDec, "Integrator");
            CTGain ctGain = new CTGain(ctDec, "Gain");
            CTZeroCrossingDetector ctDecD = new CTZeroCrossingDetector(ctDec, "ZD");
            GeneralFunctionActor ctDecGF = new GeneralFunctionActor(ctDec, "GF");
            //CTPeriodicalSampler ctDecS = new CTPeriodicalSampler(ctDec, "Sample");
            TypedIOPort ctDecGFi = (TypedIOPort)ctDecGF.newPort("in");
            ctDecGFi.setInput(true);
            ctDecGFi.setTypeEquals(DoubleToken.class);
            TypedIOPort ctDecGFo = (TypedIOPort)ctDecGF.newPort("out");
            ctDecGFo.setOutput(true);
            ctDecGFo.setTypeEquals(DoubleToken.class);
            ctDecGF.setOutputExpression("out", "in + 0.0");
            // the ports
            TypedIOPort ctDecIn = (TypedIOPort)ctDec.newPort("input");
            ctDecIn.setInput(true);
            ctDecIn.setTypeEquals(DoubleToken.class);
            TypedIOPort ctDecOut = (TypedIOPort)ctDec.newPort("output");
            ctDecOut.setOutput(true);
            ctDecOut.setTypeEquals(DoubleToken.class);
            TypedIOPort ctDecSt = (TypedIOPort)ctDec.newPort("state");
            ctDecSt.setOutput(true);
            ctDecSt.setTypeEquals(DoubleToken.class);
            TypedIOPort ctDecTr = (TypedIOPort)ctDec.newPort("trig");
            ctDecTr.setOutput(true);
            ctDecTr.setTypeEquals(DoubleToken.class);
            // connect ctDec
            ctDec.connect(ctDecIn, ctDecH.input);
            ctDec.connect(ctDecH.output, ctGain.input);
            ctDec.connect(ctGain.output, ctDecI.input);
            //ctDec.connect(ctDecGFo, ctDecD.trigger);
            Relation ctDecR2 = ctDec.newRelation("R2");
            ctDecGFo.link(ctDecR2);
            ctDecD.trigger.link(ctDecR2);
            ctDecTr.link(ctDecR2);
            ctDec.connect(ctDecD.output, ctDecOut);
            //ctDec.connect(ctDecS.output, ctDecSt);
            TypedIORelation ctDecR1 = (TypedIORelation)ctDec.newRelation("CTDecR1");
            ctDecI.output.link(ctDecR1);
            //ctDecS.input.link(ctDecR1);
            ctDecD.input.link(ctDecR1);
            ctDecGFi.link(ctDecR1);
            ctDecSt.link(ctDecR1);
            CTEmbeddedNRDirector ctDecDir = new CTEmbeddedNRDirector(
                    ctDec, "CTDecDir");

            ctrlInc.setRefinement(ctInc);
            ctrlDec.setRefinement(ctDec);

            // connect hs
            TypedIORelation hsr1 = (TypedIORelation)hs.newRelation("HSr1");
            hsin.link(hsr1);
            ctIncIn.link(hsr1);
            ctDecIn.link(hsr1);
            TypedIORelation hsr2 = (TypedIORelation)hs.newRelation("HSr2");
            //hsout.link(hsr2);
            ctIncOut.link(hsr2);
            ctDecOut.link(hsr2);
            TypedIORelation hsr3 = (TypedIORelation)hs.newRelation("HSr3");
            hsst.link(hsr3);
            ctIncSt.link(hsr3);
            ctDecSt.link(hsr3);
            Relation hsr4 = hs.newRelation("HSr4");
            hstr.link(hsr4);
            ctIncTr.link(hsr4);
            ctDecTr.link(hsr4);

            // connect the top level system
            //sys.connect(clk.output, ramp.input);
            sys.connect(ramp.output, hsin);
            //sys.connect(hsout, plot.input);
            sys.connect(hsst, plot.input);
            sys.connect(hstr, plot.input);

            //System.out.println("HSOUT: " + hsout.numLinks() + " " + hsout.numInsideLinks());
            //System.out.println("HSTR: " + hstr.numLinks() + " " + hstr.numInsideLinks());

            // try to run the system
            dedir.setStopTime(5.0);
           
            // CT director parameters
            Parameter initStep = (Parameter)ctIncDir.getAttribute("InitialStepSize");
            initStep.setToken(new DoubleToken(0.01));
            Parameter minStep = (Parameter)ctIncDir.getAttribute("MinimumStepSize");
            minStep.setToken(new DoubleToken(1e-3));
            Parameter bpsol = (Parameter)ctIncDir.getAttribute("BreakpointODESolver");
            StringToken tok = new StringToken("ptolemy.domains.ct.kernel.solver.BackwardEulerSolver");
            bpsol.setToken(tok);
            Parameter dfsol = (Parameter)ctIncDir.getAttribute("ODESolver");
            tok = new StringToken("ptolemy.domains.ct.kernel.solver.ForwardEulerSolver");
            dfsol.setToken(tok);

            //Parameter sp = (Parameter)ctIncS.getAttribute("SamplePeriod");
            //sp.setToken(new DoubleToken(0.1));

            // CT director parameters
            initStep = (Parameter)ctDecDir.getAttribute("InitialStepSize");
            initStep.setToken(new DoubleToken(0.01));
            minStep = (Parameter)ctDecDir.getAttribute("MinimumStepSize");
            minStep.setToken(new DoubleToken(1e-3));
            bpsol = (Parameter)ctDecDir.getAttribute("BreakpointODESolver");
            tok = new StringToken("ptolemy.domains.ct.kernel.solver.BackwardEulerSolver");
            bpsol.setToken(tok);
            dfsol = (Parameter)ctDecDir.getAttribute("ODESolver");
            tok = new StringToken("ptolemy.domains.ct.kernel.solver.ForwardEulerSolver");
            dfsol.setToken(tok);

            //sp = (Parameter)ctDecS.getAttribute("SamplePeriod");
            //sp.setToken(new DoubleToken(0.1));
            Parameter gain = (Parameter)ctGain.getAttribute("Gain");
            gain.setToken(new DoubleToken(-1.0));

            // CT director parameters
            initStep = (Parameter)dedir.getAttribute("InitialStepSize");
            initStep.setToken(new DoubleToken(0.01));
            minStep = (Parameter)dedir.getAttribute("MinimumStepSize");
            minStep.setToken(new DoubleToken(1e-3));
            minStep = (Parameter)dedir.getAttribute("MaximumStepSize");
            minStep.setToken(new DoubleToken(0.05));
            bpsol = (Parameter)dedir.getAttribute("BreakpointODESolver");
            tok = new StringToken("ptolemy.domains.ct.kernel.solver.BackwardEulerSolver");
            bpsol.setToken(tok);
            dfsol = (Parameter)dedir.getAttribute("ODESolver");
            tok = new StringToken("ptolemy.domains.ct.kernel.solver.ExplicitRK23Solver");
            dfsol.setToken(tok);


            System.out.println(ctIncDir.getScheduler().description());
            mgr.startRun();

        } catch (KernelException ex) {
            throw new InvalidStateException("Error in running simple hybrid "
                    + "system demo: " + ex.getMessage());
        }
    }
}
