/* Read ArrayTokens and send their elements to the output.

 Copyright (c) 1998-2000 The Regents of the University of California.
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
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.lib;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.Workspace;
import ptolemy.graph.InequalityTerm;
import ptolemy.data.Token;
import ptolemy.data.ArrayToken;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.ArrayType;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.domains.sdf.kernel.SDFAtomicActor;
import ptolemy.domains.sdf.kernel.SDFIOPort;

import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// ArrayToSequence
/**
This actor reads ArrayTokens at the input and writes the array elements
to the output. The parameter <i>TokenProductionRate</i> at the output
port must agree with the number of elements in each input ArrayToken.
If this is not true, an exception will be thrown.
<p>
This actor is polymorphic. It can accept ArrayTokens with any element
type and send out tokens corresponding to that type.
<p>

@author Yuhong Xiong
@version $Id$
*/

public class ArrayToSequence extends SDFAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ArrayToSequence(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        input = new SDFIOPort(this, "input", true, false);
        output = new SDFIOPort(this, "output", false, true);

	// TokenConsumptionRate is 1.
	input.setTokenConsumptionRate(1);

	// set the TokenProductionRate to default 1.
	output.setTokenProductionRate(1);

	// set type constraints.
	input.setTypeEquals(new ArrayType(BaseType.NAT));
	ArrayType inputType = (ArrayType)input.getType();
	InequalityTerm elemTerm = inputType.getElementTypeTerm();
	output.setTypeAtLeast(elemTerm);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The input port. */
    public SDFIOPort input;

    /** The output port. */
    public SDFIOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports and parameters.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @throw CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace ws)
	    throws CloneNotSupportedException {
        try {
            ArrayToSequence newobj = (ArrayToSequence)(super.clone(ws));
            newobj.input = (SDFIOPort)newobj.getPort("input");
            newobj.output = (SDFIOPort)newobj.getPort("output");

	    // set the type constraints
	    ArrayType inputType = (ArrayType)newobj.input.getType();
	    InequalityTerm elemTerm = inputType.getElementTypeTerm();
	    newobj.output.setTypeAtLeast(elemTerm);
            return newobj;
        } catch (CloneNotSupportedException ex) {
            // Errors should not occur here...
            throw new InternalErrorException(
                    "Clone failed: " + ex.getMessage());
        }
    }

    /** Consume the input ArrayToken and produce the outputs.
     *  @exception IllegalActionException If the number of elements in the
     *   input ArrayToken is not the same as the token production rate.
     */
    public void fire() throws IllegalActionException {
	ArrayToken token = (ArrayToken)input.get(0);
	int rate = output.getTokenProductionRate();
	if (token.length() != rate) {
	    throw new IllegalActionException("ArrayToSequence.fire: The " +
                    "number of elements in the input ArrayToken (" +
                    token.length() + ") is not the same as the token " +
                    production rate (" + rate + ").");
	}

	Token[] elements = token.arrayValue();
	for (int i = 0; i < rate; i++) {
            output.send(0, elements[i]);
	}
    }

    /** Return the type constraint that the type of the output port is no
     *  less than the type of the elements of the input array.
     *  @return A list of inequalities.
     */
    public List typeConstraintList() {
	// Override the base class implementation to not use the default
	// constraints.
	return output.typeConstraintList();
    }
}

