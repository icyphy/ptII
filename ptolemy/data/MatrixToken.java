/* Abstract base class for tokens that contain a 2-D matrix.

 Copyright (c) 1997-2003 The Regents of the University of California.
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

@ProposedRating Green (neuendor@eecs.berkeley.edu)
@AcceptedRating Yellow (wbwu@eecs.berkeley.edu)

*/

package ptolemy.data;

import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.data.type.TypeLattice;
import ptolemy.data.type.UnsizedMatrixType;
import ptolemy.graph.CPO;
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

@author Yuhong Xiong, Steve Neuendorffer
@version $Id$
@since Ptolemy II 0.2
*/
public abstract class MatrixToken extends Token {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a new token whose value is the sum of this token and
     *  the argument. Type conversion also occurs here, so that the
     *  operation is performed at the least type necessary to ensure
     *  precision.  The returned type is the same as the type chosen
     *  for the operation.  Generally, this is higher of the type of
     *  this token and the argument type.  Subclasses should not
     *  generally override this method, but override the protected
     *  _add() method to ensure that type conversion is performed
     *  consistently.
     *  @param rightArgument The token to add to this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token
     *   and this token are of incomparable types, or the operation
     *   does not make sense for the given types.
     */
    public Token add(Token rightArgument) throws IllegalActionException {
        // Get the corresponding element type for this matrix type,
        // and try a scalar operation.
        Type elementType = getElementType();
        int typeInfo = TypeLattice.compare(elementType, rightArgument);

        if (typeInfo == CPO.SAME) {
            Token result = _addElement(rightArgument);
            return result;
        } else if (typeInfo == CPO.HIGHER) {
            Token convertedArgument = elementType.convert(rightArgument);
            try {
                Token result = _addElement(convertedArgument);
                return result;
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a better
                // error message that has the types of the arguments that were
                // passed in.
                throw new IllegalActionException(null, ex,
                        notSupportedMessage("add", this, rightArgument));
            }
        }

        // If we get here, then either our element type is lower than
        // the rightArgument or incomparable to it.
        typeInfo = TypeLattice.compare(getType(), rightArgument);
        if (typeInfo == CPO.SAME) {
            Token result = _doAdd(rightArgument);
            return result;
        } else if (typeInfo == CPO.HIGHER) {
            MatrixToken convertedArgument = (MatrixToken)
                getType().convert(rightArgument);
            try {
                Token result = _doAdd(convertedArgument);
                return result;
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a
                // better error message that has the types of the
                // arguments that were passed in.
                throw new IllegalActionException(null, ex,
                        notSupportedMessage("add", this, rightArgument));
            }
        } else if (typeInfo == CPO.LOWER) {
            Token result = rightArgument.addReverse(this);
            return result;
        } else {
            // Items being added are incomparable.
            // However, addition may still be possible because
            // the LUB of the types might support it. E.g., [double]+complex,
            // where the LUB is [complex].
            Type lubType = (Type)TypeLattice.lattice()
                .leastUpperBound(getType(), rightArgument.getType());
            // If the LUB is a new type, try it.
            if (!lubType.equals(getType())) {
                Token lub = lubType.convert(this);
                // Caution: convert() might return this again, e.g.
                // if lubType is general.  Only proceed if the conversion
                // returned a new type.
                if (!(lub.getType().equals(getType()))) {
                    return lub.add(rightArgument);
                }
            }
            throw new IllegalActionException(
                    notSupportedIncomparableMessage("add",
                            this, rightArgument));
        }
    }

    /** Return a new token whose value is the sum of this token
     *  and the argument. Type resolution also occurs here, with
     *  the returned token type chosen to achieve
     *  a lossless conversion.
     *  @param leftArgument The token to add this token to.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token
     *   is not of a type that can be added to this token, or
     *   the units of this token and the argument token are not the same.
     */
    public Token addReverse(ptolemy.data.Token leftArgument)
            throws IllegalActionException {
        // Get the corresponding element type for this matrix type,
        // and try a scalar operation.
        Type elementType = getElementType();
        int typeInfo = TypeLattice.compare(leftArgument, elementType);

        if (typeInfo == CPO.LOWER) {
            Token convertedArgument = elementType.convert(leftArgument);
            try {
                Token result = _addElement(convertedArgument);
                return result;
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a
                // better error message that has the types of the
                // arguments that were passed in.
                throw new IllegalActionException(null, ex,
                        notSupportedMessage("add", this, leftArgument));
            }
        } else if (typeInfo == CPO.SAME) {
            Token result = _addElement(leftArgument);
            return result;
        }

        // If we get here, then either our element type is lower than
        // the leftArgument or incomparable to it.
        typeInfo = TypeLattice.compare(leftArgument, getType());
        // We would normally expect this to be LOWER, since this will almost
        // always be called by subtract, so put that case first.
        if (typeInfo == CPO.LOWER) {
            MatrixToken convertedArgument = (MatrixToken)
                getType().convert(leftArgument);
            try {
                Token result = convertedArgument._doAdd(this);
                return result;
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a
                // better error message that has the types of the
                // arguments that were passed in.
                throw new IllegalActionException(null, ex,
                        notSupportedMessage("addReverse",
                                this, leftArgument));
            }
        } else if (typeInfo == CPO.SAME) {
            Token result = ((MatrixToken)leftArgument)._doAdd(this);
            return result;
        } else if (typeInfo == CPO.HIGHER) {
            Token result = leftArgument.add(this);
            return result;
        } else {
            // Items being added are incomparable.
            // However, addition may still be possible because
            // the LUB of the types might support it. E.g., complex+[double],
            // where the LUB is [complex].
            Type lubType = (Type)TypeLattice.lattice()
                .leastUpperBound(getType(), leftArgument.getType());
            // If the LUB is a new type, try it.
            if (!lubType.equals(getType())) {
                Token lub = lubType.convert(this);
                // Caution: convert() might return this again, e.g.
                // if lubType is general.  Only proceed if the conversion
                // returned a new type.
                if (!(lub.getType().equals(getType()))) {
                    return lub.addReverse(leftArgument);
                }
            }
            throw new IllegalActionException(
                    notSupportedIncomparableMessage("addReverse",
                            this, leftArgument));
        }
    }

