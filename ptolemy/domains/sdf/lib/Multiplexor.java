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

@ProposedRating Red (ctsay@eecs.berkeley.edu)
@AcceptedRating Red (ctsay@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.lib;

import ptolemy.actor.*;
import ptolemy.data.*;
import ptolemy.data.type.*;
import ptolemy.domains.sdf.kernel.*;
import ptolemy.kernel.util.*;


//////////////////////////////////////////////////////////////////////////
//// Multiplexor
/**
A polymorphic multiplexor.
This actor has two input ports. One is a multiport, from which the available
Tokens to be chosen are received. The other input port receives IntTokens
representing the channel containing the the Token to send to the output.
Because Tokens are immutable, the same Token is sent without additional creation
of another Token.
<p>
The input port may receive Tokens of any type.
<p>
@author Jeff Tsay
*/

public class Multiplexor extends SDFAtomicActor {

    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public Multiplexor(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        input = new SDFIOPort(this, "input", true, false);
    	input.setMultiport(true);
	    input.setTokenConsumptionRate(1);

	    select = new SDFIOPort(this, "select", true, false);
	    select.setTypeEquals(BaseType.INT);
	    select.setTokenConsumptionRate(1);

	    output = new SDFIOPort(this, "output", false, true);
	    output.setTypeSameAs(input);
	    output.setTokenProductionRate(1);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Input receiving the tokens to select from. This may be any type. */
    public SDFIOPort input;

    /** Input for index of port to select. The type is IntToken. */
    public SDFIOPort select;

    /** Output for sending the selected token. */
    public SDFIOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and sets the public variables to point to the new ports.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace ws)
	    throws CloneNotSupportedException {
        Multiplexor newobj = (Multiplexor) super.clone(ws);
        newobj.input = (SDFIOPort) newobj.getPort("input");
        newobj.output = (SDFIOPort) newobj.getPort("output");
        newobj.select = (SDFIOPort) newobj.getPort("select");
        return newobj;
    }

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

