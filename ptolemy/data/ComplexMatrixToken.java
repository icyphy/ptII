/* A token that contains a 2-D Complex matrix.

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

@ProposedRating Green (neuendor@eecs.berkeley.edu)
@AcceptedRating Yellow (cxh@eecs.berkeley.edu)
*/

package ptolemy.data;

import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.expr.PtParser;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.data.type.TypeLattice;
import ptolemy.graph.CPO;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.math.Complex;
import ptolemy.math.ComplexMatrixMath;

//////////////////////////////////////////////////////////////////////////
//// ComplexMatrixToken
/**
A token that contains a 2-D Complex matrix.

@see ptolemy.math.Complex
@author Yuhong Xiong, Christopher Hylands, Steve Neuendorffer
@version $Id$
@since Ptolemy II 0.2
*/
public class ComplexMatrixToken extends MatrixToken {

    /** Construct an ComplexMatrixToken with a one by one matrix. The
     *  only element in the matrix has value 0.0
     */
    public ComplexMatrixToken() {
        _rowCount = 1;
        _columnCount = 1;
        _value = new Complex[1][1];
        _value[0][0] = Complex.ZERO;
    }

    /** Construct a ComplexMatrixToken with the specified 2-D matrix.
     *  Make a copy of the matrix and store the copy,
     *  so that changes on the specified matrix after this token is
     *  constructed will not affect the content of this token.
     *  @exception IllegalActionException If the specified matrix
     *   is null.
     */
    public ComplexMatrixToken(final Complex[][] value)
            throws IllegalActionException {
        if (value == null) {
            throw new IllegalActionException("ComplexMatrixToken: The "
                    + "specified matrix is null.");
        }
        _initialize(value, DO_COPY);
    }

    /** Construct a ComplexMatrixToken with the specified 2-D matrix.
     *  If copy is DO_COPY, make a copy of the matrix and store the copy,
     *  so that changes on the specified matrix after this token is
     *  constructed will not affect the content of this token.
     *  If copy is DO_NOT_COPY, just reference the matrix (do not copy
     *  its contents). This saves some time and memory.
     *  The argument matrix should NOT be modified after this constructor
     *  is called to preserve immutability.
     *  <p>
     *  Since the DO_NOT_COPY option requires some care, this constructor
     *  is protected.
     *  @exception IllegalActionException If the specified matrix
     *   is null.
     */
    public ComplexMatrixToken(final Complex[][] value, int copy)
            throws IllegalActionException {
        if (value == null) {
            throw new IllegalActionException("ComplexMatrixToken: The "
                    + "specified matrix is null.");
        }
        _initialize(value, copy);
    }

    /** Construct an ComplexMatrixToken from the specified string.
     *  @param init A string expression of a 2-D complex matrix.
     *  @exception IllegalActionException If the string does
     *   not contain a parsable 2-D complex matrix.
     */
    public ComplexMatrixToken(String init) throws IllegalActionException {
        PtParser parser = new PtParser();
        ASTPtRootNode tree = parser.generateParseTree(init);
        Token token = (new ParseTreeEvaluator()).evaluateParseTree(tree);
        if (token instanceof ComplexMatrixToken) {
            Complex[][] value = ((ComplexMatrixToken)token).complexMatrix();
            _initialize(value, DO_COPY);
        } else {
            throw new IllegalActionException("A ComplexMatrixToken cannot be"
                    + " created from the expression '" + init + "'");
        }
    }

