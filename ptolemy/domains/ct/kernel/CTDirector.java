/* An abstract base class for directors of Continuous time simulation.

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

@ProposedRating yellow (liuj@eecs.berkeley.edu)
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
//// CTDirector
/**
This is the base class for directors of continuous time simulation.
<P>
The continuous time (CT) domain is a timed domain. There is a global
notion of time that all the actors are shared. Time is maintained
by the directors. The method getCurrentTime() returns the current
global time. Time can be set by setCurrentTime() method, but this
method should not the called by the actors. Time can only be advanced
by directors or its ODE solvers.
<P>
CTDirectors has a CTScheduler which provides the schedule for firing
the actors in different phase of the execution.
<P> 
CTDirectors may have one or more ODE solvers. In each iteration, one
of the ODE solvers is taking charge of solving the ODEs. This solver
is called the <I>current ODE solver</I>.
<P>  
This base class maintains a list of parameters that may be used by 
ODE solvers. These parameters are: <Br>
<LI> start time (<code>StartTime</code>): The start time of the 
simulation. The parameter should only be affective if the director
is at the top level. Default value is 0.0.</LI><BR>
<LI> stop time (<code>StopTime</code>): The stop time of the simulation.
 The parameter should only be affective if the director
is at the top level. Default value is 1.0.</LI><BR>
<LI> initial step size (<code>InitialStepSize</code>): The suggested
step size from the user. This will be the step size for fixed step
size ODE sovlers. However, it is just a guide for variable step size
ODE solvers. Default value is 0.1</LI><Br>
<LI> minimum step size (<code>MinimumStepSize</code>): The minimum step
size the user wants to use in the simulation. Default value is 1e-5.
</LI><Br>
<LI> maximum iteration per step (<code>MaximumIterationPerStep</code>):
Used only in implicit ODE solvers. This is the maximum number of
iterations for finding the fixed point at one time point. 
Default value is 20. </LI><Br>
<LI> local trancation error tolerance (<code>LocalTruncationError
Tolerance</code>): This used for controlling the local truncation error 
in vairable step size ODE sovlers. If the local truncation error
at some error control actors are greater than this tolerance, then the 
integration step is considered failed, and should be restarted with 
a reduced step size. Default value 1e-4. </LI><Br>
<LI> value resolution for convergence (<code>ConvergeValueResolution</code>)
: This is used to control the convergence of fixed point iteration.
If in two successive iterations the differences of the state variables
is less than this resolution, then the fixed point is considered found.
Default value is 1e-6.<LI><Br>
<LI> time resolution (<code>TimeResolution</code>): The minimum resolution
of time, such that if two time values differ less than this value,
they are considered equivalent. Default value is 1e-10. </LI><Br>
<P>
This director maintains a breakpoint table to record all the break points.
The breakpoints are sorted in their chronological order in the table.
Breakpoints at the "same" time (controlled by time resolution) are
considered to be one. A breakpoint can be inserted into the table by
calling the fireAt() method. How to deal with these breakpoint are
director dependent.
@author Jie Liu
@version $Id$
@see ptolemy.actor.Director
*/
public abstract class CTDirector extends StaticSchedulingDirector
        implements ParameterListener{

    public static final boolean VERBOSE = false;
    public static final boolean DEBUG = false;
    public static final boolean STAT = true;
    public static int NSTEP = 0;
    public static int NFUNC = 0;
    public static int NFAIL = 0;
    public static int NROLL = 0;


    /** Construct a CTDirector with no name and no Container.
     *  All parameters take their default values. The scheduler
     *  is created.
     */
    public CTDirector () {
        super();
        _initParameters();
        try {
            setScheduler(new CTScheduler());
        }catch(IllegalActionException e) {
            // Should never occur.
            throw new InternalErrorException(this.getFullName() +
                "setting scheduler error");
        }
    }

    /** Construct a CTDirector in the default workspace with the given name.
     *  If the name argument is null, then the name is set to the empty
     *  string. The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  All parameters take their default values. The scheduler
     *  is created.
     *
     *  @param name The name of this director.
     */
    public CTDirector (String name) {
        super(name);
        _initParameters();
        try {
            setScheduler(new CTScheduler());
        }catch(IllegalActionException e) {
            // Should never occur.
            throw new InternalErrorException(this.getFullName() +
                "setting scheduler error");
        }
    }

    /** Construct a director in the given workspace with the given name.
     *  If the workspace argument is null, use the default workspace.
     *  The director is added to the list of objects in the workspace.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  All parameters take their default values. The scheduler
     *  is created.
     *
     *  @param workspace Object for synchronization and version tracking
     *  @param name Name of this director.
     */
    public CTDirector (Workspace workspace, String name) {
        super(workspace, name);
        _initParameters();
        try {
            setScheduler(new CTScheduler());
        }catch(IllegalActionException e) {
            // Should never occur.
            throw new InternalErrorException(this.getFullName() +
                "setting scheduler error");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the break point table.
     *  @return The break point table.
     */
    public TotallyOrderedSet getBreakPoints() {
        return _breakPoints;
    }

    /** Return the current ODESolver.
     *  @return The current ODESolver
     */
    public ODESolver getCurrentODESolver() {
        return _currentSolver;
    }

    /** Return the current step size. In a fixed step size method this is
     *  is the value set by setParam("initialStepSize"). For a variable step
     *  size method, the step size is controlled by the algorithm.
     *  @return the current step size.
     */
    public final double getCurrentStepSize() {
        return _currentStepSize;
    }

    /** Return the currentTime.
     *  @return the currentTime.
     */
    public final double getCurrentTime() {
        return _currentTime;
    }

    /** Return the initial step size.
     *  @return the initial step size.
     */
    public final double getInitialStepSize() {
        return _initStepSize;
    }

    /** Return the local trancation error tolerance, used by
     *  variable step size solvers.
     *  @return The local trancation error tolerance.
     *  FIXME: change to getErrorTolerance
     */
    public final double getLTETolerance() {
        return _lteTolerance;
    }

    /** Return the maximum number of iterations in fixed point
     *  calculation. If the iteration has exceed this number
     *  and the fixed point is still not found, then the algorithm
     *  is considered failed.
     */
    public final int getMaxIterations() {
        return _maxIterations;
    }

    /** Return the minimum step size used in variable step size
     *  ODE solvers.
     *  @return The minimum step size.
     */
    public final double getMinStepSize() {
        return _minStepSize;
    } 

    /** Return the start time.
     *  @return the start time.
     */
    public final double getStartTime() {
        return _startTime;
    }

    /** Return the stop time.
     *  @return the stop time.
     */
    public final double getStopTime() {
        return _stopTime;
    }

    /** Return the suggested next step size.
     *  @return The suggested next step size.
     */
    public final double getSuggestedNextStepSize() {
        return _suggestedNextStepSize;
    }

    /** Return the time resolution such that two time stamp within this
     *  resolution is considered identical.
     *  @return The time resolution.
     */
    public final double getTimeResolution() {
        return _timeResolution;
    }

    /** Return the value resolution, used for testing if sn implicit method
     *  has reached the fixed point. Two values differ less than
     *  this accuracy is considered identical in the fixed point
     *  calculation.
     *
     *  @return The value resolution for finding fixed point.
     */
    public final double getValueResolution() {
        return _valueResolution;
    }

    /** Register a break point at a future time. This requests the
     *  Director to fire exactly at each registered time.
     *  Override the fireAt() method in Director.
     *  @param actor The actor that requested the fire
     *  @param time The fire time
     *  @exception IllegalActionException If the time if before
     *  the current time
     */
    public void fireAt(Actor actor, double time) 
            throws IllegalActionException{
        if(_breakPoints == null) {
            _breakPoints = new TotallyOrderedSet(new DoubleComparator());
        }
        // check if the time is before the current time;
        if(time < getCurrentTime()) {
            throw new IllegalActionException((Nameable)actor, 
                    "Requested an Fire time that is earlier than" +
                    " the current time.");
        }
        _breakPoints.insert(new Double(time));
    }

    /** Return a new CTReceiver.
     *  @return A new CTReceiver.
     */
    public Receiver newReceiver() {
        return new CTReceiver();
    }

    /** If parameter changed, queue the event.
     *  FIXME: Merge to mutation handling.
     */
    public void parameterChanged(ParameterEvent e) {
        if(VERBOSE) {
            System.out.println("Parameter Changed.");
        }
        if(_parameterEvents == null) {
            _parameterEvents = new LinkedList();
        }
        _parameterEvents.insertLast(e);
    }

    /** Throw a InvalidStateException if any of the parameters are deleted.
     *  FIXME: Merge to mutation handling.
     */
    public void parameterRemoved(ParameterEvent e) {
        throw new InvalidStateException(this,
            "Critical Parameter deleted");
    }

    /** Update changed parameters. The queued parameter change events
     *  will be processed in their happening order.
     *  @exception IllegalActionException If throw by creation of some
     *       parameters.
     *  FIXME: Merge to mutation handling
     */
    public void updateParameters() throws IllegalActionException {
        LinkedList pEvents = _getParameterEvents();
        if((pEvents != null )&& (!pEvents.isEmpty())) {
            if(DEBUG) {
                System.out.println(" # of events = "+pEvents.size());
            }
            Enumeration pes = pEvents.elements();
            while(pes.hasMoreElements()) {
                ParameterEvent event = (ParameterEvent) pes.nextElement();
                 Parameter param = event.getParameter();
                 updateParameter(param);
             }
             pEvents.clear();
         }
     }

    /** Update a changed paramter. If the changed parameter name matches
     *  the name of a parameter of the director, then the coresponding
     *  parameter value will be updated. Otherwise, throw an exception.
     *  @param param The changed parameter.
     *  @exception IllegalActionException If the parameter name is not
     *     found.
     *  FIXME: MERGE TO mutation handling
     */
    public void updateParameter(Parameter param)
            throws IllegalActionException {
        if(param == _paramStopTime) {
            if(VERBOSE) {
                System.out.println("StopTime updating.");
            }
            setStopTime(((DoubleToken)param.getToken()).doubleValue());
        } else if(param == _paramInitStepSize) {
            if(VERBOSE) {
                System.out.println("initStepSize updating.");
            }
            _initStepSize =
            ((DoubleToken)param.getToken()).doubleValue();
        } else if(param == _paramStartTime) {
            if(VERBOSE) {
                System.out.println("starttime updating.");
            }
            _startTime = ((DoubleToken)param.getToken()).doubleValue();
        } else if(param == _paramLTETolerance) {
            if(VERBOSE) {
                System.out.println("LTE tolerant updating.");
            }
            _lteTolerance = ((DoubleToken)param.getToken()).doubleValue();
        } else if(param == _paramMinStepSize) {
            if(VERBOSE) {
                System.out.println("minstep updating.");
            }
            _minStepSize =
            ((DoubleToken)param.getToken()).doubleValue();
        }  else if(param == _paramValueResolution) {
            _valueResolution =
            ((DoubleToken)param.getToken()).doubleValue();
        } else if(param == _paramTimeResolution) {
            _timeResolution =
            ((DoubleToken)param.getToken()).doubleValue();
        } else if(param == _paramMaxIterations) {
            _maxIterations =
            ((IntToken)param.getToken()).intValue();
        } else {
            if (VERBOSE) {
                System.out.println("Unknowparameter: "+param.getName());
            }
            throw new IllegalActionException(this, param,
                " Unknown parameter.");
        }
    }

    /** set the given solver to be the current ODE Solver. If the solver
     *  can not be served as the current ODE solver then an
     *  exception should be thrown.
     *  @param solver The solver to be set.
     *  @exception  IllegalActionException Never thrown in this base class.
     *     It may be thrown by the direved classes if the solver is not
     *     appropriate.
     */
    public void setCurrentODESolver(ODESolver solver)
            throws IllegalActionException {
        _currentSolver = solver;
    }

    /** Set the current step size. This variable is very import during
     *  the simulation and should NOT be changed in the middle of an
     *  iteration.
     *  @param curstepsize The step size used for currentStepSize().
     */
    public void setCurrentStepSize(double curstepsize){
        _currentStepSize = curstepsize;
    }

    /** Set the current simulation time. All the actors directed by this
     *  director will share this global time. This is a very important
     *  value for a correct simulation. The method should be cafully used.
     *  @param tnow The current time.
     */
    public void setCurrentTime(double tnow){
        _currentTime = tnow;
    }

    /** Set the start time for the simulation. The start time is not 
     *  registered as a breakpoint in this method. The extended director
     *  should do it themselves if needed.
     *  @param tstart The start time. 
     */
    public void setStartTime(double tstart) {
        _stopTime = tstart;
    }

    /** Set the stop time for the simulation.  The stopt time is not 
     *  registered as a breakpoint in this method. The extended director
     *  should do it themselves if needed.
     *  @param tstop The stop time for the simulation.
     */
    public void setStopTime(double tstop) {
        _stopTime = tstop;
    }

 
    /** Set the suggested next step size.
     *  @param nextstep The suggested next step size.
     */
    public void setSuggestedNextStepSize(double nextstep) {
        _suggestedNextStepSize = nextstep;
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

    /** Return the parameter event queue.
     *  @return The parameter event queue.
     */
    protected LinkedList _getParameterEvents() {
        return _parameterEvents;
    }


    /** Instantiate ODESolver from its classname. Given the solver's full 
     *  class name, this method will try to inistantiate it by looking
     *  for the java class.
     *  @param solverclass The solver's full class name.
     *  @exception IllegalActionException If the solver is unable to be 
     *       created.
     */
    protected ODESolver _instantiateODESolver(String solverclass)
            throws IllegalActionException {
        ODESolver newsolver;
        if(VERBOSE) {
            System.out.println("instantiating solver..."+solverclass);
        }
        try {
            Class solver = Class.forName(solverclass);
            newsolver = (ODESolver)solver.newInstance();
        } catch(ClassNotFoundException e) {
            if(DEBUG) {
                System.out.println("solver class not found" + e.getMessage());
            }
            throw new IllegalActionException( this, "ODESolver: "+
                solverclass + " not found.");
        } catch(InstantiationException e) {
            if(DEBUG) {
                System.out.println("solver instantiate error" + e.getMessage());
            }
            throw new IllegalActionException( this, "ODESolver: "+
                solverclass + " instantiation failed.");
        } catch(IllegalAccessException e) {
            if(DEBUG) {
                System.out.println("solver not accessible" + e.getMessage());
            }
            throw new IllegalActionException( this, "ODESolver: "+
                solverclass + " not accessible.");
        }
        newsolver._makeSolverOf(this);
        return newsolver;
    }

    /** Returns false, indicating that this director does not need to 
     *  modify the topology during the execution.
     *
     *  @return False.
     */
    protected boolean _writeAccessPreference() {
        return false;
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         private methods                        ////
    /** Create and initialize all the parameters. All parameters take
     *  their default values.
     */
    private void _initParameters() {
        try {
            _startTime = 0.0;
            _stopTime = 1.0;
            _initStepSize = 0.1;
            _minStepSize = 1e-5;
            _maxIterations = 20;
            _lteTolerance = 1e-4;
            _valueResolution = 1e-6;
            _timeResolution = 1e-10;


            _paramStartTime = new CTParameter(
                this, "StartTime", new DoubleToken(_startTime));
            _paramStopTime = new CTParameter(
                this, "StopTime", new DoubleToken(_stopTime));
            _paramInitStepSize = new CTParameter(
                this, "InitialStepSize", new DoubleToken(_initStepSize));
            _paramMinStepSize = new CTParameter(
                this, "MinimumStepSize", new DoubleToken(_minStepSize));
            _paramMaxIterations = new CTParameter(
                this, "MaximumIterationsPerStep", 
                new IntToken(_maxIterations));
            _paramLTETolerance =  new CTParameter(
                this, "LocalTrancationErrorTolerance",
                new DoubleToken(_lteTolerance));
            _paramValueResolution =  new CTParameter(
                this, "ConvergeValueResolution", 
                new DoubleToken(_valueResolution));
            _paramTimeResolution= new CTParameter(
                this, "TimeResolution", new DoubleToken(_timeResolution));

        } catch (IllegalActionException e) {
            //Should never happens. The parameters are always compatible.
            throw new InternalErrorException("Parameter creation error.");
        } catch (NameDuplicationException ex) {
            throw new InvalidStateException(this,
                    "Parameter name duplication.");
        }
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    // current ODE solver.
    private ODESolver _currentSolver = null;

    // parameters.
    private CTParameter _paramStartTime;
    private CTParameter _paramStopTime;
    private CTParameter _paramInitStepSize;
    private CTParameter _paramMinStepSize;
    private CTParameter _paramMaxIterations;
    private CTParameter _paramLTETolerance;
    private CTParameter _paramValueResolution;
    private CTParameter _paramTimeResolution;


    //values
    private double _startTime;
    private double _stopTime;
    private double _initStepSize;
    private double _minStepSize;
    private int _maxIterations;
    private double _lteTolerance;
    private double _valueResolution;
    private double _timeResolution;


    private LinkedList _parameterEvents = null;

    // Simulation progress variables.
    private double _currentTime;
    private double _currentStepSize;
    private double _suggestedNextStepSize;

    //A table for wave form break points.
    private TotallyOrderedSet _breakPoints;
}
