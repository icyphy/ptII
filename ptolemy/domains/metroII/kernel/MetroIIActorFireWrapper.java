package ptolemy.domains.metroII.kernel;

import java.util.LinkedList;

import net.jimblackler.Utils.YieldAdapterIterable;
import net.jimblackler.Utils.YieldAdapterIterator;

import ptolemy.actor.Actor;
import ptolemy.domains.metroII.kernel.StartOrResumable.State;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event.Builder;
import ptolemy.kernel.util.IllegalActionException;

public class MetroIIActorFireWrapper extends MetroIIActorBasicWrapper {

    public MetroIIActorFireWrapper(Actor actor) {
        super(actor);
        // TODO Auto-generated constructor stub
    }
    
    @Override
    public void startOrResume(LinkedList<Builder> metroIIEventList)
            throws IllegalActionException {
        /**
         * Start executing the wrapped actor in the thread.
         */
        if (_state == State.COMPLETE) {
            assert _currentStateEvent.getName().contains("COMPLETE");
            if (_currentStateEvent.getStatus() == Event.Status.NOTIFIED) {
                /* The getfire() of each Metropolis actor is invoked by a separate thread.
                 * Each thread is encapsulated by a YieldAdapterIterable, which is used to iterate
                 * the events proposed by the thread.
                 */
                final YieldAdapterIterable<Iterable<Event.Builder>> results = ((MetroIIEventHandler) _actor)
                        .adapter();
                _eventIterator = results.iterator();
                _state = State.ONGOING;
                _currentStateEvent = _createMetroIIEvent("ONGOING");
            }
            metroIIEventList.add(_currentStateEvent);
        }
        /**
         * Resume executing the wrapped actor with states saved in the thread.
         */
        else if (_state == State.ONGOING) {
            /* Every time hasNext() is called, the thread runs until the next event
             * is proposed. If any event is proposed, hasNext() returns true.
             * The proposed event is returned by next().
             * If the getfire() terminates without proposing event, hasNext()
             * returns false.
             */
            if (_eventIterator.hasNext()) {
                Iterable<Event.Builder> result = _eventIterator.next();
                for (Builder eventBuilder : result) {
                    // Event.Builder eventBuilder = builder;
                    eventBuilder.setStatus(Event.Status.PROPOSED);
                    metroIIEventList.add(eventBuilder);
                }
            } else {
                _state = State.COMPLETE;
                _currentStateEvent = _createMetroIIEvent("COMPLETE");
                metroIIEventList.add(_currentStateEvent);
            }
        }
    }
    
    public boolean prefire () throws IllegalActionException {
        return _actor.prefire(); 
    }

    public boolean postfire () throws IllegalActionException {
        return _actor.postfire(); 
    }

    /**
     * Stop and dispose any associated thread.
     */
    @Override
    public void reset() {
        if (_state == State.ONGOING) {
            _eventIterator.dispose();
            _actor.stop();
        }
        super.reset();
    }

    ///////////////////////////////////////////////////////////////////
    ////                   protected fields                        ////

    /**
     * Thread that is firing the actor
     */
    protected YieldAdapterIterator<Iterable<Event.Builder>> _eventIterator;

}
