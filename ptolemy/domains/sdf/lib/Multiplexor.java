/* A polymorphic multiplexor.

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

@ProposedRating Yellow (ctsay@eecs.berkeley.edu)
@AcceptedRating Red (ctsay@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.lib;

import ptolemy.actor.*;
import ptolemy.actor.lib.Transformer;
import ptolemy.data.*;
import ptolemy.data.type.*;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;


//////////////////////////////////////////////////////////////////////////
//// Multiplexor
/**
A type polymorphic multiplexor. 
This actor has two input ports. One is a multiport, from which the
available Tokens to be chosen are received. The other input port
receives IntTokens representing the channel containing the the Token
to send to the output.  Because Tokens are immutable, the same Token
is sent without additional creation of another Token.
<p>
This actor is useful in the SDF domain, because it always consumes a 
token from each channel of the input port, regardless of which channel 
is being selected.  This makes it safe to use under SDF.
<p>
The input port may receive Tokens of any type.

@author Jeff Tsay
@version $Id$
*/

public class Multiplexor extends Transformer {

    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public Multiplexor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

    	input.setMultiport(true);
                
        select = new TypedIOPort(this, "select", true, false);
        select.setTypeEquals(BaseType.INT);
                
        output.setTypeSameAs(input);
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
        int index = ((IntToken) select.get(0)).intValue();

        for (int i = 0; i < input.getWidth(); i++) {
            Token token = input.get(i);
            if (i == index) {
                output.send(0, token);
            }
        }
    }
}

