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

import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.InstantiableNamedObj;
import ptolemy.kernel.util.HierarchyListener;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// PubSubPort

/**
 An abstract base class for publisher and subscriber ports.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public abstract class PubSubPort extends TypedIOPort implements
        HierarchyListener, Initializable {

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
    public PubSubPort(ComponentEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        channel = new StringParameter(this, "channel");

        global = new Parameter(this, "global");
        global.setTypeEquals(BaseType.BOOLEAN);
        global.setExpression("false");

        initialTokens = new Parameter(this, "initialTokens") {
            /** Override the base class to to allow the type to be unknown.
             *  @return True if the current type is acceptable.
             */
            @Override
            public boolean isTypeAcceptable() {
                return super.isTypeAcceptable()
                        || getType().equals(BaseType.UNKNOWN);
            }
        };
        setTypeAtLeast(ArrayType.elementType(initialTokens));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** If set, then this port is used to communicate over a named
     *  publish and subscribe channel, rather than over manually
     *  established connections.
     */
    public StringParameter channel;

    /** Specification of whether the published data is global.
     *  This is ignored if {@link #channel} is empty.
     *  If this is set to true, then a subscriber anywhere in the model that
     *  references the same channel by name will see values published by
     *  this port. If this is set to false (the default), then only
     *  those subscribers that are controlled by the same director will see
     *  values published on this channel.
     */
    public Parameter global;

    /** The values that will be made available in the initialize method.
     *  By default, this is empty, indicating that no initial tokens are
     *  available. If you wish for this port to have initial tokens,
     *  then give this parameter an array value specifying
     *  the sequence of initial values. If this is an output port,
     *  these initial values will be sent in the initialize() phase.
     *  If this is an input port, then these initial values will be
     *  available for reading after the initialize() phase.
     *  Changes to this parameter after initialize() has been invoked
     *  are ignored until the next execution of the model.
     */
    public Parameter initialTokens;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Throw an exception.
     * Adding initializables to the container is not supported.
     */
    @Override
    public void addInitializable(Initializable initializable) {
        throw new InternalErrorException(
                "Cannot add Initializables to publisher and subscriber ports.");
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then resets the type constraints.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        PubSubPort newObject = (PubSubPort) super.clone(workspace);

        // Set the type constraints.
        try {
            newObject.setTypeAtLeast(ArrayType
                    .elementType(newObject.initialTokens));
        } catch (IllegalActionException e) {
            throw new InternalErrorException(e);
        }

        return newObject;
    }

    /** Notify this object that the containment hierarchy above it has
     *  changed. This method does nothing because instead we use
     *  {@link #preinitialize()} to handle re-establishing the connections.
     *  @exception IllegalActionException If the change is not
     *   acceptable.
     */
    @Override
    public void hierarchyChanged() throws IllegalActionException {
        // Make sure we are registered as to be initialized
        // with the container.
        Initializable container = _getInitializableContainer();
        if (container != null) {
            container.addInitializable(this);
        }
    }

    /** Notify this object that the containment hierarchy above it will be
     *  changed, which results in this port being removed from the set
     *  of initializables of the container.
     *  @exception IllegalActionException If unlinking to a published port fails.
     */
    @Override
    public void hierarchyWillChange() throws IllegalActionException {
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

    /** Do nothing.  Subclasses should check to see if the port
     *  is in the top level and throw an exception that suggests
     *  using a Publisher or Subscriber.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
    }

    /** Do nothing. */
    @Override
    public void removeInitializable(Initializable initializable) {
    }

    /** Override the base class to register as an
     *  {@link Initializable}
     *  so that preinitialize() is invoked, and as a
     *  {@link HierarchyListener}, so that we are notified of
     *  changes in the hierarchy above.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the action would result in a
     *   recursive containment structure, or if
     *   this entity and container are not in the same workspace.
     *  @exception NameDuplicationException If the container already has
     *   an entity with the name of this entity.
     */
    @Override
    public void setContainer(Entity container) throws IllegalActionException,
            NameDuplicationException {
        Initializable previousInitializableContainer = _getInitializableContainer();
        NamedObj previousContainer = getContainer();
        if (previousContainer != container) {
            hierarchyWillChange();
            try {
                super.setContainer(container);
                Initializable newInitializableContainer = _getInitializableContainer();
                if (previousInitializableContainer != newInitializableContainer) {
                    if (previousInitializableContainer != null) {
                        previousInitializableContainer
                                .removeInitializable(this);
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
            } finally {
                hierarchyChanged();
            }
        }
    }

    /** Do nothing. */
    @Override
    public void wrapup() throws IllegalActionException {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Cached channel name, for publish and subscribe. */
    protected String _channel;

    /** Cached variable indicating whether publishing or subscribing is global. */
    protected boolean _global;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return the first Initializable encountered above this
     *  in the hierarchy that will be initialized (i.e., it is either
     *  an atomic actor or an opaque composite actor).
     *  @return The first Initializable above this in the hierarchy,
     *   or null if there is none.
     */
    private Initializable _getInitializableContainer() {
        NamedObj container = getContainer();
        if (container instanceof InstantiableNamedObj) {
            if (((InstantiableNamedObj) container).isWithinClassDefinition()) {
                return null;
            }
        }
        while (container != null) {
            if (container instanceof Initializable) {
                if (container instanceof CompositeActor) {
                    if (((CompositeActor) container).isOpaque()) {
                        return (Initializable) container;
                    }
                } else {
                    return (Initializable) container;
                }
            }
            container = container.getContainer();
        }
        return null;
    }
}
