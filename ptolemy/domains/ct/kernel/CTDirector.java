/* An abstract base class for directors of Continuous time simulation.

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

@ProposedRating yellow (liuj@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
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
in variable step size ODE sovlers. If the local truncation error
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
public abstract class CTDirector extends StaticSchedulingDirector {

    public static boolean STAT = true;
    public  int NSTEP = 0;
    public  int NFUNC = 0;
    public  int NFAIL = 0;
    public  int NROLL = 0;

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *  All the parameters takes their default values.The scheduler
     *  is created.
     */
    public CTDirector() {
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

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  All the parameters takes their default values.The scheduler
     *  is created.
     *  @param workspace The workspace of this object.
     */
    public CTDirector(Workspace workspace) {
        super(workspace);
        _initParameters();
        try {
            setScheduler(new CTScheduler(workspace));
        }catch(IllegalActionException e) {
            // Should never occur.
            throw new InternalErrorException(this.getFullName() +
                "setting scheduler error");
        }
    }

    /** Construct a director in the given container with the given name.
     *  If the container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  All the parameters takes their default values.The scheduler
     *  is created.
     *  @param workspace Object for synchronization and version tracking
     *  @param name Name of this director.
     *  @exception It may be thrown in derived classes if the
     *      director is not compatible with the specified container.
     */
    public CTDirector(CompositeActor container, String name)
            throws IllegalActionException {
        super(container, name);
        _initParameters();
        try {
            setScheduler(new CTScheduler(container.workspace()));
        }catch(IllegalActionException e) {
            // Should never occur.
            throw new InternalErrorException(this.getFullName() +
                "setting scheduler error");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute. If the changed atrribute matches
     *  a parameter of the director, then the coresponding private copy of the
     *  parameter value will be updated.
     *  @param param The changed parameter.
     *  @exception IllegalActionException If the parameter set is not valid.
     *     Not thrown in this class. May be needed by derived classes.
     */
    public void attributeChanged(Attribute attr) 
            throws IllegalActionException {
        _debug(attr.getName() + " updating.");
        if(attr == StopTime) {
            Parameter param = (Parameter)attr;
            setStopTime(((DoubleToken)param.getToken()).doubleValue());
        } else if(attr == InitStepSize) {
            Parameter param = (Parameter)attr;
            _initStepSize = ((DoubleToken)param.getToken()).doubleValue();
        } else if(attr == StartTime) {
            Parameter param = (Parameter)attr;
            _startTime = ((DoubleToken)param.getToken()).doubleValue();
        } else if(attr == ErrorTolerance) {
            Parameter param = (Parameter)attr;
            _lteTolerance = ((DoubleToken)param.getToken()).doubleValue();
        } else if(attr == MinStepSize) {
            Parameter param = (Parameter)attr;
            _minStepSize = ((DoubleToken)param.getToken()).doubleValue();
        } else if(attr == MaxStepSize) {
            Parameter param = (Parameter)attr;
            _maxStepSize = ((DoubleToken)param.getToken()).doubleValue();
        } else if(attr == ValueResolution) {
            Parameter param = (Parameter)attr;
            _valueResolution = ((DoubleToken)param.getToken()).doubleValue();
        } else if(attr == TimeResolution) {
            Parameter param = (Parameter)attr;
            _timeResolution = ((DoubleToken)param.getToken()).doubleValue();
        } else if(attr == MaxIterations) {
            Parameter param = (Parameter)attr;
            _maxIterations = ((IntToken)param.getToken()).intValue();
        }
    }

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

    /** Return the fire begin time, which is the value set by 
     *  setFireBeginTime().
     *  @return Fire begin time.
     */
    public double getFireBeginTime() {
        return _fireBeginTime;
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
    public final double getErrorTolerance() {
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

    /** Return the maximum step size used in variable step size
     *  ODE solvers.
     *  @return The maximum step size.
     */
    public final double getMaxStepSize() {
        return _maxStepSize;
    } 

    /** Return the minimum step size used in variable step size
     *  ODE solvers.
     *  @return The minimum step size.
     */
    public final double getMinStepSize() {
        return _minStepSize;
    } 

    /** Return the current step size plus the current time.
     *  @return The current time plus the current step size.
     */
    public final double getNextIterationTime() {
        return getFireBeginTime() + getCurrentStepSize();
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
        if(time < getCurrentTime()-getTimeResolution()) {
            throw new IllegalActionException((Nameable)actor, 
                    "Requested an Fire time " + time + " is earlier than" +
                    " the current time." + getCurrentTime() );
        }
        _breakPoints.insert(new Double(time));
    }

    /** Return true if this is a breakpoint iteration. A breakpoint
     *  iteration is the iteration just after a breakpoint. In a 
     *  breakpoint iteration, the ODE solver is the breakpoint 
     *  ODE solver, and the step size is the minimum step size.
     *  @return True if this is a breakpoint iteration.
     */
    public boolean isBPIteration() {
        return _bpIteration;
    }

    /** Return a new CTReceiver.
     *  @return A new CTReceiver.
     */
    public Receiver newReceiver() {
        return new CTReceiver();
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
        _startTime = tstart;
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
        if(nextstep >getMaxStepSize()) {
            _suggestedNextStepSize = getMaxStepSize();
        } else {
            _suggestedNextStepSize = nextstep;
        }
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

    /** Create and initialize all parameters to their default values.
     */
    protected void _initParameters() {
        try {
            _startTime = 0.0;
            _stopTime = 1.0;
            _initStepSize = 0.1;
            _minStepSize = 1e-5;
            _maxStepSize = 1.0;
            _maxIterations = 20;
            _lteTolerance = 1e-4;
            _valueResolution = 1e-6;
            _timeResolution = 1e-10;


            StartTime = new Parameter(
                this, "StartTime", new DoubleToken(_startTime));
            StopTime = new Parameter(
                this, "StopTime", new DoubleToken(_stopTime));
            InitStepSize = new Parameter(
                this, "InitialStepSize", new DoubleToken(_initStepSize));
            MinStepSize = new Parameter(
                this, "MinimumStepSize", new DoubleToken(_minStepSize));
            MaxStepSize = new Parameter(
                this, "MaximumStepSize", new DoubleToken(_maxStepSize));
            MaxIterations = new Parameter(
                this, "MaximumIterationsPerStep", 
                new IntToken(_maxIterations));
            ErrorTolerance =  new Parameter(
                this, "LocalTrancationErrorTolerance",
                new DoubleToken(_lteTolerance));
            ValueResolution =  new Parameter(
                this, "ConvergeValueResolution", 
                new DoubleToken(_valueResolution));
            TimeResolution= new Parameter(
                this, "TimeResolution", new DoubleToken(_timeResolution));

        } catch (IllegalActionException e) {
            //Should never happens. The parameters are always compatible.
            throw new InternalErrorException("Parameter creation error.");
        } catch (NameDuplicationException ex) {
            throw new InvalidStateException(this,
                    "Parameter name duplication.");
        }
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
        _debug("instantiating solver..."+solverclass);
        try {
            Class solver = Class.forName(solverclass);
            newsolver = (ODESolver)solver.newInstance();
        } catch(ClassNotFoundException e) {
            throw new IllegalActionException( this, "ODESolver: "+
                solverclass + " not found.");
        } catch(InstantiationException e) {
            throw new IllegalActionException( this, "ODESolver: "+
                solverclass + " instantiation failed.");
        } catch(IllegalAccessException e) {
            throw new IllegalActionException( this, "ODESolver: "+
                solverclass + " not accessible.");
        }
        newsolver._makeSolverOf(this);
        return newsolver;
    }

    /** Set whether this is a breakpoint processing iteration.
     *  @param bp True if this is a breakpoint iteration.
     */
    protected void _setIsBPIteration(boolean bp) {
        _bpIteration = bp;
    }

    /** Returns false, indicating that this director does not need to 
     *  modify the topology during the execution.
     *
     *  @return False.
     */
    protected boolean _writeAccessRequired() {
        return false;
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         private methods                        ////

    /** Set the fire begin time.
     *  @param fbt Fire begin time.
     */
    protected void _setFireBeginTime(double fbt) {
        _fireBeginTime = fbt;
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    // current ODE solver.
    private ODESolver _currentSolver = null;

    // parameters.
    public Parameter StartTime;
    public Parameter StopTime;
    public Parameter InitStepSize;
    public Parameter MinStepSize;
    public Parameter MaxStepSize;    
    public Parameter MaxIterations;
    public Parameter ErrorTolerance;
    public Parameter ValueResolution;
    public Parameter TimeResolution;


    //values
    private double _startTime;
    private double _stopTime;
    private double _initStepSize;
    private double _minStepSize;
    private double _maxStepSize;    
    private int _maxIterations;
    private double _lteTolerance;
    private double _valueResolution;
    private double _timeResolution;

    //indicate whether this is a breakpoint iteration.
    private boolean _bpIteration = false;
    
    // Simulation progress variables.
    private double _currentTime;
    private double _currentStepSize;
    private double _suggestedNextStepSize;

    //A table for wave form break points.
    private TotallyOrderedSet _breakPoints;

    // the start time of a iteration. This value is remembered so that
    // we don't need to resolve it from the end time and step size.
    private double _fireBeginTime;
}
