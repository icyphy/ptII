/* A director that uses a static schedule.

 Copyright (c) 1998-2001 The Regents of the University of California.
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

@ProposedRating Yellow (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (neuendor@eecs.berkeley.edu)
Scheduler is an Attribute,
*/
package ptolemy.actor.sched;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;

import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// StaticSchedulingDirector
/**
A director that uses static scheduling to govern the execution of the
CompositeActor it belongs to.
<p>
A StaticSchedulingDirector contains a scheduler. By calling the getSchedule()
method on the scheduler, the director can get an instance of the
schedule class.  This class represents the number of times each actor
should be fired and their firing order.
<p>
"Static" means that the schedule, once constructed, can be
used during the execution repeatedly.
So the schedule is locally cached in the scheduler, and can be reused
when needed. A schedule is called "valid" if is can be used to correctly
direct the execution of the CompositeActor.
However, the schedule may become invalid when the CompositeActor mutates.

@author Jie Liu, Steve Neuendorffer
@version $Id$
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
     *  @param workspace Object for synchronization and version tracking
     *  @param name Name of this director.
     *  @exception IllegalActionException Not thrown in this base class;
     *   thrown in the derived classes if the director is not compatible with
     *   the specified container.
     *  @exception NameDuplicationException If the container is not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public StaticSchedulingDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Calculate the current schedule, if necessary,
     *  and iterate the contained actors
     *  in the order given by the schedule.  No internal state of the
     *  director is updated during fire, so it may be used with domains that
     *  require this property, such as CT.
     *  <p>
     *  Iterating an actor involves calling the actor's iterate() method,
     *  which is equivalent to calling the actor's  prefire(), fire() and
     *  postfire() methods in succession.  If iterate() returns NOT_READY,
     *  indicating that the actor is not ready to execute, then an
     *  IllegalActionException will be thrown. The values returned from
     *  iterate() are recorded and are used to determine the value that
     *  postfire() will return at the end of the director's iteration.
     *  <p>
     *  This base class is intended to be sample code for statically
     *  scheduled domains.  In many cases, these domains will need to
     *  override this method to perform domain specific operations.
     *  @exception IllegalActionException If any actor executed by this
     *  actor return false in prefire.
     *  @exception InvalidStateException If this director does not have a
     *  container.
     */
    public void fire() throws IllegalActionException {
        TypedCompositeActor container = ((TypedCompositeActor)getContainer());

        if (container == null) {
            throw new InvalidStateException("Director " + getName() +
                    " fired, but it has no container!");
        } else {
            Scheduler s = getScheduler();
            if (s == null)
                throw new IllegalActionException("Attempted to fire " +
                        "system with no scheduler");
	    Schedule sched = s.getSchedule();
	    Iterator firings = sched.firingIterator();
            while (firings.hasNext()) {
		Firing firing = (Firing)firings.next();
		Actor actor = (Actor)firing.getActor();
		int iterationCount = firing.getIterationCount();

		if(_debugging) {
                    _debug(new FiringEvent(this, actor,
                            FiringEvent.BEFORE_ITERATE));
		}

		int returnVal =
                    actor.iterate(iterationCount);
		if (returnVal == COMPLETED) {
		    _postfireReturns = _postfireReturns && true;
		} else if (returnVal == NOT_READY) {
		    throw new IllegalActionException(this,
                            (ComponentEntity) actor, "Actor " +
                            "is not ready to fire.");
		} else if (returnVal == STOP_ITERATING) {
		    _postfireReturns = false;
		}
		if(_debugging) {
                    _debug(new FiringEvent(this, actor,
                            FiringEvent.AFTER_ITERATE));
		}
            }
        }
    }

    /** Return the scheduler that is responsible for scheduling the
     *  directed actors.
     *  This method is read-synchronized on the workspace.
     *
     *  @return The contained scheduler.
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
     *  or for that matter when any change that may invalidate
     *  the schedule is made.  In this base class, the method simply sets
     *  a flag that forces scheduling to be redone at the next opportunity.
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

    /** Return false if the system has finished executing, either by
     *  reaching the iteration limit, or having an actor in the system return
     *  false in postfire.
     *  Increment the number of iterations.
     *  If the "iterations" parameter is greater than zero, then
     *  see if the limit has been reached.  If so, return false.
     *  Otherwise return true if all of the fired actors since the last
     *  call to prefire returned true.
     *  @return True if the Director wants to be fired again in the
     *  future.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public boolean postfire() throws IllegalActionException {
        return _postfireReturns;
    }

    /** Check the input ports of the container composite actor (if there
     *  are any) to see whether they have enough tokens, and return true
     *  if they do.  If there are no input ports, then also return true.
     *  Otherwise, return false.  Note that this does not call prefire()
     *  on the contained actors.
     *  @exception IllegalActionException If port methods throw it.
     *  @return True.
     */
    public boolean prefire() throws IllegalActionException {
        _postfireReturns = true;
        return super.prefire();
    }

    /** Set the scheduler for this StaticSchedulingDirector.
     *  The container of the specified scheduler is set to this director.
     *  If there was a previous scheduler, the container of that scheduler
     *  is set to null. This method is write-synchronized on the workspace.
     *  If the scheduler is not compatible with the director, an
     *  IllegalActionException is thrown.
     *  @param director The Director responsible for execution.
     *  @exception IllegalActionException Not thrown in this base class,
     *   but derived classes may throw it if the scheduler is not compatible.
     */
    public void setScheduler(Scheduler scheduler)
            throws IllegalActionException, NameDuplicationException {
        if (scheduler != null) {
            scheduler.setContainer(this);
        } else {
            _setScheduler(null);
        }
    }

    /** Validate/Invalidate the schedule. A true argument indicate that
     *  the current (cached) schedule is valid, and the director can use
     *  it in the further execution. A false argument indicate that
     *  the CompositeActor has been significantly changed so that the
     *  cached schedule is no longer valid, and the director should
     *  invoke the scheduler again for a new schedule. This calls the
     *  setValid() method of Scheduler.
     *  @param true to set the schedule to be valid.
     *  @exception IllegalActionException If there's no scheduler.
     */
    // FIXME: This should be protected.
    public void setScheduleValid(boolean valid)
            throws IllegalActionException {
        if(_scheduler == null) {
            throw new IllegalActionException(this,
                    "has no scheduler.");
        }
        _scheduler.setValid(valid);
    }

    /** Return true if the current (cached) schedule is valid.
     *  This calls the valid() method of Scheduler.
     *  @return true if the schedule is valid.
     *  @exception IllegalActionException If there's no scheduler.
     */
    public boolean isScheduleValid() throws IllegalActionException {
        if(_scheduler == null) {
            throw new IllegalActionException(this,
                    "has no scheduler.");
        }
        return _scheduler.isValid();
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
     *  @exception IllegalActionException If removing the old scheduler
     *   causes this to be thrown. Should not be thrown.
     *  @exception NameDuplicationException If removing the old scheduler
     *   causes this to be thrown. Should not be thrown.
     */
    protected void _setScheduler(Scheduler scheduler)
            throws IllegalActionException, NameDuplicationException {

        invalidateSchedule();
        _scheduler = scheduler;
        invalidateSchedule();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The scheduler.
    private Scheduler _scheduler;

    // The value that the postfire method will return;
    private boolean _postfireReturns;
}
