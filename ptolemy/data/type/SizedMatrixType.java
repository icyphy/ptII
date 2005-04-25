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

import ptolemy.data.BooleanMatrixToken;
import ptolemy.data.ComplexMatrixToken;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.FixMatrixToken;
import ptolemy.data.IntMatrixToken;
import ptolemy.data.LongMatrixToken;
import ptolemy.data.MatrixToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.Token;
import ptolemy.graph.CPO;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;

import java.io.Serializable;


//////////////////////////////////////////////////////////////////////////
//// MatrixType

/**
   The base type of matrix token classes.

   @author Steve Neuendorffer
   @version $Id$
   @since Ptolemy II 2.0
   @Pt.ProposedRating Red (yuhong)
   @Pt.AcceptedRating Red
*/
public abstract class SizedMatrixType extends StructuredType
    implements Serializable {
    /** Construct a new matrix type that represents matrix tokens of the
     *  given class with the given number of rows and columns.
     */
    public SizedMatrixType(Class c, String name, int rows, int columns) {
        _tokenClass = c;
        _name = name;
        _rows = rows;
        _columns = columns;
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
    public abstract Token convert(Token token) throws IllegalActionException;

    /** Determine if the argument represents the same MatrixType as this
     *  object.
     *  @param object A Type.
     *  @return True if the argument type is a matrix type representing the
     *  same class, with the same number of rows and columns.
     */
    public boolean equals(Object object) {
        if (!(object instanceof SizedMatrixType)) {
            return false;
        }

        SizedMatrixType matrixType = (SizedMatrixType) object;

        if (matrixType.getTokenClass() != _tokenClass) {
            return false;
        }

        if (matrixType.getRowCount() != _rows) {
            return false;
        }

        if (matrixType.getColumnCount() != _columns) {
            return false;
        }

        return true;
    }

    /** Return the number of columns in matrix tokens represented by this
     *  type.
     */
    public int getColumnCount() {
        return _columns;
    }

    /** Return the number of rows in matrix tokens represented by this
     *  type.
     */
    public int getRowCount() {
        return _rows;
    }

    /** Return the class for tokens that this basetype represents.
     *  @return A class that represents a matrix token.
     */
    public Class getTokenClass() {
        return _tokenClass;
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
        // ignore.. this type has no components that are unknown.
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
        return _name + "(" + _rows + "," + _columns + ")";
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
        if (newType._getRepresentative() != _getRepresentative()) {
            throw new InternalErrorException(
                "SizedMatrixType.updateType: Cannot "
                + "updateType the element type to " + newType + ".");
        }
    }

    /** The boolean matrix data type */
    public static class BooleanMatrixType extends SizedMatrixType {
        public BooleanMatrixType(int rows, int columns) {
            super(BooleanMatrixToken.class, "[boolean]", rows, columns);
        }

        public Token convert(Token token) throws IllegalActionException {
            if (token instanceof MatrixToken) {
                return BooleanMatrixToken.convert((MatrixToken) token);
            } else {
                throw new IllegalActionException(Token
                                .notSupportedConversionMessage(token, toString()));
            }
        }

        public StructuredType _getRepresentative() {
            return BaseType.BOOLEAN_MATRIX;
        }
    }

    /** The complex matrix data type */
    public static class ComplexMatrixType extends SizedMatrixType {
        public ComplexMatrixType(int rows, int columns) {
            super(ComplexMatrixToken.class, "[complex]", rows, columns);
        }

        public Token convert(Token token) throws IllegalActionException {
            if (token instanceof MatrixToken) {
                return ComplexMatrixToken.convert((MatrixToken) token);
            } else {
                throw new IllegalActionException(Token
                                .notSupportedConversionMessage(token, toString()));
            }
        }

        protected StructuredType _getRepresentative() {
            return BaseType.COMPLEX_MATRIX;
        }
    }

    /** The double matrix data type */
    public static class DoubleMatrixType extends SizedMatrixType {
        public DoubleMatrixType(int rows, int columns) {
            super(DoubleMatrixToken.class, "[double]", rows, columns);
        }

        public Token convert(Token token) throws IllegalActionException {
            if (token instanceof MatrixToken) {
                return DoubleMatrixToken.convert((MatrixToken) token);
            } else {
                throw new IllegalActionException(Token
                                .notSupportedConversionMessage(token, toString()));
            }
        }

        protected StructuredType _getRepresentative() {
            return BaseType.DOUBLE_MATRIX;
        }
    }

    /** The int matrix data type */
    public static class IntMatrixType extends SizedMatrixType {
        public IntMatrixType(int rows, int columns) {
            super(IntMatrixToken.class, "[int]", rows, columns);
        }

        public Token convert(Token token) throws IllegalActionException {
            if (token instanceof MatrixToken) {
                return IntMatrixToken.convert((MatrixToken) token);
            } else if ((getRowCount() == getColumnCount())
                            && token instanceof ScalarToken) {
                return IntMatrixToken.convert((ScalarToken) token, getRowCount());
            } else {
                throw new IllegalActionException(Token
                                .notSupportedConversionMessage(token, toString()));
            }
        }

        protected StructuredType _getRepresentative() {
            return BaseType.DOUBLE_MATRIX;
        }
    }

    /** The fix matrix data type */
    public static class FixMatrixType extends SizedMatrixType {
        public FixMatrixType(int rows, int columns) {
            super(FixMatrixToken.class, "[fixedpoint]", rows, columns);
        }

        public Token convert(Token token) throws IllegalActionException {
            if (token instanceof MatrixToken) {
                return FixMatrixToken.convert((MatrixToken) token);
            } else {
                throw new IllegalActionException(Token
                                .notSupportedConversionMessage(token, toString()));
            }
        }

        protected StructuredType _getRepresentative() {
            return BaseType.FIX_MATRIX;
        }
    }

    /** The long matrix data type */
    public static class LongMatrixType extends SizedMatrixType {
        public LongMatrixType(int rows, int columns) {
            super(LongMatrixToken.class, "[long]", rows, columns);
        }

        public Token convert(Token token) throws IllegalActionException {
            if (token instanceof MatrixToken) {
                return LongMatrixToken.convert((MatrixToken) token);
            } else {
                throw new IllegalActionException(Token
                                .notSupportedConversionMessage(token, toString()));
            }
        }

        protected StructuredType _getRepresentative() {
            return BaseType.LONG_MATRIX;
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
    protected int _compare(StructuredType type) {
        return CPO.SAME;

        //         if (equals(type)) {
        //             return CPO.SAME;
        //         } else if (equals(_getRepresentative()) ||
        //                 type.equals(BaseType.MATRIX)) {
        //             return CPO.LOWER;
        //         } else if (equals(BaseType.MATRIX) ||
        //                 type.equals(_getRepresentative())) {
        //             return CPO.HIGHER;
        //         } else {
        //             return CPO.INCOMPARABLE;
        //         }
    }

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
    protected StructuredType _greatestLowerBound(StructuredType type) {
        return this;

        //         if (equals(type)) {
        //              return this;
        //         } else if (equals(_getRepresentative()) ||
        //                 type.equals(BaseType.MATRIX)) {
        //             return this;
        //         } else if (equals(BaseType.MATRIX) ||
        //                 type.equals(_getRepresentative())) {
        //             return type;
        //         } else {
        //             return _getRepresentative();
        //         }
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
        //         } else if (equals(_getRepresentative()) ||
        //                 type.equals(BaseType.MATRIX)) {
        //             return type;
        //         } else if (equals(BaseType.MATRIX) ||
        //                 type.equals(_getRepresentative())) {
        //             return this;
        //         } else {
        //             throw new IllegalArgumentException("fubar");
        //             //     return BaseType.MATRIX;
        //         }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private Class _tokenClass;
    private String _name;
    private int _rows;
    private int _columns;
}
