/* A base class for schedulers.

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
Made an Attribute.  Deprecated enumeration methods.
Caching should move into StaticSchedulingDirector.
*/

package ptolemy.actor.sched;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.kernel.util.*;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Iterator;

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
A schedule is simply a collection of objects. It could be the firing
order of actors in a particular composite actor, and it also could
consist of sub-schedules, each of which is another collection. We
leave for the director to interpret what a schedule means. In this
base class, the default schedule is an Enumeration of deep contained
actors in their construction order.
<p>
The schedule, once constructed, is cached and reused in the next time
if the schedule is still valid. The validation of a schedule is set by
the setValid() method. If the current schedule is set to be not valid,
the schedule() method will call the protected _schedule() method to
reconstruct it. The _schedule() method is the place the scheduling
algorithm goes, and the derived class should override it.
<p>
Scheduler does perform any mutations, and it is not a topology change
listener. The director who uses this scheduler should set the validation
flag accordingly when mutations occur.

@author Jie Liu, Steve Neuendorffer
@version $Id$
*/

public class Scheduler extends Attribute {
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

    /** Construct a scheduler in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public Scheduler(Director container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the scheduler into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new scheduler with no container, and no valid schedule.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If one of the attributes
     *   cannot be cloned.
     *  @return The new Scheduler.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        Scheduler newObject = (Scheduler) super.clone(workspace);
        newObject._container = null;
        newObject._valid = false;
        newObject._cachedSchedule = null;
        return newObject;
    }

    /** Return the scheduling sequence as an instance of Schedule.
     *  For efficiency, this method returns a cached version of the
     *  schedule, if it is valid.  Otherwise, it calls the protected
     *  method _getSchedule() to update the schedule.  Derived classes
     *  would normally override the protected method, not this one.
     *  The validity of the current schedule is set by the setValid()
     *  method.  This method is read-synchronized on the workspace.
     *  @return The Schedule returned by the _getSchedule() method.
     *  @exception IllegalActionException If the scheduler has no container
     *  (a director), or the director has no container (a CompositeActor),
     *  or the scheduling algorithm throws it.
     *  @exception NotSchedulableException If the _getSchedule() method
     *  throws it. Not thrown in this base class, but may be needed
     *  by the derived schedulers.
     */
    public Schedule getSchedule()
            throws IllegalActionException, NotSchedulableException {
        try {
            workspace().getReadAccess();
            StaticSchedulingDirector director =
                (StaticSchedulingDirector)getContainer();
            if (director == null) {
                throw new IllegalActionException(this,
                        "Scheduler has no director.");
            }
            CompositeActor compositeActor =
                (CompositeActor)(director.getContainer());
            if (compositeActor == null) {
                throw new IllegalActionException(this,
                        "Director has no container.");
            }
            if(!isValid() || _cachedGetSchedule == null) {
                _cachedGetSchedule = _getSchedule();
            }
            return _cachedGetSchedule;
        } finally {
            workspace().doneReading();
        }
    }

    /** Return the scheduling sequence as an enumeration.  For
     *  efficiency, this method returns a cached version of the
     *  schedule, if it is valid.  Otherwise, it calls the protected
     *  method _schedule() to update the schedule.  Derived classes
     *  would normally override the protected method, not this one.
     *  The validity of the current schedule is set by the setValid()
     *  method.  This method is read-synchronized on the workspace.
     *
     *  @return The Enumeration returned by the _schedule() method.
     *  @exception IllegalActionException If the scheduler has no container
     *  (a director), or the director has no container (a CompositeActor).
     *  @exception NotSchedulableException If the _schedule() method
     *  throws it. Not thrown in this base class, but may be needed
     *  by the derived schedulers.
     *  @deprecated Use the getSchedule method instead.
     */
    public Enumeration schedule() throws
            IllegalActionException, NotSchedulableException {
        try {
            workspace().getReadAccess();
            StaticSchedulingDirector director =
                (StaticSchedulingDirector)getContainer();
            if (director == null) {
                throw new IllegalActionException(this,
                        "Scheduler has no director.");
            }
            CompositeActor compositeActor =
                (CompositeActor)(director.getContainer());
            if (compositeActor == null) {
                throw new IllegalActionException(this,
                        "Director has no container.");
            }
            if(!isValid() || _cachedSchedule == null) {
                _cachedSchedule = new ArrayList();
                Enumeration newScheduleEnumeration = _schedule();
                while (newScheduleEnumeration.hasMoreElements()) {
                    _cachedSchedule.add(newScheduleEnumeration.nextElement());
                }
            }
            return Collections.enumeration(_cachedSchedule);
        } finally {
            workspace().doneReading();
        }
    }

