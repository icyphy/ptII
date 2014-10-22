/* Director of the component interaction domain.

 Copyright (c) 2002-2014 The Regents of the University of California.
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
package ptolemy.domains.ci.kernel;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// CIDirector

/**
 Director of the component interaction (CI) domain. The CI domain supports
 two styles of interaction between actors, push and pull. In push interaction,
 the actor that produces data initiates the interaction. The receiving actor
 reacts to the data. The computation proceeds as data-driven. In pull
 interaction, the actor that consumes data decides when the interaction
 takes place, and the computation proceeds as demand-driven.
 <p>
 When a relation connects the push(pull) output port of one actor with the
 push(pull) input port of another, the style of interaction between the two
 actors is push(pull). To configure a port as a push port, add a parameter
 named <i>push</i> to the port, and give the value "true" to the parameter.
 Ports are pull ports by default. Relations connecting both push and pull
 ports are not supported. (TODO: add check in the director.)
 <p>
 Actors in a CI model are classified as either active or inactive. Each
 active actor is controlled by an {@link ActiveActorManager actor manager},
 which runs asynchronously with respect to the director (i.e. not in the
 same thread of control). Active actors include:
 <ul>
 <li> source actors with push output ports </li>
 <li> sink actors with pull input ports </li>
 <li> actors with pull input ports and push output ports </li>
 </ul>
 These actors initiate all the computation in a CI model. The director
 controls the execution of inactive actors by maintaining a task queue.
 A task in the queue is an inactive actor that either receives a token
 from an active actor via the push interaction, or is requested to
 produce a token by an active actor via the pull interaction. In one
 iteration of the CI model, the director removes the first actor from
 the queue. If the actor is pushed, the computation proceeds as
 data-driven from the actor, until the produced data reach actors that
 are either not ready to fire or with pull output ports and no pending
 pull request. If the actor is pulled, then the computation proceeds
 as demand-driven from the actor, until either the actor is fired or
 the pull request reaches actors that have push input ports and are not
 ready to fire.

 @author Xiaojun Liu, Yang Zhao
 @version $Id$
 @since Ptolemy II 3.0
 @Pt.ProposedRating Yellow (liuxj)
 @Pt.AcceptedRating Red (liuxj)
 */
public class CIDirector extends Director {
    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *  @exception NameDuplicationException If construction of Time objects fails.
     *  @exception IllegalActionException If construction of Time objects fails.
     */
    public CIDirector() throws IllegalActionException, NameDuplicationException {
        super();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace of this director.
     *  @exception NameDuplicationException If construction of Time objects fails.
     *  @exception IllegalActionException If construction of Time objects fails.
     */
    public CIDirector(Workspace workspace) throws IllegalActionException,
            NameDuplicationException {
        super(workspace);
    }

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *
     *  @param container The container.
     *  @param name The name of this director.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an attribute with the specified name.
     */
    public CIDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is an object with no container.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown in this base class
     *  @return The new object.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        CIDirector newObject = (CIDirector) super.clone(workspace);
        newObject._actorManagers = new HashSet();
        newObject._asyncPulledActors = new LinkedList();
        newObject._asyncPushedActors = new LinkedList();
        newObject._actorsToFire = new LinkedList();
        newObject._pulledActors = new HashSet();
        return newObject;
    }

