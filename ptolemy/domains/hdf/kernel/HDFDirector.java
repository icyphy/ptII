/* Director for the heterochronous dataflow model of computation.

 Copyright (c) 1997-2001 The Regents of the University of California.
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

@ProposedRating Red (vogel@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.hdf.kernel;

import ptolemy.graph.*;
import ptolemy.actor.*;
import ptolemy.actor.sched.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.domains.sdf.kernel.*;

import java.util.*;

////////////////////////////////////////////////////////////////////
//// HDFDirector
/**
The heterochronous dataflow (HDF) domain implements the HDF model 
of computation [1]. The HDF model of computation is a generalization 
of synchronous dataflow (SDF). In SDF, the set of port rates of an 
actor (called the type signature) are constant. In HDF, however, 
an actor has a finite number of type signatures which are allowed 
to change between iterations of the HDF schedule.
<p>
An HDF actor has an initial type signature when execution begins. 
The balance equations can then be solved to find a 
periodic schedule, as in SDF. Unlike SDF, an HDF actor is allowed to 
change its type signature after an iteration of the schedule. 
If a port rate change occurs, a new schedule 
corresponding to the new ports rates must then be obtained.
<p>
Since an HDF actor has a finite number of type signatures, it 
may be useful to use an FSM to control when type signature changes 
may occur. The HDFFSMDirector may be used to compose HDF with
 hierarchical FSMs according to the *charts [1] semantics.
<p>
Since an HDF actor has a finite number of possible type 
signatures, the number of possible schedules is also finite. 
As a result of this finite state space, deadlock and bounded 
channel lengths are decidable in HDF. In principle, all possible 
schedules could be computed at compile time. However, the number 
of schedules can be exponential in the number of actors, so this 
may not be practical.
<p>
This director makes use of an HDF scheduler that computes the 
schedules dynamically, and caches them. The size of the cache 
can be set by the <i>scheduleCacheSize</i> parameter. The default 
value of this parameter is 100.
<p>
<b>References</b>
<p>
<OL>
<LI>
A. Girault, B. Lee, and E. A. Lee, 
``<A HREF="http://ptolemy.eecs.berkeley.edu/papers/98/starcharts">Hierarchical
Finite State Machines with Multiple Concurrency Models</A>,'' April 13,
1998.</LI>
</ol>

@see HDFFSMDirector
@see HDFScheduler
@see HDFActor

@author Brian K. Vogel
@version $Id$
*/
public class HDFDirector extends SDFDirector {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *
     */
    public HDFDirector() {
        super();
	_init();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *
     *  @param workspace The workspace for this object.
     */
    public HDFDirector(Workspace workspace) {
        super(workspace);
	_init();
    }

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  The HDFDirector will have a default scheduler of type
     *   SDFScheduler.
     *
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *   with the specified container.  May be thrown in a derived class.
     *  @exception NameDuplicationException If the container is not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public HDFDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
	_init();
    }

    /** A Parameter representing the size of the schedule cache to
     *  use. The default value is 100. If the value is less than
     *  or equal to zero, then schedules will never be discarded
     *  from the cache.
     *  <p>
     *  Note that the number of schedules in an HDF model can be 
     *  exponential in the number of actors. Setting the cache size to a
     *  very large value is therefore not recommended if the
     *  model contains a large number of HDF actors.
     */
    public Parameter scheduleCacheSize;

