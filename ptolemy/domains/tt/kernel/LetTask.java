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
	 * create a new LET task
	 * 
	 * @param actor
	 * @param let
	 * @param invocationPeriod
	 * @param start
	 */
	public LetTask(Actor actor, long let, long invocationPeriod, long start) {
		_invocationPeriod = invocationPeriod;
		_let = let;
		_actor = actor;
		_offset = start;
	}

	public long getInvocationPeriod() {
		return _invocationPeriod;
	}

	public long getLet() {
		return _let;
	}

	public Actor getActor() {
		return _actor;
	}

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

	/** performs the task execution */
	private Actor _actor;

	private long _offset;

}
