/* An actor that limits the input to a specified range.

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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (yuhong@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// Limiter
/**
Produce an output token on each firing with a value that is
equal to the input if the input lies between the <i>bottom</i> and
<i>top</i> parameters.  Otherwise, if the input is greater than <i>top</i>,
output <i>top</i>.  If the input is less than <i>bottom</i>, output
<i>bottom</i>.  This actor operates on doubles only.

@author Edward A. Lee
@version $Id$
@since Ptolemy II 2.0
*/

public class Limiter extends Transformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Limiter(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        bottom = new Parameter(this, "bottom");
        bottom.setExpression("0.0");
        bottom.setTypeEquals(BaseType.DOUBLE);

        top = new Parameter(this, "top");
        top.setExpression("1.0");
        top.setTypeEquals(BaseType.DOUBLE);

        input.setTypeEquals(BaseType.DOUBLE);
        output.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The bottom of the limiting range.  This is a double with default
     *  value 0.0.
     */
    public Parameter bottom;

    /** The top of the limiting range.  This is a double with default
     *  value 1.0.
     */
    public Parameter top;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Compute the output and send it to the output port. If there is
     *  no input, then produce no output.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        if (input.hasToken(0)) {
            DoubleToken in = (DoubleToken)input.get(0);
            double inValue = in.doubleValue();
            if (inValue < ((DoubleToken)bottom.getToken()).doubleValue()) {
                output.send(0, bottom.getToken());
            } else if (inValue > ((DoubleToken)top.getToken()).doubleValue()) {
                output.send(0, top.getToken());
            } else {
                output.send(0, in);
            }
        }
    }
}
