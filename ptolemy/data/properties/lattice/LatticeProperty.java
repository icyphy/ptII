/**
 * The base class for a lattice property.
 * 
 * Copyright (c) 1997-2009 The Regents of the University of California. All
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
 * 
 * 
 */
package ptolemy.data.properties.lattice;

import ptolemy.data.properties.Property;
import ptolemy.graph.CPO;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// LatticeProperty

/**
 * A base class representing a lattice property. A lattice property is a element
 * in a property lattice. The user should create new sub-classes to represent
 * different elements in a property lattice (See PropertyLattice.java).
 * 
 * @author Thomas Mandl, Man-Kit Leung, Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
public class LatticeProperty extends Property implements PropertyTerm {

    /**
     * Create a new lattice property associated with the given lattice.
     * @param lattice The given lattice.
     */
    public LatticeProperty(PropertyLattice lattice) {
        _lattice = lattice;
    }

    /**
     * Create a new lattice property with the specified name and the specified
     * lattice.
     * @param lattice The specified lattice where this property resides.
     * @param name The specified name for the property.
     */
    public LatticeProperty(PropertyLattice lattice, String name) {
        super(name);
        _lattice = lattice;
    }

    /**
     * Return a copy of this LatticeProperty.
     * @return The clone.
     * @exception CloneNotSupportedException Not thrown in this base class.
     */
    public Object clone() throws CloneNotSupportedException {
        return this;
    }

    /**
     * Return the associated object of this property term. By default, a lattice
     * property is a constant property term. It has no associated object. This
     * method returns null.
     * @return Null.
     */
    public Object getAssociatedObject() {
        return null;
    }

    /**
     * Return an array of constants contained in this term. By default, a
     * lattice property is a constant property term. Return a size-1 array which
     * contains this property.
     */
    public InequalityTerm[] getConstants() {
        return new InequalityTerm[] {
            this
        };
    }

    /**
     * Get the property lattice associated with this property.
     * @return The associated property lattice.
     */
    public PropertyLattice getPropertyLattice() {
        return _lattice;
    }

    /**
     * Return the value of the property term. Return this property.
     * @return This property.
     */
    public Object getValue() {
        return this;
    }

    /**
     * Return an array of constants contained in this term. Return an array of
     * zero length.
     * @return An empty array.
     */
    public InequalityTerm[] getVariables() {
        return new InequalityTerm[0];
    }

    /**
     * Initialize the property term. Since LatticeProperty is a constant term,
     * this base class does nothing.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public void initialize(Object object) throws IllegalActionException {
        // do nothing
    }

    /**
     * Return true if this property does not correspond to a single property
     * class. This occurs if the type is not instantiable, or it represents
     * either an abstract base class or an interface. Return false in this base
     * class by default.
     * @return False.
     */
    public boolean isAbstract() {
        return false;
    }

    /**
     * Test if the argument property is compatible with this property.
     * @param property An instance of Property.
     * @return True if the argument is compatible with this property.
     */
    public boolean isCompatible(Property property) {
        int propertyInfo = _lattice.compare(this, property);
        return propertyInfo == CPO.SAME || propertyInfo == CPO.HIGHER;
    }

    /**
     * Test if this property is a constant. Subclass should overrides this to
     * return false if the property is the bottom of the lattice. This base
     * class returns true by default.
     * @return True in this base class.
     */
    public boolean isConstant() {
        return true;
    }

    public boolean isEffective() {
        return true;
    }

    /**
     * Determine if this Type corresponds to an instantiable token class.
     * @return True if this type corresponds to an instantiable token class.
     */
    public boolean isInstantiable() {
        return true;
    }

    /**
     * Return true if this property term is a settable (variable) term.
     * Otherwise, return false. Return false in this base class by default.
     * @return False.
     */
    public boolean isSettable() {
        return false;
    }

    /**
     * Return true if the specified property is a substitution instance of this
     * property. For the argument to be a substitution instance, it must be
     * either the same as this property, or it must be a property that can be
     * obtained by replacing the Baseproperty.UNKNOWN component of this property
     * by another property.
     * @param property A property.
     * @return True if the argument is a substitution instance of this property.
     */
    public boolean isSubstitutionInstance(Property property) {
        return //(this == UNKNOWN) ||
        this == _lattice.bottom() || this == property;
    }

    /**
     * Return true if this property is a valid property value. Otherwise, return
     * false. Return true in this base class by default.
     * @return True.
     */
    public boolean isValueAcceptable() {
        return true;
    }

    /**
     * Set the effectiveness of this property term to the specified value. Do
     * nothing in this base by default.
     * @param isEffective The specified effective value.
     */
    public void setEffective(boolean isEffective) {
        // do nothing
    }

    /**
     * Set the value of this property term. Do nothing in this base by default.
     * @param value The specified value.
     */
    public void setValue(Object value) throws IllegalActionException {
        // do nothing
    }

    /**
     * Return the string that represents this lattice property. This base class
     * returns the simple class name of the lattice property.
     */
    public String toString() {
        if (_name.length() > 0) {
            return _name;
        }
        return getClass().getSimpleName();
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected variables                 ////

    /**
     * The property lattice containing this lattice property.
     */
    protected PropertyLattice _lattice;

}
