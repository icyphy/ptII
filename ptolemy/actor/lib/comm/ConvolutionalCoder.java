/* Generate a convolutional code

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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.comm;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// ConvolutionalCoder
/**
Generate a convolutional code by passing the information sequence to be
transmitted through a linear finite-state shift register.
The initial state of the shift register is given by the <i>initial</i>
parameter, which should be a non-negative integer.
The <i>uncodeBlockSize</i> parameter, denoted by "k", is the number of
bits per firing that should be shifted into and along the shift register.
It should be a positive integer. We call a k-bit block of input sequence
as an <i>information symbol</i>.
The <i>polynomialArray</i> parameter should be an array of positive
integers. Each integer indicates one polynomial used for computing
parity. The leading zero in each polynomial indicates it is an octal
number. The i-th bit of the polynomial indicates whether the i-th
tap of the delay line should be taken to compute the exclusive-ored
parity. See more details in Scrambler actor on using an integer to
define a polynomial.
The "n" parity results are produced in a sequence, where "n" is the
length of the <i>polynomialArray</i>. The i-th bit in the sequence
corresponds to the parity computed from the i-th polynomial. We call such
an n-bit block of result as a <i>codeword</i>.
<p>
Like Scrambler, the input port accepts a sequence of booleans. When computing
parities, the actor treats "true" as 1 and "false" as 0. The output port
produces the encoded bits also into booleans.
<p>
Therefore, during each firing the encoder consumes "k" bits and produces
"n" bits. The rate of this convolutional code is k/n.
<p>
A good convolutional code should have large Hamming distance between
any two of its codewords. This generally cannot be easily observed
unless by checking its complete code book. However, there are some
basic lines that all "good" codes should satisfy:
<ol>
<li> "k" should be strictly smaller than "n", otherwise the code is
not uniquely decodable.
<li> "k" should not be higher than the highest order of all polynomials,
</ol>
otherwise, some bits never get involved in computing parities.
In the above two cases, the actor will throw an exception. However, they
do not guarantee the codeword can be decoded successfully, and it is
not always true that "the larger the polynomials are, the better."
Users should check tables for convolutional code from professional
references, which are achieved using computer search methods.
<p>
For more information on convolutional codes, see Proakis, Digital
Communications, Fourth Edition, McGraw-Hill, 2001, pp. 471-477.
<p>
@author Rachel Zhou
@version $Id$
@since Ptolemy II 3.0
*/
public class ConvolutionalCoder extends Transformer {

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

        uncodeBlockSize = new Parameter(this, "uncodeBlockSize");
        uncodeBlockSize.setTypeEquals(BaseType.INT);
        uncodeBlockSize.setExpression("1");

        polynomialArray = new Parameter(this, "polynomialArray");
        polynomialArray.setTypeEquals(new ArrayType(BaseType.INT));
        polynomialArray.setExpression("{05, 07}");

        initial = new Parameter(this, "initial");
        initial.setTypeEquals(BaseType.INT);
        initial.setExpression("0");

        // Declare data types, consumption rate and production rate.
        input.setTypeEquals(BaseType.BOOLEAN);
        _inputRate = new Parameter(input, "tokenConsumptionRate",
                new IntToken(1));
        output.setTypeEquals(BaseType.BOOLEAN);
        _outputRate = new Parameter(output, "tokenProductionRate",
                new IntToken(1));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** An array of integers defining an array of polynomials with
     *  binary coefficients. The coefficients indicate the presence (1)
     *  or absence (0) of a tap in the shift register. Each element
     *  of this array parameter should be a positive integer.
     *  The array's default value is {05, 07}.
     */
    public Parameter polynomialArray;

    /** Integer defining the intial state of the shift register.
     *  The i-th bit of the integer indicates the value of the
     *  i-th register. This parameter should be a non-negative
     *  integer. Its default value is the integer 0.
     */
    public Parameter initial;

