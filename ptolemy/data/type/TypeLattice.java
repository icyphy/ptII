/* Type hierarchy of token classes.

 Copyright (c) 1997-2014 The Regents of the University of California.
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
package ptolemy.data.type;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import ptolemy.data.ActorToken;
import ptolemy.data.Token;
import ptolemy.graph.CPO;
import ptolemy.graph.DirectedAcyclicGraph;

///////////////////////////////////////////////////////////////////
//// TypeLattice

/**
 Type hierarchy for token classes.
 <p>
 There is exactly one instance of the type lattice. It is constructed
 once and then does not change during execution. A concurrent hash
 table is used to cache type comparison results to optimize for
 frequently occurring type comparisons.
 </p>
 <p><a href="http://java.sun.com/docs/books/jls/third_edition/html/typesValues.html">The Java Language Spec, 3rd ed.</a>
 says:
 <blockquote>
 "The integral types are byte, short, int, and long,
 whose values are 8-bit, 16-bit, 32-bit and 64-bit
 signed two's-complement integers, respectively, and
 char, whose values are 16-bit unsigned integers
 representing UTF-16 code units (3.1).

 The floating-point types are float, whose values
 include the 32-bit IEEE 754 floating-point numbers,
 and double, whose values include the 64-bit IEEE
 754 floating-point numbers"
 </blockquote>

 Thus,
 <menu>
 <li>16-bit shorts are not losslessly convertable into
 16-bit chars (unsigned integers).
 <li>32-bit ints are not losslessly convertable into
 32-bit floats.
 <li>64-bit longs are not losslessly convertable into
 64-bit doubles.
 </menu>

 @author Yuhong Xiong, Steve Neuendorffer, Marten Lohstroh
 @version $Id$
 @since Ptolemy II 0.4
 @Pt.ProposedRating Red (yuhong)
 @Pt.AcceptedRating Red
 @see ptolemy.graph.CPO
 */
public class TypeLattice {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the an instance of CPO representing the basic type
     *  lattice.
     *  @return an instance of CPO.
     */
    public static CPO basicLattice() {
        return _lattice._basicLattice;
    }

    /** Compare the types of the two specified tokens in the type lattice.
     *  This method returns one of ptolemy.graph.CPO.LOWER,
     *  ptolemy.graph.CPO.SAME, ptolemy.graph.CPO.HIGHER,
     *  ptolemy.graph.CPO.INCOMPARABLE, indicating the the type of the
     *  first argument is lower than, equal to, higher than, or
     *  incomparable with that of the second in the type hierarchy,
     *  respectively.
     *  @param token1 a Token.
     *  @param token2 a Token.
     *  @return An integer.
     */
    public static int compare(Token token1, Token token2) {
        if (token1 == null || token2 == null) {
            throw new IllegalArgumentException(
                    "TypeLattice.compare(Token, Token): "
                            + "one or both of the argument tokens is null: "
                            + " token1 = " + token1 + ", token2 = " + token2);
        }

        return compare(token1.getType(), token2.getType());
    }

    /** Compare the types of the two specified tokens in the type lattice.
     *  This method returns one of ptolemy.graph.CPO.LOWER,
     *  ptolemy.graph.CPO.SAME, ptolemy.graph.CPO.HIGHER,
     *  ptolemy.graph.CPO.INCOMPARABLE, indicating the the type of the
     *  first argument is lower than, equal to, higher than, or
     *  incomparable with that of the second in the type hierarchy,
     *  respectively.
     *  @param token a Token.
     *  @param type a Type.
     *  @return An integer.
     */
    public static int compare(Token token, Type type) {
        if (token == null) {
            throw new IllegalArgumentException(
                    "TypeLattice.compare(Token, Type): "
                            + "token argument is null");
        }

        return compare(token.getType(), type);
    }

    /** Compare the types of the two specified tokens in the type lattice.
     *  This method returns one of ptolemy.graph.CPO.LOWER,
     *  ptolemy.graph.CPO.SAME, ptolemy.graph.CPO.HIGHER,
     *  ptolemy.graph.CPO.INCOMPARABLE, indicating the the type of the
     *  first argument is lower than, equal to, higher than, or
     *  incomparable with that of the second in the type hierarchy,
     *  respectively.
     *  @param token a Token.
     *  @param type a Type.
     *  @return An integer.
     */
    public static int compare(Type type, Token token) {
        if (token == null) {
            throw new IllegalArgumentException(
                    "TypeLattice.compare(Type, Token): "
                            + "token argument is null");
        }

        return compare(type, token.getType());
    }

