/* A nonstrict actor that delays tokens by one iteration.

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
package ptolemy.actor.lib.vhdl;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.AbsentToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// RegisterSR

/**
 This actor provides a one-tick delay.  On each firing, it produces
 on the output port whatever value it read on the input port in the
 previous tick of the clock. If the input was absent on the previous
 tick of the clock, then the output will be absent. On the first tick,
 the output is <i>initialValue</i> if it is given, and absent otherwise.
 In contrast to the Pre actor, this actor is non-strict, and hence can
 break causality loops.  Whereas Pre provides a one-step delay of
 non-absent values, this actor simply delays by one clock tick.

 @see ptolemy.domains.sr.lib.Pre
 @see ptolemy.domains.sdf.lib.SampleDelay
 @see ptolemy.domains.de.lib.TimedDelay

 @author Paul Whitaker, Elaine Cheong, and Edward A. Lee
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Yellow (celaine)
 @Pt.AcceptedRating Yellow (cxh)
 */
public class RegisterSR extends SynchronousFixTransformer {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public RegisterSR(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(BaseType.FIX);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The input port.  The default type is Fix. */
    public TypedIOPort input;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the input known and there is a token on the input port,
     *  consume the token from the input port, and store it for output
     *  on the next iteration. Otherwise, store an AbsentToken for
     *  output on the next iteration.
     *  If a token was received on the previous iteration, output it to the
     *  receivers. Otherwise, notify the receivers that there will never be
     *  any token available in the current iteration.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (input.isKnown(0)) {
            Token result;
            if (input.hasToken(0)) {
                result = input.get(0);
            } else {
                result = AbsentToken.ABSENT;
            }
            sendOutput(output, 0, result);
        } else {
            output.resend(0);
        }
    }

    /** Override the base class to declare that the <i>input</i>
     *  port does not depend on the <i>output</i> in a firing.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        removeDependency(input, output);
    }
}
