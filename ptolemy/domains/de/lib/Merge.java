/* A merge actor for DE.

Copyright (c) 1997-2004 The Regents of the University of California.
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

import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// Merge
/**
   A timed merge actor, that merges a set of input signals
   into a single output signal based on the order of the tags
   associated with the events of signals. A tag is a tuple of a time
   (as double) and an index (as non-negative integer). The tags have a
   lexicographic order.

   <p> This actor has an input port (a multiport) and an output port
   (a single port). The types of the ports are undeclared and will be
   resolved by the type resolution mechanism, with the constraint that
   the output type must be greater than or equal to the input type.

   <p> There is a boolean parameter <i>discardEvents</i> associated
   with this actor, which decides how to handle simultaneously
   available inputs.  Each time this actor fires, it reads the first
   available token from the input channels and sends it to the output
   port. If the <i>discardEvents</i> parameter is configured to true,
   then this actor discards all the remaining inputs in other
   channels. Otherwise, this actor requests refirings at the current
   time till no more events are left in the channels. By this way, we
   construct an output signal that no two events share the same
   tag. By default, the discardEvents parameter is false.

   @author Edward A. Lee, Haiyang Zheng
   @version $Id$
   @since Ptolemy II 0.4
   @Pt.ProposedRating Red (hyzheng)
   @Pt.AcceptedRating Red (hyzheng)
*/
public class Merge extends DETransformer  {

    /** Construct an actor in the specified container with the specified
     *  name. Create ports and make the input port a multiport.
     *  @param container The container.
     *  @param name The name.
     *  @exception NameDuplicationException If an actor
     *   with an identical name already exists in the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     */
    public Merge(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        input.setMultiport(true);

        discardEvents = new Parameter(this, "discardEvents",
                new BooleanToken(false));
        discardEvents.setTypeEquals(BaseType.BOOLEAN);

        _attachText("_iconDescription", "<svg>\n" +
                "<polygon points=\"-10,20 10,10 10,-10, -10,-20\" "
                + "style=\"fill:green\"/>\n" +
                "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                       ports and parameters                ////

    /** The flag to indicate whether the input events can be discarded.
     */
    public Parameter discardEvents;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read the first available tokens from input channels and
     *  send it to the output port. If the discardEvents parameter
     *  is true, consume all the available events of the other channels
     *  and discard them. Otherwise, if the other channels have tokens,
     *  request a refiring at the current time to process them.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        boolean discard =
            ((BooleanToken)discardEvents.getToken()).booleanValue();
        Token firstAvailableToken = null;
        Token currentToken = null;
        // If tokens can be discarded, this actor sends
        // out the first available event only but discards all the
        // other events. Otherwise, handle one event for each firing
        // and request refiring at the current time to handle the
        // the remaining events.
        for (int i = 0; i < input.getWidth(); i++) {
            if (input.hasToken(i)) {
                if (firstAvailableToken == null) {
                    // we see the first available event
                    // record it and send it out.
                    firstAvailableToken = input.get(i);
                    output.send(0, firstAvailableToken);
                } else {
                    if (discard) {
                        // this event is not the first available
                        // event, consume the token from the input channel
                        currentToken = input.get(i);
                    } else {
                        // Refiring the actor to handle the other events
                        // that are still in channels
                        getDirector().fireAtCurrentTime(this);
                    }
                }
            }
        }
    }
}
