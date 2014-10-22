/* Viterbi Decoder.

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

import ptolemy.actor.TypeAttribute;
import ptolemy.actor.lib.Transformer;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.ComplexToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;
import ptolemy.math.Complex;

///////////////////////////////////////////////////////////////////
//// ViterbiDecoder

/**
 The Viterbi algorithm is an optimal way to decode convolutional and
 trellis codes. The code is specified jointly by the <i>uncodedRate</i>
 and <i>polynomialArray</i> parameters.  To get a <i>k</i>/<i>n</i>
 code, set <i>uncodedRate</i> to <i>k</i> and give <i>n</i> integers
 in <i>polynomialArray</i>.  See ConvolutionalCoder for details about
 the meaning of these parameters. On each firing, this actor will
 read <i>n</i> inputs and produce <i>k</i> outputs.
 <p>
 The decoder finds the most likely data sequence given noisy inputs
 by searching all possibilities and computing the distance
 between the codewords they produce and the observed noisy data.
 The sequence yielding the minimum distance is the decoded output.
 <p>
 There are two choices offered in this actor to compute the distance.
 If it the parameter <i>softDecoding</i> is set to be false, the input
 port will accept boolean tokens and compute the Hamming distance.
 If the parameter <i>softDecoding</i> is set to be true, the input port
 will accept double tokens and compute the Euclidean distance.
 The parameter <i>constellation</i> should be a double array of length 2.
 The first element specifies the amplitude of "false" input. The second
 element specifies the amplitude of "true" input.  At this time,
 this actor can only handle binary antipodal constellations, but
 we expect to generalize this.
 <p>
 Soft decoding has lower probability of decoding error than hard decoding.
 But distance computation for hard decoding is easier, since it is based
 on bit operations. Moreover, hard decoding can be used when there is no
 direct observation of the noisy data, but only observations of a bit
 sequence that may have errors in it.  With hard decoding, this
 actor serves the role of correcting errors.  With soft decoding, it
 serves the role of reducing the likelyhood of errors.
 <p>
 There is some delay between the reading of input data and the
 production of decoded output data.  That delay, which is called
 the <i>trace-back depth</i> or <i>truncation depth</i> of the
 decoder, is controlled by the
 <i>delay</i> parameter, which is required to be a positive integer.
 On the first <i>delay</i> firings of this actor, the outputs will
 be <i>false</i>.  On each firing, the number of outputs produced
 is <i>uncodedRate</i>, so the output will have a prefix of
 <i>delay</i>*<i>uncodedRate</i> false-valued tokens before any
 decoded bits are produced.  Larger values of <i>delay</i> generally
 reduce the probability of error.  A good rule of thumb is to set
 <i>delay</i> to five times the highest order of all polynomials, provided
 that the convolutional code is a one that has good distance properties.
 <p>
 For more information on convolutional codes and Viterbi decoder,
 see the ConvolutionalCoder actor and
 Proakis, <i>Digital Communications</i>, Fourth Edition, McGraw-Hill,
 2001, pp. 471-477 and pp. 482-485,
 or Barry, Lee and Messerschmitt, <i>Digital Communication</i>, Third Edition,
 Kluwer, 2004.
 <p>
 @author Ye Zhou, contributor: Edward A. Lee
 @version $Id$
 @since Ptolemy II 3.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class ViterbiDecoder extends Transformer {
    /** Construct an actor with the given container and name.
     *  The output and trigger ports are also constructed.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ViterbiDecoder(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        uncodedRate = new Parameter(this, "uncodedRate");
        uncodedRate.setTypeEquals(BaseType.INT);
        uncodedRate.setExpression("1");

        polynomialArray = new Parameter(this, "polynomialArray");
        polynomialArray.setTypeEquals(new ArrayType(BaseType.INT));
        polynomialArray.setExpression("{05, 07}");

        delay = new Parameter(this, "delay");
        delay.setTypeEquals(BaseType.INT);
        delay.setExpression("10");

        softDecoding = new Parameter(this, "softDecoding");
        softDecoding.setExpression("false");
        softDecoding.setTypeEquals(BaseType.BOOLEAN);

        trellisDecoding = new Parameter(this, "trellisDecoding");
        trellisDecoding.setExpression("false");
        trellisDecoding.setTypeEquals(BaseType.BOOLEAN);
        trellisDecoding.setVisibility(Settable.NONE);

        constellation = new Parameter(this, "constellation");
        constellation.setTypeEquals(new ArrayType(BaseType.DOUBLE));
        constellation.setExpression("{-1.0, 1.0}");

        //mode = new StringAttribute(this, "mode");
        //mode.setExpression("Hard Decoding");
        //mode.setVisibility(Settable.NONE);
        // Declare data types, consumption rate and production rate.
        _type = new ptolemy.actor.TypeAttribute(input, "inputType");
        _type.setExpression("boolean");
        _inputRate = new Parameter(input, "tokenConsumptionRate");
        _inputRate.setExpression("1");
        output.setTypeEquals(BaseType.BOOLEAN);
        _outputRate = new Parameter(output, "tokenProductionRate");
        _outputRate.setExpression("1");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** An array of integers defining polynomials with
     *  binary coefficients. The coefficients indicate the presence (1)
     *  or absence (0) of a tap in the shift register. Each element
     *  of this array parameter should be a positive integer.
     *  The default value is {05, 07}.
     */
    public Parameter polynomialArray;

    /** Integer defining the number of bits produced at the output
     *  in each firing. It should be a positive integer. Its
     *  default value is 1.
     */
    public Parameter uncodedRate;

    /** Integer defining the trace back depth of the viterbi decoder.
     *  It should be a positive integer. Its default value is the
     *  integer 10.
     */
    public Parameter delay;

    /** Boolean defining the decoding mode. If it is true, the decoder
     *  will do soft decoding, and the input data type will be double;
     *  otherwise it will do hard decoding, and the input data type will
     *  be boolean. The default value is false.
     */
    public Parameter softDecoding;

    /** Boolean defining whether the decoder will do trellis decoding.
     *  If it is true, the input data and constellation type will be
     *  complex; otherwise, they follow the constraints set by
     *  <i>softDecoding</i>. This parameter is always set to "false"
     *  in ViterbiDecoder. It will always be set to "true" in
     *  TrellisDecoder subclass.
     */
    public Parameter trellisDecoding;

    /** The constellation for soft decoding.  Inputs are expected to be
     *  symbols from this constellation with added noise.
     *  This parameter should be a double array of length 2. The first
     *  element defines the amplitude of "false" input. The second element
     *  defines the amplitude of "true" input.
     */
    public Parameter constellation;

    //public StringAttribute mode;
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute being changed is <i>softDecoding</i> or
     *  <i>trellisDecoding</i>, set input port and constellation
     *  type to be complex if <i>trellisDecoding</i> is true; else
     *  if <i>softDecoding</i> is true, set them to double type;
     *  otherwise set the input port to type boolean.
     *  If the attribute being changed is <i>uncodedRate</i> or
     *  <i>delay</i> then verify it is a positive integer; if it is
     *  <i>polynomialArray</i>, then verify that each of its elements
     *  is a positive integer.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If <i>uncodedRate</i>,
     *  or <i>delay</i> is non-positive, or any element of
     *  <i>polynomialArray</i> is non-positive.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        /*if (attribute == mode) {
         String modeName = mode.getExpression();
         if (modeName.equals("Hard Decoding")) {
         //_mode = _HARD;
         //_type.setExpression("boolean");
         } else if (modeName.equals("Soft Decoding")) {
         //_mode = _SOFT;
         //_type.setExpression("double");
         //constellation.setTypeEquals(new ArrayType(BaseType.DOUBLE));
         } else if (modeName.equals("Trellis Decoding")) {
         //_mode = _TRELLIS;
         //_type.setExpression("complex");
         //constellation.setTypeEquals(new ArrayType(BaseType.COMPLEX));
         }
         else {
         throw new IllegalActionException(this,
         "Unrecognized interpolation type: " + modeName);
         }
         } else */
        if (attribute == softDecoding || attribute == trellisDecoding) {
            _trellisMode = ((BooleanToken) trellisDecoding.getToken())
                    .booleanValue();
            _softMode = ((BooleanToken) softDecoding.getToken()).booleanValue();

            // Set different input port types for soft and hard decoding.
            if (_trellisMode) {
                _mode = _TRELLIS;
                _type.setExpression("complex");
                constellation.setTypeEquals(new ArrayType(BaseType.COMPLEX));
            } else if (_softMode) {
                _mode = _SOFT;
                _type.setExpression("double");
                constellation.setTypeEquals(new ArrayType(BaseType.DOUBLE));
            } else {
                _mode = _HARD;
                _type.setExpression("boolean");
            }
        } else if (attribute == uncodedRate) {
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
            _outputRate.setToken(new IntToken(_inputNumber));
        } else if (attribute == delay) {
            _depth = ((IntToken) delay.getToken()).intValue();

            if (_depth < 1) {
                throw new IllegalActionException(this,
                        "Delay must be a positive integer.");
            }

            _depthInvalid = true;
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

                // Find maximum value in integer of all polynomials.
                if (_mask[i] > _maxPolyValue) {
                    _maxPolyValue = _mask[i];
                }
            }

            _inputNumberInvalid = true;

            // Set the output production rate.
            boolean trellisMode = ((BooleanToken) trellisDecoding.getToken())
                    .booleanValue();

            if (trellisMode) {
                _inputRate.setToken(new IntToken(1));
            } else {
                _inputRate.setToken(new IntToken(_maskNumber));
            }
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
        ViterbiDecoder newObject = (ViterbiDecoder) super.clone(workspace);

        newObject._inputRate = (Parameter) newObject.input
                .getAttribute("tokenConsumptionRate");
        newObject._mask = new int[newObject._maskNumber];
        newObject._outputRate = (Parameter) newObject.output
                .getAttribute("tokenProductionRate");
        newObject._type = (TypeAttribute) newObject.input
                .getAttribute("inputType");
        return newObject;
    }

    /** Read <i>n</i> inputs and produce <i>k</i> outputs, where <i>n</i>
     *  is the number of integers in <i>polynomialArray</i> and <i>k</i>
     *  is the value of the <i>uncodedRate</i> parameter.  The outputs
     *  are a decoded bit sequence, with a prefix of <i>false</i>-valued
     *  tokens produced on the first <i>delay</i> firings.  The number
     *  of leading <i>false</i> outputs, therefore, is
     *  <i>delay</i>*<i>uncodedRate</i>.
     *  To decode, the actor searches iteratively of all possible
     *  input sequence and find the one that has the minimum distance
     *  to the observed inputs.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        //boolean trellisMode =
        //    ((BooleanToken)trellisDecoding.getToken()).booleanValue();
        int constellationOrder;
        int inputRate;

        if (_mode == _TRELLIS) {
            constellationOrder = _maskNumber;
            inputRate = 1;
        } else {
            constellationOrder = 1;
            inputRate = _maskNumber;
        }

        if (_mode == _TRELLIS) {
            _constellation = new Complex[1 << constellationOrder];

            ArrayToken ampToken = (ArrayToken) constellation.getToken();

            if (ampToken.length() != 1 << constellationOrder) {
                throw new IllegalActionException(this,
                        "Invalid amplitudes for soft decoding!");
            }

            for (int i = 0; i < ampToken.length(); i++) {
                _constellation[i] = ((ComplexToken) ampToken.getElement(i))
                        .complexValue();
            }
        } else if (_mode == _SOFT) {
            ArrayToken ampToken = (ArrayToken) constellation.getToken();

            if (ampToken.length() != 1 << constellationOrder) {
                throw new IllegalActionException(this,
                        "Invalid amplitudes for soft decoding!");
            }

            _falseAmp = ((DoubleToken) ampToken.getElement(0)).doubleValue();
            _trueAmp = ((DoubleToken) ampToken.getElement(1)).doubleValue();
        }

        // If the private variable _inputNumberInvalid is true, verify
        // the validity of the parameters. If they are valid, compute
        // the state-transition table of this convolutional code, which
        // is stored in a 3-D array _truthTable[][][].
        if (_inputNumberInvalid) {
            if (_inputNumber >= _maskNumber) {
                throw new IllegalActionException(this,
                        "Output rate should be larger than input rate.");
            }

            //Comput the length of shift register.
            _shiftRegLength = 0;

            int regLength = 1;

            while (regLength <= _maxPolyValue) {
                //regLength = regLength << _inputNumber;
                //_shiftRegLength = _shiftRegLength + _inputNumber;
                regLength = regLength << 1;
                _shiftRegLength++;
            }

            if (_inputNumber >= _shiftRegLength) {
                throw new IllegalActionException(this,
                        "The highest order of all polynomials is "
                                + "still too low.");
            }

            _inputNumberInvalid = false;

            // Compute the necessary dimensions for the truth table and
            // the length of buffers used to store possible input sequence.
            _rowNum = 1 << _shiftRegLength - _inputNumber;
            _colNum = 1 << _inputNumber;
            _truthTable = new int[_rowNum][_colNum][3];
            _distance = new double[_rowNum];
            _tempDistance = new double[_rowNum];

            // Initialize the truth table and the buffer.
            for (int i = 0; i < _rowNum; i++) {
                _distance[i] = 0;
                _tempDistance[i] = 0;
            }

            int inputMask = (1 << _inputNumber) - 1;

            // Compute the truth table.
            // _truthTable[m][n][1:3] has the following meanings:
            // "m" is the possible current state of the shift register.
            // It has 2<sup>k</sup> possible previous states, where "k"
            // is the <i>uncodedRate</i>.
            // Hence _truthTable[m][n][1:3] stores the truth values for
            // the n-th possible previous state for state "m".
            // _truthTable[m][n][1] is the "value" of the previous
            // shift register's states.
            // _truthTable[m][n][2] is the corresponding input block.
            // _truthTable[m][n][0] is the corresponding codewords
            // produced from the encoder.
            for (int state = 0; state < _rowNum; state++) {
                for (int head = 0; head < _colNum; head++) {
                    int reg = head << _shiftRegLength - _inputNumber;
                    reg = reg + state;

                    int[] parity = _calculateParity(_mask, _maskNumber, reg);
                    int outValue = 0;

                    // store the output values as an integer
                    // in the order of yn...y1y0
                    for (int i = _maskNumber - 1; i >= 0; i--) {
                        outValue = outValue << 1;
                        outValue = outValue + parity[i];
                    }

                    _truthTable[state][head][0] = outValue;

                    int oldState = reg >> _inputNumber;
                    _truthTable[state][head][1] = oldState;

                    int input = reg & inputMask;
                    _truthTable[state][head][2] = input;
                }
            }
        }

        if (_depthInvalid) {
            _path = new int[_rowNum][_depth + 1];
            _tempPath = new int[_rowNum][_depth + 1];

            for (int i = 0; i < _rowNum; i++) {
                for (int j = 0; j < _depth; j++) {
                    _path[i][j] = 0;
                    _tempPath[i][j] = 0;
                }
            }

            _depthInvalid = false;
        }

        // Read from the input port.
        Token[] inputToken = input.get(0, inputRate);

        // Search the optimal path (minimum distance) for each state.
        for (int state = 0; state < _rowNum; state++) {
            double minDistance = 0;
            int minInput = 0;
            int minState = 0;

            for (int colIndex = 0; colIndex < _colNum; colIndex++) {
                // Compute the distance for each possible path to "state".
                double d = 0.0;

                if (_mode == _TRELLIS) {
                    Complex y = ((ComplexToken) inputToken[0]).complexValue();
                    d = _computeTrellisDistance(y, _constellation,
                            _truthTable[state][colIndex][0]);
                } else if (_mode == _SOFT) {
                    double[] y = new double[inputRate];

                    for (int i = 0; i < inputRate; i++) {
                        y[i] = ((DoubleToken) inputToken[i]).doubleValue();
                    }

                    d = _computeSoftDistance(y, _falseAmp, _trueAmp,
                            _truthTable[state][colIndex][0], inputRate);
                } else {
                    boolean[] y = new boolean[_maskNumber];

                    for (int i = 0; i < _maskNumber; i++) {
                        y[i] = ((BooleanToken) inputToken[i]).booleanValue();
                    }

                    d = _computeHardDistance(y,
                            _truthTable[state][colIndex][0], _maskNumber);
                }

                // The previous state for that possibility.
                int oldState = _truthTable[state][colIndex][1];
                d = _tempDistance[oldState] + d;

                // Find the minimum distance and corresponding previous
                // state for each possible current state of the shift register.
                if (colIndex == 0 || d < minDistance) {
                    minDistance = d;
                    minState = oldState;
                    minInput = _truthTable[state][colIndex][2];
                }
            }

            // update the buffers for minimum distance and its
            // corresponding possible input sequence.
            _distance[state] = minDistance;

            for (int i = 0; i < _flag; i++) {
                _path[state][i] = _tempPath[minState][i];
            }

            _path[state][_flag] = minInput;
        }

        // Send all-false tokens for the first "D" firings.
        // If the waiting time has reached "D", the decoder starts to send
        // the decoded bits to the output port.
        if (_flag < _depth) {
            BooleanToken[] initialOutput = new BooleanToken[_inputNumber];

            for (int i = 0; i < _inputNumber; i++) {
                initialOutput[i] = BooleanToken.FALSE;
            }

            output.broadcast(initialOutput, _inputNumber);
        } else {
            // make a "final" decision among minimum distances of all states.
            double minD = 0;
            int minIndex = 0;

            for (int state = 0; state < _rowNum; state++) {
                if (state == 0 || _distance[state] < minD) {
                    minD = _distance[state];
                    minIndex = state;
                }
            }

            // Cast the decoding result into booleans and
            // send them in sequence to the output.
            BooleanToken[] decoded = new BooleanToken[_inputNumber];
            decoded = _convertToBit(_path[minIndex][0], _inputNumber);
            output.broadcast(decoded, _inputNumber);

            // Discard those datum in the buffers which have already
            // been made a "final" decision on. Move the rest datum
            // to the front of the buffers.
            for (int state = 0; state < _rowNum; state++) {
                for (int i = 0; i < _flag; i++) {
                    _path[state][i] = _path[state][i + 1];
                }
            }

            _flag = _flag - 1;
        }

        _flag = _flag + 1;
    }

    /** Initialize the actor.
     *  @exception IllegalActionException If the parent class throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _inputNumberInvalid = true;
        _flag = 0;
    }

    /** Record the datum in buffers into their temporary versions.
     *  @exception IllegalActionException If the base class throws it
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        // Copy datum in buffers to their temp versions.
        for (int i = 0; i < _rowNum; i++) {
            _tempDistance[i] = _distance[i];

            for (int j = 0; j < _flag; j++) {
                _tempPath[i][j] = _path[i][j];
            }
        }

        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Calculate the parity given by the polynomial and the
     *  state of shift register.
     *  @param mask Polynomial.
     *  @param maskNumber Number of polynomials.
     *  @param reg State of shift register.
     *  @return Parity.
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

    /** Compute the Hamming distance given by the datum received from
     *  the input port and the value in the truthTable.
     *  @param y Array of the booleans received from the input port.
     *  @param truthValue integer representing the truth value
     *  from the truth table.
     *  @param maskNum The length of "y" and "truthValue".
     *  @return The distance.
     */
    private int _computeHardDistance(boolean[] y, int truthValue, int maskNum) {
        int hammingDistance = 0;

        for (int i = 0; i < maskNum; i++) {
            int truthBit = truthValue & 1;
            truthValue = truthValue >> 1;

            // Compute Hamming distance for hard decoding.
            hammingDistance = hammingDistance + ((y[i] ? 1 : 0) ^ truthBit);
        }

        return hammingDistance;
    }

    /** Compute the Euclidean distance given by the datum received from
     *  the input port and the value in the truthTable.
     *  @param y Array of the double-type numbers received from
     *  the input port.
     *  @param falseAmp Amplitude of "false" input.
     *  @param trueAmp Amplitude of "true" input.
     *  @param truthValue integer representing the truth value
     *  from the truth table.
     *  @param inputRate The length of "y" and "truthValue".
     *  @return The distance.
     */
    private double _computeSoftDistance(double[] y, double falseAmp,
            double trueAmp, int truthValue, int inputRate) {
        /*if (trellisMode) {
         Complex truthComplex = constellation[truthValue];
         Complex z = truthComplex.subtract(y[0].complexValue());
         return z.magnitudeSquared();
         } else {*/
        double distance = 0.0;
        double truthAmp;

        for (int i = 0; i < inputRate; i++) {
            int truthBit = truthValue & 1;

            if (truthBit == 1) {
                truthAmp = trueAmp;
            } else {
                truthAmp = falseAmp;
            }

            // Euclidean distance for soft decoding. Here we
            // actually compute the square of the Euclidean distance.
            distance = distance + java.lang.Math.pow(y[i] - truthAmp, 2);

            truthValue = truthValue >> 1;
        }

        return distance;
    }

    /** Compute the Euclidean distance given by the datum received
     *  from the input port and the value in the truthTable in
     *  trellis decoding mode.
     *  @param y Complex number received from the input port.
     *  @param constellation Complex array defining the constellation.
     *  @param truthValue integer representing the truth value
     *  from the truth table.
     *  @return The distance.
     */
    private double _computeTrellisDistance(Complex y, Complex[] constellation,
            int truthValue) {
        Complex truthComplex = constellation[truthValue];

        //Complex z = y;
        Complex v = truthComplex.subtract(y);
        return v.magnitudeSquared();
    }

    /** Convert an integer to its binary form. The bits
     *  are stored in an array.
     *  @param integer The integer to be converted.
     *  @param length The length of "integer" in binary form.
     *  @return The bits of "integer" stored in an array.
     */
    private BooleanToken[] _convertToBit(int integer, int length) {
        BooleanToken[] bit = new BooleanToken[length];

        for (int i = length - 1; i >= 0; i--) {
            if ((integer & 1) == 1) {
                bit[i] = BooleanToken.TRUE;
            } else {
                bit[i] = BooleanToken.FALSE;
            }

            integer = integer >> 1;
        }

        return bit;
    }

    ///////////////////////////////////////////////////////////////////
    ////           private parameters and variables           ////
    // Consumption rate of the input port.
    private Parameter _inputRate;

    // Production rate of the output port.
    private Parameter _outputRate;

    // Input port type.
    private TypeAttribute _type;

    // Decoding mode.
    private boolean _trellisMode;

    private boolean _softMode;

    private int _mode;

    // Amplitudes for soft decoding.
    private double _trueAmp;

    private double _falseAmp;

    private Complex[] _constellation;

    // Number bits the actor consumes per firing.
    private int _inputNumber;

    // Number of polynomials.
    private int _maskNumber;

    // Polynomial array.
    private int[] _mask;

    // The maximum value in integer among all polynomials.
    // It is used to compute the necessary shift register's length.
    private int _maxPolyValue;

    // Length of the shift register.
    private int _shiftRegLength = 0;

    // A flag indicating that the private variable
    // _inputNumber is invalid.
    private transient boolean _inputNumberInvalid = true;

    // A flag indicating that the the private variable
    // _depth is invalid.
    private transient boolean _depthInvalid = true;

    // Truth table for the corresponding convolutional code.
    private int[][][] _truthTable;

    // Size of first dimension of the truth table.
    private int _rowNum;

    // Size of second dimension of the truth table.
    private int _colNum;

    // The delay specified by the user.
    private int _depth;

    // Buffers for minimum distance, possible input sequence
    // for each state. And their temporary versions used
    // when updating.
    private double[] _distance;

    private double[] _tempDistance;

    private int[][] _path;

    private int[][] _tempPath;

    // A flag used to indicate the positions that new values
    // should be inserted in the buffers.
    private int _flag;

    private static final int _HARD = 0;

    private static final int _SOFT = 1;

    private static final int _TRELLIS = 2;
}
