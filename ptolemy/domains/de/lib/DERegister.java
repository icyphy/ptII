/* An actor that samples the data input when the clock input arrives.

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
//// DERegister
/**
This actor samples the data input at the times given by events on the clock
input. The values of the clock input events are ignored. If no data input is
available at the time of sampling, the most recently seen data input is used.
If there has been no data input, then a "zero" token is produced. The exact
meaning of zero depends on the token type.

@author Lukito Muliadi
@version $Id$
@see Actor
*/
public class DERegister extends DEActor {

    /** Construct a DERegister actor.
     *  @param container The composite actor that this actor belongs too.
     *  @param name The name of this actor.
     *
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DERegister(TypedCompositeActor container,
            String name,
            Token initToken)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        if (initToken == null) {
            _lastInput = new DoubleToken(0.0);
        } else {
            _lastInput = initToken;
        }
        // create an output port
        output = new DEIOPort(this, "output", false, true);
        // create input ports
        input = new DEIOPort(this, "data input", true, false);
        clock = new DEIOPort(this, "clock input", true, false);
        clock.triggers(output);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** If there's an event in the clock input port then produce an event,
     *  otherwise just record the value of the input port.
     *
     * @exception IllegalActionException Not thrown in this class.
     */
    public void fire() throws IllegalActionException{


        while (input.hasToken(0)) {
            _lastInput = input.get(0);
        }

        while (clock.hasToken(0)) {
            clock.get(0);
            output.broadcast(_lastInput);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Private variables should not have doc comments, they should
    // have regular C++ comments.

    // the last token seen in the input port.
    private Token _lastInput;

    // the ports.
    public DEIOPort output;
    public DEIOPort input;
    public DEIOPort clock;
}










