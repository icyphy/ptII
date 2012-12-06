/**
 * 
 */
package ptolemy.domains.metroII.kernel;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import net.jimblackler.Utils.CollectionAbortedException;
import net.jimblackler.Utils.Collector;
import net.jimblackler.Utils.ResultHandler;
import net.jimblackler.Utils.ThreadedYieldAdapter;
import net.jimblackler.Utils.YieldAdapterIterable;

import ptolemy.actor.Director;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event.Builder;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.EventVector;

/**
 * @author leo
 *
 */
public class MetroIISystemCDirector extends Director implements
        MetroIIEventHandler {

    /**
     * @param container
     * @param name
     * @throws IllegalActionException
     * @throws NameDuplicationException
     */
    public MetroIISystemCDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param workspace
     * @throws NameDuplicationException 
     * @throws IllegalActionException 
     */
    public MetroIISystemCDirector(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
        // TODO Auto-generated constructor stub
    }

    String path = "../temp/";
    String pipe2server = "m2event_ptolemy_buffer";
    String pipe2client = "m2event_metro_buffer";

    public void pushEvents(Iterable<Event.Builder> events) {
        if (_debugging) {
            _debug("pushEvents:");
        }

        EventVector.Builder evb = EventVector.newBuilder();
        for (Iterator<Event.Builder> it = events.iterator(); it.hasNext();) {
            evb.addEvent(it.next().build());
        }

        EventVector ev = evb.build();
        try {
            FileOutputStream fos = new FileOutputStream(path + pipe2server);
            ev.writeTo(fos);
            fos.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("Pushing events: ");
        for (Iterator<Event.Builder> it = events.iterator(); it.hasNext();) {
            Event.Builder etb = it.next();
            System.out
                    .println(etb.getName() + " " + etb.getStatus().toString());
        }
        if (_debugging) {
            for (Iterator<Event.Builder> it = events.iterator(); it.hasNext();) {
                Event.Builder etb = it.next();
                _debug(etb.getName() + " " + etb.getStatus().toString());
            }
        }
        if (_debugging) {
            _debug("finished pushEvents");
        }

    }

    public void syncEvents(LinkedList<Event.Builder> events) {
        if (_debugging) {
            _debug("syncEvents:");
        }
        events.clear();
        EventVector ev = null;
        try {
            FileInputStream fis = new FileInputStream(path + pipe2client);
            ev = EventVector.parseFrom(fis);
            fis.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        for (Event e : ev.getEventList()) {
            events.add(e.toBuilder());
        }
        System.out.println("Sync events: ");
        for (Iterator<Event.Builder> it = events.iterator(); it.hasNext();) {
            Event.Builder etb = it.next();
            System.out
                    .println(etb.getName() + " " + etb.getStatus().toString());
        }
        if (_debugging) {
            for (Iterator<Event.Builder> it = events.iterator(); it.hasNext();) {
                Event.Builder etb = it.next();
                _debug(etb.getName() + " " + etb.getStatus().toString());
            }
        }
    }

    LinkedList<Event.Builder> events;

    public void getfire(ResultHandler<Iterable<Event.Builder>> resultHandler)
            throws CollectionAbortedException {

        syncEvents(events);

        resultHandler.handleResult(events);

        pushEvents(events);
    }

    @Override
    public YieldAdapterIterable<Iterable<Builder>> adapter() {
        return new ThreadedYieldAdapter<Iterable<Event.Builder>>()
                .adapt(new Collector<Iterable<Event.Builder>>() {
                    public void collect(
                            ResultHandler<Iterable<Event.Builder>> resultHandler)
                            throws CollectionAbortedException {
                        getfire(resultHandler);
                    }
                });
    }

    public void initialize() throws IllegalActionException {
        super.initialize();

        events = new LinkedList<Event.Builder>();
    }
}
