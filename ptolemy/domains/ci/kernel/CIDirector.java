/* Director of the component interaction domain.

 Copyright (c) 2002 The Regents of the University of California.
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

@ProposedRating Yellow (liuxj@eecs.berkeley.edu)
@AcceptedRating Red (liuxj@eecs.berkeley.edu)
*/

package ptolemy.domains.ci.kernel;

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

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// CIDirector
/**
Director of the component interaction (CI) domain. A CIDirector governs
the execution of a CompositeActor with extended CORBA Event Service (ES)
semantics.
<p>
CORBA ES has two basic models: push model and pull model. In a push model,
the supplier decides when the data are sent; the consumer dose not take
any initiative and just waits for the data. In a pull model, the consumer
decides when to request data; the supplier dose not take any initiative and
just waits for those requesting the data. In a CORBA ES model, there could be
four kinds of components: event driven component (push input/push output),
demand driven component (pull input/pull output), queueing component
(push input/pull output), and active agent component (pull input/push output).
<p>
The CI domain does not aim for an implementation of CORBA ES. It abstracts the
push/pull semantics, and creates an environment to simulate different kinds of
interactions among components in a model.
<p>
In the CI domain, a port contains a push/pull attribute. According to the input
and output configuration, an actor can be sorted into four kinds as a
component in CORBA ES. An actor with push output may trigger the actor
receiving data from it to fire; an actor with pull input may pull the actor
providing data to it to fire.
<p>
An actor with pull input/push output, a source actor with push output, or
a sink actor with pull input is called an active actor. Each active actor
executes in a single thread created by the CI director. Actors that are not
active execute in the same thread as the director.
<p>
The CI director maintains several lists: the _asyncPushedActors list for actors
with push input that are triggered by active actors; the _asyncPulledActors
list for actors pulled by active actors; the _actorsToFire list for actors ready
to fire, including actors added by the director thread directly and actors from
the _asyncPulledActors and _asyncPushedActors list.
<p>
Input ports in a CI model contain instances of CIReceiver. When a token is put
into a CIReceiver, that receiver checks whether the port is push or pull, and
whether the current thread is the same as the director thread. If it is a push
port and the current thread is the same as the director thread, the director
will add the actor containing the port to the _actorsToFire list, so that the
model executes as data-driven. If it is a push port and the current thread is
not the same as the director thread, the active actor thread will add the actor
to _asyncPushedactors list. If it is a pull port, the director will check
whether the actor which contains the port has been pulled. If so and the prefire
is true, the director will remove this actor from _asyncPulledActors list and
add it to _actorToFire list; if the prefire return false, the director then
recursively registers actors providing data to this actor as being pulled.
<p>
Currently this director does not properly deal with cooperating with other
domains.

@author Xiaojun Liu, Yang Zhao
@version $Id$
@since Ptolemy II 2.1
*/
public class CIDirector extends Director {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public CIDirector() {
        super();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace of this object.
     */
    public CIDirector(Workspace workspace) {
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
     *   an entity with the specified name.
     */
    public CIDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Check whether there is an asynchronously pushed/pulled actor. If
     *  there is an asynchronously pushed actor, fire the actor and any
     *  actors triggered by the actor. For asynchronously pulled actors,
     *  if its prefire() returns true, then fire the actor, which may
     *  enable other pulled actors to fire, otherwise, request the actors
     *  providing data to it to be fired.
     *  <p>
     *  If the CI director is not at the top level of the hierarchy,
     *  return after completing the data-driven computation for the pushed
     *  actors or propagating pull requests; otherwise, wait until active
     *  actors produce pushed data or pull requests.
     *  <p>
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @exception IllegalActionException If any called method of one
     *  of the associated actors throws it.
     */
    public void fire() throws IllegalActionException {
        Actor pushedActor = _nextAsyncPushedActor();
        if (pushedActor != null) {
            _actorsToFire.add(pushedActor);
        } else {
            Actor pulledActor = _nextAsyncPulledActor();
            if (pulledActor != null) {
                if (_debugging)
                    _debug("Process async pulled actor "
                            + ((Nameable)pulledActor).getName());
                if (pulledActor.prefire()) {
                    if (_debugging)
                        _debug("Async pulled actor ready to fire "
                                + ((Nameable)pulledActor).getName());
                    _actorsToFire.add(pulledActor);
                } else {
                    if (_debugging)
                        _debug("Request sync pull for async pulled actor "
                                + ((Nameable)pulledActor).getName());
                    _requestSyncPull(pulledActor);
                }
            }
        }

        if (_actorsToFire.size() > 0) {
            while (_actorsToFire.size() > 0) {
                Actor actor = (Actor)_actorsToFire.removeFirst();
                if (actor.prefire()) {
                    if (_debugging)
                        _debug("Fire actor " + ((Nameable)actor).getName());
                    actor.fire();
                    actor.postfire();
                }
            }
        } else {
            // if this director is at the top-level, wait for async push/pull
            // requests, otherwise just return
            if (!_isTopLevel()) {
                return;
            } else {
                synchronized (this) {
                    if (_asyncPushedActors.size() == 0 &&
                            _asyncPulledActors.size() == 0) {
                        try {
                            if (_debugging)
                                _debug("Wait for async request...");
                            wait();
                            if (_debugging)
                                _debug("Wake up from wait...");
                        } catch (InterruptedException ex) {
                            //FIXME how to handle this
                        }
                    }
                }
            }
        }
    }

    /** Initialize the model controlled by this director. For each actor deeply
     *  contained by this director, check if it is an active actor. Create
     *  a thread (an instance of ActiveActorManager) for each active actor and
     *  start it.
     *  <p>
     *  This method is <i>not</i> synchronized on the workspace, so the caller
     *  should be.
     *
     *  @exception IllegalActionException If the initialize() method of
     *   one of the associated actors throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();

        Nameable container = getContainer();
        if (container instanceof CompositeActor) {
            Iterator actors =
                    ((CompositeActor)container).deepEntityList().iterator();
            while (actors.hasNext()) {
                Actor actor = (Actor)actors.next();
                if (_isActive(actor)) {
                    if (_debugging)
                        _debug("Initialize -- create actor manager for "
                                + ((Nameable)actor).getName());
                    ActiveActorManager manager =
                            new ActiveActorManager(actor, this);
                    manager.start();
                }
            }
        }
    }

    /** Return a new receiver of type CIReceiver.
     *  @return A new CIReceiver.
     */
    public Receiver newReceiver() {
        Receiver r = new CIReceiver(this);
        _receivers.add(r);
        return r;
    }

    /** Return false if all active actors have finished execution and there
     *  is no pushed data or pull request to be processed; otherwise,
     *  return true.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public boolean postfire() throws IllegalActionException {
        if (_actorManagers.size() == 0 && _asyncPushedActors.size() == 0 &&
                _asyncPulledActors.size() == 0 && _actorsToFire.size() == 0) {
            return false;
        } else {
            return true;
        }
    }

    /** If the CI director is at the top level of the hierarchy, always
     *  return true. If the CI director is not at the top level, return true
     *  if there is pushed data or pull request to be processed; otherwise,
     *  return false.
     *
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public boolean prefire() throws IllegalActionException {
        _iteratingStarted = true;
        super.prefire();
        if (_isTopLevel()) {
            if (_debugging)
                _debug("Is top-level, prefire returns true...");
            return true;
        } else {
            return (_asyncPushedActors.size() > 0)
                    || (_asyncPulledActors.size() > 0);
        }
    }

    /** This method is invoked once per execution, before any
     *  iteration, and before the initialize() method.
     *  Time is not set during this stage. So preinitialize() method
     *  of actors should not make use of time. They should wait
     *  until the initialize phase of the execution.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @exception IllegalActionException If the preinitialize() method of
     *   one of the associated actors throws it.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        _asyncPushedActors.clear();
        _asyncPulledActors.clear();
        _actorsToFire.clear();
        _pulledActors.clear();
        _actorManagers.clear();
        _stopRequested = false;
        _iteratingStarted = false;
        _directorThread = Thread.currentThread();
        _receivers.clear();
        Parameter interval = (Parameter)getAttribute("interval");
        if (interval != null) {
            _interval = ((IntToken)interval.getToken()).intValue();
        } else {
            _interval = 0;
        }
    }

    /** Request that execution of the current iteration stop.
     *  Pass the request to all actors that are deeply contained by the
     *  container of this director. Notify the threads that manage active
     *  actors to stop.
     */
    public void stopFire() {
        super.stopFire();
        if (_debugging)
            _debug("Stop fire called...");
        if (_iteratingStarted) {
            _stopRequested = true;
        }
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
     *  of this director. set the <i>_stopRequested<i> flag to be true,
     *  so that the ActiveActorManager thread will terminate the executing
     *  of the active actor.
     */
    public void terminate() {
        super.terminate();
        if (_debugging)
            _debug("Terminate called...");
        if (_iteratingStarted) {
            _stopRequested = true;
        }
        synchronized (this) {
            notifyAll();
        }
        //FIXME: terminate actor managers
    }

    /** Invoke the wrapup() method of all the actors contained in the
     *  director's container. Notify and wait for the threads that manage
     *  active actors to stop.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @exception IllegalActionException If the wrapup() method of
     *   one of the associated actors throws it.
     */
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        if (_debugging)
            _debug("Wrap up...");
        _stopRequested = true;
        Iterator actors =
                ((CompositeEntity)getContainer()).entityList().iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor)actors.next();
            if (_isActive(actor)) {
                synchronized (actor) {
                    actor.notifyAll();
                }
            }
        }

