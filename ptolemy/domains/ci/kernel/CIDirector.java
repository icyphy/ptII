/* Director of the component interaction domain.

 Copyright (c) 2002-2003 The Regents of the University of California.
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

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;
import ptolemy.data.expr.Parameter;
import ptolemy.data.IntToken;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.HashSet;
import java.util.Collections;
import java.io.PrintStream;

//////////////////////////////////////////////////////////////////////////
//// CIDirector
/**

Director of the component interaction(CI) domain. A CIDirector governs
the execution of a CompositeActor with extended CORBA Event
Service(ES) semantics.

<p> CORBA ES has two basic models: push model and pull model. In a
push model, the supplier decides when the data are sent; the consumer
doesn't take any initiative and just waits for the data. In a pull
model, the consumer decides when to go for data; the supplier doesn't
take any initiative and just waits for those requesting the data. In a
CORBA ES model, there could be four kinds of components: event driven
component(push input/push output), demand driven component(pull
input/pull output), queueing component(push input/pull output), and
active agent component(pull input/push output).

<p> The CI domain doesn't aim for an implementation of CORBA ES. It
abstracts the push/pull semantics, and creates an environment to
simulate different kinds of interaction among components in a model.

<p> In the CI domain, a port contains a push/pull attribute. According
to the input and output configuration, an actor can be sorted into
four kinds as a component in CORBA ES. An actor with push output may
trigger the actor receiving data from it to fire; an actor with pull
input may pull the actor providing data to it to fire. An actor with a
push output may not be connected to an actor with pull input
directly(???)

<p> An actor with pull input/push output, a source actor with push
output, or a sink actor with pull input has to be executed by a single
thread, otherwise it is a dead component in the model. This kind of
actors is referred to active actors. The CI director creates a thread
(an instance of ActiveActorManager) for each active actor.

<p> In the CI domain, there are two kinds of threads: the director
thread and the active actor thread. The director thread governs the
execution of unactive actors. Active actors can register to the CI
director to fire an unactive actor via _addAsyncPushedActor(Actor
actor) or _requestAsyncPull(Actor actor) method.

<p> The CI director maintains several lists: the _asyncPushedActors
list for actors with push input and are trigged by active actors; the
_asyncPulledActors list for actors pulled by active actors; the
_actorsToFire list for actors ready to fire, including actors added by
the director thread directly and actors from the _asyncPulledActors
and _asyncPushedActors list.

<p> Input ports in a CI model contain instances of CIReceiver. When a
token is put into a CIReceiver, that receiver check whether the port
is push or pull, and whether the current thread equals to the director
thread. If it is a push port and the current thread equals to the
director thread, the director will add the actor contains the port to
_actorsToFire list. If it is a push port and the current thread
doesn't equal to the director thread, the active actor thread will add
the actor to _asyncPushedActors list. If it is a pull port, the
current thread has to equal to the director thread. The director will
check whether the actor which contains the port has been pulled. if so
and the prefire is true, the director will remove this actor from
_asyncPulledActors list and add it to _actorToFire list; if the
prefire return false, the director then register actors providing data
to this actor to be fired.

<p> Currently this director does not properly deal with cooperating
with other domains.

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

    /** Check the _asyncPushedActors and _asyncPulledActors lists. For
     *  asynchronous pushed actors, add them to the _actorsToFire list. For
     *  asynchronous pulled actors, if its prefire returns true, add it to
     *  the _actorsToFire list, otherwise, request the actors providing data to
     *  it to be fired.
     *  <p>Fire all the actors ready to be fired in the _actorsToFire list.
     *  Then, if the CI director is not at the top level of the hierarchy,
     *  return; otherwise, wait until active actors add actors to the
     *  _asyncPushedActors or _asyncPulledActors list.
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
            //if is top-level, wait for async pushed actor,
            // otherwise just return
            if (!_isTopLevel()) {
                return;
            } else {
                //FIXME: which object to synchronize on
                //FIXME: move to prefire, and wait there?
//NOTE: check for any async push/pull here before wait
                synchronized (this) {
                    if (_asyncPushedActors.size() == 0 &&
                            _asyncPulledActors.size() == 0) {
                        try {
                            if (_debugging)
                                _debug("Wait for async request...");
                            wait();
                            if (_debugging)
                                _debug("Wake up from wait...");
                        } catch (InterruptedException ex) {}
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

        // get the list of active actors, start a manager for each
        Nameable container = getContainer();
        if (container instanceof CompositeActor) {
            Iterator actors = ((CompositeActor)container)
                .deepEntityList().iterator();
            while (actors.hasNext()) {
                Actor actor = (Actor)actors.next();
                if (_isActive(actor)) {
                    if (_debugging)
                        _debug("Initialize -- create manager for "
                                + ((Nameable)actor).getName());
                    ActiveActorManager manager =
                            new ActiveActorManager(actor, this);
                    _actorManagers.add(manager);
                    manager.start();
                }
            }
        }
    }

    /** Return a new receiver of a type CIReceiver.
     *  @return A new CIReceiver.
     */
    public Receiver newReceiver() {
        Receiver r = new CIReceiver(this);
        _receivers.add(r);
        return r;
    }

    /** Return false if there is no active actor and the _asyncPushedActors
     *  _asyncPulledActors, and _actorsToFire lists are all empty; otherwise,
     *  return true.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public boolean postfire() throws IllegalActionException {
        //FIXME: or all active source actors are done
        if (_activeCount == 0 && _asyncPushedActors.size() == 0 &&
                _asyncPulledActors.size() == 0 && _actorsToFire.size() == 0) {
            return false;
        } else {
            return true;
        }
    }

    /** If the CI director is at the top level of the hierarchy, always
     *  return ture. if the CI director is not at the top level, return ture
     *  if the _asyncPushedActors or _asyncPulledActors list is not empty;
     *  otherwise, return false.
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
        _activeCount = 0;
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
     *  container of this director. and if <i>_interatingStarted<i> is ture, set
     *  the <i>_stopRequested<i> flag to be ture, so that the ActiveActorManager
     *  thread will suspend the executing of the active actor.
     */
    public void stopFire() {
        super.stopFire();
        if (_debugging)
            _debug("Stop fire called...");

        System.out.println(getName() +
                " set stop requested to true in stopFire()");
        try {
        Exception ex = new Exception();
        ex.fillInStackTrace();
        ex.printStackTrace(new PrintStream(System.out));
        } catch (Exception ex) {}
        if (_iteratingStarted) {
            _stopRequested = true;
        }
        synchronized (this) {
            notifyAll();
        }
        //FIXME: notify actor managers to stop
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
     *  of this director. set the <i>_stopRequested<i> flag to be ture,
     *  so that the ActiveActorManager thread will terminate the executing
     *  of the active actor.
     */
    public void terminate() {
        super.terminate();
        if (_debugging)
            _debug("Terminate called...");
        System.out.println(getName() +
                " set stop requested to true in terminate()");
        _stopRequested = true;
        //FIXME: terminate actor managers
    }

    /** Invoke the wrapup() method of all the actors contained in the
     *  director's container. set the <i>_stopRequested<i> flag to be ture,
     *  notify actors synchronized on it(???) and wait for all
     *  activeActorManager to finish.
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

        System.out.println(getName()
                + " set stop requested to true in stopFire()");
        try {
        Exception ex = new Exception();
        ex.fillInStackTrace();
        ex.printStackTrace(new PrintStream(System.out));
        } catch (Exception ex) {}

        System.out.println(getName()
                + " set stop requested to true in wrapup()");
        _stopRequested = true;
        /*Iterator receivers = _receivers.iterator();
        while (receivers.hasNext()) {
            CIReceiver receiver = (CIReceiver)receivers.next();
            synchronized (receiver) {
                receiver.notifyAll();
            }
        }*/
        Iterator actors =
                ((CompositeEntity)getContainer()).entityList().iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor)actors.next();
            synchronized (actor) {
                actor.notifyAll();
            }
        }

        while (_activeCount > 0) {
            try {
                synchronized (this) {
                    wait();
                }
            } catch (InterruptedException ex) {}
        }
        //FIXME: wait for all managers to finish
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Schedule the enabled (pulled) actor to fire.
     */
    protected void _actorEnabled(Actor actor) {
        // assert actor is pulled
        if (_debugging)
            _debug("Schedule pulled actor to fire "
                    + ((Nameable)actor).getName());
        _pulledActors.remove(actor);
        _actorsToFire.add(actor);
    }

    /** Add the given actor to the list of asynchronously pushed actors.
     */
    protected synchronized void _addAsyncPushedActor(Actor actor) {
        if (_debugging)
            _debug("Async pushed actor " + ((Nameable)actor).getName());
        _asyncPushedActors.add(actor);
        // the director may be waiting for an async request
        notifyAll();
    }

    /** Add the given actor to the list of synchronously pushed actors.
     *  This is done when the actor receives input from an actor executing
     *  in the director's thread.
     */
    protected void _addSyncPushedActor(Actor actor) {
        if (_debugging)
            _debug("Sync pushed actor " + ((Nameable)actor).getName());
        _actorsToFire.add(actor);
    }

    /**Get the thread of the CI director.
     * @return the director thread.
     *
     */
    protected Thread _getThread() {
        return _directorThread;
    }

    /** Return true if the actor is active, such as:
     *  source with push output; sink with pull input; actor with pull input
     *  and push output.
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
                if (port.getAttribute("push") != null) inputIsPush = true;
            }
        }
        Iterator outputPorts = actor.outputPortList().iterator();
        while (outputPorts.hasNext()) {
            IOPort port = (IOPort)outputPorts.next();
            if (port.getWidth() > 0) {
                hasOutput = true;
                if (port.getAttribute("push") != null) outputIsPush = true;
            }
        }
        return (!hasInput && outputIsPush) || (!hasOutput && !inputIsPush)
                || (!inputIsPush && outputIsPush);
    }

    /** Return true if the given actor is in the set of pulled actors.
     *
     */
    protected boolean _isPulled(Actor actor) {
        return _pulledActors.contains(actor);
    }

    /** Handle the pull request from the given actor. Add actors providing data
     *  to the given actor to the asynchronously pulled actor list. Wake up the
     *  director thread if it is waiting.
     *
     */
    protected synchronized void _requestAsyncPull(Actor actor) {
        if (_debugging)
            _debug("Async pull requested " + ((Nameable)actor).getName());
        //_asyncPulledActors.addAll(_providerActors(actor));
        Iterator actors1 = _providerActors(actor).iterator();
        while (actors1.hasNext()) {
            Actor actor1 = (Actor)actors1.next();
            if (!_asyncPulledActors.contains(actor1))
                _asyncPulledActors.add(actor1);
        }

        if (_debugging) {
            String message = "  Provider actors are:";
            Iterator actors = _providerActors(actor).iterator();
            while (actors.hasNext()) {
                message += " " + ((Nameable)actors.next()).getName();
            }
            _debug(message);
        }
        notifyAll();
    }

    /** If the given actor is pulled, but is not ready to fire yet, then
     *  propagate the pull request to actors providing data to it.
     *
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
    ////                         protected variables                 ////

    /** Number of active actors. */
    protected int _activeCount = 0;


    //FIXME: is it possible that the director is called by the manager from
    // more than one threads? Provide a protected method to get this?

    /** The CI director thread. */
    protected Thread _directorThread;

    /** Interval between iterations of an actor. */
    protected long _interval;

    /** Flag that indicates that a stop has been requested. */
    protected boolean _stopRequested = false;



    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // return true if actor is a pull source or both its input and output are
    // pull.
    private boolean _isPullThrough(Actor actor) {
        boolean inputIsPush = false;
        boolean hasInput = false;
        boolean outputIsPush = false;
        Iterator inputPorts = actor.inputPortList().iterator();
        while (inputPorts.hasNext()) {
            IOPort port = (IOPort)inputPorts.next();
            if (port.getWidth() > 0) {
                hasInput = true;
                if (port.getAttribute("push") != null) inputIsPush = true;
            }
        }
        Iterator outputPorts = actor.outputPortList().iterator();
        while (outputPorts.hasNext()) {
            IOPort port = (IOPort)outputPorts.next();
            if (port.getWidth() > 0) {
                if (port.getAttribute("push") != null) outputIsPush = true;
            }
        }
        return (!outputIsPush && (!hasInput || !inputIsPush));
    }

    //get the next actor from the asynchronously pulled actor list.
    //@return the next actor.
    private synchronized Actor _nextAsyncPulledActor() {
        Actor result = null;
        if (_asyncPulledActors.size() > 0) {
            result = (Actor)_asyncPulledActors.removeFirst();
        }
        return result;
    }

    //get the next actor from the asynchronously pushed actor list.
    //@return the next actor.
    private synchronized Actor _nextAsyncPushedActor() {
        if (_asyncPushedActors.size() > 0)
            return (Actor)_asyncPushedActors.removeFirst();
        else
            return null;
    }


    //Get actors providing data to the given actor.
    //FIXME: duplication in the returned list?
    //assume that the actor is homogeneous, i.e. it need one token at each
    //input port to fire, so this list contains the providers that have not
    //produced a token to the actor
    private List _providerActors(Actor actor) {
        List result = new LinkedList();
        Iterator ports = actor.inputPortList().iterator();
        while (ports.hasNext()) {
            IOPort port = (IOPort)ports.next();

            //FIXME: this does not handle multi-ports
            try {
                if (port.hasToken(0)) continue;
            } catch (IllegalActionException ex) {
                // this should not happen
                throw new InternalErrorException("Error in testing token "
                        + "presence in CI domain: " + ex);
            }

            Iterator sourcePorts = port.sourcePortList().iterator();
            while (sourcePorts.hasNext()) {
                IOPort sourcePort = (IOPort)sourcePorts.next();
                result.add(sourcePort.getContainer());
            }
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // list of asynchronously pushed actors
    private LinkedList _asyncPushedActors = new LinkedList();

    // list of asynchronously pulled actors
    private LinkedList _asyncPulledActors = new LinkedList();

    // list of actors that have pushed input, or pulled and can fire
    private LinkedList _actorsToFire = new LinkedList();

    // the set of actors being pulled
    private HashSet _pulledActors = new HashSet();

    // the list of active actor managers
    private LinkedList _actorManagers = new LinkedList();

    // list of CI receivers.
    private LinkedList _receivers = new LinkedList();

    // flag that indicates that a iteration is started.
    private boolean _iteratingStarted;
}
