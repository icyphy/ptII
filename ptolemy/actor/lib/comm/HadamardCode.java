/* Source of Hadamard codes.

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

import ptolemy.actor.lib.Source;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// HadamardCode
/**
FIXME: document

@author Edward A. Lee and Rachel Zhou
@version $Id$
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
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        index = new Parameter(this, "index");
        index.setTypeEquals(BaseType.INT);
        index.setExpression("0");

        log2Length = new Parameter(this, "log2Length");
        log2Length.setTypeEquals(BaseType.INT);
        log2Length.setExpression("5");

        // Declare output data type.
        output.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Index of the code to generate. Codes with different indexes
     *  are orthogonal.  This is an int with default 0. It should
     *  not exceed length-1, where length = 2^log2Length.
     */
    public Parameter index;

    /** Log base 2 of the length of the code.  This is an integer with
     *  default 6.  It is required to be greater than 0.
     */
    public Parameter log2Length;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute being changed is <i>log2Length</i>, then
     *  calculate the new Hadamard sequence; if it is <i>index</i>,
     *  then verify that is non-negative.
     *  @exception IllegalActionException If <i>index</i> is negative
     *   or <i>log2Length</i> is not strictly positive.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == index) {
            int indexValue = ((IntToken)index.getToken()).intValue();
            if (indexValue < 0) {
                throw new IllegalActionException(this,
                "index parameter is not permitted to be negative.");
            }
            // Set a flag indicating that the private variable _row
            // is invalid, but don't recompute the value until all
            // parameters have been set.
            _rowValueInvalid = true;
        } else if (attribute == log2Length) {
            int log2LengthValue = ((IntToken)log2Length.getToken()).intValue();
            if (log2LengthValue <= 0) {
                throw new IllegalActionException(this,
                "log2Length parameter is required to be strictly positive.");
            }
            // Assuming an int is 32 bits, our implementation will only
            // work if this is less than 32.
            if (log2LengthValue >= 32) {
                throw new IllegalActionException(this,
                "log2Length parameter is required to be less than 32.");
            }
            // Set a flag indicating that the private variable _row
            // is invalid, but don't recompute the value until all
            // parameters have been set.
            _rowValueInvalid = true;
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Read at most one input token from each channel of the trigger
     *  input and discard it.  If the trigger input is not connected,
     *  then this method does nothing.  Derived classes should be
     *  sure to call super.fire(), or to consume the trigger input
     *  tokens themselves, so that they aren't left unconsumed.
     *  @exception IllegalActionException If <i>index</i> is out of range.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        if (_rowValueInvalid) {
            int log2LengthValue = ((IntToken)log2Length.getToken()).intValue();
            int indexValue = ((IntToken)index.getToken()).intValue();
            // Power of two calculated using a shift.
            int matrixDimension = 1 << log2LengthValue;
            if (indexValue >= matrixDimension) {
                throw new IllegalActionException(this,
                "index is out of range.");
            }
            _row = _calculateRow(matrixDimension, indexValue);
            _rowValueInvalid = false;
            // Reset the index to start at the beginning of the
            // new sequence.
            _index = 0;
        }
        if (_row[_index] == 1) {
            output.broadcast(_tokenOne);
        } else {
            output.broadcast(_tokenMinusOne);
        }
        _index++;
        if (_index >= _row.length) {
            _index = 0;
        }
    }

    /** Initialize the actor by resetting the index counter to begin
     *  at the beginning of the Hadamard sequence.
     *  @exception IllegalActionException If there is no director.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _index = 0;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // FIXME: Document
    private int[] _calculateRow(int matrixDimension, int index) {
        // NOTE: Don't need to check the arguments for validity
        // because this is a private method, and the usage pattern
        // guarantees that matrixDimension is a power of 2 and that
        // index is in range.

        // NOTE: use <= in case a bug somewhere results in this
        // dropping to one or zero.  Shouldn't happen.  In theory,
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
            int[] result = new int[matrixDimension];
            int halfDimension = matrixDimension/2;
            int indexIntoHalfMatrix = index;
            if (index >= halfDimension) {
                indexIntoHalfMatrix -= halfDimension;
            }
            int[] halfRow = _calculateRow(halfDimension, indexIntoHalfMatrix);
            System.arraycopy(halfRow, 0, result, 0, halfDimension);
            if (index >= halfDimension) {
                for(int i=0; i<halfDimension; i++) {
                    result[halfDimension+i] = -halfRow[i];
                }
            } else {
                System.arraycopy(
                        halfRow, 0, result, halfDimension, halfDimension);
            }
            return result;
        }
    }

    // FIXME: Document these.
    private int _index;
    private int[] _row;
    private static int[] _row0 = {1, 1};
    private static int[] _row1 = {1, -1};
    private transient boolean _rowValueInvalid = true;

    // Since this actor always sends one of two tokens, we statically
    // create those tokens to avoid unnecessary object construction.
    private static IntToken _tokenMinusOne = new IntToken(-1);
    private static IntToken _tokenOne = new IntToken(1);
}
