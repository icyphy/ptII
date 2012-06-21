/* An output port that publishes its data on a named channel.

 Copyright (c) 1997-2011 The Regents of the University of California.
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

import java.util.HashSet;
import java.util.Set;

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.InstantiableNamedObj;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.HierarchyListener;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// PublisherPort

/**
 This is a specialized output port that publishes data sent through it on
 the specified named channel.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 9.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class PublisherPort extends TypedIOPort implements HierarchyListener {

    /** Construct an IOPort with a containing actor and a name
     *  that is neither an input nor an output.  The specified container
     *  must implement the Actor interface, or an exception will be thrown.
     *
     *  @param container The container actor.
     *  @param name The name of the port.
     *  @exception IllegalActionException If the port is not of an acceptable
     *   class for the container, or if the container does not implement the
     *   Actor interface.
     *  @exception NameDuplicationException If the name coincides with
     *   a port already in the container.
     */
    public PublisherPort(ComponentEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        channel = new StringParameter(this, "channel");
        
        global = new Parameter(this, "global");
        global.setTypeEquals(BaseType.BOOLEAN);
        global.setExpression("false");
        
        propagateNameChanges = new Parameter(this, "propagateNameChanges");
        propagateNameChanges.setExpression("false");
        propagateNameChanges.setTypeEquals(BaseType.BOOLEAN);
        
        setOutput(true);
        setInput(false);
        // FIXME: Should disable making changes to the above.
                
        // FIXME: if you also wire something to this port, then the
        // port will be required to be a multiport, and it will not be
        // clear which channel goes where!
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** Specification of whether the published data is global.
     *  This is ignored if {@link #channel} is empty.
     *  If this is set to true, then a subscriber anywhere in the model that
     *  references the same channel by name will see values published by
     *  this port. If this is set to false (the default), then only
     *  those subscribers that are fired by the same director will see
     *  values published on this channel.
     */
    public Parameter global;

    /** If set, then this port is used to communicate over a named
     *  publish and subscribe channel, rather than over manually
     *  established connections.
     */
    public StringParameter channel;

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

    /** If a publish and subscribe channel is set, then set up the connections.
     *  If a quantity manager is added, removed or modified update the list of
     *  quantity managers.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException Thrown if the new color attribute cannot
     *      be created.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == channel || attribute == global) {
            // We only get the value if we are not in a class definition.
            // Class definitions need not have connections.
            // The connections will be made in the instances.
            // The reason is that some of the Actor Oriented Classes
            // that use Publishers do not have the parameter defined
            // in the definition.  See
            // ptolemy/actor/lib/test/auto/PublisherClassNoParameter.xml
            NamedObj immediateContainer = getContainer();
            if (immediateContainer != null) {
                NamedObj container = immediateContainer.getContainer();
                // NOTE: During cloning, the container reports that it is in a class definition!
                // Hence, this PublisherPort has to do the registering when clone is
                // set to no longer be a class definition.
                if (container instanceof InstantiableNamedObj
                        && !((InstantiableNamedObj)container).isWithinClassDefinition()) {
                    String newValue = channel.stringValue();
                    boolean globalValue = ((BooleanToken) global.getToken())
                            .booleanValue();
                    if (!newValue.equals(_channel) || globalValue != _global) {
                        if (container instanceof CompositeActor) {
                            // The vergil and config tests were failing because
                            // moml.EntityLibrary sometimes contains Subscribers.
                            try {
                                if (attribute == global) {
                                    if (_global && !globalValue) {
                                        // Changing from global to non-global.
                                        ((CompositeActor) container)
                                                .unregisterPublisherPort(_channel,
                                                this, true);
                                    }
                                }

                                if (attribute == channel
                                        && (!(_channel == null || _channel.trim()
                                        .equals("")))) {
                                    // Changing the channel from a previous channel name. 
                                    if (((BooleanToken) propagateNameChanges
                                            .getToken()).booleanValue()) {
                                        try {
                                            _updateChannelNameOfConnectedSubscribers(
                                                    _channel, newValue);
                                        } catch (KernelException ex) {
                                            throw new IllegalActionException(this, ex,
                                                    "Failed to set channel to "
                                                            + newValue);
                                        }
                                    }
                                }

                                if (attribute == channel
                                        && (!(_channel == null || _channel.trim().equals("")))) {
                                    ((CompositeActor) container)
                                            .unregisterPublisherPort(_channel, this, _global);
                                }
                                ((CompositeActor) container).registerPublisherPort(
                                        newValue, this, globalValue);

                            } catch (NameDuplicationException e) {
                                throw new IllegalActionException(this, e,
                                        "Can't add published port.");
                            }
                        }
                        _channel = newValue;
                        _global = globalValue;
                    }
                }
            }
        } else {
            super.attributeChanged(attribute);
        }
    }
    
    /** Notify this object that the containment hierarchy above it has
     *  changed. This method does nothing because instead we use
     *  {@link #preinitialize()} to handle re-establishing the connections.
     *  @exception IllegalActionException If the change is not
     *   acceptable.
     */
    public void hierarchyChanged() throws IllegalActionException {
        // NOTE: It is not OK to access the cached variable _channel
        // here instead of the channel parameter because the parameter
        // may not have been validating (during instantiation of
        // actor-oriented classes).
        String channelValue = channel.stringValue();
        if (channelValue != null && !channelValue.equals("")) {
            NamedObj immediateContainer = getContainer();
            if (immediateContainer != null) {
                NamedObj container = immediateContainer.getContainer();
                if (container instanceof CompositeActor) {
                    try {
                        ((CompositeActor) container).registerPublisherPort(
                                channelValue, this, _global);
                    } catch (NameDuplicationException e) {
                        throw new InternalErrorException(e);
                    }
                }
            }
        }
    }

    /** Notify this object that the containment hierarchy above it will be
     *  changed, which results in 
     *  @exception IllegalActionException If unlinking to a published port fails.
     */
    public void hierarchyWillChange() throws IllegalActionException {
        if (channel != null) {
            String channelValue = channel.stringValue();
            NamedObj immediateContainer = getContainer();
            if (immediateContainer != null) {
                NamedObj container = immediateContainer.getContainer();
                if (container instanceof CompositeActor) {
                    try {
                        ((CompositeActor) container).unregisterPublisherPort(
                                channelValue, this, _global);
                    } catch (NameDuplicationException e) {
                        throw new InternalErrorException(e);
                    }
                }
            }
        }
    }

    /** Override the base class to register as a {@link HierarchyListener}
     *  so that we are notified of changes in the hierarchy above.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the action would result in a
     *   recursive containment structure, or if
     *   this entity and container are not in the same workspace.
     *  @exception NameDuplicationException If the container already has
     *   an entity with the name of this entity.
     */
    @Override
    public void setContainer(Entity container)
            throws IllegalActionException, NameDuplicationException {
        NamedObj previousContainer = super.getContainer();
        if (previousContainer != container && previousContainer != null) {
            previousContainer.removeHierarchyListener(this);
        }
        super.setContainer(container);
        if (container != null) {
            container.addHierarchyListener(this);
        }
    }
    
    /** Return a Set of SubscriberPort that are connected to this Publisher.
     *  @return A Set of Subscribers that are connected to this Publisher
     *  @exception KernelException If thrown when a Manager is added to
     *  the top level or if preinitialize() fails.
     */
    public Set<SubscriberPort> subscribers()
            throws KernelException {
        // preinitialize() creates connections between Publishers and
        // Subscribers.
        Manager.preinitializeThenWrapup((Actor)getContainer());
        return _dependents(this);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Cached channel name, for publish and subscribe. */
    protected String _channel;

    /** Cached variable indicating whether publishing or subscribing is global. */
    protected boolean _global;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** If the specified port is an instance of
     *  {@link SubscriberPort}, then return a set containing it;
     *  otherwise, return a set of SubscriberPort instances that downstream
     *  of the specified port that subscribe to this publisher.
     *  This method traverses opaque composites.   
     *  @param port The port to be checked   
     *  @param filter The class of dependent actors to be returned.
     *  @return The Set of all AtomicActors connected to the port.
     */
    private Set<SubscriberPort> _dependents(IOPort port) 
            throws IllegalActionException {
        //System.out.println("ActorDependencies._dependents: START" + remotePort.getFullName());
        Set<SubscriberPort> results = new HashSet<SubscriberPort>();
        if (port instanceof SubscriberPort) {
            results.add((SubscriberPort)port);
        } else {
            Receiver[][] receivers = null;
            if (port.isOutput() && port.isInput()) {
                throw new IllegalActionException(port, "Can't handle port that is both input and output.");
            }
            if (port.isOutput()) {
                receivers = port.getRemoteReceivers();
            } else if (port.isInput()) {
                receivers = port.deepGetReceivers();
            } else {
                throw new IllegalActionException(port, "Can't handle port that is neither input nor output.");
            }
            if (receivers != null) {
                for (int i = 0; i < receivers.length; i++) {
                    if (receivers[i] != null) {
                        for (int j = 0; j < receivers[i].length; j++) {
                            if (receivers[i][j] != null) {
                                IOPort remotePort2 = receivers[i][j].getContainer();
                                if (remotePort2 != null) {
                                    results.addAll(_dependents(remotePort2));
                                }
                            }
                        }
                    }
                }
            }
        }
        return results;
    }

    /** Update the channel name of any connected SubscriberPorts.
     *  Note that the channel name of connected SubscriptionAggregatorPorts
     *  are not updated.
     *  @param previousChannelName The previous name of the channel.
     *  @param newChannelName The new name of the channel.
     *  @exception KernelException If thrown when a Manager is added to
     *  the top level, or when the channel name of a Subscriber is changed.
     */
    private void _updateChannelNameOfConnectedSubscribers(
            String previousChannelName, String newChannelName)
            throws KernelException {

        // We use subscribers() here so that we get any subscribers in
        // Opaque TypedCompositeActors.
        for (SubscriberPort port : subscribers()) {
            if (port.channel.getExpression().equals(previousChannelName)) {
                // Avoid updating SubscriptionAggregators that have regular
                // expressions that are different than the Publisher channel name.
                port.channel.setExpression(newChannelName);
                port.attributeChanged(port.channel);
            }
        }
    }
}
