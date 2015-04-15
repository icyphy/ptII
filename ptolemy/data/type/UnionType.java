/** A class representing the type of a UnionToken.

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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import ptolemy.data.Token;
import ptolemy.data.UnionToken;
import ptolemy.graph.CPO;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;

///////////////////////////////////////////////////////////////////
//// UnionType

/**
 A class representing the type of a UnionToken.
 To set the type of a typeable object (such as a port or parameter)
 to a union with particular fields, create an instance of this
 class and call setTypeEquals() with that instance as an argument.
 <p>
 The depth subtyping is similar to that of <code>RecordTypes</code>. However,
 the width subtyping for <code>UnionType</code> is opposite compared to
 <code>RecordType</code> i.e., a <code>UnionType</code> with more fields
 is a supertype of a <code>UnionType</code> with a subset of the fields.
 For example, {|x = double, y = int} is a supertype of {|x = double}.

 @author Yuhong Xiong, Elaine Cheong and Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Red (yuhongx)
 @Pt.AcceptedRating Red (cxh)
 */
public class UnionType extends AssociativeType implements Cloneable {
    /** Construct a new UnionType with the specified labels and types.
     *  To leave the types of some fields undeclared, use BaseType.UNKNOWN.
     *  The labels and the types are specified in two arrays. These two
     *  arrays must have the same length, and their elements have one to
     *  one correspondence. That is, the i'th entry in the types array is
     *  the type for the i'th label in the labels array. To construct the
     *  empty record type, set the length of the argument arrays to 0.
     *  @param labels An array of String.
     *  @param types An array of Type.
     *  @exception IllegalArgumentException If the two arrays do not have
     *   the same size.
     *  @exception NullPointerException If one of the arguments is null.
     */
    public UnionType(String[] labels, Type[] types) {
        if (labels.length != types.length) {
            throw new IllegalArgumentException("UnionType: the labels "
                    + "and types arrays do not have the same size.");
        }

        for (int i = 0; i < labels.length; i++) {
            FieldType fieldType = new FieldType(types[i]);
            _fields.put(labels[i], fieldType);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a deep copy of this UnionType if it is a variable, or
     *  itself if it is a constant.
     *  @return A UnionType.
     */
    @Override
    public Object clone() {
        if (isConstant()) {
            return this;
        }

        // empty union is a constant, so this record type is not empty.
        // construct the labels and declared types array
        Object[] labelsObj = _fields.keySet().toArray();
        String[] labels = new String[labelsObj.length];
        Type[] types = new Type[labelsObj.length];

        for (int i = 0; i < labels.length; i++) {
            labels[i] = (String) labelsObj[i];

            FieldType fieldType = (FieldType) _fields.get(labels[i]);
            types[i] = fieldType._declaredType;
        }

        UnionType newObj = new UnionType(labels, types);

        try {
            newObj.updateType(this);
        } catch (IllegalActionException ex) {
            throw new InternalErrorException("UnionType.clone: Cannot "
                    + "update new instance. " + ex.getMessage());
        }

        return newObj;
    }

    /** Convert the argument token into a UnionToken having this
     *  type, if lossless conversion can be done.  The argument must
     *  be a UnionToken, and its type must be a subtype of this
     *  record type.  The argument token must have at least the fields
     *  of this type.  Extra fields in the argument token that are not
     *  in this type are removed.
     *  @param token A token.
     *  @return An UnionToken.
     *  @exception IllegalActionException If lossless conversion
     *   cannot be done.
     */
    @Override
    public Token convert(Token token) throws IllegalActionException {
        if (!isCompatible(token.getType())) {
            throw new IllegalArgumentException(
                    Token.notSupportedConversionMessage(token, this.toString()));
        }

        UnionToken unionToken = (UnionToken) token;

        // The converted token has the same label as this one.
        String label = unionToken.label();
        Type newType = get(label);
        Token newValue = newType.convert(unionToken.value());
        return new UnionToken(label, newValue);
    }

    /** Return the depth of a union type. The depth of a
     *  union type is the number of times it
     *  contains other structured types.
     *  @return the depth of a union type.
     */
    @Override
    public int depth() {
        Object[] labelsObj = _fields.keySet().toArray();
        String[] labels = new String[labelsObj.length];
        int[] depth = new int[labelsObj.length];
        int maxDepth = 1;
        for (int i = 0; i < labels.length; i++) {
            labels[i] = (String) labelsObj[i];
            Type fieldType = get(labels[i]);
            depth[i] = 1;
            if (fieldType instanceof StructuredType) {
                depth[i] += ((StructuredType) fieldType).depth();
            }
            if (depth[i] > maxDepth) {
                maxDepth = depth[i];
            }
        }
        return maxDepth;
    }

    /** Determine if the argument represents the same UnionType as this
     *  object.  Two record types are equal if they have the same field names
     *  and the type of each field is the same.
     *  @param object Another object.
     *  @return True if the argument represents the same UnionType as
     *  this object.
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof UnionType)) {
            return false;
        }

        UnionType unionType = (UnionType) object;

        // Check that the label sets are equal
        Set myLabelSet = _fields.keySet();
        Set argLabelSet = unionType._fields.keySet();

        if (!myLabelSet.equals(argLabelSet)) {
            return false;
        }

        Iterator fieldNames = myLabelSet.iterator();

        while (fieldNames.hasNext()) {
            String label = (String) fieldNames.next();
            Type myType = this.get(label);
            Type argType = unionType.get(label);

            if (!myType.equals(argType)) {
                return false;
            }
        }

        return true;
    }

    /** Return the type of the specified label. If this type does not
     *  contain the specified label, return null.
     *  @param label The specified label.
     *  @return a Type.
     */
    @Override
    public Type get(String label) {
        FieldType fieldType = (FieldType) _fields.get(label);

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
        return UnionToken.class;
    }

    /** Return the InequalityTerm representing the type of the specified
     *  label.
     *  @param label The specified label.
     *  @return An InequalityTerm.
     *  @see ptolemy.graph.InequalityTerm
     */
    public InequalityTerm getTypeTerm(String label) {
        return (InequalityTerm) _fields.get(label);
    }

    /** Return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return _fields.keySet().hashCode() + 2917;
    }

    /** Set the elements that have declared type BaseType.UNKNOWN (the leaf
     *  type variable) to the specified type.
     *  @param type the type to set the leaf type variable to.
     */
    @Override
    public void initialize(Type type) {
        try {
            for (Map.Entry<String, FieldType> fields: _fields.entrySet()) {
                FieldType fieldType = fields.getValue();
                if (fieldType.isSettable()) {
                    fieldType.initialize(type);
                }
            }
        } catch (IllegalActionException iae) {
            throw new InternalErrorException("UnionType.initialize: Cannot "
                    + "initialize the element type to " + type + " "
                    + iae.getMessage());
        }
    }

    /** Test if this type corresponds to an abstract token
     *  class. A UnionType is abstract if any field is abstract
     *  @return True if this type is instantiable.
     */
    @Override
    public boolean isAbstract() {
        // Loop through all of the fields.
        Iterator fieldNames = _fields.keySet().iterator();

        while (fieldNames.hasNext()) {
            String label = (String) fieldNames.next();
            Type type = this.get(label);

            // Return false if the field is not instantiable.
            if (type.isAbstract()) {
                return true;
            }
        }

        return false;
    }

    /** Test if the argument type is compatible with this type.  The
     *  given type will be compatible with this type if it is
     *  BaseType.UNKNOWN, or a UnionType that contains at most as
     *  many fields.
     *  @param type An instance of Type.
     *  @return True if the argument is compatible with this type.
     */
    @Override
    public boolean isCompatible(Type type) {
        if (type.equals(BaseType.UNKNOWN)) {
            return true;
        }

        if (!(type instanceof UnionType)) {
            return false;
        }

        UnionType argumentUnionType = (UnionType) type;

        // Loop through all of the fields of argument type...
        Iterator iterator = argumentUnionType.labelSet().iterator();

        while (iterator.hasNext()) {
            String label = (String) iterator.next();

            // The given type cannot be losslessly converted to this type
            // if it contains extra fields
            Type myFieldType = this.get(label);

            if (myFieldType == null) {
                // This token does not contain a label in the argument
                return false;
            }

            // The given type cannot be losslessly converted to this type
            // if the individual fields are not compatible.
            Type argumentFieldType = argumentUnionType.get(label);

            if (!myFieldType.isCompatible(argumentFieldType)) {
                return false;
            }
        }

        return true;
    }

    /** Test if this UnionType is a constant. A UnionType is a constant if
     *  the declared type of all of its fields are constant.
     *  @return True if this type is a constant.
     */
    @Override
    public boolean isConstant() {
        // Loop through all of the fields.
        Iterator fieldTypes = _fields.values().iterator();

        while (fieldTypes.hasNext()) {
            FieldType fieldType = (FieldType) fieldTypes.next();
            Type type = fieldType._declaredType;

            // Return false if the field is not constant.
            if (!type.isConstant()) {
                return false;
            }
        }

        return true;
    }

    /** Test if this type corresponds to an instantiable token
     *  class. A UnionType is instantiable if all of its fields are
     *  instantiable.
     *  @return True if this type is instantiable.
     */
    @Override
    public boolean isInstantiable() {
        // Loop through all of the fields.
        Iterator fieldNames = _fields.keySet().iterator();

        while (fieldNames.hasNext()) {
            String label = (String) fieldNames.next();
            Type type = this.get(label);

            // Return false if the field is not instantiable.
            if (!type.isInstantiable()) {
                return false;
            }
        }

        return true;
    }

    /** Test if the specified type is a substitution instance of this
     *  type.  One union is a substitution instance of another if they
     *  have fields with the same names and each field of the given type is
     *  a substitution instance of the corresponding field in this type.
     *  @param type A Type.
     *  @return True if the argument is a substitution instance of this type.
     *  @see Type#isSubstitutionInstance
     */
    @Override
    public boolean isSubstitutionInstance(Type type) {
        if (!(type instanceof UnionType)) {
            return false;
        }

        UnionType unionType = (UnionType) type;

        // Check if this union type and the argument have the same
        // label set.
        Set myLabelSet = _fields.keySet();
        Set argLabelSet = unionType._fields.keySet();

        if (!myLabelSet.equals(argLabelSet)) {
            return false;
        }

        // Loop over all the labels.
        for (Map.Entry<String, FieldType> fields: _fields.entrySet()) {
            FieldType fieldType = fields.getValue();

            Type myDeclaredType = fieldType._declaredType;
            Type argType = unionType.get(fields.getKey());

            if (!myDeclaredType.isSubstitutionInstance(argType)) {
                return false;
            }
        }

        return true;
    }

    /** Return the labels of this record type as a Set.
     *  @return A Set containing strings.
     */
    public Set labelSet() {
        return _fields.keySet();
    }

    /** Return the string representation of this type. The format is
     *  {|<i>label</i> = <i>type</i>, <i>label</i> = <i>type</i>, ...|}.
     *  The record fields are listed in the lexicographical order of the
     *  labels determined by the java.lang.String.compareTo() method.
     *  @return A String.
     */
    @Override
    public String toString() {
        Object[] labelArray = _fields.keySet().toArray();

        // Order the labels
        int size = labelArray.length;

        for (int i = 0; i < size - 1; i++) {
            for (int j = i + 1; j < size; j++) {
                String labeli = (String) labelArray[i];
                String labelj = (String) labelArray[j];

                if (labeli.compareTo(labelj) >= 0) {
                    Object temp = labelArray[i];
                    labelArray[i] = labelArray[j];
                    labelArray[j] = temp;
                }
            }
        }

        // construct the string representation of this token.
        StringBuffer results = new StringBuffer("{|");

        for (int i = 0; i < size; i++) {
            String label = (String) labelArray[i];
            String type = this.get(label).toString();

            if (i != 0) {
                results.append(", ");
            }

            results.append(label + " = " + type);
        }

        return results.toString() + "|}";
    }

    /** Update this Type to the specified UnionType.
     *  The specified type must be a UnionType and have the same structure
     *  as this one.
     *  This method will only update the component whose declared type is
     *  BaseType.UNKNOWN, and leave the constant part of this type intact.
     *  @param newType A StructuredType.
     *  @exception IllegalActionException If the specified type is not a
     *   UnionType or it does not have the same structure as this one.
     */
    @Override
    public void updateType(StructuredType newType)
            throws IllegalActionException {
        if (this.isConstant()) {
            if (this.equals(newType)) {
                return;
            }

            throw new IllegalActionException("UnionType.updateType: "
                    + "This type is a constant and the argument is not the"
                    + " same as this type. This type: " + this.toString()
                    + " argument: " + newType.toString());
        }

        // This type is a variable.
        if (!this.isSubstitutionInstance(newType)) {
            throw new IllegalActionException("UnionType.updateType: "
                    + "Cannot update this type to the new type.");
        }

        for (Map.Entry<String, FieldType> fields: _fields.entrySet()) {
            FieldType fieldType = fields.getValue();
            if (fieldType.isSettable()) {
                //Type newFieldType = ((UnionType) newType).get(label);
                Type newFieldType = ((UnionType) newType).get(fields.getKey());
                fieldType.setValue(newFieldType);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Compare this type with the specified type. The specified type
     *  must be a UnionType, otherwise an exception will be thrown.
     *
     *  This method returns one of ptolemy.graph.CPO.LOWER,
     *  ptolemy.graph.CPO.SAME, ptolemy.graph.CPO.HIGHER,
     *  ptolemy.graph.CPO.INCOMPARABLE, indicating this type is lower
     *  than, equal to, higher than, or incomparable with the
     *  specified type in the type hierarchy, respectively.
     *  @param type a UnionType.
     *  @return An integer.
     *  @exception IllegalArgumentException If the specified type is
     *   not a UnionType.
     */
    @Override
    protected int _compare(StructuredType type) {
        if (!(type instanceof UnionType)) {
            throw new IllegalArgumentException("UnionType._compare: "
                    + "The argument is not a UnionType.");
        }

        if (this.equals(type)) {
            return CPO.SAME;
        }

        if (_isLessThanOrEqualTo(this, (UnionType) type)) {
            return CPO.LOWER;
        }

        if (_isLessThanOrEqualTo((UnionType) type, this)) {
            return CPO.HIGHER;
        }

        return CPO.INCOMPARABLE;
    }

    /** Return a static instance of RecordType.
     *  @return a UnionType.
     */
    @Override
    protected StructuredType _getRepresentative() {
        return _representative;
    }

    /** Return the greatest lower bound of this type with the specified
     *  type. The specified type must be a UnionType, otherwise an
     *  exception will be thrown.
     *  @param type a UnionType.
     *  @return a UnionType.
     *  @exception IllegalArgumentException If the specified type is
     *   not a UnionType.
     */
    @Override
    protected StructuredType _greatestLowerBound(StructuredType type) {
        if (!(type instanceof UnionType)) {
            throw new IllegalArgumentException(
                    "UnionType.greatestLowerBound: The argument is not a "
                            + "UnionType.");
        }

        UnionType unionType = (UnionType) type;

        // the label set of the GLB is the intersection of the two label sets.
        Set intersectionSet = new HashSet();
        Set myLabelSet = _fields.keySet();
        Set argLabelSet = unionType._fields.keySet();

        intersectionSet.addAll(myLabelSet);
        intersectionSet.retainAll(argLabelSet);

        // construct the GLB UnionToken
        Object[] labelArray = intersectionSet.toArray();
        int size = labelArray.length;
        String[] labels = new String[size];
        Type[] types = new Type[size];

        for (int i = 0; i < size; i++) {
            labels[i] = (String) labelArray[i];

            Type type1 = this.get(labels[i]);
            Type type2 = unionType.get(labels[i]);
            types[i] = (Type) TypeLattice.lattice().greatestLowerBound(type1,
                    type2);
        }

        return new UnionType(labels, types);
    }

    /** Return the least Upper bound of this type with the specified
     *  type. The specified type must be a UnionType, otherwise an
     *  exception will be thrown.
     *  @param type a UnionType.
     *  @return a UnionType.
     *  @exception IllegalArgumentException If the specified type is
     *   not a UnionType.
     */
    @Override
    protected StructuredType _leastUpperBound(StructuredType type) {
        if (!(type instanceof UnionType)) {
            throw new IllegalArgumentException("UnionType.leastUpperBound: "
                    + "The argument is not a UnionType.");
        }

        UnionType unionType = (UnionType) type;

        // the label set of the LUB is the union of the two label sets.
        Set unionSet = new HashSet();
        Set myLabelSet = _fields.keySet();
        Set argLabelSet = unionType._fields.keySet();

        unionSet.addAll(myLabelSet);
        unionSet.addAll(argLabelSet);

        // construct the GLB UnionToken
        Object[] labelArray = unionSet.toArray();
        int size = labelArray.length;
        String[] labels = new String[size];
        Type[] types = new Type[size];

        for (int i = 0; i < size; i++) {
            labels[i] = (String) labelArray[i];

            Type type1 = this.get(labels[i]);
            Type type2 = unionType.get(labels[i]);

            if (type1 == null) {
                types[i] = type2;
            } else if (type2 == null) {
                types[i] = type1;
            } else {
                types[i] = (Type) TypeLattice.lattice().leastUpperBound(type1,
                        type2);
            }
        }

        return new UnionType(labels, types);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // Test if the first UnionType is less than or equal to the second
    private boolean _isLessThanOrEqualTo(UnionType t1, UnionType t2) {
        Set labelSet1 = t1._fields.keySet();
        Set labelSet2 = t2._fields.keySet();

        if (!labelSet2.containsAll(labelSet1)) {
            return false;
        }

        // iterate over the labels of the first type
        Iterator iter = labelSet1.iterator();

        while (iter.hasNext()) {
            String label = (String) iter.next();
            Type type1 = t1.get(label);
            Type type2 = t2.get(label);
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
    private Map<String, FieldType> _fields = new HashMap<String, FieldType>();

    // the representative in the type lattice is the empty record.
    private static UnionType _representative = new UnionType(new String[0],
            new Type[0]);

    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////
    // A class that encapsulates the declared and resolved types of a
    // field and implements the InequalityTerm interface.
    private class FieldType implements InequalityTerm {
        // Construct an instance of FieldType.
        private FieldType(Type declaredType) {
            try {
                _declaredType = (Type) declaredType.clone();
                _resolvedType = _declaredType;
            } catch (CloneNotSupportedException cnse) {
                throw new InternalErrorException("UnionType.FieldType: "
                        + "The specified type cannot be cloned.");
            }
        }

        ///////////////////////////////////////////////////////////////
        ////                   public inner methods                ////

        /** Return this UnionType.
         *  @return a UnionType.
         */
        @Override
        public Object getAssociatedObject() {
            return UnionType.this;
        }

        /** Return the resolved type.
         *  @return a Type.
         */
        @Override
        public Object getValue() {
            return _resolvedType;
        }

        /** Return this FieldType in an array if it represents a type
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
                throw new IllegalActionException("UnionType$FieldType."
                        + "initialize: The type is not settable.");
            }

            if (!(e instanceof Type)) {
                throw new IllegalActionException("FieldType.initialize: "
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
                        "UnionType$FieldType.setValue: The type is not "
                                + "settable.");
            }

            if (!_declaredType.isSubstitutionInstance((Type) e)) {
                throw new IllegalActionException("FieldType.setValue: "
                        + "Cannot update the field type of this UnionType "
                        + "to the new type." + " Field type: "
                        + _declaredType.toString() + ", New type: "
                        + e.toString());
            }

            if (_declaredType == BaseType.UNKNOWN) {
                try {
                    _resolvedType = (Type) ((Type) e).clone();
                } catch (CloneNotSupportedException cnse) {
                    throw new InternalErrorException(
                            "UnionType$FieldType.setValue: "
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
            return "(UnionFieldType, " + getValue() + ")";
        }

        ///////////////////////////////////////////////////////////////
        ////                  private inner variables              ////
        private Type _declaredType = null;

        private Type _resolvedType = null;
    }
}
