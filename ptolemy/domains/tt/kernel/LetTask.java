package ptolemy.domains.tt.kernel;

import ptolemy.actor.Actor;

/**
 * A TTTask (time triggered task) is an actor with an invocationPeriod and a
 * logical execution time.
 * 
 * @author Patricia Derler
 */
public class LetTask {

	/**
	 * Create a new LET task.
	 * 
	 * @param actor
	 *            The LET task.
	 * @param let
	 *            The LET of the task.
	 * @param invocationPeriod
	 *            The period of invocation of the task.
	 * @param start
	 *            The start time of the task.
	 */
	public LetTask(Actor actor, long let, long invocationPeriod, long start) {
		_invocationPeriod = invocationPeriod;
		_let = let;
		_actor = actor;
		_offset = start;
	}

	/**
	 * Return the invocation period of the task.
	 * 
	 * @return the incovationPeriod.
	 */
	public long getInvocationPeriod() {
		return _invocationPeriod;
	}

	/**
	 * Return the LET of the task.
	 * 
	 * @return the LET.
	 */
	public long getLet() {
		return _let;
	}

	/**
	 * Return the actor representing the task.
	 * 
	 * @return the task actor.
	 */
	public Actor getActor() {
		return _actor;
	}

	/**
	 * Return the offset of the task.
	 * 
	 * @return the offset.
	 */
	public long getOffset() {
		return _offset;
	}

	/**
	 * The invocation period of a task specifies the amount of time that passes
	 * before the task needs to be executed again.
	 */
	private long _invocationPeriod;

	/**
	 * The logical execution time is the logical time required by the task for
	 * execution. At the beginning of the logical execution time, input ports
	 * are updated and the task execution is started. At the end of the logical
	 * execution time, output ports of the task are updated.
	 */
	private long _let;

	/** performs the task execution, */
	private Actor _actor;

	/**
	 * the offset of the task specifies the time the task is first invoked.
	 * After this first invocation, the task is invoked periodically.
	 */
	private long _offset;

}
