/* An actor that repeats each input sample a specified number of times.

 Copyright (c) 1997-2003 The Regents of the University of California.
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
import ptolemy.actor.lib.Transformer;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// Repeat
/**
An actor that repeats a block of input tokens the specified number of times
on the output.  On each firing, it reads <i>blockSize</i> tokens
and repeats each block <i>numberOfTimes</i> times
on the output.  Note that this causes a sample rate increase by
a factor of <i>numberOfTimes</i>,
and hence affects the number of invocations of downstream actors.

@author Shankar Rao, Steve Neuendorffer
@version $Id$
@since Ptolemy II 1.0
*/

public class Repeat extends SDFTransformer {

    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this Repeat actor within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public Repeat(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // parameters
        numberOfTimes = new Parameter(this, "numberOfTimes", new IntToken(2));
        numberOfTimes.setTypeEquals(BaseType.INT);

        blockSize = new Parameter(this, "blockSize", new IntToken(1));
        blockSize.setTypeEquals(BaseType.INT);

        input_tokenConsumptionRate.setExpression("blockSize");
        output_tokenProductionRate.setExpression("numberOfTimes*blockSize");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The repetition factor.  It is of type integer and has a default
     *  value of 2.  It must be greater than zero.
     */
    public Parameter numberOfTimes;

    /** The number of tokens in a block.  It is of type integer and has a
     *  default value of 1.  It must be greater than zero.
     */
    public Parameter blockSize;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the argument is the <i>numberOfTimes</i> parameter or the
     *  <i>blockSize</i> parameter, then
     *  set the rate of the ports, and invalidate
     *  the schedule of the director.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the parameters are out of range.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == numberOfTimes || attribute == blockSize) {
            int repetitions = ((IntToken)numberOfTimes.getToken()).intValue();
            int count = ((IntToken)blockSize.getToken()).intValue();
            if (repetitions < 1)
                throw new IllegalActionException(numberOfTimes,
                        "The value of numberOfTimes must be positive, but "
                        + "was set to " + repetitions);

            if (count < 1)
                throw new IllegalActionException(blockSize,
                        "The value of blockSize must be positive, but "
                        + "was set to " + count);

        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Consume <i>blockSize</i> input tokens from the input port.
     *  Produce <i>blockSize*numberOfTimes</i>
     *  tokens on the output port, consisting of <i>numberOfTimes</i>
     *  repetitions of the input.  For example, if <i>blockSize</i> = 3 and
     *  <i>numberOfTimes</i> = 2, then on the following input:<br>
     *  <pre>  1 2 3 4 5 6</pre><br>
     *  two invocations of this method will send the following output:<br>
     *  <pre>  1 2 3 1 2 3 4 5 6 4 5 6</pre><br>
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        int repetitions = ((IntToken)numberOfTimes.getToken()).intValue();
        int count = ((IntToken)blockSize.getToken()).intValue();
        Token[] inputBlock = input.get(0, count);
        for (int i = 0; i < repetitions; i += 1) {
            output.send(0, inputBlock, count);
        }
    }
}
