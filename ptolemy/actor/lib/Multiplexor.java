/* A polymorphic multiplexor.

 Copyright (c) 1997-2002 The Regents of the University of California.
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
@AcceptedRating Yellow (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// Multiplexor
/**
A type polymorphic multiplexor.  This actor consumes exactly one token
from each input channel of the <i>input</i> port, and sends one of
these tokens to the output.  The token sent to the output is
determined by the <i>select</i> input, which is required to be an
integer between 0 and <i>n</i>-1, where <i>n</i> is the width of the
<i>input</i> port. Because tokens are immutable, the same Token is
sent to the output, rather than a copy.  The <i>input</i> port may
receive Tokens of any type.

<p> This actor is similar to the Select actor, except that it always
consumes its input tokens.  Input tokens that are not immediately
selected are discarded.

@author Jeff Tsay
@version $Id$
@see ptolemy.actor.lib.Select
*/

public class Multiplexor extends Transformer {

    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this actor within the container.
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
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Input for the index of the port to select. The type is IntToken. */
    public TypedIOPort select;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read a token from the <i>select</i> port and from each channel
     *  of the <i>input</i> port, and output a token on the selected
     *  channel.  This method will throw a NoTokenException if any
     *  input channel does not have a token.
     *
     *  @exception IllegalActionException If there is no director, or if
     *   the <i>select</i> input is out of range.
     */
    public void fire() throws IllegalActionException {
        int index = ((IntToken) select.get(0)).intValue();

        boolean inRange = false;
        for (int i = 0; i < input.getWidth(); i++) {
            Token token = input.get(i);
            if (i == index) {
                output.send(0, token);
                inRange = true;
            }
        }
        if (!inRange) {
            throw new IllegalActionException(this,
                    "Select input is out of range: " + index + ".");
        }
    }

    /** Return false if any input channel does not have a token.
     *  Otherwise, return whatever the superclass returns.
     *  @return False if there are not enough tokens to fire.
     *  @exception IllegalActionException If there is no director.
     */
    public boolean prefire() throws IllegalActionException {
        if (!select.hasToken(0)) return false;
        for (int i = 0; i < input.getWidth(); i++) {
            if (!input.hasToken(i)) return false;
        }
        return super.prefire();
    }
}

