/* A CT Director that handles the interaction with event based domains.

 Copyright (c) 1998-2010 The Regents of the University of California.
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
 */
package ptolemy.domains.ct.kernel;

import java.util.Iterator;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// CTMixedSignalDirector

/**
 <p>A CTDirector that supports the interaction of the continuous-time
 simulation with event-based domains. This director can both serve as
 a top-level director and an inside director that is contained by
 a composite actor in an event-based domain. If it is a top-level
 director, it behaves exactly like a CTMultiSolverDirector. If it is
 embedded in an event-based domain, it will run ahead of the global
 time and prepare to roll back if necessary.</p>

 <p> This director has an extra parameter compared to the
 CTMultiSolverDirector, the maximum run ahead of time length
 (<code>runAheadLength</code>).  Its default value is 1.0.</p>

 <p>The running ahead of time is achieved by the following mechanism.</p>

 <ul>
 <li> At the initialize stage of an execution, the director requests
 a firing at the global current time.</li>
 <li> At each prefire stage of the execution, the end time the the firing is
 computed based on the current time of the executive director, t1, the next
 iteration time of the executive director, t2, and the value of the parameter
 <code>runAheadLength</code>, t3. The fire end time is t1 + min(t2, t3)
 </li>
 <li> At the fire stage, the director will stop at the first of the
 following two times, the fire end time and the time of the first detected
 event.
 </li>
 </ul>

<p>At the prefire stage, the local current time is compared with the
 current time of the executive director. If the local time is later than
 the executive director time, then the directed system will rollback to a
 "known good" state. The "known good" state is the state of the system at
 the time when local time is equal to the current time of the executive
 director</p>.

 @deprecated As of July, 2011, the CTDirector no longer works with DEDirector.
 @author  Jie Liu, Haiyang Zheng
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Yellow (hyzheng)
 @Pt.AcceptedRating Red (hyzheng)
 */
public class CTMixedSignalDirector extends CTMultiSolverDirector {
    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *  All the parameters take their default values.
     *  @throws NameDuplicationException If construction of Time objects fails.
     *  @throws IllegalActionException If construction of Time objects fails.
     */
    public CTMixedSignalDirector() throws IllegalActionException, NameDuplicationException {
        super();
    }

