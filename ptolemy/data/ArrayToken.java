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

@ProposedRating Green (neuendor@eecs.berkeley.edu)
@AcceptedRating Yellow (cxh@eecs.berkeley.edu)
*/

package ptolemy.data;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.PtParser;
import ptolemy.data.type.Type;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.ArrayType;

//////////////////////////////////////////////////////////////////////////
//// ArrayToken
/**
A token that contains an array of tokens.  The operations between arrays
are defined pointwise, and require that the lengths of arrays are of
similar lengths.  The elements of an ArrayToken are all assumed to have the
same type, and zero length array tokens cannot be created.

@author Yuhong Xiong, Steve Neuendorffer
@version $Id$
@since Ptolemy II 0.4
*/

public class ArrayToken extends AbstractNotConvertibleToken {

    /** Construct an ArrayToken with the specified token array. All
     *  the tokens in the array must have the same type, otherwise an
     *  exception will be thrown.  Generally, the type of the array
     *  created is determined by the type of the first element in the
     *  given array.  An array of length zero implies an element type
     *  of BaseType.UNKOWN.
     *  @param value An array of tokens.
     *  @exception IllegalActionException If the tokens in the array
     *   do not have the same type.
     */
    public ArrayToken(Token[] value) throws IllegalActionException {
        _initialize(value);
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

    /** Add the given token to each element of this array. 
     *  @return An array token with the same element type as this
     *  array token.
     *  @exception IllegalActionException If the argument token is not
     *  of a type that can be added to an element of this token.
     */
    public ArrayToken addElement(Token token)
            throws IllegalActionException {
        Token[] result = new Token[_value.length];
        for (int i = 0; i < _value.length; i++) {
            result[i] = _value[i].add(token);
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
// (Token[])
//             java.lang.reflect.Array.newInstance(
//                     getElementType().getTokenClass(), 
//                     _value.length);
        System.arraycopy(_value, 0, result, 0, _value.length);
        return result;
    }

    /** Divide each element of this array by the given token. 
     *  @return An array token with the same element type as this
     *  array token.
     *  @exception IllegalActionException If the argument token is not
     *  of a type that can be divided into an element of this token.
     */
    public ArrayToken divideElement(Token token) 
            throws IllegalActionException {
        Token[] result = new Token[_value.length];
        for (int i = 0; i < _value.length; i++) {
            result[i] = _value[i].divide(token);
        }
        return new ArrayToken(result);
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
        if(_value.length > 0) {
            return _value[0].getType();
        } else {
            return BaseType.UNKNOWN;
        }
    }

    /** Return the type of this ArrayToken.
     *  @return An ArrayType.
     */
    public Type getType() {
        return new ArrayType(getElementType());
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

    /** Return the length of the contained token array.
     *  @return The length of the contained token array.
     */
    public int length() {
        return _value.length;
    }

    /** Modulo each element of this array by the given token. 
     *  @return An array token with the same element type as this
     *  array token.
     *  @exception IllegalActionException If the argument token is not
     *  of a type that can be used with modulo.
     */
    public ArrayToken moduloElement(Token token)
            throws IllegalActionException {
        Token[] result = new Token[_value.length];
        for (int i = 0; i < _value.length; i++) {
            result[i] = _value[i].modulo(token);
        }
        return new ArrayToken(result);
    }

    /** Multiply each element of this array by the given token.
     *  @return An array token with the same element type as this
     *  array token.
     *  @exception IllegalActionException If the argument token is
     *  not of a type that can be multiplied to an element of this token.
      */
    public ArrayToken multiplyElement(Token token)
            throws IllegalActionException {
        Token[] result = new Token[_value.length];
        for (int i = 0; i < _value.length; i++) {
            result[i] = _value[i].multiply(token);
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

    /** Add the given token to each element of this array.
     *  @return An array token with the same element type as this
     *  array token.
     *  @exception IllegalActionException If the argument token is not
     *  of a type that can be subtracted from an element of this token.
     */
    public ArrayToken subtractElement(Token token)
            throws IllegalActionException {
        Token[] result = new Token[_value.length];
        for (int i = 0; i < _value.length; i++) {
            result[i] = _value[i].subtract(token);
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
    ////                         protected methods                 ////

    /** Return a new token whose value is the value of the
     *  argument Token added to the value of this Token.  It is assumed
     *  that the type of the argument is the same as the type of this class.
     *  It should be overridden in derived
     *  classes to provide type specific actions for add.
     *  @param rightArgument The token whose value we add to the value of
     *   this token.
     *  @exception IllegalActionException If the argument is not an
     *   ArrayToken, or is an ArrayToken of different length, or calling
     *   the add method of the element token throws it.
     *  @return A new Token containing the result.
     */
    protected Token _add(Token rightArgument)
            throws IllegalActionException {
        _checkArgument(rightArgument);
        Token[] argArray = ((ArrayToken)rightArgument).arrayValue();
        Token[] result = new Token[_value.length];
        for (int i = 0; i < _value.length; i++) {
            result[i] = _value[i].add(argArray[i]);
        }

        return new ArrayToken(result);
    }

    /** Return a new token whose value is the value of this token
     *  divided by the value of the argument token.
     *  Type resolution also occurs here, with the returned token type
     *  chosen to achieve a lossless conversion.
     *  @param rightArgument The token to divide this token by
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument is not an
     *  ArrayToken, or is an ArrayToken of different length, or
     *  calling the divide method of the element token throws it.
     */ 
    protected Token _divide(Token rightArgument)
            throws IllegalActionException {
        _checkArgument(rightArgument);
        Token[] argArray = ((ArrayToken)rightArgument).arrayValue();
        Token[] result = new Token[_value.length];
        for (int i = 0; i < _value.length; i++) {
            result[i] = _value[i].divide(argArray[i]);
        }

        return new ArrayToken(result);
    }

    /** Test for closeness of the values of this Token and the argument
     *  Token.  It is assumed that the type of the argument is
     *  RecordToken.
     *  @param rightArgument The token to add to this token.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A BooleanToken containing the result.
     */
    protected BooleanToken _isCloseTo(Token rightArgument, double epsilon)
            throws IllegalActionException {
        _checkArgument(rightArgument);

        Token[] argArray = ((ArrayToken)rightArgument).arrayValue();
        for (int i = 0; i < _value.length; i++) {
            // Here is where isCloseTo() differs from isEqualTo().

            // Note that we return false the first time we hit an
            // element token that is not close to our current element token.
            BooleanToken result = _value[i].isCloseTo(argArray[i], epsilon);
            if (result.booleanValue() == false) {
                return BooleanToken.FALSE;
            }
        }

        return BooleanToken.TRUE;
    }

    /** Test for closeness of the values of this Token and the argument
     *  Token.  It is assumed that the type of the argument is
     *  ArrayToken.
     *  @param rightArgument The token to add to this token.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A BooleanToken containing the result.
     */
    protected BooleanToken _isEqualTo(Token rightArgument)
            throws IllegalActionException {
        _checkArgument(rightArgument);

        Token[] argArray = ((ArrayToken)rightArgument).arrayValue();
        for (int i = 0; i < _value.length; i++) {
            BooleanToken result = _value[i].isEqualTo(argArray[i]);
            if (result.booleanValue() == false) {
                return BooleanToken.FALSE;
            }
        }

        return BooleanToken.TRUE;
    }

    /** Return a new token whose value is the value of this token
     *  modulo the value of the argument token.
     *  Type resolution also occurs here, with the returned token type
     *  chosen to achieve a lossless conversion.
     *  @param rightArgument The token to modulo this token by.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument is not an
     *  ArrayToken, or is an ArrayToken of different length, or
     *  calling the modulo method of the element token throws it.
     */
    protected Token _modulo(Token rightArgument)
            throws IllegalActionException {
        _checkArgument(rightArgument);
        Token[] argArray = ((ArrayToken)rightArgument).arrayValue();
        Token[] result = new Token[_value.length];
        for (int i = 0; i < _value.length; i++) {
            result[i] = _value[i].modulo(argArray[i]);
        }

        return new ArrayToken(result);
    }

    /** Return a new token whose value is the value of this token
     *  multiplied by the value of the argument token.
     *  Type resolution also occurs here, with the returned token type
     *  chosen to achieve a lossless conversion.
     *  @param rightArgument The token to multiply this token by.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument is not an
     *  ArrayToken, or is an ArrayToken of different length, or
     *  calling the multiply method of the element token throws it.
     */
    protected Token _multiply(Token rightArgument)
            throws IllegalActionException {
        _checkArgument(rightArgument);
        Token[] argArray = ((ArrayToken)rightArgument).arrayValue();
        Token[] result = new Token[_value.length];
        for (int i = 0; i < _value.length; i++) {
            result[i] = _value[i].multiply(argArray[i]);
        }

        return new ArrayToken(result);
    }

    /** Return a new token whose value is the value of the argument token
     *  subtracted from the value of this token.
     *  Type resolution also occurs here, with the returned token type
     *  chosen to achieve a lossless conversion.
     *  @param rightArgument The token to subtract to this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException If the argument is not an
     *  ArrayToken, or is an ArrayToken of different length, or
     *  calling the subtract method of the element token throws it.
     */
    protected Token _subtract(Token rightArgument)
            throws IllegalActionException {
        _checkArgument(rightArgument);
        Token[] argArray = ((ArrayToken)rightArgument).arrayValue();
        Token[] result = new Token[_value.length];
        for (int i = 0; i < _value.length; i++) {
            result[i] = _value[i].subtract(argArray[i]);
        }

        return new ArrayToken(result);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

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
            throw new IllegalActionException("The "
                    + "length of the specified array is zero.");
        }

        Type elementType = value[0].getType();
        int length = value.length;
        _value = new Token[length];
        for (int i = 0; i < length; i++) {
            if (elementType.equals(value[i].getType())) {
                _value[i] = value[i];
            } else {
                throw new IllegalActionException(
                        "Elements of the array do not have the same type:"
                        + "value[0]=" + value[0].toString()
                        + " value[" + i + "]=" + value[i]);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Token[] _value;
}
