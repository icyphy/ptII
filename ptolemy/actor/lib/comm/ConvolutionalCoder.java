/* Encode an input sequence with a convolutional code.

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
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// ConvolutionalCoder

/**
 Encode an input sequence with a convolutional code. The inputs and
 outputs are booleans. 

 <p>The input sequence
 enters a shift register, and the contents of the shift register are
 combined using boolean functions given by the <i>polynomialArray</i>
 parameter. The initial state of the shift register is given by the
 <i>initialState</i> parameter, which should be a non-negative integer.
 The <i>uncodedRate</i> parameter, often denoted by <i>k</i> in the
 coding literature, is the number of bits per firing that are shifted
 into the shift register. The <i>polynomialArray</i> parameter is an
 array of positive integers. Each integer indicates one polynomial
 used for computing output bits. To get a <i>k</i>/<i>n</i>
 convolutional code, set <i>uncodedRate</i> to <i>k</i> and provide
 <i>n</i> integers in <i>polynomialArray</i>.</p>

 <p> The integers in <i>polynomialArray</i> are usually most conveniently
 given as octal numbers. A leading zero indicates an octal
 number. The <i>i</i>-th bit of the integer indicates whether the
 <i>i</i>-th tap of the delay line should be used.  All bits that
 are used are exclusive-ored, thus yielding the parity of the selected
 bits. See more details in Scrambler actor on using an integer to
 define a polynomial. The <i>n</i> parity results are produced on
 the output in a sequence.</p>

 <p> A good convolutional code should have large Hamming distance between
 any two of its codewords. This is not easily checked, but there are some
 simple rules that all "good" codes should satisfy:</p>
 <ol>
 <li> <i>k</i> should be strictly smaller than <i>n</i>, otherwise
 the code is not uniquely decodable.  Thus, <i>uncodedRate</i>
 should be less than the length of <i>polynomialArray</i>.</li>
 <li> <i>k</i> should not be higher than the highest order of
 all polynomials, otherwise, some input bits never get
 involved in computing parities.</li>
 </ol>

 <p>If these rules are violated, the actor will throw an exception.
 However, these rules do not guarantee the codeword can be decoded
 successfully, and it is not always true that larger polynomials
 yield better codes. Users should check tables for convolutional
 codes from professional references. For convenience, we list here
 some convolutional codes that have large distance property.</p>
 <pre>
 Rate = 1/2
 polynomialArray
 {05, 07}
 {013, 017}
 {031, 027}
 {065, 057}
 {0155, 0117}

 Rate = 1/3
 polynomialArray
 {05, 07, 07}
 {015, 013, 017}
 {025, 033, 037}
 {071, 065, 057}
 {0155, 0123, 0137}

 Rate = 1/4
 polynomialArray
 {05, 07, 07, 07}
 {015, 013, 013, 017}
 {025, 035, 033, 037}
 {065, 073, 047, 057}
 {0135, 0135, 0163, 0147}

 Rate = 1/5
 polynomialArray
 {07, 07, 07, 05, 05}
 {017, 017, 015, 013, 013}
 {037, 035, 033, 025, 027}
 {057, 047, 067, 053, 075}

 Rate = 1/6
 polynomialArray
 {07, 07, 07, 07, 05, 05}
 {017, 017, 015, 015, 013, 013}
 {037, 027, 035, 033, 025, 027}
 {067, 057, 055, 053, 071, 075}

 Rate = 2/3
 polynomialArray
 {017, 06, 013}
 {072, 057, 027}
 {0171, 0166, 0273}

 Rate = k/5
 k     polynomialArray
 2     {017, 016, 011, 05, 02}
 2     {072, 047, 025, 053, 075}
 3     {056, 062, 057, 043, 071}

 Rate = k/7
 k     polynomialArray
 2     {012, 06, 05, 013, 013, 015, 017}
 2     {066, 055, 027, 071, 052, 056, 057}
 3     {051, 042, 036, 023, 075, 061, 047}

 Rate  polynomialArray
 3/4   {064, 052, 043, 071}
 3/8   {054, 021, 062, 043, 045, 036, 057, 071}
 </pre>

 <p> Note that this implementation is limited to a shift register
 length of 32 because of the specification of the polynomials and
 initial shift register state as 32-bit integers.</p>

 <p>For more information on convolutional codes, see Proakis, Digital
 Communications, Fourth Edition, McGraw-Hill, 2001, pp. 471-477,
 or Barry, Lee and Messerschmitt, <i>Digital Communication</i>, Third Edition,
 Kluwer, 2004.</p>

 @author Ye Zhou, contributor: Edward A. Lee
 @version $Id$
 @since Ptolemy II 3.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 @see Scrambler
 @see ViterbiDecoder
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
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        uncodedRate = new Parameter(this, "uncodedRate");
        uncodedRate.setTypeEquals(BaseType.INT);
        uncodedRate.setExpression("1");

        polynomialArray = new Parameter(this, "polynomialArray");
        polynomialArray.setTypeEquals(new ArrayType(BaseType.INT));
        polynomialArray.setExpression("{05, 07}");

        initialState = new Parameter(this, "initialState");
        initialState.setTypeEquals(BaseType.INT);
        initialState.setExpression("0");

        // Declare data types, consumption rate and production rate.
        input.setTypeEquals(BaseType.BOOLEAN);
        _inputRate = new Parameter(input, "tokenConsumptionRate");
        _inputRate.setExpression("1");
        output.setTypeEquals(BaseType.BOOLEAN);
        _outputRate = new Parameter(output, "tokenProductionRate");
        _outputRate.setExpression("1");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** An array of integers defining an array of polynomials with
     *  binary coefficients. The coefficients indicate the presence (1)
     *  or absence (0) of a tap in the shift register. Each element
     *  of this array parameter should be a positive integer.
     *  The default value is {05, 07}.
     */
    public Parameter polynomialArray;

    /** Integer defining the initial state of the shift register.
     *  The i-th bit of the integer indicates the value of the
     *  i-th register. This parameter should be a non-negative
     *  integer. Its default value is the integer 0.
     */
    public Parameter initialState;

    /** Integer defining the number of bits that the shift register
     *  takes in each firing. It should be a positive integer. Its
     *  default value is the integer 1.
     */
    public Parameter uncodedRate;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute being changed is <i>uncodedRate</i>,
     *  then verify that it is a positive integer; if it is
     *  <i>polynomialArray</i>, then verify that each of its elements is
     *  a positive integer and find the maximum value among them, which
     *  is used to compute the highest order among all polynomials.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If <i>uncodedRate</i> is
     *  non-positive or any element of <i>polynomialArray</i> is non-positive.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == uncodedRate) {
            _inputNumber = ((IntToken) uncodedRate.getToken()).intValue();

            if (_inputNumber < 1) {
                throw new IllegalActionException(this,
                        "inputLength must be non-negative.");
            }

            // Set a flag indicating the private variable
            // _inputNumber is invalid, but do not compute
            // the value until all parameters have been set.
            _inputNumberInvalid = true;

            // Set the input consumption rate.
            _inputRate.setToken(new IntToken(_inputNumber));
        } else if (attribute == polynomialArray) {
            ArrayToken maskToken = (ArrayToken) polynomialArray.getToken();
            _maskNumber = maskToken.length();
            _mask = new int[_maskNumber];
            _maxPolyValue = 0;

            for (int i = 0; i < _maskNumber; i++) {
                _mask[i] = ((IntToken) maskToken.getElement(i)).intValue();

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

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ConvolutionalCoder newObject = (ConvolutionalCoder) super
                .clone(workspace);

        newObject._inputRate = (Parameter) newObject.input
                .getAttribute("tokenConsumptionRate");
        newObject._mask = new int[newObject._maskNumber];
        newObject._outputRate = (Parameter) newObject.output
                .getAttribute("tokenProductionRate");

        return newObject;
    }

    /** Read <i>uncodedRate</i> bits from the input port and shift
     *  them into the shift register. Compute the parity for each
     *  polynomial specified in <i>polynomialArray</i>. Send the results
     *  in sequence to the output. The i-th bit in the output
     *  corresponds to the parity computed using the i-th polynomial.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (_inputNumberInvalid) {
            if (_inputNumber >= _maskNumber) {
                throw new IllegalActionException(this,
                        "Output rate should be larger than input rate.");
            }

            if (1 << _inputNumber > _maxPolyValue) {
                throw new IllegalActionException(this,
                        "The highest order of all polynomials is too low.");
            }

            _inputNumberInvalid = false;
        }

        _latestShiftReg = _shiftReg;

        // Read from the input port and shift the bits into
        // the shift register.
        Token[] inputToken = input.get(0, _inputNumber);
        int reg = _latestShiftReg;

        for (int i = 0; i < _inputNumber; i++) {
            reg = reg << 1;

            BooleanToken input = (BooleanToken) inputToken[i];
            reg = reg | (input.booleanValue() ? 1 : 0);
        }

        _latestShiftReg = reg;

        // Compute the parities for all polynomials respectively.
        BooleanToken[] result = new BooleanToken[_maskNumber];
        int[] parity; /*= new int[_maskNumber];*/
        parity = _calculateParity(_mask, _maskNumber, reg);

        // Send the parity results to the output.
        for (int i = 0; i < _maskNumber; i++) {
            if (parity[i] == 1) {
                result[i] = BooleanToken.TRUE;
            } else {
                result[i] = BooleanToken.FALSE;
            }
        }

        output.broadcast(result, result.length);
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
     *  state for the next iteration.
     *  @exception IllegalActionException If the base class throws it
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        _shiftReg = _latestShiftReg;
        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

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
            while (masked > 0) {
                parity[i] = parity[i] ^ masked & 1;
                masked = masked >> 1;
            }
        }

        return parity;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
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
