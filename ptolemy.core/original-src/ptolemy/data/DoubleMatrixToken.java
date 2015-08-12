/* A token that contains a 2-D double matrix.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
import ptolemy.math.DoubleArrayMath;
import ptolemy.math.DoubleMatrixMath;

///////////////////////////////////////////////////////////////////
//// DoubleMatrixToken

/**
 A token that contains a 2-D double matrix.

 @author Yuhong Xiong, Jeff Tsay, Christopher Hylands, Steve Neuendorffer,
 Shuvra S. Bhattacharyya
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Green (neuendor)
 @Pt.AcceptedRating Yellow (cxh)
 */
public class DoubleMatrixToken extends MatrixToken {
    /** Construct an DoubleMatrixToken with a one by one matrix. The
     *  only element in the matrix has value 0.0
     */
    public DoubleMatrixToken() {
        _value = new double[1];
        _value[0] = 0;
        _rowCount = 1;
        _columnCount = 1;
    }

    /** Construct a DoubleMatrixToken with the specified 1-D matrix.
     *  Make a copy of the matrix and store the copy,
     *  so that changes on the specified matrix after this token is
     *  constructed will not affect the content of this token.
     *  @param value The 1-D matrix.
     *  @param rows The number of rows.
     *  @param columns The number of columns.
     *  @exception IllegalActionException If the specified matrix
     *   is null.
     */
    public DoubleMatrixToken(double[] value, int rows, int columns)
            throws IllegalActionException {
        this(value, rows, columns, DO_COPY);
    }

    /** Construct a DoubleMatrixToken with the specified 1-D matrix.
     *  If copy is DO_COPY, make a copy of the matrix and store the copy,
     *  so that changes on the specified matrix after this token is
     *  constructed will not affect the content of this token.
     *  If copy is DO_NOT_COPY, just reference the matrix (do not copy
     *  its contents). This saves some time and memory.
     *  The argument matrix should NOT be modified after this constructor
     *  is called to preserve immutability.
     *  @param value The 1-D matrix.
     *  @param rows The number of rows.
     *  @param columns The number of columns.
     *  @param copy If this parameter is
     *  {@link ptolemy.data.MatrixToken#DO_COPY}, then the value matrix
     *  is copied.  If this parameter is
     *  {@link ptolemy.data.MatrixToken#DO_NOT_COPY}, then the value matrix
     *  is NOT copied and should not be modified after construction of this
     *  object.
     *  @exception IllegalActionException If the specified matrix
     *   is null.
     */
    public DoubleMatrixToken(double[] value, int rows, int columns, int copy)
            throws IllegalActionException {
        if (value == null) {
            throw new IllegalActionException(
                    "DoubleMatrixToken: The specified " + "matrix is null.");
        }

        _rowCount = rows;
        _columnCount = columns;

        if (copy == DO_COPY) {
            _value = DoubleArrayMath.allocCopy(value);
        } else {
            _value = value;
        }
    }

    /** Construct a DoubleMatrixToken with the specified 2-D matrix.
     *  Make a copy of the matrix and store the copy,
     *  so that changes on the specified matrix after this token is
     *  constructed will not affect the content of this token.
     *  @param value The 2-D matrix used to initialize this object.
     *  @exception IllegalActionException If the specified matrix
     *   is null.
     */
    public DoubleMatrixToken(double[][] value) throws IllegalActionException {
        this(value, DO_COPY);
    }

    /** Construct a DoubleMatrixToken with the specified 2-D matrix.
     *  If copy is DO_COPY, make a copy of the matrix and store the copy,
     *  so that changes on the specified matrix after this token is
     *  constructed will not affect the content of this token.
     *  If copy is DO_NOT_COPY, just reference the matrix (do not copy
     *  its contents). This saves some time and memory.
     *  The argument matrix should NOT be modified after this constructor
     *  is called to preserve immutability, although this is not enforced.
     *  @param value The 2-D matrix used to initialize this object.
     *  @param copy If this parameter is
     *  {@link ptolemy.data.MatrixToken#DO_COPY}, then the value matrix
     *  is copied.  If this parameter is
     *  {@link ptolemy.data.MatrixToken#DO_NOT_COPY}, then the value matrix
     *  is NOT copied and should not be modified after construction of this
     *  object.
     *  @exception IllegalActionException If the specified matrix
     *   is null.
     */
    public DoubleMatrixToken(double[][] value, int copy)
            throws IllegalActionException {
        if (value == null) {
            throw new IllegalActionException("DoubleMatrixToken: The "
                    + "specified matrix is null.");
        }

        _initialize(value);
    }

