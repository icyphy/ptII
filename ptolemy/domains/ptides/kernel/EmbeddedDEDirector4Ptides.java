package ptolemy.domains.ptides.kernel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.FiringEvent;
import ptolemy.actor.IOPort;
import ptolemy.actor.Initializable;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.Receiver;
import ptolemy.actor.TokenSentEvent;
import ptolemy.actor.lib.Clock;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.util.FunctionDependency;
import ptolemy.actor.util.FunctionDependencyOfCompositeActor;
import ptolemy.actor.util.Time;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.dde.kernel.DDEDirector;
import ptolemy.domains.dde.kernel.DDEReceiver;
import ptolemy.domains.dde.kernel.DDEThread;
import ptolemy.domains.dde.kernel.PrioritizedTimedQueue;
import ptolemy.domains.dde.kernel.TimeKeeper;
import ptolemy.domains.de.kernel.DECQEventQueue;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.domains.de.kernel.DEEvent;
import ptolemy.domains.de.kernel.DEEventQueue;
import ptolemy.domains.de.lib.TimedDelay;
import ptolemy.domains.ptides.kernel.DEReceiver4Ptides;
import ptolemy.domains.ptides.kernel.DDEReceiver4Ptides;
import ptolemy.domains.ptides.lib.ScheduleListener;
import ptolemy.graph.DirectedAcyclicGraph;
import ptolemy.graph.Edge;
import ptolemy.graph.Node;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.DebugListener;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

public class EmbeddedDEDirector4Ptides extends DEDirector {

	public EmbeddedDEDirector4Ptides() throws IllegalActionException,
			NameDuplicationException {
		super();
		_initialize();
	}

	public EmbeddedDEDirector4Ptides(Workspace workspace) throws IllegalActionException, NameDuplicationException {
		super(workspace);
		_initialize();
	}

	public EmbeddedDEDirector4Ptides(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
		super(container, name);
		_initialize();
	}
	
	public Parameter preemptive;
	public boolean _preemptive;
	


	public static boolean mustBeFiredAtRealTime(Actor actor) {
		return isSensor(actor) || isActuator(actor);
	}
	
	public static double getWCET(Actor actor) {
		try {
            Parameter parameter = (Parameter) ((NamedObj)actor).getAttribute("WCET");

            if (parameter != null) {
            	DoubleToken token = (DoubleToken) parameter.getToken();

                return token.doubleValue();
            } else {
            	return 0.0;
            }
        } catch (ClassCastException ex) {
            return 0.0;
        } catch (IllegalActionException ex) {
            return 0.0;
        }
	}
	
	public static boolean isActuator(Actor actor) {
		try {
			if (actor == null)
				actor = null;
            Parameter parameter = (Parameter) ((NamedObj)actor).getAttribute("isActuator");

            if (parameter != null) {
            	BooleanToken intToken = (BooleanToken) parameter.getToken();

                return intToken.booleanValue();
            } else {
                return false;
            }
        } catch (ClassCastException ex) {
            return false;
        } catch (IllegalActionException ex) {
            return false;
        }
	}
	
	public static boolean isSensor(Actor actor) {
		try {
			if (actor == null)
				actor = null;
            Parameter parameter = (Parameter) ((NamedObj)actor).getAttribute("isSensor");

            if (parameter != null) {
            	BooleanToken intToken = (BooleanToken) parameter.getToken();

                return intToken.booleanValue();
            } else {
                return false;
            }
        } catch (ClassCastException ex) {
            return false;
        } catch (IllegalActionException ex) {
            return false;
        }
	}
	
