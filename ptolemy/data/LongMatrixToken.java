/* A token that contains a 2-D long matrix.

 Copyright (c) 1998-2001 The Regents of the University of California.
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
import ptolemy.math.LongMatrixMath;
import ptolemy.data.type.*;
import ptolemy.data.expr.PtParser;
import ptolemy.data.expr.ASTPtRootNode;

//////////////////////////////////////////////////////////////////////////
//// LongMatrixToken
/**
A token that contains a 2-D long matrix.

@author Yuhong Xiong
@version $Id$
*/
public class LongMatrixToken extends MatrixToken {

    /** Construct an LongMatrixToken with a one by one matrix. The
     *  only element in the matrix has value 0
     */
    public LongMatrixToken() {
	_rowCount = 1;
	_columnCount = 1;
	_value = new long[1][1];
	_value[0][0] = 0;
    }

    /** Construct a LongMatrixToken with the specified 2-D matrix.
     *  This method makes a copy of the matrix and stores the copy,
     *  so changes on the specified matrix after this token is
     *  constructed will not affect the content of this token.
     *  @exception IllegalActionException If the specified matrix
     *   is null.
     */
    public LongMatrixToken(long[][] value) throws IllegalActionException {
        this(value, DO_COPY);
    }

    /** Construct a LongMatrixToken with the specified 2-D matrix.
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
    protected LongMatrixToken(final long[][] value, final int copy)
            throws IllegalActionException {
        if (value == null) {
	    throw new IllegalActionException("LongMatrixToken: The "
		    + "specified matrix is null.");
        }
        _initialize(value, copy);
    }

    /** Construct a LongMatrixToken from the specified string.
     *  @param init A string expression of a 2-D long matrix.
     *  @exception IllegalActionException If the string does
     *   not contain a parsable 2-D long matrix.
     */
    public LongMatrixToken(String init) throws IllegalActionException {
        PtParser parser = new PtParser();
        ASTPtRootNode tree = parser.generateParseTree(init);
	LongMatrixToken token = (LongMatrixToken)tree.evaluateParseTree();
        long[][] value = token.longMatrix();
        _initialize(value, DO_NOT_COPY);
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
    public Token add(Token token) throws IllegalActionException {
	int compare = TypeLattice.compare(this, token);
	if (compare == CPO.INCOMPARABLE) {
	    throw new IllegalActionException(
                    _notSupportedMessage("add", this, token));
	} else if (compare == CPO.LOWER) {
	    return token.addReverse(this);
	} else {
	    // type of the specified token <= LongMatrixToken
	    long[][] result = null;

	    if (token instanceof ScalarToken) {
		long scalar = ((ScalarToken)token).longValue();
		result = LongMatrixMath.add(_value, scalar);
	    } else {
		// the specified token is not a scalar.
		LongMatrixToken tem = (LongMatrixToken)this.convert(token);
	    	if (tem.getRowCount() != _rowCount ||
                        tem.getColumnCount() != _columnCount) {
                    throw new IllegalActionException("Cannot add two " +
                            "matrices with different dimensions.");
	    	}

		result = LongMatrixMath.add(_value,
                        tem._getInternalLongMatrix());
	    }
	    return new LongMatrixToken(result, DO_NOT_COPY);
	}
    }

    /** Return a new token whose value is the sum of this token
     *  and the argument. The type of the specified token must
     *  be lower than LongMatrixToken.
     *  @param token The token to add this Token to.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the type of the specified
     *   token is not lower than LongMatrixToken.
     */
    public Token addReverse(Token token) throws IllegalActionException {
	int compare = TypeLattice.compare(this, token);
	if (! (compare == CPO.HIGHER)) {
	    throw new IllegalActionException("The type of the specified "
                    + "token " + token.getClass().getName()
		    + " is not lower than "
                    + getClass().getName());
	}
	// add is commutative on long matrix.
	return add(token);
    }

    /** Convert the specified token into an instance of LongMatrixToken.
     *  This method does lossless conversion.
     *  If the argument is already an instance of LongMatrixToken,
     *  it is returned without any change. Otherwise, if the argument
     *  is below LongMatrixToken in the type hierarchy, it is converted to
     *  an instance of LongMatrixToken or one of the subclasses of
     *  LongMatrixToken and returned. If none of the above condition is
     *  met, an exception is thrown.
     *  @param token The token to be converted to a LongMatrixToken.
     *  @return A LongMatrixToken
     *  @exception IllegalActionException If the conversion cannot
     *   be carried out.
     */
    public static Token convert(Token token)
	    throws IllegalActionException {
	int compare = TypeLattice.compare(BaseType.LONG_MATRIX, token);
	if (compare == CPO.LOWER || compare == CPO.INCOMPARABLE) {
	    throw new IllegalActionException("LongMatrixToken.convert: " +
                    "type of argument: " + token.getClass().getName() +
                    "is higher or incomparable with LongMatrixToken in the " +
                    "type hierarchy.");
	}

	if (token instanceof LongMatrixToken) {
	    return token;
	}

	// try long
	compare = TypeLattice.compare(BaseType.LONG, token);
	if (compare == CPO.SAME || compare == CPO.HIGHER) {
	    LongToken tem = (LongToken)LongToken.convert(token);
	    long[][] result = new long[1][1];
	    result[0][0] = tem.longValue();
	    return new LongMatrixToken(result);
	}

	// try IntMatrix
	compare = TypeLattice.compare(BaseType.INT_MATRIX, token);
	if (compare == CPO.SAME || compare == CPO.HIGHER) {
	    IntMatrixToken tem = (IntMatrixToken)IntMatrixToken.convert(token);
	    long[][] result = tem.longMatrix();
	    return new LongMatrixToken(result);
	}

	// The argument is below LongMatrixToken in the type hierarchy,
        // but I don't recognize it.
        throw new IllegalActionException("cannot convert from token " +
                "type: " + token.getClass().getName() + " to a " +
		"LongMatrixToken.");
    }

    /** Return true if the argument is an instnace of LongMatrixToken
     *  of the same dimensions and the corresponding elements of the matrices
     *  are equal.
     *  @param object An instance of Object.
     *  @return True if the argument is an instance of LongMatrixToken
     *   of the same dimensions and the corresponding elements of the
     *   matrices are equal.
     */
    public boolean equals(Object object) {
	// This test rules out instances of a subclass.
	if (object.getClass() != LongMatrixToken.class) {
	    return false;
	}

	LongMatrixToken matrixArgument = (LongMatrixToken)object;
        if (_rowCount != matrixArgument.getRowCount()) {
            return false;
	}
	if (_columnCount != matrixArgument.getColumnCount()) {
	    return false;
	}

	long[][] matrix = matrixArgument.longMatrix();
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
     *  row and column in a LongToken.
     *  @param row The row index of the desired element.
     *  @param column The column index of the desired element.
     *  @return A LongToken containing the matrix element.
     *  @exception ArrayIndexOutOfBoundsException If the specified
     *   row or column number is outside the range of the matrix.
     */
    public Token getElementAsToken(int row, int column)
            throws ArrayIndexOutOfBoundsException {
	return new LongToken(_value[row][column]);
    }

    /** Return the element of the contained matrix at the specified
     *  row and column.
     *  @param row The row index of the desired element.
     *  @param column The column index of the desired element.
     *  @return The long at the specified matrix entry.
     *  @exception ArrayIndexOutOfBoundsException If the specified
     *   row or column number is outside the range of the matrix.
     */
    public long getElementAt(int row, int column) {
        return _value[row][column];
    }

    /** Return the number of rows in the matrix.
     *  @return The number of rows in the matrix.
     */
    public int getRowCount() {
	return _rowCount;
    }

    /** Return the type of this token.
     *  @return BaseType.LONG_MATRIX
     */
    public Type getType() {
	return BaseType.LONG_MATRIX;
    }

    /** Return a hash code value for this token. This method returns the
     *  sum of the elements, casted to integer.
     *  @return A hash code value for this token.
     */
    public int hashCode() {
	long code = 0;
	for (int i = 0; i < _rowCount; i++) {
	    for (int j = 0; j < _columnCount; j++) {
		code += _value[i][j];
	    }
	}

	return (int)code;
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
    public BooleanToken isEqualTo(Token token) throws IllegalActionException {
	int compare = TypeLattice.compare(this, token);
	if ( !(token instanceof MatrixToken) ||
                compare == CPO.INCOMPARABLE) {
	    throw new IllegalActionException("Cannot check equality " +
                    "between " + this.getClass().getName() + " and " +
                    token.getClass().getName());
	}

	if ( ((MatrixToken)token).getRowCount() != _rowCount ||
                ((MatrixToken)token).getColumnCount() != _columnCount) {
	    return new BooleanToken(false);
	}

	if (compare == CPO.LOWER) {
	    return token.isEqualTo(this);
	} else {
	    // type of specified token <= LongMatrixToken
	    LongMatrixToken tem = (LongMatrixToken)convert(token);
	    long[][] matrix = tem.longMatrix();

	    for (int i = 0; i < _rowCount; i++) {
		for (int j = 0; j < _columnCount; j++) {
		    if (_value[i][j] != matrix[i][j]) {
			return new BooleanToken(false);
		    }
		}
	    }
	    return new BooleanToken(true);
	}
    }

    /** Return the content in the token as a 2-D long matrix.
     *  The returned matrix is a copy so the caller is free to
     *  modify it.
     *  @return A 2-D long matrix.
     */
    public long[][] longMatrix() {
	long[][] matrix = new long[_rowCount][_columnCount];
	for (int i = 0; i < _rowCount; i++) {
	    for (int j = 0; j < _columnCount; j++) {
	 	matrix[i][j] = _value[i][j];
	    }
	}
	return matrix;
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
            // type of the specified token <= LongMatrixToken
            long[][] result = null;

            if (token instanceof ScalarToken) {
                long scalar = ((ScalarToken)token).longValue();
                result = LongMatrixMath.multiply(_value, scalar);
            } else {
                // the specified token is not a scalar.
                LongMatrixToken tem = (LongMatrixToken)convert(token);
                if (tem.getRowCount() != _columnCount) {
                    throw new IllegalActionException("Cannot multiply " +
                            "matrix with " + _columnCount +
                            " columns by a matrix with " +
                            tem.getRowCount() + " rows.");
                }

                result = LongMatrixMath.multiply(
                        _value, tem._getInternalLongMatrix());
            }
            return new LongMatrixToken(result, DO_NOT_COPY);
        }
    }

    /** Return a new token whose value is the product of this token
     *  and the argument. The type of the specified token must
     *  be lower than LongMatrixToken.
     *  @param token The token to multiply this Token by.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the type of the specified
     *   token is not lower than LongMatrixToken.
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
        // LongMatrixToken because matrix multiplication is not
        // commutative.
        if (token instanceof ScalarToken) {
            // multiply is commutative on long matrices, for scalar types.
            return multiply(token);
        } else {
            // the specified token is not a scalar
            LongMatrixToken tem = (LongMatrixToken) this.convert(token);
            if (tem.getColumnCount() != _rowCount) {
                throw new IllegalActionException("Cannot multiply " +
                        "matrix with " + tem.getColumnCount() +
                        " columns by a matrix with " +
                        _rowCount + " rows.");
            }
            return new LongMatrixToken(LongMatrixMath.multiply(
                    tem._getInternalLongMatrix(), _value), DO_NOT_COPY);
        }
    }

   /** Return a new Token representing the left multiplicative
     *  identity. The returned token contains an identity matrix
     *  whose dimensions are the same as the number of rows of
     *  the matrix contained in this token.
     *  @return A new LongMatrixToken containing the left multiplicative
     *   identity.
     */
    public Token one() {
	long[][] result = new long[_rowCount][_rowCount];
	for (int i = 0; i < _rowCount; i++) {
	    for (int j = 0; j < _rowCount; j++) {
		result[i][j] = 0;
	    }
	    result[i][i] = 1;
	}
	try {
	    return new LongMatrixToken(result);
	} catch (IllegalActionException illegalAction) {
            // should not happen
	    throw new InternalErrorException("LongMatrixToken.one: "
		    + "Cannot create identity matrix.");
	}
    }

    /** Return a new Token representing the right multiplicative
     *  identity. The returned token contains an identity matrix
     *  whose dimensions are the same as the number of columns of
     *  the matrix contained in this token.
     *  @return A new LongMatrixToken containing the right multiplicative
     *   identity.
     */
    public Token oneRight() {
	long[][] result = new long[_columnCount][_columnCount];
	for (int i = 0; i < _columnCount; i++) {
	    for (int j = 0; j < _columnCount; j++) {
		result[i][j] = 0;
	    }
	    result[i][i] = 1;
	}

	try {
	    return new LongMatrixToken(result);
	} catch (IllegalActionException illegalAction) {
            // should not happen
	    throw new InternalErrorException("LongMatrixToken.oneRight: "
		    + "Cannot create identity matrix.");
	}
    }


    /** Return a new Token whose value is the value of the argument Token
     *  subtracted from the value of this Token.
     *  The type of the specified token
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
            // type of the specified token <= LongMatrixToken
            long[][] result = null;

            if (token instanceof ScalarToken) {
                long scalar = ((ScalarToken)token).longValue();
                result = LongMatrixMath.add(_value, -scalar);
            } else {
                // the specified token is not a scalar.
                LongMatrixToken tem = (LongMatrixToken)this.convert(token);
                if (tem.getRowCount() != _rowCount ||
                        tem.getColumnCount() != _columnCount) {
                    throw new IllegalActionException("Cannot subtract two " +
                            "matrices with different dimensions.");
                }

                result = LongMatrixMath.subtract(_value,
                        tem._getInternalLongMatrix());
            }
            return new LongMatrixToken(result, DO_NOT_COPY);
        }
    }

    /** Return a new Token whose value is the value of this Token
     *  subtracted from the value of the argument Token.
     *  The type of the specified token must be lower than LongMatrixToken.
     *  @param token The token to add this Token to.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the type of the specified
     *   token is not lower than LongMatrixToken.
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
        LongMatrixToken negativeToken =
            new LongMatrixToken(LongMatrixMath.negative(_value),
	                          DO_NOT_COPY);
        return negativeToken.add(token);
    }

    /** Return a new Token representing the additive identity.
     *  The returned token contains a matrix whose elements are
     *  all zero, and the size of the matrix is the same as the
     *  matrix contained in this token.
     *  @return A new LongMatrixToken containing the additive identity.
     */
    public Token zero() {
	long[][] result = new long[_rowCount][_columnCount];
	for (int i = 0; i < _rowCount; i++) {
	    for (int j = 0; j < _columnCount; j++) {
		result[i][j] = 0;
	    }
	}

	try {
	    return new LongMatrixToken(result);
	} catch (IllegalActionException illegalAction) {
            // should not happen
	    throw new InternalErrorException("LongMatrixToken.zero: "
		    + "Cannot create zero matrix.");
	}
    }

   ///////////////////////////////////////////////////////////////////
    ////                      protected methods                    ////

    /** Return a reference to the internal 2-D matrix of longs that
     *  represents this Token. Because no copying is done, the contents
     *  must NOT be modified to preserve the immutability of Token.
     *  @return A 2-D long matrix.
     */
    protected long[][] _getInternalLongMatrix() {
        return _value;
    }

    ///////////////////////////////////////////////////////////////////
    ////                          private methods                  ////

    // initialize the row and column count and copy the specified
    // matrix. This method is used by the constructors.
    private void _initialize(long[][] value, int copy) {
	_rowCount = value.length;
	_columnCount = value[0].length;
	  if (copy == DO_NOT_COPY) {
            _value = value;
        } else {
            _value = LongMatrixMath.allocCopy(value);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private long[][] _value = null;
    private int _rowCount = 0;
    private int _columnCount = 0;
}
