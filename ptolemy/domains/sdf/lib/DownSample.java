/* Downsample a stream by the specified amount.

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
@AcceptedRating Yellow (neuendor@eecs.berkeley.edu)
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
//// DownSample
/**
This actor downsamples an input stream by an integer factor by
removing tokens.  The downsample factor is given by the <i>factor</i>
parameter. On each firing, this actor consumes <i>factor</i> tokens from the
input and sends only one of them to the output.  The one sent
depends on the <i>phase</i> parameter.  If <i>phase</i> is 0, then
the most recent one (the last one consumed) is sent.  If <i>phase</i>
is 1, then the next most recent one is sent. The value of <i>phase</i>
can range up to <i>factor</i>-1, in which case the first one consumed
is sent. By default, the <i>factor</i> parameter is 2,
so the input sample rate is twice that of the output.
The default value for <i>phase</i> is 0.
<p>
This actor is data polymorphic. It can accept any token
type on the input.

@see UpSample
@author Steve Neuendorffer, Edward A. Lee
@version $Id$
@since Ptolemy II 1.0
*/

public class DownSample extends SDFTransformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DownSample(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        // Set parameters.
        factor = new Parameter(this, "factor");
        factor.setExpression("2");

        phase = new Parameter(this, "phase");
        phase.setExpression("0");

        input_tokenConsumptionRate.setExpression("factor");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The number of input tokens to read per output token produced.
     *  This is an integer that defaults to 2 and must be greater than
     *  zero.
     */
    public Parameter factor;

    /** The phase of the output with respect to the input.
     *  This is an integer that defaults to 0 and must be between 0
     *  and <i>factor</i>-1. If <i>phase</i> = 0, the most recent
     *  sample is the output, while if <i>phase</i> = <i>factor</i>-1
     *  the oldest sample is the output.
     */
    public Parameter phase;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the argument is the <i>factor</i> parameter, then
     *  set the consumption rate of the input port, and invalidate
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

    /** Consume <i>factor</i> tokens from the input, and select one of
     *  them to send to the output based on the <i>phase</i>.
     *  @exception IllegalActionException If there is no director, or
     *   if the <i>phase</i> value is out of range.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        int factorValue = ((IntToken)factor.getToken()).intValue();
        Token[] valueArray = input.get(0, factorValue);

        int phaseValue = ((IntToken)phase.getToken()).intValue();
        if (phaseValue >= factorValue) {
            throw new IllegalActionException(this,
                    "Phase is out of range: " + phaseValue);
        }
        // Send the token.
        output.send(0, valueArray[factorValue - phaseValue - 1]);
    }
}
