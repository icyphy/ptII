/* An actor that produces a copy of the most recent input each time
   the inhibit input does not receive an event.

 Copyright (c) 2001 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (eal@eecs.berkeley.edu)
*/

package ptolemy.domains.de.lib;

import ptolemy.domains.de.kernel.*;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.actor.lib.Transformer;
import ptolemy.actor.lib.SequenceActor;
import ptolemy.actor.lib.TimedActor;
import ptolemy.actor.*;

//////////////////////////////////////////////////////////////////////////
//// Inhibit
/**
Output the most recent input token, unless the <i>inhibit</i> port receives a
token.  If no token has been received on the <i>input</i> port when a
token is received on the <i>inhibit</i> port, then no output is
produced.  The inputs can be of any token type, and the output
is constrained to be of a type at least that of the input.
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

@author Steve Neuendorffer
@version $Id$
*/

public class Inhibit extends DETransformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Inhibit(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        input.setMultiport(true);
        output.setMultiport(true);
        output.setTypeAtLeast(input);
        inhibit = new TypedIOPort(this, "inhibit", true, false);
        inhibit.setTypeEquals(BaseType.GENERAL);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The inhibit port, which has type Token. If this port
     *  does not receive a token, then the most recent token from the
     *  <i>input</i> port will be emitted on the <i>output</i> port.
     */
    public TypedIOPort inhibit;

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
        Inhibit newObject = (Inhibit)super.clone(workspace);
        newObject.output.setTypeAtLeast(newObject.input);
        return newObject;
    }

    /** If there is a token in the <i>inhibit</i> port,
     *  emit the most recent token from the <i>input</i> port. If there
     *  has been no input token, or there is no token on the <i>inhibit</i>
     *  port, emit nothing.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        if(inhibit.hasToken(0)) {
            // Consume the inhibit token.
            inhibit.get(0);
            for(int i = 0; i < input.getWidth(); i++) {
                while(input.hasToken(i)) {
                    input.get(i);
                }
            }
        } else {
            for(int i = 0; i < input.getWidth(); i++) {
                while(input.hasToken(i)) {
                    output.send(i, input.get(i));
                }
            }
        }
    }
}