	public static double getMinDelayTime(NamedObj actor) {
		try {
			
//			double clockSyncError = ((EmbeddedDEDirector4Ptides)((Actor)actor.getContainer()).getDirector()).getExecutiveDirector().getClockSyncError();
//	    	double networkDelay = ((EmbeddedDEDirector4Ptides)((Actor)actor.getContainer()).getDirector()).getExecutiveDirector().getNetworkDelay();
			
            Parameter parameter = (Parameter) actor.getAttribute("minDelay");

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
	
	public static void setMinDelay(IOPort out, double minDelay) throws IllegalActionException {
    	Parameter parameter = (Parameter) ((NamedObj)out).getAttribute("minDelay");
		if (parameter == null)
			try {
			parameter = new Parameter((NamedObj)out, "minDelay", new DoubleToken(minDelay));
			} catch (NameDuplicationException ex) {
				// can never happen
			}
		else 
			parameter.setToken(new DoubleToken(minDelay));
		System.out.println(out.getName() + ": " + minDelay);
	}

	

	public void fire() throws IllegalActionException {
		if (_stopRequested)
			return;
        while (true) {
            List eventsToFire = _getNextEventsToFire();
            while (_nothingToDoNow(eventsToFire)) { 
            	_enqueueRemainingEventsAgain(eventsToFire);
            	_waitForPhysicalTime("notingToDo");
            	if (_stopRequested) return;
            	eventsToFire = _getNextEventsToFire();
            }
            if (_stopRequested) return;
            int eventsfired = 0;
            for (int i = 0; i < eventsToFire.size(); i++) {
            	// before executing actors with wcet > 0 check if there are other actors to be fired now without wcet update eventsToFire
            	DEEvent event = (DEEvent) eventsToFire.get(i);
            	Actor actorToFire = event.actor();
            	if (mustBeFiredAtRealTime(actorToFire)) {
                	if (event.timeStamp().compareTo(physicalTime) > 0 && !_stopRequested) {
                		_enqueueEventAgain(event); // fire the rest
                		System.out.println(getContainer().getName() + ": ignore event that should be fired later");
                		nextRealTimeEvent = event.timeStamp();
                		continue;
                	}
                	nextRealTimeEvent = Time.POSITIVE_INFINITY;
                	if (event.timeStamp().compareTo(physicalTime) < 0) {
                		_displaySchedule(actorToFire, event.timeStamp().getDoubleValue(), ScheduleListener.MISSEDEXECUTION);
                		throw new IllegalActionException(actorToFire + " should have been fired at " + event.timeStamp() + " but physical time is already " + physicalTime + ".");
                	}
                } else if (!_preemptive && nextRealTimeEvent.compareTo(physicalTime.add(getWCET(actorToFire))) < 0) { // next execution does not fit into time before next real time event
                	System.out.println(getContainer().getName() + ": ignore event that should be fired later");
                	_enqueueEventAgain(event); // fire the rest
            		continue;
                }
            	if (eventsfired > 0 && getWCET(actorToFire) > 0.0) { // enqueue remaining events and update eventqueue
            		System.out.println(getContainer().getName() + ": next actor has wcet > 0 -> see if other wcet=0 actors to fire first");
            		_enqueueRemainingEventsAgain(eventsToFire.subList(i, eventsToFire.size()));
            		break;
            	}
            	while (!actorToFire.prefire() && !_stopRequested) { 
                	_waitForPhysicalTime("prefireFalse");
                }
            	
            	_currentModelTime = event.timeStamp();
            	_displaySchedule(actorToFire, physicalTime.getDoubleValue(), ScheduleListener.START);
	            actorToFire.fire();
	            eventsfired++;
	            System.out.println(getContainer().getName() + ": fired " + actorToFire + " at " + physicalTime + "/" + getModelTime());
	            Time fireTime = physicalTime;
	            double wcet = getWCET(actorToFire);
	            _addSynchronizationPoint(physicalTime.add(EmbeddedDEDirector4Ptides.getWCET(actorToFire)));
	            while (!(fireTime.add(wcet)).equals(physicalTime)) {
	            	if (fireTime.add(wcet).compareTo(physicalTime) <  0) {
	            		_displaySchedule(actorToFire, event.timeStamp().add(wcet).getDoubleValue(), ScheduleListener.MISSEDEXECUTION);
	            		throw new IllegalActionException("missed sync point");
	            	}
	            	_waitForPhysicalTime("actorExecuting");	
	            	if (_stopRequested)	return;
	            }
	            _displaySchedule(actorToFire, physicalTime.getDoubleValue(), ScheduleListener.STOP);
	            if (!actorToFire.postfire()) {
	                _disableActor(actorToFire);
	                break;
	            }
	            if (_transferAllOutputs() && !_stopRequested) 
	        		_getExecutiveDirector().notifyWaitingThreads();
	            _currentModelTime = null;
            }
        } 
    }


	

	/** Update the director parameters when attributes are changed.
     *  Changes to <i>isCQAdaptive</i>, <i>minBinCount</i>, and
     *  <i>binCountFactor</i> parameters will only be effective on
     *  the next time when the model is executed.
     *  @param attribute The changed parameter.
     *  @exception IllegalActionException If the parameter set is not valid.
     *  Not thrown in this class.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        super.attributeChanged(attribute);
        if (attribute == preemptive) {
			 _preemptive = ((BooleanToken) preemptive.getToken()).booleanValue();
		 } else {
            super.attributeChanged(attribute);
        }
    }

      

    /** Schedule an actor to be fired at the specified time by posting
     *  a pure event to the director.
     *  @param actor The scheduled actor to fire.
     *  @param time The scheduled time to fire.
     *  @exception IllegalActionException If event queue is not ready.
     */
    public void fireAt(Actor actor, Time time) throws IllegalActionException {
        if (_eventQueues == null || _eventQueues.get(actor) == null) {
            throw new IllegalActionException(this, "Calling fireAt() before preinitialize().");
        }
        if (_debugging) _debug("DEDirector: Actor " + actor.getFullName() + " requests refiring at " + time);

        synchronized (_eventQueues) {
            _enqueueEvent(actor, time);
            _eventQueues.notifyAll();
        }
    }
    
    @Override
    public Time getModelTime() {
    	if (_currentModelTime == null)
    		return _currentTime;
    	else {
    		return _currentModelTime;
    	}
    }

    /** Schedule a firing of the given actor at the current time.
     *  @param actor The actor to be fired.
     *  @exception IllegalActionException If event queue is not ready.
     */
    public void fireAtCurrentTime(Actor actor) throws IllegalActionException {
        fireAt(actor, getModelTime());
    }

    /** Schedule an actor to be fired in the specified time relative to
     *  the current model time.
     *  @param actor The scheduled actor to fire.
     *  @param time The scheduled time to fire.
     *  @exception IllegalActionException If the specified time contains
     *  a negative time value, or event queue is not ready.
     */
    public void fireAtRelativeTime(Actor actor, Time time)
            throws IllegalActionException {
        fireAt(actor, time.add(getModelTime()));
    }
    
	public void getEquivalenceClasses() {
    	DirectedAcyclicGraph graph = _computeGraph();
    	CompositeActor actor = (CompositeActor) getContainer();
    	List outputPortList = actor.outputPortList();
    	List inputPortList = actor.inputPortList();
    	double[][] connections = new double[outputPortList.size()][inputPortList.size()];
    	for (Iterator it = outputPortList.iterator(); it.hasNext(); ) {
    		IOPort out = (IOPort) it.next();
    		Collection col = graph.backwardReachableNodes(graph.node(out));
    		for (Iterator it2 = inputPortList.iterator(); it2.hasNext(); ) {
    			IOPort in = (IOPort) it2.next();
    			if (col.contains(graph.node(in)))
    				connections[outputPortList.indexOf(out)][inputPortList.indexOf(in)] = 1;
    		}
    	}
    	System.out.println("----------" + this.getContainer().getName());
    	System.out.print("\t");
    	for (int j = 0; j < inputPortList.size(); j++) {
    		System.out.print(((IOPort)inputPortList.get(j)).getName() + "\t");
    	}
    	System.out.println();
    	for (int i = 0; i < outputPortList.size(); i++) {
    		System.out.print(((IOPort)outputPortList.get(i)).getName() + "\t");
    		for (int j = 0; j < inputPortList.size(); j++) {
    			System.out.print(connections[i][j] + "\t");
    		}
    		System.out.println();
    	}
    	System.out.println("------------------------------------------");
    }

    public Set getEquivalenceClassesPortLists() throws IllegalActionException {
    	if (equivalenceClasses == null) {
	    	equivalenceClasses = new HashSet();
	    	CompositeActor actor = (CompositeActor) getContainer();
	    	DirectedAcyclicGraph graph = _computeGraph();	
	    	
	    	Hashtable ioMapping = new Hashtable();
			for (Iterator colIt = graph.connectedComponents().iterator(); colIt.hasNext();) {
				Set ports = new HashSet();
				for (Iterator col2It = ((Collection) colIt.next()).iterator(); col2It.hasNext();) {
					Port p = (Port) ((Node)col2It.next()).getWeight();
					if (p.getContainer().equals(actor) && ((IOPort)p).isInput())
						ports.add(p);
				}
				if (!ports.isEmpty())
					equivalenceClasses.add(ports);
			}
    	}
    	for (Iterator it = equivalenceClasses.iterator(); it.hasNext(); ) {
    		Set eqc = (Set) it.next();
    		System.out.print("c: ");
    		for (Iterator it2 = eqc.iterator(); it2.hasNext(); ) {
    			IOPort port = (IOPort) it2.next();
    			System.out.print(port.getName()+ " ");
    		}
    		System.out.println();
    	}
		return equivalenceClasses;
	}

    /** Return the timestamp of the next event in the queue.
     *  The next iteration time, for example, is used to estimate the
     *  run-ahead time, when a continuous time composite actor is embedded
     *  in a DE model. If there is no event in the event queue, a positive
     *  infinity object is returned.
     *  @return The time stamp of the next event in the event queue.
     */
    public Time getModelNextIterationTime() {
        return null;
    }
 
    /** Return the system time at which the model begins executing.
     *  That is, the system time (in milliseconds) when the initialize()
     *  method of the director is called.
     *  The time is in the form of milliseconds counting
     *  from 1/1/1970 (UTC).
     *  @return The real start time of the model.
     */
    public long getRealStartTimeMillis() {
        return _realStartTime;
    }

    public void getMinDelays() throws IllegalActionException {
    	DirectedAcyclicGraph graph = _computeGraph();
    	CompositeActor actor = (CompositeActor) getContainer();
    	for (Iterator it = actor.outputPortList().iterator(); it.hasNext(); ) {
    		IOPort out = (IOPort) it.next();
    		System.out.println("---------" + out.getName());
    		double minDelay = _getMinDelay(false, graph.node(out), graph, Double.MAX_VALUE, new HashSet());
    		setMinDelay(out, minDelay);
    	}
    	for (Iterator it = actor.inputPortList().iterator(); it.hasNext(); ) {
    		IOPort in = (IOPort) it.next();
    		System.out.println("---------" + in.getName());
    		if (!_isInputConnectedToAnyOutput(graph, in)) {
    			double minDelay = _getMinDelay(true, graph.node(in), graph, Double.MAX_VALUE, new HashSet());
        		setMinDelay(in, minDelay);
    		}
    	}
    }
    
    public boolean isInputConnectedToOutput(IOPort in, IOPort out) {
    	DirectedAcyclicGraph graph = _computeGraph();
    	return graph.reachableNodes(graph.node(in)).contains(graph.node(out));
    }



    /** Initialize all the contained actors by invoke the initialize() method
     *  of the super class. If any events are generated during the
     *  initialization, and the container is not at the top level, request a
     *  refiring.
     *  <p>
     *  The real start time of the model is recorded when this method
     *  is called. This method is <i>not</i> synchronized on the workspace,
     *  so the caller should be.</p>
     *
     *  @exception IllegalActionException If the initialize() method of
     *   the super class throws it.
     */
    public void initialize() throws IllegalActionException {
        _isInitializing = true;

        // Reset the following private variables.
        _disabledActors = null;
        _exceedStopTime = false;
        _microstep = 0;
        _noMoreActorsToFire = false;
        _realStartTime = System.currentTimeMillis();

        super.initialize();

        // Register the stop time as an event such that the model is 
        // guaranteed to stop at that time. This event also serves as
        // a guideline for an embedded Continuous model to know how much
        // further to integrate into future.
        //fireAt((Actor) getContainer(), getModelStopTime());

        _isInitializing = false;
    }

    /** Indicate that the topological sort of the model may no longer be valid.
     *  This method should be called when topology changes are made.
     *  It sets a flag which will cause the topological
     *  sort to be redone next time when an event is enqueued.
     */
    public void invalidateSchedule() {
        _sortValid = -1;
    }

	public Receiver newReceiver() {
        if (_debugging && _verbose) {
            _debug("Creating a new DE receiver.");
        }

        return new DEReceiver4Ptides();
    }
    
    /** Return false if there are no more actors to be fired or the stop()
     *  method has been called. Otherwise, if the director is an embedded
     *  director and the local event queue is not empty, request the executive
     *  director to refire the container of this director at the timestamp of
     *  the first event in the event queue.
     *  @return True If this director will be fired again.
     *  @exception IllegalActionException If the postfire method of the super
     *  class throws it, or the stopWhenQueueIsEmpty parameter does not contain
     *  a valid token, or refiring can not be requested.
     */
    public boolean postfire() throws IllegalActionException {
        boolean result = !isStopRequested();
        boolean stop = ((BooleanToken) stopWhenQueueIsEmpty.getToken())
                .booleanValue();

        // There are two conditions to stop the model.
        // 1. There are no more actors to be fired (i.e. event queue is
        // empty), and either of the following conditions is satisfied:
        //     a. the stopWhenQueueIsEmpty parameter is set to true.
        //     b. the current model time equals the model stop time.
        // 2. The event queue is not empty, but the current time exceeds
        // the stop time.
        if (_noMoreActorsToFire
                && (stop || (getModelTime().compareTo(getModelStopTime()) == 0))) {
            _exceedStopTime = true;
            result = result && false;
        } else if (_exceedStopTime) {
            // If the current time is bigger than the stop time,
            // stop the model execution.
            result = result && false;
        }

        // NOTE: The following commented block enforces that no events with
        // different tags can exist in the same receiver.
        // This is a quite different semantics from the previous designs,
        // and its effects are still under investigation and debate.
        //        // Clear all of the contained actor's input ports.
        //        for (Iterator actors = ((CompositeActor)getContainer())
        //                .entityList(Actor.class).iterator();
        //                actors.hasNext();) {
        //            Entity actor = (Entity)actors.next();
        //            Iterator ports = actor.portList().iterator();
        //            while (ports.hasNext()) {
        //                IOPort port = (IOPort)ports.next();
        //                if (port.isInput()) {
        //                    // Clear all receivers.
        //                    Receiver[][] receivers = port.getReceivers();
        //                    if (receivers == null) {
        //                        throw new InternalErrorException(this, null,
        //                                "port.getReceivers() returned null! "
        //                                + "This should never happen. "
        //                                + "port was '" + port + "'");
        //                    }
        //                    for (int i = 0; i < receivers.length; i++) {
        //                        Receiver[] receivers2 = receivers[i];
        //                        for (int j = 0; j < receivers2.length; j++) {
        //                            receivers2[j].clear();
        //                        }
        //                    }
        //                }
        //            }
        //        }
        return result;
    }

    /** Set the model timestamp to the outside timestamp if this director is
     *  not at the top level. Check the timestamp of the next event to decide
     *  whether to fire. Return true if there are inputs to this composite
     *  actor, or the timestamp of the next event is equal to the current model
     *  timestamp. Otherwise, return false.
     *  <p>
     *  Note that microsteps are not synchronized.
     *  </p><p>
     *  Throw an exception if the current model time is greater than the next
     *  event timestamp.
     *  @return True if the composite actor is ready to fire.
     *  @exception IllegalActionException If there is a missed event,
     *  or the prefire method of the super class throws it, or can not
     *  query the tokens of the input ports of the container of this
     *  director.</p>
     */
    public boolean prefire() throws IllegalActionException {
        
        return !_stopRequested;
    }

    /** Set the current timestamp to the model start time, invoke the
     *  preinitialize() methods of all actors deeply contained by the
     *  container.
     *  <p>
     *  This method should be invoked once per execution, before any
     *  iteration. Actors cannot produce output data in their preinitialize()
     *  methods. If initial events are needed, e.g. pure events for source
     *  actor, the actors should do so in their initialize() method.
     *  </p><p>
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.</p>
     *
     *  @exception IllegalActionException If the preinitialize() method of the
     *  container or one of the deeply contained actors throws it, or the
     *  parameters, minBinCount, binCountFactor, and isCQAdaptive, do not have
     *  valid tokens.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize(); // bad, but have to do this because otherwise the eventqueue that is not used is not initialized and this causes problems in wrapup
    	
        
        _eventQueues = new Hashtable();
        for (Iterator it = ((CompositeActor)getContainer()).entityList().iterator(); it.hasNext();) {
        	_eventQueues.put(it.next(), new DECQEventQueue(((IntToken) minBinCount.getToken())
                .intValue(), ((IntToken) binCountFactor.getToken()).intValue(),
                ((BooleanToken) isCQAdaptive.getToken()).booleanValue()));
        }
        
        physicalTime = _getExecutiveDirector().getModelTime();


        // Call the preinitialize method of the super class.
        if (_debugging) {
            _debug(getFullName(), "Preinitializing ...");
        }

        // First invoke initializable methods.
        if (_initializables != null) {
            for (Initializable initializable : _initializables) {
                initializable.preinitialize();
            }
        }

        // validate all settable attributes.
        Iterator attributes = attributeList(Settable.class).iterator();
        while (attributes.hasNext()) {
            Settable attribute = (Settable) attributes.next();
            attribute.validate();
        }
        // preinitialize protected variables.
        _currentTime = getModelStartTime();
        _stopRequested = false;
        // preinitialize all the contained actors.
        Nameable container = getContainer();
        if (container instanceof CompositeActor) {
            Iterator actors = ((CompositeActor) container).deepEntityList()
                    .iterator();
            while (actors.hasNext()) {
                Actor actor = (Actor) actors.next();
                if (_debugging) {
                    _debug("Invoking preinitialize(): ", ((NamedObj) actor)
                            .getFullName());
                }
                actor.preinitialize();
            }
        }
        if (_debugging) {
            _debug(getFullName(), "Finished preinitialize().");
        }

        // Do this here because it updates the workspace version.
        if (_sortValid != workspace().getVersion()) {
            // Reset the hashtables for actor and port depths.
            // These two variables have to be reset here because the initialize
            // method constructs them.
            _actorToDepth = null;
            _portToDepth = null;
            _computeActorDepth();
        }
    }

    /** Unregister a debug listener.  If the specified listener has not
     *  been previously registered, then do nothing.
     *  @param listener The listener to remove from the list of listeners
     *   to which debug messages are sent.
     *  @see #addDebugListener(DebugListener)
     */
    public void removeDebugListener(DebugListener listener) {
        super.removeDebugListener(listener);
    }

    /** Request the execution of the current iteration to stop.
     *  This is similar to stopFire(), except that the current iteration
     *  is not allowed to complete.  This is useful if there is actor
     *  in the model that has a bug where it fails to consume inputs.
     *  An iteration will never terminate if such an actor receives
     *  an event.
     *  If the director is paused waiting for events to appear in the
     *  event queue, then it stops waiting, and calls stopFire() for all actors
     *  that are deeply contained by the container of this director.
     */
    public void stop() {

        super.stop();
    }

    /** Request the execution of the current iteration to complete.
     *  If the director is paused waiting for events to appear in the
     *  event queue, then it stops waiting,
     *  and calls stopFire() for all actors
     *  that are deeply contained by the container of this director.
     */
    public void stopFire() {
        super.stopFire();
    }

    // FIXME: it is questionable whether the multirate FSMActor and FSMDirector
    // should be used in DE as the default? I will say NO.

    /** Return an array of suggested directors to use with
     *  ModalModel. Each director is specified by its full class
     *  name.  The first director in the array will be the default
     *  director used by a modal model.
     *  @return An array of suggested directors to be used with ModalModel.
     *  @see ptolemy.actor.Director#suggestedModalModelDirectors()
     */
    public String[] suggestedModalModelDirectors() {
        String[] defaultSuggestions = new String[2];
        defaultSuggestions[1] = "ptolemy.domains.fsm.kernel.MultirateFSMDirector";
        defaultSuggestions[0] = "ptolemy.domains.fsm.kernel.FSMDirector";
        return defaultSuggestions;
    }

    // NOTE: Why do we need an overridden transferOutputs method?
    // This director needs to transfer ALL output tokens at boundary of
    // hierarchy to outside. Without this overriden method, only one
    // output token is produced. See de/test/auto/transferInputsandOutputs.xml.
    // Do we need an overridden transferInputs method?
    // No. Because the DEDirector will keep firing an actor until it returns
    // false from its prefire() method, meaning that the actor has not enough
    // input tokens.

    /** Override the base class method to transfer all the available
     *  tokens at the boundary output port to outside.
     *  No data remains at the boundary after the model has been fired.
     *  This facilitates building multirate DE models.
     *  The port argument must be an opaque output port. If any channel
     *  of the output port has no data, then that channel is ignored.
     *
     *  @exception IllegalActionException If the port is not an opaque
     *   output port.
     *  @param port The port to transfer tokens from.
     *  @return True if data are transferred.
     */
    public boolean transferOutputs(IOPort port) throws IllegalActionException {
        boolean anyWereTransferred = false;
        boolean moreTransfersRemaining = true;

        while (moreTransfersRemaining) {
            moreTransfersRemaining = _transferOutputs(port);
            anyWereTransferred |= moreTransfersRemaining;
        }

        return anyWereTransferred;
    }
    
    @Override
    public void setModelTime(Time newTime) throws IllegalActionException {
    	super.setModelTime(newTime);
    }

    /** Invoke the wrapup method of the super class. Reset the private
     *  state variables.
     *  @exception IllegalActionException If the wrapup() method of
     *  one of the associated actors throws it.
     */
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        _disabledActors = null;
        _eventQueues.clear();
        _currentModelTime = new Time(this, 0.0);
        _noMoreActorsToFire = false;
        _microstep = 0;
    }
    
    protected final void _displaySchedule(Actor actor, double time,
            int scheduleEvent) {
        _getExecutiveDirector()._displaySchedule((Actor) getContainer(), actor, time, scheduleEvent);
    }
    
	@Override
	protected boolean _transferInputs(IOPort port) throws IllegalActionException {
		System.out.print(this.getContainer().getName() + "-ti: ");
		if (!port.isInput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,"Attempted to transferInputs on a port is not an opaque"+ "input port.");
        }
        boolean wasTransferred = false;
        
        for (int i = 0; i < port.getWidth(); i++) {
            try {
                while (port.hasToken(i)) {
                	Receiver[][] recv = port.getReceivers();
                	DDEReceiver4Ptides receiver = (DDEReceiver4Ptides) recv[0][0];
                	Token t = receiver.get();
                    Time time = receiver.getReceiverTime();
                    if ((_getExecutiveDirector().usePtidesExecutionSemantics() && _isSafeToProcess(time, port)) ||
                    		(!_getExecutiveDirector().usePtidesExecutionSemantics() && time.compareTo(physicalTime) > 0)) {
                   // if (minDelayTime != Double.MAX_VALUE && this.physicalTime.compareTo(time.subtract(minDelayTime)) < 0) {
                    	System.out.println("cannot ti yet " + time);
                    	_addSynchronizationPoint(time); // at this time, the new input should be read
                    	receiver.put(t, time);
                    	break; // couldn't transfer newest token
                    }
                    Receiver[][] farReceivers = port.deepGetReceivers();
                    if ((farReceivers == null) || (farReceivers.length <= i) || (farReceivers[i] == null)) {
                    	System.out.println("no receivers");
                        continue;
                    }
                    for (int k = 0; k < farReceivers.length; k++) {
                        for (int l = 0; l < farReceivers[k].length; l++) {
                            DEReceiver4Ptides farReceiver = (DEReceiver4Ptides) farReceivers[k][l];
                            farReceiver.put(t, time);
                            _displaySchedule((Actor) port.getContainer(), physicalTime.getDoubleValue(), ScheduleListener.TRANSFERINPUT);
                            System.out.println("	in " + t + " at " + time);
                            wasTransferred = true;
                        }
                    }
                }
            } catch (ArithmeticException ex) {
            	System.out.println("cannot ti yet");
            	return false;
            } catch (NoTokenException ex) {
                throw new InternalErrorException(this, ex, null);
            }
        }
        return wasTransferred;
	}
	


