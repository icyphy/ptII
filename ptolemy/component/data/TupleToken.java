/* A token that contains an array of tokens.

Copyright (c) 1997-2004 The Regents of the University of California.
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

package ptolemy.component.data;

import ptolemy.component.data.type.TupleType;
import ptolemy.data.ArrayToken;
import ptolemy.data.Token;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// TupleToken
/**
   A token that contains an ordered set of tokens.

   @author Yang Zhao
   @version $Id$
   @since Ptolemy II 0.4
   @Pt.ProposedRating Green (neuendor)
   @Pt.AcceptedRating Green (cxh)
*/

public class TupleToken extends Token {
    /** Construct an TupleToken with the specified token array. 
     *  @param value An array of tokens.
     *  @exception IllegalActionException FIXME...
     */
    public TupleToken(Token[] value) {
        _initialize(value);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /** Return an array of tokens populated with the contents of this
     *  array token.  The returned array is a copy so the caller is
     *  free to modify it.
     *  @return An array of tokens.
     */
    public Token[] tupleValue() {
        Token[] result = new Token[_value.length];
        // This code will create a token array of a more specific type
        // than token.  Eventually, we would like to use this code
        // since it will simplify writing some actors, but for the
        // moment the code generator cannot deal with it.
        // (Token[])
        //             java.lang.reflect.Array.newInstance(
        //                     getElementType().getTokenClass(),
        //                     _value.length);
        System.arraycopy(_value, 0, result, 0, _value.length);
        return result;
    }

    /** Return true if the class of the argument is ArrayToken and of
     *  the same length and the elements are equal to that of this
     *  token.  Equality of the contained elements is tested by their
     *  equals() method.
     *  @param object An instance of Object.
     *  @return True if the argument is an array token of the same length
     *   and the elements are equal to that of this token.
     */
    public boolean equals(Object object) {
        // This test rules out instances of a subclass.
        if (object.getClass() != getClass()) {
            return false;
        }

        TupleToken tupleArgument = (TupleToken)object;
        int length = tupleArgument.length();
        if (_value.length != length) {
            return false;
        }

        Token[] array = tupleArgument.tupleValue();
        for (int i = 0; i < length; i++) {
            if ( !_value[i].equals(array[i])) {
                return false;
            }
        }

        return true;
    }

    /** Return the element at the specified index.
     *  @param index The index of the desired element.
     *  @return The token contained in this array token at the
     *  specified index.
     *  @exception ArrayIndexOutOfBoundException If the specified index is
     *   outside the range of the token array.
     */
    public Token getElement(int index) {
        return _value[index];
    }
    
    /** Return the element at the specified index.
     *  @param index The index of the desired element.
     *  @return The token contained in this array token at the
     *  specified index.
     *  @exception ArrayIndexOutOfBoundException If the specified index is
     *   outside the range of the token array.
     */
    public Type getElementType(int index) {
        return _value[index].getType();
    }

    /** Return the type of this ArrayToken.
     *  @return An ArrayType.
     */
    public Type getType() {
        Type[] types = new Type[_value.length];
        for (int i =0; i < _value.length; i++) {
            types[i] = getElementType(i);
        }
        return new TupleType(types);
    }

    /** Return the length of the contained token array.
     *  @return The length of the contained token array.
     */
    public int length() {
        return _value.length;
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
    public Token one() throws IllegalActionException {
        Token[] oneValueTuple = new Token[_value.length];
        for (int i = 0; i < _value.length; i++) {
            oneValueTuple[i] = _value[i].one();
        }
        return new TupleToken(oneValueTuple);
    }

    /** Return the value of this token as a string that can be parsed
     *  by the expression language to recover a token with the same value.
     *  @return A string beginning with "{" that contains expressions
     *  for every element in the array separated by commas, ending with "}".
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer("{");
        for (int i = 0; i < _value.length; i++) {
            buffer.append(_value[i].toString());
            if (i < (_value.length - 1)) {
                buffer.append(", ");
            }
        }
        buffer.append("}");
        return buffer.toString();
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
    public Token zero() throws IllegalActionException {
        Token[] zeroValueTuple = new Token[_value.length];
        for (int i = 0; i < _value.length; i++) {
            zeroValueTuple[i] = _value[i].zero();
        }
        return new ArrayToken(zeroValueTuple);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Throw an exception if the argument is not a TupleToken of the
    // same length.
    private void _checkArgumentLength(Token token)
            throws IllegalActionException {

        int length = ((TupleToken)token).length();
        if (length() != length) {
            throw new IllegalActionException("The length of the argument (" +
                    length + ") is not the same as the length of this token ("
                    + length() + ").");
        }
    }

    // initialize this token using the specified array.
    private void _initialize(Token[] value){
        int length = value.length;
        _value = new Token[length];
        for (int i = 0; i < length; i++) {
            _value[i] = value[i];// elementType.convert(value[i]);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Token[] _value;
    public static Token[] _VOID = new Token[0];
    public static TupleToken _VOIDTUPLE = new TupleToken(_VOID);
}
