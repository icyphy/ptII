/* A token that contains a 2-D double array.

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
import ptolemy.kernel.util.*;
import ptolemy.graph.CPO;
import ptolemy.math.Complex;
import ptolemy.math.DoubleMatrixMath;
import ptolemy.data.type.*;

//////////////////////////////////////////////////////////////////////////
//// DoubleMatrixToken
/**
A token that contains a 2-D double array.

@author Yuhong Xiong, Jeff Tsay
@version $Id$
*/
public class DoubleMatrixToken extends MatrixToken {

    /** Construct an DoubleMatrixToken with a one by one array. The
     *  only element in the array has value 0.0
     */
    public DoubleMatrixToken() {
        this(new double[1][1], DO_NOT_COPY);
    }

    /** Construct a DoubleMatrixToken with the specified 2-D array.
     *  Make a copy of the array and store the copy,
     *  so that changes on the specified array after this token is
     *  constructed will not affect the content of this token.
     *  @exception NullPointerException If the specified array
     *   is null.
     */
    public DoubleMatrixToken(final double[][] value) {
        this(value, DO_COPY);
    }

    /** Construct a DoubleMatrixToken with the specified 2-D array.
     *  If copy is DO_COPY, make a copy of the array and store the copy,
     *  so that changes on the specified array after this token is
     *  constructed will not affect the content of this token.
     *  If copy is DO_NOT_COPY, just reference the array (do not copy
     *  its contents). This saves some time and memory.
     *  The argument array should NOT be modified after this constructor 
     *  is called to preserve immutability.          
     *  @exception NullPointerException If the specified array
     *   is null.
     */
    protected DoubleMatrixToken(final double[][] value, final int copy) {
        _rowCount = value.length;
        _columnCount = value[0].length;

        if (copy == DO_NOT_COPY) {
           _value = value;
        } else {
           _value = DoubleMatrixMath.allocCopy(value);
        }
    }

    // FIXME: finish this method after array is added to the
    //               expression language.
    // Construct an DoubleMatrixToken from the specified string.
    // @param init A string expression of a 2-D double array.
    // @exception IllegalArgumentException If the string does
    //  not contain a parsable 2-D int array.
    //
    // public DoubleMatrixToken(String init) {
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
    public final Token add(Token t)
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
            // type of the specified token <= DoubleMatrixToken
            double[][] result = null;

