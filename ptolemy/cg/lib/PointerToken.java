/* A token that contains the memory location of a C object.

 Copyright (c) 2008-2013 The Regents of the University of California.
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
package ptolemy.cg.lib;

import java.io.Serializable;

import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// PointerToken

/**
A token that contains the memory location of a C object.
Used to maintain C objects between JNI calls.
Attempts to access the object from Java will raise an IllegalActionException.

@author Teale Fristoe
@version $Id$
@since Ptolemy II 8.0
@Pt.ProposedRating Red (tbf)
@Pt.AcceptedRating Red
*/
public class PointerToken extends Token {
    /** Construct an empty token.
     */
    public PointerToken() {
        super();
    }

    /** Construct a token with the specified memory location.
     *  @param pointer The specified object referred to by this token.
     *  @exception IllegalActionException If the argument is not of
     *  the appropriate type
     *  (may be thrown by derived classes, but is not thrown here).
     */
    public PointerToken(int pointer) throws IllegalActionException {
        _value = pointer;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the type of this token.
     *  @return {@link #POINTER}, the least upper bound of all the pointer
     *  types.
     */
    public Type getType() {
        return POINTER;
    }

    /** Return the address of the pointer.
     *  @return The int that this Token was created with.
     */
    public int getValue() {
        return _value;
    }

    /** Test for equality of the values of this Token and the argument
     *  Token.  Two PointerTokens are equal if their addresses are equal.
     *
     *  @param rightArgument The Token to test against.
     *  @exception IllegalActionException Not thrown in this base class.
     *  @return A boolean token that contains the value true if the
     *  addresses are the same.
     */
    public final BooleanToken isEqualTo(Token rightArgument)
            throws IllegalActionException {
        if (!(rightArgument instanceof PointerToken)) {
            return BooleanToken.FALSE;
        }

        if (getValue() != ((PointerToken) rightArgument).getValue()) {
            return BooleanToken.FALSE;
        }

        return BooleanToken.TRUE;
    }

    /** Return a String representation of the PointerToken,
     *  including the address.
     *  @return A String representation of the PointerToken.
     */
    public String toString() {
        return "Memory @" + getValue();
    }

    /** The pointer type.
     */
    public static class PointerType implements Cloneable, Type, Serializable {
        ///////////////////////////////////////////////////////////////////
        ////                         constructors                      ////
        // The constructor is private to make a type safe enumeration.
        // We could extend BaseType, yet the BaseType(Class, String)
        // Constructor is private.
        private PointerType() {
            super();
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** Return a new type which represents the type that results from
         *  adding a token of this type and a token of the given argument
         *  type.
         *  @param rightArgumentType The type to add to this type.
         *  @return A new type, or BaseType.GENERAL, if the operation does
         *  not make sense for the given types.
         */
        public Type add(Type rightArgumentType) {
            return this;
        }

        /** Return this, that is, return the reference to this object.
         *  @return A PointerType
         */
        public Object clone() {

            // FIXME: Note that we do not call super.clone() here.  Is that right?
            return this;
        }

        /** Convert the specified token to a token having the type
         *  represented by this object.
         *  @param token A token.
         *  @return A token.
         *  @exception IllegalActionException If lossless conversion cannot
         *   be done.
         */
        public Token convert(Token token) throws IllegalActionException {
            if (token instanceof PointerToken) {
                return token;
            } else {
                throw new IllegalActionException("Attempt to convert token "
                        + token
                        + " into a Pointer token, which is not possible.");
            }
        }

        /** Return a new type which represents the type that results from
         *  dividing a token of this type and a token of the given
         *  argument type.
         *  @param rightArgumentType The type to add to this type.
         *  @return A new type, or BaseType.GENERAL, if the operation does
         *  not make sense for the given types.
         */
        public Type divide(Type rightArgumentType) {
            return this;
        }

        /** Return the class for tokens that this basetype represents.
         *  @return the class for tokens that this basetype represents.
         */
        public Class getTokenClass() {
            return PointerToken.class;
        }

        /** Return true if this type does not correspond to a single token
         *  class.  This occurs if the type is not instantiable, or it
         *  represents either an abstract base class or an interface.
         *  @return Always return false, this token is instantiable.
         */
        public boolean isAbstract() {
            return false;
        }

        /** Test if the argument type is compatible with this type.
         *  The method returns true if this type is UNKNOWN, since any type
         *  is a substitution instance of it. If this type is not UNKNOWN,
         *  this method returns true if the argument type is less than or
         *  equal to this type in the type lattice, and false otherwise.
         *  @param type An instance of Type.
         *  @return True if the argument type is compatible with this type.
         */
        public boolean isCompatible(Type type) {
            return type == this;
        }

        /** Test if this Type is UNKNOWN.
         *  @return True if this Type is not UNKNOWN; false otherwise.
         */
        public boolean isConstant() {
            return true;
        }

        /** Return this type's node index in the (constant) type lattice.
         * @return this type's node index in the (constant) type lattice.
         */
        public int getTypeHash() {
            return Type.HASH_INVALID;
        }

        /** Determine if this type corresponds to an instantiable token
         *  classes. A BaseType is instantiable if it does not correspond
         *  to an abstract token class, or an interface, or UNKNOWN.
         *  @return True if this type is instantiable.
         */
        public boolean isInstantiable() {
            return true;
        }

        /** Return true if the argument is a
         *  substitution instance of this type.
         *  @param type A Type.
         *  @return True if this type is UNKNOWN; false otherwise.
         */
        public boolean isSubstitutionInstance(Type type) {
            return this == type;
        }

        /** Return a new type which represents the type that results from
         *  moduloing a token of this type and a token of the given
         *  argument type.
         *  @param rightArgumentType The type to add to this type.
         *  @return A new type, or BaseType.GENERAL, if the operation does
         *  not make sense for the given types.
         */
        public Type modulo(Type rightArgumentType) {
            return this;
        }

        /** Return a new type which represents the type that results from
         *  multiplying a token of this type and a token of the given
         *  argument type.
         *  @param rightArgumentType The type to add to this type.
         *  @return A new type, or BaseType.GENERAL, if the operation does
         *  not make sense for the given types.
         */
        public Type multiply(Type rightArgumentType) {
            return this;
        }

        /** Return the type of the multiplicative identity for elements of
         *  this type.
         *  @return A new type, or BaseType.GENERAL, if the operation does
         *  not make sense for the given types.
         */
        public Type one() {
            return this;
        }

        /** Return a new type which represents the type that results from
         *  subtracting a token of this type and a token of the given
         *  argument type.
         *  @param rightArgumentType The type to add to this type.
         *  @return A new type, or BaseType.GENERAL, if the operation does
         *  not make sense for the given types.
         */
        public Type subtract(Type rightArgumentType) {
            return this;
        }

        /** Return the string representation of this type.
         *  @return A String.
         */
        public String toString() {
            return "Pointer";
        }

        /** Return the type of the additive identity for elements of
         *  this type.
         *  @return A new type, or BaseType.GENERAL, if the operation does
         *  not make sense for the given types.
         */
        public Type zero() {
            return this;
        }
    }

    /** The Pointer type: the least upper bound of all the pointer
     *  types.
     */
    public static final Type POINTER = new PointerType();

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The memory address of the pointer.
     */
    private int _value;

}
