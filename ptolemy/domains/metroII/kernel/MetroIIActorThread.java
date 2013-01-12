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
        this.actor = actor;
        this.type = type;
        this.state = state;
        this.thread = thread;
    }

    public Actor actor;
    public Type type;
    public State state;
    public YieldAdapterIterator<Iterable<Event.Builder>> thread;
}
