/* Base class for top level CT directors.

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
@ProposedRating red (liuj@eecs.berkeley.edu)
*/

package ptolemy.domains.ct.kernel;

import ptolemy.domains.ct.kernel.util.*;
import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.data.expr.*;
import ptolemy.data.*;
import java.util.*;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// CTSingleSolverDirector
/** 
A CT director that does not change its ODE solver.
@author Jie Liu
@version $Id$
*/
public class CTSingleSolverDirector extends StaticSchedulingDirector
        implements CTDirector, ParameterListener{

    public static final boolean VERBOSE = false;
    public static final boolean DEBUG = false;

    /** Construct a CTDirector with no name and no Container.
     *  The default startTime and stopTime are all zeros. There's no
     *  scheduler associated.
     */	
    public CTSingleSolverDirector () {
        super();
        try {
            setScheduler(new CTScheduler());
        }catch(IllegalActionException e) {
            // Should never occur.
            throw new InternalErrorException(this.getFullName() + 
                "setting scheduler error");
        }
        _initParameters();
    }

    /** Construct a CTDirector in the default workspace with the given name.
     *  If the name argument is null, then the name is set to the empty
     *  string. The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  The default startTime and stopTime are all zeros. There's no
     *  scheduler associated.
     *
     *  @param name The name of this director.
     */
    public CTSingleSolverDirector (String name) {
        super(name);
        try {
            setScheduler(new CTScheduler());
        }catch(IllegalActionException e) {
            // Should never occur.
            throw new InternalErrorException(this.getFullName() + 
                "setting scheduler error");
        }
        _initParameters();
    }

    /** Construct a director in the given workspace with the given name.
     *  If the workspace argument is null, use the default workspace.
     *  The director is added to the list of objects in the workspace.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  The default startTime and stopTime are all zeros. There's no
     *  scheduler associated.
     *
     *  @param workspace Object for synchronization and version tracking
     *  @param name Name of this director.
     */
    public CTSingleSolverDirector (Workspace workspace, String name) {
        super(workspace, name);
        try {
            setScheduler(new CTScheduler());
        }catch(IllegalActionException e) {
            // Should never occur.
            throw new InternalErrorException(this.getFullName() +
                "setting scheduler error");
        }
        _initParameters();
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Return the current ODESolver. 
     *  @return The current ODESolver
     */
    public final ODESolver getCurrentODESolver() {
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

    /** Return the initial step size, as in the parameter.
     */
    public final double getInitialStepSize() {
        return ((DoubleToken)_paramInitStepSize.getToken()).doubleValue();
    }

    /** Return the startTime.
     *  @return the startTime.
     */
    public final double getStartTime() {
        return _startTime;
    }

    /** Return the stopTime.
     *  @return the stopTime.
     */
    public final double getStopTime() {
        return _stopTime;
    }

    /** Return the time accuracy such that two time stamp within this
     *  accuracy is considered identical.
     *  @return The time accuracy.
     */
    public final double getTimeAccuracy() {
        return _timeAccuracy;
    }

    public final double getLTETolerant() {
        return _lteTolerant;
    }

    public final double getValueAccuracy() {
        return _valueAccuracy;
    }
    
    public final double getMinStepSize() {
        return _minStepSize;
    }

    public final int getMaxIterations() {
        return _maxIterations;
    }

    /** This does the initialization for the entire subsystem. This 
     *  is called exactly once at the start of the entire execution.
     *  It set the current time to the start time and the current step
     *  size to the initial step size.
     *  It invoke the initialize() method for all the Actors in the
     *  system. The ODE solver is not checked here, since in some
     *  cases, the ODE solver may be set after the initialize phase.
     *
     *  @exception IllegalActionException If there's no scheduler or
     *       thrown by a contained actor.
     */
    public void initialize() throws IllegalActionException {
        if (VERBOSE||DEBUG) {
            System.out.println("Director initialize.");
        }
        CompositeActor ca = (CompositeActor) getContainer();
        if (ca == null) {
            if(DEBUG) {
                System.out.println("Director has no container.");
            }
            throw new IllegalActionException(this, "Has no container.");
        }
        if (ca.getContainer() != null) {
            if(DEBUG) {
                System.out.println("Director can only be the top director.");
            }
            throw new IllegalActionException(this,
            " can only serve as the top level director.");
        }
        CTScheduler sch = (CTScheduler)getScheduler();
        if (sch == null) {
            if(DEBUG) {
                System.out.println("Director does not have a scheduler.");
            }
            throw new IllegalActionException( this,
            "does not have a scheduler.");
        }
        if(VERBOSE) {
            System.out.println("updating parameters");
        }
        updateParameters();
        // Instanciate ODE solver
        if(VERBOSE) {
            System.out.println("instantiating ODE solver"+_solverclass);
        }
        if(getCurrentODESolver() == null) {
            _defaultSolver = _instantiateODESolver(_solverclass);
        }
        // set time
        setCurrentTime(getStartTime());
        setCurrentStepSize(getInitialStepSize());
        sch.setValid(false);
        _first = true;
        if (VERBOSE) {
            System.out.println("Director.super initialize.");
        }
        super.initialize();
    }

    /** Return a CTReceiver.
     */
    public Receiver newReceiver() {
        return new CTReceiver();
    }

    /** Perform mutation and process pause/stop request.
     *  If the CTSubSystem is requested a stop (if CTSubSystem.isPaused()
     *  returns true) then pause the thread.
     *  The pause can be wake up by notify(), at that time if the 
     *  CTSubSystem is not paused (isPaused() returns false) then
     *  resume the simulation. So the simulation can only be
     *  paused at the prefire stage.
     *  If stop is requested return false, otherwise return true.
     *  Transfer time from the outer domain, if there is any. If this
     *  is the toplevel domain, if the currenttime + currentstepsize
     *  is greater than the stop time, set the currentStepSize to be
     *  stopTime-currentTime.
     *
     *  @return true If stop is not requested
     *  @exception IllegalActionException If the pause is interrupted or it
     *       is thrown by a contained actor.
     *  @exception NameDuplicationException If thrown by a contained actor.
     */
    public boolean prefire() throws IllegalActionException {
         if (VERBOSE) {
            System.out.println("Director prefire.");
        }
        if(!scheduleValid()) {
            // mutation occured, redo the schedule;
            CTScheduler scheduler = (CTScheduler)getScheduler();
            if (scheduler == null) {
                throw new IllegalActionException (this,
                "does not have a Scheuler.");
            }
            scheduler.schedule();
            setScheduleValid(true);
        }
        updateParameters();
        setCurrentODESolver(_defaultSolver);
        // prefire all the actors.
        boolean ready = true;
        CompositeActor ca = (CompositeActor) getContainer();
        Enumeration actors = ca.deepGetEntities();
        while(actors.hasMoreElements()) {
            Actor a = (Actor) actors.nextElement();
            ready = ready && a.prefire();
        }
        return ready;
   }

   /**  Fire the system for one iteration.
     *
     *  @exception IllegalActionException If thrown by the ODE solver.
     */
    public void fire() throws IllegalActionException {
        if (_first) {
            _first = false;
            produceOutput();
            updateStates(); // call postfire on all actors 
            return;
        }
        ODESolver solver = getCurrentODESolver();
        double bp;
        // If now is a break point, remove the break point from table;
        if(!_breakPoints.isEmpty()) {
            bp = ((Double)_breakPoints.first()).doubleValue();
            if(bp <= getCurrentTime()) {
                // break point now!
                _breakPoints.removeFirst();   
            }
            //adjust step size;
            if(!_breakPoints.isEmpty()) {
                bp = ((Double)_breakPoints.first()).doubleValue();
                double iterEndTime = getCurrentTime()+getCurrentStepSize();
                if (iterEndTime > bp) {
                    setCurrentStepSize(bp-getCurrentTime());
                }
            }
        }
        solver.iterate();
        produceOutput();
        updateStates(); // call postfire on all actors 
    }

    /** Register a break point in the future. This request the
     *  Director to fire exactly at the each registered time.
     *  Override the fireAfterDelay() method in Director.
     */
    public void fireAfterDelay(Actor actor, double delay) {
        if(_breakPoints == null) {
            _breakPoints = new TotallyOrderedSet(new DoubleComparator());
        }
        Double bp = new Double(delay+getCurrentTime());   
        _breakPoints.insert(bp);
    }

    /** test if the current time is the stop time. 
     *  If so, return false ( for stop further simulaiton).
     *  @return false If the simulation time expires.
     *  @exception IllegalActionException If there is no ODE solver, or
     *        thrown by the solver.
     */
    public boolean postfire() throws IllegalActionException {
        if(Math.abs(getCurrentTime() - getStopTime()) < getTimeAccuracy()){
            return false;
        }
        return true;
    }

    /** produce outputs
     *  @exception IllegalActionException If the actor on the output
     *      path throws it.
     */
    public void produceOutput() throws IllegalActionException {
        CTScheduler scheduler = (CTScheduler) getScheduler();
        // Integrators emit output.
        // FIXME: Do we need this? If the last fire of the integrators
        //        has already emitted token, then the output actors
        //        can use them. That is at least true for implicit methods.
        Enumeration integrators = scheduler.dynamicActorSchedule();
        while(integrators.hasMoreElements()) {
            CTDynamicActor integrator=(CTDynamicActor)integrators.nextElement();
            if(VERBOSE) {
                System.out.println("Excite State..."+
                    ((Nameable)integrator).getName());
            }
            integrator.emitPotentialStates();
        }
        // outputSchdule.fire()
        Enumeration outputactors = scheduler.outputSchedule();
        while(outputactors.hasMoreElements()) {
            Actor nextoutputactor = (Actor)outputactors.nextElement();
            if(VERBOSE) {
                System.out.println("Fire output..."+
                    ((Nameable)nextoutputactor).getName());
            }
            nextoutputactor.fire();
        }
    }  

    /** update States
     */
     public void updateStates() throws IllegalActionException {
         CompositeActor container = (CompositeActor) getContainer();
         Enumeration allactors = container.deepGetEntities();
         while(allactors.hasMoreElements()) {
             Actor nextactor = (Actor)allactors.nextElement();
             nextactor.postfire();
         }
     }
    
    /** If parameter changed, queue the event
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
     */
    public void parameterRemoved(ParameterEvent e) {
        throw new InvalidStateException(this,
            "Critical Parameter deleted");
    }
    /** Update paramters.
     */
    public void updateParameters() throws IllegalActionException {
        if((_parameterEvents != null )&& (!_parameterEvents.isEmpty())) {
            if(DEBUG) {
                System.out.println("events = "+_parameterEvents.size());
            }
            Enumeration pevents = _parameterEvents.elements();
            while(pevents.hasMoreElements()) {
                ParameterEvent event = (ParameterEvent) pevents.nextElement();
                Parameter param = event.getParameter();
                if(param == _paramStopTime) {
                    if(VERBOSE) {
                        System.out.println("StopTime updating.");
                    }
                    _stopTime = ((DoubleToken)param.getToken()).doubleValue();
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
                } else if(param == _paramODESolver) {
                    if(VERBOSE) {
                        System.out.println("solver updating.");
                    }
                    _solverclass =((StringToken)param.getToken()).stringValue();
                    _defaultSolver = _instantiateODESolver(_solverclass);
                } else if(param == _paramLTETolerant) {
                    if(VERBOSE) {
                        System.out.println("LTE tolerant updating.");
                    }
                    _lteTolerant = 
                    ((DoubleToken)param.getToken()).doubleValue();
                } else if(param == _paramMinStepSize) {
                    if(VERBOSE) {
                        System.out.println("minstep updating.");
                    }
                    _minStepSize = 
                    ((DoubleToken)param.getToken()).doubleValue();
                }  else if(param == _paramValueAccuracy) {
                    _valueAccuracy = 
                    ((DoubleToken)param.getToken()).doubleValue();
                } else if(param == _paramTimeAccuracy) {
                    _timeAccuracy = 
                    ((DoubleToken)param.getToken()).doubleValue();
                } else if(param == _paramMaxIterations) {
                    _maxIterations = 
                    ((IntToken)param.getToken()).intValue();
                } else {
                    System.out.println("Unknowparameter"+param.getName());
                }
            }
            _parameterEvents.clear();
        }
    }

    /** set the currentODESolver
     */
    public void setCurrentODESolver(ODESolver solver)
            throws IllegalActionException {
        _currentSolver = solver;
    }   

    /** Set the current step size. This variable is very import during
     *  the simulation and can not be changed in the middle of an
     *  iteration.
     *  @param curstepsize The step size used for currentStepSize().
     */
    public void setCurrentStepSize(double curstepsize){
        _currentStepSize = curstepsize;
    }

    /** Set the current simulation time. All the actors directed by this
     *  director will share this global time. 
     *  @param tnow The current time.
     */
    public void setCurrentTime(double tnow){
        _currentTime = tnow;
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

    /** Add all the parameters.
     */
    protected void _initParameters() {
        try {
            _startTime = 0.0;
            _stopTime = 1.0;
            _initStepSize = 0.1;
            _minStepSize = 0.001;
            _maxIterations = 20;
            _lteTolerant = 1e-4;
            _valueAccuracy = 1e-6;
            _timeAccuracy = 1e-6;
            _solverclass = "ptolemy.domains.ct.kernel.solver.ForwardEulerSolver";

            _paramStartTime = new CTParameter(
                this, "StartTime", new DoubleToken(_startTime));
            _paramStopTime = new CTParameter(
                this, "StopTime", new DoubleToken(_stopTime));
            _paramInitStepSize = new CTParameter(
                this, "InitialStepSize", new DoubleToken(_initStepSize));
            _paramMinStepSize = new CTParameter(
                this, "MinimumStepSize", new DoubleToken(_minStepSize));
            _paramMaxIterations = new CTParameter(
                this, "MaximumIterationsPerStep", new IntToken(_maxIterations));
            _paramLTETolerant =  new CTParameter(
                this, "LocalTrancationErrorTolerant",
                new DoubleToken(_lteTolerant));
            _paramValueAccuracy =  new CTParameter(
                this, "ConvergeValueAccuracy", new DoubleToken(_valueAccuracy));
            _paramTimeAccuracy= new CTParameter(
                this, "TimeAccuracy", new DoubleToken(_timeAccuracy));
            _paramODESolver = new CTParameter(
                this, "ODESolver", new StringToken(_solverclass));
            //The director is registered as parameterlistener automatically. 

        } catch (IllegalActionException e) {
            //Should never happens. The parameters are always compatible.
            throw new InternalErrorException("Parameter creation error.");
        } catch (NameDuplicationException ex) {
            throw new InvalidStateException(this,"Parameter name duplication.");
        }
    }
  
    /** Instantiate ODESolver from its classname
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

    ////////////////////////////////////////////////////////////////////////
    ////                         private methods                        ////

    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    // current ODE solver.
    private ODESolver _currentSolver = null;
    private ODESolver _defaultSolver = null;

    // parameters.
    private CTParameter _paramStartTime;
    private CTParameter _paramStopTime;
    private CTParameter _paramInitStepSize;
    private CTParameter _paramMinStepSize;
    private CTParameter _paramMaxIterations;
    private CTParameter _paramLTETolerant;
    private CTParameter _paramValueAccuracy;
    private CTParameter _paramTimeAccuracy;
    private CTParameter _paramODESolver;
    
    //values
    private double _startTime;
    private double _stopTime;
    private double _initStepSize;
    private double _minStepSize;
    private int _maxIterations;
    private double _lteTolerant;
    private double _valueAccuracy;
    private double _timeAccuracy;
    private String _solverclass;

    private LinkedList _parameterEvents = null;
    
    // Simulation progress variables.
    private double _currentTime;
    private double _currentStepSize;

    //indicate the first round of execution.
    private boolean _first;

    //A table for wave form break points.
    private TotallyOrderedSet _breakPoints;
}
