/* Type hierarchy of token classes.

 Copyright (c) 1997-2000 The Regents of the University of California.
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

@ProposedRating Red (yuhong@eecs.berkeley.edu)
@AcceptedRating Red
*/

package ptolemy.data.type;

import ptolemy.kernel.util.InternalErrorException;
import ptolemy.data.Token;
import ptolemy.graph.*;

//////////////////////////////////////////////////////////////////////////
//// DataTypeLattice
/**
Type hierarchy for token classes. This class implements the CPO interface.

@author Yuhong Xiong, Steve Neuendorffer
@version $Id$
@see ptolemy.graph.CPO
*/

public class TypeLattice {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Compare the types of the two specified tokens in the type lattice.
     *  This method returns one of ptolemy.graph.CPO.LOWER,
     *  ptolemy.graph.CPO.SAME, ptolemy.graph.CPO.HIGHER,
     *  ptolemy.graph.CPO.INCOMPARABLE, indicating the the type of the
     *  first argument is lower than, equal to, higher than, or
     *  incomparable with that of the second in the type hierarchy,
     *  respectively.
     *  @param t1 a Token.
     *  @param t2 a Token.
     *  @return An integer.
     */
    public static int compare(Token t1, Token t2) {
	return compare(t1.getType(), t2.getType());
    }

    /** Compare two types in the type lattice.
     *  This method returns one of ptolemy.graph.CPO.LOWER,
     *  ptolemy.graph.CPO.SAME, ptolemy.graph.CPO.HIGHER,
     *  ptolemy.graph.CPO.INCOMPARABLE, indicating the first argument
     *  is lower than, equal to, higher than, or incomparable with the
     *  second argument in the type hierarchy, respectively.
     *  @param t1 an instance of Type.
     *  @param t2 an instance of Type.
     *  @return An integer.
     */
    public static int compare(Type t1, Type t2) {
	return _lattice.compare(t1, t2);
    }

    /** Return the an instance of CPO representing the infinite type
     *  lattice.
     *  @return an instance of CPO.
     */
    public static CPO lattice() {
	return _lattice;
    }

    ///////////////////////////////////////////////////////////////////
    ////                          inner class                      ////

    // The infinite type lattice
    private static class TheTypeLattice implements CPO {

        /** Return the bottom element of the type lattice, which is NaT
         *  (not a type).
         *  @return The Type object representing NaT.
         */
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
        public int compare(Object t1, Object t2) {
	    if ( !(t1 instanceof Type) || !(t2 instanceof Type)) {
	        throw new IllegalArgumentException("TheTypeLattice.compare: "
			+ "Arguments are not instances of Type.");
	    }

	    Type ct1 = (Type)t1;
	    Type ct2 = (Type)t2;

	    if (ct1 instanceof BaseType && ct2 instanceof BaseType) {
	        return _basicLattice.compare(ct1, ct2);
	    }

	    Type t1Rep = _toRepresentative(ct1);
	    Type t2Rep = _toRepresentative(ct2);
	    if (t1Rep == t2Rep) {
	        // t1 and t2 are of the same structured type
	        return ((StructuredType)t1)._compare((StructuredType)ct2);
	    }

	    // t1 and t2 are of different structured type, or one is
	    // a base type.
	    return _basicLattice.compare(t1Rep, t2Rep);
        }

	/** Throw an exception. This operation is not supported since the
	 *  type lattice is infinite,
	 *  @exception UnsupportedOperationException Always thrown.
	 */
	public Object[] downSet(Object e) {
	    throw new UnsupportedOperationException(
                    "TheTypeLattice.downSet(): operation not supported for " +
                    "the type lattice.");
	}

        /** Return the greatest lower bound of two types.
         *  @param t1 an instance of Type.
         *  @param t2 an instance of Type.
	 *  @return an instance of Type.
	 *  @exception IllegalArgumentException If one or both of the
	 *   specified arguments are not instances of Type.
         */
        public Object greatestLowerBound(Object t1, Object t2) {
	    if ( !(t1 instanceof Type) || !(t2 instanceof Type)) {
	        throw new IllegalArgumentException(
			"TheTypeLattice.greatestLowerBound: " +
		    	"Arguments are not instances of Type.");
	    }

	    Type ct1 = (Type)t1;
	    Type ct2 = (Type)t2;

	    if (t1 instanceof BaseType && t2 instanceof BaseType) {
	        return _basicLattice.greatestLowerBound(ct1, ct2);
	    }

	    Type t1Rep = _toRepresentative(ct1);
	    Type t2Rep = _toRepresentative(ct2);
	    if (t1Rep == t2Rep) {
	        // t1 and t2 are of the same structured type
	        return ((StructuredType)ct1)._greatestLowerBound(
			(StructuredType)ct2);
	    }

	    // t1 and t2 are of different structured type, or one is
	    // a base type. If they are comparable, then one of them
	    // must be a base type.
	    int compareResult = _basicLattice.compare(t1Rep, t2Rep);
	    if (compareResult == HIGHER || compareResult == SAME) {
		return ct2;
	    } else if (compareResult == LOWER) {
		return ct1;
	    } else {
		// incomparable, GLB should be NaT.
		return _basicLattice.greatestLowerBound(t1Rep, t2Rep);
	    }
	}

