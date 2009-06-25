/**
 * Base class for structured property.
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
 * 
 * 
 */
package ptolemy.data.properties.lattice;

import ptolemy.data.properties.Property;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// StructuredProperty

/**
 * Base class for structured property. Making this an abstract class (not an
 * interface) allows the methods to be protected. All the properties of the same
 * structured property (e.g. all the array properties) must form a lattice. Each
 * instance of a structured property must know how to compare itself with
 * another instance of the same structured property, and compute the least upper
 * bound and greatest lower bound. This class defines methods for these
 * operations.
 * <p>
 * Subclasses should override clone() to do a deep cloning.
 * 
 * @author Man-Kit Leung
 * @version $Id: StructuredProperty.java 47513 2007-12-07 06:32:21Z cxh $
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
public abstract class StructuredProperty extends LatticeProperty {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * @param lattice
     */
    public StructuredProperty(PropertyLattice lattice) {
        super(lattice);
    }

    /**
     * Return a deep copy of this StructuredProperty.
     * 
     * @return A StructuredProperty.
     * @exception CloneNotSupportedException Not thrown in this base class.
     */
    @Override
    abstract public Object clone() throws CloneNotSupportedException;

    /**
     * Return the depth of a structured property. The depth of a structured
     * property is the number of times a structured property contains other
     * structured properties. For example, an array of arrays has depth 2, and
     * an array of arrays of records has depth 3.
     * 
     * @return the depth of a structured property.
     */
    public int depth() {
        return 1;
    }

    /**
     * Return a static instance of this structured property. The return value is
     * used by PropertyLattice to represent this property.
     * 
     * @return a StructuredProperty.
     */
    public abstract StructuredProperty getRepresentative();

    /**
     * Set the elements that have declared property BaseProperty.UNKNOWN to the
     * specified property.
     * 
     * @param property A Property.
     */
    public abstract void initialize(Property property);

    /**
     * Return true if this type does not correspond to a single token class.
     * This occurs if the type is not instantiable, or it represents either an
     * abstract base class or an interface. This method should be overridden in
     * derived classes to return true only for types which are not abstract.
     * @return true.
     */
    @Override
    public boolean isAbstract() {
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Update this StructuredProperty to the specified Structured Property. The
     * specified property must have depth less than the MAXDEPTHBOUND, and have
     * the same structure as this property. This method will only update the
     * component property that is BaseProperty.UNKNOWN, and leave the constant
     * part of this property intact.
     * 
     * @param newProperty A StructuredProperty.
     * @exception IllegalActionException If the specified property has a
     * different structure.
     */
    public void updateProperty(StructuredProperty newProperty)
            throws IllegalActionException {
        if (newProperty.depth() >= MAXDEPTHBOUND) {
            throw new IllegalActionException(
                    "Large property structure detected during property resolution."
                            + "  The structured property "
                            + newProperty.toString()
                            + " has depth larger than the bound "
                            + MAXDEPTHBOUND
                            + ".  This may be an indicator of property constraints "
                            + "in a model with no finite solution.");
        }
    }

    /**
     * Compare this property with the specified property. The specified property
     * must be of the same structured property, otherwise an exception will be
     * thrown. This method returns one of ptolemy.graph.CPO.LOWER,
     * ptolemy.graph.CPO.SAME, ptolemy.graph.CPO.HIGHER,
     * ptolemy.graph.CPO.INCOMPARABLE, indicating this property is lower than,
     * equal to, higher than, or incomparable with the specified property in the
     * property hierarchy, respectively.
     * 
     * @param property a StructuredProperty.
     * @return An integer.
     * @exception IllegalArgumentException If the specified property is not the
     * same structured property as this one.
     */
    protected abstract int _compare(StructuredProperty property);

    /**
     * Return the greatest lower bound of this property with the specified
     * property. The specified property must be of the same structured property,
     * otherwise an exception will be thrown.
     * 
     * @param property a StructuredProperty.
     * @return a StructuredProperty.
     * @exception IllegalArgumentException If the specified property is not the
     * same structured property as this one.
     */
    protected abstract StructuredProperty _greatestLowerBound(
            StructuredProperty property);

    /**
     * Return the least upper bound of this property with the specified
     * property. The specified property must be of the same structured property,
     * otherwise an exception will be thrown.
     * 
     * @param property a StructuredProperty.
     * @return a StructuredProperty.
     * @exception IllegalArgumentException If the specified property is not the
     * same structured property as this one.
     */
    protected abstract StructuredProperty _leastUpperBound(
            StructuredProperty property);

    /**
     * Set up a bound for the max depth of structured properties. This bound is
     * used to detect infinite iterations.
     */
    protected static final int MAXDEPTHBOUND = 20;
}
