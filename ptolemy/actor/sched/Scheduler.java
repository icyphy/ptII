/* A base class for schedulers.

 Copyright (c) 1998-1999 The Regents of the University of California.
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
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.sched;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.kernel.event.*;
import ptolemy.actor.*;

import java.util.Enumeration;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// Scheduler
/**
The base class for schedulers. A scheduler schedules the execution order
of the containees of a CompositeActor.
<p>
A scheduler has a reference to a StaticSchedulingDirector, and
provides the schedule.
The director will use this schedule to govern the execution of a
CompositeActor.
<p>
The schedule sequence, once constructed, is cached and reused in the
next time if the schedule is still valid. The validation of a schedule
is set by the <code>setValid()</code> method. If the current schedule
is set to be not valid, the schedule() method will call the protected
_schedule() method to reconstruct it. _schedule() is the place the
scheduling algorithm goes, and the derived class should override it.
<p>
Scheduler implements the TopologyListener interface, and register itself
as a TopologyListener to the host director.  When a topology change occurs,
the director will inform all the listeners (including the
scheduler), and the scheduler will invalidate the current schedule.

FIXME: This class uses LinkedList in the collections package. Change it
to Java collection when update to JDK1.2
@author Jie Liu
@version $Id$
*/
public class Scheduler extends NamedObj implements TopologyListener{
    /** Construct a scheduler with no container(director)
     *  in the default workspace, the name of the scheduler is
     *  "Basic_Scheduler".
     */
    public Scheduler() {
        super(_DEFAULT_SCHEDULER_NAME);
    }

    /** Construct a scheduler in the given workspace with the name
     *  "Basic_Scheduler".
     *  If the workspace argument is null, use the default workspace.
     *  The scheduler is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *
     *  @param workspace Object for synchronization and version tracking
     */
    public Scheduler(Workspace ws) {
        super(ws, _DEFAULT_SCHEDULER_NAME);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the scheduler into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new scheduler with no container, and no valid schedule.
     *
     *  @param ws The workspace for the cloned object.
     *  @exception CloneNotSupportedException If one of the attributes
     *   cannot be cloned.
     *  @return The new Scheduler.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        Scheduler newobj = (Scheduler) super.clone(ws);
        newobj._container = null;
        newobj._valid = false;
        newobj._cachedschedule = null;
        return newobj;
    }

    /** Invalidate the current schedule since an entity has been added
     *  to a composite.
     *
     *  @param event The mutation event
     */
    public void entityAdded (TopologyEvent event) {
        setValid(false);
    }

    /** Invalidate the current schedule since an entity has been removed
     *  from a composite.
     *
     * @param event The mutation event
     */
    public void entityRemoved (TopologyEvent event) {
        setValid(false);
    }

    /** Return the container, which is the StaticSchedulingDirector
     *  for which this is the scheduler.
     *  @return The StaticSchedulingDirector that this scheduler is
     *  contained.
     */
    public Nameable getContainer() {
        return _container;
    }

    /** Invalidate the current schedule since a port has been added 
     *  to an entity.
     *
     * @param event The mutation event
     */
    public void portAdded (TopologyEvent event) {
        setValid(false);
    }

    /** Invalidate the current schedule since a port has been linked
     *  to a relation.
     *
     * @param event The mutation event
     */
    public void portLinked (TopologyEvent event){
        setValid(false);
    }

    /** Invalidate the current schedule since a port has been removed
     *  from a entity.
     *
     * @param event The mutation event
     */
    public void portRemoved (TopologyEvent event){
        setValid(false);
    }

    /** Invalidate the current schedule since a port has been unlinked
     *  from a relation.
     *
     * @param event The mutation event
     */
    public void portUnlinked (TopologyEvent event){
        setValid(false);
    }

    /** Invalidate the current schedule since a relation has been added
     *  to a composite.
     *
     * @param event The mutation event
     */
    public void relationAdded (TopologyEvent event){
        setValid(false);
    }

    /** Invalidate the current schedule since a relation has been removed
     *  from a composite.
     *
     * @param event The mutation event
     */
    public void relationRemoved (TopologyEvent event){
        setValid(false);
    }

    /** Return the scheduling sequence. If the cached version of the
     *  schedule is valid, return it directly. Otherwise call
     *  _schedule() to reconstruct. The validity of the current schedule
     *  is set by setValid() method.
     *  If the scheduler has no container, or the container
     *  StaticSchedulingDirector has no container, throw an
     *  IllegalActionException.
     *  This method read synchronize the workspace.
     *
     * @return An Enumeration returned by _schedule() method.
     * @exception IllegalActionException If the scheduler has no container
     *  (director), or the container has no container (CompositeActor).
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
            if( dir == null) {
                throw new IllegalActionException(this,
                        "is a dangling scheduler.");
            }
            CompositeActor ca = (CompositeActor)(dir.getContainer());
            if( ca == null) {
                throw new IllegalActionException(this,
                        "is a dangling scheduler.");
            }
            if(!valid()) {
                _cachedschedule = new LinkedList();
                Enumeration newSchedEnum = _schedule();
                while (newSchedEnum.hasMoreElements()) {
                    _cachedschedule.insertLast(newSchedEnum.nextElement());
                }
            }
            return _cachedschedule.elements();
        } finally {
            workspace().doneReading();
        }
    }

    /** Validate/invalidate the current schedule by giving a true/false
     *  argument.
     *  A <code>true</code> argument will indicate that the current
     *  schedule is valid
     *  and can be returned immediately when schedule() is called without
     *  running the scheduling algorithm. A <code>false</code> argument
     *  will invalidate it.
     *  @param true to set the current schedule to valid.
     */
    public void setValid(boolean valid) {
        _valid = valid;
    }

    /** Return true if the current schedule is valid.
     *  @return true if the current schedule is valid.
     */
    public boolean valid() {
        return _valid;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Make this scheduler the scheduler of the specified director, and
     *  register itself as a topology listener of the director.
     *  This method should not be called directly.  Instead, call
     *  setScheduler() method of the StaticSchedulingDirector class
     *  (or a derived class).
     */
    protected void _makeSchedulerOf (StaticSchedulingDirector dir) {
        _container = dir;
        if (dir != null) {
            workspace().remove(this);
            dir.addTopologyListener(this);
        }
    }

    /** Return the scheduling sequence. In this base class, it returns
     *  the containees of the CompositeActor in the order of construction.
     *  (Same as calling deepCetEntities()). The derived classes should
     *  override this method and add their scheduling algorithms here.
     *  This method should not be called directly, rather the schedule()
     *  will call it when the schedule is not valid. So it is not
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
        return ca.deepGetEntities();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The container
    private StaticSchedulingDirector _container = null;
    // The flag that indicate whether the current schedule is valid.
    private boolean _valid = false;
    // The cached schedule.
    private LinkedList _cachedschedule = null;
    // The static name
    private static final String _DEFAULT_SCHEDULER_NAME = "Basic_Scheduler";
}
