/* MetroIIDEDirector is a Discrete Event (DE) director that adapts to MetroII semantics.

Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 1995-2014 The Regents of the University of California.
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

Ptolemy II includes the work of others, to see those copyrights, follow
the copyright link on the splash page or see copyright.htm.
 */

package ptolemy.domains.metroII.kernel;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

import net.jimblackler.Utils.CollectionAbortedException;
import net.jimblackler.Utils.ResultHandler;
import net.jimblackler.Utils.YieldAdapterIterable;
import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.FiringEvent;
import ptolemy.actor.IOPort;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.domains.de.kernel.DEEvent;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event.Builder;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// MetroIIDEDirector

/**
 * MetroIIDEDirector is a Discrete Event (DE) director that adapts to MetroII
 * semantics. This MoC is mainly used to design the functional model, in which
 * some actors can be mapped to an architectural models. This allows users to
 * explore choices about how the model can be implemented.
 *
 * <p>
 * In DE director, events are totally ordered and executed. In
 * MetroIIDEDirector, these events are called Ptolemy events that are still
 * totally ordered. But the execution of Ptolemy events has some variances.
 * Typically, a Ptolemy event is associated with a fire() action of an actor.
 * Executing the Ptolemy event triggers the firing of the actor. In
 * MetroIIDEDirector, the firing has two variances, depending on the types of
 * the actor:</p>
 * <ol>
 * <li>If the actor is a normal Ptolemy actor, the execution of the event
 * instantly triggers fire() of the actor.</li>
 * <li>If the actor is an actor with a MetroII wrapper (@see BlockingFire or @see
 * ResumableFire), the execution of the event will trigger a MetroII event to be
 * PROPOSED. The firing will not be executed until the MetroII event is
 * NOTIFIED.</li>
 * </ol>
 * <p>A MetroII actor is a Ptolemy actor that implements GetFirable interface,
 * which includes MetroIICompositeActor. To understand MetroII event and its
 * states (e.g. PROPOSED, WAITING, NOTIFIED), please @see MetroIIDirector.
 * </p>
 *
 * <p>
 * By using a MetroII actor under the MetroIIDEDirector, the user understands
 * the firing of the MetroII actor might be delayed because the scheduling is
 * not solely determined by the MetroIIDEDirector but also determined by
 * MetroIIDirector on the upper level and the architectural model which the
 * MetroII actor is mapped onto. This introduces some non-determinisms. But
 * these non-determinisms are desirable and can be used to optimize the
 * architectures.
 * </p>
 *
 * <p>
 * It is highly recommend not to place MetroIIDEDirector in a
 * MetroIICompositeActor under another MetroIIDEDirector because there would be
 * a semantic conflict if the enclosed MetroIIDEDirector directs a normal
 * Ptolemy actor.
 * </p>
 *
 * @author Liangpeng Guo
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (glp)
 * @Pt.AcceptedRating Red (glp)
 *
 */
public class MetroIIDEDirector extends DEDirector implements GetFirable {

    /**
     * Constructs a director in the given container with the given name. The
     * container argument must not be null, or a NullPointerException will be
     * thrown. If the name argument is null, then the name is set to the empty
     * string. Increment the version number of the workspace.
     *
     * @param container
     *            Container of the director.
     * @param name
     *            Name of this director.
     * @exception IllegalActionException
     *                If the director is not compatible with the specified
     *                container. May be thrown in a derived class.
     * @exception NameDuplicationException
     *                If the container is not a CompositeActor and the name
     *                collides with an entity in the container.
     */
    public MetroIIDEDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        setEmbedded(false);
        _initializeParameters();

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Clones the object into the specified workspace. The new object is
     * <i>not</i> added to the directory of that workspace (you must do this
     * yourself if you want it there).
     *
     * @param workspace
     *            The workspace for the cloned object.
     * @exception CloneNotSupportedException
     *                Not thrown in this base class
     * @return The new Attribute.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        MetroIIDEDirector newObject = (MetroIIDEDirector) super
                .clone(workspace);
        newObject._nameToActor = (Hashtable<String, Actor>) _nameToActor
                .clone();
        newObject._actorDictionary = (Hashtable<String, FireMachine>) _actorDictionary
                .clone();
        newObject._events = (ArrayList<Builder>) _events.clone();