            if (t instanceof ScalarToken) {
                double scalar = ((ScalarToken)t).doubleValue();
                result = DoubleMatrixMath.add(_value, scalar);
            } else {
                // the specified token is not a scalar.
                DoubleMatrixToken tem = (DoubleMatrixToken)this.convert(t);
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
     *  @param t The token to add this Token to.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the type of the specified
     *   token is not lower than DoubleMatrixToken.
     */
    public final Token addReverse(Token t)
            throws IllegalActionException {
        int compare = TypeLattice.compare(this, t);
        if (! (compare == CPO.HIGHER)) {
            throw new IllegalActionException("The type of the specified "
                    + "token " + t.getClass().getName() + " is not lower than "
                    + getClass().getName());
        }
        // add is commutative on double matrix.
        return add(t);
    }

    /** Return the content of this token as a 2-D Complex array.
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
     *   be carried out in a lossless fashion.
     */
    public static final Token convert(Token token)
            throws IllegalActionException {

        int compare = TypeLattice.compare(new DoubleMatrixToken(), token);
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
        compare = TypeLattice.compare(new DoubleToken(), token);
        if (compare == CPO.SAME || compare == CPO.HIGHER) {
            DoubleToken tem = (DoubleToken) DoubleToken.convert(token);
            double[][] result = new double[1][1];
            result[0][0] = tem.doubleValue();
            return new DoubleMatrixToken(result);
        }

        // try IntMatrix
        compare = TypeLattice.compare(new IntMatrixToken(), token);
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

    /** Return the content in the token as a 2-D double array.
     *  The returned array is a copy so the caller is free to
     *  modify it.
     *  @return A 2-D double array.
     */
    public final double[][] doubleMatrix() {
       return DoubleMatrixMath.allocCopy(_value);
    }

    /** Return the type of this token.
     *  @return BaseType.DOUBLE_MATRIX
     */
    public final Type getType() {
       return BaseType.DOUBLE_MATRIX;
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
    public final BooleanToken isEqualTo(Token t)
            throws IllegalActionException {
        int compare = TypeLattice.compare(this, t);
        if ( !(t instanceof MatrixToken) ||
                compare == CPO.INCOMPARABLE) {
            throw new IllegalActionException("Cannot check equality " +
                    "between " + this.getClass().getName() + " and " +
                    t.getClass().getName());
        }

        if (((MatrixToken)t).getRowCount() != _rowCount ||
            ((MatrixToken)t).getColumnCount() != _columnCount) {
            return new BooleanToken(false);
        }

        if (compare == CPO.LOWER) {
            return t.isEqualTo(this);
        } else {
            // type of specified token <= DoubleMatrixToken
            DoubleMatrixToken tem = (DoubleMatrixToken) convert(t);
        
            return new BooleanToken(DoubleMatrixMath.within(_value, 
             tem._getInternalDoubleMatrix(), 0.0));
        }
    }

    /** Return the element of the matrix at the specified
     *  row and column wrapped in a token.
     *  @param row The row index of the desired element.
     *  @param column The column index of the desired element.
     *  @return A DoubleToken containing the matrix element.
     *  @exception ArrayIndexOutOfBoundsException If the specified
     *   row or column number is outside the corresponding range
     *   of the index of the contained array.
     */
    public final Token getElementAsToken(final int row, final int column)
            throws ArrayIndexOutOfBoundsException {
        return new DoubleToken(_value[row][column]);
    }

    /** Return the element of the contained array at the specified
     *  row and column.
     *  @param row The row index of the desired element.
     *  @param column The column index of the desired element.
     *  @return The double at the specified array entry.
     *  @exception ArrayIndexOutOfBoundsException If the specified
     *   row or column number is outside the corresponding range
     *   of the index of the contained array.
     */
    public final double getElementAt(final int row, final int column) {
        return _value[row][column];
    }

    /** Return the number of columns in the matrix.
     *  @return An integer.
     */
    public final int getColumnCount() {
        return _columnCount;
    }

    /** Return the number of rows in the matrix.
     *  @return An integer.
     */
    public final int getRowCount() {
        return _rowCount;
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
     *  @param t The token to add to this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the specified token is
     *   not of a type that can be added to this token in a lossless
     *   fashion.
     */
    public final Token multiply(final Token t)
            throws IllegalActionException {

        int compare = TypeLattice.compare(this, t);
        if (compare == CPO.INCOMPARABLE) {
            String msg = "multiply method not supported between " +
                this.getClass().getName() + " and " +
                t.getClass().getName();
            throw new IllegalActionException(msg);
        } else if (compare == CPO.LOWER) {
            return t.multiplyReverse(this);
        } else {
            // type of the specified token <= DoubleMatrixToken
            double[][] result = null;

            if (t instanceof ScalarToken) {
                double scalar = ((ScalarToken)t).doubleValue();
                result = DoubleMatrixMath.multiply(_value, scalar);
            } else {
                // the specified token is not a scalar.
                DoubleMatrixToken tem = (DoubleMatrixToken) this.convert(t);
                if (tem.getRowCount() != _columnCount) {
                    throw new IllegalActionException("Cannot multiply " +
                            "matrix with " + _columnCount +
                            " columns by a matrix with " +
                            tem.getRowCount() + " rows.");
                }

                result = DoubleMatrixMath.multiply(
                          tem._getInternalDoubleMatrix(), _value);
            }
            return new DoubleMatrixToken(result, DO_NOT_COPY);
        }
    }

    /** Return a new token whose value is the product of this token
     *  and the argument. The type of the specified token must
     *  be lower than DoubleMatrixToken.
     *  @param t The token to multiply this Token by.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the type of the specified
     *   token is not lower than DoubleMatrixToken.
     */
    public final Token multiplyReverse(final Token t)
            throws IllegalActionException {
       int compare = TypeLattice.compare(this, t);
       if (! (compare == CPO.HIGHER)) {
           throw new IllegalActionException("The type of the specified "
                    + "token " + t.getClass().getName() + " is not lower than "
                    + getClass().getName());
       }
       
       // Check if t is matrix. In that case we must convert t into a
       // DoubleMatrixToken because matrix multiplication is not
       // commutative.
       if (t instanceof ScalarToken) {
          // multiply is commutative on double matrices, for scalar types.
          return multiply(t);
       } else {
          // the specified token is not a scalar       
          DoubleMatrixToken tem = (DoubleMatrixToken) this.convert(t);
          return new DoubleMatrixToken(DoubleMatrixMath.multiply(
                      tem._getInternalDoubleMatrix(), _value), DO_NOT_COPY);
       }       
    }

    /** Return a new Token representing the left multiplicative
     *  identity. The returned token contains an identity matrix
     *  whose dimension is the same as the number of rows of
     *  the matrix contained in this token.
     *  @return A new Token containing the left multiplicative identity.
     */
    public final Token one() {
        return new DoubleMatrixToken(DoubleMatrixMath.identity(_rowCount), DO_NOT_COPY);
    }

    /** Return a new Token representing the right multiplicative
     *  identity. The returned token contains an identity matrix
     *  whose dimension is the same as the number of columns of
     *  the matrix contained in this token.
     *  @return A new Token containing the right multiplicative identity.
     */
    public final Token oneRight() {
       return new DoubleMatrixToken(DoubleMatrixMath.identity(_columnCount), DO_NOT_COPY);
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
     *  @param t The token to subtract to this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the specified token is
     *   not of a type that can be added to this token in a lossless
     *   fashion.
     */
    public final Token subtract(final Token t)
            throws IllegalActionException {

        int compare = TypeLattice.compare(this, t);
        if (compare == CPO.INCOMPARABLE) {
            String msg = "subtract method not supported between " +
                this.getClass().getName() + " and " +
                t.getClass().getName();
            throw new IllegalActionException(msg);
        } else if (compare == CPO.LOWER) {
            Token me = t.convert(this);            
            return me.subtract(t); 
        } else {
            // type of the specified token <= DoubleMatrixToken
            double[][] result = null;

            if (t instanceof ScalarToken) {
                double scalar = ((ScalarToken)t).doubleValue();
                result = DoubleMatrixMath.add(_value, -scalar);
            } else {
                // the specified token is not a scalar.
                DoubleMatrixToken tem = (DoubleMatrixToken)this.convert(t);
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
     *  @param t The token to add this Token to.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the type of the specified
     *   token is not lower than DoubleMatrixToken.
     */
    public final Token subtractReverse(final Token t)
            throws IllegalActionException {
        int compare = TypeLattice.compare(this, t);
        if (! (compare == CPO.HIGHER)) {
            throw new IllegalActionException("The type of the specified "
                    + "token " + t.getClass().getName() + " is not lower than "
                    + getClass().getName());
        }
        // add the argument Token to the negative of this Token
        DoubleMatrixToken negativeToken = 
         new DoubleMatrixToken(DoubleMatrixMath.negative(_value), DO_NOT_COPY);
        return negativeToken.add(t);
    }

    /** Return a new Token representing the additive identity.
     *  The returned token contains a matrix whose elements are
     *  all zero, and the size of the matrix is the same as the
     *  matrix contained in this token.
     *  @return A new Token containing the additive identity.
     */
    public final Token zero() {
       return new DoubleMatrixToken(new double[_rowCount][_columnCount], DO_NOT_COPY);
    }

    ///////////////////////////////////////////////////////////////////
    ////                      protected methods                    ////

    /** Return a reference to the internal 2-D array of doubles that represents
     *  this Token. Because no copying is done, the contents must NOT be modified
     *  to preserve the immutability of Token.
     *  @return A 2-D double array.
     */
    protected double[][] _getInternalDoubleMatrix() {
        return _value;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private final double[][] _value;
    private final int _rowCount;
    private final int _columnCount;        
}
