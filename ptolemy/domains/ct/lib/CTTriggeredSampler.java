/* Generate discrete events by periodically sampling a CT signal.

 Copyright (c) 1998-2000 The Regents of the University of California.
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
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.ct.lib;
import ptolemy.domains.ct.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.*;
import ptolemy.actor.*;
import ptolemy.actor.lib.TimedActor;


//////////////////////////////////////////////////////////////////////////
//// CTTriggeredSampler
/**
This actor samples the continuous input signal when there is a discrete
event presents at the "trigger" input. 
The actor has a multi-inputport and a multi-outputport. Singals in
each input channel are sampled and produced to corresponding output
channel.
@author Jie Liu
@version $Id$
*/
public class CTTriggeredSampler extends TypedAtomicActor
    implements CTEventGenerator, TimedActor {

    /** Construct an actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  The actor can be either dynamic, or not.  It must be set at the
     *  construction time and can't be changed thereafter.
     *  A dynamic actor will produce a token at its initialization phase.
     *
     *  @param CompositeActor The subsystem that this actor is lived in
     *  @param name The actor's name
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If name coincides with
     *   an entity already in the container.
     */
    public CTTriggeredSampler(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(true);
        input.setTypeEquals(BaseType.DOUBLE);
        output = new TypedIOPort(this, "output", false, true);
        output.setMultiport(true);
        output.setTypeEquals(BaseType.DOUBLE);
        trigger = new TypedIOPort(this, "trigger", true, false);
        trigger.setMultiport(false);
        // The trigger input has a generic type.
        // trigger.setTypeEquals(BaseType.DOUBLE);
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public variables                       ////

    /** The multi-input port with type double.
     */
    public TypedIOPort input;

    /** The multi-output port with type double.
     */
    public TypedIOPort output;

    /** The input port for triggering.
     */
    public TypedIOPort trigger;


    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Emit the current event, which has the token of the latest input
     *  token.
     */
    public void emitCurrentEvents() {
        try {
            if (trigger.hasToken(0)) {
                trigger.get(0);
                for (int i = 0; 
                     i < Math.min(input.getWidth(), output.getWidth());
                     i++) {
                    if(input.hasToken(i)) {
                        output.send(i, input.get(i));
                    }
                }
            }
        } catch (IllegalActionException e) {
            throw new InternalErrorException("Token mismatch.");
        }
    }

    /** Return true if there is a trigger event.
     */
    public boolean hasCurrentEvent() {
        try {
            if (trigger.hasToken(0)) return true;
        } catch (IllegalActionException e) {
            throw new InternalErrorException("Token mismatch.");
        }
        return false;
    }
}
