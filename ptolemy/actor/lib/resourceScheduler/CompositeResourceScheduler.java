/* This is a composite resource scheduler.

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
import ptolemy.actor.ScheduleListener;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.ScheduleListener.ExecutionEventType;
import ptolemy.actor.gui.ColorAttribute; 
import ptolemy.actor.ResourceAttributes;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
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

/** This is a composite resource scheduler. Actors decorated by this 
 *  resource scheduler also specify a <i>requestPort</i>. A record 
 *  token with the actor and its execution time is 
 *  created on this requestPort whenever the director wants to schedule
 *  the actor. When this token is received by a ResourceMappingOutputport
 *  inside this composite resource scheduler, the director is informed 
 *  that the actor can now fire. 
 *
 * @author Patricia Derler
   @version $Id$
   @since Ptolemy II 0.2

   @Pt.ProposedRating Red (derler)
   @Pt.AcceptedRating Red (derler)
 */
public class CompositeResourceScheduler extends TypedCompositeActor implements ResourceScheduler {

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
    public CompositeResourceScheduler(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name); 
        ColorAttribute attribute = new ColorAttribute(this, "decoratorHighlightColor");
        attribute.setExpression("{0.0,0.8,0.0,1.0}"); 
        _requestPorts = new HashMap<Actor, String>();
        _previousY = new HashMap<NamedObj, Double>();
        
        justMonitor = new Parameter(this, "justMonitor");
        justMonitor.setTypeEquals(BaseType.BOOLEAN);
        justMonitor.setExpression("false");
        _justMonitor = false;
        
