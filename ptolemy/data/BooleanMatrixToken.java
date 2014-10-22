/* A token that contains a 2-D boolean matrix.

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

 There are currently no interesting operations implemented.
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

///////////////////////////////////////////////////////////////////
//// BooleanMatrixToken

/**
 A token that contains a 2-D boolean matrix.

 @author Yuhong Xiong, Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Green (neuendor)
 @Pt.AcceptedRating Yellow (cxh)
 */
public class BooleanMatrixToken extends MatrixToken {
    /** Construct an BooleanMatrixToken with a one by one matrix. The
     *  only element in the matrix has value false.
     */
    public BooleanMatrixToken() {
        _rowCount = 1;
        _columnCount = 1;
        _value = new boolean[1][1];
        _value[0][0] = false;
    }

    /** Construct a BooleanMatrixToken with the specified 2-D matrix.
     *  This method makes a copy of the matrix and stores the copy,
     *  so changes on the specified matrix after this token is
     *  constructed will not affect the content of this token.
     *  @param value The 2-D boolean matrix.
     *  @exception IllegalActionException If the specified matrix
     *   is null.
     */
    public BooleanMatrixToken(boolean[][] value) throws IllegalActionException {
        if (value == null) {
            throw new IllegalActionException("BooleanMatrixToken: The "
                    + "specified matrix is null.");
        }

        _initialize(value);
    }

    /** Construct a BooleanMatrixToken from the specified string.
     *  @param init A string expression of a boolean matrix.
     *  @exception IllegalActionException If the string does
     *   not contain a parsable boolean matrix.
     */
    public BooleanMatrixToken(String init) throws IllegalActionException {
        PtParser parser = new PtParser();
        ASTPtRootNode tree = parser.generateParseTree(init);
        Token token = new ParseTreeEvaluator().evaluateParseTree(tree);

        if (token instanceof BooleanMatrixToken) {
            boolean[][] value = ((BooleanMatrixToken) token).booleanMatrix();
            _initialize(value);
        } else {
            throw new IllegalActionException("A BooleanMatrixToken cannot be"
                    + " created from the expression '" + init + "'");
        }
    }

