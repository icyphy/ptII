/* An actor that stops a model executing when it receives a true token.

Copyright (c) 1997-2005 The Regents of the University of California.
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

*/
package ptolemy.actor.lib;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Settable;


//////////////////////////////////////////////////////////////////////////
//// Stop

/**

An actor that stops execution of a model when it receives a true
token on any input channel. This is accomplished by calling
finish() on the manager, which requests that the current iteration
be completed and then the model execution be halted. If the input
is not connected to anything, then this actor requests a stop
whenever it fires.

<p>
When exactly this stops the execution depends on the domain.  For
example, in DE, if an event with time stamp <i>T</i> and value
<i>true</i> arrives at this actor, then the current iteration will
be concluded, and then the model will halt.  Concluding the current
iteration means processing all events in the event queue with time
stamp <i>T</i>. Thus, it is possible for actors to be invoked after
this one is invoked with a <i>true</i> input.

<p>

In SDF, if this actor receives <i>true</i>, then the current
iteration is concluded and then execution is stopped.  Similarly in
SR.

<p>
In PN, where each actor has its own thread, there is no
well-defined notion of an iteration. The finish() method of the
manager calls stopFire() on all actors, which for threaded actors
results in halting them upon their next attempt to read an input or
write an output. When all actor threads have stopped, the iteration
concludes and the model halts. <b>NOTE</b>: <i>This is not the best
way to stop a PN model!</i> This mechanism is nondeterministic in
the sense that there is no way to control exactly what data is
produced or consumed on the connections before the model stops.  To
stop a PN model, it is better to design the model so that all
actors are starved of data when the model is to stop.  The director
will detect this starvation, and halt the model.  Nonetheless, if
the nondeterminism is acceptable, this actor can be used.

@author Edward A. Lee
@version $Id$
@since Ptolemy II 2.1
@Pt.ProposedRating Green (eal)
@Pt.AcceptedRating Green (neuendor)
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

        // Icon is a stop sign.
        _attachText("_iconDescription",
                "<svg>\n" + "<polygon points=\"-8,-19 8,-19 19,-8 19,8 8,19 "
                + "-8,19 -19,8 -19,-8\" " + "style=\"fill:red\"/>\n"
                + "<text x=\"-15\" y=\"4\""
                + "style=\"font-size:11; fill:white; font-family:SansSerif\">"
                + "STOP</text>\n" + "</svg>\n");

        // Hide the name because the name is in the icon.
        _hideName = new SingletonParameter(this, "_hideName");
        _hideName.setToken(BooleanToken.TRUE);
        _hideName.setVisibility(Settable.EXPERT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The parameter that hides the name of the actor.  The default
     * value is true.
     */
    public SingletonParameter _hideName;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read one token from each input channel that has a token,
     *  and if any token is true, call finish() on the manager.
     *  If nothing at all is connected to the input port, then
     *  call finish() unconditionally.
     *  @exception IllegalActionException If there is no director or
     *   if there is no manager, or if the container is not a
     *   CompositeActor.
     *  @return False if a stop is requested, and true otherwise.
     */
    public boolean postfire() throws IllegalActionException {
        boolean result = false;

        if (input.getWidth() == 0) {
            result = true;
        }

        // NOTE: We need to consume data on all channels that have data.
        // If we don't then DE will go into an infinite loop.
        for (int i = 0; i < input.getWidth(); i++) {
            if (input.hasToken(i)) {
                if (((BooleanToken) input.get(i)).booleanValue()) {
                    result = true;
                }
            }
        }

        if (result) {
            Nameable container = getContainer();

            if (container instanceof CompositeActor) {
                Manager manager = ((CompositeActor) container).getManager();

                if (manager != null) {
                    manager.finish();
                } else {
                    throw new IllegalActionException(this,
                            "Cannot stop without a Manager.");
                }
            } else {
                throw new IllegalActionException(this,
                        "Cannot stop without a container that is a "
                        + "CompositeActor.");
            }
        }

        return !result;
    }
}
