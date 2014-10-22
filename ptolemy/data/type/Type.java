/** An Interface representing the Type of a Token.

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
package ptolemy.data.type;

import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// Type

/**
 An interface representing the type of a Token.

 @author Yuhong Xiong, Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 0.4
 @Pt.ProposedRating Red (neuendor)
 @Pt.AcceptedRating Red (cxh)
 */
public interface Type {

    /** Return a new type which represents the type that results from
     *  adding a token of this type and a token of the given argument
     *  type.
     *  @param rightArgumentType The type to add to this type.
     *  @return A new type, or BaseType.GENERAL, if the operation does
     *  not make sense for the given types.
     */
    public Type add(Type rightArgumentType);

    /** Return a deep clone of this type.
     *  @return A Type.
     *  @exception CloneNotSupportedException If an instance cannot be cloned.
     */
    public Object clone() throws CloneNotSupportedException;

    /** Convert the specified token into a token having the type
     *  represented by this object.
     *  @param token a token.
     *  @return a token.
     *  @exception IllegalActionException If lossless conversion
     *   cannot be done.
     */
    public Token convert(Token token) throws IllegalActionException;

    /** Return a new type which represents the type that results from
     *  dividing a token of this type and a token of the given
     *  argument type.
     *  @param rightArgumentType The type to add to this type.
     *  @return A new type, or BaseType.GENERAL, if the operation does
     *  not make sense for the given types.
     */
    public Type divide(Type rightArgumentType);

    /** Determine if the argument represents the same type as this object.
     *  @param object A Type.
     *  @return True if the argument represents the same type as this
     *   object; false otherwise.
     */
    @Override
    public boolean equals(Object object);

    /** Return a perfect hash for this type.  This number corresponds
     *  uniquely to a particular type, and is used to improve
     *  performance of certain operations in the TypeLattice class.
     *  All instances of a particular type (e.g. integer array) must
     *  return the same number.  Types that return HASH_INVALID will
     *  not have results in TypeLattice cached.  Note that it is safer
     *  to return HASH_INVALID, than to return a number that is not
     *  unique, or different number for the same type from different
     *  instances.
     *  @return A number greater than or equal to 0, or HASH_INVALID.
     */
    public int getTypeHash();

    /** Return the class for tokens that this type represents.
     *  @return The class for tokens that this type represents.
     */
    public Class getTokenClass();

    /** Return true if this type does not correspond to a single token
     *  class.  This occurs if the type is not instantiable, or it
     *  represents either an abstract base class or an interface.
     *  @return True if this type does not correspond to a single token
     *  class.
     */
    public boolean isAbstract();

    /** Test if the argument type is compatible with this type.
     *  Compatible is defined as follows: If this type is a constant, the
     *  argument is compatible if it is the same or less than this type in
     *  the type lattice; If this type is a variable, the argument is
     *  compatible if it is a substitution instance of this type.
     *  @param type An instance of Type.
     *  @return True if the argument is compatible with this type.
     */
    public boolean isCompatible(Type type);

    /** Test if this Type is a constant. A Type is a constant if it
     *  does not contain BaseType.UNKNOWN in any level within it.
     *  @return True if this type is a constant.
     */
    public boolean isConstant();

    /** Determine if this Type corresponds to an instantiable token
     *  class.
     *  @return True if this type corresponds to an instantiable
     *   token class.
     */
    public boolean isInstantiable();

    /** Return true if the specified type is a substitution instance of this
     *  type. For the argument to be a substitution instance, it must be
     *  either the same as this type, or it must be a type that can be
     *  obtained by replacing the BaseType.UNKNOWN component of this type by
     *  another type.
     *  @param type A Type.
     *  @return True if the argument is a substitution instance of this type.
     */
    public boolean isSubstitutionInstance(Type type);

    /** Return a new type which represents the type that results from
     *  moduloing a token of this type and a token of the given
     *  argument type.
     *  @param rightArgumentType The type to add to this type.
     *  @return A new type, or BaseType.GENERAL, if the operation does
     *  not make sense for the given types.
     */
    public Type modulo(Type rightArgumentType);

    /** Return a new type which represents the type that results from
     *  multiplying a token of this type and a token of the given
     *  argument type.
     *  @param rightArgumentType The type to add to this type.
     *  @return A new type, or BaseType.GENERAL, if the operation does
     *  not make sense for the given types.
     */
    public Type multiply(Type rightArgumentType);

    /** Return the type of the multiplicative identity for elements of
     *  this type.
     *  @return A new type, or BaseType.GENERAL, if the operation does
     *  not make sense for the given types.
     */
    public Type one();

    /** Return a new type which represents the type that results from
     *  subtracting a token of this type and a token of the given
     *  argument type.
     *  @param rightArgumentType The type to add to this type.
     *  @return A new type, or BaseType.GENERAL, if the operation does
     *  not make sense for the given types.
     */
    public Type subtract(Type rightArgumentType);

    /** Return the string representation of this type.
     *  @return A String.
     */
    @Override
    public String toString();

    /** Return the type of the additive identity for elements of
     *  this type.
     *  @return A new type, or BaseType.GENERAL, if the operation does
     *  not make sense for the given types.
     */
    public Type zero();

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Used to indicate that the type comparison cache is invalid.
     *  @since Ptolemy II 2.1
     */
    public static final int HASH_INVALID = Integer.MIN_VALUE;

    // The maximum size of the type hash.
    // HASH_MAX was used in TypeLattice but as of r66807 is no longer used.
    //public static final int HASH_MAX = 16;
}
