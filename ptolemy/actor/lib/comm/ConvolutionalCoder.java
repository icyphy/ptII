/* Generate a convolutional code

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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (bilung@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.comm;

import ptolemy.domains.sdf.lib.SDFTransformer;
import ptolemy.actor.lib.Transformer;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;

import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.ArrayType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// ConvolutionalCoder
/**
Generate a convolutional code by passing the information sequence to be
transmitted through a linear finite-state shift register.
The initial state of the shift register is given by the <i>initial</i>
parameter, which should be a non-negative integer.
The number of bits per time that should be shifted into and along the
shift register is given by the <i>constraintLength</i> parameter, which
should be a positive integer.
The polynomials are given by the <i>polynomialArray</i> parameter, which
should be an array of positive integers. Each integer indicates one
polynomial.
The n-th bit of the polynomial indicates whether the n-th tap of the delay
line should be taken to compute the exclusive-ored parity.
The result is produced as a sequence of length <i>N</i>, where <i>N</i>
is the length of the <i>polynomialArray</i>. The n-th bit in the sequence
corresponds to the parity computed from the n-th polynomial
<p>
Note in this actor types of input and output port are set to be boolean. 
True and false are converted to 1 and 0 before computing the parity.
The results are converted back to boolean and then sent to the output port.
<p>
The leading zero in each polynomial indicates an octal number.
The order is simply the index of the highest-order non-zero in the polynomial,
where the low-order bit has index zero.
Since the polynomial and the shift register are both implemented using type
"int", the order of the polynomial is limited by the size of the "int" data
type. For simplicity and portability, the polynomial is not allowed to be
intepreted as a negative integer, so the sign bit cannot be used.
Since Java has 32-bit integers, the highest order polynomial allowed
is 30 (recall that indexing for the order starts at zero, and we cannot
use the sign bit).
<p>
For more information on convolutional codes, see Proakis, Digital
Communications, Fourth Edition, McGraw-Hill, 2001, pp. 471-477.
<p>
@author Edward A. Lee and Rachel Zhou
@version $Id: ConvolutionalCoder.java
*/

public class ConvolutionalCoder extends SDFTransformer {

    /** Construct an actor with the given container and name.
     *  The output and trigger ports are also constructed.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ConvolutionalCoder(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        constraintLength = new Parameter(this, "constraintLength");
        constraintLength.setTypeEquals(BaseType.INT);
        constraintLength.setExpression("1");

        polynomialArray = new Parameter(this, "polynomialArray");
        polynomialArray.setTypeEquals(new ArrayType(BaseType.INT));
        polynomialArray.setExpression("{03}");

        initial = new Parameter(this, "initial");
        initial.setTypeEquals(BaseType.INT);
        initial.setExpression("0");

        // Declare data types, consumption rate and production rate.
        input.setTypeEquals(BaseType.BOOLEAN);
        input.setTokenConsumptionRate(1);
        output.setTypeEquals(BaseType.BOOLEAN);
        output.setTokenProductionRate(1);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

     /** An array of integers defining an array of polynomials with
      *  binary coefficients. The coefficients indicate the presence (1)
      *  or absence (0) of a tap in the shift register. Each element
      *  of this array parameter should be a positive integer.
      *  The array's default value is {03}.
      */
    public Parameter polynomialArray;

    /** Integer defining the intial state of the shift register.
     *  The n-th bit of the integer indicates the value of the
     *  n-th register. This parameter should be a non-negative
     *  integer. Its default value is the integer 0.
     */
    public Parameter initial;

