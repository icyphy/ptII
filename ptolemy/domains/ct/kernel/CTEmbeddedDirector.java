/* An embedded director for CT inside CT/FSM.

 Copyright (c) 1999-2003 The Regents of the University of California.
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
@AcceptedRating Yellow (chf@eecs.berkeley.edu)
*/

package ptolemy.domains.ct.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.domains.ct.kernel.util.TotallyOrderedSet;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Workspace;

import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// CTEmbeddedDirector
/**
An embedded director for CT inside CT/FSM. Conceptually, this director
interacts with a continuous outer domain. As a consequence, this
director exposes its step size control information. If the container
of this director is a CTCompositeActor, then this information is
further exposed to the outer domain.
<P>
Unlike the CTMixedSignalDirector, this director does not run ahead
of the global time and rollback, simply because the step size control
information is accessible from outer domain which has a continuous
time and understands the meaning of step size.

@author  Jie Liu
@version $Id$
@since Ptolemy II 0.2
@see CTMultiSolverDirector
@see CTTransparentDirector
*/
public class CTEmbeddedDirector extends CTMultiSolverDirector
    implements CTTransparentDirector {
    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *  All the parameters take their default values.
     */
    public CTEmbeddedDirector() {
        super();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  All the parameters take their default values.
     *  @param workspace The workspace of this object.
     */
    public CTEmbeddedDirector(Workspace workspace)  {
        super(workspace);
    }

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  All the parameters take their default values.
     *  @param container The container.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *   with the specified container.  May be thrown in a derived class.
     *  @exception NameDuplicationException If the container is not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public CTEmbeddedDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        //addDebugListener(new StreamListener());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Always return true indicating that this director can be
     *  an inside director.
     *  @return True always.
     */
    public boolean canBeInsideDirector() {
        return true;
    }

    /** Always return false indicating that this director cannot
     *  be a top level director.
     *  @return False always.
     */
    public boolean canBeTopLevelDirector() {
        return false;
    }

    /** Execute the subsystem for one iteration. An iteration includes
     *  an event phase, a state resolving phase, and a output phase.
     *  If the state cannot be accurately resolved, then subsystem
     *  may produce a meaningless output. It is the outer domain's
     *  responsibility to check if this subsystem is accurate in
     *  this iteration, by calling the isThisStepAccurate() method
     *  of the CTCompositeActor that contains this director.
     *  @exception IllegalActionException If one of the actors throw
     *  it during one iteration.
     */
    public void fire() throws IllegalActionException {
        // Get the inputs if there are any
        CompositeActor container = (CompositeActor)getContainer();
        Director exe = container.getExecutiveDirector();
        _outsideTime = exe.getCurrentTime();
        if (_debugging) _debug("Outside Time: "+ _outsideTime);
        double nextIterationTime = exe.getNextIterationTime();
        if (_debugging) _debug("Next Iteration Time: " + nextIterationTime);
        setCurrentTime(getIterationBeginTime());
        _outsideStepSize = nextIterationTime - getIterationBeginTime();

        if (_outsideStepSize == 0) {
            if (_debugging) {
                _debug("Outside step size is 0 so treat this as a breakpoint.");
            }
            // it must be a breakpoint now.
            _setCurrentODESolver(getBreakpointSolver());
            _setBreakpointIteration(true);
            setCurrentStepSize(getMinStepSize());
        } else {
            _setCurrentODESolver(getODESolver());
            _setBreakpointIteration(false);
            setCurrentStepSize(_outsideStepSize);
        }
        // if break point now, change solver.
        Double now = new Double(_outsideTime);
        TotallyOrderedSet breakPoints = getBreakPoints();
        if (breakPoints != null && !breakPoints.isEmpty()) {
            breakPoints.removeAllLessThan(now);
            if (breakPoints.contains(now)) {
                if (_debugging)
                    _debug(getName(),
                            ": Break point now at" + _outsideTime);
                // Breakpoints iterations are always successful
                // so remove the breakpoints.
                breakPoints.removeFirst();
                // does not adjust step size,
                // since the exe-dir should do it.
                _setCurrentODESolver(getBreakpointSolver());
                _setBreakpointIteration(true);
                setCurrentStepSize(getMinStepSize());
            }
        }

        if (_debugging) {
            _debug("Step size is "
                    + getCurrentStepSize()
                    + " at "
                    + getCurrentTime()
                    + "; breakpoint table contains: "
                    + getBreakPoints().toString());
        }

        _setDiscretePhase(true);
        Iterator waveGenerators = getScheduler().getSchedule().get(
                CTSchedule.WAVEFORM_GENERATORS).actorIterator();
        while (waveGenerators.hasNext() && !_stopRequested) {
            CTWaveformGenerator generator =
                (CTWaveformGenerator) waveGenerators.next();
            if (!isPrefireComplete(generator)) {
                setPrefireComplete(generator);
                if (_debugging) {
                    _debug("Prefire generator actor: "
                            + ((Nameable)generator).getName()
                            + " at time "
                            + getCurrentTime());
                }
                if (!generator.prefire()) {
                    throw new IllegalActionException((Nameable)generator,
                            "Actor is not ready to fire. In the CT domain, "
                            + "all generator actors should be ready to fire "
                            + "at all times.\n"
                            + "Does the actor only operate on sequence "
                            + "of tokens?");
                }
            }
            if (_debugging) {
                _debug("Fire generator actor: "
                        + ((Nameable)generator).getName()
                        + " at time "
                        + getCurrentTime());
            }
            generator.fire();
        }
        _setDiscretePhase(false);
        // continuous phase;
        if (_debugging) _debug("Execute the system from "+
                getCurrentTime() + " with step size " + getCurrentStepSize()
                + " using solver " + getCurrentODESolver().getName());
        // NOTE: Used to prefire() all actors before any firing.
        prefireClear();
        _prefireDynamicActors();
        ODESolver solver = getCurrentODESolver();
        if (!solver.resolveStates()) {
            _stateAcceptable = false;
            if (_debugging) _debug("Resolve states failed.");
        }

        if (_debugging) {
            _debug("Current time after"
                    + " solver.resolveStates() is "
                    + getCurrentTime());
        }
        produceOutput();
        _discretePhaseExecution();
    }

    /** Register the break point to this director and to the executive
     *  director.
     *  @param actor The actor that requested the fire
     *  @param time The fire time
     *  @exception IllegalActionException If the time is before
     *  the current time, or there is no executive director.
     */
    public void fireAt(Actor actor, double time)
            throws IllegalActionException{
        super.fireAt(actor, time);
        CompositeActor container = (CompositeActor)getContainer();
        Director exeDirector = container.getExecutiveDirector();
        exeDirector.fireAt(container, time);
    }

    /** Set the current time to be the current time of the outside model.
     *  Both the current time and the stop time are registered
     *  as a breakpoint.
     *  It invokes the initialize() method for all the Actors in the
     *  container.
     *
     *  @exception IllegalActionException If the instantiation of the solvers
     *  does not succeed or one of the directed actors throws it.
     */
    public void initialize() throws IllegalActionException {
        CompositeActor container = (CompositeActor)getContainer();
        Director exe = container.getExecutiveDirector();
        setCurrentTime(exe.getCurrentTime());
        _setIterationBeginTime(getCurrentTime());
        super.initialize();
    }

    /** Return true if the current integration step
     *  is accurate. This is determined by asking all the
     *  step size control actors in the state transition schedule and
     *  output schedule.
     *  @return True if the current step is accurate.
     */
    public boolean isThisStepAccurate() {
        try {
            _debug(getName() + ": Checking local actors for success.");
            if (!_isStateAccurate()) {
                //if (_debugging) _debug(getFullName() +
                //        " current step not successful because of STATE.");
                _stateAcceptable = false;
                return false;
            } else if (!_isOutputAccurate()) {
                //if (_debugging) _debug(getFullName() +
                //        " current step not successful because of OUTPUT.");
                _stateAcceptable = true;
                _outputAcceptable = false;
                return false;
            } else {
                _stateAcceptable = true;
                _outputAcceptable = true;
                return true;
            }
        } catch (IllegalActionException ex) {
            throw new InternalErrorException (
                    "Nothing to schedule with out make schedule invalid." +
                    ex.getMessage());
        }
    }

    /** Update the states of actors directed by this director.
     *  Discrete events at current time will be consumed and produced.
     *  @return True if this is not a top-level director, or the simulation
     *     is not finished and stop() has not been called.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public boolean postfire() throws IllegalActionException {
        if (_debugging) _debug(getFullName(), " postfire.");
        // FIXME: this postfire method produces outputs
        //_discretePhaseExecution();
        updateContinuousStates();
        // The current time will be the begin time of the next iteration.
        _setIterationBeginTime(getCurrentTime());
        return !_stopRequested;
    }

    /** Return the predicted next step size, which is the minimum
     *  of the prediction from step size control actors.
     *  @return The predicted step size from this subsystem.
     */
    public double predictedStepSize() {
        try {
            if (_debugging) _debug(getName(), "at " + getCurrentTime(),
                    " predict next step size" + _predictNextStepSize());
            return _predictNextStepSize();
        } catch (IllegalActionException ex) {
            throw new InternalErrorException (
                    " Fail predict the next step size." + ex.getMessage());
        }
    }

    /** Return true always. Recompute the schedules if there
     *  was a mutation. Synchronize time with the outer domain,
     *  and adjust the contents of the breakpoint table with
     *  respect to the current time.
     *  @return True always.
     */
    public boolean prefire() throws IllegalActionException {
        if (_debugging) _debug("Director prefire.");
        if (!isScheduleValid()) {
            // mutation occurred, redo the schedule;
            CTScheduler scheduler = (CTScheduler)getScheduler();
            if (scheduler == null) {
                throw new IllegalActionException (this,
                        "does not have a Scheduler.");
            }
            scheduler.getSchedule();
            setScheduleValid(true);
        }
        return true;
    }

    /** Check whether the container implements the CTStepSizeControlActor
     *  interface. If not, then throw an exception.
     *  @exception IllegalActionException If the container of this
     *  director does not implement CTStepSizeControlActor, or one of
     *  the actors throws it.
     */
    public void preinitialize() throws IllegalActionException {
        if (!(getContainer() instanceof CTStepSizeControlActor)) {
            throw new IllegalActionException(this, "can only be contained by "
                    + "a composite actor that implements "
                    + "the CTStepSizeControlActor "
                    + "interface, for example, the continuous "
                    + "time composite actor or the modal model.");
        }
        super.preinitialize();
    }

    /** Return the refined step size if the current fire is not accurate.
     *  @return The refined step size.
     */
    public double refinedStepSize() {
        try {
            if (!_stateAcceptable) {
                return _refinedStepWRTState();
            } else if (!_outputAcceptable){
                return _refinedStepWRTOutput();
            } else {
                return Double.MAX_VALUE;
            }
        } catch( IllegalActionException ex) {
            throw new InternalErrorException (
                    "Fail to refine step size. " + ex.getMessage());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Step size of the outer domain.
    private double _outsideStepSize;

    // Indicates whether actors in the output schedule are satisfied.
    private boolean _outputAcceptable;

    // Indicates whether actors in the dynamic actor schedule and the
    // state transition schedule are satisfied.
    private boolean _stateAcceptable;

    // The current time of the outer domain.
    private double _outsideTime;
}

