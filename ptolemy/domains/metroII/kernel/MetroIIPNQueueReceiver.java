package ptolemy.domains.metroII.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.data.Token;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event;
import ptolemy.domains.pn.kernel.PNQueueReceiver;
import ptolemy.kernel.util.IllegalActionException;

public class MetroIIPNQueueReceiver extends PNQueueReceiver {

    /** The director in charge of this receiver. */
    protected MetroIIPNDirector _director;

    public MetroIIPNDirector getDirector() {
        return _director;
    }

    public void proposeM2Event(String suffix) throws InterruptedException {
        // Actor actor = (Actor) getContainer().getContainer();
        Thread current_thread = Thread.currentThread();
        String event_name = current_thread.getName() + suffix;
        _director.AddEvent(_director.makeEventBuilder(event_name,
                Event.Type.BEGIN));
        System.out.println("propose: " + event_name);
        Object lock = _director.eventLock.get(_director
                .eventName2Id(event_name));
        synchronized (lock) {
            _director._proposedThreads.add(current_thread);
            lock.wait();
        }
        _director._proposedThreads.remove(current_thread);
    }

    public Token get() {
        Token t = super.get();
        try {
            proposeM2Event(".get.end");
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            _terminate = true;
            e.printStackTrace();
        }
        return t;
    }

    public void put(Token token) {
        try {
            proposeM2Event(".put.begin");
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            _terminate = true;
            e.printStackTrace();
        }
        super.put(token);
    }

    public void setContainer(IOPort port) throws IllegalActionException {
        super.setContainer(port);
        if (port == null) {
            _director = null;
        } else {
            Actor actor = (Actor) port.getContainer();
            Director director;

            // For a composite actor,
            // the receiver type of an input port is decided by
            // the executive director.
            // While the receiver type of an output is decided by the director.
            // NOTE: getExecutiveDirector() and getDirector() yield the same
            // result for actors that do not contain directors.
            if (port.isInput()) {
                director = actor.getExecutiveDirector();
            } else {
                director = actor.getDirector();
            }

            if (!(director instanceof MetroIIPNDirector)) {
                throw new IllegalActionException(port,
                        "Cannot use an instance of PNQueueReceiver "
                                + "since the director is not a PNDirector.");
            }

            _director = (MetroIIPNDirector) director;
        }
    }

}