        while (_actorManagers.size() > 0) {
            try {
                synchronized (this) {
                    wait();
                }
            } catch (InterruptedException ex) {}
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Schedule the enabled (pulled) actor to fire.
     *  @param actor The pulled actor that is ready to fire.
     */
    protected void _actorEnabled(Actor actor) {
        // assert actor is pulled
        if (_debugging)
            _debug("Schedule pulled actor to fire "
                    + ((Nameable)actor).getName());
        _pulledActors.remove(actor);
        _actorsToFire.add(actor);
    }

    /** Add the actor manager to the set of active actor managers.
     *  @param actorManager An active actor manager.
     */
    protected void _addActorManager(ActiveActorManager actorManager) {
        _actorManagers.add(actorManager);
    }

    /** Add the given actor to the list that contains actors that received
     *  pushed data from active actors. Notify the director that there is
     *  pushed data to process.
     *  @param actor The actor that received pushed data from an active actor.
     */
    protected synchronized void _addAsyncPushedActor(Actor actor) {
        if (_debugging)
            _debug("Async pushed actor " + ((Nameable)actor).getName());
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
        if (_debugging)
            _debug("Sync pushed actor " + ((Nameable)actor).getName());
        _actorsToFire.add(actor);
    }

    /** Return true if the actor is active, such as: source with push output;
     *  sink with pull input; actor with pull input and push output.
     *  @param actor The actor to be tested whether it is active.
     */
    protected static boolean _isActive(Actor actor) {
        boolean inputIsPush = false;
        boolean hasInput = false;
        boolean outputIsPush = false;
        boolean hasOutput = false;
        Iterator inputPorts = actor.inputPortList().iterator();
        while (inputPorts.hasNext()) {
            IOPort port = (IOPort)inputPorts.next();
            if (port.getWidth() > 0) {
                hasInput = true;
                inputIsPush |= _isPushPort(port);
            }
        }
        Iterator outputPorts = actor.outputPortList().iterator();
        while (outputPorts.hasNext()) {
            IOPort port = (IOPort)outputPorts.next();
            if (port.getWidth() > 0) {
                hasOutput = true;
                outputIsPush |= _isPushPort(port);
            }
        }
        return (!hasInput && outputIsPush) || (!hasOutput && !inputIsPush)
                || (!inputIsPush && outputIsPush);
    }

    /** Return true if the given actor has been pulled.
     *  @param actor The actor to test.
     *  @return True if the given actor has been pulled.
     */
    protected boolean _isPulled(Actor actor) {
        return _pulledActors.contains(actor);
    }

    /** Return true if the port is a push port, that is a port having a
     *  parameter named push with boolean value true.
     *  @param port The port to test.
     *  @return True if the port is a push port.
     */
    protected static boolean _isPushPort(IOPort port) {
        boolean result = false;
        Parameter p = (Parameter)port.getAttribute("push");
        if (p != null) {
            try {
                Token t = p.getToken();
                if (t instanceof BooleanToken) {
                    result = ((BooleanToken)t).booleanValue();
                }
            } catch (IllegalActionException e) {
                // ignore, the port is considered not a push port
            }
        }
        return result;
    }

    /** Return the thread of the CI director.
     *  @return The director thread.
     */
    protected Thread _getThread() {
        return _directorThread;
    }

    /** Remove the actor manager from the set of active actor managers.
     *  @param actorManager An active actor manager.
     */
    protected synchronized void
            _removeActorManager(ActiveActorManager actorManager) {
        _actorManagers.remove(actorManager);
        notifyAll();
    }

    /** Handle the pull request from the given active actor. Add actors
     *  providing data to the given actor to the asynchronously pulled actor
     *  list. Wake up the director thread if it is waiting.
     *  @param actor The active actor with pull request.
     */
    protected synchronized void _requestAsyncPull(Actor actor) {
        if (_debugging)
            _debug("Async pull requested " + ((Nameable)actor).getName());
        Iterator actors = _providerActors(actor).iterator();
        while (actors.hasNext()) {
            Actor a = (Actor)actors.next();
            if (!_asyncPulledActors.contains(a)) {
                _asyncPulledActors.add(a);
                if (_debugging)
                    _debug("   Add async pulled actor "
                            + ((Nameable)a).getName());
            }
        }
        notifyAll();
    }

    /** Record the given actor as being pulled, and recursively propagate
     *  the pull request to those actors providing data to it.
     *  @param actor The actor being pulled.
     */
    protected void _requestSyncPull(Actor actor)
            throws IllegalActionException {
        _pulledActors.add(actor);
        if (_isPullThrough(actor)) {
            Iterator providers = _providerActors(actor).iterator();
            while (providers.hasNext()) {
                Actor provider = (Actor)providers.next();
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

    /** Flag that indicates that a stop has been requested.
     */
    protected boolean _stopRequested = false;

    /** The thread in which this director executes.
     */
    protected Thread _directorThread;

    /** The default interval between iterations of active actors.
     */
    protected long _interval;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Return the next actor having pushed data from an active actor.
    private synchronized Actor _nextAsyncPushedActor() {
        if (_asyncPushedActors.size() > 0)
            return (Actor)_asyncPushedActors.removeFirst();
        else
            return null;
    }

    // Return the next actor pulled by an active actor.
    private synchronized Actor _nextAsyncPulledActor() {
        if (_asyncPulledActors.size() > 0)
            return (Actor)_asyncPulledActors.removeFirst();
        else
            return null;
    }

    // Return the list of actors providing data to the given actor.
    //FIXME:
    // assumes that the actor is homogeneous, i.e. it need one token at each
    // input port to fire, so this list contains the providers that have not
    // produced a token to the actor
    private List _providerActors(Actor actor) {
        List result = new LinkedList();
        Iterator ports = actor.inputPortList().iterator();
        while (ports.hasNext()) {
            IOPort port = (IOPort)ports.next();

            //FIXME: handle multiports
            try {
                if (port.hasToken(0)) continue;
            } catch (IllegalActionException ex) {
                // this should not happen
                throw new InternalErrorException("Error in testing token "
                        + "presence in the CI domain: " + ex);
            }
            Iterator sourcePorts = port.sourcePortList().iterator();
            while (sourcePorts.hasNext()) {
                IOPort sourcePort = (IOPort)sourcePorts.next();
                result.add(sourcePort.getContainer());
            }
        }
        return result;
    }

    // Return true if the actor has both pull input and pull output.
    private boolean _isPullThrough(Actor actor) {
        boolean inputIsPush = false;
        boolean hasInput = false;
        boolean outputIsPush = false;
        Iterator inputPorts = actor.inputPortList().iterator();
        while (inputPorts.hasNext()) {
            IOPort port = (IOPort)inputPorts.next();
            if (port.getWidth() > 0) {
                hasInput = true;
                inputIsPush |= _isPushPort(port);
            }
        }
        Iterator outputPorts = actor.outputPortList().iterator();
        while (outputPorts.hasNext()) {
            IOPort port = (IOPort)outputPorts.next();
            if (port.getWidth() > 0) {
                outputIsPush |= _isPushPort(port);
            }
        }
        return (!outputIsPush && (!hasInput || !inputIsPush));
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

    // List of CI receivers.
    private LinkedList _receivers = new LinkedList();

    // Flag that indicates that an iteration is started.
    private boolean _iteratingStarted;
}

