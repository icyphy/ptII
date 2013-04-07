package ptolemy.domains.metroII.kernel;

import java.util.LinkedList;

import net.jimblackler.Utils.YieldAdapterIterable;
import net.jimblackler.Utils.YieldAdapterIterator;

import ptolemy.actor.Actor;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event.Builder;
import ptolemy.kernel.util.IllegalActionException;

public class ResumableFire extends FireMachine {

    public ResumableFire(Actor actor) {
        super(actor);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void startOrResume(LinkedList<Builder> metroIIEventList)
            throws IllegalActionException {
        if (getStatus() == Status.START) {
            _currentStateEvent = _createMetroIIEvent("FIRE_BEGIN");
            metroIIEventList.add(_currentStateEvent);
            setStatus(Status.BEGIN);
        /**
         * Start executing the wrapped actor in the thread.
         */
        } else if (getStatus() == Status.BEGIN) {
            assert _currentStateEvent.getName().contains("FIRE_BEGIN");
            if (_currentStateEvent.getStatus() == Event.Status.NOTIFIED) {
                /* The getfire() of each Metropolis actor is invoked by a separate thread.
                 * Each thread is encapsulated by a YieldAdapterIterable, which is used to iterate
                 * the events proposed by the thread.
                 */
                final YieldAdapterIterable<Iterable<Event.Builder>> results = ((MetroIIEventHandler) actor())
                        .adapter();
                _eventIterator = results.iterator();
                setStatus(Status.PROCESS);
            } else {
                metroIIEventList.add(_currentStateEvent);
            }
        }
        /**
         * Resume executing the wrapped actor with states saved in the thread.
         */
        else if (getStatus() == Status.PROCESS) {
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
                setStatus(Status.END);
                _currentStateEvent = _createMetroIIEvent("FIRE_END");
                // metroIIEventList.add(_currentStateEvent);
            }
        } else if (getStatus() == Status.END) {
            assert _currentStateEvent.getName().contains("FIRE_END");
            if (_currentStateEvent.getStatus() == Event.Status.NOTIFIED) {
                _currentStateEvent = null; 
                setStatus(Status.FINAL);
            } else {
                metroIIEventList.add(_currentStateEvent);
            }
        } else if (getStatus() == Status.FINAL) {
            // do nothing
        } else {
            // unknown state; 
            assert false; 
        }
    }

    /**
     * Stop and dispose any associated thread.
     */
    @Override
    public void reset() {
        if (getStatus() == Status.PROCESS) {
            _eventIterator.dispose();
            actor().stop();
        }
        super.reset(); 
    }

    ///////////////////////////////////////////////////////////////////
    ////                   protected fields                        ////

    /**
     * Thread that is firing the actor
     */
    protected YieldAdapterIterator<Iterable<Event.Builder>> _eventIterator;

    @Override
    public State getState() {
        // TODO Auto-generated method stub
        assert false; 
        return null;
    }

}