    /** Check whether there is an actor pushed or pulled by an active
     *  actor. If there is a pushed actor, proceed with data-driven
     *  computation from the actor. For an asynchronously pulled actor,
     *  proceed with demand-driven computation from the actor.
     *  <p>
     *  If this director is not at the top level of the model, return
     *  after completing the data-driven computation for a pushed actor
     *  or propagating the pull request for a pulled actor; otherwise,
     *  wait until active actors produce pushed data or pull requests.
     *  <p>
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @exception IllegalActionException If any called method of one
     *  of the associated actors throws it.
     */
    @Override
    public void fire() throws IllegalActionException {
        Actor pushedActor = _nextAsyncPushedActor();

        if (pushedActor != null) {
            _actorsToFire.add(pushedActor);
        } else {
            Actor pulledActor = _nextAsyncPulledActor();

            if (pulledActor != null) {
                if (_debugging) {
                    _debug("Process async pulled actor "
                            + ((Nameable) pulledActor).getName());
                }

                if (pulledActor.prefire()) {
                    if (_debugging) {
                        _debug("Async pulled actor ready to fire "
                                + ((Nameable) pulledActor).getName());
                    }

                    _actorsToFire.add(pulledActor);
                } else {
                    if (_debugging) {
                        _debug("Request sync pull for async pulled actor "
                                + ((Nameable) pulledActor).getName());
                    }

                    _requestSyncPull(pulledActor);
                }
            }
        }

        if (_actorsToFire.size() > 0) {
            while (_actorsToFire.size() > 0) {
                Actor actor = (Actor) _actorsToFire.removeFirst();

                if (actor.prefire()) {
                    if (_debugging) {
                        _debug("Fire actor " + ((Nameable) actor).getName());
                    }

                    actor.fire();
                    actor.postfire();
                }
            }
        } else {
            // if this director is at the top level, wait for async push/pull
            // requests, otherwise just return
            if (!_isTopLevel()) {
                return;
            } else {
                synchronized (this) {
                    if (_asyncPushedActors.size() == 0
                            && _asyncPulledActors.size() == 0) {
                        try {
                            if (_debugging) {
                                _debug("Wait for async request...");
                            }

                            wait();

                            if (_debugging) {
                                _debug("Wake up from wait...");
                            }
                        } catch (InterruptedException ex) {
                            // stop
                            _stopRequested = true;
                        }
                    }
                }
            }
        }
    }

    /** Initialize the model controlled by this director. For each active
     *  actor, create an instance of ActiveActorManager and start it.
     *  <p>
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @exception IllegalActionException If the initialize() method of
     *   one of the associated actors throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        Nameable container = getContainer();

        if (container instanceof CompositeActor) {
            Iterator actors = ((CompositeActor) container).deepEntityList()
                    .iterator();

            while (actors.hasNext()) {
                Actor actor = (Actor) actors.next();

                if (_isActive(actor)) {
                    if (_debugging) {
                        _debug("Initialize -- create actor manager for "
                                + ((Nameable) actor).getName());
                    }

                    ActiveActorManager manager = new ActiveActorManager(actor,
                            this);
                    manager.start();
                }
            }
        }
    }

    /** Return a new receiver of type CIReceiver.
     *  @return A new CIReceiver.
     */
    @Override
    public Receiver newReceiver() {
        Receiver r = new CIReceiver(this);
        return r;
    }

    /** Return false if all active actors have finished execution and
     *  there is no pushed data or pull request to be processed;
     *  otherwise, return true.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        if (_actorManagers.size() == 0 && _asyncPushedActors.size() == 0
                && _asyncPulledActors.size() == 0 && _actorsToFire.size() == 0) {
            return false;
        } else {
            return !_finishRequested;
        }
    }

    /** If this director is at the top level of the model or there is pushed
     *  data or pull request to be processed, return true; otherwise, return
     *  false.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        _iteratingStarted = true;
        super.prefire();

        if (_pauseRequested) {
            synchronized (this) {
                notifyAll();
                _pauseRequested = false;
            }
        }

        if (_isTopLevel()) {
            return true;
        } else {
            return _asyncPushedActors.size() > 0
                    || _asyncPulledActors.size() > 0;
        }
    }

    /** Validate the attributes and then invoke the preinitialize()
     *  methods of all its deeply contained actors.
     *  This method is invoked once per execution, before any
     *  iteration, and before the initialize() method.
     *  Time is not set during this stage. So preinitialize() method
     *  of actors should not make use of time. They should wait
     *  until the initialize phase of the execution.
     *  <p>
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @exception IllegalActionException If the preinitialize() method of
     *   one of the associated actors throws it.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        _asyncPushedActors.clear();
        _asyncPulledActors.clear();
        _actorsToFire.clear();
        _pulledActors.clear();
        _actorManagers.clear();
        _pauseRequested = false;
        _iteratingStarted = false;

        Parameter interval = (Parameter) getAttribute("interval");

        if (interval != null) {
            _interval = ((IntToken) interval.getToken()).intValue();
        } else {
            _interval = 0;
        }
    }

    /** Request that the director cease execution altogether.
     *  This causes a call to stop() on all actors contained by
     *  the container of this director, and sets a flag
     *  so that the next call to postfire() returns false.
     */
    @Override
    public void stop() {
        super.stop();

        // FindBugs: [M M NN] Naked notify [NN_NAKED_NOTIFY]
        // Actually FindBugs in not completely correct
        // since super.stop() will change the state.
        // I however don't want to put super.stop() within
        // the synchronized(this) to avoid deadlocks.
        synchronized (this) {
            notifyAll();
        }
    }

