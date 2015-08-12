/** A class representing the type of a RecordToken.

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

import java.util.Arrays;

import ptolemy.data.FunctionToken;
import ptolemy.data.Token;
import ptolemy.data.TupleToken;
import ptolemy.graph.CPO;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;

///////////////////////////////////////////////////////////////////
//// TupleType

/**
 A class representing the type of a FunctionToken.

 @author Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (neuendor)
 @Pt.AcceptedRating Red (cxh)
 */
public class TupleType extends StructuredType implements Cloneable {
    /** Construct a new TupleType with the specified argument types
     *  and the given return type.  To leave the types of some fields
     *  undeclared, use BaseType.UNKNOWN.  To construct the type for a
     *  function of no arguments, set the length of the argument array
     *  to 0.
     *  @param types An array of Type.
     *  @exception IllegalArgumentException If the labels and types do
     *  not have the same size.
     *  @exception NullPointerException If one of the arguments is null.
     */
    public TupleType(Type[] types) {
        _elementTypeTerms = new FieldTypeTerm[types.length];

        for (int i = 0; i < types.length; i++) {
            FieldTypeTerm fieldType = new FieldTypeTerm(types[i]);
            _elementTypeTerms[i] = fieldType;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a deep copy of this TupleType if it is a variable, or
     *  itself if it is a constant.
     *  @return A TupleType.
     */
    @Override
    public Object clone() {
        if (isConstant()) {
            return this;
        } else {
            // construct the labels and declared types array
            Type[] types = new Type[_elementTypeTerms.length];

            for (int i = 0; i < types.length; i++) {
                types[i] = getElementType(i);
            }

            TupleType newObj = new TupleType(types);

            try {
                newObj.updateType(this);
            } catch (IllegalActionException ex) {
                throw new InternalErrorException(null, ex,
                        "Failed to update new instance.");
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
    @Override
    public Token convert(Token token) throws IllegalActionException {
        if (!(token instanceof TupleToken)) {
            throw new IllegalArgumentException(
                    Token.notSupportedIncomparableConversionMessage(token,
                            toString()));
        }

        TupleToken argumentTupleToken = (TupleToken) token;

        Token[] argumentTuple = argumentTupleToken.tupleValue();
        Token[] resultArray = new Token[argumentTuple.length];

        if (argumentTupleToken.length() == _elementTypeTerms.length) {
            try {
                for (int i = 0; i < argumentTuple.length; i++) {
                    resultArray[i] = getElementType(i)
                            .convert(argumentTuple[i]);
                }
            } catch (IllegalActionException ex) {
                throw new IllegalActionException(null, ex,
                        Token.notSupportedConversionMessage(token, "int"));
            }
        }

        return new TupleToken(resultArray);
    }

    /** Determine if the argument represents the same TupleType as
     *  this object.  Two function types are equal if they have the same
     *  field names and the type of each field is the same, and they
     *  have the same return type.
     *  @param object Another object.
     *  @return True if the argument represents the same TupleType as
     *  this object.
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof TupleType)) {
            return false;
        }

        TupleType TupleType = (TupleType) object;

        if (getElementCount() != TupleType.getElementCount()) {
            return false;
        }

        for (int i = 0; i < getElementCount(); i++) {
            Type myType = this.getElementType(i);
            Type argType = TupleType.getElementType(i);

            if (!myType.equals(argType)) {
                return false;
            }
        }

        return true;
    }

    /** Return the number of arguments in this type.
     *  @return The number of arguments in this type.
     */
    public int getElementCount() {
        return _elementTypeTerms.length;
    }

    /** Return the type of the given argument.
     *  @param i The given argument.
     *  @return a Type.
     */
    public Type getElementType(int i) {
        if (i < 0 || i >= _elementTypeTerms.length) {
            return null;
        }

        FieldTypeTerm fieldType = _elementTypeTerms[i];

        if (fieldType == null) {
            return null;
        }

        return fieldType._resolvedType;
    }

    /** Return the class for tokens that this type represents.
     *  @return The class for tokens that this type represents.
     */
    @Override
    public Class getTokenClass() {
        return FunctionToken.class;
    }

    /** Return the InequalityTerm representing the type of the given
     *  argument.
     *  @param i The given argument.
     *  @return An InequalityTerm.
     *  @see ptolemy.graph.InequalityTerm
     */
    public FieldTypeTerm getArgTypeTerm(int i) {
        return _elementTypeTerms[i];
    }

    /** Return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(_elementTypeTerms) + 2917;
    }

    /** Set the elements that have declared type BaseType.UNKNOWN (the leaf
     *  type variable) to the specified type.
     *  @param type the type to set the leaf type variable to.
     */
    @Override
    public void initialize(Type type) {
        try {
            for (int i = 0; i < getElementCount(); i++) {
                FieldTypeTerm fieldType = getArgTypeTerm(i);

                if (fieldType.isSettable()) {
                    fieldType.initialize(type);
                }
            }
        } catch (IllegalActionException iae) {
            throw new InternalErrorException("TupleType.initialize: Cannot "
                    + "initialize the element type to " + type + " "
                    + iae.getMessage());
        }
    }

    /** Test if the argument type is compatible with this type.  The
     *  given type will be compatible with this type if it is
     *  BaseType.UNKNOWN, or...
     *  @param type An instance of Type.
     *  @return True if the argument is compatible with this type.
     */
    @Override
    public boolean isCompatible(Type type) {
        if (type.equals(BaseType.UNKNOWN)) {
            return true;
        }

        if (!(type instanceof TupleType)) {
            return false;
        }

        TupleType argumentTupleType = (TupleType) type;

        // The given type cannot be losslessly converted to this type
        // if it does not contain the same number of arguments.
        if (argumentTupleType.getElementCount() != getElementCount()) {
            return false;
        }

        // Loop through all of the fields of this type...
        for (int i = 0; i < getElementCount(); i++) {
            Type argumentFieldTypeTerm = argumentTupleType.getElementType(i);

            // The given function type cannot be losslessly converted
            // to this type if the individual arguments are not
            // compatible.
            Type thisFieldTypeTerm = getElementType(i);

            if (!argumentFieldTypeTerm.isCompatible(thisFieldTypeTerm)) {
                return false;
            }
        }

        return true;
    }

    /** Test if this TupleType is a constant. A TupleType is a
     *  constant if the declared type of all of its fields are
     *  constant.
     *  @return True if this type is a constant.
     */
    @Override
    public boolean isConstant() {
        // Loop through all of the fields of this type...
        for (int i = 0; i < getElementCount(); i++) {
            FieldTypeTerm fieldType = getArgTypeTerm(i);
            Type type = fieldType._declaredType;

            // Return false if the field is not constant.
            if (!type.isConstant()) {
                return false;
            }
        }

        return true;
    }

    /** Test if this type corresponds to an instantiable token
     *  class. A TupleType is instantiable if all of its fields are
     *  instantiable.
     *  @return True if this type is instantiable.
     */
    @Override
    public boolean isInstantiable() {
        // Loop through all of the fields of this type...
        for (int i = 0; i < getElementCount(); i++) {
            Type type = getElementType(i);

            // Return false if the field is not instantiable.
            if (!type.isInstantiable()) {
                return false;
            }
        }

        return true;
    }

    /** Test if the specified type is a substitution instance of this
     *  type.  One function is a substitution instance of another if they
     *  have arguments with the same types and each field of the given type is
     *  a substitution instance of the corresponding field in this type.
     *  @param type A Type.
     *  @return True if the argument is a substitution instance of this type.
     *  @see Type#isSubstitutionInstance
     */
    @Override
    public boolean isSubstitutionInstance(Type type) {
        if (!(type instanceof TupleType)) {
            return false;
        }

        TupleType TupleType = (TupleType) type;

        // Check that the argument counts are the same
        int argCount = getElementCount();

        if (TupleType.getElementCount() != argCount) {
            return false;
        }

        // Loop through all of the fields of this type...
        for (int i = 0; i < getElementCount(); i++) {
            Type myArgType = getElementType(i);
            Type argType = TupleType.getElementType(i);

            if (!myArgType.isSubstitutionInstance(argType)) {
                return false;
            }
        }

        return true;
    }

    /** Return the string representation of this type. The format is
     *  function(a0:&gt;type&lt;, a1:&gt;type&lt;, ...) &gt;type&lt;.
     *  Note that the function argument names are not semantically
     *  significant.
     *  @return A String.
     */
    @Override
    public String toString() {
        // construct the string representation of this token.
        StringBuffer s = new StringBuffer("{");

        for (int i = 0; i < getElementCount(); i++) {
            if (i != 0) {
                s.append(", ");
            }

            s.append("a" + i + ":" + getElementType(i));
        }

        return s.toString() + "}";
    }

    /** Update this type to the specified TupleType.
     *  The specified type must be a TupleType and have the same structure
     *  as this one.
     *  This method will only update the component whose declared type is
     *  BaseType.UNKNOWN, and leave the constant part of this type intact.
     *  @param newType A StructuredType.
     *  @exception IllegalActionException If the specified type is not a
     *   TupleType or it does not have the same structure as this one.
     */
    @Override
    public void updateType(StructuredType newType)
            throws IllegalActionException {
        if (this.isConstant()) {
            if (this.equals(newType)) {
                return;
            } else {
                throw new IllegalActionException("TupleType.updateType: "
                        + "This type is a constant and the argument is not the"
                        + " same as this type. This type: " + this.toString()
                        + " argument: " + newType.toString());
            }
        }

        // This type is a variable.
        if (!this.isSubstitutionInstance(newType)) {
            throw new IllegalActionException("TupleType.updateType: "
                    + "Cannot update this type to the new type.");
        }

        // Loop through all of the fields of this type...
        for (int i = 0; i < getElementCount(); i++) {
            FieldTypeTerm argTypeTerm = getArgTypeTerm(i);

            if (argTypeTerm.isSettable()) {
                Type newArgType = ((TupleType) newType).getElementType(i);
                argTypeTerm.setValue(newArgType);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Compare this type with the specified type. The specified type
     *  must be a TupleType, otherwise an exception will be thrown.
     *
     *  This method returns one of ptolemy.graph.CPO.LOWER,
     *  ptolemy.graph.CPO.SAME, ptolemy.graph.CPO.HIGHER,
     *  ptolemy.graph.CPO.INCOMPARABLE, indicating this type is lower
     *  than, equal to, higher than, or incomparable with the
     *  specified type in the type hierarchy, respectively.
     *  @param type a TupleType.
     *  @return An integer.
     *  @exception IllegalArgumentException If the specified type is
     *   not a TupleType.
     */
    @Override
    protected int _compare(StructuredType type) {
        if (!(type instanceof TupleType)) {
            throw new IllegalArgumentException("TupleType.compare: "
                    + "The argument is not a TupleType.");
        }

        if (this.equals(type)) {
            return CPO.SAME;
        }

        if (_isLessThanOrEqualTo(this, (TupleType) type)) {
            return CPO.LOWER;
        }

        if (_isLessThanOrEqualTo((TupleType) type, this)) {
            return CPO.HIGHER;
        }

        return CPO.INCOMPARABLE;
    }

    /** Return a static instance of TupleType.
     *  @return a TupleType.
     */
    @Override
    protected StructuredType _getRepresentative() {
        return _representative;
    }

    /** Return the greatest lower bound of this type with the specified
     *  type. The specified type must be a TupleType, otherwise an
     *  exception will be thrown.
     *  @param type a TupleType.
     *  @return a TupleType.
     *  @exception IllegalArgumentException If the specified type is
     *   not a TupleType.
     */
    @Override
    protected StructuredType _greatestLowerBound(StructuredType type) {
        if (!(type instanceof TupleType)) {
            throw new IllegalArgumentException(
                    "TupleType.greatestLowerBound: The argument is not a "
                            + "TupleType.");
        }

        TupleType TupleType = (TupleType) type;

        // construct the GLB FunctionToken
        int argCount = getElementCount();

        if (TupleType.getElementCount() != argCount) {
            throw new IllegalArgumentException(
                    "Types are not comparable because they have"
                            + " different numbers of arguments");
        }

        Type[] types = new Type[argCount];

        for (int i = 0; i < argCount; i++) {
            Type type1 = getElementType(i);
            Type type2 = TupleType.getElementType(i);

            if (type1 == null) {
                types[i] = type2;
            } else if (type2 == null) {
                types[i] = type1;
            } else {
                types[i] = (Type) TypeLattice.lattice().greatestLowerBound(
                        type1, type2);
            }
        }

        return new TupleType(types);
    }

    /** Return the least upper bound of this type with the specified
     *  type. The specified type must be a TupleType, otherwise an
     *  exception will be thrown.
     *  @param type a TupleType.
     *  @return a TupleType.
     *  @exception IllegalArgumentException If the specified type is
     *   not a TupleType.
     */
    @Override
    protected StructuredType _leastUpperBound(StructuredType type) {
        if (!(type instanceof TupleType)) {
            throw new IllegalArgumentException("TupleType.leastUpperBound: "
                    + "The argument is not a TupleType.");
        }

        TupleType TupleType = (TupleType) type;

        // construct the LUB FunctionToken
        int argCount = getElementCount();

        if (TupleType.getElementCount() != argCount) {
            throw new IllegalArgumentException(
                    "Types are not comparable because they have"
                            + " different numbers of arguments");
        }

        Type[] types = new Type[argCount];

        for (int i = 0; i < argCount; i++) {
            Type type1 = getElementType(i);
            Type type2 = TupleType.getElementType(i);

            if (type1 == null) {
                types[i] = type2;
            } else if (type2 == null) {
                types[i] = type1;
            } else {
                types[i] = (Type) TypeLattice.lattice().leastUpperBound(type1,
                        type2);
            }
        }

        return new TupleType(types);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // Test if the first TupleType is less than or equal to the second
    private boolean _isLessThanOrEqualTo(TupleType t1, TupleType t2) {
        // construct the LUB FunctionToken
        int argCount = t1.getElementCount();

        if (t2.getElementCount() != argCount) {
            return false;
        }

        // iterate over the labels of the second type
        for (int i = 0; i < argCount; i++) {
            Type type1 = t1.getElementType(i);
            Type type2 = t2.getElementType(i);
            int result = TypeLattice.compare(type1, type2);

            if (result == CPO.HIGHER || result == CPO.INCOMPARABLE) {
                return false;
            }
        }

        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Mapping from label to field information.
    private FieldTypeTerm[] _elementTypeTerms;

    // the representative in the type lattice is the empty function.
    private static TupleType _representative = new TupleType(new Type[0]);

    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////
    // A class that encapsulates the declared and resolved types of a
    // field and implements the InequalityTerm interface.
    private class FieldTypeTerm implements InequalityTerm {
        // Construct an instance of FieldTypeTerm.
        private FieldTypeTerm(Type declaredType) {
            try {
                _declaredType = (Type) declaredType.clone();
                _resolvedType = _declaredType;
            } catch (CloneNotSupportedException cnse) {
                throw new InternalErrorException("TupleType.FieldTypeTerm: "
                        + "The specified type cannot be cloned.");
            }
        }

        ///////////////////////////////////////////////////////////////
        ////                   public inner methods                ////

        /** Return this TupleType.
         *  @return a TupleType.
         */
        @Override
        public Object getAssociatedObject() {
            return TupleType.this;
        }

        /** Return the resolved type.
         *  @return a Type.
         */
        @Override
        public Object getValue() {
            return _resolvedType;
        }

        /** Return this FieldTypeTerm in an array if it represents a type
         *  variable. Otherwise, return an array of size zero.
         *  @return An array of InequalityTerm.
         */
        @Override
        public InequalityTerm[] getVariables() {
            if (isSettable()) {
                InequalityTerm[] variable = new InequalityTerm[1];
                variable[0] = this;
                return variable;
            }

            return new InequalityTerm[0];
        }

        /** Reset the variable part of the element type to the specified
         *  type.
         *  @param e A Type.
         *  @exception IllegalActionException If this type is not settable,
         *   or the argument is not a Type.
         */
        @Override
        public void initialize(Object e) throws IllegalActionException {
            if (!isSettable()) {
                throw new IllegalActionException("TupleType$FieldTypeTerm."
                        + "initialize: The type is not settable.");
            }

            if (!(e instanceof Type)) {
                throw new IllegalActionException("FieldTypeTerm.initialize: "
                        + "The argument is not a Type.");
            }

            if (_declaredType == BaseType.UNKNOWN) {
                _resolvedType = (Type) e;
            } else {
                // this field type is a structured type.
                ((StructuredType) _resolvedType).initialize((Type) e);
            }
        }

        /** Test if this field type is a type variable.
         *  @return True if this field type is a type variable.
         */
        @Override
        public boolean isSettable() {
            return !_declaredType.isConstant();
        }

        /** Check whether the current element type is acceptable.
         *  The element type is acceptable if it represents an
         *  instantiable object.
         *  @return True if the element type is acceptable.
         */
        @Override
        public boolean isValueAcceptable() {
            return _resolvedType.isInstantiable();
        }

        /** Set the element type to the specified type.
         *  @param e a Type.
         *  @exception IllegalActionException If the specified type violates
         *   the declared field type.
         */
        @Override
        public void setValue(Object e) throws IllegalActionException {
            if (!isSettable()) {
                throw new IllegalActionException(
                        "TupleType$FieldTypeTerm.setValue: The type is not "
                                + "settable.");
            }

            if (!_declaredType.isSubstitutionInstance((Type) e)) {
                throw new IllegalActionException("FieldTypeTerm.setValue: "
                        + "Cannot update the field type of this TupleType "
                        + "to the new type." + " Field type: "
                        + _declaredType.toString() + ", New type: "
                        + e.toString());
            }

            if (_declaredType == BaseType.UNKNOWN) {
                try {
                    _resolvedType = (Type) ((Type) e).clone();
                } catch (CloneNotSupportedException cnse) {
                    throw new InternalErrorException(
                            "TupleType$FieldTypeTerm.setValue: "
                                    + "The specified type cannot be cloned.");
                }
            } else {
                ((StructuredType) _resolvedType).updateType((StructuredType) e);
            }
        }

        /** Return a string representation of this term.
         *  @return A String.
         */
        @Override
        public String toString() {
            return "(FunctionFieldTypeTerm, " + getValue() + ")";
        }

        ///////////////////////////////////////////////////////////////
        ////                  private inner variables              ////
        private Type _declaredType = null;

        private Type _resolvedType = null;
    }
}
