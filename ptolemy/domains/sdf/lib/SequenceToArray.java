/* Bundle a sequence of n input tokens into an ArrayToken.

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
import ptolemy.graph.Inequality;
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
//// SequenceToArray
/**
This actor bundles a certain number of input tokens into an ArrayToken.
The number of tokens to be bundled into an ArrayToken is determined
by the parameter <i>TokenConsumptionRate</i> at the input port.
<p>
This actor is polymorphic. It can accept intput of any type and will
send ArrayTokens of corresponding type.
<p>

@author Yuhong Xiong
@version $Id$
*/

public class SequenceToArray extends SDFAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SequenceToArray(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        input = new SDFIOPort(this, "input", true, false);
        output = new SDFIOPort(this, "output", false, true);

	// set the TokenConsumptionRate to default 1.
	input.setTokenConsumptionRate(1);

	// TokenProductionRate is 1.
	output.setTokenProductionRate(1);

	// set the output type to be an ArrayType.
	output.setTypeEquals(new ArrayType(BaseType.NAT));
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
            SequenceToArray newobj = (SequenceToArray)(super.clone(ws));
            newobj.input = (SDFIOPort)newobj.getPort("input");
            newobj.output = (SDFIOPort)newobj.getPort("output");
            return newobj;
        } catch (CloneNotSupportedException ex) {
            // Errors should not occur here...
            throw new InternalErrorException(
                    "Clone failed: " + ex.getMessage());
        }
    }

    /** Consume the inputs and produce the output ArrayToken.
     *  @exception IllegalActionException Not Thrown in this base class.
     */
    public void fire() throws IllegalActionException {
	int length = input.getTokenConsumptionRate();
	Token[] valueArray = new Token[length];

	for (int i = 0; i < length; i++) {
	    valueArray[i] = input.get(0);
	}
        output.send(0, new ArrayToken(valueArray));
    }

    /** Return the type constraint that the type of the elements of the
     *  output array is no less than the type of the input port.
     *  @return A list of inequalities.
     */
    public List typeConstraintList() {
	ArrayType outArrType = (ArrayType)output.getType();
	InequalityTerm elemTerm = outArrType.getElementTypeTerm();
	Inequality ineq = new Inequality(input.getTypeTerm(), elemTerm);

	List result = new LinkedList();
	result.add(ineq);
	return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
}

