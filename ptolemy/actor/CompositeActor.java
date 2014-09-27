/* An aggregation of actors.

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

 FIXME: things to review:
 setDirector throws NameDuplicationException
 preinitialize: validate attributes of this composite and
 the attributes of its ports.
 setDirector invalidatesSchedule of executiveDirector.
 moved invalidation code from _addEntity to _finishedAddEntity
 initialize now clears receivers.. This helps SampleDelay inside a modal models with reset transition work better.
 */
package ptolemy.actor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.util.BooleanDependency;
import ptolemy.actor.util.CausalityInterface;
import ptolemy.actor.util.CausalityInterfaceForComposites;
import ptolemy.actor.util.Dependency;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.InstantiableNamedObj;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelRuntimeException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// CompositeActor

/**
 A CompositeActor is an aggregation of actors.  It may have a
 <i>local director</i>, which is an attribute of class Director that
 is responsible for executing the contained actors.
 At the top level of a hierarchy, a composite actor (the toplevel
 CompositeActor of the topology) will normally exist with a local Director,
 and no container.  A composite actor at a lower level
 of the hierarchy may also have a local director.  A composite actor
 with a local director is <i>opaque</i>, and serves the role of the
 <i>wormhole</i> from Ptolemy Classic. Its ports are opaque, but it can
 contain actors and relations.  The toplevel composite actor is also
 associated with a Manager object that is responsible for managing
 any execution within the topology at a high level.
 <p>
 The <i>executive director</i> of a composite actor is the local director of
 the actor's container.   The toplevel composite actor has no executive
 director, and getExecutiveDirector will return null.   For transparent
 composite actors, the executive director and the local director will be the
 same.
 <p>
 The getDirector() method returns the local director if there is one.
 Otherwise, it returns the <i>executive director</i> of the CompositeActor,
 if there is one.  Whatever it returns is called (simply) the
 <i>director</i> of the composite (it may be local or executive).  This
 Director is responsible for the execution of all the actors contained
 within the composite actor.
 <p>
 A composite actor must have an executive director in order to communicate with
 the hierarchy around it.   In fact, it cannot even receive data in its
 input ports without an executive director, since the executive director
 is responsible for supplying the receivers to the ports.
 The toplevel composite actor has no executive director and cannot have
 ports that transmit data, but it can still be executed as long as it has a
 local director.  If the getDirector() method returns null, then the
 composite is not executable.
 <p>
 When a composite actor has both a director and an executive director, then
 the model of computation implemented by the director need not be the
 same as the model of computation implemented by the executive director.
 This is the source of the hierarchical heterogeneity in Ptolemy II.
 Multiple models of computation can be cleanly nested.
 <p>
 The ports of a CompositeActor are constrained to be IOPorts, the
 relations to be IORelations, and the actors to be instances of
 ComponentEntity that implement the Actor interface.  Derived classes
 may impose further constraints by overriding newPort(), _addPort(),
 newRelation(), _addRelation(), and _addEntity().

 @author Mudit Goel, Edward A. Lee, Lukito Muliadi, Steve Neuendorffer, Contributor: Daniel Crawl, Bert Rodiers
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Green (cxh)
 @Pt.AcceptedRating Yellow (neuendor)
 @see ptolemy.actor.IOPort
 @see ptolemy.actor.IORelation
 @see ptolemy.kernel.ComponentEntity
 @see ptolemy.actor.Director
 @see ptolemy.actor.Manager
 */
