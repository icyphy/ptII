package ptolemy.domains.metroII.kernel;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

import net.jimblackler.Utils.CollectionAbortedException;
import net.jimblackler.Utils.Collector;
import net.jimblackler.Utils.ResultHandler;
import net.jimblackler.Utils.ThreadedYieldAdapter;
import net.jimblackler.Utils.YieldAdapterIterable;
import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Executable;
import ptolemy.actor.FiringEvent;
import ptolemy.actor.sched.Firing;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.sched.Scheduler;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Workspace;

public class MetroSequentialSDFDirector extends SDFDirector implements
        MetroEventHandler {

    public MetroSequentialSDFDirector() throws IllegalActionException,
            NameDuplicationException {
        // TODO Auto-generated constructor stub
    }

    public MetroSequentialSDFDirector(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
        // TODO Auto-generated constructor stub
    }

    public MetroSequentialSDFDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        // TODO Auto-generated constructor stub
    }

    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        MetroSequentialSDFDirector newObject = (MetroSequentialSDFDirector) super
                .clone(workspace);
        newObject._actorDictionary = (Hashtable<String, FireMachine>) _actorDictionary
                .clone();
        newObject._pendingIteration = (Hashtable<String, Integer>) _pendingIteration
                .clone();

        return newObject;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public void initialize() throws IllegalActionException {
        super.initialize();
        Nameable container = getContainer();

        // In the actor library, the container might be an moml.EntityLibrary.
        if (container instanceof CompositeActor) {
            Iterator<?> actors = ((CompositeActor) container).deepEntityList()
                    .iterator();

            _actorDictionary.clear();
            _pendingIteration.clear();
            while (actors.hasNext()) {
                Actor actor = (Actor) actors.next();
                if (actor instanceof MetroEventHandler) {
                    _actorDictionary.put(actor.getFullName(),
                            new ResumableFire(actor));
                } else {
                    _actorDictionary.put(actor.getFullName(), new BlockingFire(
                            actor));
                }
                _pendingIteration.put(actor.getFullName(), 0);
            }
        }
    }

    /**
     * YieldAdapter interface
     */
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

    public void getfire(ResultHandler<Iterable<Event.Builder>> resultHandler)
            throws CollectionAbortedException {

        _prefire = false;
        try {
            // Don't call "super.fire();" here because if you do then
            // everything happens twice.
            Iterator firings = null;

            Scheduler scheduler = getScheduler();

            if (scheduler == null) {
                throw new IllegalActionException("Attempted to fire "
                        + "system with no scheduler");
            }

            // This will throw IllegalActionException if this director
            // does not have a container.
            Schedule schedule = scheduler.getSchedule();
            firings = schedule.firingIterator();

            Firing firing = null;
            while (firings.hasNext() && !_stopRequested) {
                firing = (Firing) firings.next();
                Actor actor = firing.getActor();

                int iterationCount = firing.getIterationCount();

                if (_debugging) {
                    _debug(new FiringEvent(this, actor,
                            FiringEvent.BEFORE_ITERATE, iterationCount));
                }
                _pendingIteration.put(actor.getFullName(), iterationCount);
                
                int returnValue = Executable.NOT_READY;
                FireMachine firingProcess = _actorDictionary.get(actor
                        .getFullName());
                while (_pendingIteration.get(actor.getFullName()) > 0) {
                    _pendingIteration.put(actor.getFullName(),
                            _pendingIteration.get(actor.getFullName()) - 1);
                    // Check if the actor has reached the end of postfire()
                    while (firingProcess.getState() != FireMachine.State.FINAL) {
                        LinkedList<Event.Builder> metroIIEventList = new LinkedList<Event.Builder>();
                        firingProcess.startOrResume(metroIIEventList);
                        resultHandler.handleResult(metroIIEventList);
                    }
                    boolean pfire = firingProcess.actor().postfire();
                    if (!pfire) {
                        returnValue = Executable.STOP_ITERATING;
                    } else {
                        returnValue = Executable.COMPLETED;
                    }
                    firingProcess.reset();

                }

                if (returnValue == STOP_ITERATING) {
                    _postfireReturns = false;
                } else if (returnValue == NOT_READY) {
                    // See de/test/auto/knownFailedTests/DESDFClockTest.xml
                    throw new IllegalActionException(
                            this,
                            actor,
                            "Actor "
                                    + "is not ready to fire.  Perhaps "
                                    + actor.getName()
                                    + ".prefire() returned false? "
                                    + "Try debugging the actor by selecting "
                                    + "\"Listen to Actor\".  Also, for SDF check moml for "
                                    + "tokenConsumptionRate on input.");
                }

                if (_debugging) {
                    _debug(new FiringEvent(this, actor,
                            FiringEvent.AFTER_ITERATE, iterationCount));
                }
            }
        } catch (IllegalActionException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    /**
     * The list of actors governed by MetroIIDEDirector
     */
    private Hashtable<String, FireMachine> _actorDictionary = new Hashtable<String, FireMachine>();

    private Hashtable<String, Integer> _pendingIteration = new Hashtable<String, Integer>();

}
