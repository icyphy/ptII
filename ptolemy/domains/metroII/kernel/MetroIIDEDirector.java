package ptolemy.domains.metroII.kernel;

import java.util.ArrayList;

import net.jimblackler.Utils.CollectionAbortedException;
import net.jimblackler.Utils.ResultHandler;
import net.jimblackler.Utils.YieldAdapterIterable;

import ptolemy.actor.Actor;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event.Builder;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public class MetroIIDEDirector extends DEDirector implements
        MetroIIEventHandler {
    public MetroIIDEDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        // TODO Auto-generated constructor stub
    }

    public ArrayList<Event.Builder> events = new ArrayList<Event.Builder>();

    protected int _fire(ResultHandler<Iterable<Event.Builder>> resultHandler)
            throws CollectionAbortedException {
        try {
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
                    if (!_stopFireRequested) {
                        _noMoreActorsToFire = true;
                    }
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
                return -1;
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
                return 1;
            }

            if (_debugging) {
                _debug("****** Actor to fire: " + actorToFire.getFullName());
            }

            // Keep firing the actor to be fired until there are no more input
            // tokens available in any of its input ports with the same tag, or its prefire()
            // method returns false.
            boolean refire;

            do {
                refire = false;

                // NOTE: There are enough tests here against the
                // _debugging variable that it makes sense to split
                // into two duplicate versions.
                //                if (_debugging) {
                //                    // Debugging. Report everything.
                //                    // If the actor to be fired is not contained by the container,
                //                    // it may just be deleted. Put this actor to the
                //                    // list of disabled actors.
                //                    if (!((CompositeEntity) getContainer())
                //                            .deepContains((NamedObj) actorToFire)) {
                //                        _debug("Actor no longer under the control of this director. Disabling actor.");
                //                        _disableActor(actorToFire);
                //                        break;
                //                    }
                //
                //                    _debug(new FiringEvent(this, actorToFire,
                //                            FiringEvent.BEFORE_PREFIRE));
                //
                //                    if (!actorToFire.prefire()) {
                //                        _debug("*** Prefire returned false.");
                //                        break;
                //                    }
                //
                //                    _debug(new FiringEvent(this, actorToFire,
                //                            FiringEvent.AFTER_PREFIRE));
                //
                //                    _debug(new FiringEvent(this, actorToFire,
                //                            FiringEvent.BEFORE_FIRE));
                //
                //                    Event.Builder eb = makeEventBuilder(
                //                            actorToFire.getFullName(), Event.Type.BEGIN);
                //                    events.add(eb);
                //                    resultHandler.handleResult(events);
                //                    events.remove(events.size() - 1);
                //
                //                    if (actorToFire instanceof MetroIIEventHandler) {
                //                        ((MetroIIEventHandler) actorToFire).getfire(resultHandler);
                //                    } else {
                //                        actorToFire.fire();
                //                    }
                //
                //                    //actorToFire.fire();
                //
                //                    _debug(new FiringEvent(this, actorToFire,
                //                            FiringEvent.AFTER_FIRE));
                //
                //                    _debug(new FiringEvent(this, actorToFire,
                //                            FiringEvent.BEFORE_POSTFIRE));
                //
                //                    if (!actorToFire.postfire()) {
                //                        _debug("*** Postfire returned false:",
                //                                ((Nameable) actorToFire).getName());
                //
                //                        // This actor requests not to be fired again.
                //                        _disableActor(actorToFire);
                //                        break;
                //                    }
                //
                //                    _debug(new FiringEvent(this, actorToFire,
                //                            FiringEvent.AFTER_POSTFIRE));
                //                } else {
                //                    // No debugging.
                //                    // If the actor to be fired is not contained by the container,
                //                    // it may just be deleted. Put this actor to the
                //                    // list of disabled actors.
                //                    if (!((CompositeEntity) getContainer())
                //                            .deepContains((NamedObj) actorToFire)) {
                //                        _disableActor(actorToFire);
                //                        break;
                //                    }
                //
                //                    if (!actorToFire.prefire()) {
                //                        break;
                //                    }
                //
                //                    Event.Builder eb = makeEventBuilder(
                //                            actorToFire.getFullName(), Event.Type.BEGIN);
                //                    events.add(eb);
                //                    resultHandler.handleResult(events);
                //                    events.remove(events.size() - 1);
                //
                //                    if (actorToFire instanceof MetroIIEventHandler) {
                //                        ((MetroIIEventHandler) actorToFire).getfire(resultHandler);
                //                    } else {
                //                        actorToFire.fire();
                //                    }
                //
                //                    // actorToFire.fire();
                //
                //                    // NOTE: It is the fact that we postfire actors now that makes
                //                    // this director not comply with the actor abstract semantics.
                //                    // However, it's quite a redesign to make it comply, and the
                //                    // semantics would not be backward compatible. It really needs
                //                    // to be a new director to comply.
                //                    if (!actorToFire.postfire()) {
                //                        // This actor requests not to be fired again.
                //                        _disableActor(actorToFire);
                //                        break;
                //                    }
                //                }

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
                //                Iterator<?> inputPorts = actorToFire.inputPortList().iterator();
                //
                //                while (inputPorts.hasNext() && !refire) {
                //                    IOPort port = (IOPort) inputPorts.next();
                //
                //                    // iterate all the channels of the current input port.
                //                    for (int i = 0; i < port.getWidth(); i++) {
                //                        if (port.hasToken(i)) {
                //                            refire = true;
                //
                //                            // Found a channel that has input data,
                //                            // jump out of the for loop.
                //                            break;
                //                        }
                //                    }
                //                }
            } while (refire); // close the do {...} while () loop

        } catch (IllegalActionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // NOTE: On the above, it would be nice to be able to
        // check _stopFireRequested, but this doesn't actually work.
        // In particular, firing an actor may trigger a call to stopFire(),
        // for example if the actor makes a change request, as for example
        // an FSM actor will do.  This will prevent subsequent firings,
        // incorrectly.
        return 0;
    }

    public void getfire(ResultHandler<Iterable<Event.Builder>> resultHandler)
            throws CollectionAbortedException {
        try {
            if (_debugging) {
                _debug("========= DE director fires at " + getModelTime()
                        + "  with microstep as " + _microstep);
            }
            // NOTE: This fire method does not call super.fire()
            // because this method is very different from that of the super class.
            // A BIG while loop that handles all events with the same tag.
            while (true) {
                int result;
                result = _fire(resultHandler);
                assert (result <= 1 && result >= -1);
                if (result == 1) {
                    continue;
                } else if (result == -1) {
                    _noActorToFire();
                    return;
                } // else if 0, keep executing

                // after actor firing, the subclass may wish to perform some book keeping
                // procedures. However in this class the following method does nothing.
                _actorFired();

                if (!_checkForNextEvent()) {
                    break;
                } // else keep executing in the current iteration
            } // Close the BIG while loop.

            // Since we are now actually stopping the firing, we can set this false.
            _stopFireRequested = false;

            if (_debugging) {
                _debug("DE director fired!");
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

}
