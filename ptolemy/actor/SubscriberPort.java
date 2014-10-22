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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ptolemy.actor.util.DFUtilities;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.InstantiableNamedObj;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// SubscriberPort

/**
 This is a specialized input port that subscribes to data sent
 to it on the specified named channel.
 The tokens are "tunneled" from an instance of
 {@link PublisherPort} that names the same channel.
 If {@link #global} is false (the default), then this subscriber
 will only see instances of PublisherPort that are under the
 control of the same director. That is, it can
 be at a different level of the hierarchy, or in an entirely different
 composite actor, as long as the relevant composite actors are
 transparent (have no director). If {@link #global} is true,
 then the publisher may be anywhere in the model, as long as its
 <i>global</i> parameter is also true.
 <p>
 Any number of instances of SubscriberPort can subscribe to the same
 channel.
 <p>
 This actor actually has a hidden input port that is connected
 to the publisher via hidden "liberal links" (links that are
 allowed to cross levels of the hierarchy).  Consequently,
 any data dependencies that the director might assume on a regular
 "wired" connection will also be assumed across Publisher-Subscriber
 pairs.  Similarly, type constraints will propagate across
 Publisher-Subscriber pairs. That is, the type of the Subscriber
 output will match the type of the Publisher input.

 @author Edward A. Lee, Contributor: Christopher Brooks
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class SubscriberPort extends PubSubPort {

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
    public SubscriberPort(ComponentEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        setOutput(false);
        setInput(true);

        // In order for this to show up in the vergil library, it has to have
        // an icon description.
        _attachText("_smallIconDescription", "<svg>\n"
                + "<polygon points=\"0,4 0,9 12,0 0,-9 0,-4 -8,-4 -8,4\" "
                + "style=\"fill:cyan\"/>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If a publish and subscribe channel is set, then set up the connections.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException Thrown if the new color attribute cannot
     *      be created.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == channel) {
            String newValue = channel.stringValue();
            if (!newValue.equals(_channel)) {
                NamedObj immediateContainer = getContainer();
                if (immediateContainer != null) {
                    NamedObj container = immediateContainer.getContainer();
                    if (container instanceof CompositeActor
                            && !(_channel == null || _channel.trim().equals(""))) {
                        ((CompositeActor) container).unlinkToPublishedPort(
                                _channel, this, _global);
                    }
                }
                _channel = newValue;
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
                                _channel, this, _global);
                    }
                }
            }
            _global = newValue;
            // Do not call SubscriptionAggregator.attributeChanged()
            // because it will remove the published port name by _channel.
            // If _channel is set to a real name (not a regex pattern),
            // Then chaos ensues.  See test 3.0 in SubscriptionAggregator.tcl
        } else if (attribute == initialTokens) {
            // Set the initial token parameter for the benefit of SDF.
            // If this port is not opaque, SDF will not see it, so we
            // will need in preinitialize() to set the init production
            // of the inside ports.
            Token initialOutputsValue = initialTokens.getToken();
            if (initialOutputsValue != null) {
                if (!(initialOutputsValue instanceof ArrayToken)) {
                    throw new IllegalActionException(this,
                            "initialOutputs value is required to be an array.");
                }
                int length = ((ArrayToken) initialOutputsValue).length();
                DFUtilities.setOrCreate(this, "tokenInitProduction", length);
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Notify this object that the containment hierarchy above it has
     *  changed. This restores the tokenInitConsumption parameters of
     *  any ports that had that parameter changed in a previous
     *  call to preinitialize().
     *  @exception IllegalActionException If the change is not
     *   acceptable.
     */
    @Override
    public void hierarchyChanged() throws IllegalActionException {
        // If we have previously set the tokenInitConsumption variable
        // of some port, restore it now to its original value.
        if (_tokenInitConsumptionSet != null) {
            for (IOPort port : _tokenInitConsumptionSet.keySet()) {
                String previousValue = _tokenInitConsumptionSet.get(port);
                Variable variable = DFUtilities.getRateVariable(port,
                        "tokenInitConsumption");
                if (previousValue == null) {
                    try {
                        variable.setContainer(null);
                    } catch (NameDuplicationException e) {
                        // Should not occur.
                        throw new InternalErrorException(e);
                    }
                } else {
                    variable.setExpression(previousValue);
                }
            }
        }
        super.hierarchyChanged();
    }

    /** Notify this object that the containment hierarchy above it will be
     *  changed, which results in the channel being unlinked from the publisher.
     *  @exception IllegalActionException If unlinking to a published port fails.
     */
    @Override
    public void hierarchyWillChange() throws IllegalActionException {
        if (channel != null) {
            String channelValue = null;
            try {
                // The channel may refer to parameters via $
                // but the parameters are not yet in scope.
                channelValue = channel.stringValue();
            } catch (Throwable throwable) {
                channelValue = channel.getExpression();
            }
            if (channelValue != null) {
                NamedObj immediateContainer = getContainer();
                if (immediateContainer != null) {
                    NamedObj container = immediateContainer.getContainer();
                    if (container instanceof CompositeActor) {
                        ((CompositeActor) container).unlinkToPublishedPort(
                                channelValue, this);
                    }
                }
            }
        }
        super.hierarchyWillChange();
    }

    /** If {@link #initialTokens} has been set, then make available the
     *  inputs specified by its array value.
     */
    @Override
    public void initialize() throws IllegalActionException {
        if (((InstantiableNamedObj) getContainer()).isWithinClassDefinition()) {
            // Don't initialize Class Definitions.
            // FIXME: Probably shouldn't even be a registered Initializable.
            // See $PTII/ptolemy/actor/lib/test/auto/PublisherToplevelSubscriberPortAOC.xml
            return;
        }
        // If the publisher port is not opaque and is an instance of
        // ConstantPublisherPort, then we have some work to do. If
        // this port is opaque, we set it to return a constant value
        // provided by the ConstantPublisherPort. If not, then we have
        // set the inside destination ports to return constant values.
        if (_publisherPort instanceof ConstantPublisherPort) {
            Token constantToken = ((ConstantPublisherPort) _publisherPort).constantValue
                    .getToken();
            Token limitToken = ((ConstantPublisherPort) _publisherPort).numberOfTokens
                    .getToken();
            int limit = ((IntToken) limitToken).intValue();
            if (isOpaque()) {
                _setConstant(constantToken, limit);
            } else {
                // NOTE: insideSinkPortList() doesn't work here if the
                // port is transparent. The returned list is empty,
                // unfortunately, so we have duplicate that functionality
                // here.
                Director dir = ((Actor) getContainer()).getDirector();
                int depthOfDirector = dir.depthInHierarchy();
                LinkedList<IOPort> insidePorts = new LinkedList<IOPort>();
                Iterator<?> ports = deepInsidePortList().iterator();

                while (ports.hasNext()) {
                    IOPort port = (IOPort) ports.next();
                    int depth = port.getContainer().depthInHierarchy();

                    if (port.isInput() && depth >= depthOfDirector) {
                        insidePorts.addLast(port);
                    } else if (port.isOutput() && depth < depthOfDirector) {
                        insidePorts.addLast(port);
                    }
                }
                for (IOPort insidePort : insidePorts) {
                    insidePort._setConstant(constantToken, limit);
                }
            }
        }

        Token initialOutputsValue = initialTokens.getToken();
        if (initialOutputsValue instanceof ArrayToken) {
            // If this port is opaque, put the tokens into the receivers.
            if (isOpaque()) {
                Receiver[][] receivers = getReceivers();
                if (receivers != null) {
                    for (Receiver[] receiver : receivers) {
                        for (int j = 0; j < receivers.length; j++) {
                            for (Token token : ((ArrayToken) initialOutputsValue)
                                    .arrayValue()) {
                                receiver[j].put(token);
                            }
                        }
                    }
                }
            } else {
                // The port is not opaque.
                for (Token token : ((ArrayToken) initialOutputsValue)
                        .arrayValue()) {
                    for (int i = 0; i < getWidth(); i++) {
                        sendInside(i, token);
                    }
                }
            }
        }
    }

    /** Override the base class to ensure that there is a publisher.
     *  @exception IllegalActionException If there is no matching
     *   publisher, if the channel is not specified or if the port
     *   is in the top level.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        if (_channel == null) {
            throw new IllegalActionException(this, "No channel specified.");
        }
        NamedObj actor = getContainer();
        if (actor != null && actor.getContainer() == null) {
            throw new IllegalActionException(
                    this,
                    "SubscriberPorts cannot be used at the top level, use a Subscriber actor instead.");
        }
        if (((InstantiableNamedObj) getContainer()).isWithinClassDefinition()) {
            // Don't preinitialize Class Definitions.
            // See $PTII/ptolemy/actor/lib/test/auto/PublisherToplevelSubscriberPortAOC.xml
            return;
        }
        _updateLinks();
    }

    /** Override the base class to only accept setting to be an input.
     *  @param isInput True to make the port an input.
     *  @exception IllegalActionException If the argument is false.
     */
    @Override
    public void setInput(boolean isInput) throws IllegalActionException {
        if (!isInput) {
            throw new IllegalActionException(this,
                    "SubscriberPort is required to be an input port.");
        }
        super.setInput(true);
    }

    /** Override the base class to refuse to make the port an output.
     *  @param isOutput Required to be false.
     *  @exception IllegalActionException If the argument is true.
     */
    @Override
    public void setOutput(boolean isOutput) throws IllegalActionException {
        if (isOutput) {
            throw new IllegalActionException(this,
                    "SubscriberPort cannot be an output port.");
        }
        super.setOutput(false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Update the connection to the publisher, if there is one.
     *  Note that this method is computationally intensive for large
     *  models as it traverses the model by searching
     *  up the hierarchy for the nearest opaque container
     *  or the top level and then traverses the contained entities.
     *  Thus, avoid calling this method except when the model
     *  is running.
     *  @exception IllegalActionException If creating the link
     *   triggers an exception.
     */
    protected void _updateLinks() throws IllegalActionException {
        // If the channel has not been set, then there is nothing
        // to do.  This is probably the first setContainer() call,
        // before the object is fully constructed.
        if (_channel == null) {
            return;
        }

        NamedObj immediateContainer = getContainer();
        if (immediateContainer != null) {
            NamedObj container = immediateContainer.getContainer();
            if (container instanceof CompositeActor) {
                try {
                    IOPort publisherPort = null;
                    try {
                        publisherPort = ((CompositeActor) container)
                                .linkToPublishedPort(_channel, this, _global);
                    } catch (IllegalActionException ex) {
                        // If we have a LazyTypedCompositeActor that
                        // contains the Publisher, then populate() the
                        // model, expanding the LazyTypedCompositeActors
                        // and retry the link.  This is computationally
                        // expensive.
                        // See $PTII/ptolemy/actor/lib/test/auto/LazyPubSub.xml
                        _updatePublisherPorts((CompositeEntity) toplevel());
                        // Now try again.
                        try {
                            publisherPort = ((CompositeActor) container)
                                    .linkToPublishedPort(_channel, this,
                                            _global);
                        } catch (IllegalActionException ex2) {
                            // Rethrow with the "this" so that Go To Actor works.
                            throw new IllegalActionException(this, ex2,
                                    "Failed to update link.");
                        }
                    }
                    // Set the init consumption parameter for this port, or if this
                    // port is not opaque, for the opaque ports connected to it on the inside.
                    // The init consumption will be the sum of the number of initial
                    // tokens this port has and the number of initial tokens produced
                    // by the publisher port if it is not opaque (if it is opaque, then
                    // its token init production parameter will be seen by the scheduler).
                    int length = 0;

                    Token initialOutputsValue = initialTokens.getToken();
                    if (initialOutputsValue != null) {
                        length = ((ArrayToken) initialOutputsValue).length();
                    }

                    // If the publisherPort has initial production and is not opaque,
                    // then for the benefit of SDF we need to set the tokenInitConsumption
                    // parameter here so that the SDF scheduler knows that initial tokens
                    // will be available.
                    if (!publisherPort.isOpaque()) {
                        length += DFUtilities
                                .getTokenInitProduction(publisherPort);
                    }
                    _publisherPort = publisherPort;

                    if (length > 0) {
                        if (isOpaque()) {
                            DFUtilities.setOrCreate(this,
                                    "tokenInitConsumption", length);
                        } else {
                            // If this port is not opaque, then we have
                            // to set the parameter for inside ports that will
                            // actually receive the initial token.
                            if (_tokenInitConsumptionSet == null) {
                                _tokenInitConsumptionSet = new HashMap<IOPort, String>();
                            }
                            List<IOPort> insidePorts = deepInsidePortList();
                            for (IOPort port : insidePorts) {
                                Variable previousVariable = DFUtilities
                                        .getRateVariable(port,
                                                "tokenInitConsumption");
                                if (previousVariable == null) {
                                    _tokenInitConsumptionSet.put(port, null);
                                } else {
                                    String previousValue = previousVariable
                                            .getExpression();
                                    _tokenInitConsumptionSet.put(port,
                                            previousValue);
                                }
                                DFUtilities.setOrCreate(port,
                                        "tokenInitConsumption", length);
                            }
                        }
                    }
                } catch (NameDuplicationException e) {
                    throw new IllegalActionException(this, e,
                            "Can't link SubscriptionAggregatorPort with a PublisherPort.");
                }
            }
        }
    }

    /** Traverse the model, starting at the specified object
     *  and examining objects below it in the hierarchy, to find
     *  all instances of PublisherPort and make sure that they have
     *  registered their port. This method defeats lazy composites
     *  and is expensive to execute.
     *  @param root The root of the tree to search.
     *  @exception IllegalActionException If the port rejects its channel.
     */
    protected void _updatePublisherPorts(Entity root)
            throws IllegalActionException {
        List<Port> ports = root.portList();
        for (Port port : ports) {
            if (port instanceof PublisherPort) {
                // FIXME: Not sure if this is necessary
                StringParameter channel = ((PublisherPort) port).channel;
                channel.validate();
                port.attributeChanged(channel);
            }
        }
        if (root instanceof CompositeEntity) {
            List<Entity> entities = ((CompositeEntity) root).entityList();
            for (Entity entity : entities) {
                _updatePublisherPorts(entity);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The associated publisherPort, found during preinitialize(). */
    private IOPort _publisherPort;

    /** Set of ports whose tokenInitConsumption variable has been set
     *  in preinitialize to something other than 0. This is needed so
     *  that these variables can be unset if the hierarchy changes.
     */
    private Map<IOPort, String> _tokenInitConsumptionSet;
}
