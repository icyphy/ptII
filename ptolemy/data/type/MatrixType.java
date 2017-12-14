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

import ptolemy.data.BooleanMatrixToken;
import ptolemy.data.ComplexMatrixToken;
import ptolemy.data.ComplexToken;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.FixMatrixToken;
import ptolemy.data.FixToken;
import ptolemy.data.IntMatrixToken;
import ptolemy.data.IntToken;
import ptolemy.data.LongMatrixToken;
import ptolemy.data.LongToken;
import ptolemy.data.MatrixToken;
import ptolemy.data.Token;
import ptolemy.graph.CPO;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.math.Complex;
import ptolemy.math.FixPoint;

///////////////////////////////////////////////////////////////////
//// MatrixType

/**
 The base type of matrix token classes. This type functions as a union
 of the various matrix types. It allows for the creation of arrays
 that consist of diverse matrix types, because the array type will
 be {matrix}.

 @author Steve Neuendorffer and Edward A. Lee
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Red (yuhong)
 @Pt.AcceptedRating Red
 */
public class MatrixType extends StructuredType implements Cloneable {

    /** Construct a new matrix type that represents matrix tokens of the
     *  given class with the given number of rows and columns.
     *  @param c The token class.
     *  @param type The element type.
     *  @param name The name.
     */
    public MatrixType(Class c, Type type, String name) {
        _tokenClass = c;
        _elementType = type;
        _name = name;
        BaseType._addType(this, name, c);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return this, that is, return the reference to this object.
     *  @return A MatrixType.
     */
    @Override
    public Object clone() {
        return this;
    }

    /** If the argument is an instance of this class or a subclass,
     *  then return the token. Otherwise, throw an exception.
     *  @param token A token.
     *  @return An instance of this class.
     *  @exception IllegalActionException If the argument is not
     *   an instance of this class.
     */
    @Override
    public Token convert(Token token) throws IllegalActionException {
        if (token instanceof MatrixToken) {
            return token;
        }
        throw new IllegalActionException(Token
                .notSupportedIncomparableConversionMessage(token, "matrix"));
    }

    /** Determine if the argument represents the same MatrixType as this
     *  object.
     *  @param object A Type.
     *  @return True if the argument type is a matrix type representing the
     *  same class, with the same number of rows and columns.
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof MatrixType)) {
            return false;
        }

        MatrixType matrixType = (MatrixType) object;

        if (matrixType.getTokenClass() != _tokenClass) {
            return false;
        }

        return true;
    }

    /** Return the type of the elements contained in an instance of
     *  this matrix type.  Tokens of the type returned by this method
     *  will be returned by the getElementAsToken of matrices that
     *  have this type. If this is an instance of this base class
     *  rather than the specific matrix types, then return null.
     *  @return The type of the elements.
     */
    public Type getElementType() {
        return _elementType;
    }

    /** Return a matrix type whose element type is the given element
     *  type.
     *  @param elementType The type of the element.
     *  @return The matrix type.
     *  @exception IllegalActionException If the elementType is not a
     *  type that has a corresponding matrix type.
     */
    public static MatrixType getMatrixTypeForElementType(Type elementType)
            throws IllegalActionException {
        MatrixType matrixType;

        if (elementType.equals(BaseType.UNKNOWN)) {
            throw new IllegalActionException(
                    "Cannot resolve type for " + "matrix construction.");
        } else if (elementType.equals(BaseType.BOOLEAN)) {
            matrixType = BaseType.BOOLEAN_MATRIX;
        } else if (elementType.equals(BaseType.INT)) {
            matrixType = BaseType.INT_MATRIX;
        } else if (elementType.equals(BaseType.LONG)) {
            matrixType = BaseType.LONG_MATRIX;
        } else if (elementType.equals(BaseType.DOUBLE)) {
            matrixType = BaseType.DOUBLE_MATRIX;
        } else if (elementType.equals(BaseType.COMPLEX)) {
            matrixType = BaseType.COMPLEX_MATRIX;
        } else if (elementType instanceof FixType) {
            matrixType = BaseType.FIX_MATRIX;
        } else if (elementType.equals(BaseType.UNSIZED_FIX)) {
            matrixType = BaseType.FIX_MATRIX;
        } else {
            throw new IllegalActionException("Type " + elementType
                    + " does have a corresponding matrix type.");
        }

        return matrixType;
    }

    /** Return the class for tokens that this type represents.
     *  @return A class that represents a matrix token.
     */
    @Override
    public Class getTokenClass() {
        return _tokenClass;
    }

    /** Return a hash code value for this object.
     *  @return The hash code for the token class of this type.
     */
    @Override
    public int hashCode() {
        return getTokenClass().hashCode();
    }

    /** Set the elements that have declared type BaseType.UNKNOWN to the
     *  specified type.
     *  @param type A Type.
     */
    @Override
    public void initialize(Type type) {
        // Ignore... This type has no components that are unknown.
    }

    /** Return true if the element type is abstract.
     *  @return True.
     */
    @Override
    public boolean isAbstract() {
        return _elementType.isAbstract();
    }

    /** Test if the argument type is compatible with this type. The method
     *  returns true if this type is UNKNOWN, since any type is a substitution
     *  instance of it. If this type is not UNKNOWN, this method returns true
     *  if the argument type is less than or equal to this type in the type
     *  lattice, and false otherwise.
     *  @param type An instance of Type.
     *  @return True if the argument is compatible with this type.
     */
    @Override
    public boolean isCompatible(Type type) {
        int typeInfo = TypeLattice.compare(this, type);
        return typeInfo == CPO.SAME || typeInfo == CPO.HIGHER;
    }

    /** Test if this Type is a constant. A Type is a constant if it
     *  does not contain BaseType.UNKNOWN in any level within it.
     *  @return False.
     */
    @Override
    public boolean isConstant() {
        return true;
    }

    /** Return true, indicating that instances of this type can
     *  exist.
     *  @return True.
     */
    @Override
    public boolean isInstantiable() {
        return _elementType.isInstantiable();
    }

    /** Test if the argument is a substitution instance of this type.
     *  @param type A Type.
     *  @return False.
     */
    @Override
    public boolean isSubstitutionInstance(Type type) {
        if (type instanceof StructuredType) {
            return ((StructuredType) type)
                    ._getRepresentative() == _getRepresentative();
        } else {
            return false;
        }
    }

    /** Return the string representation of this type.
     *  @return A String.
     */
    @Override
    public String toString() {
        return _name;
    }

    /** Update this StructuredType to the specified Structured Type.
     ** The specified type must have the same structure as this type.
     *  This method will only update the component type that is
     *  BaseType.UNKNOWN, and leave the constant part of this type intact.
     *  @param newType A StructuredType.
     *  @exception IllegalActionException If the specified type has a
     *   different structure.
     */
    @Override
    public void updateType(StructuredType newType)
            throws IllegalActionException {
        if (newType._getRepresentative() != _getRepresentative()) {
            throw new InternalErrorException("MatrixType.updateType: Cannot "
                    + "updateType the element type to " + newType + ".");
        }
    }

    /** The boolean matrix data type. */
    public static class BooleanMatrixType extends MatrixType {
        public BooleanMatrixType() {
            super(BooleanMatrixToken.class, BaseType.BOOLEAN, "[boolean]");
        }

        @Override
        public Token convert(Token token) throws IllegalActionException {
            if (token instanceof MatrixToken) {
                return BooleanMatrixToken.convert(token);
            } else {
                // NOTE: No point in converting to a boolean matrix,
                // since you can't do anything with it.
                throw new IllegalActionException(
                        Token.notSupportedConversionMessage(token, toString()));
            }
        }
    }

    /** The complex matrix data type. */
    public static class ComplexMatrixType extends MatrixType {
        public ComplexMatrixType() {
            super(ComplexMatrixToken.class, BaseType.COMPLEX, "[complex]");
        }

        @Override
        public Token convert(Token token) throws IllegalActionException {
            if (token instanceof MatrixToken) {
                return ComplexMatrixToken.convert(token);
            } else {
                if (token.isNil()) {
                    throw new IllegalActionException(Token
                            .notSupportedConversionMessage(token, toString()));
                }
                // Try to create a new [complex] type with just one member.
                // The following conversion will fail if the member cannot
                // be converted to an int.
                ComplexToken singleMember = ComplexToken.convert(token);
                Complex[][] matrix = new Complex[1][1];
                matrix[0][0] = singleMember.complexValue();
                return new ComplexMatrixToken(matrix);
            }

        }
    }

    /** The double matrix data type. */
    public static class DoubleMatrixType extends MatrixType {
        public DoubleMatrixType() {
            super(DoubleMatrixToken.class, BaseType.DOUBLE, "[double]");
        }

        @Override
        public Token convert(Token token) throws IllegalActionException {
            if (token instanceof MatrixToken) {
                return DoubleMatrixToken.convert(token);
            } else {
                if (token.isNil()) {
                    // MatrixTokens do not support nil because there is no
                    // way to represent a nil double as a Java.lang.Double.
                    // Double.NaN is a possibility, but NaN is a value, whereas
                    // nil is the absent value.  Other types, like int do
                    // not have NaN, so we catch this problem here.
                    // Note that this code is called by the "cast" expression
                    // language method from data.expr.UtilityFunctions
                    throw new IllegalActionException(Token
                            .notSupportedConversionMessage(token, toString()));
                }
                // Try to create a new [double] type with just one member.
                // The following conversion will fail if the member cannot
                // be converted to an int.
                DoubleToken singleMember = DoubleToken.convert(token);
                double[] matrix = new double[1];
                matrix[0] = singleMember.doubleValue();
                return new DoubleMatrixToken(matrix, 1, 1);
            }
        }
    }

    /** The integer matrix data type. */
    public static class IntMatrixType extends MatrixType {
        public IntMatrixType() {
            super(IntMatrixToken.class, BaseType.INT, "[int]");
        }

        @Override
        public Token convert(Token token) throws IllegalActionException {
            if (token instanceof MatrixToken) {
                return IntMatrixToken.convert(token);
            } else {
                if (token.isNil()) {
                    throw new IllegalActionException(Token
                            .notSupportedConversionMessage(token, toString()));
                }

                // Try to create a new [int] type with just one member.
                // The following conversion will fail if the member cannot
                // be converted to an int.
                IntToken singleMember = IntToken.convert(token);
                int[] matrix = new int[1];
                matrix[0] = singleMember.intValue();
                return new IntMatrixToken(matrix, 1, 1);
            }
        }
    }

    /** The fix matrix data type. */
    public static class FixMatrixType extends MatrixType {
        public FixMatrixType() {
            super(FixMatrixToken.class, BaseType.UNSIZED_FIX, "[fixedpoint]");
        }

        @Override
        public Token convert(Token token) throws IllegalActionException {
            if (token instanceof MatrixToken) {
                return FixMatrixToken.convert(token);
            } else {
                if (token.isNil()) {
                    throw new IllegalActionException(Token
                            .notSupportedConversionMessage(token, toString()));
                }
                // Try to create a new [fix] type with just one member.
                // The following conversion will fail if the member cannot
                // be converted to an int.
                FixToken singleMember = FixToken.convert(token);
                FixPoint[][] matrix = new FixPoint[1][1];
                matrix[0][0] = singleMember.fixValue();
                return new FixMatrixToken(matrix);
            }
        }
    }

    /** The long matrix data type. */
    public static class LongMatrixType extends MatrixType {
        public LongMatrixType() {
            super(LongMatrixToken.class, BaseType.LONG, "[long]");
        }

        @Override
        public Token convert(Token token) throws IllegalActionException {
            if (token instanceof MatrixToken) {
                return LongMatrixToken.convert(token);
            } else {
                if (token.isNil()) {
                    throw new IllegalActionException(Token
                            .notSupportedConversionMessage(token, toString()));
                }
                // Try to create a new [long] type with just one member.
                // The following conversion will fail if the member cannot
                // be converted to an int.
                LongToken singleMember = LongToken.convert(token);
                long[] matrix = new long[1];
                matrix[0] = singleMember.longValue();
                return new LongMatrixToken(matrix, 1, 1);
            }
        }
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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

    /** The type of MatrixToken that this class represents. */
    private Class _tokenClass;

    /** The type of the elements of the matrix, or ScalarType for this base class. */
    private Type _elementType;

    /** The name of this instance. */
    private String _name;
}
