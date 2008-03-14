package ptolemy.domains.ptides.platform;

import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.util.Time;
import ptolemy.domains.de.kernel.DEEvent;
import ptolemy.domains.ptides.kernel.PtidesEmbeddedDirector;
import ptolemy.kernel.util.IllegalActionException;

/**
 * This abstract class provides all methods that need to be implemented for a
 * specific platform execution strategy.
 * 
 * For every execution strategy, a name has to be defined which can be used in
 * the execution strategy property field of the PtidesEmbeddedDirector.
 * 
 * @author Patricia Derler
 * 
 */
public abstract class PlatformExecutionStrategy {

	/**
	 * The basic non-preemptive execution strategy is implemented by
	 * NonPreemptivePlatformExecutionStrategy.
	 */
	public static String BASIC_NON_PREEMPTIVE = "basic non-preemptive";

	/**
	 * Director for this platform.
	 */
	protected Director _director;

	/**
	 * Selects one event out of a set of events that are safe to fire. This one
	 * event can be fired or start firing.
	 * 
	 * @param actorsFiring
	 *            List of actors that are currently being fired. If this list
	 *            contains more than one element, than all actors except for the
	 *            last actor are preempted. If this method returns a new event,
	 *            then also the last actor in the list will be preempted and the
	 *            new actor will be added to actorsFiring.
	 * @param eventsToFire
	 *            List of events that are safe to fire. This list is computed by
	 *            the PtidesEmbeddedDirector.
	 * @return event that can be fired. if no event can be fired then it returns
	 *         null
	 * @throws IllegalActionException
	 */
	public abstract DEEvent getNextEventToFire(List actorsFiring,
			List eventsToFire) throws IllegalActionException;

	/**
	 * set the physical time.
	 * 
	 * @param time
	 */
	public void setPhysicalTime(Time time) {
		_physicalTime = time;
	}

	/**
	 * the execution strategy needs to know about the pysical time, it should
	 * only be set by the PtidesEmbeddedDirector and not changed by the
	 * Executionstrategy.
	 */
	public Time _physicalTime;

	/**
	 * forward an event that should be displayed by a schedulelistener
	 * 
	 * @param actor
	 * @param time
	 * @param scheduleEvent
	 */
	protected final void _displaySchedule(Actor actor, double time,
			int scheduleEvent) {
		((PtidesEmbeddedDirector) _director).displaySchedule(actor, time,
				scheduleEvent);
	}

}
