package ptolemy.domains.ptides.platform;

import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.util.FunctionDependencyOfCompositeActor;
import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.de.kernel.DEEvent;
import ptolemy.domains.ptides.kernel.PtidesGraphUtilities;
import ptolemy.graph.DirectedAcyclicGraph;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

/**
 * unfinished!!!
 * 
 * @author Patricia Derler
 * 
 */
public class PreemptivePlatformExecutionStrategy extends
		PlatformExecutionStrategy {

	public boolean actorCanBeFired(Actor actorToFire, Time nextRealTimeEvent) {
		return true; // assuming that this event was selected before with the
						// isSafeToProcess method
	}

	public boolean actorExecutionFinished(Actor actorToFire, Time fireTime) {
		return (fireTime.add(PtidesGraphUtilities.getWCET(actorToFire))
				.equals(_physicalTime));
	}

	public boolean dealWithNewlyReceivedEvents(Actor actorToFire,
			Hashtable eventQueues) {
		// if there exists an event that should be fired before the currently
		// executing Actor
		// TODO !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		return false;
	}

	public boolean missedExecution(Actor actorToFire, Time fireTime) {
		return fireTime.add(
				getExecutionTimeWithPreemption((NamedObj) actorToFire))
				.compareTo(_physicalTime) < 0;
	}

	private static double getExecutionTimeWithPreemption(NamedObj actor) {
		try {
			Parameter parameter = (Parameter) actor
					.getAttribute("preemptedExecutionTime");

			if (parameter != null) {
				DoubleToken intToken = (DoubleToken) parameter.getToken();

				return intToken.doubleValue();
			} else {
				return Double.MAX_VALUE;
			}
		} catch (ClassCastException ex) {
			return Double.MAX_VALUE;
		} catch (IllegalActionException ex) {
			return Double.MAX_VALUE;
		}
	}

	public static void setExecutionTimeWithPreemption(IOPort out,
			double preemptedExecutionTime) throws IllegalActionException {
		Parameter parameter = (Parameter) ((NamedObj) out)
				.getAttribute("preemptedExecutionTime");
		if (parameter == null)
			try {
				parameter = new Parameter((NamedObj) out,
						"preemptedExecutionTime", new DoubleToken(
								preemptedExecutionTime));
			} catch (NameDuplicationException ex) {
				// can never happen
			}
		else
			parameter.setToken(new DoubleToken(preemptedExecutionTime));
	}

	public boolean nothingToDoNow(List eventsToFire) {
		return eventsToFire.isEmpty();
	}

	public void sort(List list, Time physicalTime) {
		Collections.sort(list, new WCETComparator());
	}

	private class WCETComparator implements Comparator {

		private boolean _preemptive = false;

		public int compare(Object arg0, Object arg1) {
			DEEvent event1 = (DEEvent) arg0;
			DEEvent event2 = (DEEvent) arg1;
			Actor actor1 = event1.actor();
			Actor actor2 = event2.actor();
			double wcet1 = PtidesGraphUtilities.getWCET(actor1);
			double wcet2 = PtidesGraphUtilities.getWCET(actor2);
			Time time1 = event1.timeStamp();
			Time time2 = event2.timeStamp();
			boolean fireAtRT1 = PtidesGraphUtilities
					.mustBeFiredAtRealTime(actor1, event1.ioPort());
			boolean fireAtRT2 = PtidesGraphUtilities
					.mustBeFiredAtRealTime(actor2, event2.ioPort());
			int index1 = -1;
			int index2 = -1;

			CompositeActor actor = (CompositeActor) actor1.getContainer();
			FunctionDependencyOfCompositeActor functionDependency = (FunctionDependencyOfCompositeActor) (actor)
					.getFunctionDependency();
			DirectedAcyclicGraph graph = functionDependency
					.getDetailedDependencyGraph().toDirectedAcyclicGraph();
			Object[] objects = graph.topologicalSort();
			for (int i = 0; i < objects.length; i++) {
				if (((IOPort) objects[i]).equals(actor1))
					index1 = i;
				else if (((IOPort) objects[i]).equals(actor2))
					index2 = i;
			}

			if (time1.add(wcet1).compareTo(wcet2) > 0)
				return -1;
			if (time2.add(wcet2).compareTo(wcet1) > 0)
				return 1;
			if (wcet1 == 0 && wcet2 == 0) {
				if (fireAtRT1 && !fireAtRT2)
					return -1;
				else if (fireAtRT2 && !fireAtRT1)
					return 1;
				else {
					if (time1.compareTo(time2) < 0)
						return -1;
					else if (time1.compareTo(time2) > 0)
						return 1;
					else {
						if (index1 < index2)
							return -1;
						else if (index1 > index2)
							return 1;
					}
				}
			} else { // wcet1 > 0 && wcet2 > 0
				if (fireAtRT1 && !fireAtRT2) {
					// if execution of non real time actor can fit before real
					// time actor
					if ((!_preemptive && _physicalTime.getDoubleValue() + wcet2 <= time1
							.getDoubleValue())
							|| (_preemptive && _physicalTime.getDoubleValue() <= time1
									.getDoubleValue())) {
						return 1;
					} else {
						return -1;
					}
				} else if (fireAtRT2 && !fireAtRT1) {
					// if execution of non real time actor can fit before real
					// time actor
					if ((!_preemptive && _physicalTime.getDoubleValue() + wcet1 <= time2
							.getDoubleValue())
							|| (_preemptive && _physicalTime.getDoubleValue() <= time2
									.getDoubleValue()))
						return -1;
					else {
						return 1;
					}
				} else if (fireAtRT1 && fireAtRT2) {
					if (time1.compareTo(time2) < 0)
						return -1;
					else if (time1.compareTo(time2) > 0)
						return 1;
					else {
						// two actors with WCET > 0 require to be fired at the
						// same physical time
					}
				} else {
					if (time1.compareTo(time2) < 0)
						return -1;
					else if (time1.compareTo(time2) > 0)
						return 1;
					else {
						if (index1 < index2)
							return -1;
						else if (index1 > index2)
							return 1;
					}
				}
			}
			return 0;
		}
	}

	@Override
	public DEEvent getNextEventToFire(List actorsFiring, List eventsToFire)
			throws IllegalActionException {
		// TODO Auto-generated method stub
		return null;
	}

}
