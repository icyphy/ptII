/* An actor that stops a model executing when it receives a true token.

 Copyright (c) 1997-2002 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (neuendor@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.data.BooleanToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// Stop
/**
An actor that stops a model executing when it receives a true token
on any input channel. This is accomplished by calling stop() on the
director, which for some directors causes the director to return
<i>false</i> when its postfire() method is called, and by returning
<i>false</i> from the postfire() method of this actor, which for
some other directors causes execution to stop.  A return value
of <i>false</i> from postfire() indicates to the
enclosing context that an actor should not be invoked again.
<p>
This actor reads inputs in its postfire() method, and if a true-valued
input is found, then the director's stop() method is called.
For most directors, this results in a call to stopFire() for each
actor, followed by the setting of a flag that indicates that the
next call to postfire() should return false.
<p>
When exactly this stops the execution depends on the domain.
For example, in DE, if an event with time stamp <i>T</i> and
value <i>true</i> arrives at this actor, then the current
iteration will be concluded, and then the model will halt.
Concluding the current iteration means processing all events
in the event queue with time stamp <i>T</i>. Thus, it is possible
for actors to be invoked after this one is invoked with a <i>true</i>
input.
<p>
In SDF, if this actor receives <i>true</i>, then the current
iteration is concluded and then execution is stopped.  The
SDF director reacts to this actor returning <i>false</i> from
postfire(), which indicates that this actor does not wish to
be fired again.  In SDF, if any single actor does not wish to
be fired again, then none of the actors are fired again.
Because of the static scheduling, it would not make sense to
continue execution with only some of the actors being active.
<p>
In PN, where each actor has its own thread, the actors continue
executing until their next attempt to read an input or write an
output, at which point the thread is stopped. When all actor threads
have stopped, the PN director concludes, returns from fire(),
and returns <i>false</i> in postfire(). <b>NOTE</b>:
<i>This is not the best way to stop a PN model!</i> 
This mechanism is nondeterministic in the sense that there is
no way to control exactly what data is produced or consumed on
the connections before the model stops.  To stop a PN model,
it is better to design the model so that all actors are starved
of data when the model is to stop.  The director will detect
this starvation, and halt the model.  Nonetheless, if
the nondeterminism is acceptable, this actor can be used.

@author Edward A. Lee
@version $Id$
*/

public class Stop extends Sink {

    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public Stop(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        input.setTypeEquals(BaseType.BOOLEAN);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read one token from each input channel that has a token,
     *  and if any token is true, call stop() on the director. 
     *  @exception IllegalActionException If there is no director or
     *   if there is no manager, or if the container is not a
     *   CompositeActor.
     *  @return False if a stop is requested, and true otherwise.
     */
    public boolean postfire() throws IllegalActionException {
        boolean result = false;
        for (int i = 0; i < input.getWidth(); i++) {
            if (input.hasToken(i)) {
                if (((BooleanToken)input.get(i)).booleanValue()) {
                    result = true;
                }
            }
        }
        if (result) {
            Nameable container = getContainer();
            if (container instanceof CompositeActor) {
                Manager manager = ((CompositeActor)container).getManager();
                if (manager != null) {
                    manager.finish();
                } else {
                    throw new IllegalActionException(this,
                    "Cannot stop without a Manager.");
                }
            } else {
                throw new IllegalActionException(this,
                "Cannot stop without a container that is a CompositeActor.");
            }
        }
        return !result;
    }
}

