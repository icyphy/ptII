/* Generate discrete events by sampling a CT signal whenever there
   is a trigger.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.TimedActor;
import ptolemy.actor.lib.Transformer;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.ct.kernel.CTDirector;
import ptolemy.domains.ct.kernel.CTEventGenerator;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// CTTriggeredSampler
/**
This actor samples the continuous input signal when there is a discrete
event presents at the "trigger" input.
The actor has a multi-input port and a multi-output port. Signals in
each input channel are sampled and produced to corresponding output
channel.
@author Jie Liu
@version $Id$
@since Ptolemy II 1.0
*/
public class CTTriggeredSampler extends Transformer
    implements CTEventGenerator, TimedActor {

    /** Construct an actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *
     *  @param container The container.
     *  @param name The actor's name
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If name coincides with
     *   an entity already in the container.
     */
    public CTTriggeredSampler(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input.setMultiport(true);
        new Parameter(input, "signalType",
                new StringToken("CONTINUOUS"));
        output.setMultiport(true);
        output.setTypeAtLeast(input);
        new Parameter(output, "signalType",
                new StringToken("DISCRETE"));
        trigger = new TypedIOPort(this, "trigger", true, false);
        trigger.setMultiport(false);
        new Parameter(trigger, "signalType",
                new StringToken("DISCRETE"));
        // The trigger input has a generic type.

        _attachText("_iconDescription", "<svg>\n" +
                "<rect x=\"-30\" y=\"-20\" "
                + "width=\"60\" height=\"40\" "
                + "style=\"fill:white\"/>\n"
                + "<polyline points=\"-30,10 2,10 2,0\"/>\n"
                + "<polyline points=\"-30,-10 -20,-10 -20,0 -10,0 10,-7\"/>\n"
                + "<polyline points=\"10,0 30,0\"/>\n"
                + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The input port for triggering. The port has a generic type.
     *  Only the presents of a token matters.
     */
    public TypedIOPort trigger;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the port types.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        CTTriggeredSampler newObject =
                (CTTriggeredSampler)super.clone(workspace);
        newObject.output.setTypeAtLeast(newObject.input);
        return newObject;
    }

    /** Emit the current events, which are the tokens of the latest input
     *  tokens if a trigger input is present.
     *  @exception IllegalActionException If the hasToken() query failed
     *  or tokens cannot be sent from the output.
     */
    public void fire() throws IllegalActionException {
        CTDirector director = (CTDirector)getDirector();
        if (director.isDiscretePhase() && trigger.hasToken(0)) {
            trigger.get(0);
            for (int i = 0;
                 i < Math.min(input.getWidth(), output.getWidth());
                 i++) {
                if (input.hasToken(i)) {
                    output.send(i, input.get(i));
                }
            }
        }
    }

    /** Return true if there is a trigger event.
     *  If the hasToken() query on the trigger input throws an exception
     *  then throw an InternalErrorException.
     *  @return True if there is a token in the trigger port.
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
