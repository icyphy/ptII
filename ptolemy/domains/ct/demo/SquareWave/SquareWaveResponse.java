/* A second order system for profile the performance.

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
@AcceptedRating Red (cx@eecs.berkeley.edu)
*/

package ptolemy.domains.ct.demo.SquareWave;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.actor.gui.*;
import ptolemy.actor.lib.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.domains.ct.kernel.*;
import ptolemy.domains.ct.kernel.solver.*;
import ptolemy.domains.ct.lib.*;


//////////////////////////////////////////////////////////////////////////
//// SquareWaveResponse
/**
A second order system simulation. For performance testing.
@author  Jie Liu
@version $Id$
*/
public class SquareWaveResponse {
    public static void main(String[] args) {

        String _breaksolver = new String();
        String _solver = new String();

        if(args.length == 0) {
            _breaksolver = new String(
                    "ptolemy.domains.ct.kernel.solver.BackwardEulerSolver");
            _solver = new String(
                    "ptolemy.domains.ct.kernel.solver.ExplicitRK23Solver");
        }else if(args.length == 1) {
            _solver = new String(
                    "ptolemy.domains.ct.kernel.solver.ExplicitRK23Solver");
            _breaksolver = "ptolemy.domains.ct.kernel.solver."+args[0];
        }else if(args.length == 2) {
            _solver = "ptolemy.domains.ct.kernel.solver." + args[1];
            _breaksolver = "ptolemy.domains.ct.kernel.solver."+args[0];
        } else {
            System.out.println(
                    "Usage: WaveBERK23 [breakpointODESolver] [ODESolver]");
            return;
        }
             
        try {
            TypedCompositeActor sys = new TypedCompositeActor();
            sys.setName( "system");
            Manager man = new Manager();
            sys.setManager(man);

            CTMultiSolverDirector dir = new CTMultiSolverDirector(
                    sys, "DIR");
            //dir.addDebugListener(new StreamListener());
            CTSquareWave sqwv = new CTSquareWave(sys, "SQWV");
            AddSubtract add1 = new AddSubtract( sys, "Add1");
            CTIntegrator intgl1 = new CTIntegrator(sys, "Integrator1");
            CTIntegrator intgl2 = new CTIntegrator(sys, "Integrator2");
            Scale gain1 = new Scale( sys, "Gain1");
            Scale gain2 = new Scale( sys, "Gain2");
            Scale gain3 = new Scale( sys, "Gain3");
            TimedPlotter plot = new TimedPlotter( sys, "Sink");

            IORelation r1 = (IORelation)
                sys.connect(sqwv.output, gain1.input, "R1");
            IORelation r2 = (IORelation)
                sys.connect(gain1.output, add1.plus, "R2");
            IORelation r3 = (IORelation)
                sys.connect(add1.output, intgl1.input, "R3");
            IORelation r4 = (IORelation)
                sys.connect(intgl1.output, intgl2.input, "R4");
            IORelation r5 = (IORelation)
                sys.connect(intgl2.output, plot.input, "R5");
            gain2.input.link(r4);
            gain3.input.link(r5);
            IORelation r6 = (IORelation)
                sys.connect(gain2.output, add1.plus, "R6");
            IORelation r7 = (IORelation)
                sys.connect(gain3.output, add1.plus, "R7");
            plot.input.link(r1);

            dir.StartTime.setToken(new DoubleToken(0.0));

            dir.InitStepSize.setToken(new DoubleToken(0.000001));

            dir.MinStepSize.setToken(new DoubleToken(1e-6));

            dir.StopTime.setToken(new DoubleToken(4.0));

            dir.BreakpointODESolver.setToken(new StringToken(_breaksolver ));

            dir.ODESolver.setToken(new StringToken(_solver));

            Parameter freq = (Parameter)sqwv.getAttribute("Frequency");
            sqwv.Frequency.setToken(new DoubleToken(0.25));

            gain1.gain.setToken(new DoubleToken(500.0));

            gain2.gain.setToken(new DoubleToken(-25.0));

            gain3.gain.setToken(new DoubleToken(-2500.0));

            man.startRun();
        } catch (NameDuplicationException ex) {
            throw new InternalErrorException("NameDuplication");
        } catch (IllegalActionException ex) {
            throw new InternalErrorException("IllegalAction:"+
                    ex.getMessage());
        }
    }
}
