/* A token that contains an array of tokens.

 Copyright (c) 1997-2000 The Regents of the University of California.
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

@ProposedRating Red (yuhong@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.data;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.data.type.Type;
import ptolemy.data.type.ArrayType;

//////////////////////////////////////////////////////////////////////////
//// ArrayToken
/**
A token that contains an array of tokens.

@author Yuhong Xiong
@version $Id$
*/

// FIXME: to support operations like adding each element of an array
// with a certain value, an extra set of methods like elementAdd(),
// elementSubtract(), etc may be needed.

public class ArrayToken extends Token {

    /** Construct an ArrayToken with the specified token array. All the
     *  tokens in the array must have the same type, otherwise an
     *  exception will be thrown.
     *  @param value An array of tokens.
     *  @exception IllegalArgumentException If the tokens in the array
     *   do not have the same type.
     */
    public ArrayToken(Token[] value) {
	_elementType = value[0].getType();
	int length = value.length;
	_value = new Token[length];
	for (int i = 0; i < length; i++) {
	    if (_elementType.isEqualTo(value[i].getType())) {
	    	_value[i] = value[i];
	    } else {
		throw new IllegalArgumentException("ArrayToken: " +
                        "Elements of the array do not have the same type.");
	    }
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a new token whose value is the sum of this token and
     *  the argument. The type of the argument must be comparable
     *  with the type of this token. The type of the returned token is
     *  the higher type of the two.
     *  @param t The token to add to this token.
     *  @return A new ArrayToken.
     *  @exception IllegalActionException If the argument is not an
     *   ArrayToken, or is an ArrayToken of different length, or calling
     *   the add method of the element token throws it.
     */
    public Token add(Token t)
	    throws IllegalActionException {
	_checkArgument(t);
	Token[] argArray = ((ArrayToken)t).arrayValue();
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

    /** Throw an exception. Use the convert method in ArrayType.
     *  @exception IllegalActionException Always thrown.
     */
    public static Token convert(Token t)
	    throws IllegalActionException {
	throw new IllegalActionException("ArrayToken.convert: " +
                "This method cannot be used, use the convert method " +
                "in ArrayType.");
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

    /** Return the type of this ArrayToken.
     *  @return An ArrayType.
     */
    public Type getType() {
	return new ArrayType(_elementType);
    }

    /** Test for equality of the values of this Token and the argument.
     *  @param t The token with which to test equality.
     *  @return A new BooleanToken which contains the result of the test.
     *  @exception IllegalActionException If the argument is not an
     *   ArrayToken, or is an ArrayToken of different length, or calling
     *   the isEqualTo method of the element token throws it.
     */
    public BooleanToken isEqualTo(Token t)
	    throws IllegalActionException {

	_checkArgument(t);
	Token[] argArray = ((ArrayToken)t).arrayValue();
	for (int i = 0; i < _value.length; i++) {
	    BooleanToken result = _value[i].isEqualTo(argArray[i]);
	    if (result.booleanValue() == false) {
		return new BooleanToken(false);
	    }
	}

	return new BooleanToken(true);
    }

    /** Return the length of the contained token array.
     *  @return an Int.
     */
    public int length() {
	return _value.length;
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
    public Token one()
	    throws IllegalActionException {
	Token oneVal = _value[0].one();
	Token[] oneValArray = new Token[_value.length];
	for (int i = 0; i < _value.length; i++) {
	    oneValArray[i] = oneVal;
	}
	return new ArrayToken(oneValArray);
    }

    /** Return the value of this token as a string that can be parsed
     *  by the expression language to recover a token with the same value.
     *  The syntax is similar to a Matlab row vector.
     *  @return A String beginning with "[" that contains expressions 
     *  for every element in the array separated by commas, ending with "]".
     */
    public String toString() {
	String s = "[";
	for (int i = 0; i < length(); i++) {
	    s += _value[i].toString();
	    if (i < (length()-1)) {
		s += ", ";
	    }
	}
	return s + "]";
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
    public Token zero()
	    throws IllegalActionException {
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
    private void _checkArgument(Token t)
	    throws IllegalActionException {
	if ( !(t instanceof ArrayToken)) {
	    throw new IllegalActionException("The argument is not " +
                    "an ArrayToken.");
	}

	int length = ((ArrayToken)t).length();
	if (_value.length != length) {
	    throw new IllegalActionException("The argument is an " +
                    "ArrayToken of different length.");
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private variables                   ////

    private Token[] _value;
    private Type _elementType;
}
