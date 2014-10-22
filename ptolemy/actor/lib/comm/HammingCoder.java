/* Hamming Coder.

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
//// HammingCoder

/**
 Encode the information symbols into Hamming code.
 Let <i>k</i> denotes parameter <i>uncodedRate</i> and <i>n</i> denotes
 parameter <i>codedRate</i>. During each firing, the actor consumes
 <i>k</i> bits and encode them into a block of code with length <i>n</i>.
 The rate of the code is <i>k/n</i>.
 <p>
 For a Hamming code, <i>k</i> and <i>n</i> must satisfy the following:
 <i>n</i> = 2<i><sup>m</sup></i> - 1
 <i>k</i> = 2<sup><i>m</i></sup> - 1 - <i>m</i>;
 where <i>m</i> is any positive integer. Note <i>m</i> = <i>n</i> - <i>k</i>.
 It is called the order of the Hamming code. The lowest order is <i>m</i> = 2,
 and (<i>n</i>, <i>k</i>) = (3, 1).
 <p>
 The generator matrix G is defined as:
 G<i><sub>k*n</sub></i> = [I<i><sub>k</sub></i> | P<i><sub>k*(n-k)</sub></i> ]
 where P is called the parity matrix.
 The subscript of a matrix indicates its dimension.
 <p>
 The parity check matrix H is defined as:
 H<sub><i>(n-k)*n</i></sub> = [P<sup>T</sup> | I<sub><i>n-k</i></sub> ]
 Each column of H must be one of the non-zero <i>n</i> = 2<sup><i>m</i></sup> - 1
 combinations of <i>m</i> bits.
 <p>
 To generate a Hamming code, the <i>k</i> information bits is considered
 as a row vector <i><u>X</u></i>. Its Hamming code is
 <i><u>Y</u></i> = <i><u>X</u></i> * G.
 Hence <i><u>Y</u></i> is a row vector of length <i>n</i>. The result is
 then sent to the output port in sequence.
 <p>
 For more information on Hamming codes, see Proakis, Digital
 Communications, Fourth Edition, McGraw-Hill, 2001, pp. 416-424.
 <p>
 @author Ye Zhou
 @version $Id$
 @since Ptolemy II 3.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class HammingCoder extends Transformer {
    /** Construct an actor with the given container and name.
     *  The output and trigger ports are also constructed.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public HammingCoder(CompositeEntity container, String name)
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
     *  @exception IllegalActionException If <i>codedRate</i>
     *  or <i>uncodedRate</i> is not positive.
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

            // set the output production rate.
            _outputRate.setToken(new IntToken(_codeSizeValue));
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

            // Set the input consumption rate.
            _inputRate.setToken(new IntToken(_uncodeSizeValue));
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
        HammingCoder newObject = (HammingCoder) super.clone(workspace);

        newObject._inputRate = (Parameter) newObject.input
                .getAttribute("tokenConsumptionRate");
        newObject._outputRate = (Parameter) newObject.output
                .getAttribute("tokenProductionRate");
        return newObject;
    }

    /** If the attributes has changed, check the validity of
     *  uncodedRate and codedRate. Generate the parity matrix.
     *  Read "uncodedRate" number of tokens from the input port
     *  and compute the parities. Send the parities in sequence to the
     *  output port.
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

            // Generate P.
            _parityMatrix = new int[_uncodeSizeValue][_order];

            int flag = 0;
            int index = 0;

            for (int i = 1; i <= _codeSizeValue; i++) {
                if (i == 1 << flag) {
                    flag++;
                } else {
                    for (int j = 0; j < _order; j++) {
                        _parityMatrix[index][j] = i >> _order - j - 1 & 1;
                    }

                    index++;
                }
            }

            _parameterInvalid = false;
        }

        // Read from the input; set up output size.
        Token[] inputToken = input.get(0, _uncodeSizeValue);
        BooleanToken[] result = new BooleanToken[_codeSizeValue];

        for (int i = 0; i < _uncodeSizeValue; i++) {
            result[i] = (BooleanToken) inputToken[i];
        }

        // Compute parities.
        int[] parity = new int[_order];

        // Initialize.
        for (int i = 0; i < _order; i++) {
            parity[i] = 0;
        }

        for (int i = 0; i < _uncodeSizeValue; i++) {
            for (int j = 0; j < _order; j++) {
                parity[j] = parity[j] ^ (result[i].booleanValue() ? 1 : 0)
                        & _parityMatrix[i][j];
            }
        }

        // Send the parity results to the output.
        for (int i = 0; i < _order; i++) {
            result[i + _uncodeSizeValue] = new BooleanToken(parity[i] == 1);
        }

        output.broadcast(result, result.length);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Consumption rate of the input port.
    private Parameter _inputRate;

    // Production rate of the output port.
    private Parameter _outputRate;

    // Uncode block length.
    private int _uncodeSizeValue;

    // Hamming codeword length.
    private int _codeSizeValue;

    // Order of the Hamming code.
    private int _order;

    // matrix "P" for this Hamming code.
    private int[][] _parityMatrix;

    // A flag indicating that the private variable
    // _inputNumber is invalid.
    private transient boolean _parameterInvalid = true;
}
