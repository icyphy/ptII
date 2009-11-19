/* A micro-accelerometer demo that uses CT and DE domains.

 Copyright (c) 1998-2005 The Regents of the University of California.
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
package ptolemy.domains.ct.demo.SigmaDelta;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.actor.lib.AddSubtract;
import ptolemy.actor.lib.Average;
import ptolemy.actor.lib.Clock;
import ptolemy.actor.lib.CurrentTime;
import ptolemy.actor.lib.Quantizer;
import ptolemy.actor.lib.Scale;
import ptolemy.actor.lib.TrigFunction;
import ptolemy.actor.lib.gui.TimedPlotter;
import ptolemy.data.DoubleToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.ct.kernel.CTMixedSignalDirector;
import ptolemy.domains.ct.lib.CTPeriodicSampler;
import ptolemy.domains.ct.lib.Integrator;
import ptolemy.domains.ct.lib.ZeroOrderHold;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.domains.de.lib.Sampler;
import ptolemy.domains.sdf.lib.FIR;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.plot.Plot;

//////////////////////////////////////////////////////////////////////////
//// SigmaDelta

/**
 A demo of Sigma Delta quantization. The underlying model is a
 micro-accelerometer which uses beam-gap capacitor to measure accelerations.
 A second order CT subsystem is used to model the beam.
 The voltage on the beam-gap capacitor is sampled every T seconds (much
 faster than the required output of the digital signal),  then filtered by
 a lead compensator (FIR filter), and fed to an one-bit quantizer.
 The outputs of the quantizer are converted to force and fed back to the
 beams. The outputs are also counted and averaged every N*T seconds to
 produce the digital output. In our example, the external acceleration
 is a Sin wave.
 Reference:  Mark A. Lemkin, <I>"Micro Accelerometer Design with Digital
 Feedback Control"</I>, doctoral dissertation,  University of California,
 Berkeley, Fall 1997

 @author Jie Liu
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Red (liuj)
 @Pt.AcceptedRating Red (cxh)
 */
