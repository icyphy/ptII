/* An output port that publishes its data on a named channel.

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

 Review vectorized methods.
 Review broadcast/get/send/hasRoom/hasToken.
 Review setInput/setOutput/setMultiport.
 Review isKnown/broadcastClear/sendClear.
 createReceivers creates inside receivers based solely on insideWidth, and
 outsideReceivers based solely on outside width.
 connectionsChanged: no longer validates the attributes of this port.  This is
 now done in Manager.initialize().
 Review sendInside, getInside, getWidthInside, transferInputs/Outputs, etc.
 */
package ptolemy.actor;

import java.util.regex.Pattern;

import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// SubscriptionAggregatorPort

/**
 Aggregate data produced by multiple publishers.

 <p>This is a generalization of the {@link
 ptolemy.actor.SubscriberPort} (the base class) where the channel name
 is interpreted as a regular expression.  Data produced by all
 publishers that publish on a channel name that matches the regular
 expression are aggregated using the operation given by the {@link
 #operation} parameter.</p>

 <p>Note that the {@link ptolemy.actor.SubscriberPort#channel <i>channel</i>}
 parameter of the superclass is now a regular expression in this class.
 One thing to watch out for is using <code>.</code> instead of <code>\.</code>.
 For example, <code>channel.foo</code> does not mean the same thing as
 <code>channel\.foo</code>. The latter requires a dot between channel and
 foo, where the former does not.

 <p>Note that although this is a multiport, calls to get() should only reference
 channel 0. An exception will be thrown otherwise. The result of the get will
 be the aggregate of what is received on all the input channels.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class SubscriptionAggregatorPort extends SubscriberPort {

    /** Construct a subscriber port with a containing actor and a name.
     *  This is always an input port.
     *  @param container The container actor.
     *  @param name The name of the port.
     *  @exception IllegalActionException If the port is not of an acceptable
     *   class for the container, or if the container does not implement the
     *   Actor interface.
     *  @exception NameDuplicationException If the name coincides with
     *   a port already in the container.
     */
    public SubscriptionAggregatorPort(ComponentEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        operation = new StringParameter(this, "operation");
        operation.addChoice("add");
        operation.addChoice("multiply");
        operation.setExpression("add");

        setMultiport(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The operation used to aggregate the data produced by
     *  matching publishers. The choices are "add" and "multiply".
     *  Note that "multiply" is a poor choice if the data type
     *  has a non-commutative multiplication operation (e.g.
     *  matrix types) because the result will be nondeterministic.
     *  This is a string that defaults to "add".
     */
    public StringParameter operation;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If a publish and subscribe channel is set, then set up the connections.
     *  If an aspect is added, removed or modified update the list of
     *  aspects.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException Thrown if the new color attribute cannot
     *      be created.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == operation) {
            String newValue = operation.stringValue();
            if (newValue.equals("add")) {
                _addOperation = true;
            } else {
                _addOperation = false;
            }
        } else if (attribute == channel) {
            // Override the base class to use the version
            // of unlinkToPublishedPort() that takes a Pattern
            // argument rather than a String.
            String newValue = channel.stringValue();
            if (!newValue.equals(_channel)) {
                NamedObj immediateContainer = getContainer();
                if (immediateContainer != null) {
                    NamedObj container = immediateContainer.getContainer();
                    if (container instanceof CompositeActor
                            && !(_channel == null || _channel.trim().equals(""))) {
                        ((CompositeActor) container).unlinkToPublishedPort(
                                _channelPattern, this, _global);
                    }
                    _channel = newValue;
                    // Don't call super here because super.attributeChanged() tries to unlink _channel
                    // as a non-regular expression string, which seems wrong.
                    // super.attributeChanged(attribute);
                    _channelPattern = Pattern.compile(_channel);
                }
            }
        } else if (attribute == global) {
            boolean newValue = ((BooleanToken) global.getToken())
                    .booleanValue();
            if (newValue == false && _global == true) {
                NamedObj immediateContainer = getContainer();
                if (immediateContainer != null) {
                    NamedObj container = immediateContainer.getContainer();
                    if (container instanceof CompositeActor
                            && !(_channel == null || _channel.trim().equals(""))) {
                        ((CompositeActor) container).unlinkToPublishedPort(
                                _channelPattern, this, _global);
                    }
                }
            }
            _global = newValue;
            // Do not call SubscriptionAggregator.attributeChanged()
            // because it will remove the published port name by _channel.
            // If _channel is set to a real name (not a regex pattern),
            // Then chaos ensues.  See test 3.0 in SubscriptionAggregator.tcl
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Get a token from the specified channel.
     *  This overrides the base class to first ensure that
     *  the <i>channelIndex</i> is 0 (or an exception is
     *  thrown), and then to aggregate the tokens from all
     *  of the input channels according to the
     *  {@link #operation} parameter and return the
     *  single token result.
     *  Specifically, it reads one token from each input channel
     *  that has a token, aggregates these, and returns the aggregate.
     *  @param channelIndex The channel index. This is required to be 0.
     *  @return An aggregation of the tokens from all input channels.
     *  @exception NoTokenException If there is no token.
     *  @exception IllegalActionException If there is no director, and hence
     *   no receivers have been created, if the port is not an input port, or
     *   if the channel index is not 0.
     */
    @Override
    public Token get(int channelIndex) throws NoTokenException,
            IllegalActionException {
        if (channelIndex != 0) {
            throw new IllegalActionException(
                    this,
                    "Although it is a multiport, you can only read"
                            + " from channel 0 of a SubscriptionAggregatorPort.");
        }
        Token result = null;
        for (int i = 0; i < getWidth(); i++) {
            if (super.hasToken(i)) {
                Token input = super.get(i);
                if (result == null) {
                    result = input;
                } else {
                    if (_addOperation) {
                        result = result.add(input);
                    } else {
                        result = result.multiply(input);
                    }
                }
            }
        }
        if (result == null) {
            throw new NoTokenException(this, "No input tokens");
        }
        return result;
    }

    /** Get an array of tokens from the specified channel.
     *  This overrides the base class to first ensure that
     *  the <i>channelIndex</i> is 0 (or an exception is
     *  thrown), and then to aggregate the tokens from all
     *  of the input channels according to the
     *  {@link #operation} parameter and return the
     *  single token result.
     *  Specifically, it reads one token from each input channel
     *  that has a token, aggregates these, and returns the aggregate.
     *  @param channelIndex The channel index. This is required to be 0.
     *  @param vectorLength The number of valid tokens to get in the
     *   returned array.
     *  @return A token array with length
     *   <i>vectorLength</i> aggregating the inputs.
     *  @exception NoTokenException If there is not enough tokens.
     *  @exception IllegalActionException If there is no director, and hence
     *   no receivers have been created, if the port is not an input port, or
     *   if the channel index is not 0.
     */
    @Override
    public Token[] get(int channelIndex, int vectorLength)
            throws NoTokenException, IllegalActionException {
        if (channelIndex != 0) {
            throw new IllegalActionException(
                    this,
                    "Although it is a multiport, you can only read"
                            + " from channel 0 of a SubscriptionAggregatorPort.");
        }
        Token[] result = null;
        for (int i = 0; i < getWidth(); i++) {
            if (super.hasToken(i, vectorLength)) {
                Token[] input = super.get(i, vectorLength);
                if (result == null) {
                    result = input;
                } else {
                    if (_addOperation) {
                        for (int j = 0; j < vectorLength; j++) {
                            result[j] = result[j].add(input[j]);
                        }
                    } else {
                        for (int j = 0; j < vectorLength; j++) {
                            result[j] = result[j].multiply(input[j]);
                        }
                    }
                }
            }
        }
        if (result == null) {
            throw new NoTokenException(this, "Not engouh input tokens");
        }
        return result;
    }

    /** Return the inside width of this port, which in this class is
     *  always 1.
     *  @return The width of the inside of the port.
     */
    @Override
    public int getWidthInside() {
        return 1;
    }

    /** Return true if any input channel has a token.
     *  @param channelIndex The channel index. This is required to be 0.
     *  @return True if any input channel has a token.
     *  @exception IllegalActionException If the channel index is not 0
     *   or if the superclass throws it.
     */
    @Override
    public boolean hasToken(int channelIndex) throws IllegalActionException {
        /* Allow asking about other channels.
        if (channelIndex != 0) {
            throw new IllegalActionException(
                    this,
                    "Although it is a multiport, you can only read"
                            + " from channel 0 of a SubscriptionAggregatorPort.");
        }
         */
        for (int i = 0; i < getWidth(); i++) {
            if (super.hasToken(i)) {
                return true;
            }
        }
        return false;
    }

    /** Return true if every input channel that has tokens has enough tokens.
     *  @param channelIndex The channel index. This is required to be 0.
     *  @param vectorLength The number of tokens to query the channel for.
     *  @return True if every input channel that has tokens has enough tokens.
     *  @exception IllegalActionException If the channel index is not 0
     *   or if the superclass throws it.
     */
    @Override
    public boolean hasToken(int channelIndex, int vectorLength)
            throws IllegalActionException {
        /* Allow asking about other channels.
        if (channelIndex != 0) {
            throw new IllegalActionException(
                    this,
                    "Although it is a multiport, you can only read"
                            + " from channel 0 of a SubscriptionAggregatorPort.");
        }
         */
        boolean foundOne = false;
        for (int i = 0; i < getWidth(); i++) {
            if (super.hasToken(i)) {
                foundOne = true;
                if (!super.hasToken(i, vectorLength)) {
                    return false;
                }
            }
        }
        return foundOne;
    }

    /** Check that the port is not in the top level, then
     *  call preinitialize() in the super class.
     *  @exception IllegalActionException If the port is in
     *  the top level.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        NamedObj actor = getContainer();
        if (actor != null && actor.getContainer() == null) {
            throw new IllegalActionException(
                    this,
                    "SubscriptionAggregatorPorts cannot be used at the top level, use a SubscriptionAggregator actor instead.");
        }
        super.preinitialize();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Override the base class to always return 1.
     *  @param except The relation to exclude.
     *  @return The sums of the width of the relations linked on the inside,
     *  except for the specified port.
     */
    @Override
    protected int _getInsideWidth(IORelation except) {
        return 1;
    }

    /** Update the connection to the publishers, if there are any.
     *  @exception IllegalActionException If creating the link
     *   triggers an exception.
     */
    @Override
    protected void _updateLinks() throws IllegalActionException {
        // This overrides the base class to Pattern version
        // rather than the String version of linkToPublishedPort().

        // If the channel has not been set, then there is nothing
        // to do.  This is probably the first setContainer() call,
        // before the object is fully constructed.
        if (_channelPattern == null) {
            return;
        }

        NamedObj immediateContainer = getContainer();
        if (immediateContainer != null) {
            NamedObj container = immediateContainer.getContainer();
            if (container instanceof CompositeActor) {
                try {
                    try {
                        ((CompositeActor) container).linkToPublishedPort(
                                _channelPattern, this, _global);
                    } catch (IllegalActionException ex) {
                        // If we have a LazyTypedCompositeActor that
                        // contains the Publisher, then populate() the
                        // model, expanding the LazyTypedCompositeActors
                        // and retry the link.  This is computationally
                        // expensive.
                        // See $PTII/ptolemy/actor/lib/test/auto/LazyPubSub.xml
                        _updatePublisherPorts((CompositeEntity) toplevel());
                        // Now try again.
                        ((CompositeActor) container).linkToPublishedPort(
                                _channelPattern, this, _global);
                    }
                } catch (NameDuplicationException e) {
                    throw new IllegalActionException(this, e,
                            "Can't link SubscriptionAggregatorPort with a PublisherPort.");
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Indicator that the operation is "add" rather than "multiply". */
    private boolean _addOperation = true;

    /** Regex Pattern for _channelName. */
    private Pattern _channelPattern;
}
