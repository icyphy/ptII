/* A token that contains a 2-D FixPoint matrix.

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
 This class does not handle operations well, because the result may have
 unusual precision.
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
import ptolemy.math.FixPoint;
import ptolemy.math.Precision;
import ptolemy.math.Quantizer;

///////////////////////////////////////////////////////////////////
//// FixMatrixToken

/**
 A token that contains a 2-D FixToken matrix.

 @author Bart Kienhuis, Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 0.4
 @Pt.ProposedRating Yellow (neuendor)
 @Pt.AcceptedRating Yellow (kienhuis)
 @see ptolemy.math.FixPoint
 */
public class FixMatrixToken extends MatrixToken {
    /** Construct a FixMatrixToken with a one by one matrix. The only
     *  element present in the matrix has value 0.0 and a precision of
     *  (32.0) which means 32 bits of which 0 bits are used for the
     *  fractional part.
     */
    public FixMatrixToken() {
        _rowCount = 1;
        _columnCount = 1;
        _precision = new Precision(32, 32);
        _value = new FixPoint[1][1];
        _value[0][0] = Quantizer.round(0.0, _precision);
    }

    /** Construct a FixMatrixToken with the specified 2-D matrix.
     *  This method makes a copy of the matrix and stores the copy,
     *  so changes on the specified matrix after this token is
     *  constructed will not affect the content of this token.
     *  If any element of the specified matrix is null, then it
     *  is replaced with an element with value zero and precision
     *  the same as previous elements in the matrix.  Note that the
     *  very first element cannot be null.
     *  @param value the 2D matrix of FixPoint values.
     *  @exception IllegalActionException If the precisions of the
     *   entries in the matrix are not all identical, or the specified
     *   matrix is null.
     */
    public FixMatrixToken(FixPoint[][] value) throws IllegalActionException {
        if (value == null) {
            throw new IllegalActionException("FixMatrixToken: The specified "
                    + "matrix is null.");
        }

        _initialize(value, null);
    }

    /** Construct a FixMatrixToken from the specified string.
     *  @param init A string expression of a 2-D fix matrix.
     *  @exception IllegalActionException If the string does
     *   not contain a parsable 2-D fix matrix.
     */
    public FixMatrixToken(String init) throws IllegalActionException {
        PtParser parser = new PtParser();
        ASTPtRootNode tree = parser.generateParseTree(init);
        Token token = new ParseTreeEvaluator().evaluateParseTree(tree);

        if (token instanceof FixMatrixToken) {
            FixPoint[][] value = ((FixMatrixToken) token).fixMatrix();
            _initialize(value, null);
        } else {
            throw new IllegalActionException("A FixMatrixToken cannot be"
                    + " created from the expression '" + init + "'");
        }
    }

    /** Construct a FixMatrixToken with the specified 2-D matrix.
     *  This method makes a copy of the matrix and stores the copy,
     *  so changes on the specified matrix after this token is
     *  constructed will not affect the content of this token.
     *  If any element of the specified matrix is null, then it
     *  is replaced with an element with value zero and precision
     *  the same as previous elements in the matrix.  Note that the
     *  very first element cannot be null.
     *  @param value the 2D matrix of FixPoint values.
     *  @param precision The precision to use.
     *  @exception IllegalActionException If the precisions of the
     *   entries in the matrix are not all identical, or the specified
     *   matrix is null.
     */
    public FixMatrixToken(FixPoint[][] value, Precision precision)
            throws IllegalActionException {
        if (value == null) {
            throw new IllegalActionException("FixMatrixToken: The specified "
                    + "matrix is null.");
        }
        _initialize(value, precision);
    }

