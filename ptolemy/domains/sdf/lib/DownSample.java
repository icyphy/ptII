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
@AcceptedRating Red (cxh@eecs.berkeley.edu)
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
//// DownSample
/**
This actor downsamples an input stream by an integer factor by
removing tokens.  The downsample factor is given by the
<i>tokenConsumptionRate</i> parameter of the input port.
On each firing, this actor reads a number of tokens from the input
and copies only the first token to the output.
The number of tokens consumed during the firing is the same as the
<i>tokenConsumptionRate</i> parameter of the input port.
By default, this actor sets the value of this parameter to be two,
so the input sample rate is twice that of the output.
<p>
This actor is data polymorphic. It can accept any token
type on the input.
<p>

@author Steve Neuendorffer
@version $Id$
*/

public class DownSample extends SDFTransformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DownSample(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

     	// Set tokenConsumptionRate to default 2.
	input.setTokenConsumptionRate(2);

	// tokenProductionRate is 1.
	output.setTokenProductionRate(1);

        output.setTypeAtLeast(input);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Consume the first input Token and produce the same token on the output.
     *  Then consume a number of tokens from the input port
     *  so that input.tokenConsumptionRate tokens are consumed in total.
     *  All tokens after the first are discarded. If there is not
     *  enough tokens on the input,
     *  then this method throws a NoTokenException (which is a runtime
     *  exception).  This exception should not be thrown because the
     *  prefire() method checks token availability.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
	Token token = input.get(0);
        // Send the first token.
        output.send(0, token);

        // count is the number of zero tokens to create.
        int count = input.getTokenConsumptionRate() - 1;
        Token array[] = input.get(0, count);
        // ignore the other consumed tokens.
    }

    /** Return false if the number of tokens available on the input
     *  is less than the <i>tokenConsumptionRate</i> parameter of the input
     *  port.  Otherwise, return whatever the superclass returns.
     *  @return False if there are not enough input tokens to fire.
     *  @exception IllegalActionException If there is no director.
     */
    public boolean prefire() throws IllegalActionException {
        int count = input.getTokenConsumptionRate();
        if (!input.hasToken(0, count)) return false;
        else return super.prefire();
    }
}
