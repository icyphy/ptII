/* Viterbi Decoder.

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
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypeAttribute;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.ArrayType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////////////
//// ViterbiDecoder
/**
The Viterbi algorithm is one optimal way to decode convolutional codes.
The <i>polynomialArray<i> indicates the polynomials used to compute
parities for the corresponding convolutional encoder.
The <i>uncodeBlockSize</i> is the input rate of the encoder, and it is
actually the output rate of the decoder.
<p>
The input port of the decoder is double type and receives signals from
the encoder corrupted with noise. The decoder tries to "guess" the most
likely input sequence of the encoder by searching all possibilities and
computing the "distance" between the codewords they produce and the
received ones. The one that makes the minimum distance is the most likely
input sequence.
<p>
There are 2 choices offered in this actor to compute such "distance".
If the parameter <i>softDecoding</i> is set to be true, it will compute
the Euclidean distance. If it is false, the decoder will first make a
decision between 0 and 1 of the corrupted signals with a threshold of 0.5.
Then it computes the Hamming distance. Soft decoding has lower probability
of decoding error than hard decoding. But distance computation for hard
decoding is easier, since it is based on bit-operations. Users can choose
either mode based on the trade-off between probability of decoding error
and computational complexity.
<p>
As each new corrupted codeword is received, the decoder makes a final
decision on the most-likely input symbol of "D" firings earlier, where "D"
is specified by the <i>delay</i> parameter. It should be a positive integer.
Therefore, in the first "D" firings, the decoder does not make any
decoding decision and sends all-zero tokens to the output.
And the last "D" codewords are "lost" in the decoder.
The larger "D" is, the more likely the decoder can "guess" correctly.
The trade-off is more waiting time and more complexity in the computation.
Users who wish to get a complete sequence of the decoded bits should attach
"D" blocks of redundant inputs when they send their information bits into the
ConvolutionalCoder. It has been found experimentally that a proper value
for "D" would be 5 times of the highest order of all polynomials, provided
that the convolutional code is a one that has good distance property.
<p>
For more information on convolutional codes and Viterbi decoder,
see the ConvolutionalCoder actor and
Proakis, Digital Communications, Fourth Edition, McGraw-Hill,
2001, pp. 471-477 and pp. 482-485.
<p>
@author Rachel Zhou
@version $Id$
@since Ptolemy II 3.0
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
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        uncodeBlockSize = new Parameter(this, "uncodeBlockSize");
        uncodeBlockSize.setTypeEquals(BaseType.INT);
        uncodeBlockSize.setExpression("1");

        polynomialArray = new Parameter(this, "polynomialArray");
        polynomialArray.setTypeEquals(new ArrayType(BaseType.INT));
        polynomialArray.setExpression("{05, 07}");

        delay = new Parameter(this, "delay");
        delay.setTypeEquals(BaseType.INT);
        delay.setExpression("10");

        softDecoding = new Parameter(this, "softDecoding");
        softDecoding.setExpression("true");
        softDecoding.setTypeEquals(BaseType.BOOLEAN);

        // Declare data types, consumption rate and production rate.
        input.setTypeEquals(BaseType.DOUBLE);
        _inputRate = new Parameter(input, "tokenConsumptionRate",
                new IntToken(1));
        output.setTypeEquals(BaseType.INT);
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

    /** Integer defining the number of bits that the shift register
     *  takes in each firing. It should be a positive integer. Its
     *  default value is the integer 1.
     */
    public Parameter uncodeBlockSize;

    /** Integer defining the trace back depth of the viterbi decoder.
     *  It should be a positive integer. Its default value is the
     *  integer 10.
     */
    public Parameter delay;

    /** Boolean defining the decoding mode. If it is true, the decoder
     *  will do soft decoding; otherwise it will do hard decoding.
     *  Its default value is true, which means it by default will do
     *  soft decoding.
     */
    public Parameter softDecoding;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute being changed is <i>uncodeBlockSize</i> or
     *  <i>delay</i> then verify it is a positive integer; if it is
     *  <i>polynomialArray</i>, then verify that each of its elements
     *  is a positive integer.
     *  @exception IllegalActionException If <i>uncodeBlockSize</i>,
     *  or <i>delay</i> is non-positive, or any element of
     *  <i>polynomialArray</i> is non-positive.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == softDecoding) {
            _mode = ((BooleanToken)softDecoding.getToken()).booleanValue();
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
            _outputRate.setToken(new IntToken(_inputNumber));
        } else if (attribute == delay) {
            _depth = ((IntToken)delay.getToken()).intValue();
            if (_depth < 1) {
                throw new IllegalActionException(this,
                        "Delay must be a positive integer.");
            }
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
                // Find maximum value in integer of all polynomials.
                if (_mask[i] > _maxPolyValue) {
                    _maxPolyValue = _mask[i];
                }
            }
            _inputNumberInvalid = true;
            // Set the output production rate.
            _inputRate.setToken(new IntToken(_maskNumber));
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** If the private variable _inputNumberInvalid is true, verify
     *  the validity of the parameters. If they are valid, compute
     *  the state-transition table of this convolutional code, which
     *  is stored in a 3-D array _truthTable[][][].
     *
     *  To decode, the actor searches iteratively of all possible
     *  input sequence and find the one that has the minimum distance.
     *  After the first "D" firings, the actor starts to make a
     *  decision and begins to send the decoded bits to the output
     *  port.
     */
    public void fire() throws IllegalActionException {
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
                _shiftRegLength ++;
            }

            if (_inputNumber >= _shiftRegLength) {
                throw new IllegalActionException(this,
                        "The highest order of all polynomials is "
                        + "still too low.");
            }
            _inputNumberInvalid = false;

            // Compute the necessary dimensions for the truth table and
            // the length of buffers used to store possible input sequence.
            _rowNum = 1 << (_shiftRegLength - _inputNumber);
            _colNum = 1 << _inputNumber;
            _truthTable = new int[_rowNum][_colNum][3];
            _path = new int[_rowNum][_depth + 1];
            _tempPath = new int[_rowNum][_depth + 1];
            _distance = new double[_rowNum];
            _tempDistance = new double[_rowNum];
            // Initialize the truth table and the buffer.
            for (int i = 0; i < _rowNum; i ++) {
                _distance[i] = 0;
                _tempDistance[i] = 0;
                for (int j = 0; j < _depth; j ++) {
                    _path[i][j] = 0;
                    _tempPath[i][j] = 0;
                }
            }

            int inputMask = (1 << _inputNumber) - 1;
            // Compute the truth table.
            // _truthTable[m][n][1:3] has the following meanings:
            // "m" is the possible current state of the shift register.
            // It has 2<i>k</i> possible previous states, where "k"
            // is the <i>uncodeBlockSize</i>.
            // Hence _truthTable[m][n][1:3] stores the truth values for
            // the n-th possible previous state for state "m".
            // _truthTable[m][n][1] is the "value" of the previous
            // shift register's states.
            // _truthTable[m][n][2] is the corresponding input block.
            // _truthTable[m][n][0] is the corresponding codewords
            // produced from the encoder.
            for (int state = 0; state < _rowNum; state ++) {
                for (int head = 0; head < _colNum; head ++) {
                    int reg = head << (_shiftRegLength - _inputNumber);
                    reg = reg + state;
                    int[] parity =  _calculateParity(_mask, _maskNumber, reg);
                    int outValue = 0;
                    // store the output values as an integer
                    // in the order of yn...y1y0
                    for (int i = _maskNumber - 1; i >= 0; i --) {
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

        // Read from the input port.
        Token[] inputToken = (Token[])input.get(0, _maskNumber);
        double[] y = new double[_maskNumber];
        for (int i = 0; i < _maskNumber; i++) {
            y[i] = ((DoubleToken)inputToken[i]).doubleValue();
        }

        // Search the optimal path (minimum distance) for each state.
        for (int state = 0; state < _rowNum; state ++) {
            double minDistance = 0;
            int minInput = 0;
            int minState = 0;
            for (int colIndex = 0; colIndex < _colNum; colIndex ++) {
                // Compute the distance for each possible path to "state".
                double d = _computeDistance(y,
                        _truthTable[state][colIndex][0], _maskNumber, _mode);
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
            for (int i = 0; i < _flag; i ++) {
                _path[state][i] = _tempPath[minState][i];
            }
            _path[state][_flag] = minInput;

        }

        // Send all-zero tokens for the first "D" firings.
        // If the waiting time has reached "D", the decoder starts to send
        // the decoded bits to the output port.
        if (_flag < _depth) {
            IntToken[] initialOutput = new IntToken[_inputNumber];
            for (int i = 0; i < _inputNumber; i ++) {
                initialOutput[i] = _tokenZero;
            }
            output.broadcast(initialOutput, _inputNumber);
        } else {
            // make a "final" decision among minimum distances of all states.
            double minD = 0;
            int minIndex = 0;
            for (int state = 0; state < _rowNum; state ++) {
                if (state == 0 || _distance[state] < minD) {
                    minD = _distance[state];
                    minIndex = state;
                }
            }

            // Cast the decoding result into binary bits and
            // send them in sequence to the output.
            IntToken[] decoded = new IntToken[_inputNumber];
            decoded = _convertToBit(_path[minIndex][0], _inputNumber);
            output.broadcast(decoded, _inputNumber);

            // Discard those datum in the buffers which have already
            // been made a "final" decision on. Move the rest datum
            // to the front of the buffers.
            for (int state = 0; state < _rowNum; state ++ ) {
                for (int i = 0; i < _flag; i ++) {
                    _path[state][i] = _path[state][i+1];
                }
            }
            _flag = _flag - 1;
        }
        _flag = _flag + 1;
    }

    /** Initialize the actor by resetting _inputNumberInvalid to be
     *  true, and _flag to be 0.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _inputNumberInvalid = true;
        _flag = 0;
    }

    /** Record the datum in buffers into their temporary versions.
     *  @exception IllegalActionException If the base class throws it
     */
    public boolean postfire() throws IllegalActionException {
        // Copy datum in buffers to their temp version.
        for (int i = 0; i < _rowNum; i ++) {
            _tempDistance[i] = _distance[i];
            for (int j = 0; j < _flag; j ++) {
                _tempPath[i][j] = _path[i][j];
            }
        }
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

    /** Compute the distance given by the datum received from
     *  the input port and the value in the truthTable.
     *  @param y Array of the double-type numbers received from
     *  the input port.
     *  @param truthValue integer representing the truth value
     *  from the truth table.
     *  @param maskNum The length of "y" and "truthValue".
     *  @param mode The mode based on which the distance is computed.
     *  @return The distance.
     */
    private double _computeDistance(
            double[] y, int truthValue, int maskNum, boolean mode) {
        double distance = 0.0;
        int hammingDistance = 0;
        int z = 0;
        for (int i = 0; i < maskNum; i ++) {
            int truthBit = truthValue & 1;
            truthValue = truthValue >> 1;
            if (mode == true) {
                // Euclidean distance for soft decoding. Here we
                // actually compute the square of the Euclidean distance.
                distance = distance
                    + java.lang.Math.pow(y[i] - truthBit, 2);
            } else {
                // Hard decoding.
                // Make a decision of 0 or 1 before computing distance.
                if (y[i] > 0.5) {
                    z = 1;
                } else {
                    z = 0;
                }
                // Compute Hamming distance for hard decoding.
                hammingDistance = hammingDistance + (z ^ truthBit);
                distance = hammingDistance;
            }
        }
        return distance;
    }

    /** Convert an integer to its binary form. The bits
     *  are stored in an array.
     *  @param integer Interger that should be converted.
     *  @param length The length of "integer" in binary form.
     *  @return The bits of "integer" stored in an array.
     */
    private IntToken[] _convertToBit(int integer, int length) {
        IntToken[] bit = new IntToken[length];
        for (int i = length -1; i >= 0; i --) {
            if ((integer & 1) == 1) {
                bit[i] = _tokenOne;
            } else {
                bit[i] = _tokenZero;
            }
            integer = integer >> 1;
        }
        return bit;
    }


    //////////////////////////////////////////////////////////////
    ////           private parameters and variables           ////

    // Consumption rate of the input port.
    private Parameter _inputRate;

    // Production rate of the output port.
    private Parameter _outputRate;

    // Decoding mode.
    private boolean _mode;

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
    //  _inputNumber is invalid.
    private transient boolean _inputNumberInvalid = true;

    // Truth table for the corresponding convolutinal code.
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

    // Since this actor always sends one of two tokens,
    // we statically create those tokens to avoid unnecessary
    // object construction.
    private static IntToken _tokenZero = new IntToken(0);
    private static IntToken _tokenOne = new IntToken(1);
}
