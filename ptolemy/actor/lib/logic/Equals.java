/* A polymorphic logical equals operator.

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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (johnli@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.logic;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// Equals
/**
A polymorphic logical equals operator.  This operator has one input
multiport and one output port that is not a multiport. It will consume
exactly one token from each input channel, and compare the tokens
using the isEqualTo() method of the Token class.  If all observed
input tokens are equal, then the output will be a true-valued boolean
token.  If there is not at least one token on each input channel,
then no output is produced.
The type of the input port is undeclared and will be resolved by the type
resolution mechanism.  Note that all input channels must resolve to the
same type.  The type of the output port is boolean.

@see Token#isEqualTo(Token)
@author John Li and Edward A. Lee
@version $Id$
*/

public class Equals extends Transformer {

    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public Equals(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input.setMultiport(true);
        output.setTypeEquals(BaseType.BOOLEAN);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Consume exactly one token from each input channel, and output
     *  the result of comparing these tokens using the isEqualTo() method
     *  of the Token class.  If the input has width 1, then the output
     *  is always true.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        BooleanToken result = BooleanToken.TRUE;
        Token previous = input.get(0);
        for (int i = 1; i < input.getWidth(); i++) {
            Token next = input.get(i);
            if (!(next.isEqualTo(previous)).booleanValue()) {
                result = BooleanToken.FALSE;
            }
            previous = next;
        }
        output.broadcast(result);
    }

    /** Check that each input channel has at least one token, and if
     *  so, return the result of the superclass prefire() method.
     *  Otherwise, return false.
     *  @return True if there inputs available on all input channels.
     *  @exception IllegalActionException If the base class throws it.
     */
    public boolean prefire() throws IllegalActionException {
        // First check that each input has a token.
        for (int i = 0; i < input.getWidth(); i++) {
            if (!input.hasToken(i)) return false;
        }
        return super.prefire();
    }
}
