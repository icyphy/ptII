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
import pt.data.*;

import collections.LinkedList;
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
@author Jie Liu
@version $Id$
@see classname
@see full-classname
*/
public class Scheduler extends NamedObj{
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
    public Schedule(String name) {
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

    
    /** Return the container, which is the StaticSchedulingDirector
     *  for which this is the scheduler.
     *  @return The StaticSchedulingDirector that this scheduler is
     *  contained.
     */
    public Nameable getContainer() {
        return _container;
    }

    /** Return the scheduling sequence. In this base class, it returns
     *  the containees of the CompositeActor in the order of construction.
     *  (Same as calling deepCetEntities()). The derived classes will
     *  override this method and add their scheduling algorithms here.
     *  If the scheduler has no container, or the contained 
     *  StaticSchedulingDirector has no container, return null.
     * @see pt.kernel.CompositeEntity#deepGetEntities()
     * @return An Enumeration of the deeply contained atomic entities
     *  in the firing order.
     * @exception NotScheduleableException If the CompositeActor is not
     *  scheduleable. Not thrown in this base class, but may be needed
     *  by the derived scheduler.
     */	
    public Enumeration schedule() throws NotScheduleableException {
        StaticSchedulingDirector dir = (StaticSchedulingDirector)getContainer();
        if( dir == null) {
            return null;
        }
        CompositeActor ca = (CompositeActor)(dir.getContainer());
        if( ca == null) {
            return null;
        }
        return ca.deepGetEntities();
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

    /** Make this scheduler the scheduler of the specified director.
     *  This method should not be called directly.  Instead, call
     *  setScheduler of the StaticSchedulingDirector class
     *  (or a derived class).
     */
    protected void _makeSchedulerOf (StaticSchedulingDirector dir) {
        _container = dir;
        if (dir != null) {
            workspace().remove(this);
        }
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    // Private variables should not have doc comments, they should
    // have regular C++ comments.
    private StaticSchedulingDirector _container = null;
}
