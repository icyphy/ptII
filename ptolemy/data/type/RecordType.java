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

import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.graph.CPO;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

//////////////////////////////////////////////////////////////////////////
//// RecordType
/**
A class representing the type of a RecordToken.
To set the type of a typeable object (such as a port or parameter)
to a record with particular fields, create an instance of this
class and call setTypeEquals() with that instance as an argument.
<p>
Note that a record type with more fields is a subtype of a record
type with a subset of the fields.  For example, {x = double, y = int}
is a subtype of {x = double}. When a record of type
{x = double, y = int} is converted to one of type {x = double},
the extra field is discarded. The converted record, therefore,
will have exactly the fields in the type.
<p>
A consequence of this is that all record types are subtypes
of the empty record type. Hence, to require that a typeable
object be a record type without specifying what the fields
are, use
<pre>
    typeable.setTypeAtMost(new RecordType(new String[0], new Type[0]));
</pre>
Note, however, that by itself this type constraint will
not be useful because it does not, by itself, prevent the
type from resolving to unknown (the unknown type is at the
bottom of the type lattice, and hence satisfies this type
constraint).

@author Yuhong Xiong, Elaine Cheong and Steve Neuendorffer
$Id$
*/

public class RecordType extends StructuredType {

    /** Construct a new RecordType with the specified labels and types.
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
    public RecordType(String[] labels, Type[] types) {
        if (labels.length != types.length) {
            throw new IllegalArgumentException("RecordType: the labels " +
                    "and types arrays do not have the same size.");
        }

        for (int i = 0; i < labels.length; i++) {
            FieldType fieldType = new FieldType(types[i]);
            _fields.put(labels[i], fieldType);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a deep copy of this RecordType if it is a variable, or
     *  itself if it is a constant.
     *  @return A RecordType.
     */
    public Object clone() {
        if (isConstant()) {
            return this;
        } else {
            // empty record is a constant, so this record type is not empty.

            // construct the labels and declared types array
            Object[] labelsObj = _fields.keySet().toArray();
            String[] labels = new String[labelsObj.length];
            Type[] types = new Type[labelsObj.length];
            for (int i = 0; i < labels.length; i++) {
                labels[i] = (String)labelsObj[i];
                FieldType fieldType = (FieldType)_fields.get(labels[i]);
                types[i] = fieldType._declaredType;
            }
            RecordType newObj = new RecordType(labels, types);
            try {
                newObj.updateType(this);
            } catch (IllegalActionException ex) {
                throw new InternalErrorException("RecordType.clone: Cannot " +
                        "update new instance. " + ex.getMessage());
            }
            return newObj;
        }
    }

    /** Convert the argument token into a RecordToken having this
     *  type, if lossless conversion can be done.  The argument must
     *  be an RecordToken, and its type must be a subtype of this
     *  record type.  The argument token must have at least the fields
     *  of this type.  Extra fields in the argument token that are not
     *  in this type are removed.
     *  @param token A token.
     *  @return An RecordToken.
     *  @exception IllegalActionException If lossless conversion
     *   cannot be done.
     */
    public Token convert(Token token) throws IllegalActionException {
        if ( !isCompatible(token.getType())) {
            throw new IllegalArgumentException(
                    Token.notSupportedConversionMessage(
                            token, this.toString()));
        }

        RecordToken recordToken = (RecordToken)token;
        // The converted token has the same set of labels as this type.
        Object[] labelArray = labelSet().toArray();

        // Arrays that will be used to create the new token.
        String[] labelStringArray = new String[labelArray.length];
        Token[] values = new Token[labelArray.length];

        for (int i = 0; i < labelArray.length; i++) {
            String label = (String)labelArray[i];

            // Convert each field of the record.
            Token fieldToken = recordToken.get(label);
            Type newFieldType = get(label);

            // If the type of the field is specified, then convert it.
            values[i] = newFieldType.convert(fieldToken);

            // Store the label for each field.
            labelStringArray[i] = label;
        }

        return new RecordToken(labelStringArray, values);
    }

    /** Determine if the argument represents the same RecordType as this
     *  object.  Two record types are equal if they have the same field names
     *  and the type of each field is the same.
     *  @param object Another object.
     *  @return True if the argument represents the same RecordType as
     *  this object.
     */
    public boolean equals(Object object) {
        if (!(object instanceof RecordType)) {
            return false;
        }

        RecordType recordType = (RecordType)object;

        // Check that the label sets are equal
        Set myLabelSet = _fields.keySet();
        Set argLabelSet = recordType._fields.keySet();
        if (!myLabelSet.equals(argLabelSet)) {
            return false;
        }

        Iterator fieldNames = myLabelSet.iterator();
        while (fieldNames.hasNext()) {
            String label = (String)fieldNames.next();
            Type myType = this.get(label);
            Type argType = recordType.get(label);
            if (!myType.equals(argType)) {
                return false;
            }
        }

        return true;
    }

