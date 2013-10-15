/* A fixed priority scheduler resource managers.

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

package ptolemy.actor.lib.aspect;

import java.util.HashSet;
import java.util.Stack;

import ptolemy.actor.Actor;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.ExecutionAspectListener.ExecutionEventType;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.graph.Inequality;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.DecoratorAttributes;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

/**
This is a fixed priority scheduler resource manager on a single processor or core.
This attribute decorates actors in the model at the same and lower levels of the
hierarchy, including those inside opaque composite actors. It decorates them
with the following parameters:
<ul>
<li> <i>enable</i>: If true, then the decorated actor will use this resource.
     This is a boolean that defaults to false.
<li> <i>executionTime</i>: Specifies the execution time of the
     decorated actor. This means the time that the decorated actor occupies
     the processor or core when it fires.
     This is a double that defaults to 0.0.
<li> <i>priority</i>: An integer where a lower number indicates a higher priority.
     E.g., priority 1 means higher priority than priority 2.
     Priority 0 means higher priority than any positive number.
     Priority -1 means higher priority than any non-negative number.
</ul>

For usage and supported directors {@link ptolemy.actor.ActorExecutionAspect}.

When using this ExecutionAspect in the Ptides domain, the platform
time in Ptides will be used to schedule execution times. When
 the Ptides director requests that an actor fire, if this resource is
free, it will immediately schedule it. Otherwise, it will queue it to be
executed when the resource becomes free. When the resource becomes free,
the actor with the highest priority (the lower priority number) will
be chosen from the queue to be executed. If two actors have the same
highest priority, then they will be executed in FIFO order.

@author Patricia Derler
@author Edward A. Lee
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Yellow (eal)
@Pt.AcceptedRating Red (derler)
*/
public class FixedPriorityScheduler extends AtomicExecutionAspect {

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
    public FixedPriorityScheduler(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        preemptive = new Parameter(this, "preemptive");
        preemptive.setTypeEquals(BaseType.BOOLEAN);
        preemptive.setExpression("true");
        _preemptive = true;
    }
    
    /** Parameter to configure whether a preemptive or non-preemptive
     *  scheduling strategy should be used. The default value is the
     *  boolean value true.
     */
    public Parameter preemptive;

    /** Lowest task priority. */
    public static int LOWEST_PRIORITY = Integer.MAX_VALUE; 
    
    /** If the attribute is <i>preemptive</i> then change the
     *  scheduling algorithm to be preemptive or non-preemptive.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (not thrown in this base class).
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == preemptive) {
            _preemptive = ((BooleanToken)preemptive.getToken()).booleanValue();
        }
    }
    
    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        FixedPriorityScheduler newObject = (FixedPriorityScheduler) super.clone(workspace);
        newObject._currentlyExecuting = new Stack<Actor>();
        newObject._preemptive = true;

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
                return new PriorityResourceAttributes(target, this);
            } catch (KernelException ex) {
                // This should not occur.
                throw new InternalErrorException(ex);
            }
        } else {
            return null;
        }
    }
    
    

    /** Initialize local variables.
     *  @exception IllegalActionException Thrown in super class.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _currentlyExecuting = new Stack(); 
    }

    /** Perform rescheduling actions when no new actor requests to be
     *  scheduled.
     * @param environmentTime The outside time.
     * @return Relative time when this aspect has to be executed
     *    again to perform rescheduling actions.
     * @exception IllegalActionException Thrown in subclasses.   
     */
    @Override
    public Time schedule(Time environmentTime) throws IllegalActionException {
        Time time = Time.POSITIVE_INFINITY;
        if (_currentlyExecuting.size() > 0) {
            Actor actor = _currentlyExecuting.peek();
            time = schedule(actor, environmentTime, null, null);
            if (_lastActorThatFinished == actor && lastActorFinished()) { 
                actor.getDirector().resumeActor(actor);
            }
        } 
        return time;
    }
    
