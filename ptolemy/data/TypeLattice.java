/** Type hierarchy of token classes.

 Copyright (c) 1997-1999 The Regents of the University of California.
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

@ProposedRating Yellow (yuhong@eecs.berkeley.edu, nsmyth@eecs.berkeley.edu)
@AcceptedRating Yellow (wbwu@eecs.berkeley.edu)

*/

package ptolemy.data;

import ptolemy.kernel.util.InternalErrorException;
import ptolemy.graph.*;

import java.lang.reflect.Modifier;
import java.lang.Void;		/* Needed for javadoc */

//////////////////////////////////////////////////////////////////////////
//// TypeLattice
/**
Type hierarchy for token classes.  The type hierarchy is a lattice
of types. A type lower in the lattice can be converted to a higher
type without loss of information.  The elements in this lattice are
instances java class <code>Class</code>.  NaT (not a type) is represented
by java.lang.Void.TYPE.  The type hierarchy is separate from the class
hierarchy.

@author Yuhong Xiong
@version $Id$
@see ptolemy.graph.CPO
@see java.lang.Void
*/

public final class TypeLattice
{
    /** Compare two types in the type lattice.
     *  Calling this method is equivalent to call
     *  TypeLattice.lattice().compare().
     *  This method returns one of ptolemy.graph.CPO.LOWER,
     *  ptolemy.graph.CPO.SAME, ptolemy.graph.CPO.HIGHER,
     *  ptolemy.graph.CPO.INCOMPARABLE, indicating the first argument
     *  is lower than, equal to, higher than, or incomparable with the
     *  second argument in the type hierarchy, respectively.
     *  @param c1 a Class representing a token type.
     *  @param c2 a Class representing a token type.
     *  @return An integer.
     *  @exception IllegalArgumentException at least one argument
     *   is not an element in the type lattice.
     */
    public static int compare(Class c1, Class c2) {
        return _typeLattice.compare(c1, c2);
    }

    /** Compares two types associated with the specified tokens
     *  in the type lattice.
     *  This method returns one of ptolemy.graph.CPO.LOWER,
     *  ptolemy.graph.CPO.SAME, ptolemy.graph.CPO.HIGHER,
     *  ptolemy.graph.CPO.INCOMPARABLE, indicating that the class
     *  representing the first argument is lower than, equal to,
     *  higher than, or incomparable with the class representing the
     *  second argument in the type hierarchy, respectively.
     *  @param t1 a Token.
     *  @param t1 a Token.
     *  @return An integer.
     */
    public static int compare(Token t1, Token t2) {
        return compare(t1.getClass(), t2.getClass());
    }

    /** Returns the type lattice. This method partially exposes the
     *  underline object representing the type lattice, so it breaks
     *  information hiding. But this is necessary since the type
     *  resolution mechanism in the actor package needs access to the
     *  underline lattice.
     *  @return a CPO modeling the type hierarchy.
     */
    public static CPO lattice() {
	return _typeLattice;
    }

    /** Test if the specified Class is an element in the type lattice
     *  other than NaT.
     *  @param c An instance of Class.
     *  @return True if the specified Class is an element in the type
     *   lattice other than NaT; false otherwise.
     */
    public static boolean isAType(Class c) {
	// _typeLattice.bottom() is NaT.
	if (_typeLattice.contains(c) && !c.equals(_typeLattice.bottom())) {
            return true;
        }
        return false;
    }

    /** Test if the specified Class is an element in the type lattice
     *  and is instantiable. The Class is instantiable if it does
     *  not correspond to an abstract token class or an interface.
     *  @param c An instance of Class.
     *  @return True if the specified Class is an element in the type
     *   lattice and does not correspond to an abstract token class, or
     *   an interface, or NaT; false otherwise.
     */
    public static boolean isInstantiableType(Class c) {
	if ( !isAType(c)) {
	    return false;
	}

	int mod = c.getModifiers();
	if (Modifier.isAbstract(mod)) {
	    return false;
	}

	if (c.isInterface()) {
	    return false;
	}

	return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // construct the lattice of types.
    private static DirectedAcyclicGraph _setup() {

        DirectedAcyclicGraph lattice = new DirectedAcyclicGraph();

        Class tGeneral = Token.class;
        Class tObject = ObjectToken.class;
        Class tString = StringToken.class;

	Class tMatrixUpperBound = MatrixUpperBound.class;

        Class tNumerical = Numerical.class;
        Class tBooleanMatrix = BooleanMatrixToken.class;
        Class tLongMatrix = LongMatrixToken.class;
        Class tComplexMatrix = ComplexMatrixToken.class;
        Class tDoubleMatrix = DoubleMatrixToken.class;
	Class tIntMatrix = IntMatrixToken.class;

	Class tMatrixLowerBound = MatrixLowerBound.class;

	Class tBoolean = BooleanToken.class;

        Class tScalar = ScalarToken.class;
	Class tLong = LongToken.class;
	Class tComplex = ComplexToken.class;
	Class tDouble = DoubleToken.class;
	Class tInt = IntToken.class;

	Class tNaT = Void.TYPE;

        lattice.add(tGeneral);
        lattice.add(tObject);

        lattice.add(tString);
	lattice.add(tMatrixUpperBound);
        lattice.add(tNumerical);
        lattice.add(tBooleanMatrix);
        lattice.add(tLongMatrix);
        lattice.add(tComplexMatrix);
        lattice.add(tDoubleMatrix);
        lattice.add(tIntMatrix);
	lattice.add(tMatrixLowerBound);
	lattice.add(tBoolean);
	lattice.add(tScalar);
	lattice.add(tLong);
	lattice.add(tComplex);
	lattice.add(tDouble);
	lattice.add(tInt);

	lattice.add(tNaT);		// NaT

        lattice.addEdge(tObject, tGeneral);
        lattice.addEdge(tNaT, tObject);

        lattice.addEdge(tString, tGeneral);
        lattice.addEdge(tMatrixUpperBound, tString);
        lattice.addEdge(tBooleanMatrix, tMatrixUpperBound);
        lattice.addEdge(tBoolean, tBooleanMatrix);
        lattice.addEdge(tNaT, tBoolean);

	lattice.addEdge(tNumerical, tMatrixUpperBound);
        lattice.addEdge(tLongMatrix, tNumerical);
        lattice.addEdge(tComplexMatrix, tNumerical);
        lattice.addEdge(tDoubleMatrix, tComplexMatrix);
        lattice.addEdge(tIntMatrix, tLongMatrix);
        lattice.addEdge(tIntMatrix, tDoubleMatrix);

        lattice.addEdge(tMatrixLowerBound, tBooleanMatrix);
        lattice.addEdge(tMatrixLowerBound, tIntMatrix);
        lattice.addEdge(tNaT, tMatrixLowerBound);

	lattice.addEdge(tScalar, tNumerical);
	lattice.addEdge(tLong, tScalar);
	lattice.addEdge(tLong, tLongMatrix);
	lattice.addEdge(tComplex, tScalar);
	lattice.addEdge(tComplex, tComplexMatrix);
	lattice.addEdge(tDouble, tDoubleMatrix);
	lattice.addEdge(tDouble, tComplex);
	lattice.addEdge(tInt, tLong);
	lattice.addEdge(tInt, tIntMatrix);
	lattice.addEdge(tInt, tDouble);

        lattice.addEdge(tNaT, tInt);

	if ( !lattice.isLattice()) {
	    throw new InternalErrorException("TypeLattice: The type " +
                    "hierarchy is not a lattice.");
	}
	return lattice;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private static DirectedAcyclicGraph _typeLattice = _setup();
}

