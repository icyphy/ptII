/* A CT director that utilizes multiple ODE solvers

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
@ProposedRating Yellow (liuj@eecs.berkeley.edu)
@AcceptedRating Yellow (johnr@eecs.berkeley.edu)

*/

package ptolemy.domains.ct.kernel;

import ptolemy.domains.ct.kernel.util.*;
import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.actor.sched.*;
import ptolemy.data.expr.*;
import ptolemy.data.*;
import java.util.Iterator;


//////////////////////////////////////////////////////////////////////////
//// CTMultiSolverDirector
/**
A director that uses multiple ODE solvers. The reason of switching
solvers is that when abrupt changes in the signal occurs (also called
breakpoints), the history information is useless for further calculation.
For ODE solvers that depends on history points, it is essential to switch
to use low order implicit method
with minimum step size to rebuild the history points. For input signals
that contains Dirac impulses, it is also essential to switch to the
impulse backward Euler solver to deal with them.
<P>
This class has one additional parameters than the CTSingleSolverDirector,
which is "breakpointODESolver". The value of the
parameter is a String that specifies the full class name of ODE solvers.
The default "ODESolver" is ExplicitRK23Solver. The default
"breakpointODESolver" is the DerivativeResolver, which assumes the
continuity of state.
All other parameters are maintained by the CTDirector base class. And the
two solvers share them.

@author  Jie Liu
@version $Id$
@see ptolemy.domains.ct.kernel.CTDirector
*/
public class CTMultiSolverDirector extends CTSingleSolverDirector {
    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *  All the parameters takes their default values.
     */
    public CTMultiSolverDirector() {
        super();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  All the parameters takes their default values.
     *  @param workspace The workspace of this object.
     */
    public CTMultiSolverDirector(Workspace workspace)  {
        super(workspace);
    }

    /** Construct a director in the given container with the given name.
     *  If the container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  All the parameters takes their default values.
     *  @param workspace Object for synchronization and version tracking
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *  with the specified container.  May be thrown in derived classes.
     */
    public CTMultiSolverDirector(CompositeActor container, String name)
            throws IllegalActionException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** parameter of breakpoint ODE solver.
     */
    public Parameter BreakpointODESolver;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute. If the changed attribute matches
     *  a parameter of the director, then the corresponding private copy of the
     *  parameter value will be updated.
     *  @param param The changed parameter.
     *  @exception IllegalActionException If the new solver that is specified
     *   is not valid.
     */
    public void attributeChanged(Attribute attr)
            throws IllegalActionException {
        Parameter param = (Parameter)attr;
        if (param == BreakpointODESolver) {
            if(_debugging) _debug(getName() + "breakpoint solver updating.");
            _bpsolverclassname =
                ((StringToken)param.getToken()).toString();
            _breakpointsolver =
                _instantiateODESolver(_bpsolverclassname);
        } else {
            super.attributeChanged(param);
        }
    }

    /** Return the breakpoint ODE solver.
     *  @return The breakpoint ODE solver.
     */
    public ODESolver getBreakpointSolver() {
        return _breakpointsolver;
    }

    /** In addition to initialize the system as those in
     *  CTSingleSolverDirector, this method set up the breakpoint
     *  solver.
     *  It throws an exception if the super class does.
     *
     *  @exception IllegalActionException If the super class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        if(_debugging) _debug(getFullName(), "instantiate breakpoint solver.");
        _breakpointsolver =
            _instantiateODESolver(_bpsolverclassname);
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

    /** Initialize parameters to their default values. */
    protected void _initParameters() {
        super._initParameters();
        try {
            String defaultsolverclass =
                "ptolemy.domains.ct.kernel.solver.ExplicitRK23Solver";
            ODESolver.setToken(
                    new StringToken(defaultsolverclass));
            _bpsolverclassname =
                "ptolemy.domains.ct.kernel.solver.DerivativeResolver";
            BreakpointODESolver = new Parameter(
                    this, "BreakpointODESolver",
                    new StringToken(_bpsolverclassname));
        } catch (IllegalActionException e) {
            //Should never happens. The parameters are always compatible.
            throw new InternalErrorException("Parameter creation error.");
        } catch (NameDuplicationException ex) {
            throw new InvalidStateException(this,
                    "Parameter name duplication.");
        }
    }

    /** clear obsolete breakpoints, switch to breakpointODESolver if this
     *  is the first fire after a breakpoint, and adjust step sizes
     *  accordingly.
     *  @exception IllegalActionException If breakpoint solver is not
     *     illegal.
     */
    protected void _processBreakpoints() throws IllegalActionException  {
        double bp;
        TotallyOrderedSet breakPoints = getBreakPoints();
        Double tnow = new Double(getCurrentTime());
        _setIsBPIteration(false);
        //choose ODE solver
        _setCurrentODESolver(getODESolver());
        // If now is a break point, remove the break point from table;
        if(breakPoints != null && !breakPoints.isEmpty()) {
            breakPoints.removeAllLessThan(tnow);
            if(breakPoints.contains(tnow)) {
                // now is the break point.
                breakPoints.removeFirst();
                _setIsBPIteration(true);
                _setCurrentODESolver(_breakpointsolver);
                setCurrentStepSize(0.0); //getMinStepSize());
                if(_debugging) _debug(getFullName(),
                        "IN BREAKPOINT iteration.");
            }else {
                // adjust step size according to the first break point.
                bp = ((Double)breakPoints.first()).doubleValue();
                double iterEndTime = getCurrentTime() + getCurrentStepSize();
                if (iterEndTime > bp) {
                    setCurrentStepSize(bp-getCurrentTime());
                }
            }
        }
    }

    /** Predict the next step size. This method should be called if the
     *  current integration step is acceptable. The predicted step size
     *  is the minimum of all predictions from step size control actors.
     *  @exception IllegalActionException If the scheduler throws it.
     */
    protected double _predictNextStepSize() throws IllegalActionException {
        if(!isBPIteration()) {
            double predictedstep = 10.0*getCurrentStepSize();
            CTScheduler sched = (CTScheduler)getScheduler();
            Iterator sscs = sched.stateTransitionSSCActorList().iterator();
            while (sscs.hasNext()) {
                CTStepSizeControlActor a =
                    (CTStepSizeControlActor) sscs.next();
                predictedstep = Math.min(predictedstep, a.predictedStepSize());
            }
            sscs = sched.outputSSCActorList().iterator();
            while (sscs.hasNext()) {
                CTStepSizeControlActor a =
                    (CTStepSizeControlActor) sscs.next();
                predictedstep = Math.min(predictedstep, a.predictedStepSize());
            }
            return predictedstep;
        } else {
            return getInitialStepSize();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The classname of the default ODE solver
    private String _bpsolverclassname;
    // The default solver.
    private ODESolver _breakpointsolver = null;

}
