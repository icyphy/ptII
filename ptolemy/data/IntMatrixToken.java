/* A token that contains a 2-D int matrix.

 Copyright (c) 1998-2002 The Regents of the University of California.
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

@ProposedRating Yellow (yuhong@eecs.berkeley.edu)
@AcceptedRating Yellow (wbwu@eecs.berkeley.edu)

*/

package ptolemy.data;
import ptolemy.kernel.util.*;
import ptolemy.graph.CPO;
import ptolemy.math.Complex;
import ptolemy.math.DoubleMatrixMath;
import ptolemy.math.IntegerMatrixMath;
import ptolemy.data.type.*;
import ptolemy.data.expr.PtParser;
import ptolemy.data.expr.ASTPtRootNode;

//////////////////////////////////////////////////////////////////////////
//// IntMatrixToken
/**
A token that contains a 2-D int matrix.

@author Yuhong Xiong, Jeff Tsay
@version $Id$
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
    public IntMatrixToken(final int[] value, int rows, int columns)
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
    public IntMatrixToken(final int[][] value) throws IllegalActionException {
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
    protected IntMatrixToken(final int[][] value, final int copy)
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
	IntMatrixToken token = (IntMatrixToken)tree.evaluateParseTree();
        int[][] value = token.intMatrix();
        _initialize(value, DO_COPY);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a new token whose value is the sum of this token
     *  and the argument. The type of the specified token
     *  must be such that either it can be converted to the type
     *  of this token, or the type of this token can be converted
     *  to the type of the specified token, without loss of
     *  information. The type of the returned token is one of the
     *  above two types that allows lossless conversion from the other.
     *  If the specified token is a matrix, its dimension must be the
     *  same as this token.
     *  @param token The token to add to this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the specified token is
     *   not of a type that can be added to this token.
     */
    public final Token add(Token token) throws IllegalActionException {
        int compare = TypeLattice.compare(this, token);
        if (compare == CPO.INCOMPARABLE) {
            throw new IllegalActionException(
                    _notSupportedMessage("add", this, token));
        } else if (compare == CPO.LOWER) {
            return token.addReverse(this);
        } else {
            // type of the specified token <= IntMatrixToken
            int[][] result = null;

            if (token instanceof ScalarToken) {
                int scalar = ((ScalarToken)token).intValue();
                result = IntegerMatrixMath.add(_value, scalar);
            } else {
                // the specified token is not a scalar.
                IntMatrixToken tem = (IntMatrixToken)this.convert(token);
                if (tem.getRowCount() != _rowCount ||
                        tem.getColumnCount() != _columnCount) {
                    throw new IllegalActionException("Cannot add two " +
                            "matrices with different dimensions.");
                }

                result = IntegerMatrixMath.add(
                        tem._getInternalIntMatrix(), _value);
            }
            return new IntMatrixToken(result);
        }
    }

    /** Return a new token whose value is the sum of this token
     *  and the argument. The type of the specified token must
     *  be lower than IntMatrixToken.
     *  @param token The token to add this Token to.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the type of the specified
     *   token is not lower than IntMatrixToken.
     */
    public final Token addReverse(Token token)
            throws IllegalActionException {
        int compare = TypeLattice.compare(this, token);
        if (! (compare == CPO.HIGHER)) {
            throw new IllegalActionException("The type of the specified "
                    + "token " + token.getClass().getName()
		    + " is not lower than "
                    + getClass().getName());
        }
        // add is commutative on int matrix.
        return add(token);
    }

    /** Return the content of this token as a 2-D Complex matrix.
     *  @return A 2-D Complex matrix
     */
    public final Complex[][] complexMatrix() {
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
    public static final Token convert(Token token)
            throws IllegalActionException {
        int compare = TypeLattice.compare(BaseType.INT_MATRIX, token);
        if (compare == CPO.LOWER || compare == CPO.INCOMPARABLE) {
            throw new IllegalActionException("IntMatrixToken.convert: " +
                    "type of argument: " + token.getClass().getName() +
                    "is higher or incomparable with IntMatrixToken in the " +
                    "type hierarchy.");
        }

        if (token instanceof IntMatrixToken) {
            return token;
        }

        // try int
        compare = TypeLattice.compare(BaseType.INT, token);
        if (compare == CPO.SAME || compare == CPO.HIGHER) {
            IntToken tem = (IntToken) IntToken.convert(token);
            int[][] result = new int[1][1];
            result[0][0] = tem.intValue();
            return new IntMatrixToken(result);
        }

        // try IntMatrix
        compare = TypeLattice.compare(BaseType.INT_MATRIX, token);
        if (compare == CPO.SAME || compare == CPO.HIGHER) {
            IntMatrixToken tem = (IntMatrixToken) IntMatrixToken.convert(token);
            int[][] result = tem.intMatrix();
            return new IntMatrixToken(result);
        }

        // The argument is below IntMatrixToken in the type hierarchy,
        // but I don't recognize it.
        throw new IllegalActionException("cannot convert from token " +
                "type: " + token.getClass().getName() + " to a " +
                "IntMatrixToken.");
    }

    /** Return the content of this token as a 2-D double matrix.
     *  @return A 2-D double matrix.
     */
    public final double[][] doubleMatrix() {
        return IntegerMatrixMath.toDoubleMatrix(_value);
    }

    /** Return true if the argument is an instnace of IntMatrixToken
     *  of the same dimensions and the corresponding elements of the matrices
     *  are equal.
     *  @param object An instance of Object.
     *  @return True if the argument is an instance of IntMatrixToken
     *   of the same dimensions and the corresponding elements of the
     *   matrices are equal.
     */
    public boolean equals(Object object) {
	// This test rules out instances of a subclass.
	if (object.getClass() != IntMatrixToken.class) {
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
    public final int getColumnCount() {
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
    public final Token getElementAsToken(final int row, final int column)
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
    public final int getElementAt(final int row, final int column) {
        return _value[row][column];
    }

    /** Return the number of rows in the matrix.
     *  @return The number of rows in the matrix.
     */
    public final int getRowCount() {
        return _rowCount;
    }

    /** Return the type of this token.
     *  @return BaseType.DOUBLE_MATRIX
     */
    public final Type getType() {
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
    public final int[][] intMatrix() {
        return IntegerMatrixMath.allocCopy(_value);
    }

    /** Test if the content of this token is equal to that of the specified
     *  token. These two tokens are equal only if the specified token
     *  is also a matrix token with the same dimension, and all the
     *  corresponding elements of the matrices are equal, and lossless
     *  conversion is possible from either this token to the specified
     *  one, or vice versa.
     *  @param token The token with which to test equality.
     *  @return A BooleanToken containing the result.
     *  @exception IllegalActionException If the specified token is
     *   not a matrix token, or lossless conversion is not possible.
     */
    public final BooleanToken isEqualTo(Token token)
            throws IllegalActionException {
        int compare = TypeLattice.compare(this, token);
        if ( !(token instanceof MatrixToken) ||
                compare == CPO.INCOMPARABLE) {
            throw new IllegalActionException("Cannot check equality " +
                    "between " + this.getClass().getName() + " and " +
                    token.getClass().getName());
        }

        if (((MatrixToken)token).getRowCount() != _rowCount ||
                ((MatrixToken)token).getColumnCount() != _columnCount) {
            return new BooleanToken(false);
        }

        if (compare == CPO.LOWER) {
            return token.isEqualTo(this);
        } else {
            // type of specified token <= IntMatrixToken
            IntMatrixToken tem = (IntMatrixToken) convert(token);

            return new BooleanToken(IntegerMatrixMath.within(_value,
                    tem._getInternalIntMatrix(), 0));
        }
    }

    /** Return the content of this token as a 2-D long matrix.
     *  @return A 2-D long matrix.
     */
    public final long[][] longMatrix() {
        return IntegerMatrixMath.toLongMatrix(_value);
    }

    /** Return a new token whose value is the product of this token
     *  and the argument. The type of the specified token
     *  must be such that either it can be converted to the type
     *  of this token, or the type of this token can be converted
     *  to the type of the specified token, without loss of
     *  information. The type of the returned token is one of the
     *  above two types that allows lossless conversion from the other.
     *  If the specified token is a matrix, its number of rows should
     *  be the same as this token's number of columns.
     *  @param token The token to add to this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the specified token is
     *   not of a type that can be added to this token.
     */
    public final Token multiply(final Token token)
            throws IllegalActionException {

        int compare = TypeLattice.compare(this, token);
        if (compare == CPO.INCOMPARABLE) {
            throw new IllegalActionException(
                    _notSupportedMessage("multiply", this, token));
        } else if (compare == CPO.LOWER) {
            return token.multiplyReverse(this);
        } else {
            // type of the specified token <= IntMatrixToken
            int[][] result = null;

            if (token instanceof ScalarToken) {
                int scalar = ((ScalarToken)token).intValue();
                result = IntegerMatrixMath.multiply(_value, scalar);
            } else {
                // the specified token is not a scalar.
                IntMatrixToken tem = (IntMatrixToken) this.convert(token);
                if (tem.getRowCount() != _columnCount) {
                    throw new IllegalActionException("Cannot multiply " +
                            "matrix with " + _columnCount +
                            " columns by a matrix with " +
                            tem.getRowCount() + " rows.");
                }

                result = IntegerMatrixMath.multiply(
                        _value, tem._getInternalIntMatrix());
            }
            return new IntMatrixToken(result, DO_NOT_COPY);
        }
    }

    /** Return a new token whose value is the product of this token
     *  and the argument. The type of the specified token must
     *  be lower than IntMatrixToken.
     *  @param token The token to multiply this Token by.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the type of the specified
     *   token is not lower than IntMatrixToken.
     */
    public final Token multiplyReverse(final Token token)
            throws IllegalActionException {
        int compare = TypeLattice.compare(this, token);
        if (! (compare == CPO.HIGHER)) {
            throw new IllegalActionException("The type of the specified "
                    + "token " + token.getClass().getName()
		    + " is not lower than "
                    + getClass().getName());
        }

        // Check if token is matrix. In that case we must convert t into a
        // IntMatrixToken because matrix multiplication is not
        // commutative.
        if (token instanceof ScalarToken) {
            // multiply is commutative on int matrices, for scalar types.
            return multiply(token);
        } else {
            // the specified token is not a scalar
            IntMatrixToken tem = (IntMatrixToken) this.convert(token);
            if (tem.getColumnCount() != _rowCount) {
                throw new IllegalActionException("Cannot multiply " +
                        "matrix with " + tem.getColumnCount() +
                        " columns by a matrix with " +
                        _rowCount + " rows.");
            }
            return new IntMatrixToken(IntegerMatrixMath.multiply(
                    tem._getInternalIntMatrix(), _value), DO_NOT_COPY);
        }
    }

    /** Return a new Token representing the left multiplicative
     *  identity. The returned token contains an identity matrix
     *  whose dimensions are the same as the number of rows of
     *  the matrix contained in this token.
     *  @return A new IntMatrixToken containing the left multiplicative
     *   identity.
     */
    public final Token one() {
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
    public final Token oneRight() {
	try {
            return new IntMatrixToken(IntegerMatrixMath.identity(_columnCount),
	                          DO_NOT_COPY);
	} catch (IllegalActionException illegalAction) {
	    // should not happen
	    throw new InternalErrorException("IntMatrixToken.oneRight: "
		    + "Cannot create identity matrix.");
	}
    }

    /** Return a new Token whose value is the value of the argument Token
     *  subtracted from the value of this Token. The type of the specified token
     *  must be such that either it can be converted to the type
     *  of this token, or the type of this token can be converted
     *  to the type of the specified token, without loss of
     *  information. The type of the returned token is one of the
     *  above two types that allows lossless conversion from the other.
     *  If the specified token is a matrix, its dimension must be the
     *  same as this token.
     *  @param token The token to subtract to this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the specified token is
     *   not of a type that can be added to this token.
     */
    public final Token subtract(final Token token)
            throws IllegalActionException {

        int compare = TypeLattice.compare(this, token);
        if (compare == CPO.INCOMPARABLE) {
            throw new IllegalActionException(
                    _notSupportedMessage("subtract", this, token));
        } else if (compare == CPO.LOWER) {
            Token me = token.convert(this);
            return me.subtract(token);
        } else {
            // type of the specified token <= IntMatrixToken
            int[][] result = null;

            if (token instanceof ScalarToken) {
                int scalar = ((ScalarToken)token).intValue();
                result = IntegerMatrixMath.add(_value, -scalar);
            } else {
                // the specified token is not a scalar.
                IntMatrixToken tem = (IntMatrixToken)this.convert(token);
                if (tem.getRowCount() != _rowCount ||
                        tem.getColumnCount() != _columnCount) {
                    throw new IllegalActionException("Cannot subtract two " +
                            "matrices with different dimensions.");
                }

                result = IntegerMatrixMath.subtract(_value,
                        tem._getInternalIntMatrix());
            }
            return new IntMatrixToken(result, DO_NOT_COPY);
        }
    }

    /** Return a new Token whose value is the value of this Token
     *  subtracted from the value of the argument Token.
     *  The type of the specified token must be lower than IntMatrixToken.
     *  @param token The token to add this Token to.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the type of the specified
     *   token is not lower than IntMatrixToken.
     */
    public final Token subtractReverse(final Token token)
            throws IllegalActionException {
        int compare = TypeLattice.compare(this, token);
        if (! (compare == CPO.HIGHER)) {
            throw new IllegalActionException("The type of the specified "
                    + "token " + token.getClass().getName()
		    + " is not lower than "
                    + getClass().getName());
        }
        // add the argument Token to the negative of this Token
        IntMatrixToken negativeToken =
            new IntMatrixToken(IntegerMatrixMath.negative(_value), DO_NOT_COPY);
        return negativeToken.add(token);
    }

    /** Return a new Token representing the additive identity.
     *  The returned token contains a matrix whose elements are
     *  all zero, and the size of the matrix is the same as the
     *  matrix contained in this token.
     *  @return A new IntMatrixToken containing the additive identity.
     */
    public final Token zero() {
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
    ////                      protected methods                    ////

    /** Return a reference to the internal 2-D matrix of ints that represents
     *  this Token. Because no copying is done, the contents must NOT be
     *  modified to preserve the immutability of Token.
     *  @return A 2-D int matrix.
     */
    protected int[][] _getInternalIntMatrix() {
        return _value;
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private methods                     ////

    // initialize the row and column count and copy the specified
    // matrix. This method is used by the constructors.
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
