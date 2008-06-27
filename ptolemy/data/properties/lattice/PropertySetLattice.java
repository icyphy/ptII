/* Property hierarchy.

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

import ptolemy.graph.CPO;

//////////////////////////////////////////////////////////////////////////
//// PropertySetLattice

/**
 Property hierarchy base class.
 Note that all public methods are synchronized.
 There are more than one instances of a property lattice.
 Although the property lattice is constructed once and then typically
 does not change during execution, the methods need to be synchronized
 because there are various data structures used to cache results that
 are expensive to compute. These data structures do change during
 execution. Multiple threads may be accessing the property lattice
 simultaneously and modifying these data structures. To ensure
 thread safety, the methods need to be synchronized.

 @author Man-Kit Leung
 @version $Id: SetLattice.java,v 1.8 2008/04/20 07:32:02 mankit Exp $
 @since Ptolemy II 7.0
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 @see ptolemy.graph.CPO
 */
public class PropertySetLattice extends PropertyLattice {
    
    protected PropertySetLattice() {
    }
        
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
   
    /** Compare two properties in the property lattice.
     *  This method returns one of ptolemy.graph.CPO.LOWER,
     *  ptolemy.graph.CPO.SAME, ptolemy.graph.CPO.HIGHER,
     *  ptolemy.graph.CPO.INCOMPARABLE, indicating the first argument
     *  is lower than, equal to, higher than, or incomparable with the
     *  second argument in the property hierarchy, respectively.
     *  @param property1 an instance of Property.
     *  @param property2 an instance of Property.
     *  @return An integer.
     */
    public synchronized int compare(PropertySet property1, PropertySet property2) {
        if ((property1 == null) || (property2 == null)) {
            throw new IllegalArgumentException(
                    "PropertySetLattice.compare(Property, Property): "
                            + "one or both of the argument properties is null: "
                            + " property1 = " + property1 + ", property2 = " + property2);
        }

        boolean isSuperset = property1.containsAll(property2);
        boolean isSubset = property1.containsAll(property2);
        
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
    
    /** Return the greatest lower bound of the two given properties.
     *  @param property1 The first given property.
     *  @param property2 The second given property.
     *  @return The greatest lower bound of property1 and property2.
     */
    public synchronized PropertySet greatestLowerBound(
            PropertySet property1, PropertySet property2) {
        PropertySet union = new PropertySet(this, property1);
        union.addAll(property2);
        return union;
    }

    /**
     * Return true if this is an acceptable solution.
     * @return true if this is an acceptable solution; otherwise, false;
     */
    public boolean isAcceptableSolution(PropertySet propertySet) {
        throw new UnsupportedOperationException(
                "PropertySetLattice.isAcceptableSolution(): " +
                "operation not supported for the base class.");
    }

    /** Return the least upper bound of the two given properties.
     *  @param property1 The first given property.
     *  @param property2 The second given property.
     *  @return The least upper bound of property1 and property2.
     */
    public synchronized PropertySet leastUpperBound(PropertySet property1, PropertySet property2) {
        PropertySet intersection = new PropertySet(this, property1);
        intersection.retainAll(property2);
        return intersection;
    }

    public String toString() {
        String name = getClass().getPackage().getName();
        return name.substring(name.lastIndexOf('.') + 1);
    }
    
    public String getName() {
        return toString();
    }
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

}
