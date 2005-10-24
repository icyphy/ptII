/* A director that uses a static schedule.

 Copyright (c) 1998-2005 The Regents of the University of California.
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
import ptolemy.actor.Director;
import ptolemy.actor.FiringEvent;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.DebugListener;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
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
     */
    public StaticSchedulingDirector() {
        super();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace of this object.
     */
    public StaticSchedulingDirector(Workspace workspace) {
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
    public synchronized void addDebugListener(DebugListener listener) {
        super.addDebugListener(listener);

        Scheduler scheduler = getScheduler();

        if (scheduler != null) {
            scheduler.addDebugListener(listener);
        }
    }

    /** Calculate the current schedule, if necessary, and iterate the
     *  contained actors in the order given by the schedule.  No
     *  internal state of the director is updated during fire, so it
     *  may be used with domains that require this property, such as
     *  CT. <p>
     *  Iterating an actor involves calling the actor's iterate() method,
     *  which is equivalent to calling the actor's prefire(), fire() and
     *  postfire() methods in succession.  If iterate() returns NOT_READY,
     *  indicating that the actor is not ready to execute, then an
     *  IllegalActionException will be thrown. The values returned from
     *  iterate() are recorded and are used to determine the value that
     *  postfire() will return at the end of the director's iteration. <p>
     *
     *  This method may be overridden by some domains to perform additional
     *  domain-specific operations.
     *  @exception IllegalActionException If any actor executed by this
     *  actor return false in prefire.
     *  @exception InvalidStateException If this director does not have a
     *  container.
     */
    public void fire() throws IllegalActionException {
        Scheduler scheduler = getScheduler();

        if (scheduler == null) {
            throw new IllegalActionException("Attempted to fire "
                    + "system with no scheduler");
        }

        // This will throw IllegalActionException if this director
        // does not have a container.
        Schedule schedule = scheduler.getSchedule();
        Iterator firings = schedule.firingIterator();

        while (firings.hasNext() && !_stopRequested) {
            Firing firing = (Firing) firings.next();
            Actor actor = (Actor) firing.getActor();
            int iterationCount = firing.getIterationCount();

            if (_debugging) {
                _debug(new FiringEvent(this, actor, FiringEvent.BEFORE_ITERATE,
                        iterationCount));
            }

            int returnValue = actor.iterate(iterationCount);

            if (returnValue == STOP_ITERATING) {
                _postfireReturns = false;
            } else if (returnValue == NOT_READY) {
                throw new IllegalActionException(this, (ComponentEntity) actor,
                        "Actor " + "is not ready to fire.");
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
    public void invalidateSchedule() {
        _debug("Invalidating schedule.");

        try {
            setScheduleValid(false);
        } catch (IllegalActionException ex) {
            // no scheduler.  ignore.
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
    public boolean postfire() throws IllegalActionException {
        return _postfireReturns && !_stopRequested;
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
    public boolean prefire() throws IllegalActionException {
        _postfireReturns = true;
        return super.prefire();
    }

    /** Override the base class to also remove the listener from the scheduler,
     *  if there is one.
     *  @param listener The listener to remove from the list of listeners
     *   to which debug messages are sent.
     *  @see #addDebugListener(DebugListener)
     */
    public synchronized void removeDebugListener(DebugListener listener) {
        super.removeDebugListener(listener);

        Scheduler scheduler = getScheduler();

        if (scheduler != null) {
            scheduler.removeDebugListener(listener);
        }
    }

    /** Validate/Invalidate the schedule. A true argument indicate that
     *  the current (cached) schedule is valid, and the director can use
     *  it in the further execution. A false argument indicate that
     *  the CompositeActor has been significantly changed so that the
     *  cached schedule is no longer valid, and the director should
     *  invoke the scheduler again for a new schedule. This calls the
     *  setValid() method of Scheduler.
     *  @param valid True if the schedule is to be marked valid.
     *  @exception IllegalActionException If there's no scheduler.
     */
    public void setScheduleValid(boolean valid) throws IllegalActionException {
        // FIXME: This should be protected.  Edward Added this
        // comment 5/99 r1.26
        // The only other place it is called is CTEmbeddedDirector,
        // which extends this class?
        if (_scheduler == null) {
            throw new IllegalActionException(this, "has no scheduler.");
        }

        _scheduler.setValid(valid);
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
            _setScheduler(null);
        }
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
     *  @exception IllegalActionException Not thrown in this base class,
     *   but derived classes may throw it if the scheduler is not compatible.
     *  @exception NameDuplicationException Not thrown in this base class,
     *   but derived classes may throw it if the scheduler is not compatible.
     */
    protected void _setScheduler(Scheduler scheduler)
            throws IllegalActionException, NameDuplicationException {
        invalidateSchedule();
        _scheduler = scheduler;
        invalidateSchedule();
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected variables                 ////

    /** The value that the postfire method will return. */
    protected boolean _postfireReturns;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The scheduler. */
    private Scheduler _scheduler;
}
