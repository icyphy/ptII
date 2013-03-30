package ptolemy.domains.metroII.kernel;

import java.util.LinkedList;

import ptolemy.actor.Actor;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event.Builder;
import ptolemy.kernel.util.IllegalActionException;

public class MetroIIAtomicFireActor implements StartOrResumable {

    /** Construct a basic wrapper.
    *
    * @param actor The actor
    */
    public MetroIIAtomicFireActor(Actor actor) {
        this._actor = actor;
        reset();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Dispose the current execution.
     */
    public void reset() {
        _state = State.START;
        _currentStateEvent = null;
    }

    /**
    * The functions prefire(), fire() and postfire()
    * are wrapped in startOrResume() as follows:
    * <ol>
    * <li> Propose MetroII event POSTFIRE_END_PREFIRE_BEGIN and wait for
    * the event being notified</li>
    * <li> prefire() </li>
    * <li> Propose MetroII event PREFIRE_END_FIRE_BEGIN and wait for the
    * event being notified</li>
    * <li> fire() </li>
    * <li> Propose MetroII event FIRE_END_POSTFIRE_BEGIN and wait for the
    * the event being notified</li>
    * <li> postfire() </li>
    * </ol>
    * where 'wait' means checking the status of MetroII event. If notified,
    * continue execution, otherwise proposing the same event again.
    *
    * @param metroIIEventList A list of MetroII events.
    */
    @Override
    public void startOrResume(LinkedList<Builder> metroIIEventList)
            throws IllegalActionException {
        assert metroIIEventList != null;

        if (getState() == State.START) {
            _currentStateEvent = _createMetroIIEvent("FIRE_BEGIN");
            metroIIEventList.add(_currentStateEvent);
            setState(State.BEGIN);
        } else if (getState() == State.BEGIN) {
            assert _currentStateEvent.getName().contains("FIRE_BEGIN");
            if (_currentStateEvent.getStatus() == Event.Status.NOTIFIED) {
                _actor.fire();
                setState(State.END);
                _currentStateEvent = _createMetroIIEvent("FIRE_END");
                metroIIEventList.add(_currentStateEvent);
            } else {
                metroIIEventList.add(_currentStateEvent);
            }
        } else if (getState() == State.END) {
            assert _currentStateEvent.getName().contains("FIRE_END");
            if (_currentStateEvent.getStatus() == Event.Status.NOTIFIED) {
                _currentStateEvent = null; 
                setState(State.FINAL);
            } else {
                metroIIEventList.add(_currentStateEvent);
            }
        } else if (getState() == State.FINAL) {
            // do nothing
        } else {
            // unknown state; 
            assert false; 
        }
        
    }
    
    public boolean prefire() throws IllegalActionException {
        return _actor.prefire();
    }

    public boolean postfire() throws IllegalActionException {
        reset(); 
        return _actor.postfire();
    }


    /**
     * Get the current state
     */
    public State getState() {
        return _state;
    }

    ///////////////////////////////////////////////////////////////////
    ////                    protected fields                       ////

    /** Create a MetroII event
     *
     */
    protected Builder _createMetroIIEvent(String name) {
        Event.Builder builder = Event.newBuilder();
        builder.setName(_actor.getFullName() + "." + name);
        builder.setOwner(_actor.getFullName());
        builder.setStatus(Event.Status.PROPOSED);
        builder.setType(Event.Type.GENERIC);
        return builder;
    }

    protected void setState(State s) {
        _state = s;
    }

    /** Current state event
     *
     */
    protected Builder _currentStateEvent;

    /** Actor state
     *
     */
    protected State _state;

    /** Actor which is being fired
     *
     */
    protected Actor _actor;

}
