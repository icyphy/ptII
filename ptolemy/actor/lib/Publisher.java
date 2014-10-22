/* A publisher that transparently tunnels messages to subscribers.

 Copyright (c) 2006-2014 The Regents of the University of California.
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

import java.util.Set;

import ptolemy.actor.AtomicActor;
import ptolemy.actor.PublisherPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.ActorDependencies;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/**
 This actor publishes input tokens on a named channel. The tokens are
 "tunneled" to any instance of {@link Subscriber} that names the same channel.
 If {@link #global} is false (the default), then this publisher
 will only send to instances of Subscriber that are under the
 control of the same director. That is, it can
 be at a different level of the hierarchy, or in an entirely different
 composite actor, as long as the relevant composite actors are
 transparent (have no director). If {@link #global} is true,
 then the subscriber may be anywhere in the model, as long as its
 <i>global</i> parameter is also true.
 <p>
 It is an error to have two instances of Publisher using the same
 channel under the control of the same director. When you create a
 new Publisher, by default, it has no channel name. You have to
 specify a channel name to use it.
 <p>
 <b>How it works:</b>
 This actor has a hidden output port. When the channel name
 is specified, typically during model construction, this actor
 causes a relation to be created in the least opaque composite
 actor above it in the hierarchy and links to that relation.
 In addition, if {@link #global} is set to true, it causes
 a port to be created in that composite, and also links that
 port to the relation on the inside.  The relation is recorded by the opaque
 composite.  When a Subscriber is preinitialized that refers
 to the same channel, that Subscriber finds the relation (by
 finding the least opaque composite actor above it) and links
 to the relation. Some of these links are "liberal links" in that
 they cross levels of the hierarchy.
 <p>
 Since publishers are linked to subscribers,
 any data dependencies that the director might assume on a regular
 "wired" connection will also be assumed across Publisher-Subscriber
 pairs. Similarly, type constraints will propagate across
 Publisher-Subscriber pairs. That is, the type of the Subscriber
 output will match the type of the Publisher input.

 @author Edward A. Lee, Raymond A. Cardillo, Bert Rodiers
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Green (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class Publisher extends TypedAtomicActor {

    /** Construct a publisher with the specified container and name.
     *  @param container The container actor.
     *  @param name The name of the actor.
     *  @exception IllegalActionException If the actor is not of an acceptable
     *   class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public Publisher(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        channel = new StringParameter(this, "channel");

        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(true);

        output = new PublisherPort(this, "output");
        output.setMultiport(true);
        output.setTypeAtLeast(input);

        // We only have constraints from the publisher on the subscriber
        // and the output of the subscriber and not the other way around
        // to not break any existing models.
        output.setWidthEquals(input, false);

        Parameter hide = new SingletonParameter(output, "_hide");
        hide.setToken(BooleanToken.TRUE);
        // hide = new SingletonParameter(this, "_hideName");
        // hide.setToken(BooleanToken.TRUE);

        global = new Parameter(this, "global");
        global.setExpression("false");
        global.setTypeEquals(BaseType.BOOLEAN);

        propagateNameChanges = new Parameter(this, "propagateNameChanges");
        propagateNameChanges.setExpression("false");
        propagateNameChanges.setTypeEquals(BaseType.BOOLEAN);

        // Refer the parameters of the output port to those of
        // this actor.
        output.channel.setExpression("$channel");
        output.global.setExpression("global");
        output.propagateNameChanges.setExpression("propagateNameChanges");
    }

    ///////////////////////////////////////////////////////////////////
    ////                   ports and parameters                    ////

    /** The name of the channel.  Subscribers that reference this same
     *  channel will receive any transmissions to this port.
     *  This is a string that defaults to empty, indicating that
     *  no channel is specified. A channel must be set before
     *  the actor executes or an exception will occur.
     */
    public StringParameter channel;

    /** Specification of whether the published data is global.
     *  If this is set to true, then a subscriber anywhere in the model that
     *  references the same channel by name will see values published by
     *  this publisher. If this is set to false (the default), then only
     *  those subscribers that are fired by the same director will see
     *  values published on this channel.
     */
    public Parameter global;

    /** The input port.  This is a multiport, allowing multiple
     *  signals to be be transmitted through the publisher channel.
     *  This base class imposes no type constraints except
     *  that the type of the input cannot be greater than the type of the
     *  output.
     */
    public TypedIOPort input;

    /** The output port. This port is hidden and should not be
     *  directly used. By default, the type of this output is constrained
     *  to be at least that of the input. This port is hidden by default
     *  and the actor handles creating connections to it.
     */
    public PublisherPort output;

    /** If true, then propagate channel name changes to any
     *  Subscribers.  The default value is a BooleanToken with the
     *  value false, indicating that if the channel name is changed,
     *  then the channel names of the Subscribers are not changed.  If
     *  the value is true, then if the channel name is changed, the
     *  channel names of the connected Subscribers are updated.
     *
     *  <p>If the value is true, then SubscriptionAggregators that
     *  have the same regular expression as the channel name of the
     *  Publisher will be updated.  However, SubscriptionAggregators
     *  usually have regular expressions as channel names, so usually
     *  the channel name of the SubscriptionAggregator will <b>not</b>
     *  be updated.</p>
     *
     *  <p>Note that if a Publisher is within an Actor Oriented Class
     *  definition, then any Subscribers with the same channel name in
     *  Actor Oriented Class definitions will <b>not</b> be updated.
     *  This is because there is no connection between the Publisher
     *  in the Actor Oriented Class definition and the Subscriber.
     *  However, if the channel name in a Publisher in an instance of
     *  an Actor Oriented Class is updated, then the
     *  corresponding Subscribers in instances of Actor Oriented Class
     *  will be updated.</p>
     */
    public Parameter propagateNameChanges;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Publisher newObject = (Publisher) super.clone(workspace);

        // We only have constraints from the publisher on the subscriber
        // and the output of the subscriber and not the other way around
        // to not break any existing models.
        newObject.output.setWidthEquals(newObject.input, false);

        newObject.output.setTypeAtLeast(newObject.input);

        return newObject;
    }

    /** Read at most one input token from each
     *  input channel and send it to the subscribers,
     *  if any.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        for (int i = 0; i < input.getWidth(); i++) {
            if (input.hasToken(i)) {
                Token token = input.get(i);
                output.send(i, token);
            }
        }
    }

    /** Override the base class to ensure that links to subscribers
     *  have been updated.
     *  @exception IllegalActionException If there is already a publisher
     *   publishing on the same channel, or if the channel name has not
     *   been specified.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        String channelValue = channel.stringValue();
        if (channelValue == null || channelValue.trim().equals("")) {
            throw new IllegalActionException(this,
                    "No channel name has been specified.");
        }

        // Call super.preinitialize() after updating links so that
        // we have connections made before possibly inferring widths.
        super.preinitialize();
    }

    /** Return a Set of Subscribers that are connected to this Publisher.
     *  @return A Set of Subscribers that are connected to this Publisher
     *  @exception KernelException If thrown when a Manager is added to
     *  the top level or if preinitialize() fails.
     */
    public Set<AtomicActor> subscribers() throws KernelException {
        return ActorDependencies.dependents(this, Subscriber.class);
    }
}
