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
@AcceptedRating Yellow (pwhitake@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.lib;

import ptolemy.actor.AtomicActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.Token;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Workspace;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// DotProduct
/**
Compute the dot product of two arrays. This actor has two
input ports, from which it receives two ArrayTokens. The elements of the
ArrayTokens must be of type ScalarToken. The output is the dot product of
the two arrays.
<p>
This actor requires that each input port have a token upon firing. On each
firing, it produces exactly one token, which is of type ScalarToken.

@author Jeff Tsay, Paul Whitaker
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

        // set input types to array
        input1.setTypeEquals(new ArrayType(BaseType.UNKNOWN));
        input2.setTypeEquals(new ArrayType(BaseType.UNKNOWN));

        // set the output type to be no less than the element type of the
        // input arrays.
        ArrayType input1Type = (ArrayType)input1.getType();
        InequalityTerm elementTerm1 = input1Type.getElementTypeTerm();
        output.setTypeAtLeast(elementTerm1);

        ArrayType input2Type = (ArrayType)input2.getType();
        InequalityTerm elementTerm2 = input2Type.getElementTypeTerm();
        output.setTypeAtLeast(elementTerm2);
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
     *  base class and then sets the value public variable in the new
     *  object to equal the cloned parameter in that new object.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace)
	    throws CloneNotSupportedException {
        DotProduct newObject = (DotProduct)super.clone(workspace);

        // set the type constraints
        ArrayType input1Type = (ArrayType)newObject.input1.getType();
        InequalityTerm elementTerm1 = input1Type.getElementTypeTerm();
        newObject.output.setTypeAtLeast(elementTerm1);

        ArrayType input2Type = (ArrayType)newObject.input2.getType();
        InequalityTerm elementTerm2 = input2Type.getElementTypeTerm();
        newObject.output.setTypeAtLeast(elementTerm2);
        return newObject;
    }

    /** Read an ArrayToken from each of the input ports, and output the
     *  dot product.
     *  @exception IllegalActionException If there is no director, if
     *  the input arrays have unequal widths, or if the input arrays
     *  have no elements..
     */
    public void fire() throws IllegalActionException {
        super.fire();
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
}