    /** Construct an ComplexMatrixToken from the specified array of
     *  tokens.  The tokens in the array must be scalar tokens
     *  convertible into integers.
     *  @param tokens The array of tokens, which must contains
     *  rows*columns ScalarTokens.
     *  @param rows The number of rows in the matrix to be created.
     *  @param columns The number of columns in the matrix to be
     *  created.
     *  @exception IllegalActionException If the array of tokens is
     *  null, or the length of the array is not correct, or if one of
     *  the elements of the array is null, or if one of the elements
     *  of the array cannot be losslessly converted to an integer.
     */
    public ComplexMatrixToken(Token[] tokens, int rows, int columns)
            throws IllegalActionException {
        if (tokens == null) {
            throw new IllegalActionException(
                    "ComplexMatrixToken: The specified"
                    + " array is null.");
        }
        if (tokens.length != rows * columns) {
            throw new IllegalActionException(
                    "ComplexMatrixToken: The specified"
                    + " array is not of the correct length");
        }
        _rowCount = rows;
        _columnCount = columns;
        _value = new Complex[rows][columns];
        for (int i = 0; i < tokens.length; i++) {
            Token token = tokens[i];
            if (token instanceof ScalarToken) {
                _value[i / columns][i % columns] =
                    ((ScalarToken)token).complexValue();
            } else {
                throw new IllegalActionException("ComplexMatrixToken: Element "
                        + i + " in the array with value " + token +
                        " is not a ScalarToken");
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the content of this token as a new 2-D Complex matrix.
     *  @return A 2-D Complex matrix
     */
    public Complex[][] complexMatrix() {
        return ComplexMatrixMath.allocCopy(_value);
    }

    /** Convert the specified token into an instance of ComplexMatrixToken.
     *  This method does lossless conversion.
     *  If the argument is already an instance of ComplexMatrixToken,
     *  it is returned without any change. Otherwise, if the argument
     *  is below ComplexMatrixToken in the type hierarchy, it is converted to
     *  an instance of ComplexMatrixToken or one of the subclasses of
     *  ComplexMatrixToken and returned. If none of the above condition is
     *  met, an exception is thrown.
     *  @param token The token to be converted to a ComplexMatrixToken.
     *  @return A ComplexMatrixToken
     *  @exception IllegalActionException If the conversion cannot
     *   be carried out.
     */
    public static ComplexMatrixToken convert(Token token)
            throws IllegalActionException {
        if (token instanceof ComplexMatrixToken) {
            return (ComplexMatrixToken)token;
        }

        int compare = TypeLattice.compare(BaseType.COMPLEX_MATRIX,
                token);
        if (compare == CPO.LOWER || compare == CPO.INCOMPARABLE) {
            throw new IllegalActionException(
                    notSupportedIncomparableConversionMessage(
                            token, "[complex]"));
        }

        // try Complex
        //  compare = TypeLattice.compare(BaseType.COMPLEX, token);
        //         if (compare == CPO.SAME || compare == CPO.HIGHER) {
        //             Complex[][] result = new Complex[1][1];
        //             ComplexToken tem = ComplexToken.convert(token);
        //             result[0][0] = tem.complexValue();
        //             return new ComplexMatrixToken(result);
        //         }

        // try DoubleMatrix
        compare = TypeLattice.compare(BaseType.DOUBLE_MATRIX, token);
        if (compare == CPO.SAME || compare == CPO.HIGHER) {
            DoubleMatrixToken tem = DoubleMatrixToken.convert(token);
            return new ComplexMatrixToken(tem.complexMatrix());
        }

        // The argument is below ComplexMatrixToken in the type hierarchy,
        // but I don't recognize it.
        throw new IllegalActionException(
                notSupportedConversionMessage(token, "[complex]"));
    }

    /** Return true if the argument is an instance of ComplexMatrixToken
     *  of the same dimensions and the corresponding elements of the matrices
     *  are equal.
     *  @param object An instance of Object.
     *  @return True if the argument is an instance of ComplexMatrixToken
     *   of the same dimensions and the corresponding elements of the
     *   matrices are equal.
     */
    public boolean equals(Object object) {
        // This test rules out instances of a subclass.
        if (object.getClass() != getClass()) {
            return false;
        }

        ComplexMatrixToken matrixArgument = (ComplexMatrixToken)object;
        if (_rowCount != matrixArgument.getRowCount()) {
            return false;
        }
        if (_columnCount != matrixArgument.getColumnCount()) {
            return false;
        }

        Complex[][] matrix = matrixArgument.complexMatrix();
        for (int i = 0; i < _rowCount; i++) {
            for (int j = 0; j < _columnCount; j++) {
                if ( !_value[i][j].equals(matrix[i][j])) {
                    return false;
                }
            }
        }

        return true;
    }

    /** Return the number of columns in the matrix.
     *  @return The number of columns in the matrix.
     */
    public int getColumnCount() {
        return _columnCount;
    }

    /** Return the element of the matrix at the specified
     *  row and column in a ComplexToken.
     *  @param row The row index of the desired element.
     *  @param column The column index of the desired element.
     *  @return A ComplexToken containing the matrix element.
     *  @exception ArrayIndexOutOfBoundsException If the specified
     *   row or column number is outside the range of the matrix.
     */
    public Token getElementAsToken(final int row, final int column)
            throws ArrayIndexOutOfBoundsException {
        return new ComplexToken(_value[row][column]);
    }

    /** Return the element of the contained matrix at the specified
     *  row and column.
     *  @param row The row index of the desired element.
     *  @param column The column index of the desired element.
     *  @return The Complex at the specified matrix entry.
     *  @exception ArrayIndexOutOfBoundsException If the specified
     *   row or column number is outside the range of the matrix.
     */
    public Complex getElementAt(final int row, final int column) {
        return _value[row][column];
    }

    /** Return the Type of the tokens contained in this matrix token.
     *  This must be a type representing a scalar token.
     *  @return BaseType.COMPLEX.
     */
    public Type getElementType() {
        return BaseType.COMPLEX;
    }

    /** Return the number of rows in the matrix.
     *  @return The number of rows in the matrix.
     */
    public int getRowCount() {
        return _rowCount;
    }

    /** Return the type of this token.
     *  @return BaseType.COMPLEX_MATRIX
     */
    public Type getType() {
        return BaseType.COMPLEX_MATRIX;
    }

    /** Return a hash code value for this token. This method returns the
     *  integer portion of the magnitude of the sum of the elements.
     *  @return A hash code value for this token.
     */
    public int hashCode() {
        Complex sum = new Complex(0);
        for (int i = 0; i < _rowCount; i++) {
            for (int j = 0; j < _columnCount; j++) {
                sum = sum.add(_value[i][j]);
            }
        }

        return (int)sum.magnitude();
    }

    /** Return a new Token representing the left multiplicative
     *  identity. The returned token contains an identity matrix
     *  whose dimensions are the same as the number of rows of
     *  the matrix contained in this token.
     *  @return A new ComplexMatrixToken containing the left multiplicative
     *   identity.
     */
    public Token one() {
        try {
            return new ComplexMatrixToken(
                    ComplexMatrixMath.identity(_rowCount), DO_NOT_COPY);
        } catch (IllegalActionException illegalAction) {
            // should not happen
            throw new InternalErrorException("ComplexMatrixToken.one: "
                    + "Cannot create identity matrix.");
        }
    }

    /** Return a new Token representing the right multiplicative
     *  identity. The returned token contains an identity matrix
     *  whose dimensions are the same as the number of columns of
     *  the matrix contained in this token.
     *  @return A new ComplexMatrixToken containing the right
     *   multiplicative identity.
     */
    public Token oneRight() {
        try {
            return new ComplexMatrixToken(
                    ComplexMatrixMath.identity(_columnCount), DO_NOT_COPY);
        } catch (IllegalActionException illegalAction) {
            // should not happen
            throw new InternalErrorException("ComplexMatrixToken.oneRight: "
                    + "Cannot create identity matrix.");
        }
    }

    /** Return a new Token representing the additive identity.
     *  The returned token contains a matrix whose elements are
     *  all zero, and the size of the matrix is the same as the
     *  matrix contained in this token.
     *  @return A new ComplexMatrixToken containing the additive identity.
     */
    public Token zero() {
        try {
            return new ComplexMatrixToken(
                    ComplexMatrixMath.zero(_rowCount, _columnCount),
                    DO_NOT_COPY);
        } catch (IllegalActionException illegalAction) {
            // should not happen
            throw new InternalErrorException("ComplexMatrixToken.zero: "
                    + "Cannot create zero matrix.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a new token whose value is the value of the argument
     *  Token added to the value of this Token.  It is assumed that
     *  the type of the argument is ComplexMatrixToken.
     *  @param rightArgument The token to add to this token.
     *  @exception IllegalActionException If the units are not
     *  compatible, or this operation is not supported by the derived
     *  class.
     *  @return A new ComplexMatrixToken containing the result.
     */
    protected MatrixToken _add(MatrixToken rightArgument)
            throws IllegalActionException {
        ComplexMatrixToken convertedArgument =
            (ComplexMatrixToken)rightArgument;
        Complex[][] result = ComplexMatrixMath.add(_value,
                convertedArgument._getInternalComplexMatrix());
        return new ComplexMatrixToken(result);
    }

    /** Return a new token whose value is the value of the argument
     *  Token added to the value of each element of this Token. It is
     *  assumed that the type of the argument is the same as the type
     *  of each element of this class.
     *  @param rightArgument The token to add to this token.
     *  @exception IllegalActionException If this operation is not
     *  supported by the derived class.
     *  @return A new ComplexMatrixToken containing the result.
     */
    protected MatrixToken _addElement(Token rightArgument)
            throws IllegalActionException {
        Complex scalar = ((ComplexToken)rightArgument).complexValue();
        Complex[][] result = ComplexMatrixMath.add(_value, scalar);
        return new ComplexMatrixToken(result);
    }

    /** Return a new token whose elements are the result of dividing
     *  the elements of this token by the argument. It is
     *  assumed that the type of the argument is the same as the type
     *  of each element of this class.
     *  @param rightArgument The token that divides this token.
     *  @exception IllegalActionException If this operation is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
     */
    protected MatrixToken _divideElement(Token rightArgument)
            throws IllegalActionException {
        Complex scalar = ((ComplexToken)rightArgument).complexValue();
        Complex[][] result = ComplexMatrixMath.divide(_value, scalar);
        return new ComplexMatrixToken(result);
    }

    /** Return a reference to the internal 2-D matrix of complex
     *  numbers that represents this Token. Because no copying is
     *  done, the contents must NOT be modified to preserve the
     *  immutability of Token.
     *  @return A 2-D complex Java matrix.
     */
    protected Complex[][] _getInternalComplexMatrix() {
        return _value;
    }

    /** Return a new token whose value is the value of the argument
     *  Token multiplied to the value of this Token.  It is assumed that
     *  the type of the argument is ComplexMatrixToken.
     *  @param rightArgument The token to multiply this token by.
     *  @exception IllegalActionException If the units are not
     *  compatible, or this operation is not supported by the derived
     *  class.
     *  @return A new ComplexMatrixToken containing the result.
     */
    protected MatrixToken _multiply(MatrixToken rightArgument)
            throws IllegalActionException {
        ComplexMatrixToken convertedArgument =
            (ComplexMatrixToken)rightArgument;
        Complex[][] result = ComplexMatrixMath.multiply(_value,
                convertedArgument._getInternalComplexMatrix());
        return new ComplexMatrixToken(result);
    }

    /** Return a new token whose value is the value of this token
     *  multiplied by the value of the argument token.
     *  @param rightArgument The token to multiply this token by.
     *  @exception IllegalActionException If this method is not
     *   supported by the derived class.
     *  @return A new ComplexMatrixToken containing the result.
     */
    protected MatrixToken _multiplyElement(Token rightArgument)
            throws IllegalActionException {
        Complex scalar = ((ComplexToken)rightArgument).complexValue();
        Complex[][] result = ComplexMatrixMath.multiply(_value, scalar);
        return new ComplexMatrixToken(result);
    }

    /** Return a new token whose value is the value of the argument
     *  Token subtracted to the value of this Token.  It is assumed that
     *  the type of the argument is ComplexMatrixToken.
     *  @param rightArgument The token to subtract to this token.
     *  @exception IllegalActionException If the units are not
     *  compatible, or this operation is not supported by the derived
     *  class.
     *  @return A new ComplexMatrixToken containing the result.
     */
    protected MatrixToken _subtract(MatrixToken rightArgument)
            throws IllegalActionException {
        ComplexMatrixToken convertedArgument =
            (ComplexMatrixToken)rightArgument;
        Complex[][] result = ComplexMatrixMath.subtract(_value,
                convertedArgument._getInternalComplexMatrix());
        return new ComplexMatrixToken(result);
    }

    /** Return a new token whose value is the value of the argument
     *  Token subtracted from the value of each element of this Token. It is
     *  assumed that the type of the argument is the same as the type
     *  of each element of this class.
     *  @param rightArgument The token to subtract from this token.
     *  @exception IllegalActionException If this operation is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
     */
    protected MatrixToken _subtractElement(Token rightArgument)
            throws IllegalActionException {
        Complex scalar = ((ComplexToken)rightArgument).complexValue();
        Complex[][] result = ComplexMatrixMath.add(_value, scalar.negate());
        return new ComplexMatrixToken(result);
    }

    /** Return a new token whose value is the value of the argument
     *  Token subtracted from the value of each element of this Token. It is
     *  assumed that the type of the argument is the same as the type
     *  of each element of this class.
     *  @param rightArgument The token to subtract from this token.
     *  @exception IllegalActionException If this operation is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
     */
    protected MatrixToken _subtractElementReverse(Token rightArgument)
            throws IllegalActionException {
        Complex scalar = ((ComplexToken)rightArgument).complexValue();
        Complex[][] result = ComplexMatrixMath.negative(
                ComplexMatrixMath.add(_value, scalar.negate()));
        return new ComplexMatrixToken(result);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // initialize the row and column count and copy the specified
    // matrix. This method is used by the constructors.
    private void _initialize(Complex[][] value, int copy) {
        _rowCount = value.length;
        _columnCount = value[0].length;

        if (copy == DO_NOT_COPY) {
            _value = value;
        } else {
            _value = ComplexMatrixMath.allocCopy(value);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private Complex[][] _value = null;
    private int _rowCount = 0;
    private int _columnCount = 0;
}
