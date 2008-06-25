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

import java.util.HashMap;

import ptolemy.data.properties.Property;
import ptolemy.graph.CPO;
import ptolemy.graph.DirectedAcyclicGraph;

//////////////////////////////////////////////////////////////////////////
//// PropertyLattice

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

 @author Thomas Mandl, Man-Kit Leung, Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.4
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 @see ptolemy.graph.CPO
 */
public class PropertyLattice {
    
    protected PropertyLattice() {
    }
        
    protected class ThePropertyLattice implements CPO {
        
        /** Return the bottom element of the property lattice, which is UNKNOWN.
         *  @return The Property object representing UNKNOWN.
         */
        public Object bottom() {
            synchronized (PropertyLattice.class) {
                return _basicLattice.bottom();
            }
        }
        
        public CPO basicLattice() {
            return _basicLattice;
        }

        /** Compare two properties in the property lattice. The arguments must be
         *  instances of Property, otherwise an exception will be thrown.
         *  This method returns one of ptolemy.graph.CPO.LOWER,
         *  ptolemy.graph.CPO.SAME, ptolemy.graph.CPO.HIGHER,
         *  ptolemy.graph.CPO.INCOMPARABLE, indicating the first argument
         *  is lower than, equal to, higher than, or incomparable with the
         *  second argument in the property hierarchy, respectively.
         *  @param t1 an instance of Property.
         *  @param t2 an instance of Property.
         *  @return An integer.
         *  @exception IllegalArgumentException If one or both arguments
         *   are not instances of Property.
         */
        public int compare(Object t1, Object t2) {
            synchronized (PropertyLattice.class) {
                if (!(t1 instanceof Property) || !(t2 instanceof Property)) {
                    throw new IllegalArgumentException("ThePropertyLattice.compare: "
                            + "Arguments are not instances of Property: " + " property1 = "
                            + t1 + ", property2 = " + t2);
                }
                return _basicLattice.compare((Property)t1, (Property)t2);
            }
        }

        /** Throw an exception. This operation is not supported since we don't
         *  need it.
         *  @exception UnsupportedOperationException Always thrown.
         */
        public Object[] downSet(Object e) {
            throw new UnsupportedOperationException(
                    "ThePropertyLattice.downSet(): operation not supported for "
                            + "the property lattice.");
        }

        /** Return the greatest lower bound of two properties.
         *  @param t1 an instance of Property.
         *  @param t2 an instance of Property.
         *  @return an instance of Property.
         *  @exception IllegalArgumentException If one or both of the
         *   specified arguments are not instances of Property.
         */
        public Object greatestLowerBound(Object t1, Object t2) {
            synchronized (PropertyLattice.class) {
                if (t1 == null || t2 == null) {
                    return null;
                } 
                
                if (!(t1 instanceof Property) || !(t2 instanceof Property)) {
                    throw new IllegalArgumentException(
                            "ThePropertyLattice.greatestLowerBound: "
                            + "Arguments are not instances of Property.");
                }
                if (!_basicLattice.containsNodeWeight(t1)) {
                    throw new IllegalArgumentException(
                            "ThePropertyLattice does not contain " + t1);
                }
                if (!_basicLattice.containsNodeWeight(t2)) {
                    throw new IllegalArgumentException(
                            "ThePropertyLattice does not contain " + t2);
                }
                int relation = _basicLattice.compare(t1, t2);
                if (relation == SAME) {
                    return t1;
                } else if (relation == LOWER) {
                    return t1;
                } else if (relation == HIGHER) {
                    return t2;
                } else { // INCOMPARABLE
                    return _basicLattice.greatestLowerBound(t1, t2);
                }
            }
        }

        /** Return the greatest lower bound of a subset.
         *  @param subset an array of properties.
         *  @return an instance of Property.
         */
        public Object greatestLowerBound(Object[] subset) {
            throw new UnsupportedOperationException(
                    "ThePropertyLattice.greatestUpperBound(Object[]) :" +
                    " operation not supported for the property lattice.");
        }

        /** Return the greatest property of a set of properties, or null if the
         *  greatest one does not exist.
         *  @param subset an array of properties.
         *  @return A Property or null.
         */
        public Object greatestElement(Object[] subset) {
            synchronized (PropertyLattice.class) {
                // Compare each element with all of the other elements to search
                // for the greatest one. This is a simple, brute force algorithm,
                // but may be inefficient. A more efficient one is used in
                // the graph package, but more complex.
                for (int i = 0; i < subset.length; i++) {
                    boolean isGreatest = true;

                    for (int j = 0; j < subset.length; j++) {
                        int result = compare(subset[i], subset[j]);

                        if ((result == CPO.LOWER) || (result == CPO.INCOMPARABLE)) {
                            isGreatest = false;
                            break;
                        }
                    }

                    if (isGreatest == true) {
                        return subset[i];
                    }
                }
                // FIXME: Shouldn't this return GENERAL?
                return null;
            }
        }

        /** Return true.
         *  @return true.
         */
        public boolean isLattice() {
            return true;
        }

        /** Return the least property of a set of properties, or null if the
         *  least one does not exist.
         *  @param subset an array of properties.
         *  @return A Property or null.
         */
        public Object leastElement(Object[] subset) {
            synchronized (PropertyLattice.class) {
                // Compare each element with all of the other elements to search
                // for the least one. This is a simple, brute force algorithm,
                // but may be inefficient. A more efficient one is used in
                // the graph package, but more complex.
                for (int i = 0; i < subset.length; i++) {
                    boolean isLeast = true;

                    for (int j = 0; j < subset.length; j++) {
                        int result = compare(subset[i], subset[j]);

                        if ((result == CPO.HIGHER) || (result == CPO.INCOMPARABLE)) {
                            isLeast = false;
                            break;
                        }
                    }

                    if (isLeast == true) {
                        return subset[i];
                    }
                }
                // FIXME: Shouldn't thir return bottom?
                return null;
            }
        }

