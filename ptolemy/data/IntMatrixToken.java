/* A token that contains a 2-D integer array.

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

@ProposedRating Yellow (yuhong@eecs.berkeley.edu)
@AcceptedRating Yellow (wbwu@eecs.berkeley.edu)
*/

package ptolemy.data;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.graph.CPO;
import ptolemy.math.Complex;
import ptolemy.data.type.*;

//////////////////////////////////////////////////////////////////////////
//// IntMatrixToken
/**
A token that contains a 2-D integer array.

FIXME: Except add() and addReverse(), other arithmetics operations are
not implemented yet. Those methods will be added after the corresponding
operations are added to the math package.

@author Yuhong Xiong, Steve Neuendorffer
@version $Id$
*/
public final class IntMatrixToken extends MatrixToken {

    /** Construct an IntMatrixToken with a one by one array. The
     *  only element in the array has value 0.
     */
    public IntMatrixToken() {
        _rowCount = 1;
        _columnCount = 1;
        _value = new int[1];
        _value[0] = 0;
    }

    /** Construct an IntMatrixToken with the specified 2-D array.
     *  This method makes a copy of the array and stores the
     *  copy, so changes on the specified array after this token
     *  is constructed will not affect the content of this token.
     *  @exception NullPointerException If the specified array
     *   is null.
     */
    public IntMatrixToken(int[][] value) {
        _rowCount = value.length;
        _columnCount = value[0].length;
        _value = new int[_rowCount * _columnCount];
        for (int i = 0; i < _rowCount; i++) {
            System.arraycopy(value[i], 0, _value, i * _columnCount,
                    _columnCount);
        }
    }

    /** Construct an IntMatrixToken with the specified 1-D array,
     *  with the stated dimensions.   The element at (r, c) is in
     *  location r * columns + c.
     *
     *  This method makes a copy of the array and stores the
     *  copy, so changes on the specified array after this token
     *  is constructed will not affect the content of this token.
     *  @exception NullPointerException If the specified array
     *   is null.
     *  @exception RuntimeException If the specified array is not
     *  of length rows * columns
     */
    public IntMatrixToken(int[] value, int rows, int columns) {
        if(value.length != rows * columns)
            throw new RuntimeException("Attempted to create invalid " +
                    "IntMatrixToken with " + rows + " rows and " +
                    columns + " columns, but the array was length " +
                    value.length + ".");
        _rowCount = rows;
        _columnCount = columns;
        _value = new int[_rowCount * _columnCount];
        System.arraycopy(value, 0, _value, 0,
                _rowCount * _columnCount);

    }

    // FIXME: finish this method after array is added to the
    //               expression language.
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
    public Token add(final Token t)
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
             // type of the specified token <= IntMatrixToken
            int[][] result = null;

