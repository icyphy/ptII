/* A token that contains a 2-D FixPoint array.

 Copyright (c) 1998-1999 The Regents of the University of California.
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
@ProposedRating Red
@AcceptedRating Red
*/

package ptolemy.data;

import ptolemy.kernel.util.*;
import ptolemy.graph.CPO;
import ptolemy.math.FixPoint;
import ptolemy.math.Quantizer;
import ptolemy.math.Precision;
import ptolemy.data.type.*;

//////////////////////////////////////////////////////////////////////////
//// FixMatrixToken
/**
A token that contains a 2-D FixToken array.

FIXME: Except add() and addReverse(), other arithmetics operations are
not implemented yet. Those methods will be added after the corresponding
operations are added to the math package.

@author Bart Kienhuis
@version $Id$
@see ptolemy.math.FixPoint
*/
public class FixMatrixToken extends MatrixToken {

    /** Construct an FixMatrixToken with a one by one array. The only
     *  element in the array has value 0.0
     */
    public FixMatrixToken() {
	_rowCount = 1;
	_columnCount = 1;
        _precision = new Precision(32, 32);
	_value = new FixPoint[1][1];
	_value[0][0] = Quantizer.round( 0.0, _precision );
    }

    /** Construct a FixMatrixToken with the specified 2-D array.
     *  This method makes a copy of the array and stores the copy,
     *  so changes on the specified array after this token is
     *  constructed will not affect the content of this token.
     *  @exception NullPointerException If the specified array
     *   is null.
     */
    public FixMatrixToken(FixPoint[][] value ) {
	_rowCount = value.length;
	_columnCount = value[0].length;
	_value = new FixPoint[_rowCount][_columnCount];
	for (int i = 0; i < _rowCount; i++) {
	    for (int j = 0; j < _columnCount; j++) {
		_value[i][j] = value[i][j];
	    }
	}
    }

    // FIXME: finish this method after array is added to the
    // 	      expression language.
    // Construct an FixMatrixToken from the specified string.
    // @param init A string expression of a 2-D double array.
    // @exception IllegalArgumentException If the string does
    //  not contain a parsable 2-D int array.
    //
    // public FixMatrixToken(String init) {
    // }

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
     *  @param t The token to add to this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the specified token is
     *   not of a type that can be added to this token in a lossless
     *   fashion.
     */
    public Token add(Token t)
	    throws IllegalActionException {

	int compare = TypeLattice.compare(this, t);
	if (compare == CPO.INCOMPARABLE) {
	    String msg = "add method not supported between " +
                this.getClass().getName() + " and " +
                t.getClass().getName();
	    throw new IllegalActionException(msg);
	} else if (compare == CPO.LOWER) {
	    return t.addReverse(this);
	} else {
	    // type of the specified token <= FixMatrixToken
	    FixPoint[][] result = null;

	    if (t instanceof ScalarToken) {
		FixPoint scalar = ((ScalarToken)t).fixValue();
		result = new FixPoint[_rowCount][_columnCount];
		for (int i = 0; i < _rowCount; i++) {
		    for (int j = 0; j < _columnCount; j++) {
			result[i][j] = scalar.add(_value[i][j]);
		    }
		}
	    } else {
		// the specified token is not a scalar.
		FixMatrixToken tem = (FixMatrixToken)convert(t);

	    	if (tem.getRowCount() != _rowCount ||
                        tem.getColumnCount() != _columnCount) {
                    throw new IllegalActionException("Cannot add two " +
                            "matrices with different dimension.");
	    	}

		result = tem.fixMatrix();
		for (int i = 0; i < _rowCount; i++) {
		    for (int j = 0; j < _columnCount; j++) {
			result[i][j] = result[i][j].add(_value[i][j]);
		    }
		}
	    }
	    return new FixMatrixToken(result);
	}
    }

    /** Return a new token whose value is the sum of this token
     *  and the argument. The type of the specified token must
     *  be lower than FixMatrixToken.
     *  @param t The token to add this Token to.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the type of the specified
     *   token is not lower than FixMatrixToken.
     */
    public Token addReverse(Token t)
	    throws IllegalActionException {
	int compare = TypeLattice.compare(this, t);
	if (! (compare == CPO.HIGHER)) {
	    throw new IllegalActionException("The type of the specified "
                    + "token " + t.getClass().getName() + " is not lower than "
                    + getClass().getName());
	}
	// add is commutative on FixPoint matrix.
	return add(t);
    }

    /** Return the content of this token as a new 2-D FixPoint array.
     *  @return A 2-D FixPoint matrix
     */
    public FixPoint[][] fixMatrix() {
	FixPoint[][] array = new FixPoint[_rowCount][_columnCount];
	for (int i = 0; i < _rowCount; i++) {
	    for (int j = 0; j < _columnCount; j++) {
                // FixPoint is immutable, so no need to copy.
		array[i][j] = _value[i][j];
	    }
	}
	return array;
    }

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
     *   be carried out in a lossless fashion.
     */
    public static Token convert(Token token)
	    throws IllegalActionException {

	int compare = TypeLattice.compare( new FixMatrixToken(), token);
	if (compare == CPO.LOWER || compare == CPO.INCOMPARABLE) {
	    throw new IllegalActionException("FixMatrixToken.convert: " +
                    "type of argument: " + token.getClass().getName() +
                    "is higher or incomparable with FixMatrixToken " +
                    "in the type hierarchy.");
	}

	if (token instanceof FixMatrixToken) {
	    return token;
	}

	// try Fix
	compare = TypeLattice.compare(new FixToken(0.0, 32, 32), token);
	if (compare == CPO.SAME || compare == CPO.HIGHER) {
	    FixPoint[][] result = new FixPoint[1][1];
	    FixToken tem = (FixToken)FixToken.convert(token);
	    result[0][0] = tem.fixValue();
	    return new FixMatrixToken(result);
	}

	// The argument is below FixMatrixToken in the type hierarchy,
        // but I don't recognize it.
        throw new IllegalActionException("cannot convert from token " +
                "type: " + token.getClass().getName() + " to a " +
		"FixMatrixToken.");
    }

