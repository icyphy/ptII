/* A token that contains a 2-D integer array.

 Copyright (c) 1997 The Regents of the University of California.
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
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.graph.CPO;
import ptolemy.math.Complex;

//////////////////////////////////////////////////////////////////////////
//// IntMatrixToken
/**
A token that contains a 2-D integer array.

FIXME: Except add() and addR(), other arithmetics operations are
not implemented yet. Those methods will be added after the corresponding
operations are added to the math package.

@author Yuhong Xiong
@version $Id$
*/
public class IntMatrixToken extends MatrixToken {

    /** Construct an IntMatrixToken with a one by one array. The
     *  only element in the array has value 0.
     */
    public IntMatrixToken() {
	_numRows = 1;
	_numColumns = 1;
	_value = new int[1][1];
	_value[0][0] = 0;
    }

    /** Construct an IntMatrixToken with the specified 2-D array.
     *  This method makes a copy of the array and stores the
     *  copy, so changes on the specified array after this token
     *  is constructed will not affect the content of this token.
     *  @exception NullPointerException If the specified array
     *   is null.
     */
    public IntMatrixToken(int[][] value) {
	_numRows = value.length;
	_numColumns = value[0].length;
	_value = new int[_numRows][_numColumns];
	for (int i = 0; i < _numRows; i++) {
	    for (int j = 0; j < _numColumns; j++) {
		_value[i][j] = value[i][j];
	    }
	}
    }

    // FIXME: finish this method after array is added to the 
    // 	      expression language.
    // Construct an IntMatrixToken from the specified string.
    // @param init A string expression of a 2-D int array.
    // @exception IllegalArgumentException If the string does
    //  not contain a parsable 2-D int array.
    //
    // public IntMatrixToken(String init) {
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
     *  @return A new token.
     *  @exception IllegalActionException If the specified token is
     *   not of a type that can be added to this token in a lossless
     *   fashion.
     */
    public Token add(Token t)
	    throws IllegalActionException {

	int compare = TypeCPO.compare(this, t);
	if (compare == CPO.INCOMPARABLE) {
	    String msg = "add method not supported between " +
			 this.getClass().getName() + " and " +
			 t.getClass().getName();
	    throw new IllegalActionException(msg);
	} else if (compare == CPO.LOWER) {
	    return t.addR(this);
	} else {
	    // type of the specified token <= IntMatrixToken
	    int[][] result = null;

	    if (t instanceof ScalarToken) {
		int scalar = ((ScalarToken)t).intValue();
		result = new int[_numRows][_numColumns];
		for (int i = 0; i < _numRows; i++) {
		    for (int j = 0; j < _numColumns; j++) {
			result[i][j] = scalar + _value[i][j];
		    }
		}
	    } else {
		// the specified token is not a scalar.
	        if (t instanceof MatrixToken) {
	    	    if (((MatrixToken)t).numRows() != _numRows ||
		        ((MatrixToken)t).numColumns() != _numColumns) {
		    	throw new IllegalActionException("Cannot add two " +
				"matrices with different dimension.");
	    	    }

		    if (t instanceof IntMatrixToken) {
		    	result = ((IntMatrixToken)t).intMatrix();
		    } else {
		        IntMatrixToken tem = (IntMatrixToken)this.convert(t);
			result = tem.intMatrix();
		    }
		    for (int i = 0; i < _numRows; i++) {
			for (int j = 0; j < _numColumns; j++) {
			    result[i][j] += _value[i][j];
			}
		    }
		} else {
		    // FIXME: what if the specified token is user defined?
		}
	    }
	    return new IntMatrixToken(result);
	}
    }

    /** Return a new token whose value is the sum of this token
     *  and the argument. The type of the specified token should
     *  be lower than IntMatrixToken.
     *  @param t The token to be added to this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the type of the specified
     *   token is not lower than IntMatrixToken; or if the specified
     *   token is not of a type that can be added to this token in a
     *   lossless fashion.
     */
    public Token addR(Token t)
	    throws IllegalActionException {
	int compare = TypeCPO.compare(this, t);
	if (! (compare == CPO.HIGHER)) {
	    throw new IllegalActionException("The type of the specified "
		+ "token " + t.getClass().getName() + " is not lower than "
		+ getClass().getName());
	}
	// add is commutative on integer matrix.
	return add(t);
    }

    /** Return the content of this token as a 2-D Complex array.
     *  @return A 2-D Complex array.
     */
    public Complex[][] complexMatrix() {
        Complex[][] array = new Complex[_numRows][_numColumns];
        for (int i = 0; i < _numRows; i++) {
            for (int j = 0; j < _numColumns; j++) {
                array[i][j] = new Complex((double)_value[i][j]);
            }
        }
        return array;
    }

