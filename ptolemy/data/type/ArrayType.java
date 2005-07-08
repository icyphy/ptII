/** A class representing the type of an ArrayToken.

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

import ptolemy.data.ArrayToken;
import ptolemy.data.Token;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;

//////////////////////////////////////////////////////////////////////////
//// ArrayType

/**

 A class representing the type of an ArrayToken.

 @author Steve Neuendorffer, Yuhong Xiong
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class ArrayType extends StructuredType {
    /** Construct a new ArrayType with the specified type for the array
     *  elements. To leave the element type undeclared, use BaseType.UNKNOWN.
     *  @param elementType The type of the array elements.
     *  @exception IllegalArgumentException If the argument is null.
     */
    public ArrayType(Type elementType) {
        if (elementType == null) {
            throw new IllegalArgumentException("Cannot create ArrayType "
                    + " with null elementType");
        }

        try {
            _declaredElementType = (Type) elementType.clone();
        } catch (CloneNotSupportedException cnse) {
            throw new InternalErrorException("The specified type "
                    + elementType + " cannot be cloned.");
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
                throw new InternalErrorException("ArrayType.clone: "
                        + "Cannot update new instance. " + ex.getMessage());
            }

            return newObj;
        }
    }

    /** Convert the argument token into an ArrayToken having this
     *  type, if losslessly conversion can be done.  The argument must
     *  be an ArrayToken.
     *  @param token A token.
     *  @return An ArrayToken.
     *  @exception IllegalActionException If lossless conversion
     *   cannot be done.
     */
    public Token convert(Token token) throws IllegalActionException {
        if (!(token instanceof ArrayToken)) {
            throw new IllegalArgumentException(Token
                    .notSupportedIncomparableConversionMessage(token,
                            toString()));
        }

        ArrayToken argumentArrayToken = (ArrayToken) token;

        if (getElementType().equals(argumentArrayToken.getElementType())) {
            return token;
        }

        Token[] argumentArray = argumentArrayToken.arrayValue();
        Token[] resultArray = new Token[argumentArray.length];

        try {
            for (int i = 0; i < argumentArray.length; i++) {
                resultArray[i] = getElementType().convert(argumentArray[i]);
            }
        } catch (IllegalActionException ex) {
            throw new IllegalActionException(null, ex, Token
                    .notSupportedConversionMessage(token, "int"));
        }

        return new ArrayToken(resultArray);
    }

    /** Determine if the argument represents the same ArrayType as this
     *  object.
     *  @param object Another object.
     *  @return True if the argument represents the same ArrayType as
     *   this object; false otherwise.
     */
    public boolean equals(Object object) {
        if (!(object instanceof ArrayType)) {
            return false;
        }

        return _elementType.equals(((ArrayType) object).getElementType());
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
        return _elemTypeTerm;
    }

    /** Return the class for tokens that this type represents.
     */
    public Class getTokenClass() {
        return ArrayToken.class;
    }

    /** Return a hash code value for this object.
     */
    public int hashCode() {
        return _elementType.hashCode() + 2917;
    }

    /** Set the elements that have declared type BaseType.UNKNOWN (the leaf
     *  type variable) to the specified type.
     *  @param t the type to set the leaf type variable to.
     */
    public void initialize(Type t) {
        try {
            if (!isConstant()) {
                getElementTypeTerm().initialize(t);
            }
        } catch (IllegalActionException iae) {
            throw new InternalErrorException("ArrayType.initialize: Cannot "
                    + "initialize the element type to " + t + ". "
                    + iae.getMessage());
        }
    }

    /** Test if the argument type is compatible with this type.
     *  If this type is a constant, the argument is compatible if it is less
     *  than or equal to this type in the type lattice; If this type is a
     *  variable, the argument is compatible if it is a substitution
     *  instance of this type.
     *  @param type A Type.
     *  @return True if the argument is compatible with this type.
     *  @see ptolemy.data.type.ArrayType#convert
     */
    public boolean isCompatible(Type type) {
        ArrayType arrayType;

        if (type instanceof ArrayType) {
            arrayType = (ArrayType) type;
        } else {
            return false;
        }

        Type elementType = arrayType.getElementType();
        return _elementType.isCompatible(elementType);
    }

    /** Test if this ArrayType is a constant. An ArrayType is a constant if
     *  it does not contain BaseType.UNKNOWN in any level.
     *  @return True if this type is a constant.
     */
    public boolean isConstant() {
        return _declaredElementType.isConstant();
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
     *  type.
     *  @param type A Type.
     *  @return True if the argument is a substitution instance of this type.
     *  @see Type#isSubstitutionInstance
     */
    public boolean isSubstitutionInstance(Type type) {
        if (!(type instanceof ArrayType)) {
            return false;
        }

        Type argElemType = ((ArrayType) type).getElementType();
        return _declaredElementType.isSubstitutionInstance(argElemType);
    }

    /** Return the string representation of this type. The format is
     *  {<i>type</i>}, where <i>type</i> is the element type.
     *  @return A String.
     */
    public String toString() {
        return "{" + _elementType.toString() + "}";
    }

    /** Update this Type to the specified ArrayType.
     *  The specified type must be an ArrayType with the same structure as
     *  this type.
     *  This method will only update the component whose declared type is
     *  BaseType.UNKNOWN, and leave the constant part of this type intact.
     *  @param newType A StructuredType.
     *  @exception IllegalActionException If the specified type is not an
     *   ArrayType or it does not have the same structure as this one.
     */
    public void updateType(StructuredType newType)
            throws IllegalActionException {
        if (this.isConstant()) {
            if (this.equals(newType)) {
                return;
            } else {
                throw new IllegalActionException("ArrayType.updateType: "
                        + "This type is a constant and the argument is not "
                        + "the same as this type. " + "This type: "
                        + this.toString() + " argument: " + newType.toString());
            }
        }

        // This type is a variable.
        if (!this.isSubstitutionInstance(newType)) {
            throw new IllegalActionException("ArrayType.updateType: "
                    + "The type " + this + " cannot be updated to " + newType
                    + ".");
        }

        Type newElemType = ((ArrayType) newType).getElementType();

        if (_declaredElementType == BaseType.UNKNOWN) {
            try {
                _elementType = (Type) newElemType.clone();
            } catch (CloneNotSupportedException cnse) {
                throw new InternalErrorException("ArrayType.updateType: "
                        + "The specified element type cannot be cloned: "
                        + _elementType);
            }
        } else {
            // _declaredElementType is a StructuredType. _elementType
            // must also be.
            ((StructuredType) _elementType)
                    .updateType((StructuredType) newElemType);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Compare this type with the specified type. The specified type
     *  must be an ArrayType, otherwise an exception will be thrown.
     *  This method returns one of ptolemy.graph.CPO.LOWER,
     *  ptolemy.graph.CPO.SAME, ptolemy.graph.CPO.HIGHER,
     *  ptolemy.graph.CPO.INCOMPARABLE, indicating this type is lower
     *  than, equal to, higher than, or incomparable with the
     *  specified type in the type hierarchy, respectively.
     *  @param type an ArrayType.
     *  @return An integer.
     *  @exception IllegalArgumentException If the specified type is
     *   not an ArrayType.
     */
    protected int _compare(StructuredType type) {
        if (!(type instanceof ArrayType)) {
            throw new IllegalArgumentException("ArrayType.compare: "
                    + "The argument " + type + " is not an ArrayType.");
        }

        return TypeLattice.compare(_elementType, ((ArrayType) type)
                .getElementType());
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
     *  @param type an ArrayType.
     *  @return an ArrayType.
     *  @exception IllegalArgumentException If the specified type is
     *   not an ArrayType.
     */
    protected StructuredType _greatestLowerBound(StructuredType type) {
        if (!(type instanceof ArrayType)) {
            throw new IllegalArgumentException("ArrayType.greatestLowerBound: "
                    + "The argument " + type + " is not an ArrayType.");
        }

        Type elementGLB = (Type) TypeLattice.lattice().greatestLowerBound(
                _elementType, ((ArrayType) type).getElementType());
        return new ArrayType(elementGLB);
    }

    /** Return the least Upper bound of this type with the specified
     *  type. The specified type must be an ArrayType, otherwise an
     *  exception will be thrown.
     *  @param type an ArrayType.
     *  @return an ArrayType.
     *  @exception IllegalArgumentException If the specified type is
     *   not an ArrayType.
     */
    protected StructuredType _leastUpperBound(StructuredType type) {
        if (!(type instanceof ArrayType)) {
            throw new IllegalArgumentException("ArrayType.leastUpperBound: "
                    + "The argument " + type + " is not an ArrayType.");
        }

        Type elementLUB = (Type) TypeLattice.lattice().leastUpperBound(
                _elementType, ((ArrayType) type).getElementType());
        return new ArrayType(elementLUB);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // the type of array elements.
    private Type _declaredElementType;

    private Type _elementType;

    private ElementTypeTerm _elemTypeTerm = new ElementTypeTerm();

    private static ArrayType _representative = new ArrayType(BaseType.UNKNOWN);

    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////
    private class ElementTypeTerm implements InequalityTerm {
        ///////////////////////////////////////////////////////////////
        ////                   public inner methods                ////

        /** Return this ArrayType.
         *  @return an ArrayType.
         */
        public Object getAssociatedObject() {
            return ArrayType.this;
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
        public void initialize(Object e) throws IllegalActionException {
            if (isConstant()) {
                throw new IllegalActionException(
                        "ArrayType$ElementTypeTerm.initialize: " + "This type "
                                + this + " is not settable.");
            }

            if (!(e instanceof Type)) {
                throw new IllegalActionException(
                        "ArrayType$ElementTypeTerm.initialize: "
                                + "The argument " + this + " is not a Type.");
            }

            if (_declaredElementType == BaseType.UNKNOWN) {
                _elementType = (Type) e;
            } else {
                // element type is a structured type.
                ((StructuredType) _elementType).initialize((Type) e);
            }
        }

        /** Test if the element type is a type variable.
         *  @return True if the element type is a type variable.
         */
        public boolean isSettable() {
            return !_declaredElementType.isConstant();
        }

        /** Check whether the current element type is acceptable.
         *  The element type is acceptable if it represents an
         *  instantiable object.
         *  @return True if the element type is acceptable.
         */
        public boolean isValueAcceptable() {
            return _elementType.isInstantiable();
        }

        /** Set the element type to the specified type.
         *  @param e a Type.
         *  @exception IllegalActionException If the specified type violates
         *   the declared type of the element.
         */
        public void setValue(Object e) throws IllegalActionException {
            if (!isSettable()) {
                throw new IllegalActionException(
                        "ArrayType$ElementTypeTerm.setValue: This type " + e
                                + " is not settable.");
            }

            if (!_declaredElementType.isSubstitutionInstance((Type) e)) {
                // The LUB of the _elementType and another type is General,
                // this is a type conflict.
                throw new IllegalActionException(
                        "ArrayType$ElementTypeTerm.setValue: "
                                + "Cannot update the element type of this array to "
                                + "the new type." + " Element type: "
                                + _declaredElementType.toString()
                                + ", New type: " + e.toString());
            }

            if (_declaredElementType == BaseType.UNKNOWN) {
                try {
                    _elementType = (Type) ((Type) e).clone();
                } catch (CloneNotSupportedException cnse) {
                    throw new InternalErrorException(
                            "ArrayType$ElementTypeTerm.setValue: "
                                    + "The specified type " + e
                                    + " cannot be cloned.");
                }
            } else {
                ((StructuredType) _elementType).updateType((StructuredType) e);
            }
        }

        /** Return a string representation of this term.
         *  @return A String.
         */
        public String toString() {
            return "(ArrayElementType(" + getAssociatedObject() + "), "
                    + getValue() + ")";
        }
    }
}
