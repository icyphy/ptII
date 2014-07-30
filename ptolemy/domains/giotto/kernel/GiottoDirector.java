/* Director for the Giotto model of computation.

 Copyright (c) 2000-2014 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.
 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY


 // NOTE: Downgraded to red due to extensive changes.  EAL
 */
package ptolemy.domains.giotto.kernel;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.Manager;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.sched.Firing;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.sched.Scheduler;
import ptolemy.actor.sched.StaticSchedulingDirector;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Decorator;
import ptolemy.kernel.util.DecoratorAttributes;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

////GiottoDirector
/**
 This class implements a director for the Giotto model of computation
 without Giotto modes. Schedules are generated according to the Giotto
 semantics. The GiottoScheduler class contains methods to compute the
 schedules. The GiottoReceiver class implements the data flow between
 actors using double-buffering.
 <p>
 If the parameter <i>synchronizeToRealTime</i> is set to <code>true</code>,
 then the director will not process events until the real time elapsed
 since the model started matches the time stamp of the event.
 This ensures that the director does not get ahead of real time,
 but, of course, it does not ensure that the director keeps up with
 real time.
 <p>
 @author  Christoph Meyer Kirsch, Edward A. Lee, Haiyang Zheng, and Shanna-Shaye Forbes
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Yellow (cm)
 @Pt.AcceptedRating Red (eal)
 @see GiottoScheduler
 @see GiottoReceiver
 */
