package ptolemy.domains.metroII.kernel;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import net.jimblackler.Utils.CollectionAbortedException;
import net.jimblackler.Utils.Collector;
import net.jimblackler.Utils.ResultHandler;
import net.jimblackler.Utils.ThreadedYieldAdapter;
import net.jimblackler.Utils.YieldAdapterIterable;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.Receiver;
import ptolemy.actor.process.ProcessDirector;
import ptolemy.data.IntToken;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event.Builder;
import ptolemy.domains.pn.kernel.PNDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

public class MetroIIPNDirector extends PNDirector implements
        MetroIIEventHandler {

    public List eventLock;

    public MetroIIPNDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        // TODO Auto-generated constructor stub
        eventLock = Collections.synchronizedList(new ArrayList<Object>());
    }

    private boolean _firstTimeFire;

    /** Clone the director into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        MetroIIPNDirector newObject = (MetroIIPNDirector) super
                .clone(workspace);
        newObject.eventLock = Collections
                .synchronizedList(new ArrayList<Object>());
        newObject.eventNameID = new Hashtable<String, Integer>();
        newObject.events = Collections
                .synchronizedList(new ArrayList<Event.Builder>());
        newObject._proposedThreads = Collections.synchronizedSet(new HashSet());
        return newObject;
    }

    public void initialize() throws IllegalActionException {
        super.initialize();
        _firstTimeFire = true;
    }

    public boolean prefire() throws IllegalActionException {
        if (_firstTimeFire) {
            _firstTimeFire = false;
            return super.prefire();
        }
        return true;
    }

    public Event.Builder makeEventBuilder(String name, Event.Type t) {
        Event.Builder meb = Event.newBuilder();
        meb.setName(name);
        meb.setOwner(name);
        meb.setStatus(Event.Status.PROPOSED);
        meb.setType(t);
        return meb;
    }

    Hashtable<String, Integer> eventNameID = new Hashtable<String, Integer>();

    public synchronized int eventName2Id(String event_name) {
        if (!eventNameID.containsKey(event_name)) {
            eventNameID.put(event_name, eventLock.size());
            eventLock.add(new Object());
        }
        return eventNameID.get(event_name);
    }

    public Receiver newReceiver() {
        MetroIIPNQueueReceiver receiver = new MetroIIPNQueueReceiver();
        _receivers.add(new WeakReference(receiver));

        // Set the capacity to the default. Note that it will also
        // be set in preinitialize().
        try {
            int capacity = ((IntToken) initialQueueCapacity.getToken())
                    .intValue();
            receiver.setCapacity(capacity);
        } catch (IllegalActionException e) {
            throw new InternalErrorException(e);
        }

        return receiver;
    }

    public List events = Collections
            .synchronizedList(new ArrayList<Event.Builder>());

    public synchronized void AddEvent(Event.Builder e) {
        events.add(e);
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

    // FIXME: move this decl.
    protected Set _proposedThreads = Collections.synchronizedSet(new HashSet());

    protected final synchronized int _getProposedThreadsCount() {
        return _proposedThreads.size();
    }

    @Override
    public void getfire(ResultHandler<Iterable<Builder>> resultHandler)
            throws CollectionAbortedException {
        // Don't call "Director.super.fire();" here, do the work instead.
        if (_debugging) {
            _debug("Called fire().");
        }

        Workspace workspace = workspace();

        // In case we have an enclosing process director,
        // we identify it so that we can notify it when we are blocked.
        CompositeActor container = (CompositeActor) getContainer();
        Director outsideDirector = container.getExecutiveDirector();

        if (!(outsideDirector instanceof ProcessDirector)) {
            outsideDirector = null;
        }

        int depth = 0;
        try {
            synchronized (this) {
                while (!_areThreadsDeadlocked() && !_areAllThreadsStopped()
                        && !_stopRequested) {
                    // Added to get thread to stop reliably on pushing stop button.
                    // EAL 8/05
                    if (_stopRequested) {
                        return;
                    }

                    if (_debugging) {
                        _debug("Waiting for actors to stop.");
                    }

                    try {
                        if (outsideDirector != null) {
                            ((ProcessDirector) outsideDirector).threadBlocked(
                                    Thread.currentThread(), null);
                        }
                        // NOTE: We cannot use workspace.wait(Object) here without
                        // introducing a race condition, because we have to release
                        // the lock on the _director before calling workspace.wait(_director).
                        if (depth == 0) {
                            depth = workspace.releaseReadPermission();
                        }

                        System.out.println(_getActiveThreadsCount());
                        System.out.println(_getProposedThreadsCount());
                        System.out.println(_getStoppedThreadsCount());
                        System.out.println(_getBlockedThreadsCount());
                        //System.out.println("Priority: "+getPriority() getPriority());
                        System.out.println("Before MetroIIPNDirector wait()");

                        while (!_areThreadsDeadlocked()
                                && !_areAllThreadsStopped()
                                && _getActiveThreadsCount() != _getProposedThreadsCount()
                                        + _getStoppedThreadsCount()
                                        + _getBlockedThreadsCount()) {
                            wait(1);
                        }
                        System.out.println("After MetroIIPNDirector wait()");

                        System.out.println(_getActiveThreadsCount());
                        System.out.println(_getProposedThreadsCount());
                        System.out.println(_getStoppedThreadsCount());
                        System.out.println(_getBlockedThreadsCount());

                        if (_getProposedThreadsCount()
                                + _getStoppedThreadsCount()
                                + _getBlockedThreadsCount() == 0) {
                            continue;
                        }

                        while (!_areThreadsDeadlocked()
                                && !_areAllThreadsStopped()
                                && _getActiveThreadsCount() != _getProposedThreadsCount()
                                        + _getStoppedThreadsCount()
                                        + _getBlockedThreadsCount()) {
                            wait(1);
                        }

                        System.out.println("events: " + events.size());
                        ArrayList<Event.Builder> tmp_events = new ArrayList<Event.Builder>(
                                events);
                        System.out.println("tmp_events: " + tmp_events.size());
                        events.clear();
                        resultHandler.handleResult(tmp_events);
                        for (Builder etb : tmp_events) {
                            if (etb.getStatus() == Event.Status.NOTIFIED) {
                                String event_name = etb.getName();
                                Object lock = eventLock
                                        .get(eventName2Id(event_name));
                                synchronized (lock) {
                                    lock.notifyAll();
                                    System.out.println("notify: " + event_name);
                                }

                            } else {
                                events.add(etb);
                            }
                        }

                    } catch (InterruptedException e) {
                        // stop all threads
                        stop();
                        return;
                    } finally {
                        if (outsideDirector != null) {
                            ((ProcessDirector) outsideDirector)
                                    .threadUnblocked(Thread.currentThread(),
                                            null);
                        }
                    }
                }

                if (_debugging) {
                    _debug("Actors have stopped.");
                }

                // Don't resolve deadlock if we are just pausing
                // or if a stop has been requested.
                // NOTE: Added !_stopRequested.  EAL 3/12/03.
                if (_areThreadsDeadlocked() && !_stopRequested) {
                    if (_debugging) {
                        _debug("Deadlock detected.");
                    }

                    try {
                        _notDone = _resolveDeadlock();
                    } catch (IllegalActionException e) {
                        // stop all threads.
                        stop();
                        try {
                            throw e;
                        } catch (IllegalActionException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                    }
                }
            }
        } finally {
            if (depth > 0) {
                workspace.reacquireReadPermission(depth);
            }
        }
    }
}