            if (t instanceof ScalarToken) {
                int scalar = ((ScalarToken)t).intValue();
                result = new int[_rowCount][_columnCount];
                for (int i = 0; i < _rowCount; i++) {
                    for (int j = 0; j < _columnCount; j++) {
                        result[i][j] =
                            scalar + _value[i * _columnCount + j];
                    }
                }
            } else {
                // the specified token is not a scalar.
                IntMatrixToken tem = (IntMatrixToken)this.convert(t);
                    if (tem.getRowCount() != _rowCount ||
                        tem.getColumnCount() != _columnCount) {
                    throw new IllegalActionException("Cannot add two " +
                            "matrices with different dimension.");
                    }

                result = tem.intMatrix();
                for (int i = 0; i < _rowCount; i++) {
                    for (int j = 0; j < _columnCount; j++) {
                        result[i][j] +=
                            _value[i * _columnCount + j];
                    }
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
     *   token is not lower than IntMatrixToken.
     */
    public Token addReverse(final Token t)
            throws IllegalActionException {
        int compare = TypeLattice.compare(this, t);
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
        Complex[][] array = new Complex[_rowCount][_columnCount];
        for (int i = 0; i < _rowCount; i++) {
            for (int j = 0; j < _columnCount; j++) {
                array[i][j] =
                    new Complex((double)_value[i * _columnCount + j]);
            }
        }
        return array;
    }

    /** Convert the specified token to an instance of IntMatrixToken.
     *  This method does lossless conversion.
     *  If the argument is already an instance of IntMatrixToken,
     *  it is returned without any change. Otherwise, if the argument
     *  is below IntMatrixToken in the type hierarchy, it is converted to
     *  an instance of IntMatrixToken or one of the subclasses of
     *  IntMatrixToken and returned. If none of the above condition is
     *  met, an exception is thrown.
     *  @param token The token to be converted to IntMatrixToken.
     *  @return A IntMatrixToken
     *  @exception IllegalActionException If the conversion cannot
     *   be carried out in a lossless fashion.
     */
    public static Token convert(final Token token)
            throws IllegalActionException {

        int compare = TypeLattice.compare(new IntMatrixToken(), token);
        if (compare == CPO.LOWER || compare == CPO.INCOMPARABLE) {
            throw new IllegalActionException("IntMatrixToken.convert: " +
                    "type of argument: " + token.getClass().getName() +
                    "is higher or incomparable with IntMatrixToken in the " +
                    "type hierarchy.");
        }

        if (token instanceof IntMatrixToken) {
            return token;
        }

        compare = TypeLattice.compare(new IntToken(), token);
        if (compare == CPO.SAME || compare == CPO.HIGHER) {
            IntToken tem = (IntToken)IntToken.convert(token);
            int[][] result = new int[1][1];
            result[0][0] = tem.intValue();
            return new IntMatrixToken(result);
        }

        // The argument is below IntMatrixToken in the type hierarchy,
        // but I don't recognize it.
        throw new IllegalActionException("cannot convert from token " +
                "type: " + token.getClass().getName() + " to a " +
                "IntMatrixToken.");
    }

    /** Return the content of this token as a 2-D double array.
     *  @return A 2-D double array.
     */
    public double[][] doubleMatrix() {
        double[][] array = new double[_rowCount][_columnCount];
        for (int i = 0; i < _rowCount; i++) {
            for (int j = 0; j < _columnCount; j++) {
                array[i][j] = (double)_value[i * _columnCount + j];
            }
        }
        return array;
    }

    /** Return the type of this token.
     *  @return BaseType.INT_MATRIX
     */
    public Type getType() {
        return BaseType.INT_MATRIX;
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
     *   not a matrix token, or lossless conversion is not possible.
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
            // type of specified token <= IntMatrixToken
            IntMatrixToken tem = null;
            if (t instanceof IntMatrixToken) {
                tem = (IntMatrixToken)t;
            } else {
                tem = (IntMatrixToken)convert(t);
            }

            for (int i = 0; i < _rowCount; i++) {
                for (int j = 0; j < _columnCount; j++) {
                    if (_value[i * _columnCount + j] !=
                            tem.getElementAt(i, j)) {
                        return new BooleanToken(false);
                    }
                }
            }
            return new BooleanToken(true);
        }
    }

    /** Return the number of columns in the matrix.
     *  @return An integer.
     */
    public int getColumnCount() {
        return _columnCount;
    }

    /** Return the element of the matrix at the specified
     *  row and column wrapped in a token.
     *  @param row The row index of the desired element.
     *  @param column The column index of the desired element.
     *  @return An IntToken containing the matrix element.
     *  @exception ArrayIndexOutOfBoundsException If the specified
     *   row or column number is outside the corresponding range
     *   of the index of the contained array.
     */
    public Token getElementAsToken(int row, int column)
            throws ArrayIndexOutOfBoundsException {
        return new IntToken(_value[row * _columnCount + column]);
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
        return _value[row * _columnCount + column];
    }

    /** Return the number of rows of the contained  matrix.
     *  @return An integer.
     */
    public int getRowCount() {
        return _rowCount;
    }

    /** Return the content of this token as a 2-D integer array.
     *  The returned array is a copy so the caller is free to
     *  modify it.
     *  @return A 2-D integer array.
     */
    public int[][] intMatrix() {
        int[][] array = new int[_rowCount][_columnCount];
        for (int i = 0; i < _rowCount; i++) {
            System.arraycopy(_value, i * _columnCount, array[i], 0,
                    _columnCount);
        }
        return array;
    }

    /** Return the content of this token as a 1-D integer array.
     *  The returned array contains a row-by-row copy of the Matrix,
     *  starting with row zero.
     *  @return A 1-D integer Array
     */
    public int[] intArray() {
        int[] array = new int[_rowCount*_columnCount];
        System.arraycopy(_value, 0, array, 0, _rowCount*_columnCount);
        return array;
    }


    /* Return a reference to the internal representation of the
     *  Matrix, which is similar in format to that returned by intArray()
     *  This method is provided for speed ONLY and the returned reference
     *  should not be modified!
     *  @return A reference to the internal 1-D integer Array.
     * FIXME should this method be here?
     */
    /*public int[] intArrayRef() {
      return _value;
      }*/

    /** Return the content of this token as a 2-D long array.
     */
    public long[][] longMatrix() {
        long[][] array = new long[_rowCount][_columnCount];
        for (int i = 0; i < _rowCount; i++) {
            for (int j = 0; j < _columnCount; j++) {
                array[i][j] = (long)_value[i * _columnCount + j];
            }
        }
        return array;
    }

    /** Return a new Token representing the left multiplicative
     *  identity. The returned token contains an identity matrix
     *  whose dimension is the same as the number of rows of
     *  the matrix contained in this token.
     *  @return A new Token containing the left multiplicative identity.
     */
    public Token one() {
        int[][] result = new int[_rowCount][_rowCount];
        for (int i = 0; i < _rowCount; i++) {
            for (int j = 0; j < _rowCount; j++) {
                result[i][j] = 0;
            }
            result[i][i] = 1;
        }
        return new IntMatrixToken(result);
    }

    /** Return a new Token representing the right multiplicative
     *  identity. The returned token contains an identity matrix
     *  whose dimension is the same as the number of columns of
     *  the matrix contained in this token.
     *  @return A new Token containing the right multiplicative identity.
     */
    public Token oneRight() {
        int[][] result = new int[_columnCount][_columnCount];
        for (int i = 0; i < _columnCount; i++) {
            for (int j = 0; j < _columnCount; j++) {
                result[i][j] = 0;
            }
            result[i][i] = 1;
        }
        return new IntMatrixToken(result);
    }

    /** Return a new Token whose value is the value of the argument Token
     *  subtracted from the value of this Token.
     *  It should be overridden in derived classes to provide type specific
     *  actions for subtract.
     *  @param rightArg The token whose value we subtract from this Token.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
     */
    public Token subtract(Token t)
            throws IllegalActionException {

        int compare = TypeLattice.compare(this, t);
        if (compare == CPO.INCOMPARABLE) {
            String msg = "subtract method not supported between " +
                this.getClass().getName() + " and " +
                t.getClass().getName();
            throw new IllegalActionException(msg);
        } else if (compare == CPO.LOWER) {
            return t.subtractReverse(this);
        } else {
             // type of the specified token <= IntMatrixToken
            int[][] result = null;

            if (t instanceof ScalarToken) {
                int scalar = ((ScalarToken)t).intValue();
                result = new int[_rowCount][_columnCount];
                for (int i = 0; i < _rowCount; i++) {
                    for (int j = 0; j < _columnCount; j++) {
                        result[i][j] =
                            _value[i * _columnCount + j] - scalar;
                    }
                }
            } else {
                // the specified token is not a scalar.
                IntMatrixToken tem = (IntMatrixToken)this.convert(t);
                    if (tem.getRowCount() != _rowCount ||
                        tem.getColumnCount() != _columnCount) {
                    throw new IllegalActionException("Cannot subtract two " +
                            "matrices with different dimension.");
                    }

                result = tem.intMatrix();
                for (int i = 0; i < _rowCount; i++) {
                    for (int j = 0; j < _columnCount; j++) {
                        result[i][j] = _value[i * _columnCount + j] -
                            result[i][j];
                    }
                }
            }
            return new IntMatrixToken(result);
        }
    }

    /** Return a new Token whose value is the value of this Token
     *  subtracted from the value of the argument Token.
     *  It should be overridden in derived classes to provide type specific
     *  actions for subtract.
     *  @param leftArg The token to subtract the value of this Token from.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
     */
    public Token subtractReverse(Token t)
            throws IllegalActionException {

        int compare = TypeLattice.compare(this, t);
        if (compare == CPO.INCOMPARABLE) {
            String msg = "subtractReverse method not supported between " +
                this.getClass().getName() + " and " +
                t.getClass().getName();
            throw new IllegalActionException(msg);
        } else if (compare == CPO.LOWER) {
            return t.subtract(this);
        } else {
             // type of the specified token <= IntMatrixToken
            int[][] result = null;

            if (t instanceof ScalarToken) {
                int scalar = ((ScalarToken)t).intValue();
                result = new int[_rowCount][_columnCount];
                for (int i = 0; i < _rowCount; i++) {
                    for (int j = 0; j < _columnCount; j++) {
                        result[i][j] = scalar -
                            _value[i * _columnCount + j];
                    }
                }
            } else {
                // the specified token is not a scalar.
                IntMatrixToken tem = (IntMatrixToken)this.convert(t);
                if (tem.getRowCount() != _rowCount ||
                    tem.getColumnCount() != _columnCount) {
                   throw new IllegalActionException("Cannot subtract two " +
                    "matrices with different dimension.");
                }

                result = tem.intMatrix();
                for (int i = 0; i < _rowCount; i++) {
                    for (int j = 0; j < _columnCount; j++) {
                        result[i][j] -= _value[i * _columnCount + j];
                    }
                }
            }
            return new IntMatrixToken(result);
        }
    }

    /** Return a new Token representing the additive identity.
     *  The returned token contains a matrix whose elements are
     *  all zero, and the size of the matrix is the same as the
     *  matrix contained in this token.
     *  @return A new Token containing the additive identity.
     */
    public Token zero() {
        // assume arrays are initialized with elements equal to 0
        return new IntMatrixToken(new int[_rowCount][_columnCount]);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private int[] _value = null;
    private int _rowCount = 0;
    private int  _columnCount = 0;
}