public class CompositeActor extends CompositeEntity implements Actor,
        FiringsRecordable {
    /** Construct a CompositeActor in the default workspace with no container
     *  and an empty string as its name. Add the actor to the workspace
     *  directory.
     *  You should set a director before attempting to execute it.
     *  You should set the container before sending data to it.
     *  Increment the version number of the workspace.
     */
    public CompositeActor() {
        super();
        _relationWidthInference = new RelationWidthInference(this);
    }

    /** Construct a CompositeActor in the specified workspace with no container
     *  and an empty string as a name. You can then change the name with
     *  setName(). If the workspace argument is null, then use the default
     *  workspace.
     *  You should set a director before attempting to execute it.
     *  You should set the container before sending data to it.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the actor.
     */
    public CompositeActor(Workspace workspace) {
        super(workspace);
        _relationWidthInference = new RelationWidthInference(this);
    }

    /** Create an actor with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  This actor will have no
     *  local director initially, and its executive director will be simply
     *  the director of the container.
     *  You should set a director before attempting to execute it.
     *
     *  @param container The container actor.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CompositeActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Append a listener to the current set of actor firing listeners.
     *  If the listener is already in the set, it will not be added again.
     *  Note that this method is basically the same as addDebugListener
     *  in the class NamedObj.
     *  @param listener The listener to which to send actor firing messages.
     *  @see #removeActorFiringListener(ActorFiringListener)
     */
    @Override
    public void addActorFiringListener(ActorFiringListener listener) {
        // NOTE: This method needs to be synchronized to prevent two
        // threads from each creating a new _actorFiringListeners list.
        synchronized (this) {
            if (_actorFiringListeners == null) {
                _actorFiringListeners = new LinkedList<ActorFiringListener>();
            }
        }

        // NOTE: This has to be synchronized to prevent
        // concurrent modification exceptions.
        synchronized (_actorFiringListeners) {
            if (_actorFiringListeners.contains(listener)) {
                return;
            } else {
                _actorFiringListeners.add(listener);
            }

            _notifyingActorFiring = true;
        }
    }

    /** Add the specified object to the set of objects whose
     *  preinitialize(), initialize(), and wrapup()
     *  methods should be invoked upon invocation of the corresponding
     *  methods of this object.
     *  @param initializable The object whose methods should be invoked.
     *  @see #removeInitializable(Initializable)
     *  @see #addPiggyback(Executable)
     */
    @Override
    public void addInitializable(Initializable initializable) {
        if (_initializables == null) {
            _initializables = new LinkedHashSet<Initializable>();
        }
        _initializables.add(initializable);
    }

    /** Add the specified object to the set of objects whose action
     *  methods should be invoked upon invocation of the corresponding
     *  actions methods of this object. These methods will be invoked
     *  before the corresponding methods of this object.
     *  @param piggyback The piggyback object.
     *  @see #addInitializable(Initializable)
     *  @see #removePiggyback(Executable)
     */
    public void addPiggyback(Executable piggyback) {
        if (_piggybacks == null) {
            _piggybacks = new LinkedHashSet<Executable>();
        }
        _piggybacks.add(piggyback);
    }

    /** Clone the actor into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a composite actor with clones of the ports of the
     *  original actor, the contained actors, and the contained relations.
     *  The ports of the returned actor are not connected to anything.
     *  The connections of the relations are duplicated in the new composite,
     *  unless they cross levels, in which case an exception is thrown.
     *  The local director is cloned, if there is one.
     *  The executive director is not cloned.
     *  NOTE: This will not work if there are level-crossing transitions.
     *
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If the actor contains
     *   level crossing transitions so that its connections cannot be cloned,
     *   or if one of the attributes cannot be cloned.
     *  @return A new CompositeActor.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        // Some local variables are written to by constructors of contained
        // actors. Those variables need to be set to null _before_ cloning
        // so that the new instance gets its own version.
        Set<Initializable> oldInitializables = _initializables;
        _initializables = null;
        Map<String, Set<IOPort>> oldPublishedPorts = _publishedPorts;
        _publishedPorts = null;
        Map<String, IORelation> oldPublisherRelations = _publisherRelations;
        Director oldDirector = _director;
        _director = null;
        Set<Executable> oldPiggybacks = _piggybacks;
        _piggybacks = null;

        CompositeActor newObject = (CompositeActor) super.clone(workspace);

        _initializables = oldInitializables;
        _publishedPorts = oldPublishedPorts;
        _publisherRelations = oldPublisherRelations;
        _director = oldDirector;
        _piggybacks = oldPiggybacks;

        newObject._actorFiringListeners = null;
        newObject._notifyingActorFiring = false;
        newObject._causalityInterface = null;
        newObject._derivedPiggybacks = null;
        newObject._manager = null;
        newObject._inputPortsVersion = -1;
        newObject._outputPortsVersion = -1;
        newObject._causalityInterfaceDirector = null;
        newObject._receiversVersion = -1L;
        // Don't set _relationWidthInference to null, see 7.1.5 in CompositeActor.tcl
        //newObject._relationWidthInference = null;
        newObject._relationWidthInference = new RelationWidthInference(
                newObject);
        return newObject;
    }

    /** Invalidate the schedule and type resolution and create
     *  new receivers if the specified port is an opaque
     *  output port.  Also, notify the containers of any ports
     *  deeply connected on the inside by calling their connectionsChanged()
     *  methods, since their width may have changed.
     *  @param port The port that has connection changes.
     */
    @Override
    public void connectionsChanged(Port port) {
        if (_debugging) {
            _debug("Connections changed on port: " + port.getName());
        }

        super.connectionsChanged(port);

        if (port instanceof ComponentPort) {
            // NOTE: deepInsidePortList() is not the right thing here
            // since it will return the same port if it is opaque.
            Iterator<?> insidePorts = ((ComponentPort) port).insidePortList()
                    .iterator();

            try {
                _inConnectionsChanged = true;

                while (insidePorts.hasNext()) {
                    ComponentPort insidePort = (ComponentPort) insidePorts
                            .next();
                    Entity portContainer = (Entity) insidePort.getContainer();

                    // Avoid an infinite loop where notifications are traded.
                    if (!(portContainer instanceof CompositeActor)
                            || !((CompositeActor) portContainer)._inConnectionsChanged) {
                        portContainer.connectionsChanged(insidePort);
                    }
                }
            } finally {
                _inConnectionsChanged = false;
            }
        }

        if (port instanceof IOPort) {
            IOPort castPort = (IOPort) port;

            if (castPort.isOpaque()) {
                Manager manager = getManager();

                if (castPort.isOutput() && getDirector() != null
                        && manager != null
                        && manager.getState() != Manager.IDLE
                        && manager.getState() != Manager.INFERING_WIDTHS
                        && manager.getState() != Manager.PREINITIALIZING) {
                    // Note that even if castPort is opaque, we still have to
                    // check for director above.
                    try {
                        castPort.createReceivers();
                    } catch (IllegalActionException ex) {
                        // Should never happen.
                        throw new InternalErrorException(this, ex,
                                "Cannot create receivers");
                    }
                }

                if (castPort.isInput() && getExecutiveDirector() != null
                        && manager != null
                        && manager.getState() != Manager.IDLE
                        && manager.getState() != Manager.INFERING_WIDTHS
                        && manager.getState() != Manager.PREINITIALIZING) {
                    try {
                        castPort.createReceivers();
                    } catch (IllegalActionException ex) {
                        // Should never happen.
                        throw new InternalErrorException(this, ex,
                                "Cannot create receivers");
                    }
                }

                // Invalidate the local director schedule and types
                if (getDirector() != null) {
                    getDirector().invalidateSchedule();
                    getDirector().invalidateResolvedTypes();
                }
            }
        }
    }

    /** If this actor is opaque, transfer any data from the input ports
     *  of this composite to the ports connected on the inside, and then
     *  invoke the fire() method of its local director.
     *  The transfer is accomplished by calling the transferInputs() method
     *  of the local director (the exact behavior of which depends on the
     *  domain).  If the actor is not opaque, throw an exception.
     *  This method is read-synchronized on the workspace, so the
     *  fire() method of the director need not be (assuming it is only
     *  called from here).  After the fire() method of the director returns,
     *  send any output data created by calling the local director's
     *  transferOutputs method.
     *
     *  @exception IllegalActionException If there is no director, or if
     *   the director's fire() method throws it, or if the actor is not
     *   opaque.
     */
    @Override
    public void fire() throws IllegalActionException {
        if (_debugging) {
            _debug("Calling fire()");
        }

        try {
            _workspace.getReadAccess();

            // First invoke piggybacked methods.
            if (_piggybacks != null) {
                // Invoke the fire() method of each piggyback.
                for (Executable piggyback : _piggybacks) {
                    piggyback.fire();
                }
            }
            if (_derivedPiggybacks != null) {
                // Invoke the fire() method of each piggyback.
                for (Executable piggyback : _derivedPiggybacks) {
                    piggyback.fire();
                }
            }

            if (!isOpaque()) {
                throw new IllegalActionException(this,
                        "Cannot fire a non-opaque actor.");
            }

            _transferPortParameterInputs();

            // Use the local director to transfer inputs from
            // everything that is not a port parameter.
            // The director will also update the schedule in
            // the process, if necessary.
            for (Iterator<?> inputPorts = inputPortList().iterator(); inputPorts
                    .hasNext() && !_stopRequested;) {
                IOPort p = (IOPort) inputPorts.next();

                if (!(p instanceof ParameterPort)) {
                    _director.transferInputs(p);
                }
            }

            if (_stopRequested) {
                return;
            }

            _director.fire();

            if (_stopRequested) {
                return;
            }

            // Use the local director to transfer outputs.
            _director.transferOutputs();
        } finally {
            _workspace.doneReading();
        }

        if (_debugging) {
            _debug("Called fire()");
        }
    }

    /** Create receivers for each port. If the port is an
     *  input port, then receivers are created for outside
     *  connections. If it is an output port, then receivers
     *  are created for inside connections. This method replaces
     *  any pre-existing receivers, so any data they contain
     *  will be lost.
     *  @exception IllegalActionException If any port throws it.
     */
    @Override
    public void createReceivers() throws IllegalActionException {

        // NOTE: It really doesn't help to track the _receiversVersion
        // here because if there is more than one composite actor
        // in the model, then the workspace version will be changed
        // when createReceivers() is called on the next one, so this
        // one will think next time that the workspace has changed.
        // This optimization needs to be done higher in the hierarchy.
        if (workspace().getVersion() != _receiversVersion) {
            List portList = new LinkedList(portList());
            Iterator<?> ports = portList.iterator();

            try {
                workspace().getWriteAccess();
                while (ports.hasNext()) {
                    IOPort onePort = (IOPort) ports.next();
                    onePort.createReceivers();
                }
                _receiversVersion = workspace().getVersion();
            } finally {
                // Note that this does not increment the workspace version.
                // We have not changed the structure of the model.
                workspace().doneTemporaryWriting();
            }
        }

        // Undid this change temporarily since the move of createReceivers breaks HDF
        /*
         for (Object actor : deepEntityList()) {
         ((Actor) actor).createReceivers();
         }
         */
    }

    /** Create the schedule for this model, if necessary.
     *  @exception IllegalActionException If the schedule can't be created.
     */
    public void createSchedule() throws IllegalActionException {
        for (Object actor : deepEntityList()) {
            if (actor instanceof CompositeActor) {
                ((CompositeActor) actor).createSchedule();
            }
        }
        getDirector().createSchedule();
    }

    /** Return a causality interface for this actor. This returns an
     *  instance of {@link CausalityInterfaceForComposites}.
     *  If this is called multiple times, the same object is returned each
     *  time unless the director has changed since the last call, in
     *  which case a new object is returned.
     *  @return A representation of the dependencies between input ports
     *   and output ports.
     */
    @Override
    public CausalityInterface getCausalityInterface() {
        // FIXME: Although this director may not have changed, a director
        // higher in the hierarchy may have changed. The base class Director
        // delegates up the hierarchy to decide what kind of Dependency to use,
        // so if a director higher in the hierarchy has changed, then we really
        // need to recreate the causality interface.
        // However, it doesn't seem to work to use the workspace version to
        // replace the causality interface! The reason seems to be that
        // if we do that, the causality interface gets recreated _after_
        // actors have had preinitialize() called, which is when they prune
        // their input/output dependencies.
        Director director = getDirector();
        if (_causalityInterface != null
                && _causalityInterfaceDirector == director) {
            return _causalityInterface;
        }
        if (director != null) {
            _causalityInterface = director.getCausalityInterface();
            _causalityInterfaceDirector = director;
            return _causalityInterface;
        }
        Dependency defaultDependency = BooleanDependency.OTIMES_IDENTITY;
        _causalityInterface = new CausalityInterfaceForComposites(this,
                defaultDependency);
        _causalityInterfaceDirector = null;
        return _causalityInterface;
    }

    /** Return the director responsible for execution of the contained
     *  actors.  This will be either the local director (if it exists) or the
     *  executive director (obtained using getExecutiveDirector()).
     *  This method is read-synchronized on the workspace.
     *
     *  @return The director responsible for invocation of inside actors.
     *  @see #setDirector(Director)
     */
    @Override
    public Director getDirector() {
        try {
            _workspace.getReadAccess();

            if (_director != null) {
                return _director;
            }
            // NOTE: It is slightly more efficient to
            // call this directly, rather than using
            // getExecutiveDirector(), and this gets
            // called alot.
            // NOTE: Cast should be safe because this
            // has to be contained by an actor to be run.
            Nameable container = getContainer();
            if (container instanceof Actor) {
                return ((Actor) container).getDirector();
            }
            return null;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Return the executive director of this CompositeActor.
     *  The container (if any) is queried for its (local) director.
     *  If it has none, or there
     *  is no container, then return null. This method is read-synchronized
     *  on the workspace.
     *
     *  @return The executive director of this composite actor.
     */
    @Override
    public Director getExecutiveDirector() {
        try {
            _workspace.getReadAccess();

            Nameable container = getContainer();

            if (container instanceof Actor) {
                return ((Actor) container).getDirector();
            }

            return null;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Get the manager responsible for execution of this composite actor.
     *  If this is the toplevel composite actor, then return what was
     *  set with setManager().
     *  For others, recursively call on the container, until the
     *  toplevel composite actor is reached.
     *  This method is read-synchronized on the workspace.
     *  @see #setManager(Manager)
     *
     *  @return The Manager of the topology that contains the composite actor.
     */
    @Override
    public Manager getManager() {
        try {
            _workspace.getReadAccess();

            if (_manager != null) {
                return _manager;
            }

            Nameable container = getContainer();

            if (container instanceof Actor) {
                return ((Actor) container).getManager();
            }

            return null;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Get the published port with named name.
     *  @param name The name of the published port.
     *  @return The port of the publisher that has named name.
     *  @exception IllegalActionException If the publisher can't be found.
     *  @exception NameDuplicationException If there are multiple
     *  publishers with the same name.
     */
    final public IOPort getPublishedPort(String name)
            throws IllegalActionException, NameDuplicationException {
        NamedObj container = getContainer();
        if (!isOpaque() && container instanceof CompositeActor
                && !((CompositeActor) container).isClassDefinition()) {
            return ((CompositeActor) container).getPublishedPort(name);
        } else {
            if (_publishedPorts == null) {
                throw new IllegalActionException(this,
                        "Can't find the publisher for \"" + name + "\".");
            }

            Set<IOPort> publishedPorts = _publishedPorts.get(name);
            if (publishedPorts == null || publishedPorts.size() == 0) {
                StringBuffer message = new StringBuffer();
                for (String keyName : _publishedPorts.keySet()) {
                    message.append(keyName + " ");
                }
                throw new IllegalActionException(this,
                        "Can't find the publisher for \"" + name
                                + "\"., names were: " + message);
            } else if (publishedPorts.size() > 1) {
                // Check to see if any of the publishedPorts are within a ClassDefinition.
                // FIXME: we should be able to do this before now, but when ports are being
                // registered, the container of the CompositeActor is often null, so we
                // can't tell if a port is in a ClassDefinition.
                // Test: $PTII/ptolemy/actor/lib/test/auto/PublisherPortSubscriberChannelVariablesAOC2.xml
                StringBuffer message = new StringBuffer();
                Iterator ports = publishedPorts.iterator();
                while (ports.hasNext()) {
                    IOPort port = (IOPort) ports.next();
                    InstantiableNamedObj actor = (InstantiableNamedObj) port
                            .getContainer();
                    if (actor != null && actor.isWithinClassDefinition()) {
                        ports.remove();
                    } else {
                        if (port instanceof PubSubPort) {
                            PubSubPort pubSubPort = (PubSubPort) port;
                            message.append(" port: " + pubSubPort + "name: "
                                    + pubSubPort.getName() + " channel: "
                                    + pubSubPort.channel + "\n");
                        }
                    }
                }
                if (publishedPorts.size() != 1) {
                    throw new NameDuplicationException(this, "We have "
                            + publishedPorts.size() + " ports with the name \""
                            + name + "\", which is not equal to 1.\n" + message);
                }
            }

            Iterator<IOPort> iterator = publishedPorts.iterator();
            return iterator.next();
        }
    }

    /** Get the published ports with names that match a regular expression.
     *  @param pattern The regular expression pattern to match.
     *  @return The ports of the publisher that match the regular expression.
     *  @exception IllegalActionException If the publisher can't be found.
     *  @exception NameDuplicationException If there are multiple
     *  publishers with the same name.
     */
    final public List<IOPort> getPublishedPorts(Pattern pattern)
            throws IllegalActionException, NameDuplicationException {
        List<IOPort> ports = new LinkedList<IOPort>();
        NamedObj container = getContainer();
        if (!isOpaque() && container instanceof CompositeActor
                && !((CompositeActor) container).isClassDefinition()) {
            return ((CompositeActor) container).getPublishedPorts(pattern);
        } else {
            if (_publishedPorts != null) {
                for (String name : _publishedPorts.keySet()) {
                    Matcher matcher = pattern.matcher(name);
                    if (matcher.matches()) {
                        ports.addAll(_publishedPorts.get(name));
                    }
                }
            }
        }

        return ports;
    }

    /** Get the channel name of a published port.
     * @param port The published port.
     * @return The name of the channel of the published port.
     */

    public String getPublishedPortChannel(IOPort port) {
        NamedObj container = getContainer();
        if (!isOpaque() && container instanceof CompositeActor
                && !((CompositeActor) container).isClassDefinition()) {
            return ((CompositeActor) container).getPublishedPortChannel(port);
        } else {
            if (_publishedPorts != null) {
                for (String name : _publishedPorts.keySet()) {
                    if (_publishedPorts.get(name).contains(port)) {
                        return name;
                    }
                }
            }
        }

        return "";
    }

    /** Get the name of the channel that the port subscribes to.
     * @param port The subscribed port.
     * @return The name of the channel.
     */

    public String getSubscribedPortChannel(IOPort port) {
        NamedObj container = getContainer();
        if (!isOpaque() && container instanceof CompositeActor
                && !((CompositeActor) container).isClassDefinition()) {
            return ((CompositeActor) container).getPublishedPortChannel(port);
        } else {
            if (_subscribedPorts != null) {
                for (String name : _subscribedPorts.keySet()) {
                    if (_subscribedPorts.get(name).contains(port)) {
                        return name;
                    }
                }
            }
        }

        return "";
    }

    /** Determine whether widths are currently being inferred or not.
     *  @return True When widths are currently being inferred.
     */
    public boolean inferringWidths() {
        RelationWidthInference widthInferenceAlgorithm = _getWidthInferenceAlgorithm();
        if (widthInferenceAlgorithm != null) {
            return widthInferenceAlgorithm.inferringWidths();
        } else {
            return false;
        }
    }

    /**
     *  Infer the width of the relations for which no width has been
     *  specified yet.
     *  The specified actor must be the top level container of the model.
     *  @exception IllegalActionException If the widths of the
     *  relations at port are not consistent or if the width cannot be
     *  inferred for a relation.
     */
    public void inferWidths() throws IllegalActionException {
        RelationWidthInference relationWidthInference = _getWidthInferenceAlgorithm();
        relationWidthInference.inferWidths();
    }

    /** Initialize this actor.  If this actor is opaque, invoke the
     *  initialize() method of its local director. Otherwise, throw an
     *  exception.  This method is read-synchronized on the workspace,
     *  so the initialize() method of the director need not be
     *  (assuming it is only called from here).
     *
     *  @exception IllegalActionException If there is no director, or
     *  if the director's initialize() method throws it, or if the
     *  actor is not opaque.
     */
    @Override
    public void initialize() throws IllegalActionException {
        if (_debugging) {
            _debug("Called initialize()");
        }

        try {
            _workspace.getReadAccess();

            if (!isOpaque()) {
                throw new IllegalActionException(this,
                        "Cannot initialize a non-opaque actor.");
            }

            // Clear all of the contained actor's input ports.
            for (Iterator<?> actors = deepEntityList().iterator(); actors
                    .hasNext();) {
                Entity actor = (Entity) actors.next();
                Iterator<?> ports = actor.portList().iterator();

                while (ports.hasNext()) {
                    IOPort port = (IOPort) ports.next();

                    // if port has default value reset the saved persistent value.
                    port.reset();

                    if (port.isInput()) {
                        // Reset all receivers.
                        Receiver[][] receivers = port.getReceivers();

                        if (receivers == null) {
                            throw new InternalErrorException(this, null,
                                    "port.getReceivers() returned null! "
                                            + "This should never happen. "
                                            + "port was '" + port + "'");
                        }

                        for (Receiver[] receivers2 : receivers) {
                            for (Receiver element : receivers2) {
                                element.reset();
                            }
                        }
                    }
                }
            }

            // Clear all of the output ports.
            Iterator<?> ports = portList().iterator();

            while (ports.hasNext()) {
                IOPort port = (IOPort) ports.next();

                if (port.isOutput()) {
                    // Clear all insideReceivers.
                    Receiver[][] receivers = port.getInsideReceivers();

                    for (Receiver[] receivers2 : receivers) {
                        for (Receiver element : receivers2) {
                            element.reset();
                        }
                    }
                }
            }

            // Next invoke initializable methods.
            // This should be done _after_ clearing the receivers above
            // because an initializable or piggyback may produce outputs.
            if (_initializables != null) {
                for (Initializable initializable : _initializables) {
                    initializable.initialize();
                }
            }

            // Next invoke piggybacked methods.
            if (_piggybacks != null) {
                // Invoke the initialize() method of each piggyback.
                for (Executable piggyback : _piggybacks) {
                    piggyback.initialize();
                }
            }
            if (_derivedPiggybacks != null) {
                // Invoke the initialize() method of each piggyback.
                for (Executable piggyback : _derivedPiggybacks) {
                    piggyback.initialize();
                }
            }

            // Note that this is assured of firing the local director,
            // not the executive director, because this is opaque.
            // The initialize() method of the local director must be called
            // after the ports are cleared, because the FixedPointDirector
            // relies on this to reset the status of its receivers.
            getDirector().initialize();
        } finally {
            _workspace.doneReading();
        }
    }

    /** List the input ports of this actor.
     *  Note that this method returns the ports directly
     *  contained by this actor, whether they are transparent or not.
     *  This method is read-synchronized on the workspace.
     *  @return A list of IOPort objects.
     */
    @Override
    public List inputPortList() {
        try {
            _workspace.getReadAccess();

            if (_inputPortsVersion != _workspace.getVersion()) {
                // Update the cache.
                List<IOPort> inputPorts = new LinkedList<IOPort>();
                Iterator<?> ports = portList().iterator();

                while (ports.hasNext()) {
                    IOPort p = (IOPort) ports.next();

                    if (p.isInput()) {
                        inputPorts.add(p);
                    }
                }

                _cachedInputPorts = inputPorts;
                _inputPortsVersion = _workspace.getVersion();
            }

            return _cachedInputPorts;
        } finally {
            _workspace.doneReading();
        }
    }

    /** If this actor is opaque, invoke the isFireFunctional() method
     *  of the local director and return its result. Otherwise, return true.
     *  Normally this method will not be invoked on a non-opaque
     *  composite actor.
     *
     *  @return True if the local director's isFireFunctional() method
     *   returns true or if this actor is not opaque.
     */
    @Override
    public boolean isFireFunctional() {
        if (isOpaque()) {
            // If any piggybacked object is not functional, then this object
            // is not functional.
            if (_piggybacks != null) {
                // Invoke the isFireFunctional() method of each piggyback.
                for (Executable piggyback : _piggybacks) {
                    if (!piggyback.isFireFunctional()) {
                        return false;
                    }
                }
            }
            if (_derivedPiggybacks != null) {
                // Invoke the isFireFunctional() method of each piggyback.
                for (Executable piggyback : _derivedPiggybacks) {
                    if (!piggyback.isFireFunctional()) {
                        return false;
                    }
                }
            }
            return getDirector().isFireFunctional();
        } else {
            return true;
        }
    }

    /** Return true if this actor contains a local director.
     *  Otherwise, return false.  This method is <i>not</i>
     *  synchronized on the workspace, so the caller should be.
     */
    @Override
    public boolean isOpaque() {
        return _director != null;
    }

    /** Return true if a port is in the published port list
     *  at this level.
     *  @param port The port to be checked against the list of published ports.
     *  @return True if the port was added with
     *  {@link #registerPublisherPort(String, IOPort, boolean)}.
     */
    public boolean isPublishedPort(IOPort port) {
        if (_publishedPorts != null) {
            for (Set<IOPort> ports : _publishedPorts.values()) {
                if (ports.contains(port)) {
                    return true;
                }
            }
        }

        return false;
    }

    /** Return false if all input ports have non-empty default values,
     *  or if this actor is opaque and the contained director indicates
     *  that it is non-strict.
     *  Normally this method will not be invoked on a non-opaque
     *  composite actor.
     *  Note that ParameterPort is not treated as having a default value
     *  because such ports might be used in a context where it is important
     *  to supply them with an input value.
     *  @return False if this actor does not need to be provided with
     *   inputs to fire.
     *  @exception IllegalActionException Thrown if causality interface
     *   cannot be computed, or if the defaultValue expression cannot be
     *   evaluated on an input port.
     */
    @Override
    public boolean isStrict() throws IllegalActionException {
        List<IOPort> ports = inputPortList();
        boolean foundAnInputPort = false;
        for (IOPort port : ports) {
            foundAnInputPort = true;
            if (port.defaultValue.getToken() == null) {
                // Found an input port with no default value.
                if (isOpaque()) {
                    return getDirector().isStrict();
                } else {
                    return true;
                }
            }
        }
        // Get to here if either all input ports have a default value
        // or there are no input ports.
        if (isOpaque() && foundAnInputPort) {
            return getDirector().isStrict();
        } else {
            return true;
        }
    }

    /** Invoke a specified number of iterations of the actor. An
     *  iteration is equivalent to invoking prefire(), fire(), and
     *  postfire(), in that order. In an iteration, if prefire()
     *  returns true, then fire() will be called once, followed by
     *  postfire(). Otherwise, if prefire() returns false, fire()
     *  and postfire() are not invoked, and this method returns
     *  NOT_READY. If postfire() returns false, then no more
     *  iterations are invoked, and this method returns STOP_ITERATING.
     *  Otherwise, it returns COMPLETED.  If stop() is called during
     *  this iteration, then cease iterating and return STOP_ITERATING.
     *  <p>
     *  This base class method actually invokes prefire(), fire(),
     *  and postfire(), as described above, but a derived class
     *  may override the method to execute more efficient code.
     *
     *  @param count The number of iterations to perform.
     *  @return NOT_READY, STOP_ITERATING, or COMPLETED.
     *  @exception IllegalActionException If iterating is not
     *   permitted, or if prefire(), fire(), or postfire() throw it.
     */
    @Override
    public int iterate(int count) throws IllegalActionException {
        if (_debugging) {
            _debug("Called iterate(" + count + ")");
        }

        // First invoke piggybacked methods.
        // If any piggybacked object returns NOT_READY,
        // then we stop there and return NOT_READY.
        // If any returns STOP_ITERATING, then we
        // continue through to the completion of this
        // iteration, but then return STOP_ITERATING.
        boolean stopIterating = false;
        if (_piggybacks != null) {
            // Invoke the iterate() method of each piggyback.
            for (Executable piggyback : _piggybacks) {
                int result = piggyback.iterate(count);
                if (result == NOT_READY) {
                    return NOT_READY;
                } else if (result == STOP_ITERATING) {
                    stopIterating = true;
                }
            }
        }
        if (_derivedPiggybacks != null) {
            // Invoke the iterate() method of each piggyback.
            for (Executable piggyback : _derivedPiggybacks) {
                int result = piggyback.iterate(count);
                if (result == NOT_READY) {
                    return NOT_READY;
                } else if (result == STOP_ITERATING) {
                    stopIterating = true;
                }
            }
        }

        int n = 0;

        while (n++ < count && !_stopRequested) {

            if (_notifyingActorFiring) {
                _actorFiring(FiringEvent.BEFORE_PREFIRE, n);
            }

            if (prefire()) {

                if (_notifyingActorFiring) {
                    _actorFiring(FiringEvent.AFTER_PREFIRE, n);
                    _actorFiring(FiringEvent.BEFORE_FIRE, n);
                }

                fire();

                if (_notifyingActorFiring) {
                    _actorFiring(FiringEvent.AFTER_FIRE, n);
                    _actorFiring(FiringEvent.BEFORE_POSTFIRE, n);
                }

                boolean pfire = postfire();

                if (_notifyingActorFiring) {
                    _actorFiring(FiringEvent.AFTER_POSTFIRE, n);
                }

                if (!pfire) {
                    return Executable.STOP_ITERATING;
                }
            } else {

                if (_notifyingActorFiring) {
                    _actorFiring(FiringEvent.AFTER_PREFIRE, n);
                }
                return Executable.NOT_READY;
            }
        }

        if (_stopRequested || stopIterating) {
            return Executable.STOP_ITERATING;
        } else {
            return Executable.COMPLETED;
        }
    }

    /** Link the subscriberPort with a already registered "published port" coming
     *  from a publisher. The name is the name being used in the
     *  matching process to match publisher and subscriber. A
     *  subscriber interested in the output of this publisher uses
     *  the  name. This registration process of publisher
     *  typically happens before the model is preinitialized,
     *  for example when opening the model. The subscribers
     *  will look for publishers during the preinitialization phase.
     *  @param name The name is being used in the matching process
     *          to match publisher and subscriber.
     *  @param subscriberPort The subscribed port.
     *  @return The publisher port.
     *  @exception NameDuplicationException If there are name conflicts
     *          as a result of the added relations or ports.
     *  @exception IllegalActionException If the published port cannot be found.
     */
    public IOPort linkToPublishedPort(String name, IOPort subscriberPort)
            throws IllegalActionException, NameDuplicationException {
        NamedObj container = getContainer();
        if (!isOpaque() && container instanceof CompositeActor
                && !((CompositeActor) container).isClassDefinition()) {
            // Published ports are not propagated if this actor
            // is opaque.
            return ((CompositeActor) container).linkToPublishedPort(name,
                    subscriberPort);
        } else {
            IOPort publishedPort = getPublishedPort(name);
            IORelation relation = _publisherRelations != null ? _publisherRelations
                    .get(name) : null;
            if (relation == null) {
                try {
                    // CompositeActor always creates an IORelation.
                    relation = (IORelation) newRelation(uniqueName("publisherRelation"));
                } catch (NameDuplicationException e) {
                    // Shouldn't happen.
                    throw new IllegalStateException(e);
                }
                // Prevent the relation and its links from being exported.
                relation.setPersistent(false);
                // Prevent the relation from showing up in vergil.
                new Parameter(relation, "_hide", BooleanToken.TRUE);
                publishedPort.liberalLink(relation);
                if (_publisherRelations == null) {
                    _publisherRelations = new HashMap<String, IORelation>();
                }
                _publisherRelations.put(name, relation);
            }
            if (!subscriberPort.isLinked(relation)) {
                subscriberPort.liberalLink(relation);
                notifyConnectivityChange();

                Director director = getDirector();
                if (director != null) {
                    director.invalidateSchedule();
                    director.invalidateResolvedTypes();
                }
            }
            return publishedPort;
        }
    }

    /** Link the subscriberPort with an already registered "published port" coming
     *  from a publisher. The name gives the channel that
     *  matches the publisher and subscriber.
     *  The publisher is registered before the model is preinitialized,
     *  when its channel parameter is set.
     *  The subscribers call this method
     *  to look for publishers during the preinitialization phase.
     *  @param name The name being used in the matching process
     *   to match publisher and subscriber.
     *  @param subscriberPort The subscriber port.
     *  @param global Specification of whether the data is subscribed
     *   globally.  If set to true, then subscribers will see values
     *   published by publishers anywhere in the model that reference
     *   the same channel by name.  If set to false, then only values
     *   published by publishers that are fired by the same director
     *   are seen by this subscriber.
     *  @return The publisher port.
     *  @exception NameDuplicationException If there are name conflicts
     *   as a result of the added relations or ports.
     *  @exception IllegalActionException If the published port cannot be found.
     */
    public IOPort linkToPublishedPort(String name, IOPort subscriberPort,
            boolean global) throws IllegalActionException,
            NameDuplicationException {
        NamedObj container = getContainer();
        if (!isOpaque() && container instanceof CompositeActor
                && !((CompositeActor) container).isClassDefinition()) {
            // Published ports are not propagated if this actor
            // is opaque.
            return ((CompositeActor) container).linkToPublishedPort(name,
                    subscriberPort, global);
        } else {
            try {
                return linkToPublishedPort(name, subscriberPort);
            } catch (IllegalActionException e) {
                if (!global || !(container instanceof CompositeActor)) {
                    throw e;
                } else {
                    if (_debugging) {
                        _debug("Failed to find publisher for channel \"" + name
                                + "\" in " + getFullName()
                                + ". Trying up the hierarchy.");
                    }

                    String portName = "_subscriber_"
                            + StringUtilities.sanitizeName(name);
                    IOPort port = (IOPort) getPort(portName);
                    if (port == null) {
                        port = (IOPort) newPort(portName);
                        new Parameter(port, "_hide", BooleanToken.TRUE);
                        // Also make sure the port does not show up on the inside.
                        new Parameter(port, "_hideInside", BooleanToken.TRUE);
                        port.setPersistent(false);
                        port.setInput(true);
                        port.setMultiport(true);
                        port.setDefaultWidth(0);

                        IORelation relation = null;
                        //connect the newly created port to the subscriber port
                        try {
                            // CompositeActor always creates an IORelation.
                            relation = (IORelation) newRelation(uniqueName(subscriberPort
                                    .getContainer().getName()
                                    + "subscriberExternalRelationB"));
                        } catch (NameDuplicationException ex) {
                            // Shouldn't happen.
                            throw new IllegalStateException(ex);
                        }
                        // Prevent the relation and its links from being exported.
                        relation.setPersistent(false);
                        // Prevent the relation from showing up in vergil.
                        new Parameter(relation, "_hide", BooleanToken.TRUE);
                        port.liberalLink(relation);

                        if (!subscriberPort.isLinked(relation)) {
                            subscriberPort.liberalLink(relation);
                        }
                        notifyConnectivityChange();
                    }

                    return ((CompositeActor) container).linkToPublishedPort(
                            name, port, global);
                }
            }
        }
    }

    /** Link the subscriberPort with a already registered "published port" coming
     *  from a publisher. The pattern represents the name being used in the
     *  matching process to match publisher and subscriber. A
     *  subscriber interested in the output of this publisher uses
     *  the  name. This registration process of publisher
     *  typically happens before the model is preinitialized,
     *  for example when opening the model. The subscribers
     *  will look for publishers during the preinitialization phase.
     *  @param pattern The pattern is being used in the matching process
     *          to match publisher and subscriber.
     *  @param subscriberPort The subscribed port.
     *  @exception NameDuplicationException If there are name conflicts
     *          as a result of the added relations or ports.
     *  @exception IllegalActionException If the published port cannot be found.
     */
    public void linkToPublishedPort(Pattern pattern, TypedIOPort subscriberPort)
            throws IllegalActionException, NameDuplicationException {
        NamedObj container = getContainer();
        if (!isOpaque() && container instanceof CompositeActor
                && !((CompositeActor) container).isClassDefinition()) {
            // Published ports are not propagated if this actor
            // is opaque.
            ((CompositeActor) container).linkToPublishedPort(pattern,
                    subscriberPort);
        } else {
            if (_publishedPorts != null) {
                boolean matched = false;
                for (String name : _publishedPorts.keySet()) {
                    Matcher matcher = pattern.matcher(name);
                    //System.out.println("Match " + name);
                    if (matcher.matches()) {
                        matched = true;
                        linkToPublishedPort(name, subscriberPort);
                    }
                }
                if (!matched) {
                    throw new IllegalActionException(this,
                            "Failed to find a publisher to match \"" + pattern
                                    + "\"");
                }
            } else {
                throw new IllegalActionException(this,
                        "No Publishers were found adjacent to or " + "below "
                                + subscriberPort.getContainer().getFullName());
            }
        }
    }

    /** Link the subscriberPort with a already registered "published port" coming
     *  from a publisher. The pattern represents the name being used in the
     *  matching process to match publisher and subscriber. A
     *  subscriber interested in the output of this publisher uses
     *  the  name. This registration process of publisher
     *  typically happens before the model is preinitialized,
     *  for example when opening the model. The subscribers
     *  will look for publishers during the preinitialization phase.
     *  @param pattern The pattern is being used in the matching process
     *          to match publisher and subscriber.
     *  @param subscriberPort The subscribed port.
     *  @param global Specification of whether the data is subscribed
     *  globally.  If set to true, then subscribers will see values
     *  published by publishers anywhere in the model that reference
     *  the same channel by name.  If set to false, then only values
     *  published by publishers that are fired by the same director
     *  are seen by this subscriber.
     *  @exception NameDuplicationException If there are name conflicts
     *          as a result of the added relations or ports.
     *  @exception IllegalActionException If the published port cannot be found.
     */
    public void linkToPublishedPort(Pattern pattern,
            TypedIOPort subscriberPort, boolean global)
            throws IllegalActionException, NameDuplicationException {
        NamedObj container = getContainer();
        if (!isOpaque() && container instanceof CompositeActor
                && !((CompositeActor) container).isClassDefinition()) {
            // Published ports are not propagated if this actor
            // is opaque.
            ((CompositeActor) container).linkToPublishedPort(pattern,
                    subscriberPort, global);
        } else {
            if (_publishedPorts == null) {
                if (!global || this == toplevel()) {
                    throw new IllegalActionException(
                            subscriberPort.getContainer(),
                            "No Publishers were found adjacent to or below "
                                    + subscriberPort.getContainer()
                                            .getFullName());
                }
            } else {
                boolean matched = false;
                for (String name : _publishedPorts.keySet()) {
                    Matcher matcher = pattern.matcher(name);
                    //System.out.println("Match " + name);
                    if (matcher.matches()) {
                        matched = true;
                        linkToPublishedPort(name, subscriberPort);
                    }
                }
                if (!matched && (!global || this == toplevel())) {
                    throw new IllegalActionException(
                            subscriberPort.getContainer(),
                            "Failed to find a publisher to match \"" + pattern
                                    + "\"");
                }
            }
            if (global && this != toplevel()) {
                String portName = "_subscriber_"
                        + StringUtilities.sanitizeName(pattern.toString());
                IOPort port = (IOPort) getPort(portName);
                if (port == null) {
                    port = (IOPort) newPort(portName);
                    new Parameter(port, "_hide", BooleanToken.TRUE);
                    // Also make sure the port does not show up on the inside.
                    new Parameter(port, "_hideInside", BooleanToken.TRUE);
                    port.setPersistent(false);
                    port.setInput(true);
                    port.setMultiport(true);
                    port.setDefaultWidth(0);
                }

                if (!subscriberPort.connectedPortList().contains(port)) {
                    IORelation relation = null;
                    //connect the newly created port to the subscriber port
                    try {
                        // CompositeActor always creates an IORelation.
                        relation = (IORelation) newRelation(uniqueName(subscriberPort
                                .getContainer().getName()
                                + "subscriberExternalRelationA"));
                    } catch (NameDuplicationException e) {
                        // Shouldn't happen.
                        throw new IllegalStateException(e);
                    }
                    // Prevent the relation and its links from being exported.
                    relation.setPersistent(false);
                    // Prevent the relation from showing up in vergil.
                    new Parameter(relation, "_hide", BooleanToken.TRUE);

                    port.liberalLink(relation);

                    if (!subscriberPort.isLinked(relation)) {
                        subscriberPort.liberalLink(relation);
                        notifyConnectivityChange();
                    }

                }

                if (container instanceof CompositeActor) {
                    ((CompositeActor) container).linkToPublishedPort(pattern,
                            (TypedIOPort) port, global);
                }
            }
        }
    }

    /**
     *  Return whether the current widths of the relation in the model
     *  are no longer valid anymore and the widths need to be inferred again.
     *  @return True when width inference needs to be executed again.
     *  @exception KernelRuntimeException If toplevel not a CompositeActor.
     */
    public boolean needsWidthInference() throws KernelRuntimeException {
        return _getWidthInferenceAlgorithm().needsWidthInference();
    }

    /** Notify the manager that the connectivity in the model changed
     *  (width of relation changed, relations added, linked to different ports, ...).
     *  This will invalidate the current width inference.
     */
    public void notifyConnectivityChange() {
        try {
            RelationWidthInference widthInferenceAlgorithm = _getWidthInferenceAlgorithm();
            if (widthInferenceAlgorithm != null) {
                widthInferenceAlgorithm.notifyConnectivityChange();
            }
        } catch (KernelRuntimeException ex) {
            // Exception is not relevant when reporting changes.
        }
    }

    /** Return a new receiver of a type compatible with the local director.
     *  Derived classes may further specialize this to return a receiver
     *  specialized to the particular actor.  This method is <i>not</i>
     *  synchronized on the workspace, so the caller should be.
     *
     *  @exception IllegalActionException If there is no local director.
     *  @return A new object implementing the Receiver interface.
     */
    public Receiver newInsideReceiver() throws IllegalActionException {
        if (_director == null) {
            throw new IllegalActionException(this,
                    "Cannot create a receiver without a director.");
        }

        return _director.newReceiver();
    }

    /** Create a new IOPort with the specified name.
     *  The container of the port is set to this actor.
     *  This method is write-synchronized on the workspace.
     *
     *  @param name The name for the new port.
     *  @return The new port.
     *  @exception NameDuplicationException If the actor already has a port
     *   with the specified name.
     */
    @Override
    public Port newPort(String name) throws NameDuplicationException {
        try {
            _workspace.getWriteAccess();

            IOPort port = new IOPort(this, name);
            return port;
        } catch (IllegalActionException ex) {
            // This exception should not occur, so we throw a runtime
            // exception.
            throw new InternalErrorException(this, ex, null);
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Return a new receiver of a type compatible with the executive director.
     *  Derived classes may further specialize this to return a receiver
     *  specialized to the particular actor.  This method is <i>not</i>
     *  synchronized on the workspace, so the caller should be.
     *
     *  @exception IllegalActionException If there is no executive director.
     *  @return A new object implementing the Receiver interface.
     */
    @Override
    public Receiver newReceiver() throws IllegalActionException {
        Director director = getExecutiveDirector();

        if (director == null) {
            throw new IllegalActionException(this,
                    "Cannot create a receiver without an executive director.");
        }

        return director.newReceiver();
    }

    /** Create a new IORelation with the specified name, add it to the
     *  relation list, and return it. Derived classes can override
     *  this to create domain-specific subclasses of IORelation.
     *  This method is write-synchronized on the workspace.
     *
     *  @exception IllegalActionException If name argument is null.
     *  @exception NameDuplicationException If name collides with a name
     *   already on the container's contents list.
     */
    @Override
    public ComponentRelation newRelation(String name)
            throws IllegalActionException, NameDuplicationException {
        try {
            _workspace.getWriteAccess();

            IORelation relation = new IORelation(this, name);
            return relation;
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Return an enumeration of the output ports.
     *  Note that this method returns the ports directly
     *  contained by this actor, whether they are transparent or not.
     *  This method is read-synchronized on the workspace.
     *  @return An enumeration of IOPort objects.
     */
    @Override
    public List outputPortList() {
        try {
            _workspace.getReadAccess();

            if (_outputPortsVersion != _workspace.getVersion()) {
                _cachedOutputPorts = new LinkedList<IOPort>();

                Iterator<?> ports = portList().iterator();

                while (ports.hasNext()) {
                    IOPort p = (IOPort) ports.next();

                    if (p.isOutput()) {
                        _cachedOutputPorts.add(p);
                    }
                }

                _outputPortsVersion = _workspace.getVersion();
            }

            return _cachedOutputPorts;
        } finally {
            _workspace.doneReading();
        }
    }

    /** If this actor is opaque, invoke the postfire() method of its
     *  local director and transfer output data.
     *  Specifically, transfer any data from the output ports of this composite
     *  to the ports connected on the outside. The transfer is accomplished
     *  by calling the transferOutputs() method of the executive director.
     *  If there is no executive director, then no transfer occurs.
     *  This method is read-synchronized on the workspace.
     *
     *  @return True if the execution can continue into the next iteration.
     *  @exception IllegalActionException If there is no director,
     *   or if the director's postfire() method throws it, or if this
     *   actor is not opaque.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        if (_debugging) {
            _debug("Calling postfire()");
        }

        try {
            _workspace.getReadAccess();

            if (!isOpaque()) {
                throw new IllegalActionException(this,
                        "Cannot postfire a non-opaque actor.");
            }

            // First invoke piggybacked methods.
            boolean result = true;
            if (_piggybacks != null) {
                // Invoke the postfire() method of each piggyback.
                for (Executable piggyback : _piggybacks) {
                    result = result && piggyback.postfire();
                }
            }
            if (_derivedPiggybacks != null) {
                // Invoke the postfire() method of each piggyback.
                for (Executable piggyback : _derivedPiggybacks) {
                    result = result && piggyback.postfire();
                }
            }

            // Note that this is assured of firing the local director,
            // not the executive director, because this is opaque.
            result = result && getDirector().postfire();

            if (_debugging) {
                _debug("Postfire returns " + result);
            }

            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    /** If this actor is opaque, invoke the prefire() method of the local
     *  director. This method returns true if the actor is ready to fire
     *  (determined by the prefire() method of the director).
     *  It is read-synchronized on the workspace.
     *
     *  @exception IllegalActionException If there is no director,
     *   or if the director's prefire() method throws it, or if this actor
     *   is not opaque.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        if (_debugging) {
            _debug("CompositeActor: Calling prefire()");
        }

        try {
            _workspace.getReadAccess();

            if (!isOpaque()) {
                throw new IllegalActionException(this,
                        "Cannot invoke prefire on a non-opaque actor, "
                                + " please add a director.");
            }

            // First invoke piggybacked methods.
            // If any piggyback method returns false, then we stop
            // there and return false.
            if (_piggybacks != null) {
                // Invoke the prefire method of each piggyback.
                for (Executable piggyback : _piggybacks) {
                    if (!piggyback.prefire()) {
                        if (_debugging) {
                            _debug("CompositeActor: prefire returns false due to piggybacked object.");
                        }
                        return false;
                    }
                }
            }
            if (_derivedPiggybacks != null) {
                // Invoke the prefire method of each piggyback.
                for (Executable piggyback : _derivedPiggybacks) {
                    if (!piggyback.prefire()) {
                        if (_debugging) {
                            _debug("CompositeActor: prefire returns false due to piggybacked object.");
                        }
                        return false;
                    }
                }
            }

            boolean result = getDirector().prefire();

            if (_debugging) {
                _debug("CompositeActor: prefire returns: " + result);
            }
            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Create receivers and invoke the
     *  preinitialize() method of the local director. If this actor is
     *  not opaque, throw an exception.  This method also resets
     *  the protected variable _stopRequested
     *  to false, so if a derived class overrides this method, then it
     *  should also do that.  This method is
     *  read-synchronized on the workspace, so the preinitialize()
     *  method of the director need not be, assuming it is only called
     *  from here.
     *
     *  @exception IllegalActionException If there is no director, or if
     *   the director's preinitialize() method throws it, or if this actor
     *   is not opaque.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        _stopRequested = false;

        if (_debugging) {
            _debug("Called preinitialize()");
        }

        try {
            _workspace.getReadAccess();

            // First invoke initializable methods.
            if (_initializables != null) {
                for (Initializable initializable : _initializables) {
                    initializable.preinitialize();
                }
            }

            // Next invoke piggybacked methods.
            if (_piggybacks != null) {
                // Invoke the preinitialize() method of each piggyback.
                for (Executable piggyback : _piggybacks) {
                    piggyback.preinitialize();
                }
            }

            // Fill in derived piggy backs
            if (_derivedPiggybacks == null) {
                _derivedPiggybacks = new HashSet<Executable>();
            } else {
                _derivedPiggybacks.clear();
            }
            _getTransparentActorPiggybacks(_derivedPiggybacks, false);
            if (_derivedPiggybacks.isEmpty()) {
                _derivedPiggybacks = null;
            }

            if (_derivedPiggybacks != null) {
                // Invoke the preinitialize() method of each piggyback.
                for (Executable piggyback : _derivedPiggybacks) {
                    piggyback.preinitialize();
                }
            }

            if (!isOpaque()) {
                if (getContainer() == null && deepEntityList().size() == 0) {
                    // If the user runs an empty model, they get
                    // this error message.
                    throw new IllegalActionException(this,
                            "Cannot preinitialize an empty model, "
                                    + "please add actors and a director.");
                }
                throw new IllegalActionException(this,
                        "Cannot preinitialize a non-opaque actor, "
                                + "please add a director.");
            }

            if (_director == null) {
                throw new InternalErrorException(
                        "Actor says it is opaque, but it has no director: "
                                + getFullName());
            }

            // The director creates receivers in its preinitialize(),
            // so it should not be needed to do it here. There was a comment here:
            // "Undid this change temporarily since the move of createReceivers breaks HDF"
            // However, this change does not seem to break HDF actually.
            // However, commenting out this line causes some tests to fail the first
            // time they are run. I'm mystified. EAL 11/16/12
            createReceivers();

            // Note that this is assured of firing the local director,
            // not the executive director, because this is opaque.
            getDirector().preinitialize();

        } finally {
            _workspace.doneReading();
        }
    }

    /** Record a firing event.
     *  @param type The firing event to be recorded.
     */
    @Override
    public void recordFiring(FiringEvent.FiringEventType type) {
        _actorFiring(new FiringEvent(null, this, type));
    }

    /** Register a "published port" coming from a publisher. The name
     *  is the name being used in the
     *  matching process to match publisher and subscriber. A
     *  subscriber interested in the output of this publisher uses
     *  the same name. This registration process of publisher
     *  typically happens before the model is preinitialized,
     *  for example when opening the model. The subscribers
     *  will look for publishers during the preinitialization phase.
     *  @param name The name is being used in the matching process
     *          to match publisher and subscriber.
     *  @param port The published port.
     *  @exception NameDuplicationException If the published port
     *          is already registered.
     *  @exception IllegalActionException If the published port can't
     *          be added.
     */
    public void registerPublisherPort(String name, IOPort port)
            throws NameDuplicationException, IllegalActionException {
        registerPublisherPort(name, port, false);
    }

    /** Register a "published port" coming from a publisher. The name
     *  is the name being used in the
     *  matching process to match publisher and subscriber. A
     *  subscriber interested in the output of this publisher uses
     *  the same name. This registration process of publisher
     *  typically happens before the model is preinitialized,
     *  for example when opening the model. The subscribers
     *  will look for publishers during the preinitialization phase.
     *  This call is ignored if this composite is a class definition
     *  or is within a class definition.
     *
     *  @param name The name is being used in the matching process
     *          to match publisher and subscriber.
     *  @param port The published port.
     *  @param global If true, publish globally. If false, publish
     *   only to subscribers that are fired by the same director.
     *  @exception NameDuplicationException If the published port
     *          is already registered.
     *  @exception IllegalActionException If the published port can't
     *          be added.
     */
    public void registerPublisherPort(String name, IOPort port, boolean global)
            throws NameDuplicationException, IllegalActionException {
        NamedObj container = getContainer();
        if (isWithinClassDefinition()) {
            return;
        }
        // NOTE: The following strategy is fragile in that if
        // a director is added or removed later, then things will break.
        // Hence, HierarchyListeners need to be notified when
        // directors are added or removed.
        if (!isOpaque() && container instanceof CompositeActor) {
            // Published ports are not propagated if this actor
            // is opaque.
            ((CompositeActor) container).registerPublisherPort(name, port,
                    global);
        } else {
            if (_publishedPorts == null) {
                _publishedPorts = new HashMap<String, Set<IOPort>>();
            }
            Set<IOPort> portList = _publishedPorts.get(name);
            if (portList == null) {
                portList = new LinkedHashSet<IOPort>();
                _publishedPorts.put(name, portList);
            }

            if (!portList.contains(port)) {
                portList.add(port);
            }

            if (global && this != toplevel()) {

                // In addition, if the publisher is set to perform an
                // "export" then we should create a new port in this
                // composite and register it with our container, and
                // also link on the inside to the publisher relation
                // corresponding to the port passed in.  Check the
                // container of port argument, which is presumably a
                // Publisher actor, for the value of an "export"
                // parameter. That parameter will have an integer
                // value. If the value is GLOBAL_EXPORT_LEVEL (-1). If
                // it is 0 (the default), then don't do the following.
                // If it is 1, then export only one level up
                // (transparent level?  opaque level?). If it is 2,
                // export two levels up, etc.  FIXME: For now, assume
                // name collisions will not occur.
                String portName = "_publisher_"
                        + StringUtilities.sanitizeName(name);
                IOPort publisherPort = (IOPort) getPort(portName);
                if (publisherPort == null) {
                    publisherPort = (IOPort) newPort(portName);
                    new Parameter(publisherPort, "_hide", BooleanToken.TRUE);
                    // Also make sure the port does not show up on the inside.
                    new Parameter(publisherPort, "_hideInside",
                            BooleanToken.TRUE);
                    publisherPort.setPersistent(false);
                    publisherPort.setOutput(true);
                    publisherPort.setMultiport(true);
                }

                // FIXME: Hide the port. Note that we need to fix vergil
                // so that when it lays out port, hidden ports do not take up
                // space on the icon.

                // NOTE: The following will result in an _inside_ link to the port.
                linkToPublishedPort(name, publisherPort);

                if (container instanceof CompositeActor) {
                    ((CompositeActor) container).registerPublisherPort(name,
                            publisherPort, global);
                }
            }
        }
    }

    /** Unregister an actor firing listener.  If the specified listener has not
     *  been previously registered, then do nothing.  Note that this method
     *  is basically the same as removeDebugListener in the class NamedObj.
     *  @param listener The listener to remove from the list of listeners
     *   to which actor firing messages are sent.
     *  @see #addActorFiringListener(ActorFiringListener)
     */
    @Override
    public void removeActorFiringListener(ActorFiringListener listener) {
        if (_actorFiringListeners == null) {
            return;
        }

        // NOTE: This has to be synchronized to prevent
        // concurrent modification exceptions.
        synchronized (_actorFiringListeners) {
            _actorFiringListeners.remove(listener);

            if (_actorFiringListeners.isEmpty()) {
                _notifyingActorFiring = false;
            }

            return;
        }
    }

    /** Remove the specified object from the list of objects whose
     *  preinitialize(), initialize(), and wrapup()
     *  methods should be invoked upon invocation of the corresponding
     *  methods of this object. If the specified object is not
     *  on the list, do nothing.
     *  @param initializable The object whose methods should no longer be invoked.
     *  @see #addInitializable(Initializable)
     *  @see #removePiggyback(Executable)
     */
    @Override
    public void removeInitializable(Initializable initializable) {
        if (_initializables != null) {
            _initializables.remove(initializable);
            if (_initializables.size() == 0) {
                _initializables = null;
            }
        }
    }

    /** Remove the specified object from the list of objects whose action
     *  methods should be invoked upon invocation of the corresponding
     *  actions methods of this object. If the specified object is not
     *  on the list, do nothing.
     *  @param piggyback The piggyback object.
     *  @see #removeInitializable(Initializable)
     *  @see #addPiggyback(Executable)
     */
    public void removePiggyback(Executable piggyback) {
        if (_piggybacks != null) {
            _piggybacks.remove(piggyback);
            if (_piggybacks.size() == 0) {
                _piggybacks = null;
            }
        }
    }

    /** Queue a change request.  If there is a manager, then first call
     *  stopFire() before deferring to the base class.
     *  @param change The requested change.
     */
    @Override
    public void requestChange(ChangeRequest change) {
        super.requestChange(change);
        // stopFire() should be called after the change has been requested
        // to ensure that if it is being requested in a separate thread
        // from the director thread that it will be present in the change
        // queue when the director gets around to handling it. The call
        // to stopFire() below may wake up the director to handle it.
        // The call of stopFire() only needs to happen in case the change
        // represents a structural change. Not if we request a change to
        // refresh the GUI.
        if (getManager() != null && change.isStructuralChange()) {
            stopFire();
        }
    }

    /** Specify whether this object is a class definition.
     *  If the argument is true and this entity is not a class
     *  definition, then the cache of published and subscribed ports
     *  is cleared and the superclass called.
     *  @param isClass True to make this object a class definition.
     *  @exception IllegalActionException If the argument is true and
     *   this entity contains ports with links.
     */
    //     public void setClassDefinition(boolean isClass)
    //             throws IllegalActionException {
    //         if (isClass && !isClassDefinition()) {
    //             _publishedPorts = null;
    //             _subscribedPorts = null;
    //             _publisherRelations = null;
    //             NamedObj immediateContainer = getContainer();
    //             if (immediateContainer != null
    //                     && immediateContainer instanceof CompositeActor) {
    //                 CompositeActor container = (CompositeActor)immediateContainer;
    //                 List<Port> portList = portList();
    //                 for (Port port: portList) {
    //                     if (port instanceof PublisherPort) {
    //                         PublisherPort publisherPort = (PublisherPort)port;
    //                         try {
    //                             container.unregisterPublisherPort(publisherPort.channel.stringValue(), publisherPort, ((BooleanToken) publisherPort.global.getToken())
    //                                 .booleanValue());
    //                         } catch (NameDuplicationException ex) {
    //                             throw new IllegalActionException(this, ex, "Could not unregister " + publisherPort);
    //                         }
    //                     }
    //                 }
    //             }
    //         } else if (!isClass && isClassDefinition()) {
    //             if (_publishedPorts != null
    //                     || _subscribedPorts != null
    //                     || _publisherRelations != null) {
    //                 System.out.println("FIXME: conversion from a class definition to an instance causes problems with Publishers and Subscribers.");
    //             }
    //         }
    //         super.setClassDefinition(isClass);
    //     }

    /** Override the base class to invalidate the schedule and
     *  resolved types of the director.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the action would result in a
     *   recursive containment structure, or if
     *   this entity and container are not in the same workspace.
     *  @exception NameDuplicationException If the container already has
     *   an entity with the name of this entity.
     */
    @Override
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        // Invalidate the schedule and type resolution of the old director.
        Director oldDirector = getDirector();

        if (oldDirector != null) {
            oldDirector.invalidateSchedule();
            oldDirector.invalidateResolvedTypes();
        }

        if (!(container instanceof CompositeActor)) {
            _relationWidthInference = new RelationWidthInference(this);
        } else {
            _relationWidthInference = null;
        }

        super.setContainer(container);

        Director director = getDirector();

        // Invalidate the schedule and type resolution of the new director.
        if (director != null) {
            director.invalidateSchedule();
            director.invalidateResolvedTypes();
        }
    }

    /** Set the local director for execution of this CompositeActor.
     *  Calling this method with a non-null argument makes this entity opaque.
     *  Calling it with a null argument makes it transparent.
     *  The container of the specified director is set to this composite
     *  actor, and if there was previously a local director, its container
     *  is set to null. This method is write-synchronized on the workspace.
     *  NOTE: Calling this method is almost equivalent to calling setContainer()
     *  on the director with this composite as an argument. The difference
     *  is that if you call this method with a null argument, it effectively
     *  removes the director from its role as director, but without
     *  removing it from its container.
     *
     *  @param director The Director responsible for execution.
     *  @exception IllegalActionException If the director is not in
     *  the same workspace as this actor. It may also be thrown in derived
     *  classes if the director is not compatible.
     *  @exception NameDuplicationException If an attribute already exists
     *  in this container with the same name as the given director.
     *  @see #getDirector()
     */
    public void setDirector(Director director) throws IllegalActionException,
            NameDuplicationException {
        if (director != null) {
            director.setContainer(this);
            // No need to call _setDirector in here since the
            // director will do this directly.
        } else {
            _setDirector(null);
        }
    }

    /** Set the Manager for execution of this CompositeActor.
     *  This can only be done for a composite actor that has no container.
     *  For others, the Manager is inherited from the container.
     *  This method is write-synchronized on the workspace.
     *
     *  @param manager The Manager
     *  @exception IllegalActionException If this actor has a
     *  container, or the manager is not in the same workspace as this
     *  actor.
     *  @see #getManager()
     */
    public void setManager(Manager manager) throws IllegalActionException {
        if (manager != null && _workspace != manager.workspace()) {
            throw new IllegalActionException(this, manager,
                    "Cannot set manager because workspaces are different."
                            + "manager: " + manager.workspace() + ", "
                            + _workspace);
        }

        try {
            _workspace.getWriteAccess();

            if (getContainer() != null && manager != null) {
                throw new IllegalActionException(this, manager,
                        "Cannot set the Manager of an actor "
                                + "with a container.");
            }

            // If there was a previous manager, we need to reset it.
            if (_manager != null) {
                // Remove this from the list of debug listeners of the old
                // manager.
                _manager.removeDebugListener(this);

                // Notify the old manager that it is no longer the manager
                // of anything.
                _manager._makeManagerOf(null);
            }

            if (manager != null) {
                // Add this to the list of debug listeners of the new manager.
                // This composite actor will relay debug messages from
                // the manager.
                manager.addDebugListener(this);
                manager._makeManagerOf(this);
            }

            _manager = manager;
            return;
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Request that execution stop as soon as possible.
     *  This sets a flag indicating that this request has been made
     *  (the protected variable _stopRequested).
     *  If this actor is opaque, then invoke the
     *  stop() method of the local director.
     *  This method is read-synchronized on the workspace.
     */
    @Override
    public void stop() {
        if (_debugging) {
            _debug("Called stop()");
        }

        try {
            _workspace.getReadAccess();

            // First invoke piggybacked methods.
            if (_piggybacks != null) {
                // Invoke the stop method of each piggyback.
                for (Executable piggyback : _piggybacks) {
                    piggyback.stop();
                }
            }
            if (_derivedPiggybacks != null) {
                // Invoke the stop method of each piggyback.
                for (Executable piggyback : _derivedPiggybacks) {
                    piggyback.stop();
                }
            }

            _stopRequested = true;

            if (!isOpaque()) {
                return;
            }

            Director director = getDirector();
            Director executiveDirector = getExecutiveDirector();

            // Call stop() on the director. Be sure the
            // director is an internal director, or else an infinite
            // loop will result!
            if (director != null && director != executiveDirector) {
                director.stop();
            }
        } finally {
            _workspace.doneReading();
        }
    }

    /** Request that execution of the current iteration complete.
     *  If this actor is opaque, then invoke the stopFire()
     *  method of the local director, if there is one.
     *  Otherwise, do nothing.
     *  This method is read-synchronized on the workspace.
     */
    @Override
    public void stopFire() {
        if (_debugging) {
            _debug("Called stopFire()");
        }

        try {
            _workspace.getReadAccess();

            // First invoke piggybacked methods.
            if (_piggybacks != null) {
                // Invoke the stopFire() method of each piggyback.
                for (Executable piggyback : _piggybacks) {
                    piggyback.stopFire();
                }
            }
            if (_derivedPiggybacks != null) {
                // Invoke the stopFire() method of each piggyback.
                for (Executable piggyback : _derivedPiggybacks) {
                    piggyback.stopFire();
                }
            }

            if (!isOpaque()) {
                return;
            }

            Director director = getDirector();
            Director executiveDirector = getExecutiveDirector();

            // Call stopFire() on the director. Be sure the
            // director is an internal director, or else an infinite
            // loop will result!
            if (director != null && director != executiveDirector) {
                director.stopFire();
            }
        } finally {
            _workspace.doneReading();
        }
    }

    /** If this is an opaque CompositeActor, then look to our director
     *  for help.   If we are transparent, then we really shouldn't have been
     *  called, so just ignore.
     */
    @Override
    public void terminate() {
        if (_debugging) {
            _debug("Called terminate()");
        }

        // First invoke piggybacked methods.
        if (_piggybacks != null) {
            // Invoke the terminate() method of each piggyback.
            for (Executable piggyback : _piggybacks) {
                piggyback.terminate();
            }
        }
        if (_derivedPiggybacks != null) {
            // Invoke the terminate() method of each piggyback.
            for (Executable piggyback : _derivedPiggybacks) {
                piggyback.terminate();
            }
        }

        if (!isOpaque()) {
            return;
        }

        getDirector().terminate();
    }

    /** Unlink the subscriberPort with a already registered "published port" coming
     *  from a publisher. The name is the name being used in the
     *  matching process to match publisher and subscriber. A
     *  subscriber interested in the output of this publisher uses
     *  the  name. This registration process of publisher
     *  typically happens before the model is preinitialized,
     *  for example when opening the model. The subscribers
     *  will look for publishers during the preinitialization phase.
     *  @param name The name is being used in the matching process
     *          to match publisher and subscriber.
     *  @param subscriberPort The subscribed port.
     *  @exception IllegalActionException If the published port cannot be found.
     */
    public void unlinkToPublishedPort(String name, IOPort subscriberPort)
            throws IllegalActionException {
        NamedObj container = getContainer();
        if (!isOpaque() && container instanceof CompositeActor
                && !((CompositeActor) container).isWithinClassDefinition()) {
            // Published ports are not propagated if this actor
            // is opaque.
            ((CompositeActor) container).unlinkToPublishedPort(name,
                    subscriberPort);
        } else {
            // Remove the link to a previous relation, if necessary.

            IORelation relation = _publisherRelations != null ? _publisherRelations
                    .get(name) : null;

            if (relation != null) {
                subscriberPort.unlink(relation);
                notifyConnectivityChange();
            }

            Director director = getDirector();
            if (director != null) {
                director.invalidateSchedule();
                director.invalidateResolvedTypes();
            }
        }
    }

    /** Unlink the subscriberPort with a already registered "published port" coming
     *  from a publisher. The name is the name being used in the
     *  matching process to match publisher and subscriber. A
     *  subscriber interested in the output of this publisher uses
     *  the  name. This registration process of publisher
     *  typically happens before the model is preinitialized,
     *  for example when opening the model. The subscribers
     *  will look for publishers during the preinitialization phase.
     *  @param name The name is being used in the matching process
     *          to match publisher and subscriber.
     *  @param subscriberPort The subscribed port.
     *  @param global Specification of whether the data is subscribed
     *  globally.  If set to true, then subscribers will see values
     *  published by publishers anywhere in the model that reference
     *  the same channel by name.  If set to false, then only values
     *  published by publishers that are fired by the same director
     *  are seen by this subscriber.
     *  @exception IllegalActionException If the published port cannot be found.
     */
    public void unlinkToPublishedPort(String name, IOPort subscriberPort,
            boolean global) throws IllegalActionException {
        NamedObj container = getContainer();
        if (!isOpaque() && container instanceof CompositeActor
                && !((CompositeActor) container).isWithinClassDefinition()) {
            // Published ports are not propagated if this actor
            // is opaque.
            ((CompositeActor) container).unlinkToPublishedPort(name,
                    subscriberPort, global);
        } else {
            // Remove the link to a previous relation, if necessary.

            IORelation relation = _publisherRelations != null ? _publisherRelations
                    .get(name) : null;

            if (relation != null) {
                subscriberPort.unlink(relation);
                notifyConnectivityChange();
            }

            Director director = getDirector();
            if (director != null) {
                director.invalidateSchedule();
                director.invalidateResolvedTypes();
            }

            if (global && container instanceof CompositeActor) {
                for (Object relationObj : subscriberPort.linkedRelationList()) {
                    try {
                        for (Object port : ((IORelation) relationObj)
                                .linkedPortList(subscriberPort)) {

                            IOPort subscribedPort = (IOPort) port;
                            if (subscribedPort.isInput()) {
                                ((CompositeActor) container)
                                        .unlinkToPublishedPort(name,
                                                subscribedPort, global);

                                subscribedPort.setContainer(null);

                            }
                        }
                        ((IORelation) relationObj).setContainer(null);
                        notifyConnectivityChange();
                    } catch (NameDuplicationException ex) {
                        throw new InternalErrorException(
                                subscriberPort.getContainer(), ex,
                                "Failed to set the container to null?");
                    }
                }
            }
        }
    }

    /** Unlink the subscriberPort with a already registered "published port" coming
     *  from a publisher. The pattern is the pattern being used in the
     *  matching process to match publisher and subscriber. A
     *  subscriber interested in the output of this publisher uses
     *  the  name. This registration process of publisher
     *  typically happens before the model is preinitialized,
     *  for example when opening the model. The subscribers
     *  will look for publishers during the preinitialization phase.
     *  @param pattern The pattern is being used in the matching process
     *          to match publisher and subscriber.
     *  @param subscriberPort The subscribed port.
     *  @exception IllegalActionException If the published port cannot be found.
     */
    public void unlinkToPublishedPort(Pattern pattern,
            TypedIOPort subscriberPort) throws IllegalActionException {
        NamedObj container = getContainer();
        if (!isOpaque() && container instanceof CompositeActor
                && !((CompositeActor) container).isWithinClassDefinition()) {
            // Published ports are not propagated if this actor
            // is opaque.
            ((CompositeActor) container).unlinkToPublishedPort(pattern,
                    subscriberPort);
        } else {
            if (_publishedPorts != null) {
                for (String name : _publishedPorts.keySet()) {
                    Matcher matcher = pattern.matcher(name);
                    if (matcher.matches()) {
                        unlinkToPublishedPort(name, subscriberPort);
                    }
                }
            }

        }
    }

    /** Unlink the subscriberPort with a already registered "published port" coming
     *  from a publisher. The pattern is the pattern being used in the
     *  matching process to match publisher and subscriber. A
     *  subscriber interested in the output of this publisher uses
     *  the  name. This registration process of publisher
     *  typically happens before the model is preinitialized,
     *  for example when opening the model. The subscribers
     *  will look for publishers during the preinitialization phase.
     *  @param pattern The pattern is being used in the matching process
     *          to match publisher and subscriber.
     *  @param subscriberPort The subscribed port.
     *  @param global Specification of whether the data is subscribed
     *  globally.  If set to true, then subscribers will see values
     *  published by publishers anywhere in the model that reference
     *  the same channel by name.  If set to false, then only values
     *  published by publishers that are fired by the same director
     *  are seen by this subscriber.
     *  @exception IllegalActionException If the published port cannot be found.
     */
    public void unlinkToPublishedPort(Pattern pattern,
            TypedIOPort subscriberPort, boolean global)
            throws IllegalActionException {
        NamedObj container = getContainer();
        if (!isOpaque() && container instanceof CompositeActor
                && !((CompositeActor) container).isWithinClassDefinition()) {
            // Published ports are not propagated if this actor
            // is opaque.
            ((CompositeActor) container).unlinkToPublishedPort(pattern,
                    subscriberPort, global);
        } else {

            if (_publishedPorts != null) {
                for (String name : _publishedPorts.keySet()) {
                    Matcher matcher = pattern.matcher(name);
                    if (matcher.matches()) {
                        unlinkToPublishedPort(name, subscriberPort);
                    }
                }
            }

            for (Object relationObj : subscriberPort.linkedRelationList()) {
                try {
                    for (Object port : ((IORelation) relationObj)
                            .linkedPortList(subscriberPort)) {

                        IOPort subscribedPort = (IOPort) port;
                        if (subscribedPort.isInput()) {
                            //if the subscribed port does not connect to any inside port, then unlink the port
                            Set connectedInsidePort = new HashSet(
                                    subscribedPort.insidePortList());
                            connectedInsidePort.remove(subscriberPort);
                            if (connectedInsidePort.size() == 0) {
                                ((CompositeActor) container)
                                        .unlinkToPublishedPort(pattern,
                                                (TypedIOPort) subscribedPort,
                                                global);

                                subscribedPort.setContainer(null);

                            }

                        }
                    }
                    ((IORelation) relationObj).setContainer(null);
                } catch (NameDuplicationException ex) {
                    throw new InternalErrorException(
                            subscriberPort.getContainer(), ex,
                            "Failed to set the container to null?");
                }
            }
        }
    }

    /** Unregister a "published port" coming
     *  from a publisher. The name is the name being used in the
     *  matching process to match publisher and subscriber. A
     *  subscriber interested in the output of this publisher uses
     *  the same name. This registration process of publisher
     *  typically happens before the model is preinitialized,
     *  for example when opening the model. The subscribers
     *  will look for publishers during the preinitialization phase.
     *  @param name The name is being used in the matching process
     *          to match publisher and subscriber. This will be the port
     *          that should be removed
     *  @param publisherPort The publisher port.
     *  @exception IllegalActionException If the container of the port cannot
     *  be set.
     *  @exception NameDuplicationException If the container of the port cannot
     *  be set
     */
    public void unregisterPublisherPort(String name, IOPort publisherPort)
            throws IllegalActionException, NameDuplicationException {
        unregisterPublisherPort(name, publisherPort, false);
    }

    /** Unregister a "published port" coming
     *  from a publisher. The name is the name being used in the
     *  matching process to match publisher and subscriber. A
     *  subscriber interested in the output of this publisher uses
     *  the same name. This registration process of publisher
     *  typically happens before the model is preinitialized,
     *  for example when opening the model. The subscribers
     *  will look for publishers during the preinitialization phase.
     *  @param name The name is being used in the matching process
     *          to match publisher and subscriber. This will be the port
     *          that should be removed
     *  @param publisherPort The publisher port.
     *  @param global If true, unregister the port all the way up the hierarchy.
     *  @exception IllegalActionException If the container of the port cannot
     *  be set.
     *  @exception NameDuplicationException If the container of the port cannot
     *  be set
     */
    public void unregisterPublisherPort(String name, IOPort publisherPort,
            boolean global) throws IllegalActionException,
            NameDuplicationException {
        NamedObj container = getContainer();
        if (!isOpaque() && container instanceof CompositeActor
                && !((CompositeActor) container).isWithinClassDefinition()) {
            // Published ports are not propagated if this actor
            // is opaque.
            ((CompositeActor) container).unregisterPublisherPort(name,
                    publisherPort);
        } else {
            if (_publishedPorts != null) {
                Set<IOPort> ports = _publishedPorts.get(name);
                // if (ports == null) {
                //     // If we are changing the name of a Publisher channel in an
                //     // opaque, then ports might be null.
                //      throw new InternalErrorException("While trying to unregister "
                //              + "port \"" + name + "\" from port \""
                //              + publisherPort.getFullName()
                //              + "\", the port was not found in the map of published "
                //              + "ports accessible in the container of the port.");
                // }
                if (ports != null) {
                    // If we are changing the name of a Publisher channel in an
                    // opaque, then ports might be null.
                    ports.remove(publisherPort);
                    if (ports.isEmpty()) {
                        _publishedPorts.remove(name);
                    }
                }
            }

            if (_publisherRelations != null) {
                IORelation relation = _publisherRelations.get(name);
                if (relation != null) {
                    if (global && container instanceof CompositeActor) {
                        for (Object port : relation
                                .linkedPortList(publisherPort)) {
                            IOPort publishedPort = (IOPort) port;
                            if (publishedPort.isOutput()) {
                                ((CompositeActor) container)
                                        .unregisterPublisherPort(name,
                                                publishedPort, global);
                                publishedPort.setContainer(null);
                            }
                        }
                    }

                    relation.setContainer(null);

                    _publisherRelations.remove(name);
                }
            }

        }
    }

    /** If this actor is opaque, then invoke the wrapup() method of the local
     *  director. This method is read-synchronized on the workspace.
     *
     *  @exception IllegalActionException If there is no director,
     *   or if the director's wrapup() method throws it, or if this
     *   actor is not opaque.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        if (_debugging) {
            _debug("Called wrapup()");
        }

        try {
            _workspace.getReadAccess();

            // First invoke initializable methods.
            if (_initializables != null) {
                for (Initializable initializable : _initializables) {
                    initializable.wrapup();
                }
            }

            // Next invoke piggybacked methods.
            if (_piggybacks != null) {
                // Invoke the wrapup() method of each piggyback.
                for (Executable piggyback : _piggybacks) {
                    piggyback.wrapup();
                }
            }
            if (_derivedPiggybacks != null) {
                // Invoke the wrapup() method of each piggyback.
                for (Executable piggyback : _derivedPiggybacks) {
                    piggyback.wrapup();
                }
            }

            if (!isOpaque()) {
                // Don't throw an exception here, calling wrapup() on
                // a composite that is not opaque is not always an error.
                // Generating WebStart for ptolemy/demo/ElectricPowerSystem/ElectricPowerSystem.xml
                // One possibility is to add the DoNothingDirector.
                System.out.println("Warning: CompositeActor.wrapup() was called on "
                        + getFullName() + ", which is not opaque (it does not have a director?).");
            }

            // Note that this is assured of firing the local director,
            // not the executive director, because this is opaque.
            // However, there may not be a director (e.g. DifferentialSystem
            // actor in CT).
            Director director = getDirector();

            if (director != null) {
                director.wrapup();
            }
        } finally {
            _workspace.doneReading();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Send an actor firing event to all actor firing listeners that
     *  have registered with this actor.
     *  @param event The event.
     */
    protected final void _actorFiring(FiringEvent event) {
        if (_notifyingActorFiring) {
            for (ActorFiringListener listener : _actorFiringListeners) {
                listener.firingEvent(event);
            }
        }
    }

    /** Send an actor firing event type to all actor firing listeners that
     *  have registered with this actor.
     *  @param type The type.
     *  @param multiplicity The multiplicity of the firing, that is,
     *  the number of times the firing will occur or has occurred.
     */
    protected final void _actorFiring(FiringEvent.FiringEventType type,
            int multiplicity) {
        _actorFiring(new FiringEvent(null, this, type, multiplicity));
    }

    /** Add an actor to this container with minimal error checking.
     *  This overrides the base-class method to make sure the argument
     *  implements the Actor interface.
     *  It is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @param entity Actor to contain.
     *  @exception IllegalActionException If the actor has no name, or the
     *   action would result in a recursive containment structure, or the
     *   argument does not implement the Actor interface.
     *  @exception NameDuplicationException If the name collides with a name
     *   already on the actor contents list.
     */
    @Override
    protected void _addEntity(ComponentEntity entity)
            throws IllegalActionException, NameDuplicationException {
        if (!(entity instanceof Actor)) {
            throw new IllegalActionException(this, entity,
                    "CompositeActor can only contain entities that "
                            + " implement the Actor interface.");
        }

        super._addEntity(entity);
    }

    /** Add a port to this actor. This overrides the base class to
     *  throw an exception if the added port is not an instance of
     *  IOPort.  This method should not be used directly.  Call the
     *  setContainer() method of the port instead. This method does not set
     *  the container of the port to point to this actor.
     *  It assumes that the port is in the same workspace as this
     *  actor, but does not check.  The caller should check.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @param port The port to add to this actor.
     *  @exception IllegalActionException If the port class is not
     *   acceptable to this actor, or the port has no name.
     *  @exception NameDuplicationException If the port name collides with a
     *   name already in the actor.
     */
    @Override
    protected void _addPort(Port port) throws IllegalActionException,
            NameDuplicationException {
        if (!(port instanceof IOPort)) {
            throw new IllegalActionException(this, port,
                    "CompositeActor can only contain instances of IOPort.");
        }

        super._addPort(port);
    }

    /** Add a relation to this container. This method should not be used
     *  directly.  Call the setContainer() method of the relation instead.
     *  This method does not set the container of the relation to refer
     *  to this container. This method is <i>not</i> synchronized on the
     *  workspace, so the caller should be.
     *
     *  @param relation Relation to contain.
     *  @exception IllegalActionException If the relation has no name, or is
     *   not an instance of IORelation.
     *  @exception NameDuplicationException If the name collides with a name
     *   already on the contained relations list.
     */
    @Override
    protected void _addRelation(ComponentRelation relation)
            throws IllegalActionException, NameDuplicationException {
        if (!(relation instanceof IORelation)) {
            throw new IllegalActionException(this, relation,
                    "CompositeActor can only contain instances of IORelation.");
        }

        super._addRelation(relation);
    }

    /** Notify this actor that the given entity has been added inside it.
     *  This overrides the base-class method to invalidate the schedule
     *  and type resolution, and to request initialization with the director.
     *  This method does not alter the actor in any way.
     *  It is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @param entity Actor to contain.
     */
    @Override
    protected void _finishedAddEntity(ComponentEntity entity) {
        Director director = getDirector();

        if (director != null) {
            director.invalidateSchedule();
            director.invalidateResolvedTypes();
            director.requestInitialization((Actor) entity);
        }
    }

    /** Set the local director for execution of this CompositeActor.
     *  This should not be called be directly.  Instead, call setContainer()
     *  on the director.  This method removes any previous director
     *  from this container, and caches a local reference to the director
     *  so that this composite does not need to search its attributes each
     *  time the director is accessed.
     *  @param director The Director responsible for execution.
     *  @exception IllegalActionException If removing the old director
     *   causes this to be thrown. Should not be thrown.
     *  @exception NameDuplicationException If removing the old director
     *   causes this to be thrown. Should not be thrown.
     */
    protected void _setDirector(Director director)
            throws IllegalActionException, NameDuplicationException {
        Director oldDirector = getDirector();

        if (director != oldDirector) {
            if (oldDirector != null) {
                oldDirector.invalidateSchedule();
                oldDirector.invalidateResolvedTypes();
            }
        }
        // Do not use getDirector() to get the old director because
        // that will look up the hierarchy if there is no director.
        if (director != _director) {
            // If we are changing from opaque to transparent or
            // vice versa, then we need to notify of a hierarchy change.
            if (director == null || oldDirector == null) {
                _notifyHierarchyListenersBeforeChange();
            }
        }
        Director previousLocalDirector = _director;
        try {
            if (director != oldDirector) {
                _director = director;

                if (director != null) {
                    director.invalidateSchedule();
                    director.invalidateResolvedTypes();
                } else {
                    // When deleting, the executive director also needs to be
                    // notified that its schedule must be recomputed.
                    Director executiveDirector = getExecutiveDirector();

                    if (executiveDirector != null) {
                        executiveDirector.invalidateSchedule();
                    }
                }
            }
        } finally {
            // If we are changing from opaque to transparent or
            // vice versa, then we need to notify of a hierarchy change.
            if (director == null || previousLocalDirector == null) {
                _notifyHierarchyListenersAfterChange();
            }
        }
    }

    /** Read inputs from ParameterPorts and update.
     *  @exception IllegalActionException If reading from parameter associated
     *  with port fails.
     */
    protected void _transferPortParameterInputs() throws IllegalActionException {
        // Need to read from port parameters
        // first because in some domains (e.g. SDF)
        // the behavior of the schedule might depend on rate variables
        // set from ParameterPorts.
        for (Iterator<?> inputPorts = inputPortList().iterator(); inputPorts
                .hasNext() && !_stopRequested;) {
            IOPort p = (IOPort) inputPorts.next();

            if (p instanceof ParameterPort) {
                ((ParameterPort) p).getParameter().update();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Fill in the piggybacks in the contained transparent CompositeActors.
     *  If addPiggyBackAtThisLevel equals to True, the piggybacks directly in
     *  this composite actor will also be included.
     *  @param piggybacks The piggybacks that will be filled it.
     *  @param addPiggyBackAtThisLevel True when the piggybacks directly in this composite
     *          actor should also be included.
     */
    private void _getTransparentActorPiggybacks(Set<Executable> piggybacks,
            boolean addPiggyBackAtThisLevel) {
        assert piggybacks != null;
        if (addPiggyBackAtThisLevel && _piggybacks != null) {
            piggybacks.addAll(_piggybacks);
        }
        for (CompositeActor actor : entityList(CompositeActor.class)) {
            if (!actor.isOpaque()) {
                actor._getTransparentActorPiggybacks(piggybacks, true);
            }
        }
    }

    /** Return the RelationWidthInference algorithm.
     *  _relationWidthInference is only stored at the top CompositeActor
     *  for the complete model.
     *  @return The RelationWidthInference algorithm.
     *  @exception KernelRuntimeException If toplevel not a CompositeActor.
     */
    private RelationWidthInference _getWidthInferenceAlgorithm() {
        NamedObj container = getContainer();
        if (container instanceof CompositeActor) {
            return ((CompositeActor) container)._getWidthInferenceAlgorithm();
        } else {
            // assert _relationWidthInference != null;
            // Removed the assert. When your cloning this object it is possible
            //  that _relationWidthInference hasn't been filled in yet.
            // If _relationWidthInference is null, then running newly created models
            // will fail.  See 7.1.5 in CompositeActor.tcl
            return _relationWidthInference;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The list of ActorFiringListeners registered with this object.
     *  NOTE: Because of the way we synchronize on this object, it should
     *  never be reset to null after the first list is created.
     */
    protected LinkedList<ActorFiringListener> _actorFiringListeners = null;

    /** The causality interface, if it has been created. */
    protected CausalityInterface _causalityInterface;

    /** The derived piggybacked executables. Derived piggybacked executables
     * are executables that are added to transparent composite actors that are
     * contained by this composite actor. These should also piggy back on this actor.
     * These are only filled in if this actor is a opaque composite actor.*/
    protected transient Set<Executable> _derivedPiggybacks;

    /** List of objects whose (pre)initialize() and wrapup() methods
     *  should be slaved to these.
     */
    protected transient Set<Initializable> _initializables;

    /** Flag that is true if there are actor firing listeners. */
    protected boolean _notifyingActorFiring = false;

    /** List piggybacked objects. */
    protected transient Set<Executable> _piggybacks;

    /** Keep track of all published ports accessible in this container.*/
    protected Map<String, Set<IOPort>> _publishedPorts;

    /** Keep track of all published ports accessible in this container.*/
    protected Map<String, List<IOPort>> _subscribedPorts;

    /** Keep track of all relations with published ports accessible in this container.*/
    protected Map<String, IORelation> _publisherRelations;

    /** Indicator that a stop has been requested by a call to stop(). */
    protected boolean _stopRequested = false;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The director for this composite actor.
    private Director _director;

    // Indicator that we are in the connectionsChanged method.
    private boolean _inConnectionsChanged = false;

    // The manager for this composite actor.
    private Manager _manager;

    // Cached lists of input and output ports.
    private transient long _inputPortsVersion = -1;

    private transient List<IOPort> _cachedInputPorts;

    private transient long _outputPortsVersion = -1;

    private transient List<IOPort> _cachedOutputPorts;

    /** The director for which the causality interface was created. */
    private Director _causalityInterfaceDirector;

    /** Record of the workspace version the last time receivers were created. */
    private long _receiversVersion = -1;

    /* A helper class that does the width inference.
     * _relationWidthInference is only stored at the top CompositeActor
     * for the complete model.
     */
    private RelationWidthInference _relationWidthInference;

}
