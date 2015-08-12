/* A director that uses a static schedule.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.actor.sched;

import java.util.Iterator;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.FiringEvent;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.DebugListener;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// StaticSchedulingDirector

/**
 A director that uses static scheduling to govern the execution of the
 CompositeActor it belongs to. <p>

 This class does not directly implement a scheduling algorithm, but
 defers to its contained scheduler.  The contained scheduler creates an
 instance of the Schedule class which determines the number of times
 each actor should be fired and their firing order.  This allows new
 scheduling algorithms to be easily created for existing domains.<p>

 This class is generally useful for statically scheduled domains where
 a schedule can be constructed once and used to repeatedly execute the
 model.  The Scheduler class caches the schedule until the model changes
 so that the schedule does not have to be recomputed.

 @author Jie Liu, Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Green (neuendor)
 @Pt.AcceptedRating Yellow (neuendor)
 @see ptolemy.actor.Director
 @see Scheduler
 @see Schedule
 */
public class StaticSchedulingDirector extends Director {
    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *  @exception NameDuplicationException If construction of Time objects fails.
     *  @exception IllegalActionException If construction of Time objects fails.
     */
    public StaticSchedulingDirector() throws IllegalActionException,
    NameDuplicationException {
        super();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace of this object.
     *  @exception NameDuplicationException If construction of Time objects fails.
     *  @exception IllegalActionException If construction of Time objects fails.
     */
    public StaticSchedulingDirector(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
    }

    /** Construct a director in the given container with the given name.
     *  If the container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *
     *  @param container The container of this director.
     *  @param name Name of this director.
     *  @exception IllegalActionException Not thrown in this base class.
     *   May be thrown in the derived classes if the director
     *  is not compatible with the specified container.
     *  @exception NameDuplicationException If the name collides with
     *  an attribute that already exists in the given container.
     */
    public StaticSchedulingDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to also listen to the scheduler, if there
     *  is one.
     *  @param listener The listener to which to send debug messages.
     *  @see #removeDebugListener(DebugListener)
     */
    @Override
    public synchronized void addDebugListener(DebugListener listener) {
        super.addDebugListener(listener);

        Scheduler scheduler = getScheduler();

        if (scheduler != null) {
            scheduler.addDebugListener(listener);
        }
    }

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown in this base class
     *  @return The new Attribute.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        StaticSchedulingDirector newObject = (StaticSchedulingDirector) super
                .clone(workspace);
        Scheduler scheduler = getScheduler();
        if (scheduler == null) {
            newObject._setScheduler(null);
        } else {
            newObject._setScheduler((Scheduler) newObject
                    .getAttribute(getScheduler().getName()));
        }
        return newObject;
    }

    /** Initialize local variables.
     *  @exception IllegalActionException Thrown by super class.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        _savedSchedule = null;
        _savedSchedulePosition = -1;
        _savedIterationCount = 0;

        _actorFinished = false;
    };

    /** Calculate the current schedule, if necessary, and iterate the
     *  contained actors in the order given by the schedule.
     *  Iterating an actor involves calling the actor's iterate() method,
     *  which is equivalent to calling the actor's prefire(), fire() and
     *  postfire() methods in succession.  If iterate() returns NOT_READY,
     *  indicating that the actor is not ready to execute, then an
     *  IllegalActionException will be thrown. The values returned from
     *  iterate() are recorded and are used to determine the value that
     *  postfire() will return at the end of the director's iteration.
     *  NOTE: This method does not conform with the strict actor semantics
     *  because it calls postfire() of actors. Thus, it should not be used
     *  in domains that require a strict actor semantics, such as SR or
     *  Continuous.
     *  @exception IllegalActionException If any actor executed by this
     *   actor return false in prefire.
     *  @exception InvalidStateException If this director does not have a
     *   container.
     */
    @Override
    public void fire() throws IllegalActionException {
        // Don't call "super.fire();" here because if you do then
        // everything happens twice.
        Iterator firings = null;

        Scheduler scheduler = getScheduler();

        if (scheduler == null) {
            throw new IllegalActionException("Attempted to fire "
                    + "system with no scheduler");
        }

        // This will throw IllegalActionException if this director
        // does not have a container.
        Schedule schedule = scheduler.getSchedule();
        firings = schedule.firingIterator();

        Firing firing = null;
        while (firings.hasNext() && !_stopRequested) {
            firing = (Firing) firings.next();
            Actor actor = firing.getActor();

            int iterationCount = firing.getIterationCount();

            if (_debugging) {
                _debug(new FiringEvent(this, actor, FiringEvent.BEFORE_ITERATE,
                        iterationCount));
            }

            int returnValue = actor.iterate(iterationCount);

            if (returnValue == STOP_ITERATING) {
                _postfireReturns = false;
                if (_debugging) {
                    _debug("Actor requests no more firings: "
                            + actor.getFullName());
                }
            } else if (returnValue == NOT_READY) {
                // See de/test/auto/knownFailedTests/DESDFClockTest.xml
                throw new IllegalActionException(this, actor, "Actor "
                        + "is not ready to fire.  Perhaps " + actor.getName()
                        + ".prefire() returned false? "
                        + "Try debugging the actor by selecting "
                        + "\"Listen to Actor\".  Also, for SDF check moml for "
                        + "tokenConsumptionRate on input.");
            }

            if (_debugging) {
                _debug(new FiringEvent(this, actor, FiringEvent.AFTER_ITERATE,
                        iterationCount));
            }
        }
    }