    /** Create a new instance of a MatrixToken subclass with the
     *  given number of rows and columns.  The token will contain
     *  all of the elements of the given array.  The element type
     *  of the matrix token will be the least upper bound of the
     *  types of all of the tokens in the given array.  The size
     *  of the array must be (rows*columns).
     *  @return An instance of a subclass of MatrixToken.
     *  @exception IllegalActionException If no type exists for the
     *   matrix token, or the array of tokens is not the right size, or
     *   the array is null.
     */
    public static MatrixToken arrayToMatrix(
            Token[] tokens, int rows, int columns)
            throws IllegalActionException {
        Object[] typeTerms = new Object[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            typeTerms[i] = tokens[i].getType();
        }

        Type type = (Type)TypeLattice.lattice().leastUpperBound(
                typeTerms);
        
        return arrayToMatrix(type, tokens, rows, columns);
    }

    /** Create a new instance of a MatrixToken subclass with the given
     *  element type and number of rows and columns.  The token will
     *  contain all of the elements of the given array.  The size of
     *  the array must be (rows*columns).
     *  @return An instance of a subclass of MatrixToken.
     *  @exception IllegalActionException If the type is not possible,
     *  or the array of tokens is not the right size, or the array is
     *  null.
     */
    public static MatrixToken arrayToMatrix(Type type,
            Token[] tokens, int rows, int columns)
            throws IllegalActionException {
        MatrixToken token;
        if (type.equals(BaseType.UNKNOWN)) {
            throw new IllegalActionException("Cannot resolve type for "
                    + "matrix construction.");
        } else if (type.equals(BaseType.BOOLEAN)) {
            token = new BooleanMatrixToken(tokens, rows, columns);
        } else if (type.equals(BaseType.INT)) {
            token = new IntMatrixToken(tokens, rows, columns);
        } else if (type.equals(BaseType.LONG)) {
            token = new LongMatrixToken(tokens, rows, columns);
        } else if (type.equals(BaseType.DOUBLE)) {
            token = new DoubleMatrixToken(tokens, rows, columns);
        } else if (type.equals(BaseType.COMPLEX)) {
            token = new ComplexMatrixToken(tokens, rows, columns);
        } else if (type.equals(BaseType.FIX)) {
            token = new FixMatrixToken(tokens, rows, columns);
        } else {
            throw new IllegalActionException("Unrecognized type " + type +
                    " for matrix creation.");
        }
        return token;
    }

    /** Return the (exact) return type of the arrayToMatrix function
     *  above.  If the first argument type is an array type, then
     *  return a matrix type of the same element type, otherwise return
     *  BaseType.UNKNOWN.
     *  @param type1 The type of the first argument to the
     *  corresponding function.
     *  @param type2 The type of the second argument to the
     *  corresponding function.
     *  @param type3 The type of the third argument to the
     *  corresponding function.
     *  @return The type of the value returned from the corresponding function.
     */
    public static Type arrayToMatrixReturnType(
            Type type1, Type type2, Type type3)
            throws IllegalActionException {
        if (type1 instanceof ArrayType) {
            Type elementType = ((ArrayType)type1).getElementType();
            return UnsizedMatrixType.getMatrixTypeForElementType(elementType);
        } else {
            return BaseType.UNKNOWN;
        }
    }