    /** Construct a director in the workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  All the parameters take their default values.
     *  @param workspace The workspace of this object.
     *  @throws NameDuplicationException If construction of Time objects fails.
     *  @throws IllegalActionException If construction of Time objects fails.
     */
    public CTMixedSignalDirector(Workspace workspace) throws IllegalActionException, NameDuplicationException {
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
    public CTMixedSignalDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                           parameters                      ////

    /** Parameter of the run ahead length. The default value is 1.0.
     */
    public Parameter runAheadLength;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute. If the changed attribute matches
     *  a parameter of the director, then the corresponding private copy of the
     *  parameter value will be updated.
     *  @param attribute The changed attribute.
     *  @exception IllegalActionException If the runAhendLength does not have
     *  a valid token, or the superclass throws it.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == runAheadLength) {
            if (_debugging) {
                _debug("run ahead length updating.");
            }

            double value = ((DoubleToken) runAheadLength.getToken())
                    .doubleValue();

            if (value < 0) {
                throw new IllegalActionException(this,
                        " runAheadLength cannot be negative.");
            }

            _runAheadLength = value;
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Return true indicating that this director can be an inside director.
     *  @return True always.
     */
    public boolean canBeInsideDirector() {
        return true;
    }

    /** Record the current model time as a known good time for roll back
     *  and call the super.fire() method.
     *  @exception IllegalActionException If thrown by the super class.
     */
    public void fire() throws IllegalActionException {
        _knownGoodTime = getModelTime();
        super.fire();
    }

    /** Initialize the execution. If this director is not at the top level,
     *  ask the executive director to fire the container of this director
     *  at the current model time.
     *  @see CTMultiSolverDirector#initialize()
     *  @exception IllegalActionException If thrown by the initialize method
     *   of super class, or the quest for refiring can not be accepted, or if the director does not
     *   agree to fire the actor at the specified time.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _mutationVersion = -1;
        fireContainerAt(getModelTime());
    }

    /** If this is not a top-level director, request a refiring at the current
     *  model time. Otherwise, behave exactly as a CTMultiSolverDirector.
     *  @return True if the simulation is not finished.
     *  @exception IllegalActionException If thrown in the postfire method
     *   in the super class or the refiring can not be granted, or if the director does not
     *   agree to fire the actor at the specified time.
     */
    public boolean postfire() throws IllegalActionException {
        if (!_isTopLevel()) {
            // If this director is not at the top level, then the enclosing
            // director's notion of current time is that of the start time
            // of the integration period.  This director has locally set
            // time to be t + h, where h is the step size.
            // To ensure that the enclosing director fires this composite
            // again at t + h, we insert t + h into the breakpoint table.
            // This will cause the superclass (in its postfire() method)
            // to call fireAt() on the enclosing (executive) director.
            // Note that we do not want to directly call the superclass
            // fireAt() here.
            fireAt((CompositeActor) getContainer(), getModelTime());
        }

        return super.postfire();
    }

    /** Always returns true, indicating that the (sub)system is always ready
     *  for one iteration.
     *  <p>
     *  If this is not a top-level director, some additional work is done
     *  to synchronize time with the executive director. In particular,
     *  it will compare its local time, say t, with the current time
     *  of the executive director, say t0.
     *  <br>If t == t0, do nothing. </br>
     *  <br>If t > t0, then rollback to the "known good" time (which should be
     *  less than the outside time). </br>
     *  <br>If t < t0, then throw an exception because the CT subsystem
     *  should always run ahead of the outside event-based system. </br>
     *  </p><p>
     *  If this director is not a top-level director, the iteration end time is
     *  resolved from the current time of the outside system, say t1,
     *  the next iteration time of the outside system, say t2, and
     *  the runAheadLength parameter of this director, say t3.
     *  The iteration end time is set to be <code>t5 = t1 + min(t2, t3)</code>.
     *  The iteration end time may be further refined in the fire() method
     *  due to possible event generated during the iteration.
     *  In particular, when the first event is detected, say at t5 and t5 < t4,
     *  then the iteration ends at t5.
     *  </p><p>
     *  This method updates the suggested step size.</p>
     *
     *  @return true Always.
     *  @exception IllegalActionException If the local time is
     *       less than the current time of the executive director,
     *       or thrown by a directed actor.
     */
    public boolean prefire() throws IllegalActionException {
        if (_debugging) {
            _debug("\n", getName(), " prefire: ");
        }

        if (!_isTopLevel()) {
            // synchronize the local time with the outside time.
            CompositeActor container = (CompositeActor) getContainer();
            Director executiveDirector = container.getExecutiveDirector();
            _outsideTime = executiveDirector.getModelTime();

            Time localTime = getModelTime();
            // As an optimization, we try to keep the step size
            // bounded by the time to the next event in the
            // enclosing model.
            Time outsideNextIterationTime = executiveDirector
                    .getModelNextIterationTime();

            if (_debugging) {
                _debug("The current time of outside model is " + _outsideTime,
                        " and its next iteration time is "
                                + outsideNextIterationTime,
                        "\nThe current time of this director is " + localTime);
            }

            // Now, check the next iteration time.
            if (outsideNextIterationTime.compareTo(_outsideTime) < 0) {
                // NOTE: This check is redundant. The outside director should
                // guarantee that this never happen.
                throw new IllegalActionException(this, "Outside domain"
                        + " time is going backward."
                        + " Current outside time = " + _outsideTime
                        + ", but the next iteration time = "
                        + outsideNextIterationTime);
            }

            double aheadLength = _runAheadLength;

            // Ideally, the outside time should equal the local time.
            // If the outside time is less than the local time, then rollback
            // is needed. If the outside time is greater than the local time,
            // an exception will be thrown.
            if (_outsideTime.compareTo(localTime) > 0) {
                throw new IllegalActionException(this, executiveDirector,
                        "Outside time is later than the local time. "
                                + "This should never happen.");
            } else if (_outsideTime.compareTo(localTime) < 0) {
                // Outside time less than the local time. Rollback!
                // NOTE: This can happen, for example, if the CT model is
                // inside a DE model, and it has advanced its time too
                // far into the future. For example, if it was previously
                // fired at time 0.0 and it advanced its local time
                // to 0.1, but later it gets fired again at time 0.05.
                // In that case, it has to restart the integration
                // from time 0.0 and ensure that it doesn't progress
                // its local time past 0.05.
                // An example is to have two interacting CT subsystems
                // embedded inside a DE model, where one system (called A)
                // produces an event at time 0.1 and the other one (called B)
                // produces an event at time 0.05. A may integrate with a step
                // size of 0.1 but it has to roll back and use a step size 0.05
                // such that the event produced by B can be handled.
                if (_debugging) {
                    _debug(getName() + " rollback from: " + localTime + " to: "
                            + _knownGoodTime + "due to outside time "
                            + _outsideTime);
                }

                // The local time is set backwards to a known good time.
                _rollback();

                aheadLength = _outsideTime.subtract(getModelTime())
                        .getDoubleValue();

            } else {
                aheadLength = outsideNextIterationTime.subtract(_outsideTime)
                        .getDoubleValue();
            }

            if (_debugging) {
                _debug(getName(), " local time = " + localTime,
                        " Outside Time = " + _outsideTime,
                        " NextIterationTime = " + outsideNextIterationTime
                                + " Inferred run length = " + aheadLength);
            }

            if (aheadLength < getTimeResolution()) {
                // This is a zero step size iteration.
                // TESTIT: simultaneous events from the outside model drives
                // a CT subsystem.
                if (_debugging) {
                    _debug("This is an iteration with the step size as 0.");
                }
                aheadLength = 0;
            } else if (aheadLength > _runAheadLength) {
                aheadLength = _runAheadLength;
            }

            double currentSuggestedNextStepSize = getSuggestedNextStepSize();
            if (aheadLength < currentSuggestedNextStepSize
                    || currentSuggestedNextStepSize == 0) {
                setSuggestedNextStepSize(aheadLength);
            }

            // Now it is safe to execute the continuous part.
            if (_debugging) {
                _debug(getName(), "The suggested step size is set to "
                        + getSuggestedNextStepSize());
            }

            // set the start time of the current iteration
            // The begin time of an iteration can be changed only by directors.
            // On the other hand, the model time may be changed by ODE solvers.
            // The reason is that when the CurrentTime actor is involved in a
            // multi-step integration, it needs to report the current time at
            // the intermediate steps. The CurrentTime actor reports the model
            // time.
            _setIterationBeginTime(getModelTime());

            return true;
        } else {
            return super.prefire();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Initialize parameters in addition to the parameters inherited
     *  from CTMultiSolverDirector. In this class the additional
     *  parameter is the maximum run ahead time length
     *  (<code>runAheadLength</code>). The default value is 1.0.
     */
    protected void _initParameters() {
        super._initParameters();

        try {
            _runAheadLength = 0.1;
            runAheadLength = new Parameter(this, "runAheadLength",
                    new DoubleToken(_runAheadLength));
            runAheadLength.setTypeEquals(BaseType.DOUBLE);
        } catch (IllegalActionException e) {
            //Should never happens. The parameters are always compatible.
            throw new InternalErrorException("Parameter creation error.");
        } catch (NameDuplicationException ex) {
            throw new InvalidStateException(this, "Parameter name duplication.");
        }
    }

    /**Return true if this is a top-level director.
     * @return True if this director is at the top level.
     */
    protected final boolean _isTopLevel() {
        // This is a syntactic sugar.
        long version = workspace().getVersion();

        if (version == _mutationVersion) {
            return _isTop;
        }

        try {
            workspace().getReadAccess();

	    NamedObj couldBeEntityLibrary = getContainer();
	    if (!(couldBeEntityLibrary instanceof CompositeActor)) {
		// If we expand the configuration, then the container
		// could be an EntityLibrary.  To replicate this,
		// in vergil, open up Directors -> Classic Directors.
		_isTop = false;
	    } else {
		CompositeActor container = (CompositeActor) couldBeEntityLibrary;
		if (container.getExecutiveDirector() == null) {
		    _isTop = true;
		} else {
		    _isTop = false;
		}
	    }
            _mutationVersion = version;
        } finally {
            workspace().doneReading();
        }

        return _isTop;
    }

    /** Rollback the system to a "known good" state. All the actors with
     *  states are called to restore their saved states. The
     *  current time of the director is set to the time of the "known
     *  good" state.
     *  @exception IllegalActionException If thrown by the goToMarkedState()
     *  method of an actor, or the schedule does not exist.
     */
    protected void _rollback() throws IllegalActionException {
        localClock.setLocalTime(_knownGoodTime);

        CTSchedule schedule = (CTSchedule) getScheduler().getSchedule();
        Iterator actors = schedule.get(CTSchedule.STATEFUL_ACTORS)
                .actorIterator();

        while (actors.hasNext()) {
            CTStatefulActor actor = (CTStatefulActor) actors.next();

            if (_debugging) {
                _debug("Restore State..." + ((Nameable) actor).getName());
            }

            actor.goToMarkedState();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Indicate if this is the top level director.
    private boolean _isTop;

    // The time for the "known good" state.
    private Time _knownGoodTime;

    // The version of mutation. If this version is not the workspace
    // version then every thing related to mutation need to be updated.
    private long _mutationVersion = -1;

    // The current outside time.
    private Time _outsideTime;

    // The local variable of the run ahead length parameter.
    private double _runAheadLength;
}