    /** Return the type of the specified label. If this type does not
     *  contain the specified label, return null.
     *  @return a Type.
     */
    public Type get(String label) {
        FieldType fieldType = (FieldType)_fields.get(label);
        if (fieldType == null) {
            return null;
        }
        return fieldType._resolvedType;
    }

    /** Return the class for tokens that this type represents.
     */
    public Class getTokenClass() {
        return RecordToken.class;
    }

    /** Return the InequalityTerm representing the type of the specified
     *  label.
     *  @return An InequalityTerm.
     *  @see ptolemy.graph.InequalityTerm
     */
    public InequalityTerm getTypeTerm(String label) {
        return (InequalityTerm)_fields.get(label);
    }

    /** Return a hash code value for this object.
     */
    public int hashCode() {
        return _fields.keySet().hashCode() + 2917;
    }

    /** Set the elements that have declared type BaseType.UNKNOWN (the leaf
     *  type variable) to the specified type.
     *  @param type the type to set the leaf type variable to.
     */
    public void initialize(Type type) {
        try {
            Iterator fieldNames = _fields.keySet().iterator();
            while (fieldNames.hasNext()) {
                String label = (String)fieldNames.next();
                FieldType fieldType = (FieldType)_fields.get(label);
                if (fieldType.isSettable()) {
                    fieldType.initialize(type);
                }
            }
        } catch (IllegalActionException iae) {
            throw new InternalErrorException("RecordType.initialize: Cannot " +
                    "initialize the element type to " + type + " " +
                    iae.getMessage());
        }
    }

    /** Test if the argument type is compatible with this type.  The
     *  given type will be compatible with this type if it is
     *  BaseType.UNKNOWN, or a RecordType that contains at least as
     *  many fields.
     *  @param type An instance of Type.
     *  @return True if the argument is compatible with this type.
     */
    public boolean isCompatible(Type type) {
        if (type.equals(BaseType.UNKNOWN)) {
            return true;
        }

        if ( !(type instanceof RecordType)) {
            return false;
        }

        RecordType argumentRecordType = (RecordType)type;

        // Loop through all of the fields of this type...
        Iterator iterator = _fields.keySet().iterator();
        while (iterator.hasNext()) {
            String label = (String)iterator.next();

            // The given type cannot be losslessly converted to this type
            // if it does not contain one of the fields of this type.
            Type argumentFieldType = argumentRecordType.get(label);
            if (argumentFieldType == null) {
                // argument token does not contain this label
                return false;
            }

            // The given type cannot be losslessly converted to this type
            // if the individual fields are not compatible.
            Type thisFieldType = this.get(label);
            if (!thisFieldType.isCompatible(argumentFieldType)) {
                return false;
            }
        }

        return true;
    }

    /** Test if this RecordType is a constant. A RecordType is a constant if
     *  the declared type of all of its fields are constant.
     *  @return True if this type is a constant.
     */
    public boolean isConstant() {
        // Loop through all of the fields.
        Iterator fieldTypes = _fields.values().iterator();
        while (fieldTypes.hasNext()) {
            FieldType fieldType = (FieldType)fieldTypes.next();
            Type type = fieldType._declaredType;
            // Return false if the field is not constant.
            if (!type.isConstant()) {
                return false;
            }
        }
        return true;
    }

    /** Test if this type corresponds to an instantiable token
     *  class. A RecordType is instantiable if all of its fields are
     *  instantiable.
     *  @return True if this type is instantiable.
     */
    public boolean isInstantiable() {
        // Loop through all of the fields.
        Iterator fieldNames = _fields.keySet().iterator();
        while (fieldNames.hasNext()) {
            String label = (String)fieldNames.next();
            Type type = this.get(label);
            // Return false if the field is not instantiable.
            if (!type.isInstantiable()) {
                return false;
            }
        }
        return true;
    }