    /** Return the scheduler that is responsible for scheduling the
     *  directed actors.  This method is read-synchronized on the
     *  workspace.
     *
     *  @return The contained scheduler.
     *  @see #setScheduler(Scheduler)
     */
    public Scheduler getScheduler() {
        try {
            workspace().getReadAccess();
            return _scheduler;
        } finally {
            workspace().doneReading();
        }
    }

    /** Indicate that a schedule for the model may no longer be valid.
     *  This method should be called when topology changes are made,
     *  or for that matter when any change that may invalidate the
     *  schedule is made.  In this base class, this method sets a flag
     *  that forces scheduling to be redone at the next opportunity.
     *  If there is no scheduler, do nothing.
     */
    @Override
    public void invalidateSchedule() {
        _debug("Invalidating schedule.");
        if (_scheduler != null) {
            _scheduler.setValid(false);
        }
    }

    /** Return true if the current (cached) schedule is valid.
     *  This calls the valid() method of Scheduler.
     *  @return true if the schedule is valid.
     *  @exception IllegalActionException If there's no scheduler.
     */
    public boolean isScheduleValid() throws IllegalActionException {
        if (_scheduler == null) {
            throw new IllegalActionException(this, "has no scheduler.");
        }

        return _scheduler.isValid();
    }

    /** Return true if the director wishes to be scheduled for another
     *  iteration. This base class returns true if all of the actors
     *  iterated since the last call to prefire returned true from their
     *  postfire() method and if stop() has not been called. Subclasses
     *  may override this method to perform additional
     *  domain-specific behavior.
     *  @return True if the Director wants to be fired again in the
     *  future.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        boolean result = super.postfire() && _postfireReturns;
        if (_debugging) {
            _debug("Postfire returns: " + result);
        }
        return result;
    }

    /** Resume the execution of an actor that was previously blocked because
     *  it didn't have all the resources it needed for execution.
     *  @param actor The actor that resumes execution.
     *  @exception IllegalActionException Not thrown here but in derived classes.
     */
    @Override
    public void resumeActor(NamedObj actor) throws IllegalActionException {
        _actorFinished = true;
    }

