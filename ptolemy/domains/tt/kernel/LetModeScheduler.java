package ptolemy.domains.tt.kernel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import ptolemy.domains.tt.tdl.kernel.MathUtilities;

/**
 * The TTModeScheduler takes care of scheduling tasks accoring to the logical
 * execution time principle. The period for creating the schedule is the least
 * common multiple of the task's invocation periods.
 * 
 * The generated mode schedule is a hashmap of the form:
 * -------------------------------------------------------------------
 * | Keys | Values .................................................. | 
 * | ---------------------------------------------------------------- | 
 * | time as long . | ----------------------------------------------- | 
 * | ............ . | | Keys ........... | Values ................. | | 
 * | .............. | | ------------------------------------------- | | 
 * | .............. | | name of action | ArrayList  ............... | | 
 * | .............. | | .............. | -------------------------- | | 
 * | .............. | | .............. | | action1 | action2 | .... | | 
 * | .............. | | .............. | | ------------------------ | | 
 * | ...............| ----------------------------------------------- |
 * --------------------------------------------------------------------
 * 
 * to ease calculations with time, the long value (time as double /
 * timeresolution of director) is used.
 * 
 * Name of action and actions in the associated list are - output port update
 * with output ports as actions - input port update and execution with input
 * ports and actors as actions.
 * 
 * @author Patricia Derler
 */
public class LetModeScheduler {

	/**
	 * create a new LET mode scheduler.
	 */
	public LetModeScheduler() {
		_modeSchedule = new HashMap();
	}

	/** Least common multiple of all task's invocation periods. */
	public long lcmPeriod;

	/**
	 * add a TTTask to the collection of tasks, only create the schedule when
	 * all tasks are added.
	 * 
	 * @param task
	 *            Task to be added.
	 */
	public void addTask(LetTask task) {
		_tasks.add(task);
	}

	/**
	 * Return a new mode schedule.
	 * 
	 * @return mode schedule as a hashmap.
	 * @throws TTModeSchedulerException
	 *             Thrown if the mode schedule cannot be generated.
	 */
	public Object getModeSchedule() throws TTModeSchedulerException {
		if (_tasks.size() == 0)
			return null;
		_calculateLcmPeriod();
		_scheduleAllTasks("regular task");
		return _modeSchedule;
	}

	// ////////////////////////////////////////////////////////////////////////////
	// private

	/**
	 * Schedule a single task by inserting output ports, input ports and the
	 * actor that performs execution for the task in the hashmap.
	 * 
	 * @param task
	 *            The task to be scheduled.
	 * @param taskName
	 *            The name of the task to be scheduled.
	 */
	private void _scheduleTask(LetTask task, String taskName) {
		// action names
		String taskInputPortUpdateAndExecution = taskName
				+ " input port update and execution";
		String taskOutputPortUpdate = taskName + " output port update";

		for (long j = task.getOffset(); j < lcmPeriod; j = j
				+ task.getInvocationPeriod()) {
			long startOfLet = j;
			long endOfLet = (j + task.getLet()) % lcmPeriod;

			if (_modeSchedule.get(startOfLet) == null)
				_modeSchedule.put(startOfLet, new HashMap());
			if (_modeSchedule.get(endOfLet) == null)
				_modeSchedule.put(endOfLet, new HashMap());

			if (((HashMap) _modeSchedule.get(startOfLet))
					.get(taskInputPortUpdateAndExecution) == null)
				((HashMap) _modeSchedule.get(startOfLet)).put(
						taskInputPortUpdateAndExecution, new ArrayList());
			if (((HashMap) _modeSchedule.get(endOfLet))
					.get(taskOutputPortUpdate) == null)
				((HashMap) _modeSchedule.get(endOfLet)).put(
						taskOutputPortUpdate, new ArrayList());

			((ArrayList) ((HashMap) _modeSchedule.get(startOfLet))
					.get(taskInputPortUpdateAndExecution)).add(0, task
					.getActor());
			((ArrayList) ((HashMap) _modeSchedule.get(startOfLet))
					.get(taskInputPortUpdateAndExecution)).addAll(0, task
					.getActor().inputPortList());
			((ArrayList) ((HashMap) _modeSchedule.get(endOfLet))
					.get(taskOutputPortUpdate)).addAll(
					((ArrayList) ((HashMap) _modeSchedule.get(endOfLet))
							.get(taskOutputPortUpdate)).size(), task.getActor()
							.outputPortList());

		}
	}

	/**
	 * Generate a schedule for all tasks in the list.
	 * 
	 * @param taskName
	 *            Becomes part of the action name.
	 */
	private void _scheduleAllTasks(String taskName) {
		LetTask task;
		Iterator it = _tasks.iterator();
		while (it.hasNext()) {
			task = (LetTask) it.next();
			_scheduleTask(task, taskName);
		}
	}

	/**
	 * Least common multiple of all tasks in the list.
	 */
	private void _calculateLcmPeriod() {
		long lcm;
		LetTask task = null;
		Iterator it = _tasks.iterator();
		if (it.hasNext())
			task = (LetTask) it.next();
		lcm = task.getInvocationPeriod();
		while (it.hasNext()) {
			task = (LetTask) it.next();
			lcm = lcm
					* (task.getInvocationPeriod() / MathUtilities.gcd(lcm, task
							.getInvocationPeriod()));
		}
		lcmPeriod = lcm;
	}

	/**
	 * Tasks that can be scheduled according to the logical execution time.
	 */
	private Collection _tasks = new ArrayList();

	/** Contains scheduled tasks. */
	private HashMap _modeSchedule;

}
