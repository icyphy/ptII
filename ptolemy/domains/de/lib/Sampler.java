/* An actor that produces a copy of the most recent input each time
   the trigger input receives an event.

 Copyright (c) 1998-2000 The Regents of the University of California.
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
@AcceptedRating Red (eal@eecs.berkeley.edu)
*/

package ptolemy.domains.de.lib;

import ptolemy.domains.de.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.actor.lib.Transformer;
import ptolemy.actor.lib.SequenceActor;
import ptolemy.actor.lib.TimedActor;
import ptolemy.actor.*;

//////////////////////////////////////////////////////////////////////////
//// Sampler
/**
Output the most recent input token when the <i>trigger</i> port receives a
token.  If no token has been received on the <i>input</i> port when a
token is received on the <i>trigger</i> port, then no output is
produced.  The inputs and can be of any token type, and the output
is constrained to be of a type at least that of the input.
<p>
Both the <i>input</i> port and the <i>output</i> port are multiports.
Generally, their widths should match. Otherwise, if the width of the
<i>input</i> is greater than
the width of the <i>output</i>, the extra input tokens will
not be produced. If the width of the <i>output</i> is greater
than that of the <i>input</i>, then the last few
channels of the <i>output</i> will never emit tokens.
<p>
Note: If the width of the input changes during execution, then
the most recent inputs are forgotten, as if the execution of the model
were starting over.

@author Jie Liu, Edward A. Lee
@version $Id$
*/

public class Sampler extends DETransformer {

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
        output.setTypeAtLeast(input);
        trigger = new TypedIOPort(this, "trigger", true, false);
        trigger.setTypeEquals(BaseType.GENERAL);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The trigger port, which has type Token. If this port
     *  receives a token, then the most recent token from the
     *  <i>input</i> port will be emitted on the <i>output</i> port.
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
        newobj.output.setTypeAtLeast(newobj.input);
        newobj.trigger = (TypedIOPort)newobj.getPort("trigger");
        newobj.trigger.setTypeEquals(BaseType.GENERAL);
        return newobj;
    }

    /** If there is a token in the <i>trigger</i> port,
     *  emit the most recent token from the <i>input</i> port. If there
     *  has been no input token, or there is no token on the <i>trigger</i>
     *  port, emit nothing.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        if (trigger.hasToken(0)) {
            // Consume the trigger token.
            trigger.get(0);
            int win = input.getWidth();
            int wout = output.getWidth();
            int n = Math.min(win, wout);
            if (_lastInputs == null || _lastInputs.length != win) {
                _lastInputs = new Token[win];
            }
            for (int i = 0; i < n; i++) {
                while (input.hasToken(i)) {
                    _lastInputs[i] = input.get(i);
                }
                // in is the most recent token, assuming
                // the receiver has a FIFO behavior.
                if (_lastInputs[i] != null) {
                    output.send(i, _lastInputs[i]);
                }
            }
            // Consume tokens in extra input channels so they
            // don't get accumulated.
            for (int i = n; i < win; i++) {
                while (input.hasToken(i)) {
                    input.get(i);
                }
            }
        }
    }

    /** If there is no input on the <i>trigger</i> port, return
     *  false, indicating that this actor does not want to fire.
     *  This has the effect of leaving input values in the input
     *  ports, if there are any.
     *  @exception IllegalActionException If there is no director.
     */
    public boolean prefire() throws IllegalActionException {
        // If the trigger input is not connected, never fire.
        if (trigger.getWidth() > 0) {
            return (trigger.hasToken(0));
        } else {
            return false;
        }
    }

    /** Clear the cached input tokens.
     *  @exception IllegalActionException If there is no director.
     */
    public void preinitialize() throws IllegalActionException {
        _lastInputs = null;
        super.preinitialize();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Token[] _lastInputs;
}
