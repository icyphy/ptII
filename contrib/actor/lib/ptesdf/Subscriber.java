/* A subscriber that transparently receives tunneled messages from publishers.

 Copyright (c) 2003-2006 The Regents of the University of California.
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
package contrib.actor.lib.ptesdf;

import java.util.Iterator;

import ptolemy.actor.Director;
import ptolemy.actor.IORelation;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;

//////////////////////////////////////////////////////////////////////////
//// Subscriber

/**
 This actor subscribes to tokens on a named channel. The tokens are
 "tunneled" from an instance of Publisher that names the same channel
 and that is under the control of the same director. That is, it can
 be at a different level of the hierarchy, or in an entirely different
 composite actor, as long as the relevant composite actors are
 transparent (have no director).
 <p>
 Any number of instances of Subscriber can subscribe to the same
 channel. If <code>aggregate</code> is enabled, dot segmented asterisk wildcard
 processing is performed on the subscriber channel name specification so that
 multiple publishers can be aggregated into one Subscriber output as a single
 multi-port. For example, if Publisher1 is <em>"northfield.apples"</em> and
 Publisher2 is <em>"southfield.apples"</em>, and Subscriber1 is <em>"*.apples"</em>,
 then Subscriber1 would output a multi-port consisting of the ports from both
 Publisher1 and Publisher2.
 
 <p>
 This actor actually has a hidden input port that is connected
 to the publisher via hidden "liberal links" (links that are
 allowed to cross levels of the hierarchy).  Consequently,
 any data dependencies that the director might assume on a regular
 "wired" connection will also be assumed across Publisher-Subscriber
 pairs.  Similarly, type constraints will propagate across
 Publisher-Subscriber pairs. That is, the type of the Subscriber
 output will match the type of the Publisher input.
 <p>
 
 @author Edward A. Lee, Raymond A. Cardillo
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Green (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class Subscriber extends TypedAtomicActor {

    /** Construct a publisher with the specified container and name.
     *  @param container The container actor.
     *  @param name The name of the actor.
     *  @exception IllegalActionException If the actor is not of an acceptable
     *   class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public Subscriber(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        // Set this up as input port.
        super(container, name);

        aggregate = new Parameter(this, "aggregate", BooleanToken.FALSE);
        aggregate.setTypeEquals(BaseType.BOOLEAN);

        channel = new StringParameter(this, "channel");
        channel.setToken("channel.name");

        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(true);

        output = new TypedIOPort(this, "output", false, true);
        output.setMultiport(true);

        Parameter hide = new SingletonParameter(input, "_hide");
        hide.setToken(BooleanToken.TRUE);
        hide.setVisibility(Settable.NOT_EDITABLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                   ports and parameters                    ////

    /** The name of the channel.  Subscribers that reference this channel will
     *  receive any transmissions from Publishers of this same channel name.
     *  If <code>aggregate</code> is enabled, dot segmented asterisk wildcards
     *  can be used to match multiple publishers and aggregate them into a
     *  multi-port output. The default value is "channel.name".
     */
    public StringParameter channel;

    /** Enable aggregation.  Check for aggregate subscriptions using asterisk wildcards.
     *  This parameter controls whether the subscriber will look for wildcard
     *  publishers and output a single aggregated multi-port output.
     */
    public Parameter aggregate;

    /** The input port.  This base class imposes no type constraints except
     *  that the type of the input cannot be greater than the type of the
     *  output.
     */
    public TypedIOPort input;

    /** The output port.  By default, the type of this output is constrained
     *  to be at least that of the input. This port is hidden by default
     *  and the actor handles creating connections to it.
     */
    public TypedIOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Determine if aggregation is enabled.
     *  @return <code>true</code> if this Subscriber is aggregating.
     */
    public boolean isAggregating() {
        boolean result;

        try {
            result = aggregate.getToken().equals(BooleanToken.TRUE);
        } catch (IllegalActionException ex) {
            result = false;
        }

        return result;
    }

    /** If the attribute is the channel, cache the string value.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == channel) {
            _channel = channel.stringValue();
            _subSegs = _channel.split("[.]");
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Prepare the Subscriber for use, and ensure that there is a matching Publisher.
     *  @exception IllegalActionException If there is no matching publisher.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();

        if (!_hasPublishers()) {
            throw new IllegalActionException(this,
                    "Subscriber has no matching Publisher on channel \""
                            + _channel + "\".");
        }
    }

    /** Recalculate the output port widths if the input port reports changes.
     *  @param port The port that has connection changes.
     */
    public void connectionsChanged(Port p) {
        super.connectionsChanged(p);

        if (p == input) {
            try {
                _updateOutputWidths();
            } catch (IllegalActionException ex) {
            }
        }
    }

    /** Read at most one input token from each input channel
     *  and send it to the output.
     *  @exception IllegalActionException If there is no director,
     *  or if there is no input connection.
     */
    public void fire() throws IllegalActionException {
        super.fire();

        int width = input.getWidth();
        for (int i = 0; i < width; i++) {
            if (input.hasToken(i)) {
                Token token = input.get(i);
                output.send(i, token);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected variables                 ////

    /** Cached channel name string. */
    protected String _channel;

    /** Cached channel name segment strings. */
    protected String _subSegs[];

    ///////////////////////////////////////////////////////////////////
    ////                       protected methods                   ////

    /** Determine if a Published channel name matches this channel name.
     *  If <code>aggregate</code> is enabled, then the match allows
     *  multiple Publishers to match if any asterisk wildcards are specified.
     *  @return <code>true</code> if the channel matches.
     */
    protected boolean channelMatches(String pubChannelName) {
        boolean result = false;

        if (!isAggregating() || _subSegs == null || _subSegs.length == 0) {
            result = _channel.equals(pubChannelName);
        } else {
            String pubSegs[] = pubChannelName.split("[.]");
            if (_subSegs.length == pubSegs.length) {
                int iSegment = 0;
                for (iSegment = 0; iSegment < _subSegs.length; iSegment++) {
                    if (_subSegs[iSegment].equals("*")) {
                        // wildcard, do not bother comparing, just skip                            
                        continue;
                    } else if (!_subSegs[iSegment].equals(pubSegs[iSegment])) {
                        // segment did not match, we do not have a match
                        break;
                    }
                }
                // result is true if we successfully evaluated all segments
                result = (iSegment == pubSegs.length);
            }
        }

        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Determine if there are any matching Publishers for this Subscriber.
     *  @return <code>true</code> if there are any publishers.
     */
    private boolean _hasPublishers() throws IllegalActionException {
        // Find the nearest opaque container above in the hierarchy.
        CompositeEntity container = (CompositeEntity) getContainer();
        while (container != null && !container.isOpaque()) {
            container = (CompositeEntity) container.getContainer();
        }

        if (container != null) {
            Iterator actors = container.deepEntityList().iterator();
            while (actors.hasNext()) {
                Object actor = actors.next();
                if (actor instanceof Publisher) {
                    String pubChannelName = ((Publisher) actor)._channel;
                    if (channelMatches(pubChannelName)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /** Update any connected output widths using the new input width.
     */
    private void _updateOutputWidths() throws IllegalActionException {
        int inputWidth = input.getWidth();
        Iterator outputRelations = output.linkedRelationList().iterator();
        while (outputRelations.hasNext()) {
            Object obj = outputRelations.next();
            if ((obj != null) && (obj instanceof IORelation)) {
                ((IORelation) obj).setWidth(inputWidth);
            }
        }

        Director director = getDirector();
        if (director != null) {
            director.invalidateSchedule();
            director.invalidateResolvedTypes();
        }
    }
}