    /** Return true if the director is ready to fire. This method is
     *  called by the container of this director to determine whether
     *  the director is ready to execute. It does <i>not</i> call
     *  prefire() on the contained actors.  If this director is not at
     *  the top level of the hierarchy, and the current time of the
     *  enclosing model is greater than the current time of this
     *  director, then this base class updates current time to match
     *  that of the enclosing model.  <p>
     *
     *  In this base class, assume that the director is always ready
     *  to be fired, and so return true.  Domain directors should
     *  probably override this method to provide domain-specific
     *  operation.  However, they should call super.prefire() if they
     *  wish to propagate time as done here.
     *
     *  @return True.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        _postfireReturns = true;
        _prefire = super.prefire();
        if (_aspectsPresent && _prefire) {

            Iterator firings = null;
            if (_savedSchedule == null) {
                Scheduler scheduler = getScheduler();
                Schedule schedule = scheduler.getSchedule();
                _savedSchedule = schedule;
                _savedSchedulePosition = 0;
                firings = schedule.firingIterator();
            } else {
                firings = _savedSchedule.firingIterator();
                for (int i = 0; i < _savedSchedulePosition; i++) {
                    firings.next();
                }
            }

            Firing firing = null;
            while ((_savedIterationCount > 0 || firings.hasNext())
                    && !_stopRequested) {

                if (firing == null || _savedIterationCount == 0) {
                    firing = (Firing) firings.next();
                }
                Actor actor = firing.getActor();

                if (!_actorFinished) {
                    if (_tokenSentToCommunicationAspect) {
                        _tokenSentToCommunicationAspect = false;
                        if (((CompositeActor) getContainer()).getContainer() != null) {
                            ((CompositeActor) getContainer())
                                    .getExecutiveDirector().fireAtCurrentTime(
                                            (CompositeActor) getContainer());
                        }
                        _prefire = false;
                        return false;
                    }
                    boolean finished = _schedule((NamedObj) actor,
                            getModelTime());
                    if (!finished) {
                        _prefire = false;
                        return false;
                    }
                }
                _actorFinished = false;

                if (_savedIterationCount == 0) {
                    _savedIterationCount = firing.getIterationCount();
                }

                _savedIterationCount--;
                if (_savedIterationCount == 0) {
                    _savedSchedulePosition++;
                }

            }
            if (_savedSchedule.size() <= _savedSchedulePosition) {
                _savedSchedule = null;
            }
        }
        return _prefire;
    }

    /** Override the base class to also remove the listener from the scheduler,
     *  if there is one.
     *  @param listener The listener to remove from the list of listeners
     *   to which debug messages are sent.
     *  @see #addDebugListener(DebugListener)
     */
    @Override
    public synchronized void removeDebugListener(DebugListener listener) {
        super.removeDebugListener(listener);

        Scheduler scheduler = getScheduler();

        if (scheduler != null) {
            scheduler.removeDebugListener(listener);
        }
    }

    /** Set the scheduler for this StaticSchedulingDirector.
     *  The container of the specified scheduler is set to this director.
     *  If there was a previous scheduler, the container of that scheduler
     *  is set to null. This method is write-synchronized on the workspace.
     *  If the scheduler is not compatible with the director, an
     *  IllegalActionException is thrown.
     *  @param scheduler The scheduler that this director will use.
     *  @exception IllegalActionException Not thrown in this base class,
     *   but derived classes may throw it if the scheduler is not compatible.
     *  @exception NameDuplicationException Not thrown in this base class,
     *   but derived classes may throw it if the scheduler is not compatible.
     *  @see #getScheduler()
     */
    public void setScheduler(Scheduler scheduler)
            throws IllegalActionException, NameDuplicationException {
        if (scheduler != null) {
            scheduler.setContainer(this);
        } else {
            if (_scheduler != null) {
                _scheduler.setContainer(null);
            }
        }
        _setScheduler(scheduler);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Set the local scheduler for execution of this Director.
     *  This should not be called be directly.  Instead, call setContainer()
     *  on the scheduler.  This method removes any previous scheduler
     *  from this container, and caches a local reference to the scheduler
     *  so that this composite does not need to search its attributes each
     *  time the scheduler is accessed.
     *  @param scheduler The Scheduler responsible for execution.
     */
    protected void _setScheduler(Scheduler scheduler) {
        // If the scheduler is not changed, do nothing.
        if (_scheduler != scheduler) {
            _scheduler = scheduler;
            invalidateSchedule();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The value that the postfire method will return. */
    protected boolean _postfireReturns;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The value returned by the prefire() method. */
    protected boolean _prefire = false;

    private boolean _actorFinished;

    /** Computed schedule that has not been fully executed because this
     *  director is waiting for resources.
     */
    private Schedule _savedSchedule;

    /** Saved position in the fixed schedule. Resume execution from there.
     */
    private int _savedSchedulePosition;

    /** Number of iterations that have been performed already.
     */
    private int _savedIterationCount;

    /** The scheduler. */
    private Scheduler _scheduler;
}
