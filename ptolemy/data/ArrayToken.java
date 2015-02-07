/* A token that contains an array of tokens.

 Copyright (c) 1997-2014 The Regents of the University of California.
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

import java.util.LinkedList;
import java.util.List;

import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.expr.PtParser;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.data.type.TypeLattice;
import ptolemy.graph.CPO;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// ArrayToken

/**
 A token that contains an array of tokens.  The operations between
 arrays are defined pointwise, and require that the lengths of the
 arrays are the same.  The elements of the ArrayToken will be
 converted to the least upper bound of their input types.  Zero length
 array tokens are supported, given a prototype element to determine
 the type of the array.

 @author Yuhong Xiong, Steve Neuendorffer, Contributor: Christopher Brooks
 @version $Id$
 @since Ptolemy II 0.4
 @Pt.ProposedRating Yellow (cxh)
 @Pt.AcceptedRating Red (cxh) nil token code
 */
public class ArrayToken extends AbstractNotConvertibleToken {
    /** Construct an ArrayToken with the specified token array.
     *  The type of the resulting array is the least upper bound
     *  of the types of the elements.
     *  This class makes a copy of the given array, so
     *  the passed array may be reused.  Note that this method cannot
     *  be used to create an empty array token, since the array token
     *  must have a type.  Instead, use the constructor that takes a
     *  type argument or use the "emptyArray" function from the expression
     *  language.
     *  @param value An (not empty) array of tokens.
     *  @exception IllegalActionException If the length of the given array is
     *   zero.
     */
    public ArrayToken(Token[] value) throws IllegalActionException {
        int length = value.length;

        if (length < 1) {
            throw new IllegalActionException("ArrayToken(Token[]) called with "
                    + "a an array of length less than 1.  To create an array "
                    + "of length 0, use the ArrayToken(Token) constructor "
                    + "or the \"emptyArray(type)\" function in the expression "
                    + "language. The reason is that elements in ArrayToken "
                    + "must have a type.");
        }

        _elementType = value[0].getType();

        for (int i = 0; i < length; i++) {
            Type valueType = value[i].getType();
            _elementType = TypeLattice.leastUpperBound(_elementType, valueType);
        }

        _value = new Token[length];

        for (int i = 0; i < length; i++) {
            _value[i] = _elementType.convert(value[i]);
        }
    }

    /** Construct an ArrayToken with the first <i>length</i>
     *  elements of the specified token array. If <i>length</i>
     *  is greater than the length of the specified array, then the
     *  resulting array token will have exactly the number of elements
     *  in the specified array.
     *  The type of the resulting array is the least upper bound
     *  of the types of the elements.
     *  This class makes a copy of the given array, so
     *  the passed array may be reused.  Note that this method cannot
     *  be used to create an empty array token, since the array token
     *  must have a type.  Instead, use the constructor that takes a
     *  type argument or use the "emptyArray" function from the expression
     *  language.
     *  @param value An (not empty) array of tokens.
     *  @param length The number of elements to be used from the
     *  specified array.
     *  @exception IllegalActionException If the length of the given array is
     *   zero.
     */
    public ArrayToken(Token[] value, int length) throws IllegalActionException {
        if (length > value.length) {
            length = value.length;
        }

        if (length < 1) {
            throw new IllegalActionException("ArrayToken(Token[]) called with "
                    + "a an empty array.  To create an array "
                    + "of length 0, use the ArrayToken(Token) constructor "
                    + "or the \"emptyArray(type)\" function in the expression "
                    + "language. The reason is that elements in ArrayToken "
                    + "must have a type.");
        }

        _elementType = value[0].getType();

        for (int i = 0; i < length; i++) {
            Type valueType = value[i].getType();
            _elementType = TypeLattice.leastUpperBound(_elementType, valueType);
        }

        _value = new Token[length];

        for (int i = 0; i < length; i++) {
            _value[i] = _elementType.convert(value[i]);
        }
    }

    /** Construct an ArrayToken from the specified string.
     *  The format of the string is a list of comma separated
     *  token values that begins with "{" and ends with "}".
     *  For example
     *  <code>
     *  "{1, 2, 3}"
     *  </code>
     *  @param init A string expression of an array.
     *  @exception IllegalActionException If the string does
     *   not contain a parsable array.
     */
    public ArrayToken(String init) throws IllegalActionException {
        PtParser parser = new PtParser();
        ASTPtRootNode tree = parser.generateParseTree(init);
        ParseTreeEvaluator evaluator = new ParseTreeEvaluator();
        Token token = evaluator.evaluateParseTree(tree);

        if (token instanceof ArrayToken) {
            _value = ((ArrayToken) token)._value;
            _elementType = ((ArrayToken) token)._elementType;
        } else {
            throw new IllegalActionException("An array token cannot be"
                    + " created from the expression '" + init + "'");
        }
    }

    /** Construct an empty ArrayToken with the given element type.
     *  @param elementType A token type.
     */
    public ArrayToken(Type elementType) {
        _value = new Token[0];
        _elementType = elementType;
    }

