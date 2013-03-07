/* This is a resource scheduler.

@Copyright (c) 2008-2013 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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


 */
package ptolemy.actor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.DecoratedAttributesImplementation;
import ptolemy.kernel.util.DecoratedAttributes;
import ptolemy.kernel.util.Decorator;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLModelAttribute;

/** This is a resource scheduler.
 *
 * @author Patricia Derler
   @version $Id$
   @since Ptolemy II 9.0

   @Pt.ProposedRating Red (derler)
   @Pt.AcceptedRating Red (derler)
 */
public abstract class ResourceScheduler extends MoMLModelAttribute implements
        ResourceSchedulerInterface, Decorator {

    /** Create a new actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If this actor cannot be contained
     *   by the proposed container (see the setContainer() method).
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public ResourceScheduler(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        try {
            _schedulePlotterEditorFactory = new SchedulePlotterEditorFactory(
                    this, this.uniqueName("_editorFactory"));
        } catch (NameDuplicationException e) {
            // Do nothing, we made sure that there cannot be a name duplication
            // exception.
        }

    }

    ///////////////////////////////////////////////////////////////////
    //                         public variables                      //

    /** Execution time event type. */
    public static enum ExecutionEventType {
        /** Started the execution of an actor. */
        START,
        /** Stopped the execution of an actor. */
        STOP,
        /** Preempted the execution of an actor. */
        PREEMPTED
    }

    ///////////////////////////////////////////////////////////////////
    //                           public methods                      //

    /** Return the decorated attributes for the target NamedObj.
     *  @param target The NamedObj that will be decorated.
     *  @return The decorated attributes for the target NamedObj.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public DecoratedAttributes createDecoratedAttributes(NamedObj target)
            throws IllegalActionException, NameDuplicationException {
        DecoratedAttributesImplementation decoratedAttributes = new DecoratedAttributesImplementation(
                target, this);
        if (target.getAttribute("scheduler") == null) {
            Parameter schedulerParameter = new Parameter(target, "scheduler");
            schedulerParameter.setExpression("");
        }
        if (target.getAttribute("executionTime") == null) {
            Parameter executionTime = new Parameter(target, "executionTime");
            executionTime.setExpression("0.0");
        }
        return decoratedAttributes;
    }

    /** Set the current type of the decorated attributes.
     *  The type information of the parameters are not saved in the
     *  model hand hence this has to be reset when reading the model
     *  again.
     *  @param decoratedAttributes The decorated attributes.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     */
    public void setTypesOfDecoratedVariables(
            DecoratedAttributes decoratedAttributes)
            throws IllegalActionException {
        // FIXME: What has to be done here?

    }

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ResourceScheduler newObject = (ResourceScheduler) super
                .clone(workspace);

        try {
            newObject._schedulePlotterEditorFactory = new SchedulePlotterEditorFactory(
                    newObject, this.uniqueName("_editorFactory"));
            newObject._previousY = new HashMap<NamedObj, Double>();
        } catch (NameDuplicationException e) {
            // Do nothing, we made sure that there cannot be a name duplication
            // exception.
        } catch (IllegalActionException e) {
            // If we would run into this catch clause
        }

        return newObject;
    }

    /** If the last actor that was scheduled finished execution
     *  then this method returns true.
     *  @return True if last actor that was scheduled finished
     *   execution.
     */
    public boolean lastScheduledActorFinished() {
        return _lastActorFinished;
    }

    /** Initialize local variables and if this resource
     *  scheduler wants to be fired at a future time, return
     *  this time.
     * @return Next time this scheduler requests a firing.
     * @exception IllegalActionException Thrown if list of actors
     *   scheduled by this scheduler cannot be retrieved.
     */
    public Time initialize() throws IllegalActionException {
        _remainingTimes = new HashMap<Actor, Time>();
        _lastTimeScheduled = new HashMap<Actor, Time>();
        _actors = new ArrayList<NamedObj>();

        _getActorsToSchedule((CompositeActor) getContainer());
        _actors.add(this);

        if (_schedulePlotterEditorFactory.plot != null) {
            _schedulePlotterEditorFactory.plot.clear(false);
            _schedulePlotterEditorFactory.plot.clearLegends();

            for (NamedObj actor : _actors) {
                _schedulePlotterEditorFactory.plot.addLegend(
                        _actors.indexOf(actor), actor.getName());
                event(actor, 0.0, null);
            }
            _schedulePlotterEditorFactory.plot.doLayout();
        }
        return null;
    }

    /** Schedule a new actor for execution and return the next time
     *  this scheduler has to perform a reschedule. Derived classes
     *  must implement this method to actually schedule actors, this
     *  base class implementation just creates events for scheduler
     *  activity that is displayed in the plotter.
     *  @param actor The actor to be scheduled.
     *  @param currentPlatformTime The current platform time.
     *  @param deadline The deadline of the event.
     *  @param executionTime The execution time of the actor.
     *  @return Relative time when this Scheduler has to be executed
     *    again.
     *  @exception IllegalActionException Thrown if actor parameters such
     *    as execution time or priority cannot be read.
     */
    public Time schedule(Actor actor, Time currentPlatformTime,
            Double deadline, Time executionTime) throws IllegalActionException {
        event(this, currentPlatformTime.getDoubleValue(),
                ExecutionEventType.START);
        event(this, currentPlatformTime.getDoubleValue(),
                ExecutionEventType.STOP);

        return null;
    }

    /** Return a new time object using the enclosing director.
     *  @param time Double value of the new time object.
     *  @return The new time object.
     *  @exception IllegalActionException If the time object cannot be created.
     */
    public Time getTime(double time) throws IllegalActionException {
        return new Time(((CompositeActor) getContainer()).getDirector(), time);
    }

    /** Return whether last actor that was scheduled finished execution.
     *  @return True if last actor finished execution.
     */
    public boolean lastActorFinished() {
        return _lastActorFinished;
    }

    /** Return remaining time actor needs to finish.
     *  @param actor The actor.
     *  @return The time the actor still needs.
     */
    public Time getRemainingTime(Actor actor) {
        return _remainingTimes.get(actor);
    }

    /** Plot a new execution event for an actor (i.e. an actor
     *  started/finished execution, was preempted or resumed).
     * @param actor The actor.
     * @param physicalTime The physical time when this scheduling event occurred.
     * @param scheduleEvent The scheduling event.
     */
    public void event(final NamedObj actor, double physicalTime,
            ExecutionEventType scheduleEvent) {
        if (_schedulePlotterEditorFactory.plot == null) {
            return;
        }

        double x = physicalTime;
        int actorDataset = _actors.indexOf(actor);
        if (actorDataset == -1) {
            return; // actor is not being monitored
        }
        if (scheduleEvent == null) {
            if (_previousY.get(actor) == null) {
                _previousY.put(actor, (double) actorDataset);
            }
            _schedulePlotterEditorFactory.plot.addPoint(actorDataset, x,
                    _previousY.get(actor), true);
            _previousY.put(actor, (double) actorDataset);
        } else if (scheduleEvent == ExecutionEventType.START) {
            _schedulePlotterEditorFactory.plot.addPoint(actorDataset, x,
                    _previousY.get(actor), true);
            _schedulePlotterEditorFactory.plot.addPoint(actorDataset, x,
                    actorDataset + 0.6, true);
            _previousY.put(actor, actorDataset + 0.6);
        } else if (scheduleEvent == ExecutionEventType.STOP) {
            _schedulePlotterEditorFactory.plot.addPoint(actorDataset, x,
                    actorDataset + 0.6, true);
            _schedulePlotterEditorFactory.plot.addPoint(actorDataset, x,
                    actorDataset, true);
            _previousY.put(actor, (double) actorDataset);
        } else if (scheduleEvent == ExecutionEventType.PREEMPTED) {
            _schedulePlotterEditorFactory.plot.addPoint(actorDataset, x,
                    actorDataset + 0.6, true);
            _schedulePlotterEditorFactory.plot.addPoint(actorDataset, x,
                    actorDataset + 0.4, true);
            _previousY.put(actor, actorDataset + 0.4);
        }
        _schedulePlotterEditorFactory.plot.fillPlot();
        _schedulePlotterEditorFactory.plot.repaint();
    }

    /** Create end events for the plotter.
     *  @exception IllegalActionException Thrown by super class.
     */
    public void wrapup() throws IllegalActionException {
        for (NamedObj actor : _actors) {
            event(actor, ((CompositeActor) getContainer()).getDirector()
                    .getEnvironmentTime().getDoubleValue(), null);
        }
    }

    ///////////////////////////////////////////////////////////////////
    //                          protected methods                    //

    /** Iterate through all actors in the container and find those
     *  that are scheduled by this ResourceScheduler.
     *  @param compositeActor The container.
     *  @exception IllegalActionException If actor parameters that describe
     *  the schedulers cannot be read.
     */
    protected void _getActorsToSchedule(CompositeActor compositeActor)
            throws IllegalActionException {
        for (Object entity : compositeActor.entityList()) {
            Parameter schedulerAttribute = (Parameter) ((NamedObj) entity)
                    .getAttribute("scheduler");
            if (schedulerAttribute != null
                    && schedulerAttribute.getToken() != null
                    && schedulerAttribute.getToken() instanceof ObjectToken
                    && ((ObjectToken) schedulerAttribute.getToken()).getValue() instanceof ResourceScheduler) {
                ResourceScheduler scheduler = (ResourceScheduler) ((ObjectToken) schedulerAttribute
                        .getToken()).getValue();
                if (scheduler == this || !(entity instanceof ResourceScheduler)) {
                    Double executionTime = ResourceScheduler
                            ._getDoubleParameterValue(compositeActor,
                                    "executionTime");

                    if (executionTime != null) {
                        _remainingTimes.put(compositeActor, null);
                    }
                    _actors.add((NamedObj) entity);
                    event(compositeActor, 0.0, null);
                }
            } else if (entity instanceof CompositeActor) {
                _getActorsToSchedule((CompositeActor) entity);
            }
        }
    }

    /** Return the value stored in a parameter associated with
     *  the input port.
     *  Used for deviceDelay, deviceDelayBound, networkDelayBound,
     *  platformDelay and sourcePlatformDelay.
     *  FIXME: specialized ports do contain the parameters, don't
     *  have to get the attribute with the string! For now leave it
     *  that way to support older models that do not use PtidesPorts.
     *  @param object The object that has the parameter.
     *  @param parameterName The name of the parameter to be retrieved.
     *  @return the value of the named parameter if the parameter is not
     *  null. Otherwise return null.
     *  @exception IllegalActionException If thrown while getting the value
     *  of the parameter.
     */
    protected static Double _getDoubleParameterValue(NamedObj object,
            String parameterName) throws IllegalActionException {
        Parameter parameter = (Parameter) object.getAttribute(parameterName);
        if (parameter != null && parameter.getToken() != null) {
            return Double.valueOf(((DoubleToken) parameter.getToken())
                    .doubleValue());
        }
        return null;
    }

    ///////////////////////////////////////////////////////////////////
    //                        protected variables                      //

    /** True if in the last request to schedule an actor, this actor
     *  finished execution.
     */
    protected boolean _lastActorFinished;

    /** The last time an actor's remaining time was updated due to a scheduling
     *  request.
     */
    protected HashMap<Actor, Time> _lastTimeScheduled;

    /** The remaining execution time for every actor that has been scheduled
     *  or null if the actor execution finished.
     */
    protected HashMap<Actor, Time> _remainingTimes;

    ///////////////////////////////////////////////////////////////////
    //                        private methods                        //

    ///////////////////////////////////////////////////////////////////
    //                      private variables                        //

    /** Contains the actors inside a ptides platform (=platforms). */
    protected List<NamedObj> _actors;

    /** Previous positions of the actor data set. */
    private HashMap<NamedObj, Double> _previousY = new HashMap<NamedObj, Double>();

    private SchedulePlotterEditorFactory _schedulePlotterEditorFactory;

}
