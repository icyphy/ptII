package ptolemy.domains.metroII.kernel;

import ptolemy.actor.Actor;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event.Builder;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event.Status;

public abstract class FireMachine implements StartOrResumable {

    public enum State {
        START, BEGIN, PROCESS, END, FINAL
    }
    
    public FireMachine(Actor actor) {
        _actor = actor;
        _BeginEvent = _createMetroIIEvent("FIRE_BEGIN"); 
        _ProcessEvent = _createMetroIIEvent("PROCESS"); 
        _EndEvent = _createMetroIIEvent("FIRE_END"); 
        reset();
    }

    /**
     * Dispose the current execution.
     */
    public void reset() {
        setState(State.START);
    }
    
    public Builder getCurrentStateEvent() {
        switch (getCurrentState()) {
        case BEGIN: 
            return _BeginEvent; 
        case PROCESS:
            return _ProcessEvent;
        case END:
            return _EndEvent;
        default:
            return null; 
        }
    }
    
    public Builder proposeCurrentStateEvent() {
        Builder event = getCurrentStateEvent(); 
        event.setStatus(Status.PROPOSED); 
        return event; 
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

    public void setState(State state) {
        _state = state; 
    }
    
    public State getCurrentState() {
        return _state; 
    }
    
    /** Current state event
    *
    */
    final private Builder _BeginEvent;
    
    final private Builder _ProcessEvent;
    
    final private Builder _EndEvent;

    private Actor _actor;

    private State _state; 
}
