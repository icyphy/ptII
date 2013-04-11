/*
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 1995-2013 The Regents of the University of California.
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

public class MetroIIDEDirectorForPtides extends DEDirector implements
        MetroIIEventHandler {

    public MetroIIDEDirectorForPtides(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        // TODO Auto-generated constructor stub
        _initializeParameters();
    }

    /** Clone the object into the specified workspace. The new object
     *  is <i>not</i> added to the directory of that workspace (you
     *  must do this yourself if you want it there).
     *
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown in this base class
     *  @return The new Attribute.
     */
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
                if (actor instanceof MetroIIEventHandler) {
                    _actorDictionary.put(actor.getFullName(),
                            new ResumableFire(actor));
                } else {
                    _actorDictionary.put(actor.getFullName(),
                            new BlockingFire(actor));
                }
            }
        }

    }

    public class Pair<F, S> {
        private F first; //first member of pair
        private S second; //second member of pair

        public Pair(F first, S second) {
            this.first = first;
            this.second = second;
        }

        public void setFirst(F first) {
            this.first = first;
        }

        public void setSecond(S second) {
            this.second = second;
        }

        public F getFirst() {
            return first;
        }

        public S getSecond() {
            return second;
        }
    }

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




    //    public int getFiringEventSize() {
    //        return _eventList.size(); 
    //    }

    public void getfire(ResultHandler<Iterable<Event.Builder>> resultHandler)
            throws CollectionAbortedException {
        try {
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

                //if (((BooleanToken) printTrace.getToken()).booleanValue()) {
                //    System.out.println(this.getFullName() + ": " + "Time "
                //            + this.getModelTime());
                //}

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

                        if (((BooleanToken) printTrace.getToken())
                                .booleanValue()) {
                            System.out.println(this.getFullName() + ": "
                                    + "Logical Time " + getModelTime() + " "
                                    + "READY "
                                    + eventAndState.first.actor().getName());
                        }
                    }
                    //}
                    // after actor firing, the subclass may wish to perform some book keeping
                    // procedures. However in this class the following method does nothing.
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

                    FireMachine firing = _actorDictionary
                            .get(actor.getFullName());
                    LinkedList<Event.Builder> metroIIEventList = new LinkedList<Event.Builder>();

                    if (((BooleanToken) printTrace.getToken()).booleanValue()) {
                        System.out.println(this.getFullName() + ": "
                                + "Logical Time " + getModelTime() + " "
                                + "EXEC " + actor.getName());
                    }

                    firing.startOrResume(metroIIEventList);
                    stable = false;

                    if (firing.getCurrentState() == FireMachine.State.FINAL) {
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
                    } else {
                        if (firing.getCurrentState() == FireMachine.State.END) {
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
        } catch (IllegalActionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public YieldAdapterIterable<Iterable<Builder>> adapter() {
        // TODO Auto-generated method stub
        return null;
    }

    public Parameter printTrace;

    ///////////////////////////////////////////////////////////////////
    ////                       protected methods                   ////

    protected PtidesEvent _getNextEventToFire() throws IllegalActionException {
        assert false;
        return null;
    }

    protected void _setLogicalTime(PtidesEvent ptidesEvent) {
        assert false;
    }

    protected void _resetLogicalTime() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected variables                 ////

    /**
     * The list of actors governed by MetroIIDEDirector
     */
    protected Hashtable<String, FireMachine> _actorDictionary = new Hashtable<String, FireMachine>();

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Initialize parameters. This is called by the constructor.
     *  @exception IllegalActionException
     *  @exception NameDuplicationException
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
     * Lookup table for actor by MetroII event name
     */
    private Hashtable<String, Actor> _nameToActor = new Hashtable<String, Actor>();

    private ArrayList<Event.Builder> _events = new ArrayList<Event.Builder>();

    protected ArrayList<PtidesEvent> _eventList = new ArrayList<PtidesEvent>();

    // private Map<IOPort, Map<IOPort, Boolean>> _causalityPortTable = new HashMap<IOPort, Map<IOPort, Boolean>>();
}
