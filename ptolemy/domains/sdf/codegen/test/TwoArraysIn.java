/* An actor that reads in array tokens and outputs a double

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

@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.codegen.test;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.*;
import ptolemy.codegen.data.*;
import ptolemy.data.type.*;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;

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
*/

public class TwoArraysIn extends TypedAtomicActor {

    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public TwoArraysIn(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        input1 = new TypedIOPort(this, "input1", true, false);
        input2 = new TypedIOPort(this, "input2", true, false);
        output = new TypedIOPort(this, "output", false, true);

	/*
        // set input types to array
        input1.setTypeEquals(ArrayType.DOUBLE_ARRAY_TYPE);
        input2.setTypeEquals(ArrayType.DOUBLE_ARRAY_TYPE);
        
        output.setTypeEquals(BaseType.DOUBLE);
	*/
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

    /** Read an ArrayToken from each of the input ports, and output the 
     *  dot product.
     *  @exception IllegalActionException If there is no director, if 
     *  the input arrays have unequal widths, or if the input arrays
     *  have no elements..
     */
    public void fire() throws IllegalActionException {
        ArrayToken array1 = (ArrayToken) input1.get(0);
        //Token token1 = array1.getElement(0);
        output.broadcast(array1);
    }

    /** If both of the input ports have at least one token, return
     *  what the superclass returns (presumably true).  Otherwise return 
     *  false.
     *  @exception IllegalActionException If there is no director.
     */
    public boolean prefire() throws IllegalActionException {
        if (!input1.hasToken(0)) return false;
        if (!input2.hasToken(0)) return false;
        //return super.prefire();
	return true;
    }
}