        /** Return the greatest lower bound of a subset.
         *  @param subset an array of Types.
	 *  @return an instance of Type.
         */
	public Object greatestLowerBound(Object[] subset) {
	    if (subset.length == 0) {
		return BaseType.GENERAL;
	    }

	    Object glb = subset[0];

	    // start looping from index 0 so that subset[0] is checked for
	    // possible exception, in case the subset has only one element.
	    for (int i = 0; i < subset.length; i++) {
		glb = greatestLowerBound(glb, subset[i]);
	    }
	    return glb;
	}

	/** Return the greatest type of a set of types, or null if the
	 *  greatest one does not exist.
	 *  @param subset an array of Types.
	 *  @return A Type or null.
	 */
	public Object greatestElement(Object[] subset) {
	    // Compare each element with all of the other elements to search
	    // for the greatest one. This is a simple, brute force algorithm,
	    // but may be inefficient. A more efficient one is used in
	    // the graph package, but more complex.
	    for (int i = 0; i < subset.length; i++) {
	    	boolean isGreatest = true;
		for (int j = 0; j < subset.length; j++) {
		    int result = compare(subset[i], subset[j]);
		    if (result == CPO.LOWER || result == CPO.INCOMPARABLE) {
			isGreatest = false;
			break;
		    }
		}

		if (isGreatest == true) {
		    return subset[i];
		}
	    }
	    return null;
	}

	/** Return true.
	 *  @return true.
	 */
	public boolean isLattice() {
	    return true;
	}

	/** Return the least type of a set of types, or null if the
	 *  least one does not exist.
	 *  @param subset an array of Types.
	 *  @return A Type or null.
	 */
	public Object leastElement(Object[] subset) {
	    // Compare each element with all of the other elements to search
	    // for the least one. This is a simple, brute force algorithm,
	    // but may be inefficient. A more efficient one is used in
	    // the graph package, but more complex.
	    for (int i = 0; i < subset.length; i++) {
	    	boolean isLeast = true;
		for (int j = 0; j < subset.length; j++) {
		    int result = compare(subset[i], subset[j]);
		    if (result == CPO.HIGHER || result == CPO.INCOMPARABLE) {
			isLeast = false;
			break;
		    }
		}

		if (isLeast == true) {
		    return subset[i];
		}
	    }
	    return null;
	}

        /** Return the least upper bound of two types.
         *  @param t1 an instance of Type.
         *  @param t2 an instance of Type.
	 *  @return an instance of Type.
         */
        public Object leastUpperBound(Object t1, Object t2) {
	    if ( !(t1 instanceof Type) || !(t2 instanceof Type)) {
	        throw new IllegalArgumentException(
			"TheTypeLattice.leastUpperBound: " +
		    	"Arguments are not instances of Type.");
	    }

	    Type ct1 = (Type)t1;
	    Type ct2 = (Type)t2;

	    if (ct1 instanceof BaseType && ct2 instanceof BaseType) {
	        return _basicLattice.leastUpperBound(ct1, ct2);
	    }

	    Type t1Rep = _toRepresentative(ct1);
	    Type t2Rep = _toRepresentative(ct2);
	    if (t1Rep == t2Rep) {
	        // t1 and t2 are of the same structured type
	        return ((StructuredType)ct1)._leastUpperBound(
			(StructuredType)ct2);
	    }

	    // t1 and t2 are of different structured type, or one is
	    // a base type. If they are comparable, then one of them
	    // must be a base type.
	    int compareResult = _basicLattice.compare(t1Rep, t2Rep);
	    if (compareResult == HIGHER || compareResult == SAME) {
		return ct1;
	    } else if (compareResult == LOWER) {
		return ct2;
	    } else {
		// incomparable, LUB should be General
		return _basicLattice.leastUpperBound(t1Rep, t2Rep);
	    }
	}

        /** Return the least upper bound of a subset.
         *  @param subset an array of Types.
	 *  @return an instance of Type.
         */
	public Object leastUpperBound(Object[] subset) {
	    if (subset.length == 0) {
		return BaseType.NAT;
	    }

	    Object lub = subset[0];

	    // start looping from index 0 so that subset[0] is checked for
	    // possible exception, in case the subset has only one element.
	    for (int i = 0; i < subset.length; i++) {
		lub = leastUpperBound(lub, subset[i]);
	    }
	    return lub;
	}

        /** Return the top element of the type lattice, which is General.
         *  @return The Type object representing General.
         */
        public Object top() {
	    return _basicLattice.top();
        }

	/** Throw an exception. This operation is not supported since the
	 *  type lattice is infinite,
	 *  this operation is not supported.
	 *  @exception UnsupportedOperationException Always thrown.
	 */
	public Object[] upSet(Object e) {
	    throw new UnsupportedOperationException(
                    "TheTypeLattice.upSet(): operation not supported for " +
                    "the type lattice.");
	}

        ///////////////////////////////////////////////////////////////
        ////                    private constructor                ////

