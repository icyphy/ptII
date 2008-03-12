package ptolemy.domains.ptides.platform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.util.FunctionDependencyOfCompositeActor;
import ptolemy.actor.util.Time;
import ptolemy.domains.de.kernel.DEEvent;
import ptolemy.domains.ptides.kernel.PtidesGraphUtilities;
import ptolemy.domains.ptides.lib.ScheduleListener;
import ptolemy.graph.DirectedAcyclicGraph;
import ptolemy.kernel.util.IllegalActionException;

public class NonPreemptivePlatformExecutionStrategy extends PlatformExecutionStrategy {
	
	public NonPreemptivePlatformExecutionStrategy(Time physicalTime, Director director) {
		_director = director;
		_physicalTime = physicalTime;
	}
	
	public void sort(List list, Time physicalTime) {
		Collections.sort(list, new WCETComparator());
	}

	
	private class WCETComparator implements Comparator {
		
		private boolean _preemptive = false;

		/*public int compare(Object arg0, Object arg1) {
			DEEvent event1 = (DEEvent) arg0;
			DEEvent event2 = (DEEvent) arg1;
			Actor actor1 = event1.actor();
			Actor actor2 = event2.actor();
			double wcet1 = PtidesEmbeddedDirector.getWCET(actor1);
			double wcet2 = PtidesEmbeddedDirector.getWCET(actor2);
			Time time1 = event1.timeStamp();
			Time time2 = event2.timeStamp();
			boolean fireAtRT1 = PtidesEmbeddedDirector.mustBeFiredAtRealTime(actor1);
			boolean fireAtRT2 = PtidesEmbeddedDirector.mustBeFiredAtRealTime(actor2);
			int index1 = -1;
			int index2 = -1;
			
			CompositeActor actor = (CompositeActor) actor1.getContainer();
	    	FunctionDependencyOfCompositeActor functionDependency = (FunctionDependencyOfCompositeActor) (actor).getFunctionDependency();
			DirectedAcyclicGraph graph = functionDependency.getDetailedDependencyGraph().toDirectedAcyclicGraph();
			Object[] objects = graph.topologicalSort();
			for (int i = 0; i < objects.length; i++) {
				if (((IOPort)objects[i]).equals(actor1))
					index1 = i;
				else if (((IOPort)objects[i]).equals(actor2))
					index2 = i;
			}
			
			if (wcet1 == 0 && wcet2 > 0)				return -1;
			if (wcet2 == 0 && wcet1 > 0)				return 1;
			if (wcet1 == 0 && wcet2 == 0) {
				if (fireAtRT1 && !fireAtRT2)			return -1;
				else if (fireAtRT2 && !fireAtRT1)		return 1;
				else {
					if (time1.compareTo(time2) < 0)		return -1;
					else if (time1.compareTo(time2) > 0)return 1;
					else {
						if (index1 < index2)			return -1;
						else if (index1 > index2)		return 1;
					}
				} 
			} else { // wcet1 > 0 && wcet2 > 0
				if (fireAtRT1 && !fireAtRT2) {
					// if execution of non real time actor can fit before real time actor
					if ((!_preemptive && _physicalTime.getDoubleValue() + wcet2 <= time1.getDoubleValue()) ||
							(_preemptive && _physicalTime.getDoubleValue() <= time1.getDoubleValue())) {
						return 1;
					} else {
						return -1;
					}
				} else if (fireAtRT2 && !fireAtRT1) {	
//					 if execution of non real time actor can fit before real time actor
					if ((!_preemptive && _physicalTime.getDoubleValue() + wcet1 <= time2.getDoubleValue()) ||
							(_preemptive && _physicalTime.getDoubleValue() <= time2.getDoubleValue()))	
						return -1;
					else { 
						return 1;
					}
				}
				else if (fireAtRT1 && fireAtRT2) {
					if (time1.compareTo(time2) < 0)		return -1;
					else if (time1.compareTo(time2) > 0)return 1;
					else {
						// two actors with WCET > 0 require to be fired at the same physical time
					}
				} else {
					if (time1.compareTo(time2) < 0)		return -1;
					else if (time1.compareTo(time2) > 0)return 1;
					else {
						if (index1 < index2)			return -1;
						else if (index1 > index2)		return 1;
					}
				}
			}
			return 0;
		}*/
		
