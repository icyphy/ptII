/* A timed actor that outputs the current time .

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.actor.lib;

import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// CurrentTime

/**
 Produce an output token on each firing with a value that is
 the current time. The output is of type double.
 By default, this uses the local notion of time, which may
 lag behind the global notion of time if this actor is used
 inside a modal model. Under the Continuous director, it is
 essential to use local time, and not global time
 because of the speculative executions during numerical
 ODE solving.

 @author Jie Liu and Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.3
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Yellow (cxh)
 */
public class CurrentTime extends TimedSource {
    /** Construct an actor with the given container and name.
     *
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public CurrentTime(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // set the type constraints.
        output.setTypeEquals(BaseType.DOUBLE);

        useLocalTime = new Parameter(this, "useLocalTime");
        useLocalTime.setTypeEquals(BaseType.BOOLEAN);
        useLocalTime.setExpression("true");

        // Override the clock to make it look a bit
        // different from the DiscreteClock and PoissonClock.
        _attachText(
                "_iconDescription",
                "<svg>\n"
                        + "<rect x=\"-20\" y=\"-20\" "
                        + "width=\"40\" height=\"40\" "
                        + "style=\"fill:lightGrey\"/>\n"
                        + "<circle cx=\"0\" cy=\"0\" r=\"17\""
                        + "style=\"fill:black\"/>\n"
                        + "<line x1=\"0\" y1=\"-15\" x2=\"0\" y2=\"-13\" style=\"stroke:white\"/>\n"
                        + "<line x1=\"0\" y1=\"14\" x2=\"0\" y2=\"16\" style=\"stroke:white\"/>\n"
                        + "<line x1=\"-15\" y1=\"0\" x2=\"-13\" y2=\"0\" style=\"stroke:white\"/>\n"
                        + "<line x1=\"14\" y1=\"0\" x2=\"16\" y2=\"0\" style=\"stroke:white\"/>\n"
                        + "<line x1=\"0\" y1=\"-8\" x2=\"0\" y2=\"0\" style=\"stroke:white\"/>\n"
                        + "<line x1=\"0\" y1=\"0\" x2=\"11.26\" y2=\"-6.5\" style=\"stroke:white\"/>\n"
                        + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** If true, use the model time reported by the input port,
     *  which is normally the model time of the local director.
     *  If false (the default), use the model time reported by
     *  the top-level director. Local time may differ
     *  from global time inside modal models and certain domains
     *  that manipulate time.
     */
    public Parameter useLocalTime;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Send the current time to the output.  If there are trigger inputs,
     *  then the current time is the minimum of the times of each of the
     *  input tokens (currently, these can be different only in the DT
     *  domain).  Otherwise, current time is that reported by the director.
     *  @exception IllegalActionException If send() throws it.
     */
    @Override
    public void fire() throws IllegalActionException {
        // For domain polymorphism getCurrentTime(channel_number) has
        // to be called before get(channel_number).  Currently,
        // the only domain in which the two versions of getCurrentTime
        // are different is in DT... getCurrentTime() on the director
        // returns the "start of iteration" time, whereas getCurrentTime()
        // on the channel returns the time of the current sample.
        double currentTimeValue = Double.POSITIVE_INFINITY;

        if (trigger.isOutsideConnected()) {
            // Trigger port is connected.
            // If there is a token in a channel of the trigger port,
            // output the current time (that is associated with the token).
            for (int i = 0; i < trigger.getWidth(); i++) {
                if (trigger.hasToken(i)) {
                    boolean localTime = ((BooleanToken) useLocalTime.getToken())
                            .booleanValue();
                    if (localTime) {
                        currentTimeValue = Math.min(currentTimeValue, trigger
                                .getModelTime(i).getDoubleValue());
                    } else {
                        currentTimeValue = Math.min(currentTimeValue,
                                getDirector().getGlobalTime().getDoubleValue());
                    }

                    // Do not consume the token... It will be consumed
                    // in the superclass fire().
                    // trigger.get(i);
                    output.send(0, new DoubleToken(currentTimeValue));
                }
            }
        } else {
            // Trigger port is not connected.
            boolean localTime = ((BooleanToken) useLocalTime.getToken())
                    .booleanValue();
            if (localTime) {
                currentTimeValue = getDirector().getModelTime()
                        .getDoubleValue();
            } else {
                currentTimeValue = getDirector().getGlobalTime()
                        .getDoubleValue();
            }
            output.send(0, new DoubleToken(currentTimeValue));
        }

        super.fire();
    }
}
