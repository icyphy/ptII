/* An actor that computes the dot product of two double arrays.

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

@ProposedRating Red (ctsay@eecs.berkeley.edu)
@AcceptedRating Red (ctsay@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.lib;

import ptolemy.actor.*;
import ptolemy.data.*;
import ptolemy.data.type.*;
import ptolemy.domains.sdf.kernel.*;
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
*/

public class DotProduct extends SDFAtomicActor {

    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public DotProduct(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
      	input1 = new SDFIOPort(this, "input1", true, false);
	    input1.setTypeAtMost(BaseType.DOUBLE_MATRIX);
	    input1.setTokenConsumptionRate(1);

	    input2 = new SDFIOPort(this, "input2", true, false);
	    input2.setTypeAtMost(BaseType.DOUBLE_MATRIX);
	    input2.setTokenConsumptionRate(1);

	    output = new SDFIOPort(this, "output", false, true);
	    output.setTypeEquals(BaseType.DOUBLE);
	    output.setTokenProductionRate(1);

    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Input the first matrix token. */
    public SDFIOPort input1 = null;

    /** Input for second matrix token. */
    public SDFIOPort input2 = null;

    /** Output port, which always is the double type. */
    public SDFIOPort output = null;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and sets the public variables to point to the new ports.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace ws)
	    throws CloneNotSupportedException {
        DotProduct newobj = (DotProduct) super.clone(ws);
        newobj.input1 = (SDFIOPort) newobj.getPort("input1");
        newobj.input2 = (SDFIOPort) newobj.getPort("input2");
        newobj.output = (SDFIOPort) newobj.getPort("output");
        return newobj;
    }

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
           throw new IllegalActionException("Input to DotProduct is not a matrix with one row.");
        }

        output.send(0, new DoubleToken(DoubleArrayMath.dotProduct(
                                       doubleMatrix1[0], doubleMatrix2[0])));
    }
}

