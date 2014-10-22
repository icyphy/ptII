/* A base class for schedulers.

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

 FIXME: Caching should move into StaticSchedulingDirector.
 */
package ptolemy.actor.sched;

import java.util.Iterator;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// Scheduler

/**
 The base class for schedulers. A scheduler schedules the execution
 order of the containees of a CompositeActor.  <p>

 A scheduler is contained by a StaticSchedulingDirector, and provides
 the schedule for it.  The director will use this schedule to govern
 the execution of a CompositeActor. <p>

 A schedule is represented by the Schedule class, and determines the
 order of the firing of the actors in a particular composite actor.  In
 this base class, the default schedule fires the deeply
 contained actors in the order of their construction.  A domain specific
 scheduler will override this to provide a different order. <p>

 The schedule, once constructed, is cached and reused as long as the
 schedule is still valid.  The validity of the schedule is set by the
 setValid() method.  If the current schedule is not valid, then the
 schedule will be recomputed the next time the getSchedule() method is
 called.  However, derived classes will usually override only the
 protected _getSchedule() method. <p>

 The scheduler does not perform any mutations, and it does not listen
 for changes in the model.  Directors that use this scheduler should
 normally invalidate the schedule when mutations occur.

 @author Jie Liu, Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Green (neuendor)
 @Pt.AcceptedRating Yellow (neuendor)
 @see ptolemy.actor.sched.Schedule
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
            throw new InternalErrorException(this, ex, null);
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
            throw new InternalErrorException(this, ex, null);
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
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Scheduler newObject = (Scheduler) super.clone(workspace);
        newObject._valid = false;
        newObject._cachedGetSchedule = null;
        return newObject;
    }

    /** Return the scheduling sequence as an instance of the Schedule class.
     *  For efficiency, this method returns a cached version of the
     *  schedule, if it is valid.  Otherwise, it calls the protected
     *  method _getSchedule() to update the schedule.  Derived classes
     *  normally override the protected method, not this one.
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
    public Schedule getSchedule() throws IllegalActionException,
            NotSchedulableException {
        try {
            workspace().getReadAccess();

            StaticSchedulingDirector director = (StaticSchedulingDirector) getContainer();

            if (director == null) {
                throw new IllegalActionException(this,
                        "Scheduler has no director.");
            }

            CompositeActor compositeActor = (CompositeActor) director
                    .getContainer();

            if (compositeActor == null) {
                throw new IllegalActionException(this,
                        "Director has no container.");
            }

            if (!isValid() || _cachedGetSchedule == null) {
                _cachedGetSchedule = _getSchedule();
                _workspaceVersion = workspace().getVersion();
            }

            return _cachedGetSchedule;
        } finally {
            workspace().doneReading();
        }
    }

    /** Return true if the current schedule is valid.
     *  @return true if the current schedule is valid.
     */
    public boolean isValid() {
        if (_workspaceVersion != workspace().getVersion()) {
            return false;
        }
        return _valid;
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
    @Override
    public void setContainer(NamedObj container) throws IllegalActionException,
            NameDuplicationException {
        try {
            _workspace.getWriteAccess();

            Nameable oldContainer = getContainer();

            if (oldContainer instanceof Director && oldContainer != container) {
                // Need to remove this scheduler as the active one of the
                // old container. Search for another scheduler contained
                // by the composite.  If it contains more than one,
                // use the most recently added one.
                Scheduler previous = null;
                StaticSchedulingDirector castContainer = (StaticSchedulingDirector) oldContainer;
                Iterator schedulers = castContainer.attributeList(
                        Scheduler.class).iterator();

                while (schedulers.hasNext()) {
                    Scheduler altScheduler = (Scheduler) schedulers.next();

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
                ((StaticSchedulingDirector) container)._setScheduler(this);
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
        if (valid == false) {
            _cachedGetSchedule = null;
        } else {
            _workspaceVersion = workspace().getVersion();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The default name.
     */
    protected static final String _DEFAULT_SCHEDULER_NAME = "Scheduler";

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Reschedule the model.  In this base class, this method returns
     *  the actors contained by the CompositeActor in the order of
     *  their construction, i.e. the same order as returned by the
     *  CompositeActor.deepGetEntities() method.  Derived classes
     *  should override this method to provide a domain-specific
     *  scheduling algorithm.  This method is not intended to be
     *  called directly, but is called in turn by the getSchedule()
     *  method.  This method is not synchronized on the workspace, because
     *  the getSchedule() method is.
     *
     *  @return A Schedule of the deeply contained opaque entities
     *  in the firing order.
     *  @exception IllegalActionException If the scheduling algorithm
     *  throws it. Not thrown in this base class, but may be thrown
     *  by derived classes.
     *  @exception NotSchedulableException If the CompositeActor is not
     *  schedulable. Not thrown in this base class, but may be thrown
     *  by derived classes.
     *  @see ptolemy.kernel.CompositeEntity#deepEntityList()
     */
    protected Schedule _getSchedule() throws IllegalActionException,
            NotSchedulableException {
        StaticSchedulingDirector director = (StaticSchedulingDirector) getContainer();
        CompositeActor compositeActor = (CompositeActor) director
                .getContainer();
        List actors = compositeActor.deepEntityList();
        Schedule schedule = new Schedule();
        Iterator actorIterator = actors.iterator();

        while (actorIterator.hasNext()) {
            Actor actor = (Actor) actorIterator.next();
            Firing firing = new Firing();
            firing.setActor(actor);
            schedule.add(firing);
        }

        return schedule;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The cached schedule for getSchedule().
    private Schedule _cachedGetSchedule = null;

    // The flag that indicate whether the current schedule is valid.
    private boolean _valid = false;

    // Workspace version when the last schedule was constructed.
    private long _workspaceVersion = -1L;
}
