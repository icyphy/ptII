/* Align Smooth tokens by extrapolating.

 Copyright (c) 1997-2014 The Regents of the University of California.
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
package ptolemy.domains.qss.lib;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.Transformer;
import ptolemy.actor.util.Time;
import ptolemy.data.SmoothToken;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

///////////////////////////////////////////////////////////////////
//// Align

/**
 Given any new input event, produce on the output channels the current value
 of all the corresponding input channels. If an input channel has not yet
 received any value and has no default value, then no output is produced.
 If any input channel has most recently received a {@link SmoothToken},
 then the value of that token is extrapolated to the current time to get
 the current value.
 <p>
 This actor has a <i>trigger</i> port, which, if connected, will determine
 when outputs are sent. That is, if this port is connected, then outputs
 will be produced only when an event is received on some channel of the
 trigger port.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 11.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 @deprecated No longer needed. Use Sampler.
 */
public class Align extends Transformer {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public Align(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input.setMultiport(true);
        output.setMultiport(true);

        input.setWidthEquals(output, true);

        trigger = new TypedIOPort(this, "trigger", true, false);
        trigger.setMultiport(true);
        StringAttribute cardinality = new StringAttribute(trigger, "_cardinal");
        cardinality.setExpression("SOUTH");

        _attachText("_iconDescription", "<svg>\n"
                + "<polygon points=\"-10,20 10,10 10,-10, -10,-20\" "
                + "style=\"fill:yellow\"/>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The trigger port, which has undeclared type. If this port
     *  is connected, then events on this port determine when outputs
     *  are produced.
     */
    public TypedIOPort trigger;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Consume and record all available inputs and produce the current
     *  value on all output channels.
     *  @exception IllegalActionException If there is no director or
     *   if the number of input channels does not equal the number of
     *   output channels.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        
        // Check the trigger port.
        boolean hasTrigger = false;
        boolean triggerConnected = false;
        for (int i = 0; i < trigger.getWidth(); i++) {
            triggerConnected = true;
            while (trigger.hasNewToken(i)) {
                trigger.get(i);
                hasTrigger = true;
            }
        }

        int width = input.getWidth();
        for (int i = 0; i < width; i++) {
            while (input.hasNewToken(i)) {
        	_received[i] = input.get(i);
            }
        }
        if (!triggerConnected || hasTrigger) {
            Time time = getDirector().getModelTime();
            Token[] result = SmoothToken.align(_received, time);
            for (int i = 0; i < width; i++) {
        	if (result[i] != null) {
        	    output.send(i, result[i]);
        	}
            }
        }
    }
    
    /** Initialize this actor by clearing any stored inputs.
     *  @throws IllegalActionException If the width of the input does
     *   not match the width of the output.
     */
    public void initialize() throws IllegalActionException {
	super.initialize();
        int outWidth = output.getWidth();
        int inWidth = input.getWidth();

        if (inWidth != outWidth) {
            throw new IllegalActionException(this,
                    "Unequal Align channels: " + inWidth
                    + " inputs and " + outWidth + " outputs.");
        }
        _received = new Token[inWidth];
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////

    /** Most recently received token on all input channels. */
    private Token[] _received;
}
