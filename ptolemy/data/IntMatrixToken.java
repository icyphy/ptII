/* A token that contains a 2-D int matrix.

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
@AcceptedRating Yellow (wbwu@eecs.berkeley.edu)

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
import ptolemy.math.DoubleMatrixMath;
import ptolemy.math.IntegerMatrixMath;

//////////////////////////////////////////////////////////////////////////
//// IntMatrixToken
/**
A token that contains a 2-D int matrix.

@author Yuhong Xiong, Jeff Tsay, Steve Neuendorffer
@version $Id$
@since Ptolemy II 0.2
*/
public class IntMatrixToken extends MatrixToken {

    /** Construct an IntMatrixToken with a one by one matrix. The
     *  only element in the matrix has value 0.0
     */
    public IntMatrixToken() {
        int[][] value = new int[1][1];
        value[0][0] = 0;
        _initialize(value, DO_NOT_COPY);
    }

    /** Construct a IntMatrixToken with the specified 1-D matrix.
     *  Make a copy of the matrix and store the copy,
     *  so that changes on the specified matrix after this token is
     *  constructed will not affect the content of this token.
     *  @exception IllegalActionException If the specified matrix
     *   is null.
     */
    public IntMatrixToken(int[] value, int rows, int columns)
            throws IllegalActionException {
        if (value == null) {
            throw new IllegalActionException("IntMatrixToken: The specified "
                    + "matrix is null.");
        }
        _rowCount = rows;
        _columnCount = columns;
        _value = IntegerMatrixMath.toMatrixFromArray(value, rows, columns);
    }

    /** Construct a IntMatrixToken with the specified 2-D matrix.
     *  Make a copy of the matrix and store the copy,
     *  so that changes on the specified matrix after this token is
     *  constructed will not affect the content of this token.
     *  @exception IllegalActionException If the specified matrix
     *   is null.
     */
    public IntMatrixToken(int[][] value) throws IllegalActionException {
        this(value, DO_COPY);
    }

    /** Construct a IntMatrixToken with the specified 2-D matrix.
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
    public IntMatrixToken(int[][] value, int copy)
            throws IllegalActionException {
        if (value == null) {
            throw new IllegalActionException("IntMatrixToken: The specified "
                    + "matrix is null.");
        }
        _initialize(value, copy);
    }

    /** Construct an IntMatrixToken from the specified string.
     *  @param init A string expression of a 2-D int matrix.
     *  @exception IllegalActionException If the string does
     *   not contain a parsable 2-D int matrix.
     */
    public IntMatrixToken(String init) throws IllegalActionException {
        PtParser parser = new PtParser();
        ASTPtRootNode tree = parser.generateParseTree(init);
        Token token = (new ParseTreeEvaluator()).evaluateParseTree(tree);
        if (token instanceof IntMatrixToken) {
            int[][] value = ((IntMatrixToken)token).intMatrix();
            _initialize(value, DO_COPY);
        } else {
            throw new IllegalActionException("A record token cannot be"
                    + " created from the expression '" + init + "'");
        }
    }

    /** Construct an IntMatrixToken from the specified array of
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
    public IntMatrixToken(Token[] tokens, int rows, int columns)
            throws IllegalActionException {
        if (tokens == null) {
            throw new IllegalActionException("IntMatrixToken: The specified"
                    + " array is null.");
        }
        if (tokens.length != rows * columns) {
            throw new IllegalActionException("IntMatrixToken: The specified"
                    + " array is not of the correct length");
        }
        _rowCount = rows;
        _columnCount = columns;
        _value = new int[rows][columns];
        for (int i = 0; i < tokens.length; i++) {
            Token token = tokens[i];
            if (token instanceof ScalarToken) {
                _value[i / columns][i % columns] =
                    ((ScalarToken)token).intValue();
            } else {
                throw new IllegalActionException("IntMatrixToken: Element "
                        + i + " in the array with value " + token +
                        " is not a ScalarToken");
            }
        }
    }

    public IntMatrixToken(Token[] starts, Token[] increments, Token[] ends) {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the content of this token as a 2-D Complex matrix.
     *  @return A 2-D Complex matrix
     */
    public Complex[][] complexMatrix() {
        return DoubleMatrixMath.toComplexMatrix(doubleMatrix());
    }