    /** Request that execution of the current iteration stop.
     *  Pass the request to all actors that are deeply contained by the
     *  container of this director. Notify the threads that manage active
     *  actors to stop.
     */
    @Override
    public void stopFire() {
        super.stopFire();

        if (_debugging) {
            _debug("Stop fire called...");
        }

        if (_iteratingStarted) {
            _pauseRequested = true;
        }

        // FindBugs: [M M NN] Naked notify [NN_NAKED_NOTIFY]
        // Actually FindBugs in not completely correct
        // since super.stop() will change the state.
        // I however don't want to put super.stopFire() within
        // the synchronized(this) to avoid deadlocks.

        synchronized (this) {
            notifyAll();
        }
    }

    /** Terminate any currently executing model with extreme prejudice.
     *  This method is not intended to be used as a normal route of
     *  stopping execution. To normally stop execution, call the finish()
     *  method instead. This method should be called only
     *  when execution fails to terminate by normal means due to certain
     *  kinds of programming errors (infinite loops, threading errors, etc.).
     *  There is no assurance that the topology will be in a consistent
     *  state after this method returns.  The
     *  topology should probably be recreated before attempting any
     *  further operations.
     *  <p>
     *  Calls terminate() on all actors contained by the container
     *  of this director. Set the <i>_stopRequested</i> flag to be true,
     *  and interrupt the actor manager threads.
     */
    @Override
    public void terminate() {
        super.terminate();

        if (_debugging) {
            _debug("Terminate called...");
        }

        if (_iteratingStarted) {
            _stopRequested = true;
        }

        synchronized (this) {
            Iterator actorManagers = _actorManagers.iterator();

            while (actorManagers.hasNext()) {
                Thread actorManager = (Thread) actorManagers.next();
                actorManager.interrupt();
            }
        }
    }

    /** Invoke the wrapup() method of all the actors contained in the
     *  director's container. Notify and wait for the threads that manage
     *  active actors to stop.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *  @exception IllegalActionException If the wrapup() method of
     *   one of the associated actors throws it.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        _stopRequested = true;

        Iterator actors = ((CompositeEntity) getContainer()).entityList()
                .iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();

            if (_isActive(actor)) {
                synchronized (actor) {
                    actor.notifyAll();
                }
            }
        }

        synchronized (this) {
            Iterator actorManagers = _actorManagers.iterator();

            while (actorManagers.hasNext()) {
                Thread actorManager = (Thread) actorManagers.next();
                actorManager.interrupt();
            }

            while (_actorManagers.size() > 0) {
                try {
                    wait();
                } catch (InterruptedException ex) {
                }
            }
        }

        super.wrapup();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Schedule the pulled inactive actor to fire.
     *  @param actor The pulled actor that is ready to fire.
     */
    protected void _actorEnabled(Actor actor) {
        // assert actor is pulled
        if (_debugging) {
            _debug("Schedule pulled actor to fire "
                    + ((Nameable) actor).getName());
        }

        _pulledActors.remove(actor);
        _actorsToFire.add(actor);
    }

    /** Add the actor manager to the set of active actor managers.
     *  @param actorManager An active actor manager.
     */
    protected void _addActorManager(ActiveActorManager actorManager) {
        _actorManagers.add(actorManager);
    }

