/* Viterbi Decoder.

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
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.IntMatrixToken;
import ptolemy.data.Token;

import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.ArrayType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// ViterbiDecoder
/**
FIXME
<p>
For more information on convolutional codes, see Proakis, Digital
Communications, Fourth Edition, McGraw-Hill, 2001, pp. 471-477.
<p>
b@author Rachel Zhou
@version $Id$
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

        inputNumber = new Parameter(this, "inputNumber");
        inputNumber.setTypeEquals(BaseType.INT);
        inputNumber.setExpression("1");

        constraintLength = new Parameter(this, "constraintLength");
        constraintLength.setTypeEquals(BaseType.INT);
        constraintLength.setExpression("3");

        polynomialArray = new Parameter(this, "polynomialArray");
        polynomialArray.setTypeEquals(new ArrayType(BaseType.INT));
        polynomialArray.setExpression("{05, 07}");

        delay = new Parameter(this, "delay");
        delay.setTypeEquals(BaseType.INT);
        delay.setExpression("10");

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
      *  The array's default value is {03}.
      */
    public Parameter polynomialArray;

    /** Integer defining the number of bits that the shift register
     *  takes in each time. It should be a positive integer. Its
     *  default value is the integer 1.
     */
    public Parameter inputNumber;

    public Parameter constraintLength; 

    public Parameter delay;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute being changed is <i>inputNumber</i>,
     *  then verify that it is a positive integer; if it is
     *  <i>polynomailArray</i>, then verify that each of its elements is
     *  a positive integer and compute the shift register's length,
     *  which is the highest order of all polynomials.
     *  @exception IllegalActionException If <i>initial</i> is negative
     *  or <i>inputLength</i> is non-positive or any element of
     *  <i>polynomialArray</i> is non-positive.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == inputNumber) {
            _inputNumber = ((IntToken)inputNumber.getToken()).intValue();
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
        } else if (attribute == constraintLength) {
            _stageNumber = ((IntToken)constraintLength.getToken()).intValue();
            if (_stageNumber <= 1) {
                throw new IllegalActionException(this,
                        "constraintLength must be an integer greater than 1.");
            }
            _inputNumberInvalid = true;
        }
          else if (attribute == delay) {
            _depth = ((IntToken)delay.getToken()).intValue();
            if (_depth < 1) {
                throw new IllegalActionException(this,
                        "Delay must be a positive integer.");
            }
        } else if (attribute == polynomialArray) {
            ArrayToken maskToken = ((ArrayToken)polynomialArray.getToken());
            _maskNumber = maskToken.length();
            _mask = new int[_maskNumber];
            int maxPolyValue = 0;
            for(int i = 0; i < _maskNumber; i++) {
                _mask[i] = ((IntToken)maskToken.getElement(i)).intValue();
                if (_mask[i] <= 0) {
                    throw new IllegalActionException(this,
                    "Polynomial is required to be strictly positive.");
                }
            }
            _inputNumberInvalid = true;
            // Set the output production rate.
            _inputRate.setToken(new IntToken(_maskNumber));
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Read "<i>inputLength</i>" bits from the input port and shift
     *  them into the shift register. Compute the parity for each
     *  polynomial specified in <i>polynomialArray</i>. Send the results
     *  in sequence. The n-th bit corresponds to the parity computed
     *  using the n-th polynomial.
     */
    public void fire() throws IllegalActionException {

        if (_inputNumberInvalid) {
            if (_inputNumber >= _maskNumber) {
                throw new IllegalActionException(this,
                "Output rate should be larger than input rate.");
            }
            _shiftRegLength = _stageNumber * _inputNumber;
            if (_inputNumber >= _shiftRegLength) {
                throw new IllegalActionException(this,
                "Input rate must be smaller than shift register length.");
            }
            for (int i = 0; i < _maskNumber; i ++) {
                if (_mask[i] >= (1 << _shiftRegLength)) {
                    throw new IllegalActionException(this,
             "shift register's length must be higher than polynomials' order");
                }
            }
            _inputNumberInvalid = false;
 
            _rowNum = 1 << (_shiftRegLength - _inputNumber);
            _colNum = 1 << _inputNumber;
            _truthTable = new int[_rowNum][_colNum][3];
            _path = new int[_rowNum][_depth + 1];
            _tempPath = new int[_rowNum][_depth + 1];
            _distance = new double[_rowNum];
            _tempDistance = new double[_rowNum];
            // Initialize.
            for(int i = 0; i < _rowNum; i ++) {
                _distance[i] = 0;
                _tempDistance[i] = 0;
                for(int j = 0; j < _depth; j ++) {
                    _path[i][j] = 0;
                    _tempPath[i][j] = 0;
                }
            }    
           
            int inputMask = (1 << _inputNumber) - 1;
            // Compute truthTable
            // _truthTable[][][0] is the output y
            // _truthTable[][][1] is the corresponding previous state
            // _truthTable[][][2] is the input (actually it's the last k
            // bit of the current state.
            for (int state = 0; state < _rowNum; state ++) {
                for (int head = 0; head < _colNum; head ++) {
                   int reg = head << (_shiftRegLength - _inputNumber);
                   reg = reg + state;
                   int[] parity =  _calculateParity(_mask, _maskNumber, reg);
                   int outValue = 0;
                   for (int i = 0; i < _maskNumber; i ++) {
                       outValue = outValue << 1;
                       outValue = outValue + parity[i];
                   }
                   _truthTable[state][head][0] = outValue;
                   int oldState = reg >> _inputNumber;
                   _truthTable[state][head][1] = oldState;
                   int input = reg & inputMask;
                   _truthTable[state][head][2] = input;
                   System.out.println("state=" + state + " y=" + outValue + 
                      " oldState=" + oldState + " input=" + input);
               }
            }
        }
        System.out.println(" ");
        System.out.println("flag =" + _flag);
        // Read from the input port.
        Token[] inputToken = (Token[])input.get(0, _maskNumber);
        double[] y = new double[_maskNumber];
        for (int i = 0; i < _maskNumber; i++) {
             y[i] = ((DoubleToken)inputToken[i]).doubleValue();             
        }

        // Search the optimal path (minimum distance) for each state.
        for (int state = 0; state < _rowNum; state ++) {
            System.out.println("state = " + state);
            double minDistance = 0;
            int minInput = 0;
            int minState = 0;
            for (int colIndex = 0; colIndex < _colNum; colIndex ++) {
                // Compute the distance for each possible path to "state".
                double d = _computeSoftDistance(y,
                        _truthTable[state][colIndex][0], _maskNumber);
                // The previous state for that path.
                int oldState = _truthTable[state][colIndex][1];
                d = _tempDistance[oldState] + d;
                if (colIndex == 0 || d < minDistance) {
                    minDistance = d;
                    minState = oldState;
                    minInput = _truthTable[state][colIndex][2];
                }
            }
            _distance[state] = minDistance;
            for (int i = 0; i < _flag; i ++) {
                _path[state][i] = _tempPath[minState][i];
            }  
            _path[state][_flag] = minInput;
            // Note each element in path[][] is an integer representing k bits.
            // order: x0x1x2x3... where x0 is the first input bit that entered.
            // When sending to the output, should convert to binary.
            System.out.println("d = " + minDistance);
            System.out.print("path = ");
            for (int i = 0; i <= _flag; i ++) {
                System.out.print( _path[state][i] + " ");
            }
            System.out.println(" ");
        }

        //When to decide to output?
        if (_flag >= _depth) {
            //send to the output;
            
            double minD = 0;
            int minIndex = 0;
            for (int state = 0; state < _rowNum; state ++) {
                if (state == 0 || _distance[state] < minD) {
                    minD = _distance[state];
                    minIndex = state; 
                }
            }

            IntToken[] decoded = new IntToken[_inputNumber];
            System.out.println("minIndex = " + minIndex);
            System.out.println("decoded = " + _path[minIndex][0]);
            decoded = _convertToBit(_path[minIndex][0], _inputNumber);
            output.broadcast(decoded, _inputNumber);

            // path should move to the front.
            for (int state = 0; state < _rowNum; state ++ ) {
                //System.out.println("move to the front path for state " + state);
                for (int i = 0; i < _flag; i ++) {
                   _path[state][i] = _path[state][i+1];
                }
            }
            _flag = _flag - 1;
        }
        _flag = _flag + 1;
    }


    /** Initialize the actor by resetting the shift register state
     *  equal to the value of <i>initial</i>
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _inputNumberInvalid = true;
        _flag = 0;
    }

    /** Record the most recent shift register state as the new
     *  initial state for the next iteration.
     *  @exception IllegalActionException If the base class throws it
     */
    public boolean postfire() throws IllegalActionException {
        //_shiftReg = _latestShiftReg;
        // copy distance/path to tempDistance/Path
        for (int i = 0; i < _rowNum; i ++) {
            _tempDistance[i] = _distance[i];
            //System.out.print("temp path state for " + i + " ");    
            for (int j = 0; j < _flag; j ++) {
                _tempPath[i][j] = _path[i][j];
                //System.out.print(_tempPath[i][j] + " ");
            }
            //System.out.println(" ");
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

    private double _computeSoftDistance(double[] y, int truthValue, int maskNum) {
        double softDistance = 0.0;
        for (int i = maskNum -1; i >= 0; i --) {
            int truthBit = truthValue & 1;
            truthValue = truthValue >> 1;
            softDistance = softDistance + java.lang.Math.pow(y[i] - truthBit, 2);
        }
        return softDistance;
    }

    private IntToken[] _convertToBit(int integer, int length) {
        IntToken[] bit = new IntToken[length];
        for(int i = length -1; i >= 0; i --) {
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

    // Number bits the actor consumes per firing.
    private int _inputNumber;

    private int _stageNumber;

    // Number of polynomials.
    private int _maskNumber;

    // Polynomial array.
    private int[] _mask;

    // Length of the shift register.
    private int _shiftRegLength = 0;

    // A flag indicating that the private variable
    //  _inputNumber is invalid.
    private transient boolean _inputNumberInvalid = true;

    private int[][][] _truthTable;
    private int _rowNum;
    private int _colNum;

    private int _depth;
    private double[] _distance;
    private double[] _tempDistance;
    private int[][] _path;
    private int[][] _tempPath;
    private int _flag;

    // Since this actor always sends one of two tokens,
    // we statically create those tokens to avoid unnecessary
    // object construction.
    private static IntToken _tokenZero = new IntToken(0);
    private static IntToken _tokenOne = new IntToken(1);
}
