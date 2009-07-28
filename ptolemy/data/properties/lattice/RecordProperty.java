/**
 * A class representing the record property.
 * 
 * Copyright (c) 2008-2009 The Regents of the University of California. All
 * rights reserved. Permission is hereby granted, without written agreement and
 * without license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the above
 * copyright notice and the following two paragraphs appear in all copies of
 * this software.
 * 
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
 * CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN
 * "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE
 * MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 * 
 * PT_COPYRIGHT_VERSION_2 COPYRIGHTENDKEY
 */
package ptolemy.data.properties.lattice;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import ptolemy.data.RecordToken;
import ptolemy.data.properties.Property;
import ptolemy.graph.CPO;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;

//////////////////////////////////////////////////////////////////////////
//// RecordProperty

/**
 * A class representing the property of a RecordToken. To set the property of a
 * propertyable object (such as a port or parameter) to a record with particular
 * fields, create an instance of this class and call setEquals() with that
 * instance as an argument.
 * <p>
 * Note that a record property with more fields is a subproperty of a record
 * property with a subset of the fields. For example, {x = double, y = int} is a
 * subproperty of {x = double}. When a record of property {x = double, y = int}
 * is converted to one of property {x = double}, the extra field is discarded.
 * The converted record, therefore, will have exactly the fields in the
 * property.
 * <p>
 * A consequence of this is that all record properties are subproperties of the
 * empty record property. Hence, to require that a propertyable object be a
 * record property without specifying what the fields are, use
 * 
 * <pre>
 * propertyable.setAtMost(new RecordProperty(new String[0], new Property[0]));
 * </pre>
 * 
 * Note, however, that by itself this property constraint will not be useful
 * because it does not, by itself, prevent the property from resolving to
 * unknown (the unknown property is at the bottom of the property lattice, and
 * hence satisfies this property constraint).
 * 
 * @author Man-Kit Leung
 * @version $Id: RecordProperty.java 49948 2008-06-24 20:46:43Z eal $
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
public class RecordProperty extends StructuredProperty implements Cloneable {
    /**
     * Construct a RecordProperty with the labels and values specified by a
     * given Map object. The object cannot contain any null keys or values.
     * @param lattice The specified lattice where this property resides.
     * @param fieldMap A Map that has keys of property String and values of
     * property Token.
     * @exception IllegalActionException If the map contains null keys or
     * values, or if it contains non-String keys or non-Property values
     */
    public RecordProperty(PropertyLattice lattice, Map fieldMap)
            throws IllegalActionException {
        super(lattice);

        Iterator fields = fieldMap.entrySet().iterator();
        while (fields.hasNext()) {
            Map.Entry entry = (Map.Entry) fields.next();
            if (entry.getKey() == null || entry.getValue() == null) {
                throw new IllegalActionException("RecordProperty: given map"
                        + " contains either null keys or null values.");
            }
            if (!(entry.getKey() instanceof String)
                    || !(entry.getValue() instanceof Property)) {
                throw new IllegalActionException("RecordProperty: given map"
                        + " contains either non-String keys or"
                        + " non-Property values.");
            }
            _fields.put(entry.getKey(), new FieldProperty(
                    (LatticeProperty) entry.getValue()));
        }
    }

    /**
     * Construct a new RecordProperty with the specified labels and properties.
     * To leave the properties of some fields undeclared, use
     * BaseProperty.UNKNOWN. The labels and the properties are specified in two
     * arrays. These two arrays must have the same length, and their elements
     * have one to one correspondence. That is, the i'th entry in the properties
     * array is the property for the i'th label in the labels array. To
     * construct the empty record property, set the length of the argument
     * arrays to 0.
     * @param lattice The specified lattice where this property resides.
     * @param labels An array of String.
     * @param properties An array of Property.
     * @exception IllegalArgumentException If the two arrays do not have the
     * same size.
     * @exception NullPointerException If one of the arguments is null.
     */
    public RecordProperty(PropertyLattice lattice, String[] labels,
            LatticeProperty[] properties) {
        super(lattice);
        if (labels.length != properties.length) {
            throw new IllegalArgumentException("RecordProperty: the labels "
                    + "and properties arrays do not have the same size.");
        }

        for (int i = 0; i < labels.length; i++) {
            FieldProperty fieldProperty = new FieldProperty(properties[i]);
            _fields.put(labels[i], fieldProperty);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Return a deep copy of this RecordProperty if it is a variable, or itself
     * if it is a constant.
     * @return A RecordProperty.
     */
    @Override
    public Object clone() {
        if (isConstant()) {
            return this;
        } else {
            // empty record is a constant, so this record property is not empty.
            // construct the labels and declared properties array
            Object[] labelsObj = _fields.keySet().toArray();
            String[] labels = new String[labelsObj.length];
            LatticeProperty[] properties = new LatticeProperty[labelsObj.length];

            for (int i = 0; i < labels.length; i++) {
                labels[i] = (String) labelsObj[i];

                FieldProperty fieldProperty = (FieldProperty) _fields
                        .get(labels[i]);
                properties[i] = fieldProperty._declaredProperty;
            }

            RecordProperty newObj = new RecordProperty(_lattice, labels,
                    properties);

            try {
                newObj.updateProperty(this);
            } catch (IllegalActionException ex) {
                throw new InternalErrorException(
                        "RecordProperty.clone: Cannot "
                                + "update new instance. " + ex.getMessage());
            }

            return newObj;
        }
    }

    /**
     * Return the depth of a record property. The depth of a record property is
     * the number of times it contains other structured properties. For example,
     * a record of arrays has depth 2.
     * @return the depth of a record property.
     */
    @Override
    public int depth() {
        Object[] labelsObj = _fields.keySet().toArray();
        String[] labels = new String[labelsObj.length];
        int[] depth = new int[labelsObj.length];
        int maxDepth = 1;
        for (int i = 0; i < labels.length; i++) {
            labels[i] = (String) labelsObj[i];
            Property fieldProperty = get(labels[i]);
            depth[i] = 1;
            if (fieldProperty instanceof StructuredProperty) {
                depth[i] += ((StructuredProperty) fieldProperty).depth();
            }
            if (depth[i] > maxDepth) {
                maxDepth = depth[i];
            }
        }
        return maxDepth;
    }

    /**
     * Determine if the argument represents the same RecordProperty as this
     * object. Two record properties are equal if they have the same field names
     * and the property of each field is the same.
     * @param object Another object.
     * @return True if the argument represents the same RecordProperty as this
     * object.
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof RecordProperty)) {
            return false;
        }

        RecordProperty RecordProperty = (RecordProperty) object;

        // Check that the label sets are equal
        Set myLabelSet = _fields.keySet();
        Set argLabelSet = RecordProperty._fields.keySet();

        if (!myLabelSet.equals(argLabelSet)) {
            return false;
        }

        Iterator fieldNames = myLabelSet.iterator();

        while (fieldNames.hasNext()) {
            String label = (String) fieldNames.next();
            Property myProperty = get(label);
            Property argProperty = RecordProperty.get(label);

            if (!myProperty.equals(argProperty)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Return the property of the specified label. If this property does not
     * contain the specified label, return null.
     * @param label The specified label.
     * @return a Property.
     */
    public LatticeProperty get(String label) {
        FieldProperty fieldProperty = (FieldProperty) _fields.get(label);

        if (fieldProperty == null) {
            return null;
        }

        return fieldProperty._resolvedProperty;
    }

    /**
     * Return the PropertyTerm representing the property of the specified label.
     * @param label The specified label.
     * @return An PropertyTerm.
     */
    public PropertyTerm getPropertyTerm(String label) {
        return (PropertyTerm) _fields.get(label);
    }

    /**
     * Return a static instance of RecordProperty.
     * @return a RecordProperty.
     */
    public StructuredProperty getRepresentative() {
        Object key = _lattice.getName();
        if (!_representativeMap.containsKey(key)) {
            _representativeMap.put(key, new RecordProperty(_lattice,
                    new String[0], new LatticeProperty[0]));
        }
        return _representativeMap.get(key);
    }

    /**
     * Return the class for tokens that this property represents.
     * @return The class for tokens that this property represents.
     */
    public Class getTokenClass() {
        return RecordToken.class;
    }

    /**
     * Return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return _fields.keySet().hashCode() + 2917;
    }

    /**
     * Set the elements that have declared property BaseProperty.UNKNOWN (the
     * leaf property variable) to the specified property.
     * @param property the property to set the leaf property variable to.
     */
    @Override
    public void initialize(Property property) {
        try {
            Iterator fieldNames = _fields.keySet().iterator();

            while (fieldNames.hasNext()) {
                String label = (String) fieldNames.next();
                FieldProperty fieldProperty = (FieldProperty) _fields
                        .get(label);

                if (fieldProperty.isSettable()) {
                    fieldProperty.initialize(property);
                }
            }
        } catch (IllegalActionException iae) {
            throw new InternalErrorException(
                    "RecordProperty.initialize: Cannot "
                            + "initialize the element property to " + property
                            + " " + iae.getMessage());
        }
    }

    /**
     * Return true if this property does not correspond to a single token class.
     * This occurs if the property is not instantiable, or it represents either
     * an abstract base class or an interface.
     * @return true if the property of any field is abstract.
     */
    @Override
    public boolean isAbstract() {
        // Loop through all of the fields.
        Iterator fieldNames = _fields.keySet().iterator();

        while (fieldNames.hasNext()) {
            String label = (String) fieldNames.next();
            LatticeProperty property = get(label);

            // Return false if the field is not instantiable.
            if (property.isAbstract()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Test if the argument property is compatible with this property. The given
     * property will be compatible with this property if it is
     * BaseProperty.UNKNOWN, or a RecordProperty that contains at least as many
     * fields.
     * @param property An instance of Property.
     * @return True if the argument is compatible with this property.
     */
    @Override
    public boolean isCompatible(Property property) {
        if (!(property instanceof RecordProperty)) {
            return false;
        }

        RecordProperty argumentRecordProperty = (RecordProperty) property;

        // Loop through all of the fields of this property...
        Iterator iterator = _fields.keySet().iterator();

        while (iterator.hasNext()) {
            String label = (String) iterator.next();

            // The given property cannot be losslessly converted to this property
            // if it does not contain one of the fields of this property.
            Property argumentFieldProperty = argumentRecordProperty.get(label);

            if (argumentFieldProperty == null) {
                // argument token does not contain this label
                return false;
            }

            // The given property cannot be losslessly converted to this property
            // if the individual fields are not compatible.
            LatticeProperty thisFieldProperty = get(label);

            if (!thisFieldProperty.isCompatible(argumentFieldProperty)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Test if this RecordProperty is a constant. A RecordProperty is a constant
     * if the declared property of all of its fields are constant.
     * @return True if this property is a constant.
     */
    @Override
    public boolean isConstant() {
        // Loop through all of the fields.
        Iterator fieldProperties = _fields.values().iterator();

        while (fieldProperties.hasNext()) {
            FieldProperty fieldProperty = (FieldProperty) fieldProperties
                    .next();
            LatticeProperty property = fieldProperty._declaredProperty;

            // Return false if the field is not constant.
            if (!property.isConstant()) {
                return false;
            }
        }

        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public fields                     ////

    /**
     * Test if this property corresponds to an instantiable token class. A
     * RecordProperty is instantiable if all of its fields are instantiable.
     * @return True if this property is instantiable.
     */
    @Override
    public boolean isInstantiable() {
        // Loop through all of the fields.
        Iterator fieldNames = _fields.keySet().iterator();

        while (fieldNames.hasNext()) {
            String label = (String) fieldNames.next();
            Property property = get(label);

            // Return false if the field is not instantiable.
            if (!property.isInstantiable()) {
                return false;
            }
        }

        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Test if the specified property is a substitution instance of this
     * property. One record is a substitution instance of another if they have
     * fields with the same names and each field of the given property is a
     * substitution instance of the corresponding field in this property.
     * @param property A Property.
     * @return True if the argument is a substitution instance of this property.
     */
    public boolean isSubstitutionInstance(Property property) {
        if (!(property instanceof RecordProperty)) {
            return false;
        }

        RecordProperty RecordProperty = (RecordProperty) property;

        // Check if this record property and the argument have the same
        // label set.
        Set myLabelSet = _fields.keySet();
        Set argLabelSet = RecordProperty._fields.keySet();

        if (!myLabelSet.equals(argLabelSet)) {
            return false;
        }

        // Loop over all the labels.
        Iterator fieldNames = myLabelSet.iterator();

        while (fieldNames.hasNext()) {
            String label = (String) fieldNames.next();

            FieldProperty fieldProperty = (FieldProperty) _fields.get(label);
            LatticeProperty myDeclaredProperty = fieldProperty._declaredProperty;
            Property argProperty = RecordProperty.get(label);

            if (!myDeclaredProperty.isSubstitutionInstance(argProperty)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Return the labels of this record property as a Set.
     * @return A Set containing strings.
     */
    public Set labelSet() {
        return _fields.keySet();
    }

    /**
     * Return the string representation of this property. The format is {<i>label</i> =
     * <i>property</i>, <i>label</i> = <i>property</i>, ...}. The record
     * fields are listed in the lexicographical order of the labels determined
     * by the java.lang.String.compareTo() method.
     * @return A String.
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
        StringBuffer results = new StringBuffer("{");

        for (int i = 0; i < size; i++) {
            String label = (String) labelArray[i];
            String property = get(label).toString();

            if (i != 0) {
                results.append(", ");
            }

            results.append(label + " = " + property);
        }

        return results.toString() + "}";
    }

    /**
     * Update this Property to the specified RecordProperty. The specified
     * property must be a RecordProperty and have the same structure as this
     * one, and have depth less than the MAXDEPTHDOUND. This method will only
     * update the component whose declared property is BaseProperty.UNKNOWN, and
     * leave the constant part of this property intact.
     * @param newProperty A StructuredProperty.
     * @exception IllegalActionException If the specified property is not a
     * RecordProperty or it does not have the same structure as this one.
     */
    public void updateProperty(StructuredProperty newProperty)
            throws IllegalActionException {
        super.updateProperty(newProperty);
        if (isConstant()) {
            if (equals(newProperty)) {
                return;
            }

            throw new IllegalActionException("RecordProperty.updateProperty: "
                    + "This property is a constant and the argument is not the"
                    + " same as this property. This property: " + toString()
                    + " argument: " + newProperty.toString());
        }

        // This property is a variable.
        if (!isSubstitutionInstance(newProperty)) {
            throw new IllegalActionException("RecordProperty.updateProperty: "
                    + "Cannot update this property to the new property.");
        }

        Iterator fieldNames = _fields.keySet().iterator();

        while (fieldNames.hasNext()) {
            String label = (String) fieldNames.next();
            FieldProperty fieldProperty = (FieldProperty) _fields.get(label);

            if (fieldProperty.isSettable()) {
                Property newFieldProperty = ((RecordProperty) newProperty)
                        .get(label);
                fieldProperty.setValue(newFieldProperty);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////
    // A class that encapsulates the declared and resolved properties of a
    // field and implements the PropertyTerm interface.
    private class FieldProperty implements PropertyTerm {
        // Construct an instance of FieldProperty.
        private FieldProperty(LatticeProperty declaredProperty) {
            try {
                _declaredProperty = (LatticeProperty) declaredProperty.clone();
                _resolvedProperty = _declaredProperty;
            } catch (CloneNotSupportedException cnse) {
                throw new InternalErrorException(
                        "RecordProperty.FieldProperty: "
                                + "The specified property cannot be cloned.");
            }
        }

        ///////////////////////////////////////////////////////////////
        ////                   public inner methods                ////

        /**
         * Return this RecordProperty.
         * @return a RecordProperty.
         */
        public Object getAssociatedObject() {
            return RecordProperty.this;
        }

        /**
         * Return this FieldProperty in an array if it represents a property
         * constant. Otherwise, return an array of size zero.
         * @return An array of PropertyTerm.
         */
        public PropertyTerm[] getConstants() {
            if (!isSettable()) {
                PropertyTerm[] variable = new PropertyTerm[1];
                variable[0] = this;
                return variable;
            }
            return new PropertyTerm[0];
        }

        /**
         * Return the resolved property.
         * @return a Property.
         */
        public Object getValue() {
            return _resolvedProperty;
        }

        /**
         * Return this FieldProperty in an array if it represents a property
         * variable. Otherwise, return an array of size zero.
         * @return An array of PropertyTerm.
         */
        public PropertyTerm[] getVariables() {
            if (isSettable()) {
                PropertyTerm[] variable = new PropertyTerm[1];
                variable[0] = this;
                return variable;
            }

            return new PropertyTerm[0];
        }

        /**
         * Reset the variable part of the element property to the specified
         * property.
         * @param property A Property.
         * @exception IllegalActionException If this property is not settable,
         * or the argument is not a Property.
         */
        public void initialize(Object property) throws IllegalActionException {
            if (!isSettable()) {
                throw new IllegalActionException(
                        "RecordProperty$FieldProperty."
                                + "initialize: The property is not settable.");
            }

            if (!(property instanceof Property)) {
                throw new IllegalActionException("FieldProperty.initialize: "
                        + "The argument is not a Property.");
            }

            // if (_declaredProperty == BaseProperty.UNKNOWN) {
            if (_declaredProperty == _lattice.bottom()) {
                _resolvedProperty = (LatticeProperty) property;
            } else {
                // this field property is a structured property.
                ((StructuredProperty) _resolvedProperty)
                        .initialize((Property) property);
            }
        }

        /**
         * Return true.
         * @return True.
         */
        public boolean isEffective() {
            return true;
        }

        /**
         * Test if this field property is a property variable.
         * @return True if this field property is a property variable.
         */
        public boolean isSettable() {
            return !_declaredProperty.isConstant();
        }

        /**
         * Check whether the current element property is acceptable. The element
         * property is acceptable if it represents an instantiable object.
         * @return True if the element property is acceptable.
         */
        public boolean isValueAcceptable() {
            return _resolvedProperty.isInstantiable();
        }

        /**
         * Do nothing by default.
         * @param isEffective Not used.
         */
        public void setEffective(boolean isEffective) {
        }

        /**
         * Set the element property to the specified property.
         * @param e a Property.
         * @exception IllegalActionException If the specified property violates
         * the declared field property.
         */
        public void setValue(Object e) throws IllegalActionException {
            if (!isSettable()) {
                throw new IllegalActionException(
                        "RecordProperty$FieldProperty.setValue: The property is not "
                                + "settable.");
            }

            if (!_declaredProperty.isSubstitutionInstance((Property) e)) {
                throw new IllegalActionException(
                        "FieldProperty.setValue: "
                                + "Cannot update the field property of this RecordProperty "
                                + "to the new property." + " Field property: "
                                + _declaredProperty.toString()
                                + ", New property: " + e.toString());
            }

            //if (_declaredProperty == BaseProperty.UNKNOWN) {
            if (_declaredProperty == _lattice.bottom()) {
                try {
                    _resolvedProperty = (LatticeProperty) ((LatticeProperty) e)
                            .clone();
                } catch (CloneNotSupportedException cnse) {
                    throw new InternalErrorException(
                            "RecordProperty$FieldProperty.setValue: "
                                    + "The specified property cannot be cloned.");
                }
            } else {
                ((StructuredProperty) _resolvedProperty)
                        .updateProperty((StructuredProperty) e);
            }
        }

        /**
         * Return a string representation of this term.
         * @return A String.
         */
        public String toString() {
            return "(RecordFieldProperty, " + getValue() + ")";
        }

        ///////////////////////////////////////////////////////////////
        ////                  private inner variables              ////
        private LatticeProperty _declaredProperty = null;

        private LatticeProperty _resolvedProperty = null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                      protected methods                    ////    

    /**
     * Compare this property with the specified property. The specified property
     * must be a RecordProperty, otherwise an exception will be thrown.
     * 
     * This method returns one of ptolemy.graph.CPO.LOWER,
     * ptolemy.graph.CPO.SAME, ptolemy.graph.CPO.HIGHER,
     * ptolemy.graph.CPO.INCOMPARABLE, indicating this property is lower than,
     * equal to, higher than, or incomparable with the specified property in the
     * property hierarchy, respectively.
     * @param property a RecordProperty.
     * @return An integer.
     * @exception IllegalArgumentException If the specified property is not a
     * RecordProperty.
     */
    @Override
    protected int _compare(StructuredProperty property) {
        if (!(property instanceof RecordProperty)) {
            throw new IllegalArgumentException("RecordProperty._compare: "
                    + "The argument is not a RecordProperty.");
        }

        if (equals(property)) {
            return CPO.SAME;
        }

        if (_isLessThanOrEqualTo(this, (RecordProperty) property)) {
            return CPO.LOWER;
        }

        if (_isLessThanOrEqualTo((RecordProperty) property, this)) {
            return CPO.HIGHER;
        }

        return CPO.INCOMPARABLE;
    }

    /**
     * Return the greatest lower bound of this property with the specified
     * property. The specified property must be a RecordProperty, otherwise an
     * exception will be thrown.
     * @param property a RecordProperty.
     * @return a RecordProperty.
     * @exception IllegalArgumentException If the specified property is not a
     * RecordProperty.
     */
    @Override
    protected StructuredProperty _greatestLowerBound(StructuredProperty property) {
        // FIXME: we should consider the case where the two RecordProperty
        // are incomparable.

        if (!(property instanceof RecordProperty)) {
            throw new IllegalArgumentException(
                    "RecordProperty.greatestLowerBound: The argument is not a "
                            + "RecordProperty.");
        }

        RecordProperty RecordProperty = (RecordProperty) property;

        // the label set of the GLB is the union of the two label sets.
        Set unionSet = new HashSet();
        Set myLabelSet = _fields.keySet();
        Set argLabelSet = RecordProperty._fields.keySet();

        unionSet.addAll(myLabelSet);
        unionSet.addAll(argLabelSet);

        // construct the GLB RecordToken
        Object[] labelArray = unionSet.toArray();
        int size = labelArray.length;
        String[] labels = new String[size];
        LatticeProperty[] properties = new LatticeProperty[size];

        for (int i = 0; i < size; i++) {
            labels[i] = (String) labelArray[i];

            LatticeProperty property1 = get(labels[i]);
            LatticeProperty property2 = RecordProperty.get(labels[i]);

            if (property1 == null) {
                properties[i] = property2;
            } else if (property2 == null) {
                properties[i] = property1;
            } else {
                properties[i] = (LatticeProperty) _lattice.greatestLowerBound(
                        property1, property2);
            }
        }

        return new RecordProperty(_lattice, labels, properties);
    }

    /**
     * Return the least Upper bound of this property with the specified
     * property. The specified property must be a RecordProperty, otherwise an
     * exception will be thrown.
     * @param property a RecordProperty.
     * @return a RecordProperty.
     * @exception IllegalArgumentException If the specified property is not a
     * RecordProperty.
     */
    protected StructuredProperty _leastUpperBound(StructuredProperty property) {
        // FIXME: we should consider the case where the two RecordProperty
        // are incomparable.

        if (!(property instanceof RecordProperty)) {
            throw new IllegalArgumentException(
                    "RecordProperty.leastUpperBound: "
                            + "The argument is not a RecordProperty.");
        }

        RecordProperty RecordProperty = (RecordProperty) property;

        // the label set of the LUB is the intersection of the two label sets.
        Set intersectionSet = new HashSet();
        Set myLabelSet = _fields.keySet();
        Set argLabelSet = RecordProperty._fields.keySet();

        intersectionSet.addAll(myLabelSet);
        intersectionSet.retainAll(argLabelSet);

        // construct the GLB RecordToken
        Object[] labelArray = intersectionSet.toArray();
        int size = labelArray.length;
        String[] labels = new String[size];
        LatticeProperty[] properties = new LatticeProperty[size];

        for (int i = 0; i < size; i++) {
            labels[i] = (String) labelArray[i];

            LatticeProperty property1 = get(labels[i]);
            LatticeProperty property2 = RecordProperty.get(labels[i]);
            properties[i] = (LatticeProperty) _lattice.leastUpperBound(
                    property1, property2);
        }

        return new RecordProperty(_lattice, labels, properties);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Test if the first RecordProperty is less than or equal to the second
    private boolean _isLessThanOrEqualTo(RecordProperty t1, RecordProperty t2) {
        Set labelSet1 = t1._fields.keySet();
        Set labelSet2 = t2._fields.keySet();

        if (!labelSet1.containsAll(labelSet2)) {
            return false;
        }

        // iterate over the labels of the second property
        Iterator iter = labelSet2.iterator();

        while (iter.hasNext()) {
            String label = (String) iter.next();
            Property property1 = t1.get(label);
            Property property2 = t2.get(label);
            int result = _lattice.compare(property1, property2);

            if (result == CPO.HIGHER || result == CPO.INCOMPARABLE) {
                return false;
            }
        }

        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Mapping from label to field information. */
    private final Map _fields = new HashMap();

    /** Mapping the representative record to lattice. */
    private static Map<Object, RecordProperty> _representativeMap = new HashMap<Object, RecordProperty>();

}
