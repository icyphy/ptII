package ptolemy.domains.metroII.kernel;

import java.util.ArrayList;
import java.util.Hashtable;

import net.jimblackler.Utils.CollectionAbortedException;
import net.jimblackler.Utils.Collector;
import net.jimblackler.Utils.ResultHandler;
import net.jimblackler.Utils.ThreadedYieldAdapter;
import net.jimblackler.Utils.YieldAdapterIterable;
import ptolemy.actor.Actor;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event;
import ptolemy.domains.modal.kernel.FSMActor;
import ptolemy.domains.modal.kernel.FSMDirector;
import ptolemy.domains.modal.kernel.State;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public class MetroIIFSMDirector extends FSMDirector implements
        MetroIIEventHandler {

    public MetroIIFSMDirector() throws IllegalActionException,
            NameDuplicationException {
        super();
        // TODO Auto-generated constructor stub
    }

    public MetroIIFSMDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    public YieldAdapterIterable<Iterable<Event.Builder>> adapter() {
        return new ThreadedYieldAdapter<Iterable<Event.Builder>>()
                .adapt(new Collector<Iterable<Event.Builder>>() {
                    public void collect(
                            ResultHandler<Iterable<Event.Builder>> resultHandler)
                            throws CollectionAbortedException {
                        getfire(resultHandler);
                    }
                });
    }

    public ArrayList<Event.Builder> events = new ArrayList<Event.Builder>();
    public Hashtable<String, Actor> name2actor = new Hashtable<String, Actor>();

    public Event.Builder makeEventBuilder(String name, Event.Type t,
            Event.Status s) {
        Event.Builder meb = Event.newBuilder();
        meb.setName(name);
        meb.setOwner(name);
        meb.setStatus(s);
        meb.setType(t);
        return meb;
    }

    public void getfire(ResultHandler<Iterable<Event.Builder>> resultHandler)
            throws CollectionAbortedException {
        FSMActor controller = null;
        try {
            controller = getController();
        } catch (IllegalActionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (controller == null) {
        } else {
            State currentState = controller.currentState();
            if (_debugging) {
                _debug("*** Firing " + getFullName(), " at time "
                        + getModelTime());
                _debug("Current state is:", currentState.getName());
            }

            Event.Builder eb;
            do {
                eb = makeEventBuilder(currentState.getFullName(),
                        Event.Type.BEGIN, Event.Status.PROPOSED);
                events.add(eb);
                resultHandler.handleResult(events);
                events.remove(events.size() - 1);
            } while (eb.getStatus() != Event.Status.NOTIFIED);

            try {
                super.fire();
            } catch (IllegalActionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}
