package ptolemy.domains.tt.tdl.kernel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.fsm.kernel.Transition;
import ptolemy.domains.tt.kernel.LetModeScheduler;
import ptolemy.domains.tt.kernel.LetTask;
import ptolemy.domains.tt.kernel.TTModeSchedulerException;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

/**
 * The TDLModeScheduler uses the TTModeScheduler to schedule tasks and 
 * additionaly schedules 
 * - actuators
 * - module output ports
 * - fast tasks
 * - mode switches
 * 
 * The generated schedule has the following form: 
 * 
 *           ---------------------------------------------
 *          | Keys         | Values                       |
 *          |---------------------------------------------|
 *          | time as long | ArrayList                    |
 *          |              |  -------------------------   |
 *          |              | | action1 | action2 | ... |  |
 *          |              |  -------------------------   |
 *           ---------------------------------------------
 *           
 *  actions are sorted according to the following list:
 *  - regular task output port update
 *  - update actuators
 *  - calculate mode switch
 *  - fast task
 *  - regular task input port update and execution
 * 
 *  @author Patricia Derler
 */
public class TDLModeScheduler extends LetModeScheduler {

	public TDLModeScheduler() {
		super();
	}

	/**
	 * add actuator to the list but do not schedule until all
	 * actuators are added
	 * @param port
	 * @throws IllegalActionException 
	 * @throws TDLModeSchedulerException 
	 */
	public void addActuator(IOPort port) throws IllegalActionException,
			TDLModeSchedulerException {

		_actuatorUpdates.add(port);
	}

	/** 
	 * add transition = mode switch to the list but do not
	 * schedule untill all mode switches are added
	 * 
	 * this mode switch is executed with a frequency, not asap
	 */
	public void addModeSwitch(Transition transition) {
		_transitions.add(transition);
	}

	/**
	 * add task to list of tasks in TTModeScheduler
	 * @param actor
	 * @throws IllegalActionException 
	 * @throws TDLModeSchedulerException 
	 */
	public void addTask(Actor actor) throws IllegalActionException,
			TDLModeSchedulerException {
		int frequency = TDLModeScheduler.getFrequency((NamedObj) actor);
		Parameter parameter = (Parameter) ((NamedObj) actor)
				.getAttribute("slots");
		if (parameter != null) {
			String token = parameter.getExpression();
			_analyzeSlotSelection(actor, token, frequency);
		}
	}

	/**
	 * in a fast actuator, input ports, output ports and actuators 
	 * are all executed/updated at once in logical zero time, therefore
	 * add them as a unit in the correct order to the list
	 * @param actor
	 * @param actuators
	 */
	public void addFastTask(Actor actor, Collection actuators) {
		ArrayList list = new ArrayList();
		list.addAll(actor.inputPortList());
		list.add(actor);
		list.addAll(actor.outputPortList());
		if (actuators != null)
			list.addAll(actuators);
		_fastTasks.add(list);
	}

