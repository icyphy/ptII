/* Encode an input sequence with a convolutional code.

 Copyright (c) 2003 The Regents of the University of California.
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
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.hdf.kernel;

import ptolemy.actor.Director;
import ptolemy.actor.lib.Transformer;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// HDFActor
/**
FIXME
<p>
@author Rachel Zhou
@version $Id$
@since Ptolemy II 3.0
@see Scrambler
@see ViterbiDecoder
*/
public class HDFActor extends Transformer {

    /** Construct an actor with the given container and name.
     *  The output and trigger ports are also constructed.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public HDFActor(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        rate = new PortParameter(this, "rate");
        rate.setTypeEquals(BaseType.INT);
        rate.setExpression("1");

        // Declare data types, consumption rate and production rate.
        _inputRate = new Parameter(input, "tokenConsumptionRate",
                new IntToken(1));
        _outputRate = new Parameter(output, "tokenProductionRate",
                new IntToken(1));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Consumption and production rate of this actor.
     */
    public PortParameter rate;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute being changed is <i>uncodedRate</i>,
     *  then verify that it is a positive integer; if it is
     *  <i>polynomialArray</i>, then verify that each of its elements is
     *  a positive integer and find the maximum value among them, which
     *  is used to compute the highest order among all polynomials.
     *  @exception IllegalActionException If <i>uncodedRate</i> is
     *  non-positive or any element of <i>polynomialArray</i> is non-positive.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == rate) {
            _rateValue = ((IntToken)rate.getToken()).intValue();
            if (_rateValue < 0 ) {
                throw new IllegalActionException(this,
                        "rate must be non-negative.");
            }
            _outputRate.setToken(new IntToken(_rateValue));
            _inputRate.setToken(new IntToken(_rateValue));
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Read <i>uncodedRate</i> bits from the input port and shift
     *  them into the shift register. Compute the parity for each
     *  polynomial specified in <i>polynomialArray</i>. Send the results
     *  in sequence to the output. The i-th bit in the output 
     *  corresponds to the parity computed using the i-th polynomial.
     */
    public void fire() throws IllegalActionException {

        Token[] inputToken = (Token[])input.get(0, _rateValue);
        output.broadcast(inputToken, _rateValue);
    }


    /** Initialize the actor by resetting the shift register state
     *  equal to the value of <i>initialState</i>.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void preinitialize() throws IllegalActionException {
        //rate.update();
        _rateValue = ((IntToken)rate.getToken()).intValue();
        _outputRate.setToken(new IntToken(_rateValue));
        _inputRate.setToken(new IntToken(_rateValue));
    }

    /** Record the most recent shift register state as the new
     *  state for the next iteration.
     *  @exception IllegalActionException If the base class throws it
     */
    public boolean postfire() throws IllegalActionException {
        rate.update();
        _rateValue = ((IntToken)rate.getToken()).intValue();
        _outputRate.setToken(new IntToken(_rateValue));
        _inputRate.setToken(new IntToken(_rateValue));
        Director director = getDirector();
        if (director instanceof HDFDirector) {
            ((HDFDirector)director).invalidateSchedule();
             //System.out.println("invalidate HDF schedule");
        }
        return super.postfire();
    }

    //////////////////////////////////////////////////////////
    ////            private methods                        ////


    //////////////////////////////////////////////////////////////
    ////           private variables                          ////

    // Consumption rate of the input port.
    private Parameter _inputRate;

    // Production rate of the output port.
    private Parameter _outputRate;

    // Record the state of the shift register.
    private int _rateValue;

}
