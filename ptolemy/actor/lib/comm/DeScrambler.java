/* Descramble the input bit sequence using a feedback shift register.

 Copyright (c) 2003-2014 The Regents of the University of California.
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
package ptolemy.actor.lib.comm;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// DeScrambler

/**
 Descramble the input bit sequence using a feedback shift register.
 The taps of the feedback shift register are given by the <i>polynomial</i>
 parameter. The initial state of the shift register is given by the
 <i>initialState</i> parameter. This is a self-synchronizing descrambler that
 will exactly reverse the operation of the Scrambler if the polynomials
 are the same. The low-order bit of the polynomial should always be set.
 For more information, see the documentation for the Scrambler actor
 and Lee and Messerschmitt, Digital Communication, Second Edition,
 Kluwer Academic Publishers, 1994, pp. 595-603.
 <p>
 @author Edward A. Lee and Ye Zhou
 @version $Id$
 @since Ptolemy II 3.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class DeScrambler extends Transformer {
    /** Construct an actor with the given container and name.
     *  The output and trigger ports are also constructed.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DeScrambler(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        polynomial = new Parameter(this, "polynomial");
        polynomial.setTypeEquals(BaseType.INT);
        polynomial.setExpression("0440001");

        initialState = new Parameter(this, "initialState");
        initialState.setTypeEquals(BaseType.INT);
        initialState.setExpression("1");

        // Declare input data type.
        input.setTypeEquals(BaseType.BOOLEAN);

        // Declare output data type.
        output.setTypeEquals(BaseType.BOOLEAN);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Integer defining a polynomial with binary coefficients.
     *  The coefficients indicate the presence (1) or absence (0)
     *  of a tap in a feedback shift register. This parameter should
     *  contain a positive integer with the lower-order bit being 1.
     *  Its default value is the integer 0440001.
     */
    public Parameter polynomial;

    /** Integer defining the initial state of the shift register.
     *  The n-th bit of the integer indicates the value of the
     *  n-th register. This parameter should be a non-negative
     *  integer. Its default value is the integer 1.
     */
    public Parameter initialState;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute being changed is <i>polynomial</i>, then
     *  verify that is a positive integer and the lower-order bit is 1.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If <i>polynomial</i> is
     *  non-positive or the lower-order bit is not 1.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == polynomial) {
            int mask = ((IntToken) polynomial.getToken()).intValue();

            if (mask <= 0) {
                throw new IllegalActionException(this,
                        "Polynomial is required to be strictly positive.");
            }

            if ((mask & 1) == 0) {
                throw new IllegalActionException(this,
                        "The low-order bit of the the polynomial is not set.");
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Read bit from the input port and fill it into the shift register
     *  to descramble. Compute the parity and send it to the output port.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        _latestShiftReg = _shiftReg;

        int mask = ((IntToken) polynomial.getToken()).intValue();
        BooleanToken inputToken = (BooleanToken) input.get(0);
        boolean inputTokenValue = inputToken.booleanValue();
        int reg = _latestShiftReg << 1;

        // Put the input in the low-order bit.
        reg = reg ^ (inputTokenValue ? 1 : 0);

        // Find the parity of "masked".
        int masked = mask & reg;
        int parity = 0;

        // Calculate the parity of the masked word.
        while (masked > 0) {
            parity = parity ^ masked & 1;
            masked = masked >> 1;
        }

        _latestShiftReg = reg;

        if (parity == 1) {
            output.broadcast(BooleanToken.TRUE);
        } else {
            output.broadcast(BooleanToken.FALSE);
        }
    }

    /** Initialize the actor by resetting the shift register state
     *  equal to the value of <i>initialState</i>.
     *  @exception IllegalActionException If the parent class throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _latestShiftReg = _shiftReg = ((IntToken) initialState.getToken())
                .intValue();
    }

    /** Record the most recent shift register state as the new
     *  initial state for the next iteration.
     *  @exception IllegalActionException If the base class throws it.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        _shiftReg = _latestShiftReg;
        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Record the state of the shift register.
    private int _shiftReg;

    // Updated state of the shift register.
    private int _latestShiftReg;
}