public class GiottoDirector extends StaticSchedulingDirector implements
Decorator {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *  @exception IllegalActionException If the name has a period in it, or
     *  the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *  an entity with the specified name.
     */
    public GiottoDirector() throws IllegalActionException,
    NameDuplicationException {
        super();
        _init();
    }

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *   with the specified container.
     *  @exception NameDuplicationException If the name collides with an
     *   attribute in the container or if there is a name duplication during
     *   initialization.
     */
    public GiottoDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    /** Construct a director in the given workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace for this object.
     *  @exception IllegalActionException If there is an exception thrown by
     *  the super class or while initializing parameters.
     *  @exception NameDuplicationException If the container reports an entity
     *  that duplicates an existing name during initialization.
     */
    public GiottoDirector(Workspace workspace) throws IllegalActionException,
    NameDuplicationException {
        super(workspace);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the specified attribute is <i>filename</i>, then close
     *  the current file (if there is one) and open the new one.
     *  If the specified attribute is <i>period</i> or
     *  <i>synchronizeToRealTime</i>, then cache the new values.
     *  If the specified attribute is <i>timeResolution</i>,
     *  then cache the new value.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the specified attribute
     *   is <i>filename</i> and the file cannot be opened.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == period) {
            _periodValue = ((DoubleToken) period.getToken()).doubleValue();
        } else if (attribute == synchronizeToRealTime) {
            _synchronizeToRealTime = ((BooleanToken) synchronizeToRealTime
                    .getToken()).booleanValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is an object with no container.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown in this base class
     *  @return The new object.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        GiottoDirector newObject = (GiottoDirector) super.clone(workspace);
        newObject._receivers = new LinkedList();
        return newObject;
    }

    /** Return the decorated attributes for the target NamedObj.
     *  @param target The NamedObj that will be decorated.
     *  @return The decorated attributes for the target NamedObj.
     */
    @Override
    public DecoratorAttributes createDecoratorAttributes(NamedObj target) {
        if (_debugging) {
            _debug("createDecoratorAttributes method called for Giotto Director");
        }
        if (target instanceof Actor) {
            try {
                return new GiottoAttributes(target, this);
            } catch (KernelException e) {
                throw new InternalErrorException(e);
            }
        } else {
            return null;
        }
    }

    /** Return a list of the entities deeply contained by the container
     *  of this resource scheduler.
     *  @return A list of the objects decorated by this decorator.
     */
    @Override
    public List<NamedObj> decoratedObjects() {
        CompositeEntity container = (CompositeEntity) getContainer();
        return container.deepEntityList();
    }

    /** Fire a complete iteration and advance time to the current time plus
     *  the period value. A complete iteration consists of several minor cycles.
     *  At each minor cycle, iterate actors in the corresponding minor cycle
     *  schedule. After iterating the actors, increment time by the minor cycle
     *  time. Also, update the receivers that are destinations of all actors
     *  that will be invoked in the next minor cycle of the schedule.
     *  This works because all actors in Giotto are invoked periodically,
     *  and the ones that will be invoked in the next cycle are the ones
     *  that are completing invocation at the end of this cycle.
     *  @exception IllegalActionException If this director does not have a
     *   container.
     */
    @Override
    public void fire() throws IllegalActionException {
        if (_debugging) {
            _debug("the fire method for the giotto director was called");
        }
        TypedCompositeActor container = (TypedCompositeActor) getContainer();

        if (container == null) {
            throw new IllegalActionException(this, "Has no container!");
        }

        if (_debugging) {
            _debug("Giotto director firing!");
        }

        if (!_readyToFire) {
            return;
        }

        while (_unitIndex < _schedule.size() && !_stopRequested) {
            setModelTime(_expectedNextIterationTime);

            // Grab the next minor cycle (unit) schedule to execute.
            Schedule unitSchedule = (Schedule) _schedule.get(_unitIndex);

            // We only do synchronization to real time here and leave time
            // update to upper level directors or the postfire() method.
            if (_synchronizeToRealTime) {
                long elapsedTime = System.currentTimeMillis() - _realStartTime;
                double elapsedTimeInSeconds = elapsedTime / 1000.0;

                if (_expectedNextIterationTime.getDoubleValue() > elapsedTimeInSeconds) {
                    long timeToWait = (long) ((_expectedNextIterationTime
                            .getDoubleValue() - elapsedTimeInSeconds) * 1000.0);

                    if (timeToWait > 0) {
                        if (_debugging) {
                            _debug("Waiting for real time to pass: "
                                    + timeToWait);
                        }

                        // Synchronize on the scheduler.
                        Scheduler scheduler = getScheduler();

                        synchronized (scheduler) {
                            try {
                                scheduler.wait(timeToWait);
                            } catch (InterruptedException ex) {
                                // Continue executing.
                            }
                        }
                    }
                }
            }

            // Find the actors that will be invoked in this minor cycle (unit)
            // and update the receivers that are their destinations.
            // The reason that the destinations are updated is that any
            // actor that is to be run in this unit is also presumably
            // just completing a run from the previous iteration (because
            // of the periodic semantics of Giotto).
            // Of course, in the first iteration, the actors are not
            // (obviously) completing any previous cycle.  However, according
            // to Giotto semantics, the first unit always involves a firing
            // of every actor.  Hence, the first unit schedule will include
            // every actor.  Thus, in the first iteration, we will be
            // committing the outputs of every actor.  This has the effect
            // of committing any data values that the actor may have produced
            // in its initialize() method.
            Iterator scheduleIterator = unitSchedule.iterator();

            while (scheduleIterator.hasNext()) {
                Actor actor = ((Firing) scheduleIterator.next()).getActor();
                if (_debugging) {
                    _debug("actor to be fired in this iteration has name "
                            + actor.getFullName());
                }
                Time thistime = getModelTime();

                if (_debugging) {
                    _debug("the current time is " + thistime.toString());
                }
                if (_debugging) {
                    _debug("Updating destination receivers of "
                            + ((NamedObj) actor).getFullName());
                }

                List outputPortList = actor.outputPortList();
                Iterator outputPorts = outputPortList.iterator();

                while (outputPorts.hasNext()) {
                    IOPort port = (IOPort) outputPorts.next();
                    if (_debugging) {
                        _debug("output port is " + port.getDisplayName());
                    }
                    Receiver[][] channelArray = port.getRemoteReceivers();

                    for (Receiver[] receiverArray : channelArray) {
                        for (Receiver element : receiverArray) {
                            GiottoReceiver receiver = (GiottoReceiver) element;
                            receiver.update();
                        }
                    }
                }
                if (_debugging) {
                    _debug("Done firing actor "
                            + actor
                            + " now going to check to see if it went over time.");
                }

            }

            scheduleIterator = unitSchedule.iterator();

            while (scheduleIterator.hasNext()) {
                Actor actor1 = ((Firing) scheduleIterator.next()).getActor();

                if (_debugging) {
                    _debug("Iterating " + ((NamedObj) actor1).getFullName());
                }

                if (actor1.iterate(1) == STOP_ITERATING) {
                    // FIXME: How to handle this?
                    // put the actor on a no-fire hashtable?
                    System.err.println("Warning: Giotto iterate returned "
                            + "STOP_ITERATING for actor \""
                            + actor1.getFullName() + "\"");
                }
            }
            if (_debugging) {
                _debug("unit index has value " + _unitIndex);
            }
            _unitIndex++;

            _expectedNextIterationTime = _expectedNextIterationTime
                    .add(_unitTimeIncrement);
            //this compensates for rounding errors that may occur
            if (_unitIndex == _lcm) {
                _expectedNextIterationTime = new Time(this, _iterationCount
                        + _periodValue * _unitIndex);
                if (_debugging) {
                    _debug("unit index is equal to lcm");
                    _debug("iteration count is: " + _iterationCount);
                }

            }

            if (_debugging) {
                _debug("next Iteration time " + _expectedNextIterationTime
                        + "\n");
            }
        }

        if (_unitIndex >= _schedule.size()) {
            _unitIndex = 0;

            // Iteration is complete when the unit index wraps around.
            if (_debugging) {
                _debug("===== Director completing unit of iteration: "
                        + _iterationCount);
            }

            _iterationCount++;
        }
        if (_debugging) {
            _debug("done firing for this time unit");
        }
        if (_debugging) {
            _debug("end of the call to the fire method for the giotto director");
        }
    }

    /** Request a firing of the given actor at the given absolute
     *  time.  This method calculates the period of invocation of
     *  the specified actor (which is the period of this director
     *  divided by the actor's frequency), and if the requested time
     *  is ahead of current time by some multiple of the actor's period,
     *  then return the requested time.
     *  @param actor The actor scheduled to be fired.
     *  @param time The requested time.
     *  @param microstep The microstep (ignored by this director).
     *  @return The time at which the actor passed as an argument
     *   will be fired.
     *  @exception IllegalActionException If the operation is not
     *    permissible (e.g. the given time is in the past).
     */
    @Override
    public Time fireAt(Actor actor, Time time, int microstep)
            throws IllegalActionException {
        if (_debugging) {
            _debug("fireAt method was called for actor: " + actor.getFullName());
        }
        // No executive director. Return current time plus the period divided
        // by the frequency of the specified actor,
        // or some multiple of that number.
        // NOTE: this is potentially very expensive to compute precisely
        // because the Time class has an infinite range and only supports
        // precise addition. Determining whether the argument satisfies
        // the criterion seems difficult. Hence, we check to be sure
        // that the test is worth doing.
        Time currentTime = getModelTime();

        int frequencyValue = getActorFrequency((NamedObj) actor, this);

        double actorPeriod = _periodValue / frequencyValue;
        if (_debugging) {
            _debug("inside fireAt the frequency value is : " + frequencyValue);
            _debug("inside fireAt the actor period is: " + actorPeriod);
        }

        Time nextFiringTime = currentTime.add(actorPeriod);

        if (_debugging) {
            _debug("current time is: " + currentTime.getDoubleValue());
            _debug("next firing time is: " + nextFiringTime.getDoubleValue());
            _debug("desired firing time is: " + time.getDoubleValue());

        }
        // First check to see whether we are in the initialize phase, in
        // which case, return the start time.
        NamedObj container = getContainer();
        if (container != null) {
            Manager manager = ((CompositeActor) container).getManager();
            if (manager.getState().equals(Manager.INITIALIZING)) {
                return currentTime;
            }
        }
        // Check the most common cases next.
        if (time.equals(currentTime) || time.equals(nextFiringTime)) {
            return nextFiringTime;
        }
        if (time.isInfinite() || currentTime.compareTo(time) > 0) {
            // Either the requested time is infinite or it is in the past.
            return currentTime.add(nextFiringTime);
        }
        Time futureTime = currentTime;
        while (time.compareTo(futureTime) > 0) {
            futureTime = futureTime.add(actorPeriod);
            if (futureTime.equals(time)) {
                return time;
            }
        }
        return currentTime.add(nextFiringTime);
    }

    /** Return the frequency of the specified actor by accessing a
     *  parameter named "frequency". If there is no such parameter,
     *  then look for a decorator parameter named "frequency."
     *  @param actor The actor.
     *  @param director The director.
     *  @return The frequency of the actor firings.
     *  @exception IllegalActionException If thrown while getting the value
     *  of the frequency decorator attribute.
     */
    public static int getActorFrequency(NamedObj actor, GiottoDirector director)
            throws IllegalActionException {
        int frequencyValue = 1;
        Attribute frequency = actor.getAttribute("frequency");
        if (frequency == null) {
            frequency = actor.getDecoratorAttribute(director, "frequency");
        }
        if (frequency instanceof Parameter) {
            Token result = ((Parameter) frequency).getToken();
            if (result instanceof IntToken) {
                frequencyValue = ((IntToken) result).intValue();
            }
        }
        return frequencyValue;
    }

    /** Get the period of the giotto director in ms.
     *  @return int value of period in ms.
     */
    public int getIntPeriod() {
        // In ptolemy model, for simulation, time is double with unit
        // Second however, for giotto code, we need integer and its
        // unit is microSecond
        return (int) (_periodValue * 1000);
    }

    /** Return the next time that this director expects activity.
     *  @return The time of the next iteration.
     */
    @Override
    public Time getModelNextIterationTime() {
        return getModelTime().add(_unitTimeIncrement);
    }

    /** Get the period of the giotto director in ms.
     *
     *  @return double value of period in ms.
     */
    public double getPeriod() {
        // In ptolemy models, for simulation, time is double with seconds
        // unit; however, for giotto code, we need integer and its
        // unit is milliSecond.
        return _periodValue;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Handle a model error.
     *  @param context The object in which the error occurred.
     *  @param exception An exception that represents the error.
     *  @return True if the error has been handled, or false if the
     *   error is not handled.
     *  @exception IllegalActionException If the handler handles the
     *   error by throwing an exception.///
     */

    //    public boolean handleModelError(NamedObj context,
    //         IllegalActionException exception) throws IllegalActionException {
    //
    //     if (_debugging) {
    //         _debug("Handle Model Error Called for GiottoDirector");
    //     }
    //
    //     NamedObj dummyContainer = this.getContainer();
    //     NamedObj parentContainer = dummyContainer.getContainer();
    //     if (parentContainer != null) {
    //         return parentContainer.handleModelError(context, exception);
    //     } else {
    //         throw new IllegalActionException(this,
    //                 "Unable to set error transition. This is the top most director ");
    //     }
    //
    //    }

    /** Initialize the actors associated with this director.
     *  The order in which the actors are initialized is arbitrary.
     *  @exception IllegalActionException If the initialize() method of
     *   one of the associated actors throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        _iterationCount = 0;
        _unitIndex = 0;
        _expectedNextIterationTime = getModelTime();

        // The receivers should be reset before their initialization.
        ListIterator receivers = _receivers.listIterator();
        while (receivers.hasNext()) {
            GiottoReceiver receiver = (GiottoReceiver) receivers.next();
            if (receiver.getContainer() != null) {
                receiver.reset();
            } else {
                // Receiver is no longer in use.
                receivers.remove();
            }
        }

        // FIXME: SampleDelay does not call update().
        //        Need an explicit initialValue??
        super.initialize();

        // Iterate through all output ports to see if any have initialValue
        // parameters or init values from initialization.
        CompositeActor compositeActor = (CompositeActor) getContainer();
        List actorList = compositeActor.deepEntityList();
        ListIterator actors = actorList.listIterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            List outputPortList = actor.outputPortList();
            Iterator outputPorts = outputPortList.iterator();

            while (outputPorts.hasNext()) {
                IOPort port = (IOPort) outputPorts.next();
                Parameter initialValueParameter = (Parameter) ((NamedObj) port)
                        .getAttribute("initialValue");

                if (initialValueParameter != null) {
                    // Since we delay the transfer of outputs, we have to
                    // make the receivers of the port call 'update'
                    // instead of 'put' only.
                    port.broadcast(initialValueParameter.getToken());

                    Receiver[][] channelArray = port.getRemoteReceivers();

                    for (Receiver[] receiverArray : channelArray) {
                        for (Receiver element : receiverArray) {
                            GiottoReceiver receiver = (GiottoReceiver) element;
                            receiver.update();
                        }
                    }
                }
            }
        }

        // Set the initial time.
        setModelTime(_expectedNextIterationTime);
        resume();

        _realStartTime = System.currentTimeMillis();
    }

    /** Return false to indicate that this decorator should not
     *  decorate objects across opaque hierarchy boundaries.
     */
    @Override
    public boolean isGlobalDecorator() {
        return false;
    }

    /** Return a new receiver consistent with the Giotto domain.
     *  @return A new GiottoReceiver.
     */
    @Override
    public Receiver newReceiver() {
        Receiver receiver = new GiottoReceiver();
        _receivers.add(receiver);
        return receiver;
    }

    /** Return false if the system has finished executing, either by
     *  reaching the iteration limit, or by having an actor in the model
     *  return false in postfire.
     *  @return True if the execution is not finished.
     *  @exception IllegalActionException If the iterations parameter does
     *   not have a valid token.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        // FIXME: We are ignoring the return value here.
        super.postfire();
        if (_debugging) {
            _debug("Giotto director postfiring!");
        }

        if (!_readyToFire) {
            return !_stopRequested && !_finishRequested;
        }

        int numberOfIterations = ((IntToken) iterations.getToken()).intValue();

        if (numberOfIterations > 0 && _iterationCount >= numberOfIterations) {
            // iterations limit is reached
            _iterationCount = 0;

            if (isEmbedded()) {
                return !_stopRequested && !_finishRequested;
            } else {
                return false;
            }
        } else {
            // continue iterations
            if (isEmbedded()) {
                // unless the iteration counts are met,
                // keep requesting to fire itself.
                _requestFiring();
            }
        }

        return !_stopRequested && !_finishRequested;
    }

    /** This method always return true.
     *  If this director is at the top level, returning true means always
     *  ready to fire.
     *  If embedded, return true usually means that the current time of
     *  the outside domain is greater than or equal to the current time.
     *  However, when a giotto model is used inside a CT model, its inputs
     *  may either be DISCRETE or CONTINUOUS. When the inputs are of type
     *  CONTINUOUS, this method should always return true. To accommodate
     *  this requirement, the prefire method still returns true but
     *  an internal flag will be set to false and the fire and postfire
     *  methods are forced to do nothing.
     *
     *  @return True if the director is ready to run for one iteration.
     *  @exception IllegalActionException If time is set backwards.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        if (isEmbedded()) {
            CompositeActor container = (CompositeActor) getContainer();
            Time outsideCurrentTime = ((Actor) container)
                    .getExecutiveDirector().getModelTime();

            if (outsideCurrentTime.compareTo(_expectedNextIterationTime) < 0) {
                // not the scheduled time to fire.
                _readyToFire = false;
            } else if (outsideCurrentTime.compareTo(_expectedNextIterationTime) == 0) {
                // the outside time is equal to the expected
                // next iteration time...
                setModelTime(outsideCurrentTime);

                if (_debugging) {
                    _debug("Set current time as: " + getModelTime());
                }

                _readyToFire = true;
            } else {
                // the outside time is later than the expected next iteration
                // time. This may happen when a giotto model stops firing at
                // some time and resumes firing after a while. See the giotto
                // composite demo.
                _expectedNextIterationTime = outsideCurrentTime;
                setModelTime(outsideCurrentTime);

                if (_debugging) {
                    _debug("Set current time as: " + getModelTime());
                }

                _readyToFire = true;
            }
        } else {
            _readyToFire = true;
        }

        return true;
    }

    /** Preinitialize the actors associated with this director.
     *  Generate the giotto schedule.
     *  @exception IllegalActionException If the preinitialize() method of
     *   one of the associated actors throws it.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        if (_debugging) {
            _debug("preinitialize method in giotto director called");
        }
        // before initialize the contained actors, reset the period parameter
        // if the model is embedded inside another giotto model.
        CompositeActor compositeActor = (CompositeActor) getContainer();

        if (isEmbedded()) {
            Director executiveDirector = compositeActor.getExecutiveDirector();

            if (executiveDirector instanceof GiottoDirector) {
                double periodValue = ((GiottoDirector) executiveDirector)
                        .getPeriod();
                int frequencyValue = getActorFrequency(compositeActor,
                        (GiottoDirector) executiveDirector);

                _periodValue = periodValue / frequencyValue;
                period.setExpression(Double.toString(_periodValue));
            }
        }

        // Next, construct the schedule.
        // FIXME: Note that mutations will not be supported since the
        // schedule is constructed only once.
        GiottoScheduler scheduler = (GiottoScheduler) getScheduler();
        _schedule = scheduler.getSchedule();
        _unitTimeIncrement = scheduler._getMinTimeStep(_periodValue);

        // Actor actor;

    }

    /** Override the base class to first set the container, then establish
     *  a connection with any decorated objects it finds in scope in the new
     *  container.
     *  @param container The container to attach this attribute to..
     *  @exception IllegalActionException If this attribute is not of the
     *   expected class for the container, or it has no name,
     *   or the attribute and container are not in the same workspace, or
     *   the proposed container would result in recursive containment.
     *  @exception NameDuplicationException If the container already has
     *   an attribute with the name of this attribute.
     *  @see #getContainer()
     */
    @Override
    public void setContainer(NamedObj container) throws IllegalActionException,
    NameDuplicationException {
        super.setContainer(container);
        if (container != null) {
            List<NamedObj> decoratedObjects = decoratedObjects();
            for (NamedObj decoratedObject : decoratedObjects) {
                // The following will create the DecoratorAttributes if it does not
                // already exist, and associate it with this decorator.
                decoratedObject.getDecoratorAttributes(this);
            }
        }
    }

    /** Return an array of suggested directors to be used with
     *  ModalModel. Each director is specified by its full class
     *  name.  The first director in the array will be the default
     *  director used by a modal model.
     *  @return An array of suggested directors to be used with ModalModel.
     *  @see ptolemy.actor.Director#suggestedModalModelDirectors()
     */
    @Override
    public String[] suggestedModalModelDirectors() {
        String[] defaultSuggestions = new String[2];
        defaultSuggestions[0] = "ptolemy.domains.modal.kernel.NonStrictFSMDirector";
        defaultSuggestions[1] = "ptolemy.domains.modal.kernel.FSMDirector";
        return defaultSuggestions;
    }

    /** Transfer data from an input port of the container to the ports
     *  it is connected to on the inside. The port argument must be an
     *  opaque input port. If any channel of the input port has no data,
     *  then that channel is ignored. This method will transfer exactly
     *  one token on each input channel that has at least one token
     *  available. Update all receivers to which a token is transferred.
     *
     *  @param port The port to transfer tokens from.
     *  @return True if at least one data token is transferred.
     *  @exception IllegalActionException If the port is not an opaque
     *  input port.
     */
    @Override
    public boolean transferInputs(IOPort port) throws IllegalActionException {
        if (!port.isInput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "transferInputs: port argument is not an opaque"
                            + "input port.");
        }

        boolean transfer = false;
        Receiver[][] insideReceivers = port.deepGetReceivers();

        for (int i = 0; i < port.getWidth(); i++) {
            if (port.hasToken(i)) {
                Token t = port.get(i);

                if (insideReceivers != null && insideReceivers[i] != null) {
                    if (_debugging) {
                        _debug(getName(),
                                "transferring input from " + port.getName()
                                + " channel " + i);
                    }

                    for (int j = 0; j < insideReceivers[i].length; j++) {
                        if (_debugging) {
                            _debug("Sending token to receiver of "
                                    + insideReceivers[i][j].getContainer());
                        }

                        insideReceivers[i][j].put(t);
                        ((GiottoReceiver) insideReceivers[i][j]).update();
                    }

                    transfer = true;
                }
            }
        }

        return transfer;
    }

    /** Transfer data from this port to the ports it is connected to on
     *  the outside.
     *  This port must be an opaque output port.  If any
     *  channel of this port has no data, then that channel is
     *  ignored. This method will transfer exactly one token on
     *  each output channel that has at least one token available.
     *  @param port The port to transfer tokens from.
     *  @return True if at least one data token is transferred.
     *  @exception IllegalActionException If the port is not an opaque
     *   output port.
     */
    @Override
    public boolean transferOutputs(IOPort port) throws IllegalActionException {
        if (!port.isOutput() || !port.isOpaque()) {
            throw new IllegalActionException(port,
                    "transferOutputs: this port is not "
                            + "an opaque output port.");
        }

        boolean wasTransferred = false;
        Receiver[][] insideReceivers = port.getInsideReceivers();

        if (insideReceivers != null) {
            for (int i = 0; i < insideReceivers.length; i++) {
                if (insideReceivers[i] != null) {
                    for (int j = 0; j < insideReceivers[i].length; j++) {
                        try {
                            if (insideReceivers[i][j].isKnown()) {
                                if (insideReceivers[i][j].hasToken()) {
                                    if (_debugging) {
                                        _debug(getName(),
                                                "transferring output from "
                                                        + port.getName()
                                                        + " to channel " + i);
                                    }

                                    Token t = ((GiottoReceiver) insideReceivers[i][j])
                                            .remove();
                                    port.send(i, t);
                                    wasTransferred = true;
                                }
                            }
                        } catch (NoTokenException ex) {
                            throw new InternalErrorException(port, ex, null);
                        }
                    }
                }
            }
        }

        return wasTransferred;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The number of times that postfire may be called before it
     *  returns false. If the value is less than or equal to zero,
     *  then the execution will never return false in postfire,
     *  and thus the execution can continue forever.
     *  The default value is an IntToken with the value zero.
     */
    public Parameter iterations;

    /** The period of an iteration. This is a double that defaults to
     *  <I>0.1</I>.
     */
    public Parameter period;

    /** Specify whether the execution should synchronize to the
     *  real time. This parameter must contain a BooleanToken.
     *  If this parameter is true, then do not process events until the
     *  elapsed real time matches the time stamp of the events.
     *  The value defaults to false.
     */
    public Parameter synchronizeToRealTime;

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The static default Giotto period is 100ms.
     */
    protected static final double _DEFAULT_GIOTTO_PERIOD = 0.1;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /* Initialize the director by creating a scheduler and parameters.
     *  @exception NameDuplicationException If the container reports an entity
     *  that duplicates an existing name during initialization.
     *  @exception IllegalActionException If any of the methods called by
     *  _init()throws an exception. For instance a call to setToken() may
     *  throw an IllegalActionException if the token type is not
     *  compatible with specified constraints, or if you are attempting
     *  to set to null a variable that has value dependents, or if the
     *  container rejects the change.
     */
    private void _init() throws NameDuplicationException,
    IllegalActionException {
        GiottoScheduler scheduler = new GiottoScheduler(workspace());
        setScheduler(scheduler);

        period = new Parameter(this, "period");
        period.setToken(new DoubleToken(_DEFAULT_GIOTTO_PERIOD));
        iterations = new Parameter(this, "iterations", new IntToken(0));

        synchronizeToRealTime = new Parameter(this, "synchronizeToRealTime",
                new BooleanToken(false));
    }

    // Request that the container of this director be refired in the future.
    // This method is used when the director is embedded inside an opaque
    // composite actor. If the outside director is a Giotto director, this
    // method has no effect. If the outside director is a DE director, this
    // method will cause the container of this director to be fired again.
    private void _requestFiring() throws IllegalActionException {
        if (_debugging) {
            _debug("Request refiring of opaque composite actor at "
                    + _expectedNextIterationTime);
        }
        // Enqueue a refire for the container of this director.

        fireContainerAt(_expectedNextIterationTime);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The time for next iteration.
    private Time _expectedNextIterationTime;

    // The count of iterations executed.
    private int _iterationCount = 0;

    // Period of the director.
    private double _periodValue = 0.0;

    // Flag to indicate whether the current director is ready to fire.
    private boolean _readyToFire = true;

    // The real time at which the initialize() method was invoked.
    private long _realStartTime = 0;

    // List of all receivers this director has created.
    private LinkedList _receivers = new LinkedList();

    // Schedule to be executed.
    private Schedule _schedule;

    // Specify whether the director should wait for elapsed real time to
    // catch up with model time.
    private boolean _synchronizeToRealTime = false;

    // Counter for minimum time steps.
    private int _unitIndex = 0;

    // Minimum time step size (a Giotto "unit").
    private double _unitTimeIncrement = 0.0;

    //lcm of frequencies see my this director
    private int _lcm;
}
