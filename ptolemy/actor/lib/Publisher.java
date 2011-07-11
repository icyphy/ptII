/* A publisher that transparently tunnels messages to subscribers.

 Copyright (c) 2006-2011 The Regents of the University of California.
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

import java.util.Iterator;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Manager;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

/**
 This actor publishes input tokens on a named channel. The tokens are
 "tunneled" to any instance of Subscriber that names the same channel
 and that is under the control of the same director. That is, it can
 be at a different level of the hierarchy, or in an entirely different
 composite actor, as long as the relevant composite actors are
 transparent (have no director).
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
 In addition, if <i>export</i> is set to non-zero, it causes
 a port to be created in that composite, and also links that
 port to the relation.  The relation is recorded by the opaque
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
        output = new TypedIOPort(this, "output", false, true);
        output.setMultiport(true);

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

    /** The input port.  This base class imposes no type constraints except
     *  that the type of the input cannot be greater than the type of the
     *  output.
     */
    public TypedIOPort input;

    /** The output port. By default, the type of this output is constrained
     *  to be at least that of the input. This port is hidden by default
     *  and the actor handles creating connections to it.
     */
    public TypedIOPort output;

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

    /** If the attribute is the channel, increment the workspace version
     *  to force cached receiver lists to be updated, and invalidate
     *  the schedule and resolved types of the director, if there is one.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == channel || attribute == global) {
            // We only get the value if we are not in a class definition.
            // The reason is that some of the Actor Oriented Classes
            // that use Publishers do not have the parameter defined
            // in the definition.  See
            // ptolemy/actor/lib/test/auto/PublisherClassNoParameter.xml
            if (!isWithinClassDefinition()) {
                String newValue = channel.stringValue();
                boolean globalValue = ((BooleanToken) global.getToken())
                        .booleanValue();
                if (!newValue.equals(_channel) || globalValue != _global) {
                    NamedObj container = getContainer();
                    if (container instanceof CompositeActor) {
                        // The vergil and config tests were failing because
                        // moml.EntityLibrary sometimes contains Subscribers.
                        try {
                            if (attribute == global) {
                                if (_global && !globalValue) {
                                    ((CompositeActor) container)
                                            .unregisterPublisherPort(_channel,
                                                    output, true);
                                }
                            }

                            if (attribute == channel
                                    && (!(_channel == null || _channel.trim()
                                            .equals("")))) {
                                if (((BooleanToken) propagateNameChanges
                                        .getToken()).booleanValue()) {
                                    _updateChannelNameOfConnectedSubscribers(
                                            _channel, newValue);
                                }
                            }
                            ((CompositeActor) container).registerPublisherPort(
                                    newValue, output, globalValue);

                            if (attribute == channel
                                    && (!(_channel == null || _channel.trim()
                                            .equals("")))) {
                                ((CompositeActor) container)
                                        .unregisterPublisherPort(_channel,
                                                output, _global);
                            }

                        } catch (NameDuplicationException e) {
                            throw new IllegalActionException(this, e,
                                    "Can't add published port.");
                        }
                    }
                    _channel = newValue;
                    _global = globalValue;
                }
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then set the filename public member.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Publisher newObject = (Publisher) super.clone(workspace);
        try {
            newObject._channel = _channel;
            newObject._global = _global;
        } catch (Throwable throwable) {
            CloneNotSupportedException exception = new CloneNotSupportedException();
            exception.initCause(throwable);
            throw exception;
        }

        // We only have constraints from the publisher on the subscriber
        // and the output of the subscriber and not the other way around
        // to not break any existing models.
        newObject.output.setWidthEquals(newObject.input, false);

        return newObject;
    }

    /** Read at most one input token from each
     *  input channel and send it to the subscribers,
     *  if any.
     *  @exception IllegalActionException If there is no director.
     */
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
    public void preinitialize() throws IllegalActionException {
        if (_channel == null || _channel.trim().equals("")) {
            throw new IllegalActionException(this,
                    "No channel name has been specified.");
        }

        // Call super.preinitialize() after updating links so that
        // we have connections made before possibly inferring widths.
        super.preinitialize();
    }

    /** If the new container is null, delete the named channel.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the action would result in a
     *   recursive containment structure, or if
     *   this entity and container are not in the same workspace.
     *  @exception NameDuplicationException If the container already has
     *   an entity with the name of this entity.
     */
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {

        if (container == null
                && !(_channel == null || _channel.trim().equals(""))) {
            NamedObj previousContainer = getContainer();
            if (previousContainer instanceof CompositeActor) {
                ((CompositeActor) previousContainer).unregisterPublisherPort(
                        _channel, output);
            }
        }

        super.setContainer(container);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Cached channel name. */
    protected String _channel;

    /** Cached variable indicating whether publishing is global. */
    protected boolean _global;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Update the channel name of any connected Subscribers.
     *  Note that the channel name of connected Subscription Aggregators
     *  are not updated.
     *  @param previousChannelName The previous name of the channel.
     *  @param newChannelName The new name of the channel.
     *  @exception IllegalActionException If thrown when a Manager is added to
     *  the top level, or when the channel name of a Subscriber is changed.
     */
    private void _updateChannelNameOfConnectedSubscribers(
            String previousChannelName, String newChannelName)
            throws IllegalActionException {
        // If preinitialize() has not yet been called, then we don't
        // yet know what Subscribers are using connected to the
        // Publisher.
        Manager manager = getManager();
        if (manager == null) {
            CompositeActor toplevel = (CompositeActor) (getContainer()
                    .toplevel());
            manager = new Manager(toplevel.workspace(), "PubManager");
            toplevel.setManager(manager);
        }
        try {
            // Create connections between Publishers and Subscribers.
            manager.preinitializeAndResolveTypes();
        } catch (KernelException ex) {
            throw new IllegalActionException(this, ex,
                    "Failed to preinitialize() while trying to update the connected"
                            + " Subscribers.");
        } finally {
            try {
                manager.wrapup();
            } catch (Throwable throwable) {
                // The Exit actor causes Manager.wrapup() to throw this.
                if (!manager.isExitingAfterWrapup()) {
                    throw new IllegalActionException(this, throwable,
                            "Manager.wrapup() failed while trying to update the names"
                                    + " of the connected Subscribers.");
                }
            }
        }
        // For each Subscriber, go and change the channel name
        //for (IOPort port : output.deepConnectedInPortList()) {
        Iterator ports = output.sinkPortList().iterator();
        while (ports.hasNext()) {
            IOPort port = (IOPort) ports.next();
            NamedObj container = port.getContainer();
            if (container instanceof Subscriber) {
                Subscriber subscriber = (Subscriber) container;
                if (subscriber.channel.getExpression().equals(
                        previousChannelName)) {
                    // Avoid updating SubscriptionAggregators that have regular
                    // expressions that are different than the Publisher channel name.
                    subscriber.channel.setExpression(newChannelName);
                    subscriber.attributeChanged(subscriber.channel);
                }
            } else {
                Receiver[][] receivers = port.getRemoteReceivers();
                if (receivers != null) {
                    for (int i = 0; i < receivers.length; i++) {
                        if (receivers[i] != null) {
                            for (int j = 0; j < receivers[i].length; j++) {
                                if (receivers[i][j] != null) {
                                    IOPort remotePort = receivers[i][j]
                                            .getContainer();
                                    if (remotePort != null) {
                                        container = remotePort.getContainer();
                                        if (container instanceof Subscriber) {
                                            Subscriber subscriber = (Subscriber) container;
                                            if (subscriber.channel
                                                    .getExpression()
                                                    .equals(previousChannelName)) {
                                                // Avoid updating
                                                // SubscriptionAggregators
                                                // that have regular
                                                // expressions that
                                                // are different than
                                                // the Publisher
                                                // channel name.
                                                subscriber.channel
                                                        .setExpression(newChannelName);
                                                subscriber
                                                        .attributeChanged(subscriber.channel);

                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
