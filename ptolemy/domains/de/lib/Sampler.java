/* An actor that outputs the average of the inputs so far.

 Copyright (c) 1998-1999 The Regents of the University of California.
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

@ProposedRating Red (liuj@eecs.berkeley.edu)
@AcceptedRating Red (xxx@eecs.berkeley.edu)
*/

package ptolemy.domains.de.lib;

import ptolemy.domains.de.kernel.*;
import ptolemy.domains.de.lib.DETransformer;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.actor.lib.SequenceActor;
import ptolemy.actor.lib.TimedActor;
import ptolemy.actor.*;

//////////////////////////////////////////////////////////////////////////
//// Sampler
/**
Output the most recent input token if the trigger port has an input token. 
There may not be an output if there is no input token.
The inputs and outputs can be any token type.
The output type is constrained to be the same as the input type.
<p>
Both the input port and the output port are multiport. Generally, their
width should match. Otherwise, if the width of the input is greater than
the width of the output, the extra input tokens will be missing. If the 
width of the output is greater then that of the input, the last few 
channels will never emit tokens.

@author Jie Liu
@version $Id$
*/

public class Sampler extends DETransformer 
    implements TimedActor, SequenceActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Sampler(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        input.setMultiport(true);
        output.setMultiport(true);
        output.setTypeSameAs(input);
        trigger = new TypedIOPort(this, "trigger", true, false);
        //trigger.setTypeEquals(BooleanToken.class);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The trigger port of type BooleanToken. If this port
     *  receives a token, then the most recent token from the
     *  input will be emitted.
     */
    public TypedIOPort trigger;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the ports.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   has an attribute that cannot be cloned.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        Sampler newobj = (Sampler)super.clone(ws);
        newobj.input.setMultiport(true);
        newobj.output.setMultiport(true);
        newobj.output.setTypeSameAs(newobj.input);
        //System.out.println(newobj.output.getName());
        newobj.trigger = (TypedIOPort)newobj.getPort("trigger");
        //System.out.println(newobj.trigger.getName());
        newobj.trigger.setInput(true);
        //newobj.trigger.setTypeEquals(BooleanToken.class);
        return newobj;
    }

    /** Examine the trigger port, if there is a token in the port, 
     *  emit the most recent token from the input port. If there's
     *  no input token either from the trigger port or the input
     *  port, then there will be no output token.
     *  
     *  @exception IllegalActionException If the right arithmetic operations
     *   are not supported by the supplied tokens.
     */
    public void fire() throws IllegalActionException {
        try {
            // First test if it is dangling... then if it has token.
            if (trigger.getWidth() > 0) {
                if (trigger.hasToken(0)) {
                    // Consume the trigger token.
                    trigger.get(0);
                    int win = input.getWidth();
                    int wout = output.getWidth();
                    int n = Math.min(win, wout);
                    Token in;
                    for (int i = 0; i < n; i++) {
                        in = null;
                        while (input.hasToken(i)) {
                            in = input.get(i);
                        }
                        // in is the most recent token, assuming 
                        // the receiver has a FIFO behavior.
                        if (in != null) {
                            output.send(i, in);
                        }
                    }
                    // Consume tokens in extra input channels so they 
                    // don't get accumulated.
                    if (n < win) {
                        for (int i = n; i < win; i++) {
                            while (input.hasToken(i)) {
                                input.get(i);
                            }
                        }
                    }
                }
            }
        } catch (IllegalActionException ex) {
            // Should not be thrown because this is an output port.
            throw new InternalErrorException(ex.getMessage());
        }
    }
}
