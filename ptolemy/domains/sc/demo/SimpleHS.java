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

package ptolemy.domains.sc.demo;

import ptolemy.actor.*;
import ptolemy.domains.de.kernel.*;
import ptolemy.domains.de.lib.*;
import ptolemy.domains.sc.kernel.*;
import ptolemy.domains.sc.lib.*;
import ptolemy.domains.ct.kernel.*;
import ptolemy.domains.ct.lib.*;
import ptolemy.automata.util.*;
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
            DEDirector dedir = new DEDirector("DETopLevelDirector");
            sys.setDirector(dedir);

            // a DE clock
            DEClock clk = new DEClock(sys, "Clock");
            Parameter intv = (Parameter)clk.getAttribute("interval");
            intv.setToken(new DoubleToken(0.1));

            // a DE ramp
            DERamp ramp = new DERamp(sys, "Ramp", 1.0, 0.0);

            // the plot
            DEPlot plot = new DEPlot(sys, "Plot");

            // a simple hybrid system
            TypedCompositeActor hs = new TypedCompositeActor(sys, "HS");
            // the ports
            TypedIOPort hsin = (TypedIOPort)hs.newPort("input");
            hsin.setInput(true);
            hsin.setDeclaredType(DoubleToken.class);
            TypedIOPort hsout = (TypedIOPort)hs.newPort("output");
            hsout.setOutput(true);
            hsout.setDeclaredType(DoubleToken.class);
            TypedIOPort hsst = (TypedIOPort)hs.newPort("state");
            hsst.setOutput(true);
            hsst.setDeclaredType(DoubleToken.class);
 

            // the FSM controller
            SCController ctrl = new DEFSMActor(hs, "Controller");
            SCState ctrlInc = new SCState(ctrl, "Increasing");
            SCState ctrlDec = new SCState(ctrl, "Decreasing");
            ctrl.setInitialState(ctrlInc);
            SCTransition ctrlTr1 = 
                    ctrl.createTransition(ctrlInc, ctrlDec);
            ctrlTr1.setTriggerEvent("output");
            HSInit hsinit1 = new HSInit(ctrlTr1, "Integrator", "output");
            SCTransition ctrlTr2 = 
                    ctrl.createTransition(ctrlDec, ctrlInc);            
            ctrlTr2.setTriggerEvent("output");
            HSInit hsinit2 = new HSInit(ctrlTr2, "Integrator", "output");

            // the hybrid system director
            SCDirector hsdir = new SCDirector("HSDirector");
            hs.setDirector(hsdir);
            hsdir.setController(ctrl);

            // the first ct subsystem
            TypedCompositeActor ctInc = new TypedCompositeActor(hs, "Increasing");
            CTZeroOrderHold ctIncH = new CTZeroOrderHold(ctInc, "Hold");
            CTIntegrator ctIncI = new CTIntegrator(ctInc, "Integrator");
            CTZeroCrossingDetector ctIncD = new CTZeroCrossingDetector(ctInc, "ZD");
            GeneralFunctionActor ctIncGF = new GeneralFunctionActor(ctInc, "GF");
            CTPeriodicalSampler ctIncS = new CTPeriodicalSampler(ctInc, "Sample");
            TypedIOPort ctIncGFi = (TypedIOPort)ctIncGF.newPort("in");
            ctIncGFi.setInput(true);
            ctIncGFi.setDeclaredType(DoubleToken.class);
            TypedIOPort ctIncGFo = (TypedIOPort)ctIncGF.newPort("out");
            ctIncGFo.setOutput(true);
            ctIncGFo.setDeclaredType(DoubleToken.class);
            ctIncGF.setOutputExpression("out", "in - 2.5");
            // the ports
            TypedIOPort ctIncIn = (TypedIOPort)ctInc.newPort("input");
            ctIncIn.setInput(true);
            ctIncIn.setDeclaredType(DoubleToken.class);
            TypedIOPort ctIncOut = (TypedIOPort)ctInc.newPort("output");
            ctIncOut.setOutput(true);
            ctIncOut.setDeclaredType(DoubleToken.class);
            TypedIOPort ctIncSt = (TypedIOPort)ctInc.newPort("state");
            ctIncSt.setOutput(true);
            ctIncSt.setDeclaredType(DoubleToken.class);
            // connect ctInc
            ctInc.connect(ctIncIn, ctIncH.input);
            ctInc.connect(ctIncH.output, ctIncI.input);
            ctInc.connect(ctIncGFo, ctIncD.trigger);
            ctInc.connect(ctIncD.output, ctIncOut);
            ctInc.connect(ctIncS.output, ctIncSt);
            TypedIORelation ctIncR1 = (TypedIORelation)ctInc.newRelation("CTIncR1");
            ctIncI.output.link(ctIncR1);
            ctIncS.input.link(ctIncR1);
            ctIncD.input.link(ctIncR1);
            ctIncGFi.link(ctIncR1);
            CTMixedSignalDirector ctIncDir = new CTMixedSignalDirector("CTIncDir");
            ctInc.setDirector(ctIncDir);

            // the second ct subsystem
            TypedCompositeActor ctDec = new TypedCompositeActor(hs, "Decreasing");
            CTZeroOrderHold ctDecH = new CTZeroOrderHold(ctDec, "Hold");
            CTIntegrator ctDecI = new CTIntegrator(ctDec, "Integrator");
            CTGain ctGain = new CTGain(ctDec, "Gain");
            CTZeroCrossingDetector ctDecD = new CTZeroCrossingDetector(ctDec, "ZD");
            GeneralFunctionActor ctDecGF = new GeneralFunctionActor(ctDec, "GF");
            CTPeriodicalSampler ctDecS = new CTPeriodicalSampler(ctDec, "Sample");
            TypedIOPort ctDecGFi = (TypedIOPort)ctDecGF.newPort("in");
            ctDecGFi.setInput(true);
            ctDecGFi.setDeclaredType(DoubleToken.class);
            TypedIOPort ctDecGFo = (TypedIOPort)ctDecGF.newPort("out");
            ctDecGFo.setOutput(true);
            ctDecGFo.setDeclaredType(DoubleToken.class);
            ctDecGF.setOutputExpression("out", "in + 0.0");
            // the ports
            TypedIOPort ctDecIn = (TypedIOPort)ctDec.newPort("input");
            ctDecIn.setInput(true);
            ctDecIn.setDeclaredType(DoubleToken.class);
            TypedIOPort ctDecOut = (TypedIOPort)ctDec.newPort("output");
            ctDecOut.setOutput(true);
            ctDecOut.setDeclaredType(DoubleToken.class);
            TypedIOPort ctDecSt = (TypedIOPort)ctDec.newPort("state");
            ctDecSt.setOutput(true);
            ctDecSt.setDeclaredType(DoubleToken.class);
            // connect ctDec
            ctDec.connect(ctDecIn, ctDecH.input);
            ctDec.connect(ctDecH.output, ctGain.input);
            ctDec.connect(ctGain.output, ctDecI.input);
            ctDec.connect(ctDecGFo, ctDecD.trigger);
            ctDec.connect(ctDecD.output, ctDecOut);
            ctDec.connect(ctDecS.output, ctDecSt);
            TypedIORelation ctDecR1 = (TypedIORelation)ctDec.newRelation("CTDecR1");
            ctDecI.output.link(ctDecR1);
            ctDecS.input.link(ctDecR1);
            ctDecD.input.link(ctDecR1);
            ctDecGFi.link(ctDecR1);
            CTMixedSignalDirector ctDecDir = new CTMixedSignalDirector("CTDecDir");
            ctDec.setDirector(ctDecDir);

            ctrlInc.setRefinement(ctInc);
            ctrlDec.setRefinement(ctDec);

            // connect hs
            TypedIORelation hsr1 = (TypedIORelation)hs.newRelation("HSr1");
            hsin.link(hsr1);
            ctIncIn.link(hsr1);
            ctDecIn.link(hsr1);
            TypedIORelation hsr2 = (TypedIORelation)hs.newRelation("HSr2");
            hsout.link(hsr2);
            ctIncOut.link(hsr2);
            ctDecOut.link(hsr2);
            TypedIORelation hsr3 = (TypedIORelation)hs.newRelation("HSr3");
            hsst.link(hsr3);
            ctIncSt.link(hsr3);
            ctDecSt.link(hsr3);

            // connect the top level system
            sys.connect(clk.output, ramp.input);
            sys.connect(ramp.output, hsin);
            sys.connect(hsout, plot.input);
            sys.connect(hsst, plot.input);

            // try to run the system
            dedir.setStopTime(15.0);

            // CT director parameters
            Parameter initStep = (Parameter)ctIncDir.getAttribute("InitialStepSize");
            initStep.setExpression("0.001");
            initStep.parameterChanged(null);
            Parameter minStep = (Parameter)ctIncDir.getAttribute("MinimumStepSize");
            minStep.setExpression("1e-3");
            minStep.parameterChanged(null);
            Parameter bpsol = (Parameter)ctIncDir.getAttribute("BreakpointODESolver");
            StringToken tok = new StringToken("ptolemy.domains.ct.kernel.solver.BackwardEulerSolver");
            bpsol.setToken(tok);
            bpsol.parameterChanged(null);
            Parameter dfsol = (Parameter)ctIncDir.getAttribute("ODESolver");
            tok = new StringToken("ptolemy.domains.ct.kernel.solver.ExplicitRK23Solver");
            dfsol.setToken(tok);
            dfsol.parameterChanged(null);

            Parameter sp = (Parameter)ctIncS.getAttribute("SamplePeriod");
            sp.setExpression("0.1");
            sp.parameterChanged(null);

            // CT director parameters
            initStep = (Parameter)ctDecDir.getAttribute("InitialStepSize");
            initStep.setExpression("0.001");
            initStep.parameterChanged(null);
            minStep = (Parameter)ctDecDir.getAttribute("MinimumStepSize");
            minStep.setExpression("1e-3");
            minStep.parameterChanged(null);
            bpsol = (Parameter)ctDecDir.getAttribute("BreakpointODESolver");
            tok = new StringToken("ptolemy.domains.ct.kernel.solver.BackwardEulerSolver");
            bpsol.setToken(tok);
            bpsol.parameterChanged(null);
            dfsol = (Parameter)ctDecDir.getAttribute("ODESolver");
            tok = new StringToken("ptolemy.domains.ct.kernel.solver.ExplicitRK23Solver");
            dfsol.setToken(tok);
            dfsol.parameterChanged(null);

            sp = (Parameter)ctDecS.getAttribute("SamplePeriod");
            sp.setExpression("0.1");
            sp.parameterChanged(null);
            Parameter gain = (Parameter)ctGain.getAttribute("Gain");
            gain.setExpression("-1.0");
            gain.parameterChanged(null);

            mgr.startRun();

        } catch (KernelException ex) {
            throw new InvalidStateException("Error in running simple hybrid "
                    + "system demo: " + ex.getMessage());
        }
    }
}
