/* A logical equals operator.

 Copyright (c) 1997-2014 The Regents of the University of California.
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

 */
package ptolemy.actor.lib.logic;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// Equals

/**
 A logical equals operator.  This operator has one input
 multiport and one output port that is not a multiport. It will consume
 at most one token from each input channel, and compare the tokens
 using the isEqualTo() method of the Token class.  If all observed
 input tokens are equal, then the output will be a true-valued boolean
 token.  If there is not at least one token on the input channels,
 then no output is produced.
 The type of the input port is undeclared and will be resolved by the type
 resolution mechanism.  Note that all input channels must resolve to the
 same type.  The type of the output port is boolean.

 @see Token#isEqualTo(Token)
 @author John Li and Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.4
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Green (johnli)
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

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-30\" y=\"-15\" " + "width=\"60\" height=\"30\" "
                + "style=\"fill:white\"/>\n" + "<text x=\"-14\" y=\"8\""
                + "style=\"font-size:24\">==</text>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Consume at most one token from each input channel, and output
     *  the result of comparing these tokens using the isEqualTo() method
     *  of the Token class.  If the input has width 1, then the output
     *  is always true.  If the input has width 0, or there are no
     *  no input tokens available, then no output is produced.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        BooleanToken result = BooleanToken.TRUE;
        Token reference = null;
        boolean foundOne = false;

        for (int i = 0; i < input.getWidth(); i++) {
            if (!input.hasToken(i)) {
                continue;
            }

            foundOne = true;

            Token next = input.get(i);

            if (reference == null) {
                reference = next;
            } else if (!next.isEqualTo(reference).booleanValue()) {
                result = BooleanToken.FALSE;
            }
        }

        if (foundOne) {
            output.send(0, result);
        }
    }
}
