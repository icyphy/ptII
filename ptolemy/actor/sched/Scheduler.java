/* A base class for schedulers.

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
import ptolemy.kernel.event.*;
import ptolemy.actor.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// Scheduler
/**
The base class for schedulers. A scheduler schedules the execution order
of the containees of a CompositeActor.
<p>
A scheduler has a reference to a StaticSchedulingDirector, and
provides the schedule for it.
The director will use this schedule to govern the execution of a
CompositeActor.
<p>
 A schedule is simply a
collection of objects. It could be the firing order of actors in a
particular composite actor, and it also could consist of sub-schedules,
each of which is another collection. We leave for the director to
interpret what a schedule means. In this base class, the default schedule
is an Enumeration of deep contained actors in their construction order.
<P>
The schedule, once constructed, is cached and reused in the next time
if the schedule is still valid. The validation of a schedule is set by
the setValid() method. If the current schedule is set to be not valid,
the schedule() method will call the protected _schedule() method to
reconstruct it. The _schedule() method is the place
the scheduling algorithm goes, and the derived class should override it.
<p>
Scheduler does perform any mutations, and it is not a topology change
listener. The director who uses this scheduler should set the validation
flag accordingly when mutations occur.

@author Jie Liu
@version $Id$
*/

public class Scheduler extends NamedObj {
    /** Construct a scheduler with no container(director)
     *  in the default workspace, the name of the scheduler is
     *  "Scheduler".
     */
    public Scheduler() {
        super();
        try {
            setName(_DEFAULT_SCHEDULER_NAME);
        } catch (KernelException ex) {
            // Should not be thrown.
            throw new InternalErrorException(ex.toString());
        }
    }

    /** Construct a scheduler in the given workspace with the name
     *  "Scheduler".
     *  If the workspace argument is null, use the default workspace.
     *  The scheduler is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace Object for synchronization and version tracking.
     */
    public Scheduler(Workspace workspace) {
        super(workspace);
        try {
            setName(_DEFAULT_SCHEDULER_NAME);
        } catch (KernelException ex) {
            // Should not be thrown.
            throw new InternalErrorException(ex.toString());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the scheduler into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new scheduler with no container, and no valid schedule.
     *
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If one of the attributes
     *   cannot be cloned.
     *  @return The new Scheduler.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        Scheduler newobj = (Scheduler) super.clone(workspace);
        newobj._container = null;
        newobj._valid = false;
        newobj._cachedSchedule = null;
        return newobj;
    }

    /** Return the container, which is the StaticSchedulingDirector
     *  for which this is the scheduler.
     *  @return The StaticSchedulingDirector that this scheduler is
     *  contained within.
     */
    public Nameable getContainer() {
        return _container;
    }

    /** Return the scheduling sequence as an enumeration.
     *  For efficiency, this method returns a cached version of the schedule,
     *  if it is valid.  Otherwise, it calls the protected method _schedule()
     *  to update the schedule.  Derived classes would normally override
     *  the protected method, not this one.
     *  The validity of the current schedule is set by the setValid() method.
     *  This method is read-synchronized on the workspace.
     *
     * @return The Enumeration returned by the _schedule() method.
     * @exception IllegalActionException If the scheduler has no container
     *  (a director), or the director has no container (a CompositeActor).
     * @exception NotSchedulableException If the _schedule() method
     *  throws it. Not thrown in this base class, but may be needed
     *  by the derived schedulers.
     */
    public Enumeration schedule() throws
            IllegalActionException, NotSchedulableException {
        try {
            workspace().getReadAccess();
            StaticSchedulingDirector dir =
                (StaticSchedulingDirector)getContainer();
            if (dir == null) {
                throw new IllegalActionException(this,
                        "Scheduler has no director.");
            }
            CompositeActor ca = (CompositeActor)(dir.getContainer());
            if (ca == null) {
                throw new IllegalActionException(this,
                        "Director has no container.");
            }
            if(!isValid()) {
                _cachedSchedule = new ArrayList();
                Enumeration newSchedEnum = _schedule();
                while (newSchedEnum.hasMoreElements()) {
                    _cachedSchedule.add(newSchedEnum.nextElement());
                }
            }
            return Collections.enumeration(_cachedSchedule);
        } finally {
            workspace().doneReading();
        }
    }

    /** Validate/invalidate the current schedule by giving a true/false
     *  argument.
     *  A true argument will indicate that the current
     *  schedule is valid
     *  and can be returned immediately when schedule() is called without
     *  running the scheduling algorithm. A false argument
     *  will invalidate it.
     *  @param valid True to set the current schedule to valid.
     */
    public void setValid(boolean valid) {
        _valid = valid;
    }

    /** Return true if the current schedule is valid.
     *  @return true if the current schedule is valid.
     */
    public boolean isValid() {
        return _valid;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    // The static name
    protected static String _DEFAULT_SCHEDULER_NAME = "Scheduler";

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Make this scheduler the scheduler of the specified director, and
     *  register it as a topology listener of the director.
     *  This method should not be called directly.  Instead, call the
     *  setScheduler() method of the StaticSchedulingDirector class
     *  (or a derived class).
     */
    protected void _makeSchedulerOf(StaticSchedulingDirector dir) {
        _container = dir;
        if (dir != null) {
            workspace().remove(this);
        }
    }

    /** Return the scheduling sequence. In this base class, it returns
     *  the containees of the CompositeActor in the order of construction.
     *  (Same as calling CompositeActor.deepGetEntities()).
     *  The derived classes should
     *  override this method and add their scheduling algorithms here.
     *  This method should not be called directly, rather the schedule()
     *  method
     *  will call it when the schedule is invalid. So it is not
     *  synchronized on the workspace.
     *
     * @see ptolemy.kernel.CompositeEntity#deepGetEntities()
     * @return An Enumeration of the deeply contained opaque entities
     *  in the firing order.
     * @exception NotSchedulableException If the CompositeActor is not
     *  schedulable. Not thrown in this base class, but may be needed
     *  by the derived scheduler.
     */
    protected Enumeration _schedule() throws NotSchedulableException {
        StaticSchedulingDirector dir =
            (StaticSchedulingDirector)getContainer();
        CompositeActor ca = (CompositeActor)(dir.getContainer());
        return Collections.enumeration(ca.deepEntityList());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The container
    private StaticSchedulingDirector _container = null;
    // The flag that indicate whether the current schedule is valid.
    private boolean _valid = false;
    // The cached schedule.
    private List _cachedSchedule = null;
}
