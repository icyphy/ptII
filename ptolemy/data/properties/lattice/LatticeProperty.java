/** A base class representing a lattice property.

 Copyright (c) 1997-2006 The Regents of the University of California.
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
package ptolemy.data.properties.lattice;

import java.util.LinkedList;

import ptolemy.data.properties.Property;
import ptolemy.graph.CPO;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.IllegalActionException;


//////////////////////////////////////////////////////////////////////////
//// LatticeProperty

/**
 A base class representing a lattice property. A lattice property is a
 element in a property lattice. The user should create new sub-classes
 to represent different elements in a property lattice 
 (See PropertyLattice.java).

 @author Thomas Mandl, Man-Kit Leung, Edward A. Lee
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class LatticeProperty extends Property implements PropertyTerm {
        
    /**
     * Create a new lattice property associated with the given
     * lattice.
     * @param lattice The given lattice.
     */
    public LatticeProperty(PropertyLattice lattice) {
        _lattice = lattice;
    }

    /** Test if the argument property is compatible with this property.
     *  @param property An instance of Property.
     *  @return True if the argument is compatible with this property.
     */
    public boolean isCompatible(Property property) {
        int propertyInfo = _lattice.compare(this, property);
        return ((propertyInfo == CPO.SAME) || (propertyInfo == CPO.HIGHER));
    }

    /** Test if this property is a constant. Returns true in this base class.
     *  @return True in this base class.
     */
    public boolean isConstant() {
        return true;
    }


    /** Determine if this Type corresponds to an instantiable token
     *  class.
     *  @return True if this type corresponds to an instantiable
     *   token class.
     */
    public boolean isInstantiable() {
        return true;
    }

    /** Return true if the specified property is a substitution instance of this
     *  property. For the argument to be a substitution instance, it must be
     *  either the same as this property, or it must be a property that can be
     *  obtained by replacing the Baseproperty.UNKNOWN component of this property by
     *  another property.
     *  @param property A property.
     *  @return True if the argument is a substitution instance of this property.
     */
    public boolean isSubstitutionInstance(Property property) {
        return //(this == UNKNOWN) ||
        (this == _lattice.basicLattice().bottom()) ||
        (this == property);
    }
    
    /** Get the property lattice associated with this property.
     *  @return The associated property lattice.
     */
    public PropertyLattice getPropertyLattice() {
        return _lattice;
    }
    
    /**
     * Return the string that represents this lattice property. This
     * base class returns the simple class name of the lattice property. 
     */
    public String toString() {
        return this.getClass().getSimpleName();
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     * The property lattice containing this lattice property.
     */
    protected PropertyLattice _lattice;

    
    
    public Object getValue() {
        return this;
    }

    public boolean isEffective() {
        return true;
    }

    public void setEffective(boolean isEffective) {
        // do nothing
    }

    public Object getAssociatedObject() {
        return null;
    }

    public InequalityTerm[] getVariables() {
        return new InequalityTerm[0];
    }

    public InequalityTerm[] getConstants() {
        return (InequalityTerm[]) new LinkedList().toArray();
    }

    public void initialize(Object e) throws IllegalActionException {
        // do nothing
    }

    public boolean isSettable() {
        return false;
    }

    public boolean isValueAcceptable() {
        return true;
    }

    public void setValue(Object e) throws IllegalActionException {
        // do nothing
    }
    
}
