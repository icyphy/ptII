/* Produce a Hadamard codeword by selecting a row from a Hadamard matrix.

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

import ptolemy.actor.lib.Source;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// HadamardCode

/**
 Produce a Hadamard codeword by selecting a row from a Hadamard matrix.
 The log base 2 of the matrix dimension is given by the <i>log2Length</i>
 parameter, which should be a non-negative integer smaller than 32.
 The row index is given by the <i>index</i> parameter or by the associated
 <i>index</i> port, which should be a non-negative integer smaller
 than the matrix dimension. If the index changes value when the actor is
 in the middle of producing a sequence of Hadamard codeword, the actor
 will take on the new index value, and start to produce the new codeword
 from the beginning.
 <p>
 A Hadamard matrix is defined in the following way:
 <p>
 <i>H</i><sub>1</sub> = [1, 1; 1, -1]
 <p>
 <i>H</i><sub><i>n</i>+1</sub> = [<i>H</i><sub><i>n</i></sub>,
 <i>H</i><sub><i>n</i></sub>;
 <i>H</i><sub><i>n</i></sub>, -<i>H</i><sub><i>n</i></sub>]
 <p>
 where <i>n</i> is a positive integer.
 Therefore, H<sub><i>n</i></sub> is a 2<sup><i>n</i></sup> by
 2<sup><i>n</i></sup> square matrix.
 The codeword length is 2<sup><i>n</i></sup>.
 <p>
 The actor produces Hadamard codeword in booleans. Therefore, 1 is treated
 as "true" and -1 is treated as "false".
 <p>
 @author Edward A. Lee and Ye Zhou
 @version $Id$
 @since Ptolemy II 3.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class HadamardCode extends Source {
    /** Construct an actor with the given container and name.
     *  The output and trigger ports are also constructed.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public HadamardCode(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        index = new PortParameter(this, "index");
        index.setTypeEquals(BaseType.INT);
        index.setExpression("0");

        log2Length = new Parameter(this, "log2Length");
        log2Length.setTypeEquals(BaseType.INT);
        log2Length.setExpression("5");

        // Declare output data type.
        output.setTypeEquals(BaseType.BOOLEAN);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Index of the code to generate. Codes with different indexes
     *  are orthogonal.  This is an int with default 0. It should
     *  not exceed length-1, where length = 2^log2Length.
     */
    public PortParameter index;

    /** Log base 2 of the length of the code.  This is an integer with
     *  default 5.  It is required to be greater than 0.
     */
    public Parameter log2Length;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute being changed is <i>log2Length</i>, then
     *  calculate the new Hadamard sequence; if it is <i>index</i>,
     *  then verify that is non-negative and check if it is different
     *  from the previous value.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If <i>index</i> is negative
     *   or <i>log2Length</i> is not strictly positive.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == index) {
            int indexValue = ((IntToken) index.getToken()).intValue();

            if (indexValue < 0) {
                throw new IllegalActionException(this,
                        "index parameter is not permitted to be negative.");
            }

            // Set a flag indicating that the private variable _row
            // is invalid, but don't recompute the value until all
            // parameters have been set.
            if (indexValue != _previousIndex) {
                _rowValueInvalid = true;
            }
        } else if (attribute == log2Length) {
            int log2LengthValue = ((IntToken) log2Length.getToken()).intValue();

            if (log2LengthValue <= 0) {
                throw new IllegalActionException(this,
                        "log2Length parameter is required to be "
                                + "strictly positive.");
            }

            // Assuming an int is 32 bits, our implementation will only
            // work if this is less than 32.
            if (log2LengthValue >= 32) {
                throw new IllegalActionException(this,
                        "log2Length parameter is required to be "
                                + "less than 32.");
            }

            // Set a flag indicating that the private variable _row
            // is invalid, but don't recompute the value until all
            // parameters have been set.
            _rowValueInvalid = true;
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Read from the associated <i>index</i> port if there is any input.
     *  The actor compares the new index value with the old one.
     *  If the value changes, the actor will interrupt the current
     *  output sequence, compute the new Hadamard codeword, and send it
     *  to the output in sequence.
     *  If the index remains constant when it reaches the end of a
     *  Hadamard codeword sequence, the next iteration will restart
     *  from the beginning of that codeword.
     *  @exception IllegalActionException If <i>index</i> is out of range.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        index.update();

        if (_rowValueInvalid) {
            _latestIndex = ((IntToken) index.getToken()).intValue();

            int log2LengthValue = ((IntToken) log2Length.getToken()).intValue();

            // Power of two calculated using a shift.
            int matrixDimension = 1 << log2LengthValue;

            if (_latestIndex >= matrixDimension) {
                throw new IllegalActionException(this, "index is out of range.");
            }

            _row = _calculateRow(matrixDimension, _latestIndex);
            _rowValueInvalid = false;

            // Reset the index to start at the beginning of the
            // new sequence.
            _index = 0;
        }

        output.broadcast(new BooleanToken(_row[_index]));

        _index++;

        if (_index >= _row.length) {
            _index = 0;
        }
    }

    /** Initialize the actor by resetting the index counter to begin
     *  at the beginning of the Hadamard sequence.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        // Since the actor should always compute the Hadamard
        // sequence when it fires for the first time, the _previousIndex
        // is set to a value that _latestIndex can never take.
        // Thus the computation can be carried out.
        _previousIndex = -1;
        _index = 0;
    }

    @Override
    public boolean postfire() throws IllegalActionException {
        _previousIndex = _latestIndex;
        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Calculate Hardmard row given by the Hadamard matrix dimension
     *  and the row index. The method computes iteratively by degrading
     *  the matrix dimension into half, until it reaches H<sub>1</sub>.
     *  @param matrixDimension Hadamard matrix dimension.
     *  @param index Row index.
     *  @return Desired Hadamard row.
     */
    private boolean[] _calculateRow(int matrixDimension, int index) {
        // NOTE: Don't need to check the arguments for validity
        // because this is a private method, and the usage pattern
        // guarantees that matrixDimension is a power of 2 and that
        // index is in range.
        // NOTE: use <= in case a bug somewhere results in this
        // dropping to one or zero.  Shouldn't happen. In theory,
        // == is sufficient. However, such a bug would lead to
        // an infinite recursion and stack overflow, which is a
        // particularly nasty error.
        if (matrixDimension <= 2) {
            if (index == 0) {
                return _row0;
            } else {
                return _row1;
            }
        } else {
            boolean[] result = new boolean[matrixDimension];
            int halfDimension = matrixDimension / 2;
            int indexIntoHalfMatrix = index;

            if (index >= halfDimension) {
                indexIntoHalfMatrix -= halfDimension;
            }

            boolean[] halfRow = _calculateRow(halfDimension,
                    indexIntoHalfMatrix);
            System.arraycopy(halfRow, 0, result, 0, halfDimension);

            if (index >= halfDimension) {
                for (int i = 0; i < halfDimension; i++) {
                    result[halfDimension + i] = !halfRow[i];
                }
            } else {
                System.arraycopy(halfRow, 0, result, halfDimension,
                        halfDimension);
            }

            return result;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////               private variable                     ////
    // Index of the element in the Hadamard row.
    private int _index;

    // The previous index value from the input port.
    private int _previousIndex;

    // The current index value from the input port.
    private int _latestIndex;

    // Hadamard row computed from _calculateRow.
    private boolean[] _row;

    // Rows of H<sub>1</sub>.
    private static boolean[] _row0 = { true, true };

    private static boolean[] _row1 = { true, false };

    // A flag indicating that the private variable _row is invalid.
    private transient boolean _rowValueInvalid = true;
}
