/* A token that contains a 2-D boolean matrix.

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
import ptolemy.data.type.*;
import ptolemy.data.expr.PtParser;
import ptolemy.data.expr.ASTPtRootNode;

//////////////////////////////////////////////////////////////////////////
//// BooleanMatrixToken
/**
A token that contains a 2-D boolean matrix.

@author Yuhong Xiong
@version $Id$
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
	BooleanMatrixToken token = (BooleanMatrixToken)tree.evaluateParseTree();
        boolean[][] value = token.booleanMatrix();
        _initialize(value);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a new token whose value is the sum of this token
     *  and the argument. A BooleanMatrixToken can only have a
     *  StringToken added to it. Note that this means adding with
     *  a BooleanMatrixTokens or a BooleanToken will trigger an
     *  exception.
     *  @param token The token to add to this token.
     *  @return A new token.
     *  @exception IllegalActionException If the specified token is
     *   not of a type that can be added to this token.
     */
    public Token add(Token token) throws IllegalActionException {
	int compare = TypeLattice.compare(this, token);
	if (compare == CPO.INCOMPARABLE || compare == CPO.HIGHER ||
                compare == CPO.SAME) {
            throw new IllegalActionException(
                    _notSupportedMessage("add", this, token));
        } else {
            return token.addReverse(this);
        }
    }

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
    public static Token convert(Token token)
	    throws IllegalActionException {

	int compare = TypeLattice.compare(BaseType.BOOLEAN_MATRIX, token);
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
	compare = TypeLattice.compare(BaseType.BOOLEAN, token);
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

    /** Return true if the argument is an instnace of BooleanMatrixToken
     *  of the same dimensions and the corresponding elements of the matrices
     *  are equal.
     *  @param object An instance of Object.
     *  @return True if the argument is an instance of BooleanMatrixToken
     *   of the same dimensions and the corresponding elements of the
     *   matrices are equal.
     */
    public boolean equals(Object object) {
	// This test rules out instances of a subclass.
	if (object.getClass() != BooleanMatrixToken.class) {
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
	return new BooleanToken(_value[row][column]);
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

    /** Test whether the content of this token is equal to that of the
     *  specified token. These two tokens are equal only if the specified token
     *  is also a BooleanMatrixToken with the same dimension, and all the
     *  corresponding elements of the matrices are equal.
     *  @param token The token with which to test equality.
     *  @return A BooleanToken containing the result.
     *  @exception IllegalActionException If the specified token is
     *   not a matrix token, or lossless conversion between this and the
     *   specified tokens is not possible.
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
	    // type of specified token <= BooleanMatrixToken
	    BooleanMatrixToken tem = null;
	    if (token instanceof BooleanMatrixToken) {
		tem = (BooleanMatrixToken)token;
	    } else {
		tem = (BooleanMatrixToken)convert(token);
	    }
	    boolean[][] matrix = tem.booleanMatrix();

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

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // initialize the row and column count and copy the specified
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
