package ptolemy.domains.metroII.kernel;

import net.jimblackler.Utils.YieldAdapterIterator;
import ptolemy.actor.Actor;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event;

public class MetroIIActorThread {
    public enum Type {
        Ptolemy, Metropolis
    }

    public enum State {
        ACTIVE, READY, WAITING
    }

    public MetroIIActorThread(Actor actor, Type type, State state,
            YieldAdapterIterator<Iterable<Event.Builder>> thread) {
        _actor = actor;
        _type = type;
        _state = state;
        _thread = thread;
    }

    public Actor _actor;
    public Type _type;
    public State _state;
    public YieldAdapterIterator<Iterable<Event.Builder>> _thread;
}
