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
 */
package ptolemy.actor;

import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;

///////////////////////////////////////////////////////////////////
//// ConstantPublisherPort

/**
 This is a specialized output port that publishes constant data sent on
 the specified named channel.  The tokens are
 "tunneled" to any instance of {@link SubscriberPort} that names the same channel.
 If {@link #global} is false (the default), then this publisher
 will only send to instances of SubscriberPort that are under the
 control of the same director. That is, it can
 be at a different level of the hierarchy, or in an entirely different
 composite actor, as long as the relevant composite actors are
 transparent (have no director). If {@link #global} is true,
 then the subscriber may be anywhere in the model, as long as its
 <i>global</i> parameter is also true.
  <p>
 Note that this port should be used just like a {@link PublisherPort}.
 If you put it in an opaque composite actor, then it requires a
 token on the inside in order to produce its output. It will replace
 each token with the constant value. Similarly, if it is put into
 a transparent composite actor, then tokens must be sent through
 it. Those tokens will be replaced with the constant value. If it
 is put into an atomic actor, then the actor should call one of
 its broadcast or send methods, providing a token that will be
 replaced with the constant token. This pattern ensures that data
 dependencies work with this port just as with any other port.
<p>
 It is an error to have two instances of PublisherPort
 or ConstantPublisherPort using the same
 channel under the control of the same director. When you create a
 new PublisherPort or
 ConstantPublisherPort, by default, it has no channel name. You have to
 specify a channel name to use it.
 <p>
 <b>How it works:</b>
 When the channel name
 is specified, typically during model construction, this actor
 causes a relation to be created in the least opaque composite
 actor above it in the hierarchy and links to that relation.
 In addition, if {@link #global} is set to true, it causes
 a port to be created in that composite, and also links that
 port to the relation on the inside.  The relation is recorded by the opaque
 composite.  When a SubscriberPort is preinitialized that refers
 to the same channel, that SubscriberPort finds the relation (by
 finding the least opaque composite actor above it) and links
 to the relation. Some of these links are "liberal links" in that
 they cross levels of the hierarchy.
 <p>
 Since publishers are linked to subscribers,
 any data dependencies that the director might assume on a regular
 "wired" connection will also be assumed across publisher-subscriber
 pairs. Similarly, type constraints will propagate across
 publisher-subscriber pairs. That is, the type of the subscriber
 output will match the type of the publisher input.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class ConstantPublisherPort extends PublisherPort {

    /** Construct a constant publisher port with the specified name and container.
     *  @param container The container actor.
     *  @param name The name of the port.
     *  @exception IllegalActionException If the port is not of an acceptable
     *   class for the container, or if the container does not implement the
     *   Actor interface.
     *  @exception NameDuplicationException If the name coincides with
     *   a port already in the container.
     */
    public ConstantPublisherPort(ComponentEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        constantValue = new Parameter(this, "constantValue");
        constantValue.setExpression("0");

        Parameter UNBOUNDED = new Parameter(this, "UNBOUNDED");
        UNBOUNDED.setTypeEquals(BaseType.INT);
        UNBOUNDED.setExpression("-1");
        UNBOUNDED.setPersistent(false);
        UNBOUNDED.setVisibility(Settable.NONE);

        numberOfTokens = new Parameter(this, "numberOfTokens");
        numberOfTokens.setExpression("UNBOUNDED");
        numberOfTokens.setTypeEquals(BaseType.INT);

        numberOfTokens.moveToFirst();
        constantValue.moveToFirst();

        // Hide the initial tokens, as they make no sense for this port.
        initialTokens.setVisibility(Settable.NONE);

        setTypeSameAs(constantValue);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The constant value to publish. This can have any type.
     *  It defaults to the integer 0.
     */
    public Parameter constantValue;

    /** The number of constant tokens to publish. By default,
     *  this is UNBOUNDED, which means that there is no limit.
     */
    public Parameter numberOfTokens;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to replace the specified token with
     *  the value of <i>constantValue</i>.
     *  @param token A token, which will be replaced.
     *  @exception IllegalActionException Not thrown in this base class.
     *  @exception NoRoomException If a send to one of the channels throws
     *     it.
     */
    @Override
    public void broadcast(Token token) throws IllegalActionException,
            NoRoomException {
        if (token == null) {
            super.broadcast(null);
        } else {
            int limit = ((IntToken) numberOfTokens.getToken()).intValue();
            if (limit >= 0) {
                if (_numberOfTokensSent >= limit) {
                    return;
                }
                _numberOfTokensSent++;
            }
            super.broadcast(constantValue.getToken());
        }
    }

    /** Override the base class to replace the specified tokens with
     *  the value of <i>constantValue</i>.
     *  @param tokenArray The token array to replace.
     *  @param vectorLength The number of elements of the token
     *   array to send.
     *  @exception NoRoomException If there is no room in the receiver.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public void broadcast(Token[] tokenArray, int vectorLength)
            throws IllegalActionException, NoRoomException {
        Token[] replacement = new Token[tokenArray.length];
        for (int i = 0; i < tokenArray.length; i++) {
            replacement[i] = tokenArray[i];
        }
        int limit = ((IntToken) numberOfTokens.getToken()).intValue();
        if (limit >= 0) {
            if (_numberOfTokensSent >= limit) {
                return;
            }
            _numberOfTokensSent += vectorLength;
        }
        super.broadcast(replacement, vectorLength);
    }

    /** Override the base class to initialize the token count.
     *  @exception IllegalActionException If initialTokens is invalid.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _numberOfTokensSent = 0;
    }

    /** Override the base class to replace the specified token with
     *  the value of <i>constantValue</i>.
     *  @param channelIndex The index of the channel, from 0 to width-1
     *  @param token The token to replace, or null to send no token.
     *  @exception NoRoomException If there is no room in the receiver.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public void send(int channelIndex, Token token)
            throws IllegalActionException, NoRoomException {
        if (token == null) {
            super.send(channelIndex, null);
        } else {
            int limit = ((IntToken) numberOfTokens.getToken()).intValue();
            if (limit >= 0) {
                if (_numberOfTokensSent >= limit) {
                    return;
                }
                _numberOfTokensSent++;
            }
            super.send(channelIndex, constantValue.getToken());
        }
    }

    /** Override the base class to replace the specified tokens with
     *  the value of <i>constantValue</i>.
     *  @param channelIndex The index of the channel, from 0 to width-1
     *  @param tokenArray The token array to replace.
     *  @param vectorLength The number of elements of of the token
     *   array to send.
     *  @exception NoRoomException If there is no room in the receiver.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public void send(int channelIndex, Token[] tokenArray, int vectorLength)
            throws IllegalActionException, NoRoomException {
        Token[] replacement = new Token[tokenArray.length];
        for (int i = 0; i < tokenArray.length; i++) {
            replacement[i] = tokenArray[i];
        }
        int limit = ((IntToken) numberOfTokens.getToken()).intValue();
        if (limit >= 0) {
            if (_numberOfTokensSent >= limit) {
                return;
            }
            _numberOfTokensSent += vectorLength;
        }
        super.send(channelIndex, replacement, vectorLength);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Number of tokens sent since initialize(). */
    private int _numberOfTokensSent = 0;
}
