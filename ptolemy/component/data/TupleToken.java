/* A token that contains an array of tokens.

Copyright (c) 1997-2005 The Regents of the University of California.
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
   @Pt.ProposedRating Yellow (neuendor)
   @Pt.AcceptedRating Red (cxh)
*/
public class TupleToken extends Token {
    /** Construct a TupleToken with the specified token array as its value.
     *  @param value The value.
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

    /** Return true if the class of the argument is TupleToken and it
     *  has the same length and the elements are equal to that of this
     *  token.  Equality of the contained elements is tested by their
     *  equals() method.
     *  @param object The object to compare with.
     *  @return True if the argument is a tuple token of the same length
     *   and the elements are equal to that of this token.
     */
    public boolean equals(Object object) {
        // This test rules out instances of a subclass.
        if (object.getClass() != getClass()) {
            return false;
        }

        TupleToken tupleArgument = (TupleToken) object;
        int length = tupleArgument.length();

        if (_value.length != length) {
            return false;
        }

        Token[] array = tupleArgument._value;

        for (int i = 0; i < length; i++) {
            if (!_value[i].equals(array[i])) {
                return false;
            }
        }

        return true;
    }

    /** Return the element at the specified index.
     *  @param index The index of the desired element.
     *  @return The token contained in this array token at the
     *   specified index.
     *  @exception ArrayIndexOutOfBoundException If the specified index is
     *   outside the range of the token array.
     */
    public Token getElement(int index) {
        return _value[index];
    }

    /** Return the element type at the specified index.
     *  @param index The index of the desired element.
     *  @return The type of the token contained in this array token at the
     *   specified index.
     *  @exception ArrayIndexOutOfBoundException If the specified index is
     *   outside the range of the token array.
     */
    public Type getElementType(int index) {
        return _value[index].getType();
    }

    /** Return the type of this token, which is a TupleType populated
     *  with the types of the value of this token.
     *  @return A TupleType.
     */
    public Type getType() {
        Type[] types = new Type[_value.length];

        for (int i = 0; i < _value.length; i++) {
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

    /** Merge two tuple tokens into one by concatenating their tokens.
     *  @param first The first tuple token.
     *  @param second The second tuple token.
     *  @return The merged tuple token.
     */
    public static TupleToken merge(TupleToken first, TupleToken second) {
        if (first == VOID) {
            return second;
        }

        if (second == VOID) {
            return first;
        }

        Token[] firstTokens = first._value;
        Token[] secondTokens = second._value;
        Token[] result = new Token[firstTokens.length + secondTokens.length];
        System.arraycopy(firstTokens, 0, result, 0, firstTokens.length);
        System.arraycopy(secondTokens, 0, result, firstTokens.length,
            secondTokens.length);
        return new TupleToken(result);
    }

    /** Return a new TupleToken representing the multiplicative
     *  identity.  The returned token contains a tuple of the same
     *  size as the tuple contained by this token, where each element of
     *  the tuple in the returned token is the multiplicative identity
     *  of the corresponding element of this token.
     *  @return A TupleToken of multiplicative identities.
     *  @exception IllegalActionException If multiplicative identity is not
     *   supported by an element token.
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
     *  FIXME: This is not currently supported by the expression language.
     *  @return A string beginning with "<" that contains expressions
     *   for every element in the tuple separated by commas, ending with ">".
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer("<");

        for (int i = 0; i < _value.length; i++) {
            buffer.append(_value[i].toString());

            if (i < (_value.length - 1)) {
                buffer.append(", ");
            }
        }

        buffer.append(">");
        return buffer.toString();
    }

    /** Returns a new TupleToken representing the additive identity.
     *  The returned token contains a tuple of the same size as the
     *  tuple contained by this token, and each element of the tuple
     *  in the returned token is the additive identity of the
     *  corresponding element of this token.
     *  @return A TupleToken with additive identities.
     *  @exception IllegalActionException If the additive identity is not
     *   supported by an element token.
     */
    public Token zero() throws IllegalActionException {
        Token[] zeroValueTuple = new Token[_value.length];

        for (int i = 0; i < _value.length; i++) {
            zeroValueTuple[i] = _value[i].zero();
        }

        return new TupleToken(zeroValueTuple);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** An empty tuple token. */
    public static final TupleToken VOID = new TupleToken(new Token[0]);

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // Initialize this token using the specified array.
    private void _initialize(Token[] value) {
        int length = value.length;
        _value = new Token[length];

        for (int i = 0; i < length; i++) {
            _value[i] = value[i];
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private Token[] _value;
}
