/* An abstract base class for directors for continuous time simulation.

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

@ProposedRating Green (liuj@eecs.berkeley.edu)
@AcceptedRating Yellow (johnr@eecs.berkeley.edu)
*/

package ptolemy.domains.ct.kernel;

import ptolemy.domains.ct.kernel.util.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.Actor;
import ptolemy.actor.Receiver;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.sched.StaticSchedulingDirector;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// CTDirector
/**
This is the abstract base class for directors in the CT domain.
<P>
A CTDirector has a CTScheduler which provides the schedules for firing
the actors in different phases of the execution.
<P>
CTDirectors may have more than one ODE solvers. In each iteration, one
of the ODE solvers is taking charge of solving the ODEs. This solver
is called the <I>current ODE solver</I>.
<P>
The continuous time (CT) domain is a timed domain. There is a global
notion of time that all the actors are aware of. Time is maintained
by the director. The method getCurrentTime() returns the current
global time. Time can be set by setCurrentTime() method, but this
method should not the called by the actors. Time can only be advanced
by directors or its ODE solvers.
<P>
This base class maintains a list of parameters that may be used by
ODE solvers and actors. These parameters are: <Br>
<LI> <code>startTime</code>: The start time of the
simulation. The parameter is effective only if the director
is at the top level. Default value is 0.0.</LI><BR>
<LI> <code>stopTime</code>: The stop time of the simulation.
 The parameter is effective only if the director
is at the top level. Default value is 1.0.</LI><BR>
<LI> <code>initStepSize</code>: The suggested integration step size
from the user. This will be the step size for fixed step
size ODE solvers. However, it is just a guide for variable step size
ODE solvers. Default value is 0.1</LI><Br>
<LI> <code>minStepSize</code>: The minimum step
size that the user wants to use in the simulation. Default value is 1e-5.
</LI>
<LI> <code>maxStepSize</code>: The maximum step
size the user wants to use in the simulation. Usually used to control
the simulation speed. Default value is 1.0.
</LI><Br>
<LI> <code>maxIterations</code>:
Used only in implicit ODE solvers. This is the maximum number of
iterations for finding the fixed point at one time point.
Default value is 20. </LI><Br>
<LI> <code>errorTolerance</code>: This is the local truncation
error tolerance, used for controlling the integration accuracy
in variable step size ODE solvers. If the local truncation error
at some step size control actors are greater than this tolerance, then the
integration step is considered failed, and should be restarted with
a reduced step size. Default value 1e-4. </LI><Br>
<LI> <code>valueResolution</code>:
 This is used to control the convergence of fixed point iterations.
If in two successive iterations the differences of the state variables
is less than this resolution, then the fixed point is considered reached.
Default value is 1e-6.<LI><Br>
<LI> <code>timeResolution</code>: The minimum resolution
of time. If two time values differ less than this value,
they are considered equivalent. Default value is 1e-10. </LI><Br>
<P>
This director also maintains a breakpoint table to record all
predictable breakpoints in the future.
The breakpoints are sorted in their chronological order in the table.
Breakpoints at the "same" time (controlled by time resolution) are
considered to be one. A breakpoint can be inserted into the table by
calling the fireAt() method. How to deal with these breakpoints
could be director dependent.
@author Jie Liu
@version $Id$
@see ptolemy.actor.Director
*/
public abstract class CTDirector extends StaticSchedulingDirector {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *  All the parameters take their default values. A CTScheduler
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
                    "Error setting a CTScheduler.");
        }
    }

    /** Construct a director in the workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  All the parameters take their default values. A CTScheduler
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
                    "Error setting a CTScheduler.");
        }
    }

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string.
     *  All the parameters take their default values. A CTScheduler
     *  is created.
     *  @param workspace Object for synchronization and version tracking
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *  with the specified container.  May also be thrown by a derived class.
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
    ////                         parameters                        ////

    /** ODE solving error tolerance, only effective in variable step
     *  size methods.
     *  The default value is 1e-4, of type DoubelToken.
     */
    public Parameter errorTolerance;

    /** User's guide for the initial integration step size.
     *  The default value is 0.1, of
     *  type DoubleToken.
     */
    public Parameter initStepSize;

    /** The maximum number of iterations in looking for a fixed-point.
     *  The default value is 20, of type IntToken.
     */
    public Parameter maxIterations;

    /** User's guide for the maximum integration step size.
     *  The default value is 1.0, of
     *  type DoubleToken.
     */
    public Parameter maxStepSize;

    /** User's guide for the minimum integration step size.
     *  The default value is 1e-5, of
     *  type DoubleToken.
     */
    public Parameter minStepSize;

    /** Starting time of the simulation. The default value is 0.0, of
     *  type DoubleToken.
     */
    public Parameter startTime;

    /** Stop time of the simulation. The default value is 1.0, of
     *  type DoubleToken.
     */
    public Parameter stopTime;

    /** The resolution in comparing time.
     *  The default value is 1e-10, of type DoubelToken.
     */
    public Parameter timeResolution;

    /** Value resolution in looking for a fixed-point.
     *  The default value is 1e-6, of type DoubelToken.
     */
    public Parameter valueResolution;


    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Public variable indicating whether the statistics
     *  is to be collected.
     */
    public boolean STAT = false;

    /** The number of integration steps.
     */
    public int NSTEP = 0;

    /** The number of function evaluations, which is the same as the
     *  total number of rounds when solving the ODEs.
     */
    public int NFUNC = 0;

    /** The number of failed steps.
     */
    public int NFAIL = 0;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute. If the changed attribute
     *  matches a parameter of the director, then the corresponding
     *  local copy of the parameter value will be updated.
     *  @param attr The changed parameter.
     *  @exception IllegalActionException If the parameter set is not valid.
     */
    public void attributeChanged(Attribute attr)
            throws IllegalActionException {
        if(_debugging) _debug(attr.getName() + " updating.");
        if(attr == startTime) {
            Parameter param = (Parameter)attr;
            _startTime = ((DoubleToken)param.getToken()).doubleValue();
        } else if(attr == stopTime) {
            Parameter param = (Parameter)attr;
            setStopTime(((DoubleToken)param.getToken()).doubleValue());
        } else if(attr == initStepSize) {
            Parameter param = (Parameter)attr;
            _initStepSize = ((DoubleToken)param.getToken()).doubleValue();
        } else if(attr == errorTolerance) {
            Parameter param = (Parameter)attr;
            _lteTolerance = ((DoubleToken)param.getToken()).doubleValue();
        } else if(attr == minStepSize) {
            Parameter param = (Parameter)attr;
            _minStepSize = ((DoubleToken)param.getToken()).doubleValue();
        } else if(attr == maxStepSize) {
            Parameter param = (Parameter)attr;
            _maxStepSize = ((DoubleToken)param.getToken()).doubleValue();
        } else if(attr == valueResolution) {
            Parameter param = (Parameter)attr;
            _valueResolution = ((DoubleToken)param.getToken()).doubleValue();
        } else if(attr == timeResolution) {
            Parameter param = (Parameter)attr;
            _timeResolution = ((DoubleToken)param.getToken()).doubleValue();
            TotallyOrderedSet bptable = getBreakPoints();
            // change the breakpoint table comparator if it is created.
            if(bptable!=null) {
                FuzzyDoubleComparator comp =
                    (FuzzyDoubleComparator) bptable.getComparator();
                comp.setThreshold(_timeResolution);
            }
        } else if(attr == maxIterations) {
            Parameter param = (Parameter)attr;
            _maxIterations = ((IntToken)param.getToken()).intValue();
        }
    }

    /** Return true if the director can be an inside director, i.e.
     *  a director of an opaque composite actor not at the top level.
     *  In this base class, it always return false.
     *  Derived class may override this to show whether it can
     *  serve as an inside director. This value is hard coded,
     *  in the sense that it cannot be set at run-time.
     */
    public abstract boolean canBeInsideDirector();

    /** Return true if the director can be a top-level director.
     *  In this base class, it always return false.
     *  Derived class may override this to show whether it can
     *  serve as a top-level director. This value is hard coded,
     *  in the sense that it can not be set at run-time.
     */
    public abstract boolean canBeTopLevelDirector();

    /** Clone the director into the specified workspace. This calls the
     *  base class and then copies the parameter of this director.  The new
     *  actor will have the same parameter values as the old.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     */
    public Object clone(Workspace ws) {
        try {
            CTDirector newobj = (CTDirector)(super.clone(ws));
            newobj.startTime =
                (Parameter)newobj.getAttribute("startTime");
            newobj.stopTime =
                (Parameter)newobj.getAttribute("stopTime");
            newobj.initStepSize =
                (Parameter)newobj.getAttribute("initStepSize");
            newobj.minStepSize =
                (Parameter)newobj.getAttribute("minStepSize");
            newobj.maxStepSize =
                (Parameter)newobj.getAttribute("maxStepSize");
            newobj.maxIterations =
                (Parameter)newobj.getAttribute("maxIterations");
            newobj.errorTolerance =
                (Parameter)newobj.getAttribute("errorTolerance");
            newobj.valueResolution =
                (Parameter)newobj.getAttribute("valueResolution");
	    newobj.timeResolution =
                (Parameter)newobj.getAttribute("timeResolution");
            return newobj;
        } catch (CloneNotSupportedException ex) {
            // Errors should not occur here...
            throw new InternalErrorException(
                    "Clone failed: " + ex.getMessage());
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

    /** Return the current integration step size.
     *  @return the current step size.
     */
    public final double getCurrentStepSize() {
        return _currentStepSize;
    }

    /** Return the begin time of the current iteration.
     *  @return the begin time of the current iteration.
     */
    public double getIterationBeginTime() {
        return _iterationBeginTime;
    }

    /** Return the initial step size.
     *  @return the initial step size.
     */
    public final double getInitialStepSize() {
        return _initStepSize;
    }

    /** Return the local truncation error tolerance, used by
     *  variable step size solvers.
     *  @return The local truncation error tolerance.
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

    /** Return the current iteration begin time plus the current
     *  step size.
     *  @return The iteration begin time plus the current step size.
     */
    public final double getNextIterationTime() {
        return getIterationBeginTime() + getCurrentStepSize();
    }
    
    /** Return the start time parameter value.
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

    /** Return the suggested next step size. The suggested step size is
     *  the step size that the step-size-control actors suggested
     *  at the end of last integration step. It is the prediction
     *  of the new step size.
     *  @return The suggested next step size.
     */
    public final double getSuggestedNextStepSize() {
        return _suggestedNextStepSize;
    }

    /** Return the time resolution such that two time stamps within this
     *  resolution are considered identical.
     *  @return The time resolution.
     */
    public final double getTimeResolution() {
        return _timeResolution;
    }

    /** Return the value resolution, used for testing if an implicit method
     *  has reached the fixed point. Two values differ less than
     *  this accuracy are considered identical in the fixed point
     *  calculation.
     *
     *  @return The value resolution for finding fixed point.
     */
    public final double getValueResolution() {
        return _valueResolution;
    }

    /** Register a breakpoint at a future time. The
     *  Director will fire exactly at each registered time.
     *  From this director's point of view, it is irrelavant
     *  which actor requests the breakpoint. All actors will be
     *  executed at every breakpoint.
     *  The first argument is used only for reporting
     *  exceptions.
     *  @param actor The actor that requested the fire
     *  @param time The fire time
     *  @exception IllegalActionException If the time is before
     *  the current time
     */
    public void fireAt(Actor actor, double time)
            throws IllegalActionException{
        if(time < getCurrentTime()-getTimeResolution()) {
            throw new IllegalActionException((Nameable)actor,
                    "Requested fire time: " + time + " is earlier than" +
                    " the current time." + getCurrentTime() );
        }
        // _breakPoints can never be null.
        _breakPoints.insert(new Double(time));
    }

    /** Set the current time to be the start time or the current
     *  time of the executive director, depends on whether this
     *  director is at the top level. Initialize all actors.
     *  @exception IllegalActionException If thrown by an actor.
     */
    public void initialize() throws IllegalActionException {
        if(_debugging) {
            _debug(getFullName(), " initializing");
            _debug(getFullName(), " get start Time parameter " +
                    ((DoubleToken)startTime.getToken()).doubleValue());
        }
        CompositeActor containersContainer =
            (CompositeActor)getContainer().getContainer();
        if( containersContainer == null ) {
            setCurrentTime(((DoubleToken)startTime.getToken()).doubleValue());
        } else {
            double time =
                containersContainer.getDirector().getCurrentTime();
            //startTime.setToken(new DoubleToken(time));
            setCurrentTime(time);
        }
        if(_debugging) {
            _debug(getFullName(), " set current time to " + getCurrentTime());
        }
        super.initialize();
    }

    /** Return true if this is the first iteration after a breakpoint.
     *  In a breakpoint iteration, the ODE solver is the breakpoint
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
        System.out.println(getName() + " return new CTReceiver.");
        return new CTReceiver();
    }

    /** Prepare for an execution.
     *  Check whether the director has a container and a scheduler.
     *  Check whether the director fits this level of hirarchy,
     *  by checking canBeInsideDirector() and canBeTopLevelDirector().
     *  Invalidate the schedule. Clear statistic variables.
     *  Clear the break point table.
     *  Set the current time to the start time.
     *  And preinitialize all the directed actors.
     *  Time is <B>not</B> guaranteed to be correct in this stage.
     *  Time is synchronzied in the initialize() method.
     *  @exception IllegalActionException If the director has no
     *  container, the director does not fit this level of hierarchy,
     *  or there is no scheduler.
     */
    public void preinitialize() throws IllegalActionException {
        if(_debugging) _debug(getFullName(), "preinitializing.");
        System.out.println(getName() + "preinitializing.");
        //from here
        CompositeActor ca = (CompositeActor) getContainer();
        if (ca == null) {
            throw new IllegalActionException(this, "Has no container.");
        }
        if (ca.getContainer() != null) {
            if (!canBeInsideDirector()) {
                throw new IllegalActionException(this,
                        " cannot serve as an inside director.");
            }
        } else {
            if (!canBeTopLevelDirector()) {
                throw new IllegalActionException(this,
                        " cannot serve as an top-level director.");
            }
        }
        CTScheduler scheduler = (CTScheduler)getScheduler();
        if (scheduler == null) {
            throw new IllegalActionException( this,
                    "does not have a scheduler.");
        }
        if(STAT) {
            NSTEP = 0;
            NFUNC = 0;
            NFAIL = 0;
        }
        // invalidate schedule
        scheduler.setValid(false);
        if(_debugging) _debug(getFullName(), " clearing break point table.");
        TotallyOrderedSet bps = getBreakPoints();
        if(bps != null) {
            bps.clear();
        } else {
            _breakPoints = new TotallyOrderedSet(
                    new FuzzyDoubleComparator(_timeResolution));
        }
        if(_debugging) _debug(getName(), " preinitialize actors");
        Iterator allactors = ca.deepEntityList().iterator();
        while (allactors.hasNext()) {
            Actor actor = (Actor)allactors.next();
            if(_debugging) _debug("Invoking preinitialize(): ",
                    ((NamedObj)actor).getFullName());
            System.out.println("Invoking preinitialize(): " + 
                    ((NamedObj)actor).getFullName());
            actor.preinitialize();
        }
    }

    /** Set the current step size. This variable is very import during
     *  the simulation and should NOT be changed in the middle of an
     *  iteration.
     *  @param curstepsize The step size used for currentStepSize().
     */
    public void setCurrentStepSize(double curstepsize) {
        _currentStepSize = curstepsize;
    }

    /** Set the current time of the model under this director.
     *  This override the setCurrentTime in the Director base class.
     *  It is OK that the new time is less than the current time
     *  in the director, since CT sometimes needs roll-back.
     *  This is a critical parameter in an execution, and the
     *  actors are not supposed to call it.
     *  @exception IllegalActionException Not thrown in this class.
     *  @param newTime The new current simulation time.
     */
    public void setCurrentTime(double newTime) throws IllegalActionException {
        _currentTime = newTime;
    }

    /** Set the stop time for the simulation.  The stop time is not
     *  registered as a breakpoint in this method. The extended directors
     *  should do it themselves if needed.
     *  @param tstop The stop time for the simulation.
     */
    public void setStopTime(double tstop) {
        _stopTime = tstop;
    }

    /** Set the suggested next step size, upper bounded by the
     *  maximum step size.
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
            _stopTime = java.lang.Double.MAX_VALUE;
            _startTime = 0.0;
            _initStepSize = 0.1;
            _minStepSize = 1e-5;
            _maxStepSize = 1.0;
            _maxIterations = 20;
            _lteTolerance = 1e-4;
            _valueResolution = 1e-6;
            _timeResolution = 1e-10;


            startTime = new Parameter(
                    this, "startTime", new DoubleToken(0.0));
            startTime.setTypeEquals(BaseType.DOUBLE);
            stopTime = new Parameter(
                    this, "stopTime", new DoubleToken(_stopTime));
            stopTime.setTypeEquals(BaseType.DOUBLE);
            initStepSize = new Parameter(
                    this, "initStepSize", new DoubleToken(_initStepSize));
            initStepSize.setTypeEquals(BaseType.DOUBLE);
            minStepSize = new Parameter(
                    this, "minStepSize", new DoubleToken(_minStepSize));
            minStepSize.setTypeEquals(BaseType.DOUBLE);
            maxStepSize = new Parameter(
                    this, "maxStepSize", new DoubleToken(_maxStepSize));
            maxStepSize.setTypeEquals(BaseType.DOUBLE);
            maxIterations = new Parameter(
                    this, "maxIterations",
                    new IntToken(_maxIterations));
            maxIterations.setTypeEquals(BaseType.INT);
            errorTolerance =  new Parameter(
                    this, "errorTolerance",
                    new DoubleToken(_lteTolerance));
            errorTolerance.setTypeEquals(BaseType.DOUBLE);
            valueResolution =  new Parameter(
                    this, "valueResolution",
                    new DoubleToken(_valueResolution));
            valueResolution.setTypeEquals(BaseType.DOUBLE);
            timeResolution = new Parameter(
                    this, "timeResolution", new DoubleToken(_timeResolution));
            timeResolution.setTypeEquals(BaseType.DOUBLE);

        } catch (IllegalActionException e) {
            //Should never happens. The parameters are always compatible.
            throw new InternalErrorException("Parameter creation error.");
        } catch (NameDuplicationException ex) {
            throw new InvalidStateException(this,
                    "Parameter name duplication.");
        }
    }

    /** Instantiate ODESolver from its classname. Given the solver's full
     *  class name, this method will try to instantiate it by looking
     *  for the java class.
     *  @param solverclass The solver's full class name.
     *  @exception IllegalActionException If the solver is unable to be
     *       created.
     */
    protected ODESolver _instantiateODESolver(String solverclass)
            throws IllegalActionException {
        ODESolver newsolver;
        if(_debugging) _debug("instantiating solver..."+solverclass);
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

    /** set the given solver to be the current ODE Solver.
     *  Derived class may throw an exception if the argument
     *  cannot serve as the current ODE solver
     *  @param solver The solver to be set.
     *  @exception  IllegalActionException Not thrown in this base class.
     *     It may be thrown by the derived classes if the solver is not
     *     appropriate.
     */
    protected void _setCurrentODESolver(ODESolver solver)
            throws IllegalActionException {
        _currentSolver = solver;
    }

    /** Set whether this is an iteration just after a breakpoint.
     *  @param bp True if this is a breakpoint iteration.
     */
    protected void _setIsBPIteration(boolean bp) {
        _bpIteration = bp;
    }

    /** Set the iteration begin time.
     *  @param begintime The iteration begin time.
     */
    protected void _setIterationBeginTime(double begintime) {
        _iterationBeginTime = begintime;
    }

    /** Returns false always, indicating that this director does not need to
     *  modify the topology during one iteration.
     *
     *  @return False.
     */
    protected boolean _writeAccessRequired() {
        return false;
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    // current ODE solver.
    private ODESolver _currentSolver = null;

    // local copies of parameters.
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

    // Simulation step sizes.
    private double _currentStepSize;
    private double _suggestedNextStepSize;

    //A table for breakpoints.
    private TotallyOrderedSet _breakPoints;

    // the begin time of a iteration. This value is remembered so that
    // we don't need to resolve it from the iteration end time and step size.
    private double _iterationBeginTime;
}
