
/* An actor that produces a copy of the  input at the output each time
   the trigger input receives an event.In the hde domain, the output is
   statically dependent on the value of the previous trigger token.
   This version is compatible with the HDE domains.

 Copyright (c) 1998-2003 The Regents of the University of California.
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
@AcceptedRating Yellow (eal@eecs.berkeley.edu)
*/
package ptolemy.domains.hde.lib;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.domains.de.lib.DETransformer;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// Sampler
/**
Output the most recent input token when the <i>trigger</i> port receives a
token.  If no token has been received on the <i>input</i> port when a
token is received on the <i>trigger</i> port, then no output is
produced.  The inputs can be of any token type, and the output
is constrained to be of a type at least that of the input.In the hde domain,
 the output is  statically dependent on the value of the previous trigger token.
<p>
Both the <i>input</i> port and the <i>output</i> port are multiports.
Generally, their widths should match. Otherwise, if the width of the
<i>input</i> is greater than
the width of the <i>output</i>, the extra input tokens will
not appear on any output, although they will be consumed from
the input port. If the width of the <i>output</i> is greater
than that of the <i>input</i>, then the last few
channels of the <i>output</i> will never emit tokens.
<p>
Note that an event on the input port does not directly cause an output event.
Hence, this actor can be used to break feedback loops.
<p>
Note: If the width of the input changes during execution, then
the most recent inputs are forgotten, as if the execution of the model
were starting over.
<p>
This actor is similar to the Inhibit actor in that it modifies a
stream of events based on the presence or absence of events from another
input.  This actor reacts to the presence of the other event, whereas
Inhibit reacts to the absence of it.

@author Jie Liu, Edward A. Lee, Steve Neuendorffer, Jim Armstrong
@version $Id$
@since Ptolemy II 2.0
@see ptolemy.domains.de.lib.Inhibit
*/

public class StateSampler extends DETransformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public StateSampler(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        input.setMultiport(true);
        output.setMultiport(true);
        output.setTypeAtLeast(input);
        trigger = new TypedIOPort(this, "trigger", true, false);
        trigger.setTypeEquals(BaseType.GENERAL);

        _attachText("_iconDescription", "<svg>\n" +
                "<rect x=\"-30\" y=\"-20\" "
                + "width=\"60\" height=\"40\" "
                + "style=\"fill:white\"/>\n"
                + "<polyline points=\"-30,10 2,10 2,0\"/>\n"
                + "<polyline points=\"-30,-10 -20,-10 -20,0 -10,0 10,-7\"/>\n"
                + "<polyline points=\"10,0 30,0\"/>\n"
                + "</svg>\n");
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
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   has an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        StateSampler newObject = (StateSampler)super.clone(workspace);
        newObject.output.setTypeAtLeast(newObject.input);
        _lastInputs = null;
        return newObject;
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
            int widthOfInputs = input.getWidth();
            int widthOfOutputs = output.getWidth();
            int n = Math.min(widthOfInputs, widthOfOutputs);
            if (_lastInputs == null || _lastInputs.length != widthOfInputs) {
                _lastInputs = new Token[widthOfInputs];
            }
            //In the DE domain this is a while loop
            for (int i = 0; i < n; i++) {
                if (input.hasToken(i)) {
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
            for (int i = n; i < widthOfInputs; i++) {
                if (input.hasToken(i)) {
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
