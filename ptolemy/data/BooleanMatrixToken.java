/* A token that contains a 2-D boolean matrix.

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

//////////////////////////////////////////////////////////////////////////
//// BooleanMatrixToken
/**
A token that contains a 2-D boolean matrix.

@author Yuhong Xiong, Steve Neuendorffer
@version $Id$
@since Ptolemy II 0.2
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
     *  @exception IllegalActionException If the specified matrix
     *   is null.
     */
    public BooleanMatrixToken(boolean[][] value)
            throws IllegalActionException {
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
        Token token = (new ParseTreeEvaluator()).evaluateParseTree(tree);
        if (token instanceof BooleanMatrixToken) {
            boolean[][] value = ((BooleanMatrixToken)token).booleanMatrix();
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
                    "BooleanMatrixToken: The specified"
                    + " array is null.");
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
                _value[i / columns][i % columns] =
                    ((BooleanToken)token).booleanValue();
            } else {
                throw new IllegalActionException("BooleanMatrixToken: Element "
                        + i + " in the array with value " + token +
                        " is not a ScalarToken");
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a copy of the contained 2-D matrix.
     *  It is safe for the caller to modify the returned matrix.
     *  @return A 2-D boolean matrix.
     */
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
            return (BooleanMatrixToken)token;
        }

        int compare = TypeLattice.compare(BaseType.BOOLEAN_MATRIX, token);
        if (compare == CPO.LOWER || compare == CPO.INCOMPARABLE) {
            throw new IllegalActionException(
                    notSupportedIncomparableConversionMessage(
                            token, "[boolean]"));
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
        throw new IllegalActionException(
                notSupportedConversionMessage(token, "[boolean]"));
    }

    /** Return true if the argument is an instance of BooleanMatrixToken
     *  of the same dimensions and the corresponding elements of the matrices
     *  are equal.
     *  @param object An instance of Object.
     *  @return True if the argument is an instance of BooleanMatrixToken
     *   of the same dimensions and the corresponding elements of the
     *   matrices are equal.
     */
    public boolean equals(Object object) {
        // This test rules out instances of a subclass.
        if (object.getClass() != getClass()) {
            return false;
        }

        BooleanMatrixToken matrixArgument = (BooleanMatrixToken)object;
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
     *  @return BaseType.BOOLEAN_MATRIX
     */
    public Type getType() {
        return BaseType.BOOLEAN_MATRIX;
    }

    /** Return a hash code value for this token. This method returns the
     *  number of elements with value true in the contained matrix.
     *  @return A hash code value for this token.
     */
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

    /** Return a new Token representing the left multiplicative
     *  identity. The returned token contains an identity matrix
     *  whose dimensions are the same as the number of rows of
     *  the matrix contained in this token.
     *  @return A new BooleanMatrixToken containing the left multiplicative
     *   identity.
     */
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
    public Token zero() {
        try {
            return new BooleanMatrixToken(
                    new boolean [_rowCount][_columnCount]);
        } catch (IllegalActionException illegalAction) {
            // should not happen
            throw new InternalErrorException("BooleanMatrixToken.zero: "
                    + "Cannot create zero matrix.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected methods                   ////

    /** Return an new identity matrix with the specified dimension. The
     *  matrix is square, so only one dimension specifier is needed.
     */
    protected boolean [][] _createIdentity(int dim) {
        boolean [][] a = new boolean[dim][dim];
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
