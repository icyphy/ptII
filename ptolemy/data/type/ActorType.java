/* The base type of matrix token classes.

 Copyright (c) 2006-2014 The Regents of the University of California.
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
package ptolemy.data.type;

import ptolemy.data.ActorToken;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// ActorType

/**
 The base type of matrix token classes. This type functions as a union
 of the various matrix types. It allows for the creation of arrays
 that consist of diverse matrix types, because the array type will
 be {matrix}.

 @author Steve Neuendorffer and Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (yuhong)
 @Pt.AcceptedRating Red
 */
public class ActorType implements Type, Cloneable {

    public ActorType() {
        super();
        BaseType._addType(this, "actor", ActorToken.class);
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
    @Override
    public Type add(Type rightArgumentType) {
        return this;
    }

    /** Return this, that is, return the reference to this object.
     *  @return A BaseType.
     */
    @Override
    public Object clone() {
        return this;
    }

    /** Convert the specified token to a token having the type
     *  represented by this object.
     *  @param token A token.
     *  @return A token.
     *  @exception IllegalActionException If lossless conversion cannot
     *   be done.
     */
    @Override
    public Token convert(Token token) throws IllegalActionException {
        if (token instanceof ActorToken) {
            return token;
        } else {
            throw new IllegalActionException("Attempt to convert token " + token
                    + " into a test token, which is not possible.");
        }
    }

    /** Return a new type which represents the type that results from
     *  dividing a token of this type and a token of the given
     *  argument type.
     *  @param rightArgumentType The type to add to this type.
     *  @return A new type, or BaseType.GENERAL, if the operation does
     *  not make sense for the given types.
     */
    @Override
    public Type divide(Type rightArgumentType) {
        return this;
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

    /** Return the class for tokens that this basetype represents.
     *  @return the class for tokens that this basetype represents.
     */
    @Override
    public Class getTokenClass() {
        return ActorToken.class;
    }

    /** Return this type's node index in the (constant) type lattice.
     * @return this type's node index in the (constant) type lattice.
     */
    @Override
    public int getTypeHash() {
        return Type.HASH_INVALID;
    }

    /** Return true if this type does not correspond to a single token
     *  class.  This occurs if the type is not instantiable, or it
     *  represents either an abstract base class or an interface.
     *  @return Always return false, this token is instantiable.
     */
    @Override
    public boolean isAbstract() {
        return false;
    }

    /** Model if the argument type is compatible with this type.
     *  The method returns true if this type is UNKNOWN, since any type
     *  is a substitution instance of it. If this type is not UNKNOWN,
     *  this method returns true if the argument type is less than or
     *  equal to this type in the type lattice, and false otherwise.
     *  @param type An instance of Type.
     *  @return True if the argument type is compatible with this type.
     */
    @Override
    public boolean isCompatible(Type type) {
        return type == this;
    }

    /** Model if this Type is UNKNOWN.
     *  @return True if this Type is not UNKNOWN; false otherwise.
     */
    @Override
    public boolean isConstant() {
        return true;
    }

    /** Determine if this type corresponds to an instantiable token
     *  classes. A BaseType is instantiable if it does not correspond
     *  to an abstract token class, or an interface, or UNKNOWN.
     *  @return True if this type is instantiable.
     */
    @Override
    public boolean isInstantiable() {
        return true;
    }

    /** Return true if the argument is a
     *  substitution instance of this type.
     *  @param type A Type.
     *  @return True if this type is UNKNOWN; false otherwise.
     */
    @Override
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
    @Override
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
    @Override
    public Type multiply(Type rightArgumentType) {
        return this;
    }

    /** Return the type of the multiplicative identity for elements of
     *  this type.
     *  @return A new type, or BaseType.GENERAL, if the operation does
     *  not make sense for the given types.
     */
    @Override
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
    @Override
    public Type subtract(Type rightArgumentType) {
        return this;
    }

    /** Return the string representation of this type.
     *  @return A String.
     */
    @Override
    public String toString() {
        return "actor";
    }

    /** Return the type of the additive identity for elements of
     *  this type.
     *  @return A new type, or BaseType.GENERAL, if the operation does
     *  not make sense for the given types.
     */
    @Override
    public Type zero() {
        return this;
    }
}