		public int compare(Object arg0, Object arg1) {
			DEEvent event1 = (DEEvent) arg0;
			DEEvent event2 = (DEEvent) arg1;
			Actor actor1 = event1.actor();
			Actor actor2 = event2.actor();
			double wcet1 = PtidesGraphUtilities.getWCET(actor1);
			double wcet2 = PtidesGraphUtilities.getWCET(actor2);
			Time time1 = event1.timeStamp();
			Time time2 = event2.timeStamp();
			boolean fireAtRT1 = PtidesGraphUtilities.mustBeFiredAtRealTime(actor1);
			boolean fireAtRT2 = PtidesGraphUtilities.mustBeFiredAtRealTime(actor2);
			int index1 = -1;
			int index2 = -1;
			
			// TODO wrong!!!
			CompositeActor actor = (CompositeActor) actor1.getContainer();
	    	FunctionDependencyOfCompositeActor functionDependency = (FunctionDependencyOfCompositeActor) (actor).getFunctionDependency();
			DirectedAcyclicGraph graph = functionDependency.getDetailedDependencyGraph().toDirectedAcyclicGraph();
			Object[] objects = graph.topologicalSort();
			for (int i = 0; i < objects.length; i++) {
				if (((IOPort)objects[i]).equals(actor1))
					index1 = i;
				else if (((IOPort)objects[i]).equals(actor2))
					index2 = i;
			}
			
 			if (wcet1 == 0 && (!fireAtRT1 || time1.equals(_physicalTime)) && wcet2 > 0)				return -1;
 			if (wcet1 > 0 && wcet2 == 0 && (!fireAtRT2 || time2.equals(_physicalTime)))				return 1;
			if (wcet1 == 0 && wcet2 == 0) {
				if (fireAtRT1 && time1.equals(_physicalTime) && !fireAtRT2)
					return -1;
				if (fireAtRT1 && time1.compareTo(_physicalTime) > 0 && !fireAtRT2)
					return 1;
				if (fireAtRT2 && time2.equals(_physicalTime) && !fireAtRT1)
					return 1;
				if (fireAtRT2 && time2.compareTo(_physicalTime) > 0 && !fireAtRT1)
					return -1;
				if (fireAtRT1 && fireAtRT2 && time1.equals(_physicalTime) && time2.equals(_physicalTime))
					return 0;
			    if (time1.compareTo(time2) < 0)		
			    	return -1;
				if (time2.compareTo(time1) < 0)
					return 1;
				else {
					if (event1.depth() < event2.depth())			return -1;
					else if (event1.depth() > event2.depth())		return 1;
				}
			} else { // wcet1 > 0 && wcet2 > 0
				if (fireAtRT1 && !fireAtRT2) {
					// if execution of non real time actor can fit before real time actor
					if ((!_preemptive && _physicalTime.getDoubleValue() + wcet2 <= time1.getDoubleValue()) ||
							(_preemptive && _physicalTime.getDoubleValue() <= time1.getDoubleValue())) {
						return 1;
					} else {
						return -1;
					}
				} else if (fireAtRT2 && !fireAtRT1) {	
	//					 if execution of non real time actor can fit before real time actor
					if ((!_preemptive && _physicalTime.getDoubleValue() + wcet1 <= time2.getDoubleValue()) ||
							(_preemptive && _physicalTime.getDoubleValue() <= time2.getDoubleValue()))	
						return -1;
					else { 
						return 1;
					}
				}
				else if (fireAtRT1 && fireAtRT2) {
					if (time1.compareTo(time2) < 0)		return -1;
					else if (time1.compareTo(time2) > 0)return 1;
					else {
						// two actors with WCET > 0 require to be fired at the same physical time
					}
				} else {
					if (time1.compareTo(time2) < 0)		return -1;
					else if (time1.compareTo(time2) > 0)return 1;
					else {
						if (index1 < index2)			return -1;
						else if (index1 > index2)		return 1;
					}
				}
			}
			return 0;
		}
    }
	
