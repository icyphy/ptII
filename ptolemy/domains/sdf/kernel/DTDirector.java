/* An DTDirector governs the execution of a CompositeActor containing
   SDFActors

 Copyright (c) 1997-1998 The Regents of the University of California.
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

@ProposedRating Red (neuendor@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.kernel;

import ptolemy.graph.*;
import ptolemy.actor.*;
import ptolemy.actor.sched.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;

import collections.LinkedList;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// DTDirector
/**  An DTDirector is the class that controls execution of a set of SDFActors.
     It is derived from StaticSchedulingDirector and by default uses an
     SDFScheduler to static schedule the execution of the Actors.
     Furthermore, it creates Receivers of type QueueReceiver, which is
     consistant with a dataflow domain.

     The SDF director has a single parameter, "Iterations" corresponding to a
     limit on the number of times the director will fire its hierarchy
     before it returns false in postfire.   If this number is not greater
     than zero, then no limit is set and postfire will always return false.
     The default number of iterations is zero.
@author Steve Neuendorffer
@version $Id$
*/
public class DTDirector extends StaticSchedulingDirector {

    /** Construct a director in the default workspace with an empty string
     *  as its name.
     *  The DTDirector will have a default scheduler of type SDFScheduler.
     */
    public DTDirector() {
        super();
        _init();
    }

    /** Construct an DTDirector in the default workspace with the given name.
     *  The DTDirector will have a default scheduler of type SDFScheduler.
     *
     *  @param name Name of this object.
     */
    public DTDirector(String name) {
        super(name);
        _init();
    }

    /** Create a new DTDirector in the specified workspace with the specified
     *   name.   The DTDirector will have a default scheduler of type
     *   SDFScheduler.
     *
     *  @param workspace Object for synchronization and version tracking
     *  @param name Name of this director.
     */
    public DTDirector(Workspace workspace, String name) {
        super(workspace, name);
        _init();
    }



    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** Return a new receiver consistant with the SDF domain.
        All SDF receivers are FIFOQueues.
        @return A new FIFOQueue
        */
    public Receiver newReceiver() {
        return new QueueReceiver();
    }


    /** Prepare for firing and return true if firing can proceed.
     *  If there is no container, return false immediately.  Otherwise,
     *  the first step is to perform any pending mutations, and to initialize
     *  any actors that are added by those mutations.  This sequence is
     *  repeated until no more mutations are performed.  This way, the
     *  initialize() method in actors can perform mutations, and the
     *  mutations will be fully executed before proceeding. Then,
     *  if this is the local director of its container, invoke the prefire()
     *  methods of all its deeply contained actors, and return the logical AND
     *  of what they return.  If this is the executive director of its
     *  container, then invoke the prefire() method of the container and
     *  return what it returns.  Otherwise, return false.
     *  <p>
     *  This method should be invoked once per iteration, before any
     *  invocation of fire() in that iteration. It may produce output data.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @return True if the iteration can proceed.
     *  @exception CloneNotSupportedException If the prefire() method of the
     *   container or one of the deeply contained actors throws it.
     *  @exception IllegalActionException If the prefire() method of the
     *   container or one of the deeply contained actors throws it, or a
     *   pending mutation throws it.
     */
    public boolean prefire() throws IllegalActionException {
	CompositeActor c = getContainer();
	Director ed = c.getExecutiveDirector();
	if(ed = null) return true;
	if(ed.getCurrentTime()>_nextFiringTime) return true;
        return false;
    }

    public boolean postfire() throws IllegalActionException {
        int iterations = ((IntToken) (_parameteriterations.getToken()))
            .intValue();
        if((iterations > 0) && (_iteration >= iterations)) {
            _iteration = 0;
            return false;
        }
        return true;
    }

    /** If this is the local director of the container, then invoke the fire
     *  methods of all its deeply contained actors.  Otherwise, invoke the
     *  fire() method of the container.  In general, this may be called more
     *  than once in the same iteration, where an iteration is defined as one
     *  invocation of prefire(), any number of invocations of fire(),
     *  and one invocation of postfire().
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @exception CloneNotSupportedException If the fire() method of the
     *   container or one of the deeply contained actors throws it.
     *  @exception IllegalActionException If the fire() method of the
     *   container or one of the deeply contained actors throws it.
     */
    public void fire()
            throws IllegalActionException {
        CompositeActor container = ((CompositeActor)getContainer());

        if (container == null) {
            throw new InvalidStateException("DTDirector " + getName() +
                    " fired, but it has no container!");
        } else {
	    Director ed = container.getExecutiveDirector();
	    boolean iterate;
	    if(ed = null)
		iterate=_advanceTime(_nextFiringTime);
	    else {
		iterate=_advanceTime(ed.getCurrentTime);
	    }
	    if(!iterate) return;

            Scheduler s = getScheduler();
            if (s == null)
                throw new IllegalActionException("Attempted to fire " +
                        "SDF system with no scheduler");
            Enumeration allactors = s.schedule();
            while (allactors.hasMoreElements()) {
                Actor actor = (Actor)allactors.nextElement();
                if(!actor.prefire()) {
                    throw new IllegalActionException("SDF Schedule " +
                            "invalid.   Actor " +
                            "is not ready to fire.");
                }
                actor.fire();
                actor.postfire();
            }
        }
        _iteration++;
    }


    /** If this is the local director of its container, invoke the wrapup()
     *  methods of all its deeply contained actors.  If this is the executive
     *  director of the container, then invoke the wrapup() method of the
     *  container.
     *  <p>
     *  This method should be invoked once per execution.  None of the other
     *  action methods should be invoked after it in the execution.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @exception IllegalActionException If the wrapup() method of the
     *   container or one of the deeply contained actors throws it.
     */
    public void wrapup() throws IllegalActionException {
        CompositeActor container = ((CompositeActor)getContainer());
        if (container!= null) {
            Enumeration allactors = container.deepGetEntities();
            while (allactors.hasMoreElements()) {
                Actor actor = (Actor)allactors.nextElement();
                actor.wrapup();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Initialize the object.   In this case, we give the DTDirector a
     *  default scheduler
     */

    private void _init() {
        try {
            SDFScheduler scheduler = new SDFScheduler();
            setScheduler(scheduler);
        }
        catch (IllegalActionException e) {
            // if setScheduler fails, then we should just set it to Null.
            // this should never happen because we don't override
            // setScheduler() to do sanity checks.
            Debug.println("Illegal schedule caught, " +
                "which should never happen!");
        }

        try {
            _parameteriterations
                = new Parameter(this,"Iterations",new IntToken(0));
        }
        catch (Exception e) {
            Debug.println("Cannot create default iterations parameter.");
            Debug.println("This should never happen.");
        }
    }

    /** 
     * Advance the current time to the given time.   Release all tokens to
     * output ports that should have been created before this time.
     *
     * @return true if it is time to fire the system again at the
     * new time.
     */   
    boolean _advanceTime(double time) {
	boolean firenow = (time >= _nextfiringtime);
	_currenttime = time;
	if(firenow) {
	    Parameter p = (Parameter) getAttribute("Iteration Time");
	    DoubleToken t = (DoubleToken) p.getToken();
	    _nextfiringtime = _currenttime + t.doubleValue();
	}
	
	return firenow;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private int _iteration;
    private Parameter _parameteriterations;

    // Support for mutations.
    // private LinkedList _pendingMutations = null;
    // private LinkedList _mutationListeners = null;
//    private ActorListener _actorListener = null;
}