    /** Return a copy of the content of this token as a 2-D Complex matrix.
     *  In this base class, just throw an exception.
     *  @return A 2-D Complex matrix.
     *  @exception IllegalActionException If the token cannot be represented
     *   as requested (always thrown in this base class).
     */
    public Complex[][] complexMatrix() throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedConversionMessage(this, "complex matrix"));
    }

    /** Create a new instance of a MatrixToken subclass with the given number
     *  of rows and columns.  The token will contain all of the elements of the
     *  given array.  The element type of the matrix token will be
     *  the least upper bound of the types of all of the tokens in the given
     *  array.  The size of the array must be (rows*columns).
     *  @return An instance of a subclass of MatrixToken.
     *  @exception IllegalActionException If no type exists for the
     *  matrix token, or the array of tokens is not the right size, or
     *  the array is null.
     *  @deprecated Use arrayToMatrix() instead.
     */
    public static MatrixToken create(Token[] tokens, int rows, int columns)
            throws IllegalActionException {
        return arrayToMatrix(tokens, rows, columns);
    }

    /** Create an array of tokens of the given length.  The first
     *  token in the sequence will have value start and each succeeding
     *  token will have the value created by adding the given increment
     *  to the preceding token.
     *  @param start The value of the first element of the returned array.
     *  @param increment The difference between elements in the array.
     *  @param length The size of the array to create, which must not be
     *  negative.
     *  @exception IllegalActionException If the length is negative, or
     *  tokens of the given type cannot be added together.
     */
    public static Token[] createSequence(
            Token start, Token increment, int length)
            throws IllegalActionException {
        Token[] result = new Token[length];
        Token value = start;
        if (length > 0) {
            result[0] = start;
            for (int i = 1; i < length; i++) {
                value = value.add(increment);
                result[i] = value;
            }
        }
        return result;
    }

    /** Return the (exact) return type of the createSequence function
     *  above.  The return type is an array type whose element type is
     *  the least upper bound of the first two types.
     *  @param type1 The type of the first argument to the
     *  corresponding function.
     *  @param type2 The type of the second argument to the
     *  corresponding function.
     *  @param type3 The type of the third argument to the
     *  corresponding function.
     *  @return The type of the value returned from the corresponding function.
     */
    public static Type createSequenceReturnType(
            Type type1, Type type2, Type type3)
            throws IllegalActionException {
        return new ArrayType(TypeLattice.leastUpperBound(type1, type2));
    }

    /** Create an array of tokens of the given length.  The first
     *  token in the sequence will have value start and each succeeding
     *  token will have the value created by adding the given increment
     *  to the preceding token.
     *  @param start The value of the first element of the returned array.
     *  @param increment The difference between elements in the array.
     *  @param length The size of the array to create, which must not be
     *  negative.
     *  @exception IllegalActionException If the length is negative, or
     *  tokens of the given type cannot be added together.
     *  @deprecated Use createSequence() instead.
     */
    public static Token[] createTokenSequence(
            Token start, Token increment, int length)
            throws IllegalActionException {
        return createSequence(start, increment, length);
    }

    /** Count the number of tokens differing by the given increment
     *  that fit in a range between the given start token and the
     *  given end token.  This computes, type polymorphically, the
     *  value floor(((end-start)/increment) + 1).  If the size of the
     *  sequence cannot be determined, because the increment is zero,
     *  or the increment is positive and end is greater than start, or
     *  the increment is negative and start is greater than end,
     *  then an exception will be thrown.
     *  @return The size of the sequence, which will be greater than zero.
     *  @exception IllegalActionException If the length of the
     *  sequence cannot be determined, or the tokens provided do not
     *  support the zero, isEqualTo, isLessThan, add, divide, or
     *  subtract operations, or is not convertible to a double token.
     */
    public static int determineSequenceLength(
            ScalarToken start, ScalarToken increment, ScalarToken end)
            throws IllegalActionException {
        ScalarToken zero = (ScalarToken)increment.zero();
        if (increment.isEqualTo(increment.zero()).booleanValue()) {
            throw new IllegalActionException("Sequence length cannot " +
                    "be determined because the increment is zero.");
        } else if (increment.isLessThan(zero).booleanValue() &&
                start.isLessThan(end).booleanValue()) {
            throw new IllegalActionException("Sequence length cannot " +
                    "be determined because the increment has the wrong sign.");
        } else if (zero.isLessThan(increment).booleanValue() &&
                end.isLessThan(start).booleanValue()) {
            throw new IllegalActionException("Sequence length cannot " +
                    "be determined because the increment has the wrong sign.");
        } else {
            ScalarToken diff =
                (ScalarToken)end.subtract(start).divide(increment);
            int count;
            // UGH...  I don't see how to abstract this nicely...
            if (diff instanceof LongToken) {
                count = ((int)diff.longValue()) + 1;
            } else if (diff instanceof DoubleToken) {
                count = ((int)diff.doubleValue()) + 1;
            } else {
                count = diff.intValue() + 1;
            }
            if (count < 1) {
                throw new InternalErrorException(
                        "The determined count does not make sense.");
            }
            return count;
        }
    }

    /** Return a new token whose value is the value of this token
     *  divided by the value of the argument token.  Division is not
     *  supported for matrices, so this throws an exception if the
     *  argument is a matrix. However, if it is a scalar, then division
     *         is performed elementwise.
     *  @param rightArgument The token that divides this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the operation
     *   does not make sense for the given types.
     */
    public final Token divide(Token rightArgument)
            throws IllegalActionException {
        // Get the corresponding element type for this matrix type,
        // and try a scalar operation.
        Type elementType = getElementType();
        int typeInfo = TypeLattice.compare(elementType, rightArgument);

        if (typeInfo == CPO.SAME) {
            Token result = _divideElement(rightArgument);
            return result;
        } else if (typeInfo == CPO.HIGHER) {
            Token convertedArgument = elementType.convert(rightArgument);
            try {
                Token result = _divideElement(convertedArgument);
                return result;
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a better
                // error message that has the types of the arguments that were
                // passed in.
                throw new IllegalActionException(null, ex,
                        notSupportedMessage("divide", this, rightArgument));
            }
        }

        // If we get here, then either our element type is lower than
        // the rightArgument or incomparable to it.

        typeInfo = TypeLattice.compare(getType(), rightArgument);
        if (typeInfo == CPO.INCOMPARABLE) {
            // Items being added are incomparable.
            // However, division may still be possible because
            // the LUB of the types might support it. E.g., [double]/complex,
            // where the LUB is [complex].
            Type lubType = (Type)TypeLattice.lattice()
                .leastUpperBound(getType(), rightArgument.getType());
            // If the LUB is a new type, try it.
            if (!lubType.equals(getType())) {
                Token lub = lubType.convert(this);
                // Caution: convert() might return this again, e.g.
                // if lubType is general.  Only proceed if the conversion
                // returned a new type.
                if (!(lub.getType().equals(getType()))) {
                    return lub.divide(rightArgument);
                }
            }
        }
        throw new IllegalActionException(
                notSupportedMessage("divide", this, rightArgument));
    }

    /** Return a new token whose value is the value of the argument
     *  token divided by the value of this token.  Division is not
     *  supported for matrices, so this always throws an exception.
     *  @param leftArgument The token to be divided by the value of this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token and
     *  this token are of incomparable types, or the operation does
     *  not make sense for the given types.
     */
    public Token divideReverse(Token leftArgument)
            throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedMessage("divideReverse", this, leftArgument));
    }

    /** Return the content of this token as a 2-D double matrix.
     *  In this base class, just throw an exception.
     *  @return A 2-D double matrix.
     *  @exception IllegalActionException If the token cannot be represented
     *   as requested (always thrown in this base class).
     */
    public double[][] doubleMatrix() throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedConversionMessage(this, "double matrix"));
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

    /** Return the Type of the tokens contained in this matrix token.
     *  @return A Type.
     */
    public abstract Type getElementType();

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
        throw new IllegalActionException(
                notSupportedConversionMessage(this, "int matrix"));
    }

    /** Test whether the value of this Token is close to the argument
     *  Token.  In this base class, we call isEqualTo() and the
     *  epsilon argument is ignored.  This method should be overridden
     *  in derived classes such as DoubleToken and ComplexToken to
     *  provide type specific actions for equality testing using the
     *  epsilon argument
     *
     *  @see #isEqualTo
     *  @param rightArgument The token to test closeness of this token with.
     *  @param epsilon The value that we use to determine whether two
     *  tokens are close.  In this base class, the epsilon argument is
     *  ignored.
     *  @return a boolean token that contains the value true if the
     *   value and units of this token are close to those of the argument
     *   token.
     *  @exception IllegalActionException If the argument token is
     *   not of a type that can be compared with this token.
     */
    public final BooleanToken isCloseTo(Token rightArgument, double epsilon)
            throws IllegalActionException {
        // Note that if we had absolute(), subtraction() and islessThan()
        // we could perhaps define this method for all tokens.  However,
        // Precise classes like IntToken not bother doing the absolute(),
        // subtraction(), and isLessThan() method calls and should go
        // straight to isEqualTo().  Also, these methods might introduce
        // exceptions because of type conversion issues.
        int typeInfo = TypeLattice.compare(getType(), rightArgument);
        if (typeInfo == CPO.SAME) {
            return _doIsCloseTo(rightArgument, epsilon);
        } else if (typeInfo == CPO.HIGHER) {
            MatrixToken convertedArgument = (MatrixToken)
                getType().convert(rightArgument);
            try {
                return _doIsCloseTo(convertedArgument, epsilon);
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a better
                // error message that has the types of the arguments that were
                // passed in.
                throw new IllegalActionException(null, null, ex,
                        notSupportedMessage("isCloseTo", this, rightArgument));
            }
        } else if (typeInfo == CPO.LOWER) {
            return rightArgument.isCloseTo(this, epsilon);
        } else {
            throw new IllegalActionException(
                    notSupportedIncomparableMessage("isCloseTo",
                            this, rightArgument));
        }
    }

    /** Test for equality of the values of this Token and the argument
     *  Token.  The argument and this token are converted to
     *  equivalent types, and then compared.  Generally, this is the
     *  higher of the type of this token and the argument type.  This
     *  method defers to the _isEqualTo method to perform a
     *  type-specific equality check.  Derived classes should override
     *  that method to provide type specific actions for equality
     *  testing.
     *
     *  @see #isCloseTo
     *  @param rightArgument The token with which to test equality.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A BooleanToken which contains the result of the test.
     */
    public final BooleanToken isEqualTo(Token rightArgument)
            throws IllegalActionException {
        int typeInfo = TypeLattice.compare(getType(), rightArgument);
        if (typeInfo == CPO.SAME) {
            return _doIsEqualTo(rightArgument);
        } else if (typeInfo == CPO.HIGHER) {
            MatrixToken convertedArgument = (MatrixToken)
                getType().convert(rightArgument);
            try {
                return _doIsEqualTo(convertedArgument);
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a better
                // error message that has the types of the arguments that were
                // passed in.
                throw new IllegalActionException(null, ex,
                        notSupportedMessage("isEqualTo", this, rightArgument));
            }
        } else if (typeInfo == CPO.LOWER) {
            return rightArgument.isEqualTo(this);
        } else {
            throw new IllegalActionException(
                    notSupportedIncomparableMessage("isEqualTo",
                            this, rightArgument));
        }
    }

    /** Return the content of this matrix as a 2-D long matrix.
     *  In this base class, just throw an exception.
     *  @return A 2-D long matrix.
     *  @exception IllegalActionException If the token cannot be represented
     *   as requested (always thrown in this base class).
     */
    public long[][] longMatrix() throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedConversionMessage(this, "long matrix"));
    }

    /** Create a new instance of ArrayToken that contains the values
     *  in the specified matrix.
     *  @return An array.
     */
    public static ArrayToken matrixToArray(MatrixToken matrix) {
        return matrix.toArray();
    }

    /** Return the (exact) return type of the toArray function above.  If the
     *  argument is a matrix type, then return an array type of its
     *  element type, otherwise return BaseType.UNKNOWN.
     *  @param type The type of the argument to the corresponding function.
     *  @return The type of the value returned from the corresponding function.
     */
    public static Type matrixToArrayReturnType(Type type) {
        return toArrayReturnType(type);
    }

    /** Return a new token whose value is this token
     *  modulo the value of the argument token.
     *  @param rightArgument The token that performs modulo on this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the operation
     *   does not make sense for the given types.
     */
    public final Token modulo(Token rightArgument)
            throws IllegalActionException {

        /*
          // If the rightArgument is a complex token, throw an error
          // message as modulo can't be operated.
          if (rightArgument instanceof ComplexToken)
          throw new IllegalActionException(
          notSupportedMessage("modulo", this, rightArgument));
        */

        // Get the corresponding element type for this matrix type,
        // and try a scalar operation.
        Type elementType = getElementType();

        /*
          // If this is a complex array, throw an error message as modulo
          // can't be performed on it
          if (elementType == BaseType.COMPLEX)
          throw new IllegalActionException(
          notSupportedMessage("modulo", this, rightArgument));
        */

        int typeInfo = TypeLattice.compare(elementType, rightArgument);
        if (typeInfo == CPO.SAME) {
            Token result = _moduloElement(rightArgument);
            return result;
        } else if (typeInfo == CPO.HIGHER) {
            Token convertedArgument = elementType.convert(rightArgument);
            try {
                Token result = _moduloElement(convertedArgument);
                return result;
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a better
                // error message that has the types of the arguments that were
                // passed in.
                throw new IllegalActionException(null, ex,
                        notSupportedMessage("modulo", this, rightArgument));
            }
        }

        // If we get here, then either our element type is lower than
        // the rightArgument or incomparable to it.

        typeInfo = TypeLattice.compare(getType(), rightArgument);
        if (typeInfo == CPO.INCOMPARABLE) {
            // Items being added are incomparable.
            // However, division may still be possible because
            // the LUB of the types might support it. E.g., [double]/complex,
            // where the LUB is [complex].
            Type lubType = (Type)TypeLattice.lattice()
                .leastUpperBound(getType(), rightArgument.getType());
            // If the LUB is a new type, try it.
            if (!lubType.equals(getType())) {
                Token lub = lubType.convert(this);
                // Caution: convert() might return this again, e.g.
                // if lubType is general.  Only proceed if the conversion
                // returned a new type.
                if (!(lub.getType().equals(getType()))) {
                    return lub.modulo(rightArgument);
                }
            }
        }
        throw new IllegalActionException(
                notSupportedMessage("modulo",
                        this, rightArgument));
    }

    /** Return a new token whose value is the value of the argument token
     *  modulo the value of this token.  Since modulo is not
     *  supported for matrices, this always throws an exception.
     *  @param leftArgument The token to apply modulo to by the value
     *  of this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the the operation does
     *  not make sense for the given types.
     */
    public final Token moduloReverse(Token leftArgument)
            throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedMessage("moduloReverse",
                        this, leftArgument));
    }

    /** Return a new token whose value is the value of this token
     *  multiplied by the value of the argument token.  Type
     *  conversion also occurs here, so that the operation is
     *  performed at the least type necessary to ensure precision.
     *  The returned type is the same as the type chosen for the
     *  operation.  Generally, this is higher of the type of this
     *  token and the argument type.  This class overrides the base
     *  class to perform conversion from scalars to matrices
     *  appropriately for matrix multiplication.  Subclasses should
     *  not generally override this method, but override the protected
     *  _multiply() method to ensure that type conversion is performed
     *  consistently.
     *  @param rightArgument The token to multiply this token by.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token
     *   and this token are of incomparable types, or the operation
     *   does not make sense for the given types.
     */
    public final Token multiply(Token rightArgument)
            throws IllegalActionException {
        // UnsizedMatrixType type = (UnsizedMatrixType)getType();
        // Get the corresponding element type for this matrix type,
        // and try a scalar operation.
        Type elementType = getElementType();
        int typeInfo = TypeLattice.compare(elementType, rightArgument);

        if (typeInfo == CPO.SAME) {
            Token result = _multiplyElement(rightArgument);
            return result;
        } else if (typeInfo == CPO.HIGHER) {
            Token convertedArgument = elementType.convert(rightArgument);
            try {
                Token result = _multiplyElement(convertedArgument);
                return result;
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a better
                // error message that has the types of the arguments that were
                // passed in.
                throw new IllegalActionException(null, ex,
                        notSupportedMessage("multiply", this, rightArgument));
            }
        }

        // Argument must be a matrix or incomparable.
        typeInfo = TypeLattice.compare(getType(), rightArgument);
        if (typeInfo == CPO.SAME) {
            Token result = _doMultiply(rightArgument);
            return result;
        } else if (typeInfo == CPO.HIGHER) {
            MatrixToken convertedArgument = (MatrixToken)
                getType().convert(rightArgument);
            try {
                Token result = _doMultiply(convertedArgument);
                return result;
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a
                // better error message that has the types of the
                // arguments that were passed in.
                throw new IllegalActionException(null, ex,
                        notSupportedMessage("multiply", this, rightArgument));
            }
        } else if (typeInfo == CPO.LOWER) {
            Token result = rightArgument.multiplyReverse(this);
            return result;
        } else {
            // Items being multiplied are incomparable.
            // However, multiplication may still be possible because
            // the LUB of the types might support it. E.g., [double]*complex,
            // where the LUB is [complex].
            Type lubType = (Type)TypeLattice.lattice()
                .leastUpperBound(getType(), rightArgument.getType());
            // If the LUB is a new type, try it.
            if (!lubType.equals(getType())) {
                Token lub = lubType.convert(this);
                // Caution: convert() might return this again, e.g.
                // if lubType is general.  Only proceed if the conversion
                // returned a new type.
                if (!(lub.getType().equals(getType()))) {
                    return lub.multiply(rightArgument);
                }
            }
            throw new IllegalActionException(
                    notSupportedIncomparableMessage("multiply",
                            this, rightArgument));
        }
    }

    /** Return a new token whose value is the value of the argument token
     *  multiplied by the value of this token.
     *  Type resolution also occurs here, with the returned token
     *  type chosen to achieve a lossless conversion.
     *  @param leftArgument The token to be multiplied by the value of
     *   this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token
     *   is not of a type that can be multiplied by this token.
     */
    public final Token multiplyReverse(Token leftArgument)
            throws IllegalActionException {
        // Get the corresponding element type for this matrix type,
        // and try a scalar operation.
        Type elementType = getElementType();
        int typeInfo = TypeLattice.compare(leftArgument, elementType);

        if (typeInfo == CPO.LOWER) {
            Token convertedArgument = elementType.convert(leftArgument);
            try {
                Token result = _multiplyElement(convertedArgument);
                return result;
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a
                // better error message that has the types of the
                // arguments that were passed in.
                throw new IllegalActionException(null, ex,
                        notSupportedMessage("multiply", this, leftArgument));
            }
        } else if (typeInfo == CPO.SAME) {
            Token result = _multiplyElement(leftArgument);
            return result;
        }

        // Must be a matrix or incomparable.
        typeInfo = TypeLattice.compare(leftArgument, getType());
        // We would normally expect this to be LOWER, since this will almost
        // always be called by subtract, so put that case first.
        if (typeInfo == CPO.LOWER) {
            MatrixToken convertedArgument = (MatrixToken)
                getType().convert(leftArgument);
            try {
                Token result = convertedArgument._doMultiply(this);
                return result;
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a
                // better error message that has the types of the
                // arguments that were passed in.
                throw new IllegalActionException(null, ex,
                        notSupportedMessage("multiplyReverse",
                                this, leftArgument));
            }
        } else if (typeInfo == CPO.SAME) {
            Token result = ((MatrixToken)leftArgument)._doMultiply(this);
            return result;
        } else if (typeInfo == CPO.HIGHER) {
            Token result = leftArgument.multiply(this);
            return result;
        } else {
            // Items being multiplied are incomparable.
            // However, multiplication may still be possible because
            // the LUB of the types might support it. E.g., [double]*complex,
            // where the LUB is [complex].
            Type lubType = (Type)TypeLattice.lattice()
                .leastUpperBound(getType(), leftArgument.getType());
            // If the LUB is a new type, try it.
            if (!lubType.equals(getType())) {
                Token lub = lubType.convert(this);
                // Caution: convert() might return this again, e.g.
                // if lubType is general.  Only proceed if the conversion
                // returned a new type.
                if (!(lub.getType().equals(getType()))) {
                    return lub.multiplyReverse(leftArgument);
                }
            }
            throw new IllegalActionException(
                    notSupportedIncomparableMessage("multiplyReverse",
                            leftArgument, this));
        }
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

    /** Return a new token whose value is the value of the argument token
     *  subtracted from the value of this token.   Type conversion
     *  also occurs here, so that the operation is performed at the
     *  least type necessary to ensure precision.  The returned type
     *  is the same as the type chosen for the operation.  Generally,
     *  this is higher of the type of this token and the argument
     *  type.  Subclasses should not override this method,
     *  but override the protected _subtract() method to ensure that type
     *  conversion is performed consistently.
     *  @param rightArgument The token to subtract from this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token
     *   and this token are of incomparable types, or the operation
     *   does not make sense for the given types.
     */
    public final Token subtract(Token rightArgument)
            throws IllegalActionException {
        // Get the corresponding element type for this matrix type,
        // and try a scalar operation.
        Type elementType = getElementType();
        int typeInfo = TypeLattice.compare(elementType, rightArgument);

        if (typeInfo == CPO.SAME) {
            Token result = _subtractElement(rightArgument);
            return result;
        } else if (typeInfo == CPO.HIGHER) {
            Token convertedArgument = elementType.convert(rightArgument);
            try {
                Token result = _subtractElement(convertedArgument);
                return result;
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a better
                // error message that has the types of the arguments that were
                // passed in.
                throw new IllegalActionException(null, ex,
                        notSupportedMessage("subtract", this, rightArgument));
            }
        }

        // If we get here, then either our element type is lower than
        // the rightArgument or incomparable to it.
        typeInfo = TypeLattice.compare(getType(), rightArgument);
        if (typeInfo == CPO.SAME) {
            Token result = _doSubtract(rightArgument);
            return result;
        } else if (typeInfo == CPO.HIGHER) {
            MatrixToken convertedArgument = (MatrixToken)
                getType().convert(rightArgument);
            try {
                Token result = _doSubtract(convertedArgument);
                return result;
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a
                // better error message that has the types of the
                // arguments that were passed in.
                throw new IllegalActionException(null, ex,
                        notSupportedMessage("subtract", this, rightArgument));
            }
        } else if (typeInfo == CPO.LOWER) {
            Token result = rightArgument.subtractReverse(this);
            return result;
        } else {
            // Items being subracted are incomparable.
            // However, subtraction may still be possible because
            // the LUB of the types might support it. E.g., [double]-complex,
            // where the LUB is [complex].
            Type lubType = (Type)TypeLattice.lattice()
                .leastUpperBound(getType(), rightArgument.getType());
            // If the LUB is a new type, try it.
            if (!lubType.equals(getType())) {
                Token lub = lubType.convert(this);
                // Caution: convert() might return this again, e.g.
                // if lubType is general.  Only proceed if the conversion
                // returned a new type.
                if (!(lub.getType().equals(getType()))) {
                    return lub.subtract(rightArgument);
                }
            }
            throw new IllegalActionException(
                    notSupportedIncomparableMessage("subtract",
                            this, rightArgument));
        }
    }

    /** Return a new token whose value is the value of this token
     *  subtracted from the value of the argument token.
     *  Type resolution also occurs here, with the returned token type
     *  chosen to achieve a lossless conversion.
     *  @param leftArgument The token to subtract this token from.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token is not
     *  of a type that can be subtracted to this token, or the units
     *  of this token and the argument token are not the same.
     */
    public final Token subtractReverse(Token leftArgument)
            throws IllegalActionException {
        // Get the corresponding element type for this matrix type,
        // and try a scalar operation.
        Type elementType = getElementType();
        int typeInfo = TypeLattice.compare(leftArgument, elementType);

        if (typeInfo == CPO.LOWER) {
            Token convertedArgument = elementType.convert(leftArgument);
            try {
                Token result = _subtractElementReverse(convertedArgument);
                return result;
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a
                // better error message that has the types of the
                // arguments that were passed in.
                throw new IllegalActionException(null, ex,
                        notSupportedMessage("subtract", this, leftArgument));
            }
        } else if (typeInfo == CPO.SAME) {
            Token result = _subtractElementReverse(leftArgument);
            return result;
        }

        // If we get here, then either our element type is lower than
        // the rightArgument or incomparable to it.
        typeInfo = TypeLattice.compare(leftArgument, getType());
        // We would normally expect this to be LOWER, since this will almost
        // always be called by subtract, so put that case first.
        if (typeInfo == CPO.LOWER) {
            MatrixToken convertedArgument = (MatrixToken)
                getType().convert(leftArgument);
            try {
                Token result = convertedArgument._doSubtract(this);
                return result;
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a
                // better error message that has the types of the
                // arguments that were passed in.
                throw new IllegalActionException(null, ex,
                        notSupportedMessage("subtractReverse",
                                this, leftArgument));
            }
        } else if (typeInfo == CPO.SAME) {
            Token result = ((MatrixToken)leftArgument)._doSubtract(this);
            return result;
        } else if (typeInfo == CPO.HIGHER) {
            Token result = leftArgument.subtract(this);
            return result;
        } else {
            // Items being subtracted are incomparable.
            // However, subtraction may still be possible because
            // the LUB of the types might support it. E.g., complex-[double],
            // where the LUB is [complex].
            Type lubType = (Type)TypeLattice.lattice()
                .leastUpperBound(getType(), leftArgument.getType());
            // If the LUB is a new type, try it.
            if (!lubType.equals(getType())) {
                Token lub = lubType.convert(this);
                // Caution: convert() might return this again, e.g.
                // if lubType is general.  Only proceed if the conversion
                // returned a new type.
                if (!(lub.getType().equals(getType()))) {
                    return lub.subtractReverse(leftArgument);
                }
            }
            throw new IllegalActionException(
                    notSupportedIncomparableMessage("subtractReverse",
                            this, leftArgument));
        }
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

    /** Return the (exact) return type of the toArray function above.  If the
     *  argument is a matrix type, then return an array type of its
     *  element type, otherwise return BaseType.UNKNOWN.
     *  @param type The type of the argument to the corresponding function.
     *  @return The type of the value returned from the corresponding function.
     */
    public static Type toArrayReturnType(Type type) {
        if (type instanceof UnsizedMatrixType) {
            Type elementType = ((UnsizedMatrixType)type).getElementType();
            return new ArrayType(elementType);
        } else {
            return BaseType.UNKNOWN;
        }
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
     *  modified after construction without violating the immutability of
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

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a new token whose value is the value of the argument
     *  Token added to the value of this Token.  It is guaranteed by
     *  the caller that the type of the argument is the same as the
     *  type of this class, and that the matrices have appropriate
     *  dimensions.  This method should be overridden in derived
     *  classes to provide type-specific operation and return a token
     *  of the appropriate subclass.
     *  @param rightArgument The token to add to this token.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
     */
    protected MatrixToken _add(MatrixToken rightArgument)
            throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedMessage("add", this, rightArgument));
    }

    /** Return a new token whose value is the value of the argument
     *  Token added to the value of each element of this Token. It is
     *  guaranteed by the caller that the type of the argument
     *  is the same as the type of each element of this class.
     *  @param rightArgument The token to add to this token.
     *  @exception IllegalActionException If this operation is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
     */
    protected MatrixToken _addElement(Token rightArgument)
            throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedMessage("add", this, rightArgument));
    }

    /** Return a new token whose elements are the result of dividing
     *  the elements of this token by the argument. It is
     *  guaranteed by the caller that the type of the argument
     *  is the same as the type of each element of this class.
     *  @param rightArgument The token that divides this token.
     *  @exception IllegalActionException If this operation is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
     */
    protected MatrixToken _divideElement(Token rightArgument)
            throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedMessage("divide", this, rightArgument));
    }

    /** Test whether the value of this token is close to the first
     *  argument, where "close" means that the distance between their
     *  elements is less than or equal to the second argument. This
     *  base class delegates to the token class corresponding to the
     *  matrix elements, using its isCloseTo() method. If the two
     *  matrices do have the same dimension, then this method returns
     *  false.
     *
     *  @param token The matrix token to compare to this token.
     *  @param distance The specified distance.
     *  @return True if every element of the specified matrix is within
     *  the specified distance of the corresponding element of this matrix.
     *  @exception IllegalActionException If a derived class throws it.
     */
    protected BooleanToken _isCloseTo(
            MatrixToken token, double distance)
            throws IllegalActionException {
        int rows = getRowCount();
        int columns = getColumnCount();
        if (token.getRowCount() != rows || token.getColumnCount() != columns) {
            return BooleanToken.FALSE;
        }
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                Token element = getElementAsToken(i, j);
                Token other = token.getElementAsToken(i, j);
                if (!element.isCloseTo(other, distance).booleanValue()) {
                    return BooleanToken.FALSE;
                }
            }
        }
        return BooleanToken.TRUE;
    }

    /** Test for equality of the values of this Token and the argument
     *  Token.  It is guaranteed by the caller that the type and
     *  dimensions of the argument is the same as the type of this
     *  class.  This method can be overridden in derived classes to
     *  provide type-specific operation and return a token of the
     *  appropriate subclass.  In this base class, this method
     *         delegates to equals().
     *  @param rightArgument The token to compare to this token.
     *  @exception IllegalActionException If this method is not
     *   supported by a derived class.
     *  @return A true-valued token if the argument is equal to this
     *          matrix.
     */
    protected BooleanToken _isEqualTo(MatrixToken rightArgument)
            throws IllegalActionException {
        return BooleanToken.getInstance(equals(rightArgument));
    }

    /** Return a new token whose elements are the modulo of
     *  the elements of this token by the argument. It is
     *  guaranteed by the caller that the type of the argument
     *  is the same as the type of each element of this class.
     *  @param rightArgument The token that performs modulo on this token.
     *  @exception IllegalActionException If this operation is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
     */
    protected MatrixToken _moduloElement(Token rightArgument)
            throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedMessage("modulo", this, rightArgument));
    }

    /** Return a new token whose value is the value of this token
     *  multiplied by the value of the argument token.  It is
     *  guaranteed by the caller that the type of the argument is the
     *  same as the type of this class, and that the matrices have
     *  appropriate dimensions.  This method should be overridden in
     *  derived classes to provide type-specific operation and return
     *  a token of the appropriate subclass.
     *  @param rightArgument The token to multiply this token by.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
     */
    protected MatrixToken _multiply(MatrixToken rightArgument)
            throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedMessage("multiply", this, rightArgument));
    }

    /** Return a new token whose value is the value of this token
     *  multiplied by the value of the argument scalar token.
     *  This method should be overridden in derived
     *  classes to provide type specific actions for multiply.
     *  @param rightArgument The token to multiply this token by.
     *  @exception IllegalActionException If this method is not
     *   supported by the derived class.
     *  @return A new Token containing the result that is of the same class
     *  as this token.
     */
    protected MatrixToken _multiplyElement(Token rightArgument)
            throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedMessage("multiply", this, rightArgument));
    }

    /** Return a new token whose value is the value of the argument
     *  token subtracted from the value of this token.  It is
     *  guaranteed by the caller that the type of the argument is the
     *  same as the type of this class, and that the matrices have
     *  appropriate dimensions.  This method should be overridden in
     *  derived classes to provide type-specific operation and return
     *  a token of the appropriate subclass.
     *  @param rightArgument The token to subtract from this token.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
     */
    protected MatrixToken _subtract(MatrixToken rightArgument)
            throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedMessage("subtract", this, rightArgument));
    }

    /** Return a new token whose value is the value of the argument
     *  Token subtracted from the value of each element of this
     *  Token. It is guaranteed by the caller that the type of the
     *  argument is the same as the type of each element of this
     *  class.
     *  @param rightArgument The token to subtract from this token.
     *  @exception IllegalActionException If this operation is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
     */
    protected MatrixToken _subtractElement(Token rightArgument)
            throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedMessage("subtract", this, rightArgument));
    }

    /** Return a new token whose value is the value of each element of
     *  this Token subtracted from the value the argument Token. It is
     *  guaranteed by the caller that the type of the argument is the
     *  same as the type of each element of this class.
     *  @param rightArgument The token to subtract this token from.
     *  @exception IllegalActionException If this operation is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
     */
    protected MatrixToken _subtractElementReverse(Token rightArgument)
            throws IllegalActionException {
        throw new IllegalActionException(
                notSupportedMessage("subtract", this, rightArgument));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return a new token whose value is the value of the argument
     *  Token added to the value of this Token. It is guaranteed by
     *  the caller that the type of the argument is the same as the
     *  type of this class.  This method defers to the _add method
     *  that takes a MatrixToken.  Derived classes should override
     *  that method instead to provide type-specific operation.
     *  @param rightArgument The token to add to this token.
     *  @exception IllegalActionException If the matrix dimensions are
     *  not compatible, or this operation is not supported by the
     *  derived class.
     *  @return A new Token containing the result.
     */
    private Token _doAdd(Token rightArgument)
            throws IllegalActionException {
        MatrixToken convertedArgument = (MatrixToken)rightArgument;
        if (convertedArgument.getRowCount() != getRowCount() ||
                convertedArgument.getColumnCount() != getColumnCount()) {
            throw new IllegalActionException(
                    Token.notSupportedMessage("add", this, rightArgument)
                    + " because the matrices have different dimensions.");
        }
        MatrixToken result = _add(convertedArgument);
        return result;
    }

    /** Test for closeness of the values of this Token and the
     *  argument Token.  It is guaranteed by the caller that the type
     *  of the argument is the same as the type of this class.  This
     *  class overrides the base class to return BooleanToken.FALSE if
     *  the dimensions of this token and the given token are not
     *  identical.  This method may defer to the _isEqualTo method
     *  that takes a MatrixToken.  Derived classes should override
     *  that method instead to provide type-specific operation.
     *  @param rightArgument The token with which to test equality.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A BooleanToken which contains the result of the test.
     */
    private BooleanToken _doIsCloseTo(
            Token rightArgument, double epsilon)
            throws IllegalActionException {
        MatrixToken convertedArgument = (MatrixToken)rightArgument;
        if (convertedArgument.getRowCount() != getRowCount() ||
                convertedArgument.getColumnCount() != getColumnCount()) {
            return BooleanToken.FALSE;
        }

        return _isCloseTo(convertedArgument, epsilon);
    }

    /** Test for equality of the values of this Token and the argument
     *  Token.  It is guaranteed by the caller that the type of the
     *  argument is the same as the type of this class.  This class
     *  overrides the base class to return BooleanToken.FALSE if the
     *  dimensions of this token and the given token are not
     *  identical.  This method may defer to the _isEqualTo method
     *  that takes a MatrixToken.  Derived classes should override
     *  that method instead to provide type-specific operation.
     *  @param rightArgument The token with which to test equality.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A BooleanToken which contains the result of the test.
     */
    private BooleanToken _doIsEqualTo(Token rightArgument)
            throws IllegalActionException {
        MatrixToken convertedArgument = (MatrixToken)rightArgument;
        if (convertedArgument.getRowCount() != getRowCount() ||
                convertedArgument.getColumnCount() != getColumnCount()) {
            return BooleanToken.FALSE;
        }

        return _isEqualTo(convertedArgument);
    }

    /** Return a new token whose value is the value of this token
     *  multiplied by the value of the argument token.  It is
     *  guaranteed by the caller that the type of the argument is the
     *  same as the type of this class.  This method defers to the
     *  _multiply method that takes a MatrixToken.  Derived classes
     *  should override that method instead to provide type-specific
     *  operation.
     *  @param rightArgument The token to multiply this token by.
     *  @exception IllegalActionException If the matrix dimensions are
     *  not compatible, or this operation is not supported by the
     *  derived class.
     *  @return A new Token containing the result.
     */
    private Token _doMultiply(Token rightArgument)
            throws IllegalActionException {
        MatrixToken convertedArgument = (MatrixToken)rightArgument;
        if (convertedArgument.getRowCount() != getColumnCount()) {
            throw new IllegalActionException(
                    Token.notSupportedMessage("multiply", this, rightArgument)
                    + " because the matrices have incompatible dimensions.");

        }
        MatrixToken result = _multiply(convertedArgument);
        return result;
    }

    /** Return a new token whose value is the value of the argument
     *  token subtracted from the value of this token.  It is
     *  guaranteed by the caller that the type of the argument is the
     *  same as the type of this class and has the same units as this
     *  token.  This method defers to the _subtract method that takes
     *  a MatrixToken.  Derived classes should override that method
     *  instead to provide type-specific operation.
     *  @param rightArgument The token to subtract from this token.
     *  @exception IllegalActionException If the matrix dimensions are
     *  not compatible, or this operation is not supported by the
     *  derived class.
     *  @return A new Token containing the result.
     */
    private Token _doSubtract(Token rightArgument)
            throws IllegalActionException {
        MatrixToken convertedArgument = (MatrixToken)rightArgument;
        if (convertedArgument.getRowCount() != getRowCount() ||
                convertedArgument.getColumnCount() != getColumnCount()) {
            throw new IllegalActionException(
                    Token.notSupportedMessage("subtract", this, rightArgument)
                    + " because the matrices have different dimensions.");
        }

        MatrixToken result = _subtract(convertedArgument);
        return result;
    }


}
