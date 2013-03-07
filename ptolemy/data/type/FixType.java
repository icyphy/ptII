/* The type of fixed point token classes.

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

import java.io.Serializable;

import ptolemy.data.FixToken;
import ptolemy.data.Token;
import ptolemy.graph.CPO;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.math.FixPoint;
import ptolemy.math.FixPointQuantization;
import ptolemy.math.Overflow;
import ptolemy.math.Precision;
import ptolemy.math.Rounding;

///////////////////////////////////////////////////////////////////
//// FixType

/**
 This class represents the type of fix point token objects.  Generally the
 type of a fix point token includes the precision of the token, along with
 the rounding and quantization techniques that are being applied.

 @author Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 2.1
 @Pt.ProposedRating Red (neuendor)
 @Pt.AcceptedRating Red
 */
public class FixType extends StructuredType implements Cloneable, Serializable {
    /** Construct a new fix type, with no integer bits and no
     * fractional bits.  This (rather useless) type represents the
     * bottom of the FixPoint type lattice.
     */
    public FixType() {
        _precision = new Precision(0, 0);
    }

    /** Construct a new fix type.
     *  @param precision The precision.
     */
    public FixType(Precision precision) {
        _precision = precision;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a new type which represents the type that results from
     *  adding a token of this type and a token of the given argument
     *  type.
     *
     *  @param rightArgumentType The type to add to this type.
     *  @return A new type, or BaseType.GENERAL, if the operation does
     *  not make sense for the given types.
     */
    public Type add(Type rightArgumentType) {
        if (rightArgumentType instanceof FixType) {
            Precision rPrecision = ((FixType) rightArgumentType).getPrecision();
            Precision newPrecision = FixPoint.addPrecision(rPrecision,
                    _precision);
            FixType returnType = new FixType(newPrecision);
            return returnType;
        } else {
            return TypeLattice.leastUpperBound(this, rightArgumentType);
        }
    }

    /** Return this, that is, return the reference to this object.
     *  @return A FixType.
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
        if (token.getType() instanceof FixType
                && (_compare((FixType) token.getType()) == CPO.SAME || _compare((FixType) token
                        .getType()) == CPO.HIGHER)) {

            // The overflow and rounding modes could be anything here,
            // since the above check should ensure that rounding and
            // overflow will never occur.
            return ((FixToken) token).quantize(new FixPointQuantization(
                    getPrecision(), Overflow.GROW, Rounding.HALF_EVEN));
        }

        throw new IllegalActionException(Token.notSupportedConversionMessage(
                token, toString()));
    }

    /** Return a new type which represents the type that results from
     *  dividing a token of this type and a token of the given
     *  argument type.
     *  @param rightArgumentType The type to add to this type.
     *  @return A new type, or BaseType.GENERAL, if the operation does
     *  not make sense for the given types.
     */
    public Type divide(Type rightArgumentType) {
        if (rightArgumentType instanceof FixType) {
            Precision rPrecision = ((FixType) rightArgumentType).getPrecision();
            Precision newPrecision = FixPoint.dividePrecision(rPrecision,
                    _precision);
            FixType returnType = new FixType(newPrecision);
            return returnType;
        } else {
            return TypeLattice.leastUpperBound(this, rightArgumentType);
        }
    }

    /** Determine if the argument represents the same FixType as this
     *  object.
     *  @param object A Type.
     *  @return Always return true
     */
    public boolean equals(Object object) {
        if (!(object instanceof FixType)) {
            return false;
        }
        Precision precision = ((FixType) object).getPrecision();
        if (!precision.equals(_precision)) {
            return false;
        }

        return true;
    }

    /** Return the precision associated with this FixType.
     *  @return A Precision.
     */
    public Precision getPrecision() {
        return _precision;
    }

    /** Return the class for tokens that this type represents.
     *  @return The class representing ptolemy.data.token.FixToken.
     */
    public Class getTokenClass() {
        return FixToken.class;
    }

    /** Return a hash code value for this object.
     *  @return The hash code for the token class of this type.
     */
    public int hashCode() {
        return getTokenClass().hashCode();
    }

    /** Set the elements that have declared type BaseType.UNKNOWN to the
     *  specified type.
     *  @param type A Type.
     */
    public void initialize(Type type) {
        // Ignore... This type has no components that are unknown.
    }

    /** Return true if this type does not correspond to a single token
     *  class.  This occurs if the type is not instantiable, or it
     *  represents either an abstract base class or an interface.
     *  This method should be overridden in derived classes to return
     *  true only for types which are not abstract.
     *  @return true.
     */
    public boolean isAbstract() {
        return !isInstantiable();
    }

    /** Test if the argument type is compatible with this type. The method
     *  returns true if this type is UNKNOWN, since any type is a substitution
     *  instance of it. If this type is not UNKNOWN, this method returns true
     *  if the argument type is less than or equal to this type in the type
     *  lattice, and false otherwise.
     *  @param type An instance of Type.
     *  @return True if the argument is compatible with this type.
     */
    public boolean isCompatible(Type type) {
        int typeInfo = TypeLattice.compare(this, type);
        return typeInfo == CPO.SAME || typeInfo == CPO.HIGHER;
    }

    /** Test if this Type is a constant. A Type is a constant if it
     *  does not contain BaseType.UNKNOWN in any level within it.
     *  @return False.
     */
    public boolean isConstant() {
        return true;
    }

    /** Test if this type corresponds to an instantiable token
     *  classes.
     *  @return True if the precision of this fix type has any bits.
     */
    public boolean isInstantiable() {
        if (_precision.getNumberOfBits() == 0) {
            return false;
        } else {
            return true;
        }
    }

    /** Test if the argument is a substitution instance of this type.
     *  @param type A Type.
     *  @return False.
     */
    public boolean isSubstitutionInstance(Type type) {
        if (type instanceof StructuredType) {
            return ((StructuredType) type)._getRepresentative() == _getRepresentative();
        } else {
            return false;
        }
    }

    /** Return a new type which represents the type that results from
     *  moduloing a token of this type and a token of the given
     *  argument type.
     *  @param rightArgumentType The type to add to this type.
     *  @return A new type, or BaseType.GENERAL, if the operation does
     *  not make sense for the given types.
     */
    public Type modulo(Type rightArgumentType) {
        // FIXME...  deal with precisions correctly.
        return TypeLattice.leastUpperBound(this, rightArgumentType);
    }

    /** Return a new type which represents the type that results from
     *  multiplying a token of this type and a token of the given
     *  argument type.
     *  The resulting Precision of a multiply between two FixType
     *  arguments is as follows: the integer location is the sum
     *  of the integer locations of the two arguments and the
     *  fractional location is the sum of the fractional locations
     *  of the two arguments.
     *
     *  @param rightArgumentType The type to add to this type.
     *  @return A new type, or BaseType.GENERAL, if the operation does
     *  not make sense for the given types.
     */
    public Type multiply(Type rightArgumentType) {
        if (rightArgumentType instanceof FixType) {
            Precision rPrecision = ((FixType) rightArgumentType).getPrecision();
            Precision newPrecision = FixPoint.multiplyPrecision(rPrecision,
                    _precision);
            FixType returnType = new FixType(newPrecision);
            return returnType;
        } else {
            return TypeLattice.leastUpperBound(this, rightArgumentType);
        }
    }

    /** Return the type of the multiplicative identity for elements of
     *  this type.
     *  @return A new type, or BaseType.GENERAL, if the operation does
     *  not make sense for the given types.
     */
    public Type one() {
        // FIXME...  deal with precisions correctly.
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
        if (rightArgumentType instanceof FixType) {
            Precision rPrecision = ((FixType) rightArgumentType).getPrecision();
            Precision newPrecision = FixPoint.subtractPrecision(rPrecision,
                    _precision);
            FixType returnType = new FixType(newPrecision);
            return returnType;
        } else {
            return TypeLattice.leastUpperBound(this, rightArgumentType);
        }
    }

    /** Return the string representation of this type.
     *  @return A String.
     */
    public String toString() {
        return "fixedpoint"
                + _precision.toString(Precision.EXPRESSION_LANGUAGE);
    }

    /** Update this StructuredType to the specified Structured Type.
     *  The specified type must have the same structure as this type.
     *  This method will only update the component type that is
     *  BaseType.UNKNOWN, and leave the constant part of this type intact.
     *  @param newType A StructuredType.
     *  @exception IllegalActionException If the specified type has a
     *   different structure.
     */
    public void updateType(StructuredType newType)
            throws IllegalActionException {
        super.updateType(newType);
        if (newType._getRepresentative() != _getRepresentative()) {
            throw new InternalErrorException("FixType.updateType: Cannot "
                    + "updateType the element type to " + newType + ".");
        }
    }

    /** Return the type of the additive identity for elements of
     *  this type.
     *  @return A new type, or BaseType.GENERAL, if the operation does
     *  not make sense for the given types.
     */
    public Type zero() {
        // FIXME...  deal with precisions correctly.
        return this;
    }

    ///////////////////////////////////////////////////////////////////
    ////                           public fields                   ////

    /** The bottom fix type. */
    public static final FixType BOTTOM = new FixType();

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
    protected int _compare(StructuredType type) {
        if (!(type instanceof FixType)) {
            throw new IllegalArgumentException("FixType._compare: "
                    + "The argument is not a FixType.");
        }

        Precision precision = ((FixType) type).getPrecision();

        int fractionBits1 = _precision.getFractionBitLength();
        int fractionBits2 = precision.getFractionBitLength();
        int integerBits1 = _precision.getFractionBitLength();
        int integerBits2 = precision.getFractionBitLength();
        boolean signBit1 = _precision.isSigned();
        boolean signBit2 = precision.isSigned();
        int compareBits1, compareBits2;

        if (_precision.equals(precision)) {
            return CPO.SAME;
        } else if (signBit1 == signBit2) {
            if (fractionBits1 < fractionBits2 && integerBits1 < integerBits2) {
                return CPO.LOWER;

            } else if (fractionBits1 > fractionBits2
                    && integerBits1 > integerBits2) {
                return CPO.HIGHER;

            } else if (integerBits1 == integerBits2) {
                compareBits1 = fractionBits1;
                compareBits2 = fractionBits2;
            } else {
                compareBits1 = integerBits1;
                compareBits2 = integerBits2;
            }

            if (compareBits1 < compareBits2) {
                return CPO.LOWER;
            } else if (compareBits1 > compareBits2) {
                return CPO.HIGHER;
            } else { // (signBit1 == signBit2)
                return CPO.SAME;
            }
        } else if (signBit1 && !signBit2) {
            if (fractionBits1 >= fractionBits2 && integerBits1 >= integerBits2) {
                return CPO.HIGHER;
            }
        } else {
            if (fractionBits1 <= fractionBits2 && integerBits1 <= integerBits2) {
                return CPO.LOWER;
            }
        }
        return CPO.INCOMPARABLE;
    }

    /** Return a static instance of this structured type. The return
     *  value is used by TypeLattice to represent this type.
     *  @return a StructuredType.
     */
    protected StructuredType _getRepresentative() {
        return FixType.BOTTOM;
    }

    /** Return the greatest lower bound of this type with the specified
     *  type. The specified type must be of the same structured type,
     *  otherwise an exception will be thrown.
     *  @param type a StructuredType.
     *  @return a StructuredType.
     *  @exception IllegalArgumentException If the specified type is
     *   not the same structured type as this one.
     */
    protected StructuredType _greatestLowerBound(StructuredType type) {
        if (!(type instanceof FixType)) {
            throw new IllegalArgumentException("FixType._greatestLowerBound: "
                    + "The argument is not a FixType.");
        }
        Precision precision = ((FixType) type).getPrecision();
        int fractionBits = Math.min(precision.getFractionBitLength(),
                _precision.getFractionBitLength());
        int integerBits = Math.min(precision.getIntegerBitLength(),
                _precision.getIntegerBitLength());
        return new FixType(new Precision(fractionBits + integerBits,
                integerBits));
    }

    /** Return the least upper bound of this type with the specified
     *  type. The specified type must be of the same structured type,
     *  otherwise an exception will be thrown.
     *  @param type a StructuredType.
     *  @return a StructuredType.
     *  @exception IllegalArgumentException If the specified type is
     *   not the same structured type as this one.
     */
    protected StructuredType _leastUpperBound(StructuredType type) {
        if (!(type instanceof FixType)) {
            throw new IllegalArgumentException("FixType._greatestLowerBound: "
                    + "The argument is not a FixType.");
        }
        Precision precision = ((FixType) type).getPrecision();
        int fractionBits = Math.max(precision.getFractionBitLength(),
                _precision.getFractionBitLength());
        int integerBits = Math.max(precision.getIntegerBitLength(),
                _precision.getIntegerBitLength());
        FixType returnType = new FixType(new Precision(fractionBits
                + integerBits, integerBits));
        returnType._checkPrecision();
        return returnType;
    }

    /** Check the precision.
     *  If the number of bits is greater than 128, throw an exception.
     */
    protected void _checkPrecision() {
        if (_precision.getNumberOfBits() > 128) {
            throw new RuntimeException(
                    "Large fixed point type detected during type resolution."
                            + "  The structured type "
                            + this
                            + " has depth larger than the bound "
                            + 128
                            + ".  This may be an indicator of type constraints "
                            + "in a model with no finite solution, which may occur "
                            + "if there is a feedback loop that requires an "
                            + "explicit FixToFix conversion actor.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private Precision _precision;
    //  private Quantization _quantization;
    //  private Rounding _rounding;
}