    /**
     *
     *  @exception IllegalActionException If fixme.
     */
    public Schedule getSchedule() throws IllegalActionException{
	Scheduler scheduler = 
	    getScheduler();
	if (scheduler == null) {
	    throw new IllegalActionException(this, "Unable to get " + 
					 "the SDF or HDF scheduler.");
	}
	Schedule schedule;
	if (isScheduleValid()) {
	    // This will return a the current schedule.
	    schedule = scheduler.getSchedule();
	} else {
	    // The schedule is no longer valid, so check the schedule
	    // cache.

	    // Convert the model to a moml string. Note: This can generate
	    // quite a bit of text. This should still be more efficient
	    // than solving the balance equations. Do
	    // performance analysis to verify this.
	    CompositeActor container =  (CompositeActor)getContainer();
	    String momlKey = container.exportMoML();
	    //System.out.println("MoML: " + momlKey);

	    if (_scheduleCache.containsKey(momlKey)) {
		// cache hit.
		if (_debug_info) { 
		    System.out.println(getName() + 
				       " : Cache hit!");
		}
		// Remove the key from the list.
		_scheduleKeyList.remove(momlKey);
		// Now add the key to head of list.
		_scheduleKeyList.add(0, momlKey);

		schedule = (Schedule)_scheduleCache.get(momlKey);
	    } else {
		// cache miss.
		if (_debug_info) { 
		    System.out.println(getName() + 
				       " : Cache miss.");
		}
		// Add key to head of list.
		_scheduleKeyList.add(0, momlKey);
		int cacheSize = 
		    ((IntToken)(scheduleCacheSize.getToken())).intValue();
		if (_scheduleCache.size() >= cacheSize) {
		    // cache is  full.
		    // remove tail of list.
		    _scheduleKeyList.remove(cacheSize - 1);
		    // remove key from map.
		    _scheduleCache.remove(momlKey);
		}
		// Add key/schedule to the schedule map.
		schedule = scheduler.getSchedule();
		_scheduleCache.put(momlKey, schedule);
	    } 
	}
	return schedule;
    }

    /** Return the firing count of the specified actor in the schedule.
     *  The specified actor must be director contained by this director.
     *  Otherwise an exception will occur.
     *
     *  @param actor The actor to return the firing count for.
     *  @exception IllegalActionException I
     */
    public int getFiringCount(Actor actor) throws IllegalActionException {
	
	Schedule schedule = getSchedule();
	Iterator firings = schedule.firingIterator();
	int occurrence = 0;
	while (firings.hasNext()) {
	    Firing firing = (Firing)firings.next();
	    Actor actorInSchedule = (Actor)(firing.getActor());
	    String actorInScheduleName = 
		((Nameable)actorInSchedule).getName();
	    String actorName = ((Nameable)actor).getName();
	    if (actorInScheduleName.equals(actorName)) {
		// Current actor in the static schedule is
		// the HDF composite actor containing this FSM.
		// Increment the occurrence count of this actor.
		occurrence += firing.getIterationCount();
	    }

	    if (_debug_info) { 
		System.out.println(getName() + 
     " :  _getFiringsPerSchedulIteration(): Actor in static schedule: " +
				   ((Nameable)actor).getName());
		System.out.println(getName() + 
      " : _getFiringsPerSchedulIteration(): Actors in static schedule:" +
				   occurrence);
	    }
	}
	return occurrence;
    }

    /** Initialize the actors associated with this director, set the
     *  size of the schedule cache, and then compute the schedule. 
     *  The schedule is computed during initialization so that 
     *  hierarchical opaque composite actors can be scheduled 
     *  properly (since the act of computing the schedule sets the
     *  rate parameters of the external ports). The order in which 
     *  the actors are initialized is arbitrary.
     *
     *  @exception IllegalActionException If the initialize() method of
     *  one of the associated actors throws it, or if there is no
     *  scheduler, or if the cache size parameter is not set to
     *  a valid value.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        SDFScheduler scheduler = (SDFScheduler)getScheduler();
	int cacheSize = 
	    ((IntToken)(scheduleCacheSize.getToken())).intValue();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Initialize the object. In this case, we give the HDFDirector a
     *  default scheduler of the class HDFScheduler.
     */
    private void _init() {
        try {
            SDFScheduler scheduler = 
                new SDFScheduler(this, uniqueName("Scheduler"));
            setScheduler(scheduler);
        }
        catch (Exception e) {
            // if setScheduler fails, then we should just set it to Null.
            // this should never happen because we don't override
            // setScheduler() to do sanity checks.
            throw new InternalErrorException(
                    "Could not create Default Scheduler:\n" +
                    e.getMessage());
        }
        try {
	    int cacheSize = 100;
	    scheduleCacheSize
                = new Parameter(this,"scheduleCacheSize",new IntToken(cacheSize));

	    _scheduleCache = new HashMap();
	    _scheduleKeyList = new ArrayList(cacheSize);
        }
        catch (Exception e) {
            throw new InternalErrorException(
                    "Cannot create default iterations parameter:\n" +
                    e.getMessage());
        }
    }

    // The hashmap for the schedule cache.
    private Map _scheduleCache;
    private List _scheduleKeyList;

    // Set to true to enable debugging.
    //private boolean _debug_info = true;
    private boolean _debug_info = false;
}