    /** Add the given actor to the list of actors that received pushed
     *  data from active actors. Notify this director.
     *  @param actor The actor that received pushed data from an active
     *   actor.
     */
    protected synchronized void _addAsyncPushedActor(Actor actor) {
        if (_debugging) {
            _debug("Async pushed actor " + ((Nameable) actor).getName());
        }

        _asyncPushedActors.add(actor);

        // this director may be waiting for an async request
        notifyAll();
    }

    /** Add the given actor to the list of synchronously pushed actors.
     *  This is done when the actor receives pushed data from an actor
     *  executing in the director's thread.
     *  @param actor The actor that received synchronously pushed data.
     */
    protected void _addSyncPushedActor(Actor actor) {
        if (_debugging) {
            _debug("Sync pushed actor " + ((Nameable) actor).getName());
        }

        _actorsToFire.add(actor);
    }

    /** Return true if the actor is active, such as: source with push
     *  output; sink with pull input; actor with pull input and push
     *  output.
     *  @param actor The actor to be tested whether it is active.
     *  @return True if the actor is active.
     * @exception IllegalActionException
     */
    protected static boolean _isActive(Actor actor)
            throws IllegalActionException {
        //TODO: check all inputs and outputs have the same setting.
        boolean inputIsPush = false;
        boolean hasInput = false;
        boolean outputIsPush = false;
        boolean hasOutput = false;
        Iterator inputPorts = actor.inputPortList().iterator();

        while (inputPorts.hasNext()) {
            IOPort port = (IOPort) inputPorts.next();

            if (port.isOutsideConnected()) {
                hasInput = true;
                inputIsPush |= _isPushPort(port);
            }
        }

        Iterator outputPorts = actor.outputPortList().iterator();

        while (outputPorts.hasNext()) {
            IOPort port = (IOPort) outputPorts.next();

            if (port.isOutsideConnected()) {
                hasOutput = true;
                outputIsPush |= _isPushPort(port);
            }
        }

        return (!hasInput && outputIsPush) || (!hasOutput && !inputIsPush)
                || (!inputIsPush && outputIsPush);
    }

    /** Return true if the given actor has a pending pull request.
     *  @param actor The actor to test.
     *  @return True if the given actor has been pulled.
     */
    protected boolean _isPulled(Actor actor) {
        return _pulledActors.contains(actor);
    }

    /** Return true if the port is a push port, that is a port having a
     *  parameter named push with boolean value true. Otherwise the
     *  port is considered a pull port.
     *  @param port The port to test.
     *  @return True if the port is a push port.
     */
    protected static boolean _isPushPort(IOPort port) {
        boolean result = false;
        Parameter p = (Parameter) port.getAttribute("push");

        if (p != null) {
            try {
                Token t = p.getToken();

                if (t instanceof BooleanToken) {
                    result = ((BooleanToken) t).booleanValue();
                }
            } catch (IllegalActionException e) {
                // ignore, the port is considered a pull port
            }
        }

        return result;
    }

    /** Return true if this director is requested to stop.
     *  @return True if this director is requested to stop.
     */
    protected boolean _isStopRequested() {
        return _stopRequested;
    }

    /** Remove the actor manager from the set of active actor managers.
     *  @param actorManager An active actor manager.
     */
    protected synchronized void _removeActorManager(
            ActiveActorManager actorManager) {
        _actorManagers.remove(actorManager);
        notifyAll();
    }

    /** Handle the pull request from the given active actor. Add actors
     *  providing data to the given actor to the asynchronously pulled actor
     *  list. Wake up the director thread if it is waiting.
     *  @param actor The active actor with pull request.
     */
    protected synchronized void _requestAsyncPull(Actor actor) {
        if (_debugging) {
            _debug("Async pull requested " + ((Nameable) actor).getName());
        }

        Iterator actors = _providerActors(actor).iterator();

        while (actors.hasNext()) {
            Actor a = (Actor) actors.next();

            if (!_asyncPulledActors.contains(a)) {
                _asyncPulledActors.add(a);

                if (_debugging) {
                    _debug("   Add async pulled actor "
                            + ((Nameable) a).getName());
                }
            }
        }

        notifyAll();
    }

