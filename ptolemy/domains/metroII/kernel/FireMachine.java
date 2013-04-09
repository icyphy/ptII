package ptolemy.domains.metroII.kernel;

import ptolemy.actor.Actor;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event.Builder;

public abstract class FireMachine implements StartOrResumable {
    public enum Status {
        START, BEGIN, PROCESS, END, FINAL
    }

    public FireMachine(Actor actor) {
        _actor = actor;
        _iteration = 0;
        reset();
    }

    public Status getStatus() {
        return _status;
    }

    public void setStatus(Status status) {
        _status = status;
    }

    /**
     * Dispose the current execution.
     */
    public void reset() {
        if (_iteration > 0) {
            setStatus(Status.BEGIN);
            _currentStateEvent = _createMetroIIEvent("FIRE_BEGIN");
            _iteration--; 
        }
        else {
            setStatus(Status.START);
            _currentStateEvent = null; 
            _iteration = 0;
        }
    }

    public void addIteration() {
        _iteration++;
    }

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

    protected Actor actor() {
        return _actor;
    }

    /** Current state event
    *
    */
    protected Builder _currentStateEvent;

    private Status _status;

    private Actor _actor;

    private int _iteration;
}
