/* A polymorphic adder/subtractor.

 Copyright (c) 2010-2014 The Regents of the University of California.
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
package ptolemy.domains.taskpt.lib;

import ptolemy.actor.lib.AddSubtract;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// AddSubtractAll

/** Add/subtract the values of all tokens present on the input channels
 * (in contrast to one token per channel in the AddSubtract class). For further
 * documentation see {@link ptolemy.actor.lib.AddSubtract}.
 *
 * @author Bastian Ristau
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating red (ristau)
 * @Pt.AcceptedRating red (ristau)
 */
public class AddSubtractAll extends AddSubtract {

    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public AddSubtractAll(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If there is at least one token on the input ports, add
     *  tokens from the <i>plus</i> port, subtract tokens from the
     *  <i>minus</i> port, and send the result to the
     *  <i>output</i> port. All tokens are read
     *  from each channel.  If none of the input
     *  channels has a token, do nothing.  If none of the plus channels
     *  have tokens, then the tokens on the minus channels are subtracted
     *  from a zero token of the same type as the first token encountered
     *  on the minus channels.
     *
     *  @exception IllegalActionException Thrown if there is no director,
     *   or if addition and subtraction are not supported by the
     *   available tokens.
     */
    @Override
    public void fire() throws IllegalActionException {
        Token sum = null;

        for (int i = 0; i < plus.getWidth(); i++) {
            while (plus.hasNewToken(i)) {
                if (sum == null) {
                    sum = plus.get(i);
                } else {
                    sum = sum.add(plus.get(i));
                }
            }
        }

        for (int i = 0; i < minus.getWidth(); i++) {
            while (minus.hasNewToken(i)) {
                Token in = minus.get(i);

                if (sum == null) {
                    sum = in.zero();
                }

                sum = sum.subtract(in);
            }
        }

        if (sum != null) {
            output.send(0, sum);
        }
    }
}
