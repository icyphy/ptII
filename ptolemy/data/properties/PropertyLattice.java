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
package ptolemy.data.properties;

import ptolemy.data.Token;
import ptolemy.graph.CPO;
import ptolemy.graph.DirectedAcyclicGraph;
import ptolemy.kernel.util.InternalErrorException;

//////////////////////////////////////////////////////////////////////////
//// PropertyLattice

/**
 Property hierarchy base class.
 Note that all public methods are static and synchronized.
 There is exactly one instance of a property lattice.
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
 @Pt.ProposedRating Red (yuhong)
 @Pt.AcceptedRating Red
 @see ptolemy.graph.CPO
 */
public class PropertyLattice {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the an instance of CPO representing the basic property
     *  lattice.
     *  @return an instance of CPO.
     */
    public static CPO basicLattice() {
        return _lattice._basicLattice;
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
    public synchronized static int compare(Property property1, Property property2) {
        if ((property1 == null) || (property2 == null)) {
            throw new IllegalArgumentException(
                    "PropertyLattice.compare(Property, Property): "
                            + "one or both of the argument properties is null: "
                            + " property1 = " + property1 + ", property2 = " + property2);
        }
        // System.out.println("compare " + property1 + " and " + property2 + " = " + _lattice.compare(property1, property2));

        /*
        int i1 = property1.getPropertyHash();
        int i2 = property2.getPropertyHash();

        // Uncommment the false below to measure the impact of
        // _lattice.compare() on ptolemy.data package performance... Run
        // ptolemy/data/property/test/performance.xml before and after...(zk)
        if ( //false &&
        (i1 != Property.HASH_INVALID) && (i2 != Property.HASH_INVALID)) {
            if (_getCachedPropertyComparisonResult(i1, i2) == Property.HASH_INVALID) {
                _setCachedPropertyComparisonResult(i1, i2, _lattice.compare(property1,
                        property2));
            }

            return _getCachedPropertyComparisonResult(i1, i2);
        }
        */
        
        return _lattice.compare(property1, property2);
    }

    /** Return the an instance of CPO representing the infinite property
     *  lattice.
     *  @return an instance of CPO.
     */
    public static CPO lattice() {
        return _lattice;
    }

    /** Return the least upper bound of the two given properties.
     *  @param property1 The first given property.
     *  @param property2 The second given property.
     *  @return The least upper bound of property1 and property2.
     */
    public synchronized static Property leastUpperBound(Property property1, Property property2) {
        return (Property) _lattice.leastUpperBound(property1, property2);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return the result for the properties that have the given two
     * indexes as hashes.
     */
    private static final int _getCachedPropertyComparisonResult(int index1,
            int index2) {
        return _compareCache[index1][index2];
    }

    /** Set the result for the properties that have the given two
     *  indexes as hashes.
     */
    private static final void _setCachedPropertyComparisonResult(int index1,
            int index2, int value) {
        _compareCache[index1][index2] = value;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////
    // The infinite property lattice
    private static class ThePropertyLattice implements CPO {
        /** Return the bottom element of the property lattice, which is UNKNOWN.
         *  @return The Property object representing UNKNOWN.
         */
        public Object bottom() {
            synchronized (PropertyLattice.class) {
                return _basicLattice.bottom();
            }
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
                int relation = _basicLattice.compare(t1, t1);
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
            synchronized (PropertyLattice.class) {
                if (subset.length == 0) {
                    //return BaseProperty.GENERAL;
                }

                Object glb = subset[0];

                // start looping from index 0 so that subset[0] is checked for
                // possible exception, in case the subset has only one element.
                for (int i = 0; i < subset.length; i++) {
                    glb = greatestLowerBound(glb, subset[i]);
                }

                return glb;
            }
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
            synchronized (PropertyLattice.class) {
                if (subset.length == 0) {
                    //return BaseProperty.UNKNOWN;
                }

                Object lub = subset[0];

                // start looping from index 0 so that subset[0] is checked for
                // possible exception, in case the subset has only one element.
                for (int i = 0; i < subset.length; i++) {
                    lub = leastUpperBound(lub, subset[i]);
                }

                return lub;
            }
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

        ///////////////////////////////////////////////////////////////
        ////                    private constructor                ////
        // the constructor is private so only the outer class can use it.
        private ThePropertyLattice() {
            synchronized (PropertyLattice.class) {
                _basicLattice = new DirectedAcyclicGraph();
                
                /*

                StructuredProperty arrayRep = (new ArrayProperty(BaseProperty.UNKNOWN))
                ._getRepresentative();

                String[] labels = new String[0];
                Property[] properties = new Property[0];
                StructuredProperty recordRep = (new RecordProperty(labels, properties))
                ._getRepresentative();
                StructuredProperty unionRep = (new UnionProperty(labels, properties))
                ._getRepresentative();

                //StructuredProperty functionRep = 
                 new ptolemy.data.property.FunctionProperty(
                        new ptolemy.data.property.Property[0],
                        ptolemy.data.property.BaseProperty.UNKNOWN)._getRepresentative();

                _basicLattice.addNodeWeight(BaseProperty.BOOLEAN);
                _basicLattice.addNodeWeight(BaseProperty.BOOLEAN_MATRIX);
                _basicLattice.addNodeWeight(BaseProperty.UNSIGNED_BYTE);
                _basicLattice.addNodeWeight(BaseProperty.COMPLEX);
                _basicLattice.addNodeWeight(BaseProperty.COMPLEX_MATRIX);
                _basicLattice.addNodeWeight(BaseProperty.DOUBLE);
                _basicLattice.addNodeWeight(BaseProperty.DOUBLE_MATRIX);
                _basicLattice.addNodeWeight(BaseProperty.UNSIZED_FIX);
                _basicLattice.addNodeWeight(BaseProperty.SIZED_FIX);
                _basicLattice.addNodeWeight(BaseProperty.FIX_MATRIX);
                _basicLattice.addNodeWeight(BaseProperty.INT);
                _basicLattice.addNodeWeight(BaseProperty.INT_MATRIX);
                _basicLattice.addNodeWeight(BaseProperty.LONG);
                _basicLattice.addNodeWeight(BaseProperty.LONG_MATRIX);
                _basicLattice.addNodeWeight(BaseProperty.MATRIX);
                _basicLattice.addNodeWeight(BaseProperty.UNKNOWN);
                // NOTE: Removed NUMERICAL from the property lattice, EAL 7/22/06.
                // _basicLattice.addNodeWeight(BaseProperty.NUMERICAL);
                _basicLattice.addNodeWeight(BaseProperty.OBJECT);
                _basicLattice.addNodeWeight(BaseProperty.XMLTOKEN);
                _basicLattice.addNodeWeight(BaseProperty.SCALAR);
                _basicLattice.addNodeWeight(BaseProperty.STRING);
                _basicLattice.addNodeWeight(BaseProperty.EVENT);
                _basicLattice.addNodeWeight(BaseProperty.GENERAL);
                _basicLattice.addNodeWeight(BaseProperty.PETITE);
                _basicLattice.addNodeWeight(BaseProperty.NIL);

                _basicLattice.addNodeWeight(arrayRep);
                _basicLattice.addNodeWeight(BaseProperty.ARRAY_BOTTOM);
                _basicLattice.addNodeWeight(recordRep);
                _basicLattice.addNodeWeight(unionRep);

                _basicLattice.addEdge(BaseProperty.XMLTOKEN, BaseProperty.GENERAL);
                _basicLattice.addEdge(BaseProperty.UNKNOWN, BaseProperty.XMLTOKEN);
                _basicLattice.addEdge(BaseProperty.OBJECT, BaseProperty.GENERAL);
                _basicLattice.addEdge(BaseProperty.UNKNOWN, BaseProperty.OBJECT);
                _basicLattice.addEdge(BaseProperty.STRING, BaseProperty.GENERAL);
                _basicLattice.addEdge(BaseProperty.MATRIX, BaseProperty.STRING);
                _basicLattice.addEdge(BaseProperty.BOOLEAN_MATRIX, BaseProperty.MATRIX);
                _basicLattice.addEdge(BaseProperty.BOOLEAN, BaseProperty.BOOLEAN_MATRIX);
                _basicLattice.addEdge(BaseProperty.BOOLEAN, BaseProperty.SCALAR);
                _basicLattice.addEdge(BaseProperty.UNKNOWN, BaseProperty.BOOLEAN);

                // NOTE: Removed NUMERICAL from the property lattice, EAL 7/22/06.
                // _basicLattice.addEdge(BaseProperty.NUMERICAL, BaseProperty.MATRIX);
                _basicLattice.addEdge(BaseProperty.FIX_MATRIX, BaseProperty.MATRIX);
                _basicLattice.addEdge(BaseProperty.SCALAR, BaseProperty.MATRIX);
                _basicLattice.addEdge(BaseProperty.LONG_MATRIX, BaseProperty.MATRIX);
                _basicLattice.addEdge(BaseProperty.COMPLEX_MATRIX, BaseProperty.MATRIX);

                _basicLattice.addEdge(BaseProperty.UNSIZED_FIX, BaseProperty.FIX_MATRIX);
                _basicLattice.addEdge(BaseProperty.SIZED_FIX, BaseProperty.UNSIZED_FIX);
                _basicLattice.addEdge(BaseProperty.UNSIZED_FIX, BaseProperty.SCALAR);
                _basicLattice.addEdge(BaseProperty.UNKNOWN, BaseProperty.SIZED_FIX);
                _basicLattice.addEdge(BaseProperty.LONG, BaseProperty.SCALAR);
                _basicLattice.addEdge(BaseProperty.LONG, BaseProperty.LONG_MATRIX);
                _basicLattice.addEdge(BaseProperty.INT_MATRIX, BaseProperty.LONG_MATRIX);
                _basicLattice.addEdge(BaseProperty.INT, BaseProperty.LONG);
                _basicLattice.addEdge(BaseProperty.INT, BaseProperty.INT_MATRIX);
                _basicLattice.addEdge(BaseProperty.UNKNOWN, BaseProperty.UNSIGNED_BYTE);
                _basicLattice.addEdge(BaseProperty.UNKNOWN, BaseProperty.PETITE);
                _basicLattice.addEdge(BaseProperty.INT_MATRIX, BaseProperty.DOUBLE_MATRIX);
                _basicLattice.addEdge(BaseProperty.DOUBLE_MATRIX,
                        BaseProperty.COMPLEX_MATRIX);
                _basicLattice.addEdge(BaseProperty.DOUBLE, BaseProperty.DOUBLE_MATRIX);
                _basicLattice.addEdge(BaseProperty.INT, BaseProperty.DOUBLE);
                _basicLattice.addEdge(BaseProperty.DOUBLE, BaseProperty.SCALAR);

                _basicLattice.addEdge(BaseProperty.PETITE, BaseProperty.DOUBLE);
                _basicLattice.addEdge(BaseProperty.COMPLEX, BaseProperty.SCALAR);
                _basicLattice.addEdge(BaseProperty.COMPLEX, BaseProperty.COMPLEX_MATRIX);

                _basicLattice.addEdge(BaseProperty.DOUBLE, BaseProperty.COMPLEX);
                _basicLattice.addEdge(BaseProperty.INT, BaseProperty.DOUBLE);
                _basicLattice.addEdge(BaseProperty.UNSIGNED_BYTE, BaseProperty.INT);

                _basicLattice.addEdge(BaseProperty.EVENT, BaseProperty.GENERAL);
                _basicLattice.addEdge(BaseProperty.UNKNOWN, BaseProperty.EVENT);

                _basicLattice.addEdge(arrayRep, BaseProperty.STRING);
                _basicLattice.addEdge(BaseProperty.ARRAY_BOTTOM, arrayRep);
                _basicLattice.addEdge(BaseProperty.UNKNOWN, BaseProperty.ARRAY_BOTTOM);

                _basicLattice.addEdge(recordRep, BaseProperty.STRING);
                _basicLattice.addEdge(BaseProperty.UNKNOWN, recordRep);

                _basicLattice.addEdge(unionRep, BaseProperty.GENERAL);
                _basicLattice.addEdge(BaseProperty.UNKNOWN, unionRep);

                _basicLattice.addEdge(BaseProperty.UNKNOWN, BaseProperty.NIL);
                _basicLattice.addEdge(BaseProperty.NIL, BaseProperty.BOOLEAN);
                // NOTE: Redundant, given edge to UnsignedByte
                // _basicLattice.addEdge(BaseProperty.NIL, BaseProperty.DOUBLE);
                // _basicLattice.addEdge(BaseProperty.NIL, BaseProperty.LONG);
                // _basicLattice.addEdge(BaseProperty.NIL, BaseProperty.INT);
                _basicLattice.addEdge(BaseProperty.NIL, BaseProperty.UNSIGNED_BYTE);

                */
                
                // FIXME: Replace this with an assert when we move to 1.5
                if (!_basicLattice.isLattice()) {
                    throw new InternalErrorException("ThePropertyLattice: The "
                            + "property hierarchy is not a lattice.");
                }
            }
        }

        ///////////////////////////////////////////////////////////////
        ////                      private methods                  ////

        ///////////////////////////////////////////////////////////////
        ////                     private variables                 ////
        private DirectedAcyclicGraph _basicLattice;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The infinite property lattice. */
    private static ThePropertyLattice _lattice = new ThePropertyLattice();

    /** The result cache for parts of the property lattice. */
    private static int[][] _compareCache;

}
