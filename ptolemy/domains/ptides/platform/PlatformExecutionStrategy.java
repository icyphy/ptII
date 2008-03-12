package ptolemy.domains.ptides.platform;

import java.util.Hashtable;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.util.Time;
import ptolemy.domains.de.kernel.DEEvent;
import ptolemy.domains.ptides.kernel.PtidesEmbeddedDirector;
import ptolemy.kernel.util.IllegalActionException;

public abstract class PlatformExecutionStrategy {

	public static String BASIC_NON_PREEMPTIVE = "basic non-preemptive";
	
	protected Director _director;
	
	public abstract DEEvent getNextEventToFire(List actorsFiring, List eventsToFire) throws IllegalActionException;
	
	public abstract void sort(List list, Time physicalTime);
	
	public abstract boolean nothingToDoNow(List eventsToFire);
	
	public abstract boolean actorCanBeFired(Actor actorToFire, Time nextRealTimeEvent);
	
	public abstract boolean actorExecutionFinished(Actor actorToFire, Time fireTime);
	
	public abstract boolean missedExecution(Actor actorToFire, Time fireTime);
	
	public abstract boolean dealWithNewlyReceivedEvents(Actor actorToFire, Hashtable eventQueues);

	public void setPhysicalTime(Time time) {
		_physicalTime = time;
	}
	
	public Time _physicalTime;
	
	 protected final void _displaySchedule(Actor actor, double time,
	            int scheduleEvent) {
	        ((PtidesEmbeddedDirector)_director).displaySchedule(actor, time, scheduleEvent);
	    }
	
}
