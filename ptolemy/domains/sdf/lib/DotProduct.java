/* An actor that computes the dot product of two double arrays.

 Copyright (c) 1997-2001 The Regents of the University of California.
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

@ProposedRating Yellow (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (ctsay@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.lib;

import ptolemy.actor.*;
import ptolemy.data.*;
import ptolemy.data.type.*;
import ptolemy.domains.sdf.kernel.*;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.math.DoubleArrayMath;

//////////////////////////////////////////////////////////////////////////
//// DotProduct
/**
An actor that computes the dot product of two arrays.
This actor has two input ports, from which it receives two MatrixTokens.
The output is the dot product of the two matrices, assuming that each
matrix only has one row.
<p>
This actor is strict. That is, it requires that each input
port have a token upon firing. It always sends one DoubleToken as
its output.

@author Jeff Tsay
@version $Id$
*/

public class DotProduct extends TypedAtomicActor {

    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public DotProduct(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
      	input1 = new TypedIOPort(this, "input1", true, false);
        input1.setTypeAtMost(BaseType.DOUBLE_MATRIX);

        input2 = new TypedIOPort(this, "input2", true, false);
        input2.setTypeAtMost(BaseType.DOUBLE_MATRIX);

        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The first input port. This has type MatrixToken, and should be
     *  given matrices with only one row.
     */
    public TypedIOPort input1 = null;

    /** The second input port. This has type MatrixToken, and should be
     *  given matrices with only one row.
     */
    public TypedIOPort input2 = null;

    /** The output port. This has type DoubleToken.
     */
    public TypedIOPort output = null;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read a token each of the input ports, converting them into
     *  DoubleMatrixTokens, and output a DoubleToken representing the
     *  dot product. The input matrix tokens are assumed to have only one
     *  row, representing vector operands.
     *
     *  @exception IllegalActionException If there is no director, if
     *  an input port does not have a token of the correct type, or if
     *  the input token(s) are not row vectors.
     */
    public void fire() throws IllegalActionException {
        MatrixToken token1 = (MatrixToken) input1.get(0);
        MatrixToken token2 = (MatrixToken) input2.get(0);

        double[][] doubleMatrix1 = token1.doubleMatrix();
        double[][] doubleMatrix2 = token2.doubleMatrix();

        if ((doubleMatrix1.length != 1) || (doubleMatrix2.length != 1)) {
            throw new IllegalActionException("Input to DotProduct is not " +
                    "a matrix with one row.");
        }

        output.send(0, new DoubleToken(DoubleArrayMath.dotProduct(
                doubleMatrix1[0], doubleMatrix2[0])));
    }
}

