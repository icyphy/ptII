/** A class representing the type of an ArrayToken.

 Copyright (c) 1997-2000 The Regents of the University of California.
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

@ProposedRating Red (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.data.type;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.graph.*;
import ptolemy.data.Token;
import ptolemy.data.MatrixToken;
import ptolemy.data.ArrayToken;
import ptolemy.data.expr.Variable;

//////////////////////////////////////////////////////////////////////////
//// ArrayType
/**
A class representing the type of an ArrayToken.

@author Steve Neuendorffer, Yuhong Xiong
$Id$
*/

public class ArrayType extends StructuredType {

    /** Construct a new ArrayType with the specified type for the array
     *  elements. To leave the element type undeclared, use BaseType.ANY.
     *  @exception IllegalArgumentException If the argument is null.
     */
    public ArrayType(Type elementType) {
        if (elementType == null) {
            throw new IllegalArgumentException("ArrayType: elementType is"
                    + " null");
        }

        try {
            _declaredElementType = (Type)elementType.clone();
        } catch (CloneNotSupportedException cnse) {
            throw new InternalErrorException("ArrayType: The specified type " +
                    "cannot be cloned.");
        }
        _elementType = _declaredElementType;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a deep copy of this ArrayType if it is a variable, or
     *  itself if it is a constant.
     *  @return An ArrayType.
     */
    public Object clone() {
        if (isConstant()) {
            return this;
        } else {
            ArrayType newObj = new ArrayType(_declaredElementType);
            try {
                newObj.updateType(this);
            } catch (IllegalActionException ex) {
                throw new InternalErrorException("ArrayType.clone: " +
                        "Cannot update new instance. " + ex.getMessage());
            }
            return newObj;
        }
    }

    /** Convert the argument token into an ArrayToken having this type,
     *  if losslessly conversion can be done.
     *  The argument can be an ArrayToken or a MatrixToken. If the argument
     *  is a MatrixToken, it will be converted to an ArrayToken containing
     *  a one dimensional token array if the MatrixToken has only one row,
     *  or it will be converted to an ArrayToken containing another ArrayToken
     *  (an array of arrays) if the MatrixToken has more than one row.
     *  @param t A token.
     *  @return An ArrayToken.
     *  @exception IllegalActionException If lossless conversion
     *   cannot be done.
     */
    public Token convert(Token t)
            throws IllegalActionException {
        if ( !isCompatible(t)) {
            throw new IllegalArgumentException("ArrayType.convert: " +
                    "Cannot convert the argument token to this type.");
        }

        ArrayToken argArrTok;
        if (t instanceof MatrixToken) {
            argArrTok = fromMatrixToken((MatrixToken)t);
        } else {
            argArrTok = (ArrayToken)t;
        }

        Token[] argArray = argArrTok.arrayValue();
        Token[] result = new Token[argArray.length];
        for (int i = 0; i < argArray.length; i++) {
            result[i] = _elementType.convert(argArray[i]);
        }
        return new ArrayToken(result);
    }

    /** Convert the argument MatrixToken to an ArrayToken. If the argument
     *  has more than one row, convert it to an ArrayToken containing an
     *  ArrayToken (an array of array).
     *  @param tok A MatrixToken.
     *  @return An ArrayToken.
     */
    public static ArrayToken fromMatrixToken(MatrixToken tok) {
        int rows = tok.getRowCount();
        if (rows == 1) {
            return _fromMatrixToken(tok, 0);
        } else {
            Token[] tokArray = new Token[rows];
            for (int i = 0; i < rows; i++) {
                tokArray[i] = _fromMatrixToken(tok, i);
            }
            return new ArrayToken(tokArray);
        }
    }

    /** Return the type of the array elements.
     *  @return a Type.
     */
    public Type getElementType() {
        return _elementType;
    }

    /** Return the InequalityTerm representing the element type.
     *  @return An InequalityTerm.
     *  @see ptolemy.graph.InequalityTerm
     */
    public InequalityTerm getElementTypeTerm() {
        if (_elemTypeTerm == null) {
            _elemTypeTerm = new ElementTypeTerm(this);
        }
        return _elemTypeTerm;
    }


    /** Set the elements that have declared type BaseType.ANY (the leaf
     *  type variable) to the specified type.
     *  This method is called at the beginning of type resolution.
     *  @param t the type to set the leaf type variable to.
     */
    public void initialize(Type t) {
        try {
            if (!isConstant()) {
                getElementTypeTerm().initialize(t);
            }
        } catch (IllegalActionException iae) {
            throw new InternalErrorException("ArrayType.initialize: Cannot " +
                    "initialize the element type to " + t + " " +
                    iae.getMessage());
        }
    }

    /** Test if the argument token is compatible with this type.
     *  If this type is a constant, the argument is compatible if it can be
     *  converted losslessly to a token of this type; If this type is a
     *  variable, the argument is compatible if its type is a substitution
     *  instance of this type, or if it can be converted losslessly to a
     *  substitution instance of this type. For ArrayTypes, in addition to
     *  the lossless conversion relation defined by the type lattice,
     *  MatrixTokens can also be losslessly converted to ArrayTokens of the
     *  same type.
     *  @param t A Token.
     *  @return True if the argument is compatible with this type.
     *  @see ptolemy.data.type.ArrayType#convert
     */
    public boolean isCompatible(Token t) {
        Type argType = t.getType();
        if (t instanceof MatrixToken) {
            MatrixToken argCast = (MatrixToken)t;
            if (argCast.getRowCount() == 1) {
                // argument is 1-D
                argType = new ArrayType(
                        argCast.getElementAsToken(0, 0).getType());
            } else {
                // argument is 2-D
                argType = new ArrayType(new ArrayType(
                        argCast.getElementAsToken(0, 0).getType()));
            }
        }

        // may not need to distinguish constant or non-constant, can
        // just check if the element type of argument token is compatible
        // with the _elementType of this ArrayType.
        if (isConstant()) {
            int typeInfo = TypeLattice.compare(this, argType);
            if (typeInfo == CPO.HIGHER || typeInfo == CPO.SAME) {
                return true;
            }
        } else {
            // This type is a variable.
            if (isSubstitutionInstance(argType)) {
                return true;
            }
        }
        return false;
    }

    /** Test if this ArrayType is a constant. An ArrayType is a constant if
     *  it does not contain BaseType.NAT in any level.
     *  @return True if this type is a constant.
     */
    public boolean isConstant() {
        return _declaredElementType.isConstant();
    }

    /** Determine if the argument represents the same ArrayType as this
     *  object.
     *  @param t A Type.
     *  @return True if the argument represents the same ArrayType as
     *   this object; false otherwise.
     */
    public boolean isEqualTo(Type t) {
        if ( !(t instanceof ArrayType)) {
            return false;
        }
        return _elementType.isEqualTo(((ArrayType)t).getElementType());
    }

    /** Determine if this type corresponds to an instantiable token
     *  class. An ArrayType is instantiable if its element type is
     *  instantiable.
     *  @return True if this type is instantiable.
     */
    public boolean isInstantiable() {
        return _elementType.isInstantiable();
    }

    /** Return true if the specified type is a substitution instance of this
     *  type. For the argument to be a substitution instance, this type must
     *  be a variable, and the argument must be a type that can be obtained
     *  by replacing the BaseType.NAT component of the declared type by
     *  another type.
     *  @parameter type A Type.
     *  @return True if the argument is a substitution instance of this type.
     */
    public boolean isSubstitutionInstance(Type type) {
        if (isConstant() || ( !(type instanceof ArrayType))) {
            return false;
        }

        Type argElemType = ((ArrayType)type).getElementType();
        return _declaredElementType.isSubstitutionInstance(argElemType);
    }

    /** Return the string representation of this type. The format is
     *  (<type>) array, where <type> is is the elemenet type.
     *  @return A String.
     */
    public String toString() {
        return "(" + _elementType.toString() + ")array";
    }

    /** Update this Type to the specified ArrayType.
     *  The specified type must be a substitution instance of this type.
     *  This method will only update the component whose declared type is
     *  BaseType.NAT, and leave the constant part of this type intact.
     *  This method does not check for circular usage, the caller should
     *  perform this check.
     *  @param st A StructuredType.
     *  @exception IllegalActionException If the specified type is not a
     *   substitution instance of this type.
     */
    public void updateType(StructuredType newType)
            throws IllegalActionException {
        if ( !this.isSubstitutionInstance(newType)) {
            throw new IllegalActionException("ArrayType.updateType: " +
                    "The argument is not a substitution instance of this type.");
        }

        Type newElemType = ((ArrayType)newType).getElementType();
        if (_declaredElementType == BaseType.NAT) {
            try {
                _elementType = (Type)newElemType.clone();
            } catch (CloneNotSupportedException cnse) {
                throw new InternalErrorException("RecordType.updateType: " +
                        "The specified type cannot be cloned.");
            }
        } else {
            // _declaredElementType is a StructuredType. _elementType
            // must also be.
            ((StructuredType)_elementType).updateType(
                    (StructuredType)newElemType);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected methods                   ////

    /** Compare this type with the specified type. The specified type
     *  must be an ArrayType, otherwise an exception will be thrown.
     *  This method returns one of ptolemy.graph.CPO.LOWER,
     *  ptolemy.graph.CPO.SAME, ptolemy.graph.CPO.HIGHER,
     *  ptolemy.graph.CPO.INCOMPARABLE, indicating this type is lower
     *  than, equal to, higher than, or incomparable with the
     *  specified type in the type hierarchy, respectively.
     *  @param t an ArrayType.
     *  @return An integer.
     *  @exception IllegalArgumentException If the specified type is
     *   not an ArrayType.
     */
    protected int _compare(StructuredType t) {
        if ( !(t instanceof ArrayType)) {
            throw new IllegalArgumentException("ArrayType.compare: " +
                    "The argument is not an ArrayType.");
        }

        return TypeLattice.compare(_elementType,
                ((ArrayType)t).getElementType());
    }

    /** Return a static instance of ArrayType.
     *  @return an ArrayType.
     */
    protected StructuredType _getRepresentative() {
        return _representative;
    }

    /** Return the greatest lower bound of this type with the specified
     *  type. The specified type must be an ArrayType, otherwise an
     *  exception will be thrown.
     *  @param t an ArrayType.
     *  @return an ArrayType.
     *  @exception IllegalArgumentException If the specified type is
     *   not an ArrayType.
     */
    protected StructuredType _greatestLowerBound(StructuredType t) {
        if ( !(t instanceof ArrayType)) {
            throw new IllegalArgumentException("ArrayType.greatestLowerBound: "
                    + "The argument is not an ArrayType.");
        }

        Type elementGLB = (Type)TypeLattice.lattice().greatestLowerBound(
                _elementType, ((ArrayType)t).getElementType());
        return new ArrayType(elementGLB);
    }

    /** Return the least Upper bound of this type with the specified
     *  type. The specified type must be an ArrayType, otherwise an
     *  exception will be thrown.
     *  @param t an ArrayType.
     *  @return an ArrayType.
     *  @exception IllegalArgumentException If the specified type is
     *   not an ArrayType.
     */
    protected StructuredType _leastUpperBound(StructuredType t) {
        if ( !(t instanceof ArrayType)) {
            throw new IllegalArgumentException("ArrayType.leastUpperBound: "
                    + "The argument is not an ArrayType.");
        }

        Type elementLUB = (Type)TypeLattice.lattice().leastUpperBound(
                _elementType, ((ArrayType)t).getElementType());
        return new ArrayType(elementLUB);
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private methods                    ////

    // convert a row of a MatrixToken into an ArrayToken.
    private static ArrayToken _fromMatrixToken(MatrixToken tok, int row) {
        int cols = tok.getColumnCount();
        Token[] tokArray = new Token[cols];
        for (int i = 0; i < cols; i++) {
            tokArray[i] = tok.getElementAsToken(row, i);
        }
        return new ArrayToken(tokArray);
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ////

    // the type of array elements.
    private Type _declaredElementType;
    private Type _elementType;

    private ElementTypeTerm _elemTypeTerm = null;

    private static ArrayType _representative = new ArrayType(BaseType.NAT);

    ///////////////////////////////////////////////////////////////////
    ////                           inner class                     ////

    private class ElementTypeTerm implements InequalityTerm {

        // Pass the ArrayType reference in the constructor so it can be
        // returned by getAssociatedObject().
        private ElementTypeTerm(ArrayType t) {
            _arrayType = t;
        }

        ///////////////////////////////////////////////////////////////
        ////                   public inner methods                ////

        /** Return this ArrayType.
         *  @return an ArrayType.
         */
        public Object getAssociatedObject() {
            return _arrayType;
        }

        /** Return the element type.
         *  @return a Type.
         */
        public Object getValue() {
            return _elementType;
        }

        /** Return this ElementTypeTerm in an array if this term
         *  represents a type variable. Otherwise, return an array of
         *  size zero.
         *  @return An array of InequalityTerm.
         */
        public InequalityTerm[] getVariables() {
            if (isSettable()) {
                InequalityTerm[] variable = new InequalityTerm[1];
                variable[0] = this;
                return variable;
            }
            return (new InequalityTerm[0]);
        }

        /** Reset the variable part of the element type to the specified
         *  type.
         *  @parameter e A Type.
         *  @exception IllegalActionException If this type is a constant,
         *   or the argument is not a Type.
         */
        public void initialize(Object e)
                throws IllegalActionException {
            if (isConstant()) {
                throw new IllegalActionException("ArrayType$ElementTypeTerm." +
                        "initialize: The type is not settable.");
            }

            if ( !(e instanceof Type)) {
                throw new IllegalActionException("ElementTypeTerm.initialize: "
                        + "The argument is not a Type.");
            }

            if (_declaredElementType == BaseType.NAT) {
                _elementType = (Type)e;
            } else {
                // element type is a structured type.
                ((StructuredType)_elementType).initialize((Type)e);
            }
        }

        /** Test if the element type is a type variable.
         *  @return True if the element type is a type variable.
         */
        public boolean isSettable() {
            return !isConstant();
        }

        /** Check whether the current element type is acceptable.
         *  The element type is acceptable if it represents an
         *  instantiable object.
         *  @return True if the element type is acceptable.
         */
        public boolean isValueAcceptable() {
            return isInstantiable();
        }

        /** Set the element type to the specified type.
         *  @param e a Type.
         *  @exception IllegalActionException If setting the element type to
         *   to the specified one would result in circular type structure;
         *   or the specified type is not a substitution instance of the
         *   element type.
         */
        public void setValue(Object e)
                throws IllegalActionException {
            if ( !isSettable()) {
                throw new IllegalActionException(
                        "ArrayType$ElementTypeTerm.setValue: The type is not " +
                        "settable.");
            }

            if ( !_declaredElementType.isSubstitutionInstance((Type)e)) {
                // The LUB of the _elementType and another type is General,
                // this is a type conflict.

                throw new IllegalActionException("ElementTypeTerm.setValue:" +
                        " The new type is not a substitution instance of the " +
                        "element type. element type: " +
                        _declaredElementType.toString() + "new type: " +
                        e.toString());
            }

            if (_declaredElementType == BaseType.NAT) {
                try {
                    _elementType = (Type)((Type)e).clone();
                } catch (CloneNotSupportedException cnse) {
                    throw new InternalErrorException(
                            "ArrayType$ElementTypeTerm.setValue: " +
                            "The specified type cannot be cloned.");
                }
            } else {
                ((StructuredType)_elementType).updateType((StructuredType)e);
            }
        }

        /** Return a string representation of this term.
         *  @return A String.
         */
        public String toString() {
            return "(ArrayElementType, " + getValue() + ")";
        }

        ///////////////////////////////////////////////////////////////
        ////                  private inner variables              ////

        private ArrayType _arrayType = null;
    }
}