    /** Test if the specified type is a substitution instance of this
     *  type.  One record is a substitution instance of another if they
     *  have fields with the same names and each field of the given type is
     *  a substitution instance of the corresponding field in this type.
     *  @param type A Type.
     *  @return True if the argument is a substitution instance of this type.
     *  @see Type#isSubstitutionInstance
     */
    public boolean isSubstitutionInstance(Type type) {
        if ( !(type instanceof RecordType)) {
            return false;
        }

        RecordType recordType = (RecordType)type;

        // Check if this record type and the argument have the same
        // label set.
        Set myLabelSet = _fields.keySet();
        Set argLabelSet = recordType._fields.keySet();
        if (!myLabelSet.equals(argLabelSet)) {
            return false;
        }

        // Loop over all the labels.
        Iterator fieldNames = myLabelSet.iterator();
        while (fieldNames.hasNext()) {
            String label = (String)fieldNames.next();

            FieldType fieldType = (FieldType)_fields.get(label);
            Type myDeclaredType = fieldType._declaredType;
            Type argType = recordType.get(label);

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
     *  {<i>label</i> = <i>type</i>, <i>label</i> = <i>type</i>, ...}.
     *  The record fields are listed in the lexicographical order of the
     *  labels determined by the java.lang.String.compareTo() method.
     *  @return A String.
     */
    public String toString() {
        Object[] labelArray = _fields.keySet().toArray();
        // Order the labels
        int size = labelArray.length;
        for (int i = 0; i < size-1; i++) {
            for (int j = i + 1; j < size; j++) {
                String labeli = (String)labelArray[i];
                String labelj = (String)labelArray[j];
                if (labeli.compareTo(labelj) >= 0) {
                    Object temp = labelArray[i];
                    labelArray[i] = labelArray[j];
                    labelArray[j] = temp;
                }
            }
        }

        // construct the string representation of this token.
        String s = "{";
        for (int i = 0; i < size; i++) {
            String label = (String)labelArray[i];
            String type = this.get(label).toString();
            if (i != 0) {
                s += ", ";
            }
            s += label + " = " + type;
        }
        return s + "}";
    }

    /** Update this Type to the specified RecordType.
     *  The specified type must be a RecordType and have the same structure
     *  as this one.
     *  This method will only update the component whose declared type is
     *  BaseType.UNKNOWN, and leave the constant part of this type intact.
     *  @param newType A StructuredType.
     *  @exception IllegalActionException If the specified type is not a
     *   RecordType or it does not have the same structure as this one.
     */
    public void updateType(StructuredType newType)
            throws IllegalActionException {
        if (this.isConstant()) {
            if (this.equals(newType)) {
                return;
            } else {
                throw new IllegalActionException("RecordType.updateType: " +
                        "This type is a constant and the argument is not the" +
                        " same as this type. This type: " + this.toString() +
                        " argument: " + newType.toString());
            }
        }

        // This type is a variable.
        if ( !this.isSubstitutionInstance(newType)) {
            throw new IllegalActionException("RecordType.updateType: "
                    + "Cannot update this type to the new type.");
        }

        Iterator fieldNames = _fields.keySet().iterator();
        while (fieldNames.hasNext()) {
            String label = (String)fieldNames.next();
            FieldType fieldType = (FieldType)_fields.get(label);
            if (fieldType.isSettable()) {
                Type newFieldType = ((RecordType)newType).get(label);
                fieldType.setValue(newFieldType);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Compare this type with the specified type. The specified type
     *  must be a RecordType, otherwise an exception will be thrown.
     *
     *  This method returns one of ptolemy.graph.CPO.LOWER,
     *  ptolemy.graph.CPO.SAME, ptolemy.graph.CPO.HIGHER,
     *  ptolemy.graph.CPO.INCOMPARABLE, indicating this type is lower
     *  than, equal to, higher than, or incomparable with the
     *  specified type in the type hierarchy, respectively.
     *  @param type a RecordType.
     *  @return An integer.
     *  @exception IllegalArgumentException If the specified type is
     *   not a RecordType.
     */
    protected int _compare(StructuredType type) {
        if ( !(type instanceof RecordType)) {
            throw new IllegalArgumentException("RecordType.compare: " +
                    "The argument is not a RecordType.");
        }

        if (this.equals(type)) {
            return CPO.SAME;
        }

        if (_isLessThanOrEqualTo(this, (RecordType)type)) {
            return CPO.LOWER;
        }

        if (_isLessThanOrEqualTo((RecordType)type, this)) {
            return CPO.HIGHER;
        }

        return CPO.INCOMPARABLE;
    }

    /** Return a static instance of RecordType.
     *  @return a RecordType.
     */
    protected StructuredType _getRepresentative() {
        return _representative;
    }

    /** Return the greatest lower bound of this type with the specified
     *  type. The specified type must be a RecordType, otherwise an
     *  exception will be thrown.
     *  @param type a RecordType.
     *  @return a RecordType.
     *  @exception IllegalArgumentException If the specified type is
     *   not a RecordType.
     */
    protected StructuredType _greatestLowerBound(StructuredType type) {
        if ( !(type instanceof RecordType)) {
            throw new IllegalArgumentException(
                    "RecordType.greatestLowerBound: The argument is not a " +
                    "RecordType.");
        }

        RecordType recordType = (RecordType)type;

        // the label set of the GLB is the union of the two label sets.
        Set unionSet = new HashSet();
        Set myLabelSet = _fields.keySet();
        Set argLabelSet = recordType._fields.keySet();

        unionSet.addAll(myLabelSet);
        unionSet.addAll(argLabelSet);

        // construct the GLB RecordToken
        Object[] labelArray = unionSet.toArray();
        int size = labelArray.length;
        String[] labels = new String[size];
        Type[] types = new Type[size];

        for (int i = 0; i < size; i++) {
            labels[i] = (String)labelArray[i];
            Type type1 = this.get(labels[i]);
            Type type2 = recordType.get(labels[i]);
            if (type1 == null) {
                types[i] = type2;
            } else if (type2 == null) {
                types[i] = type1;
            } else {
                types[i] = (Type)TypeLattice.lattice().greatestLowerBound(
                        type1, type2);
            }
        }

        return new RecordType(labels, types);
    }

    /** Return the least Upper bound of this type with the specified
     *  type. The specified type must be a RecordType, otherwise an
     *  exception will be thrown.
     *  @param type a RecordType.
     *  @return a RecordType.
     *  @exception IllegalArgumentException If the specified type is
     *   not a RecordType.
     */
    protected StructuredType _leastUpperBound(StructuredType type) {
        if ( !(type instanceof RecordType)) {
            throw new IllegalArgumentException("RecordType.leastUpperBound: "
                    + "The argument is not a RecordType.");
        }

        RecordType recordType = (RecordType)type;

        // the label set of the LUB is the intersection of the two label sets.
        Set intersectionSet = new HashSet();
        Set myLabelSet = _fields.keySet();
        Set argLabelSet = recordType._fields.keySet();

        intersectionSet.addAll(myLabelSet);
        intersectionSet.retainAll(argLabelSet);

        // construct the GLB RecordToken
        Object[] labelArray = intersectionSet.toArray();
        int size = labelArray.length;
        String[] labels = new String[size];
        Type[] types = new Type[size];
        for (int i = 0; i < size; i++) {
            labels[i] = (String)labelArray[i];
            Type type1 = this.get(labels[i]);
            Type type2 = recordType.get(labels[i]);
            types[i] = (Type)TypeLattice.lattice().leastUpperBound(
                    type1, type2);
        }

        return new RecordType(labels, types);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Test if the first RecordType is less than or equal to the second
    private boolean _isLessThanOrEqualTo(RecordType t1, RecordType t2) {
        Set labelSet1 = t1._fields.keySet();
        Set labelSet2 = t2._fields.keySet();
        if (!labelSet1.containsAll(labelSet2)) {
            return false;
        }

        // iterate over the labels of the second type
        Iterator iter = labelSet2.iterator();
        while (iter.hasNext()) {
            String label = (String)iter.next();
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
    private Map _fields = new HashMap();

    // the representative in the type lattice is the empty record.
    private static RecordType _representative =
    new RecordType(new String[0], new Type[0]);

    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////

    // A class that encapsulates the declared and resolved types of a
    // field and implements the InequalityTerm interface.
    private class FieldType implements InequalityTerm {

        // Construct an instance of FieldType.
        private FieldType(Type declaredType) {
            try {
                _declaredType = (Type)declaredType.clone();
                _resolvedType = _declaredType;
            } catch (CloneNotSupportedException cnse) {
                throw new InternalErrorException("RecordType.FieldType: " +
                        "The specified type cannot be cloned.");
            }
        }

        ///////////////////////////////////////////////////////////////
        ////                   public inner methods                ////

        /** Return this RecordType.
         *  @return a RecordType.
         */
        public Object getAssociatedObject() {
            return RecordType.this;
        }

        /** Return the resolved type.
         *  @return a Type.
         */
        public Object getValue() {
            return _resolvedType;
        }

        /** Return this FieldType in an array if it represents a type
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
                throw new IllegalActionException("RecordType$FieldType." +
                        "initialize: The type is not settable.");
            }

            if (!(e instanceof Type)) {
                throw new IllegalActionException("FieldType.initialize: "
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
                        "RecordType$FieldType.setValue: The type is not " +
                        "settable.");
            }

            if ( !_declaredType.isSubstitutionInstance((Type)e)) {
                throw new IllegalActionException("FieldType.setValue: "
                        + "Cannot update the field type of this RecordType "
                        + "to the new type."
                        + " Field type: " + _declaredType.toString()
                        + ", New type: " + e.toString());
            }

            if (_declaredType == BaseType.UNKNOWN) {
                try {
                    _resolvedType = (Type)((Type)e).clone();
                } catch (CloneNotSupportedException cnse) {
                    throw new InternalErrorException(
                            "RecordType$FieldType.setValue: " +
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
            return "(RecordFieldType, " + getValue() + ")";
        }

        ///////////////////////////////////////////////////////////////
        ////                  private inner variables              ////

        private Type _declaredType = null;
        private Type _resolvedType = null;
    }
}

