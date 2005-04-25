/* The type of fixed point token classes.

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
package ptolemy.data.type;

import ptolemy.data.FixToken;
import ptolemy.data.Token;
import ptolemy.graph.CPO;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;

import java.io.Serializable;


//////////////////////////////////////////////////////////////////////////
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
public class FixType extends StructuredType implements Serializable {
    /** Construct a new fix type.
     */
    private FixType() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return this, that is, return the reference to this object.
     *  @return A FixType.
     */
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
    public Token convert(Token token) throws IllegalActionException {
        if (token instanceof FixToken) {
            return token;
        }

        throw new IllegalActionException(Token.notSupportedConversionMessage(
                token, toString()));
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

        return true;
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
        return ((typeInfo == CPO.SAME) || (typeInfo == CPO.HIGHER));
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
     *  @return True.
     */
    public boolean isInstantiable() {
        return true;
    }

    /** Test if the argument is a substitution instance of this type.
     *  @param type A Type.
     *  @return False.
     */
    public boolean isSubstitutionInstance(Type type) {
        if (type instanceof StructuredType) {
            return (((StructuredType) type)._getRepresentative() == _getRepresentative());
        } else {
            return false;
        }
    }

    /** Return the string representation of this type.
     *  @return A String.
     */
    public String toString() {
        return "fixedpoint";
    }

    /** Update this StructuredType to the specified Structured Type.
     ** The specified type must have the same structure as this type.
     *  This method will only update the component type that is
     *  BaseType.UNKNOWN, and leave the constant part of this type intact.
     *  @param newType A StructuredType.
     *  @exception IllegalActionException If the specified type has a
     *   different structure.
     */
    public void updateType(StructuredType newType)
        throws IllegalActionException {
        if (newType._getRepresentative() != _getRepresentative()) {
            throw new InternalErrorException(
                "UnsizedMatrixType.updateType: Cannot "
                + "updateType the element type to " + newType + ".");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                           public fields                   ////
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
        return CPO.SAME;

        //         if (equals(type)) {
        //             return CPO.SAME;
        //         } else {
        //             return CPO.LOWER;
        //         }
    }

    /** Return a static instance of this structured type. The return
     *  value is used by TypeLattice to represent this type.
     *  @return a StructuredType.
     */
    protected StructuredType _getRepresentative() {
        return this;
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
        return this;
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
        return this;

        //         if (equals(type)) {
        //             return this;
        //         } else {
        //             return type;
        //         }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    //  private Precision _precision;
    //  private Quantization _quantization;
    //  private Rounding _rounding;
}
