package ptolemy.domains.metroII.kernel;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import net.jimblackler.Utils.CollectionAbortedException;
import net.jimblackler.Utils.Collector;
import net.jimblackler.Utils.ResultHandler;
import net.jimblackler.Utils.ThreadedYieldAdapter;
import net.jimblackler.Utils.YieldAdapterIterable;

import ptolemy.actor.Actor;
import ptolemy.actor.sched.Firing;
import ptolemy.actor.sched.FixedPointDirector;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.util.PeriodicDirector;
import ptolemy.actor.util.PeriodicDirectorHelper;
import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event;


public class MetroIISRDirector extends FixedPointDirector implements
        PeriodicDirector, MetroIIEventHandler {
    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public MetroIISRDirector() throws IllegalActionException,
            NameDuplicationException {
        super();
        _init();
    }

    /** Construct a director in the given workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace for this object.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public MetroIISRDirector(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
        _init();
    }

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *   with the specified container.
     *  @exception NameDuplicationException If the name collides with an
     *   attribute in the container.
     */
    public MetroIISRDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The time period of each iteration.  This parameter has type double
     *  and default value 0.0, which means that this director does not
     *  increment model time and does not request firings by calling
     *  fireAt() on any enclosing director.  If the value is set to
     *  something greater than 0.0, then if this director is at the
     *  top level, it will increment model time by the specified
     *  amount in its postfire() method. If it is not at the top
     *  level, then it will call fireAt() on the enclosing executive
     *  director with the argument being the current time plus the
     *  specified period.
     */
    public Parameter period;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown in this base class
     *  @return The new Attribute.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        MetroIISRDirector newObject = (MetroIISRDirector) super
                .clone(workspace);
        try {
            newObject._periodicDirectorHelper = new PeriodicDirectorHelper(
                    newObject);
        } catch (IllegalActionException e) {
            throw new CloneNotSupportedException("Failed to clone helper: " + e);
        }
        return newObject;
    }

    /** Request a firing of the given actor at the given absolute
     *  time, and return the time at which the specified will be
     *  fired. If the <i>period</i> is 0.0 and there is no enclosing
     *  director, then this method returns the current time. If
     *  the period is 0.0 and there is an enclosing director, then
     *  this method delegates to the enclosing director, returning
     *  whatever it returns. If the <i>period</i> is not 0.0, then
     *  this method checks to see whether the
     *  requested time is equal to the current time plus an integer
     *  multiple of the period. If so, it returns the requested time.
     *  If not, it returns current time plus the period.
     *  @param actor The actor scheduled to be fired.
     *  @param time The requested time.
     *  @param microstep The microstep (ignored by this director).
     *  @exception IllegalActionException If the operation is not
     *    permissible (e.g. the given time is in the past).
     *  @return Either the requested time or the current time plus the
     *  period.
     */
    public Time fireAt(Actor actor, Time time, int microstep)
            throws IllegalActionException {
        return _periodicDirectorHelper.fireAt(actor, time);
    }

    /** Return the time value of the next iteration.
     *  If this director is at the top level, then the returned value
     *  is the current time plus the period. Otherwise, this method
     *  delegates to the executive director.
     *  @return The time of the next iteration.
     * @throws IllegalActionException 
     */
    public Time getModelNextIterationTime() throws IllegalActionException {
        if (!_isTopLevel()) {
            return super.getModelNextIterationTime();
        }
        try {
            double periodValue = periodValue();

            if (periodValue > 0.0) {
                return getModelTime().add(periodValue);
            } else {
                return getModelTime();
            }
        } catch (IllegalActionException exception) {
            // This should have been caught by now.
            throw new InternalErrorException(exception);
        }
    }

    /** Initialize the director and all deeply contained actors by calling
     *  the super.initialize() method.
     *  If the <i>period</i> parameter is greater than zero, then
     *  request a first firing of the executive director, if there
     *  is one.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _periodicDirectorHelper.initialize();
        name2actor = new Hashtable();
        events = new ArrayList();

    }

    /** Return the value of the period as a double.
     *  @return The value of the period as a double.
     *  @exception IllegalActionException If the period parameter
     *   cannot be evaluated
     */
    public double periodValue() throws IllegalActionException {
        return ((DoubleToken) period.getToken()).doubleValue();
    }

    /** Invoke super.prefire(), which will synchronize to real time, if appropriate.
     *  Then if the <i>period</i> parameter is zero, return whatever the superclass
     *  returns. Otherwise, return true only if the current time of the enclosing
     *  director (if there is one) matches a multiple of the period. If the
     *  current time of the enclosing director exceeds the time at which we
     *  next expected to be invoked, then adjust that time to the least multiple
     *  of the period that either matches or exceeds the time of the enclosing
     *  director.
     *  @exception IllegalActionException If the <i>period</i>
     *   parameter cannot be evaluated.
     *  @return true If current time is appropriate for a firing.
     */
    public boolean prefire() throws IllegalActionException {
        return super.prefire() && _periodicDirectorHelper.prefire();
    }

    public Event.Builder makeEventBuilder(String name, Event.Type t,
            Event.Status s) {
        Event.Builder meb = Event.newBuilder();
        meb.setName(name);
        meb.setOwner(name);
        meb.setStatus(s);
        meb.setType(t);
        return meb;
    }

    public Hashtable<String, Actor> name2actor = new Hashtable<String, Actor>();
    public ArrayList<Event.Builder> events = new ArrayList<Event.Builder>();

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
        try {

            if (_debugging) {
                _debug("MetroIISRDirector: invoking fire().");
            }
            Schedule schedule = getScheduler().getSchedule();
            int iterationCount = 0;
            do {
                Iterator firingIterator = schedule.firingIterator();
                while (firingIterator.hasNext() && !_stopRequested) {
                    Actor actor = ((Firing) firingIterator.next()).getActor();
                    //                    if (_debugging) {
                    //                        _debug("firing ...");
                    //                        _debug(actor.getFullName());
                    //                    }
                    // If the actor has previously returned false in postfire(),
                    // do not fire it.
                    if (!_actorsFinishedExecution.contains(actor)) {
                        // check if the actor is ready to fire.
                        if (_isReadyToFire(actor)) {
                            if (_debugging) {
                                _debug(actor.getFullName());
                            }
                            Event.Builder eb = makeEventBuilder(
                                    actor.getFullName(), Event.Type.BEGIN,
                                    Event.Status.PROPOSED);
                            name2actor.put(eb.getName(), actor);
                            events.add(eb);
                            //_fireActor(actor);
                            //_actorsFired.add(actor);
                        }
                    } else {
                        // The postfire() method of this actor returned false in
                        // some previous iteration, so here, for the benefit of
                        // connected actors, we need to explicitly call the
                        // send(index, null) method of all of its output ports,
                        // which indicates that a signal is known to be absent.
                        if (_debugging) {
                            _debug("MetroIISRDirector: no longer enabled (return false in postfire): "
                                    + actor.getFullName());
                        }
                        _sendAbsentToAllUnknownOutputsOf(actor);
                    }
                }
                while (events.size() > 0) {

                    resultHandler.handleResult(events);

                    ArrayList<Event.Builder> tmp_events = new ArrayList<Event.Builder>();
                    for (Iterator<Event.Builder> it = events.iterator(); it
                            .hasNext();) {
                        Event.Builder etb = it.next();
                        if (etb.getType() == Event.Type.BEGIN
                                && etb.getStatus() == Event.Status.NOTIFIED) {
                            Actor actor = (Actor) name2actor.get(etb.getName());
                            if (_debugging) {
                                _debug("Firing " + actor.getFullName());
                            }
                            if (1==1) {
                                throw new RuntimeException("would call _fireActor here, but it is not checked in.");
                            }
                            //_fireActor(actor);
                            _actorsFired.add(actor);

                            // events.remove(events.size() - 1);
                            // Event.Builder eb = makeEventBuilder(actor.getFullName()+"_e", Event.Type.END); 
                        } else {
                            tmp_events.add(etb);
                        }
                        //		                else if (etb.getType()==Event.Type.END && etb.getStatus()==Event.Status.PROPOSED) {
                        //		                    events.remove(events.size()-1); 
                        //		                }
                    }
                    events = tmp_events;
                }
                iterationCount++;
            } while (!_hasIterationConverged() && !_stopRequested);

            if (_debugging) {
                _debug(this.getFullName() + ": Fixed point found after "
                        + iterationCount + " iterations.");
            }
        } catch (IllegalActionException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

    }

    /** Call postfire() on all contained actors that were fired on the last
     *  invocation of fire().  Return false if the model
     *  has finished executing, either by reaching the iteration limit, or if
     *  no actors in the model return true in postfire(), or if stop has
     *  been requested. This method is called only once for each iteration.
     *  Note that actors are postfired in arbitrary order.
     *  <p>
     *  If the <i>period</i> parameter is greater than 0.0, then
     *  if this director is at the top level, then increment time
     *  by the specified period, and otherwise request a refiring
     *  at the current time plus the period.
     *  @return True if the Director wants to be fired again in the
     *   future.
     *  @exception IllegalActionException If the iterations or
     *   period parameter does not contain a legal value.
     */
    public boolean postfire() throws IllegalActionException {
        // The super.postfire() method increments the superdense time index.
        boolean result = super.postfire();
        _periodicDirectorHelper.postfire();
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Initialize the object.   In this case, we give the SDFDirector a
     *  default scheduler of the class SDFScheduler, an iterations
     *  parameter and a vectorizationFactor parameter.
     */
    private void _init() throws IllegalActionException,
            NameDuplicationException {
        period = new Parameter(this, "period");
        period.setTypeEquals(BaseType.DOUBLE);
        period.setExpression("0.0");

        _periodicDirectorHelper = new PeriodicDirectorHelper(this);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Helper class supporting the <i>period</i> parameter. */
    private PeriodicDirectorHelper _periodicDirectorHelper;
}