public class SigmaDelta extends TypedCompositeActor {
    public SigmaDelta(Workspace workspace) throws IllegalActionException,
            NameDuplicationException {
        // Creating the model.
        super(workspace);
        setName("DigitalSystem");

        // Set up the top level composite actor, director and manager
        DEDirector deDirector = new DEDirector(this, "DEDirector");

        //deDirector.addDebugListener(new StreamListener());
        double stopT = 15.0;
        deDirector.stopTime.setToken(new DoubleToken(stopT));

        // Create toplevel parameters.
        samplePeriod = new Parameter(this, "samplePeriod",
                new DoubleToken(0.02));
        feedbackGain = new Parameter(this, "feedbackGain", new DoubleToken(
                -20.0));

        // CT subsystem
        TypedCompositeActor ctsub = new TypedCompositeActor(this, "CTSubsystem");
        Parameter ctSamplePeriod = new Parameter(ctsub, "samplePeriod");
        ctSamplePeriod.setExpression("samplePeriod");

        Parameter ctFeedbackGain = new Parameter(ctsub, "feedbackGain");
        ctFeedbackGain.setExpression("feedbackGain");

        TypedIOPort subin = new TypedIOPort(ctsub, "Pin");
        subin.setInput(true);

        TypedIOPort subout = new TypedIOPort(ctsub, "Pout");
        subout.setOutput(true);

        CTMixedSignalDirector ctdir = new CTMixedSignalDirector(ctsub,
                "CTEmbDir");

        //ctdir.addDebugListener(new StreamListener());
        // ---------------------------------
        // Create the actors.
        // ---------------------------------
        // CTActors
        CurrentTime time = new CurrentTime(ctsub, "CurTime");
        TrigFunction trigFunction = new TrigFunction(ctsub, "TrigFunction");
        ZeroOrderHold hold = new ZeroOrderHold(ctsub, "Hold");
        AddSubtract add1 = new AddSubtract(ctsub, "Add1");

        Integrator intgl1 = new Integrator(ctsub, "Integrator1");
        Integrator intgl2 = new Integrator(ctsub, "Integrator2");
        Scale scale0 = new Scale(ctsub, "Scale0");
        Scale scale1 = new Scale(ctsub, "Scale1");
        Scale scale2 = new Scale(ctsub, "Scale2");
        Scale scale3 = new Scale(ctsub, "Scale3");
        Scale scale4 = new Scale(ctsub, "Scale4");
        scale4.factor.setExpression("feedbackGain");

        TimedPlotter ctPlot = new TimedPlotter(ctsub, "CTPlot");
        ctPlot.plot = new Plot();
        ctPlot.plot.setGrid(true);
        ctPlot.plot.setXRange(0.0, stopT);
        ctPlot.plot.setYRange(-1.0, 1.0);
        ctPlot.plot.setSize(500, 180);
        ctPlot.plot.addLegend(0, "Position");
        ctPlot.plot.addLegend(1, "Input");
        ctPlot.plot.addLegend(2, "Control");

        CTPeriodicSampler ctSampler = new CTPeriodicSampler(ctsub,
                "PeriodicSampler");
        ctSampler.samplePeriod.setExpression("samplePeriod");

        // CT Connections
        ctsub.connect(time.output, scale3.input);
        ctsub.connect(scale3.output, trigFunction.input);

        Relation cr0 = ctsub.connect(trigFunction.output, scale0.input, "CR0");
        ctsub.connect(scale0.output, add1.plus, "CR1");
        ctsub.connect(add1.output, intgl1.input, "CR2");

        Relation cr3 = ctsub.connect(intgl1.output, intgl2.input, "CR3");
        Relation cr4 = ctsub.connect(intgl2.output, ctPlot.input, "CR4");
        scale1.input.link(cr3);
        scale2.input.link(cr4);
        ctSampler.input.link(cr4);

        TypedIORelation cr5 = new TypedIORelation(ctsub, "CR5");
        ctSampler.output.link(cr5);
        subout.link(cr5);
        ctsub.connect(scale1.output, add1.plus, "CR6");
        ctsub.connect(scale2.output, add1.plus, "CR7");
        ctsub.connect(scale4.output, add1.plus, "CR8");

        TypedIORelation cr9 = new TypedIORelation(ctsub, "CR9");
        hold.input.link(cr9);
        subin.link(cr9);

        Relation cr10 = ctsub.connect(hold.output, scale4.input, "CR10");
        ctPlot.input.link(cr0);
        ctPlot.input.link(cr10);

        // DE System
        ptolemy.domains.de.lib.TimedDelay delay = new ptolemy.domains.de.lib.TimedDelay(
                this, "delay");
        delay.delay.setToken(new DoubleToken(0.02));

        FIR fir = new FIR(this, "fir");
        fir.taps.setExpression("{0.7, 0.3}");

        Quantizer quan = new Quantizer(this, "Quantizer");
        Average accumulator = new Average(this, "accumulator");
        Sampler sampler = new Sampler(this, "sampler");
        Clock clk = new Clock(this, "ADClock");
        clk.offsets.setExpression("{0.0}");
        clk.period.setToken(new DoubleToken(1.0));
        clk.values.setExpression("{true}");

        TimedPlotter dePlot = new TimedPlotter(this, "DEPlot");
        Plot newPlot = new Plot();
        dePlot.plot = newPlot;
        newPlot.setGrid(true);
        newPlot.setXRange(0.0, stopT);
        newPlot.setYRange(-1.0, 1.0);
        newPlot.setSize(500, 180);
        newPlot.setConnected(false);
        newPlot.setImpulses(true);
        newPlot.setMarksStyle("dots");
        newPlot.addLegend(0, "Accum");
        newPlot.addLegend(1, "Quantize");

        FIR mav = new FIR(this, "MAV");
        mav.taps.setExpression("{0.1, 0.1, 0.1, 0.1, 0.1, 0.05, 0.05, 0.05, "
                + "0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05}");

        // DE connections.
        connect(subout, delay.input);
        connect(delay.output, fir.input);
        connect(fir.output, quan.input);

        Relation dr3 = connect(quan.output, subin);
        mav.input.link(dr3);
        connect(mav.output, accumulator.input);

        Relation dr4 = connect(clk.output, sampler.trigger);
        accumulator.reset.link(dr4);
        connect(accumulator.output, sampler.input);
        connect(sampler.output, dePlot.input);
        dePlot.input.link(dr3);

        // CT Director parameters
        ctdir.initStepSize.setToken(new DoubleToken(0.0001));

        ctdir.minStepSize.setToken(new DoubleToken(1e-6));

        //StringToken token1 = new StringToken(
        //        "ptolemy.domains.ct.kernel.solver.BackwardEulerSolver");
        //ctdir.BreakpointODESolver.setToken(token1);
        StringToken token2 = new StringToken(
                "ptolemy.domains.ct.kernel.solver.ExplicitRK23Solver");
        ctdir.ODESolver.setToken(token2);

        // CT Actor Parameters
        scale0.factor.setToken(new DoubleToken(50.0));
        scale1.factor.setToken(new DoubleToken(-2.50));
        scale2.factor.setToken(new DoubleToken(-250.0));
        scale3.factor.setToken(new DoubleToken(0.5));
    }

    ///////////////////////////////////////////////////////////////////
    ////                          parameters                       ////

    /** Sampling rate.
     */
    Parameter samplePeriod;

    /** Feedback gain.
     */
    Parameter feedbackGain;
}
