/* A nonstrict actor that performs a specified logic operation on the input.

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

@ProposedRating Red (pwhitake@eecs.berkeley.edu)
@AcceptedRating Red (pwhitake@eecs.berkeley.edu)
*/

package ptolemy.domains.sr.lib;

import ptolemy.actor.lib.logic.LogicFunction;
import ptolemy.data.BooleanToken;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// NonStrictLogicFunction
/**
On each firing, produce an output token with a value that is
equal to the specified logic operator of the input(s) if that output
can be determined.
The functions are:
<ul>
<li> <b>and</b>: The logical and operator.
This is the default function for this actor.
<li> <b>or</b>: The logical or operator.
<li> <b>xor</b>: The logical xor operator.
<li> <b>nand</b>: The logical nand operator.
Equivalent to the negation of <i>and</i>.
<li> <b>nor</b>: The logical nor operator.
Equivalent to the negation of <i>or</i>.
<li> <b>xnor</b>: The logical xnor operator.
Equivalent to the negation of <i>xor</i>.
</ul>
<p>
NOTE: All operators have
a single input port, which is a multiport, and a single output port, which
is not a multiport.  All ports have type boolean.
<p>
This actor is nonstrict.  That is, it does not require that each input
channel have a token upon firing.  If the output can be determined from the
known inputs, the output will be produced.  If the output can not be
determined in the given firing, no output will be produced.  If all of the
inputs are known and absent, the output will be made known and absent.
At most one token is consumed on each input channel.

@author Paul Whitaker
@version $Id$
@since Ptolemy II 2.0
*/
public class NonStrictLogicFunction extends LogicFunction {

    /** Construct an actor with the given container and name.  Set the
     *  logic function to the default ("and").  Set the types of the ports
     *  to boolean.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public NonStrictLogicFunction(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        new Attribute(this, "_nonStrictMarker");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Consume at most one input token from each input channel,
     *  and produce a token on the output port if it can be determined.
     *  If there is no input on any channel, then produce no output.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        BooleanToken value = null;
        BooleanToken in = null;
        for (int i = 0; i < input.getWidth(); i++) {
            if (input.isKnown(i)) {
                if (input.hasToken(i)) {
                    in = (BooleanToken)(input.get(i));
                    if (in != null) value = _updateFunction(in, value);
                }
            }
        }

        if (value == null) {
            // If value is null, there were no inputs.  If all the inputs are
            // known, they must be all absent, so make the output absent.
            if (input.isKnown()) output.sendClear(0);
        } else {
            // If the value is not null, there were some inputs.  If some of
            // the inputs are unknown, the result might be invalid.  In that
            // case, nullify the result so no token is sent.
            value = _nullifyIncompleteResults(value);
        }

        if (value != null) {
            if (_negate) value = value.not();
            output.send(0, (BooleanToken)value);
        }
    }

    /** Explicitly declare which inputs and outputs are not dependent.
     *  
     */
    public void removeDependencies() {
        removeDependency(input, output);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Nullify results that cannot be asserted due to unknown inputs.
     */
    private BooleanToken _nullifyIncompleteResults(BooleanToken inValue)
            throws IllegalActionException {
        BooleanToken outValue = inValue;;
        if (!input.isKnown()) {
            switch(_function) {
            case _AND:
                // Cannot assert that the output of AND is true unless
                // all inputs are known.
                if (inValue.booleanValue()) outValue = null;
                break;
            case _OR:
                // Cannot assert that the output of OR is false unless
                // all inputs are known.
                if (!inValue.booleanValue()) outValue = null;
                break;
            case _XOR:
                // Cannot assert the output of XOR unless
                // all inputs are known.
                outValue = null;
                break;
            default:
                throw new InternalErrorException(
                        "Invalid value for _function private variable. "
                        + "LogicFunction actor (" + getFullName()
                        + ")"
                        + " on function type " + _function);
            }
        }
        return outValue;
    }

}