	/**
	 * get schedule for tasks from TTModeScheduler and schedule additionally
	 * actuators, module output ports, fast tasks and mode switches
	 */
	public TDLModeSchedule getModeSchedule() {

		HashMap ttModeSchedule = null;
		try {
			ttModeSchedule = (HashMap) super.getModeSchedule();
			if (lcmPeriod == 0)
				lcmPeriod = _modePeriod;
			_createTDLSchedule();
		} catch (TTModeSchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IllegalActionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return new TDLModeSchedule(_modePeriod, _mergeSchedules(ttModeSchedule));
	}

	/**
	 * Return the frequency of task.
	 * The default value is 1 if there is no frequency attribute defined.
	 * @param obj
	 * @return The frequency of the task.
	 */
	public static int getFrequency(NamedObj obj) {
		try {
			Parameter parameter = (Parameter) obj.getAttribute("frequency");

			if (parameter != null) {
				IntToken intToken = (IntToken) parameter.getToken();

				return intToken.intValue();
			} else {
				return 1;
			}
		} catch (ClassCastException ex) {
			return 1;
		} catch (IllegalActionException ex) {
			return 1;
		}
	}

	public static String getSlots(NamedObj obj) {
		try {
			Parameter parameter = (Parameter) obj.getAttribute("slots");

			if (parameter != null) {
				StringToken token = (StringToken) parameter.getToken();

				return token.stringValue();
			} else {
				return "";
			}
		} catch (ClassCastException ex) {
			return "";
		} catch (IllegalActionException ex) {
			return "";
		}
	}

	/**
	 * set the mode period 
	 * to avoid problems when doing calculations with doubles, the long value of 
	 * the time is used: double value / resolution of director
	 * @param modePeriod
	 * @param resolution
	 */
	public void setModePeriod(double modePeriod, double resolution) {
		_modePeriod = (long) (modePeriod / resolution);
		_modeSchedule = new HashMap();
	}

	//////////////////////////////////////////////////////////////////////////////
	// private

	/**
	 * analyze the slot selection string. for the documentation about the slot selection
	 * string look at the definition of TDL
	 * @param actor
	 * @param slots
	 * @param frequency
	 * @throws TDLModeSchedulerException
	 */
	private void _analyzeSlotSelection(Actor actor, String slots, int frequency)
			throws TDLModeSchedulerException {
		ArrayList invocations = _getInvocations(slots, frequency);
		// if task is periodic, it is a let task. otherwise schedule it as a special action
		Long offset, let = new Long(0), inv = new Long(0);
		boolean periodic = true;
		// frequency must be dividable by amount of invocations without rest
		long newlet = 0;
		long newInv = 0;
		if (frequency % (invocations.size() / 2) != 0) {
			periodic = false;
		} else {
			// lets must be the same
			for (int i = 0; i < invocations.size(); i += 2) {
				newlet = Math.abs((Integer) invocations.get(i + 1)
						- (Integer) invocations.get(i));
				if (let == 0)
					let = newlet;
				else if (newlet != let)
					periodic = false;
			}
			// invocation periods must be the same
			if (invocations.size() > 2) {
				for (int i = 0; i < invocations.size(); i += 2) {
					if (invocations.size() > i + 2) {
						newInv = (Integer) invocations.get(i + 2)
								- (Integer) invocations.get(i);
					} else
						newInv = (Integer) invocations.get(0) + frequency;
					if (inv == 0)
						inv = newInv;
					else if (newInv != inv)
						periodic = false;
				}
			} else {
				inv = new Long(1);
			}
		}
		if (periodic) {
			offset = (_modePeriod / frequency)
					* ((Integer) invocations.get(0) - 1);
			let = _modePeriod / let;
			inv = _modePeriod / inv;
			super.addTask(new LetTask(actor, let, inv, offset));
		} else { // schedule single task as a set of tasks with different lets and invocation periods
			for (int i = 0; i < invocations.size(); i += 2) {
				let = _modePeriod
						/ frequency
						* ((Integer) invocations.get(i + 1) - (Integer) invocations
								.get(i));
				offset = _modePeriod / frequency
						* ((Integer) invocations.get(i) - 1);
				super.addTask(new LetTask(actor, let, _modePeriod, offset));
			}
		}
	}

	/**
	 * create tdl schedule without considering regular tasks
	 * @throws IllegalActionException 
	 */
	private void _createTDLSchedule() throws TDLModeSchedulerException,
			IllegalActionException {
		Iterator it = _actuatorUpdates.iterator();
		while (it.hasNext()) {
			IOPort port = (IOPort) it.next();
			int frequency = getFrequency(port);
			String slots = getSlots(port);

			ArrayList invocations = _getInvocations(slots, frequency);
			for (int i = 1; i < invocations.size(); i += 2) {
				long time = (_modePeriod / frequency * ((Integer) invocations
						.get(i) - 1))
						% _modePeriod;
				_scheduleSingleActivity("update actuators", port, time);
			}
		}

		it = _transitions.iterator();
		while (it.hasNext()) {
			Transition transition = (Transition) it.next();
			long invocationPeriod = _modePeriod
					/ TDLModeScheduler.getFrequency((NamedObj) transition);
			if (invocationPeriod % super.lcmPeriod != 0)
				throw new TDLModeSchedulerException(
						"Mode Switch can only take place when no task is running.");
			_scheduleActivity("calculate mode switch", transition,
					getFrequency(transition));
		}

		it = _fastTasks.iterator();
		while (it.hasNext()) {
			List list = (List) it.next();
			Actor actor = null;
			Iterator fastTaskIterator = list.iterator();
			while (fastTaskIterator.hasNext()) {
				Object o = fastTaskIterator.next();
				if (o instanceof Actor)
					actor = (Actor) o;
			}
			_scheduleAtomicActivities("fast task", list,
					getFrequency((NamedObj) actor));
		}
	}

	/**
	 * parse the slot selection string
	 * @param slots
	 * @param frequency
	 * @return
	 * @throws TDLModeSchedulerException
	 */
	private ArrayList _getInvocations(String slots, int frequency)
			throws TDLModeSchedulerException {
		String slotSelection = slots + '\n';
		ArrayList invocations = new ArrayList();
		String number = "";
		int firstSlot = 0, secondSlot = 0;
		boolean hadStar = false;
		for (int i = 0; i < slotSelection.length(); i++) {
			switch (slotSelection.charAt(i)) {
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
				number += slotSelection.charAt(i);
				if (slotSelection.length() > i + 1
						&& slotSelection.charAt(i + 1) > 47
						&& slotSelection.charAt(i + 1) < 58)
					number += slotSelection.charAt(++i);
				int slotNumber = Integer.parseInt(number);
				if (firstSlot == 0) {
					firstSlot = slotNumber;
					if (hadStar) { // everthing between last invocation and 'firstSlot' is scheduled the same way as the last slotGroup
						int lastStart = (Integer) invocations.get(invocations
								.size() - 2);
						int lastEnd = (Integer) invocations.get(invocations
								.size() - 1);
						int lastDiff = lastEnd - lastStart;
						while (lastEnd + 1 < firstSlot) {
							invocations.add(lastEnd);
							invocations.add(lastEnd + lastDiff);
							lastEnd += lastDiff;
						}
						hadStar = false;
					}
				} else
					secondSlot = slotNumber + 1;
				number = "";
				break;
			case '*':
				hadStar = true;
				if (slotSelection.length() >= i
						&& slotSelection.charAt(i + 1) == '|')
					break;
			case '|':
				invocations.add(firstSlot);
				invocations.add(secondSlot);
				firstSlot = secondSlot = 0;
				break;
			case '\n':
				if (hadStar) { // everthing between last invocation and 'firstSlot' is scheduled the same way as the last slotGroup
					int lastStart = (Integer) invocations.get(invocations
							.size() - 2);
					int lastEnd = (Integer) invocations
							.get(invocations.size() - 1);
					if (lastEnd == 0)
						lastEnd = lastStart + 1;
					int lastDiff = lastEnd - lastStart;
					int end = firstSlot;
					if (end == 0)
						end = frequency + 1;
					while (lastEnd < end) {
						invocations.add(lastEnd);
						invocations.add(lastEnd + lastDiff);
						lastEnd += lastDiff;
					}
					hadStar = false;
				} else {
					invocations.add(firstSlot);
					invocations.add(secondSlot);
				}
				break;
			case '-':
			case '~': // optional
			case '\'': // start and end, to avoid errors in parameter "expression could not be parsed"
				break;
			default:
				throw new TDLModeSchedulerException("'" + slots
						+ "' cannot be parsed");
			}
		}
		// update invocations so that 0-values which came from not initializing secondSlot are filled
		for (int i = 0; i < invocations.size(); i += 2) {
			if ((Integer) invocations.get(i + 1) == 0) {
				invocations.add(i + 1, (Integer) invocations.get(i) + 1);
				invocations.remove(i + 2);
			}
		}
		return invocations;
	}

	private void _scheduleSingleActivity(String activityName, Object activity,
			long time) throws TDLModeSchedulerException {
		if (_modeSchedule.get(time) == null)
			_modeSchedule.put(time, new HashMap());
		if (((HashMap) _modeSchedule.get(time)).get(activityName) == null)
			((HashMap) _modeSchedule.get(time)).put(activityName,
					new ArrayList());
		((ArrayList) ((HashMap) _modeSchedule.get(time)).get(activityName))
				.add(activity);
	}

	/**
	 * schedule a single activity
	 * @param activityName
	 * @param activity
	 * @param frequency
	 * @throws TDLModeSchedulerException
	 */
	private void _scheduleActivity(String activityName, Object activity,
			int frequency) throws TDLModeSchedulerException {
		if ((_modePeriod % lcmPeriod) / frequency != 0)
			throw new TDLModeSchedulerException("Frequency " + frequency
					+ " is not valid for mode period " + _modePeriod + ".");
		for (long i = 0; i < _modePeriod; i = i + _modePeriod / frequency) {
			if (_modeSchedule.get(i) == null)
				_modeSchedule.put(i, new HashMap());

			if (((HashMap) _modeSchedule.get(i)).get(activityName) == null)
				((HashMap) _modeSchedule.get(i)).put(activityName,
						new ArrayList());

			((ArrayList) ((HashMap) _modeSchedule.get(i)).get(activityName))
					.add(activity);
		}
	}

	/**
	 * schedule a list of activities as an atomic unit
	 * @param activityName
	 * @param activities
	 * @param frequency
	 * @throws TDLModeSchedulerException
	 */
	private void _scheduleAtomicActivities(String activityName,
			List activities, int frequency) throws TDLModeSchedulerException {
		if ((_modePeriod % lcmPeriod) / frequency != 0)
			throw new TDLModeSchedulerException("Frequency " + frequency
					+ " is not valid for mode period " + _modePeriod + ".");
		for (long i = 0; i < _modePeriod; i = i + _modePeriod / frequency) {
			if (_modeSchedule.get(i) == null)
				_modeSchedule.put(i, new HashMap());

			if (((HashMap) _modeSchedule.get(i)).get(activityName) == null)
				((HashMap) _modeSchedule.get(i)).put(activityName,
						new ArrayList());

			((ArrayList) ((HashMap) _modeSchedule.get(i)).get(activityName))
					.addAll(activities);
		}
	}

	/**
	 * merge the two schedules generated by the TTModeScheduler and the TDLModeScheduler
	 * @param ttModeSchedule
	 * @return
	 */
	private HashMap _mergeSchedules(HashMap ttModeSchedule) {
		HashMap schedule = new HashMap();
		HashMap ttModeScheduleCopy = new HashMap();

		// TTModeScheduler does not know anything about a mode period, therefore
		// the schedule generated in the TTModeScheduler might be shorter than
		// required
		// -> extend Schedule by copying content _modePeriod/lcmPerid times
		if (ttModeSchedule != null) {
			for (long i = 0; i < _modePeriod; i = i + lcmPeriod) {
				Iterator ttactivities = ttModeSchedule.keySet().iterator();
				while (ttactivities.hasNext()) {
					long time = (Long) ttactivities.next();
					ttModeScheduleCopy.put(time + i, ttModeSchedule.get(time));
				}
			}
		}
		ttModeSchedule = ttModeScheduleCopy; // ?? auskommentiert ??
		//        if (ttModeSchedule.size() == 0)
		//        	ttModeSchedule.put(0L, null);
		// add all times that are introduced by the tdl schedule
		Iterator times = _modeSchedule.keySet().iterator();
		while (times.hasNext()) {
			long l = (Long) times.next();
			if (ttModeSchedule.get(l) == null)
				ttModeSchedule.put(l, null);
		}
		Iterator it = ttModeSchedule.keySet().iterator();
		while (it.hasNext()) {
			long time = (Long) it.next();
			ArrayList slotActivities = new ArrayList();
			for (int i = 0; i < _activities.length; i++) {
				String activityName = _activities[i];
				if (_modeSchedule.get(time) != null) {
					Collection tdlActivities = (Collection) ((HashMap) _modeSchedule
							.get(time)).get(activityName);
					if (tdlActivities != null)
						slotActivities.addAll(tdlActivities);
				}

				if (ttModeSchedule.get(time) != null) {
					Collection ttActivities = (Collection) ((HashMap) ttModeSchedule
							.get(time)).get(activityName);
					if (ttActivities != null)
						slotActivities.addAll(ttActivities);
				}
			}
			schedule.put(time, slotActivities);
		}

		return schedule;
	}

	/** contains scheduled activities for the mode */
	private HashMap _modeSchedule;

	/** period of the mode */
	private long _modePeriod;

	/** in this order the actions are scheduled */
	private String[] _activities = new String[] {
			"regular task output port update", "update actuators",
			"calculate mode switch", "fast task",
			"regular task input port update and execution" };

	/** list of actuators */
	private List _actuatorUpdates = new ArrayList();

	/** list of mode switches */
	private List _transitions = new ArrayList();

	/** list of fast tasks */
	private List _fastTasks = new ArrayList();
}