    /** Convert the specified token to an instance of IntMatrixToken.
     *  This method does lossly conversion.
     *  If the argument is already an instance of IntMatrixToken,
     *  it is returned without any change. Otherwise, if the argument
     *  is below IntMatrixToken in the type hierarchy, it is converted to
     *  an instance of IntMatrixToken or one of the subclasses of
     *  IntMatrixToken and returned. If non of the above condition is
     *  met, an exception is thrown.
     *  @param token The token to be converted to IntMatrixToken.
     *  @return A IntMatrixToken
     *  @exception IllegalActionException If the conversion cannot
     *   be carried out in a lossless fashion.
     */
    public static Token convert(Token token)
	    throws IllegalActionException {

	int compare = TypeCPO.compare(new IntMatrixToken(), token);
	if (compare == CPO.LOWER || compare == CPO.INCOMPARABLE) {
	    throw new IllegalActionException("IntMatrixToken.convert: " +
		"type of argument: " + token.getClass().getName() +
		"is higher or incomparable with IntMatrixToken in the " +
                "type hierarchy.");
	}

	if (token instanceof IntMatrixToken) {
	    return token;
	}

        compare = TypeCPO.compare(new IntToken(), token);
        if (compare == CPO.SAME || compare == CPO.HIGHER) {
            IntToken tem = (IntToken)IntToken.convert(token);
            int[][] result = new int[1][1];
            result[0][0] = tem.intValue();
            return new IntMatrixToken(result);
        }

        // FIXME: token must be user defined. what to do?
        throw new IllegalActionException("cannot convert from token " +
                "type: " + token.getClass().getName() + " to a " +
                "IntMatrixToken.");
    }

    /** Return the content of this token as a 2-D double array.
     *  @return A 2-D double array. 
     */
    public double[][] doubleMatrix() {
        double[][] array = new double[_numRows][_numColumns];
        for (int i = 0; i < _numRows; i++) {
            for (int j = 0; j < _numColumns; j++) {
                array[i][j] = (double)_value[i][j];
            }
        }
	return array;
    }

    /** Test if the content of this token equals that of the specified
     *  token. These two tokens are equal only if the specified token
     *  is also a matrix token with the same dimension, and all the
     *  corresponding elements of the arrays are equal, and lossless 
     *  conversion is possible from either this token to the specified
     *  one, or vice versa.
     *  @param t The token with which to test equality.
     *  @return A booleanToken containing the result.
     *  @exception IllegalActionException If the specified token is
     *   not a matrix token, or lossless conversion is not possible.
     */
    public BooleanToken equals(Token t)
	    throws IllegalActionException {
	int compare = TypeCPO.compare(this, t);
	if ( !(t instanceof MatrixToken) ||
	     compare == CPO.INCOMPARABLE) {
	    throw new IllegalActionException("Cannot check equality " +
		"between " + this.getClass().getName() + " and " +
		t.getClass().getName());
	}

	if ( ((MatrixToken)t).numRows() != _numRows ||
	     ((MatrixToken)t).numColumns() != _numColumns) {
	    return new BooleanToken(false);
	}

	if (compare == CPO.LOWER) {
	    return t.equals(this);
	} else {
	    // type of specified token <= IntMatrixToken
	    IntMatrixToken tem = null;
	    if (t instanceof IntMatrixToken) {
		tem = (IntMatrixToken)t;
	    } else {
		tem = (IntMatrixToken)convert(t);
	    }
	    int[][] array = tem.intMatrix();

	    for (int i = 0; i < _numRows; i++) {
		for (int j = 0; j < _numColumns; j++) {
		    if (_value[i][j] != array[i][j]) {
			return new BooleanToken(false);
		    }
		}
	    }
	    return new BooleanToken(true);
	}
    }

    /** Return the element of the contained array at the specified
     *  row and column.
     *  @param row The row index of the desired element.
     *  @param column The column index of the desired element.
     *  @return The integer at the specified array entry.
     *  @exception ArrayIndexOutOfBoundsException If the specified
     *   row or column number is outside the corresponding range
     *   of the index of the contained array.
     */
    public int getElementAt(int row, int column) {
	return _value[row][column];
    }

    /** Return a copy of the contained 2-D array.
     *  It is safe for the caller to modify the returned array.
     *  @return A 2-D integer array.
     */
    public int[][] getWritableCopy() {
	int[][] result = new int[_numRows][_numColumns];
	for (int i = 0; i < _numRows; i++) {
	    for (int j = 0; j < _numColumns; j++) {
		result[i][j] = _value[i][j];
	    }
	}
	return result;
    }

    /** Return the content of this token as a 2-D integer array.
     *  The returned array is a copy so the caller is free to
     *  modify it.
     *  @return A 2-D integer array.
     */
    public int[][] intMatrix() {
        int[][] array = new int[_numRows][_numColumns];
        for (int i = 0; i < _numRows; i++) {
            for (int j = 0; j < _numColumns; j++) {
                array[i][j] = _value[i][j];
            }
        }
        return array;
    }

    /** Return the content of this token as a 2-D long array.
     */
    public long[][] longMatrix() {
        long[][] array = new long[_numRows][_numColumns];
        for (int i = 0; i < _numRows; i++) {
            for (int j = 0; j < _numColumns; j++) {
                array[i][j] = (long)_value[i][j];
            }
        }
        return array;
    }

    /** Return the number of columns in the matrix.
     *  @return An integer.
    */
    public int numColumns() {
        return _numColumns;
    }

    /** Return the number of rows of the contained  matrix.
     *  @return An integer.
    */
    public int numRows() {
        return _numRows;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private int[][] _value = null;
    private int _numRows = 0;
    private int  _numColumns = 0;
}