    /** Construct an ArrayToken with the specified element type and
     *  token array. All the tokens in the array must have the type
     *  of element type (or a subclass), otherwise an exception will
     *  be thrown. This class makes a copy of the given array, so
     *  the passed array may be reused.
     *  @param elementType The type of the array.
     *  @param value An array of tokens.
     *  @exception IllegalActionException If the tokens in the array
     *   do not have the specified type.
     */
    public ArrayToken(Type elementType, Token[] value)
            throws IllegalActionException {
        _elementType = elementType;
        int length = value.length;

        _value = new Token[length];

        for (int i = 0; i < length; i++) {
            _value[i] = _elementType.convert(value[i]);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Addition can occur between arrays, or between an array and a token 
     *  of some base type (i.e., anything less than or equal to String). 
     *  
     *  Addition is undefined between arrays and tokens of other incomparable 
     *  data types such as Record and Union. 
     *  @param rightArgument The array to be added to this array.
     *  @return The result of the addition.
     *  @exception IllegalActionException If addition between this array 
     *  and the provided argument is not supported. 
     */
    @Override
    public Token add(Token rightArgument) throws IllegalActionException {

        int typeInfo = TypeLattice.compare(rightArgument, BaseType.STRING);

        // Disallow addition between arrays and anything greater than or
        // incomparable with String (e.g., Record, Union, etc). 
        if ((typeInfo == CPO.INCOMPARABLE || typeInfo == CPO.HIGHER)
                && getClass() != rightArgument.getClass()) {
            throw new IllegalActionException(
                    notSupportedDifferentClassesMessage("add", this,
                            rightArgument));
        }
        try {
            return _add(rightArgument);
        } catch (IllegalActionException ex) {
            // If the type-specific operation fails, then create a
            // better error message that has the types of the
            // arguments that were passed in.
            throw new IllegalActionException(null, ex, notSupportedMessage(
                    "add", this, rightArgument));
        }
    }
    
    /** If the left-hand argument is a base type token, then convert it 
     *  to an array of size one. Then do an element-wise addition with 
     *  each element of the right-hand argument.
     *   
     *  This method is only invoked when addition is attempted using a 
     *  base type left-hand argument (e.g., 1 + {1, 2, 3} which will 
     *  yield {2, 3, 4}.
     *  @param leftArgument The array to add this array to.
     *  @return The result of the addition.
     *  @exception IllegalActionException If addition between this array 
     *  and the provided argument is not supported.
     */
    @Override
    public Token addReverse(Token leftArgument) throws IllegalActionException {
        try {
            int typeInfo = TypeLattice.compare(leftArgument, BaseType.STRING);
            
            // Wrap base type argument in array in order to do expansion.
            if ((typeInfo == CPO.LOWER || typeInfo == CPO.SAME)) {
                Token[] arr = {leftArgument};
                return new ArrayToken(arr).add(this);
            } 
            else if (getClass() == leftArgument.getClass()) {
                return ((ArrayToken)leftArgument).add(this);
            } else {
                throw new IllegalActionException(null, notSupportedMessage(
                        "addReverse", this, leftArgument));    
            }
        } catch (IllegalActionException ex) {
            // If the type-specific operation fails, then create a
            // better error message that has the types of the
            // arguments that were passed in.
            throw new IllegalActionException(null, ex, notSupportedMessage(
                    "addReverse", this, leftArgument));
        }
    }
    
     /** Append the given array to the end of this array, and return the
     *   resulting array. For example, if this array is {1, 2, 3} and the 
     *   given array is {4, 5, 6}, then the result would be {1, 2, 3, 4, 5, 6}. 
     *   If both arrays are empty, then an empty array is returned.
     *
     *  @param token The array to be appended to the end of this array.
     *  @return The result array.
     *  @exception IllegalActionException If an array token cannot be created.
     *  @since Ptolemy II 7.1
     */
    public ArrayToken append(ArrayToken token) throws IllegalActionException {
        // FIXME: This should probably create an instance of a subclass the
        // preserves and refers to the two subarrays.
        int newLength = length() + token.length();
        if (newLength > 0) {
            // NOTE: Do not use System.arraycopy if either of the two
            // arrays being appended are a subclasses of this class,
            // in which case values have to be accessed via the getElement() method,
            // which is overridden in the subclasses.
            Token[] result = new Token[newLength];
            if (_depth == 0 && token._depth == 0) {
                // Safe to use ArrayCopy, which is much more efficient than
                // the method below (performance measurements appending 50
                // arrays of length 10,000 yielded a huge performance improvement).
                System.arraycopy(_value, 0, result, 0, _value.length);
                System.arraycopy(token._value, 0, result, _value.length,
                        token._value.length);
            } else {
                int headLength = length();
                int tailLength = token.length();
                for (int i = 0; i < headLength; i++) {
                    result[i] = getElement(i);
                }
                for (int i = headLength; i < headLength + tailLength; i++) {
                    result[i] = token.getElement(i - headLength);
                }
            }
            return new ArrayToken(result);
        } else {
            return new ArrayToken(getElementType());
        }
    }

    /** Append the given arrays.
     *  @param tokens The list of arrays to be appended together.
     *  @return The result array.
     *  @exception IllegalActionException If an array token cannot be created,
     *   or if all specified arrays are empty.
     *  @since Ptolemy II 9.0
     */
    public static ArrayToken append(ArrayToken[] tokens)
            throws IllegalActionException {
        // FIXME: This should probably create an instance of a subclass the
        // preserves and refers to the subarrays.

        // First, figure out the length of the result array.
        boolean okToUseArrayCopy = true;
        int newLength = 0;
        for (ArrayToken token : tokens) {
            if (token != null) {
                newLength += token.length();
                if (token._depth > 0) {
                    okToUseArrayCopy = false;
                }
            }
        }
        if (newLength == 0) {
            throw new IllegalActionException("Cannot append empty arrays.");
        }
        // NOTE: Do not use System.arraycopy if any of the
        // arrays being appended are a subclasses of this class,
        // in which case values have to be accessed via the getElement() method,
        // which is overridden in the subclasses.
        Token[] result = new Token[newLength];
        if (okToUseArrayCopy) {
            // Safe to use ArrayCopy, which is much more efficient than
            // the method below (performance measurements appending 50
            // arrays of length 10,000 yielded a huge performance improvement).
            int i = 0;
            for (ArrayToken token : tokens) {
                if (token != null) {
                    System.arraycopy(token._value, 0, result, i,
                            token._value.length);
                    i += token._value.length;
                }
            }
        } else {
            int i = 0;
            for (ArrayToken token : tokens) {
                if (token != null) {
                    for (int j = 0; j < token.length(); j++) {
                        result[i++] = token.getElement(j);
                    }
                }
            }
        }
        return new ArrayToken(result);
    }

    /** Convert an ArrayToken to an array of unsigned bytes.
     *
     * @param dataArrayToken to be converted to a unsigned byte array.
     * @return dataBytes the resulting unsigned byte array.
     */
    public static byte[] arrayTokenToUnsignedByteArray(ArrayToken dataArrayToken) {
        byte[] dataBytes = new byte[dataArrayToken.length()];

        for (int j = 0; j < dataArrayToken.length(); j++) {
            UnsignedByteToken dataToken = (UnsignedByteToken) dataArrayToken
                    .getElement(j);
            dataBytes[j] = dataToken.byteValue();
        }

        return dataBytes;
    }

    /** Return an array of tokens populated with the contents of this
     *  array token.  The returned array is a copy so the caller is
     *  free to modify it.
     *  @return An array of tokens.
     */
    public Token[] arrayValue() {
        // NOTE: Do not System.arraycopy here if the
        // array is a subclasses of this class,
        // so values have to be accessed via the getElement() method,
        // which is overridden in the subclasses.
        Token[] result = new Token[length()];
        if (_depth == 0) {
            System.arraycopy(_value, 0, result, 0, _value.length);
        } else {
            for (int i = 0; i < length(); i++) {
                result[i] = getElement(i);
            }
        }
        return result;
    }

    /** Return a Boolean token that identifies whether the given token is an
     *  element of this array.
     *
     *  @param element A token to test whether it is an element of this array.
     *  @return A true Boolean token if the element is found, or false
     *   otherwise.
     */
    public BooleanToken contains(Token element) {
        for (int i = 0; i < length(); i++) {
            if (getElement(i).equals(element)) {
                return BooleanToken.TRUE;
            }
        }
        return BooleanToken.FALSE;
    }

    /** Divide this array token by the specified argument.
     *  If the argument is an array token and it has
     *  the same length as this array token, and the division is elementwise.
     *  Otherwise, each element is divided by the argument.
     *  This overrides the base class to allow division by scalars.
     *  @param rightArgument The token to divide into this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token is
     *   an array token but does not have the same length as this
     *   token, or if division is not supported between the elements
     *   of this array and the argument.
     */
    @Override
    public Token divide(Token rightArgument) throws IllegalActionException {
        try {
            return _divide(rightArgument);
        } catch (IllegalActionException ex) {
            // If the type-specific operation fails, then create a
            // better error message that has the types of the
            // arguments that were passed in.
            throw new IllegalActionException(null, ex, notSupportedMessage(
                    "divide", this, rightArgument));
        }
    }

    /** Divide this array token into the specified argument.
     *  If the argument is an array token and it has
     *  the same length as this array token, and the division is elementwise.
     *  Otherwise, each element is divided into the argument.
     *  This overrides the base class to allow division into scalars.
     *  @param rightArgument The token into which to divide this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token is
     *   an array token but does not have the same length as this
     *   token, or if division is not supported between the elements
     *   of this array and the argument.
     */
    @Override
    public Token divideReverse(Token rightArgument)
            throws IllegalActionException {
        try {
            return _divideReverse(rightArgument);
        } catch (IllegalActionException ex) {
            // If the type-specific operation fails, then create a
            // better error message that has the types of the
            // arguments that were passed in.
            throw new IllegalActionException(null, ex, notSupportedMessage(
                    "divide", this, rightArgument));
        }
    }

    /** Add the given token to each element of this array.
     *  @param token The token to be added to this token.
     *  @return A new array token.
     *  @exception IllegalActionException If the argument token is not
     *  of a type that can be added to an element of this token.
     */
    public ArrayToken elementAdd(Token token) throws IllegalActionException {
        Token[] result = new Token[length()];

        try {
            for (int i = 0; i < length(); i++) {
                result[i] = getElement(i).add(token);
            }
        } catch (IllegalActionException ex) {
            // If the type-specific operation fails, then create a
            // better error message that has the types of the
            // arguments that were passed in.
            throw new IllegalActionException(null, ex, notSupportedMessage(
                    "elementAdd", this, token));
        }

        return new ArrayToken(result);
    }

    /** Divide each element of this array by the given token.
     *  @param token The token which which to divide this token
     *  @return An array token.
     *  @exception IllegalActionException If the argument token is not
     *  of a type that can be divided into an element of this token.
     */
    public ArrayToken elementDivide(Token token) throws IllegalActionException {
        Token[] result = new Token[length()];

        try {
            for (int i = 0; i < length(); i++) {
                result[i] = getElement(i).divide(token);
            }
        } catch (IllegalActionException ex) {
            // If the type-specific operation fails, then create a
            // better error message that has the types of the
            // arguments that were passed in.
            throw new IllegalActionException(null, ex, notSupportedMessage(
                    "elementDivide", this, token));
        }

        return new ArrayToken(result);
    }

    /** Modulo each element of this array by the given token.
     *  @param token The token with which to modulo this token.
     *  @return An array token.
     *  @exception IllegalActionException If the argument token is not
     *  of a type that can be used with modulo.
     */
    public ArrayToken elementModulo(Token token) throws IllegalActionException {
        Token[] result = new Token[length()];

        try {
            for (int i = 0; i < length(); i++) {
                result[i] = getElement(i).modulo(token);
            }
        } catch (IllegalActionException ex) {
            // If the type-specific operation fails, then create a
            // better error message that has the types of the
            // arguments that were passed in.
            throw new IllegalActionException(null, ex, notSupportedMessage(
                    "elementModulo", this, token));
        }

        return new ArrayToken(result);
    }

    /** Multiply each element of this array by the given token.
     *  @param token The token with which to multiply this token.
     *  @return An array token.
     *  @exception IllegalActionException If the argument token is
     *  not of a type that can be multiplied to an element of this token.
     */
    public ArrayToken elementMultiply(Token token)
            throws IllegalActionException {
        Token[] result = new Token[length()];

        try {
            for (int i = 0; i < length(); i++) {
                result[i] = getElement(i).multiply(token);
            }
        } catch (IllegalActionException ex) {
            // If the type-specific operation fails, then create a
            // better error message that has the types of the
            // arguments that were passed in.
            throw new IllegalActionException(null, ex, notSupportedMessage(
                    "elementMultiply", this, token));
        }

        return new ArrayToken(result);
    }

    /** Return the (exact) return type of the elementMultiply function
     *  above.  This returns a new array type whose element type is
     *  the least upper bound of the element type of this token and
     *  the given type.
     *  @param type1 The type of the base of the corresponding function.
     *  @param type2 The type of the argument of the corresponding function.
     *  @return The type of the value returned from the corresponding
     *  function.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public static Type elementMultiplyReturnType(Type type1, Type type2)
            throws IllegalActionException {
        if (type1 instanceof ArrayType) {
            return new ArrayType(TypeLattice.leastUpperBound(
                    ((ArrayType) type1).getElementType(), type2));
        } else {
            return new ArrayType(BaseType.UNKNOWN);
        }
    }

    /** Subtract the given token from each element of this array.
     *  @param token The token to subtract from this token.
     *  @return An array token.
     *  @exception IllegalActionException If the argument token is not
     *  of a type that can be subtracted from an element of this token.
     */
    public ArrayToken elementSubtract(Token token)
            throws IllegalActionException {
        Token[] result = new Token[length()];

        try {
            for (int i = 0; i < length(); i++) {
                result[i] = getElement(i).subtract(token);
            }
        } catch (IllegalActionException ex) {
            // If the type-specific operation fails, then create a
            // better error message that has the types of the
            // arguments that were passed in.
            throw new IllegalActionException(null, ex, notSupportedMessage(
                    "elementSubtract", this, token));
        }

        return new ArrayToken(result);
    }

    /** Return true if the class of the argument is ArrayToken and of
     *  the same length and the elements are equal to that of this
     *  token.  Equality of the contained elements is tested by their
     *  equals() method.
     *  @param object the object to compare with.
     *  @return True if the argument is an array token of the same length
     *   and the elements are equal to that of this token.
     */
    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        // This test allows instances of a subclass.
        if (!(object instanceof ArrayToken)) {
            return false;
        }

        if (this.isNil() || ((ArrayToken) object).isNil()) {
            return false;
        }

        ArrayToken arrayArgument = (ArrayToken) object;
        int length = arrayArgument.length();

        if (length() != length) {
            return false;
        }

        for (int i = 0; i < length(); i++) {
            if (!getElement(i).equals(arrayArgument.getElement(i))
                    || getElement(i).isNil()) {
                return false;
            }
        }

        return true;
    }

    /** Extract a non-contiguous subarray either by giving a boolean array
     *  of the same length of this array describing which elements to
     *  include and which to include, or by giving an an array of an
     *  arbitrary length giving the indices of elements from this array
     *  to include in the subarray.  An example of the first form is
     *  {"red","green","blue"}.extract({true,false,true}), which evaluates
     *  to {"red", "blue"}.  An example of the second form is
     *  {"red","green","blue"}.extract({2,0,1,1}), which evaluates to
     *  {"blue", "red", "green", "green"}.
     *  @param selection An ArrayToken describing the selection of elements
     *   with which to form the subarray: either an array of integer
     *   indices, or an array of boolean inclusion/exclusion choices.
     *  @return An ArrayToken containing the extracted subarray.
     *  @exception IllegalActionException If the argument type is invalid
     *   or the result cannot be constructed.
     *  @exception ArrayIndexOutOfBoundsException  If the argument is an array
     *   of integers, and one or more of those integers is not a valid
     *   index into this array.
     *  @since Ptolemy II 4.1
     */
    public ArrayToken extract(ArrayToken selection)
            throws IllegalActionException {
        List result = new LinkedList();

        if (selection.getElementType().equals(BaseType.BOOLEAN)) {
            if (selection.length() != length()) {
                throw new IllegalActionException(
                        "When the argument is an array of booleans, it must have "
                                + "the same length as this array.");
            }

            for (int i = 0; i < selection.length(); i++) {
                if (selection.getElement(i).equals(BooleanToken.TRUE)) {
                    result.add(getElement(i));
                }
            }
        } else if (selection.getElementType().equals(BaseType.INT)) {
            for (int i = 0; i < selection.length(); i++) {
                // We could check for out-of-bounds indicies and ignore them,
                // if we wanted to.
                int index = ((IntToken) selection.getElement(i)).intValue();
                result.add(getElement(index));
            }
        } else {
            throw new IllegalActionException(
                    "The argument must be {boolean} or {int}.");
        }

        if (result.size() > 0) {
            Token[] resultArray = new Token[result.size()];
            resultArray = (Token[]) result.toArray(resultArray);
            return new ArrayToken(resultArray);
        } else {
            return new ArrayToken(getElementType());
        }
    }

    /** Return the element at the specified index.
     *  @param index The index of the desired element.
     *  @return The token contained in this array token at the
     *  specified index.
     *  @exception ArrayIndexOutOfBoundsException If the specified index is
     *   outside the range of the token array.
     */
    public Token getElement(int index) {
        return _value[index];
    }

    /** Return the type contained in this ArrayToken.
     *  @return A Type.
     */
    public Type getElementType() {
        return _elementType;
    }

    /** Return the type of this ArrayToken.
     *  @return An ArrayType.
     */
    @Override
    public Type getType() {
        return new ArrayType(getElementType(), length());
    }

    /** Return a hash code value for this token. This method returns
     *  the hash code of the first element, unless the array is empty,
     *  in which case it returns the hashCode of the element type, unless
     *  there is no element type, in which case it returns the superclass hash code.
     *  @return A hash code value for this token.
     */
    @Override
    public int hashCode() {
        if (length() > 0) {
            return getElement(0).hashCode();
        } else if (getElementType() != null) {
            return getElementType().hashCode();
        } else {
            return super.hashCode();
        }
    }

    /** Test that the value of this token is close to the first argument,
     *  where "close" means that the distance between them is less than
     *  or equal to the second argument.  This method only makes sense
     *  for arrays where where the distance between elements is reasonably
     *  represented as a double. This class overrides the superclass to not
     *  insist that the two array tokens be instances of the same class,
     *  but rather that they just both be array tokens, and then defers to the
     *  _isCloseTo() method.
     *  @param token The token to test closeness of this token with.
     *  @param epsilon  The value that we use to determine whether two
     *   tokens are close.
     *  @return A boolean token that contains the value true if the
     *   value of this token is close to that of the argument token.
     *  @exception IllegalActionException If the argument token and
     *   this token are implemented in different classes.
     */
    @Override
    public BooleanToken isCloseTo(Token token, double epsilon)
            throws IllegalActionException {
        if (!(token instanceof ArrayToken)) {
            throw new IllegalActionException(
                    notSupportedDifferentClassesMessage("isCloseTo", this,
                            token));
        }

        try {
            return _isCloseTo(token, epsilon);
        } catch (IllegalActionException ex) {
            // If the type-specific operation fails, then create a
            // better error message that has the types of the
            // arguments that were passed in.
            throw new IllegalActionException(null, ex, notSupportedMessage(
                    "isCloseTo", this, token));
        }
    }

    /** Return true if the token is nil, (aka null or missing).
     *  Nil or missing tokens occur when a data source is sparsely populated.
     *  @return True if the token is the {@link #NIL} token.
     */
    @Override
    public boolean isNil() {
        // We use a method here so that we can easily change how
        // we determine if a token is nil without modify lots of classes.
        // Can't use equals() here, or we'll go into an infinite loop.
        return this == ArrayToken.NIL;
    }

    /** Return the length of the contained token array.
     *  @return The length of the contained token array.
     */
    public int length() {
        return _value.length;
    }

    /** Return a new token whose value is the value of this token
     *  modulo the value of the argument token.
     *  This overrides the base class to allow modulo by scalars.
     *  @param rightArgument The token to divide into this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token and
     *  this token are implemented in different classes, or the
     *  operation does not make sense for the given types.
     */
    @Override
    public Token modulo(Token rightArgument) throws IllegalActionException {
        try {
            return _modulo(rightArgument);
        } catch (IllegalActionException ex) {
            // If the type-specific operation fails, then create a
            // better error message that has the types of the
            // arguments that were passed in.
            throw new IllegalActionException(null, ex, notSupportedMessage(
                    "modulo", this, rightArgument));
        }
    }

    /** Return a new token whose value is the value of the argument
     *  modulo the value of this token.
     *  This overrides the base class to allow modulo by scalars.
     *  @param rightArgument The token to divide into this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token and
     *  this token are implemented in different classes, or the
     *  operation does not make sense for the given types.
     */
    @Override
    public Token moduloReverse(Token rightArgument)
            throws IllegalActionException {
        try {
            return _moduloReverse(rightArgument);
        } catch (IllegalActionException ex) {
            // If the type-specific operation fails, then create a
            // better error message that has the types of the
            // arguments that were passed in.
            throw new IllegalActionException(null, ex, notSupportedMessage(
                    "modulo", this, rightArgument));
        }
    }

    /** Multiply this array token by the specified argument.
     *  If the argument is an array token and it has
     *  the same length as this array token, and the multiplication is elementwise.
     *  Otherwise, each element is multiplied by the argument.
     *  This overrides the base class to allow multiplication by scalars.
     *  @param rightArgument The token to multiply by this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token is
     *   an array token but does not have the same length as this
     *   token, or if multiplication is not supported between the elements
     *   of this array and the argument.
     */
    @Override
    public Token multiply(Token rightArgument) throws IllegalActionException {
        try {
            return _multiply(rightArgument);
        } catch (IllegalActionException ex) {
            // If the type-specific operation fails, then create a
            // better error message that has the types of the
            // arguments that were passed in.
            throw new IllegalActionException(null, ex, notSupportedMessage(
                    "multiply", this, rightArgument));
        }
    }

    /** Multiply this array token by the specified argument.
     *  If the argument is an array token and it has
     *  the same length as this array token, and the multiplication is elementwise.
     *  Otherwise, each element is multiplied by the argument.
     *  This overrides the base class to allow multiplication by scalars.
     *  @param leftArgument The token to multiply by this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token is
     *   an array token but does not have the same length as this
     *   token, or if multiplication is not supported between the elements
     *   of this array and the argument.
     */
    @Override
    public Token multiplyReverse(Token leftArgument)
            throws IllegalActionException {
        try {
            return _multiply(leftArgument);
        } catch (IllegalActionException ex) {
            // If the type-specific operation fails, then create a
            // better error message that has the types of the
            // arguments that were passed in.
            throw new IllegalActionException(null, ex, notSupportedMessage(
                    "multiplyReverse", this, leftArgument));
        }
    }

    /** Return a new ArrayToken representing the multiplicative
     *  identity.  The returned token contains an array of the same
     *  size as the array contained by this token, and each element of
     *  the array in the returned token is the multiplicative identity
     *  of the corresponding element of this token.
     *  @return An ArrayToken.
     *  @exception IllegalActionException If multiplicative identity is not
     *   supported by the element token.
     */
    @Override
    public Token one() throws IllegalActionException {
        Token[] oneValueArray = new Token[length()];

        for (int i = 0; i < length(); i++) {
            oneValueArray[i] = getElement(i).one();
        }

        return new ArrayToken(oneValueArray);
    }

    /** Return a new ArrayToken whose elements are in reverse order of this
     *  ArrayToken.
     *  @return An ArrayToken
     *  @exception IllegalActionException If thrown while creating the new
     *  ArrayToken.
     */
    public ArrayToken reverse() throws IllegalActionException {
        Token[] result = new Token[length()];
        for (int i = 0; i < length(); i++) {
            result[i] = getElement(length() - i - 1);
        }
        return new ArrayToken(result);
    }

    /** Return the contiguous subarray starting at the specified index to the
     *  end of this array. If the specified index is out of range,
     *  then return an empty array with the same type as this array.
     *  @param index The index of the beginning of the subarray.
     *  @return The extracted subarray.
     *  @exception IllegalActionException If the index argument is
     *   less than zero.
     *  @since Ptolemy II 7.1
     */
    public ArrayToken subarray(int index) throws IllegalActionException {
        return subarray(index, length() - index);
    }

    /** Return the contiguous subarray starting at the specified index and
     *  of the specified length. If the specified index is out of range,
     *  or if the specified length extends beyond the end of the array,
     *  then return an empty array with the same type as this array.
     *  @param index The index of the beginning of the subarray.
     *  @param count The length of the subarray.
     *  @return The extracted subarray.
     *  @exception IllegalActionException If the index argument is
     *   less than zero.
     *  @since Ptolemy II 4.1
     */
    public ArrayToken subarray(int index, int count)
            throws IllegalActionException {
        if (index < 0) {
            throw new IllegalActionException(
                    "index argument of subarray() must be non-negative.");
        }

        // NOTE: Do not use System.arraycopy here if the
        // array is a subclass of this class,
        // so values have to be accessed via the getElement() method,
        // which is overridden in the subclasses.

        if (count > 0 && index < length() && index >= 0) {
            if (count + index > length()) {
                count = length() - index;
            }

            Token[] result = new Token[count];
            if (_depth == 0) {
                System.arraycopy(_value, index, result, 0, count);
            } else {
                for (int i = 0; i < count; i++) {
                    result[i] = getElement(index + i);
                }
            }
            return new ArrayToken(result);
        } else {
            return new ArrayToken(getElementType());
        }
    }

    /** Subtract from this array token the specified argument.
     *  If the argument is an array token and it has
     *  the same length as this array token, and the subtraction is elementwise.
     *  Otherwise, subtract the argument from each element.
     *  This overrides the base class to allow subtraction by scalars.
     *  @param rightArgument The token to subtract from this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token is
     *   an array token but does not have the same length as this
     *   token or length 1, or if division is not supported between the elements
     *   of this array and the argument.
     */
    @Override
    public Token subtract(Token rightArgument) throws IllegalActionException {
        try {
            return _subtract(rightArgument);
        } catch (IllegalActionException ex) {
            // If the type-specific operation fails, then create a
            // better error message that has the types of the
            // arguments that were passed in.
            throw new IllegalActionException(null, ex, notSupportedMessage(
                    "subtract", this, rightArgument));
        }
    }

    /** Subtract this array token from the specified argument.
     *  If the argument is an array token and it has
     *  the same length as this array token, and the subtraction is elementwise.
     *  Otherwise, each element is subtracted from the argument.
     *  This overrides the base class to allow subtraction from scalars.
     *  @param rightArgument The token from which to subtract this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument token is
     *   an array token but does not have the same length as this
     *   token or length 1, or if division is not supported between the elements
     *   of this array and the argument.
     */
    @Override
    public Token subtractReverse(Token rightArgument)
            throws IllegalActionException {
        try {
            return _subtractReverse(rightArgument);
        } catch (IllegalActionException ex) {
            // If the type-specific operation fails, then create a
            // better error message that has the types of the
            // arguments that were passed in.
            throw new IllegalActionException(null, ex, notSupportedMessage(
                    "divide", this, rightArgument));
        }
    }

    /** Return the value of this token as a string that can be parsed
     *  by the expression language to recover a token with the same value.
     *  @return A string beginning with "{" that contains expressions
     *  for every element in the array separated by commas, ending with "}".
     */
    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer("{");

        for (int i = 0; i < length(); i++) {
            buffer.append(getElement(i).toString());

            if (i < length() - 1) {
                buffer.append(", ");
            }
        }

        buffer.append("}");
        return buffer.toString();
    }

