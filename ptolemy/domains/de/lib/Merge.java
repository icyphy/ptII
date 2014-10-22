/* A merge actor for the DE domain.

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
package ptolemy.domains.de.lib;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.SuperdenseTimeDirector;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

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
 available inputs.  Each time this actor fires, it reads the first
 available tokens from an input channel and sends them to the output
 port. If the <i>discardEvents</i> parameter is configured to true,
 then this actor discards all the remaining inputs in the rest of
 channels. Otherwise, this actor requests refirings at the current
 time until no more events are left in the channels. By default,
 the discardEvents parameter is false.

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
        _previousModelTime = ((CompositeActor) this.getContainer())
                .getDirector().getModelTime();
        _previousMicrostep = _getMicrostep();
        _moreTokensOnOtherChannels = false;
        boolean discard = ((BooleanToken) discardEvents.getToken())
                .booleanValue();
        Token firstAvailableToken = null;

        // If tokens can be discarded, this actor sends
        // out the first available tokens only. It discards all
        // remaining tokens from other input channels.
        // Otherwise, this actor handles one channel at each firing
        // and requests refiring at the current time to handle the
        // the remaining channels that have tokens.
        for (int i = 0; i < input.getWidth(); i++) {
            if (input.hasToken(i)) {
                if (firstAvailableToken == null) {
                    // we see the first available tokens
                    firstAvailableToken = input.get(i);
                    output.send(0, firstAvailableToken);

                    while (input.hasToken(i)) {
                        Token token = input.get(i);
                        output.send(0, token);
                    }
                } else {
                    if (discard) {
                        // this token is not the first available token
                        // in this firing, consume and discard all tokens
                        // from the input channel
                        while (input.hasToken(i)) {
                            input.get(i);
                        }
                    } else {
                        // Refiring the actor to handle the other tokens
                        // that are still in channels
                        getDirector().fireAtCurrentTime(this);
                        _moreTokensOnOtherChannels = true;
                        break;
                    }
                }
            }
        }
    }

    /** Return false if there was a firing at the current time and
     *  microstep that produced an output token and there are more
     *  tokens on other channels that are waiting to be produced at
     *  the same time but future microsteps.
     *  @return True if this actor is ready for firing, false otherwise.
     *  @exception IllegalActionException Not thrown in this class.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        int microstep = _getMicrostep();
        if (_moreTokensOnOtherChannels
                && ((CompositeActor) this.getContainer()).getDirector()
                        .getModelTime().compareTo(_previousModelTime) == 0
                && microstep == _previousMicrostep) {
            return false;
        }
        return super.prefire();
    }

    ///////////////////////////////////////////////////////////////////
    //                        private methods                        //

    /** Return the microstep of the director.
     *  @return The microstep.
     *  @exception IllegalActionException Thrown if the enclosing director
     *    is not a SuperdenseTimeDirector.
     */
    private int _getMicrostep() throws IllegalActionException {
        Director director = ((CompositeActor) this.getContainer())
                .getDirector();
        if (director instanceof SuperdenseTimeDirector) {
            return ((SuperdenseTimeDirector) director).getIndex();
        }
        //        throw new IllegalActionException(this,
        //                "This actor can only be used with a SuperdenseTimeDirector");
        // FIXME: Is the following assumption correct?
        // The TMDirector uses Merge, so we should probably not
        // throw an exception here but return a default
        // value for the microstep.
        return 0;
    }

    ///////////////////////////////////////////////////////////////////
    //                        private variables                      //

    // True if there are more tokens on other channels.
    private boolean _moreTokensOnOtherChannels = false;

    // Last time this actor was fired.
    private Time _previousModelTime;

    // Mircostep of director during last firing of this actor.
    private int _previousMicrostep;

}
