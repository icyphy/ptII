/* An SDFDirector governs the execution of a CompositeActor containing
   SDFActors

 Copyright (c) 1997-1999 The Regents of the University of California.
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

import collections.CircularList;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// SDFDirector
/**  An SDFDirector is the class that controls execution of a set of SDFActors.
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
public class SDFDirector extends StaticSchedulingDirector {

    /** Construct a director in the default workspace with an empty string
     *  as its name.
     *  The SDFDirector will have a default scheduler of type SDFScheduler.
     */
    public SDFDirector() {
        super();
        _init();
    }

    /** Construct an SDFDirector in the default workspace with the given name.
     *  The SDFDirector will have a default scheduler of type SDFScheduler.
     *
     *  @param name Name of this object.
     */
    public SDFDirector(String name) {
        super(name);
        _init();
    }

    /** Create a new SDFDirector in the specified workspace with the specified
     *   name.   The SDFDirector will have a default scheduler of type
     *   SDFScheduler.
     *
     *  @param workspace Object for synchronization and version tracking
     *  @param name Name of this director.
     */
    public SDFDirector(Workspace workspace, String name) {
        super(workspace, name);
        _init();
    }



    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** Return a new receiver consistant with the SDF domain.
     *  @return A new SDFReceiver
     */
    public Receiver newReceiver() {
        return new SDFReceiver();
    }

    /** The SDFDirector always assumes that it can be fired.  
     *  @return True If the Director can be fired.
     *  @exception IllegalActionException Not Thrown
     */
    public boolean prefire() throws IllegalActionException {
        return true;
    }

    /** Increment the number of iterations.
     *  If an iteration limit has been set, then 
     *  see if the limit has been reached.  If so, return false.
     *  otherwise return true.
     *  @return True if the Director wants to be fired again in the 
     *  future.
     *  @exception IllegalActionException Not thrown.
     */
    public boolean postfire() throws IllegalActionException {
        int iterations = ((IntToken) (_parameteriterations.getToken()))
            .intValue();
        _iteration++;
        if((iterations > 0) && (_iteration >= iterations)) {
            _iteration = 0;
            return false;
        }
        return true;
    }

    /** Calculate the current schedule, and fire the contained actors
     *  in the order given by the schedule. 
     *
     *  @exception IllegalActionException If the fire() method of the
     *   container or one of the deeply contained actors throws it.
     */
    public void fire() throws IllegalActionException {
        CompositeActor container = ((CompositeActor)getContainer());

        if (container == null) {
            throw new InvalidStateException("SDFDirector " + getName() +
                    " fired, but it has no container!");
        } else {
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
    }


    /** Call wrapup in all the contained actors.
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

    /** Initialize the object.   In this case, we give the SDFDirector a
     *  default scheduler of the class SDFScheduler.
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
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private int _iteration;
    private Parameter _parameteriterations;

    // Support for mutations.
    // private CircularList _pendingMutations = null;
    // private CircularList _mutationListeners = null;
//    private ActorListener _actorListener = null;
}



