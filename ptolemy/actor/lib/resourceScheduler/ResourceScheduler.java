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
package ptolemy.actor.lib.resourceScheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.lib.ResourceAttributes;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Decorator;
import ptolemy.kernel.util.DecoratorAttributes;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLModelAttribute;

/**
This is a base class for resource schedulers.
This is a {@link Decorator} that will decorate any instance of {@link Actor}
that is deeply contained by its container, including within opaque
composites.
This base class provides one decorator attributes. When you create an instance
of this class in a composite actor, every Actor within that composite actor
is decorated with these this parameter:
<ul>
<li> <li>enable</i>: If true, then the decorated actor will use this resource.
     This is a boolean that defaults to false.
</ul>

This base class is not used but to use derived classes, drag them into a model 
and enable the actors that will use the resource. 

Currently, the following Directors honor ResourceScheduler settings:
<ul>
<li> PtidesDirector. A ResourceScheduler on a PtidesPlatform will cause the 
platform time at which actors produce their outputs
to be delayed by the specified execution time beyond the platform time at
which the resource becomes available to execute the actor. </li>
<li> DEDirector. Note that using a ResourceScheduler in a DE model
will change the MoC and nondeterminism is introduced with ResourceSchedulers.
</li>
<li> SDF Director, if contained hierarchically directly or via multiple
hierarchy layers by a PtidesDirector. </li>
<li> SysMLSequentialDirector, if contained hierarchically directly or via multiple
hierarchy layers by a PtidesDirector. </li>
</ul>  

@author Patricia Derler
@author Edward A. Lee
@version $Id$
@since Ptolemy II 9.0
@Pt.ProposedRating Red (derler)
@Pt.AcceptedRating Red (derler)
 */
public class ResourceScheduler extends MoMLModelAttribute implements Decorator {

