/* A polymorphic logical NOT operator.

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

@ProposedRating Red (johnli.eecs.berkeley.edu)
@AcceptedRating
*/

package ptolemy.actor.lib.logic;

import ptolemy.kernel.util.*;
import ptolemy.graph.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.actor.*;

//////////////////////////////////////////////////////////////////////////
//// LogicalNot
/**
A polymorphic logical NOT operator.
This adder has one input port and one output port, 
neither of which are multiports.
For now, the type of the input port is limited to 
BooleanToken, until a standard for handling non-booleans is 
resolved.  A BooleanToken that arrives in the <i>input</i> 
will be negated and passed to the <i>output</i> port for broadcasting.
If no input token is available at all, then no output is produced.

@author John Li
@version $Id: 
*/

public class LogicalNot extends ptolemy.actor.lib.Transformer {

    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public LogicalNot(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
	input.setMultiport(false);
        input.setTypeEquals(BaseType.BOOLEAN);
        output.setTypeEquals(BaseType.BOOLEAN);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** If there exists a token in the <i>input</i>, its negation
     *  will be passed to the <i>output</i> port for broadcast.
     *
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
	if(input.hasToken(0))
            output.broadcast(((BooleanToken)input.get(0)).not());
    }

}