    /** Convert the specified token into an instance of IntMatrixToken.
     *  This method does lossless conversion.
     *  If the argument is already an instance of IntMatrixToken,
     *  it is returned without any change. Otherwise, if the argument
     *  is below IntMatrixToken in the type hierarchy, it is converted to
     *  an instance of IntMatrixToken or one of the subclasses of
     *  IntMatrixToken and returned. If none of the above condition is
     *  met, an exception is thrown.
     *  @param token The token to be converted to a IntMatrixToken.
     *  @return A IntMatrixToken
     *  @exception IllegalActionException If the conversion cannot
     *   be carried out.
     */
    public static IntMatrixToken convert(Token token)
            throws IllegalActionException {
        if (token instanceof IntMatrixToken) {
            return (IntMatrixToken)token;
        }

        int compare = TypeLattice.compare(BaseType.INT_MATRIX, token);
        if (compare == CPO.LOWER || compare == CPO.INCOMPARABLE) {
            throw new IllegalActionException(
                    notSupportedIncomparableConversionMessage(
                            token, "[int]"));
        }

        // try int
        //   compare = TypeLattice.compare(BaseType.INT, token);
        //         if (compare == CPO.SAME || compare == CPO.HIGHER) {
        //             IntToken tem = IntToken.convert(token);
        //             int[][] result = new int[1][1];
        //             result[0][0] = tem.intValue();
        //             return new IntMatrixToken(result);
        //         }

        // try ByteMatrix?
        //       compare = TypeLattice.compare(BaseType.INT_MATRIX, token);
        //         if (compare == CPO.SAME || compare == CPO.HIGHER) {
        //             IntMatrixToken tem = convert(token);
        //             int[][] result = tem.intMatrix();
        //             return new IntMatrixToken(result);
        //         }

        // The argument is below IntMatrixToken in the type hierarchy,
        // but I don't recognize it.
        throw new IllegalActionException(
                notSupportedConversionMessage(token, "[int]"));
    }

    /** Convert the specified scalar token into an instance of
     *  IntMatrixToken.  The resulting matrix will be square, with
     *  the number of rows and columns equal to the given size.
     *  This method does lossless conversion.
     *  @param token The token to be converted to a IntMatrixToken.
     *  @return A IntMatrixToken
     *  @exception IllegalActionException If the conversion cannot
     *  be carried out.
     */
    public static Token convert(ScalarToken token, int size)
            throws IllegalActionException {

        // Check to make sure that the token is convertible to INT.
        int compare = TypeLattice.compare(BaseType.INT, token);
        if (compare == CPO.SAME || compare == CPO.HIGHER) {
            IntToken tem = IntToken.convert(token);
            int[][] result = new int[size][size];
            for (int i = 0; i < size; i++)
                for (int j = 0; j < size; j++)
                    result[i][j] = tem.intValue();
            return new IntMatrixToken(result);
        }

        throw new IllegalActionException(
                notSupportedConversionMessage(token, "[int]"));
    }

    /** Return the content of this token as a 2-D double matrix.
     *  @return A 2-D double matrix.
     */
    public double[][] doubleMatrix() {
        return IntegerMatrixMath.toDoubleMatrix(_value);
    }

