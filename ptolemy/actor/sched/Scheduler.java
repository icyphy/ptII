/* A base class for shcedulers.

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

@ProposedRating Red (liuj@eecs.berkeley.edu)
*/

package pt.actor;

import pt.kernel.*;
import pt.kernel.util.*;
import pt.kernel.mutation.*;

import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// Scheduler
/** 
A base class for shcedulers. A scheduler schedules the execution order
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
scheduling algorthm lives, and is ready to be override by the derived
classes.
<p>
Sechduler implements the MutationListener interface, and register itself
as a MutationListener to the host director.  When a mutation occors,
the director will inform all the mutatin listeners (including the 
scheduler), and the scheduler will in
validate the current schedule.
@author Jie Liu
@version $Id$
*/
public class Scheduler extends NamedObj implements MutationListener{
    /** Construct a schduler with empty name and no container(director)
     *  in the default workspace.
     *  FIXME: Need? For test
     * @see pt.kernel.util.NamedObj
     * @return The scheduler
     */	
    public Scheduler() {
        super();
    }

    /** Construct a schduler in the default workspace with the given name.
     *  If the name argument is null, then the name is set to the empty
     *  string. The scheduler is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param name Name of this object.
     */
    public Scheduler(String name) {
        super(name);
    }

    /** Construct a scheduler in the given workspace with the given name.
     *  If the workspace argument is null, use the default workspace.
     *  The scheduler is added to the list of objects in the workspace.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *
     *  @param workspace Object for synchronization and version tracking
     *  @param name Name of this scheduler.
     */
    public Scheduler(Workspace ws, String name) {
        super(ws, name);
    }
        
    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Notify the scheduler that an entity has been added to a composite.
     *  This will invalidate the current schedule.
     *
     *  @param composite The container of the entity.
     *  @param entity The actor being added to the composite.
     */	
    public void addEntity(CompositeEntity composite, Entity entity) {
        setValid(false);
    }

    /** Notify the scheduler that a port has been added to an entity.
     *  This will invalidate the current schedule.
     *
     *  @param entity The entity getting a new port.
     *  @param port The new port.
     */	
    public void addPort(Entity entity, Port port) {
        setValid(false);
    }

    /** Notify the scheduler that a relation has been added to a composite.
     *  This will invalidate the current schedule.
     *
     *  @param composite The container getting a new relation.
     *  @param relation The new relation.
     */	
    public void addRelation(CompositeEntity composite, Relation relation) {
        setValid(false);
    }

    /** Clone the scheduler into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new scheduler with no container, and no valid schdule.
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

    /** Notify the scheduler that mutation is complete. Do nothing in the
     *  base class.
     */	
    public void done() {}

    /** Return the container, which is the StaticSchedulingDirector
     *  for which this is the scheduler.
     *  @return The StaticSchedulingDirector that this scheduler is
     *  contained.
     */
    public Nameable getContainer() {
        return _container;
    }

    /** Notify the scheduler that a port has been linked to a relation.
     *  This will invalidate the current schedule.
     *
     *  @param relation The relation being linked.
     *  @param port The port being linked.
     */	
    public void link(Relation relation, Port port) {
        setValid(false);
    }

    /** Notify the scheduler that an entity has been removed from 
     *  a composite.
     *  This will invalidate the current schedule.
     *
     *  @param composite The container of the entity.
     *  @param entity The entity being removed.
     */	
    public void removeEntity(CompositeEntity composite, Entity entity){
        setValid(false);
    }

    /** Notify the scheduler that a port has been removed from a entity.
     *  This will invalidate the current schedule.
     *
     *  @param entity The container of the port.
     *  @param port The port being removed.
     */	
    public void removePort(Entity entity, Port port){
        setValid(false);
    }

    /** Notify the scheduler that a relation has been removed from a composite.
     *  This will invalidate the current schedule.
     *
     *  @param entity The container of the relation.
     *  @param port The relation being removed.
     */	
    public void removeRelation(CompositeEntity entity, Relation relation) {
        setValid(false);
    }

    /** Return the scheduling sequence. If the cached version of the 
     *  schedule is valid, return it directly. Otherwise call
     *  _schedule() to reconstruct. The validity of the current schedule
     *  is set by setValid() method.
     * @return An Enumeration of the deeply contained atomic entities
     *  in the firing order.
     * @exception NotScheduleableException If the _schedule() method 
     *  throws it. Not thrown in this base class, but may be needed
     *  by the derived scheduler.
     */	
    public Enumeration schedule() throws NotScheduleableException {
       if(!valid()) {
           _cachedschedule = _schedule();
       }
       return _cachedschedule;
    }

    /** Validate/invalidate the current schedule by set the _valid member.
     *  A <code>true</code> argument will indicate that the current 
     *  schedule is valid
     *  and can be retured immediately when schedule() is called without 
     *  running the scheduling algorithm. A <code>false</code> argument
     *  will invalidate it.
     *  @param true to set _valid flag to true.
     */
    public void setValid(boolean valid) {
        _valid = valid;
    }

    /** Notify the scheduler that a port has been unlinked from a relation.
     *  This will invalidate the current schedule.
     *
     *  @param relation The relation being unlinked.
     *  @param port The port being unlinked.
     */	
    public void unlink(Relation relation, Port port) {
        setValid(false);
    }

    /** Return true if the current schedule is valid.
     *@return true if the current schedule is valid.
     */
    public boolean valid() {
        return _valid;
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

    /** Make this scheduler the scheduler of the specified director, and 
     *  register itself as a mutation listenner of the director.
     *  This method should not be called directly.  Instead, call
     *  setScheduler of the StaticSchedulingDirector class
     *  (or a derived class).
     */
    protected void _makeSchedulerOf (StaticSchedulingDirector dir) {
        _container = dir;
        if (dir != null) {
            workspace().remove(this);
            dir.addMutationListener(this);
        }
    }

    /** Return the scheduling sequence. In this base class, it returns
     *  the containees of the CompositeActor in the order of construction.
     *  (Same as calling deepCetEntities()). The derived classes will
     *  override this method and add their scheduling algorithms here.
     *  If the scheduler has no container, or the contained 
     *  StaticSchedulingDirector has no container, return null.
     *  This method read synchronize the workspace.
     *  This method should not be called directly, rather the schedule()
     *  will call it when the schedule is not valid.
     * @see pt.kernel.CompositeEntity#deepGetEntities()
     * @return An Enumeration of the deeply contained atomic entities
     *  in the firing order.
     * @exception NotScheduleableException If the CompositeActor is not
     *  scheduleable. Not thrown in this base class, but may be needed
     *  by the derived scheduler.
     */	
    public Enumeration _schedule() throws NotScheduleableException {
        try {
            workspace().getReadAccess();
            StaticSchedulingDirector dir =
                (StaticSchedulingDirector)getContainer();
            if( dir == null) {
                return null;
            }
            CompositeActor ca = (CompositeActor)(dir.getContainer());
            if( ca == null) {
                return null;
            }
            return ca.deepGetEntities();
        } finally {
            workspace().doneReading();
        }
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    // Private variables should not have doc comments, they should
    // have regular C++ comments.
    private StaticSchedulingDirector _container = null;
    private boolean _valid = false;
    private Enumeration _cachedschedule;
}
