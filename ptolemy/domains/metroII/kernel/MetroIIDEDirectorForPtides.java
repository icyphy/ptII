/* MetroIIDEDirectorForPtides is an intermediate class for MetroIIPtidesDirector to extend.

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
MERCHANTABILITY AND FITNES

S FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
CALIFORNIA HAS NO OBLIGATI

ON TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
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
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event.Builder;
import ptolemy.domains.ptides.kernel.PtidesEvent;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// MetroIIDEDirectorForPtides

/**
 * MetroIIDEDirectorForPtides is an intermediate class for MetroIIPtidesDirector
 * to extend. MetroIIPtidesDirector cannot extend PtidesDirector because the
 * super class DEDirector does not support MetroII semantics. We choose to not
 * extend MetroIIDEDirector either, because all the actors under
 * MetroIIPtidesDirector can be considered as MetroII actors. The assumption
 * greatly simplifies the implementation.
 *
 * <p>
 * MetroIIDEDirectorForPtides is a much simpler version of MetroIIDEDirector
 * with the assumption that all the actors are MetroII actors.
 * </p>
 *
 * @author Liangpeng Guo
 * @version $Id: MetroIIDEDirectorForPtides.java 67634 2013-10-03 17:12:51Z glp$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (glp)
 * @Pt.AcceptedRating Red (glp)
 *
 */
