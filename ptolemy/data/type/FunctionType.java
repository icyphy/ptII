/** A class representing the type of a RecordToken.

 Copyright (c) 1997-2003 The Regents of the University of California.
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

import ptolemy.data.FunctionToken;
import ptolemy.data.Token;
import ptolemy.graph.CPO;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;

//////////////////////////////////////////////////////////////////////////
//// FunctionType
/**
A class representing the type of a FunctionToken.

@author Steve Neuendorffer
$Id$
*/

public class FunctionType extends StructuredType {

    /** Construct a new FunctionType with the specified argument types
     *  and the given return type.  To leave the types of some fields
     *  undeclared, use BaseType.UNKNOWN.  To construct the type for a
     *  function of no arguments, set the length of the argument array
     *  to 0.
     *  @param types An array of Type.
     *  @param returnType An type.
     *  @exception IllegalArgumentException If the labels and types do
     *  not have the same size.
     *  @exception NullPointerException If one of the arguments is null.
     */
    public FunctionType(Type[] types, Type returnType) {
        _argTypeTerms = new FieldTypeTerm[types.length];
        for (int i = 0; i < types.length; i++) {
            FieldTypeTerm fieldType = new FieldTypeTerm(types[i]);
            _argTypeTerms[i] = fieldType;
        }
        _returnTypeTerm = new FieldTypeTerm(returnType);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a deep copy of this FunctionType if it is a variable, or
     *  itself if it is a constant.
     *  @return A FunctionType.
     */
    public Object clone() {
        if (isConstant()) {
            return this;
        } else {
            // construct the labels and declared types array
            Type[] types = new Type[_argTypeTerms.length];
            for (int i = 0; i < types.length; i++) {
                types[i] = getArgType(i);
            }
            FunctionType newObj =
                new FunctionType(types, getReturnType());
            try {
                newObj.updateType(this);
            } catch (IllegalActionException ex) {
                throw new InternalErrorException(null, ex,
                        "Failed to update new instance.");
            }
            return newObj;
        }
    }

    /** Convert the argument token into a FunctionToken having this type,
     *  if lossless conversion can be done.  The argument must be an
     *  FunctionToken, and its type must be a subtype of this record type.
     *  @param token A token.
     *  @return An FunctionToken.
     *  @exception IllegalActionException If lossless conversion
     *   cannot be done.
     */
    public Token convert(Token token) throws IllegalActionException {

        if ( !isCompatible(token.getType())) {
            throw new IllegalArgumentException(
                    Token.notSupportedConversionMessage(
                            token, this.toString()));
        }
        // FIXME: This should actually return a new Function that
        // includes the appropriate argument and return value
        // conversions.

        return token;
        //   if (false) {
        /*FunctionToken functionToken = (FunctionToken)token;
          // The converted token has the same set of labels as the argument.
          // That is, fields not in this type are not cut off.
          Object[] labelArray = functionToken.labelSet().toArray();

          // Arrays that will be used to create the new token.
          String[] labelStringArray = new String[labelArray.length];
          Token[] values = new Token[labelArray.length];

          for (int i = 0; i < labelArray.length; i++) {
          String label = (String)labelArray[i];

          // Convert each field of the function.
          Token fieldToken = functionToken.get(label);
          Type newFieldTypeTerm = get(label);

          // If the type of the field is specified, then convert it.
          if (newFieldTypeTerm != null) {
          values[i] = newFieldTypeTerm.convert(fieldToken);
          } else {
          values[i] = fieldToken;
          }

          // Store the label for each field.
          labelStringArray[i] = label;
          }

          return new FunctionToken(labelStringArray, values);
          }*/
    }

    /** Determine if the argument represents the same FunctionType as
     *  this object.  Two function types are equal if they have the same
     *  field names and the type of each field is the same, and they
     *  have the same return type.
     *  @param object Another object.
     *  @return True if the argument represents the same FunctionType as
     *  this object.
     */
    public boolean equals(Object object) {
        if (!(object instanceof FunctionType)) {
            return false;
        }

        FunctionType functionType = (FunctionType)object;

        if (getArgCount() != functionType.getArgCount()) {
            return false;
        }

        for (int i = 0; i < getArgCount(); i++) {
            Type myType = this.getArgType(i);
            Type argType = functionType.getArgType(i);
            if (!myType.equals(argType)) {
                return false;
            }
        }

        if (!getReturnType().equals(functionType.getReturnType())) {
            return false;
        }

        return true;
    }

    /** Return the number of arguments in this type
     */
    public int getArgCount() {
        return _argTypeTerms.length;
    }

    /** Return the type of the given argument.
     *  @return a Type.
     */
    public Type getArgType(int i) {
        if (i < 0 || i >= _argTypeTerms.length) {
            return null;
        }
        FieldTypeTerm fieldType = _argTypeTerms[i];
        if (fieldType == null) {
            return null;
        }
        return fieldType._resolvedType;
    }

    /** Return the type of the specified label.
     *  @return a Type.
     */
    public Type getReturnType() {
        return _returnTypeTerm._resolvedType;
    }

    /** Return the class for tokens that this type represents.
     */
    public Class getTokenClass() {
        return FunctionToken.class;
    }

    /** Return the InequalityTerm representing the type of the given
     *  argument.
     *  @return An InequalityTerm.
     *  @see ptolemy.graph.InequalityTerm
     */
    public FieldTypeTerm getArgTypeTerm(int i) {
        return _argTypeTerms[i];
    }

    /** Return a hash code value for this object.
     */
    public int hashCode() {
        return _returnTypeTerm.hashCode() + 1;
    }

    /** Set the elements that have declared type BaseType.UNKNOWN (the leaf
     *  type variable) to the specified type.
     *  @param type the type to set the leaf type variable to.
     */
    public void initialize(Type type) {
        try {
            for (int i = 0; i < getArgCount(); i++) {
                FieldTypeTerm fieldType = (FieldTypeTerm)getArgTypeTerm(i);
                if (fieldType.isSettable()) {
                    fieldType.initialize(type);
                }
            }
        } catch (IllegalActionException iae) {
            throw new InternalErrorException(
                    "FunctionType.initialize: Cannot " +
                    "initialize the element type to " + type + " " +
                    iae.getMessage());
        }
    }

    /** Test if the argument type is compatible with this type.  The
     *  given type will be compatible with this type if it is
     *  BaseType.UNKNOWN, or...
     *  @param type An instance of Type.
     *  @return True if the argument is compatible with this type.
     */
    public boolean isCompatible(Type type) {
        if (type.equals(BaseType.UNKNOWN)) {
            return true;
        }

        if ( !(type instanceof FunctionType)) {
            return false;
        }

        FunctionType argumentFunctionType = (FunctionType)type;

        // The given type cannot be losslessly converted to this type
        // if it does not contain the same number of arguments.
        if (argumentFunctionType.getArgCount() != getArgCount()) {
            return false;
        }

        // Loop through all of the fields of this type...
        for (int i = 0; i < getArgCount(); i++) {
            Type argumentFieldTypeTerm =
                argumentFunctionType.getArgType(i);

            // The given function type cannot be losslessly converted
            // to this type if the individual arguments are not
            // compatible.
            Type thisFieldTypeTerm = getArgType(i);
            if (!argumentFieldTypeTerm.isCompatible(thisFieldTypeTerm)) {
                return false;
            }
        }

        return true;
    }

    /** Test if this FunctionType is a constant. A FunctionType is a
     *  constant if the declared type of all of its fields are
     *  constant.
     *  @return True if this type is a constant.
     */
    public boolean isConstant() {
        // Loop through all of the fields of this type...
        for (int i = 0; i < getArgCount(); i++) {
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
     *  class. A FunctionType is instantiable if all of its fields are
     *  instantiable.
     *  @return True if this type is instantiable.
     */
    public boolean isInstantiable() {
        // Loop through all of the fields of this type...
        for (int i = 0; i < getArgCount(); i++) {
            Type type = getArgType(i);
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
    public boolean isSubstitutionInstance(Type type) {
        if (!(type instanceof FunctionType)) {
            return false;
        }

        FunctionType functionType = (FunctionType)type;

        // Check that the argument counts are the same
        int argCount = getArgCount();

        if (functionType.getArgCount() != argCount) {
            return false;
        }

        // Loop through all of the fields of this type...
        for (int i = 0; i < getArgCount(); i++) {
            Type myArgType = getArgType(i);
            Type argType = functionType.getArgType(i);
            if (!myArgType.isSubstitutionInstance(argType)) {
                return false;
            }
        }

        // Check the return type.
        if (!getReturnType().isSubstitutionInstance(
                functionType.getReturnType())) {
            return false;
        }

        return true;
    }

    /** Return the string representation of this type. The format is
     *  function(a0:<type>, a1:<type>, ...) <type>.  Note that the
     *  function argument names are not semantically significant.
     *  @return A String.
     */
    public String toString() {
        // construct the string representation of this token.
        String s = "(function(";
        for (int i = 0; i < getArgCount(); i++) {
            if (i != 0) {
                s += ", ";
            }
            s += "a" + i + ":" + getArgType(i);
        }
        return s + ") " + getReturnType() + ")";
    }

    /** Update this type to the specified FunctionType.
     *  The specified type must be a FunctionType and have the same structure
     *  as this one.
     *  This method will only update the component whose declared type is
     *  BaseType.UNKNOWN, and leave the constant part of this type intact.
     *  @param newType A StructuredType.
     *  @exception IllegalActionException If the specified type is not a
     *   FunctionType or it does not have the same structure as this one.
     */
    public void updateType(StructuredType newType)
            throws IllegalActionException {
        if (this.isConstant()) {
            if (this.equals(newType)) {
                return;
            } else {
                throw new IllegalActionException("FunctionType.updateType: " +
                        "This type is a constant and the argument is not the" +
                        " same as this type. This type: " + this.toString() +
                        " argument: " + newType.toString());
            }
        }

        // This type is a variable.
        if ( !this.isSubstitutionInstance(newType)) {
            throw new IllegalActionException("FunctionType.updateType: "
                    + "Cannot update this type to the new type.");
        }

        // Loop through all of the fields of this type...
        for (int i = 0; i < getArgCount(); i++) {
            FieldTypeTerm argTypeTerm = getArgTypeTerm(i);
            if (argTypeTerm.isSettable()) {
                Type newArgType =
                    ((FunctionType)newType).getArgType(i);
                argTypeTerm.setValue(newArgType);
            }
        }

        if (_returnTypeTerm.isSettable()) {
            _returnTypeTerm.setValue(((FunctionType)newType).getReturnType());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Compare this type with the specified type. The specified type
     *  must be a FunctionType, otherwise an exception will be thrown.
     *
     *  This method returns one of ptolemy.graph.CPO.LOWER,
     *  ptolemy.graph.CPO.SAME, ptolemy.graph.CPO.HIGHER,
     *  ptolemy.graph.CPO.INCOMPARABLE, indicating this type is lower
     *  than, equal to, higher than, or incomparable with the
     *  specified type in the type hierarchy, respectively.
     *  @param type a FunctionType.
     *  @return An integer.
     *  @exception IllegalArgumentException If the specified type is
     *   not a FunctionType.
     */
    protected int _compare(StructuredType type) {
        if ( !(type instanceof FunctionType)) {
            throw new IllegalArgumentException("FunctionType.compare: " +
                    "The argument is not a FunctionType.");
        }

        if (this.equals(type)) {
            return CPO.SAME;
        }

        if (_isLessThanOrEqualTo(this, (FunctionType)type)) {
            return CPO.LOWER;
        }

        if (_isLessThanOrEqualTo((FunctionType)type, this)) {
            return CPO.HIGHER;
        }

        return CPO.INCOMPARABLE;
    }

    /** Return a static instance of FunctionType.
     *  @return a FunctionType.
     */
    protected StructuredType _getRepresentative() {
        return _representative;
    }

    /** Return the greatest lower bound of this type with the specified
     *  type. The specified type must be a FunctionType, otherwise an
     *  exception will be thrown.
     *  @param type a FunctionType.
     *  @return a FunctionType.
     *  @exception IllegalArgumentException If the specified type is
     *   not a FunctionType.
     */
    protected StructuredType _greatestLowerBound(StructuredType type) {
        if ( !(type instanceof FunctionType)) {
            throw new IllegalArgumentException(
                    "FunctionType.greatestLowerBound: The argument is not a " +
                    "FunctionType.");
        }

        FunctionType functionType = (FunctionType)type;

        // construct the GLB FunctionToken
        int argCount = getArgCount();

        if ( functionType.getArgCount() != argCount ) {
            throw new IllegalArgumentException(
                    "Types are not comparable because they have" +
                    " different numbers of arguments");
        }

        Type[] types = new Type[argCount];

        for (int i = 0; i < argCount; i++) {
            Type type1 = getArgType(i);
            Type type2 = functionType.getArgType(i);
            if (type1 == null) {
                types[i] = type2;
            } else if (type2 == null) {
                types[i] = type1;
            } else {
                types[i] = (Type)TypeLattice.lattice().greatestLowerBound(
                        type1, type2);
            }
        }
        Type returnType = (Type)TypeLattice.lattice().greatestLowerBound(
                getReturnType(), functionType.getReturnType());

        return new FunctionType(types, returnType);
    }

    /** Return the least upper bound of this type with the specified
     *  type. The specified type must be a FunctionType, otherwise an
     *  exception will be thrown.
     *  @param type a FunctionType.
     *  @return a FunctionType.
     *  @exception IllegalArgumentException If the specified type is
     *   not a FunctionType.
     */
    protected StructuredType _leastUpperBound(StructuredType type) {
        if ( !(type instanceof FunctionType)) {
            throw new IllegalArgumentException("FunctionType.leastUpperBound: "
                    + "The argument is not a FunctionType.");
        }

        FunctionType functionType = (FunctionType)type;

        // construct the LUB FunctionToken
        int argCount = getArgCount();

        if ( functionType.getArgCount() != argCount ) {
            throw new IllegalArgumentException(
                    "Types are not comparable because they have" +
                    " different numbers of arguments");
        }

        Type[] types = new Type[argCount];

        for (int i = 0; i < argCount; i++) {
            Type type1 = getArgType(i);
            Type type2 = functionType.getArgType(i);
            if (type1 == null) {
                types[i] = type2;
            } else if (type2 == null) {
                types[i] = type1;
            } else {
                types[i] = (Type)TypeLattice.lattice().leastUpperBound(
                        type1, type2);
            }
        }
        Type returnType = (Type)TypeLattice.lattice().leastUpperBound(
                getReturnType(), functionType.getReturnType());

        return new FunctionType(types, returnType);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Test if the first FunctionType is less than or equal to the second
    private boolean _isLessThanOrEqualTo(FunctionType t1, FunctionType t2) {

        // construct the LUB FunctionToken
        int argCount = t1.getArgCount();

        if ( t2.getArgCount() != argCount ) {
            return false;
        }

        // iterate over the labels of the second type
        for (int i = 0; i < argCount; i++) {
            Type type1 = t1.getArgType(i);
            Type type2 = t2.getArgType(i);
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
    private FieldTypeTerm[] _argTypeTerms;

    private FieldTypeTerm _returnTypeTerm;
    // the representative in the type lattice is the empty function.
    private static FunctionType _representative =
    new FunctionType(new Type[0], BaseType.UNKNOWN);

    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////

    // A class that encapsulates the declared and resolved types of a
    // field and implements the InequalityTerm interface.
    private class FieldTypeTerm implements InequalityTerm {

        // Construct an instance of FieldTypeTerm.
        private FieldTypeTerm(Type declaredType) {
            try {
                _declaredType = (Type)declaredType.clone();
                _resolvedType = _declaredType;
            } catch (CloneNotSupportedException cnse) {
                throw new InternalErrorException("FunctionType.FieldTypeTerm: " +
                        "The specified type cannot be cloned.");
            }
        }

        ///////////////////////////////////////////////////////////////
        ////                   public inner methods                ////

        /** Return this FunctionType.
         *  @return a FunctionType.
         */
        public Object getAssociatedObject() {
            return FunctionType.this;
        }

        /** Return the resolved type.
         *  @return a Type.
         */
        public Object getValue() {
            return _resolvedType;
        }

        /** Return this FieldTypeTerm in an array if it represents a type
         *  variable. Otherwise, return an array of size zero.
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
         *  @exception IllegalActionException If this type is not settable,
         *   or the argument is not a Type.
         */
        public void initialize(Object e) throws IllegalActionException {
            if (!isSettable()) {
                throw new IllegalActionException("FunctionType$FieldTypeTerm." +
                        "initialize: The type is not settable.");
            }

            if (!(e instanceof Type)) {
                throw new IllegalActionException("FieldTypeTerm.initialize: "
                        + "The argument is not a Type.");
            }

            if (_declaredType == BaseType.UNKNOWN) {
                _resolvedType = (Type)e;
            } else {
                // this field type is a structured type.
                ((StructuredType)_resolvedType).initialize((Type)e);
            }
        }

        /** Test if this field type is a type variable.
         *  @return True if this field type is a type variable.
         */
        public boolean isSettable() {
            return (!_declaredType.isConstant());
        }

        /** Check whether the current element type is acceptable.
         *  The element type is acceptable if it represents an
         *  instantiable object.
         *  @return True if the element type is acceptable.
         */
        public boolean isValueAcceptable() {
            return _resolvedType.isInstantiable();
        }

        /** Set the element type to the specified type.
         *  @param e a Type.
         *  @exception IllegalActionException If the specified type violates
         *   the declared field type.
         */
        public void setValue(Object e) throws IllegalActionException {
            if ( !isSettable()) {
                throw new IllegalActionException(
                        "FunctionType$FieldTypeTerm.setValue: The type is not " +
                        "settable.");
            }

            if ( !_declaredType.isSubstitutionInstance((Type)e)) {
                throw new IllegalActionException("FieldTypeTerm.setValue: "
                        + "Cannot update the field type of this FunctionType "
                        + "to the new type."
                        + " Field type: " + _declaredType.toString()
                        + ", New type: " + e.toString());
            }

            if (_declaredType == BaseType.UNKNOWN) {
                try {
                    _resolvedType = (Type)((Type)e).clone();
                } catch (CloneNotSupportedException cnse) {
                    throw new InternalErrorException(
                            "FunctionType$FieldTypeTerm.setValue: " +
                            "The specified type cannot be cloned.");
                }
            } else {
                ((StructuredType)_resolvedType).updateType((StructuredType)e);
            }
        }

        /** Return a string representation of this term.
         *  @return A String.
         */
        public String toString() {
            return "(FunctionFieldTypeTerm, " + getValue() + ")";
        }

        ///////////////////////////////////////////////////////////////
        ////                  private inner variables              ////

        private Type _declaredType = null;
        private Type _resolvedType = null;
    }
}

