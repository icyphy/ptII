/* An actor that delays the input by the specified amount.

 Copyright (c) 1998 The Regents of the University of California.
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

package ptolemy.domains.de.lib;

import ptolemy.actor.*;
import ptolemy.domains.de.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// DESampler
/**
This actor delays the input by a specified amount.
If that amount is zero, then the actor serves only to break the chain
of precedences when calculating priorities for dealing with simultaneous
events. Thus, this actor can be used in feedback loops when there are
no other delays to maintain determinacy.

@author Edward A. Lee
@version $Id$
*/
public class DEDelay extends DEActor {

    /** Constructor.
     *  @param container The composite actor that this actor belongs too.
     *  @param name The name of this actor.
     *  @param delay The amount of delay.
     *
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container, or if the delay is less than zero.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DEDelay(CompositeActor container,
            String name, double delay)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        if (delay < 0.0) {
            throw new IllegalActionException(this,
            "Invalid delay.  Cannot be less than zero.");
        }
        _delay = delay;
        // create the ports
        output = new DEIOPort(this, "output", false, true);
        input = new DEIOPort(this, "input", true, false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Transfer the input tokens to the outputs with the specified delay.
     *  @exception CloneNotSupportedException If the output has multiple
     *   destinations and the token does not support cloning.
     *  @exception IllegalActionException Not thrown in this class.
     */
    public void fire()
            throws CloneNotSupportedException, IllegalActionException {

        for(int i = 0; i < input.getWidth(); i++) {
            Token inputToken;
            try {
                inputToken = input.get(i);
            } catch (NoSuchItemException e) {
                throw new InternalErrorException("Fired with no input event");
            }
            output.send(i, inputToken, _delay);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    // the ports.
    public DEIOPort output;
    public DEIOPort input;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private double _delay = 0.0;
}
