/* Hamming Decoder.

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
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// HammingDecoder

/**
 Decode a (<i>n</i>, <i>k</i>) Hamming code, where <i>n</i> is specified by
 parameter <i>codedRate</i> and <i>k</i> is specified by parameter
 <i>uncodedRate</i>.
 <p>
 The Hamming code can correct one-bit error.
 To encode a Hamming code, the HammingCoder consumes <i>k</i> information bits
 during each firing and consider them as a row vector <i><u>X</u></i>. Its
 Hamming code is <i><u>Y</u></i> = <i><u>X</u></i> * G.
 <p>
 If there is no error in <i><u>Y</u></i>,
 <i><u>Y</u></i> * H<sup>T</sup> should be a zero vector of length <i>n - k</i>.
 Otherwise <i><u>S</u></i> =  <i><u>Y</u></i> * H<sup>T</sup> is called the
 syndrome. Let <i><u>S</u></i> be the i-th column of H. The HammingDecoder
 declares there is an error at the i-th element of <i><u>Y</u></i>.
 <p>
 For more information on Hamming codes, see HammingCoder and Proakis, Digital
 Communications, Fourth Edition, McGraw-Hill, 2001, pp. 448-450.
 <p>
 @author Ye Zhou
 @version $Id$
 @since Ptolemy II 3.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (cxh)
 @see HammingCoder
 */
public class HammingDecoder extends Transformer {
    /** Construct an actor with the given container and name.
     *  The output and trigger ports are also constructed.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public HammingDecoder(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        uncodedRate = new Parameter(this, "uncodedRate");
        uncodedRate.setTypeEquals(BaseType.INT);
        uncodedRate.setExpression("4");

        codedRate = new Parameter(this, "codedRate");
        codedRate.setTypeEquals(BaseType.INT);
        codedRate.setExpression("7");

        // Declare data types, consumption rate and production rate.
        input.setTypeEquals(BaseType.BOOLEAN);
        _inputRate = new Parameter(input, "tokenConsumptionRate", new IntToken(
                1));
        output.setTypeEquals(BaseType.BOOLEAN);
        _outputRate = new Parameter(output, "tokenProductionRate",
                new IntToken(1));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Integer defining the uncode block size. It should be a positive
     *  integer. Its default value is the integer 4.
     */
    public Parameter uncodedRate;

    /** Integer defining the Hamming code block size.
     *  This parameter should be a non-negative integer.
     *  Its default value is the integer 7.
     */
    public Parameter codedRate;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute being changed is <i>uncodedRate</i> or
     *  <i>uncodedRate</i>, then verify that it is a positive integer.
     *  Set the tokenConsumptionRate and tokenProductionRate.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If <i>initialState</i> is negative
     *  or <i>uncodedRate</i> is non-positive or any element of
     *  <i>polynomialArray</i> is non-positive.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == codedRate) {
            _codeSizeValue = ((IntToken) codedRate.getToken()).intValue();

            if (_codeSizeValue <= 0) {
                throw new IllegalActionException(this,
                        "codedRate must be positive.");
            }

            // set the input consumption rate.
            _inputRate.setToken(new IntToken(_codeSizeValue));
        } else if (attribute == uncodedRate) {
            _uncodeSizeValue = ((IntToken) uncodedRate.getToken()).intValue();

            if (_uncodeSizeValue < 1) {
                throw new IllegalActionException(this,
                        "uncodedRate must be non-negative.");
            }

            // Set a flag indicating the private variables
            // _uncodeSizeValue and/or _codeSizeValue is invalid,
            // but do not compute the value until all parameters
            // have been set.
            _parameterInvalid = true;

            // Set the output production rate.
            _outputRate.setToken(new IntToken(_uncodeSizeValue));
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
        HammingDecoder newObject = (HammingDecoder) super.clone(workspace);

        newObject._inputRate = (Parameter) newObject.input
                .getAttribute("tokenConsumptionRate");
        newObject._outputRate = (Parameter) newObject.output
                .getAttribute("tokenProductionRate");
        return newObject;
    }

    /** If the attributes has changed, check the validity of
     *  uncodedRate and codedRate. Generate the parity matrix.
     *  Read "uncodedRate" number of tokens from the input port
     *  and compute the syndrome. If the syndrome is non-zero, correct
     *  one-bit error and send the decoded result to the output.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (_parameterInvalid) {
            if (_uncodeSizeValue >= _codeSizeValue) {
                throw new IllegalActionException(this,
                        "codedRate must be greater than uncodedRate.");
            }

            _order = _codeSizeValue - _uncodeSizeValue;

            if (_codeSizeValue != (1 << _order) - 1) {
                throw new IllegalActionException(this,
                        "Invalid pair of uncodedRate and codedRate.");
            }

            _parityMatrix = new int[_uncodeSizeValue][_order];

            // Look-up table for correcting one-bit error in Hamming code.
            // The syndrome is expressed by an integer value "i".
            // _index[i] is the position that the one-bit error occurs.
            // When "i" equals 0, it means no error.
            // Note Hamming code cannot correct more than one errors.
            _index = new int[_codeSizeValue + 1];
            _index[0] = _codeSizeValue;

            int flag = 0;
            int pos = 0;

            // Generate the parity matrix and look-up table.
            for (int i = 1; i <= _codeSizeValue; i++) {
                if (i == 1 << flag) {
                    _index[i] = _codeSizeValue - 1 - flag;
                    flag++;
                } else {
                    _index[i] = pos;

                    for (int j = 0; j < _order; j++) {
                        _parityMatrix[pos][j] = i >> _order - j - 1 & 1;
                    }

                    pos++;
                }
            }

            _parameterInvalid = false;
        }

        // Read from the input; set up output size.
        Token[] inputToken = input.get(0, _codeSizeValue);
        BooleanToken[] input = new BooleanToken[_codeSizeValue];

        for (int i = 0; i < _codeSizeValue; i++) {
            input[i] = (BooleanToken) inputToken[i];
        }

        // Compute syndrome.
        int[] syndrome = new int[_order];

        // Initialize.
        for (int i = 0; i < _order; i++) {
            syndrome[i] = 0;
        }

        int eValue = 0;

        for (int i = 0; i < _order; i++) {
            for (int j = 0; j < _uncodeSizeValue; j++) {
                syndrome[i] = syndrome[i] ^ (input[j].booleanValue() ? 1 : 0)
                        & _parityMatrix[j][i];
            }

            syndrome[i] = syndrome[i]
                    ^ (input[i + _uncodeSizeValue].booleanValue() ? 1 : 0);
            eValue = eValue << 1 | syndrome[i];
        }

        int eIndex = _index[eValue];

        if (eIndex < _uncodeSizeValue) {
            input[eIndex] = new BooleanToken(!input[eIndex].booleanValue());
        }

        output.broadcast(input, _uncodeSizeValue);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Consumption rate of the input port.
    private Parameter _inputRate;

    // Production rate of the output port.
    private Parameter _outputRate;

    // Uncode block length.
    private int _uncodeSizeValue;

    // Codeword length of the Hamming code.
    private int _codeSizeValue;

    // Order of the Hamming code.
    private int _order;

    // Matrix "P".
    private int[][] _parityMatrix;

    // Look-up table for correcting one-bit error.
    private int[] _index;

    // A flag indicating that the private variable
    // _inputNumber is invalid.
    private transient boolean _parameterInvalid = true;
}