    /** Integer defining the number of bits that the shift register
     *  takes in each time. It should be a positive integer. Its
     *  default value is the integer 1.
     */
    public Parameter constraintLength;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute being changed is <i>initial</i>, then verify
     *  that it is a non-negative integer; if it is <i>constraintLength</i>,
     *  then verify that it is a positive integer; if it is
     *  <i>polynomailArray</i>, then verify that each of its elements is
     *  a positive integer.
     *  @exception IllegalActionException If <i>initial</i> is negative
     *  or <i>constraintLength</i> is non-positive or any element of
     *  <i>polynomialArray</i> is non-positive.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == initial) {
            int seed = ((IntToken)initial.getToken()).intValue();
            if (seed < 0 ) {
                throw new IllegalActionException(this,
                "shift register's value must be non-negative.");
            }
        } else if (attribute == constraintLength) {
            int length = ((IntToken)constraintLength.getToken()).intValue();
            if (length < 1 ) {
                throw new IllegalActionException(this,
                        "constraintLength must be non-negative.");
            }
            input.setTokenConsumptionRate(length);
        } else if (attribute == polynomialArray) {
            ArrayToken maskToken = ((ArrayToken)polynomialArray.getToken());
            int maskNumber = maskToken.length();
            for(int i = 0; i < maskNumber; i++) {
                int mask = ((IntToken)maskToken.getElement(i)).intValue();
                if (mask <= 0) {
                    throw new IllegalActionException(this,
                    "Polynomial is required to be strictly positive.");
                }
            }
            output.setTokenProductionRate(maskNumber);
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Read "<i>constraintLength</i>" bits from the input port and shift
     *  them into the shift register. Compute the parity for each polynomial
     *  specified in <i>polynomialArray</i>. If the parity is 1, convert it
     *  to "true"; otherwise convert it to "false". Send these boolean
     *  values in sequence to the output port. The n-th bit corresponds to
     *  the parity computed using the n-th polynomial.
     */
    public void fire() throws IllegalActionException {
        _latestShiftReg = _shiftReg;

        // Read from the input port.
        int length = ((IntToken)constraintLength.getToken()).intValue();
        Token[] inputToken = (Token[])input.get(0, length);

        // Read the polynomial array.
        ArrayToken maskToken = ((ArrayToken)polynomialArray.getToken());
        int maskNumber = maskToken.length();
        BooleanToken[] result = new BooleanToken[maskNumber];

        // Shift the input bits into the shift register.
        int reg = _latestShiftReg;
        for (int i = 0; i < length; i++) {
             reg = reg << 1;
             BooleanToken input = (BooleanToken)inputToken[i];
            if (input.booleanValue()) {
                reg = reg | 1;
            }
        }
        _latestShiftReg = reg;

        // Compute parity for each polynomial.
        for (int i = 0; i < maskNumber; i++) {
            int mask = ((IntToken)maskToken.getElement(i)).intValue();
            int parity = _calculateParity(mask, reg);
            if (parity == 1){
                result[i] = BooleanToken.TRUE;
            }else{
                result[i] = BooleanToken.FALSE;
            }
        }

        output.broadcast(result, result.length);
    }


    /** Initialize the actor by resetting the shift register state
     *  equal to the value of <i>initial</i>
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _latestShiftReg = _shiftReg =
            ((IntToken)initial.getToken()).intValue();
    }

    /** Record the most recent shift register state as the new
     *  initial state for the next iteration.
     *  @exception IllegalActionException If the base class throws it
     */
    public boolean postfire() throws IllegalActionException {
        _shiftReg = _latestShiftReg;
        return super.postfire();
    }

    //////////////////////////////////////////////////////////
    ////            private methods                        ////

    /** Calculate the parity given by the polynomial and the
     *  state of shift register.
     *  @param mask Polynomial.
     *  @param reg State of shift register.
     *  @return Parity.
     */
    private int _calculateParity(int mask, int reg) {

        int masked = mask & reg;
        // Find the parity of the "masked".
        int parity = 0;
        // Calculate the parity of the masked word.
        while (masked > 0){
            parity = parity ^ (masked & 1);
            masked = masked >> 1;
        }

        return parity;
    }

    //////////////////////////////////////////////////////////////
    ////                     private variables                ////

    // Record the state of the shift register.
    private int _shiftReg;

    // Updated state of the shift register.
    private int _latestShiftReg;

}
