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

@ProposedRating red (liuj@eecs.berkeley.edu)
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
This class maintains most parameters that are share by all ODE solvers.
It maintains a break point table to record all the break points.
@author Jie Liu
@version $Id$
@see ptolemy.actor.Director
*/
public abstract class CTDirector extends StaticSchedulingDirector
        implements ParameterListener{

    public static final boolean VERBOSE = true;
    public static final boolean DEBUG = true;
    public static final boolean STAT = true;
    public static int NSTEP = 0;
    public static int NFUNC = 0;
    public static int NFAIL = 0;
    public static int NROLL = 0;


    /** Construct a CTDirector with no name and no Container.
     *  The default startTime and stopTime are all zeros. There's no
     *  scheduler associated.
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
     *  The default startTime and stopTime are all zeros. There's no
     *  scheduler associated.
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
     *  The default startTime and stopTime are all zeros. There's no
     *  scheduler associated.
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

    /** Return the suggested next step size.
     *  @return The suggested next step size.
     */
    public final double getSuggestedNextStepSize() {
        return _suggestedNextStepSize;
    }

    /** Return the time accuracy such that two time stamp within this
     *  accuracy is considered identical.
     *  @return The time accuracy.
     */
    public final double getTimeAccuracy() {
        return _timeAccuracy;
    }

    /** Return the local trancation error tolerant, used for
     *  adjustable step size solvers.
     *  @return The local trancation error tolerant.
     */
    public final double getLTETolerant() {
        return _lteTolerant;
    }

    /** Return the value accuracy, used for test if implicit method
     *  has reached the fixed point. Two values differ less than
     *  this accuracy is considered identical in fixed point
     *  calculation.
     *
     *  @return The local trancation error tolerant.
     */
    public final double getValueAccuracy() {
        return _valueAccuracy;
    }

    /** Return the minimum step size used in variable step size
     *  ODE solvers.
     */
    public final double getMinStepSize() {
        return _minStepSize;
    }

    /** Return the maximum number of iterations in fixed point
     *  calculation. If the iteration has exceed this number
     *  and the fixed point is still not found, then the algorithm
     *  is considered failed.
     */
    public final int getMaxIterations() {
        return _maxIterations;
    }

    /** Return a CTReceiver.
     */
    public Receiver newReceiver() {
        return new CTReceiver();
    }

    /** Register a break point in the future. This request the
     *  Director to fire exactly at the each registered time.
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

    /** abstract class for update all parameters.
     *  @exception IllegalActionException If throw by creation of some
     *       parameters.
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
    /** Update paramters.
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
        } else if(param == _paramLTETolerant) {
            if(VERBOSE) {
                System.out.println("LTE tolerant updating.");
            }
            _lteTolerant = ((DoubleToken)param.getToken()).doubleValue();
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
            if (VERBOSE) {
                System.out.println("Unknowparameter: "+param.getName());
            }
            throw new IllegalActionException(this, param,
                " Unknown parameter.");
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

    /** Set stopTime. The stop time will be registered as a break point.
     */
    public void setStopTime(double tstop) {
        _stopTime = tstop;
    }

    /** Set startTime. The start time will NOT be registered as a
     *  break point so the user should do it explicitly if needed.
     */
    public void setStartTime(double tstart) {
        _stopTime = tstart;
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
     */
    protected LinkedList _getParameterEvents() {
        return _parameterEvents;
    }

    /** Add all the parameters.
     */
    private void _initParameters() {
        try {
            _startTime = 0.0;
            _stopTime = 1.0;
            _initStepSize = 0.1;
            _minStepSize = 0.001;
            _maxIterations = 20;
            _lteTolerant = 1e-4;
            _valueAccuracy = 1e-6;
            _timeAccuracy = 1e-6;


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

   /** Indicate whether this director would like to have write access
     *  during its iteration. By default, the return value is true, indicating
     *  the need for a write access.
     *
     *  @return True if this director need write access, false otherwise.
     */
    protected boolean _writeAccessPreference() {
        return false;
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
    private CTParameter _paramLTETolerant;
    private CTParameter _paramValueAccuracy;
    private CTParameter _paramTimeAccuracy;


    //values
    private double _startTime;
    private double _stopTime;
    private double _initStepSize;
    private double _minStepSize;
    private int _maxIterations;
    private double _lteTolerant;
    private double _valueAccuracy;
    private double _timeAccuracy;


    private LinkedList _parameterEvents = null;

    // Simulation progress variables.
    private double _currentTime;
    private double _currentStepSize;
    private double _suggestedNextStepSize;

    //A table for wave form break points.
    private TotallyOrderedSet _breakPoints;

}
