/* A director that uses static shceduling.

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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
*/
package pt.actor;

import pt.kernel.*;
import pt.kernel.util.*;
import pt.kernel.mutation.*;
import pt.data.*;

import collections.LinkedList;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// StaticSchedulingDirector
/** 
A director that uses static scheduling to govern the execution of the
CompositeActor it belongs to. 
<p>
A StaticSchedulingDirector contains a scheduler. By calling the schedule()
method on the sceduler, the director can get an Enumeration of the 
actors in the firing order. Then the director can use this Enumeration 
to fire the actors in that order. 
<p> 
"Static" means that the firing sequence, once constructed, can be 
used during the execution repeatedly.
So the returned schedule is locally cached, and can be reused when needed.
A schedule is called "valid" if is can be used to correctly direct
the execution of the CompositeActor.
However, the schedule may become invalid when the CompositeActor mutates.
A flag <code>_schedulevalid</code> can be set when such a mutation 
happens and the director must invoke the scheduler again to reschedule
the CompositeActor.

@author Jie Liu
@version $Id$
@see Director
@see Scheduler
*/
public class StaticSchedulingDirector extends Director{
    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public StaticSchedulingDirector() {
        super();
    }

    /** Construct a director in the default workspace with the given name.
     *  If the name argument is null, then the name is set to the empty
     *  string. The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param name Name of this object.
     */
    public StaticSchedulingDirector(String name) {
        super(name);
    }

    /** Construct a director in the given workspace with the given name.
     *  If the workspace argument is null, use the default workspace.
     *  The director is added to the list of objects in the workspace.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *
     *  @param workspace Object for synchronization and version tracking
     *  @param name Name of this director.
     */
    public StaticSchedulingDirector(Workspace workspace, String name) {
        super(workspace, name);
    }


    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Return the scheduler responsible for scheduling the contained
     *  actors. 
     *  This method is read-synchronized on the workspace.
     *
     *  @return The scheduler responsible for scheduling inside actors.
     */
    public Scheduler getScheduler() {
        try {
            workspace().getReadAccess();
            return _scheduler;
        } finally {
            workspace().doneReading();
        }
    }

    /** Return the scheduled firing sequence. If the current schedule is
     *  valid, then return the cached version immediately. Otherwise
     *  invoke the scheduler to reschedule.
     *  @return the shceduled firing sequence in an Enumeration.
     *  @exception IllegalActionException if there's no scheduler in this
     *   director.
     *  @exception NotScheduleableException if the scheduler throws it.
     */
    public Enumeration schedule() 
             throws IllegalActionException, NotScheduleableException {
        if(_scheduler == null) {
            throw new IllegalActionException(this, 
                " has no scheduler.")
        }
        if(scheduleValid()) {
            return _cachedschedule;
        }
        return getScheduler().schedule();
    }

    /** Set the scheduler for this StaticSchedulingDirector.
     *  The container of the specified scheduler is set to this director
     *  , and if there was previously a scheduler, its container
     *  is set to null. This method is write-synchronized on the workspace.
     *
     *  @param director The Director responsible for execution.
     *  @exception IllegalActionException Not thrown in this base class,
     *   but derived classes may throw it if the scheduler is not compatible.
     */
    public void setScheduler(Scheduler scheduler) 
            throws IllegalActionException {
        try {
            workspace().getWriteAccess();
            // If there was a previous director, we need to reset it.
            if (_scheduler != null) _scheduler._makeDirectorOf(null);
            if (scheduler != null) {
                scheduler._makeSchedulerOf(this);
            }
            _scheduler = scheduler;
        } finally {
            workspace().doneWriting();
        }
    }

    /** Validate/Devalidate the schedule. A true argument indicate that
     *  the current (cached) schedule is valid, and the director can use
     *  it in the further execution. A false argument indicate that 
     *  the CompositeActor has been significantly change so that the
     *  cached schedule is no longer valid, and the director should
     *  invoke the scheduler again for a new schedule.
     *  @param true to set the schedule to be valid.
     *  @exception IllegalActionException IF there's no scheduler.
     */
    public void setScheduleValid( boolean valid)
             throws IllegalActionException {
        if(_scheduler == null) {
            throw new IllegalActionException(this, 
                " has no scheduler.")
        }
        _schedulevalid = valid;
    }

    /** Return true if the current (cached) schedule is valid.
     *  @return true if the schedule is valid.
     *  @exception IllegalActionException IF there's no scheduler.
     */
    public boolean scheduleValid() throws IllegalActionException {
        if(_scheduler == null) {
            throw new IllegalActionException(this, 
                " has no scheduler.")
        }
        return _scheduleValid;
    }
        
    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

    /** Override the _performMutations of the superclass, such that
     *  if there's any mutations performed, then set the schedule to be
     *  invalid, and inform all registered listeners
     *  of the mutations.  If there's no mutations performed, the 
     *  _schedulevalid variable remains the previous value.
     *  Return true if any mutations were performed, and false otherwise.
     *
     *  @exception IllegalActionException If the mutation throws it.
     *  @exception NameDuplicationException If the mutation throws it.
     */
    protected boolean _performMutations()
            throws IllegalActionException, NameDuplicationException {        
        if(super._performMutations()) {
            setScheduleValid(false);
            return true;
        }
        return false;
    }   

    ////////////////////////////////////////////////////////////////////////
    ////                         protected variables                    ////

    /** Description */
    protected int _aprotectedvariable;

    ////////////////////////////////////////////////////////////////////////
    ////                         private methods                        ////

    // Private methods should not have doc comments, they should
    // have regular C++ comments.
    private int _APrivateMethod() {
        return 1;
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    // Private variables should not have doc comments, they should
    // have regular C++ comments.
    private Scheduler _scheduler;
    private boolean schedulevalid = false;
    private Enumeration _cachedschedule;
}
