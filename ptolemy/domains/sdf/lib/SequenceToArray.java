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

@ProposedRating Yellow (yuhong@eecs.berkeley.edu)
@AcceptedRating Yellow (neuendor@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.lib;

import ptolemy.kernel.CompositeEntity;
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

import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// SequenceToArray
/**
This actor bundles a certain number of input tokens into an ArrayToken.
The number of tokens to be bundled into an ArrayToken is determined
by the parameter <i>tokenConsumptionRate</i> at the input port.
<p>
This actor is polymorphic. It can accept intput of any type and will
send ArrayTokens of corresponding type.
<p>

@author Yuhong Xiong
@version $Id$
*/

public class SequenceToArray extends SDFTransformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SequenceToArray(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

	// default tokenConsumptionRate is 1.
	input.setTokenConsumptionRate(1);

	// tokenProductionRate is 1.
	output.setTokenProductionRate(1);

	// set the output type to be an ArrayType.
	output.setTypeEquals(new ArrayType(BaseType.UNKNOWN));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Consume the inputs and produce the output ArrayToken.
     *  @exception IllegalActionException If not enough tokens are available.
     */
    public void fire() throws IllegalActionException {
	int length = input.getTokenConsumptionRate();
	Token[] valueArray = input.get(0, length);

        output.send(0, new ArrayToken(valueArray));
    }

    /** Return true if the input port has enough tokens for this actor to
     *  fire. The number of tokens required is determined by the token
     *  consumption rate.
     *  @return boolean True if there is enough tokens at the input port
     *   for this actor to fire.
     *  @exception IllegalActionException If the hasToken() query to the
     *   input port throws it.
     *  @see ptolemy.actor.IOPort#hasToken(int, int)
     */
    public boolean prefire() throws IllegalActionException {
	int length = input.getTokenConsumptionRate();
	return input.hasToken(0, length);
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
}

