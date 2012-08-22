/* This is a fixed priority scheduler.

@Copyright (c) 2008-2011 The Regents of the University of California.
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

package ptolemy.domains.ptides.lib;

import java.util.Stack;

import ptolemy.actor.Actor;
import ptolemy.actor.util.Time; 
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter; 
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;


/** This is a fixed priority scheduler.
 * 
 * @author Patricia Derler
   @version $Id$
   @since Ptolemy II 0.2

   @Pt.ProposedRating Red (derler)
   @Pt.AcceptedRating Red (derler)
 */ 
public class FPScheduler extends ResourceScheduler {

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
    public FPScheduler(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);   
    }
    
    /** Lowest task priority. */
    public static int LOWEST_PRIORITY = Integer.MAX_VALUE;
    
    /** Initialize local variables. 
     *  @throws IllegalActionException Thrown in super class.
     */
    @Override
    public void initialize() throws IllegalActionException { 
        super.initialize();
        _currentlyExecuting = new Stack();
    }
    
    /** Schedule a new actor for execution and return the next time
     *  this scheduler has to perform a reschedule.
     *  @param actor The actor to be scheduled.
     *  @param currentPlatformTime The current platform time.
     *  @return Relative time when this Scheduler has to be executed
     *    again.
     *  @throws IllegalActionException Thrown if actor paramaters such
     *    as execution time or priority cannot be read.
     */
    @Override 
    public Time schedule(Actor actor, Time currentPlatformTime) throws IllegalActionException {
        _lastActorFinished = false;
        event(this, currentPlatformTime.getDoubleValue(), ExecutionEventType.START);
        event(this, currentPlatformTime.getDoubleValue(), ExecutionEventType.STOP);
        
        
        Time executionTime = null;
        Double executionTimeDouble = _executionTimes.get(actor);
        if (executionTimeDouble == null) {
            executionTime = getTime(0.0);
        } else {
            executionTime = getTime(executionTimeDouble);
        }
        
        Time remainingTime = null;
        if (_currentlyExecuting.size() == 0) {
            scheduleNewActor(actor, currentPlatformTime, executionTime);
            remainingTime = executionTime; 
        } else {
            Actor executing = _currentlyExecuting.peek();
            Time lasttime = _lastTimeScheduled.get(executing);
            Time timePassed = currentPlatformTime.subtract(lasttime);
            remainingTime = _remainingTimes.get(executing).subtract(timePassed); 
            if (remainingTime.getDoubleValue() < 0) {
                throw new IllegalActionException("");
            }
            _remainingTimes.put(executing, remainingTime); 
            if (!_currentlyExecuting.contains(actor) && executing != actor) { 
                int executingPriority = _getPriority(executing);
                int newActorPriority = _getPriority(actor);
                if (newActorPriority < executingPriority) { 
                    remainingTime = executionTime; 
                    event(executing, currentPlatformTime.getDoubleValue(), ExecutionEventType.PREEMPTED);
                    scheduleNewActor(actor, currentPlatformTime, executionTime);
                }
            } 
            for (Actor preemptedActor : _currentlyExecuting) { 
                _lastTimeScheduled.put(preemptedActor, currentPlatformTime);
            }
        }
        
        if (remainingTime.getDoubleValue() == 0.0 && _currentlyExecuting.peek() == actor) { 
            event(_currentlyExecuting.peek(), currentPlatformTime.getDoubleValue(), ExecutionEventType.STOP);
            _remainingTimes.put(_currentlyExecuting.peek(), null);
            
            _currentlyExecuting.pop();
            if (_currentlyExecuting.size() > 0) {
                remainingTime = _remainingTimes.get(_currentlyExecuting.peek());
                event(_currentlyExecuting.peek(), currentPlatformTime.getDoubleValue(), ExecutionEventType.START);
            }
            _lastActorFinished = true;
        }
        return remainingTime;
        
    }
    
    ///////////////////////////////////////////////////////////////////
    //                      protected methods                        //
    
    /** Get the priority of the actor.
     *  @param actor The actor.
     *  @return The priority of the actor or, if the actor has no priority
     *    assigned, the lowest priority. 
     *  @throws IllegalActionException Thrown if parameter cannot be read.
     */
    protected static int _getPriority(Actor actor) throws IllegalActionException {
        Parameter parameter = (Parameter) ((NamedObj)actor).getAttribute("priority");
        if (parameter != null) {
            return Integer.valueOf(((IntToken) parameter.getToken())
                    .intValue());
        }
        return LOWEST_PRIORITY;
    }
    
    ///////////////////////////////////////////////////////////////////
    //                        private methods                        //
    
    /** Schedule a new actor which possibly preempts currently executing
     *  actors.
     *  @param actor The actor.
     *  @param currentPlatformTime The current platform time when the preemption occurs.
     *  @param executionTime The execution time of the actor.
     */
    private void scheduleNewActor(Actor actor, Time currentPlatformTime, Time executionTime) {
        _currentlyExecuting.push(actor);
        event(actor, currentPlatformTime.getDoubleValue(), ExecutionEventType.START);
        _remainingTimes.put(actor, executionTime);
        _lastTimeScheduled.put(actor, currentPlatformTime); 
    }
    
    ///////////////////////////////////////////////////////////////////
    //                      private variables                        //
    
    /** Stack of currently executing actors. */
    private Stack<Actor> _currentlyExecuting;
    
    
}
