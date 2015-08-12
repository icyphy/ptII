/* Output true if the input is present, false otherwise.

 Copyright (c) 1997-2014 The Regents of the University of California.
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
package ptolemy.actor.lib.logic;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.BooleanToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// TrueGate

/**
 On each firing, output true if the input is present and true.
 Otherwise, output nothing.
 The type of the input and output ports is boolean.
 The width of the input is expected to match the width of the output.
 Note that the utility of this actor varies by domain.  In PN, for
 example, the input is always present (by definition). In SDF, it is
 expected to be always present, but may not be.  In DE, the actor is
 only triggered if one of the input channels has data.  The actor is
 probably most useful in synchronous domains like SR and Giotto.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class TrueGate extends Transformer {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public TrueGate(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input.setTypeEquals(BaseType.BOOLEAN);
        input.setMultiport(true);
        output.setTypeEquals(BaseType.BOOLEAN);
        output.setMultiport(true);

        input.setWidthEquals(output, true);

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-15\" y=\"-15\" " + "width=\"40\" height=\"30\" "
                + "style=\"fill:white\"/>\n" + "<text x=\"-10\" y=\"4\""
                + "style=\"font-size:14\">true?</text>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Consume at most one token from each input channel, and output
     *  a boolean on the corresponding output channel (if there is one).
     *  The value of the boolean is true if the input is present and
     *  false otherwise.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        int outputWidth = output.getWidth();

        for (int i = 0; i < input.getWidth(); i++) {
            if (input.hasToken(i)) {
                // Consume the token.
                boolean inputValue = ((BooleanToken) input.get(i))
                        .booleanValue();

                if (i < outputWidth && inputValue) {
                    output.send(i, BooleanToken.TRUE);
                }
            }
        }
    }
}
