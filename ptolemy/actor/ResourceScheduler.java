package ptolemy.actor;

import ptolemy.actor.util.Time;
import ptolemy.kernel.util.Decorator;
import ptolemy.kernel.util.IllegalActionException;

public interface ResourceScheduler extends Decorator {

    public boolean lastScheduledActorFinished();
    
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
            throws IllegalActionException;
    
    /** Perform rescheduling actions when no new actor requests to be
     *  scheduled.
     * @param environmentTime The outside time.
     * @return Relative time when this Scheduler has to be executed
     *    again to perform rescheduling actions.
     * @exception IllegalActionException Thrown in subclasses.   
     */
    public Time schedule(Time environmentTime) throws IllegalActionException;
    
    public boolean isCurrentlyExecuting(Actor actor);
    
}
