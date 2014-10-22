/* An actor that produces a copy of the most recent input each time
 the inhibit input does not receive an event.

 Copyright (c) 2001-2014 The Regents of the University of California.
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

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.Transformer;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// Inhibit

/**
 Output a received input token, unless the <i>inhibit</i> port receives a
 token at the same time.  If no token has been received on the <i>input</i>
 port when a token is received on the <i>inhibit</i> port, then no output is
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
 This actor is similar to the Sampler actor in that it modifies a
 stream of events based on the presence or absence of events from another
 input.  This actor reacts to the absence of the other event, whereas
 Sampler reacts to the presence of it.

 @author Steve Neuendorffer and Edward A. Lee
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Yellow (neuendor)
 @Pt.AcceptedRating Yellow (neuendor)
 @see ptolemy.domains.de.lib.MostRecent
 */
public class Inhibit extends Transformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Inhibit(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        input.setMultiport(true);
        output.setMultiport(true);
        output.setTypeAtLeast(input);
        output.setWidthEquals(input, true);

        inhibit = new TypedIOPort(this, "inhibit", true, false);
        inhibit.setMultiport(true);
        inhibit.setTypeEquals(BaseType.GENERAL);
        StringAttribute cardinality = new StringAttribute(inhibit, "_cardinal");
        cardinality.setExpression("SOUTH");

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-30\" y=\"-20\" " + "width=\"60\" height=\"40\" "
                + "style=\"fill:white\"/>\n"
                + "<polyline points=\"0,20 0,0\"/>\n"
                + "<polyline points=\"-30,-0 -10,0\"/>\n"
                + "<polyline points=\"-10,-10 10,10\" style=\"stroke:red\"/>\n"
                + "<polyline points=\"-10,10 10,-10\" style=\"stroke:red\"/>\n"
                + "<polyline points=\"10,0 30,0\"/>\n" + "</svg>\n");
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
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Inhibit newObject = (Inhibit) super.clone(workspace);
        newObject.output.setTypeAtLeast(newObject.input);
        return newObject;
    }

    /** Consume at most one token from each channel of the
     *  <i>input</i> port.  If there is a token on the <i>inhibit</i>
     *  port, then consume it and discard the input tokens.  If no
     *  token is present on the <i>inhibit</i> port, then emit the
     *  input tokens on the <i>output</i> port.  If there are no
     *  input tokens then no output is produced.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        boolean hasInhibit = false;
        for (int i = 0; i < inhibit.getWidth(); i++) {
            if (inhibit.hasToken(i)) {
                // Consume the inhibit token.
                inhibit.get(i);
                hasInhibit = true;
            }
        }
        // Consume the inputs.
        int outputWidth = output.getWidth();
        for (int i = 0; i < input.getWidth(); i++) {
            if (input.hasToken(i)) {
                Token token = input.get(i);
                if (!hasInhibit && i < outputWidth) {
                    output.send(i, token);
                }
            }
        }
    }
}
