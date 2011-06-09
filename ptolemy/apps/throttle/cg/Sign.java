/* If the argument is greater than 0, return 1.0, if
it is less than 0, return -1.0, otherwise return 0.0.

 Copyright (c) 1998-2001 The Regents of the University of California.
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

@ProposedRating Red (chf@eecs.berkeley.edu)
@AcceptedRating Red (janneck@eecs.berkeley.edu)
*/
package ptolemy.apps.throttle.cg;

import ptolemy.actor.AtomicActor;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.Transformer;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;


//////////////////////////////////////////////////////////////////////////
//// Sign

/**
If the argument is greater than 0, return 1.0, if
it is less than 0, return -1.0, otherwise return 0.0.

The input and output types are DoubleToken.

<p>This actor is necessary because codegen does not work
with the MathFunction Actor.

@author C. Fong, Christopher Hylands
@version $Id$
@see ptolemy.actor.lib.MathFunction
*/
public class Sign extends Transformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Sign(CompositeEntity container, String name)
        throws NameDuplicationException, IllegalActionException {
        super(container, name);

        input.setTypeEquals(BaseType.DOUBLE);
        output.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Consume at most one input token from each input channel, and
     *  compute the specified math function of the input.
     *  If there is no input, then produce no output.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        if (input.hasToken(0)) {
            double inputValue = ((DoubleToken) input.get(0)).doubleValue();
            double result;

            if (inputValue > 0) {
                result = 1.0;
            } else if (inputValue < 0) {
                result = -1.0;
            } else {
                result = 0.0;
            }

            output.send(0, new DoubleToken(result));
        }
    }

    /** Invoke a specified number of iterations of this actor. Each
     *  iteration computes the math function specified by the
     *  <i>function</i> parameter on a single token. An invocation
     *  of this method therefore applies the function to <i>count</i>
     *  successive input tokens.
     *  <p>
     *  This method should be called instead of the usual prefire(),
     *  fire(), postfire() methods when this actor is used in a
     *  domain that supports vectorized actors.  This leads to more
     *  efficient execution.
     *  @param count The number of iterations to perform.
     *  @return COMPLETED if the actor was successfully iterated the
     *   specified number of times. Otherwise, return NOT_READY, and do
     *   not consume any input tokens.
     *  @exception IllegalActionException Not thrown in this base class
     */
    public int iterate(int count) throws IllegalActionException {
        // Check whether we need to reallocate the output token array.
        Token[] inArray1;

        if (count > _resultArray.length) {
            _resultArray = new DoubleToken[count];
        }

        if (input.hasToken(0, count)) {
            inArray1 = input.get(0, count);

            for (int i = 0; i < count; i++) {
                double inputValue = ((DoubleToken) (inArray1[i])).doubleValue();
                double result;

                if (inputValue > 0) {
                    result = 1.0;
                } else if (inputValue < 0) {
                    result = -1.0;
                } else {
                    result = 0.0;
                }

                _resultArray[i] = new DoubleToken(result);
            }

            output.send(0, _resultArray, count);
            return COMPLETED;
        } else {
            return NOT_READY;
        }

        // Note: constants COMPLETED and NOT_READY are defined in
        // ptolemy.actor.Executable
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private DoubleToken[] _resultArray = new DoubleToken[0];
}
