/* Abstract base class for tokens that contain a 2-D matrix.

 Copyright (c) 1997-2002 The Regents of the University of California.
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
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.math.Complex;

//////////////////////////////////////////////////////////////////////////
//// MatrixToken
/**
Abstract base class for tokens that contain a 2-D matrix.
This class defines methods for type conversion among different matrix
tokens. The implementation in this base class just throws an exception.
Derived classes should override those methods where the corresponding
conversion can be achieved without loss of information.

@author Yuhong Xiong
@version $Id$
*/
public abstract class MatrixToken extends Token {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a copy of the content of this token as a 2-D Complex matrix.
     *  In this base class, just throw an exception.
     *  @return A 2-D Complex matrix.
     *  @exception IllegalActionException If the token cannot be represented
     *   as requested (always thrown in this base class).
     */
    public Complex[][] complexMatrix() throws IllegalActionException {
        throw new IllegalActionException(this.getClass().getName() +
                " cannot be converted to a complex matrix.");
    }

    /** Return the argument token if it is an instance of MatrixToken,
     *  otherwise, throw an exception.
     *  @param token A Token to be converted.
     *  @return A MatrixToken.
     *  @exception IllegalActionException If the argument is not an instance
     *   of MatrixToken.
     */
    public static Token convert(Token token) throws IllegalActionException {
	if (token instanceof MatrixToken) {
	    return token;
	}

        throw new IllegalActionException(token.getClass().getName() +
                " cannot convert to a MatrixToken.");
    }

    /** Return the content of this token as a 2-D double matrix.
     *  In this base class, just throw an exception.
     *  @return A 2-D double matrix.
     *  @exception IllegalActionException If the token cannot be represented
     *   as requested (always thrown in this base class).
     */
    public double[][] doubleMatrix() throws IllegalActionException {
        throw new IllegalActionException(this.getClass().getName() +
                " cannot be converted to a double matrix.");
    }

    /** Return the number of columns of the matrix.
     *  @return The number of columns of the matrix.
     */
    public abstract int getColumnCount();

    /** Return the element of the matrix at the specified
     *  row and column wrapped in a token.
     *  @param row The row index of the desired element.
     *  @param column The column index of the desired element.
     *  @return A token containing the matrix element.
     *  @exception ArrayIndexOutOfBoundsException If the specified
     *   row or column number is outside the range of the matrix.
     */
    public abstract Token getElementAsToken(int row, int column)
            throws ArrayIndexOutOfBoundsException;

    /** Return the number of rows of the matrix.
     *  @return The number of rows of the matrix.
     */
    public abstract int getRowCount();

    /** Return the content of this token as a 2-D integer matrix.
     *  In this base class, just throw an exception.
     *  @return A 2-D integer matrix.
     *  @exception IllegalActionException If the token cannot be represented
     *   as requested (always thrown in this base class).
     */
    public int[][] intMatrix() throws IllegalActionException {
        throw new IllegalActionException(this.getClass().getName() +
                " cannot be converted to an integer matrix.");
    }

    /** Return the content of this matrix as a 2-D long matrix.
     *  In this base class, just throw an exception.
     *  @return A 2-D long matrix.
     *  @exception IllegalActionException If the token cannot be represented
     *   as requested (always thrown in this base class).
     */
    public long[][] longMatrix() throws IllegalActionException {
        throw new IllegalActionException(this.getClass().getName() +
                " cannot be converted to a long matrix.");
    }

    /** Return a new Token representing the right multiplicative
     *  identity. The returned token contains an identity matrix
     *  whose dimensions are the same as the number of columns of
     *  the matrix contained in this token.
     *  The implementation in this base class just throws an
     *  exception. This method should be overridden in the subclass
     *  when the right multiplicative identity exists.
     *  @return A new MatrixToken containing the right multiplicative
     *   identity.
     *  @exception IllegalActionException If this method is not
     *   supported by the derived class.
     */
    public Token oneRight()
	    throws IllegalActionException {
        throw new IllegalActionException("Right multiplicative identity " +
                "not supported on " + getClass().getName() + " objects.");
    }

    /** Return an ArrayToken containing the all the values of this
     *  matrix token.  The type of the tokens in the array is consistent
     *  with the type of this token.
     *
     *  @return An ArrayToken containing the elements of this matrix in
     *  row-scanned order.
     */
    public ArrayToken toArray() {
        int rowCount = getRowCount();
        int columnCount = getColumnCount();
        Token[] output = new Token[rowCount * columnCount];
        for (int i = 0, n = 0; i < rowCount; i++) {
            for (int j = 0; j < columnCount; j++) {
                output[n++] = getElementAsToken(i, j);
            }
        }

	ArrayToken result;
	try {
	    result = new ArrayToken(output);
	} catch (IllegalActionException illegalAction) {
	    // Cannot happen, since the elements of MatrixToken always
	    // have the same type.
	    throw new InternalErrorException("MatrixToken.toArray: Cannot "
	        + "construct ArrayToken. " + illegalAction.getMessage());
	}
        return result;
    }

    /** Return the value of this token as a string that can be parsed
     *  by the expression language to recover a token with the same value.
     *  The expression starts and ends with a square bracket.  The matrix is
     *  scanned starting from the upper left and proceeding across each row.
     *  Each element in the row is separated by a comma, and the end of a row
     *  is represented by a semicolon.  The value of each element is obtained
     *  using its toString method.
     *  @return A String representing a matrix similar to Matlab.
     */
    public String toString() {
        int rowCount = getRowCount();
        int columnCount = getColumnCount();
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < columnCount; j++) {
                sb.append(getElementAsToken(i, j).toString());
                if (j < columnCount - 1) sb.append(", ");
            }
            if (i < rowCount - 1) sb.append("; ");
        }
        sb.append("]");
        return sb.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                      public  variables                    ////

    /** A constant indicating to constructors that contents of an argument 2-D
     *  matrix should be copied. The contents of the input 2-D matrix may be
     *  modified after construction without violating the immutibility of
     *  MatrixTokens.
     */
    public static final int DO_COPY = 0;

    /** A constant indicating to constructors not to copy the contents
     *  of an argument 2-D matrix, but instead to just copy the
     *  pointer to the matrix. The contents of the input 2-D matrix
     *  should NOT be modified after construction of an
     *  instance of MatrixToken, if the property of immutability is
     *  to be preserved.
     */
    public static final int DO_NOT_COPY = 1;
}