    /** Construct an FixMatrixToken from the specified array of
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
     *  of the array cannot be losslessly converted to a fixed point.
     */
    public FixMatrixToken(Token[] tokens, int rows, int columns)
            throws IllegalActionException {
        if (tokens == null) {
            throw new IllegalActionException("FixMatrixToken: The specified"
                    + " array is null.");
        }

        if (tokens.length != rows * columns) {
            throw new IllegalActionException("FixMatrixToken: The specified"
                    + " array is not of the correct length");
        }

        _rowCount = rows;
        _columnCount = columns;
        _value = new FixPoint[rows][columns];

        for (int i = 0; i < tokens.length; i++) {
            Token token = tokens[i];

            if (token instanceof ScalarToken) {
                _value[i / columns][i % columns] = ((ScalarToken) token)
                        .fixValue();
            } else if (token instanceof FixToken) {
                _value[i / columns][i % columns] = ((FixToken) token)
                        .fixValue();
            } else {
                throw new IllegalActionException("FixMatrixToken: Element " + i
                        + " in the array with value " + token
                        + " is not a ScalarToken");
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Convert the specified token into an instance of FixMatrixToken.
     *  This method does lossless conversion.
     *  If the argument is already an instance of FixMatrixToken,
     *  it is returned without any change. Otherwise, if the argument
     *  is below FixMatrixToken in the type hierarchy, it is converted to
     *  an instance of FixMatrixToken or one of the subclasses of
     *  FixMatrixToken and returned. If none of the above condition is
     *  met, an exception is thrown.
     *  @param token The token to be converted to a FixMatrixToken.
     *  @return A FixMatrixToken
     *  @exception IllegalActionException If the conversion cannot
     *   be carried out.
     */
    public static FixMatrixToken convert(Token token)
            throws IllegalActionException {
        if (token instanceof FixMatrixToken) {
            return (FixMatrixToken) token;
        }

        int compare = TypeLattice.compare(BaseType.FIX_MATRIX, token);

        if (compare == CPO.LOWER || compare == CPO.INCOMPARABLE) {
            throw new IllegalActionException(
                    notSupportedIncomparableConversionMessage(token, "[fix]"));
        }

        // try Fix
        //         compare = TypeLattice.compare(BaseType.FIX, token);
        //         if (compare == CPO.SAME || compare == CPO.HIGHER) {
        //             FixPoint[][] result = new FixPoint[1][1];
        //             FixToken tem = FixToken.convert(token);
        //             result[0][0] = tem.fixValue();
        //             return new FixMatrixToken(result);
        //         }
        // The argument is below FixMatrixToken in the type hierarchy,
        // but I don't recognize it.
        throw new IllegalActionException(notSupportedConversionMessage(token,
                "[fix]"));
    }

    /** Return a new matrix that is a sub-matrix of this matrix.
     *  @param rowStart The row to start on.
     *  @param colStart The column to start on.
     *  @param rowSpan The number of rows to copy.
     *  @param colSpan The number of columns to copy.
     *  @return a sub-matrix of this matrix.
     *  @exception IllegalActionException If the returned matrix is
     *  empty or if the specified parameters result in out of bounds
     *  accesses.
     */
    @Override
    public MatrixToken crop(int rowStart, int colStart, int rowSpan, int colSpan)
            throws IllegalActionException {
        FixPoint[][] value = this.fixMatrix();
        try {
            FixPoint[][] result = new FixPoint[rowSpan][colSpan];
            for (int i = 0; i < rowSpan; i++) {
                System.arraycopy(value[rowStart + i], colStart, result[i], 0,
                        colSpan);
            }
            return new FixMatrixToken(result);
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new IllegalActionException(
                    "Matrix crop indices out of bounds (rowStart = " + rowStart
                            + ", colStart = " + colStart + ", rowSpan = "
                            + rowSpan + ", colSpan = " + colSpan + ").");
        }
    }

    /** Return true if the argument is an instance of FixMatrixToken
     *  of the same dimensions and the corresponding elements of the matrices
     *  are equal.
     *  @param object An instance of Object.
     *  @return True if the argument is an instance of FixMatrixToken
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

        FixMatrixToken matrixArgument = (FixMatrixToken) object;

        if (_rowCount != matrixArgument.getRowCount()) {
            return false;
        }

        if (_columnCount != matrixArgument.getColumnCount()) {
            return false;
        }

        FixPoint[][] matrix = matrixArgument.fixMatrix();

        for (int i = 0; i < _rowCount; i++) {
            for (int j = 0; j < _columnCount; j++) {
                if (!_value[i][j].equals(matrix[i][j])) {
                    return false;
                }
            }
        }

        return true;
    }

    /** Return the content of this token as a new 2-D FixPoint matrix.
     *  @return A 2-D FixPoint matrix
     */
    @Override
    public FixPoint[][] fixMatrix() {
        FixPoint[][] matrix = new FixPoint[_rowCount][_columnCount];

        for (int i = 0; i < _rowCount; i++) {
            for (int j = 0; j < _columnCount; j++) {
                // FixPoint is immutable, so no need to copy.
                matrix[i][j] = _value[i][j];
            }
        }

        return matrix;
    }

    /** Return the number of columns in the matrix.
     *  @return The number of columns in the matrix.
     */
    @Override
    public int getColumnCount() {
        return _columnCount;
    }

    /** Return the element of the matrix at the specified
     *  row and column in a FixToken.
     *  @param row The row index of the desired element.
     *  @param column The column index of the desired element.
     *  @return A FixToken containing the matrix element.
     *  @exception ArrayIndexOutOfBoundsException If the specified
     *   row or column number is outside the range of the matrix.
     */
    @Override
    public Token getElementAsToken(int row, int column)
            throws ArrayIndexOutOfBoundsException {
        return new FixToken(_value[row][column]);
    }

    /** Return the element of the contained matrix at the specified
     *  row and column.
     *  @param row The row index of the desired element.
     *  @param column The column index of the desired element.
     *  @return The FixPoint at the specified matrix entry.
     *  @exception ArrayIndexOutOfBoundsException If the specified
     *   row or column number is outside the range of the matrix.
     */
    public FixPoint getElementAt(int row, int column) {
        return _value[row][column];
    }

    /** Return the Type of the tokens contained in this matrix token.
     *  This must be a type representing a scalar token.
     *  @return BaseType.UNSIZED_FIX.
     */
    @Override
    public Type getElementType() {
        return BaseType.UNSIZED_FIX;
    }

    /** Return the number of rows in the matrix.
     *  @return The number of rows in the matrix.
     */
    @Override
    public int getRowCount() {
        return _rowCount;
    }

    /** Return the type of this token.
     *  @return BaseType.FIX_MATRIX
     */
    @Override
    public Type getType() {
        return BaseType.FIX_MATRIX;
    }

    /** Return a hash code value for this token. This method returns the
     *  integer portion of the sum of the elements.
     *  @return A hash code value for this token.
     */
    @Override
    public int hashCode() {
        double code = 0.0;

        for (int i = 0; i < _rowCount; i++) {
            for (int j = 0; j < _columnCount; j++) {
                code += _value[i][j].doubleValue();
            }
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
        FixPoint[][] tiled = new FixPoint[rows][columns];
        int row = 0;
        for (int i = 0; i < matrices.length; i++) {
            int column = 0;
            for (int j = 0; j < matrices[i].length; j++) {
                if (!(matrices[i][j] instanceof FixMatrixToken)) {
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
                // There is no FixMatrixMath class, so we need
                // to implement the matrix copy here.
                for (int ii = 0; ii < rowCount; ii++) {
                    System.arraycopy(matrices[i][j].fixMatrix()[ii], 0,
                            tiled[row + ii], column, columnCount);
                }
                // Starting position for the next column.
                column += matrices[0][j].getColumnCount();
            }
            // Starting position for the next column.
            row += matrices[i][0].getRowCount();
        }
        return new FixMatrixToken(tiled);
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
        FixPoint[][] source = fixMatrix();
        int row = 0;
        for (int i = 0; i < rows.length; i++) {
            int column = 0;
            for (int j = 0; j < columns.length; j++) {
                FixPoint[][] contents = new FixPoint[rows[i]][columns[j]];
                int rowspan = rows[i];
                if (row + rowspan > source.length) {
                    rowspan = source.length - row;
                }
                int columnspan = columns[j];
                if (column + columnspan > source[0].length) {
                    columnspan = source[0].length - column;
                }
                if (columnspan > 0 && rowspan > 0) {
                    // There is no FixPointMatrixMath class, so we need
                    // to implement the matrix copy here.
                    for (int ii = 0; ii < rowspan; ii++) {
                        System.arraycopy(source[row + ii], column,
                                contents[ii], 0, columnspan);
                    }
                }
                column += columns[j];
                try {
                    // Use the precision of the (0,0) element because the entire submatrix
                    // may be zero, in which case the precision cannot be inferred.
                    result[i][j] = new FixMatrixToken(contents, getElementAt(0,
                            0).getPrecision());
                } catch (IllegalActionException e) {
                    throw new InternalErrorException(e);
                }
            }
            row += rows[i];
        }
        return result;
    }

    /** Return a new Token representing the left multiplicative
     *  identity with the same precision as the current
     *  FixMatrixToken. The returned token contains an identity matrix
     *  whose dimensions are the same as the number of rows of the
     *  matrix contained in this token.
     *  @return A new FixMatrixToken containing the left multiplicative
     *   identity.
     */
    @Override
    public Token one() {
        FixPoint[][] result = new FixPoint[_rowCount][_rowCount];

        for (int i = 0; i < _rowCount; i++) {
            for (int j = 0; j < _rowCount; j++) {
                result[i][j] = Quantizer.round(0.0, _precision);
            }

            result[i][i] = Quantizer.round(1.0, _precision);
        }

        try {
            return new FixMatrixToken(result);
        } catch (IllegalActionException ex) {
            // precisions are all the same, so this should not be thrown.
            throw new InternalErrorException("Unequal precisions!");
        }
    }

    /** Return a new Token representing the right multiplicative
     *  identity with the same precision as the current
     *  FixMatrixToken.. The returned token contains an identity
     *  matrix whose dimensions are the same as the number of columns of
     *  the matrix contained in this token.
     *  @return A new FixMatrixToken containing the right multiplicative
     *   identity.
     */
    @Override
    public Token oneRight() {
        FixPoint[][] result = new FixPoint[_columnCount][_columnCount];

        for (int i = 0; i < _columnCount; i++) {
            for (int j = 0; j < _columnCount; j++) {
                result[i][j] = Quantizer.round(0.0, _precision);
            }

            result[i][i] = Quantizer.round(0.0, _precision);
        }

        try {
            return new FixMatrixToken(result);
        } catch (IllegalActionException ex) {
            // precisions are all the same, so this should not be thrown.
            throw new InternalErrorException("Unequal precisions!");
        }
    }

    /** Return a new Token representing the additive identity with the
     *  same precision as the current FixMatrixToken.  The returned
     *  token contains a matrix whose elements are all zero, and the
     *  size of the matrix is the same as the matrix contained in this
     *  token.
     *  @return A new FixMatrixToken containing the additive identity.
     */
    @Override
    public Token zero() {
        FixPoint[][] result = new FixPoint[_rowCount][_columnCount];
        FixPoint zero = Quantizer.round(0.0, _precision);

        for (int i = 0; i < _rowCount; i++) {
            for (int j = 0; j < _columnCount; j++) {
                result[i][j] = zero;
            }
        }

        try {
            return new FixMatrixToken(result);
        } catch (IllegalActionException ex) {
            // precisions are all the same, so this should not be thrown.
            throw new InternalErrorException("Unequal precisions!");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a new token whose value is the value of the argument
     *  Token added to the value of this Token.  It is assumed that
     *  the type of the argument is FixMatrixToken.
     *  @param rightArgument The token to add to this token.
     *  @exception IllegalActionException If the units are not
     *  compatible, or this operation is not supported by the derived
     *  class.
     *  @return A new FixMatrixToken containing the result.
     */
    @Override
    protected MatrixToken _add(MatrixToken rightArgument)
            throws IllegalActionException {
        FixMatrixToken convertedArgument = (FixMatrixToken) rightArgument;
        FixPoint[][] result = convertedArgument.fixMatrix();

        for (int i = 0; i < _rowCount; i++) {
            for (int j = 0; j < _columnCount; j++) {
                result[i][j] = result[i][j].add(_value[i][j]);
            }
        }

        return new FixMatrixToken(result);
    }

    /** Return a new token whose value is the value of the argument
     *  Token added from the value of each element of this Token. It is
     *  assumed that the type of the argument is the same as the type
     *  of each element of this class or is a matrix with one element.
     *  @param rightArgument The token to add from this token.
     *  @exception IllegalActionException If this operation is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
     */
    @Override
    protected MatrixToken _addElement(Token rightArgument)
            throws IllegalActionException {
        FixPoint scalar;
        if (rightArgument instanceof FixMatrixToken) {
            if (((FixMatrixToken) rightArgument).getRowCount() != 1
                    || ((FixMatrixToken) rightArgument).getColumnCount() != 1) {
                // Throw an exception.
                return super._moduloElement(rightArgument);
            }
            scalar = ((FixMatrixToken) rightArgument).getElementAt(0, 0);
        } else {
            scalar = ((FixToken) rightArgument).fixValue();
        }
        FixPoint[][] result = fixMatrix();

        for (int i = 0; i < _rowCount; i++) {
            for (int j = 0; j < _columnCount; j++) {
                result[i][j] = result[i][j].add(scalar);
            }
        }

        return new FixMatrixToken(result);
    }

    /** Return a new token whose value is the value of this token
     *  multiplied by the value of the argument token.  It is assumed
     *  that the type of the argument is FixMatrixToken.
     *  @param rightArgument The token to multiply this token by.
     *  @return A new FixMatrixToken containing the result.
     *  @exception IllegalActionException If the units are not
     *   compatible, or if the matrix dimensions are incompatible.
     */
    @Override
    protected MatrixToken _multiply(MatrixToken rightArgument)
            throws IllegalActionException {
        FixMatrixToken convertedArgument = (FixMatrixToken) rightArgument;

        if (_columnCount != convertedArgument._rowCount) {
            throw new IllegalActionException(
                    "Matrix dimensions are not compatible. Cannot multiply.");
        }

        FixPoint[][] result = new FixPoint[_rowCount][convertedArgument._columnCount];

        for (int i = 0; i < _rowCount; i++) {
            for (int j = 0; j < convertedArgument._columnCount; j++) {
                FixPoint sum = _value[i][0]
                        .multiply(convertedArgument._value[0][j]);

                for (int k = 1; k < _columnCount; k++) {
                    sum = sum.add(_value[i][k]
                            .multiply(convertedArgument._value[k][j]));
                }

                result[i][j] = sum;
            }
        }

        return new FixMatrixToken(result);
    }

    /** Return a new token whose value is the value of the argument
     *  Token multiplyed from the value of each element of this Token. It is
     *  assumed that the type of the argument is the same as the type
     *  of each element of this class or is a matrix with one element.
     *  @param rightArgument The token to multiply from this token.
     *  @exception IllegalActionException If this operation is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
     */
    @Override
    protected MatrixToken _multiplyElement(Token rightArgument)
            throws IllegalActionException {
        FixPoint scalar;
        if (rightArgument instanceof FixMatrixToken) {
            if (((FixMatrixToken) rightArgument).getRowCount() != 1
                    || ((FixMatrixToken) rightArgument).getColumnCount() != 1) {
                // Throw an exception.
                return super._moduloElement(rightArgument);
            }
            scalar = ((FixMatrixToken) rightArgument).getElementAt(0, 0);
        } else {
            scalar = ((FixToken) rightArgument).fixValue();
        }
        FixPoint[][] result = fixMatrix();

        for (int i = 0; i < _rowCount; i++) {
            for (int j = 0; j < _columnCount; j++) {
                //   System.out.println("left = " + result[i][j] + ", "
                //       + result[i][j].getPrecision());
                //   System.out.println("right = " + scalar + ", "
                //       + scalar.getPrecision());
                result[i][j] = result[i][j].multiply(scalar);

                //   System.out.println("result = " + result[i][j] + ", "
                //        + result[i][j].getPrecision());
            }
        }

        return new FixMatrixToken(result);
    }

    /** Return a new token whose value is the value of the argument
     *  Token subtracted to the value of this Token.  It is assumed that
     *  the type of the argument is FixMatrixToken.
     *  @param rightArgument The token to subtract to this token.
     *  @exception IllegalActionException If the units are not
     *  compatible, or this operation is not supported by the derived
     *  class.
     *  @return A new FixMatrixToken containing the result.
     */
    @Override
    protected MatrixToken _subtract(MatrixToken rightArgument)
            throws IllegalActionException {
        FixMatrixToken convertedArgument = (FixMatrixToken) rightArgument;
        FixPoint[][] result = convertedArgument.fixMatrix();

        for (int i = 0; i < _rowCount; i++) {
            for (int j = 0; j < _columnCount; j++) {
                result[i][j] = result[i][j].subtract(_value[i][j]);
            }
        }

        return new FixMatrixToken(result);
    }

    /** Return a new token whose value is the value of the argument
     *  Token subtracted from the value of each element of this Token. It is
     *  assumed that the type of the argument is the same as the type
     *  of each element of this class or is a matrix with one element.
     *  @param rightArgument The token to subtract from this token.
     *  @exception IllegalActionException If this operation is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
     */
    @Override
    protected MatrixToken _subtractElement(Token rightArgument)
            throws IllegalActionException {
        FixPoint scalar;
        if (rightArgument instanceof FixMatrixToken) {
            if (((FixMatrixToken) rightArgument).getRowCount() != 1
                    || ((FixMatrixToken) rightArgument).getColumnCount() != 1) {
                // Throw an exception.
                return super._moduloElement(rightArgument);
            }
            scalar = ((FixMatrixToken) rightArgument).getElementAt(0, 0);
        } else {
            scalar = ((FixToken) rightArgument).fixValue();
        }
        FixPoint[][] result = fixMatrix();

        for (int i = 0; i < _rowCount; i++) {
            for (int j = 0; j < _columnCount; j++) {
                result[i][j] = result[i][j].subtract(scalar);
            }
        }

        return new FixMatrixToken(result);
    }

    /** Return a new token whose value is the value of the argument
     *  Token subtracted from the value of each element of this Token. It is
     *  assumed that the type of the argument is the same as the type
     *  of each element of this class or is a matrix with one element.
     *  @param rightArgument The token to subtract from this token.
     *  @exception IllegalActionException If this operation is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
     */
    @Override
    protected MatrixToken _subtractElementReverse(Token rightArgument)
            throws IllegalActionException {
        FixPoint scalar;
        if (rightArgument instanceof FixMatrixToken) {
            if (((FixMatrixToken) rightArgument).getRowCount() != 1
                    || ((FixMatrixToken) rightArgument).getColumnCount() != 1) {
                // Throw an exception.
                return super._moduloElement(rightArgument);
            }
            scalar = ((FixMatrixToken) rightArgument).getElementAt(0, 0);
        } else {
            scalar = ((FixToken) rightArgument).fixValue();
        }
        FixPoint[][] result = fixMatrix();

        for (int i = 0; i < _rowCount; i++) {
            for (int j = 0; j < _columnCount; j++) {
                result[i][j] = scalar.subtract(result[i][j]);
            }
        }

        return new FixMatrixToken(result);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Initialize the row and column count and copy the specified
     *  matrix. This method is used by the constructors.
     *  @param value The value.
     *  @param precision The precision, or null to infer it from the values.
     */
    private void _initialize(FixPoint[][] value, Precision precision)
            throws IllegalActionException {
        _rowCount = value.length;
        _columnCount = value[0].length;
        _value = new FixPoint[_rowCount][_columnCount];

        for (int i = 0; i < _rowCount; i++) {
            for (int j = 0; j < _columnCount; j++) {
                _value[i][j] = value[i][j];
                if (_value[i][j] == null) {
                    // NOTE: This requires that the very first element
                    // must be non-null, in order to set the precision,
                    // or that the precision be provided in the argument.
                    _value[i][j] = Quantizer.round(0.0, precision);
                }
                if (precision == null) {
                    precision = _value[i][j].getPrecision();
                }

                if (_precision != null && !_precision.equals(precision)) {
                    throw new IllegalActionException(
                            "Attempt to create a FixMatrixToken"
                                    + " with unequal precisions: " + _precision
                                    + " and " + precision);
                }

                _precision = precision;
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The 2D matrix of FixPoints.
    private FixPoint[][] _value = null;

    // The precision of all entries in the FixPoint matrix.
    private Precision _precision = null;

    // The number of rows of the matrix.
    private int _rowCount = 0;

    // The number of column of the matrix.
    private int _columnCount = 0;
}
