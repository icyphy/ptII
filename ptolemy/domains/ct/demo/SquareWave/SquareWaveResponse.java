/* The square wave response of a second order CT system.

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

package ptolemy.domains.ct.demo.SquareWave;

import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.gui.Query;
import ptolemy.actor.gui.*;
import ptolemy.actor.lib.*;
import ptolemy.actor.lib.gui.TimedPlotter;
import ptolemy.actor.lib.gui.XYPlotter;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.domains.ct.kernel.*;
import ptolemy.domains.ct.gui.CTApplet;
import ptolemy.domains.ct.kernel.solver.*;
import ptolemy.domains.ct.lib.*;

import java.awt.BorderLayout;
import javax.swing.JPanel;

//////////////////////////////////////////////////////////////////////////
//// SquareWaveResponse
/**
The square wave response of a second order CT system. This simple
CT system demonstrate the use of ODE solvers and domain polymorphic
actors in the CT domain.
The solvers are not allowed to change during the execution.
It is also useful for correctness and performance testing.
@author  Jie Liu
@version $Id$
*/
public class SquareWaveResponse extends CTApplet {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Initialize the applet.
     */
    public void init() {
        super.init();
        JPanel controlpanel = new JPanel();
        controlpanel.setLayout(new BorderLayout());
        controlpanel.setBackground(getBackground());
        getContentPane().add(controlpanel, BorderLayout.NORTH);

        _query = new Query();
        _query.setBackground(getBackground());
        //_query.addQueryListener(new ParameterListener());
        controlpanel.add(_query, BorderLayout.WEST);
        _query.addLine("stopT", "Stop Time", "6.0");
        _query.addLine("solver", "DefaultSolver",
                "ptolemy.domains.ct.kernel.solver.ExplicitRK23Solver");
        _query.addLine("bpsolver", "BreakpointSolver",
                "ptolemy.domains.ct.kernel.solver.DerivativeResolver");

        controlpanel.add(_createRunControls(2), BorderLayout.EAST);

        try {
            _toplevel.setName( "system");

            _dir = new CTMultiSolverDirector(
                    _toplevel, "DIR");
            _dir.STAT = true;
            //dir.addDebugListener(new StreamListener());
            Clock sqwv = new Clock(_toplevel, "SQWV");
            AddSubtract add1 = new AddSubtract( _toplevel, "Add1");
            Integrator intgl1 = new Integrator(_toplevel, "Integrator1");
            Integrator intgl2 = new Integrator(_toplevel, "Integrator2");
            Scale gain1 = new Scale( _toplevel, "Gain1");
            Scale gain2 = new Scale( _toplevel, "Gain2");
            Scale gain3 = new Scale( _toplevel, "Gain3");
            TimedPlotter responsePlot = new TimedPlotter( _toplevel, "Sink");
            responsePlot.place(getContentPane());
            responsePlot.plot.setBackground(getBackground());
            responsePlot.plot.setGrid(true);
            responsePlot.plot.setXRange(0.0, 6.0);
            responsePlot.plot.setYRange(-2.0, 2.0);
            responsePlot.plot.setSize(600, 400);
            responsePlot.plot.addLegend(0,"response");

            IORelation r1 = (IORelation)
                _toplevel.connect(sqwv.output, gain1.input, "R1");
            IORelation r2 = (IORelation)
                _toplevel.connect(gain1.output, add1.plus, "R2");
            IORelation r3 = (IORelation)
                _toplevel.connect(add1.output, intgl1.input, "R3");
            IORelation r4 = (IORelation)
                _toplevel.connect(intgl1.output, intgl2.input, "R4");
            IORelation r5 = (IORelation)
                _toplevel.connect(intgl2.output, responsePlot.input, "R5");
            gain2.input.link(r4);
            gain3.input.link(r5);
            IORelation r6 = (IORelation)
                _toplevel.connect(gain2.output, add1.plus, "R6");
            IORelation r7 = (IORelation)
                _toplevel.connect(gain3.output, add1.plus, "R7");
            responsePlot.input.link(r1);

            _dir.startTime.setToken(new DoubleToken(0.0));

            _dir.initStepSize.setToken(new DoubleToken(0.000001));

            _dir.minStepSize.setToken(new DoubleToken(1e-6));

            sqwv.period.setToken(new DoubleToken(4));
            double offsets[][] = {{0.0, 2.0}};
            sqwv.offsets.setToken(new DoubleMatrixToken(offsets));
            double values[][] = {{2.0, -2.0}};
            sqwv.values.setToken(new DoubleMatrixToken(values));


            gain1.factor.setToken(new DoubleToken(500.0));

            gain2.factor.setToken(new DoubleToken(-10.0));

            gain3.factor.setToken(new DoubleToken(-1000.0));

        } catch (NameDuplicationException ex) {
            throw new InternalErrorException("NameDuplication");
        } catch (IllegalActionException ex) {
            throw new InternalErrorException("IllegalAction:"+
                    ex.getMessage());
        }
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
            _dir.breakpointODESolver.setToken(new StringToken(
                    _query.stringValue("bpsolver")));
            _dir.ODESolver.setToken(new StringToken(
                    _query.stringValue("solver")));
            super._go();
        } catch (Exception ex) {
            report(ex);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private CTMultiSolverDirector _dir;
    private Query _query;
}