    /** Schedule a new actor for execution and return the next time
     *  this aspect has to perform a reschedule.
     *  @param actor The actor to be scheduled.
     *  @param currentPlatformTime The current platform time.
     *  @param deadline The event deadline - not used here.
     *  @param executionTime The execution time of the actor.
     *  @return Relative time when this aspect has to be executed
     *    again.
     *  @exception IllegalActionException Thrown if actor parameters such
     *    as execution time or priority cannot be read.
     */
    @Override
    public Time schedule(Actor actor, Time currentPlatformTime,
            Time deadline, Time executionTime) throws IllegalActionException {
        super.schedule(actor, currentPlatformTime, deadline, executionTime);
        _lastActorFinished = false;
        Time remainingTime = null;
        if (_currentlyExecuting.size() == 0) {
            scheduleNewActor(actor, currentPlatformTime, executionTime);
            remainingTime = executionTime;
        } else {
            Actor executing = _currentlyExecuting.peek();
            Time lasttime = _lastTimeScheduled.get(executing);
            Time timePassed = currentPlatformTime.subtract(lasttime);
            remainingTime = _remainingTimes.get(executing).subtract(timePassed);
            _remainingTimes.put(executing, remainingTime);
            if (!_currentlyExecuting.contains(actor) && executing != actor) {
                if (_preemptive) {
                    double executingPriority = _getPriority(executing);
                    double newActorPriority = _getPriority(actor);
                    if (newActorPriority < executingPriority) {
                        if (remainingTime.getDoubleValue() == 0.0) {
                        	notifyExecutionListeners((NamedObj) _currentlyExecuting.peek(),
                                    currentPlatformTime.getDoubleValue(),
                                    ExecutionEventType.STOP);
                        } else {
                        	notifyExecutionListeners((NamedObj) executing,
                                    currentPlatformTime.getDoubleValue(),
                                    ExecutionEventType.PREEMPTED);
                        } 
                        remainingTime = executionTime; 
                        scheduleNewActor(actor, currentPlatformTime, executionTime);
                    } else {
                        _add(actor, executionTime);
                        // add event somewhere
                    }
                } else {
                    Object[] actors = _currentlyExecuting.toArray();
                    _currentlyExecuting.clear();
                    for (int j = 0; j < actors.length; j++) {
                        _currentlyExecuting.push((Actor) actors[j]);
                    }
                    _currentlyExecuting.push(actor);
                    _remainingTimes.put(actor, executionTime);
                }
            }
            for (Actor preemptedActor : _currentlyExecuting) {
                _lastTimeScheduled.put(preemptedActor, currentPlatformTime);
            }
        }

        if (remainingTime.getDoubleValue() == 0.0
                && _currentlyExecuting.peek() == actor) {
        	notifyExecutionListeners((NamedObj) _currentlyExecuting.peek(),
                    currentPlatformTime.getDoubleValue(),
                    ExecutionEventType.STOP);
            _remainingTimes.put(_currentlyExecuting.peek(), null);
            _currentlyExecuting.pop();
            if (_currentlyExecuting.size() > 0) {
                remainingTime = _remainingTimes.get(_currentlyExecuting.peek());
                if (remainingTime.getDoubleValue() > 0.0) {
                	notifyExecutionListeners((NamedObj) _currentlyExecuting.peek(),
                            currentPlatformTime.getDoubleValue(),
                            ExecutionEventType.START);
                }
            }
            _lastActorFinished = true;
            _lastActorThatFinished = actor;
        }
         return remainingTime;

    }
    
    

    ///////////////////////////////////////////////////////////////////
    //                      protected methods                        //

    /** Get the priority of the actor.
     *  @param actor The actor.
     *  @return The priority of the actor or, if the actor has no priority
     *    assigned, the lowest priority.
     *  @exception IllegalActionException Thrown if parameter cannot be read.
     */
    protected double _getPriority(Actor actor) throws IllegalActionException {
        return ((IntToken)((Parameter)((NamedObj)actor).getDecoratorAttribute(this, "priority")).getToken()).intValue();
    }

    ///////////////////////////////////////////////////////////////////
    //                    protected variables                        //

    /** Stack of currently executing actors. */
    protected Stack<Actor> _currentlyExecuting;

    ///////////////////////////////////////////////////////////////////
    //                        private methods                        //

    private void _add(Actor actor, Time executionTime) throws IllegalActionException {
        double priority = _getPriority(actor);  
        boolean added = false;
        Object[] actors = _currentlyExecuting.toArray();  
        _currentlyExecuting.clear();
        for (int i = 0; i < actors.length; i++) {
            Actor actorInArray = (Actor) actors[i];
            double actorInArrayPriority = _getPriority(actorInArray); 
            if (!added && priority >= actorInArrayPriority) { // has lower priority  
                _currentlyExecuting.push(actor);
                _remainingTimes.put(actor, executionTime); 
                added = true;
            } 
            _currentlyExecuting.push(actorInArray);
        }    
    }
    
    /** Schedule a new actor which possibly preempts currently executing
     *  actors.
     *  @param actor The actor.
     *  @param currentPlatformTime The current platform time when the preemption occurs.
     *  @param executionTime The execution time of the actor.
     */
    private void scheduleNewActor(Actor actor, Time currentPlatformTime,
            Time executionTime) {
        _currentlyExecuting.push(actor);
        notifyExecutionListeners((NamedObj) actor, currentPlatformTime.getDoubleValue(),
                    ExecutionEventType.START); 
        _remainingTimes.put(actor, executionTime);
        _lastTimeScheduled.put(actor, currentPlatformTime);
    }
    
    /** True if preemptive scheduling strategy should be used. 
     */
    private boolean _preemptive;

}
