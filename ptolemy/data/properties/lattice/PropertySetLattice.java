/*
 * Property hierarchy.
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
 */
package ptolemy.data.properties.lattice;

import ptolemy.graph.CPO;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// PropertySetLattice

/**
 * Property hierarchy base class. Note that all public methods are synchronized.
 * There are more than one instances of a property lattice. Although the
 * property lattice is constructed once and then typically does not change
 * during execution, the methods need to be synchronized because there are
 * various data structures used to cache results that are expensive to compute.
 * These data structures do change during execution. Multiple threads may be
 * accessing the property lattice simultaneously and modifying these data
 * structures. To ensure thread safety, the methods need to be synchronized.
 * 
 * @author Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 * @see ptolemy.graph.CPO
 */
public class PropertySetLattice extends PropertyLattice {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Compare two properties in the property lattice. This method returns one
     * of ptolemy.graph.CPO.LOWER, ptolemy.graph.CPO.SAME,
     * ptolemy.graph.CPO.HIGHER, ptolemy.graph.CPO.INCOMPARABLE, indicating the
     * first argument is lower than, equal to, higher than, or incomparable with
     * the second argument in the property hierarchy, respectively.
     * @param t1 an instance of PropertySet.
     * @param t2 an instance of PropertySet.
     * @return An integer.
     */
    @Override
    public int compare(Object t1, Object t2) {
        PropertySet property1 = (PropertySet) t1;
        PropertySet property2 = (PropertySet) t2;
        if (property1 == null || property2 == null) {
            throw new IllegalArgumentException(
                    "PropertySetLattice.compare(Property, Property): "
                            + "one or both of the argument properties is null: "
                            + " property1 = " + property1 + ", property2 = "
                            + property2);
        }

        boolean isSuperset = property1.containsAll(property2);
        boolean isSubset = property2.containsAll(property1);

        if (isSuperset && isSubset) {
            return CPO.SAME;
        } else if (isSuperset) {
            return CPO.HIGHER;
        } else if (isSubset) {
            return CPO.LOWER;
        } else {
            return CPO.INCOMPARABLE;
        }
    }

    /**
     * Return the property element for the specified name.
     * @param elementName the specified element name.
     * @return The property.
     */
    @Override
    public LatticeProperty getElement(String elementName)
            throws IllegalActionException {
        try {
            return (LatticeProperty) getClass().getField(elementName).get(this);

            //        Property property = _propertyMap.get(fieldName.toUpperCase());
            //        if (property == null) {
            //            throw new IllegalActionException(
            //                    "No lattice element named \"" + fieldName + "\".");
            //        }
            //        return property;

        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Return the greatest lower bound of the two given properties.
     * @param t1 an instance of PropertySet.
     * @param t2 an instance of PropertySet.
     * @return The greatest lower bound of property1 and property2.
     */
    @Override
    public Object greatestLowerBound(Object t1, Object t2) {
        PropertySet property1 = (PropertySet) t1;
        PropertySet property2 = (PropertySet) t2;
        PropertySet intersection = new PropertySet(this, property1);
        intersection.retainAll(property2);
        return intersection;
    }

    /**
     * Return true if this is an acceptable solution.
     * @return true if this is an acceptable solution; otherwise, false;
     */
    public boolean isAcceptableSolution(PropertySet propertySet) {
        return true;
        //        throw new UnsupportedOperationException(
        //                "PropertySetLattice.isAcceptableSolution(): " +
        //                "operation not supported for the base class.");
    }

    /**
     * Return the least upper bound of the two given properties.
     * @param t1 an instance of PropertySet.
     * @param t2 an instance of PropertySet.
     * @return The least upper bound of property1 and property2.
     */
    @Override
    public Object leastUpperBound(Object t1, Object t2) {
        PropertySet property1 = (PropertySet) t1;
        PropertySet property2 = (PropertySet) t2;
        PropertySet union = new PropertySet(this, property1);
        union.addAll(property2);
        return union;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

}
