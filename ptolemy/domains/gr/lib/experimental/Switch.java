/* A polymorphic switch

 Copyright (c) 1997-2001 The Regents of the University of California.
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

@ProposedRating Red (chf@eecs.berkeley.edu)
@AcceptedRating Red (chf@eecs.berkeley.edu)
*/

package ptolemy.domains.gr.lib.experimental;

import ptolemy.actor.*;
import ptolemy.actor.lib.*;
import ptolemy.data.*;
import ptolemy.data.type.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;


//////////////////////////////////////////////////////////////////////////
//// Switch
/**
A polymorphic multiplexor.

This actor has two input ports. One is a multiport, from which the
available Tokens to be chosen are received. The other input port
receives IntTokens representing the channel containing the the Token
to send to the output.  Because Tokens are immutable, the same Token
is sent without additional creation of another Token.
<p>
The input port may receive Tokens of any type.

@author C. Fong
*/

public class Switch extends Transformer {

    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public Switch(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

    	output.setMultiport(true);

        select = new TypedIOPort(this, "select");
        select.setInput(true);
        select.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Input for index of port to select. The type is IntToken. */
    public TypedIOPort select;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read a token from the select port and each channel of the input port,
     *  and output the token on the selected channel.
     *
     *  @exception IllegalActionException If there is no director, or if
     *  an input port does not have a token.
     */
    public void fire() throws IllegalActionException {

        if (select.getWidth() != 0) {
            if (select.hasToken(0)) {
                int index = (int) ((DoubleToken) select.get(0)).doubleValue();

                int width = output.getWidth();
                if (index < width) {
                    if (input.hasToken(0)) {
                        Token token = input.get(0);
                        output.send(index, token);
                    }
                }
            }
        }
    }
}

