/* A polymorphic logical XOR operator.

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

@ProposedRating Red (johnli@cory.eecs.berkeley.edu)
@AcceptedRating
*/

package ptolemy.actor.lib.logic;

import ptolemy.kernel.util.*;
import ptolemy.graph.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.actor.*;

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

public class LogicalXor extends ptolemy.actor.lib.Transformer {

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
	input.setMultiport(true);
        input.setTypeEquals(BaseType.BOOLEAN);
        output.setTypeEquals(BaseType.BOOLEAN);
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** If there is at least one token on the <i>input</i>, the output
     *  token will be set to the first value encountered.  The output 
     *  token will then call add() on each of the remaining input tokens
     *  and be set to the resulting value.  The add() function of a 
     *  BooleanToken is the equivalent of the logical XOR operation. 
     *
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
	Token value = null, in = null;
	for (int i = 0; i < input.getWidth(); i++) {
	    while(input.hasToken(i)) {
                in = input.get(i);
                if(value == null)
                    value = in;
                else 
                    value = value.add(in);
            }
        }
        if (value != null) 
	    output.broadcast((BooleanToken)value);
    }
}