        /** Return the least upper bound of two properties.
         *  @param t1 an instance of Property.
         *  @param t2 an instance of Property.
         *  @return an instance of Property.
         */
        public Object leastUpperBound(Object t1, Object t2) {
            synchronized (PropertyLattice.class) {
                if (t1 == null) {
                    return t2;
                } 
                if (t2 == null) {
                    return t1;
                }
                
                if (!(t1 instanceof Property) || !(t2 instanceof Property)) {
                    throw new IllegalArgumentException(
                            "ThePropertyLattice.leastUpperBound: "
                            + "Arguments are not instances of Property.");
                }

                    // Both are neither the same structured property, nor an array
                    // and non-array pair, so their property relation is defined
                    // by the basic lattice.
                    int relation = _basicLattice.compare(t1, t2);

                    if (relation == SAME) {
                        return t1;
                    } else if (relation == LOWER) {
                        return t2;
                    } else if (relation == HIGHER) {
                        return t1;
                    } else { // INCOMPARABLE
                        return _basicLattice.leastUpperBound(t1, t2);
                    }
            }
        }

        /** Return the least upper bound of a subset.
         *  @param subset an array of properties.
         *  @return an instance of Property.
         */
        public Object leastUpperBound(Object[] subset) {
            throw new UnsupportedOperationException(
                    "ThePropertyLattice.leastUpperBound(Object[]) :" +
                    " operation not supported for the property lattice.");
        }

        /** Return the top element of the property lattice, which is General.
         *  @return The Property object representing General.
         */
        public Object top() {
            synchronized (PropertyLattice.class) {
                return _basicLattice.top();
            }
        }

        /** Throw an exception. This operation is not supported since the
         *  property lattice is infinite,
         *  this operation is not supported.
         *  @exception UnsupportedOperationException Always thrown.
         */
        public Object[] upSet(Object e) {
            throw new UnsupportedOperationException(
                    "ThePropertyLattice.upSet(): operation not supported for "
                            + "the property lattice.");
        }
        
        protected DirectedAcyclicGraph _basicLattice = new DirectedAcyclicGraph();

        public void setBasicLattice(DirectedAcyclicGraph graph) {
            _basicLattice = graph;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
   
    /** Return the an instance of CPO representing the basic property
     *  lattice.
     *  @return an instance of CPO.
     */
    public CPO basicLattice() {
        return _lattice.basicLattice();
    }

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
    public synchronized int compare(Property property1, Property property2) {
        if ((property1 == null) || (property2 == null)) {
            throw new IllegalArgumentException(
                    "PropertyLattice.compare(Property, Property): "
                            + "one or both of the argument properties is null: "
                            + " property1 = " + property1 + ", property2 = " + property2);
        }

        return _lattice.compare(property1, property2);
    }

    public static void resetAll() {
        _lattices.clear();
    }
    
    /**
     * Return the property lattice described by the given lattice
     * description file. If the lattice was not created already, 
     * a new property lattice is instantiated.
     * @param latticeName The given lattice name.
     * @return The property lattice described by the given file.
     */
    public static PropertyLattice getPropertyLattice(String latticeName) {
        if (!_lattices.containsKey(latticeName)) {

            try {
                Class latticeClass = Class.forName("ptolemy.data.properties.lattice." + latticeName + ".Lattice");
                // Create a new instance of PropertyLattice.
                //PropertyLattice newLattice = new PropertyLattice();
                PropertyLattice newLattice = (PropertyLattice)
                    latticeClass.getConstructor(new Class[0]).newInstance(new Object[0]);                

                _lattices.put(latticeName, newLattice);

            } catch (Exception e) {
                e.printStackTrace();
            }            
        }
        
        PropertyLattice lattice = _lattices.get(latticeName);
        
        return lattice;
    }

    
    /** Return the greatest lower bound of the two given properties.
     *  @param property1 The first given property.
     *  @param property2 The second given property.
     *  @return The greatest lower bound of property1 and property2.
     */
    public synchronized Property greatestLowerBound(Property property1, Property property2) {
        return (Property) _lattice.greatestLowerBound(property1, property2);
    }

    /** Return the an instance of CPO representing the infinite property
     *  lattice.
     *  @return an instance of CPO.
     */
    public CPO lattice() {
        return _lattice;
    }

    /** Return the least upper bound of the two given properties.
     *  @param property1 The first given property.
     *  @param property2 The second given property.
     *  @return The least upper bound of property1 and property2.
     */
    public synchronized Property leastUpperBound(Property property1, Property property2) {
        return (Property) _lattice.leastUpperBound(property1, property2);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The infinite property lattice. */
    protected ThePropertyLattice _lattice = new ThePropertyLattice();

    /**
     * A HashMap that contains all property lattices with unique
     * lattice files as keys.
     */
    private static HashMap <String, PropertyLattice> _lattices = 
        new HashMap<String, PropertyLattice>();

    public String toString() {
        String name = getClass().getPackage().getName();
        return name.substring(name.lastIndexOf('.') + 1);
    }
    
    public String getName() {
        return toString();
    }
}