	public boolean nothingToDoNow(List eventsToFire) {
		return eventsToFire.isEmpty() || 
        		_allEventsToFireMustBeFiredAtLaterTime(eventsToFire);
	}

	private boolean _allEventsToFireMustBeFiredAtLaterTime(List eventsToFire) {
		Time nextRealTime = Time.POSITIVE_INFINITY;
		for (int i = 0; i < eventsToFire.size(); i++) {
			DEEvent event = (DEEvent) eventsToFire.get(i);
	    	Actor actorToFire = event.actor();
	    	if (PtidesGraphUtilities.mustBeFiredAtRealTime(actorToFire))
	    		if (event.timeStamp().compareTo(_physicalTime) <= 0) // < will cause an exception - missed execution
	    			return false;
	    		else if (nextRealTime.compareTo(event.timeStamp()) > 0)
	    			nextRealTime = event.timeStamp();
		}
		for (int i = 0; i < eventsToFire.size(); i++) {
			DEEvent event = (DEEvent) eventsToFire.get(i);
	    	Actor actorToFire = event.actor();
	    	// if execution fits before next real time event
	    	if (!PtidesGraphUtilities.mustBeFiredAtRealTime(actorToFire) && _physicalTime.add(PtidesGraphUtilities.getWCET(actorToFire)).compareTo(nextRealTime) < 0)
	    		return false;
		}
		return true;
    }

	@Override
	public boolean actorCanBeFired(Actor actorToFire, Time nextRealTimeEvent) {
		return nextRealTimeEvent.compareTo(_physicalTime.add(PtidesGraphUtilities.getWCET(actorToFire))) > 0;
	}

	@Override
	public boolean actorExecutionFinished(Actor actorToFire, Time fireTime) {
		return (fireTime.add(PtidesGraphUtilities.getWCET(actorToFire)).equals(_physicalTime));
	}


	@Override
	public boolean missedExecution(Actor actorToFire, Time fireTime) {
		return fireTime.add(PtidesGraphUtilities.getWCET(actorToFire)).compareTo(_physicalTime) <  0;
	}

	@Override
	public boolean dealWithNewlyReceivedEvents(Actor actorToFire, Hashtable eventQueues) {
		return false;
	}

	@Override
	public DEEvent getNextEventToFire(List actorsFiring, List eventsToFire) throws IllegalActionException {
		if (actorsFiring.size() > 0 || eventsToFire.size() == 0)
			return null;
		
		Time _nextRealTimeEvent = Time.POSITIVE_INFINITY ;
		Collections.sort(eventsToFire, new WCETComparator());
		for (int i = 0; i < eventsToFire.size(); i++) {
			DEEvent event = (DEEvent)eventsToFire.get(i);
			Actor actor = event.actor();
			if (PtidesGraphUtilities.mustBeFiredAtRealTime(actor)) {
				if (event.timeStamp().compareTo(_nextRealTimeEvent) < 0)
					_nextRealTimeEvent = event.timeStamp();
			}
		}
		System.out.println("next rt: " + _nextRealTimeEvent);
		DEEvent event = null;
		int index = 0;
		while (index < eventsToFire.size()) {
			event = (DEEvent) eventsToFire.get(index);			
			Actor actorToFire = event.actor();
			
			if (PtidesGraphUtilities.mustBeFiredAtRealTime(actorToFire)) {
				if (_physicalTime.compareTo(event.timeStamp()) > 0) {
					_displaySchedule(actorToFire, event.timeStamp().getDoubleValue(), ScheduleListener.MISSEDEXECUTION);
					throw new IllegalActionException("missed execution!");
				} else if (_physicalTime.compareTo(event.timeStamp()) < 0) {
					index++;
					continue;
				} 
			} else if (_physicalTime.add(PtidesGraphUtilities.getWCET(actorToFire)).compareTo(_nextRealTimeEvent) > 0) {
				index++;
				continue;
			}
	        return event;
	        
		}
		return null;
	}
}
