/* A second order system for profile the performance.

 Copyright (c) 1998 The Regents of the University of California.
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

*/

package ptolemy.domains.ct.demo;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.domains.ct.kernel.*;
import ptolemy.domains.ct.kernel.solver.*;
import ptolemy.domains.ct.lib.*;


//////////////////////////////////////////////////////////////////////////
//// WaveBERK23
/**
A second order system simulation. For performance testing.
@author  Jie Liu
@version $Id$
@see classname
@see full-classname
*/
public class WaveBERK23 {
     public static void main(String[] args) {
         try {
             TypedCompositeActor sys = new TypedCompositeActor();
             sys.setName( "system");
             Manager man = new Manager();
             sys.setManager(man);

             CTMultiSolverDirector dir = new CTMultiSolverDirector("DIR");
             sys.setDirector(dir);
             CTSquareWave sqwv = new CTSquareWave(sys, "SQWV");
             CTAdd add1 = new CTAdd( sys, "Add1");
             CTIntegrator intgl1 = new CTIntegrator(sys, "Integrator1");
             CTIntegrator intgl2 = new CTIntegrator(sys, "Integrator2");
             CTGain gain1 = new CTGain( sys, "Gain1");
             CTGain gain2 = new CTGain( sys, "Gain2");
             CTGain gain3 = new CTGain( sys, "Gain3");
             CTSink plot = new CTSink( sys, "Sink");

             IORelation r1 = (IORelation)
                 sys.connect(sqwv.output, gain1.input, "R1");
             IORelation r2 = (IORelation)
                 sys.connect(gain1.output, add1.input, "R2");
             IORelation r3 = (IORelation)
                 sys.connect(add1.output, intgl1.input, "R3");
             IORelation r4 = (IORelation)
                 sys.connect(intgl1.output, intgl2.input, "R4");
             IORelation r5 = (IORelation)
                 sys.connect(intgl2.output, plot.input, "R5");
             gain2.input.link(r4);
             gain3.input.link(r5);
             IORelation r6 = (IORelation)
                 sys.connect(gain2.output, add1.input, "R6");
             IORelation r7 = (IORelation)
                 sys.connect(gain3.output, add1.input, "R7");
             plot.input.link(r1);

             Parameter starttime = (Parameter)dir.getAttribute("StartTime");
             starttime.setExpression("0.0");
             starttime.parameterChanged(null);

             Parameter initstep = (Parameter)dir.getAttribute(
                 "InitialStepSize");
             initstep.setExpression("0.000001");
             initstep.parameterChanged(null);

             Parameter minstep = (Parameter)dir.getAttribute( "MinimumStepSize");
             minstep.setExpression("1e-6");
             minstep.parameterChanged(null);

             Parameter stoptime = (Parameter)dir.getAttribute("StopTime");
             stoptime.setExpression("10.0");
             stoptime.parameterChanged(null);

             Parameter bpsolver = (Parameter)dir.getAttribute(
                 "BreakpointODESolver");
             StringToken token = new StringToken(
                 "ptolemy.domains.ct.kernel.solver.BackwardEulerSolver");
             bpsolver.setToken(token);
             bpsolver.parameterChanged(null);

             Parameter dsolver = (Parameter)dir.getAttribute(
                 "DefaultODESolver");
             StringToken token1 = new StringToken(
                 "ptolemy.domains.ct.kernel.solver.ExplicitRK23Solver");
             dsolver.setToken(token1);
             dsolver.parameterChanged(null);

             Parameter freq = (Parameter)sqwv.getAttribute("Frequency");
             freq.setExpression("0.25");
             freq.parameterChanged(null);

             Parameter g1 = (Parameter)gain1.getAttribute("Gain");
             g1.setExpression("500.0");
             g1.parameterChanged(null);

             Parameter g2 = (Parameter)gain2.getAttribute("Gain");
             g2.setExpression("-25.0");
             g2.parameterChanged(null);

             Parameter g3 = (Parameter)gain3.getAttribute("Gain");
             g3.setExpression("-2500.0");
             g3.parameterChanged(null);

             man.run();
         } catch (NameDuplicationException ex) {
             throw new InternalErrorException("NameDuplication");
         } catch (IllegalActionException ex) {
             throw new InternalErrorException("IllegalAction:"+
             ex.getMessage());
         }
     }
 }
