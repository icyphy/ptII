/* A merge actor for the DE domain.

 Copyright (c) 1997-2015 The Regents of the University of California.
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

import java.util.LinkedList;
import java.util.Queue;

import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// Merge

/**
 A timed merge actor for the DE domain. It merges a set of input signals
 into a single output signal based on the order of the tags
 associated with the events of signals. A tag is a tuple of a timestamp
 (as double) and a microstep or index (as non-negative integer). Tags have a
 lexicographic order.
 <p>
 This actor has an input port (a multiport) and an output port
 (a single port). The types of the ports are undeclared and will be
 resolved by the type resolution mechanism, with the constraint that
 the output type must be greater than or equal to the input type.
 <p>
 There is a boolean parameter <i>discardEvents</i> associated
 with this actor, which decides how to handle simultaneously
 available inputs.
 <p>
 If the <i>discardEvents</i> parameter is configured to true,
 then each time this actor fires, it reads the first
 available token from an input channel and send it to the output
 port.  Then this actor discards all the remaining inputs in the rest of
 channels.
 <p>
 If the <i>discardEvents</i> parameter is configured to false (the default),
 then the handling of simultaneous events is a bit more subtle.
 On each firing, it reads all available inputs in the order of the channels
 and puts them on a queue to be produced as an output. It then takes
 the first token (the oldest one) from the queue and produces it
 on the output. If after this firing the queue is not empty, then
 it requests a refiring at the current time.

 @author Edward A. Lee, Haiyang Zheng
 @version $Id$
 @since Ptolemy II 0.4
 @Pt.ProposedRating Green (hyzheng)
 @Pt.AcceptedRating Green (hyzheng)
 */
public class Merge extends DETransformer {
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

        discardEvents = new Parameter(this, "discardEvents");
        discardEvents.setExpression("false");
        discardEvents.setTypeEquals(BaseType.BOOLEAN);

        _attachText("_iconDescription", "<svg>\n"
                + "<polygon points=\"-10,20 10,10 10,-10, -10,-20\" "
                + "style=\"fill:green\"/>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                       ports and parameters                ////

    /** A flag to indicate whether the input events can be discarded.
     *  Its default value is false.
     */
    public Parameter discardEvents;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone this actor into the specified workspace.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If cloned ports cannot have
     *   as their container the cloned entity (this should not occur), or
     *   if one of the attributes cannot be cloned.
     *  @return A new ComponentEntity.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Merge newObject = (Merge)super.clone(workspace);
        newObject._queue = null;
        return newObject;
    }

    /** Read the first available tokens from an input channel and
     *  send them to the output port. If the discardEvents parameter
     *  is true, consume all the available tokens of the other channels
     *  and discard them. Otherwise, if the other channels have tokens,
     *  request a refiring at the current time to process them.
     *  @exception IllegalActionException If there is no director, or
     *  the input can not be read, or the output can not be sent.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        boolean discard = ((BooleanToken) discardEvents.getToken()).booleanValue();
        boolean foundInput = false;

        // If tokens can be discarded, this actor sends
        // out the first available tokens only. It discards all
        // remaining tokens from other input channels.
        // Otherwise, this actor handles one channel at each firing
        // and requests refiring at the current time to handle the
        // the remaining channels that have tokens.
        for (int i = 0; i < input.getWidth(); i++) {
            if (input.hasNewToken(i)) {
                // This output will be produced regardless of discard status.
                if (!discard || !foundInput) {
                    _queue.offer(input.get(i));
                }
                foundInput = true;
            }
        }
        if (!_queue.isEmpty()) {
            output.send(0, _queue.poll());
            if (!_queue.isEmpty()) {
                // Refiring the actor to handle the other tokens.
                getDirector().fireAt(this, getDirector().getModelTime());
            }
        }
    }

    /** Initialize this actor by creating a new queue for pending outputs.
     *  @exception IllegalActionException If a superclass throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _queue = new LinkedList<Token>();
    }

    ///////////////////////////////////////////////////////////////////
    //                        private variables                      //

    // Queue of outputs to be produced.
    private Queue<Token> _queue = null;
}
