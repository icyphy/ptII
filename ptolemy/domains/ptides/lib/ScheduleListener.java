package ptolemy.domains.ptides.lib;

import java.util.Hashtable;

import ptolemy.actor.Actor;

//////////////////////////////////////////////////////////////////////////
//// ScheduleListener

/**
 * A schedule listener reacts to given events.
 * 
 * @author Patricia Derler
 */
public interface ScheduleListener {
	// /////////////////////////////////////////////////////////////////
	// // public methods ////

	/**
	 * react to the given event.
	 */
	public void event(Actor node, Actor actor, double time, int scheduleEvent);

	/**
	 * initialize the legend of the display
	 * 
	 * @param nodesActors
	 *            contains platforms and actors running on that platform
	 */
	public void initialize(Hashtable nodesActors);

	static final int START = 0;

	static final int STOP = 1;

	static final int TRANSFEROUTPUT = 2;

	static final int TRANSFERINPUT = 3;

	static final int MISSEDEXECUTION = 4;

}
