/* Scramble an input bit sequence in a pseudo random way.

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
//// Scrambler

/**
 Scramble the input bit sequence using a feedback shift register.
 The initial state of the shift register is given by the <i>initialState</i>
 parameter, which should be a non-negative integer.
 The taps of the feedback shift register are given by the <i>polynomial</i>
 parameter, which should be a positive integer.
 The n-th bit of this integer indicates whether the n-th tap of the delay
 line is fed back.
 The low-order bit is called the 0-th bit, and should always be set.
 The next low-order bit indicates whether the output of the first delay
 should be fed back, etc.
 The input port receives boolean tokens. "TRUE" is treated as 1 and "FALSE"
 is treated as 0. All the bits that are fed back are exclusive-ored together
 (i.e., their parity is computed), and the result is exclusive-ored with the
 input bit. The result is produced at the output and shifted into the delay line.
 <p>
 With a proper choice of polynomial, the resulting output appears highly
 random even if the input is highly non-random.
 If the polynomial is a <i>primitive polynomial</i>, then the feedback
 shift register is a so-called <i>maximal length feedback shift register</i>.
 This means that with a constant input (or no input, which is equivalent
 to a constant <i>false</i> input) the output will be a sequence with
 period 2<sup><i>N</i></sup>-1, where <i>N</i> is the order of the
 polynomial (the length of the shift register).
 This is the longest possible sequence.
 Moreover, within this period, the sequence will appear to be white,
 in that a computed autocorrelation will be very nearly an impulse.
 Thus, the scrambler with a constant input can be very effectively used
 to generate a pseudo-random bit sequence.
 <p>
 The maximal-length feedback shift register with constant input will
 pass through 2<sup><i>N</i></sup>-1 states before returning to a state
 it has been in before.  This is one short of the 2<sup><i>N</i></sup>
 states that a register with <i>N</i> bits can take on.  This one missing
 state, in fact, is a <i>lock-up</i> state, in that if the input is
 an appropriate constant, the scrambler will cease to produce random-looking
 output, and will output a constant. For example, if the input is all zeros,
 and the initial state of the scrambler is zero, then the outputs will be all
 zero, hardly random. This is easily avoided by initializing the scrambler
 to some non-zero state. The default value for the <i>shiftReg</i> is set to 1.
 <p>
 The <i>polynomial</i> must be carefully chosen. It must represent a
 <i>primitive polynomial</i>, which is one that cannot be factored into two
 (nontrivial) polynomials with binary coefficients.  See Lee and Messerschmitt
 (Kluwer, 1994) for more details.  For convenience, we give here
 a set of primitive polynomials
 (expressed as octal numbers so that they are easily translated into taps
 on shift register).  All of these will result in maximal-length pseudo-random
 sequences if the input is constant and lock-up is avoided:
 <pre>
 order    polynomial
 2        07
 3        013
 4        023
 5        045
 6        0103
 7        0211
 8        0435
 9        01021
 10       02011
 11       04005
 12       010123
 13       020033
 14       042103
 15       0100003
 16       0210013
 17       0400011
 18       01000201
 19       02000047
 20       04000011
 21       010000005
 22       020000003
 23       040000041
 24       0100000207
 25       0200000011
 26       0400000107
 27       01000000047
 28       02000000011
 29       04000000005
 30       010040000007
 </pre>
 <p>
 The leading zero in the polynomial indicates an octal number.
 Note also that reversing the order of the bits in any of these numbers
 will also result in a primitive polynomial.
 Thus, the default value for the polynomial parameter
 is 0440001 in octal, or "100 100 000 000 000 001" in binary.
 Reversing these bits we get "100 000 000 000 001 001" in binary, or
 0400011 in octal.
 This latter number is the one listed above as the primitive polynomial
 of order 17.
 The order is simply the index of the highest-order non-zero in the polynomial,
 where the low-order bit has index zero.
 <p>
 Since the polynomial and the feedback shift register are both implemented
 using type "int", the order of the polynomial is limited by the size of
 the "int" data type.
 For simplicity and portability, the polynomial is not allowed to be
 interpreted as a negative integer, so the sign bit cannot be used.
 Thus, if "int" is a 32-bit word, then the highest order polynomial allowed
 is 30 (recall that indexing for the order starts at zero, and we cannot
 use the sign bit).
 Java has 32-bit integers, so we give the primitive
 polynomials above only up to order 30.
 <p>
 For more information on scrambler, see Lee and Messerschmitt, Digital
 Communication, Second Edition, Kluwer Academic Publishers, 1994, pp. 595-603.
 <p>
 @author Edward A. Lee and Ye Zhou
 @version $Id$
 @since Ptolemy II 3.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class Scrambler extends Transformer {
    /** Construct an actor with the given container and name.
     *  The output and trigger ports are also constructed.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Scrambler(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        polynomial = new Parameter(this, "polynomial");
        polynomial.setTypeEquals(BaseType.INT);
        polynomial.setExpression("0440001");

        initialState = new Parameter(this, "initialState");
        initialState.setTypeEquals(BaseType.INT);
        initialState.setExpression("1");

        // Create input port and declare data types.
        //input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(BaseType.BOOLEAN);
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

    /** Read a bit from the input port and shift it into the shift register
     *  to scramble. Compute the parity and send <i>true</i> to the output
     *  port if it is 1; otherwise send <i>false</i> to the output port.
     *  The parity is shifted into the delay line for the next iteration.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        _latestShiftReg = _shiftReg;

        int mask = ((IntToken) polynomial.getToken()).intValue();
        int reg = _latestShiftReg << 1;
        int masked = mask & reg;

        // Find the parity of the "masked".
        int parity = 0;

        // Calculate the parity of the masked word.
        while (masked > 0) {
            parity = parity ^ masked & 1;
            masked = masked >> 1;
        }

        // Exclusive-or with the input if there is any.
        for (int i = 0; i < input.getWidth(); i++) {
            if (input.hasToken(0)) {
                BooleanToken inputToken = (BooleanToken) input.get(0);
                boolean inputTokenValue = inputToken.booleanValue();
                parity = parity ^ (inputTokenValue ? 1 : 0);
            }
        }

        _latestShiftReg = reg | parity;

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
     *  @exception IllegalActionException If the base class throws it
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
