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
 */
package ptolemy.actor;

import java.util.List;

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.HierarchyListener;
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

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 9.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class SubscriberPort extends TypedIOPort 
        implements HierarchyListener, Initializable {

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
        channel = new StringParameter(this, "channel");
        
        global = new Parameter(this, "global");
        global.setTypeEquals(BaseType.BOOLEAN);
        global.setExpression("false");
        
        setOutput(false);
        setInput(true);
        
        // In order for this to show up in the vergil library, it has to have
        // an icon description.
        _attachText("_smallIconDescription", "<svg>\n"
                + "<polygon points=\"0,4 0,9 12,0 0,-9 0,-4 -8,-4 -8,4\" "
                + "style=\"fill:cyan\"/>\n" + "</svg>\n");
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** Specification of whether the published data is global.
     *  This is ignored if {@link #channel} is empty.
     *  If this is set to true, then a subscriber anywhere in the model that
     *  references the same channel by name will see values published by
     *  this port. If this is set to false (the default), then only
     *  those subscribers that are controlled by the same director will see
     *  values published on this channel.
     */
    public Parameter global;

    /** If set, then this port is used to communicate over a named
     *  publish and subscribe channel, rather than over manually
     *  established connections.
     */
    public StringParameter channel;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Throw an exception. Add initializables to the container. */
    @Override
    public void addInitializable(Initializable initializable) {
        throw new InternalErrorException("Cannot add Initializables to SubscriberPort.");
    }

    /** If a publish and subscribe channel is set, then set up the connections.
     *  If a quantity manager is added, removed or modified update the list of
     *  quantity managers.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException Thrown if the new color attribute cannot
     *      be created.
     */
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
                    _channel = newValue;
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
                                _channel, this, _global);
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
    
    /** Notify this object that the containment hierarchy above it has
     *  changed. This method does nothing because instead we use
     *  {@link #preinitialize()} to handle re-establishing the connections.
     *  @exception IllegalActionException If the change is not
     *   acceptable.
     */
    public void hierarchyChanged() throws IllegalActionException {
        // Make sure we are registered as to be initialized
        // with the container.
        Initializable container = _getInitializableContainer();
        if (container != null) {
            container.addInitializable(this);
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
                    ((CompositeActor) container).unlinkToPublishedPort(
                            channelValue, this);
                }
            }
        }
        // Unregister to be initialized with the initializable container.
        // We will be re-registered when hierarchyChanged() is called.
        Initializable container = _getInitializableContainer();
        if (container != null) {
            container.removeInitializable(this);
        }
    }
    
    /** Do nothing. */
    @Override
    public void initialize() throws IllegalActionException {
    }

    /** Override the base class to ensure that there is a publisher.
     *  @exception IllegalActionException If there is no matching
     *   publisher.
     */
    public void preinitialize() throws IllegalActionException {
        if (_channel == null) {
            throw new IllegalActionException(this, "No channel specified.");
        }
        _updateLinks();
    }

    /** Do nothing. */
    @Override
    public void removeInitializable(Initializable initializable) {
    }

    /** Override the base class to register as an 
     *  {@link Initializable}
     *  so that preinitialize() is invoked, and as a
     *  {@link HierarchyListener}, so that we are notified of
     *  changes in the hiearchy above.
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
        Initializable previousInitializableContainer = _getInitializableContainer();
        NamedObj previousContainer = getContainer();
        super.setContainer(container);
        Initializable newInitializableContainer = _getInitializableContainer();
        if (previousInitializableContainer != newInitializableContainer) {
            if (previousInitializableContainer != null) {
                previousInitializableContainer.removeInitializable(this);
            }
            if (newInitializableContainer != null) {
                newInitializableContainer.addInitializable(this);
            }
        }
        if (previousContainer != container) {
            if (previousContainer != null) {
                previousContainer.removeHierarchyListener(this);
            }
            if (container != null) {
                container.addHierarchyListener(this);
            }
        }
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
    
    /** Do nothing. */
    @Override
    public void wrapup() throws IllegalActionException {
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
                    try {
                        ((CompositeActor) container).linkToPublishedPort(
                                _channel, this, _global);
                    } catch (IllegalActionException ex) {
                        // If we have a LazyTypedCompositeActor that
                        // contains the Publisher, then populate() the
                        // model, expanding the LazyTypedCompositeActors
                        // and retry the link.  This is computationally
                        // expensive.
                        // See $PTII/ptolemy/actor/lib/test/auto/LazyPubSub.xml
                        _updatePublisherPorts((CompositeEntity)toplevel());
                        // Now try again.
                        ((CompositeActor) container).linkToPublishedPort(
                                _channel, this, _global);
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
     *  @throws IllegalActionException If the port rejects its channel.
     */
    protected void _updatePublisherPorts(Entity root) throws IllegalActionException {
        List<Port> ports = root.portList();
        for (Port port : ports) {
            if (port instanceof PublisherPort) {
                port.attributeChanged(((PublisherPort)port).channel);
            }
        }
        if (root instanceof CompositeEntity) {
            List<Entity> entities = ((CompositeEntity)root).entityList();
            for (Entity entity : entities) {
                _updatePublisherPorts(entity);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the first Initializable encountered above this
     *  in the hierarchy that will be initialized (i.e., it is either
     *  an atomic actor or an opaque composite actor).
     *  @return The first Initializable above this in the hierarchy,
     *   or null if there is none.
     */
    protected Initializable _getInitializableContainer() {
        NamedObj container = getContainer();
        while (container != null) {
            if (container instanceof Initializable) {
                if (container instanceof CompositeActor) {
                    if (((CompositeActor)container).isOpaque()) {
                        return (Initializable)container;
                    }
                } else {
                    return (Initializable)container;
                }
            }
            container = container.getContainer();
        }
        return null;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Cached channel name, for publish and subscribe. */
    protected String _channel;

    /** Cached variable indicating whether publishing or subscribing is global. */
    protected boolean _global;
}
