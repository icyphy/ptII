/* Tokens that contain java.security.Keys

 Copyright (c) 2003 The Regents of the University of California.
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
@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.security;

import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

import java.io.Serializable;
import java.security.Key;

//////////////////////////////////////////////////////////////////////////
//// KeyToken
/**
Tokens that contain java.security.Keys

@author Christopher Hylands Brooks, Based on TestToken by Steve Neuendorffer
@version $Id$
@since Ptolemy II 3.1
*/
public class KeyToken extends Token {

    /** Construct an empty token.
     */
    public KeyToken() {
        super();
    }

    /** Construct a token with a specified java.security.Key
     */
    public KeyToken(Key value) {
        _value = value;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public Type getType() {
        return KEY;
    }

    /** Return the java.security.Key
     */
    public java.security.Key getValue() {
        return _value;
    }

    /** Test for equality of the values of this Token and the argument
     *  Token.
     *  @exception IllegalActionException Not thrown in this base class.
     *  @return A boolean token that contains the value true if the
     *  values and units of this token and the argument token are the same.
     */
    public final BooleanToken isEqualTo(Token rightArgument)
            throws IllegalActionException {
        java.security.Key rightKey = ((KeyToken)rightArgument).getValue();
        java.security.Key leftKey = getValue();
        byte [] rightEncoded = rightKey.getEncoded();
        byte [] leftEncoded = leftKey.getEncoded();
        if (leftEncoded.equals(rightEncoded)) {
            return BooleanToken.TRUE;
        } else {
            return BooleanToken.FALSE;
        }
    }


    public String toString() {
        return "KeyToken(" + _value + ")";
    }

    public static class KeyType implements Type, Serializable {

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** Return this, that is, return the reference to this object.
         *  @return A BaseType.
         */
        public Object clone() {
            return this;
        }

        /** Convert the specified token to a token having the type
         *  represented by this object.
         *  @param t A token.
         *  @return A token.
         *  @exception IllegalActionException If lossless conversion cannot
         *   be done.
         */
        public Token convert(Token token)
                throws IllegalActionException {
            if (token instanceof KeyToken) {
                return token;
            } else {
                throw new IllegalActionException("Attempt to convert token "
                        + token +
                        " into a Key token, which is not possible.");
            }
        }

        /** Return the class for tokens that this basetype represents.
         */
        public Class getTokenClass() {
            return KeyToken.class;
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

        /** Determine if the argument represents the same BaseType as this
         *  object.
         *  @param t A Type.
         *  @return True if the argument represents the same BaseType as
         *   this object; false otherwise.
         */
        public boolean equals(Type t) {
            return this == t;
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

        /** Return the string representation of this type.
         *  @return A String.
         */
        public String toString() {
            return "Key";
        }
    }

    public static final Type KEY = new KeyType();

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The java.security.Key */
    private java.security.Key _value;
}