    /** Construct a DoubleMatrixToken from the specified string.
     *  @param init A string expression of a 2-D double matrix.
     *  @exception IllegalActionException If the string does
     *   not contain a parsable 2-D double matrix.
     */
    public DoubleMatrixToken(String init) throws IllegalActionException {
        PtParser parser = new PtParser();
        ASTPtRootNode tree = parser.generateParseTree(init);
        Token token = new ParseTreeEvaluator().evaluateParseTree(tree);

        if (token instanceof DoubleMatrixToken) {
            double[][] value = ((DoubleMatrixToken) token).doubleMatrix();
            _initialize(value);
        } else {
            throw new IllegalActionException("A matrix token cannot be"
                    + " created from the expression '" + init + "'");
        }
    }

    /** Construct an DoubleMatrixToken from the specified array of
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
    public DoubleMatrixToken(Token[] tokens, int rows, int columns)
            throws IllegalActionException {
        int elements = rows * columns;

        if (tokens == null) {
            throw new IllegalActionException("DoubleMatrixToken: The specified"
                    + " array is null.");
        }

        if (tokens.length != rows * columns) {
            throw new IllegalActionException("DoubleMatrixToken: The specified"
                    + " array is not of the correct length");
        }

        _rowCount = rows;
        _columnCount = columns;
        _value = new double[elements];

        for (int i = 0; i < elements; i++) {
            Token token = tokens[i];

            if (token instanceof ScalarToken) {
                _value[i] = ((ScalarToken) token).doubleValue();
            } else {
                throw new IllegalActionException("DoubleMatrixToken: Element "
                        + i + " in the array with value " + token
                        + " is not a ScalarToken");
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the content of this token as a 2-D Complex matrix.
     *  @return A 2-D Complex matrix
     */
    @Override
    public final Complex[][] complexMatrix() {
        return ComplexMatrixMath
                .toMatrixFromArray(DoubleArrayMath.toComplexArray(_value),
                        _rowCount, _columnCount);
    }

    /** Convert the specified token into an instance of DoubleMatrixToken.
     *  This method does lossless conversion.
     *  If the argument is already an instance of DoubleMatrixToken,
     *  it is returned without any change. Otherwise, if the argument
     *  is below DoubleMatrixToken in the type hierarchy, it is converted to
     *  an instance of DoubleMatrixToken or one of the subclasses of
     *  DoubleMatrixToken and returned. If none of the above condition is
     *  met, an exception is thrown.
     *  @param token The token to be converted to a DoubleMatrixToken.
     *  @return A DoubleMatrixToken
     *  @exception IllegalActionException If the conversion cannot
     *   be carried out.
     */
    public static DoubleMatrixToken convert(Token token)
            throws IllegalActionException {
        if (token instanceof DoubleMatrixToken) {
            return (DoubleMatrixToken) token;
        }

        int compare = TypeLattice.compare(BaseType.DOUBLE_MATRIX, token);

        if (compare == CPO.LOWER || compare == CPO.INCOMPARABLE) {
            throw new IllegalActionException(
                    notSupportedIncomparableConversionMessage(token, "[double]"));
        }

        // try double
        //  compare = TypeLattice.compare(BaseType.DOUBLE, token);
        //         if (compare == CPO.SAME || compare == CPO.HIGHER) {
        //             DoubleToken tem = DoubleToken.convert(token);
        //             double[][] result = new double[1][1];
        //             result[0][0] = tem.doubleValue();
        //             return new DoubleMatrixToken(result);
        //         }
        // try IntMatrix
        compare = TypeLattice.compare(BaseType.INT_MATRIX, token);

        if (compare == CPO.SAME || compare == CPO.HIGHER) {
            IntMatrixToken intMatrix = IntMatrixToken.convert(token);
            double[][] result = intMatrix.doubleMatrix();
            return new DoubleMatrixToken(result);
        }

        // The argument is below DoubleMatrixToken in the type hierarchy,
        // but I don't recognize it.
        throw new IllegalActionException(notSupportedConversionMessage(token,
                "[double]"));
    }