	// the constructor is private so only the outer class can use it.
	private TheTypeLattice() {
	    _basicLattice = new DirectedAcyclicGraph();
	    StructuredType arrayRep =
		(new ArrayType(BaseType.NAT))._getRepresentative();

	    _basicLattice.add(BaseType.BOOLEAN);
	    _basicLattice.add(BaseType.BOOLEAN_MATRIX);
	    _basicLattice.add(BaseType.COMPLEX);
	    _basicLattice.add(BaseType.COMPLEX_MATRIX);
	    _basicLattice.add(BaseType.DOUBLE);
	    _basicLattice.add(BaseType.DOUBLE_MATRIX);
	    _basicLattice.add(BaseType.FIX);
	    _basicLattice.add(BaseType.FIX_MATRIX);
	    _basicLattice.add(BaseType.INT);
	    _basicLattice.add(BaseType.INT_MATRIX);
	    _basicLattice.add(BaseType.LONG);
	    _basicLattice.add(BaseType.LONG_MATRIX);
	    _basicLattice.add(BaseType.MATRIX);
	    _basicLattice.add(BaseType.NAT);
	    _basicLattice.add(BaseType.NUMERICAL);
	    _basicLattice.add(BaseType.OBJECT);
	    _basicLattice.add(BaseType.SCALAR);
	    _basicLattice.add(BaseType.STRING);
	    _basicLattice.add(BaseType.GENERAL);

	    _basicLattice.add(arrayRep);

	    _basicLattice.addEdge(BaseType.OBJECT, BaseType.GENERAL);
	    _basicLattice.addEdge(BaseType.NAT, BaseType.OBJECT);
	    _basicLattice.addEdge(BaseType.STRING, BaseType.GENERAL);
	    _basicLattice.addEdge(BaseType.MATRIX, BaseType.STRING);
	    _basicLattice.addEdge(BaseType.BOOLEAN_MATRIX, BaseType.MATRIX);
	    _basicLattice.addEdge(BaseType.BOOLEAN, BaseType.BOOLEAN_MATRIX);
	    _basicLattice.addEdge(BaseType.NAT, BaseType.BOOLEAN);

	    _basicLattice.addEdge(BaseType.NUMERICAL, BaseType.MATRIX);
	    _basicLattice.addEdge(BaseType.FIX_MATRIX, BaseType.NUMERICAL);
	    _basicLattice.addEdge(BaseType.SCALAR, BaseType.NUMERICAL);
	    _basicLattice.addEdge(BaseType.LONG_MATRIX, BaseType.NUMERICAL);
	    _basicLattice.addEdge(BaseType.COMPLEX_MATRIX, BaseType.NUMERICAL);

	    _basicLattice.addEdge(BaseType.FIX, BaseType.FIX_MATRIX);
	    _basicLattice.addEdge(BaseType.FIX, BaseType.SCALAR);
	    _basicLattice.addEdge(BaseType.NAT, BaseType.FIX);
	    _basicLattice.addEdge(BaseType.LONG, BaseType.SCALAR);
	    _basicLattice.addEdge(BaseType.LONG, BaseType.LONG_MATRIX);
	    _basicLattice.addEdge(BaseType.INT_MATRIX, BaseType.LONG_MATRIX);
	    _basicLattice.addEdge(BaseType.INT, BaseType.LONG);
	    _basicLattice.addEdge(BaseType.INT, BaseType.INT_MATRIX);
	    _basicLattice.addEdge(BaseType.NAT, BaseType.INT);

	    _basicLattice.addEdge(BaseType.INT_MATRIX, BaseType.DOUBLE_MATRIX);
	    _basicLattice.addEdge(BaseType.DOUBLE_MATRIX,
                    BaseType.COMPLEX_MATRIX);
	    _basicLattice.addEdge(BaseType.DOUBLE, BaseType.DOUBLE_MATRIX);

	    _basicLattice.addEdge(BaseType.COMPLEX, BaseType.SCALAR);
	    _basicLattice.addEdge(BaseType.COMPLEX, BaseType.COMPLEX_MATRIX);

	    _basicLattice.addEdge(BaseType.DOUBLE, BaseType.COMPLEX);
	    _basicLattice.addEdge(BaseType.INT, BaseType.DOUBLE);

	    _basicLattice.addEdge(arrayRep, BaseType.GENERAL);
	    _basicLattice.addEdge(BaseType.NAT, arrayRep);

	    if ( !_basicLattice.isLattice()) {
		throw new InternalErrorException("TheTypeLattice: The " +
			"type hierarchy is not a lattice.");
	    }
	}

        ///////////////////////////////////////////////////////////////
        ////                      private methods                  ////

    	// If the argument is a base type, return it directly; otherwise,
        // return the representative of the structured type of the argument.
        private Type _toRepresentative(Type t) {
	    if (t instanceof BaseType) {
	        return t;
	    } else {
	        return ((StructuredType)t)._getRepresentative();
	    }
        }

        ///////////////////////////////////////////////////////////////
        ////                     private variables                 ////

	private DirectedAcyclicGraph _basicLattice;
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ////

    // The infinite type lattice.
    private static TheTypeLattice _lattice = new TheTypeLattice();

}
