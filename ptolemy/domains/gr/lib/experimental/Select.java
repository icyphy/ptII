/* A polymorphic selector/multiplexer.

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
import ptolemy.domains.gr.kernel.*;
import ptolemy.kernel.util.*;


//////////////////////////////////////////////////////////////////////////
//// Select
/**
A polymorphic selector/multiplexor.

This actor has two input ports. One is a multiport, from which the
available Tokens to be chosen are received. The other input port
receives DoubleTokens representing the channel containing the the Token
to send to the output.  Because Tokens are immutable, the same Token
is sent without additional creation of another Token.
<p>
The input port may receive Tokens of any type.

@author C. Fong
@version $Id$
*/

public class Select extends TypedAtomicActor {

    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public Select(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        input = new TypedIOPort(this, "input");
        input.setInput(true);
    	input.setMultiport(true);

        select = new TypedIOPort(this, "select");
        select.setInput(true);
        select.setTypeEquals(BaseType.DOUBLE);

        output = new TypedIOPort(this, "output");
        output.setOutput(true);
        output.setTypeSameAs(input);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Input receiving the tokens to select from. This may be any type. */
    public TypedIOPort input;

    /** Input for index of port to select. The type is IntToken. */
    public TypedIOPort select;

    /** Output for sending the selected token. */
    public TypedIOPort output;

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
                double selectValue = ((DoubleToken) select.get(0)).doubleValue();
                int index = 0;
                int width = input.getWidth();

                if (width == 2 ) {
                    if (selectValue > 0 ) {
                        index = 1;
                    } else {
                        index = 0;
                    }
                } else {
                    index = (int) selectValue;
                }

                if (index < width) {
                    for (int i = 0; i < width; i++) {
                        if (input.hasToken(i)) {
                            Token token = input.get(i);
                            if (i == index) {
                                output.send(0, token);
                            }
                        }
                    }
                }
            }
        }
    }
}

