/* Director for the Giotto model of computation.

 Copyright (c) 1997-2000 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (eal@eecs.berkeley.edu)
*/

package ptolemy.domains.giotto.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.sched.StaticSchedulingDirector;
import ptolemy.actor.sched.Scheduler;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StreamListener;
import ptolemy.kernel.util.Workspace;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// GiottoDirector
/**

FIXME: document this.

@see ptolemy.domains.sdf.kernel.GiottoReceiver

@author  Cristoph Meyer, Ben Horowitz, and Edward A. Lee
@version $Id$
*/
public class GiottoDirector extends StaticSchedulingDirector {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public GiottoDirector()
            throws IllegalActionException, NameDuplicationException {
        super();
        _init();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace for this object.
     */
    public GiottoDirector(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
        _init();
    }

    /** Construct a director in the given container with the given name.
     *  If the container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *   with the specified container.
     *  @exception NameDuplicationException If the base class has an
     *   "iterations" parameter (which it should not).
     */
    public GiottoDirector(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The number of times that postfire may be called before it
     *  returns false.  If the value is less than or equal to zero,
     *  then the execution will never return false in postfire,
     *  and thus the execution can continue forever.
     *  The default value is an IntToken with the value zero.
     */
    public Parameter iterations;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the director into the specified workspace. This calls the
     *  base class and then sets the interations member.  The new
     *  actor will have the same parameter values as the old.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If one of the attributes
     *   cannot be cloned.
     */
    public Object clone(Workspace ws)
            throws CloneNotSupportedException {
        GiottoDirector newobj = (GiottoDirector)(super.clone(ws));
        newobj.iterations = (Parameter)newobj.getAttribute("iterations");
        return newobj;
    }

    /** Calculate the current schedule, if necessary, and iterate
     *  the contained actors in the order given by the schedule.
     *
     *  @exception IllegalActionException If this director does not have a
     *   container.
     */
    public void fire() throws IllegalActionException {
        _postfirereturns = true;
        TypedCompositeActor container = ((TypedCompositeActor)getContainer());
        if (container == null) {
            throw new IllegalActionException(this, "Has no container!");
        } else {
            Enumeration allactors = getScheduler().schedule();
            while (allactors.hasMoreElements()) {
                Actor actor = (Actor)allactors.nextElement();
		if(_debugging) {
		    _debug("Iterating " + ((NamedObj)actor).getFullName());
		}
                if (actor.iterate(1) == STOP_ITERATING) {
                    _postfirereturns = false;
                }
            }
        }
    }

    /** Return a new receiver consistent with the Giotto domain.
     *  @return A new GiottoReceiver.
     */
    public Receiver newReceiver() {
        Receiver receiver = new GiottoReceiver();
        _receivers.add(receiver);
        return receiver;
    }

    /** Initialize the actors associated with this director and
     *  initialize the iteration count to zero.  The order in which
     *  the actors are initialized is arbitrary.
     *  @exception IllegalActionException If the preinitialize() method of
     *   one of the associated actors throws it.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        _iteration = 0;
    }

    /** Update all the receivers that have been created by the
     *  newReceiver() method of this director.
     *  Return false if the system has finished executing, either by
     *  reaching the iteration limit, or having an actor in the model
     *  return false in postfire.
     *  Increment the number of iterations.
     *  @return True if the execution is not finished.
     *  @throws IllegalActionException If the iterations parameter does
     *   not have a valid token.
     */
    public boolean postfire() throws IllegalActionException {
        // NOTE: Some of these receivers may no longer be in use, so this
        // will be inefficient for models that are continually mutating.
        // However, it is functionally harmless.
        Iterator receivers = _receivers.iterator();
        while(receivers.hasNext()) {
            GiottoReceiver receiver = (GiottoReceiver)receivers.next();
            receiver.update();
        }
        int numiterations = ((IntToken) (iterations.getToken())).intValue();
        _iteration++;
        if((numiterations > 0) && (_iteration >= numiterations)) {
            _iteration = 0;
            return false;
        }
        return _postfirereturns;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Override the base class to indicate that this director does not
     *  need write access on the workspace during an iteration.
     *  @return False.
     */
    protected boolean _writeAccessRequired() {
        return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Initialize the director by creating a scheduler and iterations
     *  parameter.
     */
    private void _init()
            throws IllegalActionException, NameDuplicationException {
        // NOTE: This scheduler should probably be replaced by a
        // GiottoScheduler.  This scheduler returns a schedule that
        // fires actors in the order that they were created.
        Scheduler scheduler = new Scheduler(workspace());
        setScheduler(scheduler);
        iterations = new Parameter(this, "iterations", new IntToken(0));

        // FIXME: Remove this after debugging, or when GUI supports it.
        addDebugListener(new StreamListener());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The count of iterations executed.
    private int _iteration = 0;

    // The anded result of the values returned by actors' postfire().
    private boolean _postfirereturns = true;

    // List of all receivers this director has created.
    private LinkedList _receivers = new LinkedList();
}