    /** Return the type of this token.
     *  @return BaseType.FIX_MATRIX
     */
    public Type getType() {
	return BaseType.FIX_MATRIX;
    }

    /** Test if the content of this token is equal to that of the specified
     *  token. These two tokens are equal only if the specified token
     *  is also a matrix token with the same dimension, and all the
     *  corresponding elements of the arrays are equal, and lossless
     *  conversion is possible from either this token to the specified
     *  one, or vice versa.
     *  @param t The token with which to test equality.
     *  @return A booleanToken containing the result.
     *  @exception IllegalActionException If the specified token is
     *   not a matrix token; or lossless conversion is not possible.
     */
    public BooleanToken isEqualTo(Token t)
	    throws IllegalActionException {
	int compare = TypeLattice.compare(this, t);
	if ( !(t instanceof MatrixToken) ||
                compare == CPO.INCOMPARABLE) {
	    throw new IllegalActionException("Cannot check equality " +
                    "between " + this.getClass().getName() + " and " +
                    t.getClass().getName());
	}

	if ( ((MatrixToken)t).getRowCount() != _rowCount ||
                ((MatrixToken)t).getColumnCount() != _columnCount) {
	    return new BooleanToken(false);
	}

	if (compare == CPO.LOWER) {
	    return t.isEqualTo(this);
	} else {
	    // type of specified token <= FixMatrixToken
	    FixMatrixToken tem = (FixMatrixToken)convert(t);
	    FixPoint[][] array = tem.fixMatrix();

	    for (int i = 0; i < _rowCount; i++) {
		for (int j = 0; j < _columnCount; j++) {
		    if (!_value[i][j].equals(array[i][j])) {
			return new BooleanToken(false);
		    }
		}
	    }
	    return new BooleanToken(true);
	}
    }

    /** Return the element of the matrix at the specified
     *  row and column wrapped in a token.
     *  @param row The row index of the desired element.
     *  @param column The column index of the desired element.
     *  @return A FixToken containing the matrix element.
     *  @exception ArrayIndexOutOfBoundsException If the specified
     *   row or column number is outside the corresponding range
     *   of the index of the contained array.
     */
    public Token getElementAsToken(int row, int column)
            throws ArrayIndexOutOfBoundsException {
	return new FixToken(_value[row][column]);
    }

    /** Return the element of the contained array at the specified
     *  row and column.
     *  @param row The row index of the desired element.
     *  @param column The column index of the desired element.
     *  @return The FixPoint at the specified array entry.
     *  @exception ArrayIndexOutOfBoundsException If the specified
     *   row or column number is outside the corresponding range
     *   of the index of the contained array.
     */
    public FixPoint getElementAt(int row, int column) {
        return _value[row][column];
    }

    /** Return the number of columns in the matrix.
     *  @return An integer.
     */
    public int getColumnCount() {
	return _columnCount;
    }

    /** Return the number of rows in the matrix.
     *  @return An integer.
     */
    public int getRowCount() {
	return _rowCount;
    }

    /** Return a new Token representing the left multiplicative
     *  identity. The returned token contains an identity matrix
     *  whose dimension is the same as the number of rows of
     *  the matrix contained in this token.
     *  @return A new Token containing the left multiplicative identity.
     */
    public Token one() {
	FixPoint[][] result = new FixPoint[_rowCount][_rowCount];
	for (int i = 0; i < _rowCount; i++) {
	    for (int j = 0; j < _rowCount; j++) {
		result[i][j] = Quantizer.round( 0.0, _precision );
	    }
	    result[i][i] = Quantizer.round( 1.0, _precision );
	}
	return new FixMatrixToken(result);
    }

    /** Return a new Token representing the right multiplicative
     *  identity. The returned token contains an identity matrix
     *  whose dimension is the same as the number of columns of
     *  the matrix contained in this token.
     *  @return A new Token containing the right multiplicative identity.
     */
    public Token oneRight() {
	FixPoint[][] result = new FixPoint[_columnCount][_columnCount];
	for (int i = 0; i < _columnCount; i++) {
	    for (int j = 0; j < _columnCount; j++) {
		result[i][j] = Quantizer.round( 0.0, _precision );
	    }
	    result[i][i] = Quantizer.round( 0.0, _precision);
	}
	return new FixMatrixToken(result);
    }

    /** Return a new Token representing the additive identity.
     *  The returned token contains a matrix whose elements are
     *  all zero, and the size of the matrix is the same as the
     *  matrix contained in this token.
     *  @return A new Token containing the additive identity.
     */
    public Token zero() {
	FixPoint[][] result = new FixPoint[_rowCount][_columnCount];
	FixPoint zero = Quantizer.round( 0.0, _precision);
	for (int i = 0; i < _rowCount; i++) {
	    for (int j = 0; j < _columnCount; j++) {
		result[i][j] = zero;
	    }
	}
	return new FixMatrixToken(result);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private FixPoint[][] _value = null;
    private Precision _precision = null;
    private int _rowCount = 0;
    private int _columnCount = 0;
}
