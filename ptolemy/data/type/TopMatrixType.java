/* The base type of matrix token classes.

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

import java.io.Serializable;

import ptolemy.data.MatrixToken;
import ptolemy.data.Token;
import ptolemy.graph.CPO;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;

//////////////////////////////////////////////////////////////////////////
//// TopMatrixType

/**
 The least upper bound for all matrix types.

 @author Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (yuhong)
 @Pt.AcceptedRating Red
 */
public class TopMatrixType extends StructuredType implements Serializable {
    /** Construct a new matrix type that represents the top element of all
     *  matrix types.
     */
    private TopMatrixType() {
        try {
            BaseType._addType(this, "[general]", MatrixToken.class);
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return this, that is, return the reference to this object.
     *  @return A MatrixType.
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
        // Any matrix token is a valid instance of this type.
        // This is useful because there are some things like
        // the matrix visualizer that can meaningfully accept any token
        // type.
        if (token instanceof MatrixToken) {
            return token;
        } else {
            throw new IllegalActionException("Cannot convert " + token
                    + " to TopMatrixType");
        }
    }

    /** Determine if the argument represents the same MatrixType as this
     *  object.
     *  @param object A Type.
     *  @return True if the argument type is a matrix type representing the
     *  same class, with the same number of rows and columns.
     */
    public boolean equals(Object object) {
        return object == getInstance();
    }

    /** Return the singleton instance of this class.
     */
    public static TopMatrixType getInstance() {
        return _instance;
    }

    /** Return the class for tokens that this type represents.
     */
    public Class getTokenClass() {
        return Token.class;
    }

    /** Return a hash code value for this object.
     *  @return The hash code for the TopMatrixType class.
     */
    public int hashCode() {
        return getClass().hashCode();
    }

    /** Set the elements that have declared type BaseType.UNKNOWN to the
     *  specified type.
     *  @param type A Type.
     */
    public void initialize(Type type) {
        throw new InternalErrorException("TopMatrixType.initialize: Cannot "
                + "initialize the element type to " + type + ".");
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
     *  @return False.
     */
    public boolean isInstantiable() {
        return false;
    }

    /** Test if the argument is a substitution instance of this type.
     *  @param type A Type.
     *  @return False.
     */
    public boolean isSubstitutionInstance(Type type) {
        return false;
    }

    /** Return the string representation of this type.
     *  @return A String.
     */
    public String toString() {
        return "[general]";
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
        throw new InternalErrorException("TopMatrixType.updateType: Cannot "
                + "updateType the element type to " + newType + ".");
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
    protected int _compare(StructuredType type) {
        if (equals(type)) {
            return CPO.SAME;
        } else {
            return CPO.HIGHER;
        }
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
        if (equals(type)) {
            return this;
        } else {
            return type;
        }
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
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private static TopMatrixType _instance = new TopMatrixType();
}
