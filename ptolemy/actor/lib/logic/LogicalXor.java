/* A polymorphic logical XOR operator.

 Copyright (c) 1997-1999 The Regents of the University of California.
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

@ProposedRating Red (johnli@cory.eecs.berkeley.edu)
@AcceptedRating
*/

package ptolemy.actor.lib;

import ptolemy.kernel.util.*;
import ptolemy.graph.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import java.util.Enumeration;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// LogicalXor
/**
A polymorphic logical XOR operator. 
This actor has a single input port, which is a multiport, and one 
output port, which is not.
For now, the type of the multiport is set to accept only 
BooleanTokens, until a standard is established to handle numeric
values and the mixing of those and booleans.
<p>
This actor is not strict. That is, it does not require that each input
channel have a token upon firing.  As long as one channel contains a 
token, output will be produced.  In the case of only one channel having a 
token, that token will be the output.  If no input tokens are available at 
all, then no output is produced.  This actor will consume as many tokens
as are available in each channel.

@author John Li
@version $Id: 
*/

public class LogicalXor extends TypedAtomicActor {

    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public LogicalXor(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
	xorPort = new TypedIOPort(this, "xorPort", true, false);
	xorPort.setMultiport(true);
        xorPort.setTypeEquals(BooleanToken.class);
	output = new TypedIOPort(this, "output", false, true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Output port.  The type is inferred from the connections.
     */
    public TypedIOPort output = null;

    /** Input for the logical XOR operation.  This is a multiport, and its
     *  type is inferred from the connections.
     */
    public TypedIOPort xorPort = null;


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and sets the public variables to point to the new ports.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     */
    public Object clone(Workspace ws) {
        try {
            LogicalXor newobj = (LogicalXor)super.clone(ws);
            newobj.xorPort = (TypedIOPort)newobj.getPort("xorPort");
            newobj.output = (TypedIOPort)newobj.getPort("output");
            return newobj;
        } catch (CloneNotSupportedException ex) {
            // Errors should not occur here...
            throw new InternalErrorException(
                    "Clone failed: " + ex.getMessage());
        }
    }

    /** If there is at least one token on the input port, the 
     *  number of TRUE token values are counted and recorded.  If 
     *  that number is odd, the value TRUE is broadcasted, otherwise 
     *  FALSE is broadcasted.  As many tokens as are available will be read.
     *
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        int numOfTrues = 0;
        
	Token value = null;
        Token trueToken = new BooleanToken(true);
	for (int i = 0; i < xorPort.getWidth(); i++) {
	    while(xorPort.hasToken(i)) {
                if(xorPort.get(i).isEqualTo(trueToken).booleanValue())
                    numOfTrues++;
	    }
	}
        if(numOfTrues % 2 == 1)
            value = trueToken;
        else
            if(numOfTrues >= 1)
                value = trueToken.zero();
	if (value != null) {
	    output.broadcast(value);
	}
    }
}


