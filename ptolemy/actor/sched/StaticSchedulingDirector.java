/* A director that uses a static schedule.

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

@ProposedRating Yellow (liuj@eecs.berkeley.edu)
@AcceptedRating Yellow (cxh@eecs.berkeley.edu) 3/2/98
*/
package ptolemy.actor.sched;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;

import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// StaticSchedulingDirector
/**
A director that uses static scheduling to govern the execution of the
CompositeActor it belongs to.
<p>
A StaticSchedulingDirector contains a scheduler. By calling the schedule()
method on the scheduler, the director can get an Enumeration of the
actors in the firing order. Then the director can use this Enumeration
to fire the actors in that order.
<p>
"Static" means that the schedule, once constructed, can be
used during the execution repeatedly.
So the schedule is locally cached in the scheduler, and can be reused
when needed. A schedule is called "valid" if is can be used to correctly
direct the execution of the CompositeActor.
However, the schedule may become invalid when the CompositeActor mutates.
The scheduler is a TopologyListener of the director, and the schedule
is automatically invalidated when a TopologyChange occurs.
The setScheduleValid() method can also be used to explicitly validate
or invalidate the schedule when needed.

@author Jie Liu
@version $Id$
@see ptolemy.actor.Director
@see Scheduler
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
     *  @exception IllegalActionException Not thrown in this base class,
     *  thrown in the derived classes if the director is not compatible with
     *  the specified container.
     */
    public StaticSchedulingDirector(CompositeActor container, String name)
            throws IllegalActionException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the director into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new director with no container, no pending mutations,
     *  no topology listeners, and a clone of the original scheduler, 
     *  if one existed.
     *
     *  @param ws The workspace for the cloned object.
     *  @exception CloneNotSupportedException If one of the attributes
     *   cannot be cloned.
     *  @return The new StaticSchedulingDirector.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
	try {
	    StaticSchedulingDirector newobj = (StaticSchedulingDirector)
		super.clone(ws);
	    if(_scheduler != null) {
		newobj.setScheduler((Scheduler)_scheduler.clone(ws));
	    } else {
		newobj._scheduler = null;
	    }
	    return newobj;
	} catch (Exception ex) {
	    throw new CloneNotSupportedException("Clone failed:" + 
						 ex.getMessage());
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
            throws IllegalActionException {
        if (scheduler != null && workspace() != scheduler.workspace()) {
            throw new IllegalActionException(this, scheduler,
                    "Cannot set scheduler because workspaces are different.");
        }
        try {
            workspace().getWriteAccess();
            // If there was a previous director, we need to reset it.
            if (_scheduler != null) _scheduler._makeSchedulerOf(null);
            if (scheduler != null) {
                scheduler._makeSchedulerOf(this);
            }
            _scheduler = scheduler;
        } finally {
            workspace().doneWriting();
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
    ////                         private variables                 ////

    // The scheduler.
    private Scheduler _scheduler;
}
