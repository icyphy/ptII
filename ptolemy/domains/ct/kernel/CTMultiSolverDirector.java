/* A CT director that utilizes multiple ODE solvers

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

package ptolemy.domains.ct.kernel;

import ptolemy.domains.ct.kernel.util.*;
import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.actor.sched.*;
import ptolemy.data.expr.*;
import ptolemy.data.*;
import java.util.*;
import collections.LinkedList;

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
"breakpointODESolver" is the BackwardEulerSolver.
All other parameters are maintained by the CTDirector base class. And the
two solvers share them.

@author  Jie Liu
@version $Id$
@see ptolemy.domains.ct.kernel.CTDirector
*/
public class CTMultiSolverDirector extends CTSingleSolverDirector {
    /** Construct a CTMultiSolverDirector with no name and no container.
     *  All parameters take their default values.
     */
    public CTMultiSolverDirector () {
        super();
        _initParameters();
    }

    /** Construct a CTMultiSolverDirector in the default workspace with
     *  the given name.
     *  If the name argument is null, then the name is set to the empty
     *  string. The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  All parameters take their default values.
     *
     *  @param name The name of this director.
     */
    public CTMultiSolverDirector (String name) {
        super(name);
        _initParameters();
    }

    /** Construct a director in the given workspace with the given name.
     *  If the workspace argument is null, use the default workspace.
     *  The director is added to the list of objects in the workspace.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  All parameters take their default values.
     *
     *  @param workspace Object for synchronization and version tracking
     *  @param name Name of this director.
     */
    public CTMultiSolverDirector (Workspace workspace, String name) {
        super(workspace, name);
        _initParameters();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the default ODE solver.
     *  @return The default ODE solver
     */
    public ODESolver getDefaultSolver() {
        return _defaultSolver;
    }

    /** Return the breakpoint ODE solver.
     *  @return The breakpoint ODE solver.
     */
    public ODESolver getBreakpointSolver() {
        return _breakpointSolver;
    }

    /** This does the initialization for the entire subsystem. This
     *  is called exactly once at the start of the entire execution.
     *  It checks if it container and its scheduler is correct. 
     *  Otherwise throw an exception. It then calls _intialize()
     *  method to initialize parameters and times.
     *
     *  @exception IllegalActionException If there's no scheduler or
     *       thrown by a contained actor.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        fireAt(null, getCurrentTime());
    }

    /** Update given parameter. If the parameter does not exist, 
     *  throws an exception.
     *  @param param The parameter.
     *  @exception IllegalActionException If the parameter does not exist.
     */
    public void updateParameter(Parameter param)
            throws IllegalActionException {
        if(param == _paramDefaultODESolver) {
            if(VERBOSE) {
                System.out.println("default solver updating.");
            }
            _defaultsolverclass =
            ((StringToken)param.getToken()).stringValue();
            _defaultSolver = _instantiateODESolver(_defaultsolverclass);
        } else if (param == _paramBreakpointODESolver) {
            if(VERBOSE) {
                System.out.println("breakpoint solver updating.");
            }
            _breakpointsolverclass =
            ((StringToken)param.getToken()).stringValue();
            _breakpointSolver =
            _instantiateODESolver(_breakpointsolverclass);
        } else {
            super.updateParameter(param);
        }
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

    /** clear obsolete breakpoints, switch to breakpointODESolver if this
     *  is the first fire after a breakpoint, and adjust step sizes
     *  accordingly.
     *  @exception IllgalActionException If breakpoint solver is not 
     *     illegal.
     */
    protected void _processBreakpoints() throws IllegalActionException  {
        double bp;
        TotallyOrderedSet breakPoints = getBreakPoints();
        double tnow = getCurrentTime();
        _setIsBPIteration(false);
        //choose ODE solver
        setCurrentODESolver(_defaultSolver);
        // If now is a break point, remove the break point from table;
        if(breakPoints != null) {
            while (!breakPoints.isEmpty()) {
                bp = ((Double)breakPoints.first()).doubleValue();
                if(bp < (tnow-getTimeResolution())) {
                    // break point in the past or at now.
                    breakPoints.removeFirst();
                } else if(Math.abs(bp-tnow) < getTimeResolution()){
                    // break point now!
                    breakPoints.removeFirst();
                    setCurrentODESolver(_breakpointSolver);
                    setCurrentStepSize(getMinStepSize());
                    if(DEBUG) {
                        System.out.println("IN BREAKPOINT iteration.");
                    }
                    _setIsBPIteration(true);
                    break;
                } else {
                    double iterEndTime = tnow+getCurrentStepSize();
                    if (iterEndTime > bp) {
                        setCurrentStepSize(bp-getCurrentTime());
                    }
                    break;
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
            Enumeration sscs = sched.stateTransitionSSCActors();
            while (sscs.hasMoreElements()) {
                CTStepSizeControlActor a = 
                    (CTStepSizeControlActor) sscs.nextElement();
                predictedstep = Math.min(predictedstep, a.predictedStepSize());
            }
            sscs = sched.outputSSCActors();
            while (sscs.hasMoreElements()) {
                CTStepSizeControlActor a = 
                    (CTStepSizeControlActor) sscs.nextElement();
                predictedstep = Math.min(predictedstep, a.predictedStepSize());
            }
            return predictedstep;
        } else {
            return getInitialStepSize();
        }
    }
    
    ////////////////////////////////////////////////////////////////////////
    ////                         private methods                      ////
    private void _initParameters() {
        try {
            _defaultsolverclass=
                "ptolemy.domains.ct.kernel.solver.ExplicitRK23Solver";
            _paramDefaultODESolver = (CTParameter)getAttribute("ODESolver");
            _paramDefaultODESolver.setToken(
                    new StringToken(_defaultsolverclass));
            _breakpointsolverclass=
                "ptolemy.domains.ct.kernel.solver.BackwardEulerSolver";
            _paramBreakpointODESolver = new CTParameter(
                this, "BreakpointODESolver",
                new StringToken(_breakpointsolverclass));
        } catch (IllegalActionException e) {
            //Should never happens. The parameters are always compatible.
            throw new InternalErrorException("Parameter creation error.");
        } catch (NameDuplicationException ex) {
            throw new InvalidStateException(this,
                    "Parameter name duplication.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // parameter of default ODE solver
    private CTParameter _paramDefaultODESolver;
    // The classname of the default ODE solver
    private String _defaultsolverclass;
    // The default solver.
    private ODESolver _defaultSolver = null;

    // parameter of breakpoint ODE solver
    private CTParameter _paramBreakpointODESolver;
    // The classname of the default ODE solver
    private String _breakpointsolverclass;
    // The default solver.
    private ODESolver _breakpointSolver = null;
    //indicate the first round of execution.
    private boolean _first; 
}