    /** Create a new resource schedule in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
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
        _schedulePlotterEditorFactory = new SchedulePlotterEditorFactory(this,
                this.uniqueName("_editorFactory"));
    }

    ///////////////////////////////////////////////////////////////////
    //                         public variables                      //

    /** Execution time event types. */
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
        } catch (KernelException e) {
            throw new InternalErrorException(e);
        }
    
        return newObject;
    }

    /** Return the decorated attributes for the target NamedObj.
     *  If the specified target is not an Actor, return null.
     *  @param target The NamedObj that will be decorated.
     *  @return The decorated attributes for the target NamedObj, or
     *   null if the specified target is not an Actor.
     */
    public DecoratorAttributes createDecoratorAttributes(NamedObj target) {
        if (target instanceof Actor) {
            try {
                return new ResourceAttributes(target, this);
            } catch (KernelException ex) {
                // This should not occur.
                throw new InternalErrorException(ex);
            }
        } else {
            return null;
        }
    }

    /** Return a list of the entities deeply contained by the container
     *  of this resource scheduler.
     *  @return A list of the objects decorated by this decorator.
     */
    public List<NamedObj> decoratedObjects() {
        // FIXME: This should traverse opaque boundaries.
        CompositeEntity container = (CompositeEntity) getContainer();
        return container.deepEntityList();
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

    /** Return remaining time actor needs to finish.
     *  @param actor The actor.
     *  @return The time the actor still needs.
     */
    public Time getRemainingTime(Actor actor) {
        return _remainingTimes.get(actor);
    }

    /** Return a new time object using the enclosing director.
     *  @param time Double value of the new time object.
     *  @return The new time object.
     *  @exception IllegalActionException If the time object cannot be created.
     */
    public Time getTime(double time) throws IllegalActionException {
        return new Time(((CompositeActor) getContainer()).getDirector(), time);
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
    
        _initializeActorsToSchedule();
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

    /** Return true to indicate that this decorator should
     *  decorate objects across opaque hierarchy boundaries.
     */
    public boolean isGlobalDecorator() {
        return true;
    }

    /** Return whether last actor that was scheduled finished execution.
     *  @return True if last actor finished execution.
     */
    public boolean lastActorFinished() {
        return _lastActorFinished;
    }

    /** If the last actor that was scheduled finished execution
     *  then this method returns true.
     *  @return True if last actor that was scheduled finished
     *   execution.
     */
    public boolean lastScheduledActorFinished() {
        return _lastActorFinished;
    }

    /** Schedule an actor for execution and return the next time
     *  this scheduler has to perform a reschedule. Derived classes
     *  must implement this method to actually schedule actors, this
     *  base class implementation just creates events for scheduler
     *  activity that is displayed in the plotter. This
     *  base class implementation just creates events for scheduler
     *  activity that is displayed in the plotter.
     *  @param actor The actor to be scheduled.
     *  @param environmentTime The current platform time.
     *  @param deadline The deadline timestamp of the event to be scheduled.
     *  This can be the same as the environmentTime. 
     *  @return Relative time when this Scheduler has to be executed
     *    again to perform rescheduling actions.
     *  @exception IllegalActionException Thrown if actor parameters such
     *    as execution time or priority cannot be read.
     */
    public Time schedule(Actor actor, Time environmentTime, Time deadline)
            throws IllegalActionException {
        Director director = ((CompositeActor) getContainer()).getDirector();
        double executionTime = _getExecutionTime(actor);

        event(this, environmentTime.getDoubleValue(), ExecutionEventType.START);
        event(this, environmentTime.getDoubleValue(), ExecutionEventType.STOP);
        return _schedule(actor, environmentTime, deadline, new Time(director,
                executionTime));
    }

    /** Perform rescheduling actions when no new actor requests to be
     *  scheduled.
     * @param environmentTime The outside time.
     * @return Relative time when this Scheduler has to be executed
     *    again to perform rescheduling actions.
     * @exception IllegalActionException Thrown in subclasses.   
     */
    public Time schedule(Time environmentTime) throws IllegalActionException {
        return Time.POSITIVE_INFINITY;
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

    /** Iterate through all entities deeply contained by the container,
     *  record for each that it is not executing.
     *  @exception IllegalActionException If the decorator parameters cannot be read.
     */
    protected void _initializeActorsToSchedule() throws IllegalActionException {
        List<NamedObj> entities = ((CompositeEntity) getContainer())
                .deepEntityList();
        _getAllManagedEntities(entities);
    }

    /** Get the deadline for an actor that requests a firing at time
     *  <i>timestamp</i>. This base class just returns the maximum value.
     *  @param actor The actor that requests firing.
     *  @param timestamp The time when the actor wants to be fired.
     *  @return The deadline for the actor.
     *  @exception IllegalActionException If time objects cannot be created.
     */
    protected Time _getDeadline(Actor actor, Time timestamp)
            throws IllegalActionException {
        return Time.POSITIVE_INFINITY;
    }

    /** Get the execution time of an actor. If the actor does not have an attribute
     *  specifying the execution time, return the minimum execution time.
     * @param actor The actor.
     * @return The execution time.
     * @throws IllegalActionException Thrown in attribute or token cannot be read.
     */
    protected double _getExecutionTime(Actor actor)
            throws IllegalActionException {
        double executionTime = 0.0;
        for (ExecutionTimeResourceAttributes resourceAttributes : ((NamedObj) actor)
                .attributeList(ExecutionTimeResourceAttributes.class)) {
            if (resourceAttributes.getDecorator().equals(this)) {
                Token token = resourceAttributes.executionTime.getToken();
                if (token != null) {
                    executionTime = ((DoubleToken) token).doubleValue();
                }
                break;
            }
        }
        return executionTime;
    }

    /** Schedule the actor. In this base class, do nothing.  Derived
     *  classes should schedule the actor.   
     *  @param actor The actor to be scheduled.
     *  @param environmentTime The current platform time.
     *  @param deadline The deadline timestamp of the event to be scheduled.
     *  This can be the same as the environmentTime. 
     *  @return Relative time when this Scheduler has to be executed
     *    again to perform rescheduling actions.  In this base class, null
     *    is returned.
     *  @exception IllegalActionException Thrown if actor parameters such
     *    as execution time or priority cannot be read.
     */
    protected Time _schedule(Actor actor, Time environmentTime, Time deadline,
            Time executionTime) throws IllegalActionException {
        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                    protected variables                    ////

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

    /** Contains the actors inside a ptides platform (=platforms). */
    protected List<NamedObj> _actors;

    ///////////////////////////////////////////////////////////////////
    //                           private methods                     //

    private void _getAllManagedEntities(List<NamedObj> entities)
            throws IllegalActionException {
        for (NamedObj entity : entities) {
            ResourceAttributes decoratorAttributes = (ResourceAttributes) entity
                    .getDecoratorAttributes(this);
            if (decoratorAttributes != null) {
                if (((BooleanToken) decoratorAttributes.enable.getToken())
                        .booleanValue()) {
                    // The entity uses this resource scheduler.
                    _actors.add(entity);
                    // Indicate that the actor is not running.
                    _remainingTimes.put((Actor) entity, null);
                    event(entity, 0.0, null);
                } else if (entity instanceof CompositeActor) {
                    _getAllManagedEntities(((CompositeActor) entity)
                            .deepEntityList());
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    //                          private variables                    //

    /** Previous positions of the actor data set. */
    private HashMap<NamedObj, Double> _previousY = new HashMap<NamedObj, Double>();

    private SchedulePlotterEditorFactory _schedulePlotterEditorFactory;
}
