/* A token that contains a 2-D double matrix.

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
@AcceptedRating Yellow (cxh@eecs.berkeley.edu)
*/

package ptolemy.data;
import ptolemy.kernel.util.*;
import ptolemy.graph.CPO;
import ptolemy.math.Complex;
import ptolemy.math.DoubleMatrixMath;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.data.type.TypeLattice;
import ptolemy.data.expr.PtParser;
import ptolemy.data.expr.ASTPtRootNode;

//////////////////////////////////////////////////////////////////////////
//// DoubleMatrixToken
/**
A token that contains a 2-D double matrix.

@author Yuhong Xiong, Jeff Tsay, Christopher Hylands
@version $Id$
@since Ptolemy II 0.2
*/
public class DoubleMatrixToken extends MatrixToken {

    /** Construct an DoubleMatrixToken with a one by one matrix. The
     *  only element in the matrix has value 0.0
     */
    public DoubleMatrixToken() {
        double[][] value = new double[1][1];
	value[0][0] = 0.0;
        _initialize(value, DO_NOT_COPY);
    }

    /** Construct a DoubleMatrixToken with the specified 2-D matrix.
     *  Make a copy of the matrix and store the copy,
     *  so that changes on the specified matrix after this token is
     *  constructed will not affect the content of this token.
     *  @exception IllegalActionException If the specified matrix
     *   is null.
     */
    public DoubleMatrixToken(final double[][] value)
            throws IllegalActionException {
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
     *  @exception IllegalActionException If the specified matrix
     *   is null.
     */
    public DoubleMatrixToken(final double[][] value, final int copy)
            throws IllegalActionException {
        if (value == null) {
	    throw new IllegalActionException("DoubleMatrixToken: The "
		    + "specified matrix is null.");
        }
        _initialize(value, copy);
    }

    /** Construct a DoubleMatrixToken from the specified string.
     *  @param init A string expression of a 2-D double matrix.
     *  @exception IllegalActionException If the string does
     *   not contain a parsable 2-D double matrix.
     */
    public DoubleMatrixToken(String init) throws IllegalActionException {
        PtParser parser = new PtParser();
        ASTPtRootNode tree = parser.generateParseTree(init);
	DoubleMatrixToken token = (DoubleMatrixToken)tree.evaluateParseTree();
        double[][] value = token.doubleMatrix();
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
            // type of the specified token <= DoubleMatrixToken
            double[][] result = null;

            if (token instanceof ScalarToken) {
                double scalar = ((ScalarToken)token).doubleValue();
                result = DoubleMatrixMath.add(_value, scalar);
            } else {
                // the specified token is not a scalar.
                DoubleMatrixToken tem = (DoubleMatrixToken)this.convert(token);
                if (tem.getRowCount() != _rowCount ||
                        tem.getColumnCount() != _columnCount) {
                    throw new IllegalActionException("Cannot add two " +
                            "matrices with different dimensions.");
                }

                result = DoubleMatrixMath.add(
                        tem._getInternalDoubleMatrix(), _value);
            }
            return new DoubleMatrixToken(result);
        }
    }

    /** Return a new token whose value is the sum of this token
     *  and the argument. The type of the specified token must
     *  be lower than DoubleMatrixToken.
     *  @param token The token to add this Token to.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the type of the specified
     *   token is not lower than DoubleMatrixToken.
     */
    public final Token addReverse(Token token) throws IllegalActionException {
        int compare = TypeLattice.compare(this, token);
        if (! (compare == CPO.HIGHER)) {
            throw new IllegalActionException("The type of the specified "
                    + "token " + token.getClass().getName()
		    + " is not lower than "
                    + getClass().getName());
        }
        // add is commutative on double matrix.
        return add(token);
    }

