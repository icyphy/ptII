/**
 * The base class for a an element in a property lattice.
 * 
 * Copyright (c) 2007-2010 The Regents of the University of California. All
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

///////////////////////////////////////////////////////////////////
//// LatticeProperty

/**
 * A base class representing a lattice property. A lattice property is a element
 * in a property lattice. The user should create new sub-classes to represent
 * different elements in a property lattice, see
 * {@link ptolemy.data.properties.lattice.PropertyLattice}.
 * 
 * @author Thomas Mandl, Man-Kit Leung, Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
public class LatticeProperty extends Property implements PropertyTerm, Cloneable {

    // FIXME: There is no reason that for this to have to be
    // subclassed at all to create a lattice. Instead, we should have
    // either constructor arguments or getter and setter methods for
    // the key fields (depending on whether we want these fields to be
    // immutable).

    /**
     * Create a new lattice property associated with the given lattice.
     * @param lattice The given lattice.
     */
    public LatticeProperty(PropertyLattice lattice) {
        // FIXME: This constructors does not specify a name, which causes
        // toString() to use the classname as the name.  This seems wrong.
        // For one thing, if you use this constructor, equals() will
        // throw a null-pointer exception!
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
     * Return this LatticeProperty.
     * @return The clone.
     * @exception CloneNotSupportedException Not thrown in this base class.
     */
    public Object clone() throws CloneNotSupportedException {
        // FIXME: The clone() method returns this.  This is only
        // correct if the object and all instances of subclasses are
        // immutable. This seems extremely unlikely in a class that
        // has an isConstant() method.
        //
        // cloning was an issue when we combined the property system
        // with model transformation. We got it to run somehow,
        // but unfortunately I don't remember the details. It's
        // definitely something we did not understand in full detail,
        // so some refactoring may be good. In my opinion the
        // refactored code needs to be tested along with the model
        // transformation.
        return this;
    }

    /**
     * Return true if the given object is equal to this lattice property. Two
     * lattice properties are considered equal if they have the same name and
     * are from the same lattice.
     * 
     * Any instantiated LatticeProperty objects should use the
     * LatticeProperty(Lattice lattice, String name) constructor so that
     * the LatticeProperty's _name member is set to a value.
     * Any subclasses of LatticeProperty should call this constructor in their
     * constructor so that the property's _name is set correctly.
     * If this is not done, then _name will be set to the empty string and
     * will not have the correct value needed when the equals() method tests for
     * equality between two LatticeProperty objects. 
     */
    public boolean equals(Object object) {
        // FIXME: The equals() method compares names (which seems questionable,
        // why is this right?). Even if this this is right,
        // there is no corresponding hashcode() method, so if instances
        // are put into hashed data structures, we will get chaos.
        // 
        // The equals() implementation probably has to do with the
        // reset mechanism. Every time we start the solver, new
        // instances of the classes are created. Hence, comparison on
        // the id's does not match.

        if (object != null) {
            if (object instanceof LatticeProperty) {
                if (_name.equals(((LatticeProperty) object)._name)) {
                    return _lattice.getName().equals(
                            ((LatticeProperty) object)._lattice.getName());
                }
            }
        }
        return false;
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

    // FIXME: Having a hashCode method() causes lots of tests to fail?
    // why?

//     /** Return a hash code value for this LatticeProperty.
//      *  This method returns the hashCode of the {@link #_lattice}.   
//      *  @return A hash code value for this LatticeProperty.
//      */
//     public int hashCode() {
//         return _lattice.hashCode();
//     }

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

    /**
     * Return true if this property term is effective.
     *
     * <p>This method is used to mark constraints that are due to
     * unreachable parts of the model as not effective (i.e.
     * isEffective() would return false), so that they would be
     * excluded from the list passed to the constraint solver.</p>
     *
     * <p>isEffecitive() is ony used when we know for sure that parts
     * of a model are unreachable. An example is a FSM with a guard
     * like TRUE == FALSE. If we know that the guard transition can
     * never happen (and therefore we use the static/dynamic solver
     * with some basic partial evaluation) we reduce the model. In
     * order the avoid the effort of recollecting the effecitve
     * constraints the constraints are collected only once and then
     * marked as effective (default) or not effective.
     *
     * <p>Note that reachability analysis is undecidable and partial
     * reachability analysis has not yet been fully implemented.
     * Also, it's helpful to know the values of
     * parameters/outputs/transition guards for doing the analysis,
     * but there are still some fundamental issues with calculating
     * the values.</p>
     *
     * @return Always return true in this base class.
     * @see #setEffective(boolean)
     */
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
     * @param isEffective The specified effective value, ignored by this method
     * @see #isEffective()
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
        // FIXME: this seems wrong.  Why return the class name?
        return getClass().getSimpleName();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /**
     * The property lattice containing this lattice property.
     */
    protected PropertyLattice _lattice;

}