public abstract class MetroIIDEDirectorForPtides extends DEDirector implements
GetFirable {

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
     *             If the director is not compatible with the specified
     *             container. May be thrown in a derived class.
     * @exception NameDuplicationException
     *             If the container is not a CompositeActor and the name
     *             collides with an entity in the container.
     */
    public MetroIIDEDirectorForPtides(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _initializeParameters();
    }

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
        MetroIIDEDirectorForPtides newObject = (MetroIIDEDirectorForPtides) super
                .clone(workspace);
        newObject._nameToActor = (Hashtable<String, Actor>) _nameToActor
                .clone();
        newObject._actorDictionary = (Hashtable<String, FireMachine>) _actorDictionary
                .clone();
        newObject._events = (ArrayList<Builder>) _events.clone();
        return newObject;
    }

    ///////////////////////////////////////////////////////////////////
    ////                    public fields                         ////

    /**
     * Initializes the model controlled by this director. Call the initialize()
     * of super class and then wrap each actor that is controlled by this
     * director.
     *
     * <p>
     * This method should typically be invoked once per execution, after the
     * preinitialization phase, but before any iteration. It may be invoked in
     * the middle of an execution, if reinitialization is desired.
     * </p>
     *
     * <p>
     * This method is <i>not</i> synchronized on the workspace, so the caller
     * should be.
     * </p>
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
            while (actors.hasNext()) {
                Actor actor = (Actor) actors.next();
                if (actor instanceof GetFirable) {
                    _actorDictionary.put(actor.getFullName(),
                            new ResumableFire(actor));
                } else {
                    _actorDictionary.put(actor.getFullName(), new BlockingFire(
                            actor));
                }
            }
        }
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
        // System.out.println(this.getFullName()+" stops!");
    }

    /**
     * Pair is a data structure used to store two elements.
     *
     * @author Liangpeng Guo
     *
     * @param <F>
     *            The first element
     * @param <S>
     *            The second element
     */
    public static class Pair<F, S> {
        /**
         * Constructs a pair of elements.
         *
         * @param first
         *            the first element.
         * @param second
         *            the second element.
         */
        public Pair(F first, S second) {
            this.first = first;
            this.second = second;
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /**
         * Sets the first element in the pair.
         *
         * @param first
         *            the element to be set.
         * @see #getFirst
         */
        public void setFirst(F first) {
            this.first = first;
        }

        /**
         * Sets the second element in the pair.
         *
         * @param second
         *            the element to be set.
         * @see #getSecond
         */
        public void setSecond(S second) {
            this.second = second;
        }

        /**
         * Gets the first element in the pair.
         *
         * @return the first element.
         * @see #setFirst
         */
        public F getFirst() {
            return first;
        }

        /**
         * Gets the second element in the pair.
         *
         * @return the second element.
         * @see #setSecond
         */
        public S getSecond() {
            return second;
        }

        ///////////////////////////////////////////////////////////////////
        ////                         private fields                    ////

        /**
         * The first element in the pair.
         */
        private F first;

        /**
         * The second element in the pair.
         */
        private S second;

    }

    ///////////////////////////////////////////////////////////////////
    ////                   protected fields                        ////

    /**
     * _checkNextEventToFire finds the next Ptides event to fire and returns it
     * with an integer indicating: 0 if firing can be executed, and the next
     * event in event queue should be checked for processing; -1 if there's no
     * actor to fire, and we should not keep firing; 1 if there's no actor to
     * fire, but the next event should be checked for processing.
     *
     * @return A pair of elements in which the first one is Ptides event safe to
     *         be processed, the second one is the state indicating: 0 if firing
     *         can be executed, and the next event in event queue should be
     *         checked for processing; -1 if there's no actor to fire, and we
     *         should not keep firing; 1 if there's no actor to fire, but the
     *         next event should be checked for processing.
     *
     * @exception IllegalActionException
     *             If the firing actor throws it, or event queue is not ready,
     *             or an event is missed, or time is set backwards.
     */
    protected Pair<PtidesEvent, Integer> _checkNextEventToFire()
            throws IllegalActionException {
        // Find the next actor to be fired.
        PtidesEvent ptidesEvent = _getNextEventToFire();
        Actor actorToFire;
        if (ptidesEvent != null) {
            actorToFire = ptidesEvent.actor();
        } else {
            actorToFire = null;
        }

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
            return new Pair(ptidesEvent, -1);
        }

        // NOTE: Here we used to check to see whether
        // the actor to be fired is the container of this director,
        // and if so, return to give the outside domain a chance to react
        // to that event. This strategy assumed that the
        // topological sort would always assign the composcannot extend PtidesDirector because ite actor the
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
            return new Pair(ptidesEvent, 1);
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
            // list of disabled actors.cannot extend PtidesDirector because
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

            return new Pair(ptidesEvent, 0);

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

            return new Pair(ptidesEvent, 0);
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
     * Fires actors according to Ptides events in the event queue. Whether the
     * actual firing of an actor can be done also depend on the MetroII events
     * associated with the actor if the actor is a MetroII actor. Only when the
     * associated MetroII event is NOTIFIED, the firing can be executed. Once a
     * MetroII event is executed, we check again if there are any more events
     * could be executed.
     *
     * getfire() starts with looking at all the Ptides events that are ready to
     * be processed. For each actor that is ready to fire, the startOrResume()
     * interface is called, which in turns calls the getfire() of the actor.
     * These calls to startOrResume() (yields) returns with the MetroII events
     * that are PROPOSED. This getfire() 'yield returns' with all these PROPOSED
     * MetroII events.
     *
     * The getfire() normally exits when no more actors proposing MetroII
     * events.
     *
     * @exception IllegalActionException
     *                If we couldn't process an event or if an event of smaller
     *                timestamp is found within the event queue.
     */
    @Override
    public void getfire(ResultHandler<Iterable<Event.Builder>> resultHandler)
            throws CollectionAbortedException, IllegalActionException {
        //try {
        if (_debugging) {
            _debug("========= " + this.getName() + " director fires at "
                    + getModelTime() + "  with microstep as " + _microstep);
        }

        _eventList = new ArrayList<PtidesEvent>();
        boolean stable = true;
        // NOTE: This fire method does not call super.fire()
        // because this method is very different from that of the super class.
        // A BIG while loop that handles all events with the same tag.
        do {
            stable = true;

            while (true) {
                Pair<PtidesEvent, Integer> eventAndState = _checkNextEventToFire();

                int result = eventAndState.second;

                assert result <= 1 && result >= -1;
                if (result == 1) {
                    continue;
                } else if (result == -1) {
                    _noActorToFire();
                    break;
                    // return;
                } // else if 0, keep executing
                  //if (!actorList.contains(actorAndState.first)) {
                if (eventAndState.first != null) {

                    _eventList.add(eventAndState.first);
                    stable = false;

                    if (((BooleanToken) printTrace.getToken()).booleanValue()) {
                        System.out.println(this.getFullName() + ": "
                                + "Logical Time " + getModelTime() + " "
                                + "READY "
                                + eventAndState.first.actor().getName());
                    }
                }
                _actorFired();

                /*
                 * _checkForNextEvent() of Ptides always returns true.
                 */

                // if (!_checkForNextEvent()) {
                //    break;
                // } // else keep executing in the current iteration
            } // Close the BIG while loop.

            ArrayList<PtidesEvent> firingEventList = new ArrayList<PtidesEvent>();
            _events.clear();
            for (PtidesEvent ptidesEvent : _eventList) {
                Actor actor = ptidesEvent.actor();

                _setLogicalTime(ptidesEvent);

                FireMachine firing = _actorDictionary.get(actor.getFullName());
                LinkedList<Event.Builder> metroIIEventList = new LinkedList<Event.Builder>();

                if (((BooleanToken) printTrace.getToken()).booleanValue()) {
                    System.out.println(this.getFullName() + ": "
                            + "Logical Time " + getModelTime() + " " + "EXEC "
                            + actor.getName());
                }

                firing.startOrResume(metroIIEventList);
                stable = false;

                if (firing.getState() == FireMachine.State.FINAL) {
                    if (_debugging) {
                        _debug(new FiringEvent(this, actor,
                                FiringEvent.BEFORE_POSTFIRE));
                    }

                    firing.actor().postfire();
                    firing.reset();

                    Iterator<?> inputPorts = firing.actor().inputPortList()
                            .iterator();
                    boolean refire = false;
                    while (inputPorts.hasNext() && !refire) {
                        IOPort port = (IOPort) inputPorts.next();

                        // iterate all the channels of the current input port.
                        for (int i = 0; i < port.getWidth(); i++) {
                            if (port.hasToken(i) && firing.actor().prefire()) {
                                refire = true;
                                firing.startOrResume(metroIIEventList);
                                firingEventList.add(ptidesEvent);
                                _events.addAll(metroIIEventList);
                                break;
                            }
                        }
                    }

                    if (_debugging) {
                        _debug(new FiringEvent(this, actor,
                                FiringEvent.AFTER_POSTFIRE));
                    }
                } else {
                    if (firing.getState() == FireMachine.State.END) {
                        if (_debugging) {
                            _debug(new FiringEvent(this, actor,
                                    FiringEvent.AFTER_FIRE));
                        }
                    }
                    firingEventList.add(ptidesEvent);
                    _events.addAll(metroIIEventList);
                }

                _resetLogicalTime();
            }
            _eventList = firingEventList;
            resultHandler.handleResult(_events);

            // FIXME: break the loop only when no more actors can be fired and no event is being fired.
        } while (!stable);
        // Since we are now actually stopping the firing, we can set this false.
        _stopFireRequested = false;

        if (_debugging) {
            _debug("MetroIIDE director fired!");
        }
        //        } catch (IllegalActionException e) {
        //            throw new CollectionAbortedException(e);
        //        }
    }

    /**
     * Since the MetroIIDEDirectorForPtides is always used inside a
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

    /**
     * Option parameter whether trace info is printed out.
     */
    public Parameter printTrace;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     *
     */
    /**
     * Ptides director need to provide the implementation.
     *
     * @return Ptides event that is safe to process
     * @exception IllegalActionException
     */
    protected abstract PtidesEvent _getNextEventToFire()
            throws IllegalActionException;

    /**
     * Ptides director need to provide the implementation.
     *
     * @param ptidesEvent
     *            Ptides event the logical time is set to.
     */
    protected abstract void _setLogicalTime(PtidesEvent ptidesEvent);

    /**
     * Reset the logical time.
     */
    protected abstract void _resetLogicalTime();

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /**
     * The list of actors governed by MetroIIDEDirector.
     */
    protected Hashtable<String, FireMachine> _actorDictionary = new Hashtable<String, FireMachine>();

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Initialize parameters. This is called by the constructor.
     *
     * @exception IllegalActionException
     * @exception NameDuplicationException
     */
    private void _initializeParameters() throws IllegalActionException,
    NameDuplicationException {
        printTrace = new Parameter(this, "printTrace");
        printTrace.setTypeEquals(BaseType.BOOLEAN);
        printTrace.setExpression("true");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     * Lookup table for actor by MetroII event name.
     */
    private Hashtable<String, Actor> _nameToActor = new Hashtable<String, Actor>();

    /**
     * The list of MetroII events that are currently being processed.
     */
    private ArrayList<Event.Builder> _events = new ArrayList<Event.Builder>();

    /**
     * The list of Ptides events that are currently being processed.
     */
    protected ArrayList<PtidesEvent> _eventList = new ArrayList<PtidesEvent>();

}