    /** Take an array of unsigned bytes and convert it to an ArrayToken.
     *
     * @param dataBytes data to be converted to an ArrayToken.
     * @return dataArrayToken the resulting ArrayToken.
     * @exception IllegalActionException If ArrayToken can not be created.
     */
    public static ArrayToken unsignedByteArrayToArrayToken(byte[] dataBytes)
            throws IllegalActionException {
        int bytesAvailable = dataBytes.length;
        Token[] dataArrayToken = new Token[bytesAvailable];

        for (int j = 0; j < bytesAvailable; j++) {
            dataArrayToken[j] = new UnsignedByteToken(dataBytes[j]);
        }

        return new ArrayToken(dataArrayToken);
    }

    /** Return a new array with all elements equal to those of this
     *  array except the one at position <i>index</i>, which has value
     *  given by <i>value</i>. If the index is out of range, then return
     *  this array token unchanged. The element type of the new array is the
     *  least upper bound of the element type of this array and the specified
     *  value.
     *  @param index The index of the element to be updated.
     *  @param value The value of the element to be updated.
     *  @return A new array.
     *  @exception IllegalActionException Should not be thrown in this base class.
     */
    public ArrayToken update(int index, Token value)
            throws IllegalActionException {
        if (index < 0 || index >= length()) {
            return this;
        }
        return new UpdatedArrayToken(this, index, value);
    }

