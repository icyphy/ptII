/* An actor that delays the input by the specified amount.

 Copyright (c) 1998-1999 The Regents of the University of California.
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

package ptolemy.domains.de.lib;

import ptolemy.domains.de.kernel.*;
import ptolemy.domains.de.lib.DETransformer;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.actor.lib.SequenceActor;
import ptolemy.actor.TypedCompositeActor;

//////////////////////////////////////////////////////////////////////////
//// Delay
/**
This actor delays the input by a specified amount of time.
The time delay is required to be non-negative and has default value 1.0.
The input and output types are unconstrained, except that the
output type must be at least that of the input. The input
and output ports are both multiports, but they must have
the same width or the fire() method will throw an exception.
<p>
The actor assumes there is always an input token on each channel
for each iteration, or the fire() method will throw an exception.
Its behavior on each firing is to read a token from each input
channel, and to produce the token on the corresponding output
channel with the appropriate time delay.  The output is produced
in the postfire() method, consistent with the notion that persistent
state is only updated in postfire().  Notice that it produces
the output immediately, in the same iteration that it reads the
input, so that even if actor no longer exists
after the time delay elapses, the destination actor will still see
the token.
<p>
Occassionally, it is useful to set the time
delay to zero.  This causes the input tokens to be copied to the
output in a subsequent firing, but at the same global time.
It is sometimes useful to think of this as an infinitessimal delay.
This is accomplished by calling the fireAt() method of the director
with argument equal to current time. In particular, in the DE domain, 
this serves only to break the chain
of precedences when calculating priorities for dealing with simultaneous
events. Thus, this actor can be used in feedback loops when there are
no other delays.  It indicates to the DE scheduler where to break
the precedences.

@author Edward A. Lee, Lukito Muliadi
@version $Id$
*/
public class Delay extends DETransformer implements SequenceActor {

    /** Construct an actor with the specified container and name.
     *  @param container The composite actor to contain this one.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Delay(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        delay = new Parameter(this, "delay", new DoubleToken(1.0));
        delay.setTypeEquals(DoubleToken.class);
    }

    ///////////////////////////////////////////////////////////////////
    ////                       ports and parameters                ////

    /** The amount of delay.  This parameter must contain a DoubleToken
     *  with a non-negative value, or an exception will be thrown when
     *  it is set.
     */
    public Parameter delay;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read one token from each input channel and save it so that the
     *  postfire method can produce it to the output.
     *  @exception IllegalActionException If the input and output width
     *   are not the same.
     */
    public void fire() throws IllegalActionException {
        int width = input.getWidth();
        if (width != output.getWidth()) {
            throw new IllegalActionException(this,
            "Input and output width are not the same.");
        }
        if (_currentInputs == null || _currentInputs.length != width) {
            _currentInputs = new Token[width];
        }
        for(int i = 0; i < width; i++) {
            _currentInputs[i] = input.get(i);
        }
    }

    /** Produce as outputs the tokens that were read in the fire() method.
     *  The outputs are produced with a time offset equal to the value
     *  of the delay parameter.
     *  @exception IllegalActionException If the send() method of the output
     *   port throws it.
     */
    public boolean postfire() throws IllegalActionException {
        for(int i = 0; i < output.getWidth(); i++) {
            output.send(i, _currentInputs[i],
                ((DoubleToken)delay.getToken()).doubleValue());
        }
        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Current inputs.
    private Token[] _currentInputs;
}
