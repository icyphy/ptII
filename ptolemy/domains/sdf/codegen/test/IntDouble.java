/* An actor that reads in int and outputs a double

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
//// IntDouble
/**
Read in a Int, output a Double
@author Christopher Hylands
@version $Id$
*/

public class IntDouble extends TypedAtomicActor {

    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public IntDouble(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        input = new TypedIOPort(this, "input", true, false);
        output = new TypedIOPort(this, "output", false, true);
        input.setTypeEquals(BaseType.INT);
	output.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The first input port. This has type ArrayToken. The elements of
     *  the ArrayToken must be of type ScalarToken.
     */
    public TypedIOPort input = null;

    /** The output port, which has type ScalarToken.
     */
    public TypedIOPort output = null;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read an ArrayToken from one port and output the first element.
     *  @exception IllegalActionException If there is no director, if 
     *  the input arrays have unequal widths, or if the input arrays
     *  have no elements..
     */
    public void fire() throws IllegalActionException {
	
        IntToken inputToken = (IntToken)input.get(0);
	Integer inputValue = new Integer(inputToken.intValue());
	DoubleToken result = new DoubleToken(inputValue.doubleValue());

        output.send(0, result);

        //ArrayToken array1 = (ArrayToken) input.get(0);
        ////Token token1 = array1.getElement(0);
        //output.broadcast(input.get(0));

	// This works too!
        //ArrayToken array2 = (ArrayToken) input2.get(0);
        //Token token2 = array2.getElement(0);
        //output.broadcast(array2);

	// If we change the output to a double, the following
	// produces 0.0 (c:/tmp/e)
        //ArrayToken array2 = (ArrayToken) input2.get(0);
        //Token token2 = array2.getElement(0);
        //output.broadcast(token2);

	// This works if the output is double (c:/tmp/f)
        //ArrayToken array2 = (ArrayToken) input2.get(0);
        //Token token2 = array2.getElement(0);

        //output.broadcast(array2.getElement(0));

    }

    /** If both of the input ports have at least one token, return
     *  what the superclass returns (presumably true).  Otherwise return 
     *  false.
     *  @exception IllegalActionException If there is no director.
     */
    public boolean prefire() throws IllegalActionException {
        if (!input.hasToken(0)) {
	    return false;
	}
        //return super.prefire();
	return true;
    }
}