    /** Specify the container.  If the specified container is an instance
     *  of Director, then this becomes the active scheduler for
     *  that director.  Otherwise, this is an attribute like any other within
     *  the container. If the container is not in the same
     *  workspace as this director, throw an exception.
     *  If this scheduler is already an attribute of the container,
     *  then this has the effect only of making it the active scheduler.
     *  If this scheduler already has a container, remove it
     *  from that container first.  Otherwise, remove it from
     *  the directory of the workspace, if it is present.
     *  If the argument is null, then remove it from its container.
     *  This director is not added to the workspace directory, so calling
     *  this method with a null argument could result in
     *  this director being garbage collected.
     *  <p>
     *  If this method results in removing this director from a container
     *  that is a Director, then this scheduler ceases to be the active
     *  scheduler for that CompositeActor.  Moreover, if the director
     *  contains any other schedulers, then the most recently added of those
     *  schedulers becomes the active scheduler.
     *  <p>
     *  This method is write-synchronized
     *  to the workspace and increments its version number.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the action would result in a
     *   recursive containment structure, or if
     *   this scheduler and container are not in the same workspace, or
     *   if the protected method _checkContainer() throws it.
     *  @exception NameDuplicationException If the name of this scheduler
     *   collides with a name already in the container.  This will not
     *   be thrown if the container argument is an instance of
     *   CompositeActor.
     */
    public void setContainer(NamedObj container)
            throws IllegalActionException, NameDuplicationException {
        try {
            _workspace.getWriteAccess();
            Nameable oldContainer = getContainer();
            if (oldContainer instanceof Director
                    && oldContainer != container) {
                // Need to remove this scheduler as the active one of the
                // old container. Search for another scheduler contained
                // by the composite.  If it contains more than one,
                // use the most recently added one.
                Scheduler previous = null;
                StaticSchedulingDirector castContainer =
                    (StaticSchedulingDirector)oldContainer;
                Iterator schedulers =
                    castContainer.attributeList(Scheduler.class).iterator();
                while (schedulers.hasNext()) {
                    Scheduler altScheduler = (Scheduler)schedulers.next();
                    // Since we haven't yet removed this director, we have
                    // to be sure to not just set it to the active
                    // director again.
                    if (altScheduler != this) {
                        previous = altScheduler;
                    }
                }
                castContainer._setScheduler(previous);
            }

            super.setContainer(container);

            if (container instanceof StaticSchedulingDirector) {
                // Set cached value in director
                ((StaticSchedulingDirector)container)._setScheduler(this);
            }
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Validate/invalidate the current schedule by giving a
     *  true/false argument.  A true argument will indicate that the
     *  current schedule is valid and can be returned immediately when
     *  schedule() is called without running the scheduling
     *  algorithm. A false argument will invalidate it.
     *  @param valid True to set the current schedule to valid.
     */
    public void setValid(boolean valid) {
        _valid = valid;
        if(valid == false) {
            _cachedSchedule = null;
            _cachedGetSchedule = null;
        }
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

    /** Return the scheduling sequence. In this base class, it returns
     *  the containees of the CompositeActor in the order of
     *  construction.  (Same as calling
     *  CompositeActor.deepGetEntities()).  The derived classes should
     *  override this method and add their scheduling algorithms here.
     *  This method should not be called directly, but rather the
     *  getSchedule() method will call it when the schedule is
     *  invalid. So it is not synchronized on the workspace.
     *  @return A Schedule of the deeply contained opaque entities
     *  in the firing order.
     *  @exception NotSchedulableException If the CompositeActor is not
     *  schedulable. Not thrown in this base class, but may be thrown
     *  by derived classes.
     *  @exception IllegalActionException If the scheduling algorithm
     *  throws it. Not thrown in this base class, but may be thrown
     *  by derived classes.
     *  @see ptolemy.kernel.CompositeEntity#deepGetEntities()
     */
    protected Schedule _getSchedule()
            throws IllegalActionException, NotSchedulableException {
	StaticSchedulingDirector director =
            (StaticSchedulingDirector)getContainer();
        CompositeActor compositeActor =
            (CompositeActor)(director.getContainer());
        List actors = compositeActor.deepEntityList();
	Schedule schedule = new Schedule();
	Iterator actorIterator = actors.iterator();
	while (actorIterator.hasNext()) {
	    Actor actor = (Actor)actorIterator.next();
	    Firing firing = new Firing();
	    firing.setActor(actor);
	    schedule.add(firing);
	}
	return schedule;
    }

    /** Return the scheduling sequence. In this base class, it returns
     *  the containees of the CompositeActor in the order of
     *  construction.  (Same as calling
     *  CompositeActor.deepGetEntities()).  The derived classes should
     *  override this method and add their scheduling algorithms here.
     *  This method should not be called directly, rather the
     *  schedule() method will call it when the schedule is
     *  invalid. So it is not synchronized on the workspace.
     *
     *  @return An Enumeration of the deeply contained opaque entities
     *  in the firing order.
     *  @exception NotSchedulableException If the CompositeActor is not
     *  schedulable. Not thrown in this base class, but may be needed
     *  by the derived scheduler.
     *  @exception IllegalActionException If the scheduling algorithm
     *  throws it. Not thrown in this base class, but may be thrown
     *  by derived classes.
     *  @see ptolemy.kernel.CompositeEntity#deepGetEntities()
     *  @deprecated Use the getSchedule method instead.
     */
    protected Enumeration _schedule()
            throws NotSchedulableException, IllegalActionException {
        StaticSchedulingDirector director =
            (StaticSchedulingDirector)getContainer();
        CompositeActor compositeActor =
            (CompositeActor)(director.getContainer());
        return Collections.enumeration(compositeActor.deepEntityList());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The container
    private StaticSchedulingDirector _container = null;
    // The flag that indicate whether the current schedule is valid.
    private boolean _valid = false;
    // The cached schedule.
    private List _cachedSchedule = null;
    // The cached schedule for getSchedule().
    private Schedule _cachedGetSchedule = null;
}