    /** Construct an BooleanMatrixToken from the specified array of
     *  tokens.  The tokens in the array must be scalar tokens
     *  convertible into integers.
     *  @param tokens The array of tokens, which must contains
     *  rows*columns BooleanTokens.
     *  @param rows The number of rows in the matrix to be created.
     *  @param columns The number of columns in the matrix to be
     *  created.
     *  @exception IllegalActionException If the array of tokens is
     *  null, or the length of the array is not correct, or if one of
     *  the elements of the array is null, or if one of the elements
     *  of the array cannot be losslessly converted to a boolean.
     */
    public BooleanMatrixToken(Token[] tokens, int rows, int columns)
            throws IllegalActionException {
        if (tokens == null) {
            throw new IllegalActionException(
                    "BooleanMatrixToken: The specified" + " array is null.");
        }

        if (tokens.length != rows * columns) {
            throw new IllegalActionException(
                    "BooleanMatrixToken: The specified"
                            + " array is not of the correct length");
        }

        _rowCount = rows;
        _columnCount = columns;
        _value = new boolean[rows][columns];

        for (int i = 0; i < tokens.length; i++) {
            Token token = tokens[i];

            if (token instanceof BooleanToken) {
                _value[i / columns][i % columns] = ((BooleanToken) token)
                        .booleanValue();
            } else {
                throw new IllegalActionException("BooleanMatrixToken: Element "
                        + i + " in the array with value " + token
                        + " is not a ScalarToken");
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a copy of the contained 2-D matrix.
     *  It is safe for the caller to modify the returned matrix.
     *  @return A 2-D boolean matrix.
     */
    @Override
    public boolean[][] booleanMatrix() {
        boolean[][] result = new boolean[_rowCount][_columnCount];

        for (int i = 0; i < _rowCount; i++) {
            for (int j = 0; j < _columnCount; j++) {
                result[i][j] = _value[i][j];
            }
        }

        return result;
    }

    /** Convert the specified token into an instance of BooleanMatrixToken.
     *  This method does lossless conversion.
     *  If the argument is already an instance of BooleanMatrixToken,
     *  it is returned without any change. Otherwise, if the argument
     *  is below BooleanMatrixToken in the type hierarchy, it is converted to
     *  an instance of BooleanMatrixToken or one of the subclasses of
     *  BooleanMatrixToken and returned. If none of the above conditions are
     *  met, an exception is thrown.
     *  @param token The token to be converted to a BooleanMatrixToken.
     *  @return A BooleanMatrixToken
     *  @exception IllegalActionException If the conversion cannot
     *   be carried out.
     */
    public static BooleanMatrixToken convert(Token token)
            throws IllegalActionException {
        if (token instanceof BooleanMatrixToken) {
            return (BooleanMatrixToken) token;
        }

        int compare = TypeLattice.compare(BaseType.BOOLEAN_MATRIX, token);

        if (compare == CPO.LOWER || compare == CPO.INCOMPARABLE) {
            throw new IllegalActionException(
                    notSupportedIncomparableConversionMessage(token,
                            "[boolean]"));
        }

        // try boolean
        //         compare = TypeLattice.compare(BaseType.BOOLEAN, token);
        //         if (compare == CPO.SAME || compare == CPO.HIGHER) {
        //             BooleanToken tem = (BooleanToken)
        //                 BooleanToken.convert(token);
        //             boolean[][] result = new boolean[1][1];
        //             result[0][0] = tem.booleanValue();
        //             return new BooleanMatrixToken(result);
        //         }
        // The argument is below BooleanMatrixToken in the type hierarchy,
        // but I don't recognize it.
        throw new IllegalActionException(notSupportedConversionMessage(token,
                "[boolean]"));
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
        boolean[][] value = this.booleanMatrix();
        try {
            boolean[][] result = new boolean[rowSpan][colSpan];
            for (int i = 0; i < rowSpan; i++) {
                System.arraycopy(value[rowStart + i], colStart, result[i], 0,
                        colSpan);
            }
            return new BooleanMatrixToken(result);
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new IllegalActionException(
                    "Matrix crop indices out of bounds (rowStart = " + rowStart
                            + ", colStart = " + colStart + ", rowSpan = "
                            + rowSpan + ", colSpan = " + colSpan + ").");
        }
    }

    /** Return true if the argument is an instance of BooleanMatrixToken
     *  of the same dimensions and the corresponding elements of the matrices
     *  are equal.
     *  @param object An instance of Object.
     *  @return True if the argument is an instance of BooleanMatrixToken
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

        BooleanMatrixToken matrixArgument = (BooleanMatrixToken) object;

        if (_rowCount != matrixArgument.getRowCount()) {
            return false;
        }

        if (_columnCount != matrixArgument.getColumnCount()) {
            return false;
        }

        boolean[][] matrix = matrixArgument.booleanMatrix();

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
    @Override
    public int getColumnCount() {
        return _columnCount;
    }

    /** Return the element of the matrix at the specified
     *  row and column in a BooleanToken.
     *  @param row The row index of the desired element.
     *  @param column The column index of the desired element.
     *  @return A BooleanToken containing the matrix element.
     *  @exception ArrayIndexOutOfBoundsException If the specified
     *   row or column number is outside the range of the matrix.
     */
    @Override
    public Token getElementAsToken(int row, int column)
            throws ArrayIndexOutOfBoundsException {
        return BooleanToken.getInstance(_value[row][column]);
    }

    /** Return the element of the contained matrix at the specified
     *  row and column.
     *  @param row The row index of the desired element.
     *  @param column The column index of the desired element.
     *  @return The boolean at the specified matrix entry.
     *  @exception ArrayIndexOutOfBoundsException If the specified
     *   row or column number is outside the range of the matrix.
     */
    public boolean getElementAt(int row, int column) {
        return _value[row][column];
    }

    /** Return the Type of the tokens contained in this matrix token.
     *  @return BaseType.INT.
     */
    @Override
    public Type getElementType() {
        return BaseType.INT;
    }

    /** Return the number of rows in the matrix.
     *  @return The number of rows in the matrix.
     */
    @Override
    public int getRowCount() {
        return _rowCount;
    }

    /** Return the type of this token.
     *  @return BaseType.BOOLEAN_MATRIX
     */
    @Override
    public Type getType() {
        return BaseType.BOOLEAN_MATRIX;
    }

    /** Return a hash code value for this token. This method returns the
     *  number of elements with value true in the contained matrix.
     *  @return A hash code value for this token.
     */
    @Override
    public int hashCode() {
        int code = 0;

        for (int i = 0; i < _rowCount; i++) {
            for (int j = 0; j < _columnCount; j++) {
                if (_value[i][j]) {
                    code++;
                }
            }
        }

        return code;
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
        boolean[][] tiled = new boolean[rows][columns];
        int row = 0;
        for (int i = 0; i < matrices.length; i++) {
            int column = 0;
            for (int j = 0; j < matrices[i].length; j++) {
                if (!(matrices[i][j] instanceof BooleanMatrixToken)) {
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
                // There is no BooleanMatrixMath class, so we need
                // to implement the matrix copy here.
                for (int ii = 0; ii < rowCount; ii++) {
                    System.arraycopy(matrices[i][j].booleanMatrix()[ii], 0,
                            tiled[row + ii], column, columnCount);
                }
                // Starting position for the next column.
                column += matrices[0][j].getColumnCount();
            }
            // Starting position for the next column.
            row += matrices[i][0].getRowCount();
        }
        return new BooleanMatrixToken(tiled);
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
        boolean[][] source = booleanMatrix();
        int row = 0;
        for (int i = 0; i < rows.length; i++) {
            int column = 0;
            for (int j = 0; j < columns.length; j++) {
                boolean[][] contents = new boolean[rows[i]][columns[j]];
                int rowspan = rows[i];
                if (row + rowspan > source.length) {
                    rowspan = source.length - row;
                }
                int columnspan = columns[j];
                if (column + columnspan > source[0].length) {
                    columnspan = source[0].length - column;
                }
                if (columnspan > 0 && rowspan > 0) {
                    // There is no BooleanMatrixMath class, so we need
                    // to implement the matrix copy here.
                    for (int ii = 0; ii < rowspan; ii++) {
                        System.arraycopy(source[row + ii], column,
                                contents[ii], 0, columnspan);
                    }
                }
                column += columns[j];
                try {
                    result[i][j] = new BooleanMatrixToken(contents);
                } catch (IllegalActionException e) {
                    throw new InternalErrorException(e);
                }
            }
            row += rows[i];
        }
        return result;
    }

    /** Return a new Token representing the left multiplicative
     *  identity. The returned token contains an identity matrix
     *  whose dimensions are the same as the number of rows of
     *  the matrix contained in this token.
     *  @return A new BooleanMatrixToken containing the left multiplicative
     *   identity.
     */
    @Override
    public Token one() {
        try {
            return new BooleanMatrixToken(_createIdentity(_rowCount));
        } catch (IllegalActionException illegalAction) {
            // should not happen
            throw new InternalErrorException("BooleanMatrixToken.one: "
                    + "Cannot create identity matrix.");
        }
    }

    /** Return a new Token representing the right multiplicative
     *  identity. The returned token contains an identity matrix
     *  whose dimensions are the same as the number of columns of
     *  the matrix contained in this token.
     *  @return A new BooleanMatrixToken containing the right multiplicative
     *   identity.
     */
    @Override
    public Token oneRight() {
        try {
            return new BooleanMatrixToken(_createIdentity(_columnCount));
        } catch (IllegalActionException illegalAction) {
            // should not happen
            throw new InternalErrorException("BooleanMatrixToken.oneRight: "
                    + "Cannot create identity matrix.");
        }
    }

    /** Return a new Token representing the additive identity.
     *  The returned token contains a matrix whose elements are
     *  all zero, and the size of the matrix is the same as the
     *  matrix contained in this token.
     *  @return A new IntMatrixToken containing the additive identity.
     */
    @Override
    public Token zero() {
        try {
            return new BooleanMatrixToken(new boolean[_rowCount][_columnCount]);
        } catch (IllegalActionException illegalAction) {
            // should not happen
            throw new InternalErrorException("BooleanMatrixToken.zero: "
                    + "Cannot create zero matrix.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return an new identity matrix with the specified dimension. The
     *  matrix is square, so only one dimension specifier is needed.
     *  @param dim The dimension
     *  @return the identity matrix.
     */
    protected boolean[][] _createIdentity(int dim) {
        boolean[][] a = new boolean[dim][dim];

        // we rely on the fact Java fills the allocated matrix with false.
        for (int i = 0; i < dim; i++) {
            a[i][i] = true;
        }

        return a;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // Initialize the row and column count and copy the specified
    // matrix. This method is used by the constructors.
    private void _initialize(boolean[][] value) {
        _rowCount = value.length;
        _columnCount = value[0].length;
        _value = new boolean[_rowCount][_columnCount];

        for (int i = 0; i < _rowCount; i++) {
            for (int j = 0; j < _columnCount; j++) {
                _value[i][j] = value[i][j];
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private boolean[][] _value = null;

    private int _rowCount = 0;

    private int _columnCount = 0;
}