    /** Return a new matrix that is a sub-matrix of this matrix.
     *  @param rowStart The row to start on.
     *  @param colStart The column to start on.
     *  @param rowSpan The number of rows to copy.
     *  @param colSpan The number of columns to copy.
     *  @return a sub-matrix of this matrix.
     *  @exception IllegalActionException If the returned matrix is empty or if the specified
     *   parameters result in out of bounds accesses.
     */
    @Override
    public MatrixToken crop(int rowStart, int colStart, int rowSpan, int colSpan)
            throws IllegalActionException {
        double[][] value = this.doubleMatrix();
        try {
            double[][] result = DoubleMatrixMath.crop(value, rowStart,
                    colStart, rowSpan, colSpan);
            return new DoubleMatrixToken(result);
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new IllegalActionException(
                    "Matrix crop indices out of bounds (rowStart = " + rowStart
                    + ", colStart = " + colStart + ", rowSpan = "
                    + rowSpan + ", colSpan = " + colSpan + ").");
        }
    }

    /** Return the content in the token as a 2-D double matrix.
     *  The returned matrix is a copy so the caller is free to
     *  modify it.
     *  @return A 2-D double matrix.
     */
    @Override
    public final double[][] doubleMatrix() {
        return DoubleMatrixMath.toMatrixFromArray(_value, _rowCount,
                _columnCount);
    }

    /** Return true if the argument is an instance of DoubleMatrixToken
     *  of the same dimensions and the corresponding elements of the matrices
     *  are equal.
     *  @param object An instance of Object.
     *  @return True if the argument is an instance of DoubleMatrixToken
     *   of the same dimensions and the corresponding elements of the
     *   matrices are equal.
     */
    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        // This test rules out instances of a subclass.
        if (object.getClass() != getClass()) {
            return false;
        }

        DoubleMatrixToken matrixArgument = (DoubleMatrixToken) object;

        if (_rowCount != matrixArgument.getRowCount()) {
            return false;
        }

        if (_columnCount != matrixArgument.getColumnCount()) {
            return false;
        }

        double[] value = matrixArgument._value;
        int elements = _rowCount * _columnCount;

        for (int i = 0; i < elements; i++) {
            if (_value[i] != value[i]) {
                return false;
            }
        }

