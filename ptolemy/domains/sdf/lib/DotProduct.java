/* An actor that computes the dot product of two arrays.

 Copyright (c) 1997-2003 The Regents of the University of California.
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

@ProposedRating Yellow (pwhitake@eecs.berkeley.edu)
@AcceptedRating Red (acataldo@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.lib;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.MatrixToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.Token;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.MonotonicFunction;
import ptolemy.data.type.Type;
import ptolemy.data.type.TypeLattice;
import ptolemy.data.type.UnsizedMatrixType;
import ptolemy.graph.CPO;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// DotProduct
/**
Compute the dot product of two arrays or matrices. This actor has two
input ports, from which it receives two ArrayTokens or two Matrix
Tokens. The elements of the ArrayTokens or MatrixTokens must be of
type ScalarToken. The output is the dot product of the two arrays or
matrices.

<p> This actor requires that each input port have a token upon
firing. On each firing, it produces exactly one token, which is of
type ScalarToken.

@author Jeff Tsay, Paul Whitaker, Adam Cataldo
@version $Id$
@since Ptolemy II 1.0
*/

public class DotProduct extends TypedAtomicActor {

    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public DotProduct(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        input1 = new TypedIOPort(this, "input1", true, false);
        input2 = new TypedIOPort(this, "input2", true, false);
        output = new TypedIOPort(this, "output", false, true);

        // Set the type constraints.
        output.setTypeAtLeast(new PortFunction(input1, input2));

    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The first input port. This has type ArrayToken. The elements of
     *  the ArrayToken must be of type ScalarToken.
     */
    public TypedIOPort input1 = null;

    /** The second input port. This has type ArrayToken. The elements of
     *  the ArrayToken must be of type ScalarToken.
     */
    public TypedIOPort input2 = null;

    /** The output port, which has type ScalarToken.
     */
    public TypedIOPort output = null;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the type constraints.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        DotProduct newObject = (DotProduct)super.clone(workspace);
        PortFunction function =
            new PortFunction(newObject.input1, newObject.input2);
        newObject.output.setTypeAtLeast(function);
        return newObject;
    }

