/* An executable entity.

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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ptolemy.actor.util.BooleanDependency;
import ptolemy.actor.util.CausalityInterface;
import ptolemy.actor.util.DefaultCausalityInterface;
import ptolemy.actor.util.Dependency;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// AtomicActor

/**
 An AtomicActor is an executable entity that cannot itself contain
 other actors. The Ports of AtomicActors are constrained to be IOPorts.
 Derived classes may further constrain the ports by overriding the public
 method newPort() to create a port of the appropriate subclass, and the
 protected method _addPort() to throw an exception if its argument is a
 port that is not of the appropriate subclass. In this base class, the
 actor does nothing in the action methods (prefire, fire, ...).

 @author Mudit Goel, Edward A. Lee, Lukito Muliadi, Steve Neuendorffer, Contributor: Daniel Crawl
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Green (neuendor)
 @see ptolemy.actor.CompositeActor
 @see ptolemy.actor.IOPort
 */
public class AtomicActor<T extends IOPort> extends ComponentEntity<T> implements
Actor, FiringsRecordable {
    /** Construct an actor in the default workspace with an empty string
     *  as its name. Increment the version number of the workspace.
     *  The object is added to the workspace directory.
     */
    public AtomicActor() {
        super();
    }

    /** Construct an actor in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list this actor.
     */
    public AtomicActor(Workspace workspace) {
        super(workspace);
    }

    /** Create a new actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container (see the setContainer() method).
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public AtomicActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add the specified object to the set of objects whose
     *  preinitialize(), initialize(), and wrapup()
     *  methods should be invoked upon invocation of the corresponding
     *  methods of this object.
     *  @param initializable The object whose methods should be invoked.
     *  @see #removeInitializable(Initializable)
     *  @see ptolemy.actor.CompositeActor#addPiggyback(Executable)
     */
    @Override
    public void addInitializable(Initializable initializable) {
        if (_initializables == null) {
            _initializables = new LinkedHashSet<Initializable>();
        }
        _initializables.add(initializable);
    }

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

    /** Clone this actor into the specified workspace. The new actor is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new actor with the same ports as the original, but
     *  no connections and no container.  A container must be set before
     *  much can be done with this actor.
     *
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If cloned ports cannot have
     *   as their container the cloned entity (this should not occur), or
     *   if one of the attributes cannot be cloned.
     *  @return A new ComponentEntity.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        @SuppressWarnings("unchecked")
        AtomicActor<T> newObject = (AtomicActor<T>) super.clone(workspace);

        // Reset to force reinitialization of cache.
        newObject._initializables = null;
        newObject._inputPortsVersion = -1;
        newObject._outputPortsVersion = -1;
        newObject._cachedInputPorts = null;
        newObject._cachedOutputPorts = null;
        newObject._causalityInterface = null;
        newObject._causalityInterfaceDirector = null;
        newObject._receiversVersion = -1L;
        return newObject;
    }

    /** Create new receivers if the port is
     *  an input port and there is a director.
     *  @param port The port that has connection changes.
     */
    @Override
    public void connectionsChanged(Port port) {
        if (_debugging) {
            _debug("Connections changed on port: " + port.getName());
        }

        if (port instanceof IOPort) {
            IOPort castPort = (IOPort) port;
            Manager manager = getManager();
            if (castPort.isInput() && getDirector() != null && manager != null
                    && manager.getState() != Manager.IDLE
                    && manager.getState() != Manager.INFERING_WIDTHS
                    && manager.getState() != Manager.PREINITIALIZING) {
                try {
                    workspace().getWriteAccess();
                    castPort.createReceivers();
                } catch (IllegalActionException ex) {
                    // Should never happen.
                    throw new InternalErrorException(this, ex,
                            "Cannot create receivers on Port \""
                                    + port.getFullName() + "\".");
                } finally {
                    // Note that this does not increment the workspace version.
                    // We have not changed the structure of the model.
                    _workspace.doneTemporaryWriting();
                }
            }
        }
    }

    /** Create receivers for each input port.
     *  @exception IllegalActionException If any port throws it.
     */
    @Override
    public void createReceivers() throws IllegalActionException {
        if (workspace().getVersion() != _receiversVersion) {
            // NOTE:  Receivers are also getting created
            // in connectionChanged(). This to make sure that
            // the receivers are reset when the model changes while
            // running the model.

            Iterator<?> inputPorts = inputPortList().iterator();
            try {
                workspace().getWriteAccess();
                while (inputPorts.hasNext()) {
                    IOPort inputPort = (IOPort) inputPorts.next();
                    inputPort.createReceivers();
                }
                _receiversVersion = workspace().getVersion();
            } finally {
                // Note that this does not increment the workspace version.
                // We have not changed the structure of the model.
                workspace().doneTemporaryWriting();
            }
        }
    }

    /** Set the dependency between all output ports and all input
     *  ports of this actor. By default, each
     *  output port is assumed to have a dependency on all input
     *  ports. Since this is the assumed behavior, this method
     *  does nothing by default.
     *
     *  However, for subclasses such as {@link ptolemy.actor.lib.TimeDelay},
     *  where output ports depend on input ports with a time delay,
     *  this method should be overridden.
     *  Protected method _declareDelayDependency() should be used
     *  to declare dependency between input and output ports for
     *  this actor.
     *  @exception IllegalActionException Not thrown in this base
     *  class, derived classes should throw this exception if the
     *  delay dependency cannot be computed.
     *  @see #getCausalityInterface()
     *  @see #_declareDelayDependency(IOPort, IOPort, double)
     */
    public void declareDelayDependency() throws IllegalActionException {
    }

    /** Do nothing.  Derived classes override this method to define their
     *  primary run-time action.
     *
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public void fire() throws IllegalActionException {
        if (_debugging) {
            _debug("Called fire()");
        }
    }

    /** Return a causality interface for this actor. In this base class,
     *  we return an instance of {@link DefaultCausalityInterface}
     *  with the dependency type given by the director, or with a BooleanDependency
     *  if there is no director.
     *  This declares that all output ports of the actor depend on all input
     *  ports, unless the actor calls
     *  {@link #removeDependency(IOPort, IOPort)} or
     *  {@link #_declareDelayDependency(IOPort, IOPort, double)}.
     *  If this is called multiple times, the same object is returned each
     *  time unless the director has changed since the last call, in
     *  which case a new object is returned.
     *  @return A representation of the dependencies between input ports
     *   and output ports.
     * @exception IllegalActionException Thrown in subclasses if causality
     * interface cannot be computed.
     */
    @Override
    public CausalityInterface getCausalityInterface()
            throws IllegalActionException {
        Director director = getDirector();
        if (_causalityInterface != null
                && _causalityInterfaceDirector == director) {
            return _causalityInterface;
        }
        Dependency defaultDependency = BooleanDependency.OTIMES_IDENTITY;
        if (director != null) {
            defaultDependency = director.defaultDependency();
        }
        _causalityInterface = new DefaultCausalityInterface(this,
                defaultDependency);
        _causalityInterfaceDirector = director;
        return _causalityInterface;
    }

    /** Return the director responsible for the execution of this actor.
     *  In this class, this is always the executive director.
     *  Return null if either there is no container or the container has no
     *  director.
     *  @return The director that invokes this actor.
     */
    @Override
    public Director getDirector() {
        Nameable container = getContainer();

        if (container instanceof Actor) {
            return ((Actor) container).getDirector();
        }

        return null;
    }

    /** Return the executive director (same as getDirector()).
     *  @return The executive director.
     */
    @Override
    public Director getExecutiveDirector() {
        return getDirector();
    }

    /** Return the Manager responsible for execution of this actor,
     *  if there is one. Otherwise, return null.
     *  @return The manager.
     */
    @Override
    public Manager getManager() {
        try {
            _workspace.getReadAccess();

            Nameable container = getContainer();

            if (container instanceof Actor) {
                return ((Actor) container).getManager();
            }

            return null;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Initialize this actor.  Derived classes override this method
     *  to perform actions that should occur once at the beginning of
     *  an execution, but after type resolution.  Derived classes can
     *  produce output data and schedule events.
     *
     *  @exception IllegalActionException If a derived class throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        if (_debugging) {
            _debug("Called initialize()");
        }
        // First invoke initializable methods.
        if (_initializables != null) {
            for (Initializable initializable : _initializables) {
                initializable.initialize();
            }
        }
    }

    /** List all the input ports.
     *  This method is read-synchronized on the workspace.
     *  @return A list of input IOPort objects.
     */
    @Override
    public List<T> inputPortList() {
        if (_inputPortsVersion != _workspace.getVersion()) {
            try {
                _workspace.getReadAccess();

                // Update the cache.
                List<T> inputPorts = new LinkedList<T>();
                Iterator<T> ports = portList().iterator();

                while (ports.hasNext()) {
                    T p = ports.next();

                    if (p.isInput()) {
                        inputPorts.add(p);
                    }
                }

                // Create an arrayList, since the cache will not be
                // modified.  This reduces memory usage.
                _cachedInputPorts = new ArrayList<T>(inputPorts);
                _inputPortsVersion = _workspace.getVersion();
            } finally {
                _workspace.doneReading();
            }
        }

        return _cachedInputPorts;
    }

    /** Return true. Most actors are written so that the prefire() and
     *  fire() methods do not change the state of the actor. Hence, for
     *  convenience, this base class by default returns true. An actor
     *  that does change state in prefire() or fire() must override
     *  this method to return false.
     *
     *  @return True.
     */
    @Override
    public boolean isFireFunctional() {
        return true;
    }

    /** Return true unless all input ports have non-empty default values.
     *  By default, most actors do not
     *  check their inputs to see whether they are known.  They assume
     *  they are known.
     *  Note that ParameterPort is not treated as having a default value
     *  because such ports might be used in a context where it is important
     *  to supply them with an input value.
     *  @return False if this actor does not need to be provided with
     *   inputs to fire.
     *  @exception IllegalActionException If the defaultValue expression
     *   cannot be evaluated for an input port.
     */
    @Override
    public boolean isStrict() throws IllegalActionException {
        for (IOPort port : inputPortList()) {
            if (port.defaultValue.getToken() == null) {
                // Found an input port with no default value.
                return true;
            }
        }
        // If all input ports have default values, then report
        // that this actor is non-strict.
        // NOTE: If the actor has no input ports at all, this
        // returns false, indicating the actor is non-strict.
        return false;
    }

    /** Invoke a specified number of iterations of the actor. An
     *  iteration is equivalent to invoking prefire(), fire(), and
     *  postfire(), in that order. In an iteration, if prefire()
     *  returns true, then fire() will be called once, followed by
     *  postfire(). Otherwise, if prefire() returns false, fire()
     *  and postfire() are not invoked, and this method returns
     *  NOT_READY. If postfire() returns false, then no more
     *  iterations are invoked, and this method returns STOP_ITERATING.
     *  Otherwise, it returns COMPLETED. If stop() is called while
     *  this is executing, then cease executing and return STOP_ITERATING.
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

        int n = 0;

        try {
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

            if (_stopRequested) {
                return Executable.STOP_ITERATING;
            } else {
                return Executable.COMPLETED;
            }
        } catch (IllegalActionException ex) {
            // When fire() calls certain Token methods such as
            // add() or multiply() and the method throws an
            // exception, the exception will not include the
            // associated Nameable.  So, we catch exceptions
            // here and if the associated Nameable is null,
            // we rethrow with this as the Nameable.

            if (ex.getNameable1() == null) {
                throw new IllegalActionException(this, ex, ex.getMessage());
            } else {
                throw ex;
            }

        }

    }

    /** Create a new IOPort with the specified name.
     *  The container of the port is set to this actor.
     *  This method is write-synchronized on the workspace.
     *  Normally this method is not called directly by actor code.
     *  Instead, a change request should be queued with the director.
     *
     *  @param name The name for the new port.
     *  @return The new port.
     *  @exception NameDuplicationException If this actor already has a port
     *   with the specified name.
     *  @see ptolemy.kernel.util.Workspace#getWriteAccess()
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

    /** Return a new receiver of a type compatible with the director.
     *  Derived classes may further specialize this to return a receiver
     *  specialized to the particular actor.
     *
     *  @exception IllegalActionException If there is no director.
     *  @return A new object implementing the Receiver interface.
     */
    @Override
    public Receiver newReceiver() throws IllegalActionException {
        Director director = getDirector();

        if (director == null) {
            throw new IllegalActionException(this,
                    "Cannot create a receiver without a director.");
        }

        return director.newReceiver();
    }

    /** List the output ports.
     *  This method is read-synchronized on the workspace.
     *  @return A list of output IOPort objects.
     */
    @Override
    public List<T> outputPortList() {
        if (_outputPortsVersion != _workspace.getVersion()) {
            try {
                _workspace.getReadAccess();

                List<T> outputPorts = new LinkedList<T>();
                Iterator<T> ports = portList().iterator();

                while (ports.hasNext()) {
                    T p = ports.next();

                    if (p.isOutput()) {
                        outputPorts.add(p);
                    }
                }

                // Create an arrayList, since the cache will not be
                // modified.  This reduces memory usage.
                _cachedOutputPorts = new ArrayList<T>(outputPorts);
                _outputPortsVersion = _workspace.getVersion();
            } finally {
                _workspace.doneReading();
            }
        }

        return _cachedOutputPorts;
    }

    /** Return true, unless stop() has been called, in which case,
     *  return false.  Derived classes override this method to define
     *  operations to be performed at the end of every iteration of
     *  its execution, after one invocation of the prefire() method
     *  and any number of invocations of the fire() method.
     *  This method typically wraps up an iteration, which may
     *  involve updating local state.  In derived classes,
     *  this method returns false to indicate that this actor should not
     *  be fired again.
     *
     *  @return True if execution can continue into the next iteration.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        if (_debugging) {
            _debug("Called postfire()");
        }

        return !_stopRequested;
    }

    /** Return true. Derived classes override this method to define
     *  operations to be performed at the beginning of every iteration
     *  of its execution, prior the invocation of the fire() method.
     *  Derived classes may also use it to check preconditions for an
     *  iteration, if there are any.
     *
     *  @return True if this actor is ready for firing, false otherwise.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        if (_debugging) {
            _debug("Called prefire()");
        }

        return true;
    }

    /** Create receivers and declare delay dependencies. Derived classes
     *  can override this method to perform additional initialization
     *  functions, but they should call this base class methods or
     *  create the receivers themselves.
     *  This method gets executed exactly once prior to any other
     *  action methods.  It cannot produce output data since type
     *  resolution is typically not yet done. It also gets invoked
     *  prior to any static scheduling that might occur in the domain,
     *  so it can change scheduling information.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        if (_debugging) {
            _debug("Called preinitialize()");
        }

        _stopRequested = false;

        // For backward compatibility, in case there are actors
        // that override pruneDependencies() to alter their
        // causality interface, call it here.
        pruneDependencies();

        // Declare dependency for this actor. For actors such as
        // TimeDelay, the delay dependency between input and output
        // ports are declared.
        declareDelayDependency();

        // First invoke initializable methods.
        if (_initializables != null) {
            for (Initializable initializable : _initializables) {
                initializable.preinitialize();
            }
        }
    }

    /** Prune the dependency declarations, which by default state
     *  that each output port depends on all input ports in a firing.
     *  This base class does no pruning, but subclasses that have
     *  output ports that do not depend on input ports should override
     *  this method to prune the dependencies.  To declare that an
     *  output port does not depend on an input port, subclasses
     *  can call removeDependency(input, output) rather than implementing
     *  a specialized {@link CausalityInterface}, at least for the simple
     *  cases where output ports do not depend at all on input ports.
     *  @exception IllegalActionException Thrown by subclasses if causality interface
     *  cannot be computed.
     *  @see #removeDependency(IOPort, IOPort)
     *  @see #getCausalityInterface()
     *  @deprecated There is no need to override this method anymore.
     *   Just call removeDependency() in preinitialize().
     */
    @Deprecated
    public void pruneDependencies() throws IllegalActionException {
    }

    /** Record a firing event.
     *  @param type The firing event to be recorded.
     */
    @Override
    public void recordFiring(FiringEvent.FiringEventType type) {
        _actorFiring(new FiringEvent(null, this, type));
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

            if (_actorFiringListeners.size() == 0) {
                _notifyingActorFiring = false;
            }

            return;
        }
    }

    /** Remove the dependency that the specified output port has,
     *  by default, on the specified input port. By default, each
     *  output port is assumed to have a dependency on all input
     *  ports. Subclasses can call this method in preinitialize()
     *  instead of implementing a custom {@link CausalityInterface}
     *  for the simple cases where output ports do not depend
     *  at all on certain input ports.
     *  There should be one such call for each
     *  input, output pair that does not have a dependency.
     *  @param input The input port.
     *  @param output The output port that does not depend on the
     *   input port.
     *  @exception IllegalActionException If causality interface
     *  cannot be computed.
     *  @see #getCausalityInterface()
     */
    public void removeDependency(IOPort input, IOPort output)
            throws IllegalActionException {
        CausalityInterface causality = getCausalityInterface();
        causality.removeDependency(input, output);
    }

    /** Remove the specified object from the list of objects whose
     *  preinitialize(), initialize(), and wrapup()
     *  methods should be invoked upon invocation of the corresponding
     *  methods of this object. If the specified object is not
     *  on the list, do nothing.
     *  @param initializable The object whose methods should no longer be invoked.
     *  @see #addInitializable(Initializable)
     *  @see ptolemy.actor.CompositeActor#removePiggyback(Executable)
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

        super.setContainer(container);

        Director director = getDirector();

        // Invalidate the schedule and type resolution of the new director.
        if (director != null) {
            director.invalidateSchedule();
            director.invalidateResolvedTypes();
        }
    }

    /** Request that execution of the current iteration stop as soon
     *  as possible.  In this base class, we set a flag indicating that
     *  this request has been made (the protected variable _stopRequested).
     *  Most atomic actors have bounded fire() methods,
     *  so they can simply ignore this.  Atomic actors with unbounded
     *  fire() methods should react by saving their state
     *  and returning from the fire() method at the next convenient
     *  point.
     */
    @Override
    public void stop() {
        _stopRequested = true;

        if (_debugging) {
            _debug("Called stop()");
        }
    }

    /** Request that execution of the current iteration complete.
     *  Most atomic actors have bounded fire() methods, so they
     *  can simply ignore this.  Atomic actors with unbounded fire()
     *  methods should override this method to save their state and
     *  return from the fire() method at the next convenient point.
     *  In this base class, do nothing.
     */
    @Override
    public void stopFire() {
        if (_debugging) {
            _debug("Called stopFire()");
        }
    }

    /** Terminate execution immediately.  In this base class, call stop().
     *  Derived classes may wish to do something more aggressive, such
     *  as terminating any threads they have started.
     */
    @Override
    public void terminate() {
        if (_debugging) {
            _debug("Called terminate()");
        }

        stop();
    }

    /** Do nothing except invoke the wrapup() methods of any
     *  objects that have been registered with addInitializable().
     *  Derived classes override this method to define
     *  operations to be performed exactly once at the end of a complete
     *  execution of an application.  It typically closes
     *  files, displays final results, etc.
     *
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        if (_debugging) {
            _debug("Called wrapup()");
        }
        // Invoke initializable methods.
        if (_initializables != null) {
            for (Initializable initializable : _initializables) {
                initializable.wrapup();
            }
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
            Iterator<ActorFiringListener> listeners = _actorFiringListeners
                    .iterator();

            while (listeners.hasNext()) {
                listeners.next().firingEvent(event);
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

    /** Set the dependency between the input and output port to
     *  represent a time delay with the specified value.
     *  Subclasses can call this method
     *  instead of implementing a custom {@link CausalityInterface}
     *  for the cases where output ports depend on input ports with
     *  a time delay.
     *  If the time delay is 0.0, this method nonetheless
     *  assumes that the output port does not (immediately) depend on
     *  the input port (this amounts to a superdense time delay of
     *  (0.0, 1)). There should be one such call for each
     *  input, output pair that does not have a dependency.
     *  @param input The input port.
     *  @param output The output port with a time delay dependency on the
     *   input port.
     *  @param timeDelay The time delay.
     *  @exception IllegalActionException If causality interface
     *  cannot be computed.
     *  @see #getCausalityInterface()
     */
    protected void _declareDelayDependency(IOPort input, IOPort output,
            double timeDelay) throws IllegalActionException {
        CausalityInterface causality = getCausalityInterface();
        if (timeDelay == 0.0) {
            causality.declareDelayDependency(input, output, 0.0, 1);
        } else {
            causality.declareDelayDependency(input, output, timeDelay, 0);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The list of ActorFiringListeners registered with this object.
     *  NOTE: Because of the way we synchronize on this object, it should
     *  never be reset to null after the first list is created.
     */
    protected LinkedList<ActorFiringListener> _actorFiringListeners = null;

    /** List of objects whose (pre)initialize() and wrapup() methods
     *  should be slaved to these.
     */
    protected transient Set<Initializable> _initializables;

    /** Flag that is true if there are actor firing listeners. */
    protected boolean _notifyingActorFiring = false;

    /** Indicator that a stop has been requested by a call to stop(). */
    protected boolean _stopRequested = false;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Cached lists of input and output ports.
    private transient long _inputPortsVersion = -1;

    private transient List<T> _cachedInputPorts;

    private transient long _outputPortsVersion = -1;

    private transient List<T> _cachedOutputPorts;

    /** The causality interface, if it has been created. */
    private CausalityInterface _causalityInterface;

    /** The director for which the causality interface was created. */
    private Director _causalityInterfaceDirector;

    /** Record of the workspace version the last time receivers were created. */
    private long _receiversVersion = -1;
}