    /** Returns a new ArrayToken representing the additive identity.
     *  The returned token contains an array of the same size as the
     *  array contained by this token, and each element of the array
     *  in the returned token is the additive identity of the
     *  corresponding element of this token.
     *  @return An ArrayToken.
     *  @exception IllegalActionException If additive identity is not
     *  supported by an element token.
     */
    @Override
    public Token zero() throws IllegalActionException {
        if (length() == 0) {
            return this;
        }

        Token[] zeroValueArray = new Token[length()];

        for (int i = 0; i < length(); i++) {
            zeroValueArray[i] = getElement(i).zero();
        }

        return new ArrayToken(zeroValueArray);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** A token that represents a missing value.
     *  Null or missing tokens are common in analytical systems
     *  like R and SAS where they are used to handle sparsely populated data
     *  sources.  In database parlance, missing tokens are sometimes called
     *  null tokens.  Since null is a Java keyword, we use the term "nil".
     */
    public static final ArrayToken NIL = new ArrayToken(BaseType.NIL);

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a new token whose value is the value of the argument
     *  token added to the value of this token.  It is assumed that
     *  this class is the class of the argument.
     *  @param rightArgument The token whose value we add to the value
     *  of this token.
     *  @return A new array token containing the result.
     *  @exception IllegalActionException If the argument is an
     *  ArrayToken of different length, or calling the add method of
     *  an element token throws it.
     */
    @Override
    protected Token _add(Token rightArgument) throws IllegalActionException {
        Token[] result = null;

        if (rightArgument instanceof ArrayToken) {
            // Right argument is an array.
            ArrayToken rightArray = (ArrayToken) rightArgument;
            // If array's lengths match, then do element-wise addition.
            if (rightArray.length() == length()) {
                result = new Token[length()];
                for (int i = 0; i < length(); i++) {
                    result[i] = getElement(i).add(rightArray.getElement(i));
                }
            } else {
                if (rightArray.length() == 1) {
                    // If the left array has only one element, then expand it 
                    // and do element-wise addition.
                    result = new Token[length()];
                    for (int i = 0; i < length(); i++) {
                        result[i] = getElement(i).add(rightArray.getElement(0));
                    }
                } else if (length() == 1) {
                    // If the right array has only one element, then expand it 
                    // and do element-wise addition.
                    result = new Token[rightArray.length()];
                    for (int i = 0; i < rightArray.length(); i++) {
                        result[i] = getElement(0).add(rightArray.getElement(i));
                    }
                } else {
                    // Use this method to get a consistent error message
                    // defined in one place.
                    _checkArgumentLength(rightArgument);
                }

            }
        } else {
            // Right argument is not an array.
            // Add right argument to each element in the array.
            result = new Token[length()];
            for (int i = 0; i < length(); i++) {
                result[i] = getElement(i).add(rightArgument);
            }
        }
        return new ArrayToken(result);
    }

    /** Return a new token whose value is the value of this token
     *  divided by the value of the argument token.  If the argument
     *  is an array, then the division is done
     *  elementwise. Otherwise, this method assumes that each
     *  element can be divided by the argument and performs the
     *  division.
     *  @param rightArgument The token to divide this token by.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument is an
     *   ArrayToken of different length, or calling the divide method
     *   of the element token throws it.
     */
    @Override
    protected Token _divide(Token rightArgument) throws IllegalActionException {
        Token[] result = new Token[length()];
        if (rightArgument instanceof ArrayToken) {
            ArrayToken rightArray = (ArrayToken) rightArgument;
            if (rightArray.length() == length()) {
                for (int i = 0; i < length(); i++) {
                    result[i] = getElement(i).divide(rightArray.getElement(i));
                }
            } else {
                // Maybe one argument or the other is an array
                // of length one, converted automatically from
                // a scalar.
                if (rightArray.length() == 1) {
                    Token scalar = rightArray.getElement(0);
                    for (int i = 0; i < length(); i++) {
                        result[i] = getElement(i).divide(scalar);
                    }
                } else if (length() == 1) {
                    result = new Token[rightArray.length()];
                    Token scalar = getElement(0);
                    for (int i = 0; i < rightArray.length(); i++) {
                        result[i] = scalar.divide(rightArray.getElement(i));
                    }
                } else {
                    // Use this method to get a consistent error message
                    // defined in one place.
                    _checkArgumentLength(rightArgument);
                }
            }
        } else {
            for (int i = 0; i < length(); i++) {
                result[i] = getElement(i).divide(rightArgument);
            }
        }
        return new ArrayToken(result);
    }

    /** Return a new token whose value is the value of this token
     *  divided into the value of the argument token.  If the argument
     *  is an array, then the division is done
     *  elementwise. Otherwise, this method assumes that the argument
     *  can be divided by each element of this array and performs the
     *  division.
     *  @param rightArgument The token into which to divide this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument is an
     *   ArrayToken of different length, or calling the divide method
     *   of the element token throws it.
     */
    protected Token _divideReverse(Token rightArgument)
            throws IllegalActionException {
        if (rightArgument instanceof ArrayToken) {
            return ((ArrayToken) rightArgument).divide(this);
        } else {
            Token[] result = new Token[length()];
            for (int i = 0; i < length(); i++) {
                result[i] = rightArgument.divide(getElement(i));
            }
            return new ArrayToken(result);
        }
    }

    /** Test whether the value of this token is close to the first argument,
     *  where "close" means that the distance between them is less than
     *  or equal to the second argument.  This method only makes sense
     *  for tokens where the distance between them is reasonably
     *  represented as a double. It is assumed that the argument is
     *         an ArrayToken, and the isCloseTo() method of the array elements
     *         is used.
     *  @param token The token to compare to this token.
     *  @param epsilon The value that we use to determine whether two
     *   tokens are close.
     *  @exception IllegalActionException If the elements do not support
     *   this comparison.
     *  @return A true-valued token if the first argument is close
     *  to this token.
     */
    @Override
    protected BooleanToken _isCloseTo(Token token, double epsilon)
            throws IllegalActionException {
        ArrayToken rightArray = (ArrayToken) token;

        if (length() != rightArray.length()) {
            return BooleanToken.FALSE;
        }

        for (int i = 0; i < length(); i++) {
            // Here is where isCloseTo() differs from isEqualTo().
            // Note that we return false the first time we hit an
            // element token that is not close to our current element token.
            BooleanToken result = getElement(i).isCloseTo(
                    rightArray.getElement(i), epsilon);

            if (result.booleanValue() == false) {
                return BooleanToken.FALSE;
            }
        }

        return BooleanToken.TRUE;
    }

    /** Return a true-valued token if the argument is equal to this one.
     *  The isEqualTo() method of the element tokens is used to make the
     *         comparison.  It is assumed that the argument is an ArrayToken.
     *  @param token The token to compare to this token.
     *  @exception IllegalActionException If the element types do not
     *   support this comparison.
     *  @return A true-valued token if the argument is equal.
     */
    @Override
    protected BooleanToken _isEqualTo(Token token)
            throws IllegalActionException {
        if (isNil() || token.isNil()) {
            return BooleanToken.FALSE;
        }

        _checkArgumentLength(token);

        ArrayToken rightArray = (ArrayToken) token;

        for (int i = 0; i < length(); i++) {
            BooleanToken result = getElement(i).isEqualTo(
                    rightArray.getElement(i));

            if (result.booleanValue() == false) {
                return BooleanToken.FALSE;
            }
        }

        return BooleanToken.TRUE;
    }

    /** Return a new token whose value is the value of this token
     *  modulo the value of the argument token.  It is assumed that
     *  this class is the class of the argument.
     *  @param rightArgument The token to modulo this token by.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument is an
     *  ArrayToken of different length, or calling the modulo method
     *  of the element token throws it.
     */
    @Override
    protected Token _modulo(Token rightArgument) throws IllegalActionException {
        Token[] result = new Token[length()];
        if (rightArgument instanceof ArrayToken) {
            ArrayToken rightArray = (ArrayToken) rightArgument;
            if (rightArray.length() == length()) {
                for (int i = 0; i < length(); i++) {
                    result[i] = getElement(i).modulo(rightArray.getElement(i));
                }
            } else {
                // Maybe one argument or the other is an array
                // of length one, converted automatically from
                // a scalar.
                if (rightArray.length() == 1) {
                    Token scalar = rightArray.getElement(0);
                    for (int i = 0; i < length(); i++) {
                        result[i] = getElement(i).modulo(scalar);
                    }
                } else if (length() == 1) {
                    result = new Token[rightArray.length()];
                    Token scalar = getElement(0);
                    for (int i = 0; i < rightArray.length(); i++) {
                        result[i] = scalar.modulo(rightArray.getElement(i));
                    }
                } else {
                    // Use this method to get a consistent error message
                    // defined in one place.
                    _checkArgumentLength(rightArgument);
                }
            }
        } else {
            for (int i = 0; i < length(); i++) {
                result[i] = getElement(i).modulo(rightArgument);
            }
        }
        return new ArrayToken(result);
    }

    /** Return a new token whose value is the value of this token
     *  modulo the value of the argument token.  If the argument
     *  is an array, then the modulo is done
     *  elementwise. Otherwise, this method assumes that the argument
     *  can be moduloed by each element of this array and performs the
     *  modulo.
     *  @param rightArgument The token into which to modulo this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument is an
     *   ArrayToken of different length, or calling the modulo method
     *   of the element token throws it.
     */
    protected Token _moduloReverse(Token rightArgument)
            throws IllegalActionException {
        if (rightArgument instanceof ArrayToken) {
            return ((ArrayToken) rightArgument).modulo(this);
        } else {
            Token[] result = new Token[length()];
            for (int i = 0; i < length(); i++) {
                result[i] = rightArgument.modulo(getElement(i));
            }
            return new ArrayToken(result);
        }
    }

    /** Return a new token whose value is the value of this token
     *  multiplied by the value of the argument token.  If the argument
     *  is an array token, then it is required to have the same length
     *  as this array token, and the multiplication is performed
     *  elementwise. Otherwise, each element of this array is multiplied
     *  by the argument.
     *  @param rightArgument The token to multiply this token by.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument is an
     *  ArrayToken of different length, or calling the multiply method
     *  of the element token throws it.
     */
    @Override
    protected Token _multiply(Token rightArgument)
            throws IllegalActionException {
        Token[] result = new Token[length()];
        if (rightArgument instanceof ArrayToken) {
            ArrayToken rightArray = (ArrayToken) rightArgument;
            if (rightArray.length() == length()) {
                for (int i = 0; i < length(); i++) {
                    result[i] = getElement(i)
                            .multiply(rightArray.getElement(i));
                }
            } else {
                // Maybe one argument or the other is an array
                // of length one, converted automatically from
                // a scalar.
                if (rightArray.length() == 1) {
                    Token scalar = rightArray.getElement(0);
                    for (int i = 0; i < length(); i++) {
                        result[i] = getElement(i).multiply(scalar);
                    }
                } else if (length() == 1) {
                    result = new Token[rightArray.length()];
                    Token scalar = getElement(0);
                    for (int i = 0; i < rightArray.length(); i++) {
                        result[i] = rightArray.getElement(i).multiply(scalar);
                    }
                } else {
                    // Use this method to get a consistent error message
                    // defined in one place.
                    _checkArgumentLength(rightArgument);
                }
            }
        } else {
            for (int i = 0; i < length(); i++) {
                result[i] = getElement(i).multiply(rightArgument);
            }
        }
        return new ArrayToken(result);
    }

    /** Return a new token whose value is the value of the argument
     *  token subtracted from the value of this token.  It is assumed
     *  that this class is the class of the argument.
     *  @param rightArgument The token to subtract to this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument is an
     *  ArrayToken of different length, or calling the subtract method
     *  of the element token throws it.
     */
    @Override
    protected Token _subtract(Token rightArgument)
            throws IllegalActionException {
        Token[] result = new Token[length()];
        if (rightArgument instanceof ArrayToken) {
            ArrayToken rightArray = (ArrayToken) rightArgument;
            if (rightArray.length() == length()) {
                for (int i = 0; i < length(); i++) {
                    result[i] = getElement(i)
                            .subtract(rightArray.getElement(i));
                }
            } else {
                // Maybe one argument or the other is an array
                // of length one, converted automatically from
                // a scalar.
                if (rightArray.length() == 1) {
                    Token scalar = rightArray.getElement(0);
                    for (int i = 0; i < length(); i++) {
                        result[i] = getElement(i).subtract(scalar);
                    }
                } else if (length() == 1) {
                    result = new Token[rightArray.length()];
                    Token scalar = getElement(0);
                    for (int i = 0; i < rightArray.length(); i++) {
                        result[i] = scalar.subtract(rightArray.getElement(i));
                    }
                } else {
                    // Use this method to get a consistent error message
                    // defined in one place.
                    _checkArgumentLength(rightArgument);
                }
            }
        } else {
            for (int i = 0; i < length(); i++) {
                result[i] = getElement(i).subtract(rightArgument);
            }
        }
        return new ArrayToken(result);
    }

    /** Return a new token whose value is the value of this token
     *  subtracted from the value of the argument token.  If the argument
     *  is an array with the same length as this one, then the division is done
     *  elementwise. Otherwise, this method assumes that the argument
     *  can be subtracted from each element of this array and performs the
     *  subtraction.
     *  @param rightArgument The token from which to subtract this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument is an
     *   ArrayToken of different length (and not length 1), or calling the subtract method
     *   of the element token throws it.
     */
    protected Token _subtractReverse(Token rightArgument)
            throws IllegalActionException {
        if (rightArgument instanceof ArrayToken) {
            return ((ArrayToken) rightArgument).subtract(this);
        } else {
            Token[] result = new Token[length()];
            for (int i = 0; i < length(); i++) {
                result[i] = rightArgument.subtract(getElement(i));
            }
            return new ArrayToken(result);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The depth of the hierarchy of array tokens used to represent
     *  this array. If this array is an instance of this base class,
     *  then the depth is 0. Subclasses, however, represent the array
     *  in terms of instances of ArrayToken, and in that case, the
     *  value of this variable will be the longest path in the hierarchy
     *  to the an instance of this ArrayToken class.
     */
    protected int _depth = 0;

    /** The type of this array. */
    protected Type _elementType;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // Throw an exception if the argument is not an ArrayToken of the
    // same length.
    private void _checkArgumentLength(Token token)
            throws IllegalActionException {
        int length = ((ArrayToken) token).length();

        if (length() != length) {
            throw new IllegalActionException("The length of the argument ("
                    + length
                    + ") is not the same as the length of this token ("
                    + length() + ").");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The array of values of this array token. */
    private Token[] _value;
}
