/* Upsample a signal by a specified amount.

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

@ProposedRating Yellow (neuendor@eecs.berkeley.edu)
@AcceptedRating Yellow (eal@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.lib;

import ptolemy.actor.Director;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// UpSample
/**
This actor upsamples an input stream by an integer factor by inserting
tokens with value zero.  The upsample factor is given by the
<i>factor</i> parameter. On each firing, this actor reads one
token from the input produces <i>factor</i> tokens on the output
port.  All but one of these is a zero-valued token of the same type
as the input.  The remaining one is the token read from the input.
The position of this remaining one is determined by the <i>phase</i>
parameter.  This parameter has a value between 0 and <i>factor</i>-1.
If it is 0, then the input token is the last output token.
If it is <i>factor</i>-1, then it is the first output, followed
by zeros. Thus, if this actor is followed by the DownSample
actor with the same <i>factor</i> and <i>phase</i>, the combination
has no effect.
<p>
By default, <i>factor</i> is 2, and <i>phase</i> is the expression
"factor-1".  This means that by default, the input token that is read
is the first one produced at the output.
<p>
This actor is data polymorphic. It can accept any token
type on the input that supports the zero() method,
and it sends output tokens of that type.

@see DownSample
@author Steve Neuendorffer, Edward A. Lee
@version $Id$
@since Ptolemy II 1.0
*/

public class UpSample extends SDFTransformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public UpSample(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        // Set parameters.
        factor = new Parameter(this, "factor");
        factor.setExpression("2");
        phase = new Parameter(this, "phase");
        phase.setExpression("factor-1");
        
        output_tokenProductionRate.setExpression("factor");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The number of output tokens to produced per input token read.
     *  This is an integer that defaults to 2 and must be greater than
     *  zero.
     */
    public Parameter factor;

    /** The phase of the output with respect to the input.
     *  This is an integer that defaults to <i>factor</i>-1 and must be
     *  between 0 and <i>factor</i>-1. If <i>phase</i> = 0, the input
     *  is the first output, while if <i>phase</i> = <i>factor</i>-1
     *  then the input is the last output.
     */
    public Parameter phase;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the argument is the <i>factor</i> parameter, then
     *  set the production rate of the output port, and invalidate
     *  the schedule of the director.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the parameters are out of range.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == factor) {
            int factorValue = ((IntToken)factor.getToken()).intValue();
            if (factorValue <= 0) {
                throw new IllegalActionException(this,
                        "Invalid factor: " + factorValue);
            }
 
        } else if (attribute == phase) {
            int phaseValue = ((IntToken)phase.getToken()).intValue();
            if (phaseValue < 0) {
                throw new IllegalActionException(this,
                        "Invalid phase: " + phaseValue);
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Consume the input Token and produce the same token on the output.
     *  Then create a number of zero tokens of the same type as the
     *  input token on the output port, so that output.tokenProductionRate
     *  tokens are created in total.  If there is not token on the input,
     *  then this method throws a NoTokenException (which is a runtime
     *  exception).
     *  @exception IllegalActionException If a runtime type conflict occurs.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        Token token = input.get(0);
        int factorValue = ((IntToken)factor.getToken()).intValue();
        int phaseValue = ((IntToken)phase.getToken()).intValue();
        if (phaseValue >= factorValue) {
            throw new IllegalActionException(this,
                    "Phase is out of range: " + phaseValue);
        }

        Token[] result = new Token[factorValue];
        Token zero = token.zero();
        for (int i = 0; i < factorValue; i++) {
            if (i == phaseValue) {
                result[i] = token;
            } else {
                result[i] = zero;
            }
        }
        output.send(0, result, factorValue);
    }
}
