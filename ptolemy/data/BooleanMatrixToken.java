/* A token that contains a 2-D boolean array.

 Copyright (c) 1998-2000 The Regents of the University of California.
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
import ptolemy.data.type.*;

//////////////////////////////////////////////////////////////////////////
//// BooleanMatrixToken
/**
A token that contains a 2-D boolean array.

@author Yuhong Xiong
@version $Id$
*/
public class BooleanMatrixToken extends MatrixToken {

    /** Construct an BooleanMatrixToken with a one by one array. The
     *  only element in the array has value false.
     */
    public BooleanMatrixToken() {
	_rowCount = 1;
	_columnCount = 1;
	_value = new boolean[1][1];
	_value[0][0] = false;
    }

    /** Construct a BooleanMatrixToken with the specified 2-D array.
     *  This method makes a copy of the array and stores the copy,
     *  so changes on the specified array after this token is
     *  constructed will not affect the content of this token.
     *  @exception NullPointerException If the specified array
     *   is null.
     */
    public BooleanMatrixToken(boolean[][] value) {
	_rowCount = value.length;
	_columnCount = value[0].length;
	_value = new boolean[_rowCount][_columnCount];
	for (int i = 0; i < _rowCount; i++) {
	    for (int j = 0; j < _columnCount; j++) {
		_value[i][j] = value[i][j];
	    }
	}
    }

    // FIXME: finish this method after array is added to the
    // 	      expression language.
    // Construct an BooleanMatrixToken from the specified string.
    // @param init A string expression of a 2-D boolean array.
    // @exception IllegalArgumentException If the string does
    //  not contain a parsable 2-D int array.
    //
    // public BooleanMatrixToken(String init) {
    // }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a new token whose value is the sum of this token
     *  and the argument. A BooleanMatrixToken can only have a
     *  StringToken added to it. Note that this means adding with
     *  a BooleanMatrixTokens or a BooleanToken will trigger an
     *  exception.
     *  @param t The token to add to this token.
     *  @return A new token.
     *  @exception IllegalActionException If the specified token is
     *   not of a type that can be added to this token in a lossless
     *   fashion.
     */
    public Token add(Token t)
	    throws IllegalActionException {
	int compare = TypeLattice.compare(this, t);
	if (compare == CPO.INCOMPARABLE || compare == CPO.HIGHER ||
                compare == CPO.SAME) {
            String msg = "add method not supported between " +
                this.getClass().getName() + " and " +
                t.getClass().getName();
            throw new IllegalActionException(msg);
        } else {
            return t.addReverse(this);
        }
    }

    /** Return a copy of the contained 2-D array.
     *  It is safe for the caller to modify the returned array.
     *  @return A 2-D boolean array.
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
     *  BooleanMatrixToken and returned. If none of the above condition is
     *  met, an exception is thrown.
     *  @param token The token to be converted to a BooleanMatrixToken.
     *  @return A BooleanMatrixToken
     *  @exception IllegalActionException If the conversion cannot
     *   be carried out in a lossless fashion.
     */
    public static Token convert(Token token)
	    throws IllegalActionException {

	int compare = TypeLattice.compare(new BooleanMatrixToken(), token);
	if (compare == CPO.LOWER || compare == CPO.INCOMPARABLE) {
	    throw new IllegalActionException("BooleanMatrixToken.convert: " +
                    "type of argument: " + token.getClass().getName() +
                    "is higher or incomparable with BooleanMatrixToken " +
                    "in the type hierarchy.");
	}

	if (token instanceof BooleanMatrixToken) {
	    return token;
	}

	// try boolean
	compare = TypeLattice.compare(new BooleanToken(), token);
	if (compare == CPO.SAME || compare == CPO.HIGHER) {
	    BooleanToken tem = (BooleanToken)BooleanToken.convert(token);
	    boolean[][] result = new boolean[1][1];
	    result[0][0] = tem.booleanValue();
	    return new BooleanMatrixToken(result);
	}

	// The argument is below BooleanMatrixToken in the type hierarchy,
	// but I don't recognize it.
        throw new IllegalActionException("cannot convert from token " +
                "type: " + token.getClass().getName() + " to a " +
		"BooleanMatrixToken.");
    }

    /** Return the type of this token.
     *  @return BaseType.BOOLEAN_MATRIX
     */
    public Type getType() {
	return BaseType.BOOLEAN_MATRIX;
    }

    /** Test if the content of this token is equal to that of the specified
     *  token. These two tokens are equal only if the specified token
     *  is also a BooleanMatrixToken with the same dimension, and all the
     *  corresponding elements of the arrays are equal.
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
	    // type of specified token <= BooleanMatrixToken
	    BooleanMatrixToken tem = null;
	    if (t instanceof BooleanMatrixToken) {
		tem = (BooleanMatrixToken)t;
	    } else {
		tem = (BooleanMatrixToken)convert(t);
	    }
	    boolean[][] array = tem.booleanMatrix();

	    for (int i = 0; i < _rowCount; i++) {
		for (int j = 0; j < _columnCount; j++) {
		    if (_value[i][j] != array[i][j]) {
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
     *  @return A BooleanToken containing the matrix element.
     *  @exception ArrayIndexOutOfBoundsException If the specified
     *   row or column number is outside the corresponding range
     *   of the index of the contained array.
     */
    public Token getElementAsToken(int row, int column)
            throws ArrayIndexOutOfBoundsException {
	return new BooleanToken(_value[row][column]);
    }

    /** Return the element of the contained array at the specified
     *  row and column.
     *  @param row The row index of the desired element.
     *  @param column The column index of the desired element.
     *  @return The boolean at the specified array entry.
     *  @exception ArrayIndexOutOfBoundsException If the specified
     *   row or column number is outside the corresponding range
     *   of the index of the contained array.
     */
    public boolean getElementAt(int row, int column) {
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

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private boolean[][] _value = null;
    private int _rowCount = 0;
    private int _columnCount = 0;
}