    /** Return true if the argument is an instance of IntMatrixToken
     *  of the same dimensions and the corresponding elements of the matrices
     *  are equal.
     *  @param object An instance of Object.
     *  @return True if the argument is an instance of IntMatrixToken
     *   of the same dimensions and the corresponding elements of the
     *   matrices are equal.
     */
    public boolean equals(Object object) {
        // This test rules out instances of a subclass.
        if (object.getClass() != getClass()) {
            return false;
        }

        IntMatrixToken matrixArgument = (IntMatrixToken)object;
        if (_rowCount != matrixArgument.getRowCount()) {
            return false;
        }
        if (_columnCount != matrixArgument.getColumnCount()) {
            return false;
        }

        int[][] matrix = matrixArgument.intMatrix();
        for (int i = 0; i < _rowCount; i++) {
            for (int j = 0; j < _columnCount; j++) {
                if (_value[i][j] != matrix[i][j]) {
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
     *  row and column in a IntToken.
     *  @param row The row index of the desired element.
     *  @param column The column index of the desired element.
     *  @return A IntToken containing the matrix element.
     *  @exception ArrayIndexOutOfBoundsException If the specified
     *   row or column number is outside the range of the matrix.
     */
    public Token getElementAsToken(int row, int column)
            throws ArrayIndexOutOfBoundsException {
        return new IntToken(_value[row][column]);
    }

    /** Return the element of the contained matrix at the specified
     *  row and column.
     *  @param row The row index of the desired element.
     *  @param column The column index of the desired element.
     *  @return The int at the specified matrix entry.
     *  @exception ArrayIndexOutOfBoundsException If the specified
     *   row or column number is outside the range of the matrix.
     */
    public int getElementAt(int row, int column) {
        return _value[row][column];
    }

    /** Return the Type of the tokens contained in this matrix token.
     *  This must be a type representing a scalar token.
     *  @return BaseType.INT.
     */
    public Type getElementType() {
        return BaseType.INT;
    }

    /** Return the number of rows in the matrix.
     *  @return The number of rows in the matrix.
     */
    public int getRowCount() {
        return _rowCount;
    }

    /** Return the type of this token.
     *  @return BaseType.INT_MATRIX
     */
    public Type getType() {
        return BaseType.INT_MATRIX;
    }

    /** Return a hash code value for this token. This method returns the
     *  sum of the elements.
     *  @return A hash code value for this token.
     */
    public int hashCode() {
        int code = 0;
        for (int i = 0; i < _rowCount; i++) {
            for (int j = 0; j < _columnCount; j++) {
                code += _value[i][j];
            }
        }

        return code;
    }

    /** Return the content in the token as a 2-D int matrix.
     *  The returned matrix is a copy so the caller is free to
     *  modify it.
     *  @return A 2-D int matrix.
     */
    public int[][] intMatrix() {
        return IntegerMatrixMath.allocCopy(_value);
    }

    /** Return the content of this token as a 2-D long matrix.
     *  @return A 2-D long matrix.
     */
    public long[][] longMatrix() {
        return IntegerMatrixMath.toLongMatrix(_value);
    }

    /** Return a new Token representing the left multiplicative
     *  identity. The returned token contains an identity matrix
     *  whose dimensions are the same as the number of rows of
     *  the matrix contained in this token.
     *  @return A new IntMatrixToken containing the left multiplicative
     *   identity.
     */
    public Token one() {
        try {
            return new IntMatrixToken(IntegerMatrixMath.identity(_rowCount),
                    DO_NOT_COPY);
        } catch (IllegalActionException illegalAction) {
            // should not happen
            throw new InternalErrorException("IntMatrixToken.one: "
                    + "Cannot create identity matrix.");
        }
    }

    /** Return a new Token representing the right multiplicative
     *  identity. The returned token contains an identity matrix
     *  whose dimensions are the same as the number of columns of
     *  the matrix contained in this token.
     *  @return A new IntMatrixToken containing the right multiplicative
     *   identity.
     */
    public Token oneRight() {
        try {
            return new IntMatrixToken(IntegerMatrixMath.identity(_columnCount),
                    DO_NOT_COPY);
        } catch (IllegalActionException illegalAction) {
            // should not happen
            throw new InternalErrorException("IntMatrixToken.oneRight: "
                    + "Cannot create identity matrix.");
        }
    }

    /** Return a new Token representing the additive identity.
     *  The returned token contains a matrix whose elements are
     *  all zero, and the size of the matrix is the same as the
     *  matrix contained in this token.
     *  @return A new IntMatrixToken containing the additive identity.
     */
    public Token zero() {
        try {
            return new IntMatrixToken(new int[_rowCount][_columnCount],
                    DO_NOT_COPY);
        } catch (IllegalActionException illegalAction) {
            // should not happen
            throw new InternalErrorException("IntMatrixToken.zero: "
                    + "Cannot create zero matrix.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a new token whose value is the value of the argument
     *  Token added to the value of this Token.  It is assumed that
     *  the type of the argument is IntMatrixToken.
     *  @param rightArgument The token to add to this token.
     *  @exception IllegalActionException If the units are not
     *  compatible, or this operation is not supported by the derived
     *  class.
     *  @return A new IntMatrixToken containing the result.
     */
    protected MatrixToken _add(MatrixToken rightArgument)
            throws IllegalActionException {
        IntMatrixToken convertedArgument = (IntMatrixToken)rightArgument;
        int[][] result = IntegerMatrixMath.add(
                convertedArgument._getInternalIntMatrix(), _value);
        return new IntMatrixToken(result);
    }

    /** Return a new token whose value is the value of the argument
     *  Token added to the value of each element of this Token. It is
     *  assumed that the type of the argument is the same as the type
     *  of each element of this class.
     *  @param rightArgument The token to add to this token.
     *  @exception IllegalActionException If this operation is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
     */
    protected MatrixToken _addElement(Token rightArgument)
            throws IllegalActionException {
        int scalar = ((IntToken)rightArgument).intValue();
        int[][] result = IntegerMatrixMath.add(_value, scalar);
        return new IntMatrixToken(result);
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
        int scalar = ((IntToken)rightArgument).intValue();
        int[][] result = IntegerMatrixMath.divide(_value, scalar);
        return new IntMatrixToken(result);
    }


    /** Return a reference to the internal 2-D matrix of ints that represents
     *  this Token. Because no copying is done, the contents must NOT be
     *  modified to preserve the immutability of Token.
     *  @return A 2-D int matrix.
     */
    protected int[][] _getInternalIntMatrix() {
        return _value;
    }

    /** Return a new token whose elements are the remainders of
     *  the elements of this token when divided by the argument.
     *  It is guaranteed by the caller that the type of the argument
     *  is the same as the type of each element of this class.
     *  @param rightArgument The token that performs modulo on this token.
     *  @exception IllegalActionException If this operation is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
     */

    protected MatrixToken _moduloElement(Token rightArgument)
            throws IllegalActionException {
        int scalar = ((IntToken)rightArgument).intValue();
        int[][] result = IntegerMatrixMath.modulo(_value, scalar);
        return new IntMatrixToken(result);
    }

    /** Return a new token whose value is the value of this token
     *  multiplied by the value of the argument token.  It is assumed
     *  that the type of the argument is IntMatrixToken.
     *  @param rightArgument The token to multiply this token by.
     *  @exception IllegalActionException If the units are not
     *  compatible, or this operation is not supported by the derived
     *  class.
     *  @return A new IntMatrixToken containing the result.
     */
    protected MatrixToken _multiply(MatrixToken rightArgument)
            throws IllegalActionException {
        IntMatrixToken convertedArgument = (IntMatrixToken)rightArgument;
        int[][] result = IntegerMatrixMath.multiply(
                _value, convertedArgument._getInternalIntMatrix());
        return new IntMatrixToken(result);
    }

    /** Return a new token whose value is the value of this token
     *  multiplied by the value of the argument scalar token.
     *  This method should be overridden in derived
     *  classes to provide type specific actions for multiply.
     *  @param rightArgument The token to multiply this token by.
     *  @exception IllegalActionException If this method is not
     *   supported by the derived class.
     *  @return A new IntMatrixToken containing the result.
     */
    protected MatrixToken _multiplyElement(Token rightArgument)
            throws IllegalActionException {
        int scalar = ((IntToken)rightArgument).intValue();
        int[][] result = IntegerMatrixMath.multiply(_value, scalar);
        return new IntMatrixToken(result);
    }

    /** Return a new token whose value is the value of the argument token
     *  subtracted from the value of this token.  It is assumed that the
     *  type of the argument is IntMatrixToken.
     *  @param rightArgument The token to subtract from this token.
     *  @exception IllegalActionException If the units are not
     *  compatible, or this operation is not supported by the derived
     *  class.
     *  @return A new IntMatrixToken containing the result.
     */
    protected MatrixToken _subtract(MatrixToken rightArgument)
            throws IllegalActionException {
        IntMatrixToken convertedArgument = (IntMatrixToken)rightArgument;
        int[][] result = IntegerMatrixMath.subtract(_value,
                convertedArgument._getInternalIntMatrix());
        return new IntMatrixToken(result);
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
        int scalar = ((IntToken)rightArgument).intValue();
        int[][] result = IntegerMatrixMath.add(_value, -scalar);
        return new IntMatrixToken(result);
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
        int scalar = ((IntToken)rightArgument).intValue();
        int[][] result = IntegerMatrixMath.negative(
                IntegerMatrixMath.add(_value, -scalar));
        return new IntMatrixToken(result);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Initialize the row and column count and copy the specified
    // matrix.  This method is used by the constructors.
    private void _initialize(int[][] value, int copy) {
        _rowCount = value.length;
        _columnCount = value[0].length;

        if (copy == DO_NOT_COPY) {
            _value = value;
        } else {
            _value = IntegerMatrixMath.allocCopy(value);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private int[][] _value;
    private int _rowCount;
    private int _columnCount;
}
