package ptolemy.domains.metroII.kernel;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import ptolemy.domains.pn.kernel.PNDirector;
import ptolemy.domains.pn.kernel.PNQueueReceiver;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.EventVector;

public class MetroIIPNDirector extends PNDirector {

    public MetroIIPNDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        // TODO Auto-generated constructor stub
    }

    public Event.Builder makeEventBuilder(String name, Event.Type t) {
        Event.Builder meb = Event.newBuilder();
        meb.setName(name);
        meb.setOwner(name);
        meb.setStatus(Event.Status.PROPOSED);
        meb.setType(t);
        return meb;
    }

    String path = "/home/glp/Project/pIImetroII/metroII/";
    String pipe2server = "m2_fun_server_buffer";
    String pipe2client = "m2_fun_client_buffer";

    public void pushEvents(Hashtable events) {
        if (_debugging) {
            _debug("pushEvents:");
        }
        EventVector.Builder evb = EventVector.newBuilder();
        for (Enumeration<Hashtable> e = events.keys(); e.hasMoreElements();) {
            Event.Builder eb = (Event.Builder) events.get(e.nextElement());
            evb.addEvent(eb.build());
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
        if (_debugging) {
            for (Enumeration<Hashtable> e = events.keys(); e.hasMoreElements();) {
                Event.Builder etb = (Event.Builder) events.get(e.nextElement());
                _debug(etb.getName() + " " + etb.getStatus().toString());
            }
        }
        if (_debugging) {
            _debug("finished pushEvents");
        }

    }

    public void syncEvents(Hashtable events) {
        if (_debugging) {
            _debug("syncEvents:");
        }
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
            if (events.containsKey(e.getName())) {
                Event.Builder etb = (Event.Builder) events.get(e.getName());
                etb.setStatus(e.getStatus());
                events.put(e.getName(), etb);
            }
        }
        if (_debugging) {
            for (Enumeration<Hashtable> e = events.keys(); e.hasMoreElements();) {
                Event.Builder etb = (Event.Builder) events.get(e.nextElement());
                _debug(etb.getName() + " " + etb.getStatus().toString());
            }
        }
    }

    Hashtable name2actor = new Hashtable();
    Hashtable events = new Hashtable();
    Integer total = 2;

    public void propose(PNQueueReceiver receiver, String event_name)
            throws InterruptedException {

    }
}
