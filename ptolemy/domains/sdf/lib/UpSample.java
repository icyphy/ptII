/* Read ArrayTokens and send their elements to the output.

 Copyright (c) 1998-2001 The Regents of the University of California.
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
@AcceptedRating Yellow (eal@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.lib;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.Workspace;
import ptolemy.graph.InequalityTerm;
import ptolemy.data.Token;
import ptolemy.data.ArrayToken;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.ArrayType;

import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// UpSample
/**
This actor upsamples an input stream by an integer factor by inserting
tokens with value zero.  The upsample factor is given by the
<i>tokenProductionRate</i> parameter of the output port.
On each firing, this actor reads one sample from the input
and copies that token to the output.  Then it outputs a sequence of zero
tokens of the same type as the input token so that the total number
of tokens created during the firing is the same as the
<i>tokenProductionRate</i> parameter of the output port.
By default, this actor sets the value of this parameter to be two,
so the output sample rate is twice that of the input.
<p>
This actor is data polymorphic. It can accept any token
type on the input that supports the zero() method
and send out tokens corresponding to that type.
<p>

@author Steve Neuendorffer
@version $Id$
*/

public class UpSample extends SDFTransformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public UpSample(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

     	// tokenConsumptionRate is 1.
	input.setTokenConsumptionRate(1);

	// Set tokenProductionRate to default 2.
	output.setTokenProductionRate(2);

        output.setTypeAtLeast(input);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Consume the input Token and produce the same token on the output.
     *  Then create a number of zero tokens of the same type as the
     *  input token on the output port, so that output.tokenProductionRate
     *  tokens are created in total.  If there is not token on the input,
     *  then this method throws a NoTokenException (which is a runtime
     *  exception).
     *  @exception IllegalActionException If a runtime type conflict occurs.
     */
    public void fire() throws IllegalActionException {
	Token token = input.get(0);
        // Send the first token.
        output.send(0, token);

        // count is the number of zero tokens to create.
        int count = output.getTokenProductionRate() - 1;
        for(int i = 0; i < count; i++)
            output.send(0, token.zero());
    }
}
