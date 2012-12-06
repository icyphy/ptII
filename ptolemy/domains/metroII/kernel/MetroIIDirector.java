package ptolemy.domains.metroII.kernel;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

import net.jimblackler.Utils.YieldAdapterIterable;
import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.Initializable;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event.Builder;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;

public class MetroIIDirector extends Director {

    public MetroIIDirector(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        _initializeParameters();
    }

    public void addInitializable(Initializable initializable) {
        super.addInitializable(initializable);
    }

    public Parameter _mapping_file_name;

    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == _mapping_file_name) {
            StringToken str_token = (StringToken) _mapping_file_name.getToken();

            if (str_token == null) {
                _mapping_file_name = null;
            } else {
                readMapping(str_token.stringValue());
                System.out.println(_mapping_constraint_solver);
            }

        } else {
            super.attributeChanged(attribute);
        }
    }

    void _init() {

        try {
            _mapping_file_name.moveToLast();
        } catch (IllegalActionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void _initializeParameters() {
        _verbose = true;
        try {
            _mapping_file_name = new Parameter(this, "mapping");
            _mapping_file_name.setTypeEquals(BaseType.STRING);
        } catch (IllegalActionException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (NameDuplicationException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    int next_avail_id = 0;
    Hashtable<String, Integer> eventname2id = new Hashtable<String, Integer>();
    MappingConstraintSolver _mapping_constraint_solver = new MappingConstraintSolver(
            100);

    public void fire() throws IllegalActionException {

        if (_debugging) {
            _debug("Director: Called fire().");
        }

        Nameable container = getContainer();

        //if (container instanceof CompositeActor) {
        Iterator<?> actors = ((CompositeActor) container).deepEntityList()
                .iterator();
        LinkedList<MetroIIActorThread> actor_thread_list = new LinkedList<MetroIIActorThread>();
        // LinkedList<Actor> active_actor_list = new LinkedList<Actor>();
        // LinkedList<Actor> actor_waiting_list = new LinkedList<Actor>();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            if (actor instanceof MetroIIEventHandler) {
                actor_thread_list.add(new MetroIIActorThread(actor,
                        MetroIIActorThread.Type.Metropolis,
                        MetroIIActorThread.State.WAITING, null));
            } else {
                actor_thread_list.add(new MetroIIActorThread(actor,
                        MetroIIActorThread.Type.Ptolemy,
                        MetroIIActorThread.State.WAITING, null));
            }
        }

        boolean stable = false;
        while (!stable && !_stopRequested) {
            LinkedList<Event.Builder> m2event_list = new LinkedList<Event.Builder>();
            stable = true;

            for (MetroIIActorThread actor_thread : actor_thread_list) {
                if (actor_thread._actor.prefire()) {
                    if (actor_thread._type == MetroIIActorThread.Type.Metropolis) {
                        if (actor_thread._state == MetroIIActorThread.State.WAITING) {
                            final YieldAdapterIterable<Iterable<Event.Builder>> results = ((MetroIIEventHandler) actor_thread._actor)
                                    .adapter();
                            actor_thread._thread = results.iterator();
                            actor_thread._state = MetroIIActorThread.State.ACTIVE;
                        }
                    } else if (actor_thread._type == MetroIIActorThread.Type.Ptolemy) {
                        actor_thread._state = MetroIIActorThread.State.ACTIVE;
                    }
                }
            }

            for (MetroIIActorThread actor_thread : actor_thread_list) {
                if (actor_thread._type == MetroIIActorThread.Type.Metropolis
                        && actor_thread._state == MetroIIActorThread.State.ACTIVE) {
                    Actor actor = actor_thread._actor;
                    Iterator<Iterable<Event.Builder>> thread = actor_thread._thread;

                    if (thread.hasNext()) {
                        Iterable<Event.Builder> result = thread.next();
                        for (Builder builder : result) {
                            Event.Builder etb = builder;
                            String event_name = etb.getName();

                            if (!eventname2id.containsKey(event_name)) {
                                eventname2id.put(event_name, next_avail_id);
                                next_avail_id++;
                            }

                            etb.setStatus(Event.Status.WAITING);

                            m2event_list.add(etb);
                            stable = false;
                        }

                    } else {
                        boolean pfire = actor.postfire();
                        actor_thread._state = MetroIIActorThread.State.WAITING;
                        if (!pfire) {
                            if (_debugging) {
                                _debug("Actor requests halt: "
                                        + ((Nameable) actor).getFullName());
                            }
                        }
                        if (_stopRequested) {
                            if (_debugging) {
                                _debug("Actor requests halt: "
                                        + ((Nameable) actor).getFullName());
                            }

                        }
                    }
                }
            }

            for (MetroIIActorThread actor_thread : actor_thread_list) {
                if (actor_thread._type == MetroIIActorThread.Type.Ptolemy
                        && actor_thread._state == MetroIIActorThread.State.ACTIVE) {
                    actor_thread._actor.fire();
                    boolean pfire = actor_thread._actor.postfire();
                    actor_thread._state = MetroIIActorThread.State.WAITING;

                    if (!pfire) {
                        if (_debugging) {
                            _debug("Actor requests halt: "
                                    + ((Nameable) actor_thread._actor)
                                            .getFullName());
                        }
                    }
                }
            }

            for (Event.Builder etb : m2event_list) {
                String event_name = etb.getName();
                _mapping_constraint_solver.presentM2Event(eventname2id
                        .get(event_name));
            }
            // System.out.println(_mapping_constraint_solver);
            System.out.println("Before mapping resolution: ");
            for (Event.Builder etb : m2event_list) {
                System.out.println(etb.getName() + " "
                        + etb.getStatus().toString());
            }
            for (Event.Builder etb : m2event_list) {
                String event_name = etb.getName();
                if (_mapping_constraint_solver.isSatisfied(eventname2id
                        .get(event_name))) {
                    etb.setStatus(Event.Status.NOTIFIED);
                }
            }
            System.out.println("After mapping resolution: ");
            for (Event.Builder etb : m2event_list) {
                System.out.println(etb.getName() + " "
                        + etb.getStatus().toString());
            }
            _mapping_constraint_solver.reset();
        }
        if (_stopRequested) {
            for (MetroIIActorThread actor_thread : actor_thread_list) {
                if (actor_thread._type == MetroIIActorThread.Type.Metropolis
                        && actor_thread._state == MetroIIActorThread.State.ACTIVE) {
                    actor_thread._thread.dispose();
                }
            }
        }
    }

    private void readMapping(String finename) {
        System.out.println(finename);
        try {
            // Open the file that is the first
            // command line parameter
            FileInputStream fstream = new FileInputStream(finename);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            //Read File Line By Line
            while ((strLine = br.readLine()) != null) {
                // Print the content on the console
                String[] actor_name_list = strLine.split(",");
                assert actor_name_list.length == 2;
                if (!eventname2id.containsKey(actor_name_list[0])) {
                    eventname2id.put(actor_name_list[0], next_avail_id);
                    next_avail_id++;
                }
                if (!eventname2id.containsKey(actor_name_list[1])) {
                    eventname2id.put(actor_name_list[1], next_avail_id);
                    next_avail_id++;
                }
                _mapping_constraint_solver.add(
                        eventname2id.get(actor_name_list[0]),
                        eventname2id.get(actor_name_list[1]));
                System.out.println(strLine);
            }
            //Close the input stream
            in.close();
        } catch (IOException e) {//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
    }

    //    public Time fireAt(Actor actor, Time time, int microstep)
    //            throws IllegalActionException {
    //        // If this director is enclosed within another model, then we
    //        // pass the request up the chain. If the enclosing executive director
    //        // respects fireAt(), this will result in the container of this
    //        // director being iterated again at the requested time. This is
    //        // reasonable, for example, in FSM, DE, SR, CT, or any domain that
    //        // can safely fire actors other than the one that requested the
    //        // refiring at the specified time.
    //        if (isEmbedded()) {
    //            CompositeActor container = (CompositeActor) getContainer();
    //            return container.getExecutiveDirector().fireAt(container, time,
    //                    microstep);
    //        }
    //        // If we are not embedded, then return a value indicating that the
    //        // fireAt() request is being ignored. If the caller is OK with that,
    //        // then no problem.
    //        // All derived classes of Director for which the fireAt() method is
    //        // useful, should override this behavior.
    //        return time;
    //    }

    public void initialize() throws IllegalActionException {
        super.initialize();
    }

    public boolean postfire() throws IllegalActionException {
        return super.postfire();
    }

    public boolean prefire() throws IllegalActionException {
        return super.prefire();
    }

    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
    }

    public boolean transferInputs(IOPort port) throws IllegalActionException {
        return super.transferInputs(port);
    }

    public boolean transferOutputs(IOPort port) throws IllegalActionException {
        return super.transferOutputs(port);
    }

    public void wrapup() throws IllegalActionException {
        super.wrapup();
    }
}