	@Override
	protected boolean _transferOutputs(IOPort port) throws IllegalActionException {
		System.out.print(this.getContainer().getName() + ":to: ");
		Token token = null;
		boolean result = false;

        if (!port.isOutput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,"Attempted to transferOutputs on a port that "+ "is not an opaque input port.");
        }
        for (int i = 0; i < port.getWidthInside(); i++) {
            try {
                if (port.hasTokenInside(i)) {
                    token = port.getInside(i);
                    Receiver[][] outReceivers = port.getRemoteReceivers();
                    for (int k = 0; k < outReceivers.length; k++) {
                        for (int l = 0; l < outReceivers[k].length; l++) {
                            DDEReceiver4Ptides outReceiver = (DDEReceiver4Ptides) outReceivers[k][l];
                            DDEThread4Ptides thread = (DDEThread4Ptides) Thread.currentThread();
                            outReceiver.put(token, ((Actor)port.getContainer()).getDirector().getModelTime());
                            _displaySchedule((Actor)port.getContainer(), physicalTime.getDoubleValue(), ScheduleListener.TRANSFEROUTPUT);
                            System.out.println("	out " + token + " at " + ((Actor)port.getContainer()).getDirector().getModelTime());
                        }
                    }
                    result = true;
                }
            } catch (NoTokenException ex) {
                throw new InternalErrorException(this, ex, null);
            }
        }
        return result;       
	}

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Put a pure event into the event queue to schedule the given actor to
     *  fire at the specified timestamp.
     *  <p>
     *  The default microstep for the queued event is equal to zero,
     *  unless the time is equal to the current time, where the microstep
     *  will be the current microstep plus one.
     *  </p><p>
     *  The depth for the queued event is the minimum of the depths of
     *  all the ports of the destination actor.
     *  </p><p>
     *  If there is no event queue or the given actor is disabled, then
     *  this method does nothing.</p>
     *
     *  @param actor The actor to be fired.
     *  @param time The timestamp of the event.
     *  @exception IllegalActionException If the time argument is less than
     *  the current model time, or the depth of the actor has not be calculated,
     *  or the new event can not be enqueued.
     */
    protected synchronized void _enqueueEvent(Actor actor, Time time)
            throws IllegalActionException {

        // Adjust the microstep.
        int microstep = 0;

        if (time.compareTo(getModelTime()) == 0) {
            // If during initialization, do not increase the microstep.
            // This is based on the assumption that an actor only requests
            // one firing during initialization. In fact, if an actor requests
            // several firings at the same time,
            // only the first request will be granted.
            if (_isInitializing) {
                microstep = _microstep;
            } else {
                microstep = _microstep + 1;
            }
        } else if (time.compareTo(getModelTime()) < 0) {
            throw new IllegalActionException(actor,
                    "Attempt to queue an event in the past:"
                            + " Current time is " + getModelTime()
                            + " while event time is " + time);
        }

        int depth = _getDepthOfActor(actor);

        if (_debugging) {
            _debug("enqueue a pure event: ", ((NamedObj) actor).getName(),
                    "time = " + time + " microstep = " + microstep
                            + " depth = " + depth);
        }

        DEEvent newEvent = new DEEvent(actor, time, microstep, depth);
        ((DEEventQueue)_eventQueues.get(actor)).put(newEvent);
        _addSynchronizationPoint(newEvent.timeStamp());
    }

    /** Put a trigger event into the event queue.
     *  <p>
     *  The trigger event has the same timestamp as that of the director.
     *  The microstep of this event is always equal to the current microstep
     *  of this director. The depth for the queued event is the
     *  depth of the destination IO port.
     *  </p><p>
     *  If the event queue is not ready or the actor contains the destination
     *  port is disabled, do nothing.</p>
     *
     *  @param ioPort The destination IO port.
     *  @exception IllegalActionException If the time argument is not the
     *  current time, or the depth of the given IO port has not be calculated,
     *  or the new event can not be enqueued.
     */
    protected synchronized void _enqueueTriggerEvent(IOPort ioPort)
            throws IllegalActionException {
        Actor actor = (Actor) ioPort.getContainer();

        int depth = _getDepthOfIOPort(ioPort);

        if (_debugging) {
            _debug("enqueue a trigger event for ", ((NamedObj) actor).getName(), " time = " + getModelTime() + " microstep = " + _microstep + " depth = " + depth);
        }

        // Register this trigger event.
        DEEvent newEvent = new DEEvent(ioPort, getModelTime(), _microstep, depth);
        if (((DEEventQueue)_eventQueues.get(actor)) != null) {// TODO why is compositeactor scheduled after timeddelay=
        	((DEEventQueue)_eventQueues.get(actor)).put(newEvent);
        	_addSynchronizationPoint(newEvent.timeStamp());
        }
    }
    
	protected synchronized void _enqueueTriggerEvent(IOPort ioPort, Time time)
	    throws IllegalActionException {
		Actor actor = (Actor) ioPort.getContainer();
		
		if ((getEventQueue() == null) 
		        || ((_disabledActors != null) && _disabledActors
		                .contains(actor))) {
		    return;
		}
		
		int depth = _getDepthOfIOPort(ioPort);
		
		DEEvent newEvent = new DEEvent(ioPort, time, _microstep, depth);
		//getEventQueue().put(newEvent);
		if (((DEEventQueue)_eventQueues.get(actor)) != null) // TODO why is compositeactor scheduled after timeddelay=
			((DEEventQueue)_eventQueues.get(actor)).put(newEvent);	
		_addSynchronizationPoint(newEvent.timeStamp());
	}
	
	private class WCETComparator implements Comparator {

		public int compare(Object arg0, Object arg1) {
			DEEvent event1 = (DEEvent) arg0;
			DEEvent event2 = (DEEvent) arg1;
			Actor actor1 = event1.actor();
			Actor actor2 = event2.actor();
			double wcet1 = EmbeddedDEDirector4Ptides.getWCET(actor1);
			double wcet2 = EmbeddedDEDirector4Ptides.getWCET(actor2);
			Time time1 = event1.timeStamp();
			Time time2 = event2.timeStamp();
			boolean fireAtRT1 = EmbeddedDEDirector4Ptides.mustBeFiredAtRealTime(actor1);
			boolean fireAtRT2 = EmbeddedDEDirector4Ptides.mustBeFiredAtRealTime(actor2);
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
					if ((!_preemptive && time2.getDoubleValue() + wcet2 < time1.getDoubleValue()) ||
							(_preemptive && time2.getDoubleValue() < time1.getDoubleValue())) {
						return 1;
					} else {
						return -1;
					}
				} else if (fireAtRT2 && !fireAtRT1) {	
//					 if execution of non real time actor can fit before real time actor
					if ((!_preemptive && time1.getDoubleValue() + wcet1 < time2.getDoubleValue()) ||
							(_preemptive && time1.getDoubleValue() < time2.getDoubleValue()))	
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


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Based on the depths of IO ports, calculate the depths of actors.
     *  The results are cached in a hashtable _actorToDepth.
     *  Update the depths of existing events in the event queue.
     */
    private void _computeActorDepth() throws IllegalActionException {
        CompositeActor container = (CompositeActor) getContainer();
        LinkedList actors = (LinkedList) container.deepEntityList();
        // Add container. 
        actors.add(container);
        int numberOfActors = actors.size();
        _actorToDepth = new Hashtable(numberOfActors);

        Iterator actorsIterator = actors.iterator();

        // The depth of an actor starts with a negative number.
        int defaultActorDepth = -numberOfActors;

        while (actorsIterator.hasNext()) {
            Actor actor = (Actor) actorsIterator.next();

            // Calculate the depth of the given actor, which is the
            // smallest depth of all the input and output ports.
            // Why?
            // Here is the example: A model with a feedback loop, which
            // contains a non-zero TimedDelay actor. When the TimedDelay actor
            // requests a refiring, the depth of the event should have the
            // depth of its output.
            // The reason to include the depths of input ports for calculation
            // is to reduce unnecessary number of firings. In particular,
            // if an actor receives a trigger event that has the same tag as
            // one of its pure events, one firing is sufficient.

            int depth = -1;
            Iterator inputs = actor.inputPortList().iterator();

            while (inputs.hasNext()) {
                IOPort inputPort = (IOPort) inputs.next();
                int inputDepth = _getDepthOfIOPort(inputPort);

                if ((inputDepth < depth) || (depth == -1)) {
                    depth = inputDepth;
                }
            }

            Iterator outputs = actor.outputPortList().iterator();

            while (outputs.hasNext()) {
                IOPort outputPort = (IOPort) outputs.next();
                int outputDepth = _getDepthOfIOPort(outputPort);

                if ((outputDepth < depth) || (depth == -1)) {
                    depth = outputDepth;
                }
            }

            // Note that if an actor has no ports, the defaultActorDepth,
            // which is a negative number, will be used such that each
            // actor has a unique depth.
            if (depth == -1) {
                depth = defaultActorDepth;
            }
            _actorToDepth.put(actor, new Integer(depth));
            // Increment the default depth value for the next actor.
            defaultActorDepth++;
        }

    }
    
    

    /** Perform a topological sort on the directed graph and use the result
     *  to set the depth for each IO port. A new Hashtable is created each
     * time this method is called.
     */
    private void _computePortDepth() throws IllegalActionException {
        DirectedAcyclicGraph portsGraph = _constructDirectedGraph();
        _verbose = true;
        if (_debugging && _verbose) {
            _debug("## ports graph is:" + portsGraph.toString());
        }

        // NOTE: this topologicalSort can be smarter.
        // In particular, the dependency between ports belonging
        // to the same actor may be considered.
        Object[] sort = portsGraph.topologicalSort();
        int numberOfPorts = sort.length;

        if (_debugging && _verbose) {
            _debug("## Result of topological sort (highest depth to lowest):");
        }

        // Allocate a new hash table with the size equal to the
        // number of IO ports sorted.
        _portToDepth = new Hashtable(numberOfPorts);

        LinkedList ports = new LinkedList();

        // Assign depths to ports based on the topological sorting result.
        for (int i = 0; i <= (numberOfPorts - 1); i++) {
            IOPort ioPort = (IOPort) sort[i];
            ports.add(ioPort);
            int depth = i;
            Actor portContainer = (Actor) ioPort.getContainer();
            // The ports of the composite actor that contains
            // this director are set to the highest depth
            // (the lowest priority).
            if (ioPort.isOutput() && portContainer.equals(getContainer())) {
                depth += numberOfPorts;
            }

            // Insert the hashtable entry.
            _portToDepth.put(ioPort, new Integer(depth));
            if (_debugging && _verbose) {
                _debug(((Nameable) ioPort).getFullName(), "depth: " + depth);
            }
        }

        if (_debugging && _verbose) {
            _debug("## adjusting port depths based "
                    + "on the strictness constraints.");
        }

        LinkedList actorsWithPortDepthsAdjusted = new LinkedList();

        // The rule is simple. If an output depends on several inputs directly,
        // all inputs must have the same depth, the biggest one.
        for (int i = sort.length - 1; i >= 0; i--) {
            IOPort ioPort = (IOPort) sort[i];

            // Get the container actor of the current output port.
            Actor portContainer = (Actor) ioPort.getContainer();

            // get the strictnessAttribute of actor.
            Attribute strictnessAttribute = ((NamedObj) portContainer)
                    .getAttribute(STRICT_ATTRIBUTE_NAME);

            // Normally, we adjust port depths based on output ports.
            // However, if this input port belongs to a sink actor, and
            // the sink actor has more than one input ports, adjust the depths
            // of all the input ports to their maximum value.
            // For exmaple, the XYPlotter in the WirelessSoundDetection demo.
            // By default, all composite actors are non-strict. However, if
            // a composite actor declares its strictness with an attribute,
            // we adjust its input ports depths to their maximum. One example
            // is the ModalModel.
            // A third case is that if a composite actor has some of its input
            // ports as parameter ports and the others as reguler IO ports,
            // we need to adjust the depths of paramter ports also.
            // The TimedSinewave (with SDF implementation) is an example.
            // Since this actor is supposed to be a strict actor, we need to
            // add a strictness marker such that the depths of all its inputs
            // are adjusted to their maximum value.
            // For non-strict composite actors, one solution is to iterate
            // each output port and find all the parameter ports that affect
            // that output port. Note that a parameter may depend on another
            // parameter at the same level of hierarchy, which makes the
            // analysis harder. One reference will be the context analysis by
            // Steve.
            // I prefer to leave the parameter analysis to be independent of the
            // function dependency analysis. 02/2005 hyzheng
            if (ioPort.isInput()) {
                boolean depthNeedsAdjusted = false;
                int numberOfOutputPorts = portContainer.outputPortList().size();

                // If an actor has no output ports, adjustment is necessary.
                if (numberOfOutputPorts == 0) {
                    depthNeedsAdjusted = true;
                }

                // If the actor declares itself as a strict actor,
                // adjustment is necessary.
                if (strictnessAttribute != null) {
                    depthNeedsAdjusted = true;
                }

                // If depth needs adjusted:
                if (depthNeedsAdjusted) {
                    List inputPorts = portContainer.inputPortList();

                    if (inputPorts.size() <= 1) {
                        // If the sink actor has only one input port, there is
                        // no need to adjust its depth.
                        continue;
                    }

                    if (actorsWithPortDepthsAdjusted.contains(portContainer)) {
                        // The depths of the input ports of this acotr
                        // have been adjusted.
                        continue;
                    } else {
                        actorsWithPortDepthsAdjusted.add(portContainer);
                    }

                    Iterator inputsIterator = inputPorts.iterator();

                    // Iterate all input ports of the sink actor.
                    int maximumPortDepth = -1;

                    while (inputsIterator.hasNext()) {
                        Object object = inputsIterator.next();
                        IOPort input = (IOPort) object;
                        int inputPortDepth = ports.indexOf(input);

                        if (maximumPortDepth < inputPortDepth) {
                            maximumPortDepth = inputPortDepth;
                        }
                    }

                    // Set the depths of the input ports to the maximum one.
                    inputsIterator = inputPorts.iterator();

                    while (inputsIterator.hasNext()) {
                        IOPort input = (IOPort) inputsIterator.next();

                        if (_debugging && _verbose) {
                            _debug(((Nameable) input).getFullName(),
                                    "depth is adjusted to: " + maximumPortDepth);
                        }

                        // Insert the hashtable entry.
                        _portToDepth.put(input, new Integer(maximumPortDepth));
                    }
                }
            }

            // we skip the ports of the container and their depths are handled
            // by the upper level executive director of this container.
            if (portContainer.equals(getContainer())) {
                continue;
            }

            // FIXME: The following is really problematic. Check the 
            // DESchedulingTest3.xml as example. 
            // Get the function dependency of the container actor
            FunctionDependency functionDependency = portContainer
                    .getFunctionDependency();

            List inputPorts = functionDependency
                    .getInputPortsDependentOn(ioPort);
            Iterator inputsIterator = inputPorts.iterator();

            // Iterate all input ports the current output depends on,
            // find their maximum depth.
            int maximumPortDepth = -1;

            while (inputsIterator.hasNext()) {
                Object object = inputsIterator.next();
                IOPort input = (IOPort) object;
                int inputPortDepth = ports.indexOf(input);

                if (maximumPortDepth < inputPortDepth) {
                    maximumPortDepth = inputPortDepth;
                }
            }

            // Set the depths of the input ports to the maximum one.
            inputsIterator = inputPorts.iterator();

            while (inputsIterator.hasNext()) {
                IOPort input = (IOPort) inputsIterator.next();

                if (_debugging && _verbose) {
                    _debug(((Nameable) input).getFullName(),
                            "depth is adjusted to: " + maximumPortDepth);
                }

                // Insert the hashtable entry.
                _portToDepth.put(input, new Integer(maximumPortDepth));
            }
        }

        if (_debugging) {
            _debug("## End of topological sort of ports.");
        }

        // the sort is now valid.
        _sortValid = workspace().getVersion();
    }

    // Construct a directed graph with nodes representing IO ports and
    // directed edges representing their dependencies. The directed graph
    // is returned.
    private DirectedAcyclicGraph _constructDirectedGraph()
            throws IllegalActionException {
        // Clear the graph
        DirectedAcyclicGraph portsGraph = new DirectedAcyclicGraph();

        Nameable container = getContainer();

        // If the container is not composite actor, there are no actors.
        if (!(container instanceof CompositeActor)) {
            return portsGraph;
        }

        CompositeActor castContainer = (CompositeActor) container;

        // Get the functionDependency attribute of the container of this
        // director. If there is no such attribute, construct one.
        FunctionDependencyOfCompositeActor functionDependency = (FunctionDependencyOfCompositeActor) castContainer
                .getFunctionDependency();

        // NOTE: The following may be a very costly test.
        //       -- from the comments of previous implementations.
        // If the port based data flow graph contains directed
        // loops, the model is invalid. An IllegalActionException
        // is thrown with the names of the actors in the loop.
        Object[] cycleNodes = functionDependency.getCycleNodes();

        if (cycleNodes.length != 0) {
            StringBuffer names = new StringBuffer();

            for (int i = 0; i < cycleNodes.length; i++) {
                if (cycleNodes[i] instanceof Nameable) {
                    if (i > 0) {
                        names.append(", ");
                    }

                    names.append(((Nameable) cycleNodes[i]).getContainer()
                            .getFullName());
                }
            }

            throw new IllegalActionException(this.getContainer(),
                    "Found zero delay loop including: " + names.toString());
        }

        portsGraph = functionDependency.getDetailedDependencyGraph()
                .toDirectedAcyclicGraph();

        return portsGraph;
    }

    /** Disable the specified actor.  All events destined to this actor
     *  will be ignored. If the argument is null, then do nothing.
     *  @param actor The actor to disable.
     */
    private void _disableActor(Actor actor) {
        if (actor != null) {
            if (_debugging) {
                _debug("Actor ", ((Nameable) actor).getName(), " is disabled.");
            }

            if (_disabledActors == null) {
                _disabledActors = new HashSet();
            }

            _disabledActors.add(actor);
        }
    }

    /** Calculate the depth of an actor.
     *  @param actor An actor whose depth is requested.
     *  @return An integer indicating the depth of the given actor.
     *  @exception IllegalActionException If any port of this actor
     *  is not sorted.
     */
    private int _getDepthOfActor(Actor actor) throws IllegalActionException {
        if ((_sortValid != workspace().getVersion()) || (_actorToDepth == null)) {
            _computePortDepth();
            _computeActorDepth();
        }

        Integer depth = (Integer) _actorToDepth.get(actor);

        if (depth != null) {
            return depth.intValue();
        } else {
            throw new IllegalActionException("Attempt to get depth of actor "
                    + ((NamedObj) actor).getName() + " that was not sorted.");
        }
    }

	private void _enqueueNextFirings(List eventsToFire) throws InvalidStateException, IllegalActionException {
		for (int i = 0; i < eventsToFire.size(); i++) {
			DEEvent event = (DEEvent) eventsToFire.get(i);
			Actor actor = event.actor();
			double wcet = getWCET(actor);
			_addSynchronizationPoint(event.timeStamp());
			_addSynchronizationPoint(event.timeStamp().add(wcet));
		}
	}
    
    /** Return the depth of an ioPort, which is the index of this ioPort in
     *  topological sort.
     *  @param ioPort An IOPort whose depth is requested.
     *  @return An int representing the depth of the given ioPort.
     *  @exception IllegalActionException If the ioPort is not sorted.
     */
    private int _getDepthOfIOPort(IOPort ioPort) throws IllegalActionException {
        if ((_sortValid != workspace().getVersion()) || (_portToDepth == null)) {
            _computePortDepth();
            _computeActorDepth();
        }

        Integer depth = (Integer) _portToDepth.get(ioPort);

        if (depth != null) {
            return depth.intValue();
        } else {
            throw new IllegalActionException("Attempt to get depth of ioPort "
                    + ((NamedObj) ioPort).getName() + " that was not sorted.");
        }
    }
    
    
    private void _getDisconnectedGraphs() throws IllegalActionException {
    	Set disconnectedgraphs = new HashSet();
    	Set equivalenceClasses = new HashSet();
    	CompositeActor actor = (CompositeActor) getContainer();
    	DirectedAcyclicGraph graph = _computeGraph();	
    	
    	Hashtable ioMapping = new Hashtable();
		for (Iterator colIt = graph.connectedComponents().iterator(); colIt.hasNext();) {
			Set actors = new HashSet();
			Set ports = new HashSet();
			for (Iterator col2It = ((Collection) colIt.next()).iterator(); col2It.hasNext();) {
				Port p = (Port) ((Node)col2It.next()).getWeight();
				if (!p.getContainer().equals(actor))
					actors.add(p.getContainer());
				else // p is an input or output port of a platform
					if (((IOPort)p).isInput())
					ports.add(p);
			}
			if (!actors.isEmpty())
				disconnectedgraphs.add(actors);
			if (!ports.isEmpty())
				equivalenceClasses.add(ports);
		}
	}
    

    
	private boolean _nothingToDoNow(List eventsToFire) {
		return !_stopRequested && (eventsToFire.isEmpty() || 
        		_allEventsToFireMustBeFiredAtLaterTime(eventsToFire));
	}

	private boolean _allEventsToFireMustBeFiredAtLaterTime(List eventsToFire) {
		Time nextRealTime = Time.POSITIVE_INFINITY;
		for (int i = 0; i < eventsToFire.size(); i++) {
			DEEvent event = (DEEvent) eventsToFire.get(i);
	    	Actor actorToFire = event.actor();
	    	if (mustBeFiredAtRealTime(actorToFire))
	    		if (event.timeStamp().compareTo(physicalTime) <= 0) // < will cause an exception - missed execution
	    			return false;
	    		else if (nextRealTime.compareTo(event.timeStamp()) > 0)
	    			nextRealTime = event.timeStamp();
		}
		for (int i = 0; i < eventsToFire.size(); i++) {
			DEEvent event = (DEEvent) eventsToFire.get(i);
	    	Actor actorToFire = event.actor();
	    	// if execution fits before next real time event
	    	if (!mustBeFiredAtRealTime(actorToFire) && physicalTime.add(getWCET(actorToFire)).compareTo(nextRealTime) < 0)
	    		return false;
		}
		return true;
    }

	private void _enqueueEventAgain(DEEvent event) throws IllegalActionException {
		Actor actor = event.actor();
		((DEEventQueue)_eventQueues.get(actor)).put(event);
	}

	private void _enqueueRemainingEventsAgain(List list) throws IllegalActionException {
		for (int i = 0; i < list.size(); i++) {
			DEEvent event = (DEEvent) list.get(i);
			_enqueueEventAgain(event);
		}
	}
	
    private List _getNextEventsToFire() throws IllegalActionException {
    	List events = new LinkedList();
    	for (Enumeration actors = _eventQueues.keys(); actors.hasMoreElements();) {
    		Actor actor = (Actor) actors.nextElement();
    		DEEventQueue queue = (DEEventQueue) _eventQueues.get(actor);
    		if (!queue.isEmpty()) {
    			DEEvent event = queue.get();
    			while (!queue.isEmpty() && queue.get().timeStamp().equals(event.timeStamp()) && 
    					_isSafeToProcess(event.timeStamp(), (NamedObj)actor)) {
    				events.add(queue.get());
    				queue.take();
    			} 
    		}
    	}
    	// sort events according to their execution times
    	Collections.sort(events, new WCETComparator());
    	System.out.println(this.getContainer().getName() + ": " + events.size() + " " + events);
    	return events;
    }
	
	private boolean _isSafeToProcess(Time time, NamedObj object) {
		double minDelayTime = getMinDelayTime(object);
		return minDelayTime == Double.MAX_VALUE ||
			time.subtract(minDelayTime)
				.subtract(_getExecutiveDirector().getClockSyncError())
				.subtract(_getExecutiveDirector().getNetworkDelay())
				.compareTo(physicalTime) >= 0;
	}


    private void _waitForPhysicalTime(String reason) throws IllegalActionException {
    	System.out.println(getContainer().getName() + ": " + reason);
    	if (_transferAllInputs())
    		return;
        physicalTime = _getExecutiveDirector().waitForFuturePhysicalTime();
        System.out.println("+" + this.getContainer().getName() +" " + physicalTime);
        _transferAllInputs();
	}

	private DEDirector4Ptides _getExecutiveDirector() {
		return (DEDirector4Ptides) ((Actor)getContainer()).getExecutiveDirector();
	}
    
	private void _addSynchronizationPoint(Time time) throws IllegalActionException {
		_getExecutiveDirector().requestRefiringAtPhysicalTime(time);
	}

    
    private double _getMinDelay(boolean forward, Node node, DirectedAcyclicGraph graph, double minDelay, Set traversedEdges) throws IllegalActionException {
    	System.out.println("fw");
    	IOPort port = (IOPort) (node).getWeight();
    	Collection inputs;
    	if (forward)
    		inputs = graph.outputEdges(node);
    	else
    		inputs = graph.inputEdges(node);
		if (inputs.size() == 0)
			return Integer.MAX_VALUE;
		Iterator inputIt = inputs.iterator();
		while (inputIt.hasNext()) {
			double delay = 0.0;
			Edge edge = (Edge) inputIt.next();
			Node nextNode;
			if (forward)
				nextNode = edge.sink();
			else
				nextNode = edge.source();
			Actor inputActor = (Actor)((IOPort) nextNode.getWeight()).getContainer();
			if (!traversedEdges.contains(edge)) {
				traversedEdges.add(edge);
				if (inputActor instanceof TimedDelay) {
					TimedDelay delayActor = (TimedDelay) inputActor;
					delay = ((DoubleToken) (delayActor.delay.getToken())).doubleValue();
					if (forward)
						nextNode = graph.node(delayActor.output);
					else
						nextNode = graph.node(delayActor.input);
				} 
				if (isSensor(inputActor)) {
					return delay;
				} else if (inputActor instanceof Clock) { // assume periodic clock
					// TODO
					Clock clock = (Clock) inputActor;
					delay = ((DoubleToken) clock.period.getToken()).doubleValue() / ((ArrayToken) clock.offsets.getToken()).length();
				} else if (inputActor.equals((CompositeActor) getContainer()))
					return delay;
				delay += _getMinDelay(forward, nextNode, graph, minDelay, traversedEdges); 
			}
			if (delay < minDelay)
				minDelay = delay;
		}
		return minDelay;
    }
    

    
    private boolean _isInputConnectedToAnyOutput(DirectedAcyclicGraph graph, IOPort in) {
    	CompositeActor actor = (CompositeActor) getContainer();
    	Collection reachableNodes = graph.reachableNodes(graph.node(in));
		for (Iterator it2 = actor.outputPortList().iterator(); it2.hasNext(); ) {
			IOPort out = (IOPort) it2.next();
			if (reachableNodes.contains(graph.node(out))) {
				return true;
			}
		}
		return false;
	}
    

    



    
    private DirectedAcyclicGraph _computeGraph() {
    	CompositeActor actor = (CompositeActor) getContainer();
    	FunctionDependencyOfCompositeActor functionDependency = (FunctionDependencyOfCompositeActor) (actor).getFunctionDependency();
		DirectedAcyclicGraph graph = functionDependency.getDetailedDependencyGraph().toDirectedAcyclicGraph();
		
    	// add edges between timeddelay inputs and outputs
		for (Iterator nodeIterator = graph.nodes().iterator(); nodeIterator.hasNext();) {
    		IOPort port = (IOPort) ((Node)nodeIterator.next()).getWeight();
    		Actor a = (Actor) port.getContainer();
    		if (port.isOutput() && a instanceof TimedDelay) 
    			graph.addEdge(a.inputPortList().get(0), a.outputPortList().get(0));
    	}
		// remove edges between actors and sensors
		for (Iterator nodeIterator = graph.nodes().iterator(); nodeIterator.hasNext();) {
			IOPort sinkPort = (IOPort) ((Node)nodeIterator.next()).getWeight();
    		Actor a = (Actor) sinkPort.getContainer();
    		if (isSensor(a) && sinkPort.isInput()) {
    			Collection edgesToRemove = new java.util.ArrayList();
    			for (Iterator it = graph.inputEdges(graph.node(sinkPort)).iterator(); it.hasNext(); ) {
    				Edge edge = (Edge)it.next();
    				edgesToRemove.add(edge);
    			}
    			for (Iterator it = edgesToRemove.iterator(); it.hasNext(); ) {
    				graph.removeEdge((Edge) it.next());
    			}
    		}
		}
    	return graph;
    }

    
	private void _initialize() {
		try {
			preemptive = new Parameter(this, "preemptive");
			preemptive.setExpression("false");
			preemptive.setTypeEquals(BaseType.BOOLEAN);
		} catch (KernelException e) {
            throw new InternalErrorException("Cannot set parameter:\n"
                    + e.getMessage());
        }
	}
	
    private boolean _transferAllInputs() throws IllegalActionException {
    	boolean inputTransferred = false;
    	for (Iterator inputPorts = ((Actor)getContainer()).inputPortList().iterator(); inputPorts.hasNext() && !_stopRequested;) {
            IOPort p = (IOPort) inputPorts.next();
            if (p instanceof ParameterPort) {
                ((ParameterPort) p).getParameter().update();
            } else {
            	inputTransferred = transferInputs(p);
            }
        }
    	return inputTransferred;
    }

    private boolean _transferAllOutputs() throws IllegalActionException {
    	boolean transferedOutputs = false;
    	Iterator outports = ((Actor)getContainer()).outputPortList().iterator();
        while (outports.hasNext() && !_stopRequested) {
            IOPort p = (IOPort) outports.next();
            transferedOutputs = transferedOutputs | _transferOutputs(p);
        }
        return transferedOutputs;
    }
    

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** A hashtable that caches the depths of actors. */
    private Hashtable _actorToDepth = null;

    /** The set of actors that have returned false in their postfire()
     *  methods. Events destined for these actors are discarded and
     *  the actors are  never fired.
     */
    private Set _disabledActors;
    
    private Set equivalenceClasses;
    
	private Time _currentModelTime;
	private Time physicalTime;
	private Time nextRealTimeEvent = Time.POSITIVE_INFINITY;
    
    private Hashtable _eventQueues;

    /** Set to true when the time stamp of the token to be dequeue
     *  has exceeded the stopTime.
     */
    private boolean _exceedStopTime = false;

    /** A local boolean variable indicating whether this director is in
     *  initialization phase execution.
     */
    private boolean _isInitializing = false;

    /** The current microstep. */
    private int _microstep = 0;

    /**
     * Set to true when it is time to end the execution.
     */
    private boolean _noMoreActorsToFire = false;

    /** A hashtable that caches the depths of ports. */
    private Hashtable _portToDepth = null;

    /** The real time at which the model begins executing. */
    private long _realStartTime = 0;

    /** Indicator of whether the topological sort giving ports their
     *  priorities is valid.
     */
    private long _sortValid = -1;

    /** The name of an attribute that marks an actor as strict. */
    private static final String STRICT_ATTRIBUTE_NAME = "_strictMarker";    
	
}
