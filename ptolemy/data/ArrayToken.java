/* A token that contains an array of tokens.

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
@AcceptedRating Yellow (cxh@eecs.berkeley.edu)
*/

package ptolemy.data;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.PtParser;
import ptolemy.data.type.Type;
import ptolemy.data.type.ArrayType;

//////////////////////////////////////////////////////////////////////////
//// ArrayToken
/**
A token that contains an array of tokens.

@author Yuhong Xiong
@version $Id$
@since Ptolemy II 0.4
*/

public class ArrayToken extends Token {

    /** Construct an ArrayToken with the specified token array. All the
     *  tokens in the array must have the same type, otherwise an
     *  exception will be thrown. Also, this array cannot have length
     *  zero because the element type cannot be determined. To construct
     *  an array token with a zero-length array, use the constructor that
     *  takes a Type argument.
     *  @param value An array of tokens.
     *  @exception IllegalActionException If the tokens in the array
     *   do not have the same type, or the length of the array is zero.
     */
    public ArrayToken(Token[] value) throws IllegalActionException {
        _initialize(value);
    }

    /** Construct an empty ArrayToken with the specified type.
     *  @param type The element type of this array token.
     */
    public ArrayToken(Type type) {
	_value = new Token[0];
        _elementType = type;
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
	ArrayToken token = (ArrayToken)tree.evaluateParseTree();

        Token[] value = token.arrayValue();
        _initialize(value);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a new token whose value is the point-wise addition of the
     *  argument Token with this Token. The argument token must be an
     *  ArrayToken having the same length as this Token. The field types of
     *  the argument must be comparable with the corresponding field types
     *  of this token, and the field types of the returned token is the
     *  higher type of the two.
     *  @param token The token to add with this token.
     *  @return A new ArrayToken.
     *  @exception IllegalActionException If the argument is not an
     *   ArrayToken, or is an ArrayToken of different length, or calling
     *   the add method of the element token throws it.
     */
    public Token add(Token token)
	    throws IllegalActionException {
	_checkArgument(token);
	Token[] argArray = ((ArrayToken)token).arrayValue();
	Token[] result = new Token[_value.length];
	for (int i = 0; i < _value.length; i++) {
	    result[i] = _value[i].add(argArray[i]);
	}

	return new ArrayToken(result);
    }

    /** Return the token array contained by this token.
     *  The returned array is a copy so the caller is free to modify
     *  it.
     *  @return An array of tokens.
     */
    public Token[] arrayValue() {
	Token[] result = new Token[_value.length];
	System.arraycopy(_value, 0, result, 0, _value.length);
	return result;
    }

    /** Convert the argument token into an instance of ArrayToken. Since
     *  this method is static, there is not an instance of ArrayToken from
     *  which the resulting type can be obtained. So this method always
     *  throws an exception.  Use the convert method in ArrayType instead.
     *  @exception IllegalActionException Always thrown.
     */
    public static Token convert(Token token)
	    throws IllegalActionException {
	throw new IllegalActionException("ArrayToken.convert: " +
                "This method cannot be used, use the convert method " +
                "in ArrayType.");
    }

    /** Return true if the argument is an array token of the same length and
     *  the elements are equal to that of this token. The equality of the
     *  elements are tested by the equals() method of the element tokens.
     *  @param object An instance of Object.
     *  @return True if the argument is an array token of the same length
     *   and the elements are equal to that of this token.
     */
    public boolean equals(Object object) {
	// This test rules out instances of a subclass.
	if (object.getClass() != ArrayToken.class) {
	    return false;
	}

	ArrayToken arrayArgument = (ArrayToken)object;
	int length = arrayArgument.length();
	if (_value.length != length) {
	    return false;
	}

	Token[] array = arrayArgument.arrayValue();
	for (int i = 0; i < length; i++) {
	    if ( !_value[i].equals(array[i])) {
		return false;
	    }
	}

	return true;
    }

    /** Return the element at the specified index.
     *  @param index The index of the desired element.
     *  @return A Token.
     *  @exception ArrayIndexOutOfBoundException If the specified index is
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
    public Type getType() {
	return new ArrayType(_elementType);
    }

    /** Return a hash code value for this token. This method returns the
     *  sum of the hash code of the element tokens. If this token has zero
     *  length, return 0.
     *  @return A hash code value for this token.
     */
    public int hashCode() {
	if (length() == 0) {
	    return 0;
	}

	int code = _value[0].hashCode();
	for (int i = 1; i < length(); i++) {
	    code += _value[i].hashCode();
	}
	return code;
    }

    /** Test that each element of this Token is close to the
     *  corresponding element in the argument Token and that each
     *  element of this Token has the same units as the corresponding
     *  element in the argument Token.
     *  The value of the ptolemy.math.Complex epsilon field is
     *  used to determine whether the two Tokens are close.
     *
     *  @see ptolemy.math.Complex#epsilon
     *  @see #isEqualTo
     *  @param token The token to test closeness of this token with.
     *  @return a boolean token that contains the value true if the
     *   value of each element of this token is close to the
     *   value of corresponding element in the argument token and
     *   the units of each element of this token is equal to the units
     *   of the corresponding element in the argument token.
     *  @exception IllegalActionException If the argument token is
     *   not of a type that can be compared with this token.
     */
    public BooleanToken isCloseTo(Token token) throws IllegalActionException {
	return isCloseTo(token, ptolemy.math.Complex.epsilon);
    }

    /** Test that each element of this Token is close to the
     *  corresponding element in the argument Token and that each
     *  element of this Token has the same units as the corresponding
     *  element in the argument Token.
     *  The value of the epsilon argument is used to determine whether
     *  the two Tokens are close.
     *
     *  @see #isEqualTo
     *  @param token The token to test closeness of this token with.
     *  @param epsilon The value that we use to determine whether two
     *  tokens are close.
     *  @return a boolean token that contains the value true if the
     *   value of each element of this token is close to the
     *   value of corresponding element in the argument token and
     *   the units of each element of this token is equal to the units
     *   of the corresponding element in the argument token.
     *  @exception IllegalActionException If the argument token is
     *   not of a type that can be compared with this token.
     */
    public BooleanToken isCloseTo(Token token,
            double epsilon)
            throws IllegalActionException {

	_checkArgument(token);
	Token[] argArray = ((ArrayToken)token).arrayValue();
	for (int i = 0; i < _value.length; i++) {
	    // Here is where isCloseTo() differs from isEqualTo().

	    // Note that we return false the first time we hit an
	    // element token that is not close to our current element token.
	    BooleanToken result = _value[i].isCloseTo(argArray[i], epsilon);
	    if (result.booleanValue() == false) {
		return new BooleanToken(false);
	    }
	}

	return new BooleanToken(true);
    }

    /** Test for equality of the values of this Token and the argument.
     *  @see #isCloseTo
     *  @param token The token with which to test equality.
     *  @return A new BooleanToken which contains the result of the test.
     *  @exception IllegalActionException If the argument is not an
     *   ArrayToken, or is an ArrayToken of different length, or calling
     *   the isEqualTo method of the element token throws it.
     */
    public BooleanToken isEqualTo(Token token)
	    throws IllegalActionException {

	_checkArgument(token);
	Token[] argArray = ((ArrayToken)token).arrayValue();
	for (int i = 0; i < _value.length; i++) {
	    BooleanToken result = _value[i].isEqualTo(argArray[i]);
	    if (result.booleanValue() == false) {
		return new BooleanToken(false);
	    }
	}

	return new BooleanToken(true);
    }

    /** Return the length of the contained token array.
     *  @return The length of the contained token array.
     */
    public int length() {
	return _value.length;
    }

    /** Return a new ArrayToken whose value is the value of this Token
     *  multiplied by the value of the argument Token.
     *  The argument token must be a scalar token. The operation of this
     *  method can be viewed as scaling the elements of this token by the
     *  argument. The resulting token will have the same dimension as this
     *  token, with the element type being the type of the multiplication
     *  result of the element of this token and the argument.
     *  @param rightFactor The token to multiply this Token by.
     *  @exception IllegalActionException If the argument token is
     *   not of a type that can be multiplied to the elements of this
     *   Token.
     *  @return A new ArrayToken.
     */
    public Token multiply(Token rightFactor) throws IllegalActionException {
	Token[] result = new Token[_value.length];
	for (int i = 0; i < _value.length; i++) {
	    result[i] = _value[i].multiply(rightFactor);
	}

	return new ArrayToken(result);
    }

    /** Returns a new ArrayToken representing the multiplicative identity.
     *  The returned token contains an array of the same size as the
     *  array contained by this token, and each element of the array
     *  in the returned token is the multiplicative identity of the elements
     *  of this token.
     *  @return An ArrayToken.
     *  @exception IllegalActionException If multiplicative identity is not
     *   supported by the element token.
     */
    public Token one() throws IllegalActionException {
	// if this array token has length zero, return this.
	if (length() == 0) {
	    return this;
	}

	Token oneVal = _value[0].one();
	Token[] oneValArray = new Token[_value.length];
	for (int i = 0; i < _value.length; i++) {
	    oneValArray[i] = oneVal;
	}
	return new ArrayToken(oneValArray);
    }

    /** Return a new token whose value is the point-wise subtraction of the
     *  argument Token from this Token. The argument token must be an
     *  ArrayToken having the same length as this Token. The field types of
     *  the argument must be comparable with the corresponding field types
     *  of this token, and the field types of the returned token is the
     *  higher type of the two.
     *  @param token The token to subtract from this token.
     *  @return A new ArrayToken.
     *  @exception IllegalActionException If the argument is not an
     *   ArrayToken, or is an ArrayToken of different length, or calling
     *   the subtract method of the element token throws it.
     */
    public Token subtract(Token token)
	    throws IllegalActionException {
	_checkArgument(token);
	Token[] argArray = ((ArrayToken)token).arrayValue();
	Token[] result = new Token[_value.length];
	for (int i = 0; i < _value.length; i++) {
	    result[i] = _value[i].subtract(argArray[i]);
	}

	return new ArrayToken(result);
    }

    /** Return the value of this token as a string that can be parsed
     *  by the expression language to recover a token with the same value.
     *  @return A String beginning with "{" that contains expressions
     *  for every element in the array separated by commas, ending with "}".
     */
    public String toString() {
	String s = "{";
	for (int i = 0; i < length(); i++) {
	    s += _value[i].toString();
	    if (i < (length()-1)) {
		s += ", ";
	    }
	}
	return s + "}";
    }

    /** Returns a new ArrayToken representing the additive identity.
     *  The returned token contains an array of the same size as the
     *  array contained by this token, and each element of the array
     *  in the returned token is the additive identity of the elements
     *  of this token.
     *  @return An ArrayToken.
     *  @exception IllegalActionException If additive identity is not
     *   supported by the element token.
     */
    public Token zero() throws IllegalActionException {
	// if this array token has length zero, return this.
	if (length() == 0) {
	    return this;
	}

	Token zeroVal = _value[0].zero();
	Token[] zeroValArray = new Token[_value.length];
	for (int i = 0; i < _value.length; i++) {
	    zeroValArray[i] = zeroVal;
	}
	return new ArrayToken(zeroValArray);
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private methods                    ////

    // Throw an exception if the argument is not an ArrayToken of the
    // same length.
    private void _checkArgument(Token token) throws IllegalActionException {
	if ( !(token instanceof ArrayToken)) {
	    throw new IllegalActionException("The argument is not " +
                    "an ArrayToken, its type was: " + token.getType() + ".");
	}

	int length = ((ArrayToken)token).length();
	if (_value.length != length) {
	    throw new IllegalActionException("The argument is an " +
                    "ArrayToken of different length.");
	}
    }

    // initialize this token using the specified array.
    private void _initialize(Token[] value) throws IllegalActionException {
	if (value.length == 0) {
	    throw new IllegalActionException("ArrayToken._initialize: The "
	            + "length of the specified array is zero.");
	}

	_elementType = value[0].getType();
	int length = value.length;
	_value = new Token[length];
	for (int i = 0; i < length; i++) {
	    if (_elementType.equals(value[i].getType())) {
	    	_value[i] = value[i];
	    } else {
		throw new IllegalActionException("ArrayToken._initialize: "
                        + "Elements of the array do not have the same type:"
			+ "value[0]=" + value[0].toString()
			+ " value[" + i + "]=" + value[i]);
	    }
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private variables                   ////

    private Token[] _value;
    private Type _elementType;
}