    /** Compare two types in the type lattice.
     *  This method returns one of ptolemy.graph.CPO.LOWER,
     *  ptolemy.graph.CPO.SAME, ptolemy.graph.CPO.HIGHER,
     *  ptolemy.graph.CPO.INCOMPARABLE, indicating the first argument
     *  is lower than, equal to, higher than, or incomparable with the
     *  second argument in the type hierarchy, respectively.
     *  @param type1 an instance of Type.
     *  @param type2 an instance of Type.
     *  @return An integer.
     */
    public static int compare(Type type1, Type type2) {
        return _lattice.compare(type1, type2);
    }

    /** Return the an instance of CPO representing the infinite type
     *  lattice.
     *  @return an instance of CPO.
     */
    public static CPO lattice() {
        return _lattice;
    }

    /** Return the least upper bound of the two given types.
     *  @param type1 The first given type.
     *  @param type2 The second given type.
     *  @return The least upper bound of type1 and type2.
     */
    public static Type leastUpperBound(Type type1, Type type2) {
        return (Type) _lattice.leastUpperBound(type1, type2);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////

    // The infinite type lattice
    private static class TheTypeLattice implements CPO<Object> {
        /** Return the bottom element of the type lattice, which is UNKNOWN.
         *  @return The Type object representing UNKNOWN.
         */
        @Override
        public Object bottom() {
            return _basicLattice.bottom();
        }

        /** Compare two types in the type lattice. The arguments must be
         *  instances of Type, otherwise an exception will be thrown.
         *  This method returns one of ptolemy.graph.CPO.LOWER,
         *  ptolemy.graph.CPO.SAME, ptolemy.graph.CPO.HIGHER,
         *  ptolemy.graph.CPO.INCOMPARABLE, indicating the first argument
         *  is lower than, equal to, higher than, or incomparable with the
         *  second argument in the type hierarchy, respectively.
         *  @param t1 an instance of Type.
         *  @param t2 an instance of Type.
         *  @return An integer.
         *  @exception IllegalArgumentException If one or both arguments
         *   are not instances of Type.
         */
        @Override
        public int compare(Object t1, Object t2) {
            if (!(t1 instanceof Type) || !(t2 instanceof Type)) {
                throw new IllegalArgumentException("TheTypeLattice.compare: "
                        + "Arguments are not instances of Type: " + " type1 = "
                        + t1 + ", type2 = " + t2);
            }
            // System.out.println("compare " + type1 + " and " + type2 + " = " + _lattice.compare(type1, type2));

            if (t1 == t2) {
                return SAME;
            }
            Integer val;
            StringBuilder key = new StringBuilder(((Type) t1).toString());
            key.append("<");
            key.append(((Type) t2).toString());

            // Uncommment the false below to measure the impact of
            // _lattice.compare() on ptolemy.data package performance... Run
            // ptolemy/data/type/test/performance.xml before and after...(zk)
            if (//false &&
                    (val = _getCachedTypeComparisonResult(key.toString())) != null) {
                return val;
            }

            Type ct1 = (Type) t1;
            Type ct2 = (Type) t2;

            Type t1Rep = _toRepresentative(ct1);
            Type t2Rep = _toRepresentative(ct2);

            int result = INCOMPARABLE;
            if (t1Rep.equals(t2Rep) && t1Rep instanceof StructuredType) {
                result = ((StructuredType) t1)._compare((StructuredType) t2);
            } else if (t1Rep instanceof ArrayType
                    && !(t2Rep instanceof ArrayType)
                    && !t2.equals(BaseType.UNKNOWN)
                    && !t2.equals(BaseType.GENERAL)
                    && !t2.equals(BaseType.ARRAY_BOTTOM)) {
                // NOTE: Added by EAL, 7/16/06, to make scalar < {scalar}
                ArrayType arrayType = (ArrayType) t1;
                if (arrayType.hasKnownLength() && arrayType.length() != 1) {
                    // If we have a Const with {1,2,3} -> Display
                    // then we used to fail here.
                    result = INCOMPARABLE;
                } else {
                    int elementComparison = compare(
                            ((ArrayType) ct1).getElementType(), t2Rep);
                    if (elementComparison == SAME
                            || elementComparison == HIGHER) {
                        result = HIGHER;
                    } else {
                        if (t2Rep == BaseType.GENERAL) {
                            result = LOWER;
                        } else {
                            result = INCOMPARABLE;
                        }
                    }
                }
            } else if (t2Rep instanceof ArrayType
                    && !(t1Rep instanceof ArrayType)
                    && !t1.equals(BaseType.UNKNOWN)
                    && !t1.equals(BaseType.GENERAL)
                    && !t1.equals(BaseType.ARRAY_BOTTOM)) {
                // NOTE: Added by EAL, 7/16/06, to make scalar < {scalar}
                ArrayType arrayType = (ArrayType) t2;
                if (arrayType.hasKnownLength() && arrayType.length() != 1
                        && !t1.equals(BaseType.GENERAL)) {
                    result = INCOMPARABLE;
                } else {
                    int elementComparison = compare(
                            ((ArrayType) ct2).getElementType(), t1Rep);
                    if (elementComparison == SAME
                            || elementComparison == HIGHER) {
                        result = LOWER;
                    } else {
                        if (t1Rep == BaseType.GENERAL) {
                            result = HIGHER;
                        } else {
                            result = INCOMPARABLE;
                        }
                    }
                }
            } else if (_basicLattice.containsNodeWeight(t1Rep)
                    && _basicLattice.containsNodeWeight(t2Rep)) {
                // Both are neither the same structured type, nor an array
                // and non-array pair, so their type relation is defined
                // by the basic lattice.
                result = _basicLattice.compare(t1Rep, t2Rep);
            } else {
                // Both arguments are not the same structured type, and
                // at least one is user defined, so their relation is
                // rather simple.
                if (t1Rep.equals(t2Rep)) {
                    result = SAME;
                } else if (t1Rep == BaseType.UNKNOWN
                        || t2Rep == BaseType.GENERAL) {
                    result = LOWER;
                } else if (t2Rep == BaseType.UNKNOWN
                        || t1Rep == BaseType.GENERAL) {
                    result = HIGHER;
                } else {
                    result = INCOMPARABLE;
                }
            }

            _setCachedTypeComparisonResult(key.toString(), result);

            return result;
        }

        /** Throw an exception. This operation is not supported since the
         *  type lattice is infinite,
         *  @exception UnsupportedOperationException Always thrown.
         */
        @Override
        public Object[] downSet(Object e) {
            throw new UnsupportedOperationException(
                    "TheTypeLattice.downSet(): operation not supported for "
                            + "the type lattice.");
        }

        /** Return the greatest lower bound of two types.
         *  @param t1 an instance of Type.
         *  @param t2 an instance of Type.
         *  @return an instance of Type.
         *  @exception IllegalArgumentException If one or both of the
         *   specified arguments are not instances of Type.
         */
        @Override
        public Object greatestLowerBound(Object t1, Object t2) {
            if (!(t1 instanceof Type) || !(t2 instanceof Type)) {
                throw new IllegalArgumentException(
                        "TheTypeLattice.greatestLowerBound: "
                                + "Arguments are not instances of Type.");
            }

            Type ct1 = (Type) t1;
            Type ct2 = (Type) t2;

            Type t1Rep = _toRepresentative(ct1);
            Type t2Rep = _toRepresentative(ct2);

            if (t1Rep.equals(t2Rep) && t1Rep instanceof StructuredType) {
                return ((StructuredType) t1)
                        ._greatestLowerBound((StructuredType) t2);
            } else if (t1Rep instanceof ArrayType
                    && !(t2Rep instanceof ArrayType)
                    && !t2.equals(BaseType.UNKNOWN)
                    && !t2.equals(BaseType.ARRAY_BOTTOM)) {
                // NOTE: Added by EAL, 7/16/06, to make scalar < {scalar}
                ArrayType arrayType = (ArrayType) t1;
                int elementComparison = compare(
                        ((ArrayType) ct1).getElementType(), t2Rep);
                if (elementComparison == SAME || elementComparison == HIGHER) {
                    if (arrayType.hasKnownLength() && arrayType.length() != 1) {
                        return BaseType.UNKNOWN;
                    } else {
                        return t2;
                    }
                } else {
                    if (t2Rep == BaseType.GENERAL) {
                        return t1;
                    } else {
                        // INCOMPARABLE
                        if (_basicLattice.containsNodeWeight(t2Rep)) {
                            return _basicLattice.greatestLowerBound(t1Rep,
                                    t2Rep);
                        } else {
                            // t2 is a user type (has no representative in the
                            // basic lattice). Arrays of this type are not supported.
                            return BaseType.UNKNOWN;
                        }
                    }
                }
            } else if (t2Rep instanceof ArrayType
                    && !(t1Rep instanceof ArrayType)
                    && !t1.equals(BaseType.UNKNOWN)
                    && !t1.equals(BaseType.ARRAY_BOTTOM)) {
                // NOTE: Added by EAL, 7/16/06, to make scalar < {scalar}
                ArrayType arrayType = (ArrayType) t2;
                int elementComparison = compare(
                        ((ArrayType) ct2).getElementType(), t1Rep);
                if (elementComparison == SAME || elementComparison == HIGHER) {
                    if (arrayType.hasKnownLength() && arrayType.length() != 1) {
                        return BaseType.UNKNOWN;
                    } else {
                        return t1;
                    }
                } else {
                    if (t1Rep == BaseType.GENERAL) {
                        return t2;
                    } else {
                        // INCOMPARABLE
                        if (_basicLattice.containsNodeWeight(t1Rep)) {
                            return _basicLattice.greatestLowerBound(t1Rep,
                                    t2Rep);
                        } else {
                            // t1 is a user type (has no representative in the
                            // basic lattice). Arrays of this type are not supported.
                            return BaseType.UNKNOWN;
                        }
                    }
                }
            } else if (_basicLattice.containsNodeWeight(t1Rep)
                    && _basicLattice.containsNodeWeight(t2Rep)) {
                // Both are neither the same structured type, nor an array
                // and non-array pair, so their type relation is defined
                // by the basic lattice.
                int relation = _basicLattice.compare(t1Rep, t2Rep);

                if (relation == SAME) {
                    return t1;
                } else if (relation == LOWER) {
                    return t1;
                } else if (relation == HIGHER) {
                    return t2;
                } else { // INCOMPARABLE
                    return _basicLattice.greatestLowerBound(t1Rep, t2Rep);
                }
            } else {
                // Both arguments are not the same structured type, and
                // at least one is user defined, so their relation is
                // rather simple.
                if (t1Rep.equals(t2Rep)) {
                    return t1;
                } else if (t1Rep == BaseType.UNKNOWN
                        || t2Rep == BaseType.GENERAL) {
                    return t1;
                } else if (t2Rep == BaseType.UNKNOWN
                        || t1Rep == BaseType.GENERAL) {
                    return t2;
                } else {
                    return bottom();
                }
            }
        }

        /** Return the greatest lower bound of a subset.
         *  @param subset a set of Types.
         *  @return an instance of Type.
         */
        @Override
        public Object greatestLowerBound(Set<Object> subset) {
            if (subset.size() == 0) {
                return BaseType.GENERAL;
            }

            Iterator<?> itr = subset.iterator();
            Object glb = itr.next();

            // start looping from index 0 so that subset[0] is checked for
            // possible exception, in case the subset has only one element.
            while (itr.hasNext()) {
                glb = greatestLowerBound(glb, itr.next());
            }

            return glb;
        }

        /** Return the greatest type of a set of types, or null if the
         *  greatest one does not exist.
         *
         *  Note, that this only returns an element within the subset.
         *  To find the least upper bound of a set, see
         *  {@link #leastUpperBound(Set)}.
         *
         *  @param subset a set of Types.
         *  @return A Type or null.
         */
        @Override
        public Object greatestElement(Set<Object> subset) {
            // Compare each element with all of the other elements to search
            // for the greatest one. This is a simple, brute force algorithm,
            // but may be inefficient. A more efficient one is used in
            // the graph package, but more complex.
            for (Object o1 : subset) {
                boolean isGreatest = true;

                for (Object o2 : subset) {
                    int result = compare(o1, o2);

                    if (result == CPO.LOWER || result == CPO.INCOMPARABLE) {
                        isGreatest = false;
                        break;
                    }
                }

                if (isGreatest == true) {
                    return o1;
                }
            }
            // Otherwise, the subset does not contain a greatest element.
            return null;
        }

        /** Return true.
         *  @return true.
         */
        @Override
        public boolean isLattice() {
            return true;
        }

        /** Return the least type of a set of types, or null if the
         *  least one does not exist.
         *
         *  Note, that this only returns an element within the subset.
         *  To find the greatest lower bound of a set, see
         *  {@link #greatestLowerBound(Set)}.
         *
         *  @param subset a set of Types.
         *  @return A Type or null.
         */
        @Override
        public Object leastElement(Set<Object> subset) {
            // Compare each element with all of the other elements to search
            // for the least one. This is a simple, brute force algorithm,
            // but may be inefficient. A more efficient one is used in
            // the graph package, but more complex.
            for (Object o1 : subset) {
                boolean isLeast = true;

                for (Object o2 : subset) {
                    int result = compare(o1, o2);

                    if (result == CPO.HIGHER || result == CPO.INCOMPARABLE) {
                        isLeast = false;
                        break;
                    }
                }

                if (isLeast == true) {
                    return o1;
                }
            }
            // Otherwise, the subset does not contain a least element.
            return null;
        }

        /** Return the least upper bound of two types.
         *  @param t1 an instance of Type.
         *  @param t2 an instance of Type.
         *  @return an instance of Type.
         */
        @Override
        public Object leastUpperBound(Object t1, Object t2) {
            if (!(t1 instanceof Type) || !(t2 instanceof Type)) {
                throw new IllegalArgumentException(
                        "TheTypeLattice.leastUpperBound: "
                                + "Arguments are not instances of Type.");
            }

            // System.out.println("LUB of " + t1 + " and " + t2);
            Type ct1 = (Type) t1;
            Type ct2 = (Type) t2;

            Type t1Rep = _toRepresentative(ct1);
            Type t2Rep = _toRepresentative(ct2);

            if (t1Rep.equals(t2Rep) && t1Rep instanceof StructuredType) {
                return ((StructuredType) t1)
                        ._leastUpperBound((StructuredType) t2);
            } else if (t1Rep instanceof ArrayType
                    && !(t2Rep instanceof ArrayType)
                    && !t2.equals(BaseType.UNKNOWN)
                    && !t2.equals(BaseType.GENERAL)
                    && !t2.equals(BaseType.ARRAY_BOTTOM)) {
                // NOTE: Added by EAL, 7/16/06, to make scalar < {scalar}
                ArrayType arrayType = (ArrayType) t1;
                Type elementType = ((ArrayType) ct1).getElementType();
                int elementComparison = compare(elementType, t2Rep);
                if (elementComparison == SAME || elementComparison == HIGHER) {
                    if (arrayType.hasKnownLength() && arrayType.length() != 1) {
                        // Least upper bound is unsized type.
                        return new ArrayType(elementType);
                    } else {
                        return t1;
                    }
                } else {
                    if (t2Rep == BaseType.GENERAL) {
                        return t2;
                    } else {
                        // INCOMPARABLE
                        if (_basicLattice.containsNodeWeight(t2Rep)
                                && _basicLattice
                                .containsNodeWeight(elementType)) {
                            // The least upper bound is an array of the LUB
                            // of t2Rep and the element type of t1.
                            return new ArrayType(
                                    (Type) _basicLattice.leastUpperBound(
                                            elementType, t2Rep));
                        } else {
                            // t2 is a user type (has no representative in the
                            // basic lattice). Arrays of this type are not supported.
                            return BaseType.GENERAL;
                        }
                    }
                }
            } else if (t1.equals(BaseType.ARRAY_BOTTOM)
                    && !(t2Rep instanceof ArrayType)
                    && !t2.equals(BaseType.UNKNOWN)
                    && !t2.equals(BaseType.GENERAL)
                    && !t2.equals(BaseType.ARRAY_BOTTOM)) {
                // NOTE: Added by EAL, 10/8/12, to make lub(arrayBottom, double) = {double}
                // INCOMPARABLE
                if (_basicLattice.containsNodeWeight(t2Rep)) {
                    // The least upper bound is an array of t2Rep.
                    return new ArrayType(t2Rep);
                } else {
                    // t2 is a user type (has no representative in the
                    // basic lattice). Arrays of this type are not supported.
                    return BaseType.GENERAL;
                }
            } else if (t2Rep instanceof ArrayType
                    && !(t1Rep instanceof ArrayType)
                    && !t1.equals(BaseType.UNKNOWN)
                    && !t1.equals(BaseType.GENERAL)
                    && !t1.equals(BaseType.ARRAY_BOTTOM)) {
                // NOTE: Added by EAL, 7/16/06, to make scalar < {scalar}
                ArrayType arrayType = (ArrayType) t2;
                Type elementType = ((ArrayType) ct2).getElementType();
                int elementComparison = compare(elementType, t1Rep);
                if (elementComparison == SAME || elementComparison == HIGHER) {
                    if (arrayType.hasKnownLength() && arrayType.length() != 1) {
                        // Least upper bound is unsized type.
                        return new ArrayType(elementType);
                    } else {
                        return t2;
                    }
                } else {
                    if (t1Rep == BaseType.GENERAL) {
                        return t1;
                    } else {
                        // INCOMPARABLE
                        if (_basicLattice.containsNodeWeight(t1Rep)
                                && _basicLattice
                                .containsNodeWeight(elementType)) {
                            // The least upper bound is an array of the LUB
                            // of t2Rep and the element type of t1.
                            return new ArrayType(
                                    (Type) _basicLattice.leastUpperBound(
                                            elementType, t1Rep));
                        } else {
                            // t1 is a user type (has no representative in the
                            // basic lattice). Arrays of this type are not supported.
                            return BaseType.GENERAL;
                        }
                    }
                }
            } else if (t2.equals(BaseType.ARRAY_BOTTOM)
                    && !(t1Rep instanceof ArrayType)
                    && !t1.equals(BaseType.UNKNOWN)
                    && !t1.equals(BaseType.GENERAL)
                    && !t1.equals(BaseType.ARRAY_BOTTOM)) {
                // NOTE: Added by EAL, 10/8/12, to make lub(double, arrayBottom) = {double}
                // INCOMPARABLE
                if (_basicLattice.containsNodeWeight(t1Rep)) {
                    // The least upper bound is an array of t2Rep.
                    return new ArrayType(t1Rep);
                } else {
                    // t2 is a user type (has no representative in the
                    // basic lattice). Arrays of this type are not supported.
                    return BaseType.GENERAL;
                }
            } else if (_basicLattice.containsNodeWeight(t1Rep)
                    && _basicLattice.containsNodeWeight(t2Rep)) {
                // Both are neither the same structured type, nor an array
                // and non-array pair, so their type relation is defined
                // by the basic lattice.
                int relation = _basicLattice.compare(t1Rep, t2Rep);

                if (relation == SAME) {
                    return t1;
                } else if (relation == LOWER) {
                    return t2;
                } else if (relation == HIGHER) {
                    return t1;
                } else { // INCOMPARABLE
                    return _basicLattice.leastUpperBound(t1Rep, t2Rep);
                }
            } else {
                // Both arguments are not the same structured type, and
                // at least one is user defined, so their relation is
                // rather simple.
                if (t1Rep.equals(t2Rep)) {
                    return t1;
                } else if (t1Rep == BaseType.UNKNOWN
                        || t2Rep == BaseType.GENERAL) {
                    return t2;
                } else if (t2Rep == BaseType.UNKNOWN
                        || t1Rep == BaseType.GENERAL) {
                    return t1;
                } else {
                    return top();
                }
            }
        }

        /** Return the least upper bound of a subset.
         *  @param subset a set of Types.
         *  @return an instance of Type.
         */
        @Override
        public Object leastUpperBound(Set<Object> subset) {
            if (subset.size() == 0) {
                return BaseType.UNKNOWN;
            }

            Iterator<?> itr = subset.iterator();
            Object lub = itr.next();

            // start looping from index 0 so that subset[0] is checked for
            // possible exception, in case the subset has only one element.
            while (itr.hasNext()) {
                lub = leastUpperBound(lub, itr.next());
            }

            return lub;
        }

        /** Return the top element of the type lattice, which is General.
         *  @return The Type object representing General.
         */
        @Override
        public Object top() {
            return _basicLattice.top();
        }

        /** Throw an exception. This operation is not supported since the
         *  type lattice is infinite,
         *  this operation is not supported.
         *  @exception UnsupportedOperationException Always thrown.
         */
        @Override
        public Object[] upSet(Object e) {
            throw new UnsupportedOperationException(
                    "TheTypeLattice.upSet(): operation not supported for "
                            + "the type lattice.");
        }

        ///////////////////////////////////////////////////////////////
        ////                    private constructor                ////
        // the constructor is private so only the outer class can use it.
        private TheTypeLattice() {
            _basicLattice = new DirectedAcyclicGraph();

            StructuredType arrayRep = new ArrayType(BaseType.UNKNOWN)
            ._getRepresentative();

            String[] labels = new String[0];
            Type[] types = new Type[0];
            StructuredType recordRep = new RecordType(labels, types)
            ._getRepresentative();
            StructuredType unionRep = new UnionType(labels, types)
            ._getRepresentative();

            // FindBugs: Return value of FunctionType._getRepresentative() ignored, but method has no side effect
            /*StructuredType functionRep = *//*new ptolemy.data.type.FunctionType(
                    new ptolemy.data.type.Type[0],
                    ptolemy.data.type.BaseType.UNKNOWN)._getRepresentative();*/

            _basicLattice.addNodeWeight(BaseType.BOOLEAN);
            _basicLattice.addNodeWeight(BaseType.BOOLEAN_MATRIX);
            _basicLattice.addNodeWeight(BaseType.UNSIGNED_BYTE);
            _basicLattice.addNodeWeight(BaseType.COMPLEX);
            _basicLattice.addNodeWeight(BaseType.COMPLEX_MATRIX);
            _basicLattice.addNodeWeight(BaseType.DOUBLE);
            _basicLattice.addNodeWeight(BaseType.DOUBLE_MATRIX);
            _basicLattice.addNodeWeight(BaseType.UNSIZED_FIX);
            _basicLattice.addNodeWeight(BaseType.SIZED_FIX);
            _basicLattice.addNodeWeight(BaseType.FIX_MATRIX);
            _basicLattice.addNodeWeight(BaseType.FLOAT);
            _basicLattice.addNodeWeight(BaseType.INT);
            _basicLattice.addNodeWeight(BaseType.INT_MATRIX);
            _basicLattice.addNodeWeight(BaseType.LONG);
            _basicLattice.addNodeWeight(BaseType.LONG_MATRIX);
            _basicLattice.addNodeWeight(BaseType.MATRIX);
            _basicLattice.addNodeWeight(BaseType.UNKNOWN);
            // NOTE: Removed NUMERICAL from the type lattice, EAL 7/22/06.
            // _basicLattice.addNodeWeight(BaseType.NUMERICAL);
            _basicLattice.addNodeWeight(BaseType.OBJECT);
            _basicLattice.addNodeWeight(BaseType.DATE);

            // Strange bug here, see moml/test/aJVMBug.xml
            // and ptdevel email from 7/21.
            //_basicLattice.addNodeWeight(BaseType.ACTOR);
            _basicLattice.addNodeWeight(ActorToken.TYPE);

            _basicLattice.addNodeWeight(BaseType.XMLTOKEN);
            _basicLattice.addNodeWeight(BaseType.SCALAR);
            _basicLattice.addNodeWeight(BaseType.SHORT);
            _basicLattice.addNodeWeight(BaseType.STRING);
            _basicLattice.addNodeWeight(BaseType.EVENT);
            _basicLattice.addNodeWeight(BaseType.GENERAL);
            _basicLattice.addNodeWeight(BaseType.PETITE);
            _basicLattice.addNodeWeight(BaseType.NIL);

            _basicLattice.addNodeWeight(arrayRep);
            _basicLattice.addNodeWeight(BaseType.ARRAY_BOTTOM);
            _basicLattice.addNodeWeight(recordRep);
            _basicLattice.addNodeWeight(unionRep);

            _basicLattice.addEdge(BaseType.XMLTOKEN, BaseType.GENERAL);
            _basicLattice.addEdge(BaseType.UNKNOWN, BaseType.XMLTOKEN);
            _basicLattice.addEdge(BaseType.OBJECT, BaseType.GENERAL);
            _basicLattice.addEdge(BaseType.UNKNOWN, BaseType.OBJECT);
            _basicLattice.addEdge(BaseType.DATE, BaseType.STRING);
            // Allowing a conversion from STRING to a DATE would make the lattice cyclic
            //_basicLattice.addEdge(BaseType.STRING, BaseType.DATE);
            _basicLattice.addEdge(BaseType.UNKNOWN, BaseType.DATE);

            // More of the strange jvm bug, see above.
            //_basicLattice.addEdge(BaseType.ACTOR, BaseType.GENERAL);
            //_basicLattice.addEdge(BaseType.UNKNOWN, BaseType.ACTOR);
            _basicLattice.addEdge(ActorToken.TYPE, BaseType.GENERAL);
            _basicLattice.addEdge(BaseType.UNKNOWN, ActorToken.TYPE);

            _basicLattice.addEdge(BaseType.STRING, BaseType.GENERAL);
            _basicLattice.addEdge(BaseType.MATRIX, BaseType.STRING);
            _basicLattice.addEdge(BaseType.BOOLEAN_MATRIX, BaseType.MATRIX);
            _basicLattice.addEdge(BaseType.BOOLEAN, BaseType.BOOLEAN_MATRIX);
            _basicLattice.addEdge(BaseType.BOOLEAN, BaseType.SCALAR);
            _basicLattice.addEdge(BaseType.UNKNOWN, BaseType.BOOLEAN);

            // NOTE: Removed NUMERICAL from the type lattice, EAL 7/22/06.
            // _basicLattice.addEdge(BaseType.NUMERICAL, BaseType.MATRIX);
            _basicLattice.addEdge(BaseType.FIX_MATRIX, BaseType.MATRIX);
            _basicLattice.addEdge(BaseType.SCALAR, BaseType.MATRIX);
            _basicLattice.addEdge(BaseType.LONG_MATRIX, BaseType.MATRIX);
            _basicLattice.addEdge(BaseType.COMPLEX_MATRIX, BaseType.MATRIX);

            _basicLattice.addEdge(BaseType.UNSIZED_FIX, BaseType.FIX_MATRIX);
            _basicLattice.addEdge(BaseType.SIZED_FIX, BaseType.UNSIZED_FIX);
            _basicLattice.addEdge(BaseType.UNSIZED_FIX, BaseType.SCALAR);
            _basicLattice.addEdge(BaseType.UNKNOWN, BaseType.SIZED_FIX);
            _basicLattice.addEdge(BaseType.LONG, BaseType.SCALAR);
            _basicLattice.addEdge(BaseType.LONG, BaseType.LONG_MATRIX);
            _basicLattice.addEdge(BaseType.INT_MATRIX, BaseType.LONG_MATRIX);
            _basicLattice.addEdge(BaseType.INT, BaseType.LONG);
            _basicLattice.addEdge(BaseType.INT, BaseType.INT_MATRIX);
            _basicLattice.addEdge(BaseType.UNKNOWN, BaseType.UNSIGNED_BYTE);
            _basicLattice.addEdge(BaseType.UNKNOWN, BaseType.PETITE);
            _basicLattice.addEdge(BaseType.INT_MATRIX, BaseType.DOUBLE_MATRIX);
            _basicLattice.addEdge(BaseType.DOUBLE_MATRIX,
                    BaseType.COMPLEX_MATRIX);
            _basicLattice.addEdge(BaseType.DOUBLE, BaseType.DOUBLE_MATRIX);
            _basicLattice.addEdge(BaseType.INT, BaseType.DOUBLE);
            _basicLattice.addEdge(BaseType.DOUBLE, BaseType.SCALAR);

            _basicLattice.addEdge(BaseType.PETITE, BaseType.DOUBLE);
            _basicLattice.addEdge(BaseType.COMPLEX, BaseType.SCALAR);
            _basicLattice.addEdge(BaseType.COMPLEX, BaseType.COMPLEX_MATRIX);

            _basicLattice.addEdge(BaseType.DOUBLE, BaseType.COMPLEX);
            _basicLattice.addEdge(BaseType.UNSIGNED_BYTE, BaseType.SHORT);
            _basicLattice.addEdge(BaseType.SHORT, BaseType.INT);
            _basicLattice.addEdge(BaseType.SHORT, BaseType.FLOAT);
            _basicLattice.addEdge(BaseType.FLOAT, BaseType.DOUBLE);

            _basicLattice.addEdge(BaseType.EVENT, BaseType.GENERAL);
            _basicLattice.addEdge(BaseType.UNKNOWN, BaseType.EVENT);

            // NOTE: Below used to add an edge to BaseType.STRING, but
            // other concrete array types are not < STRING (see compare methods above).
            // EAL 10/8/12.
            _basicLattice.addEdge(arrayRep, BaseType.GENERAL);
            _basicLattice.addEdge(BaseType.ARRAY_BOTTOM, arrayRep);
            _basicLattice.addEdge(BaseType.UNKNOWN, BaseType.ARRAY_BOTTOM);

            _basicLattice.addEdge(recordRep, BaseType.GENERAL);
            _basicLattice.addEdge(BaseType.UNKNOWN, recordRep);

            _basicLattice.addEdge(unionRep, BaseType.GENERAL);
            _basicLattice.addEdge(BaseType.UNKNOWN, unionRep);

            _basicLattice.addEdge(BaseType.UNKNOWN, BaseType.NIL);
            _basicLattice.addEdge(BaseType.NIL, BaseType.BOOLEAN);
            // NOTE: Redundant, given edge to UnsignedByte
            // _basicLattice.addEdge(BaseType.NIL, BaseType.DOUBLE);
            // _basicLattice.addEdge(BaseType.NIL, BaseType.LONG);
            // _basicLattice.addEdge(BaseType.NIL, BaseType.INT);
            _basicLattice.addEdge(BaseType.NIL, BaseType.UNSIGNED_BYTE);

            assert _basicLattice.isLattice();
        }

        ///////////////////////////////////////////////////////////////
        ////                      private methods                  ////

        /** Return the result for the types that have the given two
         * indexes as hashes.
         */
        private static final Integer _getCachedTypeComparisonResult(String key) {
            return _compareCache.get(key);
        }

        /** Set the result for the types that have the given two
         *  indexes as hashes.
         */
        private static final void _setCachedTypeComparisonResult(String key,
                int value) {
            _compareCache.put(key, value);
        }

        // If the argument is a structured type, return its representative;
        // otherwise, return the argument. In the latter case, the argument
        // is either a base type or a user defined type that is not a
        // structured type.
        private Type _toRepresentative(Type t) {
            if (t instanceof StructuredType) {
                return ((StructuredType) t)._getRepresentative();
            } else {
                return t;
            }
        }

        ///////////////////////////////////////////////////////////////
        ////                     private variables                 ////

        private DirectedAcyclicGraph _basicLattice;

        /** The result cache for parts of the type lattice. */
        private final static ConcurrentHashMap<String, Integer> _compareCache = new ConcurrentHashMap<String, Integer>();

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The infinite type lattice. */
    private static TheTypeLattice _lattice = new TheTypeLattice();
}