    /** Record the given actor as being pulled, and recursively propagate
     *  the pull request to those actors providing data to it.
     *  @param actor The actor being pulled.
     *  @exception IllegalActionException If calling prefire() on an actor
     *  in that provides data to the actor being pulled throws it
     */
    protected void _requestSyncPull(Actor actor) throws IllegalActionException {
        _pulledActors.add(actor);

        if (_isPullThrough(actor)) {
            Iterator providers = _providerActors(actor).iterator();

            while (providers.hasNext()) {
                Actor provider = (Actor) providers.next();

                if (_isPullThrough(provider)) {
                    if (provider.prefire()) {
                        if (!_actorsToFire.contains(provider)) {
                            _actorsToFire.add(provider);
                        }
                    } else {
                        _requestSyncPull(provider);
                    }
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** If true, pause the execution of the model.
     */
    protected boolean _pauseRequested = false;

    /** The default interval between iterations of active actors.
     */
    protected long _interval;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // Return true if the actor has both pull input and pull output.
    private boolean _isPullThrough(Actor actor) throws IllegalActionException {
        boolean inputIsPush = false;
        boolean hasInput = false;
        boolean outputIsPush = false;
        Iterator inputPorts = actor.inputPortList().iterator();

        while (inputPorts.hasNext()) {
            IOPort port = (IOPort) inputPorts.next();

            if (port.isOutsideConnected()) {
                hasInput = true;
                inputIsPush |= _isPushPort(port);
            }
        }

        Iterator outputPorts = actor.outputPortList().iterator();

        while (outputPorts.hasNext()) {
            IOPort port = (IOPort) outputPorts.next();

            if (port.isOutsideConnected()) {
                outputIsPush |= _isPushPort(port);
            }
        }

        return !outputIsPush && (!hasInput || !inputIsPush);
    }

    // Return the next actor pulled by an active actor.
    private synchronized Actor _nextAsyncPulledActor() {
        if (_asyncPulledActors.size() > 0) {
            return (Actor) _asyncPulledActors.removeFirst();
        } else {
            return null;
        }
    }

    // Return the next actor having pushed data from an active actor.
    private synchronized Actor _nextAsyncPushedActor() {
        if (_asyncPushedActors.size() > 0) {
            return (Actor) _asyncPushedActors.removeFirst();
        } else {
            return null;
        }
    }

    // Return the list of actors providing data to the given actor.
    // Assume that the actor is homogeneous, i.e. it need one token at each
    // input port to fire, so this list contains the providers that have not
    // produced a token to the actor
    //TODO: handle multiports
    private List _providerActors(Actor actor) {
        List result = new LinkedList();
        Iterator ports = actor.inputPortList().iterator();

        while (ports.hasNext()) {
            IOPort port = (IOPort) ports.next();

            try {
                if (port.hasToken(0)) {
                    continue;
                }
            } catch (IllegalActionException ex) {
                // this should not happen
                throw new InternalErrorException("Error in testing token "
                        + "presence in the CI domain: " + ex);
            }

            Iterator sourcePorts = port.sourcePortList().iterator();

            while (sourcePorts.hasNext()) {
                IOPort sourcePort = (IOPort) sourcePorts.next();
                result.add(sourcePort.getContainer());
            }
        }

        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The list of active actor managers.
    private HashSet _actorManagers = new HashSet();

    // List of asynchronously pushed actors.
    private LinkedList _asyncPushedActors = new LinkedList();

    // List of asynchronously pulled actors.
    private LinkedList _asyncPulledActors = new LinkedList();

    // List of actors that got pushed input, or pulled and can fire.
    private LinkedList _actorsToFire = new LinkedList();

    // The set of actors being pulled.
    private HashSet _pulledActors = new HashSet();

    // Flag that indicates that an iteration is started.
    private boolean _iteratingStarted;
}
