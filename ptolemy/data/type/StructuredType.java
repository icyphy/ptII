/** Base class for structured type.

 Copyright (c) 1997-2013 The Regents of the University of California.
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

import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// StructuredType

/**
 Base class for structured type. Making this an abstract class (not an
 interface) allows the methods to be protected.
 All the types of the same structured type (e.g. all the array types)
 must form a lattice. Each instance of a structured type must know how
 to compare itself with another instance of the same structured type,
 and compute the least upper bound and greatest lower bound. This
 class defines methods for these operations.
 <p>
 Subclasses should override clone() to do a deep cloning.

 @author Yuhong Xiong, Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 0.4
 @Pt.ProposedRating Red (yuhong)
 @Pt.AcceptedRating Red (cxh)
 */
public abstract class StructuredType implements Type {
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
        return TypeLattice.leastUpperBound(this, rightArgumentType);
    }

    /** Return a deep copy of this StructuredType.
     *  @return A StructuredType.
     *  @exception CloneNotSupportedException Not thrown in this base class.
     */
    @Override
    abstract public Object clone() throws CloneNotSupportedException;

    /** Return a new type which represents the type that results from
     *  dividing a token of this type and a token of the given
     *  argument type.
     *  @param rightArgumentType The type to divide with this type.
     *  @return A new type, or BaseType.GENERAL, if the operation does
     *  not make sense for the given types.
     */
    @Override
    public Type divide(Type rightArgumentType) {
        return TypeLattice.leastUpperBound(this, rightArgumentType);
    }

    /** Return the depth of a structured type. The depth of a
     *  structured type is the number of times a structured type
     *  contains other structured types. For example, an array
     *  of arrays has depth 2, and an array of arrays of records
     *  has depth 3.
     *  @return the depth of a structured type.
     */
    public int depth() {
        return 1;
    }

    /** Return a perfect hash for this type.  This number corresponds
     *  uniquely to a particular type, and is used to improve
     *  performance of certain operations in the TypeLattice class.
     *  All instances of a particular type (e.g. integer array) must
     *  return the same number.  Types that return HASH_INVALID will
     *  not have results in TypeLattice cached.  Note that it is safer
     *  to return HASH_INVALID, than to return a number that is not
     *  unique, or different number for the same type from different
     *  instances.  This base class returns HASH_INVALID.
     *  @return A number greater than or equal to 0, or HASH_INVALID.
     */
    @Override
    public int getTypeHash() {
        return Type.HASH_INVALID;
    }

    /** Return true if this type does not correspond to a single token
     *  class.  This occurs if the type is not instantiable, or it
     *  represents either an abstract base class or an interface.
     *  This method should be overridden in derived classes to return
     *  true only for types which are not abstract.
     *  @return true.
     */
    @Override
    public boolean isAbstract() {
        return true;

    }

    /** Set the elements that have declared type BaseType.UNKNOWN to the
     *  specified type.
     *  @param type A Type.
     */
    public abstract void initialize(Type type);

    /** Return a new type which represents the type that results from
     *  moduloing a token of this type and a token of the given
     *  argument type.
     *  @param rightArgumentType The type to add to this type.
     *  @return A new type, or BaseType.GENERAL, if the operation does
     *  not make sense for the given types.
     */
    @Override
    public Type modulo(Type rightArgumentType) {
        return TypeLattice.leastUpperBound(this, rightArgumentType);
    }

    /** Return a new type which represents the type that results from
     *  multiplying a token of this type and a token of the given
     *  argument type.
     *  @param rightArgumentType The type to multiply by this type.
     *  @return A new type, or BaseType.GENERAL, if the operation does
     *  not make sense for the given types.
     */
    @Override
    public Type multiply(Type rightArgumentType) {
        return TypeLattice.leastUpperBound(this, rightArgumentType);
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
        return TypeLattice.leastUpperBound(this, rightArgumentType);
    }

    /** Update this StructuredType to the specified Structured Type.
     *  The specified type must have depth less than the MAXDEPTHBOUND, and
     *  have the same structure as this type.
     *  This method will only update the component type that is
     *  BaseType.UNKNOWN, and leave the constant part of this type intact.
     *  @param newType A StructuredType.
     *  @exception IllegalActionException If the specified type has a
     *   different structure.
     */
    public void updateType(StructuredType newType)
            throws IllegalActionException {
        if (newType.depth() >= MAXDEPTHBOUND) {
            throw new IllegalActionException(
                    "Large type structure detected during type resolution."
                            + "  The structured type "
                            + newType.toString()
                            + " has depth larger than the bound "
                            + MAXDEPTHBOUND
                            + ".  This may be an indicator of type constraints "
                            + "in a model with no finite solution.");
        }
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

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Compare this type with the specified type. The specified type
     *  must be of the same structured type, otherwise an exception will
     *  be thrown.
     *  This method returns one of ptolemy.graph.CPO.LOWER,
     *  ptolemy.graph.CPO.SAME, ptolemy.graph.CPO.HIGHER,
     *  ptolemy.graph.CPO.INCOMPARABLE, indicating this type is lower
     *  than, equal to, higher than, or incomparable with the
     *  specified type in the type hierarchy, respectively.
     *  @param type a StructuredType.
     *  @return An integer.
     *  @exception IllegalArgumentException If the specified type is
     *   not the same structured type as this one.
     */
    protected abstract int _compare(StructuredType type);

    /** Return a static instance of this structured type. The return
     *  value is used by TypeLattice to represent this type.
     *  @return a StructuredType.
     */
    protected abstract StructuredType _getRepresentative();

    /** Return the greatest lower bound of this type with the specified
     *  type. The specified type must be of the same structured type,
     *  otherwise an exception will be thrown.
     *  @param type a StructuredType.
     *  @return a StructuredType.
     *  @exception IllegalArgumentException If the specified type is
     *   not the same structured type as this one.
     */
    protected abstract StructuredType _greatestLowerBound(StructuredType type);

    /** Return the least upper bound of this type with the specified
     *  type. The specified type must be of the same structured type,
     *  otherwise an exception will be thrown.
     *  @param type a StructuredType.
     *  @return a StructuredType.
     *  @exception IllegalArgumentException If the specified type is
     *   not the same structured type as this one.
     */
    protected abstract StructuredType _leastUpperBound(StructuredType type);

    /** Set up a bound for the max depth of structured types. This bound
     *  is used to detect infinite iterations.
     */
    protected static final int MAXDEPTHBOUND = 20;
}