    /** Return the content of this token as a 2-D Complex matrix.
     *  @return A 2-D Complex matrix
     */
    public final Complex[][] complexMatrix() {
        return DoubleMatrixMath.toComplexMatrix(_value);
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
    public static final Token convert(Token token)
            throws IllegalActionException {
        int compare = TypeLattice.compare(BaseType.DOUBLE_MATRIX, token);
        if (compare == CPO.LOWER || compare == CPO.INCOMPARABLE) {
            throw new IllegalActionException("DoubleMatrixToken.convert: " +
                    "type of argument: " + token.getClass().getName() +
                    "is higher or incomparable with DoubleMatrixToken in the " +
                    "type hierarchy.");
        }

        if (token instanceof DoubleMatrixToken) {
            return token;
        }

        // try double
        compare = TypeLattice.compare(BaseType.DOUBLE, token);
        if (compare == CPO.SAME || compare == CPO.HIGHER) {
            DoubleToken tem = (DoubleToken) DoubleToken.convert(token);
            double[][] result = new double[1][1];
            result[0][0] = tem.doubleValue();
            return new DoubleMatrixToken(result);
        }

        // try IntMatrix
        compare = TypeLattice.compare(BaseType.DOUBLE_MATRIX, token);
        if (compare == CPO.SAME || compare == CPO.HIGHER) {
            IntMatrixToken tem = (IntMatrixToken) IntMatrixToken.convert(token);
            double[][] result = tem.doubleMatrix();
            return new DoubleMatrixToken(result);
        }

        // The argument is below DoubleMatrixToken in the type hierarchy,
        // but I don't recognize it.
        throw new IllegalActionException("cannot convert from token " +
                "type: " + token.getClass().getName() + " to a " +
                "DoubleMatrixToken.");
    }

    /** Return the content in the token as a 2-D double matrix.
     *  The returned matrix is a copy so the caller is free to
     *  modify it.
     *  @return A 2-D double matrix.
     */
    public final double[][] doubleMatrix() {
        return DoubleMatrixMath.allocCopy(_value);
    }

    /** Return true if the argument is an instnace of DoubleMatrixToken
     *  of the same dimensions and the corresponding elements of the matrices
     *  are equal.
     *  @param object An instance of Object.
     *  @return True if the argument is an instance of DoubleMatrixToken
     *   of the same dimensions and the corresponding elements of the
     *   matrices are equal.
     */
    public boolean equals(Object object) {
	// This test rules out instances of a subclass.
	if (object.getClass() != DoubleMatrixToken.class) {
	    return false;
	}

	DoubleMatrixToken matrixArgument = (DoubleMatrixToken)object;
        if (_rowCount != matrixArgument.getRowCount()) {
            return false;
	}
	if (_columnCount != matrixArgument.getColumnCount()) {
	    return false;
	}

	double[][] matrix = matrixArgument.doubleMatrix();
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
     *  row and column in a DoubleToken.
     *  @param row The row index of the desired element.
     *  @param column The column index of the desired element.
     *  @return A DoubleToken containing the matrix element.
     *  @exception ArrayIndexOutOfBoundsException If the specified
     *   row or column number is outside the range of the matrix.
     */
    public final Token getElementAsToken(final int row, final int column)
            throws ArrayIndexOutOfBoundsException {
        return new DoubleToken(_value[row][column]);
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
        return BaseType.DOUBLE_MATRIX;
    }

    /** Return a hash code value for this token. This method returns the
     *  integer portion of the sum of the elements.
     *  @return A hash code value for this token.
     */
    public int hashCode() {
	double code = 0.0;
	for (int i = 0; i < _rowCount; i++) {
	    for (int j = 0; j < _columnCount; j++) {
		code += _value[i][j];
	    }
	}

	return (int)code;
    }

    /** Test that each element of this Token is close to the
     *  corresponding element in the argument Token and that each
     *  element of this Token has the same units as the corresponding
     *  element in the argument Token.
     *  The value of the ptolemy.math.Complex epsilon field is
     *  used to determine whether the two Tokens are close.
     *
     *  <p> Two tokens are considered close only if the specified token
     *  is also a matrix token with the same dimension, and all the
     *  corresponding elements of the matrices are close, and lossless
     *  conversion is possible from either this token to the specified
     *  one, or vice versa, and the units of each of the corresponding
     *  elements are equal.
     *
     *  <p>If A and B are the values of elements of the tokens, and if
     *  the following is true:
     *  <pre>
     *  abs(A-B) < epsilon
     *  </pre>
     *  and the units of A and B are equal, then A and B are considered close.
     *
     *  @see ptolemy.math.Complex#epsilon
     *  @see #isEqualTo
     *  @param token The token to test closeness of this token with.
     *  @return a boolean token that contains the value true if the
     *   value of each element of this token is close to the
     *   value of corresponding element in the argument token and
     *   the units of each element of this token is the same as the units
     *   of the corresponding element in the argument token.
     *  @exception IllegalActionException If the argument token is
     *   not of a type that can be compared with this token.
     */
    public BooleanToken isCloseTo(Token token) throws IllegalActionException{
        return isCloseTo(token, ptolemy.math.Complex.epsilon);
    }

    /** Test that each element of this Token is close to the
     *  corresponding element in the argument Token and that each
     *  element of this Token has the same units as the corresponding
     *  element in the argument Token.
     *  The value of the epsilon argument is used to determine whether
     *  the two Tokens are close.
     *
     *  <p> Two tokens are considered close only if the specified token
     *  is also a matrix token with the same dimension, and all the
     *  corresponding elements of the matrices are close, and lossless
     *  conversion is possible from either this token to the specified
     *  one, or vice versa and the units of each of the corresponding
     *  elements are equal.
     *
     *  <p>If A and B are the values of elements of the tokens, and if
     *  the following is true:
     *  <pre>
     *  abs(A-B) < epsilon
     *  </pre>
     *  and the units of A and B are equal, then A and B are considered close.
     *
     *  @see #isEqualTo
     *  @param token The token to test closeness of this token with.
     *  @param epsilon The value that we use to determine whether two
     *  tokens are close.
     *  @return a boolean token that contains the value true if the
     *   value of each element of this token is close to the
     *   value of corresponding element in the argument token and
     *   the units of each element of this token is the same as the units
     *   of the corresponding element in the argument token.
     *  @exception IllegalActionException If the argument token is
     *   not of a type that can be compared with this token.
     */
    public BooleanToken isCloseTo(Token token,
            double epsilon)
            throws IllegalActionException {
        int compare = TypeLattice.compare(this, token);
        if ( !(token instanceof MatrixToken) || compare == CPO.INCOMPARABLE) {
            throw new IllegalActionException("Cannot check equality " +
                    "between " + this.getClass().getName() + " and " +
                    token.getClass().getName());
        }

        if (((MatrixToken)token).getRowCount() != _rowCount ||
                ((MatrixToken)token).getColumnCount() != _columnCount) {
            return new BooleanToken(false);
        }

        if (compare == CPO.LOWER) {
            return token.isCloseTo(this, epsilon);
        } else {
            // type of specified token <= DoubleMatrixToken
            DoubleMatrixToken tem = (DoubleMatrixToken)convert(token);

            return new BooleanToken(DoubleMatrixMath.within(_value,
                    tem._getInternalDoubleMatrix(), epsilon));
        }
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
        if ( !(token instanceof MatrixToken) || compare == CPO.INCOMPARABLE) {
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
            // type of specified token <= DoubleMatrixToken
            DoubleMatrixToken tem = (DoubleMatrixToken)convert(token);

            return new BooleanToken(DoubleMatrixMath.within(_value,
                    tem._getInternalDoubleMatrix(), 0.0));
        }
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
            // type of the specified token <= DoubleMatrixToken
            double[][] result = null;

            if (token instanceof ScalarToken) {
                double scalar = ((ScalarToken)token).doubleValue();
                result = DoubleMatrixMath.multiply(_value, scalar);
            } else {
                // the specified token is not a scalar.
                DoubleMatrixToken tem = (DoubleMatrixToken)convert(token);
                if (tem.getRowCount() != _columnCount) {
                    throw new IllegalActionException("Cannot multiply " +
                            "matrix with " + _columnCount +
                            " columns by a matrix with " +
                            tem.getRowCount() + " rows.");
                }

                result = DoubleMatrixMath.multiply(
                        _value, tem._getInternalDoubleMatrix());
            }
            return new DoubleMatrixToken(result, DO_NOT_COPY);
        }
    }

    /** Return a new token whose value is the product of this token
     *  and the argument. The type of the specified token must
     *  be lower than DoubleMatrixToken.
     *  @param token The token to multiply this Token by.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the type of the specified
     *   token is not lower than DoubleMatrixToken.
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

        // Check if t is matrix. In that case we must convert t into a
        // DoubleMatrixToken because matrix multiplication is not
        // commutative.
        if (token instanceof ScalarToken) {
            // multiply is commutative on double matrices, for scalar types.
            return multiply(token);
        } else {
            // the specified token is not a scalar
            DoubleMatrixToken tem = (DoubleMatrixToken) this.convert(token);
            if (tem.getColumnCount() != _rowCount) {
                throw new IllegalActionException("Cannot multiply " +
                        "matrix with " + tem.getColumnCount() +
                        " columns by a matrix with " +
                        _rowCount + " rows.");
            }
            return new DoubleMatrixToken(DoubleMatrixMath.multiply(
                    tem._getInternalDoubleMatrix(), _value), DO_NOT_COPY);
        }
    }

    /** Return a new Token representing the left multiplicative
     *  identity. The returned token contains an identity matrix
     *  whose dimensions are the same as the number of rows of
     *  the matrix contained in this token.
     *  @return A new DoubleMatrixToken containing the left multiplicative
     *   identity.
     */
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
            // type of the specified token <= DoubleMatrixToken
            double[][] result = null;

            if (token instanceof ScalarToken) {
                double scalar = ((ScalarToken)token).doubleValue();
                result = DoubleMatrixMath.add(_value, -scalar);
            } else {
                // the specified token is not a scalar.
                DoubleMatrixToken tem = (DoubleMatrixToken)this.convert(token);
                if (tem.getRowCount() != _rowCount ||
                        tem.getColumnCount() != _columnCount) {
                    throw new IllegalActionException("Cannot subtract two " +
                            "matrices with different dimensions.");
                }

                result = DoubleMatrixMath.subtract(_value,
                        tem._getInternalDoubleMatrix());
            }
            return new DoubleMatrixToken(result, DO_NOT_COPY);
        }
    }

    /** Return a new Token whose value is the value of this Token
     *  subtracted from the value of the argument Token.
     *  The type of the specified token must be lower than DoubleMatrixToken.
     *  @param token The token to add this Token to.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the type of the specified
     *   token is not lower than DoubleMatrixToken.
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
        DoubleMatrixToken negativeToken =
            new DoubleMatrixToken(DoubleMatrixMath.negative(_value),
                    DO_NOT_COPY);
        return negativeToken.add(token);
    }

    /** Return a new Token representing the additive identity.
     *  The returned token contains a matrix whose elements are
     *  all zero, and the size of the matrix is the same as the
     *  matrix contained in this token.
     *  @return A new DoubleMatrixToken containing the additive identity.
     */
    public final Token zero() {
	try {
            return new DoubleMatrixToken(new double[_rowCount][_columnCount],
                    DO_NOT_COPY);
        } catch (IllegalActionException illegalAction) {
	    // should not happen
	    throw new InternalErrorException("DoubleMatrixToken.zero: "
		    + "Cannot create zero matrix.");
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a reference to the internal 2-D matrix of doubles that
     *  represents this Token. Because no copying is done, the contents
     *  must NOT be modified to preserve the immutability of Token.
     *  @return A 2-D double matrix.
     */
    protected double[][] _getInternalDoubleMatrix() {
        return _value;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // initialize the row and column count and copy the specified
    // matrix. This method is used by the constructors.
    private void _initialize(double[][] value, int copy) {
        _rowCount = value.length;
        _columnCount = value[0].length;

        if (copy == DO_NOT_COPY) {
            _value = value;
        } else {
            _value = DoubleMatrixMath.allocCopy(value);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private double[][] _value;
    private int _rowCount;
    private int _columnCount;
}