        return true;
    }

    /** Return the number of columns in the matrix.
     *  @return The number of columns in the matrix.
     */
    @Override
    public final int getColumnCount() {
        return _columnCount;
    }

    /** Return the element of the matrix at the specified
     *  row and column in a DoubleToken.
     *  @param row The row index of the desired element.
     *  @param column The column index of the desired element.
     *  @return A DoubleToken containing the matrix element.
     *  @exception ArrayIndexOutOfBoundsException If the specified
     *   row or column number is outside the range of the matrix.
     */
    @Override
    public final Token getElementAsToken(final int row, final int column)
            throws ArrayIndexOutOfBoundsException {
        return new DoubleToken(_value[row * _columnCount + column]);
    }

    /** Return the element of the contained matrix at the specified
     *  row and column.
     *  @param row The row index of the desired element.
     *  @param column The column index of the desired element.
     *  @return The double at the specified matrix entry.
     *  @exception ArrayIndexOutOfBoundsException If the specified
     *   row or column number is outside the range of the matrix.
     */
    public final double getElementAt(final int row, final int column) {
        return _value[row * _columnCount + column];
    }

    /** Return the Type of the tokens contained in this matrix token.
     *  This must be a type representing a scalar token.
     *  @return BaseType.DOUBLE.
     */
    @Override
    public Type getElementType() {
        return BaseType.DOUBLE;
    }

    /** Return the number of rows in the matrix.
     *  @return The number of rows in the matrix.
     */
    @Override
    public final int getRowCount() {
        return _rowCount;
    }

    /** Return the type of this token.
     *  @return BaseType.DOUBLE_MATRIX
     */
    @Override
    public final Type getType() {
        return BaseType.DOUBLE_MATRIX;
    }

    /** Return a hash code value for this token. This method returns the
     *  integer portion of the sum of the elements.
     *  @return A hash code value for this token.
     */
    @Override
    public int hashCode() {
        double code = 0.0;
        int elements = _rowCount * _columnCount;

        for (int i = 0; i < elements; i++) {
            code += _value[i];
        }

        return (int) code;
    }

    /** Join a matrix of matrices into a single matrix by tiling.
     *  All matrices in the matrix must be of the same type,
     *  the same type as this matrix. But none of them needs to
     *  actually be this matrix. This base class simply throws
     *  an exception. Derived classes provide the implementation.
     *  The number of columns in the resulting matrix is the sum
     *  of the number of columns in the first row of the argument.
     *  The number of rows in the resulting matrix is the sum
     *  of the number of rows in the first column of the argument.
     *  The matrices are copied into the result starting at the
     *  position determined by the first row or column.
     *  If the matrices overlap, then while copying left to right,
     *  top-to-bottom, data will be overwritten. If there are gaps,
     *  the resulting matrix will be filled with zeros.
     *  @param matrices A two-dimensional array of matrix tokens.
     *  @return A new matrix token of the same type as the elements
     *   in the input matrix of matrix tokens.
     *  @exception IllegalActionException If the types of the matrices
     *   in the input are not all the same, or if tiling fails due
     *   to size incompatibilities, or if the input matrix has no
     *   tokens.
     */
    @Override
    public MatrixToken join(MatrixToken[][] matrices)
            throws IllegalActionException {
        if (matrices == null || matrices.length == 0 || matrices[0].length == 0) {
            throw new IllegalActionException("matrixJoin: No input matrices.");
        }
        // Calculate the size of the result.
        // This assumes the matrices tile.
        int rows = 0;
        int columns = 0;
        for (MatrixToken[] matrice : matrices) {
            rows += matrice[0].getRowCount();
        }
        for (int j = 0; j < matrices[0].length; j++) {
            columns += matrices[0][j].getColumnCount();
        }
        double[][] tiled = new double[rows][columns];
        int row = 0;
        for (int i = 0; i < matrices.length; i++) {
            int column = 0;
            for (int j = 0; j < matrices[i].length; j++) {
                if (!(matrices[i][j] instanceof DoubleMatrixToken)) {
                    throw new IllegalActionException(
                            "matrixJoin: matrices not all of the same type.");
                }
                int rowCount = matrices[i][j].getRowCount();
                if (row + rowCount > rows) {
                    rowCount = rows - row;
                }
                int columnCount = matrices[i][j].getColumnCount();
                if (column + columnCount > columns) {
                    columnCount = columns - column;
                }
                DoubleMatrixMath.matrixCopy(matrices[i][j].doubleMatrix(), 0,
                        0, tiled, row, column, rowCount, columnCount);
                // Starting position for the next column.
                column += matrices[0][j].getColumnCount();
            }
            // Starting position for the next column.
            row += matrices[i][0].getRowCount();
        }
        return new DoubleMatrixToken(tiled);
    }

    /** Return a new Token representing the left multiplicative
     *  identity. The returned token contains an identity matrix
     *  whose dimensions are the same as the number of rows of
     *  the matrix contained in this token.
     *  @return A new DoubleMatrixToken containing the left multiplicative
     *   identity.
     */
    @Override
    public final Token one() {
        try {
            return new DoubleMatrixToken(DoubleMatrixMath.identity(_rowCount),
                    DO_NOT_COPY);
        } catch (IllegalActionException illegalAction) {
            // should not happen
            throw new InternalErrorException("DoubleMatrixToken.one: "
                    + "Cannot create identity matrix.");
        }
    }

    /** Return a new Token representing the right multiplicative
     *  identity. The returned token contains an identity matrix
     *  whose dimensions are the same as the number of columns of
     *  the matrix contained in this token.
     *  @return A new DoubleMatrixToken containing the right multiplicative
     *   identity.
     */
    @Override
    public final Token oneRight() {
        try {
            return new DoubleMatrixToken(
                    DoubleMatrixMath.identity(_columnCount), DO_NOT_COPY);
        } catch (IllegalActionException illegalAction) {
            // should not happen
            throw new InternalErrorException("DoubleMatrixToken.oneRight: "
                    + "Cannot create identity matrix.");
        }
    }

    /** Split this matrix into multiple matrices. See the base
     *  class for documentation.
     *  @param rows The number of rows per submatrix.
     *  @param columns The number of columns per submatrix.
     *  @return An array of matrix tokens.
     */
    @Override
    public MatrixToken[][] split(int[] rows, int[] columns) {
        MatrixToken[][] result = new MatrixToken[rows.length][columns.length];
        double[][] source = doubleMatrix();
        int row = 0;
        for (int i = 0; i < rows.length; i++) {
            int column = 0;
            for (int j = 0; j < columns.length; j++) {
                double[][] contents = new double[rows[i]][columns[j]];
                int rowspan = rows[i];
                if (row + rowspan > source.length) {
                    rowspan = source.length - row;
                }
                int columnspan = columns[j];
                if (column + columnspan > source[0].length) {
                    columnspan = source[0].length - column;
                }
                if (columnspan > 0 && rowspan > 0) {
                    DoubleMatrixMath.matrixCopy(source, row, column, contents,
                            0, 0, rowspan, columnspan);
                }
                column += columns[j];
                try {
                    result[i][j] = new DoubleMatrixToken(contents);
                } catch (IllegalActionException e) {
                    throw new InternalErrorException(e);
                }
            }
            row += rows[i];
        }
        return result;
    }

    /** Return a new Token representing the additive identity.
     *  The returned token contains a matrix whose elements are
     *  all zero, and the size of the matrix is the same as the
     *  matrix contained in this token.
     *  @return A new DoubleMatrixToken containing the additive identity.
     */
    @Override
    public final Token zero() {
        try {
            return new DoubleMatrixToken(new double[_rowCount * _columnCount],
                    _rowCount, _columnCount, DO_NOT_COPY);
        } catch (IllegalActionException illegalAction) {
            // should not happen
            throw new InternalErrorException("DoubleMatrixToken.zero: "
                    + "Cannot create zero matrix.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a new token whose value is the value of the argument
     *  Token added to the value of this Token.  It is assumed that
     *  the type of the argument is DoubleMatrixToken.
     *  @param rightArgument The token to add to this token.
     *  @exception IllegalActionException If the units are not
     *  compatible, or this operation is not supported by the derived
     *  class.
     *  @return A new DoubleMatrixToken containing the result.
     */
    @Override
    protected MatrixToken _add(MatrixToken rightArgument)
            throws IllegalActionException {
        DoubleMatrixToken convertedArgument = (DoubleMatrixToken) rightArgument;
        double[] result = DoubleArrayMath.add(
                convertedArgument._getInternalDoubleArray(), _value);
        return new DoubleMatrixToken(result, _rowCount, _columnCount,
                DO_NOT_COPY);
    }

    /** Return a new token whose value is the value of the argument
     *  Token added to the value of each element of this Token. It is
     *  assumed that the type of the argument is the same as the type
     *  of each element of this class or is a matrix with just one
     *  element.
     *  @param rightArgument The token to add to this token.
     *  @exception IllegalActionException If this operation is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
     */
    @Override
    protected MatrixToken _addElement(Token rightArgument)
            throws IllegalActionException {
        double scalar;
        if (rightArgument instanceof DoubleMatrixToken) {
            if (((DoubleMatrixToken) rightArgument).getRowCount() != 1
                    || ((DoubleMatrixToken) rightArgument).getColumnCount() != 1) {
                // Throw an exception.
                return super._moduloElement(rightArgument);
            }
            scalar = ((DoubleMatrixToken) rightArgument).getElementAt(0, 0);
        } else {
            scalar = ((DoubleToken) rightArgument).doubleValue();
        }
        double[] result = DoubleArrayMath.add(_value, scalar);
        return new DoubleMatrixToken(result, _rowCount, _columnCount,
                DO_NOT_COPY);
    }

    /** Return a new token whose elements are the result of dividing
     *  the elements of this token by the argument. It is
     *  assumed that the type of the argument is the same as the type
     *  of each element of this class or is a matrix with just one
     *  element.
     *  @param rightArgument The token that divides this token.
     *  @exception IllegalActionException If this operation is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
     */
    @Override
    protected MatrixToken _divideElement(Token rightArgument)
            throws IllegalActionException {
        double scalar;
        if (rightArgument instanceof DoubleMatrixToken) {
            if (((DoubleMatrixToken) rightArgument).getRowCount() != 1
                    || ((DoubleMatrixToken) rightArgument).getColumnCount() != 1) {
                // Throw an exception.
                return super._moduloElement(rightArgument);
            }
            scalar = ((DoubleMatrixToken) rightArgument).getElementAt(0, 0);
        } else {
            scalar = ((DoubleToken) rightArgument).doubleValue();
        }
        double[] result = DoubleArrayMath.divide(_value, scalar);
        return new DoubleMatrixToken(result, _rowCount, _columnCount,
                DO_NOT_COPY);
    }

    /** Return a reference to the internal 2-D matrix of doubles that
     *  represents this Token. Because no copying is done, the contents
     *  must NOT be modified to preserve the immutability of Token.
     *  @return A 2-D double matrix.
     */
    protected double[] _getInternalDoubleArray() {
        return _value;
    }

    /** Return a new token whose elements are the remainders of
     *  the elements of this token when divided by the argument.
     *  It is guaranteed by the caller that the type of the argument
     *  is the same as the type of each element of this class or
     *  a scalar of the same type as the element.
     *  @param rightArgument The token that performs modulo on this token.
     *  @exception IllegalActionException If this operation is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
     */
    @Override
    protected MatrixToken _moduloElement(Token rightArgument)
            throws IllegalActionException {
        double scalar;
        if (rightArgument instanceof DoubleMatrixToken) {
            if (((DoubleMatrixToken) rightArgument).getRowCount() != 1
                    || ((DoubleMatrixToken) rightArgument).getColumnCount() != 1) {
                // Throw an exception.
                return super._moduloElement(rightArgument);
            }
            scalar = ((DoubleMatrixToken) rightArgument).getElementAt(0, 0);
        } else {
            scalar = ((DoubleToken) rightArgument).doubleValue();
        }
        double[] result = DoubleArrayMath.modulo(_value, scalar);
        return new DoubleMatrixToken(result, _rowCount, _columnCount,
                DO_NOT_COPY);
    }

    /** Return a new token whose value is the value of this token
     *  multiplied by the value of the argument token.  It is assumed
     *  that the type of the argument is DoubleMatrixToken.
     *  @param rightArgument The token to multiply this token by.
     *  @exception IllegalActionException If the units are not
     *  compatible, or this operation is not supported by the derived
     *  class.
     *  @return A new DoubleMatrixToken containing the result.
     */
    @Override
    protected MatrixToken _multiply(MatrixToken rightArgument)
            throws IllegalActionException {
        DoubleMatrixToken convertedArgument = (DoubleMatrixToken) rightArgument;
        double[] A = _value;
        double[] B = convertedArgument._getInternalDoubleArray();
        int m = _rowCount;
        int n = _columnCount;
        int p = convertedArgument.getColumnCount();
        double[] newMatrix = new double[m * p];
        int in = 0;
        int ta = 0;

        for (int i = 0; i < m; i++) {
            ta += n;

            for (int j = 0; j < p; j++) {
                double sum = 0;
                int ib = j;

                for (int ia = i * n; ia < ta; ia++, ib += p) {
                    sum += A[ia] * B[ib];
                }

                newMatrix[in++] = sum;
            }
        }

        return new DoubleMatrixToken(newMatrix, m, p, DO_NOT_COPY);
    }

    /** Return a new token whose value is the value of this token
     *  multiplied by the value of the argument token.
     *  It is assumed that the argument has the same type as
     *  the elements of this matrix or is a matrix with just
     *  one element.
     *  @param rightArgument The token to multiply this token by.
     *  @exception IllegalActionException If this method is not
     *   supported by the derived class.
     *  @return A new DoubleMatrixToken containing the result.
     */
    @Override
    protected MatrixToken _multiplyElement(Token rightArgument)
            throws IllegalActionException {
        double scalar;
        if (rightArgument instanceof DoubleMatrixToken) {
            if (((DoubleMatrixToken) rightArgument).getRowCount() != 1
                    || ((DoubleMatrixToken) rightArgument).getColumnCount() != 1) {
                // Throw an exception.
                return super._moduloElement(rightArgument);
            }
            scalar = ((DoubleMatrixToken) rightArgument).getElementAt(0, 0);
        } else {
            scalar = ((DoubleToken) rightArgument).doubleValue();
        }
        double[] result = DoubleArrayMath.multiply(_value, scalar);
        return new DoubleMatrixToken(result, _rowCount, _columnCount,
                DO_NOT_COPY);
    }

    /** Return a new token whose value is the value of the argument token
     *  subtracted from the value of this token.  It is assumed that the
     *  type of the argument is DoubleMatrixToken.
     *  @param rightArgument The token to subtract from this token.
     *  @exception IllegalActionException If the units are not
     *  compatible, or this operation is not supported by the derived
     *  class.
     *  @return A new DoubleMatrixToken containing the result.
     */
    @Override
    protected MatrixToken _subtract(MatrixToken rightArgument)
            throws IllegalActionException {
        DoubleMatrixToken convertedArgument = (DoubleMatrixToken) rightArgument;
        double[] result = DoubleArrayMath.subtract(_value,
                convertedArgument._getInternalDoubleArray());
        return new DoubleMatrixToken(result, _rowCount, _columnCount,
                DO_NOT_COPY);
    }

    /** Return a new token whose value is the value of the argument
     *  Token subtracted from the value of each element of this Token. It is
     *  assumed that the type of the argument is the same as the type
     *  of each element of this class or is a matrix with just one
     *  element.
     *  @param rightArgument The token to subtract from this token.
     *  @exception IllegalActionException If this operation is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
     */
    @Override
    protected MatrixToken _subtractElement(Token rightArgument)
            throws IllegalActionException {
        double scalar;
        if (rightArgument instanceof DoubleMatrixToken) {
            if (((DoubleMatrixToken) rightArgument).getRowCount() != 1
                    || ((DoubleMatrixToken) rightArgument).getColumnCount() != 1) {
                // Throw an exception.
                return super._moduloElement(rightArgument);
            }
            scalar = ((DoubleMatrixToken) rightArgument).getElementAt(0, 0);
        } else {
            scalar = ((DoubleToken) rightArgument).doubleValue();
        }
        double[] result = DoubleArrayMath.add(_value, -scalar);
        return new DoubleMatrixToken(result, _rowCount, _columnCount,
                DO_NOT_COPY);
    }

    /** Return a new token whose value is the value of the argument
     *  Token subtracted from the value of each element of this Token. It is
     *  assumed that the type of the argument is the same as the type
     *  of each element of this class or is a matrix with just one
     *  element.
     *  @param rightArgument The token to subtract from this token.
     *  @exception IllegalActionException If this operation is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
     */
    @Override
    protected MatrixToken _subtractElementReverse(Token rightArgument)
            throws IllegalActionException {
        double scalar;
        if (rightArgument instanceof DoubleMatrixToken) {
            if (((DoubleMatrixToken) rightArgument).getRowCount() != 1
                    || ((DoubleMatrixToken) rightArgument).getColumnCount() != 1) {
                // Throw an exception.
                return super._moduloElement(rightArgument);
            }
            scalar = ((DoubleMatrixToken) rightArgument).getElementAt(0, 0);
        } else {
            scalar = ((DoubleToken) rightArgument).doubleValue();
        }
        double[] result = DoubleArrayMath.negative(DoubleArrayMath.add(_value,
                -scalar));
        return new DoubleMatrixToken(result, _rowCount, _columnCount,
                DO_NOT_COPY);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // Initialize the row and column count and copy the specified
    // matrix. This method is used by the constructors.
    private void _initialize(double[][] value) {
        _rowCount = value.length;
        _columnCount = value[0].length;
        _value = DoubleMatrixMath.fromMatrixToArray(value);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private double[] _value;

    private int _rowCount;

    private int _columnCount;
}
