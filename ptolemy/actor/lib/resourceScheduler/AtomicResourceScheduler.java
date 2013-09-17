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
import ptolemy.actor.ResourceScheduler;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.gui.ColorAttribute;
import ptolemy.actor.lib.resourceScheduler.ScheduleListener.ExecutionEventType;
import ptolemy.actor.ResourceAttributes;
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

/**
This is a base class for resource schedulers.
This is a {@link Decorator} that will decorate any instance of {@link Actor}
that is deeply contained by its container, including within opaque
composites.
This base class provides one decorator attributes. When you create an instance
of this class in a composite actor, every Actor within that composite actor
is decorated with these this parameter:
<ul>
<li> <i>enable</i>: If true, then the decorated actor will use this resource.
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
public class AtomicResourceScheduler extends TypedAtomicActor implements ResourceScheduler {

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
    public AtomicResourceScheduler(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        ColorAttribute attribute = new ColorAttribute(this, "decoratorHighlightColor");
        attribute.setExpression("{0.0,0.8,0.0,1.0}");
        _schedulePlotListeners = new ArrayList<ScheduleListener>();
    }

    

    ///////////////////////////////////////////////////////////////////
    //                           public methods                      //

    /** Add schedule listener. If necessary, initialize list of actors
     *  scheduled by this resource scheduler.
     *  @param listener. The listener to be added. 
     *  @throws IllegalActionException If an error occurs in the initialization
     *  of actors scheduled by this resource scheduler.
     */
    public void addScheduleListener(ScheduleListener listener) throws IllegalActionException {
        _schedulePlotListeners.add(listener);
        if (_actors == null) {
            _initializeActorsToSchedule();
        }
        listener.initialize(_actors, this); 
    }

    /** Remove schedule listener.
     * @param listener. The listener to be removed.
     */
    public void removeScheduleListener(ScheduleListener listener) {
        _schedulePlotListeners.remove(listener);
    }

    /** Return the decorated attributes for the target NamedObj.
     *  If the specified target is not an Actor, return null.
     *  @param target The NamedObj that will be decorated.
     *  @return The decorated attributes for the target NamedObj, or
     *   null if the specified target is not an Actor.
     */
    public DecoratorAttributes createDecoratorAttributes(NamedObj target) {
        if (target instanceof Actor && !(target instanceof ResourceScheduler)) {
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
        CompositeEntity container = (CompositeEntity) getContainer();
        return _getEntitiesToDecorate(container);
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
    
    /** Perform rescheduling if necessary.
     *  @exception IllegalActionException Not thrown here.
     */
    @Override
    public void fire() throws IllegalActionException { 
        super.fire();
        Time time = schedule(getDirector().getModelTime());
        if (time.getDoubleValue() > 0.0) {
            getDirector().fireAt(this, getDirector().getModelTime().add(time));
        }
    }

    /** Initialize local variables.
     * @exception IllegalActionException Thrown if list of actors
     *   scheduled by this scheduler cannot be retrieved.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _remainingTimes = new HashMap<Actor, Time>();
        _lastTimeScheduled = new HashMap<Actor, Time>();
        _actors = new ArrayList<NamedObj>();
    
        _initializeActorsToSchedule();
        _actors.add(this);
        
        for (ScheduleListener listener : _schedulePlotListeners) {
            listener.initialize(_actors, this);
        }
    }

    /** Return true to indicate that this decorator should
     *  decorate objects across opaque hierarchy boundaries.
     */
    public boolean isGlobalDecorator() {
        return true;
    }

    public boolean isWaitingForResource(Actor actor) {
        return _remainingTimes.get(actor) != null;
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
    
    ///////////////////////////////////////////////////////////////////
    //                           private methods                     //
    
    /** Notify schedule listeners about rescheduling events.
     * @param entity Entity that is being scheduled.
     * @param time Time when entity is being scheduled.
     * @param eventType Type of event.
     */
    public void notifyScheduleListeners(NamedObj entity, Double time, ExecutionEventType eventType) {
        if (_schedulePlotListeners != null) {
            for (ScheduleListener listener : _schedulePlotListeners) {
                listener.event(entity, time, eventType);
            }
        }
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
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        super.setContainer((CompositeEntity) container);
        if (container != null) {
            List<NamedObj> decoratedObjects = decoratedObjects();
            for (NamedObj decoratedObject : decoratedObjects) {
                // The following will create the DecoratorAttributes if it does not
                // already exist, and associate it with this decorator.
                decoratedObject.getDecoratorAttributes(this);
            }
        }
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
        notifyScheduleListeners(this, environmentTime.getDoubleValue(), ExecutionEventType.START);
        notifyScheduleListeners(this, environmentTime.getDoubleValue(), ExecutionEventType.STOP);
        
        return schedule(actor, environmentTime, deadline, new Time(director,
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

    /** Create end events for the plotter.
     *  @exception IllegalActionException Thrown by super class.
     */
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        for (NamedObj actor : _actors) {
            notifyScheduleListeners(actor, ((CompositeActor) getContainer()).getDirector()
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
            if (resourceAttributes.getDecorator() != null && 
                    resourceAttributes.getDecorator().equals(this)) {
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
     *  @param executionTime The execution time of the actor.
     *  @return Relative time when this Scheduler has to be executed
     *    again to perform rescheduling actions.  In this base class, null
     *    is returned.
     *  @exception IllegalActionException Thrown if actor parameters such
     *    as execution time or priority cannot be read.
     */
    protected Time schedule(Actor actor, Time environmentTime, Time deadline,
            Time executionTime) throws IllegalActionException { 
        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                    protected variables                    ////

    /** Contains the actors inside a ptides platform (=platforms). */
    protected List<NamedObj> _actors;
    /** True if in the last request to schedule an actor, this actor
     *  finished execution.
     */
    protected boolean _lastActorFinished;
    
    /** Last actor that finished execution.
     */
    protected Actor _lastActorThatFinished;

    /** The last time an actor's remaining time was updated due to a scheduling
     *  request.
     */
    protected HashMap<Actor, Time> _lastTimeScheduled;

    /** The remaining execution time for every actor that has been scheduled
     *  or null if the actor execution finished.
     */
    protected HashMap<Actor, Time> _remainingTimes;

    /** Listeners that want to be informed about rescheduling events.
     */
    protected List<ScheduleListener> _schedulePlotListeners;
    

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
                    if (_actors == null) {
                        _actors = new ArrayList<NamedObj>();
                    }
                    _actors.add(entity);
                    // Indicate that the actor is not running.
                    if (_remainingTimes == null) {
                        _remainingTimes = new HashMap<Actor, Time>();
                    }
                    _remainingTimes.put((Actor) entity, null);
                    notifyScheduleListeners(entity, 0.0, null);
                    
                } else if (entity instanceof CompositeActor) {
                    _getAllManagedEntities(((CompositeActor) entity)
                            .deepEntityList());
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    //                          private variables                    //

    private List<NamedObj> _getEntitiesToDecorate(CompositeEntity container) {
        List<NamedObj> toDecorate = new ArrayList<NamedObj>();
        List entities = container.entityList();
        for (Object entity : entities) {
            if (!(entity instanceof ResourceScheduler)) {
                toDecorate.add((NamedObj) entity);
                if (entity instanceof CompositeEntity) {
                    toDecorate.addAll(_getEntitiesToDecorate((CompositeEntity) entity));
                }
            }
        }
        return toDecorate;
    }

    
}
