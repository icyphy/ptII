/* A micro-accelerometer demo that uses CT and DE domains.

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

package ptolemy.domains.ct.demo.SigmaDelta;

import java.awt.BorderLayout;
import java.awt.event.*;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import ptolemy.domains.de.kernel.*;
import ptolemy.domains.de.lib.*;
import ptolemy.domains.ct.kernel.*;
import ptolemy.domains.ct.gui.CTApplet;
import ptolemy.domains.ct.lib.*;
import ptolemy.domains.sdf.lib.FIR;
import ptolemy.actor.*;
import ptolemy.gui.Query;
import ptolemy.actor.lib.*;
import ptolemy.actor.gui.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;

//////////////////////////////////////////////////////////////////////////
//// SigmaDeltaApplet
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
*/
public class SigmaDeltaApplet extends CTApplet {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Initialize the applet.
     */
    public void init() {
        super.init();

        getContentPane().setLayout(
                new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        JPanel controlpanel = new JPanel();
        controlpanel.setLayout(new BorderLayout());
        controlpanel.setBackground(getBackground());
        getContentPane().add(controlpanel);

        _query = new Query();
        //_query.addQueryListener(new ParameterListener());
        controlpanel.add(_query, BorderLayout.WEST);
        _query.addLine("stopT", "Stop Time", "15.0");
        _query.addLine("sample", "Sample Rate", "0.02");
        _query.addLine("feedback", "Feedback Gain", "-20.0");
        _query.setBackground(getBackground());

        controlpanel.add(_createRunControls(2), BorderLayout.EAST);

        // Creating the model.
        try {
            _toplevel.setName("DESystem");

            // Set up the top level composite actor, director and manager
            _deDirector = new DEDirector(_toplevel,"DEDirector");
            //_deDirector.addDebugListener(new StreamListener());

            // CT subsystem
            TypedCompositeActor ctsub = new TypedCompositeActor(_toplevel,
                    "CTSubsystem");
            TypedIOPort subin = new TypedIOPort(ctsub, "Pin");
            subin.setInput(true);

            TypedIOPort subout = new TypedIOPort(ctsub, "Pout");
            subout.setOutput(true);

            CTMixedSignalDirector ctdir =
                new CTMixedSignalDirector(ctsub, "CTEmbDir");
            //ctdir.addDebugListener(new StreamListener());

            // ---------------------------------
            // Create the actors.
            // ---------------------------------

            // CTActors

            CurrentTime time = new CurrentTime(ctsub, "CurTime");
            Sine sine = new Sine(ctsub, "Sine");
            ZeroOrderHold hold = new ZeroOrderHold(ctsub, "Hold");
            AddSubtract add1 = new AddSubtract(ctsub, "Add1");

            Integrator intgl1 = new Integrator(ctsub, "Integrator1");
            Integrator intgl2 = new Integrator(ctsub, "Integrator2");
            Scale gain0 = new Scale(ctsub, "Gain0");
            Scale gain1 = new Scale(ctsub, "Gain1");
            Scale gain2 = new Scale(ctsub, "Gain2");
            _gain3 = new Scale(ctsub, "Gain3");

            _ctPlot = new TimedPlotter(ctsub, "CTPlot");
            _ctPlot.place(getContentPane());
            _ctPlot.plot.setBackground(getBackground());
            _ctPlot.plot.setGrid(true);
            _ctPlot.plot.setYRange(-1.0, 1.0);
            _ctPlot.plot.setSize(500, 180);
            _ctPlot.plot.addLegend(0,"Position");
            _ctPlot.plot.addLegend(1,"Input");
            _ctPlot.plot.addLegend(2, "Control");

            _sampler =
                new CTPeriodicSampler(ctsub, "Sampler");

            // CT Connections
            ctsub.connect(time.output, sine.input);
            Relation cr0 = ctsub.connect(sine.output, gain0.input, "CR0");
            Relation cr1 = ctsub.connect(gain0.output, add1.plus, "CR1");
            Relation cr2 = ctsub.connect(add1.output, intgl1.input, "CR2");
            Relation cr3 = ctsub.connect(intgl1.output, intgl2.input, "CR3");
            Relation cr4 = ctsub.connect(intgl2.output, _ctPlot.input, "CR4");
            gain1.input.link(cr3);
            gain2.input.link(cr4);
            _sampler.input.link(cr4);
            TypedIORelation cr5 = new TypedIORelation(ctsub, "CR5");
            _sampler.output.link(cr5);
            subout.link(cr5);
            Relation cr6 = ctsub.connect(gain1.output, add1.plus, "CR6");
            Relation cr7 = ctsub.connect(gain2.output, add1.plus, "CR7");
            Relation cr8 = ctsub.connect(_gain3.output, add1.plus, "CR8");
            TypedIORelation cr9 = new TypedIORelation(ctsub, "CR9");
            hold.input.link(cr9);
            subin.link(cr9);
            Relation cr10 = ctsub.connect(hold.output, _gain3.input, "CR10");
            _ctPlot.input.link(cr0);
            _ctPlot.input.link(cr10);

            // DE System
            ptolemy.domains.de.lib.Delay delay =
                new ptolemy.domains.de.lib.Delay(_toplevel, "delay");
            delay.delay.setToken(new DoubleToken(0.02));
            FIR fir = new FIR(_toplevel, "fir");
            fir.taps.setExpression("[0.7, 0.3]");
            Quantizer quan = new Quantizer(_toplevel, "Quantizer");
            Average accumulator = new Average(_toplevel, "accumulator");
            Sampler sampler = new Sampler(_toplevel, "sampler");
            Clock clk = new Clock(_toplevel, "ADClock");
            double[][] offs = {{0.0}};
            clk.offsets.setToken(new DoubleMatrixToken(offs));
            clk.period.setToken(new DoubleToken(1.0));
            clk.values.setExpression("[true]");

            _dePlot = new TimedPlotter(_toplevel, "DEPlot");
            _dePlot.place(getContentPane());
            _dePlot.plot.setBackground(getBackground());
            _dePlot.plot.setGrid(true);
            _dePlot.plot.setYRange(-1.0, 1.0);
            _dePlot.plot.setSize(500, 180);
            _dePlot.plot.setConnected(false);
            _dePlot.plot.setImpulses(true);
            _dePlot.plot.setMarksStyle("dots");
            _dePlot.plot.addLegend(0, "Accum");
            _dePlot.plot.addLegend(1, "Quantize");

            FIR mav = new FIR(_toplevel, "MAV");
            mav.taps.setExpression(
                    "[0.1, 0.1, 0.1, 0.1, 0.1, 0.05, 0.05, 0.05, "
                    + "0.05, 0.05, 0.05, 0.05, 0.05, 0.05, 0.05]");

            // DE connections.
            Relation dr0 = _toplevel.connect(subout,  delay.input);
            Relation dr1 = _toplevel.connect(delay.output,  fir.input);
            Relation dr2 = _toplevel.connect(fir.output, quan.input);
            Relation dr3 = _toplevel.connect(quan.output, subin);
            mav.input.link(dr3);
            Relation dr5 = _toplevel.connect(mav.output, accumulator.input);
            Relation dr4 = _toplevel.connect(clk.output, sampler.trigger);
            accumulator.reset.link(dr4);
            Relation dr6 = _toplevel.connect(accumulator.output, sampler.input);
            Relation dr7 = _toplevel.connect(sampler.output, _dePlot.input);
            _dePlot.input.link(dr3);

            // CT Director parameters
            ctdir.InitStepSize.setToken(new DoubleToken(0.0001));

            ctdir.MinStepSize.setToken(new DoubleToken(1e-6));

            //StringToken token1 = new StringToken(
            //        "ptolemy.domains.ct.kernel.solver.BackwardEulerSolver");
            //ctdir.BreakpointODESolver.setToken(token1);

            StringToken token2 = new StringToken(
                    "ptolemy.domains.ct.kernel.solver.ExplicitRK23Solver");
            ctdir.ODESolver.setToken(token2);

            // CT Actor Parameters

            sine.omega.setToken(new DoubleToken(0.5));

            gain0.factor.setToken(new DoubleToken(50.0));
            gain1.factor.setToken(new DoubleToken(-2.50));
            gain2.factor.setToken(new DoubleToken(-250.0));

        } catch (Exception ex) {
            report("Setup failed: ",  ex);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Execute the system only when the current system is not running.
     *  This overrides the base class to read the
     *  values in the query box first.
     *  @exception IllegalActionException Not thrown.
     */
    protected void _go() throws IllegalActionException {
        try {
            if(_manager.getState() == _manager.IDLE) {
                //System.out.println("set up parameters");

                // FIXME: use parameter when the DEDirector is changed.
                double stopT = _query.doubleValue("stopT");
                _deDirector.setStopTime(stopT);
                //System.out.println("stop time set");
                _gain3.factor.setToken(new DoubleToken(
                        _query.doubleValue("feedback")));
                //System.out.println("feedback gain set");
                _sampler.SamplePeriod.setToken(new DoubleToken(
                        _query.doubleValue("sample")));
                //System.out.println("sampler set");

                // adjust plot sizes
                //System.out.println("set up plots");
                _ctPlot.plot.setXRange(0.0, stopT);
                _dePlot.plot.setXRange(0.0, stopT);
                super._go();
            }
        } catch (Exception ex) {
            report(ex);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // The query box.
    private Query _query;

    // FIXME: Under jdk 1.2, the following can (and should) be private
    private DEDirector _deDirector;

    private double _stopTime = 15.0;
    private Scale _gain3;
    private CTPeriodicSampler _sampler;
    private TimedPlotter _ctPlot;
    private TimedPlotter _dePlot;
}