        newObject.actorList = (ArrayList<Actor>) actorList.clone();

        newObject._pendingIteration = (Hashtable<String, Integer>) _pendingIteration
                .clone();

        return newObject;
    }

    /**
     * Initializes the model controlled by this director. Call the initialize()
     * of super class and then wrap each actor that is controlled by this
     * director.
     *
     * This method should typically be invoked once per execution, after the
     * preinitialization phase, but before any iteration. It may be invoked in
     * the middle of an execution, if reinitialization is desired.
     *
     * This method is <i>not</i> synchronized on the workspace, so the caller
     * should be.
     *
     * @exception IllegalActionException
     *                If the initialize() method of one of the associated actors
     *                throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        Nameable container = getContainer();

        // In the actor library, the container might be an moml.EntityLibrary.
        if (container instanceof CompositeActor) {
            Iterator<?> actors = ((CompositeActor) container).deepEntityList()
                    .iterator();

            _actorDictionary.clear();
            _pendingIteration.clear();
            while (actors.hasNext()) {
                Actor actor = (Actor) actors.next();
                if (actor instanceof GetFirable) {
                    _actorDictionary.put(actor.getFullName(),
                            new ResumableFire(actor));
                }
                //                else {
                //                    _actorDictionary.put(actor.getFullName(),
                //                            new NonBlockingFire(actor));
                //                }
                _pendingIteration.put(actor.getFullName(), 0);
            }
        }

        _events = new ArrayList<Event.Builder>();

        actorList = new ArrayList<Actor>();

    }

    /**
     * Requests the execution of the current iteration to stop. This is similar
     * to stopFire(), except that the current iteration is not allowed to
     * complete.
     *
     */
    @Override
    public void stop() {
        for (FireMachine firing : _actorDictionary.values()) {
            if (firing.getState() == FireMachine.State.PROCESS) {
                firing.reset();
            }
        }
        // super.stop();
        System.out.println(this.getFullName() + " stops!");
    }

    /**
     * Option parameter whether trace info is printed out.
     */
    public Parameter printTrace;

    /**
     * Returns the actor that is about to fire and its state.
     *
     * @return 0 if firing can be executed, and the next event in event queue
     *         should be checked for processing; -1 if there's no actor to fire,
     *         and we should not keep firing; 1 if there's no actor to fire, but
     *         the next event should be checked for processing.
     * @exception IllegalActionException
     *                If the firing actor throws it, or event queue is not
     *                ready, or an event is missed, or time is set backwards.
     */
    protected Pair<Actor, Integer> _checkNextActorToFire()
            throws IllegalActionException {
        // Find the next actor to be fired.
        Actor actorToFire = _getNextActorToFire();

        // Check whether the actor to be fired is null.
        // -- If the actor to be fired is null,
        // There are two conditions that the actor to be fired
        // can be null.
        if (actorToFire == null) {
            if (_isTopLevel()) {
                // Case 1:
                // If this director is an executive director at
                // the top level, a null actor means that there are
                // no events in the event queue.
                if (_debugging) {
                    _debug("No more events in the event queue.");
                }

                // Setting the following variable to true makes the
                // postfire method return false.
                // Do not do this if _stopFireRequested is true,
                // since there may in fact be actors to fire, but
                // their firing has been deferred.
                //                if (!_stopFireRequested) {
                //                    _noMoreActorsToFire = true;
                //                }
            } else {
                // Case 2:
                // If this director belongs to an opaque composite model,
                // which is not at the top level, the director may be
                // invoked by an update of an external parameter port.
                // Therefore, no actors contained by the composite model
                // need to be fired.
                // NOTE: There may still be events in the event queue
                // of this director that are scheduled for future firings.
                if (_debugging) {
                    _debug("No actor requests to be fired "
                            + "at the current tag.");
                }
            }
            // Nothing more needs to be done in the current iteration.
            // Simply return.
            // Since we are now actually stopping the firing, we can set this false.
            _stopFireRequested = false;
            // FIXME: FindBugs: Load of known null value.
            return new Pair(actorToFire, -1);
        }

        // NOTE: Here we used to check to see whether
        // the actor to be fired is the container of this director,
        // and if so, return to give the outside domain a chance to react
        // to that event. This strategy assumed that the
        // topological sort would always assign the composite actor the
        // lowest priority, which would guarantee that all the inside actors
        // have fired (reacted to their triggers) before the composite
        // actor is what is returned. However, the priority no longer
        // seems to always be lower. A better strategy is to continue
        // firing until we have exhausted all events with the current
        // tag and microstep.
        if (actorToFire == getContainer()) {
            /* What we used to do (before 5/17/09):
            // Since we are now actually stopping the firing, we can set this false.
            _stopFireRequested = false;
            return;
             */
            return new Pair(actorToFire, 1);
        }

        if (_debugging) {
            _debug("****** Actor to fire: " + actorToFire.getFullName());
        }

        // Keep firing the actor to be fired until there are no more input
        // tokens available in any of its input ports with the same tag, or its prefire()
        // method returns false.
        // boolean refire;

        // Liangpeng: Ruling out the refiring would simplify the MetroII event proposing
        // do {
        // refire = false;

        // NOTE: There are enough tests here against the
        // _debugging variable that it makes sense to split
        // into two duplicate versions.
        if (_debugging) {
            // Debugging. Report everything.
            // If the actor to be fired is not contained by the container,
            // it may just be deleted. Put this actor to the
            // list of disabled actors.
            if (!((CompositeEntity) getContainer())
                    .deepContains((NamedObj) actorToFire)) {
                _debug("Actor no longer under the control of this director. Disabling actor.");
                _disableActor(actorToFire);
                return new Pair(null, 0);
            }

            _debug(new FiringEvent(this, actorToFire,
                    FiringEvent.BEFORE_PREFIRE));

            if (!actorToFire.prefire()) {
                _debug("*** Prefire returned false.");
                return new Pair(null, 0);
            }

            _debug(new FiringEvent(this, actorToFire, FiringEvent.AFTER_PREFIRE));

            _debug(new FiringEvent(this, actorToFire, FiringEvent.BEFORE_FIRE));

            return new Pair(actorToFire, 0);

            //                if (actorToFire instanceof MetroIIEventHandler) {
            //                    ((MetroIIEventHandler) actorToFire).getfire(resultHandler);
            //                } else {
            //                    actorToFire.fire();
            //                }

            //actorToFire.fire();

            //                _debug(new FiringEvent(this, actorToFire,
            //                        FiringEvent.AFTER_FIRE));
            //
            //                _debug(new FiringEvent(this, actorToFire,
            //                        FiringEvent.BEFORE_POSTFIRE));
            //
            //                if (!actorToFire.postfire()) {
            //                    _debug("*** Postfire returned false:",
            //                            ((Nameable) actorToFire).getName());
            //
            //                    // This actor requests not to be fired again.
            //                    _disableActor(actorToFire);
            //                    return new Pair(null, 0);
            //                }
            //
            //                _debug(new FiringEvent(this, actorToFire,
            //                        FiringEvent.AFTER_POSTFIRE));
        } else {
            // No debugging.
            // If the actor to be fired is not contained by the container,
            // it may just be deleted. Put this actor to the
            // list of disabled actors.
            if (!((CompositeEntity) getContainer())
                    .deepContains((NamedObj) actorToFire)) {
                _disableActor(actorToFire);
                return new Pair(null, 0);
            }

            if (!actorToFire.prefire()) {
                return new Pair(null, 0);
            }

            return new Pair(actorToFire, 0);
            //                Event.Builder eb = _makeEventBuilder(actorToFire.getFullName(),
            //                        Event.Type.BEGIN);
            //                events.add(eb);
            //                resultHandler.handleResult(events);
            //                events.remove(events.size() - 1);
            //
            //                if (actorToFire instanceof MetroIIEventHandler) {
            //                    ((MetroIIEventHandler) actorToFire).getfire(resultHandler);
            //                } else {
            //                    actorToFire.fire();
            //                }

            // actorToFire.fire();

            // NOTE: It is the fact that we postfire actors now that makes
            // this director not comply with the actor abstract semantics.
            // However, it's quite a redesign to make it comply, and the
            // semantics would not be backward compatible. It really needs
            // to be a new director to comply.
            //                if (!actorToFire.postfire()) {
            //                    // This actor requests not to be fired again.
            //                    _disableActor(actorToFire);
            //                    return new Pair(actorToFire, 0);
            //                }
        }

        // Check all the input ports of the actor to see whether there
        // are more input tokens to be processed.
        // FIXME: This particular situation can only occur if either the
        // actor failed to consume a token, or multiple
        // events with the same destination were queued with the same tag.
        // In theory, both are errors. One possible fix for the latter
        // case would be to requeue the token with a larger microstep.
        // A possible fix for the former (if we can detect it) would
        // be to throw an exception. This would be far better than
        // going into an infinite loop.
        //            Iterator<?> inputPorts = actorToFire.inputPortList().iterator();
        //
        //            while (inputPorts.hasNext() && !refire) {
        //                IOPort port = (IOPort) inputPorts.next();
        //
        //                // iterate all the channels of the current input port.
        //                for (int i = 0; i < port.getWidth(); i++) {
        //                    if (port.hasToken(i)) {
        //                        refire = true;
        //
        //                        // Found a channel that has input data,
        //                        // jump out of the for loop.
        //                        break;
        //                    }
        //                }
        //            }
        //
        //            if (refire) {
        //                throw new IllegalActionException(
        //                        "Refiring actor is not allowed in MetroII version!");
        //            }
        //
        //            // } while (refire); // close the do {...} while () loop
        //
        //            // NOTE: On the above, it would be nice to be able to
        //            // check _stopFireRequested, but this doesn't actually work.
        //            // In particular, firing an actor may trigger a call to stopFire(),
        //            // for example if the actor makes a change request, as for example
        //            // an FSM actor will do.  This will prevent subsequent firings,
        //            // incorrectly.
        //            return new Pair(actorToFire, 0);
        //

    }

    /**
     * Enforces a firing of a DE director only handles events with the same tag.
     * Checks what is the model time of the earliest event in the event queue.
     *
     * @return true if the earliest event in the event queue is at the same
     *         model time as the event that was just processed. Else if that
     *         event's timestamp is in the future, return false.
     * @exception IllegalActionException
     *                If model time is set backwards.
     */
    @Override
    protected boolean _checkForNextEvent() throws IllegalActionException {
        // The following code enforces that a firing of a
        // DE director only handles events with the same tag.
        // If the earliest event in the event queue is in the future,
        // this code terminates the current iteration.
        // This code is applied on both embedded and top-level directors.
        synchronized (_eventQueue) {
            if (!_eventQueue.isEmpty()) {
                DEEvent next = _eventQueue.get();

                if (next.timeStamp().compareTo(getModelTime()) > 0) {
                    // If the next event is in the future time,
                    // jump out of the big while loop in fire() and
                    // proceed to postfire().
                    return false;
                } else if (next.timeStamp().compareTo(getModelTime()) < 0
                        || next.microstep() < _microstep) {
                    throw new IllegalActionException(
                            "The tag of the next event (" + next.timeStamp()
                            + "." + next.microstep()
                            + ") can not be less than"
                            + " the current tag (" + getModelTime()
                            + "." + _microstep + ") !");
                } else {
                    // The next event has the same tag as the current tag,
                    // indicating that at least one actor is going to be
                    // fired at the current iteration.
                    // Continue the current iteration.
                }
            }
        }
        return true;
    }

    /**
     * Processes the mappable actors. The assumption is that a mappable actor has
     * a delay strictly greater than zero.
     *
     * @exception IllegalActionException
     * @exception CollectionAbortedException
     */
    void processMappableActorEventsUntil(
            ResultHandler<Iterable<Event.Builder>> resultHandler,
            Event.Builder event) throws IllegalActionException,
            CollectionAbortedException {

        assert event.getStatus() == Event.Status.PROPOSED;
        do {
            _events.clear();

            ArrayList<Actor> firingActorList = new ArrayList<Actor>();
            for (Actor actor : actorList) {
                FireMachine firing = _actorDictionary.get(actor.getFullName());
                LinkedList<Event.Builder> metroIIEventList = new LinkedList<Event.Builder>();
                firing.startOrResume(metroIIEventList);

                // Check if the actor has reached the end of postfire()
                if (firing.getState() == FireMachine.State.FINAL) {
                    // The actor has reached the end of postfire()
                    //FIXME: the debugging info is late
                    if (_debugging) {
                        _debug(new FiringEvent(this, actor,
                                FiringEvent.AFTER_FIRE));
                    }
                    if (_debugging) {
                        _debug(new FiringEvent(this, actor,
                                FiringEvent.BEFORE_POSTFIRE));
                    }

                    firing.actor().postfire();
                    firing.reset();

                    if (_debugging) {
                        _debug(new FiringEvent(this, actor,
                                FiringEvent.AFTER_POSTFIRE));
                    }

                    firing.actor().inputPortList().iterator();

                    if (_pendingIteration.get(actor.getFullName()) > 0) {
                        assert actor.prefire();
                        firing.startOrResume(metroIIEventList);
                        firingActorList.add(actor);
                        _events.addAll(metroIIEventList);
                        _pendingIteration.put(actor.getFullName(),
                                _pendingIteration.get(actor.getFullName()) - 1);
                    }

                } else {
                    firingActorList.add(actor);
                    _events.addAll(metroIIEventList);
                }
            }

            _events.add(event);
            resultHandler.handleResult(_events);

            actorList = firingActorList;
        } while (event.getStatus() != Event.Status.NOTIFIED);

    }

    /**
     * Fires actors according to events in the event queue. Whether the actual
     * firing of an actor can be done also depend on the MetroII events
     * associated with the actor if the actor is a MetroII actor. Only when the
     * associated MetroII event is NOTIFIED, the firing can be executed.
     * Otherwise the state of the MetroII event will be checked again along with
     * 'the next event'. Note that 'the next event' is the next event globally,
     * which could be either the next event in the event queue of
     * MetroIIDEDirector or the event in architectural model which this model is
     * mapped to.
     *
     * The time advancing is via proposing a MetroII event with time tag as its
     * quantity (@see TimeScheduler).
     *
     * @exception IllegalActionException
     *                If we couldn't process an event or if an event of smaller
     *                timestamp is found within the event queue.
     */
    @Override
    public void getfire(ResultHandler<Iterable<Event.Builder>> resultHandler)
            throws CollectionAbortedException, IllegalActionException {
        //        try {
        if (_debugging) {
            _debug("========= " + this.getName() + " director fires at "
                    + getModelTime() + "  with microstep as " + _microstep);
        }
        if (((BooleanToken) printTrace.getToken()).booleanValue()) {
            System.out.println("========= " + this.getName()
                    + " director fires at " + getModelTime()
                    + "  with microstep as " + _microstep);
        }

        Event.Builder idleEvent = MetroIIEventBuilder.newProposedEvent(
                getFullName() + ".Idle", Long.MAX_VALUE, getTimeResolution());

        Event.Builder beforeFunctionEvent = MetroIIEventBuilder
                .newProposedEvent(getFullName() + ".beforeFunctionEvent",
                        getModelTime().getLongValue(), getTimeResolution());

        processMappableActorEventsUntil(resultHandler, beforeFunctionEvent);

        // NOTE: This fire method does not call super.fire()
        // because this method is very different from that of the super class.
        // A while loop that handles all events with the same tag.
        while (_checkForNextEvent()) {
            if (((BooleanToken) printTrace.getToken()).booleanValue()) {
                System.out.println("Before checking actor Time: "
                        + this.getModelTime() + " " + this.getMicrostep());
            }
            Pair<Actor, Integer> actorAndState = _checkNextActorToFire();
            if (((BooleanToken) printTrace.getToken()).booleanValue()) {
                System.out.println("After checking actor Time: "
                        + this.getModelTime() + " " + this.getMicrostep());
            }
            // System.out.println(_eventQueue);
            int result = actorAndState.getSecond();

            if (actorAndState.getFirst() != null
                    && !(actorAndState.getFirst() instanceof GetFirable)) {

                Actor actorToFire = actorAndState.getFirst();
                boolean refire;

                do {
                    refire = false;

                    // NOTE: There are enough tests here against the
                    // _debugging variable that it makes sense to split
                    // into two duplicate versions.
                    if (_debugging) {
                        // Debugging. Report everything.
                        // If the actor to be fired is not contained by the container,
                        // it may just be deleted. Put this actor to the
                        // list of disabled actors.
                        if (!((CompositeEntity) getContainer())
                                .deepContains((NamedObj) actorToFire)) {
                            _debug("Actor no longer under the control of this director. Disabling actor.");
                            _disableActor(actorToFire);
                            break;
                        }

                        _debug(new FiringEvent(this, actorToFire,
                                FiringEvent.BEFORE_PREFIRE));

                        //
                        //                                if (!actorToFire.prefire()) {
                        //                                    _debug("*** Prefire returned false.");
                        //                                    break;
                        //                                }

                        _debug(new FiringEvent(this, actorToFire,
                                FiringEvent.AFTER_PREFIRE));

                        _debug(new FiringEvent(this, actorToFire,
                                FiringEvent.BEFORE_FIRE));
                        actorToFire.fire();
                        _debug(new FiringEvent(this, actorToFire,
                                FiringEvent.AFTER_FIRE));

                        _debug(new FiringEvent(this, actorToFire,
                                FiringEvent.BEFORE_POSTFIRE));

                        if (!actorToFire.postfire()) {

                            _debug("*** Postfire returned false:",
                                    ((Nameable) actorToFire).getName());

                            // This actor requests not to be fired again.
                            _disableActor(actorToFire);
                            break;
                        }

                        _debug(new FiringEvent(this, actorToFire,
                                FiringEvent.AFTER_POSTFIRE));
                    } else {
                        // No debugging.
                        // If the actor to be fired is not contained by the container,
                        // it may just be deleted. Put this actor to the
                        // list of disabled actors.
                        if (!((CompositeEntity) getContainer())
                                .deepContains((NamedObj) actorToFire)) {

                            _disableActor(actorToFire);
                            break;
                        }

                        //                                if (!actorToFire.prefire()) {
                        //                                    break;
                        //                                }

                        if (((BooleanToken) printTrace.getToken())
                                .booleanValue()) {
                            System.out.println("Fire actor: "
                                    + actorAndState.getFirst().getFullName()
                                    + " " + this.getModelTime() + " "
                                    + this.getMicrostep());
                        }
                        actorToFire.fire();

                        // NOTE: It is the fact that we postfire actors now that makes
                        // this director not comply with the actor abstract semantics.

                        // However, it's quite a redesign to make it comply, and the
                        // semantics would not be backward compatible. It really needs
                        // to be a new director to comply.
                        if (!actorToFire.postfire()) {
                            // This actor requests not to be fired again.
                            _disableActor(actorToFire);
                            break;
                        }
                    }

                    // Check all the input ports of the actor to see whether there
                    // are more input tokens to be processed.
                    // FIXME: This particular situation can only occur if either the
                    // actor failed to consume a token, or multiple
                    // events with the same destination were queued with the same tag.
                    // In theory, both are errors. One possible fix for the latter
                    // case would be to requeue the token with a larger microstep.
                    // A possible fix for the former (if we can detect it) would

                    // be to throw an exception. This would be far better than
                    // going into an infinite loop.
                    Iterator<?> inputPorts = actorToFire.inputPortList()
                            .iterator();

                    while (inputPorts.hasNext() && !refire) {
                        IOPort port = (IOPort) inputPorts.next();

                        // iterate all the channels of the current input port.
                        for (int i = 0; i < port.getWidth(); i++) {
                            if (port.hasToken(i)) {
                                if (_debugging) {
                                    _debug("Port named " + port.getName()
                                            + " still has input on channel "
                                            + i + ". Refire the actor.");
                                }
                                // refire only if can be scheduled.
                                if (!_aspectsPresent
                                        || _schedule((NamedObj) actorToFire,
                                                getModelTime())) {
                                    refire = true;

                                    // Found a channel that has input data,
                                    // jump out of the for loop.
                                    break;
                                }
                            }
                        }
                    }
                } while (refire); // close the do {...} while () loop

                //                        actorAndState.getFirst().fire();
                //                        actorAndState.getFirst().postfire();
                continue;
            }

            assert result <= 1 && result >= -1;
            if (result == 1) {
                continue;
            } else if (result == -1) {
                _noActorToFire();
                break;
                // return;
            } // else if 0, keep executing
            //if (!actorList.contains(actorAndState.first)) {
            if (actorAndState.getFirst() != null) {
                // System.out.println(_eventQueue);
                Actor actor = actorAndState.getFirst();
                FireMachine firing = _actorDictionary.get(actor.getFullName());
                if (firing.getState() != FireMachine.State.START) {
                    //                            _pendingIteration
                    //                                    .put(actor.getFullName(), _pendingIteration
                    //                                            .get(actor.getFullName()) + 1);
                } else {
                    actorList.add(actorAndState.getFirst());

                    if (((BooleanToken) printTrace.getToken()).booleanValue()) {
                        System.out.println(actorAndState.getFirst()
                                .getFullName() + " is added");
                    }

                    if (((BooleanToken) printTrace.getToken()).booleanValue()) {
                        System.out.println("Before firing Time: "
                                + this.getModelTime() + this.getMicrostep());
                    }
                }
            }
            //}
            // after actor firing, the subclass may wish to perform some book keeping
            // procedures. However in this class the following method does nothing.
            _actorFired();
            if (((BooleanToken) printTrace.getToken()).booleanValue()) {
                System.out.println("After firing Time: " + this.getModelTime()
                        + this.getMicrostep());
            }

        }

        Event.Builder afterFunctionEvent = MetroIIEventBuilder
                .newProposedEvent(getFullName() + ".afterFunctionEvent",
                        getModelTime().getLongValue(), getTimeResolution());

        processMappableActorEventsUntil(resultHandler, afterFunctionEvent);

        long idleEventTimeStamp = Long.MAX_VALUE;

        if (!_eventQueue.isEmpty()
                && !_eventQueue.get().timeStamp().isNegative()) {
            idleEventTimeStamp = _eventQueue.get().timeStamp().getLongValue();
        }

        idleEvent = MetroIIEventBuilder.newProposedEvent(getFullName()
                + ".Idle", idleEventTimeStamp, getTimeResolution());

        _events.clear();
        _events.add(idleEvent);

        while (idleEvent.getStatus() != Event.Status.NOTIFIED) {
            // System.out.println(idleEvent.getTime().getValue());
            resultHandler.handleResult(_events);
        }

        long timeValue = EventTimeComparator.convert(idleEvent.getTime()
                .getValue(), idleEvent.getTime().getResolution(), this
                .getTimeResolution());

        Time tmpTime = new Time(this, timeValue);
        if (tmpTime.compareTo(this.getModelTime()) > 0) {
            // increase timeValue
            this.setModelTime(tmpTime);
            this.setIndex(0);
        } else {
            assert false;
        }

        if (_debugging) {
            _debug("MetroIIDE director fired!");
        }
        //        } catch (IllegalActionException e) {
        //            throw new CollectionAbortedException(e);
        //        }
    }

    /**
     * Since the MetroIIDEDirector is always used inside a
     * MetroIICompositeActor, the adapter() in MetroIICompositeActor is
     * responsible for creating the iterator of getfire(), this adapter() should
     * never be called.
     *
     * @return iterator
     */
    @Override
    public YieldAdapterIterable<Iterable<Builder>> adapter() {
        assert false;
        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Initializes parameters. This is called by the constructor.
     *
     * @exception IllegalActionException
     * @exception NameDuplicationException
     */
    private void _initializeParameters() throws IllegalActionException,
    NameDuplicationException {
        printTrace = new Parameter(this, "printTrace");
        printTrace.setTypeEquals(BaseType.BOOLEAN);
        printTrace.setExpression("false");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     * Lookup table for actor by MetroII event name
     */
    private Hashtable<String, Actor> _nameToActor = new Hashtable<String, Actor>();

    /**
     * The list of actors governed by MetroIIDEDirector
     */
    private Hashtable<String, FireMachine> _actorDictionary = new Hashtable<String, FireMachine>();

    /**
     * The list of current live MetroII events
     */
    private ArrayList<Event.Builder> _events = new ArrayList<Event.Builder>();

    /**
     * The list of actors that are governed by MetroIIDEDirector
     */
    private ArrayList<Actor> actorList = new ArrayList<Actor>();

    /**
     * The pending iterations for refiring actor.
     */
    private Hashtable<String, Integer> _pendingIteration = new Hashtable<String, Integer>();
}