    /** Read a Token from each of the input ports, and output the
     *  dot product.
     *  @exception IllegalActionException If there is no director, if
     *  the input arrays have unequal widths, or if the input arrays
     *  have no elements, or if the input types.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        if ((input1.getType() instanceof ArrayType) &&
                (input2.getType() instanceof ArrayType)) {
            try {
                _arrayFire();
            }
            catch (IllegalActionException e) {
                throw e;
            }
        }
        else if ((input1.getType() instanceof UnsizedMatrixType) &&
                (input1.getType() instanceof UnsizedMatrixType)) {
            try {
                _matrixFire();
            }
            catch (IllegalActionException e) {
                throw e;
            }
        }
        else {
            throw new IllegalActionException("Invalid types");
        }
    }

    /** If both of the input ports have at least one token, return
     *  what the superclass returns (presumably true).  Otherwise return
     *  false.
     *  @exception IllegalActionException If there is no director.
     */
    public boolean prefire() throws IllegalActionException {
        if (!input1.hasToken(0) || !input2.hasToken(0)) {
            if (_debugging) {
                _debug("Called prefire(), which returns false.");
            }
            return false;
        } else {
            return super.prefire();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /*  Read an ArrayToken from each of the input ports, and output the
     *  dot product.
     *  @exception IllegalActionException If the input arrays have
     *  unequal widths or if the input arrays have no elements..
     */
    private void _arrayFire() throws IllegalActionException {
        ArrayToken token1 = (ArrayToken) input1.get(0);
        ArrayToken token2 = (ArrayToken) input2.get(0);

        Token[] array1 = token1.arrayValue();
        Token[] array2 = token2.arrayValue();

        if (array1.length != array2.length) {
            throw new IllegalActionException("Inputs to DotProduct have " +
                    "unequal lengths: " + array1.length + " and " +
                    array2.length + ".");
        }

        if (array1.length < 1) {
            throw new IllegalActionException("Inputs to DotProduct have " +
                    "no elements.");
        }

        Token dotProd = null;
        ScalarToken currentTerm;

        for (int i = 0; i < array1.length; i++) {
            currentTerm = (ScalarToken)array1[i].multiply(array2[i]);
            if (dotProd == null) {
                dotProd = currentTerm;
            } else {
                dotProd = dotProd.add(currentTerm);
            }
        }
        output.broadcast(dotProd);
    }

    /*  Read a MatrixToken from each of the input ports, and output
     *  the dot product.
     *  @exception IllegalActionException If the input matrices have
     *  unequal sizes.
     */
    public void _matrixFire() throws IllegalActionException {
        MatrixToken token1 = (MatrixToken)input1.get(0);
        MatrixToken token2 = (MatrixToken)input2.get(0);

        int columnCount1 = token1.getColumnCount();
        int rowCount1 = token1.getRowCount();
        int columnCount2 = token2.getColumnCount();
        int rowCount2 = token2.getRowCount();

        Token element1, element2, sum;

        if ((columnCount1 == columnCount2) &&
                (rowCount1 == rowCount2)) {
            sum = token1.getElementAsToken(0,0).zero();
            for (int i = 0; i < rowCount1; i += 1) {
                for (int j = 0; j < columnCount1; j += 1) {
                    element1 = token1.getElementAsToken(i, j);
                    element2 = token2.getElementAsToken(i, j);
                    sum = sum.add(element1.multiply(element2));
                }
            }
            output.send(0, sum);
        }
        else {
            String matrix1 = rowCount1 + " by " + columnCount1;
            String matrix2 = rowCount2 + " by " + columnCount2;
            throw new IllegalActionException("Tried to multiply a " +
                    matrix1 + " matrix with a " + matrix2 + " matrix");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** This class implements a function on the two input port types.
     *  f(port1, port2) ==
     *     LUB(elementType(port1), elementType(port2)) if both are matrices
     *     LUB(elementType(port1), elementType(port2)) if both are arrays
     *     UNKNOWN                                     otherwise
     *  This function's value determinese the output port type.
     */

    private class PortFunction extends MonotonicFunction {

        private PortFunction(TypedIOPort port1, TypedIOPort port2) {
            _port1 = port1;
            _port2 = port2;
        }

        ///////////////////////////////////////////////////////////////
        ////                       public inner methods            ////

        /** Return the function result.
         *  @return A Type.
         */
        public Object getValue() {
            Type type1 = _port1.getType();
            Type type2 = _port2.getType();
            if ((type1 == BaseType.UNKNOWN)
                    || (type2 == BaseType.UNKNOWN)) {
                return BaseType.UNKNOWN;
            }
            else if ((type1 instanceof ArrayType) &&
                    (type2 instanceof ArrayType)) {
                Type elType1 = ((ArrayType)type1).getElementType();
                Type elType2= ((ArrayType)type2).getElementType();
                CPO lattice = TypeLattice.lattice();
                return lattice.leastUpperBound(elType1, elType2);
            }
            else if ((type1 instanceof UnsizedMatrixType) &&
                    (type2 instanceof UnsizedMatrixType)) {
                Type elType1 =
                    ((UnsizedMatrixType)type1).getElementType();
                Type elType2 =
                    ((UnsizedMatrixType)type2).getElementType();
                CPO lattice = TypeLattice.lattice();
                return lattice.leastUpperBound(elType1, elType2);
            }
            else {
                return BaseType.UNKNOWN;
            }
        }

        /** Return the type variables in this InequalityTerm.  If
         *  neither type variable has been set, return an array of
         *  size two, containing the InequalityTerms corresponding to
         *  the port.  If exactly one is set, return the
         *  InequalityTerm corresponding to the other port (in a size
         *  one array).  If both have been set, return an empty
         *  InequalityTerm array.
         *  @return An array of InequalityTerm.
         */
        public InequalityTerm[] getVariables() {
            InequalityTerm term1 = _port1.getTypeTerm();
            InequalityTerm term2 = _port2.getTypeTerm();
            if ((term1.isSettable()) && (term2.isSettable())) {
                InequalityTerm[] array = {term1, term2};
                return array;
            }
            else if (term1.isSettable()) {
                InequalityTerm[] array = {term1};
                return array;
            }
            else if (term2.isSettable()) {
                InequalityTerm[] array = {term2};
                return array;
            }
            return (new InequalityTerm[0]);
        }

        ///////////////////////////////////////////////////////////////
        ////                       private inner variable          ////

        private TypedIOPort _port1;
        private TypedIOPort _port2;

    }

}