    /** Integer defining the number of bits that the shift register
     *  takes in each firing. It should be a positive integer. Its
     *  default value is the integer 1.
     */
    public Parameter uncodeBlockSize;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute being changed is <i>initial</i>, then verify
     *  that it is a non-negative integer; if it is <i>uncodeBlockSize</i>,
     *  then verify that it is a positive integer; if it is
     *  <i>polynomialArray</i>, then verify that each of its elements is
     *  a positive integer and find the maximum value among them, which
     *  is used to compute the highest order among all polynomials.
     *  @exception IllegalActionException If <i>initial</i> is negative
     *  or <i>uncodeBlockSize</i> is non-positive or any element of
     *  <i>polynomialArray</i> is non-positive.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == initial) {
            int initialValue = ((IntToken)initial.getToken()).intValue();
            if (initialValue < 0 ) {
                throw new IllegalActionException(this,
                        "shift register's value must be non-negative.");
            }
        } else if (attribute == uncodeBlockSize) {
            _inputNumber = ((IntToken)uncodeBlockSize.getToken()).intValue();
            if (_inputNumber < 1 ) {
                throw new IllegalActionException(this,
                        "inputLength must be non-negative.");
            }
            // Set a flag indicating the private variable
            // _inputNumber is invalid, but do not compute
            // the value until all parameters have been set.
            _inputNumberInvalid = true;
            // Set the input comsumption rate.
            _inputRate.setToken(new IntToken(_inputNumber));
        } else if (attribute == polynomialArray) {
            ArrayToken maskToken = ((ArrayToken)polynomialArray.getToken());
            _maskNumber = maskToken.length();
            _mask = new int[_maskNumber];
            _maxPolyValue = 0;
            for (int i = 0; i < _maskNumber; i++) {
                _mask[i] = ((IntToken)maskToken.getElement(i)).intValue();
                if (_mask[i] <= 0) {
                    throw new IllegalActionException(this,
                            "Polynomial is required to be strictly positive.");
                }
                if (_mask[i] > _maxPolyValue) {
                    _maxPolyValue = _mask[i];
                }
            }
            _inputNumberInvalid = true;
            // Set the output production rate.
            _outputRate.setToken(new IntToken(_maskNumber));
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Read "<i>uncodeBlockSize</i>" bits from the input port and shift
     *  them into the shift register. Compute the parity for each
     *  polynomial specified in <i>polynomialArray</i>. Send the results
     *  in sequence. The i-th bit corresponds to the parity computed
     *  using the i-th polynomial.
     */
    public void fire() throws IllegalActionException {

        if (_inputNumberInvalid) {
            if (_inputNumber >= _maskNumber) {
                throw new IllegalActionException(this,
                        "Output rate should be larger than input rate.");
            }
            if ((1<< _inputNumber) > _maxPolyValue) {
                throw new IllegalActionException(this,
                        "The highest order of all polynomials is too low.");
            }
            _inputNumberInvalid = false;
        }

        _latestShiftReg = _shiftReg;

        // Read from the input port and shift the bits into
        // the shift register.
        Token[] inputToken = (Token[])input.get(0, _inputNumber);
        int reg = _latestShiftReg;
        for (int i = 0; i < _inputNumber; i++) {
            reg = reg << 1;
            BooleanToken input = (BooleanToken)inputToken[i];
            reg = reg | (input.booleanValue() ? 1:0);
        }
        _latestShiftReg = reg;

        // Compute the parities for all polynomials respectively.
        BooleanToken[] result = new BooleanToken[_maskNumber];
        int[] parity = new int[_maskNumber];
        parity = _calculateParity(_mask, _maskNumber, reg);

        // Send the parity results to the output.
        for (int i = 0; i < _maskNumber; i++) {
            if (parity[i] == 1){
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

    /** Calculate the parities given by the polynomial array
     *  and the state of shift register.
     *  @param mask The polynomial array.
     *  @param reg State of shift register.
     *  @return Parities stored in an array.
     */
    private int[] _calculateParity(int[] mask, int maskNumber, int reg) {
        int[] parity = new int[maskNumber];
        for (int i = 0; i < maskNumber; i++) {
            int masked = mask[i] & reg;
            // Find the parity of the "masked".
            parity[i] = 0;
            // Calculate the parity of the masked word.
            while (masked > 0){
                parity[i] = parity[i] ^ (masked & 1);
                masked = masked >> 1;
            }
        }
        return parity;
    }

    //////////////////////////////////////////////////////////////
    ////           private variables                          ////

    // Consumption rate of the input port.
    private Parameter _inputRate;

    // Production rate of the output port.
    private Parameter _outputRate;

    // Record the state of the shift register.
    private int _shiftReg;

    // Updated state of the shift register.
    private int _latestShiftReg;

    // Number of bits the actor consumes per firing.
    private int _inputNumber;

    // Polynomial array.
    private int[] _mask;

    // Number of polynomials.
    private int _maskNumber;

    // The maximum value in integer among all polynomials.
    private int _maxPolyValue;

    // A flag indicating that the private variable
    // _inputNumber is invalid.
    private transient boolean _inputNumberInvalid = true;

}