        _lastTimeScheduled = new HashMap<Actor, Time>(); 
        _schedulePlotListeners = new ArrayList<ScheduleListener>();
    }
    
    /** This parameter indicates whether the tokens received via the 
     *  ImmediateReceivers are immediately forwarded to the wrapped 
     *  receivers or whether they are delayed by this quantity manager
     *  and only forwarded through a CQMOutputPort. 
     *  This parameter is a boolean that defaults to false.
     */
    public Parameter justMonitor;
    
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

    /** React to the change of the <i>justMonitor</i> attribute by
     *  updating internal variables.
     *  @param attribute The attribute that changed.
     *  @throws IllegalActionException If token in attribute cannot
     *    be accessed.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == justMonitor) {
            _justMonitor = ((BooleanToken) justMonitor.getToken())
                    .booleanValue();
        }
        super.attributeChanged(attribute);
    }
    
    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        CompositeResourceScheduler newObject = (CompositeResourceScheduler) super
                .clone(workspace);  
        newObject._previousY = new HashMap<NamedObj, Double>();
        newObject._requestPorts = new HashMap<Actor, String>();
        newObject._lastTimeScheduled = new HashMap<Actor, Time>(); 
        newObject._actors = new ArrayList<NamedObj>();
        return newObject;
    }

    /** Return the decorated attributes for the target NamedObj.
     *  If the specified target is not an Actor, return null.
     *  @param target The NamedObj that will be decorated.
     *  @return The decorated attributes for the target NamedObj, or
     *   null if the specified target is not an Actor.
     */
    public DecoratorAttributes createDecoratorAttributes(NamedObj target) { 
        if (target instanceof Actor && !_isPartOfResourceScheduler(target)) {
            try {
                return new CompositeResourceSchedulerAttributes(target, this);
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
    
    /** Initialize local variables.
     * @exception IllegalActionException Thrown if list of actors
     *   scheduled by this scheduler cannot be retrieved.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _actors = new ArrayList<NamedObj>();  
        _currentlyExecuting = new ArrayList<Actor>();
        _lastActorFinished = false;
        _previousY.clear();
        _lastTimeScheduled.clear();
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
        return _currentlyExecuting.contains(actor);
    }

    /** If the last actor that was scheduled finished execution
     *  then this method returns true.
     *  @return True if last actor that was scheduled finished
     *   execution.
     */
    public boolean lastScheduledActorFinished() {
        return _lastActorFinished;
    }
    
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

    /** Iterate through resource mapping output ports and if they contain
     *  tokens, inform the director of the actors in the tokens that these
     *  actors can resume execution. 
     *  @throws illegalActionException Not explicitly thrown here.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        // TODO Auto-generated method stub
        boolean postfire = super.postfire();
        
        for (Object entity : entityList()) {
            if (entity instanceof ResourceMappingOutputPort) {
                ResourceMappingOutputPort outputPort = ((ResourceMappingOutputPort)entity);
                if (outputPort.hasToken() && outputPort.getToken() instanceof RecordToken) {
                    RecordToken recordToken = (RecordToken) outputPort.getToken();
                    if (recordToken.get("actor") != null && 
                            ((ObjectToken)recordToken.get("actor")).getValue() != null) {
                        Actor actor = (Actor) ((ObjectToken)recordToken.get("actor")).getValue();
                        notifyScheduleListeners((NamedObj) actor, getExecutiveDirector().getModelTime().getDoubleValue(), ExecutionEventType.STOP);
                        outputPort.takeToken();
                        _currentlyExecuting.remove(actor);
                        actor.getExecutiveDirector().resumeActor(actor);
                        _lastActorFinished = true;
                    }
                }
            }
        }
        
        return postfire;
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
        return getDirector().getModelNextIterationTime();
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

    /** Set the name of the port that will receive scheduling requests
     *  for the actor.
     *  @param actor The actor.
     *  @param portName The request port.
     */
    public void setRequestPort(Actor actor, String portName) {
        _requestPorts.put(actor, portName);
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
    
    /** Schedule a new actor for execution. Find the const
     *  actor in the _model that is mapped to this actor and
     *  trigger a firing of that one, if the actor is not
     *  already in execution. If the actor finished execution,
     *  return zero time, otherwise return the next time the
     *  model has something to do.
     *  @param actor The actor to be scheduled.
     *  @param currentPlatformTime The current platform time.
     *  @param deadline The deadline of the event.
     *  @param executionTime The execution time of the actor.
     *  @return Relative time when this Scheduler has to be executed
     *    again.
     *  @exception IllegalActionException Thrown if actor paramaters such
     *    as execution time or priority cannot be read.
     */
    
    
    protected Time _schedule(Actor actor, Time currentPlatformTime, Time deadline,
            Time executionTime) throws IllegalActionException {   
        _lastActorFinished = false; 
        // make sure that director has the correct time.
        getDirector().setModelTime(getExecutiveDirector().localClock.getLocalTime());
        
        // create token for scheduling requests and put them into ports.
        Time time = _lastTimeScheduled.get(actor);
        if ((_justMonitor && (time == null || !time.equals(currentPlatformTime))) 
                || !_currentlyExecuting.contains(actor)) {
            _lastTimeScheduled.put(actor, currentPlatformTime);
            notifyScheduleListeners((NamedObj) actor, getExecutiveDirector().localClock.getLocalTime().getDoubleValue(), ExecutionEventType.START);
            if (_requestPorts.get(actor) == null) {
                throw new IllegalActionException(this, "Actor " + actor + " does not have a" +
                		" registered requestPort");
            }
            ResourceMappingInputPort requestPort = (ResourceMappingInputPort) getEntity(_requestPorts.get(actor));
            if (requestPort != null) { 
                RecordToken recordToken = new RecordToken(
                        new String[]{"actor", "executionTime"}, 
                        new Token[]{new ObjectToken(actor), 
                                new DoubleToken(executionTime.getDoubleValue())});
                requestPort.value.setToken(recordToken);
                getDirector().fireAtCurrentTime(requestPort); 
                getExecutiveDirector().fireAt(this, getDirector().getModelTime());
                _currentlyExecuting.add(actor);
            } else {
                throw new IllegalActionException(this, "No request port with name "
                      + _requestPorts.get(actor));
            }
        } 
        if (_justMonitor) {
            _lastActorFinished = true;
            return new Time(getDirector(), 0.0);
        } else {
            // at this point we don't know how long it will take till the actor
            // finishes execution.
            return Time.POSITIVE_INFINITY;
        }
    }

    /** Contains the actors inside a ptides platform (=platforms). */
    protected List<NamedObj> _actors;

    /** True if in the last request to schedule an actor, this actor
     *  finished execution.
     */
    protected boolean _lastActorFinished;
    
    protected List<ScheduleListener> _schedulePlotListeners;

    /** List of currently executing actors. */
    protected List<Actor> _currentlyExecuting;

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

    private boolean _isPartOfResourceScheduler(NamedObj actor) {
        if (actor instanceof ResourceScheduler) {
            return true;
        }
        CompositeEntity container = (CompositeEntity) actor.getContainer();
        while (container != null) {
            if (container instanceof ResourceScheduler) {
                return true;
            }
            container = (CompositeEntity) container.getContainer();
        }
        return false;
    }

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

    /** Previous positions of the actor data set. */
    private HashMap<NamedObj, Double> _previousY;
    
    private HashMap<Actor, Time> _lastTimeScheduled;
    
    private boolean _justMonitor; 

    private HashMap<Actor, String> _requestPorts;

    /** Attributes for actors decorated by this composite resource
     *  scheduler. The attributes in this base class only contain 
     *  the name of the port that will receive scheduling requests
     *  from the director for the decorated actor.
     *  @author Patricia Derler
     */
    public static class CompositeResourceSchedulerAttributes extends
            ExecutionTimeResourceAttributes {

        /** Constructor to use when editing a model.
         *  @param target The object being decorated.
         *  @param decorator The decorator.
         *  @throws IllegalActionException If the superclass throws it.
         *  @throws NameDuplicationException If the superclass throws it.
         */
        public CompositeResourceSchedulerAttributes(NamedObj target,
                Decorator decorator)
                throws IllegalActionException, NameDuplicationException {
            super(target, decorator);
            _init();
        }

        /** Constructor to use when parsing a MoML file.
         *  @param target The object being decorated.
         *  @param name The name of this attribute.
         *  @throws IllegalActionException If the superclass throws it.
         *  @throws NameDuplicationException If the superclass throws it.
         */
        public CompositeResourceSchedulerAttributes(NamedObj target, String name)
                throws IllegalActionException, NameDuplicationException {
            super(target, name);
            _init();
        }

        ///////////////////////////////////////////////////////////////////
        ////                         parameters                        ////

        /** The name of the port that will receive scheduling requests for 
         *  the decorated actor.
         */
        public Parameter requestPort;

        /** If attribute <i>requestPort</i> report the new value 
         *  to the resource scheduler. 
         *  @param attribute The changed parameter.
         *  @exception IllegalActionException If the parameter set is not valid.
         *  Not thrown in this class.
         */
        public void attributeChanged(Attribute attribute)
                throws IllegalActionException {
            if (attribute == requestPort) {
                Actor actor = (Actor) getContainer();
                CompositeResourceScheduler scheduler = (CompositeResourceScheduler) getDecorator();
                String portName = ((StringToken) ((Parameter) attribute).getToken()).stringValue();
                if (scheduler != null && !portName.equals("") /*&& enabled()*/) {
                    scheduler.setRequestPort(actor, portName);
                }
            } else {
                super.attributeChanged(attribute);
            }
        }

        /** Add names of available CQMInputPort in CompositeQM as
         *  choices to inputPort.
         *  @exception InteralErrorException Thrown if CompositeQM
         *    cannot be accessed.  
         */
        @Override
        public void updateContent() throws InternalErrorException { 
            super.updateContent();
            try {
                if (getDecorator() != null) {
                    requestPort.removeAllChoices();
                 
                    List cqmInputPorts = ((CompositeResourceScheduler)getDecorator()).entityList(ResourceMappingInputPort.class);
                    for (Object cqmInputPort : cqmInputPorts) {
                        String name = ((ResourceMappingInputPort)cqmInputPort).getName(); 
                        requestPort.addChoice(name); 
                    }  
                }
            } catch (IllegalActionException e) {
                throw new InternalErrorException(e);
            }
        } 
        
        ///////////////////////////////////////////////////////////////////
        ////                        private methods                    ////

        /** Create the parameters.
         */
        private void _init() {
            try {
                requestPort = new StringParameter(this, "requestPort");
            } catch (KernelException ex) {
                // This should not occur.
                throw new InternalErrorException(ex);
            }
        }
    }

}
